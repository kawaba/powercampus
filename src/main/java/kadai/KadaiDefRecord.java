/*
  課題定義レコードの処理をカプセル化する 
*/
//import java.text.*;
package kadai;

import java.util.Hashtable;
import java.util.Vector;
import database.Database;
import database.KeyGen;
import export.exKadai;
import framework.LOG;

/*
■課題データ

  講義セクション定義の中で作成するデータ

 teUid       = kawaba01 ------- 8 桁．一意なユーザーキー
 lec_key     = 003    --------- 3 桁．講義の連番
 kadai_key   = 000012 --------- 6 桁．課題の連番
 
 te_lect_key = kawaba01-003 --- 12 桁．　
  
  full key は　==>  kawaba01-003-000012   --- 19 桁

//////////////////////////////////////////////////////////////////////////////////////
  <<< 2002.8.4 変更 >>>

create table kadai (
     te_lec_key         CHAR(12)  NOT NULL,
     kadai_key          CHAR(6)   NOT NULL,
     seq_number         CHAR(3),
     shubetsu           CHAR(1),
     title              VARCHAR(200),
     content            text
);

// キーは複数列に設定する
create index kadai_idx on kadai (te_lec_key,kadai_key);


*/
public class KadaiDefRecord extends Object{
	//
	// 定数
	public final static int	TE_LEC_KEY		= 0;	// te_lec_key   18 桁
    public final static int	KADAI_KEY	    = 1;	// 課題キー      6 桁
	public final static int	SEQ_NUMBER	    = 2;	// 表示順序番号
	public final static int  SHUBETSU			= 3;
	public final static int	TITLE			= 4;	// 課題のタイトル
    public final static int	CONTENT			= 5;	// 内容
	//
	public final static int	MIN	= TE_LEC_KEY;
	public final static int	MAX	= CONTENT;
	//
	// 種別文字列
	public final static String [] SHUBETU_NAMES = { "","レポート","ファイル提出","試験","タイピング","タイプ試験"};
	public final static String  REPO   = "1";
	public final static String  FILE   = "2";
	public final static String  EXAM   = "3";
	public final static String  TYPE   = "4";
	public final static String  WRITE  = "5";

	/** レポート課題か */
	public static boolean	isReport(String str){
		return	str.equals(REPO);
	}
	public  boolean	isReport(){
		return	(shubetsu()).equals(REPO);
	}
	/** ファイル課題か */
	public static boolean	isFile(String str){
		return	str.equals(FILE);
	}
	public  boolean	isFile(){
		return	(shubetsu()).equals(FILE);
	}
	/** 試験課題か */
	public static boolean	isExam(String str){
		return	str.equals(EXAM);
	}
	public  boolean	isExam(){
		return	(shubetsu()).equals(EXAM);
	}
	/** タイピングか */
	public static boolean	isTyping(String str){
		return	str.equals(TYPE);
	}
	public  boolean	isTyping(){
		return	(shubetsu()).equals(TYPE);
	}
	/** タイプ試験か */
	public static boolean	isWriting(String str){
		return	str.equals(WRITE);
	}
	public  boolean	isWriting(){
		return	(shubetsu()).equals(WRITE);
	}	
	//
	// 種別文字列
	public final static String [] ITEM = { "","レポート課題","ファイル提出課題","試験（テスト）","タイピング練習","タイプ試験"};
	//
	public final static int	KADAI_SHURUI = SHUBETU_NAMES.length - 1;
	public final static int	KADAI_NUM	 = SHUBETU_NAMES.length - 1;
	//
	// 1) レポート, 2)ファイル提出, 3)試験 ,4)タイピング, 5)その他
	public final static String	[] KADAI_ICON	= { "spacer.gif","Paper.gif","File.gif","Examin.gif","typing.gif", "Other.gif" }; // １オリジン

