
import java.io.*;
import java.util.*;

import jakarta.servlet.http.*;
import jbbs.*;
import framework.*;
import	tktools.*;

/**
 * BBS用フレームワーク
 *
 */

public class PowerCampusBBS extends PowerCampus {
    
    String	szDB_domain;
    String	ownerKey;
   
    /** デフォルトデータベース */
    protected 	BbsInfoDB			infodb;
    
    /**
     * サーブレット初期化
     * 
     * init() の最後にコールされる追加的な初期化
     * 
     */
    void	moreInitializeSetting(){
        
        szDB_domain	=	"group_" + (property.get("serverIP")).replace('.','_');
        ownerKey	=	property.get("SYS_USER");
        infodb		=	new	BbsInfoDB(szDB_domain, myBroker);
        
        // BbsInfoDB の存在チェックと生成
        createGroupDB();
        
        // ロゴマークファイルを作成する
        CreateDomainLogo();
        return;
    }
	/**
	 * データベーステーブルがなければ作成して、
	 * guest ユーザーを登録しておく
	 *
	 */
	void	createGroupDB(){
	    
	    if(!isCreated()){
	        /*
	         * ユーザーテーブル
	         */
	        db.create_bbsInfo( szDB_domain );
	        int result	=	infodb.insertBbsInfo(ownerKey, BbsUtil.mkGuestProperty(null));
	        //LOG.println(result + "件" );
			/*
			 * 関連テーブル
			 */
			db.create_BbsForum(szDB_domain);
			db.create_BbsThread(szDB_domain);
			db.create_BbsPost(szDB_domain);
			db.create_bbsInfo(szDB_domain);
			db.create_bbslog(szDB_domain);
			db.create_jbbskes(szDB_domain);
	        
	    }
	}
	/**
	 * データベースの存在チェック
	 * @return
	 */
	boolean	isCreated(){
	    /*
	     * ownerkey ---- property.get("SYS_USER") 固定
	     * szDB -------- szDB_domain　　　　　　　固定
	     */
	    Hashtable	rec	=	BbsUtil.getOwnerRecord("guest", szDB_domain, myBroker);
	    if(rec==null){
	    	//LOG.println("rec is null");
			return	false;
	    }
	    //LOG.println("rec is  not null");
	    return	true;
	}
    
    /**
     * ロゴマークファイルを作成する
     * 
     * BBS用のドメイングループ名に基づいたロゴマークファイルを作成する
     * すでに作成されていれば何もしない
     */
    void	CreateDomainLogo(){

	    String	sample		=	property.get("titleLogo");								// サンプルロゴへのフルパス
	    String	logo		=	property.get("logoDir") + szDB_domain + ".gif";			// ロゴファイル
	    
	    File	fp			=	new File(logo);
	    if(!fp.exists()){
		    try {
	            FileGear.copyBinryFile(sample, logo);
	        } catch (IOException e) {
	            	
	        }
	    }
    }

    /**
     * アクションキーワードを返す
     * @return
     */    
    String	getActionKeyword(){
        return	"/servlet/PowerCampusBBS";
    }
    /**
     * 論理ログインプログラム名を返す
     */
    String	getLoginProcessName(){
        return	DISPATCH_BBS_LOGIN;
    }
    
    /**
     * 論理ユーザー登録プログラム名を返す
     */
    String	getRegistProcessName(){
        return	DISPATCH_BBS_REGIST;
    }
    /**
     * 論理開始ページプログラム名を返す
     */
    String	getStartProcessName(){
        return	BBS_PAGE;
    }

