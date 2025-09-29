/*
 	質問メールレコードの処理をカプセル化する 
*
　■Ｑ＆Ａメール用データベース
		
		実施講義単位にまとめ、受け取り時間順に並べる
		
		seq_no ---- 受付番号 ６桁 KeyGen で発生
		　　　　　　個人単位（講義単位ではない）
		
		内容変更に関してはＦＡＱに対する追加・削除・訂正も連動して行う
		
	create table faq_mail (
       te_aplec_key     CHAR(12)        NOT NULL,
	   seq_no           CHAR(6)         NOT NULL,
       rvdate           VARCHAR(16)     NOT NULL,
       lec_key          CHAR(3)         NOT NULL,
       stNumber         VARCHAR(12)     NOT NULL,
       ml_title         VARCHAR(200)    DEFAULT  '',
       ml_body          text            DEFAULT  '',
       read_flag        VARCHAR(6)      DEFAULT 'OFF',
	   faq_title        VARCHAR(200)    DEFAULT '',
       faq_flag         VARCHAR(6)      DEFAULT 'OFF'
	);
	
	create index faq_mail_idx on faq_mail (te_aplec_key,seq_no);

*/
//
//import java.text.*;
package faq;
import java.util.*;

import database.Database;
import framework.LOG;

public class FAQmail extends Object {
	
	////////（ FAQmail データベース ） //////////////////////////////////////////////
	//
	// 定数
	public static int	TE_APLEC_KEY	= 0;	// te_aplec_key   12 桁
	public static int	SEQ_NO			= 1;	// 固有番号
	
    public static int	RVDATE	    	= 2;	// 受取日時
	public static int	LEC_KEY	    	= 3;	// 講義定義キー
    public static int	STNUMBER		= 4;	// 学籍番号
	
    public static int	ML_TITLE		= 5;	// メール件名
    public static int	ML_BODY			= 6;	// メール本文
    public static int	READ_FLAG		= 7;	// 既読フラグ
	
	public static int	FAQ_TITLE		= 8;	// FAQ登録件名
    public static int	FAQ_FLAG		= 9;	// FAQ登録フラグ
	
