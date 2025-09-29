/*
 * 作成日: 2004/12/17
 *
 * TODO
 */
package framework;
import java.io.File;

/**
 *
 */
public interface PCvar {

	/** 定数 */
    public static final String 	LETTER 			= "abcdefghijklmnopqrstuvwxyz_ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String 	DIGIT  			= "1234567890";
    public static final String 	LETTERorDigit 	= ".abcdefghijklmnopqrstuvwxyz_ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    public static final String 	[] DATE			= {"月","火","水","木","金","土","日"};
    public static final String 	[] ORDER		= {"１","２","３","４","５","６","７","８"};

    
	/** 改行定数 */
	final String 					CR 				= System.getProperty("line.separator");
	/** ファイル記述子セパレータ定数 */
    final String 					FS 				= File.separator;

    /** セッション変数 */
    
    public static final String	SESSION_VARS	=	"sessionVariables";
    
    public static final String	PC_ID 			= 	"pc.id";
    public static final String	PC_ID2 			= 	"pc.id2";
    
    
    /**
     * システムメッセージ表示クラスのプログラム名と論理プログラム名
     */
    public static final String	SYS_MSG			=	"framework.Message";
    public static final String	SYS_MSG_KEY	=	"$framework.Message";
    public static final String	MSG_TITLE		=	"_title";
    public static final String	MSG_BODY		=	"_msg";	
    
    
    /** フレームワークのセッション制御でセットされる基本変数やその他の変数 */
	public static final String	NUMBER 			= "_sheet";
	public static final String	STAMP 			= "_stay";
	public static final String	GROUP 			= "_szDB";
	public static final String	DOMAIN			= "_domain";
	public static final String	UPLODE			= "_opFile";

	public static final String	PASSWORD		= "_passwd";
	public static final String	UID 			= "_userid";
	public static final String	UNAME			= "_userName";
	public static final String	MAIL 			= "_userMail";
	public static final String	HOMEURL			= "_url";
	public static final String	DIVISION		= "_division";
	
	public static final String	StUID 			= "_stNumber";
	public static final String	StPASSWD		= "_stPasswd";
	public static final String	StNAME			= "_kname";
	public static final String	StMAIL			= "_stMail";
	public static final String	StKEITAI		= "_stKeitai";
	public static final String	StCLASSINFO		= "_classInfo";
	
	public static final String	TUID 			= "_teUid";
	public static final String	TNAME			= "_userName";
	public static final String	TMAIL 			= "_teMail";
	public static final String	THKANA 			= "_user_hurigana";	
		
	public static final String	SYS_SEQUENCE	= "_sys.SEQUENCE";	// プログラムのシーケンス文字列
	public static final String	SYS_HIDDEN		= "_sys.HIDDEN";	// 全てのhiddenタグ文字列
	
	/**
	 * DIVISION の値
	 * 
	 * BbsInfoDB でも同じ値を定義している
	 * 
	 */
	public	static	final	String	DIV_SUPER		=	"0";
	public	static	final	String	DIV_TEACHER		=	"1";
	public	static	final	String	DIV_ASSISTANT	=	"2";
	public	static	final	String	DIV_STUDENT		=	"3";
	public	static	final	String	DIV_GUEST		=	"4";
	
	
	/**
	 * プログラムシーケンスにおける呼び出し元プログラム
	 */
	public static final String	PARENT			=	"_parent";
	
	/**
	 * 初期起動デフォルトページ
	 */
	public static final String	ST_DEFAULT_PAGE	=	"$student.StTable";
	public static final String	TE_DEFAULT_PAGE	=	"$teacher.Timetable";
	
	
	/** JBBS の場合の初期起動ページ （スタンドアロンモード） */
	public	static	final String	BBS_PAGE		=	"$jbbs.BbsForumSA";
	
	
	/** html の action キーワード */
	public static final String	ACTION_KEY	= "_action";
	
	
	/** 表示のためにロードするHTMLファイル名への完全パス名 */
	public static final String	DISPFILE	= "_dispFile";
	
	/** WEBの中に埋め込むエラーメッセージ・警告メッセージなど */
	public static final String	MESSAGE	= "_msg";
	
	/** システムハッシュに格納するサーバIPアドレスのキー名 */
	public static final String	SERVER_IP 	= "_server_ip";
	
	public	static final String	USER_IP		= "_user_ip";
    
	/** システムハッシュに格納するブローカーオブジェクトのキー名 */
	public static final String	BROKER		= "broker";
		
