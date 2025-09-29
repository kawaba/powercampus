/*
    スタイル項目を記録するクラス
	
	## base = ***** とすると そのURLが有効なら base に設定される
	## で base を指定しなければシステムURLベース（defaultURL）が使われる
	他を指定するには base = http://aaaa.bbb.com のように絶対指定する
    指定したURLの末尾に / がなければ自動的に付加する
	
	HMACが生成するHTMLに埋め込まれるスタイル項目を記録しておく
	コンストラクタは項目値を規定値で生成する
	セッター、ゲッターメソッドがある
	項目値をハッシュに設定して返すメソッドがある
	
	配列に書いた文字列は英小文字でなければならない
	StyleToken で小文字に変換してから比較するため
*/
package epml;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import epml.tools.Csv;
import epml.tools.DBG;
import epml.tools.Regularizer;
//
public class FormatStyle {
	//
	// キーワードテーブル(副指示子コードと並び順をあわせてある)
	static final String	FORMAT_KEYWORD [] = {	"NOT KEY WORD",
												"indent",
												"imgbox",
												"style",
												"variant",
												"weight",
												"size",
												"height",
												"type",
												"color",
												"align",
												"flow",
												"base",
												"alias",
												"code"
											};
	// パラメータを持つ副指示子コード
	public static final int NOT_KEYWORD	= 0;
	public static final int INDENT		= 1;
	public static final int IMGBOX		= 2;
	public static final int STYLE			= 3;
	public static final int VARIANT		= 4;
	public static final int WEIGHT		= 5;
	public static final int SIZE			= 6;
	public static final int HEIGHT		= 7;
	public static final int TYPE			= 8;
	public static final int COLOR			= 9;
	public static final int ALIGN			= 10;
	public static final int FLOW			= 11;	// 改行を<BR>としない形式(パラメータ無し)
	public static final int BASE			= 12;
	public static final int ALIAS			= 13;
	public static final int CODE			= 14;
	public static final int EXP			= 99;	// 展開すべきトークン
	//
	public static final String 	FLOW_NO			= 	"no";
	public static final boolean	DEFAUTL_FLOW	=	false;
	String	stdFolwSize						= "\"" + "700" + "\"";
	//
	// imgbox の配置指定語
	//
	// 2019.6 ＭＳゴシックをメイリオに変更した 
	//
	public static	String	npfont	= "'メイリオ', 'Osaka－等幅'";	// 等幅フォント
	public static	String	pfont	= "''メイリオ, 'Osaka'";		// プロポーショナルフォント
	//
	//  有効な単位文字列
	//
	//  10em ... 文字mの横幅（CSSの定義ではフォントの高さ）
	//  10ex ... 文字xの高さ
	//  10px ... 10ピクセル
	//
	//  10in ... 10インチ（1in=2.54cm）
	//  10cm ... 10センチメートル（1cm=10mm）
	//  10mm ... 10ミリメートル（10mm=1cm）
	//  10pt ... 10ポイント（1pt=1/72in）
	//  10pc ... 10パイカ（1pc=12pt）
	//
	public static	String	unitKWD		[]	=	{"em","ex","px","in","cm","mm","pt","pc","%"};
	public static	String	unitKWD2	[]	=	{"u"};	// indent 1u = 10px 
	//
	//  有効な指定文字列
	public static	String	imgboxKWD	[]	=	{"top","bottom","left","right"};
	public static	String	styleKWD	[]	=	{"normal","italic"};
	public static	String	varianKWD	[]	=	{"normal","small-caps"};
	public static	String	weightKWD	[]	=	{"normal","bold"};
	public static	String	sizeKWD		[]	=	{"xx-small","x-small","small","medium","large","x-large","xx-large","larger","smaller"};
	public static	String	typeKWD		[]	=	{"np","p"};
	public static	String	alignKWD	[]	=	{"left","right","center","justify","inherit"};
	public static	String	heightKWD	[]	=	{"%"};
	
	
	//
	//  インデント、グラフィックス
	//
	String	indent;		// インデントの幅 　	10px（規定値）など　指定する時は indent=10 と単位無しで指定する．
	String	imgbox;		// イメージ領域指定	
	String	imgUrl;		// イメージＵＲＬ		http://abc.com/image.gif 
	String	position;	// イメージの配置		top（規定値）,bottom,left,right
	String	border;		// イメージの枠線		0（規定値）,1
	//
	//  font: style variant weight size / height family 
	//
	String	style;		// normal（規定値：通常）, italic（イタリック）
  	String	variant;	// normal（規定値：通常）, small-caps（小文字を少し小さめの大文字で表す）
	String	weight;		// normal（規定値）, bold
	String	size;		// xx-small, x-small, small, medium（規定値）, large, x-large, xx-large、
			 			// 相対指定として larger, smaller、
						// 絶対単位指定として 10in, 10cm, 10mm, 10pt, 10pc, 
						// 相対単位指定として 10px, 10ex, 10em などを、
						// 割合指定として 120% などを指定
  	String	height;		// テキストの高さを 1.5em, 150% などで指定. 110%（規定値）
	String	type;		// npfont（規定値）, pfont
	//
	//  COLOR: #333333;
	//	text-align: left;
	//
	String		color;		// 文字色
	String		align;		// left（規定値：左寄せ）、right（右寄せ）、center（センタリング）、justify（両端揃え）、inherit（継承）
	String		flow;		// "width=" または "" 
	String		space;		// "_pre"   または "_normal"
	String		flowSize;	// フローモードでの表示幅. ノーマルモードでは""
	//
	Regularizer	rl;			// 文字列正規化クラス
	//
	String		defaultURL;	// デフォルトのＵＲＬ
	String		base;		// 起点のURL

