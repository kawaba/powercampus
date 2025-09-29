package epml;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.Vector;
import epml.tools.Csv;
import epml.tools.Regularizer;
import epml.tools.TemplateBox;
import epml.tools.info;
import framework.LOG;
import tktools.FileGear;

/*
 * □9/25 の変更点
 * 1.文法の変更
 *   PMLとの混在で問題が起きないよう終端の書き方の変更と、選択語群の指定方法を変更した
 * 　
 * 　[  ]  ⇒　![  /]
 * 　{  }  ⇒　!{  /}
 * 　#[  ] ⇒　#[  /]
 * 　#{  } ⇒　#{  /r,c/}
 *   
 *   ++  ++ 　⇒　[+   /]
 *  {@   }    ⇒  {@   /}
 *  [@   ]    ⇒  [@   /]
 *
 *2.解答語などが原文と違ってくる可能性があるので、
 *　問題原文で < > " などの文字を正規化した文字列から元の記号に直すようにした
 *　ただし、HTMLへの変換処理（doHMAC()）では再度正規化文字に直している
 *
 */
 
/**
 * 
 * EPML及びPMLテキストの解析と変換を行う．また自動採点機能を提供する．<br>
 * <pre>
 * 機能：
 * (1) EPMLテキストを解析して問題文を表すHTMLテキストを作成する
 * (2) PMLテキストを解析してHTMLテキストを作成する
 * (3) 解答を表すEPMLテキストを初期生成する
 * (4) 解答を表すHTMLテキストを更新作成する
 * (5) 正解を表すHTMLテキストを作成する
 * (6) Examオブジェクトの内部情報からEPMLテキストを逆生成する
 * (7) 解答を自動採点する
 * </pre>
 * 
 * @author kawaba
 */
public class Exam implements emlVar{
    
    /**
     * mikaType用　doHMAC で設定される
     */
    Vector					criterion;
    String					criterionName;    
    
	//
	/** 総得点（採点結果）*/
	int		points; 
	
	/** 問題文 */
	String		originalText;
	
	/** オリジナルテキストを再構成したテキスト
	 * toEpml()で結果を記録するのに使用する 
	*/
	String		modifiedText;
	
	/** ＥＰＭＬをパースして記述項を変数名に置き換えた中間テキスト */
	String		preHtmlText;	
	
	/** preHtmlText をhtmlテキストに直したもの */
	String		htmlText;	
	
	/** 最終的に生成されたHTMLテキスト */
	String		lastHtmlText;
	
	/** 埋め込まれた配点 */
	int		assignedPoint;
	
	/** 制限時間 **/
	int		timeLimit;	 
	
	/** EPML原文から抽出されたトークン */
	EpmlToken	token;
	//Token		token;			
	
	/** フォーマットで指定された（現在の）フォントレベル( h4 ならレベルは４．デフォルトは４） */
	int		fontLevel;		
	
	/** パース完了か未完かを示すフラグ */
	boolean	doneFlag;		
	//
	EmbededNumberLists		enl;	// 選択肢から番号を選んで埋める
	EmbededWordLists		ewl;	// 複数の文から正しいものを選ぶ
	EmbededTextFields		etf;	// 語句を記入する
	EmbededRadioButtons		erb;	// ラジオボタン、チェックボックス
	EmbededTextAreas		eta;	// 文、コードなどを記述する
	//
	// 設定ファイルからの入力 ：設定ファイルは epmlPath として、コンストラクタがファイル名のパスを受け取る
	//                          ここでは pc.conf を流用している
	//
	/** 設定ファイルのパス */
    String	emlPath;				
	/** /var/pc/epml/hmac.txt */
	String	html_templatePath;		
	/** /var/pc/epml/epml.txt */
	String	epmlTemplatePath;		
	
	/** WWW のホームディレクトリの絶対パス */
	String	wwwPath;
	/** システムグラフィックスディレクトリへの絶対パス */
	String	sysImgAbsPath;
	/** 
	 * システムグラフィックスディレクトリへのWWWにおける絶対パス　
	 * ex.  /pc/sysimg/ 
	 */ 
	String	sysImgURL;				
	
	/**
	 * 資料データかそれ以外かを区別するフラグ
	 */
	boolean	htmlFlag;
	
	/**
	 * ユーザーグラフィックスディレクトリへの相対パス
	 * ex.  files/
	 */
	String	imgURL;	
	
	/**
	 * ユーザーグラフィックスディレクトリへの絶対パス
	 * このパスへシステムグラフィクスをコピーするのに使う
	 * RefPrev.java など、実際にコピーの必要が発生するクラスから値がセットされる
	 * ので、常に値があるわけではない
	 */
	String	imgAbsPath;		
	
	/** 初期値を格納したデータファイルへの完全パス */
	String	parserInitialFile;
	ParserInitializer	pi;
	
	/** スペーサーデータへの絶対参照 
	 * ex. /var/pc/epml/spacer.gif */
	String	spacerFilePath;			
	/** スペーサーデータ名 
	 * ex. spacer.gif */
	String	spacerFileName;			
	//
	final	String 	FS 		= File.separator;
	final	String 	CR 		= System.getProperty("line.separator");
	/**
	 * 解答の未選択、未記入などに対応してセットする文字列
	 */
	public	static	final	String	NOT_DISCRIBED	=	"《未記入》";
	public	static	final	String	NOT_SELECTED	=	"《未選択》";
	public	static	final	String	NOT_SELECTEDNUM	=	"0";
	
	/**
	 * 初期化スイッチ付きのコンストラクタ
	 * 
	 * @param kadai_text	問題原文
	 * @param _emlPath		設定ファイルへの完全パス名
	 * @param init			初期化指示フラグ
	 */

	public	Exam(String	kadai_text, String _emlPath,boolean init){
		if(LOG.fa){
			LOG.println("");
			LOG.println("■ Exam#Exam() :コンストラクタ１の入り口");
			LOG.println("-- 入力テキスト（１）");
			LOG.println(kadai_text);
		}
		
		/*
		 * mikaType 用
		 */
		criterion		=	null;
		criterionName	=	"";
		
		//
		emlPath			= _emlPath;			//  /var/pc/conf/pc.conf
		points			= 0;
		/*
		 * 問題文はPMLでは記号（＜、＞、”など）を正規化してあるので、ここで一度元へ戻す
		 * doHmac() では、再度正規化するので問題ない
		 */
		//originalText	= str;
		//originalText	= antiRegularize(kadai_text);	// 正規化した問題原文をもとへ戻す
		originalText = kadai_text;						// 元に戻さないことにした。
		
		if(LOG.fa){
			LOG.println("==== Exam: originalText ===");
			LOG.println(originalText);
		}
		//
		preHtmlText		= null;
		htmlText		= null;
		lastHtmlText	= null;
		//
		assignedPoint	= 1;			// 配点指示がない場合はすべて１点の扱いになる
		timeLimit		= 0;			// 指定がなければ０分とする.
		fontLevel		= 4;			// デフォルトのフォントレベル[ 1- 7 ]
		//
		enl				= null;
		ewl				= null;
		etf				= null;
		erb				= null;
		eta				= null;
		setInitParam();	// パスとURLを設定ファイルから読み込む
		//
		doneFlag		= false;	// 未完	
		//
		setHtmlFlag();	
		//
		/*
		 * 
		 */
		if(init) doEPML();			// 指定があれば初期化する
		
	}
	/**
	 * 初期化なしコンストラクタ
	 * 
	 * @param kadai_text			問題原文	
	 * @param _emlPath		設定ファイルへの完全パス名
	 */
	public	Exam(String	kadai_text,String _emlPath){
		if(LOG.fa){
			LOG.println("");
			LOG.println("■ Exam#Exam() :コンストラクタ２の入り口");
			LOG.println("-- 入力テキスト（２）");
			LOG.println(kadai_text);
		}
	
		/*
		 * mikaType 用
		 */
		criterion		=	null;
		criterionName	=	"";
		
		//
		emlPath			= _emlPath;			//  /var/pc/conf/pc.conf
		points			= 0;
		//originalText	= str;
		
		//originalText	= antiRegularize(kadai_text);	// ウェブ特殊文字を<,>,",'などへ変換する
		originalText = kadai_text;						// 元へ戻さないことにした。
		
		
		
		
		if(LOG.fa){LOG.println(originalText);}
		//
		preHtmlText		= null;
		htmlText		= null;
		lastHtmlText	= null;
		//
		assignedPoint	= 1;			// 配点指示がない場合はすべて１点の扱いになる
		timeLimit		= 0;			// 指定がなければ０分とする
		fontLevel		= 4;			// デフォルトのフォントレベル[ 1- 7 ]
		//
		enl				= null;
		ewl				= null;
		etf				= null;
		erb				= null;
		eta				= null;
		setInitParam();					// パスとURLを設定ファイルから読み込む
		//
		doneFlag		= false;		// 未完
		//
		setHtmlFlag();	

	}
	/**
	 * 初期化なしコンストラクタ
	 * 
	 * @param kadai_text	問題原文	
	 * @param _emlPath		設定ファイルへの完全パス名
	 * @param _hmacParh		特定のHTMLテンプレートファイルを指定する
	 */
	/**
	 * 初期化なしコンストラクタ
	 * 
	 * @param kadai_text	問題原文	
	 * @param _emlPath		設定ファイルへの完全パス名
	 * @param _hmacParh		特定のHTMLテンプレートファイルを指定する
	 */
	public	Exam(String	kadai_text,String _emlPath,String _hmacParh){
		if(LOG.fa) LOG.println("class Exam#Exam() : ■ コンストラクタ３の入り口");

		/*
		 * mikaType 用
		 */
		criterion		=	null;
		criterionName	=	"";
		
		//
		emlPath			= _emlPath;			//  /var/pc/conf/pc.conf
		points			= 0;
		
		//originalText	= antiRegularize(str);	// 正規化した問題原文をもとへ戻す
		originalText	= kadai_text;			// 元へもどさない
		if(LOG.fa){LOG.println(originalText);}

		//
		preHtmlText		= null;
		htmlText		= null;
		lastHtmlText	= null;
		//
		assignedPoint	= 1;			// 配点指示がない場合はすべて１点の扱いになる
		timeLimit		= 0;			// 指定がなければ０分とする
		fontLevel		= 4;			// デフォルトのフォントレベル[ 1- 7 ]
		//
		enl				= null;
		ewl				= null;
		etf				= null;
		erb				= null;
		eta				= null;
		setInitParam();	// パスとURLを設定ファイルから読み込む
		//
		html_templatePath	= _hmacParh;		// 特定のHTMLテンプレートファイルを指定する
		//DBG.println("    ★　wikiTemplatePath-2 : " + wikiTemplatePath);
		//
		doneFlag		= false;				// 未完
		//
		setHtmlFlag();	
	}	
	
	
	/**
	 * ミカタイプ用評価基準を返す
	 * WikiTokenでパース時に作成されるのをdoHMAC()の中で受け取っている
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
	 * ユーザーグラフィックスディレクトリへのURLを設定する
	 * ex.  files/
	 */
	public	void	setImgPath(String path){
		imgURL	=	path;
	}
	/**
	 * ユーザーグラフィックスディレクトリへの絶対パスを設定する
	 * このパスへシステムグラフィクスをコピーするのに使う
	 * RefPrev.java など、実際にコピーの必要が発生するクラスから値がセットされる
	 * ので、常に値があるわけではない
	 */
	public	void	setImgDestinationPath(String path){
		imgAbsPath	=	path;
	}
	/**
	 * 生成するHTMLが資料データのHTMLかどうかを設定する
	 * 初期値は 資料データ （true）

	 * 資料データはHTMLのあるディレクトリ直下の file ディレクトリに
	 * グラフックスデータを保持していることが前提されているので
	 * true なら リンクの解釈で、ファイル名だけ指定された場合には、
	 * imgURLから末尾のディレクトリを削ったものが付加される
	 * 
	 * 課題データなどの場合は、そのような想定はない。そこで、
	 * false なら imgURL をそのまま付加する
	 *
	 */
	public	void	setNonHtmlFlag(){
		htmlFlag	=	false;
	}
	public	void	setHtmlFlag(){
		htmlFlag	=	true;
	}
	
	
	/** 
	 * システムグラフィックスディレクトリへのWWWにおける絶対パスをセットする　
	 * ex.  /pc/sysimg/ 
	 */ 
	public	void	setSysImgPath(String path){
		sysImgURL	=	path;
	}

