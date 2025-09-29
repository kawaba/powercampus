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
import jbbs.BbsInfoDB;
import jbbs.BbsVar;
import student.Student;
import tktools.Gear;


/**
 *　ログイン認証を行う
 *
 	#
	# ##################
	#   BbsLogin
	# ##################
	#
	<program $login.BbsLogin>
		<dispatch  html=BbsLogin.html  number=15  class=login.BbsLogin />
		<variable>
		  <receive   NUMBER />
		  <accept    CMD />
		  
		  <form      UID PASSWORD />
		</variable>
	</program>
 */
public class BbsLogin extends SuperPlayer implements LoginVar, BbsVar {

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
	BbsInfoDB			bbsDB;
	Database			db;
	
	public	BbsLogin(){
		super();
		if(LOG.fa) LOG.println("■ BbsLogin #コンストラクタ");
	}	
    /**
     * 論理ユーザー登録プログラム名を返す
     */
    String	getRegistProcessName(){
        return	DISPATCH_BBS_REGIST;
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
	@Override
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		if(LOG.fa) LOG.println("■BbsLogin #initialize()");
		broker		=	getDbConnection();
		bbsDB		=	new	BbsInfoDB( para.getDomainGroupName(), broker);// グループ名は固定
		db			=	new	Database(broker);

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
		if(LOG.fa) LOG.println("■BbsLogin #dispatch()：");
		
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

		if(cmd.equals("LOGIN")){
			/*
			 * ログイン認証
			 */
			String result	=	login();
			if(LOG.fa) LOG.println("□BbsLogin #dispatch()：result=" + result);
			
			if(result.equals("OK")){
			    setSession( getParameter(UID));
			    
				/* パラメータはクリアしておく */
				putParameter(PASSWORD,"");
				ret 		= 	DISPATCH_RETURN;
				
			}else if(result.equals("GUEST")){
			    
			    /*
			     * guest ユーザーはデータベースに初期登録してある
			     */
			    setSession( "guest");
			    
			    /* パラメータはクリアしておく */
				putParameter(PASSWORD,"");
				ret 		= 	DISPATCH_RETURN;
			
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
	 * 入力されたメールアドレスとパスワードを調べて認証する
	 * 
	 * @return		OK(認証した), ERROR(認証失敗), GUEST(ゲスト)
	 */
	public String login(){
		if(LOG.fa) LOG.println("■BbsLogin #login()");
		
		String 		uid		=	null;
		String 		passwd	=	null;
		String		result	=	"OK";

		// 全てブランクならゲストとみなす
		uid   	= getParameter(UID).trim();
        passwd 	= getParameter(PASSWORD).trim();
        
        
		if(uid.length()>10) {	// SQLインジェクション対策
			putParameter(MESSAGE, "★不正なログイン操作です");
			result	=	"ERROR";
		}        
        
        /*
		if(isEmpty(uid)&&isEmpty(passwd)){
		    uid		=	"guest";
		    passwd	=	"guest";
		}
		*/

		if(isEmpty(uid)||isEmpty(passwd)){ // SQLインジェクション対策
			putParameter(MESSAGE, "★不正なログイン操作です");
			return "ERROR";
		}

		// ブランクチェック
        if(isBlank(uid, passwd)){
        	putParameter(MESSAGE, "★不正なログイン操作です");
        	return "ERROR";

        
        // ゲストパスワードの場合
        }else if(uid.equals("guest")){
			putParameter(MESSAGE, "★不正なログイン操作です");// SQLインジェクション対策
			return "ERROR";        	
            //result	=	"GUEST";	
        
        // パスワードチェック
        }else{
        	boolean 	auth	= 	authPassword(uid, passwd);
        	if(!auth){
        		putParameter(MESSAGE,"メールアドレスまたはパスワードが間違っています");
        		result	=	"ERROR";
        	}
        }
        return	result;
	}
	/**
	 * IDとパスワードが空白かどうか調べる
	 * @return		空白のときtrue
	 */
	boolean	isBlank(String uid, String passwd){
		if(LOG.fa) LOG.println("■BbsLogin #isBlank()");


		if(isEmpty(uid)){
            putParameter(MESSAGE,"ユーザー名を入力してください");
            return	true;

		}else if(isEmpty(passwd)){
            putParameter(MESSAGE,"パスワードを入力してください");
            return	true;

		}
		return	false;
		
	}
	/**
	 * パスワードが正しいか調べる
	 * @param uid
	 * @param passwd
	 * @return
	 */
	boolean	authPassword(String uid, String passwd){
	    
	    if(chkBbsInfoDB(uid, passwd)){
	        return	true;
	    
	    }else if(chkStudentDB(uid, passwd)){
	        return	true;
	        
	    }else if(chkMenberDB(uid, passwd)){
	        return	true;
	        
	    }else{
	        return	false;
	    }
	}
	
	boolean	chkBbsInfoDB(String uid, String passwd){
	    
	    Hashtable	rec	=	new	Hashtable();
	    bbsDB.readBbsInfo(uid, rec);
	    String		registeredPasswd	=	Gear.strHash(rec, BbsInfoDB.USER_PASSWD);
	    if(passwd.equals(registeredPasswd)){
	        return	true;
	    }
	    return	false;	    
	}
	/**
	 * 全ての所属について、所属学生でないか調べる
	 * @param uid
	 * @param passwd
	 * @return
	 */
	boolean	chkStudentDB(String uid, String passwd){
	    
	    Vector	list	=	db.shozokuList();
	    if(list.size()==0)	return	false;
	    
	    int		n	=	list.size();	
	    for(int i=0; i<n; i++){
	        String	szDB =	(String)list.get(i);
			Student	st			=	new Student(szDB,uid,db);
			if(!st.isEmptyRecord()){
			    String	DBpasswd 	=	(st.stPasswd()).trim();
			    if(passwd.equals(DBpasswd)){
			        /*
			         * ユーザーをデータベースに登録してtrueを返す
			         * 登録済みの場合は失敗するだけ。実害はない。
			         */
			        addStudentUser(st, passwd);
			        return true;	// OK	        
			    }
			}
	    }
	    return	false;
	}
	
	boolean	chkMenberDB(String uid, String passwd){
	    
	    // ブランクチェック
        if(isBlank(uid, passwd)){
        	return	false;
        }
        // パスワードチェック
	    boolean	auth	= 	db.isValidUser(uid, passwd);
	    if(!auth){
	        return	false;
	    }        
        /*
         * ユーザーをデータベースに登録してtrueを返す
         */
        addTeacherUser(uid, passwd);
        return	true;
	}
	/**
	 * 教師ユーザーをデータベースに登録する
	 * @param mail
	 * @param passwd
	 */
	void	addTeacherUser(String uid, String passwd){
	    
	    Hashtable ht	=	new	Hashtable();
	    db.MembersInfo(uid, ht);
	    
	    String		pass	=	passwd;
	    String		name	=	strHash(ht, UNAME);
	    String		mail	=	strHash(ht, MAIL);
	    String		div		=	strHash(ht, DIVISION);
	    Hashtable	rec		=	setInitialData(uid, pass, div, name, mail);
	    
	    bbsDB.insertBbsInfo( uid, rec);	    
	    
	}
	/**
	 * 学生ユーザーをデータベースに登録する
	 * @param st
	 * @param password
	 */
	void	addStudentUser(Student st, String password){
	    
	    String	uid		=	st.id();
	    String	pass	=	password;
	    String	name	=	st.kname();
	    String	mail	=	st.email();
	    Hashtable	rec	=	setInitialData(uid, pass, DIV_STUDENT, name, mail);
	    
	    bbsDB.insertBbsInfo( uid, rec);
	}
	
	/**
	 * ユーザー情報の初期値をハッシュにセットする
	 * @param userid
	 * @param passwd
	 * @param division
	 * @param name
	 * @param mail
	 * @return
	 */
	public Hashtable	setInitialData(String userid, String passwd, String division, String name, String mail){
		Hashtable	rec	=	new Hashtable();
		//
		rec.put(BbsInfoDB.USER_ID		,userid);
		rec.put(BbsInfoDB.USER_PASSWD	,passwd);
		rec.put(BbsInfoDB.DIVISION		,division);
		rec.put(BbsInfoDB.NAME			,name);
		rec.put(BbsInfoDB.MAIL			,mail);
		//
		rec.put(BbsInfoDB.HANDLE		,"");
		rec.put(BbsInfoDB.ICON_FILE		,"");
		rec.put(BbsInfoDB.SIGNATURE		,mail);
		rec.put(BbsInfoDB.FORMAT		,para.formatTemplate());
		rec.put(BbsInfoDB.EDITOR		,"20");	
		
		return	rec;
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
		if(LOG.fa) LOG.println("■BbsLogin #display()");
		
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
