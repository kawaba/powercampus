/*

    ＰＭＬテキストから、表示用ＨＴＭＬを生成する

*/
package epml;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import epml.tools.Csv;
import epml.tools.DBG;
import epml.tools.Regularizer;
import epml.tools.TemplateBox;
import framework.LOG;

//
public class PmlToken {
	
    ParserInitializer	pi;

    
    /*
	 * typing 評価基準のための拡張
	 * 基準データのVector，要素はCSVで "基準文字数，評価点"
	 * 
	 */
    Vector	criterion;
    String	criterionName;
    //
    Vector	defaultCriterion;
    
    //
	String 	imgURL;			// ユーザーグラフィックスディレクトリへの相対パス   	files/
	
	/* このパスはプレビュー時にシステムグラフィックスをユーザーグラフィックスディレクトリへコピー
	 * するためのものである．
	 * プレビューから呼び出された時以外は、"" であるから、使用前に必ずチェックをする
	 */
	String	imgAbsPath;		// ユーザーグラフィックスディレクトリへの絶対パス	
	//
	String 	sysImgUrl;		// システムグラフィックスディレクトリへのWWWパス   	/pc/sysimg/
	String 	sysImgAbsPath;	// システムグラフィックスディレクトリへの絶対パス  	/var/www/html/pc/sysimg/
	//
	//
	boolean		htmlFlag;		// 資料のとき true
	//
	String		initialDataFile;	// 初期値を格納したデータファイルへの完全パス
	Hashtable	initFormats;		// フォーマットエイリアス初期値
	
	String		initTypeLeve;		// タイピング課題評価レベル初期値
	String		initTrailLeve;		// タイムトライアル入力課題評価レベル初期値
	
	//
	private		String		buffer;			// 原文
	private		int			max;			// 原文の長さ
	private		int			p;				// バッファ内の位置（ゼロオリジン）
	//
	private		int			id;				// トークン種別	
	private		String		tkn;			// トークン
	private 	String		target;			// 編集子の適用対象
	//
	private		FormatStyle	formatStyle;			// スタイルシートの設定項目を保持するオブジェクト
	//
	private		TemplateBox	templateBox;				// テンプレートファイルオブジェト
	private		String		path_HtmlTemplates;			// 集合テンプレートファイルへのパス
	//
	public		boolean		flowMode;		// フローモードかどうかを残す true=flow モード
	//
	public static final char FORMAT = '1';	// id 用の定数　"--" フォーマット指示子
	public static final char HTMLS  = '2';	// html
	public static final char DATA   = '3';	// id 用の定数　一般の文字列
	//
	public Regularizer	regularizer;
	//
	//
	public	PmlToken(String text, ParserInitializer pi){
		
	    this.pi	=	pi;
	    initFormats		=	pi.getAliases();// エイリアスの初期値
	    
	    initTypeLeve	=	pi.getTypingLevel();
	    initTrailLeve	=	pi.getTimetrialLevel();
	    
	    /*
		 * type評価基準
		 */
	    criterionName		=	"";
	    criterion			=	new Vector(16);
	    
	    
	    /*
		 * 解析対象の文字列
		 */
		buffer		= new String(text); 
		if(DBG.fa) DBG.println("buffer :" + buffer + ":");
		/*
		 * グラフィックス関連のパスをセットする
		 */
		imgURL			= pi.getUserImgURL();
		imgAbsPath		= pi.getUserImgDir();

		sysImgUrl		= pi.getSysImgURL();
		sysImgAbsPath	= pi.getSysImgDir();
		htmlFlag		= pi.isReferenceHtmlFlag();
		/*
		 * バッファとポインタ
		 */
		max				= buffer.length();
		p				= 0;
		/*
		 * トークン種別
		 */
		id				= 0; 
		/*
		 * トークン文字列＝パースしてHTML に変換してある．ただし%_target% はそのまま
		 */
		tkn				= null;	 
		/*
		 *  $編集子の対象．
		 *  さらに$編集子を含んでいる可能性があるので呼び出し元（Exam.java）
		 *  で再帰的にパースしてから %_target% に埋め込む．
		 */
		target			= null;			//
		/*
		 * フォーマットデータ
		 * 表示幅（テーブルの幅）を600で生成する。標準は700
		 */
//		formatStyle			= new FormatStyle(imgURL, sysImgUrl, imgAbsPath, sysImgAbsPath, initFormats);
		formatStyle			= new FormatStyle(imgURL, sysImgUrl, imgAbsPath, sysImgAbsPath, initFormats, 600);
		/*
		 * 変換用HTMLテンプレート集へのパス
		 */
		path_HtmlTemplates	= pi.getTemplatePath();
		/*
		 * 集合テンプレートファイルオブジェクト
		 */
		templateBox			= new TemplateBox(path_HtmlTemplates);
		/*
		 * 正規化クラス
		 */
		regularizer			= new Regularizer(); 
	}
	/**
	 * 
	 * @param str
	 */
	public	void	setDefaultCriterion(String	str){
	    /*
	     * 毎回生成する
	     */
	    defaultCriterion	=	new Vector(16);
	    /*
	     * str は　89-0,99-50,109-60,119-70,129-80,139-90 のような文字列
	     * 
	     */
	    Csv	cs	=	new	Csv(str,",");
	    for(int i=0; i<cs.size(); i++){
	        String	temp	=	cs.get(i);
	        Csv		lv		=	new	Csv(temp, "-");
	        defaultCriterion.add(lv.get(0)+","+lv.get(1));
	    }
	}
	/**
	 * ミカタイプ用評価基準を返す
	 * 
	 * 評価基準は (#t=○○）
	 * 　a1    b1
	 *   a2    b2
	 *   
	 *   an    bn
	 * (/t)
	 * で定義される水準a と評価点b のcsv  ak,bk  のベクタ
	 * 
	 * @return　　評価基準CSV のVector
	 */
	public	Vector	getCriterion(){
	   return	criterion;
	}
	/**
	 * 評価項目名を返す
	 * @return
	 */
	public	String	getCriterionName(){
		   return	criterionName;
	}	
	/**
	 *  現在のフォマットスタイルはフローモード指定を含むかどうか
	 *
	 */
	public	void	setFlowMode(boolean mode){
		flowMode	=	mode;
	}
	public	boolean	getFlowMode(){
		return	flowMode;
	}
	
	// フォーマットスタイルをセットする
	public void setFormatStyle(FormatStyle fs){
		formatStyle	=	fs;
	}
	// 現在のフォーマットスタイルを返す
	public FormatStyle getFormatStyle(){
		return	formatStyle;
	}
	// 現在のバッファ位置を返す
	public	int		tknPOS()		{ return p; }
	//
	public	int		ID()			{ return	id; }		// nextToken でセットされた id を返す
	public	String	TOKEN()			{ return	tkn; }		// nextToken でセットされた tkn を返す
	public	String	TARGET()		{ return	target; }	// nextToken でセットされた target を返す
	
	boolean	isWhiteSpace(char c){
		if(c=='\t') return true;
		if(c==' ')	return true;
		if(c=='　') return true;
		return	false;
	}
	
