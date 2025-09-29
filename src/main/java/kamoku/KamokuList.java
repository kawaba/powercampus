/*
 	科目作成・一覧
 	バージョン 2.0
*/
package kamoku;

import tktools.*;

import java.io.*;
import java.util.*;

import database.*;
import export.*;
import framework.*;
import	cabinet.*;
import	xmlparser.*;

/**
 *
 *
 	#
	# ##################
	#   KamokuList
	# ##################
	#
	<program $kamoku.KamokuList>
		<dispatch  html=kamokuList.html  number=200  class=kamoku.KamokuList />
		<variable>
		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA/>
		  <accept    CMD    UPLODE  lec_key/>
		  <keep      />
		  <form      />
		</variable>
	</program> 
 */
 
public class KamokuList extends SuperPlayer implements CBvar{

	/* ************ 
	 *  作業用変数
	 **************/
	/**
	 *  プログラムリターンコード 
	 */
	String		ret;
	/**
	 * 次の処理での表示モード 
	 */
	String		disp_mode;
	/**
	 *  処理分岐コード
	 */
	String 		cmd;
	/**
	 * データベースプール
	 */
	DbConnectionBroker	broker;
	/**
	 * ユーザー検索のためのクラス
	 */
	Database			db;
	
	public	KamokuList(){
		super();
		if(LOG.fa) LOG.println("■ Sample #コンストラクタ");
	}	

