package stkadai;

import java.io.*;
import java.util.*;

import	epml.*;
import kadai.*;
import database.*;
import framework.*;
import tktools.*;

/**
 *
 *
 	#
	# ##################
	#   CorrectAnswer
	# ##################
	#
	<program $stkadai.CorrectAnswer>
		<dispatch  html=CorrectAnswer.html  number=5880  class=stkadai.CorrectAnswer />
		<variable>
		  <receive   NUMBER STAMP GROUP StUID StNAME TUID aplec_key lec_key kadai_key title/>
		  <accept    CMD    />
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
public class CorrectAnswer extends SuperPlayer{


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
	
	String 		teUid;
	String		lec_key;
	String		te_lec_key;
	String		te_aplec_key;
	String		kadai_key;
	String		aplec_key;	
	//
	String		shubetsu;	
	String		subject;
	String		content;
	//
	KadaiDefRecord 	rec;
	KadaiApRecord 	kar;
	//
	KadaiInfo		kdf;
	String			szDB;
	String			stNumber;
	String			disposal;
	String			points;
	String			submitTime;
	
	
	public	CorrectAnswer(){
		super();
		if(LOG.fa) LOG.println("■ CorrectAnswer #コンストラクタ");
	}
	
	//
	/**
	 * イニシャライザ
	 */
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		broker	=	getDbConnection();
		db		=	new Database(broker);	
		//
		
		szDB			= getParameter(GROUP);
		stNumber		= getParameter(StUID);
		teUid			= getParameter(TUID);
		lec_key			= getParameter("_lec_key");
		aplec_key		= getParameter("_aplec_key");
		
		te_lec_key		= KeyGen.get_te_lec_key2(teUid, lec_key);
		te_aplec_key	= KeyGen.get_te_aplec_key2(teUid, aplec_key);
		kadai_key		= getParameter("_kadai_key");
		
		//
		// 課題の種別と名前
		rec			=	new KadaiDefRecord(te_lec_key,kadai_key,db);
		shubetsu	=	rec.shubetsu();	// 種別
		subject		=	rec.title();	// 課題名
		content		= 	rec.content();	// 課題の内容
		//
		kar			= 	new KadaiApRecord(te_aplec_key,kadai_key,db);
		kdf			= 	new KadaiInfo(szDB,db);
		kdf.set_keys(stNumber,te_aplec_key,kadai_key);
		int cnt 	=	 kdf.read_KadaiInfo();
		
		if(cnt==0){
			disposal		= KadaiInfo.NOTYET;
			points			= "-";
			submitTime		= "";
		}else{
			disposal	= kdf.saiten_flag();
			points		= kdf.points();
			if(points.equals("")){	points	= "-"; }
			submitTime	= kdf.latest_date_str();
		}
	}
	public	String	dispatch(){

		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;

		if(cmd.equals("VIEW")){
			/*
			 * 正解を表示する
			 */
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
			
		}else{
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
		String	text	=	"";
		if(!disposal.equals(KadaiInfo.GRADED)){
			if(LOG.fa) LOG.println("■ 採点済みではないので表示しない");
			text	=	getAlartHtml();
			
		}else{
			// 課題定義レコードから問題EPMLテキストを得て、正解を表すHTMLを生成する
			Exam	exam	= 	new Exam(content,para.getEmlConfPath());
			/*
			 * グラフィックスURLの設定
			 * 資料ではないことをexamに伝えるためにsetNonHtmlFlag()を実行しておく
			 */
			String	graphicUrl	=	para.getAttachURL(teUid, lec_key, kadai_key);
			String	graphicPath	=	para.getAttachDIR(teUid, lec_key, kadai_key);
			exam.setImgPath( graphicUrl );
			exam.setImgDestinationPath( graphicPath );
			exam.setNonHtmlFlag();				
			/*
			 *  \ " ' を元の文字に直して表示する
			 */
			text			=	Gear.toNormalString(exam.correctText());
		}
		//
		// 正解として表示する
		htb.put("_answer",text);
		htb.put("_kadai_title",subject);
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
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}
	/**
	 * 「表示できない」を表示するHTMLを得る
	 * 
	 * @return
	 */
	String	getAlartHtml(){

		String	path		=	para.getEmlConfPath();
		Property	property	=	null;
		try{
			property		=	new Property(path);	//  /var/pc/conf/exwork.conf
		}catch(IOException e){
			System.out.println("class Exam #setInitParam() : ★ 致命的なエラーです．設定ファイルが読めません.");
		}
		String		templatePath	=	property.get("wikiTemplatePath");
		TemplateBox	tb				=	new TemplateBox(templatePath);
		String 	ret	= tb.get("cannotview");
		return	ret;
	}	
}
