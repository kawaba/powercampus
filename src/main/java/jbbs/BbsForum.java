/*
 * 
 */
package jbbs;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import org.apache.log4j.Logger;
import database.DbConnectionBroker;
import database.KeyGen;
import epml.Exam;
import framework.LOG;
import framework.Param;
import framework.SuperPlayer;
import tktools.Csv;
import tktools.Gear;
import tktools.TemplateBox;

/**
 * あるオーナーの所有するフォーラムについての処理
 * 
 * 
	#
	# ######################################
	# forum
	#       (note) TUID == ownerkey
	#       RELATION == year+season_TUID_apleckey
	#
	# ######################################
	#
	<program $jbbs.BbsForum>
		<dispatch  html=forumList.html   number=3100       class=jbbs.BbsForum />
		<variable>
		  <receive  NUMBER   STAMP  GROUP  UID MAIL UNAME HOMEURL DIVISION  BBS_OWNER_KEY  BBS_RELATION />
		  <accept   SUBMIT   forumkey />
		  <keep       />
		</variable>
	</program> 
 *  
 *   BBS_OWNER_KEY(オーナーキー）とBBS_RELATION（リレーションキー）は呼び出し側でセットしている
 *   DIVISION はフレームワークの初期化処理でセットしているので常に receive される項目である
 * 　作成権のチェックはDIVITIONを利用して行う
 * 
 *   フレームワークは、毎回セッション制御時に、members テーブルから以下の情報を htb（システムハッシュ）に設定している
 * 　そのためUID,MAIL,UNAME などは常に receive される項目である
 * 		
 *	ユーザーID (user_id)				TUID,   UID,  _user_id 
 *	メールアドレス(user_mail)			TMAIL,  MAIL, _mail
 * 	パスワード(user_passwd)				_passwd
 *	登録が完了ユーザーか(user_active)	_active
 *	氏名(user_name)						TNAME,  UNAME, _name  
 *	ふりがな(user_hurigana)				THKANA
 *	エディタ行数など初期設定の値(note)	_note
 *	グループ名(user_db)					GROUP
 *	ドメイン名(user_domain)				DOMAIN
 *	戻り先URL(user_url_t)				_url_t
 *
 * 
 * 	UID、MAIL、UNAME は jbbs ライブラリのための別名設定
 * 　
 * 
 * 
 * 
 フォーラムデータベースの内容
 alive     活動中か否か 　　0=close&hidden  1=close  2=active
 gpflag    グループ学習か　 0=no      1=yes     規定値: 0
 rtflag    支持度必須か　   0=no      1=yes     規定値: 0
 hdflag    ハンドル必須か   0=no      1=yes     規定値: 1

create table bbsforum_GROUPNAME (
     ownerkey   VARCHAR(12)  PRIMARY KEY,
     forumkey   CHAR(4),  
     alive      CHAR(1),
     gpflag     CHAR(1),
     rtflag     CHAR(1),
     hdflag     CHAR(1),
     date       VARCHAR(30),
     subject    VARCHAR(100),
     content    TEXT,
     attachment TEXT,
     relation   TEXT
);
 */
public class BbsForum extends SuperPlayer implements BbsVar{
	protected Logger logger = Logger.getLogger( this.getClass() );
	BbsConf				conf;
	//
	BbsForumDB			db;
	String				ownerid;
	String				szDB;
	DbConnectionBroker	broker;
	
	String				userid;
	
	Vector				records;
	int					forumCounts;
	
	String				relation;
	String				division;
	
	public	BbsForum(){
		super();
		if(LOG.fa)	LOG.println("■ BbsForum #コンストラクタ");
	}
	
	@Override
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		
		super.setInit(out,htb,para);
		//
		if(LOG.fa)	LOG.println("■ BbsForum #initialize()");
		
