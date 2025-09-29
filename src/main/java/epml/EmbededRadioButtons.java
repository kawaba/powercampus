/*
     試験問題を保持するクラス

	複数行のテキストのどれかにチェックを入れて
	正解を示すクラス


*/
package epml;
import		epml.tools.*;
import 	java.util.*;
//
public class EmbededRadioButtons extends Object {
	
	String	key;		// 一意の識別キー
	Vector	items;		// 問題項
	int	points;		// 総得点
	String	groupNow;	// 現在のグループ番号（１オリジン）
	//
	//
	public	EmbededRadioButtons(){
		if(DBG.fa) DBG.println("class EmbededRadioButtons #EmbededRadioButtons() : コンストラクタ の先頭です");
		key			= "";
		items		= new Vector(10,5);
		groupNow	= null;
		points		= 0;
	}
	public	EmbededRadioButtons(String _key){
		key			= _key;
		items		= new Vector(10,5);
		groupNow	= null;
		points		= 0;
	}
	/**
	 * 第ｊグループの問題はチェックボックスタイプかどうかを返す
	 * 
	 * @param j    グループ番号
	 * @return		チェックボックスのとき true ,そうでなければ false
	 */
	public	boolean	getCksign(int j){
		cERB	temp	=	get(j);
		return	temp.isCheckbox();
	}
	/**
	 * グループｊのｋ番目の文について、学生の解答が正しかったかどうかを返す
	 * 
	 * t/f は採点時に付加されている
	 * 
	 * @param j    グループ番号
	 * @param k    問題文番号
	 * @return		正解のとき "t", そうでなければ "f"
	 */
	public String	getEval(int j, int k){
		cERB	temp	=	get(j);
		return	temp.getEval(k);
	}
	// 全ての問題項目についてキーと(ユーザーの)解答をハッシュに入れる
	public	void	setHash(Hashtable htb){
		//
		String	keyName	= "";
		for(int	i=0; i<items.size(); i++){
			cERB	obj	= get(i);
			int		max	= obj.size();
			for(int j=0; j<max; j++){
				
				//
				// 採点済みかどうか調べ、採点済ならばデータ部分に正誤（t/f）をそうでなければ"-"を付加する
				//
				String	eval	= obj.getEval(j);
				String	ansChk	=	"";
				if((eval==null)||(eval.length()==0)){
					ansChk	=	"~-";
				}else{
					ansChk	=	"~" + eval;
				}
				// チェックボックスとラジオボタンで処理を分ける
				if(isCheckbox(i)){
					keyName	= "ERB(" + String.valueOf(i+1) + "," + String.valueOf(j+1) + ")";	// ERB(1,2) の形式
					setHashCheck(j,htb,keyName,obj,ansChk);
				}else{
					keyName	= obj.name();														// ERB(1) の形式
					setHashRadio(j,htb,keyName,obj,ansChk);
				}
			}
		}
	}	
	//
	void	setHashCheck(int j, Hashtable htb, String keyName, cERB obj, String ansChk){
		//		
		if((obj.answer(j)).equals("1")){
			htb.put(keyName,String.valueOf(j+1) + ansChk);	
		}else{
			htb.put(keyName,"0" + ansChk);	// 選択されていないことをしめすため０を．（ref. Exam # createAnswerText(Hashtable htb) ）
		}
	}
	void	setHashRadio(int j, Hashtable htb, String keyName, cERB obj, String ansChk){
		//
		// ｊ番目が正解なら、ERB(J) ,"1" をハッシュにセットする
		// そうでなければ何もセットしない
		if((obj.answer(j)).equals("1")){
			htb.put(keyName,String.valueOf(j+1) + ansChk);	
		}
		
	}
	



	
	// 選択肢のうちどれが正解か＊をつけていない問題があるかどうか
	public boolean checkCorrects(){
		int n	= size();
		if(n==0) return	true;
		//
		for(int i=0; i<n; i++){
			if( get(i).corects()==0) return false;
		}
		return true;
	}
	// チェックボックスの場合の解答を示す文字列を作成する
	//
	//　チェックされた項目には、それが２番目の選択肢であれば "2" という文字列がhtbの中にセットされている
	//　例えば、２番目と３番目にチェックが入っていれば、htbの中で、
	//  ERB(1,2) == "2", ERB(1,3) = "3" とセットされている
	//  この場合、選択肢の数が３個であれば、"0,1,1" という文字列が解答を示す文字列である
	//
	String	getCB(Hashtable htb,String grp,int	n){
		
		StringBuffer	xbuf	= new StringBuffer(50);
		boolean			comma	= false;
		for(int	i=0; i<n; i++){
			String	subkey	= "ERB(" + grp + "," + String.valueOf(i+1) + ")";	// ERB(1,3) など
			String	val		= Exam.strHashZERO(htb, subkey);					// ハッシュをkeyで検索．なければ"0"を返す
			if(!val.equals("0")){	// 何かセットされていれば
				val	= "1";
			}
			if(comma)	xbuf.append(",");
			xbuf.append(val);
			comma = true;
		}
		return	xbuf.toString();	// "0,1,1" のようなCSV．この場合、2項と3項にチェックが入っていることを示す
	}
	//  ラジオボタンの場合の解答を示す文字列を作成する
	//
	//  チェックされていると、選択肢の何番目だったかが１オリジンの数でhtbにセットされている．
	//  例えば、htb の中で、"ERB(1)" == "2" とセットされている
	//　この場合、選択肢の数が３個であれば、 "0,1,0" という文字列が解答を示す文字列である
	//
	String	getRB(Hashtable htb,String key,int	n){
		//
		String	val		= Exam.strHashZERO(htb, key);	// "1","2",･･･ のどれか（チェックされてないと　"0"）
		int		order	= Integer.parseInt(val) - 1;	// 0,1,･･･ のどれか（チェックされてないと -1 )
		//
		StringBuffer	xbuf	= new StringBuffer(50);
		boolean			comma	= false;
		for(int	i=0; i<n; i++){
			if(comma)	xbuf.append(",");
			if(i==order){
				xbuf.append("1");
			}else{
				xbuf.append("0");
			}
			comma = true;
		}
		return	xbuf.toString();	// "0,1,1" のようなCSV．この場合、2項と3項にチェックが入っていることを示す
	}
	//
	// name で項目を検索する
	//
	public	cERB	get(String name0){
		//
		int size = items.size();
		for(int	k=0; k<size; k++){
			String	name1 = get(k).name();
			if(name0.equals(name1)) 	return	get(k);
		}
		return	null;
	}
	// ｋ番目の項目を得る（ｋは０オリジン）
	public	cERB	get(int k){
		if( !OK(k) ){
			DBG.println(" calss EmbededRadioButtons #get : 添え字の値が不正  k=" + k);
			return	null;
		}
		cERB	a = (cERB) (items.get(k));
		return	a;
	}
	// ｋ番目の項目はチェックボックスタイプか
	public	boolean	isCheckbox(int k){
		cERB	a = (cERB) (items.get(k));
		return	a.isCheckbox();
	}	
	// ｋ番目の項目の第ｊ番目の選択表示項目を得る（ｋ、ｊは０オリジン）
	public	String	dispOf(int k,int j){
		if( !OK(k) ){
			DBG.println(" calss EmbededRadioButtons #dispOf : 添え字の値が不正  k=" + k);
			return	null;
		}
		cERB	a = (cERB) (items.get(k));
		return	a.dispOf(j);
	}
	// ｋ番目の項目の選択表示項目の数を得る（ｋ）
	public	int	getAnsSize(int k){
		if( !OK(k) ){
			DBG.println(" calss EmbededWordLists #getAnsSize : 添え字の値が不正  k=" + k);
			return	0;
		}
		cERB	a = (cERB) (items.get(k));
		return	a.size();
	}
	// ｋ番目の問題のｍ番目の項目は正解かどうか
	public	boolean	isCorrectAns(int k,int m){
		cERB	a = (cERB) (items.get(k));
		if( ((String)a.answer(m)).equals("1") )	return	true;
		return false;
	}
	//
	// 問題項目数
	public	int		size()		{ return items.size();  }
	public	int		allCount()	{ return items.size();  }
	//
	// キーを得る
	public	String	key()					{	return	key;	}
	//
	// 総得点
	public	int		points()				{   return points;  }
	public	void	setPoints(int	p)		{	points	= p;	}
	public	void	clsPoints()				{	points	= 0;	}
	
