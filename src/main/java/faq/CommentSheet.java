
package faq;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import database.Database;
import database.DbConnectionBroker;
import database.KeyGen;
import framework.LOG;
import framework.Param;
import framework.SuperPlayer;
import mailutil_service.DataSet;
import mailutil_service.JmClient;
import mailutil_service.Recipient;
import student.Student;
import tktools.Gear;
import tktools.jmAuthenticate;
import tktools.jmSender;

/**
 * 
 *
 	#
	# ##################
	#   CommentSheet
	# ##################
	#
	<program $faq.CommentSheet>
		<dispatch  html=CommentSheet.html  number=6110  class=faq.CommentSheet />
		<variable>
		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA HOMEURL DIVISION title lec_key aplec_key />
		  <accept    CMD  w_k  w_te_aplec_key  w_seq_no  _w_lec_key  w_st_email  w_mail_title w_st_keitai w_stName w_faq_flag_save />
		  <keep      />
		  
		  <form      />
		</variable>
	</program> 
 *
 *
 * WEBでのアクセプト記述と実際のデータ例示
 *
 * 'SET' ,'14'      ,'mwb9q128-114'     ,'000617'     ,'007'         ,'第3回課題について' ,'OFF'
 * 'SET   %_w_k%'   ,'%_w_te_aplec_key%','%_w_seq_no%','%_w_lec_key%','%_mail_title%'     ,'%_w_faq_flag_save%'
 *
 * 'DELETE','%_w_te_aplec_key%' ,'%_w_seq_no%' ,'%_w_lec_key%'
 * 'DELETE','mwb9q128-114'      ,'000617'      ,'007'
 *
 * 'REPLY','14'     ,'mwb9q128-114'     ,'000617'     ,'007'         ,'cb021259@cis.fukuoka-u.ac.jp' ,'-'             ,'第3回課題について' ,'水上　貴雄')
 * 'REPLY','%_w_k%' ,'%_w_te_aplec_key%','%_w_seq_no%','%_w_lec_key%','%_w_st_email%'                ,'%_w_st_keitai%','%_mail_title%'     ,'%_stName%'
 *
 *
 *
 * 変数の説明
 *
 * 1. receive 
 * 
 * 2. accept
 *      REPLAY（返信）
 * 			w_k	------------------- 表示順の番号
 * 			w_te_aplec_key  ------- te_aplec_key　キー
 * 			w_seq_no  ------------- FAQでの一連番号
 * 			w_lec_key ------------- lec キー
 *  		w_st_email	----------- 学生のメールアドレス
 * 			w_st_keitai	----------- 学生の携帯メールアドレス
 * 			mail_title	----------- CommentSheetのタイトル
 * 			stName	--------------- 学生氏名
 *
 * 		SET（FAQ登録、未読化）
 * 			w_k	------------------- 表示順の番号
 * 			w_te_aplec_key  ------- te_aplec_key　キー
 * 			w_seq_no  ------------- FAQでの一連番号
 * 			w_lec_key ------------- lec キー
 * 			mail_title  ----------- CommentSheetのタイトル
 * 			w_faq_flag_save ------- FAQに登録したかどうかを示す値
 * 
 *      DELEE（削除）
 *  		w_te_aplec_key  ------- te_aplec_key　キー
 * 			w_seq_no  ------------- FAQでの一連番号
 * 			w_lec_key ------------- lec キー
 *
 * 3. keep
 * 4. form
 *
 *  
 *
 */
public class CommentSheet extends SuperPlayer {


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
	Database	db;
	//
	Gear		gear;
	//
	String 		method;
	//
	public	CommentSheet(){
		super();
		if(LOG.fa) LOG.println("■ CommentSheet #コンストラクタ");
	}
	
