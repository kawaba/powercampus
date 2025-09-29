/*
 * 作成日: 2005/02/20
 *
 * TODO
 */
package cabinet;

import java.io.*;
import java.util.*;

import kamoku.*;
import tktools.*;
import database.*;
import framework.*;

/**
 *	ファイルキャビネット<br>
 *<br>
 *
 	#
	# ##################
	#   FileCabinet
	# ##################
	#
	<program $cabinet.FileCabinet>
		<dispatch  html=FileCabinet.html  number=2000  class=cabinet.FileCabinet />
		<variable>
		  <receive   NUMBER STAMP GROUP UID MAIL UNAME DIVISION StCLASSINFO />
		  <accept    CMD    UPLODE  te_lec_key seqkey subject/>
		  <keep      />
		  
		  <work      />
		  <form      />
		</variable>
	</program> 
 *
  *
 * 変数の説明
 *
 * 1. receive 
 * 		StCLASSINFO -- 	学生がアクセスした時のみ得られる．その学生がどの講義を受講しているかのCSVデータ．
 * 						例：　"mwb9l124-131,mwb9l124-312"　te_aplec_key のCSV文字列になっている． 
 * 2. accept
 * 		te_lec_key -- ファイル表示範囲の選択（リストボックス）でセットされる
 *  	seqkey  ----- ファイル削除、subject(説明)入力でセットされる
 * 		subject ----- subject(説明)入力でセットされる
 * 
 * 3. keep
 * 4. form
 *
 */
public class FileCabinet extends SuperPlayer implements CBvar{

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
	Database	db;

	/**
	 * ファイルキャビネットへのパス
	 * /home/group/_szDB/_userid/filecabinet/
	 * 
	 */
	String		path;
	
	/**
	 * 教師(=1)、アシスタント(=2)、学生(=3)の別
	 */
	String		division;	
	
	
	public	FileCabinet(){
		super();
		if(LOG.fa) LOG.println("■ FileCabinet #コンストラクタ");
	}
	
	
	/* (非 Javadoc)
	 * @see framwork.SuperPlayer#initialize(java.io.PrintWriter, java.util.Hashtable, framwork.Param)
	 */
	public void initialize(PrintWriter out, Hashtable htb, Param parameter) {
		if(LOG.fa)	LOG.println("■ FileCabinet #initialize()");
		
		broker	=	getDbConnection();
		db		=	new Database(broker);	

		//          /home/group/_szDB/_userid/filecabinet/
		path	=	para.fileCabinetPath(getParameter(GROUP), getParameter(UID));
		
		division	=	getParameter(DIVISION);

	}

