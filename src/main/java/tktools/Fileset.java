/*
 * 
 */
package tktools;
import java.io.*;
import java.util.*;
import java.lang.Exception;

/**
 * 特定のディレクトリ内にあるファイルを扱うクラス
 * 
 */
public class Fileset implements StringEnumeration {
	//
	final String PS = File.separator;
	final String CR = System.getProperty("line.separator");
	//
	/** ポストされたファイルがあるディレクトリ ex. /home/pc/kawaba01/file/ck-0017   */
	String	postPath; 
	
	/** postPath から作った File オブジェクト */
	File	dirfp; 
	
	/** ファイル名の配列 */
	String  [] postedFiles; 
	
	/** ファイル名の配列（ディレクトリパスを含む） */
	String  [] postedDirFiles; 
	
	/** ファイル総数 */
	int	count; 
	
	int	pos;
	
	/**
	 * ディレクトリへのパスを引数に取るコンストラクタ
	 * 指定されたディレクトリがなければ作成する．あればファイルリストを作成する．
	 * @param path		対象とするディレクトリ
	 */
	public Fileset(String path){
		//System.out.println("class Files #Files() : コンストラクタです　／　path = " + path);
		postPath	= path;
		dirfp 		= new File(postPath);
		if(!dirfp.isDirectory()){
			dirfp.mkdirs();
			count	= 0;
		}else{
			count	= makeList();
		}
		pos	=	0;
	}
	/** ファイルリスト（postedDirFiles[]）を作成する */
	int	makeList(){
		postedFiles = dirfp.list(); // ファイル名の配列
		//
		// ファイル名をソートして，さらに計数を調べる
		if((postedFiles == null) || (postedFiles.length == 0)){
			return 	0;	// ファイル総数はゼロ
			//
		}else{
			Arrays.sort(postedFiles);					// ファイル名の順にソートする
			int 		n 	= postedFiles.length;		// ディレクトリパスを含む配列を作成する
			postedDirFiles 	= new String[n];
			for(int i=0; i<n; i++){
				postedDirFiles[i] = new String(postPath + PS + postedFiles[i]);
			}
			return postedFiles.length;
		}
	}
	public	StringEnumeration	files(){
		pos	=	0;
		return	this;
		
	}
	public	String	nextItem(){
		if(pos<count){			
			String	fname	=	postedFiles[pos];
			pos++;
			return		fname;
		}else{
			return	"";
		}
		
	}
	
	/** ファイル名の配列を返す */
	public	String [] list(){
		if(count==0)	return	null;
		
		String	[]flist	=	new String[count];
		for(int i=0; i<count; i++){
			flist[i]	= new String(postedFiles[i]);
		}
		return	flist;
	}
	/** ディレクトリパスを返す */
	public String getPath()	{ return postPath; }
	
	/** ファイル個数を返す */
	public int n()		{ return count; }

	/** 特定のファイルを消す */
	public void	deleteFile(String fname){
		//
		String	path	= postPath + PS + fname;
		File 	fp		= new File(path);
		fp.delete();
	}
	
	/** ポストされたファイルとそのディレクトリを全て再帰的に消す */
	public void delete(){
		delDir(postPath);
	}
	/**
	 * htmlファイル名、epmlファイル名を変更する
	 * @param oleFile		現在のファイル名
	 * @param newFile		変更したファイル名
	 */
	void	changeUrl(String olfFile, String newFile) {

		File	oldFP	=	new	File(postPath + PS + olfFile);
		File	newFP	=	new	File(postPath + PS + newFile);
		oldFP.renameTo(newFP);
	}	
	