	/*
	 *  次のトークンを返す
	 *  フォーマット指定、特殊タグ指定などはHTMLに変換して返す
	 *   
	 *  ID を返し、トークンは tkn にセットする
	 *  ID はDATA, HTMLS など
	 */
	public int nextToken() throws PmlTokenException {
		if(DBG.fa) DBG.println("class PmlToken #nextToken() : 次のトークンを返す の先頭です");
		//
		char c	= 0;
		if((c=nextChar()) == 0 ) { 
			return -1;		// 単に終了を意味する．エラーではない
		}
		if(c=='#'){
			/*
			 * フォーマットかどうか調べて処理する
			 */
		    if( (c=nextChar()) == 0 ){
			    /*
			     * バッファエンドだがデータがあるので（次回の呼び出しで EndOfBuffer 例外が発生する）
			     */
				tkn	=	"#";
				id	= DATA;
				return	id;
			}
			if(c == '#'){
				// 文頭から始まる## かどうか調べる
				int	pos	=	p;	// 現在のバッファ位置を記憶
				putBackChar();	// ２つ目の＃
				putBackChar();	// １つ目の＃
				//
				// 前の文字がコントロール文字でない間さかのぼる
				// 空白やタブたどそのままさかのぼる
				// 制御文字でなければ ## はタダの文字列として扱う
				// 制御文字であれば改行とみなして ## をフォーマット指定として扱う
				boolean	shp_flag = true;
				while(p>0){
					p--;
					char cx	= buffer.charAt(p);
					if(isWhiteSpace(cx)){
						shp_flag = true;
						continue;
					}
					//
					if(!Character.isISOControl(cx)){
						shp_flag = false;
						break;
					}else{
						shp_flag = true;
						break;
					}
				}
				//
				p	= pos;	// バッファ位置を復元
				if(shp_flag){
					try{
						tkn	= mkFormat();// html を返す（target 設定済み）
						//
						// table format の場合もここまででHTMLテンプレートを得る
						// ただし、table format の場合は id = TABLE と変更する
						// 次は上位ルーチンから表データの取得というメソッドで呼ばれる
						// PmlToken の中に表データの取得メソッドを新たに作成すること
						//
					}catch(PmlTokenException e1){
						throw e1;
					}
					id	= FORMAT;
				}else{
					tkn	=	"##";
					id	= DATA;
					return	id;
				}
				//
			}else{
				StringBuffer	bf = new StringBuffer(2048);
				bf.append('#');		// 通常の文字列として
				bf.append( c );		
				tkn	= data(bf);
				id	= DATA;
			}

			
		//////////////////////////////////////////// 簡易タグの処理
		//////////////////////////////////////////// editor()メソッドでHTMLを生成して返す
		}else if(c=='('){
			/*
			 * 
			 * ＨＴＭＬ簡易タグかどうか調べて処理する
			 * 
				(#c=#336699)        (/c)
				(#u)                (/u)
				(#u=1)              (/u)
				(#i)                (/i)
				(#b)                (/b)
				(#j=URL)            (/j)
				(#s=12px)           (/s)
				(#g=URL)
				(#a)                (/a)
				(#t)				(/t)
				(#w)				(/w)
				(#m) -- タブ表示
			*/
			if( (c=nextChar()) == 0 ){	// バッファエンドだがデータがあるので（次回の呼び出しで EndOfBuffer 例外が発生する）
				tkn	=	"(";
				id	= DATA;
				return	id;
			}else if(c!='#'){
				//
				// (# でなければ通常のデータとする
				putBackChar();	// 一文字戻す '(' かもしれないので
				tkn	=	"(";
				id	= DATA;
				return	id;
			}
			/*
			 * まだデータあるか
			 */
			c = nextChar();
			if(c== 0){
				tkn	=	"(#";
				id	= DATA;
				return	id;
			}
			/*
			 * 書式指示子を処理する
			 * c,u,s,b,g,i,j,a,p,t,z それ以外、に基準化するため
			 * match() で調べる．指示子でなければ '*' が返る
			 */
			char ch = match( c );
			if(ch=='*'){			// エラーの場合
				/*
				 * 書式指示子ではないケース
				 */
			    StringBuffer	bf = new StringBuffer(2048);
				bf.append('(');		// 通常の文字列として
				bf.append('#');
				bf.append( c );
				tkn	= data(bf);
				id	= DATA;
				return id;
			}
			/*
			 * 書式指示子にマッチしているのでさらに確かめる
			 * ) が現れるまでデータを取得する
			 * ここでは(#○= ･････ ) 内のパラメータを文字列 str に取得する
			 * 
			 * 最初の '(#' は捨てる
			 * 種別は ch に残っている
			 * str にパラメータを取得するが、空文字列のこともある
			 */
			int				pos = p;
			StringBuffer	bf 	= new StringBuffer(2048);
			char 			x;
			while( ((x=nextChar()) != 0)){
				if( x == ')'){
					/*
					 *  最後の ')' は捨てる
					 */
					break;
				}else if((x!='=')&&(x!='＝')&&( !isWhiteSpace(x)) ){
					/*
					 * 空白と等号は捨ててそれ以外を採取する 
					 */
					bf.append(x);
				}
			}
			/*
			 * param にパラメータ文字列を得る（種別はch）
			 */
			String	param	= "";
			if(bf.length() > 0)	param	= bf.toString();
			/*
			 * 
			 *  さらに細かく解析してＨＴＭＬを作成して tkn に入れる
			 */
			try{
				/*
				 * ch（種別）、parameter（パラメータ）から (#････) ～(/･･) の中間部分である対象文字列を求めて target にセットする
				 * editor() はtagrget をセットし、かつそれを埋め込むためのHTMLを作成して返す
				 * 
				 * 例示：(#c=#006600)abc(/c)　
				 * 		種別[ch]					= c
				 * 		パラメータ[parameter]	= #0066cc
				 * 		対象文字列[target] 		= abc 
				 */
				
				/* ******************************************************/
			    tkn	=	editor(ch,param);// html を返す（target 設定済み）
				
			    if(LOG.fa) {
					LOG.println("■■■tkn■■■");
					LOG.println(tkn);
					LOG.println("■■■tkn■■■");
				}			    
				/* ******************************************************/
			    
			}catch(PmlTokenException e2){
				throw e2;
			}
			id	=	HTMLS;
			
			//////////////////////////////////////////// 簡易タグ ここまで

		}else if(c=='~'){
			/*
			 * 機能のエスケープ文字化どうか調べて処理する
			 * 
			 * ~##  ~(#   ~(/ は各々の機能のエスケープとみなす
			 * ~自体は削除しせず，表示直前に上位モジュールで削除する
			 */
			StringBuffer	bf = new StringBuffer(2048);
			int 	pos	= 	p;
			char	ch1	=	nextChar();
			char	ch2	=	nextChar();

			if((ch1=='#')&&(ch2=='#')){
				/*
				 * ## が続くなら通常の文字として扱う
				 */
			    bf.append("~##");
				tkn	= data(bf);
				id	= DATA;

			}else if((ch1=='(')&&(ch2=='#')){
				/*
				 * (# が続くなら通常の文字として扱う
				 */
			    bf.append("~(#");
				tkn	= data(bf);
				id	= DATA;
			
			}else if((ch1=='(')&&(ch2=='/')){
				/*
				 * (/ が続くなら通常の文字として扱う 
				 */
			    bf.append("~(/");
				tkn	= data(bf);
				id	= DATA;

			}else{
				/*
				 * それ以外はタダの文字として扱う
				 */
			    p = pos;
				bf.append('~');
				tkn	= data(bf);
				id	= DATA;
			}
		}else{
			/*
			 * 次にデータ以外のもの（#,(,~）が現れるまでの文字をbfに取り込む
			 */
			StringBuffer	bf = new StringBuffer(2048);
			bf.append(c);
			tkn	= data(bf);
			id	= DATA;
		}
		return	id;
	}
	/**
	 * 確実にデータ文字列と判定できるところまで全てを取り出して返す
	 * 
	 * @param bf
	 * @return
	 */
	private	String	data(StringBuffer bf ){
		//System.out.println("data");
		//
		char c;
		while( ((c=nextChar()) != 0)){
			if( (c != '#') && (c!='(') && (c!='~') ){
				bf.append(c);
			}else{
				putBackChar();
				break;
			}
		}
		return bf.toString();
	}

