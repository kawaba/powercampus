package tktools;

import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.Vector;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.io.IOException;
import java.util.zip.ZipException;

/** zip形式のファイルをファイルシステムに展開する UnZip */

public class UnZip {

	/** 定数 */
	final	int EOF 	= -1;
	final	int BUFFER 	= 2048;

	/** zip形式のファイル */
	ZipFile zf;
	
	/** 解凍するZipファイルへのフルパス */
	String	zpath;
	
	/** 解凍したファイルのリスト **/
	Vector	fileList;
	
	/** 解凍先ディレクトリ名（末尾にセパレータを含まない） */
	String	outPath;
    
	/**
	 * コンストラクタ
	 * @param zpath			解凍するZipファイルへのフルパス
	 * @param outPath　　	解凍ファイルを出力するディレクトリ
	 */
	public		UnZip(String zpath, String	outPath){
		this.zpath		= 	zpath;
		this.outPath	=	outPath;
		fileList		=	new	Vector(50);
    }
	/**
	 * ひとつのエントリを解凍する
	 */
	public  Vector unzip() {
		
     	try {
            zf 					= new ZipFile( zpath );
            Enumeration	enm 	= zf.entries();
            while( enm.hasMoreElements() ) {
                  ZipEntry target = (ZipEntry)enm.nextElement();
                  saveEntry( target );
            }
            zf.close();
            
     	}catch( Exception e ){
     		e.printStackTrace();
     	}
     	return	fileList;
    }
	/**
	 * 与えられた ZipEntry の内容を解凍する
	 * @param  target			zipエントリー
	 * @throws ZipException
	 * @throws IOException
	 */
    public void saveEntry( ZipEntry target )  throws ZipException,IOException {
    	try {
    		
    		
    		// 出力ファイルパス
    		String	entry	=	(target.getName()).replace('/', File.separatorChar);
    		// 戻り値なので保存しておく
    		fileList.add(target.getName());
    		
    		String	fpath	=	outPath +  File.separator + entry;
    		File 	file 	= 	new File(fpath);
    		
    		// 出力ディレクトリがなければ作成する
    		String	dname	= file.getParent();
            if(dname!=null){
            	File 	dir = new File( dname );
            	dir.mkdirs();  // ディレクトリをリカーシブに作成する
            }    		
    		//System.out.println("□ output path      =" + fpath);

            // エントリーを出力する
            byte	buf[]	= new byte[BUFFER];
			int		count;
            BufferedInputStream		bis	= new BufferedInputStream(zf.getInputStream( target ));
            BufferedOutputStream	bos = new BufferedOutputStream( new FileOutputStream( file ) );
            while ((count = bis.read(buf, 0, BUFFER)) != -1) {
				bos.write(buf, 0, count);
			}
            bos.flush();
            bos.close();
            bis.close();
       }catch( ZipException e ){
              throw e;
       
       }catch( IOException e ){
              throw e;
       }
    }
  	//テスト実行例 
 	public	static	void	main(String[] args){
 		
		//String	destFile	=	"e:\\sample\\test\\zipping\\sample.zip";
		//String	targetDir	=	"e:\\unzip";

		String	destFile	=	"D:\\temp.zip";
		String	targetDir	=	"d:\\tempTest";

 		
 		UnZip	uz	=	new	UnZip(destFile, targetDir);
 		uz.unzip();
 		
 	}     
}
