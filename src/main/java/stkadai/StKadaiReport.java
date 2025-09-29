package stkadai;

import student.*;
import  tktools.*;
import tktools.Base64;

import java.io.*;
import java.util.*;

import database.*;
import epml.tools.FormatUtil;
import framework.*;
import	jbbs.*;
import kadai.*;

/**
 *
 *
 	#
	# ##################
	#     StKadaiReport
	# ##################
	#
	<program $stkadai.StKadaiReport>
		<dispatch  html=stKadaiReport.html  number=2410  class=stkadai.StKadaiReport />
		<variable>
		  <receive   NUMBER STAMP GROUP StUID StCLASSINFO StNAME StMAIL StKEITAI  TUID aplec_key lec_key title kadai_key />
		  <accept    CMD    UPLODE deleteFname />
		  <keep      editor_rows  alias />
		  
		  <form      answer radio_disposal />
		</variable>
	</program>  
 *
 *
 * 変数の説明
 *
 * 1. receive 
 * 2. accept
 * 3. keep
 * 		editor_rows --------- エディタの行数を保持する
 * 		deleteFname --------- ファイル削除の時、削除するファイル名
 * 
 * 4. form
 * 		answer -------------- 解答文
 * 		radio_disposal ------ 提出区分
 *
 *
 *
 * 【ノート】
 *　　AnswerRecord とKadaiInfo のレコード記述の同期は，
 *　　AnswerRecord を書き込むと同時にKadaiInfoもアップデートしている．
 *　　sub_wrt() → setKadaiInfo() を参照
 *
 *
 */public class StKadaiReport extends SuperPlayer {
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
	//
	String		szDB;
	String 		teUid;
	String 		lec_key;
	String 		aplec_key;
	String		te_lec_key;
	String		te_aplec_key;
	String		kadai_key;
	String		stNumber;
	//
	KadaiApRecord	kar;		// 課題定義レコード
	KadaiDefRecord 	rec;		// 課題実施レコード
	//
	String		shubetsu;		// 課題の種別（課題定義レコードＤＢから引く）
	String		subject;		// 課題名
	String		radio_disposal;	// 提出するかどうかの指示（学生）＜"0", "checked"＞
	String		disposal;		// 提出状況：KadaiInfo.NOTYET,WORKING,SUBMITTED,GRADED
	String		points;			// 得点（採点が済んでいればここに得点がある）
	//
	String		submitTime;		// 提出時間
	//
	boolean	tempFlag;		// 一時ファイルに記録があるかどうか
	//
	final	String	ON		=	"ON";
	final	String	OFF		=	"OFF";	
	

	String		alias;
	Hashtable	aliasHash;
	boolean	refreshFlag;
	String		alias_refresh;
	Hashtable	aliasHash_refresh;


	final static 	String CHECKED	= "checked";	//完成したので提出する mkAnswer.html の _radio_disposal の値　

	
	public	StKadaiReport(){
		super();
		if(LOG.fa) LOG.println("■ StKadaiReport #コンストラクタ");
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

		//
		szDB			= 	getParameter(GROUP);
		stNumber		= 	getParameter(StUID);
		teUid			= 	getParameter(TUID);
		lec_key			=	getParameter("lec_key");
		aplec_key		=	getParameter("aplec_key");
		kadai_key		= 	getParameter("_kadai_key");

		te_lec_key		= 	KeyGen.get_te_lec_key2(teUid, lec_key);
		te_aplec_key	= 	KeyGen.get_te_aplec_key2(teUid, aplec_key);
	
		kar				= new KadaiApRecord(te_aplec_key,kadai_key,db); // 課題実施レコード
		rec				= new KadaiDefRecord(te_lec_key,kadai_key,db);	// 課題定義レコード
		/* 
		 * 課題種別、課題名 
		 */
		shubetsu		= rec.shubetsu();
		subject			= rec.title();
		/* 
		 * 提出区分、点数、提出期日
		 */
		KadaiInfo kdf		= new KadaiInfo(szDB,db,stNumber,te_aplec_key,kadai_key);// 学生の課題情報
		int cnt = kdf.read_KadaiInfo();
		if(cnt==0){
			disposal		= KadaiInfo.NOTYET;
			points			= "-";
			submitTime		= "";

		}else{
			disposal	= kdf.saiten_flag();
			points		= kdf.points();
			if(points.equals("")){	points	= "-"; }
			submitTime	= kdf.latest_date_str();
		}
		/*
		 * レポート画面のチェックボックス 
		 */
		radio_disposal	= strHash(htb,"_radio_disposal");
		if(isEmpty(radio_disposal)){
			if((disposal.equals(KadaiInfo.NOTYET))||(disposal.equals(KadaiInfo.WORKING))){
				radio_disposal = "";		// 保存しておくだけでまだ提出しない
			}else{
				radio_disposal = CHECKED;	// 完成したので提出する
			}
		}
		/* 
		 * プレビューは OFF
		 */
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
	 * プレビューするかどうかを決めるスイッチを設定する<br>
	 * "ON" だとエディター画面を表示する際、プレビュー画面も表示される
	 * @param sw	ON または OFF 
	 */
	void	previewSwitch(String sw){
		htb.put("_preview",sw);
	}

	public	String	dispatch(){
		if(LOG.fa) LOG.println("■Sample #dispatch()");
		
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		if( cmd.equals("SAVE")){
			/*
			 * 提出可能かチェックして問題を保存する
			 */
			String	msg	=	writeCheck();
			putParameter(MESSAGE, msg);
			if(isEmpty(msg)){
				/*
				 *  一時ファイルの内容は最新でなくなるので _tempFlag をクリアする
				 */
				htb.put("_tempFlag","OFF");
				sub_wrt();				
				putParameter(MESSAGE, "★ データを保存しました ★");
			}
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;

		}else if( cmd.equals("REFRESH") ){
			if(LOG.fa)	LOG.println("□to refresh()");
		    /*
			 * 個人設定をフォーマットに反映する
			 */
		    refresh();
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;
			
		}else if(cmd.equals("FLIST")){
			/*
			 * ファイルリストの表示・非表示
			 */
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;

		}else if( cmd.equals("PLUS") ){
			/*
			 * エディタ行数を増やす
			 */
			puls();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;

		}else if( cmd.equals("MINUS") ){
			/*
			 * エディタ行数を減らす
			 */
			minus();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
			
		}else if( cmd.equals("TAB") ){
			/*
			 * タブをスペースに変換する
			 */
		    tabToSpace();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;

		}else if(cmd.equals("UPLOAD")){
			/*
			 * ファイルアップロード
			 */
		    getFile();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;

		}else if(cmd.equals("DELETE")){
			/*
			 * ファイル削除
			 */
			String	fname	=	Gear.strHash(htb, "_deleteFname");
			deleteFile(fname);
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;

		}else if(cmd.equals("PREVIEW")){
			/*
			 * 課題をファイルに書き込み
			 * 別画面でプレビューするためのフラグを立てる
			 */
			sub_wrt();		
			previewSwitch(ON);
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;

		}else if(cmd.equals("INFO")){
			/*
			 * 情報設定画面を開く
			 */
			disp_mode	=	DISP_NEW;
			ret			=	"$jbbs.BbsInfo";
			
		}else if(cmd.equals("RETURN")){
			/*
			 * 戻る
			 */
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;	
			
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
		if(LOG.fa)	LOG.println("□refresh()");

	    /*
	     * デフォルトの書式（非epml形式）を得る
	     */
	    String	fmt	=	getFormat();
	    if(LOG.fa){
	        LOG.println("new formt");
	        LOG.println(fmt);
	    }
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
	    String	ansText	=	StringGear.tabToSpace( getParameter("answer"), 4);
	    putParameter("answer", ansText);
	    return	ansText;
	    
	}
	
	/**
	 * ファイルアップロード
	 * アップロードされたファイルをプロファイルディレクトリへ移す
	 * 複数ファイルの同時アップロードに対応している
	 * アップロード先のファイル記述子を返す
	 */
	String	getFile(){
		if(LOG.fa) LOG.println("■ StKadaiReport　#getFile()");
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
		 * ファイルのアップロードディレクトリ名（セパレータ付き）
		 *  getKadaiPostDir() ⇒  "/home/pc/<TeUid>/file/aplec_key/kadai_key/stNumber/"
		 */
		String	toDir		=	para.kadaiAttachDir(teUid, aplec_key, kadai_key, stNumber);
		if(LOG.fa) LOG.println("□StKadaiReport　#getFile():　toDir  =" + toDir);
		if(LOG.fa) LOG.println("□StKadaiReport　#getFile():　fromDir=" + fromDir);

		Gear.moveFiles(toDir, fromDir);
		
		/* 作業ディレクトリが残るので全て消去する */
		Gear.delDir(fromDir);
		
		return	fname;
		
	}
	void	deleteFile(String fname){
		if(LOG.fa) LOG.println("■ StKadaiReport　#deleteFile()");
		
		String	fpath	=	para.kadaiAttachDir(teUid, aplec_key, kadai_key, stNumber) + fname;
		if(LOG.fa) LOG.println("□StKadaiReport　#deleteFile():　fpath  =" + fpath);
		File	fp		=	new File(fpath);
		fp.delete();
	}
	/**
	 * 解答をファイルに書く
	 * エラーメッセージはでない
	 */
	void	justWrite(){
		String	msg	=	writeCheck();
		if(isEmpty(msg)){
			/*
			 *  一時ファイルの内容は最新でなくなるので _tempFlag をクリアする
			 */
			htb.put("_tempFlag","OFF");
			sub_wrt();				
		}
	}
	/**
	 *  課題が書き込み可能かどうか
	 * @return	可能な場合""を返す．可能でない場合はメッセージを返す
	 */
	String	writeCheck(){
	    
		String	msg	=	"";
		if(kar.isEmptyRecord()){
			/*
			 * 期間が設定されていない 
			 */
			msg	=	"★★ この課題はまだ提出できません．しばらくお待ちください． ★★";
			
		}
		if(!kar.isStarted()){
			/*
			 * まだ期間前である
			 */
			msg	=	"★★ まだ提出期間になっていなので、この課題は提出できません ★★";
		}
		if(kar.isOver()){
			/*
			 * 期限が過ぎた
			 */
			msg	=	"★★ もう提出期限が過ぎましたので、この課題は提出できません ★★";
		}
		if(disposal.equals(KadaiInfo.GRADED)){
			/*
			 * 採点済み
			 */
			msg	=	"★★ すでに採点されていますのでこの課題は再提出できません ★★";
		}
		return	msg;		
	}
	//
	//// エディタ行数を設定情報に反映しておく
	void setInit(String rows){
		//
		Student		st		= new Student(szDB,stNumber,db);
		String		initVal	= para.getStudentInitValue();
		st.update_init(Student.EDITOR_ROWS,rows,initVal);					// 書き込む	
	}
	//
	// テキストエリアの行数が空でないかチェックして空だったら初期値をセットする
	public	void	setEditorRows(){
		if(LOG.fa) LOG.println("class StKadaiReport #setEditorRows() : テキストエリアの行数が空でないかチェックして空だったら初期値をセットす の先頭です");
		//
		// 学生の初期値を取り出してセットする
		// Param からはデフォルトの値を参照している
		//    para.getStudentInitValue() は *$18 のように初期値の全文字列（50文字以内）
		//    initialValues() は 初期値から指定したパラメータを取り出す．ない場合は指定された値を初期値として設定し、そこから値を取る
		Student	st	= new Student(szDB,stNumber,db);
		String	ln	= st.initialValues(Student.EDITOR_ROWS,para.getStudentInitValue());	// 
		htb.put("_editor_rows",ln);		//  初期値 ln 行
	}
	//
	//　行数を増やす
	//
	void	puls(){
		if(LOG.fa) LOG.println("■ class StKadaiReport #puls() : 行数を増やす の先頭です");
		//
		String	initVal	= para.getStudentInitValue();
		Student	st		= new Student(szDB,stNumber,db);
		int		max		= Integer.parseInt( st.initialValues(Student.EDITOR_ROWS_MAX,initVal) );	// 最大値
		int		min		= Integer.parseInt( st.initialValues(Student.EDITOR_ROWS_MIN,initVal) );	// 最小値
		int		delta	= Integer.parseInt( st.initialValues(Student.EDITOR_ROWS_UNIT,initVal) );	// 増分
		if(LOG.fa){
			if(LOG.fa) LOG.println("           max   = " + max);
			if(LOG.fa) LOG.println("           min   = " + min);
			if(LOG.fa) LOG.println("           delta = " + delta);
		}
		//
		String editor_rows	= strHash(htb,"_editor_rows");
		int	rows	= Integer.parseInt(editor_rows);
		if(rows <= (max - delta) ){
			editor_rows	= String.valueOf(rows + delta);
			htb.put("_editor_rows",editor_rows);
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
		if(LOG.fa) LOG.println("class StKadaiReport #minus() : 行数を減らす の先頭です");
		//
		String	initVal	= para.getStudentInitValue();
		Student	st		= new Student(szDB,stNumber,db);
		int		max		= Integer.parseInt( st.initialValues(Student.EDITOR_ROWS_MAX,initVal) );	// 最大値
		int		min		= Integer.parseInt( st.initialValues(Student.EDITOR_ROWS_MIN,initVal) );	// 最小値
		int		delta	= Integer.parseInt( st.initialValues(Student.EDITOR_ROWS_UNIT,initVal) );	// 増分
		//
		String editor_rows	= strHash(htb,"_editor_rows");
		int	rows	= Integer.parseInt(editor_rows);
		if(rows >= min + delta){
			editor_rows	= String.valueOf(rows - delta);
			htb.put("_editor_rows",editor_rows);
			setInit(editor_rows);
		}
		return;
	}	//
	//  他のオブジェクトでの表示処理用
	public	String	disposal()		{	return	disposal;	}
	public	String	points()		{	return	points;		}
	public	String	submitTime()	{	return	submitTime;	}
	public	String	subject()		{	return	subject;	}
	public	String	shubetsu()		{	return	shubetsu;	}

    /** 
     * 課題をファイルに書き込む<br>
     */
	//
	//  解答ファイルの書き込みと課題提出状況ファイルの書き込みを同時に行っている．
	//　本質的に、ひとつのデータベースファイルにまとめなくてはならない．
	//
    public void sub_wrt(){
        if(LOG.fa)  LOG.outHash(htb,"class StKadaiReport #sub_wrt() : 書き込みのみ（途中保存) の先頭です");
		//
        putDisposal();				// 課題の提出状況
		putUpdateTime();			// 更新時間
		setKadaiInfo();				// 提出状況書き込み KadaiInfo に書き込む
		
		//
		// 解答レコード作成
		AnswerRecord	ans		= 	new AnswerRecord(htb); // 入力結果からレコードを構成する
		/*
		 * 段落書式を付加する
		 */
		String			report	=	alias + ans.getAnswer();
		ans.setAns(report);
		
		String			path	= para.stFilePath(teUid,aplec_key,kadai_key,stNumber);	// 提出ファイルへのフルパス
        boolean      	chk  	= writeAnswer(path,ans);
        if(!chk) { LOG.errStop(out,"class StKadaiReport #sub_wrt() : ★ 解答をファイルに書けません.");}
    }
	/** 課題の提出時間をハッシュにセットする */
	void putUpdateTime(){
		submitTime	=	getDate_short();	// "yy/mm/dd hh:mm:ss"
		htb.put("_updateTime",submitTime);		
	}
	
	/** 課題の提出状況をハッシュにセットする */
    void	putDisposal(){
        /*
         * ここでデータを書こうとしているので、「解答ファイルは存在する」として処理する
         * 
         * radio_disposal = "checked"  提出する
         *                = ""　　　　 作成中
         */
		radio_disposal	= strHash(htb,"_radio_disposal");
		
		if(radio_disposal.equals( CHECKED )){
			disposal	= KadaiInfo.SUBMITTED;	// 提出する
		}else{
			disposal	= KadaiInfo.WORKING;	// 保存のみ
		}
		htb.put("_disposal",radio_disposal);	// CHECKED or ""  ラジオボタンの値

		if(LOG.fa){LOG.println("■■ radio_disposal :" + radio_disposal + ":"); }
		if(LOG.fa){LOG.println("disposal =" + disposal); }

		//
	}
	/**
	 * Answer オブジェクトをファイルに書き出す
	 * @param path		解答ファイルパス名
	 * @param ans		AnswerRecord(解答レコード)
	 * @param para		システムパラメータオブジェクト
	 * @param htb		システムハッシュ
	 * @return			成功すると true 失敗すると false を返す
	 */
    public static boolean  writeAnswer(String path,AnswerRecord ans){

        ObjectOutputStream  objOut  = null;
        try{
            objOut = new ObjectOutputStream(new FileOutputStream(path));
            try{
                objOut.writeObject(ans); // Serializable
                objOut.flush();
                objOut.close();
                return true;
            }catch(IOException e1){
            }
        }catch(IOException e2){
        }
        return false;
    }

	// 
	// データベースに状況を登録する
	// 
	void	setKadaiInfo(){
		if(LOG.fa) LOG.println("class StKadaiReport #setKadaiInfo() : データベースに状況を登録する の先頭です");
		//
		// 有無を確かめる
		if( !KadaiInfo.isExist(db, szDB, te_aplec_key, kadai_key, stNumber)){           
			if(LOG.fa){ LOG.println("★insert"); }
			if(LOG.fa){ LOG.println("　　disposal = " + disposal); }
			
			KadaiInfo	kdf	=	new KadaiInfo(szDB, db,stNumber,te_aplec_key,kadai_key);
			kdf.add_data(shubetsu,disposal,submitTime,subject,points);
			kdf.insert_KadaiInfo();
		}else{
			if(LOG.fa){ LOG.println("★update"); }
			if(LOG.fa){ LOG.println("　　disposal = " + disposal); }
			
			KadaiInfo	kdf	=	new KadaiInfo(szDB, db,stNumber,te_aplec_key,kadai_key);
			kdf.add_data(shubetsu,disposal,submitTime,subject,points);
			kdf.update_KadaiInfo();
		}
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
	 * レポート・ファイル提出・試験の課題を表示する.<br>
	 * インスタンスフィールドtempFlagを参照して、一時ファイルを読み込むかAnswerRecordを読込むかきめる
	 * 
	 * @param	editmode	表示モード（true なら編集、falseなら新規）
	 */
	public  void	display(boolean editmode){
		if(LOG.fa)	LOG.println("■ Sample #display(boolean editmode)");
		
		if(!editmode){
			String	msg	=	"";
			putParameter(MESSAGE,msg);
			/*
			 *  ファイルリスト表示モードも最初は値がないので設定する
			 *  規定値は「表示しない」
			 */
			putParameter( "listsw","OFF");
			/*
			 * タグマーカーの初期値を設定
			 */
			setTagMark();
			/*
			 * 解答をセット
			 * pmlデータの先頭にはフォーマットエイリアスと標準書式が付いているので分離して処理する
			 * pmlデータからフォーマットエイリアスを取り出してaliasHash に格納する
			 * aliadHash から aliasを作成し、その際、alias末尾に標準書式を含める
			 * また、pmlデータ本文をハッシュにセットする
			 * pml本文がない時は "" を返す
			 */
			String	pml		= setPml();
			putParameter("alias",Base64.encodeObject(alias) );
			putParameter("answer",pml);
			
		}
		/*
		 * タグマーカーの値を設定（ウェブ変数がある）
		 */
		setTagMarkerList();		
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
		 * その他の項目をハッシュにセットする
		 */ 
        putKadaiTitle();
        putKadaiData();
		putDisposalRadioButton();
		putPointAndTime();
        //
		setFormat();		// 段落パラグラフのボタンに tips を埋め込む
		//
        setEditorRows();	// エディタの行数を設定

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
		if(LOG.fa)    LOG.println("□setPml()");
		
		/*
		 * 解答レコードの本文（alias と 標準書式("##") を含む）
		 */
		String			text	=	getReport();
	    StringBuffer	body	=	new	StringBuffer(5000);
		if(!isEmpty(text)){
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
		     * 末尾に、標準書式("##       ") も付加される
		     * 
		     * alias は出力時に参照してpmlテキストの文頭に付加される
		     */
		    alias	=	FormatUtil.hashToString(aliasHash);
		    /*
		     * フォーマット以外の本文部分を返す
		     */
		    return	body.toString();
	    }else{
	        /*
	         * ユーザー指定があればそれを、なければデフォルトのフォーマットを取得する
	         * 最初のフォーマット指定を付加する
	         */
	        String	fmt	=	getFormat();
	        aliasHash	=	FormatUtil.formatToHash(fmt);
		    /*
		     * フォーマットはkeepするのでインスタンス変数にセットする
		     * 末尾に、標準書式("##       ") も付加される
		     */ 
	        alias		=	FormatUtil.hashToString(aliasHash);
	        return	"";
	    }
	    
	}	
	
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
        if(LOG.fa) LOG.println("□ getFormat()");

	    String	format	=	BbsUtil.getFormat(stNumber, szDB, broker);
	    if(format==null){
	        if(LOG.fa) LOG.println("format is null.");
	        format	=	para.formatTemplate();

	    }else{
	        if(LOG.fa) LOG.println("format is not null.");
	    }
	    if(LOG.fa) LOG.println("===============");
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
     * システムハッシュに表示データ（_answer）をセットする<br>
     * 保存されているファイルがなければ初期表示．<br>
     * そうでなければ、保存されているファイルから表示する<br>
     */
    String	getReport(){
		
		File	fp	=	kadaiFP();
		if(fp.exists()){
			return		(getAnsRecod(fp)).getAnswer();
		}
		return	"";	
    }

    
    
	/**
	 * 課題ファイルへのファイルポインタを得る
	 * @return
	 */
	File kadaiFP(){
		String dir 	= para.kadaiAnsDir(teUid,aplec_key,kadai_key);	// 例：/home/kawaba01/answer/112/000013
		File fp     = new File(dir);
		if(!fp.isDirectory()){ fp.mkdirs(); }   //ディレクトリがなければ作る
		//
		String 	path	= para.stFilePath(dir,stNumber);
		File 	ufp 	= new File(path);
		return	ufp;
	}
	/**
	 * ファイルパスからAnswerオブジェクトを読みこんで返す．
	 * @param 	ufp	解答ファイルポインタ
	 * @return		answerレコード．ファイルがない場合は null を返す．
	 */
	public static AnswerRecord getAnsRecod(File ufp){
		ObjectInputStream  objIn  = null;
		AnswerRecord ans = null;
		try{
			objIn = new ObjectInputStream(new FileInputStream(ufp));
			try{
				ans = (AnswerRecord)objIn.readObject();
				objIn.close();
			}catch(ClassNotFoundException e1){
				System.out.println("ClassNotFoundException:" + e1);
				ans = null;
			}catch(IOException e2){
				System.out.println("IOException:" + e2);
				ans = null;
			}
		}catch(IOException e3){
			System.out.println("can't Open :IOException:" + e3);
			ans = null;
		}
		return ans;
	}

	void putMessage(String msg){
		//
		if(isEmpty(msg)){
			htb.put("_message","");
		}else{
			htb.put("_message",msg);
		}
	}
	void putKadaiTitle(){
		htb.put("_kadai_title",rec.title());		
	}
	void putKadaiData(){
		
		htb.put("_syy",kar.s_yyyy());
		htb.put("_smm",kar.s_month());
		htb.put("_sdd",kar.s_day());
		htb.put("_shh",kar.s_hour());
		htb.put("_stt",kar.s_minute());
		//
		htb.put("_eyy",kar.e_yyyy());
		htb.put("_emm",kar.e_month());
		htb.put("_edd",kar.e_day());
		htb.put("_ehh",kar.e_hour());
		htb.put("_ett",kar.e_minute());
	}
	void putDisposalRadioButton(){

		if(radio_disposal.equals(CHECKED)){
			htb.put("_ck0","");			//  保存だけ
			htb.put("_ck1","checked");	//	提出する
		}else{
			htb.put("_ck0","checked");	//  保存だけ
			htb.put("_ck1","");			//	提出する
		}
	}

	void putPointAndTime(){
		
		htb.put("_pt",points);
		htb.put("_time",submitTime);
	}
	////////////////////////////////////////////////////////////////////////////////////////
	//
	// 	ハッシュテーブル(htb)を使って部分的に切り取ったソースデータ（exHtml）
	// 	の内容を置き換えて出力処理する．表などの反復出力に利用するが個々の処理
	// 	内容は、key で特定される．
	//
	////////////////////////////////////////////////////////////////////////////////////////

	public void	write(String key,Vector exHtml,Hashtable htb){
		if(LOG.fa)	LOG.println("■ StKadaiReport #write()");
		
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
		
		String		filePath	=	para.kadaiAttachDir(teUid, aplec_key, kadai_key, stNumber);
		Fileset		fs			=	new Fileset(filePath);
		int		n			=	fs.n();
		StringEnumeration	e	=	fs.files();

		if(LOG.fa) LOG.println("□StKadaiReport　#write:　tempFile=" + tempFile);
		if(LOG.fa) LOG.println("□StKadaiReport　#write:　filePath=" + filePath);

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
		if(LOG.fa)	LOG.println("■ StKadaiReport #getFileUrl()");

		if(Gear.isEmpty(fname))	return	"";
		return	para.kadaiAttachUrl(teUid, aplec_key, kadai_key, stNumber) + fname;
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