	///////////////////////////////////////////////////////////////
	Hashtable	alias;		// エイリアスを記憶するためのハッシュ
	///////////////////////////////////////////////////////////////

	//
	String		systemBase;		// システムのグラフィックスディレクトリへの絶対パス	/pc/sysimg/
	String		imgBase;		// ユーザーのグラフィックスディレクトリへの相対パス 	file/

	/**
	 * ユーザーグラフィックスディレクトリへの絶対パス
	 *  このパスはプレビュー時にシステムグラフィックスをユーザーグラフィックスディレクトリへコピー
	 * するためのものである．
	 * プレビューから呼び出された時以外は、"" であるから、使用前に必ずチェックをする
	 */
	String	imgAbsPath;	
	
	/**
	 * システムグラフィックスディレクトリへの絶対パス
	 * 	/var/www/html/pc/sysimg/
	 */
	String 	sysImgAbsPath;    	

	/*
	 * コンストラクタ
	 * app_flowSizeは、intで 600 のように指定する
	 */
	public	FormatStyle(String imgURL, String sysBase, String _imgAbsPath, String _sysImgAbsPath, Hashtable initFormats, int app_flowSize){
		//
		systemBase		= 	sysBase;
		imgBase			=	imgURL;
		imgAbsPath		=	_imgAbsPath;
		sysImgAbsPath	=	_sysImgAbsPath;
		//
		rl 				= 	new Regularizer();
		defaultURL		=	"";
		stdFolwSize 	=   "\"" + app_flowSize + "\"";

		setDefault();
		alias			= 	initFormats;	// 初期値としてエイリアス定義が入っている
		//
	}
	public	FormatStyle(String imgURL, String sysBase, String _imgAbsPath, String _sysImgAbsPath, Hashtable initFormats){
		
		this(imgURL, sysBase, _imgAbsPath, _sysImgAbsPath, initFormats, 700);	// 700はstdFolwSizeと同じ

	}	
	//
	// 　パラメータを規定値に設定する
	public	void setDefault(){
		indent		= "20";		// 	単位は px
		imgbox		= "";
		imgUrl		= "";
		position	= "";
		border		= "0";
		//
		style		= "normal";
		variant		= "normal";
		weight		= "normal";
		size		= "14px";		// 単位がなければ 1-7 での指定とみなす
		//
		height		= "120";
		//
		type		= npfont;
		//
		color		= "#333333";
		align		= "left";
		//
		flow		= "width=";		// フローする (width = を指定)
		flowSize	= stdFolwSize;	// フローする時の幅  "" はフローしない時
		space		= "normal";		// フローする (white-space: normal; を指定する. flowしないときは pre )
		base		= defaultURL;	// URL の起点
	}
	//
	// デフォルトＵＲＬは空かどうか
	boolean	isEmptyURL(){
		if((defaultURL==null)||(defaultURL.length()==0)){
			return	true;
		}
		return	false;
	}
	//
	// デフォルトのＵＲＬを設定する（初期化用）
	public	void	set_defaultURL(String s){ defaultURL= s;}
	//
	// デフォルトのＵＲＬをクリアする
	public	void	clear_defaultURL() {
		defaultURL	=	"";
	}
	//
	//  imgbox の指定からurl とposition を取り出してimgUrl とposition に設定する
	//	(例) imgbox = http://aaa.com/image.gif,top   の右辺
	//  
	public	void	set_imgbox(String s){
		//
		imgbox		= 	s;
		Csv	cs		=	new Csv(s,",");						// コンマをデリミッタ.両端の空白は取る
		imgUrl		= 	cs.get(0);
		if(imgUrl.charAt(0)=='@'){
			/*
			 * base 指定による起点の指定
			 * 
			 * 現在のバージョンでは意味がなくなったが互換性のために残してある
			 * 長いＵＲＬを短縮して書けるように，BASE を決めておいた場合，ここでそれを連結する
			 */
		    imgUrl		= 	base + imgUrl.substring(1);		// URL の起点を加える
		
		}else if(imgUrl.charAt(0)=='&'){
			/*
			 * システムグラフィックスを意味する
			 * 
			 * グラフィックスのＵＲＬ（imgUrl）をユーザーグラフィックスディレクトリへのパスに書き換え
			 * システムグラフィックスをそこへコピーしておく
			 * 
			 */
			String	fileName 	=	imgUrl.substring(1);
			imgUrl 				= 	imgBase + fileName;	
			copySysGraphics(fileName);		
			if(DBG.fa){
			    DBG.println("□ FormatStyle ファイルをコピー");
			    DBG.println("□ FormatStyle #graphic() :ファイルURL =" + imgUrl);
			}
		
		}else if(!isURL(imgUrl)){
			/* 
			 * 有効なURLでなければファイル名だけの指定とみなして、
			 * ファイルアップロードディレクトリへのパスを加える
			 * サーバーアドレスに関係なく画像を表示できる
			 */
			imgUrl		= 	imgBase + imgUrl;		// 起点を加える
			//imgUrl		= 	"files/" + imgUrl;		// 起点を加える
		}
		position	= 	(cs.get(1)).toLowerCase();
		border		= 	(cs.get(2)).toLowerCase();

	}
	/**
	 * システムグラフィックスファイルが、現在のユーザーディレクトリに存在するかどうか調べて
	 * 存在しなければコピーする．
	 * 
	 * @param fileName		対象のシステムグラフィックス
	 */
	void	copySysGraphics(String	fileName){

		/* ユーザーのグラフィックスディレクトリが未設定なら検査できないので何もしない */
		if(isEmpty(imgAbsPath))	return;
		
		/* システムグラフィックスがユーザーディレクトリになければコピーする */
		File	fp	=	new File(imgAbsPath + fileName);
		if(!fp.exists()){
		    String	destPath	=	imgAbsPath 		+ fileName;
			String	sourcePath	=	sysImgAbsPath	+ fileName;
		    
			if(DBG.fa){
			    DBG.println("□FormatStyle コピーする");
			    DBG.println("　　source = " + sourcePath);
			    DBG.println("　　dest   = " + destPath);
			}
			
			try{
				copyBinryFile(sourcePath,destPath);
				
			}catch(IOException e){
			    DBG.println("★ FormatStyle #copySysGraphics() : fail to copy !");
				e.printStackTrace();
			}
		    if(DBG.fa){
			    DBG.println("　　FormatStyle：コピー終了");
			}			
			
		}else{
		    if(DBG.fa){
			    DBG.println("□FormatStyle すでにあるのでコピーしない");
			    DBG.println("  source = " + sysImgAbsPath	+ fileName);
			    DBG.println("  dest   = " + imgAbsPath + fileName);
			}
		}
		
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
	}	//
	//
	public	void	set_indent(String s)		{ indent	= s.toLowerCase();}
	public	void	set_style(String s)			{ style 	= s.toLowerCase();}
	public	void	set_variant(String s)		{ variant 	= s.toLowerCase();}
	public	void	set_weight(String s)		{ weight 	= s.toLowerCase();}
	public	void	set_size(String s)			{ size		= s.toLowerCase();}
	public	void	set_height(String s)		{ height	= s.toLowerCase();}
	//
	public	void	set_type(String s)			{ type		= s.toLowerCase();}
	public	void	set_color(String s)			{ color 	= s.toLowerCase();}
	public	void	set_align(String s)			{ align 	= s.toLowerCase();}
	public	void	set_flow(String s){
		flow		= "width=";					// 幅を指定するため
		flowSize	= "\"" + s.toLowerCase() + "\"";
		space		= "normal";					// white-space: normalを指定
	}
	public	void	set_code(){
	    /*
	     * flow を解除する
	     * 行間は110%で固定
	     * 
	     */
	    set_height("110");
	    clearFlow();
	}
	public	void	set_base(String s){
								// base を defaultURL とする
		defaultURL 	= s;		// 次回からは、setDefault() で、base = defaultURL と初期化されるので
		base		= s;		// 一度設定したＵＲＬは再設定しないと消えない
		
	}
	// エイリアス
	public	void	set_alias(String key,String value){
		if(isEmpty(key))	return;
		alias.put(key,value);
		if(DBG.fa) DBG.outHash(alias,"class FontStyle #set_alias() :エイリアス です");
		return;
	}
	public	String	get_alias(String key){
		if(isEmpty(key))	return "";
		return (String)(alias.get(key));
	}
	//
	//
	public	String	get_indent()		{	return indent; 		}
	public	String	get_imgbox()		{	return imgbox; 		}
	public	String	get_imgUrl()		{	return imgUrl; 		}
	public	String	get_position()		{	return position;	}
	//
	public	String	get_style()			{	return style; 		}
	public	String	get_variant()		{	return variant; 	}
	public	String	get_weight()		{	return weight; 	}
	public	String	get_size()			{	return size; 		}
	public	String	get_height()		{	return height; 	}
	public	String	get_type()			{	return type; 		}
	public	String	get_color()			{	return color; 		}
	public	String	get_align()			{	return align; 		}
	public	String	get_flow()			{	return flow; 		}
	public	String	get_space()			{	return space;		}
	public	String	get_flowSize()		{	return flowSize;	}
	public	String	get_base()			{	return base; 		}		// 現在設定されているURLを得る
	public	String	get_sysURL()		{	return defaultURL; }		// ユーザーURLの初期化用
	//
	public	String	getSystemBase()		{	return systemBase; }		// システムURL用
	//
	//
	//  文字列は空か
	//
	public	boolean	isEmpty(String s){
		if((s==null)||(s.length()==0))	return true;
		return	false;
	}
	//
	//  使用可能な指定文字列かどうか
	//  tbl はこのクラスの 有効な指定文字列 の配列を指定する
	//  str は検査対象文字列
	public boolean	isValidWord(String [] tbl,String pStr){
		String	str	=	pStr.toLowerCase();	// 小文字になおしてから比較
		int	n = tbl.length;
		for(int k=0; k<n; k++){
			if(str.equals( tbl[k] ))	return true;	// どれかと合致すればOK
		}
		return false;
	}
	//
	// キーワード番号を返す
	public  int KeywordNumber(String pre_str){
		// 先頭に $ がつくものは alias 定義である
		// それ以外はキーワードをテーブルから引く
		// テーブルを引いてもない場合はハッシュを引く
		// ハッシュにもなければキーワードではない
		//
		// 最初に小文字に変換してから処理を始める
		String	str	=	pre_str.toLowerCase();
		if(DBG.fa) DBG.println("■ Keyword =" + str);
		if(str.charAt(0)=='$'){
			return	ALIAS;
		}
		int	n	= FORMAT_KEYWORD.length;
		for(int k=0; k<n; k++){
			if(str.equals(FORMAT_KEYWORD[k])){
				return	k;	// キーワード番号
			}
		}
		/* 小文字にする前のワードでハッシュを引く */
		String mac	= (String)alias.get(pre_str);
		if(mac != null){
			return EXP;
		}
		return	NOT_KEYWORD;	// キーワードではない
	}
	//
	//  フォント名文字列を返す
	public static String fontName(String ps){
		//
		String	s	=	ps.toLowerCase();
		if(s.equals(typeKWD[0]))	return  npfont;
		return 	pfont;
	}
	//
	//  イメージボックス指定をクリアする
	public	void	clearImgbox(){
		imgbox		= "";
		imgUrl		= "";
		position	= "";
	}
	//
	//  フロー指定をクリアする
	public	void	clearFlow(){
		flow		= "";			// つまり width= を指定しない
		flowSize	= "";
		space		= "pre";		// つまり white-space: pre を指定する
	}
	/**
	 * フローモードかどうか
	 */
	public	boolean	isFlow(){
		if(space.equals("normal"))	return	true;
		return	false;
	}

