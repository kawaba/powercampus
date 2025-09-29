/*
課題をWEBモードで見て評価する

*/
package eval;

import java.io.*;
import java.util.*;

import database.*;
import framework.*;
import setup.Setup;
import tktools.*;
import kadai.*;
/**
 * 
 *
 *
 	#
	# ##################
	#     EvalText
	# ##################
	#
	<program $eval.EvalText>
		<dispatch  html=EvalText.html  number=1530  class=eval.EvalText />
		<variable>
		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA HOMEURL DIVISION
		              title lec_key aplec_key kadai_key initRecNo  />
		              
		  <accept    CMD  UPLODE deleteFname  />
		  <keep      editor_rows recNo stNumber />
		  
		  <form      sortMode listsw search answer />
		</variable>
	</program> 
 *
 *
 * 変数の説明
 *
 * 1. receive 
 * 		initRecNo	--　最初に表示するレコードの番号
 * 						Webモードから遷移してきたときに値が設定されている
 * 
 * 2. accept
 * 		deleteFname		アップロード済みファイルの削除を選択したとき、ファイル名が入る
 * 
 * 3. keep
 * 		editor_rows	-- エディターの行数
 * 		recNo		-- 現在対象にしている提出ファイルのファイル番号
 * 
 * 4. form
 *		sortMode	-- ソート状態（"ON"なら提出時間順）デフォルトは番号順
 *		listsw		-- アップロードファイルリストを表示するかどうか
 * 		answer		-- 学生の解答
 * 		search		-- 検索する学生の学籍番号
 * 
 * 
 */
public class EvalText extends SuperPlayer implements  KadaiVar{
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
	String		aplec_key;
	String		te_lec_key;
	String		kadai_key;
	
	Answer		ans;
	/**
	 * 
	 *
	 */
	public	EvalText(){
		super();
		if(LOG.fa) LOG.println("■ EvalText #コンストラクタ");
	}
	/**
	 * 
	 * @param out
	 * @param htb
	 * @param para
	 */
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		/*
		 * 基本変数取得
		 */
		broker		=	getDbConnection();
		db			=	new Database(broker);	
		szDB		= 	getParameter("szDB");
		/*
		 * 解答ファイル名の配列を持ち、入出力を操作するためのAnswerオブジェクトを作成しておく
		 * 以下の４項はAnswer コンストラクタでhtbにあることが前提されている
		 */
		teUid		=	getParameter("teUid");
		lec_key		= 	getParameter("lec_key");
		aplec_key	=	getParameter("aplec_key");
		kadai_key	= 	getParameter("kadai_key");

		te_lec_key	=	KeyGen.get_te_lec_key2( teUid, lec_key);
		
