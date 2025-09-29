/*
  課題実施レコードの処理をカプセル化する 
*/
package kadai;
import java.text.*;
import java.util.*;

import database.Database;
import framework.LOG;
import tktools.*;
/*
■app_kadai テーブルは、時間割に割り付けた講義の課題についての補足データ

  te_oplec_key で一覧リストを得ることができるが、
  直接キーでの  検索を行う処理が中心になる
  
  saiten_flag  0 = 未採点
               1 = 採点済み


///////////////////////////////////////////////////////////////////////////////////////
  <<< 2002.8.4 変更 >>>

create table app_kadai (
     te_aplec_key       CHAR(12)  NOT NULL,
     kadai_key          CHAR(6)   NOT NULL,

     saiten_flag        CHAR(1),	---------------------  0/1　未使用

     s_yyyy             CHAR(4),
     s_month            CHAR(2),
     s_day              CHAR(2),
     s_hour             CHAR(2),
     s_minute           CHAR(2),

     e_yyyy             CHAR(4),
     e_month            CHAR(2),
     e_day              CHAR(2),
     e_hour             CHAR(2),
     e_minute           CHAR(2),
     
     passwd				VARCHAR(20)
);

// キーは複数列に設定する
create index app_kadai_idx on app_kadai (te_aplec_key,kadai_key);

*/
public class KadaiApRecord extends Object{
	//
	// 定数
	public static int	TE_APLEC_KEY		= 0;	// te_aplec_key   18 桁
    public static int	KADAI_KEY	    	= 1;	// 課題キー      6 桁

	public static int	SAITEN_FLAG	    	= 2;	// 採点が終わったかどうか(未使用)

	public static int	S_YYYY				= 3;	// 開始年月日時分
    public static int	S_MONTH				= 4;	// 月
    public static int	S_DAY				= 5;	// 日
    public static int	S_HOUR				= 6;	// 時
    public static int	S_MINUTE			= 7;	// 分

	public static int	E_YYYY				= 8;	// 終了年
    public static int	E_MONTH				= 9;	// 月
    public static int	E_DAY				= 10;	// 日
    public static int	E_HOUR				= 11;	// 時
    public static int	E_MINUTE			= 12;	// 分

	public static int	PASSWD				= 13;	// 試験開始パスワード
	
	// 採点フラグの値
	public static String  FLAG_OFF	= "0";
	public static String  FLAG_ON		= "1";
	//
    public static int	MIN = TE_APLEC_KEY;
    public static int	MAX = PASSWD + 1;
	
	boolean	empty;	// 空のレコードであることを示すフラグ．空の時 true
	
