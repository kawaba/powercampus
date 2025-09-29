
/*
	（文中に埋め込まれたテキストボックスに正しいと思う語句を
	　記入して解答する問題のクラス）の要素クラス
*/
package epml;
import java.util.Vector;
import epml.tools.Csv;
import epml.tools.DBG;
import epml.tools.Regularizer;
import epml.tools.SStringTokenizer;

public class cETF extends Object implements emlVar{
	//
	String	name;		// 一意名　	"ETF(3)" など
	String	seqNum;		// 出現番号	"3" など
	Vector	ans;		// 正解語リスト
	//
	int	ap;			// 配点
	int	points;		// 得点
	//
	int	width;		// 表示桁幅(px 単位)
	int	height;		// 表示縦幅(px 単位)
	int	fontLevel;	// フォントレベル
	//
	private	String	separator;
	//
	Vector	incorrects;	// 不正解語句（作業用）
	String	eval;		// 学生の解答のとき、その正誤を表す文字（t/f）
	/**
	 * コンストラクタ<br>
	 * 
	 * _ans として渡される文字列は、以下の二つのパターンで、不正解語リストと解答の正誤はない場合もある
	 * 
	 * 　(1)正解語リスト~不正解語リスト --- 問題のEPMLテキスト（不正解語リストは問題側にしか設定されない）
	 * 　(2)学生の解答~解答の正誤      --- 学生の解答のEPMLテキスト（正誤は採点された学生側のデータにしか設定されない）．
	 * 
	 * 不正解語のリストと区別できるように、解答の正誤は".t" または ".f" が exam#doEpml() で付加される．
	 * ただ eval には単に "t" または "f" として記録しておき、これをドット付きに変えて付加するのは
	 * exam#doEpml() の仕事である．
	 * 
	 * @param _name		一意名　	"ETF(3)" など
	 * @param _seqNum		出現番号	"3" など
	 * @param _ans			正解語リストまたは学生の解答語
	 * @param _ap			配点
	 * @param _fontLevel	フォントレベル
	 * @param _width		表示桁幅(px 単位)
	 * @param _height		表示縦幅(px 単位)
	 */
	public	cETF(String _name,String _seqNum,String _ans,int _ap,int _fontLevel,int _width,int _height){
		if(DBG.fa) DBG.println("class cETF #cETF() : コンストラクタ の先頭です");
		
		separator	=	SEP;	// 複数の正解語（不正解語）を区切るセパレータ 2005.7.24
		
		name		= _name;
		seqNum		= _seqNum;
		ap			= _ap;
		eval		= "";					// 通常の問題データの場合は""のままになる
		ans			= new Vector(10,10);	// 複数正解対応
		incorrects	= new Vector(10,10);	// 不正解語句
		//
		Csv		cs	= new Csv(_ans,"~");
		int	len	= setCorrectAnswers(cs.get(0));			// 正解語を登録
		if(cs.size() == 2){
			String	test	=	cs.get(1);
			if((test.equals(".t"))||(test.equals(".f"))){	// 学生の解答の正誤
				eval	= new String(test.substring(1));	// "." を取って残りの"t"か"f"のみを設定する
			}else{
				setIncorrectAnswers(cs.get(1));				// 不正解語を登録
			}
		}	
		points		= 0;
		fontLevel	= _fontLevel;
		width		= (len + 1);			// 文字数（実際の表示幅は出力処理でfontLeverl を見て決定する）
		height		= _height;				// 不要になった．フォントの大きさから自動的に決まる
		
	}
	/**
	 * 正解かどうかをセットする
	 * 
	 * @param s	"t" または "f" で正解のとき"t"
	 */
	public	void	setEval(String s){
		eval	= new String(s);
	}
	/**
	 * 正解かどうかを返す
	 * 
	 * @return　　正解のとき "t" そうでなければ "f" を返す.また、未設定なら "" を返す．
	 */
	public String	getEval(){
		return	eval;	
	}
	// 正解語を登録
	int	setCorrectAnswers(String words){
		// 複数解の可能性
		Regularizer	rg	= new Regularizer();
		int	len	= 0;
		SStringTokenizer	st	= new SStringTokenizer(words,separator);
		while(st.hasMoreTokens()){
            String tk 		= (st.nextToken()).trim();
			int	tk_len	= tk.length();
			if(tk_len > 0){
				// 正解語は以下のように正規化する
				//	・前後の空白は除く
				//	・途中の空白は１文字の半角空白にする
				//	・英数記号を全て半角に訂正
				//	・英字は全て大文字に変更
				//
				String	ansWord = rg.toGeneralSet(tk);	// 上の正規化を実行する
				//
				// 重複して同じものを登録しない
				boolean	flag	= false;
				int			n		= ans.size();
				for(int j=0; j<n; j++){
					if(ansWord.equals( ans.get(j) )){
						flag	= true;	// 同じものがあればここで終わり
						break;
					}
				}
				if(!flag){// 同じものがなかった時登録する
					ans.add(ansWord);
				}
			}
			if(len < tk_len)	len = tk_len;	// 最も長い解答の長さ
		}
		return	len;	// 最も長い解答の長さ
	}
	//
	// 不正解語を登録
	void	setIncorrectAnswers(String words){
		//
		Regularizer			rg	= new Regularizer();
		SStringTokenizer	cs	= new SStringTokenizer(words,separator);
		while(cs.hasMoreTokens()){
			
			String		s		= rg.toGeneralSet(cs.nextToken());// 正規化する
			boolean	flag	= false;
			int			n		= incorrects.size();
			for(int j=0; j<n; j++){
				if(s.equals( incorrects.get(j)) ){
					flag	= true;	// 同じものがあればここで終わり
					break;
				}
			}
			if(!flag){// 同じものがなかった時登録する
				if(s.length()>0){
				    incorrects.add(s);
				}
			}
		}
	}
	
