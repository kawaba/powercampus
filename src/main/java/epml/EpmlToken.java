/*
 *
 */
package epml;
import epml.tools.Csv;
import epml.tools.DBG;
/**
 * @
 */
public class EpmlToken {
	int		errPos;			// エラー発生位置
	String	errMsg;			// エラー原因
	//
	String	buffer;			// 問題原文
	int		max;			// 問題原文の長さ
	int		p;				// バッファ内の位置（ゼロオリジン）
	String	tkn;			// トークン
	int		id;				// トークン種別
	
	boolean	sequency;		// 連続する項目であるか否か（チェックボックスなど用）
	int		groupNum;		// グループ番号（直前の seqency が false のとき、ERB トークンで＋１する
	//
	int		groupNumENL;	// 同上．ENL 用（++ 選択肢 ++ ワードの出現で＋１する）
	boolean	newENLFLAG;		// まだひとつもグループ要素がないとき true
	 String		defaultRows;	// 語群テーブルの列数の規定値（設定がないとき使われる）
	//
	 String 	seq;		// トークンのシーケンス番号文字列（埋め込み用）
	 int 		seqNum;		// トークンのシーケンス番号
	//
	 int 		nENL;		// EmbededNumberLists 	番号選択
	 int 		nEWL;		// EmbededWordLists		文を選択する
	 int 		nETF;		// EmbededTextFields	１行テキスト入力
	 int 		nERB;		// EmbededRadioButtons　ラジオボタン、チェックボックス
	 int 		nETA;		// EmbededTextArea		記述式

	 int 		nEPT;		// EmbededPoinT			
	 int 		nTEXT;		// その他
	 int 		nAENL;		// AdditionalEmbededNumberLists
	 int 		nLST;		// 
	//
	// トークン区切り記号
	static final char LBL = '[';
	static final char RBL = ']';
	static final char LBM = '{';
	static final char RBM = '}';
	static final char SHP = '#';
	static final char AMK = '@';
	static final char AMP = '&';
	static final char PLS = '+';
	static final char ECL = '!';
	static final char END = '/';
	
	// nextWord()が返すトークン種別
	static final char SEL = 's';	// #[ の代わり　正しい文をチェックする問題
	static final char WRT = 'w';	// #{ の代わり　テキストエリアに記述する問題
	static final char PNT = 't';	// [@ の代わり　配点
	static final char BAM = 'a';	// [& の代わり　ダミー選択肢
	static final char TML = 'x';	// {@ の代わり　制限時間
	static final char LST = 'b';	// ++ の代わり
	static final char STR = '*';	// 一般の文字列の代わり
	
	// トークン区切り記号（日本語）
	static final char jLBL = '［';
	static final char jRBL = '］';
	static final char jLBM = '｛';
	static final char jRBM = '｝';
	static final char jSHP = '＃';
	static final char jAMK = '＠';
	static final char jAMP = '＆';
	static final char jPLS = '＋';
	static final char jECL = '！';
	static final char jEND = '／';
	
	//
	// トークン種別コード
	public static final int ENL		= 1;
	public static final int EWL		= 2;
	public static final int ETF		= 3;
	public static final int ERB		= 4;
	public static final int ETA		= 5;
	public static final int EPT		= 6;
	public static final int AENL		= 7;
	public static final int ALST		= 8;	// 選択肢のりスト
	public static final int TIME		= 9;	// 制限時間 2004.7追加
	public static final int TEXT		= 20;
	//
	// トークンシーケンス接頭辞記号（問題文に埋め込む）
	static final String _ENL	= "ENL(";	// ENL(2,5) 選択肢記入欄
	static final String _EWL	= "EWL(";	// EWL(2)
	static final String _ETF	= "ETF(";
	static final String _ERB	= "ERB(";	// ERB(1,5)
	static final String _ETA	= "ETA(";
	static final String _EPT	= "EPT(";
	static final String _TEXT	= "TEXT(";
	static final String _AENL	= "AENL(";
	static final String _ALST	= "ALST(";		// ALST(1) 選択肢のりスト
	static final String _TIME	= "TIME(";		// 埋め込まないのでこれは形式的なもの

	
	static final String RBLACKET = ")";	// 上記の右括弧．間にCSVでのパラメータが入る
	 
