/*
     Power Campus FaqEditor

*/
package faq;

import java.io.*;
import java.util.*;
import database.*;
import framework.*;
import setup.Setup;
/**
 * 
 * 
 	#
	# ##################
	#     FaqEditor
	# ##################
	#
	<program $faq.FaqEditor>
		<dispatch  html=FaqEditor.html  number=6130  class=faq.FaqEditor />
		<variable>
		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA HOMEURL DIVISION  title lec_key aplec_key rec_key/>
		  <accept    CMD      />
		  <keep      editor_rows />
		  
		  <form      />
		</variable>
	</program> 
 *
 *
 * 変数の説明
 *
 * 1. receive 
 * 		rec_key ----- 編集時、そのレコードのキー
 *                    新規作成時は明示的にクリアされて""である
 *
 * 2. accept
 * 3. keep
 * 		editor_rows　--- エディタの行数
 * 4. form
 *
 *
 */
public class FaqEditor extends SuperPlayer {

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
	Database		db;
	
	/**
	 * 	コンストラクタ
	 *
	 */
	public	FaqEditor(){
		super();
		if(LOG.fa) LOG.println("■ FaqEditor #コンストラクタ");
	}
	/**
	 * イニシャライザ
	 */
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		broker	=	getDbConnection();
		db		=	new Database(broker);			

	}
	/**
	 * ディスパッチャー
	 */
	public	String	dispatch(){
		if(LOG.fa) LOG.outHash(htb,"■Sample #dispatch()");
		if(LOG.fa) LOG.println("■Sample #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		// 
		if(cmd.equals("RETURN")){
			/*
			 * 戻る
			 * エディタの行数はDBに保存しておく
			 */
			updateEditorRows();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;
			
		}else if(cmd.equals("WRITE")){
			/*
			 * 保存
			 */
			save();
			disp_mode	=	DISP_EDIT;			
			ret			=	DISPATCH_DEFAULT;
		
		}else if(cmd.equals("PLUS")){
			/*
			 * 編集領域拡大
			 */
			plus();
			disp_mode	=	DISP_EDIT;			
			ret			=	DISPATCH_DEFAULT;
			
		}else if(cmd.equals("MINUS")){
			/*
			 * 編集領域縮小
			 */
			minus();
			disp_mode	=	DISP_EDIT;			
			ret			=	DISPATCH_DEFAULT;

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
	 * 保存
	 *
	 */
	void	save(){
		String 	teUid		= getParameter(TUID);
		String	lec_key		= getParameter("_lec_key");
		String 	te_lec_key 	= KeyGen.get_te_lec_key2(teUid,lec_key);
		String	faq_rec_key	= getParameter("rec_key");
		//
		String	faq_title	= getParameter("_faq_title");
		String	faq_body	= getParameter("_faq_body");
		/*
		 * faq_rec_key が空でなければアップデート
		 */
		FAQ 	faq		= new FAQ(db);
		if(!isEmpty(faq_rec_key)){
			faq.add_keys(te_lec_key,faq_rec_key);
			faq.add_data(faq_title, faq_body);
			faq.update_FAQ();
			
		}else{
			KeyGen keygen	= new KeyGen(teUid,db);
			faq_rec_key		= keygen.nextFaq();

			faq.add_keys(te_lec_key,faq_rec_key);
			faq.add_data(faq_title, faq_body);
			faq.insert_FAQ();
			
		}		
	}
	/**
	 * エディタ行数を増加
	 */
	void	plus(){
		Setup info	= new Setup( getParameter(TUID), db );	// デフォルト
		int	max		= info.rowMax();	// 最大値
		int	min		= info.rowMin();	// 最小値
		int	delta	= info.deltaRows();	// 増分
		//
		String editor_rows	= strHash(htb,"_editor_rows");
		int	rows	= Integer.parseInt(editor_rows);
		if(rows <= (max - delta) ){
			editor_rows	= String.valueOf(rows + delta);
			htb.put("_editor_rows",editor_rows);
		}		
	}
	/**
	 * エディタ行数を減少
	 */
	void	minus(){
		Setup info	= new Setup( getParameter(TUID), db );	// デフォルト
		int	max		= info.rowMax();	// 最大値
		int	min		= info.rowMin();	// 最小値
		int	delta	= info.deltaRows();	// 増分
		//
		String	editor_rows	=	strHash(htb,"_editor_rows");
		int		rows		= 	Integer.parseInt(editor_rows);
		if(rows >= min + delta){
			editor_rows	= String.valueOf(rows - delta);
			htb.put("_editor_rows",editor_rows);
		}
		
	}
	/**
	 *  エディタ行数を設定情報に反映しておく 
	 */
	void	updateEditorRows(){
		Setup info	= new Setup( getParameter(TUID), db );
		info.updateFaqRows(strHash(htb,"_editor_rows"));
		info.wrtInfo();		
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
	        htb.put("_msg","★ 質問部を書いたら改行し，点線 '--------------------'(20文字以上)を引きます．その次の行に回答を書きます");

		}
        // テキストエリアの行数の初期値をセットする
		String	rows	= getParameter("editor_rows");
		if(isEmpty(rows)){
			Setup info	= new Setup( getParameter(TUID), db );		// デフォルト
			htb.put("_editor_rows",String.valueOf(info.faqRows()));	
		}		
		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}
}