    /**
     * dopost(), doget()の最初にサーブレットが利用する
     * システム変数を初期化する
     * 
     * この処理はstartServlet()でコールされる
     * オーバーライドされる可能性がある
     * 
     * @param out
     * @param htb
     * @param para
     * @param response
     * @throws IOException
     */
    public	void	initialize(PrintWriter out, Hashtable  htb, Param para, HttpServletResponse response) throws IOException{
		if(LOG.fa) LOG.println("■PowerCampus #initialize()");
        
        /*
         * いつでもエラー出力できるようにoutをPara に保存
         */
        para.addResponseWriter(out);
		/* 
		 * ブローカーをhashに入れておくことにした 2004.8.15
		 */
		htb.put(BROKER, myBroker);
 		/*
 		 * 分類区分をゲストに設定しておく→setBasicVar()で設定している
 		 */
 		//htb.put(DIVISION, DIV_GUEST);	// 区分はゲスト	
		/*
		 * 暗号化用変数の準備
		 */
        for(int i=0; i<10; i++){
            String s = String.valueOf(i);
            dch.put(CRIPTO[i],s);
            ech.put(s,CRIPTO[i]);
        }
		
    }
    /**
     * doGet 用のプログラム起動シーケンスの制御
     * 
     * セッションは常に切れているものとする
     * getパラメータのコマンド(cmd)を見てコールすべきプログラムを決定する．
     * 　　	コマンドあり	-- ユーザ登録
     * 		コマンドなし	-- ログイン
     * 
     * どちらをコールしても最終的にはデフォルトのトップページが表示されるよう制御する
     * 
     * @param out
     * @param htb
     * @param para
     * @param request
     * @param response
     */
	public void setSeqence(PrintWriter out,Hashtable htb,Param para,HttpServletRequest request, HttpServletResponse response) {
		if(LOG.fa) LOG.println("■PowerCampusBBS #doPostSession()");

		String	cmd	=	Gear.strHash(htb,"cmd");
    	if(Gear.isEmpty(cmd)||cmd.equals("LOGIN")){
        	/*
        	 * デフォルトのページを起動するよう指定しておく
        	 */
    	    htb.put(NUMBER, dispatchObj.getNumber( getStartProcessName() )); // 時間割ページ
    	    /*
        	 * セッションが切れているのでセッションフラグを立てて実行に先立ちLOGINをコール
        	 * することを dispatcher()に指示する
    	     */
    	    htb.put(SESSION_FLAG,"LOGIN");
        	
    	}else{
    	    /*
    	     * getStartProcessName() をスタックに積んでおく.
    	     * 
    	     * getLoginProcessName() は終了後必ず RETURN で戻る．そこで，最終的な戻り先が 
    	     * getStartProcessName() となるようスタックに積む
    	     * 
    	     *  htb.put(SESSION_FLAG,"LOGIN");はgetStartProcessName()にリターンしたとき
    	     * setBasicVar()を実行して基本変数を受け取るために必要となるもの
    	     * 【参照】relay()
    	     */
    	    htb.put(SESSION_FLAG,"LOGIN");
    	    
	        DispatchStack	stk	=	new DispatchStack("", dispatchObj);
	        stk.updateHashWithPush(getStartProcessName(), htb);
	        /*
	         * 
	         * ログインページ（DISPATCH_LOGIN）を実行するようにNUMBERを指定しておく
	         * 最後に セッションフラグを REGIST にしておくことで
        	 * 実行に先立ちREGISTをコールすることを dispatcher()に指示する
	         * 
	         * getRegistProcessName() →(return)→ getLoginProcessName() →(return)→ getStartProcessName()
	         * 
	         * というシーケンスで実行される
	         */
    	    htb.put(NUMBER, dispatchObj.getNumber(getLoginProcessName())); // ログインページ
    	    htb.put(SESSION_FLAG,"REGIST");
    	    
    	}
		// タイムスタンプを書き直す
        String stamp     = DateGear.CalToStr(DateGear.calculateDayH(DateGear.currentDay(),LIMIT)); // ３時間先のタイムスタンプ
		String encStamp  = cryptoDigit(stamp);                          // 簡易暗号化
        htb.put(STAMP,encStamp);
	}  	

 	/**
 	 * 基本変数をシステムハッシュにセットする
 	 * 
 	 * 通常は毎セッションごとに、セッション制御の中でコールされる
 	 * 各処理はこれらの変数を与件(received)として扱うことが出来る。
 	 * 
 	 * セッションに保存されている情報は常に情報を取得するデータベースのキーフィールドの値で
 	 * あるので、これを利用してレコードを読み出すことができる。
 	 * 
 	 * @param htb
 	 * @param request
 	 */
     void	setBasicVar(Hashtable htb, HttpServletRequest request){
		if(LOG.fa) LOG.println("■PowerCampusBBS #setBasicVar()");
 		
        HttpSession session = request.getSession(true);            	// セッションオブジェクトの取得
      	String		id 		= (String)session.getAttribute(PC_ID);	// ID
      	
      	/*
      	 * 
      	 * <<BbsInfoDB>>
      	 *  userid            VARCHAR(12)  PRIMARY KEY
      	 *  passwd            VARCHAR(20)  PRIMARY KEY
      	 *  division          CHAR(1)      DEFAULT '4'
      	 *  name              VARCHAR(30)
      	 *  mail			  VARCHAR(30)
      	 *  handle            VARCHAR(30)
      	 *  iconfile          VARCHAR(30)
      	 *  signature         TEXT
      	 *  formatStyle       TEXT
      	 *  editor            CHAR(2)
      	 * 
      	 * 
      	 *  */
      	
      	Hashtable	rec	=	BbsUtil.getUserInfo(id, szDB_domain, myBroker);
      	if(LOG.fa){
          	LOG.println("setBasicVar() PC_ID =" + id);
          	LOG.println("_userid   = "+Gear.strHash(rec, "_userid"));
          	LOG.println("_division = "+Gear.strHash(rec, "_division"));
          	LOG.println("_name     = "+Gear.strHash(rec, "_name"));
          	LOG.println("_mail     = "+Gear.strHash(rec, "_mail"));
      	}
      	
      	htb.put(UID, 		Gear.strHash(rec, "_userid"));
      	htb.put(DIVISION, 	Gear.strHash(rec, "_division"));
      	htb.put(UNAME, 		Gear.strHash(rec, "_name"));
      	htb.put(MAIL, 		Gear.strHash(rec, "_mail"));
      	/*
      	 * GROUPは固定
      	 */
      	htb.put(GROUP,	szDB_domain);
	    /*
	     * bbsオーナーキーとリレーションキーは固定
	     */
	    htb.put(BbsVar.BBS_OWNER_KEY, property.get("SYS_USER"));
	    htb.put(BbsVar.BBS_RELATION	, szDB_domain);
      	/*
      	 * pc.Conf に記載のあるドメイン名.
      	 */
      	htb.put(DOMAIN, property.get("domain"));
      	/*
      	 * 戻りURLを設定
      	 * 
      	 * 　※以下の値は利用しないようHTMLを改訂した(2005.9)
      	 */
 		String	division	=	Gear.strHash(htb, DIVISION);
 		if(division.equals(DIV_STUDENT) || division.equals(DIV_GUEST)){
 		    htb.put(HOMEURL, property.get("STUDENT_URL"));
 		}else{
 		    htb.put(HOMEURL, property.get("TEACHER_URL"));
 		}
     }	

}
