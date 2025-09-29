package framework;

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Vector;
/**
*
*
	#
	# ##################
	#     Message
	# ##################
	#
	<program $framwork.Message>
		<dispatch  html=sysMessage.html  number=90000  class=$framwork.Message />
		<variable>
		  <receive   title msg switch />
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
* 		title      メッセージタイトル
*       msg　　　　メッセージ
*       switch　　呼び出し元へリターン可能なら "RETURN" を設定する
* 　　　　　　　　設定しないかそれ以外だと「了解ボタン」でウィンドウが閉じる
* 2. accept
* 　　　CMD		  switchに設定した文字が返る．実際にはswitch=="RETURN"の時のみ
* 　　　　　　　　"RETURN"が返る．それ以外ではwindowが閉じる．
* 3. keep
* 4. form
*
*
*/
public class Message extends SuperPlayer{
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

	
	public	Message(){
		super();
		if(LOG.fa) LOG.println("■ Sample #コンストラクタ");
	}	

	/**
	 * 
	 * @param out			出力ポインタ
	 * @param htb			システムハッシュ
	 * @param para			パラメータ
	 */
	public	void initialize(PrintWriter out, Hashtable htb, Param para){

	}
	/**
	 * 
	 */	
	public	String	dispatch(){
		if(LOG.fa) LOG.println("■Printer #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		if(cmd.equals("RETURN")){
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;	// 復帰			

		}else{// 発生しない
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
		if(LOG.fa)	LOG.println("■ Printer #display(boolean editmode)");
		
		if(!editmode){
			
		}
		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}

}

