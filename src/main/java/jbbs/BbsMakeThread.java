/*
*
*/
package jbbs;
import tktools.*;

import java.io.*;
import java.util.*;

import database.DbConnectionBroker;

import framework.LOG;
import framework.Param;
import framework.SuperPlayer;

/**
	<program $jbbs.BbsMakeThread>
		<dispatch  html=mkThread.html    number=3220       class=jbbs.BbsMakeThread />
		<variable>
		  <receive  NUMBER STAMP  GROUP   BBS_OWNER_KEY UID MAIL UNAME  HOMEURL DIVISION forumkey threadkey />
		  <accept   SUBMIT UPLODE deleteFile chgFile />
		  <keep      editor date attachment views />
		  
		  <form      content listsw tagmark />
		  
		</variable>
	</program>
 * 
 *  1. accept 変数
 *	
 *  2. keep 変数
 * 		editor			エディター領域の表示行数
 * 		date		
 * 		attachment		アップロードファイルリスト
 * 		views		
 * 
 *  3. form 変数
 * 		content			記述したテキスト
 * 		listsw			アップロードファイル一覧を表示するかどうかのスイッチ ("ON" / "OFF")
 *     	tagmark			現在のタグマーカー文字．リストボックス
*/
public class BbsMakeThread   extends SuperPlayer implements BbsVar{

	BbsConf				conf;
	//
	BbsThreadDB			db;
	String				ownerkey;	// フォーラムの所有者
	String				szDB;
	DbConnectionBroker	broker;
	//
	BbsKeys				bbskeys;
	Hashtable			record;
	String				forumkey;
	String				owner_forumkey;
	/*
	 * スレッドキーは値が入っていない場合は新規
	 */
	String				threadkey;
	//
	String				format;
	String				userid;		// このスレッドを作成しようとしている人のID
	Hashtable			userinfo;	// その情報レコード
	
