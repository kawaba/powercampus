/*
 * 作成日: 2004/12/17
 *
 * TODO
 */
package login;

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Vector;
import database.Database;
import database.DbConnectionBroker;
import framework.LOG;
import framework.Param;
import framework.SuperPlayer;

/**
 *　ログイン認証を行う
 *
 	#
	# ##################
	#   Tlogin
	# ##################
	#
	<program $login.Tlogin>
		<dispatch  html=tlogin.html  number=10  class=login.Tlogin />
		<variable>
		  <receive   NUMBER />
		  <accept    CMD />
		  
		  <form      UID PASSWORD/>
		</variable>
	</program>
 */
public class Tlogin extends SuperPlayer implements LoginVar{

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
	
	/* 2023.09 削除してみた
	public	Tlogin(){
		super();
		if(LOG.fa) LOG.println("■ Tlogin #コンストラクタ");
		LOG.println(para.getCommonUsername());
		LOG.println(para.getCommonUsername());
	}
	*/	

	/**
	 * コンストラクタが引数をもてないので、コンストラクタで行う内容をこの initialize() に書く．
	 * out, htb, para は SuperPlayer クラスのインスタンス変数なので、
	 * 引数にとらずとも利用できるが、変数を明示する意味で列挙してある
	 * 
	 * @param out			出力ポインタ
	 * @param htb			システムハッシュ
	 * @param para			パラメータ
	 */
	@Override
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		if(LOG.fa) LOG.println("■Tlogin #initialize()");
		broker	=	getDbConnection();
    	db		=	new Database(broker);

	}
	/**
	 * 受け入れコードにより処理を分岐させるメソッド．
	 * 
	 * 受け入れコードはシステムハッシュ htb から strHash(htb, DISPATCH_KEY); で受け取る
	 * コードは getParameter(DISPATCH_KEY)　と書いてよい
	 * 
	 * 終了コードとして次に起動したいプログラムエイリアス（論理プログラム名）を指定する
	 * 論理プログラム名は dispatch.xml に指定したものを使う
	 * 
	 * 自分自身の画面を再表示したい場合は終了コードに "SELF" を指定する
	 * 呼び出しプログラムへ復帰するには終了コードに "RETURN" を指定する
	 * 
	 * @return
	 */	
	@Override
	public	String	dispatch(){
		if(LOG.fa) LOG.println("■Tlogin #dispatch()：");
		
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
		ret			=	DISPATCH_DEFAULT;				// WEB表示
		disp_mode	=	DISP_NEW;
		cmd			=	getParameter(DISPATCH_KEY);		// null の場合は ""を返す
		if(LOG.fa) LOG.println("□Tlogin #dispatch()：CMD=" + cmd);
		
		if(isEmpty(cmd)||cmd.equals("LOGIN")){
			/*
			 * ログイン認証
			 */
			String result	=	login();
			if(LOG.fa) LOG.println("□Tlogin #dispatch()：result=" + result);
			
			if(result.equals("OK")){
				
			    makeSession();
				
				/* パラメータはクリアしておく */
				putParameter(PASSWORD,"");
				ret 		= 	DISPATCH_RETURN;
				
			}else if(result.equals("REGIST")){
			    putParameter(UID,"");
			    putParameter(PASSWORD,"");
			    disp_mode	=	DISP_NEW;
				ret			=	DISPATCH_REGIST;	// 新規ユーザー登録
				
			}else{
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;	// 再表示
			}
		    
		}else{
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示
		}
		/*
		 * 次に表示するときあるいはリターンしてきた時の表示モードをセットしてからリターンする
		 */
		putParameter(DISP_KEY,disp_mode);
		return	ret;
	}

	/**
	 * セッションに変数を設定する
	 * 必要なシステムデータをシステムハッシュに格納する
	 * 
	 * @param htb
	 * @param mail
	 * @param request
	 */
	void	makeSession(){
		if(LOG.fa) LOG.println("■Tlogin #makeSession()");
		/*
		 * セッション変数を設定する
		 * ここでセッション共通変数用のハッシュも作成される
		 */
		setSession(getParameter(UID));
	
	}	
	/**
	 * 入力されたユーザー名とパスワードを調べて認証する
	 * 
	 * @return		OK(認証した), ERROR(認証失敗), REGIST(新規登録)
	 */
	public String login(){
		if(LOG.fa) LOG.println("■Tlogin #login()");
		
		String 		userid	=	null;
		String 		passwd	=	null;
		String		result	=	"OK";

		// ブランクチェック
		userid  = getParameter(UID).trim();
        passwd 	= getParameter(PASSWORD).trim();
        
        
		
		if(userid.length()>10) {	// SQLインジェクション対策
	        putParameter(MESSAGE,"不正なログイン操作です");
	        return	"ERROR";
		}
        
        if(isBlank(userid, passwd)){
        	result	=	"ERROR";
        
        }else if(isRegist(userid, passwd, para)){
        	// 新規登録パスワードの場合
            result	=	"REGIST";	
        
        }else{
            // パスワードチェック
    	    boolean	auth	= 	db.isValidUser(userid, passwd);
    	    if(!auth){
    	        putParameter(MESSAGE,"ユーザー名またはパスワードが間違っています");
    	        return	"ERROR";
    	    }
        }
        return	result;
	}
	
	/**
	 * メールアドレスまたはパスワードが空白かどうか調べる
	 * @return		空白のときtrue
	 */
	boolean	isBlank(String userid, String passwd){
		if(LOG.fa) LOG.println("■Tlogin #isBlank()");

		if(isEmpty(userid)){
            putParameter(MESSAGE,"ユーザー名を入力してください");
            return	true;

		}else if(isEmpty(passwd)){
            putParameter(MESSAGE,"パスワードを入力してください");
            return	true;

		}
		return	false;
		
	}

	/**
	 * 新規ユーザー登録かどうか判定する．<br>
	 * 入力されたユーザー名とパスワードが新規ユーザー登録を意味するもの
	 * かどうか調べる．新規登録用のメールアドレスとパスワードはシステム
	 * パラメータとしてあらかじめ登録してある．
	 * 
	 * @param mail
	 * @param passwd
	 * @param para
	 * @return
	 */
	boolean	isRegist(String userid, String passwd, Param para){
		if(LOG.fa) LOG.println("■Tlogin #isRegist()");
		
        String	commonUsername	= para.getCommonUsername();	// ユーザー登録用ユーザー名
		String 	commonPasswd	= para.getCommonPasswd();	// 同　パスワード
		LOG.println("■Tlogin #isRegist()");
		LOG.println(para.getCommonUsername());
		LOG.println(para.getCommonPasswd());
		
		if(userid.equals(commonUsername) && passwd.equals(commonPasswd)){
			return	true;
		}
		return	false;
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
	@Override
	public  void	display(boolean editmode){
		if(LOG.fa) LOG.println("■Tlogin #display()");
		
		if(!editmode){// 新規表示
			putParameter(MESSAGE,"");
			putParameter(UID,"");
			putParameter(PASSWORD,"");
		}
		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}

}
