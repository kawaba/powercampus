/*
 * 作成日: 2005/02/03
 *
 * 科目情報をすべてパックしたZIPファイルをダウンロード
 * エリアに作成する
 * 
 */
package export;
import java.io.*;

import refer.ReferenceDEF;
import refer.ReferenceDefRecord;
import tktools.*;
import	database.*;
import kadai.KadaiDEF;
import kadai.KadaiDefRecord;
import	kamoku.*;
import framework.*;
/**
 *
 */
public class KamokuExport extends Object implements KamokuVar{

	String				teUid;
	String				lec;
	Database			db;
	Param				para;
	
	String				te_lec_key;
	KamokuDefRecord		kdr;
	Course				crs;
	
	/** ファイルを書き込むディレクトリ */
	String				downloadDir;
	
	/** 中間のzipファイルを作る作業用ディレクトリ */
	String				tempDir;
	
	
	public	KamokuExport(String teUid, String lec, Database db,Param para){
		
		this.teUid		=	teUid;
		this.lec		=	lec;
		this.db			=	db;
		this.para		=	para;
		
		te_lec_key		= KeyGen.get_te_lec_key2(teUid, lec);
		kdr				= new KamokuDefRecord(teUid,lec,db);
		crs				= new Course(kdr);

		downloadDir		= para.getDownloadDir(teUid);	// ここにzipを作成する
		tempDir			= para.exportDir(teUid);		// ここにzipにアーカイブするファイルがある
		FileGear.makeupDir(tempDir);
	
	}
	/**
	 * 科目名を返す
	 */
	public	String	kamokuName(){
		return	kdr.title();
	}
	/**
	 * 科目に関する全データをパックしたエキスポートファイルを
	 * ダウンロードエリアに作成する
	 */
	public	void	makeKamokuZipOnDownloadArea(){
		writeKamokuDef();
		writeZips();
		Zip	zip	=	new	Zip( getFilePath() );	// getFilePath() はzip ファイルへのフルパス
		makeZipFile(zip, tempDir);				// tempDir の中のファイルをzipアーカイブにする
		//
		// 作業ファイルの中身を全てクリアする（ディレクトリは消さない）
		FileGear.deleteFiles(tempDir);
	}
	public	String	getFileName(){
		return	te_lec_key + ".zip";
	}
	/**
	 * 作成するZipファイルへのフルパスを返す
	 * @return
	 */
	public	String	getFilePath(){
		return	downloadDir + getFileName();
	}
	/**
	 * 作成したzipファイルへのURLを返す
	 * @return
	 */
	public	String	getFileURL(){
		return	para.getDownloadURL(teUid) + getFileName();
	}
	/**
	 * zip ファイルを作成する
	 * @param zipObj
	 * @param targetDir
	 */
	public	void	makeZipFile(Zip zipObj, String targetDir){
		if(LOG.fa){
			LOG.println("");
			LOG.println("■■KamokuExport#makeZipFile(): ");
			LOG.println(     targetDir +" → " + zipObj.zipFileName());
		}
		try{
			zipObj.open();
			zipObj.zip(targetDir,"");
			zipObj.close();
		}catch(IOException e){
			//System.out.println(e);
			// 空のディレクトリでファイルがないと例外が発生し、
			// サイズ＝０のzipファイルができる
		}
	}
	/**
	 * 科目情報データ（html, eml, グラフィックスなど）を出力する
	 *
	 */
	public	void	writeZips(){
		// 
		String	emlPath			=	para.getEpmlPathName(teUid,lec);
		String	htmlPath		=	para.getHtmlPathName(teUid,lec);
		String	kadaigrphPath	=	para.getKamokuAttachPathName(teUid, lec);
		//
		Zip		emlZip			=	new	Zip(tempDir + KAMOKU_EML_FILE);
		Zip		htmlZip			=	new	Zip(tempDir + KAMOKU_HTML_FILE);
		Zip		kadaigrphZip	=	new	Zip(tempDir + KAMOKU_GRPH_FILE);
		//
		makeZipFile(emlZip, 	  emlPath);
		makeZipFile(htmlZip,	  htmlPath);
		makeZipFile(kadaigrphZip, kadaigrphPath);
		/*
		 * 空のディレクトリでファイルがないと例外が発生し、サイズ＝０のzipファイルができるので
		 * ここでそのようなzipファイルを削除する
		 */
		cleanFile(tempDir + KAMOKU_EML_FILE);
		cleanFile(tempDir + KAMOKU_HTML_FILE);
		cleanFile(tempDir + KAMOKU_GRPH_FILE);
	}
	/**
	 * 科目情報データのうちhtmlだけをzipアーカイブして出力する
	 * (refList.java で利用する)
	 * 出力先は /home/pc/<UID>/download/ である
	 *
	 * @return  ファイルが作成されたときファイル名を返す
	 *           ディレクトリが空でファイルを作成しなかったとき""を返す
	 */
	public	String	writeHtmlZip(){
		//
		String	zipPath			=	getHtmlZipPath();
		String	htmlPath		=	para.getHtmlPathName(teUid,lec);// htmlがあるディレクトリ
		Zip		htmlZip			=	new	Zip(zipPath);
		makeZipFile(htmlZip, htmlPath);
		/*
		 * 空のディレクトリでファイルがないと例外が発生し、サイズ＝０のzipファイルができるので
		 * ここでそのようなzipファイルを削除する
		 */
		boolean	ret	=	cleanFile(zipPath);
		if(ret){
			return	getHtmlZipFilename();	// ファイル名だけ
		}else{
			return	"";
		}
	}
	/**
	 * 科目情報データのうちhtmlだけをzipアーカイブして出力する時のファイル名を返す
	 * @return
	 */
	public	String	getHtmlZipFilename(){
		return	"html-" + lec + ".zip";
		
	}
	/**
	 * 科目情報データのうちhtmlだけをzipアーカイブして出力する時のファイルのフルパスを返す
	 * @return
	 */
	public	String	getHtmlZipPath(){
		return	para.getDownloadDir(teUid) + getHtmlZipFilename();
		
	}
	/**
	 * 科目情報データのうちhtmlだけをzipアーカイブして出力する時のファイルURLを返す
	 * @return
	 */
	public	String	getHtmlZipUrl(){
		return	para.getDownloadURL(teUid) + getHtmlZipFilename();
	}
	
