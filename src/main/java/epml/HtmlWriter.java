/*
     記述項をHTMLに置き換える出力処理クラス

*/
package epml;
import	epml.tools.*;
import java.io.*;
//import java.text.*;
import java.util.*;

//
public class HtmlWriter extends OptionPrint {
	
	TemplateBox				tb;
	EmbededNumberLists		enl;
	EmbededWordLists		ewl;
	EmbededTextFields		etf;
	EmbededRadioButtons		erb;
	EmbededTextAreas		eta;
	//
	private	static	String [] fontClass = {"0","k20","k18","k16","k14","k12","k10","k9"}; // 1 - 7 に対応
	private	static	String [] inpClass  = {"0","inp20","inp18","inp16","inp14","inp12","inp10","inp9"}; // 1 - 7 に対応
	
	public HtmlWriter(	String	htmlPath,
						EmbededNumberLists		_enl,
						EmbededWordLists		_ewl,
						EmbededTextFields		_etf,
						EmbededRadioButtons		_erb,
						EmbededTextAreas		_eta){
		
		tb		= new TemplateBox(htmlPath);
		enl		= _enl;
		ewl		= _ewl;
		etf		= _etf;
		erb		= _erb;
		eta		= _eta;
		//
	}
	//
	String	divide(String s){
		Csv	cs	= new Csv(s,"()");
		return  cs.get(0);
	}
	//
	// ハッシュテープル(htb)を使って key で特定される出力処理を行う
	//
	public void	write(String originalKey,PrintWriter out,Hashtable htb){
		//
		String subkey	=  divide(originalKey);
		//
		if(subkey.equals("ENL")){
			ENL(originalKey,out,htb);
			/*
		}else if(subkey.equals("EWL")){
			EWL(originalKey,out,htb);
			
		}else if(subkey.equals("ETF")){
			ETF(originalKey,out,htb);
			
		}else if(subkey.equals("ERB")){
			ERB(originalKey,out,htb);
			
		}else if(subkey.equals("ETA")){
			ETA(originalKey,out,htb);
				*/
		}else{
			System.out.println("class HtmlWriter #write() : キーの指定が間違っている．");
			return;
			//
		}
	
	}
	//
	// 数値番号を入力するテキストフィールドを出力する
	void	ENL(String key,PrintWriter out,Hashtable htb){
		//
		cENL item = enl.get(key);
		if(item == null){
			System.out.println("class HtmlWriter #ENL() : item is null !");
			return;
		}
		int		k		= item.fontLevel();
		String	htmlDoc	= tb.get("textField");
		//
		// <input type="text" name="%_name%"  class="%_classname%" style="width: %_length%px">
		htb.put("_name",key);				//
		htb.put("_classname",inpClass[k]);	//
		htb.put("_length","40");			// 固定値
		// 
		PrintView	pv	= new PrintView(out);
		pv.crlf_off();
		pv.PrintString(htb, htmlDoc,this);		// 単なる書き換えのみならOptionPrintオブジェクトは不要（null で実行する）
	}
	
	
	
	
}