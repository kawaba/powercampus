
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import database.Database;
import database.DbConnectionBroker;
import framework.Cp932;
import framework.Dispatch;
import framework.DispatchStack;
import framework.LOG;
import framework.Moonwalker;
import framework.PCException;
import framework.PCvar;
import framework.Param;
import framework.SuperPlayer;
import multpart.MultipartRequest;
import student.Student;
import tktools.DateGear;
import tktools.Gear;
import tktools.Property;
import tktools.tkException;

/**
 * 学生用フレームワーク
 *
 */

public class PowerCampusST extends HttpServlet  implements PCvar {

    /** データベース接続プーリングクラス */
    DbConnectionBroker 	myBroker;	// 2003.3

    /** デフォルトデータベース */
    private Database 	db;
    
    /** ディスパッチリストデータ */
	public	String		dispatchData;
	
    /** ディスパッチオブジェクト */
	Dispatch			dispatchObj;

	/** システムプロパティ **/
	private	Property				property;
	
	/** システムコンフィギュレーションファイルデータ */
	public	String		sysConfStr;
    
    /** ログファイル */
    PrintStream logs;
    
    /** クラスリストファイルの名前 */
    final String 					classFile    	= "class.lst"; 

    /** 課題リストファイルの接頭語 */
    final String 					subjctFile   	= "subject"; 
    
    /** 解答ファイルの接頭語 */
    final String 					answerFile   	= "answer"; 
    
    /** 名簿１ページの表行数 */
    final int    					ROWS         	= 20; 
    
	/** タイムスタンプ［３時間］ */
    private static final int		LIMIT  			= 3; 
    
    /**
     *   form の action キーワード
     *   同じHTMLファイルを異なるサーブレットで使うときにこの値を埋め込む
     */
	String							ACTION_KEYWORD;

    /** Power Campas システムディレクトリ */
    String 							pcpath;
    
    /** 設定ファイルディレクトリ */
	String 							confpath;
    
	/** ログファイルディレクトリ */
    String 							logpath;
    
    /** 設定ファイルへの完全パス */
	String 							pcConfPath;
    
	/** ログファイルへの完全パス */
    String 							debugPath;

    /** （データベース）ドライバー名 */
	String	driver;

	/** （データベース）URL */
	String	dburl;
    
	/** （データベース）ユーザー名 */
	String	dbuser;
    
	/** （データベース）パスワード */
	String	dbpasswd;
    
	/** （データベース）ログファイル */
	String	logfile	;

	/** 戻りURLの規定値 */
	//String		 					SYSURL; 
    
	/** ユーザーID発生用 */
    String 							numberFile; 
    
    /** ライセンスキーファイルへの完全パス名 */
    String 							keyFilePath;
    
    /** セキュリティチェック用ユーザー名 */
	String							keyUser; 
    
	/** セキュリティチェッククラス */
	Moonwalker						walker;

    
    /** タイプ記述子 */
	public static final String 	CONTENT_TYPE 	= "text/html; charset=MS932";
	//public static final String 	CONTENT_TYPE 	= "text/html";
	
	/** セッションフラグ名 */
    public static final	String	SESSION_FLAG 	= "_sessionFlag";
	
    /** 簡易暗号化用変数（エンコード） */
    private  Hashtable    			ech       		= new Hashtable(20,10);
    
    /** 簡易暗号化用変数（デコード） */
    private  Hashtable    			dch       		= new Hashtable(20,10);

    /** パスフレーズ */
    static final String 			PASSPHRASE		= "tkx280113";
  
    /**
     * 簡易暗号化用定数
     */
    private static final String [] CRIPTO   = 
        {"7","_","5","P","d","R","A","3","Q","r","1","2","S","4","T","6","U","8","9","0",
         "a","b","c","V","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t",
         "u","v","w","x","y","z","B","C","D","E","F","G","H","I","J","K","L","M","N","O"};
 
	/** ログファイルへの完全パス */
    String 	sysLogName;
    String	accessLogName;
	
    PrintWriter	sysLog;
    PrintWriter	accessLog;
    
    
    /**
   * サーブレットの初期化
   */
	@Override
	public void init() throws ServletException {
	    if(LOG.fa) LOG.println("■ class stwork #init() : 初期化 の先頭です");
		String FS 		= File.separator;

	    // システムパスを設定する
		setSystemPath();
	    
	    // システムプロパティを作成する
	    makeSysProp();

	    // データベースプロパティを設定する
		setDatabaseProperty(property);
		
		
		/*
		 * ログファイルを開く
		 * 
		 */
		openLog();
		if(property.get("LOG").equals("YES")){
		    
			LOG.logOn(sysLog);
			LOG.println("log is opend");
			
			epml.tools.DBG.logOn(accessLog);
		}		
		
		// DbConnectionBroker の生成
        try{
			myBroker 	= new DbConnectionBroker( driver,dburl,dbuser,dbpasswd,2,7,logfile,1.0 );
        } catch (IOException e5){
			System.out.println(" ★データベースプールを初期化できません; " + e5);

			LOG.println(" ★データベースプールを初期化できません; " + e5);
			LOG.println("driver=" + driver);
			LOG.println("dburl=" + dburl);
			LOG.println("dbuser=" + dbuser);
			LOG.println("dbpasswd=" + dbpasswd);
			
		}
       	db = new Database (myBroker);
		//
		//  ディスパッチリストを作成しておく
		String		dispatchFile	=	property.get("dispatch");
		dispatchData				=	Gear.getFileData(dispatchFile);
		dispatchObj					=	new Dispatch(dispatchData);

		
  	}
    public	void	openLog(){
        
        // 追記、自動フラッシュ
        try {
             sysLog		= new PrintWriter( (new FileWriter(sysLogName, true)), true);
        } catch (IOException e) {
            //e.printStackTrace();
        }
        try {
            accessLog	= new PrintWriter( (new FileWriter(accessLogName, true)), true);
       } catch (IOException e) {
           //e.printStackTrace();
       }
        
    }	
    /**
     * システムパスを設定する
     */
	   void setSystemPath(){
			if(LOG.fa) LOG.println("■PowerCampus #setSystemPath()");
			
			/*
			 * ドキュメントルートの実際のパスをpcpathにセットする
			 */
			pcpath		=	getServletContext().getRealPath(getInitParameter("baseUri"));
			confpath	=	pcpath	+	getInitParameter("confdir") + FS; // web.xmlの初期化パラメータにはFSがない
			pcConfPath	=	confpath	+	"pc.conf";

			property	= 	getConfPoperty(Gear.getFileData(pcConfPath));			
	        
	        numberFile	=	property.get("numberFile");

	        // ログパスを取得する
	        logpath			=	property.get("logdir");
	        sysLogName		=	property.get("sysLog");// PowerCampus システムログ
	        accessLogName	=	property.get("accessLog");// PowerCampus システムログ	        

	    }    
	    void makeSysProp(){
		    
	        // システムコンフィギュレーションファイル全体
	        sysConfStr	=	Gear.getFileData(pcConfPath);
	        
		    //  システムプロパティオブジェクトを得る
	        property	= 	getConfPoperty(sysConfStr);	

	    }
		/** 
		 * String のファイルデータを読んで，システムプロパティオブジェクトを作成して返す
		 */
	    public	Property	getConfPoperty(String s){
			if(LOG.fa) LOG.println("■PowerCampus #getConfPoperty()");
	    	
			Property 		property 	= null;
			StringReader	sr			= new StringReader(s);
			try{
				property	= new Property(sr);
			}catch(IOException e){
				System.out.println("★ 致命的なエラーです．設定ファイルが読めません.");
			}
	    	return	property;
	    }   
	
