/*
 * 作成日: 2005/09/18
 *
 */
package epml.tools;

import	java.io.*;
import	java.util.*;

/**
 *
 */
public class FormatUtil {
    
    static	String	p1			=	"## $p1 ";
    static	String	p2			=	"## $p2 ";
    static	String	p3			=	"## $p3 ";
    static	String	p4			=	"## $p4 ";
    static	String	p5			=	"## $p5 ";
    static	String	std			=	"##     ";	//デフォルトの書式スタイル（標準書式）
    
    
    static	int		minlength	=	p1.length();
    
    public	FormatUtil(){

    }
    
    
    
    /**
     * pmlをフォーマットエイリアスと本文に分割し、エイリアスはハッシュテーブル（format）に
     * 本文はストリングバッファ（body）に格納する
     * 
     * @param pml
     * @param body
     * @return
     */
    public	static	Hashtable	getAlias(String pml, StringBuffer body){
        
        
        Hashtable		format	=	new	Hashtable();
        BufferedReader	in		=	new	BufferedReader(new	StringReader(pml));
        String			line	=	null;
        try {
            while((line=in.readLine())!=null){
                if(line.length()>minlength){
                    String	head	=	line.substring(0,minlength);
                    if(head.equals(p1)){
                        Csv	tkn		=	new	Csv(line,"()");
                        format.put("p1", tkn.get(1));

                    }else if(head.equals(p2)){
                        Csv	tkn		=	new	Csv(line,"()");
                        format.put("p2", tkn.get(1));
                        
                    }else if(head.equals(p3)){
                        Csv	tkn		=	new	Csv(line,"()");
                        format.put("p3", tkn.get(1));
                        
                    }else if(head.equals(p4)){
                        Csv	tkn		=	new	Csv(line,"()");
                        format.put("p4", tkn.get(1));
                        
                    }else if(head.equals(p5)){
                        Csv	tkn		=	new	Csv(line,"()");
                        format.put("p5", tkn.get(1));

                    }else if(head.equals(std)){
                        //読み飛ばす
                        
                    }else{
                        body.append(line);
                        body.append("\n");
                    }
                }else{
                    body.append(line);
                    body.append("\n");
                }
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /*
         * pmlが空の場合はnullを返す
         */
        if(format.size()==0){
            return	null;
        }
        return	format;
    }
    
	/**
	 * ハッシュに入ったフォーマットエイリアスを文字列に直して返す
	 * ただし、最後に標準書式 "##      "も付加する
	 * 
	 * @param ht
	 * @return
	 */
	public	static	String	hashToString(Hashtable ht){
	    
	    StringBuffer	formatbuf	=	new	StringBuffer();
	    Enumeration		e			=	ht.keys();
	    while(e.hasMoreElements()){
	        String	key		=	(String)(e.nextElement());
	        String	fmt		=	(String)(ht.get(key));
	        formatbuf.append("## $");
	        formatbuf.append(key);
	        formatbuf.append(" (");
	        formatbuf.append(fmt);
	        formatbuf.append(")");
	        formatbuf.append("\n");
	    }
	    /*
	     * 標準書式
	     */
        formatbuf.append("##           ");
        formatbuf.append("\n");
	    
	    return	formatbuf.toString();
	    
	}    

	
	public	static	Hashtable	stringToHash(String format){
	    
	    StringBuffer	dummy	=	new	StringBuffer();
	    Hashtable		fmt		=	FormatUtil.getAlias(format, dummy);
	    return		fmt;
	    
	}
	/**
	 * p1:････　形式のフォーマット文字列をハッシュに変換する
	 * 
	 * @param format
	 * @return
	 */
	public	static	Hashtable	formatToHash(String format){
	    
	    Hashtable		ht		=	new	Hashtable();
	    BufferedReader	in		=	new	BufferedReader(new	StringReader(format));
		try {
		    String	line;
		    while((line=in.readLine())!=null){
		        Csv	cs	=	new	Csv(line, ":");
		        ht.put(cs.get(0), cs.get(1));
		    }
		    in.close();
		} catch (IOException e) {
		    e.printStackTrace();
		}	    
	    return		ht;
	    
	}
}