	/**
	 * 
	 * @param str
	 */
	public EpmlToken(String	str){
		buffer	 	= new String(str);
		max		 	= str.length();

		////////////////////////////////////////////
		if(DBG.fa){
			DBG.println("Token length =" + max);
		}
		////////////////////////////////////////////

		p		 	= 0;
		tkn		 	= null;
		id		 	= TEXT;	// 直前のid がTEXTでないと')'を文字と判定しないので、文頭が')'だった場合のために初期値はTEXTとする
		sequency 	= false;
		groupNum 	= 0;	// グループ番号。最初に１足してから始まるので０から
		groupNumENL = 0;	// グループ番号。ENL用
		defaultRows	= "5";  // ５列（規定値）
		newENLFLAG	= true;	// グループ内にまだひとつも要素が加わっていない
		seq		 	= null;
		seqNum		= 0;
		//
		nENL		= 0;
		nEWL		= 0;
		nETF		= 0;
		nERB		= 0;
		nETA		= 0;
		nEPT		= 0;
		nTEXT		= 0;
		nLST		= 0;
		//
		errPos		= 0;
		errMsg		= "";
	}
	
	
	/**
	 * 現在のバッファ位置を返す
	 */
	public	int	tknPOS()	{ return p; }
	
	/**
	 * 取り出したトークンを返す
	 * @return
	 */
	public	String	tknSTR()	{ return	tkn;}
	
	/**
	 * 取り出したトークンの種別番号を返す
	 * @return
	 */
	public	int	tknID()		{ return	id;}
	
	/**
	 * 取り出したトークンの埋め込み用シーケンス番号文字列を返す
	 * @return
	 */
	public	String	tknSEQ()	{ return	seq;}
	
	/**
	 * 取り出したトークンのシーケンス番号を返す
	 * @return
	 */
	public	int	tknSeqNum()	{ return	seqNum;}
	
	/**
	 * ラジオボタン、チェックボックス問題の現在のグループ番号（Check a Chekbox or RadioBottun で使うグループ）を返す
	 * @return
	 */
	public	int	tknGroupNum()	{ return	groupNum;}
	
	/**
	 * 番号を埋める問題の現在のグループ番号（Check a EmbededNumberLists 　で使うグループ）を返す
	 * @return
	 */
	public	int	tknGroupNumENL()	{ return	groupNumENL;}
	
	/**
	 * 連続する項目か否か
	 * @return
	 */
	public	boolean	tknCont()	{ return	sequency;}
	
