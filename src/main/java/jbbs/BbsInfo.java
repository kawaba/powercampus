/*
 * ユーザー情報の設定
 * 教師、学生共共通
 */
package jbbs;
import  tktools.*;
import  java.util.*;
import  java.io.*;

import database.*;
import framework.*;

/**
 * 個人情報を設定・変更する
 *
	#
	# ##################
	# 	BbsInfo
	# ##################
	#
	<program $jbbs.BbsInfo>
		<dispatch  html=mkSeqUserInfo.html  number=3900     class=jbbs.BbsInfo />
		<variable>
		  <receive  NUMBER   STAMP  GROUP BBS_OWNER_KEY  UID MAIL UNAME DIVISION />
		  <accept   SUBMIT   UPLODE deleteFname />
		  <keep     editor />
		</variable>
	</program>
 *
 *
 *
 */
public class BbsInfo extends SuperPlayer implements BbsVar{
	
	DbConnectionBroker	brok;
	BbsConf				conf;
	//
	String				szDB;
	String				BbsOwnerkey;
	String				userid;
	String				userMail;
	String				userName;
	String				division;
	/**
	 * 
	 */
	public	BbsInfo(){
		super();
	}
	/**
	 * 
	 */
	public	void	initialize(PrintWriter out,Hashtable htb, Param para){
		//
		super.setInit(out,htb,para);
		
		/* システムプロパティからパスを得てjbbsライブラリ のコンフィギュレーション情報オブジェクトを作成する
		 * プラグインは各々固有のコンフィギュレーション情報オブジェクトを持つことができる．
		 * 設定ファイルは通常 システムコンフィギュレーションファイルと同じ場所に置くが、そこへの
		 * フルパスを適当なプロパティ名でシステムコンフィギュレーションファイル内に書いておく．
		 * jbbs パッケージでは BBS_CONF_PATH である．
		 * 
		 */
		conf			=	createBbsConf(para.get("BBS_CONF_PATH"),htb);
		this.brok		=	(DbConnectionBroker)htb.get(BROKER);
		this.division	=	Gear.strHash(htb, DIVISION);	// 分類区分

		this.szDB			=	strHash(htb,GROUP);
		this.BbsOwnerkey	=	strHash(htb,BBS_OWNER_KEY);
		this.userid			=	strHash(htb,UID);
		this.userMail		=	strHash(htb,MAIL);
		this.userName		=	strHash(htb,UNAME);

		if(LOG.fa){
			LOG.println("■ BbsInfo #initialize() : イニシャライザ");
			LOG.println("   szDB   = " + szDB);
			LOG.println("   userid = " + userid);
		}
	}
	/**
	 * 
	 */
	public BbsConf	createBbsConf(String BbsConfPath, Hashtable ht){
		/* 
		 * BBSコンフィギュレーションクラス 2004.80.15
		 */
		String	 serverIP	= strHash(ht,SERVER_IP);
		BbsConf  conf       = null;
		
		try{
			conf  = new BbsConf(BbsConfPath,serverIP);
		}catch(IOException e){
			errPrint(out,"初期化処理で設定ファイルが読めません.<p>");
		}
		return	conf;	
	}
	public	String	dispatch(){
		if(LOG.fa) LOG.outHash(htb,"class BbsInfo #dispatch()");
		//
		String 	menu	=	strHash(htb,DISPATCH_KEY);

		/* 次に起動するプログラムコードとして初期値は(DISPATCH_DEFAULT)自己再表示を入れておく */
		String	ret		=	DISPATCH_DEFAULT;	// WEB表示
		String	mode	=	DISP_EDIT;		// データを維持して表示
		/*
		 * VIEW は postMkUserInfo.html から起動される
		 * postMkUserInfo.html は top.html などこのクラスを起動したいクラスが呼び出す．
		 * パラメータを親Htmlから受け取り、menu に VIEW をいれたのち submit()するもの．
		 */
		if(menu.equals("VIEW")){
			/* 
			 * 別画面表示のとき
			 * 新規表示モードで画面表示
			 */
			mode	=	DISP_NEW;
			
		}else if( (menu.equals("HANDLE"))||(menu.equals("FORMAT"))||(menu.equals("SIG")) ){
			/*
			 * システムハッシュのデータでレコードを更新する
			 */
			htb.put(BbsInfoDB.DIVISION	, division);
			htb.put(BbsInfoDB.NAME		, userName);
			
			BbsInfoDB	bbsDB	=	new BbsInfoDB(userid, szDB, brok);
			bbsDB.updateBbsInfo(userid,htb);			
			mode				=	DISP_EDIT;		// データを維持して表示
			ret					=	DISPATCH_DEFAULT;	// WEB表示

			
		}else if(menu.equals("UPLOAD")){
			getFile();

		}else if(menu.equals("DELETE")){
			String	deleteFname	=	Gear.strHash(htb, "_deleteFname");
			File	fp			=	new File(conf.profilePath(userid, szDB) + deleteFname);
			fp.delete();

		}else if(menu.equals("RETURN")){
			/* 
			 * 呼び出し元プログラムへ戻る
			 */ 
			ret	=	DISPATCH_RETURN;

		}else{
			/* 
			 * menu には起動したいクラスキーが入っている
			 * 'RETURN' だと呼び出し元へ戻る
			 */
			ret	=	menu;
			if(LOG.fa) LOG.println("□ ret=" + ret);
		}
		/*
		 * 表示モードをハッシュに残しておく
		 */
		htb.put(DISP_KEY,mode);
		return	ret;
	}
	/** アップロードされたファイルをプロファイルディレクトリへ移す
	 * アップロードされた複数のファイルはシステムコントローラ（exwork.java）で一時ディレクトリに
	 * 保存され、その一時ディレクトリ名がシステムハッシュにParam.UPLOAD_DIR_KEYというキーで保存されている．
	 * ここではその複数のファイルを全てプロファイルディレクトリへ移動する
	 */
	void	getFile(){
		
		/* ex.  /home/group/[グループ名]/[ユーザーＩＤ]/profile/         */
		String	toDir		=	conf.profilePath(userid, szDB);
		
		/* ex.  /home/pc/temp/[ランダムに作成したディレクトリ名]        */
		String	fromDir		=	strHash(htb, UPLOAD_DIR_NAME);
		Gear.moveFiles(toDir, fromDir);

		/* 作業ディレクトリが残るので全て消去する */
		Gear.delDir(fromDir);
				
	}
	
