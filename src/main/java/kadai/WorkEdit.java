package kadai;

import java.io.*;
import java.util.*;

import database.*;
import	epml.*;
import epml.tools.FormatUtil;
import framework.*;
import setup.Setup;
import	tktools.*;
import tktools.Base64;
import	jbbs.*;
/**
 * 課題の作成・編集
 *
	#
	# ##################
	#   WorkEdit
	# ##################
	#
	<program $kadai.WorkEdit>
		<dispatch  html=workEdit.html  number=1420  class=kadai.WorkEdit />
		<variable>
		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA DIVISION title lec_key kadai_key kadai_seqNum />
		  <accept    CMD    UPLODE  deleteFname />
		  <keep      editor_rows alias />
		  
		  <work      />
		  <form      kadai_title kadai_shubetsu  kadai_content  listsw  />
		</variable>
	</program> 
 *
 *
 *
 *  1. accept 変数
 * 		deleteFname		アップロード済みファイルの削除を選択したとき、ファイル名が入る
 *	
 *  2. keep 変数
 * 		editor_rows		エディター領域の表示行数
 * 
 *  3. form 変数
 * 		kadai_title		課題名
 * 		kadai_shubetsu	課題の種別
 * 		kadai_content	記述したテキスト
 * 		listsw			アップロードファイル一覧を表示するかどうかのスイッチ ("ON" / "OFF")

 *
*/

public class WorkEdit extends SuperPlayer {

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
	Database			db;
	
	final	String	ON		=	"ON";
	final	String	OFF		=	"OFF";	
	//
	String			szDB;
	String			teUid;
	String			lec_key;
	String			kadaikey;
	String			te_lec_key;
	String			kadaiSeq;

