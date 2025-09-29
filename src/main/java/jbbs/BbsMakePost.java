/*
 * 作成日: 2004/08/12
 *
 * 
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
 * @author kawaba
 *
  	#
	# #######
	#   post
	# #######
	#
	<program $jbbs.BbsMakePost>
		<dispatch  html=mkPost.html      number=3320       class=jbbs.BbsMakePost />
		<variable>
		 <receive  NUMBER  STAMP  GROUP BBS_OWNER_KEY UID MAIL UNAME  HOMEURL DIVISION 
		           forumkey threadkey postkey quotedkey  pastLink  mail_to mail_from mail_title  mail_body  />
		 <accept   SUBMIT  UPLODE deleteFile chgFile />
         <keep      editor  date   attachment link dispname  writeFlag />
         
         <form      content listsw tagmark />
         
        </variable>
	</program>

 *  1. accept 変数
 *	
 *  2. keep 変数
 * 		editor			エディター領域の表示行数
 * 		date		
 * 		attachment		アップロードファイルリスト
 * 		link
 * 		dispname
 * 		writeFlag	
 * 
 *  3. form 変数
 * 		content			記述したテキスト
 * 		listsw			アップロードファイル一覧を表示するかどうかのスイッチ ("ON" / "OFF")
 *     	tagmark			現在のタグマーカー文字．リストボックス
 *
 */
public class BbsMakePost  extends SuperPlayer implements BbsVar{
	BbsConf				conf;
	//
	BbsPostDB			db;
	String				ownerkey;	// フォーラムの所有者
	String				szDB;
	DbConnectionBroker	broker;
	//
	BbsKeys				keyGen;

	/** スレッド処理から受け取るキー */
	String				forumkey;
	String				threadkey;
	String				postkey;
	String				userid;

	/** ここで作成するキー */
	String				bbskey;			// ownerkey_forumkey_threakey
	String				owner_forumkey;	// ownerkey_forumkey
	
	/* 
	 * 引用情報
	 */
	/** 引用先のpostkey */
	String				quotedkey;
	/** 引用文字列 */
	String				quotedStr;
	
	/**
	 * リンクキー
	 */
	String				pastLink;
	
	/** 作業用変数 */
	String				format;
	Hashtable			userinfo;		// ユーザーの情報レコード
	Hashtable			articleRecord;	// 記事レコード
	
	
	
