package tktools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Random;
import java.util.StringTokenizer;
import java.util.Vector;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;
import jakarta.servlet.http.HttpServletRequest;
import framework.LOG;
import framework.Param;

/*
	■ toolクラス


*/
//
public class Gear extends Object{
	//
	public	final static String FS = File.separator;
	public	final static String CR = System.getProperty("line.separator");
	//
	/*
	// マルチパートインプットでハッシュに格納される情報のキー名	
	public	static final String	UPLOAD_DIR_KEY	=	"_savedir";
	public	static final String	UPLOAD_FILE_KEY	=	"_onlyFilename";
	public	static final String	UPLOAD_CNT_KEY	=	"_counts";
    */
    //
	private static final String LETTER = "abcdefghijklmnopqrstuvwxyz_ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGIT  = "1234567890";
    private static final String LETTERorDigit = ".abcdefghijklmnopqrstuvwxyz_ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    //
    static final String PASSPHRASE= "tkx280113";    // 暗号化のためのパスフレーズ
    //
    //簡易暗号化処理
    private static final String [] CRIPTO   = 
        {"7","_","5","P","d","R","A","3","Q","r","1","2","S","4","T","6","U","8","9","0",
         "a","b","c","V","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t",
         "u","v","w","x","y","z","B","C","D","E","F","G","H","I","J","K","L","M","N","O"};
    private   Hashtable    ech       = new Hashtable(20,10);
    private   Hashtable    dch       = new Hashtable(20,10);
	//
	public Gear()	{ }
	//
    //

	public	static	String	fileSeparator()		{	return	FS;		}
	public	static	String	lineSeparator()		{	return	CR;		}
	public	static	String	winLineSeparator()	{	return	"\r\n";	}
	

	/////////////////////////////////////////////////////////////////////////////////////
	//
	//  ネットワークユーティリティー
	//
	/////////////////////////////////////////////////////////////////////////////////////
	//
	//
	// サーバーのＩＰアドレスを得る
	//
	public static String getServerIP(HttpServletRequest req){
		String		hostName	= req.getServerName();
		InetAddress	inet 		= null;
		try{
			inet	= InetAddress.getByName(hostName);
		}catch(UnknownHostException e){
			System.out.println("UnknownHostException" + e);
		}
		Csv 	cs 		= new Csv(inet.toString(),"/");
		String ck_adr	= cs.get(1);
		return	ck_adr;
	}
    //
	/////////////////////////////////////////////////////////////////////////////////////
	//
	//  簡易暗号化ユーティリティー
	//
	/////////////////////////////////////////////////////////////////////////////////////
	//
	//
    //簡易暗号化処理(数字文字列しか暗号化できない)
    public  String cryptoDigit(String dig){
        //
        Random r    =   new  Random();
        int    pos;
        //
        StringBuffer    encrypted   = new StringBuffer(100);
        for(int i=0; i<dig.length(); i++){
            String  s0 = String.valueOf(dig.charAt(i)); 
            String  s1 = (String)ech.get(s0);   // 最初の10文字が数字のエンコード
            //if(DBG) System.out.println("(" + s0 + "," + s1 + ")");
            encrypted.append(s1);
            //
            pos = 9 + r.nextInt(50);            // CRIPTOの１０番目以降からランダムに１文字取って加える
            encrypted.append( CRIPTO[pos] );    // したがって 奇数番目の文字はダミー（0 origin）
        }
        return encrypted.toString();
    }
    public  String decryptoDigit(String crpt){
        //
        StringBuffer    decrypted   = new StringBuffer(100);
        for(int i=0; i<crpt.length(); i++){
            String  s1 = String.valueOf(crpt.charAt(i));
            String  s0 = (String)dch.get(s1);
            if(i%2==0) decrypted.append(s0);    // 偶数番目のみとる
        }
        return decrypted.toString();
    }
    //
	/////////////////////////////////////////////////////////////////////////////////////
	//
	//  電子メールユーティリティー
	//
	/////////////////////////////////////////////////////////////////////////////////////
	//

	//
    //  一通のメールを送信する
    //