	/**
	 * 文字列が数字がどうかチェックする
	 * 
	 */
	boolean isDigit(String s){
		if((s == null)||(s.length()==0))    return false;
		//
		//StringBuffer bf = new StringBuffer(100);
		int len = s.length();
		for(int i=0; i<len; i++){
			char ch = matchDigit(s.charAt(i));
			//bf.append(ch);
			if(ch == '*')       return false;
		}
		return true;
	}
	/**
	 *  数字文字列にして返す
	 * @param s
	 * @return
	 */
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
	/**
	 * 文字が数字かどうか
	 * @param c
	 * @return
	 */
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
	/**
	 *	■ バッファから次の tkn と id ,seq を得て（クラス変数）セットする
	 *	
	 *     繰り返し呼ぶことで、バッファ中の全てのトークンを取り出すことができる
	 *     例外が発生したらここで catch し例外コードを返す
	 *     バッファエンドで発生する例外を受け止めて、－１を返す
	 *
	 *     sequency は ラジオボタンやチェックボックスのクループ判定に用いる
	 *     具体的には、false から true に変わった時、グループ番号 groupNum を＋１する．
	 *
	 *     選択肢など問題文以外の文字列にあっては、漢字空白は半角空白に置き換えられる
	 *　　 また、両端の空白は削除される（全角空白も削除される）
	 *     採点照合のとき、解答についても同じことを行うこと！
	 *
	 * 　  戻り値：
	 *           正の値のトークン種別を表す id を返す
	 *           例外の場合の戻り値は全て負の整数である．バッファエンドは－１．
	 * 
	 * @return　トークン種別を表す数値
	 * @throws　EpmlTokenException
	 */
	public	int	getToken()  throws EpmlTokenException {
		//
		//
		////////////////////////////////////////////
		if(DBG.fa){
			DBG.println("■ Token #getToken()");
			DBG.println("□ Token enter position =" + p);
		}
		////////////////////////////////////////////

		try{
			// トークンを切り出して tkn に置き、その文字種別をsubID にセット
			// 種別からトークンの種別を確定する
			char subID	= nextWord();
			if(DBG.fa) DBG.println("■ subID=" + subID);
			//
			if(subID==LBL){ // LBL = '['
				/*
				 * ![･･･/]　または、![･･･ ,･･･  ,･･･ /]
				 * 
				 */
				getWordBeforeEndmark(); 				// 終了文字までの文字列（＝正解語のCSV）をtknに取得する
				tkn = (tkn.replace('　',' ')).trim();	// tknの全角空白を半角空白に変換し、両端の空白を取る
				/*
				 * Csv は要素に格納するとき trim() するので、空白のみのデータは長さ＝０である
				 */
				Csv cs = new Csv(tkn,",，、|｜");	// デリミッタ

				if(cs.notZeroLengthCount() > 1){	// 長さが１以上の選択肢が1つ以上ある
					/*
					 * 選択肢が複数あるので、文中に埋め込んだドロップダウンリストから正解を選ぶ形式
					 * EWL(1)のような埋め込み記号を作成				
					 * 数値は問題中のシーケンス番号
					 * また、idに種別を表す番号EWL（=2)をセットする
					 */
					++nEWL;
					seq 	= _EWL + String.valueOf(nEWL) + RBLACKET;		// ex. EWL(2) 　2はシーケンス番号
					seqNum	= nEWL;
					id		= EWL;

				}else{
					/* 
					 * 正解1つしかないので、文末の選択肢一覧から該当の番号をテキストボックスに書き込む形式
					 * ENL(1)のような埋め込み記号を作成				
					 * 数値は問題中のシーケンス番号
					 * また、idに種別を表す番号EWL（=1)をセットする
					 */					
					++nENL;
					if(newENLFLAG){				// 新しい問題文の始まり（解答の選択肢群が出現した時、終わりの目印として、trueになる）
						newENLFLAG = false;		// ひとつ要素を加えるので flase にする
						++groupNumENL;			// 新しいグループになるのでグループ番号を１増やす（１から始まる）
						nENL = 1;				// 要素番号もクリアしておく（１オリジン）
					}
					if(tkn.length()==0){
						throw new EpmlTokenException( p, "選択語が指定されていません．" ); 
					}
					seq 	= _ENL + String.valueOf(groupNumENL) + "," + String.valueOf(nENL) + RBLACKET;
					seqNum	= nENL;
					id		= ENL;
				}
				sequency = false;

			}else if(subID==LBM){  // LBM='{'
				/*
				 * !{  /}
				 * 文中に埋め込んだテキストボックスに解答語句を書き込む形式
				 * ETF(2)のような埋め込み記号を作成
				 * 数値は問題中のシーケンス番号
				 * idにETF（=3）をセットする
				 * 
				 */
				getWordBeforeEndmark();// tkn にデータを取得してセットする
				tkn = (tkn.replace('　',' ')).trim();// 漢字空白を含めて両端の空白を取る
				
				++nETF;
				seq 	= _ETF + String.valueOf(nETF) + RBLACKET;
				seqNum	= nETF;
				id		= ETF;
				sequency = false;
			//
			// 
			}else if(subID==SEL){	// SEL='s'
				/*
				 * #[  /]
				 * 複数行のリストから、ラジオボタンで正解をチェックする形式．
				 * 複数の正解語があるとチェックボックスとなる
				 * idにERB（=4）をセットする
				 */
				getWordBeforeEndmark();
				tkn = (tkn.replace('　',' ')).trim();// 漢字空白を含めて両端の空白を取る
				
				++nERB;
				if(!sequency){		// 新しいラジオボタン、チェックボックスのグループの始まりなら
					++groupNum;		// グループ番号を１増やす
					nERB	= 1;	// シーケンス番号も初期化する
				}
				// 
				
				/* ERB(1,2)のような埋め込み記号を作成（グループ１，グループ内シーケンス2の意味）
				 * 
				 */
				seq 	= _ERB + String.valueOf(groupNum) + "," + String.valueOf(nERB) + RBLACKET;
				seqNum	= nERB;
				id  	= ERB;
				sequency  = true;	// 連続
			//
			// 
			}else if(subID==WRT){
				/*
				 * #{  /}
				 * テキストエリアに解答文を書き込む形式
				 * ETA(1)のような埋め込み記号を作成
				 * 数値は問題中のシーケンス番号
				 * idにETA（=5）をセットする
				 */
				getWordBeforeEndmark();// tkn にデータを取得してセットする
				tkn = (tkn.replace('　',' ')).trim();// 漢字空白を含めて両端の空白を取る
				++nETA;
				seq 	= _ETA + String.valueOf(nETA) + RBLACKET;
				seqNum	= nETA;
				id  	= ETA;
				sequency = false;	// 問題は１つのETAで完結するので、連続するETAはない
			//
			// 
			}else if(subID==PNT){
				/*
				 * [@nn /]
				 * 配点
				 * 埋め込み文字列は、例えば、EPT(1)のようになる
				 * idにEPT（=6）をセットする
				 */
				getWordBeforeEndmark();
				tkn = (tkn.replace('　',' ')).trim();// 漢字空白を含めて両端の空白を取る
				++nEPT;
				seq 	= _EPT + String.valueOf(nEPT) + RBLACKET;
				seqNum	= nEPT;
				id  	= EPT;
				sequency = false;
			//
			// 
			}else if(subID==TML){
				/*
				 * {@nn /}
				 * 制限時間（ {@nn/} ）
				 * 埋め込み文字列は、TIME(1)になる。数値は１以外ない
				 * idにTIME（=9）をセットする
				 */
				if(DBG.fa){
					DBG.println("");
					DBG.println("★★★　Find TML　★★★");
					DBG.println("");
				}				
				getWordBeforeEndmark();
				tkn = (tkn.replace('　',' ')).trim();// 漢字空白を含めて両端の空白を取る
				seq 	= _TIME + String.valueOf(1) + RBLACKET;	// 未使用のダミー
				seqNum	= 1;									// 未使用のダミー
				id  	= TIME;
				sequency = false;
			//
			// 
			}else if(subID==BAM){
				/*
				 * [&  /]
				 * 追加的なダミーの埋め込み語句（ [&aaa,bbb,ccc] ）
				 * 埋め込み文字列は、例えば、AENL(1)のようになる
				 * idにAENL（=7）をセットする
				 */
				getWordBeforeEndmark();
				tkn = (tkn.replace('　',' ')).trim();// 漢字空白を含めて両端の空白を取る
				++nAENL;
				seq 	= _AENL + String.valueOf(nAENL) + RBLACKET;
				seqNum	= nAENL;
				id  	= AENL;
				sequency = false;
			//
			// 
			}else if(subID==ALST){
				/*
				 * [+ nn /]
				 * 解答選択肢書き込み位置 -- ひとつの問題が終了したとみなせるのでnewENLFLAG=trueとし、次の問題に備えておく
				 * 埋め込み文字列は、例えば、ALST(1/6) , ALST(1/6/2-4-3-1)のようになる
				 * 1は問題中のシーケンス番号、6は１行に表示する選択肢の数、2-3-4-1は選択肢の並び順
				 * idにALST（=8）をセットする
				 */

				getWordBeforeEndmark();
				Csv cs = tblOption(tkn); 	// 語群テーブルサイズと語順の指定を取り出す（なければ "5" と"" ）
				if(cs==null) throw new EpmlTokenException(p,"選択語を並べる語順の指定に誤りがあります");
				//
				String	sz	= cs.get(0);	// 語群テーブルサイズ
				String	pm	="";
				if(cs.size() > 1){
					pm	= cs.get(1);	// 語順
				}
				if(pm.length() >0) sz = sz + "/" + pm;
				//
				++nLST;
				seq		= _ALST + String.valueOf(nLST) + "/" + sz + RBLACKET;	// 例：ALST(1/6) , ALST(1/6/2-4-3-1)
				seqNum	= nLST;
				id 		= ALST;
				//
				newENLFLAG	= true;		// グループが終了したことをENLへ伝える
				//
				sequency = false;
			//	
			// 
			}else{	// STR
				/*
				 * 問題文テキスト
				 * TEXT(1)のような埋め込み文字列を作成する
				 * idはTEXT（=20）をセットする
				 * 
				 * 問題
				 */
				++nTEXT;
				seq  	 = _TEXT + String.valueOf(nTEXT) + "/" + tkn + RBLACKET;
				seqNum	 = nTEXT;
				id	 	 = TEXT;
				/*
				 * ラジオボタン、チェックボックスの問題は複数の選択肢があるので、その最後を判定する
				 * 現在問題の途中（sequency==true）の時、
				 * 　現在のトークンが改行のみ、または、空白行であれば、まだ、問題は続いているので、sequen == true とする。
				 * 　そうでなければ問題は終了とみなして、sequency==falseとする
				 * 
				 */
				sequency = (sequency && !count()); // 直前が true でかつ空白と改行のみの文字列なら true となる
				//
			}
			//
		}catch(EpmlTokenException e){
			int ret = 0;
			if(e.getMessageValue() == EpmlTokenException.EndOfBuffer){
				return -1;
			}else{
				DBG.println("＊＊＊　例外発生　＊＊＊");
				e.printStackTrace();
				DBG.println("＊＊＊＊＊＊＊＊＊＊＊＊");
				throw e;
				
			}
		}
		//
		////////////////////////////////////////////
		if(DBG.fa){
			DBG.println("□ Token exit position =" + p);
		}
		////////////////////////////////////////////
		
		return	id;
	}
		