		conf			=	createBbsConf(para.get("BBS_CONF_PATH"),htb);		
		/*
		 * ownerkey != userid という前提
		 * 学生やTEの場合は同じではない 
		 */
		ownerid			=	strHash(htb,BBS_OWNER_KEY);
		/*
		 * リレーションキー
		 */
		relation		=	Gear.strHash(htb, BBS_RELATION);
		
		szDB			=	strHash(htb, GROUP);
		broker			=	(DbConnectionBroker)htb.get(BROKER);
		db				=	new BbsForumDB(szDB, broker);

		userid			=	getParameter(UID);			// userid
		division		=	getParameter(DIVISION);
		
		/*
		 * ownerid で複数のレコードを読む
		 * １レコードをひとつのハッシュテーブルに格納
		 * これを引数の Vector に 読み出し順に格納する
		 * 同じリレーションのものだけを読み込む
		 */
		records			=	new Vector(20);
		db.readBbsForums(ownerid, relation, records);

	}
	/**
	 * jbbsライブラリのためのシステムコンフィギュレーションオブジェクトを作成する
	 */
	public BbsConf	createBbsConf(String BbsConfPath, Hashtable ht){
		if(LOG.fa)	LOG.println("■ BbsForum #createBbsConf()");
		/* 
		 * BBSコンフィギュレーションクラス
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
	@Override
	public	String	dispatch(){
		if(LOG.fa) LOG.outHash(htb,"■ Forum #dispatch()");
		
		/*
		 * 処理分岐を判断するキーはシステムハッシュからParam.DISPATCH_KEYをキーとして取り出す
		 * リターンコードと次に表示するWEBの表示モードは設定忘れを防ぐため規定値をセットしておく
		 */
		String 	cmd			=	strHash(htb,DISPATCH_KEY);
		String	ret			=	DISPATCH_DEFAULT;	// WEB表示
		String	disp_mode	=	DISP_NEW;			// クリアーして新規表示
		/*
		 * VIEW は postMkUserInfo.html から起動される
		 * postMkUserInfo.html は top.html などこのクラスを起動したいクラスが呼び出す．
		 * パラメータを親Htmlから受け取り、cmd に VIEW をいれたのち submit()するもの．
		 */
		if(cmd.equals("VIEW")){
			disp_mode	=	DISP_NEW;
			ret			=	DISPATCH_DEFAULT;	// WEB表示
			
		}else if(cmd.equals("CREATE")){
			/*
			 * ユーザー区分を調べて作成権があるかどうかチェックする
			 */
			String		msg	=	permissionCreate();
			if(Gear.isEmpty(msg)){
				disp_mode	=	DISP_NEW;
				htb.put(BbsForumDB.FORUM_KEY,"");
				ret			=	CREATE_FORUM;
				
			}else{
				htb.put(MESSAGE,msg);
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;	// WEB表示
			}

		}else if(cmd.equals("EDIT")){
			/*
			 * 作成者かどうか調べて編集権をチェックする
			 * 権利があれば、close や hidden でも編集できる
			 */
			if(permission( Gear.strHash(htb, BbsForumDB.FORUM_KEY) )){
				disp_mode	=	DISP_NEW;
				ret			=	CREATE_FORUM;
			}else{
				htb.put(MESSAGE,"★ 編集する権利がありません");
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;	// WEB表示
			}

		}else if(cmd.equals("DELETE")){
			/* 
			 * 作成者かどうか調べて編集権をチェックする
			 * このフォーラムを削除する
			 * スレッドやポストも再帰的に削除する
			 */
			if(permission( Gear.strHash(htb, BbsForumDB.FORUM_KEY) )){
				delete(Gear.strHash(htb, BbsForumDB.FORUM_KEY));
				disp_mode	=	DISP_NEW;
				ret			=	DISPATCH_DEFAULT;
			}else{
				htb.put(MESSAGE,"★ 編集する権利がありません");
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;	// WEB表示
			}			

		}else if(cmd.equals("LOCK")){
			/*
			 * フォーラムを編集不可にする (CLOSE)
			 * 見ることは出来るが書き込むことはできない
			 */
			if(permission( Gear.strHash(htb, BbsForumDB.FORUM_KEY) )){
				lock();
				disp_mode	=	DISP_NEW;
				ret			=	DISPATCH_DEFAULT;
			}else{
				htb.put(MESSAGE,"★ 編集する権利がありません");
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;	// WEB表示
			}

		}else if(cmd.equals("UNLOCK")){
			/*
			 * フォーラムを編集可能(ACTIVE)に戻す
			 */
			if(permission( Gear.strHash(htb, BbsForumDB.FORUM_KEY) )){
				unlock();
				disp_mode	=	DISP_NEW;
				ret			=	DISPATCH_DEFAULT;
			}else{
				htb.put(MESSAGE,"★ 編集する権利がありません");
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;	// WEB表示
			}

		}else if(cmd.equals("GO_THREAD")){
			disp_mode	=	DISP_NEW;
			ret			=	DISPATCH_THREAD;
		    
		}else if(cmd.equals("POOLING")){
		    /*
		     * フォーラムをプーリング状態にして表示から除外する
		     */
			if(permission( Gear.strHash(htb, BbsForumDB.FORUM_KEY) )){
			    db.updateRelation(ownerid, getParameter("forumkey"), KeyGen.poolingRelation() );
				disp_mode	=	DISP_NEW;
				ret			=	DISPATCH_DEFAULT;
				
			}else{
				htb.put(MESSAGE,"★ 編集する権利がありません");
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;	// WEB表示
			}
			
		}else if(cmd.equals("RETURN")){
			/* 
			 * 呼び出し元プログラムへ戻る
			 */ 
			ret	=	DISPATCH_RETURN;

		}else if(cmd.equals(DISPATCH_RELATION)){
		    /*
		     * フォーラムのインポート
		     */
		    if(division.equals(DIV_GUEST)||division.equals(DIV_STUDENT)){
				htb.put(MESSAGE,"★ 編集する権利がありません");
		    	disp_mode	=	DISP_EDIT;
		    	ret			=	DISPATCH_DEFAULT;	// WEB表示

		    }else{
				disp_mode	=	DISP_NEW;
				ret			=	DISPATCH_RELATION;
		    }
		    
		}else if(cmd.equals(DISPATCH_INFO)){
		    /*
		     * 個人情報の設定
		     */
		    if(division.equals(DIV_GUEST)){
				htb.put(MESSAGE,"★ 編集する権利がありません");
		    	disp_mode	=	DISP_EDIT;
		    	ret			=	DISPATCH_DEFAULT;	// WEB表示

		    }else{
		        disp_mode	=	DISP_NEW;
		        ret			=	DISPATCH_INFO;
		    }
		    
		}else{
			if(LOG.fa) LOG.println("BbsForum #dispatch() : cmd="+cmd);
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
	 * あるオーナーの全てのフォーラムレコードを読み出す
	 * 
	 * @return		レコードを要素にもつベクター．レコードがない場合はnullを返す
	 */
	public	 Vector	readRecords(){
		if(LOG.fa)	LOG.println("■ BbsForumRecord #readRecord() あるオーナーの全てのフォーラムレコードを読み出す");
			
		Vector	vrec	=	new	Vector(30);
		int	n		=	db.readBbsForums(ownerid, vrec);
		if(n==0)	return	null;
		return	vrec;
	}
	/** 
	 * ownerkey と forumkey を指定してひとつのレコードを読み結果をハッシュにいれて返す
	 * レコードがなかった場合戻り値は null
	 * @param ownerkey	オーナーキー
	 * @param forumkey	フォーラムキー
	 * @param db		フォーラムデータベース操作オブジェクト
	 * @return			レコードを格納したハッシュ
	 */
	public	 Hashtable	readRecord(String forumkey){
		if(LOG.fa)	LOG.println("■ BbsForumRecord #readRecord() ひとつのレコードを読み結果をハッシュにいれて返す");
			
		Hashtable	tb	=	new	Hashtable(20);
		int	n		=	db.readBbsForums(ownerid, forumkey, tb);
		if(n==0)	return	null;
		return	tb;
	}
	/**
	 * 
	 */
	String	permissionCreate(){
		/*
		 * ユーザー区分がスーパーユーザーか教師かTAなら権利あり
		 */
		if( division.equals( BbsInfoDB.SUPER) )		return	"";	// super user
		if( division.equals( BbsInfoDB.TEACHER) )	return	"";
		if( division.equals( BbsInfoDB.ASSISTANT) )	return	"";
		return	"★ この操作を行う権利がありません";		
	}
	/**
	 * ユーザー情報を新規に作成して返す
	 * 
	 * @return
	 */
	Hashtable	createInfoRecord(String forumOwnerKey){
		if(LOG.fa) LOG.println("■ BbsPost #createInfoRecord()");

		Hashtable	dt		=	BbsInfoDB.getInitialProfile(forumOwnerKey,"不明");
		BbsInfoDB	infodb	=	new BbsInfoDB(forumOwnerKey, szDB, broker);
		infodb.insertBbsInfo(forumOwnerKey, dt);
		return		dt;
	}
	/**
	 * このフォーラムリストにアクセスしているユーザーが、特定のフォーラムの
	 * 更新・削除権を持つかどうか判定する
	 * @param 		forumkey
	 * @return		権利があるとき true
	 */
	boolean	permission(String forumkey){
		if(LOG.fa) LOG.println("■ BbsForum #permission()");
		/*
		 * アクセスしているユーザーIDがフォーラム所有者IDと同じならば、更新・削除権がある
		 */
		String	realOwnerId	=	getRealOwnerId(forumkey);
		if(ownerid.equals(userid))		return	true;
		/*
		 * スーパーユーザーならば更新・削除できる 
		 */
		if( division.equals( BbsInfoDB.SUPER) )	return	true;	// super user
		
		return	false;
	}
	/**
	 * あるフォーラムのオーナーIDを得る
	 * @param forumkey
	 * @return
	 */
	String	getRealOwnerId(String forumkey){
		if(LOG.fa) LOG.println("■ BbsForum #getRealOwnerId()");
		
		Hashtable	record	=	readRecord(forumkey);
		return		Gear.strHash(record, BbsForumDB.OWNER_KEY);
	}
	
	/** 
	 * このフォーラムを削除する
	 * スレッドやポストも再帰的に削除する
	 */
	void	delete(String forumkey){
		deleteArticle(forumkey);	// 記事
		deleteFiles(forumkey);	// ファイル
		
	}

	/**
	 * フォーラムをデータベースから削除する
	 *
	 */
	void	deleteArticle(String forumkey){
		/*
		 * 所属するスレッドと配下の記事を全て削除する
		 */
		String	owner_forumkey	=	BbsKeys.ownerForumKey(ownerid, forumkey);
		deleteThreadRecords(owner_forumkey);
		/*
		 * フォーラムを削除する
		 */
		db.deleteBbsForum(ownerid, forumkey);	
	}
	/**
	 * 所属するスレッドと配下の記事を全て削除する
	 * @param owner_forumkey
	 */
	void	deleteThreadRecords(String owner_forumkey){
		/*
		 * スレッドと配下の記事を削除する
		 */
		Vector	threadkeys		=	getThreadkeys(owner_forumkey);
		for(int i=0; i<threadkeys.size(); i++){
			/*
			 * ひとつのスレッドを削除
			 */
			deleteThread(owner_forumkey, (String)threadkeys.get(i));
		}
	}
	/**
	 * ひとつのスレッドを消す
	 * @param owner_forumkey
	 * @param threadkey
	 */
	void	deleteThread(String owner_forumkey, String threadkey){
		
		/*
		 * スレッド内の記事を全て消す
		 */
		BbsPostDB	postDB	=	new BbsPostDB(szDB, broker);
		postDB.deleteAllBbsPost(BbsKeys.bbsKey(owner_forumkey, threadkey));
		/*
		 * スレッドを消す
		 */
		BbsThreadDB	threadDB	=	new BbsThreadDB(szDB, broker);
		threadDB.deleteBbsThread(owner_forumkey, threadkey);
		return;		
		
	}
	/**
	 * 特定のフォーラム配下の全スレッドキーをベクターに求める
	 * 
	 * @param 		forumkey
	 * @return		全スレッドキーを格納したベクター
	 */
	Vector	getThreadkeys(String owner_forumkey){
				
		Vector		v			=	new Vector(100);
		BbsThreadDB	threadDB	=	new BbsThreadDB(szDB, broker);
		threadDB.getKeys(owner_forumkey, v);
		return	v;
	}
	/**
	 * スレッドに関連したファイルをディレクトリごと削除する
	 * 配下の記事に関連したディレクトリも全て削除される
	 */
	void	deleteFiles(String forumkey){

		String	dir	=	conf.forumDir(ownerid, forumkey);
		Gear.deleteDir(new File(dir));		

	}

	/**
	 * フォーラムを編集不可にする (CLOSE)
	 * 見ることは出来るが書き込むことはできない
	 * フラグを変更してDBにレコードを書き込む
	 */
	void	lock(){
		String	forumkey	=	Gear.strHashSP(htb, BbsForumDB.FORUM_KEY);
		alterForumAlive(forumkey, BbsForumDB.CLOSED);
	}
	/**
	 * フォーラムを編集可能(ACTIVE)に戻す
	 * フラグを変更してDBにレコードを書き込む
	 */
	void	unlock(){
		String	forumkey	=	Gear.strHashSP(htb, BbsForumDB.FORUM_KEY);
		alterForumAlive(forumkey, BbsForumDB.ACTIVE);
	}
	/**
	 * フォーラムのアライブフラグを変更する
	 * システムハッシュにも変更後の値を反映しておく
	 */
	void	alterForumAlive(String forumkey, String alive){
		db.updateAlive(ownerid, forumkey, alive);
		htb.put(BbsForumDB.ALIVE_FLAG, alive);
	}

	/*////////////////////////////////////////////////////////////////////////////
	 * 
	 *    表 示 処 理
	 * 
	 * 　　    表示処理ははコントローラーが呼び出す表示メソッドであり
	 * 　　    直接呼び出すことはできない．（機能しない）
	 * 
	 *////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 画面表示
	 * 
	 */
	@Override
	public  void	display(boolean editmode){
		if(LOG.fa)	LOG.println("■ BbsForum #display(boolean editmode)");
		if(!editmode){
			htb.put(MESSAGE,"");
		}
		
		/* strHash(htb,DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v,htb);
		
	}
	@Override
	public	void	write(String key,Vector exHtml){
	    
	    if(key.equals("forumList")){
	        forumListSub(exHtml);
	        
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
	public	void	forumListSub(Vector exHtml){

		Vector	v	=	new Vector(15);
		/*
		 * リレーションキーが一致しないものは表示しない
		 * isNoListSub() のためにフォーラムの数をforumCountsに残す
		 */			
		forumCounts	=	db.readBbsForums(ownerid, relation, v);
		
		int	n	=	v.size();
		for(int i=0; i<n; i++){
			
			Hashtable	tb		=	(Hashtable)v.get(i);
			/*
			 * フォーラム状態表示アイコンの設定
			 */
			String	alive		=	Gear.strHashSP(tb,(BbsForumDB.ALIVE_FLAG));
			String	signIcon	=	"lockIconOpen.gif";
			if(alive.equals(BbsForumDB.HIDDEN)||alive.equals(BbsForumDB.CLOSED)){
				signIcon	=	"lockIcon.gif";
			}
			htb.put("_signIcon", signIcon);
			/*
			 * フォーラム関係の表示データ
			 */
			String	forumKey		=	Gear.strHashSP(tb,(BbsForumDB.FORUM_KEY));
			String	date			=	Gear.strHashSP(tb,(BbsForumDB.DATE));
			//String	forumOwner		=	Gear.strHashSP(tb,(BbsForumDB.OWNER_KEY));
			String	forumOwner		=	ownerid;
			String	forumOwnerName	=	BbsUtil.getOwnerName(forumOwner,szDB, broker);
			String	subject			=	Gear.strHashSP(tb,(BbsForumDB.SUBJECT));
			String	content			=	Gear.strHashSP(tb,(BbsForumDB.CONTENT));
			String	attach			=	Gear.strHashSP(tb,(BbsForumDB.ATTACHMENT));
			/*
			 * 名前で表示するかハンドルで表示するかハンドルフラグから決定してハッシュに設定する
			 */
			
			String		handleFlag	=	Gear.strHash(tb,BbsForumDB.HANDLE_FLAG);
			String		dispName	=	getDispName(forumOwner, handleFlag);
			
			/*
			 * その他の項目をハッシュに設定する
			 */ 
			htb.put(BbsForumDB.FORUM_KEY, 	forumKey);
			htb.put(BbsForumDB.DATE, 		BbsUtil.getFormattedDate(tb));
			htb.put("_ownerName", 			dispName);
			htb.put(BbsForumDB.SUBJECT, 	subject);
			htb.put(BbsForumDB.CONTENT, 	getContent(content, forumKey));
			htb.put(BbsForumDB.ATTACHMENT, 	getAttachmentHTML(tb));
			/*
			 * その他の詳細情報を作成してシステムハッシュに入れる
			 */
			Vector threadRecords	=	getThreadRecords(forumKey);
			setThreadInfo(htb, threadRecords, forumKey, handleFlag);
			
			printVector(exHtml);
		}
	    
	}
	public	void	SubjectSub(Vector exHtml){
		String	dt	=	Gear.strHashSP(htb, BbsForumDB.SUBJECT);
		if(!isEmpty(dt)){
			printVector(exHtml);
		}
	}
	public	void	ContentSub(Vector exHtml){
		String	dt	=	Gear.strHashSP(htb, BbsForumDB.CONTENT);
		if(!isEmpty(dt)){
			printVector(exHtml);
		}
	}
	public	void	AttachSub(Vector exHtml){
		String	dt	=	Gear.strHashSP(htb, BbsForumDB.ATTACHMENT);
		if(!isEmpty(dt)){
			printVector(exHtml);
		}
	}
	public	void	isNoListSub(Vector exHtml){
		if(forumCounts > 0)	return;
		printVector(exHtml);
	}

	/** スレッド・ポスト情報をセットする
	 * 
	 * @param h
	 * @param forumkey
	 */
	void	setThreadInfo(Hashtable h, Vector v, String forumkey, String handleFlag){
		if(LOG.fa)	LOG.println("■ BbsForum #setThreadInfo()");
		int		threads			=	0;
		int		posts			=	0;
		long		latestDateValue	=	0L;
		String		latestDispName	=	"";
		Hashtable	postRecord		=	null;
		/*
		 * 全てのスレッドについて
		 */
		threads			= v.size();
		for(int i=0; i<threads; i++){
			/* 
			 * ひとつのスレッド内の全ポストレコードを得る
			 */
			Hashtable	dh			=	(Hashtable)v.get(i);
			String		threadkey	=	strHash(dh, "_threadkey");	
			Vector		recs		=	getPostRecords(forumkey, threadkey);

			/*
			 * ポスト記事があればカウントする
			 */
			if(recs.size()>0){
				// 最新のポスト記事
				postRecord				=	(Hashtable)recs.get(0);
				/*
				 * POST件数を累計する
				 * 累積最新日付を作成する
				 * 最新投稿者表示名を保存する
				 */
				posts					+=	recs.size();
				long dateValue			=	Long.parseLong(Gear.strHash(postRecord, BbsPostDB.DATE));
				if(dateValue > latestDateValue){
					latestDateValue		=	dateValue;
					String	id			=	Gear.strHash(postRecord, BbsPostDB.USERID);
					latestDispName		=	getDispName(id, handleFlag);
				}
			}
		}
		//
		h.put("_pn", String.valueOf(posts));	
		h.put("_tn", String.valueOf(threads));	
		h.put("_postUser", latestDispName);
		if(latestDateValue>0){
			h.put("_latestDate", Gear.gatDateFromMili(latestDateValue));		
		}else{
			h.put("_latestDate", "-");
		}
	}

	/**
	 * このフォーラムの全てのスレッドレコードを得る
	 * @param forumkey
	 * @return
	 */
	Vector	getThreadRecords(String forumkey){
		BbsThreadDB	tdb	=	new BbsThreadDB(szDB,broker);
		Vector		v	=	new Vector(15);
		tdb.readBbsThreads(BbsKeys.ownerForumKey(ownerid,forumkey), v);
		
		return	v;		
	}
	/**
	 * 日付で降順に並べて、このスレッドにかかる全てのポストレコードをベクターで得る
	 * @param threadkey
	 * @return
	 */
	Vector	getPostRecords(String forumkey, String threadkey){
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
	 * IDからハンドルフラグを考慮して、表示すべきユーザー名を返す
	 * ユーザー情報が登録されていなければ仮登録し、ユーザーアイコンも確定する
	 *
	 * @param user_key
	 * @return
	 */
	String getDispName(String user_key,String handleFlag){
		
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
	 * ユーザーIDからユーザー情報をハッシュテーブルで得る
	 * 存在しない時は null を返す
	 * 
	 * @return
	 */
	Hashtable	getInfoRecord(String uid){
		BbsInfoDB	infodb	=	new BbsInfoDB(uid, szDB, broker);
		Hashtable	dt		=	new Hashtable();
		int		count	=	infodb.readBbsInfo(uid, dt);
		if(count>0)	return	dt;
		return			null;
	}

	/**
	 * システムアイコンを使ってユーザーフェースアイコンを初期登録する
	 * 
	 * @return	ユーザーアイコンへのＵＲＬを返す
	 */
	String	initFaceIcon(String forumOwnerKey){
		
		int	pos			=	getSysFaceIconNumber();		// random 
		String	fname		=	getSysFaceIconName(pos);
		String	fromPath	=	getSysFaceIconDir(fname);
		/*
		 * データベースへ記録
		 */
		updateDB(forumOwnerKey, fname);
		/*
		 * システムディレクトリからユーザープロファイルディレクトリへコピー
		 */
		copyToProfileDir(forumOwnerKey,fromPath,fname);
		/*
		 * ユーザープロファイルURLを返す
		 */
		String	url			=	userIconUrl(forumOwnerKey, fname);
		return	url;
	}
	/**
	 * ユーザーアイコンファイル名をユーザー情報ＤＢへ記録する
	 * @param fname
	 */
	void	updateDB(String forumOwnerKey, String fname){

		BbsInfoDB	infoDB	=	new BbsInfoDB(forumOwnerKey, szDB, broker);
		infoDB.updateIcon(forumOwnerKey, fname);
	}
	/**
	 * システムアイコンをプロファイルへコピーする
	 * @param fromfilePath
	 * @param fname
	 */
	void	copyToProfileDir(String forumOwnerKey, String fromfilePath, String fname){
		
		/*
		 * プロファイルディレクトリがなければ作る
		 */
		String 	profileDir	=	conf.profilePath(forumOwnerKey, szDB);
		File	pfp			= 	new File(profileDir);
		if(!pfp.exists()){
			pfp.mkdirs();
		}
		/*
		 * ファイルをコピーする
		 */
		try{
			Gear.copyBinryFile(fromfilePath, conf.profilePath(forumOwnerKey, szDB)+fname);
					
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	/**
	 * ユーザーのフェースアイコンへのＵＲＬを得る
	 * @param inforec
	 * @return
	 */
	String	getUserFaceIconURL(String forumOwnerKey, Hashtable inforec){
		if(LOG.fa) LOG.println("■ BbsPost #getUserFaceIcon()");
		
		String	url		=	"";
		String	fname	=	Gear.strHash(inforec, BbsInfoDB.ICON_FILE);
		if(Gear.isEmpty(fname)){
			/*
			 * システムアイコンを当てる
			 * ユーザーＤＢにも登録する
			 */
			url	=	initFaceIcon(forumOwnerKey);
						
		}else{
			url		=	userIconUrl(forumOwnerKey, fname);	
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
	String	userIconUrl( String forumOwnerKey, String fname){
		if(LOG.fa) LOG.println("■ BbsPost #getFaceIcon()");

		return	conf.userProfileURL(forumOwnerKey, szDB) + fname;
	}
	/**
	 * PMLをパースしたHTMLを返す
	 * @param content	内容を表すPML
	 * @return			同 HTML
	 */
	public	String	getContent(String content, String forumkey){
		logger.debug("★ getContent(String content, String forumkey)");
		/*
		 * pmlテキストに段落エイリアスを付加する
		 * テンプレートがあるのでそれを読み込んでアペンドする
		 */
		//if(DBG.fa) DBG.println("■ BbsForum #getContent : fileName = " + conf.pmlTemplatePath());
		TemplateBox		tb			=	new TemplateBox(conf.pmlTemplatePath());
		String			template	=	tb.get("forum");
		String			emptext		=	template + Gear.lineSeparator() + content;
		logger.debug("★パース前のテキスト");
		logger.debug(emptext);
		
		/*
		 * setImgPath() はグラフィックスへの完全パス．/bbs/[ownerid]/[forumkey]/ を設定する
		 * setImgDestinationPath()はグラフィックスの絶対ディレクトリパス． /home/bbs/[ownerid]/[forumkey]/  を設定する
		 */
		Exam	exam	= 	new Exam(emptext ,para.getEmlConfPath());
		exam.setImgPath(conf.forumUrl(ownerid, forumkey) + "/");
		exam.setImgDestinationPath(conf.forumDir(ownerid, forumkey) + Gear.fileSeparator());
		return	exam.createHtml();
	}
	/**
	 * リンクつきの添付ファイル用ＨＴＭＬを返す
	 */
	public	String	getAttachmentHTML(Hashtable record){
		if(LOG.fa)	LOG.println("■ BbsForum #getAttachmentHTML()");
		
		String	forumkey	=	strHash(record,(BbsForumDB.FORUM_KEY));
		/*
		 * 添付ファイル名はCSV形式文字列なのでCsvオブジェクトに直す
		 */
		//Csv	cs	=	new Csv(Gear.strHashSP(record, BbsForumDB.ATTACHMENT));
		// セパレータを '?' に変更したので、従来のデータと互換性を取るためには
		// このメソッドでCsvを作成する必要がある
		Csv	cs	=	BbsUtil.makeAttachmentCsv(Gear.strHashSP(record, BbsForumDB.ATTACHMENT));
		
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
			String	fileUrl		=	conf.forumUrl(ownerid, forumkey) + "/" + fileName;
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
		if(LOG.fa)	LOG.println("■ BbsMakeForum #getFilename()");

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
		if(LOG.fa)	LOG.println("■ BbsMakeForum #getColorClass()");

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
		if(LOG.fa)	LOG.println("■ BbsMakeForum #isAttachment()");

		if(pos >= cs.size())	return	false;
		
		String	fname	=	cs.get(pos);
		if(fname.charAt(0)=='*'){
			return	true;
		}
		return	false;			
	}

}
