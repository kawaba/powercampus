/*
 * 作成日: 2005/02/04
 *
 * TODO
 */
package tktools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

import framework.*;

/**
 *  文字列に関するユーティリティ
 */
public class StringGear {
    private static final char	TAB			=   '\t';
	private static final char	SPC_CHAR	=	' ';
	private static final char	SPC_KCHAR	=	'　';
	
	private static final String LETTER 			= "abcdefghijklmnopqrstuvwxyz_ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private static final String DIGIT  			= "1234567890";

	private static final String LETTERorDigit  	= ".abcdefghijklmnopqrstuvwxyz_ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

	private static final String sLETTERorDigit 	= "abcdefghijklmnopqrstuvwxyz_1234567890";

    private static final String MAIL_LETTERS   	= ".@-abcdefghijklmnopqrstuvwxyz_ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    
    private static final String DOMAIN_LETTERS 	= ".-abcdefghijklmnopqrstuvwxyz_ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    
    private static final String PATH_LETTERS 		= "\\.-:$abcdefghijklmnopqrstuvwxyz_ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    
    private static final String IP_LETTERS 		= ".1234567890";
    
    
	public StringGear()	{ }
    /**
     * パス名として正しい文字種か
     * @param s
     * @return
     */
    public static boolean isPathString(String s){
        if((s == null)||(s.length()==0))    return false;
        int n = s.length();
        //
        for(int i=0; i<n; i++){
            char ch = s.charAt(i);
            if(!xisPathLetters(ch))   return false;
        }
        return true;
    }
    public static boolean xisPathLetters(char ch){
        int n = PATH_LETTERS.length();
        for(int i=0; i<n; i++){
            if(ch == PATH_LETTERS.charAt(i))   return  true;
        }
        return false;
    } 
    /**
     * IPアドレスとして正しい文字種か
     * @param s
     * @return
     */
    public static boolean isIPString(String s){
        if((s == null)||(s.length()==0))    return false;
        int n = s.length();
        //
        for(int i=0; i<n; i++){
            char ch = s.charAt(i);
            if(!xisIPLetters(ch))   return false;
        }
        return true;
    }
    public static boolean xisIPLetters(char ch){
        int n = IP_LETTERS.length();
        for(int i=0; i<n; i++){
            if(ch == IP_LETTERS.charAt(i))   return  true;
        }
        return false;
    }  
    // メールアドレスとして正しい形式か
	//
	public static boolean isMailaddress(String s){
		//
		// ヌル文字でない
		if(s == null) 			return	false;
		// ３文字以上　x@y の文字列である
		int len = s.length();
		if( len < 3)			return	false;
		//
		//メールに使える文字だけからなる
		if(!isMailLetter(s)){
		    return	false;
		}
		// @ が含まれいている
		int pos = s.indexOf("@");
		if(pos == -1) 			return false;
		// @ の前後に文字がある
		if(s.endsWith("@"))		return false;
		if(s.startsWith("@"))	return false;
		//
		
		return true;
	}

