/*
 * 
 */
package jbbs;
import tktools.*;
import database.DbConnectionBroker;
import epml.*;
import framework.LOG;
import framework.Param;
import framework.SuperPlayer;

import	java.util.*;
import	java.io.*;

/**
 	#
	# #######
	#   post
	# #######
	#
	<program $jbbs.BbsPost>
		<dispatch  html=PostList.html    number=3310       class=jbbs.BbsPost />
		<variable>
		  <receive  NUMBER   STAMP  GROUP  BBS_OWNER_KEY  UID MAIL UNAME  HOMEURL DIVISION forumkey threadkey />
		  <accept   SUBMIT   sortmode  postkey  />
		  <keep      />
	   </variable>
	</program>

 */
public class BbsPost extends SuperPlayer implements BbsVar{
	
	
	BbsConf				conf;
	DbConnectionBroker	broker;
	//
	String				szDB;
	String				ownerkey;		// BBS_OWNER_KEY
	String				postOwnerId;	// UID
	String				userMail;
	String				userName;
	String				userUrl;
	
	String				forumkey;
	String				threadkey;
	

	/** ここで作成するキー */
	String				bbskey;				// userid_forumkey_threakey
	String				owner_forumkey;		// ownerkey_forumkey

	
	/* ************ 
	 *  作業用変数
	 **************/
	/** 
	 * スレッドに含まれる記事の数
	 */
	int		postCount;
	/**
	 *  プログラムリターンコード 
	 */
	String		ret;
	/**
	 * 次の処理での表示モード 
	 */
	String		disp_mode;
	/**
	 *  処理分岐コード
	 */
	String 		cmd;
	/**
	 * データベースオブジェクト
	 */
	BbsPostDB	db;

		
	public	BbsPost(){
		super();
		if(LOG.fa) LOG.println("■ BbsPost #コンストラクタ");

	}
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		if(LOG.fa) LOG.println("■ BbsPost #initialize()");
		super.setInit(out,htb,para);
		/*
		 * コンフィギュレーション情報を作成
		 */
		conf			=	createBbsConf(para.get("BBS_CONF_PATH"),htb);		
		broker			=	(DbConnectionBroker)htb.get(BROKER);

