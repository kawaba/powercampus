package teacher;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Vector;
import database.Database;
import database.DbConnectionBroker;
import database.KeyGen;
import framework.LOG;
import framework.Param;
import framework.SuperPlayer;
import kamoku.KamokuAP;
import kamoku.KamokuApRecord;
import kamoku.KamokuDefRecord;
import kamoku.KamokuSectionDEF;
//
import setup.Setup;
import student.Student;
import tktools.FileGear;
//
/**
 * 教師用の時間割画面を表示する
	#
	# ##################
	#   Timetable
	# ##################
	#
	<program $teacher.Timetable>
		<dispatch  html=top.html  number=100  class=teacher.Timetable />
		<variable>
		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA HOMEURL/>
		  <accept    CMD UPLODE shubetsu worder wdate url title lec_key aplec_key />
		  <keep      stNumber   stPasswd  stMail/>

		  <form      />
		</variable>
	</program> 
 *
 * accept 変数のうち
 * 	shubetsu（昼、夜、EL） worder（行） wdate（列）　 は 常にある 
 * 	title lec_key aplec_key は VIEW_CLASS で講義画面へ移る時のみある
 *
 */
public class Timetable extends SuperPlayer {
	// receive
	String	number;		// シート番号
	String	stamp;		// タイムスタンプ
	String	group;		// 学生データベース名
	String	teUid;		// 教師ID
	String	teMail;		// 教師メールアドレス
	String	teName; 	// 教師漢字氏名
	String	furigana;	// ふりがな
	
	// accept
	String	cmd;		// 機能選択語
	String	upload;		// アップロードの有無を示すフラグ
	String	shubetsu;	// 時間割種別
	String	worder;		// 時限
	String	wdate;		// 曜日
	String	url;		// 遠隔講義URL
	String	title;		// 科目名
	String	lec_key;	// 科目キー
	String	aplec_key;	// 講義キー
	
	
	/* ************ 
	 *  作業用変数
	 **************/
	/**
	 *  プログラムリターンコード 
	 */
	String			ret;
	/**
	 * 次の処理での表示モード 
	 */
	String			disp_mode;
	/**
	 * 情報
	 */
	DbConnectionBroker	broker;
	Database 			db;
	//
	public	Timetable(){
		super();
		if(LOG.fa) LOG.println("■ Timetable #コンストラクタ");
	}	
	/**
	 * フレームワークから初期化処理の前に呼び出される
	 * すべてのパラメータを受け取る処理
	 * 親クラスのメソッドをオーバーライドしている
	 * 親クラスのメソッドは何もしないメソッド．
	 */
	@Override
	public 	void	getParameters(){
		if(LOG.fa) LOG.outHash(htb,"■ Timetable#getParameter()");
		
		number		= 	getParameter(NUMBER);
		stamp		= 	getParameter(STAMP);
		group		= 	getParameter(GROUP);
		teUid		= 	getParameter(TUID);
		teMail		= 	getParameter(TMAIL);
		teName		= 	getParameter(TNAME);
		furigana	= 	getParameter(THKANA);
		
		cmd			= 	getParameter(CMD);
		upload		= 	getParameter(NUMBER);
		shubetsu	= 	getParameter("shubetsu");
		worder		= 	getParameter("worder");
		wdate		= 	getParameter("wdate");
		url			= 	getParameter("url");
		title		= 	getParameter("title");
		lec_key		= 	getParameter("lec_key");
		aplec_key	= 	getParameter("aplec_key");
	}
	/**
	 * 初期化処理を行なう
	 * ただし、PrintWriter out, Hashtable htb, Param paraを親クラスへ渡す処理[setInit()]と
	 * 全てのパラメータを変数に受け取る処理[getParameters()]は フレームワークで実行済みである
	 * 
	 * 参照:　superPlayer#dispatch(PrintWriter out, Hashtable htb, Param para)
	 *        superPlayer.display(PrintWriter out, Hashtable htb, Param para)
	 * 
	 * 
	 * @param out			出力ポインタ
	 * @param htb			システムハッシュ
	 * @param para			パラメータ
	 */
	@Override
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		getParameters();
		// データベースクラスを作成しておく
		broker	=	getDbConnection();
		db		=	new Database(broker);
		
