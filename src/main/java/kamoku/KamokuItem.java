/*
 * 作成日: 2005/02/14
 *
 * 
 * 
 * 
 */
package kamoku;
import		java.util.*;
import		tktools.*;
import		xmlparser.*;
/**
 * 科目定義のひとつの項目に対応するオブジェクト．
 * ひとつのXML記述単位についての情報を保持する．
 */
public class KamokuItem extends	Object{
	
	/** 項目タグ名 */
	String	tagName;
	
	/** 項目表示名 */
	String	label;
	
	/** 項目のタイプ（テキスト、リスト、チェックボックス） */
	String	type;
	
	/** テキストの時の入力領域の行数 */
	String	row;
	
	/** テキストの時の１行の入力文字数(規定値は100） */
	String	col;
	
	/** テキスト文字列全体 */
	String	text;
	
	/** リスト項目またはチェック項目としてのテキスト要素 */
	Vector	elm;
	
	public	KamokuItem(XmlToken xt, String text) throws	 xmlException{
		
		if(!xt.isTag())	throw	new xmlException("KamokuItem #コンストラクタ :タグでないxmlTokenで初期化しようとした");
		tagName	=	xt.getTagName();
		
		// xt.get() では、要素に対応するデータがない時は "" が返される
		label	=	xt.get("label");
		type	=	xt.get("type");
		row		=	xt.get("row");
		col		=	xt.get("col");
		
		// テキストの行末の改行コードを取る
		if(!Gear.isEmpty(text)){
			int	n	=text.length();
			if(text.charAt(n-1)=='\n'){
				text	=	text.substring(0,n-1);
			}
		}
		// リストやチェックボックスのためにtextから要素を作っておく
		elm		=	new	Vector();
		this.text	=	text;
		if(!Gear.isEmpty(text)){
			TextToken	pt	=	new	TextToken(text);
			String		s;
			while((s=pt.getNext())!=null){
				elm.add(s);
			}			
		}
	}
	/**
	 * 強制的にtextをセットする
	 * 
	 */
	public	void	setText(String str){
		text	=	str;
	}
	
	/**
	 * テキスト領域か
	 * @return
	 */
	public	boolean	isTextArea(){
		if(type.equals("text")){
			if(!Gear.isEmpty(row)){
				return	true;
			}
		}
		return	false;
	}
	/**
	 * テキストフィールドか
	 * @return
	 */
	public	boolean	isTextField(){
		if(type.equals("text")){
			if(Gear.isEmpty(row)){
				return	true;
			}
		}
		return	false;
	}
	/**
	 * リストボックスか
	 * @return
	 */
	public	boolean	isListBox(){
		if(type.equals("list")){
			return	true;
		}
		return	false;
	}
	/**
	 * チェックボックスか
	 * @return
	 */
	public	boolean	isCheckBox(){
		if(type.equals("check")){
			return	true;
		}
		return	false;
	}	
	
	/**
	 * CSV形式のテキストデータを持つタイプかどうか
	 * @return		該当するときtrue
	 */
	public	boolean	hasCsvText(){
		if(type.equals("list")||type.equals("check")){
			return	true;
		}
		return	false;
	}
	
	/**
	 * タグ名を返す
	 * @return		タグ名
	 */
	public	String	getTagName(){
		return	tagName;
	}
	public	String	getType(){
		return	type;
	}
	public	String	getLabel(){
		return	label;
	}
	public	String	getRow(){
		return	row;
	}
	public	String	getCol(){
		return	col;
	}
	public	String	getText(){
		return	text;
	}
	public	Vector	getElm(){
		return	elm;
	}
	/**
	 * 出力
	 *
	 */
	public void	print(){
		
		System.out.println("");
		System.out.println("■ KamokuItem ");
		System.out.println("　　tagName:" + tagName);
		System.out.println("　　label  :" + label);
		System.out.println("　　type   :" + type);
		System.out.println("　　row    :" + row);
		System.out.println("　　col    :" + col);
		if(type.equals("text")){
			System.out.println("　　text:");
			System.out.println("『" + text + "』");
			System.out.println("---------");
		}else{
			System.out.println("　　elements:");
			for(int k=0; k<elm.size(); k++){
				System.out.println("        " + (String)elm.get(k));
			}
		}
	}
	
}
