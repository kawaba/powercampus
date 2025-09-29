/*
 *# (11) BBS
#
#
# 1. profile
profilehome           = /home/group/
homeurl               = /web/
profile               = profile
#
# 2. directory
bbshome               = /home/bbs/
 * 
 * 
 */
package jbbs;
import java.io.*;
import java.util.*;
import	tktools.*;
/**
 *
 */
public class BbsConf{
	
	public final String	FS 			= File.separator;
	
	/**
	 * jbbs パッケージを呼び出す教師用、学生用画面のWEBは hidden 
	 * 項目として以下の名前で漢字氏名を持っていなくてはならない.
	 * BbsInfo では値は htb に格納されていることを前提する 
	 */
	public	static	final	String	TEACHER_KEY	="_user_name";
	public	static	final	String	STRDENT_KEY	="_kname";
		
	
	/** コンフィギュレーションファイルへのパス */
	String		BbsConfPath;
	
	/**
	 * サーバーのIPアドレス（ダイナミックDNSではいつも同じではない） 
	 * post リクエストの最初に以下のような処理で作成しておいたものを受け取る  
	 *  
	 * String SEVER_IP	= Gear.getServerIP(request);
     * htb.put("_server_ip",SEVER_IP);
	 * 
	 * 
	 */
	String		serverIP;
	
	/** パラメータオブジェクト */
	Parameter 	prop;
	
	/** TOMCATディレクトリ */
	String		tomcat_home;
	
	/** htmlファイルへのパス */
	String		htmlPath;

	/** htmlファイル名ハッシュ */
	Hashtable	htmls;
	
	/**
	 *  BBS関連データファイルのホームディレクトリ
	 * 
	 *  ex. /home/bbs/
	 */
	String		bbshome;
	
	/**
	 * BBSディレクトリへのURL（ appache におけるエイリアス）．実際のパスは bbshome (= /home/bbs/)
	 *   ex.  /bbs
	 */
	String		bbsURL;
	
	/**
	 * プロファイルディレクトリ
	 *
	 *  ex. /home/group/
	 *
	 * [profilehome]/[グループ名]/[ユーザーID]/　
	 *   が個人のプロファイルディレクトリ． ウェブフォルダとして公開
	 *   アクセスはシステム経由のみ．web 実習用に利用可能
	 * 
	 * [profilehome]/[グループ名]/[ユーザーID]/[profilename] 
	 *   にプロファイル用のアイコンデータを入れる
	 */
	String		profilehome;

	/** 
	 * 個人用ディレクトリへのURLエイリアス（ appache におけるエイリアス）．実際のパスは profilehome(= /home/group)
	 *   ex.  /web
	 */
	String		homeURL;

	/**
	 * プロファイルディレクトリ名
	 */
	String		profile;
	
	/**
	 * BBS システムの標準アイコンなどのデータを入れておくディレクトリ
	 */
	String		systemFilesDIR;
	String		systemFilesURL;
	
	String		systemIconsDIR;
	String		systemIconsURL;

	/** PMLテンプレートファイルへのパス */
	String		pmltemplate;
	

	
	public	BbsConf(String BbsConfPath,String serverIP)  throws FileNotFoundException,IOException{


		this.BbsConfPath	= BbsConfPath;
		this.serverIP		= serverIP;
		htmls 				= new Hashtable(100);
		prop    			= new Parameter(BbsConfPath);

		/* ディレクトリ構成 */
		bbshome		=	prop.get("bbshome");	
		profilehome	=	prop.get("profilehome");	
		profile		=	prop.get("profile");
		
		/* 個人ディレクトリへのＵＲＬ －web公開とprofileを含む- */
		homeURL		=	prop.get("homeurl");

		/* BBSディレクトリへのＵＲＬ */
		bbsURL		=	prop.get("bbsurl");
		
		/* BBS システムの標準アイコンなどのデータを入れておくURL   /bbs/files/ */
		systemFilesDIR	=	prop.get("bbsfilesdir");
		systemFilesURL	=	prop.get("bbsfilesurl");

		systemIconsDIR	=	prop.get("bbsiconsdir");
		systemIconsURL	=	prop.get("bbsiconsurl");
		
		/* MLテンプレートファイルへのパス */
		pmltemplate		=	prop.get("bbstemplate");
		
	}
	/*
	 * システムの標準データファイル名
	 */
	public	String	sysFaceIcon(){
		return		"sysFaceIcon.gif";
	}
	