	/**
	 * バッファから次のトークンを取り出す<br>
	 * 文字列の場合は tkn にその文字列を転記し、ID を返す<br>
	 * 制御文字の場合はそのIDを返し、文字自体は捨てられる<br>
	 * 二文字の制御文字も読み取って同じ様に処理する<br>
	 * ],} などの right blacket は捨てられる<br>
	 * バッファエンドになると例外を発生する<br>
	 * 
	 * @return	トークンID 
	 * 　　　　　ただし、"![" や "#{" などトークンの先頭文字を1文字の記号に置き換えたもの
	 * 　　　　　本当のトークンIDは、tkn の内容から判定する
	 * 
	 * @throws EpmlTokenException
	 */
	 char nextWord() throws EpmlTokenException {
		
		/*
		 * バッファが空だと例外を発生
		 * 単に終了を意味する．エラーではない
		 */
		char	c1 = 0;
		char	c2 = 0;
		if( (c1=nextChar()) == 0 ){ // バッファから1文字取って返す。バッファエンドなら0を返す
			throw new EpmlTokenException(EpmlTokenException.EndOfBuffer);
		}
		/*
		 * 制御文字ならその１文字を返す
		 */
		char subID	= firstChar(c1);	//   '['   '{'  '#'   '!'   '/'   '*'  のどれかを返す
		if(subID == SHP){
			/*
			 * #{･･･/} テキスト領域
			 * #[･･･/] チェックボックス、ラジオボタンによる選択肢
			 */
			if( (c2=nextChar()) == 0 ){
				tkn	=	String.valueOf(c1);
				return STR;				
			}
			subID	= secondChar(c2);
			//
			if(subID==LBL){
				subID = SEL;		// #[
				return subID;

			}else if(subID==LBM){
				subID = WRT;		// #{
				return subID;

			//}else if(subID==SHP){
			/*
			 * ここでは #ひとつだけを STR として処理し、二つ目の #はバッファに戻すこととし else の処理と合併した
			 * 次回の処理で #{ #[ 以外だとやはり STR となるので WikiToken クラスとは矛盾しない
			 */
				//StringBuffer bf = new StringBuffer(1024);	// ## は wikiToken クラスでパースするのでデータとして処理する
				//bf.append(c1);
				//bf.append(c2);
				//getLine(bf);
				//return STR;
			}else{
				/*
				 * １文字目が# でその後は一般文字列（#の場合もある）があったケース
				 * 最初の#を STR として処理し、二つ目の文字はバッファに戻す
				 * 後の処理はnormalStrings() にまかすが、normalStrings()は制御文字が
				 * 出現するまで文字列をバッファに取得する
				 */
				StringBuffer bf = new StringBuffer(1024);
				bf.append(c1);		// １文字目をバッファに入れる
				putBackChar();
				
				// 一般文字列の最後までbfに取り出し、それを tkn にセットする
				// 戻り値は一般文字列を表す STR（'*'）
				normalStrings(bf);	
				
				//StringBuffer bf = new StringBuffer(1024);
				//bf.append(c1);
				//bf.append(c2);
				//normalStrings(bf);
				return STR;				
			}
			//
		}else if(subID==LBM){		// '{'  制限時間
			/*
			 * {@30/} のケース：制限時間
			 */
			if( (c2=nextChar()) == 0 ){
				tkn	=	String.valueOf(c1);		// tknは一般文字で値は { 
				return STR;				
			}
			subID	=	secondChar(c2);		// ２文字目を得る
			//
			if(subID==AMK){
				subID	=	TML;		// subID を {@ に変更　TMLは'x' で、1文字で"{@"を意味する種別記号
				return subID;
			}else{
				//StringBuffer bf = new StringBuffer(1024);
				//bf.append(c1);
				//bf.append(c2);			// c を戻す
				//normalStrings(bf);
				/*
				 * １文字目が{ でその後は一般文字列（{の場合もある）があったケース
				 * {ひとつだけを STR として処理し、二つ目の文字はバッファに戻す
				 * 後の処理はnormalStrings() にまかすが、normalStrings()は制御文字が
				 * 出現するまで文字列をバッファに取得する
				 */
				StringBuffer bf = new StringBuffer(1024);
				bf.append(c1);
				putBackChar();
				normalStrings(bf);
				return STR;				
			}
			//
		}else if(subID==LBL){	// [ であればさらにチェック
			/*
			 * [@10 /]			配点
			 * [& abc,def /]	ダミー選択肢
			 * [+ 語群(3) /] 	選択肢語群
			 */
			if( (c2=nextChar()) == 0 ){
				tkn	=	String.valueOf(c1);
				return STR;				
			}
			subID	=	secondChar(c2);
			//
			if(subID==AMK){
				subID = PNT;		// subID を [@　に変更
				return	subID;

			}else if(subID==AMP){
				subID = BAM;		// subID を [&　に変更
				return	subID;

			}else if(subID==PLS){
				subID = ALST;		// subID を [+　に変更
				return	subID;

			}else{
				//StringBuffer bf = new StringBuffer(1024);
				//bf.append(c1);
				//bf.append(c2);
				//normalStrings(bf);
				/*
				 * １文字目が{ でその後は一般文字列（{の場合もある）があったケース
				 * {ひとつだけを STR として処理し、二つ目の文字はバッファに戻す
				 * 後の処理はnormalStrings() にまかすが、normalStrings()は制御文字が
				 * 出現するまで文字列をバッファに取得する
				 */
				StringBuffer bf = new StringBuffer(1024);
				bf.append(c1);
				putBackChar();
				normalStrings(bf);
				return STR;				
			}
			//
		}else if(subID==END){
			/*
			 *  /]  /}
			 *  
			 * 終端記号なので読み飛ばして再帰処理を行う
			 * 
			 */
			if( (c2=nextChar()) == 0 ){
				tkn	=	String.valueOf(c1);
				return STR;				
			}
			subID	=	secondChar(c2);			
			if((subID==RBL)||(subID==RBM)){
				subID = nextWord();
				return subID;

			}else{
				//StringBuffer bf = new StringBuffer(1024);
				//bf.append(c1);
				//bf.append(c2);
				//normalStrings(bf);
				/*
				 * １文字目が / でその後は一般文字列（/の場合もある）があったケース
				 * / ひとつだけを STR として処理し、二つ目の文字はバッファに戻す
				 * 後の処理はnormalStrings() にまかすが、normalStrings()は制御文字が
				 * 出現するまで文字列をバッファに取得する
				 */
				StringBuffer bf = new StringBuffer(1024);
				bf.append(c1);
				
				if(c2=='/') {			/* コメント記号で // の場合の処理　*/
					bf.append(c2);
				}else {
					putBackChar();
				}
				
				normalStrings(bf);
				return	STR;
			}
			//
		}else if(subID == ECL){
			/*
			 * ![    /]		語群式選択肢、ドロップダウン選択肢
			 * !{    /}		記述式
			 * 
			 */
			if( (c2=nextChar()) == 0 ){
				tkn	=	String.valueOf(c1);
				return STR;				
			}
			subID	=	secondChar(c2);			
			//
			if(subID==LBL){
				subID = LBL;
				return subID;
				//
			}else if(subID==LBM){
				subID = LBM;
				return subID;
			//
			}else{
				//StringBuffer bf = new StringBuffer(1024);
				//bf.append(c1);
				//bf.append(c2);
				//normalStrings(bf);
				/*
				 * １文字目が ! でその後は一般文字列（!の場合もある）があったケース
				 * ! ひとつだけを STR として処理し、二つ目の文字はバッファに戻す
				 * 後の処理はnormalStrings() にまかすが、normalStrings()は制御文字が
				 * 出現するまで文字列をバッファに取得する
				 */
				StringBuffer bf = new StringBuffer(1024);
				bf.append(c1);
				putBackChar();
				normalStrings(bf);
				return STR;
			}
		}else{
			// その他の場合一般の文字なので文字列にして返す
			StringBuffer bf = new StringBuffer(1024);
			bf.append(c1);			// c を戻す
			normalStrings(bf);
			return STR;
		}
	}