	public BbsMakeThread(){
		super();
	}
	public	 void		initialize(PrintWriter out,Hashtable htb, Param parameter){
		super.setInit(out, htb, parameter);
		//
		if(LOG.fa)	LOG.println("■ BbsMakeThread #initialize()");
		conf			=	createBbsConf(para.get(BBS_CONF_PATH),htb);		
		//
		ownerkey		=	Gear.strHash(htb, BBS_OWNER_KEY);
		forumkey		=	Gear.strHash(htb, BbsForumDB.FORUM_KEY);
		owner_forumkey	=	BbsKeys.ownerForumKey(ownerkey, forumkey);
		htb.put(BbsThreadDB.OWNER_FORUM_KEY	, owner_forumkey);

		/*
		 * forumkey が設定されていない場合は新規作成である．
		 * BbsDUMMY_KEY　を仮のキーとして設定しておく
		 */
		threadkey		=	Gear.strHash(htb,BbsThreadDB.THREAD_KEY);
		if(isEmpty(threadkey)){
			if(LOG.fa) LOG.println("■ BbsMakeThread #initialize()　□ threadkey is empty!");
			threadkey	=	DUMMY_KEY;
			htb.put(BbsThreadDB.THREAD_KEY, DUMMY_KEY);
		}
		szDB			=	Gear.strHash(htb, GROUP);
		broker			=	(DbConnectionBroker)htb.get(BROKER);
		db				=	new BbsThreadDB(szDB, broker);		
		bbskeys			=	new BbsKeys(ownerkey, szDB, broker);
		
		userid			=	Gear.strHash(htb, UID);
		userinfo		=	setUserInfoRecord(userid);
		
	}
	/**
	 * ユーザー情報がなければ初期値を作成する．初期値でデータベースレコードも作成する
	 * @param userid
	 * @return
	 */
	Hashtable	setUserInfoRecord(String userid){
		Hashtable	record	=	BbsUtil.getOwnerRecord(userid, szDB, broker);
		if(record==null){
			/**
			 *  初期値をハッシュにセットする（教師用依存）
			 *  パスワードはBbsLoginで使用するが、BbsLoginでは記載されていない場合は
			 *  membersInfo 及び szDB をチェックするので、空白を書いても問題ない 
			 */
		    record	=	
		        BbsUtil.setInitialData(getParameter(UID), "", getParameter(DIVISION), getParameter(UNAME), getParameter(MAIL), para);
			BbsInfoDB	infodb	=	new BbsInfoDB(userid, szDB, broker);
			infodb.insertBbsInfo(userid, record);
		}
		return	record;
	}	
	/**
	 * jbbsライブラリのためのシステムコンフィギュレーションオブジェクトを作成する
	 */
	public BbsConf	createBbsConf(String BbsConfPath, Hashtable ht){
		if(LOG.fa)	LOG.println("■ BbsMakeThread #createBbsConf()");
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
	
	public  String	dispatch(){
		/*
		 * 処理分岐を判断するキー(cmd)はシステムハッシュからDISPATCH_KEYをキーとして取り出す
		 * リターンコード(ret)と次に表示するWEBの表示モード(disp_mode)は設定忘れを防ぐため規定値をセットしておく
		 * 表示メッセージもクリアしておく
		 */
		String 	cmd			=	strHash(htb,DISPATCH_KEY);
		String	ret			=	DISPATCH_DEFAULT;			// WEB表示
		String	disp_mode	=	DISP_NEW;					// 新規表示
		htb.put(NUMBER,"");

		if(LOG.fa) LOG.outHash(htb,"■ BbsMakeThread #dispatch() key:" + cmd);
		
		if(cmd.equals("VIEW")){
			/*
			 * システムハッシュには対象とするスレッドキー(threadkey)が入っているので
			 * これを使ってレコードを読み出して表示する．具体的な処理は display()． 
			 */
			disp_mode	=	DISP_NEW;
			ret			=	DISPATCH_DEFAULT;	// WEB表示
		
		}else if(cmd.equals("WRITE")){
			/* 失敗するかもしれない */
			boolean	ok	=	writeRecord();
			if(!ok){
			    putParameter(MESSAGE, "★表題を記入してください");
			}
			disp_mode	=	DISP_EDIT;
			
		}else if(cmd.equals("UPLOAD")){
			/* ファイル受信 */
			boolean	ok	=	upload();
			if(!ok){
			    putParameter(MESSAGE, "★先に表題を記入してください");
			}
			disp_mode	=	DISP_EDIT;
		
		}else if(cmd.equals("FILE_EDIT")){
			delete();
			disp_mode	=	DISP_EDIT;

		}else if(cmd.equals("FILEMODE")){
		    /*
		     * 添付かアップロードか入れ替える
		     */
		    filemode();
		    //delete();
			disp_mode	=	DISP_EDIT;
		    
		}else if( cmd.equals("FLIST") ){
			/*
			 * ファイルリストの表示状態変更
			 */
		    ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;
			
		}else if(cmd.equals("PLUS")){
			puls();
			disp_mode	=	DISP_EDIT;
			
		}else if(cmd.equals("MINUS")){
			minus();
			disp_mode	=	DISP_EDIT;

		}else if( cmd.equals("TAB") ){
			/*
			 * タブをスペースに変換する
			 */
		    tabToSpace();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
			
		}else if(cmd.equals("RETURN")){
			/* 
			 * 呼び出し元プログラムへ戻る
			 */ 
			ret	=	DISPATCH_RETURN;

		}else if(cmd.equals("PREVIEW")){
			/* 
			 * プレビューを表示する
			 * 表示に必要なデータはシステムハッシュに入っている
			 */
			disp_mode	=	DISP_NEW;
			ret			=	PREV_THREAD;
			
		}else if(cmd.equals("EXECUTE")){
			/*
			 *　プログラムの実行
			 */
			disp_mode	=	DISP_NEW;
			ret			=	getParameter(PROGRAM);

		}else if(cmd.equals("INFO")){
			/*
			 *　個人情報設定
			 */
			disp_mode	=	DISP_NEW;
			ret			=	DISPATCH_INFO;

		}else{
			/*
			 *　再表示
			 */
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
		}
		/*
		 * 表示モードをセットしてからリターンする
		 */
		htb.put(DISP_KEY,disp_mode);
		return	ret;
	}
	void	filemode(){
	    
		String	filename	=	Gear.strHash(htb, CHG_FILE);
		if(isEmpty(filename)){
		    if(LOG.fa) LOG.println("filename is null");
		    return;
		}
		/*
		 * アップロードファイル情報を更新する（システムハッシュ、DBに書き込み）
		 */
		Hashtable	ht		=	getAttachmentHash();
		String		mode	=	Gear.strHash(ht, filename);	// "*"(添付）　 ""（アップロード）	
		if(mode.equals("*")){
		    mode	=	"";
		}else{
		    mode	=	"*";
		}
		ht.put(filename, mode);
		writeAttachment(BbsUtil.getAttachmentCSV(ht));
		
	}	
	/**
	 * テキストのタブを半角スペース４個分に変換して，システムハッシュにセットする
	 * @return	変換後のテキスト
	 */
	String	tabToSpace(){
	    String	text	=	StringGear.tabToSpace( getParameter("content"), 4);
	    putParameter("content", text);
	    return	text;
	    
	}	
	/**
	 * レコードを出力する 
	 *
	 */
	public	boolean	writeRecord(){
		if(LOG.fa)	LOG.println("■ BbsMakeThread #writeRecord()");
		/*
		 * 表題がかいてあるかチェック
		 */
		if(!writeOK()){
		    return	false;
		}
		/*
		 * 新規モード、更新モードでレコードを書き込む
		 */
		boolean	result	=	true;
		if(threadkey.equals(DUMMY_KEY)){
			result	=	insertRecord();
		}else{
		    result	=	updateRecord();
		}
		return	result;
	}
	/**
	 * タイトルが書かれているかチェックする
	 * 
	 */
	boolean	writeOK(){
	    String	str	=	getParameter("subject");
	    if(isEmpty(str)){
	        return	false;
	    }
	    return	true;
	}
	/**
	 * 新規モードでレコードを作成して書き込む
	 *
	 */
	public	boolean	insertRecord(){
		if(LOG.fa)	LOG.println("■ BbsMakeThread #insertRecord()");
		/* 
		 * 新規レコードのデータをシステムハッシュにセットする
		 */
		threadkey	=	bbskeys.getThreadKey(owner_forumkey);
		htb.put(BbsThreadDB.OWNER_FORUM_KEY	, owner_forumkey);
		htb.put(BbsThreadDB.THREAD_KEY		, threadkey);
		htb.put(BbsThreadDB.DATE			, BbsUtil.getTodayStringInMilis());
		/*
		 * 最初にレコード書かないとファイルアップロードできないので新規では常に ""
		 */
		htb.put(BbsPostDB.ATTACHMENT	, "");
		/*
		 * 新規モードの時にはエディタ行数も更新する
		 */
		updateEditorRows();
		/*
		 * レコードをシステムハッシュから作成してデータベースに書く
		 */
		Hashtable	rec	=	getRecordForInsert();
		if(LOG.fa)	LOG.outHash(rec,"□ 作成した insert レコードの内容");
		int	cnt	=	db.insertBbsThread(rec);
		/*
		 * 件数０件ならエラー
		 */
		if(cnt==0){
		    return	false;
		}else{
		    return	true;
		}
	}

	/**
	 * システムハッシュに入っている入力データ（通常はwebからの入力）を元に、レコードを作る
	 * @param owner_forumkey
	 * @param threadkey
	 * @return
	 */
	public Hashtable getRecordForInsert(){
		if(LOG.fa) LOG.outHash(htb,"■ BbsMakeThread #getRecord()　開始時のシステムハッシュ");
		
		/*
		 * ハッシュテーブルの値からレコードを作成する 
		 */
		Hashtable	rec	=	new Hashtable();

		rec.put(BbsThreadDB.OWNER_FORUM_KEY	,Gear.strHashIncludeNull(htb,BbsThreadDB.OWNER_FORUM_KEY));
		rec.put(BbsThreadDB.THREAD_KEY		,Gear.strHashIncludeNull(htb,BbsThreadDB.THREAD_KEY));
		//
		rec.put(BbsThreadDB.USERID			,Gear.strHashIncludeNull(htb,UID));			// 常にある
		rec.put(BbsThreadDB.DATE			,Gear.strHash(htb,BbsThreadDB.DATE			,BbsUtil.getTodayStringInMilis()));
		rec.put(BbsThreadDB.RATING			,Gear.strHash(htb,BbsThreadDB.RATING		,""));
		rec.put(BbsThreadDB.SUBJECT			,Gear.strHash(htb,BbsThreadDB.SUBJECT		,""));
		//
		String	ct	=	BbsUtil.trimFormat(Gear.strHash(htb,BbsThreadDB.CONTENT	,""));
		putParameter(BbsThreadDB.CONTENT, ct);
		rec.put(BbsThreadDB.CONTENT, ct);
		
		rec.put(BbsThreadDB.ATTACHMENT		,Gear.strHash(htb,BbsThreadDB.ATTACHMENT	,""));
		rec.put(BbsThreadDB.VIEWS			,Gear.strHash(htb,BbsThreadDB.VIEWS			,""));
		//
		if(LOG.fa){
			LOG.outHash(rec, "★ BbsMakeThread #getRecord() で作成したレコード");
		}
		return	rec;
	}		
	/**
	 * 更新モードでレコードを作成して書き込む
	 * 
	 */
	public	boolean	updateRecord(){
		if(LOG.fa)	LOG.println("■ BbsMakeThread #updateRecord()");
		/*
		 * レコードをシステムハッシュから作成してデータベースに書く
		 */
		Hashtable	rec	=	getRecordForUpdate();
		if(LOG.fa)	LOG.outHash(rec,"□ 作成した update レコードの内容");
		int	cnt	=	db.updateBbsThread(owner_forumkey, threadkey, rec);
		/*
		 * 件数０件ならエラー
		 */
		if(cnt==0){
		    return	false;
		}else{
		    return	true;
		}		
	}

	/**
	 * システムハッシュに入っている入力データ（通常はwebからの入力）を元に、レコードを作る
	 * @param owner_forumkey
	 * @param threadkey
	 * @return
	 */
	public Hashtable getRecordForUpdate(){
		if(LOG.fa) LOG.outHash(htb,"■ BbsMakeThread #getRecordForUpdate()　開始時のシステムハッシュ");
		
		/*
		 * レコードをＤＢから読み出し、ハッシュの値で必要な部分のみ上書きする 
		 */
		Hashtable	rec	=	readRecord(threadkey);
		/*
		 * 	更新ではキー変更はない
		 */
		//rec.put(BbsThreadDB.OWNER_FORUM_KEY	,Gear.strHashAlart(htb,BbsThreadDB.OWNER_FORUM_KEY));
		//rec.put(BbsThreadDB.THREAD_KEY		,Gear.strHashAlart(htb,BbsThreadDB.THREAD_KEY));
		//rec.put(BbsThreadDB.USERID			,Gear.strHashAlart(htb,UID));			// 常にある
		
		rec.put(BbsThreadDB.DATE			,Gear.strHash(htb,BbsThreadDB.DATE			,BbsUtil.getTodayStringInMilis()));
		rec.put(BbsThreadDB.RATING			,Gear.strHash(htb,BbsThreadDB.RATING		,""));
		rec.put(BbsThreadDB.SUBJECT			,Gear.strHash(htb,BbsThreadDB.SUBJECT		,""));
		//
		String	ct	=	BbsUtil.trimFormat(Gear.strHash(htb,BbsThreadDB.CONTENT	,""));
		putParameter(BbsThreadDB.CONTENT, ct);
		rec.put(BbsThreadDB.CONTENT, ct);
		//
		rec.put(BbsThreadDB.ATTACHMENT		,Gear.strHash(htb,BbsThreadDB.ATTACHMENT	,""));
		rec.put(BbsThreadDB.VIEWS			,Gear.strHash(htb,BbsThreadDB.VIEWS			,""));
		//
		if(LOG.fa){
			LOG.outHash(rec, "★ BbsMakeThread #getRecord() で作成したレコード");
		}
		return	rec;
	}
	/**
	 * あるファオラムの全てのスレッドを読み出す
	 * 
	 * @return		レコードを要素にもつベクター．レコードがない場合はnullを返す
	 */
	public	 Vector	readRecords(){
		if(LOG.fa)	LOG.println("■ BbsMakeThread #readRecord() あるオーナーの全てのフォーラムレコードを読み出す");
			
		Vector	vrec	=	new	Vector(30);
		int	n		=	db.readBbsThreads(owner_forumkey, vrec);
		if(n==0)	return	null;
		return	vrec;
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
	 * スレッドレコードの view の値を更新する
	 * @param delta
	 * @param owner_forumkey
	 * @param threadkey
	 * @param db
	 */
	public	 void updateViews(String	delta, String threadkey){

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
	
	boolean	isOK(){
		if(isEmpty(Gear.strHashSP(htb, BbsThreadDB.SUBJECT))){
			htb.put(MESSAGE, "★ タイトルは省略できません");
			return	false;
		}
		if(isEmpty(Gear.strHashSP(htb, BbsThreadDB.CONTENT))){
			htb.put(MESSAGE, "★ 内容は省略できません");
			return	false;
		}
		return	true;
	}
	
	/**
	 * アップロードされたファイルを受け取る
	 */
	boolean	upload(){
		if(LOG.fa)	LOG.println("■ BbsMakeThread #upload()");
		/*
		 * ファイル受信にはディレクトリを特定するため threadkey が必要なので
		 * レコードの書き込みを行う．この中で threadkey が確定する
		 */
		if(threadkey.equals(DUMMY_KEY)){
			/*
			 * タイトルが書かれていないとエラーになる
			 */
		    boolean	result	=	writeRecord();
			if(!result){
			    return	false;
			}
		}
		/*
		 * 添付ファイルかどうか
		 */
		String 	fname	=	getFile( conf.threadDir(ownerkey, forumkey, threadkey) );	// 移動
		String	attach	=	Gear.strHash(htb,"_attach");
		/*
		 * DBを読んでattachmentを取得し、アップロードファルを更新する
		 */
		Hashtable	ht		=	getAttachmentHash();	// from DB
		if(attach.equals("1")){
			ht.put(fname, "*");
			
		}else{
		    if(FileGear.isZipfile(fname)){
		        /*
		         * zipファイルを解凍する
		         */
		        String	zipPath		=	conf.threadDir(ownerkey, forumkey, threadkey) + FS +fname;
		        String	unzipDir	=	conf.threadDir(ownerkey, forumkey, threadkey);
		        Vector	fileList	=	FileGear.unzip(zipPath, unzipDir);
		        /* zip は削除しておく */
		        (new File(zipPath)).delete();		        
		        /*
		         * 全てのファイルを登録する
		         */
		        int	sz	=	fileList.size();
		        for(int	i=0; i<sz; i++){
		            ht.put((String)fileList.get(i), "");
		        }
		    }else{
		        ht.put(fname,"");
		    }
		}
		writeAttachment(BbsUtil.getAttachmentCSV(ht));	//	to DB
		return	true;
		
	}
	
	/**
	 * システムハッシュのアップロードファイルリストを更新し、さらにデータベースも更新する
	 * @param attachmentStr
	 */
	void	writeAttachment(String attachmentStr){
		if(LOG.fa)	LOG.println("■ BbsMakeThread #writeAttachment()");
		
		htb.put(BbsThreadDB.ATTACHMENT, attachmentStr);
		updateRecord();
	}

	/**
	 * アップロードされたファイルをプロファイルディレクトリへ移す
	 * 複数ファイルの同時アップロードに対応している
	 * アップロード先のファイル記述子を返す
	 */
	String	getFile(String toDir){
		if(LOG.fa)	LOG.println("■ BbsMakeThread #getFile()");
		/*
		 * 戻り値に使うのでファイル名を取得しておく
		 */
		String		fromDir	=	strHash(htb,UPLOAD_DIR_NAME);
		String	[]	files	=	(new File(fromDir)).list();
		String		fname	=	files[0];
		/*
		 *   /home/pc/temp/[ランダムに作成したディレクトリ名]   ⇒　/home/bbs/[owner]/[forum]/[thread]/
		 *  ファイルが複数あれば全てを移動する
		 */
		Gear.moveFiles(toDir, fromDir);
		
		/* 作業ディレクトリが残るので全て消去する */
		Gear.delDir(fromDir);
		
		return	fname;		
	}
	/**
	 * データベースからこのレコードのアップロードファイルリストを取得し、Hashtableオブジェクトにして返す
	 * 添付ファイルの場合はファイル名をキーとした list "*"を 設定し、そうでなければ "" を設定する
	 * 
	 * @return		アップロードファイルリスト
	 */	
	Hashtable	getAttachmentHash(){
		if(LOG.fa)	LOG.println("■ BbsMakeThread #getAttachmentHash()");

		/*
		 * データベースの attachment を読んでファイルリストとその種別を得る
		 */
		Hashtable	record		=	readRecord(threadkey);
		String		attachment	=	Gear.strHash(record,BbsThreadDB.ATTACHMENT);
		/*
		 * アップロードファイルをハッシュセットへ
		 * ファイル名をキーとして、添付ファイルでは値に"*"をセットする
		 */			
		Hashtable	list	=	new	Hashtable(20);
		Csv			cs		=	new	Csv(attachment);
		for(int i=0; i<cs.size(); i++){
			String	filename	=	getFilename(cs, i);
			if(isAttachment(cs,i)){
				list.put(filename,"*");
			}else{
				list.put(filename,"");
			}
		}
		return		list;		
	}
	/**
	 * アップロードファイル名を得る
	 * 添え字pos が範囲外なら空文字列を返す
	 * 添付ファイルなら１文字目のマーク(*)を取ったものを返す
	 */
	String	getFilename(Csv cs, int pos){
		if(LOG.fa)	LOG.println("■ BbsMakeThread #getFilename()");

		if(pos >= cs.size())	return	"";
		
		String	fname	=	cs.get(pos);
		if(fname.charAt(0)=='*'){
			return	fname.substring(1);
		}
		return	fname;
	}
	/**
	 * 添付ファイルかどうか
	 * @param cs
	 * @param pos
	 * @return	添付ファイルなら true を返す
	 */
	boolean	isAttachment(Csv cs, int pos){
		if(LOG.fa)	LOG.println("■ BbsMakeThread #isAttachment()");

		if(pos >= cs.size())	return	false;
		
		String	fname	=	cs.get(pos);
		if(fname.charAt(0)=='*'){
			return	true;
		}
		return	false;			
	}
	/**
	 * すでにアップロードされているファイルを削除する
	 * アップロードでスレッドが確定するので、この処理では必ず threadkey がある
	 * 添付ファイル情報が更新されるので、レコードの書き込みも行う
	 */	
	void	delete(){
		if(LOG.fa)	LOG.println("■ BbsMakeThread #file()");
		String	filename	=	Gear.strHash(htb,DELFILE);
		String	entry		=	filename.replace('/', File.separatorChar);
		delete(entry);
		/*
		 * アップロードファイル情報を更新する（システムハッシュ、DBに書き込み）
		 */
		Hashtable	ht	=	getAttachmentHash();
		ht.remove(filename);
		writeAttachment(BbsUtil.getAttachmentCSV(ht));
	}
	/**
	 * 指定のアップロードファイルを削除する
	 * @param fname
	 */
	void	delete(String fname){
		if(LOG.fa)	LOG.println("■ BbsMakeThread #delete()");
		File	fp		=	new File(conf.threadDir(ownerkey, forumkey,  threadkey) + File.separator + fname);
		fp.delete();
	}

	/**
	 * エディタの現在の設定行数を得る
	 * @return
	 */
	int getEditorRows(){
		if(LOG.fa)	LOG.println("■ BbsMakeThread #getEditorRows()");
		/*
		 * スレッドの所有者は ownerkey（フォーラムの所有者）ではなく userid で特定される
		 */
		Hashtable	tb	=	new Hashtable(20);
		BbsInfoDB	bi	=	new BbsInfoDB(userid, szDB, broker);
		bi.readBbsInfo(userid,tb);
		return	Integer.parseInt(strHash(tb, BbsInfoDB.EDITOR));
	}
	//
	//　行数を増やす
	//
	void	puls(){
		if(LOG.fa) LOG.println("■ BbsMakeThread #puls() : 行数を増やす の先頭です");
		//
		
		int	max			= BbsInfoDB.maxRows;	// 最大値
		int	min			= BbsInfoDB.minRows;	// 最小値
		int	delta		= BbsInfoDB.delta;		// 増分
		//
		String 	editor_rows	= strHash(htb,BbsInfoDB.EDITOR);
		int	rows		= Integer.parseInt(editor_rows);
		if(rows <= (max - delta) ){
			editor_rows	= String.valueOf(rows + delta);
			htb.put(BbsInfoDB.EDITOR, editor_rows);
		}
		return;
	}
	//
	// 行数を減らす
	//
	void	minus(){
		if(LOG.fa) LOG.println("■ BbsMakeThread #minus() : 行数を減らす の先頭です");
		//
		int	max			= BbsInfoDB.maxRows;	// 最大値
		int	min			= BbsInfoDB.minRows;	// 最小値
		int	delta		= BbsInfoDB.delta;		// 増分
		//
		String editor_rows	= strHash(htb, BbsInfoDB.EDITOR);
		int	rows	= Integer.parseInt(editor_rows);
		if(rows >= min + delta){
			editor_rows	= String.valueOf(rows - delta);
			htb.put(BbsInfoDB.EDITOR, editor_rows);
		}
		return;
	}	
	
	/*////////////////////////////////////////////////////////////////////////////
	 * 
	 *    表 示 処 理
	 * 
	 * 　　    表示処理ははコントローラーが呼び出す表示メソッドであり
	 * 　　    直接呼び出すことはできない．（機能しない）
	 * 
	 *////////////////////////////////////////////////////////////////////////////
	 
	public  void	display(boolean editmode){
		if(LOG.fa)	LOG.println("■ BbsMakeThread #display(boolean editmode)");
		
		if(!editmode){
			/* 初期レコードを作成しシステムハッシュに展開する */
			setInitialProperty();
			setEditorRows();
			/*
			 * ファイルリストを表示しない
			 */
			putParameter("_listsw", "OFF");	
			/*
			 * タグマーカーの初期値を設定
			 */
			setTagMark();

		}
		/*
		 * ファイルリスト表示モードを設定
		 */
		String	fileDispMode	=	getParameter( "_listsw");
		if(fileDispMode.equals("ON")){
			putParameter("_listOnSelected", "selected");
			putParameter("_listOffSelected", "");
			
		}else{
			putParameter("_listOnSelected", "");
			putParameter("_listOffSelected", "selected");
		}
				
		/*
		 * タグマーカーの値を設定（ウェブ変数がある）
		 */
		setTagMarkerList();
		
		/*
		 * コントローラによってシステムハッシュ(htb)にはファイルの完全パス名が入って
		 * いるので出力命令は常にこのようになる
		 */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v,htb);
	}
	/**
	 * タグマーカーの初期値を設定
	 */
	void	setTagMark(){
		putParameter("tagmark","^");
	    
	}
	/**
	 * タグマーカーの値を設定（ウェブ変数がある）
	 *
	 */
	void	setTagMarkerList(){
		String tagmark	=	getParameter("tagmark");
		if(isEmpty(tagmark)){
		    putParameter("tagmark","^");
		}
		/*
		 * おなじくselect 指定も作成
		 * select 句の部分はウェブでは %_tag_[タグ文字]%　となっている
		 * 
		 */
		String	tag_sel	=	"_tag_" + tagmark;
		putParameter(tag_sel, "selected");
	}	
	/**
	 * 初期表示のためのレコードを得てシステムハッシュに展開する
	 */
	void	setInitialProperty(){
		if(LOG.fa)	LOG.println("■ BbsMakeThread #setInitialProperty()");
		//		
		htb.put(MESSAGE,"");
		/*
		 * システムハッシュのフォーラムキーを調べて新規が既存かを決定し
		 * レコードを record に取り込む
		 * 新規の場合はテンプレートレコードで、キーはダミーキーを設定しておく
		 */
		threadkey	=	Gear.strHashIncludeNull(htb, BbsThreadDB.THREAD_KEY);
		if(threadkey.equals(DUMMY_KEY)){
			/* 新規作成 */
			record	=	getTemplateRecord(owner_forumkey, threadkey);
			/*　フォーマットスタイルを個人情報DBから読み込んで
			 *　編集内容にアペンドしておく 
			 */		
			//format		=	readFormatStyle();
			//record.put(BbsThreadDB.CONTENT,format);			
		}else{
			record	=	readRecord(threadkey);
			if(LOG.fa) LOG.outHash(record, "★ BbsMakeThread #setInitialProperty  :読み出した forum レコード");		
		}
		/*
		 * レコードをシステムハッシュに展開する
		 */
		toSystemHash(htb, record);
	}
	/**
	 * レコードのテンプレートを作成して返す．
	 * 全ての項目に初期が入っている
	 * @param owner_forumkey
	 * @param threadkey
	 * @return
	 */
	public	 Hashtable	getTemplateRecord(String owner_forumkey, String threadkey){
		if(LOG.fa)	LOG.println("■ BbsThreadRecord #getTemplateRecord() レコードのテンプレートを作成して返す");
		
		Hashtable	tb	=	new Hashtable(20);
		
		tb.put(BbsThreadDB.OWNER_FORUM_KEY	,owner_forumkey);
		tb.put(BbsThreadDB.THREAD_KEY		,threadkey);
		
		tb.put(BbsThreadDB.USERID		,"");
		tb.put(BbsThreadDB.DATE			,BbsUtil.getTodayStringInMilis());	// today
		tb.put(BbsThreadDB.RATING		,"");
		tb.put(BbsThreadDB.SUBJECT		,"");
		tb.put(BbsThreadDB.CONTENT		,"");
		tb.put(BbsThreadDB.ATTACHMENT	,"");
		tb.put(BbsThreadDB.VIEWS		,"");
		return	tb;
	}
	/**
	 * ハッシュに入っているレコードをシステムハッシュにコピーする
	 * @param sys
	 * @param tb
	 * @return
	 */
	public	 Hashtable	toSystemHash(Hashtable sys, Hashtable tb){
		if(LOG.fa)	LOG.println("■ BbsThreadRecord #toSystemHash() レコードをシステムハッシュにコピーする");

		sys.put(BbsThreadDB.OWNER_FORUM_KEY,	Gear.strHash(tb,BbsThreadDB.OWNER_FORUM_KEY));
		sys.put(BbsThreadDB.THREAD_KEY,			Gear.strHash(tb,BbsThreadDB.THREAD_KEY));
		//sys.put(BbsThreadDB.USERID,				Gear.strHash(tb,BbsThreadDB.USERID));	// システムデータなので常にある．コピーしない．
		sys.put(BbsThreadDB.DATE,				Gear.strHash(tb,BbsThreadDB.DATE));
		sys.put(BbsThreadDB.RATING,				Gear.strHash(tb,BbsThreadDB.RATING));
		sys.put(BbsThreadDB.SUBJECT,			Gear.strHash(tb,BbsThreadDB.SUBJECT));
		sys.put(BbsThreadDB.CONTENT,			Gear.strHash(tb,BbsThreadDB.CONTENT));
		sys.put(BbsThreadDB.ATTACHMENT,			Gear.strHash(tb,BbsThreadDB.ATTACHMENT));
		sys.put(BbsThreadDB.VIEWS,				Gear.strHash(tb,BbsThreadDB.VIEWS));
		
		return sys;
	}
	/**
	 * 規定値のフォーマットを読み込む
	 * @return
	 */
	public	String	readFormatStyle(){
		if(LOG.fa)	LOG.println("■ BbsMakeThread #readFormatStyle()");
		//		
		Hashtable	tb	=	getInfoRecord();
		format			=	Gear.strHash(tb, BbsInfoDB.FORMAT);
		return	format;	
		
	}	
	/**
	 * ユーザー情報データベースを読んで、エディタの行数をシステムハッシュにセットする
	 */
	void	setEditorRows(){
		if(LOG.fa)	LOG.println("■ BbsMakeThread #setEditorRows()");
		//		
		Hashtable	tb	=	getInfoRecord();
		htb.put(BbsInfoDB.EDITOR, strHash(tb,BbsInfoDB.EDITOR));
	}
	/**
	 * エディタの行数を更新する
	 */
	void	updateEditorRows(){
		if(LOG.fa)	LOG.println("■ BbsMakeThread #updateEditorRows()");
		//
		BbsInfoDB	bi	=	new BbsInfoDB(userid, szDB, broker);
		bi.updateEditorRow(userid, strHash(htb,BbsInfoDB.EDITOR));
	}
	/**
	 * ユーザー情報を読みハッシュに入れて返す
	 * @return
	 */
	Hashtable	getInfoRecord(){
		if(LOG.fa)	LOG.println("■ BbsMakeThread #getInfoRecord()");
		//		
		BbsInfoDB	bi	=	new BbsInfoDB(userid, szDB, broker);
		Hashtable	tb	=	new Hashtable(20);
		int		cnt	=	bi.readBbsInfo(userid,tb);
		if(cnt==0){
			tb	=	setInitialRecord();
		}
		return	tb;
	}

	Hashtable	setInitialRecord(){
		if(LOG.fa)	LOG.println("■ BbsMakePost #setInitialRecord()");

		/** 初期値をハッシュにセットする */
		Hashtable	tb		=	new Hashtable(20);
		tb.put(BbsInfoDB.USER_ID	,Gear.strHash(htb, UID));
	
		/* ユーザー名は基本情報なので常にシステムハッシュの中にあるハズ */
		String	userName	=	Gear.strHash(htb, UNAME);
		if(Gear.isEmpty(userName)){
			tb.put(BbsInfoDB.NAME	,"");
		}else{
			tb.put(BbsInfoDB.NAME	,userName);
		}
		tb.put(BbsInfoDB.HANDLE		,"");
		tb.put(BbsInfoDB.ICON_FILE	,"");
		tb.put(BbsInfoDB.SIGNATURE	,"");
		tb.put(BbsInfoDB.FORMAT		,"");
		tb.put(BbsInfoDB.EDITOR		,"20");
		
		return	tb;		
		
	}


	////////////////////////////////////////////////////////////////////////////////////////
	//
	// 	ハッシュテーブル(htb)を使って部分的に切り取ったソースデータ（exHtml）
	// 	の内容を置き換えて出力処理する．表などの反復出力に利用するが個々の処理
	// 	内容は、key で特定される．
	//
	////////////////////////////////////////////////////////////////////////////////////////

	public void	write(String key,Vector exHtml,Hashtable htb){
		if(LOG.fa)	LOG.println("■ BbsMakeForum #write()");
		
		if(key.equals("uploadFileList")){
		    uploadFileList(exHtml);
		
		}else if(key.equals("isNoFiles")){
		    isNoFiles(exHtml);
		}
	}
	void	uploadFileList(Vector exHtml){
		/*
		 * ファイルリストを表示しないモードならなにもしない
		 */
		String	fileDispMode	=	getParameter( "_listsw");
		if(fileDispMode.equals("OFF"))	return;
		/*
		 * HTML 生成のための各種テンプレート
		 */	    
	    String	template	=	(new TemplateBox(conf.pmlTemplatePath())).get("attach2");
		String	uploadFile;
		String	fileUrl;
		String	colorClass;	// gray12 , blue12(添付ファイルの時)
		String	attachIcon; // ファイル送信モード（添付か否か）で色の違うアイコン

		/*
		 * アップロードファイルリストを作成しておく
		 * データベースの attachment を読んでファイルリストとその種別を得る
		 * 新規の場合はレコードはないのでリターンする
		 */
		Hashtable	record		=	readRecord(threadkey);
		if(record==null){
		    putParameter(FCOUNT, "0");
			return;				
		}
		//
		String		attachment	=	Gear.strHash(record,BbsThreadDB.ATTACHMENT);
		/*
		 * アップファイルリストのCsvオブジェクトを作成
		 * 添付ファイルは１文字目に"*"が付いている
		 */			
		Hashtable	list	=	new	Hashtable(20);
		Csv			cs		=	new	Csv(attachment);
		int			n		=	cs.size();
		/*
		 * ファイルリストを表示する処理
		 */
		if(n==0){
			putParameter(FCOUNT, "0");
			return;
		}
		putParameter(FCOUNT, String.valueOf(n));	// blockWrite() で使う
		/*
		 * 全てのファイルを表示する
		 */
		Hashtable	tb	=	new	Hashtable(10);
		for(int i=0; i<n; i+=4){
		    /*
			 * １行文の表示（最大４つのファイルを表示）
			 */
			for(int k=0; k<4; k++){
				int	pos	=	i + k;
				uploadFile	=	getFilename(cs, pos);
				fileUrl		=	getFileUrl(cs, pos);
				colorClass	=	getColorClass(cs, pos);
				attachIcon	=	getAttachIcon(cs, pos);

				String	rep	=	"&nbsp;";
				if(!Gear.isEmpty(uploadFile)){
					tb.put("_uploadFile", uploadFile);
					tb.put("_fileUrl"	, fileUrl);
					tb.put("_colorClass", colorClass);
					tb.put("_attachIcon", attachIcon);

					rep	=	Gear.replace(template, tb);
				}
				String	repkey	=	"_fileDiscriptor-" + String.valueOf(k+1);
				htb.put(repkey, rep);					
			}
			printVector(exHtml);
		}
	    
	}
	/**
	 * 部分的に切り取ったソースデータ（exHtml）を出力するか否か、ハッシュテーブル
	 *  (htb)を使って判断し、出力する場合には、htb を使ってexHtml の内容を書き換えて
	 *  出力する．個々の処理内容は、key で特定される．
	 */
	void	isNoFiles(Vector exHtml){
		String	fileDispMode	=	getParameter( "_listsw");
		if(fileDispMode.equals("ON")){
			/*
			 * ファイルリストを表示するモードならリスト表示していないときのみブランクを表示する 
			 */
			int	n	= Integer.parseInt( getParameter(FCOUNT) );// 既に表示した行があるか
			if(n > 0)	return;
			//
			printVector(exHtml);
		}else{
			/*
			 * ファイルリスト非表示モードなら常にブランクを表示する
			 */
			printVector(exHtml);
		}
		return;
	}	
	
	/**
	 * ファイルのWebにおける完全URLを返す
	 * @param cs
	 * @param pos
	 * @return		完全URL
	 */
	String	getFileUrl(Csv cs, int pos){
		if(LOG.fa)	LOG.println("■ BbsMakeThread #getFileUrl()");

		String	fname	=	getFilename(cs, pos);
		if(Gear.isEmpty(fname))	return	"";
		return	conf.threadUrl(ownerkey, forumkey, threadkey) + "/" + fname;
	}
	/**
	 * ファイルの表示色を返す
	 * 添付ファイルとその他で色を変える
	 * @param cs
	 * @param pos
	 * @return		表示色のCSSクラス名
	 */
	String	getColorClass(Csv cs, int pos){
		if(LOG.fa)	LOG.println("■ BbsMakeThread #getColorClass()");

		if(isAttachment(cs, pos)){
			return	"#3366CC";
		}
		return "#333333";
	}
	/**
	 * アイコンファイル名を返す
	 * 
	 * @param cs
	 * @param pos
	 * @return		表示色のCSSクラス名
	 */
	String	getAttachIcon(Csv cs, int pos){
		if(LOG.fa)	LOG.println("■ BbsMakeForum #getAttachIcon()");

		if(isAttachment(cs, pos)){
			return	"attach-2.gif";
		}
		return "attach-1.gif";
	}	
}
