/*
  講義定義レコードのセクションデータの処理をカプセル化する 
*/
//import java.text.*;
package kamoku;
import java.util.Vector;
import database.Database;
import export.exSection;
import framework.LOG;
//import java.lang.Exception;
import tktools.Csv;
/*
■講義セクションデータ
create table sect (
       te_lec_key       CHAR(12)  NOT NULL,
       sect_key         CHAR(5)   NOT NULL,
       seq_number       CHAR(2),
       title             text,
       content           text,
       note              text,
	   ref_list			VARCHAR(200),
	   kadai_list		VARCHAR(200)
);

// キーは複数列に設定する
create index sect_idx on sect (te_lec_key,sect_key);

*/
public class KamokuSecDefRecord extends Object{
	//
	public static String DELIMITER = ",";
	//
	// 定数
	public static int	TE_LEC_KEY	= 0;	// te_kec_key   12 桁
    public static int	SECT_KEY	= 1;	// sect_key      5 桁
	public static int	SEQ_NUMBER	= 2;	// 順序番号
	public static int	TITLE		= 3;	// セクションタイトル
	public static int	CONTENT		= 4;	// 説明
	public static int	NOTE		= 5;	// 備考・ToDo（学生への注意・指示など）
	public static int	REF_LIST    = 6;
	public static int	KADAI_LIST  = 7;
	//
	public static int	MIN	= TE_LEC_KEY;
	public static int	MAX	= KADAI_LIST;
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
	public KamokuSecDefRecord(){
		//
		Vector w = new Vector(20,10);
		rec	= copy(w);
	}
	//
	// Vector から生成する（レコード中に null があれば "" にする）
	public KamokuSecDefRecord(Vector w){
		
		if(LOG.fa) LOG.outVector(w,"■-1 受け取ったレコード");
		//
		rec	= copy(w);
		if(LOG.fa) LOG.outVector(rec,"■-2 生成したレコード");
	}
	// 
	// キーでデータベースを検索して生成する（レコード中に null があれば "" にする）
	public KamokuSecDefRecord(String _te_lec_key,String _sect_key,Database db){
		//
		rec	= new Vector (20,10);
		for(int i=MIN; i<=MAX; i++){
			rec.add(i,"");
		}
		if(_te_lec_key != null){
			Vector w = db.getSectionDef(_te_lec_key, _sect_key);
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
			if(w.get(i)!=null){	v.set(i,w.get(i)); }
		}
		return v;
	}	
	////////////////////////////////////////////////////
	//
	//   データオブジェクトの生成
	//
	////////////////////////////////////////////////////
	public exSection	obj(){
		//
		return new exSection(rec);
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
		int    n = db.insertSectionDef( rec ); 
		return n;
	}
	// 更新
	public int update(Database db){
		int    n = db.updateSectionDef( rec ); 
		return n;
	}
	// 削除
	public int delete(Database db){
		int    n = db.deleteSectionDef( te_lec_key(),sect_key() );
		return n;
	}
	// 指定のrefkey をリストから削除して更新する
	public	void	updateRefkeys(String	refkey, Database db){
		StringBuffer	buf		=	new StringBuffer();
		boolean		flag	=	false;
		Csv keys	=	refKeys();
		if(LOG.fa){
		    LOG.println("");
		    LOG.println("■ KamokuSecDefRecord #updateRefkeys()");
		    LOG.println("        oldkeys = " + keys.toCSV());
		    LOG.println("        refkey  = " + refkey);
		}
		int n		=	keys.size();
		for(int i=0; i<n; i++){
			/*
			 * 所与のキーと等しくなければバッファに書き込む
			 */
			if( !refkey.equals( keys.get(i) ) ){
				if(flag)	buf.append(DELIMITER);
				buf.append(keys.get(i));
				flag=true;
				
				if(LOG.fa){LOG.println(keys.get(i) + "is keeped");}
			}
		}
		/*
		 *  CSV文字列を作成してレコードをアップデートする
		 */
		String	refkeyCSV	=	"";
		if(buf.length()>0)	refkeyCSV	=	buf.toString();
		set_ref_list(refkeyCSV);
		if(LOG.fa){
		    LOG.println("        newkeys = " + refkeyCSV);
		}
		update(db);
	}
	// 指定のkadaikey をリストから削除して更新する
	public	void	updateKadaikeys(String	kadaikey, Database db){
		StringBuffer	buf		=	new StringBuffer();
		boolean		flag	=	false;
		Csv keys	=	kadaiKeys();
		int n		=	keys.size();
		for(int i=0; i<n; i++){
			/*
			 * 所与のキーと等しくなければバッファに書き込む
			 */
			if( !kadaikey.equals( keys.get(i) ) ){
				if(flag)	buf.append(DELIMITER);
				buf.append(keys.get(i));
				flag=true;
			}
		}
		/*
		 *  CSV文字列を作成してレコードをアップデートする
		 */
		String	kadaikeyCSV	=	"";
		if(buf.length()>0)	kadaikeyCSV	=	buf.toString();
		set_kadai_list(kadaikeyCSV);
		update(db);
	}	
	//////////////////////////////////////////////////////////////
	//
	//    セクションのキーリストをCSVで返す
	//
	//////////////////////////////////////////////////////////////
	//
	public Csv refKeys()		{return	new Csv( ref_list(),DELIMITER ); }
	//
	public Csv kadaiKeys()		{return	new Csv( kadai_list(),DELIMITER ); }
	//
	//////////////////////////////////////////////////////////////
	//
	//    所与の資料キーがキーリストにあるかどうか調べて返す
	//
	//////////////////////////////////////////////////////////////
	//
	public boolean isUseThisRefKey(String ref_key){
		Csv keys	=	refKeys();
		int n		=	keys.size();
		for(int i=0; i<n; i++){
			if( ref_key.equals( keys.get(i) ) ){
				if(LOG.fa) LOG.println("☆ キー " + ref_key + " は、このセクション(" + te_lec_sect_key() + " )で使われています");
				return true;
			}
		}
		if(LOG.fa) LOG.println("キー " + ref_key + " は、このセクション(" + te_lec_sect_key() + " )で使われていません");
		return false;
	}
	//////////////////////////////////////////////////////////////
	//
	//    所与の資料キーをキーリストに追加する
	//
	//////////////////////////////////////////////////////////////
	//
	public String addRefKey(String ref_key){
		if(LOG.fa) LOG.println("addRefKey() の先頭です");
		//
		String new_refList	= "";
		String old_refList	= ref_list();
		if(isEmpty(old_refList)){
			new_refList = ref_key;
		}else{
			new_refList = old_refList + DELIMITER + ref_key;
		}
		set_ref_list(new_refList);
		//
		if(LOG.fa) LOG.println("addRefKey() で資料キーを追加しました");
		if(LOG.fa) LOG.println("現在の資料キーリストは " + ref_list() + " です");
		//
		return ref_list();
	}
	// 空白チェック
	boolean	isEmpty(String s){
		if((s==null) || (s.length()==0))	return true;
		return false;
	}
	//////////////////////////////////////////////////////////////
	//
	//    所与の課題キーがキーリストにあるかどうか調べて返す
	//
	//////////////////////////////////////////////////////////////
	//
	public boolean isUseThisKadaiKey(String kadai_key){
		if(LOG.fa) LOG.println("isUseThisKadaiKey() の先頭です");
		if(LOG.fa) LOG.println("           kadai_key = " + kadai_key);
		Csv keys	=	kadaiKeys();
		int n		=	keys.size();
		for(int i=0; i<n; i++){
			if( kadai_key.equals( keys.get(i) ) ){
				if(LOG.fa) LOG.println("☆ キー " + kadai_key + " は、このセクション(" + te_lec_sect_key() + " ）で使われています");
				return true;
			}
		}
		if(LOG.fa) LOG.println("キー " + kadai_key + " は、このセクション(" + te_lec_sect_key() + " ）で使われていません");
		return false;
	}
	//////////////////////////////////////////////////////////////
	//
	//    所与の課題キーをキーリストに追加する
	//
	//////////////////////////////////////////////////////////////
	//
	public String addKadaiKey(String kadai_key){
		if(LOG.fa) LOG.println("addKadaiKey() の先頭です");
		//
		String new_kadaiList	= "";
		String old_kadaiList	= kadai_list();
		if(isEmpty(old_kadaiList)){
			new_kadaiList = kadai_key;
		}else{
			new_kadaiList = old_kadaiList + DELIMITER + kadai_key;
		}
		set_kadai_list(new_kadaiList);
		//
		if(LOG.fa) LOG.println("□addKadaiKey() で課題キーを追加しました");
		if(LOG.fa) LOG.println("□現在の課題キーリストは " + kadai_list() + " です");
		//
		return kadai_list();
	}
	
