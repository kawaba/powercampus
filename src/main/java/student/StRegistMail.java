/*
     
	メールアドレス登録


*/
//import	epml.*;
package student;
//
import java.io.PrintWriter;
import java.io.StringWriter;
//import java.text.*;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import database.Database;
import database.DbConnectionBroker;
import framework.LOG;
import framework.Param;
import framework.SuperPlayer;
import framework.SuperPrint;
import net.tkxtools.Mail;
import tktools.Gear;
//import tktools.jmAuthenticate;
//import tktools.jmSender;

/**
*
*
	#
	# ##################
	#   StRegistMail
	# ##################
	#
	<program $StRegistMail>
		<dispatch  html=regMail.html  number=3020 class=student.StRegistMail />
		<variable>
		  <receive  GROUP  NUMBER STAMP StUID StCLASSINFO StNAME StMAIL StKEITAI />
		  <accept   CMD  />
		  <keep      />
		  
		  <form     stNewMail  stNewKeitai conf_i  conf_k />
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
* 		stMail		登録するメールアドレス
* 		stKeitai	登録する携帯メールアドレス
* 		conf_i		e-メール確認番号
* 		conf_k		携帯メール確認番号
*
*
*/

public class StRegistMail  extends SuperPlayer {
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
    
    //
   public StRegistMail(){
        //
        super();
        if(LOG.fa)	LOG.println("■ StRegistMail #コンストラクタ");        
    }
	
	@Override
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		super.setInit(out,htb,para);
		//
		if(LOG.fa)	LOG.println("■ StRegistMail #initialize()");
		if(htb==null) LOG.println("□□ StRegistMail: htb is NULL!!");

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
		if(LOG.fa) LOG.println("■ StRegistMail #dispatch()");
		//
		/*
		 * 処理分岐を判断するキーはシステムハッシュからParam.DISPATCH_KEYをキーとして取り出す
		 * リターンコードと次に表示するWEBの表示モードは設定忘れを防ぐため規定値をセットしておく
		 */
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;

