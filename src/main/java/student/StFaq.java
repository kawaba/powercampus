package student;

import java.util.regex.*;
import faq.FAQ;
import java.io.*;
import java.util.*;
import database.*;
import framework.*;

/**
 *	ＦＡＱを見る
 *
 	#
	# ##################
	#     StFaq
	# ##################
	#
	<program $student.StFaq>
		<dispatch  html=StFaq.html  number=5600  class=student.StFaq />
		<variable>
		  <receive   NUMBER STAMP GROUP StUID StCLASSINFO StNAME StMAIL StKEITAI  TUID aplec_key lec_key title />
		  <accept    CMD />
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
 * 3. keep
 * 4. form
 *
 *
 */
public class StFaq extends SuperPlayer{

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
	
	public	StFaq(){
		super();
		if(LOG.fa) LOG.println("■ StFaq #コンストラクタ");
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
		broker	=	getDbConnection();
		db		=	new Database(broker);			

	}
	public	String	dispatch(){
		if(LOG.fa) LOG.outHash(htb,"■StFaq #dispatch()");
		if(LOG.fa) LOG.println("■StFaq #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		if(cmd.equals("RETURN")){
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
		if(LOG.fa)	LOG.println("■ StFaq #display(boolean editmode)");
		
		if(!editmode){
			putParameter(MESSAGE,"");
		}
		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}

    public void write(String key,Vector exHtml){
		if(key.equals("faq_list")){
			
			faq_list(exHtml);
		}
    }
    void	faq_list(Vector exHtml){
		
		if(LOG.fa) LOG.println( "calss stwork #paramPrintOPT() : ＦＡＱを表示する の先頭です");
		//
		String	teUid		= getParameter(TUID);
		String	lec_key		= getParameter("lec_key");
		String	te_lec_key	= KeyGen.get_te_lec_key2(teUid,lec_key);
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
			//
			setBody(body);	// ボディ部分を二つに分けて設定する
			//
			printVector(exHtml);
		}

    }
    //
	//  ＦＡＱのボディ部分を二つに分けて HTB にセットする
	//
	void setBody(String body){
		if(LOG.fa) LOG.println("class stwork #setBody() :  ＦＡＱのボディ部分を二つに分けて HTB にセットする の先頭です");
		//
		int MAX = 20;
		StringBuffer	bf1		= new StringBuffer(1024 * 30);
		StringBuffer	bf2		= new StringBuffer(1024 * 30);
		
        try{
            BufferedReader r = new BufferedReader(new StringReader(body));
            Pattern	  pattern	= Pattern.compile("^--------------------"); // 行の先頭が２０個の　'-'
			String dt;
			while((dt=r.readLine())!=null){
				//
				Matcher matcher 	= pattern.matcher(dt);
            	if(matcher.find()) { break; }
				//
				bf1.append(dt+ "\n");
            }
			while((dt=r.readLine())!=null){	// "-----" は読み捨て
				bf2.append(dt+ "\n");
				
			}
        }catch (IOException ee){
            
        }
		//
		htb.put("_faq_body1",bf1.toString());
		htb.put("_faq_body2",bf2.toString());
		//
	}

}

