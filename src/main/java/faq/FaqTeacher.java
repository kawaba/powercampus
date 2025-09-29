/*
     Power Campus FAQ

*/
package faq;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import database.*;
import framework.*;
/**
 * 
 * 教師用のFAQ閲覧
 * 
 *
 	#
	# ##################
	#     FaqTeacher
	# ##################
	#
	<program $faq.FaqTeacher>
		<dispatch  html=FaqTeacher.html  number=6120  class=faq.FaqTeacher />
		<variable>
		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA HOMEURL DIVISION  title lec_key aplec_key />
		  <accept    CMD    faq_seq_no  />
		  <keep      />
		  
		  <form      />
		</variable>
	</program> 
 *
 *
 * 変数の説明
 *
 * 1. receive 
 * 2. accept
 * 		faq_seq_no ---- 編集（または削除）するレコードのキー
 * 3. keep
 * 4. form
 * 
 *
 */
public class FaqTeacher extends SuperPlayer {

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
		
	String 	teUid;
	String	lec_key;
	String 	te_lec_key;
	/**
	 * 
	 *
	 */
	public	FaqTeacher(){
		super();
		if(LOG.fa) LOG.println("■ FaqTeacher #コンストラクタ");
	}
	/**
	 * 
	 */
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		broker	=	getDbConnection();
		db		=	new Database(broker);	
		//
		teUid		= strHash(htb,"_teUid");
		lec_key		= strHash(htb,"_lec_key");
		te_lec_key 	= KeyGen.get_te_lec_key2(teUid,lec_key);
    }
	/**
	 * 
	 */
	public	String	dispatch(){
		if(LOG.fa) LOG.println("■FaqTeacher #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		if(cmd.equals("RETURN")){
			/*
			 *　リーターン
			 */
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;
			
		}else if(cmd.equals("DELETE")){
			FAQ	faq	= createFAQ();						// 削除するレコードを得る
			faq.delete_FAQ();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;

		}else if(cmd.equals("EDIT")){
			FAQ	faq	= createFAQ();						// 編集するレコードを得る
			htb.put("_faq_title",faq.faq_title());
			htb.put("_faq_body",faq.faq_body());
			/*
			 * レコードキーを度設定する
			 * （システムハッシュのfaq_seq_noと同じ）
			 */
			htb.put("_rec_key",faq.seq_no());
			//
			disp_mode	=	DISP_EDIT;
			ret			=	"$faq.FaqEditor";

		}else if(cmd.equals("CREATE")){
			htb.put("_faq_title","");
			htb.put("_faq_body","");
			/*
			 * レコードキーを明示的にクリアする
			 */
			htb.put("_rec_key","");
			//
			disp_mode	=	DISP_NEW;
			ret			=	"$faq.FaqEditor";

		}else if(cmd.equals("EXECUTE")){
			/*
			 *　プログラムの実行
			 */
			disp_mode	=	DISP_NEW;
			ret			=	getParameter(PROGRAM);

		}else{
			/*
			 *　再表示
			 */
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
	 * ＦＡＱレコードを取得する
	 * @return
	 */
	public	FAQ	createFAQ(){
		if(LOG.fa) LOG.outHash(htb,"■class FaqTeacher #getFAQ() : 000 の先頭です");
		//
		String	faq_seq		= strHash(htb,"_faq_seq_no");	// Web（FAQ_teacher.html）に埋め込んでいるので必ずある
		FAQ 	faq			= new FAQ(db);
		faq.add_keys(te_lec_key,faq_seq);
		faq.read_FAQ();
		return	faq;
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
	public  void	display(boolean editmode){
		if(LOG.fa)	LOG.println("■ Sample #display(boolean editmode)");
		
		if(!editmode){
			putParameter(MESSAGE,"");
		}
		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}
	/**
	 * 
	 */
	public void write(String key,Vector exHtml){
        if(LOG.fa) LOG.println("class FaqTeacher #write() : 出力処理 の先頭です");
        //
        if(key.equals("faq_list")){
        	faq_list(exHtml);

        }else if(key.equals("faq_empty")){
        	faq_empty(exHtml);
        }
    }
	/**
	 * 
	 * @param exHtml
	 */
	void	faq_list(Vector exHtml){
		//
		Vector all	= new Vector (100,50);
		db.read_FAQ_all(te_lec_key,all);	// 全レコードを得る
		//
		int	n	= all.size();
		for(int i=0; i<n; i++){
			// レコードを取り出す
			Vector	record	= (Vector) all.get(i);
			String	seqNum	= (String) record.get(FAQ.SEQ_NO);
			String	title	= (String) record.get(FAQ.FAQ_TITLE);
			String	body	= (String) record.get(FAQ.FAQ_BODY);
			//
			htb.put("_faq_n"	  ,seqNum);
			htb.put("_faq_title"  ,title);
			setBody(body);					// ボディ部分を二つに分けて設定する
			//
			printVector(exHtml);
		}
		/*
		 * faq_empty() のために件数を残す
		 */
		putParameter("faqs",String.valueOf(n));
	}
		
	/**
	 * 
	 * @param exHtml
	 */
	void	faq_empty(Vector exHtml){
		int	n	= Integer.parseInt( getParameter("faqs") );	// 既に表示した行があるか
		if(n > 0)	return;
		//
		printVector(exHtml);		
	}
	/**
	 * ＦＡＱのボディ部分を二つに分けて HTB にセットする
	 * @param body
	 */
	void setBody(String body){
		if(LOG.fa) LOG.println("class FaqTeacher #setBody() :  ＦＡＱのボディ部分を二つに分けて HTB にセットする の先頭です");
		//
		int MAX = 20;
		StringBuffer	bf1		= new StringBuffer(1024 * 30);
		StringBuffer	bf2		= new StringBuffer(1024 * 30);
		
        try{
            BufferedReader r = new BufferedReader(new StringReader(body));
            Pattern	  pattern	= Pattern.compile("^-----"); // 行の先頭が５個の　'-'
			String dt;
			while((dt=r.readLine())!=null){
				//
				Matcher matcher 	= pattern.matcher(dt);
            	if(matcher.find()) { break; }
				//
				bf1.append(dt + "\n");
            }
			while((dt=r.readLine())!=null){	// "-----" は読み捨て
				bf2.append(dt + "\n");
				
			}
        }catch (IOException ee){
            
        }
		//
		htb.put("_faq_body1",bf1.toString());
		htb.put("_faq_body2",bf2.toString());
		//
	}
}

