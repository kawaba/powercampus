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
 * フォーラムのプレビュー
 * 
	<program $jbbs.BbsForumPreview>
		<dispatch  html=ForumPreview.html number=3130      class=jbbs.BbsForumPreview />
		<variable>
		  <receive  NUMBER STAMP  GROUP  BBS_OWNER_KEY  UID MAIL UNAME  HOMEURL DIVISION forumkey subject content attachment/>
		  <accept   SUBMIT />
		  <keep      date />
		</variable>		
	</program>
 * 
 *	
 *  receive 変数
 * 		content		記述したテキスト
 * 		attachment	アップロードファイルリスト
 * 
 */
public class BbsForumPreview extends SuperPlayer implements BbsVar{
	
	BbsConf				conf;
	//
	BbsForumDB			db;
	String				ownerid;
	String				szDB;
	DbConnectionBroker	broker;
	
	String				userid;

	public	BbsForumPreview(){
		super();
		if(LOG.fa)	LOG.println("■ BbsForumPreview #コンストラクタ");
	}
	
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		
		super.setInit(out,htb,para);
		//
		if(LOG.fa)	LOG.println("■ BbsForumPreview #initialize()");
		conf			=	createBbsConf(para.get("BBS_CONF_PATH"),htb);		
		//
		ownerid			=	strHash(htb, BBS_OWNER_KEY);
		szDB			=	strHash(htb, GROUP);
		broker			=	(DbConnectionBroker)htb.get(BROKER);
		db				=	new BbsForumDB(szDB, broker);

		userid			=	Gear.strHash(htb,UID);	// userid
		
	}
	/**
	 * jbbsライブラリのためのシステムコンフィギュレーションオブジェクトを作成する
	 */
	public BbsConf	createBbsConf(String BbsConfPath, Hashtable ht){
		if(LOG.fa)	LOG.println("■ BbsForumPreview #createBbsConf()");
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
	 * 次に表示するWebの表示モード（DISP_KEY）をシステムハッシュにセットする．
	 */
	public	String	dispatch(){
		if(LOG.fa) LOG.outHash(htb,"■ BbsForumPreview #dispatch()");
		
		/*
		 * 処理分岐を判断するキーはシステムハッシュからDISPATCH_KEYをキーとして取り出す
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
			 * cmd には起動したいクラスキーが入っている
			 */
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
	 * 画面表示
	 * 
	 */
	public  void	display(boolean editmode){
		if(LOG.fa)	LOG.println("■ BbsForumPreview #display(boolean editmode)");
		if(!editmode){
			putParameter(MESSAGE,"");
		}
		/*
		 * フォーラム状態表示アイコンの設定
		 */
		String	alive		=	Gear.strHashSP(htb,"_disp");
		if(LOG.fa) LOG.println("■■ alive=" + alive);
		String	signIcon	=	"";
		if(alive.equals("1")){
			signIcon	=	"lockIconOpen.gif";
		}else{
			signIcon	=	"lockIcon.gif";
		}
		htb.put("_signIcon", signIcon);
		/*
		 * フォーラム関係の表示データ
		 */
		String	forumKey	=	Gear.strHashSP(htb,(BbsForumDB.FORUM_KEY));
		String	date		=	Gear.strHashSP(htb,(BbsForumDB.DATE));
		String	forumOwner	=	Gear.strHashSP(htb,(BbsForumDB.OWNER_KEY));
		String	ownerName	=	strHash(htb,UNAME);
		
		String	subject		=	Gear.strHashSP(htb,(BbsForumDB.SUBJECT));
		String	content		=	Gear.strHashSP(htb,(BbsForumDB.CONTENT));
		String	attach		=	Gear.strHashSP(htb,(BbsForumDB.ATTACHMENT));
		/*
		 * 名前で表示するかハンドルで表示するかハンドルフラグから決定してハッシュに設定する
		 */
		String		handleFlag	=	Gear.strHash(htb,BbsForumDB.HANDLE_FLAG);
		String		dispName 	=	"";
		Hashtable	rec			=	getInfoRecord(forumOwner);
		if(rec==null){
			dispName	=	"情報未登録";				
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
			dispName	=	"情報未登録";
		}
		/*
		 * その他の情報
		 */
		htb.put(BbsForumDB.FORUM_KEY, 	forumKey);
		htb.put("_dateStr", 		BbsUtil.getFormattedDate(htb));
		htb.put("_ownerName", 		dispName);
		htb.put("_subjectPrv", 		subject);
		htb.put("_contentPrv", 		getContent(content, forumKey));
		htb.put("_attachmentPrv", 	getAttachmentHTML(htb));
		
		/* strHash(htb,DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v,htb);
		
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
	Hashtable	getInfoRecord(String uid){
		BbsInfoDB	infodb	=	new BbsInfoDB(uid, szDB, broker);
		Hashtable	dt		=	new Hashtable();
		int		count	=	infodb.readBbsInfo(userid, dt);
		if(count>0)	return	dt;
		return			null;
	}
	/**
	 * PMLをパースしたHTMLを返す
	 * @param content	内容を表すPML
	 * @return			同 HTML
	 */
	public	String	getContent(String content, String forumkey){
		
		/*
		 * pmlテキストに段落エイリアスを付加する
		 * テンプレートがあるのでそれを読み込んでアペンドする
		 */
		//if(DBG.fa) DBG.println("■ BbsForum #getContent : fileName = " + conf.pmlTemplatePath());
		TemplateBox		tb			=	new TemplateBox(conf.pmlTemplatePath());
		String			template	=	tb.get("forum");
		String			emptext		=	template + Gear.lineSeparator() + content;
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
		if(LOG.fa)	LOG.println("■ BbsForum #getAttachmentPML()");
		
		String	forumkey	=	strHash(record,(BbsForumDB.FORUM_KEY));
		/*
		 * 添付ファイル名はCSV形式文字列なのでCsvオブジェクトに直す
		 */
		//Csv	cs	=	new Csv(Gear.strHashSP(record, BbsForumDB.ATTACHMENT));
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
			 * テンプレートをハッシュで書き変えてバッファに格納する
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
		if(LOG.fa)	LOG.println("■ BbsForumPreview #isAttachment()");

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
	    if(LOG.fa)	LOG.println("■ BbsForumPreview #write()");
	    
		if(key.equals("Subject")){
		    Subject(exHtml);
			
		}else if(key.equals("Content")){
		    Content(exHtml);
			
		}else if(key.equals("Attach")){
		    Attach(exHtml);
		}
	}	
	
	void	Subject(Vector exHtml){
	    if(LOG.fa)	LOG.println("■ BbsForumPreview #Subject()");
		String	dt	=	Gear.strHashSP(htb, BbsForumDB.SUBJECT);
		if(!isEmpty(dt)){
			printVector(exHtml);
		}	    
	}
	void	Content(Vector exHtml){
	    if(LOG.fa)	LOG.println("■ BbsForumPreview #Content()");
		String	dt	=	Gear.strHashSP(htb, BbsForumDB.CONTENT);
		if(!isEmpty(dt)){
			printVector(exHtml);
		}
	}
	void	Attach(Vector exHtml){
	    if(LOG.fa)	LOG.println("■ BbsForumPreview #Attach()");
		String	dt	=	Gear.strHashSP(htb, BbsForumDB.ATTACHMENT);
		if(!isEmpty(dt)){
			printVector(exHtml);
		}
	}
}