	//
	@Override
	public String	toString(){
		return name + "/(sq)" + seqNum + "/(正解語数)" + size() + " /" + getAll() + "/(ap)" + ap + "/(得点)" + points + "/(font)" + fontLevel + "/(W)" + width + "/(h)" + height;
	}
	public int	size(){
		return 	ans.size();
	}
	// 全ての解答のＣＳＶ表現を得る
	public	String	getAll(){
		StringBuffer bf = new StringBuffer(1024);
		boolean flag	= false;
		int	size		= ans.size();
		for(int i=0; i<size; i++){
			if(flag) bf.append(",");
			bf.append(getAnswer(i) );
			flag	= true;
		}
		return bf.toString();
	}
	// 要素データ
	public	String	name()					{ 	return name; 	}	// オブジェクトのキー
	public	Vector	ans()					{	return ans;		}	// オブジェクトの全正解語
	//
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
	// 表示桁幅
	public	int		getWidth()				{   return width;  	}
	public	void	setWidth(int w)			{	width	= w;	}
	public	void	clsWidth()				{	width	= 0;	}
	// 表示桁幅
	public	int		getHeight()				{   return height; 	}
	public	void	setHeight(int w)		{	height	= w;	}
	public	void	clsHeight()				{	height	= 0;	}
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
		if( OK(k) ){
			ans.remove(k);
		}else{
			DBG.println("class cETF#delAnswer() : 添え字の値が不正である");
			return;
		}
	}
	// ｋ番目の正解を得る
	public	String	getAnswer(int k){
		String	_ans;
		if( OK(k) ){
			_ans = (String) (ans.get(k));
		}else{
			DBG.println("class cETF#getAnswer() : 添え字の値が不正である");
			_ans = null;
		}
		return	_ans;
	}
	//
	// 引数は学生の解答．本オブジェクトが正解
	// 採点する
	public	int	grades(cETF std_ans){

		if (std_ans==null) return	0;// 2008.9.1
		
		points	= 0;
		if( equals(std_ans) ){
			std_ans.setEval("t");
			points	= ap;	// 正解オブジェクトから取る
		}else{
			std_ans.setEval("f");
			points	= 0;
		}
		return	points;
	}
	//
	// 不正解語リストをクリアする
	public void	removeAllIncorrectWords(){
		if(incorrects.size() > 0)	incorrects.removeAllElements();
		
	}
	//
	// 正解語リストをクリアする
	public void	removeAllCorrectWords(){
		if(ans.size() > 0)	ans.removeAllElements();
		
	}
	// 正解語リストを返す
	public	Vector	correctList(){
		return	ans;
	}
	// 蓄積した不正解語リストを返す
	public	Vector	incorrectList(){
		return	incorrects;
	}
	//
	// 引数は学生の解答．本オブジェクトが正解
	// 採点して不正解語句の採取を行う
	public	void	preGrades(cETF stu_ans){
		if(DBG.fa) DBG.println("class cETF #preGrades() : 不正解語句の採取を行う の先頭です");
		//
		if(stu_ans==null) return; // 2008.9.1
		
		String	answer0 = stu_ans.getAnswer(0);	// 受験者の解答はひとつ(回答がない場合がある)
	
		String	answer;							// 正解は複数ある
		//
		int		n = ans.size();							// 解答の数
		for(int k=0; k<n; k++){
			answer	= (getAnswer(k));			// ｋ番目の正解
			if(answer.equals(answer0)){					// 等しいか
				if(DBG.fa) DBG.println("           ■ 正解語 = " + answer0);
				return;									// 正解の場合ここからリターンする
			}
		}
		boolean	flag	= false;
		int		nn		= incorrects.size();
		for(int j=0; j<nn; j++){
			if(answer0.equals( incorrects.get(j) )){
				flag	= true;	// 同じものがあればここで終わり
				break;
			}
		}
		if(!flag){// 同じものがなかった時登録する
			incorrects.add(answer0);
		}
		if(DBG.fa) DBG.println("           ■発見した不正解語 = " + answer0);
		return;
	}
	// 引数は学生の解答．本オブジェクトが正解．
	// 解答が等しいか（複数の正解がある場合もある）
	public	boolean	equals(cETF std_ans){
		if(std_ans==null) return false; // 2008.9.1
		
		String	answer0 = std_ans.getAnswer(0);	// 受験者の解答はひとつ
		String	answer;							// 正解は複数ある
		//
		int	n = ans.size();					// 解答の数
		for(int k=0; k<n; k++){
			answer	= (getAnswer(k));	// ｋ番目の正解
			if(answer.equals(answer0))	return	true;
		}
		return	false;
	}
	// 添え字は適正な範囲内か
	public	boolean	OK(int k){
		int sz = ans.size();
		if( k >= sz)	return	false;
		if( k < 0 )		return	false;
		return	true;
	}
	// 同じ問題か
	public boolean	isSame(cETF obj){
		if(name.equals(obj.name))	return true;
		return	false;
	}
}