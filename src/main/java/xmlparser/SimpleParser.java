/*
 *  SimpleParser:  Takashi KAWABA 2004.9 -
 */

package xmlparser;

/**
 * xml 文書をパースし、XmlTokenオブジェクト(タグ[Tag class]またはテキストを保持する)を
 * 順にひとつづつ取り出す
 * 
 * 文頭が # で始まる行はコメントとして無視する
 */
public class SimpleParser {

	private	char		SPC	=	' ';
	
	private	String		buffer;		// 原文
	private	int		max;		// 原文の長さ
	private	int		p;			// バッファ内の位置（ゼロオリジン）
	
	private	XmlToken	xt;
	
	/**
	 * xmlを文字列で受け取るコンストラクタ
	 * @param xml	xmlテキスト
	 */
	public	SimpleParser(String	xml){
		buffer		= new String(xml);	// 解析対象の文字列
		max			= buffer.length();
		p			= 0;
		xt			= new XmlToken();
	}
	
	/**
	 * xml 文書から、XmlTokenオブジェクトをひとつづつ順に取り出すクラス．XmlTokenオブジェクトとはタグまたはテキストである．
	 * もうオブジェクトが存在しない場合は null を返す
	 * 
	 * XmlTokenの内容は、タグまたはタグに挟まれたテキストデータで、内容と種別はXmlTokenのクラスメソッドで取得することができる．
	 * 
	 * 先頭に # のついた行データはコメントとして無視する．
	 * タグを取り出すに際、改行コードはひとつの空白に変換する．
	 * 先頭の "<" と末尾の ">" または "/>" は取り除かれる
	 * 
	 * 正しい形式でなければ xmlException 例外を発生する．例外オブジェクトから、発生桁位置とメッセージを取得できる
	 */
	public XmlToken nextXml() throws xmlException {
		char			c;
		StringBuffer	buf	=	new StringBuffer();
		int	flag	=	-1;
		
		/*
		 * タグに挟まれたデータテキストがあるかどうか調べる 
		 * スペースやコメントなどを除去する
		 * 改行コードはtextに含む
		 */
		String	text	=	getText();
		if(!isEmpty(text)){
			xt	=	new XmlToken(XmlToken.TEXT, text);
			return	xt;
		}
		
		/*
		 * データテキストがなかった場合の処理
		 * タグがあるか、あるいはデータエンドのどちらかである
		 * 先頭の '<'を読み飛ばす
		 */		
		if((c=nextTextChar())==0){// データエンド
			return	null;
		}
		/* 終了タグでないか調べる */
		if((c=nextTextChar())==0){// データエンド
			return	null;
		}		
		if(c=='/'){
			/* '/'は読み捨てる */
			flag	=	XmlToken.ETAG;
		}else{
			putBackChar();			
		}
		while((c=nextTextChar())!=0){
			
			if(c=='/'){
				c = nextTextChar();
				if(c=='>'){
					flag	=	XmlToken.ETAG;
					break;
				}else{
					putBackChar();
				}
				
			}else if(c=='>'){
				/* 先頭に '</'があればすでに ETAG が設定されている */
				if(flag!=XmlToken.ETAG) flag =	XmlToken.TAG;
				break;
			}
			buf.append(c);
		}
		if(flag==-1)	throw new xmlException(p, "文末に > がない");
		String	s	=	buf.toString();
		if(isEmpty(s))	throw new xmlException(p, "タグの内容がない");
		
		/* 状態、タグの名前をセットして終了 */
		xt	=	new XmlToken(flag, s);
		return	xt;
	}

	/** システムバッファの現在位置を返す */
	public	int	location(){
		return	p;
	}
	
