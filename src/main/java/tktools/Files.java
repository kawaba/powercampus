//
package tktools;
import java.io.*;
import java.util.*;
import java.lang.Exception;
/*
   ○ポストされたファイルを操作するためのクラス 

   特定の教員の特定のクラスの特定の課題について、ポスト用ディレクトリのパスを与えて生成する
   ディレクトリが存在しない場合はクラス生成時に作成して、変数を初期化する
   ディレクトリが存在すれば、ファイル名のリストを作成し、その個数をカウントしておく
   
*/
public class Files {
	//
	final String PS = File.separator;
	final String CR = System.getProperty("line.separator");
	//
	String	postPath;			// ポストされたファイルがあるディレクトリ ex. /home/pc/kawaba01/file/ck-0017
	File	dirfp;				// postPath から作った File オブジェクト
	String  [] postedFiles;		// ファイル名の配列
	String  [] postedDirFiles;	// ファイル名の配列（ディレクトリパスを含む）
	int	counts;				// ファイル総数
	
	public Files(String path){
		//System.out.println("class Files #Files() : コンストラクタです　／　path = " + path);
		postPath	= path;
		dirfp 		= new File(postPath);
		if(!dirfp.isDirectory()){		// ディレクトリがなければ作る
			dirfp.mkdirs();
			counts	= 0;
		}else{
			counts	= makeList();		// ディレクトリがあればファイルリストを作成する
		}
	}
	// ファイルリスト（postedDirFiles[]）を作成する
	int	makeList(){
		postedFiles = dirfp.list(); // ファイル名の配列
        //
        // 解答ファイル名をソートして，さらに計数を調べる
        if((postedFiles == null) || (postedFiles.length == 0)){
			return 	0;	// ファイル総数はゼロ
			//
        }else{
            Arrays.sort(postedFiles);			// ファイル名（学籍番号）の順にソートする
            //
			
			int n = postedFiles.length;			// ディレクトリパスを含む配列を作成する
			postedDirFiles = new String[n];
			for(int i=0; i<n; i++){
				postedDirFiles[i] = new String(postPath + PS + postedFiles[i]);
				//if(DBG._tr090) System.out.println("postedDirFiles[" + i + "] = " + postedDirFiles[i]);
			}
			return postedFiles.length;
        }
	}
	//
	// ポストされたファイルとそのディレクトリを全て消す
	public void delete(){
		delDir(postPath);
	}
	// 特定の学籍番号のファイルのリストを作成して返す
	// ファイルがない場合は null を返す
	// （Answer.java, EvalList.java ,StKadai.java で利用）
	// 
	public String [] list(String sid){
		Vector	vl = new Vector(5,5);	// ファイル名リスト
		if(counts==0)	return	null;	// ファイル無し
		//
		// 読み飛ばし
		int i;
		for(i=0; ((i<counts) && (!(stUid(postedFiles[i])).equals(sid))) ; i++){
		}
		if(i==counts) return null;		// 該当無し
		//
		for( ; ((i<counts) && ((stUid(postedFiles[i])).equals(sid))) ; i++){
			vl.add( postedFiles[i] );
		}
		vl.trimToSize() ;
		int sz = vl.size();
		String [] stl = new String[sz];
		for(int k=0; k<sz; k++){
			stl[k] = new String( (String)vl.get(k) );
		}
		return stl;
	}
	// ポストされるファイルの名前は　"学籍番号-連番.拡張子" である
	// 学籍番号部分は、ファイルがポストされたときに binder2.java で自動的に作成する
	//
	// 　例:  宿題.doc  ⇒ d3820101-1.doc 
	//
	// 先頭の学籍番号　を取り出す
	// ないときは null を返す
	String stUid(String filename){
		if(filename == null) return		null;
		//
		Csv chk = new Csv(filename,"-");
		return chk.get(0);	// 最初の要素 （学籍番号）例：d3820101
	}
	//
	// ファイル拡張子を取り出す
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
	//
	// 全ファイルを zip にアーカイブしてそのファイルへのフルパスを返す
	// 　ZIP ファイル名 通常は "/home/pc/kawaba01/zip/ck-0017/ck-0017.zip" のようにフルパスで
	// 　課題キーをファイル名として与える
	//
	//  （EvalList.java Refer.java で利用している）
	//
	public String createZip(String dname, String fname){ // outPath = dname + PS + fname <-- zip ファイルパス
	    
		/* 出力パスを検査する */
		File dir	= new File(dname);
		if(!dir.isDirectory()){			// ディレクトリがなければ作る
			dir.mkdirs();
		}
		/*
		 * フルパスを作成する
		 */
		String outPath	=	"";
		int	len	=	dname.length();
		if(dname.charAt(len-1)==PS.charAt(0)){
			outPath	=	dname + fname;
		}else{
			outPath	= dname + PS + fname;
		}
		/*
		 * zipを作成する
		 */
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
	//
	// ディレクトリパスを返す
	public String getPath()	{ return postPath; }
	//
	// ファイル個数を返す
	public int n()		{ return counts; }
	public int size()		{ return counts; }
	//
	// 学籍番号でソートされたクラス全体のファイル名の配列を返す
	public String [] list() 	{ return postedFiles; }
	//
	
	//
	// 特定のファイルを全て読み出して文字列データとして返す
	//  (全くどこでも使っていない未使用メソッド－なくてもよい）
	//
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
	//
	// 特定のファイルをバイト列として読み込みＳＪＩＳにエンコードした文字列に直して返す
	// (RefEditor.java でしか使っていない）
	//
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
    public String getBytesFromFile(String fname) throws IOException,UnsupportedEncodingException {
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
	//
	// 特定のファイル名で書き出す
	// （RefEditor.java でしか使っていない）
	//
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
	// 特定のファイルを消す
	//
	public void	deleteFile(String fname){
		//
		String	path	= postPath + PS + fname;
		File 	fp		= new File(path);
		fp.delete();
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