
/*
	（テキストエリアに正しいと思う言葉や文章を記入して
	解答する問題のクラス）の要素クラス
	

*/

package epml;
import java.util.Vector;
import epml.tools.Csv;
import epml.tools.DBG;
import epml.tools.SStringTokenizer;

public class cETA extends Object implements emlVar{
	String	name;		// 一意名　	"ETA(3)" など
	String	seqNum;		// 出現番号	"3" など
	Vector	ans;		// 解答語
	int		ansMax;		// 解答語の数(= ans.size() )
	//
	int		ap;			// 配点
	int		points;		// 得点
	//
	int		nRows;		// 行数
	int		nCols;		// カラム数
	int		fontLevel;	// フォントレベル
	//
	final	int ROWS	= 2;	// default
	final	int COLS	= 25;	// default
	
	public	cETA(String _name,String _seqNum,String _ans,int _ap,int _fontLevel) throws EpmlTokenException{
		if(DBG.fa) DBG.println("class cETA #cETA() : コンストラクタ の先頭です");
		name		= _name;
		seqNum		= _seqNum;
		ap			= _ap;
		ans			= new Vector(30,10);	// 複数正解対応
		points		= 0;
		fontLevel	= _fontLevel;
		//
		// 初期値では単一解答のみ
		// デリミッタは、/で表示行数を指示する
		Csv		cs	= 	new Csv(_ans,"/／");
		int		n	=	cs.size();
		if(n==0){
			// 正解文はなくてもいい．
			// ここはエラーにしない．
			addAnswers("");
			set_rc(ROWS, COLS);	// 高さと幅を設定
		
		}else if(n==1){
			// 正解文か、行・列指定のどちらなのか、_ans の先頭文字を見て調べる
			char	c	=	_ans.charAt(0);
			if((c=='/')||(c=='／')){
				addAnswers("");		// 正解文は空白
				try{
					setRC(cs.get(0));
				}catch(EpmlTokenException ex){
					throw ex;
				}
			}else{
				addAnswers(_ans);		// 正解文のみ
				set_rc(ROWS, COLS);		// 高さと幅を設定

			}
		}else if(n>1){
			// "/" は正解文の中にも含まれている可能性があるので、最後の要素を行数、文字数指定とみなす
			String	rtimes 	= cs.get(n-1);	// 最後の要素
			try{
				setRC(rtimes);
			}catch(EpmlTokenException ex){
				throw ex;
			}
			//
			// 正解文字列の中にある'/'を復元する
			StringBuffer 	abf		=  new StringBuffer(1024);
			boolean		flag 	= false;
			for(int k=0; k<n-1; k++){		// 最後の要素を含まない
				if(flag) abf.append("/");	// 
				abf.append(cs.get(k));
				flag = true;
			}
			addAnswers(abf.toString());
		}else{
			throw new EpmlTokenException("埋め込みテキストフィールドの指定に誤りがあります．");
		}
		
	}
	/**
	 * 行数、カラム数を指定した文字列を受け取って、インスタンス変数にセットする
	 * @param str	文字列（ex.  "3,25"）
	 * @throws EpmlTokenException	文字列中の要素が２個よりも多い場合にフォーマットエラー例外を発生する
	 */
	void	setRC(String str) throws EpmlTokenException {
		//
		int	_rows	=	ROWS;
		int	_cols	=	COLS;
		//
		Csv 	rc		= new Csv(str,",");		//  例 "3,40"
		int	nn		= rc.notZeroLengthCount();
		if(nn > 2){
			throw new EpmlTokenException("埋め込みテキストフィールドで行数、文字数の指定が間違っています．");// 文字列が空なら例外発生
		}else if(nn==1){
			if(isDigit(rc.get(0)))	_rows = Integer.parseInt(rc.get(0));
		}else if(nn==2){
			if(isDigit(rc.get(0)))	_rows = Integer.parseInt(rc.get(0));
			if(isDigit(rc.get(1)))	_cols = Integer.parseInt(rc.get(1));
		}// その他は default で
		set_rc(_rows,_cols);
		
	}
	//
	// 解答が複数ある可能性がある
	// 複数解答語を前提に解答語の追加を行う
	//
	public	void	addAnswers(String str){
		
		SStringTokenizer cs	=	new	SStringTokenizer(str, SEP);
		
		while(cs.hasMoreTokens()){
		    String	term 	=	cs.nextToken();
		    ans.add(term);	// 解答に加える.正規化しない．
		    
		}
		ansMax = ans.size();	// 語数を記録
	}
	//　テキストエリアの表示の高さと幅を設定する
	void	set_rc(int _rows,int _cols){
		nRows	= _rows;
		nCols	= _cols;
	}
	//
	public	int	getRows()	{	return	nRows; }
	public	int	getCols()	{	return	nCols; }
	//
	// 要素データ
	public	String	name()					{ 	return name; 	}
	public	String	seqNum()				{ 	return seqNum; 	}
	public	String	ans(int num)			{	return getAnswer(num);}
	public	int		fontLevel()				{	return fontLevel;	}
	// 得点
	public	void	setPoints(int	pt)		{	points	= pt;	}
	public	int		getPoints()				{   return points;  }
	public	void	clsPoints()				{	points	= 0;	}
	// 配点
	public	int		getAp()					{   return ap;  	}
	public	void	setAp(int	pt)			{	ap	= pt;	}
	public	void	clsAp()					{	ap	= 0;	}
	//
	// 正解語リスト
	public	Vector	correctList(){
		return	ans;
	}
	//
	// 正解を追加する
	void	addAnswer(String _ans){
		ans.add(_ans);
	}
	// 全解答を削除する
	public	void	deleteAllAnswer(){
		ans.removeAllElements();
	}
	// ｋ番目の正解を削除する
	public	void	delAnswer(int k){
		if(ans.size()==0) return; // 解答がないケース（2022.3.30 追記）
		
		if( OK(k) ){
			ans.remove(k);
		}else{
			DBG.println("class cETA#delAnswer() : 添え字の値が不正である");
			return;
		}
	}
	// ｋ番目の正解を得る
	public	String	getAnswer(int k){
		if(ans.size()==0) return " "; // 解答がないケース。空白文字を返す（2022.3.30 追記）
		
		String	_ans;
		if( OK(k) ){
			_ans = (String) (ans.get(k));
		}else{
			DBG.println("class cETA#getAnswer() : 添え字の値が不正である");
			_ans = null;
		}
		return	_ans;
	}
	// 引数は学生の解答．本オブジェクトが正解
	// 採点する
	// 正規化した文の正確な一致で採点する
	// 自動採点前に、正解と不正解の振り分けを行う
	// 自動採点を選択しないオプションも考慮すること
	public	int	grades(cETA ansS){
		//
		points	= 0;
		if( equals(ansS) ){	points	= ap; }	// 
		return	points;
	}
	// 解答が等しいか（複数の正解がある場合もある）
	public	boolean	equals(cETA obj){
		String	ans0 = getAnswer(0);	// 受験者の解答はひとつ
		String	ans;					// 正解は複数ある
		//
		int		n = obj.ans.size();
		for(int k=0; k<n; k++){
			ans	= (obj.getAnswer(k));
			if(ans0.equals(ans))		return	true;
		}
		return	false;
	}
	// 添え字は適正な範囲内か
	// szがゼロなら解答はない。その場合OKメソッドを実行すること自体がまちがい。
	public	boolean	OK(int k){
		int sz = ans.size();
		if( k >= sz)	return	false;
		if( k < 0 )		return	false;
		return	true;
	}
	// 同じ問題か
	public boolean	isSame(cETA obj){
		if(name.equals(obj.name))	return true;
		return	false;
	}
	//
	// 文字列のテスト
	//
	// 区切り文字か
	boolean	match(char c){
		if((c=='/')||(c=='／'))	return	true;
		return false;
	}
	char	matchDigit(char c){
		if((c=='0')||(c=='０'))	return	'0';
		if((c=='1')||(c=='１'))	return	'1';
		if((c=='2')||(c=='２'))	return	'2';
		if((c=='3')||(c=='３'))	return	'3';
		if((c=='4')||(c=='４'))	return	'4';
		if((c=='5')||(c=='５'))	return	'5';
		if((c=='6')||(c=='６'))	return	'6';
		if((c=='7')||(c=='７'))	return	'7';
		if((c=='8')||(c=='８'))	return	'8';
		if((c=='9')||(c=='９'))	return	'9';
		return '*';
	}
	// 文字列が数字がどうかチェックする
    boolean isDigit(String s){
        if((s == null)||(s.length()==0))    return false;
		//
        int len = s.length();
        for(int i=0; i<len; i++){
            char ch = matchDigit(s.charAt(i));
            if(ch == '*')       return false;
        }
        return true;
    }
	// 内容
	@Override
	public String	toString(){
		String	CR = System.getProperty("line.separator");
		StringBuffer bf = new StringBuffer(1024);
		bf.append(name + "/(sq)" + seqNum + "/(ap)" + ap + "/(得点)" + points + "/(font)" + fontLevel + "/(row)" + nRows + "/(col)" + nCols + CR);
		getAll(bf);
		return bf.toString();
	}

	// 全ての解答のＣＳＶ表現を得る
	public	void	getAll(StringBuffer bf){
		String	CR 		= System.getProperty("line.separator");
		int	size		= ans.size();
		for(int i=0; i<size; i++){
			bf.append( "   要素(" + i + ")" + getAnswer(i) + CR );
		}
		return;
	}

}