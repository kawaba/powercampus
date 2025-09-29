/*
     Power Campus HybridMail

*/
package hybridmail;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import jakarta.ws.rs.core.Response;
import database.Database;
import database.DbConnectionBroker;
import database.KeyGen;
import epml.tools.DBG;
import framework.LOG;
import framework.Param;
import framework.SuperPlayer;
import kamoku.KamokuApRecord;
import mailutil.Exmail;
import mailutil_service.DataSet;
import mailutil_service.JmClient;
import mailutil_service.Recipient;
import meibo.Meibo;
import student.Student;
import tktools.Csv;

/**
*
*
	#
	# ##################
	#     HybridMail
	# ##################
	#
	<program $hybridmail.HybridMail>
		<dispatch  html=HybridMail.html  number=6020  class=hybridmail.HybridMail />
		<variable>
		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA DIVISION HOMEURL title lec_key aplec_key/>
		  <accept    CMD    UPLODE  />
		  <keep      rv_flag  clear_mode  ckbox  mail_edit_mode mail_mode  mail_flag  sendEmail  sendKmail  max_students  total_view  view />
		  
		  <form      />
		</variable>
	</program> 
*
*
* 変数の説明
*
* 1. receive 
* 2. accept
* 3. keep
* 				下を参照。
* 4. form
*
* 
		HTML に埋め込んだ情報とプログラムの関係
			
	　	１．名簿のチェックボックスの処理に関連するもの
			
	  		(1) <input type="hidden" name="_rv_flag"       value="%_rv_flag%"> 
      		値：ON ---------- リバースモードオン．全てのチェックボックスをオンかオフにする
				OFF --------- リバースモードオフ．WEBで指定されたとおりの表示を維持する
				INIT -------- 初期化する．全てのチェックボックスをオンにする
				
			説明：メール送信画面から差し込み確認画面に移るとき、差込確認画面にはチェックボックスが
			　　　ないので、状態をhidden 変数に覚えている。
				-----------------------------------------------------------------
				%@ckboxList%
				<<
	  					<input type="hidden" name="_rv%_n%""       value="ON">
				>>
				-----------------------------------------------------------------
			　　　これを'ckboxList'で処理して値のあるものを転記する。
			
			(2) <input type="hidden" name="_clear_mode"    value="%_clear_mode%"> 

			　値：　OFF/ON 
			　
			　説明：　_rv_flag がオンのとき、全てクリア(OFF)か全てオンかを決める変数．
				　　　トグルになっている

      		(3) <input type="hidden" name="_ckbox"         value="%_ckbox%">
			
			  値：　0,0,1　のようなCSV
			  
			  説明：全てのチェックボックスの　ON/OFF 状態を 1/0 に直してCSVで保持している．
			　　　　送信除外の判断に使用する．値は paramPrintOPT() の中で、チェックボックスの
			　　　　状態が変更されるときに書き直されるので、チェックボックスのない送信確認画
			　　　　面のなかでは値が古いが、送信画面に戻るときに、正しく書き直される．
			
	  	２．メール送信モードに関連するもの
      		(1) <input type="hidden" name="_mail_mode"     value="%_mail_mode%"> 
      		　
			　値　：　OFF/ON
			　説明：　差し込み送信モードか否か
			　
			(2) <input type="hidden" name="_sendEmail"     value="%_sendEmail%">
			　
			　値　： e-mail を送信するかどうか
			　説明： OFF/ON
			　
      		(3) <input type="hidden" name="_sendKmail"     value="%_sendKmail%">	
			
			　値　：　携帯メールを送信するか否か
			　説明：　OFF/ON
		　	　
				
	　	３．確認画面表示に関連するもの
			
      		(1) <input type="hidden" name="_max_students"	value="%_max_students%">
      		(2) <input type="hidden" name="_total_view"	 	value="%_total_view%">
      		(3) <input type="hidden" name="_view"			value="%_view%">
*/

public class HybridMail extends SuperPlayer {

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
	
	String		teUid;
	String		aplec_key;
	String		method;
	/**
	 * 
	 *
	 */
	public	HybridMail(){
		super();
		if(LOG.fa) LOG.println("■ HybridMail #コンストラクタ");
	}	
	/**
	 * 
	 */
	@Override
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		broker	=	getDbConnection();
		db		=	new Database(broker);	

