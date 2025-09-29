
package stkadai;


import java.io.*;
import java.util.*;
import database.*;
import framework.*;

import	epml.*;
import	jbbs.BbsInfoDB;
import kadai.KadaiApRecord;
import kadai.KadaiDefRecord;
import kadai.KadaiInfo;
import tktools.*;

/**
* 
*	レポート課題作成の指示を見る
*
	#
	# ##################
	#     Sample
	# ##################
	#
	<program $stkadai.StKadaiExp>
		<dispatch  html=StKadaiExp.html  number=5830  class=stkadai.StKadaiExp />
		<variable>
		  <receive   NUMBER  STAMP  GROUP StUID  StNAME   TUID  aplec_key lec_key title  kadai_key/>
		  <accept    CMD />
		  <keep      />
		  
		  <form      />
		</variable>
	</program> 
*
*
* 変数の説明
*
* 1. receive 
* 2. accept
* 3. keep
* 4. form
*
* 
*/
public class StKadaiExp extends SuperPlayer {
	//



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
	
	
	
	String		exp;
	
	String		szDB;
	String 		teUid;
	String		te_lec_key;
	String		te_aplec_key;
	String		kadai_key;
	String		stNumber;
	String		aplec_key;
	String		lec_key;
	
	
	String		disposal;		// 提出状況：KadaiInfo.NOTYET,WORKING,SUBMITTED,GRADED
	String		points;			// 得点（採点が済んでいればここに得点がある）
	String		submitTime;		// 提出時間
		
	public	StKadaiExp(){
		super();
		if(LOG.fa) LOG.println("■ StKadaiExp #コンストラクタ");
	}
	
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		
		broker			=	getDbConnection();
		db				=	new Database(broker);			

		szDB			= 	strHash(htb,"_szDB");
		teUid			= 	strHash(htb,"_teUid");
		te_lec_key		= 	KeyGen.get_te_lec_key(htb);
		te_aplec_key	= 	KeyGen.get_te_aplec_key(htb);
		kadai_key		= 	strHash(htb,"_kadai_key");
		stNumber		= 	strHash(htb,"_stNumber");
		aplec_key		= 	strHash(htb,"_aplec_key");
		lec_key			= 	strHash(htb,"_lec_key");
		//
		KadaiDefRecord 	rec		= new KadaiDefRecord(te_lec_key, kadai_key, db);
		exp						= rec.content();	// レポート課題の作成指示内容


	}
	public	String	dispatch(){
		if(LOG.fa) LOG.outHash(htb,"■Sample #dispatch()");
		if(LOG.fa) LOG.println("■Sample #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;

		if(cmd.equals("VIEW")){
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
			

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
		if(LOG.fa)	LOG.println("■ Sample #display(boolean editmode)");
		
		if(!editmode){
			putParameter(MESSAGE,"");
		}
		KadaiDefRecord 	rec		= new KadaiDefRecord(te_lec_key, kadai_key, db);
		htb.put("_kadai_title", rec.title());
		
		KadaiApRecord kar	= new KadaiApRecord(te_aplec_key,kadai_key,db);
		//
		htb.put("_syy",kar.s_yyyy());
        htb.put("_smm",kar.s_month());
        htb.put("_sdd",kar.s_day());
        htb.put("_shh",kar.s_hour());
        htb.put("_stt",kar.s_minute());
        //
        htb.put("_eyy",kar.e_yyyy());
        htb.put("_emm",kar.e_month());
        htb.put("_edd",kar.e_day());
        htb.put("_ehh",kar.e_hour());
        htb.put("_ett",kar.e_minute());
		//
        String	msg	=	"";
		htb.put(MESSAGE,msg);
		if(isEmpty(exp.trim())){
			msg	=	"◎ レポート作成についての詳細な指示はありません";
			htb.put(MESSAGE,msg);
		}
		/*
		 * 得点と提出時間
		 */
		setInfo();
		htb.put("_p",points);
		htb.put("_time",submitTime);

		//////////////////// 提出状況の可視化 //////////////////////////////
		putParameter("kubun",disposal);
		putParameter("con", KadaiInfo.MSG[Integer.parseInt(disposal)]);
		////////////////////////////////////////////////////////////////////
		
		//
		htb.put("_exp",getContent() );		// 生成した html を返す（またはエラーメッセージ）		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}
    /**
     * 提出状況のセット
     *
     */
	void	setInfo(){
		KadaiInfo	kdf	= new KadaiInfo(szDB,db);
		kdf.set_keys(stNumber,te_aplec_key,kadai_key);
		
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
	/**
	 * PMLをパースしたHTMLを返す
	 * @param content	内容を表すPML
	 * @return			同 HTML
	 */
	public	String	getContent(){
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
		String cont	=	 convert(exam.createHtml());
		/*
		 * \ " ' を元の文字に戻す
		 */
		return	Gear.toNormalString(cont);
	}
	/**
	 * ユーザーIDからユーザー情報をハッシュテーブルで得る
	 * 存在しない時は null を返す
	 * 
	 * @return
	 */
	Hashtable	getInfoRecord(String uid){
		if(LOG.fa) LOG.println("■ BbsPost #getInfoRecord()");

		BbsInfoDB	infodb	=	new BbsInfoDB(uid, szDB, broker);
		Hashtable	dt		=	new Hashtable();
		int		count	=	infodb.readBbsInfo(uid, dt);
		if(count>0)	return	dt;
		return			null;
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
