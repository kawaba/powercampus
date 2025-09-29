package epml;
import	epml.tools.*;
/**
 * 
 * 解答選択肢から解答を選んで答えるクラス．<BR>
 *
 * クラス変数の説明<PRE>
 * 
 *	String	name;           一意名　"ENL(3)" など
 *	String	seqNum;         出現番号	"3" など
 *	String	ans;            解答語（表示にしか使われない）
 *	String	userAnsNumber;  この項目について受験者が解答した解答番号（ansNumber に対応する）
 *	String	refName;        参照名＝他の項目と同じ答えの時、その項目の name ．このとき、ans は空白になる．
 *	String	ansNumber;      解答（の選択肢番号）［採点ではこちらが正解＝問題原文から作成した正しい解答番号］
 *	int		ap;             配点
 *	int		points;         得点
 *	int		fontLevel;      相対的なフォントの大きさ
 * </PRE>
 *  
 */
public class cENL extends Object{

	String	name;			// 一意名　	"ENL(3)" など
	String	seqNum;			// 出現番号	"3" など
	String	ans;			// 具体的な解答語（表示にしか使われない）
	String	refName;		// 参照名＝他の項目と同じ答えの時、その項目の name ．このとき、ans は空白になる．
	String	ansNumber;		// 解答（の選択肢番号）［採点ではこちらが正解＝問題原文から作成した正しい解答番号］
	String	userAnsNumber;	// この項目について受験者が解答した解答番号（ansNumber に対応する）
	String	eval;			// 正誤を表すマーク．採点時に付与するのでそれまでは空白．正解="t" 不正解="f"
	int	ap;				// 配点
	int	points;			// 得点
	int	fontLevel;		// フォントの相対的な大きさ
    /**
     * 
     * @param _name
     * @param _seqNum
     * @param _ans
     * @param _ap
     * @param _fontLevel
     */
	public	cENL(String _name,String _seqNum,String _ans,int _ap,int _fontLevel){
		if(DBG.fa) DBG.println("class cENL #cENL() : コンストラクタ の先頭です");
		name			= _name;
		seqNum			= _seqNum;
		ap				= _ap;
		userAnsNumber	= "";
		eval			= "";
		refName			= "";
		//
		ansNumber		= "";
		points			= 0;
		fontLevel		= _fontLevel;
		//
		// ユーザーが解答するとハッシュからその解答番号を取りだし、 鉄鋼石~3 のように正解語の後に付け足す処理を行っている
		// cENL を生成する時はこれを分離して番号はuserAnsNumber に格納する．
		// 採点では ansNumber と userAnsNumber を比較する事になる．
		// また、ユーザーに解答を示す時は、userAnsNumber から解答語を検索して表示する
		//
		// 入力された文字は以下のように正規化する
		// 		両端の空白を取り、中間の全角空白を半角空白に変換
		// 		全ての文字を大文字に変換
		// 		全角英数記号を半角に変更する
		// 
		Csv	cs	= new Csv(_ans,"~");			// ~をデリミッタとして解答者の解答番号を分離する
		Regularizer	rg	= new Regularizer();	// 正規化クラス
		//
		// 解答語の構造
		//   arpanet~9~t  --->  正解語は arapanet 学生の解答番号は9 答えとしては正解(t)
		//
		ans = rg.toGeneralSet(cs.get(0));
		if(cs.size() >1) userAnsNumber	= rg.toGeneralSet(cs.get(1));	// システムで付加したユーザーの解答番号
		if(cs.size() >2) eval 			= cs.get(2);					// 採点済みの学生の解答の場合、"t"か"f"がセットされている
		//
	}
	//
	// 解答番号関係
	//
	/**	参照名を得る */
	public	String	refName()				{   return refName; }		
	/**
	 * 参照名を設定する
	 * @param s   同じ名前の解答語を持つオブジェクトの一意名
	 */
	public	void	setRefName(String s)	{   refName	= s; 	} 
	/** 解答語を得る */
	public	String	ans()					{	return ans;		}
	/** ユーザーの解答番号を得る　*/
	public	String	userAnsNumber()			{	return userAnsNumber;}	
	public	String	getStAnsNum()			{	return userAnsNumber();}	
	/** 解答語をクリアする */
	public	void	clearAns()				{	ans	= "";		} 
	/** このオブジェクトの一意名を得る */
	public	String	name()					{ 	return name; 	}
	/** このオブジェクトの解答番号を得る */
	public	String	getAnsNumber()			{ 	return ansNumber; }
	/**
	 * 解答番号をセットする
	 * @param k    解答番号
	 */
	public	void	setAnsNumber(String k)	{	ansNumber = k;    }
	/** 解答番号をクリアーする*/
	public	void	clsAnsNumber()			{ 	ansNumber ="";	  }
	//
	//
	// 要素データ
	//
	/** 並び順の番号を得る */
	public	String	seqNum()				{ 	return seqNum; 	}
	/** フォントレベルを得る */
	public	int	fontLevel()				{	return fontLevel;	}
	/**
	 * 正誤をセットする
	 * @param e    正誤文字（t/f）
	 */
	public	void	setEval(String e)		{	eval	= e;	}
	/** 正誤を得る */
	public	String	getEval()				{	return	eval;	}
	/**
	 * 得点をセットする
	 * @param pt   得点
	 */
	public	void	setPoints(int	pt)		{	points	= pt;	}
	/** 得点を得る */
	public	int	getPoints()				{   return points;  }
	/** 得点を消去する */
	public	void	clsPoints()				{	points	= 0;	}
	/** 配点を得る */
	public	int	getAp()					{   return ap;  	}
	/**
	 * 配点をセットする
	 * @param pt　配点
	 */
	public	void	setAp(int	pt)			{	ap	= pt;	}
	/** 配点をクリアする */
	public	void	clsAp()					{	ap	= 0;	}
	/**
	 * 解答を採点し正誤を記録する<br>
	 * <br>
	 * @param ansS  解答のcENLオブジェクト
	 * @return      ansSオブジェクトに正誤を記録する．得点を返す．
	 */
	public	int	grades(cENL ansS){
		points	= 0;
		ansS.setEval("f");
		if( equals(ansS) ){
			points	= ap; 
			ansS.setEval("t");		
		}
		return	points;	// 正解オブジェクトから取る
	}
	/**
	 * 正解かどうか比較する<br>
	 * 正解は本オブジェクトの方であることに注意する．<br>
	 * @param obj    解答のcENLオブジェクト
	 * @return       正解ならtrue 不正解ならfalse を返す
	 */	
	public	boolean	equals(cENL obj){
		if(ansNumber.equals(obj.userAnsNumber))		return	true;
		return	false;
	}
	/**
	 * 同じ問題かどうか、名前を比較する
	 * @param obj      対象オブジェクト
	 * @return         同じならtrue 違うならfalse を返す
	 */
	public boolean	isSame(cENL obj){
		if(name.equals(obj.name))	return true;
		return	false;
	}
	/**
	 * オブジェクトのフィールドデータを表示する
	 */
	public String	toString(){
		return name + "/(sq)" + seqNum + "/" + ans + "/(選択肢)" + ansNumber + "/(ap)" + ap + "/(得点)" + points + "/(font)" + fontLevel;
	}
}