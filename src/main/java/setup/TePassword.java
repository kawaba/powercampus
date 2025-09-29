/*
     Power Campus TePassword

*/
package setup;

import 	java.util.*;
import	java.io.*;
import database.*;
import framework.*;
//
/**
 * パスワード変更
 *  
 	#
	# ##################
	#   Sample
	# ##################
	#
	<program $setup.TePassword>
		<dispatch  html=passwd_teacher.html  number=120  class=setup.TePassword />
		<variable>
		<variable>
		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA/>
		  <accept    CMD    UPLODE  />
		  <keep      />
		  
		  <work      />
		  <form      passwd1 passwd2/>
		</variable>
	</program>  
 *
 */

public class TePassword extends SuperPlayer{

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
	Database			db;
	
	public	TePassword(){
		super();
		if(LOG.fa) LOG.println("■ TePassword #コンストラクタ");
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

	}
	/**
	 * 受け入れコードにより処理を分岐させるメソッド．
	 * 受け入れコードは getParameter(CMD) で受け取る
	 * 
	 * 終了コードとして次に起動したいプログラムエイリアス（論理プログラム名）を指定する
	 * 論理プログラム名は dispatch.xml に指定したものを使う
	 * 
	 * 自分自身の画面を再表示したい場合は終了コードに DISPATCH_DEFAULT を指定する
	 * 呼び出しプログラムへ復帰するには終了コードに   DISPATCH_RETURN  を指定する
	 *
	 * disp_mode とは
	 * 　　次にこの画面を表示するときの表示モード(DISP_NEW=新規表示、DISP_EDIT=編集表示)．
	 * 　　あるいは次に起動するプログラムで使われる表示モード
	 *　（注）
	 * 　　他のプログラムが終了して、DISPATCH_RETURN でこのプログラムが呼び出される時は 
	 * 　　コントローラーにより display(boolean disp_mode) へ直接復帰する．
	 * 　　この場合、disp_mode はコントローラーによってDISP_EDIT に設定されている 
	 */	
	public	String	dispatch(){
		if(LOG.fa) LOG.outHash(htb,"■Sample #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		if(cmd.equals("DO")){
			updatePasswd();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示

		}else if(cmd.equals("RETURN")){
			disp_mode	=	DISP_NEW;
			ret			=	DISPATCH_RETURN;	// 復帰			

		}else{
			/* 
			 * cmd には起動したいクラスキーが入っている（$some.prog）
			 * 該当のクラスを起動する
			 */
			disp_mode	=	DISP_NEW;
			ret	=	cmd;
		}
		/*
		 * 次に表示するときあるいはリターンしてきた時の表示モードをセットしてからリターンする
		 */
		putParameter(DISP_KEY,disp_mode);
		return	ret;
	}
	//
	//  パスワードをチェックして更新する
	//
	void updatePasswd(){
		//
		String passwd1	= getParameter("passwd1");
		String passwd2	= getParameter("passwd2");
		String msg		= checkPasswd( passwd1, passwd2 );
		putParameter(MESSAGE,msg);
		//
		if(msg.length() > 0) {
			return;
		}
		String userid = getParameter(TUID);
		changePasswd(userid, passwd1);
		//
		putParameter(MESSAGE,"パスワードを変更しました");
		putParameter("passwd1","");
		putParameter("passwd2","");
		//
		return;
	}
	//
	//　パスワードをチェックする
	//
	String checkPasswd(String passwd1,String passwd2){
		//
		String msg		= "";
		//
		int result		= isHankaku( passwd1, 5, 20);
		if(result == -1) { msg = "５文字以上の半角英数字で作成してください";}
		if(result ==  1) { msg = "２０文字以下の半角英数字で作成してください";}
		if(result ==  9) { msg = "半角英数字のみで作成してください（記号はピリオドのみ）";}
		//
		if(!passwd1.equals(passwd2)) {msg = "入力した二つのパスワードが一致しません";}
		return msg;
	}
	//
	//　パスワードを変更する
	//
	int changePasswd(String userid , String passwd){
		//
		return db.updateMemberPasswd(userid,passwd);
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

		if(!editmode){
			putParameter(MESSAGE,"");
		}
		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}
}

