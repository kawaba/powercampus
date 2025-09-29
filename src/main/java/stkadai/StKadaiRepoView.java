/*
 * 作成日: 2005/03/20
 *
 * TODO
 */
package stkadai;

import java.io.*;
import java.util.*;


import kadai.AnswerRecord;
import kadai.KadaiApRecord;
import kadai.KadaiDefRecord;
import kadai.KadaiInfo;
import database.*;
import epml.Exam;
import framework.*;
import	tktools.*;

/**
 *
 *
 	#
	# ##################
	#     StKadaiRepoView
	# ##################
	#
	<program $stkadai.StKadaiRepoView>
		<dispatch  html=StKadaiRepoView.html  number=2405  class=stkadai.StKadaiRepoView />
		<variable>
		  <receive   NUMBER STAMP GROUP StUID StCLASSINFO StNAME StMAIL StKEITAI  TUID aplec_key lec_key title kadai_key />
		  <accept    CMD  />
		  <keep      />
		  
		  <form      />
		</variable>
	</program>
 *
 */
public class StKadaiRepoView extends SuperPlayer{
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
	Database	db;
	
	String		html;
	
	String		szDB;
	String 		teUid;
	String		stNumber;

	String		lec_key;	
	String		aplec_key;
	String		kadai_key;

	String		te_lec_key;
	String		te_aplec_key;
	
	KadaiInfo	kdf;			// 課題情報レコード
	String		disposal;		// 提出状況：KadaiInfo.NOTYET,WORKING,SUBMITTED,GRADED
	String		points;			// 得点（採点が済んでいればここに得点がある）
	String		submitTime;		// 提出時間
	
	public	StKadaiRepoView(){
		super();
		if(LOG.fa) LOG.println("■ StKadaiRepoView #コンストラクタ");
	}
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		broker	=	getDbConnection();
		db		=	new Database(broker);
		
		szDB			= 	strHash(htb,"_szDB");
		teUid			= 	strHash(htb,"_teUid");
		te_lec_key		= 	KeyGen.get_te_lec_key(htb);
		te_aplec_key	= 	KeyGen.get_te_aplec_key(htb);
		kadai_key		= 	strHash(htb,"_kadai_key");
		stNumber		= 	strHash(htb,"_stNumber");
		aplec_key		= 	strHash(htb,"_aplec_key");
		lec_key			= 	strHash(htb,"_lec_key");
		
		kdf	= new KadaiInfo(szDB,db);
		kdf.set_keys(stNumber,te_aplec_key,kadai_key);
		