	/**
	 * 先頭文字が一般文字列と分かっているときに、
	 * 一般文字列を一区切り取り出してトークンに入れる
	 */
	void normalStrings(StringBuffer	bf){
		// 一般の文字なので文字列にして返す
		//
		char c;
		while( (c=nextChar()) != 0  ){
			if(firstChar(c)!=STR){	
				putBackChar();
				break;
			}
			bf.append(c);
		}
		tkn	= bf.toString();
	}
	/**
	 * 終了文字までの文字列をtknに取得する
	 * @throws 	データがないときはfalse
	 */
	boolean	getWordBeforeEndmark(){
		StringBuffer bf = new StringBuffer(1024);
		char	c1 = 0;
		char	c2 = 0;
		
		if( (c1=nextChar()) == 0 ){
			return	false;
		}
		putBackChar();
		//
		while((c1=nextChar())!=0){
			/*
			 * '/' を探す
			 */
			char	check	=	firstChar(c1);
			if(c1!=END){
				bf.append(c1);
				continue;
			}
			/*
			 * '}'  ']' を探す
			 */
			if( (c2=nextChar()) == 0 ){
				bf.append(c1);
				break;
				
			}else{
				check	=	lastChar(c2);
				if((check==RBL)||(check==RBM)){
					break;
				
				}else if(isSlash(c2)){
					/*
					 * '//' のようにスラッシュが２個以上続いた時は
					 * １個目を文字列とし、２個目はバッファに戻して次の繰り返しで
					 * 再度解析する
					 */
					bf.append(c1);
					putBackChar();
					
				}else{
					bf.append(c1);
					bf.append(c2);
				}
			}
		}
		tkn	=	bf.toString();
		return	true;
	}
	/**
	 *
	 * 選択肢の表示列数と語順指定をCsvで取り出す
	 *
	 * 	語群を置くテーブルサイズ（列数）の指定があれば取り出す
	 * 	なければ defaultRows = "5" を返す
	 * 	選択語順の指定があれば取り出す
	 * 	指定がない時は "" が返る
	 *
	 * （例）
	 *   指定:　"++ 語群(6) ++" 　⇒　str="語群(6)" となっている時
	 *   "6" と "" を取り出し Csv にして返す
	 *
	 * (例-2)
	 *   指定:　"++ 語群(6/3-5-2-4-1) ++" 　⇒　str="語群(6/3-5-2-4-1)" となっている時
	 *   "6" と"3-5-2-4-1" をCsvクラスにして取り出し、Csv にして返す
	 *   <3-5-2-4-1 は選択肢の語順．>
	 *
	 * 
	 * @param _str
	 * @return
	 */
	Csv	tblOption(String _str){
		if(DBG.fa) {
			DBG.println("■■ tblOption()");
			DBG.println("□ tkn =" + _str);
		}
		//
		String	row		= "";
		String	order	= "";
		//
		// 漢字文字を半角に置き換え
		substitute(_str,"（","(");
		substitute(_str,"）",")");
		String	str	= getString(_str,'(',')');	// ( ) の間の文字列を取得　6/3-5-2-4-1、ないし 6
		//
		if(str.length()==0){						// ()内に文字がないとき
			Csv		cs	= new Csv(defaultRows);		// 列数をデフォルトの (5) にしておく
			return	cs;
		}
		// str.length() > 0
		Csv		cs	= new Csv(str,"/");
		row	= toDigit(cs.get(0));					// 必ずある．有効な数字になおしておく．
		if(row.length()==0){						// 有効な数字が指定してなかったときはデフォルトの(5)にする
			row		= defaultRows;
		}
		if(cs.size() > 1){								// 語順の指定があるとき order に取り出す
			order			= cs.get(1);				// 各指定を取り出し
			Csv 	ocs		= new Csv(order,"-");		// 有効な数字が指定されているかどうかチェックする
			for(int k=0; k<ocs.size(); k++){
				if(!isDigit(ocs.get(k)))	return null;	// 無効な場合は null を返す（予備元で例外を投げる）
			}
		}
		return	new Csv(row + "," + order);
	}

