/*
 	課題提出履歴の処理をカプセル化する 

　　■課題提出履歴データベース
		
	/*
	create table kadai_000000 (
    	stNumber           VARCHAR(12)   NOT NULL,
    	te_aplec_key       CHAR(12)      NOT NULL,
    	kadai_key          CHAR(6)       NOT NULL,
    	shubetsu           CHAR(1)       NOT NULL,
    	saiten_flag        CHAR(1)       DEFAULT '0',
    	date_str           text          DEFAULT '',
		subject            text          DEFAULT '',
		points             VARCHAR(3)    DEFAULT ''
	);
	create index kadai_000000_idx on kadai_000000 (stNumber,te_aplec_key);	
*/
//
//import java.text.*;
package kadai;
import java.util.*;

import database.Database;
import framework.LOG;
//import java.io.*;
//import java.lang.Exception;

import	tktools.*;

public class KadaiInfo extends Object implements KadaiVar {
	//
	// 定数１
	public static int	STNUMBER			= 0;	// te_aplec_key   12 桁
	public static int	TE_APLEC_KEY		= 1;	// te_aplec_key   12 桁
	public static int	KADAI_KEY			= 2;	// 課題キー
	//
	public static int	SHUBETSU			= 3;	// 課題種別
	public static int	SAITEN_FLAG			= 4;	// 採点フラグ( 1: 未提出 2:作成中・未提出　3:提出済み  4:採点済み　5:エラー）
	public static int	DATE_STR			= 5;	// 提出日履歴CSV
	public static int	SUBJECT				= 6;	// 課題名
	public static int	POINTS				= 7;	// 得点
	//
	// 採点フラグ(saiten_flag)
	public	static String	NOTYET			= "1";	// 未提出
	public	static String	WORKING			= "2";	// 作成中・未提出
	public	static String	SUBMITTED		= "3";	// 提出済み
	public	static String	GRADED			= "4";	// 採点済み
	public	static String	HOLD			= "5";	// 試験の場合の受験中、この時subjectフィールドにはCSV形式の開始時間が入っている
	
	public	static	String	[]MSG			=	{"", "未提出", "作成中", "提出済み", "受領・採点済み", "HOLD状態"};
	
