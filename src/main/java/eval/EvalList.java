/*
課題提出一覧表

*/
package eval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.Vector;
import cabinet.CBdatabase;
import cabinet.CBvar;
import database.Database;
import database.DbConnectionBroker;
import database.KeyGen;
import framework.Cp932;
import framework.LOG;
import framework.Param;
import framework.SuperPlayer;
import kadai.Answer;
import kadai.AnswerRecord;
import kadai.KadaiApRecord;
import kadai.KadaiDefRecord;
import kadai.KadaiInfo;
import kamoku.KamokuApRecord;
import meibo.Meibo;
import student.Student;
import tktools.Csv;
import tktools.FileGear;
import tktools.Files;
/**
 *
 *
 	#
	# ##################
	#     EvalList
	# ##################
	#
	<program $eval.EvalList>
		<dispatch  html=evalList.html  number=1510  class=eval.EvalList  />
		<variable>
		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA title lec_key aplec_key kadai_key/>
		  <accept    CMD    UPLODE  stNum/>
		  <keep      kadai_title start end/>
		  
		  <form      sortMode />
		</variable>
	</program> 
 *
 *
 * 変数の説明
 *
 * 1. receive 
 * 2. accept
 *       stNum			提出レコードの削除を選択したとき得られる対象学生の学籍番号
 * 　　　　　　　　　　 郵便マークの削除時にも
 * 3. keep
 *       kadai_title	課題名（display()にて保存される）
 *       start			課題提出期間の始まり日時（同上）
 *       end			課題提出期間の終了日時（同上）
 * 4. form
 *       sortMode	並べ替えのモード変更（名簿順、提出順）
 *
 *
 */
public class EvalList extends SuperPlayer  implements CBvar{
	//
	final String PS = File.separator;	
	final String CR = System.getProperty("line.separator");

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
	

	String 		szDB;
	String 		teUid;
	String		lec_key;
	String 		kadai_key;

	String 		aplec_key;
	String 		te_aplec_key;
	String		te_lec_key;
	
	String		teachersName;
	String		teachersMail;

	String		code;
	
	public	EvalList(){
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

		szDB			=	getParameter(GROUP);
		teUid    		= 	getParameter(TUID);
			
		kadai_key		=	getParameter("kadai_key");
		lec_key			=	getParameter("lec_key");
		aplec_key		=	getParameter("aplec_key");
		te_lec_key		=	KeyGen.get_te_lec_key2(teUid, lec_key);
		te_aplec_key	=	KeyGen.get_te_aplec_key2(teUid, aplec_key);
		
		/*
		 * 教師の名前とメールアドレスを得る
		 */
		teUid       	= 	getParameter(TUID);
		teachersName	=	getParameter(TNAME);
		teachersMail    =	getParameter(TMAIL);		
		
		// 解答ファイルのあるディレクトリ名
		code =	para.kadaiAnsDir(teUid,aplec_key, kadai_key);
		putParameter("code", code);
		
	}

