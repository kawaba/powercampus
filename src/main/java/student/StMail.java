package student;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import database.Database;
import database.DbConnectionBroker;
import database.KeyGen;
import faq.FAQmail;
import framework.Cp932;
import framework.LOG;
import framework.Param;
import framework.SuperPlayer;
import mailutil_service.Recipient;
import mailutil_service.Util;
/**
*
*	コメントシートを送信する
*
	#
	# ##################
	#     Sample
	# ##################
	#
	<program $student.StMail>
		<dispatch  html=stMail.html  number=5500  class=student.StMail />
		<variable>
		  <receive   NUMBER STAMP GROUP StUID StCLASSINFO StNAME StMAIL StKEITAI  TUID aplec_key lec_key title/>
		  <accept    CMD />
		  <keep      />
		  
		  <form      ml_title  ml_body/>
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
* 		ml_title	タイトル
* 		ml_body		本文
*
*
*/

public class StMail extends SuperPlayer{

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
	Database		db;
	
	String			teUid;
	String			stNumber;
	boolean		send;
	
	public	StMail(){
		super();
		if(LOG.fa) LOG.println("■ StMail #コンストラクタ");
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
		broker	=	getDbConnection();
		db		=	new Database(broker);
		
		teUid	= strHash(htb,"_teUid");
		stNumber	= strHash(htb,"_stNumber");
		db.MembersInfo(teUid,htb);
		send	=	false;
	}
	@Override
	public	String	dispatch(){
		if(LOG.fa) LOG.outHash(htb,"■StMail #dispatch()");
		if(LOG.fa) LOG.println("■StMail #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		if(cmd.equals("SEND")){
			send	= testEmpty();
			if(send)	{
				receive_qmail(out,htb,para);
				disp_mode	=	DISP_NEW;
				ret			=	DISPATCH_DEFAULT;	// 再表示
				
			}else{
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;	// 再表示
			}
		
		}else if(cmd.equals("RETURN")){
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;	// 復帰			

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
	 * データが空でないかチェックする
	 */
	boolean testEmpty(){
		if(LOG.fa) LOG.println("class stwork #testEmpty() : データが空でないかチェックする の先頭です");
		//
		String title = strHash(htb,"_ml_title");
		if(isEmpty(title)){
			htb.put("_msg","★ 件名が入力されていません．"+ CR + "　　簡潔で本文内容を示す具体的な件名を書いてください．");
			return false;
		}
		String body = strHash(htb,"_ml_body");
		if(isEmpty(body)){
			htb.put("_msg","★ 本文が入力されていません．");
			return false;
		}else{
			if((body.trim()).length()==0){
				htb.put("_msg","★ 本文が入力されていません．");
				return false;
			}
		}
		return true;
	}
	/**
	 * 送信
	 * @param out
	 * @param htb
	 * @param para
	 */
	void receive_qmail(PrintWriter out,Hashtable htb,Param para){
		//
		setupData();
		
		FAQmail fqm		= new FAQmail(db);
		fqm.byHash(htb);
		//
		// 本文の最後に線を入れて登録
		String  body_new		= fqm.ml_body() + CR + "-----------------------------------------------------------" + CR;
		fqm.set_ml_body(body_new);
		int n = fqm.insert_FAQmail();
		if(n==0){
			LOG.println("★★ 質問メールをデータベースにかけませんでした");
		}else{
			// 担当教員へお知らせのメールを出す 2004.2.
			// 
			String	title	= "[Question/Comments: " + strHash(htb,"_title") + "] " + strHash(htb,"_ml_title");
			String	host	= para.getMailhost();
			//
			// なぜか _user_mail の値が消えてしまうことがあるので
			// ない場合はデータベースを引くように訂正
			String	to		= strHash(htb,"_user_mail");	// 教員メールアドレス
			if(isEmpty(to,"_user_mail")){
				Hashtable	wtbl	=	getTeacherInfo(teUid, db);
				htb.put("_user_mail", strHash(wtbl,"_user_mail"));
			}
			//String	from	= para.getSysadmin();			// システム設置責任者メールアドレス
			String	from	= to;								// 自分から自分へ
			//
			// 日付文字列
			String 			body 	= strHash(htb,"_ml_body");
			String 			wdate	= CalToStr(currentDay(),false);
			StringBuffer	bf 		= new StringBuffer(1000);
			//
			bf.append("■下記の質問／コメントが出されていますのでお知らせします." + CR);
			bf.append("　　　　　　　提出者への返信は PowerCampus システムから送信してください." + CR + CR);
			bf.append("　発信日時：" + wdate + CR );
			bf.append("　講義科目：" + strHash(htb,"_aplec_key") + "/" + strHash(htb,"_title") + CR );
			bf.append("　発 信 者：" + strHash(htb,"_stNumber")  + "/" + strHash(htb,"_kname") + CR );
			bf.append("　タイトル：" + strHash(htb,"_ml_title")  + CR );
			bf.append("-----" + CR);
			bf.append(strHash(htb,"_ml_body")  + CR );
			body	= bf.toString();
			//
			title 	= Cp932.toJIS(title);	// メールなのでJIS
			body	= Cp932.toJIS(body);
			//
			
			// 2021.4 サービスを使う方法に変更のためコメントアウト
			//sendWithAuth(host,to,from,title,body, para,true);	// true は e-mail であることを示す．false はケータイ
			
			Util.sendMailService(from, title, "TO", body, createToList(to));
			
		}
	}

	/////////// 2021,4 送信方法をサービスを利用する方法に変更　/////////////////////////////////////////////////////////
	
	//
	// 1件だけ（教師宛）の宛先リストを作成
	public List<Recipient> createToList(String to){
		List<Recipient> ls = new ArrayList<>();
		ls.add(new Recipient(true, "kawaba", "川場隆", to, "ダミー本文", "様"));	// trueは送信対象であることを示す
		return ls;
		
	}

	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * データベースを検索して教師データのセットをハッシュテーブルに入れて返す
	 * @param teUid
	 * @param db
	 * @return
	 */
	public	Hashtable	getTeacherInfo(String teUid, Database db){
		//
		Hashtable	wtbl	= new Hashtable(50);
		db.MembersInfo(teUid,wtbl);
		return	wtbl;
	}	
	/**
	 * 規定値のデータなどをハッシュに設定する
	 */
	void setupData(){
		if(LOG.fa) LOG.outHash(htb,"class stwork #setupData() : 規定値のデータなどをハッシュに設定する の先頭です");
		//
		// 講義実施キー
		String	te_aplec_key	= KeyGen.get_te_aplec_key(htb);
		htb.put("_te_aplec_key",te_aplec_key);
		// シーケンスキー
		KeyGen	kg				= new KeyGen(strHash(htb,"_teUid"),db);
		String	seq_no			= kg.nextFaq();
		htb.put("_seq_no",seq_no);
		//
		// タイトルと本文
		// 本来必要ないが、ウェブに依存した名前なのでかえることができるようにしている
		String 	title 			= strHash(htb,"_ml_title");
		String 	body 			= strHash(htb,"_ml_body");
		htb.put("_ml_title",title);
		htb.put("_ml_body",body);
		//
		// 日付文字列
		String wdate	= CalToStr(currentDay(),false);
		htb.put("_rvdate",wdate);
		//
		// 既読フラグなど
		htb.put("_read_flag","OFF");	// 	既読ではない
		htb.put("_faq_flag" ,"OFF");	//  ＦＡＱ未登録
		htb.put("_faq_title","-");		//  ＦＡＱ用タイトル
		//
		return;
	}
	
	/* 2021,2 送信方法変更のため削除
	 * 
	 * pop-before-smtp 認証を確認して１件のメールを送信する
	 * 
	 * @param host
	 * @param to
	 * @param from
	 * @param title
	 * @param msg
	 * @param para
	 * @param email

	public 
		// 必要なら pop before SMTP のためにシステムユーザー名で認証を受けておく
		String pop = "";
		if(email){
			pop = para.popBeforSmtp();		// pop 認証がいるかどうか
	   }else{
			pop = para.popBeforSmtp_k();
		}
		//
		if(pop.equals("yes")){
			String  user    = "";   // POP user名
	       String  passwd  = "";   // pop パスワード
	       if(email){
				user    = para.getMailmaster();             // POP user名
	       	passwd  = para.getMailmasterPass();         // pop パスワード
	       }else{
				user    = para.getMailmaster_k();           // POP user名
		        passwd  = para.getMailmasterPass_k();       // pop パスワード
			}
			boolean auth    = jmAuthenticate.authenticate(host,user,passwd);          // チェック
	       if(!auth){
	           String s1 = "★pop before smtp で認証に失敗しました。UID/PASSWD を確認してください。<P>";
	           String s2 = "   host=" + host + "<br>";
	           String s3 = "   user=" + user + "<br>";
	           String s4 = "   passwd=" + passwd + "<br>";
				String s5 = "   auth=" + auth + "<br>";
	           errPrint(para.getResponseWriter(),s1 + s2 + s3 + s4);
	           return;
	       }
	   }
	

	   try{
	       jmSender.send(host,to,from,title,msg);
	   }catch(Exception e){
	       System.out.println(to + "へのメール送信に失敗しました (" + getDate() + "）");
	       System.out.println( e );
	   }
		
	}	   
	*/
		
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
			htb.put("_ml_body","");		// 本文
			htb.put("_ml_title","");	// タイトル
		}
		if(send){
			htb.put("_msg","★ あなたのコメントシートを受領しました．");
			send	=	false;
		}
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}

}

