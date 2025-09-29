/*
     



*/
package student;

import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import database.Database;
import database.DbConnectionBroker;
import framework.LOG;
import framework.Param;
import framework.SuperPlayer;
import kamoku.KamokuApRecord;
import	setup.Setup;
import tktools.Csv;

/**
 *
 *
 	#
	# ##################
	#     StTable
	# ##################
	#
	<program $student.StTable>
		<dispatch  html=stTable.html  number=5100  class=student.StTable />
		<variable>
		  <receive   NUMBER STAMP GROUP StUID StCLASSINFO StNAME StMAIL StKEITAI />
		  <accept    CMD shubetsu worder wdate TUID lec_key aplec_key title />
		  <keep      />
		  
		  <form      />
		</variable>
	</program> 
 *
 *
 * 変数の説明
 *
 * 1. receive
 * 		NUMBER			プログラム番号
 * 		STAMP			タイムスタンプ
 * 		GROUP			所属グループID
 * 		StUID			学籍番号（ユーザー名）
 * 		StCLASSINFO		受講情報のCSV文字列
 * 		StNAME			漢字氏名
 * 		StMAIL			e-メールアドレス
 * 		StKEITAI		携帯メールアドレス
 * 
 * 2. accept
 * 		shubetsu		時間割種別（一般、夜間、e-LEarning）
 * 		worder			時限
 * 		wdate			曜日
 * 		TUID			科目担当教師ID
 * 		lec_key			科目キー
 * 		aplec_key		講義実施キー
 * 		title			漢字科目名
 * 		
 * 3. keep
 * 4. form
 *
 *
 */
public class StTable extends SuperPlayer {
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
	
