
/*
	（文中に埋め込まれたドロップダウンリストから
	正しいと思うものを選んで答える形式の問題）の要素クラス
	
	先頭の要素として正解を指定するが、cEWL 内で乱数によって
	攪拌し、何番目が正解かを変数 CorrectNumber に記憶しておく
*/
package epml;
import	epml.tools.*;
import java.util.*;

public class cEWL extends Object{
	String	name;			// 一意名　	"EWL(3)" など
	String	seqNum;			// 出現番号	"3" など
	Vector	ans;			// 解答語
	String	correctNumber;	// 正解番号(１オリジン）
	//
	int		size;			// 選択肢数
	int		ap;				// 配点
	int		points;			// 得点
	int		fontLevel;		// フォントレベル
	String		eval;			// 学生の解答データのとき、正誤を示す文字（t/f）
	//
	public	cEWL(String _name,String _seqNum,String _ans,int _ap,int _fontLevel) throws EpmlTokenException {
		if(DBG.fa){
			DBG.println("class cEWL #cEWL() : コンストラクタ の先頭です");
			DBG.println("           _name   = " + _name);
			DBG.println("           _seqNum = " + _seqNum);
			DBG.println("           _ans    = " + _ans);
			DBG.println("           _ap     = " + _ap);
		}	
		//
		name		= 	_name;
		seqNum		= 	_seqNum;
		ap			= 	_ap;
		ans			= 	new Vector(10,10);	// 複数正解対応
		points		= 	0;
		fontLevel	= 	_fontLevel;
		//
		// 正誤文字があれば分離して記憶しておく
		String	ansStr	=	_ans;	// 正解番号（１オリジン）とその正誤（正誤はない場合もある）
		eval			=	"";
		Csv		cs		=	new Csv(_ans,"~");
		if(cs.size()==2){
			ansStr	=	cs.get(0);
			eval	=	cs.get(1);
		}
		//
		// 選択肢は複数
		// 複数でなければ Token()で ENL と解釈されてしまうので必ず複数個あるはず
		correctNumber 			= "-1";
		StringTokenizer	st		= new StringTokenizer(ansStr,",，、|｜");
		int	k=0;
		while(st.hasMoreTokens()){
            String tk 	= (st.nextToken()).trim();
			if(tk.length()>0){
				//
				char	ch	= tk.charAt(0);
				if((ch=='*')||(ch=='＊')){
				 	if(tk.length()>1){
						tk = tk.substring(1);
						correctNumber= String.valueOf(k + 1);// 正解番号（１オリジン）を記憶する
						ans.add(tk);
						k++;
					}
				}else{
					ans.add(tk);
					k++;
				}
			}
		}
		if(correctNumber == "-1"){
			throw new EpmlTokenException("ドロップダウンリストでどれが正解か指定がありません．");// 例外発生
		}
		if(ans.size()<2){
			throw new EpmlTokenException("ドロップダウンリストで有効な正解が１個以下しかありません．");// 例外発生
		}
		size = ans.size();
		//if(DBG._epml) DBG.println(toString());
	}
	/**
	 * 正誤文字をセットする
	 * 
	 * @param e	セットする文字列 "t"または"f"
	 */
	public	void	setEval(String e){
		eval	=	new String(e);
	}
	/**
	 * 正誤文字を得る
	 * 
	 * @return		正誤文字 "t", "f", "" のいづれか
	 */
	public	String	getEval(){
		return		eval;
	}
	//
	// 要素データ
	public	String	name()					{ 	return name; 	}
	public	String	seqNum()				{ 	return seqNum; 	}
	public	String	ans(int k)				{	return getItem(k); }
	public	int	fontLevel()				{	return fontLevel;	}
	//
	public	int	sizeOfItems()			{	return size;}
	
	// 正解番号
	public	String	getCorrectNum()				{   return correctNumber;  	}	// (１オリジン）
	public	void	setCorrectNum(String num)	{	correctNumber	= num;	}	// (１オリジン）
	public	void	setAnsNumber(String num)	{	correctNumber	= num;	}	// (１オリジン）
	public	void	clsCorrectNum()				{	correctNumber	= "0";	}	// (１オリジン）
	// 得点
	public	void	setPoints(int	pt)		{	points	= pt;	}
	public	int	getPoints()				{   return points;  }
	public	void	clsPoints()				{	points	= 0;	}
	// 配点
	public	int	getAp()					{   return ap;  	}
	public	void	setAp(int	pt)			{	ap	= pt;	}
	public	void	clsAp()					{	ap	= 0;	}
	//
	// ｋ番目の選択肢を得る
	public	String	getItem(int k){
		String	_ans;
		if( OK(k) ){
			_ans = (String) (ans.get(k));
		}else{
			DBG.println("class cEWL#getAnswer() : 添え字の値が不正である");
			_ans = null;
		}
		return	_ans;
	}
	// 全ての解答のＣＳＶ表現を得る
	public	String	getAll(){
		StringBuffer bf = new StringBuffer(1024);
		boolean flag	= false;
		for(int i=0; i<size; i++){
			if(flag) bf.append(",");
			bf.append(getItem(i) );
			flag	= true;
		}
		return bf.toString();
	}
	// 正解の番号を得る(１オリジン）
	public String	correctNum()	{ return	correctNumber; }	// (１オリジン）
	/**
	 * 採点する
	 * 
	 * 本オブジェクトが問題オブジェクト、すなわち正解で、引数は採点されるオブジェクトである
	 * 
	 * @param ansS		採点される学生の解答オブジェクト
	 * @return			得点を返す．得点は配点から得る．
	 * 
	 */
	public	int	grades(cEWL ansS){
		//
		if( correctNumber.equals(ansS.correctNumber)){	// (１オリジン）
			ansS.setEval("t");
			points	= 	ap;								// 本オブジェクトから取る
		}else{
			ansS.setEval("f");
			points	=	0;
		}
		return	points;
	}
	// 添え字は適正な範囲内か
	public	boolean	OK(int k){
		int sz = ans.size();
		if( k >= sz)	return	false;
		if( k < 0 )		return	false;
		return	true;
	}
	// 同じ問題か
	public boolean	isSame(cEWL obj){
		if(name.equals(obj.name))	return true;
		return	false;
	}
	public String	toString(){
		return name + "/(sq)" + seqNum + "/" + getAll() + "/(正解番）" + correctNumber + "/(size)" + size + "/(ap)" + ap + "/(得点)" + points + "/(font)" + fontLevel;
	}
}