/*
Power Campus SetupView
設定情報を更新する

	#
	# ##################
	#   SetupView
	# ##################
	#
	<program $setup.SetupView>
		<dispatch  html=setup.html  number=110  class=setup.SetupView />
		<variable>
		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA/>
		  <accept    CMD    UPLODE  />
		  <set             />
		  
		  <form     rows night eLearning kadai_editorsRows html_editorsRows faq_editorsRows />
		  <work            />
		</variable>
	</program>
 
*/
package setup;
import 	java.util.*;
import 	java.io.*;

import database.Database;
import database.DbConnectionBroker;
import framework.*;
//
//
public class SetupView extends SuperPlayer {

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
	
	String			mail;
	Setup			st;

	public SetupView() {
		super();
		if(LOG.fa) LOG.println("■ SetupView #コンストラクタ");
	}
	/**
	 * コンストラクタが引数をもてないので、コンストラクタで行う内容をこの initialize() に書く．
	 * out, htb, para は SuperPlayer クラスのインスタンス変数なので、
	 * 引数にとらずとも利用できるが、変数を明示する意味で列挙してある
	 * 
	 * @param out			出力ポインタ
	 * @param htb			システムハッシュ
	 * @param para			パラメータ
	 */
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		broker	=	getDbConnection();
		db		=	new Database(broker);
		st		=	new Setup( getParameter(TUID), db );
	}
    //
    public String dispatch(){
        if(LOG.fa) LOG.outHash(htb,"SetupView #dispatch()");
		/*
		 * 処理分岐を判断するキーはシステムハッシュからDISPATCH_KEYをキーとして取り出す
		 * リターンコードと次に表示するWEBの表示モードは設定忘れを防ぐため規定値をセットしておく
		 * 
		 * disp_mode とは
		 * 　　次にこの画面を表示するときの表示モード．
		 * 　　あるいは次に起動するプログラムで使われる表示モード
		 *　（注）
		 * 　　他のプログラムが終了して、DISPATCH_RETURN でこのプログラムが呼び出される時は 
		 * 　　コントローラーにより display(boolean disp_mode) へ直接復帰する．
		 * 　　この場合、disp_mode はコントローラーによってDISP_EDIT に設定されている 
		 * 
		 */
		ret			=	DISPATCH_DEFAULT;		// WEB表示
		disp_mode	=	DISP_NEW;
		cmd			=	getParameter(CMD);		// null の場合は ""を返す
		if(cmd.equals("DO")){
			boolean	flag	= updateSetup();
			if(flag){
				disp_mode	=	DISP_NEW;
				ret			=	DISPATCH_RETURN;

			}else{
				putParameter(MESSAGE,st.msg());			// エラーメッセージをセット
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;
				
			}
		}else if(cmd.equals("RETURN")){
			disp_mode	=	DISP_NEW;
			ret			=	DISPATCH_RETURN;			
        // エラー
        }else{
			/* 
			 * cmd には起動したいクラスキーが入っている（$jbbs.･･･）
			 * 該当のクラスを起動する
			 */
			disp_mode	=	DISP_NEW;
			ret			=	cmd;        
		}
		/*
		 * 次に表示するときあるいはリターンしてきた時の表示モードをセットしてからリターンする
		 */
		putParameter(DISP_KEY,disp_mode);
		return	ret;		
    }
	//
	//  設定情報を更新する
	//
	boolean	updateSetup(){
		//
		String	rows		= strHash(htb,"_rows");
		String	night		= strHash(htb,"_night");
		String	eLearning	= strHash(htb,"_eLearning");
		//
		String	kadai_editorsRows	= strHash(htb,"_kadai_editorsRows");
		String	html_editorsRows	= strHash(htb,"_html_editorsRows");
		String	faq_editorsRows		= strHash(htb,"_faq_editorsRows");
		//
		// 行数の範囲をチェックする
		if(!st.rowCheck(kadai_editorsRows))		return	false;
		if(!st.rowCheck(html_editorsRows))		return	false;
		if(!st.rowCheck(faq_editorsRows))		return	false;
		//
		// 更新登録
		st.updateRecord(rows,night,eLearning,faq_editorsRows,kadai_editorsRows,html_editorsRows);
		st.wrtInfo();
		return	true;
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
	 * 出力処理
	 */
	public  void	display(boolean editmode){
		if(LOG.fa)	LOG.println("■ Tlogin #display(boolean editmode)");
		
		if(!editmode){// 新規表示
			putParameter(MESSAGE,"");
		}
		initialData();
		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		String	fn	=	getParameter(DISPFILE);
		Vector	v	=	loadHtml(fn);
		printVector(v);			
		
	}
	public void initialData(){
		//
		// 現在のセットアップ情報を得る
		Setup info	= new Setup( getParameter(TUID), db );
		//
		// selected 文字列を設定
		int	n1 = info.maxGyo() + 1 ;  // 選択なしの分が＋１
		for(int i=0; i<n1; i++){
			if(i==info.rows()){
				putParameter( ("_lb1" + String.valueOf(i) ), "selected");
			}else{
				putParameter( ("_lb1" + String.valueOf(i) ), "");
			}
		}
		// selected 文字列を設定
		int	n2 = info.maxNight() + 1 ;
		for(int i=0; i<n2; i++){
			if( i==info.nightSchool() ){
				putParameter( ("_lb2" + String.valueOf(i) ), "selected");
			}else{
				putParameter( ("_lb2" + String.valueOf(i) ), "");
			}
		}
		// selected 文字列を設定
		int	n3 = info.maxCourse() + 1 ;
		for(int i=0; i<n3; i++){
			if( i==info.eLearningSchool() ){
				putParameter( ("_lb3" + String.valueOf(i) ), "selected");
			}else{
				putParameter( ("_lb3" + String.valueOf(i) ), "");
			}
		}
		//
		// エディタ行数
		putParameter("_kadai_editorsRows",String.valueOf(info.kadaiRows()));
		putParameter("_html_editorsRows",String.valueOf(info.htmlRows()));
		putParameter("_faq_editorsRows",String.valueOf(info.faqRows()));

		return;
    }
}

