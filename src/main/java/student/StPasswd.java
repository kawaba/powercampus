/*
 * 学生のパスワード変更
 */
package student;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import database.Database;
import database.DbConnectionBroker;
import framework.LOG;
import framework.Param;
import framework.SuperPlayer;
import framework.SuperPrint;
import mailutil_service.Recipient;
import mailutil_service.Util;
import tktools.Gear;
import tktools.jmAuthenticate;
import tktools.jmSender;

/**
 *
 *
	#
	# ##################
	#   StPasswd
	# ##################
	#
	<program $StPasswd>
		<dispatch  html=passwd.html  number=3010     class=student.StPasswd />
		<variable>
		  <receive  GROUP  NUMBER STAMP StUID StCLASSINFO StNAME StMAIL StKEITAI />
		  <accept   SUBMIT    />
		  <keep      />
		  
		  <form    _passwd1 _passwd2 />
		</variable>
	</program>
 *
 *
 * 変数の説明
 *
 * 1. receive 
 * 2. accept
 * 3. keep
 * 4. form
 *
 *
 */
public class StPasswd extends SuperPlayer{

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
	
	String				szDB;		// GROUP
	String				stNumber;	// StUID
	String				stMail;		// StMAIL
	String				stName;		// StNAME	
	
	public	StPasswd(){
		super();
		if(LOG.fa) LOG.println("■ StPasswd #コンストラクタ");
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
		if(LOG.fa)	LOG.println("■ StPasswd #initialize()");

		broker			=	(DbConnectionBroker)htb.get(BROKER);
		db				=	new Database(broker);

		szDB 			= Gear.strHash(htb,GROUP);
		stNumber 		= Gear.strHash(htb,StUID);
		stMail 			= Gear.strHash(htb,StMAIL);
		stName 			= Gear.strHash(htb,StNAME);
    }
    //
    @Override
	public String dispatch(){
		if(LOG.fa)	LOG.println("■ StPasswd #dispatch()");

		/*
		 * 処理分岐を判断するキーはシステムハッシュからParam.DISPATCH_KEYをキーとして取り出す
		 * リターンコードと次に表示するWEBの表示モードは設定忘れを防ぐため規定値をセットしておく
		 */
		String 	cmd			=	strHash(htb,DISPATCH_KEY);
		String	ret			=	DISPATCH_DEFAULT;	// WEB表示
		String	disp_mode	=	DISP_NEW;			// クリアーして新規表示

		if(cmd.equals("PASSWD")){
			if(LOG.fa)  LOG.println("■ StPasswd #dispatch()");

			String passwd1  = (String)htb.get("_passwd1");
			String passwd2  = (String)htb.get("_passwd2");
			Student st 		= null;
			//
			if( checkPasswd(passwd1,passwd2,htb) ){
				st = changePasswd(szDB,stNumber,passwd1);
				if(st==null){
					htb.put("_msg","★ パスワード変更でデータベースの更新に失敗しました");
					disp_mode	=	DISP_EDIT;
				}else{
					htb.put("_msg","◎ パスワードを変更しました.この後、結果のメールが届きます.");
					forStudent(st, htb, para);	// 変更通知を送信する
					disp_mode	=	DISP_NEW;
				}
			}else{
				disp_mode	=	DISP_EDIT;

			}
			ret			=	DISPATCH_DEFAULT;	// WEB表示
			

		}else if(cmd.equals("VIEW")){
			/* 
			 * 別画面表示のとき
			 * 新規表示モードで画面表示
			 */
			disp_mode	=	DISP_NEW;
			ret			=	DISPATCH_DEFAULT;	// WEB表示
			
		}else if(cmd.equals("RETURN")){
			/* 
			 * 呼び出し元プログラムへ戻る
			 */ 
			ret	=	DISPATCH_RETURN;

		}else{
			/* 
			 * cmd には起動したいクラスキーが入っている
			 * 戻りは編集モードではない＝レコードを再読込して表示するので状態が変わっている可能性もある
			 */
			disp_mode	=	DISP_NEW;
			ret			=	cmd;
		}
		/*
		 * 表示モードをセットしてからリターンする
		 */
		htb.put(DISP_KEY,disp_mode);
		return	ret;
    }
	//
	// 入力されたパスワードをチェックする
	//
	boolean	checkPasswd(String passwd1,String passwd2,Hashtable htb){
		if(LOG.fa)	LOG.println("■ StPasswd #checkPasswd()");
		//
		if( (passwd1==null)||(passwd2==null)||(passwd1.length() < 1)||(passwd2.length() < 1) ){
			htb.put("_msg","★ パスワードを入力してください");
			return false;
		}else if(!isHankaku(passwd1)){
			htb.put("_msg","★ パスワードは半角英数字で作成してください");
			return false;
		}else if(!passwd1.equals(passwd2)){
			htb.put("_msg","★ 二つのパスワードが同じではありません");
			return false;
		}else if( passwd1.length()< 5 ){
			htb.put("_msg","★ パスワードは５文字以上で作成してください");
			return false;
		}
		return true;
	}
	//
	// パスワードを変更する
	//
	Student changePasswd(String szDB,String stNumber,String passwd){
		if(LOG.fa)	LOG.println("■ StPasswd #changePasswd()");
		//
		Student st = new Student(szDB,stNumber,db);
		st.set_stPasswd(passwd);
		int n = st.update();
		//
		return st;
	}
	//
	//  パスワード変更通知を学生へ送信する
	//
    public void forStudent(Student st,Hashtable htb,Param para){
		if(LOG.fa)	LOG.println("■ StPasswd #forStudent()");
		//
		String Number	= st.id();
		String Name		= st.kname();
		String Mail		= st.email();
		String Keitai	= st.keitai();
		String Passwd	= st.stPasswd();
		//
		Hashtable	tb	=	new Hashtable();
		tb.put("_stNumber",Number);
		tb.put("_stName",Name);
		tb.put("_stPasswd",Passwd);
		tb.put("_stMail",Mail);
		tb.put("_stKeitai",Keitai);
		//
        String from     = para.getSysadmin();							// 管理者のメールアドレス
        String title    = "パスワード変更のお知らせ(mail-and-work)";	// メールタイトル
		//
		// e-mail送信
		String to	= st.email();
		if(!to.equals(Student.EMPTY)){							// 登録があれば送信
			
			String msg  	= 	mktemplateCF(false,tb,para);
			
			/* ***********************************************************
			//sendInfo(host,to,from,title,msg,para,true);
			メールサービスを利用するように変更した(2021.4.1)
			************************************************************** */			
			List<Recipient> ls = Util.createToList(st.id(), to, Util.ms932_utf8(stName));
			Util.sendMailService(from, Util.ms932_utf8(title), "TO", Util.ms932_utf8(msg), ls);
			
			/* ************************************************************ */
		}
		// 携帯メール送信
		to = st.keitai();
		if(!to.equals(Student.EMPTY)){							// 登録があれば送信
			String msg  = mktemplateCF(false,tb,para);
			
			/* ***********************************************************
			//sendInfo(host,to,from,title,msg,para,true);
			メールサービスを利用するように変更した(2021.4.1)
			************************************************************** */			
			
			List<Recipient> ls = Util.createToList(st.id(), to, Util.ms932_utf8(stName));
			Util.sendMailService(from, Util.ms932_utf8(title), "TO", Util.ms932_utf8(msg), ls);
			
			/* ************************************************************ */			
		}
		
        /*
        // e-mail送信
		String to	= st.email();
		if(!to.equals(Student.EMPTY)){							// 登録があれば送信
			if(LOG.fa)  LOG.println("class stwork #forStudent() :e-mailを送信します");
			String msg  = mktemplateCF(true,tb,para);
			String host	= para.sysMailhost();
			sendInfo(host,to,from,title,msg,para,true);
		}
		// 携帯メール送信
		to = st.keitai();
		if(!to.equals(Student.EMPTY)){							// 登録があれば送信
			if(LOG.fa)  LOG.println("class stwork #forStudent() :携帯メールを送信します");
			
			String 		msg  	= 	mktemplateCF(false,tb,para);
			String 		host	= 	para.sysMailhost_keitai();
			sendInfo(host,to,from,title,msg,para,false);
		}
		*/
	}
	//
	// pop-before-smtp 認証を確認して１件のメールを送信する
	//
	public void sendInfo(String host,String to,String from,String title,String msg,Param para,boolean email){
		if(LOG.fa)	LOG.println("■ StPasswd #sendInfo()");
		// 必要なら pop before SMTP のためにシステムユーザー名で認証を受けておく
        String pop = para.popBeforSmtp();
        if(pop.equals("yes")){
			String  user    = "";                                    // POP user名
            String  passwd  = "";                                // pop パスワード
            if(email){
				user    = para.getMailmaster();                                    // POP user名
            	passwd  = para.getMailmasterPass();                                // pop パスワード
            }else{
				user    = para.getMailmaster_k();                                    // POP user名
    	        passwd  = para.getMailmasterPass_k();                                // pop パスワード
			}
			boolean auth    = jmAuthenticate.authenticate(host,user,passwd);          // チェック
            if(!auth){
                String s1 = "★pop before smtp で認証に失敗しました。StUID/PASSWD を確認してください。<P>";
                String s2 = "   host=" + host + "<br>";
                String s3 = "   user=" + user + "<br>";
                //String s4 = "   passwd=" + passwd + "<br>";
				String s5 = "   auth=" + auth + "<br>";
                LOG.errStop(para.getResponseWriter(),s1 + s2 + s3 + s5);
                return;
            }
			if(LOG.fa)  LOG.println("class stwork #sendInfo() :　認証成功");
        }
        try{
            jmSender.send(host,to,from,title,msg);
        }catch(Exception e){
            System.out.println(to + "へのメール送信に失敗しました (" + getDate() + "）");
            System.out.println( e );
        	if(LOG.fa)  LOG.println("class stwork #sendInfo() :　送信失敗");
		}
		if(LOG.fa)  LOG.println("class stwork #sendInfo() :　送信成功");
	}
	//
	// テンプレートにパラメータを埋め込んで送信本文を作る
	//
	String mktemplateCF(boolean email,Hashtable tb,Param para){
		if(LOG.fa)	LOG.println("■ StPasswd #mktemplateCF()");
		//
		String confMailTempPath 	= para.stInfoTempPath(); 			// 登録情報テンプレート
        //if(!email) confMailTempPath = para.stInfoTempPath_k();			// keitai
		//
		StringWriter	strOut	= new StringWriter(5000);
        PrintWriter		sout    = new PrintWriter( strOut );
		//
		SuperPrint		sp		= new SuperPrint(sout);
        sp.repPrint(confMailTempPath,tb,false);    // メールなので false 
        //
        String msg = strOut.toString(); 			// 編集された受領確認用送信メッセージ
		if(LOG.fa)  LOG.println("class stwork #mktemplateCF() : 編集された受領確認用送信メッセージ");
		if(LOG.fa)  LOG.println(msg);
		return msg;
	}


//	///////////////////////////////////////////////////////////////////////////////////////////////////
//
//		 出　　力　　処　　理
//
//	///////////////////////////////////////////////////////////////////////////////////////////////////

    //
    // 画面を表示する
    //
	@Override
	public  void    display(boolean edit){
		if(LOG.fa)	LOG.println("■ StPasswd #display()");
		//
		if(!edit){
			htb.put("_passwd1","");
			htb.put("_passwd2","");
		}
		/* strHash(htb,DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v,htb);
    }

}

