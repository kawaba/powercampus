package epml.tools;

//import java.text.*;
import java.util.*;

public class Regularizer extends Object{
	
	static final String str10	= "１２３４５６７８９０";
	static final String str11	= "1234567890";
	static final String str20	= "＋－＊／（）［］｛｝＜＞・－＝，．；：！”＃＄％＆’～＾｜￥";
	static final String str21	= "+-*/()[]{}<>･-=,.;:!\"#$%&'~^|\\";
	static final String str30	= "ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ";
	static final String str31	= "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	
	static final String str40	= "　１２３４５６７８９０ＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚ？＋－＊／（）［］｛｝＜＞・－＝，．；：！”＃＄％＆’～＾｜￥";
	static final String str41	= " 1234567890ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz?+-*/()[]{}<>･-=,.;:!\"#$%&'~^|\\";
	
	Hashtable	exchg;
	public Regularizer(){
		
		exchg	= new Hashtable(500);
		int len	= str40.length();
		for(int k=0; k<len; k++){
			exchg.put(String.valueOf(str40.charAt(k)),String.valueOf(str41.charAt(k)));
		}
	}

	//
	// 一般的な文字の正規化を行う
	public String	toGeneralSet(String	str){
		
		/*
		 * 文字ケースを全て大文字に変換は強すぎるので
		 * 省いた 2004.10.31  
		 * 
		String	str1	= str.replace('　',' ');			// 両端の空白を取り、中間の全角空白を半角空白に変換
		String	str2	= str1.toUpperCase();				// 全ての文字を大文字に変換
		String	str3	= toHan(str2);						// 全角英数記号を半角に変更する
		String	last	= doSpace(str3);					// 連続する半角空白を１個の空白にまとめる
		return	last.trim();
		*/
		
		String	str1	= str.replace('　',' ');			// 両端の空白を取り、中間の全角空白を半角空白に変換
		String	str3	= toHan(str1);						// 全角英数記号を半角に変更する
		String	last	= doSpace(str3);					// 連続する半角空白を１個の空白にまとめる
		return	last.trim();

	}

		//
	// 一般的な文字の正規化を行う
	public String	toRegular(String str){
		//
		String	str1	= str.replace('　',' ');			// 両端の空白を取り、中間の全角空白を半角空白に変換
		String	str3	= toHan(str1);						// 全角英数記号を半角に変更する
		String	last	= doSpace(str3);					// 連続する半角空白を１個の空白にまとめる
		return	last.trim();
	}

	//
	// 一般的な文字の正規化を行う(英字は小文字に)
	public String	toRegularLow(String str){
		//
		String	str1	= str.replace('　',' ');			// 両端の空白を取り、中間の全角空白を半角空白に変換
		String	str2	= str1.toLowerCase();				// 全ての文字を小文字に変換
		String	str3	= toHan(str2);						// 全角英数記号を半角に変更する
		String	last	= doSpace(str3);					// 連続する半角空白を１個の空白にまとめる
		return	last.trim();
	}

