/*
 * 作成日: 2005/10/03
 *
 */
package framework;

import java.io.*;
import java.util.*;

/**
 *
 *
 	#
	# ##################
	#   DownloadList
	# ##################
	#
	<program $framework.DownloadList>
		<dispatch  html=DownloadList.html  number=99992  class=framework.DownloadList />
		<variable>
		  <receive   NUMBER STAMP name mail shozoku/>
		  <accept    CMD />
		  
		</variable>
	</program> 
 *
 *
 */
public class DownloadList extends SuperPlayer{

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
	
	public	DownloadList(){
		super();

	}	
	/**
	 * 
	 */
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		//broker	=	getDbConnection();
		//db		=	new Database(broker);			

	}
	/**
	 * 
	 */
	public	String	dispatch(){
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		if(cmd.equals("EXECUTE")){
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
		
		if(!editmode){
			putParameter(MESSAGE,"");

		}
		
		LOG.sp	=	true;
		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}

}
