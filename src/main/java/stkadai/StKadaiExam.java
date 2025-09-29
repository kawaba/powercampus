package stkadai;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Vector;
import database.Database;
import database.DbConnectionBroker;
import database.KeyGen;
import epml.Exam;
import framework.LOG;
import framework.Param;
import framework.SuperPlayer;
import jbbs.BbsInfoDB;
import kadai.AnswerRecord;
import kadai.KadaiApRecord;
import kadai.KadaiDefRecord;
import kadai.KadaiInfo;
import tktools.Csv;
import tktools.DateGear;
import tktools.Gear;
import tktools.tkException;

//
/**
 * 学生用の課題解答および参照処理<br>
 * 
 * 試験中の一時保存に一時ファイルを使う．一時ファイルには、オブジェクト書き込みとして
 * システムハッシュから変数を取り出し、問題であるepml原文に埋め込んだテキストを書き出す
 *
 *
 *
	#
	# ##################
	#     StKadaiExam
	# ##################
	#
	<program $stkadai.StKadaiExam>
		<dispatch  html=StKadaiExam.html  number=2520  class=stkadai.StKadaiExam />
		<variable>
		<receive     NUMBER STAMP GROUP StUID TUID  aplec_key lec_key title kadai_key clock />
		  <accept    CMD    UPLODE  />
		  
		  <form      />
		</variable>
	</program> 
 *
 *
 * 変数の説明
 *
 * 1. receive 
 * 		clock ----  開始時のタイムスタンプ
 * 　　　　　　　　 StKadaiExam.htmlのjavaScriptで、残り時間を計算するのに利用する
 * 
 * 2. accept
 * 3. keep
 * 4. form
 *
 * 
 */
public class StKadaiExam extends SuperPlayer {

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
	String		lec_key;
	String		te_lec_key;
	String		te_aplec_key;
	String		kadai_key;
	String		stNumber;
	String		aplec_key;
	//
	KadaiDefRecord 	rec;		// 課題定義レコード
	KadaiApRecord	kar;		// 課題実施レコード
	Exam			examObj;	// 試験問題
	//
	int			cnt;			// この課題に解答の記録があるかないか
	String		shubetsu;		// 課題の種別（課題定義レコードＤＢから引く）
	String		subject;		// 課題名
	String		disposal;		// 提出状況：KadaiInfo.NOTYET,WORKING,SUBMITTED,GRADED
	String		points;			// 得点（採点が済んでいればここに得点がある）
	//
	String		submitTime;		// 提出時間
	String		timeLimit;		// 秒単位の制限時間
	//
	boolean	tempFlag;		// 一時ファイルに記録があるかどうか
	//
	final int	 	STARTED			= 0;	// 課題提出期間が既に開始している 
	final int	 	NOT_READY		= 1; 	// 課題提出期間が設定されていない
	final int	 	NOT_STARTED		= 2; 	// 課題提出期間前である	
	
	public	StKadaiExam(){
		super();
		if(LOG.fa) LOG.println("■ StKadaiExam #コンストラクタ");
	}
   
