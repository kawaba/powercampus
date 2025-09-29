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
 * スレッドのプレビュー
 * 
	<program $jbbs.BbsThreadPreview>
		<dispatch  html=ThreadPreview.html number=3230     class=jbbs.BbsThreadPreview />
		<variable>
		  <receive  NUMBER   STAMP  GROUP  BBS_OWNER_KEY  UID MAIL UNAME  HOMEURL DIVISION forumkey threadkey
		            subject content attachment date  />
		  <accept   SUBMIT/>
		  <keep      date />
		</variable>		
	</program> 
 * 
 */
public class BbsThreadPreview extends SuperPlayer implements BbsVar{
	
	BbsConf				conf;
	//
	BbsThreadDB			db;
	String				ownerid;
	String				szDB;
	DbConnectionBroker	broker;
	
	String				forumkey;
	String				owner_forumkey;
	String				userid;

	public	BbsThreadPreview(){
		super();
		if(LOG.fa)	LOG.println("■ BbsThreadPreview #コンストラクタ");
	}
	
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		if(LOG.fa) LOG.outHash(htb,"■ BbsThreadPreview #initialize()");
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
		owner_forumkey	=	BbsKeys.ownerForumKey(ownerid, forumkey);
		userid			=	strHash(htb,UID);
	}
	/**
	 * jbbsライブラリのためのシステムコンフィギュレーションオブジェクトを作成する
	 */
	public BbsConf	createBbsConf(String BbsConfPath, Hashtable ht){
		if(LOG.fa) LOG.println("■ BbsThreadPreview #createBbsConf()");
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
		if(LOG.fa) LOG.outHash(htb,"■ BbsThreadPreview #dispatch()");
		
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
		if(LOG.fa) LOG.outHash(htb,"■ BbsThreadPreview #display(boolean editmode)");
		if(!editmode){
			htb.put(MESSAGE,"");
		}
		/*
		 * フォーラム状態表示アイコンの設定
		 */
		Hashtable	tbl			=	getForumRecord(forumkey);
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
		
		/*
		 * スレッド関係の表示データ
		 */
		String	threadkey		=	Gear.strHashSP(htb,(BbsThreadDB.THREAD_KEY));
		String	uid				=	Gear.strHashSP(htb,(BbsThreadDB.USERID));
		String	date			=	Gear.strHashSP(htb,(BbsThreadDB.DATE));
		String	subject			=	Gear.strHashSP(htb,(BbsThreadDB.SUBJECT));
		String	content			=	Gear.strHashSP(htb,(BbsThreadDB.CONTENT));
		String	attach			=	Gear.strHashSP(htb,(BbsThreadDB.ATTACHMENT));
		/*
		 * スレッド所有者の個人情報レコードを得て、そこから名前またはハンドルを取り出す
		 */
		String		dispName 	=	"";
		Hashtable	rec			=	getInfoRecord(uid);
		if(rec==null){
			dispName	=	"#" + uid;				
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
			dispName	=	"#" + uid;
		}
		/*
		 * ハッシュに設定する
		 */ 
		htb.put(BbsThreadDB.THREAD_KEY	, threadkey);
		htb.put("_ownerName"			, dispName);
		htb.put("_dateStr"				, BbsUtil.getFormattedDate(date));
		htb.put("_subjectPrv"			, subject);
		htb.put("_contentPrv"			, getContent(content, forumkey, threadkey));
		htb.put("_attachmentPrv"		, getAttachmentHTML(htb));

		/* strHash(htb,DISPFILE)にはファイルの完全パス名が入っている */
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
		if(LOG.fa)	LOG.println("■ BbsForumRecord #readRecord() ひとつのレコードを読み結果をハッシュにいれて返す");
			
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
	public	String	getContent(String content, String forumkey, String threadkey){
		if(LOG.fa)	LOG.println("■ BbsThreadPreview #getContent()");
		
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
		if(LOG.fa)	LOG.println("■ BbsThreadPreview #getAttachmentHTML()");
		
		String	threadkey	=	strHash(record,(BbsThreadDB.THREAD_KEY));
		/*
		 * 添付ファイル名はCSV形式文字列なのでCsvオブジェクトに直す
		 */
		//Csv	cs	=	new Csv(Gear.strHashSP(record, BbsThreadDB.ATTACHMENT));
		Csv	cs	=	BbsUtil.makeAttachmentCsv(Gear.strHashSP(record, BbsThreadDB.ATTACHMENT));
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
		if(LOG.fa)	LOG.println("■ BbsThreadPreview #getFilename()");

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
		if(LOG.fa)	LOG.println("■ BbsThreadPreview #getColorClass()");

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
		if(LOG.fa)	LOG.println("■ BbsThreadPreview #isAttachment()");

		if(pos >= cs.size())	return	false;
		
		String	fname	=	cs.get(pos);
		if(fname.charAt(0)=='*'){
			return	true;
		}
		return	false;			
	}	
	// 部分的に切り取ったソースデータ（exHtml）を出力するか否か、ハッシュテーブル
	// (htb)を使って判断し、出力する場合には、htb を使ってexHtml の内容を書き換えて
	// 出力する．個々の処理内容は、key で特定される．
	//
	public void	blockWrite(String key,Vector exHtml,Hashtable htb){
		if(LOG.fa)	LOG.println("■ BbsThreadPreview #blockWrite()");
		
		if(key.equals("Subject")){
			String	dt	=	Gear.strHashSP(htb, BbsThreadDB.SUBJECT);
			if(!isEmpty(dt)){
				printVector(exHtml,htb);
			}
			return;
					
		}else if(key.equals("Content")){
			String	dt	=	Gear.strHashSP(htb, BbsThreadDB.CONTENT);
			if(!isEmpty(dt)){
				printVector(exHtml,htb);
			}
			return;
		}else if(key.equals("Attach")){
			String	dt	=	Gear.strHashSP(htb, BbsThreadDB.ATTACHMENT);
			if(!isEmpty(dt)){
				printVector(exHtml,htb);
			}
			return;
		}
	}

}
