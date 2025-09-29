
package student;

import java.io.File;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Vector;
import database.Database;
import database.DbConnectionBroker;
import database.KeyGen;
import epml.Exam;
import framework.LOG;
import framework.Param;
import framework.SuperPlayer;
import jbbs.BbsInfoDB;
import jbbs.BbsUtil;
import jbbs.BbsVar;
import kadai.KadaiApRecord;
import kadai.KadaiDEF;
import kadai.KadaiDefRecord;
import kadai.KadaiInfo;
import kamoku.KamokuApRecord;
import kamoku.KamokuSecApRecord;
import kamoku.KamokuSecDefRecord;
import kamoku.KamokuSectionDEF;
import kamoku.SectionRecord;
import note.NOTE;
import refer.ReferenceDEF;
import refer.ReferenceDefRecord;
import tktools.Csv;
import tktools.StringGear;

/**
*
*
	#
	# ##################
	#     StKougi
	# ##################
	#
	<program $student.StKougi>
		<dispatch  html=stKougi.html  number=5700  class=student.StKougi />
		<variable>
		  <receive   NUMBER STAMP GROUP StUID StCLASSINFO StNAME StMAIL StKEITAI  TUID aplec_key lec_key title/>
		  <accept    CMD    kadai_key />
		  <keep      sect_seq totalSections edit_mode />
		  
		  <form      />
		</variable>
	</program> 
*
*
* 変数の説明
*
 * 1.keep変数
 *	sect_seq		--	現在、何番目のセクションを表示しているか（display() でセットして記録）
 *	totalSections	-- 	この科目の総セクション数（display() でセットして記録）
 *	edit_mode		--  編集エリアにあるセクションのセクション実施レコードが記録済み("update")か否か("new")を記録
 *                      display()の中でset_sectionEditArea()を呼んで毎回セットする
 * 2.accept変数
 *	kadai_key		--  課題名をクリックした時に返される課題キー
 *	
 * 3.form変数
 *	autodate_off	--	「日付の自動更新を行なわない」を選択するチェックボックス
 *	secList			--	上段の編集領域のリストボックスで選んだ表示セクションのシーケンス番号（ "1" "2" など）
 *	memo			--	講義メモを書くテキストエリア
*
*
*/
public class StKougi extends SuperPlayer implements BbsVar{

	/**
	 * フォーラム用識別子
	 */
	String			relation;	// forumDB の relation に設定する識別子	
	//
	final String CR = System.getProperty("line.separator");
    final String PS = File.separator;
    //
    //
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

	String			szDB;
	String			stNumber;
	String			teUid;
	String			te_lec_key;
	String			te_aplec_key;
	String			aplec_key;
	
