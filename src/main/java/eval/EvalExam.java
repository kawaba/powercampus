
package eval;
import database.*;
import epml.*;
import framework.*;

import java.io.*;
import java.util.*;

import kadai.*;
/**
 * 
 * 
 *
 	#
	# ##################
	#     EvalExam
	# ##################
	#
	<program $eval.EvalExam>
		<dispatch  html=EvalExam.html  number=1550  class=eval.EvalExam />
		<variable>
 		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA HOMEURL DIVISION title lec_key aplec_key kadai_key kadai_title start end/>
		  <accept    CMD  />
		  <keep      correctEPML recNo max />
		  
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
 *		correctEPML	-- 	複数の正解語と全学生の解答から採取した複数の不正解語をもつEPMLテキスト
 * 		recNo		--	現在の項目番号
 * 		max			--	総項目数
 * 4. form 
 *
 *
 *
 */


public class EvalExam extends SuperPlayer {

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
	String		te_lec_key;
	String		kadai_key;
	
	public	EvalExam(){
		super();
		if(LOG.fa) LOG.println("■ EvalExam #コンストラクタ");
	}	

	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		broker		=	getDbConnection();
		db			=	new Database(broker);			
		szDB		= 	getParameter(GROUP);
		te_lec_key	= 	KeyGen.get_te_lec_key2(getParameter(TUID), getParameter("lec_key"));
		kadai_key	= 	getParameter("kadai_key");

	}
	/**
	 * 
	 */
	public	String	dispatch(){
		if(LOG.fa) LOG.println("■EvalExam #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;

		if(cmd.equals("GRADE")){
			/*
			 * 自動採点を実行してリターンする
			 */
			sub_grade();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;
			
		}else if(cmd.equals("REFDATA")){
			/*
			 * 提出されたデータを参照する
			 */
			disp_mode	=	DISP_NEW;
			ret			=	"$eval.ExamOnWeb";
			
		}else if(cmd.equals("WRITE")){
			/*
			 * 変更を書き込む
			 */
			sub_wrtAnswer();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;			
			
		}else if(cmd.equals("FIRST")){
			/*
			 * 先頭のレコードを表示
			 */
			sub_gotoTop();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;			
			
		}else if(cmd.equals("LAST")){
			/*
			 * 末尾のレコードを表示
			 */
			sub_gotoLast();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;			
			
		}else if(cmd.equals("NEXT")){
			/*
			 * 次のレコードを表示
			 */
			sub_gotoNext();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;			
			
		}else if(cmd.equals("BACK")){
			/*
			 * 前のレコードを表示
			 */
			sub_gotoBack();
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
	
	/**
	 * 先頭の項目へ
	 */
	public void sub_gotoTop(){
		putParameter("recNo","0");
	}
	/**
	 * 末尾の項目へ
	 */
	public void sub_gotoLast(){
		int	max	= Integer.parseInt(getParameter("max")) - 1;
		putParameter("recNo", String.valueOf(max));
	}
	/**
	 * 次の項目へ
	 */
	public void sub_gotoNext(){
		int	max		=	Integer.parseInt(getParameter("max"));
		int	rec		=	Integer.parseInt(getParameter("recNo"));
		if(rec < max -1){
			rec++;
		}
		putParameter("recNo",String.valueOf(rec));
	}
	/**
	 * 前の項目へ
	 */
	public void sub_gotoBack(){
		int	max		=	Integer.parseInt(getParameter("max"));
		int	rec		=	Integer.parseInt(getParameter("recNo"));
		if(rec > 0){
			rec--;
		}
		putParameter("recNo",String.valueOf(rec));
	}
	/**
	 * 自動採点の実行
	 *
	 */
	public void sub_grade(){
		if(LOG.fa) LOG.println("class exwork #sub_grade() : 採点を実行する の先頭です");
		/*
		 * データベース中の問題（正解）から Exam オブジェクトを生成する．
		 * Exam の grades() メソッドはひとつの解答テキストを引数にとって、採点結果を整数で返すので、
		 * これを繰り返し呼ぶ事により採点できる
		 * 
		 * Answer オブジェクトを使って各学生の解答テキストの読み出しと書き込みを行う
		 * 
		 */
		
		String	kadai_key	= getParameter("_kadai_key");
		KadaiDefRecord rec	= new KadaiDefRecord(te_lec_key,kadai_key,db);
		Exam		exam	= new Exam(rec.content(),para.getEmlConfPath(),true);	// 問題ＥＰＭＬ（初期化指定で生成）
		Answer		answers	= new Answer(htb,para,db);
		
		
		int	max	= answers.getTotal();
		for(int	k=0; k<max; k++){
			if(LOG.fa){countTest(k);}
			/*
			 *  Exam # getResult() で得点を埋め込んだテキストが得られるので得点フィールドに書き込む
			 *  また、正誤情報を付加したEpmlテキストを新らしいansとして書き換える
			 */
			AnswerRecord	ans		= answers.getdAnsRecordAt(k);		// 学籍番号順にソートしたファイルのk番目
			Exam			examSt	= new Exam(ans.getAnswer(),para.getEmlConfPath(),true);
			int				points	= exam.grades(examSt);				// 採点
			String			graded	= examSt.getEpml();					// 採点済みのEpmlテキスト（正誤情報を付加）
			//
			ans.setScore(String.valueOf(points));						// 点数をレコードにセット
			ans.setAns(graded);											// 解答Epmlを書き換え
			AnswerRecord ar = answers.writeAnsRecord(k,ans);			// ファイルに書き戻し
			//
			update_KadaiInfo(ar);	// KadaiInfo を更新する
		}
	} 
	/**
	 * 自動採点に連動してKadaiInfo を更新する
	 * 学生の講義画面で採点済みを表示するのに必要
	 * 
	 * @param ans
	 */
	public	void update_KadaiInfo(AnswerRecord ans){
		String		stNumber		= 	ans.getStNumber();
		String 		te_aplec_key	= 	KeyGen.get_te_aplec_key2(ans.getTeUid(),ans.getClassKey());
		String 		kadai_key		= 	ans.getKadaiKey();
		String 		score			= 	ans.getScore();
		//
		KadaiInfo 	kdi				= 	new KadaiInfo(szDB,db);
		kdi.updateScore( stNumber, te_aplec_key, kadai_key, score);
	}
	/**
	 * 変更を書き込む
	 *
	 */
	public void sub_wrtAnswer(){
		if(LOG.fa) LOG.println("class exwork #sub_wrtAnswer() : 変更を書き込む（次のレコードへ） の先頭です");
		//
		/*
		 * チェック用HTMLを生成しハッシュに入れる
		 * 正規化してあるので元に戻しておく 
		 */
		String		correctEPML	= 	Exam.antiRegularize( fromB64(getParameter("correctEPML")) );
		Corrector	ctr			= 	new Corrector(correctEPML,para.getEmlConfPath());
		/*
		 * ハッシュの内容で書き換えた正解のEPMLを返すので、ハッシュを書き換える
		 * ただし、Webで問題がおきないように正規化しておく
		 */
		String	newEPML	= Exam.regularize(ctr.update(htb));
		putParameter("correctEPML",toB64(newEPML) );
		/*
		 * データベースから問題レコードを検索し、内容（contents）を書き換える
		 */
		KadaiDefRecord rec	= new KadaiDefRecord(te_lec_key,kadai_key,db);
		rec.set_content(newEPML);
		rec.update(db);
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
			String	html	=	makeInitHtml();
			putParameter("correctEPML",toB64(html) );			
			putParameter("recNo", "0");
			putParameter(MESSAGE,"");
		}
		/*
		 * 表示すべきhtmlを得る
		 */
		String	html	=	makeHtml(Integer.parseInt(getParameter("recNo")));
		putParameter("html"	, html);
		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}
	/**
	 * 
	 * @return
	 */
	public	String	makeInitHtml(){
		/*
		 * 試験問題のEPML原文を得る
		 */
		KadaiDefRecord rec	= new KadaiDefRecord(te_lec_key,kadai_key,db);	
		String	epmlText	= rec.content();
		//
		if(LOG.fa) {
			LOG.println("== epmlText ==");
			LOG.println(epmlText);
		}
		/*
		 * HTMLに変換する
		 */
		Exam		exam	= new Exam(epmlText ,para.getEmlConfPath());
		exam.doEPML();
		/*
		 * correctEPMLは各問題項目のについて
		 * 複数の正解語と全学生の解答から採取した複数の不正解語をもつEPMLテキスト
		 * 
		 * getEpmls(htb,para)は全ての解答レコードをベクターに収集する
		 */
		Corrector	cr			= 	new Corrector(para.getEmlConfPath());
		Vector		answers		= 	getEpmls(htb,para); 
		String		correctEPML = 	cr.correctEPML(exam,answers);
		/*
		 * 最大項目数を記憶しておく
		 */
		putParameter("max", String.valueOf(cr.howmany()));
		
		if(LOG.fa){
			LOG.println("");
			LOG.println("== correctEPML ==");
			LOG.println(Exam.regularize(correctEPML));
			LOG.println("========== ここまで ==========");
		}
		String	html	=	Exam.regularize(correctEPML);
		return	html;
		
	}
	
	public String makeHtml(int	i){
		if(LOG.fa) LOG.println("■ EvalExam #display_exam() : 試験問題の採点画面を表示する の先頭です");
		/*
		 * 記述式項目についてチェック用HTMLを生成する Corrector を作成し、０番の項目をハッシュにセットする
		 * 
		 * Corrector#checkListHtml()は第ｋ番目の問題項目のチェックリストのHTMLを返す
		 * 記述式項目がない場合は、「ない」ことを表示するHTMLを返す
		 */
		Corrector	ckCtr	= 	new Corrector( fromB64(getParameter("correctEPML")), para.getEmlConfPath());
		
		/*
		 * html生成のためのグラフィックスディレクトリとURL情報をセットする
		 */
		String	graphicUrl	=	para.getAttachURL(getParameter(TUID), getParameter("lec_key"), getParameter("kadai_key"));
		String	graphicPath	=	para.getAttachDIR(getParameter(TUID), getParameter("lec_key"), getParameter("kadai_key"));
		ckCtr.setImgPath( graphicUrl );
		ckCtr.setImgDestinationPath( graphicPath );
		/*
		 * 資料データではないという設定をしておく
		 * これにより、ファイルへのリンクがイメージへのリンクと同じになる 
		 */
		ckCtr.setNonHtmlFlag();
		/*
		 * HTMLを生成する
		 */
		String		CorrectHtml	= 	ckCtr.checkListHtml(i);
		return		CorrectHtml;
	}
	/**
	 * 全ての解答レコードをベクターに収集する
	 * @param htb
	 * @param para
	 * @return
	 */
	public Vector	getEpmls(Hashtable htb,Param para){
		if(LOG.fa){
			LOG.println("■EvalExam #getEpmls()：全ての解答レコードをベクターに収集する");
		}
		Answer	ans			= new Answer(htb,para,db);		// htb 内に問題キーなど検索情報があることが前提
		int	max			= ans.getTotal();					// 存在する解答ファイルの個数
		Vector	ansEpmls	= new Vector(100,10);
		for(int	i=0; i<max; i++){
			AnswerRecord	rec	= ans.getdAnsRecordAt(i);
			ansEpmls.add(rec.getAnswer());					// 解答のEPML
			if(LOG.fa){
				
			    LOG.println("("+i+") " + rec.getStNumber()+"/"+rec.getStName());
				        
			    //DBG.println("□EvalExam #getEpmls()/ 学生の解答-" + i);
				//DBG.println(rec.getAnswer());
				//DBG.println("-------------------------------------------------------");
				//DBG.println("");
			}
		}
		return	ansEpmls;
	}
	/*
	 * 文字列をBase64エンコード
	 */
	String	toB64(String str){
	    return	Base64.encodeObject( str );
	}
	/*
	 * 元に戻す
	 */
	String	fromB64(String b64){
	    return	(String)( Base64.decodeToObject(b64) );
	}
	
	
	/**
	 * デバッグ用
	 *
	 */
	void	keyTest(String te_lec_key,String kadai_key,int max){
		LOG.println("■■ 採点実行時のキーとファイル数");
		LOG.println("    te_lec_key =" + te_lec_key);
		LOG.println("    kadai_key  =" + kadai_key);
		LOG.println("    max        =" + max);
	}	

	void	countTest(int i){
		LOG.println("□ i の値  =" + i);
	}
}

