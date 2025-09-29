/*
 * 
 */
package jbbs;
import tktools.*;

import java.io.*;
import java.util.*;

import database.*;

import framework.LOG;
import framework.Param;
import framework.SuperPlayer;

/**
 * 
	# ######################################
	# forum
	#       (note) TUID == ownerkey
	#       RELATION == year+season_TUID_apleckey
	#
	# ###################################### 
* 
	<program $jbbs.BbsMakeForum>
		<dispatch  html=mkForum.html     number=3120       class=jbbs.BbsMakeForum />
		<variable>
		  <receive  NUMBER   STAMP  GROUP  BBS_OWNER_KEY UID MAIL UNAME HOMEURL BBS_RELATION DIVISION forumkey />
		  <accept   SUBMIT UPLODE deleteFile chgFile/>
		  <keep      editor date   attachment relation alive  hdflag  gpflag rtflag />
		  
		  <form      subject content listsw tagmark />
		  
		</variable>
	</program>
 * 
 * 		forumkey が設定されていない場合は新規作成である．
 * 		（BbsDUMMY_KEY　が仮のキーとして設定される）
 * 
 *  1. accept 変数
 *	
 *  2. keep 変数
 * 		editor		エディター領域の表示行数
 * 		date		作成日付のlong値の文字列表現
 * 		attachment	アップロードファイルリスト
 * 		relation 	掲示板表示・非表示識別コード
 * 		alive  		編集可かどうかをあらわす値
 * 		hdflag  	ハンドルを使うかどうかを表す値
 * 		gpflag 		グループ学習フラグ（現在は未使用）
 * 		rtflag 		支持度が有効かどうか
 * 
 *  3. form 変数
 * 		content			記述したテキスト
 * 		listsw			アップロードファイル一覧を表示するかどうかのスイッチ ("ON" / "OFF")
 *     	tagmark			現在のタグマーカー文字．リストボックス * 
 */
public class BbsMakeForum  extends SuperPlayer implements BbsVar{

	BbsConf				conf;
	//
	BbsForumDB			db;
	String				ownerkey;
	String				relation;
	
	String				szDB;
	DbConnectionBroker	broker;
	//
	BbsKeys				bbskeys;
	Hashtable			record;
	String				forumkey;
	//
	String				format;
	String				userid;		// このフォーラムを作成しようとしている人のID
	Hashtable			userinfo;	// その情報レコード
	
	String				subject;
	String				content;
	String				attachment;
	
	public	BbsMakeForum(){
		super();
	}
	
