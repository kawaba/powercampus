/*
     一覧表を見て、正しいと思う番号をテキストボックスに
	 書き込んで解答する問題のクラス

*/
package epml;
import	epml.tools.*;
//import java.io.*;
//import java.text.*;
import java.util.*;
//
public class EnlObject extends Object {
	
	Vector	items;
	int	size;
	int	points;
	Vector	dummyAnswers;		// ダミー選択肢
	Vector	dummyNumbers;		// ダミー選択肢の解答番号
	int	dummyCounts;
	//
	int	wordCount;			// 重複を除いた表示用項目数
	Vector	forDisp;			// 表示用
	String  permutation;		// 語　順＝ダミーを含む解答語の並び順 　例："3-2-4-1"
	Vector	perm;				// 並び順を入れる
	//
	public	EnlObject(){
		if(DBG.fa) DBG.println("class EnlObject #EnlObject() : ■コンストラクタ の先頭です");
		size	= 0;
		items	= new Vector(30,10);
		points	= 0;
		dummyAnswers	= new Vector(10,10);
		dummyNumbers	= new Vector(10,10);
		dummyCounts		= 0;
		//
		forDisp			= new Vector(30,10);
		permutation		= "";
		perm			= new Vector(50,10);
	}
	// 語順をセットする（古い語順は破棄される）
	public	void setPermutation(String pm){
		if(DBG.fa) DBG.println("class EnlObject #setPermutation() : ■語順をセットする（古い語順は破棄される） の先頭です");
		//
		permutation	= pm;
		if(perm.size() !=0) perm.clear();
		Csv	cs	= new	Csv(pm,"-");
		int	n	= cs.size();
		for(int	k=0; k<n; k++){
			perm.add(cs.get(k));
		}
	}
	// 語順はセットされているか（セットされている時、true)
	public	boolean	isSetPermutation(){
		if(DBG.fa) DBG.println("class EnlObject #isSetPermutation() : ■語順はセットされているか（セットされている時、true) の先頭です");
		
		if(perm.size()==0)	return false;
		return	true;
	}
	//
	// name で項目を検索する	
	public	cENL	get(String name0){	// ENL(1,3) のような２次元名
		//if(DBG.fa) DBG.println("class EnlObject #get(String name0) : ■name で項目を検索する の先頭です");
		//
		for(int	k=0; k<size; k++){
			String	name1 = get(k).name();
			if(name0.equals(name1)) 	return	get(k);
		}
		return	null;
	}
	//
	// ｋ番目の項目を得る
	public	cENL	get(int k){
		//if(DBG.fa) DBG.println("class EnlObject #get(int k) : ■ｋ番目の項目を得る の先頭です");
		//
		if( !OK(k) ){
			DBG.println(" calss EnlObject #get : 添え字の値が不正  k=" + k);
			return	null;
		}
		cENL	a = (cENL) (items.get(k));
		return	a;
	}
	// 表示用選択肢 の個数を得る
	int	dispSize()	{ return forDisp.size();}
	//
	// ｋ番目の表示用選択肢を得る（番号付き）
	String dispItem(int k)	{
		if(DBG.fa) DBG.println("■ ⇒表示用項目インデックス（max:" + forDisp.size() + ") " + k);
		return (String)forDisp.get(k);
	}
	// ｋ番目の表示用選択肢を得る（番号なし）
	String dispItemString(int k)	{
		if(DBG.fa) DBG.println("■ ⇒表示用項目インデックス（max:" + forDisp.size() + ") " + k);
		Csv	cs = new Csv( (String)forDisp.get(k),")" ); // 解答は (1)鉄鋼石 のように()で囲まれた数字が付いている
		return cs.get(1);								// 番号を含まない部分
	}	
	//
	// 総得点
	public	int	points()				{   return points;  }
	public	void	setPoints(int	p)		{	points	= p;	}
	public	void	clsPoints()				{	points	= 0;	}
	//
	// 項目を追加する
	public	int	add(String name,String seqNum,String ans,int pt,int fontLevel){
		if(DBG.fa) DBG.println("class EnlObject #add() : ■項目を追加する の先頭です");
		//
		cENL	item	= new cENL(name,seqNum,ans,pt,fontLevel);
		// 重複解答語を避ける処理（解答語をクリアしてどの項目と同じかその項目名を記録しておく）
		for(int k=0; k<size; k++){
			String	comp	= ans(k);			// ｋ番目の項目の解答語
			if(ans.equals(comp)){
				item.clearAns();				// 解答語をクリアして
				item.setRefName(getName(k));	// ｋ番目の項目のname を参照名として記録する
				break;
			}
		}
		items.add(item);
		size	= items.size();
		return	size;
	}
	// 全項目を採点する
	public	int	grades(EnlObject ansS){
		if(DBG.fa) DBG.println("class EnlObject #grades() : ■全項目を採点する の先頭です");
		//
		int		points	= 0;
		for(int k=0; k<size; k++){
			cENL		a0 	= ansS.get(k);
			cENL		a1 	= get(k);
			int			pt	= a1.grades(a0);
			points		+= pt;
		}
		return	points;
	}
	// 全項目に同じ配点 pt をセットする
	public	void	setApAll(int pt){
		if(DBG.fa) DBG.println("class EnlObject #setApAll() :  ■全項目に同じ配点 pt をセットする の先頭です");
		//
		for(int k=0; k<size; k++){
			setAp(k,pt);
		}
	}
	// 全項目に解答番号をセットする（ダミーの選択肢にも）
	// EPML 原文を全てパースし終わったタイミングでこの処理が呼ばれる（参照: doEPML() )
	// EPML 原文中に語順の指示があればそれを使い、なければ新規に語順を作る
	public void	setAnsNumberAll(String orderStr){
		if(DBG.fa) DBG.println("class EnlObject #setAnsNumberAll() :  ■-全項目に解答番号をセットする（ダミーの選択肢にも） の先頭です");
		//
		// 最初に語数をカウントする．語順リストが指定してあってもプレビュー時の編集で実際の語数が増減している可能性がある．
		// 重複を含めた選択語全てに語順を割り当てるためにRandomPermutation を使うが、この要素を総語数分作成しなくてはならないが、
		// 食い違いがあると破綻するので、その場合は新規に語順を生成し直す．
		// 
		int	checkSize	= size + dummyCounts;		// 総語数（dummyCounts はダミー選択語の数）
		int	check		= sizeOfList(orderStr);		// 語順リストにある語数
		//
		//DBG.println("■ checkSize   = " + checkSize);
		//DBG.println("■ check 	  = " + check);
		if( (orderStr.length()==0)||(check != checkSize)){	// 新規に番号を生成するとき
			setNewAnsNumberAll();
			return;
		}
		//
		// 以下は語順リストに従って並べ替えするケース
		// 
		setPermutation(orderStr);	// perm に語順をセットする
		//
		int		assignSize	= size;						// 真の解答選択肢の数
		int		dummySize	= dummyCounts;				// ダミー選択肢の数
		int		mapSize		= assignSize + dummySize;	// マップの大きさ＝両者の合計
		
		// 選択肢
		for(int k=0; k<assignSize; k++){
			setAnsNumber(k,(String)perm.get(k));		// ｋ番目の解答文字列の選択肢番号をセット(１オリジン）
		}
		// ダミー選択肢
		for(int m=0; m<dummySize; m++){
			dummyNumbers.set(m,(String)perm.get(assignSize+m));	// assignSize+m 番目の解答文字列の選択肢番号をセット(１オリジン)
		}
		mkDispArray();
	}
	public	int	sizeOfList(String str){
		//
		if((str==null) || (str.length()==0)) return 0;
		// "-" で幾つに分けられるか調べて個数を返す
		Csv	cs	= new Csv(str,"-");
		return	cs.size();
	}
	// 新規
	public void	setNewAnsNumberAll(){
		if(DBG.fa) DBG.println("class EnlObject #setAnsNumberAll() :  ■「新規に」全項目に解答番号をセットする（ダミーの選択肢にも） の先頭です");
		//
		// 重複した解答語があるといけないので、登録時にチェックして、重複する場合にはどの問題項目と重複したかを覚えておき
		// 解答語自体は "" にしてある．そこで全ての解答語について、"" でないものをカウントすると重複しない語数が分かる．
		//
		int	n	= 0;
		for(int k=0; k<size; k++){									// size は解答語の数
			if(ans(k).length() != 0)	n++;						// n は重複しない解答語の数
		}
		int		mapSize		= n + dummyCounts;						// dummyCounts はダミー選択語の数．マップの大きさ＝両者の合計
		if(DBG.fa) DBG.println("           --- mapSize = " + mapSize);
		RandomPermutation rp	= new RandomPermutation(mapSize);	// 重複のない解答語の数でセット
		//
		if(perm.size()!=0) perm.clear();							// 語順をクリアしておく
		// 選択肢
		for(int k=0; k<size; k++){
			String	ans	= ans(k);	// 解答語
			if(ans.length()!=0){	// 解答語が "" であるのは同じ解答語を持つ項目があるということなので除外する
				int		pos  = rp.nextNumber();
				if(DBG.fa) DBG.println("           --- rp.nextNumber() = " + pos);
				perm.add( String.valueOf(pos) );		// 語順に登録
				setAnsNumber(k,String.valueOf(pos));	// ｋ番目の解答文字列は選択肢のPOS番(１オリジン）
			}else{
				cENL	obj	= get(refName(k));		// 同じ解答語をもつオブジェクトを項目キー(name)から検索する
				String	num	= obj.getAnsNumber();	// obj の解答番号を得て
				perm.add( num );					// 語順に重複登録
				setAnsNumber(k,num);				// 同じものをセットしておく
			}
		}
		// ダミー選択肢
		for(int m=0; m<dummyCounts; m++){
			int		pos  = rp.nextNumber();
			if(DBG.fa) DBG.println("           --- rp.nextNumber() = " + pos);
			perm.add( String.valueOf(pos) );			// 語順に登録
			dummyNumbers.set(m,String.valueOf(pos));	// m番目の解答文字列は選択肢のPOS番(１オリジン)
			
		}
		mkDispArray();
		//
		// 語順の文字列を生成する
		boolean			flag	= false;
		StringBuffer	bx		= new StringBuffer(100);
		for(int k=0; k<perm.size(); k++){
			if(flag)	bx.append("-");
			bx.append( (String)perm.get(k) );
			flag = true;
		}
		permutation = bx.toString();
		//
		// ★ 新規に作成したので、Exam では EPML原文に語順を追記しなくてはならない
	}
	//
	// 表示用の選択肢配列を作成する（ソートしておく）
	void mkDispArray(){
		if(DBG.fa) DBG.println("class EnlObject #mkDispArray() :  ■表示用の選択肢配列を作成する（ソートしておく） の先頭です");
		//
		int		sz		= size + dummyCounts;	// 総解答語数
		//
		Vector	tbl	= new Vector(50,10);
		//
		int	order	= 0;
		for(int i=0; i<size; i++){
			if(ans(i).length() > 0){//	重複解答語をもつ項目の解答語は "" になっている
				tbl.add( get00type(getAnsNumber(i))   + "$" + getAnsNumber(i) + "$" + get(i).ans() );
				order++;
			}
		}
		for(int i=0; i<dummyCounts; i++){
			tbl.add( get00type(getDummyNumber(i)) + "$" + getDummyNumber(i) + "$" + getDummyAnswer(i) );
		}
		wordCount	= order + dummyCounts;
		if(DBG.fa){
			DBG.println("■■■class EnlObject #mkDispArray() : wordCount = "+ wordCount);
			for(int	k=0; k<wordCount; k++){
				DBG.println("      " + k + ")" + (String)tbl.get(k));
			}
			
		}
		String	[]temp	= new String[wordCount];
		for(int	k=0; k<wordCount; k++){
			temp[k] = (String)tbl.get(k);
		}
		if(wordCount > 1) { Arrays.sort(temp);	} // ソート(n==1 ならソートしない)
		//
		//
		for(int i=0; i<wordCount; i++){
			Csv 	cs		= new Csv(temp[i],"$");
			String	word	= "(" + cs.get(1) + ")" + cs.get(2);
			forDisp.add(word);
		}
	}
	//
	// 全項目の解答番号をクリアする（ダミーの選択肢も）
	public	void	clsAnsNumberAll(){
		for(int k=0; k<size; k++){
			clsAnsNumber(k);
		}
		dummyNumbers.removeAllElements();
	}
	// ダミー項目に選択肢を追加する
	public	void addDummyAnswer(String str){
		if(DBG.fa) DBG.println("class EnlObject #addDummyAnswer() :  ■ダミー項目に選択肢を追加する の先頭です");
		//
		Csv	cs	= new Csv(str,",|，、｜");
		int	n	= cs.size();
		int	sz	= 0;
		for(int i=0; i<n; i++){
			if(!cs.isEmptyElement(i)){
				dummyAnswers.add(cs.get(i));
				dummyNumbers.add(" ");
				sz++;
			}
		}
		dummyCounts		= sz;
	}
	// 項目数（ダミーを含まない）
	public int	size() { return size;}
	//
	// ダミー項目の個数を得る
	public int	getDummyCounts() { return dummyCounts;}
	//
	// ｋ番目のダミー項目の選択肢を得る
	public	String	getDummyAnswer(int k){
		return 	(String)(dummyAnswers.get(k));
	}
	// ｋ番目のダミー項目の解答番号を得る
	public	String	getDummyNumber(int k){
		return 	(String)(dummyNumbers.get(k));
	}
	// ダミーを含む総項目数
	public int	allCount()	{
		return	size + dummyCounts; 
	}
	//
	// 選択語の並び順を表す文字列を返す
	public	String	getPermutation()	{	return permutation; }
	//
	//-----------------------------------------------------------------------------------------------
	// ｋ番目の項目の参照名を得る
	public	String	refName(int k){
		cENL	a = get(k);
		if(a==null){
			DBG.println(" calss EnlObject #refName : 添え字の値が不正  k=" + k);
			return null;
		}
		return	a.refName();		
	}
	// ｋ番目の項目の参照名を設定する
	public	void	setRefName(int k,String s){
		cENL	a = get(k);
		if(a==null){
			DBG.println(" calss EnlObject #setRefName : 添え字の値が不正  k=" + k);
		}
		a.setRefName(s);		
	}
	// ｋ番目の項目の解答語を得る
	public	String	ans(int k){
		cENL	a = get(k);
		if(a==null){
			DBG.println(" calss EnlObject #ans : 添え字の値が不正  k=" + k);
			return	null;
		}
		return	a.ans();
	}
	// ｋ番目の項目の解答語をクリアする
	public	void	clearAns(int k){
		cENL	a = get(k);
		if(a==null){
			DBG.println(" calss EnlObject #clearAns : 添え字の値が不正  k=" + k);
		}
		a.clearAns();		
	}
	// ｋ番目の項目の name を得る
		public	String	getName(int k){
		cENL	a = get(k);
		if(a==null){
			DBG.println(" calss EnlObject #getName : 添え字の値が不正  k=" + k);
			return	null;
		}
		return	a.name();		
	}
	// ｋ番目の項目の正誤を得る
	public String	getEval(int k){
		cENL	a = get(k);
		if(a==null){
			DBG.println(" calss EnlObject #getEval : 添え字の値が不正  k=" + k);
			return	"";
		}
		return	a.getEval();
	}
	// ｋ番目の項目の学生の解答した番号を得る
	public	String	getStAnsNum(int k){
		cENL	a = get(k);
		if(a==null){
			DBG.println(" calss EnlObject #getAnsNumber : 添え字の値が不正  k=" + k);
			return	"";
		}
		return	a.getStAnsNum();
	}