	//	フォーマットのHTMLを返す
	//
	private	String	mkFormat() throws PmlTokenException {	
		if(DBG.fa) DBG.println("class PmlToken #mkFormat() : フォーマットのHTMLを返す の先頭です");
		//
		StringBuffer	bf	= new StringBuffer(1024);
		char			c	= 0;
		int				brk	= 0;
		// 
		// １行分のデータをバッファ(bf) に取り込む　-- 行末の改行文字も読み取りバッファに入れずに捨てる
		// 
		while( (c=nextChar()) != 0  ){
			if(Character.isISOControl(c)){		// 最初に改行が出てくるまでをフォーマットデータとして取り出す
				c = nextChar();					// Windows の場合 cr+lf で改行なのでこれに対処する
				if(c==0){						// つまり２文字目の lf があれば読み込んで捨ててしまう
					break;
				}else if(Character.isISOControl(c)){
					break;
				}
				putBackChar();	// １文字もどして
				break;			// それ以降は無視する
			}
			bf.append(c);
		}
		//
		tkn 	= (bf.toString()).trim();
		
		if(tkn.length() !=0){
			makeStyle(tkn);				// ## 以下に指定があればパースしてstyle に設定する
		}else{							
			makeStyle(" ");				// なければ、現在の style をそのまま使うが、mkStyleを実行する必要がある
		}
		///////////////////
		//
		//  ここでHTMLを生成する
		//
		////////////////////
		/* フローモードかどうか記録しておく */
		setFlowMode(formatStyle.isFlow());
		//
		id			= FORMAT;
		String str	= getFormat();	// 設定されたフォーマットスタイルからＨＴＭＬを生成する
		return str;
	}
	/**
	 * フォーマット指定をパースしてフォーマットスタイルオブジェクトの値を更新する
	 * 
	 * 例示：　imgbox = http://aaa.com/image.gif,top
	 * 
	 * @param ss	１行分の書式指定文字列全体
	 * @throws PmlTokenException
	 */
	void makeStyle(String ss) throws PmlTokenException {
		if(DBG.fa) DBG.println("class Exam#makeStyle() : ■フォーマット指定をパースしてスタイルオブジェクトの値を更新する の先頭です");
		//
		//
		boolean		imgboxFlag	= false;	// イメージボックスの指定があったときtrue
		boolean		flowFlag	= false;	// フローの指定があったときtrue（初期値は false)
		/*
		 * 　## だけの場合（ss==" ")の時、flow ON とする
		 */
		if(isEmptyTrimed(ss)){
		    flowFlag	=	true;
		}
		/*
		 * 書式に初期値を埋める
		 */
		formatStyle.setDefault();		
		/*
		 * 書式パーサークラスに書式指定文字列全体を渡して初期化 
		 */
		StyleToken	stoken	= new StyleToken(ss,p,formatStyle);
		/*
		 * トークン種別に初期値
		 * 
		 */
		int			id	= 1;
		try{
		  /* ss =" " ならid には -1 が返るので、これ以下を実行しない */
		  while((id=stoken.getToken()) > 0){
		    /*
		     * ○○=×× という指定で，id には，○○を示す種別コードを得ている
		     * また，tknには ×× がそのまま入っている
		     */  
			String	tkn 	= stoken.tknSTR();		// 
			// id はトークン種別またはバッファエンド(-1)
			switch(id){
			  case	FormatStyle.IMGBOX:		// 
				formatStyle.set_imgbox(tkn);
				imgboxFlag = true;
				break;
				//
			  case	FormatStyle.INDENT:
				formatStyle.set_indent(tkn);
				break;
				//
			  case	FormatStyle.STYLE:		// 
				formatStyle.set_style(tkn);
				break;
				//
			  case	FormatStyle.VARIANT:	// 
				formatStyle.set_variant(tkn);
				break;
				//
			  case	FormatStyle.WEIGHT:		// 
				formatStyle.set_weight(tkn);
				break;
				//
			  case	FormatStyle.SIZE:		// 
				formatStyle.set_size(tkn);
				break;
				//
			  case	FormatStyle.HEIGHT:		// 
				formatStyle.set_height(tkn);
				break;
				//
			  case	FormatStyle.TYPE:		// 
				formatStyle.set_type(tkn);
				break;
			  	//
			  case	FormatStyle.COLOR:	// 
				formatStyle.set_color(tkn);
				break;
			  	//
			  case FormatStyle.CODE:
			    formatStyle.set_code();
			    break;
			    
			  case	FormatStyle.ALIGN:	// 
				formatStyle.set_align(tkn);
				break;
				//
			  case	FormatStyle.FLOW:
				if(tkn.equals(FormatStyle.FLOW_NO)){
				    /*
				     * FLOW_NO ならばflow指定のクリア
				     */ 
				    flowFlag	= false;
				}else{
				    formatStyle.set_flow(tkn);
				    flowFlag	= true;
				}
				break;
				//
			  case	FormatStyle.BASE:	// 
				//if(DBG.fa) DBG.println("base URL = " + tkn);
				formatStyle.set_base(tkn);
				break;
			  case	FormatStyle.ALIAS:	// 
				//if(DBG.fa) DBG.println("class PmlToken #makeStyle() : case	FormatStyle.ALIAS: の先頭です");
				String	akey = stoken.tknKEY();		// alias のときだけ意味を持つ
				formatStyle.set_alias(akey,tkn);
				break;
				//
			  case	FormatStyle.EXP:	// 
				//if(DBG.fa) DBG.println("class PmlToken #makeStyle() : case	FormatStyle.EXT: の先頭です");
				break;
				//
			 default:
			  	
				break;
			}
		  }
		}catch(PmlTokenException e){
			throw  e;// 語順やパラメータ指定の有無などのミス
		}
		// imgbox をクリアーするかどうか（指定がなければクリアする）
		if(!imgboxFlag)		formatStyle.clearImgbox();
		
		// flow   をクリアーするかどうか（## だけの場合を除いて、指定がなければクリアする）
		if(!flowFlag){
		    formatStyle.clearFlow();
		}
	}
	/*
	// alias として正しいかチェックする
	public	void	check_alias(String str,String fpath) throws PmlTokenException {
		//
		// フォーマットに間違いがあれば例外が発生する
		
	    ParserInitializer	ckPI	=	(ParserInitializer)pi.clone();
	    ckPI.setText(str);
	    ckPI.setTemplatePath(fpath);
	    
	    //PmlToken	wk	= new PmlToken(str,fpath,imgURL,sysImgUrl,imgAbsPath, sysImgAbsPath, htmlFlag);
	    PmlToken	wk	= new PmlToken(ckPI);
	    try{
			wk.makeStyle(str);
		}catch(PmlTokenException e){
			String	s	= e.getMessageString();
			int		val	= p + e.getMessageValue();
			throw new PmlTokenException(val,s);
		}
	}
	*/
	//  標準フォーマットを返す
	public	String	getSTDFormat(){
		//
		Hashtable	htb	= new Hashtable(10);
		FormatStyle	fs	= new FormatStyle(imgURL,sysImgUrl, imgAbsPath, sysImgAbsPath, initFormats);
		String stdHtml	= templateBox.get("std");
		//
		fs.putHash(htb);
		htb.put("_htmls","%_htmls%");				// %_html% をそのままにしておく工夫．ここはデータが入る部分なので
		Replace rp	= new Replace(stdHtml,htb);
		String fomt = rp.subst();
		return	fomt;
	}
	//
	//　フォーマットhtml を返す
	//
	public	String	getFormat(){
		if(DBG.fa) DBG.println("class PmlToken #getFormat() : フォーマットhtml を返す の先頭です");
		//
		String		key;
		String		sourceHtml= "";
		String  	html;
		Hashtable	htb	= new Hashtable(10);
		formatStyle.putHash(htb);						// スタイル設定値をハッシュに移す
		htb.put("_htmls","%_htmls%");			// もとのままにしておく工夫

		//
		String pos	=	formatStyle.get_position();
		if(pos.length()==0){
			sourceHtml	= 	templateBox.get("std");
		}else if(pos.equals(FormatStyle.imgboxKWD[0])){		// top
			sourceHtml	= 	templateBox.get("g-top");
		}else if(pos.equals(FormatStyle.imgboxKWD[1])){		// bottom
			sourceHtml	= 	templateBox.get("g-bottom");
		}else if(pos.equals(FormatStyle.imgboxKWD[2])){		// left
			sourceHtml	= 	templateBox.get("g-left");
		}else if(pos.equals(FormatStyle.imgboxKWD[3])){		// right
			sourceHtml	= 	templateBox.get("g-right");
		}
		Replace rp	= new Replace(sourceHtml,htb);
		String fomt = rp.subst();
		return	fomt;
	}	//
	//
	//
	
