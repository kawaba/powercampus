/*
  資料データの処理をカプセル化する 
*/
//import java.text.*;
package refer;
import java.util.*;

import database.Database;
import framework.LOG;
//import java.lang.Exception;
import tktools.*;
/*
■資料データ

　講義セクション定義の中で作成するデータ

 teUid       = kawaba01 ------- 8 桁．一意なユーザーキー
 lec_key     = 003    --------- 3 桁．講義の連番
 ref_key     = 000012 --------- 6 桁．資料の連番
 

 te_lec_key = kawaba01-003 ----------- 12 桁．　
 full key は　==>  kawaba01-003-000012 ---- 19 桁

  shubetsu:   1 = ウェブ資料
              2 = ビデオ
			  3 = 書籍・雑誌
			  4 = プリント等
			  5 = その他

///////////////////////////////////////////////////////////
  <<< 2002.8.4 変更 >>>

create table reference (
     te_lec_key         CHAR(12)  NOT NULL,
     ref_key            CHAR(6)   NOT NULL,
     seq_number         CHAR(2),
     shubetsu           CHAR(1),
     title              VARCHAR(200),
     url                VARCHAR(200)
);

// キーは複数列に設定する
create index reference_idx on reference (te_lec_key,ref_key);

*/
//
public class ReferenceDEF extends Object{
    //
	public static int	TE_LEC_KEY		= 0;	// te_lec_key   18 桁
    public static int	REF_KEY	        = 1;	// 資料キー      6 桁
	public static int	SEQ_NUMBER	    = 2;	// 表示順序番号
	public static int	SHUBETU			= 3;	// 資料の種類
    public static int	TITLE			= 4;	// 資料のタイトル
    public static int	UTL 			= 5;	// url
	//
    Vector 		idx;
	Database	db;
	String    	te_lec_key;
	Vector    	v;     				//  全資料レコード（各要素は Vector ）
	Hashtable 	keyHash;
    //
	//////////////////////////////////////////////////////////////
	//
	//    コンストラクタ
	//
	//////////////////////////////////////////////////////////////
	//
	//
    public ReferenceDEF(String _te_lec_key,Database _db){    // 教師ＩＤ
        if(LOG.fa) LOG.println("コンストラクタ ReferenceDEF()の先頭です: te_lec_key = " + _te_lec_key);
		//
		idx         = new Vector();
		keyHash 	= new Hashtable(50);
        v 			= new Vector(20,10);
		db			= _db;
		te_lec_key  = _te_lec_key;
		//
		db.getReferenceDefs(te_lec_key,v);
		//
		sort(SEQ_NUMBER);	// シーケンス番号でソートしたレコード番号配列を作成する
		mkKeyHash();		// te_lec_key でのハッシュ
        if(LOG.fa) LOG.println("コンストラクタ ReferenceDEF()の最後です: " + String.valueOf(size()) + " 件のデータがあります");
	}
	//
    //
	//////////////////////////////////////////////////////////////
	//
	//    シーケンスで取り出す
	//
	//////////////////////////////////////////////////////////////
    //
	// 全資料数を返す
    public int size()   { return v.size(); }
	//
	//
	// シーケンス番号で並べ替えられたレコード番号配列（Vector）を使って
	// シーケンスｋ番目のレコードを返す
	//
	public ReferenceDefRecord get(int k){
		Integer num = (Integer)idx.get(k);
		int     pos = num.intValue();
		Vector  rec = (Vector)v.get(pos);
		return  new ReferenceDefRecord(rec);
	}
	
	/**
	 * 資料キーリストを受け取り、リストにあるレコードだけをベクターに入れて返す
	 * レコードの並び順はシーケンス番号順
	 *
	 */
	public	Vector	selectedRec(String te_lec_key, Csv keylist){
		
		Vector	vtemp	=	new	Vector();
		for(int	 i=0; i<keylist.size(); i++){
			/*
			 * 資料定義キーでレコードを検索して、存在すればベクターに保管する
			 */
			ReferenceDefRecord	rdf	=	fromKeyHash(te_lec_key, keylist.get(i));
			if(rdf!=null){
				vtemp.add(rdf);
			}
		}
		
		return	sort(vtemp);
	}
	
	// レコードをシーケンスの順で並び替える
	public Vector sort(Vector data){

		Vector nv	=	new	Vector();

		int n		=	data.size();
		if(n==0) return null;
		
		String  [] rec 	= new String[n];// ソート用配列
		for(int i=0; i<n; i++){
			String 				num  	= 	get000type(i);
			ReferenceDefRecord 	rdf		= 	(ReferenceDefRecord)data.get(i);
			rec[i]	    				= 	rdf.seq_number() + "$" + num;	// ｋ番目の項目　＋ レコード番号 （$はデリミッタ） 
		}
		if(n > 1) { Arrays.sort(rec);	} // ソート(n==1 ならソートしない)
		//
		for(int i=0; i<n; i++){
			Csv      cs  = new Csv(rec[i],"$");		// デリミッタ指定
			nv.add(cs.get(1));						// レコード番号を登録していく
		}
		Vector	sortedV	=	new Vector();
		for(int i=0; i<n; i++){
			sortedV.add( data.get(Integer.parseInt(  (String)nv.get(i)  )) );
		}
		return	sortedV;
	}
	
