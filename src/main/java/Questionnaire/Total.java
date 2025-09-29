/*
 * アンケート処理パッケージ
 * 
 */
package Questionnaire;
import		java.util.*;
import		java.io.*;
import		epml.*;
import framework.LOG;
import		tktools.*;
/**
 * アンケート結果からCSVファイルを作成する
 *
 */
public class Total extends Object{
	/**	 回答のCsv文字列を件数分格納する */
	Vector	replies;
	
	/** 初期化処理で回答のCsvを作成するコンストラクタ */
	public	Total(Vector emls){
		replies		=	new Vector();
		
		Exam	exm	=	null;
		int	n	=	emls.size();
		for(int i=0; i<n; i++){
			exm	=	(Exam)emls.get(i);
			replies.add(reply(exm));		// CSV文字列作成
		}
	}
	/**
	 * 回答をCSVファイルとして出力する（Windows-31J）
	 * @param path		ファイル名を含む出力パス
	 */
	public void mkCsvFile(String path){
		PrintWriter 	out;
		try {
			out	= new PrintWriter(new BufferedWriter(new OutputStreamWriter( new FileOutputStream(path),"Windows-31J")),true);
			String	str	=	null;
			int	n	=	replies.size();
			for(int i=0; i<n; i++){
				out.println((String)replies.get(i));				
			}
			out.close();
        
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * 1件の回答をCSV文字列にして返す
	 * @param em	回答を含むExamオブジェクト
	 * @return		CSV文字列
	 */
	String	reply(Exam em){
		Hashtable	htb	=	setHash(em);
		String	[]	rp	=	new String [htb.size()];
		getElementArray(htb,rp);
		Arrays.sort(rp);
		return	getElementCsv(rp);	
		
	}
	/**
	 * ハッシュにキーと回答を取り出す
	 * @param em	回答を含むExamオブジェクト
	 * @return		キーと回答を含むハッシュ
	 */
	Hashtable	setHash(Exam em){
		Hashtable	htb	=	new	Hashtable(100);
		em.setHash(htb);
		return	htb;
	}
	/**
	 * ハッシュ内の回答をキーでソートした文字配列にして返す
	 * @param h	キーと回答を含むハッシュ
	 * @return		キーと回答をソート済み文字配列
	 */
	String [] getElementArray(Hashtable h,String []ret){
		Enumeration	en	=	h.keys();
		int 	n 		=	h.size();
		for(int i=0; i<n; i++){
			String	key		=	(String)en.nextElement();
			String	value	=	(String)h.get(key);
			ret[i]			=	keyNo(key) + "=" + value;
		}
		return	ret;
		
	}
	String	keyNo(String key){
		Csv		cs	=	new Csv(key,"()");
		String	no	=	get000type(cs.get(1));
		return	cs.get(0) + "(" + no + ")";
	}
	public static String get000type(String dt){
		if((dt == null)||(dt.length() == 0)) return "000";
		String	dt2		= dt.trim();
		int	pos		= dt2.length();
		String  pattern = "000" + dt2;
		return pattern.substring(pos);
	}	
	/**
	 * 回答だけをCSV文字列にして返す<br>
	 * キーをとりのぞき、回答の中の ~ 以下を捨てる
	 * @param strs		キーと回答をソート済み文字配列
	 * @return			回答だけのCSV文字列
	 */
    String	getElementCsv(String [] strs){
		if(LOG.fa){
			LOG.println("■ class Total #getElementCsv() ");
			for(int i=0; i<strs.length; i++){
				LOG.println("rp[" + i + "]  " + strs[i]);
			}
		}
		//
		StringBuffer bf		= new StringBuffer(); 
    	Csv			cs1 	= null;
    	Csv			cs2 	= null;
    	String		ss		= null;
    	boolean	flag	= false;	
    	int 		n 		= strs.length;
    	for(int i=0; i<n; i++){
    		if(flag){
    			bf.append(",");
    		}
    		cs1		= new Csv(strs[i],"=");
    		cs2		= new Csv(cs1.get(1),"~");
    		ss		= deletCTRL( substitute(cs2.get(0),","," ") );
    		flag	= true;
			bf.append(ss);
    	}
    	return	bf.toString();
    }
	// source 文字列の全ての target を rep に置き換える
	public String	substitute(String source,String target,String rep){
		//
		int	pos 		= source.indexOf(target);
		if(pos<0)	return	source;
		//
		int	len			= target.length();
		String	str1	= "";
		String	str2	= "";
		//
		try{
			str1	= source.substring(0,pos);
		}catch(IndexOutOfBoundsException  e){
			str1 = "";
		}
		try{
			str2	= substitute(source.substring(pos+len),target,rep);
		}catch(IndexOutOfBoundsException  e){
			str2 = "";
		}
		//
		return	str1 + rep + str2;
	}
	public String	deletCTRL(String str){
		StringBuffer buf	=	new StringBuffer();
		int n = str.length();
		for(int i=0; i<n; i++){
			char c	=	str.charAt(i);
			if(Character.isISOControl(c)){
				buf.append(' ');
			}else{
				buf.append(c);
			}
		}
		return	buf.toString();
		
	}

}