	public	StKougi(){
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

		/*
		 * 学生情報データべーすから基本情報を設定
		 */
		stNumber	= getParameter("_stNumber");
		szDB		= getParameter("_szDB");
		Student	st	= new Student(szDB,stNumber, db);
		putParameter("_kname",st.kname());	// 漢字氏名
		putParameter("_stMail",st.email());	// メールアドレス
		/*
		 * 所属データベースから学生の戻りＵＲＬを設定 
		 */
		String 	url_s	= db.getUrl_s(szDB);
		putParameter("_url_s",url_s);
		//			
		teUid			= getParameter(TUID);
		aplec_key		= getParameter("_aplec_key");
		te_lec_key		= KeyGen.get_te_lec_key2(teUid, getParameter("lec_key"));
		te_aplec_key	= KeyGen.get_te_aplec_key2(teUid, aplec_key);

	}
	void	setRelation(){
		/*
		 * フォーラムのための識別子を作成しシステムハッシュに格納しておく 
		 * ここでの識別子には年度が含まれる ⇒ 年度を越えてアクセスできない
		 * 
		 * 半期の講義時間割りに対応するためシーズン文字列を年度に加えた(2005.3)
		 * // 2月までを同じ年度とする 2022.3
		 */
		relation	=	KeyGen.getRelation(teUid, aplec_key, "b,b,a,a,a,a,a,a,b,b,b,b");
		putParameter(BBS_RELATION, relation);
		
		if(LOG.fa) LOG.println("Kougi #initialize() : relation=" + relation);
	}	
    //
	@Override
	public	String	dispatch(){
		if(LOG.fa) LOG.println("■StKougi #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_EDIT;		// 
		
		if(cmd.equals("COMMENT")){
			/*
			 * コメントシート送信
			 */
			disp_mode	=	DISP_NEW;
			ret			=	"$student.StMail";
			
		}else if(cmd.equals("FAQ")){
			/*
			 * Ｑ＆Ａを見る
			 */
			disp_mode	=	DISP_NEW;
			ret			=	"$student.StFaq";

		}else if(cmd.equals("CABINET")){
			/*
			 * ファイルキャビネット
			 */
			disp_mode	=	DISP_NEW;
			ret			=	"$cabinet.FileCabinet";
			
		}else if(cmd.equals("FORUM")){
			/*
			 *  ユーザーデータベースに登録がなければ初期値を登録しておく
			 */
		    addToBbsInfoDB();
		    /*
		     * オーナーキーとして教師IDをセット
		     */
		    putParameter(BBS_OWNER_KEY, teUid);
		    /*
		     * リレーションキーをセットする 
		     */
		    setRelation();

		    disp_mode	=	DISP_NEW;
			ret			=	"$jbbs.BbsForum";
			
		}else if(cmd.equals("KADAI")){
			/*
			 * 課題を作成する
			 *   レポート課題、ファイル提出課題、試験に分岐する
			 *   
			 * 　guest は受験できないようにする
			 */
			String id = (String)htb.get(StUID);
			//LOG.println("★id=" + id);
			if(id!=null&&id.equals("guest")) {
				
				putParameter(MESSAGE, "★GUESTは受験できません．");
				ret			=	DISPATCH_DEFAULT;
				disp_mode	=	DISP_EDIT;
				
			}else {
				boolean	result	=	kadaiSelect();
				if(!result){
					putParameter(MESSAGE, "★その課題はまだ開けません．");
				}
			}
			
		}else if(cmd.equals("SECT_LIST")){
			/*
			 * リストボックスで指示されたセクションを表示する
			 */
			String seq = 	getParameter("_secList");	// リストボタンのシーケンス番号でデータをセットする
			set_sectionEditArea(seq );
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;		// 

			
		}else if(cmd.equals("BACK")){
			/*
			 * 前のセクションを表示する
			 */
			go_back();
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;		// 
		
		}else if(cmd.equals("FWD")){
			/*
			 * 次のセクションを表示する
			 */
			go_forward();
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;		// 

		}else if(cmd.equals("WRITE")){
			/*
			 * ノートを書き込む
			 */
			wrt_note();
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;		// 
			

		}else if(cmd.equals("SECT_EDIT")){
			/*
			 * セクションの内容を編集領域に上げる
			 */
			String seq = 	getParameter("_sect_seq");	// シーケンス番号でデータをセットする
			set_sectionEditArea(seq);
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;		// 

		}else if(cmd.equals("RETURN")){
			/*
			 * 時間割画面に戻る
			 */
			ret			=	DISPATCH_RETURN;
			disp_mode	=	DISP_NEW;
		
		}else{
			ret			=	DISPATCH_DEFAULT;
			disp_mode	=	DISP_EDIT;		// 
		}
		/*
		 * 次に表示するときあるいはリターンしてきた時の表示モードをセットしてからリターンする
		 */
		putParameter(DISP_KEY,disp_mode);
		return	ret;
	}
	/**
	 * 学生ユーザーをデータベースに登録する
	 * @param st
	 * @param password
	 */
	void	addToBbsInfoDB(){
	    
	    Student	st			=	new Student(szDB,stNumber,db);
	    
	    String		uid		=	st.id();
	    String		pass	=	st.stPasswd();
	    String		name	=	st.kname();
	    String		mail	=	st.email();
	    Hashtable	rec		=	BbsUtil.setInitialData(uid, pass, DIV_STUDENT, name, mail, para);
	    
	    BbsInfoDB	bbsDB	=	new	BbsInfoDB(szDB, broker);
	    bbsDB.insertBbsInfo( uid, rec);
	}	

	
	/** 課題を実行する
	 * 課題を見れるかどうかの設定をしていないので後で追加する
	 * 個々の問題で期間前から見れるかどうか決めることができるか
	 * 原則として期間前は表示しないか
	 * レポートとファイル提出課題は表示し、試験のみ非表示とするか
	 *
	 */
	boolean	kadaiSelect(){
		
		String	kadai_key	= 	getParameter("_kadai_key");
		String	shubetsu	=	getKadaiShubetsu(kadai_key);
		/*
		 * 課題は期間が始まらないと開けない
		 * 開始後はいつでも開ける
		 * 
		 * 課題実施レコードの期間チェックメソッドで開示できるかどうかチェックする
		 */
		if(!isVisible()){
			ret			=	DISPATCH_DEFAULT;
		    disp_mode	=	DISP_EDIT;
		    return	false;
		}
		/*
		 * 課題種別によって処理を分ける
		 * 
		 */
		if(KadaiDefRecord.isReport(shubetsu)){
			/*
			 * レポート課題
			 */
		    disp_mode	=	DISP_NEW;
			ret			=	"$stkadai.StKadaiRepoView";
				
		}else if(KadaiDefRecord.isFile(shubetsu)){
			/*
			 * ファイル提出課題
			 */
			disp_mode	=	DISP_NEW;
			ret			=	"$stkadai.StKadaiFile";
				
		}else if(KadaiDefRecord.isExam(shubetsu)){
		    /*
		     * 試験
		     * 試験後の確認も何時でも開けるが、受験済みかどうかをここでチェックしてから開く
		     * 受験済みでなければ試験実施を開く
		     * 試験実施は、StKadaiStartExam.java でチェックが入る
		     */
			if(KadaiInfo.isExist(db,szDB,te_aplec_key,kadai_key,stNumber)){
				if(KadaiInfo.isHOLD(db,szDB, te_aplec_key, kadai_key, stNumber)){
					/*
					 * HOLD 状態 StKadaiStartExam クラスで再チェック(check())してメッセージが表示される
					 */
					disp_mode	=	DISP_NEW;
					ret			=	"$stkadai.StKadaiStartExam";

				}else{
					/*
					 * 採点後確認
					 * 
					 */
					disp_mode	=	DISP_NEW;
					ret			=	"$stkadai.StKadaiExamView";
				}
					
			}else{
				/*
				 *  試験実施
				 */
				disp_mode	=	DISP_NEW;
				ret			=	"$stkadai.StKadaiStartExam";
			}
		}else if(KadaiDefRecord.isTyping(shubetsu)){
		    /*
		     * ファイル提出と同じ処理だが，採点処理を引き続き行うところが
		     * ちがう
		     */
		    
			disp_mode	=	DISP_NEW;
			ret			=	"$stkadai.StKadaiTyping";
		    
		    
		}else if(KadaiDefRecord.isWriting(shubetsu)){
		    /*
		     * タイプ試験
		     */
		    
			if(KadaiInfo.isExist(db,szDB,te_aplec_key,kadai_key,stNumber)){
				if(KadaiInfo.isHOLD(db,szDB, te_aplec_key, kadai_key, stNumber)){
					/*
					 * HOLD 状態 StKadaiStartExam クラスで再チェック(check())してメッセージが表示される
					 */
					disp_mode	=	DISP_NEW;
					ret			=	"$stkadai.StKadaiStartWriting";

				}else{
					/*
					 * 採点後確認
					 * 
					 */
					disp_mode	=	DISP_NEW;
					ret			=	"$stkadai.StKadaiWritingView";
				}
					
			}else{
				/*
				 *  試験実施
				 */
				disp_mode	=	DISP_NEW;
				ret			=	"$stkadai.StKadaiStartWriting";
			}
		    
		}
		
		return	true;
	}
	/**
	 * 課題を表示してもよいかどうか判定する
	 */
	boolean	isVisible(){
	
	    boolean		retcode		=	false;
		String			kadai_key	= 	getParameter("_kadai_key");
		KadaiApRecord	kar			= new KadaiApRecord(te_aplec_key,kadai_key,db);
		/*
		 * 課題実施期間が未設定か
		 */
		if(kar.isEmptyRecord()){
		    retcode	=	false;
		}else{
		    /*
		     * 開始日を過ぎているか
		     */
		    if(kar.isStarted()){
		        retcode	=	true;
		    }else{
		        retcode	=	false;
		    }
		}
	    if(LOG.fa) LOG.println("■ StKougi #isVisible() is " + retcode);
	    return	retcode;

	}
	
	/** 課題種別を返す */
	String	getKadaiShubetsu(String key){
		//
		KadaiDefRecord	rec	= new KadaiDefRecord(te_lec_key,key,db);
		String			shu	= rec.shubetsu();	// 種別
		return	shu;
	}
	/**
	 * ノートをデータベースに書き出す
	 * 
	 */
	void wrt_note(){
		/*
		 * セクションのシーケンス番号からセクションレコードを得て、
		 * レコードからせクションキーを得る
		 * フォームの入力データをノートレコードに登録する
		 */
	    int	 				seqNo		=	Integer.parseInt(getParameter("_sect_seq")) - 1;// zero オリジンに直す
		KamokuSectionDEF 	ksd			=	new KamokuSectionDEF(te_lec_key, db);
		KamokuSecDefRecord	ksdrec		=	ksd.get(seqNo);
		String 				sect_key	=	ksdrec.sect_key();
		String				memo		=	getParameter("_memo");
		/*
		 *  空のNOTEオブジェクトを作成し、キー（学籍番号、実施講義キー,セクション番号）を埋め込む
		 *  検索をかけてこのレコードがＤＢ中に存在するかどうか確かめる．存在するとupdate,でないとcreate を実行する
		 */
		NOTE 	note		= 	new NOTE(szDB,db);
		note.add_keys(stNumber,te_aplec_key,sect_key);
		int n	= note.read_NOTE();	// 存在するかどうか
		if(n==0){
			note.add_data(memo);	// 追加
			note.insert_NOTE();
		}else{
			note.set_note(memo);	// 更新
			note.update_NOTE();
		}
	}
	//
	//
	//  ひとつ先へ
	//
	void go_back(){
		String total = getParameter("_totalSections");
		String seq	 = getParameter("_sect_seq");
		//
		int	max	= Integer.parseInt( total );
		int	cur	= Integer.parseInt( seq );
		if(cur > 1){
			--cur;
		}else{
			cur = max;
		}
		set_sectionEditArea(String.valueOf(cur));
		return;
	}
	//
	//　ひとつ前へ
	//
	void go_forward(){
		String total = getParameter("_totalSections");
		String seq	 = getParameter("_sect_seq");
		//
		int	max	= Integer.parseInt( total );
		int	cur	= Integer.parseInt( seq );
		if(cur < max){
			++cur;
		}else{
			cur = 1;
		}
		set_sectionEditArea(String.valueOf(cur));
		return;
	}
	
	/* **************************************************************************
	 * 	getDisplaySeqnumber() メソッドを改訂した 2024.3.2
	 * 		処理をメソッドにして、本体メソッドを簡単にした。
	 * 		旧版はgetDisplaySeqnumber2() として保存している。
	 *   
	 *****************************************************************************/
	// 今日表示するセクションのシーケンス番号を返す（１オリジン）
	// 日付が求まらない時は -1 を返す
	String	getDisplaySeqnumber(){
		//
		
		int 	yy = Integer.parseInt(getNendo()); // 講義の実施年度を得る
		int 	ajustNumber 	= 0;				// 年度を年に変換するための調整値（０か１）
		int 	currentMonth 	= -1;				// 比較する月の値（1-12）
		String 	returnValue 	= "-1";				// 戻り値
		
		// セクション定義クラスを作る（シーケンス順にセクションキーを得るため）
		int max = sectionSize();
				
		for(int	index=0; index<max; index++){
			
			SectionRecord record = secRecord(index);	// セクションレコードを得る
			int recordMonth = record.startMonth();	// レコードの月の値
			
			// 前回の月よりも今回の月が小さければ、ajustNumberを1にする
			if(currentMonth>recordMonth) {
				ajustNumber = 1;
			}
			currentMonth = recordMonth;	// 比較する月の値を更新する
			
			LocalDate recordDate = record.startDay(yy + ajustNumber); 	// レコードの日付
			LocalDate today 	 = LocalDate.now();							// 今日の日付
			
			// 今日の日付と同じかより大きければ、それが表示する日付
			if(today.equals(recordDate) || today.isBefore(recordDate)) {
				returnValue = String.valueOf(index + 1);
				break;
			}
		}
		return	returnValue;
	}	
////////////////////////////////////////////////////////////////////////////////////////////////////////
/*
	セクションキーと課題定義キーは定義と実施では同じものを使う．
		上位に付加するのが講義キーか講義実施キーかで区別できるから．
	------------------------------------------------------------
	create table key_gen (
		teUid           CHAR(8)  PRIMARY KEY,			教員のユーザＩＤ
		lec             CHAR(3)  DEFAULT '001',			講義の定義キー
		aplec           CHAR(3)  DEFAULT '001',			講義の実施キー
		sect            CHAR(5)  DEFAULT '00001',		セクションキー
		kadai           CHAR(6)  DEFAULT '000001',		課題キー
		ref             CHAR(6)  DEFAULT '000001',		資料キー
		faq             CHAR(6)  DEFAULT '000001'
	); 	
*/
	/////////////////////////////////////////////////////////////////
	// 教師idを返す
	private String teacher_id() {
		return getParameter("_teUid");
	}
	// 講義定義キーを返す
	private String lec_key() {
		return getParameter("_lec_key");
	}
	// 講義実施キーを返す
	private String aplec_key() {
		return  getParameter("_aplec_key");
	}	
	// 教師の講義定義キーを返す
	private String teUid_lec_key() {
		return teacher_id() + "-" + lec_key();
	}
	// 教師の講義実施キーを返す
	private String teUid_aplec_key() {
		return  teacher_id() + "-" + aplec_key();
	}
	// index番目のセクションキーを返す
	private String section_key(int index) {
		return kamoku_sec_def().sect_key(index);
	}
	
	//////////////////////////////////////////////////////////////////
	// 科目の実施レコードを返す
	private KamokuApRecord kamoku_ap_record() {
		return new	KamokuApRecord( teacher_id(), aplec_key(), db );
	}
	// セクション定義レコードを取得する
	private KamokuSectionDEF kamoku_sec_def() {
		return new KamokuSectionDEF(teUid_lec_key() ,db);
	}
	// 科目の中のindex番目のセクションレコードを返す
	private SectionRecord secRecord(int index) {
		return new	KamokuSecApRecord(db, teUid_aplec_key(), section_key(index)).getRecord();
	}
	
	////////////////////////////////////////////////////////////////////
	// 実施年度を返す
	private String getNendo() {
		return	kamoku_ap_record().getYear();
	}
	// セクションの数を返す
	private int sectionSize() {
		return kamoku_sec_def().size();		
	}
	// セクションレコードの開始月を得る
	private int startMonth(SectionRecord record) {
		return Integer.parseInt(record.getStart_month().strip());
	}
	private int startDay(SectionRecord record) {
		return Integer.parseInt(record.getStart_day().strip());
	}
	// セクションレコード開始日を得る
	
	// 
	/////////////////////////////////////////////////////////////////////
	
	/*
	//
	// 今日表示するセクションのシーケンス番号を返す（１オリジン）
	// 日付が求まらない時は -1 を返す
	String	getDisplaySeqnumber2(){
		//
		// 講義の実施年度を得る（課目実施レコードから）
		KamokuApRecord	kar		=	new	KamokuApRecord( getParameter("_teUid"), getParameter("_aplec_key"), db );
		String			yyyy	=	kar.getYear();	// 4桁の半角数字文字列または""を返す
		if(yyyy.length()==0){
			// 年度が得られない場合は -1 を返す
			return	"-1";
		}
		// セクション定義クラスを作る（シーケンス順にセクションキーを得るため）
		KamokuSectionDEF	ksDef			=	new KamokuSectionDEF(te_lec_key,db);
		//
		// 残りのセクション実施レコードについてシーケンスエンドになるまで以下を繰り返す
		// 時間以下を含まない今日の GregorianCalendar を得る(０時０分)
		GregorianCalendar	today_temp		=	new GregorianCalendar();
		int					yy				=	today_temp.get(Calendar.YEAR);
		int					mm				=	today_temp.get(Calendar.MONTH);
		int					dd				=	today_temp.get(Calendar.DAY_OF_MONTH);
		GregorianCalendar	today			=	new GregorianCalendar(yy,mm,dd);		
		//
		int					max				=	ksDef.size();
		for(int	k=0; k<max; k++){
			// セクションのシーケンス番号から次のセクションキーを得て
			// 日付をセットするためにセクション実施レコードを取得する
			String				sectKey		=	ksDef.sect_key(k);	// ゼロオリジンなので
			KamokuSecApRecord	secApRec	=	new	KamokuSecApRecord(te_aplec_key,sectKey,db);
			GregorianCalendar	date		=	secApRec.startDay(yyyy);
			// 日付が得られなければ -1 を返す
			if(date	== null)	return	"-1";
			//
			// 日付が今日より後か、今日と同じであれば日付のレコードのシーケンス番号を返す
			if(today.before(date))	return	String.valueOf(k + 1);
			if(today.equals(date))	return	String.valueOf(k + 1);
		}
		return	"-1";
	}
	*/
	
	//  シーケンス番号の値を渡してセクション編集領域のデータをセットする
	//  シーケンス番号は１オリジンなので注意
	//
	void set_sectionEditArea(String seq){
		
		//-------------------------------------------------------------------------------------
		putParameter("sect_seq", seq); 			// 現在のシーケンス番号を覚えておく
		//-------------------------------------------------------------------------------------
		
		KamokuSectionDEF ksd	= new KamokuSectionDEF(te_lec_key,db);

		if(LOG.fa){
			LOG.println("  te_lec_key =" + te_lec_key);
		}

		
		int seqNo				= Integer.parseInt(seq) -1 ;	// seq は１オリジン,seqNo は０オリジン
		KamokuSecDefRecord rec	= ksd.get(seqNo);	
		/*
		 *  セクション定義レコード（必ずある）
		 */
		putParameter("sect_key"	   , rec.sect_key());// セクションキー
		putParameter("subject"	   , rec.title());		// タイトル
		
		////////////////////////////////////////////////////////////////
		
//		putParameter("contentPlan" , rec.content());	// 内容
		
		/*
		 * 講義内容の説明をPMLで記述するので、表示用のHTMLに変換する
		 * PML以外のテキストは従来どおり表示される
		 */
		String HTML_text = pml_to_Html(rec.content().trim());
		putParameter("contentPlan" ,HTML_text);			// 内容
				
		
		putParameter("todo"        , rec.note());		// 備考
		/*
		 *  セクションキーとそれに付属する資料、課題のキーリスト
		 */
		String	sect_key = rec.sect_key();
		putParameter("sect_key"		, sect_key);		// セクションキー
		putObject	("ref_list"		, rec.refKeys());	// 資料リスト Csv（ 先頭に _ をつけないとデバッグでは表示されない）
		putObject	("kadai_list"	, rec.kadaiKeys());	// 課題リスト Csv（ 先頭に _ をつけないとデバッグでは表示されない）
		/*
		 * セクション実施レコード（空の場合がある）
		 * 年度や単位など。空の場合がある。
		 * 
		 */
		KamokuSecApRecord ksar	= new KamokuSecApRecord ( te_aplec_key, sect_key, db);
		/*
		 *  シーケンス番号
		 */
		putParameter("_no",seq);
		putParameter("_k", seq);
		/*
		 * セクション実施レコードの表示データ
		 */
		String yyyy = getParameter("_yyyy");
		putParameter("_dateString" ,ksar.dateString(yyyy));	// 実施日。○月○日（□）～　 ○月○日（□）　の形
		/*
		 *  学生のノート
		 */
		NOTE	nt		= new NOTE(szDB,db);
		nt.add_keys(stNumber,te_aplec_key,sect_key);
		int 	cnt		= nt.read_NOTE();	
		String  note	= "";
		if(cnt>0){
			note	= nt.note();
		} 
		putParameter("memo",note);
	}
	
	/** ****************************************************************
	 * 講義画面の記述にPMLを使えるように修正した
	 * PMLをパースしたHTMLを返す　（2023.10.1 追加）
	 * @param lecture_content	講義内容の説明テキスト
	 * @return			同 HTML
	 ********************************************************************/
	public	String	pml_to_Html(String lecture_content){

		/*
		 * para.getEmlConfPath()は、/system/config/pc.conf の絶対パスを返す
		 */
		Exam	exam		= 	new Exam(lecture_content ,para.getEmlConfPath());
		
		/*
		 * グラフィクスのパスを設定する
		 * ユーザーごとのグラフィックスデータディレクトリを設定して使うことにした
		 * この設定がHTMLに反映されるよう、ディレクトリ情報を設定する
		 *
		 * setImgPath() はグラフィックスへの完全URL．
		 *    /user/(teUid)/secImg/
		 * 
		 * setImgDestinationPath()はグラフィックスの絶対ディレクトリパス．(おそらく使っていない）
		 *   d:\\pc_data\\user\\(teUid)\\secImg\\ 
		 */
		exam.setImgPath( "/user/" + teUid + "/file/secImg/" );	// imgURL
		exam.setImgDestinationPath( para.get("homedir") + teUid + "\\secImg\\" ); // おそらく使っていない
		/*
		 * 資料データではないという設定をしておく
		 * これにより、ファイルへのリンクがイメージへのリンクと同じになる 
		 */
		exam.setNonHtmlFlag();
		/*
		 * HTMLを生成し、特殊文字を元の記号に戻して返す
		 * Exam#createHtml()メソッドで生成する
		 */
		String html = exam.createHtml();
		return	replace_special_words(html);
	}	
	/*
	 * &lt; &gt; &quot; &rsquo; &yen; → <  >  "  ' \ に変換する
	 */
	public String replace_special_words(String text) {
		return text.replace("&lt;", "<")
					.replace("&gt;", ">")
					.replace("&quot;", "\"")
					.replace("&rsquo;", "\'")
					.replace("&yen;","\\");
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
		if(LOG.fa)	LOG.println("■ StKougi #display(boolean editmode)");
		
		if(!editmode){
			/*
			 *  日付の自動設定を指示するので"-1"をセット
			 */
			putParameter("_sect_seq","-1" );
			putParameter(MESSAGE,"");
			
		}
		//db.MembersInfo(teUid,htb);
		/*
		  db.MembersInfo(teUid,htb)　⇒　以下の情報が htb に追加される
		  	
		    putParameter("_user_id",rs.getString("user_id"));
			putParameter("_user_mail",rs.getString("user_mail"));
   			putParameter("_user_passwd",rs.getString("user_passwd"));
   			putParameter("_user_active",rs.getString("user_active"));
   			putParameter("_user_name",rs.getString("user_name"));
   			putParameter("_user_hurigana",rs.getString("user_hurigana"));
		*/
		KamokuApRecord kap	= new KamokuApRecord(teUid, aplec_key, db);
		//
		// 実施年度を得る
		String yyyy = kap.yyyy();
		if(isEmpty(yyyy)){
			Calendar cal = new GregorianCalendar();
   			int	year 	 = cal.get(Calendar.YEAR);
			int month 	 = cal.get(Calendar.MONTH);  // 0=Jan, 1=Feb, ...
			if(month <= 1) year--; // 2月までを同じ年度とする
			//
			yyyy = String.valueOf(year);
		}
		putParameter("_yyyy",yyyy);			// 年度
		putParameter("_term",kap.term());	// 期
		putParameter("_unit",kap.unit());	// 単位
		//
		// セクションの数を調べる．（必ず１つはある）
		KamokuSectionDEF ksd	= new KamokuSectionDEF(te_lec_key,db);
		String total = String.valueOf( ksd.size() );
		putParameter("_totalSections",total);				// 総セクション数
		
		/*
		 * 自動設定を要求する＝ "-1" 
		 */
		String seq   = getParameter( "_sect_seq");
		if(seq.equals("-1")){
			/*
			 * 今日表示するセクションのシーケンス番号を返す（１オリジン）
			 * 日付が求まらない時は -1 を返す
			 */
			String	sqStr	=	getDisplaySeqnumber();
			if(sqStr.equals("-1")){
				putParameter("_sect_seq","1");
			}else{
				putParameter("_sect_seq",sqStr);
			}
		}
		set_sectionEditArea(getParameter( "_sect_seq"));
		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}
	/**
	 * 
	 */
	@Override
	public void write(String key,Vector exHtml){
		if(LOG.fa) LOG.println("■ StKougi #write(String key,Vector exHtml)");
		
		if(key.equals("sect_optionList")){
			//	講義画面で上段のオプションリストを表示する
			sect_optionList(exHtml);
			
		}else if(key.equals("edit_ref_list")){
			// 講義画面で上段のセクションリスト中の資料リストを表示する
			edit_ref_list(exHtml);
			
		}else if(key.equals("edit_kadai_list")){
			// 講義画面で上段のセクションリスト中の課題リストを表示する
			edit_kadai_list(exHtml);
		
		}else if(key.equals("sect_list")){
			// 講義画面で下段のセクションリストを表示する
			sect_list(exHtml);
		
		}else if(key.equals("sect_ref_list")){
			// 講義画面で下段のセクションリスト中の資料リストを表示する
			sect_ref_list(exHtml);
		
		}else if(key.equals("sect_kadai_list")){
			// 講義画面で下段のセクションリスト中の課題リストを表示する
			sect_kadai_list(exHtml);
		}
	}
	/**
	 * 
	 * @param exHtml
	 */
	void	sect_optionList(Vector exHtml){
		if(LOG.fa) LOG.println("■ Kougi #sect_optionList()");
		
		String total = getParameter("_totalSections");
		String seq	 = getParameter("_sect_seq");
		//
		if( total == null){
			LOG.errStop( out,"総セクション数が設定されていない : paramPrintOPT() key = sect_optionList" );
			return;
		}
		int kei		 = Integer.parseInt(total);
		for(int i=0; i<kei; i++){
			// value
			String opt = String.valueOf(i+1);
			putParameter("_s",opt);
			// selected
			if(opt.equals(seq)){
				putParameter("_sel","selected");
			}else{
				putParameter("_sel","");
			}
			// ラベル
			putParameter("_ss",getSStype(i+1));
			printVector(exHtml);
		}
	}
	/**
	 * 
	 * @param exHtml
	 */
	void	edit_ref_list(Vector exHtml){
		if(LOG.fa) LOG.println("■ Kougi #edit_ref_list()");
		disp_refDefList(exHtml,"_x","_x_refIcon","_x_ref","_x_ref_url");
		
	}
	/**
	 * 
	 * @param exHtml
	 */
	void	edit_kadai_list(Vector exHtml){
		if(LOG.fa) LOG.println("■ Kougi #edit_kadai_list()");
		disp_kadaiDefList(exHtml,"_y","_y_kadaiIcon","_y_kadai");

	}
	/**
	 * 
	 * @param exHtml
	 */
	void	sect_list(Vector exHtml){

		String yyyy				=  	getParameter("_yyyy");		// 空白でも構わない
		if(!KeyGen.is_te_lec_key(te_lec_key)){
			LOG.errStop( out,"科目キーがない.または異常(001) :" + te_lec_key + ":" );
			return;
		}
		// 学生のノートを取り出しておくためのキー
		String stNumber		= getParameter("_stNumber");
		String teUid		= getParameter("_teUid");
		String aplec_key	= getParameter("_aplec_key");
		String te_aplec_key	= KeyGen.get_te_aplec_key2(teUid,aplec_key);
		//
		// ノート全件を取り出す（０件の可能性あり）
		Vector	records		= new Vector(20,10);
		String  szDB		= getParameter("_szDB");
		NOTE	note		= new NOTE(szDB,db);
		int		noteCounts	= note.read_NOTE_all(stNumber,te_aplec_key,records);
		//
		// ノートをセクションキーで引けるようにハッシュにいれておく
		Hashtable noteHash	= new Hashtable(60);
		if(noteCounts > 0){
			if(LOG.fa) LOG.println("class stwork #parmPrintOPT() [sect_list]: ノートをハッシュに入れます");
			for(int	i=0; i<noteCounts; i++){
				if(LOG.fa) LOG.println("stwork#paramPrintOPT() [sec_list]: -- loop [" + i + "] 回目 ");
				Vector	vrec		= (Vector)records.get(i);
				String	sect_key	= (String)vrec.get(NOTE.SECT_KEY);
				String	body		= (String)vrec.get(NOTE.NOTES);
				noteHash.put(sect_key,body);
			}
		}
		
		KamokuSectionDEF ksd	= new KamokuSectionDEF(te_lec_key, db);
		int	n	= ksd.size();
		for(int i=0; i<n; i++){
			// セクション定義レコード
			KamokuSecDefRecord	 	ksdrec		= ksd.get(i);
			String sect_key		= 	ksdrec.sect_key();
			putParameter("_seq",String.valueOf(i+1));	// 編集領域に転記するときに要るのでシーケンス番号をhtmlに埋め込んでおく
			putParameter("_sect_key",sect_key);			// 後の資料や課題表示で必要になるのでここで入れておく（再帰処理で値は残る）
			//
			// セクション実施レコード
			KamokuSecApRecord		ksap_rec	= new KamokuSecApRecord(te_aplec_key, sect_key, db);
			//
			putParameter("_k"			,String.valueOf(i+1) );					// 編集用アイコンの番号、１オリジン
			putParameter("_e"				,StringGear.get00type(i+1));			// 第○回の表示用
			putParameter("_sect_subject"	,(ksdrec.title()).trim() );	// セクションタイトル
			
			
			/*
			 * 講義内容の説明をPMLで記述するので、表示用のHTMLに変換する
			 * PML以外のテキストは従来どおり表示される
			 */
			String HTML_text = pml_to_Html(ksdrec.content());
			putParameter("_sect_content"	,HTML_text );			// セクション内容
			
			
			putParameter("_sect_todo"	,(ksdrec.note()).trim() );				// 備考・ToDo（学生への注意・指示など）
			//
			putParameter("_dateString"	,ksap_rec.dateString(yyyy));			// 実施日。○月○日（□）～　 ○月○日（□）　の形
			//
			// ノート
			String nt	= (String) noteHash.get(sect_key);
			if(nt==null)	nt	= "";
			putParameter("_sect_memo"	,nt );	// 学生のノート
			//
			printVector(exHtml);
		}		
	}
	void	sect_ref_list(Vector exHtml){
		if(LOG.fa) LOG.println("■ Kougi #sect_ref_list()");
		disp_refDefList(exHtml,"_k","_k_refIcon","_k_ref","_k_ref_url");
		
	}
	void	sect_kadai_list(Vector exHtml){
		if(LOG.fa) LOG.println("■ Kougi #sect_kadai_list()");
		disp_kadaiDefList(exHtml,"_m","_m_kadaiIcon","_m_kadai");
		
	}
	//
	// 資料リストを表示する
	//
	//  teUid  と lec_key は必須
	//
	void disp_refDefList(Vector exHtml,String s1,String s2,String s3,String s4){

		// この科目の資料レコードを全て得る
		String sect_key		= getParameter("sect_key");
		ReferenceDEF refDef	= new ReferenceDEF(te_lec_key, db);	
		//
		// このセクションに属する全ての資料キーのリスト refkeys を得る
		KamokuSecDefRecord	ksdRec	= new  KamokuSecDefRecord(te_lec_key, sect_key,db);
		Csv refkeys = ksdRec.refKeys();
		
		/*
		 * シーケンスでソート済みのレコード
		 */
		int 	n 		= 	refkeys.size();// 資料キーの数
		if(n==0){
			putParameter(s1 , "-");			// 項目番号表示用
			putParameter(s2 , "spacer.gif");
			putParameter(s3 , "&nbsp;");
			putParameter(s4 , "&nbsp;");
			//
			printVector(exHtml);
		}else{
			//
			Vector aliveRefkeys = new Vector(10,10);
			//
			for(int i=0; i<n; i++){ // 全てのキーから
				/*
				 * 資料キーでハッシュを検索するが、古いレコードでは削除されたキーもふくまれているので
				 * null が返されることがある。
				 */
				ReferenceDefRecord	 rdrec	= refDef.fromKeyHash( te_lec_key, refkeys.get(i) );
				if(rdrec!=null){
					putParameter(s1, String.valueOf(i+1) + ")" );	// 項目番号表示用
					putParameter(s2, rdrec.refIcon());				// アイコンデータ名
					putParameter(s3, rdrec.title());  				// 資料名 
					putParameter("_iconName",rdrec.shubetsuName());
					if(  !isEmpty(rdrec.url()) ){
						putParameter(s4, rdrec.url());  				// URL
						putParameter("refFlag","");					// URL なしと置き換えをしない
					}else{
						putParameter("refFlag","replace");			// URL なしと置き換えをする
						putParameter(s4, "#");
					}
					
					aliveRefkeys.add( rdrec.ref_key() );        // 有効なキーは記憶しておく
					printVector(exHtml);
				}
			}
		}
	}
    //
	// 課題リストを表示する
	//
	//
	void disp_kadaiDefList(Vector exHtml,String s1,String s2,String s3){

		// この科目の課題レコードを全て得る
		String sect_key		= getParameter("sect_key");
		KadaiDEF kadaiDef	= new KadaiDEF(te_lec_key, db);	
		//
		// このセクションに属する全ての課題キーのリスト kadaikeys を得る
		KamokuSecDefRecord	ksdRec	= new  KamokuSecDefRecord(te_lec_key, sect_key,db);
		Csv kadaikeys = ksdRec.kadaiKeys();
		int 	n 		= 	kadaikeys.size();// 課題キーの数
		putParameter("kadaiEmptyFlag" ,"");

		if(n==0){
			putParameter(s1 , "-");			// 項目番号表示用
			putParameter(s2 , "spacer.gif");
			putParameter(s3 , "&nbsp;");
			putParameter("_kubun","0");		// = spacer.gif
			/*
			 * 課題の種別アイコンにリンクが付いているので、データがない場合に不都合．
			 * リンクの無い行データに差し替えるためのタグ
			 */
			putParameter("kadaiEmptyFlag" ,"ON");			printVector(exHtml);
		}else{
			Vector aliveKadaikeys = new Vector(10,10);
			for(int i=0; i<n; i++){ // 全てのキーから
				/*
				 * 課題キーでハッシュを検索するが、古いレコードでは削除されたキーもふくまれているので
				 * null が返されることがある。
				 */
				KadaiDefRecord	 kdrec	= kadaiDef.fromKeyHash( te_lec_key, kadaikeys.get(i) ); // キーでハッシュを検索する
				if(kdrec!=null){
					putParameter(s1, String.valueOf(i+1) + ")" );		// 項目番号表示用
					putParameter(s2, kdrec.kadaiIcon());					// アイコンデータ名
					putParameter(s3, kdrec.title());  					// 課題名
					putParameter("_kadai_key",kadaikeys.get(i));					// 課題キー

					putParameter("_disposalMsg",kdrec.shubetsuName2());	// 種別名
					putParameter("_kadai_title",kdrec.title() );

					//////////////////// 提出状況の可視化 //////////////////////////////
					String	disposalStr	=	getDisposal(kadaikeys.get(i));
					putParameter("_kubun",disposalStr);
					////////////////////////////////////////////////////////////////////
					
					aliveKadaikeys.add( kdrec.kadai_key() );        // 有効なキーは記憶しておく
					printVector(exHtml);					
				}
			}
		}
	}
	/**
	 * 課題の提出状況を得る
	 * @param kadai_key	課題キー
	 * @return				提出状況を表す文字列
	 */
	String	getDisposal(String	kadai_key){
		
		String	disposal;
		KadaiInfo kdf	= new KadaiInfo(szDB,db);
		kdf.set_keys(stNumber,te_aplec_key,kadai_key);
		int cnt = kdf.read_KadaiInfo();
		if(cnt==0){
			disposal	= KadaiInfo.NOTYET;
		}else{
			disposal	= kdf.saiten_flag();
		}
		return	disposal;	
	}
}