	public	 void		initialize(PrintWriter out,Hashtable htb, Param parameter){
		super.setInit(out, htb, parameter);
		//
		if(LOG.fa)	LOG.println("■ BbsMakeForum #initialize()");
		conf			=	createBbsConf(para.get(BBS_CONF_PATH),htb);		
		//
		ownerkey		=	strHash(htb,BBS_OWNER_KEY);
		relation		=	strHash(htb,BBS_RELATION);
		/*
		 * forumkey が設定されていない場合は新規作成である．
		 * BbsDUMMY_KEY　を仮のキーとして設定しておく
		 */
		forumkey		=	strHash(htb,BbsForumDB.FORUM_KEY);
		if(isEmpty(forumkey)){
			forumkey	=	DUMMY_KEY;
			htb.put(BbsForumDB.FORUM_KEY, DUMMY_KEY);
		}
		szDB			=	strHash(htb, GROUP);
		broker			=	(DbConnectionBroker)htb.get(BROKER);
		db				=	new BbsForumDB(szDB, broker);		
		bbskeys			=	new BbsKeys(ownerkey, szDB, broker);
		
		userid			=	Gear.strHash(htb, UID);
		
		subject			=	getParameter("subject");
		content			=	getParameter("content");
		attachment		=	getParameter("attachment");
		  
		
	}
	/**
	 * jbbsライブラリのためのシステムコンフィギュレーションオブジェクトを作成する
	 */
	public BbsConf	createBbsConf(String BbsConfPath, Hashtable ht){
		if(LOG.fa)	LOG.println("■ BbsMakeForum #createBbsConf()");
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

		if(LOG.fa) LOG.outHash(htb,"■ BbsMakeForum #dispatch() key:" + cmd);
		
		if(cmd.equals("VIEW")){
			/*
			 * 別ウィンドウとして呼び出される時はVIEWが指定される
			 * システムハッシュには対象とするフォーラムキー(forumkey)が入っているので
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

		}else if(cmd.equals("FILEMODE")){
		    /*
		     * 添付かアップロードか入れ替える
		     */
		    filemode();
		    //delete();
			disp_mode	=	DISP_EDIT;
		    
		    
		}else if(cmd.equals("FILE_EDIT")){
		    /* ファイル削除 */
		    delete();
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
			
		}else if(cmd.equals("PREVIEW")){
			
			// ハンドルフラグ
			if(Gear.strHashSP(htb,"_handle").equals("1")){
				htb.put(BbsForumDB.HANDLE_FLAG, BbsForumDB.BY_HANDLE);
			}else{
				htb.put(BbsForumDB.HANDLE_FLAG, BbsForumDB.BY_UID);
			}
			putParameter(BbsForumDB.SUBJECT, subject);
			putParameter(BbsForumDB.CONTENT, content);
			putParameter(BbsForumDB.ATTACHMENT, attachment);
			
			ret			=	PREV_FORUM;
			disp_mode	=	DISP_EDIT;

		}else if(cmd.equals("RETURN")){
			/* 
			 * 呼び出し元プログラムへ戻る
			 */ 
			ret	=	DISPATCH_RETURN;

		}else if(cmd.equals("INFO")){
			/*
			 *　個人情報設定
			 */
			disp_mode	=	DISP_NEW;
			ret			=	DISPATCH_INFO;

		}else if(cmd.equals("EXECUTE")){
			/*
			 *　プログラムの実行
			 */
			disp_mode	=	DISP_NEW;
			ret			=	getParameter(PROGRAM);

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
	 * ファイル表示モードを変更する
	 * @param fswitch		"ON" or "OFF"
	 */
	void	setFlistMode(String sw){
		putParameter("_listsw",sw);
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
		if(LOG.fa)	LOG.println("■ BbsMakeForum #writeRecord()");
		/*
		 * 表題がかいてあるかチェック
		 */
		
		if(!writeOK()){
		    if(LOG.fa) LOG.println("no title");
		    return	false;
		}		
		/*
		 * 新規モード、更新モードでレコードを書き込む
		 * 
		 */
		boolean	result	=	true;
		if(forumkey.equals(DUMMY_KEY)){
			result	=	insertRecord( relation );
		    if(!result){
		        /*
		         * データベース未作成のバグ(ver 1.0.0)対応
		         */
		        createForumTable();
		        result	=	insertRecord( relation );
		    }

		}else{
		    result	=	updateRecord();

		}
		return	result;
	}
	/**
	 * ver 1.0.0 のバグ対応
	 * 
	 * SQL の書き誤りがあり、forum tabaleが未作成になっている
	 * すでにシステムを構築した場合、ここでの処理が必要
	 */
	void	createForumTable(){
	    
	    Database	dbms	=	new	Database(broker);
	    dbms.create_BbsForum(szDB);
	    
	}
	/**
	 * 新規モードでレコードを作成して書き込む
	 *
	 * @param relation	 リレーションキー
	 */
	public	boolean	insertRecord(String	relation){
		if(LOG.fa)	LOG.println("■ BbsMakeForum #writeRecord()");
		/* 
		 * 新規レコードのデータをシステムハッシュにセットする
		 */
		htb.put(BbsForumDB.RELATION, relation);	// リレーションキー
		
		forumkey	=	bbskeys.getForumKey(ownerkey);
		htb.put(BbsForumDB.FORUM_KEY	, forumkey);
		htb.put(BbsForumDB.DATE			, BbsUtil.getTodayStringInMilis());
		/*
		 * 最初にレコード書かないとファイルアップロードできないので新規では常に ""
		 */
		htb.put(BbsPostDB.ATTACHMENT	, "");
		/*
		 * 新規モードの時にはエディタ行数も更新する
		 */
		updateEditorRows();
		/*
		 * チェックボックスの値から各フラグの値をシステムハッシュに設定する
		 */
		setFlags();
		/*
		 * レコードをシステムハッシュから作成してデータベースに書く
		 */
		Hashtable	rec	=	getRecordByHash();
		if(LOG.fa)	LOG.outHash(rec,"□ 作成した insert レコードの内容");
		int	cnt	=	db.insertBbsForum(rec);
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
	 * @param ownerkey
	 * @param forumkey
	 * @return
	 */
	public  Hashtable getRecordByHash(){
		if(LOG.fa) LOG.outHash(htb,"■ BbsForumRecord #createRecord()　開始時のシステムハッシュ");
		/*
		 * ハッシュからレコードを作成する
		 */
		Hashtable	rec	=	new Hashtable();
		
		
		/*
		 * ハッシュテーブルの値からレコードを作成する 
		 */
		//Hashtable	rec	=	getTemplateRecord(ownerkey, forumkey);
		//
		rec.put(BbsForumDB.OWNER_KEY,	Gear.strHash(htb,BBS_OWNER_KEY));
		rec.put(BbsForumDB.FORUM_KEY,	Gear.strHash(htb,BbsForumDB.FORUM_KEY));
		
		rec.put(BbsForumDB.ALIVE_FLAG,	Gear.strHash(htb,BbsForumDB.ALIVE_FLAG,	BbsForumDB.HIDDEN));
		rec.put(BbsForumDB.GROUP_FLAG,	Gear.strHash(htb,BbsForumDB.GROUP_FLAG,	BbsForumDB.NORMAL_BBS));
		rec.put(BbsForumDB.RATING_FLAG,	Gear.strHash(htb,BbsForumDB.RATING_FLAG,BbsForumDB.NOT_RATING_MODE));
		rec.put(BbsForumDB.HANDLE_FLAG,	Gear.strHash(htb,BbsForumDB.HANDLE_FLAG,BbsForumDB.BY_HANDLE));
		//
		rec.put(BbsForumDB.DATE,		Gear.strHash(htb,BbsForumDB.DATE,		BbsUtil.getTodayStringInMilis()));
		rec.put(BbsForumDB.SUBJECT,		Gear.strHash(htb,BbsForumDB.SUBJECT,	""));
		// フォーマット記述を取り除く処理を追加
		
		
		String	ct	=	BbsUtil.trimFormat(Gear.strHash(htb,BbsForumDB.CONTENT,	""));
		putParameter(BbsForumDB.CONTENT, ct);
		rec.put(BbsForumDB.CONTENT,		 ct);
		
		
		rec.put(BbsForumDB.ATTACHMENT,	Gear.strHash(htb,BbsForumDB.ATTACHMENT,	""));
		//
		rec.put(BbsForumDB.RELATION,	Gear.strHash(htb,BbsForumDB.RELATION));
		//
		if(LOG.fa){
			LOG.outHash(rec, "★ BbsFrumRecord #createRecord() で作成したレコード");
		}
		return	rec;
	}
		
	/**
	 * 更新モードでレコードを作成して書き込む
	 * 
	 */
	public	boolean	updateRecord(){
		if(LOG.fa)	LOG.println("■ BbsMakeForum #updateRecord()");
		/*
		 * チェックボックスの値から各フラグの値をシステムハッシュに設定する
		 */
		setFlags();		/*
		 * レコードをDBから読みシステムハッシュのデータで修正してデータベースに書き戻す
		 */
		Hashtable	rec	=	getRecord();
		if(LOG.fa)	LOG.outHash(rec,"□ 作成した update レコードの内容");
		int	cnt	=	db.updateBbsForum(rec);
		/*
		 * 件数０件ならエラー
		 */
		if(cnt==0){
		    return	false;
		}else{
		    return	true;
		}
	}	
	public void setFlags(){
		if(LOG.fa)	LOG.println("■ BbsMakeForum #setFlags()");
		/*
		 * チェックボックスの値から各フラグの値を設定する
		 * ハッシュから取り出した値はnullかもしれないので strHashSP() を使って "" に変えて受け取る
		 */	
		// アクティブ（disp）フラグ	
		if(Gear.strHashSP(htb,"_disp").equals("1")){
			htb.put(BbsForumDB.ALIVE_FLAG, BbsForumDB.ACTIVE);
		}else{
			htb.put(BbsForumDB.ALIVE_FLAG, BbsForumDB.HIDDEN);
		}
		// グループフラグ	
		if(Gear.strHashSP(htb,"_group").equals("1")){
			htb.put(BbsForumDB.GROUP_FLAG, BbsForumDB.GROUP_LEARNING);
		}else{
			htb.put(BbsForumDB.GROUP_FLAG, BbsForumDB.NORMAL_BBS);
		}
		// 支持度フラグ
		if(Gear.strHashSP(htb,"_rating").equals("1")){
			htb.put(BbsForumDB.RATING_FLAG, BbsForumDB.RATING_MODE);
		}else{
			htb.put(BbsForumDB.RATING_FLAG, BbsForumDB.NOT_RATING_MODE);
		}
		// ハンドルフラグ
		if(Gear.strHashSP(htb,"_handle").equals("1")){
			htb.put(BbsForumDB.HANDLE_FLAG, BbsForumDB.BY_HANDLE);
		}else{
			htb.put(BbsForumDB.HANDLE_FLAG, BbsForumDB.BY_UID);
		}		
	}
	/**
	 * ＤＢとシステムハッシュに入っている入力データ（通常はwebからの入力）を元に、レコードを作る
	 * @param ownerkey
	 * @param forumkey
	 * @return
	 */
	public  Hashtable getRecord(){
		if(LOG.fa) LOG.outHash(htb,"■ BbsForumRecord #createRecord()　開始時のシステムハッシュ");
		/*
		 * レコードをＤＢから読み出し、ハッシュの値で必要な部分のみ上書きする 
		 */
		forumkey		=	Gear.strHash(htb,BbsForumDB.FORUM_KEY);
		Hashtable	rec	=	readRecord(forumkey);
		/*
		 * ハッシュテーブルの値で上書き 
		 */
		// キーは変化しないので書き変えない
		//rec.put(BbsForumDB.OWNER_KEY,	Gear.strHash(htb,BBS_OWNER_KEY));
		//rec.put(BbsForumDB.FORUM_KEY,	Gear.strHash(htb,BbsForumDB.FORUM_KEY));
		
		rec.put(BbsForumDB.ALIVE_FLAG,	Gear.strHash(htb,BbsForumDB.ALIVE_FLAG,	BbsForumDB.HIDDEN));
		rec.put(BbsForumDB.GROUP_FLAG,	Gear.strHash(htb,BbsForumDB.GROUP_FLAG,	BbsForumDB.NORMAL_BBS));
		rec.put(BbsForumDB.RATING_FLAG,	Gear.strHash(htb,BbsForumDB.RATING_FLAG,BbsForumDB.NOT_RATING_MODE));
		rec.put(BbsForumDB.HANDLE_FLAG,	Gear.strHash(htb,BbsForumDB.HANDLE_FLAG,BbsForumDB.BY_HANDLE));
		//
		rec.put(BbsForumDB.DATE,		Gear.strHash(htb,BbsForumDB.DATE,		BbsUtil.getTodayStringInMilis()));
		rec.put(BbsForumDB.SUBJECT,		Gear.strHash(htb,BbsForumDB.SUBJECT,	""));
		
		String	ct	=	BbsUtil.trimFormat(Gear.strHash(htb,BbsForumDB.CONTENT,	""));
		rec.put(BbsForumDB.CONTENT,		ct);
		putParameter(BbsForumDB.CONTENT,ct);
		
		rec.put(BbsForumDB.ATTACHMENT,	Gear.strHash(htb,BbsForumDB.ATTACHMENT,	""));
		//
		// relation は変化しないので書き変えない
		//rec.put(BbsForumDB.RELATION,	Gear.strHash(htb,BbsForumDB.RELATION));
		//
		if(LOG.fa){
			LOG.outHash(rec, "★ BbsFrumRecord #createRecord() で作成したレコード");
		}
		return	rec;
	}
	/**
	 * レコードのテンプレートを作成して返す．
	 * 全ての項目に初期が入っている
	 * @param ownerkey
	 * @param forumkey
	 * @return
	 */
	public	 Hashtable	getTemplateRecord(String ownerkey, String forumkey){
		if(LOG.fa)	LOG.println("■ BbsForumRecord #getTemplateRecord() レコードのテンプレートを作成して返す");
		
		Hashtable	tb	=	new Hashtable(20);
		
		tb.put(BbsForumDB.OWNER_KEY, ownerkey);
		tb.put(BbsForumDB.FORUM_KEY, forumkey);
		
		tb.put(BbsForumDB.ALIVE_FLAG, 	BbsForumDB.ACTIVE);
		tb.put(BbsForumDB.GROUP_FLAG, 	BbsForumDB.NORMAL_BBS);
		tb.put(BbsForumDB.RATING_FLAG,	BbsForumDB.RATING_MODE);
		tb.put(BbsForumDB.HANDLE_FLAG,	BbsForumDB.BY_HANDLE);
		
		tb.put(BbsForumDB.DATE, 	BbsUtil.getTodayStringInMilis());	// today
		tb.put(BbsForumDB.SUBJECT, 		"");
		tb.put(BbsForumDB.CONTENT, 		"");
		tb.put(BbsForumDB.ATTACHMENT, 	"");
		return	tb;
	}
	/**
	 * あるオーナーの全てのフォーラムレコードを読み出す
	 * 
	 * @return		レコードを要素にもつベクター．レコードがない場合はnullを返す
	 */
	public	 Vector	readRecords(){
		if(LOG.fa)	LOG.println("■ BbsForumRecord #readRecord() あるオーナーの全てのフォーラムレコードを読み出す");
			
		Vector	vrec	=	new	Vector(30);
		int	n		=	db.readBbsForums(ownerkey, vrec);
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
		int	n		=	db.readBbsForums(ownerkey, forumkey, tb);
		if(n==0)	return	null;
		return	tb;
	}



	/**
	 * 
	 * @return
	 */
	boolean	isOK(){
		if(isEmpty(Gear.strHashSP(htb, BbsForumDB.SUBJECT))){
			htb.put(MESSAGE, "★ タイトルは省略できません");
			return	false;
		}
		if(isEmpty(Gear.strHashSP(htb, BbsForumDB.CONTENT))){
			htb.put(MESSAGE, "★ 内容は省略できません");
			return	false;
		}
		return	true;
	}
	
	/**
	 * アップロードされたファイルを受け取る
	 */
	boolean	upload(){
		if(LOG.fa)	LOG.println("■ BbsMakeForum #upload()");
		/*
		 * ファイル受信にはディレクトリを特定するためforumkey が必要なので
		 * レコードの書き込みを行う．この中で forumkey が確定する
		 */
		if(forumkey.equals(DUMMY_KEY)){
			boolean	ok	=	writeRecord();
			if(!ok){
			    return	false;
			}
		}
		/*
		 * 添付ファイルかどうか
		 */
		//
		String 	fname	=	getFile(conf.forumDir(ownerkey, forumkey));	// 移動
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
		         * ファイル名の中のディレクトリは / で区切られているので、
		         * FSに変換してファイル処理を行うこと
		         * 表示する関係から以下ではfnameをそのまま使っている
		         */
		        String	zipPath		=	conf.forumDir(ownerkey, forumkey) + FS +fname;
		        String	unzipDir	=	conf.forumDir(ownerkey, forumkey);
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
		if(LOG.fa)	LOG.println("■ BbsMakeForum #writeAttachment()");
		
		htb.put(BbsForumDB.ATTACHMENT, attachmentStr);
		updateRecord();
	}

	/**
	 * アップロードされたファイルをプロファイルディレクトリへ移す
	 * 複数ファイルの同時アップロードに対応している
	 * アップロード先のファイル記述子を返す
	 */
	String	getFile(String toDir){
		if(LOG.fa)	LOG.println("■ BbsMakeForum #getFile()");
		/*
		 * 戻り値に使うのでファイル名を取得しておく
		 */
		String		fromDir	=	strHash(htb,UPLOAD_DIR_NAME);
		String	[]	files	=	(new File(fromDir)).list();
		String		fname	=	files[0];
		/*
		 *   /home/pc/temp/[ランダムに作成したディレクトリ名]   ⇒　/home/bbs/[owner]/[forum]
		 *  ファイルが複数あれば全てを移動する
		 */
		Gear.moveFiles(toDir, fromDir);
		
		/* 作業ディレクトリが残るので全て消去する */
		Gear.delDir(fromDir);
		
		return	fname;		
	}
	/**
	 * データベースからこのレコードの添付ファイルリストを取得し、HashSetオブジェクトにして返す
	 * @return		添付ファイルリスト
	 */	
	Hashtable	getAttachmentHash(){
		if(LOG.fa)	LOG.println("■ BbsMakeForum #getAttachmentHash()");

		/*
		 * データベースの attachment を読んでファイルリストとその種別を得る
		 */
		Hashtable	record		=	readRecord(forumkey);
		String		attachment	=	Gear.strHash(record,BbsForumDB.ATTACHMENT);
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
		if(LOG.fa)	LOG.println("■ BbsMakeForum #getFilename()");

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
		if(LOG.fa)	LOG.println("■ BbsMakeForum #isAttachment()");

		if(pos >= cs.size())	return	false;
		
		String	fname	=	cs.get(pos);
		if(fname.charAt(0)=='*'){
			return	true;
		}
		return	false;			
	}
	/**
	 * すでにアップロードされているファイルを削除する
	 * アップロードでフォーラムキーが確定するので、この処理では必ず forumkey がある
	 * 添付ファイル情報が更新されるので、レコードの書き込みも行う
	 */	
	void	delete(){
		if(LOG.fa)	LOG.println("■ BbsMakeForum #file()");
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
		if(LOG.fa)	LOG.println("■ BbsMakeForum #delete()");
		File	fp		=	new File(conf.forumDir(ownerkey, forumkey) + File.separator + fname);
		fp.delete();
	}

	/**
	 * エディタの現在の設定行数を得る
	 * @return
	 */
	int getEditorRows(){
		if(LOG.fa)	LOG.println("■ BbsMakeForum #getEditorRows()");
		//		
		Hashtable	tb	=	new Hashtable(20);
		BbsInfoDB	bi	=	new BbsInfoDB(ownerkey, szDB, broker);
		bi.readBbsInfo(ownerkey,tb);
		return	Integer.parseInt(strHash(tb, BbsInfoDB.EDITOR));
	}
	//
	//　行数を増やす
	//
	void	puls(){
		if(LOG.fa) LOG.println("■ BbsMakeForum #puls() : 行数を増やす の先頭です");
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
		if(LOG.fa) LOG.println("BbsMakeForum #minus() : 行数を減らす の先頭です");
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
		if(LOG.fa)	LOG.println("■ BbsMakeForum #display(boolean editmode)");
		
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
		
		setCheckbox(htb);
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
		if(LOG.fa)	LOG.println("■ BbsMakeForum #setInitialProperty()");
		//		
		htb.put(MESSAGE,"");
		/*
		 * システムハッシュのフォーラムキーを調べて新規が既存かを決定し
		 * レコードを record に取り込む
		 * 新規の場合はテンプレートレコードで、キーはダミーキーを設定しておく
		 */
		forumkey	=	Gear.strHashIncludeNull(htb, BbsForumDB.FORUM_KEY);
		if(forumkey.equals(DUMMY_KEY)){
			/* 新規作成 */
			record	=	getTemplateRecord(ownerkey, forumkey);
			/*　フォーマットスタイルを個人情報DBから読み込んで
			 *　編集内容にアペンドしておく 
			 */		
			//format		=	readFormatStyle();
			//record.put(BbsForumDB.CONTENT,format);			
		}else{
			record	=	readRecord(forumkey);
			if(LOG.fa) LOG.outHash(record, "★ BbsMakeForum #setInitialProperty  :読み出した forum レコード");		
		}
		/*
		 * レコードをシステムハッシュに展開する
		 */
		toSystemHash(htb, record);
	}
	/**
	 * ハッシュに入っているレコードをシステムハッシュにコピーする
	 * @param sys
	 * @param tb
	 * @return
	 */
	public	 Hashtable	toSystemHash(Hashtable sys, Hashtable tb){
		if(LOG.fa)	LOG.println("■ BbsForumRecord #toSystemHash() レコードをシステムハッシュにコピーする");
		
		sys.put(BbsForumDB.OWNER_KEY,	Gear.strHash(tb,BbsForumDB.OWNER_KEY));
		sys.put(BbsForumDB.FORUM_KEY,	Gear.strHash(tb,BbsForumDB.FORUM_KEY));
		sys.put(BbsForumDB.ALIVE_FLAG,	Gear.strHash(tb,BbsForumDB.ALIVE_FLAG));
		sys.put(BbsForumDB.GROUP_FLAG,	Gear.strHash(tb,BbsForumDB.GROUP_FLAG));
		sys.put(BbsForumDB.RATING_FLAG,	Gear.strHash(tb,BbsForumDB.RATING_FLAG));
		sys.put(BbsForumDB.HANDLE_FLAG,	Gear.strHash(tb,BbsForumDB.HANDLE_FLAG));
		sys.put(BbsForumDB.DATE,		Gear.strHash(tb,BbsForumDB.DATE));
		sys.put(BbsForumDB.SUBJECT,		Gear.strHash(tb,BbsForumDB.SUBJECT));
		sys.put(BbsForumDB.CONTENT,		Gear.strHash(tb,BbsForumDB.CONTENT));
		sys.put(BbsForumDB.ATTACHMENT,	Gear.strHash(tb,BbsForumDB.ATTACHMENT));
		
		/*
		 * 各フラグの値からチェックボックスの値を設定する
		 */	
		
		// アクティブフラグ	
		if(Gear.strHashSP(tb,BbsForumDB.ALIVE_FLAG).equals(BbsForumDB.ACTIVE)){
			sys.put("_disp", "1");
		}else{
			sys.put("_disp", "");
		}
		// グループフラグ	
		if(Gear.strHashSP(tb,BbsForumDB.GROUP_FLAG).equals(BbsForumDB.GROUP_LEARNING)){
			sys.put("_group", "1");
		}else{
			sys.put("_group", "");
		}
		// 支持度フラグ
		if(Gear.strHashSP(tb,BbsForumDB.RATING_FLAG).equals(BbsForumDB.RATING_MODE)){
			sys.put("_rating", "1");
		}else{
			sys.put("_rating", "");
		}
		// ハンドルフラグ
		if(Gear.strHashSP(tb,BbsForumDB.HANDLE_FLAG).equals(BbsForumDB.BY_HANDLE)){
			sys.put("_handle", "1");
		}else{
			sys.put("_handle", "");
		}		
		return sys;
	}	
	/**
	 * WEB のチェックボックスの値を初期設定する
	 * @param htb
	 */
	void	initCheckbox(Hashtable htb){
		if(LOG.fa)	LOG.println("■ BbsMakeForum #initCheckbox()");
		//		
		htb.put("_ck_group", "");
		htb.put("_ck_rating", "");
		htb.put("_ck_handle", "checked");
		//htb.put("_attach", "");
	}
	/**
	 * WEB のチェックボックスの値を初期設定する
	 * @param htb
	 */
	void	setCheckbox(Hashtable htb){
		if(LOG.fa)	LOG.outHash(htb,"■ BbsMakeForum #setCheckbox()");

		/*
		 * ハッシュから取り出した値はnullかもしれないので strHashSP() を使って "" に変えて受け取る
		 */	
		// アクティブフラグ	
		if(Gear.strHashSP(htb,"_disp").equals("1")){
			htb.put("_ck_disp", "checked");
			htb.put(BbsForumDB.ALIVE_FLAG, BbsForumDB.ACTIVE);
		}else{
			htb.put("_ck_disp", "");
			htb.put(BbsForumDB.ALIVE_FLAG, BbsForumDB.HIDDEN);
		}
		// グループフラグ	
		if(Gear.strHashSP(htb,"_group").equals("1")){
			htb.put("_ck_group", "checked");
			htb.put(BbsForumDB.GROUP_FLAG, BbsForumDB.GROUP_LEARNING);
		}else{
			htb.put("_ck_group", "");
			htb.put(BbsForumDB.GROUP_FLAG, BbsForumDB.NORMAL_BBS);
		}
		// 支持度フラグ
		if(Gear.strHashSP(htb,"_rating").equals("1")){
			htb.put("_ck_rating", "checked");
			htb.put(BbsForumDB.RATING_FLAG, BbsForumDB.RATING_MODE);
		}else{
			htb.put("_ck_rating", "");
			htb.put(BbsForumDB.RATING_FLAG, BbsForumDB.NOT_RATING_MODE);
		}
		// ハンドルフラグ
		if(Gear.strHashSP(htb,"_handle").equals("1")){
			htb.put("_ck_handle", "checked");
			htb.put(BbsForumDB.HANDLE_FLAG, BbsForumDB.BY_HANDLE);
		}else{
			htb.put("_ck_handle", "");
			htb.put(BbsForumDB.HANDLE_FLAG, BbsForumDB.BY_UID);
		}

	}
	
	/**
	 * 規定値のフォーマットを読み込む
	 * @return
	 */
	public	String	readFormatStyle(){
		if(LOG.fa)	LOG.println("■ BbsMakeForum #readFormatStyle()");
		//		
		Hashtable	tb	=	getInfoRecord();
		format			=	Gear.strHash(tb, BbsInfoDB.FORMAT);
		return	format;	
		
	}	
	/**
	 * ユーザー情報データベースを読んで、エディタの行数をシステムハッシュにセットする
	 */
	void	setEditorRows(){
		if(LOG.fa)	LOG.println("■ BbsMakeForum #setEditorRows()");
		//		
		Hashtable	tb	=	getInfoRecord();
		htb.put(BbsInfoDB.EDITOR, strHash(tb,BbsInfoDB.EDITOR));
	}
	/**
	 * エディタの行数を更新する
	 */
	void	updateEditorRows(){
		if(LOG.fa)	LOG.println("■ BbsMakeForum #updateEditorRows()");
		//
		BbsInfoDB	bi	=	new BbsInfoDB(ownerkey, szDB, broker);
		bi.updateEditorRow(ownerkey, strHash(htb,BbsInfoDB.EDITOR));
	}
	/**
	 * ユーザー情報を読みハッシュに入れて返す
	 * @return
	 */
	Hashtable	getInfoRecord(){
		if(LOG.fa)	LOG.println("■ BbsMakeForum #getInfoRecord()");
		//		
		BbsInfoDB	bi	=	new BbsInfoDB(ownerkey, szDB, broker);
		Hashtable	tb	=	new Hashtable(20);
		int			cnt	=	bi.readBbsInfo(ownerkey,tb);
		if(cnt==0){
			tb	=	setInitialRecord();
		}
		return	tb;
	}

	Hashtable	setInitialRecord(){
	    if(LOG.fa)	LOG.println("■ BbsMakeForum #setInitialRecord()");
		/**
		 *  初期値をハッシュにセットする（教師用依存）
		 *  パスワードはBbsLoginで使用するが、BbsLoginでは記載されていない場合は
		 *  membersInfo 及び szDB をチェックするので、空白を書いても問題ない 
		 */
	    Hashtable	tb	=	
	        BbsUtil.setInitialData(getParameter(UID), "", getParameter(DIVISION), getParameter(UNAME), getParameter(MAIL), para);
		return	tb;		
		
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
	////////////////////////////////////////////////////////////////////////////////////////
	//
	// 	ハッシュテーブル(htb)を使って部分的に切り取ったソースデータ（exHtml）
	// 	の内容を置き換えて出力処理する．表などの反復出力に利用するが個々の処理
	// 	内容は、key で特定される．
	//
	////////////////////////////////////////////////////////////////////////////////////////

	public void	write(String key,Vector exHtml){
		if(LOG.fa)	LOG.println("■ BbsMakeForum #write()");
		
		if(key.equals("uploadFileList")){
		    uploadFileList(exHtml);
		
		}else if(key.equals("isNoFiles")){
		    isNoFiles(exHtml);
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
			int	n	= Integer.parseInt( Gear.strHash(htb,FCOUNT) );// 既に表示した行があるか
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
		Hashtable	record		=	readRecord(forumkey);
		if(record==null){
			htb.put(FCOUNT, "0");
			return;				
		}
		//
		String		attachment	=	Gear.strHash(record,BbsForumDB.ATTACHMENT);
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
	 * ファイルのWebにおける完全URLを返す
	 * @param cs
	 * @param pos
	 * @return		完全URL
	 */
	String	getFileUrl(Csv cs, int pos){
		if(LOG.fa)	LOG.println("■ BbsMakeForum #getFileUrl()");

		String	fname	=	getFilename(cs, pos);
		if(Gear.isEmpty(fname))	return	"";
		return	conf.forumUrl(ownerkey, forumkey) + "/" + fname;
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


