	public	String	getSysImgPath(){
		return		sysImgURL;
	}
	public	String	getImgPath(){
		return	imgURL;
	}
	/** < と > と " を特殊文字に直す */
	public	static String	regularize(String	str){
		String	temp1	=  	Regularizer.substitute(str,  "<","&lt;");
		String	temp2	=  	Regularizer.substitute(temp1,">","&gt;");
		
		String	temp3	=	toDefindStr(temp2);
		return	temp3;
	}
	/** < と > と " を特殊文字に戻す */
	public	static String	antiRegularize(String	str){
		
		return str; 	// もどに戻さない　2023.3.29
		
	//	String	temp1	=  Regularizer.substitute(str,"&lt;",  "<");
	//	String	temp2	=  Regularizer.substitute(temp1,"&gt;",">");
	//	String	temp3	=	toNormalString(temp2);
	//	return	temp3;
	}
	/**  \ " ' を特殊文字に直す */
	public	static String	toDefindStr(String	str){
		String	temp1	=  Regularizer.substitute(str,"\"","&quot;");
		String	temp2	=  Regularizer.substitute(temp1,"\'","&rsquo;");
		String	temp3	=  Regularizer.substitute(temp2,"\\","&yen;");
		return	temp3;
	}
	/** \ ' "  を特殊文字に戻す */
	public	static String	toNormalString(String	str){
		
		return str;	// 元に戻さない　2023.3.29
		
	//	String	temp1	=  Regularizer.substitute(str,"&quot;","\"");
	//	String	temp2	=  Regularizer.substitute(temp1,"&rsquo;","\'");
	//	String	temp3	=  Regularizer.substitute(temp2,"&yen;","\\");
	//	return	temp3;
	}	

	/**
	 * 	変換用テンプレートのパスとシステムグラフィックスのＵＲＬ
	 * 	を設定ファイルから読み込む
	 */
	void	setInitParam(){
		if(LOG.fa) LOG.println("■ class Exam #setInitParam() : 設定ファイルから読み込む の先頭です");
		//
        info	property	= null;
		try{
            property	= new info(emlPath);	//  /var/pc/conf/pc.conf
        }catch(IOException e){
            System.out.println("class Exam #setInitParam() : ★ 致命的なエラーです．設定ファイルが読めません.");
        }
        //
		html_templatePath	= property.get("wikiTemplatePath");		// 1 C:\Tomcat\webapps\pc\system\epml\hmac.txt
		epmlTemplatePath	= property.get("epmlTemplatePath");		// 2 C:\Tomcat\webapps\pc\system\epml\epml.txt

		sysImgURL			= property.get("sysURL");				// 3 /pc/sysimg/
		sysImgAbsPath		= property.get("sysImgDir");			// 4 C:\Tomcat\webapps\pc\sysimg\

		imgURL				= property.get("imgURL");				// 5 files/
		imgAbsPath			= "";									//   ユーザーグラフィックスディレクトリへの絶対パス
		//
		spacerFilePath		= property.get("spacerFilePath");		// 6 /var/pc/epml/spacer.gif
		spacerFileName		= (new File(spacerFilePath)).getName();	//   spacer.gif
		//
		parserInitialFile	= property.get("FORMAT_TEMPLATE");		// 7.初期値データファイルの完全パス
		//
		if(LOG.fa){
			LOG.println("    wikiTemplatePath : " + html_templatePath);
			LOG.println("    epmlTemplatePath : " + epmlTemplatePath);
			LOG.println("    sysImgURL        : " + sysImgURL);
			LOG.println("    parserInitialFile: " + parserInitialFile);
		}
	}

	/**
	 * 制限時間を返す
	 * @return		制限時間(秒単位に直して返す)
	 */
	public	int	getTimeLimit(){
		return	timeLimit * 60;
	}
	
	//
	// スペーサーのフルパスとファイル名を返す
	public	String	getSpacerPath()			{ return	spacerFilePath; }
	public	String	getSpacerFilename()		{ return	spacerFileName; }
	//
	// 
	// doEPML() の実行に先立って変数を初期化する
	void	init(){
		enl				= new EmbededNumberLists();
		ewl				= new EmbededWordLists();
		etf				= new EmbededTextFields();
		erb				= new EmbededRadioButtons();
		eta				= new EmbededTextAreas();
	}
	public	String				hmacPath()	{ return	html_templatePath; }	// epml.txt
	public	String				epmlPath()	{ return	epmlTemplatePath; }	// epml.txt
	public	EmbededNumberLists	getENL()	{ return	enl; }
	public	EmbededWordLists	getEWL()	{ return	ewl; }
	public	EmbededRadioButtons	getERB()	{ return	erb; }
	public	EmbededTextFields	getETF()	{ return	etf; }
	public	EmbededTextAreas	getETA()	{ return	eta; }
	//
	public	String	getemlPath()	{ return	emlPath; }
	// 
	/**
	  * EPML原文を返す． 
	  * 選択語順などパースする過程でオリジナルテキストが書き換わる場合があるので
	  * パース直後にはこのメソッドで最新のオリジナルテキストを得る必要がある
	  * 
	 */
	public	String	getText()	{
		if(LOG.fa){
			LOG.println("");
			LOG.println("■ Exam#getText()");
			LOG.println("-- 出力テキスト");
			LOG.println(originalText);
		}
		return	originalText;
	}
	/**
	 * 解答を採点する
	 * 
	 * @param 	examSt	解答のExamオブジェクト
	 * @return			採点結果の得点を返す
	 */
	public	int	grades(Exam examSt){
		// 採点する
		int	pt		=	gradesOP(examSt);
		// 採点後の正誤情報を含むように解答EPMLを書き換え、フィールドに保存する
		// 具体的には解答語の後に "~t" "~f" を付加して解答の正誤が分かるようにする
		// 書き換えた学生の解答を表すEPML文は、クラスフィールド変数 modifiedText に残っている
		// ので、grade()を呼び出した後で、getEpml()で取得する
		examSt.toEpml();
		return	pt;
	}