		//
		setInfo();	// 課題情報をセットする
	}
	/** 提出、得点、提出期日を得る */
	
    void	setInfo(){
		int cnt = kdf.read_KadaiInfo();
		if(cnt==0){
			disposal		= KadaiInfo.NOTYET;
			points			= "-";
			submitTime		= "-";
		}else{
			disposal	= kdf.saiten_flag();
			points		= kdf.points();
			if(isEmpty(points)){
				points	= "-"; 
			}
			submitTime	= kdf.latest_date_str();
			if(isEmpty(submitTime)){
				submitTime	= "-"; 
			}
		}
    }	
	public	String	dispatch(){
		if(LOG.fa) LOG.println("■StKadaiRepoView #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		if(cmd.equals("EDIT")){
			/*
			 *　プログラム起動
			 */
			disp_mode	=	DISP_NEW;
			ret			=	"$stkadai.StKadaiReport";

		}else if(cmd.equals("EXP")){
			/*
			 *　説明表示
			 */
			disp_mode	=	DISP_NEW;
			ret			=	"$stkadai.StKadaiExp";

		}else if(cmd.equals("RETURN")){
			/*
			 *　リーターン
			 */
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;

		}else if(cmd.equals("EXECUTE")){
			/*
			 *　プログラムの実行
			 */
			disp_mode	=	DISP_NEW;
			ret			=	getParameter(PROGRAM);

		}else{
			/*
			 *　再表示
			 */
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
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
	public  void	display(boolean editmode){
		if(LOG.fa)	LOG.println("■ StKadaiRepoView #display(boolean editmode)");
		
		if(!editmode){
			putParameter(MESSAGE,"");
		}
		/*
		 * 表示データ
		 */
		setDispData();
		/*
		 * 作成した課題をHTMLに直したもの
		 */
		setContent();
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}

	/**
	 * 解答のレポート内容をシステムハッシュにセットする
	 * レポートが未作成なら、作成指示をセットする
	 */
	void	setContent(){
		String	dispStr	=	"";
	    /*
		 * 解答を得る
		 */
		String	content			=	getReport();
		if(isEmpty(content)){
			/*
			 * レポート課題の作成指示内容
			 */
			KadaiDefRecord 	rec		=	new KadaiDefRecord(te_lec_key, kadai_key, db);
			String			exp		= 	rec.content();
			dispStr					=	getExp(exp);
			
		}else{
			/*
			 * 作成したレポート解答
			 */
		    dispStr	=	setHtml(content) ;
			
		}
		/*
		 *  \ " ' を元の文字に戻してセットする
		 */
		htb.put("_html",Gear.toNormalString(dispStr));
	}
	/**
	 * 時限、課題名、期間をシステムハッシュにセットする
	 * 科目名はtitleで与件なので自動的にセットされる
	 *
	 */
	void	setDispData(){
		htb.put("dateString",KeyGen.wdateTypeA(aplec_key));
		KadaiDefRecord 	rec		= new KadaiDefRecord(te_lec_key, kadai_key, db);
		htb.put("_kadai_title", rec.title());
		
		KadaiApRecord kar	= new KadaiApRecord(te_aplec_key,kadai_key,db);

		htb.put("_eyy",kar.e_yyyy());
        htb.put("_emm",kar.e_month());
        htb.put("_edd",kar.e_day());
        htb.put("_ehh",kar.e_hour());
        htb.put("_ett",kar.e_minute());
		/*
		 * 得点と提出時間
		 */
		htb.put("_p",points);
		htb.put("_time",submitTime);

		//////////////////// 提出状況の可視化 //////////////////////////////
		putParameter("kubun",disposal);
		putParameter("con", KadaiInfo.MSG[Integer.parseInt(disposal)]);
		////////////////////////////////////////////////////////////////////
		
	}

	void putPointAndTime(){
	}	
	/**
	* 解答を html に直したものを返す
	*/
	public	String	setHtml(String answerString){
		/*
		 * レポート課題なのでPMLのみパースしてHTMLを生成する
		 */
		Exam	exam			= new Exam(answerString,para.getEmlConfPath());

		String	graphicUrl	=	para.kadaiAttachUrl(teUid, aplec_key, kadai_key,  getParameter("stNumber"));
		String	graphicPath	=	para.kadaiAttachDir(teUid, aplec_key, kadai_key,  getParameter("stNumber"));
		exam.setNonHtmlFlag();// 資料ではなく課題データであることを exam に伝える
		exam.setImgPath( graphicUrl );
		exam.setImgDestinationPath( graphicPath );
		
		return	exam.createHtml();
	}
    /**
     * システムハッシュに表示データ（_answer）をセットする<br>
     * 保存されているファイルがなければ初期表示．<br>
     * そうでなければ、保存されているファイルから表示する<br>
     */
    String	getReport(){
		
		File	fp	=	kadaiFP();
		if(fp.exists()){
			return		(getAnsRecod(fp)).getAnswer();
		}
		/* 
		 * レポートでは課題内容は別表示なので _answer にセットしない.
		 * 課題内容は課題定義レコードにある
		 */	
		return	"";	
    }
	/**
	 * ファイルパスからAnswerオブジェクトを読みこんで返す．
	 * @param 	ufp	解答ファイルポインタ
	 * @return		answerレコード．ファイルがない場合は null を返す．
	 */
	public AnswerRecord getAnsRecod(File ufp){
		ObjectInputStream  objIn  = null;
		AnswerRecord ans = null;
		try{
			objIn = new ObjectInputStream(new FileInputStream(ufp));
			try{
				ans = (AnswerRecord)objIn.readObject();
				objIn.close();
			}catch(ClassNotFoundException e1){
				System.out.println("ClassNotFoundException:" + e1);
				ans = null;
			}catch(IOException e2){
				System.out.println("IOException:" + e2);
				ans = null;
			}
		}catch(IOException e3){
			System.out.println("can't Open :IOException:" + e3);
			ans = null;
		}
		return ans;
	}    
	/**
	 * 課題ファイルへのファイルポインタを得る
	 * @return
	 */
	File kadaiFP(){
		String dir 	= para.kadaiAnsDir(teUid,aplec_key,kadai_key);	// 例：/home/kawaba01/answer/112/000013
		File fp     = new File(dir);
		if(!fp.isDirectory()){ fp.mkdirs(); }   //ディレクトリがなければ作る
		//
		String 	path	= para.stFilePath(dir,stNumber);
		File 	ufp 	= new File(path);
		return	ufp;
	}    
	/**
	 * 課題作成指示内容をHTMLにして返す
	 * 
	 * @param content	内容を表すPML
	 * @return			同 HTML
	 */
	public	String	getExp(String exp){

		/*
		 * setImgPath() はグラフィックスへの完全URL．/user/(teUid)/file/(aplec_key)/(kadai_key)/(stNumber)/
		 * setImgDestinationPath()はグラフィックスの絶対ディレクトリパス． /home/pc/(teUid)/file/(aplec_key)/(kadai_key)/(stNumber)/ 
		 */
		Exam	exam		= 	new Exam(exp ,para.getEmlConfPath());
		String	graphicUrl	=	para.getAttachURL(teUid, lec_key, kadai_key);
		String	graphicPath	=	para.getAttachDIR(teUid, lec_key, kadai_key);
		
		exam.setImgPath( graphicUrl );
		exam.setImgDestinationPath( graphicPath );
		/*
		 * 資料データではないという設定をしておく
		 * これにより、ファイルへのリンクがイメージへのリンクと同じになる 
		 */
		exam.setNonHtmlFlag();
		return convert(exam.createHtml());
	}
	/** 本家システムであればURLをIPアドレス表記に変える */
	String	convert(String html){
		String temp	=	"";
		String product	=	para.getViewSwitch();
		if( (product.equals("SYSTEM")) || (product.equals("SYSTEM_VIEW")) ){
			String		server_ip	=	strHash(htb,"_server_ip");
			temp	=	replace(html, "http://mail-and-work.net", "http://" + server_ip);
			return	temp;
		}else{
			return	html;
		}
		
	}	
}
