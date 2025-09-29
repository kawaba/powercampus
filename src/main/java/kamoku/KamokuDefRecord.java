/*
講義定義レコードの処理をカプセル化する 
*/
//import java.text.*;
package kamoku;
import java.util.*;

import xmlparser.*;
import database.Database;
import export.exKamoku;
import framework.LOG;
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

public class KamokuDefRecord extends Object{
	//
	// 定数
	public static int	TEUID	= 0;	// teuid         8 桁
	public static int	LEC		= 1;	// lec_key       3 桁
	public static int	TITLE	= 2;	// 講義タイトル
	public static int	CONTENT	= 3;	// 内容
	// 
	// xml になってからこれ以下は利用していない
	public static int	HYOHKA	= 4;	// 成績評価の方法
	public static int	KEYWORD	= 5;	// キーワード
	public static int	TEXTBOOK= 6;	// テキスト
  	public static int	REF_BOOK= 7;	// 参考文献
  	public static int	REF_URL	= 8;	// 参考URL
  	public static int	NOTE	= 9;	// 履修上の注意
  	public static int	BIKOU	= 10;	// 備考
	
	Vector rec;
	//
	///////////////////////////////////////////////////
	//
	//   コンストラクタ
	//
	///////////////////////////////////////////////////
	//
	//  空のレコードを生成する
	//
	public KamokuDefRecord(){
		//
		rec	= new Vector(20,10);
		for(int i=0; i<=BIKOU; i++){
			rec.add("");
		}
	}

	//-------------------------------------------------------------------
	// Vector から生成する
	//    レコード中に null があれば "" にする
	//
	public KamokuDefRecord(Vector w){
		if(LOG.fa) LOG.println("class KamokuDefRecord #KamokuDefRecord() : Vector から生成する コンストラクタの先頭です");
		if(LOG.fa) LOG.outVector(w,"■-1 受け取ったレコード");
		//
		rec	= copy(w);
		if(LOG.fa) LOG.outVector(rec,"■-2 生成したレコード");
	}
	//----------------------------------------------------------------
	// キーでデータベースを検索して生成する
	//
	// 　　	teUid が null なら全て "" のレコードを生成する
	//		レコード中に null があれば "" にする
	//
	public KamokuDefRecord(String teUid,String lec_key,Database db){
      if(LOG.fa) LOG.println("KamokuDefRecord()の先頭です" + "/ teUid=" + teUid + " ,lec_key=" + lec_key);
		//
		rec	= new Vector (20,10);
		for(int i=TEUID; i<=BIKOU; i++){
			rec.add(i,"");
		}
		if(teUid != null){
			Vector w = db.getDefLecture(teUid, lec_key);
			if(LOG.fa) LOG.outVector(w,"データベース検索直後の w");
			rec		 = copy(w);
			if(LOG.fa) LOG.outVector(rec,"w を整形したはずの rec");
		}
	}
	//
	//  全ての要素を "" に初期化したのち、null でないものだけをコピーする
	//  コピーは、レコードサイズを超えて行われることはない
	//
	Vector copy(Vector w){
		//
		Vector v = new Vector( 20,10 );
		for(int i=TEUID; i<=BIKOU; i++){	// "" に初期化しておく
			v.add(i,"");
		}
		// w をコピーする
		// null の要素は コピーしない
		//
		for(int i=0; ( (i<w.size())&&(i<=BIKOU) ); i++){
			if(w.get(i)!=null){	v.set(i,(String)w.get(i)); }
		}
		return v;
	}
	/**
	 * シラバス定義XMLのserialをnnnにセットしてデータベースを更新する．
	 * すでにレコードには値が入っていなければならない．
	 *
	 */
	public	void	setSerial(String nnn, String templatePath, Database db) throws xmlException{
		
		KamokuParser	kp	=	new KamokuParser( content() );
		kp.setSerial(nnn);
		//
		KamokuRevParser	krp	=	new	KamokuRevParser(kp, templatePath);
		set_content(krp.getXml());
		update(db);
		
	}
	