	/*
	 * 所与の課題キーをキーリストに追加して、更新する
	 */
	public void addKadaiKey(String kadai_key,Database db) {
		addKadaiKey(kadai_key);
		update(db);
	}
	
	
	//
	///////////////////////////////////////////////////
	//
	//　　レコードの値を返す
	//
	///////////////////////////////////////////////////
	//
	// セクションのフルキーを返す（課題等の検索用）
	public String te_lec_sect_key()	{	return  te_lec_key() + "-" + sect_key(); }
	//
	public String te_lec_key()		{	return (String)rec.get(TE_LEC_KEY); }
	public String sect_key()		{	return (String)rec.get(SECT_KEY); 	}
	public String seq_number()		{	return (String)rec.get(SEQ_NUMBER); }
	//
	public String title()			{	return (String)rec.get(TITLE); 		}
	public String content()			{	return (String)rec.get(CONTENT); 	}
	public String note()			{	return (String)rec.get(NOTE); 		}
	public String ref_list()		{	return (String)rec.get(REF_LIST); 	}
	public String kadai_list()		{	return (String)rec.get(KADAI_LIST); }
	//
	///////////////////////////////////////////////////
	//
	//　　レコードの値をセットする
	//
	///////////////////////////////////////////////////
	//
	public void set_te_lec_key(String str)		{ rec.set(TE_LEC_KEY,str); 	}
	public void set_sect_key(String str)		{ rec.set(SECT_KEY,str); 	}
	public void set_seq_number(String str)		{ rec.set(SEQ_NUMBER,str); 	}
	public void set_title(String str)			{ rec.set(TITLE,str); 		}
	public void set_content(String str)			{ rec.set(CONTENT,str); 	}
	public void set_note(String str)			{ rec.set(NOTE,str); 		}
	public void set_ref_list(String str)		{ rec.set(REF_LIST,str); 	}
	public void set_kadai_list(String str)		{ rec.set(KADAI_LIST,str); 	}
	//
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Vector から資料キーリストを作成する
	// null または 空 の場合 "" を返す
	public String make_ref_list(Vector list){
		if(list==null)		{ LOG.errStop("KamokuSecDefRecord: set_ref_list(Vector list)　でlist が null である"); return "";}
		if(list.size()==0)	{ LOG.errStop("KamokuSecDefRecord: set_ref_list(Vector list)　でlist が空である");     return "";}
		//
		int			n 	= list.size();
       	boolean 	fl 	= false;
		StringBuffer bf = new StringBuffer(1000);
		//
		for(int i=0; i<n; i++){
       	    if(fl){ bf.append( DELIMITER ); }
			bf.append( (String)list.get(i) );
			fl = true;
		}
       	String str = bf.toString();
		return str;
	}
	// Vector から資料リストを作成して登録する
	public void set_ref_list(Vector list){
		String str = make_ref_list(list);
		set_ref_list(str);
	}
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// Vector から課題キーリストを作成する
	// null または 空 の場合 "" を返す
	public String make_kadai_list(Vector list){
		if(list==null)		{ LOG.errStop("KamokuSecDefRecord: set_kadai_list(Vector list)　でlist が null である"); return "";}
		if(list.size()==0)	{ LOG.errStop("KamokuSecDefRecord: set_kadai_list(Vector list)　でlist が空である");     return "";}
		//
		int			n 	= list.size();
       	boolean 	fl 	= false;
		StringBuffer bf = new StringBuffer(1000);
		//
		for(int i=0; i<n; i++){
       	    if(fl){ bf.append( DELIMITER ); }
			bf.append( (String)list.get(i) );
			fl = true;
		}
       	String str = bf.toString();
		return str;
	}
	// Vector から課題リストを作成して登録する
	public void set_kadai_list(Vector list){
		String str = make_kadai_list(list);
		set_kadai_list(str);
	}
    /*
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	// 受け取った Vector 内のキーリストと、現在のキーリストを比較し、
	// 異なっている場合には受け取ったもので更新する
	public void updateRefkeyList(Vector newlist ,Database db){
		updateRefkeyList(newlist, db,false);
	}
	public void updateRefkeyList(Vector newlist,Database db,boolean test){
		//
		String  currentListString	= ref_list();
		String  newListString		= make_ref_list(newlist);
		if(!currentListString.equals(newListString)){
			//
			if(test){
				DBG.println("★ 資料キーリストが変化しています");
				DBG.println("　 -- current list ---> " + currentListString);
				DBG.println("　 -- new     list ---> " + newListString);
			}else{
				set_ref_list(newListString);	// キーリストを書き換え
				update(db);						// 実際にデータベースも書き換える
			}
		}else{
			if(test){
				DBG.println("☆ 資料キーリストには変更はありませんでした");
				DBG.println("　 -- current list ---> " + currentListString);
				DBG.println("　 -- new     list ---> " + newListString);
			}
		}
	}
	*/
}