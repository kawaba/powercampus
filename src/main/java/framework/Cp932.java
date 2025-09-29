/*
 * 風間一洋さんのJavaHouse-Brewers投稿記事[14452]のCp932クラス
 * The Cp932 class contains a utility method for converting Microsoft's
 * Cp 932 into JIS.
 *
 * @author Kazuhiro Kazama
 * @version 1.0 01/06/97
 
    使い方としては、まず、プラットフォームネイティブなメソッドからStringを
    受け取る部分で次のようにJISのマッピングに正規化します。

        String s = Cp932.toJIS(textfield.getText());

    また、プラットフォームネイティブなメソッドにStringを渡す部分でも、次の
    ように正規化されたStringを再びCp 932のマッピングに戻してあげてください。

        button2.setLabel(Cp932.toCp932(s));
        
        
   ★ 2005.9. NOTE
   　Windows プラットフォームでは
        サーブレットでのエンコードを "text/plain; charset=MS932" と指定すればCp932は不要
        ただし、電子メールでは文字化けするので出力時のみに forJisMail() でJISに変換する
   　
   　Linux プラットフォームでは
        サーブレットでのエンコードを "text/plain; charset=Shift-JIS" と指定してCp932を利用する
        ただし、電子メールでは出力時には適用しない
   
   
   
*/
//
package framework;
import java.util.*;
public class Cp932 {
    
    public static boolean isCp932 = false;
    //
    //
    static {
        String p = System.getProperty("iscp932");
        String os = System.getProperty("os.name");
        
        if (Locale.getDefault().getLanguage().equals("ja")) {
            if (p != null && Boolean.getBoolean(p)){
                isCp932 = true;
            }else if (os != null && os.contains("Windows")){
                isCp932 = true;
            }
        }
    }
    /*
     * You can't use this constructor.
     */
    private Cp932() {
    }
    
    /*
     * This method converts Cp932 to JIS.
     */
    public static String toJIS(String s) {
	    if(isCp932)	return	s;
        
        //if (!isCp932)   return s;
        /*
        if(LOG.sp){
            if((s!=null) && (s.length()>1)){
	            if(s.substring(0,1).equals("～")){
	                int c  = s.charAt(0);
	                LOG.println("toJIS: String =" + s);
	                LOG.println("toJIS: int    =" + c);
	                LOG.println("cf. 0xff3c => " + 0xff3c );
	                LOG.println("cf. 0xff5e => " + 0xff5e );
	                LOG.println("cf. 0x2225 => " + 0x2225 );
	                LOG.println("cf. 0xff0d => " + 0xff0d );
	                LOG.println("cf. 0xffe0 => " + 0xffe0 );
	                LOG.println("cf. 0xffe1 => " + 0xffe1 );
	                LOG.println("cf. 0xffe2 => " + 0xffe2 );
	            }
            }
        }
        */
	    StringBuffer sb = new StringBuffer();
	    char c;
	    for (int i = 0; i < s.length(); i++) {
	        c  = s.charAt(i);
	        switch (c) {
	        //case 0xff3c:    // FULLWIDTH REVERSE SOLIDUS ->
	        //c = 0x005c; // REVERSE SOLIDUS
	        //break;
	        case 0xff5e:    // FULLWIDTH TILDE ->
	            c = 0x301c; // WAVE DASH
	            //LOG.println("0xff5e → 0x301c");
	            break;
	        case 0x2225:    // PARALLEL TO ->
	        c = 0x2016; // DOUBLE VERTICAL LINE
	        break;
	        case 0xff0d:    // FULLWIDTH HYPHEN-MINUS ->
	        c = 0x2212; // MINUS SIGN
	        break;
	        case 0xffe0:    // FULLWIDTH CENT SIGN ->
	        c = 0x00a2; // CENT SIGN
	        break;
	        case 0xffe1:    // FULLWIDTH POUND SIGN ->
	        c = 0x00a3; // POUND SIGN
	        break;
	        case 0xffe2:    // FULLWIDTH NOT SIGN ->
	        c = 0x00ac; // NOT SIGN
	        break;
	        }
	        sb.append(c);
	    }
	    
	    //return new String(sb);
	    return	sb.toString();
    }

