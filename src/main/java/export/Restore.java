/*
Power Campus Calender

*/
package export;
import refer.ReferenceDefRecord;
import tktools.*;

import java.io.*;
import java.util.*;

import kadai.KadaiDefRecord;
import kamoku.*;
import	xmlparser.*;
import database.*;
import framework.*;

/**
* 
#
# ##################
#   Restore
# ##################
#
<program $export.Restore>
	<dispatch  html=restore.html  number=206  class=export.Restore />
	<variable>
	  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA/>
	  <accept    CMD    UPLODE  />
	  <keep      />
	  <form      />
	</variable>
</program> 
*
*/

public class Restore extends SuperPlayer implements KamokuVar{
	
	

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

	/**
	* インポートファイルへのフルパス
	*/
	String	importFilePath;

	/**
	 * レストアファイル名＝もとのte_lec_key
	 */
	String	restoreFileName;
	
	
	/** 教師id */
	String teUid;

	/** グループid */
	String szDB;

	/** 講義キー */
	String	lec_key;	

	/** 教師＋講義キー */
	String	te_lec_key;		


	public Restore(){
		super();
		if(LOG.fa) LOG.println("■ Import #コンストラクタ");
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
		if(LOG.fa) LOG.println("■ Import #initialize()");
		broker	=	getDbConnection();
		db		=	new Database(broker);
		
		teUid	=	getParameter(TUID);
		szDB	=	getParameter(GROUP);

	}	
	/**
	 * 分岐処理
	 */	
	public	String	dispatch(){
		if(LOG.fa) LOG.outHash(htb,"■Restore #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		if(cmd.equals("RESTORE")){
			if(LOG.fa) LOG.println("■Restore #dispatch(): レストア処理 の先頭です");
			//
			// ファイルを受け取る
			boolean	result	=	importFile();
			if(result){
				// データを再配置する
				resetKamoku();
				putParameter("MESSAGE","■■ 『" + getParameter("title") + "』の全てのデータをレストアしました ■■");
			}
			
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示

		}else if(cmd.equals("ERROR_FILE")){
			htb.put("_msg","★ ファイル名を指定してください");
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示

		}else if(cmd.equals("RETURN")){
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;	// 復帰			

		}else{
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示			
		}
		/*
		 * 次に表示するときあるいはリターンしてきた時の表示モードをセットしてからリターンする
		 */
		putParameter(DISP_KEY,disp_mode);
		return	ret;
	}
	//
	//	 科目定義ファイルの受け取り
	//
	boolean importFile(){
		String method = "■Restore #importFile() : ";
		if(LOG.fa) LOG.println(method + ": 科目定義ファイルの受け取り の先頭です");
		//
		// マルチパート入力で受け取った変数を得る
	   String savedir   = getParameter(UPLOAD_DIR_NAME);  		// 一時的格納場所 2003.6.15
	   String mfilename = getParameter(UPLOAD_FILE_NAME);		// ファイル名のみ
	   String mailFile  = getParameter(UPLOAD_FILE_PATH);		// savedir + onlyFilename == 一時記録ファイルへのフルパス
	   String files	 = getParameter(UPLOAD_COUNTS);			// マルチパートで読み込んだファイル数
		//
		boolean eflag    = false;
		int		fcnt	 = Integer.parseInt(files);
		//
	   if(mfilename!=null){
	   	if(LOG.fa) LOG.println(method + "科目定義ファイル名/ file name =" + mfilename);
			//
			String ext	= getTriler(mfilename);
			if( !ext.equals("zip") ) {
				if(LOG.fa) LOG.println(method + "ファイルの種類が違います．インポートするのは科目定義ファイルでなくてはなりません/ file name = " + mfilename);
				htb.put("_msg","★ ファイルの種類が違います．インポートするのは科目定義ファイルでなくてはなりません");
	          	eflag = true;
			}else{
				//
				File mfp   = new File(mailFile);
		        if(mfp.length()==0){    // （長さがゼロならファイル名の間違い）
	   	        if(LOG.fa) LOG.println(method + "指定された科目定義ファイルは存在しません/ file name = " + mfilename);
	       	    htb.put("_msg","★ 指定された科目定義ファイルは存在しません");
	           	eflag = true;
	           }
		    }
		}else{
			eflag = true;
			htb.put("_msg","★ 科目定義ファイルを指定してください");
			if(LOG.fa) LOG.println(method + "科目定義ファイルを指定してください");
	   }
	   //
	   if(eflag){// エラーありの場合
	       if(LOG.fa) LOG.println(method + "エラーがありましたので科目定義ファイルを取得できませんでした");
			//
			// もし送信された差込ファイルがあれば削除する
	       if((mailFile != null)&&(mailFile.length() > 0)){
	           File saveDir    = new File(savedir);
	           if(saveDir.exists()){
	               deleteDir(saveDir);
	           }
	       }
	       return false;
	   }// ** ここで終わり **
	   //
	   // もし送信された科目定義ファイルがあれば正規のディレクトリに移動する
		if((mailFile != null)&&(mailFile.length() > 0)){
	       File 	mfp    = new File(mailFile);			// 一時格納場所（ここにある）
	       String  dir    = para.importDir(teUid);			// インポート用ディレクトリのフルパス ex.  /home/pc/kawaba01/import/
			File    moveTo = new File(dir);
			if(!moveTo.isDirectory()){	// ディレクトリがなければ作る
				moveTo.mkdirs();
			}else{
				deleteFiles(moveTo);	// 移動先の(dir)のファイルを全て消してから移動処理を行う
			}
	       //
	       boolean flag   = mfp.renameTo(new File(moveTo,mfp.getName()));	// 移動処理
	      	deleteDir(new File(savedir));									// 作業ディレクトリの消去
	       //
			if(!flag){
	          	LOG.errStop("KamokuApRecord class #addMeibo() :指定されたファイルをコピーできない");
				return false;
	       }
	       // インポートファイルへのフルパスとファイル名を記憶しておく
			restoreFileName	=	mfilename;
			importFilePath	=	para.importDir(teUid) + mfilename;
			if(LOG.fa) LOG.println(method + "正常にファイルを格納しました/ FILE NAME = " + importFilePath );
	   }
		//
		return true;
	}
	//
	//	 ファイル拡張子を取り出す
	String getTriler(String filename){
		Csv chk = new Csv(filename,".");
		if(chk.size() <= 1) return "";	// 拡張子がないとき
		//
		return chk.get(chk.size() -1);	// 最後の要素
	}