	Vector rec;
	//
	///////////////////////////////////////////////////
	//
	//   コンストラクタ
	//
	///////////////////////////////////////////////////
	//
	// 空のレコードを生成する（完全にカラ）
	public KadaiApRecord(){
		//
		empty	= true;
		Vector w = new Vector(20,10);
		rec	= copy(w);
	}	//
	//--------------------------------------------------------------------
	//  空のレコードを作成する（初期値で埋める）
	//
	public KadaiApRecord(String _te_aplec_key,String _kadai_key){
		//
		empty	= true;
		rec = new Vector(20,10);
		// なにかセットしていないと set メソッドで書き換えられない
		for(int i=0; i<=MAX; i++){
			rec.add(" ");
		}
		mkEmptyRecord( _te_aplec_key, _kadai_key);// 値をセットして書き換え
		//
	}
	//----------------------------------------------------------------------
	//
	// Vector から生成する（レコード中に null があれば "" にする）
	public KadaiApRecord(Vector w){
		if(LOG.fa) LOG.outVector(w,"KadaiApRecord: コンストラクタで受け取ったベクターです");
		//
		empty	= false;
		rec	= copy(w);
		if(LOG.fa) LOG.outVector(rec,"KadaiApRecord: コンストラクタで課題実施レコードを生成しました");
	}// 
	// キーでデータベースを検索して生成する
	public KadaiApRecord(String _te_aplec_key,String _kadai_key,Database db){
		if(LOG.fa) LOG.println("class KadaiApRecord のコンストラクタ/ _te_aplec_key =" + _te_aplec_key + ", _kadai_key= " + _kadai_key);
		//
		rec	= new Vector (20,10);
		for(int i=MIN; i<=MAX; i++){
			rec.add(i,"");
		}
		Vector 	w 		= new Vector (20,10);
		int	counts 	= 0;
		
		empty = false;
		if(_te_aplec_key != null){
			counts = db.getKadaiAp(_te_aplec_key, _kadai_key, w ); // null は帰ってこない
			if(counts > 0){
					rec	 = copy(w);// null をコピーしないため
			}else{
				empty	= true;
				rec 	= new Vector(20,10);
				mkEmptyRecord( _te_aplec_key, _kadai_key);// 初期値にしたレコードを作る
			}
		}
		// デバッグ用
		if(LOG.fa) { 
			if( isEmptyRecord() ){
				LOG.println("class KadaiApRecord のコンストラクタ/ データベースに該当がありません");
			}else{
				LOG.outVector(rec,"class KadaiApRecord のコンストラクタ/ データベースから取得したレコード");
			}
		}
	}
	//
	//------------------------------------------------------------------------------
	//  全ての要素を "" に初期化したのち、null でないものだけをコピーする
	//  コピーは、レコードサイズを超えて行われることはない
	Vector copy(Vector w){
		//
		Vector v = new Vector( 20,10 );
		for(int i=MIN; i<=MAX; i++){	// "" に初期化しておく
			v.add(i,"");
		}
		// w をコピーする
		// null の要素は コピーしない
		//
		for(int i=0; ( (i<w.size())&&(i<=MAX) ); i++){
			if(w.get(i)!=null){	v.set(i,(String)w.get(i)); }
		}
		return v;
	}	
	///////////////////////////////////////////////////
	//
	//　　データベースの更新
	//
	///////////////////////////////////////////////////
	//
	// 挿入
	public int insert(Database db){
		int    n = db.insertKadaiAp( rec ); 
		return n;
	}
	// 更新
	public int update(Database db){
		int    n = db.updateKadaiAp( rec ); 
		return n;
	}
	// 削除
	public int delete(Database db){
		int    n = db.deleteKadaiAp( te_aplec_key(),kadai_key() );
		return n;
	}
	//
	///////////////////////////////////////////////////
	//
	//　　レコードの値をセットする
	//
	///////////////////////////////////////////////////
	//
	//  空のレコードを作る
	public void mkEmptyRecord(String _te_aplec_key,String _kadai_key){
		for(int i=0; i<MAX; i++){
			rec.add("");
		}
		//
		set_te_aplec_key(_te_aplec_key);
		set_kadai_key(_kadai_key);
		//
		set_saiten_flag(FLAG_OFF);
		//
		set_s_yyyy(getDate("yyyy"));
		set_s_month(getDate("MM"));
		set_s_day(getDate("dd"));
		set_s_hour(getDate("HH"));
		set_s_minute(getDate("mm"));
		//
		set_e_yyyy(getDate("yyyy"));
		set_e_month(getDate("MM"));
		set_e_day(getDate("dd"));
		set_e_hour(getDate("HH"));
		set_e_minute(getDate("mm"));
		//
		set_passwd("");	// デフォルトでは "" をセットする
	}
	// ハッシュにデータを返す
	public void setToHash(Hashtable htb){
		//
		htb.put("_saiten_flag",saiten_flag());
		//
		htb.put("_s_yyyy"	,s_yyyy()	);
		htb.put("_s_month"	,s_month()	);
		htb.put("_s_day"	,s_day()	);
		htb.put("_s_hour"	,s_hour()	);
		htb.put("_s_minute" ,s_minute()	);
		//
		htb.put("_e_yyyy"	,e_yyyy()	);
		htb.put("_e_month"	,e_month()	);
		htb.put("_e_day"	,e_day()	);
		htb.put("_e_hour"	,e_hour()	);
		htb.put("_e_minute" ,e_minute()	);
		//
		htb.put("_passwd" 	,passwd()	);
	}	
	//
	// ハッシュのデータで内容を埋める
	public void setFromHash(Hashtable htb){
		
		String _saiten_flag	= (String)htb.get("_saiten_flag");
		if(isEmpty(_saiten_flag))	_saiten_flag = FLAG_OFF;	// あぷないので念のため
		//
		String _s_yyyy		= (String)htb.get("_s_yyyy");
		String _s_month		= (String)htb.get("_s_month");
		String _s_day		= (String)htb.get("_s_day");
		String _s_hour		= (String)htb.get("_s_hour");
		String _s_minute	= (String)htb.get("_s_minute");
		
		String _e_yyyy		= (String)htb.get("_e_yyyy");
		String _e_month		= (String)htb.get("_e_month");
		String _e_day		= (String)htb.get("_e_day");
		String _e_hour		= (String)htb.get("_e_hour");
		String _e_minute	= (String)htb.get("_e_minute");

		String _passwd		= Gear.strHash(htb, "_passwd");
		//
		set_saiten_flag(_saiten_flag);
		//
		set_s_yyyy(_s_yyyy);
		set_s_month(_s_month);
		set_s_day(_s_day);
		set_s_hour(_s_hour);
		set_s_minute(_s_minute);
		//
		set_e_yyyy(_e_yyyy);
		set_e_month(_e_month);
		set_e_day(_e_day);
		set_e_hour(_e_hour);
		set_e_minute(_e_minute);
		//
		set_passwd(_passwd);
	}
	//
	// 値をセットする
	public void set_te_aplec_key(String str)	{ rec.set(TE_APLEC_KEY,str);}
	public void set_kadai_key(String str)		{ rec.set(KADAI_KEY,str); 	}
	//
	public void set_saiten_flag(String str)	{ rec.set(SAITEN_FLAG,str); }
	