	@Override
	public	String	dispatch(){
		if(LOG.fa) LOG.outHash(htb,"■Sample #dispatch()");
		if(LOG.fa) LOG.println("■Sample #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		//
		
		if(cmd.equals("EVAL")){
			/* 
			 * 採点画面へ
			 */ 
			setEvalType();
			
		}else if(cmd.equals("DELINFO")){
			/*
			 * 特定の学生の課題提出情報の削除＋提出した課題ファイルも削除
			 */
			delKadaiFile();
			delInfo();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示

		}else if(cmd.equals("NOTICE")){
			/*
			 * 採点結果を通知する
			 */
			Announce2 ann2= new Announce2(out,htb,szDB,para,db);	// 2003.3.14 スレッド化した
			ann2.start();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示
			
		}else if(cmd.equals("CSV")){
			/*
			 * 採点結果をファイル化してファイルキャビネットに登録する
			 */
			evalData();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示
			
		}else if(cmd.equals("ZIP")){
			/*
			 * 提出ファイルをzipにしてファイルキャビネットに登録する
			 */
			getFiles();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示
			
		}else if(cmd.equals("SORT")){
			/*
			 * ソートする
			 */
			sortTable();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示

		}else if(cmd.equals("MARK")){
			/*
			 * ソートする
			 */
			deleteMark();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示

		}else if(cmd.equals("CABINET")){
			/*
			 * ファイルキャビネットを開く
			 */
			sortTable();
			disp_mode	=	DISP_NEW;
			ret			=	"$cabinet.FileCabinet";

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
			 * エラー
			 */
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
		}
		//
	   // ソート状態を設定
		String	mode	= getParameter("_sortMode");		// 
		putParameter("_sort",mode);							// _sortMode は変更を選択するリストボックス、_sortMode は _sortに値を記憶しておく
		//												// 具体的な処理は paramPrintOPT() で行う
		if(mode.equals("ON")){
			putParameter("_no_dateTime","");					// リストボックスの表示を制御（ここでやっておかないと間に合わない）
			putParameter("_yes_dateTime","selected");
		}else{
			putParameter("_no_dateTime","selected");
			putParameter("_yes_dateTime","");
		}
		/*
		 * 次に表示するときあるいはリターンしてきた時の表示モードをセットしてからリターンする
		 */
		putParameter(DISP_KEY,disp_mode);
		return	ret;	
	}
	/**
	 * 通知メール送信済未済とする
	 */
	void	deleteMark(){
	    
	    String			stNum		=	getParameter("stNum");
	    Answer			ans			=	new	Answer(htb,para,db);
	    Hashtable		ht			=	ans.getHash();
		AnswerRecord 	arec 		= 	(AnswerRecord)ht.get(stNum);
		/*
		 * クリアする
		 */
		arec.evalSetOff();
		
		String  		kadaiPath   = 	para.kadaiAnsDir(teUid,aplec_key,kadai_key) + PS;
		String 			asPath 		= 	kadaiPath + stNum + ".ans";	// 課題ファイル名
		ans.writeAnsRec(asPath,arec);					// レコードをファイルに書き込み			    
	    
	    
	}
	/**
	 * 提出されている解答の総数を得る
	 * ファイル数をカウントする
	 * @return
	 */
	int	countAnswers(){
		String  	filePath 	= para.kadaiAnsDir(teUid,aplec_key,kadai_key);
		Files		fl			= new Files(filePath);
		if(LOG.fa){
			LOG.println("★EvalList#countAnswers()");
			LOG.println("  filePath="+filePath);
			LOG.println("  conrnts ="+fl.size());
			
		}
		return		fl.size();
	}
	
	/**
	 * 課題採点画面を選択
	 */
	public	void setEvalType(){
		/*
		 * 採点プログラムは必ず１件以上の解答があるという前提で動作するので
		 * 解答があるかどうか調べて、ない場合は起動しない。
		 */
		if(countAnswers()==0){
			putParameter(MESSAGE, "★解答はありません");
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
			return;
		}
		/*
		 * 種別を得て起動プログラムを選択する
		 */
		String	shubetst	= getShubetsu();								// 	
		if(shubetst.equals(KadaiDefRecord.EXAM)){
			disp_mode	=	DISP_NEW;
			ret			=	"$eval.EvalExam";			

		}else if(shubetst.equals(KadaiDefRecord.FILE)){
			disp_mode	=	DISP_NEW;
			ret			=	"$eval.EvalFile";			

		}else if(shubetst.equals(KadaiDefRecord.TYPE)){
			/*
			 * タイピング練習は提出時に自動採点なので採点の必要はないが
			 * 修正目的で，ファイル提出課題と同様な扱いとする
			 */
		    disp_mode	=	DISP_NEW;
			ret			=	"$eval.EvalFile";			

		}else if(shubetst.equals(KadaiDefRecord.REPO)){
		    /*
		     * レポート
		     */
		    disp_mode	=	DISP_NEW;
			ret			=	"$eval.EvalWeb";			

		}
		if(LOG.fa){
			LOG.println("★EvalList#setEvalType()");
			LOG.println("  shubetst  ="+shubetst);
			LOG.println("  ret       ="+ret);
			
		}		
	}
	String	getShubetsu(){
		String	te_lec_key	= KeyGen.get_te_lec_key(htb);
		String	kadai_key	= getParameter("_kadai_key");
		KadaiDefRecord rec	= new KadaiDefRecord(te_lec_key,kadai_key,db);	
		return	rec.shubetsu();
		
	}
	/**
	* 提出した課題ファイルの削除
	* 
	*/
	void	delKadaiFile(){
		String	stNumber	=	strHashWithNullAlart(htb,"_stNum","学籍番号の取り出し");
		Answer	ans			=	new Answer(htb,para,db);
		ans.deleteAnsRecord(stNumber);
	}
	/**
	* 特定の学生のこの課題提出情報を削除する<br>
	* 削除すると再提出が可能になる
	*/
	void	delInfo(){
		String	stNumber	=	strHashWithNullAlart(htb,"_stNum","選択した学籍番号の取り出し");
		KadaiInfo kdf		=	new KadaiInfo(szDB,db);
		kdf.set_keys(stNumber,te_aplec_key,kadai_key);
		kdf.delete_A_KadaiInfo();

	}
	/**
	 * リストをソートする
	 *
	 */
	void	sortTable(){
		String	mode	= getParameter("_sortMode");		// 
		putParameter("_sort",mode);							// _sortMode は変更を選択するリストボックス、_sortMode は _sortに値を記憶しておく
		if(mode.equals("ON")){
			putParameter("_no_dateTime","");					// リストボックスの表示を制御（ここでやっておかないと間に合わない）
			putParameter("_yes_dateTime","selected");
		}else{
			putParameter("_no_dateTime","selected");
			putParameter("_yes_dateTime","");
		}
	}
	
	/**
	 * 提出ファイルをzipアーカイブしてファイルキャビネットに登録する
	 *
	 */
	void	getFiles(){
		
	    /*
	     * ファイル提出課題でなければリターンする
	     */
	   	KadaiDefRecord	kdr	=	new	KadaiDefRecord(te_lec_key, kadai_key, db);
	    if(!(kdr.shubetsu()).equals(KadaiDefRecord.FILE)){
	        putParameter(MESSAGE,"★ファイル提出課題のための機能です");
	        return;
	    }
	    /*
		 * zipファイルを作成する
		 */
		String 			sourceDir	=	para.getKadaiPostDir2(teUid,aplec_key,kadai_key); //課題ファイルの所在
		String			zipFile		= 	te_lec_key + "-" + kadai_key +  ".zip";
		String 			zipDir		= 	para.fileCabinetPath(szDB,teUid);
		boolean 		result 		= 	getZip(sourceDir,zipDir + zipFile);
		
		if(!result){
		    putParameter(MESSAGE,"★現在、提出されたファイルはありません");
		    return;
		}
		/*
		 * ファイルキャビネットに登録する
		 */
		String	subject	=	getParameter("title") + ":" + getParameter("kadai_title");
		addCabinet(subject, zipFile);
		
		putParameter(MESSAGE,"★提出された全解答を圧縮ファイルにしてファイルキャビネットに登録しました．");
	}

	/**
	 * 提出されたファイルを指定の場所にzipアーカイブする
	 * @return
	 */
	public boolean  getZip(String sourceDir, String zipPath){

	    if(LOG.fa) {
	        LOG.println("");
	        LOG.println("■EvalList #getZip()");
	        LOG.println("    sourceDir   = " + sourceDir);
	        LOG.println("    zipPath     = " + zipPath);
	    }
	    /*
		 * 提出ファイルが一つもなければリターンする
		 */
		Files  fl 	= new Files(sourceDir);
		if(fl.n() == 0) {
			return false;
		}
		/*
		 * files オブジェクトからZIPファイルを生成する
		 */
	    FileGear.makeZipFile(zipPath, sourceDir);
		return	true;
	}
	/**
	 * 採点結果をファイルキャビネットに登録する
	 */
	public void  evalData(){
	   	
	   	KamokuApRecord	kar		= new KamokuApRecord(teUid,aplec_key,db);	// レコードに名簿ファイル名を持つ
		Meibo			meibo	= kar.getMeibo(para);
	   	/*
	   	 *  ハッシングした解答レコードオブジェクトを得る
	   	 */
	   	Answer      	as 	= 	new Answer(htb,para,db);
	   	int				n	=	as.getTotal();
	   	if(n==0){
	   	    putParameter(MESSAGE,"★現在、提出された課題はありません");
		    return;
	   	}
	   	Hashtable   	ht 	= 	as.getHash();	// ［学籍番号（キー）、成績データオブジェクト］からなるハッシュ
	   	/*
	   	 * 
	   	 */
	   	String	className	=	getParameter("title");
	   	String	kadaiTitle	=	getParameter("kadai_title");
	   	String	dateStart	=	getParameter("start");
	   	String	dateEnd		=	getParameter("end");
	   	/*
	   	 * 課題種別
	   	 */
	   	KadaiDefRecord	kdr	=	new	KadaiDefRecord(te_lec_key, kadai_key, db);
	   	String			shu	=	kdr.shubetsuName3();	
	   	
	   	
	   	
	   	Vector  dt  		= 	makeTable2(teUid,meibo,ht,className, kadaiTitle, shu, dateStart, dateEnd);
	   	
	   	String	fname		=	evalFname();
	   	String	path		=	para.fileCabinetPath(szDB,teUid) + fname;
	   	WriteEval(path,dt);
	   	/*
	   	 * 個人用ファイルキャビネットに登録する
	   	 */
	   	String	subject		=	getParameter("kadai_title")+"の採点結果";
	   	addCabinet(subject, fname);
	   	
		putParameter(MESSAGE,"★採点一覧表をファイルキャビネットに登録しました．");

	   	
	}
	/**
	 * 課題の採点結果を格納するファイル名を得る
	 * 
	 * @param htb
	 * @return
	 */
	public String	evalFname(){
	   	String kadaiKey = getParameter("kadai_key");
	   	//return "ev" + lec_key + "-" + kadaiKey + ".csv";
	   	return lec_key + "-" + kadaiKey + "_"+getParameter("kadai_title")+".csv";
	}
	/**
	 * 成績名簿ファイル作成
	 * @param path
	 * @param dt
	 * @param itms
	 */
	public void WriteEval(String path,Vector dt){
	   	try{
	    	// Windows-31Jで出力／自動フラッシュ
	    	PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(path),"Windows-31J"),true);

	    	int len;
	       	for(int i=0; i<dt.size(); i++){
	           	Csv c = new Csv(((Vector)dt.get(i)));
	           	String str = Cp932.toJIS(c.toCSV());
	           	str = replaceCTR_M(str); // 文字列をtrimし，改行コードは {<BR>} に置換する．
	           	out.println(str);
	       	}
	       	out.close();
	   	}catch(IOException e){
	   	}
	}	
	/**
	 * 個人用ファイルキャビネットに登録する
	 * @param ke
	 */
	public void addCabinet(String subject, String fname){
		if(LOG.fa) LOG.println("■RefList #addCabinet()");
		
		CBdatabase	cbd	=	new	CBdatabase(getDbConnection(), szDB);
		cbd.regist(getParameter(TUID), fname, subject, para);
		
	}
	/**
	 * ファイルデータの表等を作成する
	 */
	public Vector makeHedder(){
	   	if(LOG.fa) LOG.println("class exwork #makeHedder2() : 送信用名簿ヘッダを作る の先頭です");
		//
	   	Vector v = new Vector(15,10);
	   	v.add("番号");
	   	v.add("氏名");
	   	v.add("得点");
	   	v.add("提出日時");
	   	//
	   	return v;
	}
	/**
	 * ファイルデータの内容を作成する
	 * 
	 *   番号，氏名，得点，提出状況，提出日時
	 * 
	 * 
	 * @param teUid
	 * @param meibo
	 * @param ht
	 * @param name
	 * @param className
	 * @param kadaiTitle
	 * @param dateStart
	 * @param dateEnd
	 * @return
	 */
	public Vector makeTable2(String teUid, Meibo meibo,	Hashtable ht, String className, String kadaiTitle, String shu, String dateStart,String dateEnd){

	    
	    Vector	vr		=	new Vector(300,100);
	   	int 	n		=	meibo.getCounts();
	   	/*
	   	 * 課題に関する情報を先頭に入れる
	   	 */
	   	setInfo(vr, className, kadaiTitle, shu, dateStart, dateEnd);
	   	/*
	   	 * 行頭項目名をセットする
	   	 */
	   	vr.add(infoItem(""));	// 空行
	   	vr.add(makeHedder());		

	   	/*
	   	 * 本体データを作成する
	   	 */
	   	for(int i=0; i<n; i++){
	       	Vector	vc				=	new Vector(20,10);
	       	String	stNumber		=	meibo.getNumber(i); // 学籍番号
			Student	ma				=	new	Student(szDB,stNumber, db);
			
			vc.add(stNumber);		// 学籍番号
			vc.add(ma.kname());     // 学生の氏名

			Object obj = ht.get(stNumber);
           	if((obj==null)&&(!KadaiInfo.isExist(db,szDB,te_aplec_key,kadai_key,stNumber))){
				/* 解答ファイルがない */
			    vc.add("-");	//	score
			    vc.add("-");	// 	submig

            }else{
				KadaiInfo	kdf	= new KadaiInfo(szDB,db,stNumber,te_aplec_key,kadai_key);
				int 		cnt = kdf.read_KadaiInfo();					
				//				
				String	score   =	kdf.points();			if(isEmpty(score)){ score	=	"-";	}
				String	submit	=	kdf.latest_date_str();	if(isEmpty(submit)) { submit	=	"???"; }	
				//
			    vc.add(score);
			    vc.add(submit);
            }

			vr.add(vc);
	   	}
	   	return vr;
	}
	/**
	 * １行に１件の情報フィールドを作成してデータベクターの先頭に付加する
	 * @param vr
	 * @param className
	 * @param kadaiTitle
	 * @param dateStart
	 * @param dateEnd
	 */
	void	setInfo(Vector vr,String className, String kadaiTitle, String shu, String dateStart,String dateEnd){
	    
	    vr.add(infoItem(className));
	    vr.add(infoItem(kadaiTitle));
	    vr.add(infoItem(shu));
	    vr.add(infoItem(dateStart + " ～ " + dateEnd));
	    
	}
	Vector	infoItem(String	dataItem){
	    Vector	v	=	new	Vector();
	    v.add(dataItem);
	    v.add("");
	    v.add("");
	    v.add("");
	    return	v;
	}
	
	/**
	 * 文字列中の改行コードを{<BR>}に変換する
	 * 
	 * @param str
	 * @return
	 */
	public String replaceCTR_M(String str){
	   	if(str==null)               {return "";}
	   	if(str.length()==0)         {return str;}
	   	if(str.indexOf(CR) == -1)   {return str;}
	   	//
	   	StringBuffer sb   = new StringBuffer(10240);
	   	BufferedReader in = new BufferedReader(new StringReader(str));
	   	try{
	       	String s;
	       	boolean flag = false;
	       	while((s=in.readLine())!=null){
	           	if(flag) sb.append("{<BR>}");
	           	flag = true;
	           	sb.append(s.trim()); // 両端の空白を取って擬似改行コードを付ける
	       	}
	   	}catch(IOException e){
	   	}
	   	return sb.toString();
	}
	public void WriteStringBuffer(PrintWriter out,String str){
	   	BufferedReader in = new BufferedReader(new StringReader(str));
	   	String s;
	   	try{
	       	while((s=in.readLine())!=null){
	           	out.println(s);
	       	}
	       	in.close();
	   	}catch(IOException e){
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
	 * 
	 * @param	editmode	表示モード（true なら編集、falseなら新規）
	 */
	@Override
	public  void	display(boolean editmode){
		if(LOG.fa)	LOG.println("■ Sample #display(boolean editmode)");
		
		if(!editmode){
			putParameter(MESSAGE,"");
		}
	   	/*
	   	 * クラス名簿の有無を調べる（名簿がないと見れない）
	   	 * 解答レコードの件数を調べる
	   	 */
		String kadaiKey = getParameter("_kadai_key"); // 選択された課題のキー
		Answer ans 		= new Answer(htb,para,db);
		/*
		 * 解答レコードがあれば表示処理を行う
		 */
		KadaiDefRecord	kdr		= 	ans.getKadaiDefRecord();	// 課題定義レコード
		KadaiApRecord	kar		= 	ans.getKadaiApRecord();		// 課題実施レコード
		putParameter("_pos",String.valueOf(0)); 				// 表示位置．初回は先頭から表示
	   	//
	   	String 			title = 	kdr.title();
	   	String 			start = 	kar.getStartDate2();
	   	String 			end   = 	kar.getEndDate2();
	   	/*
	   	 * 置き換えのためパラメータに加えておく
	   	 */
	   	putParameter("_kadai_title",title);
	   	putParameter("_start",start);
	   	putParameter("_end",end);
	   	/*
	   	 * 解答レコード(ans) を使って計数データをhtb に埋め込む
	   	 * ans.getTotal() == 0　でも可
	   	 */
	   	putParameter("_n1",get000String(String.valueOf(ans.getAll())));           	// 名簿上の総数
		putParameter("_n2",get000String(String.valueOf(ans.getTotal())));         	// 提出数
	  	putParameter("_n3",get000String(String.valueOf(ans.getSubmitted())));     	// 作成済み数
	   	putParameter("_n4",get000String(String.valueOf(ans.getMaking())));        	// 作成中数
	   	/*
	   	 * 提出日時でソートするかどうか
	   	 */
		String sortMode = getParameter("_sort");
		if(isEmpty(sortMode)){
			putParameter("_sort","OFF");					// _sortMode は変更を選択するリストボックス、_sortMode は _sortに値を記憶しておく
			putParameter("_sortMode","OFF");				// ソートしない
			putParameter("_no_dateTime","selected");
			putParameter("_yes_dateTime","");
		}// それ以外は指定に従う
		/*
		 * 課題の種別を表すアイコンファイル名
		 */
	  	String kadai_Icon	= ans.kadaiIcon();
		putParameter("_kadaiIcon",kadai_Icon);
		
		/* 
		 * getParameter(DISPFILE)にはファイルの完全パス名が入っている 
		 */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}
	/**
	 * ブロック出力
	 */
	@Override
	public void	write(String key,Vector exHtml){

		if(key.equals("evlist")){
			evlist(exHtml);
			
		}else if(key.equals("empty")){
			empty(exHtml);
			
		}
	}
	/**
	 * 提出リストの表示
	 * 
	 * @param exHtml
	 */
	void	evlist(Vector exHtml){
		
		Answer		as 	= new Answer(htb,para,db);
       	Hashtable	ht 	= as.getHash();
		KadaiDefRecord	kadaiDef	= as.getKadaiDefRecord();
		//
		// クラス名簿を得る
		KamokuApRecord	kar		= new KamokuApRecord(teUid,aplec_key,db);	// レコードに名簿ファイル名を持つ
		Meibo		meibo		= kar.getMeibo(para);
		if(meibo==null){
			putParameter("_meibo_counts",String.valueOf(0));
			return;
		}
		//
		// ソートの選択
		String sortFlag = getParameter("_sort");
		if(sortFlag.equals("ON")){
			Hashtable hsort = as.makeSmb();	// 提出時間と学籍番号のハッシュ
			meibo.makeMeibo2(hsort);		// 提出時間でソートした名簿を作成する
			meibo.setSw(false);				// 標準で提出時間でソートした名簿を使うように設定する
		}else{
			meibo.setSw(true);				// 学籍番号順の名簿を使うよう設定する
		}
		/*
		 * 課題採点結果一覧表処理準備
		 * 課題のファイルの所在　ex. /home/pc/kawaba01/file/103/000078/
		 */
		String  	filePath 	= para.getKadaiPostDir2(teUid,aplec_key,kadai_key);
		//System.out.println("*** filePath = " + filePath);
		
		Files		fl			= new Files(filePath);
		int 		max   		= meibo.getCounts();
		/*
		 * 	プランク行を表示するかどうかの判断のため覚えておく( #meibo_blankBLK 参照）
		 */
		putParameter("_meibo_counts",String.valueOf(max));
		
		for( int i=0; i<max; i++){
           	if(LOG.fa) LOG.println( "class exwork #paramPrintOPT() : 課題作成リスト[" + i + "]行目です");
           	/*
           	 * 行番号
           	 */
           	String  numStr = "000" + String.valueOf(i+1);
           	int     posE = numStr.length();
           	int     posS = posE - 3;
           	String  num  = numStr.substring(posS,posE);
			putParameter("_k",num);
			/*
			 * 名簿ファイルから
			 */
           	String stNumber = meibo.getNumber(i);	putParameter("_stNumber",stNumber);
			String stName	= meibo.getName(i);		putParameter("_stName",stName);
			//
           	/*
            * 解答ファイルがあるか、または、（個人の）課題情報レコードがあれば具体的な表示を行う
            * 
            */ 
           	Object obj = ht.get(stNumber);
	       	if((obj==null)&&(!KadaiInfo.isExist(db,szDB,te_aplec_key,kadai_key,stNumber))){
				/* 解答ファイルがない */
				putParameter("_score"  		, "-");
				putParameter("_updateTime"	, "-");
				putParameter("_kubun"		, KadaiInfo.NOTYET);// 未提出
				putParameter("_sent" 		, "0");
				putParameter("_fileList"	, "-");
				putParameter("_turn"		, "OFF");
				/*
				 * 行入れ替えフラグオン
				 */
				putParameter("_DelFlag","*");
				putParameter("_MarkFlag","*");

            }else{
				/*
				 * 行入れ替えフラグをオフ
				 */
				putParameter("_DelFlag","");
				putParameter("_MarkFlag","");

				KadaiInfo	kdf	= new KadaiInfo(szDB,db,stNumber,te_aplec_key,kadai_key);
				int 		cnt = kdf.read_KadaiInfo();					
				//				
				String	score   	=	kdf.points();			if(isEmpty(score)){ score	=	"-";	}
				String	kubun		=	KadaiInfo.NOTYET;		if(cnt>0){	kubun	= kdf.saiten_flag();}			
				/*
				 * 異常終了したレコードではファイルがないので obj は null である．
				 * しかし，KadaiInfo はあるのでここへ来る．
				 * 表示しなくてはならないので間違った処理ではない 
				 */
				String	sentMark		=	"";
				if(obj!=null){
				    sentMark	=	((AnswerRecord)obj).getEval();
				}
				 /*
				 * Annponce2 で送信済みマークが AnswerRecord に入っている
				 * それをチェックして，送信済みであれば"1" とする
				 * グラフィックス yubin_0 または yubin_1 のどちらかを表示する
				 */
				String	sent		=	"0";
				if(sentMark.equals("done"))	sent	=	"1";
				
				String	submit		=	kdf.latest_date_str();	if(isEmpty(submit)) { submit	=	"???"; }	
				//
				putParameter("_score" ,		score);
				putParameter("_updateTime",	submit);
				putParameter("_kubun",		kubun);	// 状態区分アイコン用
				putParameter("_sent",		sent);	// 送信済みメールアイコン用
				putParameter("_turn", 		"ON");	// 削除アイコン用
				/*
				 * ファイルアクセスのための埋め込みURL作成用データ
				 * 内部アドレスからのアクセスでは、リンク先を http://192.168.0.213/usr/ に変える
				 */
				String aRef		= 	"";
				String product	=	para.getViewSwitch();
				if( (product.equals("SYSTEM")) || (product.equals("SYSTEM_VIEW")) ){
					aRef		=	para.getKadaiPostURL(htb,teUid,aplec_key,kadai_key); // IPアドレスでURLを返す
				}else{
					aRef		=	para.getKadaiPostURL(teUid,aplec_key,kadai_key); // URLを返す
				}
				//
				String lineHtml = 	
					new String("<a href=\"%_link%\" target=\"_blank\"><img src=\"/pc/images/fileIcon.gif\" name=\"fileIcon\" width=\"13\" height=\"12\" border=\"0\"  alt=\"%_fileName%\" ></a>");
				
				String [] files = fl.list(stNumber);
				if(files != null) {
					StringBuffer	bf 	= 	new StringBuffer(2000);
					boolean 		sp	= 	false;
					Hashtable		fh	=	new	Hashtable();
					
					for(int m=0; m<files.length; m++){
						if(sp) bf.append(", "); 
						fh.put("_link",aRef + files[m]);
						fh.put("_fileName",files[m]);
						
						String	s	=	substitute(lineHtml,fh);
						bf.append(s);
						sp = true;
					}
					putParameter("_fileList",bf.toString());
				}else{
					putParameter("_fileList","-");
				}
           }
			printVector(exHtml);
       }

	}
	/**
	 * ブランク行の表示
	 * @param exHtml
	 */
	void	empty(Vector exHtml){
		int	n	= Integer.parseInt( strHashZero(htb,"_meibo_counts") );	// 既に表示した行があるか
		if(n > 0)	return;
		//
		printVector(exHtml);
		
	}
}