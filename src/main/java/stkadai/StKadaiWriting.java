package stkadai;

import java.io.*;
import java.util.*;
import framework.*;
import tktools.*;
import	epml.*;
import kadai.*;

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
	#   StKadaiWriting
	# ##################
	#
	<program $stkadai.StKadaiWriting>
		<dispatch  html=StKadaiExam.html  number=2525  class=stkadai.StKadaiWriting />
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
public class StKadaiWriting extends StKadaiExam {
    
    String	levelVal;
    String	scoreVal;    

	public	StKadaiWriting(){
		super();
		if(LOG.fa) LOG.println("■ StKadaiWriting #コンストラクタ");
	}
   
	/**
	 * 終了処理
	 * オーバーライド
	 *
	 */
	void	doEnd(){
		/*
		 * 文字数を数えて採点する
		 */
		eval();
		/*
		 * 得点をシステムハッシュにセットする
		 */
		putParameter("score", scoreVal);

		points	=	scoreVal;
	    writeKadaiInfo();
		sub_wrt();
	}
	/**
	 * KadaiInfo に状態を書き込む<br>
	 * 
	 * point に得点を持っている
	 * 
	 */
    void	writeKadaiInfo(){
		if(LOG.fa)  LOG.println("■■ StKadaiWriting #writeKadaiInfo()");

		String				stamp	=	strHash(htb,"_clock");	
		GregorianCalendar	start	=	getCalendar(stamp);
		GregorianCalendar	finish	=	currentDay();
		if(LOG.fa) isOKTimeTest(start,finish);
		
		disposal		=	KadaiInfo.GRADED;	// 採点済み
		
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
    /** 
     * 解答を書き込む<br>
     * 
     * putParameter("score", scoreVal);
     * を実行済みでこれを呼び出すことにより，得点を含んだAnswerRecordを生成して書き込む
     * 
	*/
    public void sub_wrt(){
		if(LOG.fa)  LOG.println("■■ StKadaiWriting #sub_wrt()");
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
		 * 
		 * ※特別な値（文字列）は putParameter("fn",･･･) [N=1,2,3]でセットできる
		 * 　ここでは入力文字数を f1 にセットした．
		 * 　AnswerRcord#getF1() で取得できる.
		 * 
		 */
		putParameter("f1", levelVal);	// 入力した文字数
		AnswerRecord	ans		= new AnswerRecord(htb); 
		String			path	= para.stFilePath(teUid,aplec_key,kadai_key,stNumber);// 提出ファイルへのフルパス
        boolean      	chk  	= writeAnswer(path,ans);
        if(!chk) {
        	String	msg	=	"class StKadaiWriting #sub_wrt() : ★ 解答をファイルに書けません.:path="+path;
        	LOG.errStop(out, msg);
        }else{
            //putParameter(MESSAGE,"★ 解答を保存しました");
        }
    }    
	/**
	 * タイプ課題の評価点を得る
	 * エラーがあれば null を返す
	 * 
	 * @return
	 */
	String	eval(){
	    
	    if(LOG.fa){
	        LOG.println("■ StKadaiWriting #eval() ");
	        LOG.outHash(htb,"");
	    }
	    
	    /*
	     * 課題をパースして評価項目名と評価基準を得る
	     * 
	     */
	    Exam 	ex		= 	new Exam( rec.content(), para.getEmlConfPath());
	    ex.createHtml();
	    String	item	=	ex.getCriterionName();
	    Vector	table	=	ex.getCriterion();
	    
	    if( isEmpty(item) || (table==null) || table.size()==0){
	        return	null;
	    }
	    /*
	     * getParameter("ETA(0)")はテキストエリアの内容
	     * 
	     */
	    EvalWriting	et	=	new	EvalWriting( getItem("ETA(1)"), item, table);

	    /*
	     * evalWriting() で使うので保存する 
	     */
	    scoreVal	=	et.getScore();
	    levelVal	=	et.getLevel();
	    
	    if(LOG.fa){
	        LOG.println("    ★LV   =" + levelVal);
	        LOG.println("    ★得点 =" + scoreVal);
	    }
	    
	    return	scoreVal;
	}
	
}
