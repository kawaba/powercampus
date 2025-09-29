package stkadai;

import java.io.*;
import java.util.*;

import tktools.Csv;
import tktools.DateGear;
import database.*;
import epml.Exam;
import framework.*;
import kadai.*;
//
/**
 * 学生用の課題解答および参照処理<br>
 * 試験中の一時保存に一時ファイルを使う．一時ファイルには、オブジェクト書き込みとして
 * システムハッシュから変数を取り出し、問題であるepml原文に埋め込んだテキストを書き出す
 *
	#
	# #######################
	#     StKadaiStartExam
	# #######################
	#
	<program $stkadai.StKadaiStartExam>
		<dispatch  html=StKadaiStartExam.html  number=2510  class=stkadai.StKadaiStartExam />
		<variable>
		  <receive   NUMBER STAMP GROUP StUID TUID aplec_key lec_key title kadai_key/>
		  <accept    CMD    UPLODE  />
		  <keep      clock />
		  
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
* 		clock ---- 開始時のタイムスタンプ
* 4. form
*
* 
*/
public class StKadaiStartExam extends SuperPlayer {

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
	KadaiApRecord	kar;
	KadaiDefRecord	kdr;
	//
	int			cnt;			// この課題に解答の記録があるかないか
	String		shubetsu;		// 課題の種別（課題定義レコードＤＢから引く）
	String		subject;		// 課題名
	String		disposal;		// 提出状況：KadaiInfo.NOTYET,WORKING,SUBMITTED,GRADED
	String		submitTime;		// 提出時間
	String		points;			// 得点（採点が済んでいればここに得点がある）
	//
	String		saved_msg;		// エラーメッセージ
	
	public	StKadaiStartExam(){
		super();
		if(LOG.fa) LOG.println("■ StKadaiStartExam #コンストラクタ");
		
	}

	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		
		broker	=	getDbConnection();
		db		=	new Database(broker);			

		szDB			= 	getParameter(GROUP);
		teUid			= 	getParameter(TUID);
		stNumber		= 	getParameter(StUID);

		kadai_key		= 	getParameter("_kadai_key");
		lec_key			=	getParameter("_lec_key");
		aplec_key		= 	getParameter("_aplec_key");

		te_lec_key		= 	KeyGen.get_te_lec_key2(teUid, lec_key);
		te_aplec_key	= 	KeyGen.get_te_aplec_key2(teUid, aplec_key);

		setInfo();
		
		kar				= 	new KadaiApRecord(te_aplec_key,kadai_key,db);	// 課題実施レコード
		