	/** ファイル拡張子を取り出す */
	String getTriler(String filename){
		Csv chk = new Csv(filename,".");
		if(chk.size() <= 1) return "";	// 拡張子がないとき
		//
		return chk.get(chk.size() -1);	// 最後の要素
	}
	/**
	 * ディレクトリ内のファイルをサブディレクトリも含めてZIP圧縮する
	 * 出力ファイル名とバスは呼び出し側が指定する
	 * 
	 * zipするディレクトリはインスタンス変数 postPath 
	 * zip圧縮するファイルの配列はインスタンス変数 postedFiles 
	 * 
	 * @param dname	ZIPファイルを出力するディレクトリ 
	 * @param fname	作成するZiPファイル名
	 * @return
	 */
	public String createZip(String dname, String fname){ // outPath = dname + PS + fname <-- zip ファイルパス
	    
		/* 出力パスを作成する */
		File dir	= new File(dname);
		if(!dir.isDirectory()){			// ディレクトリがなければ作る
			dir.mkdirs();
		}
		String outPath = dname + PS + fname;
		Zip	zip	=	new Zip(outPath);
		try{
			zip.open();
			zip.zip(postPath,postedFiles,"");
			zip.close();
		}catch(IOException e){
			System.out.println("Zip ファイルを作れない。File#createZip()");
			System.out.println(e);
			return null;
		}
		return outPath;
	}
	/**
	 * 特定のテキストファイルを全て読み出して文字列データとして返す
	 * @param filename		読み出すファイル名．パスは規定のパス．
	 * @return				読み出したファイル内容
	 */
	public String get(String filename){
		StringBuffer	bf		= new StringBuffer(5000);
		String			data	= "";
		try {
			String	str;
			BufferedReader	in 		= new BufferedReader(new InputStreamReader(new FileInputStream(postPath + PS + filename),"Windows-31J"));
			boolean			flag	= false;
			while ((str = in.readLine()) != null) {
				if(flag)	bf.append(CR);
				bf.append(str);
			}
			data	= bf.toString();
		}catch(IOException e) {
			data	= "";	// ファイルがなければ "" を返す
		}
		return	data;
	}
	/**
	 * 特定のファイルをバイト列として読み込みＳＪＩＳにエンコードした文字列に直して返す
	 * 
	 * @param filename		読み出すファイル名．パスは規定のパス．
	 * @return				読み出したファイル内容
	 */
	public String getFile(String fname){
		//
		String	str;
		try{
			str	= getBytesFromFile(fname);
		}catch(UnsupportedEncodingException e1){
			str	= "";
		}catch(IOException e2){
			str	= "";
		}
		return	str;
	}
	String getBytesFromFile(String fname) throws IOException,UnsupportedEncodingException {
		//
		File	file	= new File(postPath + PS + fname);
		InputStream is 	= new FileInputStream(file);
		long 	length 	= file.length();	// Get the size of the file
		if (length > Integer.MAX_VALUE) {
			// File is too large
		}
		//
		byte[] bytes = new byte[(int)length];	// Create the byte array to hold the data
		// Read in the bytes
		int offset 	= 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
			offset += numRead;
		}
		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file "+file.getName());
		}
		is.close();
		String data = "";
		try{
			data	= new String(bytes,"Windows-31J");
		}catch(UnsupportedEncodingException e){
			System.out.println("■ class files #getBytesFromFile() : Windows-31Jエンコードができない");
			System.out.println(e);
		}
		return	data;
	}
	/**
	 * 特定のファイル名で書き出す
	 * @param filename		書き出すファイル名
	 * @param data			ファイルの全内容
	 */
	public void put(String filename,String data){
		//
		String		path 	= postPath + PS + filename;
		PrintWriter out		= null;
		try{
			out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path),"Windows-31J"),true);
			//String temp = Cp932.toJIS(data);
			///out.print(Cp932.toCp932(data));
			out.print(data);
		}catch (Exception exc) {
			System.out.println("■ class Files # put() : ファイルを書き出せない");
			System.out.println(exc);
		}
		out.close();
		return;
	}
	//-------------------------------------- U T I L -----------------------
	boolean makeDir(String dir){
		return (new File(dir)).mkdirs();
	}
	boolean delDir(String dir){
		File fp = new File(dir);
		return deleteDir(fp);
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
	// ----------------------------------------------------------------------
}