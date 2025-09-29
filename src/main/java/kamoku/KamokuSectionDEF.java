/*
  講義定義データ中のセクション定義データの処理をカプセル化する 
  
*/
//import java.text.*;
package kamoku;
import java.util.*;

import database.Database;
import framework.LOG;
//import java.lang.Exception;
import tktools.*;
/*
■講義セクションデータ（講義定義の一部）

　te_lec_key は講義データのキー　teUid + '-' + lec_key を意味する
　
　例示：　kawaba01-001
  
　フルキーの例示： kawaba01-003-00012

  <<< 2002.8.4 変更 >>>
  
create table sect (
       te_lec_key       CHAR(12)  NOT NULL,
       sect_key         CHAR(5)   NOT NULL,
       seq_number       VARCHAR(2),
       title            text,
       content          text,
       note             text,
       ref_list         VARCHAR(200),  <------- 追加 （キーのリスト）
       kadai_list       VARCHAR(200)   <------- 追加 （同上）
);

// キーは複数列に設定する
create index sect_idx on sect (te_lec_key,sect_key);

　　キーリストの例
　　
　　000001$000001&000013 など６桁の資料キー、または課題キー。
　　デリミッタは '&' 
　　kawaba01-002 のような te_lec キーは、所属セクションのキーと同じだから省略してある

*/
//
public class KamokuSectionDEF extends Object{
	//
	public static String DELIMITER = "&";
    //
	public static int	TE_LEC_KEY	= 0;	// te_kec_key   12 桁
    public static int	SECT_KEY	= 1;	// sect_key      5 桁
	public static int	SEQ_NUMBER	= 2;	// 順序番号
	public static int	TITLE       = 3;	// セクションタイトル
	public static int	CONTENT     = 4;	// 説明
	public static int	NOTE        = 5;	// 備考・ToDo（学生への注意・指示など）
	public static int	REF_LIST    = 6;
	public static int	KADAI_LIST  = 7;
	//
	public static int	MIN	= TE_LEC_KEY;
	public static int	MAX	= KADAI_LIST;
	//
    Vector 		idx;
    Database	db;
	String    	te_lec_key;
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
    public KamokuSectionDEF(String _te_lec_key,Database _db){    // 教師ＩＤ
        if(LOG.fa) LOG.println("class KamokuSectionDEF #KamokuSectionDEF() : コンストラクタ の先頭です");
		if(LOG.fa) LOG.println("           _te_lec_key = " + _te_lec_key);
		//
		idx         = new Vector();
		keyHash 	= new Hashtable(50);
        v 			= new Vector(20,10);
		db			= _db;
		te_lec_key 	= _te_lec_key;
		//
		db.getSectionDefs(te_lec_key,v);
		//
		//
		if(LOG.fa) {
			int n = v.size();
			LOG.println("getSectionDefs で得られたレコード数：",String.valueOf(n));
			for(int k=0; k<n; k++){
				Vector chk = (Vector)v.get(k);
				LOG.outVector(chk,"レコード内容");
			}
		}
		// ソートが先
		sort(SEQ_NUMBER);		// 最初からレコードをソートしておく
		if(LOG.fa) LOG.println("sort(SEQ_NUMBER) 終了");
		//
		mkKeyHash();			// te_lec_sect キーでのハッシュ
		if(LOG.fa) LOG.println("mkKeyHash() 終了");
	}
	//
    //
	//////////////////////////////////////////////////////////////
	//
	//    シーケンス番号で並び替えて順に取り出す
	//
	//////////////////////////////////////////////////////////////
    //
	// 全セクション数を返す
    public int size()   { return v.size(); }
	//
	// シーケンス番号で並べ替えられたレコード番号配列（Vector）を使って
	// シーケンスｋ番目のレコードを返す
	//
	// 第i番目のレコードを返す
	public KamokuSecDefRecord get(int k){
        if(LOG.fa) LOG.println("KamokuSectionDEF(): get() の先頭です","シーケンス番号＝" + String.valueOf(k));
		//
		if( (k<0)||(k>=size())) return null;
		//
		Integer num = (Integer)idx.get(k);
		int     pos = num.intValue();
		Vector  rec = (Vector)v.get(pos);
		return  new KamokuSecDefRecord(rec);
	}
	//////////////////////////////////////////////////////////////
	//
	//    第ｉ番目のセクションのキーリストをCSVで返す
	//
	//////////////////////////////////////////////////////////////
	//
	public Csv refKeys(int i)		{return	new Csv( ref_list(i),DELIMITER ); }
	//
	public Csv kadaiKeys(int i)		{return	new Csv( kadai_list(i),DELIMITER ); }
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
		db.getSectionDefs(te_lec_key,ww);
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
			db.updateSectionDef( cs ); 
		}
    }
	//
	// 特定のレコードを挿入する
	public void addRecord(Vector r){
		db.insertSectionDef( r );
		refresh();
	}
	//
	// 特定のレコードを更新する
	public void updateRecord(Vector r){
		db.updateSectionDef( r );
		refresh();
	}
	//
	// 特定のレコードを削除する
	// （関連する実施課題データも削除しなければならない）
	//
	public void deleteRecord(String te_lec_key,String sect_key){
		db.deleteSectionDef( te_lec_key, sect_key);
		refresh();
	}
	//
	//////////////////////////////////////////////////////////////
	//
	//   ハッシュテーブルの作成と検索
	//
	//////////////////////////////////////////////////////////////
	// 
	// 
	// 講義定義キー（te_lec_sect_key） のハッシュを作成する
	void	mkKeyHash(){
		if(LOG.fa) LOG.println("mkKeyHash()開始");
		for(int i=0; i<v.size(); i++){
			String key = te_lec_sect_key(i);
			Integer no = new Integer(i);
			keyHash.put(key,no);
		}
	}
	//
	// 講義定義キー（te_lec_key とsect_key）でレコードを検索して返す
	//  該当がないと NULL を返す
	public KamokuSecDefRecord fromKeyHash(String te_lec_key, String sect_key){
		return fromKeyHash(te_lec_key + "-" + sect_key);
	}
	public KamokuSecDefRecord fromKeyHash(String te_lec_sect_key){
		Integer no 	= (Integer)keyHash.get(te_lec_sect_key);
		if(no==null){
			return	null;
		}
		int i 		= no.intValue();
		return new KamokuSecDefRecord( (Vector)v.get(i) );
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
		if(LOG.fa) LOG.println("classPorp()開始");
		if(LOG.fa) LOG.println("ｉの値",String.valueOf(i));
		if(LOG.fa) LOG.println("kの値" ,String.valueOf(k));
		//
        if(i >= size()) return null;
        //
		Integer num = (Integer)idx.get(i);
		int     pos = num.intValue();
		//
        Vector vk = (Vector) v.get(pos);
		if(k >= vk.size()) return null;

		if(LOG.fa) LOG.println("classPorp()の終了");

        return (String)(vk.get(k));
    }
	//-------------------------------------------------------------
    //
    // 第i番目のte_aplec_key
    public String te_lec_sect_key(int i) {
        return classPorp(i,TE_LEC_KEY) + "-" + classPorp(i,SECT_KEY);
    }
    // 第i番目の講義定義キー
    public String te_lec_key(int i) {
		return classPorp(i,TE_LEC_KEY);
    }
    //
    // 第i番目のセクションキー
    public String sect_key(int i) {
		return classPorp(i,SECT_KEY);
    }
    //
    // 第i番目の順序番号
    public String seq_number(int i) {
		return classPorp(i,SEQ_NUMBER);
    }
	// セクションタイトル
    public String title(int i) {
		return classPorp(i,TITLE);
    }
	// 説明
    public String content(int i) {
		return classPorp(i,CONTENT);
    }
	// 備考・ToDo（学生への注意・指示など）
    public String note(int i) {
		return classPorp(i,NOTE);
    }
	// 
    public String ref_list(int i) {
		return classPorp(i,REF_LIST);
    }
	//
    public String kadai_list(int i) {
		return classPorp(i,KADAI_LIST);
    }
	//
	/////////////////////////////////////////////////////////////
	//
	//    ソートメソッド
	//
	/////////////////////////////////////////////////////////////
	//
	// レコードを第ｋ番目の項目の順で並び替える
	public void sort(int k){
		int n			= v.size();
		if(n < 1 ) return;
		String  [] rec 	= new String[n];		// ソート用配列
		for(int i=0; i<n; i++){
			String n_3  = get000type(i);
			Vector vd	= (Vector)v.get(i);
			rec[i]	    = (String)(vd.get(k)) + "$" + n_3;	// ｋ番目の項目　＋ レコード番号 （$はデリミッタ） 
		}
		if(n > 1) { Arrays.sort(rec);	} // ソート(n==1 ならソートしない)
		//
		Vector temp  = new Vector(100,50);
		for(int i=0; i<n; i++){
			Csv      cs  = new Csv(rec[i],"$");			// デリミッタ指定
			Integer  num = new Integer( cs.get(1) ); 	// レコード番号部分
			//
			temp.add(num);// レコード番号を登録していく
		}
		idx = temp;	// ソート済みと差し替える
	}
	// 先頭を０で埋めて 3 桁の整数にする
    String get000type(int s){
        String  dt      = String.valueOf(s);
        if((dt == null)||(dt.length() == 0)) return "000";
        int     pos     = dt.length();
        String  pattern = "000" + dt;
        return  pattern.substring(pos);
    }
}