	//
	// 問題項目の追加
	public void	add(String _group,String _tkn,int _assignedPoint,int fontLevel) {
		//
		// まだひとつも項目を生成していない
		if(groupNow==null){
			groupNow	= _group;
			cERB item	= new cERB(_group,  _tkn, _assignedPoint,fontLevel);	// 項目を生成して追加しておく
			items.add(item);
		//
		// 新しい項目
		}else if( !(groupNow.equals(_group)) ){
			//
			groupNow	= _group;
			cERB item	= new cERB(_group,  _tkn, _assignedPoint,fontLevel);
			items.add(item);
		//
		// 項目の新しい選択肢
		}else{
			cERB item	= (cERB)items.get( size() - 1 );	// 最後の問題項目に追加
			item.add(_tkn);
		}
		
	}
	// 全項目を採点する
	public	int	grades(EmbededRadioButtons ansS){
		int		points	= 0;
		for(int k=0; k<size(); k++){
			cERB	a0 	= ansS.get(k);
			cERB	a1 	= get(k);
			int	pt	= a1.grades(a0);
			points		+= pt;
		}
		return	points;
	}
	// 全項目に同じ配点 pt をセットする
	public	void	setApAll(int pt){
		for(int k=0; k<size(); k++){
			setAp(k,pt);
		}
	}
	// 等しいか
	public	boolean	equals(EmbededRadioButtons obj){
		if(key.equals(obj.key))		return true;
		return	false;
	}

	// ｋ番目の項目の得点を得る
	public	int	getPoints(int k){
		cERB	a = get(k);
		if(a==null){
			DBG.println(" calss EmbededRadioButtons #getPoints : 添え字の値が不正  k=" + k);
			return	-1;
		}
		return	a.getPoints();		
	}
	// ｋ番目の項目に配点 pt をセットする
	public	boolean	setAp(int k,int pt){
		cERB	a = get(k);
		if(a==null){
			DBG.println(" calss EmbededRadioButtons #setAp : 添え字の値が不正  k=" + k);
			return	false;
		}
		a.setAp(pt);
		return	true;
	}
	// 添え字は適正な範囲内か
	public	boolean	OK(int k){
		if( k >= size())	return	false;
		if( k < 0 )			return	false;
		return	true;
	}	
	// 内容を出力する
	public void	print(){
		DBG.println("-- class EmbededRadioButtons");
		DBG.println("key : " + key);
		DBG.println("items :");
		int	size = items.size();
		for(int i=0; i<size; i++){
			DBG.println("  " + String.valueOf(i+1) + ":" + ((cERB)get(i)).toString() );
		}
		DBG.println("points : " + points);
	}

}