	/**
	 * toEpml()で変更されたEPMLテキストを返す
	 * @return   変更されたEPMLテキストを返す
	 */
	public	String	getEpml(){
		return	modifiedText;
	}
	/**
	 * 
	 * @param examS		解答のEPMLオブジェクト
	 * @return			採点結果の得点を返す
	 */
	public	int	gradesOP(Exam	examS){
		if(LOG.fa) LOG.println("class Exam #grades() : 採点する の先頭です");
		//
		if(examS.preHtmlText==null){
			LOG.println("■■ 学生の解答オブジェクトが初期化されていません．doEPML()を先に実行してください");
			return	0;
		}
		points	= 0;
		points	+=	enl.grades(examS.getENL());
		points	+=	ewl.grades(examS.getEWL());
		points	+=	erb.grades(examS.getERB());
		points	+=	etf.grades(examS.getETF());
		points	+=	eta.grades(examS.getETA());
		
		return	points;
	}
	//
	// 採点結果を埋め込んだテキストを受け取る（電子メールに添付する目的）
	// 改行コードは後で確認すること
	String	getResult(){
		
		String	result	= "　【 得　点 】" + String.valueOf(points) + " 点" + CR + CR + originalText;
		
		// あとで不要なHMACコードなどを取り去る処理を追加する
		return	result;
	}
	
	
	/**
	 * EPMLテキストを解析して問題文を表すHTMLテキストを作成する
	 * 
	 * @return   EPMLから作成した問題文を表すHTMLテキスト
	 */
	public String create(){
		if(LOG.fa) LOG.println("■Exam #create() : 表示用HTMLを生成する の先頭です");
		//
		String	tx01	= null;
		String	tx02	= null;
		String	tx03	= null;
		//
		tx01 = doEPML();				// EPMLをパース
		if(isDone()){
			tx02 = doHMAC( tx01);		// HMACをパース
			if(isDone()){
				tx03 =  subst(tx02);	// EPML要素をHTMLに置き換え
			}else{
				tx03 = "<pre> <span class='k14'>" +  tx02 + "</span>" ;	// エラーメッセージ出力
			}
		}else{
			tx03 = "<pre> <span class='k14'>" + tx01 + "</span>" ;		// エラーメッセージ出力
		}

		return tx03;
	}
	/**
	 * PMLテキストを解析してHTMLテキストを作成する
	 * 
	 * @return　　PMLをHTMLに変換したテキスト
	 */ 
	public String createHtml(){
		if(LOG.fa) LOG.println("class Exam #create() : 表示用HTMLを生成する の先頭です");
		//
		String	tx01	= null;
		//
		tx01 = doHMAC(originalText);	// ＥＭＬ原文をパース
		if(!isDone()){
			tx01 = "<pre> <span class='k14'>" + tx01 + "</span>" ;		// エラーメッセージ出力
		}
		
		return tx01.replace("_PCNT_","%").replace("_PERCENT_", "%");	// ％文字を復元する
		
		//return Regularizer.substitute(tx01,"_PCNT_","%");
	}
	/**
	 * 学生の解答を表すHTMLテキストを更新作成する<br>
	 *
	 * EPMLテキストと設問への解答を格納したハッシュから、問題への解答を表すHTMLテキストを作成する
	 * 具体的には以下のように、学生の解答を表すEPML(ans)から、解答をsetHash()でハッシュテーブルに取り出し
	 * 問題文(examText)の表示用初期値として合成する働きをする
	 * <code>
	 * 	ans.doEPML();												
	 *	ans.setHash(htb);
	 *	Exam	exam = new Exam(examText,emlConfPath);
	 *	temp		 = exam.update(htb);
	 * </code>
	 * 
	 * @param data　　	各設問への解答
	 * @return			問題への解答を表すHTMLテキスト
	 */
	public String update(Hashtable data){
		if(LOG.fa) LOG.println("class Exam #update() : data によって初期値を設定して表示する の先頭です");
		//
		String	tx01	= null;
		String	tx02	= null;
		String	tx03	= null;
		//
		tx01 = doEPML();					// EPMLをパース
		if(isDone()){
			tx02 = doHMAC( tx01);		// HMACをパース
			if(isDone()){
				tx03 =  substUpdate(data,tx02);	// EPML要素をHTMLに置き換え
			}else{
				tx03 = "<pre> <span class='k14'>" +  tx02 + "</span>" ;	// エラーメッセージ出力
			}
		}else{
			tx03 = "<pre> <span class='k14'>" + tx01 + "</span>" ;		// エラーメッセージ出力
		}
		//
		return Regularizer.substitute(tx03,"_PCNT_","%");
	}
	/**
	 * 解答のHTMLテキストを作成する最後のステップとして、設問項目名を具体的なHTMLに置き換える<br>
	 * 
	 * CreateInitializedHtml() は Replace の中で呼び出されて設問項目名を具体的なHTMLに置き換える
	 * 
	 * @param data		解答のハッシュテーブル
	 * @param txt		問題文を表すEPMLテキスト
	 * @return			解答を表すHTMLテキスト
	 */
	public	String	substUpdate(Hashtable data,String txt){
		if(LOG.fa) LOG.println("class Exam#substUpdate() : ■解答を表すHTMLテキストを生成する");
		//
		Hashtable				htb		= new Hashtable(100);
		CreateInitializedHtml	chtml	= new CreateInitializedHtml(data,epmlTemplatePath,enl,ewl,etf,erb,eta);	// 中間テキストに置き換えを実行するクラス
		Replace					rp		= new Replace(txt,htb,chtml);											// 汎用の置換えクラス
		//
		rp.replacing();		// 置換の実行
		htmlText			= rp.getText();	// 置換したテキストを得る
		//
		return	htmlText;
	}
	/**
	 * （学生の）解答を保持するExamオブジェクトから、正誤情報を含む解答のハッシュテーブルを作成する<br>
	 * 
	 * update()を呼び出す前に必ず実行してハッシュテーブルを作成する
	 * 正誤情報が付加されるのは採点済みの場合のみである
	 * 
	 * @param htb　　設問項目名をキーとする解答及び正誤情報のハッシュテーブル
	 */
	public	void	setHash(Hashtable htb){
		//
		enl.setHash( htb);
		ewl.setHash( htb);
		etf.setHash( htb);
		erb.setHash( htb);
		eta.setHash( htb);
		//
	}
	/**
	 * 問題文を表すHTMLテキストを作成する<br>
	 * doEPML()によって置き換えられた問題項目名を実際のHTMLに置き換える
	 * 
	 * @param txt　　	doEPML()によって置き換えられた中間テキスト
	 * @return			問題文を表すHTMLテキスト
	 */
	public	String	subst(String txt){
		if(LOG.fa) LOG.println("class Exam#create() : ■EPMLをパースした中間テキストをHTMLテキストに置き換える");
		//
		Hashtable	htb		= new Hashtable(100);
		CreateHtml	chtml	= new CreateHtml(epmlTemplatePath,enl,ewl,etf,erb,eta);	// 中間テキストに置き換えを実行するクラス
		Replace		rp		= new Replace(txt,htb,chtml);								// 汎用の置換えクラス
		//
		rp.replacing();		// 置換の実行
		htmlText			= rp.getText();	// 置換したテキストを得る
		//
		return	Regularizer.substitute(htmlText,"_PCNT_","%");
	}
	/**
	 * 正解を表すHTMLテキストを作成する<br>
	 * 
	 * doEPML()により正解EPMLを解析し、その結果から正解をハッシュテーブルに
	 * 取り出す．このハッシュテーブルを問題の表示項目として埋め込んだHTMLを
	 * 作成する．複数正解のあるETAなどでは、最初の正解が表示用として採用される 
	 * 
	 * @return　　正解を表示項目として埋め込んだHTMLテキスト
	 */
	public	String	correctText(){
		//
		Hashtable	ht	=	new Hashtable(100);
		doEPML();
		setHash(ht);
		return	update(ht);
	}
	/**
	 * Examオブジェクトの内部情報からEPMLテキストを逆生成する<br>
	 * 
	 * 主に、学生の解答を採点後に再構成するため呼ばれるメソッドである
	 * また、問題作成時にENLタイプの選択肢語順を固定するためにもこのメソッドが呼ばれる
	 * 
	 * @return		Examオブジェクトの内部情報から逆作成したEPMLテキスト
	 * 
	 */
	public	String	toEpml(){
		if(LOG.fa){
			LOG.println("class Exam#toEpml() : ■状態が変化したEPMLをEPML文に書き戻す処理 の先頭です");
			LOG.println("==== Exam: originalText ===");
			LOG.println(originalText);
		}
		//
		token				= new EpmlToken(originalText);
		StringBuffer	bf	= new StringBuffer(10240);
		int eof=1;
		try{
			while((eof=token.getToken()) > 0){
				int		id		= token.tknID();						// 4
				int		grp		= token.tknGroupNum();					// 1   ERB用
				int		grpENL	= token.tknGroupNumENL();				// 1 　ENL用
				String	seqNum	= String.valueOf(token.tknSeqNum());	// 2
				String	tkn 	= token.tknSTR();						// ＊水によって
				String	name	= token.tknSEQ();						// ERB(1,2)
				String	embeded	= "%@" + token.tknSEQ() + "%";			// %@ERB(1,2)%
				//
				String	ans		= "";
				String	temp	= "";
				int	gp		= -1;
				int	num		= -1;
				//
				switch(id){
				  case	EpmlToken.ENL:
					//
					int		x			= Integer.parseInt(seqNum);			// 要素番号（シーケンス番号）
					ans					= enl.getAns(grpENL-1,x-1);				// この要素の解答語
					String	userAnsNum	= enl.getStAnsNum(grpENL-1,x-1);		// （あれば）学生の解答した番号
					String	userEval	= enl.getEval(grpENL-1,x-1);			// （あれば）学生の解答の正誤
					//
					//学生の解答を表すEPMLの場合で、さらに採点も済んでいるときは~に続けて正誤（t/f）を記入しておく
					if((userAnsNum.length()>0)&&(userEval.length()>0)){
						ans	= ans + "~" + userAnsNum + "~" + userEval;
											
					//学生の解答を表すEPMLの場合で、採点が済んでいないときは解答番号のみ付記しておく
					}else if(userAnsNum.length()>0){
						ans	= ans + "~" + userAnsNum;
					}
					//
					bf.append("![" + ans + "/]");
					break;
					//
				  case	EpmlToken.EWL:		// EmbededWordLists
					//
					// ewl から各項目データを取り出して埋め込む
					//
					int				sq		= 	Integer.parseInt(seqNum);				// シーケンス番号（１オリジン）
					int				kk		=	sq - 1;									// ０オリジンの数
					StringBuffer	xbuf	= 	new StringBuffer(200);
					boolean 		cm		= 	false;									// "、"をつけるかどうかのフラグ
					int			max		= 	ewl.sizeOfItems(kk);						// sq は０オリジンに直して使う
					int			correct	= 	Integer.parseInt(ewl.getCorrectNum(kk));	// 正解番号
					for(int i=0; i<max; i++){
						if(cm)	xbuf.append(",");
						if(correct==(i+1)){
							xbuf.append("*");
						}
						xbuf.append(ewl.getItem(kk,i));
						cm	= true;
					}
					// 正誤番号があれば末尾に追加する
					String	evalStr	=	ewl.getEval(kk);	
					if(evalStr.length()>0){
						xbuf.append("~");
						xbuf.append(evalStr);
					}
					//
					ans	= xbuf.toString();
					bf.append("![" + ans + "/]");
					break;
					//
				  case	EpmlToken.ETF:		// EmbededTextFields
					//
					// 解答語の増減がある．全ての解答語を{ }内に列記すると共に、間違い語が指定してある場合は
					// ~ を付けた後に列記する．
					//
					int				k		= Integer.parseInt(seqNum) - 1;		// シーケンス番号（０オリジン）
					StringBuffer	bufETF	= new StringBuffer(200);
					//
					// 正解語リストまたは学生の解答
					// ※学生の解答の場合、解答語数は常に１個
					boolean 		cmb			= false;							// "、"をつけるかどうかのフラグ
					Vector			correctList	= etf.correctList(k);
					for(int	i=0; i<correctList.size(); i++){
						
					    if(cmb)	bufETF.append( SEP );	/* セパレータ |&| */
						
						bufETF.append( (String)correctList.get(i) );
						cmb = true;
					}
					// 不正解語リスト
					// ※学生の解答データの場合は不正解語リストは常に空である
					// ※問題データの場合は解答の正誤は常に "" である
					// したがってここでは不正解語リスト処理か正誤処理か、どちらか一方の処理のみが実行される
					//                                               ^^^^^^^^^^^^^^^^^^^^^^^^^^
					// ■不正解語リスト処理
					cmb						= false;
					Vector	incorrectList	= etf.incorrectList(k);
					for(int	i=0; i<incorrectList.size(); i++){
						if(!cmb) bufETF.append("~");
						
						if(cmb)	 bufETF.append( SEP );	/* セパレータ |&| */
						
						bufETF.append( (String)incorrectList.get(i) );
						cmb = true;
					}
					//
					// ■正誤処理 ( ".t" か ".f" を付加する） 
					String	test	=	etf.getEval(k);
					if(test.length()>0){
						bufETF.append("~");
						bufETF.append("." + test);
					}
					//
					//
					bf.append("!{" + bufETF.toString() + "/}");
					break;
					//
				  case	EpmlToken.ERB:		// EmbededRadioButtons
					//
					// 解答文文字列を作成する
					// ckSign はチェックボックスかどうかを示す．true ならチェックボックスである．
					// 学生の解答EPMLテキストでは、その生成時に該当すれば付加されている．
					// eval は採点の結果付加されるもので、解答の正誤を表す文字（t/f）である
					//
					// 解答文字列は最長の場合、あいうえお~ck~t  などとなる
					//
					int j			=	Integer.parseInt(seqNum);			// 現在のシーケンス番号
					StringBuffer	ansbuf	=	new StringBuffer(1000);
					boolean		ckSign	=	erb.getCksign(grp-1);		//
					String			eval	=	erb.getEval(grp-1,j-1);		// 
					ansbuf.append( erb.dispOf(grp-1,j-1) );					// 現在の位置の解答語句
					if(ckSign){
						ansbuf.append( "~ck"); 	
					}
					if(eval.length()>0){
						ansbuf.append("~" + eval);
					}
					ans	=	ansbuf.toString();
					//
					if(erb.isCorrectAns(grp-1,j-1)){						// 現在の位置の解答語句は正解か
						bf.append("#[*" + ans + "/]");
					}else{
						bf.append("#[ " + ans + "/]");
					}
					break;
						
				  case	EpmlToken.ETA:		// EmbededTextAreas
					//
					int			m			= Integer.parseInt(seqNum) - 1;		// シーケンス番号（０オリジン）
					StringBuffer	bufETA		= new StringBuffer(200);
					//
					// 正解語リスト
					boolean 		cma			= false;							// "、"をつけるかどうかのフラグ
					Vector			listETA		= eta.correctList(m);
					for(int	i=0; i<listETA.size(); i++){
						
					    if(cma)	bufETA.append( SEP );	/* セパレータ |&| */
						
					    bufETA.append( (String)listETA.get(i) );
						cma = true;
					}
					bf.append("#{" + ans + bufETA.toString() + eta.getSizeString(m) + "/}");
					break;
					//
				  case	EpmlToken.EPT:		// EmbededPoinT
					bf.append("[@" + tkn + "/]");
					break;
					//
				  case	EpmlToken.TIME:		// TimeLimit
				  	if(LOG.fa){
				  		LOG.println("");
				  		LOG.println("■■■　find EpmlToken.TIME ■■■");
				  		LOG.println("");
				  	}
				  	bf.append("{@" + tkn + "/}");
				  	break;
					//
				  case	EpmlToken.AENL:		// ダミーの選択肢
					bf.append("[&" + tkn + "/]");
					break;
					//
				  case	EpmlToken.ALST:	// 選択肢一覧表表示場所
					bf.append("[+" + tkn + "/]");
					break;
				  	
				  case	EpmlToken.TEXT:		// text
					default:
					bf.append(tkn);
					break;
				}
			}
			//
			modifiedText	= bf.toString();
			doneFlag		= true;	// 完了
			//
		}catch(EpmlTokenException e){
			String 	CR 		= System.getProperty("line.separator");
			int	pos		= e.getMessageValue();
			String 	msg 	= e.getNontitleMessage();
			String 	temp 	= originalText.substring(0,pos) + "<span style='color:red; font-size: 14px'><== 誤りを検出 ==></span>" + CR + CR +  msg;
			modifiedText	= temp;
			doneFlag		= false;	// 未完了
			return modifiedText;
		}
		if(LOG.fa) LOG.println("【変換EPML文】");
		if(LOG.fa) LOG.println(modifiedText);
		
		return	modifiedText;
	}
	
