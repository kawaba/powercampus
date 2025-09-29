
package stkadai;

import java.io.*;
import java.util.*;
import database.*;
import framework.*;
import tktools.*;

import kadai.AnswerRecord;
import	epml.*;

/**
 * 
 * 作成したレポート課題をプレビューする<br>
 * 課題ファイルを読込んでテキストをepmlにロードする
 * 
 * @author kawaba
 *
 *
 *
 	#
	# ##################
	#     StKadaiPrev
	# ##################
	#
	<program $stkadai.StKadaiPrev>
		<dispatch  html=StKadaiPrev.html  number=5820  class=stkadai.StKadaiPrev />
		<variable>
		  <receive   NUMBER STAMP TUID GROUP StUID lec_key aplec_key kadai_key />
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
 *
 */
public class StKadaiPrev extends SuperPlayer {
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
	
	String		szDB;
	String 		teUid;
	String		te_lec_key;
	String		te_aplec_key;
	String		kadai_key;
	String		stNumber;
	String		aplec_key;
	String		lec_key;
	
	String		content;
	
	public	StKadaiPrev(){
		super();
		if(LOG.fa) LOG.println("■ StKadaiPrev #コンストラクタ");
	}
	public	void initialize(PrintWriter out, Hashtable htb, Param para){

		broker	=	getDbConnection();
		
		szDB			= strHash(htb,"_szDB");
		teUid			= strHash(htb,"_teUid");
		te_lec_key		= KeyGen.get_te_lec_key(htb);
		te_aplec_key	= KeyGen.get_te_aplec_key(htb);
		kadai_key		= strHash(htb,"_kadai_key");
		stNumber		= strHash(htb,"_stNumber");
		aplec_key		= strHash(htb,"_aplec_key");
		lec_key			= strHash(htb,"_lec_key");

	}	

	public	String	dispatch(){
		if(LOG.fa) LOG.println("■StKadaiPrev #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		if(cmd.equals("VIEW")){
			/* web 経由の自動起動
			 * PostStReportPreview.html から起動される
			 */
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;

		}else{
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;		}
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
		/*
		 * ファイルを読んでプレビューする解答をHTMLで得る
		 */
		htb.put("_html", htmlStr());	
		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}
	/**
	 * htmlに直した解答を得る
	 * @return
	 */
	String	htmlStr(){
		/*
		 * setImgPath() はグラフィックスへの完全URL．/user/(teUid)/file/(aplec_key)/(kadai_key)/(stNumber)/
		 * setImgDestinationPath()はグラフィックスの絶対ディレクトリパス． /home/pc/(teUid)/file/(aplec_key)/(kadai_key)/(stNumber)/ 
		 */
		Exam	exam		= 	new Exam( textStr() ,para.getEmlConfPath());
		String	graphicUrl	=	para.kadaiAttachUrl(teUid, aplec_key, kadai_key, stNumber);
		String	graphicPath	=	para.kadaiAttachDir(teUid, aplec_key, kadai_key, stNumber);
		
		exam.setNonHtmlFlag();// 資料ではなく課題データであることを exam に伝える
		exam.setImgPath( graphicUrl );
		exam.setImgDestinationPath( graphicPath );

		/*
		 * \ " ' を元の文字に戻しておく
		 */
		return	Gear.toNormalString(exam.createHtml());
	}	

    /**
     * 解答を読み込む。
     * 保存されているファイルがなければ""を返す
     */
    String	textStr(){
		
		File	fp	=	kadaiFP();
		if(fp.exists()){
			AnswerRecord	ans	=(AnswerRecord)FileGear.readObj(fp);
			return			ans.getAnswer();
		}
		/* 
		 * レポートでは課題内容は別表示なので _answer にセットしない.
		 * 課題内容は課題定義レコードにある
		 */	
		return	"";	
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
	
}