	// 既読フラグの値
	public static String  READ_FLAG_ON	= "ON";		// 既読
	public static String  READ_FLAG_OFF	= "OFF";	// 未読
	//
	// ＦＡＱフラグの値
	public static String  FAQ_FLAG_ON	= "ON";		// 登録済み
	public static String  FAQ_FLAG_OFF	= "OFF";	// 未登録
	//
	// 反復回数制御
    public static int	MIN = TE_APLEC_KEY;
    public static int	MAX = FAQ_FLAG + 1;
	//
	/////////////////////////////////////////////////////////////////////////////////
	//
    Database	db;
	Vector		rec;	// レコード本体
	//
	// コンストラクタ
	// rec に値を入れて初期化してはならない！！
	//
	public FAQmail(Database _db){
		if(LOG.fa) LOG.println("class FAQmail #FAQmail() :  のコンストラクタの入り口です");
		//
		db		= _db;
		rec		= new Vector (20,10);
		//
	}
	//
	// ハッシュテーブルのデータでレコードを初期化する
	//
	//   あとで set　が使えるように全ての項目に値をセットする
	//   キー部分が null の場合は明らかなエラーとして、プログラムを停止する
	//
	public void byHash(Hashtable htb){
		if(LOG.fa) LOG.outHash(htb,"class FAQmail #byHash() : ハッシュテーブルのデータでレコードを初期化する の入り口です");
		//
		for(int i=0; i<MAX; i++){
			rec.add("-");
		}
		String te_aplec_key	=	strHash(htb,"_te_aplec_key");	if(isEmptyString(te_aplec_key))	LOG.errStop("class FAQmail #byHash() : te_aplec_key が null です．");
		String seq_no		=	strHash(htb,"_seq_no");			if(isEmptyString(seq_no))		LOG.errStop("class FAQmail #byHash() : seq_no が null です．");
		String rvdate		=	strHash(htb,"_rvdate");			if(isEmptyString(rvdate))		rvdate			= "-";
		String lec_key		=	strHash(htb,"_lec_key");		if(isEmptyString(lec_key))		lec_key			= "-";
    	String stNumber		=	strHash(htb,"_stNumber");		if(isEmptyString(stNumber))		stNumber		= "-";
    	String ml_title		=	strHash(htb,"_ml_title");		if(isEmptyString(ml_title))		ml_title		= "-";
    	String ml_body		=	strHash(htb,"_ml_body");		if(isEmptyString(ml_body))		ml_body			= "-";
    	String read_flag	=	strHash(htb,"_read_flag");		if(isEmptyString(read_flag))	read_flag		= "OFF";
		String faq_title	=	strHash(htb,"_faq_title");		if(isEmptyString(faq_title))	faq_title		= "-";
    	String faq_flag		=	strHash(htb,"_faq_flag");		if(isEmptyString(faq_flag))		faq_flag		= "OFF";
		//
		set_te_aplec_key(te_aplec_key);
		set_seq_no(seq_no);
		set_rvdate(rvdate);
		set_lec_key(lec_key);
		set_stNumber(stNumber);
		set_ml_title(ml_title);
		set_ml_body(ml_body);
		set_read_flag(read_flag);
		set_faq_title(faq_title);
		set_faq_flag(faq_flag);
		//
		if(LOG.fa) LOG.outVector(rec,"class FAQmail #byHash() : ハッシュテーブルのデータでレコードを初期化する の出口です");
	}
	// ベクターのデータでレコードを初期化する
	public void byVector(Vector v){
		if(LOG.fa) LOG.outVector(v,"class FAQmail #byVector() :ベクターのデータでレコードを初期化する の入り口です");
		//
		for(int i=0; i<MAX; i++){
			rec.add("-");
		}
		String te_aplec_key	=	(String) v.get(TE_APLEC_KEY);	if(isEmptyString(te_aplec_key))	LOG.errStop("class FAQmail #byHash() : te_aplec_key が null です．");
		String seq_no		=	(String) v.get(SEQ_NO);			if(isEmptyString(seq_no))		LOG.errStop("class FAQmail #byHash() : seq_no が null です．");
		String rvdate		=	(String) v.get(RVDATE);			if(isEmptyString(rvdate))		rvdate			= "-";
		String lec_key		=	(String) v.get(LEC_KEY);		if(isEmptyString(lec_key))		lec_key			= "-";
    	String stNumber		=	(String) v.get(STNUMBER);		if(isEmptyString(stNumber))		stNumber		= "-";
    	String ml_title		=	(String) v.get(ML_TITLE);		if(isEmptyString(ml_title))		ml_title		= "-";
    	String ml_body		=	(String) v.get(ML_BODY);		if(isEmptyString(ml_body))		ml_body			= "-";
    	String read_flag	=	(String) v.get(READ_FLAG);		if(isEmptyString(read_flag))	read_flag		= "OFF";
		String faq_title	=	(String) v.get(FAQ_TITLE);		if(isEmptyString(faq_title))	faq_title		= "-";
    	String faq_flag		=	(String) v.get(FAQ_FLAG);		if(isEmptyString(faq_flag))		faq_flag		= "OFF";
		//
		set_te_aplec_key(te_aplec_key);
		set_seq_no(seq_no);
		set_rvdate(rvdate);
		set_lec_key(lec_key);
		set_stNumber(stNumber);
		set_ml_title(ml_title);
		set_ml_body(ml_body);
		set_read_flag(read_flag);
		set_faq_title(faq_title);
		set_faq_flag(faq_flag);
		//
		if(LOG.fa) LOG.outVector(rec,"class FAQmail #byHash() : ベクターのデータでレコードを初期化する の出口です");
	}
	//
	// 個々のフィールドに値をセットする
	//
	public void set_te_aplec_key(String str)	{ rec.set(TE_APLEC_KEY,str);}
	public void set_seq_no(String str)			{ rec.set(SEQ_NO,str); 		}
	public void set_rvdate(String str)			{ rec.set(RVDATE,str); 		}
	public void set_lec_key(String str)			{ rec.set(LEC_KEY,str); 	}
	public void set_stNumber(String str)		{ rec.set(STNUMBER,str); 	}
	public void set_ml_title(String str)		{ rec.set(ML_TITLE,str); 	}
	public void set_ml_body(String str)			{ rec.set(ML_BODY,str); 	}
	public void set_read_flag(String str)		{ rec.set(READ_FLAG,str); 	}
	public void set_faq_title(String str)		{ rec.set(FAQ_TITLE,str); 	}
	public void set_faq_flag(String str)		{ rec.set(FAQ_FLAG,str); 	}
	//
	// ここのフィールドの値を得る
	//
	public String te_aplec_key()	{	return (String)rec.get(TE_APLEC_KEY); 	}
	public String seq_no()			{	return (String)rec.get(SEQ_NO); 		}
	public String rvdate()			{	return (String)rec.get(RVDATE); 		}
	public String lec_key()			{	return (String)rec.get(LEC_KEY); 		}
	public String stNumber()		{	return (String)rec.get(STNUMBER); 		}
	public String ml_title()		{	return (String)rec.get(ML_TITLE); 		}
	public String ml_body()			{	return (String)rec.get(ML_BODY); 		}
	public String read_flag()		{	return (String)rec.get(READ_FLAG); 		}
	public String faq_title()		{	return (String)rec.get(FAQ_TITLE); 		}
	public String faq_flag()		{	return (String)rec.get(FAQ_FLAG); 		}
	//
	//
	// 未読かどうか
	//
	public boolean	isNotRead(){
		if((read_flag()).equals("OFF")) return true;	// 未読
		return false;
	}
	//
	// ＦＡＱ登録済みかどうか
	//
	public boolean	isOnFaq(){
		if((faq_flag()).equals("ON")) return true;	// 登録
		return false;
	}
	//
	//  第ｉ番目のフィールド値を返す
	//
	public String get(int i){
		//
		if((i<MIN)||(i>=MAX)){
			LOG.errStop("class FAQmail #get(int i) : i の値が不正です i = [" + i + "]");
		}
		return 	(String) rec.get(i);
	}
	//
	// データベースへの処理
	