		//
	// 全角英数記号を半角に変更する
	public	String	toHan(String str){
		int				len	= str.length();
		StringBuffer	bf	= new StringBuffer(1000);
		for(int k=0; k<len; k++){
			String	x	=	String.valueOf(str.charAt(k));
			String	s	=	(String)exchg.get(x);
			if(s==null){
				bf.append( x );
			}else{
				bf.append( s );// 全角を半角に変換
			}
		}
		return	bf.toString();
	}
	// 文字列中の連続する半角空白を１個の空白にまとめる
	// 先頭と最後には空白文字列はないという前提
	public	String	doSpace(String str){
		
		StringBuffer	bf		= new StringBuffer(1000);
		boolean			spflag	= false;
		int				len		= str.length();
		for(int	k=0; k<len; k++){
			
			char	c	= str.charAt(k);
			// 空白文字でフラグが立ってなければバッファに加える
			// が、そうでなければ捨てる．フラグを立てておく．
			if(c==' '){
				if(!spflag){
					bf.append(" ");
					spflag	= true;
				}
			// 一般の文字ならバッファに加え、フラグをおろしておく
			}else{
				bf.append(c);
				spflag	= false;
			}
		}
		return	bf.toString();
	}
	// tkn の文字数が１以上か調べる．ただし、半角、全角のスペースと改行コードは
	// 文字数に含めない
	// zero でなければ true を返す
	boolean count(String tkn){
		int len = tkn.length();
		for(int k=0; k<len; k++){
			if( isCount(tkn.charAt(k)) ) return true;
		}
		return false;
	}
	boolean isCount(char c){
		if( (c==' ')||(c=='　') ){
			return false;
		}
		if(Character.isISOControl(c)) return false;	// 制御文字
		//
		return true;
	}
	//
	// 文字列 source の中で、from と to で挟まれた部分を取り出す
	String	getString(String source,char from ,char to){
		//
		int	pos1	= source.indexOf(from);
		if(pos1 < 0)		return	"";			// 開始文字がない
		int pos2	= source.indexOf(to);
		if(pos2 < 0)		return	"";			// 終了文字がない
		if((pos2-pos1)<=1)	return	"";			// [] で中身がないか][ で順序が逆
		//
		return	source.substring(pos1+1,pos2);
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
	
	
	// ウェブ表示のために
	// 書き換え文字列中の改行文字(\n)を<br>に置き換える
	public static String replaceToBR(String ps){
		//
		return replaceing(ps,"\n","<br>");
	}
	// 
	// 文字列 str 中の全ての pattern を replace に置き換える
	public	static String replaceing(String str, String pattern, String replace) {
		int s = 0;
		int e = 0;
		StringBuffer result = new StringBuffer();
		//
		while ((e = str.indexOf(pattern, s)) >= 0) {
			result.append(str.substring(s, e));
			result.append(replace);
			s = e+pattern.length();
		}
		result.append(str.substring(s));
		return result.toString();
	}	
	//
	// 文字列が数字がどうかチェックする
    public	boolean isDigit(String s){
        if((s == null)||(s.length()==0))    return false;
		//
        //StringBuffer bf = new StringBuffer(100);
		int len = s.length();
        for(int i=0; i<len; i++){
            char ch = matchDigit(s.charAt(i));
            //bf.append(ch);
			if(ch == '*')       return false;
        }
        return true;
    }
	// 半角の数字文字列にして返す
	// 数値以外のものが出現するとそこまでを数字文字として返し、残りを無視する
    public	String toDigit(String s){
        if((s == null)||(s.length()==0))    return "";
		//
        StringBuffer bf = new StringBuffer(100);
		int len = s.length();
        for(int i=0; i<len; i++){
            char ch = matchDigit(s.charAt(i));
			if(ch == '*'){
				if(DBG.fa) DBG.println("-- value :" + bf.toString() + "(" + (bf.toString()).length() + ")");
				return bf.toString();	// 数字以外のとき
			}else{
				bf.append(ch);
			}
        }
        if(DBG.fa) DBG.println("-- value :" + bf.toString() + "(" + (bf.toString()).length() + ")");
		return bf.toString();
    }
	// 右端に付加された単位文字列を返す
	// 単位英字、記号以外のものが出現するとそこまでを返し、残りを無視する
    public	String unitString(String str){
        if((str == null)||(str.length()==0))    return "";
		String	s = removeSpace(str);			// 空白文字を取り去っておく
		if(s.length()==0) return	"";			// 長さゼロなら""を返す
		//
        StringBuffer bf = new StringBuffer(100);
		int len = s.length();
        for(int i=len-1; i>=0; i--){			// 右端から見ていく
            char c	= s.charAt(i);
			char ch = matchDigit(c);
			if(ch != '*'){						// 数字の時
				if(bf.length()==0){
					if(DBG.fa) DBG.println("-- unit :" + bf.toString() + "(" + (bf.toString()).length() + ")");
					return "";	// 長さゼロなら""を返す
				}
				bf.reverse();
				if(DBG.fa) DBG.println("-- unit :" + bf.toString() + "(" + (bf.toString()).length() + ")");
				return (bf.toString()).toLowerCase();
			}else{
				bf.append(c);
			}
        }
        bf.reverse();
		if(DBG.fa) DBG.println("-- unit :" + bf.toString() + "(" + (bf.toString()).length() + ")");
		return (bf.toString()).toLowerCase();
    }
	//
	public	char	matchDigit(char c){
		if((c=='0')||(c=='０'))	return	'0';
		if((c=='1')||(c=='１'))	return	'1';
		if((c=='2')||(c=='２'))	return	'2';
		if((c=='3')||(c=='３'))	return	'3';
		if((c=='4')||(c=='４'))	return	'4';
		if((c=='5')||(c=='５'))	return	'5';
		if((c=='6')||(c=='６'))	return	'6';
		if((c=='7')||(c=='７'))	return	'7';
		if((c=='8')||(c=='８'))	return	'8';
		if((c=='9')||(c=='９'))	return	'9';
		return '*';
	}
	//
	//  文字列から半角、全角の空白文字を全て取り去る
	//  null でも "" を返す
	public	String	removeSpace(String s){
        if((s == null)||(s.length()==0))    return "";
		//
        StringBuffer bf = new StringBuffer(500);
		int len = s.length();
        for(int i=0; i<len; i++){
            char ch = s.charAt(i);
			if(!isSpace(ch))	bf.append(ch);	// 空白でなければバッファに加える
        }
        return bf.toString();
	}
	//
	// 空白文字かどうかのチェック
	boolean	isSpace(char c){
		if(c==' ')		return	true;
		if(c=='　')		return	true;
		return false;
	}
	
	
}