		ans			=	new	Answer(htb, para, db);
		/*
		 * Web変数 "sortMode" を見て、ans のソートモードを設定する
		 * 設定がなければ番号順がデフォルトとなる
		 * 
		 * 動作設定は毎回initializeで行なうので以下では表示設定だけ行なう
		 * 
		 */
		sortAnsewers();

	}
	/**
	 * 
	 *
	 */
	public	String	dispatch(){
		if(LOG.fa) LOG.println("■EvalText #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		//
	
		if(cmd.equals("PREVIEW")){
			/*
			 * プレビュー
			 */
			preview();
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;
		
		}else if( cmd.equals("PLUS") ){
			/*
			 * エディタ行数を増やす
			 */
			justWrite(); // データ書き込み
			plus();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;

		}else if( cmd.equals("MINUS") ){
			/*
			 * エディタ行数を減らす
			 */
			justWrite(); // データ書き込み
			minus();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;

		}else if(cmd.equals("UPLOAD")){
			/*
			 * アップロード
			 */
			justWrite(); // データ書き込み
			fileUpload();
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;

		}else if( cmd.equals("TAB") ){
			/*
			 * タブをスペースに変換する
			 */
		    tabToSpace();
		    justWrite(); // データ書き込み
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;

		}else if(cmd.equals("FLIST")){
			/*
			 * ファイルリストの表示・非表示
			 */
			justWrite(); // データ書き込み
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;

		}else if(cmd.equals("DELETE")){
			/*
			 * アップロードファイルの削除
			 */
			justWrite(); // データ書き込み	
			fileDelete();
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;

		}else if(cmd.equals("INFO")){
			justWrite();
			ret			=	"$jbbs.BbsInfo";
			disp_mode	=	DISP_NEW;
			

		}else if(cmd.equals("SEARCH")){
			/*
			 * 学籍番号で検索
			 */
			sub_search();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
			
		}else if(cmd.equals("SORT")){
			/*
			 * answer のソートは initialize() ですんでいるのでここでは何もしない。
			 */
			justWrite();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;			
			
		}else if(cmd.equals("WRITE")){
			/*
			 * 採点を書き込む
			 */
			justWrite(); // 書き込んで	
			sub_fwd();	 // 次のレコード			
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;				
			
		}else if(cmd.equals("FIRST")){
			/*
			 * 先頭のレコードを表示
			 */
			justWrite();
			sub_top();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;				
			
		}else if(cmd.equals("LAST")){
			/*
			 * 末尾ののレコードを表示
			 */
			justWrite();
			sub_btm();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;				
			
		}else if(cmd.equals("NEXT")){
			/*
			 * 次のレコードを表示
			 */
			justWrite();
			sub_fwd();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;				
			
		}else if(cmd.equals("BACK")){
			/*
			 * 前のレコードを表示
			 */
			justWrite();
			sub_bwd();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;				
		

		}else if(cmd.equals("RETURN")){
			/*
			 * Webモード画面へ戻る
			 * 最初に表示すべきレコードの番号をセッション共通変数として保存する
			 * initRecNO を読ませるため NEWモードで戻る
			 */
			setSessionVar("initRecNo", getParameter("recNo"));
			disp_mode		=	DISP_NEW;
			ret				=	DISPATCH_RETURN;
		
		}else if(cmd.equals("EXECUTE")){
			/*
			 *　プログラムの実行
			 */
			justWrite();
			disp_mode	=	DISP_NEW;
			ret			=	getParameter(PROGRAM);

		}else{
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
	 * テキストのタブを半角スペース４個分に変換して，システムハッシュにセットする
	 * @return	変換後のテキスト
	 */
	String	tabToSpace(){
	    String	ansText	=	StringGear.tabToSpace( getParameter("answer"), 4);
	    putParameter("answer", ansText);
	    return	ansText;
	    
	}
	/**
	 * ファイルアップロード
	 * （学生用のフォルダにアップロードする）
	 * 
	 * アップロードされたファイルをプロファイルディレクトリへ移す
	 * 複数ファイルの同時アップロードに対応している
	 * アップロード先のファイル記述子を返す
	 */
	String	fileUpload(){
		/* 
		 * マルチパートインプット処理で作成したディレクトリ名を得る
		 * ここにアップロードした全てのファイルがある       
		 * ex.   /home/pc/temp/[ランダムに作成したディレクトリ名] 
		 */
		String		fromDir	=	strHash(htb, UPLOAD_DIR_NAME);
		/*
		 * 戻り値に使うのでファイル名を取得しておく
		 */
		String	[]	files	=	(new File(fromDir)).list();
		if(files==null){
			htb.put("_msg","★ 送信ファイル名の誤りです．[参照]ボタンを使ってファイル選択してください");
			Gear.delDir(fromDir);
			return "";
		}
		String		fname	=	files[0];
		/*
		 * 学生用のファイルアップロードディレクトリ名（セパレータ付き）
		 *  getKadaiPostDir() ⇒  "/home/pc/<TeUid>/file/aplec_key/kadai_key/stNumber/"
		 */
		String	toDir		=	para.kadaiAttachDir(teUid, aplec_key, kadai_key,  getParameter(StUID));
		if(LOG.fa) LOG.println("□EvalText　#getFile():　toDir  =" + toDir);
		if(LOG.fa) LOG.println("□EvalText　#getFile():　fromDir=" + fromDir);

		Gear.moveFiles(toDir, fromDir);
		
		/* 作業ディレクトリが残るので全て消去する */
		Gear.delDir(fromDir);
		
		return	fname;
		
	}
	/**
	 * アップロードしたファイルの削除
	 * （学生用のフォルダから削除する）
	 *  
	 * @param fname
	 */
	void	fileDelete(){

		String	fname	=	getParameter( "_deleteFname");
		String	fpath	=	para.kadaiAttachDir(teUid, aplec_key, kadai_key, getParameter(StUID)) + fname;
		if(LOG.fa) LOG.println("□EvalText　#deleteFile():　fpath  =" + fpath);
		File	fp		=	new File(fpath);
		fp.delete();
	}	
	/**
	 * プレビュー
	 *
	 */
	void	preview(){
		justWrite();
		previewSwitch(PREVIEW_ON);
		
	}	
	/**
	 * プレビューするかどうかを決めるスイッチを設定する<br>
	 * "ON" だとエディター画面を初期表示する際、プレビュー画面も表示される
	 * 
	 * @param sw	ON または OFF 
	 */	
	void	previewSwitch(String sw){
		putParameter("_preview",sw);
	}
	//
	// テキストエリアの行数が空でないかチェックして空だったら初期値をセットする
	//
	public	void	setEditorRows(){
		if(LOG.fa) LOG.println("■ EvalText #setEditorRows() : テキストエリアの行数が空でないかチェックして空だったら初期値をセットす の先頭です");
		// HTML用テキストエリアの行数
		Setup	st		= new Setup( getParameter(TUID), db);
		String	ln		= st.s_htmlRows();
		String	rows	= getParameter("_editor_rows");
		if(isEmpty(rows,"_editor_rows")){	// 値が設定されていない時の文字列 "_editor_rows" を拾ってしまう
			putParameter("_editor_rows",ln);		//  初期値 ln 行
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
		}
		
	}	
	/**
	 * データを検索して現在のレコード番号にセットする
	 *
	 */
	public void sub_search(){
		String	num	=	getParameter("search").trim();
		int		pos	=	ans.search(num);
		setRecordNum(pos);
		
	}
	/**
	 * 現在のソートモードを answer に設定する
	 * Webでの指定を反映する
	 * 最初は設定値がないがそのときはgetParameter("sortMode")で "" が返るのでOK
	 * 
	 * 学籍番号（＝ファイル名）順にレコードはソートされている。
	 * 提出時間順ソートのために、提出時間＋レコード番号(0～)でソートしたテーブルを作成している
	 * changeSortmodeはレコード＝ファイル名の取り出時の動作を決める
	 */
	public	void	sortAnsewers(){
		
		String	mode	=	getParameter("sortMode");
		
		if(mode.equals("ON")){
			ans.changeSortmode(DATE_MODE);

		}else{
			ans.changeSortmode(NUMBER_MODE);
		}
		
	}
	/**
	 * データを書き込む
	 * 
	 * レコード番号はweb変数 recNo にある。
	 *
	 */
	void	justWrite(){
		int pos 	 = getRecordNum(); 			// 現在のレコード番号
		String msg   = getParameter("_message");// 2005.3 削除したが形だけ残す
		String score = getParameter("_score");
		String eval  = getParameter("_eval");	// 採点結果を通知したかどうか
		String a     = getParameter("_answer");

		AnswerRecord ar = ans.writeAnsRecord(pos,msg,score,eval,a);
		update_KadaiInfo(ar);// KadaiInfo を更新
	}
	/**
	 * KadaiInfo の得点を更新する
	 * @param ans
	 */
	public	void update_KadaiInfo(AnswerRecord ansr){

		String 		te_aplec_key	= 	KeyGen.get_te_aplec_key2(ansr.getTeUid(),ansr.getClassKey());
		String 		score			= 	ansr.getScore();
		KadaiInfo 	kdi				=	new KadaiInfo(szDB,db);
		kdi.updateScore( getParameter("stNumber"), te_aplec_key, kadai_key, score);
	}
	/**
	 * 現在のレコード番号を得る
	 * ０ オリジン
	 * @return
	 */
	int	getRecordNum(){
		int		pos	=	Integer.parseInt(getParameter("recNo")) -1;
		return	pos;
	}
	/**
	 * レコード番号をシステムハッシュにセットする
	 * １オリジン
	 * @return
	 */
	String	setRecordNum(int pos){
		String	posStr	=	String.valueOf(pos + 1);
		putParameter("recNo", posStr);
		return	posStr;
	}
	/**
	 * 次のレコード番号を得る
	 * 
	 */
	int	incNum(){
		int	pos	=	getRecordNum();
		if(isOK(pos+1)){
			return	pos + 1;
		}else{
			return	pos;
		}
	}
	/**
	 * 一つ前のレコード番号を得る
	 * @return
	 */
	int	decNum(){
		int	pos	=	getRecordNum();
		if(isOK(pos-1)){
			return	pos - 1;
		}else{
			return	pos;
		}		
	}
	/**
	 * レコード番号として正しいか
	 * @param num
	 * @return
	 */
	boolean	isOK(int num){
		int	n	=	ans.getTotal();
		if((num>=0)&&(num<n)){
			return	true;
		}else{
			return	false;
		}
	}
	/**
	 * 先頭のレコードを指すようポインタを
	 * システムハッシュにセットする
	 *
	 */
	public void sub_top(){
		setRecordNum(0);
	}
	/**
	 * 最後のレコード 
	 *
	 */
	public void sub_btm(){
		setRecordNum(ans.getTotal()-1);
		
	}
	/**
	 * 次のレコード
	 *
	 */
	public void sub_fwd(){
		setRecordNum(incNum());
	}
	/**
	 * 前のレコード
	 */
	public void sub_bwd(){
		setRecordNum(decNum());
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
		if(LOG.fa)	LOG.println("■EvalText #display(boolean editmode)");
		
		if(!editmode){
			putParameter(MESSAGE,"");
			/*
			 * 最初はform 変数 "sortMode" の値がないのでここで提出順に設定しておく
			 */
			putParameter("_sortMode","OFF");
			/*
			 * 最初に表示するレコード番号を設定する
			 */			
			String	recNo	=	getParameter("initRecNo");
			if(isEmpty(recNo)){
				setRecordNum(0);
				
			}else{
				setRecordNum(Integer.parseInt(recNo)-1); // ゼロオリジン
			}
			/*
			 *  ファイルリスト表示モードも最初は値がないので設定する
			 *  規定値は「表示しない」
			 */
			putParameter( "listsw","OFF");
		}
		/*
		 * エディターの行数
		 */ 
		setEditorRows();	// 設定してあればそのまま、でなければsetup での指定行数をセットする		
		/*
		 * ファイルリスト表示モードを設定
		 */
		String	fileDispMode	=	getParameter( "listsw");
		if(fileDispMode.equals("ON")){
			putParameter("listOnSelected", "selected");
			putParameter("listOffSelected", "");
			
		}else{
			putParameter("listOnSelected", "");
			putParameter("listOffSelected", "selected");
			
		}
		/*
		 * 検索窓の表示をクリア
		 */
		putParameter("search"," ");
		/*
		 * 表示に必要な情報をシステムハッシュにセットする
		 * レコードのアクセスには getRecordNum() を使う。
		 * getRecordNum() は 現在のレコード番号 "recNo" を参照する 
		 * 
		 */
		setSort( getParameter("_sortMode") );
		setRecord();
		setDefRecordInfo();
		setApRecordInfo();
		setCountInfo();
		/*
		 * 解答をそのままシステムハッシュにセットする
		 */
		putParameter("_html", getParameter("_answer"));
		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}
	public	void	initSortmode(){

		
	}
	/**
	 * ソートモードの表示設定
	 * @param mode
	 */
	public void setSort(String mode){

		if(mode.equals("ON")){
			/*
			 * 提出順
			 */
			htb.put("_no_dateTime","");
			htb.put("_yes_dateTime","selected");

		}else{
			/*
			 * 番号順
			 */
			htb.put("_no_dateTime","selected");
			htb.put("_yes_dateTime","");
		}
		
	}	
	/**
	 * レコード情報をシステムハッシュにセットする
	 * 
	 */
	void	setRecord(){
		int				pos	=	getRecordNum();
		AnswerRecord	rec	=	ans.getdAnsRecordAt(pos);

		htb.put("_rec"			,String.valueOf(Gear.get000type(pos+1)));
		htb.put("_answer"		,rec.getAnswer());
	    htb.put("_message"		,rec.getMessage());
	    htb.put("_score"		,rec.getScore());
	    htb.put("_eval"			,rec.getEval());
	    htb.put("_stUid"		,rec.getStUid());
	    htb.put("_stNumber"		,rec.getStNumber());
	    htb.put("_stName"		,rec.getStName());
	    htb.put("_updateTime"	,rec.getUpdateTime());
	    htb.put("_disposal"		,rec.getDisposal());
	    
	    setIconInfo(rec);
		setUrlInfo(rec);
	}
	/**
	 * アイコンファイル名と結果通知状況ファイル名
	 *
	 */
	void	setIconInfo(AnswerRecord ar){
		/*
		 * 提出状態
		 */
		String kubun;
		if((ar.getDisposal()).equals(CHECKED)){
			kubun = KadaiInfo.SUBMITTED;// 提出済み( = 3)
        }else{
			kubun = KadaiInfo.WORKING;	// 作成中( = 2) 
        }
		htb.put("_kubun" ,kubun);
		/*
		 * 結果の通知
		 */
		String sent;
		if((ar.getEval()).equals(MAIL_DONE)){
			sent = "1"; // 結果送信済みマーク
		}else{
			sent = "0";
		}
		htb.put("_sent"  ,sent);		
	}
	/**
	 * 提出ファイルのアイコンとそのURL
	 *
	 */
	void	setUrlInfo(AnswerRecord ar){
		/*
		 * 課題ファイルのあるディレクトリで
		 */
		String  	filePath 	= para.getKadaiPostDir2(teUid,aplec_key,kadai_key);
		Files		fl			= new Files(filePath);						// 提出ファイルを処理するクラス
		/*
		 * htmlテンプレートからファイルとそのURLを記述するためのテンプレートを得る
		 */
		TemplateBox	tbx			=	new TemplateBox(para.htmlTemplate());
		String 		lineHtml 	=	tbx.get("report");
		/*
		 * 
		 */
		String 		aRef		= 	"";
		String 		product		=	para.getViewSwitch();
		if( (product.equals("SYSTEM")) || (product.equals("SYSTEM_VIEW")) ){
			aRef	=	para.getKadaiPostURL(htb,teUid,aplec_key,kadai_key); // IPアドレスでURLを返す
		}else{
			aRef	=	para.getKadaiPostURL(teUid,aplec_key,kadai_key); // URLを返す
		}
		/*
		 * 
		 */
		Hashtable	fh 		= new Hashtable();
		String [] 	files 	= fl.list(ar.getStNumber());
		if(files != null) {
			StringBuffer bf = new StringBuffer(2000);
			boolean sp		= false;
			for(int m=0; m<files.length; m++){
				if(sp) bf.append(", "); 
				//
				fh.put("_link",aRef + files[m]);
				fh.put("_fileName",files[m]);
				String s = substitute(lineHtml,fh);
				bf.append(s);
				sp = true;
			}
			htb.put("_fileList",bf.toString());
		}else{
			htb.put("_fileList","-");
		}
		htb.put("_max","");		
	}	
	/**
	 * 課題定義レコードにかかる情報
	 */
	void	setDefRecordInfo(){
		
		KadaiDefRecord	kadaiDef	=	ans.getKadaiDefRecord();
		String 			kadai_Icon	= 	kadaiDef.kadaiIcon();// 課題アイコンファイル名
		htb.put("_kadaiIcon",kadai_Icon);
		//
		htb.put("_titleString",kadaiDef.title());
	    htb.put("_kadai_title",kadaiDef.title());// 2003.4.10 追加
	}
	/**
	 * 課題実施レコードにかかる情報
	 */
	void	setApRecordInfo(){
		KadaiApRecord	kadaiAp	=	ans.getKadaiApRecord();
		htb.put("_syy",kadaiAp.s_yyyy());
	    htb.put("_smm",kadaiAp.s_month());
	    htb.put("_sdd",kadaiAp.s_day());
	    htb.put("_shh",kadaiAp.s_hour());
	    htb.put("_stt",kadaiAp.s_minute());
	    htb.put("_eyy",kadaiAp.e_yyyy());
	    htb.put("_emm",kadaiAp.e_month());
	    htb.put("_edd",kadaiAp.e_day());
	    htb.put("_ehh",kadaiAp.e_hour());
	    htb.put("_ett",kadaiAp.e_minute());
		//
		htb.put("_start",kadaiAp.getStartDate2());// 2003.4.10 追加
		htb.put("_end",  kadaiAp.getEndDate2());  // 2003.4.10 追加
	}
	/**
	 * 提出されている解答の数についての情報
	 */
	void	setCountInfo(){
		htb.put("_n1",String.valueOf(ans.getAll()));
	    htb.put("_n2",String.valueOf(ans.getTotal()));
	    htb.put("_n3",String.valueOf(ans.getSubmitted()));
	    htb.put("_n4",String.valueOf(ans.getMaking()));
	}
	////////////////////////////////////////////////////////////////////////////////////////
	//
	// 	ハッシュテーブル(htb)を使って部分的に切り取ったソースデータ（exHtml）
	// 	の内容を置き換えて出力処理する．表などの反復出力に利用するが個々の処理
	// 	内容は、key で特定される．
	//
	////////////////////////////////////////////////////////////////////////////////////////

	public void	write(String key,Vector exHtml,Hashtable htb){
		if(LOG.fa)	LOG.println("■ EvalText #write()");
		
		if(key.equals("uploadFileList")){
			uploadFileList(exHtml);
		
		}else if(key.equals("isNoFiles")){
			isNoFiles(exHtml);
			
		}
	}
	void uploadFileList(Vector exHtml){
		/*
		 * 表示しないモードならなにもしない
		 */
		if(getParameter("listsw").equals("OFF")){
			return;
		}
		/*
		 * HTML 生成のための各種テンプレート
		 */
		String		tempFile	=	para.htmlTemplate();
		TemplateBox	tpb			=	new TemplateBox(tempFile);
		String		template	=	tpb.get("attach");
		
		String		filePath	=	para.kadaiAttachDir(teUid, aplec_key, kadai_key, getParameter(StUID));
		Fileset		fs			=	new Fileset(filePath);
		int		n			=	fs.n();
		StringEnumeration	e	=	fs.files();

		if(LOG.fa) LOG.println("□EvalText　#write:　tempFile=" + tempFile);
		if(LOG.fa) LOG.println("□EvalText　#write:　filePath=" + filePath);

		/*
		 * ファイルリストを表示する処理
		 */
		if(n==0){
			htb.put("_filecount", "0");
			return;
		}
		htb.put("_filecount", String.valueOf(n));	// blockWrite() で使う
		/*
		 * 全てのファイルを表示する
		 */
		Hashtable	tb	=	new	Hashtable(10);
		for(int i=0; i<n; i+=4){
			/*
			 * １行文の表示（最大４つのファイルを表示）
			 */
			for(int k=0; k<4; k++){
				String	uploadFile	=	e.nextItem();
				String 	fileUrl		=	getFileUrl(uploadFile);

				String	rep	=	"&nbsp;";
				if(!Gear.isEmpty(uploadFile)){
					tb.put("_uploadFile", uploadFile);
					tb.put("_fileUrl"	, fileUrl);
					rep	=	Gear.replace(template, tb);
				}
				String	repkey	=	"_fileDiscriptor-" + String.valueOf(k+1);
				htb.put(repkey, rep);					
			}
			printVector(exHtml);
		}
		
	}
 	/**
	 * ファイルへのURLを返す
	 * @param cs
	 * @param pos
	 * @return		完全URL
	 */
	String	getFileUrl(String fname){
		if(LOG.fa)	LOG.println("■ EvalText #getFileUrl()");

		if(Gear.isEmpty(fname))	return	"";
		return	para.kadaiAttachUrl(teUid, aplec_key, kadai_key, getParameter(StUID)) + fname;
	}
	/**
	 * 部分的に切り取ったソースデータ（exHtml）を出力するか否か、ハッシュテーブル
	 *  (htb)を使って判断し、出力する場合には、htb を使ってexHtml の内容を書き換えて
	 *  出力する．個々の処理内容は、key で特定される．
	 */
	public void	isNoFiles(Vector exHtml){
		if(getParameter("listsw").equals("OFF")){
			/*
			 * ファイルリストを表示しないモードなら常に空行を表示する
			 */
			printVector(exHtml);
			return;
		}else{
			/*
			 * すでに表示したファイルリストがないとき空行を表示する
			 */
			int	n	= Integer.parseInt( Gear.strHash(htb,"_filecount") );// 既に表示した行があるか
			if(n == 0){
				printVector(exHtml);
			}
		}
	}
}
	