	/* (非 Javadoc)
	 * @see framwork.SuperPlayer#dispatch()
	 */
	public String dispatch() {
		if(LOG.fa) LOG.outHash(htb,"■FileCabinet #dispatch()");
		if(LOG.fa)	LOG.println("■ FileCabinet #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		
		if(cmd.equals("UPLOAD")){

			upload();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示

		}else if(cmd.equals("SCOPE")){
			//リストボックスの表示を設定			
			setListBox();	
			
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示

		}else if(cmd.equals("SUBJECT")){
			
			setSubject();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示

		}else if(cmd.equals("DELETE")){
			
			delete();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示

		}else if(cmd.equals("RETURN")){
			
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;	// 復帰			

		}else{
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;	// 復帰	
		}
		/*
		 * 次に表示するときあるいはリターンしてきた時の表示モードをセットしてからリターンする
		 */
		putParameter(DISP_KEY,disp_mode);
		return	ret;
	}
	/**
	 * 学生ユーザーでかつファイルモードがprivate以外かどうか
	 * @return
	 */
	boolean	isClassAndStudent(){
		if(LOG.fa)	LOG.println("■ FileCabinet #isClassAndStudent()");
		
		String	te_lec_key	=	getParameter("scope");

		if(division.equals(DIV_STUDENT)){
			if(!te_lec_key.equals("PRIVATE")){
				if(LOG.fa)	LOG.println("□ 　　　　　true");
				return	true;
			}
		}
		if(LOG.fa)	LOG.println("□ 　　　　　false");
		return	false;
	}
	
	/**
	 * ファイルの表示範囲リストボックスの表示を設定
	 */
	public	void	setListBox(){
		if(LOG.fa)	LOG.println("■ FileCabinet #setListBox()");
		
		String	scope	=	getParameter("scope");	// リストボックスの値
		if(scope.equals("PRIVATE")){
			putParameter("_sel-1", "selected");
			putParameter("_sel-2", "");
			putParameter("_sel-3", "");
		}else if(scope.equals("ALLFILES")){
			putParameter("_sel-1", "");
			putParameter("_sel-2", "");
			putParameter("_sel-3", "selected");
			
		}		
	}
	public	void	upload(){
		if(LOG.fa)	LOG.println("■ FileCabinet #upload()");
		
		// ファイルを受け取る

		String		te_lec_key	=	getParameter("scope");
		if(te_lec_key.equals("ALLFILES")){
			putParameter(MESSAGE, "★全てのファイルを表示している状態ではアップロードは出来ません");
			deleteUploadFile();
			return;
			
		}else if(!te_lec_key.equals("PRIVATE")){
			/*
			 * 学生はクラスにファイルをアップロードできないようにする
			 */
			if(division.equals(DIV_STUDENT)){
				putParameter(MESSAGE, "★学生の権限ではクラスにアップロードは出来ません");
				deleteUploadFile();
				return;
				
			}
		}
		/*
		 * データベースに新規または更新で登録する
		 */
		registTo();
	}
	/**
	 * ファイルキャビネットデータベースに新規または更新で登録する
	 *
	 */
	void	registTo(){
		/*
		 * ファイルの重複を調ておいてから上でファイルを受け取る
		 */
		boolean	dup		=	exists();
		String		fname	=	getUploadedFile();	// 実際にアップロードファイルを得る
		/*
		 * データベースに新規または更新で登録する
		 */
		if(fname!=null){
			if(dup){
				// 重複ファイルは更新
				if(!updateDB(fname)){
					// 過去のバグのために起こりえるので
					//if(LOG.fa)	LOG.println("■ FileCabinet update failed !");
					writeDB(fname);
				}
			}else{
				// データベースに新規登録
				writeDB(fname);
			}
		}
	}
    /**
     * 受け取るファイルと同名のファイルがキャビネットディレクトリの中にすでにあるかどうか
     * @return		すでに同名のファイルがある時 true を返す
     */
    public	boolean	exists(){
		if(LOG.fa)	LOG.println("■ FileCabinet #existsUploadedFile()");
		
    	String	fname		=	getParameter(UPLOAD_FILE_NAME);// アップロードされたファイル名
    	String	checkPath	=	path + fname;
    	File	fp			=	new File(checkPath);
    	if(fp.exists()){
    		return	true;
    	}else{
    		return	false;
    	}
    }	
	/**
	 * アップロードされたファイルを削除する
	 *
	 */
	public	void	deleteUploadFile(){
		if(LOG.fa)	LOG.println("■ FileCabinet #deleteUploadFile()");

		String	dir	=	getParameter(UPLOAD_DIR_NAME);
		FileGear.delDir(dir);
	}
	
	/**
     * ファイルを受け取ってキャビネットフォルダに置く
     *
     * 個人用フォルダは /home/group/_szDB/_userid/filecabinet/
     * 
     * 
     * @return		受け取ったファイル名を返す．失敗の場合は null を返す
     */
    public	String	getUploadedFile(){
		if(LOG.fa)	LOG.println("■ FileCabinet #getUploadedFile()");
		
    	String	fname	=	getFile(path);// ファイル受け取り
   		if(!isEmpty(fname)){
   			putParameter(MESSAGE,"★ファイルを受け取りました．");
   			return		fname;
   		}else{
   			putParameter(MESSAGE,"★指定されたものは有効なファイルではありません．");
   			return		null;
   		}
    }

	/**
	 * アップロードされたファイルをプロファイルディレクトリへ移す
	 * 複数ファイルの同時アップロードに対応しているがここでは１個のみとする
	 * 
     * @param 		toDir		受け取りディレクトリ
     * @return		成功したときtureを返す
     * 
	 */
	public String	getFile(String toDir){
		if(LOG.fa)	LOG.println("■ BbsMakePost #getFile()");
		/*
		 * ファイル受け取り
		 */
		String		fromDir	=	getParameter(UPLOAD_DIR_NAME);
		String	[]	files	=	(new File(fromDir)).list();
		/*
		 *   /home/pc/temp/[ランダムに作成したディレクトリ名]   ⇒　/home/bbs/[owner]/[forum]/[thread]/[Post]/
		 *  ファイルが複数あれば全てを移動する
		 */
		boolean	retcode	=	FileGear.moveFiles(toDir, fromDir);
		if(retcode){
			return	files[0];// ファイル名を返す
		}else{
			return	"";
		}
	}
	/**
	 *  受け取りファイルをデータベースに登録する

	 *
	 * @param fname		ファイル名
	 */
	public	void	writeDB(String fname){
		if(LOG.fa)	LOG.println("■ FileCabinet #writeDB(String fname)");
		

		// ファイルデータベース
		CBdatabase	cbd		=	new CBdatabase(broker, getParameter(GROUP));
		
		Hashtable	record	=	mkRecord(fname);
		String		te_lec_key	=	getParameter("scope");
		if(te_lec_key.equals("PRIVATE")){
			// ファイルデータベースに登録し、個人用インデックステーブルにも登録する
			cbd.insertByid(record, getParameter(UID));
			
		}else{
			// ファイルデータベースに登録し、クラス用インデックステーブルにも登録する
			cbd.insertByClass(record, te_lec_key);
		}
		
	}
	/**
	 * レコードを更新書き込みする
	 * 
	 * 2006.6 データベースにファイル名を正しく書けないバグを修正
	 * \ が消えてしまうことが原因。/ に変換して書き込むことにした。
	 * 
	 * なお、
	 * 過去のバグを回避するため、更新で失敗すると新規で書き込む
	 * ように修正した。
	 * 
	 * @param fname
	 * @return 成功するとtrue 失敗ではfalse
	 */
	public	boolean	updateDB(String	fname){
		if(LOG.fa)	LOG.println("■ FileCabinet #updateDB(String	fname)");
		
		Hashtable	record	=	mkUpdateRecord(fname);	// 読み出したレコード．更新日付を書き換えている．
		String		seqkey	=	getParameter(record, KEY);

		CBdatabase	cbd		=	new CBdatabase(broker, getParameter(GROUP));
		int			cnt		=	cbd.update(seqkey, record);
		if(cnt==0){
			// ファイルはあるがデータベースに記録が無い
			// 過去のバグのため起こりえる
			return	false;
		}
		
		String	te_lec_key	=	getParameter("scope");
		if(te_lec_key.equals("PRIVATE")){
			// 個人用インデックスの有無を調べ、なければ新規に挿入する
			CBidIndex	cbid	=	new	CBidIndex(broker, getParameter(GROUP));
			if( !cbid.exists(getParameter(UID), seqkey) ){
				// ファイルデータベースのフラグを更新する
				cbd.addIdx(record, "001");
				
				// 個人用インデックスを挿入
				cbid.insert(getParameter(UID), seqkey);
			}

		}else{
			// クラス用インデックスの有無を調べ、なければ新規に挿入する用インデックスを更新
			CBclassIndex	cbclass	=	new	CBclassIndex(broker, getParameter(GROUP));
			if( !cbclass.exists(te_lec_key, seqkey) ){
				cbd.addIdx(record, "010");
				cbclass.insert(te_lec_key, seqkey);
			}
		}
		return	true;
	}
	/**
	 * 更新レコードを得る
	 * 
	 * @param fname
	 * @return
	 */
	Hashtable	mkUpdateRecord(String fname){
		if(LOG.fa)	LOG.println("■ FileCabinet #mkUpdateRecord(String fname)");
		
		Hashtable	record	=	new	Hashtable(30);
		CBdatabase	cbd		=	new CBdatabase(broker, getParameter(GROUP));
		int			count	=	cbd.readRecord(fname, record);
		
		// 更新日付の書き換え
		String		from	=	DateGear.getLongDateString();	// yyyyMMddHHmmss (14桁)
		putParameter(record, FROM, from.substring(0,12));		// yyyyMMddHHmm(12桁)
		
		return	record;
	}
	
	/**
	 * レコードを作成する<br>
	 * subject, idx の値は""で作成
	 *
	　　[記録すべきデータ]
     	seqkey		  CHAR(18)      キー
     	subject       TEXT　　　　　内容説明
     	owner         VARCHAR(10)   登録者
     	fname         VARCHAR(256)  ファイル名(英字)
     	kubun         VARCHAR(5),   ファイル種別（拡張子） 
     	size          VARCHAR(20)   ファイルサイズ
     	filepath      VARCHAR(256)  アクセス用PATH
     	fileurl       VARCHAR(256)  アクセス用URL
     	date_from     CHAR(12)      登録日
     	date_to       CHAR(12)      有効期限日	 * 
	 * 
	 * @param 	fname	ファイル名
	 * @return			作成したレコードのハッシュテーブル
	 */
	public	Hashtable	mkRecord(String fname){
		if(LOG.fa)	LOG.println("■ FileCabinet #mkRecord(String fname)");
		
		Hashtable	tb	=	new	Hashtable(30);
		CBdatabase	cbd	=	new CBdatabase(broker, getParameter(GROUP));
		cbd.setInitRecord(tb);
		
		CBkey	cbkey	=	new CBkey(broker);
		String	seqkey	=	cbkey.getNextNumber(getParameter(GROUP));
		putParameter(tb, KEY, seqkey);
		
		putParameter(tb, OWNER, 	getParameter(UID));
		putParameter(tb, FNAME, 	fname);
		putParameter(tb, KUBUN, 	FileGear.getExt(fname));
		
		String	filepath	=	path + fname;
		filepath	=	StringGear.substitute(filepath, "\\","/");
		if(LOG.fa)	LOG.println("filepath="+filepath);
		
		
		String	fileurl		=	para.fileCabinetURL(getParameter(GROUP), getParameter(UID))+ fname;
		File	fp			=	new File(path + fname);
		long	size		=	(fp.length() + 512 )/ 1024;
		if(size==0)	size	=	1;
		putParameter(tb, SIZE, 		String.valueOf(size)+"KB");
		putParameter(tb, PATH, 		filepath);
		putParameter(tb, URL, 		fileurl);	
		putParameter(tb, IDX, 		"");	
		
		return	tb;
	}
	/**
	 * ファイルの説明をデータベースに書き込む
	 *
	 */
	public void	setSubject(){
		if(LOG.fa)	LOG.println("■ FileCabinet #setSubject()");
		if(isClassAndStudent()){
			putParameter(MESSAGE, "★学生の権限ではクラスのファイルを変更できません");
			return;
		}		
		
		Hashtable	record	=	new Hashtable(30);
		CBdatabase	cbd		=	new CBdatabase(broker, getParameter(GROUP));
		cbd.read(getParameter(KEY),record);
		
		putParameter(record, SUBJECT, getParameter(SUBJECT));
		cbd.update(getParameter(KEY),record);
		
	}
	/**
	 * 指定されたファイルをデータベースとファイルディレクトリから削除する<br>
	 * 
	 * キーテーブルはそのままにしておき、表示処理でCBdatabaseテーブルに該当が
	 * ないと分かった時にそれぞれ削除する
	 */
	public	void	delete(){
		if(LOG.fa)	LOG.println("■ FileCabinet #delete()");
		
		String		te_lec_key	=	getParameter("scope");
		if(te_lec_key.equals("ALLFILES")){
			putParameter(MESSAGE, "★全てのファイルを表示している状態では削除は出来ません");
			return;
		}
		
		if(isClassAndStudent()){
			putParameter(MESSAGE, "★学生の権限ではクラスのファイルは削除は出来ません");
			return;
		}
			
		String		key		=	getParameter(KEY);	// webから
		Hashtable	record	=	getRecord(key);
		String		idxFlag	=	getParameter(record, IDX);
		if(idxFlag.equals("011")){
			// インデックスファイルを消すだけ
			if(te_lec_key.equals("PRIVATE")){
				
				CBidIndex	cii	=	new CBidIndex(broker, getParameter(GROUP));
				cii.delete(key);
				CBdatabase	cdb	=	new	CBdatabase(broker, getParameter(GROUP));
				cdb.subIdx(record, "001");
				
			}else{
				CBclassIndex	cci	=	new CBclassIndex(broker, getParameter(GROUP));
				cci.delete(key);
				CBdatabase	cdb	=	new	CBdatabase(broker, getParameter(GROUP));
				cdb.subIdx(record, "010");
			}			
			
		}else{// "010" か "001"　の時
			
			// インデックスファイルとファイルデータベース、およびファイル自身も消す
			if(te_lec_key.equals("PRIVATE")){
				CBidIndex	cii	=	new CBidIndex(broker, getParameter(GROUP));
				cii.delete(key);
				deleteFile(record);
				deleteFileRecord(key);
				
			}else{
				CBclassIndex	cci	=	new CBclassIndex(broker, getParameter(GROUP));
				cci.delete(key);
				deleteFile(record);
				deleteFileRecord(key);
				
			}
		}
		putParameter(MESSAGE,"★ファイルを削除しました．");
	}
	/**
	 * キーを使ってレコードを得る
	 * 
	 * @param key
	 * @return 	レコードを格納したハッシュテーブル．レコードが無い時はnull．
	 */
	public	Hashtable	getRecord(String key){
		if(LOG.fa)	LOG.println("■ FileCabinet #getRecord(String key)");
		
		CBdatabase	cbd		=	new	CBdatabase( broker, getParameter(GROUP));
		Hashtable	dt		=	new	Hashtable();
		int			count	=	cbd.read(key,dt);
		if(count==0){
			return	null;
		}
		//DBG.outHash(dt,"★キーを使ってレコードを得る");
		return	dt;
		
	}
	/**
	 * レコード情報を使ってファイルを削除する
	 * @param dt
	 * @return
	 */
	public	boolean	deleteFile(Hashtable dt){
		if(LOG.fa)	LOG.println("■ FileCabinet #deleteFile(Hashtable dt)");
		
		String	fpath	=	getParameter(dt, PATH);
		fpath	=	StringGear.substitute(fpath, "/", "\\");
		if(LOG.fa)	LOG.println("delete filepath="+fpath);
		File	fp		=	new	File(fpath);// ファイルの絶対パス
		return	fp.delete();
		
	}
	/**
	 * キーを使ってレコードを削除する
	 * @param key
	 * @return
	 */
	public	boolean	deleteFileRecord(String key){
		if(LOG.fa)	LOG.println("■ FileCabinet #deleteFile(Hashtable dt)");
		
		CBdatabase	cbd	=	new	CBdatabase( broker, getParameter(GROUP));
		int			n	=	cbd.delete(key);
		if(n==1)	return	true;
		return		false;
		
	}
	/////////////////////////////////////////////////////////////////////////
	//
	//   表示処理
	//
	/////////////////////////////////////////////////////////////////////////
	
	/* (非 Javadoc)
	 * @see framwork.SuperPlayer#display(boolean)
	 */
	public void display(boolean editmode) {
		if(LOG.fa)	LOG.println("■ FileCabinet #display(boolean editmode)");
		
		if(!editmode){
			htb.put(MESSAGE,"");
			putParameter("scope", "PRIVATE");	// 強制的に値を与えておかないと初期表示できない
			putParameter("_sel-1", "selected");
			putParameter("_sel-2", "");
			putParameter("_sel-3", "");
		}
		
		/* strHash(htb,DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);	
	}
	/**
	 * 表示対象と表示ファイルリストの編集
	 */
	public void	write(String key,Vector exHtml){
		if(LOG.fa)	LOG.println("■ FileCabinet #write()");
		
		if(key.equals("kamokuSelection")){
			if(division.endsWith(DIV_STUDENT)){// 学生のとき
				st_KamokuSelection(exHtml);
				
			}else{
				kamokuSelection(exHtml);
			}
						
		}else if(key.equals("fileList")){
			fileList(exHtml);
			
		}else if(key.equals("noFile")){
			noFile(exHtml);
			
		}
	}
	/** 
	 * 表示範囲のうち、全ての科目について科目名と科目キーを埋め込み、さらに選択されていれば select を埋め込む
	 * 
	 * exHtml 内のデータ（１件のみ）
	 * <option value="%_title%用ファイル" class="list01" %_sel-2% onChange="setEdit('LIST', '%_lec_key')">%_title%用ファイル</option>
	 * 
	 * @param exHtml
	 */
	public	void	kamokuSelection(Vector exHtml){
		if(LOG.fa) LOG.println("■FileCabinet #kamokuSelection()");
		
		String		selectedKey	=	getParameter("scope");
		KamokuDEF	kdf			=	new	KamokuDEF(getParameter(UID), db);
		int			n			=	kdf.size();

		for(int i=0; i<n; i++){
			/*
			 * te_lec_key を作る
			 */
			KamokuDefRecord	kdr			=	kdf.get(i);
			String			te_lec_key	=	KeyGen.get_te_lec_key2(getParameter(UID), kdr.lec_key());
			/*
			 * パラメータを埋め込む
			 */
			putParameter("title"		, kdr.title());
			putParameter("te_lec_key"	, te_lec_key);
			/*
			 * select の表示
			 */
			if(selectedKey.equals( te_lec_key )){
				putParameter("sel-2","selected");
			}else{
				putParameter("sel-2","");
			}
			printVector(exHtml);
			
		}
	}
	/**
	 * 学生の受講情報から、対象となる科目を求め、全ての科目について情報を埋め込む．
	 * さらにその科目が選択されていれば select を埋め込む
	 * 
	 * @return
	 */
	void	st_KamokuSelection(Vector exHtml){
		if(LOG.fa) LOG.println("■ FileCabinet #st_KamokuSelection()");
		
		String			selected_te_lec_key	=	getParameter("scope");// 現在の選択
		/*
		 * 一覧を作る
		 */
		Csv				classInfo	=	new Csv(getParameter(StCLASSINFO));	
		int				n			=	classInfo.size();
		for(int i=0; i<n; i++){
			/*
			 * 受講情報から科目キー(te_lec_key)などを得る
			 */
			String			te_aplec_key	=	classInfo.get(i);
			KamokuApRecord	kar				=	new KamokuApRecord(te_aplec_key, db);
			String			lec_key			=	kar.lec_key();
			String			teUid			=	getTeUid(te_aplec_key);
			String			te_lec_key		=	KeyGen.get_te_lec_key2(teUid, lec_key);
			/*
			 * 科目定義レコードを引いて情報を埋め込む
			 */
			KamokuDefRecord	kdr				=	new	KamokuDefRecord(teUid, lec_key, db);
			putParameter("title"		, kdr.title());
			putParameter("te_lec_key"	, te_lec_key);
			/*
			 * select の埋め込み
			 */
			if(selected_te_lec_key.equals( te_lec_key )){
				putParameter("sel-2","selected");
			}else{
				putParameter("sel-2","");
			}
			printVector(exHtml);			
			
		}
	}	
	/**
	 * ファイルリストを表示する<br>
	 * 
	 * 表示範囲に基づいてインデックスキーを選択し、テーブルを引いて表示すべきレコードを求める
	 * インデックスキーに対応するレコードが無い場合、それは削除レコードなのでインデックステーブルからキーを削除する
	 * 
	 * 設定するリスト１行分のパラメータは
	 * 		n			: 一連番号
	 * 		fileurl		: ファイルへのURL
	 * 		fname		: ファイル名
	 * 		seqkey		: CBdatabeseテーブルでのキー
	 * 		subject		: ファイルの説明
	 * 		kubun		: ファイル種別（拡張子）
	 * 		size		: ファイルサイズ
	 * 		from		: 登録日
	 * 
	 *	<tr> 
     * 		<td height="44" valign="top"><!--DWLayoutEmptyCell-->&nbsp;</td>
     * 		<td align="center" valign="middle" class="b01">%_n%</td>
     * 		<td width="151" align="left" valign="middle" class="b021"><a href="%_fileurl%">%_fname%</a></td>
     * 		<td width="36" align="center" valign="middle" class="b011"><a href="#" onClick="submitDelete('DELETE', '%_seqkey%')"><img src="/pc/images/deleteIcon.gif" width="16" height="15" border="0" alt="このファイルを削除する"></a></td>
     * 		<td align="left" valign="middle" class="b021">%_content%</td>
     * 		<td align="center" valign="middle" class="b01">%_kubun%</td>
     * 		<td align="center" valign="middle" class="b01">%_size%</td>
     * 		<td align="center" valign="middle" class="b02">%_from</td>
     * 		<td valign="top"><!--DWLayoutEmptyCell-->&nbsp;</td>
     * </tr>
     * 
	 * @param exHtml	部分HTMLを格納したベクター
	 */
	public	void	fileList(Vector exHtml){
		if(LOG.fa)	LOG.println("■ FileCabinet #fileList()");
		
		// scope から対応する範囲のseqkeyのリストを得る
		Vector	seqKeys	=	getKeys();
		int		n		=	seqKeys.size();
		int		count	=	0;
		for(int i=0; i<n; i++){
			// レコードを検索する．
			String		seqkey	=	(String)seqKeys.get(i);
			Hashtable	record	=	getRecord(seqkey);
			if(record==null){
				continue;
			}
			
			//DBG.outHash(record,"★データベースレコード");
			// パラメータを設定して１行分表示する
			// 空白には &nbsp; をあてて表の罫線が表示されるようにする
			putParameter("n", get000type(i+1));
			String	url		=	getParameter(record, URL);
			putParameter(URL	, (isEmpty(url) 	? "&nbsp;" : url));
			
			String	fname	=	getParameter(record, FNAME);	putParameter(FNAME	, (isEmpty(fname) 	? "&nbsp;" : fname));
			String	key		=	getParameter(record, KEY);		putParameter(KEY	, (isEmpty(key) 	? "&nbsp;" : key));
			String	subject	=	getParameter(record, SUBJECT);	putParameter(SUBJECT, (isEmpty(subject) ? "&nbsp;" : subject));
			String	kubun	=	getParameter(record, KUBUN);	putParameter(KUBUN	, (isEmpty(kubun) 	? "&nbsp;" : kubun));
			String	size	=	getParameter(record, SIZE);		putParameter(SIZE	, (isEmpty(size) 	? "&nbsp;" : size));
			String	datefrom=	getParameter(record, FROM);		putParameter(FROM	, (isEmpty(datefrom) ? "&nbsp;" : fdate(datefrom)));
			printVector(exHtml);
			count++;
		}
		/*
		 * noFile()の処理（レコードの登録が無い時の表示）のためにシステムハッシュに
		 * 表示件数を記録しておく
		 */
		putParameter("count", String.valueOf(count));
		
	}
	/**
	 * １２桁の時間文字列を 例えば 200502130112 → 2005/02/13 01:12 のように編集する
	 * @param str
	 * @return
	 */
	public	String	fdate(String str){
		if(LOG.fa)	LOG.println("■ FileCabinet #fdate()");
		
		
		String	dateString	=	str.substring(0,4)	+ "/" + str.substring(4,6)+"/"+str.substring(6,8)+ " " 
		                      + str.substring(8,10)	+ ":" + str.substring(10,12);
		return	dateString;
		
	}
	/**
	 * 現在の表示モードにあったレコードキーを収集して返す
	 * 
	 * @return		表示すべきレコードのキー集合のベクター
	 */
	public	Vector	getKeys(){
		/*
		 * 学生がそれ以外かで分ける
		 */
		if(division.equals(DIV_STUDENT)){
			return	getKeys_student();
			
		}else{
			return	getKeys_teacher();
			
		}
		
	}
	/**
	 * 学生以外の場合に該当する seqkey を全て得る
	 * @return
	 */
	public	Vector	getKeys_teacher(){
		if(LOG.fa)	LOG.println("■ FileCabinet #getKeys()");
		
		
		Vector	keys		=	new	Vector(20);
		String	te_lec_key	=	getParameter("scope");
		
		if(te_lec_key.equals("PRIVATE")){
			CBidIndex	cbid	=	new	CBidIndex(broker, getParameter(GROUP));
			cbid.read(getParameter(UID), keys);

		}else if(te_lec_key.equals("ALLFILES")){
			CBclassIndex	cbclass	=	new	CBclassIndex(broker, getParameter(GROUP));
			cbclass.read(keys);// クラスインデックスの全てのレコード
			
			//CBdatabase	cbd		=	new	CBdatabase( broker, getParameter(GROUP));
			//cbd.readAllKeys(keys);
			
		}else{
			CBclassIndex	cbclass	=	new	CBclassIndex(broker, getParameter(GROUP));
			cbclass.read(te_lec_key, keys);
			
		}
		return	keys;
		
	}
	/**
	 * 学生の場合に該当する seqkey を全て得る
	 * 
	 * @return
	 */
	public	Vector	getKeys_student(){
		if(LOG.fa)	LOG.println("■ FileCabinet #getKeys()");
		
		String	te_lec_key		=	getParameter("scope");// te_lec_key , "PRIVATE" , "ALLFILES" のいづれか
		if(te_lec_key.equals("PRIVATE")){
			Vector		keys	=	new	Vector(20);
			CBidIndex	cbid	=	new	CBidIndex(broker, getParameter(GROUP));
			cbid.read(getParameter(UID), keys);
			return	keys;

		}else if(te_lec_key.equals("ALLFILES")){
			return	getAllClasses();
			
		}else{
			Vector			keys	=	new	Vector(20);
			CBclassIndex	cbclass	=	new	CBclassIndex(broker, getParameter(GROUP));
			cbclass.read(te_lec_key, keys);
			return	keys;
			
		}
	}
	/**
	 * 受講している全ての科目の seqkey を得る
	 * 
	 * @return
	 */
	Vector	getAllClasses(){
		Vector			keys		=	new	Vector(20);
		CBclassIndex	cbclass		=	new	CBclassIndex(broker, getParameter(GROUP));
		Csv				classInfo	=	new Csv(getParameter(StCLASSINFO));	
		int				n			=	classInfo.size();
		for(int i=0; i<n; i++){
			/*
			 * 受講情報から科目キー(te_lec_key)を得る
			 */
			String			te_aplec_key	=	classInfo.get(i);
			KamokuApRecord	kar				=	new KamokuApRecord(te_aplec_key, db);
			String			te_lec_key		=	KeyGen.get_te_lec_key2( getTeUid(te_aplec_key) , kar.lec_key() );
			/*
			 * 科目キーに該当する seqkey を全て keys に得る
			 */
			cbclass.read(te_lec_key, keys);
		}
		return	keys;
	}
	
	/**
	 * "mwb9l124-131" のようなte_aplec_key, te_lec_key から 教師ID部分(mwb9l124)を取り出す． 
	 * @param te_aplec_key
	 * @return
	 */
	String	getTeUid(String key){
		
		Csv	cs	=	new	Csv(key, "-");
		return		cs.get(0);
	}	
	/**
	 * レコードの登録が無い時の表示を行なう<br>
	 * 
	 * fileList()で既表示件数が count に保存されているので
	 * それを見て0件ならば表示する．
	 * 
	 * 
	 * @param exHtml
	 */
	public	void	noFile(Vector exHtml){
		if(LOG.fa)	LOG.println("■ FileCabinet #noFile()");
		
		String	count	=	getParameter("count");
		if(Integer.parseInt(count)==0){
			printVector(exHtml);
			
		}
	}
}