	////////////////////////////////////////////////////
	//
	//   データオブジェクトの生成
	//
	////////////////////////////////////////////////////
	public exKamoku	obj(){
		//
		return new exKamoku(rec);
	}
	
	///////////////////////////////////////////////////
	//
	//　　データベースの更新
	//
	///////////////////////////////////////////////////
	//
	// 挿入
	public int insert(Database db){
		if(LOG.fa) LOG.outVector(rec,"class KamokuDefRecord #insert() : ★★★　挿入 の先頭です　★★★");
		int    n = db.insertDefLecture( rec ); 
		return n;
	}
	// 更新
	public int update(Database db){
		if(LOG.fa) LOG.outVector(rec,"class KamokuDefRecord #update() : ★★★　更新 の先頭です　★★★");
		int    n = db.updateDefLecture( rec ); 
		return n;
	}
	// 削除
	public int delete(Database db){
		int    n = db.deleteDefLecture( teuid() , lec_key() );
		return n;
	}
	///////////////////////////////////////////////////
	//
	//　　レコードの値をセットする
	//
	///////////////////////////////////////////////////
	//
	//
	public void set_teuid(String str)		{ rec.set(TEUID,str); 	}
	public void set_lec_key(String str)	{ rec.set(LEC,str); 	}
	public void set_title(String str)		{ rec.set(TITLE,str); 	}
	public void set_content(String str)	{ rec.set(CONTENT,str); }
	public void set_hyoka(String str)		{ rec.set(HYOHKA,str); 	}
	public void set_keywords(String str)	{ rec.set(KEYWORD,str); }
	public void set_textbook(String str)	{ rec.set(TEXTBOOK,str);}
	public void set_ref_book(String str)	{ rec.set(REF_BOOK,str);}
	public void set_ref_url(String str)	{ rec.set(REF_URL,str); }
	public void set_note(String str)		{ rec.set(NOTE,str); 	}
	public void set_bikou(String str)		{ rec.set(BIKOU,str); 	}
	//
	///////////////////////////////////////////////////
	//
	//　　レコードの値を返す
	//
	///////////////////////////////////////////////////
	//
	//
	public String teuid()		{	return (String)rec.get(TEUID); 		}
	public String lec_key()		{	return (String)rec.get(LEC); 		}
	public String title()		{	return (String)rec.get(TITLE); 		}
	public String content()		{	return (String)rec.get(CONTENT); 	}
	public String hyoka()		{	return (String)rec.get(HYOHKA); 	}
	public String keywords()	{	return (String)rec.get(KEYWORD); 	}
	public String textbook()	{	return (String)rec.get(TEXTBOOK); 	}
	public String ref_book()	{	return (String)rec.get(REF_BOOK);	}
	public String ref_url()		{	return (String)rec.get(REF_URL); 	}
	public String note()		{	return (String)rec.get(NOTE); 		}
	public String bikou()		{	return (String)rec.get(BIKOU); 		}
	//
	// ハッシュにデータを返す（キーは既にハッシュの中にあるものとする）
	public void setToHash(Hashtable htb){
		//
		htb.put("_title"			,title()	);
		htb.put("_content"			,content()	);
		htb.put("_seiseki_hyoka"	,hyoka()	);
		htb.put("_keywords"			,keywords()	);
		htb.put("_textbook"			,textbook()	);
		htb.put("_ref_texts" 		,ref_book()	);
		htb.put("_ref_urls"			,ref_url()	);
		htb.put("_syllabusNote"		,note()		);
		htb.put("_bikou"			,bikou()	);
	}
	//
	///////////////////////////////////////////////////////
	//  特殊な目的の satitc メソッド
	////////////////////////////////////////////////////////
	//
	//  teUid とlec_key から title を引いて返す
	//  レコードがないときは,"" を返す
	//
	public static String getTitle(String teUid,String lec_key,Database db){
		//
		Vector temp = db.getDefLecture(teUid, lec_key);
		if(temp.size() > 0) return	(String)temp.get(TITLE);
		return "";
	}
}