/*
 	ＮＯＴＥレコードの処理をカプセル化する 
*
　　■ＮＯＴＥデータベース
		
		create table note_000000 (
    		    stNumber     	 VARCHAR(12)   NOT NULL,
       			te_aplec_key     CHAR(12)      NOT NULL,
                sect_key         CHAR(5)       NOT NULL,
                note             text          DEFAULT ''
        );
		
     create index note_000000_idx on note_000000 (stNumber,te_aplec_key);
		



*/
//
//import java.text.*;
package note;
import java.util.*;

import database.Database;
import framework.LOG;

public class NOTE extends Object {
	//
	// 定数
	public static int	STNUMBER			= 0;	// te_aplec_key   12 桁
	public static int	TE_APLEC_KEY		= 1;	// te_aplec_key   12 桁
	public static int	SECT_KEY			= 2;	// セクションキー
	public static int	NOTES				= 3;	// NOTE
	//
	// 反復回数制御
    public static int	MIN = STNUMBER;
    public static int	MAX = NOTES + 1;
	//
	//
	String	 szDB;
	Database db;
    Vector	rec;	// レコード本体
	//
	// コンストラクタ
	// rec に値を入れて初期化してはならない！！
	//
	public NOTE(String _szDB,Database _db){
		if(LOG.fa) LOG.println("class NOTE #NOTE() :  NOTE のコンストラクタの入り口です");
		//
		szDB	= _szDB;
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
		if(LOG.fa) LOG.outHash(htb,"class NOTE #byHash() : ハッシュテーブルのデータでレコードを初期化する の入り口です");
		//
		for(int i=0; i<MAX; i++){
			rec.add(" ");
		}
		String stNumber		=	strHash(htb,"_stNumber");		if(isEmptyString(stNumber))		LOG.errStop("class NOTE #byHash() : stNumber が null です．");
		String te_aplec_key	=	strHash(htb,"_te_aplec_key");	if(isEmptyString(te_aplec_key))	LOG.errStop("class NOTE #byHash() : te_aplec_key が null です．");
		String sect_key		=	strHash(htb,"_sect_key");		if(isEmptyString(sect_key))		LOG.errStop("class NOTE #byHash() : sect_key が null です．");
		String note			=	strHash(htb,"_note");			if(isEmptyString(note))			note	= "";
		//
		set_stNumber(stNumber);
		set_te_aplec_key(te_aplec_key);
		set_sect_key(sect_key);
		set_note(note);
		//
		if(LOG.fa) LOG.outVector(rec,"class NOTE #byHash() : ハッシュテーブルのデータでレコードを初期化する の出口です");
	}
	/**
	 * 全ての値を受け取ってレコードを初期化する
	 * 
	 * @param stNumber
	 * @param te_aplec_key
	 * @param sect_key
	 * @param note
	 */
	public void fill(String stNumber, String te_aplec_key, String sect_key, String note){

		for(int i=0; i<MAX; i++){
			rec.add(" ");
		}
		set_stNumber(stNumber);
		set_te_aplec_key(te_aplec_key);
		set_sect_key(sect_key);
		set_note(note);
	}	
	//
	// キーをADDでセットする
	//
	public	void set_keys(String _stNumber,String _te_aplec_key,String _sect_key){
		add_keys( _stNumber, _te_aplec_key,_sect_key);
	}
	public	void add_keys(String _stNumber,String _te_aplec_key,String _sect_key){
		if(LOG.fa) LOG.println("class NOTE #add_keys() : ＮＯＴＥ/キーをADDでセットする の入り口です");
		// コンストラクタの直後に呼ぶこと
		// set でなくadd なのは、DB 検索で残りのデータが add されるから
		rec.add(_stNumber);
		rec.add(_te_aplec_key);
		rec.add(_sect_key);
	}
	//
	// その他のデータをadd で追加する
	//
	public	void add_data(String _note){
		if(LOG.fa) LOG.println("class NOTE #add_data() : NOTE/その他のデータをadd で追加する の入り口です");
		//
		rec.add(_note);
	}
	