		teUid		= strHash(htb,"_teUid");
		aplec_key	= strHash(htb,"_aplec_key");
		/*
		 * 送信メール種別をウェブから得る
		 */
		String	emailChk	=	getParameter("sw_email");
		String	kmailChk	=	getParameter("sw_keitai");
		if(!isEmpty(emailChk)){
			htb.put("_sendEmail","ON");
		    
		}else{
			htb.put("_sendEmail","OFF");
		    
		}
		if(!isEmpty(kmailChk)){
			htb.put("_sendKmail","ON"); 
		    
		}else{
			htb.put("_sendKmail","OFF"); 
		    
		}
		/*
		 * display()以下で変数を作成しそれをWebに保存するため、hidden タグの%変数%を
		 * 画面表示後に実際の値で埋めるという指示を SuperPrint()にしておく
		 */
		clearPresetFlag();
		
    }
	/**
	 * 
	 */
	@Override
	public	String	dispatch(){
		if(LOG.fa) LOG.println("★HybridMail#dispatch()："+LocalTime.now());
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		if(cmd.equals("SEND_MAIL")){
			if(LOG.fa) LOG.println("★HybridMail#送信処理："+LocalTime.now());
			//
			checkboxInfo(htb);	// チェックボックスの情報を集める
			/* *********************************************/
			boolean		retcode	= mail_send(out, htb, para);
			/* *********************************************/
			if(retcode){
				htb.put("_rv_flag","INIT");			// リバース、初期化フラグ。ここでは初期状態から始めることを示す
				htb.put("_clear_mode","OFF");		// リバースの方向．最初はクリア
				htb.put("_ckbox","");				// 名簿のチェックボックスの状態．最初なのでなにもない
				//
				htb.put("_mail_mode","OFF");		// 初期状態では差し込みメールをオフにしておく．_mailMode は選択リストボックス
				htb.put("_mailMode","OFF");			// の変数、_mail_mode はそれを記憶しておくための変数である
				//
				htb.put("_mail_edit_mode","NEW");	// 編集モードを新規に
				htb.put("_mailSubject","");			// 送信文タイトルだけ消しておく
				
				
				// 送信終了後、差し込みデータファイルを削除しておく
				String	aplec_key	= strHash(htb,"_aplec_key");
				String	teUid		= strHash(htb,"_teUid");
				String 	path 		= para.getMailTempDir(teUid ,aplec_key );
				deleteFiles(new File(path));
				
			}
			/*
			 *　再表示
			 */
			if(LOG.fa) LOG.println("★HybridMail#dispatch()終了："+LocalTime.now());
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
		
		}else if(cmd.equals("MAIL_MODE")){
			//
			String	mode	= strHash(htb,"_mailMode");		// 差し込みメールの有効／無効を変更する
			htb.put("_mail_mode",mode);						// _mailMode は変更を選択するリストボックス、_mail_mode は値を記憶しておく
			//												// 具体的な処理は paramPrintOPT() で行う
			if(mode.equals("ON")){
				htb.put("_no_sashikomi","");				// リストボックスの表示を制御（ここでやっておかないと間に合わない）
				htb.put("_ok_sashikomi","selected");
			}else{
				htb.put("_no_sashikomi","selected");
				htb.put("_ok_sashikomi","");
			}
			htb.put("_rv_flag","INIT");							// モードが代わるのでチェックボックスの指定を全て初期化する
			htb.put("_mail_edit_mode","ON");					// モードの違いにより送信対象のリストが、名簿なのか差し込みリストなのか
			/*
			 *　再表示
			 */
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;

		}else if(cmd.equals("VIEW")){
			if(LOG.fa) LOG.println(method + "(VIEW) 差込確認画面を表示します");
			//
			String	mode	= strHash(htb,"_mail_mode");		// 差し込みメールの有効／無効
			if(mode.equals("ON")){
				// 差し込みデータはあるか
				if( isExistSashikomi(htb,para) ){
					/*
					 * 差込確認画面を表示する
					 */
					putParameter("ckbox",getCeckboxInfo());
					if(!existMailto(getCeckboxInfo())){
						/*
						 * どの学生にもチェックがないとプレビューはみれない
						 */
						String	msg	=	"★送信先チェックがひとつもチェックされていません";
						putParameter(MESSAGE, msg);
						disp_mode	=	DISP_EDIT;
						ret			=	DISPATCH_DEFAULT;			
						
					}else{
						/*
						 * プレビューへ
						 */
						disp_mode	=	DISP_NEW;
						ret			=	"$hybridmail.MailPreview";				
						
					}
				
				}else{
					htb.put("_msg","★ まず、差し込みファイルを指定してください．");
					htb.put("_mail_edit_mode","ON");					// モードの違いにより送信対象のリストが、名簿なのか差し込みリストなのか
					disp_mode	=	DISP_EDIT;
					ret			=	DISPATCH_DEFAULT;
				}
			}else{
				htb.put("_msg","★ 確認画面は差し込み送信でのメールイメージを確認するのに使います．現在は差し込み送信モードではありません．");
				htb.put("_mail_edit_mode","ON");						// モードの違いにより送信対象のリストが、名簿なのか差し込みリストなのか
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;
			}
			// 
		}else if(cmd.equals("REVERSE")){
			if(LOG.fa) LOG.println(method + "リバースボタンの処理。モードを反転します");
			//
			htb.put("_rv_flag","ON");							// オールクリア／ＯＮの処理をさせるためにフラグを立てる
			//
			String clMode	= strHash(htb,"_clear_mode");		// ⇒処理の方向を決めるフラグを取り出しておく
			if(isEmpty(clMode))			clMode 	= "OFF";		//
			if(clMode.equals("ON")){							//  _clear_mode は parmPringOPT() では設定できないので、
				htb.put("_clMode"	 ,"ON");	// 今回			// 次回のために _clear_mode の値をここで反転しておく．
				htb.put("_clear_mode","OFF");	// 次回			// しかし parmPringOPT() で反転後の値を参照すると次回の
			}else{												// モードで設定するという誤動作になる
				htb.put("_clMode"	 ,"OFF");					// そのため、parmPringOPT() で参照する変数 _clMode をここ
				htb.put("_clear_mode","ON");					// で作成して htb に埋め込んでおく
			}
			//
			htb.put("_mail_edit_mode","ON");					// 編集モードをONにしておかないと文面が消えてしまう
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
			
		}else if(cmd.equals("SEND_FILE")){
			if(LOG.fa) LOG.println(method + "差込ファイルを受け取ります");
			//
			boolean result = receiveFile( out, htb, para);	// マルチインプットから差込ファイルを受け取り (user)/mailtemp/ に格納する
			if(result){										// 送信されたファイルは学籍番号の列がどこか、また正しい学籍番号が書いてあるか
				makeTagFile(out,htb,para);					// 不明であるので、これを検査して正しいタグファイルを生成する
				//
				htb.put("_mail_mode","ON");					// 強制的に差し込みモードにする
				htb.put("_no_sashikomi","");
				htb.put("_ok_sashikomi","selected");
				//
				htb.put("_rv_flag","INIT");					// 送信先名簿が代わるのでチェックボックスの指定を全て初期化する
				//
			}else{
				if(LOG.fa) LOG.println(method + "差込ファイルの受け取りに失敗しました");
			}
			htb.put("_mail_edit_mode","ON");	// 編集モードをONにしておかないと文面が消えてしまう
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
			//
		}else if(cmd.equals("RETURN")){
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;
		
		}else if(cmd.equals("ERROR_FILE")){
			if(LOG.fa) LOG.println(method +"exwork class #sub_mail() -- ＜ERROR_FILE＞ ファイル名入力欄が空白です");
			 //
			htb.put("_msg", "差込ファイルが指定されていません。差込みたいファイルがあるときのみこのボタンを使います。");
			htb.put("_mail_edit_mode","ON");// 編集モードをONにしておかないと文面が消えてしまう
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
			
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
	/**
	 * 送信先名簿に少なくともひとつはチェックが入っているかどうか
	 * @param 		csvStr --- 名簿のチェック状態を示すCSV。チェックの時は"1"
	 * @return     少なくともひとつはチェックされている時 true
	 */
	boolean	existMailto(String	csvStr){
		
		Csv			cs		=	new	Csv(csvStr);
		int			n		=	cs.size();
		boolean	retCode	=	false;	//ない	
		for(int i=0; i<n; i++){
			String	s	=	cs.get(i);
			if(s.equals("1")){
				retCode	=	true;
				break;
			}
			
		}
		return	retCode;
	}
	
	//
	//// オリジナルの差込ファイルの有無を見る
	//
	public boolean isExistSashikomi(Hashtable htb,Param para){
		String	aplec_key	= strHash(htb,"_aplec_key");
		String	teUid		= strHash(htb,"_teUid");
		String 	path 		= para.getMailTempDir(teUid ,aplec_key ) + Exmail.NAME_OF_I;
		File	fp			= new File(path);
		if(!fp.exists()){
			return	false;
		}
		return true;
	}
	//
	//  メールを送信する
	//
	//
	//	eXmail server の受け取り項目は以下のとおり．
	//	このうち mailhost は使用していないのでダミーで構わない
	//	
	//	try{
    //      //アプレットからのデータを読む
    //      ObjectInputStream in = new ObjectInputStream(req.getInputStream());
    //        try{
    //          //
	//			mailhost   = (String)in.readObject();   // *メールサーバー名（結局、使っていない））
    //          title      = (String)in.readObject();   // メールタイトル
    //          from       = (String)in.readObject();   // 発信者メールアドレス
    //          template   = (String)in.readObject();   // テンプレート
    //          mailAdmin  = (String)in.readObject();   // *メール管理者
    //          dt         = (Vector)in.readObject();   // 差込データ配列
    //          itms       = (Vector)in.readObject();   // 差込データ配列の項目名 2002.4. 追加
	//			midnight   = (String)in.readObject();   // *ミッドナイトオプションフラグ 2003.1.4 追加
	//			//
	//			servletURL = (String)in.readObject();   // *サーバーサーブレットのURL 2002.9.7 追加
	//	        //
	//			path1	   = (String)in.readObject(); 	// *台帳のパス
	//			path2	   = (String)in.readObject();
	//			path3	   = (String)in.readObject();
	//			appPassword  = (String)in.readObject(); // *アプリケーション版対応 2003.1.7 追加
	//			//
	//			in.close();
	// 
	// 
	// 
	void	checkboxInfo(Hashtable htb){
		Vector	ckbox	= new Vector(200,100);
		if(LOG.fa) LOG.println("■class exwork #checkboxInfo() : チェックボックスの状態を CSV にまとめる");
		//
		String temp = strHash(htb,"_ckbox");
		if(LOG.fa) LOG.println("現在："+ temp);
		int		n	= (new Csv(temp)).size();
		for(int i=0; i<n; i++){
			if(LOG.fa) LOG.println("□-"+ i);
			String rv		= "_rv" + String.valueOf(i);
			String value	= strHash(htb,rv);
			if(!isEmpty(value)){// =ON
				if(LOG.fa) LOG.println("□-fill");
				htb.put("_rvSign","checked");
				ckbox.add("1");
			}else{
				if(LOG.fa) LOG.println("□-empty");
				htb.put("_rvSign","");
				ckbox.add("0");
			}
		}
		if(LOG.fa) LOG.println("class exwork #checkboxInfo() : 再作成したチェックボックス情報＝" + (new Csv(ckbox)).getCsvString());
		//
		htb.put("_ckbox",(new Csv(ckbox)).getCsvString());
	}
	//
	//
	boolean mail_send(PrintWriter out,Hashtable htb,Param para){
		
		if(LOG.fa) LOG.println("★HybridMail#mail_send："+LocalTime.now());
		//
		// 送信選択
		//
		//  チェックボックスはチェックしてONのときしか値が帰ってこないので _se_email などはnullの可能性
		//  がある。そこで、これらを移したsendEmail、sendKmailを使って判断する
		//
		String	mail_mode	= strHash(htb,"_mailMode");		// 差し込みモードか否か（ON / OFF）
		String	sendEmail	= strHash(htb,"_sendEmail");	// インターネットメールを送信する（ON / ?）
		String	sendKmail	= strHash(htb,"_sendKmail");	// 携帯メールを送信する（ON / ?）
		//
		// 送信データ
		String	from		= 	strHash(htb,"_returnAd");
		String	subject		=	strHash(htb,"_mailSubject");
		String	template_i	=	strHash(htb,"_emText");
		String	template_k	=	strHash(htb,"_kmText");
		
		String returnMessage = ""; // 送信結果のメッセージ
		//
		//
		// データチェック
		if( isEmpty(from) ) {
			htb.put("_msg","★ 返信メールアドレスを記入してください");
			return false;
		}
		if( isEmpty(subject) ) {
			htb.put("_msg","★ メール題名（タイトル）を記入してください");
			return false;
		}
		if( isEmpty(template_i)&&(sendEmail.equals("ON")) ) {
			htb.put("_msg","★ e-mail のメール本文を記入してください");
			return false;
		}
		if( isEmpty(template_k)&&(sendKmail.equals("ON")) ) {
			htb.put("_msg","★ 携帯メールの本文を記入してください");
			return false;
		}
		if( (!sendEmail.equals("ON"))&&(!sendKmail.equals("ON")) ){
			htb.put("_msg","★ 送信したいメールの種類をチェックしてください");
			return false;
		}
		//
		// その他のデータ
        String	mailAdmin	=	para.getMailadmin();
		String	servletURL	=	para.getServletUrl(); // eXmailサーバーURL
		String returnMsg_i = "";
		String returnMsg_k = "";
		
		
		/* **** メール送信サービスを使うように改訂 2021,4.5 ************************************/
		
		// 差し込みメール送信 ////////////////////////////////
		if(mail_mode.equals("ON")){
			if(sendEmail.equals("ON")){

				// メールサービスで差し込みEメールを送信する
				// 差し込みデータファイルの文字セットを指定する
				returnMessage = callSashikomiMailService(htb, para, EMAIL, "MS932");	// Email=1、スマホ=2
				htb.put("_emText",returnMessage);
			}
			if(sendKmail.equals("ON")){
				// メールサービスで差し込みスマホメールを送信する
				// 差し込みデータファイルの文字セットを指定する
				returnMessage = callSashikomiMailService(htb, para, KMAIL, "MS932");	// Email=1、スマホ=2
				htb.put("_kmText",returnMessage);
			}
			
			
		// 同報メール送信 ////////////////////////////////
		}else{
			
			if(sendEmail.equals("ON")){
				
				// メールサービスでEメールを送信する
				returnMessage = callEmailService(htb, para, EMAIL);
				htb.put("_emText",returnMessage);
			}
			if(sendKmail.equals("ON")){
				
				// メールサービスでスマホメールを送信する
				returnMessage = callEmailService(htb, para, KMAIL);
				htb.put("_kmText",returnMessage);
			}
		}
		
		if(LOG.fa) LOG.println("★HybridMail#送信処理終了："+LocalTime.now());
		
		return true;
	}

	/** ****************************　一斉メール送信　****************************************************/

	public static int EMAIL = 1;
	public static int KMAIL = 2;
	
	
	/**
	 * メールサービスでメールを送信する
	 * @param htb
	 * @param para
	 * @param mailShubetu インターネットメールの時１，それ以外は2
	 */
	public String callEmailService(Hashtable htb,Param para, 
								   int mailShubetu)			// Email=1、スマホ=2
	{
		
		DataSet ds = createDataSet(htb,para, mailShubetu);	// データセット作成
		JmClient jc = new JmClient();							// サービスクライアント作成
		Response res = jc.send(ds);
		String message = res.readEntity(String.class); 		// 応答を取得
		return message;
	}
	
	public String callSashikomiMailService(Hashtable htb, Param para,
											int mailShubetu,		// Email=1、スマホ=2
											String charset)			// 差し込みデータファイルの文字コード
	{

		// データセットを作成する
		DataSet ds = createSashikomi_DataSet(htb, para, 
											 mailShubetu,	// Email=1、スマホ=2,	
											 charset);		// 差し込みデータファイルの文字コード
		
		JmClient jc = new JmClient();									// サービスクライアント作成
		Response res = jc.send_Sashikomi(ds);
		String message = res.readEntity(String.class); 				// 応答を取得
		return message;
		
	}
	
	
	/**
	 * 名簿オブジェクトを返す
	 * @param htb
	 * @param para
	 * @return	Meibo型のオブジェクト
	 */
	public Meibo getMeibo(Hashtable htb,Param para) {
		String  szDB		= strHash(htb,"_szDB");
		String	teUid		= strHash(htb,"_teUid");
		String	aplec_key	= strHash(htb,"_aplec_key");
		//
		KamokuApRecord	kar	= new KamokuApRecord(teUid,aplec_key,db);	// kar.getMeiboPath(para) は、データベースを引いて
		Meibo			mb	= new Meibo(kar.getMeiboPath(para));		// 名簿ファイル名を調べ,para からパスを得て、meibo を生成する
		return mb;
	}
	
	/**
	 * メールサービス用のデータセットを作成して返す
	 * 送信メールテキストはインターネットメールとスマホで共通にした
	 * 
	 * @param htb
	 * @param para
	 * @param mailShubetu インターネットメールの時１，それ以外は2
	 * @return DataSet型のデータ
	 */
	public DataSet createDataSet(Hashtable htb,Param para, int mailShubetu) {
		
		String	sender		= 	strHash(htb,"_returnAd");
		String	title		=	strHash(htb,"_mailSubject");
		String	message		=	strHash(htb,"_emText");		// 本文または本文のテンプレート
		
		
		Meibo meibo = getMeibo(htb, para);
		
		List<Recipient> recipients = getRecipients(htb, meibo, mailShubetu);
		
		DataSet ds = new DataSet(sender, 		// 送信元アドレス
								 title, 		// メール表題
								 "TO", 			// 個別に送信
								 message, 		// メール本文   ★差し込みでは、このデータを変更するだけ
								 recipients);	// 対象者リスト ★送信対象者のみのリスト
		return ds;
		
	}

	
	/**
	 * 送信対象者のRecipientレコードをリストにして返す
	 * @param htb
	 * @param mb
	 * @param mailShubetu インターネットメールの時１，それ以外は2
	 * @return　送信対象者のリスト（Recipientレコードのリスト）
	 */
	public List<Recipient> getRecipients(Hashtable htb, Meibo mb, int mailShubetu) {
		
		String  szDB	= strHash(htb,"_szDB");
		String 	ckbox 	= strHash(htb,"_ckbox");
		Csv		ck		= new Csv(ckbox);
		
		List<Recipient> recipients = new ArrayList<>();
		for(int i=0; i<mb.size(); i++){
			if((ck.get(i)).equals("1")){	// チェックが入っている（＝送信対象）

				// 学生オブジェクトをDBから検索して作成し、メールアドレスを得る
				Student	st	= new Student(szDB,mb.getNumber(i), db);
				String mailad = mailShubetu==EMAIL ? st.email() : st.keitai();
				
				// メールアドレスが登録されていなければRecipientを作成しない
				if(mailad.equals("-"))	continue;
				
				// 送信対象者オブジェクト（Recipient）を作成する
				Recipient recipient = new Recipient(true,				// 送信対象であることを示す 
													  mb.getNumber(i), 	// 対象者のID番号
													  mb.getName(i),	// 対象者の漢字氏名
													  mailad, 			// 対象者のメールアドレス
													  "本文",	// 差し込みメール送信時にのみ必要なので、これはダミー
													  "様");			// メール宛名の敬称
				
				recipients.add(recipient);
				
			}
		}
		// 送信対象者オブジェクト（Recipient）のリストを返す
		return recipients;
	}

	/** ********************* 差し込みメール送信 *************************************************************/
	
	/**
	 * メールサービス用の差し込みデータセットを作成して返す
	 * 送信メールテキストはインターネットメールとスマホで共通にした
	 * 
	 * @param htb
	 * @param para
	 * @param mailShubetu インターネットメールの時１，それ以外は2
	 * @return Sashikomi_DataSet型のデータ
	 */
	public DataSet createSashikomi_DataSet(Hashtable htb,Param para, 
											int mailShubetu, 	// インターネットメールの時１，それ以外は2
											String charset) 	// // 差し込みデータファイルの文字セット
	{
		
		String	sender			= 	strHash(htb,"_returnAd");
		String	title			=	strHash(htb,"_mailSubject");
		String	msgTemplate	=	strHash(htb,"_emText");		// 本文のテンプレート(email、スマホで共通)
		
		
		// 送信対象者オブジェクト（Recipient）のリストを作成する
		Meibo meibo = getMeibo(htb, para);
		List<Recipient> recipients = getSashikomi_Recipients(htb, 
															  meibo,			// クラス名簿オブジェクト 
															  msgTemplate, 	// 本文のテンプレート
															  mailShubetu,		// インターネットメールの時１，それ以外は2
															  charset);			// 差し込みデータファイルの文字セット
		
		// サービスに送信するデータオブジェクトを作成する
		DataSet ds = new DataSet(sender, 		// 送信元アドレス
								 title, 		// メール表題
								 "TO", 			// 個別に送信
								 msgTemplate,	// 本文テンプレート
								 recipients);	// 対象者リスト ★送信対象者のみのリスト
		
		if(DBG.fa) {
			DBG.println("-- DataSet ---");
			DBG.println(ds.toString());
			DBG.println("--------------");
		}
		
		return ds;
		
	}	
	
	/**
	 * 送信対象者のSashikomi_Recipientレコードをリストにして返す
	 * 差し込み処理済みのメール本文をフィールドに持つ
	 * 
	 * @param htb
	 * @param mb
	 * @Param msgTemplate 差し込みメールのテンプレート
	 * @param mailShubetu インターネットメールの時１，それ以外は2
	 * @return　送信対象者のリスト（Sashikomi_Recipientレコードのリスト）
	 */
	public List<Recipient> getSashikomi_Recipients(Hashtable htb, 
															 Meibo mb,				// クラス名簿オブジェクト
															 String msgTemplate,	// 差し込みテンプレート
															 int mailShubetu, 		// インターネットメールの時１，それ以外は2
															 String charset)		// 差し込みデータファイルの文字セット
	{
		
		String  szDB	= strHash(htb,"_szDB");
		String 	ckbox 	= strHash(htb,"_ckbox");
		
		Csv		ck		= new Csv(ckbox);
		
		List<Recipient> recipients = new ArrayList<>();
		
		// 学生IDで検索できる差し込みデータのマップ
		// NIO2を使うので、差し込みファイルの文字コードを指定する必要がある
		Map<String, Map<String, String>> sashikomiMaps = sashikomiHash(charset);
		
		for(int i=0; i<mb.size(); i++){
			
			if((ck.get(i)).equals("1")){	// チェックが入っている（＝送信対象）
				
				// データベースを引いて学生オブジェクトを作成し、メールアドレスを得る
				Student	st	= new Student(szDB,mb.getNumber(i), db);
				String mailad = (mailShubetu==EMAIL ? st.email() : st.keitai());
				
				// メールアドレスが登録されていなければRecipientを作成しない
				if(mailad.equals("-"))	continue;				

				
				// 差し込みメール本文を作成する
				String message = sashikomi_message(mb.getNumber(i),	// 名簿から取った学籍番号 
													msgTemplate,		// 本文のテンプレート
													sashikomiMaps);	// 学生IDで検索できる差し込みデータのマップ	
				
				// 送信対象者オブジェクト（Recipient）を作成する
				Recipient recipient = 	new Recipient(true,					// 送信対象であることを示す 
														mb.getNumber(i), 	// 対象者のID番号
														mb.getName(i),		// 対象者の漢字氏名
													  	mailad,				// 対象者のメールアドレス
													  	message,			// 差し込み処理済のメール本文
													  	"様");				// メール宛名の敬称
				recipients.add(recipient);
				
			}
		}
		// 送信対象者オブジェクト（Recipient）のリストを返す
		return recipients;
	}
	/**
	 * 差し込みメールの本文を作成して返す
	 * 
	 * @param id			学生ID
	 * @param template		テンプレート文
	 * @param sashikomiMaps	学生IDで検索できる差し込みデータのマップ
	 * @return				差し込み処理済みのメール本文
	 * 
	 */
	public String sashikomi_message(String id, String template, Map<String, Map<String, String>> sashikomiMaps) {
		
		// この学生用のMapを取得する
		Map<String, String> sashikomiMap = sashikomiMaps.get(id);
		 
		//差し込みデータを書き換えた本文を返す
		String msg = sashikomi(sashikomiMap, template);
		return msg;
	}
	
	
	//
    // {}で囲まれた部分をパースして置きかえる
	// テンプレートでは文字 '{'、 '}' は文の内容として使えないことに注意
	//
	public String sashikomi(Map<String, String> sashikomiMap,String template){
    	
		StringBuffer bf = new StringBuffer(102400);// これで十分か？100KB
        String dt;
        try{
            BufferedReader r = new BufferedReader(new StringReader(template));
            while((dt=r.readLine())!=null){
                
            	/*
            	 * 1行分の処理
            	 */
            	StringTokenizer st = new StringTokenizer(dt,"{}");
                String line = "";
                while(st.hasMoreTokens()){
                    String s = st.nextToken();
                    if((s.startsWith("$"))&&(s.length()>=2)){//＄･･で最低2文字
                    	String key = s.substring(1);
                        line += sashikomiMap.get(key);
                    	
                    }else {
                    	line += s;
                    }
                }
                // 行データと改行を加えてバッファに入れる
                bf.append(line + "\n");	// 
                
            }
        }catch(Exception e) {
        	e.printStackTrace();
        }
        return bf.toString();
    }
	
	/**
	 * 学籍番号で検索できる、差し込みデータのマップを作成して返す
	 * 
	 * 　キー＝学籍番号
	 * 　値　＝＜差し込み項目名、値＞のMaｐ
	 * 
	 * 
	 * @return
	 */
	public Map<String, Map<String, String>> sashikomiHash(String charset){
		
		// ファイルの文字コードを指定して、差し込みデータのリスト（全学生分）を読み出す
		List<String> sashikomiList = getSashikomiDataList(charset);
		
		
		String items = sashikomiList.get(0);					// １行目は項目名のCSVデータ
		Csv itemsCsv = new Csv(items);							// CSVデータから項目名のCsvオブジェクトを作成
		
		// IDで検索できる,（各IDごとの）差し込み項目名と差し込みデータのマップ
		Map<String,Map<String, String>> idMap = new LinkedHashMap<>();
		
		for(int i=1; i< sashikomiList.size(); i++) {		// 学生数分のループ
			
			// Csv型のi番目の差し込みデータ
			Csv dataCsv = new Csv(sashikomiList.get(i));		// 一人の学生のCSV
			
			// 学生ごとの＜項目名, 値＞のMapを作成する
			Map<String, String> map = new HashMap<>();
			for(int k=0; k<dataCsv.size(); k++) {
				// itemsCsv.get(k) --- k番目の項目名
				// dataCsv.get(k) ------- k番目の値
				map.put(itemsCsv.get(k), dataCsv.get(k));		// 項目名、値のマップを作成する
			}
			// dataCsv.get(1)は学籍番号
			// 学籍番号がキー、＜項目名、値＞のマップが値になる
			// システムで、dataCsv.get(0)にはメールアドレスが付加されているので、dataCsv.get(1)が番号になる
			/* *****************************/
			idMap.put(dataCsv.get(1), map);
			/* *****************************/		}
		return idMap;
		
	}
	
	/**
	 * CSV形式の差し込みデータファイルをリストに読み込んで返す
	 * リストの1行目は項目名
	 * リストの1列目はID（学籍番号）でなければならない
	 * インターネットメールと携帯メールの区別はしない
	 * NIOを使うので、UTF-8がデフォルト。ファイルの文字セットを指定する必要がある
	 * 
	 * @return
	 */
	List<String> getSashikomiDataList(String charset){

		String	fpath	= para.getMailTempDir(teUid,aplec_key) + Exmail.NAME_OF_I;  // ファイル名　<dir>/temp_i.csv など
		Path path = Paths.get(fpath);
		List<String> sashikomiList;
		
		//DBG.println("fpath="+ fpath);
		
		try {
			// NIOはUTF-8がデフォルトなので、文字セットの指定が必須
			sashikomiList = Files.readAllLines(path, Charset.forName(charset)); 
			
		} catch (IOException e) {
			sashikomiList = new ArrayList();	// 空のリスト
			DBG.println("★データファイルがない");
			DBG.println(e.toString());
		}
		return	sashikomiList;
	}
	/** ********************************************************************************************************/	
		

	// ★使っていないはずだが、念のため消さないでおく
	
	// 差し込みモードのとき
 	// 項目名ベクトルとデータテーブルを生成して返す
	// パラメータにより、インターネットメール用、携帯メール用と使い分ける
	boolean	sashikomiData(Vector items,Vector dt,boolean keitai,Hashtable htb,Param para){
		if(LOG.fa) LOG.println("class exwork #sashikomiData() : 差し込みモードのときメール用ベクターを生成します");
		//
		String fname;
		if(keitai){
			fname	= Exmail.NAME_OF_K;
		}else{
			fname	= Exmail.NAME_OF_I;
		}
		//
		String	teUid		= strHash(htb,"_teUid");
		String	aplec_key	= strHash(htb,"_aplec_key");
		Vector	vFile		= new Vector(200,100);
		String	path		= para.getMailTempDir(teUid,aplec_key) + fname;
		File	fp			= new File(path);
		if( !fp.exists() )	return false;
		//
		loadToVector( path, vFile);
		Csv cs  = new Csv( (String)vFile.get(0) );
		for(int i=0; i<cs.size(); i++){
			items.add(cs.get(i));
		}
		if(LOG.fa) LOG.outVector( items, "class exwork #sashikomiData() : 差し込みモードの時の項目名ベクターです");
		//
		String 	ckbox 	= strHash(htb,"_ckbox");
		if(LOG.fa) LOG.println("class exwork #sashikomiData() : チェックボックスデータです");
		if(LOG.fa) LOG.println( "    ⇒ " + ckbox );
		Csv		ck		= new Csv(ckbox);
		for(int i=1; i<vFile.size(); i++){
			if((ck.get(i-1)).equals("1")){
				String csdata 	= (String)vFile.get(i);
				Csv    csv 		= new Csv(csdata);
				dt.add(csv.toVector());
				if(LOG.fa) LOG.outVector(csv.toVector(), "class exwork #sashikomiData() : 差し込みモードの時の[" + (i-1) + "]番目のデータベクターを出力しました");
			}
		}
		return	true;
	}	
	
	
	void normalData(Vector items,Vector dt,Hashtable htb,Param para){
		if(LOG.fa) LOG.println("class exwork #normalData() : 差し込みモードでないときメール用ベクターを生成します");
		//
		items.add("mailto");
		items.add("番号");
		items.add("予備アドレス");	// 携帯メールアドレスを入れておく
		//
		emailData(dt,htb,para);
	}
	//
	// ノーマルモードのインターネットメール用ベクターを生成する
	//
	void emailData(Vector dt,Hashtable htb,Param para){
		if(LOG.fa) LOG.println("class exwork #emailData() : ノーマルモードのインターネットメール用ベクターを生成します");
		//
		String  szDB		= strHash(htb,"_szDB");
		String	teUid		= strHash(htb,"_teUid");
		String	aplec_key	= strHash(htb,"_aplec_key");
		//
		KamokuApRecord	kar	= new KamokuApRecord(teUid,aplec_key,db);	// kar.getMeiboPath(para) は、データベースを引いて
		Meibo			mb	= new Meibo(kar.getMeiboPath(para));		// 名簿ファイル名を調べ,para からパスを得て、meibo を生成する
		//
		// 名簿から学籍番号を得て、それを使って Student オブジェクトを作成する
		// Student には学生の全情報が含まれるので、これから学籍番号、e-mailアドレス、携帯アドレスを得る
		// eXmail server がレコードはVectorであることを必要とするので、rec を作ってこれに値を詰め込み
		// dt に add する。
		// 携帯アドレスも記録しておくのは、データベースを２度引きしなくてもいいようにするため。
		// 携帯用の dt が必要なときはこのdtを参照して作成できる
		//
		String 	ckbox 	= strHash(htb,"_ckbox");
		if(LOG.fa) LOG.println("class exwork #emailData() : チェックボックスデータです");
		if(LOG.fa) LOG.println( "    ⇒ " + ckbox );
		Csv		ck		= new Csv(ckbox);
		//
		for(int i=0; i<mb.size(); i++){
			if((ck.get(i)).equals("1")){	// チェックが入っている（＝送信対象）
				Vector	rec	= new Vector(5,5);
				Student	st	= new Student(szDB,mb.getNumber(i), db);
				rec.add( st.email() );	// Student のフィールド値はnullでないことが保証されている
				rec.add( st.id() );	
				rec.add( st.keitai() );
				if(LOG.fa) LOG.println( "     学籍番号 = " + st.id() + " のデータを出力しました" );
				//
				dt.add(rec);
			}
		}
	}
	//
	// 差し込みモードでないときインターネットメール用のデータテーブルから携帯用のデータテーブルを得る
	//
	//  インターネット用のデータを作成していれば、
	//  携帯メールアドレスもフィールドとして持っている
	//  ので、これから順序を変えるだけで、携帯用データを作成できる
	//
	Vector keitaiFromNormal(Vector inet){
		Vector	temp	= new Vector(200,100);
		for(int i=0; i<inet.size(); i++){
			Vector	rec		= new Vector(5,5);
			Vector  i_rec 	= (Vector)inet.get(i);
			rec.add( i_rec.get(2) ); // keitai
			rec.add( i_rec.get(1) ); // stNumber
			rec.add( i_rec.get(0) ); // email
			//
			temp.add(rec);
		}
		return	temp;
	}	//
	//
	// 差込ファイルの受け取り
	//
	void makeTagFile(PrintWriter out,Hashtable htb,Param para){
		if(LOG.fa) LOG.println("class exwork #makeTagFile() : 差込ファイルの受け取りに成功しました");
		//
		//
		String  teUid		= strHash(htb,"_teUid");
		String  aplec_key	= strHash(htb,"_aplec_key");
		// 名簿を作成
		KamokuApRecord	kar = new KamokuApRecord(teUid,aplec_key,db);
		String	Fpath_o		= para.getMailTempDir(teUid,aplec_key) + strHash(htb,"_onlyFilename");
		Meibo	mb			= kar.getMeibo(para);
		String	szDB		= strHash(htb,"_szDB");
		//
		// Exmailクラスはデータを検査しタグファイルを生成する
		// 名簿にないデータは差し込みデータから除外される
		Exmail	exm	= new Exmail( Fpath_o, teUid,aplec_key,mb, szDB, db, out, para);	// 差込送信クラス
		if(exm.isEmptyObject()){
			htb.put("_msg","★ 指定されたファイルはクラス名簿に適合しません．あるいは形式が不正です．");
		}
	}
	//
	// 差込ファイル受け取り処理．（ファイル名未入力ではこの処理には来ない）
	// 
	//    ファイル格納先　ユーザーディレクトリの mailtemp      例）/home/pc/kawaba01/mailtemp/
	//
	//    RETURN  true ---- 成功
	//            false --- 失敗
	// 
	public boolean receiveFile(PrintWriter out,Hashtable htb,Param para){
        String method = "exwork class #receiveFile(): ";
		if(LOG.fa)  LOG.outHash(htb,method + "の先頭です");
		//
		String teUid		= strHash(htb,"_teUid");
		String szDB			= strHash(htb,"_szDB");
		String aplec_key	= strHash(htb,"_aplec_key");
		//
		// マルチパート入力で受け取った変数を得る
        String savedir  = strHash(htb,"_savedir");  		// 一時的格納場所 2003.6.15
        String mfilename = strHash(htb,"_onlyFilename");	// ファイル名のみ
        String mailFile = strHash(htb,"_fileName");			// savedir + onlyFilename == 一時記録ファイルへのフルパス
        String files	 = strHash(htb,"_counts");			// マルチパートで読み込んだファイル数
		//
		boolean eflag    = false;
		int		fcnt	 = Integer.parseInt(files);
		//
        if(mfilename!=null){
        	if(LOG.fa) LOG.println(method + "差込ファイル名を受け取りました/ file name =" + mfilename);
			//
			String ext	= getTriler(mfilename);
			if( !ext.equals("csv") ) {
				if(LOG.fa) LOG.println(method + "ファイルの種類が違います．差込ファイルは csv ファイルでなくてはなりません/ file name = " + mfilename);
				htb.put("_msg","★ ファイルの種類が違います．差込ファイルは CSV ファイルでなくてはなりません");
               	eflag = true;
			}else{
				//
				File mfp   = new File(mailFile);
    	        if(mfp.length()==0){    // （長さがゼロならファイル名の間違い）
	    	        if(LOG.fa) LOG.println(method + "指定された差込ファイルは存在しません/ file name = " + mfilename);
            	    htb.put("_msg","★ 指定されたファイルは存在しません");
                	eflag = true;
	            }
    	    }
		}else{
			eflag = true;
			htb.put("_msg","★ ファイル名を指定してください");
			if(LOG.fa) LOG.println(method + "ファイル名を指定してください");
        }
        //
        if(eflag){// エラーありの場合
            if(LOG.fa) LOG.println(method + "エラーがありましたので差込ファイルを取得できませんでした");
			//
			// もし送信された差込ファイルがあれば削除する
            if((mailFile != null)&&(mailFile.length() > 0)){
                File saveDir    = new File(savedir);
                if(saveDir.exists()){
                    deleteDir(saveDir);
                }
            }
            return false;
        }// ** ここで終わり **
        //
        // もし送信された差込ファイルがあれば正規のディレクトリに移動する
		if((mailFile != null)&&(mailFile.length() > 0)){
            File 	mfp    = new File(mailFile);					// 一時格納場所（ここにある）
            String  dir    = para.getMailTempDir(teUid,aplec_key);	// 差込ファイル用ディレクトリのフルパス ex.  /home/pc/kawaba01/mailtemp/112/
			File    moveTo = new File(dir);
			if(!moveTo.isDirectory()){	// ディレクトリがなければ作る
				moveTo.mkdirs();
			}else{
				deleteFiles(moveTo);	// 移動先の(dir)のファイルを全て消してから移動処理を行う
			}
            //
            boolean flag   = mfp.renameTo(new File(moveTo,mfp.getName()));	// 移動処理
           	deleteDir(new File(savedir));									// 作業ディレクトリの消去
            //
			if(!flag){
               	LOG.errStop("KamokuApRecord class #addMeibo() :指定されたファイルをコピーできない");
				return false;
            }
            if(LOG.fa) LOG.println(method + "正常にファイルを格納しました");
        }
		//
		return true;
	}
	//
	// ファイル拡張子を取り出す
	String getTriler(String filename){
		Csv chk = new Csv(filename,".");
		if(chk.size() <= 1) return "";	// 拡張子がないとき
		//
		return chk.get(chk.size() -1);	// 最後の要素
	}
	/**
	 * ベクターからCsvを作成してそのCSV文字列を返す
	 * @param v
	 * @return
	 */
	String	csvStr(Vector v){
		return	(new Csv(v)).getCsvString();
	}
	/**
	 * 表示されている名簿のチェック／クリアの状態を 0/1 のCSV文字列で得る
	 * @return
	 */
	String	getCeckboxInfo(){
		
		Vector	ckbox	=	new	Vector(30);
		int		n		=	Integer.parseInt( getParameter("max_students"));
		for(int i=0; i<n; i++){

			if(!isEmpty( getParameter(getCBVarName(i)) )){// 現在の値
				/*
				 * check のケース
				 */
				ckbox.add("1");
				
			}else{
				/*
				 * ""のケース
				 */
				ckbox.add("0");
			}
		}
		return	csvStr(ckbox);
		
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
			htb.put("_mailSubject","");		// メール題名、送信文をクリアする
			htb.put("_kmText","");			//
			htb.put("_emText","");			//			
			init();
		}
		// 	差し込みメールの有効／無効を変更するリストボックスの表示
		//  ここでやっておかないと間に合わない
		String	mode	= strHash(htb,"_mail_mode");
		if(mode.equals("ON")){
			htb.put("_no_sashikomi","");
			htb.put("_ok_sashikomi","selected");
		}else{
			htb.put("_no_sashikomi","selected");
			htb.put("_ok_sashikomi","");
		}
		// 曜日時限の文字列を表示する．科目名は既にwebが_titleとして記憶している
		String aplec_key 	= strHash(htb,"_aplec_key");
		String wdateString	= KeyGen.wdateTypeA(aplec_key);
		htb.put("_wdateString",wdateString);
		//
		// 返信先は常に自動設定する
		String teMail = strHash(htb,"_teMail");
		htb.put("_returnAd",teMail);
		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}
	/**
	 *  メール画面表示のための初期パラメータをセットする
	 *
	 */
	public	void	init(){
		//
		htb.put("_rv_flag","INIT");		// リバース、初期化フラグ。ここでは初期状態から始めることを示す
		htb.put("_clear_mode","OFF");	// リバースの方向．最初はクリア
		htb.put("_ckbox","");			// 名簿のチェックボックスの状態．最初なのでなにもない
		//
		/*
		 * _mailMode は差込選択リストボックス
		 * の変数、_mail_mode はそれを記憶しておくための変数である
		 * 初期状態では差し込みメールをオフにしておく．
		 */
		htb.put("_mailMode","OFF"); 
		htb.put("_mail_mode","OFF");
		
		/*
		 * 初期状態では e-mail のみ送信選択とする
		 */
		htb.put("_sendEmail","ON");
		htb.put("_sendKmail","OFF"); 
	}
	// メール送信画面の表示（送信するメール種類のチェックボックス）
	//
	//   送信選択を覚えておくために、プログラムでは
	//    	_sendEmail --- e-mail を送信する
	//		_sendKmail --- ケータイを送信する
	//   を設定する。以下の処理はこの値に基づいてチェックボックスの状態を設定する
	//
    @Override
	public void write(String key,Vector exHtml){

    	if(key.equals("sendMark")){
		
    		sendMark(exHtml);

    	}else if(key.equals("listForMail")){
			
    		listForMail(exHtml);

		}else if(key.equals("tagList")){

			tagList(exHtml);
			
    	}else if(key.equals("listForMail_blankBLK")){
    		
    		listForMail_blankBLK(exHtml);
    	
		}else if(key.equals("tagList_blank_OFF_BLK")){
			
			tagList_blank_OFF_BLK(exHtml);

		}else if(key.equals("tagList_blank_ON_BLK")){

			tagList_blank_ON_BLK(exHtml);
		}
    }
    /**
	 * メール送信画面の表示（送信するメール種類のチェックボックス）
	 * 送信選択を覚えておくために、プログラムでは
	 * 
	 *  	_sendEmail --- e-mail を送信する
	 * 		_sendKmail --- ケータイを送信する
	 * 
	 * を設定する。以下の処理はこの値に基づいてチェックボックスの状態を設定する
	 * 
     * @param exHtml
     */
	void	sendMark(Vector exHtml){
		if(LOG.fa) LOG.outHash(htb,"class HybridMail #write() :メール送信画面の表示（送信するメール種類のチェックボックス）／ sendMark の入り口です");
		//
		String sendEmail	= strHash(htb,"_sendEmail"); if(isEmpty(sendEmail))	sendEmail="OFF";
		if(sendEmail.equals("ON")){
			htb.put("_imSign","checked");
		}else{
			htb.put("_imSign","");
		}
		//
		String sendKmail	= strHash(htb,"_sendKmail"); if(isEmpty(sendKmail))	sendKmail="OFF";
		if(sendKmail.equals("ON")){
			htb.put("_kmSign","checked");
		}else{
			htb.put("_kmSign","");
		}
		printVector(exHtml);
		
	}
	/**
	 * メール送信画面の表示（クラス名簿部分）
	 * 
	 * @param exHtml
	 */
	void	listForMail(Vector exHtml){
		
		String	ckboxInfo=	"";
		if(getParameter("_mail_mode").equals("ON")){
			ckboxInfo	=	sashikomi(exHtml);
		
		}else{
			ckboxInfo	=	normal(exHtml);
		}
		htb.put("_rv_flag","OFF");
		htb.put("_ckbox",ckboxInfo);
		/**
		 * 対象総学生数（差込とそうでない時で違う値）
		 */
		Csv	cs	=	new	Csv(ckboxInfo);
		putParameter("max_students", String.valueOf(cs.size()) );
		
		if(LOG.fa){
			LOG.println("■■ listForMail() : ckbox=" + ckboxInfo);
			LOG.println("■■ listForMail() : max  =" + String.valueOf(cs.size()));
		}
	}
	/**
	 * 
	 * @param exHtml
	 * @return
	 */
	String	sashikomi(Vector exHtml){
		/*
		 * 差込ファイルを得る
		 */
		Vector	vFile	= 	getSashikomiFP();
		int		max		=	vFile.size() - 1;	// １行目はヘッダ
		setData(vFile);
		if(max==0){
			return	"";
		}
		Meibo	meibo	=	getMeibo();
		Vector	ckbox	= 	new Vector(200,100);
		for(int i=0; i<max; i++){
			/*
			 * チェックボックス表示用変数をセットする
			 * また、ckbox に状態を0/1で記録する
			 */
			setCheckBox(ckbox, i);
			/*
			 * 差込データの学籍番号から名簿を引いて学生のデータを得る
			 */
			Csv		dt			= 	new Csv((String)vFile.get(i+1) ); // ０行目がヘッダだから
			String	stNumber	= 	dt.get(1);			
			Csv		st			=	getStData(stNumber, meibo);
			if(st==null){
				setInvalidData(stNumber, ckbox);
			}else{
				htb.put("_stNumber"	,stNumber);		// 学籍番号
				htb.put("_kname"	,st.get(2));	// 氏名
			}
			printVector(exHtml);
		}
		return	csvStr(ckbox);
	}
	/**
	 * 不正な学籍番号の処理
	 * 
	 * @param stNumber
	 * @param ckbox
	 */
	void	setInvalidData(String stNumber, Vector ckbox){
		
		/*
		 * 番号・氏名をセット
		 */
		htb.put("_stNumber"	,stNumber);	// 学籍番号
		htb.put("_kname"	,"名簿に該当なし");	// 氏名
		/*
		 * 直前に設定していたチェックボックスの値をオフにする
		 */
		htb.put("_rvSign","");
		int pos = ckbox.size();
		ckbox.set(pos - 1 ,"0");
		
	}
	/**
	 * 名簿のデータのあるなしをシステムハッシュにセットする
	 * @param v
	 * @return		学生数
	 */
	void	setData(Vector v){
		int	max	=	v.size();	
		if(max==0){
			htb.put("_rv_flag","OFF");
			htb.put("_meiboExist","NO");
			return;
		}
		htb.put("_meiboExist","YES");
		return;
	}
	/**
	 * 差込ファイルの学籍番号から、名簿を引いて一連の学生データを
	 * Csvオブジェクトで得る
	 * 
	 * @param vFile
	 * @param mb
	 * @return
	 */
	Csv	getStData(String stNumber, Meibo mb){
		Csv		meiboData	= mb.get(stNumber);
		return	meiboData;
	}
	

	
	
	/**
	 * 差込ファイルデータをベクターにロードして返す
	 * 
	 * @return
	 */
	Vector	getSashikomiFP(){
		/*
		 * 	差込ファイルは (user)/mailtemp/(aplec_key)/temp_i.csv で固定である
		 */
		String	path	= para.getMailTempDir(teUid,aplec_key) + Exmail.NAME_OF_I;  // <dir>/temp_i.csv など
		File	fp		= new File(path);		
		Vector	v		= new Vector(200,100);
		if(fp.exists()){
			loadToVector( path, v);
		}
		return	v;
	}
	
	
	/**
	 * 
	 * @param exHtml
	 */
	String	normal(Vector exHtml){
		if(LOG.fa) LOG.println("■normal()");
		Vector	ckbox	= 	new Vector(200,100);
		Meibo	meibo	=	getMeibo();
		int		max		=	meibo.size();
		for(int i=0; i<max; i++){
			setCheckBox(ckbox, i);
			htb.put("_stNumber"	,meibo.getNumber(i));	// 学籍番号
			htb.put("_kname"	,meibo.getName(i));		// 氏名
			
			printVector(exHtml);
		}
		return	csvStr(ckbox);
	}
	
	/**
	 * 表示のためにハッシュにデータをセットする
	 * 
	 * 　　_rvSign　と _n   ----- 二つあわせて名簿のチェック状態をセットするのに使う
	 * 
	 * またどの行位置にチェックとクリアが設定されたか0/1の文字をベクターにセットする　　
	 * 
	 * @param 	ckbox
	 */
	void	setCheckBox(Vector ckbox, int i){
		/*
		 * チェックボックスの状態をどうするか設定するフラグを得る。
		 *   OFF ----- 現在の状態を読み取ってそのままセットする
		 *   INIT ---- 全てをチェックの状態にする
		 *   ON ------ 全てをチェックまたはクリアのどちらかに統一する
		 * 　 　　　　 どちらにするかはクリアーモードフラグによる
		 */
		String flag	= getParameter("_rv_flag");

		/*
		 * クリアーモードフラグ
		 * 　ON ------  すべてチェックする　
		 *   OFF -----  すべてクリアする
		 */
		String clMode		= getParameter("_clMode");
		
		
		if(flag.equals("OFF")){	
			if(!isEmpty( getParameter(getCBVarName(i)) )){// 現在の値
				if(LOG.fa) LOG.println("■--11");
				/*
				 * check のケース
				 */
				putParameter("_rvSign","checked");
				ckbox.add("1");
				
			}else{
				if(LOG.fa) LOG.println("■--12");
				/*
				 * ""のケース
				 */
				putParameter("_rvSign","");
				ckbox.add("0");
			}

		}else if(flag.equals("INIT")){
			if(LOG.fa) LOG.println("■--2");
			/*
			 * 初期化モードなので全てチェックに設定する
			 */
			putParameter("_rvSign","checked");
			ckbox.add("1");

		}else{ // ON
			if(LOG.fa) LOG.println("■--3");
			/*
			 * 全てをclModeに従って同じ値にセットする
			 */
			if(clMode.equals("OFF")){
				putParameter("_rvSign","");	
				ckbox.add("0");
			}else{
				putParameter("_rvSign","checked");
				ckbox.add("1");
			}
		}		
		if(LOG.fa){
			LOG.println("■--String.valueOf(i)=" + String.valueOf(i));
			LOG.println("■--ckbox.size()     =" + ckbox.size());
		}
		
		putParameter("_n", String.valueOf(i));// 何番目か
		
	}
	/**
	 * チェックボックスのi番目の変数名を得る
	 * @param i
	 * @return
	 */
	String	getCBVarName(int i){
		return	"_rv" + String.valueOf(i);	

	}
	/**
	 * 名簿を得る
	 * 
	 * @return
	 */
	Meibo	getMeibo(){
		String	teUid		= strHash(htb,"_teUid");
		String	aplec_key	= strHash(htb,"_aplec_key");
		KamokuApRecord	kar	= new KamokuApRecord(teUid,aplec_key,db);
		return	new	Meibo(kar.getMeiboPath(para));
		
	}

	/**
	 * メール送信画面の表示（タグ部分）
	 * 
	 * @param exHtml
	 */
	void	tagList(Vector exHtml){
		if(LOG.fa) LOG.outHash(htb,"class HybridMail #write() : メール送信画面の表示（タグ部分）／tagList の入り口です");
		// _tag と _tagValue に値を入れる
		//
		// 差し込みメールはＯＮか？
		String	mode	= strHash(htb,"_mail_mode");
		if(mode.equals("OFF")){
			if(LOG.fa) LOG.println("メール送信画面の表示（タグ部分）：現在、差し込みメールはＯＦＦですので表示しません");
			return;
		}
		//
		// 差込ファイルの有無を見る
		String	aplec_key	= strHash(htb,"_aplec_key");
		String	teUid		= strHash(htb,"_teUid");
		String 	path 		= para.getMailTempDir(teUid ,aplec_key ) + Exmail.NAME_OF_I;
		File	fp			= new File(path);
		if(!fp.exists()){
			return;
		}
		//  タグとタグの表すデータの例示を作るのに、作業ディレクトリにある NAME_OF_I を使う
		//　インターネットメール向けデータファイルだが、携帯向けとヘッダは同じである
		//
		Vector	vdata	= new Vector(200,100);
		String	fpath	= para.getMailTempDir( teUid ,aplec_key  ) + Exmail.NAME_OF_I;		// temp_i.csv
		loadToVector( fpath, vdata);
		//
		Csv	headder	= new Csv( (String)vdata.get(0) );
		Csv data	= new Csv( (String)vdata.get(1) );	// temp_i.csv作成時に Exmail クラスで確認しているので必ずある
		if(LOG.fa) LOG.println("class HybridMail #write() : headder = [" + headder.getCsvString() + "]");
		if(LOG.fa) LOG.println("                          :    data = [" + data.getCsvString() + "]");
		// 最初の項目はメールアドレスなので表示しない．したがってループの始まりは１からである
		// 表示の都合からデータ例は文字列の長さが３０文字までに制限する
		for(int i=1; i<headder.size(); i++){
			String	hd	= "{$" + headder.get(i) + "}";
			String	dt	= data.get(i);
			if(dt.length() > 30)	dt = dt.substring(0,27) + "･･･";
			//
			htb.put("_tag",hd);
			htb.put("_tagValue",dt);
			printVector(exHtml);
		}
		
	}
	/**
	 * メール表示画面の名簿リストにブランク行を表示する
	 * 
	 * @param exHtml
	 */
	void	listForMail_blankBLK(Vector exHtml){
		if(LOG.fa)  LOG.println("メール表示画面の名簿リストにブランク行を表示します");
		//
		String	mode	= strHash(htb,"_mail_mode");
		if(mode.equals("OFF")){
			if(LOG.fa) LOG.println("現在、差し込みメールはＯＦＦです");
			String  teUid		= strHash(htb,"_teUid");
			String  aplec_key	= strHash(htb,"_aplec_key");
			KamokuApRecord	kar = new KamokuApRecord(teUid,aplec_key,db);	// レコードに名簿ファイル名を持つ
			if(isEmpty(kar.meiboFname())){ // 名簿ファイル名フィールドをチェック
				printVector(exHtml);
				return;
			}
		}else{
			String	meiboExist	= strHash(htb,"_meiboExist");
			if(meiboExist.equals("NO")){	// 差し込みモードで名簿を表示しようとしたが、データがなかったケース
				printVector(exHtml);
				return;
			}
		}
		
	}
	/**
	 * メール表示画面のタグリストにブランク（差し込みメールはＯＦＦ）行を表示する
	 * 
	 * @param exHtml
	 */
	void	tagList_blank_OFF_BLK(Vector exHtml){
		if(LOG.fa)  LOG.println("メール表示画面のタグリストにブランク行（差し込みメールはＯＦＦ）を表示します");
		//
		// 差し込みメールはＯＮか？
		String	mode	= strHash(htb,"_mail_mode");
		if(mode.equals("ON")){
			if(LOG.fa) LOG.println("現在、差し込みメールはＯＮです");
			return;
		}
		printVector(exHtml);
		return;
		
	}
	/**
	 * メール表示画面のタグリストにブランク（差し込みメールはＯＮ）行を表示する
	 * @param exHtml
	 */
	void	tagList_blank_ON_BLK(Vector exHtml){
		if(LOG.fa)  LOG.println("メール表示画面のタグリストにブランク行（差し込みメールはＯＮ）を表示します");
		//
		// 差し込みメールはＯＦＦか？
		String	mode	= strHash(htb,"_mail_mode");
		if(mode.equals("OFF")){
			if(LOG.fa) LOG.println("現在、差し込みメールはＯＦＦです");
			return;
		}
		// オリジナルの差込ファイルの有無を見る
		String	aplec_key	= strHash(htb,"_aplec_key");
		String	teUid		= strHash(htb,"_teUid");
		String 	path 		= para.getMailTempDir(teUid ,aplec_key ) + Exmail.NAME_OF_I;
		File	fp			= new File(path);
		if(!fp.exists()){
			printVector(exHtml);
		}
		return;
		
	}	
}

