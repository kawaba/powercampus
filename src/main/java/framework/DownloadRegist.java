/*
 * 作成日: 2005/09/12
 *
 */
package framework;

import java.io.*;
import java.util.*;

import mailutil.MailUtil;
import tktools.*;

/**
 *
 *
 	#
	# ##################
	#  DownloadRegist
	# ##################
	#
	<program $framework.DownloadRegist>
		<dispatch  html=DownloadRegist.html  number=99991  class=framework.DownloadRegist />
		<variable>
		  <receive   NUMBER STAMP />
		  <accept    CMD />
		  
		  <form      name mail shozoku  />
		</variable>
	</program> 
 *
 *
 * 変数の説明
 *
 * 1. receive 
 * 2. accept
 * 3. keep
 * 4. form    enter---テキストエリアの文字列
 *
 * 
 */
public class DownloadRegist extends SuperPlayer{

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
	//DbConnectionBroker	broker;
	/**
	 * ユーザー検索のためのクラス
	 */
	//Database	db;
	
	public	DownloadRegist(){
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
		
		if(cmd.equals("REGIST")){
			/*
			 *　プログラム起動
			 */
			String	msg	=	chk();
			putParameter(MESSAGE, msg);
			
		    if(isEmpty(msg)){
				
		        notice();
		        
		        disp_mode	=	DISP_NEW;
				ret			=	"$framework.DownloadList";
			}else{
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;
			}

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
	String	chk(){
	    
	    String	msg		=	"";
	    
	    String	name	=	getParameter("name");
	    String	mail	=	getParameter("mail");
	    String	shozoku	=	getParameter("shozoku");
	    
	    if(isEmpty(name)){
	        msg	=	"氏名を記入してください";
	    }
	    if(isEmpty(mail)){
	        msg	=	"メールアドレスを記入してください";
	    }
	    if(isEmpty(shozoku)){
	        msg	=	"所属を記入してください";
	    }
	    if(!StringGear.isMailaddress(mail)){
	        msg	=	"メールアドレスが正しくありません";
	        
	    }
	    return	msg;
	    
	}
	/**
	 * 通知
	 *
	 */
	void	notice(){
		//  登録内容をメールでユーザーに送信する
		String	from	= getParameter("mail");
		String	to		= "takashi_kawaba@kwassui.ac.jp";
		
		String	title	= "★ ダウンロード通知";
		String	body	= "　氏名　：" + getParameter("name")	+ CR +
				          "　所属　：" + getParameter("shozoku")	+ CR +
				  		  "　メール：" + getParameter("mail")	+ CR;
		
		MailUtil	ml	=	new	MailUtil();
		/*
		 * ★ 2005.9. NOTE
   　	 * Windows プラットフォームでは
         * サーブレットでのエンコードを "text/plain; charset=MS932" と指定すればCp932は不要
         * ただし、電子メールでは文字化けするので出力時のみに forJisMail() でJISに変換する
   　    *
   　    * Linux プラットフォームでは
         * サーブレットでのエンコードを "text/plain; charset=Shift-JIS" と指定してCp932を利用する
         * ただし、電子メールでは出力時には適用しない
         *
		 */
		if(Cp932.isCp932){
		    // Windowsシステムなら
		    ml.send_To_email(to, from, Cp932.forJisMail(title), Cp932.forJisMail(body), para);

		}else{
		    // linux なら
		    ml.send_To_email( to, from, title, body, para);
		    
		}		
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
			putParameter("_name","");
			putParameter("_mail","");
			putParameter("_shozoku","");
		}
		
		LOG.sp	=	true;
		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}

}
