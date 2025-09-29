/*
   課題レコードを出力するための
　　コンテナクラス
	
	KadaiDefRecord クラスを保存再生する

*/
package export;
import java.io.Serializable;
//import java.text.*;
import java.util.Vector;

public class exKadai extends Object implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 3868226424540789779L;
	//private static final long serialVersionUID = 1L;
	//
	String  te_lec_key;
    String  kadai_key;
    String  seq_number;
    String  shubetsu;
    String  title;
    String  content;
	//
	// 新しいレコードにするためキーを書き換える
	public	void	keyEdit(String _te_lec_key){
		te_lec_key	= new String(_te_lec_key);
	}
	//
	 public exKadai(Vector v) {
		String	_te_lec_key	= (String)v.get(0);	if(_te_lec_key==null) 	_te_lec_key = "";		te_lec_key 	= new String(_te_lec_key);
		String  _kadai_key	= (String)v.get(1);	if(_kadai_key==null) 	_kadai_key 	= "";		kadai_key 	= new String(_kadai_key);
		String  _seq_number	= (String)v.get(2);	if(_seq_number==null) 	_seq_number = "";		seq_number 	= new String(_seq_number);
		String  _shubetsu	= (String)v.get(3);	if(_shubetsu==null) 	_shubetsu 	= "";		shubetsu 	= new String(_shubetsu);
		String  _title		= (String)v.get(4);	if(_title==null)		_title 		= "";		title 		= new String(_title);
		String  _content	= (String)v.get(5);	if(_content==null)		_content 	= "";		content 	= new String(_content);
	}
	//
	public Vector contents(){
		Vector v = new Vector(15,5);
		v.add(te_lec_key);
		v.add(kadai_key);
		v.add(seq_number);
		v.add(shubetsu);
		v.add(title);
		v.add(content);
		//
		return v;
	}
}
