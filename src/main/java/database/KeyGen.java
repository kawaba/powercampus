/*
  個人用の全てのキーを生成する
*/
//import java.text.*;
package database;
import java.util.Hashtable;
import java.util.Vector;
import framework.LOG;
import tktools.Csv;
import tktools.Gear;
//import java.lang.Exception;
/*
	■個人用キーの値
		
　		キーは連番である
　		１つキーを取り出すたびに、値を１増やしておく
　		１つキーを取り出すたびに、「次の値セット」としてレコードをＣＳＶで書き出しておく
			
　			teUid    ------- 教員のユーザＩＤ（システムで完全に一意）
  			lec ------------ 講義定義キー
  			applec --------- 講義実施キー
  			sect ----------- セクションキー
  			kadai ---------- 課題キー
  			ref ------------ 資料キー
  			
  		セクションキーと課題定義キーは定義と実施では同じものを使う．
  		上位に付加するのが講義キーか講義実施キーかで区別できるから．
	------------------------------------------------------------
	create table key_gen (
    	teUid           CHAR(8)  PRIMARY KEY,			教員のユーザＩＤ
    	lec             CHAR(3)  DEFAULT '001',			講義定義キー
		aplec           CHAR(3)  DEFAULT '001',			講義実施キー
    	sect            CHAR(5)  DEFAULT '00001',		セクションキー
    	kadai           CHAR(6)  DEFAULT '000001',		課題キー
    	ref             CHAR(6)  DEFAULT '000001',		資料キー
		faq             CHAR(6)  DEFAULT '000001'
	); 	
	------------------------------------------------------------
*/
//
public class KeyGen extends Object{
	//
	// 数値文字列整形用
	private static final String [] zero = {"","0","00","000","0000","00000","000000","0000000","00000000","000000000","0000000000"};
    //
	public static int	TEUID		= 0;	// 教員のユーザＩＤ
    public static int	LEC	    	= 1;	// 講義定義キー
	public static int	APLEC	   	= 2;	// 講義実施キー
	public static int	SECT		= 3;	// セクションキー
    public static int	KADAI		= 4;	// 課題キー
    public static int	REF			= 5;	// 資料キー
    public static int	FAQ			= 6;	// ＦＡＱキー
	//
	// キーの長さ
	//public static int	LEN_TEUID	= 8;
	public static int	LEN_LEC		= 3;
	public static int	LEN_APLEC	= 3;
	public static int	LEN_SECT	= 5;
	public static int	LEN_KADAI	= 6;
	public static int	LEN_REF		= 6;
	public static int	LEN_FAQ		= 6;
	//
	Database	db;
	String    	id;
	Vector    	rec;     					//  レコード（各要素は Vector ）
    //
	//////////////////////////////////////////////////////////////
	//
	//    コンストラクタ
	//
	//////////////////////////////////////////////////////////////
	//
	//
    public KeyGen(String _id,Database _db){    // 教師ＩＤ
        rec		= new Vector(10,10);
		db		= _db;
		id 		= _id;
		//
		db.getKeyGen(id, rec);
	}
	//
    //
	//////////////////////////////////////////////////////////////
	//
	//    更新メソッド ---  特定のキーの現在の値を得る
	//                      同時にそのキーの値を１増やして書き戻す
	//
	//      追加や削除はない
	//
	//////////////////////////////////////////////////////////////
	//
	// 科目定義キー
	public String nextLec(){
		String 	key		= lec();
		String  newKey	= increase(key);
		rec.set(LEC,newKey);
		updateRecord();
		return key;
	}
	// 講義実施キー
	// 必ずNULLチェックをすること
	public static String nextAplec(Hashtable htb){
		//
		String	shubetsu	= (String)htb.get("_shubetsu");
		String	worder		= (String)htb.get("_worder");
		String	wdate		= (String)htb.get("_wdate");
		//
		if(isEmpty(shubetsu))	{if(LOG.fa) LOG.println("★★ shubetst は null です / KegGen.nextAplec() "); 	return null;}
		if(isEmpty(worder))		{if(LOG.fa) LOG.println("★★ worder は null です / KegGen.nextAplec() ");	return null;}
		if(isEmpty(wdate))		{if(LOG.fa) LOG.println("★★ wdate は null です / KegGen.nextAplec() ");		return null;}
		//
		return	shubetsu + worder + wdate;
	}
	static boolean isEmpty(String str){
		if(str==null) 			return  true;
		if(str.length()==0)		return  true;
		return false;
	}
	/*
	//
	// 講義実施キー
	public String nextAplec(){
		String 	key		= aplec();
		String  newKey	= increase(key);
		rec.set(APLEC,newKey);
		updateRecord();
		return key;
	}
	*/
	