	char	match(char c){
		//System.out.println("match(char c)");
		//
		if((c=='c')||(c=='C')||(c=='ｃ')||(c=='Ｃ'))	return	'c';
		if((c=='u')||(c=='U')||(c=='ｕ')||(c=='Ｕ'))	return	'u';
		if((c=='s')||(c=='S')||(c=='ｓ')||(c=='Ｓ'))	return	's';
		if((c=='b')||(c=='B')||(c=='ｂ')||(c=='Ｂ'))	return	'b';
		if((c=='g')||(c=='G')||(c=='ｇ')||(c=='Ｇ'))	return	'g';
		if((c=='i')||(c=='I')||(c=='ｉ')||(c=='Ｉ'))	return	'i';
		if((c=='j')||(c=='J')||(c=='ｊ')||(c=='Ｊ'))	return	'j';
		if((c=='a')||(c=='A')||(c=='ａ')||(c=='Ａ'))	return	'a';
		if((c=='p')||(c=='P')||(c=='ｐ')||(c=='Ｐ'))	return	'p';
		if((c=='t')||(c=='T')||(c=='ｔ')||(c=='Ｔ'))	return	't';
		if((c=='w')||(c=='W')||(c=='ｗ')||(c=='Ｗ'))	return	'w';
		if((c=='z')||(c=='Z')||(c=='ｚ')||(c=='Ｚ'))	return	'z';
		if((c=='m')||(c=='M')||(c=='ｍ')||(c=='Ｍ'))	return	'm';
		return	'*';	// error
	}
	//
	// ターゲット部分を切り出す
	//
	//  	c   		= 種別を表す文字
	//  	param		= パラメータ
	//　		targetStr 	= ターゲット文字列
	//
	String	editor(char c,String param) throws PmlTokenException {
		if(DBG.fa) DBG.println("class PmlToken #editor() : ターゲット部分を切り出す の先頭です");
		//
		String	targetStr	= "";
		try{
			switch(c){
			  case	'j':
				targetStr	= targetString('j');
				tkn	=	link(param,targetStr);
				break;
			  case	'c':
				targetStr	= targetString('c');
			    tkn	=	color(param,targetStr);
				break;
			  case	'u':
				targetStr	= targetString('u');
			    tkn	=	uline(param,targetStr);
				break;
			  case	's':
				targetStr	= targetString('s');
			    tkn	=	fontSize(param,targetStr);
				break;
			  case	'b':
				targetStr	= targetString('b');
			    tkn	=	fontWeight(param,targetStr);
				break;
			  case	'i':
				  targetStr	= targetString('i');
				  tkn	=	fontItalic(param,targetStr);
				  break;
			  case	'g':
				tkn	=	graphic(param);
				break;
			  case	'a':
			    targetStr	= ""; // ターゲットはない
				tkn	=	anch(param,targetStr);
				  break;
			  case	'p':
			  	targetStr	= ""; // ターゲットはない、パラメータもない（str=""）
				tkn	=	para(param,targetStr);
				break;
			  case	't':
				/*
				 * タイピング評価基準(t) 
				 * parameter には評価項目名
				 * targetStr には評価基準（数行分）
				 * 
				 */
			    targetStr	= targetString('t');
				tkn	=	typing(param,targetStr);
				break;
			  case	'w':
				/*
				 * 入力テスト評価基準(w)
				 * parameter には評価項目名
				 * targetStr には評価基準（数行分）
				 * 
				 */
				targetStr	= targetString('w');
				tkn	=	typing(param,targetStr);
				break;

			  case	'z':
			  	targetStr	= ""; // ターゲットはない、パラメータもない（parameter=""）
				tkn	=	space(param,targetStr);
				break;
				
			  case 'm' :
				tkn = tabPage(param);  
				break;
				
			  default:
			    throw (new PmlTokenException( p, "文字編集指示に使えない文字が使われています．"));
				//break;
			}
		}catch(PmlTokenException e){
			throw e;
		}
		id	= c;
		return tkn;
	}
	//
	// 文字列 htmls の中の置き換え文字を、ハッシュテーブル htb の内容で
	// 書き換えた文字列を返す
	//
	// PrintView の PrintString() はデータを行データとして入出力する．
	// その際、println() を使うので、最後に改行コードがついてしまう．
	// そこで、print() を使うように変更できるスイッチを設けた．
	//
	// 修飾指示語は行内に埋め込むので、改行がついているとそこで改行されてしまう
	// replace() はデフォルトでは print() を使うよう指示する．
	//
	// フォーマットスケルトンへの置き換えで assemble() から呼ばれるときは
	// assemble() で boolean crlf へtrue を指定している．
	//
	String	replace(String htmls,Hashtable htb){
		return replace( htmls, htb,false);
	}
	String	replace(String htmls,Hashtable htb,boolean crlf){		//System.out.println("replace -----> ");
		StringWriter 	sw 	= new StringWriter(10240);
		PrintWriter		out	= new PrintWriter(sw);
		PrintView		pv	= new PrintView(out);
		if(!crlf)		pv.crlf_off();
		pv.PrintString(htb,htmls,new OptionPrint());
		//
		String s = (sw.getBuffer()).toString();
		return s;
	}
	/**
	 * ターゲット文字列部分を取り出す
	 * 
	 * @param ch
	 * @return
	 * @throws PmlTokenException
	 */
	String targetString(char ch) throws PmlTokenException {

	    StringBuffer	bf		= new StringBuffer(1024);
		char			c;
		boolean		flag	= false;
		while( (c=nextChar()) != 0  ){
			/*
			 * 終端タグが出現するまでバッファに加える
			 * 終端タグは読み捨てる
			 */
		    if(c=='('){
				if(atEnd(ch)){
					nextChar();	//  '/'　　よみ捨て
					nextChar();	//  ch
					nextChar();	//  ')'
					flag	= true;
					break;
				}else{
					bf.append(c);
				}
			}else if(c=='~'){
			    /*
			     * エスケープされた指示子を処理する
			     * 　　~(/
			     *     ~(#
			     *     ~##
			     *   について，データとしてバッファに入れる
			     */
			    int	pos	= p;
				char	c1	= nextChar();
				char	c2	= nextChar();
				p			= pos;	// 復元
				//
				if( (c1=='(')&&(c2=='/') ){
					bf.append("~(/");		//  ~(/ をデータに加える
					nextChar();				//  '('　　よみ捨て
					nextChar();				//  '/'　　よみ捨て
				}
				if( (c1=='(')&&(c2=='#') ){
					bf.append("~(#");		//  ~(# をデータに加える
					nextChar();				//  '('　　よみ捨て
					nextChar();				//  '#'　　よみ捨て
				}
				if( (c1=='#')&&(c2=='#') ){
					bf.append("~##");		// ~##  をデータに加える
					nextChar();				//  '#'　　よみ捨て
					nextChar();				//  '#'　　よみ捨て
				}
			}else{
			    /*
			     * 部分文字列に加える
			     */
				bf.append(c);
			}
		}
		if(flag==false){ throw (new PmlTokenException(   p, "(/" + ch + ") がありません" )); }
		//
		String	str	= "";
		if(bf.length()>0)	str	= bf.toString();
		return	str;
	}
	/**
	 * 終端タグかどうか判定
	 * 先読みして文字列が終端文字列かどうか調べる
	 * バッファ位置は変化しない
	 * 
	 * @param ch
	 * @return
	 */
	boolean	atEnd(char ch){
		//
		int		pos	= p;							// p を保存
		char	c1	= nextChar();
		char	c2	= nextChar();
		char	c3	= nextChar();
		//
		p	=	pos;								// p を復元
		if( (c1=='/')&&(c2==ch)&&(c3==')') ){
			return	true;
		}
		return	false;
	}
	
