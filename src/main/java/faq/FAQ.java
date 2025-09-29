/*
 	ＦＡＱレコードの処理をカプセル化する 
*
　　■ＦＡＱデータベース
		
		定義講義単位にまとめ、受けつけ番号順に並べる
		
		seq_no ---- 受付番号 ６桁 KeyGen で発生
		　　　　　　個人単位（講義単位ではない）
		
		内容はfaq_mailの変更に伴い連動して追加・削除・訂正される
		
	create table faq (
       te_lec_key       CHAR(12)       NOT NULL,
	   seq_no           VARCHAR(6)     NOT NULL,
       faq_title        VARCHAR(200)   DEFAULT '',
       faq_body         text           DEFAULT ''
	);
	create index faq_idx on faq (te_lec_key,seq_no);
*/
//
//import java.text.*;
package faq;
import java.util.*;

import database.Database;
import framework.LOG;

public class FAQ extends Object {
	//
	// 定数
	public static int	TE_LEC_KEY		= 0;	// te_lec_key   12 桁
	public static int	SEQ_NO			= 1;	// 固有番号
	
	public static int	FAQ_TITLE		= 2;	// FAQ登録件名
    public static int	FAQ_BODY		= 3;	// FAQ登録フラグ
	//
	// 反復回数制御
    public static int	MIN = TE_LEC_KEY;
    public static int	MAX = FAQ_BODY + 1;
	//
	//
	Database db;
    Vector	rec;	// レコード本体
	//
	// コンストラクタ
	// rec に値を入れて初期化してはならない！！
	//
	public FAQ(Database _db){
		if(LOG.fa) LOG.println("class FAQ #FAQ() :  のコンストラクタの入り口です");
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
	void byHash(Hashtable htb){
		if(LOG.fa) LOG.outHash(htb,"class FAQ #byHash() : ハッシュテーブルのデータでレコードを初期化する の入り口です");
		//
		for(int i=0; i<MAX; i++){
			rec.add("-");
		}
		String te_lec_key	=	strHash(htb,"_te_lec_key");		if(isEmptyString(te_lec_key))		LOG.errStop("class FAQ #byHash() : te_lec_key が null です．");
		String seq_no		=	strHash(htb,"_seq_no");			if(isEmptyString(seq_no))			LOG.errStop("class FAQ #byHash() : seq_no が null です．");
		String faq_title	=	strHash(htb,"_faq_title");		if(isEmptyString(faq_title))		faq_title		= "-";
    	String faq_body		=	strHash(htb,"_faq_body");		if(isEmptyString(faq_body))		faq_body		= "-";
		//
		set_te_lec_key(te_lec_key);
		set_seq_no(seq_no);
		set_faq_title(faq_title);
		set_faq_body(faq_body);
		//
		if(LOG.fa) LOG.outVector(rec,"class FAQ #byHash() : ハッシュテーブルのデータでレコードを初期化する の出口です");
	}
	//
	// キーをADDでセットする
	//
	public	void set_keys(String _set_te_lec_key,String _set_seq_no){
		add_keys( _set_te_lec_key, _set_seq_no);
	}
	public	void add_keys(String _set_te_lec_key,String _set_seq_no){
		if(LOG.fa) LOG.println("class FAQ #add_keys() : ＦＡＱ/キーをADDでセットする の入り口です");
		// コンストラクタの直後に呼ぶこと
		// set でなくadd なのは、DB 検索で残りのデータが add されるから
		rec.add(_set_te_lec_key);
		rec.add(_set_seq_no);
	}
	//
	// その他のデータをadd で追加する
	//
	public	void add_data(String _faq_title,String _faq_body){
		if(LOG.fa) LOG.println("class FAQ #add_data() : ＦＡＱ/その他のデータをadd で追加する の入り口です");
		//
		rec.add(_faq_title);
		rec.add(_faq_body);
	}
	
	//
	// 個々のフィールドに値をセットする
	//
	public void set_te_lec_key(String str)		{ rec.set(TE_LEC_KEY,str);}
	public void set_seq_no(String str)			{ rec.set(SEQ_NO,str); 		}
	public void set_faq_title(String str)		{ rec.set(FAQ_TITLE,str); 	}
	public void set_faq_body(String str)		{ rec.set(FAQ_BODY,str); 	}
	//
	// ここのフィールドの値を得る
	//
	public String te_lec_key()		{	return (String)rec.get(TE_LEC_KEY); 	}
	public String seq_no()			{	return (String)rec.get(SEQ_NO); 		}
	//
	public String faq_title()		{
		if(rec.size() > FAQ_TITLE){
			return (String)rec.get(FAQ_TITLE);
		}
		return "";
	}
	//
	public String faq_body(){
		if(rec.size() > FAQ_BODY){
			return (String)rec.get(FAQ_BODY);
		}
		return "";
	}
	//
	// データベースへの処理
	//
	// (1) 読み出し
	//
	int	read_FAQ(){
		if(LOG.fa) LOG.outVector(rec,"class FAQ #read_FAQ() : ＦＡＱデータベース読み出し の入り口です");
		//
		if(isEmpty(TE_LEC_KEY))		LOG.errStop("class FAQ #read_FAQ() : te_lec_key が 空 です");
		if(isEmpty(SEQ_NO))			LOG.errStop("class FAQ #read_FAQ() : SEQ_NO が 空 です");
		return db.read_FAQ(rec);
	}
	//
	// (2) 挿入
	//
	int	insert_FAQ(){
		if(LOG.fa) LOG.outVector(rec,"class FAQ #insert_FAQ() : ＦＡＱデータベース挿入 の入り口です");
		//
		if(isEmpty(TE_LEC_KEY))	LOG.errStop("class FAQ #insert_FAQ() : TE_LEC_KEY が 空 です");
		if(isEmpty(SEQ_NO))			LOG.errStop("class FAQ #insert_FAQ() : SEQ_NO が 空 です");
		return db.insert_FAQ(rec);
	}
	//
	// (3) アップデート
	//
	int	update_FAQ(){
		if(LOG.fa) LOG.outVector(rec,"class FAQ #update_FAQ() : ＦＡＱデータベースアップデート の入り口です");
		//
		if(isEmpty(TE_LEC_KEY))	LOG.errStop("class FAQ #update_FAQ() : te_lec_key が 空 です");
		if(isEmpty(SEQ_NO))			LOG.errStop("class FAQ #update_FAQ() : SEQ_NO が 空 です");
		return db.update_FAQ(rec);
	}
	//
	// (4) 削除
	//
	int	delete_FAQ(){
		if(LOG.fa) LOG.outVector(rec,"class FAQ #delete_FAQ() : ＦＡＱデータベース削除 の入り口です");
		//
		if(isEmpty(TE_LEC_KEY))	LOG.errStop("class FAQ #update_FAQ() : te_lec_key が 空 です");
		if(isEmpty(SEQ_NO))			LOG.errStop("class FAQ #update_FAQ() : SEQ_NO が 空 です");
		return db.delete_FAQ(rec);
	}
	//
	// フィールドの値が未設定がどうか
	//
	boolean isEmpty(int k){
		if(LOG.fa) LOG.outVector(rec,"class FAQ #isEmpty() : ＦＡＱフィールドの値が未設定がどうか の入り口です");
		//
		if(k<MIN)	LOG.errStop("class FAQ #isEmpty() : フィールド番号が不正です / k =" + k);
		if(k>=MAX)	LOG.errStop("class FAQ #isEmpty() : フィールド番号が不正です / k =" + k);
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
		if(LOG.fa) LOG.outVector(rec,"class FAQmail #isEmptyString() : ＦＡＱ文字列の値が空白がnullでないか の入り口です");
		//
		if(item==null)			return true;
		if(item.equals("")) 	return true;
		//
		return false;
	}	//
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