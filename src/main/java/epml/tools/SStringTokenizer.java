/*
 * 作成日: 2005/07/24
 *
 */
package epml.tools;

/**
 *	Super String Tokenizer クラス
 *
 *　セパレータとして任意の文字列を指定できるStringTokenizer
 */
import	java.util.*;

public class SStringTokenizer {

    String		str;		// 対象文字列
    int			length;		// 対象文字列の長さ

    String		pattern;	// セパレータ文字列
    Vector		v;			// 要素を格納する
    int			size;		// 要素数
    int			pos;		// 現在のインデックス
    /**
     * コンストラクタ
     * @param str		文字列
     * @param pattern	セパレータのパターン文字列
     */
    public	SStringTokenizer(String str, String pattern){
        this.str		=	str;
        length			=	str.length();	
        this.pattern	=	pattern;
        v				=	new	Vector(20);
        init();
    }
    /**
     * 文字列をトークンに分けてベクターに格納する
     */
	private	void init(){
		int s = 0;
		int e = 0;
		//System.out.println("(s,e) = (" + s + "," + e +")");
		while ((e = str.indexOf(pattern, s)) >= 0) {
			if(e == s){
			    /* セパレータから始まる文字列．
			     * 単にセパレータを取るだけ
			     */
			    s = e + pattern.length();
			    
			}else{
			    v.add(str.substring(s, e));
			    s = e + pattern.length();
			}
			//System.out.println("(s,e) = (" + s + "," + e +")");
		}
		//System.out.println("(s,e) = (" + s + "," + e +")");
		if(s<length){
		    v.add(str.substring(s));
		}
		size	=	v.size();
		initPos();
	}
	/**
	 * 要素位置を初期化する
	 *
	 */
	private	void	initPos(){
	    pos		=	0;
	}    
    /**
     * まだ要素が残っているかどうか
     * @return
     */
    public	boolean	hasMoreTokens(){
        
        return	(pos<size);
    }
    /**
     * トークナイザの nextToken メソッドを呼び出せる回数を計算します。
     * 現在の位置は進みません。 
     * @return		残り回数
     */
    public int countTokens(){
        int		n	=	size - pos;
        return	n;
    }
    /**
     * 次のトークンを返す
     * @return
     */
    public	String	nextToken(){
        String	ret	=	null;
        if(pos<size){
            ret	=	(String)v.elementAt(pos);
            pos++;
        }
        return	ret;
    }
    /*
     *	TEST 
     */
    public static void main(String[] args) {
        
        int 	n	=	1;

        String	str	=	"&sep&sepこ ん&sep&sepに ち  は&sep&sep";
        SStringTokenizer	sst	=	new	SStringTokenizer(str,"&sep");
        System.out.println("要素数=" + sst.countTokens());
        while(sst.hasMoreTokens()){
            System.out.print("("+n+") ");
            n++;
            
            String	s	=	sst.nextToken();
            System.out.println(s);
        }
        
        
    }
    
    
}