	/*
	 * 各種のパスを返す
	 */
	/**
	 * BBS システムのデータを入れておくディレクトリ
	 */
	public	String	sysFileDir()	{
		return	 systemFilesDIR;
	}
	public	String	sysFileUrl()	{
		return	 systemFilesURL;
	}
	/**
	 * BBS システムの標準アイコンを入れておくディレクトリ
	 */
	public	String	sysIconDir()	{
		return	 systemIconsDIR;
	}
	public	String	sysIconUrl()	{
		return	 systemIconsURL;
	}
		
	/**
	 * オーナーディレクトリへのパスを返す
	 * WEB 用に公開されるディレクトリ
	 * 
	 * @param ownerkey	オーナーID
	 * @param szDB		グループ名
	 * @return			オーナーディレクトリへのパス
	 */
	public String	ownerDir(String ownerkey, String szDB){
		return		profilehome + szDB + FS + ownerkey + FS;
	}
    /**
     * フォーラムの説明に添付するデータをポストするディレクトリへの完全パスを得る
	 * @param ownerkey		オーナーID
     * @param forumkey　	フォーラムキー
     * @return				フォーラムディレクトリへのパス
     */
	public	String	forumDir(String ownerkey,String forumkey){
		return		bbshome + ownerkey + FS + forumkey;
	}
	public	String	forumUrl(String ownerkey,String forumkey){
		return		bbsURL + ownerkey + "/" + forumkey;
	}
	/**
	 * スレッドの説明に添付するデータをポストするディレクトリへの完全パスを得る
	 * @param ownerkey		オーナーID
     * @param forumkey　	フォーラムキー
	 * @param threadkey	スレッドキー
	 * @return				スレッドディレクトリへのパス
	 */
	public	String	threadDir(String ownerkey,String forumkey, String threadkey){
		return		bbshome + ownerkey + FS + forumkey + FS + threadkey;
	}
	public	String	threadUrl(String ownerkey,String forumkey, String threadkey){
		return		bbsURL + ownerkey + "/" + forumkey + "/" + threadkey;
	}
	/**
	 * 記事の説明に添付するデータをポストするディレクトリへの完全パスを得る
	 * @param ownerkey		オーナーID
     * @param forumkey　	フォーラムキー
	 * @param threadkey	スレッドキー
	 * @param postkey		投稿キー
	 * @return				投稿記事ディレクトリへのパス
	 */
	public	String	postDir(String ownerkey,String forumkey, String threadkey, String postkey){
		return		bbshome + ownerkey + FS + forumkey + FS + threadkey + FS + postkey;
	}
	public	String	postUrl(String ownerkey,String forumkey, String threadkey, String postkey){
		return		bbsURL + ownerkey + "/" + forumkey + "/" + threadkey + "/" + postkey;
	}
	/**
	 * プロファイルディレクトリへの完全パスを返す<br>
	 *
	 *  (ex.) /home/group/kwd/d500133/profile/
	 * 
	 * @param ownerkey	ユーザーID
	 * @param szDB		グループ名
	 * @return			プロファイルディレクトリへの完全パス
	 */
	public	String	profilePath(String ownerkey, String szDB){
		return		ownerDir(ownerkey, szDB) + profile + FS;
	}

	/** プロファイルディレクトリへのエイリアスＵＲＬ を返す */
	public	String	profileURL(){
		return	homeURL;
	}
	/** プロファイルディレクトリへのＵＲＬ をWebのエイリアス名で返す  */
	public	String	userProfileURL(String ownerkey, String szDB){
		return		homeURL + szDB + "/" + ownerkey + "/" + profile + "/";
	}

	/** (現在の)サーバーIPアドレスを返す */
	public String ipAddress()	{
		return	serverIP;			
	}
	/** プロファイルディレクトリへのＵＲＬ をＩＰアドレスで返す */
	public	String	userProfileURL_IP(String ownerkey, String szDB){
		return	"http://" + serverIP + "/" + homeURL + "/" + szDB + "/" + ownerkey + "/" + profile + "/";
	}

	/** PMLテンプレートファイルへのパスを返す */
	public	String	pmlTemplatePath(){
		return	pmltemplate;
	}


}
