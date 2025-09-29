/*
  課題定義レコードの処理をカプセル化する 
*/
//import java.text.*;
package refer;
import java.util.*;

import database.Database;
import export.exReference;
import framework.LOG;

/*
■資料データ

　講義セクション定義の中で作成するデータ

 teUid       = kawaba01 ------- 8 桁．一意なユーザーキー
 lec_key     = 003    --------- 3 桁．講義の連番
 ref_key     = 000012 --------- 6 桁．資料の連番
 

 te_lec_key = kawaba01-003 ----------- 12 桁．　
 full key は　==>  kawaba01-003-000012 ---- 19 桁

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
public class ReferenceDefRecord extends Object{
	//
	// 定数
	public static int	TE_LEC_KEY		= 0;	// te_lec_key   18 桁
    public static int	REF_KEY	        = 1;	// 資料キー      6 桁
	public static int	SEQ_NUMBER	    = 2;	// 表示順序番号
	public static int	SHUBETSU		= 3;	// 資料の種類
    public static int	TITLE			= 4;	// 資料のタイトル
    public static int	UTL 			= 5;	// url
	//
	// 種別文字列
	public static String [] SHUBETU_NAMES = { "","書籍・雑誌","Web参照","ビデオ","ファイル","プリント等","その他","作成Web教材"};
	public static String [] SHUBETU_E_NAMES ={ "","book","web","video","file","doc","other","web"};
	public static int		SHUBETSU_MAX	= SHUBETU_NAMES.length - 1;			// 種別の個数
	static public int   	REF_NUM			= SHUBETU_NAMES.length - 1;
	//
	public static	String	REF_BOOK	= "1";
	public static	String	REF_WEB		= "2";
	public static	String	REF_VIDEO	= "3";
	public static	String	REF_FILE	= "4";
	public static	String	REF_DOC		= "5";
	public static	String	REF_OTHER	= "6";
	public static	String	REF_HTML	= "7";
	//
	public static int	MIN	= TE_LEC_KEY;
	public static int	MAX	= UTL;
	//
	//
	// 1) 本, 2)ウェブ,3)ストリーミングビデオ ,4)ファイル,5)プリント等 ,6)その他,7)ウェブ
	static String	[] REF_ICON	= { "spacer.gif","Book.gif","Web.gif","Video.gif","File.gif","Memo.gif","Other.gif","Web.gif" }; // １オリジン
	
	Vector rec;
	//
	///////////////////////////////////////////////////
	//
	//   コンストラクタ
	//
	///////////////////////////////////////////////////
	//
	// 空のレコードを生成する
	public ReferenceDefRecord(){
		//
		Vector w = new Vector(20,10);
		rec	= copy(w);
	}
	//
	// Vector から生成する（レコード中に null があれば "" にする）
	public ReferenceDefRecord(Vector w){
		if(LOG.fa) LOG.outVector(w,"■-1 ReferenceDefRecord / 受け取ったレコード");
		//
		rec	= copy(w);
		if(LOG.fa) LOG.outVector(rec,"■-2 ReferenceDefRecord / 生成したレコード");
	}
	// 
	// キーでデータベースを検索して生成する（レコード中に null があれば "" にする）
	public ReferenceDefRecord(String _te_lec_key,String _ref_key,Database db){
		//
		rec	= new Vector (20,10);
		for(int i=MIN; i<=MAX; i++){
			rec.add(i,"");
		}
		if(_te_lec_key != null){
			Vector w = db.getReferenceDef(_te_lec_key, _ref_key);
			rec		 = copy(w);
		}
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
			if(w.get(i)!=null){	v.set(i,(String)w.get(i)); }
		}
		return v;
	}	
	////////////////////////////////////////////////////
	//
	//   データオブジェクトの生成
	//
	////////////////////////////////////////////////////
	public exReference	obj(){
		//
		exReference	xref	=	new exReference(rec);
		return xref;
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
		int    n = db.insertReferenceDef( rec ); 
		return n;
	}
	// 更新
	public int update(Database db){
		int    n = db.updateReferenceDef( rec ); 
		return n;
	}
	// 削除
	public int delete(Database db){
		int    n = db.deleteReferenceDef( te_lec_key(),ref_key() );
		return n;
	}
	//
	///////////////////////////////////////////////////
	//
	//　　レコードの値をセットする
	//
	///////////////////////////////////////////////////
	//
	public void set_te_lec_key(String str)	{ rec.set(TE_LEC_KEY,str); 	}
	public void set_ref_key(String str)		{ rec.set(REF_KEY,str); 	}
	public void set_seq_number(String str)	{ rec.set(SEQ_NUMBER,str); 	}
	public void set_shubetsu(String str)	{ rec.set(SHUBETSU,str); 	}
	public void set_title(String str)		{ rec.set(TITLE,str); 		}
	public void set_url(String str)			{ rec.set(UTL,str); 		}
	//
	///////////////////////////////////////////////////
	//
	//　　レコードの値を返す
	//
	///////////////////////////////////////////////////
	//
	// 課題のフルキーを返す
	public String te_lec_ref_key()	{	return  te_lec_key() + "-" + ref_key(); }
	//
	public String te_lec_key()		{	return (String)rec.get(TE_LEC_KEY); 	    }
	public String ref_key()			{	return (String)rec.get(REF_KEY); 			}
	public String seq_number()		{	return (String)rec.get(SEQ_NUMBER); 		}
	public String shubetsu()		{	return (String)rec.get(SHUBETSU); 			}
	public String title()			{	return (String)rec.get(TITLE); 			}
	public String url()				{	return (String)rec.get(UTL); 				}
	//
	// アイコンの画像名を帰す／種別は１オリジン
	public String refIcon(){
		int     k = Integer.parseInt( shubetsu() );
		return	REF_ICON[k];
	}
	//
	public String shubetsuName(){
		int     k = Integer.parseInt( shubetsu() );
		return	SHUBETU_NAMES[k];
	}
	public String shubetsuName_E(){
		int     k = Integer.parseInt( shubetsu() );
		return	SHUBETU_E_NAMES[k];
	}
	//
	// 更新してもいいか
	public boolean isUpdateRefOK(){
		if(LOG.fa) LOG.println("class ReferenceDefRecord #isUpdateRefOK() : 更新してもいいか の先頭です");
		if(LOG.fa) LOG.println("           seq_number() = " + seq_number());
		if(LOG.fa) LOG.println("           shubetsu()   = " + shubetsu());
		if(LOG.fa) LOG.println("           title()      = " + title());
		if(LOG.fa) LOG.println("           url()        = " + url()  );
		//
		boolean  flag = true;
		if((shubetsu()).equals(REF_VIDEO)||(shubetsu()).equals(REF_WEB)||(shubetsu()).equals(REF_FILE)||(shubetsu()).equals(REF_HTML)){	// 
			if((title()).length() == 0){ // 資料名が入力してあること
				flag = false;
			}
			if((url()).length() == 0){ // 資料ＵＲＬ名が入力してあること
				flag = false;
			}
		}else{
			if((title()).length() == 0){ // 資料名が入力してあること
				flag = false;
			}
		}
		return flag;
	}
}
