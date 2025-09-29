package jbbs;
import tktools.*;
import database.DbConnectionBroker;
import epml.*;
import framework.LOG;
import framework.Param;
import framework.SuperPlayer;

import java.io.*;
import java.util.*;
/**
 * ポスト（記事）のプレビュー
 * 
	<program $jbbs.BbsPostPreview>
		<dispatch  html=PostPreview.html number=3330       class=jbbs.BbsPostPreview />
		<variable>
		    <receive  NUMBER STAMP  GROUP  BBS_OWNER_KEY  UID MAIL UNAME  HOMEURL DIVISION forumkey threadkey postkey 
		              subject content attachment date  />
		    <accept   SUBMIT />
            <keep     date />
        </variable>
	</program>	
 * 
 */
public class BbsPostPreview extends SuperPlayer implements BbsVar{
	// NUMBER STAMP  GROUP  BBS_OWNER_KEY  UID MAIL UNAME  HOMEURL forumkey threadkey postkey 
	// subject content attachment date
	
	BbsConf				conf;
	//
	BbsPostDB			db;
	DbConnectionBroker	broker;

	/** 受け取り変数 */
	String				szDB;
	String				ownerkey;

	String				userid;
	String				userMail;
	String				userName;
	String				userUrl;
	
	String				forumkey;
	String				threadkey;
	String				postkey;
	
	String				subject;
	String				content;
	String				attachment;
	String				date;

	/** ここで作成するキー */
	String				bbskey;				// userid_forumkey_threakey

	public	BbsPostPreview(){
		super();
		if(LOG.fa)	LOG.println("■ BbsPostPreview #コンストラクタ");
	}
	
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		if(LOG.fa) LOG.outHash(htb,"■ BbsPostPreview #initialize()");
		super.setInit(out,htb,para);
		//
		conf			=	createBbsConf(para.get("BBS_CONF_PATH"),htb);		
		broker			=	(DbConnectionBroker)htb.get(BROKER);
		db				=	new BbsPostDB(szDB, broker);		
		//
		szDB			=	Gear.strHash(htb, GROUP);
		ownerkey		=	Gear.strHash(htb, BBS_OWNER_KEY);
		userid			=	Gear.strHash(htb, UID);
		userMail		=	Gear.strHash(htb, MAIL);
		userName		=	Gear.strHash(htb, UNAME);
		userUrl			=	Gear.strHash(htb, HOMEURL);

		forumkey		=	strHash(htb,"_forumkey");
		threadkey		=	strHash(htb,"_threadkey");
		postkey			=	strHash(htb,"_postkey");
		
