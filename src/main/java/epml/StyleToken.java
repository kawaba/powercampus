
package epml;
import	epml.tools.*;
//import 	java.io.*;
//import 	java.text.*;
//import 	java.util.*;
import 	java.net.*;
/**
 * １行分の書式指定をパースする
 * 入力された文字列に対して、最初に英数記号は全て半角になおし、1個以上の連続するスペースは１個の半角スペースに直す
 * ここで取り出されたトークンを使う処理では、半角を前提にしてよい
*/
public class StyleToken extends Object {
	//
	FormatStyle	fstyle;
	Csv			all_tkn;		// 書式文をトークンに分割したもの
	String		buffer;			// 書式文
	int			max;			// 書式文の長さ
	int			p;				// バッファ内の位置（ゼロオリジン）
	String		tkn;			// トークン
	int			id;				// トークン種別
	String      key;			// alias のキー
	//
	int			glovalPos;		// Wikitoken クラスでのバッファ位置
	//
	Regularizer	rgl;			// 文字列正規化クラス
	//
	// トークン区切り記号（正規化するので全角は考えなくて良い）
	static final char SPC	= ' ';
	static final char equ 	= '=';
	//
	//
	//
	public StyleToken(String str,int gpos,FormatStyle fs){
		//
		fstyle			= fs;
		glovalPos		= gpos;
		//
		rgl				= new Regularizer();
		buffer	 		= rgl.toRegular(str);		// 正規化した文字列（全角英数は半角英数に、全角・半角の空白は１個の半角空白に）
		all_tkn			= new Csv(buffer," =()");	// キーワードとそのパラメータをトークンとする
		max		 		= all_tkn.size();
		p		 		= 0;
		tkn		 		= null;
		id		 		= 0;
		key				= "";
		//
		if(DBG.fa) DBG.println("buffer = " + buffer);
		if(DBG.fa) DBG.println("max    = " + max);
	}
	//
	public	String	tknSTR()	{ return	tkn;}	// 取り出したトークンを返す
	public	int		tknID()		{ return	id; }	// 取り出したトークンの種別番号を返す
	public	String	tknKEY()	{ return	key; }	// alias のキー値を返す
	//
	// フォーマットからトークンを取り出す
	//      １行文のフォーマット文字列はあらかじめ、" " と "=" をデリミッタとしてトークンに分解してある．
	//　    nextWord() では、このトークンをひとつずつ返す
	//
	public	int		getToken()  throws PmlTokenException {
		if(DBG.fa) DBG.println("class StyleToke # getToken() :■ フォーマットからトークンを取り出す　の先頭です");
		/*
		 * 作業用変数
		 */
		String	value,unit;
		//
		// ひとつトークンを取り出す
		String temp	= nextWord();
		//
		if(temp == null){
			return	-1; // end of buffer
		}
		// Style クラスのスタティックメソッドから名前で現在のトークンのＩＤを引く
		// 先頭に $ が付いたものはエイリアスと判定される(ID = ALIAS)
		int	id	= fstyle.KeywordNumber(temp);
		//if(DBG.fa) DBG.println("■ id =" + id);
		//if(DBG.fa) DBG.println("■ id[] =" + FormatStyle.FORMAT_KEYWORD[id]);

		/*
		 * 取り出したトークンのIDによって処理を分ける
		 * 具体的には指示語とそのパラメータを分離し、適当な変数にセットしなければならない
		 */
		int	num	= FormatStyle.NOT_KEYWORD;// 次のトークンのＩＤ（初期化しただけ）
		switch(id) {
		  //
		  // インデント処理
		  case FormatStyle.INDENT:
			temp	= nextWord();
			if(temp == null){
				throw new PmlTokenException(glovalPos,"インデント(indent)の値の指定がありません");
			}
			num = fstyle.KeywordNumber(temp);
			if(num!=FormatStyle.NOT_KEYWORD)	throw new PmlTokenException( glovalPos,"インデント(indent)の値の指定がありません");
			//
			/* 文字列 temp から数値部分と単位部分を取り出す */
			value	= rgl.toDigit(temp);		// 数字
			unit	= rgl.unitString(temp);		// 単位(全て小文字に直されている)
			
			if(value.length()==0)	throw new PmlTokenException( glovalPos,"インデント(indent)の値を指定してください");
			if(unit.length()==0)	unit = "px";
			//
			// indent=1u という指定を inden = 10px に直す
			if(fstyle.isValidWord(FormatStyle.unitKWD2,unit)){
				int	val = Integer.parseInt(value) * 10; 
				value	= String.valueOf(val);
				unit	= "px";
			}
			if(!fstyle.isValidWord(FormatStyle.unitKWD,unit)) throw new PmlTokenException( glovalPos,"インデント(indent)値の単位の指定が違っています");
			//
			tkn	= value + unit;
		  	break;
		  //
		  // イメージボックス処理
		  case FormatStyle.IMGBOX:
			temp	= nextWord();
			if(DBG.fa) DBG.println("■ imgbox / nextWord =" + temp);
			if(temp == null){
				throw new PmlTokenException( glovalPos,"グラフィックス配置指定(imgbox) の値の指定がありません");
			}
			num 	= fstyle.KeywordNumber(temp);
			if(num!=FormatStyle.NOT_KEYWORD)	throw new PmlTokenException( glovalPos,"グラフィックス配置指定(imgbox) の値の指定がありません");
			//
			String	imgUrl 		= "";
			String	position	= "";
			String	border		= "0";
			Csv	cs		=	new Csv(temp,",",true);	// コンマをデリミッタとする. 文字列はtrim()され、長さゼロの要素は登録されない
			if( (cs.size() == 0)||(cs.size() > 3) )	throw new PmlTokenException( glovalPos,"imgbox のパラメータ指定に誤りがあります");
			//
			if(cs.size()==3){
				String	loc = cs.get(1);
				if(!fstyle.isValidWord(FormatStyle.imgboxKWD,loc) ){
					throw new PmlTokenException( glovalPos,"imgbox のパラメータ指定に誤りがあります(top,bottom,left,right のどれかを指定します)");
				}else{
					imgUrl		= cs.get(0);
					position	= cs.get(1);
					border		= rgl.toDigit(cs.get(2));		// 数字
					if(border.length()==0)	throw new PmlTokenException( glovalPos,"ボーダーの値の設定に誤りがあります");
				}
			}else if(cs.size()==2){
				String	loc = cs.get(1);
				if(!fstyle.isValidWord(FormatStyle.imgboxKWD,loc) ){
					throw new PmlTokenException( glovalPos,"imgbox のパラメータ指定に誤りがあります(top,bottom,left,right のどれかを指定します)");
				}else{
					imgUrl		= cs.get(0);
					position	= cs.get(1);
				}
			}else{
				imgUrl		= cs.get(0);
				position	= "top";
			}
			tkn	= imgUrl+ "," + position + "," + border;
			if(DBG.fa) DBG.println("■ imgbox / tkn =" + tkn);
		  	break;
		  //
		  // 斜体字の指定
		  case FormatStyle.STYLE:
			temp	= nextWord();
			if(temp == null){
				throw new PmlTokenException( glovalPos,"style の値の指定がありません");
			}
			num = fstyle.KeywordNumber(temp);
			if(num!=FormatStyle.NOT_KEYWORD)	throw new PmlTokenException( glovalPos,"style の値の指定がありません");
			//
			if(!fstyle.isValidWord(FormatStyle.styleKWD,temp)) throw new PmlTokenException( glovalPos,"文字スタイル(style) の値の指定に誤りがあります");
			tkn	= temp;
		  	break;
		  //
		  // バリアントの処理
		  case FormatStyle.VARIANT:
			temp	= nextWord();
			if(temp == null){
				throw new PmlTokenException( glovalPos,"variant の値の指定がありません");
			}
			num = fstyle.KeywordNumber(temp);
			if(num!=FormatStyle.NOT_KEYWORD)	throw new PmlTokenException( glovalPos,"variant の値の指定がありません");
			//
			if(!fstyle.isValidWord(FormatStyle.varianKWD,temp)) throw new PmlTokenException( glovalPos,"variant の値の指定に誤りがあります");
			tkn	= temp;
		  	break;
		  //
		  // 太字
		  case FormatStyle.WEIGHT:
			temp	= nextWord();
			if(temp == null){
				throw new PmlTokenException( glovalPos,"weight の値の指定がありません");
			}
			num = fstyle.KeywordNumber(temp);
			if(num!=FormatStyle.NOT_KEYWORD)	throw new PmlTokenException( glovalPos,"weight の値の指定がありません");
			//
			if(!fstyle.isValidWord(FormatStyle.weightKWD,temp)) throw new PmlTokenException( glovalPos,"太字(weight) の値の指定に誤りがあります");
			tkn	= temp;
		  	break;
		  //
		  // 文字サイズ
		  case FormatStyle.SIZE:
			temp	= nextWord();
			if(temp == null){
				throw new PmlTokenException( glovalPos,"size の値の指定がありません");
			}
			num = fstyle.KeywordNumber(temp);
			if(num!=FormatStyle.NOT_KEYWORD)	throw new PmlTokenException( glovalPos,"size の値の指定がありません");
			//
			value	= rgl.toDigit(temp);		// 数字
			unit	= rgl.unitString(temp);		// 単位
			// 単位指定があるとき
			if(unit.length()>0){
				// 値の指定がない-->表意ワードによる指定
				if(value.length()==0){
					if(!fstyle.isValidWord(FormatStyle.sizeKWD,unit)) throw new PmlTokenException( glovalPos,"フォントサイズ (size)の指定に誤りがあります");
				// 値の指定がある-->単位文字
				}else{
					if(!fstyle.isValidWord(FormatStyle.unitKWD,unit)) throw new PmlTokenException( glovalPos,"フォントサイズ(size) の単位指定に誤りがあります");
				}
			// 単位指定がないとき
			// 1-7 を適当な単位指定に変換する
			}else{
				if(value.equals("1")){
					temp = "10px";
				}else if(value.equals("2")){
					temp = "12px";
				}else if(value.equals("3")){
					temp = "14px";
				}else if(value.equals("4")){
					temp = "16px";
				}else if(value.equals("5")){
					temp = "18px";
				}else if(value.equals("6")){
					temp = "20px";
				}else if(value.equals("7")){
					temp = "24px";
				}else{
					throw new PmlTokenException( glovalPos,"フォントサイズ(size) の簡易指定で使える数字は１から７までです．単位を付ければ任意に指定可能です．");
				}
			}
			tkn	= temp;
		  	break;
		  //
		  // 行間
		  case FormatStyle.HEIGHT:
			temp	= nextWord();
			if(temp == null){
				throw new PmlTokenException( glovalPos,"height の値の指定がありません");
			}
			num 	= fstyle.KeywordNumber(temp);
			if(num!=FormatStyle.NOT_KEYWORD)	throw new PmlTokenException( glovalPos,"height の値の指定がありません");
			//
			value	= rgl.toDigit(temp);		// 数字
			unit	= rgl.unitString(temp);		// 単位 は％のみ可能なので指定できない
			if(value.length()==0)	throw new PmlTokenException( glovalPos,"行の高さ(height)の値を指定してください");
			if(unit.length()>0)		throw new PmlTokenException( glovalPos,"行の高さ(height)の単位を指定しないでください．％として処理します．");
			//if(!FormatStyle.isValidWord(FormatStyle.heightKWD,unit)) throw new WikiTokenException( glovalPos,"行の高さ(height)の指定の単位に誤りがあります");
			tkn	= temp;
		  	break;
		  //
		  // プロポーショナルフォントを使うかどうか
		  case FormatStyle.TYPE:
			temp	= nextWord();
			if(temp == null){
				throw new PmlTokenException( glovalPos,"type の値の指定がありません");
			}
			num 	= fstyle.KeywordNumber(temp);
			if(num!=FormatStyle.NOT_KEYWORD)	throw new PmlTokenException( glovalPos,"type の値の指定がありません");
			//
			if(!fstyle.isValidWord(FormatStyle.typeKWD,temp)) throw new PmlTokenException( glovalPos,"フォントタイプ(type)の指定に誤りがあります");
			tkn	= FormatStyle.fontName(temp);	// フォント名に変換
		  	break;
		  //
		  // 文字の色
		  case FormatStyle.COLOR:
			temp	= nextWord();
			if(temp == null){
				throw new PmlTokenException( glovalPos,"color の値の指定がありません");
			}
			num 	= fstyle.KeywordNumber(temp);
			if(num!=FormatStyle.NOT_KEYWORD)	throw new PmlTokenException( glovalPos,"color の値の指定がありません");
			//
			//if(temp.charAt(0)== '#')	temp = temp.substring(1);	// ＃を使えないので
			tkn	= temp;
		  	break;
		  //
		  // 文字揃え
		  case FormatStyle.ALIGN:
			temp	= nextWord();
			if(temp == null){
				throw new PmlTokenException( glovalPos,"text_align の値の指定がありません");
			}
			num = fstyle.KeywordNumber(temp);
			if(num!=FormatStyle.NOT_KEYWORD)	throw new PmlTokenException( glovalPos,"text_align の値の指定がありません");
			//
			if(!fstyle.isValidWord(FormatStyle.alignKWD,temp)) throw new PmlTokenException( glovalPos,"行内の配置(align)の指定に誤りがあります");
			tkn	= temp;
		  	break;
		  //
		  // フロー指定
		  case FormatStyle.CODE:
		    /*
		     * ○○=×× の ××にあたる部分はないので""を設定しておく
		     */
		    tkn	=	"";	  
		    break;  
		      
		  case FormatStyle.FLOW:
			/*
			 * temp にフローサイズを得る
			 * 300 のような数字の場合と no などの文字列の場合がある
			 * 文字列の場合は tkn = "" とセットして，フローしないということをtknに残す
			 * tkn = "" の場合，WikiTokenで flow 指定がクリアされる
			 */
		    temp	= nextWord();
			if(temp == null){
				throw new PmlTokenException( glovalPos,"flow の表示幅の指定がありません");
			}
			num 	= fstyle.KeywordNumber(temp);
			if(num!=FormatStyle.NOT_KEYWORD)	throw new PmlTokenException( glovalPos,"flow の表示幅の指定がありません");
			//
			tkn	= temp;
			if(temp.equals(FormatStyle.FLOW_NO)){// ="no"
			    /*
			     * flow しないという指定
			     */
			    tkn		=	temp;
			}else{
			    /*
			     * 値と単位を確認するしてOKならtknに値文字列をセットする
			     */
			    value	=	rgl.toDigit(temp);		// 数字
			    unit	=	rgl.unitString(temp);	// 単位 はpxのみ可能なので指定できない
			    if(value.length()==0)	throw new PmlTokenException( glovalPos,"表示幅の値を指定してください");
			    if(unit.length()>0)		throw new PmlTokenException( glovalPos,"表示幅のの単位を指定しないでください．ピクセル(px)として処理します．");
			    tkn		=	temp;
			}
		  	break;
		  //
		  // URL ベースの設定
		  case FormatStyle.BASE:
		    temp	= nextWord();	// 実際のＵＲＬ部分
			if(temp == null){
				throw new PmlTokenException( glovalPos,"baseでＵＲＬが書かれていません");
			}
			// 最後が"/"でなければ付加する
			int	n	= temp.length();
			if(temp.charAt(n-1) != '/')		temp = temp + "/" ;
			//
			// 厳密なURLチェック
			if(!isURL(temp)){
				throw new PmlTokenException( glovalPos,"base のＵＲＬの書き方に誤りがあります");
			}
			tkn	= temp;
			break;
		  //
		  // ALIAS の設定     $tom(indent=50 color=blue)  ⇒ $tom ( indent = 50  color = blue ) と分解された $tom 部分
		  case FormatStyle.ALIAS:
		    if(DBG.fa) DBG.println("class StyleToken #getToken() : ALIAS の設定 の先頭です");
			//
			key			=	temp.substring(1);		// $ を除いた部分がエイリアスのキー
			Csv	body	=	new Csv(buffer,"()");	// $tom(indent=50 color=blue)  ⇒ "$tom" と "indent=50  color=blue"
			if(body.size()<2){
				throw new PmlTokenException( glovalPos,"エイリアスの内容が書かれていません");
			}else if(body.size()>2){
				throw new PmlTokenException( glovalPos,"ここにエイリアス以外の記述を書くことは出来ません");
			}
			tkn	= body.get(1);
			p 	= max;
			break;
			//
		  // 展開すべきエイリアス
		  case FormatStyle.EXP:
		    if(DBG.fa) DBG.println("class StyleToken #getToken() : 展開すべきエイリアス の先頭です");
			//
			String	exp		=  	fstyle.get_alias(temp);	// temp には abc などのエイリアスが入っている
			Csv		expCsv 	=	new Csv(exp," =");
			int		pos		= 	p - 1;
			//
			if(DBG.fa) DBG.println("エイリアスを復元前の書式文字列: " + all_tkn.toCSV());
			//
			Csv		ext	 	=	all_tkn.merge(expCsv,pos);
			all_tkn			=	ext;
			max				=	ext.size();
			p--;
			//
			if(DBG.fa) DBG.println("エイリアスを復元した書式文字列: " + all_tkn.toCSV());
			if(DBG.fa) DBG.println("現在位置　　　　　　　　　　　: " + p);
			//
			break;
		  //
		  // エラー
		  case FormatStyle.NOT_KEYWORD:
		  default:
		    throw new PmlTokenException( glovalPos,"書式の指定ではない語句が書かれています: " + temp);
		}
		//
		if(DBG.fa) DBG.println("       決定したトークンＩＤ --->" + id);
		if(DBG.fa) DBG.println("       トークンパラメータ値 --->" + temp);
		return	id;
	}
	//
	// ■ バッファから次のトークンを取り出す．
	String nextWord(){
		if(p == max)	return	null;	// バッファエンド
		//
		String	temp = all_tkn.get(p);
		p++;
		return	temp;
	}
	//
	// 有効なＵＲＬ文字列かどうか調べる
	boolean	isURL(String urlStr){
		URL		url;
		try{
			url = new URL(urlStr);
		}catch( MalformedURLException e){
			return false;
		}
		return true;
	}
}