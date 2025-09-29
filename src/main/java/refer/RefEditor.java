
package refer;

import java.io.*;
import java.util.*;

import database.*;
import framework.*;
import setup.*;
import	tktools.*;
import tktools.Base64;
import	epml.*;
import	epml.tools.FormatUtil;
import	jbbs.*;



/**
 * web教材作成エディタ
 * 
 * 
 	#
	# ##################
	#   RefEditor
	# ##################
	#
	<program $refer.RefEditor>
		<dispatch  html=refEditor.html  number=1320  class=refer.RefEditor/>
		<variable>
		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA DIVISION title lec_key ref_key ref_url ref_title />
		  <accept    CMD    UPLODE  deleteFname />
		  <keep      editor_rows  pmlFilePath alias/>
		  
		  <work      />
		  <form      ref_content listsw />
		</variable>
	</program> 
 *
 *
 *  1. accept 変数
 * 		deleteFname		アップロード済みファイルの削除を選択したとき、ファイル名が入る
 *	
 *  2. keep 変数
 * 		editor_rows		エディター領域の表示行数
 * 		epmlFilePath	プレビューのためにPMLファイルへの完全パスを記憶しておく
 * 
 *  3. form 変数
 * 		ref_content		記述したテキスト
 * 		listsw			アップロードファイル一覧を表示するかどうかのスイッチ ("ON" / "OFF")
 *     	tagmark			現在のタグマーカー文字．リストボックス
 * 
 * ファイル名はシステムハッシュのURLから得る事に注意
 * キー "_ref_url" では作成する教材のWWW上の相対URL　/user/(教師キー)/html/(科目キー)/(ファイル名)
 * を得ることができるので、このファイル名を使って保存・読み出しを行う
 */
public class RefEditor  extends SuperPlayer {

	/* ************ 
	 *  作業用変数
	 **************/
	/**
	 *  プログラムリターンコード 
	 */
	String		ret;
	/**
	 * 次の処理での表示モード 
	 */
	String		disp_mode;
	/**
	 *  処理分岐コード
	 */
	String 		cmd;
	/**
	 * データベースプール
	 */
	DbConnectionBroker	broker;
	/**
	 * ユーザー検索のためのクラス
	 */
	Database	db;

	String		szDB;
	String		teUid;
	String		lec_key;
	String		ref_key;

	String		alias;
	Hashtable	aliasHash;
	
	
	public	RefEditor(){
		super();
		if(LOG.fa) LOG.println("■ RefEditor #コンストラクタ");
	}	

	/**
	 * コンストラクタが引数をもてないので、コンストラクタで行う内容をこの initialize() に書く．
	 * out, htb, para は SuperPlayer クラスのインスタンス変数なので、
	 * 引数にとらずとも利用できるが、変数を明示する意味で列挙してある
	 * 
	 * @param out			出力ポインタ
	 * @param htb			システムハッシュ
	 * @param para			パラメータ
	 */
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		broker	=	getDbConnection();
		db		=	new Database(broker);			

		szDB	=	getParameter("_szDB");
		teUid	= 	getParameter("_teUid");
		lec_key	= 	getParameter("_lec_key");
		
