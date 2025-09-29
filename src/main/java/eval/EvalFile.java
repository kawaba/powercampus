/*
課題提出一覧表

*/
package eval;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Vector;
import database.Database;
import database.DbConnectionBroker;
import database.KeyGen;
import framework.LOG;
import framework.PCvar;
import framework.Param;
import framework.SuperPlayer;
import kadai.Answer;
import kadai.AnswerRecord;
import kadai.KadaiDefRecord;
import kadai.KadaiInfo;
import kamoku.KamokuApRecord;
import meibo.Meibo;
import tktools.Files;
import tktools.Gear;
import tktools.TemplateBox;

/**
 * 
 *
 	#
	# ##################
	#     EvalFile
	# ##################
	#
	<program $eval.EvalFile>
		<dispatch  html=EvalFile.html  number=1540  class=eval.EvalFile />
		<variable>
		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA title lec_key aplec_key kadai_key/>
		  <accept    CMD  stNumber k  />
		  <keep      delta page />
		  
		  <form      sortMode />
		</variable>
	</program> 
 *
 *
 * 変数の説明
 *
 * 1. receive 
 * 2. accept
 * 		stNumber	--  採点された学生の学籍番号
 * 		k			--	ページ内データ番号。得点を[getParameter(score+k)]とアクセスする 
 * 3. keep
 * 		delta	-- １ページ当たりの行数
 * 		page	-- 最大ページ番号
 * 4. form
 * 		score[k]	-- 採点された点数
 *
 */
public class EvalFile extends SuperPlayer implements PCvar{

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
	
	String szDB;
	String teUid;
	String aplec_key;
	String kadai_key;
	String te_aplec_key;
	
	int	page;
	int	DELTA;// ページ送り数

	/**
	* 名簿ファイルと登録数の最大値
	*/
	KamokuApRecord	kamoku;
	Meibo			meibo;
	int				max;
	int				maxPage;	

	public	EvalFile(){
		super();
		if(LOG.fa) LOG.println("■ EvalFile #コンストラクタ");
	}
	/**
	 * 
	 */
	@Override
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		broker	=	getDbConnection();
		db		=	new Database(broker);	
		szDB			=	strHashWithNullAlart(htb,"_szDB","学生ＤＢ名の取り出し");
		teUid    		= 	strHashWithNullAlart(htb,"_teUid","教師ＩＤの取り出し");
		aplec_key 		= 	strHashWithNullAlart(htb,"_aplec_key","講義実施キーの取り出し");
		kadai_key 		= 	strHashWithNullAlart(htb,"_kadai_key","課題キーの取り出し");
		te_aplec_key	=	KeyGen.get_te_aplec_key2(teUid,aplec_key);
		
		/*
		 * クラス名簿と登録数
		 */
		kamoku			= 	new KamokuApRecord(teUid,aplec_key,db);	// レコードに名簿ファイル名を持つ
		meibo			= 	kamoku.getMeibo(para);
		if(meibo==null){
			htb.put("_meibo_counts",String.valueOf(0));
			return;
		}
		max	=	meibo.getCounts();	
	
		/*
		 * １行当たり表示行数、最大ページ番号
		 */
		String	deltaStr	=	Gear.strHash(htb, "_delta");
		if((Gear.isEmpty(deltaStr))||(deltaStr.equals("_delta")) ){
			DELTA	=	16;
		}else{
			DELTA	=	Integer.parseInt(deltaStr);
		}
		htb.put("_delta", String.valueOf(DELTA));	
		