    /**
     * GET 処理
     * 
     * _sheet 変数にプログラム番号を入れて呼び出す
     * 
     * http://mail-and-work.net/PowerCampus/?_sheet=100
     * 
     */
    @Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if(LOG.fa) LOG.println("■PowerCampusST #doGet()");

		response.setContentType(CONTENT_TYPE);
		PrintWriter out     = response.getWriter();  // エンコードは、response.setContentType(CONTENT_TYPE)に従う
		response.setHeader("Content-Language","ja");

        // システム変数を初期化する
    	Hashtable   htb			=	null;
    	Param  		para		=	null;
        htb						= 	new Hashtable(100);
        para  = new Param(property);

        // html の action に埋めるキーワード
		ACTION_KEYWORD	= request.getContextPath() + "/servlet/PowerCampusST";	        
        
        /*
         * プログラム番号が受け取りパラメータにない場合でも
         * マルチパートインプットを行なわない設定にしておく．
         */
        htb.put(MULTI_MODE_KEY, MULTI_OFF);
        /*
         * 初期設定を行ない、全ての受け取りパラメータを取得する
         */
    	startServlet(out, htb, para, request, response);
        /*
		 * セッション制御を行なう
		 * 必ずセッションエラーとなるのでLOGIN処理がコールされる
         * 指定した処理はLOGIN処理コール後にリターンして来てから実行される
         */
        setSeqence(out,htb,para,request,response);
        dispatcher(out,htb,para,request, response);	
		return;
	}
    /**
     * doGet 用のプログラム起動シーケンスの制御
     * 
     * セッションは常に切れているものとする
     * getパラメータのコマンド(cmd)を見てコールすべきプログラムを決定する．
     * 　　	コマンドあり	-- ユーザ登録
     * 		コマンドなし	-- ログイン
     * 
     * どちらをコールしても最終的にはデフォルトのトップページが表示されるよう制御する
     * 
     * @param out
     * @param htb
     * @param para
     * @param request
     * @param response
     */
	public void setSeqence(PrintWriter out,Hashtable htb,Param para,HttpServletRequest request, HttpServletResponse response) {
		if(LOG.fa) LOG.println("■PowerCampus #doPostSession()");

		String	cmd	=	Gear.strHash(htb,"cmd");
       	/*
       	 * デフォルトの時間割ページ表示を起動するよう指定しておく
       	 */
   	    htb.put(NUMBER, dispatchObj.getNumber(ST_DEFAULT_PAGE)); // 時間割ページ
   	    /*
       	 * セッションが切れているのでセッションフラグを立てて実行に先立ちLOGINをコール
       	 * することを dispatcher()に指示する
   	     */
   	    htb.put(SESSION_FLAG,"LOGIN");
        	
		// タイムスタンプを書き直す
        String stamp     = DateGear.CalToStr(DateGear.calculateDayH(DateGear.currentDay(),LIMIT)); // ３時間先のタイムスタンプ
		String encStamp  = cryptoDigit(stamp);                          // 簡易暗号化
        htb.put(STAMP,encStamp);
	}  
    /**
     * POST 処理
     */
    @Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) 
    															throws ServletException, IOException {
		if(LOG.fa) LOG.println("■PowerCampusST #doPost()");

		response.setContentType(CONTENT_TYPE);
		PrintWriter out     = response.getWriter();  // エンコードは、response.setContentType(CONTENT_TYPE)に従う
		response.setHeader("Content-Language","ja");
        
        /*
         *  システム変数を初期化する
         *  システム固有の処理でオーバーライドされない部分
         */
		Hashtable   htb			=	null;
    	Param  		para		=	null;
        htb						= 	new Hashtable(100);
        para  = new Param(property);

        // html の action に埋めるキーワード
		ACTION_KEYWORD		= 	request.getContextPath() + "/servlet/PowerCampusST";	        
        
        String	sheetNum	=	startServlet(out, htb, para, request, response);
        
        /*
         * ログイン処理ではパスワードチェックを行う．
         * 
         * 
         * それ以外は doPostSession() を実行しセッションチェックなどを行う
         * 
         */
        String	login_number	=	dispatchObj.getNumber(DISPATCH_StLOGIN);
        
        
        if(sheetNum.equals(login_number)){
			/*
			 * セッション制御なしでログインプログラムに分岐する
			 */
        	dispatcher(out, htb, para,request,response);

        }else{
			/*
			 * セッション制御を行なう
			 * エラーならLOGIN処理へ分岐するようフラグが立つ
			 */
			doSession(out,htb,para,request,response);
			/*
			 * 起動する
			 */
			dispatcher(out,htb,para,request, response);	
		}
		return;
	}
    /**
     * サーブレットの開始処理
     * 
     * @param request
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    String	startServlet(PrintWriter out, Hashtable   htb, Param  para,
    		            HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	
		if(LOG.fa) LOG.println("■PowerCampusST #startServlet()");
		
	   	/*
    	 * 初期化処理．
    	 * オーバーライド可能とする
    	 */
    	initialize(out,htb,para,response);

		//ライセンスチェック
		//license(out, htb, para, request);
		
		//	本サーバーの稼動時のＩＰアドレス（外からと内からで違う）
        htb.put(SERVER_IP, Gear.getServerIP(request));
        
        // ユーザーＩＰアドレス
        htb.put(USER_IP,request.getRemoteAddr());

        /*
         * 全てのパラメータを htb に得る
         * 必要ならマルチパートインプットも実行する
         */
        String	sw			=	Gear.strHash(htb, MULTI_MODE_KEY);
        if(Gear.isEmpty(sw)){
        	sw	=	MULTI_ON;
        }
        String	sheetNum	=	getParameters(out, htb, para, request, sw);

        /*
         * ログイン処理でセッションを作成するのに備えてセッションオブジェクトをハッシュに保存する
         */
        HttpSession session  = request.getSession(true);
        htb.put("_httpSession",session);
        
        return	sheetNum;
    }	
    
    /**
     * セッション制御を行なう
     * 
     * セッションを確認した場合はユーザー変数の初期設定を行う
     * セッション切れの場合は sessionFlag を立て，分岐先を指定する
     * 　　	
     * 		LOGIN	-- ログイン
     * 
     * @param out
     * @param htb
     * @param para
     * @param request
     * @param response
     */
	public void doSession(PrintWriter out,Hashtable htb,Param para,HttpServletRequest request, HttpServletResponse response) {
	    if(LOG.fa) LOG.println("■PowerCampusST #doPostSession()");
	    
		if(checkSsession(request)){
			/*
			 * セッション情報に基づいてデータベースを検索し、ユーザー基本情報をシステムハッシュにセットする
			 * オーバーライド可能
			 */
		    setBasicVar(htb, request);
			
		}else{
			/*
			 * セッションフラグを立てる（セッション切れ）
			 * dispacher() で再ログイン処理を行なう
			 */
			htb.put("_sessionFlag","LOGIN");
		}
        // タイムスタンプを書き直す
        String stamp     = DateGear.CalToStr(DateGear.calculateDayH(DateGear.currentDay(),LIMIT)); // ３時間先のタイムスタンプ
		String encStamp  = cryptoDigit(stamp);                          // 簡易暗号化
        htb.put(STAMP,encStamp);
	}
	/**
	 * プログラムナンバー(NUMBER)によりプログラムを検索してそのdispatch()メソッドを実行する．
	 * 
	 * 最初に検索したプログラムを実体化する
	 * 
	 * (A) 実体化に成功しセッションも正常ならば
	 * 		1.ビジネスロジックを実行し，
	 * 		2.そのリターンコードをもとにプログラム遷移を行うようrelay()に指示を出す
	 * 
	 * (B)プログラム番号の書き誤りなどで実体化できなかった場合はシステムエラーページを表示するようrelay()に指示を出す
	 * 
	 * (C)セッション切れで実行できない（セッションフラグ）場合は
	 *		1.ビジネスロジックの実行をスキップし
	 * 		2.LOGINをコールするようrelay()に指示を出す
	 * 
	 * @param out
	 * @param htb
	 * @param para
	 * @param req
	 * @param res
	 */
	public void dispatcher(PrintWriter out,Hashtable htb,Param para,HttpServletRequest req, HttpServletResponse res){
	    if(LOG.fa) LOG.println("■PowerCampusST #dispatcher()");
	    
		Dispatch 	dispatch	=	new Dispatch(dispatchData);
		String		number		=	Gear.strHash(htb,NUMBER);
		int			sw			= 	(Integer.valueOf(number)).intValue();   // 整数に直す

		/* 
		 * プログラム番号から起動するプログラムを求め実体化する
		 */
		String	className		=	null;
		String	classKey		=	null;	
	  	SuperPlayer	ctrl		=	null;
	  	try{
	  		className		=	dispatch.getClassName(sw);	// 実クラス名 (ex.  jbbs.BbsInfo  )
	  		classKey		=	dispatch.getKey(sw);		// クラスキー (ex.  $jbbs.BbsInfo )	
	  		ctrl			=	getCtrlObj(className, htb , out);	// クラスオブジェクトを取得する
	  		
	  	}catch(PCException e){
	  		/*
			 * プログラムまたはdispatch.xml の書き誤りである．
			 * システムエラーセッションフラグを立る．
			 */
			htb.put(SESSION_FLAG,"ERROR");
			
	  	}
	    /*
	     * セッションが切れていれば　ret に起動プログラム名を入れて、ログインをコールする．
	     * ログインが成功すると、その中でセッションデータが設定される．
	     * （現在のプログラム名とシステムハッシュは退避されるので、RETURN で戻ってこれる）
	     * 
	     * そうでなければプログラムを起動し結果を受け取る
	     */
	    String	sessionFlag	=	Gear.strHash(htb, SESSION_FLAG);
        String		ret		=	"";
        
	    if(sessionFlag.equals("LOGIN")){
	    	/*
	    	 * doSession()でセッション切れと判明した場合、"LOGIN"がセットされる
	    	 * 
	    	 * 現在の実行プログラムを実行シーケンスにプッシュして，呼び出しにより
	    	 * ログイン画面を表示するように設定（ビジネスロジックはスキップする）
	    	 */
	    	ret	=	DISPATCH_StLOGIN;
	    
	    }else if(sessionFlag.equals("ERROR")){
	    	/*
	  		 * エラーメッセージ画面を表示するように設定（システムは停止する）
	  		 */
	    	htb.put(MSG_TITLE	,"致命的なシステムエラー");
	  		htb.put(MSG_BODY	,"指定されたプログラムは存在しません ( 確認してください．Program Number = " + sw + " )");
	    	ret	=	SYS_MSG_KEY;	// システムエラーメッセージ表示の論理プログラム名
	    		    	
	    }else{
	    	/*
	    	 * 指定されたプログラムを起動し、ビジネスロジックを実行する
	    	 * 自動ログインはdoSession()を実行しないので直接ここへ落ちる
	    	 */	    	
	    	ret	=	ctrl.dispatch(out, htb, para);

	    }
	    /*
	     * 次の画面への遷移を行なう
	     * ret に遷移情報がある
	     */
		relay(classKey,ctrl, ret, dispatch, out, htb, para, req, res);
		return;
    }
	/**
	 * 正しいクラス名（dispatch.xmlに記述されていることを確認したクラス名）を元にして
	 * そのクラスオブジェクトを取得する．
	 * 
	 * エラーになった場合はdispatch.xmlの書き誤りであるから、
	 * システムエラーメッセージ表示のプログラムオブジェクトを得る
	 * 
	 * @param className
	 * @param htb
	 * @param out
	 * @return
	 */
	SuperPlayer	getCtrlObj(String className, Hashtable htb ,PrintWriter out){

  		/*
  		 * クラスオブジェクトを返す
  		 */
		SuperPlayer	ctrl		=	null;
  		try{
  			Class 	name	=	Class.forName(className);
  			Object	obj		=	name.newInstance();
  			ctrl			=	(SuperPlayer)obj;
		
  		}catch(Exception e){
  			/*
	  		 * システムエラーメッセージ表示の論理プログラム名からプログラムを得る
	  		 */
	  		String 	title	=	"致命的なシステムエラー[ PowerCampusST #initialDisplay() ]";
	  		String	body	=	"指定されたプログラムは存在しません (  確認してください．Program = " + className + " )";
  			ctrl			=	getErrmsgObj(title, body, htb, out);
  		}
  		return	ctrl;
	}
	/**
	 * システムエラーメッセージ表示の論理プログラム名からプログラムを得る
	 * 
	 * @param msgtitle
	 * @param msgbody
	 * @param htb
	 * @param out
	 * @return
	 */
	SuperPlayer	getErrmsgObj(String msgtitle, String msgbody, Hashtable htb ,PrintWriter out){
  		/*
  		 * システムエラーメッセージ表示の論理プログラム名からプログラムを得る
  		 */
		SuperPlayer	ctrl	=	null;
		String errClassName	=	dispatchObj.getClassName(SYS_MSG_KEY);

  		htb.put(MSG_TITLE	,msgtitle );
  		htb.put(MSG_BODY	,msgbody);
		htb.put(NUMBER, dispatchObj.getNumber(SYS_MSG_KEY));			
  		/*
  		 * エラーメッセージ表示クラスを返す
  		 */
		try{
			Class 	name	=	Class.forName(errClassName);
			Object	prog	=	name.newInstance();
			ctrl			=	(SuperPlayer)prog;
		}catch(Exception e2){
			/*
			 * dixpatch.xmlを書き間違わなければここには落ちない．
			 * プログラムを停止する．
			 */
			e2.printStackTrace();
			String	msg	=	"★PowerCampus#initialDsplay() : システムエラーページのキーを確認してください．/ "+ SYS_MSG_KEY + "　→ " + errClassName;
			LOG.errStop(out, msg + "\\n" + e2);
			
		}  			
		return	ctrl;
		
	}
	/**
	 * 次の状態への遷移処理
	 * 同じクラスの画面の再表示するか、または他のクラスを起動して初期画面を表示する
	 * 他のクラスを起動する場合は、クラスキー ret によりディスパッチテーブルを検索してクラス名を得る
	 * 
	 * @param fromKey			現在のクラスキー（プログラムシーケンスを構成する）
	 * @param fromName			現在のクラス名
	 * @param gotoKey			次に起動するクラスキー．SELF なら現クラスの再表示を行う
	 * @param dispatch			プログラムシーケンス情報オブジェクト
	 * @param out				出力ポインタ
	 * @param htb				システムハッシュ
	 * @param conf				システムプロパティ情報を持つオブジェクト．プレーヤー内でキャストされる
	 */
     public	void	relay(String fromKey, SuperPlayer from, String gotoKey, Dispatch dispatch, PrintWriter out, Hashtable htb, 
     						Param conf, HttpServletRequest req, HttpServletResponse res){
		
     	if(LOG.fa) LOG.println("■PowerCampusST #relay()");
     	
     	if(LOG.fa){
     		LOG.println("");
     		LOG.println("□PowerCampusST #relay() : gotoKey =" + gotoKey);
     	}
     	
		/* html に埋め込むアクションキーワードをシステムハッシュに設定する */
		htb.put(ACTION_KEY, actionKeyword());
		/* 
		 * リターンコードがSELFの時、画面の再表示を処理する
		 * htb の中の、キー DISP で参照する値が DISP_NEW なら新規表示、それ以外では変数の状態を維持して再表示する 
		 * DISPで参照する値はプログラマの責任で情報を残す．設定しないと常にDISP_NEWと解釈される
		 * 
		 */
   		if(gotoKey.equals( DISPATCH_DEFAULT )){
   			selfReturn(fromKey, from, dispatch, out, htb, conf);
			if(LOG.fa) LOG.println("□PowerCampusST #relay() : " + fromKey + "  → DISPATCH_DEFAULT");
    		return;
    	}
   		/*
		 * ディスパッチシーケンスを調整する.
		 * 
		 * ディスパッチシーケンスは表示画面の遷移を記録しているスタックであり、
		 * 画面WEBの中にhidden項目としてCSV形式で埋め込まれている．
		 * ただしデリミッタは':'
		 * ここではそれからスタックを復元し、遷移状況を調整する．
		 * 
		 * すなわち、呼び出し元へ戻る場合はひとつシーケンスをPOPし、
		 * 他のクラスを起動する場合は自分のクラスキーをシーケンスにPUSHする．
		 */
		String	seq	=	Gear.strHashIncludeNull(htb, SYS_SEQUENCE);
		if(Gear.isEmpty(seq)){
			htb.put(SYS_SEQUENCE,"");
			seq	=	"";
		}
		DispatchStack	stk	=	new DispatchStack(seq, dispatch);
		//
		if(gotoKey.equals( DISPATCH_RETURN )){
			// 単純なリターン
			Hashtable	newHash	=	null;
			try{
				/* 
				 * リーターンした状態のスタック（システムハッシュ）を復元する
				 * ディスパッチシーケンスもその状態に復元する
				 */
				newHash	=	stk.extractLastHash(htb);
				/*
				 * NonSerializable なシステムデータはここで復元しておく
				 * NonSerializable なシステムデータはプログラム固有のものではなく、
				 * システムの都合で必要なものである
				 */
				setNonSerializableData(newHash, req);
				
			}catch(tkException e){
				e.printStackTrace();
				Gear.errPrint(out,"□PowerCampusST #relay() : プログラムシーケンスに対応するハッシュアーカイブがない");
			}
			htb	=	newHash;
			/*
			 * ディスパッチシーケンスの末尾をPOPして、次に起動するプログラムとする．
			 * これはリターン動作に対応する
			 * また、POPした論理プログラム名はシステムハッシュのディスパッチシーケンス文字列から削除される
			 */
			gotoKey	=	stk.pop();
			/*
			 * 戻り先画面がクリアされないようにするため
			 * 戻り先のプログラムの表示モードを EDIT にセットする
			 */
			htb.put(DISP_KEY, DISP_EDIT);
	 		/*
	 		 * ログインプログラムからリターンした時は、セッションフラグが立っている．
	 		 * すなわち、Gear.strHash(htb,"_sessionflag")=="LOGIN" である．
	 		 * この場合は、リターン先プログラムはログイン未済なのでスタックには基本変数がふくまれない．
	 		 * そこで、セッション変数から基本情報を作成してシステムハッシュにセットする必要がある．
	 		 * 
	 		 * その理由は、通常、基本変数のセットはセッション制御をパスした時点で作成されるが、
	 		 * ログインプログラムはセッション制御をスキップするからである．したがって、ログインが成功して
	 		 * からここでセッション変数を元にして作成しなければならない．
	 		 * 
	 		 */
			String	sessionFlag	=	Gear.strHash(htb,SESSION_FLAG);
			if(LOG.fa){
			    LOG.println("■PowerCampusST #relay() : sessionFlag=" + sessionFlag);
			}
			if(sessionFlag.equals("LOGIN")){
			    setBasicVar(htb, req);
			}
			
		}else if(gotoKey.equals( DISPATCH_GOBACK )){
			// いくつかの戻り先を飛ばして指定のプログラムにリターンする
			String		progKey	=	Gear.strHash(htb,GOBACK_KEY);
			Hashtable	newHash	=	null;
			try{
				/* 
				 * リーターンした状態のスタック（システムハッシュ）を復元する
				 * ディスパッチシーケンスもその状態に復元する
				 */
				newHash	=	stk.extractHashByName(htb,progKey);
				/*
				 * NonSerializable なシステムデータはここで復元しておく
				 * NonSerializable なシステムデータはプログラム固有のものではなく、
				 * システムの都合で必要なものである
				 */
				setNonSerializableData(newHash, req);

			}catch(tkException e){
				e.printStackTrace();
				Gear.errPrint(out,"□PowerCampusST #relay() : プログラムシーケンスに対応するハッシュアーカイブがない");
			}
			htb	=	newHash;
			/*
			 * stk から対応する位置までシーケンスをPOPして、次に実行する論理プログラム名とする
			 * これはリターン動作に対応する
			 * また、POPした論理プログラム名はシステムハッシュのディスパッチシーケンス文字列から削除される
			 */
			gotoKey	=	stk.popTo(progKey);
			if(gotoKey==null){
				Gear.errPrint(out,"□PowerCampusST #relay() :  戻り先プログラムがシステムハッシュのシーケンス中にない");
			}
			/*
			 * 戻り先のプログラムの表示モードを EDIT にセットする
			 * 戻り先画面がクリアされないようにするため
			 */
			htb.put(DISP_KEY, DISP_EDIT);			
			if(LOG.fa) LOG.println("□□" + fromKey + " → BACK to (" + gotoKey +")");

		}else if(isAutoLogin(htb)){
			/*
			 * 自動ログインのためにセッション変数から基本情報を作成してシステムハッシュにセットする
			 * セッション変数はログイン処理でセットされている
			 */
		    setBasicVar(htb, req);
			
		}else{
			/*
			 * プログラムの遷移（ディスパッチシーケンス）を記録するため、呼び出しもとの論理プログラム名をシス
			 * テムハッシュに保存する.
			 * 例えば、システムハッシュには、"_sys.SEQUENCE" というキーで "$prog1:$prog2:$prog3" というように
			 * 呼び出しシーケンスが記録されていく
			 * 
			 * また、システムハッシュには、スタック（その時のシステムハッシュ内の変数）も記録される
			 * 例えば、すでに３つのプログラムが呼び出されている時は以下のように３つのキーで、３つのスタックが
			 * 保存されている．
			 *    "_sys.prog1"
			 *    "_sys.prog2"
			 *    "_sys.prog3"
			 * 
			 * これらの値（スタック値）は、htb をシリアライズしてさらに Base64エンコードした文字列である．
			 * スタック値には非serializable な変数は含まれない．
			 * また、スタック値はそれまでのスタック値も含まない．つまり、String stackVal = htb.get("_sys.prog3");を
			 * もとのHashtableにデコードしても、その中に "_sys.prog1"、"_sys.prog2" の対応するスタック値は含まれない．
			 * スタック値はそれぞれ独立に保存されているからである．
			 * 
			 * ここでは
			 * (1)現在のシステムハッシュ（htb）をスタック値として、htb に追加保存する
			 * (2)呼び出しシーケンスにfromKey（呼び出し元論理プログラム名）を追加した新しいシーケンス値を
			 * 　 作成してシステムハッシュ(htb)に再登録する
			 * 
			 * なお、(2)を行なっておくことにより、webに書き込む hidden タグも自動的に生成されるようになる
			 */
			stk.updateHashWithPush(fromKey,htb);

			if(LOG.fa) LOG.println("□□" + fromKey + "  → GOTO (" + gotoKey + ")" );

		}    	
		/*
		 * 全てのhiddenタグを生成してシステムハッシュに "_sys.HIDDEN" というキーで格納しておく．
		 * getHiddenTags(gotoKey)はプログラムシーケンスタグも生成するのでシーケンスを調整後に行なう必要がある．
		 * 
		 * 　※ _sys.HIDDEN は htmlの出力を担当するSuperPrintクラスが全ての出力に先立ってhtmlに埋め込む
		 * 　　 また、埋め込んだ後の%変数%の値への置き換えも先立って行なうので、埋め込み位置はどこでもよい
		 * 
		 */
		String	hiddenTags	=	stk.getHiddenTags(gotoKey);
		htb.put(SYS_HIDDEN, hiddenTags);
		/*
		 * プログラム画面を表示する
		 */
		initialDisplay(dispatch, gotoKey, out, htb, conf);
		return;
    }
  	/**
  	 * html に埋め込むアクションキーワードを返す
  	 */
  	public	String	actionKeyword(){
  		return	ACTION_KEYWORD;
  	}
    /**
     * 自動ログインかどうか調べる
     * @return
     */
    boolean	isAutoLogin(Hashtable htb){
    	
    	String	auto	=	Gear.strHash(htb, AUTO_LOGIN);
    	if(!isEmpty(auto)){
    		if(auto.equals(AUTO_LOGIN_ON)){
    			return	true;
    		}
    	}
    	return	false;
    }
     
    /**
     * 同じプログラムを再表示する（繰り返し）
     * @param fromKey
     * @param from
     * @param dispatch
     * @param out
     * @param htb
     * @param conf
     */
    public void selfReturn(String fromKey, SuperPlayer from, Dispatch dispatch, PrintWriter out, Hashtable htb, Param conf){
		if(LOG.fa) LOG.println("■PowerCampusST #selfReturn()");

    	/*
		 * ディスパッチシーケンスを得る
		 */			
		String	seq	=	Gear.strHashIncludeNull(htb, SYS_SEQUENCE);
		if(Gear.isEmpty(seq)){
			htb.put(SYS_SEQUENCE, "");
			seq	=	"";
		}
		DispatchStack	stk	=	new DispatchStack(seq, dispatch);
		
		/*
		 * 全てのhiddenタグを生成してシステムハッシュに "_sys.HIDDEN" というキーで格納しておく．
		 *  _sys.HIDDEN を実際にhtmlの中に埋め込むのは htmlの出力を担当するSuperPrintクラスが行う
		 */			
		String	hiddenTags	=	stk.getHiddenTags(fromKey);
		htb.put(SYS_HIDDEN, hiddenTags);
					
		/* ロードするファイル名をシステムハッシュにセットして表示処理を実行する */
		htb.put(DISPFILE, getFilePath(fromKey, dispatch, conf));
		//from.display(out,htb,conf);
		from.display();
		return;
    }
     /**
      * 実行されるプログラムを初期表示する
      * 
      * @param dispatch
      * @param gotoKey
      * @param out
      * @param htb
      * @param conf
      */
     public void	initialDisplay(Dispatch dispatch, String gotoKey, PrintWriter out, Hashtable htb, Param conf){
		if(LOG.fa) LOG.println("■PowerCampusST #initialDisplay()");
		/*
		 * リターンコード(ret)から表示するプログラムのクラスオブジェクトを得る
		 * クラスオブジェクトは SuperPlayer クラスのサブクラスである．
		 */
		SuperPlayer	sp		=	null;
		String	className	=	dispatch.getClassName(gotoKey);
		if(isEmpty(className)){
	    	/*
	    	 * エラーメッセージ画面を表示する
	    	 */
			errdisplay(dispatch, gotoKey, out, htb, conf);
			return;		
		}
		if(LOG.fa) LOG.println("■PowerCampusST #initialDsplay() :gotoKey =" + gotoKey);
		
		/*
		 * 初期表示のためにプログラムを起動する
		 */
		try{
			Class 	name	=	Class.forName(className);
			Object	prog	=	name.getDeclaredConstructor().newInstance();;
			sp				=	(SuperPlayer)prog;
		}catch(Exception e){
	    	/*
	  		 * ここに落ちるのはプログラムの書き誤りである
	  		 * すなわち、プログラム内で 論理プログラム名を誤って記述しているか、dispatch.xmlに何らかの記述間違いが
	  		 * ある場合が考えられる．
	  		 * 
	  		 * エラーメッセージ画面を表示する
	  		 */
			errdisplay(dispatch, gotoKey, out, htb, conf);
			return;		
		}    		
		if(LOG.fa){
			LOG.println("■PowerCampus#initialDsplay() :getFilePath(gotoKey, dispatch, conf)=" + getFilePath(gotoKey, dispatch, conf));
		}
		/*
		 * ロードするファイル名をシステムハッシュにセットして表示処理を実行する
		 * display(out,htb,conf)はクラスを初期化した後、display()を実行する
		 */
		htb.put(DISPFILE, getFilePath(gotoKey, dispatch, conf));
		sp.display(out,htb,conf);
		out.close();
		return;
    }

     /**
      * データベースのプロパティを設定する
      */
     void setDatabaseProperty(Property property){
 		if(LOG.fa) LOG.println("■PowerCampusST #setDatabaseProperty()");
     	
 		driver		= property.get("db_driver");
 		dburl		= property.get("db_url");
 		dbuser		= property.get("db_user");
 		dbpasswd	= property.get("db_passwd");
 		logfile		= logpath + "PowerCampas.dbclog";
 		
 		if(LOG.fa){
 			LOG.println("    driver   / " + driver);
 			LOG.println("    dburl    / " + dburl);
 			LOG.println("    dbuser   / " + dbuser);
 			LOG.println("    dbpasswd / " + dbpasswd);
 			LOG.println("    logfile  / " + logfile);
 		}		
     	
     }	
 	/** 
 	 * システムプロパティオブジェクトを返す
 	 */
     public	Property	getConfPoperty(){
 		if(LOG.fa) LOG.println("■PowerCampusST #getConfPoperty()");
    	
 		Property 			property 	= null;
 		StringReader	sr			= new StringReader(sysConfStr);
 		try{
 			property	= new Property(sr);
 		}catch(IOException e){
 			System.out.println("★ 致命的なエラーです．設定ファイルが読めません.");
 		}
     	return	property;
     }  	
     /**
      * システム変数を初期化する
      * 
      * この処理はオーバーライドされる可能性がある
      * 
      * @param out
      * @param htb
      * @param para
      * @param response
      * @throws IOException
      */
     public	void	initialize(PrintWriter out, Hashtable  htb, Param para, HttpServletResponse response) throws IOException{

         /*
          * いつでもエラー出力できるようにoutをPara に保存
          */
         para.addResponseWriter(out);
 		/* 
 		 * ブローカーをhashに入れておくことにした 2004.8.15
 		 */
 		htb.put(BROKER, myBroker);

 		/*
 		 * 分類区分を学生に設定しておく
 		 */
 		htb.put(DIVISION, DIV_STUDENT);	// 区分は学生
		/*
		 * 暗号化用変数の準備
		 */
        for(int i=0; i<10; i++){
            String s = String.valueOf(i);
            dch.put(CRIPTO[i],s);
            ech.put(s,CRIPTO[i]);
        } 		
     }
  	
     /**
      * 全てのパラメータを htb に得る
      * 必要ならマルチパートインプットも実行する
      * @param out
      * @param htb
      * @param para
      * @param request
      * @return
      */
     String	getParameters(PrintWriter out, Hashtable htb, Param para, HttpServletRequest request ,String multi_mode){
 		if(LOG.fa) LOG.println("■PowerCampusST #getParameters()");

         /*
          * シート番号を取得できないとき、multi_modeがMULTI_ONならマルチパート入力とみなす
          * それ以外では単にmakeHash()を実行する
          * 
          * マルチパート入力は、MultiInput() を実行し makeHash()と同様にhtb にパラメータを得る他、
          * ファイルがあればそれも取得する．取得したファイルのファイル名やディレクトリなどは
          * システムパラメータにセットされる．
          * 
          * 　参照：
          * 		UPLOAD_DIR_NAME
          * 		UPLOAD_FILE_NAME
          * 		UPLOAD_FILE_PATH
          * 
          */
         htb.put("_msg","");     // メッセージをクリアしておく
         String s = getParamAuto(request,NUMBER);  // htmlのシート番号

         if( (s==null)||(s.length()==0) ){
 			/*
 			 * NUMBER がない時自動的に実行するかどうか調べて
 			 * からマルチパートインプットを実行する
 			 */
         	if(multi_mode.equals(MULTI_ON)){
         		
 				Hashtable wk = MultiInput(request,out,htb,para);
 				if(wk==null){
 					// multiInputの失敗
 					String msg = Gear.strHash(htb,"_errorMsg"); // MultiInput() で設定されているものを取り出す
 					LOG.errStop(out,msg); // 停止する
 					return null;
 				}
 				s = Gear.strHash(htb,NUMBER);
 				if(LOG.fa) LOG.println("MultiInput から復帰");
 				
         	}else{
         		makeHash(request,htb,para);
         	}
         	
 		}else{
 			makeHash(request,htb,para);
 		}
         return	s;
     }
     /**
      * 標準出力の切り替え
      * @param para
      */
     void	setLog(Param para){
 		if(LOG.fa) LOG.println("■PowerCampusST #setLog()");

 		if(para.isLog()){
         	try{
         	    logs = new PrintStream (new FileOutputStream(debugPath,true),true); // 追記，自動フラッシュ
         	    System.setOut(logs);
         	}catch(FileNotFoundException e){
         	}
 		}
     }
     
 	/**
 	 * シリアライズされないデータはハッシュアーカイブに含まれないので
 	 * それを再設定するためのもの
 	 * @param ht
 	 */
 	public void	setNonSerializableData(Hashtable ht, HttpServletRequest req){
 		
 		/*
 		 * ログインプログラムがセッションを使うので
 		 */
         HttpSession session  = req.getSession(true);
         ht.put("_httpSession",session);	
 		/* 
 		 * ブローカーをBBSライブラリで使うのでhashに入れておくことにした 2004.8.15
 		 */
 		ht.put(BROKER, myBroker);
 	
 	}
	/**
	 * セッションが有効かチェックする
	 * 
	 * ログインプログラムのsetSession()と対になる
	 * 
	 * @param request
	 * @return
	 */
	public boolean checkSsession(HttpServletRequest request) {
	    if(LOG.fa) LOG.println("■PowerCampusST #checkSsession()");
		
        HttpSession session = request.getSession(true);            	// セッションオブジェクトの取得
      	String	stNumber 	= (String)session.getAttribute(PC_ID);  // 一意なユーザーＩＤ
      	String	szDB     	= (String)session.getAttribute(PC_ID2); // 一意なデータベース名（グループ名）

      	if((szDB == null)||(stNumber==null)){
			return	false;
		}else{
			return	true;
		}
	}
	/**
	 * 基本変数をシステムハッシュにセットする
	 * 
	 * @param htb
	 * @param request
	 */
	void setBasicVar(Hashtable htb, HttpServletRequest request) {

		HttpSession session		= null;
		String szDB				= null;
		String stNumber			= null;
		Hashtable rec			= new Hashtable();
		Student st				= null;

		try {
			// ユーザー情報
			session = request.getSession(true);           	// セッションオブジェクトの取得
			szDB = (String) session.getAttribute(PC_ID2);    	// 一意なデータベース名（グループ名）
			stNumber = (String) session.getAttribute(PC_ID);   // 一意なユーザーＩＤ

			/* 所属するドメイン名. */
			rec = db.shozokuRead(szDB);
			htb.put(DOMAIN, rec.get("sz_domain"));

			/* 基本情報をシステムハッシュに入れる */
			htb.put(GROUP, szDB);
			st = new Student(szDB, stNumber, db);
			if(st != null) {
				htb.put(StUID, st.id());
				htb.put(StNAME, st.kname());
				htb.put(StMAIL, st.email());
				htb.put(StKEITAI, st.keitai());
				htb.put(StCLASSINFO, st.classInfo());
				/* jbbs ライブラリのために別名でもセットする */
				htb.put(UID, st.id());
				htb.put(MAIL, st.email());
				htb.put(UNAME, st.kname());

			} else {
				LOG.println("PowerCampusST# setBasicVar(): 学生DBに情報がない");
			}
		} catch (Exception e) {
			
			LOG.println("★setBasicVarで例外が発生した/" + LocalDateTime.now());
			LOG.println("　session==null :"+ (session==null));
			LOG.println("　rec==null :" + (rec==null));
			LOG.println("---------");
			LOG.println("　szDB =" + szDB);
			LOG.println("　stNumber=" + stNumber);
			LOG.println("　st=" + st);			
			throw new NullPointerException();
		}
	}	

    /**
     * エラー処理画面の表示
     */
     void	errdisplay(Dispatch dispatch, String gotoKey, PrintWriter out, Hashtable htb, Param conf){
    	/*
  		 * ここに落ちるのはプログラムの書き誤りである
  		 * すなわち、プログラム内で 論理プログラム名を誤って記述しているか、dispatch.xmlに何らかの記述間違いが
  		 * ある場合が考えられる．
  		 * 
  		 * エラーメッセージ画面を表示する
  		 */
		SuperPlayer	sp	=	recovery(dispatch, gotoKey, out, htb, conf);
    	String	fname	=	dispatch.getHtml(SYS_MSG_KEY);
		htb.put(DISPFILE, conf.getHtmlPath() + fname);
		htb.put("_switch","CLOSE");

		sp.display(out,htb,conf);
		out.close();
		return;		     	
     }     
     /**
      * エラー処理
      * dispatch.xml との不整合を処理する
      */
     SuperPlayer	recovery(Dispatch dispatch, String gotoKey, PrintWriter out, Hashtable htb, Param conf){
     	/*
   		 * ここに落ちるのはプログラムの書き誤りである
   		 * すなわち、プログラム内で 論理プログラム名を誤って記述しているか、dispatch.xmlに何らかの記述間違いが
   		 * ある場合が考えられる．
   		 * 
   		 * エラーメッセージ画面を表示する
   		 */
     	String	title		=	"致命的なシステムエラー[ PowerCampus Framework ]";
   		String	msg			=	"指定されたプログラム " + gotoKey + " は dispatch.xml の中に記述がありません." ;
     	htb.put(MSG_TITLE	,title);
     	htb.put(MSG_BODY	,msg);
     	
     	SuperPlayer	sp		=	null;
 		htb.put(NUMBER, dispatch.getNumber(SYS_MSG_KEY));			
     	gotoKey				=	SYS_MSG_KEY;// 書き換えてしまう
     	String	className	=	dispatch.getClassName(SYS_MSG_KEY);// システムエラーメッセージ表示の論理プログラム名
   		try{
   			
 			Class<?> 	name	=	Class.forName(className);
   			sp = (SuperPlayer)(name.getConstructor().newInstance());
   			
 			//Object	prog	=	name.newInstance(); // java 9から使用禁止になった
 			//sp				=	(SuperPlayer)prog;
 		}catch(Exception e2){
 			/*
 			 * dixpatch.xmlを書き間違わなければここには落ちない
 			 */
 			msg	=	"★PowerCampus#initialDsplay() : didpatch.xmlで、"+ SYS_MSG_KEY + " の記述が間違っています";
 			LOG.errStop(out, msg );
 		}
 		String	fname	=	dispatch.getHtml(SYS_MSG_KEY);
 		if(fname==null){
 			/*
 			 * dixpatch.xmlを書き間違わなければここには落ちない
 			 */
 			msg	=	"★PowerCampus#initialDsplay() : didpatch.xmlで、"+ SYS_MSG_KEY + " の記述が間違っています";
 			LOG.errStop(out, msg );
 		}
 		return	sp;
     }
     
     ////////////////////////////////////////////////////////////////////////////////////////////////////
     //
     //      汎用メソッド
  	//
     ////////////////////////////////////////////////////////////////////////////////////////////////////
     //
  	//
     //
     //   （１）ＨＴＭＬファイルからパラメータを取り出すための汎用メソッド
     //
     //
     //      指定されたHTMLファイルを解析してパラメータ名を抽出し、さらに、post されたストリームから
     //      各パラメータ名に対応するパラメータ値を取り出し、ハッシュテーブル htb に詰め込む
     //      fpath は完全なファイルパスでなくてはならない
  	//
     //
     Hashtable makeHash(HttpServletRequest req,Hashtable htb,Param para,String fpath){
 		return makeHash(req,htb,para);
 	}
     Hashtable makeHash(HttpServletRequest req,Hashtable htb,Param para){
         //
         Vector params  = new Vector(20,10);
 		Enumeration parmNames =  req.getParameterNames();
 		while(parmNames.hasMoreElements()){
 			params.add( (parmNames.nextElement()) );
 		}
 		getParameters(req,htb,params);
         return htb;
 	 }
     // 
     //
     //    パラメータ名とその値をハッシュテーブルに格納する(htb は　new でインスタンスを生成してあること)
     //    パラメータ名はinit()で Vector keyList に格納してある
     //    日本語変換の問題もgetParamAuto()で解決されている。
     //
     Hashtable getParameters(HttpServletRequest req,Hashtable htb,Vector v){
         Enumeration e = v.elements();
         while(e.hasMoreElements()){
             String paramName    = (String)(e.nextElement());
             String s            = getParamAuto(req,paramName);
 			htb.put(paramName,s);
         }
         return htb;
     }
     // 日本語エンコードの問題をクリアしておく 2000/9/7
     public String getParamAuto(HttpServletRequest req,String paramName){
         String value = "";
         try{
             String s = req.getParameter(paramName);
             if((s != null)&&(s.length()>0)){
 				value = new String(s.getBytes("8859_1"),"JISAutoDetect");

                 //DBG.println("□input□" + value + "=" + Gear.toHexString(value));
             }else{
                 value = "";
             }
         }catch (IOException e){
         }
         return Cp932.toJIS(value); // 受け取り口で JIS に基準化しておく( see Cp932.java ）
     }
     //
 	// マルチパートインプット
 	// 
 	Hashtable MultiInput(HttpServletRequest req,PrintWriter out,Hashtable htb,Param para){
 		//
 		// マルチパートの処理
 		String savedir  = getTempdir(para);  		// 一時的格納場所 2003.3.1 　再帰をかけたほうが安全だが・・・
 		
 		htb.put(UPLOAD_DIR_NAME,savedir);			// ハッシュに入れておく
         htb.put("_errorMsg","");
 		htb.put("_counts","0");						// ファイル数
 		//
 		File temp = new File(savedir);
 		boolean flag = temp.mkdirs();
 		//
 		if(!flag){
 			LOG.println("■ PowerCampus class #MultiInput() : -- マルチ－パート処理で一時ファイル用ディレクトリを作成できない");
 			htb.put("_errorMsg","MultiInput(): マルチ－パート処理で一時ファイル用ディレクトリを作成できない");
 			return null;
 		}
 		try {
 			/* 全てのフォームパラメータとファイルを保存する．
 			 * ファイルは日本語に対応し、複数ファイルにも対応している
 			 * ファイル最大サイズは規定値は１Ｍ．ここでは20Mとしているが、この値を超えると
 			 * エラーになる．
 			 */
 			int maxSize = 20 * 1024 * 1024;
 			MultipartRequest multi = new MultipartRequest(req, savedir,maxSize); 	// Max 4MB とウェブには表示
 			//
 			// パラメータリストを得る（ 全パラメータの名前と値）
 			Enumeration params = multi.getParameterNames();
             while (params.hasMoreElements()) {//  マルチパートクラスを修正しパラメータは日本語対応で格納するようにしてある
                 String name  = (String)params.nextElement();
                 String value = multi.getParameter(name);
                 htb.put(name,value);                                    
             }
             /*
              * ファイル名と保存先ファイル名をセット
 			 * NOFILE だとここは読まない。NOFILE はファイル入力がない場合
 			 * パラメータの設定がない場合は No_Fileが指定されたものとみなす
 			 * _opFile はウェブ側でファイル送信をしたかどうかのフラグ	
              */
             String opr = Gear.strHash(htb,"_opFile");	
 			if(isEmpty(opr)){
 				opr= "No_FILE";
 			}
 			/*
 			 * 以下の処理は不要だが古いバージョンとの互換性のためにのこしてある．
 			 * この時点でアップロードファイルは全てsavedirに保存されていて、ディレクトリ名もParam.UPLOAD_DIR_KEYという
 			 * キーでシステムハッシュに保存されているので取り出しは可能である．
 			 *  
 			 * if(!opr.equals("FILE")){ Gear.delDir(savedir); } のみ残せばよい
 			 */
 			int 		filecount 	= 	0;
 			if(opr.equals("FILE")){
                 /* filesは全てのファイル名と保存ディレクトリ名、タイプを記録している*/
                 Enumeration files 	=	multi.getFileNames();                   // ファイル名（日本語対応済み）
                 while   (files.hasMoreElements()) {
                     String pname = (String)files.nextElement();             // name = "_fileName"
                     String filename = multi.getFilesystemName(pname);		// null のケースもあり得る
 					//
                     if(filename!=null){
 						htb.put("_onlyFilename",filename);                  // ファイル名のみ
                     	htb.put("_fileName",savedir + filename);            // フルパス（"_classFile",ファイル名）の対
                     	htb.put("_upfileFullPath",savedir + filename);    	// 同上（PCvar.java ではこちらを定義）
 						filecount++;										// ファイル数をカウントアップする
                 	}
                 }
 			}else{
 				Gear.delDir(savedir);	// ファイル入力名なければ不要なので消しておく 2003.10.28
 			}
 			String cnt = String.valueOf(filecount);
 			htb.put("_counts",cnt);
 			//
         }catch (Exception e) {
 			Gear.delDir(savedir);												// 作業ディレクトリを消去する
 			htb.put("_errorMsg","MultiInput(): マルチパート入力エラー");
 			e.printStackTrace();
 			return null;
         }
 		return htb;	// ハッシュテーブルを明示的に返す
 	}
 	//
 	// 一時的なシステムテンポラリディレクトリ名を生成する
 	//
 	String getTempdir(Param para){
 		String path = para.getTempDir();
 		//String path = "/home/pc/temp/";
 		//
 		return	getTempdir( path);
 	}
 	//　-STATIC-
 	// 一時的なディレクトリ名を生成する
 	//
 	public static String getTempdir(String path){
 		//
 		Random    r = new  Random(); // ミリ秒単位の現在時刻をシードとして乱数を発生
 		//
 		String s1 = "123456789";
 		String s2 = "abcdefghkmnprstwxyz";
 		int ln1   = s1.length();
 		int ln2   = s2.length();
 		//
         String p1,p2,p3,p4,p5,p6,p7,p8;
 		int pos;
 		pos = r.nextInt(ln2);	p1	= s2.substring(pos,pos+1);
 		pos = r.nextInt(ln2);	p2	= s2.substring(pos,pos+1);
 		pos = r.nextInt(ln2);	p3	= s2.substring(pos,pos+1);
 		pos = r.nextInt(ln2);	p4	= s2.substring(pos,pos+1);
 		pos = r.nextInt(ln1);	p5	= s1.substring(pos,pos+1);
 		pos = r.nextInt(ln1);	p6	= s1.substring(pos,pos+1);
 		pos = r.nextInt(ln1);	p7	= s1.substring(pos,pos+1);
 		pos = r.nextInt(ln1);	p8	= s1.substring(pos,pos+1);
 		//
 		String dirname = path + p1 + p2 + p3 + p4 + p5 + p6 + p7 + p8 + File.separator;
 		return dirname;
 	}
 	     
 	//
     //簡易暗号化処理(数字文字列しか暗号化できない)
     String cryptoDigit(String dig){
         //
         Random r    =   new  Random();
         int    pos;
         //
         StringBuffer    encrypted   = new StringBuffer(100);
         for(int i=0; i<dig.length(); i++){
             String  s0 = String.valueOf(dig.charAt(i)); 
             String  s1 = (String)ech.get(s0);   // 最初の10文字が数字のエンコード
             //if(DBG) System.out.println("(" + s0 + "," + s1 + ")");
             encrypted.append(s1);
             //
             pos = 9 + r.nextInt(50);            // CRIPTOの１０番目以降からランダムに１文字取って加える
             encrypted.append( CRIPTO[pos] );    // したがって 奇数番目の文字はダミー（0 origin）
         }
         return encrypted.toString();
     }
     String decryptoDigit(String crpt){
         //
         StringBuffer    decrypted   = new StringBuffer(100);
         for(int i=0; i<crpt.length(); i++){
             String  s1 = String.valueOf(crpt.charAt(i));
             String  s0 = (String)dch.get(s1);
             if(i%2==0) decrypted.append(s0);    // 偶数番目のみとる
         }
         return decrypted.toString();
     }
 	// 文字列が空かどうかテストする
 	//
 	boolean isEmpty(String str){
 		if(str==null) 			return  true;
 		if(str.length()==0)		return  true;
 		return false;
 	}

	/**
 	 * 
 	 * @param key
 	 * @param dp
 	 * @param para
 	 * @return
 	 */    
     String	getFilePath(String key, Dispatch dp, Param para) {
     	
     	String	fname	=	dp.getHtml(key);
     	if(Gear.isEmpty(fname)){
     		String	msg1	=	"★ PowerCampusST #getFilePath: 表示するファイル名が見つからない";
     		String	msg2	=	"    key   = " + key;
     		LOG.println(msg1);
     		LOG.println(msg2);
     		
     		return	"";
     	}
 		/* 完全パス名を返す */
     	return	para.getHtmlPath() + fname; 
     }

     
 }
