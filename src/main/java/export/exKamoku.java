/*
    科目レコードを出力するための
　　コンテナクラス
	
	KamokuDefRecord クラスを保存再生する

*/
package export;
import java.io.Serializable;
//import java.text.*;
import java.util.Vector;

public class exKamoku extends Object implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 8127697935555633198L;
	//private static final long serialVersionUID = 1L;
	//
	String  teuid;
    String  lec_key;
    String  title;
    String  content;
    String  seiseki_hyoka;
    String  keywords;
    String  textbook;
    String  ref_texts;
    String  ref_urls;
    String  note;
    String  bikou;
	//
	//
	// 新しいレコードにするためキーを書き換える
	public	void	keyEdit(String _teUid,String _lec_key){
		teuid	= new String(_teUid);
		lec_key	= new String(_lec_key);
	}
	//
	public exKamoku(Vector v) {
		String  _teuid			= (String)v.get(0);		if(_teuid==null)		 _teuid 	= "";			teuid 		= new String(_teuid);
    	String  _lec_key		= (String)v.get(1);		if(_lec_key==null) 		_lec_key	= "";			lec_key 	= new String(_lec_key);
    	String  _title			= (String)v.get(2);		if(_title==null) 		_title 		= "";			title 		= new String(_title);
    	String  _content		= (String)v.get(3);		if(_content==null) 		_content 	= "";			content 	= new String(_content);
    	String  _seiseki_hyoka	= (String)v.get(4);		if(_seiseki_hyoka==null) _seiseki_hyoka = "";		seiseki_hyoka = new String(_seiseki_hyoka);
    	String  _keywords		= (String)v.get(5);		if(_keywords==null) 	_keywords 	= "";			keywords 	= new String(_keywords);
    	String  _textbook		= (String)v.get(6);		if(_textbook==null) 	_textbook 	= "";			textbook 	= new String(_textbook);
    	String  _ref_texts		= (String)v.get(7);		if(_ref_texts==null) 	_ref_texts 	= "";			ref_texts 	= new String(_ref_texts);
    	String  _ref_urls		= (String)v.get(8);		if(_ref_urls==null)		_ref_urls 	= "";			ref_urls 	= new String(_ref_urls);
    	String  _note			= (String)v.get(9);		if(_note==null) 		_note 		= "";			note 		= new String(_note);
    	String  _bikou			= (String)v.get(10);	if(_bikou==null) 		_bikou 		= "";			bikou 		= new String(_bikou);
	}
	//
	public Vector contents(){
		Vector v = new Vector(15,5);
		v.add(teuid);
		v.add(lec_key);
		v.add(title);
		v.add(content);
		v.add(seiseki_hyoka);
		v.add(keywords);
		v.add(textbook);
		v.add(ref_texts);
		v.add(ref_urls);
		v.add(note);
		v.add(bikou);
		//
		return v;
	}
}