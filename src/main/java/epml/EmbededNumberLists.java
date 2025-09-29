/*
     一覧表を見て、正しいと思う番号をテキストボックスに
	 書き込んで解答する問題のクラス

*/
package epml;
//import java.io.*;
//import java.text.*;
import java.util.Hashtable;
import java.util.Vector;
import epml.tools.Csv;
import epml.tools.DBG;
//
public class EmbededNumberLists extends Object {
	//
	String	key;		// オブジェクトキー
	Vector	items;		// ひとかたまりの問題であるEnlObject を要素として持つ
	int	size;		// 要素の個数
	int	groupNow;
	//
	public	EmbededNumberLists(){
		if(DBG.fa) DBG.println("class EmbededNumberLists#コンストラクタ の先頭です");
		key			= "";
		size		= 0;
		items		= new Vector(100,10);
		groupNow	= -1;
	}
	public	EmbededNumberLists(String _key){
		if(DBG.fa) DBG.println("class EmbededNumberLists#コンストラクタ2 の先頭です");
		//
		key			= _key;
		size		= 0;
		items		= new Vector(100,10);
		groupNow	= -1;
	}
	/**
	 * 全ての問題項目についてキーとユーザーの解答をハッシュに入れる<br>
	 * 
	 * cENL にかぎり、学生の解答と問題文そのものでは、解答の構成が違う.
	 * 解答語を arpanet, 正解番号を 2 とするとdoEPML() でEPML文から取り出される「正解語」部分は以下のようになる
	 * 
	 * 　問　題　文					:	arpanet
	 * 　学生の解答(採点前)			:	arpanet~2
	 * 　学生の解答(採点後)			:	arpanet~2~t
	 * 　学生の解答(採点前,未解答)	:	arpanet~0
	 * 
	 * 問題文EPML自体を解析して正解表示のために、正解番号を解答番号として取り出そうとしているのかどうかは
	 * cENL の userAnsNumber が空白かどうか調べると判明する．学生の解答であれば、未解答の場合でも"0"がセット
	 * されているからである．
	 * 
	 * @param htb　	キーと解答のハッシュテーブル
	 */
	public	void	setHash(Hashtable htb){
		if(DBG.fa) DBG.println("class EmbededNumberLists #setHash() : 全ての問題項目についてキーとユーザーの解答をハッシュに入れる の先頭です");
		//
		for(int	i=0; i<size; i++){
			int max	= get(i).size();
			for(int	j=0; j<max; j++){
				cENL	obj		= 	get(i,j);
				String	eval	=	obj.getEval();
				// 採点済みかどうか調べ、採点済ならばデータ部分に正誤（t/f）をそうでなければ"-"を付加する
				if((eval==null)||(eval.length()==0)){
					//
					// 学生の解答か、表示用正解作成のための問題文自体かを調べる．
					// 問題文自体では、解答した番号欄は空欄になっている．しかし、
					// 学生の解答では、必ず解答番号がある．未解答の場合でも "0" がセットされている
					String	userAnsNum	=	obj.userAnsNumber();
					//
					// 正解表示のために問題文自体を処理している場合は、正解番号をユーザーの解答番号の代わりにセットする
					if(userAnsNum.length()==0){
						userAnsNum	=	obj.getAnsNumber();
					}
					htb.put(obj.name(),userAnsNum + "~-");							// 例    "ENL(1,2)"  "5~-"
				}else{
					// eval があればこれは学生の解答である
					// userAnsNumber() には必ず値がある
					htb.put(obj.name(),obj.userAnsNumber() + "~" + obj.getEval());			// 例    "ENL(1,2)"  "5~t"
				}
			}
		}
	}
	// 要素数
	public	int	size()	{ return	size;}
	