	/**
	 *  １文字目の制御文字を判定する
	 *  @return
	 */
	char	firstChar(char c){
		if((c==LBL)||(c==jLBL))	return	LBL;	// [
		if((c==LBM)||(c==jLBM))	return	LBM;	// {
		if((c==SHP)||(c==jSHP))	return	SHP;	// #
		if((c==ECL)||(c==jECL))	return	ECL;	// !
		/*
		 * 終端を判定する区切り文字として必要
		 * nextWord() で利用する
		 */
		if((c==END)||(c==jEND))	return	RBL;	// /
		return	STR;
	}
	/**
	 * ２文字目の制御文字を判定する
	 * @param c
	 * @return
	 */
	char	secondChar(char c){
		if((c==LBL)||(c==jLBL))	return	LBL;	// [
		if((c==LBM)||(c==jLBM))	return	LBM;	// {
		if((c==PLS)||(c==jPLS))	return	PLS;	// +
		if((c==AMK)||(c==jAMK))	return	AMK;	// @
		if((c==AMP)||(c==jAMP))	return	AMP;	// &
		return	STR;
	}
	/**
	 * 最後の制御文字を判定する
	 * @param c
	 * @return
	 */
	char	lastChar(char c){
		if((c==RBL)||(c==jRBL))	return	RBL;	// ]	
		if((c==RBM)||(c==jRBM))	return	RBM;	// }
		return	STR;
	}
	/**
	 * '/' かどうか判定する
	 * @param c
	 * @return
	 */
	boolean	isSlash(char c){
		if((c==END)||(c==jEND))	return	true;	// /
		return false;
	}
	
