//
package framework;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import kamoku.KamokuParser;
import tktools.Csv;
import tktools.FileGear;
import tktools.Property;
import tktools.StringGear;
import tktools.TemplateBox;
import xmlparser.xmlException;
//
public class Param extends Object {
    private boolean DEBUG = false;
	private boolean DEBUG_NEW = false;
    
	/** オリジナルシステムかどうかを判定する値 **/
	public	static String SYSTEM_ADMIN_ID	=	"mailman@mail-and-work.net";
	
	/** システムハッシュに格納するサーバIPアドレスのキー名 */
	public static final String	SERVER_IP 	= "_server_ip";
    
	/** システムハッシュに格納するブローカーオブジェクトのキー名 */
	public static final String	BROKER		= "broker";
		
	/**
	 * （マルチパートインプット）古いバージョンとの互換性維持
	 */
	public	static final String	UPLOAD_FILE_KEY	=	"_onlyFilename";
	public	static final String	UPLOAD_CNT_KEY	=	"_counts";

	/**
	 *  マルチパートインプットでハッシュに格納される情報のキー名
	 */	
	public	static final String	UPLOAD_DIR_KEY	=	"_savedir";

	/**
	 * 処理選択のための値をシステムハッシュから得るためのキー(DISPATCH_KEY)とその規定値(DISPATCH_DEFAULT)
	 * 規定値は画面（WEB）の再表示である
	 */
	public static final String	DISPATCH_KEY		=	"_menu";
	public static final String	DISPATCH_DEFAULT	=	"SELF";
	
	
	/**
	 * Web表示処理用の値(DISP_NEW,DISP_EDIT)とそれをシステムハッシュに保存する時のキー名(DISP_KEY)
	 */
	public static final String DISP_KEY	=	"display_mode";
	public static final String DISP_NEW	=	"NEW";	
	public static final String DISP_EDIT	=	"EDIT";

	
	//	
	
	String log;
	String	ViewSwitch;		// ライセンスキーの照合状態をログに残す
	//
    private Hashtable htmls = null; // html　ファイルのパスを受け取る
    //
    PrintWriter out;                                // HTML出力用
    //
	Property 	prop;
	//
	//
	String	sysURL;									// システムグラフィックスの起点URL
	String	emlConfPath;
	String	ex_wikiTemplatePath;
	//
	//String	init_szDB;
	String	init_urls;
	String	init_urlt;
	String	commonUsername;
	String	commonPasswd;
	String	initValue;
	String	initValue_s;
	//
    String tomcat_home;                             // サーブレットのホームディレクトリ
    String htmlPath;                                // 書き換えるHTMLファイルのパス
    //
	String ans_EXT;
	//
	String	userhome;								// user, group, bbs の親ディレクトリ
    String 	homedir;                                // 特定の個人のホーム 
    String	groupdir;
    String	bbsdir;
    //
	String KamokuDummyPath;                         // 講義ファイル作り忘れに対する予防
    //
	String defaultTemplatePath;                     // デフォルトの送信用テンプレート
    String defaultTemplatePath_k;
	String confMailTempPath;                        // 課題受け取り確認用メールのテンプレート 2002.6.
    String confMailTempPath_k;
    String upMailTempPath;                          // 課題掲示通知用テンプレート
    String upMailTempPath_k;
    //
	String stInfoTempPath;
    String stInfoTempPath_k;
	//
	String i_confRegPath;							// メールアドレス登録確認用テンプレート（e-mail)
	String k_confRegPath;							// メールアドレス登録確認用テンプレート（keitai-mail)
	String infoMailTempPath;                        // 期限の到来を知らせる情報メールのテンプレート
	//
    String server;									// サーバー名　(ex.   mail-and-work.net )
	String sysUser;                                 // システム管理者
    String sysMail;									// システム管理者メールアドレス
    //
    String mailadmin;                               // 
    String sysadmin;                                // 
    //
	//
    String mailmaster;								// メール管理者（mailadminのユーザー名）
	String mailmasterPass;                           // 同パスワード
    String mailmaster_k;                               // メール管理者名 popBeforeSmtp に使う 2003.6.7
    String mailmasterPass_k;                           // 同パスワード
	//
    String sysMailhost;
	String sysMailhost_k;
	String sysMailPort;
	String sysMailPort_k;
	
	String PopBeforSmtp;
	String PopBeforSmtp_k;
	//
	//String mailhost;                                // メールサーバ名（=sysMailhost)
    //String k_mailhost;								// 携帯送信用メールサーバー名(=sysMailhost_k)
    String servletURL;                              // exmail server へのURL
    //
    String logPath;                                 // ログを記録するパス
    //
    //String USER_URL;								// 公開URL ポストされたファイルの取得などに使う
	//
    String END_URL;									// 学生・教員共に終了時に表示するウェブのURL
	//
	String KAMOKU;                                  // クラスリストファイルの名前
    String KADAI ;                                  // 課題リストファイルの名前
    String ANSWERS;                                 // 解答ファイルの接頭辞
    String AnsTemp;									// 試験問題解答の一時保存ディレクトリ anstemp
    //
    String CLASS_TEMP;								// クラスファイルを登録するときの一時ディレクトリ
    String CLASS_MEIBO;                             // 講義クラスの名簿を置くディレクトリ名
    String MEIBO;                                   // 採点結果の名簿を出力するディレクトリ名
    String BACKUP;									// 
	String TEMPDIR;									// 提出ファイルの一時的な記録場所
	String POSTDIR;									// 課題ファイル提出用ディレクトリ名
	String ZIPDIR;									// 課題ファイルのZIP アーカイブを格納するディレクトリ名
	String KADAIDIR;                                // 課題のグラフィックスを保存するディレクトリ名
	String MAILTEMP;
	//
	String QDIR;	// アンケート集計結果ファイルの保存場所
	//
	String EPMLDIR;	// ユーザーの作成したウェブのEPMLデータ格納ディレクトリ
	String HTMLDIR;	// ユーザーの作成したウェブのHTMLデータ格納ディレクトリ
	
