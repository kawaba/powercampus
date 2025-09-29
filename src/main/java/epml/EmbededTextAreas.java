/*
　　テキストエリアに正しいと思う言葉や文章を記入して解答する問題のクラス
	
*/
package epml;
//import java.io.*;
//import java.text.*;
import java.util.Hashtable;
import java.util.Vector;
import epml.tools.DBG;
//
public class EmbededTextAreas extends Object {
	
	String	key;		// 一意の識別キー
	Vector	items;		// 問題項
	int		size;		// 問題項の数
	int		points;		// 総得点
	//
	
	public	EmbededTextAreas(){
		if(DBG.fa) DBG.println("class EmbededTextAreas #EmbededTextAreas() : コンストラクタ の先頭です");
		key		= "";
		size	= 0;
		items	= new Vector(100,10);
		points	= 0;
	}
	public	EmbededTextAreas(String _key){
		
		key		= _key;
		size	= 0;
		items	= new Vector(100,10);
		points	= 0;
	}
	public	EmbededTextAreas(String _key,int _width,int _height){
		
		key		= _key;
		size	= 0;
		items	= new Vector(100,10);
		points	= 0;
	}
	// 全ての問題項目についてキーと(ユーザーの)解答をハッシュに入れる
	/*
	 * 2022.03.30 追記
	 * 解答の数が0なら、解答がないことを意味する
	 * その場合、obj.ans(0)はnullを返すので、htb.putで例外が発生する
	 * 解答がない場合、obj.ans(0)が " "を返すように修正した
	 */
	public	void	setHash(Hashtable htb){
		for(int	i=0; i<size; i++){
			cETA	obj	= get(i);
			htb.put(obj.name(), obj.ans(0));
		}
	}
	//
	// 問題数
	public	int	size()		{ return	size; }
	public	int	allCount()	{ return	size; }
	//
	// ｋ番目の問題の高さと幅を求めるて文字列 "/rows,cols" にして返す
	public	String	getSizeString(int	k){
		
		return	"/" + String.valueOf(get(k).getRows()) + "," + String.valueOf(get(k).getCols());
		
	}
	//
	// name で項目を検索する
	//
	public	cETA	get(String name0){
		//
		for(int	k=0; k<size; k++){
			String	name1 = get(k).name();
			if(name0.equals(name1)) 	return	get(k);
		}
		return	null;
	}
	// ｋ番目の項目を得る
	public	cETA	get(int k){
		if( !OK(k) ){
			DBG.println(" calss EmbededTextAreas #get : 添え字の値が不正  k=" + k);
			return	null;
		}
		cETA	a = (cETA) (items.get(k));
		return	a;
	}
	// ｋ番目の項目の正解語リストを得る
	public	Vector	correctList(int k){
		cETA	a = (cETA) (items.get(k));
		return	a.correctList();
	}
	// ｋ番目の項目のｍ番目の正解を得る
	public	String	get(int k,int m){
		cETA	a = (cETA) (items.get(k));
		return	a.getAnswer(m);
	}
	//
	// キーを得る
	public	String	key()					{	return	key;	}
	//
	// 総得点
	public	int		points()				{   return points;  }
	public	void	setPoints(int	p)		{	points	= p;	}
	public	void	clsPoints()				{	points	= 0;	}
	//
	// 項目を追加する
	public	int	add(String name,String seqNum,String ans,int pt,int fontLevel) throws EpmlTokenException {	// width height は既設定値で
		cETA	item = null;
		try{
			item	= new cETA(name,seqNum,ans,pt,fontLevel);
		}catch(EpmlTokenException e){
			throw e;
		}
		//
		//item	= new cETA(name,seqNum,ans,pt);
		items.add(item);
		size	= items.size();
		return	size;
	}
	// 全項目を採点する
	public	int	grades(EmbededTextAreas ansS){
		
	    return	0;
		/*
	    int		points	= 0;
		for(int k=0; k<size; k++){
			
			cETA	a0 	= ansS.get(k);
			cETA	a1 	= get(k);
			int		pt	= a1.grades(a0);
			points		+= pt;
		}
		return	points;
		*/
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
	
	// ｋ番目の項目の得点を得る
	public	int	getPoints(int k){
		cETA	a = get(k);
		if(a==null){
			DBG.println(" calss EmbededTextAreas #getPoints : 添え字の値が不正  k=" + k);
			return	-1;
		}
		return	a.getPoints();		
	}
	// ｋ番目の項目に配点 pt をセットする
	public	boolean	setAp(int k,int pt){
		cETA	a = get(k);
		if(a==null){
			DBG.println(" calss EmbededTextAreas #getPoints : 添え字の値が不正  k=" + k);
			return	false;
		}
		a.setAp(pt);
		return	true;
	}
	// 等しいか
	public	boolean	equals(EmbededTextAreas obj){
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
		DBG.println("-- class EmbededTextAreas");
		DBG.println("key : " + key);
		DBG.println("items :");
		for(int i=0; i<size; i++){
			DBG.println("  " + String.valueOf(i+1) + ":" + get(i).toString() );
		}
		DBG.println("size : " + size);
		DBG.println("points : " + points);
	}
}