    /**
     * メールアドレスとして正しい文字種か
     * @param s
     * @return
     */
    public static boolean isMailLetter(String s){
        if((s == null)||(s.length()==0))    return false;
        int n = s.length();
        //
        for(int i=0; i<n; i++){
            char ch = s.charAt(i);
            if(!xisMailLetter(ch))   return false;
        }
        return true;
    }
    public static boolean xisMailLetter(char ch){
        int n = MAIL_LETTERS.length();
        for(int i=0; i<n; i++){
            if(ch == MAIL_LETTERS.charAt(i))   return  true;
        }
        return false;
    }    
    /**
     * ドメイン名として正しい文字種か
     * @param s
     * @return
     */
    public static boolean isDomain(String s){
        if((s == null)||(s.length()==0))    return false;
        int n = s.length();
        //
        for(int i=0; i<n; i++){
            char ch = s.charAt(i);
            if(!xisDomainLetter(ch))   return false;
        }
        return true;
    }
    public static boolean xisDomainLetter(char ch){
        int n = DOMAIN_LETTERS.length();
        for(int i=0; i<n; i++){
            if(ch == DOMAIN_LETTERS.charAt(i))   return  true;
        }
        return false;
    }    
    /**
     * 最大１６桁として，全体でsize桁の文字列にする
     * 不足する場合は先頭に空白を補う
     * １６桁以上の場合は null を返す
     * 
     * @param dt		文字列
     * @param size		長さ
     * @return
     */
	public static String setLength(String dt, int size){
	    if(size>16)				return	null;
	    if(size<=dt.length())	return	dt;
	    
	    String	spaces	=	"                ";	// 16文字
	    String	spc		=	spaces.substring(0,size);
	    
	    if((dt == null)||(dt.length() == 0)) return spc;
		int     	pos		=	dt.length();
		String  	pattern = 	spc + dt;
		return  	pattern.substring(pos);
	}
	/**
	 * 文字列の長さを返す
	 * 前後の空白は取ってから計算する
	 * 改行は文字に数えない．
	 * 
	 * @param str
	 * @return
	 */
	public	static	int	howManyChar(String str){
	  
		   if(LOG.fa){
		       LOG.println("");
		       LOG.println("■StringGear #hawManyChar()");
		       LOG.println( str );
		       LOG.println("-------------------------");
		   }
		   
	   String			bun		=	str.trim();
	   int				counts	=	0;
	   
	   BufferedReader	br		=	new	BufferedReader(new StringReader(bun));
	   String			line;
	   try{
		   while((line=br.readLine())!=null){
		       counts	+=	line.length();
		   }
	   }catch(IOException e){
	       e.printStackTrace();
	       
	   }
	   if(LOG.fa){
	       LOG.println("");
	       LOG.println("■StringGear #hawManyChar()");
	       LOG.println("   ★ 文字数 = " + counts);
	       LOG.println("");
	   }
	   return	counts;
	}
	/**
	 * 文字列の中のタブを半角スペースに置き換える
	 * @param text
	 * @param width
	 * @return	タブを半角スペースに変えたテキスト
	 */
	public	static	String	tabToSpace(String text, int width){
	    StringBuffer	buf	=	new	StringBuffer(text.length() + 300);
	    BufferedReader	in	=	new BufferedReader(new	StringReader(text));
	    try{
	        boolean	crFlag	=	false;
	        String		line;
	        while((line=in.readLine())!=null){
	            if(crFlag){
	                buf.append(Gear.CR);
	            }
	            buf.append( tabToSPC(line, width) );
	            crFlag	=	true;
	        }
	    }catch(IOException e){
	        
	    }
	    return	buf.toString();
	}
	/** １行分のデータについてタブをスペースに変換 */
	public	static  String  tabToSPC(String s, int width){
        
		StringBuffer 	buf =	new StringBuffer();
		int 			n	=   s.length();
		int 			pos	=	0;	// 現在のカラム位置
		for(int i=0; i<n; i++){
			char    c   =   s.charAt(i);
			if(c==TAB){
				// タブを width - spcnt 個のスペースに変換する
				int spcnt	= pos % width;
				for(int j=0; j<width-spcnt; j++){
					buf.append(SPC_CHAR);
					pos++;
				}
			}else{
				buf.append(c);
				pos++;
			}
		}
		return	buf.toString();
	}	
	/**
	 * Stringをベクターに入れて返す
	 * @param 	str
	 * @return	
	 */
	public static	Vector	StringToVector(String str){
		
		Vector	v	=	new Vector(100);
		StringReader	sr		=	new	StringReader(str);
		BufferedReader	br		=	new	BufferedReader(sr);
        String			line	=	"";
		try{
            while((line=br.readLine())!=null){
                if(line.length() > 0) { v.add(line); }
            }
        }catch (IOException e){
            
        }
        return	v;
		
	}	
	/**
	 * 文字列をbyte型の１６進文字列に変換する
	 * @param str
	 * @return
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
     * intを２桁の数字文字列にして返す
     * @param s
     * @return
     */
	public static String get00type(int s){
        String  dt      = String.valueOf(s);
        if((dt == null)||(dt.length() == 0)) return "00";
        int     pos     = dt.length();
        String  pattern = "00" + dt;
        return  pattern.substring(pos);
    }
    /**
     * intを３桁の数字文字列にして返す
     * @param s
     * @return
     */
    public static String get000type(int s){
        String  dt      = String.valueOf(s);
        if((dt == null)||(dt.length() == 0)) return "000";
        int     pos     = dt.length();
        String  pattern = "000" + dt;
        return  pattern.substring(pos);
    }
    /**
     * intを４桁の数字文字列にして返す
     * @param s
     * @return
     */
	public static String get0000type(int s){
		String  dt      = String.valueOf(s);
		if((dt == null)||(dt.length() == 0)) return "0000";
		int     pos     = dt.length();
		String  pattern = "0000" + dt;
		return  pattern.substring(pos);
	}
    /**
     * intを５桁の数字文字列にして返す
     * @param s
     * @return
     */
	public static String get00000type(int s){
		String  dt      = String.valueOf(s);
		if((dt == null)||(dt.length() == 0)) return "00000";
		int     pos     = dt.length();
		String  pattern = "00000" + dt;
		return  pattern.substring(pos);
	}	
	/**
	 * 先頭を空白で埋めて２桁の整数文字にする(末尾に空白を１個付加)
	 * @param s
	 * @return
	 */
	public static String getSSform(int s){
	   String  dt      = String.valueOf(s);
	   if((dt == null)||(dt.length() == 0)) return "  ";
	   int     pos     = dt.length();
	   String  pattern = "  " + dt + " ";
	   return  pattern.substring(pos);
	}
	/**
	 * 先頭を空白で埋めて２桁の整数文字にする
	 * @param s
	 * @return
	 */
	public static String getS2form(int s){
	   String  dt      = String.valueOf(s);
	   if((dt == null)||(dt.length() == 0)) return "  ";
	   int     pos     = dt.length();
	   String  pattern = "  " + dt;
	   return  pattern.substring(pos);
	}
	/** < と > と " を特殊文字に直す */
	public	static String	regularize(String	str){
		String	temp1	=  substitute(str,  "<","&lt;");
		String	temp2	=  substitute(temp1,">","&gt;");
		String	temp3	=  substitute(temp2,"\"","&quot;");
		return	temp3;
	}
	/** < と > と " を特殊文字に戻す */
	public	static String	antiRegularize(String	str){
		String	temp1	=  substitute(str,"&lt;",  "<");
		String	temp2	=  substitute(temp1,"&gt;",">");
		String	temp3	=  substitute(temp2,"&quot;","\"");
		return	temp3;
	}	
	/**
	 * 文字列 str 中の全ての pattern を replace に置き換える
	 * @param str
	 * @param pattern
	 * @param replace
	 * @return
	 */
	public	static String substitute(String str, String pattern, String replace) {
		return	replace(str, pattern, replace);
	}	
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
	/**
	 * 文字列 dataStr 内の変数をハッシュ表 ht の変数で置き換えた結果の文字列を返す<p>
	 * データを作成するだけなら printData = false を指定する
	 * 
	 * @param dataStr	元の文字列
	 * @param ht		変換するワードの入ったハッシュ
	 * @return			変換後の文字列
	 */
	public	static String substitute(String dataStr,Hashtable ht){
		return	substitute(dataStr,ht,true);
	}
	public static	String substitute(String dataStr,Hashtable ht,boolean printData){
		return	replace(dataStr, ht, printData);
	}
	public static	String replace(String dataStr,Hashtable ht){
		return	replace(dataStr,ht,true);
	}
	public static	String replace(String dataStr,Hashtable ht,boolean printData){
		StringBuffer bf = new StringBuffer(1000);
		if(dataStr==null) return "";
		//
		StringTokenizer st = new StringTokenizer(dataStr,"%");
		boolean printFlag = true;
		while(st.hasMoreTokens()){
			String tk = st.nextToken();
			if(tk.charAt(0)=='_'){
				String ps = (String)ht.get(tk); //ハッシュ表を参照
				if(ps==null){
					bf.append("");	//ハッシュになければ""とする
				}else{
					bf.append(ps);
				}
			}else{
				bf.append(tk);
			}
		}
		String	ret	= bf.toString();			// そのまま
		if(printData){
	       ret	=	Cp932.toCp932(ret);     	// Cp932 にもどす
	  	}
		return ret;
	}
    /**
     * 文字列のnull，空白チェック
     * @param s
     * @return
     */
    public static boolean isSpaceOrNull(String s){
        if((s != null) && (s.length() > 0) && !isSpace(s)) return false;
        return true;
    }
    /**
     * 空白文字かどうか検査する
     * @param c
     * @return
     */
    public	static	boolean	isSpaceChar(char c){
    	if(c==SPC_CHAR)		return	true;
    	if(c==SPC_KCHAR)	return	true;
    	return		false;
    }
    /**
     * 漢字空白を含んで、全て空白文字かどうかチェックする
     * @param s
     * @return
     */
    public static boolean isSpace(String s){
        if((s.length()==0)||(s==null)) return false;
        String  space = " ";   // 空白文字
        String s1;
        int n = s.length();
        for(int i=0; i<n; i++){
            s1 = s.substring(i,i+1);
            if((!s1.equals(space))&&(!s1.equals("　"))){ //  ascii と漢字の空白文字を比較
                return false;
            }
        }
        return true;
    }
    /**
     * 文字列が数字がどうかチェックする
     * @param s
     * @return
     */
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
    /**
     * 文字列が空かどうかテストする
     * @param str
     * @return
     */
	public static boolean isEmpty(String str){
		if(str==null) 			return  true;
		if(str.length()==0)	return  true;
		return false;
	}
	/**
	 * 前後の空白を除いて、空かどうかテストする
	 * @param str
	 * @return
	 */
	public static boolean isEmptyData(String str){
		if(str==null) 					return  true;
		if((str.trim()).length()==0)	return  true;
		return false;
	}
	/**
	 * 数字文字としては空かどうかテストする
	 * @param s
	 * @return
	 */
	public static boolean isEmptyDigit(String s){
		if(isEmpty(s)){
			return	true;
		}else	if(!isDigitx(s.trim())){
			return	true;
		}
		return	false;
	}
    /**
     * 文字列がｎ桁の英字かどうかチェックする
     * @param s
     * @param n
     * @return
     */
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
    /**
     * 文字列がｎ桁の数字がどうかチェックする
     * @param s
     * @param n
     * @return
     */
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
    /**
     * 文字列が英数字からなるかどうかチェックする
     * @param s
     * @return
     */
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
    /**
     * 文字列が小文字英字と数字からなるかどうかチェックする
     * @param s
     * @return
     */
    public static boolean isSmallHankaku(String s){
        if((s == null)||(s.length()==0))    return false;
        int n = s.length();
        //
        for(int i=0; i<n; i++){
            char ch = s.charAt(i);
            if(!xisSmallLETTERorDigit(ch))   return false;
        }
        return true;
    }
    /**
     * 文字列がｎ桁以上ｍ桁以下の英数字からなるかどうかチェックする
     * @param s
     * @param min
     * @param max
     * @return
     */
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
    /**
     * 英字かどうか
     * @param ch
     * @return
     */
    public static boolean xisLetter(char ch){
        int n = LETTER.length();
        for(int i=0; i<n; i++){
            if(ch == LETTER.charAt(i))  return  true;
        }
        return false;
    }
    /**
     * 数字かどうか
     * @param ch
     * @return
     */
    public static boolean xisDigit(char ch){
        int n = DIGIT.length();
        for(int i=0; i<n; i++){
            if(ch == DIGIT.charAt(i))   return  true;
        }
        return false;
    }
    /**
     * 数字または英字か
     * @param ch
     * @return
     */
    public static boolean xisLETTERorDigit(char ch){
        int n = LETTERorDigit.length();
        for(int i=0; i<n; i++){
            if(ch == LETTERorDigit.charAt(i))   return  true;
        }
        return false;
    }
    /**
     * 数字または英字小文字か
     * @param ch
     * @return
     */
    public static boolean xisSmallLETTERorDigit(char ch){
        int n = sLETTERorDigit.length();
        for(int i=0; i<n; i++){
            if(ch == sLETTERorDigit.charAt(i))   return  true;
        }
        return false;
    }
    /**
     * 文字列をintに直す
     * 例外発生時には指定の値を返す（していなければ0）
     * @param s
     * @param init
     * @return
     */
    public	static	int	toInt(String s){
    	return	toInt(s,0);
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
	/**
	 * ＄や＠（識別子）で始まる名前を文字列から取り出す。
	 * ＄や＠を含まない名前を返す
	 * 
	 * @param 		line	対象文字列
	 * @param		head	識別子の文字列（複数指定可能）
	 * @return		フラグ名。ない場合はnullを返す
	 */
	public static String	getName(String line, String head){
		StringBuffer	bf		=	new	StringBuffer();
		StringReader	sk		=	new	StringReader(line);
		byte			[]cp	=	head.getBytes();
		int				c;
		try{
			/*
			 * @ か $ の頭だし
			 */
			boolean	flag	=	false;
			while((c=sk.read())!=-1){
				for(int i=0; i<cp.length; i++){
					if(c==cp[i]){
						flag	=	true;
						break;
					}
				}
				if(flag){
					break;
				}
			}
			if(c==-1)		return	null;
			/*
			 * 空白が出てくるか文字列の最後になるまで取り出す
			 */
			while((c=sk.read())!=-1){
				if(StringGear.isSpaceChar((char)c)){// 漢字空白も含める
					break;
				}
				bf.append((char)c);
			}
			
		}catch(IOException e){
			e.printStackTrace();
		}
		
		if(bf.length()==0)	return	null;
		return		bf.toString();
	}
	
	/**
	 * デバッグ用
	 * 
	 * @param arg
	 */
	static public void	main(String []arg){
		
	    String str = "～";
	    System.out.println(str.charAt(0));
	    
	    String s =	StringGear.toHexString(str);
	    System.out.println(s);
	    
	    
		
	
	}		
	
}