	/**
	 * 教師ID発生用のナンバーファイルへの絶対パス(regist.javaで)
	 */
	public static final String	NUMBER_FILE =	"number_file";
	
	
	/**
	 * シート番号がリクエストにない時、マルチパートインプットを実行するか否か
	 */
	public	static final String	MULTI_MODE_KEY	=	"_multi_mode";	//　キー名 
	public	static final String	MULTI_ON		=	"_multi_on";	//　実行する
	public	static final String	MULTI_OFF		=	"_multi_off";	//　実行しない
	
	
	/**
	 *  マルチパートインプットでハッシュに格納される情報のキー名
	 */	
	public	static final String	UPLOAD_FILE_NAME	=	"_onlyFilename";	// アップロードファイル名
	public	static final String	UPLOAD_DIR_NAME		=	"_savedir";			// アップロード先一時ディレクトリ（末尾にセパレータあり）
	public	static final String	UPLOAD_FILE_PATH	=	"_upfileFullPath";	// フルパス． UPLOAD_DIR_NAME + UPLOAD_FILE_NAME
	public	static final String	UPLOAD_COUNTS		=	"_counts";			// アップロードされたファイル個数

	/**
	 * （マルチパートインプット）古いバージョンとの互換性維持
	 */
	public	static final String	UPLOAD_FILE_KEY	=	"_onlyFilename";
	public	static final String	UPLOAD_CNT_KEY	=	"_counts";

	/**
	 * 処理選択のための値をシステムハッシュから得るための
	 * キー(DISPATCH_KEY)とその規定値(DISPATCH_DEFAULT)
	 * 規定値は画面（WEB）の再表示である
	 */
	public static final String	DISPATCH_KEY		=	"_menu";
	public static final String	CMD					=	"_menu";
	
	public static final String	DISPATCH_DEFAULT	=	"SELF";
	public static final String	DISPATCH_RETURN		=	"RETURN";		// ひとつ戻る
	public static final String	DISPATCH_GOBACK		=	"GOBACK";		// いくつか前に戻る
	public static final String	GOBACK_KEY			=	"_gobackKey";	// 戻り先論理プログラム名

	
	/**
	 * 任意のプログラムを実行させるキー
	 * 
	 * 　既存のプログラムでは、dispatch() において以下の文脈で任意のプログラム
	 * 　を実行するよう組み込まれている
	 * 
	 * 		}else if(cmd.equals("EXECUTE")){
	 *		    disp_mode	=	DISP_NEW;
	 *		    ret			=	getParameter(PROGRAM); // 実行したいプログラム名
	 *      }
	 */
	public static final String	EXECUTE				=	"EXECUTE";		
	public static final String	PROGRAM				=	"_programName";
	
	
	/** マルチウェブ画面で起動する論理プログラム名をシステムハッシュに設定するときのキー名 */
	public static final String	INVOKE				=	"_invokeName";
	
	
	/**
	 * Web表示処理用の値(DISP_NEW,DISP_EDIT)とそれをシステムハッシュに保存する時のキー名(DISP_KEY)
	 */
	public static final String DISP_KEY	=	"_display_mode";
	public static final String DISP_NEW	=	"NEW";	
	public static final String DISP_EDIT	=	"EDIT";
	
	/**
	 * dispatch.xml での変数区分名
	 */
	public static final String XRECEIVE	=	"receive";
	public static final String XACCEPT	=	"accept";
	public static final String XSET		=	"keep";
	
	/**
	 * セッション切れの時呼ばれるログインのプログラム名
	 */
	public static final String DISPATCH_LOGIN		=	"$login.Tlogin";	// 教師 
	public static final String DISPATCH_REGIST	=	"$login.Regist";	// 教師 

	public static final String DISPATCH_StLOGIN	=	"$login.StLogin";	// 学生

	public	static	final String DISPATCH_BBS_LOGIN		=	"$login.BbsLogin";	// BBSへのログイン
	public	static	final String DISPATCH_BBS_REGIST	=	"$login.BbsRegist";	// BBSへの登録
	
	/**
	 * 自動ログインの時にセットされるキーと値
	 */
	public static final String AUTO_LOGIN			=	"_autoLogin";		// 自動ログイン
	public static final String AUTO_LOGIN_ON		=	"ON";				// その値
	public static final String DISPATCH_TOPPAGE	=	"$student.StTable";	// 自動ログイン先プログラム名
	
	/**
	 * VarStackDB のフィールド名
	 *
	 */
	public static final String VSDB_KEY		=	"_key";
	public static final String VSDB_UID		=	"_uid";
	public static final String VSDB_DATE		=	"_date";
	
	
}