	/**
	 * 解答を表すEPMLテキストを初期生成する<br>
	 * 
	 * 問題文への解答を表すフォームデータのハッシュテーブルと問題文自体から、
	 * 解答を表すEPMLテキストを作成する．このEPMLテキストはそのまま表示に用いること
	 * はできない．一度Examオブジェクトに直してから update() により表示用HTMLを生成する
	 *
	 * @param htb     学生の解答（フォーム変数）を格納したハッシュ
	 * @return　　　　解答を表現するEPMLテキスト
	 */
	public	String	createAnswerText(Hashtable htb){
		if(LOG.fa) LOG.println("class Exam#createAnswerText() : ■ハッシュに入った解答語をEPML原文に埋め込んだテキストを返す の先頭です");
		
		//
		// 一度ＥＰＭＬをパースしてメンバーデータを生成しておく
		// これによって、選択肢の語群やドロップダウンリストのメンバーデータなどが作成済みとなる
		doEPML();
		//
		// 以下でもう一度パースして解答語を埋め込む
		// 番号で答えているものなどは、文字列に変換して埋め込まなくてはいけない
		//
		boolean setAnswerFlag	= false;	// ERB で＊のついた選択肢があれば、これをセットする．
											// 最後までこれがfalseのままだと、ERB に対して未解答ということが分かる
		token				= new EpmlToken(originalText);
		StringBuffer	bf	= new StringBuffer(10240);
		int eof=1;
		try{
			while((eof=token.getToken()) > 0){
				int 	id		= token.tknID();						// 4
				int		grp		= token.tknGroupNum();					// 1   ERB用
				int		grpENL	= token.tknGroupNumENL();				// 1 　ENL用
				String	seqNum	= String.valueOf(token.tknSeqNum());	// 2
				String	tkn 	= token.tknSTR();						// ＊水によって
				String	name	= token.tknSEQ();						// ERB(1,2)
				String	embeded	= "%@" + token.tknSEQ() + "%";			// %@ERB(1,2)%
				//
				String	ans	= "";
				String temp	= "";
				int		gp	= -1;
				int		num	= -1;
				//
				switch(id){
				  case	EpmlToken.ENL:
					// そのままユーザーの解答語を埋め込むと問題の解答語や語順が狂ってしまうので
					// ユーザーの解答番号を正解の後に付加したものを埋め込む．
					// ユーザーの解答番号はcENL を生成するときに分離して、userAnsNumber に書き込まれる
					// 未解答は "0" が解答番号として書き込まれる
					//
					int		x			= Integer.parseInt(seqNum);		// 要素番号（シーケンス番号）
					String	trueAns		= enl.getAns(grpENL-1,x-1);		// この要素の解答語
					temp	= getDigit( strHashZERO(htb,name),"0");		// 数値文字列にして返す．不正なら "0" を返す
					ans		= trueAns + "~" + temp;						// この要素の解答語＋ユーザーの解答番号
					bf.append("![" + ans + "/]");
					break;
					//
				  case	EpmlToken.EWL:		// EmbededWordLists
					//
					// ユーザーが解答した番号によって［aaa,bbb,ccc］という解答語のいずれかに＊印をつけ、解答語として登録する
					// 未選択状態の場合は、［aaa、bbb、ccc、*《未選択》] という解答語を作成する
					// どれか選択されていないと後でパースできないからである．
					//
					int sq	= Integer.parseInt(seqNum);				// シーケンス番号（１オリジン）
					temp	= toDigit( strHashZERO(htb,name),"0");	// 数値文字列にして返す．不正なら "0" を返す
					num		= Integer.parseInt(temp);				// 選択されたEWL の値（１オリジン）．
					//
					StringBuffer	xbuf	= new StringBuffer(200);
					boolean 		cm		= false;					// "、"をつけるかどうかのフラグ
					int			max		= ewl.sizeOfItems(sq-1);	// sq は０オリジンに直して使う
					for(int i=0; i<max; i++){
						if(cm)	xbuf.append("、");
						if(num==(i+1)){
							xbuf.append("*");
						}
						xbuf.append(ewl.getItem(sq-1,i));		// sq は１オリジンなので０オリジンに直して
						cm	= true;
					}
					if(num==0){ 								// 何も選択されていなかった場合
						xbuf.append("、*" + NOT_DISCRIBED);		// 解答語"未選択"を追加しこれに正解マークを付けておく
					}
					ans	= xbuf.toString();
					bf.append("![" + ans + "/]");
					break;
					//
				  case	EpmlToken.ETF:		// EmbededTextFields
					//
					// 正解語や不正解語は ans に含めない
					// 解答としての正誤が null になるときは "" を返す
					ans	= strHashSP(htb,name);						
					if((ans.trim()).length()==0){
						ans	= NOT_DISCRIBED;
						//ans	= "";
					}
					bf.append("!{" + ans + "/}");
					break;
					//
				  case	EpmlToken.ERB:		// EmbededRadioButtons
					//
					// #[aaa}
					// #[bbb}  というように複数の行からなるので、ユーザーの選択番号から、選択されていたもの
					// には＊を付けて #[*bbb} を埋め込む
					// 全項目について最後の処理かどうかを登録されている選択肢の数から判断し、もしそうならば
					// "どれも選択されていない" という状態でないかどうか調べる．（どれか選択されていないと後でパースできない）
					// もしどれも選択されていない時は、#[*《未選択》] という項目を追加しておく
					//
					// (注意)ここでパースで得られる name (キー名)は 常に引数二つの"ERB(1,2)" の形式である．しかし、
					//      hash 内のキーはラジオボタンの時は "ERB(1)" であり、チェックボックスの時は"ERB(1,2)"で
					//　　　入っている．以下でhtb をname で引く特はこの点を注意して使い分けねばならない．
					//
					
					
					// チェックボックス
					//   チェックボックスでひとつしかチェックが入っていないと、それをEPMLにしたとき*付き項目がひとつ
					//   になる．これを再度パースするとラジオボタンと解釈されてしまう．
					//   そこで、ユーザーの解答では、チェックボックスの場合は選択語の後全てに"~ck"を不可することにした
					//   "~ck"は cERB を生成する時は分離して、メンバー変数 chkbox に記録を残す
					//
					if(erb.isCheckbox(grp-1)){
						int j	= Integer.parseInt(seqNum);				// 現在のシーケンス番号
						ans		= erb.dispOf(grp-1,j-1);				// 現在の位置の解答語句
						temp	= toDigit( strHashZERO(htb,name),"0");	// 解答番号（seqNum と同じ値、選択されてないと"0"が返る）
						if(seqNum.equals(temp)){
							bf.append("#[*" + ans + "~ck/]");
							setAnswerFlag	= true;					// 解答をセットしたことを記憶しておく
						}else{
							bf.append("#[ " + ans + "~ck/]");
						}
					// ラジオボタン
					}else{
						//
						// 例えば htb.get("ERB(1)") を実行するのでいつでも選択された番号として同じ答え（例えば temp="2"）が返ってくる 
						// したがって、現在のシーケンス番号(seqNum)と同じなら ＊印をつける
						//
						int j	= Integer.parseInt(seqNum);												// 現在のシーケンス番号
						ans		= erb.dispOf(grp-1,j-1);												// 現在の位置の解答語句
						temp	= toDigit( strHashZERO(htb,"ERB(" + String.valueOf(grp) + ")"),"0");	// 解答番号
						if(seqNum.equals(temp)){
							bf.append("#[*" + ans + "/]");
							setAnswerFlag	= true;						// 解答をセットしたことを記憶しておく
						}else{
							bf.append("#[ " + ans + "/]");
						}
					}
					//
					// 全て未選択になっていないか調べそうであれば［*《未選択》］を追加する
					int	ansSize	= erb.getAnsSize(grp-1);			// 第grpグループの選択肢の数
					if(num == ansSize){								// 最後の選択しまで処理済みか
						if(!setAnswerFlag){							// どこかに＊をつけたか（何かを選択していたか）
							bf.append("#[*" + NOT_SELECTED + "/]");		// 選択がなければ未選択を追加する
						}
						setAnswerFlag	= false;	// 初期化しておく
					}
					break;
						
				  case	EpmlToken.ETA:		// EmbededTextAreas
					ans	= strHashSP(htb,name);
					if((ans.trim()).length()==0){
						ans	= NOT_DISCRIBED;
					}
					int	k = token.tknSeqNum() -1;
					bf.append("#{" + ans + eta.getSizeString(k) + "/}");
					break;
					
				  case	EpmlToken.EPT:		// EmbededPoinT
					bf.append("[@" + tkn + "/]");
					break;
					//
  				  case	EpmlToken.TIME:		// TimeLimit
				  	bf.append("{@" + tkn + "/}");
					break;
					//
				  case	EpmlToken.AENL:		// ダミーの選択肢
					bf.append("[&" + tkn + "/]");
					break;
					//
				  case	EpmlToken.ALST:	// 選択肢一覧表表示場所
					bf.append("[+" + tkn + "/]");
					break;
				  	
				  case	EpmlToken.TEXT:		// text
					default:
					bf.append(tkn);
					break;
				}
			}
			//
			preHtmlText	= bf.toString();
			doneFlag	= true;	// 完了
			//
		}catch(EpmlTokenException e){
			String CR = System.getProperty("line.separator");
			int		pos	= e.getMessageValue();
			String 	msg = e.getNontitleMessage();
			String 	temp = originalText.substring(0,pos) + "<span style='color:red; font-size: 14px'><== 誤りを検出 ==></span>" + CR + CR +  msg;
			preHtmlText	= temp;
			doneFlag	= false;	// 未完了
			return preHtmlText;
		}
		if(LOG.fa){
			LOG.println("★ EPML 解答埋め込み処理の結果");
			LOG.println(preHtmlText);
		}
		/*
		 * < > \ " ' を特殊文字に変換して返す
		 */
		return	regularize(preHtmlText);
	}