	public	static	void send_To_email(String to,String from,String title,String body,Param para){
		//
		String host	= para.getMailhost();
		//
		String sysUser	= "";
		String sysPass	= "";
		String pbs = para.popBeforSmtp();
		if(pbs.equals("yes")){
			sysUser	= para.getMailmaster();
			sysPass = para.getMailmasterPass();
			boolean ret = jmAuthenticate.authenticate(host,sysUser,sysPass);
		}
		send(host,to,from,title,body);
	}
	//	一通の携帯メールを送信する
	public	static	void send_To_keitai(String to,String from,String title,String body,Param para){
		//
		String host	= para.getMailhostToKeitai();
		//
		String sysUser	= "";
		String sysPass	= "";
		String pbs = para.popBeforSmtp_k();
		if(pbs.equals("yes")){
			sysUser	= para.getMailmaster_k();
			sysPass = para.getMailmasterPass_k();
			boolean ret = jmAuthenticate.authenticate(host,sysUser,sysPass);
		}
		send(host,to,from,title,body);
	}
	//
	//	  メッセージ送信
	//
	public	static	void send(String host,String to,String from, String title,String msg){
		try{
	       jmSender.send(host,to,from,title,msg);
	   }catch(AddressException e2){
	   }catch(MessagingException e1){
	   }
	}
    // メールアドレスとして正しい形式か
	//
	public static boolean isMailaddress(String s){
		//
		// ヌル文字でない
		if(s == null) 			return	false;
		//
		// ３文字以上　x@y の文字列である
		int len = s.length();
		if( len < 3)			return	false;
		//
		// @ が含まれいている
		int pos = s.indexOf("@");
		if(pos == -1) 			return false;
		// @ の前後に文字がある
		if(s.endsWith("@"))		return false;
		if(s.startsWith("@"))	return false;
		//
		return true;
	}
    //
	/////////////////////////////////////////////////////////////////////////////////////
	//
	//  ファイルユーティリティー
	//
	/////////////////////////////////////////////////////////////////////////////////////
	//
	/**
	 * ディレクトリを指すファイルポインタからその中にあるファイルリストを作成して返す
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
	 * 特定のファイルをバイト列として読み込みＳＪＩＳにエンコードした文字列に直して返す
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
			System.out.println("■ FileGear #getStringFromFile() : ファイルを読めませんでした");
			e.printStackTrace();
		}
		String 	data 	= 	"";
		try{
			data	= new String(bytes,"Windows-31J");
		}catch(UnsupportedEncodingException e){
			System.out.println("■ FileGear #getBytesFromFile() : Windows-31Jエンコードができない");
			e.printStackTrace();
		}
		return	data;
	}
	/**
	 * ファイルをbyte[]に取り出す
	 * @param fname
	 * @return
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
	 * ファイルを移動する.
	 * マルチパート入力でアップロードされたファイルは作業エリアにあるので、これを
	 * 正規の場所に移動するときに使う．<br>
	 * 
	 * @param dir			移動先ディレクトリ（なければ作成する）
	 * @param savedir		現在ファイルがあるディレクトリ
	 * @param mfilename	ファイル名（名前だけ）
	 * @param fcnt			ファイル個数
	 * @param dofile		ファイルを削除するとき false をセットする
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
			if(LOG.fa) LOG.println("■ Gear #getFile() : file name = " + dataFile);
			File mfp   = new File(dataFile);
			//
			if(mfp.length()==0){    // （長さがゼロならファイル名の間違い）
				if(LOG.fa) { LOG.println("class stwork #doFile() : ★ 指定されたファイルは存在しません");} 
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
				LOG.println("★ class stwork #doFile() : ポストされたファイルをリネームできない");
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
	 * 移動先に同名ファイルがあれば消してから移動する
	 *  
	 * @param toDir			移動先ディレクトリへの絶対パス
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
            e.printStackTrace();
        }
    }
    /**
     * 
     * @param filename
     * @return
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
     * @return
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
	 * @param 		dir
	 * @return
	 */
	public static boolean makeDir(String dir){
		return (new File(dir)).mkdirs();
	}
	/**
	 * 再帰的にファイルとディレクトリを消す
	 * 
	 * @param dir	削除するディレクトリ名
	 * @return		成功するとtrue
	 */
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
	 * オブジェクトを読込む
	 * @param 		ufp		ファイルポインタ
	 * @return		読込んだString
	 */
	public static Object readStringObj(File ufp){
		ObjectInputStream	objIn	= null;
		Object				str		= null;
		try{
			objIn = new ObjectInputStream(new FileInputStream(ufp));
			try{
				str = objIn.readObject();
				objIn.close();
			}catch(ClassNotFoundException e1){
				System.out.println("ClassNotFoundException:" + e1);
				str = null;
			}catch(IOException e2){
				System.out.println("IOException:" + e2);
				str = null;
			}
		}catch(IOException e3){
			System.out.println("can't Open :IOException:" + e3);
			str = null;
		}
		return str;
	}
	/**
	 * String をオブジェクトとしてファイルに書き出す
	 * @param path		ファイルパス
	 * @param str		書き出す文字列
	 * @return			成功のときtrue ,そうでなければ false
	 */
	public static boolean  writeStringObj(String path,Object str){
		//
		ObjectOutputStream  objOut  = null;
		try{
			objOut = new ObjectOutputStream(new FileOutputStream(path));
			try{
				objOut.writeObject(str);
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
	 * @param str		書き出すオブジェクト
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
    //
	/////////////////////////////////////////////////////////////////////////////////////
	//
	//  文字列ユーティリティー
	//
	/////////////////////////////////////////////////////////////////////////////////////
	//
	
	/*
	 * 文字列をbyte型の１６進文字列に変換する
	 */
	public	static String toHexString(String str){
		
		StringBuffer	buf	=	new	StringBuffer();
		byte [] 		b	=	str.getBytes();
		
		boolean	flag = false;
		for(int i=0; i<b.length; i++){
			if(flag){
				buf.append(" ");
			}
			buf.append(byteToHexString(b[i]));
			flag	= true;
		}
		return	buf.toString();
	}
	
	/**
	 * byte value to hexString.
	 * 
	 * @param b byte-data
	 * @return hexa-decimal string of byte b.
	 */
	public	static String byteToHexString(byte b) {
		String s = "0" + Integer.toHexString(b & 0xFF);
		String hex = s.substring(s.length() - 2, s.length());
		return hex.toUpperCase();
	}
	
	/**
	 * 文字に記号⇒特殊文字の変換を掛けてから返す
	 */
	public	static String	doSafty(String buf){
		
		if(isEmpty(buf))	return	"";
		return	regularize(buf);
		
	}
	/**
	 * ハッシュから取り出した文字に記号⇒特殊文字の変換を掛け、ハッシュに戻しておく
	 */
	public	static void	revConvertHash(Hashtable htb,String key){
		
		String	buf	=	strHash(htb,key);
		if(isEmpty(buf)){
			htb.put(key,""); 
		}else{
			htb.put(key,regularize(buf));
		}
	}
	/** < と > と " を特殊文字に直す */
	public	static String	regularize(String	str){
		String	temp1	=  	substitute(str,  "<","&lt;");
		String	temp2	=  	substitute(temp1,">","&gt;");
		
		String	temp3	=	toDefindStr(temp2);
		return	temp3;
	}
	/** < と > と " を特殊文字に戻す */
	public	static String	antiRegularize(String	str){

		return str; // 戻さない　2023.3.29
		
	//	String	temp1	=  substitute(str,"&lt;",  "<");
	//	String	temp2	=  substitute(temp1,"&gt;",">");
	//	String	temp3	=	toNormalString(temp2);
	//	return	temp3;
	}
	/**  \ " ' を特殊文字に直す */
	public	static String	toDefindStr(String	str){
		String	temp1	=  substitute(str,"\"","&quot;");
		String	temp2	=  substitute(temp1,"\'","&rsquo;");
		String	temp3	=  substitute(temp2,"\\","&yen;");
		return	temp3;
	}
	/** 特殊文字を \ ' " に戻す */
	public	static String	toNormalString(String	str){
		
		return str;	// 戻さない　2023.3.29
		
		//	String	temp1	=  substitute(str,"&quot;","\"");
	//	String	temp2	=  substitute(temp1,"&rsquo;","\'");
	//	String	temp3	=  substitute(temp2,"&yen;","\\");
	//	return	temp3;
	}	
	// 
	// 文字列 str 中の全ての pattern を replace に置き換える
	public	static String replace(String str, String pattern, String replace) {
		int s = 0;
		int e = 0;
		StringBuffer result = new StringBuffer();
		//
		while ((e = str.indexOf(pattern, s)) >= 0) {
			result.append(str.substring(s, e));
			result.append(replace);
			s = e+pattern.length();
		}
		if(s==0){
			return	str;
		}
		result.append(str.substring(s));
		return result.toString();
	}
	
	// source 文字列の全ての target を rep に置き換える
	public static String	substitute(String source,String target,String rep){
		StringBuffer	buf	=	new StringBuffer(1024);
		substitute_sub(buf, source, target, rep);
		return buf.toString();
	}
	public static void substitute_sub(StringBuffer buf, String source,String target,String rep){
		//
		int	pos 		= source.indexOf(target);
		if(pos<0){
			buf.append(source);
			return; 
		}
		//
		int	len			= target.length();
		String	str1	= "";
		String	str2	= "";
		//
		try{
			buf.append(source.substring(0,pos));
			buf.append(rep);
		}catch(IndexOutOfBoundsException  e){
		}
		try{
			substitute_sub(buf,source.substring(pos+len),target,rep);
		}catch(IndexOutOfBoundsException  e){
		}
		return;
	}	
	//
	// 文字列 line 内の変数をハッシュ表 ht の変数で置き換えた
	// 結果の文字列を返す
	public static String replace(String line,Hashtable ht){
		StringBuffer bf = new StringBuffer(1000);
		if(line==null) return "";
		//
		StringTokenizer st = new StringTokenizer(line,"%");
		boolean printFlag = true;
		while(st.hasMoreTokens()){
			String tk = st.nextToken();
			if(tk.charAt(0)=='_'){
				String ps = (String)ht.get(tk); //ハッシュ表を参照
				if(ps==null){
					bf.append(tk);	//ハッシュになければ変換しない
				}else{
					bf.append(ps);
				}
			}else{
				bf.append(tk);
			}
		}
		return	bf.toString();
	}
	/*
	//
	// 文字列 line 内の変数をハッシュ表 ht の変数で置き換えた
	// 結果の文字列を返す
	public static String substitute(String line,Hashtable ht){
		StringBuffer bf = new StringBuffer(1000);
		if(line==null) return "";
		//
		StringTokenizer st = new StringTokenizer(line,"%");
		boolean printFlag = true;
		while(st.hasMoreTokens()){
			String tk = st.nextToken();
			if(tk.charAt(0)=='_'){
				String ps = (String)ht.get(tk); //ハッシュ表を参照
				if(ps==null){
					bf.append(tk);	//ハッシュになければ変換しない
				}else{
					bf.append(ps);
				}
			}else{
				bf.append(tk);
			}
		}
		return Cp932.toCp932(bf.toString());
	}
	*/
    //
    // 文字列のnull，空白チェック
    public static boolean isSpaceOrNull(String s){
        if((s != null) && (s.length() > 0) && !isSpace(s)) return false;
        return true;
    }
    // 漢字空白を含んで、全て空白文字かどうかチェックする
    public static boolean isSpace(String s){
        if((s.length()==0)||(s==null)) return false;
        String  space = " ";   // 空白文字
        //
        String s1;
        int n = s.length();
        for(int i=0; i<n; i++){
            s1 = s.substring(i,i+1);
            if((!s1.equals(space))&&(!s1.equals("　"))){ //  ascii と漢字の空白文字を比較
                //System.out.println(s + " = not Space");
                return false;
            }
        }
        // System.out.println(s + " = Space !");
        return true;
    }
    //
    // 文字列が数字がどうかチェックする
    public static boolean isDigitx(String s){
        if((s == null)||(s.length()==0))    return false;
		//
        int len = s.length();
        for(int i=0; i<len; i++){
            char ch = s.charAt(i);
            if(!xisDigit(ch))       return false;
        }
        return true;
    }
	// 文字列が空かどうかテストする
	//
	public static boolean isEmpty(String str){
		if(str==null) 			return  true;
		if(str.length()==0)	return  true;
		return false;
	}
	// 前後の空白を除いて、空かどうかテストする
	public static boolean isEmptyData(String str){
		if(str==null) 					return  true;
		if((str.trim()).length()==0)	return  true;
		return false;
	}
	//
	// 数字文字としては空かどうかテストする
	public static boolean isEmptyDigit(String s){
		if(isEmpty(s)){
			return	true;
		}else	if(!isDigitx(s.trim())){
			return	true;
		}
		return	false;
	}
    // 文字列がｎ桁の英字かどうかチェックする
    public static boolean isNLetter(String s,int n){
        if((s == null)||(s.length()==0))    return false;
        if(s.length() != n )                return false;
        //
        for(int i=0; i<n; i++){
            char ch = s.charAt(i);
            if(!xisLetter(ch))  return false;
        }
        return true;
    }
    //
    // 文字列がｎ桁の数字がどうかチェックする
    public static boolean isNDigit(String s,int n){
        if((s == null)||(s.length()==0))    return false;
        if(s.length() != n )                return false;
        //
        for(int i=0; i<n; i++){
            char ch = s.charAt(i);
            if(!xisDigit(ch))       return false;
        }
        return true;
    }
    // 文字列が英数字からなるかどうかチェックする
    public static boolean isHankaku(String s){
        if((s == null)||(s.length()==0))    return false;
        int n = s.length();
        //
        for(int i=0; i<n; i++){
            char ch = s.charAt(i);
            if(!xisLETTERorDigit(ch))   return false;
        }
        return true;
    }
    // 文字列がｎ桁以上ｍ桁以下の英数字からなるかどうかチェックする
    public static int isHankaku(String s,int min,int max){
        if((s == null)||(s.length()==0))    return -1;
        int n = s.length();
        //
		if(n < min) return -1;
		if(n > max) return 1;
		
        for(int i=0; i<n; i++){
            char ch = s.charAt(i);
            if(!xisLETTERorDigit(ch))   return 9;
        }
        return 0;
    }
    //
    public static boolean xisLetter(char ch){
        int n = LETTER.length();
        for(int i=0; i<n; i++){
            if(ch == LETTER.charAt(i))  return  true;
        }
        return false;
    }
    public static boolean xisDigit(char ch){
        int n = DIGIT.length();
        for(int i=0; i<n; i++){
            if(ch == DIGIT.charAt(i))   return  true;
        }
        return false;
    }
    public static boolean xisLETTERorDigit(char ch){
        int n = LETTERorDigit.length();
        for(int i=0; i<n; i++){
            if(ch == LETTERorDigit.charAt(i))   return  true;
        }
        return false;
    }
	public	static	int	toInt(String s, int	init){
		int	n	= init;
		try{
			n	=	Integer.parseInt(s);
		}catch(NumberFormatException e){
			n	=	init;
		}
		return	n;
	}
    //
	/////////////////////////////////////////////////////////////////////////////////////
	//
	//  日付ユーティリティー
	//
	/////////////////////////////////////////////////////////////////////////////////////
	//
    //
	/**
	 * 現在の年度を得る
	 * @return
	 */
	public static String	getyear(){
		if(LOG.fa) LOG.println("■ Gear #getyear()");
		
		GregorianCalendar	cal		=	new GregorianCalendar();
		int					year	=	cal.get(Calendar.YEAR);
		int					month	=	cal.get(Calendar.MONTH);
		if(LOG.fa){
			LOG.println("  □ 年:" + year);
			LOG.println("  □ 月:" + month);
		}
		
		if(month<2){ // ２月までを同じ年度とする
			year--;
		}
		return	String.valueOf(year);
		
	}
	/**
	 * 現在の月に対応したシーズン文字列を返す<br>
	 * 
	 * 例えば現在が５月なら getSeason("4,4,4,9,9,9,9,9,9,9,9,9") は文字列 "9" を返す
	 * 例えば現在が２月なら getSeason("4,4,4,9,9,9,9,9,9,9,9,9") は文字列 "4" を返す
	 * 
	 * @param termCsv	シーズンを表す文字列を12個並べたCSV文字列．<br>
	 *                  最初の文字が１月に、最後の文字が１２月に対応する．<br>
	 * 　　　　　　　　　例：　"b,b,b,a,a,a,a,a,b,b,b,b"　（規定値）<br>
	 *                  指定しないと上の規定値が使われる．
	 * 
	 * @return			現在の月に対応するシーズン文字列
	 */
	public static String	getSeason(String termCsv){
		if(LOG.fa) LOG.println("■ Gear #getyear()");
		
		/*
		 * シーズン文字列を配列にセットする
		 */
		Csv	term	=	null;
		if(isEmpty(termCsv)){
			term	=	new	Csv("b,b,b,a,a,a,a,a,b,b,b,b");
		}else{
			term	=	new	Csv(termCsv);
			if(term.size()!=12){
				term	=	new	Csv("b,b,b,a,a,a,a,a,b,b,b,b");
			}
		}
		/*
		 * 現在の月を得る
		 */
		GregorianCalendar	cal		=	new GregorianCalendar();
		int					month	=	cal.get(Calendar.MONTH);
		if(LOG.fa){
			LOG.println("  □ 月:" + month);
		}
		/*
		 * 月に応じたシーズン文字列を返す
		 */
		return	term.get(month);
	}
	public static String	getSeason(){
		return	getSeason("");
	}
	/**
	 * ミリセコンドを表す文字列からフォーマットされた日付文字列を作成して返す
	 */
	public	static	String	gatDateFromMiliStr(String miliStr){
		if(Gear.isEmpty(miliStr))	return	"";
		long	mili	=	Long.parseLong(miliStr);
		return	gatDateFromMili(mili);
	}
	/**
	 * ミリセコンドを表すlong の整数値からフォーマットされた日付文字列を作成して返す
	 * @param mili
	 * @return
	 */	
	public	static	String	gatDateFromMili(long mili){
		GregorianCalendar	cal	=	new	GregorianCalendar();
		cal.setTimeInMillis(mili);
		return	getFormattedDate(cal,"yyyy/MM/dd HH:mm:ss");		
	}
	/** カレンダーオブジェクトから form で指定した書式の日付文字列を得る */
	public static String getFormattedDate(Calendar cal,String form){
		SimpleDateFormat format = new SimpleDateFormat(form);
		String strDate = format.format(cal.getTime());
		return strDate;
	}	
    // 今日の日付のカレンダーオブジェクトを返す
    public static GregorianCalendar currentDay(){
        return new GregorianCalendar();
    }
    // cal から minutes だけ先のカレンダーオブジェクトを計算して返す
    public static GregorianCalendar calculateDay(GregorianCalendar cal,int minutes){
        return calculateDayM(cal,minutes);
    }
    public static GregorianCalendar calculateDayM(GregorianCalendar cal,int minutes){
        GregorianCalendar cc = calculateDayMinutes(cal,minutes,true);
        return cc;
    }
    // cal から hours だけ先のカレンダーオブジェクトを計算して返す
    public static GregorianCalendar calculateDayH(GregorianCalendar cal,int hours){
        GregorianCalendar cc = calculateDayHour(cal,hours,true);
        return cc;
    }
    //  cal から minutes だけ先（前）のカレンダーオブジェクトを計算して返す
    public static GregorianCalendar calculateDayMinutes(GregorianCalendar cal,int minutes,boolean sw){
        if(sw){
            cal.add(Calendar.MINUTE,minutes);
        }else{
            cal.add(Calendar.MINUTE,-1*minutes);
        }
        return cal;
    }
    //  cal から hour だけ先（前）のカレンダーオブジェクトを計算して返す
    public static GregorianCalendar calculateDayHour(GregorianCalendar cal,int hours,boolean sw){
        if(sw){
            cal.add(Calendar.HOUR_OF_DAY,hours);
        }else{
            cal.add(Calendar.HOUR_OF_DAY,-1*hours);
        }
        return cal;
    }
    // 特定の日付を yyyy-mm-dd-HH-MM の形ｮの文字列で返す
    public static String CalToStr(GregorianCalendar date){
        return CalToStr(date,true);
    }
    public static String CalToStr(GregorianCalendar date,boolean sw){
        int _yy = date.get(Calendar.YEAR);
        int _mm = date.get(Calendar.MONTH) + 1;
        int _dd = date.get(Calendar.DATE);
        int _HH = date.get(Calendar.HOUR_OF_DAY);   // 2003.2.15
        int _MM = date.get(Calendar.MINUTE);
        //
        String yy = String.valueOf(_yy);    // 4桁固定
        String mm = get00type(_mm);
        String dd = get00type(_dd);
        String HH = get00type(_HH);
        String MM = get00type(_MM);
        //
        String datestr = yy +  mm +  dd +  HH +  MM;
        if(!sw) datestr = yy + "-" + mm +  "-" + dd +  "/ " + HH +  ":" + MM;
        return datestr;
    }
	// 年、月で特定される日付けを CalToStr と同じ文字列にして返す
    public static String CalToStr_app(int yy,int mm,boolean sw){
		GregorianCalendar theDay = new GregorianCalendar(yy, mm-1, 1,0,0,0); // yy年mm月1日0時0分0秒
		return CalToStr(theDay, sw);
	}
	//
    public static String get00type(int s){
        String  dt      = String.valueOf(s);
        if((dt == null)||(dt.length() == 0)) return "00";
        int     pos     = dt.length();
        String  pattern = "00" + dt;
        return  pattern.substring(pos);
    }
    public static String get000type(int s){
        String  dt      = String.valueOf(s);
        if((dt == null)||(dt.length() == 0)) return "000";
        int     pos     = dt.length();
        String  pattern = "000" + dt;
        return  pattern.substring(pos);
    }
	public static String get0000type(int s){
		String  dt      = String.valueOf(s);
		if((dt == null)||(dt.length() == 0)) return "0000";
		int     pos     = dt.length();
		String  pattern = "0000" + dt;
		return  pattern.substring(pos);
	}
	public static String get00000type(int s){
		String  dt      = String.valueOf(s);
		if((dt == null)||(dt.length() == 0)) return "00000";
		int     pos     = dt.length();
		String  pattern = "00000" + dt;
		return  pattern.substring(pos);
	}	
    public static String getSStype(int s){
        String  dt      = String.valueOf(s);
        if((dt == null)||(dt.length() == 0)) return "00";
        int     pos     = dt.length();
        String  pattern = "  " + dt + " ";
        return  pattern.substring(pos);
    }
    //
    //現在の日付の文字列を得る
    public static String getDate(){
        return getDate("yyyy年MM月dd日HH時mm分ss秒");
    }
    // yyyyMMddHH などを指定する
    public static  String getDate(String form){
        SimpleDateFormat format = new SimpleDateFormat(form);
        String strDate = format.format(Calendar.getInstance().getTime());
        return strDate;
    }
    // うるう年かどうか(すこしいい加減)
    public boolean isLeapYear(String yy){
        int ck = Integer.parseInt(yy) % 4;
        return ck == 0;
    }

	
	// メッセージを出力してクライアントに返す
    // プログラムはここで終了する
    public static void errPrint(PrintWriter out,String str){
        out.println("<html>");
        out.println("<head><title>Servlet1</title></head>");
        out.println("<body>");
        out.println("* * *  致命的なエラーのために処理を中止しました  * * *<p>" );
        out.println(str);
        out.println("</body></html>");
        out.close();
    }
	
    //
	/////////////////////////////////////////////////////////////////////////////////////
	//
	//  デバッグユーティリティー
	//
	/////////////////////////////////////////////////////////////////////////////////////
	//
	//
	

	public static	void	setupHash(Hashtable ht, String key,Object dt) throws tkException {
		if(dt==null){
			throw (new tkException("★ Gear #setupHash(): " + key + " is null !")); 
		}else{
			 ht.put(key,dt);
		}
	}
	/**
	 * 返す値がnullだったら""を返す
	 * @param htb
	 * @param key
	 * @return
	 */
	public static String strHash(Hashtable htb,String key){
		//
		String	str	= (String) htb.get(key);
		if(str==null){
			return	"";		
		}
		return	str;
	}
	public static String strHashSP(Hashtable htb,String key){

		return	strHash(htb,key);
	}
	/**
	 * 返す値がnullだったらあらかじめ指定した文字列defを返す
	 * @param htb
	 * @param key
	 * @param def
	 * @return
	 */
	public static String strHash(Hashtable htb,String key,String def){
		//
		String	str	= (String) htb.get(key);
		if(str==null){
			return	def;
		}
		return str;
	}
	/**
	 * 返す値がnullだったらそのままnullを返す
	 * @param htb
	 * @param key
	 * @return
	 */
	public static String strHashIncludeNull(Hashtable htb,String key){
		//
		String	str	= (String) htb.get(key);
		if(isEmpty(str))	return	null;
		return	str;
	}

	public String strHashWithNullAlart(Hashtable htb,String key,String comment){
		//
		String	str	= (String) htb.get(key);
		if(comment!=null){
			if((LOG.fa)&&(str==null)){
				LOG.println("★ key =  " + key + "/ " + comment);
				LOG.println("★★★★★★★★★★★★★★★★★★★★★★★★★★");
				LOG.println("★★   　　　　　　　　　　　　　　　　　　　　   ★★");
				LOG.println("★★   ハッシュから取り出したデータは NULL です   ★★");
				LOG.println("★★   　　　　　　　　　　　　　　　　　　　　   ★★");
				LOG.println("★★★★★★★★★★★★★★★★★★★★★★★★★★★");
			}
		}
		return str;
	}


}