    /*
     * This method convert JIS to Cp932.
     */
    public static String toCp932(String s) {
        
        if(isCp932)	return	s;
        
        //if (!isCp932)  return s;
	    /*
        if(LOG.sp){
            if((s!=null) && (s.length()>1)){
	            if(s.charAt(0)==0x301c ){
	                LOG.println("toCp932: String =" + s);
	                LOG.println("toCp932: int    =" + 0x301c);

	                LOG.println("cf. 0x005c => " + 0x005c );
	                LOG.println("cf. 0x301c => " + 0x301c );
	                LOG.println("cf. 0x2016 => " + 0x2016 );
	                LOG.println("cf. 0x2212 => " + 0x2212 );
	                LOG.println("cf. 0x00a2 => " + 0x00a2 );
	                LOG.println("cf. 0x00a3 => " + 0x00a3 );
	                LOG.println("cf. 0x00ac => " + 0x00ac );
	            }
            }
        }        
        */
	    StringBuffer sb = new StringBuffer();
	    char c;
	    for (int i = 0; i < s.length(); i++) {
	        c  = s.charAt(i);
	        switch (c) {
	        //case 0x005c:    // REVERSE SOLIDUS ->
	        //c = 0xff3c; // FULLWIDTH REVERSE SOLIDUS
	        //break;
	        case 0x301c:    // WAVE DASH ->
	            c = 0xff5e; // FULLWIDTH TILDE
	            //LOG.println("0x301c → 0xff5e");
	            break;
	        case 0x2016:    // DOUBLE VERTICAL LINE ->
	        c = 0x2225; // PARALLEL TO
	        break;
	        case 0x2212:    // MINUS SIGN ->
	        c = 0xff0d; // FULLWIDTH HYPHEN-MINUS
	        break;
	        case 0x00a2:    // CENT SIGN ->
	        c = 0xffe0; // FULLWIDTH CENT SIGN
	        break;
	        case 0x00a3:    // POUND SIGN ->
	        c = 0xffe1; // FULLWIDTH POUND SIGN
	        break;
	        case 0x00ac:    // NOT SIGN ->
	        c = 0xffe2; // FULLWIDTH NOT SIGN
	        break;
	        }
	        sb.append(c);
	    }
	    /*
        if(LOG.sp){
            if((s!=null) && (s.length()>1)){
	            if(s.charAt(0)==0x301c ){
	                LOG.println("toCp932: String =" + sb.toString());
	            }
            }
        }
        */ 	    
	    return sb.toString();
    }
    
    /*
     * This method converts Cp932 to JIS.
     */
    public static String forJisMail(String s) {
        
        //if (!isCp932)   return s;
        /*
        if(LOG.sp){
            if((s!=null) && (s.length()>1)){
	            if(s.substring(0,1).equals("～")){
	                int c  = s.charAt(0);
	                LOG.println("toJIS: String =" + s);
	                LOG.println("toJIS: int    =" + c);
	                LOG.println("cf. 0xff3c => " + 0xff3c );
	                LOG.println("cf. 0xff5e => " + 0xff5e );
	                LOG.println("cf. 0x2225 => " + 0x2225 );
	                LOG.println("cf. 0xff0d => " + 0xff0d );
	                LOG.println("cf. 0xffe0 => " + 0xffe0 );
	                LOG.println("cf. 0xffe1 => " + 0xffe1 );
	                LOG.println("cf. 0xffe2 => " + 0xffe2 );
	            }
            }
        }
        */
	    StringBuffer sb = new StringBuffer();
	    char c;
	    for (int i = 0; i < s.length(); i++) {
	        c  = s.charAt(i);
	        switch (c) {
	        //case 0xff3c:    // FULLWIDTH REVERSE SOLIDUS ->
	        //c = 0x005c; // REVERSE SOLIDUS
	        //break;
	        case 0xff5e:    // FULLWIDTH TILDE ->
	            c = 0x301c; // WAVE DASH
	            //LOG.println("0xff5e → 0x301c");
	            break;
	        case 0x2225:    // PARALLEL TO ->
	        c = 0x2016; // DOUBLE VERTICAL LINE
	        break;
	        case 0xff0d:    // FULLWIDTH HYPHEN-MINUS ->
	        c = 0x2212; // MINUS SIGN
	        break;
	        case 0xffe0:    // FULLWIDTH CENT SIGN ->
	        c = 0x00a2; // CENT SIGN
	        break;
	        case 0xffe1:    // FULLWIDTH POUND SIGN ->
	        c = 0x00a3; // POUND SIGN
	        break;
	        case 0xffe2:    // FULLWIDTH NOT SIGN ->
	        c = 0x00ac; // NOT SIGN
	        break;
	        }
	        sb.append(c);
	    }
	    
	    //return new String(sb);
	    return	sb.toString();
    }

}
