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

import java.io.*;
import java.util.*;
import java.math.*;

import org.apache.log4j.Logger;


/**
	#
	# #########
	#  thread
	# #########
	#
	<program $jbbs.BbsThread>
		<dispatch  html=threadList.html  number=3210       class=jbbs.BbsThread />
		<variable>
		  <receive  NUMBER STAMP  GROUP  BBS_OWNER_KEY  UID MAIL UNAME  HOMEURL DIVISION forumkey />
		  <accept   CMD threadkey sortmode listMode />
		  <keep      />
		</variable>
	</program>
	
 *
 *
 * 変数の説明
 *
 * 1. receive 
 * 2. accept
 * 		threadkey
 * 		sortmode		ソートできるように変数は設定しているが実装していない
 * 		listMode		簡易表示（SIMPLE）か詳細表示（MORE）か［リストボックス］
 * 3. keep
 *		
 *
 * 4. form 
 *
 *
	
 */
public class BbsThread extends SuperPlayer implements BbsVar{
	protected Logger logger = Logger.getLogger( this.getClass() );
	
	BbsConf				conf;
	//
	BbsThreadDB			db;
	String				ownerid;
	String				szDB;
	DbConnectionBroker	broker;
	
	String				forumkey;
	String				owner_forumkey;
	String				userid;
	
	/* ************ 
	 *  作業用変数
	 **************/
	/** 
	 * フォーラムに含まれるスレッドの数
	 */
	int		threadCount;
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
	
	
	public	BbsThread(){
		super();
	}
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		if(LOG.fa) LOG.outHash(htb,"■ BbsThread #initialize()");
		super.setInit(out,htb,para);
		//
		conf			=	createBbsConf(para.get("BBS_CONF_PATH"),htb);		
		//
		ownerid			=	strHash(htb,BBS_OWNER_KEY);
		szDB			=	strHash(htb, GROUP);
		broker			=	(DbConnectionBroker)htb.get(BROKER);
		db				=	new BbsThreadDB(szDB, broker);		
		//
		forumkey		=	strHash(htb,BbsForumDB.FORUM_KEY);	// forumkey
		if(LOG.fa) LOG.println("□ BbsThread #createBbsConf()　forumkey="+forumkey);
		owner_forumkey	=	BbsKeys.ownerForumKey(ownerid, forumkey);
		threadCount		=	0;
		