		String	pgStr	=	Gear.strHash(htb, "_page");
		if((Gear.isEmpty(pgStr))||(pgStr.equals("_page")) ){
			page	=	0;
		}else{
			page	=	Integer.parseInt(pgStr);
		}
		maxPage	=	max / DELTA;
		htb.put("_page", String.valueOf(page));
	}
	/**
	 * 
	 */
	@Override
	public	String	dispatch(){
		if(LOG.fa) LOG.println("■EvalFile #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		if(cmd.equals("WRITE")){
			/*
			 * 採点データ書き込み
			 */
			update();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
			
		}else if(cmd.equals("SORT")){
			/*
			 * ソートする
			 */
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
			
		}else if(cmd.equals("GOTO_TOP")){
			gotoTopPage();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
			
		}else if(cmd.equals("GOTO_END")){
			gotoLastPage();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
			
		}else if(cmd.equals("NEXT")){
			nextPage();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
		
		}else if(cmd.equals("BACK")){
			prePage();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
	
		}else if(cmd.equals("DISP")){
			/*
			 * １ページ当たり表示行数制御
			 */
			setPage();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
	
		}else if(cmd.equals("RETURN")){
			/*
			 * リターンする
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
	void	gotoTopPage(){
		page	=	0;
		putParameter("_page", String.valueOf(page));
	}
	void	gotoLastPage(){
		page	=	maxPage;
		putParameter("_page", String.valueOf(page));
	}
	void	nextPage(){
		if(isValidPage(page + 1))	{page++;}
		putParameter("_page", String.valueOf(page));
	}
	void	prePage(){
		if(isValidPage(page - 1))	{page--;} 
		putParameter("_page", String.valueOf(page));
	}
	void	setPage(){
		putParameter("_page", String.valueOf(page));
	}
	/**
	* ページ番号として正しいか
	* ページ番号はゼロオリジン
	* @param pg
	* @return
	*/
	boolean	isValidPage(int pg){
		if( (pg >=0)&&(pg<=maxPage))	return	true;
		return	false;
		
	}	
	/**
	 * 採点データを書き込む
	 */
	void	update(){
		
		String	stNumber=	Gear.strHash(htb, "_stNumber");
		String	k		=	Gear.strHash(htb, "_k");
		String	score	=	Gear.strHash(htb, "_score" + k);
		//
		String	ansPath	=	para.stFilePath(teUid, aplec_key, kadai_key, stNumber);
		File	ansFp	=	new File(ansPath);
		if(ansFp.exists()){
			updateFileRecord(stNumber, score);
		}else{
			createFileRecord(stNumber, score);
		}
		update_KadaiInfo(stNumber, score);		
	}
	/**
	* KadaiInfo を更新する
	* 
	* @param ans
	*/	
	public	void update_KadaiInfo(String stNumber, String score){
		//
		KadaiInfo kdi	= new KadaiInfo(szDB,db);
		kdi.updateScore( stNumber, te_aplec_key, kadai_key, score);
	}	
	/**
	* 解答レコードを新規作成し得点を書き込む（FILEの解答用）
	* 
	* @param stNumber
	* @param score
	* @return
	*/
	public AnswerRecord createFileRecord(String stNumber, String score){
		if(LOG.fa) LOG.outHash(htb,"■ EvalFile #createFileRecord() ");
		
		File		parent	=	new File(para.kadaiAnsDir(teUid, aplec_key, kadai_key));
		if(!parent.exists()){
			boolean result	=	parent.mkdirs();
			if(LOG.fa) LOG.println("　⇒　mkdirs " + para.kadaiAnsDir(teUid, aplec_key, kadai_key ) + " is " + result);
		}
		/* 
		 * _stNumber がシステムハッシュにあることが前提
		 */
		htb.put("_stNumber", stNumber);
		String			ansPath	=	para.stFilePath(teUid, aplec_key, kadai_key, stNumber);
		AnswerRecord	r		= 	new AnswerRecord(htb);
		r.setScore(score);
		writeAnsRec(ansPath,r);
		//
		return	r;
	} 
	/**
	* 解答レコードの得点のみ更新する（FILEの解答用）
	* 
	* @param stNumber
	* @param score
	* @return
	*/
	public AnswerRecord updateFileRecord(String stNumber, String score){
		if(LOG.fa) LOG.outHash(htb,"■ EvalFile #updateFileRecord() ");
		
		File		parent	=	new File(para.kadaiAnsDir(teUid, aplec_key, kadai_key));
		if(!parent.exists()){
			boolean result	=	parent.mkdirs();
			if(LOG.fa) LOG.println("　⇒　mkdirs " + para.kadaiAnsDir(teUid, aplec_key, kadai_key ) + " is " + result);
		}
	
		String		ansPath	=	para.stFilePath(teUid, aplec_key, kadai_key, stNumber);
		//File		ansFP	=	new File(ansPath);
		AnswerRecord	r 	= 	getAnsRecord(ansPath);
		//AnswerRecord	r		= 	new AnswerRecord(htb);
		r.setScore(score);
		writeAnsRec(ansPath,r);
		//
		return	r;
	}    
	/**
	* パスを指定して解答レコードを書き込む
	*
	* @param path
	* @param r
	* @return
	*/
	public boolean  writeAnsRec(String path,AnswerRecord r){
		if(LOG.fa) LOG.println("■ EvalFile #writeAnsRec() ");
	
		ObjectOutputStream  objOut  = null;
		try{
			objOut = new ObjectOutputStream(new FileOutputStream(path));
			try{
				objOut.writeObject(r); // Serializable
				objOut.flush();
				objOut.close();
				return true;
			}catch(IOException e1){
			}
		}catch(IOException e2){
		}
		return false;
	}
	/**
	* ◎ファイルパスからAnswerオブジェクトを読みこんで返す．
	* 
	* @param fname
	* @return			ファイルがない場合は null を返す．
	*/
	public AnswerRecord getAnsRecord(String fname){
		if(LOG.fa) LOG.println("■ EvalFile #getAnsRecord() ");
	
		ObjectInputStream  objIn  = null;
		AnswerRecord ans = null;
		try{
			objIn = new ObjectInputStream(new FileInputStream(fname));
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
			/*
			 * 最初はform 変数 "sortMode" の値がないのでここで提出順に設定しておく
			 */
			putParameter("_sortMode","OFF"); 
			
			/*
			 * 表示開始位置（初回は先頭から表示）
			 */
			htb.put("_pos","0");
		}
		/*
		 * 諸データをシステムハッシュにセットする
		 */
		setupData();
		/*
		 * ソート表示設定
		 * 実際のソートは表示処理の中で行なう
		 */
		String sortMode = strHash(htb,"sortMode");
		setSort(sortMode);
		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}
	/**
	 * 諸データの取得
	 *
	 */
	public	void setupData(){
		/*
		 * 解答ファイル名クラスを得る
		 * EvalList で解答が１件以上あることは確認されている
		 */
		Answer 			ans 		= 	new Answer(htb,para,db);
		KadaiDefRecord	kdr			= 	ans.getKadaiDefRecord();
		/*
		 * 課題タイトル
		 */
		String title = kdr.title();
		if(title.length()>42){
			title = title.substring(0,42) + "...";
		}
		htb.put("_disp_kadai_title",title);
		/*
		 * 課題アイコンファイル名
		 */
	  	String kadai_Icon	= ans.kadaiIcon();
		htb.put("_kadaiIcon",kadai_Icon);
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
	@Override
	public void	write(String key,Vector exHtml){
		if(key.equals("meibo")){
			meibo(exHtml);
				
		}else if(key.equals("fileNotExist")){
			fileNotExist(exHtml);
			
		}else if(key.equals("fileExist")){
			fileExist(exHtml);
			
		}else if(key.equals("meibo_blankBLK")){
			meibo_blankBLK(exHtml);
			
		}
	}
	/**
	 * １行分の表示を行なう
	 * 
	 * @param exHtml
	 */
	void	meibo(Vector exHtml){
		/*
		 * 学籍番号をキーとする回答レコードのハッシュ
		 * 採点結果を書き込むが、この中に解答があるのではない。
		 * 解答は別ファイルである。
		 */
		Answer			as 			=	new Answer(htb,para,db);
		Hashtable		ht 			=	as.getHash();
		/*
		 * 提出されたファイルを扱う Files オブジェクトを作成
		 * 課題のファイルの所在　ex. /home/pc/kawaba01/file/103/000078/
		 */
		String  	filePath 	=	para.getKadaiPostDir2(teUid,aplec_key,kadai_key);
		Files		fl			=	new Files(filePath);						// 提出ファイルを処理するクラス
		/*
		 * ソート指定による使用名簿ファイルの選択
		 * 採点は名簿を元に提出をチェックする
		 */
		selectMeiboSort( strHash(htb,"_sort") , as);
		/*
		 * 表示ページ位置の設定
		 */
		htb.put("_meibo_counts",String.valueOf(max));
		int			start		=	page * DELTA;
		int			temp		=	start+DELTA;
		int			end			=	(temp<=max) ?  temp : max ;
		/*
		 * 採点表の表示 
		 */
		for( int i=start ; i<end; i++){
			/*
            * 行番号、学籍番号と名前
            */
           String stNumber	= meibo.getNumber(i);
           String stName	= meibo.getName(i);

           htb.put("_k",Gear.get000type(i+1));
           htb.put("_next",Gear.get000type(i<end?i+2:start));
           htb.put("_stNumber",stNumber);
           htb.put("_stName",stName);
           /*
            * ファイル提出の有無を判定して表示を行なう
            */
           Object obj = ht.get(stNumber);// 採点用レコード
           if((obj==null)&&(!KadaiInfo.isExist(db,szDB,te_aplec_key,kadai_key,stNumber))){
           		/*
           		 * 採点ファイルが未作成またはデータベースに提出履歴が無いことをセット
           		 */
           		setNoFile();
            }else{
                /*
                 * 課題提出履歴情報
                 */
                setupKadaiInfo(stNumber);
				/*
				 * 学生の提出ファイルをリストアップする
				 */
				htb.put("_fileList",FilelistHtml(fl, stNumber));
				/*
				 * 結果送信済みマーク
				 */
				String	sentMark		=	"";
				if(obj!=null){
				    sentMark	=	((AnswerRecord)obj).getEval();
				}
				String	sent		=	"0";
				if(sentMark.equals("done")){
				    sent	=	"1";
				}
				putParameter("_sent",		sent);	// 送信済みメールアイコン用
				
           }
			printVector(exHtml,htb);
       }
       if(LOG.fa) LOG.println( "class exwork #paramPrintOPT() : 課題作成リストで全ての行を生成しました");
	}
	/**
	 * 採点ファイルが未作成またはデータベースに提出履歴が無いことを
	 * システムハッシュにセットする
	 */
	void	setNoFile(){
		htb.put("_fileCount",   "NOFILE");	
		htb.put("_score"  ,		"");
		htb.put("_updateTime",	"-");
		htb.put("_kubun",		KadaiInfo.NOTYET);// 未提出
		htb.put("_sent" ,		"0");
		htb.put("_writeIcon",   "spacer.gif");
		htb.put("_fileList",	"-");

	}
	/**
	 * 特定の学生の課題提出履歴をシステムハッシュにセットする
	 * 
	 * @param stNumber
	 */
	void	setupKadaiInfo(String stNumber){
    	/*
    	 * 課題提出履歴データベースを検索して情報を得る
    	 */
		htb.put("_fileCount",   "EXIST_FILE");	
		KadaiInfo	kdf	= new KadaiInfo(szDB,db,stNumber,te_aplec_key,kadai_key);
		int 		cnt = kdf.read_KadaiInfo();					
		/*
		 * 得点(score)など表示情報は課題情報DB（KadaiInfo）から取得する．
		 */			
		String	score   =	kdf.points();
		if(isEmpty(score)||score.equals("-")){
			score	=	"";	
		}
		String	kubun	=	KadaiInfo.NOTYET;
		if(cnt>0){
			kubun	= kdf.saiten_flag();
		}			
		String	sent	=	"0"; // 使っていないのか？
		String	submit	=	kdf.latest_date_str();
		if(isEmpty(submit)) {
			submit	=	"???"; 
		}	

		htb.put("_score" ,		score);
		htb.put("_updateTime",	submit);
		htb.put("_kubun",		kubun);
		htb.put("_sent",		"0");
		htb.put("_writeIcon",   "fd12.gif");
	}
	/**
	 * 指定に従ってソートされた名簿を選択する
	 * @param sortFlag
	 */
	void selectMeiboSort(String sortFlag, Answer as){
		if(sortFlag.equals("ON")){
			/*
			 * hsortは提出時間と学籍番号のハッシュテーブル。
			 * hsortを使って提出時間でソートした名簿を作成しこれを使うよう設定する
			 */
			Hashtable hsort = as.makeSmb(); 
			meibo.makeMeibo2(hsort); 
			meibo.setSw(false); 
			
		}else{
			/*
			 * 学籍番号順の名簿を使うよう設定する
			 */
			meibo.setSw(true); 
		}
		
	}
	/**
	 * 提出ファイルがない時の表示
	 * 
 	 * @param exHtml
 	 */
	void	fileNotExist(Vector exHtml){
		if(( getParameter("fileCount") ).equals("NOFILE")){
			printVector(exHtml);
		}
		return;
	}
	/**
	 * 提出ファイルがある時の表示
	 * 
 	 * @param exHtml
 	 */	
	void	fileExist(Vector exHtml){
		if(( getParameter("fileCount") ).equals("EXIST_FILE")){
			printVector(exHtml);
		}		
	}
	/**
	 * リスト画面でブランク行を表示する
	 * 
 	 * @param exHtml
	 */	
	void	meibo_blankBLK(Vector exHtml){
		/*
		 * 	既に表示した行がなければ表示する
		 */
		int	n	= Integer.parseInt( strHashZero(htb,"_meibo_counts") );
		if(n > 0){
			return;
		}else{
			printVector(exHtml);
		}
	}

	/**
	 * 提出ファイルをリストアップするHTMLを返す
	 * 
	 * @param fl
	 * @param stNumber
	 * @return
	 */
	String	FilelistHtml(Files fl, String stNumber){
		/*
		 * 課題提出先相対URL
		 */
		String 		aRef	= 	para.getKadaiPostURL(teUid,aplec_key,kadai_key);
		/*
		 * 提出ファイルとそのURLを書き込むためのHTMLテンプレートを得る
		 */
		TemplateBox	tbx			=	new TemplateBox(para.htmlTemplate());
		String 		lineHtml 	=	tbx.get("report");
		/*
		 * HTML記述を作成し、"fileList"としてセットする
		 */
		String		html	=	"";
		String [] 	files 	= fl.list(stNumber);
		if(files != null) {
			StringBuffer	bf	= 	new StringBuffer(2000);
			boolean 		sp	= 	false;
			Hashtable		fh	=	new	Hashtable();
			
			for(int m=0; m<files.length; m++){
				if(sp) bf.append(", "); 
				//
				fh.put("_link",aRef + files[m]);
				fh.put("_fileName",files[m]);
				String s = substitute(lineHtml,fh);
				bf.append(s);
				sp = true;
			}
			html	=	bf.toString();
		}
		return	html;
		
	}

}