	// 各要素の持つ項目数の合計
	public	int	allCount(){
		if(DBG.fa) DBG.println("class EmbededNumberLists #allCount() : 各要素の持つ項目数の合計 の先頭です");
		//
		int	cnt	= 0;
		for(int	k=0; k<size; k++){
			cnt		+=	get(k).size();
		}
		if(DBG.fa) DBG.println("           総項目数 = " + cnt);
		return	cnt;
	}
	//  ダミー項目に選択肢を追加する
	public void	addDummyAnswer(int grp,String ans){
		if(DBG.fa) DBG.println("class EmbededNumberLists #addDummyAnswer() : ダミー項目に選択肢を追加する の先頭です");
		if(grp > 0){
			get(grp - 1).addDummyAnswer(ans);	// ゼロオリジンに直して実行
		}else{
			grp				= 1;
			groupNow		= grp;
			EnlObject obj	= new EnlObject();	// グループを生成して追加しておく
			obj.addDummyAnswer(ans);
			items.add(obj);	// グループを登録しておく
			size = items.size();
		}
	}
	// ｋ番目の要素のダミーを含む総項目数
	public int	allCount(int	k)	{
		
		EnlObject obj = 	get(k);
		if(obj==null)	return	0;
		return	obj.allCount();
	}
	// ｋ番目の要素を得る
	EnlObject	get(int	k){
		//if(DBG.fa) DBG.println("class EmbededNumberLists #get() : ｋ番目の要素を得る の先頭です");
		//
		if( !OK(k) ){
			DBG.println(" calss EmbededNumberLists #get : 添え字の値が不正  k =" + k);
			return	null;
		}
		return	(EnlObject)items.get(k);
	}
	// ｋ番目の語群の並び順を得る
	String	getPermutation(int	k){
		if(DBG.fa) DBG.println("class EmbededNumberLists #getPermutation() : ｋ番目の語群の並び順を得る の先頭です");
		//
		if( !OK(k) ){
			DBG.println(" calss EmbededNumberLists #get : 添え字の値が不正  k =" + k);
			return	null;
		}
		return	((EnlObject)items.get(k)).getPermutation();
	}
	//
	// name で項目を検索する
	public	cENL	get(String name0){	// name = ENL(1,3) の形式
		//if(DBG.fa) DBG.println("class EmbededNumberLists #get(String name0) : name で項目を検索する の先頭です");
		//
		Csv cs	= 	new Csv(name0,"(,)");				// ENL  1   3 に分解
		int	grp	=	Integer.parseInt(cs.get(1))	- 1;	// ゼロオリジン
		if( !OK(grp) ){
			DBG.println(" calss EmbededNumberLists #get : 添え字の値が不正  grp =" + grp);
			return	null;
		}
		EnlObject	obj	= get(grp);
		return		obj.get(name0);	// null or obj
	}
	//
	// jグループのｋ番目の項目を得る
	public	cENL	get(int j,int k){	// ゼロオリジンであることに注意
		//if(DBG.fa) DBG.println("class EmbededNumberLists #get(int j,int k) : jグループのｋ番目の項目を得る の先頭です");
		//
		if( !OK(j) ){
			DBG.println(" calss EmbededNumberLists #get : 添え字の値が不正  J =" + j);
			return	null;
		}
		EnlObject	obj	= get(j);
		return	obj.get(k);
	}
	/**
	 * jグループのｋ番目の項目に付いて正解の解答番号を得る
	 * 
	 * @param j     グループ番号
	 * @param k     シーケンス番号
	 * @return      正解の解答番号
	 */
	public	String	getAnsNumber(int j, int k){
		
		EnlObject	obj		= get(j);
		String		temp	= obj.getAnsNumber(k);
		return		temp;
	}
	/**
	 * jグループのｋ番目の項目に付いての学生の解答番号を得る
	 * 
	 * @param j     グループ番号
	 * @param k     シーケンス番号
	 * @return      解答番号
	 */
	public	String	getStAnsNum(int j, int k){
		
	EnlObject	obj		= get(j);
	String		temp	= obj.getStAnsNum(k);
	return		temp;
	}
	/**
	 * jグループのｋ番目の項目の解答に埋め込まれた正・誤判定マーク（t/f）を得る
	 * 
	 * @param j     グループ番号
	 * @param k     シーケンス番号
	 * @return      正誤判定マーク(t/f)を返す
	 */
	public	String	getEval(int j, int k){
		
		EnlObject	obj		= get(j);
		String		temp	= obj.getEval(k);
		return		temp;
	}
	
