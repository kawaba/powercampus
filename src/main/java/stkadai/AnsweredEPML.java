package stkadai;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Hashtable;
import database.Database;
import database.DbConnectionBroker;
import database.KeyGen;
import epml.Exam;
import framework.LOG;
import framework.Param;
import jbbs.BbsInfoDB;
import kadai.AnswerRecord;
import kadai.KadaiDefRecord;
import tktools.Gear;
import tktools.tkException;
/**
 *  学生の解答を表すHTMLを作成して返す
 *
 *
 *
 */
public class AnsweredEPML {
	

	DbConnectionBroker	broker;
	Param				para;
	
	String				szDB;
	String				stNumber;
	String				teUid;
	String				lec_key;
	String				aplec_key;
	String				kadai_key;
	
	Database			db;
	Hashtable			htb;
	String				te_lec_key;
	KadaiDefRecord		kadaiDefRec;
	
	/**
	 * 先頭に書式情報をつける必要がないときtrue
	 * デフォルトは false
	 */
	boolean			NOT_NEED;	
	
	public	AnsweredEPML(	DbConnectionBroker	broker, 
							Param 				para, 
							String				szDB,
							String				stNumber,
							String				teUid,
							String				lec_key,
							String				aplec_key,
							String				kadai_key){
		
		this.broker		=	broker;
		this.para		=	para;
		this.szDB		=	szDB;
		this.stNumber	=	stNumber;
		this.teUid		=	teUid;
		this.lec_key	=	lec_key;
		this.aplec_key	=	aplec_key;
		this.kadai_key	=	kadai_key;
		
		db				=	new Database(broker);
		htb				=	new	Hashtable();
		te_lec_key		=	KeyGen.get_te_lec_key2(teUid, lec_key);
		kadaiDefRec		= 	new KadaiDefRecord(te_lec_key,kadai_key,db); // デバッグ出力を設定した
		
		NOT_NEED		=	false;

		if(LOG.fa)  LOG.println("■■ AnsweredEPML #コンストラクタ");

	}
	/**
	 * 書式付加不要のフラグをON／OFFする
	 *
	 */
	public	void	setNoFormat(){
		NOT_NEED	=	true;
	}
	public	void	resetNoFormat(){
		NOT_NEED	=	false;
	}
	
	
	/**
	 * 表示する問題のHTMLを作成して返す。
	 * 初期表示はDBから問題をそのまま、解答してファイルがあればそのファイルから
	 * 表示データを作成する
	 * 
	 * @return
	 */
	public	String	getAnswerHtml(){
		if(LOG.fa)  LOG.println("■■ AnsweredEPML #getAnswerHtml()");
		
		String	answer	=	"";
		File	fp	=	getStudentAnswerFP(); // LOG.println(fp.toString());
		if(fp.exists()){
			answer	=	getHtml(fp);
		}else{
			answer	=	getInitHtml();
		}
		
		//LOG.println("正解HTMLの文字列");
		//LOG.println(answer);
		
		return	answer;
		
	}
	/**
	 * 学生の解答ファイルへのファイルポインタを得る<br>
	 * ディレクトリが存在しない時（ファイルも存在しないが）一応ディレクトリを作成し、ファイルポインタを作成する
	 * @return		ファイルポインタ
	 */
	File getStudentAnswerFP(){
		if(LOG.fa)  LOG.println("■■ AnsweredEPML #getStudentAnswerFP()");
		
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
		if(LOG.fa)  LOG.println("■■ AnsweredEPML #getHtml()");

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
		if(LOG.fa)  LOG.println("■■ AnsweredEPML #getAnswerHtml()");
		/*
		 * 学生の解答EPMLを取り出す
		 */
		String 	answerText;
		try{
			String		pmlText	= 	getAnswerText(ufp);
			answerText 			= 	addFormat(pmlText);
			 
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
		String	source	=	kadaiDefRec.content();

		/*
		 * それと作成元の問題（問題原文EPML）とシステムハッシュを使って、
		 * 問題が学生の解答を初期とするよう作成する 
		 */
		Exam	exam		= new Exam(source, para.getEmlConfPath());
		
		String	graphicUrl	=	para.getAttachURL(teUid, lec_key, kadai_key);
		String	graphicPath	=	para.getAttachDIR(teUid, lec_key, kadai_key);
		exam.setImgPath( graphicUrl );
		exam.setImgDestinationPath( graphicPath );
		exam.setNonHtmlFlag();			

		String	htmlText	=	exam.update(htb);
		return	htmlText;
		
	}
	/** 初期表示のための学生の解答HTMLを返す */
    String	getInitHtml(){
		if(LOG.fa)  LOG.println("■■ AnsweredEPML #getInitHtml()");
		/*
		 * 課題定義レコードから問題（EPML）を読み込む
		 */
		String	pmlText	=	addFormat(kadaiDefRec.content());
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
	 * pmlテキストの先頭に書式情報を付加する
	 * @param content
	 * @return
	 */
	String	addFormat(String content){
		if(LOG.fa) LOG.println("■ AnsweredEPML #addFormat()");

		if(NOT_NEED)	return content;
		
		/*
		 * pmlテキストに段落エイリアスを付加する
		 * テンプレートがあるのでそれを読み込んでアペンドする
		 */
		Hashtable	ht		=	getInfoRecord(teUid); // null の場合がある

		String		format	=	"";
		if(ht==null) {
			format= para.formatTemplate();
			return format + Gear.lineSeparator() + content;
		}
		
		if(kadaiDefRec!=null){
			format	=	Gear.strHash(ht,BbsInfoDB.FORMAT);
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
		if(LOG.fa)  LOG.println("■■ AnsweredEPML #getAnswerText()");
		
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
		if(LOG.fa) LOG.println("■ AnsweredEPML #getAnsRecod()");
		
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
		if(LOG.fa) LOG.println("■ AnsweredEPML #getInfoRecord()");
		
		BbsInfoDB	infodb	=	new BbsInfoDB(uid, szDB, broker);
		Hashtable	dt		=	new Hashtable();
		int		count	=	infodb.readBbsInfo(uid, dt);
		if(count>0)	return	dt;
		return			null;
	}	
	
}