	/**
	*  講義定義キーを取得する
	*/
	void	receiveKeys(){
		/*
		 * 受け取ったファイル名は元のte_lec_keyである
		 * レストアの時ファイル名を変更してはいけない
		 */
		te_lec_key	=	FileGear.fileNameBody(restoreFileName);
		lec_key		= 	KeyGen.lecFromTe_lec(te_lec_key);	
	}

	/**
	* 科目定義データを再配置する
	* 
	*/
	void resetKamoku(){
		if(LOG.fa) LOG.println("■Restore #resetKamoku() : 科目定義データを再配置する の先頭です");
		/*
		 * 講義定義キーを取得する
		 * lec_key, te_lec_key
		 */
		/////////////
		receiveKeys();
		/////////////
		/*
		 * 既存の科目定義情報だけを全て削除する
		 */
		KamokuDelete	kdel	= new KamokuDelete(db);
		kdel.deleteForRestore(teUid, lec_key, para);
		
		
		// zipファイルをimport作業ディレクトリに解凍する
		UnZip	uz	=	new	UnZip(importFilePath, para.importDirName(teUid));
		uz.unzip();
		
		String	path;
		
		// 科目関連データを（zipを解凍することによって）ファイルシステムにコピーする
		/*
		 * 資料emlファイル
		 */
		path	=	para.importDir(teUid)+KAMOKU_EML_FILE;
		if(isExist(path)){// 上のファイルはない場合があるので、存在を確認してから処理する
			UnZip	eml		=	new	UnZip(path, para.getEpmlPathName(teUid, lec_key));
			eml.unzip();
		}
		/*
		 * 資料htmlファイル
		 */
		path	=	para.importDir(teUid)+KAMOKU_HTML_FILE;
		if(isExist(path)){	
			UnZip	html	=	new	UnZip(path, para.getHtmlPathName(teUid, lec_key));
			html.unzip();
		}
		/*
		 * 課題関連グラフィックス
		 */
		path	=	para.importDir(teUid)+KAMOKU_GRPH_FILE;
		if(isExist(path)){	
			UnZip	grph	=	new	UnZip(path, para.getKamokuAttachPathName(teUid, lec_key));
			grph.unzip();
		}
		// ■■ 科目定義ファイルから科目定義情報を復元する ■■
		Course	csr			= readCourseFile(para.importDir(teUid)+KAMOKU_DEF_FILE);	// 科目定義ファイル名 kamoku.def（固定）
		//
		/*
		 *  1. 科目定義レコード
		 */
		exKamoku		ekm		= csr.get_kamoku();
		//ekm.keyEdit(teUid,lec_key);	// 新しいキーに書き換え
		KamokuDefRecord	kdrec	= new KamokuDefRecord( ekm.contents() );
		kdrec.insert(db);
		/*
		 * 1.1 シラバスタイトル
		 */
		String			title	=	kdrec.title();
		putParameter("title", title);	// 表示用
		
		/*
		 * 1.2 シラバスXMLをチェック更新
		 */
		KamokuParser	kp		=	getKamokuParser(kdrec.content(), kdrec);
		if(kp==null){
			alart(kdrec);// システムのxmlをセットしてDBに書く
			kp		=	getKamokuParser(kdrec.content(), kdrec);// kdrec.content()はalart()で更新済み
		}
		String	serial	=	"";
		try{
			serial	=	para.getKamokuSerial(szDB, teUid);
		}catch(xmlException e){
			
		}
		/*
		 * 1.3 正しいシリアルのXMLに修正
		 */
		kp.setSerial(serial);
		KamokuRevParser	krp		=	new	KamokuRevParser(kp, para.kamokuTemplatePath());
		String			newXml	=	krp.getXml();
		kdrec.set_content( newXml );
		kdrec.update(db);
		
		/*
		 * 2. セクションレコード
		 */
		
		
		Vector	sections	= csr.get_Sections();
		int		n			= sections.size();
		for(int i=0; i<n; i++){
			exSection esc	= (exSection) sections.get(i);
			//esc.keyEdit(te_lec_key);
			KamokuSecDefRecord ksdf	= new KamokuSecDefRecord(esc.contents());
			ksdf.insert(db);
		}
		
		/*
		 * 3. リファレンスレコード
		 */
		Vector	refs		= csr.get_References();	// 全てのリファレンスレコード
		int		n1			= refs.size();
		for(int i=0; i<n1; i++){
			exReference erf	= (exReference) refs.get(i);
			//erf.keyEdit(te_lec_key, lec_key);
			ReferenceDefRecord rfRec	= new ReferenceDefRecord(erf.contents());
			rfRec.insert(db);
		}
		
		/*
		 * 4. 課題レコード
		 */
		Vector	kadais		= csr.get_Kadai();
		int		n2			= kadais.size();
		for(int i=0; i<n2; i++){
			exKadai ekd	= (exKadai) kadais.get(i);
			//ekd.keyEdit(te_lec_key);
			KadaiDefRecord kdRec	= new KadaiDefRecord(ekd.contents());
			kdRec.insert(db);
		}
		
	}
	/**
	* ファイルがあるかどうか調べる
	* @param path
	* @return
	*/
	public	boolean	isExist(String path){
		File	fp	=	new	File(path);
		return	fp.exists();
	}
	//
	//	 科目定義データを読み込む
	//
	public Course readCourseFile(String fname){
		if(LOG.fa) LOG.println("■Restore #readCourseFile() : 科目定義データを読み込む の先頭です");
		if(LOG.fa) LOG.println("          fname = " + fname);
		//
		ObjectInputStream  objIn  = null;
	   Course crs = null;
	   try{
	       objIn = new ObjectInputStream(new FileInputStream(fname));
	       try{
	           crs = (Course)objIn.readObject();
	           objIn.close();
	       }catch(ClassNotFoundException e1){
	           System.out.println("ClassNotFoundException:" + e1);
	           crs = null;
	       }catch(IOException e2){
	           System.out.println("IOException:" + e2);
	           crs = null;
	       }
	   }catch(IOException e3){
	       System.out.println("can't Open :IOException:" + e3);
	       crs = null;
	   }
	   return crs;
	}