	/**
	 * タイプ練習評価基準
	 * @param str
	 * @param targetStr
	 * @return
	 */
	String	typing(String str, String targetStr) throws PmlTokenException {
	    
	    makeCriterion(str,targetStr);
	    
	    StringBuffer	sbf	=	new	StringBuffer(1000);
	    typeHeader(sbf);
	    typeBody(sbf);
	    typeTail(sbf);
	    if(DBG.fa){
	        DBG.println("");
	        DBG.println("■PmlToken #typing");
	        DBG.println(sbf.toString());
	        DBG.println("");
	    }
	    
		target	= "";		// 実は ""
	    return	sbf.toString();
	}
	
	/**
	 * テーブルのヘッダ部分を作成する
	 * @param bf
	 */
	void	typeHeader(StringBuffer bf){

	    String		html	= templateBox.get("$t01");
	    Hashtable	ht		= new Hashtable(10);
	    ht.put("_target", criterionName);
	    bf.append( replace(html,ht) );

	}
	/**
	 * テーブルの末尾を作成する
	 * @param bf
	 */
	void	typeTail(StringBuffer bf){

	    String		html	= templateBox.get("$t03");
	    bf.append( html );
	    
	}
	/**
	 * テーブルの本体（文字数　点数）を作成する
	 * @param bf
	 */
	void	typeBody(StringBuffer bf){
	    
	    String		html	=	templateBox.get("$t02");
	    /*
	     * criterionは"○○○,□□□"という要素のベクター
	     * ○○○ は評価基準
	     * □□□ は評価点
	     */
	    int			n		=	criterion.size();	
	    Hashtable	ht		=	new	Hashtable(10);
	    
	    for(int i=0; i<n; i++){
	        Csv	cs	=	new	Csv( (String)(criterion.get(i)) );
	        ht.put("_a", cs.get(0));
	        ht.put("_b", cs.get(1));
	        /*
	         * 60点はボーダーなので赤色にする
	         */
	        if(cs.get(1).equals("60")){
	            ht.put("_color","#CC6666");
	        }else{
	            ht.put("_color","#333333");
	        }
	        bf.append( replace(html,ht) );
	    }
	    
	}
	/**
	 * パースして評価基準のベクタを作成する
	 * @param str
	 * @param targetStr
	 * @throws PmlTokenException
	 */
	void	makeCriterion(String str, String targetStr) throws PmlTokenException {
	    
	    /*
	     * 評価基準名（ex. 上一段）をベクタに保存
	     */
	    if(!isEmpty(str)){
	        criterionName	=	str;
	    }else{
	        /*
	         * mikatype ではリストボックスで選ぶので空白にはならない
	         * 空白なのはおおむね入力問題の場合である．
	         * そこで，空白ならばここで定型文字列をセットしておく
	         */
	        str	=	"タイピング練習";
	        //throw (new PmlTokenException( p, "評価基準名が書かれていない" ));
	    }
	    BufferedReader	in		=	new BufferedReader( new StringReader(targetStr) );
	    String			line;
	    try{
	        Regularizer	rl	=	new	Regularizer();
	        while((line=in.readLine())!=null){
	            /* 
	             * 半角英数に，連続する空白は１個に，trimして返す
	             */ 
	            String			temp	=	rl.toRegular(line);
	            StringTokenizer	stk		=	new	StringTokenizer(temp, " ,/-");
	            /*
	             * 要素は２個
	             */
	            int				size	=	stk.countTokens();
	            if(size!=2){
	                continue;
	                //throw (new PmlTokenException(   p, "評価基準の記述が不完全:" + line ));
	            }
	            /*
	             * 評価カテゴリと評価点をチェック
	             */
	            String	level	=	stk.nextToken();
	            if(!rl.isDigit(level)){
	                throw (new PmlTokenException(   p, "数字でない評価カテゴリ:" + level ));
	            }
	            String	grade	=	stk.nextToken();
	            if(!rl.isDigit(grade)){
	                throw (new PmlTokenException(   p, "数字でない評点:" + grade ));
	            }
	            /*
	             * CSVにして記録する
	             */
	            String	csv	=	level + "," + grade;
	            criterion.add(csv);
	        }
	    }catch(IOException e){
	        e.printStackTrace();
	    }
	    // 評価基準がない場合は例外を投げる
	    
	    if(criterion.size()==0){
	        //setDefaultLevel();
	        throw (new PmlTokenException(   p, "採点基準が書かれていない" ));
	    }
	}
	/**
	 * 規定の評価基準を設定する
	 */
	void	setDefaultLevel(){
	    
	    int	n	=	defaultCriterion.size();
	    for(int i=0; i<n; i++){
	        String	temp	=	(String)defaultCriterion.get(i);
	        criterion.add(temp);
	    }
	}
	
	
	//
	// アンカー
	//  targeStr ="" を受け取っている
	String	anch(String parmStr,String targeStr) throws PmlTokenException {
		//System.out.println("color");
		//
		if(parmStr.length() == 0 ){  throw (new PmlTokenException(   p, "アンカー名がありません．" )); }
		//
		Hashtable	htb	= new Hashtable(10);
		htb.put("_string","%_string%");			// 変換されて _string とならないための工夫
		target	= targeStr;						// 対象文字列、ここでは常に "" を受け取っている
		htb.put("_anchName",parmStr);			// アンカー名
		//
		String	htmls	= templateBox.get("$a");			// ancher html の雛型をハッシュから得る
		return	replace(htmls,htb);		
	}
	//
	// 改行
	//   parmStr, targetStr 共に "" を受け取っている．
	//   スタイルの整合性のためと、今後の拡張のためこのような形式にした
	String	para(String parmStr,String targeStr) throws PmlTokenException {
		
		target	= targeStr;	// 実は ""
		return	"<br>";
		
	}
	//
	// 空白
	//   parmStr, targetStr 共に "" を受け取っている．
	//   スタイルの整合性のためと、今後の拡張のためこのような形式にした
	String	space(String parmStr,String targeStr) throws PmlTokenException {
		//
		// 設定されている文字のサイズに応じた長さを計算
		// spacer.gif をその長さで表示することで空白を作る
		String	fontsize	= formatStyle.get_size();				// StyleToke style では英字は英小文字に正規化してある
		String	dig			= regularizer.toDigit(fontsize);		// 数字
		String	uni			= regularizer.unitString(fontsize);		// 単位
		//
		String	wd	= "14";	// 一応標準とする
		if((uni==null)||(uni.length()==0)){
			if((dig!=null)&&(dig.length()>0)){
				wd = String.valueOf( Integer.parseInt(dig) * 2  + 6);
			}
		}else if((uni.equals("px"))&&(dig!=null)&&(dig.length()>0)){
			wd	=	dig;
		}
		Hashtable	htb	= new Hashtable(10);
		htb.put("_string","%_string%");		// 変換されて _string とならないための工夫
		htb.put("_wd",wd);					// 1文字の幅
		String	htmls		= templateBox.get("$z");
		target	= targeStr;		// 実は ""
		return	replace(htmls,htb);
		
	}
	