	//
	//
	public	String 		getPreHtmlText()	{ return 	preHtmlText; 	}	// EPMLの中間テキストを返す
	public String 		getHtmlText()		{ return  	htmlText; 		}
	public String 		getLastHtmlText()	{ return  	lastHtmlText; 	}
	public	boolean	isDone()			{ return 	doneFlag;		}	// 完了したかどうか
	/**
	 * EPMLテキストを解析して問題項目名に置き換え、問題情報を内部オブジェクトに蓄積する
	 * 
	 * @return　問題項目名に置き換えられた中間テキスト
	 */
	public String	doEPML() {

		init();	// メンバー変数の初期化
		//
		Vector		order	= new Vector(10,10);	// EPML中に指定されていたALSTの語順を記憶しておくため．作業用
		//
		token				= new EpmlToken(originalText);
		//
		StringBuffer	bf	= new StringBuffer(10240);
		
		int eof=1;	// トークン種別を表す数値（１はENLだがここではダミー
		
		//getToken()はトークンがなくなると0を返す
		try{
			while((eof=token.getToken()) > 0){
				
				int 	id		= token.tknID();						// トークン種別番号
				String	tkn 	= token.tknSTR();						// 取り出したトークン
				String	name	= token.tknSEQ();						// 埋め込みシーケンス文字列 ex. ERB(1,2)
				String	embeded	= "%@" + token.tknSEQ() + "%";			// %@ERB(1,2)% 置き換えできる形式にしたもの

				String	seqNum	= String.valueOf(token.tknSeqNum());	// トークンの問題中のシーケンス番号
				int		grpENL	= token.tknGroupNumENL();				// 番号を埋める問題の現在のグループ番号
				int		grp		= token.tknGroupNum();					// ラジオボタン、チェックボックス問題の現在のグループ番号
				
				switch(id){
				  case	EpmlToken.ENL:		// EmbededNumberLists
				  	enl.add(name,grpENL,seqNum,tkn,assignedPoint,fontLevel);
					bf.append(embeded);
					break;
					
				  case	EpmlToken.EWL:		// EmbededWordLists
				  	try{
						ewl.add(name,seqNum,tkn,assignedPoint,fontLevel);
					}catch(EpmlTokenException e){
						e.setMessageValue(token.tknPOS());	// 現在のバッファ位置
						throw e;
					}
					bf.append(embeded);
					break;
					//
				  case	EpmlToken.ETF:		// EmbededTextFields
				  	etf.add(name,seqNum,tkn,assignedPoint,fontLevel);
					bf.append(embeded);
					break;
					//
				  case	EpmlToken.ERB:		// EmbededRadioButtons
				  	erb.add( String.valueOf(grp),tkn,assignedPoint,fontLevel);
					// チェックボックスもラジオボタンも%ERB(a,b)%を埋め込む．
					bf.append(embeded);
					break;
						
				  case	EpmlToken.ETA:		// EmbededTextAreas
				  	try{
						eta.add(name,seqNum,tkn,assignedPoint,fontLevel);
					}catch(EpmlTokenException e){
						e.setMessageValue(token.tknPOS());	// 現在のバッファ位置
						throw e;
					}				
					bf.append(embeded);
					break;
					
				  case	EpmlToken.EPT:		// EmbededPoinT
				  	int			sz	= tkn.length();
					StringBuffer	sbf	= new StringBuffer(100);
					for(int i=0; i<sz; i++){
						char c = matchDigit(tkn.charAt(i));
						if(c!='*'){
							sbf.append(c);
						}else{
							break;
						}
					}
					String	pt	= sbf.toString();
					if(LOG.fa) LOG.println("□ pt=" + pt);
					if(pt.length()==0){
						LOG.println("配点の指定が不正 : (tkn) = ("  + tkn + ")"  );
					}else{
						assignedPoint	= Integer.parseInt(pt);	// 配点を設定しておく
					}
					break;
				  	//
				  case	EpmlToken.TIME:		// 制限時間
					if(LOG.fa) LOG.println("★ 制限時間 :" + tkn);
					int			timeSZ	= tkn.length();
					StringBuffer	timeBUF	= new StringBuffer(100);
					for(int i=0; i<timeSZ; i++){
						char c = matchDigit(tkn.charAt(i));
						if(c!='*'){
							timeBUF.append(c);
						 }else{
						  	break;
						 }
					}
					String	timeLimitStr= timeBUF.toString();
					if(timeLimitStr.length()==0){
						LOG.println("配点の指定が不正 : (tkn) = ("  + tkn + ")"  );
					}else{
						timeLimit	= Integer.parseInt(timeLimitStr);	// 配点を設定しておく
						if(LOG.fa) LOG.println("■ class Exam #doEPML() : 制限時間は<" + timeLimit + ">に設定します");
					}
					break;
					//
				  case	EpmlToken.AENL:		// ダミーの選択肢
				  	enl.addDummyAnswer(grpENL,tkn);
					break;
					//
				  case	EpmlToken.ALST:	// 選択肢一覧表表示場所
				  	//
					////////////////////// [ 語順の指定を取り込む ]///////////////////////////////////////////////////////////////////////////////////
					//
					String	paramStr	= getString(name,'(',')');		// 例： name = "ALST(1/4/3-2-4-1)" または、"ALST(1/4)"、または、"ALST(1/5)"
					if(LOG.fa) LOG.println("□ paramStr=" + paramStr);
					
					Csv		parameter	= new Csv(paramStr,"/");		//      1: シーケンス番号、4: 語群表示列数、3-2-4-1: 語順
					if(parameter.size() == 3){			// 語順指定があれば
						order.add(parameter.get(2));	// "3-2-4-1"
					}else{
						order.add("");					// ない場合は ""
					}
				  	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
					bf.append(embeded);
					break;
				  	
				  case	EpmlToken.TEXT:		// text
				  	default:
					int fn	= fmt(tkn);	// フォーマット指定でないか調べる
					if(fn > 0){			// そうであればレベルを記録しておく
						fontLevel	= fn;
					}
					//DBG.println("■《" + tkn + "》-----> " +  fontLevel);
					bf.append(tkn);
					break;
				}
			}
			if(!erb.checkCorrects()){
				
				throw new EpmlTokenException(token.tknPOS(),"＃[ ･･･ ]で指定された選択形式の問題で、どれが正解か指定されていないものがあります．");
				
			}else{
				//
				// order(i) には "" か "3-2-4-1" のような語順がある
				if(LOG.fa) LOG.println("□□□ order.size() =" + order.size());
				if(LOG.fa) LOG.println("□□□   enl.size() =" +  enl.size());
				if((order.size()==0)&&(enl.size() > 0)){
					if(order.size()==0) LOG.println("order.size()==0");
					if(enl.size() > 0)  LOG.println("enl.size() > 0");
					throw new EpmlTokenException(token.tknPOS(),"選択語の語群リストを表示する位置を指示してください");
				}
				enl.setAnsNumberAll(order); // 解答選択肢番号をセットする
				//
				int n = order.size();		// order に語順がないものは新規なので EPML を修正する
				for(int k=0; k<n; k++){
					String	str = (String)order.get(k);
					int	sizeByOrderString	= sizeOfList(str);
					int	sizeOfObjects		= enl.allCount(k);		// ダミーを含む総個数
					if(sizeOfObjects==0){
						throw new EpmlTokenException(token.tknPOS(),"選択語の語群リスト表示位置が指定してありますが、![ ･･･ /] 形式の問題項目がひとつも見つかりません");
					}else{
						if(sizeByOrderString!=sizeOfObjects){			// 選択語群の並び順がまだオリジナルテキストに記載されていないか変更された
							String fmt		= enl.getPermutation(k);	// パースして設定された選択語群の並び順を得て
							//////////////////////////////////
							if(LOG.fa) LOG.println("★★★ fmt =" + fmt);
							originalText 	= doALST(k+1,fmt);			// オリジナルテキストの該当位置に挿入する．グループ番号k+1 は１オリジン．
							//////////////////////////////////
							if(LOG.fa) {
								LOG.println("■ 修正EPML原文 ---");
								LOG.println(originalText);
								LOG.println("----- ここまで ----");
							}
						}
					}
				}
				preHtmlText	= bf.toString();
				doneFlag	= true;	// 完了
			}
		}catch(EpmlTokenException e){
			String CR = System.getProperty("line.separator");
			int		pos	= e.getMessageValue();
			String 	msg = e.getNontitleMessage();
			String 	temp = originalText.substring(0,pos) + "<span style='color:red; font-size: 14px'><== 誤りを検出 ==></span>" + CR + CR +  msg;
			preHtmlText	= temp;
			doneFlag	= false;	// 未完了
			return preHtmlText;
		}
		if(LOG.fa){
			LOG.println("★ EPML 解析データ");
			LOG.println(preHtmlText);
		}
		return	preHtmlText;
	}
	//
	//
	public	int	sizeOfList(String str){
		if((str==null) || (str.length()==0)) return 0;
		// "-" で幾つに分けられるか調べて個数を返す
		Csv	cs	= new Csv(str,"-");
		return	cs.size();
	}
	//
	// フォーマット指示からフォントレベルを取得する
	int		fmt(String	str){
		int	pos	= str.lastIndexOf("##");
		if(pos < 0) return	-1;
		//
		String	sub	= str.substring(pos);
		if(sub.length() < 5)	return -1;
		//
		char	c1 = sub.charAt(3);		// ４文字目
		char	c2 = sub.charAt(4);		// ５文字目
		if((c1!='h')&&(c1!='m')&&(c1!='s'))		return	-1;
		//
		if(matchDigit(c2)=='*')	return	-1;
		//
		return	Integer.parseInt(String.valueOf(c2));
	}
	/**
	 * PMLテキストを解析してHTMLテキストを作成する
	 * 
	 * テキスト中にある'<'などの危険な文字は regularize()によって最初に無害化してから変換を始める
	 * 
	 * @param HMACText　	PMLテキスト
	 * @return				HTMLテキスト
	 */
	public String	doHMAC(String textStr){
		if(LOG.fa) LOG.println("class Exam#doHMAC() : ■PMLをパースし、ＨＴＭＬテキストを作成");
		if(LOG.fa) LOG.println("           HMACText =>>> " + textStr);
		/*
		 * オリジナルテキストの < > " を特種文字に変換する
		 */
		String	HMACText	=	regularize(textStr);
		originalText		=	HMACText;
		//		
		StringBuffer	textBuf	= new StringBuffer(10240);					// 全体のHTMLを格納するバッファ
		StringBuffer	bf		= new StringBuffer(10240);					// フォーマットで区切られる部分HTMLを格納するバッファ
		String			wtext	= null;									// ある時点でのフォーマットスケルトン
		//
		pi	=	new	ParserInitializer(html_templatePath ,imgURL, imgAbsPath, sysImgURL,  sysImgAbsPath, htmlFlag, parserInitialFile);
		PmlToken pmlToken	= new PmlToken(HMACText, pi);
		
		int 			eof		= 1;
		int				fmt		= 0;
		boolean		flowFlag= true;
		try{
			while((eof=pmlToken.nextToken()) > 0){	// 一区切りの文を切り取る
				int 	id		= pmlToken.ID();		// 文の種類（フォーマット文、修飾指示語、一般の文字列）
				switch(id){
				  /*
				   * ## による書式指定が出現してた場合の処理
				   * (1) それまで蓄えていた１段落分のデータを現在の wtext に埋め込んで完成した１段落分のHTMLを得る
				   * (2) 新しい書式指定から段落フォーマットのHTMLを得てwtextに格納しておく
				   */
				case	PmlToken.FORMAT:// ■フォーマットスケルトンＨＴＭＬ
					int	n	= bf.length();
					if(n>0){// 部分HTMLがある
						if(wtext==null){
							wtext	= pmlToken.getSTDFormat();	// std タイプのHTMLを返す
						}else{
							fmt--;  // 持っていたフォーマットを消費した
						}
						String	emb	= bf.toString();						// 現在のフォーマットスケルトンに格納する部分HTML
						/*
						 * flow モードなら全ての改行コードを <br> に変換しておく
						 */
						if(flowFlag){
							emb	=	Regularizer.replaceToBR(emb);
							//if(DBG.fa) DBG.println("emb flow mode:" + emb);
						}
						textBuf.append( assemble(wtext,emb) );				// 部分HTMLをスケルトンに書き込み、出力バッファに追加する
						bf.delete(0,n);										// 部分HTMLのバッファをクリアしておく
					}
					wtext		= 	pmlToken.TOKEN();	// 新しいフォーマットスケルトン
				  	flowFlag	=	pmlToken.getFlowMode();
					//if(DBG.fa) DBG.println("flowFlag: " + flowFlag);	
				  	fmt++;	// フォーマットを生成した
					break;
				  	//
				  //
				  /* case WikiToken.TABLE: 
				   *  テーブル作成機能を入れるためにここに追加予定
				   *  表内容のデータをWikiToken から取得し、
				   *  フォーマットと合成し出力する
				   */

				case	PmlToken.HTMLS:// ■フォーマットＨＴＭＬの %_htmls% に埋め込む部分HTML
					/*
					 * 色の設定など段落内での書式指定が出現した場合の処理
					 * 対応するHTMLスケルトンをpreHtmlに、埋め込むデータをpreTargetに得て合成する
					 * preTarget内にさらに他の書式指定がある可能性から、再帰的に処理する 
					 */
					String	preHtml		= pmlToken.TOKEN();					// 修飾指示語に相当するHTML
					String	preTarget	= pmlToken.TARGET();				// 修飾される文字列など
					/*
					 * 再帰的に修飾語を含む可能性があるので再帰的にパースする
					 * FormatStyle は pml 文書の全体的な設定事項なので引数として渡す
					 * base などが未定義になるのを防ぐ目的である
					 * 
					*/
					String	target		= doHMAC_recur(preTarget,pmlToken.getFormatStyle()) ;	

					/*
					 * HTMLを置き換える
					 */
					Hashtable	ht		= new Hashtable(10);
					ht.put("_string",target);					// 修飾される文字列をハッシュに入れ
					String	subText		= replace(preHtml,ht);	// 部分HTMLに埋め込む（preHtml について htb の要素で置き換える）
					bf.append(subText);							// 部分HTMLのバッファに追加しておく
					break;
					//
				  
				  /*
				   * 通常の文字列
				   * そのままバッファに追加しておく
				   * WikiToken.FORMAT:の処理で、ひとつの段落を構成するデータ部分としてHTMLに埋め込まれる
				   */
				default:
					String	str	=	pmlToken.TOKEN();
					bf.append(str);
					break;
				}
			}
		}catch(PmlTokenException e){
			//
			String 	CR  = System.getProperty("line.separator");
			int		pos	= e.getMessageValue();
			String 	msg = e.getNontitleMessage();
			String 	temp = HMACText.substring(0,pos) + "<span style='color:red; font-size: 14px'><== 誤りを検出 ==></span>" + CR + CR +  msg ;
			lastHtmlText		= temp;
			doneFlag	= false;	// 未完了
			return lastHtmlText;
		}
		
		if(bf.length()>0){
			/*
			 * 最後はデータ部分で終わった
			 */
		    String	emb	= bf.toString();		// 最後に残った部分HTMLを
			if(wtext==null){
				wtext = pmlToken.getSTDFormat();		// std タイプのHTMLを返す

			}else{
				fmt--;

			}
			/*
			 *  フローモードなら改行を<br>に直す
			 */ 
			if(flowFlag){
				emb	=	Regularizer.replaceToBR(emb);
			}
			
			textBuf.append( assemble(wtext,emb) );			// 現在のフォーマットスケルトンに埋め込んでバッファに追加しておく

		}else if(fmt > 0){	// 最後の行は ## だったかどうか
			textBuf.append( assemble(wtext,"") );

		}
		/*
		 * mikaType用・入力問題用のパラメータがあれば受け取っておく
		 * 常に値があるわけではないので，criterionの要素数を調べてから使う
		 * 
		 * 対象名と評価基準テーブル
		 * 
		 */
		criterion		=	pmlToken.getCriterion();
		criterionName	=	pmlToken.getCriterionName();
		if(LOG.fa){
		    if(LOG.fa) LOG.println(" ");
		    if(LOG.fa) LOG.println("■ Exam #doHMAC() ");
		    if(LOG.fa) LOG.println("     criterion     = " + criterion);
		    if(LOG.fa) LOG.println("     criterionName = " + criterionName);
		    
		}
		
		/*
		 * 完成したHTMLを返す
		 * 指示子等でエスケープされたものは先頭に~がついたままになっているのでここで~を取り除く
		 */
		lastHtmlText 	=	spliter( textBuf.toString() ); 
		doneFlag		=	true;	
		return			lastHtmlText;
	}
	/**
	 * 指示子等でエスケープされたものは先頭に~がついたままになっているのでここで~を取り除く
	 * @param str
	 * @return
	 */
	String	spliter(String	str){
		String	html_1	=	Regularizer.replace(str,"~##", "##");
		String	html_2	=	Regularizer.replace(html_1,"~(#", "(#");
		String	target	=	Regularizer.replace(html_2,"~(/", "(/#");
		
		return	target;
	}
	