	// 反復回数制御
    public static int	MIN = STNUMBER;
    public static int	MAX = POINTS + 1;
	//
	//
	String	 szDB;
	Database db;
    Vector	rec;	// レコード本体
	//
	// ＤＢを引いて初期化するコンストラクタ
	//
	public KadaiInfo(String stNumber, String te_aplec_key, String kadai_key,String _szDB, Database _db){
		if(LOG.fa) LOG.println("class KadaiInfo #KadaiInfo() :  KadaiInfo のコンストラクタの入り口です");
		//
		szDB	= _szDB;
		db		= _db;
		rec		= new Vector (20,10);
		add_keys(stNumber, te_aplec_key, kadai_key);
		db.read_KadaiInfo(szDB,rec);
	}
	//
	// コンストラクタ
	// rec に値を入れて初期化してはならない！！
	//
	public KadaiInfo(String _szDB,Database _db){
		if(LOG.fa) LOG.println("class KadaiInfo #KadaiInfo() :  KadaiInfo のコンストラクタの入り口です");
		//
		szDB	= _szDB;
		db		= _db;
		rec		= new Vector (20,10);
		//
	}	
	/**
	 * キーでレコードを引いて結果をハッシュテーブルに返す
	 * @param stNumber
	 * @param te_aplec_key
	 * @param kadai_key
	 * @return
	 */
	public	void	getRecord( String stNumber, String te_aplec_key, String kadai_key, Hashtable ht){
		/*
		 * キーをセットしてDBを引く
		 */
		add_keys(stNumber, te_aplec_key, kadai_key);
		db.read_KadaiInfo(szDB,rec);
		/*
		 * 引数のハッシュテーブルにセットする
		 */
		ht.put(KI_SHUBETSU	, rec.get(SHUBETSU));
		ht.put(KI_SAITEN	, rec.get(SAITEN_FLAG));
		ht.put(KI_DATECSV	, rec.get(DATE_STR));
		ht.put(KI_SUBJECT	, rec.get(SHUBETSU));
		ht.put(KI_POINTS	, rec.get(POINTS));
		
		return;
	}
	public KadaiInfo(String	_szDB,
					  Database	_db,
					  String	_stNumber,
					  String	_te_aplec_key,
					  String	_kadai_key)
	{
		szDB	= _szDB;
		db		= _db;
		rec		= new Vector (20,10);
		add_keys(_stNumber, _te_aplec_key, _kadai_key);
	}
	/**
	 * デバッグ用
	 * @return		レコード本体
	 */
	public Vector getRec(){
		return rec;
	}
	//
	// ハッシュテーブルのデータでレコードを初期化する
	//
	//   あとで set　が使えるように全ての項目に値をセットする
	//   キー部分が null の場合は明らかなエラーとして、プログラムを停止する
	//
	void byHash(Hashtable htb){
		if(LOG.fa) LOG.outHash(htb,"class KadaiInfo #byHash() : ハッシュテーブルのデータでレコードを初期化する の入り口です");
		//
		for(int i=0; i<MAX; i++){
			rec.add(" ");
		}
		String stNumber		=	strHash(htb,"_stNumber");		if(isEmptyString(stNumber))		LOG.errStop("class KadaiInfo #byHash() : stNumber が null です．");
		String te_aplec_key	=	strHash(htb,"_te_aplec_key");	if(isEmptyString(te_aplec_key))	LOG.errStop("class KadaiInfo #byHash() : te_aplec_key が null です．");
		String kadai_key	=	strHash(htb,"_kadai_key");		if(isEmptyString(kadai_key))	LOG.errStop("class KadaiInfo #byHash() : kadai_key が null です．");
		
		String shubetsu		=	strHash(htb,"_shubetsu");		if(isEmptyString(shubetsu))	LOG.errStop("class KadaiInfo #byHash() : shubetsu が null です．");
		String saiten_flag	=	strHash(htb,"_saiten_flag");	if(isEmptyString(saiten_flag))	LOG.errStop("class KadaiInfo #byHash() : saiten_flag が null です．");
		String date_str		=	strHash(htb,"_date_str");		if(isEmptyString(date_str))	LOG.errStop("class KadaiInfo #byHash() : date_str が null です．");
		String subject		=	strHash(htb,"_subject");		if(isEmptyString(subject))	LOG.errStop("class KadaiInfo #byHash() : subject が null です．");
		String points		=	strHash(htb,"_points");			if(isEmptyString(points))	LOG.errStop("class KadaiInfo #byHash() : points が null です．");
		//
		set_stNumber(stNumber);
		set_te_aplec_key(te_aplec_key);
		set_kadai_key(kadai_key);
		//
		set_shubetsu(shubetsu);
		set_saiten_flag(saiten_flag);
		set_date_str(date_str);
		set_subject(subject);
		set_points(points);
		//
		if(LOG.fa) LOG.outVector(rec,"class KadaiInfo #byHash() : ハッシュテーブルのデータでレコードを初期化する の出口です");
	}
	//
	// キーをADDでセットする
	//
	public	void set_keys(String _stNumber,
	                      String _te_aplec_key,
	                      String _kadai_key){

		add_keys( _stNumber, _te_aplec_key,_kadai_key);
	}
	public	void add_keys(String _stNumber,
						  String _te_aplec_key,
						  String _kadai_key){

		if(LOG.fa) LOG.println("class KadaiInfo #add_keys() : ＮＯＴＥ/キーをADDでセットする の入り口です");
		// コンストラクタの直後に呼ぶこと
		// set でなくadd なのは、DB 検索で残りのデータが add されるから
		rec.add(_stNumber);
		rec.add(_te_aplec_key);
		rec.add(_kadai_key);
	}
	//
	// その他のデータをadd で追加する
	//
	public	void add_data(  String _shubetsu,
							String _saiten_flag,
							String _date_str,
							String _subject,
							String _points){
		if(LOG.fa) LOG.println("class KadaiInfo #add_data() : KadaiInfo/その他のデータをadd で追加する の入り口です");
		//
		rec.add(_shubetsu);
		rec.add(_saiten_flag);
		rec.add(_date_str);
		rec.add(_subject);
		rec.add(_points);
	}
	//
	// その他のデータをset で追加する
	//
	public	void set_data(  String _shubetsu,
							String _saiten_flag,
							String _date_str,
							String _subject,
							String _points){
		if(LOG.fa) LOG.println("class KadaiInfo #add_data() : KadaiInfo/その他のデータをadd で追加する の入り口です");
		//
		set_shubetsu(_shubetsu);
		set_saiten_flag(_saiten_flag);
		set_date_str(_date_str);
		set_subject(_subject);
		set_points(_points);
	}
	//
	// HOLDデータをセットする
	//
	public	void mkHOLD(String _shubetsu, String stamp){
		if(LOG.fa) LOG.println("class KadaiInfo #add_data() : KadaiInfo/その他のデータをadd で追加する の入り口です");
		//
		rec.add(_shubetsu);
		rec.add(HOLD);
		rec.add(stamp);
		rec.add("");
		rec.add("");
	}
	//
	//  特定のキーのレコードについて、得点を更新する
	//　採点レコードなので、レコードは必ず存在する
	//  採点済みへの変更も行う
	public	void	updateScore(String stNumber,
							    String te_aplec_key,
							    String kadai_key,
							    String score){
		if(LOG.fa) LOG.println("class KdaiInfo #updateScore() : ■ 特定のキーのレコードについて、得点を更新する の先頭です");
		/*
		 *  点数が空でなくかつ数値が記入されているとき採点済みとする
		 *  採点欄に ?? や 再提出 と書いても採点済みとはならない 
		 */
		if(!Gear.isEmptyDigit(score)){
			int	cnt	=	db.updateKadaiScore( szDB, stNumber, te_aplec_key, kadai_key, score.trim(),GRADED);
			if(LOG.fa) LOG.println("   set GRADED: cnt=" + cnt);
			
		}else{
			if(Gear.isEmpty(score))	{ score=""; }
			int	cnt	=	db.updateKadaiScore( szDB, stNumber, te_aplec_key, kadai_key, score,SUBMITTED);
			if(LOG.fa) LOG.println("   set SUBMITTED: cnt=" + cnt);
		}
	}
	//
	// 個々のフィールドに値をセットする
	//
	public void set_stNumber(String str)		{ rec.set(STNUMBER,str); 	}
	public void set_te_aplec_key(String str)	{ rec.set(TE_APLEC_KEY,str);}
	public void set_kadai_key(String str)		{ rec.set(KADAI_KEY,str); 	}
	//
	public void set_shubetsu(String str)		{ rec.set(SHUBETSU,str); 	}
	public void set_saiten_flag(String str)	{ rec.set(SAITEN_FLAG,str); }
	public void set_date_str(String str)		{ rec.set(DATE_STR,str); 	}
	public void set_subject(String str)		{ rec.set(SUBJECT,str); 	}
	public void set_points(String str)		{ rec.set(POINTS,str); 		}
	//
	// フィールドの値を得る
	//
	public String stNumber()		{	return (String)rec.get(STNUMBER); 		}
	public String te_aplec_key()	{	return (String)rec.get(TE_APLEC_KEY); 	}
	public String kadai_key()		{	return (String)rec.get(KADAI_KEY); 	}
	//
	public String shubetsu()		{	return (String)rec.get(SHUBETSU); 		}
	public String saiten_flag()		{	return (String)rec.get(SAITEN_FLAG); 	}
	public String date_str()		{	return (String)rec.get(DATE_STR); 		}
	public String subject()			{	return (String)rec.get(SUBJECT); 		}
	public String points()			{	return (String)rec.get(POINTS); 		}
	//
	// 最新の日付
	public String latest_date_str()	{
		String	str	= date_str();
		if((str==null)||(str.length()==0))	return	"";
		Csv		cs	= new	Csv(str);
		return	cs.get(0);
	}
	// 採点フラグを検査する
	public boolean	isNotyet()		{return saiten_flag().equals(NOTYET); }
	public boolean	isWorking()		{return saiten_flag().equals(WORKING); }
	public boolean	isSubmitted()	{return saiten_flag().equals(SUBMITTED); }
	public boolean	isGraded()		{return saiten_flag().equals(GRADED); }
	public boolean	isHOLD()		{return saiten_flag().equals(HOLD); }
	