	public void set_s_yyyy(String str)			{ rec.set(S_YYYY,str); 	}
	public void set_s_month(String str)		{ rec.set(S_MONTH,str); }
	public void set_s_day(String str)			{ rec.set(S_DAY,str); 	}
	public void set_s_hour(String str)			{ rec.set(S_HOUR,str); 	}
	public void set_s_minute(String str)		{ rec.set(S_MINUTE,str);}
	
	public void set_e_yyyy(String str)			{ rec.set(E_YYYY,str); 	}
	public void set_e_month(String str)		{ rec.set(E_MONTH,str); }
	public void set_e_day(String str)			{ rec.set(E_DAY,str); 	}
	public void set_e_hour(String str)			{ rec.set(E_HOUR,str); 	}
	public void set_e_minute(String str)		{ rec.set(E_MINUTE,str);}

	public void set_passwd(String str)			{ rec.set(PASSWD,str);}
	//
	///////////////////////////////////////////////////
	//
	//　　レコードの値を返す
	//
	///////////////////////////////////////////////////
	//
	// 課題のフルキーを返す
	public String te_aplec_kadai_key()	{	return  te_aplec_key() + "-" + kadai_key(); }
	//
	public String te_aplec_key()		{	return (String)rec.get(TE_APLEC_KEY); 		}
	public String kadai_key()			{	return (String)rec.get(KADAI_KEY); 			}
	public String saiten_flag()			{	return (String)rec.get(SAITEN_FLAG); 		}
	public String s_yyyy()				{	return (String)rec.get(S_YYYY); 			}
	public String s_month()				{	return (String)rec.get(S_MONTH); 			}
	public String s_day()				{	return (String)rec.get(S_DAY); 				}
	public String s_hour()				{	return (String)rec.get(S_HOUR); 			}
	public String s_minute()			{	return (String)rec.get(S_MINUTE); 			}
	public String e_yyyy()				{	return (String)rec.get(E_YYYY); 			}
	public String e_month()				{	return (String)rec.get(E_MONTH); 			}
	public String e_day()				{	return (String)rec.get(E_DAY); 				}
	public String e_hour()				{	return (String)rec.get(E_HOUR); 			}
	public String e_minute()			{	return (String)rec.get(E_MINUTE); 			}
	public String passwd()				{	return (String)rec.get(PASSWD); 			}
	//
	//
	public void DBG_print(){
		DBG_print("");
	}
	//
	public void DBG_print(String msg){
		LOG.println(msg);
		//
		LOG.println("     te_aplec_key	=" + te_aplec_key() );
		LOG.println("     kadai_key		=" + kadai_key() );
		LOG.println("     saiten_flag		=" + saiten_flag() );
		LOG.println("     s_yyyy			=" + s_yyyy() );
		LOG.println("     s_month			=" + s_month() );
		LOG.println("     s_day			=" + s_day() );
		LOG.println("     s_hour			=" + s_hour() );
		LOG.println("     s_minute		=" + s_minute() );
		LOG.println("     e_yyyy			=" + e_yyyy() );
		LOG.println("     e_month			=" + e_month() );
		LOG.println("     e_day			=" + e_day() );
		LOG.println("     e_hour			=" + e_hour() );
		LOG.println("     e_minute		=" + e_minute() );
		LOG.println("     passwd			=" + passwd() );
	}
	// パスワードがマッチするかどうか
	public	boolean	isMatch(String pw){
		if(isEmpty(pw))			return	false;
		if(pw.equals(passwd()))	return	true;
		return	false;
	}
	//
	// 採点処理済みかどうか
	public boolean isMarked()    { return ( saiten_flag().equals(FLAG_ON)  ?  true : false ); }
	//
	//  レコードは空かどうか
	//
	public boolean isEmptyRecord() {
		if(LOG.fa) LOG.println("enpty is " + empty);
		return empty;
	}
	// 文字列が空かどうかテストする
	//
	boolean isEmpty(String str){
		if(str==null) 			return  true;
		if(str.length()==0)	return  true;
		return false;
	}
	//
	// データベースを読んで結果を返す
	//
	public static boolean	isExist(String _te_aplec_key,String _kadai_key,Database db){
		if(LOG.fa) LOG.println("KadaiApRecord クラス: isExist() の先頭です");
		//
		Vector w 	= new Vector(20,10);
		int counts 	= 0;
		counts = db.getKadaiAp(_te_aplec_key, _kadai_key,w);
		if((counts == 0)){
			if(LOG.fa) LOG.println("KadaiApRecord クラス: readRecord() の実行結果：データは存在しません ／te_aplec_key=" + _te_aplec_key + " ,kadai_key=" + _kadai_key );
			return false;
		}
		if(LOG.fa) LOG.println("KadaiApRecord クラス: readRecord() の実行結果：データは存在しています ／te_aplec_key=" + _te_aplec_key + " ,kadai_key=" + _kadai_key );
		return true;
	}
	/*
	public boolean isEmpty(){
		if((rec==null)||(rec.size()==0))	return true;
		return	false;
	}
	*/
    public String getStartDate()  {
        return s_yyyy() + s_month() + s_day() + s_hour() + s_minute();
    }
    public String getEndDate()  {
        return e_yyyy() + e_month() + e_day() + e_hour() + e_minute();
    }
    public String getStartDate3()  {
        return s_yyyy() + "/" + s_month() + "/" + s_day() + "/" + s_hour() + "/" + s_minute() + " ";
    }
    public String getEndDate3()  {
        return e_yyyy() + "/" + e_month() + "/" + e_day() + "/" + e_hour() + "/" + e_minute() + " ";
    }
    public String getStartDate2()  {
        return s_month() + "/" + s_day() + " " + s_hour() + ":" + s_minute() + " ";
    }
    public String getEndDate2()  {
        return e_month() + "/" + e_day() + " " + e_hour() + ":" + e_minute() + " ";
    }	    
    //現在の日付の文字列を得る
    public String getDate(){
        return getDate("yyyy年MM月dd日HH時mm分ss秒");
    }
    // yyyyMMddHH などを指定する
    public String getDate(String form){
        SimpleDateFormat format = new SimpleDateFormat(form);
        String strDate = format.format(Calendar.getInstance().getTime());
        return strDate;
    }
    // 課題の提出期間と現在の日時を比較して範囲内かどうか判定する
    public boolean isWithin(){
        String start   = getStartDate(); // 分まで比較
        String end     = getEndDate();
        String current = getDate("yyyyMMddHHmm");
        //System.out.println("start:" + start + "  " + "current:" + current + "  " + "end:" + end);
        //
        return ( (start.compareTo(current) <= 0 ) && ( current.compareTo(end) <= 0) );
    }
    // 課題の提出開始日と現在の日時を比較して解答可能かどうか判定する
    public boolean isStarted(){
        String start   = getStartDate(); // 分まで比較
        String end     = getEndDate();
        String current = getDate("yyyyMMddHHmm");
        //System.out.println("start:" + start + "  " + "current:" + current + "  " + "end:" + end);
        //
        return ( start.compareTo(current) <= 0 );	// すでに期間が始まっているとき  TRUE
    }
    // 課題の提出期間と現在の日時を比較して範囲内かどうか判定する
    public boolean isOver(){
        String start   = getStartDate(); // 分まで比較
        String end     = getEndDate();
        String current = getDate("yyyyMMddHHmm");
        if(LOG.fa){
            LOG.println("■KadaiApRecird #isOver()");
            LOG.println("　start:" + start + "  " + "current:" + current + "  " + "end:" + end);
        }
        //
        return ( current.compareTo(end) > 0 );  // すでに期限を過ぎているとき true
    }
    //
    // 課題の提出期限と現在の日時を比較して提出可能かどうか判定する
    public boolean doesItDo(){
        String end     = getEndDate();
        String current = getDate("yyyyMMddHHmm");
        //System.out.println("current:" + current + "  " + "end:" + end);
        //
        return (current.compareTo(end) <= 0);
    }
    // 課題の提出期限と現在の日時を比較して期限到来かどうか判定する
    // すでにマークされていれば，処理済として無視する
    public boolean doesItOver(){
        String end     = getEndDate();
        String current = getDate("yyyyMMddHHmm");
        return ( (current.compareTo(end) > 0) && ( !isMarked() )  );
    }

}