	public	StTable(){
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
	@Override
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		broker	=	getDbConnection();
		db		=	new Database(broker);			

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
	@Override
	public	String	dispatch(){
		if(LOG.fa) LOG.println("■StTable #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;

		if(cmd.equals("VIEW_CLASS")){
			/*
			 * 講義画面を表示する
			 */
			ret			=	"$student.StKougi";
			disp_mode	=	DISP_NEW;

		}else if(cmd.equals("SEND_Q")){
			/*
			 * 質問メールを出す 
			 */
			ret			=	"$student.StMail";
			disp_mode	=	DISP_NEW;
			
		}else if(cmd.equals("SYLABUS")){
			/*
			 * シラバスを見る
			 */
			
			ret			=	"$student.StSyllabus";
			disp_mode	=	DISP_NEW;

		}else if(cmd.equals("CABINET")){
			/*
			 * ファイルキャビネット
			 */
			ret			=	"$cabinet.FileCabinet";
			disp_mode	=	DISP_NEW;
			
		}else if(cmd.equals("PASSWD")){
			/*
			 * パスワード変更
			 * 
			 * guest は変更できないようにする
			 */
			
			String id = (String)htb.get(StUID);
			//LOG.println("★id=" + id);
			if(id!=null&&id.equals("guest")) {
				ret			=	DISPATCH_DEFAULT;
				disp_mode	=	DISP_EDIT;	
				
			}else {
			
				ret			=	"$student.StPasswd";
				disp_mode	=	DISP_NEW;
			}
			
		}else if(cmd.equals("REGMAIL")){
			/*
			 * メールアドレス設定
			 * 
			 * guest は変更できないようにする
			 */
			String id = (String)htb.get(StUID);
			//LOG.println("★id=" + id);
			if(id!=null&&id.equals("guest")) {
				ret			=	DISPATCH_DEFAULT;
				disp_mode	=	DISP_EDIT;					
			}else {
				ret			=	"$student.StRegistMail";
				disp_mode	=	DISP_NEW;
			}
			
		}else if(cmd.equals("INFO")){
			/*
			 * 個人情報設定
			 * guest は変更できないようにする
			 */
			String id = (String)htb.get(StUID);
			//LOG.println("★id=" + id);
			if(id!=null&&id.equals("guest")) {
				ret			=	DISPATCH_DEFAULT;
				disp_mode	=	DISP_EDIT;		
			}else {
				ret			=	"$jbbs.BbsInfo";
				disp_mode	=	DISP_NEW;
			}

		}else{
			/*
			 * エラー回避
			 */
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;
		}
		/*
		 * 次に表示するときあるいはリターンしてきた時の表示モードをセットしてからリターンする
		 */
		putParameter(DISP_KEY,disp_mode);
		return	ret;
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
	@Override
	public  void	display(boolean editmode){
		if(LOG.fa)	LOG.println("■ Sample #display(boolean editmode)");
		
		if(!editmode){
			putParameter(MESSAGE,"");
		}
		set_displayData();
		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}
    //
    // 画面を表示する
    //
    public void set_displayData(){
		//
        //  一般時間割、夜間、e-Learning の表示行数をあらかじめ求めて htb に入れておく
		// 
		Hashtable	teachers	=	teachersInfoHash();
		Hashtable 	hs 			= 	classInfoHash();
		putObject("_infoHash",hs); // あとで使う
		/*
		 * 昼間、夜間をtype1で、e-learningをtype2で設定する
		 */
		setGyo_type1(teachers);
		setGyo_type2(hs);
		
        //学生の戻りＵＲＬを設定
		String	szDB	= getParameter("_szDB");
		String 	url_s	= db.getUrl_s(szDB);
		putParameter("_url_s",url_s);
		
		return;
    }
    /**
     * 教師と同じ行数を表示するようにする
     * 
     * 教師の学生ログインの場合は、teachers が nullの場合がある
     * 
     * @param teachers
     */
	void	setGyo_type1(Hashtable teachers){
	    
	    int		t1		=	0;
	    int		t2		=	0;
	    //int		t3		=	0;
	    /*
	     * 全ての教師について調べ、最大行数をセットする
	     */
	    if(teachers==null){
	        // 教師の学生ログインである
	        // 教師IDとして学籍番号をあて、キーはダミーを設定する
	        // これで、教師の設定データが読める
	        teachers	=	new	Hashtable();
	        teachers.put(getParameter(StUID), getParameter(StUID) + "-aaa");
	        
	    }
	    
	    Enumeration	e	=	teachers.keys();
	    while(e.hasMoreElements()){
	        String	key		=	(String)(e.nextElement());
	        Setup	setup	=	new	Setup(key, db);
	        int		st1		=	setup.rows();
	        int		st2		=	setup.nightSchool();
	        //int		st3		=	setup.eLearningSchool();
	        
	        if(t1<st1)	t1	=	st1;
	        if(t2<st2)	t2	=	st2;
	        //if(t3<st3)	t3	=	st3;
	    }
		putParameter("_line_1",String.valueOf(t1));
		putParameter("_line_2",String.valueOf(t2));
		//putParameter("_line_3",String.valueOf(t3));	    
	    
	}
	/**
	 * 学生の受講状態により表示行数を変える
	 * @param hs
	 */
    void	setGyo_type2(Hashtable hs){
		
		/*
        int	type_1 = countLines(hs,"1",Setup.max_gyo,6);
		if(type_1<6){
				type_1 = 5;	// 5行以下は5行
		}
		int	type_2 = countLines(hs,"2",Setup.max_night,6);
		if(type_2>0){
			if(type_2<4){
			    type_2 = 3;	// ３限目以下は３行を表示
			}
		}else{
			type_2 = 0; // 表示しないケース. 念のため
		}
		*/
		int type_3 = countLines(hs,"3",Setup.max_course,6);
		
		//putParameter("_line_1",String.valueOf(type_1));
		//putParameter("_line_2",String.valueOf(type_2));
		putParameter("_line_3",String.valueOf(type_3));

    }
    
	//
	// 表示すべき最大行番号を調べます
	//
	int	countLines(Hashtable hs,String type,int maxgyo,int maxdate){
		if(LOG.fa) LOG.println("class stwork #countLines() : 表示すべき最大行番号を調べます");
		//
		int lines	= -1;
		for(int i=0; i<maxgyo; i++){
			for(int j=0; j<maxdate; j++){
				String  aplec_key	= type + String.valueOf(i) + String.valueOf(j);
				String	teUid		= (String)hs.get(aplec_key);
				if(teUid!=null){
					lines = i;
					if(LOG.fa) LOG.println("class stwork #countLines() : 最大行番号を更新します / no = " + (lines + 1) + "/ teUid = " + teUid + " / aplec_key = " + aplec_key);
				}
			}
		}
		return lines + 1; // １オリジンに直す
	}    
	//
	// 受講情報をaplec_key で引くためのハッシュを作成
	// また、何人の教師の講義を受講しているかを知るためのハッシュを作成
	//
	Hashtable classInfoHash(){
		//
		// Student オブジェクトを作成し、講義受講情報（classInfo）を取り出す
		// classInfo は　"kawaba01-101,kawaba01-123" のようなCSV形式の文字列である
		// (teUid)-(aplec_key) の形式で、これが te_aplec_key
		// このうち、aplec_key の部分は（種別、時限、曜日）の番号を繋げたものである
		// 種別（１～３）、時限（０～８）、曜日（０～５）となっている
		//
		String	szDB		= getParameter("_szDB");
		String	stNumber	= getParameter("_stNumber");
		//
		// 漢字氏名は StNAME で受け取っているので不要になった2005.3.12
		//Student	st			= new Student(szDB,stNumber, db);
		//putParameter("_kname",st.kname());	// 漢字氏名　2004.8.15
		//
		String	classInfo	= getParameter(StCLASSINFO);
		Csv		cs			= new Csv(classInfo,",",true);
		int		max			= cs.size();
		//
		// classInfo を　Csv に変換し、 te_aplec_key を順に取り出して、teUid と aplec_key に分解する
		// そして、aplec_key をキーとして、（aplec_key, teUid) のハッシュを作成する
		// こうすると、時間割の描画の中で te_aplec_key を生成し、該当する時間帯に受講している講義が
		// あるかどうか、このハッシュを引いて求めることができる
		//
		Hashtable	hs		= new Hashtable(50);
		Hashtable	teacher	= new Hashtable(20);
		
		for(int i=0; i<max; i++){
			Csv	temp	= new Csv( cs.get(i), "-");
			hs.put(temp.get(1),temp.get(0));
			/*
			 * 教師IDをキーとするハッシュも作成する
			 * 何人の教師の講義を受講しているかを知るため
			 */
			teacher.put(temp.get(0),temp.get(1));
		}
		//if(DBG._tr090) DBG.outHash(hs,"class stwork #paramPrintOPT() [table_1] : 受講情報から作成した検索用Ｈａｓｈです");
		return	hs;
	}
	
	Hashtable teachersInfoHash(){
		//
		// 講義受講情報（classInfo）を取り出す
		// classInfo は　"kawaba01-101,kawaba01-123" のようなCSV形式の文字列である
		// (teUid)-(aplec_key) の形式で、これが te_aplec_key
		// このうち、aplec_key の部分は（種別、時限、曜日）の番号を繋げたものである
		// 種別（１～３）、時限（０～８）、曜日（０～５）となっている
		//
		String	szDB		= getParameter("_szDB");
		String	stNumber	= getParameter("_stNumber");
		String	classInfo	= getParameter(StCLASSINFO);
		/*
		 * データがなければnullを返す
		 * これは教師の学生としてのログインで発生するケース
		 */
		if(isEmptyData(classInfo)||classInfo.equals("-")){
		    return	null;
		}
		
		
		Csv		cs			= new Csv(classInfo,",",true);
		int		max			= cs.size();
		
		//
		// classInfo を　Csv に変換し、 te_aplec_key を順に取り出して、teUid と aplec_key に分解する
		// そして、teUid をキーとして、（aplec_key ,aplec_key) のハッシュを作成する
		// keys() によって教師情報を得ることができる
		//
		Hashtable	teacher	= new Hashtable(20);
		
		for(int i=0; i<max; i++){
			Csv	temp	= new Csv( cs.get(i), "-");
			/*
			 * 教師IDをキーとするハッシュを作成する
			 * 何人の教師の講義を受講しているかを知るため
			 */
			teacher.put(temp.get(0),temp.get(1));
		}
		return	teacher;
	}
    //
    // ハッシュテーブル(htb)を使って部分的に切り取ったソースデータ（exHtml）
    // の内容を置き換えて出力処理する．表などの反復出力に利用するが個々の処理
    // 内容は、key で特定される．
    //
    @Override
	public void write(String key,Vector exHtml){
    	
    	if(key.equals("top_table_1")){
    		top_table_1(exHtml);
    		
    	}else if(key.equals("table_1")){
    		table_1(exHtml);
        		
    	}else if(key.equals("top_table_2")){
    		top_table_2(exHtml);
        		
    	}else if(key.equals("table_2")){
    		table_2(exHtml);
        		
    	}else if(key.equals("top_table_3")){
    		top_table_3(exHtml);
        		
    	}else if(key.equals("table_3")){
    		table_3(exHtml);
        		
    	}
    }
    void	top_table_1(Vector exHtml){

    	int lines = Integer.parseInt( getParameter("_line_1"));
		if(lines > 0) {
			if(LOG.fa)  LOG.println("class stwork #paramPrintBLK() [top_table_1] :一般時間割を表示します");
			putParameter("_rows",String.valueOf(lines));			// 時間割行数が正なら出力するので
			printVector(exHtml);
		}
    	
    }
    void	table_1(Vector exHtml){
    	
		/*
		//
		// Student オブジェクトを作成し、講義受講情報（classInfo）を取り出す
		// classInfo は　"kawaba01-101,kawaba01-123" のようなCSV形式の文字列である
		// (teUid)-(aplec_key) の形式で、これが te_aplec_key
		// このうち、aplec_key の部分は（種別、時限、曜日）の番号を繋げたものである
		// 種別（１～３）、時限（０～４）、曜日（０～５）となっている
		//
		String	szDB		= (String) htb.get("_szDB");
		String	stNumber	= (String) htb.get("_stNumber");
		Student	st			= new Student(szDB,stNumber, db);
		String	classInfo	= st.classInfo();
		Csv		cs			= new Csv(classInfo);
		int		max			= cs.size();
		//
		// classInfo を　Csv に変換し、 te_aplec_key を順に取り出して、teUid と aplec_key に分解する
		// そして、aplec_key をキーとして、（aplec_key, teUid) のハッシュを作成する
		// こうすると、時間割の描画の中で te_aplec_key を生成し、該当する時間帯に受講している講義が
		// あるかどうか、このハッシュを引いて求めることができる
		//
		Hashtable	hs		= new Hashtable(100);
		for(int i=0; i<max; i++){
			Csv	temp	= new Csv( cs.get(i), "-");
			hs.put(temp.get(1),temp.get(0));
		}
		*/
		Hashtable	hs = (Hashtable)getObject("_infoHash");
		if(LOG.fa) LOG.outHash(hs,"class stwork #paramPrintOPT() [table_1] : 受講情報から作成した検索用Ｈａｓｈです");
		// 変数の準備
		String 	[] 	title 		= new String[10];	// 一行分のタイトルを入れる
		String 	[] userID 		= new String[10];	// 
		String 	[] 	aplec_key 	= new String[10];	// 一行分の講義実施キーを入れる
		String 	[] 	lec_key 	= new String[10];	// 一行分の講義定義キーを入れる
		String		shubetsu	= "1";				// 一般講義
		//
		//  
		//
		int gyoMax 			= Integer.parseInt( getParameter("_line_1"));// １オリジン
		//int display_gyo 	= 0;
		for(int gyo=0; gyo < gyoMax  ; gyo++){
			//
			putParameter("_gyo",String.valueOf(gyo));							// 行番号をハッシュに入れる
			//
			int	wdateMax 	= 6;// １オリジン
			for(int wdate=0; wdate < wdateMax; wdate++){
				//
				// 種別＋時限＋曜日でaplec_keyを求め、これで hs を検索して teUid を求める
				// もしも null でない teUid が求まれば、このコマを受講している事になる
				// 
				String _aplec_key	= shubetsu + String.valueOf(gyo) + String.valueOf(wdate);
				String _teUid		= (String)hs.get(_aplec_key);
				if(LOG.fa) LOG.println("class stwork #paramPrintOPT() [table_1] : ハッシュhs を検索した結果　teUid = " + _teUid);
				String flag 		= "_flag" + String.valueOf(wdate);				// flag0 - flag5 を作っておく（出力制御用）
				if(isEmpty(_teUid)){
				    // 受講していないケース
					title[wdate] 		= "";
					userID[wdate]		= "";
					aplec_key[wdate]	= "";
					lec_key[wdate]		= "";
					putParameter(flag,"replace");	// ブランク行と差し替えることを指示する（置き換えとリンクを含まないデータに）
				}else{
					// 受講しているケース
					// 科目実施レコードを引いてweb表示に必要な項目を埋める
					KamokuApRecord kar	= new KamokuApRecord( _teUid, _aplec_key,db);
					title[wdate] 		= kar.title();			// 科目名
					userID[wdate]		= _teUid;				// 教員キー
					aplec_key[wdate]	= kar.aplec_key();		// 科目実施キー
					lec_key[wdate]		= kar.lec_key();		// 科目定義キー
					putParameter(flag,"");							// ブランク行と差し替えないことを指示する
				}
			}
			/*
			//
			// 表示すべき行数を htb に残す
			if(display_gyo <= 4){
				display_gyo	= 4;
			}else{
				display_gyo = 5;
			}
			putParameter("_lines-1",String.valueOf(display_gyo));
			*/
			
			//
			putParameter("_title-0",title[0]);			// 講義タイトル文字列
			putParameter("_title-1",title[1]);
			putParameter("_title-2",title[2]);
			putParameter("_title-3",title[3]);
			putParameter("_title-4",title[4]);
			putParameter("_title-5",title[5]);
			//
			if(title[0].length() > 9 ){ putParameter("_sz-0","11"); }else{ putParameter("_sz-0","12"); }	// フォントサイズ
			if(title[1].length() > 9 ){ putParameter("_sz-1","11"); }else{ putParameter("_sz-1","12"); }
			if(title[2].length() > 9 ){ putParameter("_sz-2","11"); }else{ putParameter("_sz-2","12"); }
			if(title[3].length() > 9 ){ putParameter("_sz-3","11"); }else{ putParameter("_sz-3","12"); }
			if(title[4].length() > 9 ){ putParameter("_sz-4","11"); }else{ putParameter("_sz-4","12"); }
			if(title[5].length() > 9 ){ putParameter("_sz-5","11"); }else{ putParameter("_sz-5","12"); }
			//
			putParameter("_teUid-0",userID[0]);			// 教師キー
			putParameter("_teUid-1",userID[1]);
			putParameter("_teUid-2",userID[2]);
			putParameter("_teUid-3",userID[3]);
			putParameter("_teUid-4",userID[4]);
			putParameter("_teUid-5",userID[5]);
			//
			putParameter("_aplec_key-0",aplec_key[0]);	// 講義実施キー
			putParameter("_aplec_key-1",aplec_key[1]);
			putParameter("_aplec_key-2",aplec_key[2]);
			putParameter("_aplec_key-3",aplec_key[3]);
			putParameter("_aplec_key-4",aplec_key[4]);
			putParameter("_aplec_key-5",aplec_key[5]);
			//
			putParameter("_lec_key-0",lec_key[0]);		// 講義定義キー
			putParameter("_lec_key-1",lec_key[1]);
			putParameter("_lec_key-2",lec_key[2]);
			putParameter("_lec_key-3",lec_key[3]);
			putParameter("_lec_key-4",lec_key[4]);
			putParameter("_lec_key-5",lec_key[5]);
			//
			printVector(exHtml);
		}
    	
    }
    void	top_table_2(Vector exHtml){
    	
		int lines = Integer.parseInt( getParameter("_line_2"));
		if(lines > 0) {
			if(LOG.fa)  LOG.println("class stwork #paramPrintBLK() [top_table_2] : 夜間時間割を表示します");
			putParameter("_nightSchool",String.valueOf(lines));
			printVector(exHtml);
		}
    	
    }
    void	table_2(Vector exHtml){
    	
		/*
		//
		// Student オブジェクトを作成し、講義受講情報（classInfo）を取り出す
		// classInfo は　"kawaba01-101,kawaba01-123" のようなCSV形式の文字列である
		// (teUid)-(aplec_key) の形式で、これが te_aplec_key
		// このうち、aplec_key の部分は（種別、時限、曜日）の番号を繋げたものである
		// 種別（１～３）、時限（０～４）、曜日（０～５）となっている
		//
		String	szDB		= (String) htb.get("_szDB");
		String	stNumber	= (String) htb.get("_stNumber");
		Student	st			= new Student(szDB,stNumber, db);
		String	classInfo	= st.classInfo();
		Csv		cs			= new Csv(classInfo);
		int		max			= cs.size();
		//
		// classInfo を　Csv に変換し、 te_aplec_key を順に取り出して、teUid と aplec_key に分解する
		// そして、aplec_key をキーとして、（aplec_key, teUid) のハッシュを作成する
		// こうすると、時間割の描画の中で te_aplec_key を生成し、該当する時間帯に受講している講義が
		// あるかどうか、このハッシュを引いて求めることができる
		//
		Hashtable	hs		= new Hashtable(100);
		for(int i=0; i<max; i++){
			Csv	temp	= new Csv( cs.get(i), "-");
			hs.put(temp.get(1),temp.get(0));
		}
		*/
		Hashtable	hs = (Hashtable)getObject("_infoHash");
		if(LOG.fa) LOG.outHash(hs,"class stwork #paramPrintOPT() [table_2] : 受講情報から作成した検索用Ｈａｓｈです");
		// 変数の定義
		String 	[] 	title 		= new String[10];	// 一行分のタイトルを入れる
		String 	[] userID 		= new String[10];	// 
		String 	[] 	aplec_key 	= new String[10];	// 一行分の講義実施キーを入れる
		String 	[] 	lec_key 	= new String[10];	// 一行分の講義定義キーを入れる
		String		shubetsu	= "2";				// 講義
		//
		int gyoMax 			= Integer.parseInt( getParameter("_line_2"));	// １オリジン
		//int display_gyo 	= 0;
		for(int gyo=0; gyo < gyoMax  ; gyo++){
			//
			putParameter("_gyo",String.valueOf(gyo));							// 行番号をハッシュに入れる
			//
			int	wdateMax = 6;	// １オリジン
			for(int wdate=0; wdate < wdateMax; wdate++){
				//KamokuApRecord rec = kap.fromHash(shubetsu, gyo, wdate);	// この時限（gyo) の曜日(wdate)の科目レコードを得る
				String _aplec_key	= shubetsu + String.valueOf(gyo) + String.valueOf(wdate);
				String _teUid		= (String)hs.get(_aplec_key);
				if(LOG.fa) LOG.println("class stwork #paramPrintOPT() [table_1] : ハッシュhs を検索した結果　teUid = " + _teUid);
				String flag 		= "_flag" + String.valueOf(wdate);				// flag0 - flag5 を作っておく（出力制御用）
				if(isEmpty(_teUid)){
				    title[wdate] 		= "";
					userID[wdate]		= "";
					aplec_key[wdate]	= "";
					lec_key[wdate]		= "";
					putParameter(flag,"replace");	// ブランク行と差し替えることを指示する（置き換えとリンクを含まないデータに）
				}else{
					KamokuApRecord kar	= new KamokuApRecord( _teUid, _aplec_key,db);
					title[wdate] 		= kar.title();			// 科目名
					userID[wdate]		= _teUid;				// 教員キー
					aplec_key[wdate]	= kar.aplec_key();		// 科目実施キー
					lec_key[wdate]		= kar.lec_key();		// 科目定義キー
					putParameter(flag,"");							// ブランク行と差し替えないことを指示する
				}
			}
			
			putParameter("_title-0",title[0]);			// 講義タイトル文字列
			putParameter("_title-1",title[1]);
			putParameter("_title-2",title[2]);
			putParameter("_title-3",title[3]);
			putParameter("_title-4",title[4]);
			putParameter("_title-5",title[5]);
			//
			if(title[0].length() > 9 ){ putParameter("_sz-0","11"); }else{ putParameter("_sz-0","12"); }	// フォントサイズ
			if(title[1].length() > 9 ){ putParameter("_sz-1","11"); }else{ putParameter("_sz-1","12"); }
			if(title[2].length() > 9 ){ putParameter("_sz-2","11"); }else{ putParameter("_sz-2","12"); }
			if(title[3].length() > 9 ){ putParameter("_sz-3","11"); }else{ putParameter("_sz-3","12"); }
			if(title[4].length() > 9 ){ putParameter("_sz-4","11"); }else{ putParameter("_sz-4","12"); }
			if(title[5].length() > 9 ){ putParameter("_sz-5","11"); }else{ putParameter("_sz-5","12"); }
			//
			putParameter("_teUid-0",userID[0]);			// 教師キー
			putParameter("_teUid-1",userID[1]);
			putParameter("_teUid-2",userID[2]);
			putParameter("_teUid-3",userID[3]);
			putParameter("_teUid-4",userID[4]);
			putParameter("_teUid-5",userID[5]);
			//
			putParameter("_aplec_key-0",aplec_key[0]);	// 講義実施キー
			putParameter("_aplec_key-1",aplec_key[1]);
			putParameter("_aplec_key-2",aplec_key[2]);
			putParameter("_aplec_key-3",aplec_key[3]);
			putParameter("_aplec_key-4",aplec_key[4]);
			putParameter("_aplec_key-5",aplec_key[5]);
			//
			putParameter("_lec_key-0",lec_key[0]);		// 講義定義キー
			putParameter("_lec_key-1",lec_key[1]);
			putParameter("_lec_key-2",lec_key[2]);
			putParameter("_lec_key-3",lec_key[3]);
			putParameter("_lec_key-4",lec_key[4]);
			putParameter("_lec_key-5",lec_key[5]);
			//
			printVector(exHtml);
		}
    	
    }
    void	top_table_3(Vector exHtml){
        

        
		int lines = Integer.parseInt( getParameter("_line_3"));
		if(lines > 0) {
			if(LOG.fa)  LOG.println("class stwork #paramPrintBLK() [top_table_3] : e-Learnig時間割を表示します");
			putParameter("_eLearningSchool",String.valueOf(lines));
			printVector(exHtml);
		}else{
		    putParameter("_line_3","1");
		    putParameter("_eLearningSchool",String.valueOf(1));
			printVector(exHtml);
		    
		}
		
    	
    }
    void	table_3(Vector exHtml){
		/*
		//
		// Student オブジェクトを作成し、講義受講情報（classInfo）を取り出す
		// classInfo は　"kawaba01-101,kawaba01-123" のようなCSV形式の文字列である
		// (teUid)-(aplec_key) の形式で、これが te_aplec_key
		// このうち、aplec_key の部分は（種別、時限、曜日）の番号を繋げたものである
		// 種別（１～３）、時限（０～４）、曜日（０～５）となっている
		//
		String	szDB		= (String) htb.get("_szDB");
		String	stNumber	= (String) htb.get("_stNumber");
		Student	st			= new Student(szDB,stNumber, db);
		String	classInfo	= st.classInfo();
		Csv		cs			= new Csv(classInfo);
		int		max			= cs.size();
		//
		// classInfo を　Csv に変換し、 te_aplec_key を順に取り出して、teUid と aplec_key に分解する
		// そして、aplec_key をキーとして、（aplec_key, teUid) のハッシュを作成する
		// こうすると、時間割の描画の中で te_aplec_key を生成し、該当する時間帯に受講している講義が
		// あるかどうか、このハッシュを引いて求めることができる
		//
		
		for(int i=0; i<max; i++){
			Csv	temp	= new Csv( cs.get(i), "-");
			hs.put(temp.get(1),temp.get(0));
		}
		*/
		Hashtable	hs = (Hashtable)getObject("_infoHash");
		
		// 変数の定義
		String 	[] 	title 		= new String[10];	// 一行分のタイトルを入れる
		String 	[] userID 		= new String[10];	// 
		String 	[] 	aplec_key 	= new String[10];	// 一行分の講義実施キーを入れる
		String 	[] 	lec_key 	= new String[10];	// 一行分の講義定義キーを入れる
		String		shubetsu	= "3";				// e-Learnig
		//
		//
		int gyoMax 			= Integer.parseInt( getParameter("_line_3")); // １オリジン
		//int display_gyo 	= 0;
		for(int gyo=0; gyo < gyoMax  ; gyo++){
			//
			putParameter("_gyo",String.valueOf(gyo));							// 行番号をハッシュに入れる
			//
			int	wdateMax = 6; // １オリジン
			for(int wdate=0; wdate < wdateMax; wdate++){
				//KamokuApRecord rec = kap.fromHash(shubetsu, gyo, wdate);	// この時限（gyo) の曜日(wdate)の科目レコードを得る
				String _aplec_key	= shubetsu + String.valueOf(gyo) + String.valueOf(wdate);
				String _teUid		= (String)hs.get(_aplec_key); // これは間違いではない（ref. classInfoHash()  ）
				if(LOG.fa) LOG.println("class stwork #paramPrintOPT() [table_1] : ハッシュhs を検索した結果　teUid = " + _teUid);
				String flag 		= "_flag" + String.valueOf(wdate);				// flag0 - flag5 を作っておく（出力制御用）
				if(isEmpty(_teUid)){
				    title[wdate] 		= "";
					userID[wdate]		= "";
					aplec_key[wdate]	= "";
					lec_key[wdate]		= "";
					putParameter(flag,"replace");	// ブランク行と差し替えることを指示する（置き換えとリンクを含まないデータに）
				}else{
					KamokuApRecord kar	= new KamokuApRecord( _teUid, _aplec_key,db);
					title[wdate] 		= kar.title();			// 科目名
					userID[wdate]		= _teUid;				// 教員キー
					aplec_key[wdate]	= kar.aplec_key();		// 科目実施キー
					lec_key[wdate]		= kar.lec_key();		// 科目定義キー
					putParameter(flag,"");							// ブランク行と差し替えないことを指示する
				}
			}
			
			putParameter("_title-0",title[0]);			// 講義タイトル文字列
			putParameter("_title-1",title[1]);
			putParameter("_title-2",title[2]);
			putParameter("_title-3",title[3]);
			putParameter("_title-4",title[4]);
			putParameter("_title-5",title[5]);
			//
			if(title[0].length() > 9 ){ putParameter("_sz-0","11"); }else{ putParameter("_sz-0","12"); }	// フォントサイズ
			if(title[1].length() > 9 ){ putParameter("_sz-1","11"); }else{ putParameter("_sz-1","12"); }
			if(title[2].length() > 9 ){ putParameter("_sz-2","11"); }else{ putParameter("_sz-2","12"); }
			if(title[3].length() > 9 ){ putParameter("_sz-3","11"); }else{ putParameter("_sz-3","12"); }
			if(title[4].length() > 9 ){ putParameter("_sz-4","11"); }else{ putParameter("_sz-4","12"); }
			if(title[5].length() > 9 ){ putParameter("_sz-5","11"); }else{ putParameter("_sz-5","12"); }
			//
			putParameter("_teUid-0",userID[0]);			// 教師キー
			putParameter("_teUid-1",userID[1]);
			putParameter("_teUid-2",userID[2]);
			putParameter("_teUid-3",userID[3]);
			putParameter("_teUid-4",userID[4]);
			putParameter("_teUid-5",userID[5]);
			//
			putParameter("_aplec_key-0",aplec_key[0]);	// 講義実施キー
			putParameter("_aplec_key-1",aplec_key[1]);
			putParameter("_aplec_key-2",aplec_key[2]);
			putParameter("_aplec_key-3",aplec_key[3]);
			putParameter("_aplec_key-4",aplec_key[4]);
			putParameter("_aplec_key-5",aplec_key[5]);
			//
			putParameter("_lec_key-0",lec_key[0]);		// 講義定義キー
			putParameter("_lec_key-1",lec_key[1]);
			putParameter("_lec_key-2",lec_key[2]);
			putParameter("_lec_key-3",lec_key[3]);
			putParameter("_lec_key-4",lec_key[4]);
			putParameter("_lec_key-5",lec_key[5]);
			//
			printVector(exHtml);
		}
	}

}