	/**
	 * ファイルをチェックして、長さ０のファイルであれば削除する
	 * 
	 * @param 	fpath
	 * @return	長さ０のファイルがなければtrueを返す
	 */
	public	boolean	cleanFile(String fpath){
		
		File	fp	=	new	File(fpath);
		if(fp.length()==0){
			fp.delete();
			return	false;
		}
		return	true;
	}
	/**
	 *  科目定義情報をファイル出力する
	 */
	public	void	writeKamokuDef(){

		getSection();
		getRef();
		getKadai();
		//
		String	filename	= KAMOKU_DEF_FILE;	// "kamoku.def" (科目定義ファイル名．固定)
		writeObj(crs, tempDir + filename);		
	}
	/** 
	 * セクションの取得
	 */
	public	void	getSection(){
		KamokuSectionDEF	ksd	= new KamokuSectionDEF(te_lec_key,db);
		int	n1	= ksd.size();
		for(int	i=0; i<n1; i++){
			KamokuSecDefRecord	ksdr	= ksd.get(i);
			exSection			secObj	= ksdr.obj();
			crs.add_section(secObj);
		}		
	}
	/** 
	 * 資料レコードの取得
	 */
	public	void	getRef(){
		ReferenceDEF	rd	= 	new ReferenceDEF(te_lec_key,db);
		int	n2	= rd.size();
		for(int	i=0; i<n2; i++){
			ReferenceDefRecord	rdr		= rd.get(i);
			exReference			refObj	= rdr.obj();
			crs.add_reference(refObj);
		}		
	}
	/**
	 * 課題レコードの取得
	 */
	public	void	getKadai(){
		KadaiDEF	kd	= new KadaiDEF(te_lec_key,db);
		int	n3	= kd.size();
		for(int	i=0; i<n3; i++){
			KadaiDefRecord	krec	= kd.get(i);
			exKadai			kdObj	= krec.obj();
			crs.add_kadai(kdObj);
		}
	}
    /**
     * オブジェクトをファイル出力する
     * @param path
     * @param crs
     * @return
     */
    public	String	writeObj(Object crs, String path){	
	
		ObjectOutputStream  objOut  = null;
        try{
            objOut = new ObjectOutputStream(new FileOutputStream(path));
            objOut.writeObject(crs); // Serializable
            objOut.flush();
            objOut.close();
        }catch(IOException e){
        	path	=	null;
        	e.printStackTrace();
		}
		return path;
    }		
}