	//
	// 読み出しのためにキーをレコードにＡＤＤする
	//
	public	void add_keys(String _te_aplec_key,String _seq_no){
		// コンストラクタの直後に呼ぶこと
		// set でなくadd なのは、DB 検索で残りのデータが add されるから
		rec.add(_te_aplec_key);
		rec.add(_seq_no);
	}
	
	//
	// (1) 読み出し
	//
	public int	read_FAQmail(){
		if(LOG.fa) LOG.outVector(rec,"class FAQmail #read_FAQmail() : データベース読み出し の入り口です");
		//
		if(isEmpty(TE_APLEC_KEY))	LOG.errStop("class FAQmail #read_FAQmail() : te_aplec_key が 空 です");
		if(isEmpty(SEQ_NO))			LOG.errStop("class FAQmail #read_FAQmail() : SEQ_NO が 空 です");
		return db.read_FAQmail(rec);
	}
	//
	// (2) 挿入
	//
	public int	insert_FAQmail(){
		if(LOG.fa) LOG.outVector(rec,"class FAQmail #insert_FAQmail() : データベース挿入 の入り口です");
		//
		if(isEmpty(TE_APLEC_KEY))	LOG.errStop("class FAQmail #insert_FAQmail() : TE_APLEC_KEY が 空 です");
		if(isEmpty(SEQ_NO))			LOG.errStop("class FAQmail #insert_FAQmail() : SEQ_NO が 空 です");
		return db.insert_FAQmail(rec);
	}
	//
	// (3) アップデート
	//
	public int	update_FAQmail(){
		if(LOG.fa) LOG.outVector(rec,"class FAQmail #update_FAQmail() : データベースアップデート の入り口です");
		//
		if(isEmpty(TE_APLEC_KEY))	LOG.errStop("class FAQmail #update_FAQmail() : te_aplec_key が 空 です");
		if(isEmpty(SEQ_NO))			LOG.errStop("class FAQmail #update_FAQmail() : SEQ_NO が 空 です");
		return db.update_FAQmail(rec);
	}
	//
	// 本文のみ書き換え（read フラグは自動的にONにする）
	// 
	public int	update_FAQmail_body(String body){
		if(LOG.fa) LOG.outVector(rec,"class FAQmail #update_FAQmail_body() : 本文のみ書き換え（read フラグは自動的にONにする） の入り口です");
		//
		if(isEmpty(TE_APLEC_KEY))	LOG.errStop("class FAQmail #update_FAQmail_body() : te_aplec_key が 空 です");
		if(isEmpty(SEQ_NO))			LOG.errStop("class FAQmail #update_FAQmail_body() : SEQ_NO が 空 です");
		return db.update_FAQmail_body( te_aplec_key(),seq_no(),body);
	}
	//
	// 本文のみ書き換え（フラグはそのまま）
	// 
	public int	update_FAQmail_body_2(String body){
		if(LOG.fa) LOG.outVector(rec,"class FAQmail #update_FAQmail_body_2() : 本文のみ書き換え（read フラグはそのまま） の入り口です");
		//
		if(isEmpty(TE_APLEC_KEY))	LOG.errStop("class FAQmail #update_FAQmail_body_2() : te_aplec_key が 空 です");
		if(isEmpty(SEQ_NO))			LOG.errStop("class FAQmail #update_FAQmail_body_2() : SEQ_NO が 空 です");
		return db.update_FAQmail_body_2( te_aplec_key(),seq_no(),body);
	}
	//
	//
	// readFlag のみアップデート
	public int	update_FAQmail_readFlag(String readFlag){
		if(LOG.fa) LOG.outVector(rec,"class FAQmail #update_FAQmail_reagFlag() : データベースアップデート の入り口です");
		//
		if(isEmpty(TE_APLEC_KEY))	LOG.errStop("class FAQmail #update_FAQmail_reagFlag() : te_aplec_key が 空 です");
		if(isEmpty(SEQ_NO))			LOG.errStop("class FAQmail #update_FAQmail_reagFlag() : SEQ_NO が 空 です");
		return db.update_FAQmail_readFlag( te_aplec_key(),seq_no(),readFlag );
	}
	// faqFlag とタイトルのみアップデート
	public int	update_FAQmail_faqFlag(String faqFlag,String faqTitle){
		if(LOG.fa) LOG.outVector(rec,"class FAQmail #update_FAQmail_faqFlag() : データベースアップデート の入り口です");
		//
		if(isEmpty(TE_APLEC_KEY))	LOG.errStop("class FAQmail #update_FAQmail_faqFlag() : te_aplec_key が 空 です");
		if(isEmpty(SEQ_NO))			LOG.errStop("class FAQmail #update_FAQmail_faqFlag() : SEQ_NO が 空 です");
		return db.update_FAQmail_faqFlag( te_aplec_key(),seq_no(),faqFlag,faqTitle );
	}
	/////////////////////////////////////////////
	// faqFlag とタイトルと本文をアップデート
	/////////////////////////////////////////////
	public int	update_FAQmail_faq_set(String faqFlag,String faqTitle,String body){
		if(LOG.fa) LOG.outVector(rec,"class FAQmail #update_FAQmail_faq_set() : データベースアップデート の入り口です");
		//
		if(isEmpty(TE_APLEC_KEY))	LOG.errStop("class FAQmail #update_FAQmail_faqFlag() : te_aplec_key が 空 です");
		if(isEmpty(SEQ_NO))			LOG.errStop("class FAQmail #update_FAQmail_faqFlag() : SEQ_NO が 空 です");
		return db.update_FAQmail_faq_set( te_aplec_key(),seq_no(),faqFlag,faqTitle,body );
	}
	// FAQ フラグをアップデートする  ON/OFF
	public int	setFaqFLAG(String faqFlag){
		return db.update_FAQmail_faqFlagOnly( te_aplec_key(),seq_no(),faqFlag );
	}
	//
	// (4) 削除
	//
	public int	delete_FAQmail(){
		if(LOG.fa) LOG.outVector(rec,"class FAQmail #delete_FAQmail() : データベース削除 の入り口です");
		//
		if(isEmpty(TE_APLEC_KEY))	LOG.errStop("class FAQmail #update_FAQmail() : te_aplec_key が 空 です");
		if(isEmpty(SEQ_NO))			LOG.errStop("class FAQmail #update_FAQmail() : SEQ_NO が 空 です");
		return db.delete_FAQmail(rec);
	}
	//
	// フィールドの値が未設定がどうか
	//
	boolean isEmpty(int k){
		if(LOG.fa) LOG.outVector(rec,"class FAQmail #isEmpty() : フィールドの値が未設定がどうか の入り口です");
		//
		if(k<MIN)	LOG.errStop("class FAQmail #isEmpty() : フィールド番号が不正です / k =" + k);
		if(k>=MAX)	LOG.errStop("class FAQmail #isEmpty() : フィールド番号が不正です / k =" + k);
		String item	= (String)rec.get(k);
		if(item==null)			return true;
		if(item.equals("-")) 	return true;
		if(item.equals("")) 	return true;
		//
		return false;
	}
	//
	// 文字列の値が空白がnullでないか
	//
	boolean isEmptyString(String item){
		if(LOG.fa) LOG.println("class FAQmail #isEmptyString() : 文字列の値が空白がnullでないか の入り口です");
		//
		if((item==null)||(item.equals(""))){
			LOG.outVector(rec,"class FAQmail #isEmptyString() : 文字列の値が空白かnullでした");
		 	return true;
		}
		//
		return false;
	}
	void testPirnt(){
		LOG.outVector(rec,"レコードをプリントします");
	}
	//
	// ハッシュから変数を取り出す
	String strHash(Hashtable htb,String key){
		//
		String	str	= (String) htb.get(key);
		if(str==null){
			LOG.println("★★★★★★★★★★★★★★★★★★★★★★★★★★★");
			LOG.println("★★   　　　　　　　　　　　　　　　　　　　　   ★★");
			LOG.println("★★   ハッシュから取り出したデータは NULL です   ★★");
			LOG.println("★★   key = " + key );
			LOG.println("★★★★★★★★★★★★★★★★★★★★★★★★★★★");
		}
		return str;
	}
}