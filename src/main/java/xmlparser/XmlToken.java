/*
 * XmlTolen : Takashi_KAWABA  2004.9 -
 */
package xmlparser;

/**
 * ひとつのXml要素を保持する構造体オブジェクト
 * 
 * 要素がタグの場合、タグ文字列から（<, </, ,> ,/>） を除いて Tag クラスに渡し、
 * Tag オブジェクトを作成して保持する．Tag オブジェクトは タグ要素の名前やアトリビュートを
 * 操作するメソッドを提供する
 */
public class XmlToken extends Object{
	
	/**
	 * 要素
	 *   テキスト
	 * 　タグ
	 * 　終了タグ
	 */ 
	private	String		tkn;
	
	/**
	 * タグ
	 * 　要素がタグの場合に作成されるタグオブジェクト
	 */
	private	Tag		tag;
	
	/** トークンの種別 */
	private	int		status;
	
	/** タグ名 */
	private	String		name;
	
	/** データテキストであることを示す status の種別 */
	public	static	final	int TEXT	= 1;

	/** タグであることを示す status の種別 */
	public	static	final	int TAG	= 2;
	
	/** 終了タグ（ /> で終わるタグ）であることを示す status の種別 */
	public	static	final	int ETAG	= 3;

	/** デフォルトコンストラクタ */
	public	XmlToken(){
		this.status	=	TEXT;
		this.tkn		=	"";
		tag				=	null;
	}
	
	/** 
	 * コンストラクタ
	 * @param status	トークンの種類
	 * @param tkn		トークン文字列
	 */
	public	XmlToken(int	status, String tkn){
		this.status		=	status;
		this.tkn		=	tkn;
		tag				=	null;
		
		/* タグであればタグオブジェクトを作成する */
		if(isTag()){
			tag	=	new Tag(tkn);
		}
	}
	public	boolean	isTag(){
		if((status==TAG)||(status==ETAG))	return	true;
		return	false;
	}
	public	boolean	isEtag(){
		if(status==ETAG)	return	true;
		return	false;
	}
	public	boolean	isText(){
		if(status==TEXT)	return	true;
		return	false;
	}
	/**
	 * テキストを返す
	 * @return
	 */
	public	String	getText(){
		if(isText())	return	tkn;
		return	null;
	}
	/**
	 * 行末の改行コードを除去したテキストを返す
	 * ただし、改行コードは \n に変換されているものとする
	 * 
	 * @return
	 */
	public	String	getTextString(){
		if(!isText())	return	null;
		//
		int	n	=	tkn.length();
		if(n==0)	return	"";
		
		StringBuffer	sbf	=	new	StringBuffer();
		char			c;
		for(int	k=0; k<n; k++){
			c	=	tkn.charAt(k);
			if(c=='\n'){
				break;
			}
			sbf.append(c);
		}
		if(sbf.length()==0)	return	"";
		return	sbf.toString();
	}
	/**
	 * タグ全体を返す
	 * @return
	 */
	public String	getToken(){
		if(isTag())	return	tkn;
		return		null;
	}	
	/**
	 * トークンを返す（デバッグ用）
	 * @return
	 */
	public	String	token(){
		return	tkn;
	}
	/**
	 * タグ名を返す
	 * @return		トークンがタグのときタグ名を返す．それ以外では "" を返す．
	 */
	public	String	getTagName(){
		if(status==TEXT)	return	null;
		return	tag.getName();	
	}
	/**
	 * アトリビュート名でその値を検索して返す
	 * @param attrName
	 * @return
	 */
	public	String	get(String attrName){
		return	tag.get(attrName);
	}
	/**
	 * タグの場合に、タグ名を除くアトリビュートを順に返す
	 * @return		アトリビュート．終端ではnullを返す
	 */
	public	Attribute	nextAttr(){
		if(isText())	return		null;	
		Attribute	atr	=	tag.next();
		return	atr;
	}
	/**
	 * アトリビュートの抽出位置を先頭にリセットする
	 */
	public	void	reset(){
		if(isText())	return;
		tag.reset();
	}
	/**
	 * アトリビュートの個数を返す
	 */
	public	int	attrSize(){
		return	tag.size();
	}
}




