	// ｋ番目の項目の解答番号を得る
	public	String	getAnsNumber(int k){
		cENL	a = get(k);
		if(a==null){
			DBG.println(" calss EnlObject #getAnsNumber : 添え字の値が不正  k=" + k);
			return	"";
		}
		return	a.getAnsNumber();
	}
	// ｋ番目の項目に解答番号 n をセットする
	public	boolean	setAnsNumber(int k,String n){
		cENL	a = get(k);
		if(a==null){
			DBG.println(" calss EnlObject #setAnsNumber : 添え字の値が不正  k=" + k);
			return	false;
		}
		a.setAnsNumber(n);
		return	true;
	}

	// ｋ番目の項目の得点を得る
	public	int	getPoints(int k){
		cENL	a = get(k);
		if(a==null){
			DBG.println(" calss EnlObject #getPoints : 添え字の値が不正  k=" + k);
			return	-1;
		}
		return	a.getPoints();		
	}
	// ｋ番目の項目に配点 pt をセットする
	public	boolean	setAp(int k,int pt){
		cENL	a = get(k);
		if(a==null){
			DBG.println(" calss EnlObject #setAp : 添え字の値が不正  k=" + k);
			return	false;
		}
		a.setAp(pt);
		return	true;
	}
	// ｋ番目の項目の解答番号をクリアする
	public	boolean	clsAnsNumber(int k){
		cENL	a = get(k);
		if(a==null){
			DBG.println(" calss EnlObject #clsAnsNumber : 添え字の値が不正  k=" + k);
			return	false;
		}
		a.clsAnsNumber();
		return	true;
	}
	// 添え字は適正な範囲内か
	public	boolean	OK(int k){
		if( k >= size)	return	false;
		if( k < 0 )		return	false;
		return	true;
	}
	// 先頭を０で埋めて 2 桁の整数にする
    String get00type(String dt){
        if((dt == null)||(dt.length() == 0)) return "00";
        int     pos     = dt.length();
        String  pattern = "00" + dt;
        return  pattern.substring(pos);
    }
	// 内容を出力する
	public void	print(){
		if(DBG.fa) DBG.println("class EnlObject #print() : ■内容を出力する の先頭です");
		//
		int	maxlen	= 80;
		DBG.println("-- class EnlObject");
		DBG.println("items :");
		for(int i=0; i<size; i++){
			DBG.println("  " + String.valueOf(i+1) + ":" + ((cENL)get(i)).toString() );
		}
		DBG.println("size : " + size);
		DBG.println("points : " + points);
		int n = dummyAnswers.size();
		DBG.println("dummys :"+ n);
		for(int i=0; i<n; i++){
			DBG.println("  " + "(" + getDummyNumber(i) + ") " + getDummyAnswer(i));
			
		}
		String		 CR 		= System.getProperty("line.separator");
		StringBuffer dbf		= new StringBuffer(1024);
		int			 dispLEN 	= 0;
		String		 sp			= "          ";
		String		 line		= sp;
		boolean		 flag		= false;
		for(int k=0; k<dispSize(); k++){
			line	= line + dispItem(k) + " ";
			flag	= true;
			if(line.length() > maxlen){
				dbf.append(line + CR);
				line	= sp;
				flag	= false;
			}
		}
		if(flag) dbf.append(line + CR);
		DBG.println(dbf.toString());
		//
	}

}