	String epml;	// EPMLのテンプレートファイル
	String hmac;	// HMACのテンプレートファイル
	
	//
	String STLOGIN;
	String STMENU_1;
	String STMENU_2;
	//
	String	typingLevel1;
	String	typingLevel2;
	
    //infoEx user; // オーナー名とそのディレクトリのリスト，名簿ファイル名など <--- user.conf をデータベース化したら user はCsv クラスに変更する
    //
    final String FS = File.separator;
    //
/*
    public Param(String binderConfPath) throws FileNotFoundException,IOException{
        //
        // 設定ファイルを読み込む
        info property     = new info(binderConfPath);
        //
		initialize(property);
    }
*/
	public Param(Property property)  {
		prop	 = property;
		out      = null;             // HTML出力用

		initialize();
	
	}
	/**
	 * システムプロパティの値をキーで引く
	 * @param key
	 * @return
	 */
	public	String	get(String key){
	    
	    return	prop.get(key);
	    
	}
	/**
	 * システムプロパティの値を変更・追加する
	 * @param key
	 * @param val
	 */
	public	synchronized	void	put(String key, String val){
	    
	    prop.put(key, val);
	}
	/**
	 * 初期化
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public void	initialize(){
	

        tomcat_home    			= prop.get("tomcat_home");              // サーブレットのホームディレクトリ
        htmlPath       			= prop.get("htmldir");     				// 書き換えに使用するHTMLファイルのあるディレクトリ
        emlConfPath				= prop.get("emlConfPath");				// eml ライブラリ用  /var/pc/conf/exwork.conf
		ex_wikiTemplatePath		= prop.get("ex_wikiTemplatePath");
		sysURL					= prop.get("sysURL");					//　ユーザー用にシステムのグラフィックスなどを格納したＵＲＬ
		//
        ViewSwitch				= prop.get("TEST");
		log						= prop.get("LOG");
		EPMLDIR					= prop.get("EPMLDIR");
		HTMLDIR					= prop.get("HTMLDIR");
		//
		//init_szDB				= prop.get("SHOZOKU_DB"); 		// 初期値のグループ名（インストールした大学ごとに設定）
		init_urls				= prop.get("STUDENT_URL"); 		// 初期値の学生用戻りURL（インストールした大学ごとに設定）
		init_urlt				= prop.get("TEACHER_URL"); 		// 初期値の教師用戻りURL（インストールした大学ごとに設定）
		commonUsername			= prop.get("commonUsername");	// ユーザー登録用ユーザー名
		commonPasswd			= prop.get("commonPasswd");		// ユーザー登録用パスワード
		initValue				= prop.get("initValue");		// ユーザー設定初期値
		initValue_s				= prop.get("initValue_s");		// 学生ユーザー設定初期値
		/*
		 * 基本ディレクトリ
		 * homedir             = %userhome%user\
		 * groupdir            = %userhome%group\
		 * bbsdir              = %userhome%bbs\
		 */
		homedir		=	prop.get("homedir");			// ユーザーホームディレクトリの起点  /home/pc/
		groupdir	=	prop.get("groupdir");
		bbsdir		=	prop.get("bbsdir");
		//
		ans_EXT					= prop.get("ans_EXT");			// 解答ファイルの拡張子
		//
		epml					= prop.get("epmlTemplatePath");
		hmac					= prop.get("wikiTemplatePath");
		//
		defaultTemplatePath   	= prop.get("TEMPLATE");          	// デフォルトの学生への採点結果送信用テンプレートファイル
        defaultTemplatePath_k 	= prop.get("TEMPLATE_K");
        confMailTempPath    	= prop.get("CONFMAILTEMPLATE");  	// デフォルトの学生への受け取り確認送信用テンプレートファイル
        confMailTempPath_k    	= prop.get("CONFMAILTEMPLATE_K");
        upMailTempPath      	= prop.get("UP_MAIL");           	// 期限の到来を知らせる情報メールのテンプレート
		upMailTempPath_k      	= prop.get("UP_MAIL_K");
		//
		stInfoTempPath			= prop.get("stInfo");
		stInfoTempPath_k		= prop.get("stInfo_k");
		///
		infoMailTempPath    	= prop.get("INFO_MAIL");         	// 期限の到来を知らせる情報メールのテンプレート
		//
		i_confRegPath			= prop.get("i_confRegPath");
		k_confRegPath			= prop.get("k_confRegPath");
		//
		//****************
		//   システム情報
		//****************
		server		  			= prop.get("SERVER");               // FQDNサーバー名またはIPアドレス
        sysUser       			= prop.get("SYS_USER");             // システム管理名
        sysMail       			= prop.get("SYS_MAIL");             // システム管理者メールアドレス
		sysMailhost   			= prop.get("MAIL_HOST");			// システムのメールサーバー
		sysMailPort				= prop.get("mail_port");
        
		sysadmin	  			= sysMail;
        mailadmin     			= sysadmin;                  		// exmailアドミンメールアドレス
		//
		//  一般メール用と携帯メール用
		//
		mailmaster       		= prop.get("mailmaster");				//  
		mailmasterPass   		= prop.get("mailmasterPass");			//  