	//
	//  設定内容をハッシュテーブルに設定する
	//
	public	Hashtable	putHash(Hashtable htb){
		htb.put("_indent",indent);
		htb.put("_imgbox",imgbox);
		htb.put("_imgUrl",imgUrl);
		htb.put("_position",position);
		htb.put("_border",border);
		//
		htb.put("_style",style);
		htb.put("_variant",variant);
		htb.put("_weight",weight);
		htb.put("_size",size);
		htb.put("_height",height);
		htb.put("_type",type);
		htb.put("_color",color);
		htb.put("_align",align);
		htb.put("_flow",flow);
		htb.put("_space",space);
		htb.put("_flowSize",flowSize);
		htb.put("_base",base);
		//
		return	htb;
	}
	// 内容の出力
	public void	printall(){
		DBG.println("*** Style クラス ***");
		DBG.println("	indent	=" + indent);
		DBG.println("	imgbox	=" + imgbox);
		DBG.println("	imgUrl	=" + imgUrl);
		DBG.println("	position=" + position);
		DBG.println("	border  =" + border);
		DBG.println("	style	=" + style);
		DBG.println("	variant	=" + variant);
		DBG.println("	weight	=" + weight);
		DBG.println("	size	=" + size);
		DBG.println("	height	=" + height);
		DBG.println("	type	=" + type);
		DBG.println("	color	=" + color);
		DBG.println("	align	=" + align);
		DBG.println("	flow	=" + flow);
		DBG.println("	space	=" + space);
		DBG.println("	flowSize=" + flowSize);
		//
		DBG.println("	defaultURL	=" + defaultURL);
		DBG.println("	base	    =" + base);
		DBG.println(" ------");
	}

}