	/**
	* 所余のxmlから科目パーサーを作成して返す<br>
	* @param xml
	* @param kdrec
	* @return
	*/
	KamokuParser	getKamokuParser(String	xml, KamokuDefRecord kdrec){
		if(LOG.fa)	LOG.println("■ Restore #getKamokuParser()");
		KamokuParser	kp	=	null;
		try{
			kp	=	new	KamokuParser(xml);
			
		}catch(xmlException e){
			return null;
			
		}
		return	kp;
		
	}
	/**
	* 何らかの原因で受け取ったデータのXMLが不正なものだった場合、
	* 現在の新しいシラバスXML（値は未記入）を content にセットしてデータベースを更新する
	* それにも誤りがあれば、システムのXMLをcontent にセットしてデータベースを更新する
	*/
	void	alart(KamokuDefRecord kdrec){
		if(LOG.fa)	LOG.println("■ Restore #alart()");
		try{
			kdrec.set_content( para.getKamokuXml(szDB, teUid) );
			
		}catch(xmlException e){
			putParameter(MESSAGE, "★システムの科目定義XMLが正しい記述ではありませんのでシステムのXMLを代用しました．<br>" + e.getMessage());
			kdrec.set_content( FileGear.getFileData(para.syllabusPath()) );
		}
		kdrec.update(db);
		
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////	/
	//
	//	出　　力　　処　　理
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////	/
	/**
	* 出力処理
	*/
	public  void	display(boolean editmode){
		if(LOG.fa)	LOG.println("■ Restore #display(boolean editmode)");
		
		if(!editmode){
			htb.put(MESSAGE,"");
		}
		
		/* strHash(htb,DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);	
	}
}