	@Override
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		broker	=	getDbConnection();
		db		=	new Database(broker);	
		//
		method	= "class CommentSheet #launch() : ";	// コメント用

	}
	@Override
	public	String	dispatch(){
		if(LOG.fa) LOG.outHash(htb,"■Sample #dispatch()");
		if(LOG.fa) LOG.println("■Sample #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		//
		// ◎FAQを見る
		if(cmd.equals("QA")){
			if(LOG.fa) LOG.println(method + "[QA] FAQ を見る");
			disp_mode	=	DISP_NEW;
			ret			=	"$faq.FaqTeacher";
			
		// 表示期間の変更
		}else if(cmd.equals("TERM")){
			if(LOG.fa) LOG.println(method + "[TERM] 表示期間の変更");
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
			
		// ◎ソート変更
		}else if(cmd.equals("SORT")){
			if(LOG.fa) LOG.println(method + "[SORT] ソート変更");
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
			
		// 表示データの選別変更
		}else if(cmd.equals("FILTER")){
			if(LOG.fa) LOG.println(method + "[FILTER] 表示データの選別変更" );
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
			
		// ◎返信を出す
		}else if(cmd.equals("REPLY")){
			if(LOG.fa) LOG.println(method + "[REPLY] 返信を出す" );
			//
			reply(htb,para);
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
			
		// ◎削除する
		}else if(cmd.equals("DELETE")){
			if(LOG.fa) LOG.println(method + "[DELETE] 削除する" );
			//
			deleteMail(htb);
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
			
		// ◎FAQ登録＋未読化 の実行
		}else if(cmd.equals("SET")){
			if(LOG.fa) LOG.println(method + "[SET] FAQ登録＋未読化 の実行" );
			//
			// FAQ 登録のとき writeMail(htb,para);
			do_set(htb);
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
			
			
		// ◎講義画面に戻る
		}else if(cmd.equals("RETURN")){
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;
			
		}else if(cmd.equals("EXECUTE")){
			/*
			 *　プログラムの実行
			 */
			disp_mode	=	DISP_NEW;
			ret			=	getParameter(PROGRAM);

		// エラー
		}else{
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
		}
		/*
		 * 次に表示するときあるいはリターンしてきた時の表示モードをセットしてからリターンする
		 */
		putParameter(DISP_KEY,disp_mode);
		return	ret;
	}
	//
//	 FAQ登録＋未読化 の実行
	//
	void	do_set(Hashtable htb){
		if(LOG.fa) LOG.outHash(htb,"class CommentSheet #do_set() :  FAQ登録＋未読化 の実行 の先頭です");
		//
		String	teUid			= strHash(htb,"_teUid");
		String	te_aplec_key	= strHash(htb,"_w_te_aplec_key");
		String	seq_no			= strHash(htb,"_w_seq_no");
		String	lec_key			= strHash(htb,"_w_lec_key");
		String	ml_title		= strHash(htb,"_w_mail_title");		// メールタイトル
		String  faq_flag		= strHash(htb,"_w_faq_flag_save");	// 表示の時に保存していたＦＡＱフラグの状態
		//
		String	recNum			= strHash(htb,"_w_k");
		//
		String	chgRead			= strHash(htb,"_toNotRead_" + recNum);
		String	toFaq			= strHash(htb,"_toFAQ_" 	+ recNum);
		if(LOG.fa) LOG.println("      te_aplec_key = " + te_aplec_key);
		if(LOG.fa) LOG.println("            seq_no = " + seq_no);
		if(LOG.fa) LOG.println("            recNum = " + recNum);
		//
		// 未読化の処理
		if( ( chgRead!=null)&&(chgRead.equals("ON")) ){
			FAQmail	fm				= new FAQmail(db);
			fm.add_keys(te_aplec_key,seq_no);
			fm.update_FAQmail_readFlag("OFF");
		}
		//ＦＡＱ登録の処理
		if( ( toFaq!=null)&&(toFaq.equals("ON")) ){
			//
			String	faqTitle	= strHash(htb,"_faq_title_"  + recNum);
			String	body		= strHash(htb,"_reply_mail_" + recNum);
			//
			if( isEmptyData(faqTitle) ){
				faqTitle	= getfaqTitle(ml_title);	// null の可能性もある
			}
			if( (!isEmptyData(faqTitle))&&(!isEmptyData(body)) ){	// 件名も本文も空でなければ
				boolean ret = addFaq(teUid,lec_key,seq_no,faqTitle,body,faq_flag);	// FAQ に登録する
				if(ret)	{											// うまく登録できればメールレコードを書き戻し、フラグも立てる
					write_setall(te_aplec_key,seq_no,faqTitle,body);
				}
			}
		}
	}
	//
//	 ＦＡＱ登録の後処理としてのメールレコード更新
	//
	void write_setall(String te_aplec_key,String seq_no,String faqTitle,String body){
		if(LOG.fa) LOG.println("class CommentSheet #write_reply() : ＦＡＱ登録の後処理としてメールを更新保存する の先頭です");
		//
		// 本文を書き換え更新する
		FAQmail fm	= new FAQmail(db);
		fm.add_keys(te_aplec_key,seq_no);							// 読み出しのためにキーをレコードにＡＤＤする
		fm.update_FAQmail_faq_set("ON",faqTitle,body);				// 本文のみ書き換え
		//
	}
	//
//	  FAQ に登録する
	//
	boolean addFaq(String teUid,String lec_key,String seq_no,String faqTitle,String body,String faq_flag){
		if(LOG.fa) LOG.println("class CommentSheet #addFaq() :  FAQ に登録する の先頭です");
		if(LOG.fa) LOG.println("       lec_key  =" + lec_key);
		if(LOG.fa) LOG.println("       teUid    =" + teUid);
		if(LOG.fa) LOG.println("       seq_no   =" + seq_no);
		if(LOG.fa) LOG.println("       faqTitle =" + faqTitle);
		if(LOG.fa) LOG.println("       body     =" + body);
		if(LOG.fa) LOG.println("       faq_flag =" + faq_flag);
		//
		String	te_lec_key	= KeyGen.get_te_lec_key2(teUid,lec_key);
		FAQ	faq	= new FAQ(db);
		faq.add_keys(te_lec_key,seq_no);
		faq.add_data(faqTitle,body);
		int n =0;
		if(faq_flag.equals("ON")){	// 既に登録されていたなら更新
			n	= faq.update_FAQ();
		}else{
		 	n	= faq.insert_FAQ();
		}
		if(n>0) return	true;
		return false;
	}

	String  getfaqTitle(String ml_title){
		if( isEmptyData(ml_title) ){
			return	null;
		}
		return ml_title;
	}
	//
//	 返信メールを出す
	//
	void reply(Hashtable htb,Param para){
		if(LOG.fa) LOG.println("class CommentSheet #reply() : 返信メールを出す の先頭です");
		//
		String te_aplec_key	= strHash(htb,"_w_te_aplec_key");
		String seq_no		= strHash(htb,"_w_seq_no");
		String recNum		= strHash(htb,"_w_k");
		//
		String from			= strHash(htb,"_teMail");
		String st_mail		= strHash(htb,"_w_st_email");
		String st_keitai	= strHash(htb,"_w_st_keitai");
		String st_name		= strHash(htb,"_w_stName");
		
		String m_title		= "［返信］:" + strHash(htb,"_w_mail_title");
		String m_body_temp	= strHash(htb,"_reply_mail_" + recNum);
		String m_body		= mkReplyBody(m_body_temp,htb);
		//
		//
		
		
		// email 送信
		String emailFlag 	= strHash(htb,"_w_email_" + recNum);
		if( (!isEmpty(emailFlag))&&(emailFlag.equals("ON")) ){
			if(LOG.fa) LOG.println("class CommentSheet #reply() : e-mail フラグは ON です");
			if(!st_mail.equals(Student.EMPTY)){
				
				//send_To_email(st_mail,from,m_title,m_body,para);
				sendMailService(from, m_title, m_body, "TO", createToList(st_mail, st_name));
				
				
			}
		}else{
			if(LOG.fa) LOG.println("class CommentSheet #reply() : e-mail フラグは OFF です");
		}
		// 携帯送信
		String keutaiFlag 	= strHash(htb,"_w_keitai_" + recNum);
		if( (!isEmpty(keutaiFlag))&&(keutaiFlag.equals("ON")) ){
			if(LOG.fa) LOG.println("class CommentSheet #reply() : 携帯 フラグは ONです");
			if(!st_keitai.equals(Student.EMPTY)){
				
				//send_To_keitai(st_keitai,from,m_title,m_body,para);
				sendMailService(from, m_title, m_body, "TO", createToList(st_keitai, st_name));
				
				
			}
		}else{
			if(LOG.fa) LOG.println("class CommentSheet #reply() : 携帯 フラグは OFF です");
		}
		
		
		// 本文とフラグを書き換え更新する
		if(LOG.fa) LOG.println("class CommentSheet #reply() : ＤＢの本文を書き換え、readFlag を ON にします");
		FAQmail fm	= new FAQmail(db);
		fm.add_keys(te_aplec_key,seq_no);
		fm.update_FAQmail_body(m_body_temp);
		//
	}
	/////////// 2021,2 送信方法をサービスを利用する方法に変更　/////////////////////////////////////////////////////////
	
	// 1件だけ（学生宛）の宛先リストを作成
	public List<Recipient> createToList(String to, 		// 宛先メール
										 String to_name		// 宛先氏名
		){
		String st_id = (to.split("@"))[0];	// 学籍番号を取り出す
		List<Recipient> ls = new ArrayList<>();
		ls.add(new Recipient(true, 			// trueは送信対象であることを示す
							  st_id, 		// 宛先のID
							  to_name, 		// 宛先氏名
							  to, 			// 宛先メール
							  "本文ダミー",
							  "様"));		// 宛先に付加する敬称
		return ls;
		
	}
	// 送信データを作成して、送信サービスを呼び出す
	public void sendMailService(String sender, 		// 送信元メール
								 String title, 			// メール表題
								 String body, 			// 本文
								 String flag, 			// "TO"、"CC"、"BCC"
								 List<Recipient> list	// 宛先リスト
		) {
		DataSet ds = new DataSet(sender,	// 送信元メール
								 title, 	// メール表題
								 "TO", 		// １件の通常送信
								 body, 		// 本文
								 list		// 宛先リスト
		);
		JmClient client = new JmClient();
		client.send(ds);
		//client.close();
	}
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
	
	
	
	
	//
//  ＦＡＱのボディ部分を二つに分け返信文を作成する
	//
	String mkReplyBody(String body ,Hashtable htb){
		if(LOG.fa) LOG.println("class stwork #setBody() :  ＦＡＱのボディ部分を二つに分けて HTB にセットする の先頭です");
		//
		int MAX = 20;
		StringBuffer	bf1		= new StringBuffer(1024 * 30);
		StringBuffer	bf2		= new StringBuffer(1024 * 30);
		
	   try{
	       BufferedReader r = new BufferedReader(new StringReader(body));
	       Pattern	  pattern	= Pattern.compile("^----------"); // 行の先頭が１０個の　'-'
			String dt;
			while((dt=r.readLine())!=null){
				//
				Matcher matcher 	= pattern.matcher(dt);
	       	if(matcher.find()) { break; }
				//
				bf1.append(">" + dt + "\n");
	       }
			//
			bf2.append( mkMessageTop(htb));
			while((dt=r.readLine())!=null){	// "-----" は読み捨て
				bf2.append(dt + "\n");
			}
	   }catch (IOException ee){
	       
	   }
		//
		return	bf2.toString()	 + "\n" + bf1.toString();
		//
	}

	String mkMessageTop(Hashtable htb){
		//
		String teUid	= strHash(htb,"_teUid");
		String title	= strHash(htb,"_title");
		String stName	= strHash(htb,"_w_stName");
		//
		db.MembersInfo(teUid,htb);
		String	teName	= strHash(htb,"_user_name");
		//
		String top1 = "こんにちは、" + stName + " さん。\n" + title + " 担当の" + teName + " です.\n";
		String top2 = "あなたからのメールについて返信します.\n\n";
		return	top1 + top2;
	}
//
// コメントシートをを削除する
//  (FAQ は消さない)
//
	void deleteMail(Hashtable htb){
		if(LOG.fa) LOG.println("class CommentSheet #deleteMail() : Ｑ＆Ａメールを削除する の先頭です");
		//
		String te_aplec_key	= strHash(htb,"_w_te_aplec_key");
		String seq_no		= strHash(htb,"_w_seq_no");
		String recNum		= strHash(htb,"_w_k");
		//
		FAQmail	fm	= new FAQmail(db);
		fm.add_keys(te_aplec_key,seq_no);
		fm.delete_FAQmail();
		//
	}

	
	//
// htb に表示のための初期データを詰め込む
	void doInit(){
		if(LOG.fa) LOG.outHash(htb,"class CommentSheet #init_QandA() : htb に表示のための初期データを詰め込む の先頭です");
		//
		GregorianCalendar today = currentDay();
		int	ey = today.get(Calendar.YEAR);
		int em = today.get(Calendar.MONTH);
		htb.put("_e_yyyy", String.valueOf( ey ) );
	   htb.put("_e_month", String.valueOf( em + 1 ) );	// 今月
		//
		today.add(Calendar.MONTH ,-1);
		int	sy = today.get(Calendar.YEAR);
		int sm = today.get(Calendar.MONTH);
		htb.put("_s_yyyy", String.valueOf( sy ) );
	   htb.put("_s_month", String.valueOf( sm + 1 ) );	// 先月
		//
		htb.put("_mail_dispMode","1"); 	// 未読のみ
		htb.put("_sortMode","OFF");		// 受け付け日付順
		
	}

	//
//  一通のメールを送信する
//

	void send_To_email(String to,String from,String title,String body,Param para){
		//
		String host	= para.getMailhost();
		if(LOG.fa){
			LOG.println("class exwork #send_To_email() :  一通のメールを送信する の先頭です");
			LOG.println("      host   = " + host);
			LOG.println("       to    = " + to);
			LOG.println("       from  = " + from);
			LOG.println("       title = " + title);
			LOG.println("       body  = " + body );
		}
		//
		String sysUser	= "";
		String sysPass	= "";
		String pbs = para.popBeforSmtp();
		if(pbs.equals("yes")){
			sysUser	= para.getMailmaster();
			sysPass = para.getMailmasterPass();
			boolean ret = jmAuthenticate.authenticate(host,sysUser,sysPass);
			if(LOG.fa) LOG.println("class exwork #send_To_email(): jmAuthenticate is " + ret);
			if(LOG.fa) LOG.println("      host is " + host);
			if(LOG.fa) LOG.println("      user is " + sysUser);
			if(LOG.fa) LOG.println("      Pass is " + sysPass);
		}
		send(host,to,from,title,body);
	}
//	一通の携帯メールを送信する
	void send_To_keitai(String to,String from,String title,String body,Param para){
		//
		String host	= para.getMailhostToKeitai();
		if(LOG.fa){
			LOG.println("class exwork #send_To_keitai() :  一通の携帯メールを送信する の先頭です");
			LOG.println("      host   = " + host);
			LOG.println("       to    = " + to);
			LOG.println("       from  = " + from);
			LOG.println("       title = " + title);
			LOG.println("       body  = " + body );
		}
		//
		String sysUser	= "";
		String sysPass	= "";
		String pbs = para.popBeforSmtp_k();
		if(pbs.equals("yes")){
			sysUser	= para.getMailmaster_k();
			sysPass = para.getMailmasterPass_k();
			boolean ret = jmAuthenticate.authenticate(host,sysUser,sysPass);
			if(LOG.fa) LOG.println("class exwork #send_To_email(): jmAuthenticate is " + ret);
			if(LOG.fa) LOG.println("      host is " + host);
			if(LOG.fa) LOG.println("      user is " + sysUser);
			if(LOG.fa) LOG.println("      Pass is " + sysPass);
		}
		send(host,to,from,title,body);
	}
	//
//	  メッセージ送信
	//
	void send(String host,String to,String from, String title,String msg){
		if(LOG.fa)  LOG.println("class stwork #send() : メッセージ送信 の先頭です");
		try{
	       jmSender.send(host,to,from,title,msg);
	   }catch(AddressException e2){
	   }catch(MessagingException e1){
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
	@Override
	public  void	display(boolean editmode){
		if(LOG.fa)	LOG.println("■ Sample #display(boolean editmode)");
		
		if(!editmode){
			putParameter(MESSAGE,"");
			doInit();	// htb に初期化データを埋め込む
		}
		
		// 表示選択状態
		String mail_dispMode	= strHash(htb,"_mail_dispMode");
		if(mail_dispMode.equals("1")){
			htb.put("_mail_notread","selected");
			htb.put("_mail_tofaq",	"");
			htb.put("_mail_all",	"");
		}else if(mail_dispMode.equals("2")){
			htb.put("_mail_notread","");
			htb.put("_mail_tofaq",	"selected");
			htb.put("_mail_all",	"");
		}else{
			htb.put("_mail_notread","");
			htb.put("_mail_tofaq",	"");
			htb.put("_mail_all",	"selected");
		}
		//
		// 全メールを先読みする
		FAQmailAll allMail = new FAQmailAll(htb,db); // 学籍番号順でソートされる
		int total		= allMail.size();			// 全件数
		int newMails	= allMail.notReads();		// 未読件数
		htb.put("_kei",String.valueOf(total));
		htb.put("_new",String.valueOf(newMails));
		//
		//
		// ソートモード
		String sortMode	= strHash(htb,"_sortMode");
	   if(sortMode.equals("OFF")){
			htb.put("_no_dateTime","");	
			htb.put("_yes_dateTime","selected");// 受け付け順
		}else{
			htb.put("_no_dateTime",	"selected");// 学籍番号順
			htb.put("_yes_dateTime","");
			allMail.stNumberSortr(); // 学籍番号でソートする
		}
		//
		htb.put("_allMail",allMail);	// FAQmailAll をhtb に入れ表示処理で使わせる
		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}
	/**
	 * 
	 */
	@Override
	public void	write(String key){
		//
	   // 開始年
		if(key.equals("set_syy2")){
	       String x = strHash(htb,"_s_yyyy");
			if(LOG.fa)  {if(x==null)  LOG.debugStop(out,"_s_yyyy_ が null です:  CommentSheet class #write()"); }
			int yyN = Integer.parseInt(x); // 指定された開始年
	       setOptionNumber(out,yyN,yyN-5,yyN,1);
	   // 開始月
	   }else if(key.equals("set_smm2")){
	       String x = strHash(htb,"_s_month");
			if(LOG.fa)  {if(x==null)  LOG.debugStop(out,"_s_month_ が null です:  CommentSheet class #write()"); }
	       int mmN = Integer.parseInt(x);// 指定された開始月
	       setOptionNumber(out,mmN,1,12);
	   //終了年
	   }else if(key.equals("set_eyy2")){
	       String x = strHash(htb,"_e_yyyy");
			if(LOG.fa)  {if(x==null)  LOG.debugStop(out,"_e_yyyy_ が null です:  CommentSheet class #write()"); }
	       int yyN = Integer.parseInt(x);// 指定された終了年
	       setOptionNumber(out,yyN,yyN-5,yyN,1);
	   // 終了月
	   }else if(key.equals("set_emm2")){
	       String x = strHash(htb,"_e_month");
			if(LOG.fa)  {if(x==null)  LOG.debugStop(out,"_e_month_ が null です:  CommentSheet class #write()"); }
	       int mmN = Integer.parseInt(x);// 指定された終了月
	       setOptionNumber(out,mmN,1,12);
		}
	}
	//
//	 HTML中に整数の option 指示子を繰り返し生成する
	//
	public void setOptionNumber(PrintWriter out,int selectedNumber,int startNumber,int endNumber){
	   setOptionNumber(out,selectedNumber,startNumber,endNumber,1);
	}
	public void setOptionNumber(PrintWriter out,int selectedNumber,int startNumber,int endNumber,int step){
	   for(int k=startNumber; k<=endNumber; k+=step){
	       String value = null;
	       if(k >= 10) {
	           value = String.valueOf(k);
	       }else{
	           value = "0" + String.valueOf(k);
	       }
	       String sel="\">";
	       if(k==selectedNumber){sel = "\"  selected>";}
	       String opt = "   <option value=\"" + value + sel + String.valueOf(k) + "</option>";
	       out.println(opt);
	   }
	}
	/**
	 * 
	 */
	@Override
	public void	write(String key,Vector exHtml){
		if(key.equals("Comment")){
			Comment(exHtml);
			
		}
	}
	/**
	 * 
	 * @param key
	 * @param exHtml
	 * @param htb
	 */
	public void Comment(Vector exHtml){
		
		FAQmailAll	allMail	= (FAQmailAll)htb.get("_allMail");
		int	n	= allMail.size();
		putParameter("_w_all_mails",String.valueOf(n));	// 表示した個数を覚えておく
		/*
		 * 表示期間を設定( FAQmailAll#dateCheck() で検査できる)
		 */
		String 	syy		=	strHash(htb,"_s_yyyy");
		String 	smm		=	strHash(htb,"_s_month");
		String 	eyy		=	strHash(htb,"_e_yyyy");
		String 	emm		=	strHash(htb,"_e_month");
		allMail.regTerm( syy, smm, eyy, emm);
		/*
		 * 
		 */
		for(int i=0; i<n; i++){
			// 表示するか否かチェック
			boolean ck1	= allMail.dateCheck(i);
			//if(DBG._tr090)	DBG.println("★class CommentSheet #write(): 期間チェック = " + ck1 );
			if( !ck1 ){	// 対象期間内でなければパス
				continue;
			}
			FAQmail	fm	= allMail.getSorted(i);
			String mode	= strHash(htb,"_mail_dispMode");
			boolean ck2	= okDisp(mode,fm);
			//if(DBG._tr090)	DBG.println("★class CommentSheet #write(): 対象チェック = " + ck2 );
			if( !ck2 ){		// 表示対象（ 未読｜FAQ｜ALL ）でなければパス
				continue;
			}
			//
			String	te_aplec_key= fm.te_aplec_key();
			String	seq_no		= fm.seq_no();
			//
			String	rvdate		= fm.rvdate();
			String	lec_key		= fm.lec_key();
			String	stNumber	= fm.stNumber();
			String	ml_title	= fm.ml_title();
			String	ml_body		= fm.ml_body();
			String	read_flag	= fm.read_flag();
			String	faq_title	= fm.faq_title();
			String	faq_flag	= fm.faq_flag();
			//
			if(read_flag.equals("ON")){
				htb.put("_reply.gif","reply.gif");
				htb.put("_sz"		,"12");
			}else{
				htb.put("_reply.gif","spacer.gif");
				htb.put("_sz"		,"1");
			}
			if(faq_flag.equals("ON")){
				htb.put("_w_faq_flag_save","ON");	// faq 登録時に insert か update か判断するのに必要
				//
				htb.put("_faq.gif"	,"faq.gif");
				htb.put("_sz2"		,"12");
			}else{
				htb.put("_w_faq_flag_save","OFF");
				//
				htb.put("_faq.gif","spacer.gif");
				htb.put("_sz2"		,"1");
			}
			//
			htb.put("_w_k",String.valueOf(i));
			//
			htb.put("_w_te_aplec_key",te_aplec_key);
			htb.put("_w_seq_no"		,seq_no);
			htb.put("_w_lec_key"	,lec_key);	// 注意 te_lec ではなく lec である
			//
			htb.put("_mail_title"	,ml_title);
			htb.put("_reply_mail"	,ml_body);
			//
			Hashtable wh	= new Hashtable(30);
			getStudentInfo(wh,stNumber,htb);
			//
			htb.put("_stName"		,strHash(wh,"_kname"));		// DB を引く
			htb.put("_w_st_email"	,strHash(wh,"_email"));		// DB を引く
			htb.put("_w_st_keitai"	,strHash(wh,"_keitai"));	// DB を引く
			htb.put("_stNumber"		,stNumber);
			htb.put("_receive"		,rvdate);
			//
			if( faq_flag.equals("ON") ){
				//
				// lec_key と seq_no で FAQ テーブル を引いてfaqタイトルを得るが、FAQ テーブルで
				// このレコードが削除されている場合がある．その場合 "DELETED" が返ってくるので
				// FAQmail テーブルでも　FAQ 登録済みのフラグを消しておかねばならない．
				// 
				String	title	= faqTitle(faq_flag,lec_key,seq_no,htb); 	// DB を引く
				if(title.equals("DELETED")){
					fm.setFaqFLAG("OFF");
					title	= "";
					// 上のFAQ フラグがOFF だった場合の処理を行っておく
					htb.put("_w_faq_flag_save","OFF");
					htb.put("_faq.gif","spacer.gif");
					htb.put("_sz2"		,"1");
					//
				}
				htb.put("_faq_title" ,title);
			}else{
				htb.put("_faq_title" ,"");
			}
			//
			//if(DBG._tr090) DBG.outHash(htb,"class CommentSheet #write() :  出口におけるハッシュの内容です");
			printVector(exHtml);
		}
	}
	//
//	 CommentSheetで表示対象かどうか
	//
	boolean	okDisp(String mode,FAQmail fm){
		if(LOG.fa) LOG.println("class  CommentSheet #okDisp() : CommentSheetで表示対象かどうか の先頭です");
		//
		if(mode.equals("1")){		// 未読のみ
			return	fm.isNotRead();	// 未読のとき　true
			
		}else if(mode.equals("2")){	// ＦＡＱ登録済みのみ
			return	fm.isOnFaq();	// 登録済みのとき true
			
		}
		// 全部
		return	true;
	}
	//
//	  学生情報をハッシュに返す
	//
	void getStudentInfo(Hashtable wh,String stNumber,Hashtable htb){
		if(LOG.fa) LOG.println("class CommentSheet #getStudentInfo():  学生情報をハッシュに返す　の先頭です");
		//
		String	szDB	= strHash(htb,"_szDB");
		Student st		= new Student(szDB,stNumber,db);
		st.setToHash(wh);
		return;
	}
	//
//	  FAQに掲載されてないか調べ、あればそのタイトル名を返す.なければ　""を返す．
//	  2004.2 FAQ自体で削除更新ができるので、faq_flag が ON でも FAQ データベースにレコードがない場合がある
//	  その場合は FAQmail データベースの FAQ 掲載フラグをクリアしておく
	//
	String faqTitle(String faq_flag,String lec_key,String seq_no,Hashtable htb){
		if(LOG.fa) LOG.println("class CommentSheet #faqTitle():  FAQに掲載されてないか調べ、あればそのタイトル名を返す　の先頭です");
		if(LOG.fa) LOG.println("    faq_flag  = " + faq_flag);
		if(LOG.fa) LOG.println("    lec_key   = " + lec_key);
		if(LOG.fa) LOG.println("    seq_no    = " + seq_no);
		//
		if(faq_flag.equals("OFF"))	return "";
		//
		String teUid		= strHash(htb,"_teUid");
		String te_lec_key 	= KeyGen.get_te_lec_key2(teUid,lec_key);
		//
		FAQ 	faq = new FAQ(db);
		faq.set_keys(te_lec_key,seq_no);
		int 	n 	= faq.read_FAQ();
		// データベースに該当がなかった時は、"DELETED" を返す (2004.2)
		if(n==0) {
			return	"DELETED";
		}
		return faq.faq_title();
	}
}

