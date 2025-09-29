/*
    文中に埋め込まれたドロップダウンリストから
	正しいと思うものを選んで答える形式の問題

*/
package epml;
import	epml.tools.*;
//import java.io.*;
//import java.text.*;
import java.util.*;
//
public class EmbededWordLists extends Object {
	
	String	key;		// 一意の識別キー
	Vector	items;		// 問題項
	int	size;		// 問題項の数
	int	points;		// 総得点
	//
	public	EmbededWordLists(){
		if(DBG.fa) DBG.println("class EmbededWordLists #EmbededWordLists() : コンストラクタ の先頭です");
		key		= "";
		size	= 0;
		items	= new Vector(100,10);
		points	= 0;
	}
	public	EmbededWordLists(String _key){
		
		key		= _key;
		size	= 0;
		items	= new Vector(100,10);
		points	= 0;
	}
	// 全ての問題項目についてキーと(ユーザーの)解答をハッシュに入れる
	public	void	setHash(Hashtable htb){
		for(int	i=0; i<size; i++){
			cEWL	obj		= 	get(i);
			String	eval	=	obj.getEval();
			if((eval==null)||(eval.length()==0)){
				htb.put(obj.name(), obj.getCorrectNum() + "~-");	
			}else{
				htb.put(obj.name(), obj.getCorrectNum() + "~" + eval);
			}
		}
	}
	// 問題数
	public	int	size()		{ return	size; }
	public	int	allCount()	{ return	size; }
	//
	//
	// name で項目を検索する
	//
	public	cEWL	get(String name0){
		//
		for(int	k=0; k<size; k++){
			String	name1 = get(k).name();
			if(name0.equals(name1)) 	return	get(k);
		}
		return	null;
	}
	//
	// ｋ番目の項目を得る
	public	cEWL	get(int k){
		if( !OK(k) ){
			DBG.println(" calss EmbededWordLists #get : ■添え字の値が不正  k=" + k);
			return	null;
		}
		cEWL	a = (cEWL) (items.get(k));
		return	a;
	}
	// ｋ番目の項目のm番目の正解番号（１オリジン）を得る
	public	String	getCorrectNum(int k){
		cEWL	a = (cEWL) (items.get(k));
		return	a.getCorrectNum();
	}
	/**
	 * k 番目の項目の正誤文字を得る
	 * 
	 * @param k	シーケンス番号
	 * @return		正誤文字として "t", "f", ""  のいづれかを返す．
	 */
	public	String	getEval(int k){
		cEWL	a = (cEWL) (items.get(k));
		return	a.getEval();
	}
	//
	// ｋ番目の項目のm番目の選択語を得る
	public	String	getItem(int k,int m){
		if( !OK(k) ){
			DBG.println(" calss EmbededWordLists #get : ■添え字の値が不正  k=" + k);
			return	null;
		}
		cEWL	a = (cEWL) (items.get(k));
		return	a.getItem(m);
	}
	// ｋ番目の項目の選択語の数を得る
	public	int	sizeOfItems(int k){
		if( !OK(k) ){
			DBG.println(" calss EmbededWordLists #get : ■添え字の値が不正  k=" + k);
			return	0;
		}
		cEWL	a = (cEWL) (items.get(k));
		return	a.sizeOfItems();
	}
	//
	// キーを得る
	public	String	key()					{	return	key;	}
	//
	// 総得点
	public	int	points()				{   return points;  }
	public	void	setPoints(int	p)		{	points	= p;	}
	public	void	clsPoints()				{	points	= 0;	}
	//
	// 項目を追加する
	public	void add(String name,String seqNum,String ans,int ap,int fontLevel) throws EpmlTokenException {	// width height は既設定値で
		if(DBG.fa) DBG.println("class EmbededWordLists #add() :  項目を追加する の先頭です");
		if(DBG.fa) DBG.println("           name   = " + name);
		if(DBG.fa) DBG.println("           seqNum = " + seqNum);
		if(DBG.fa) DBG.println("           ans    = " + ans);
		if(DBG.fa) DBG.println("           ap     = " + ap);
		//
		try{
			cEWL	item	= new cEWL(name,seqNum,ans,ap,fontLevel);
			items.add(item);
			size	= items.size();
		}catch(EpmlTokenException e){
			throw e;
		}
	}
	// 全項目を採点する
	public	int	grades(EmbededWordLists ansS){
		int		points	= 0;
		for(int k=0; k<size; k++){
			
			cEWL	a0 	= 	ansS.get(k);
			cEWL	a1 	= 	get(k);
			int	pt	= 	a1.grades(a0);
			points		+= 	pt;
		}
		return	points;
	}
	// 全項目に同じ配点 pt をセットする
	public	void	setApAll(int pt){
		for(int k=0; k<size; k++){
			setAp(k,pt);
		}
	}
	//
	//
	//-----------------------------------------------------------------------------------------------
	//
	// ｋ番目の項目の得点を得る
	public	int	getPoints(int k){
		cEWL	a = get(k);
		if(a==null){
			DBG.println(" calss EmbededWordLists #getPoints : 添え字の値が不正  k=" + k);
			return	-1;
		}
		return	a.getPoints();		
	}
	// ｋ番目の項目に配点 pt をセットする
	public	boolean	setAp(int k,int pt){
		cEWL	a = get(k);
		if(a==null){
			DBG.println(" calss EmbededWordLists #getPoints : 添え字の値が不正  k=" + k);
			return	false;
		}
		a.setAp(pt);
		return	true;
	}
	// 等しいか
	public	boolean	equals(EmbededWordLists obj){
		if(key.equals(obj.key))		return true;
		return	false;
	}
	// 添え字は適正な範囲内か
	public	boolean	OK(int k){
		if( k >= size)	return	false;
		if( k < 0 )		return	false;
		return	true;
	}	
	// 内容を出力する
	public void	print(){
		DBG.println("-- class EmbededWordLists");
		DBG.println("key : " + key);
		DBG.println("items :");
		for(int i=0; i<size; i++){
			DBG.println("  " + String.valueOf(i+1) + ":" + ((cEWL)get(i)).toString() );
		}
		DBG.println("size : " + size);
		DBG.println("points : " + points);
	}
	

}