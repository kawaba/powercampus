/*
 * 課題のプレビュー 320 
 *　
*/
package kadai;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Vector;
import database.Database;
import database.DbConnectionBroker;
import database.KeyGen;
import epml.Exam;
import framework.LOG;
import framework.Param;
import framework.SuperPlayer;
import tktools.Gear;


/**
 *
 *
	#
	# ##################
	#      WorkPrev
	# ##################
	#
	<program $kadai.WorkPrev>
		<dispatch  html=workPrev.html  number=320  class=kadai.WorkPrev />
		<variable>
		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA lec_key  kadai_key/>
		  <accept    CMD    UPLODE  />
		  <keep      />
		  
		  <work      />
		  <form      />
		</variable>
	</program>
 *
 * 
 *  
*/
public class WorkPrev extends SuperPlayer {


	/* ************ 
	 *  作業用変数
	 **************/
	/**
	 *  プログラムリターンコード 
	 */
	String		ret;
	/**
	 * 次の処理での表示モード 
	 */
	String		disp_mode;
	/**
	 *  処理分岐コード
	 */
	String 		cmd;
	/**
	 * データベースプール
	 */
	DbConnectionBroker	broker;
	/**
	 * ユーザー検索のためのクラス
	 */
	Database		db;

	String			kadai_key;
	KadaiDefRecord	ksd;
	
	String			teUid;
	String			lec_key;
	String			te_lec_key;
	String			kadaikey;
	
	String			content;

	
	public	WorkPrev(){
		super();
		if(LOG.fa) LOG.println("■ WorkPrev #コンストラクタ");
	}	
	/**
	 * 
	 * @param out			出力ポインタ
	 * @param htb			システムハッシュ
	 * @param para			パラメータ
	 */
	@Override
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		broker	=	getDbConnection();
		db		=	new Database(broker);
		
		teUid		=	getParameter(TUID);
		lec_key		=	getParameter("_lec_key");
		kadai_key	= 	getParameter("_kadai_key");

		// データベースから課題を読み込む
		te_lec_key	= 	KeyGen.get_te_lec_key2(teUid, lec_key);
		ksd 		= 	new KadaiDefRecord( te_lec_key, kadai_key, db);
		content		=	ksd.content();
	}	

	@Override
	public	String	dispatch(){
		if(LOG.fa) LOG.outHash(htb,"■WorkPrev #dispatch()");
		if(LOG.fa) LOG.println("■WorkPrev #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		if(cmd.equals("VIEW")){
			/* web 経由の自動起動
			 * PostKadaiPreview.html から起動される
			 */
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示
						
		}else{
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示
		}
		/*
		 * 次に表示するときあるいはリターンしてきた時の表示モードをセットしてからリターンする
		 */
		putParameter(DISP_KEY,disp_mode);
		return	ret;
		
	}
	/*////////////////////////////////////////////////////////////////////////////
	 * 
	 *    表 示 処 理
	 * 
	 * 　　    以下はコントローラーが呼び出す表示メソッドである．
	 * 　　    一般には、このクラス内のメソッドから直接呼び出さない．
	 * 
	 *////////////////////////////////////////////////////////////////////////////
	/**
	 * 画面を表示する
	 * 
	 * @param	editmode	表示モード（true なら編集、falseなら新規）
	 */
	@Override
	public  void	display(boolean editmode){
		if(LOG.fa)	LOG.println("■ WorkPrev #display(boolean editmode)");
		
		if(!editmode){
			putParameter(MESSAGE,"");
		}
		putParameter("_html", getContent());
		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}	
	/**
	 * PMLをパースしたHTMLを返す
	 * @param content	内容を表すPML
	 * @return			同 HTML
	 */
	public	String	getContent(){

		/*
		 * setImgPath() はグラフィックスへの完全URL．
		 *    /user/(teUid)/kadai/(lec_key)/(kadai_key)/
		 * 
		 * setImgDestinationPath()はグラフィックスの絶対ディレクトリパス．
		 *   P:\\pc_data\\user\\kawaba\\(lec_key)\\(kadai_key) 
		 */
		Exam	exam		= 	new Exam(content ,para.getEmlConfPath());
		String	graphicUrl	=	para.getAttachURL(teUid, lec_key, kadai_key);
		String	graphicPath	=	para.getAttachDIR(teUid, lec_key, kadai_key);
		
		exam.setImgPath( graphicUrl );
		exam.setImgDestinationPath( graphicPath );
		/*
		 * 資料データではないという設定をしておく
		 * これにより、ファイルへのリンクがイメージへのリンクと同じになる 
		 */
		exam.setNonHtmlFlag();
		/*
		 * 試験問題のみ EPML のパースを行い、レポートやファイルでは PML のパースのみ行う
		 */
		String	parsedHtml	=	"";
		String	retText		=	"";
		if(ksd.isExam()||ksd.isWriting()){
			parsedHtml	=	exam.create();
			retText		=	convert(parsedHtml);	// convertは機能していない
		}else{
			parsedHtml	=	exam.createHtml();
			retText		=	convert(parsedHtml);	// convertは機能していない
		}
		/*
		 * " ' \ を元に戻してから表示するため変換しておく
		 */
		return	Gear.toNormalString(retText);		// 無効化している
	}
	/** 本家システムであればURLをIPアドレス表記に変える　←　意味不明 */
	String	convert(String html){
		String temp	=	"";
		String product	=	para.getViewSwitch();	// USER となっているので、普通は何もしない
		if( (product.equals("SYSTEM")) || (product.equals("SYSTEM_VIEW")) ){
			String		server_ip	=	getParameter("_server_ip");
			temp					=	replace(html, "http://mail-and-work.net", "http://" + server_ip);
			return	temp;
		}else{
			return html;
		}
		
		
	}

}

