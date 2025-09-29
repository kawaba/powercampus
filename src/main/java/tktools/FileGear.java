package tktools;

import java.io.*;
import java.util.*;

/**
 *  ファイルユーティリティクラス 
 *　(C) Takashi KAWABA 2004- 
 */
public class FileGear extends Object{
	//
	public	final static String FS = File.separator;
	public	final static String CR = System.getProperty("line.separator");
	//
	public FileGear()	{ }
	public	static	String	fileSeparator()		{	return	FS;		}
	public	static	String	lineSeparator()		{	return	CR;		}
	public	static	String	winLineSeparator()	{	return	"\r\n";	}
	/**
	 * 
	 */
	
	/**
	 * ディレクトリを含めてzip圧縮する
	 * （zipファイルを作成する）
	 *
	 * 例 
	 *	String	zipFile		=	"e:\\sample\\test\\zipping\\sample.zip";
	 *	String	targetDir	=	"d:\\test_zip1";
	 * 　
	 * 
	 * @param zipFile		作成するzipファイルへの完全パス
	 * @param targetDir		ファイルが存在するディレクトリ
	 */
	public	static void	zip(String newZipfile, String fromDir){
	    makeZipFile(newZipfile, fromDir);
	}
	public	static void	makeZipFile(String zipFile, String targetDir){

		/* 出力パスを検査する */
		File fp		= 	new File(zipFile);
		File dir	=	new File(fp.getParent());
		if(!dir.isDirectory()){			// ディレクトリがなければ作る
			dir.mkdirs();
		}

	    Zip	zipObj	= new	Zip(zipFile);
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
	 * zipファイルを指定のディレクトリへ解凍する
	 * @param zipFile
	 * @param toDir
	 */
	public	static	Vector	unzip(String zipFile, String toDir){
 		UnZip	uz			=	new	UnZip(zipFile, toDir);
 		Vector	fileList	=	uz.unzip();
 		return	fileList;
	}
	/**
	 * zipファイルかどうか
	 * @param fname
	 * @return
	 */ 
	public	static	boolean	isZipfile(String fname){
	    String	extTemp	=	getExt(fname);
	    if(Gear.isEmpty(extTemp)){
	    	// 拡張子がないファイル
	    	return	false;
	    }
		String	ext	=	extTemp.toLowerCase();
	    if(ext.equals("zip")){
	        return	true;
	    }else{
	        return	false;
	    }
	}
	/**
	 * ファイル名の拡張子を返す
	 * 
     * @param fname		ファイル名文字列
     * @return			拡張子、無い時は ""
	 */
	public	static	String	getExt(String fname){
		
		int	pos	=	fname.lastIndexOf('.');
		if(pos<0)	return	"";
		
		StringBuffer	bf	=	new StringBuffer();
		int				ln	=	fname.length();
		if(pos < ln -1){
			pos++;
		}else{
			return	"";
		}
		for(int i=pos; i<ln; i++){
			char	c	=	fname.charAt(i);
			bf.append(c);
		}
		if(bf.length()==0){
			return	"";
		}
		return	bf.toString();
	}
	/**
	 * ディレクトリを指すファイルポインタからその中にあるファイルリストを作成して返す<br>
	 * ファイルがない場合は null　を返す
	 */
	public	static String [] getFileList(File fp){
		String [] files	=	fp.list();
		if(files==null)		return	null;
		if(files.length==0)	return	null;
		return	files;
	}
	/**
	 * ／で区切られたURLの最後の部分を返す
	 * @param FileURL
	 * @return　URLの最後の部分
	 */
	public static String	getLastUrl(String url){
		Csv		cs	=	new Csv(url,"/",true);
		int	n	=	cs.size();
		if(n==0)	return "";
		
		/* 末尾のファイル名のみを返す */
		return	cs.get(n-1);
		
	}
	/**
	 * ／で区切られたURLの最後の部分をファイル名とみなし、
	 * 拡張子を含まないファイル名部分を返す．
	 * 
	 * @param FileURL
	 * @return
	 */
	public static String	getFileNameBody(String FileURL){
		Csv		cs	=	new Csv(FileURL,"/",true);
		int		n	=	cs.size();
		if(n==0)	return "";
		
		/* 拡張子を含まないファイル名のみを返す */
		return	fileNameBody(cs.get(n-1));
		
	}	
	/**
	 * ファイル名のうち、拡張子を含まない部分を返す
	 * @param FileName	有効なファイル名
	 * @return			拡張子を含まないファイル名
	 */
	public static String	fileNameBody(String	FileName){
		Csv		cs	=	new Csv(FileName,".",true);
		int		n	=	cs.size();	
		if(n==0)	return	"";
		
		/* 先頭のファイル名部分のみを返す */
		return	cs.get(0);
	}
	/**
	 * Windows-31J エンコーディングしてファイルに書き出す
	 * @param fname
	 * @param data
	 */
	public	static	boolean	putFileData(String fname, String data){
		try {
		   	BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fname), "Windows-31J"));
	        out.write(data);
	        out.close();
	    } catch (IOException e) {
			System.out.println("■  Gear#putFileData() : ファイルに書けませんでした.");
			System.out.println(e);
			return	false;
	    }
	    return	true;
	}	
	/**
	 * 特定のファイルをバイト列として読み込みＳＪＩＳとしてエンコードした文字列に直して返す
	 * つまり、データはWindows-31Jで作成されていることが前提
	 * @param fname
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws IOException
	 */
	public static String getFileData(String fname){
		return getStringFromFile(fname);
	}	
	public static String getFileToString(String fname) {
		return	getStringFromFile(fname);
	}
	public static String getStringFromFile(String fname) {
	
		byte[] bytes	=	null;
		try{
			bytes	=	fileToByteArray(fname);
		}catch(IOException e){
			System.out.println("■ FileGear #getStringFromFile() : ファイルを読めませんでした:" + fname);
			e.printStackTrace();
		}
		String 	data 	= 	"";
		try{
			data	= new String(bytes,"MS932");
		}catch(UnsupportedEncodingException e){
			System.out.println("■ FileGear #getBytesFromFile() : Windows-31Jエンコードができない");
			e.printStackTrace();
		}
		return	data;
	}
	/**
	 * ファイルをbyte[]に取り出す
	 * @param fname				ファイルへの完全パス
	 * @return					成功するとtrue
	 * @throws IOException		
	 */
	public static byte[] fileToByteArray(String fname) throws IOException {
		//
		File	file	= new File(fname);
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
		return	bytes;
	}
	/**
	 * ファイル名を変更する
	 * @param sou		元のファイルへの完全パス
	 * @param dest		変更先ファイルへの完全パス
	 * @return			成功するとture
	 */
	public	static	boolean	rename(String sou, String dest){
		File	souFp	=	new	File(sou);
		File	destFp	=	new	File(dest);
		return	souFp.renameTo(destFp);
		
	}
	/**
	 * ファイルを移動する.
	 * マルチパート入力でアップロードされたファイルは作業エリアにあるので、これを
	 * 正規の場所に移動するときに使う．<br>
	 * 
	 * @param dir			移動先ディレクトリ（なければ作成する）
	 * @param savedir		現在ファイルがあるディレクトリ
	 * @param mfilename	ファイル名（名前だけ）
	 * @return				正常終了のときtrue
	 * 
	 */
	public	static	boolean	getFile(String	dir, String savedir, String mfilename){
		return	getFile(dir, savedir, mfilename,1, true);
	}
	public	static	boolean	getFile(String	dir, String savedir, String mfilename,int fcnt, boolean dofile){
		
		String	dataFile	=	savedir + mfilename;
		// ファイル処理 
		if(!dofile){
			delDir(savedir);	// ファイルを受け取らない場合はディレクトリごと消去して戻る
			return true;
		}
		//
		// ファイルの存在チェック
		if(fcnt > 0){ // ファイルを読み込んでいれば１以上
			//
			File mfp   = new File(dataFile);
			//
			if(mfp.length()==0){    // （長さがゼロならファイル名の間違い）
				delDir(savedir);	// 作業用のディレクトリとファイルがあれば再帰的に消す
				return false;
			}
		}else{
			delDir(savedir);		// 作業用のディレクトリとファイルがあれば再帰的に消す
			return false;			// ファイルを読み込んでいない
		}
		// ファイルポストを受け付け＆ファイルがポストされている
		if(fcnt > 0){ 
			// 移動先ディレクトリ（例えば /home/pc/kawaba/file/102/000013/ ）のFileオブジェクトを返す
			//
			File 	mfp			= new File(dataFile);									// 一時ファイル格納場所（ここにある）
			File 	moveTo		= new File(dir);
			if(!moveTo.isDirectory()){													// ディレクトリがなければ作る
				moveTo.mkdirs();
			}
			boolean flag   = mfp.renameTo(new File(moveTo,mfilename));	// 移動処理
			delDir(savedir);												// ディレクトリを削除
			//
			if(!flag) {
				System.out.println("★ class stwork #doFile() : ポストされたファイルをリネームできない");
			}
		}else{
			delDir(savedir);	// ディレクトリとファイルを削除
		}
		return true;		
	}
	/**
	 * 移動元ディレクトリから移動先ディレクトリへファイルを移動する
	 * 空のファイルは移動せず削除する
	 * 移動元ディレクトリはいづれにせよ常に削除する
	 * 移動先に同名ファイルがあれば消す
	 *  
	 * @param toDir		移動先ディレクトリへの絶対パス
	 * @param fromDir		移動元ディレクトリへの絶対パス
	 * @return				成功した場合ture, それ以外はfalseを返す．
	 */
	public	static	boolean	moveFiles(String	toDir, String fromDir){
		
		File	from		=	new File(fromDir);
		File	to			=	new File(toDir);
		
		/* 有効なファイルがなければ移動元ディレクトリを削除してfalseを返す */
		if(isNoFile(from)){
			delDir(fromDir);
			return	false;
		}
		
		/* ディレクトリがなければ作る */
		if(!to.isDirectory()){ 
			to.mkdirs();
		}
		
		/* 全てのファイルを移動し、移動元ディレクトリを削除する */
		String	[]fileList	=	from.list();
		int	fcnt		=	fileList.length;
		for(int i=0; i<fcnt; i++){
			String	fname	=	fileList[i];
			File	wfp		=	new File(from.getPath() + FS + fname);
			/*
			 * 同名ファイルがあれば消す
			 */
			File	ckfp	=	new File(to.getPath() + FS + fname);
			if(ckfp.exists()){
			    ckfp.delete();
			}			
			boolean result	=	wfp.renameTo(new File(to,fname));
		}
		delDir(fromDir);
		return true;
	}
	/**
	 * ディレクトリ内に長さがゼロでないファイルがひとつもないのかどうか検査する
	 * また、長さゼロのファイルは削除する
	 * 
	 * @param 	fp	ディレクトリのファイルポインタ
	 * @return		長さがゼロでないファイルが少なくともひとつ以上あるときfalse, そうでなければtrue
	 */
	public static	boolean	isNoFile(File fp){
		String	dirPath	=	fp.getPath();
		String	[]files	=	fp.list();
		if(files==null)	return	true;
		int	cnt		=	files.length;
		if(cnt==0)	return	true;
		
		/* 空のファイルは削除しておく */
		int	empty	=	0;
		for(int i=0; i<cnt; i++){
			File	wfp		=	new File(dirPath + FS + files[i]);
			if(wfp.length()==0){
				empty++;
				wfp.delete();
			}
		}
		if(empty==cnt){
			return	true;
		}
		
		return	false;
	}
	/**
	 * 与えられた名前を元にランダムなディレクトリ名を生成する
	 * @param path		ディレクトリ名
	 * @return			ランダムなディレクトリ名
	 */
	public static String getTempdir(String path){
		//
		Random    r = new  Random(); // ミリ秒単位の現在時刻をシードとして乱数を発生
		//
		String s1 = "123456789";
		String s2 = "abcdefghkmnprstwxyz";
		int ln1   = s1.length();
		int ln2   = s2.length();
		//
        String p1,p2,p3,p4,p5,p6,p7,p8;
		int pos;
		pos = r.nextInt(ln2);	p1	= s2.substring(pos,pos+1);
		pos = r.nextInt(ln2);	p2	= s2.substring(pos,pos+1);
		pos = r.nextInt(ln2);	p3	= s2.substring(pos,pos+1);
		pos = r.nextInt(ln2);	p4	= s2.substring(pos,pos+1);
		pos = r.nextInt(ln1);	p5	= s1.substring(pos,pos+1);
		pos = r.nextInt(ln1);	p6	= s1.substring(pos,pos+1);
		pos = r.nextInt(ln1);	p7	= s1.substring(pos,pos+1);
		pos = r.nextInt(ln1);	p8	= s1.substring(pos,pos+1);
		//
		String dirname = path + p1 + p2 + p3 + p4 + p5 + p6 + p7 + p8 + File.separator;
		return dirname;
	}
	/**
	 * ファイルデータをVectorに格納する
	 * 
	 * @param fpath		ファイルへの完全パス
	 * @param Vhtml		内容を格納するVector
	 */
    public static void loadToVector(String fpath,Vector Vhtml){
        BufferedReader in = null;
        String         line;
        try{
            in = new BufferedReader(new InputStreamReader(new FileInputStream(fpath),"Windows-31J"));
            while((line=in.readLine())!=null){
                if(line.length() > 0) { Vhtml.add(line); }
            }
        }catch (IOException e){
            System.out.println("★ Gear #loadToVector(): can't read :" + fpath);
        }
    }
    /**
     * 
     * @param filename		ファイル名またはディレクトリ名
     * @return				成功するとtrue
     */
    //
	//  ファイルやディレクトリの存在チェック
	//  （存在する時 true を返す）
	//
	public static boolean isExistFile(String filename){
		boolean exists = (new File(filename)).exists();
    	return	exists;
	}
	/**
	 * ファイルのコピー(バイナリー版)
	 * If the dst file does not exist, it is created
	 * 
	 * @param 	srcPath		コピー元ファイルへの完全パス
	 * @param 	dstPath		コピー先ファイルへの完全パス
	 * @throws IOException
	 */
    public static void  copyBinryFile(String srcPath, String dstPath) throws IOException {
        //
		File	src	= new File(srcPath);
		File	dst	= new File(dstPath);
		/*
		 * 出力先のディレクトリがなければ作成する
		 */
		File	parent	=	new File( dst.getParent() );
		if(!parent.exists()){
		    parent.mkdirs();
		}
		
		InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);
        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }
    /**
     * ファイルのコピー（Windows-31J　テキスト版）
     * @param sfile		コピー元ファイルへの完全パス
     * @param dfile		コピー先ファイルへの完全パス
     * @return			成功するとtrue
     */
	public static boolean copyFile(String sfile,String dfile){
		
	    try{
			BufferedReader in  = new BufferedReader(new InputStreamReader(new FileInputStream(sfile),"Windows-31J"));
			PrintWriter	   out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dfile),"Windows-31J")));
			//
            String dt;
            while((dt=in.readLine())!=null){
				out.println(dt);
			}
		    in.close();
			out.close();
		}catch(IOException e){
			System.out.println(e);
			return false;
		}
		return true;
	}
	/**
	 * ディレクトリ作成
	 * 存在していないが必要な親ディレクトリも一緒に作成する
	 * 
	 * @param 		dir		削除するディレクトリ名	
	 * @return				成功するとtrue
	 */
	public static boolean makeDir(String dir){
		return (new File(dir)).mkdirs();
	}
	/**
	 * 再帰的にファイル名（相対パス文字列）を取得する
	 * 
	 * 起点となったディレクトリ名は含まない相対パス文字列
	 * 
	 * @param dir	捜査対象のディレクトリ名
	 * @param v    ファイル名文字列を入れるベクター
	 */
	public static String []  fileList(String	path) {
		//
	    File filefp	=	new	File(path);
	    
		if(!filefp.exists()){// 存在しないとき
			return	null;
		}
		if (!filefp.isDirectory()) {
		    return null;
		}
        String[] children = filefp.list();
        if(children==null){
            return	null;
        }
        Vector v	=	new	Vector(50);
        for (int i=0; i<children.length; i++) {
            fileListSub(new File(filefp, children[i]), v, children[i]);

        }
        /*
         * ソート済み文字列配列に変換する
         */
		return toSortedStringArray(v);
	}
	public static void fileListSub(File filefp, Vector v, String fname) {

		if (filefp.isDirectory()) {
		   
		    String[] children = filefp.list();
		    if(children==null)	return;
		    
            for (int i=0; i<children.length; i++) {
                fileListSub(new File(filefp, children[i]), v, fname+FS+children[i]);

            }
        }else{
            v.add(fname);
            
        }
		return;
	}
	public	static	String [] toSortedStringArray(Vector v){
	    
	    int		sz		=	v.size();
	    String	[]array	=	new String[sz];	
	    for	(int i=0; i<sz; i++){
	        array[i]	=	(String)v.get(i);
	    }
	    Arrays.sort(array);
	    return	array;
	}
	
	
	public static boolean delDir(String dir){
		File fp = new File(dir);
		return deleteDir(fp);
	}
    /**
     * 再帰的にファイルとディレクトリを消す
     * @param dir	削除するディレクトリのFILEオブジェクト
     * @return		成功するとtrue
     */
	public static boolean deleteDir(File dir) {
		//
		if(!dir.exists()){// 存在しないとき
			return	true;
		}
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
	
	/**
	 *  再帰的にファイルを消す（親ディレクトリは消さない）
	 * @param dir	ファイルのあるディレクトリ名
	 * @return		成功するとtrue
	 */
	public static boolean deleteFiles(String dir) {
		File	dirfp	=	new	File(dir);
		return	deleteFiles(dirfp);
	}
    /**
     * 再帰的にファイルを消す（親ディレクトリは消さない）
     * 
     * @param dir	ファイルのあるディレクトリのFileオブジェクト
     * @return		成功するとtrue
     */
	public static boolean deleteFiles(File dir) {
		
		if(!dir.exists()){// 存在しないとき
			return	true;
		}        
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
	/**
	 * ディレクトリがなければ作成し、あれば中のファイル・ディレクトリを再帰的に全て削除する
	 * @param dir	クリーンアップするディレクトリ
	 * @return		成功するとtrue
	 */
	public	static	boolean	makeupDir(String dir){
		return	makeupDir(new	File(dir));
	}
	/**
	 * ディレクトリがなければ作成し、あれば中のファイル・ディレクトリを再帰的に全て削除する
	 * @param dir	クリーンアップするディレクトリ
	 * @return		成功するとtrue
	 */
	public	static	boolean	makeupDir(File dir){
		if(!dir.isDirectory()){
			return	dir.mkdirs();
		}else{
			return	FileGear.deleteFiles(dir);
		}
	}
	/**
	 * オブジェクトを読込む
	 * @param 		ufp		ファイルポインタ
	 * @return		読込んだString
	 */
	public static Object readStringObj(File ufp){
		return	readObj(ufp);
	}
	public static Object readObj(File ufp){
		
		ObjectInputStream	objIn	= null;
		Object				obj		= null;
		try{
			objIn = new ObjectInputStream(new FileInputStream(ufp));
			try{
				obj = objIn.readObject();
				objIn.close();
			}catch(ClassNotFoundException e1){
				System.out.println("ClassNotFoundException:" + e1);
			}catch(IOException e2){
				System.out.println("IOException:" + e2);
			}
		}catch(IOException e3){
			System.out.println("can't Open :IOException:" + e3);
		}
		return obj;
	}
	/**
	 * String をオブジェクトとしてファイルに書き出す
	 * @param path		ファイルパス
	 * @param str		書き出す文字列
	 * @return			成功のときtrue ,そうでなければ false
	 */
	public static boolean  writeStringObj(String path,Object str){
		return	writeObj(path, str);
	}
	
	public static boolean  writeObj(String path,Object obj){

		//
		ObjectOutputStream  objOut  = null;
		try{
			objOut = new ObjectOutputStream(new FileOutputStream(path));
			try{
				objOut.writeObject(obj);
				objOut.flush();
				objOut.close();
				return true;
			}catch(IOException e1){
			}
		}catch(IOException e2){
		}
		return false;
	}
	/**
	 * オブジェクトとしてバイト配列に書き出す
	 * 
	 * @param object	書き出すオブジェクト
	 * @return			成功のときtrue ,そうでなければ false
	 */	
	public	static	byte[]	writeObjectToByteArray(Object object){

		byte[] buf =	null;
		try {
			// Serialize to a byte array
			ByteArrayOutputStream 	bos = new ByteArrayOutputStream() ;
			ObjectOutputStream		out = new ObjectOutputStream(bos) ;
			out.writeObject(object);
			out.close();
    
			// Get the bytes of the serialized object
			buf = bos.toByteArray();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		return	buf;
	}

}