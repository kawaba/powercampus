package stkadai;
import java.io.*;
import java.util.*;
import database.*;
import framework.*;
import tktools.*;

import	epml.*;
import	jbbs.*;
import kadai.*;

//
/**
 * 学生用の課題参照処理<br>
 * 正解を見る処理は、Web の中に記述したjavaScript が別ウィンドウに起動する
 *
 *
 *
 *
	#
	# ##################
	#   StKadaiExamView
	# ##################
	#
	<program $stkadai.StKadaiExamView>
		<dispatch  html=StKadaiExamView.html  number=2530  class=stkadai.StKadaiExamView />
		<variable>
		  <receive   NUMBER STAMP GROUP StUID StNAME TUID  aplec_key lec_key kadai_key title/>
		  <accept    CMD     />
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
public class StKadaiExamView extends SuperPlayer {

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

	String		szDB;
	String 		teUid;
	String		te_lec_key;
	String		te_aplec_key;
	String		kadai_key;
	String		lec_key;
	String		stNumber;
	String		aplec_key;
	//
	KadaiApRecord	kar;		// 課題定義レコード
	KadaiDefRecord 	rec;		// 課題実施レコード
	KadaiInfo		kdf;		// 課題情報レコード
	//
	int			cnt;			// この課題に解答の記録があるかないか
	String		shubetsu;		// 課題の種別（課題定義レコードＤＢから引く）
	String		subject;		// 課題名
	String		disposal;		// 提出状況：KadaiInfo.NOTYET,WORKING,SUBMITTED,GRADED
	String		points;			// 得点（採点が済んでいればここに得点がある）
	//
	String		submitTime;		// 提出時間
	
	public	StKadaiExamView(){
		super();
		if(LOG.fa) LOG.println("■ StKadaiExamView #コンストラクタ");
	}
	/*
	 * イニシャライザ
	 *  (非 Javadoc)
	 * @see framwork.SuperPlayer#initialize(java.io.PrintWriter, java.util.Hashtable, framwork.Param)
	 */
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		
		broker	=	getDbConnection();
		db		=	new Database(broker);			

		szDB			= getParameter(GROUP);
		stNumber		= getParameter(StUID);
		teUid			= getParameter(TUID);
		lec_key			= getParameter("_lec_key");
		aplec_key		= getParameter("_aplec_key");
		
		te_lec_key		= KeyGen.get_te_lec_key2(teUid, lec_key);
		te_aplec_key	= KeyGen.get_te_aplec_key2(teUid, aplec_key);
		kadai_key		= getParameter("_kadai_key");
		//
		kar				= new KadaiApRecord(te_aplec_key,kadai_key,db);
		rec				= new KadaiDefRecord(te_lec_key,kadai_key,db);
		shubetsu		= rec.shubetsu();	// 種別
		subject			= rec.title();		// 課題名		
		
		kdf				= new KadaiInfo(szDB,db);
		kdf.set_keys(stNumber,te_aplec_key,kadai_key);
		
		cnt = kdf.read_KadaiInfo();
		/*
		 * 課題情報をセットする
		 */
		setInfo();
		
    }
	/** 提出、得点、提出期日を得る */
	void	setInfo(){
		if(cnt==0){
			disposal		= KadaiInfo.NOTYET;
			points			= "-";
			submitTime		= "-";
		}else{
			disposal	= kdf.saiten_flag();
			points		= kdf.points();
			if(isEmpty(points))	{
				points	= "-"; 
			}
			submitTime	= kdf.latest_date_str();
			if(isEmpty(submitTime)){
				submitTime	= "-"; 
			}
		}
	}
	public	String	dispatch(){
		if(LOG.fa) LOG.outHash(htb,"■Sample #dispatch()");
		if(LOG.fa) LOG.println("■Sample #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		if(cmd.equals("END")){
			/*
			 * リターンする
			 */
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;
			
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
	/**
	 * レポート・ファイル提出・試験の課題を表示する.<br>
	 * 
	 * 解答ファイルが存在しないときは問題そのものを表示するが
	 * ファイルがないというのは異常事態である
	 * 
	 * @param	editmode	表示モード（true なら編集、falseなら新規）
	 */
	public  void	display(boolean editmode){
		if(LOG.fa)	LOG.println("■ Sample #display(boolean editmode)");
		
		if(!editmode){
			putParameter(MESSAGE,"");
		}
		/* 
		 * システムハッシュに表示データ（_answer）をセットする
		 * すでに解答ファイルがあればそれから表示データを作成し、なければ
		 * 問題をデータベースから読み込んで作成する
		 */
		String	answerHtml	=	getAnswerHtml();
		htb.put("_answer"	, answerHtml);
		/*
		 * その他の項目をハッシュにセットする
		 */
        putKadaiTitle();
        putKadaiData();
		putPointAndTime();
		/* 
		 * getParameter(DISPFILE)にはファイルの完全パス名が入っている 
		 */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}
	/**
	 * 表示する問題のHTMLを作成して返す。
	 * 初期表示はDBから問題をそのまま、解答してファイルがあればそのファイルから
	 * 表示データを作成する
	 * 
	 * @return
	 */
	String	getAnswerHtml(){
		String	answer	=	"";
		File	fp	=	getStudentAnswerFP();
		if(fp.exists()){
			answer	=	getHtml(fp);
		}else{
			answer	=	getInitHtml();
		}
		return	Gear.toNormalString(answer);
		
	}
	/** 初期表示のための学生の解答HTMLを返す */
    String	getInitHtml(){
		if(LOG.fa)  LOG.println("■■ StkadaiExam #getInitHtml()");
		/*
		 * 課題定義レコードから問題（EPML）を読み込む
		 */
		rec				= 	new KadaiDefRecord(te_lec_key,kadai_key,db);
		String	pmlText	=	rec.content();
		Exam	exam2	=	new Exam(pmlText, para.getEmlConfPath());
		/*
		 * グラフィックスURLの設定
		 * 資料ではないことをexamに伝えるためにsetNonHtmlFlag()を実行しておく
		 */
		String	graphicUrl	=	para.getAttachURL(teUid, lec_key, kadai_key);
		String	graphicPath	=	para.getAttachDIR(teUid, lec_key, kadai_key);
		exam2.setImgPath( graphicUrl );
		exam2.setImgDestinationPath( graphicPath );
		exam2.setNonHtmlFlag();			
		
		return	exam2.create();
    }
	/**
	 * 学生の解答ファイルへのファイルポインタを得る<br>
	 * ディレクトリが存在しない時（ファイルも存在しないが）一応ディレクトリを作成し、ファイルポインタを作成する
	 * @return		ファイルポインタ
	 */
	File getStudentAnswerFP(){
		if(LOG.fa)  LOG.println("■■ StkadaiExam #getStudentAnswerFP()");
		
		String dir 	= para.kadaiAnsDir(teUid,aplec_key,kadai_key);	// 例：/home/kawaba01/answer/112/000013
		File fp     = new File(dir);
		if(!fp.isDirectory()){ fp.mkdirs(); }   //ディレクトリがなければ作る
		//
		String 	path	= para.stFilePath(dir,stNumber);
		File 	ufp 	= new File(path);
		return	ufp;
	}    
	/** 一度書き込んだ場合の学生の解答HTMLをファイルを読込んで返す */
	String	getHtml(File fp){
		if(LOG.fa)  LOG.println("■■ StkadaiExam #getHtml()");

		String	html;
		try{
			html		=	getAnswerHtml( fp );
		}catch(tkException e){
			html	="■■ StkadaiExam #getHtml(): 指定された解答レコードが見つかりません";
		}
		return	html;
	}

	/**
	 * 表示すべきEPMLを課題ファイルから得、html を作成して返す<br>
	 * 解答ファイルが存在しないと例外 tkException を発生する
	 * @return		解答のHTMLデータ
	 */
	String getAnswerHtml(File ufp) throws tkException {
		if(LOG.fa)  LOG.println("■■ StkadaiExam #getAnswerHtml()");
		/*
		 * 学生の解答EPMLを取り出す
		 */
		String 	answerText;
		try{
			String		pmlText	= 	getAnswerText(ufp);
			answerText 			= 	pmlText;
			 
		}catch(tkException e){
			throw e;
		}
		/*
		 * 解答EPMLをパースして中間テキストを作成する
		 */
		Exam	st_exam		= 	new Exam(answerText,para.getEmlConfPath());			// 解答ＥＰＭＬファイルから作成
		st_exam.doEPML();
		/*
		 * 中間テキストから入力データをハッシュにセットし、
		 */
		st_exam.setHash(htb);
		/*
		 * それと作成元の問題（問題原文EPML）とシステムハッシュを使って、
		 * 問題が学生の解答を初期とするよう作成する 
		 */
		Exam	exam		= new Exam(rec.content(),para.getEmlConfPath());
		
		String	graphicUrl	=	para.getAttachURL(teUid, lec_key, kadai_key);
		String	graphicPath	=	para.getAttachDIR(teUid, lec_key, kadai_key);
		exam.setImgPath( graphicUrl );
		exam.setImgDestinationPath( graphicPath );
		exam.setNonHtmlFlag();			

		String	htmlText	=	exam.update(htb);
		return	htmlText;
		
	}
	/**
	 * pmlテキストの先頭に書式情報を付加する
	 * @param content
	 * @return
	 */
	String	addFormat(String content){
		/*
		 * pmlテキストに段落エイリアスを付加する
		 * テンプレートがあるのでそれを読み込んでアペンドする
		 */
		Hashtable	rec		=	getInfoRecord(teUid);
		String		format	=	"";
		if(rec!=null){
			format	=	Gear.strHash(rec,BbsInfoDB.FORMAT);
		}else{
			format	=	para.formatTemplate();
		}
		String		emptext	=	format + Gear.lineSeparator() + content;
		return	emptext; 	
	}	
	/**
	 * 解答テキスト・epmlテキストをファイルから読込んで返す<br>
	 * 解答ファイルが存在しないと例外 tkException を発生する
	 * @param ufp	解答テキストへのファイルポインタ
	 * @return		解答テキスト・epmlテキスト
	 */
	String	getAnswerText(File ufp) throws tkException {
		if(LOG.fa)  LOG.println("■■ StkadaiExam #getAnswerText()");
		
		String			answerText	=	"";
		AnswerRecord	ansrec		=	getAnsRecod(ufp);
		if(ansrec==null){
			throw (new tkException("解答データがありません" ));
		}
		answerText	=	ansrec.getAnswer();
		return	answerText;
	}	

	/**
	 * ファイルパスからAnswerオブジェクトを読みこんで返す．
	 * @param 	ufp	解答ファイルポインタ
	 * @return		answerレコード．ファイルがない場合は null を返す．
	 */
	public static AnswerRecord getAnsRecod(File ufp){
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


	void putKadaiTitle(){
		htb.put("_kadai_title",rec.title());		
	}
	
	void putKadaiData(){
		
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
	}
	
	void putPointAndTime(){
		
		htb.put("_pt",points);
		htb.put("_time",submitTime);
	}
	
}