		userid			=	Gear.strHash(htb,UID);	// 常にある
	}
	/**
	 * jbbsライブラリのためのシステムコンフィギュレーションオブジェクトを作成する
	 */
	public BbsConf	createBbsConf(String BbsConfPath, Hashtable ht){
		if(LOG.fa) LOG.println("■ BbsThread #createBbsConf()");
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
		if(LOG.fa) LOG.outHash(htb,"■ BbsThread #dispatch()");
		
		/*
		 * 処理分岐を判断するキーはシステムハッシュからParam.DISPATCH_KEYをキーとして取り出す
		 * リターンコードと次に表示するWEBの表示モードは設定忘れを防ぐため規定値をセットしておく
		 */
		cmd			=	strHash(htb,DISPATCH_KEY);
		ret			=	DISPATCH_DEFAULT;	// WEB表示
		disp_mode	=	DISP_NEW;			// クリアーして新規表示
		
		if(cmd.equals("VIEW")){
			/*
			 * VIEW 別ウィンドウとして起動される時のためのもの
			 */
			disp_mode	=	DISP_NEW;
			ret			=	DISPATCH_DEFAULT;	// WEB表示
		
		}else if(cmd.equals("LISTING")){
		    /*
		     * 簡易表示と詳細表示の切り替え
		     */
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// WEB表示				
		
		}else if(cmd.equals("OPEN")){
			
			/*
			 * 選択されたスレッドの view を１件プラスして更新する
			 */
			String	threadkey	=	Gear.strHash(htb, BbsThreadDB.THREAD_KEY);
			updateViews(BbsThreadDB.INCREASE, threadkey);
			/*
			 * ポスト一覧へ移る
			 */
			disp_mode	=	DISP_NEW;
			ret			=	"$jbbs.BbsPost";		// ポスト一覧
			

		}else if(cmd.equals("CREATE")){
			/*
			 * active でなければ作成できない
			 */
			if(!isActive()){
				htb.put(MESSAGE,"★ フォーラムが閉じらているので作成できません");
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;	// WEB表示				
			}else{			
			    String	msg	=	permissionCreate();
			    if(Gear.isEmpty(msg)){
			        disp_mode	=	DISP_NEW;
					/*
					 * BbsThreadDB.THREAD_KEY に ""ではなく "_threadkey" が入っている可能性を防ぐ
					 */
					htb.put(BbsThreadDB.THREAD_KEY,"");
					ret			=	CREATE_THREAD;
					
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
				if(permission( Gear.strHash(htb, BbsThreadDB.THREAD_KEY) )){
					disp_mode	=	DISP_NEW;
					ret			=	CREATE_THREAD;
				}else{
					htb.put(MESSAGE,"★ 編集する権利がありません");
					disp_mode	=	DISP_EDIT;
					ret			=	DISPATCH_DEFAULT;	// WEB表示
				}
			}
		}else if(cmd.equals("DELETE")){

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
				if(permission( Gear.strHash(htb, BbsThreadDB.THREAD_KEY) )){
					delete(Gear.strHash(htb, BbsThreadDB.THREAD_KEY));
					disp_mode	=	DISP_NEW;
					ret			=	DISPATCH_DEFAULT;
				}else{
					htb.put(MESSAGE,"★ 削除する権利がありません");
					disp_mode	=	DISP_EDIT;
					ret			=	DISPATCH_DEFAULT;	// WEB表示
				}
			}

		}else if(cmd.equals("SORT")){
			/* 
			 * 表示を並び替える
			 * 並び替えのパラメータ（SORT_MODE）はシステムハッシュに入っているので
			 * ここでは単に再表示をすればよい
			 */
			disp_mode	=	DISP_NEW;
			ret			=	DISPATCH_DEFAULT;
			
		}else if(cmd.equals("RETURN")){
			/* 
			 * 呼び出し元プログラムへ戻る
			 */ 
			ret	=	DISPATCH_RETURN;

		}else{
			/* 
			 * cmd には起動したいクラスキーが入っている（$jbbs.BbsPost）
			 * 該当のクラスを起動する
			 */
			disp_mode	=	DISP_NEW;
			ret	=	cmd;
		}
		/*
		 * 表示モードをセットしてからリターンする
		 */
		htb.put(DISP_KEY,disp_mode);
		return	ret;
	}
	/**
	 *	ゲストの書き込みかどうか 
	 */
	String	permissionCreate(){
		Hashtable	user	=	getInfoRecord();
		String	division	=	Gear.strHash(user, BbsInfoDB.DIVISION);
		if( division.equals( BbsInfoDB.GUEST) )		return	"★ ゲストの書き込みはできません";
		return	"";		
	}
	/**
	 * スレッドレコードの view の値を更新する
	 * @param delta
	 * @param owner_forumkey
	 * @param threadkey
	 * @param db
	 */
	public	 void updateViews(String delta, String threadkey){

		Hashtable	rec		=	readRecord(threadkey);
		String		viewstr	=	Gear.strHash(rec, BbsThreadDB.VIEWS);
		int		views	=	0;
		if(!Gear.isEmpty(viewstr)){
			views	=	Integer.parseInt(viewstr);	
		}
		if(delta.equals(BbsThreadDB.INCREASE)){
			views++;
		}else{
			views--;
		}
		viewstr	=	String.valueOf(views);
		db.updateViews(owner_forumkey, threadkey,viewstr);
		
	}
	/** 
	 * owner_forumkey と threadkey を指定してひとつのレコードを読み結果をハッシュにいれて返す
	 * レコードがなかった場合戻り値は null
	 * @param owner_forumkey	オーナースレッドキー
	 * @param threadkey		スレッドキー
	 * @param db				スレッドデータベース操作オブジェクト
	 * @return					レコードを格納したハッシュ
	 */
	public	 Hashtable	readRecord(String threadkey){
		if(LOG.fa)	LOG.println("■ BbsMakeThread #readRecord() ひとつのレコードを読み結果をハッシュにいれて返す");
			
		Hashtable	tb	=	new	Hashtable(20);
		int	n		=	db.readBbsThreadRecord(owner_forumkey, threadkey, tb);
		if(n==0)	return	null;
		return	tb;
	}	
	/**
	 * スレッドを削除する
	 * @param threadkey
	 */
	void delete(String threadkey){
		
		deleteArticle(threadkey);	// 記事
		deleteFiles(threadkey);	// ファイル
		
	}
	/**
	 * スレッドをデータベースから削除する
	 *
	 */
	void	deleteArticle(String threadkey){
		
		/*
		 * スレッド内の記事を全て消す
		 */
		BbsPostDB	postDB	=	new BbsPostDB(szDB, broker);
		postDB.deleteAllBbsPost(BbsKeys.bbsKey(owner_forumkey, threadkey));
		/*
		 * スレッドを消す
		 */
		db.deleteBbsThread(owner_forumkey, threadkey);
		return;
	}
	/**
	 * スレッドに関連したファイルをディレクトリごと削除する
	 * 配下の記事に関連したディレクトリも全て削除される
	 */
	void	deleteFiles(String threadkey){
		
		String	dir	=	conf.threadDir(ownerid, forumkey, threadkey);
		Gear.deleteDir(new File(dir));
			
	}

	/**
	 * このスレッドリストにアクセスしているユーザーが、特定のスレッドの
	 * 更新・削除権を持つかどうか判定する
	 * @param 		threadkey
	 * @return		権利があるとき true
	 */
	boolean	permission(String threadkey){
		if(LOG.fa) LOG.println("■ BbsThread #permission()");
		/*
		 * アクセスしているユーザーIDが、スレッド所有者IDと同じか、
		 * あるいはフォーラム所有者IDと同じならば、更新・削除権がある
		 */
		String	realOwnerId	=	getRealOwnerId(threadkey);
		if(LOG.fa){
			LOG.println("□□ (ownid)" + realOwnerId + "   (userID)" + userid);
		}
		
		if(realOwnerId.equals(userid))	return	true;
		if(ownerid.equals(userid))		return	true;
		
		/*
		 * スーパーユーザーは常に更新・削除できる 
		 */
		Hashtable	user	=	getInfoRecord();
		if(user==null)			return	false;
		String	division	=	Gear.strHash(user, BbsInfoDB.DIVISION);
		if( division.equals( BbsInfoDB.SUPER) )	return	true;	// super user		
		
		return	false;
		
	}
	/**
	 * スレッドの所有者IDを得る
	 * @param threadkey
	 * @return
	 */
	String	getRealOwnerId(String threadkey){
		if(LOG.fa) LOG.println("■ BbsThread #getRealOwnerId()");
		
		Hashtable	record	=	readRecord(threadkey);
		String		id		=	Gear.strHash(record, BbsThreadDB.USERID);	
		if(LOG.fa) LOG.println("□□ real owner id = " + id);
		return		id;
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
		if(LOG.fa) LOG.println("■ BbsThread #getActiveFlag()");
		
		Hashtable	record	=	readForumRecord(forumkey);
		return		Gear.strHash(record, BbsForumDB.ALIVE_FLAG);
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
		if(LOG.fa)	LOG.println("■ BbsThread #readRecord() ひとつのレコードを読み結果をハッシュにいれて返す");
			
		Hashtable	tb	=	new	Hashtable(20);
		int	n		=	(new BbsForumDB(szDB, broker)).readBbsForums(ownerid, forumkey, tb);
		if(n==0)	return	null;
		return	tb;
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
	 * 
	 */
	public  void	display(boolean editmode){
		if(LOG.fa) LOG.outHash(htb,"■ BbsThread #display(boolean editmode)");
		if(!editmode){
			htb.put(MESSAGE,"");
			/*
			 * 簡易表示が規定値
			 */
			putParameter("listMode","SIMPLE");
		}
		/*
		 * 表示モードのリストボックスの表示設定
		 */
		setModeDisp();
		
		/* strHash(htb,DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v,htb);		
	}
	/**
	 * リストボックスの値から，表示用のselected
	 * を設定する
	 */
	public	void	setModeDisp(){
	    String	mode	=	getParameter("listMode");
	    if(mode.equals("MORE")){
	        putParameter("simpleDisp","");
	        putParameter("moreDisp","selected");
	        
	    }else{// SIMPLE
	        putParameter("simpleDisp","selected");
	        putParameter("moreDisp","");

	    }
	}
	
	public	void	write(String key,Vector exHtml){
		logger.debug("write: key = " + key);
	    if(key.equals("threadList")){
		    /*
		     * 簡易表示モードなら何もしない
		     */
		    String	mode	=	getParameter("listMode");
		    if(mode.equals("SIMPLE")){
		    	logger.debug("threadList --> SIMPLE-->return");
		        return;
		    }
	        threadListSub(exHtml);
	        logger.debug("threadList --> threadListSub(exHtml)完了");
	    }else if(key.equals("threadSimpleList")){
		    /*
		     * 詳細表示モードなら何もしない
		     */
		    String	mode	=	getParameter("listMode");
		    if(mode.equals("MORE")){
		    	logger.debug("threadList --> MORE-->return");
		        return;
		    }
		    threadListSub(exHtml);
	        logger.debug("threadSimpleList --> threadListSub(exHtml)完了");
		        
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
	public	void	threadListSub(Vector exHtml){
		logger.debug("★threadListSub() -----");
	    /*
		 * フォーラムレコードを読み、必要なフラグを読み取る
		 */
		Hashtable	tbl			=	getForumRecord(forumkey);
		String		ratingFlag	=	Gear.strHash(tbl, BbsForumDB.RATING_FLAG);
		String		alive		=	Gear.strHashSP(tbl,(BbsForumDB.ALIVE_FLAG));
		String		handleFlag	=	Gear.strHash(tbl,BbsForumDB.HANDLE_FLAG);
								
		/*
		 * 表示する鍵アイコンを設定
		 */
		String		signIcon	=	"lockIconOpen.gif";
		if(alive.equals(BbsForumDB.HIDDEN)||alive.equals(BbsForumDB.CLOSED)){
			signIcon	=	"lockIcon.gif";
		}
		htb.put("_signIcon", signIcon);

		/*
		 * すべてのスレッドを表示する
		 * スレッドの件数も記録する（ blockWrite() で利用するため ）
		 */
		Vector	v	=	new Vector(15);
		logger.debug("■owner_forumkey＝"+owner_forumkey);
		logger.debug("");
		db.readBbsThreads(owner_forumkey, v);
		threadCount	=	v.size();
		logger.debug("　★threadListSub() ---スレッド件数＝"+ threadCount);
		for(int i=0; i<threadCount; i++){
			
			Hashtable	tb		=	(Hashtable)v.get(i);
			if(tb == null){
				continue;
			}
			
			/*
			 * スレッド関係の表示データ
			 */
			String	threadkey		=	Gear.strHashSP(tb,(BbsThreadDB.THREAD_KEY));
			String	threadOwner		=	Gear.strHashSP(tb,(BbsThreadDB.USERID));
			String	date			=	Gear.strHashSP(tb,(BbsThreadDB.DATE));
			String	rating			=	Gear.strHashSP(tb,(BbsThreadDB.RATING));
			String	subject			=	Gear.strHashSP(tb,(BbsThreadDB.SUBJECT));
			String	content			=	Gear.strHashSP(tb,(BbsThreadDB.CONTENT));
			String	attach			=	Gear.strHashSP(tb,(BbsThreadDB.ATTACHMENT));
			String	views			=	Gear.strHashSP(tb,(BbsThreadDB.VIEWS));
			logger.debug("　●threadkey="+threadkey);
			logger.debug("　●threadOwner="+threadOwner);
			logger.debug("　●date="+date);
			logger.debug("　●rating="+rating);
			logger.debug("　●subject="+subject);
			logger.debug("　●content="+content);
			logger.debug("　●attach="+attach);
			logger.debug("　●views="+views);
			logger.debug("　");
			/*
			 * スレッド所有者の個人情報レコードを得て、そこから名前またはハンドルを取り出す
			 */
			String		dispName 	=	getDispName(threadOwner, handleFlag);
			/*
			 * ハッシュに設定する
			 */ 
			htb.put(BbsThreadDB.THREAD_KEY	, threadkey);
			htb.put("_ownerName"			, dispName);
			htb.put(BbsThreadDB.DATE		, BbsUtil.getFormattedDate(date));
			/*
			 * rating は表示の都度計算し、レコードには書かない
			 */
			//
			htb.put(BbsThreadDB.SUBJECT		, subject);
			String	ct	=	getContent(content, forumkey, threadkey);
			
			
			htb.put(BbsThreadDB.CONTENT		, ct);
			htb.put(BbsThreadDB.VIEWS		, (!Gear.isEmpty(views) ? views : "0"));
			String	at	=	getAttachmentHTML(tb);
			
			
			htb.put(BbsThreadDB.ATTACHMENT	, at);
			/*
			 * このスレッドの全ての記事を日付降順にベクターに得る 
			 */
			Vector	posts 	=	getPostRecords(threadkey);
			/*
			 * 最新記事日付と投稿者
			 */
			setPostInfo(htb, posts, handleFlag);
			/*
			 * rating
			 */				
			if(ratingFlag.equals(BbsForumDB.RATING_MODE)){
				String	average	=	getRatingAverage(posts);
				htb.put(BbsThreadDB.RATING, average);
			}else{
				htb.put(BbsThreadDB.RATING, "-");
			}
			/*
			 * ひとつのスレッドを表示
			 */
			
			printVector(exHtml);
			
		}

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
		if(threadCount > 0)	return;
		printVector(exHtml);
	}
	/**
	 * IDからハンドルフラグを考慮して、表示すべきユーザー名を返す
	 * ユーザー情報が登録されていなければ仮登録し、ユーザーアイコンも確定する
	 *
	 * @param user_key
	 * @return
	 */
	String getDispName(String user_key,String handleFlag){
		if(LOG.fa) LOG.println("●getDispName()");
		
		String		dispName 	=	"";
		Hashtable	rec			=	getInfoRecord(user_key);
		if(rec==null){
			dispName	=	"#" + user_key;

			// ユーザー情報を仮作成し、グラフィックアイコンを決定する
			createInfoRecord(user_key);
			initFaceIcon(user_key);					
							
		}else	if(handleFlag.equals(BbsForumDB.BY_UID)){
			/*
			 * 名前で表示
			 */
			dispName	=	Gear.strHash(rec, BbsInfoDB.NAME);
		}else{
			/*
			 * ハンドルで表示
			 */
			dispName	=	Gear.strHash(rec, BbsInfoDB.HANDLE);
		}
		
		if(Gear.isEmpty(dispName)){
			dispName	=	"#" + user_key;
		}
		return	dispName;
	}
	/**
	 * 日付で降順に並べて、このスレッドにかかる全てのポストレコードをベクターで得る
	 * @param threadkey
	 * @return
	 */
	Vector	getPostRecords(String threadkey){
		if(LOG.fa)	LOG.println("■ BbsThread #getPostRecords()");
		/*
		 * 日付は新しいものが先にくるように読み出す
		 */
		BbsPostDB	tdb	=	new BbsPostDB(szDB,broker);
		Vector		v	=	new Vector(50);
		tdb.readBbsPostsDateOredrDEC(BbsKeys.bbsKey(ownerid,forumkey,threadkey), v);
		return	v;	// 全ポストレコードが入っている			
	}
	/** 
	 * 最新の投稿者と日付をシステムハッシュにセットする
	 * @param htb
	 * @param v
	 */
	void	setPostInfo(Hashtable htb, Vector v , String handleFlag){
		if(LOG.fa)	LOG.println("■ BbsThread #setThreadInfo()");

		int		posts			=	0;
		String		latestDate		=	"";
		String		latestPostName	=	"";
		Hashtable	postRecord		=	null;

		posts				=	v.size();				//　ポスト件数
		if(posts>0){
			postRecord		=	(Hashtable)v.get(0);	//  最新レコード
			String	id		=	Gear.strHash(postRecord, BbsPostDB.USERID);
			latestPostName	=	getDispName(id, handleFlag);
			latestDate		=	Gear.gatDateFromMiliStr( Gear.strHash(postRecord, BbsPostDB.DATE) );
		}			
		htb.put("_latestDate"	, (!Gear.isEmpty(latestDate) ? latestDate : "-"));
		htb.put("_postUser"		, latestPostName);
		htb.put("_pn"			, String.valueOf(posts));	
	}
	/**
	 * 支持度の平均値を求めて文字列で返す
	 * 値は小数点以下２桁で四捨五入されている
	 * 
	 * @return		支持度の平均値．データがなかった場合は "-"を返す．
	 */
	String	getRatingAverage(Vector points){
		if(LOG.fa)	LOG.outHash(htb,"■ BbsThread #getAverageRating()");
		
		int		n		=	points.size();
		/*
		 * 総ポスト数（割り算の分母にする）
		 */
		int		rated	=	n;
		
		if(n==0){
			return	"-";	// ポスト無し
		}
		double	sum	=	0.0;
		for(int i=0; i<n; i++){
			/*
			 * 指示度をポストしていない場合は "0.00" となっている
			 */
			Hashtable	tb		=	(Hashtable)points.get(i);
			String		pt		=	Gear.strHash(tb, BbsPostDB.RATING);
			double		rate	=	Double.parseDouble(pt);
			/*
			 * 指示度の入力をしていない場合はカウントしない
			 */
			if(rate > 0.0){
			    sum					+=	rate;
			}else{
			    // カウントしないので総数から１マイナス
			    rated--;
			}
			
		}
		BigDecimal  sumValue	=	new BigDecimal(String.valueOf(sum));
		BigDecimal	div			=	new BigDecimal(String.valueOf(rated + ".00"));
		/*
		 * 誰も指示度を入力していないケースでは,"-"を返す
		 * 
		 */
		if(div.doubleValue()<=0.0){
		    return		"-";
		}
		/*
		 * 割り算を行う
		 */
		BigDecimal	average		=	sumValue.divide(div, BigDecimal.ROUND_HALF_UP);	// 小数点以下１桁で四捨五入
		String		retStr		=	average.toString();
		return 	retStr;
	}
	
	/** 
	 * ownerkey と forumkey を指定してひとつのレコードを読み結果をハッシュにいれて返す
	 * レコードがなかった場合戻り値は null
	 * @param ownerkey	オーナーキー
	 * @param forumkey	フォーラムキー
	 * @param db		フォーラムデータベース操作オブジェクト
	 * @return			レコードを格納したハッシュ
	 */
	public	 Hashtable	getForumRecord(String forumkey){
		if(LOG.fa)	LOG.println("■ BbsThread #readRecord() ひとつのレコードを読み結果をハッシュにいれて返す");
			
		Hashtable	tb	=	new	Hashtable(20);
		int	n		=	(new BbsForumDB(szDB, broker)).readBbsForums(ownerid, forumkey, tb);
		if(n==0)	return	null;
		return	tb;
	}	
	/**
	 * ユーザー情報をハッシュテーブルで得る
	 * 存在しない時は null を返す
	 * 
	 * @return
	 */
	Hashtable	getInfoRecord(){
		
		return	getInfoRecord(userid);
	}
	/**
	 * ユーザーIDからユーザー情報をハッシュテーブルで得る
	 * 存在しない時は null を返す
	 * 
	 * @return
	 */
	Hashtable	getInfoRecord(String threadOwnerKey){
		BbsInfoDB	infodb	=	new BbsInfoDB(threadOwnerKey, szDB, broker);
		Hashtable	dt		=	new Hashtable();
		int		count	=	infodb.readBbsInfo(threadOwnerKey, dt);
		if(count>0)	return	dt;
		return			null;
	}
	/**
	 * ユーザー情報を新規に作成して返す
	 * 
	 * @return
	 */
	Hashtable	createInfoRecord(String threadOwnerKey){
		if(LOG.fa) LOG.println("■ BbsThread #createInfoRecord()");

		Hashtable	dt		=	BbsInfoDB.getInitialProfile(threadOwnerKey, "不明");
		BbsInfoDB	infodb	=	new BbsInfoDB(threadOwnerKey, szDB, broker);
		infodb.insertBbsInfo(threadOwnerKey, dt);
		return		dt;
	}
	/**
	 * システムアイコンを使ってユーザーフェースアイコンを初期登録する
	 * 
	 * @return	ユーザーアイコンへのＵＲＬを返す
	 */
	String	initFaceIcon(String threadOwnerKey){
		
		int	pos			=	getSysFaceIconNumber();		// random 
		String	fname		=	getSysFaceIconName(pos);
		String	fromPath	=	getSysFaceIconDir(fname);
		/*
		 * データベースへ記録
		 */
		updateDB(threadOwnerKey, fname);
		/*
		 * システムディレクトリからユーザープロファイルディレクトリへコピー
		 */
		copyToProfileDir(threadOwnerKey,fromPath,fname);
		/*
		 * ユーザープロファイルURLを返す
		 */
		String	url			=	userIconUrl(threadOwnerKey, fname);
		return	url;
	}
	/**
	 * ユーザーアイコンファイル名をユーザー情報ＤＢへ記録する
	 * @param fname
	 */
	void	updateDB(String threadOwnerKey, String fname){

		BbsInfoDB	infoDB	=	new BbsInfoDB(threadOwnerKey, szDB, broker);
		infoDB.updateIcon(threadOwnerKey, fname);
	}
	/**
	 * システムアイコンをプロファイルへコピーする
	 * @param fromfilePath
	 * @param fname
	 */
	void	copyToProfileDir(String threadOwnerKey, String fromfilePath, String fname){
		
		/*
		 * プロファイルディレクトリがなければ作る
		 */
		String 	profileDir	=	conf.profilePath(threadOwnerKey, szDB);
		File	pfp			= 	new File(profileDir);
		if(!pfp.exists()){
			pfp.mkdirs();
		}
		/*
		 * ファイルをコピーする
		 */
		try{
			Gear.copyBinryFile(fromfilePath, conf.profilePath(threadOwnerKey, szDB)+fname);
					
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	/**
	 * ユーザーのフェースアイコンへのＵＲＬを得る
	 * @param inforec
	 * @return
	 */
	String	getUserFaceIconURL(String threadOwnerKey, Hashtable inforec){
		if(LOG.fa) LOG.println("■ BbsThread #getUserFaceIcon()");
		
		String	url		=	"";
		String	fname	=	Gear.strHash(inforec, BbsInfoDB.ICON_FILE);
		if(Gear.isEmpty(fname)){
			/*
			 * システムアイコンを当てる
			 * ユーザーＤＢにも登録する
			 */
			url	=	initFaceIcon(threadOwnerKey);
						
		}else{
			url		=	userIconUrl(threadOwnerKey, fname);	
		}
		return	url;
	}
	/**
	 * システムフェイスアイコンをひとつえらんでその番号を返す
	 * @return
	 */
	int	getSysFaceIconNumber(){
		if(LOG.fa) LOG.println("■ BbsThread #getSysFaceIcon()");
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
	String	userIconUrl( String threadOwnerKey, String fname){
		if(LOG.fa) LOG.println("■ BbsThread #getFaceIcon()");

		return	conf.userProfileURL(threadOwnerKey, szDB) + fname;
	}
	/**
	 * PMLをパースしたHTMLを返す
	 * @param content	内容を表すPML
	 * @return			同 HTML
	 */
	public	String	getContent(String content, String forumkey, String threadkey){
		if(LOG.fa) LOG.println("●getContent()");
		/*
		 * pmlテキストに段落エイリアスを付加する
		 * テンプレートがあるのでそれを読み込んでアペンドする
		 */
		//if(DBG.fa) DBG.println("■ BbsThread #getContent : fileName = " + conf.pmlTemplatePath());
		TemplateBox		tb			=	new TemplateBox(conf.pmlTemplatePath());
		String			template	=	tb.get("thread");
		String			emptext		=	template + Gear.lineSeparator() + content;
		/*
		 * setImgPath() はグラフィックスへの完全パス．/bbs/[ownerid]/[forumkey]/ を設定する
		 * setImgDestinationPath()はグラフィックスの絶対ディレクトリパス． /home/bbs/[ownerid]/[forumkey]/  を設定する
		 */
		Exam	exam	= 	new Exam(emptext ,para.getEmlConfPath());
		exam.setImgPath(conf.threadUrl(ownerid, forumkey, threadkey) + "/");
		exam.setImgDestinationPath(conf.threadDir(ownerid, forumkey, threadkey) + Gear.fileSeparator());
		return	exam.createHtml();
	}
	/**
	 * リンクつきの添付ファイル用ＨＴＭＬを返す
	 */
	public	String	getAttachmentHTML(Hashtable record){
		if(LOG.fa)	LOG.println("■ Bbsthread #getAttachmentHTML()");
		
		String	threadkey	=	strHash(record,(BbsThreadDB.THREAD_KEY));
		/*
		 * 添付ファイル名はCSV形式文字列なのでCsvオブジェクトに直す
		 */
		//Csv	cs	=	new Csv(Gear.strHashSP(record, BbsThreadDB.ATTACHMENT));
		Csv	cs	=	BbsUtil.makeAttachmentCsv(Gear.strHashSP(record, BbsThreadDB.ATTACHMENT));
		if(cs.size()==0){
			
			return	"";
		}

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
			String	fileUrl		=	conf.threadUrl(ownerid, forumkey, threadkey) + "/" + fileName;
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
		if(LOG.fa)	LOG.println("■ BbsThread #getFilename()");

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
		if(LOG.fa)	LOG.println("■ BbsThread #getColorClass()");

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
		if(LOG.fa)	LOG.println("■ BbsThread #isAttachment()");

		if(pos >= cs.size())	return	false;
		
		String	fname	=	cs.get(pos);
		if(fname.charAt(0)=='*'){
			return	true;
		}
		return	false;			
	}

}