	String		alias;
	Hashtable	aliasHash;
	
	
	public	WorkEdit(){
		super();
		if(LOG.fa) LOG.println("■ Sample #コンストラクタ");
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

		broker		=	getDbConnection();
		db			=	new Database(broker);
		
		szDB		=	getParameter(GROUP);
		teUid		=	getParameter(TUID);
		lec_key		=	getParameter("_lec_key");
		te_lec_key	= 	KeyGen.get_te_lec_key2(teUid, lec_key);
		/*
		 * 課題キーが "" なら新規モード。それ以外では値がある。 
		 */
		kadaikey	=	getParameter("_kadai_key");	
		kadaiSeq	=	getParameter("_kadai_seqNum");	
		/*
		 * タイプ練習課題の評価基準
		 * HTMLに埋め込む
		 */
		putParameter("_typingLevel1", para.tLevel1());
		putParameter("_typingLevel2", para.tLevel2());
		//
		previewSwitch(OFF);
		
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
	/**
	 * 受け入れコードにより処理を分岐させるメソッド．
	 * 受け入れコードは getParameter(CMD) で受け取る
	 * 
	 * 終了コードとして次に起動したいプログラムエイリアス（論理プログラム名）を指定する
	 * 論理プログラム名は dispatch.xml に指定したものを使う
	 * 
	 * 自分自身の画面を再表示したい場合は終了コードに DISPATCH_DEFAULT を指定する
	 * 呼び出しプログラムへ復帰するには終了コードに   DISPATCH_RETURN  を指定する
	 *
	 * disp_mode とは
	 * 　　次にこの画面を表示するときの表示モード(DISP_NEW=新規表示、DISP_EDIT=編集表示)．
	 * 　　あるいは次に起動するプログラムで使われる表示モード
	 *　（注）
	 * 　　他のプログラムが終了して、DISPATCH_RETURN でこのプログラムが呼び出される時は 
	 * 　　コントローラーにより display(boolean disp_mode) へ直接復帰する．
	 * 　　この場合、disp_mode はコントローラーによってDISP_EDIT に設定されている 
	 */	
	public	String	dispatch(){
        if(LOG.fa) LOG.println("■ WorkEdit #dispatch()");
		
    	cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		if(cmd.equals("PREVIEW")){
			preview();
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;
			
		}else if(cmd.equals("REFRESH")){
		    refresh();
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;
		
		}else if(cmd.equals("WRITE")){
			write();
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;

		}else if(cmd.equals("PLUS")){
			plus();
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;

		}else if(cmd.equals("MINUS")){
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

		}else if(cmd.equals("UPLOAD")){
			fileUpload();
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;

		}else if(cmd.equals("FLIST")){
			//
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;

		}else if(cmd.equals("DELETE")){
			fileDelete();
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;

		}else if(cmd.equals("INFO")){
			ret			=	"$jbbs.BbsInfo";
			disp_mode	=	DISP_NEW;
			
		}else if(cmd.equals("RETURN")){
			ret			=	DISPATCH_RETURN;
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
	    String	text	=	StringGear.tabToSpace( getParameter("_kadai_content"), 4);
	    putParameter("_kadai_content", text);
	    return	text;
	    
	}
	/**
	 * 
	 */
	void	preview(){
		/*
		 * プレビュー
		 * パースするとオリジナルテキストが書き換えられるので、ここでパースして
		 * オリジナルテキストをハッシュにも保存しておく．
		 * Exam のコンストラクタにより _kadai_content の < ,> は特殊文字に基準化されている
		 * ので、ハッシュで受け渡ししても問題は起こらない
		 * 
		 * さらにプレビューに先立ってデータベースに書き込む．プレビューでは表示のために
		 * データベースを読み込んでいる．
		 */
		parseContent();
		if(!doUpdateKadaiRecord()){
			putParameter(MESSAGE,"★ プレビューの前に課題を保存してください");
			
		}else{
			/*
			 * web のロード時にこの値がjavaScriptでチェックされる．
			 * ON であれば JumpPrev('/pc/launch/postKadaiPreview.html') を実行し、
			 * 必要な変数を受け渡した後にKadaiPrevがPOSTで起動される．
			 * プレビュー画面はポップアップで表示される．
			 */
			previewSwitch(ON);
		}
		
	}
	void	write(){
		/*
		 * プレビューのリロードに備えるためここでもパースしておく
		 * リロードは直接ファイルを読むので書き込む前に選択肢の語順を確定しておく必要がある
		 */
		parseContent();
		/* 
		 * シーケンス、種別、タイトルが入力済みを確認し、課題キーの有無で insert か update を決定して書き込む
		 * 書き込み成功なら true を返す
		 */
		if(!doUpdateKadaiRecord()){
			putParameter(MESSAGE,"★ 課題のタイトルを記入しないと書き込みはできません");
		}else{
			putParameter(MESSAGE,"★ 課題の内容を書き込みました");
		}
		
	}
	void	plus(){
		// 編集領域拡大
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
			updateRows(editor_rows);
		}

	}
	void	minus(){
		// 編集領域縮小
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
			updateRows(editor_rows);
		}
		
	}
	/*
	 * データベースのエディタ行数を更新する
	 */
	void	updateRows(String rows){
	    Setup	st		= new Setup( getParameter(TUID), db);
	    st.updateKadaiRows(rows);
	    st.wrtInfo();
	    
	}
	//
	// テキストエリアの行数が空でないかチェックして空だったら初期値をセットする
	//
	public	void	setEditorRows(){
		if(LOG.fa) LOG.println("class RefEditor #setEditorRows() : テキストエリアの行数が空でないかチェックして空だったら初期値をセットす の先頭です");
		// HTML用テキストエリアの行数
		Setup	st		= new Setup( getParameter(TUID), db);
		String	ln		= st.s_kadaiRows();
		String	rows	= getParameter("_editor_rows");
		if(isEmpty(rows,"_editor_rows")){	// 値が設定されていない時の文字列 "_editor_rows" を拾ってしまう
			putParameter("_editor_rows",ln);		//  初期値 ln 行
		}
	}	
	void	fileUpload(){
	    if(LOG.fa) LOG.println("class RefEditor #fileUpload()");
	    /*
		 * ファイルアップロード
		 */
		if(Gear.isEmpty(kadaikey)){
			/*
			 * 課題がデータベースに登録されいない場合は
			 * 登録してから処理する
			 * 
		     * 試験の場合は、パースすると選択肢欄のデータが書き換わるので
		     * プレビューで選択肢の順序が変わってしまわないよう、書き込み時、
		     * プレビュー時に実行してテキストをあらかじめ更新しておく
		    */
		    parseContent();
		    /*
		     *  課題をデータベースに書き込む
		     *  同時に、kadai_key を得てシステムハッシュにセットして戻る
		     */
			boolean	ret =	doUpdateKadaiRecord();
			if(!ret){
			    // 書き込めなかった
			    putParameter(MESSAGE,"★ 課題タイトルを記入してください");
			    return;
			}
		}
		kadaikey	=	getParameter("kadai_key");
		if(LOG.fa) LOG.println("kadai_key =" + getParameter("kadai_key"));
		upload();
	}
	void	fileDelete(){
		/*
		 * ファイル削除
		 */
		String	deleteFname	=	getParameter("_deleteFname").replace('/', File.separatorChar);
		deleteFile(deleteFname);
		
	}
	
