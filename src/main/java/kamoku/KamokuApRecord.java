/*
  講義実施レコードの処理をカプセル化する 
*/
//import java.text.*;
package kamoku;

import java.io.File;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Vector;
import database.Database;
import epml.tools.Regularizer;
import framework.LOG;
import framework.PCvar;
import framework.Param;
import meibo.Meibo;
import tktools.Csv;
import tktools.Gear;
/*
■講義実施データ
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
public class KamokuApRecord extends Object
{
    //
	// 定数
	public static int	TEUID	= 0;	// teuid         8 桁
	public static int	AP_LEC  = 1;	// applec_key    3 桁
    public static int	LEC		= 2;	// lec_key       3 桁
	public static int	MEIBO	= 3;	// 名簿ファイル名
    public static int	SHUBETSU= 4;	// 実施種別
    public static int	WDATE	= 5;	// 実施曜日または番号
    public static int	WORDER	= 6;    // 時限
    public static int	YYYY	= 7;	// 実施年度
    public static int	TERM	= 8;	// 実施期
    public static int	UNIT	= 9;	// 実施期
    public static int	TITLE	= 10;	// 科目名（冗長情報／時間割表示用）
	//
    public static int	MIN	= TEUID;	// 最小番号
	public static int	MAX	= TITLE;	// 最大番号
	//
	Vector 		rec;
	Database	db;
	//
	public Meibo		m1 = null;
	public Meibo		m2 = null;
	public Meibo		m3 = null;
	
	
//	public Vector get_rec_of_KamokuApRecord() {
//		return rec;
//	}
	///////////////////////////////////////////////////
	//
	//   コンストラクタ
	//
	///////////////////////////////////////////////////
	//
	//  空のレコードを生成する
	//
	public KamokuApRecord(){
		//
		Vector w = new Vector(20,10);
		rec	= copy(w);
	}
	//
	// Vector から生成する
	//    レコード中に null があれば "" にする
	//
	public KamokuApRecord(Vector w){
		//
		db	= null;
		rec	= copy(w);
        if(LOG.fa) LOG.outVector(rec,"コンストラクタKamokuApRecord()の結果です（Vectorから生成）");
	}
	//
	// キーでデータベースを検索して生成する
	//
	// 　　	teUid が null なら全て "" のレコードを生成する
	//		レコード中に null があれば "" にする
	//
	//
	//      ※ 該当レコードがデータベース中にないときは空のレコードが生成される
	// 
	public KamokuApRecord(String te_aplec_key,Database _db){
		Csv		cs			=	new	Csv(te_aplec_key, "-");
		String	teUid		=	cs.get(0);
		String	aplec_key	=	cs.get(1);
		
		db = _db;
		rec	= new Vector (20,10);
		for(int i=MIN; i<=MAX; i++){
			rec.add(i,"");
		}
		if(teUid != null){
			Vector w = db.getApLecture(teUid, aplec_key);
			if(LOG.fa) LOG.outVector(w,"データベース検索直後の KamokuApRecord ");
			//
			rec		 = copy(w);
			if(LOG.fa) LOG.outVector(rec,"w を整形した KamokuApRecord ");
		}		
		
	}
	public KamokuApRecord(String teUid,String aplec_key,Database _db){
        if(LOG.fa) LOG.println("コンストラクタKamokuApRecord()の先頭です（ＤＢから生成）" + "/ teUid=" + teUid + " ,aplec_key=" + aplec_key);
		//
		db = _db;
		rec	= new Vector (20,10);
		for(int i=MIN; i<=MAX; i++){
			rec.add(i,"");
		}
		if(teUid != null){
			Vector w = db.getApLecture(teUid, aplec_key);
			if(LOG.fa) LOG.outVector(w,"データベース検索直後の KamokuApRecord ");
			//
			rec		 = copy(w);
			if(LOG.fa) LOG.outVector(rec,"w を整形した KamokuApRecord ");
		}
	}
	
	public Vector getRecord() {
		return rec;
	}
	
	
	//
	//  全ての要素を "" に初期化したのち、null でないものだけをコピーする
	//  コピーは、レコードサイズを超えて行われることはない
	//
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
	
	///////////////////////////////////////////////////
	//
	//　　データベースの更新
	//
	///////////////////////////////////////////////////
	//
	// データベースに書き込む（更新）
	public int insert(Database db){
		Vector v = copy(rec);					// null を書き出さないように
		int    n = db.insertApLecture( v ); 
		return n;
	}
	public int update(Database db){
		Vector v = copy(rec);					// null を書き出さないように
		int    n = db.updateApLecture( v ); 
		return n;
	}
	public int delete(Database db){
		int    n = db.deleteApLecture( teuid(),aplec_key() );
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
	public void set_aplec_key(String str)	{ rec.set(AP_LEC,str); 	}
	public void set_lec_key(String str)	{ rec.set(LEC,str); 	}
	public void set_meiboFname(String str)	{ rec.set(MEIBO,str); 	}
	public void set_shubetsu(String str)	{ rec.set(SHUBETSU,str);}
	public void set_wdate(String str)		{ rec.set(WDATE,str);   }
	public void set_worder(String str)		{ rec.set(WORDER,str);  }
	public void set_yyyy(String str)		{ rec.set(YYYY,str);    }
	public void set_term(String str)		{ rec.set(TERM,str);    }
	public void set_unit(String str)		{ rec.set(UNIT,str); 	}
	public void set_title(String str)		{ rec.set(TITLE,str); 	}
	//
	///////////////////////////////////////////////////
	//
	//　　レコードの値を返す
	//
	///////////////////////////////////////////////////
	//
	public Meibo	getM1(){ return m1;}
	public Meibo	getM2(){ return m2;}
	public Meibo	getM3(){ return m3;}
	//
	//  レコードは空かどうか。割り付けられている科目キーの値があるかどうかで判断する
	//
	public boolean isEmpty(){
		if(lec_key().length()==0) return true;
		return	false;
	}
	public	Database getDB()	{return db;}
	//
	//
	public boolean isEmpty(String str){
		if((str==null)||(str.length()==0)) return true;
		return	false;
	}
	//
	public String teuid()		{	return (String)rec.get(TEUID); 		}
	public String teUid()		{	return (String)rec.get(TEUID); 		}	// 安全策
	//
	public String aplec_key()	{	return (String)rec.get(AP_LEC); 	}
	public String lec_key()		{	return (String)rec.get(LEC); 		}
	public String meiboFname()	{	return (String)rec.get(MEIBO); 		}
	public String shubetsu()	{	return (String)rec.get(SHUBETSU); 	}
	public String wdate()		{	return (String)rec.get(WDATE); 		}
	public String worder()		{	return (String)rec.get(WORDER); 	}
	public String yyyy()		{	return (String)rec.get(YYYY); 		}
	public String term()		{	return (String)rec.get(TERM); 		}
	public String unit()		{	return (String)rec.get(UNIT); 		}
	public String title()		{	return (String)rec.get(TITLE); 		}
	//
	//
	// 年度を半角数字で返す
	// 2004 または 04 であれば、返値は "2004" となる
	// それ以外では "" を返すので受け取り側でチェックする
	public String	getYear(){
		Regularizer	rgl	=	new Regularizer();
		String		yy	=	rgl.toDigit( rgl.removeSpace( yyyy() ) );	// 空白文字を削除
		if(Gear.isEmpty(yy))	return	"";
		if(yy.length() != 4){
			if(yy.length() == 2)	return	"20" + yy;
			return	"";
		}
		return	yy;
	}
    // そのクラスの名簿ファイルオブジェクト( Meibo )を返す
	//
	//     □ Param class #getClassDir( teUid ) を使う
	//
    public Meibo getMeibo(Param para){
        if(LOG.fa) LOG.println("KamokuApRecord class #getMeibo() の先頭です");
		//
		// 名簿クラスを生成して返す
		//   名簿ファイル名が記録されていない場合（初期状態）では null を返すので注意！！
		//
		if(!isEmpty(meiboFname()) ){
			if(LOG.fa) LOG.println("KamokuApRecord class #getMeibo() / 名簿ファイル名は " + meiboFname() + " です");
			return new Meibo( para.getClassMeiboDir( teUid(), aplec_key()) +  meiboFname() );
		}else{
			if(LOG.fa) LOG.println("KamokuApRecord class #getMeibo() / 名簿ファイル名は登録されていません。null を返します。");
			return null;
		}
    }
    // クラスキーに応じた名簿ファイル名を返す
    public String getMeiboName(String key){
        return meiboFname(); // 名簿ファイル名
    }
    // クラスキーに応じた名簿ファイル名をフルパスで返す
    public String getMeiboPath(Param para){
        return para.getClassMeiboDir( teUid(), aplec_key()) + meiboFname();
    }
    /**
     * ************************
     * 名簿ファイル処理（新規）
     * ************************
     *
     * 名簿ファイルを受け取り、その正当性を検証した上で格納場所に
     * ファイルを保存する
     * 
     * 検証はヘッダ行と全ての名簿データについて行うので、ここで
     * 格納された名簿ファイルは正当なものであることが保障される 
     * 
     * @param out
     * @param htb
     * @param para
     * @return
     */
	public boolean addMeibo(PrintWriter out,Hashtable htb,Param para){
        if(LOG.fa)  LOG.outHash(htb,"KamokuApRecord class #addMeibo() の先頭です");
		//
		String teUid		= teUid();
		String szDB			= (String) htb.get("_szDB");
		String aplec_key	= strHash(htb,"_aplec_key");
		//
		// マルチパート入力で受け取った変数を得る
        String savedir  = (String)htb.get("_savedir");  		// 一時的格納場所 2003.6.15
        String mfilename = (String)htb.get("_onlyFilename");	// ファイル名のみ
        String meiboFile = (String)htb.get("_fileName");		// savedir + onlyFilename == 一時記録ファイルへのフルパス
        String files	 = (String)htb.get("_counts");			// マルチパートで読み込んだファイル数
		//
		boolean eflag    = false;
		int		fcnt	 = Integer.parseInt(files);
		//
        if(mfilename!=null){
            String ext	= getTriler(mfilename);
			if( !ext.equals("csv") ) {
				htb.put("_msg","★ 名簿ファイルは csv ファイルでなくてはなりません");
               	eflag = true;

			}else{
				File mfp   = new File(meiboFile);
	            if(mfp.length()==0){    // ファイルの長さがゼロ
    	            htb.put("_msg","★ 指定されたファイルは存在しません");
        	        eflag = true;
            	}
			}
        }else{
			eflag = true;
			htb.put("_msg","★ ファイル名を指定してください");
        }
        if(eflag){// エラーありの場合

            // もし送信された名簿ファイルがあれば削除する
            if((meiboFile != null)&&(meiboFile.length() > 0)){
                File saveDir    = new File(savedir);
                if(saveDir.exists()){
                    deleteDir(saveDir);
                }
            }
            return false;
        }// ** ここで終わり **

        if((meiboFile != null)&&(meiboFile.length() > 0)){
		    /*
		     * Meiboオブジェクトを生成してみて、ファイルの正当性を検証する
		     * ヘッダ及びファイル内容の正しさまで検証される
		     */
		    Meibo	mtest = new Meibo(meiboFile);
			if(mtest.isInvalid()){
				/*
				 * ファイルを削除する
				 */
	            if((meiboFile != null)&&(meiboFile.length() > 0)){
	                File saveDir    = new File(savedir);
	                if(saveDir.exists()){
	                    deleteDir(saveDir);
	                }
	            }
				htb.put("_msg","★ 名簿ファイルの形式が正しくありません");
	            return false;
			}
			/*
			 * 名簿ファイルに教師ID記述がなければ追加し、
			 * ファイルそのものを更新しておく
			 */
			Hashtable	teacher	=	new	Hashtable();
			db.MembersInfo(teUid, teacher);
			String		tname	=	Gear.strHash(teacher, PCvar.UNAME);
			String		tmail	=	Gear.strHash(teacher, PCvar.MAIL);
			mtest.complete(teUid, tname, tmail);
			
			/*
			 * ファイルを格納場所に受け取る
			 */
			File 	mfp    = new File(meiboFile);						// 一時格納場所（ここにある）
            String  dir    = para.getClassMeiboDir(teUid, aplec_key);	// 最後に '/' 付き
			File    moveTo = new File(dir);
			if(!moveTo.isDirectory()){	// ディレクトリがなければ作る
				moveTo.mkdirs();
			}else{
				deleteFiles(moveTo);	// 移動先の(dir)のファイルを全て消してから移動処理を行う
			}
			boolean flag   = mfp.renameTo(new File(moveTo,mfp.getName()));
           	deleteDir(new File(savedir));// 一時格納ファイルをディレクトリごと削除する
			if(!flag){
			    LOG.errStop("KamokuApRecord class #addMeibo() :指定されたファイルをコピーできない");
				return false;
            }
			/*
 			 * 科目実施レコードに名簿ファイル名を書き込む
 			 */
			set_meiboFname(mfilename);
			int ok = update(db);
        }
		//
		return true;
	}
    /**
     * ************************
     * 名簿ファイル処理（更新）
     * ************************
     *
     * 名簿ファイルを受け取り、その正当性を検証した上で格納場所に
     * ファイルを保存する
     * 
     * 検証はヘッダ行と全ての名簿データについて行うので、ここで
     * 格納された名簿ファイルは正当なものであることが保障される 
     * 
     * @param out
     * @param htb
     * @param para
     * @return
     */
    public boolean updateMeibo(PrintWriter out,Hashtable htb,Param para){
        if(LOG.fa)  LOG.outHash(htb,"KamokuApRecord class #updateMeibo() の先頭です");
		//
		String teUid	= teUid();
		String szDB		= (String) htb.get("_szDB");
		String aplec_key	= strHash(htb,"_aplec_key");
		//
		// マルチパート入力で受け取った変数を得る
        String savedir  = (String)htb.get("_savedir");  		// 一時的格納場所 2003.6.15
        String mfilename = (String)htb.get("_onlyFilename");	// ファイル名のみ
        String meiboFile = (String)htb.get("_fileName");		// savedir + onlyFilename == 一時記録ファイルへのフルパス
        String files	 = (String)htb.get("_counts");			// マルチパートで読み込んだファイル数
		//
		boolean eflag    = false;
		int		fcnt	 = Integer.parseInt(files);
		//
        if(mfilename!=null){
            String ext	= getTriler(mfilename);
			if( !ext.equals("csv") ) {
				htb.put("_msg","★ 名簿ファイルは csv ファイルでなくてはなりません");
               	eflag = true;

			}else{
				File mfp   = new File(meiboFile);
	            if(mfp.length()==0){    // ファイルの長さがゼロ
    	            htb.put("_msg","★ 指定されたファイルは存在しません");
        	        eflag = true;
            	}
			}
        }else{
			htb.put("_msg","★ ファイル名を指定してください");
			eflag = true;
		}
        if(eflag){
            /*
             * 送信された名簿ファイルがあれば削除する
             */
            if((meiboFile != null)&&(meiboFile.length() > 0)){
                delDir(savedir);	// 作業用のディレクトリとファイルがあれば再帰的に消す 2003.10.28
            }
            return false;
		}// ここでおしまい

        if((meiboFile != null)&&(meiboFile.length() > 0)){
		    /*
		     * Meiboオブジェクトを生成してみて、ファイルの正当性を検証する
		     * ヘッダ及びファイル内容の正しさまで検証される
		     */
		    Meibo	mtest = new Meibo(meiboFile);
			if(mtest.isInvalid()){
				/*
				 * ファイルを削除する
				 */
                File saveDir    = new File(savedir);
                deleteDir(saveDir);
				htb.put("_msg","★ 名簿ファイルの形式が正しくありません");
	            return false;
			}
			/*
			 * 名簿ファイルに教師ID記述がなければ追加し、
			 * ファイルそのものを更新しておく
			 */
			Hashtable	teacher	=	new	Hashtable();
			db.MembersInfo(teUid, teacher);
			String		tname	=	Gear.strHash(teacher, PCvar.UNAME);
			String		tmail	=	Gear.strHash(teacher, PCvar.MAIL);
			mtest.complete(teUid, tname, tmail);
			
            /*
             * 新旧ファイルの差分リストを作成する（Vector vdel,Vectror vadd）
             */
            String	currentPath	= getMeiboPath(para);   	// 現在の名簿ファイル（フルパス）
            String	current     = meiboFname();    			// 現在の名簿ファイル（ファイル名のみ）
            Meibo 	mNew 		= new Meibo(meiboFile);		// meiboFile   = savedir + onlyFilename == 一時記録ファイルへのフルパス true はチェック生成モード
			Meibo 	mOld 		= new Meibo(currentPath);	// currentPath = 現在の名簿ファイル（フルパス）
			m1 					= mNew.dif_deleted(mOld); 	// なくなった学生データ
			m2 					= mNew.dif_added(mOld);   	// 追加された学生データ
			m3 					= mNew.dif_modified(mOld);	// 変更された学生データ
            /*
             * 現在の名簿ファイルを削除する（同名かもしれないので）
             */
			File curFile = new File(currentPath);
            curFile.delete();
			/*
			 * ファイルを格納場所に受け取る
			 */
			File    	mfp     = new File(meiboFile);						// meiboFile = savedir + filename (一時保留場所のファイルへのフルパス)
			String  	dir     = para.getClassMeiboDir(teUid,aplec_key);	// 例： /pc/kawaba01/classes/103/
            File		moveTo 	= new File(dir);
            boolean	flag   	= mfp.renameTo(new File(moveTo,mfp.getName()));
            delDir(savedir); // 作業ディレクトリは消しておかねばならない 2003.10.28
            if(!flag){
				return false;
            }
 			/*
 			 * 科目実施レコードに名簿ファイル名を書き込む
 			 */
			set_meiboFname(mfilename);
			int ok = update(db);

        }
		return true;
	}
    //
    //      ユーティリティ
    //
	//
	// ファイル拡張子を取り出す
	String getTriler(String filename){
		Csv chk = new Csv(filename,".");
		if(chk.size() <= 1) return "";	// 拡張子がないとき
		//
		return chk.get(chk.size() -1);	// 最後の要素
	}
	//-------------------------- ファイル削除 --------------------------------
	boolean makeDir(String dir){
		return (new File(dir)).mkdirs();
	}
	boolean delDir(String dir){
		boolean flag = true;
		//
		File fp = new File(dir);
		if(fp.exists()){
			flag = deleteDir(fp);
		}
		return flag;
	}
    // 再帰的にファイルとディレクトリを消す
	boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
	}
    // 再帰的にファイルを消す（親ディレクトリは消さない）
	boolean deleteFiles(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return true;
	}
	String strHash(Hashtable htb,String key){
		//
		String	str	= (String) htb.get(key);
		if(str==null){
			LOG.println("★★★★★★★★★★★★★★★★★★★★★★★★★★★");
			LOG.println("★★   　　　　　　　　　　　　　　　　　　　　   ★★");
			LOG.println("★★   ハッシュから取り出したデータは NULL です   ★★");
			LOG.println("★★   key = " + key );
			LOG.println("★★★★★★★★★★★★★★★★★★★★★★★★★★★");
		}
		return str;
	}
}