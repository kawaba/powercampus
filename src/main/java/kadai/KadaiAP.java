/*
  課題定義データの処理をカプセル化する 
*/
//import java.text.*;
package kadai;
import java.util.*;

import database.Database;
//import java.lang.Exception;
/*

■app_kadai テーブルは、時間割に割り付けた講義の課題についての補足データ

  te_oplec_sect_key で一覧リストを得ることができるが、
  直接キーでの  検索を行う処理が中心になる
  
  saiten_flag  0 = 未採点
               1 = 採点済み


///////////////////////////////////////////////////////////////////////////////////////
  <<< 2002.8.4 変更 >>>

create table app_kadai (
     te_aplec_key       CHAR(12)  NOT NULL,
     kadai_key          CHAR(6)   NOT NULL,
     saiten_flag        CHAR(1),
     s_yyyy             CHAR(4),
     s_month            CHAR(2),
     s_day              CHAR(2),
     s_hour             CHAR(2),
     s_minute           CHAR(2),
     e_yyyy             CHAR(4),
     e_month            CHAR(2),
     e_day              CHAR(2),
     e_hour             CHAR(2),
     e_minute           CHAR(2)
);

// キーは複数列に設定する
create index app_kadai_idx on app_kadai (te_aplec_key,kadai_key);
*/
//
public class KadaiAP extends Object{
    //
	public static int	TE_APLEC_KEY		= 0;	// te_lec_key   18 桁
    public static int	KADAI_KEY	    	= 1;	// 課題キー      6 桁
	public static int	SAITEN_FLAG	    	= 2;	// 表示順序番号
	public static int	S_YYYY				= 3;	// ファイル提出の有無
    public static int	S_MONTH				= 4;	// 課題のタイトル
    public static int	S_DAY				= 5;	// 内容
    public static int	S_HOUR				= 6;	// 内容
    public static int	S_MINUTE			= 7;	// 内容
	public static int	E_YYYY				= 8;	// ファイル提出の有無
    public static int	E_MONTH				= 9;	// 課題のタイトル
    public static int	E_DAY				= 10;	// 内容
    public static int	E_HOUR				= 11;	// 内容
    public static int	E_MINUTE			= 12;	// 内容
	public static int	PASSWD				= 13;	// 試験開始パスワード
	//
    Vector 		idx;
	Database	db;
	String    	te_aplec_key;
	Vector    	v;     				//  全講義レコード（各要素は Vector ）
	Hashtable 	keyHash;
    //
	//////////////////////////////////////////////////////////////
	//
	//    コンストラクタ
	//
	//////////////////////////////////////////////////////////////
	//
	//
    public KadaiAP(String key,Database _db){    // 教師ＩＤ
		idx               = new Vector();
		keyHash 		  = new Hashtable(50);
        v 				  = new Vector(20,10);
		db				  = _db;
		te_aplec_key = key;
		//
		db.getKadaiAps(te_aplec_key,v);
		mkKeyHash();		// te_aplec_kadai_key でのハッシュ
	}
	//
    //
	//////////////////////////////////////////////////////////////
	//
	//    全課題数を返す
	//
	//////////////////////////////////////////////////////////////
    //
	// 全課題数を返す
    public int size()   { return v.size(); }
	//
    //
	//////////////////////////////////////////////////////////////
	//
	//   ハッシュテーブルの作成と検索
	//
	//////////////////////////////////////////////////////////////
	// 
	// 
	// 課題定義キー（te_aplec_kadai_key） のハッシュを作成する
	void	mkKeyHash(){
		for(int i=0; i<v.size(); i++){
			String key = te_aplec_kadai_key(i);
			Integer no = new Integer(i);
			keyHash.put(key,no);
		}
	}
	//
	// 課題定義キー（te_aplec_kadai_key）でレコードを検索して返す
	// 該当がないと NULL を返す
	public KadaiApRecord fromKeyHash(String te_aplec_kadai_key){
		Integer no 	= (Integer)keyHash.get(te_aplec_kadai_key);
		if(no==null){
			return	null;
		}
		int i 		= no.intValue();
		return new KadaiApRecord( (Vector)v.get(i) );
	}
	//
    //
	//////////////////////////////////////////////////////////////
	//
	//    更新メソッド
	//
	//////////////////////////////////////////////////////////////
    //
	// 削除や挿入のあったケースで全データを再読み込みする
	public void refresh(){
		Vector ww = new Vector(20,10);
		db.getKadaiAps(te_aplec_key,ww);
		v = ww;
		mkKeyHash();
	}
	//
    // 現在のベクターで課題定義データデータを上書きして更新する
    public void update(){
        //
		int n = size();
		for(int i=0; i<n; i++){
			Vector cs = (Vector)v.get(i);
			//
			// 文字列を要素とするVectorでレコードを渡して更新する
			db.updateKadaiAp( cs ); 
		}
    }
	//
	// 特定のレコードを挿入する
	public void addRecord(Vector r){
		db.insertKadaiAp( r );
		refresh();
	}
	//
	// 特定のレコードを更新する
	public void updateRecord(Vector r){
		db.updateKadaiAp( r );
		refresh();
	}
	//
	// 特定のレコードを削除する
	public void deleteRecord(String _te_aplec_key,String _kadai_key){
		db.deleteKadaiAp( _te_aplec_key, _kadai_key);
		refresh();
	}
	//
	//
	///////////////////////////////////////////////////////////////////////
	//
	// レコードをシーケンス番号ｉで指定し、そのＫ番目の属性項目の値を返す
	//
	//  以下のメソッドはＮＵＬＬを返す場合があるので注意する
	//
	////////////////////////////////////////////////////////////////////////
	//
	//
    public String classPorp(int i,int k) {
        if(i >= size()) return null;
        //
		Integer num = (Integer)idx.get(i);
		int     pos = num.intValue();
		//
        Vector vk = (Vector) v.get(pos);
		if(k >= vk.size()) return null;
        return (String)(vk.get(k));
    }
	//-------------------------------------------------------------
    //
    // 第i番目のte_aplec_kadai_key
    public String te_aplec_kadai_key(int i) {
        return classPorp(i,TE_APLEC_KEY) + "-" + classPorp(i,KADAI_KEY);
    }
}