		/*
		 * 基本情報を取り出す
		 */		
		szDB			=	strHash(htb, GROUP);
		ownerkey		=	strHash(htb,BBS_OWNER_KEY);
		postOwnerId		=	Gear.strHash(htb,UID);	// アクセスしている人のID（常にある）
		userName		=	Gear.strHash(htb,UNAME);
		userMail		=	Gear.strHash(htb,MAIL);
		userUrl			=	Gear.strHash(htb,HOMEURL);
		forumkey		=	strHash(htb,BbsForumDB.FORUM_KEY);
		threadkey		=	strHash(htb,BbsThreadDB.THREAD_KEY);
		/*
		 * 必要なキー値を作成
		 */
		bbskey			=	BbsKeys.bbsKey(ownerkey, forumkey, threadkey);
		owner_forumkey	=	BbsKeys.ownerForumKey(ownerkey, forumkey);
		htb.put(BbsThreadDB.OWNER_FORUM_KEY ,bbskey);
		htb.put(BbsPostDB.BBS_KEY			,bbskey);
		/*
		 * データベースオブジェクトを作成しておく
		 */		
		db				=	new BbsPostDB(szDB, broker);
		/*
		 * display() で使うレコード数
		 */
		postCount		=	0;

	}

	/**
	 * jbbsライブラリのためのシステムコンフィギュレーションオブジェクトを作成する
	 */
	public BbsConf	createBbsConf(String BbsConfPath, Hashtable ht){
		if(LOG.fa) LOG.println("■ BbsPost #createBbsConf()");
		/* 
		 * BBSコンフィギュレーションクラス 2004.80.15
		 */
		String	 serverIP	= strHash(ht,"_server_ip");
		BbsConf  conf       = null;
		
		try{
			conf  = new BbsConf(BbsConfPath,serverIP);
		}catch(IOException e){
			errPrint(out,"初期化処理で設定ファイルが読めません.<p>");
		}
		return	conf;	
	}
	/**
	 * WEBから受け取ったキーで該当する処理を行うコントローラー
	 * 表示処理はコントローラーが行うので終了時にはリターンコードを返すと共に
	 * 次に表示するWebの表示モード（Param.DISP_KEY）をシステムハッシュにセットする．
	 */
	public	String	dispatch(){
		if(LOG.fa) LOG.outHash(htb,"■■ BbsPost #dispatch()");
		
		/*
		 * 処理分岐を判断するキーはシステムハッシュからParam.DISPATCH_KEYをキーとして取り出す
		 * リターンコードと次に表示するWEBの表示モードは設定忘れを防ぐため規定値をセットしておく
		 * 
		 * disp_mode とは
		 * 　　次にこの画面を表示するときの表示モード．
		 * 　　あるいは次に起動するプログラムで使われる表示モード
		 *　（注）
		 * 　　他のプログラムからは display(boolean edit_mode) へ直接復帰する．
		 * 　　そのとき、edit_mode はコントローラーによってDISP_EDIT に設定されている 
		 * 
		 */
		cmd			=	strHash(htb,DISPATCH_KEY);
		ret			=	DISPATCH_DEFAULT;	// WEB表示
		disp_mode	=	DISP_NEW;			// クリアーして新規表示
		/*
		 * VIEW は postMkUserInfo.html から起動される
		 * postMkUserInfo.html は top.html などこのクラスを起動したいクラスが呼び出す．
		 * パラメータを親Htmlから受け取り、menu に VIEW をいれたのち submit()するもの．
		 */
		if(cmd.equals("VIEW")){
			/*
			 * VIEW 別ウィンドウとして起動される時のためのもの
			 */
			disp_mode	=	DISP_NEW;
			ret			=	DISPATCH_DEFAULT;	// WEB表示
			
		}else if(cmd.equals("CREATE")){
			/*
			 * スレッドについて投稿する
			 */
			if(!isActive()){
				htb.put(MESSAGE,"★ フォーラムが閉じらているので作成できません");
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;	// WEB表示				
			}else{			
			    String	msg	=	permissionCreate();
			    if(Gear.isEmpty(msg)){
			        /*
					 * 新規ポストパラメータ
					 */
					boolean	result	=	postNewArtcle();
					/*
					 * うまくパラメータをセットできたとき
					 * スレッドレコードを読んでユーザーIDを得てメールで知らせるためのパラメータをセットする
					 */
					if(result){
					    Hashtable	wht	=	getThreadRecord();
					    makeNotice( getParameter(wht, BbsThreadDB.USERID) );
					}
					
				}else{
					htb.put(MESSAGE,msg);
					disp_mode	=	DISP_EDIT;
					ret			=	DISPATCH_DEFAULT;	// WEB表示
				}
			}
			
		}else if(cmd.equals("QUOTE_TOP")){
			/*
			 * スレッドを引用して投稿する
			 */
			if(!isActive()){
				htb.put(MESSAGE,"★ フォーラムが閉じらているので作成できません");
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;	// WEB表示				
			}else{			
			    String	msg	=	permissionCreate();
			    if(Gear.isEmpty(msg)){
				    /*
					 * ポストトップのスレッドを引用してポストするためのパラメータをセットする
					 * スレッドレコードを返す
					 */
				    Hashtable	wht	=	postOnTopArtcle();
					/*
					 * うまくパラメータをセットできたとき
					 * スレッドレコードを読んでスレッドのユーザーIDを得，
					 * メールで知らせるためのパラメータをセットする
					 */
				    if(wht!=null){
				        makeNotice( getParameter(wht, BbsThreadDB.USERID) );
				    }
					
				}else{
					htb.put(MESSAGE,msg);
					disp_mode	=	DISP_EDIT;
					ret			=	DISPATCH_DEFAULT;	// WEB表示
				}
			}
			
		}else if(cmd.equals("QUOTE")){
		    /*
		     * 投稿に対して投稿する
		     */
			if(!isActive()){
				htb.put(MESSAGE,"★ フォーラムが閉じらているので作成できません");
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;	// WEB表示				
			}else{			
			    String	msg	=	permissionCreate();
			    if(Gear.isEmpty(msg)){
				    /*
					 * postkey の記事を引用してポストするためのパラメータをセットする
					 * 引用先のレコードを返す
					 */
				    Hashtable	wht	=	postOnThisArtcle();
					/*
					 * うまくパラメータをセットできたとき
					 * postレコードを読んで引用先記事のユーザーIDを得，
					 * メールで知らせるためのパラメータをセットする
					 */
				    if(wht!=null){
				        makeNotice( getParameter(wht, BbsPostDB.USERID) );
				    }
					
				}else{
					htb.put(MESSAGE,msg);
					disp_mode	=	DISP_EDIT;
					ret			=	DISPATCH_DEFAULT;	// WEB表示
				}
			}
			
		}else if(cmd.equals("QUOTE_THIS")){
		    /*
		     * 投稿に対して投稿する
		     */
			if(!isActive()){
				htb.put(MESSAGE,"★ フォーラムが閉じらているので作成できません");
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;	// WEB表示				
			}else{			
			    String	msg	=	permissionCreate();
			    if(Gear.isEmpty(msg)){
				    /*
					 * postkey の記事を引用してポストするためのパラメータをセットする
					 * 引用先のレコードを返す
					 */
				    Hashtable	wht	=	postOnThisArtcle(false);
					/*
					 * うまくパラメータをセットできたとき
					 * postレコードを読んで引用先記事のユーザーIDを得，
					 * メールで知らせるためのパラメータをセットする
					 */
				    if(wht!=null){
				        makeNotice( getParameter(wht, BbsPostDB.USERID) );;
				    }
					
				}else{
					htb.put(MESSAGE,msg);
					disp_mode	=	DISP_EDIT;
					ret			=	DISPATCH_DEFAULT;	// WEB表示
				}				
			}

		}else if(cmd.equals("EDIT")){
			/*
			 * active でなければ編集できない
			 */
			if(!isActive()){
				htb.put(MESSAGE,"★ フォーラムが閉じらているので編集できません");
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;	// WEB表示				
			}else{
				/*
				 * このスレッドを編集する
				 */
				String	postkey	=	Gear.strHash(htb, BbsPostDB.POST_KEY);	
				if(permission( postkey )){
					/*
					 * 既にポストした記事を編集する
					 * postkey はシステムハッシュに入っている
					 * ただし，記事が削除されてないか検査してから
					 */
				    Hashtable	rec			=	readRecord(postkey);//あるか
				    if(rec!=null){
				        editArtcles();
				    }else{
						ret			=	DISPATCH_DEFAULT;	// WEB表示
						disp_mode	=	DISP_NEW;			// クリアーして新規表示
				    }

				}else{
					htb.put(MESSAGE,"★ 編集する権利がありません");
					disp_mode	=	DISP_EDIT;
					ret			=	DISPATCH_DEFAULT;	// WEB表示
				}
			}
						
		}else if(cmd.equals("DELETE")){

			/*
			 * active でなければ削除できない
			 */
			if(!isActive()){
				htb.put(MESSAGE,"★ フォーラムが閉じらているので編集できません");
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;	// WEB表示				
			}else{
				/* 
				 * 既にポストした記事を削除する
				 */
				String	postkey	=	Gear.strHash(htb, BbsPostDB.POST_KEY);	
				if(permission( postkey )){
				    /*
				     * 記事があるかどうかテストしてから削除する
				     * 
				     */
				    Hashtable	rec			=	readRecord(postkey);//あるか
				    if(rec!=null){
						delete(postkey);
						
				    }			    
					disp_mode	=	DISP_EDIT;
					ret			=	DISPATCH_DEFAULT;
				}else{
					htb.put(MESSAGE,"★ 削除する権利がありません");
					disp_mode	=	DISP_EDIT;
					ret			=	DISPATCH_DEFAULT;	// WEB表示
				}
			}
		}else if(cmd.equals("RETURN")){
			/* 
			 * 呼び出し元プログラムへ戻る
			 */ 
			ret	=	DISPATCH_RETURN;

		}else{
			/* 
			 * cmd には起動したいクラスキーが入っている（$jbbs.･･･）
			 * 該当のクラスを起動する
			 */
			disp_mode	=	DISP_NEW;
			ret	=	cmd;
		}
		/*
		 * 次に表示するときあるいはリターンしてきた時の表示モードをセットしてからリターンする
		 */
		htb.put(DISP_KEY,disp_mode);
		return	ret;
	}
	/**
	 *	ゲストの書き込みかどうか 
	 */
	String	permissionCreate(){
		Hashtable	user	=	getInfoRecord(getParameter(UID));
		String	division	=	Gear.strHash(user, BbsInfoDB.DIVISION);
		if( division.equals( BbsInfoDB.GUEST) )		return	"★ ゲストの書き込みはできません";
		return	"";		
	}
	/**
	 * このスレッドリストにアクセスしているユーザーが、特定のスレッドの
	 * 更新・削除権を持つかどうか判定する
	 * @param 		threadkey
	 * @return		権利があるとき true
	 */
	boolean	permission(String postkey){
		if(LOG.fa) LOG.println("■ BbsPost #permission()");
		/*
		 * アクセスしているユーザーIDが、ポスト記事所有者IDと同じか、
		 * あるいはフォーラム所有者IDと同じならば、更新・削除権がある
		 */
		String	realOwnerId	=	getRealOwnerId(postkey);
		if(realOwnerId.equals(postOwnerId))	return	true;
		if(ownerkey.equals(postOwnerId))		return	true;
		
		/*
		 * スーパーユーザーは常に更新・削除できる 
		 */
		Hashtable	user	=	getInfoRecord(postOwnerId);
		if(user==null)			return	false;
		String	division	=	Gear.strHash(user, BbsInfoDB.DIVISION);
		if( division.equals( BbsInfoDB.SUPER) )	return	true;	// super user		
		
		return	false;
		
	}
	/**
	 * ポスト記事の所有者IDを得る
	 * @param threadkey
	 * @return
	 */
	String	getRealOwnerId(String postkey){
		if(LOG.fa) LOG.println("■ BbsPost #getRealOwnerId()");
		
		Hashtable	record	=	readRecord(postkey);
		return		Gear.strHash(record, BbsPostDB.USERID);
	}

	/**
	 * 所属するフォーラムはアクティブか
	 * @return
	 */
	boolean	isActive(){
		return	(getActiveFlag()).equals(BbsForumDB.ACTIVE);
	}
	/**
	 * 属するファーラムがアクティブかどうか、フラグを得る
	 * @param forumkey
	 * @return
	 */
	String	getActiveFlag(){
		if(LOG.fa) LOG.println("■ BbsPost #getActiveFlag()");
		
		Hashtable	record	=	readForumRecord(forumkey);
		return		Gear.strHash(record, BbsForumDB.ALIVE_FLAG);
	}
	
	/**
	 * 引用無しで記事を作成するためのパラメータをセットする
	 *
	 */
	boolean	postNewArtcle(){
		if(LOG.fa) LOG.println("■ BbsPost #postNewArtcle()");
		/*
		 * 記事が削除されたのにポストする可能性はある
		 * スレッドヘッダがあるかどうかチェックする
		 */
		Hashtable	rec	=	getThreadRecord();
		if(rec==null){
		    return	false;
		}
		/*
		 * 引用無しなし．
		 * 引用文、引用キー、リンクはない
		 */
		htb.put(QUOTED		, "");
		htb.put(QUOTED_KEY	, "");
		htb.put(PAST_LINK	, "");

		htb.put(BbsPostDB.POST_KEY, "");
		disp_mode	=	DISP_NEW;
		ret			=	CREATE_POST;
		return	true;
		
	}
	/**
	 * スレッドの本文を引用して記事を作成するためのパラメータをセットする
	 *
	 */
	Hashtable	postOnTopArtcle(){
		if(LOG.fa) LOG.println("■ BbsPost #postOnTopArtcle()");
		/*
		 * 引用文をスレッドからとりだす
		 * 引用キーとリンクはない（ "" ） 
		 */
		Hashtable	rec	=	getThreadRecord();
		/*
		 * 記事が削除されたのにポストする可能性はある
		 */
		if(rec==null){
		    return	null;
		}		
		htb.put(QUOTED, getThreadArticle(Gear.strHash(rec,BbsThreadDB.CONTENT)));
		htb.put(QUOTED_KEY	, "");
		htb.put(PAST_LINK	, "");

		htb.put(BbsPostDB.POST_KEY	, "");
		disp_mode	=	DISP_NEW;
		ret			=	CREATE_POST;
		
		/*
		 * スレッドレコードを返す
		 */
		return	rec;
		
	}
	/**
	 * 他の記事を引用して記事を作成するためのパラメータをセットする
	 *
	 */
	Hashtable	postOnThisArtcle(){
	    return	postOnThisArtcle(true);
	    
	}
	/**
	 * 他の記事について記事を作成するためのパラメータをセットする
	 * atricle の値が true のときのみ記事の本文をあらかじめ付加する
	 * 
	 * @param article
	 * @return
	 */
	Hashtable	postOnThisArtcle(boolean article){
		if(LOG.fa) LOG.println("■ BbsPost #postOnThisArtcle( boolean article)");
		/*
		 *  引用記事のpostkey、本文、link をセットする
		 */
		String		quotedkey	=	Gear.strHash(htb,BbsPostDB.POST_KEY);
		Hashtable	rec			=	readRecord(quotedkey);
		/*
		 * 記事が削除されたのにポストする可能性はある
		 */
		if(rec==null){
		    return	null;
		}
		/*
		 * システムハッシュにセットする
		 */
		if(article){
		    htb.put(QUOTED	, getPostedArticle(Gear.strHash(rec, BbsPostDB.CONTENT)));
		}else{
		    htb.put(QUOTED	, "");
		}
		htb.put(QUOTED_KEY	, quotedkey);
		htb.put(PAST_LINK	, Gear.strHash(rec, BbsPostDB.LINK));
		//
		htb.put(BbsPostDB.POST_KEY, "");

		disp_mode	=	DISP_NEW;
		ret			=	CREATE_POST;
		/*
		 * 引用した記事レコードを返す
		 */
		return	rec;

	}
	/** 
	 * bbskey と postkey を指定してひとつのレコードを読み結果をハッシュにいれて返す
	 * レコードがなかった場合戻り値は null
	 */
	public	Hashtable	readRecord( String postkey){
		if(LOG.fa)	LOG.println("■ BbsPost #readRecord() ひとつのレコードを読み結果をハッシュにいれて返す");
			
		Hashtable	tb	=	new	Hashtable(20);
		int		n	=	db.readBbsPost(bbskey, postkey, tb);
		if(n==0)	return	null;
		return	tb;
	}
	/**
	 * 記事を編集する
	 *
	 */
	void	editArtcles(){
		if(LOG.fa) LOG.println("■ BbsPost #editArtcles()");

		disp_mode	=	DISP_NEW;
		ret			=	CREATE_POST;
		
	}
	/**
	 * 引用する記事（スレッドトップ記事）の本文を返す
	 * 
	 * @param key
	 * @return
	 */
	String	getThreadArticle(String content){
		if(LOG.fa) LOG.println("■ BbsPost #getThreadArticle()");
		
		StringBuffer	buf	=	new StringBuffer();
		buf.append("(#c=#999999)" + CR);
		buf.append(content +  CR);
		buf.append("(/c)" + CR);
		
		return	buf.toString();
	}
	/**
	 * 引用する記事（ポストされた記事）の本文を返す
	 * @param key
	 * @return
	 */
	String	getPostedArticle(String content){
		if(LOG.fa) LOG.println("■ BbsPost #getPostedArticle()");

		StringBuffer	buf	=	new StringBuffer();
		buf.append("(#c=#999999)" + CR);
		buf.append(content +  CR);
		buf.append("(/c)" + CR);
		
		return	buf.toString();
	}
	/**
	 * スレッドレコードを得る
	 * 存在しない時はnullを返す
	 * @return
	 */
	Hashtable	getThreadRecord(){
		if(LOG.fa) LOG.println("■ BbsPost #getThreadRecord()");
		
		Hashtable	tb		=	new	Hashtable(20);
		BbsThreadDB	thDB	=	new 	BbsThreadDB(szDB, broker);
		int	n			=	thDB.readBbsThreadRecord(BbsKeys.ownerForumKey(ownerkey, forumkey), threadkey, tb);
		if(n==0)	return	null;
		return	tb;
		
	}
	/**
	 * 記事を削除する
	 * @param postkey
	 */
	void	delete(String postkey){
		if(LOG.fa) LOG.println("■ BbsPost #delete()");
		
		deleteArticle(postkey);	// 記事
		deleteFiles(postkey);	// ファイル
		
	}
	/**
	 * 記事をデータベースから削除する
	 *
	 */
	void	deleteArticle(String postkey){
		if(LOG.fa) LOG.println("■ BbsPost #deleteArticle()");
		
		db.deleteBbsPost(bbskey, postkey);
		return;
	}
	/**
	 * 記事に関連したファイルをディレクトリごと削除する
	 *
	 */
	void	deleteFiles(String postkey){
		if(LOG.fa) LOG.println("■ BbsPost #deleteFiles()");

		String	dir	=	conf.postDir(ownerkey, forumkey, threadkey, postkey);
		Gear.deleteDir(new File(dir));
			
	}
	/**
	 * 記事に関連して投稿があったことを知らせる
	 * @param address
	 */
	void	makeNotice(String	id){
	    
	    Hashtable	tbl		=	readForumRecord(forumkey);
	    String	forumName	=	"フォーラム：【" + (String)tbl.get(BbsForumDB.SUBJECT) + "】";
	    
	    String	to			=	getMailAdr(id);
	    String	from		=	para.bbsAdminMail();
	    String	title		=	"フォーラムのあなたの記事に投稿がありました";
	    String	body		=	forumName + CR + DateGear.getDate() + " に" + postOwnerId + " さんから投稿がありました";
	    
	    putParameter("mail_to", to);
	    putParameter("mail_from", from);
	    putParameter("mail_title", title);
	    putParameter("mail_body", body);
	    
	    //Gear.send_To_email(to,from,title,body,para);
	    
	}
	/**
	 * メールアドレスを得る
	 * 
	 * @param id
	 * @return
	 */
	String	getMailAdr(String	id){
	    BbsInfoDB	info	=	new	BbsInfoDB(szDB, broker);
	    String		mail	=	info.getMailAddress(id);
	    return		mail;
	    
	}
	/*////////////////////////////////////////////////////////////////////////////
	 * 
	 *    表 示 処 理
	 * 
	 * 　　    以下はコントローラーが呼び出す表示メソッドである．
	 * 　　    一般には、このクラス内のメソッドから直接呼び出さない．
	 * 　　   （呼び出しが出来ない訳ではない．）
	 * 
	 *////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 出力処理
	 */
	public  void	display(boolean editmode){
		if(LOG.fa) LOG.outHash(htb,"■■ BbsPost #display(boolean editmode)");
		if(!editmode){
			htb.put(MESSAGE,"");
		}
		/*
		 * フォーラム情報（ロックアイコン、ハンドルフラグ）をシステムハッシュにセット
		 */
		setForumInfo();
		/*
		 * トップアーティクルをシステムハッシュにセット
		 */
		setTopArticle();
		/* 
		 * 表示
		 * strHash(htb,DISPFILE)にはファイルの完全パス名が入っている
		 */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v,htb);			
	}
	/**
	 * フォーラム情報（）をシステムハッシュにセット
	 *
	 */
	void	setForumInfo(){
		if(LOG.fa) LOG.println("■ BbsPost #setForumInfo()");

		/*
		 * フォーラム情報を取得して、ロックアイコンとハンドルフラグを得る
		 */
		Hashtable	tbl			=	readForumRecord(forumkey);
		String		alive		=	Gear.strHashSP(tbl,(BbsForumDB.ALIVE_FLAG));
		String		signIcon	=	"lockIconOpen.gif";
		if(alive.equals(BbsForumDB.HIDDEN)||alive.equals(BbsForumDB.CLOSED)){
			signIcon	=	"lockIcon.gif";
		}
		htb.put("_signIcon", signIcon);
		/*
		 * 名前で表示するかハンドルで表示するかハンドルフラグを得ておく
		 */
		String		handleFlag	=	Gear.strHash(tbl,BbsForumDB.HANDLE_FLAG);
		htb.put(BbsForumDB.HANDLE_FLAG, handleFlag);
			
	}
	/** 
	 * ownerkey と forumkey を指定してひとつのレコードを読み結果をハッシュにいれて返す
	 * レコードがなかった場合戻り値は null
	 * @param ownerkey	オーナーキー
	 * @param forumkey	フォーラムキー
	 * @param db		フォーラムデータベース操作オブジェクト
	 * @return			レコードを格納したハッシュ
	 */
	public	 Hashtable	readForumRecord(String forumkey){
		if(LOG.fa)	LOG.println("■ BbsForumRecord #readRecord() ひとつのレコードを読み結果をハッシュにいれて返す");
			
		Hashtable	tb	=	new	Hashtable(20);
		int	n		=	(new BbsForumDB(szDB, broker)).readBbsForums(ownerkey, forumkey, tb);
		if(n==0)	return	null;
		return	tb;
	}

	/**
	 * スレッドトップの記事をシステムハッシュに入れる
	 * @param rec
	 * @param handleFlag
	 */
	void	setTopArticle(){
		if(LOG.fa) LOG.println("■ BbsPost #setTopArticle()");
		/*
		 * システムハッシュから既にセットしてあるハンドルフラグを取り出す
		 */
		String		handleflag	=	Gear.strHash(htb, BbsForumDB.HANDLE_FLAG);	
		/*
		 * 表示しようとしているトップレコード（＝スレッド）の情報を得て
		 * システムハッシュにセットする
		 */
		Hashtable	record		=	getThreadRecord();
		setTopArticleSub(record, handleflag);
	}
	/**
	 * スレッドレコードをトップの記事としてシステムハッシュに入れる
	 * @param rec
	 * @param handleFlag
	 */
	void	setTopArticleSub(Hashtable rec, String handleFlag){
		if(LOG.fa) LOG.println("■ BbsPost #setTopArticleSub()");
		/*
		 * スレッド関係の表示データ
		 */
		String	threadkey		=	Gear.strHashSP(rec,(BbsThreadDB.THREAD_KEY));
		String	uid				=	Gear.strHashSP(rec,(BbsThreadDB.USERID));
		String	date			=	Gear.strHashSP(rec,(BbsThreadDB.DATE));
		String	rating			=	Gear.strHashSP(rec,(BbsThreadDB.RATING));
		String	subject			=	Gear.strHashSP(rec,(BbsThreadDB.SUBJECT));
		String	content			=	Gear.strHashSP(rec,(BbsThreadDB.CONTENT));
		String	attach			=	Gear.strHashSP(rec,(BbsThreadDB.ATTACHMENT));
		String	views			=	Gear.strHashSP(rec,(BbsThreadDB.VIEWS));
		/*
		 * 表示名とフェースアイコンをシステムハッシュにセットする
		 */
		setUserInfo(uid, handleFlag);
		/*
		 * ハッシュに設定する
		 */ 		
		htb.put(BbsThreadDB.THREAD_KEY	, threadkey);
		htb.put(BbsThreadDB.DATE		, BbsUtil.getFormattedDate(date));
		htb.put(BbsThreadDB.RATING		, (!Gear.isEmpty(rating) ? rating : "0"));
		htb.put(BbsThreadDB.SUBJECT		, subject);
		htb.put(BbsThreadDB.CONTENT		, getTopContent(content, forumkey, threadkey));
		htb.put(BbsThreadDB.VIEWS		, (!Gear.isEmpty(views) ? views : "0"));
		htb.put(BbsThreadDB.ATTACHMENT	, getTopAttachmentHTML(rec));		
		
	}
	/**
	 * PMLをパースしたHTMLを返す
	 * @param content	内容を表すPML
	 * @return			同 HTML
	 */
	public	String	getTopContent(String content, String forumkey, String threadkey){
		if(LOG.fa) LOG.println("■ BbsPost #getTopContent()");
		/*
		 * pmlテキストに段落エイリアスを付加する
		 * テンプレートがあるのでそれを読み込んでアペンドする
		 */
		//if(DBG.fa) DBG.println("■ BbsThread #getContent : fileName = " + conf.pmlTemplatePath());
		TemplateBox		tb			=	new TemplateBox(conf.pmlTemplatePath());
		String			template	=	tb.get("thread");
		String			emptext		=	template + Gear.lineSeparator() + content;
		/*
		 * setImgPath() はグラフィックスへの完全パス．/bbs/[ownerkey]/[forumkey]/ を設定する
		 * setImgDestinationPath()はグラフィックスの絶対ディレクトリパス． /home/bbs/[ownerkey]/[forumkey]/  を設定する
		 */
		Exam	exam	= 	new Exam(emptext ,para.getEmlConfPath());
		exam.setImgPath(conf.threadUrl(ownerkey, forumkey, threadkey) + "/");
		exam.setImgDestinationPath(conf.threadDir(ownerkey, forumkey, threadkey) + Gear.fileSeparator());
		return	exam.createHtml();
	}
	/**
	 * リンクつきの添付ファイル用ＨＴＭＬを返す
	 */
	public	String	getTopAttachmentHTML(Hashtable record){
		if(LOG.fa) LOG.println("■ BbsPost #getTopAttachmentHTML()");
		
		String	threadkey	=	strHash(record,(BbsThreadDB.THREAD_KEY));
		/*
		 * 添付ファイル名はCSV形式文字列なのでCsvオブジェクトに直す
		 */
		Csv	cs	=	new Csv(Gear.strHashSP(record, BbsThreadDB.ATTACHMENT));
		if(cs.size()==0)	return	"";
		/*
		 * HTMLを作成する
		 * pmlテキストテンプレートがあるのでそれを読み込んで変数を埋める
		 */
		TemplateBox		tpb			=	new TemplateBox(conf.pmlTemplatePath());
		String			template	=	tpb.get("attach");
		
		Hashtable		tb			=	new Hashtable(10);
		StringBuffer	buf			=	new StringBuffer(1024);
		boolean flag				=	false;	
		for(int i=0; i<cs.size(); i++){
			/*
			 * 添付ファイル以外は処理しない
			 * 添付ファイル名は先頭にアスタリスク(*)が付いているのでそれを見て判断
			 */
			if(!isAttachment(cs, i)){
				continue;
			}
			/*
			 * ファイル名とファイルURL、グラフィックス名をハッシュに設定
			 */
			String	fileName	=	getFilename(cs,i);
			String	image		=	conf.sysFileUrl() + ATTACH_ICON;	
			String	fileUrl		=	conf.threadUrl(ownerkey, forumkey, threadkey) + "/" + fileName;
			String	colorClass	=	getColorClass(cs, i);
			
			tb.put("_fileName"	, fileName);
			tb.put("_image"		, image);
			tb.put("_alt"		, fileName);
			tb.put("_fileUrl"	, fileUrl);
			tb.put("_colorClass", colorClass);
			/*
			 * テンプレートをハッシュで書き変えてEML文字列を作りバッファに格納する
			 */			
			if(flag) {buf.append("&nbsp;&nbsp;");} 
			buf.append( Gear.replace(template, tb) );
			flag	=	true;
		}
		return	buf.toString();
	}	
	
	/***************************************************************************
	 * ハッシュテーブル(htb)を使って部分的に切り取ったソースデータ（exHtml）
	 * の内容を置き換えて出力処理する．表などの反復出力に利用するが個々の処理
	 * 内容は、key で特定される．
	 ***************************************************************************/
	public	void	write(String key,Vector exHtml){
	    
	    if(key.equals("postList")){
	        postListSub(exHtml);
	        
	    }else if(key.equals("Subject")){
	        SubjectSub(exHtml);
	        
	    }else if(key.equals("Content")){
	        ContentSub(exHtml);
	        
	    }else if(key.equals("Attach")){
	        AttachSub(exHtml);
	        
	    }else if(key.equals("isNoList")){
	        isNoListSub(exHtml);
	        
	    }
	}
	public	void	postListSub(Vector exHtml){
		/*
		 * 各ポスト記事を表示する
		 */
		String	handleFlag	=	Gear.strHash(htb, BbsForumDB.HANDLE_FLAG);
		Vector	records		=	new Vector(30);	
		/*
		 * bbskey によって、リンクキーの順にレコードを読み出す
		 */
		TreeSet		articles	=	db.getSortedBbsPosts(bbskey);
		Iterator	it			=	articles.iterator();
		while(it.hasNext()){
		    BbsArticle	item	=	(BbsArticle)it.next();
			Hashtable	rec		=	item.getElm();
			setArticle(rec, handleFlag);
			printVector(exHtml);
		}
		postCount	=	articles.size();
	}	
	public	void	SubjectSub(Vector exHtml){
		String	dt	=	Gear.strHashSP(htb, BbsThreadDB.SUBJECT);
		if(!isEmpty(dt)){
			printVector(exHtml);
		}
	}
	public	void	ContentSub(Vector exHtml){
		String	dt	=	Gear.strHashSP(htb, BbsThreadDB.CONTENT);
		if(!isEmpty(dt)){
			printVector(exHtml);
		}
	}
	public	void	AttachSub(Vector exHtml){
		String	dt	=	Gear.strHashSP(htb, BbsThreadDB.ATTACHMENT);
		if(!isEmpty(dt)){
			printVector(exHtml);
		}
	}
	public	void	isNoListSub(Vector exHtml){
		if(LOG.fa) LOG.println("□ postCount=" + postCount);
		if(postCount > 0)	return;
		printVector(exHtml);
	}
	
	/**
	 * ひとつの記事について、情報をシステムハッシュに埋め込む
	 * 
	 * @return
	 */
	void	setArticle(Hashtable rec, String handleFlag){
		if(LOG.fa) LOG.println("■ BbsPost #setArticle()");
		/*
		 * POST関連データを取り出す
		 */
		//String	bbs_key	=	Gear.strHash(rec, BbsPostDB.BBS_KEY);	
		String	postkey		=	Gear.strHash(rec, BbsPostDB.POST_KEY);	
		String	post_uid	=	Gear.strHash(rec, BbsPostDB.USERID);	
		String	date		=	Gear.strHash(rec, BbsPostDB.DATE);	
		//String	rating		=	Gear.strHash(rec, BbsPostDB.RATING);	
		String	subject		=	Gear.strHash(rec, BbsPostDB.SUBJECT);	
		String	content		=	Gear.strHash(rec, BbsPostDB.CONTENT);	
		String	attachment	=	Gear.strHash(rec, BbsPostDB.ATTACHMENT);	
		//String	dispName	=	Gear.strHash(rec, BbsPostDB.DISP_NAME);	
		//String	link		=	Gear.strHash(rec, BbsPostDB.LINK);
		/*
		 * 表示名とフェースアイコンをシステムハッシュにセットする
		 */
		setUserInfo(post_uid, handleFlag);
		/*
		 * ハッシュに設定する
		 */
		htb.put(BbsPostDB.POST_KEY		, postkey);
		htb.put(BbsThreadDB.DATE		, BbsUtil.getFormattedDate(date));
		htb.put(BbsThreadDB.SUBJECT		, subject);
		htb.put(BbsThreadDB.CONTENT		, getContent(content, forumkey, threadkey, postkey));
		htb.put(BbsThreadDB.ATTACHMENT	, getAttachmentHTML(postkey, attachment));
		
	}
	/**
	 * 表示名とフェースアイコンをシステムハッシュにセットする
	 * ユーザー情報ＤＢを読む
	 * @param userid
	 * @param handleFlag
	 */
	void	setUserInfo(String	postOwnerKey, String handleFlag){
		if(LOG.fa) LOG.println("■ BbsPost #setUserInfo()");
		/*
		 * スレッド所有者の個人情報レコードを得て、そこから名前またはハンドル及びアイコンファイル名を取り出す
		 */
		String		dispName 		=	"";
		String		faceIconURL 	=	"";
		Hashtable	inforec			=	getInfoRecord(postOwnerKey);
		if(inforec==null){
			dispName	=	"#" + postOwnerKey;
			createInfoRecord(postOwnerKey);
			faceIconURL	=	initFaceIcon(postOwnerKey);			
			
		}else	if(handleFlag.equals(BbsForumDB.BY_UID)){
			/*
			 * 名前で表示
			 */
			dispName	=	Gear.strHash(inforec, BbsInfoDB.NAME);
			faceIconURL	=	getUserFaceIconURL(postOwnerKey,inforec);
			if(LOG.fa){
				LOG.println("□ handleFlag        =" + handleFlag);
				LOG.println("□ BbsForumDB.BY_UID =" + BbsForumDB.BY_UID);
				LOG.println("□ dispName          =" + dispName); 
			}			
		}else{
			/*
			 * ハンドルで表示
			 */
			dispName	=	Gear.strHash(inforec, BbsInfoDB.HANDLE);
			faceIconURL	=	getUserFaceIconURL(postOwnerKey, inforec);
			if(LOG.fa){
				LOG.println("□ handleFlag           =" + handleFlag);
				LOG.println("□ BbsForumDB.BY_HANDEL =" + BbsForumDB.BY_HANDLE);
				LOG.println("□ dispName             =" + dispName); 
			}			
		}
		if(Gear.isEmpty(dispName)){
			dispName	=	"#" + postOwnerKey;
		}
		htb.put("_faceIconURL"			, faceIconURL);
		htb.put("_dispName"				, dispName);
	}
	/**
	 * システムアイコンを使ってユーザーフェースアイコンを初期登録する
	 * 
	 * @return	ユーザーアイコンへのＵＲＬを返す
	 */
	String	initFaceIcon(String postOwnerKey){
		
		int	pos			=	getSysFaceIconNumber();		// random 
		String	fname		=	getSysFaceIconName(pos);
		String	fromPath	=	getSysFaceIconDir(fname);
		/*
		 * データベースへ記録
		 */
		updateDB(postOwnerKey, fname);
		/*
		 * システムディレクトリからユーザープロファイルディレクトリへコピー
		 */
		copyToProfileDir(postOwnerKey,fromPath,fname);
		/*
		 * ユーザープロファイルURLを返す
		 */
		String	url			=	userIconUrl(postOwnerKey, fname);
		return	url;
	}
	/**
	 * ユーザーアイコンファイル名をユーザー情報ＤＢへ記録する
	 * @param fname
	 */
	void	updateDB(String postOwnerKey, String fname){

		BbsInfoDB	infoDB	=	new BbsInfoDB(postOwnerKey, szDB, broker);
		infoDB.updateIcon(postOwnerKey, fname);
	}
	/**
	 * システムアイコンをプロファイルへコピーする
	 * @param fromfilePath
	 * @param fname
	 */
	void	copyToProfileDir(String postOwnerKey, String fromfilePath, String fname){
		
		/*
		 * プロファイルディレクトリがなければ作る
		 */
		String 	profileDir	=	conf.profilePath(postOwnerKey, szDB);
		File	pfp			= 	new File(profileDir);
		if(!pfp.exists()){
			pfp.mkdirs();
		}
		/*
		 * ファイルをコピーする
		 */
		try{
			Gear.copyBinryFile(fromfilePath, conf.profilePath(postOwnerKey, szDB)+fname);
					
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	/**
	 * ユーザーのフェースアイコンへのＵＲＬを得る
	 * @param inforec
	 * @return
	 */
	String	getUserFaceIconURL(String postOwnerKey, Hashtable inforec){
		if(LOG.fa) LOG.println("■ BbsPost #getUserFaceIcon()");
		
		String	url		=	"";
		String	fname	=	Gear.strHash(inforec, BbsInfoDB.ICON_FILE);
		if(Gear.isEmpty(fname)){
			/*
			 * システムアイコンを当てる
			 * ユーザーＤＢにも登録する
			 */
			url	=	initFaceIcon(postOwnerKey);
						
		}else{
			url		=	userIconUrl(postOwnerKey, fname);	
		}
		return	url;
	}
	/**
	 * システムフェイスアイコンをひとつえらんでその番号を返す
	 * @return
	 */
	int	getSysFaceIconNumber(){
		if(LOG.fa) LOG.println("■ BbsPost #getSysFaceIcon()");
		/*
		 * システムアイコンファイル一覧を得る
		 */
		String		dir		=	conf.sysIconDir();
		File		dfp		=	new File(dir);
		String []	files	=	dfp.list();
		/*
		 * 乱数でひとつ選ぶ
		 */
		Random 	rd	=	new Random(BbsUtil.getTodayInMilis());
		return	rd.nextInt(files.length); 
	}	
	/**
	 * pos 番目のシステムアイコンファイル名を返す
	 * @param pos
	 * @return
	 */
	String getSysFaceIconName(int pos){
		/*
		 * システムアイコンファイル一覧を得る
		 */
		String		dir		=	conf.sysIconDir();
		File		dfp		=	new File(dir);
		String []	files	=	dfp.list();
		return	files[pos];
	}
	/**
	 * システムフェースアイコンで fname というファイルへの完全パスを返す
	 * @param fname
	 * @return
	 */
	String getSysFaceIconDir(String fname){
		String	dir	=	conf.sysIconDir() + fname;
		return	dir;
	}	
	/**
	 * システムフェースアイコンで fname というファイルへのＵＲＬを返す
	 * @param fname
	 * @return
	 */
	String getSysFaceIconUrl(String fname){
		String	url	=	conf.sysIconUrl() + fname;
		return	url;
	}
	/**
	 * ユーザーフェイスアイコンで fname というファイルへのＵＲＬを返す
	 * @param fname
	 * @return
	 */
	String	userIconUrl( String postOwnerKey, String fname){
		if(LOG.fa) LOG.println("■ BbsPost #getFaceIcon()");

		return	conf.userProfileURL(postOwnerKey, szDB) + fname;
	}
	/**
	 * ユーザーIDからユーザー情報をハッシュテーブルで得る
	 * 存在しない時は null を返す
	 * 
	 * @return
	 */
	Hashtable	getInfoRecord(String uid){
		if(LOG.fa) LOG.println("■ BbsPost #getInfoRecord()");

		BbsInfoDB	infodb	=	new BbsInfoDB(uid, szDB, broker);
		Hashtable	dt		=	new Hashtable();
		int		count	=	infodb.readBbsInfo(uid, dt);
		if(count>0)	return	dt;
		return			null;
	}
	/**
	 * ユーザー情報を新規に作成して返す
	 * 
	 * @return
	 */
	Hashtable	createInfoRecord(String postOwnerKey){
		if(LOG.fa) LOG.println("■ BbsPost #getInfoRecord()");

		Hashtable	dt		=	BbsInfoDB.getInitialProfile(postOwnerKey, "不明");
		BbsInfoDB	infodb	=	new BbsInfoDB(postOwnerKey, szDB, broker);
		infodb.insertBbsInfo(postOwnerKey, dt);
		return		dt;
	}
	/**
	 * PMLをパースしたHTMLを返す
	 * @param content	内容を表すPML
	 * @return			同 HTML
	 */
	public	String	getContent(String content, String forumkey, String threadkey, String postkey){
		if(LOG.fa) LOG.println("■ BbsPost #getContent()");
		/*
		 * pmlテキストに段落エイリアスを付加する
		 * テンプレートがあるのでそれを読み込んでアペンドする
		 */
		//if(DBG.fa) DBG.println("■ BbsThread #getContent : fileName = " + conf.pmlTemplatePath());
		TemplateBox		tb			=	new TemplateBox(conf.pmlTemplatePath());
		String			template	=	tb.get("post");
		String			emptext		=	template + Gear.lineSeparator() + content;
		/*
		 * setImgPath() はグラフィックスへの完全パス．/bbs/[ownerkey]/[forumkey]/ を設定する
		 * setImgDestinationPath()はグラフィックスの絶対ディレクトリパス． /home/bbs/[ownerkey]/[forumkey]/  を設定する
		 */
		Exam	exam	= 	new Exam(emptext ,para.getEmlConfPath());
		exam.setImgPath(conf.postUrl(ownerkey, forumkey, threadkey, postkey) + "/");
		exam.setImgDestinationPath(conf.postDir(ownerkey, forumkey, threadkey, postkey) + Gear.fileSeparator());
		return	exam.createHtml();
	}
	/**
	 * リンクつきの添付ファイル用ＨＴＭＬを返す
	 */
	public	String	getAttachmentHTML(String postkey, String attachment){
		if(LOG.fa) LOG.println("■ BbsPost #getAttachmentHTML()");
		
		/*
		 * 添付ファイル名はCSV形式文字列なのでCsvオブジェクトに直す
		 */
		//Csv	cs	=	new Csv(attachment);
		Csv	cs	=	BbsUtil.makeAttachmentCsv(attachment);
		if(cs.size()==0)	return	"";
		/*
		 * HTMLを作成する
		 * pmlテキストテンプレートがあるのでそれを読み込んで変数を埋める
		 */
		TemplateBox		tpb			=	new TemplateBox(conf.pmlTemplatePath());
		String			template	=	tpb.get("attach");
		
		Hashtable		tb			=	new Hashtable(10);
		StringBuffer	buf			=	new StringBuffer(1024);
		boolean flag				=	false;	
		for(int i=0; i<cs.size(); i++){
			/*
			 * 添付ファイル以外は処理しない
			 * 添付ファイル名は先頭にアスタリスク(*)が付いているのでそれを見て判断
			 */
			if(!isAttachment(cs, i)){
				continue;
			}
			/*
			 * ファイル名とファイルURL、グラフィックス名をハッシュに設定
			 */
			String	fileName	=	getFilename(cs,i);
			String	image		=	conf.sysFileUrl() + ATTACH_ICON;
			if(BbsUtil.isHtml(cs,i)){
			    image	=	conf.sysFileUrl() + ATTACH_HTML;
			}			
			String	fileUrl		=	conf.postUrl(ownerkey, forumkey, threadkey, postkey) + "/" + fileName;
			String	colorClass	=	getColorClass(cs, i);
			
			tb.put("_fileName"	, fileName);
			tb.put("_image"		, image);
			tb.put("_alt"		, fileName);
			tb.put("_fileUrl"	, fileUrl);
			tb.put("_colorClass", colorClass);
			/*
			 * テンプレートをハッシュで書き変えてEML文字列を作りバッファに格納する
			 */			
			if(flag) {buf.append("&nbsp;&nbsp;");} 
			buf.append( Gear.replace(template, tb) );
			flag	=	true;
		}
		return	buf.toString();
	}
	
	/**
	 * アップロードファイル名を得る
	 * 添え字pos が範囲外なら空文字列を返す
	 * 添付ファイルなら１文字目のマーク(*)を取ったものを返す
	 */
	String	getFilename(Csv cs, int pos){
		if(LOG.fa) LOG.println("■ BbsPost #getFilename()");

		if(pos >= cs.size())	return	"";
		
		String	fname	=	cs.get(pos);
		if(fname.charAt(0)=='*'){
			return	fname.substring(1);
		}
		return	fname;
	}
	/**
	 * ファイルの表示色を返す
	 * 添付ファイルとその他で色を変える
	 * @param cs
	 * @param pos
	 * @return		表示色のCSSクラス名
	 */
	String	getColorClass(Csv cs, int pos){
		if(LOG.fa) LOG.println("■ BbsPost #getColorClass()");

		if(isAttachment(cs, pos)){
			return	"#3366CC";
		}
		return "#333333";
	}
	/**
	 * 添付ファイルかどうか
	 * @param cs
	 * @param pos
	 * @return	添付ファイルなら true を返す
	 */
	boolean	isAttachment(Csv cs, int pos){
		if(LOG.fa) LOG.println("■ BbsPost #isAttachment()");

		if(pos >= cs.size())	return	false;
		
		String	fname	=	cs.get(pos);
		if(fname.charAt(0)=='*'){
			return	true;
		}
		return	false;			
	}	

}