	/**
	 * プレビューするかどうかを決めるスイッチを設定する<br>
	 * "ON" だとエディター画面を初期表示する際、プレビュー画面も表示される
	 * @param sw	ON または OFF 
	 */
	void	previewSwitch(String sw){
		putParameter("_preview",sw);
	}

	/**
	 * ファイルアップロード
	 * アップロードされたファイルをプロファイルディレクトリへ移す
	 * 複数ファイルの同時アップロードに対応している
	 * アップロード先のファイル記述子を返す
	 */
	void	upload(){
		if(LOG.fa) LOG.println("■ StKadaiReport　#getFile()");
		
		String	fromDir	=	getParameter( UPLOAD_DIR_NAME);
		String	toDir	=	para.getAttachDIR(teUid, lec_key, getParameter("kadai_key"));
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
	void	deleteFile(String fname){
		if(LOG.fa) LOG.println("■ StKadaiReport　#deleteFile()");
		
		String	fpath	=	para.getAttachDIR(teUid, lec_key, kadaikey) + fname;
		if(LOG.fa) LOG.println("□StKadaiReport　#deleteFile():　fpath  =" + fpath);
		File	fp		=	new File(fpath);
		fp.delete();
	}	
	
	
	/**
	 * 課題テキストを取りだし、一度パースして書き戻す
	 * パースすると選択肢欄のデータが書き換わる
	 * プレビューで選択肢の順序が変わってしまわないよう、書き込み時、プレビュー時に実行して
	 * テキストをあらかじめ更新しておく
	 */
	void parseContent(){
		String	shubetsu	=	getParameter("_kadai_shubetsu");
		if(KadaiDefRecord.isExam(shubetsu)){
			String	kadai_text	=	getParameter("_kadai_content");
			if(!Gear.isEmpty(kadai_text)){
				Exam	exam	= 	new Exam(kadai_text ,para.getEmlConfPath(),true); // doEpml()でパースする
				putParameter("_kadai_content",exam.getText());
			}
		}
	}
	/**
	 *	課題を書き込む 
	 */
	boolean 	doUpdateKadaiRecord(){

		if( isUpdateKadaiOK()){
			// kadai_key があれば更新、なければ新規モードとする
			String kadai_key = getParameter("_kadai_key");		// 課題キーを取り出す
			if(isEmpty(kadai_key)){								// なければ新規
				addKadaiRecord();
			}else{
				updateKadaiRecord();							// あれば更新
			}
			return true;
		}else{
			return false;	// 更新はなかった
		}
	}
	//
	boolean isUpdateKadaiOK(){

		String	kadai_seqNum	= getParameter("_kadai_seqNum");	// シーケンス番号（１オリジン）
		String	kadai_shubetsu	= getParameter("_kadai_shubetsu");	// 課題種別
		String	kadai_title		= getParameter("_kadai_title");		// 課題タイトル
		//
		boolean flag	= true;
		if(isEmpty(kadai_seqNum))	flag	= false;
		if(isEmpty(kadai_shubetsu)) flag	= false;
		if(isEmpty(kadai_title))	flag	= false;
		//
		return flag;
	}
	/**
	 * 課題レコードを新規作成する
	 */
	void addKadaiRecord(){
		
		KeyGen 	kg		= new KeyGen(teUid,db);
		/*
		 * このクラスのメンバー変数 kadaikey に新規の課題キーを設定しておく
		 */
		kadaikey		= kg.nextKadai();
		putParameter("_kadai_key",kadaikey);
		/*
		 * 新規モードの場合、取得した課題キーをシステムハッシュに書き込んでおく
		 */

		String	kadai_seqNum	= getParameter("_kadai_seqNum");				// シーケンス番号（１オリジン）
		String	kadai_shubetsu	= getParameter("_kadai_shubetsu");			// 課題種別
		/*
		 * 含まれる問題のある記号は特種文字に変えてから受け取る
		 */
		String	kadai_title		= Gear.doSafty(getParameter("_kadai_title"));	// 課題タイトル
		/*
		 * 段落エイリアスを付加する
		 */
		String	kadai_content	= alias + Gear.doSafty(getParameter("_kadai_content"));	// 課題内容
		/*
		 * レコードに値を入れて書き込む
		 */
		KadaiDefRecord	rdRec	= new KadaiDefRecord();		// 空の課題レコードを生成
		rdRec.set_te_lec_key(te_lec_key);
		rdRec.set_kadai_key(kadaikey);
		rdRec.set_seq_number(kadai_seqNum);
		rdRec.set_shubetsu(kadai_shubetsu);
		rdRec.set_title(kadai_title);
		rdRec.set_content(kadai_content);
		//
		rdRec.insert(db);	// 書き込み
		return;
	}
	/**
	 * 課題レコードを更新する
	 */
	void updateKadaiRecord(){
		//
		String  te_lec_key		= KeyGen.get_te_lec_key2(getParameter(TUID), getParameter("lec_key"));
		String	kadai_key		= getParameter("_kadai_key");
		//
		String	kadai_seqNum	= getParameter("_kadai_seqNum");				// シーケンス番号（１オリジン）
		String	kadai_shubetsu	= getParameter("_kadai_shubetsu");			// 課題種別
		/*
		 * 含まれる問題のある記号は特種文字に変えてから受け取る
		 */
		
		String	kadai_title		= Gear.doSafty(getParameter("_kadai_title"));	// 課題タイトル
		/*
		 * 段落エイリアスを付加する
		 */
		String	kadai_content	= alias + Gear.doSafty(getParameter("_kadai_content"));	// 課題内容

		/*
		 * レコードに値を入れて書き込む
		 */
		KadaiDefRecord	rdRec	= new KadaiDefRecord();		// 空の科目レコードを生成
		rdRec.set_te_lec_key(te_lec_key);
		rdRec.set_kadai_key(kadai_key);
		rdRec.set_seq_number(kadai_seqNum);
		rdRec.set_shubetsu(kadai_shubetsu);
		rdRec.set_title(kadai_title);
		rdRec.set_content(kadai_content);
		//
		rdRec.update(db);	// 書き込み
		return;
	}
	
	/*////////////////////////////////////////////////////////////////////////////
	 * 
	 *    表 示 処 理
	 * 
	 * 　　    以下はコントローラーが呼び出す表示メソッドである．
	 * 　　    一般には、このクラス内のメソッドから直接呼び出さない．
	 * 
	 *////////////////////////////////////////////////////////////////////////////
	/**
	 * 画面を表示する
	 * 
	 * @param	editmode	表示モード（true なら編集、falseなら新規）
	 */
	public  void	display(boolean editmode){
		if(LOG.fa)	LOG.println("■ Sample #display(boolean editmode)");
		
		if(!editmode){
			putParameter(MESSAGE,"");
			putParameter("_listsw", "OFF");	// ファイルリストを表示しない
			/*
			 * pmlデータの先頭にはフォーマットエイリアスが付いているので分離して処理する
			 * pmlデータからフォーマットエイリアスを取り出して alias と aliasHash に格納する
			 * また、pmlデータ本文をハッシュにセットする
			 */
			String	pml		= setPml();
			putParameter("alias",Base64.encodeObject(alias) );
			putParameter("_kadai_content",pml);
			
			/*
			 * タグマーカーの初期値を設定
			 */
			setTagMark();
			
		}
		/*
		 * タグマーカーの値を設定（ウェブ変数がある）
		 */
		setTagMarkerList();		
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
		 * 種別リストボックスの表示を設定する
		 */
		setShubetsu( getParameter( "_kadai_shubetsu"));
		/*
		 * エディターの行数
		 */ 
		setEditorRows();	// 設定してあればそのまま、でなければsetup での指定行数をセットする
		/*
		 * 段落パラグラフのボタンに tips を埋め込む
		 */
		setFormat();
		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}
	/**
	 * 課題の種別とタイトルをシステムハッシュにセットし、さらに課題の内容を返す
	 * 段落フォーマットも設定する
	 * 
	 * @return
	 */
	String	setPml(){
		String	content	=	"";
	    if(Gear.isEmpty(kadaikey)){
			/*
			 * 新規作成（kadaikeyがない）ならフォーマット付きの新規データを作成
			 */
		    putParameter("_kadai_shubetsu"	, "1");
			putParameter("_kadai_title"		, "");
			//putParameter("_kadai_content"	, "");
			
	        /*
	         * ユーザー指定があればそれを、なければデフォルトのフォーマットを取得する
	         */
	        String	fmt	=	getFormat();
	        aliasHash	=	FormatUtil.formatToHash(fmt);
	        alias		=	FormatUtil.hashToString(aliasHash);
	        return	"";			
			
		}else{
		    /*
		     * 更新なら作成した課題定義レコードを読み込む
		     * 先頭にはフォーマットデータが付属している
		     */
			KadaiDefRecord kdr	=	new KadaiDefRecord(te_lec_key, kadaikey, db);
			putParameter("_kadai_shubetsu"	, kdr.shubetsu());
			putParameter("_kadai_title"		, kdr.title());

			content					=	kdr.content();
			StringBuffer	body	=	new	StringBuffer(5000);
			/*
		     * aliasHash は段落指定ボタンにチップスを付加するのに使う 
		     */
		    aliasHash				=	getPml(content, body);
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
		}
		
	}
	/**
	 * 書式指定と本文を分ける
	 * 
	 * @param pml
	 * @param body
	 * @return
	 */
	public	Hashtable	getPml(String pml, StringBuffer body){
	    
	    Hashtable		aliasHash	=	FormatUtil.getAlias(pml,body);
	    return			aliasHash;
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
	 * フォーマットを返す
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
	 * 種別リストボックスの表示を設定する
	 * @param shubetsu
	 */
	void	setShubetsu(String shubetsu){
		if(LOG.fa){
		    LOG.println("■ WorkEdit #setShubetsu :shubetsu=" + shubetsu + ":");
		}
		putParameter("_lbx1","");
		putParameter("_lbx2","");
		putParameter("_lbx3","");
		putParameter("_lbx4","");
		putParameter("_lbx5","");
		
		if(shubetsu.equals(KadaiDefRecord.REPO)){
		    putParameter("_lbx1","selected");
			
		}else if(shubetsu.equals(KadaiDefRecord.FILE)){
			putParameter("_lbx2","selected");
			
		}else if(shubetsu.equals(KadaiDefRecord.EXAM)){
			putParameter("_lbx3","selected");
		
		}else if(shubetsu.equals(KadaiDefRecord.TYPE)){
			putParameter("_lbx4","selected");

		}else if(shubetsu.equals(KadaiDefRecord.WRITE)){
			putParameter("_lbx5","selected");
		
		}else{
			putParameter("_lbx1","selected");
			
		}
		
		
	}
	boolean isEmptyRowParam(String str){
		if(str==null) 					return  true;
		if(str.length()==0)				return  true;
		if(str.equals("_editor_rows"))	return  true;	// HTMLの　<input type="hidden" name="_editor_rows"    value="%_editor_rows%"> から 
		return false;									// 値が設定されていない時の文字列 "_editor_rows" を拾ってしまう
	}
	/**
	 * ブロック出力
	 */
	public void	write(String key,Vector exHtml){
		if(LOG.fa)	LOG.println("■ WorkEdit #write()");
		
		if(key.equals("uploadFileList")){
			uploadFileList(exHtml);
		
		}else if(key.equals("noFiles")){
			noFiles(exHtml);
		}
	}
	/**
	 * アップロードされたファイルのリストを表示する
	 * @param exHtml
	 */
	void	uploadFileList(Vector exHtml){
		/*
		 * ファイルリストを表示しないモードならなにもしない
		 */
		String	fileDispMode	=	getParameter( "_listsw");
		if(LOG.fa) {
			LOG.println("■ WorkEditor #write()");
			LOG.println("□ fileDispMode" + fileDispMode);
		} 
		if(fileDispMode.equals("OFF"))	return;
		
		if(Gear.isEmpty(kadaikey)){
			putParameter("_filecount", "0");
			return;
		}
		/*
		 * HTML 生成のための各種テンプレート
		 */
		String		tempFile	=	para.htmlTemplate();
		TemplateBox	tpb			=	new TemplateBox(tempFile);
		String		template	=	tpb.get("attach");
		
		String		filePath	=	para.getAttachDIR(teUid, lec_key, kadaikey);
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
					rep		=	Gear.replace(template, tb);
				}
				String	repkey	=	"_fileDiscriptor-" + String.valueOf(k+1);
				putParameter(repkey, rep);					
			}
			printVector(exHtml);
		}
	}
	/**
	 * アップロードされたファイルがない時、空白を表示する
	 * @param exHtml
	 */
	void	noFiles(Vector exHtml){
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
	/**
	 * ファイルへのURLを返す
	 * @param cs
	 * @param pos
	 * @return		完全URL
	 */
	String	getFileUrl(String fname){
		if(LOG.fa)	LOG.println("■ WorkEdit #getFileUrl()");

		if(Gear.isEmpty(fname))	return	"";
		return	para.getAttachURL(teUid, lec_key, kadaikey) + fname;
	}

}