	//
	// 個々のフィールドに値をセットする
	//
	public void set_stNumber(String str)		{ rec.set(STNUMBER,str); 	}
	public void set_te_aplec_key(String str)	{ rec.set(TE_APLEC_KEY,str);}
	public void set_sect_key(String str)		{ rec.set(SECT_KEY,str); 	}
	public void set_note(String str)			{ rec.set(NOTES,str); 		}
	//
	// フィールドの値を得る
	//
	public String stNumber()		{	return (String)rec.get(STNUMBER); 		}
	public String te_aplec_key()	{	return (String)rec.get(TE_APLEC_KEY); 	}
	public String sect_key()		{	return (String)rec.get(SECT_KEY); 		}
	public String note()			{	return (String)rec.get(NOTES); 			}
	//
	// データベースへの処理
	//
	//  (全件)読み出し
	//
	public int	read_NOTE_all(String stNumber,String te_aplec_key,Vector records){
		if(LOG.fa) LOG.outVector(rec,"class NOTE #read_NOTE() : ＮＯＴＥデータベース読み出し の入り口です");
		//
		return db.read_NOTE_all(szDB,stNumber,te_aplec_key,records);
	}
	//
	// データベースへの処理
	//
	// (1) 読み出し
	//
	public int	read_NOTE(){
		if(LOG.fa) LOG.outVector(rec,"class NOTE #read_NOTE() : ＮＯＴＥデータベース読み出し の入り口です");
		//
		if(isEmpty(STNUMBER))			LOG.errStop("class NOTE #read_NOTE() : stNumber が 空 です");
		
		if(isEmpty(TE_APLEC_KEY))		LOG.errStop("class NOTE #read_NOTE() : te_aplec_key が 空 です");
		return db.read_NOTE(szDB,rec);
	}
	//
	// (2) 挿入
	//
	public int	insert_NOTE(){
		if(LOG.fa) LOG.outVector(rec,"class NOTE #insert_NOTE() : ＮＯＴＥデータベース挿入 の入り口です");
		//
		if(isEmpty(STNUMBER))			LOG.errStop("class NOTE #insert_NOTE() : stNumber が 空 です");
		if(isEmpty(TE_APLEC_KEY))		LOG.errStop("class NOTE #insert_NOTE() : te_aplec_key が 空 です");
		return db.insert_NOTE(szDB,rec);
	}
	//
	// (3) アップデート
	//
	public int	update_NOTE(){
		if(LOG.fa) LOG.outVector(rec,"class NOTE #update_NOTE() : ＮＯＴＥデータベースアップデート の入り口です");
		//
		if(isEmpty(STNUMBER))			LOG.errStop("class NOTE #update_NOTE() : stNumber が 空 です");
		if(isEmpty(TE_APLEC_KEY))		LOG.errStop("class NOTE #update_NOTE() : te_aplec_key が 空 です");
		return db.update_NOTE(szDB,rec);
	}
	//
	// (4) 削除
	//
	public int	delete_NOTE(String stNumber,String	te_aplec_key){
		Vector	v	= new Vector(10,5);
		v.add(stNumber);
		v.add(te_aplec_key);
		return	db.delete_NOTE(szDB,v);
	}
	//
	//
	public int	delete_NOTE(){
		if(LOG.fa) LOG.outVector(rec,"class NOTE #delete_NOTE() : ＮＯＴＥデータベース削除 の入り口です");
		//
		if(isEmpty(STNUMBER))			LOG.errStop("class NOTE #update_NOTE() : stNumber が 空 です");
		if(isEmpty(TE_APLEC_KEY))		LOG.errStop("class NOTE #update_NOTE() : te_aplec_key が 空 です");
		return db.delete_NOTE(szDB,rec);
	}
	//
	// フィールドの値が未設定がどうか
	//
	boolean isEmpty(int k){
		if(LOG.fa) LOG.outVector(rec,"class NOTE #isEmpty() : ＮＯＴＥフィールドの値が未設定がどうか の入り口です");
		//
		if(k<MIN)	LOG.errStop("class NOTE #isEmpty() : フィールド番号が不正です / k =" + k);
		if(k>=MAX)	LOG.errStop("class NOTE #isEmpty() : フィールド番号が不正です / k =" + k);
		String item	= (String)rec.get(k);
		if(item==null)			return true;
		//if(item.equals("-")) 	return true;
		if(item.equals("")) 	return true;
		//
		return false;
	}
	//
	// 文字列の値が空白かnullでないか
	//
	boolean isEmptyString(String item){
		if(LOG.fa) LOG.outVector(rec,"class NOTEmail #isEmptyString() : ＮＯＴＥ文字列の値が空白がnullでないか の入り口です");
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