	//
	// ターゲット項を再帰的にパースして完成したHTMLを返す
	//
	private String	doHMAC_recur(String HMACText,FormatStyle fst) throws PmlTokenException {

	    if((HMACText==null)||(HMACText.length()==0))  return "";
	    pi				=	new	ParserInitializer(html_templatePath ,imgURL,imgAbsPath, sysImgURL, sysImgAbsPath, htmlFlag, parserInitialFile);
		PmlToken	wkt	= 	new PmlToken(HMACText, pi);
		
		/*
		 * WikiToke を生成するが、FormatStyle は上位から受け取ったものを利用する
		 */
		wkt.setFormatStyle(fst);
		//
		StringBuffer	bf	= new StringBuffer(10240);
		int eof=1;
		try{
			while((eof=wkt.nextToken()) > 0){
				int 	id		= wkt.ID();//
				switch(id){
				  case	PmlToken.FORMAT:// フォーマットＨＴＭＬ
				  	//
					throw   (new PmlTokenException(wkt.tknPOS(),"ここにフォーマット指定は書けません．"));
					//
				  case	PmlToken.HTMLS:// フォーマットＨＴＭＬの %_htmls% に埋め込むHTML
					//
					String	preHtml		= wkt.TOKEN();
					String	preTarget	= wkt.TARGET();
				  	String	target		= doHMAC_recur(preTarget,wkt.getFormatStyle());
					//
					Hashtable	ht		= new Hashtable(10);
					ht.put("_string",target);
					String	subText		= replace(preHtml,ht);	// preHtml について htb の要素で置き換える
					bf.append(subText);
					break;
					//
				  default:	// フォーマットＨＴＭＬの %_htmls% に埋め込むデータ
					String	str	=	wkt.TOKEN();
					bf.append(str);
					break;
				}
			}
		}catch(PmlTokenException e){
			//
			if(e.getMessageValue() != PmlTokenException.EndOfBuffer){
				throw e;
			}else{
				return	bf.toString();	// 現在までのHTMLを持って帰る
			}
		}
		return	bf.toString();	// 完成したHTMLを返す
	}
	/**
	 * ひとつの段落テキストを作成する．テキストはHTMLである．
	 * @param wtext	段落のスケルトンHTML
	 * @param emb		段落内のデータ
	 * @return			完成したHTMLテキスト
	 */
	//
	//  フォーマットHTMLに埋め込みテキストを埋め込んで返す
	//
	String assemble(String wtext,String emb){
		//System.out.println("assemble");
		//
		String	formatText = wtext;
		if(formatText==null){
			TemplateBox tb 	= new TemplateBox(html_templatePath);	// 集合テンプレートファイルオブジェクト
			formatText		= tb.get("std");						// 規定値のフォーマット
		}
		Hashtable	htb	= new Hashtable(10);
		htb.put("_htmls",emb);
		//
		return replace(formatText,htb,true);
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
	String	replace(String htmls,Hashtable htb,boolean crlf){
		//System.out.println("replace");
		//
		StringWriter 	sw 	= new StringWriter(10240);
		PrintWriter		out	= new PrintWriter(sw);
		PrintView		pv	= new PrintView(out);
		if(!crlf)		pv.crlf_off();
		pv.PrintString(htb,htmls,new OptionPrint());
		//
		String s = sw.toString();
		return s;
	}
	// 文字列が数字がどうかチェックする
    boolean isDigit(String s){
        if((s == null)||(s.length()==0))    return false;
		//
        StringBuffer bf = new StringBuffer(100);
		int len = s.length();
        for(int i=0; i<len; i++){
            char ch = matchDigit(s.charAt(i));
            bf.append(ch);
			if(ch == '*')       return false;
        }
        return true;
    }
	// 数字文字列にして返す
    String toDigit(String s){
		return	toDigit(s,"");
	}
    String toDigit(String s,String t){
        if((s == null)||(s.length()==0))    return "";
		//
        StringBuffer bf = new StringBuffer(100);
		int len = s.length();
        for(int i=0; i<len; i++){
            char ch = matchDigit(s.charAt(i));
			if(ch == '*')       return t;
            bf.append(ch);
        }
        return bf.toString();
    }
    /**
     * 数値文字列部分を抜き出して返す
     * @param s
     * @param t		不正な値のとき返すべきも数値文字列
     * @return
     */
    String	getDigit(String s, String t){
        
        if((s == null)||(s.length()==0))    return t;
        StringBuffer bf = new StringBuffer(100);
		int 		len = s.length();
        for(int i=0; i<len; i++){
            char ch = matchDigit(s.charAt(i));
            if(ch != '*'){
                bf.append(ch);
            }
        }
        /*
         * １文字以上あればそれを返す
         */
        if(bf.length()>0){
            return bf.toString();
        }else{
            return	t;
        }
        
    }
	// 数字へのマッチ
	private char	matchDigit(char c){
		      if((c=='0')||(c=='０')){	return	'0';
		}else if((c=='1')||(c=='１')){	return	'1';
		}else if((c=='2')||(c=='２')){	return	'2';
		}else if((c=='3')||(c=='３')){	return	'3';
		}else if((c=='4')||(c=='４')){	return	'4';
		}else if((c=='5')||(c=='５')){	return	'5';
		}else if((c=='6')||(c=='６')){	return	'6';
		}else if((c=='7')||(c=='７')){	return	'7';
		}else if((c=='8')||(c=='８')){	return	'8';
		}else if((c=='9')||(c=='９')){	return	'9';
		}else{ return '*';}
	}
	// ---- STATIC ----
	//
	// 文字列が空かどうか
	public static boolean isEmptyData(String str){
		if(str==null) 					return  true;
		if((str.trim()).length()==0)	return  true;
		return false;
	}
	// ハッシュテーブルからキーで値を検索して返す
	// null になる場合は "-" を返す
	public static String strHashBAR(Hashtable htb,String key){
		//
		String	str	= (String) htb.get(key);
		if(isEmptyData(str)){
			str = "-";
		}
		return 	str;
	}
	// ハッシュテーブルからキーで値を検索して返す
	// null になる場合は "" を返す
	public static String strHashSP(Hashtable htb,String key){
		//
		String	str	= (String) htb.get(key);
		if(isEmptyData(str)){
			str = "";
		}
		return 	str;
	}
	// ハッシュテーブルからキーで値を検索して返す
	// null になる場合は "0" を返す
	public static String strHashZERO(Hashtable htb,String key){
		//
		String	str	= (String) htb.get(key);
		if(isEmptyData(str)){
			str = "0";
		}
		return 	str;
	}

	// 文字列 source の中で、from と to で挟まれた部分を取り出す
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
	// source 文字列の全ての target を rep に置き換える
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
	//
	//
	// 原文からALST（選択肢一覧表表示場所）を探して、所与のデータと置き換える
	public String	doALST(int g, String fmt) {
		if(LOG.fa) LOG.println("■ Exam#doALST() : 原文からALST を探して、所与のデータと置き換える の先頭です");
		if(LOG.fa) LOG.println("  □ group  ="+g);
		if(LOG.fa) LOG.println("  □ format ="+fmt);
		//
		String			group	= String.valueOf(g);
		SearchToken 	stk	= new SearchToken(originalText);
		
		StringBuffer	bf	= new StringBuffer(10240);
		int eof=1;
		try{
			while((eof=stk.searchALST()) > 0){
				int 	id		= stk.tknID();						// 4
				int	grpENL	= stk.tknGroupNumENL();				// 1
				String	seqNum	= String.valueOf(stk.tknSeqNum());	// 2
				String	tkn 	= stk.tknSTR();						// ＊水によって
				String	name	= stk.tknSEQ();						// ERB(1,2)
				String	embeded	= "%@" + stk.tknSEQ() + "%";		// %@ERB(1,2)%
				int	grp		= stk.tknGroupNum();				// 1
				//
				if(LOG.fa) LOG.println("  ■■■ id       =" + id);
				switch(id){
				  case	EpmlToken.ALST:	// 選択肢一覧表表示場所

				  	if(LOG.fa) LOG.println("  □□ id       =" + id);
					if(LOG.fa) LOG.println("  □□ grpENL   =" + grpENL);
					if(LOG.fa) LOG.println("  □□ seqNum   =" + seqNum);
					if(LOG.fa) LOG.println("  □□ tkn      =" + tkn);
					if(LOG.fa) LOG.println("  □□ name     =" + name);
					if(LOG.fa) LOG.println("  □□ embeded  =" + embeded);
					if(LOG.fa) LOG.println("  □□ grp      =" + grp);
				
				
				  	//
					String	paramStr	= getString(name,'(',')');		// 例： name = "ALST(1/4/3-2-4-1)" または、"ALST(1/4)"、または、"ALST(1/5)"
					Csv		parameter	= new Csv(paramStr,"/");		//      1: シーケンス番号、4: 語群表示列数、3-2-4-1: 語順
					String	grpNum		= parameter.get(0);
					String	str			= "";
					if(grpNum.equals(group)){
						str		= "[+ 語群(" + parameter.get(1) + "/" + fmt + ") /]";
					}else{
						str		= "[+ 語群(" + parameter.get(1);
						if(parameter.size() == 3){
							str = str + "/" + parameter.get(2) + ") /]";
						}else{
							str = str +  ") /]";
						}
					}
					bf.append(str);
					break;
				  	
				  case	EpmlToken.TEXT:		// text
				  	default:
					bf.append(tkn);
					break;
				}
			}
			preHtmlText	= bf.toString();
			doneFlag	= true;	// 完了
			//
		}catch(EpmlTokenException e){
			String CR = System.getProperty("line.separator");
			int		pos	= e.getMessageValue();
			String 	msg = e.getNontitleMessage();
			String 	temp = originalText.substring(0,pos) + "<span style='color:red; font-size: 14px'><== 誤りを検出 ==></span>" + CR + CR +  msg;
			preHtmlText	= temp;
			doneFlag	= false;	// 未完了
			return preHtmlText;
		}
		return	preHtmlText;
	}
	
	// 内容を出力する
	public void	print(){
		//
		LOG.println(CR + "* * *  EXMA PRINT * * *" + CR);
		if(enl.allCount() != 0)  enl.print();
		if(ewl.allCount() != 0)  ewl.print();
		if(etf.allCount() != 0)  etf.print();
		if(erb.allCount() != 0)  erb.print();
		if(eta.allCount() != 0)  eta.print();
		
	}

	////////////////////////////////////////////////////////////////////
	///
	///   テスト
	///
	////////////////////////////////////////////////////////////////////
	
	public static void main(String[] args) {
		
		String	epml	=	FileGear.getFileData("e:\\temp\\test.txt");
		String	path	=	"D:\\PowerCampus\\conf\\conf_203\\pc.conf";
		Exam	exam	=	new Exam(epml, path, true);
		
		
	}
	
}
