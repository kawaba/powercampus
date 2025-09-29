package jbbs;

/**
 * 変数名を保持するクラス
 */
public interface BbsVar {
    
	public static final String	BBS_OWNER_KEY	= "_bbs_owner_key";
	public static final String	BBS_RELATION	= "_relation";
	
	
	/* ********************
	 * jbbs のプログラム名 
	 * ********************/
	public static final String DISPATCH_INFO		= "$jbbs.BbsInfo";
	
	public static final String DISPATCH_FORUM		= "$jbbs.BbsForum";
	public static final String CREATE_FORUM		= "$jbbs.BbsMakeForum";
	public static final String PREV_FORUM			= "$jbbs.BbsForumPreview";
	
	public static final String DISPATCH_THREAD	= "$jbbs.BbsThread";
	public static final String CREATE_THREAD		= "$jbbs.BbsMakeThread";
	public static final String PREV_THREAD		= "$jbbs.BbsThreadPreview";

	public static final String DISPATCH_POST		= "$jbbs.BbsPost";
	public static final String CREATE_POST		= "$jbbs.BbsMakePost";
	public static final String PREV_POST			= "$jbbs.BbsPostPreview";
			
	public static final String DISPATCH_RELATION	= "$jbbs.Relation";

	
	/** Bbs コンフィギュレーションファイルへの完全パス */
	public static final String BBS_CONF_PATH	= "BBS_CONF_PATH";

	/** エディタ行数 */
	public static final String EDITOR		= "_edtorRows";

	/** ダミーキー */
	public static final String DUMMY_KEY		= "dummykey";

	/** 削除するファイル */
	public static final String DELFILE		= "_deleteFile";

	/** モード変更するファイル */
	public static final String CHG_FILE		= "_chgFile";
	
	/** ファイルを添付ファイルにするかどうか */
	public static final String ATTACH		= "_attachment";

	/** アップロードしたファイル名 */
	public static final String UPFILE		= "_uploadFile";

	/** アップロードしたファイルのURL */
	public static final String UPURL			= "_fileUrl";
	
	/** アップロードしたファイル数 */
	public static final String FCOUNT		= "_FileCounts";

	/** スイッチ＝ON */
	public static final String ON			= "ON";
	

	/** 添付ファイルにするかどうかのチェックボック名 */
	public static final String ATTACH_CKBOX	= "_attach";

/* ********************
 *  Bbsforum 関連の変数
 * ********************/

	/** アタッチであることを示すアイコンファイル名 */
	public static final String ATTACH_ICON_KEY	= "_attachIcon";
	public static final String ATTACH_ICON		= "attachIcon.gif";
	public static final String ATTACH_HTML		= "attach_html.gif";
	public static final String NORMAL_ICON		= "normalIcon.gif";
	
	/**
	 * フォーラム画面でのチェックボックス変数名
	 */
	public static final String CK_ALIVE			= "_disp";
	public static final String CK_HANDLE			= "_handle";
	public static final String CK_GROUP			= "_group";
	public static final String CK_RATING			= "_rating";
	public static final String CK_ATTACH			= "_attach";
	
	/**
	 * フォーラム状態を示すアイコンファイル名変数とアイコンファイル名
	 */
	public static final String	FORUM_STATE_ICON	=	"_signIcon";
	public static final String	OPENICON			=	"lockIconOpen.gif";
	public static final String	LOCKICON			=	"lockIcon.gif";

/* ********************
 * BbsThread 関連の変数
 * ********************/
 /**
  * ソート種別
  */
	public static final String SORT_MODE			= "_sortmode";
 	public static final String BY_UID				= "UID";		// 設置者（学生）ＩＤ
	public static final String BY_VIEW			= "VIEW";		// 閲覧数
 	public static final String BY_POST			= "POST";		// 投稿数
 	public static final String BY_RATE			= "RATE";		// 支持度

 /* ********************
  * BbsPost 関連の変数
  * ********************/
	public static final String QUOTED				= "_quoted";	// 引用された記事
	public static final String QUOTED_KEY			= "_quotedkey";	// 引用された記事のポストキー
	public static final String PAST_LINK			= "_pastLink";	// 引用された記事のポストキー

	public static final int   SEL_RATING_MAX		= 11;			// 支持度選択肢の数

	

	
}
