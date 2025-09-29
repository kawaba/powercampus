/*
  教員１人ごとの講義実施データの処理をカプセル化する 
*/
//import java.text.*;
package kamoku;
import java.util.*;

import database.Database;
import framework.LOG;
/*
■講義実施データ

  lec_key は 講義定義データのサブキー部分 teUid-lec_key で講義定義キーとなる
  
  meibo はクラスの名簿ファイル名
  
  shubetsu  1 = 一般講義
            2 = 夜間講義
			3 = e-Learning

      3 の場合、worder のみ意味を持つ

　yyyy は年度だが、 2003 とか 平成１５ か　決められないので VARCHAR にしてある
　term も前期、前後期、通年などいろいろな書き方があるので同上

　unit は単位数。1.5 とかもありえる 

  title は lecture のtitleと同じ（表示用の冗長情報）

create table app_lecture (
       teUid            CHAR(8) NOT NULL,
       aplec_key        CHAR(3) NOT NULL,
       lec_key          CHAR(3) NOT NULL,
       meibo            VARCHAR(100),
	   shubetsu         CHAR(1),
       wdate            CHAR(1),
       worder           CHAR(1),
       yyyy             VARCHAR(20),
       term             VARCHAR(20),
       unit             VARCHAR(3),
	   title            text
);

// キーは複数列に設定する
create index app_lecture_idx on app_lecture (teUid,aplec_key);
  
*/
//
public class KamokuAP extends Object{
    //
	public static int	TEUID	= 0;	// teuid         8 桁
	public static int	AP_LEC  = 1;	// applec_key    3 桁
    public static int	LEC		= 2;	// lec_key       3 桁
	public static int	MEIBO	= 3;	// 名簿ファイル名
    public static int	SHUBETU	= 4;	// 実施種別
    public static int	WDATE	= 5;	// 実施曜日または番号
    public static int	WORDER	= 6;    // 時限
    public static int	YYYY	= 7;	// 実施年度
    public static int	TERM	= 8;	// 実施期
    public static int	UNIT	= 9;	// 実施期
    public static int	TITLE	= 10;	// 科目名（冗長情報／時間割表示用）
	//
    Database	db;
	String    	teUid;
    Vector    	v;     				//  全講義レコード（各要素は Vector ）
	Hashtable 	keyHash;
	Hashtable 	kmHash;				// （時限 + 曜日、レコード） のハッシュ
    //
	//////////////////////////////////////////////////////////////
	//
	//    コンストラクタ
	//
	//////////////////////////////////////////////////////////////
	//
	//
    public KamokuAP(String id,Database _db){    // 教師ＩＤ
        if(LOG.fa) LOG.println("コンストラクタ KamokuAP()の先頭です: teUid = " + id);
		//
		keyHash = new Hashtable(50);
		kmHash 	= new Hashtable(50);
        v 		= new Vector(20,10);
		db		= _db;
		teUid  	= id;
		//
		db.getApLectures(teUid,v);
		mkHash();
		mkKeyHash();
		//
        if(LOG.fa) LOG.outVector2(v,"* コンストラクタ KamokuAP()の生成結果です");
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
		db.getApLectures(teUid,ww);
		v = ww;
		//
        //sort(WDATE,WORDER);	// 曜日+ 時限で並び替える
		mkHash();
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
			db.updateApLecture( cs ); 
		}
    }
	//
	// 特定のレコードを挿入する
	public void addRecord(Vector r){
		db.insertApLecture( r );
		refresh();
	}
	//
	// 特定のレコードを更新する
	public void updateRecord(Vector r){
		db.updateApLecture( r );
		refresh();
	}
	//
	// 特定のレコードを削除する
	public void deleteRecord(String teUid, String aplec_key  ){
		db.deleteApLecture( teUid, aplec_key  );
		refresh();
	}
    //
	// 全科目数を返す
    public int size()   { return v.size(); }
	//
    //
	//////////////////////////////////////////////////////////////
	//
	//   特定の科目について講義実施レコード内の科目名を一括更新する
	//
	//////////////////////////////////////////////////////////////
	// 
	// 
	void	updateTitles( String target_lec_key,String kamokuMei ){
		for(int i=0; i<size(); i++){
			Vector 	rec		= (Vector) v.get(i);		// 第i番目のレコード
			String	wlec	= (String) rec.get(LEC);	// 科目キーを取り出す
			if(wlec.equals(target_lec_key)){			// 指定された科目キーと同一か
				rec.set(TITLE,kamokuMei);				// 同じなら、タイトルを新しく付け替え
				db.updateApLecture( rec );				// さらにデータベースを更新する
			}
		}
		refresh();
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
	// （種別 + 時限 + 曜日、レコード） のハッシュを作成する
	void	mkHash(){
		for(int i=0; i<v.size(); i++){
			String key = shubetsu(i) + order(i) + date(i);
			Integer no = new Integer(i);
			kmHash.put(key,no);
		}
	}
	// 講義実施キー（te_aplec_key） のハッシュを作成する
	void	mkKeyHash(){
		for(int i=0; i<v.size(); i++){
			String key = te_aplec_key(i);
			Integer no = new Integer(i);
			keyHash.put(key,no);
		}
	}
	//
	// （種別 + 時限 + 曜日)でレコードを検索して返す
	//  該当がないと NULL を返す
	public KamokuApRecord fromHash(String shubetsu, int jigen,int date){
		if(LOG.fa)	LOG.println("fromHash()の先頭です: " + shubetsu + "/" + jigen + "/" + date);
		String key 	= shubetsu + String.valueOf(jigen) + String.valueOf(date);
		Integer no 	= (Integer)kmHash.get(key);
		if(no==null){
			if(LOG.fa)	LOG.println("fromHash() での結果は null です");
			return	null;
		}
		int i 		= no.intValue();
	if(LOG.fa)	LOG.println("fromHash() で KamokuApRecord レコードを生成しました");
		return new KamokuApRecord( (Vector)v.get(i) );
	}
	//
	// 講義実施キー（te_aplec_key）でレコードを検索して返す
	//  該当がないと NULL を返す
	public KamokuApRecord fromKeyHash(String te_aplec_key){
		Integer no 	= (Integer)keyHash.get(te_aplec_key);
		if(no==null){
			return	null;
		}
		int i 		= no.intValue();
		return new KamokuApRecord( (Vector)v.get(i) );
	}
	//
	//
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
    // 第i番目のteuid-applec_key
    public String te_aplec_key(int i) {
        return classPorp(i,TEUID) + "-" + classPorp(i,AP_LEC);
    }
    // 第i番目のlec_key
    public String lec_key(int i) {
		return classPorp(i,LEC);
    }
    //
    // 第i番目の名簿ファイル名
    public String meibo(int i) {
		return classPorp(i,MEIBO);
    }
    //
    // 第i番目の実施種別
    public String shubetsu(int i) {
		return classPorp(i,SHUBETU);
    }
    //
    // 第i番目の実施曜日または番号
    public String date(int i) {
		return classPorp(i,WDATE);
    }
    //
    // 第i番目の開講時限
    public String order(int i) {
		return classPorp(i,WORDER);
    }
    //
    // 第i番目の実施年度
    public String yyyy(int i) {
		return classPorp(i,YYYY);
    }
    //
    // 第i番目の実施期
    public String term(int i) {
		return classPorp(i,TERM);
    }
    //
    // 第i番目の単位数
    public String unit(int i) {
		return classPorp(i,UNIT);
    }
    //
    // 第i番目の講義タイトル
    public String title(int i) {
		return classPorp(i,TITLE);
    }
}
/*
	/////////////////////////////////////////////////////////////
	//
	//    ソートメソッド
	//
	/////////////////////////////////////////////////////////////
	// レコードを第（ｋ番目＋j番目）の項目の順で並び替える
	public void sort(int k,int j){
		int n			= v.size();
		if(n <= 1 ) return;
		String  [] rec 	= new String[n];		// ソート用配列
		for(int i=0; i<n; i++){
			Csv cs	= (Csv)v.get(i);
			rec[i]	= cs.get(k) + cs.get(j) + "$" + cs.toCSV();	// ｋ番目の項目　＋ レコード全体 （$はデリミッタ） 
		}
		Arrays.sort(rec);	// ソート
		//
		Vector w	= new Vector(10,5);
		for(int i=0; i<n; i++){
			Csv cs = new Csv(rec[i],"$");	// デリミッタ指定
			w.add(new Csv( cs.get(1) ));	// キー部分を除いたもとのレコード
		}
		v = w;	// ソート済みと差し替える
	}
	// ベクターを（ｋ番目＋j番目）の項目の順で並び替えて返す
	public Vector sort(Vector r,int k,int j){
		int n			= r.size();
		if(n <= 1 ) return r;
		String  [] rec 	= new String[n];		// ソート用配列
		for(int i=0; i<n; i++){
			Csv cs	= (Csv)r.get(i);
			rec[i]	= cs.get(k) + cs.get(j) + "$" + cs.toCSV();	// ｋ番目の項目　＋ レコード全体 （$はデリミッタ） 
		}
		Arrays.sort(rec);	// ソート
		//
		Vector w	= new Vector(10,5);
		for(int i=0; i<n; i++){
			Csv cs = new Csv(rec[i],"$");	// デリミッタ指定
			w.add(new Csv( cs.get(1) ));	// キー部分を除いたもとのレコード
		}
		return w;	// ソート済みを返す
	}
	// レコードを第ｋ番目の項目の順で並び替える
	public void sort(int k){
		int n			= v.size();
		if(n <= 1 ) return;
		String  [] rec 	= new String[n];		// ソート用配列
		for(int i=0; i<n; i++){
			Csv cs	= (Csv)v.get(i);
			rec[i]	= cs.get(k) + "$" + cs.toCSV();	// ｋ番目の項目　＋ レコード全体 （$はデリミッタ） 
		}
		Arrays.sort(rec);	// ソート
		//
		Vector w	= new Vector(10,5);
		for(int i=0; i<n; i++){
			Csv cs = new Csv(rec[i],"$");	// デリミッタ指定
			w.add(new Csv( cs.get(1) ));	// キー部分を除いたもとのレコード
		}
		v = w;	// ソート済みと差し替える
	}
	// ベクターを第ｋ番目の項目の順で並び替えて返す
	public Vector sort(Vector r,int k){
		int n			= r.size();
		if(n <= 1 ) return r;
		String  [] rec 	= new String[n];		// ソート用配列
		for(int i=0; i<n; i++){
			Csv cs	= (Csv)r.get(i);
			rec[i]	= cs.get(k) + "$" + cs.toCSV();	// ｋ番目の項目　＋ レコード全体 （$はデリミッタ） 
		}
		Arrays.sort(rec);	// ソート
		//
		Vector w	= new Vector(10,5);
		for(int i=0; i<n; i++){
			Csv cs = new Csv(rec[i],"$");	// デリミッタ指定
			w.add(new Csv( cs.get(1) ));	// キー部分を除いたもとのレコード
		}
		return w;	// ソート済みを返す
	}

*/