		kdr				= 	new KadaiDefRecord(te_lec_key,kadai_key,db);	// 課題定義レコード
		subject			=	kdr.title();
		shubetsu		=	kdr.shubetsu();

    }
	
	/** 提出、得点、提出期日を得る */
	void	setInfo(){
		KadaiInfo kdf	= new KadaiInfo(szDB,db,stNumber,te_aplec_key,kadai_key);
		cnt = kdf.read_KadaiInfo();
		if(cnt==0){
			disposal		= KadaiInfo.NOTYET;
			points			= "-";
			submitTime		= "";
			/*
			 * 再実行ではない
			 */
			putParameter("reStart","");
			
		}else{
			disposal	= kdf.saiten_flag();
			points		= kdf.points();
			if(points.equals("")){	points	= "-"; }
			submitTime	= kdf.date_str();
			/*
			 * レコードあるということは再実行である
			 */
			putParameter("reStart","YES");
		}
	}    
	public	String	dispatch(){
		if(LOG.fa) LOG.println("■StKadaiStartExam #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		if( cmd.equals("SEND")){

			KadaiApRecord	kar	= new KadaiApRecord(te_aplec_key,kadai_key,db);		// 課題実施レコード
			/*
			 * 実行可能かチェックしてから開始する
			 */
			boolean		ok	=	checkAll(kar);
			if(ok){
				/*
				 * 開始時刻を システムハッシュ に書き込んでおいてから開始する
				 * また，ＨＯＬＤと開始時刻をKadaiInfoにも書き込む
				 */
				String	stamp	=	setClock();
				setHoldRecord(stamp);
				
				disp_mode		=	DISP_NEW;
				ret				=	"$stkadai.StKadaiExam";
				
			}else{
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;					
			}
			
		}else if(cmd.equals("CANCEL")){
			/*
			 * 取り消し
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
	/*
	 * タイムスタンプをシステムハッシュにセットする 
	 * 設定したタイムスタンプを返す
	 */
	String	setClock(){
	    /*
	     * 新規または継続でタイムスタンプを作成してシステムハッシュにセットする
	     */
	    String	stamp	=	setTimestamp();
		putParameter("_clock",stamp);
		return	stamp;
	}
	/**
	 * HOLDレコードを作成する
	 * @param stamp
	 */
	void	setHoldRecord(String stamp){
	    /*
	     * 再実行でない場合のみ作成する
	     */
	    String	restart	=	getParameter("reStart");
	    if(isEmpty(restart)){
		    /*
			 * HOLD レコードを作成しておく
			 * HOLD レコードは試験が正常に終了すると上書きされて消えるが、強制終了すると残る
			 */
			KadaiInfo 			kdf		= 	new KadaiInfo(szDB,db,stNumber,te_aplec_key,kadai_key);
			kdf.mkHOLD(shubetsu, stamp);
			kdf.insert_KadaiInfo();	    
	    }
	}
	/**
	 * 開始時刻のタイムスタンプをシステムハッシュにセットする
	 * StKadaiExamが受け取る
	 * 
	 * @return stamp  設定したタイムスタンプ（CSV文字列形式）
	 */
	String	setTimestamp(){
		
	    String 	stamp	=	"";
	    /*
	     * 初期化処理でreStartをセットしている
	     * submitTimeもは初期化処理で取得している
	     */
	    String	restart	=	getParameter("reStart");
	    if(isEmpty(restart)){
		    /* 
			 * 現在時刻を "YY,MM,DD,hh,mm,ss" の csv形式文字列にしたものをタイムスタンプとしてセットする
			 */
			GregorianCalendar	now		=	currentDay();
			stamp						= 	timeCsv(now);
			
	    }else{
	        /*
	         * 再実行なので開始時刻は変えない
	         * submitTime は初期化処理で取得している
	         */
	        stamp	=	submitTime;
	        
	    }
		return	stamp;
	}
	/**
	 * 開始前の全てのチェックを行なう
	 * エラーの場合はメッセージをシステムハッシュにセットして戻る
	 * 
	 * @return	OKならtrue
	 */
	boolean	checkAll(KadaiApRecord	kar){
	
		String	msg	= check();
		if(!isEmpty(msg)){
			putParameter(MESSAGE, msg);
			return	false;
		}
		msg	=	passwdChk(kar);
		if(!isEmpty(msg)){
			putParameter(MESSAGE, msg);
			return	false;
		}
		putParameter(MESSAGE, msg);
		return	true;
	}

	/**
	 * 表示するときと受験開始の前に必ず行うチェック
	 * @return　　エラーメッセージ、正常なら "" を返す
	 */
	String	check(){

	    if(KadaiInfo.isHOLD(db,szDB,te_aplec_key,kadai_key,stNumber)){
	        /*
			 * 再実行であるので，KadaiInfoレコードに最初に実行した時のタイムスタンプが CSV 形式で残っている．
			 * KadaiInfo のタイムスタンプから現在時刻までの秒数を計算し，制限時間内であれば続行する．
			 * 
			 * 開始時刻は本来の開始時刻が setTimestamp でセットされる
			 */
	        if(limitSeconds()>pastSeconds()){
	            saved_msg	=	"";
	            
	        }else{
		        /*
				 * HOLD状態では受験できない．<br>
				 * StKougi.java で未受験かHOLD状態の時のみこのクラスを実行するよう制御している
				 * 教師画面で該当の KadaiInfo と（あれば）解答ファイルを削除すると再受験可能になる
				 */
				saved_msg	=	"★★ 現在、受験できません．担当教師に連絡してください ★★";

	        }
			
	    }else if(kar.isEmptyRecord()){
			/*
			 * 期間が設定されていない（StKougi.java でブロックしているので起こらない） 
			 */
		    saved_msg	=	"★★ この課題はまだ受験できません．";
			
	    }else if(!kar.isStarted()){
			/*
			 * まだ期間前である（StKougi.java でブロックしているので起こらない）
			 */
		    saved_msg	=	"★★ 提出期間前なので受験できません";
	    
	    }else if(kar.isOver()){
			/*
			 * 期限が過ぎた（起こりえる．StKougi.java では期間が過ぎると無条件に開ける．未受験の場合はここで処理）
			 */
		    saved_msg	=	"★★ 提出期限が過ぎたので受験できません";
			    
		}else{
			saved_msg	=	"";
		}
		return	saved_msg;
	}
	
	/**
	 * 過去の開始時刻から現在時刻までの経過時間を秒数で得る
	 * @return
	 */
	int	pastSeconds(){
	
		GregorianCalendar	start	=	getCalendar(submitTime);
		GregorianCalendar	finish	=	currentDay();
		long				sec		=	DateGear.difSeconds(start, finish);
		    
		return	(int)sec;
	}
	/**
	 * 課題の制限時間を秒数で返す
	 * @return
	 */
	int	limitSeconds(){
	    
		/*
		 * exampaper.html が使う制限時間をシステムハッシュに入れておく
		 */
	    Exam 	examObj	=	new Exam(kdr.content(),para.getEmlConfPath(),true);
		int		seconds	=	examObj.getTimeLimit();		// 秒単位の制限時間
	    
		return	seconds;
	    
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

	String	passwdChk(KadaiApRecord	kar){
		/*
		 * パスワードが空白であれば、何時でも誰でも受験可能と判定する
		 * そうでなければパスワードの照合を行う
		 */
		String	msg			=	"";
		String	sysPasswd	=	(kar.passwd()).trim();
		if(!isEmpty(sysPasswd)){
			String	inputPasswd	=	strHash(htb,"_startPasswd");
			if( (isEmpty(inputPasswd))||(!sysPasswd.equals(inputPasswd)) ){
				/*
				 * パスワード不一致 
				 * 開始パスワードの入力をクリアしておく 
				 */
				msg	=	"★★ パスワードが違います ★★";
				htb.put("_startPasswd","");
			}
		}
		return	msg;		
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
			/*
			 * ホールドしていないかチェックしてメッセージを設定する
			 */
			putParameter(MESSAGE,check());
		}
		putParameter("kadai_title",kdr.title());
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}	

}
