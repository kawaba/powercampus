/*
    セクションレコードを出力するための
　　コンテナクラス
	
	KamokuSectionDefRecord クラスを保存再生する

*/
package export;
import java.io.Serializable;
//import java.text.*;
import java.util.Vector;

public class exSection extends Object implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = -2995289726027613996L;
	//private static final long serialVersionUID = 1L;
	//
	String  te_lec_key;
    String  sect_key;
    String  seq_number;
    String  title;
    String  content;
    String  note;
	String  ref_list;
	String  kadai_list;
	//
	// 新しいレコードにするためキーを書き換える
	public	void	keyEdit(String _te_lec_key){
		te_lec_key	= new String(_te_lec_key);
	}
	//
	public exSection(Vector v) {
		String  _te_lec_key	= (String)v.get(0);	if(_te_lec_key==null)	_te_lec_key  = "";		te_lec_key 	= new String(_te_lec_key);
    	String  _sect_key	= (String)v.get(1);	if(_sect_key==null) 	_sect_key 	 = "";		sect_key 	= new String(_sect_key);
    	String  _seq_number	= (String)v.get(2);	if(_seq_number==null) 	_seq_number  = "";		seq_number 	= new String(_seq_number);
    	String  _title		= (String)v.get(3);	if(_title==null) 		_title 		 = "";		title 		= new String(_title);
    	String  _content	= (String)v.get(4);	if(_content==null) 		_content 	 = "";		content 	= new String(_content);
    	String  _note		= (String)v.get(5);	if(_note==null) 		_note 		 = "";		note 		= new String(_note);
    	String  _ref_list	= (String)v.get(6);	if(_ref_list==null) 	_ref_list 	 = "";		ref_list 	= new String(_ref_list);
    	String  _kadai_list	= (String)v.get(7);	if(_kadai_list==null) 	_kadai_list  = "";		kadai_list  = new String(_kadai_list);
	}
	//
	public Vector contents(){
		Vector v = new Vector(15,5);
		v.add(te_lec_key);
		v.add(sect_key);
		v.add(seq_number);
		v.add(title);
		v.add(content);
		v.add(note);
		v.add(ref_list);
		v.add(kadai_list);
		//
		return v;
	}
}