	/**
	 * タグに挟まれたデータテキストがあるかどうか調べる
	 * 
	 * 初回の走査では該当しないが、スペースやコメントなどを除去する働きもある
	 * ので最初にこのチェックを行う
	 * 
	 * 走査では、テキストを '<' が出現するまで順に見て、その間に現れたテキストを
	 * 収集して返す．テキストがあれば
	 * 		トークン種別に DATA をセット
	 * 		tkn に データテキストをセット
	 * 
	 * テキストがない場合は、単に "" を返す
	 */
	String	getText(){
		/* 
		 * 空白・改行その他の制御文字をスキップしてテキストがあるかどうか調べる
		 * テキストがあればさらにコメント行をスキップして次に<が出現するまでの
		 * 文字をバッファに入れる
		 */
		skipSpace();// 空白と全てのコントロール文字を読み飛ばす
		
		StringBuffer	bf	=	new StringBuffer();
		char	c;
		
		// nextRawChar() は改行文字(\n)だけはそのまま返し、全角文字の半角変換も行なわない
		while((c=nextRawChar()) !=0){
			
			if(c=='#'){
				skipComment();	// 行末の改行文字まで捨てる
				skipSpace();
				// コメントの前までで１ワードとする
				if(bf.length()>0){
					break;
				}				
				
			}else if(c=='<'){
				putBackChar();
				break;
				
			}else{
				bf.append(c);// 改行コードは取り込む
				
			}
		}
		int	size	=	bf.length();
		if(size>0)	return	bf.toString();
		return	"";
	}
	/**
	 * 空白・改行をスキップする
	 */
	void	skipSpace(){
		/* nextTextChar() は全てのコントロール文字を読み飛ばす */
		char	c;
		while ((c=nextTextChar())!=0){
			if(!isSpace(c)){
				putBackChar();
				return;
			}
		}
	}
	/**
	 * 空白をスキップする
	 * 改行以外の制御文字にスキップする
	 */
	void	skipRawSpace(){
		/* nextRawChar() は改行以外のコントロール文字をひとつの空白として読み込む */
		char	c;
		while ((c=nextRawChar())!=0){
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
		/* nextRawChar() は改行以外のコントロール文字をひとつの空白として読み込む */
		char	c;
		while((c=nextRawChar())!=0){
			if(isCR(c)){
				// 改行文字は捨てる
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
	 * 全てのコントロール文字を読み飛ばす
	 * <,=,/,> について全角→半角の変換を行なう
	 * 
	 * @return		取り出した文字
	 */
	char	nextTextChar(){
		return	nextTextChar(true, true);
	}
	/**
	 * バッファから１文字取って返す．ポインタは＋１される
	 * \n 以外のコントロール文字を読み飛ばす
	 * \n だけはそのまま返す．
	 * <,=,/,> について全角→半角の変換は行なわない
	 * 
	 * @return		取り出した文字
	 */
	char	nextRawChar(){
		return	nextTextChar(false, false);
	}
	
	/**
	 * バッファから１文字取って返す．ポインタは＋１される
	 * コントロール文字は原則として読み飛ばす．

	 * sw が true  なら 全ての制御文字を読み飛ばす．
	 * sw が false なら \n だけはそのまま返す．
	 * 
	 * @return		取り出した文字
	 */
	char	nextTextChar(boolean sw1, boolean sw2){
		char	c;
		// nextChar() はOSで異なる様々な改行文字を1文字の\nとして返す
		c = nextChar(sw2);
		if(c==0)	return	c;
		
		
		if(Character.isISOControl(c)){
			/* 全ての制御文字を読み飛ばす */
			if(sw1){
				//c = SPC;
				c	=	nextTextChar(sw1, sw2);
			
			/* \n 以外の制御文字を読み飛ばす */
			}else{
				if(c!='\n'){
					//c	= SPC;
					c	=	nextTextChar(sw1, sw2);
				}
			}
		}
		return	c;
	}	
	/**
	 * バッファから１文字取って返す．ポインタは＋１される
	 * @return		取り出した文字
	 */
	char nextChar(){
		return	nextChar(true);
	}
	/**
	 * バッファから１文字取って返す．ポインタは＋１される
	 * OSの種類にかかわらす、改行コードを \n の１文字に変換する
	 * sw が true なら<,=,/,> について全角→半角の変換を行なう
	 * 
	 * @param sw
	 * @return
	 */
	char nextChar(boolean	sw){
		char	c	=	nextCHAR();
		if(c=='\r'){
			char c2	=	nextCHAR();// 次の文字
			if(c2=='\n'){
				// windows[\r\n]
				// （\rを読み飛ばし）
			}else{
				// MAC[\r]
				putBackChar();
			}
			c	=	'\n';
			
		}// unix[\n]は 何もしない
		if(sw){
			return	rglToHan(c);// ( <,=,/,> ) を半角にそろえる
		}else{
			return	c;
		}
	}
	/**
	 * バッファから１文字取って返す．ポインタは＋１される
	 * @return		取り出した文字
	 */
	char nextCHAR(){
		char	c = 0;
		if( !EOB() ){
			c = buffer.charAt(p);
			p++;
		}else{
			c = 0;	// バッファが空
			return	 c;	
		}
		return	c;		
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
	
}