	/** 特定の学生の特定の課題提出履歴がデータベースに存在するかどうか調べる */
	public static boolean isExist(Database db,String szDB,String te_aplec_key,String kadai_key,String stNumber){
		
		KadaiInfo 	kdf	=	new KadaiInfo(szDB,db);
		kdf.set_keys(stNumber,te_aplec_key,kadai_key);
		int 		cnt = kdf.read_KadaiInfo();
		if(cnt>0)	return	true;
		return false;
	}
	/** 
	 * 特定の学生の特定の課題提出履歴がデータベースに存在するかどうか調べるた上で
	 * さらに、その採点フラグがHOLDでないかどうか調べる
	 * @author kawaba
	 *
	 * この生成されたコメントの挿入されるテンプレートを変更するため
	 * ウィンドウ > 設定 > Java > コード生成 > コードとコメント
	 */
	
	public static boolean isHOLD(Database db,String szDB,String te_aplec_key,String kadai_key,String stNumber){
		
		KadaiInfo 	kdf	=	new KadaiInfo(szDB,db);
		kdf.set_keys(stNumber,te_aplec_key,kadai_key);
		int 		cnt = kdf.read_KadaiInfo();
		if(cnt==0)	return	false;
		return	kdf.isHOLD();
	}
	//
	// データベースへの処理
	//
	//  (全件)読み出し
	//
	public int	read_KadaiInfo_all(String stNumber,String te_aplec_key,Vector records){
		if(LOG.fa) LOG.outVector(rec,"class KadaiInfo #read_KadaiInfo_all() : (全件)読み出し の入り口です");
		//
		return db.read_KadaiInfo_all(szDB,stNumber,te_aplec_key,records);
	}
	//
	// データベースへの処理
	//
	// (1) 読み出し
	//
	public int	read_KadaiInfo(){
		if(LOG.fa) LOG.outVector(rec,"class KadaiInfo #read_KadaiInfo() : 読み出し の入り口です");
		//
		if(isEmpty(STNUMBER))			LOG.errStop("class KadaiInfo #read_KadaiInfo() : stNumber が 空 です");
		if(isEmpty(TE_APLEC_KEY))		LOG.errStop("class KadaiInfo #read_KadaiInfo() : te_aplec_key が 空 です");
		if(isEmpty(KADAI_KEY))			LOG.errStop("class KadaiInfo #read_KadaiInfo() : kadai_key が 空 です");
		return db.read_KadaiInfo(szDB,rec);
	}
	//
	// (2) 挿入
	//
	public int	insert_KadaiInfo(){
		if(LOG.fa) LOG.outVector(rec,"class KadaiInfo #insert_KadaiInfo() : kasaiInfo データベース挿入 の入り口です");
		//
		if(isEmpty(STNUMBER))			LOG.errStop("class KadaiInfo #insert_KadaiInfo() : stNumber が 空 です");
		if(isEmpty(TE_APLEC_KEY))		LOG.errStop("class KadaiInfo #insert_KadaiInfo() : te_aplec_key が 空 です");
		if(isEmpty(KADAI_KEY))			LOG.errStop("class KadaiInfo #read_KadaiInfo() : kadai_key が 空 です");
		return db.insert_KadaiInfo(szDB,rec);
	}
	//
	// (3) アップデート
	//
	public int	update_KadaiInfo(){
		if(LOG.fa) LOG.outVector(rec,"class KadaiInfo #update_KadaiInfo() : kasaiInfo データベースアップデート の入り口です");
		//
		if(isEmpty(STNUMBER))			LOG.errStop("class KadaiInfo #update_KadaiInfo() : stNumber が 空 です");
		if(isEmpty(TE_APLEC_KEY))		LOG.errStop("class KadaiInfo #update_KadaiInfo() : te_aplec_key が 空 です");
		if(isEmpty(KADAI_KEY))			LOG.errStop("class KadaiInfo #read_KadaiInfo() : kadai_key が 空 です");
		return db.update_KadaiInfo(szDB,rec);
	}
	//
	// (4) 削除
	//
	public int	delete_KadaiInfo(String stNumber,String	te_aplec_key){
		Vector	v	= new Vector(10,5);
		v.add(stNumber);
		v.add(te_aplec_key);
		return	db.delete_KadaiInfo(szDB,v);
	}
	//
	//
	public int	delete_KadaiInfo(){
		if(LOG.fa) LOG.outVector(rec,"class KadaiInfo #delete_KadaiInfo() : kasaiInfo データベース削除 の入り口です");
		//
		if(isEmpty(STNUMBER))			LOG.errStop("class KadaiInfo #update_KadaiInfo() : stNumber が 空 です");
		if(isEmpty(TE_APLEC_KEY))		LOG.errStop("class KadaiInfo #update_KadaiInfo() : te_aplec_key が 空 です");
		return db.delete_KadaiInfo(szDB,rec);
	}
	/** 特定の学生の特定の講義の特定の課題提出履を削除する */
	public int	delete_A_KadaiInfo(){
		if(LOG.fa) LOG.outVector(rec,"class KadaiInfo #delete_KadaiInfo() : kasaiInfo  の入り口です");
		//
		if(isEmpty(STNUMBER))			LOG.errStop("class KadaiInfo #update_KadaiInfo() : stNumber が 空 です");
		if(isEmpty(TE_APLEC_KEY))		LOG.errStop("class KadaiInfo #update_KadaiInfo() : te_aplec_key が 空 です");
		return db.delete_A_KadaiInfo(szDB,rec);
	}
	/** 課題キーを指定して、特定の学生の特定の講義の特定の課題提出履を削除する */
	public int	delete_KadaiInfo(String kadaiKey){
		if(LOG.fa) LOG.outVector(rec,"class KadaiInfo #delete_KadaiInfo() : kasaiInfo  の入り口です");
		//
		if(isEmpty(STNUMBER))			LOG.errStop("class KadaiInfo #update_KadaiInfo() : stNumber が 空 です");
		if(isEmpty(TE_APLEC_KEY))		LOG.errStop("class KadaiInfo #update_KadaiInfo() : te_aplec_key が 空 です");
		set_kadai_key(kadaiKey);
		return db.delete_A_KadaiInfo(szDB,rec);
	}
	//
	// フィールドの値が未設定がどうか
	//
	boolean isEmpty(int k){
		if(LOG.fa) LOG.outVector(rec,"class KadaiInfo #isEmpty() : kasaiInfo フィールドの値が未設定がどうか の入り口です");
		//
		if(k<MIN)	LOG.errStop("class KadaiInfo #isEmpty() : フィールド番号が不正です / k =" + k);
		if(k>=MAX)	LOG.errStop("class KadaiInfo #isEmpty() : フィールド番号が不正です / k =" + k);
		String item	= (String)rec.get(k);
		if(item==null)			return true;
		if(item.equals("")) 	return true;
		//
		return false;
	}
	//
	// 文字列の値が空白かnullでないか
	//
	boolean isEmptyString(String item){
		if(LOG.fa) LOG.outVector(rec,"class KadaiInfomail #isEmptyString() : 文字列が空白かnullでないか の入り口です");
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