	/**
	 * １行の終わりまでを取得する
	 */
	void getLine(StringBuffer	bf){

		char c;
		/*
		 * 制御文字が出現するまでバッファに入れる
		 *（文字列を取得） 
		 */
		while( (c=nextChar()) != 0  ){
			if(Character.isISOControl(c)){
				putBackChar();
				break;
			}
			bf.append(c);
		}
		/*
		 * 非制御文字が出現するまでバッファに入れる
		 *（改行コードを取得） 
		 */
		while( (c=nextChar()) != 0  ){
			if(!Character.isISOControl(c)){
				putBackChar();
				break;
			}
			bf.append(c);
			//
		}
		tkn	= bf.toString();
	}
	/**
	 * バッファから１文字取って返す．ポインタは＋１される
	 * @return
	 */
	char nextChar(){
		char	c = 0;
		if( !EOB() ){
			c = buffer.charAt(p);
			p++;
		}else{
			c = 0;	// バッファが空
		}
		return	c;
	}
	/**
	 * バッファを１文字戻す
	 * 
	 */
	 void	putBackChar(){
		--p;
	}
	/**
	 * バッファが空かどうか
	 * @return
	 */
	boolean	EOB(){
		return (p >= max);	// 最後のとき true
	}
	/**
	 * tkn の文字数が１以上か調べる．
	 * ただし、半角、全角のスペースと改行コードは文字数に含めない
	 * 
	 * @return		zero でなければ true を返す
	 */
	boolean count(){
		int len = tkn.length();
		for(int k=0; k<len; k++){
			if( isCount(tkn.charAt(k)) ) return true;
		}
		return false;
	}
	boolean isCount(char c){
		if( (c==' ')||(c=='　') ){
			return false;
		}
		if(Character.isISOControl(c)) return false;	// 制御文字
		//
		return true;
	}
	/**
	 * 文字列 source の中で、from と to で挟まれた部分を取り出す
	 * 
	 * @param source
	 * @param from
	 * @param to
	 * @return
	 */
	String	getString(String source,char from ,char to){
		//
		int	pos1	= source.indexOf(from);
		if(pos1 < 0)		return	"";			// 開始文字がない
		int pos2	= source.indexOf(to);
		if(pos2 < 0)		return	"";			// 終了文字がない
		if((pos2-pos1)<=1)	return	"";			// [] で中身がないか][ で順序が逆
		//
		return	source.substring(pos1+1,pos2);
	}
	/**
	 * source 文字列の全ての target を rep に置き換える
	 * 
	 * @param source
	 * @param target
	 * @param rep
	 * @return
	 */	
	public String	substitute(String source,String target,String rep){
		//
		int	pos 		= source.indexOf(target);
		if(pos<0)	return	source;
		//
		int	len			= target.length();
		String	str1	= "";
		String	str2	= "";
		//
		try{
			str1	= source.substring(0,pos);
		}catch(IndexOutOfBoundsException  e){
			str1 = "";
		}
		try{
			str2	= substitute(source.substring(pos+len),target,rep);
		}catch(IndexOutOfBoundsException  e){
			str2 = "";
		}
		//
		return	str1 + rep + str2;
	}

}

























