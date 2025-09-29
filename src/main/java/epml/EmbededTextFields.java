/*
    文中に埋め込まれたテキストボックスに正しいと思う語句を
	記入して解答する問題のクラスを保持する

*/
package epml;
import	epml.tools.*;
//import java.io.*;
//import java.text.*;
import java.util.*;
//
public class EmbededTextFields extends Object {
	
	String	key;		// 一意の識別キー
	Vector	items;		// 問題項
	int	size;		// 問題項の数
	int	points;		// 総得点
	//
	int	width;		// 
	int	height;		// 
	//
	final	int		FONTWIDTH	= 14;	// 標準フォントサイズ
	final	int		HEIGHT		= 18;	// 標準の行の高さ
	//
	
	public	EmbededTextFields(){
		if(DBG.fa) DBG.println("class EmbededTextFields #EmbededTextFields() : コンストラクタ の先頭です");
		key		= "";
		size	= 0;
		items	= new Vector(100,10);
		points	= 0;
		width	= FONTWIDTH;
		height	= HEIGHT;
	}
	public	EmbededTextFields(String _key){
		
		key		= _key;
		size	= 0;
		items	= new Vector(100,10);
		points	= 0;
		width	= FONTWIDTH;	// 指定がなければ標準を使う
		height	= HEIGHT;
	}
	public	EmbededTextFields(String _key,int _width,int _height){
		
		key		= _key;
		size	= 0;
		items	= new Vector(100,10);
		points	= 0;
		width	= _width;
		height	= _height;
	}
	// 全ての問題項目についてキーと(ユーザーの)解答をハッシュに入れる
	public	void	setHash(Hashtable htb){
		for(int	i=0; i<size; i++){
			cETF	obj		= 	get(i);
			String	eval	=	obj.getEval();
			// 採点済みかどうか調べ、採点済ならばデータ部分に正誤（t/f）をそうでなければ"-"を付加する
			// ans(0) は正解語の配列の先頭要素．学生の解答の場合、解答語＝正解語はひとつしかないのでこれでよい
			if((eval==null)||(eval.length()==0)){
				htb.put(obj.name(),obj.ans(0) + "~-");							// 例    "ETF(3)"  "水素~-"
			}else{
				htb.put(obj.name(),obj.ans(0) + "~" + obj.getEval());			// 例    "ETF(3)"  "水素~t"
			}
		}
	}
	//
	// 問題数
	public	int	size()		{ return	size; }
	public	int	allCount()	{ return	size; }
	//
	//
	// name で項目を検索する
	//
	public	cETF	get(String name0){
		//
		for(int	k=0; k<size; k++){
			String	name1 = get(k).name();
			if(name0.equals(name1)) 	return	get(k);
		}
		return	null;
	}
	// ｋ番目の項目を得る
	public	cETF	get(int k){
		if( !OK(k) ){
			DBG.println(" calss EmbededTextFields #get : 添え字の値が不正  k=" + k);
			// 項目数がゼロ（無回答）のときもnullを返す
			return	null;
		}
		cETF	a = (cETF) (items.get(k));
		return	a;
	}

	// 全項目に幅と高さを再設定する
	public	void	setWHAll(){
		for(int k=0; k<size; k++){
			setWH(k,width,height);
		}
	}
	// 表示桁幅
	public	int	getWidth()				{   return width;  	}
	public	void	setWidth(int w)		{	width	= w;	}
	public	void	clsWidth()				{	width	= 0;	}
	// 表示行の高さ
	public	int	getHeight()				{   return height; 	}
	public	void	setHeight(int w)		{	height	= w;	}
	public	void	clsHeight()				{	height	= 0;	}
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
	public	int	add(String name,String seqNum,String ans,int pt,int fontLevel){	// width height は既設定値で
		return add(name, seqNum, ans, pt,fontLevel,width, height);
	}
	public	int	add(String name,String seqNum,String ans,int pt,int fontLevel,int _width,int _height){
		cETF	item	= new cETF(name,seqNum,ans,pt,fontLevel,_width,_height);
		items.add(item);
		size	= items.size();
		return	size;
	}
	/**
	 * 学生の解答データの場合その正誤を返す
	 * 
	 * @param k	シーケンス番号
	 * @return		正解のとき"t" 不正解の時"f" ．ただし、まだ設定されていない時や問題データの場合は "" を返す
	 */
	public	String	getEval(int k){
		cETF	wk	= get(k);
		return	wk.getEval();
	}
	