	//
	// 埋め込みグラフィックス
	String	graphic(String urldata) throws PmlTokenException {
		if(DBG.fa) DBG.println("■ class PmlToken #graphic() : url=" + urldata);
		//
		if(urldata.length() ==0) {  throw (new PmlTokenException( p, "埋め込みグラフィックス指定の書き方に間違いがあります．" )); }
		/*
		 * (#g=filename,1,○○○)　でボーダーとaltを指定できるようにした．
		 * ボーダーとaltの順序は変更不可  
		 */
		
		Hashtable	htb	= new Hashtable(10);
		htb.put("_border", "0");
		htb.put("_alt", "");
		/*
		 * わざとチェックを甘くした．HTMLなので間違っていれば無視されるだけ
		 */
		Csv	cs		=	new Csv(urldata, ",");
		int size	=	cs.size();
		if(size==1){
			/* なにもしない */			
		}else if(size==2){
			htb.put("_border", cs.get(1));
			
		}else if(size==3){
			htb.put("_border", cs.get(1));
			htb.put("_alt", cs.get(2));
			
		}
		if(DBG.fa) DBG.println("□ class PmlToken #graphic() : url:csv.get(0) =" + cs.get(0));
		String	url	=	cs.get(0);		
		if(url.charAt(0)=='@'){
			// 先頭に＠が付いていればベースＵＲＬを付加する
			url	= formatStyle.get_base() + url.substring(1);
			if(DBG.tr) DBG.println("formatStyle.get_base() + url.substring(1)=" + url);
		
		}else if(url.charAt(0)=='&'){
		    
		    /*
			 * ファイルをユーザーのグラフィックスディレクトリへコピーする
			 * URLはユーザーのグラフィックスディレクトリを指すようにする
			 * ファイルは　copySysGraphics() によって実際にコピーされる
			 */
			String	fileName 	=	url.substring(1);
			url 				= 	imgURL + fileName;
			copySysGraphics(fileName);
			
			if(DBG.fa){
			    DBG.println("□ class PmlToken ファイルをコピー");
			    DBG.println("□ PmlToken #graphic() :ファイルURL =" + url);
			}

		}else if(!isURL(url)){
			/* 
			 * 有効なURLでなければファイル名だけの指定とみなして、
 			 * ファイルアップロードディレクトリへのURLを加える
             * サーバーアドレスに関係なく画像を表示できる
			 */
			url		= 	imgURL + url;		// 相対・完全パスで起点を加える

			if(DBG.fa){
			    DBG.println("□ PmlToken #graphic() :ファイルアップロードディレクトリへのパスを加える url=" + url);
			}
		}
		/*
		 * ターゲットを設定
		 */
		target			= url;
		htb.put("_string",url);	// 書き変え用
		
		/*
		 * パラメータを埋め込んだHTMLを返す
		 */
		String	htmls		= templateBox.get("$g");		// 雛型をハッシュから得る
		String	resultHtml	= replace(htmls,htb);
		
		if(DBG.fa){
			DBG.println("・ urldata    =" + urldata);
			DBG.println("・ htmls      =" + htmls);
			DBG.println("・ resultHtml =" + resultHtml);
		}
		return	resultHtml;
	}
	/**
	 * システムグラフィックスファイルが、現在のユーザーディレクトリに存在するかどうか調べて
	 * 存在しなければコピーする．
	 * 
	 * @param fileName		対象のシステムグラフィックス
	 */
	void	copySysGraphics(String	fileName){
	    if(DBG.fa) DBG.println("□copySysGraphics()");
	    
		/* ユーザーのグラフィックスディレクトリが未設定なら検査できないので何もしない */
		if(isEmpty(imgAbsPath)){
		    if(DBG.fa) DBG.println("imgAbsPath is Empty .");
		    return;
		}
		
		/* システムグラフィックスがユーザーディレクトリになければコピーする */
		File	fp	=	new File(imgAbsPath + fileName);
		if(!fp.exists()){
		    String	destPath	=	imgAbsPath 		+ fileName;
			String	sourcePath	=	sysImgAbsPath	+ fileName;
		    
			if(DBG.fa){
			    DBG.println("source = " + sourcePath);
			    DBG.println("dest   = " + destPath);
			}
			
			try{
				copyBinryFile(sourcePath,destPath);
				
			}catch(IOException e){
			    DBG.println("■ PmlToken #copySysGraphics() : fail to copy !");
				e.printStackTrace();
			}
		}else{
		    if(DBG.fa){
			    DBG.println("すでにあるのでコピーしない");
			    DBG.println("  source = " + sysImgAbsPath	+ fileName);
			    DBG.println("  dest   = " + imgAbsPath + fileName);
			}
		}
		if(DBG.fa) DBG.println("==============");
	}
	/**
	 * ファイルのコピー（バイナリー版）
	 * If the dst file does not exist, it is created
	 */ 
	public void  copyBinryFile(String srcPath, String dstPath) throws IOException {
		//
		File	src	= new File(srcPath);
		File	dst	= new File(dstPath);
		/*
		 * コピー先のディレクトリがなければ作る
		 */
		File	pr	=	new	File(dst.getParent());
		if(!pr.exists()){
		    pr.mkdirs();
		}
		
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);
		// Transfer bytes from in to out
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}	
	
	// アンダーライン
	String	uline(String num,String blk) throws PmlTokenException {
		//System.out.println("uline");
		//
		Hashtable	htb	= new Hashtable(10);
		htb.put("_string","%_string%");		// 変換されて _string とならないための工夫
		target	= blk;		// 対象文字列
		//
		if(num.length() > 0){
			htb.put("_no",num);
			
		}else{
			htb.put("_ulineflag","1");
		}
		String	htmls	= templateBox.get("$u");	// link html の雛型をハッシュから得る
		//if(sz==1) return	htmls;
		return	replace(htmls,htb);
	}
	// 強調
	String	fontWeight(String pstr,String blk) throws PmlTokenException {
		//System.out.println("fontWeight");
		//
		if(pstr.length() > 0 ){  throw (new PmlTokenException(   p, "強調指定の書き方に間違いがあります．")); }
		target	= blk;			// 対象文字列
	  	//
		String	htmls	= templateBox.get("$w");	// fontWeight html の雛型をハッシュから得る
		return	htmls;
	}
	// イタリック
	String	fontItalic(String pstr,String blk) throws PmlTokenException {
		//System.out.println("fontWeight");
		//
		if(pstr.length() > 0 ){  throw (new PmlTokenException(   p, "斜体指定の書き方に間違いがあります．")); }
		target	= blk;			// 対象文字列
		//
		String	htmls	= templateBox.get("$i");	// fontWeight html の雛型をハッシュから得る
		return	htmls;
	}
	// フォントサイズ
	String	fontSize(String pstr,String blk) throws PmlTokenException {
		//System.out.println("fontSize");
		//
		if(pstr.length() == 0 ){  throw (new PmlTokenException(   p, "フォントサイズ指定の書き方に間違いがあります．" )); }
		//
		Hashtable	htb	= new Hashtable(10);
		htb.put("_string","%_string%");			// 変換されて _string とならないための工夫
		target	= blk;							// 対象文字列
		//
		Csv	prm		= new Csv(pstr,",");		// 各要素の両端の空白は取られる
		if(prm.size() > 0){
			String	sz 		= prm.get(0);
			String	value	= regularizer.toDigit(sz);			// 数字
			String	unit	= regularizer.unitString(sz);		// 単位
			if(isEmpty(value)){ throw (new PmlTokenException( p, "フォントサイズを指定してください." )); }
			// unit は px に固定なので無視する
			//
			htb.put("_size", value);					// フォントサイズ
	  	}
		if(prm.size() > 1){
			String	hsz		= prm.get(1);
			String	value	= regularizer.toDigit(hsz);			// 数字
			String	unit	= regularizer.unitString(hsz);		// 単位
			if(isEmpty(value)){ throw (new PmlTokenException( p, "行間を指定してください." )); }
			// unit は % に固定なので無視する
			//
			htb.put("_lineheight", value);				// 行間　％単位
		}
		//
		String	htmls	= templateBox.get("$s");			// color html の雛型をハッシュから得る
		return	replace(htmls,htb);
	}
	// カラー指定
	String	color(String pstr,String blk) throws PmlTokenException {
		//System.out.println("color");
		//
		if(pstr.length() == 0 ){  throw (new PmlTokenException(   p, "カラー指定の書き方に間違いがあります．" )); }
		//
		Hashtable	htb	= new Hashtable(10);
		htb.put("_string","%_string%");			// 変換されて _string とならないための工夫
		target	= blk;			// 対象文字列
		htb.put("_color",pstr);	// カラーコード
	  	//
		String	htmls	= templateBox.get("$c");	// color html の雛型をハッシュから得る
		return	replace(htmls,htb);
	}
	// リンクの埋め込み
	String	link(String pstr,String blk) throws PmlTokenException {
		//System.out.println("link");
		//
		Hashtable	htb	= new Hashtable(10);
		htb.put("_string","%_string%");		// 変換されて _string とならないための工夫
		if(pstr.length() ==0){
			throw (new PmlTokenException(   p, "リンクの書き方に間違いがあります．" ));
		}
		Csv	cs	= new Csv(pstr,",");
		target	= blk;		// 対象文字列
		//
		if(cs.size() ==0){
			throw (new PmlTokenException(   p, "リンクの書き方に間違いがあります．" ));
		}
		// 先頭に＠が付いていればベースＵＲＬを付加する
		String	url	= cs.get(0);
		if(url.charAt(0)=='@'){
			url	= formatStyle.get_base() + url.substring(1);
		}

		if(isURLorANCHR(url)){
			htb.put("_url",url);
		}else{
			/*
			 * 正しくないURLの場合はファイル名のみを指定したものと解釈する
			 * 
			 * 資料データの場合は
			 * gethtmlPath() の値をURLの前に付加する
			 * gethtmlPath() はプレビューモードの時のみユーザーのHTMLディレクトリへの
			 * WWWにおける絶対パスを返す．その他のモードでは "" を返す．
			 * 
			 * それ以外ではイメージファイルと同じパスになるように
			 * imgURL を前に付加する
			 */
			if(htmlFlag){
				htb.put("_url",gethtmlPath() + url);
				if(DBG.fa){
					DBG.println("■■ PmlToken #link() : a)gethtmlPath() + url=" + url);
				}				
			}else{
				htb.put("_url",imgURL + url);
				if(DBG.fa){
					DBG.println("■■ PmlToken #link() : b)imgURL + url=" + url);
				}
			}
		}
		
		//
		if(cs.size()==1){
			// アンカーの場合は"_self"とする
			if(url.charAt(0)=='#'){
				htb.put("_target","_self");
			}else{
				htb.put("_target","_blank");	//  _blank をデフォルトとする
			}
		}else if(cs.size()==2){
			String tg	= cs.get(1);
			if((tg.equals("_self"))||(tg.equals("_blank"))||(tg.equals("_top"))||(tg.equals("_parent"))){
				htb.put("_target",tg);
			}else{
				if(tg.equals("0")){
					htb.put("_target","_self");
				}else if(tg.equals("1")){
					htb.put("_target","_blank");
				}else if(tg.equals("2")){
					htb.put("_target","_top");
				}else if(tg.equals("3")){
					htb.put("_target","_parent");
				}else{
					throw (new PmlTokenException(   p, "リンクのtarge指定は、0(_self),1(_blank),2(_top),3(_parent)のどれかです" ));
				}
			}
		}else{
			throw (new PmlTokenException(   p, "リンクに指定できるパラメータは、URLとtargeの二つです．" ));
		}
	  	//
		String	htmls	= templateBox.get("$$");	// link html の雛型をハッシュから得る
		return	replace(htmls,htb);
	}
	/**
	 * グラフィックスフィルのユーザーURLから htmlのユーザーURLを得る
	 * ひとつ上のディレクトリだから　
	 *　/･･･/･･･/files/ という形のurl から、最後の files/ を除いたもの
	 *　を返す．
	 *　ただし、files/ だけだった場合（＝プレビューでない場合）は "" を返す
	 *
	 * @param urlStr
	 * @return
	 */
	String	gethtmlPath(){
		//if(DBG.fa) DBG.println("imgURL = " + imgURL);
		Csv	cs = new Csv(imgURL,"/",true);
		if(cs.size()<2){
			return	"";
		}
		//if(DBG.fa) DBG.println("Csv size = " + cs.size());
		
		StringBuffer	urls	=	new StringBuffer();
		urls.append("/");
		int	n	= cs.size();
		for(int k=0; k<n-1; k++){
			//if(DBG.fa) DBG.println("str = " + cs.get(k));
			urls.append(cs.get(k));
			urls.append("/");
		}
		
		String	s =	urls.toString();	
		//if(DBG.fa) DBG.println("url = " + s);
		return	s;
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
	//
	// 有効なアンカーかまたはＵＲＬ文字列かどうか調べる
	boolean	isURLorANCHR(String urlStr){
		if(DBG.fa) DBG.println("testURL = " + urlStr);
		//
		// アンカーかどうか
		if(urlStr.charAt(0)=='#'){
			return	true;
		}
		// URLかどうか
		URL		url;
		try{
			url = new URL(urlStr);
		}catch( MalformedURLException e){
			return false;
		}
		return true;
	}	
	//
	// バッファから１文字取って返す．ポインタは＋１される
	//
	private char nextChar(){
		//System.out.print("nextChar");
		//
		char	c = 0;
		if( !EOB() ){
			c = buffer.charAt(p);
			p++;
		}else{
			c = 0;	// バッファが空
			return	 c;	
		}
		return	rglToHan(c);// (,#,/,) を半角にそろえる
	}
	//
	char	rglToHan(char c){
		//System.out.println("match(char c)");
		//
		if((c=='#')||(c=='＃'))		return	'#';
		if((c=='(')||(c=='（'))		return	'(';
		if((c==')')||(c=='）'))		return	')';
		if((c=='/')||(c=='／'))		return	'/';
		//if((c==' ')||(c=='　'))		return	' ';
		return	c;
	}
	//
	// バッファを１文字戻す
	private void	putBackChar(){
		//System.out.println("putBackChar");
		--p;
	}
	//
	// バッファが空かどうか
	boolean	EOB(){
		//System.out.println("EOB : " + p + "/" + max);
		//
		return (p >= max);	// 最後のとき true
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
	// 数字文字列にして返す
    String toDigit(String s){
        if((s == null)||(s.length()==0))    return "";
		//
        StringBuffer bf = new StringBuffer(100);
		int len = s.length();
        for(int i=0; i<len; i++){
            char ch = matchDigit(s.charAt(i));
			if(ch == '*')       return "";
            bf.append(ch);
        }
        return bf.toString();
    }
	//
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
	// 文字列が空かどうかテストする
	//
	boolean isEmpty(String str){
		if(str==null) 			return  true;
		if(str.length()==0)		return  true;
		return false;
	}
	boolean isEmptyTrimed(String str){
		String	s	=	str.trim();
	    if(s==null) 			return  true;
		if(s.length()==0)		return  true;
		return false;
	}
	/*
	 * タブページのHTMLを作成して返す
	 * graphicsのURLは、imgUrl（課題番号別のフォルダ） + filename
	 * ⇒ /user/ea006/kadai/001/000106/abc.gif
	 * 
	 * 指定
	 * (#m=en/aaa.gif,vi/bbb.gif,np/ccc.gif,ch/ddd.gif)
	 * 
	 * param = "en/aaa.gif,vi/bbb.gif,np/ccc.gif,ch/ddd.gif"
	 * 
	 */
	public String tabPage(String param) throws PmlTokenException {
		
		if(DBG.fa){
			DBG.println("■PmlToken#tabPage() : フィールドの値を調べる");
			DBG.println(toString());
		}
		
		String li_a1 = "<li class=\"tab-btn active\">%title%</li>\n";
		String li_a2 = "<li class=\"tab-btn\">%title%</li>\n";
		
		String div_a1 = "<div class=\"tab-content active\">\n";
		String div_a2 = "<div class=\"tab-content \">\n";
		String div_b = "<img src=\"" + imgURL + "%filename%\" width=\"100%_PCNT_%\" style=\"max-width:100%_PCNT_%; height:auto;\">\n";
		String div_c = "</div>\n";
		
		Csv	cs		=	new Csv(param, ",");	// タブごとのデータに分ける
		int size	=	cs.size();				// タブの個数

		var li_titles = new StringBuilder();
		var div_graphics = new StringBuilder();
		
		// 2つのバッファにliとdivのHTMLをそれぞれ追加する
		try {
			for(int i=0; i<size; i++) {
				String title = cs.get(i).split("/")[0];
				String filename = cs.get(i).split("/")[1];
				
				if(i==0) {
					li_titles.append(li_a1.replace("%title%", title));
					div_graphics.append(div_a1);
				}
				else {
					li_titles.append(li_a2.replace("%title%", title));
					div_graphics.append(div_a2);
				}
				
				div_graphics.append(div_b.replace("%filename%", filename));
				div_graphics.append(div_c);
			}
		}catch(Exception e){
			throw new PmlTokenException("マルチタブでパラメータの指定に誤りがあります："+ param);
		}

		target	= param;	// Examによる再帰処理のために設定しておく
		
		return replacement(li_titles.toString(), div_graphics.toString());
	}
	
	public String replacement(String li_tags, String div_tags) {
		
		Hashtable	htb	= new Hashtable(10);	// 書き変え用
		htb.put("_li_tags",li_tags);	
		htb.put("_div_tags",div_tags);	
		
		/*
		 * パラメータを埋め込んだHTMLを返す
		 */
		String	htmls		= templateBox.get("$m");	// 雛型をハッシュから得る
		String	resultHtml	= replace(htmls,htb);

		return	resultHtml;
	}
	@Override
	public String toString() {
		return "PmlToken ["
				+ "\n\t pi=" + pi + ", "
				+ "\n\t imgURL=" + imgURL 
				+ "\n\t imgAbsPath=" + imgAbsPath 
				+ "\n\t sysImgUrl=" + sysImgUrl 
				+ "\n\t sysImgAbsPath=" + sysImgAbsPath 
				+ "\n\t htmlFlag=" + htmlFlag
				+ "\n\t nitialDataFile=" + initialDataFile 
				+ "\n\t buffer=" + buffer 
				+ "\n\t max=" + max 
				+ "\n\t p=" + p 
				+ "\n\t id=" + id 
				+ "\n\t tkn=" + tkn 
				+ "\n\t target=" + target 
				+ "\n\t path_HtmlTemplates="+ path_HtmlTemplates + "]";
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