	//
	// セクションキー
	public String nextSect(){
		String 	key		= sect();
		String  newKey	= increase(key);
		rec.set(SECT,newKey);
		updateRecord();
		return key;
	}
	//
	// 課題キー
	public String nextKadai(){
		String 	key		= kadai();
		String  newKey	= increase(key);
		rec.set(KADAI,newKey);
		updateRecord();
		return key;
	}
	//
	// 資料キー
	public String nextRef(){
		String 	key		= ref();
		String  newKey	= increase(key);
		rec.set(REF,newKey);
		updateRecord();
		return key;
	}
	//
	// ＦＡＱキー
	public String nextFaq(){
		String 	key		= faq();
		String  newKey	= increase(key);
		rec.set(FAQ,newKey);
		updateRecord();
		return key;
	}
	//
	//
	//////////////////////////////////////////////////////////////
    //
    //  更新用下請けメソッド
	//
	//////////////////////////////////////////////////////////////
	//
	// レコードを更新する
	public void updateRecord(){
		db.updateKeyGen( id, rec );
	}
	//
	//
	// キーの値を１増やした固定長の文字列を得る。（先頭はゼロで埋める）
	String increase(String key){
		int		n 	= Integer.parseInt(key);		// 整数化して１増やす
		++n;
		//
		int		len = key.length();					// 文字列の長さ
		//
        String  n2      = String.valueOf(n);		// 新しい値の文字列
        int     pos     = n2.length();				// 先頭にゼロを埋めて固定長にする
        String  pattern = zero[len] + n2;
        return  pattern.substring(pos);
    }
	//
	// 各レコードの値を返す
	String teUid()		{	return (String)rec.get(TEUID);	}
	String lec()		{	return (String)rec.get(LEC); 	}
	String aplec()		{	return (String)rec.get(APLEC); 	}
	String sect()		{	return (String)rec.get(SECT); 	}
	String kadai()		{	return (String)rec.get(KADAI);	}
	String ref()		{	return (String)rec.get(REF); 	}
	String faq()		{	return (String)rec.get(FAQ); 	}
	//
	//
	/////////////////////////////////////////////////////////////////////////
	//
	//   Static なユーティリティ
	//
	/////////////////////////////////////////////////////////////////////////
	//
	//
	//　講義実施キーは、曜日時限と対応しているので、これから何曜日何限という文字列を
	//　作って返す
	//
	static String [] wdate  = {"月","火","水","木","金","土","日"};	// 曜日は０オリジン
	static String [] worder = {"１","２","３","４","５","６","７","８","９"};	// 時限は０オリジン
	static String [] course = {"Ａ","Ｂ","Ｃ","Ｄ","Ｅ","Ｆ"};      // コースは０オリジン
	//
	/**
	 * クラスのリレーションキーを作成する
	 * @param teUid
	 * @param aplec_key
	 * @return
	 */
	public static	String	getRelation(String teUid, String aplec_key, String seasonCsv){
		if(LOG.fa) LOG.println("■ KeyGen #getRelation()");
		
		String	year		=	Gear.getyear();
		String	season		=	Gear.getSeason(seasonCsv);
		
		String	relation	=	year + season +  "-" + teUid + "-" + aplec_key;
		return	relation;
	}
	/**
	 * プール中を示すリレーションキーを返す
	 * 
	 * @return	プール中を示すリレーションキー
	 */
	public static	String	poolingRelation(){
		if(LOG.fa) LOG.println("■ KeyGen #poolingRelation()");
		
		return	"pooling-forum";
	}
	
	
	public static String wdateTypeA(String aplec_key){
		//
		int		s1	= Integer.parseInt( aplec_key.substring(0, 1) );	// 種別：一般、夜間、e-Learning
		int		s2	= Integer.parseInt( aplec_key.substring(1, 2) );	// 時限： 0 - 8
		int		s3	= Integer.parseInt( aplec_key.substring(2, 3) );	// 曜日： 0 - 5
		//
		String str = "";
		if(s1 == 1){
			str 	= wdate[s3] + "曜日 " + worder[s2] + "限";
		}else if(s1 == 2){
			str 	= wdate[s3] + "曜日 " + worder[s2] + "限（夜）";
		}else if(s1 == 3){
			str 	= course[s3] + "コース";
		}
		return str;
	}
	public static String wdateTypeB(String aplec_key){
		//
		int		s1	= Integer.parseInt( aplec_key.substring(0, 1) );	// 種別：一般、夜間、e-Learning
		int		s2	= Integer.parseInt( aplec_key.substring(1, 2) );	// 時限： 0 - 8
		int		s3	= Integer.parseInt( aplec_key.substring(2, 3) );	// 曜日： 0 - 5
		//
		String str = "";
		if(s1 == 1){
			str 	= wdate[s3] + "／" + worder[s2];
		}else if(s1 == 2){
			str 	= wdate[s3] + "／" + worder[s2] + "（夜）";
		}else if(s1 == 3){
			str 	= course[s3] + "コース";
		}
		return str;
	}
	//
	// ハッシュテーブルのデータから科目キーを作成して返す
	//  
	public static String	get_te_lec_key(Hashtable htb){
		String teUid	= (String)htb.get("_teUid");
		String lec_key	= (String)htb.get("_lec_key");
		if( (teUid==null) || (lec_key==null))  return null;
		//
		return (String)htb.get("_teUid") + "-" + (String)htb.get("_lec_key");
	}
	//
	public static String	get_te_lec_key2(String teUid,String lec_key){
		if( (teUid==null) || (lec_key==null))  return null;
		//
		return teUid + "-" + lec_key;
	}
	public static String	lecFromTe_lec(String te_kec_key){
		Csv	cs	=	new	Csv(te_kec_key, "-");
		int	n	=	cs.size();
		return		cs.get(n-1);	// 最後の要素

	}
	public static String	teUidFromTe_lec(String te_lec_key){
		Csv	cs	=	new	Csv(te_lec_key, "-");
		int	n	=	cs.size();
		return		cs.get(0);	// 先頭の要素

	}	
	//
	public static String	get_te_lec_sect_key(Hashtable htb){
		return (String)htb.get("_teUid") + "-" + (String)htb.get("_lec_key") + "-" + (String)htb.get("_sect_key");
	}
	//
	public static String	get_te_aplec_key(Hashtable htb){
		String teUid		= (String)htb.get("_teUid");
		String aplec_key	= (String)htb.get("_aplec_key");
		if( teUid==null)  		teUid 		= "?";
		if( aplec_key==null)  	aplec_key	= "?";
		//
		String te_aplec_key	= htb.get("_teUid") + "-" + (String)htb.get("_aplec_key");
		htb.put("_te_aplec_key",te_aplec_key);
		return te_aplec_key;
	}
	//
	public static String	get_te_aplec_key2(String teUid,String aplec_key){
		if( (teUid==null) || (aplec_key==null))  return null;
		//
		return teUid + "-" + aplec_key;
	}
	//
	public static String	get_te_aplec_sect_key(Hashtable htb){
		return (String)htb.get("_teUid") + "-" + (String)htb.get("_aplec_key") + "-" + (String)htb.get("_sect_key");
	}
	// 講義実施キーとセクションキーを指定して生成する
	public static String	get_te_aplec_sect_key2(String teApLecKey,String sectKey){
		if( (teApLecKey==null) || (sectKey==null))  return null;
		return teApLecKey + "-" + sectKey;
	}
	//
	// ダミーのキーを返す
	//
	public static String  get_Dummy_sect_key(){
		return	"*****";
	}
	//
	//  キーの長さをチェックする
	//
	public static boolean  is_te_lec_key(String key){
		if(key==null)	return false;
		if(key.length() <= (LEN_LEC + 1) ) return false;
		return true;
	}
	public static boolean  is_te_lec_sect_key(String key){
		if(key==null)	return false;
		if(key.length() <= ( LEN_LEC + LEN_SECT + 2) ) return false;
		return true;
	}
}
/*
	public static int	LEN_TEUID	= 不定;
	public static int	LEN_LEC		= 3;
	public static int	LEN_APLEC	= 3;
	public static int	LEN_SECT	= 5;
	public static int	LEN_KADAI	= 6;
	public static int	LEN_REF		= 6;
*/