    //
	//////////////////////////////////////////////////////////////
	//
	//   ハッシュテーブルの作成と検索
	//
	//////////////////////////////////////////////////////////////
	// 
	// 
	// 資料定義キー（te_lec_ref_key） のハッシュを作成する
	void	mkKeyHash(){
		for(int i=0; i<v.size(); i++){
			String key = te_lec_ref_key(i);
			Integer no = new Integer(i);
			keyHash.put(key,no);
		}
		/*
		DBG.println("------------資料定義キー（te_lec_ref_key） のハッシュを作成する------");
		for(int i=0; i<v.size(); i++){
			String key = te_lec_ref_key(i);
			Integer no = new Integer(i);
			keyHash.put(key,no);
			DBG.println(key,no.intValue());
		}
		DBG.println("----------- idx  ----------------------------------------------------");
		for(int i=0; i < idx.size(); i++){
			DBG.println("## idx(" + String.valueOf(i) + ") = " + String.valueOf( ((Integer)idx.get(i)).intValue() ) );
		}
		DBG.println("------------ v  ----------------------------------------------------");
		for(int k=0; k < v.size(); k++){
				DBG.println("##   v(" + String.valueOf(k) + ") = " + "(" + (String)(((Vector)v.get(k)).get(0)) + "," + (String)(((Vector)v.get(k)).get(1)) + "," + (String)(((Vector)v.get(k)).get(2)) );
		}
		DBG.println("---------------- ----------------------------------------------------");
		*/
	}
	//
	// 資料定義キー（te_lec_ref_key）でレコードを検索して返す
	public ReferenceDefRecord fromKeyHash(String te_lec_key, String ref_key){
		return fromKeyHash(te_lec_key + "-" + ref_key);
	}
	//
	// null を返すことがあるので受け取り側で必ずチェックする
	public ReferenceDefRecord fromKeyHash(String te_lec_ref_key){
		Integer no 	= (Integer)keyHash.get(te_lec_ref_key);
		if(no==null)  	return	null;
		//
		int 	i	= no.intValue();
		Integer iv	= (Integer)idx.get(i);	// シーケンス番号
		if(iv==null) 	return	null;
		//
		int		k	= iv.intValue();		// 実レコード番号
		Vector	wk	= (Vector)v.get(k);
		if(wk==null)	return	null;
		//
		return new ReferenceDefRecord(wk); // 実レコード
	}
	
	public Vector fromKeyHash2(String te_lec_ref_key){
		Integer no 	= (Integer)keyHash.get(te_lec_ref_key);
		if(no==null)  	return	null;
		//
		int 	i	= no.intValue();
		Integer iv	= (Integer)idx.get(i);	// シーケンス番号
		if(iv==null) 	return	null;
		//
		int		k	= iv.intValue();		// 実レコード番号
		Vector	wk	= (Vector)v.get(k);
		if(wk==null)	return	null;
		//
		return wk; // 実レコード
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
		db.getReferenceDefs(te_lec_key,ww);
		v = ww;
		mkKeyHash();
		sort(SEQ_NUMBER);	// シーケンス番号でソートしたレコード番号配列を作成する
	}
	//
    // 現在のベクターで資料定義データデータを上書きして更新する
    public void update(){
        //
		int n = size();
		for(int i=0; i<n; i++){
			Vector cs = (Vector)v.get(i);
			//
			// 文字列を要素とするVectorでレコードを渡して更新する
			db.updateReferenceDef( cs ); 
		}
    }
	//
	// 特定のレコードを挿入する
	public void addRecord(Vector r){
		db.insertReferenceDef( r );
		refresh();
	}
	//
	// 特定のレコードを更新する
	public void updateRecord(Vector r){
		db.updateReferenceDef( r );
		refresh();
	}
	//
	// 特定のレコードを削除する
	public void deleteRecord(String te_lec_key,String ref_key){
		db.deleteReferenceDef( te_lec_key, ref_key);
		refresh();
	}
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
		Integer num = (Integer)idx.get(i);
		int     pos = num.intValue();
        Vector vk = (Vector) v.get(pos);
		if(k >= vk.size()) return null;
        return (String)(vk.get(k));
    }
	//-------------------------------------------------------------
    //
    // 第i番目のte_lec_ref_key
    public String te_lec_ref_key(int i) {
        return classPorp(i,TE_LEC_KEY) + "-" + classPorp(i,REF_KEY);
    }
    // 第i番目のte_lec_key
    public String te_lec_key(int i) {
		return classPorp(i,TE_LEC_KEY);
    }
    //
    // 第i番目の資料キー
    public String ref_key(int i) {
		return classPorp(i,REF_KEY);
    }
    //
    // 第i番目のシーケンス番号
    public String seq_number(int i) {
		return classPorp(i,SEQ_NUMBER);
    }
    //
    // 第i番目の資料種別
    public String shubetsu(int i) {
		return classPorp(i,SHUBETU);
    }
    //
    // 第i番目の資料のタイトル
    public String title(int i) {
		return classPorp(i,TITLE);
    }
    //
    // 第i番目のＵＲＬ
    public String url(int i) {
		return classPorp(i,UTL);
    }
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