	/**
	 * コンストラクタが引数をもてないので、コンストラクタで行う内容をこの initialize() に書く．
	 * out, htb, para は SuperPlayer クラスのインスタンス変数なので、
	 * 引数にとらずとも利用できるが、変数を明示する意味で列挙してある
	 * 
	 * @param out			出力ポインタ
	 * @param htb			システムハッシュ
	 * @param para			パラメータ
	 */
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		broker	=	getDbConnection();
		db		=	new Database(broker);			

	}
	/**
	 * 受け入れコードにより処理を分岐させるメソッド．
	 * 受け入れコードは getParameter(CMD) で受け取る
	 * 
	 * 終了コードとして次に起動したいプログラムエイリアス（論理プログラム名）を指定する
	 * 論理プログラム名は dispatch.xml に指定したものを使う
	 * 
	 * 自分自身の画面を再表示したい場合は終了コードに DISPATCH_DEFAULT を指定する
	 * 呼び出しプログラムへ復帰するには終了コードに   DISPATCH_RETURN  を指定する
	 *
	 * disp_mode とは
	 * 　　次にこの画面を表示するときの表示モード(DISP_NEW=新規表示、DISP_EDIT=編集表示)．
	 * 　　あるいは次に起動するプログラムで使われる表示モード
	 *　（注）
	 * 　　他のプログラムが終了して、DISPATCH_RETURN でこのプログラムが呼び出される時は 
	 * 　　コントローラーにより display(boolean disp_mode) へ直接復帰する．
	 * 　　この場合、disp_mode はコントローラーによってDISP_EDIT に設定されている 
	 */	
	public	String	dispatch(){
		if(LOG.fa) LOG.outHash(htb,"■KamokuList #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		// 科目レコードの追加または訂正
		if(cmd.equals("CREATE")){
			putParameter("lec_key","");	// 念のため
			ret			=	"$kamoku.KamokuEdit";
			disp_mode	=	DISP_NEW;				
				
		// 科目キーはシステムハッシュにセット済み
		}else if(cmd.equals("EDIT")){
			ret			=	"$kamoku.KamokuEdit";
			disp_mode	=	DISP_NEW;				
	
		// 科目レコードの削除
		}else if(cmd.equals("DELETE")){
			//
			// レコードの削除
			String	szDB	=	getParameter(GROUP);
			String	teUid	=	getParameter(TUID);
			String	lec_key	=	getParameter("lec_key");
			//
			// 関連の定義データや実施データ、実施科目のディレクトリなど
			// 全てを削除する
			KamokuDelete	kdel	= new KamokuDelete(db);
			kdel.delete_kamokuInfo(szDB,teUid,lec_key,para);
			//
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_NEW;
			
		// 実施計画作成画面へ移る
		}else if(cmd.equals("PLAN")){
			ret			=	"$kamoku.KamokuPlan";
			disp_mode	=	DISP_NEW;
			
		// 科目データインポートへ
		}else if(cmd.equals("IMPORT")){
			ret			=	"$export.Import";
			disp_mode	=	DISP_NEW;
			
		// 科目データをエクスポートする
		}else if(cmd.equals("EXPORT")){
			export(getParameter(TUID), getParameter("lec_key"));
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;				
			
		// キャビネットを開く
		}else if(cmd.equals("CABINET")){
			ret			=	"$cabinet.FileCabinet";
			disp_mode	=	DISP_NEW;				
			
		// 科目データをレストアする
		}else if(cmd.equals("RESTORE")){
			ret			=	"$export.Restore";
			disp_mode	=	DISP_NEW;

		// 科目定義XMLファイルを受け取る
		}else if(cmd.equals("SEND_XML")){
			putXmlFile();
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;	
			

		// 戻る
		}else if(cmd.equals("RETURN")){
			ret			=	DISPATCH_RETURN;
			disp_mode	=	DISP_EDIT;
			
		}else{
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;	
		}
		/*
		 * 次に表示するときあるいはリターンしてきた時の表示モードをセットしてからリターンする
		 */
		putParameter(DISP_KEY,disp_mode);
		return	ret;
	}
	/**
	 * エクスポート処理を行う
	 * @param teUid
	 * @param lec_key
	 */
	void export(String	teUid, String lec_key){
		if(LOG.fa) LOG.outHash(htb,"KamokuList#export() : エクスポート処理を行う の先頭です");
		//
		KamokuExport	ke	=	new	KamokuExport(teUid, lec_key, db, para);
		ke.makeKamokuZipOnDownloadArea();
		
		Hashtable	tb		=	getCB_UpdateRecord(ke.getFileName());
		if(tb==null){
		    insert(ke);
		}else{
		    update(tb);
		}
		putParameter(MESSAGE, "■科目定義ファイルをファイルキャビネットに登録しました．");

	}
	void	insert(KamokuExport ke){
		
	    Hashtable	tb		=	makeCabinetRecord(ke);
		CBdatabase 	cb		=	new	CBdatabase(broker, getParameter(GROUP));
		cb.insertByid(tb, getParameter(TUID));

	}
	public Hashtable makeCabinetRecord(KamokuExport ke){
		
		
		Hashtable	tb		=	new	Hashtable();
		CBkey		cbkey	=	new	CBkey(broker);
		String		key		=	cbkey.getNextNumber(getParameter(GROUP));	
		File		fp		=	new File(ke.getFilePath());
		long		len		=	(fp.length() + 512 )/ 1024;
		if(len==0)	len	=	1;
		GregorianCalendar	to	=	DateGear.calculateMonth(DateGear.currentDay(),1,true);

		putParameter(tb,KEY		, key);
		putParameter(tb,SUBJECT	, "科目定義データ（" + ke.kamokuName() +"）");
		putParameter(tb,OWNER	, getParameter(TUID));
		putParameter(tb,FNAME	, ke.getFileName());
		putParameter(tb,KUBUN	, "zip");
		putParameter(tb,SIZE	, String.valueOf(len)+"KB");	
		putParameter(tb,PATH	, ke.getFilePath());
		putParameter(tb,URL		, ke.getFileURL());
		putParameter(tb,FROM	, (DateGear.getLongDateString(  )).substring(0,12));	// 今日の日付（yyyyMMddHHmm）
		putParameter(tb,IDX		, "001");	// 個人用
		
		return	tb;
	}

	
	/**
	 * 日付を更新してレコードをアップデートする
	 * 
	 * zipファイルはすでに新しく作成されているので、日付を更新するだけでいい
	 * @param ke
	 */
	void	update(Hashtable record){
	    
	    String		seqkey	=	getParameter(record, KEY);
		CBdatabase	cbd		=	new CBdatabase(broker, getParameter(GROUP));
		cbd.update(seqkey, record);	    
	    
	}
	/**
	 * 更新レコードを得る
	 * 存在チェックを兼ねる
	 * 
	 * @param fname
	 * @return
	 */
	Hashtable	getCB_UpdateRecord(String fname){
		if(LOG.fa)	LOG.println("■ RefLidt #mkUpdateRecord() :" + fname);
		
		Hashtable	record	=	new	Hashtable(30);
		CBdatabase	cbd		=	new CBdatabase(broker, getParameter(GROUP));
		int			count	=	cbd.readRecord(fname, record);
		if(count==0){
		    return	null;
		}
		// 更新日付の書き換え
		String		from	=	DateGear.getLongDateString();	// yyyyMMddHHmmss (14桁)
		putParameter(record, FROM, from.substring(0,12));		// yyyyMMddHHmm(12桁)
		
		return	record;
	}	
	/**
	 * メール本文のテンプレートを読み出し、内容を置き換えて返す
	 * 
	 * @return
	 */
	String	getMailMessage(String kamokuName){
		
		String	url	=	"http:/" + para.getDownloadURL(getParameter(TUID));
		
		// teName tiltle kamokuUrl
		Hashtable	ht		=	new	Hashtable();
		ht.put("_teName"		,getParameter(TNAME));
		ht.put("_kamokuName"	,kamokuName);
		ht.put("_kamokuUrl"		,url);
		
		String	filePath	=	para.get("kamokuExportMail");
		String	message		=	FileGear.getFileData(filePath);
		
		return	substitute(message, ht);
	}
	/**
	 * 科目定義情報ファイル名を得る<br>
	 * <br>例示： <te_lec_key>.zip
	 *  
	 * @param teUid		教師ID
	 * @param lec_key	科目キー
	 * @return			ファイル名
	 */
	public	String	getKamokuInfoFileName(String teUid, String lec_key){
		
		String	te_lec_key	= KeyGen.get_te_lec_key2(teUid, lec_key);	
		String	path		= te_lec_key + ".zip";
		return	path;
	}
    /**
     * コースオブジェクトをファイル出力する
     * @param path
     * @param crs
     * @return
     */
    public	String	writeCourse(Course crs, String path){	
	
		ObjectOutputStream  objOut  = null;
        try{
            objOut = new ObjectOutputStream(new FileOutputStream(path));
            try{
                objOut.writeObject(crs); // Serializable
                objOut.flush();
                objOut.close();
                htb.put("_export","ok");
				//
            }catch(IOException e1){
        		htb.put("_export","error");
		    }
        }catch(IOException e2){
        	htb.put("_export","error");
		}
		return path;
    }
    /**
     * 科目定義xmlファイルを受け取って個人用フォルダに置く
     *
     * 個人用フォルダは /home/group/ 以下にある
     */
    public	void	putXmlFile(){
    	
    	String	filename	=	para.userSyllbusPath(getParameter(GROUP), getParameter(TUID));
    	File	fp			=	new File(filename);
    	String	fname		=	getFile(fp.getParent());// ファイル受け取り
    	
    	// ファイル名が違っている時は正しい名前に直す
    	if(!fname.equals("syllabus.xml")){
    		File	old	=	new	File(fp.getParent() + FS + fname);
    		old.renameTo(fp);
    	}
    	/*
    	 * ファイル形式を確かめてserial を得る
    	 */
    	try{
        	String			xml	=	FileGear.getFileData(filename);
    		KamokuParser	kp	=	new KamokuParser(xml);
    		putParameter(MESSAGE,"★ファイルを受け取りました．[serial="+ kp.getSerial() + "]");
    		
    	}catch(xmlException e){
    		putParameter(MESSAGE,"★科目定義Xmlとして不正なファイルです．");
    		System.out.println(e.getMessage());
    		FileGear.deleteFiles(fp.getParent());// ファイル削除
    	}
    	return;
    }
	/**
	 * アップロードされたファイルをプロファイルディレクトリへ移す
	 * 複数ファイルの同時アップロードに対応している
	 * アップロード先のファイル記述子を返す
	 */
	String	getFile(String toDir){
		if(LOG.fa)	LOG.println("■ BbsMakePost #getFile()");
		/*
		 * 戻り値に使うのでファイル名を取得しておく
		 */
		String		fromDir	=	getParameter(UPLOAD_DIR_NAME);
		String	[]	files	=	(new File(fromDir)).list();
		String		fname	=	files[0];	// 実際には１個しかない
		/*
		 *   /home/pc/temp/[ランダムに作成したディレクトリ名]   ⇒　/home/bbs/[owner]/[forum]/[thread]/[Post]/
		 *  ファイルが複数あれば全てを移動する
		 */
		FileGear.deleteFiles(toDir);
		FileGear.moveFiles(toDir, fromDir);
		
		/* 作業ディレクトリが残るので全て消去する */
		Gear.delDir(fromDir);
		
		return	fname;		
	}    
	/*////////////////////////////////////////////////////////////////////////////
	 * 
	 *    表 示 処 理
	 * 
	 * 　　    以下はコントローラーが呼び出す表示メソッドである．
	 * 　　    一般には、このクラス内のメソッドから直接呼び出さない．
	 * 
	 *////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 出力処理
	 */
	public  void	display(boolean editmode){
		if(LOG.fa)	LOG.println("■ KamkuList #display(boolean editmode)");
		if(!editmode){
			putParameter(MESSAGE,"★ 一覧表の中の科目を編集したいときは <span style='color: #6699CC'>科目名</span>をクリックしてください．");
		}
		/* strHash(htb,DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}
    //
    // ハッシュテーブル(htb)を使って部分的に切り取ったソースデータ（exHtml）
    // の内容を置き換えて出力処理する．表などの反復出力に利用するが個々の処理
    // 内容は、key で特定される．
    //
    public void write(String key,Vector exHtml){
        //
    	if(key.equals("noItem")){
			String 		teUid	= getParameter(TUID);
			KamokuDEF	kd		= new KamokuDEF(teUid, db);
			int			n		= kd.size();
			if(n > 0)	return;		// すでに表示した行がある
			//
			printVector(exHtml,htb);
			return;
			
		}else if(key.equals("itemList")){

			String 		teUid	= getParameter(TUID);
			KamokuDEF	kd		= new KamokuDEF(teUid, db);
			int			n		= kd.size();
			// n=0 なら１件も表示しない
			for(int i=0; i<n; i++){
				KamokuDefRecord	kdr	= kd.get(i);
				//
				putParameter("n"		,String.valueOf(i+1) );
				putParameter("title"	,kdr.title() );
				putParameter("lec_key"	,kdr.lec_key() );
				//
				printVector(exHtml,htb);
			}
		}

    }
}