	// 引数は学生の解答．本オブジェクトが正解
	// 全項目を採点する
	public	int	grades(EmbededTextFields ansS){
		int		points	= 0;
		for(int k=0; k<size; k++){
			
			cETF	a0 	= ansS.get(k);		// 学生の解答
			cETF	a1 	= get(k);			// 正解
			int	pt	= a1.grades(a0);
			points		+= pt;
		}
		return	points;
	}
	// 引数は学生の解答．本オブジェクトが正解
	// 全項目について解答と照合し、不正解語を cETF オブジェクトに蓄える
	public	void	preGrades(EmbededTextFields stuAns){
		if(DBG.fa) DBG.println("■ EmbededTextFields #preGrades() : 全項目について解答と照合し、不正解語を cETF オブジェクトに蓄える の先頭です");
		//
		if(DBG.fa) DBG.println("    size=" + size);
		int		points	= 0;
		if(DBG.fa){
		    stuAns.print();
		}
		for(int k=0; k<size; k++){
			//if(DBG.fa){
			//    DBG.println("       k=" + k);
			//}
			cETF	a0 	= stuAns.get(k);	// 学生の解答（無回答はnullが返る）
			//if(DBG.fa){
			//    if(a0==null) DBG.println(k + " is null!");
			//    continue;
			//}
			cETF	a1 	= get(k);			// 正解
			a1.preGrades(a0);				// 不正解語を蓄える
		}
	}
	// 不正解語リストをクリアする
	public void	removeAllIncorrectWords( int k ){
		if(DBG.fa) DBG.println("class EmbededTextFields #removeAllIncorrectWords() : 不正解語リストをクリアする の先頭です");
		//
		cETF	wk	= get(k);
		wk.removeAllIncorrectWords();
	}
	//
	// 正解語リストをクリアする
	public void	removeAllCorrectWords( int k ){
		if(DBG.fa) DBG.println("class EmbededTextFields #removeAllCorrectWords() : 正解語リストをクリアする の先頭です");
		
		cETF	wk	= get(k);
		wk.removeAllCorrectWords();
	}
	//
	// 正解語リストのみを書き換える
	public	void	updateAllCorrectWords(int k,Vector v){
		if(DBG.fa) DBG.println("class EmbededTextFields #updateAllCorrectWords() : 正解語リストのみを書き換える の先頭です");
		
		removeAllCorrectWords(k);
		Vector	list	= correctList(k);
		for(int m=0; m<v.size(); m++){
			list.add( (String)v.get(m) );
		}
	}
	//
	// 不正解語リストのみを書き換える
	public	void	updateAllIncorrectWords(int k,Vector v){
		if(DBG.fa) DBG.println("class EmbededTextFields #updateAllIncorrectWords() : 不正解語リストのみを書き換える の先頭です");
		
		removeAllIncorrectWords(k);
		Vector	list	= incorrectList(k);
		for(int m=0; m<v.size(); m++){
			list.add( (String)v.get(m) );
		}
	}
	//
	// 第ｋ番目の要素の正解語のリストを得る
	public	Vector	correctList( int k){
		if(DBG.fa) DBG.println("class EmbededTextFields #correctList() : 第ｋ番目の要素の正解語のリストを得る の先頭です");
		//
		cETF	wk	= get(k);
		return	wk.correctList();
	}
	//
	// 第ｋ番目の要素の不正解語のリストを得る
	public	Vector	incorrectList( int k){
		if(DBG.fa) DBG.println("class EmbededTextFields #incorrectList() : 第ｋ番目の要素の不正解語のリストを得る の先頭です");
		//
		cETF	wk	= get(k);
		return	wk.incorrectList();
	}
	//
	// 全項目に同じ配点 pt をセットする
	public	void	setApAll(int pt){
		for(int k=0; k<size; k++){
			setAp(k,pt);
		}
	}
	//
	//
	//-----------------------------------------------------------------------------------------------
	
	// ｋ番目の項目に幅と高さを設定する
	public	void	setWH(int k,int width,int height){
		cETF	a = get(k);
		if(a==null){
			DBG.println(" calss EmbededTextFields #getPoints : 添え字の値が不正  k=" + k);
			return;
		}
		a.setWidth(width);
		a.setHeight(height);
	}
	// ｋ番目の項目の得点を得る
	public	int	getPoints(int k){
		cETF	a = get(k);
		if(a==null){
			DBG.println(" calss EmbededTextFields #getPoints : 添え字の値が不正  k=" + k);
			return	-1;
		}
		return	a.getPoints();		
	}
	// ｋ番目の項目に配点 pt をセットする
	public	boolean	setAp(int k,int pt){
		cETF	a = get(k);
		if(a==null){
			DBG.println(" calss EmbededTextFields #getPoints : 添え字の値が不正  k=" + k);
			return	false;
		}
		a.setAp(pt);
		return	true;
	}
	// 等しいか
	public	boolean	equals(EmbededTextFields obj){
		if(key.equals(obj.key))		return true;
		return	false;
	}
	// 添え字は適正な範囲内か
	public	boolean	OK(int k){
		if( k >= size)	return	false;
		if( k < 0 )	return	false;
		return	true;
	}
	// 内容を出力する
	public void	print(){
		DBG.println("-- class EmbededTextFields");
		DBG.println("key : " + key);
		DBG.println("items :");
		for(int i=0; i<size; i++){
			DBG.println("  " + String.valueOf(i+1) + ":" + ((cETF)get(i)).toString() );
		}
		DBG.println("size : " + size);
		DBG.println("points : " + points);
		DBG.println("width : " + width);
		DBG.println("height : " + height);
	}

	
	
}