		/*
		 * フォーマットエイリアス
		 * alias は、新規作成なら,本文がないので起動直後はないが
		 * 一度でも保存すると、保存時にフォーマットが付加されるので
		 * それ以降は値がある
		 * 
		 * 新規の場合は出力処理時にdisplay()で aliasとaliasHash が作成される
		 * 
		 */
		alias		=	"";
		aliasHash	=	null;
		String	tmp	=	getParameter("alias");
		if(!isEmpty(tmp)){
		    alias		=	(String)Base64.decodeToObject(tmp);
		}
		if(!isEmpty(alias)){
		    aliasHash	=	FormatUtil.stringToHash(alias);
		}

	}	

	// 処理分岐
	public	String	dispatch(){
		if(LOG.fa) LOG.outHash(htb,"■RefEditor #dispatch()");
		if(LOG.fa) LOG.println("■RefEditor #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		//
		// 個人設定へ移る
		if(cmd.equals("INFO")){
			ret			=	"$jbbs.BbsInfo";
			disp_mode	=	DISP_NEW;

		}else if( cmd.equals("REFRESH") ){
			/*
			 * 個人設定をフォーマットに反映する
			 */
		    refresh();
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;
		
		// EPML,HTML を書き込む（入力は消さない）
		}else if( cmd.equals("WRITE") ){
			writeFiles();
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;
			
		// ファイルリストの表示状態変更
		}else if( cmd.equals("FLIST") ){
			//String	fswitch	=	getParameter("_listsw");
			//setFlistMode(fswitch);
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;
			
		// プレビューを見る
		}else if( cmd.equals("PREVIEW") ){
			if(LOG.fa) LOG.println("■RefEditor #dispatch():PREVIEW");
			writeFiles();
			/*
			 * regularize していないテキストをwebのhiddenで受け渡しすると問題が発生するので
			 * pmlファイルへの絶対パスを渡して、プレビュー側で読み出しで表示する
			 */
			putParameter("pmlFilePath", getPmlFilePath());
			/*
			 * web のロード時にこの値がjavaScriptでチェックされる．
			 * ON であれば JumpPrev('/pc/launch/postRefPreview.html') を実行し、
			 * 必要な変数を受け渡した後にRevPrevがPOSTで起動される．
			 * プレビュー画面はポップアップで表示される．
			 */
			putParameter("preview","ON");
			
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;
			
		// エディタ行数を増やす
		}else if( cmd.equals("PLUS") ){
			if(LOG.fa) LOG.println("class RefEditor #launch()--> PLUS : エディタ行数を増やすへ分岐 の先頭です");
			puls();
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;

		// エディタ行数を減らす
		}else if( cmd.equals("MINUS") ){
			if(LOG.fa) LOG.println("class RefEditor #launch()--> MINUS : エディタ行数を減らすへ分岐 の先頭です");
			//
			minus();
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;
			
		}else if( cmd.equals("TAB") ){
			/*
			 * タブをスペースに変換する
			 */
		    tabToSpace();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;

		// 戻る（入力は保存しない）
		}else if( cmd.equals("RETURN") ){
			ret			=	DISPATCH_RETURN;
			disp_mode	=	DISP_EDIT;
			
			
		}else if(cmd.equals("UPLOAD")){
			getFile();
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;

		}else if(cmd.equals("DELETE")){
			String	deleteFname	=	getParameter("_deleteFname").replace('/', File.separatorChar);
			File	fp			=	new File(para.getUploadPath(teUid, lec_key) + deleteFname);
			fp.delete();
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;
			
		}else if(cmd.equals("EXECUTE")){
			/*
			 *　プログラムの実行
			 */
			disp_mode	=	DISP_NEW;
			ret			=	getParameter(PROGRAM);

		}else{
			/*
			 *　再表示
			 */
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
		}
		/*
		 * 次に表示するときあるいはリターンしてきた時の表示モードをセットしてからリターンする
		 */
		putParameter(DISP_KEY,disp_mode);
		return	ret;
	}
	/**
	 * 個人設定の書式を現在の段落書式に反映する
	 *
	 */
	void	refresh(){
	    /*
	     * デフォルトの書式（非epml形式）を得る
	     */
	    String	fmt	=	getFormat();
	    /*
	     * システムデータを作成する
	     */
	    aliasHash	=	FormatUtil.formatToHash(fmt);
	    alias		=	FormatUtil.hashToString(aliasHash);
	    putParameter("alias",Base64.encodeObject(alias) );
	    
	}
	/**
	 * テキストのタブを半角スペース４個分に変換して，システムハッシュにセットする
	 * @return	変換後のテキスト
	 */
	String	tabToSpace(){
	    String	text	=	StringGear.tabToSpace( getParameter("ref_content"), 4);
	    putParameter("ref_content", text);
	    return	text;
	    
	}	
	/**
	 * ファイル表示モードを変更する
	 * @param fswitch		"ON" or "OFF"
	 */
	void	setFlistMode(String sw){
		putParameter("_listsw",sw);
	}
	/**
	 * アップロードされたファイルをプロファイルディレクトリへ移す
	 * 複数ファイルの同時アップロードに対応している
	 */
	void	getFile(){

		/* ex.   /home/pc/<UID>/html/<lec_key>/files/                 */
		String	toDir		=	para.getUploadPath(teUid, lec_key);
		
		/* 
		 * マルチパートインプット処理で作成したディレクトリ名を得る
		 * ここにアップロードした全てのファイルがある       
		 * ex.   /home/pc/temp/[ランダムに作成したディレクトリ名] 
		 */
		String	fromDir		=	getParameter(UPLOAD_DIR_NAME);
		Gear.moveFiles(toDir, fromDir);
		
		/* 作業ディレクトリが残るので全て消去する */
		Gear.delDir(fromDir);
		
		/*
		 * アップロードディレクトリにzipファイルがないかどうか探す
		 * あれば解凍する
		 */
		String	zipFile		=	getZipFile(toDir);
		if(zipFile!=null){
	        /*
	         * zipファイルを解凍する
	         * ファイル名の中のディレクトリは / で区切られているので、
	         * FSに変換してファイル処理を行うこと
	         * 表示する関係から以下ではfnameをそのまま使っている
	         */
	        String	unzipDir	=	toDir;
	        Vector	fileList	=	FileGear.unzip(zipFile, unzipDir);
	        /* zip は削除しておく */
	        (new File(zipFile)).delete();		        
		    
		}
	}
	/**
	 * Zipファイルを探してそのフルパスを返す
	 * @param dir
	 * @return
	 */
	String	getZipFile(String dir){
	    
	    String	file		=	null;
	    String	[] flist	=	FileGear.fileList(dir);
	    for(int i=0; i<flist.length; i++){
	        if(FileGear.isZipfile(flist[i])){
	            file	=	flist[i];
	            break;
	        }    
	    }
        if(file==null)	return	null;
        return	dir + FS + file;
	}
	/**
	 * 出力するpmlファイルへの完全パスを得る
	 * @return
	 */
	String	getPmlFilePath(){
		String	epmlPath	=	para.getEpmlPath(teUid,lec_key);
		String	originalUrl	=	getParameter("_ref_url");
		String	epmlname	=	Gear.getFileNameBody(originalUrl) + ".epml";
		return	epmlPath + epmlname;
	}
	/**
	 * 出力するhtmlファイルへの完全パスを得る
	 * @return
	 */
	String	gerHtmlFilePath(){
		String	htmlPath	=	para.getHtmlPath(teUid,lec_key);
		String	originalUrl	=	getParameter("_ref_url");
		String	htmlName	=	Gear.getFileNameBody(originalUrl) + ".html";
		return	htmlPath + htmlName;
	}
	//
	// データを書き出す
	//
	void	writeFiles(){
		if(LOG.fa) LOG.outHash(htb,"class RefEditor #writeFiles() : データを書き出す の先頭です");
		//
		String	epmlPath	=	para.getEpmlPathName(teUid,lec_key);						// /home/pc/(teUid)/epml/(lec_key)
		String	htmlPath	=	para.getHtmlPathName(teUid,lec_key);						// /home/pc/(teUid)/html/(lec_key)
		
		if(LOG.fa){
		    LOG.println("teUid   ="+teUid);
		    LOG.println("lec_key ="+lec_key);
		    LOG.println("epmlPath="+epmlPath);
		    LOG.println("htmlPath="+htmlPath);
		    
		}
		
		
		Files	epf			= 	new Files(epmlPath);										// これはディレクトリがなければ作る働きがある
		Files	htf			= 	new Files(htmlPath);										// これはディレクトリがなければ作る働きがある
		String	ref_key		= 	getParameter("_ref_key");
		
		/* ファイル名をシステムハッシュにある _ref_url の値から取り出すように変更 2004.9.1 */
		String	originalUrl	=	getParameter("_ref_url");
		String	htmlName	=	Gear.getFileNameBody(originalUrl) + ".html";
		String	epmlname	=	Gear.getFileNameBody(originalUrl) + ".epml";
		
		if(LOG.fa){
		    LOG.println("originalUrl="+originalUrl);
		    LOG.println("htmlName   ="+htmlName);
		    LOG.println("epmlname   ="+epmlname);
		    
		}
		/*
		 * エイリアス定義を付加して出力する
		 */
		String	data		= 	alias + getParameter("_ref_content");
		//
		// epml と html の各ファイルを出力する
		epf.put(epmlname,data);
		/*
		 * exam
		 */
		String	exPath		= para.get_ex_wikiTemplatePath();// "/pc/images/" ⇒ files/
		Exam	exam		= new Exam(data ,para.getEmlConfPath(),exPath);
		//
		htf.put(htmlName,Cp932.toJIS(getHtml(exam)));	// 内部用－なぜこれでうまくいくのか分からない。（2004.3.9）
		//
		//　コピー先に、HTMLの中で余白を作るのに使用しているスペーサーファイルがなければコピーする
		String	srcPath		=	exam.getSpacerPath();											//    "/pc/epml/spacer.gif"
		String	dstPath		=	para.getUploadPath(teUid, lec_key) + exam.getSpacerFilename();		//    "spacer.gif"
		if(Gear.isExistFile(dstPath)){
			return; // すでにあればコピーしない
		}
		//
		try{
			Gear.copyBinryFile(srcPath,dstPath);	// バイナリ-でのファイルコピー
		}catch(IOException e){
			System.out.println("class RefEditor #writeFile() : spacer.gif ファイルをコピーできない");
			System.out.println("      srcPath=" + srcPath);
			System.out.println("      dstPath=" + dstPath);
			System.out.println(e);
		}
	}

	//
	// epml から Html を得る
	//
	String	getHtml(Exam exam){
		
		/*
		 * HTMLを生成し、書き換え用ハッシュに値をセットする<br>
		 * HTMLを生成に先だって、'<'のような危険な文字はexam.doHmac()の先頭で
		 * regularize()によって無害化される
		 * 
		 */
		Hashtable	htw		=	new	Hashtable();
		/* \ " ' は元に戻しておく */
		String		body	= 	Gear.toNormalString(exam.createHtml());
		putParameter(htw,"body",body);
		putParameter(htw,"ref_title",getParameter("ref_title"));
		
		/*
		 * 書き換え
		 */
		String	webTemplate	=	FileGear.getFileData(para.getWebTemplateFile());
		Replace	rp			= 	new Replace(webTemplate,htw);

		return	rp.subst(true);		
		
		
		/*
		Hashtable	htw		=	new	Hashtable();
		String		body	= 	exam.createHtml();		// 生成した html を返す（またはエラーメッセージ）
		putParameter(htw,"_body",body);
		//
		// 全体のスケルトン
		String	path		= para.getHtmlPath(); // /usr/local/jakarta-tomcat-3.2.4/webapps/pc/htmls/ 
		String	fname		= para.getFileName("template");
		Files	fs			= new Files(path);
		String	template	= fs.getFile(fname);
		//
		// 書き換え
		Replace	rp			= new Replace(template,htw);
		//
		//
		return	rp.subst(true);
		*/
	}
	//
	//
	void setInit(String rows){
		// エディタ行数を設定情報に反映しておく
		Setup info	= new Setup( getParameter(TUID), db );
		info.updateHtmlRows(rows);
		info.wrtInfo();
	}
	//
	//　行数を増やす
	//
	void	puls(){
		if(LOG.fa) LOG.println("■ class RefEditor #puls() : 行数を増やす の先頭です");
		//
		Setup info	= new Setup( getParameter(TUID), db );	// デフォルト
		int	max		= info.rowMax();	// 最大値
		int	min		= info.rowMin();	// 最小値
		int	delta	= info.deltaRows();	// 増分
		//
		String editor_rows	= getParameter("_editor_rows");
		int	rows	= Integer.parseInt(editor_rows);
		if(rows <= (max - delta) ){
			editor_rows	= String.valueOf(rows + delta);
			putParameter("_editor_rows",editor_rows);
			setInit(editor_rows);
		}
		if(LOG.fa){
			LOG.println("　　　max=" + max);
			LOG.println("　　　min=" + min);
			LOG.println("　　　row=" + rows);
		}
		return;
	}
	//
	// 行数を減らす
	//
	void	minus(){
		if(LOG.fa) LOG.println("class RefEditor #minus() : 行数を減らす の先頭です");
		//
		Setup info	= new Setup( getParameter(TUID), db );	// デフォルト
		int	max		= info.rowMax();	// 最大値
		int	min		= info.rowMin();	// 最小値
		int	delta	= info.deltaRows();	// 増分
		//
		String editor_rows	= getParameter("_editor_rows");
		int	rows	= Integer.parseInt(editor_rows);
		if(rows >= min + delta){
			editor_rows	= String.valueOf(rows - delta);
			putParameter("_editor_rows",editor_rows);
			setInit(editor_rows);
		}
		return;
	}
	//
	// テキストエリアの行数が空でないかチェックして空だったら初期値をセットする
	//
	public	void	setEditorRows(){
		if(LOG.fa) LOG.println("class RefEditor #setEditorRows() : テキストエリアの行数が空でないかチェックして空だったら初期値をセットす の先頭です");
		// HTML用テキストエリアの行数
		Setup	st		= new Setup( getParameter(TUID), db);
		String	ln		= st.s_htmlRows();
		String	rows	= getParameter("_editor_rows");
		if(isEmpty(rows,"_editor_rows")){	// 値が設定されていない時の文字列 "_editor_rows" を拾ってしまう
			putParameter("_editor_rows",ln);		//  初期値 ln 行
		}
	}


	/////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	//		 出　　力　　処　　理
	//
	/////////////////////////////////////////////////////////////////////////////////////////////////////
	//
	//
	/**
	 * HTML エディターを表示する
	 * 
	 * 		Refer からeditor を表示する際は、データを一度ＤＢにセーブし、参照キーも付与するので
	 * 		ここでの表示は常にデータベースから読み出したものを表示する
	 * 
	 * @param	editmode
	 */
	public  void	display(boolean editmode){
		if(LOG.fa)	LOG.println("■ RefEditor #display(boolean editmode)");
		
		if(!editmode){
			if(LOG.fa){
			    LOG.println("□display");
			}
		    putParameter(MESSAGE,"");
			putParameter("_listsw", "OFF");	// ファイルリストを表示しない
			
			/*
			 * pmlデータの先頭にはフォーマットエイリアスが付いているので分離して処理する
			 * pmlデータからフォーマットエイリアスを取り出して alias と aliasHash に格納する
			 * また、pmlデータ本文をハッシュにセットする
			 */
			String	pml		= setPml();
			putParameter("alias",Base64.encodeObject(alias) );
			putParameter("ref_content",pml);
			/*
			 * タグマーカーの初期値を設定
			 */
			setTagMark();
		}
		/*
		 * タグマーカーの値を設定（ウェブ変数がある）
		 */
		setTagMarkerList();
		
		
		// エディターの行数
		setEditorRows();	// 設定してあればそのまま、でなければsetup での指定行数をセットする
		/*
		 * ファイルリスト表示モードを設定
		 */
		String	fileDispMode	=	getParameter( "_listsw");
		if(fileDispMode.equals("ON")){
			putParameter("_listOnSelected", "selected");
			putParameter("_listOffSelected", "");
			
		}else{
			putParameter("_listOnSelected", "");
			putParameter("_listOffSelected", "selected");
		}

		/*
		 * 段落パラグラフのボタンに tips を埋め込む
		 */
	    if(LOG.fa){
	        LOG.println("□display()");
	        if(aliasHash==null)	 LOG.println("aliasHash is null.");
	    }
		setFormat();
		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}

	/**
	 * pml からフォーマットを取り出して変数に保存し、
	 * フォーマット以外の部分を返す
	 * 
	 * @return
	 */ 
	public	String	setPml(){

	    String			text	=	getPmldataFromFile();
	    
	    if(!isEmpty(text)){
	        if(LOG.fa){
	            LOG.println("=text is not null =");
	            LOG.println(text);
	        }
	        StringBuffer	body	=	new	StringBuffer(5000);
		    /*
		     * aliasHash は段落指定ボタンにチップスを付加するのに使う 
		     */
		    aliasHash				=	getPml(text, body);
	        if(aliasHash==null){
		        /*
		         * デフォルトのフォーマットを取得する
		         */
		        String	fmt	=	getFormat();
		        aliasHash	=	FormatUtil.formatToHash(fmt);
	        }
		    /*
		     * フォーマットはkeepするのでインスタンス変数にセットする
		     * alias は出力時に参照してpmlテキストの文頭に付加される
		     */
		    alias	=	FormatUtil.hashToString(aliasHash);
		    /*
		     * フォーマット以外の本文部分を返す
		     */
		    return	body.toString();
	    }else{
	        if(LOG.fa){
	            LOG.println("=text is null =");
	        }
	        /*
	         * ユーザー指定があればそれを、なければデフォルトのフォーマットを取得する
	         */
	        String	fmt	=	getFormat();
	        aliasHash	=	FormatUtil.formatToHash(fmt);
	        alias		=	FormatUtil.hashToString(aliasHash);
	        return	"";
	    }
	    
	}
	
	public	Hashtable	getPml(String pml, StringBuffer body){
	    
	    Hashtable		aliasHash	=	FormatUtil.getAlias(pml,body);
	    return			aliasHash;
	    
	}
	
	/**
	 * ファイルからPML形式のデータを読み込んで返す
	 * @return
	 */
	public	String	getPmldataFromFile(){
		if(LOG.fa) LOG.println("■ RefEditor #getPmldataFromFile() ");
		/*
		 * epml データはWWWでは /user/(teUid)/epml/(lec_key)/ にある．
		 * キー"_ref_url" を使ってシステムハッシュからデータのWWW上のURLを得、
		 * さらにこれから右端のファイル名（拡張子は取る）を得る
		 */ 
		String	originalUrl	=	getParameter("_ref_url");
		String	epmlName	=	Gear.getFileNameBody(originalUrl) + ".epml";
		String	pathName	= 	para.getEpmlPathName(teUid,lec_key);	// 出力するディレクトリ名
		/*
		 * 古いバージョンではファイル名の先頭はep ,新しいバージョンではURLから取るので ht
		 * 互換性を取るため引き継ぐ必要がある
		 */
		String	epmlNameOldversion	=	"ep" + epmlName.substring(2); 
		File fp_ep	=	new File(pathName + Gear.FS + epmlNameOldversion);
		if(fp_ep.exists()){
			try{
				Gear.copyBinryFile(pathName + Gear.FS + epmlNameOldversion, pathName + Gear.FS + epmlName);
			}catch(IOException e){
				e.printStackTrace();
			}
			fp_ep.delete();
		}
		/*
		 * ファイルを読み込む（なければ "" となる）
		 */
		Files	epmlFiles	= new Files(pathName);
		String	epml		= 	"";
		try{
			epml	= 	epmlFiles.getBytesFromFile(epmlName);
			
		}catch(UnsupportedEncodingException e1){
			//e1.printStackTrace();
			epml	= "";
		}catch(IOException e2){
			//e2.printStackTrace();
			epml	= "";
		}
		/*
		 * 新規作成ではフォーマットを付加する
		
		if(epml.length()==0){
			epml = getFormat();
		}
		 */
		return	epml;
		
	}	
	/**
	 * タグマーカーの初期値を設定
	 */
	void	setTagMark(){
		putParameter("tagmark","^");
	    
	}
	/**
	 * タグマーカーの値を設定（ウェブ変数がある）
	 *
	 */
	void	setTagMarkerList(){
		String tagmark	=	getParameter("tagmark");
		if(isEmpty(tagmark)){
		    putParameter("tagmark","^");
		}
		/*
		 * おなじくselect 指定も作成
		 * select 句の部分はウェブでは %_tag_[タグ文字]%　となっている
		 * 
		 */
		String	tag_sel	=	"_tag_" + tagmark;
		putParameter(tag_sel, "selected");
	}
	/**
	 * 段落パラグラフのボタンに tips を埋め込む
	 * @param content	内容を表すPML
	 * @return			同 HTML
	 */
	public	void	setFormat(){
	    if(LOG.fa){
	        LOG.println("□setFormat()");
	        if(aliasHash==null)	 LOG.println("aliasHash is null.");
	    }
        
		putParameter("_par1", strHash(aliasHash,"p1"));
		putParameter("_par2", strHash(aliasHash,"p2"));
		putParameter("_par3", strHash(aliasHash,"p3"));
		putParameter("_par4", strHash(aliasHash,"p4"));
		putParameter("_par5", strHash(aliasHash,"p5")); 
	    
	}
	/**
	 * フォーマット規定値を返す
	 * @return
	 */
	public	String	getFormat(){

	    String	format	=	BbsUtil.getFormat(teUid, szDB, broker);
	    if(format==null){
	        format	=	para.formatTemplate();
	    }
	    return	format;
	}

	/**
	 * ユーザーIDからユーザー情報をハッシュテーブルで得る
	 * 存在しない時は null を返す
	 * 
	 * @return
	 */
	Hashtable	getInfoRecord(String uid){
		if(LOG.fa) LOG.println("■ BbsPost #getInfoRecord()");

		BbsInfoDB	infodb	=	new BbsInfoDB(uid, szDB, broker);
		Hashtable	dt		=	new Hashtable();
		int		count	=	infodb.readBbsInfo(uid, dt);
		if(count>0)	return	dt;
		return			null;
	}
	
	
	
	public void	write(String key,Vector exHtml,Hashtable htb){

		if(key.equals("uploadFileList")){
			uploadFileList(exHtml);
			
		}else if(key.equals("isNoFiles")){
			isNoFiles(exHtml);
		}
	}		
	/**
	 * 
	 * @param exHtml
	 */
	void	uploadFileList(Vector exHtml){
		/*
		 * ファイルリストを表示しないモードならなにもしない
		 */
		String	fileDispMode	=	getParameter( "_listsw");
		if(LOG.fa) {
			LOG.println("■ RefEditor #write()");
			LOG.println("□ fileDispMode" + fileDispMode);
		} 
		if(fileDispMode.equals("OFF"))	return;
		
		/*
		 * HTML 生成のための各種テンプレート
		 */
		String		tempFile	=	para.htmlTemplate();
		TemplateBox	tpb			=	new TemplateBox(tempFile);
		String		template	=	tpb.get("attach");
		
		String		filePath	=	para.getUploadPath(teUid, lec_key);
		String	[]	files		=	FileGear.fileList(filePath);

		/*
		 * ファイルリストを表示する処理
		 */
		if(files==null){
			putParameter("_filecount", "0");
			return;
		}
		int	n	=	files.length;

		putParameter("_filecount", String.valueOf(n));	// blockWrite() で使う
		/*
		 * 全てのファイルを表示する
		 */
		Hashtable	tb	=	new	Hashtable(10);
		for(int i=0; i<n; i+=4){
			/*
			 * １行文の表示（最大４つのファイルを表示）
			 */
			for(int k=0; k<4; k++){
				
			    String	uploadFile;
			    int	pos	=	i+k;
			    if(pos < n){
			        uploadFile	=	(files[i+k]).replace(File.separatorChar, '/');

			    }else{
			        uploadFile	=	"";
			    }
				String 	fileUrl		=	para.getAliasUrl(teUid, lec_key) + uploadFile;

				String	rep	=	"&nbsp;";
				if(!Gear.isEmpty(uploadFile)){
					tb.put("_uploadFile", uploadFile);
					tb.put("_fileUrl"	, fileUrl);
					rep	=	Gear.replace(template, tb);
				}
				String	repkey	=	"_fileDiscriptor-" + String.valueOf(k+1);
				putParameter(repkey, rep);					
			}
			printVector(exHtml);
		}
	}
	/**
	 * 
	 * @param exHtml
	 */
	void	isNoFiles(Vector exHtml){
		String	fileDispMode	=	getParameter( "_listsw");
		if(fileDispMode.equals("ON")){
			/*
			 * ファイルリストを表示するモードならリスト表示していないときのみブランクを表示する 
			 */
			int	n	= Integer.parseInt( getParameter("_filecount") );// 既に表示した行があるか
			if(n > 0)	return;
			//
			printVector(exHtml);
		}else{
			/*
			 * ファイルリスト非表示モードなら常にブランクを表示する
			 */
			printVector(exHtml);
		}
		return;
	}		

}