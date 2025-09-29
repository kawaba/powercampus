/*
 *　ディレクトリを含めてzip圧縮する
 *  日本語名ファイルの場合はunzipでなければ復元できない
 *	2004.9.2 Takashi KAWAB  
 	

*/
 
package tktools;
import java.io.*;
import java.util.zip.*;

//import framework.LOG;


public class Zip {

	ZipOutputStream zout;
	String			zipFile;
	
	/**
	 * 出力ファイル名によるコンストラクタ
	 * @param zipFile		出力ファイル名
	 */
	public	Zip(String	zipFile){

		this.zipFile	=	zipFile;
	}
	
	
	/**
	 * zipファイル名を返す
	 * @return
	 */
	public	String	zipFileName(){
		return	zipFile;
	}
	/** zipストリームを開く */
	public	void	open()   throws IOException {
		/* 
		 * 出力ディレクトリがなければ作成する
		 */
		File 	wfp			=	new File(zipFile);
		String	parentdir	=	wfp.getParent();
		if(!Gear.isEmpty(parentdir)){
			FileGear.makeDir(parentdir);
		}
		//
		zout 		= 	new ZipOutputStream(new FileOutputStream(zipFile));
	}
	
	/** zipストリームを閉じる */
	public	void	close() throws IOException {
		zout.close();
	}
	/**
	 * 対象ディレクトリと対象ファイル名の配列を指定してzipファイルを作成する
	 * ファイル名の配列にディレクトリが含まれている場合は自身を呼び出して再帰的に処理する<p>
	 * 
	 * zip ファイルに登録されるファイルエントリーには / で区切った相対パスが含まれる
	 * 対象ディレクトリ名(parent)をファイルエントリーの基点とする<p>
	 * 
	 * 解答する場合は / を File.separatorChar に置き換えてから解凍する必要がある
	 * 
	 * @param baseEntry			対象ディレクトリ
	 * @param relativeEntry		zipアーカイブ内のファイルに付与する相対ディレクトリエントリー名．<br>
	 * 							相対パスなので最初は""を指定する.b
	 * 							再帰処理の途中で aseEntry以下に新たなディレクトリが出現するとrelativeEntryに追加する．
 	 * @param files				圧縮するファイル名の配列
	 * @throws IOException
	 */
	public  void zip(String baseEntry, String [] files, String relativeEntry) throws IOException {
		/*
	    if(LOG.fa){
			LOG.println("");
			LOG.println("■■Zip #zip(): ");
			LOG.println(     "baseEntry ------ " + baseEntry);
			LOG.println(     "relativeEntry -- " + relativeEntry);
			LOG.println(     "file list ------ " + ((files==null) ? "null" : String.valueOf(files.length)));
		}
		*/
		// 存在しないbaseEntryなら何もしない
		File ck	=	new File(baseEntry);
		if(!ck.exists()) return;
		
		for(int i = 0; i < files.length; i ++) {
	  		
	  		String	fname	= files[i];
	  		String	path	= baseEntry + File.separator + fname;
	  		File	fp		= new	File(path);
	  		if(fp.isDirectory()){
	  			/* ディレクトリ配下のファイルリストを作成して再帰的に zip() を実行する */
	  			String	relativeDir	=	"";
	  			
	  			if(Gear.isEmpty(relativeEntry)){
	  				relativeDir	=	fname;
	  			}else{
	  				relativeDir	=	relativeEntry + "/" + fname;
	  			}
	  			String []	subFiles	=	fp.list();
	  			zip(path, subFiles,relativeDir);

	  		}else{
	  			zipping(baseEntry, files[i], relativeEntry);
			}
	  	}
		return;
	}
	/**
	 * 対象ディレクトリを指定してzipファイルを作成する<p>
	 * アーカイブするファイル配列は対象ディレクトリから得る	 
	 * 
	 * zip ファイルに登録されるファイルエントリーには / で区切った相対パスが含まれる
	 * 対象ディレクトリ名(baseEntry)をファイルエントリーの基点とする<p>
	 * 
	 * @param baseEntry			対象ディレクトリ
	 * @param relativeEntry		zipアーカイブ内のファイルに付与する相対ディレクトリエントリー名．<br>
	 * 							相対パスなので最初は""を指定する.b
	 * 							再帰処理の途中で aseEntry以下に新たなディレクトリが出現するとrelativeEntryに追加する．
	 * @throws IOException
	 */
	public  void zip(String baseEntry, String relativeEntry) throws IOException {

		File 	wfp		=	new File(baseEntry);
		String	[]files	=	wfp.list();
		zip(baseEntry, files, relativeEntry);
		return;
	}	
	/**
	 * ひとつのファイルをzip圧縮する
	 * @param zout
	 * @param src
	 * @throws IOException
	 */
	public	void	zipping(String baseEntry, String fname, String relativeEntry) throws IOException {
		//System.out.println("○ parent      =" + baseEntry);
		//System.out.println("○ fname　     =" + fname);
		
		byte buf[] 			= new byte[512];
		String		path	= baseEntry + File.separator + fname;
		InputStream in 		= new FileInputStream(path);

		//String		entry	= path.replace(File.separatorChar,'/');
		String		entry	=	getEntry(relativeEntry,fname);
		ZipEntry 	e 		= new ZipEntry(entry);
		zout.putNextEntry(e);
		//System.out.println("★ entry　=" + entry);

		int len=0;
		while((len=in.read(buf)) != -1) {
			zout.write(buf,0,len);
		}
		zout.closeEntry();
	}
	
	public	String	getEntry(String relativeEntry, String fname){
		
		if(Gear.isEmpty(relativeEntry)){
			return	fname;
		}else{
			return	relativeEntry + "/" + fname;
		}
	}
	/** 進行状況表示：テスト用
	public static void print(ZipEntry e){
		PrintStream err = System.err;
		err.print("added " + e.getName());
		if (e.getMethod() == ZipEntry.DEFLATED) {
			long size = e.getSize();
		  	if (size > 0) {
				long csize = e.getCompressedSize();
				long ratio = ((size-csize)*100) / size;
				err.println(" (deflated " + ratio + "%)");
		
			}else {
				err.println(" (deflated 0%)");
			}
	 	}else{
	  		err.println(" (stored 0%)");
	  	}
	}
	*/

 	//テスト実行例 
	public	static	void	main(String[] args){
		
		//String	targetDir	=	"D:\\PowerCampusWin\\deploy\\deploy\\webapps";
		//String	zipFile		=	"D:\\PowerCampusWin\\deploy\\deploy\\packager_data\\PowerCampus-desktop-1.0.01.zip";

		String	targetDir	=	"e:\\テスト";
		String	zipFile		=	"D:\\temp.zip";

		//File	fp			=	new	File(targetDir);
		//String	[] files	=	fp.list();
		//if(files.length==0) { System.out.println("■ Zip #No file! " ); System.exit(0); }
		//		
		Zip	z	= new	Zip(zipFile);
		try{
			z.open();
			z.zip(targetDir,"");
			z.close();
		}catch(IOException e){
			System.out.println(e);	
		}
	}
}
 