	final static String   FILE_TYPE = "2";
	//
	Vector rec;
	//
	///////////////////////////////////////////////////
	//
	//   コンストラクタ
	//
	///////////////////////////////////////////////////
	//
	// 空のレコードを生成する
	public KadaiDefRecord(){
		//
		Vector w = new Vector(20,10);
		rec	= copy(w);
	}	//
	// Vector から生成する（レコード中に null があれば "" にする）
	public KadaiDefRecord(Vector w){
		if(LOG.fa) LOG.outVector(w,"■-1 KadaiDefRecord / 受け取ったレコード");
		//
		rec	= copy(w);
		if(LOG.fa) LOG.outVector(rec,"■-2 KadaiDefRecord / 生成したレコード");
	}// 
	// キーでデータベースを検索して生成する
	public KadaiDefRecord(String _te_lec_key,String _kadai_key,Database db){
		if(LOG.fa) LOG.println("class KadaiDefRecord # KadaiDefRecord(): コンストラクタの入り口です");
		//
		rec	= new Vector (20,10);
		for(int i=MIN; i<=MAX; i++){
			rec.add(i,"");
		}
		if(_te_lec_key != null){
			Vector w = db.getKadaiDef(_te_lec_key, _kadai_key);
			rec		 = copy(w);
		}
		if(LOG.fa) LOG.outVector(rec,"class KadaiDefRecord # KadaiDefRecord(): ◆コンストラクタで生成したレコードを示します");
	}
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
			if(w.get(i)!=null){	v.set(i,w.get(i)); }
		}
		return v;
	}	
	////////////////////////////////////////////////////
	//
	//   データオブジェクトの生成
	//
	////////////////////////////////////////////////////
	public exKadai	obj(){
		//
		return new exKadai(rec);
	}
	//
	///////////////////////////////////////////////////
	//
	//　　データベースの更新
	//
	///////////////////////////////////////////////////
	//
	// 挿入
	public int insert(Database db){
		int    n = db.insertKadaiDef( rec ); 
		return n;
	}
	// 更新
	public int update(Database db){
		int    n = db.updateKadaiDef( rec ); 
		return n;
	}
	// 削除
	public int delete(Database db){
		int    n = db.deleteKadaiDef( te_lec_key(),kadai_key() );
		return n;
	}
	//
	///////////////////////////////////////////////////
	//
	//　　レコードの値をセットする
	//
	///////////////////////////////////////////////////
	//
	// 課題レコードの値をハッシュテーブルからセットする
	public void  setFromHash(Hashtable htb){
        if(LOG.fa) LOG.outHash(htb,"KadaiDefRecord89:  setFromHash() の先頭です");
		//
		// teUid と lec_key は hash にあるが、復号キーは KeyGen で合成して得る
		String te_lec_key	= KeyGen.get_te_lec_key(htb);
		//
		String kadai_key	= (String)htb.get("_kadai_key");
		String seq_number	= (String)htb.get("_kadai_seqNum");
		String shubetsu		= (String)htb.get("_kadai_shubetsu");
		String title		= (String)htb.get("_kadai_title");
		String content		= (String)htb.get("_kadai_content");
		//
		if(te_lec_key==null)	te_lec_key 	= "";
		if(kadai_key==null)		kadai_key 	= "";
		if(seq_number==null)	seq_number 	= "";
		if(shubetsu==null)		shubetsu 	= "";
		if(title==null)			title 		= "";
		if(content==null)		content 	= "";
		//
		set_te_lec_key	(te_lec_key);
		set_kadai_key	(kadai_key);
		set_seq_number	(seq_number);
		set_shubetsu	(shubetsu);
		set_title		(title);
		set_content		(content);
		//
		//
		if(LOG.fa){
			LOG.println("     te_lec_key	=" + te_lec_key() );
			LOG.println("     kadai_key	=" + kadai_key() );
			LOG.println("     seq_number	=" + seq_number() );
			LOG.println("     shubetsu	=" + shubetsu() );
			LOG.println("     title		=" + title() );
			LOG.println("     content		=" + content() );
		}
		//
	}
	//
	public void set_te_lec_key(String str)		{ rec.set(TE_LEC_KEY,str); 	}
	public void set_kadai_key(String str)		{ rec.set(KADAI_KEY,str); 	}
	public void set_seq_number(String str)		{ rec.set(SEQ_NUMBER,str); 	}
	public void set_shubetsu(String str)		{rec.set(SHUBETSU,str); 	}
	public void set_title(String str)			{ rec.set(TITLE,str); 		}
	public void set_content(String str)		{ rec.set(CONTENT,str); 	}
	//
	// 文字列が空かどうかテストする
	//
	boolean isEmpty(String str){
		if(str==null) 			return  true;
		if(str.length()==0)	return  true;
		return false;
	}
	//
	///////////////////////////////////////////////////
	//
	//　　レコードの値を返す
	//
	///////////////////////////////////////////////////
	//
	// 課題のフルキーを返す
	public String te_lec_kadai_key()	{	return  te_lec_key() + "-" + kadai_key(); }
	//
	public String te_lec_key()		{	return (String)rec.get(TE_LEC_KEY); 	}
	public String kadai_key()		{	return (String)rec.get(KADAI_KEY); 	}
	public String seq_number()		{	return (String)rec.get(SEQ_NUMBER); 	}
	public String shubetsu()		{	return (String)rec.get(SHUBETSU); 		}
	public String title()			{	return (String)rec.get(TITLE); 		}
	public String content()			{	return (String)rec.get(CONTENT); 		}
	//
	// ファイル提出タイプかどうか
	public boolean isFiletype()	{ return (shubetsu()).equals(FILE_TYPE)  ?  true : false ; }
	//
	// アイコンの画像名を帰す／種別は１オリジン
	public String kadaiIcon(){
		int     k = Integer.parseInt( shubetsu() );
		return	KADAI_ICON[k];
	}
	public static String	kadaiIcon(String shu){
		int     k = Integer.parseInt( shu );
		return	KADAI_ICON[k];
	}
	// 種別名
	public String shubetsuName(){
		int     k = Integer.parseInt( shubetsu() );
		return	SHUBETU_NAMES[k];
	}
	// 種別名
	public String shubetsuName3(){
		int     k = Integer.parseInt( shubetsu() );
		return	"種別：" + SHUBETU_NAMES[k];
	}	

	// 種別名
	public String shubetsuName2(){
		int     k = Integer.parseInt( shubetsu() );
		return	ITEM[k];
	}	
	//
	// 更新してもいいか
	public boolean isUpdateKadaiOK(){
		boolean  flag = true;
		if((title()).length() == 0){ // タイトル名が入力してあること
			flag = false;
		}
		return flag;
	}
}