	@Override
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
		/*
		 * 課題情報をセットする
		 */
		setInfo();
		/*
		 * exampaper.html が使う制限時間をシステムハッシュに入れておく
		 */
		examObj		=	new Exam(rec.content(),para.getEmlConfPath(),true);
		timeLimit	=	String.valueOf( examObj.getTimeLimit() );		// 秒単位の制限時間
		htb.put("_timeLimit",timeLimit);
		
    }

	/** 提出、得点、提出期日を得る */
	void	setInfo(){
		if(LOG.fa)  LOG.println("■■ StkadaiExam #setInfo()");
		KadaiInfo kdf	= new KadaiInfo(szDB,db,stNumber,te_aplec_key,kadai_key);
		cnt = kdf.read_KadaiInfo();
		if(cnt==0){
			disposal	= KadaiInfo.NOTYET;
			points		= "-";
			submitTime	= "";
		}else{
			disposal	= kdf.saiten_flag();
			points		= kdf.points();
			if(points.equals("")){	points	= "-"; }
			submitTime	= kdf.latest_date_str();
		}
	}	
	/** 
	 * 処理分岐<br>
	 * 
	 */
	@Override
	public	String	dispatch(){
		if(LOG.fa) LOG.println("■StKadaiExam #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		if( cmd.equals("SAVE")){
			/*
			 * 問題を保存する（epml テキストをここで生成して書き込んでいる）
			 * KadaiInfo は更新しないでそのまま継続する
			 */
			//writeKadaiInfo();
		    wrtTemp();				
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
			
		}else if(cmd.equals("END")){
			/*
			 * 書き込んで終了する
			 */
		    doEnd();
			/*
			 * 講義画面まで一気に戻る 
			 */
			putParameter(GOBACK_KEY, "$student.StKougi");
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_GOBACK;

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
	/**
	 * 終了処理
	 * オーバーライドできる
	 *
	 */
	void	doEnd(){
		writeKadaiInfo();
		sub_wrt();
	    
	}
	/**
	 * KadaiInfo に状態を書き込む<br>
	 */
    void	writeKadaiInfo(){
		if(LOG.fa)  LOG.println("■■ StkadaiExam #writeKadaiInfo()");

		String				stamp	=	strHash(htb,"_clock");	
		GregorianCalendar	start	=	getCalendar(stamp);
		GregorianCalendar	finish	=	currentDay();
		if(LOG.fa) isOKTimeTest(start,finish);
		
		disposal		=	KadaiInfo.SUBMITTED;	// 提出する
		
		long	sec		=	DateGear.difSeconds(start, finish);
		String	mmss	=	DateGear.secToMinutes(sec);
		submitTime		=	DateGear.getDateFromCal("MM/dd HH:mm:ss",finish) + " (" + mmss + ")";
		//
		/* 新規に作成したほうが安全 */
		KadaiInfo	kdf	= new KadaiInfo(szDB,db,stNumber,te_aplec_key,kadai_key);
		kdf.add_data( shubetsu, disposal, submitTime, subject, points );
		if(isNew()){
			kdf.insert_KadaiInfo();
		}else{
			kdf.update_KadaiInfo();
		}
		if(LOG.fa)	LOG.outVector(kdf.getRec(),"■ 登録された課題提出情報の内容");
    }
    void	isOKTimeTest( GregorianCalendar start, GregorianCalendar finish ){
		if(LOG.fa)  LOG.println("■■ StkadaiExam #isOKTimeTest()");

		LOG.println("■■ 経過時間チェック");
    	LOG.println("  □□ start          :" + getFormattedDate(start,  "yyyy/MM/dd HH:mm:ss"));
		LOG.println("  □□ finish         :" + getFormattedDate(finish, "yyyy/MM/dd HH:mm:ss"));
		LOG.println("  □□ finish - start :" + DateGear.difSeconds(start,finish));
		LOG.println("  □□ limit          :" + getInitTimer());
    }
	/**
	 * Csv文字列からカレンダーオブジェクトを得る
	 * @param 		csv		csv文字列　例　2004,12,13,13,30,28
	 * @return		カレンダーオブジェクト
	 */
	GregorianCalendar	getCalendar(String csv){
		if(LOG.fa)  LOG.println("■■ StkadaiExam #getCalendar()");
		
		int	YY;
		int	MM;
		int	DD;
		int	hh;
		int	mm;
		int	ss;
		Csv	cs	=	new Csv(csv);
		if(cs.size()>=6){
			YY	=	Integer.parseInt(cs.get(0));
			MM	=	Integer.parseInt(cs.get(1));
			DD	=	Integer.parseInt(cs.get(2));
			hh	=	Integer.parseInt(cs.get(3));
			mm	=	Integer.parseInt(cs.get(4));
			ss	=	Integer.parseInt(cs.get(5));
			return	new GregorianCalendar(YY, MM, DD, hh, mm, ss);
		}else if(cs.size() >=5 ){
			YY	=	Integer.parseInt(cs.get(0));
			MM	=	Integer.parseInt(cs.get(1));
			DD	=	Integer.parseInt(cs.get(2));
			hh	=	Integer.parseInt(cs.get(3));
			mm	=	Integer.parseInt(cs.get(4));
			return	new GregorianCalendar(YY, MM, DD, hh, mm);
		}else if(cs.size()>=3){
			YY	=	Integer.parseInt(cs.get(0));
			MM	=	Integer.parseInt(cs.get(1));
			DD	=	Integer.parseInt(cs.get(2));
			return	new GregorianCalendar(YY, MM, DD);
		}
		return	null;
	}    

	/** 
	 * この課題に解答の記録があるかないか<br>
	 */
	public boolean	isNew(){
		if(LOG.fa)  LOG.println("■■ StkadaiExam #isNew()");
		KadaiInfo kdf	= new KadaiInfo(szDB,db,stNumber,te_aplec_key,kadai_key);
		cnt = kdf.read_KadaiInfo();
		boolean	flag	=	false;
		if(cnt==0){
			flag	=	true;
		}
		return	flag;
	}
    /** 
     * 解答を書き込む<br>
	*/
    public void sub_wrt(){
		if(LOG.fa)  LOG.println("■■ StkadaiExam #sub_wrt()");
		/*
		 *  解答EPMLを生成してハッシュ（answer）にセットする
		 */ 
		String	exampaper	=	setAnswerEPML();
		htb.put("_answer",exampaper);
		/*
		 * ディレクトリがなければ作成する 
		 */
		File fp	=	new File(para.kadaiAnsDir(teUid, aplec_key, kadai_key));
		if(!fp.exists()){
			fp.mkdirs();
		}
		/* 
		 * ハッシュに時間をセットする
		 */
		htb.put("_updateTime",submitTime);
		/*
		 * システムハッシュからレコードを構成して書き込む
		 */
		AnswerRecord	ans		= new AnswerRecord(htb); 
		String			path	= para.stFilePath(teUid,aplec_key,kadai_key,stNumber);// 提出ファイルへのフルパス
        boolean      	chk  	= writeAnswer(path,ans);
        if(!chk) {
        	String	msg	=	"class StkadaiExam #sub_wrt() : ★ 解答をファイルに書けません.:path="+path;
        	LOG.errStop(out, msg);
        }else{
            //putParameter(MESSAGE,"★ 解答を保存しました");
        }
    }
    /** 
     * 解答を書き込む<br>
	*/
    public void wrtTemp(){
		if(LOG.tr)  LOG.println("■■ StkadaiExam #sub_wrt()");        //
		/*
		 *  解答EPMLを生成してハッシュ（answer）にセットする
		 */ 
		String	exampaper	=	setAnswerEPML();
		htb.put("_answer",exampaper);
		
		/* ************************************************************/ 
		
		if(LOG.tr) {
			LOG.println("exampaperを出力");		// &quot;
			LOG.println(exampaper);
		}
		
		/* **************************************************************/
		
		/*
		 * ディレクトリがなければ作成する 
		 */
		File fp	=	new File(para.kadaiAnsDir(teUid, aplec_key, kadai_key));
		if(!fp.exists()){
			fp.mkdirs();
		}
		/* 
		 * ハッシュに時間をセットする
		 */
		String		stamp			=	strHash(htb,"_clock");	
		GregorianCalendar	start	=	getCalendar(stamp);
		GregorianCalendar	finish	=	currentDay();
		long	sec		=	DateGear.difSeconds(start, finish);
		String	mmss	=	DateGear.secToMinutes(sec);
		String	time	=	DateGear.getDateFromCal("MM/dd HH:mm:ss",finish) + " (" + mmss + ")";
		htb.put("_updateTime",time);
		/*
		 * システムハッシュからレコードを構成して書き込む
		 */
		AnswerRecord	ans		= new AnswerRecord(htb);
		
		/* ************************************************************/ 
		
		if(LOG.tr) ans.printRecord();	// &quot;
		
		
		/* **************************************************************/
		
		
		String			path	= para.stFilePath(teUid,aplec_key,kadai_key,stNumber);// 提出ファイルへのフルパス
        boolean      	chk  	= writeAnswer(path,ans);
        if(!chk) {
        	String	msg	=	"class StkadaiExam #sub_wrt() : ★ 解答をファイルに書けません.:path="+path;
        	LOG.errStop(out, msg);
        }else{
            //putParameter(MESSAGE,"★ 解答を保存しました");
        }
    }

    /** ハッシュから解答EPMLを生成して返す */
	String	setAnswerEPML(){
		if(LOG.fa)  LOG.println("■■ StkadaiExam #setAnswerEPML()");
				
		Exam	exam		= new Exam(rec.content(),para.getEmlConfPath());
		/*
		 * 解答を表すEPMLテキストを初期生成する
		 */
		String	exampaper	= exam.createAnswerText(htb);

		return	exampaper;
	}


	/**
	 * Answer オブジェクトをファイルに書き出す
	 * @param path		解答ファイルパス名
	 * @param ans		AnswerRecord(解答レコード)
	 * @param para		システムパラメータオブジェクト
	 * @param htb		システムハッシュ
	 * @return			成功すると true 失敗すると false を返す
	 */
    public static boolean  writeAnswer(String path,AnswerRecord ans){
		if(LOG.fa)  LOG.println("■■ StkadaiExam #writeAnswer()");
		
        ObjectOutputStream  objOut  = null;
        try{
            objOut = new ObjectOutputStream(new FileOutputStream(path));
            try{
                objOut.writeObject(ans); // Serializable
                objOut.flush();
                objOut.close();
                return true;
            }catch(IOException e1){
            	e1.printStackTrace();
            }
        }catch(IOException e2){
        	e2.printStackTrace();
        }
        return false;
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
		
		
		/* ************************************************************/ 
		
		if(LOG.tr) {
			LOG.println("answerHTMLを出力");	// " になっている
			LOG.println(answerHtml);
		}
		
		/* **************************************************************/		
		
		/*
		 * 制限時間(_timeLimit)をセットする
		 * 開始時間はWebにhidden である
		 */
		htb.put("_timeLimit", getInitTimer());
        
        /* その他の項目をハッシュにセットする */
        putKadaiTitle();
        putKadaiData();
		//putPointAndTime();
		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
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
		
		/* ************************************************************/ 
		
		if(LOG.tr) {
			LOG.println("★解答ファイルから作成したanswerを出力（変換前）"); // " になっている
			LOG.println(answer);
		}
		
		/* **************************************************************/	
		
		
		/*
		 * 表示のために \ " ' を元の文字列に戻す
		 * 
		 */
		//String html = Gear.toNormalString(answer);  //2023.3.29  削除
		String html = answer;
		
		/* ************************************************************/ 
		
		if(LOG.tr) {
			LOG.println("★★解答ファイルから作成したanswerを出力（変換後）");// " になっている
			LOG.println(html);
		}
		
		/* **************************************************************/			
		return	html;
		
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
	/** 一度書き込んだ場合の学生の解答HTMLをファイルを読込んで返す */
	String	getHtml(File fp){
		if(LOG.fa)  LOG.println("■■ StkadaiExam #getHtml()");

		String	html;
		try{
			html		=	getAnswerHtml( fp );
		}catch(tkException e){
			html	="□ 指定された解答レコードが見つかりません";
		}
		return	html;
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
		
		
		// 解答EPMLからExamを作る。その際、&quot;などの特殊文字は、ノーマルな文字に直される
		Exam	st_exam		= 	new Exam(answerText,para.getEmlConfPath());			
		
		
		st_exam.doEPML();
		
		
		
		/*
		 * 中間テキストから入力データをハッシュにセットし、
		 * それと元の問題を使って、問題が学生の解答を初期とするよう作成する 
		 */
		st_exam.setHash(htb);														// 解答をハッシュに取り出している
		Exam	exam		= new Exam(rec.content(),para.getEmlConfPath());		// 問題原文EPMLから作成
		
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
    /** 初期表示のための制限時間を秒数で返す */
    String	getInitTimer(){
		if(LOG.fa)  LOG.println("■■ StkadaiExam #getInitTimer()");
    	return	timeLimit;
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
		if(LOG.fa)  LOG.println("■■ StkadaiExam #getAnsRecod()");
		
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
    
	void putMessage(String msg){
		if(LOG.fa)  LOG.println("■■ StkadaiExam #putMessage()");

		//
		if(isEmpty(msg)){
			htb.put(MESSAGE,""); 
		}else{
			htb.put(MESSAGE,msg);
		}
	}

	void putKadaiTitle(){
		if(LOG.fa)  LOG.println("■■ StkadaiExam #putKadaiTitle()");
	
		htb.put("_kadai_title",rec.title());		
	}
	
	void putKadaiData(){
		if(LOG.fa)  LOG.println("■■ StkadaiExam #putKadaiData()");
		
		/* 試験では期間を使用しないことにしたので不要
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
		*/
		// 制限時間０分なら、時間制限をしないということ
		int	hun	=	(Integer.parseInt(timeLimit)) / 60;
		if(hun>0){
			htb.put("_tm",String.valueOf(hun));
		}else{
			htb.put("_tm"," - ");
		}
	}
	/*
	void putPointAndTime(){
		if(DBG.fa)  DBG.println("■■ StkadaiExam #putPointAndTime()");
		
		htb.put("_pt",points);
		htb.put("_time",submitTime);
	}
	*/
}
