/*
 * 作成日: 2005/02/25
 *
 * TODO
 */
package kamoku;

import java.util.Hashtable;
import java.util.Vector;

import tktools.Gear;
import tktools.StringGear;
import tktools.TemplateBox;
import framework.LOG;

/**
 *　KamokuParser オブジェクトからXMLを生成するクラス<br>
 *　ウェブ入力を収めたハッシュテーブルをコンストラクタに与えると
 *　入力を反映したXMLを生成する
 *
 */
public class KamokuRevParser implements KamokuVar{

	/** 対象とする科目定義XMLから作成したKamokuParserオブジェクト */
	KamokuParser	kp;
	
	/** xml生成用テンプレートデータのパス( para.getTemplateParh() ) */
	String			templatePath;
	
	/** 生成したxml */
	String			xml;
	
	/** Webからの入力値で更新するとき入力値を保持したハッシュテーブル */
	Hashtable		web;
	
	/**
	 * パーサーからそのままXmlを生成する場合のコンストラクタ
	 * @param kp
	 * @param templatePath
	 */
	public	KamokuRevParser(KamokuParser kp, String templatePath){
		
		this.kp				=	kp;
		this.templatePath	=	templatePath;
		this.web			=	null;
		reverse();
	}
	/**
	 * パーサーとウェブ入力からXmlを生成する場合のコンストラクタ
	 * @param kp
	 * @param templatePath
	 * @param web
	 */
	public	KamokuRevParser(KamokuParser kp, String templatePath, Hashtable web){
		
		this.kp				=	kp;
		this.templatePath	=	templatePath;
		this.web			=	web;
		reverse();
	}	
	/**
	 * 生成したXMLを返す
	 * @return
	 */
	public String	getXml(){
		return	xml;
	}
	/**
	 * 所与の科目定義XMLデータと実際の入力値から、科目定義XMLを更新して生成する
	 * 
	 * @param xml
	 */
	void	reverse(){
		if(LOG.fa) LOG.println("KamokuEdit #updateXml()");
		
		StringBuffer	buf		=	new	StringBuffer(2048);	// 新しいXML
		TemplateBox		tb		=	new	TemplateBox(templatePath);
		buf.append(tb.get("headderXml"));
		/*
		 * <kamoku serial=_number> という科目タグを読み、_number を実際の serial に書き換える
		 */
		String	kamokuTag		=	StringGear.substitute(tb.get("kamokuTag"), "_number", kp.getSerial());
		buf.append(kamokuTag);
		/*
		 * タグ要素をひとつづつとりだしてXMLに書き換える
		 */
		kp.reset();
		while(kp.hasNext()){
			KamokuItem ki	=	(KamokuItem)kp.next();
			buf.append(getXml(ki, tb));
		}
		
		buf.append(tb.get("bottomXml"));
		xml	=	buf.toString();
		
	}
	/**
	 * type を判断してXmlデータを作成する<br>
	 * インスタンス変数 web が null ならば単にパーサーオブジェクトからXMLを生成する<br>
	 * web が null でない時は、web内の値を反映したXMLを生成する
	 * 
	 * @param ki
	 * @return
	 */
	String	getXml(KamokuItem ki, TemplateBox tb){
		if(LOG.fa) LOG.println("KamokuEdit #getXml()");
		
		String	xml	=	null;
		if(ki.isTextArea()){
			xml	=	textAreaXml(ki, tb);
			
		}else if(ki.isTextField()){
			xml	=	textFiledXml(ki, tb);
			
		}else if(ki.isListBox()){
			xml	=	listBoxXml(ki, tb);
			
		}else if(ki.isCheckBox()){
			xml	=	checkBoxXml(ki, tb);
			
		}
		return	xml;
	}	
	/**
	 * テキストエリアのXMLを生成する
	 * 
	 * @param ki
	 * @param tb
	 * @return
	 */
	String	textAreaXml(KamokuItem ki, TemplateBox tb){
		if(LOG.fa) LOG.println("KamokuEdit #textAreaXml()");
		
		String		html	=	tb.get("TextAreaXml");
		Hashtable	ht		=	new	Hashtable();

		ht.put("_name"	, ki.getTagName());
		ht.put("_label"	, ki.getLabel());
		ht.put("_row"	, (Gear.isEmpty(ki.getRow()) ? DEFAULT_ROWS   : ki.getRow()) );
		ht.put("_col"	, (Gear.isEmpty(ki.getCol()) ? DEFAULT_COLS_A : ki.getCol()) );
		ht.put("_content", getTextAreaContent(ki));
		
		return	StringGear.replace(html, ht, false);
	}
	/** 
	 * テキストエリアの内容部分を生成する<br>
	 * web が null かどうかによって生成の方法を分ける
	 *  
	 * @param ki
	 * @return
	 */
	String	getTextAreaContent(KamokuItem ki){
		String	content	=	null;
		if(web!=null){
			content	=	getItem( ki.getTagName() );// _ の付加をしない
		}else{
			content	=	ki.getText();
		}
		return	content;
	}
	/**
	 * テキストフィールドのXMLを生成する
	 * 
	 * @param ki
	 * @param tb
	 * @return
	 */
	String	textFiledXml(KamokuItem ki, TemplateBox tb){
		if(LOG.fa) LOG.println("KamokuEdit #textFiledXml()");
		
		String		html	=	tb.get("TextFieldXml");
		Hashtable	ht		=	new	Hashtable();

		ht.put("_name"	, ki.getTagName());
		ht.put("_label"	, ki.getLabel());
		ht.put("_col"	, (Gear.isEmpty(ki.getCol()) ? DEFAULT_COLS_A : ki.getCol()) );
		ht.put("_content", getTextFieldContent(ki));

		return	StringGear.replace(html, ht, false);	
	}
	/**
	 * テキストフィールドの内容部分を生成する<br>
	 * web が null かどうかによって生成の方法を分ける
	 * 
	 * @param ki
	 * @return
	 */
	String	getTextFieldContent(KamokuItem ki){
		String	content	=	null;
		if(web!=null){
			content	=	getItem( ki.getTagName() );// _ の付加をしない
		}else{
			content	=	ki.getText();
		}
		return	content;
	}
	/**
	 * リストボックスのXMLを生成する
	 * 
	 * @param ki
	 * @param tb
	 * @return
	 */
	String	listBoxXml(KamokuItem ki, TemplateBox tb){
		if(LOG.fa) LOG.println("KamokuEdit #listBoxXml()");
		
		String		html	=	tb.get("ListBoxXml");	// xmlテンプレート
		Hashtable	ht		=	new	Hashtable();

		ht.put("_name"		, ki.getTagName());
		ht.put("_label"		, ki.getLabel());
		ht.put("_content"	, getListBoxContent(ki));

		return	StringGear.replace(html, ht, false);	
	}
	/**
	 * リストボックスの内容部分を生成する<br>
	 * web が null かどうかによって生成の方法を分ける
	 * 
	 * @param ki
	 * @return
	 */
	String	getListBoxContent(KamokuItem ki){

		String	content	=	null;
		if(web==null){
			content	=	ki.getText();
			
		}else{
			StringBuffer	buf	=	new	StringBuffer();
			boolean		spc	=	false;
			Vector			elm	=	ki.getElm();
			
			String		value	=	getItem(ki.getTagName());// 選択された選択肢文字列
			for(int i=0; i<elm.size(); i++){

				// elm の要素には選択されていたものに * がついているのでこれを取ってから比較する
				String	oldItem	=	reset((String)elm.get(i));
				String	item		=	"";
				if(value.equals(oldItem)){
					item	=	set(oldItem);
				}else{
					item	=	reset(oldItem);
				}
				if(spc)	buf.append(" ");
				buf.append(item);
				spc	=	true;
			}
			content	=	buf.toString();
		}
		return	content;
		
	}
	/**
	 * チェックボックスのXMLを生成する
	 * 
	 * @param ki
	 * @param tb
	 * @return
	 */
	String	checkBoxXml(KamokuItem ki, TemplateBox tb){
		if(LOG.fa) LOG.println("KamokuEdit #checkBoxXml()");
		
		String		html	=	tb.get("CheckBoxXml");	// xmlテンプレート
		Hashtable	ht		=	new	Hashtable();

		ht.put("_name"		, ki.getTagName());
		ht.put("_label"		, ki.getLabel());
		ht.put("_content"	, getCheckBoxContent(ki));

		return	StringGear.replace(html, ht, false);	
	}
	/**
	 * チェックボックスの内容部分を生成する<br>
	 * web が null かどうかによって生成の方法を分ける
	 * 
	 * @param ki
	 * @return
	 */
	String	getCheckBoxContent(KamokuItem ki){

		String	content	=	null;
		if(web==null){
			content	=	ki.getText();
			
		}else{
			StringBuffer	buf	=	new	StringBuffer();
			boolean		spc	=	false;
			Vector			elm	=	ki.getElm();// 選択肢
			for(int i=0; i<elm.size(); i++){
				String	item	=	(String)elm.get(i);
				String	value	=	getItem(ki.getTagName() + String.valueOf(i));// 選択されてなければnull
				if(Gear.isEmpty(value)){
					item	=	reset(item);
				}else{
					item	=	set(item);
				}
				if(spc)	buf.append(" ");
				buf.append(item);
				spc	=	true;
			}
			content	=	buf.toString();
		}
		return	content;
		
	}	
	/**
	 * 要素の先頭にマーク(*)が付いていれば取り去る
	 * @param item
	 * @return
	 */
	String	reset(String item){
		if(LOG.fa) LOG.println("KamokuEdit #reset()");
		
		if(Gear.isEmpty(item))	return	item;
		
		if(item.charAt(0)=='*'){
			return	item.substring(1);
		}else{
			return	item;
		}
	}
	/**
	 * 要素の先頭にマーク(*)が付いていなければ付ける
	 * @param item
	 * @return
	 */
	String	set(String item){
		if(LOG.fa) LOG.println("KamokuEdit #set()");
		
		if(Gear.isEmpty(item))	return	item;
		
		if(item.charAt(0)=='*'){
			return	item;
		}else{
			return	"*" + item;
		}
	}
	/**
	 * ハッシュテーブルから値を取り出す
	 * @param key
	 * @return
	 */
	String	getItem(String	key){
		if(!Gear.isEmpty(key)){
			return	Gear.strHash(web, key);
		}
		return	"";		
	}
	
}