		String mode;
		if(cmd.equals("EMAIL")){
			// e-mail 仮登録
			regEmail(out,htb,para);
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// WEB表示
			

		}else if(cmd.equals("KEITAI")){
			// 携帯 仮登録
			regKeitai(out,htb,para);
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// WEB表示
		
		}else if(cmd.equals("KAKUNIN-I")){
			// e-mail 確認
			confEmail(out,htb,para);
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// WEB表示

		}else if(cmd.equals("KAKUNIN-K")){
			// 携帯 確認
			confKeitai(out,htb,para);
			disp_mode	=	DISP_EDIT;
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
	// e-mail 仮登録の処理
	//
	boolean regEmail(PrintWriter out,Hashtable htb,Param para){
		
		if(LOG.fa) LOG.println("■ StRegistMail #regEmail()");

		//if(DBG.fa) DBG.println("class stwork #regEmail() : e-mail 仮登録の処理 の先頭です");
		/*
		 * 新しいメールアドレス
		 */
		String address	= getParameter("_stNewMail");
		
		if(!check_Address_email(address,htb)){
			if(LOG.fa) LOG.println("★ StRegistMail #regEmail() : 不正なアドレス");
			return false;
		}
		String stNumber		= (String)htb.get(StUID);
		String stMail		= (String)htb.get(StMAIL);
		String szDB			= (String)htb.get(GROUP);			// shozoku = データベース名
		
		//
		// ＤＢに確認番号設定
		Random rand = new Random();
		int		max = 999;
    	String 	ci		= get000type(rand.nextInt(max+1));	// 3桁の乱数
		Student	stw		= new Student(szDB,stNumber,db);
		String	kname	= stw.kname();
		//
		if(address.equals("-")){			// "-" (削除）なら確認メールはださない
			stw.set_email(address);
			stw.update();
			htb.put("_msg","☆e-maiアドレスを削除しました");
			return	true;
		}
		stw.set_active_i(address);	// メールアドレスを記憶しておく
		stw.set_number_i(ci);		// 確認番号を設定
		stw.update();
		htb.put("_msg","☆ <span class='blue'>" + address+ "</span> を仮登録しました．引き続き確認番号入力の操作を行なってください．");

		//
		// メール送信
		/*
		 * あれば送信元は各グループの管理者とする
		 */
		String from		= para.getSysadmin();// システム管理者
		
		String		shozokuAdminMail	=	db.getShozokuAdminMail(szDB);
		if(!Gear.isEmpty(shozokuAdminMail)){
			from		=	shozokuAdminMail;
		}

		String to		= address;
		String title	= "[メールアドレス登録確認] メールアドレスが仮登録されました";
		//
		htb.put("_conf_i",ci);							// 登録確認番号
		htb.put("_to"		,to);
		htb.put("_from"		,from);
		htb.put("_toName"	,kname);
		String path	= para.i_confRegPath();
		String body	= editMsg(path,htb,para);
		//
		send_To_email( to, from, title, body, para);
		if(LOG.fa) LOG.println("class stwork #regEmail() : 仮登録成功");
		return true;
		//
	}
	//
	// ケータイ 仮登録の処理
	//
	boolean regKeitai(PrintWriter out,Hashtable htb,Param para){
		if(LOG.fa) LOG.println("■ StRegistMail #regKeitai() : ケータイ 仮登録の処理 の先頭です");
		//
		//
		String address	= strHash(htb,"_stNewKeitai");
		
		if(!check_Address_keitai(address,htb)){
			if(LOG.fa) LOG.println("class stwork #regKeitai() : 不正なアドレス");
			return false;
		}
		String stNumber		= (String)htb.get(StUID);
		String stName		= (String)htb.get(StNAME);
		String szDB			= (String)htb.get(GROUP);			// shozoku = データベース名
		//
		// ＤＢに確認番号設定
		Random rand 		=	new Random();
		int		max 		=	999;
    	String 	ck			=	get000type(rand.nextInt(max+1));	// 3桁の乱数
		Student	stw			=	new Student(szDB,stNumber,db);
		String	stKeitai	=	stw.keitai();
		//
		if(address.equals("-")){			// "-" (削除）なら確認メールはださない
			stw.set_keitai(address);
			stw.update();
			htb.put("_msg","☆keitaiアドレスを削除しました");

			return	true;
		}
		stw.set_active_k(address);	// メールアドレスを記憶しておく
		stw.set_number_k(ck);		// 確認番号を設定
		stw.update();
		htb.put("_msg","☆ <span class='blue'>" + address + "</span> を仮登録しました．引き続き確認番号入力の操作を行なってください．");

		//
		// メール送信

		String	from				= para.getSysadmin();
		/*
		 * あれば送信元は各グループの管理者とする
		 *
		String	shozokuAdminMail	= db.getShozokuAdminMail(szDB);
		if(!Gear.isEmpty(shozokuAdminMail)){
			from		=	shozokuAdminMail;
		}
		*/
		String to		= address;
		String title	= "[携帯アドレス登録確認] 携帯アドレスが仮登録されました";
		//
		htb.put("_conf_k",ck);							// 登録確認番号
		htb.put("_to"		,to);
		htb.put("_from"		,from);
		htb.put("_toName"	,stName);
		String path	= para.k_confRegPath();
		String body	= editMsg(path,htb,para);
		//
		send_To_keitai( to, from, title, body, para);
		if(LOG.fa) LOG.println("class stwork #regKeitai() : 仮登録成功");
		return true;
	}
	//
	// e-mail の確認番号処理
	//
	void confEmail(PrintWriter out,Hashtable htb,Param para){
		if(LOG.fa) LOG.println("■ StRegistMail #confEmail() : e-maail の確認番号処理 の先頭です");
		//
		String stNumber	= (String)htb.get(StUID);
		String szDB		= (String)htb.get(GROUP);
		Student stw 	= new Student(szDB,stNumber,db);
		//
		String number_i = stw.number_i();
		if(number_i.equals("-")){
			htb.put("_msg2","☆仮登録が済んでいません．先に「メール仮登録」を行ってください．");
			htb.put("_conf_i","");	// クリアしておく
			return;
		}
		String inp  	= strHash(htb,"_conf_i");
		if(!number_i.equals(inp)){
			htb.put("_msg2","★確認番号が間違っています");
			return;
		}
		String	address	= stw.active_i();
		stw.set_active_i("");				// クリアしておく
		stw.set_number_i("-");
		stw.set_email(address);				// メールアドレス設定
		//
		int	n	= stw.update();
		if(n>0){
			htb.put("_msg2","☆e-mail アドレスが有効になりました");
			htb.put("_conf_i","");	// クリアしておく
			htb.put("_stMail", address);
			return;
		}else{
			if(LOG.fa) LOG.println("class stwork #confEmail() : ★ Detabese を更新できません");
		}
		return;
	}
	//
	// ケータイ の確認番号処理
	//
	void confKeitai(PrintWriter out,Hashtable htb,Param para){
		if(LOG.fa) LOG.println("■ StRegistMail #confKeitai() : ケータイ の確認番号処理 の先頭です");
		//
		String stNumber	= (String)htb.get(StUID);
		String szDB		= (String)htb.get(GROUP);
		Student stw 	= new Student(szDB,stNumber,db);
		//
		String number_k = stw.number_k();
		if(number_k.equals("-")){
			htb.put("_msg2","☆仮登録が済んでいません．先に「メール仮登録」を行ってください．");
			htb.put("_conf_k","");	// クリアしておく
			return;
		}
		String inp  	= strHash(htb,"_conf_k");
		if(!number_k.equals(inp)){
			htb.put("_msg2","★確認番号が間違っています");
			return;
		}
		String	address	= stw.active_k();
		stw.set_active_k("");				// クリアしておく
		stw.set_number_k("-");
		stw.set_keitai(address);			// メールアドレス設定
		//
		int	n	= stw.update();
		if(n>0){
			htb.put("_msg2","☆ 携帯アドレスが有効になりました");
			htb.put("_conf_k","");	// クリアしておく
			htb.put("_stKeitai", address);
			return;
		}else{
			if(LOG.fa) LOG.println("class stwork #confKeitai() : ★ Detabese を更新できません");
		}
		return;
	}

	//
	//
	String editMsg(String path,Hashtable htb,Param para){
		if(LOG.fa) LOG.println("■ StRegistMail #editMsg()");

		// 送信メッセージを編集
        StringWriter 	strOut 	= new StringWriter(20000);
        PrintWriter 	sout    = new PrintWriter( strOut );
		SuperPrint		sp		= new SuperPrint(sout);
        sp.repPrint(path,htb,false);    // メールなので false 
        return strOut.toString(); 		// 編集された受領確認用送信メッセージ
	}
	//
	// メールアドレス入力チェック
	//
	boolean	check_Address_email(String adr,Hashtable htb){
		if(LOG.fa) LOG.println("■ StRegistMail #check_Address_email() : メールアドレス入力チェック の先頭です");
		if(LOG.fa) LOG.println("           adr = " + adr);
		//
		if(isEmpty(adr)){
			htb.put("_msg","e-mail アドレスを入力してください");
			return false;
		}
		if(!isMailaddress(adr)){
			htb.put("_msg","アドレスの形式が正しくありません．再入力してください");
			return false;
		}
		return true;
	}
	//
	// ケータイアドレス入力チェック
	//
	boolean	check_Address_keitai(String adr,Hashtable htb){
		if(LOG.fa) LOG.println("■ StRegistMail #check_Address_keitai() : ケータイアドレス入力チェック の先頭です");
		if(LOG.fa) LOG.println("           adr = " + adr);
		//
		if(isEmpty(adr)){
			htb.put("_msg","ケータイアドレスを入力してください");
			return false;
		}
		//if(!isKeitai(adr)){
		if(!isMailaddress(adr)){
			htb.put("_msg","アドレスの形式が正しくありません．再入力してください");
			return false;
		}
		return true;
	}
	//
	// 携帯チェック
	//
	//  このチェックはしない事にした．
	//
	/* docomo.ne.jp   ezweb.ne.jp   ido.ne.jp  jp-■.ne.jp,■.vodafone.ne.jp  △△.sky.tu-ka.ne.jp  △△.sky.tkc.ne.jp  △△.sky.tkk.ne.jp
	 * pdx.ne.jp  di.pdx.ne.jp  phone.ne.jp mozio.ne.jp  em.nttpnet.ne.jp  mopera.ne.jp 
	 * 
	 */
	boolean isKeitai(String str){
		if(LOG.fa) LOG.println("■ StRegistMail #isKeitai() : 携帯チェック の先頭です");
		if(LOG.fa) LOG.println("           str = " + str);
		//
		if(str==null) return false;
		int p  = str.indexOf("@");
		if(p == -1)	return false;
		//
		String s = (str.substring(p)).trim();
		if(s.equals("@docomo.ne.jp")) 	return true;
		if(s.equals("@ezweb.ne.jp")) 	return true;
		if(s.equals("@pdx.ne.jp")) 	return true;
		//
		if(s.matches("@[a-z].pdx.ne.jp")) return true;		// 2004.11.07 PHS
		if(s.matches("@[a-z].vodafone.ne.jp")) return true;	// 2003.11.14 jphone ⇒vodafone への変更に伴う
		if(s.matches("@jp-[a-z].ne.jp")) return true;	    // 動作はテスト済み　2003.5.17
		//
		if(s.equals("-")) 	return true;	// 削除の指示
		//
		return false;
	}
	//
	// メールアドレスとして正しい形式か
	//
	@Override
	public boolean isMailaddress(String s){
		if(LOG.fa) LOG.println("■ StRegistMail #isMailaddress() : メールアドレスとして正しい形式か の先頭です");
		if(LOG.fa) LOG.println("           s = " + s);
		//
		// ヌル文字でない
		if(s == null) 			return	false;
		if(s.equals("-")) 		return true;	// 削除の指示なのでＯＫ
		//
		//
		// ３文字以上　x@y の文字列である
		int len = s.length();
		if( len < 3)			return	false;
		//
		// @ が含まれいている
		int pos = s.indexOf("@");
		if(pos == -1) 			return false;
		// @ の前後に文字がある
		if(s.endsWith("@"))		return false;
		if(s.startsWith("@"))	return false;
		//
		return true;
	}
	//
	//  一通のメールを送信する
	//
	void send_To_email(String to,String from,String title,String body,Param para){
		if(LOG.fa) LOG.println("■ StRegistMail #send_To_email()");

		//
		String host	= para.getMailhost();
		if(LOG.fa){
			LOG.println("■ StRegistMail #send_To_email() :  一通のメールを送信する の先頭です");
			LOG.println("      host   = " + host);
			LOG.println("       to    = " + to);
			LOG.println("       from  = " + from);
			LOG.println("       title = " + title);
			LOG.println("       body  = " + body );
		}
		//
		/*
		String sysUser	= "";
		String sysPass	= "";
		String pbs = para.popBeforSmtp();
	
		if(pbs.equals("yes")){
			sysUser	= para.getMailmaster();
			sysPass = para.getMailmasterPass();
			boolean ret = jmAuthenticate.authenticate(host,sysUser,sysPass);
		}
		*/
		send(host,to,from,title,body);
	}
	//一通の携帯メールを送信する
	void send_To_keitai(String to,String from,String title,String body,Param para){
		if(LOG.fa) LOG.println("■ StRegistMail #send_To_keitai()");

		//
		String host	= para.getMailhostToKeitai();
		if(LOG.fa){
			LOG.println("■ StRegistMail #send_To_keitai() :  一通の携帯メールを送信する の先頭です");
			LOG.println("      host   = " + host);
			LOG.println("       to    = " + to);
			LOG.println("       from  = " + from);
			LOG.println("       title = " + title);
			LOG.println("       body  = " + body );
		}
		/*
		String sysUser	= "";
		String sysPass	= "";
		String pbs = para.popBeforSmtp_k();
		
		if(pbs.equals("yes")){
			sysUser	= para.getMailmaster_k();
			sysPass = para.getMailmasterPass_k();
			boolean ret = jmAuthenticate.authenticate(host,sysUser,sysPass);
		}
		*/
		send(host,to,from,title,body);
	}
	//
	//  メッセージ送信
	//
	void send(String host,String to,String from, String title,String msg){
        if(LOG.fa) {
        	LOG.println("■ StRegistMail #send() : メッセージ送信 の先頭です");
        	LOG.println("para.getMailmaster() :"+para.getMailmaster());
        	LOG.println("para.getMailmasterPass() :"+para.getMailmasterPass());
        	LOG.println("para.sysMailhost() :"+para.sysMailhost());
        	LOG.println("para.sysMailPort() :"+para.sysMailPort());
        	LOG.println("to :"+to);
        	LOG.println("from :"+from);
        	LOG.println("title:"+title);
        	LOG.println("msg :\n"+msg);

        }
        
		
  		boolean result = Mail.send(
					para.getMailmaster(),
					para.getMailmasterPass(),
					para.sysMailhost(), 
					para.sysMailPort(),
					to, 
					from, 
					title, 
					msg);
			
  		if(LOG.fa) LOG.println("★★ メール送信：" + result);
	}


/////////////////////////////////////////////////////////////////////////////////////////////////////
//
//     出　　力　　処　　理
//
/////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // 画面を表示する
  //
  @Override
public  void    display(boolean edit){
	if(LOG.fa)  LOG.println("■ StRegistMail #display() : メールアドレス仮登録画面の表示 の先頭です");
	//
	if(!edit){
		htb.put("_msg" ,"");
		htb.put("_msg2","");
	}

	Student st 	 		= new Student(szDB,stNumber,db);
	//
	String stMail		= st.email();
	String stKeitai 	= st.keitai();
	if(stMail.equals("-"))   stMail   = "";	// 登録削除は空白に
	if(stKeitai.equals("-")) stKeitai = "";
	//
	htb.put(StMAIL		,stMail);
	htb.put(StKEITAI	,stKeitai);
	
	htb.put("_conf_i","");
	htb.put("_conf_k","");
	
	if(LOG.fa) LOG.outHash(htb,"□□□□ StRegistMail #display()");
	
	/* strHash(htb,DISPFILE)にはファイルの完全パス名が入っている */
	Vector	v	=	loadHtml(getParameter(DISPFILE));
	printVector(v,htb);
  }
}

