/*
教員１人ごとの講義定義データの処理をカプセル化する 
*/
//import java.text.*;
package kamoku;
import java.util.*;

import database.Database;
import framework.LOG;
//import java.lang.Exception;
import tktools.*;
/*
■講義定義データ
create table lecture (
     teuid            CHAR(8) NOT NULL,
     lec_key          CHAR(3) NOT NULL,
     title            text,
     content          text,
     seiseki_hyoka    text,
     keywords         text,
     textbook         text,
     ref_texts        text,
     ref_urls         text,
     note             text,
     bikou            text
);
create index lectur_idx on lecture (teUid,lec_key);
*/
//
public class KamokuDEF extends Object{
  //
	public static int	TEUID	= 0;	// teuid         8 桁
	public static int	LEC		= 1;	// lec_key       3 桁
	public static int	TITLE	= 2;	// 講義タイトル
	public static int	CONTENT	= 3;	// 内容
	public static int	HYOHKA	= 4;	// 成績評価の方法
	public static int	KEYWORD	= 5;	// キーワード
	public static int	TEXTBOOK= 6;	// テキスト
	public static int	REF_BOOK= 7;	// 参考文献
	public static int	REF_URL	= 8;	// 参考URL
	public static int	NOTE	= 9;	// 履修上の注意
	public static int	BIKOU	= 10;	// 備考
	//
  	Vector 		idx;
  	Database	db;
	String    	teUid;
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
  public KamokuDEF(String id,Database _db){    // 教師ＩＤ
		idx     = new Vector();
		keyHash = new Hashtable(50);
		v 		= new Vector(20,10);
		db		= _db;
		teUid  	= id;
		//
		db.getDefLectures(teUid,v);
		mkKeyHash();		// te_lec キーでのハッシュ
		sort(LEC);			// 最初からレコードをソートしておく
	}
	//
  //
	//////////////////////////////////////////////////////////////
	//
	//    基本メソッド
	//
	//////////////////////////////////////////////////////////////
	//
	// 削除や挿入のあったケースで全データを再読み込みする
	public void refresh(){
		Vector ww = new Vector(20,10);
		db.getDefLectures(teUid,ww);
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
			db.updateDefLecture( cs ); 
		}
	}
	//
	// 特定のレコードを挿入する
	public void addRecord(Vector r){
		db.insertDefLecture( r );
		refresh();
	}
	//
	// 特定のレコードを更新する
	public void updateRecord(Vector r){
		db.updateDefLecture( r );
		refresh();
	}
	//
	// 特定のレコードを削除する
	public void deleteRecord(String teUid, String aplec_key  ){
		db.deleteDefLecture( teUid, aplec_key);
		refresh();
	}
	//
  //
	//////////////////////////////////////////////////////////////
	//
	//    シーケンス番号で並び替えて順に取り出す
	//
	//////////////////////////////////////////////////////////////
  //
	// シーケンス番号で並べ替えられたレコード番号配列（Vector）を使って
	// シーケンスｋ番目のレコードを返す
	//
	// 第i番目のレコードを返す
	public KamokuDefRecord 	get(int k){
		Integer num = (Integer)idx.get(k);
		int     pos = num.intValue();
		Vector  rec = (Vector)v.get(pos);
		return  new KamokuDefRecord(rec);
	}
  //
	// 全科目数を返す
  public int size()   { 
		if(LOG.fa) LOG.println( "KamokuDEF:size()","講義定義レコードの数＝" + String.valueOf( v.size()) ); 
		return v.size(); 
	}
	//
  //
	//////////////////////////////////////////////////////////////
	//
	//   ハッシュテーブルの作成と検索
	//
	//////////////////////////////////////////////////////////////
	// 
	// 
	// 講義定義キー（te_lec_key） のハッシュを作成する
	void	mkKeyHash(){
		for(int i=0; i<v.size(); i++){
			String key = te_lec_key(i);
			Integer no = new Integer(i);
			keyHash.put(key,no);
		}
	}
	//
	// 講義定義キー（te_lec_key）でレコードを検索して返す
	//  該当がないと NULL を返す
	public KamokuDefRecord fromKeyHash(String te_lec_key){
		Integer no 	= (Integer)keyHash.get(te_lec_key);
		if(no==null){
			return	null;
		}
		int i 		= no.intValue();
		return new KamokuDefRecord( (Vector)v.get(i) );
	}
	//////////////////////////////////////////////////////////////
	//
	//  第i番目のレコードの各項目を返す
	//  
	//  以下のメソッドはＮＵＬＬを返す場合があるので注意する
	//
	//////////////////////////////////////////////////////////////
	//
	//
	// 第i番目の科目のk番目の属性を返す
	//
	public String classPorp(int i,int k) {
      if(i >= size()) return null;
      //
      Vector vk = (Vector) v.get(i);
		if(k >= vk.size()) return null;
      return (String)vk.get(k);
	}
	//-------------------------------------------------------------
	//
	// 第i番目のte_lec_key
	public String te_lec_key(int i) {
      return classPorp(i,TEUID) + "-" + classPorp(i,LEC);
	}
	// 第i番目の teUid
	public String teUid(int i) {
      return  classPorp(i,TEUID);
	}
	// 第i番目の lec_key
	public String lec_key(int i) {
      return  classPorp(i,LEC);
	}
	// 第i番目の講義タイトル
	public String title(int i) {
		return classPorp(i,TITLE);
	}
	//
  	// 第i番目の内容
	public String content(int i) {
		return classPorp(i,CONTENT);
	}
	//
	// 第i番目の成績評価の方法
	public String hyoka(int i) {
		return classPorp(i,HYOHKA);
	}
	//
	// 第i番目キーワード
	public String keywords(int i) {
		return classPorp(i,KEYWORD);
	}
	//
	// 第i番目のテキスト
	public String textbook(int i) {
		return classPorp(i,TEXTBOOK);
	}
	//
	// 第i番目の参考文献
	public String ref_book(int i) {
		return classPorp(i,REF_BOOK);
	}
	//
	// 第i番目の参考URL
	public String ref_url(int i) {
		return classPorp(i,REF_URL);
	}
	//
	// 第i番目のNOTE
	public String note(int i) {
		return classPorp(i,NOTE);
	}
	//
	// 第i番目の備考
  	public String bikou(int i) {
		return classPorp(i,BIKOU);
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
		if(n < 1 ) 		return;
		//
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
