package tktools;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javax.swing.JOptionPane;
/**
 * 特定のディレクトリ（フォルダ）内の全てのファイルについて、タブをスペースに変換したファイルを作成する<br>
 * 指定のディレクトリ配下に 作業ディレクトリを作成し、そこに変換したファイルを作成する * 
 */
public class Tf {
	final	static char	TAB		=   '\t';
	final	static char	SPC		=   ' ';
	final	static String	FS		=	System.getProperty("file.separator");
	final	static String	TEMP	=	"changed$" + FS;
	final	static String	MSG101	=	"   ファイルのあるディレクトリ名を入力してください   ";
	final	static String	MSG102	=	"   処理を中止しました   ";
	final	static String	MSG001	=	"   変換終了しました.   ";
	final	static String	MSG002	=	"   指定されたディレクトリは存在しません  ";

	public	Tf(){
	}
	/**
	 * タブをスペースに変換したファイルを作成する 
	 * @param path	ファイルのあるディレクトリの名前
	 * @param wd	タブの幅
	 */
	public static void convert(String dir, int wd){
		
		if(doConvert(dir,wd)){
			JOptionPane.showMessageDialog(null,MSG001);
		}else{
			JOptionPane.showMessageDialog(null,MSG002);
		}
	}
	/**
	 * タブをスペースに変換したファイルを作成する 
	 * @param path	ファイルのあるディレクトリの名前
	 * @param wd	タブの幅
	 * @return		変換成功ならtrue, そうでなければ false
	 */
	static boolean doConvert(String dir, int wd){
		if(!isDir(dir))	{ return false; }
		String  dirName	= 	chkDir(dir);
		mkTempDir(dirName);
		int	width	=	wd;
		File	fp		=	new File(dirName);
		String []flist	=	fp.list();

		int	n	=	flist.length;
		for(int i=0; i<n; i++){
			toSpc(dirName, flist[i], width);
		}
		return	true;
	}
	/** ディレクトリが存在するかチェックする */
	static boolean isDir(String dirName){
		File	fp	= new File(dirName);
		return	fp.exists();
	}
	/** 変換ファイル用のフォルダを作成する */
	static void  mkTempDir(String dirName){
		File	tempDir	=	new File(dirName + TEMP);
		tempDir.mkdirs();
	}

	/** パスの最後にファイル区切り文字がなければ付加する */
	static String	chkDir(String path){
		String last	=	String.valueOf(path.charAt(path.length() - 1 ));
		if(!last.equals(FS)){
			path	=	path + FS;
		}
		return	path;
	}
	/** ひとつのファイルについて、タブをスペースに書き換える */
	static void toSpc(String dir, String fname, int width){
		
		if(!isFile(dir + fname))	return;
		//
		BufferedReader	in ;
		PrintWriter 	out;
		String str;
		String	src		=	dir + fname;
		String	dst		=	dir + TEMP + fname;
		try {
			in 		= new BufferedReader(new FileReader(src));
			out		= new PrintWriter(new BufferedWriter(new FileWriter(dst)));
			while((str=in.readLine())!=null){
				str =   tabToSpace(str,width);
				out.println(str);
			}
			in.close();
			out.close();
        
		} catch (IOException e) {
			e.printStackTrace();
		}
		File	fp	= new File (fname);
		fp.delete();
	}
	/** 普通のファイルかどうか調べる */
	static boolean isFile(String fpath){
		File fp	= new File(fpath);
		return	fp.isFile();
	}
	/** １行分のデータについてタブをスペースに変換 */
	static  String  tabToSpace(String s, int width){
        
		StringBuffer 	buf =	new StringBuffer();
		int 			n	=   s.length();
		int 			pos	=	0;	// 現在のカラム位置
		for(int i=0; i<n; i++){
			char    c   =   s.charAt(i);
			if(c==TAB){
				// タブを width - spcnt 個のスペースに変換する
				int spcnt	= pos % width;
				for(int j=0; j<width-spcnt; j++){
					buf.append(SPC);
					pos++;
				}
			}else{
				buf.append(c);
				pos++;
			}
		}
		return	buf.toString();
	}
}