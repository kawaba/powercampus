/*
 * 作成日: 2005/08/18
 *
 */
package login;

import java.io.*;
import java.util.*;

import	tktools.*;
import	database.*;
import	jbbs.*;
import framework.*;


/**
 *
 *
 *　新規ユーザー登録
 *
 	#
	# ##################
	#   BbsRegist
	# ##################
	#
	<program $login.BbsRegist>
		<dispatch  html=BbsRegist.html  number=30  class=login.BbsRegist />
		<variable>
		  <receive   NUMBER />
		  <accept    CMD />
		  
		  <form      UID PASSWORD MAIL UNAME />
		</variable>
	</program>
 */
public class BbsRegist  extends SuperPlayer{

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

	/** ユーザーID発生用 */
    String 		numberFile; 
	
	/**
	 * データベースプール
	 */
	DbConnectionBroker	broker;
	/**
	 * ユーザー検索のためのクラス
	 */
	BbsInfoDB			bbsDB;
    Database            memberDB;	
	
	/**
	 * 作成したユーザーID
	 */
	String			teUid;
	
	public	BbsRegist(){
		super();
		if(LOG.fa) LOG.println("■ BbsRegist #コンストラクタ");
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
    	
		broker		=	(DbConnectionBroker)(htb.get(BROKER));
    	memberDB	=	new Database(broker);
    	bbsDB		=	new	BbsInfoDB(para.getDomainGroupName(),broker);// グループ名は固定
    	
    	/* 存在チェックをしてなければ作成する */
    	//createGroupDB();
	}	


	/**
	 * 受け入れコードにより処理を分岐させるメソッド．
	 * 
	 * 受け入れコードはシステムハッシュ htb から strHash(htb, DISPATCH_KEY); で受け取る
	 * 
	 * 終了コードとして次に起動したいプログラムエイリアス（論理プログラム名）を指定する
	 * 論理プログラム名は dispatch.xml に指定したものを使う
	 * 
	 * 自分自身の画面を再表示したい場合は終了コードに "SELF" を指定する
	 * 呼び出しプログラムへ復帰するには終了コードに "RETURN" を指定する
	 * 
	 * @return
	 */	
	public	String	dispatch(){
		if(LOG.fa) LOG.outHash(htb,"■■ BbsPost #dispatch()");
		/*
		 * 処理分岐
		 */
		cmd			=	Gear.strHash(htb,DISPATCH_KEY);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;				// WEB表示
		disp_mode	=	DISP_NEW;
		
		if(Gear.isEmpty(cmd)||cmd.equals("REGIST")){// 登録ボタン
			/*
			 *  データの正当性チェック
			 */
			if(isValid()){
				/*
			     * IDを登録する
			     * 
				 * データベースに登録しユーザー環境を作成する
				 * セッションは作成しないので，リターンしてもセッション切れとなり
				 * login 処理をコールすることになる
				 */
			    if(regist()){					// 同じメールアドレスがすでにあればエラーで帰る
			        disp_mode	=	DISP_EDIT;
					ret			=	DISPATCH_RETURN;	// 呼び出し元へ復帰
					
				}else{
				    htb.put("_msg","★このユーザー名はすでに使われています");
				    disp_mode	=	DISP_EDIT;
					ret			=	DISPATCH_DEFAULT;	// 再表示
				}
				
			}else{
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;	// 再表示
			}
		
		}else	if(cmd.equals("CLEAR")){		// クリアボタン
			disp_mode	=	DISP_NEW;
			ret			=	DISPATCH_DEFAULT;	// 再表示
			
		}else	if(cmd.equals("RETURN")){		// 戻るボタン
			disp_mode	=	DISP_NEW;
			ret			=	DISPATCH_RETURN;	// 戻る
			
		}else{
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示
			
		}
		/*
		 * 次に表示するときあるいはリターンしてきた時の表示モードをセットしてからリターンする
		 */
		htb.put(DISP_KEY,disp_mode);
		return	ret;
	}
	/**
	 * 登録データの正当性チェック
	 * @return
	 */
	public boolean isValid(){
		if(LOG.fa) LOG.outHash(htb,"BbsRegist #isValid() : ★登録データの正当性チェック");

		boolean	ret	=	false;
		String 		msg = 	isOK(); 
        htb.put("_msg",msg);
        
        return	msg.equals("ok"); 

	}
	public	String	isOK(){
		//
        // 全項目に記入が必要
		//
		String	userid		= getParameter(UID).trim();
		String	password	= getParameter(PASSWORD).trim();
		String	mail		= getParameter(MAIL).trim();
		String	name		= getParameter(UNAME).trim();
		//
        if(Gear.isSpaceOrNull(userid))      	return "★ ユーザーIDを記入してください";
		if(!StringGear.isMailaddress(mail))		return	"★ メールアドレスが正しくありません";
        if(Gear.isSpaceOrNull(password))      	return "★ パスワードを記入してください";
		if(!StringGear.isHankaku( password))	return	"★ パスワードは半角英数字で作成してください";
		if(password.length()<5)					return "★ パスワードは５文字以上の長さにしてください";
        if(Gear.isSpaceOrNull(name))      		return "★ 漢字氏名を記入してください";
		//
        return "ok";
    }	
	/**
	 * ユーザー登録．<br>
	 * 重複登録はエラーとなる
	 * @return
	 */
	public	boolean regist(){
	    /*
	     * membersInfoテーブル（教師登録用）を参照して、教師ユーザーなら
	     * DIVISIONを合わせておく
	     */
	    String		uid			=	getParameter(UID).trim();
	    String		division	=	DIV_STUDENT;

	    Hashtable	member		=	new	Hashtable();
	    int			cnt			=	memberDB.MembersInfo(uid, member);
	    if(cnt>0){
	        division	=	Gear.strHash(member, DIVISION);	    
	    }
	    Hashtable	rec	=	setInitialData(	getParameter(UID).trim(), 
											getParameter(PASSWORD).trim(),
											division,
											getParameter(UNAME).trim(),
											getParameter(MAIL).trim()	);
	    
	    /*
	     * すでに同名のユーザーが登録されていないか調べる
	     * （登録してみる）
	     */
	    int	result	=	bbsDB.insertBbsInfo(uid, rec);	
		if(result==0){
			return false;
		}
		return	true;
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
	public  void	display(boolean editmode){
		if(LOG.fa)	LOG.println("■ BbsRegist #display(boolean editmode)");
		
		if(!editmode){
			htb.put(MESSAGE,"");
			clearScreen(htb, para);
			
		}
		
		/* strHash(htb,DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v,htb);		
	}
    /**
     * ユーザー登録ページの初期値を設定する
     * @param _htb
     * @param para
     * @return
     */
    Hashtable  clearScreen(Hashtable _htb,Param para) {
    	if(LOG.fa) LOG.println("class exwork #clearScreen() : ユーザー登録ページの初期値を設定する の先頭です");
        
        _htb.put("_userid","");
        _htb.put("_passwd","");
        _htb.put("_userName","");
        _htb.put("_userMail","");
		//
        return  _htb;
    }	



}