		PopBeforSmtp     		= prop.get("PopBeforSmtp");			//  
		//
        sysMailhost_k       	= prop.get("MAIL_HOST_K");			// システムの携帯メールサーバー
		sysMailPort_k			= prop.get("mail_port_k");
		mailmaster_k   			= prop.get("mailmaster_k");			//  
		mailmasterPass_k  		= prop.get("mailmasterPass_k");		//  

		PopBeforSmtp_k   		= prop.get("PopBeforSmtp_k");		//  
		//
		//
		servletURL  			= prop.get("SERVLET_URL");                   // exmail server へのURL

        END_URL					= prop.get("END_URL");						 //
		//
        logPath     			= prop.get("logPath");           			// ログファイルパス
        //
        KAMOKU      			= prop.get("KAMOKU");                        // 講義ファイル名
        KADAI       			= prop.get("KADAI");                         // 課題ファイル名
        //
		//USER_URL				= prop.get("USER_URL");				//  "http://mail-and-work.net/user/"  公開URL（ "/home/exwk" をマップしている）
		//
		ANSWERS     			= prop.get("ANSWERS");              // 解答保存ディレクトリ名 (getUserPath(String man) を付加して使用する)
		AnsTemp					= prop.get("AnsTemp");
        CLASS_TEMP  			= prop.get("CLASS_MEIBO_TEMP");     // 講義クラスの名簿を一時的に置くディレクトリ名
        CLASS_MEIBO 			= prop.get("CLASS_MEIBO");          // 講義クラスの名簿を置くディレクトリ名(getUserPath(String man) を付加して使用する)
        MEIBO       			= prop.get("MEIBO");                // 採点結果の名簿を出力するディレクトリ名(getUserPath(String man) を付加して使用する)
        BACKUP      			= prop.get("BACKUP");				// バックアップ用
        KADAIDIR				= prop.get("KADAIDIR");				// 課題に表示するグラフィックスを置くディレクトリ
        //
		POSTDIR					= prop.get("POSTDIR");				// ポスト用ディレクトリ名        "file"
		ZIPDIR					= prop.get("ZIPDIR");				// ZIP アーカイブディレクトリ名  "zip"
		//
        TEMPDIR					= prop.get("TEMPDIR");
		//
		MAILTEMP				= prop.get("MAILTEMP");			// 電子メールの差込ファイル用ディレクトリ名 "mailtemp"
		//
		QDIR					= prop.get("QDIR");
		//
		typingLevel1			=	prop.get("typingLevel1");
		typingLevel2			=	prop.get("typingLevel2");
    }
	/**
	 * タイプ練習（入力）の評価基準を返す
	 * Html の javaScript に埋め込むための文字列
	 * @return
	 */
	public	String	tLevel1(){
	    
	    StringBuffer	bf		=	new	StringBuffer();
	    Csv				cs		=	new	Csv(typingLevel1,",");
	    int				n		=	cs.size();
	    for(int i=0; i<n; i++){
	        bf.append(cs.get(i));
            bf.append("\\n");
	    }
	    return	bf.toString();
	}
	/**
	 * タイプ練習（ミカタイプ）の評価基準を返す
	 * Html の javaScript に埋め込むための文字列
	 * 
	 * @return
	 */
	public	String	tLevel2(){
	    
	    StringBuffer	bf		=	new	StringBuffer();
	    Csv				cs		=	new	Csv(typingLevel2,",");
	    int				n		=	cs.size();
	    for(int i=0; i<n; i++){
	        bf.append(cs.get(i));
            bf.append("\\n");
	    }
	    return	bf.toString();
	}
	/**
	 * 
	 * @return
	 */
	public	boolean	isLog(){
		
		if(log.equals("YES")||log.equals("yes"))	return true;
		return	false;
	}
	/**
	 *  pc.conf に指定したドメイン名を返す
	 * @return
	 */
	public	String	getDomainName(){
	    
	    return	prop.get("domain");
	}
	
	/**
	 * サーバーIPアドレスから作成した固定のグループ名を返す
	 * @return
	 */
	public	String	getDomainGroupName(){
	    String	ip	=	serverIP();
	    return	"group_" + ip.replace('.','_');
	}
	/**
	 * pc.conf に指定したサーバIPアドレスを返す
	 * @return
	 */
	public	String	serverIP(){
	    return	prop.get("serverIP");
	}
	/**
	 * システムコンフィギュレーションファイル(pc.conf)のフルパスを返す
	 */
	public	String	sysConfFile(){
	    return	prop.get("SYS_CONF_PATH");
	}
	/**
	 * 基本ディレクトリへのパス
	 * @return
	 */
    public	String	getHomedir()	{	return	homedir;	}
    public	String	getGroupdir()	{	return	groupdir;	}
    public	String	getBbsdir()		{	return	bbsdir;		}
    
    /*
     * タイトルロゴ
     */
    public	String	getTitleLogo()	{	return	prop.get("titleLogo"); }
    public	String	getLogodir()	{	return	prop.get("logoDir"); }
    
    
	public	String	getViewSwitch()	{ return ViewSwitch; }
	//
    //**********************************************************************************************
	// インストール大学で設定した初期値
    //**********************************************************************************************
	public String  getServer()					{return server;}			// サーバー名
	public String	getSystemAdmin()			{return sysUser;}
	public String	getSystemAdminMail()		{return sysMail;}
	public String  getMailServer()				{return sysMailhost;}		// メールサーバー名
    //**********************************************************************************************
		
	/**
	 * BBS ADMIN のメールアドレスを返す
	 * @return
	 */
	public	String	bbsAdminMail(){
	    String	mail	=	prop.get("bbsAdminMail");
	    return	mail;
	}
	
	
	/**
	 * エディタで使うフォーマットデータの初期値を返す
	 */
	public	String	formatTemplate(){
	    String		path	=	prop.get("FORMAT_TEMPLATE");
	    TemplateBox	tbx		=	new	TemplateBox(path);
	    String		fmt		=	tbx.get("format");
	    return		fmt;
	}
	
    /**
     * シラバス定義xmlデータへのパス
     */
    public	String	syllabusPath(){
    	return		prop.get("SYLLABUS");
    }
    /**
     * グループのシラバス定義xmlデータへのパス
     * /var/pc/data/_szDB.xml
     * @param szDB
     * @return
     */
    
    public	String	groupSyllbusPath(String szDB){
    	String	path	=	prop.get("GROUP_SYLLABUS");	// C:\Tomcat\webapps\pc\system\data\_szDB.xml
    	return		StringGear.substitute(path, "_szDB", szDB);
    }
    
    
    
    /**
     * 個人のシラバス定義xmlデータへのパス
     * 
     * @param uid
     * @return
     */
    public	String	userSyllbusPath(String szDB, String uid){
    	String	path	=	prop.get("USER_SYLLABUS");
    	String	temp	=	StringGear.substitute(path, "_szDB", szDB);
    	return	StringGear.substitute(temp, "_userid", uid);
    }
    /**
     * シラバス定義XMLからhtmlを作成するときの雛形データファイルへのパス
     * @return
     */
    public	String	kamokuTemplatePath(){
    	return		prop.get("KAMOKU_TEMPLATE");
    }
    /**
     * ユーザー定義とグループ定義で大きい方の serial を返す．<br>
     * xmlにエラーがあると例外を発生する
     * 
     * @param szDB	グループ名
     * @param uid	教師ID
     * @return		現在のserial番号
     * @throws xmlException
     */
    public	String	getKamokuSerial(String szDB, String uid) throws xmlException {
    	
    	String			groupFile	=	groupSyllbusPath(szDB);	// グループファイル
		KamokuParser	group		=	new KamokuParser( FileGear.getFileData(groupFile) );

		String			userFile	=	userSyllbusPath(szDB, uid);	// ユーザーファイル
		if(!FileGear.isExistFile(userFile)){
			return	group.getSerial();
		
		}else{
			KamokuParser	user	=	new KamokuParser( FileGear.getFileData(userFile)  );
			int				com		=	(group.getSerial()).compareTo(user.getSerial());
			if(com >= 0){
				return	group.getSerial();
			}else{
				return	user.getSerial();
			}
		} 

    }
    /**
     * ユーザー定義とグループ定義で serial の大きい方のXMLテキストを返す．<br>
     * xmlにエラーがあると例外を発生する
     * 
     * @param szDB	グループ名
     * @param uid	教師ID
     * @return		現在のserial番号
     * @throws xmlException
     */
    public	String	getKamokuXml(String szDB, String uid) throws xmlException {
		
    	String			groupFile	=	groupSyllbusPath(szDB);	// グループファイル
		String			groupXml	=	FileGear.getFileData(groupFile);
		KamokuParser	group		=	new KamokuParser( groupXml );

		String			userFile	=	userSyllbusPath(szDB, uid);	// ユーザーファイル
		
		if(!FileGear.isExistFile(userFile)){
			return	groupXml;
		
		}else{
			String			userXML	=	FileGear.getFileData(userFile);
			KamokuParser	user	=	new KamokuParser( userXML );
			int				com		=	(group.getSerial()).compareTo(user.getSerial());
			if(com >= 0){
				return	groupXml;
			}else{
				return	userXML;
			}
		}
		
    	
    }    
    /**
     * ユーザー定義とグループ定義で serial の大きい方の科目パーサを返す．<br>
     * xmlにエラーがあると例外を発生する
     * 
     * @param szDB	グループ名
     * @param uid	教師ID
     * @return		現在のserial番号
     * @throws xmlException
     */

    public	KamokuParser	getKamokuParser(String szDB, String uid) throws xmlException {
		String			groupFile	=	groupSyllbusPath(szDB);	// グループファイル
		KamokuParser	group		=	new KamokuParser( FileGear.getFileData(groupFile) );

		String			userFile	=	userSyllbusPath(szDB, uid);	// ユーザーファイル
		if(!FileGear.isExistFile(userFile)){
			return	group;
		
		}else{
			KamokuParser	user	=	new KamokuParser( FileGear.getFileData(userFile)  );
			int				com		=	(group.getSerial()).compareTo(user.getSerial());
			if(com >= 0){
				return	group;
			}else{
				return	user;
			}
		}    	
    } 

    /**
     * ユーザーごとのファイルキャビネットへのパス
     * 
     * 　/home/group/_szDB/_userid/filecabinet/
     * 
     * @param szDB
     * @param uid
     * @return
     */
    public	String	fileCabinetPath(String szDB, String uid){
    	
    	String	path	=	prop.get("FILE_CABINET");
    	String	temp	=	StringGear.substitute(path, "_szDB", szDB);
    	return	StringGear.substitute(temp, "_userid", uid);
    }
    /**
     * ユーザーごとのファイルキャビネットへのURL
     * 
     * 　/web/_szDB/_userid/filecabinet/
     * 
     * @param szDB
     * @param uid
     * @return
     */
    public	String	fileCabinetURL(String szDB, String uid){
    	
    	String	path	=	prop.get("FILE_CABINET_URL");
    	String	temp	=	StringGear.substitute(path, "_szDB", szDB);
    	return	StringGear.substitute(temp, "_userid", uid);
    }    
    public	Property	getProperty(){
    	return	prop;
    }
	//
	public	String	getEmlConfPath()			{return	  emlConfPath;}			// eml ライブラリ用設定ファイルへのパス
	public	String  get_ex_wikiTemplatePath()	{ return ex_wikiTemplatePath; }	// エクスポート用 hmac ファイルへのフルパス
	//
	public	String	getSYSURL(){	/// ユーザー用にシステムのグラフィックスなどを格納したＵＲＬ
		return	sysURL;
	}

	
	public	String	getInitDB()					{return prop.get("SHOZOKU_DB");}			// データベース名（グループ名）
	
	
	public	String	getInitTeacherURL()			{return init_urlt;}			// 教師の戻りURL
	public	String	getInitStudentURL()			{return init_urls;}			// 学生の戻りURL
	
	public	String	getCommonUsername()			{return commonUsername;}	// ユーザー登録のための共通ユーザー名
	public	String	getCommonPasswd()			{return commonPasswd;}		// ユーザー登録のための共通パスワード
	public	String	getInitValue()				{return initValue;}			// ユーザー設定初期値
	public	String	getStudentInitValue()		{return initValue_s;}		// 学生ユーザー設定初期値
	//
	// 試験問題生成用 htmlテンプレートファイルへのパス(2004.1)
	public	String	epml()	{ return epml; }
	public	String	hmac()	{ return hmac; }
	
	
	public String getNoHomepage()				 { return "http://mail-and-work.net";} // シラバスURLがないときの代替
	//
	public String ansEXT()						 { return ans_EXT; } 				// 解答ファイルの拡張子
	
	
    public String getTomcat_home()               { return tomcat_home;}
    public String getHtmlPath()                  { return htmlPath; }
    //
	public String GetKamokuDummyPath()           { return KamokuDummyPath; }
    //
    public String getTempDir()         		 	  { return homedir + TEMPDIR + FS;}	// 一時ファイル用ディレクトリを作成する場所
	//
	public String GetDefaultTemplatePath()       { return defaultTemplatePath; }
    public String GetConfMailTempPath()          { return confMailTempPath; }
    public String GetUpMailTempPath()            { return upMailTempPath; }
	//
	public String GetDefaultTemplatePath_k()     { return defaultTemplatePath_k; }	// 携帯用送信テンプレート
    public String GetConfMailTempPath_k()        { return confMailTempPath_k; }
    public String GetUpMailTempPath_k()          { return upMailTempPath_k; }
	//
	public String stInfoTempPath()				 { return stInfoTempPath;}
	public String stInfoTempPath_k()			 { return stInfoTempPath_k;}
	//
    public String GetInfoMailTempPath()          { return infoMailTempPath; }
    //
    public String i_confRegPath()        		 { return i_confRegPath; }
	public String k_confRegPath()        		 { return k_confRegPath; }
	//
    public String getSysadmin()                  { return sysadmin; }   // システム設置責任者メールアドレス
    public String getMailadmin()				 { return mailadmin;}	// eXmail の管理者メールアドレス
	//***********************************
	public String popBeforSmtp() 			 	 { return PopBeforSmtp;} 
	public String popBeforSmtp_k() 			 	 { return PopBeforSmtp_k;} 
	public String getMailmaster()				 { return mailmaster; }			// mailman
    public String getMailmasterPass()			 { return mailmasterPass; }		// 13mailman28
    public String getMailmaster_k()				 { return mailmaster_k; }			// mailman
    public String getMailmasterPass_k()			 { return mailmasterPass_k; }		// 13mailman28
	//***********************************
	public String sysMailhost()                  { return sysMailhost; }
	public String sysMailhost_keitai()           { return sysMailhost_k; }
	public int	  sysMailPort()					  {return getMailPort() ;}
	//
    public String getMailhost()                  { return sysMailhost; }
    public int getMailPort()                  	  { return Integer.valueOf(sysMailPort); }
    
    public String getMailhostToKeitai()          { return sysMailhost_k; }
    public int getMailPortToKeitai()             { return Integer.valueOf(sysMailPort); }

    public String getServletUrl()                { return servletURL; } // eXmail server へのURL
    public String getSysUser()                   { return sysUser; }    // eXmail server のシステムユーザー名
    //
    public String getLogPath()                   { return logPath; }
    //
    public String getKamokuFileName()            { return KAMOKU;}
    public String getKadaiFileName()             { return KADAI;}
    //
  
	/*
	 *  /home/pc/(teUid)/
	 */
	public String getUserPath(String man)		 { return 	homedir + man + FS;}
	public String getAnsDirName()      			 { return  ANSWERS + FS;}		// 解答ディレクトリ名（上位ディレクトリ含まず））
	public String getAnsTempDirName()			 { return  AnsTemp + FS;}
    public String getTempClassDirName()			 { return  CLASS_TEMP + FS;}	// クラス一時ディレクトリ名（上位ディレクトリ含まず）
    public String getClassDirName()    			 { return  CLASS_MEIBO + FS;}	// クラス名簿ディレクトリ名（上位ディレクトリ含まず）
    public String getMeiboDirName()    			 { return  MEIBO + FS;}			// 採点結果ディレクトリ名（上位ディレクトリ含まず）
    public String getBackupDirName()    		 { return  BACKUP + FS;}		// バックアップディレクトリ
    public String getMailTempDirName()     		 { return  MAILTEMP + FS;}		// 差込ファイル用   例：/home/kawaba01/mailtemp/
	public String getQDirName()   		 		 { return  QDIR + FS;}			// アンケート集計結果用   例：/home/kawaba01/qdir/
	public String getKadaiDirName()				 { return  KADAIDIR + FS;}		// 課題に表示するグラフィックスを置くディレクトリ
																				//    例：/usr/kawaba01/kadai/
	//
	public String getAnsDir(String man)          	{ return getUserPath(man) + ANSWERS + FS;}
	public String getAnsTempDir(String man)	  		{ return getUserPath(man) + AnsTemp + FS;}
	//
    public String getTempClassDir(String man)    	{ return getUserPath(man) + CLASS_TEMP + FS;}	//例：/home/kawaba01/temp/
    public String getMeiboDir(String man)        	{ return getUserPath(man) + MEIBO + FS;}
    public String getBackupDir(String man)       	{ return getUserPath(man) + BACKUP + FS;}
	//
    public String getMailTempDir(String man,String aplec_key)     { return getUserPath(man) + MAILTEMP + FS + aplec_key + FS;}		/// 差込ファイル用   例：/home/kawaba01/mailtemp/112/
    /* 
	   ---------------------------------------------------------------------
		ディレクトリ削除のための、課題実施キーまでのディレクトリパスを返す
	   --------------------------------------------------------------------- 
	*/
	public String filePost(String teUid,String aplec_key)		{ return  getUserPath(teUid) + POSTDIR     + FS + aplec_key;  }	// ファイル提出
	public String classMeibo(String teUid,String aplec_key)		{ return  getUserPath(teUid) + CLASS_MEIBO + FS + aplec_key;  }	// 名簿
	public String answers(String teUid,String aplec_key)		{ return  getUserPath(teUid) + ANSWERS     + FS + aplec_key;  }	// 課題解答
	public String ansTemp(String teUid,String aplec_key)		{ return  getUserPath(teUid) + AnsTemp		+ FS + aplec_key;  }	// 試験問題一時保存
	public String sashikomi(String teUid,String aplec_key)		{ return  getUserPath(teUid) + MAILTEMP    + FS + aplec_key;  }	// 差し込みファイル
	public String zip(String teUid,String aplec_key)			{ return  getUserPath(teUid) + ZIPDIR      + FS + aplec_key;  }	// ＺＩＰファイル
	//
	// ---------  名簿ディレクトリ---------------------------------------------------------------------------------------------------
    public String getClassMeiboDir(String teUid,String aplec_key)
	          { return getUserPath(teUid) + CLASS_MEIBO + FS + aplec_key + FS ;}	// あるクラスの名簿ファイル用ディレクトリ
	// ----------------------------------------------------------------------------------------------------------------------------------------------
	//
	// エクスポート用作業ディレクトリ
	public String exportDir(String teUid)		{ return  getUserPath(teUid) + "export" + FS;  }
	public String importDir(String teUid)		{ return  getUserPath(teUid) + "import" + FS;  }
	public String importDirName(String teUid)	{ return  getUserPath(teUid) + "import";  		} 


	//
	// ---------  特定の実施講義の解答ディレクトリ---------------------------------------------------------------------------------------------------
	public String kadaiAnsDir(String man,String aplec_key,String kadai_key) 
			 { return getUserPath(man) + ANSWERS + FS + aplec_key + FS + kadai_key;}	// 解答ファイル用   例：/home/kawaba01/answer/112/000013

	public String kadaiTempAnsDir(String man,String aplec_key,String kadai_key) 
			 { return getAnsTempDir(man) + aplec_key + FS + kadai_key;}	// 解答一時ファイル用   例：/home/kawaba01/anstemp/112/000013
	// ----------------------------------------------------------------------------------------------------------------------------------------------
		
    // 特定の学生の特定の課題についての解答ファイルへのフルパスを得る
    public String stFilePath(String path,String stNumber){   return path  + FS  +  stNumber + ansEXT();   }
    public String stFilePath(String man,String aplec_key,String kadai_key,String stNumber){ return kadaiAnsDir(man,aplec_key,kadai_key)  + FS + stNumber + ansEXT(); }
 	
    //
	// 
	// ---------  ファイルポストディレクトリ---------------------------------------------------------------------------------------------------
    public String getKadaiPostDir(String man,String aplec_key,String kadai_key)         
	          { return getUserPath(man) + POSTDIR + FS + aplec_key + FS + kadai_key + FS;}	// ある課題のファイルポスト用ディレクトリ
	
    public String getKadaiPostDir2(String man,String aplec_key,String kadai_key) 
		      { return getUserPath(man) + POSTDIR + FS + aplec_key + FS + kadai_key;}		// ある課題のファイルポスト用ディレクトリ
	
	public String getPostDir(String man) 
	          { return getUserPath(man) + POSTDIR + FS;}	// ファイルポスト用ディレクトリ	
	// ----------------------------------------------------------------------------------------------------------------------------------------------

	/**
	 * ファイル提出用URL（学生データ）
	 */
	/*   /user/(teUid)/ を返す */
	public String getUserURL(String man)		 { return "/user/" + man + "/"; }
    // "/user/(teUid)/file/"
	public String getUserFileURL(String man)	 { return getUserURL(man) + POSTDIR + "/"; } 
	// "/user/(teUid)/file/(aplec_key)/(kadai_key)/"
	public String getKadaiPostURL(String man,String aplec_key,String kadai_key){
		return  getUserFileURL(man) + aplec_key + "/" + kadai_key + "/";
	}
	
	/**
	 * 課題添付ファイル
	 * 　学生が課題レポートに添付するデータファイルを置く場所
	 * @return
	 */
	public	String	kadaiAttachDir(String man,String aplec_key,String kadai_key,String stNumber){
		{ return  getKadaiAttachDIR( man, aplec_key, kadai_key) + stNumber + FS;}
		
	}
	public String getKadaiAttachDIR(String man,String aplec_key,String kadai_key){
		return  getKadaiAttachDIR(man) + aplec_key + FS + kadai_key + FS;
	}
	public String getKadaiAttachDIR(String man)	 { return getUserPath(man) + "attach" + FS; } 
	
	//	"/user/(teUid)/file/(aplec_key)/(kadai_key)/(stNumber)/"
	public	String	kadaiAttachUrl(String man,String aplec_key,String kadai_key,String stNumber){
		{ return  getKadaiAttachURL( man, aplec_key, kadai_key) + stNumber + "/";}
		
	}	
	public String getKadaiAttachURL(String man,String aplec_key,String kadai_key){
		return  getKadaiAttachURL(man) + aplec_key + "/" + kadai_key + "/";
	}
	public String getKadaiAttachURL(String man)	 { return getUserURL(man) + "attach" + "/"; } 
	
	
	/**
	 * 科目単位での絶対パス
	 * zip圧縮用
	 */
	public String getKamokuAttachPathName(String man,String lec_key)	 { 
		return getUserPath(man) + "kadai" + FS + lec_key; 
	}
	/**	課題ディレクトリ名 */
	public String getKadaiDirName(String man)	 { 
		return getUserPath(man) + "kadai"; 
	} 	
	/**
	 * kadai作成でグラフィックスデータを置く場所（教師用）
	 * @param man
	 * @return
	 */
	public String getAttachDIR(String man)	 { return getUserPath(man) + "kadai" + FS; } 
	public String getAttachDIR(String man,String lec_key, String kadai_key){
		return  getAttachDIR(man) + lec_key + FS + kadai_key + FS;
	}
	
	public String getAttachURL(String man,String lec_key, String kadai_key){
		return  getAttachURL(man) + lec_key + "/" + kadai_key + "/";
	}
	public String getAttachURL(String man)	 { return getUserURL(man) + "kadai" + "/"; } 
	
	/**
	 * 教師別ダウンロードエリアへの絶対パス
	 * @param man
	 * @return
	 */
	public	String	getDownloadDir(String man) 		{return getUserPath(man) + "download"  + FS; }
	public	String	getDownloadDirName(String man) 	{return getUserPath(man) + "download"; }
	public	String	getDownloadURL(String man) 		{return getUserURL(man) + "download/"; }
	/**
	 * HTMLテンプレートファイル名
	 * PMLエディタでアップロードしたファイルリストを表示するために使うテンプレート
	 */
	public	String	htmlTemplate(){
		String	tempFileName       = prop.get("HTML_TEMPLATE");
		return	tempFileName;

	}
	/**
	 * アンケート集計結果ファイル（kadai_key.csv) の書き込みディレクトリ
	 * @param man
	 * @param aplec_key
	 * @return
	 */
	public	String	getQPath(String man,String aplec_key){
		return getUserPath(man) + QDIR + FS + aplec_key + FS;	
	}
	/**
	 * HTML からアクセスするURLを得る（本家専用）
     * アクセスする場所が内部でも外部でも、サーバーIPアドレスで返せばアクセスできる
	 * "http://(IP address)/user/kawaba01/"
	 * 
	 * @param htb
	 * @param man
	 * @return
	 */
	public String getUserURL(Hashtable htb,String man){
		String	server_ip 	= (String)htb.get("_server_ip");
		return	"http://" + server_ip + "/" + "user" + "/" + man + "/";
	}
	public String getUserFileURL(Hashtable htb,String man)	 {
	    return getUserURL(htb,man) + POSTDIR + "/"; 
	} 
	// "http://www.exbinder.com/user/kawaba01/file/103/000013/"
	public String getKadaiPostURL(Hashtable htb,String man,String aplec_key,String kadai_key)  {
	    return  getUserFileURL(htb,man) + aplec_key + "/" + kadai_key + "/";
	}	
	/**
	 * 教材ウェブを作成する時、その雛形となるテンプレートファイルのフルパスを得る<br>
	 * 通常は "/var/text/webTemplate.txt" が得られる
	 * ファイルはWindows-31Jである．
	 * 
	 * @return
	 */
	public	String	getWebTemplateFile(){
		return	prop.get("webTemplateFile");
	}
	
	/**
	 * 教材ウェブ(HTML)ファイル名をそのHTMLファイルのURLから得る
	 * ファイル名はURLの変更が可能なため一意に定めることができないので、
	 * 逆にURLから得ることにした．
	 * ただし、URLが空の時は規定のファイル名を返す
	 * 
	 * @param 		url			HTMLファイルURL
	 * @param		ref_key		資料キー		
	 * @return					HTMLファイルの名前 
	 */
	public	String getHtmlFilename(String url, String ref_key){
		if(StringGear.isEmpty(url)){
			return	"ht" + ref_key + ".html";
			
		}else{
			String	htmlName	=	FileGear.getFileNameBody(url) + ".html";
			return	htmlName;
		}
	}
	/**
	 * 教材ウェブ(PML)ファイル名をそのHTMLファイルのURLから得る
	 * ファイル名はURLの変更が可能なため一意に定めることができないので、
	 * 逆にURLから得ることにした．
	 * ただし、URLが空の時は規定のファイル名を返す
	 * 
	 * @param 		url			HTMLファイルURL
	 * @param		ref_key		資料キー		
	 * @return					PMLファイルの名前 
	 */	
	public	String getPmlFilename(String url, String ref_key){
		if(StringGear.isEmpty(url)){
			return	"ep" + ref_key + ".epml";
			
		}else{
			String	pmlName		=	FileGear.getFileNameBody(url) + ".epml";
			return	pmlName;
		}
	}
	/**
	 * 新規作成した教材ウェブファイルのURL
	 * 例示：　/user/<UID>/html/[lec_key]/ht[ref_key].html
	 * 
	 * @param man
	 * @param lec_key
	 * @param ref_key
	 * @return
	 */
	public	String getUrl(String man,String lec_key,String ref_key){
		return	getUserURL(man) + HTMLDIR + "/" + lec_key + "/" + getHtmlFilename("",ref_key);
	}
	/**
	 * 新規作成した教材ウェブファイルのファイル名を含まないURL
	 * 例示：　/user/<UID>/html/[lec_key]/
	 * 
	 * @param man
	 * @param lec_key
	 * @param ref_key
	 * @return
	 */
	public	String getParentUrl(String man,String lec_key,String ref_key){
		return	getUserURL(man) + HTMLDIR + "/" + lec_key + "/";
	}		
	/**
	 * 教材ウェブを作成するディレクトリへのパス
	 * @param man
	 * @param lec_key
	 * @return
	 */
	public String getHtmlPath(String man,String lec_key){
		return getHtmlDir(man) + lec_key + FS ;	//   /home/pc/<UID>/html/<lec_key>/
	}
	public String getHtmlPathName(String man,String lec_key){
		return getHtmlDir(man) + lec_key ;	//   /home/pc/<UID>/html/<lec_key>
	}
	/**
	 * 教材ウェブのPMLデータを保存するディレクトリへのパス 
	 * @param man
	 * @param lec_key
	 * @return
	 */
	public String getEpmlPath(String man,String lec_key){
		return getEpmlDir(man) + lec_key + FS ;	//   /home/pc/<UID>/epml/<lec_key>/
	}
	public String getEpmlPathName(String man,String lec_key){
		return getEpmlDir(man) + lec_key ;	//   /home/pc/<UID>/epml/<lec_key>
	}	
	/**
	 * 教材ウェブプレビューで files/ の代わりに画像ファイルの所在を示すために使う
	 * @param man
	 * @param lec_key
	 * @return
	 */
	public String getAliasUrl(String man,String lec_key){
		return "/user/" + man + "/html/" + lec_key + "/" + "files/";	//   /user/<UID>/html/<lec_key>/files/
	}	
    //
	//  getZipDir("kawaba01")  ⇒　/home/pc/kawaba01/zip/
	//　getZipDir2("kawaba01") ⇒　/home/pc/kawaba01/zip
	//
	public String getZipDir(String man)          { return getUserPath(man) + ZIPDIR + FS;}	// ファイルポスト用ディレクトリ
    public String getZipDir2(String man)         { return getUserPath(man) + ZIPDIR;}		// ファイルポスト用ディレクトリ
	//
	public String getZipDir3(String man)         { return getUserPath(man) + "zipTemp";}	// zip 作成用ディレクトリ（使用後削除）
	
	//  この直下に kadaiKey 名で作られるZIP用ディレクトリを返す
	//　getKadaiPostDir("kawaba01","ck-0017") ⇒ /home/pc/kawaba01/zip/103/000013/
	//
	public String getKadaiZipDir(String man,String aplec_key,String kadai_key)   { return getZipDir(man) + aplec_key + FS + kadai_key + FS;}
	public String getKadaiZipDir2(String man,String aplec_key,String kadai_key)  { return getZipDir(man) + aplec_key + FS + kadai_key;}
	//
	// html作成用
	public String getHtmlDir(String man)          { return getUserPath(man) + HTMLDIR + FS;}		// /home/pc/<UID>/ + html + /
	public String getHtmlDirName(String man)      { return getUserPath(man) + HTMLDIR;}			// /home/pc/<UID>/ + html 
	public String getEmlDir(String man)          	{ return getUserPath(man) + EPMLDIR + FS;}		// /home/pc/<UID>/ + epml + /
	public String getEmlDirName(String man)      	{ return getUserPath(man) + EPMLDIR;}			// /home/pc/<UID>/ + epml

	public String getEpmlDir(String man)          { return getUserPath(man) + EPMLDIR + FS;}		// /home/pc/<UID>/ + epml + /
	public String getEpmlDirName(String man)      { return getUserPath(man) + EPMLDIR;}			// /home/pc/<UID>/ + epml
	
	///
	/// 以下を実際には使う
	public String getUploadPath(String man,String lec_key){
		return getHtmlDir(man) + lec_key + FS + "files" + FS;	//   /home/pc/<UID>/html/<lec_key>/files/
	}
	
    public String getUserKamokuPath(String man)  { return getUserPath(man) + KAMOKU; }// クラスファイルへのフルパス
    public String getUserKadaiPath(String man)   { return getUserPath(man) + KADAI; } // 課題ファイルへのフルパス
    //
    public void addResponseWriter(PrintWriter o) { out = o; }
    public PrintWriter getResponseWriter()       { return out; }
    //
}






