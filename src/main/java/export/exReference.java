/*
   課題レコードを出力するための
　　コンテナクラス
	
	ReferenceDefRecord クラスを保存再生する

*/
package export;

import java.io.Serializable;
import java.util.Vector;
import database.KeyGen;
import framework.LOG;
import refer.ReferenceDefRecord;
import tktools.StringGear;

public class exReference extends Object implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = -603358362090123082L;
	//private static final long serialVersionUID = 1L;
	//
	String  te_lec_key;
    String  ref_key;
    String  seq_number;
    String  shubetsu;
    String  title;
    String  url;
	
    //
	// 新しいレコードにするためキーを書き換える
	public	void	keyEdit(String te_lec, String new_lec){
		if(LOG.fa) LOG.println("■■■■ exReference #keyEdit()");
		/*
		 * 古いキーを取り出す
		 */
		String	old_teUid	=	KeyGen.teUidFromTe_lec(te_lec_key);
		String	old_lec		=	KeyGen.lecFromTe_lec(te_lec_key);
		/*
		 * 古い科目キーを新しいものに書き換える
		 */
		te_lec_key		= 	new String(te_lec);
		String	teUid	=	KeyGen.teUidFromTe_lec(te_lec);
		
		/* 
		 * web作成ではurlの中の[lec_key]部分を正しいものに書き換える
		 */
		if(shubetsu.equals(ReferenceDefRecord.REF_HTML)){

			String	oldStr	=	old_teUid + "/html/" + old_lec + "/";
			String	newStr	=	teUid     + "/html/" + new_lec + "/";
			url	=	StringGear.replace(url, oldStr, newStr);
		}
		
	}
	//
	public exReference(Vector v) {
		String	_te_lec_key	= (String)v.get(0);	if(_te_lec_key==null) 	_te_lec_key 	= "";		te_lec_key 	= new String(_te_lec_key);
		String  _ref_key	= (String)v.get(1);	if(_ref_key==null) 		_ref_key 		= "";		ref_key 	= new String(_ref_key);
		String  _seq_number	= (String)v.get(2);	if(_seq_number==null)	 _seq_number 	= "";		seq_number 	= new String(_seq_number);
		String  _shubetsu	= (String)v.get(3);	if(_shubetsu==null) 	_shubetsu 		= "";		shubetsu 	= new String(_shubetsu);
		String  _title		= (String)v.get(4);	if(_title==null) 		_title 			= "";		title 		= new String(_title);
		String  _url		= (String)v.get(5);	if(_url==null) 			_url 			= "";		url 		= new String(_url);
	}
	//
	public Vector contents(){
		Vector v = new Vector(15,5);
		v.add(te_lec_key);
		v.add(ref_key);
		v.add(seq_number);
		v.add(shubetsu);
		v.add(title);
		v.add(url);
		//
		return v;
	}
}