		titleGraphics();
		/*
		 * 学生ユーザーデータベースに登録がなければ登録しておく
		 */
		setStudentDB();
	}
	/**
	 * タイトルグラフックスがなければ作成する
	 * 
	 * Ver 1.0.0 のバグ対応
	 * 
	 */
	void	titleGraphics(){
	    
	    String	sample	=	para.getTitleLogo();						// サンプルロゴへのフルパス
	    String	logo	=	para.getLogodir() + getParameter(GROUP) + ".gif";	// ロゴファイル
	    File	fp		=	new	File(logo);
	    if(!fp.exists()){
		    try {
	            FileGear.copyBinryFile(sample, logo);
	        } catch (IOException e) {
	            LOG.println("■makeTitleGraphics");
	            LOG.println(e.getMessage());
	            LOG.println("from:" + sample);
	            LOG.println("to  :" + logo);
	        }
	    }
	}
	
	/**
	 * 学生ユーザーとしてDBに登録があるか調べなければ登録しておく
	 *
	 */
	void	setStudentDB(){
	    
	    Student	st	=	new	Student(group, teUid, db);
	    if(st.isEmptyRecord()){
			st.set_kname(teName);
			st.set_email(teMail);
	        st.insert();
	    }
	}	
	//
	@Override
	public String dispatch(){
        if(LOG.fa) LOG.outHash(htb,"class Timetable #launch() の先頭です");
		
		ret			=	DISPATCH_DEFAULT;				// WEB表示
		disp_mode	=	DISP_NEW;
		
		// 初期設定
		if(cmd.equals("INIT_SETUP")){
			disp_mode	=	DISP_NEW;
			ret			=	"$setup.SetupView";			
			
		// パスワード設定
		}else if(cmd.equals("CHG_PASSWD")){
			disp_mode	=	DISP_NEW;
			ret			=	"$setup.TePassword";			
			
		// ファイルキャビネットを開く
		}else if(cmd.equals("CABINET")){
			ret			=	"$cabinet.FileCabinet";
			disp_mode	=	DISP_NEW;				
			
		// 科目作成
		}else if(cmd.equals("CLASS")){
			//
			disp_mode	=	DISP_NEW;
			ret			=	"$kamoku.KamokuList";			
			
		// コマを選んだ上で科目の作成を行う．終了時は科目割付へ移行する
		// 科目割付のために aplec_key を合成して保持しておく
		}else if(cmd.equals("CREATE_CLASS")){
			if(LOG.fa) LOG.println("class exwork #sub_timetable() [CLASS] : コマを選んだ上で科目の作成を行う の分岐です");
			
			System.out.println("★url= "+ url);
			//
			String	aplec_key	= KeyGen.nextAplec(htb); // 行列番号と種別キーから aplec_key を合成する
			putParameter("aplec_key",aplec_key);
			disp_mode			=	DISP_NEW;
			ret					=	"$kamoku.KamokuList";			
			
		// 科目割付
		}else if(cmd.equals("UPDATE_CLASS")){
			// KamokuTouroku_1 からの呼び出しでは _msg にメッセージが入るがここではないのでクリアしておく
			putParameter(MESSAGE, "");
			String	aplec_key	= KeyGen.nextAplec(htb); // 行列番号と種別キーから aplec_key を合成する
			putParameter("aplec_key",aplec_key);
			disp_mode			=	DISP_NEW;
			ret					=	"$kamoku.Assignment";			
			
		// 講義画面へ移る
		}else if(cmd.equals("VIEW_CLASS")){
		    /*
		     * 少なくともひとつのセクションがあることを確認する
		     */
		    if(canMove()){
			    /*
			     * シーケンス番号を埋め込んでおく
			     * "-1" は自動検索表示を指示するシーケンス番号
			     */
			    putParameter("sect_seq","-1" );
				disp_mode	=	DISP_NEW;
				ret			=	"$teacher.Kougi";			
		        
		    }else{
		        /*
		         * 詳細計画定義画面を開く
		         */
		        putParameter("lec_key",lec_key );
		        putParameter("notation", "★講義画面を開くためには最低１回分の詳細計画を作成してください");
				disp_mode	=	DISP_NEW;
				ret			=	"$kamoku.KamokuPlan";			
		        
		    }
			
		// 個人情報設定
		}else if(cmd.equals("INFO")){
			disp_mode			=	DISP_NEW;
			ret					=	"$jbbs.BbsInfo";			
			
		// その他
		}else{
			/* 
			 * cmd には起動したいクラスキーが入っている（$jbbs.･･･）
			 * 該当のクラスを起動する
			 */
			disp_mode	=	DISP_NEW;
			ret	=	cmd;
		}
		/*
		 * 次に表示するときあるいはリターンしてきた時の表示モードをセットしてからリターンする
		 */
		putParameter(DISP_KEY,disp_mode);
		return	ret;
	}
	/**
	 * セクションの有無を調べる
	 * @return
	 */
	boolean	canMove(){
		String te_lec_key		= KeyGen.get_te_lec_key2(teUid, lec_key);
		KamokuSectionDEF ksd	= new KamokuSectionDEF(te_lec_key,db);
		if(ksd.size()==0){
		    return	false;
		    
		}else{
		    return	true;
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
	 * 出力処理
	 */
	@Override
	public  void	display(boolean editmode){
		if(LOG.fa) LOG.outHash(htb,"clsss Timetable #diplay() : メインメニュー表示処理の先頭です");
		
		if(!editmode){
			putParameter("msg","");
		}
		
		// 学生画面表示のためのパラメータを設定しておく
		setStudentLogin();
		
		// 教師戻りＵＲＬを設定する
		String url  = getParameter(HOMEURL); 
		if(url!=null){
			putParameter(HOMEURL,url);
		}else{
			putParameter(HOMEURL,para.getInitTeacherURL());	// 戻りURLの規定値;
		}
		/* 表示する
		 *  getParameter(DISPFILE)はファイルの完全パス名 
		 */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}
	/**
	 * 学生画面表示用アカウントとパスワード
	 * 
	 */
	public	void	setStudentLogin(){
		//
		// 学生画面表示用アカウントとパスワード

		String	stNumber	=	teUid;
		putParameter("stNumber", stNumber );
		putParameter("stPasswd", stNumber );	// どちらも学籍番号をあてる
		putParameter("stMail"  , getParameter(TMAIL));
	}

	@Override
	public void	write(String key,Vector exHtml){
		if(key.equals("table_1")){
			table_1(exHtml);

		}else if(key.equals("table_2")){
			table_2(exHtml);
		
		}else if(key.equals("table_3")){
			table_3(exHtml);
		}else if(key.equals("top_table_1")){
			top_table_1(exHtml);
			
		}else if(key.equals("top_table_2")){
			top_table_2(exHtml);
			
		}else if(key.equals("top_table_3")){
			top_table_3(exHtml);
			
		}
	}
	void	table_1(Vector exHtml){
		String 	[] 	title 		= new String[10];	// 一行分のタイトルを入れる
		String 	[] 	aplec_key 	= new String[10];	// 一行分の講義実施キーを入れる
		String 	[] 	lec_key 	= new String[10];	// 一行分の講義定義キーを入れる
		String		shubetsu	= "1";				// 一般講義
		//
		//String	teUid		= strHash(htb,"_teUid");	// 教員コード
		
		KamokuAP	kap	= new KamokuAP(teUid,db);	// 教員の全科目実施データ
		String 		num = getParameter("rows");		// blockWrite() で記録している時間割の表示行数
		int    		n	= Integer.parseInt( num );
		for(int gyo=0; gyo < n ; gyo++){
			//
			putParameter("gyo",String.valueOf(gyo));						// 行番号をハッシュに入れる
			for(int wdate=0; wdate < 6; wdate++){
				KamokuApRecord rec = kap.fromHash(shubetsu, gyo, wdate);	// この時限（gyo) の曜日(wdate)の科目レコードを得る
				String flag = "flag" + String.valueOf(wdate);				// flag0 - flag5 を作っておく（出力制御用）
				if(rec==null){
				    title[wdate] 		= "";
					aplec_key[wdate]	= "";
					lec_key[wdate]		= "";
					putParameter(flag,"replace");				// 差し替えることを指示する（置き換えとリンクを含まないデータに）
				}else{
					//title[wdate] 		= rec.title();
					title[wdate] 		= KamokuDefRecord.getTitle(teUid,rec.lec_key(),db);	//(static) 科目定義レコードから科目名を得る
					aplec_key[wdate]	= rec.aplec_key();
					lec_key[wdate]		= rec.lec_key();
					putParameter(flag,"");						// 差し替えないことを指示する
				}
			}
			putParameter("title-0",title[0]);			// 講義タイトル文字列
			putParameter("title-1",title[1]);
			putParameter("title-2",title[2]);
			putParameter("title-3",title[3]);
			putParameter("title-4",title[4]);
			putParameter("title-5",title[5]);
			//
			if(title[0].length() > 9 ){ putParameter("sz-0","11"); }else{ putParameter("sz-0","12"); }	// フォントサイズ
			if(title[1].length() > 9 ){ putParameter("sz-1","11"); }else{ putParameter("sz-1","12"); }
			if(title[2].length() > 9 ){ putParameter("sz-2","11"); }else{ putParameter("sz-2","12"); }
			if(title[3].length() > 9 ){ putParameter("sz-3","11"); }else{ putParameter("sz-3","12"); }
			if(title[4].length() > 9 ){ putParameter("sz-4","11"); }else{ putParameter("sz-4","12"); }
			if(title[5].length() > 9 ){ putParameter("sz-5","11"); }else{ putParameter("sz-5","12"); }
			//
			putParameter("aplec_key-0",aplec_key[0]);	// 講義実施キー
			putParameter("aplec_key-1",aplec_key[1]);
			putParameter("aplec_key-2",aplec_key[2]);
			putParameter("aplec_key-3",aplec_key[3]);
			putParameter("aplec_key-4",aplec_key[4]);
			putParameter("aplec_key-5",aplec_key[5]);
			//
			putParameter("lec_key-0",lec_key[0]);		// 講義定義キー
			putParameter("lec_key-1",lec_key[1]);
			putParameter("lec_key-2",lec_key[2]);
			putParameter("lec_key-3",lec_key[3]);
			putParameter("lec_key-4",lec_key[4]);
			putParameter("lec_key-5",lec_key[5]);
			//
			printVector(exHtml);
		}
		
		
	}
	void	table_2(Vector exHtml){
		String 	[] 	title 		= new String[10];	// 一行分のタイトルを入れる
		String 	[] 	aplec_key 	= new String[10];	// 一行分の講義実施キーを入れる
		String 	[] 	lec_key 	= new String[10];	// 一行分の講義定義キーを入れる
		String		shubetsu	= "2";				// 一般講義
		//
		KamokuAP	kap			= new KamokuAP(teUid,db);	// その教員の全科目実施データ
		//
		String num 	= getParameter("nightSchool");			// blockWrite() で記録している夜間時間割の表示行数
		int    n	= Integer.parseInt( num );
		for(int gyo=0; gyo < n ; gyo++){
			//
			putParameter("gyo",String.valueOf(gyo));						// 行番号をハッシュに入れる
			for(int wdate=0; wdate < 6; wdate++){
				KamokuApRecord rec = kap.fromHash(shubetsu, gyo, wdate);	// この時限（gyo) の曜日(wdate)の科目レコードを得る
				String flag = "flag" + String.valueOf(wdate);				// flag0 - flag5 を作っておく（出力制御用）
				if(rec==null){
				    title[wdate] 		= "";
					aplec_key[wdate]	= "";
					lec_key[wdate]		= "";
					putParameter(flag,"replace");				// 差し替えることを指示する（置き換えとリンクを含まないデータに）
				}else{
					title[wdate] 		= KamokuDefRecord.getTitle(teUid,rec.lec_key(),db);	//(static) 科目定義レコードから科目名を得る
					aplec_key[wdate]	= rec.aplec_key();
					lec_key[wdate]		= rec.lec_key();
					putParameter(flag,"");						// 差し替えないことを指示する
				}
			}
			putParameter("title-0",title[0]);			// 講義タイトル文字列
			putParameter("title-1",title[1]);
			putParameter("title-2",title[2]);
			putParameter("title-3",title[3]);
			putParameter("title-4",title[4]);
			putParameter("title-5",title[5]);
			//
			if(title[0].length() > 9 ){ putParameter("sz-0","11"); }else{ putParameter("sz-0","12"); }	// フォントサイズ
			if(title[1].length() > 9 ){ putParameter("sz-1","11"); }else{ putParameter("sz-1","12"); }
			if(title[2].length() > 9 ){ putParameter("sz-2","11"); }else{ putParameter("sz-2","12"); }
			if(title[3].length() > 9 ){ putParameter("sz-3","11"); }else{ putParameter("sz-3","12"); }
			if(title[4].length() > 9 ){ putParameter("sz-4","11"); }else{ putParameter("sz-4","12"); }
			if(title[5].length() > 9 ){ putParameter("sz-5","11"); }else{ putParameter("sz-5","12"); }
			//
			putParameter("aplec_key-0",aplec_key[0]);	// 講義実施キー
			putParameter("aplec_key-1",aplec_key[1]);
			putParameter("aplec_key-2",aplec_key[2]);
			putParameter("aplec_key-3",aplec_key[3]);
			putParameter("aplec_key-4",aplec_key[4]);
			putParameter("aplec_key-5",aplec_key[5]);
			//
			putParameter("lec_key-0",lec_key[0]);		// 講義定義キー
			putParameter("lec_key-1",lec_key[1]);
			putParameter("lec_key-2",lec_key[2]);
			putParameter("lec_key-3",lec_key[3]);
			putParameter("lec_key-4",lec_key[4]);
			putParameter("lec_key-5",lec_key[5]);
			//
			printVector(exHtml);
		}
		
		
	}
	void	table_3(Vector exHtml){
		String 	[] 	title 		= new String[10];	// 一行分のタイトルを入れる
		String 	[] 	aplec_key 	= new String[10];	// 一行分の講義実施キーを入れる
		String 	[] 	lec_key 	= new String[10];	// 一行分の講義定義キーを入れる
		String		shubetsu	= "3";				// 一般講義
		//
		String	  	teUid		= strHash(htb,"_teUid");	// 教員コード
		KamokuAP	kap			= new KamokuAP(teUid,db);	// その教員の全科目実施データ
		//
		String num 	= getParameter("eLearningSchool");		// blockWrite() で記録している時間割の表示行数
		int    n	= Integer.parseInt( num );
		for(int gyo=0; gyo < n ; gyo++){
			//
			putParameter("gyo",String.valueOf(gyo));						// 行番号をハッシュに入れる
			for(int wdate=0; wdate < 6; wdate++){
				KamokuApRecord rec = kap.fromHash(shubetsu, gyo, wdate);	// この時限（gyo) の曜日(wdate)の科目レコードを得る
				String flag = "flag" + String.valueOf(wdate);				// flag0 - flag5 を作っておく（出力制御用）
				if(rec==null){
				    title[wdate] 		= "";
					aplec_key[wdate]	= "";
					lec_key[wdate]		= "";
					putParameter(flag,"replace");				// 差し替えることを指示する（置き換えとリンクを含まないデータに）
				}else{
					title[wdate] 		= KamokuDefRecord.getTitle(teUid,rec.lec_key(),db);	//(static) 科目定義レコードから科目名を得る
					aplec_key[wdate]	= rec.aplec_key();
					lec_key[wdate]		= rec.lec_key();
					putParameter(flag,"");						// 差し替えないことを指示する
				}
			}
			putParameter("title-0",title[0]);			// 講義タイトル文字列
			putParameter("title-1",title[1]);
			putParameter("title-2",title[2]);
			putParameter("title-3",title[3]);
			putParameter("title-4",title[4]);
			putParameter("title-5",title[5]);
			//
			if(title[0].length() > 9 ){ putParameter("sz-0","11"); }else{ putParameter("sz-0","12"); }	// フォントサイズ
			if(title[1].length() > 9 ){ putParameter("sz-1","11"); }else{ putParameter("sz-1","12"); }
			if(title[2].length() > 9 ){ putParameter("sz-2","11"); }else{ putParameter("sz-2","12"); }
			if(title[3].length() > 9 ){ putParameter("sz-3","11"); }else{ putParameter("sz-3","12"); }
			if(title[4].length() > 9 ){ putParameter("sz-4","11"); }else{ putParameter("sz-4","12"); }
			if(title[5].length() > 9 ){ putParameter("sz-5","11"); }else{ putParameter("sz-5","12"); }
			//
			putParameter("aplec_key-0",aplec_key[0]);	// 講義実施キー
			putParameter("aplec_key-1",aplec_key[1]);
			putParameter("aplec_key-2",aplec_key[2]);
			putParameter("aplec_key-3",aplec_key[3]);
			putParameter("aplec_key-4",aplec_key[4]);
			putParameter("aplec_key-5",aplec_key[5]);
			//
			putParameter("lec_key-0",lec_key[0]);		// 講義定義キー
			putParameter("lec_key-1",lec_key[1]);
			putParameter("lec_key-2",lec_key[2]);
			putParameter("lec_key-3",lec_key[3]);
			putParameter("lec_key-4",lec_key[4]);
			putParameter("lec_key-5",lec_key[5]);
			//
			printVector(exHtml);
		}
		
		
	}
	void	top_table_1(Vector exHtml){
		
		// 設定情報を読んで判断する
		if(teMail==null)	LOG.println("Timetable #blockWrite() : teMail is null");
		if(db==null)		LOG.println("Timetable #blockWrite() : db is null");
		Setup	info	= new Setup(getParameter(TUID),db);

		int   	lines  	= info.rows();											// 時間割行数（１オリジン）
		if(LOG.fa){ LOG.println("Setup:rows() ",String.valueOf(lines)); }	
		//
		if(lines > 0) {
			putParameter("rows",String.valueOf(lines));							// 時間割行数が正なら出力するので
			printVector(exHtml);												// 行数を htb に保存して 出力処理を実行する
		}
		
		
	}
	void	top_table_2(Vector exHtml){

		// 設定情報を読んで判断する
		Setup 	info   	= new Setup(getParameter(TUID),db);
		int   	lines  	= info.nightSchool();									// 夜間時間割行数（１オリジン）
		if(LOG.fa){ LOG.println("Setup:nightSchool() ",String.valueOf(lines)); }
		//
		if(lines > 0) {
			putParameter("nightSchool",String.valueOf(lines));
			printVector(exHtml);												// 行数を htb に保存して 出力処理を実行する
		}

		
	}
	void	top_table_3(Vector exHtml){

		// 設定情報を読んで判断する
		Setup 	info   	= new Setup(getParameter(TUID),db);
		int		lines  	= info.eLearningSchool();								// e-Learningコースの有無（0/1）
		if(LOG.fa){ LOG.println("Setup:eLearningSchool() ",String.valueOf(lines)); }
		if(lines >= 1) {
			putParameter("eLearningSchool",String.valueOf(lines));
			printVector(exHtml);												// 行数を htb に保存して 出力処理を実行する
		}

		
	}

}