		subject			=	strHash(htb,"_subject");
		content			=	strHash(htb,"_content");
		attachment		=	strHash(htb,"_attachment");
		date			=	strHash(htb,"_date");
		/*
		 * 複合キーを作成する
		 */
		bbskey			=	BbsKeys.bbsKey(ownerkey, forumkey, threadkey);

	}
	/**
	 * jbbsライブラリのためのシステムコンフィギュレーションオブジェクトを作成する
	 */
	public BbsConf	createBbsConf(String BbsConfPath, Hashtable ht){
		if(LOG.fa) LOG.println("■ BbsPostPreview #createBbsConf()");
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
		if(LOG.fa) LOG.outHash(htb,"■ BbsPostPreview #dispatch()");
		
		/*
		 * 処理分岐を判断するキーはシステムハッシュからParam.DISPATCH_KEYをキーとして取り出す
		 * リターンコードと次に表示するWEBの表示モードは設定忘れを防ぐため規定値をセットしておく
		 */
		String 	cmd			=	strHash(htb,DISPATCH_KEY);
		String	ret			=	DISPATCH_DEFAULT;	// WEB表示
		String	disp_mode	=	DISP_NEW;			// クリアーして新規表示
		if(cmd.equals("RETURN")){
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

	/*////////////////////////////////////////////////////////////////////////////
	 * 
	 *    表 示 処 理
	 * 
	 * 　　    表示処理ははコントローラーが呼び出す表示メソッドであり
	 * 　　    直接呼び出すことはできない．（機能しない）
	 * 
	 *////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 
	 */
	
	public  void	display(boolean editmode){
		if(LOG.fa) LOG.outHash(htb,"■ BbsPostPreview #display(boolean editmode)");
		if(!editmode){
			htb.put(MESSAGE,"");
		}
		/*
		 * 名前で表示するかハンドルで表示するかハンドルフラグを得ておく
		 */
		Hashtable	tbl			=	getForumRecord(forumkey);
		String		handleFlag	=	Gear.strHash(tbl,BbsForumDB.HANDLE_FLAG);
		
		/*
		 * ポスト所有者の個人情報レコードを得て、そこから名前またはハンドルを取り出す
		 */
		setUserInfo(userid, handleFlag);		
		/*
		 * 変換する必要のある項目をハッシュに設定する
		 * content を htmlに変換したものをWeb変数 _contenHtml に入れる
		 */ 
		putParameter("_dateStr"		, BbsUtil.getFormattedDate(date));
		
		putParameter("_subjectPrv"	, getParameter("_subject"));
		/*
		 * 罫線表示のために必ず表示される
		 * 空白の場合でも何か値が必要なので " "を入れる
		 */
		String	conStr	=	getContent(content, forumkey, threadkey, postkey);
		
		if(LOG.fa){
		    LOG.println("★content");
		    LOG.println(":" + getParameter("content") + ":" );
		}
		
		
		if(isEmpty(conStr)){
		   putParameter("_contentPrv"	, " ");
		}else{
		    putParameter("_contentPrv"	, conStr);
		}
		
		String	atStr	=	getAttachmentHTML(postkey, attachment);
		if(!isEmpty(atStr)){
		    putParameter("_attachmentPrv", atStr );
		}else{
		    putParameter("_attachmentPrv", "" );
		}
		/* 
		 * 表示
		 * strHash(htb,DISPFILE)にはファイルの完全パス名が入っている
		 */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v,htb);			
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
		if(LOG.fa)	LOG.println("■ BbsPostPreview #readRecord() ひとつのレコードを読み結果をハッシュにいれて返す");
			
		Hashtable	tb	=	new	Hashtable(20);
		int	n		=	(new BbsForumDB(szDB, broker)).readBbsForums(ownerkey, forumkey, tb);
		if(n==0)	return	null;
		return	tb;
	}
	
	/**
	 * 表示名とフェースアイコンをシステムハッシュにセットする
	 * ユーザー情報ＤＢを読む
	 * @param userid
	 * @param handleFlag
	 */
	void	setUserInfo(String	id, String handleFlag){
		if(LOG.fa) LOG.println("■ BbsPostPreview #setUserInfo()");
		/*
		 * スレッド所有者の個人情報レコードを得て、そこから名前またはハンドル及びアイコンファイル名を取り出す
		 */
		String		dispName 	=	"";
		String		faceIcon 	=	"";
		Hashtable	inforec		=	getInfoRecord(id);
		if(inforec==null){
			dispName	=	"#" + id;
			faceIcon	=	getFaceIcon();				
		}else	if(handleFlag.equals(BbsForumDB.BY_UID)){
			/*
			 * 名前で表示
			 */
			dispName	=	Gear.strHash(inforec, BbsInfoDB.NAME);
			faceIcon	=	getFaceIcon( Gear.strHash(inforec, BbsInfoDB.ICON_FILE) );	
		}else{
			/*
			 * ハンドルで表示
			 */
			dispName	=	Gear.strHash(inforec, BbsInfoDB.HANDLE);
			faceIcon	=	getFaceIcon( Gear.strHash(inforec, BbsInfoDB.ICON_FILE) );	
		}
		if(Gear.isEmpty(dispName)){
			dispName	=	"#" + id;
		}
		htb.put("_faceIconURL", faceIcon);
		htb.put("_dispName"	  , dispName);
	}
	/**
	 * システム標準フェイスアイコンへのＵＲＬを返す
	 * @return
	 */
	String	getFaceIcon(){
		if(LOG.fa) LOG.println("■ BbsPostPreview #getFaceIcon()");

		return	conf.sysFileUrl() + conf.sysFaceIcon();
	}
	/**
	 * ユーザーフェイスアイコンへのＵＲＬを返す
	 * @param fname
	 * @return
	 */
	String	getFaceIcon(String fname){
		if(LOG.fa) LOG.println("■ BbsPostPreview #getFaceIcon()");
		
		return	conf.userProfileURL(userid, szDB) + fname;
	}
	
	/**
	 * ユーザー情報をハッシュテーブルで得る
	 * 存在しない時は null を返す
	 * 
	 * @return
	 */
	Hashtable	getInfoRecord(){
		if(LOG.fa) LOG.println("■ BbsPostPreview #getInfoRecord()");
		
		return	getInfoRecord(userid);
	}
	/**
	 * ユーザーIDからユーザー情報をハッシュテーブルで得る
	 * 存在しない時は null を返す
	 * 
	 * @return
	 */
	Hashtable	getInfoRecord(String uid){
		if(LOG.fa) LOG.println("■ BbsPostPreview #getInfoRecord()");
		
		BbsInfoDB	infodb	=	new BbsInfoDB(uid, szDB, broker);
		Hashtable	dt		=	new Hashtable();
		int		count	=	infodb.readBbsInfo(uid, dt);
		if(count>0)	return	dt;
		return			null;
	}

	/**
	 * PMLをパースしたHTMLを返す
	 * @param content	内容を表すPML
	 * @return			同 HTML
	 */
	public	String	getContent(String content, String forumkey, String threadkey, String postkey){
		if(LOG.fa) LOG.println("■ BbsPostPreview #getContent()");
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
		if(LOG.fa) LOG.println("■ BbsPostPreview #getAttachmentHTML()");
		
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
		if(LOG.fa) LOG.println("■ BbsPostPreview #getFilename()");

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
		if(LOG.fa) LOG.println("■ BbsPostPreview #getColorClass()");

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
		if(LOG.fa) LOG.println("■ BbsPostPreview #isAttachment()");

		if(pos >= cs.size())	return	false;
		
		String	fname	=	cs.get(pos);
		if(fname.charAt(0)=='*'){
			return	true;
		}
		return	false;			
	}	
	/**
	 * ブロック出力
	 */
	public void	write(String key,Vector exHtml){
	    if(LOG.fa)	LOG.println("■ BbsPostPreview #write()");
	    
		if(key.equals("Subject")){
		    Subject(exHtml);
			
		}else if(key.equals("Content")){
		    Content(exHtml);
			
		}else if(key.equals("Attach")){
		    Attach(exHtml);
		}
	}	
	
	void	Subject(Vector exHtml){
	    if(LOG.fa)	LOG.println("■ BbsPostPreview #Subject()");
		String	dt	=	getParameter("_subjectPrv");
		if(!isEmpty(dt)){
			printVector(exHtml);
		}	    
	}
	void	Content(Vector exHtml){
	    if(LOG.fa)	LOG.println("■ BbsPostPreview #Content()");
	    /*
	     * 罫線の問題があるので内容は必ず表示する
	     * 最悪 " " が入っている
	     */
	    printVector(exHtml);
	}
	void	Attach(Vector exHtml){
	    if(LOG.fa)	LOG.println("■ BbsPostPreview #Attach()");
		String	dt	=	getParameter("_attachmentPrv");
		if(!isEmpty(dt)){
			printVector(exHtml);
		}
	}

}
