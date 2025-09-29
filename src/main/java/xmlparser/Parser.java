/*
 *
 */
package xmlparser;

/**
 * パーサーの雛型
 */
public class Parser extends Object{

	final	char	SPC	=	' ';
	
	String		buffer;		// 原文
	int		max;		// 原文の長さ
	int		p;			// バッファ内の位置（ゼロオリジン）
	
	public	Parser(){
		buffer		= "";	// 解析対象の文字列
		max			= 0;
		p			= 0;
	}

	public	Parser(String	str){
		buffer		= new String(str);	// 解析対象の文字列
		max			= buffer.length();
		p			= 0;
	}

	
	/** 名前文字列などを返す時に使う（オーバーライドする） */
	public	String	getName(){
		return	" class Parser";
	}
	/**
	 * バッファから制御文字でない文字を１文字取って返す．
	 * 制御文字は無視される
	 */
	/**
	 * 空白・改行をスキップする
	 */
	void	skipSpace(){
		/* nextTextChar() はコントロール文字をひとつの空白として読み込む */
		char	c;
		while ((c=nextTextChar())!=0){
			if(!isSpace(c)){
				putBackChar();
				return;
			}
		}
	}
	/**
	 * 改行文字が現れるまでのデータをスキップする
	 * 改行文字の直前までポインタを進めて返る
	 */
	void	skipComment(){
		/* nextChar() はバッファからそのままの文字を読み込む */
		char	c;
		while((c=nextChar())!=0){
			if(isCR(c)){
				putBackChar();
				return;
			}
		}
	}
	/**
	 *  空白文字かどうかのチェック
	 * @param c
	 * @return
	 */
	boolean	isSpace(char c){
		if(c==' ')			return	true;
		if(c=='　')		return	true;
		return false;
	}	
	/**
	 * 改行文字かどうかの判断
	 * @param c	文字
	 * @return		改行文字(\n または \r)なら true
	 */
	boolean	isCR(char	c){
		if(Character.isISOControl(c)){
			if((c=='\n')||(c=='\r')){
				return	true;	
			}
		}
		return	false;
	}
	/**
	 * バッファが空かどうか
	 * @return
	 */
	boolean	EOB(){
		return (p >= max);	// 最後のとき true
	}
	/**
	 * 文字列が空かどうかテストする
	 * @param str
	 * @return
	 */
	boolean isEmpty(String str){

		if(str==null) 			return  true;

		str	=	str.trim();
		if(str.length()==0)	return  true;
		return false;
	}
	/**
	 * バッファから１文字取って返す．ポインタは＋１される
	 * コントロール文字は一文字の空白として返す
	 * @return		取り出した文字
	 */
	char	nextTextChar(){
		char	c;
		c = nextChar();
		if(c==0)	return	c;
		
		/* 制御文字は空白文字として返す */
		if(Character.isISOControl(c)){
			c = SPC;	
		}
		return	c;
	}
	/**
	 * バッファから１文字取って返す．ポインタは＋１される
	 * @return		取り出した文字
	 */
	char nextChar(){

		char	c = 0;
		if( !EOB() ){
			c = buffer.charAt(p);
			p++;
		}else{
			c = 0;	// バッファが空
			return	 c;	
		}
		return	rglToHan(c);// ( <,=,/,> ) を半角にそろえる
	}
	/**
	 *  <,=,/,> を半角にそろえる
	 */
	char	rglToHan(char c){
		if((c=='<')||(c=='＜'))		return	'<';
		if((c=='=')||(c=='＝'))		return	'=';
		if((c=='>')||(c=='＞'))		return	'>';
		if((c=='/')||(c=='／'))		return	'/';
		return	c;
	}
	/**
	 * バッファに１文字戻す
	 *
	 */	
	void	putBackChar(){
		--p;
	}

}