	/** 初期値をハッシュにセットする */
	void	setInitialData(){
		htb.put("_userid"	,userid);
		
		/* ユーザー名は基本情報なので常にシステムハッシュの中にあるハズ */
		String	userName	=	Gear.strHash(htb,UNAME);
		if(isEmpty(userName)){
			htb.put("_name"		,"");
		}else{
			htb.put("_name"		,userName);
		}
		htb.put("_handle"		,"");
		htb.put("_iconfile"		,"");
		htb.put("_signature"	,"");
		htb.put("_formatStyle"	,para.formatTemplate());	// 初期値
		htb.put("_editor"		,"20");		
	}
	
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	//		 出　　力　　処　　理
	//
	/////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 表示処理
	 * レコードがない場合は新規にデフォルトデータを作成しデータベースに書き込む
	 * @param	表示モード
	 */
	public	void	display(boolean editmode){
		
		BbsInfoDB	bbsDB	=	new BbsInfoDB(userid, szDB, brok);
		if(!editmode){
			int	n	=	bbsDB.readBbsInfo(userid,htb);
			if(n==0){
				setInitialData();
				bbsDB.insertBbsInfo( userid, htb);
			}
		}
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v,htb);
	}
	//
	//
	// ハッシュテーブル(htb)を使って部分的に切り取ったソースデータ（exHtml）
	// の内容を置き換えて出力処理する．表などの反復出力に利用するが個々の処理
	// 内容は、key で特定される．
	//
	public void	write(String key,Vector exHtml,Hashtable htb){
		if(key.equals("uploadFileList")){
			/*
			 * HTML 生成のための各種テンプレート
			 */
			String		tempFile	=	conf.pmlTemplatePath();
			TemplateBox	tpb			=	new TemplateBox(tempFile);
			String		template	=	tpb.get("attach2");;
			
			String		filePath	=	conf.profilePath(userid,szDB);
			Fileset		fs			=	new Fileset(filePath);
			int		n			=	fs.n();
			StringEnumeration	e	=	fs.files();

			if(LOG.fa) LOG.println("□BbsInfo　#write:　tempFile=" + tempFile);
			if(LOG.fa) LOG.println("□BbsInfo　#write:　filePath=" + filePath);

			/*
			 * ファイルリストを表示する処理
			 */
			if(n==0){
				htb.put("_filecount", "0");
				return;
			}
			htb.put("_filecount", String.valueOf(n));	// blockWrite() で使う
			/*
			 * 全てのファイルを表示する
			 */
			Hashtable	tb	=	new	Hashtable(10);
			for(int i=0; i<n; i+=4){
				/*
				 * １行文の表示（最大４つのファイルを表示）
				 */
				for(int k=0; k<4; k++){
					String	uploadFile	=	e.nextItem();
					String 	fileUrl		=	conf.userProfileURL(userid, szDB) + uploadFile;

					String	rep	=	"&nbsp;";
					if(!Gear.isEmpty(uploadFile)){
						tb.put("_uploadFile", uploadFile);
						tb.put("_fileUrl"	, fileUrl);
						rep	=	Gear.replace(template, tb);
					}
					String	repkey	=	"_fileDiscriptor-" + String.valueOf(k+1);
					htb.put(repkey, rep);					
				}
				printVector(exHtml,htb);
			}
		}
	}
	// 部分的に切り取ったソースデータ（exHtml）を出力するか否か、ハッシュテーブル
	// (htb)を使って判断し、出力する場合には、htb を使ってexHtml の内容を書き換えて
	// 出力する．個々の処理内容は、key で特定される．
	//
	public void	blockWrite(String key,Vector exHtml,Hashtable htb){
		
		if(key.equals("isNoFiles")){
			//
			int	n	= Integer.parseInt( strHash(htb,"_filecount") );// 既に表示した行があるか
			if(n > 0)	return;
			//
			printVector(exHtml,htb);
			return;
		}		
	}

	
}
