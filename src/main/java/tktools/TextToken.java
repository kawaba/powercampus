/*
 * 作成日: 2005/02/14
 *
 * TODO
 */
package tktools;

/**
 *	テキストの中から連続した文字列を次々に取り出すためのクラス
 *　## 以下はコメント行として無視する
 */
public class TextToken {
	
	private	char		SPC		=	' ';
	private	char		KSPC	=	'　';
	private	char		COM		=	'#';
	
	private	String		buffer;		// 原文
	private	int			max;		// 原文の長さ
	private	int			p;			// バッファ内の位置（ゼロオリジン）
	
	
	/**
	 * xmlを文字列で受け取るコンストラクタ
	 * @param xml	xmlテキスト
	 */
	public	TextToken(String	str){
		buffer		= new String(str);	// 解析対象の文字列
		max			= buffer.length();
		p			= 0;
	}
	/**
	 * データテキストがあるかどうか調べる
	 * 
	 * 初回の走査では該当しないが、スペースやコメントなどを除去する働きもある
	 * ので最初にこのチェックを行う
	 * 
	 * 走査では、テキストを 2度目にSPC が出現するか、データエンドまで順に見て、
	 * その間に現れたテキストを収集して返す．
	 * テキストエンドでは、null を返す
	 */
	public	String	getNext(){

		//  空白をスキップしてテキストがあるかどうか調べる
		skipSpace();
		
		StringBuffer	bf	=	new StringBuffer();
		char			c;
		while((c=nextTextChar()) !=0){
			
			if(c==COM){	// コメント
				char	c2	=	nextTextChar();
				if(c2==COM){
					skipComment();
					skipSpace();
					// コメントの前までで１ワードとする
					if(bf.length()>0){
						break;
					}
				}else if(c2!=0){
					// コメントでなかった
					putBackChar();
					bf.append(c);
				}else{
					// テキストの最後の１文字だった
					bf.append(c);
				}
				
			}else if(isSpace(c)){// テキスト終端
				putBackChar();
				break;
				
			}else{
				bf.append(c);
				
			}
		}
		int	size	=	bf.length();
		if(size>0)	return	bf.toString();
		return	null;
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
		if(c==SPC)			return	true;
		if(c==KSPC)			return	true;
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
	private void	putBackChar(){
		--p;
	}
	
	static	public	void	main(String [] arg){
		
		String	path	=	"e:\\temp\\test.txt";
		String	str		=	FileGear.getFileData(path);
		if(Gear.isEmpty(str)){
			System.out.println("data is empty!");
		}		
		
		TextToken	pt	=	new	TextToken(str);
		String		s;
		while((s=pt.getNext())!=null){
			System.out.println(s);
		}
		
	}

}