	// jグループのｋ番目の項目の解答語を得る
	public	String	getAns(int j,int k){	// ゼロオリジンであることに注意
		if(DBG.fa) DBG.println("class EmbededNumberLists #getAns(int j,int k) :  jグループのｋ番目の項目の解答語を得る の先頭です");
		//
		if( !OK(j) ){
			DBG.println(" calss EmbededNumberLists #getAns : 添え字の値が不正  J =" + j);
			return	null;
		}
		EnlObject	obj		= get(j);
		String		temp	= obj.ans(k);
		//
		// 重複解答語のため、解答語が空欄になっている場合は、同じ解答語を持つ項目を得て
		// その項目の解答語を返す．同じ解答語を持つ項目の名前が refName() で得られるので
		// これから、名前で検索して求めることができる
		if(temp.length()==0){
			String keyName 	= obj.refName(k);			// ENL(1,3) のような名前
			temp			= get(keyName).ans();		// get(keyName)は、(1,3) からグループ＝１、シーケンス＝３を得て直接 cENL を求める
		}
		return	temp;
	}
	//
	// jグループのｋ番目の表示用文字列（語群にリストされるもの）を得る
	// jはゼロオリジンであることに注意．ｋもゼロオリジンに直して渡される．
	public	String	dispItemString(int j,int k){	
		if(DBG.fa) DBG.println("class EmbededNumberLists #gdispItemString(int j,int k) :  jグループのｋ番目の表示用文字列（語群にリストされるもの）を得る の先頭です");
		//
		if( !OK(j) ){
			DBG.println(" calss EmbededNumberLists #get : 添え字の値が不正  J =" + j);
			return	null;
		}
		EnlObject	obj	= get(j);
		int	max	= obj.dispSize();	// 語群選択肢の個数＝番号の上限
		if((k<0)||(k>=max))		return "《範囲外番号》";
		//
		return	obj.dispItemString(k);
	}
	// キーを得る
	public	String	key()					{	return	key;	}
	
	/*
	 *  項目を追加する String.valueOf(grpENL),seqNum,tkn,assignedPoint,fontLevel
	 *  
	 *  
	 */
	public	int	add(String name,		// 埋め込みシーケンス文字列 ENL(1)など
					int grp,			// グループ番号
					String seqNum,		// 問題中のシーケンス番号
					String ans,			// トークン（正解語）
					int pt,				// 埋め込まれた配点
					int fontLevel){	// フォントのレベル
		
		if(DBG.fa) DBG.println("class EmbededNumberLists #add() : 項目を追加する の先頭です");
		//
		// まだひとつもグループを生成していない
		if(groupNow < 0){
			groupNow		= grp;
			EnlObject obj	= new EnlObject();	// グループを生成して追加しておく
			obj.add( name, seqNum, ans, pt, fontLevel);
			items.add(obj);	// グループを登録しておく
		//
		// 新しいグループ
		}else if( groupNow != grp ){
			groupNow		= grp;
			EnlObject obj	= new EnlObject();	// グループを生成して追加しておく
			obj.add( name, seqNum, ans, pt, fontLevel);
			items.add(obj);	// グループを登録しておく
		//
		// 既存グループの新しい選択肢
		}else{
			(get(grp -1 )).add( name, seqNum, ans, pt, fontLevel);	// 指定のグループを得て登録する
		}
		size	= items.size();
		return	size;
	}
	// 全項目を採点する
	public	int	grades(EmbededNumberLists ansS){
		if(DBG.fa) DBG.println("class EmbededNumberLists #grades() : 全項目を採点する の先頭です");
		//
		int		points	= 0;
		for(int k=0; k<size; k++){
			
			EnlObject objS	= 	ansS.get(k);		// 学生分
			EnlObject obj	= 	get(k);				// 正解
			points 			+=	obj.grades(objS);
		}
		return	points;
	}
	// 添え字は適正な範囲内か
	public	boolean	OK(int k){
		//if(DBG.fa) DBG.println("class EmbededNumberLists #OK(int k) : 添え字は適正な範囲内か の先頭です");
		//
		if( k >= size)	return	false;
		if( k < 0 )		return	false;
		return	true;
	}
	// 内容を出力する
	public void	print(){
		if(DBG.fa) DBG.println("class EmbededNumberLists #print() : 内容を出力する の先頭です");
		//
		for(int	i=0; i<size; i++){
			get(i).print();
		}
	}
	// 等しいか（キーを比較する）
	public	boolean	equals(EmbededNumberLists stdAns){
		if(DBG.fa) DBG.println("class EmbededNumberLists #grades() : 全項目を採点する の先頭です");
		//
		return	key.equals(stdAns.key);
	}
	// 全項目に解答番号をセットする（ダミーの選択肢にも）
	public	void setAnsNumberAll(Vector order){
		if(DBG.fa) DBG.println("class EmbededNumberLists #setAnsNumberAll() :全項目に解答番号をセットする（ダミーの選択肢にも） の先頭です");
		if(DBG.fa) DBG.println("           項目数 = " + size);
		//
		for(int k=0; k<size; k++){
			String	orderStr = (String)order.get(k);	// "" か "3-2-4-1" のような語順
			get(k).setAnsNumberAll(orderStr);
		}
	}
}