	public BbsMakePost(){
		super();
	}
	public	 void		initialize(PrintWriter out,Hashtable htb, Param parameter){
		super.setInit(out, htb, parameter);
		//
		if(LOG.fa)	LOG.println("■ BbsMakePost #initialize()");
		/*
		 * コンフィギュレーション情報を作成
		 */
		conf			=	createBbsConf(para.get("BBS_CONF_PATH"),htb);		
		/*
		 * 基本情報を取り出す
		 */		
		szDB			=	strHash(htb, GROUP);
		broker			=	(DbConnectionBroker)htb.get(BROKER);
		/*
		 * キーを取り出して owner_forumkey, bbskey を作成する
		 * システムハッシュにも設定しておく
		 */
		ownerkey		=	strHash(htb,BBS_OWNER_KEY);
		forumkey		=	strHash(htb,BbsForumDB.FORUM_KEY);
		threadkey		=	strHash(htb,BbsThreadDB.THREAD_KEY);
		
		owner_forumkey	=	BbsKeys.ownerForumKey(ownerkey, forumkey);
		bbskey			=	BbsKeys.bbsKey(ownerkey, forumkey, threadkey);

		htb.put(BbsThreadDB.OWNER_FORUM_KEY ,owner_forumkey);
		htb.put(BbsPostDB.BBS_KEY			,bbskey);

		/*
		 * ユーザー情報
		 */		
		userid			=	Gear.strHash(htb,UID);	// アクセスしている人のID（常にある）
		userinfo		=	setUserInfoRecord(userid);
		/*
		 * キー生成器
		 */
		keyGen			=	new BbsKeys(ownerkey, szDB, broker);
		/*
		 * データベースオブジェクトを作成しておく
		 */		
		db				=	new BbsPostDB(szDB, broker);
		/*
		 * postkey が設定されていない場合は新規作成である．
		 * BbsDUMMY_KEY　を仮のキーとして設定しておく
		 */
		postkey			=	strHash(htb,BbsPostDB.POST_KEY);
		if(isEmpty(postkey)){
			postkey	=	DUMMY_KEY;
			htb.put(BbsPostDB.POST_KEY, DUMMY_KEY);
		}
		/*
		 * 引用情報（quotedStr はWeb変数に保存していないので初回しかない）
		 * どのpostも引用しない時は "" となっている
		 */
		quotedkey	=	Gear.strHash(htb, QUOTED_KEY);	// 引用先 postkey
		quotedStr	=	Gear.strHash(htb, QUOTED);
		pastLink	=	Gear.strHash(htb, PAST_LINK);
		
		
		
		if(LOG.fa) LOG.outHash( htb,"□ BbsMakePost #initialize()");
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
		if(LOG.fa)	LOG.println("■ BbsMakePost #createBbsConf()");
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

		if(LOG.fa) LOG.outHash(htb,"■ BbsMakePost #dispatch() key:" + cmd);
		
		if(cmd.equals("VIEW")){
			/*
			 * システムハッシュには対象とするポストキー(postkey)が入っているので
			 * これを使ってレコードを読み出して表示する．具体的な処理は display()． 
			 */
			disp_mode	=	DISP_NEW;
			ret			=	DISPATCH_DEFAULT;	// WEB表示
		
		}else if(cmd.equals("WRITE")){
			/* 失敗するかもしれない */
			writeRecord();
			putParameter("writeFlag","ON");
			
			disp_mode	=	DISP_EDIT;
			
		}else if(cmd.equals("UPLOAD")){
			/* ファイル受信 */
			upload();
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
			 * 実際にデータを書いていれば対象へメールで知らせる
			 */ 
			String	writeFlag	=	getParameter("writeFlag");
		    if(writeFlag.equals("ON")){
			    makeNotice();
			    
			}
		    ret	=	DISPATCH_RETURN;

		}else if(cmd.equals("PREVIEW")){
			/* 
			 * プレビューを表示する
			 * 表示に必要なデータはシステムハッシュに入っている
			 */
			disp_mode	=	DISP_NEW;	// 編集モード
			ret			=	PREV_POST;
			
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
	 * 投稿した相手にメールで知らせる
	 * スレッド化した
	 *
	 */
	void	makeNotice(){
	    
	    String	to		=	getParameter("mail_to");
	    String	from	=	getParameter("mail_from");
	    String	title	=	getParameter("mail_title");
	    String	body	=	getParameter("mail_body");
	    
	    SendMail	sm	=	new	SendMail(to, from, title, body, para);
	    sm.start();
	    //Gear.send_To_email(to,from,title,body,para);
	    
	    
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
	public	void	writeRecord(){
		if(LOG.fa)	LOG.println("■ BbsMakePost #writeRecord()");
		/*
		 * 新規モード、更新モードでレコードを書き込む
		 */
		if(postkey.equals(DUMMY_KEY)){
			insertRecord();
		}else{
			updateRecord();
		}
	}
	/**
	 * 新規モードでレコードを作成して書き込む
	 *
	 */
	public	void	insertRecord(){
		if(LOG.fa)	LOG.println("■ BbsMakePost #writeRecord()");
		/* 
		 * 新規レコードのデータをシステムハッシュにセットする
		 */
		postkey		=	keyGen.getPostKey(bbskey);
		htb.put(BbsPostDB.POST_KEY		, postkey);
		htb.put(BbsPostDB.DATE			, BbsUtil.getTodayStringInMilis());
		/*
		 * 最初にレコード書かないとファイルアップロードできないので新規では常に ""
		 */
		htb.put(BbsPostDB.ATTACHMENT	, "");
		/*
		 * リンクを設定する
		 */
		String	pastLink	=	Gear.strHash(htb,PAST_LINK);
		String	link		=	postkey;
		if(!Gear.isEmpty(pastLink)){
			link	=	Gear.strHash(htb,PAST_LINK) + "_" + postkey;
		}		 
		htb.put(BbsPostDB.LINK, link);

		/*
		 * 新規モードの時にはエディタ行数も更新する
		 */
		updateEditorRows();
		/*
		 * レコードをシステムハッシュから作成してデータベースに書く
		 */
		Hashtable	rec	=	getRecordByHash(htb);
		if(LOG.fa)	LOG.outHash(rec,"□ 作成した insert レコードの内容");
		db.insertBbsPost(bbskey, postkey,rec);
	}
	/**
	 * システムハッシュの内容からレコードを作成する
	 * @param htb
	 * @return
	 */
	public Hashtable getRecordByHash(Hashtable htb){
		if(LOG.fa)	LOG.println("■ BbsMakePost #getRecord() レコードのテンプレートを作成して返す");
		
		/*
		 * 現在のレコードを読み込んで初期レコードとする
		 * 作成者のキーを得る必要がある
		 */
		Hashtable	dest	=	new Hashtable();
		/*
		 * キー
		 */
		dest.put(BbsPostDB.BBS_KEY,		Gear.strHash(htb,BbsPostDB.BBS_KEY));
		dest.put(BbsPostDB.POST_KEY,	Gear.strHash(htb,BbsPostDB.POST_KEY));
		/*
		 * 書き込みユーザーのＩＤ
		 */		
		dest.put(BbsPostDB.USERID,		Gear.strHash(htb,UID));
		/*
		 * その他のデータ
		 */
		dest.put(BbsPostDB.DATE,		Gear.strHash(htb,BbsPostDB.DATE));
		// 入力がなければ "0.00" 
		dest.put(BbsPostDB.RATING,		Gear.strHash(htb,BbsPostDB.RATING));
		
		dest.put(BbsPostDB.SUBJECT,		Gear.strHash(htb,BbsPostDB.SUBJECT));
		
		String	ct	=	BbsUtil.trimFormat(Gear.strHash(htb,BbsPostDB.CONTENT));
		putParameter(BbsPostDB.CONTENT, ct);
		dest.put(BbsPostDB.CONTENT,		ct);
		
		dest.put(BbsPostDB.ATTACHMENT,	Gear.strHash(htb,BbsPostDB.ATTACHMENT));
		dest.put(BbsPostDB.DISP_NAME,	Gear.strHash(htb,BbsPostDB.DISP_NAME));
		dest.put(BbsPostDB.LINK,		Gear.strHash(htb,BbsPostDB.LINK));
		
		return dest;
	}
	/**
	 * 更新モードでレコードを作成して書き込む
	 * 
	 */
	public	void	updateRecord(){
		if(LOG.fa)	LOG.println("■ BbsMakePost #updateRecord()");
		/*
		 * レコードをシステムハッシュから作成してデータベースに書く
		 */
		Hashtable	rec	=	getRecord(htb);
		if(LOG.fa)	LOG.outHash(rec,"□ 作成した update レコードの内容");
		db.updateBbsPost(bbskey, postkey,rec);
	}
	/**
	 * 既存レコードを雛型にしてシステムハッシュからレコードを作成する
	 * @param htb
	 * @return
	 */

	public Hashtable getRecord(Hashtable htb){
		if(LOG.fa)	LOG.println("■ BbsMakePost #getRecord() レコードのテンプレートを作成して返す");
		
		/*
		 * 現在のレコードを読み込んで初期レコードとする
		 * 作成者のキーを得る必要がある
		 */
		Hashtable	dest	=	readRecord(postkey);
		/*
		 * キーは変更ない
		 */
		//dest.put(BbsPostDB.BBS_KEY,	Gear.strHash(htb,BbsPostDB.BBS_KEY));
		//dest.put(BbsPostDB.POST_KEY,	Gear.strHash(htb,BbsPostDB.POST_KEY));
		/*
		 * UIDがpostする者のIDだが、教師が修正書き込みをしてもＩＤが変わらないように
		 * BbsPostDB.USERID は設定しない
		 */		
		//dest.put(BbsPostDB.USERID,		Gear.strHash(htb,BbsPostDB.USERID));
		/*
		 * その他のデータ
		 * dispatch.xml で以下のようにWebに保存している
		 * <work    editor date attachment link dispname/>
		 */
		
		// 日付は更新
		dest.put(BbsPostDB.DATE,		BbsUtil.getTodayStringInMilis());	// 今日の日付
		//
		dest.put(BbsPostDB.RATING,		Gear.strHash(htb,BbsPostDB.RATING));
		dest.put(BbsPostDB.SUBJECT,		Gear.strHash(htb,BbsPostDB.SUBJECT));
		
		String	ct	=	BbsUtil.trimFormat(Gear.strHash(htb,BbsPostDB.CONTENT));
		putParameter(BbsPostDB.CONTENT, ct);
		dest.put(BbsPostDB.CONTENT,		ct);
		
		dest.put(BbsPostDB.ATTACHMENT,	Gear.strHash(htb,BbsPostDB.ATTACHMENT));
		dest.put(BbsPostDB.DISP_NAME,	Gear.strHash(htb,BbsPostDB.DISP_NAME));
		/*
		 * link は変更がないので書き変えない
		 */
		//dest.put(BbsPostDB.LINK,		Gear.strHash(htb,BbsPostDB.LINK));
		
		return dest;
	}
	/**
	 * あるスレッドの全ての記事を読み出す
	 */
	public	Vector	readRecords(){
		if(LOG.fa)	LOG.println("■ BbsMakePost #readRecord() あるオーナーの全てのフォーラムレコードを読み出す");
			
		Vector	vrec	=	new	Vector(30);
		int	n		=	db.readBbsPosts(bbskey, vrec);
		if(n==0)	return	null;
		return	vrec;
	}
	/** 
	 * bbskey と postkey を指定してひとつのレコードを読み結果をハッシュにいれて返す
	 * レコードがなかった場合戻り値は null
	 */
	public	Hashtable	readRecord( String postkey){
		if(LOG.fa)	LOG.println("■ BbsMakePost #readRecord() ひとつのレコードを読み結果をハッシュにいれて返す");
			
		Hashtable	tb	=	new	Hashtable(20);
		int		n	=	db.readBbsPost(bbskey, postkey, tb);
		if(n==0)	return	null;
		return	tb;
	}
	/**
	 * アップロードされたファイルを受け取る
	 */
	void	upload(){
		if(LOG.fa)	LOG.println("■ BbsMakePost #upload()");
		/*
		 * ファイル受信にはディレクトリを特定するため threadkey が必要なので
		 * レコードの書き込みを行う．この中で threadkey が確定する
		 */
		if(postkey.equals(DUMMY_KEY)){
			writeRecord();
		}
		/*
		 * 添付ファイルかどうか
		 */
		String 	fname	=	getFile( conf.postDir(ownerkey, forumkey, threadkey, postkey) );	// 移動
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
		        String	zipPath		=	conf.postDir(ownerkey, forumkey, threadkey, postkey) + FS +fname;
		        String	unzipDir	=	conf.postDir(ownerkey, forumkey, threadkey, postkey);
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
		
	}
	/**
	 * システムハッシュのアップロードファイルリストを更新し、さらにデータベースも更新する
	 * @param attachmentStr
	 */
	void	writeAttachment(String attachmentStr){
		if(LOG.fa)	LOG.println("■ BbsMakePost #writeAttachment()");
		
		htb.put(BbsPostDB.ATTACHMENT, attachmentStr);
		updateRecord();
	}

	/**
	 * アップロードされたファイルをプロファイルディレクトリへ移す
	 * 複数ファイルの同時アップロードに対応している
	 * アップロード先のファイル記述子を返す
	 */
	String	getFile(String toDir){
		if(LOG.fa)	LOG.println("■ BbsMakePost #getFile()");
		/*
		 * 戻り値に使うのでファイル名を取得しておく
		 */
		String		fromDir	=	strHash(htb,UPLOAD_DIR_NAME);
		String	[]	files	=	(new File(fromDir)).list();
		String		fname	=	files[0];
		/*
		 *   /home/pc/temp/[ランダムに作成したディレクトリ名]   ⇒　/home/bbs/[owner]/[forum]/[thread]/[Post]/
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
		if(LOG.fa)	LOG.println("■ BbsMakePost #getAttachmentHash()");
		LOG.println("□□4□□ postkey=" + postkey);

		/*
		 * データベースの attachment を読んでファイルリストとその種別を得る
		 */
		Hashtable	record		=	readRecord(postkey);
		String		attachment	=	Gear.strHash(record,BbsPostDB.ATTACHMENT);
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
		if(LOG.fa)	LOG.println("■ BbsMakePost #getFilename()");

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
		if(LOG.fa)	LOG.println("■ BbsMakePost #isAttachment()");

		if(pos >= cs.size())	return	false;
		
		String	fname	=	cs.get(pos);
		if(fname.charAt(0)=='*'){
			return	true;
		}
		return	false;			
	}
	/**
	 * すでにアップロードされているファイルを削除する
	 * アップロードでポストが確定するので、この処理では必ず threadkey がある
	 * 添付ファイル情報が更新されるので、レコードの書き込みも行う
	 */	
	void	delete(){
		if(LOG.fa)	LOG.println("■ BbsMakePost #file()");
		String	filename	=	Gear.strHash(htb,DELFILE);
		String	entry		=	filename.replace('/', File.separatorChar);
		delete(entry);		/*
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
		if(LOG.fa)	LOG.println("■ BbsMakePost #delete()");
		File	fp		=	new File(conf.postDir(ownerkey, forumkey,  threadkey, postkey) + File.separator + fname);
		fp.delete();
	}

	/**
	 * エディタの現在の設定行数を得る
	 * @return
	 */
	int getEditorRows(){
		if(LOG.fa)	LOG.println("■ BbsMakePost #getEditorRows()");
		/*
		 * ポストの所有者は ownerkey（フォーラムの所有者）ではなく userid で特定される
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
		if(LOG.fa) LOG.println("■ BbsMakePost #puls() : 行数を増やす の先頭です");
		//
		
		int	max			= BbsInfoDB.maxRows;	// 最大値
		int	min			= BbsInfoDB.minRows;	// 最小値
		int	delta		= BbsInfoDB.delta;		// 増分
		//
		String 	editor_rows	= Gear.strHash(htb,BbsInfoDB.EDITOR);
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
		if(LOG.fa) LOG.println("■ BbsMakePost #minus() : 行数を減らす の先頭です");
		//
		int	max			= BbsInfoDB.maxRows;	// 最大値
		int	min			= BbsInfoDB.minRows;	// 最小値
		int	delta		= BbsInfoDB.delta;		// 増分
		//
		String editor_rows	= Gear.strHash(htb, BbsInfoDB.EDITOR);
		int	rows		= Integer.parseInt(editor_rows);
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
		if(LOG.fa)	LOG.println("■ BbsMakePost #display(boolean editmode)");
		
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
			/*
			 * 書き込んだかを覚えておくフラグをクリア
			 */
			putParameter("writeFlag","Off");

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
		
		/* 支持度の値を画面のチェックボックスに反映する */
		setRatingSelect(Gear.strHash(htb, BbsPostDB.RATING));
		
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
	 * 支持度の値を画面のリストボックスに反映する
	 * @param rating
	 */
	public	void	setRatingSelect(String rating){
		/*
		 * 支持度のチェック状態を設定
		 */
		int	r	=	(int)(Double.parseDouble(rating));
		if(LOG.fa) LOG.println("□ rating-select=" + r);
		setRatingSelect(htb, r);		
	}
	/**
	 * 支持度の値を指定して、選択状態を設定する．
	 * 
	 * _rsel0 ～ _rsel10 までが webのドロップダウンリストの各項目に対する
	 * "selected" または "" に対応する
	 * 
	 * @param r
	 */
	void setRatingSelect(Hashtable tb, int r){
		
		for(int i=0; i<SEL_RATING_MAX; i++){
			if(i==r){
				tb.put("_rsel" + String.valueOf(i), "selected");
			}else{
				tb.put("_rsel" + String.valueOf(i), "");
			}
		}
	}	
	/**
	 * 初期表示のためのレコードを得てシステムハッシュに展開する
	 */
	void	setInitialProperty(){
		if(LOG.fa)	LOG.println("■ BbsMakePost #setInitialProperty()");
		//		
		htb.put(MESSAGE,"");
		/*
		 * システムハッシュのポストキーを調べて新規が既存かを決定し
		 * レコードを record に取り込む
		 * 新規の場合はテンプレートレコードで、キーはダミーキーを設定しておく
		 */
		postkey	=	Gear.strHashIncludeNull(htb, BbsPostDB.POST_KEY);
		if(postkey.equals(DUMMY_KEY)){
			/* 新規作成 */
			articleRecord	=	getTemplateRecord(postkey, quotedStr);
			
		}else{
			articleRecord	=	readRecord(postkey);
			//if(DBG.fa) DBG.outHash(articleRecord, "★ BbsMakePost #setInitialProperty  :読み出したPostレコード");		
		}
		/*
		 * レコードをシステムハッシュに展開する
		 */
		copyTo(htb, articleRecord);
	}
	/**
	 * 初期レコードを作成して返す．
	 * @param bbskey
	 * @param threadkey
	 * @return
	 */
	public	Hashtable	getTemplateRecord(String postkey, String quotedStr){
		if(LOG.fa)	LOG.println("■ BbsMakePost #getTemplateRecord() レコードのテンプレートを作成して返す");
		
		Hashtable	tb	=	new Hashtable(20);
		
		tb.put(BbsPostDB.BBS_KEY	,bbskey);
		tb.put(BbsPostDB.POST_KEY	,postkey);
		
		tb.put(BbsPostDB.USERID		,UID);
		tb.put(BbsPostDB.DATE		,BbsUtil.getTodayStringInMilis());	// today
		
		tb.put(BbsPostDB.RATING		,"0.00");	// 0.00 ～ 10.00
		
		tb.put(BbsPostDB.SUBJECT	,"");
		tb.put(BbsPostDB.CONTENT	,quotedStr);
		tb.put(BbsPostDB.ATTACHMENT	,"");
		tb.put(BbsPostDB.DISP_NAME	,"");	// 使ってない
		tb.put(BbsPostDB.LINK		,"");
		
		return	tb;
	}

	/**
	 * ハッシュ間のレコードデータコピー
	 * システムハッシュにレコードを移すのに利用
	 * @param sys
	 * @param tb
	 * @return
	 */
	public	Hashtable	copyTo(Hashtable dest, Hashtable src){
		if(LOG.fa)	LOG.println("■ BbsMakePost #toSystemHash() レコードをシステムハッシュにコピーする");

		dest.put(BbsPostDB.BBS_KEY,		Gear.strHash(src,BbsPostDB.BBS_KEY));
		dest.put(BbsPostDB.POST_KEY,	Gear.strHash(src,BbsPostDB.POST_KEY));
		dest.put(BbsPostDB.USERID,		Gear.strHash(src,BbsPostDB.USERID)); //　"_post_ownerid"
		dest.put(BbsPostDB.DATE,		Gear.strHash(src,BbsPostDB.DATE));
		dest.put(BbsPostDB.RATING,		Gear.strHash(src,BbsPostDB.RATING));
		dest.put(BbsPostDB.SUBJECT,		Gear.strHash(src,BbsPostDB.SUBJECT));
		dest.put(BbsPostDB.CONTENT,		Gear.strHash(src,BbsPostDB.CONTENT));
		dest.put(BbsPostDB.ATTACHMENT,	Gear.strHash(src,BbsPostDB.ATTACHMENT));
		dest.put(BbsPostDB.DISP_NAME,	Gear.strHash(src,BbsPostDB.DISP_NAME));
		dest.put(BbsPostDB.LINK,		Gear.strHash(src,BbsPostDB.LINK));

		/* rating の値を選択状態に反映する */
		//setRatingSelect(Gear.strHash(src,BbsPostDB.RATING));
		
		return dest;
	}
	
	/**
	 * 規定値のフォーマットを読み込む
	 * @return
	 */
	public	String	readFormatStyle(){
		if(LOG.fa)	LOG.println("■ BbsMakePost #readFormatStyle()");
		//		
		Hashtable	tb	=	getInfoRecord();
		format			=	Gear.strHash(tb, BbsInfoDB.FORMAT);
		return	format;	
		
	}	
	/**
	 * ユーザー情報データベースを読んで、エディタの行数をシステムハッシュにセットする
	 */
	void	setEditorRows(){
		if(LOG.fa)	LOG.println("■ BbsMakePost #setEditorRows()");
		//		
		Hashtable	tb	=	getInfoRecord();
		htb.put(BbsInfoDB.EDITOR, strHash(tb,BbsInfoDB.EDITOR));
	}
	/**
	 * エディタの行数を更新する
	 */
	void	updateEditorRows(){
		if(LOG.fa)	LOG.println("■ BbsMakePost #updateEditorRows()");
		//
		BbsInfoDB	bi	=	new BbsInfoDB(userid, szDB, broker);
		bi.updateEditorRow(userid, strHash(htb,BbsInfoDB.EDITOR));
	}
	/**
	 * ユーザー情報を読みハッシュに入れて返す
	 * @return
	 */
	Hashtable	getInfoRecord(){
		if(LOG.fa)	LOG.println("■ BbsMakePost #getInfoRecord()");
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
		Hashtable	record		=	readRecord(postkey);
		if(record==null){
			htb.put(FCOUNT, "0");
			return;				
		}
		//
		String		attachment	=	Gear.strHash(record,BbsPostDB.ATTACHMENT);
		/*
		 * アップファイルリストのCsvオブジェクトを作成
		 * 添付ファイルは１文字目に"*"が付いている
		 */			
		Hashtable	list	=	new	Hashtable(20);
		Csv			cs		=	new	Csv(attachment);
		int		n		=	cs.size();
		/*
		 * ファイルリストを表示する処理
		 */
		if(n==0){
			htb.put(FCOUNT, "0");
			return;
		}
		htb.put(FCOUNT, String.valueOf(n));	// blockWrite() で使う
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
		if(LOG.fa)	LOG.println("■ BbsMakePost #getFileUrl()");

		String	fname	=	getFilename(cs, pos);
		if(Gear.isEmpty(fname))	return	"";
		return	conf.postUrl(ownerkey, forumkey, threadkey, postkey) + "/" + fname;
	}
	/**
	 * ファイルの表示色を返す
	 * 添付ファイルとその他で色を変える
	 * @param cs
	 * @param pos
	 * @return		表示色のCSSクラス名
	 */
	String	getColorClass(Csv cs, int pos){
		if(LOG.fa)	LOG.println("■ BbsMakePost #getColorClass()");

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
	/**
	 * 部分的に切り取ったソースデータ（exHtml）を出力するか否か、ハッシュテーブル
	 *  (htb)を使って判断し、出力する場合には、htb を使ってexHtml の内容を書き換えて
	 *  出力する．個々の処理内容は、key で特定される．
	 */
	public void	blockWrite(String key,Vector exHtml,Hashtable htb){
		if(LOG.fa)	LOG.println("■ BbsMakePost #blockWrite()");
		
		if(key.equals("isNoFiles")){
			//
			int	n	= Integer.parseInt( Gear.strHash(htb,"_FileCounts") );// 既に表示した行があるか
			if(n > 0)	return;
			//
			printVector(exHtml,htb);
			return;
		}		
	}

	
}
