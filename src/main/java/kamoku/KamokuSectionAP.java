/*
  講義定義データ中のセクションデータの処理をカプセル化する 
  
  実質的にはシーケンス番号の管理を行うだけ
  
*/
//import java.text.*;
package kamoku;
import java.util.*;

import database.Database;
//import java.lang.Exception;
/*
■講義セクション実施データ

　te_aplec_key は講義実施データのキー　teUid + '-' + aplec_key を意味する
　
　例示：　kawaba01-003
  
　フルキーの例示： kawaba01-003-00012

  実施にかかる日付と講義メモのデータ

create table app_sect (
       te_aplec_key     CHAR(12)  NOT NULL,
       sect_key         CHAR(5)   NOT NULL,
       s_mm             CHAR(2),
       s_dd             CHAR(2),
       e_mm             CHAR(2),
       e_dd             CHAR(2),
       memo             text
);

// キーは複数列に設定する
create index sect_idx on app_sect (te_aplec_key,sect_key);

*/
//
public class KamokuSectionAP extends Object{
    //
	public static int	TE_APLEC_KEY	= 0;	// te_kec_key   12 桁
    public static int	SECT_KEY		= 1;	// sect_key      5 桁
	public static int	S_MM       		= 2;	// 実施開始月
	public static int	S_DD		 	= 3;	// 同日
	public static int	E_MM			= 4;	// 実施終了月（空白でも可）
	public static int	E_DD       		= 5;	// 同日（空白でも可）
	public static int	MEMO       		= 6;	// 講義メモ（講義実施前、実施後のメモ）
	//
    Vector 		idx;
    Database	db;
	String    	te_aplec_key;		// 講義実施キー
    Vector    	v;     				// 全講義レコード（各要素は Vector ）
	Hashtable 	keyHash;
    //
	//////////////////////////////////////////////////////////////
	//
	//    コンストラクタ
	//
	//////////////////////////////////////////////////////////////
	//
	//
    public KamokuSectionAP(String key,Database _db){    // 教師ＩＤ
		idx         	= new Vector();
		keyHash 		= new Hashtable(50);
        v 				= new Vector(20,10);
		db				= _db;
		te_aplec_key 	= key;
		//
		db.getSectionAps(te_aplec_key,v);
		mkKeyHash();			// te_aplec_sect キーでのハッシュ
	}
	//
    //
	//////////////////////////////////////////////////////////////
	//
	//    全セクション数を返す
	//
	//////////////////////////////////////////////////////////////
    //
	// 全セクション数を返す
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
	// 講義定義キー（te_aplec_sect_key） のハッシュを作成する
	void	mkKeyHash(){
		for(int i=0; i<v.size(); i++){
			String key = te_aplec_sect_key(i);
			Integer no = new Integer(i);
			keyHash.put(key,no);
		}
	}
	//
	// 講義定義キー（te_aplec_key）でレコードを検索して返す
	// 該当がないと NULL を返す
	public KamokuSecApRecord fromKeyHash(String te_aplec_sect_key){
		Integer no 	= (Integer)keyHash.get(te_aplec_sect_key);
		if(no==null){
			return	null;
		}
		int i 		= no.intValue();
		return new KamokuSecApRecord( (Vector)v.get(i) );
	}
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
		db.getSectionAps(te_aplec_key,ww);
		v = ww;
		mkKeyHash();
	}
	//
    // 現在のベクターで講義実施データを上書きして更新する
    public void update(){
        //
		int n = size();
		for(int i=0; i<n; i++){
			Vector cs = (Vector)v.get(i);
			//
			// 文字列を要素とするVectorでレコードを渡して更新する
			db.updateSectionAp( cs ); 
		}
    }
	//
	// 特定のレコードを挿入する
	public void addRecord(Vector r){
		db.insertSectionAp( r );
		refresh();
	}
	//
	// 特定のレコードを更新する
	public void updateRecord(Vector r){
		db.updateSectionAp( r );
		refresh();
	}
	//
	// 特定のレコードを削除する
	// （関連する実施課題データも削除しなければならない）
	//
	public void deleteRecord(String te_aplec_key,String sect_key){
		db.deleteSectionAp( te_aplec_key, sect_key);
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
    // 第i番目のte_aplec_key
    public String te_aplec_sect_key(int i) {
        return classPorp(i,TE_APLEC_KEY) + "-" + classPorp(i,SECT_KEY);
    }
    // 第i番目の講義定義キー
    public String te_aplec_key(int i) {
		return classPorp(i,TE_APLEC_KEY);
    }
    //
    // 第i番目のセクションキー
    public String sect_key(int i) {
		return classPorp(i,SECT_KEY);
    }
	// 実施開始月
    public String s_mm(int i) {
		return classPorp(i,S_MM);
    }
	// 同日
    public String s_dd(int i) {
		return classPorp(i,S_DD);
    }
	// 実施終了月（空白でも可）
    public String e_mm(int i) {
		return classPorp(i,E_MM);
    }
	// 同日（空白でも可）
    public String e_dd(int i) {
		return classPorp(i,E_DD);
    }
	// 講義メモ（講義実施前、実施後のメモ）
    public String memo(int i) {
		return classPorp(i,MEMO);
    }
}