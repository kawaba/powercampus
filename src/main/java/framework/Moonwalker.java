package framework;
/*
	ライセンス情報を複合化する
	
*/
import java.io.*;
import java.io.UnsupportedEncodingException;
import tktools.Gear;
import tktools.Csv;
import tktools.DesEncrypter;;


//import jakarta.servlet.*;
//import jakarta.mail.NoSuchProviderException;
//import jakarta.mail.MessagingException;
//import jakarta.mail.internet.AddressException;
//import jakarta.mail.internet.InternetAddress;
//import jakarta.mail.internet.MimeMessage;
//
//import	epml.tools.*;
//
public class Moonwalker extends Object {
	//
	String		key;			// ライセンスキー
	
	String		serverName;
	String		mailserverName;
	String		userName;
	String		userAddress;
	
	String		uid;			// 
	String		adr_local;		// 
	String		adr_glov;		// 
	String		max;			//
	//
	String			_license;
	PrintWriter		out;
	//
	final 	String 		CR 			= System.getProperty("line.separator");
	static final String PASSPHRASE	= "tkx280113";    // 暗号化のためのパスフレーズ
	//
	public	Moonwalker(String _keyPath,PrintWriter sysLog){
		//
		out		= sysLog;
		File	fp	= new File(_keyPath);
		byte[] bytes = null;
		try{
			bytes	= getBytesFromFile(fp);
		}catch(IOException e){
			System.out.println("■" + Gear.getDate());
			System.out.println("   コンストラクタ Moonwalker() でライセンスファイルが読めない");
		}
		try{
			key	= new String(bytes,"Windows-31J");
		}catch(UnsupportedEncodingException e2){
			System.out.println("■" +  Gear.getDate());
			System.out.println("   コンストラクタ Moonwalker() でライセンスファイルのエンコードができない");
		}
		//
		String	temp 	= decryptoLK(key);
		Csv		keys	= new Csv(temp,"#");
		
		serverName		=	keys.get(0);
		mailserverName	=	keys.get(1);
		userName		=	keys.get(2);
		userAddress		=	keys.get(3);
		//
	    if(LOG.fa){
    	    out.println("■ start:" +  Gear.getDate() + CR);
			out.println("    serverName      = " + serverName + CR);
			out.println("    userName        = " + userName + CR);
			out.println("    userAddress     = " + userAddress + CR);
			out.println("    mailserverName  = " + mailserverName + CR);
	    }
	}
	//
	public	boolean checkStar(String _license, String CKserver, String CKuser, String CKusermail, String CKmailhost){

	    if(LOG.fa){
	    	if( (_license.equals("VIEW")) || (_license.equals("SYSTEM_VIEW")) ){
				LOG.println("■ access :" +  Gear.getDate());
				LOG.println("    serverName      = " + serverName + CR);
				LOG.println("    userName        = " + userName + CR);
				LOG.println("    userAddress     = " + userAddress + CR);
				LOG.println("    mailserverName  = " + mailserverName + CR);
				//
			}
	    }
		// 本家システムならチェックをしない
		if( (_license.equals("SYSTEM_VIEW"))||(_license.equals("SYSTEM")) ){
			//
			if( (_license.equals("VIEW")) || (_license.equals("SYSTEM_VIEW")) ){
			    LOG.println("Authentication Free.(ABS OK)");
			}
			return true;
		}
		//
		// check 
		if(!CKserver.equals(serverName)){
			if(_license.equals("VIEW")){ LOG.println("not authenticated.(101)");}
			return  false;
		}else if(!CKuser.equals(userName))	{
			if(_license.equals("VIEW")){ LOG.println("not authenticated.(102)"); }
			return	false; 
		}else if(!CKusermail.equals(userAddress))	{
			if(_license.equals("VIEW")){ LOG.println("not authenticated.(103)"); }
			return	false; 
		}else if(!CKmailhost.equals(mailserverName))	{
			if(_license.equals("VIEW")){ LOG.println("not authenticated.(104)"); }
			return	false; 
		}else if(_license.equals("VIEW")){ 
		    LOG.println("authenticated.(100)");
		}
		return true;
	}
    //
    // アクセスコントロールワードを復号する
    String decryptoLK(String KEY){
        //
        String decrypted = "";
        try {
            DesEncrypter encrypter = new DesEncrypter(PASSPHRASE);
            decrypted = encrypter.decrypt(KEY);
        } catch (Exception e) {
        }
        return decrypted;
    }
    //
	// Returns the contents of the file in a byte array.
    public static byte[] getBytesFromFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);
        long length = file.length();	// Get the size of the file
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
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
        // Close the input stream and return bytes
        is.close();
        return bytes;
    }
}