package stkadai;

import java.io.*;
import java.util.*;

import database.*;
import framework.*;
import tktools.*;
import	epml.*;
import kadai.*;

 
/**
 *	学生用のファイル提出課題およびその参照
 *
 	#
	# ##################
	#     StKadaiFile
	# ##################
	#
	<program $stkadai.StKadaiFile>
		<dispatch  html=StKadaiFile.html  number=2440  class=stkadai.StKadaiFile />
		<variable>
		  <receive   NUMBER STAMP GROUP StUID  StNAME  TUID aplec_key lec_key title kadai_key/>
		  <accept    CMD    UPLODE  deleteFname />
		  <keep      />
		  
		  <form      />
		</variable>
	</program> 
 *
 *
 * 変数の説明
 *
 * 1. receive 
 * 2. accept
 * 		deleteFname --- 削除の時、ファイル名
 * 3. keep
 * 4. form
 *
 * 
 */
public class StKadaiFile extends SuperPlayer {

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
	String 		teUid;
	String		lec_key;
	String		te_lec_key;
	String		te_aplec_key;
	String		kadai_key;
	String		stNumber;
	String		aplec_key;
	//
	KadaiApRecord	kar;		// 課題定義レコード
	KadaiDefRecord 	rec;		// 課題実施レコード
	KadaiInfo		kdf;		// 課題情報レコード
	//
	String		shubetsu;		// 課題の種別（課題定義レコードＤＢから引く）
	String		subject;		// 課題名
	String		radio_disposal;	// 提出するかどうかの指示（学生）＜"0", "checked"＞
	String		disposal;		// 提出状況：KadaiInfo.NOTYET,WORKING,SUBMITTED,GRADED
	String		points;			// 得点（採点が済んでいればここに得点がある）
	//
	String		submitTime;		// 提出時間
	//
	String		submittedFilePath;	// 今回ポストされたファイルへの完全パス
	String		submittedFileName;	// 今回ポストされたファイル名
    
	String		levelVal;
    String		scoreVal;	
    //
	public	StKadaiFile(){
		super();
		if(LOG.fa) LOG.println("■ Sample #コンストラクタ");
	}
	
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		
		broker			=	getDbConnection();
		db				=	new Database(broker);	
		
		szDB			= 	strHash(htb,"_szDB");
		teUid			= 	strHash(htb,"_teUid");
		lec_key			=	strHash(htb,"_lec_key");
		aplec_key		= 	strHash(htb,"_aplec_key");
		kadai_key		= 	strHash(htb,"_kadai_key");
		
		stNumber		= 	getParameter(StUID);

		te_lec_key		= 	KeyGen.get_te_lec_key2(teUid, lec_key);
		te_aplec_key	=	KeyGen.get_te_aplec_key2(teUid, aplec_key);
		//
		kar				= 	new KadaiApRecord(te_aplec_key,kadai_key,db);	
		rec				= 	new KadaiDefRecord(te_lec_key,kadai_key,db);
		shubetsu		= 	rec.shubetsu();	// 種別
		subject			= 	rec.title();		// 課題名
		//
		setInfo();	// 課題情報をセットする
    }
	
    void	setInfo(){
		kdf	= new KadaiInfo(szDB,db);
		kdf.set_keys(stNumber,te_aplec_key,kadai_key);
		int cnt = kdf.read_KadaiInfo();
		if(cnt==0){
			disposal		= KadaiInfo.NOTYET;
			points			= "-";
			submitTime		= "-";
		}else{
			disposal	= kdf.saiten_flag();
			points		= kdf.points();
			if(points.equals("")){	points	= "-"; }
			submitTime	= kdf.latest_date_str();
			if(isEmpty(submitTime)){
				submitTime	= "-"; 
			}
		}
    }
	//
	public	String	dispatch(){
		if(LOG.fa) LOG.println("■StKadaiFile #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		if( cmd.equals("SAVE")){
		    /*
		     * 課題の受け取り
		     */
		    doSave();
		    
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
		
		
		}else if( cmd.equals("DELETE_FILE") ){
			/*
			 *  送信した特定のファイルを削除する
			 */
			String	msg	=	modifyCheck();	// 削除可能か
			if(!isEmpty(msg)){
				putParameter(MESSAGE, msg);

			}else{
				/*
				 * ファイル削除
				 */
			    String		fname	=	strHash(htb,"_deleteFname");	// 削除するファイル名（パス含まず）
				File		fp		=	getFileFP(fname);
				fp.delete();
				/*
				 * 特定の学生のポストしたファイルは複数ある可能性がある．
				 * 他にファイルがなければ，課題情報レコードも削除する
				 */
				String[] fileList	=	getFileList();
				if(fileList==null){
					int m = kdf.delete_KadaiInfo(kadai_key);
					setInfo();
					deleteAnsRecord();
				}
			}
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
		
		}else if(cmd.equals("RETURN")){
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;
			
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
	 * ファイルの受け取り処理
	 *
	 */
	void	doSave(){
	    if(LOG.fa)	LOG.println("■StKadai#doSave()");
	    /*
		 * ファイルを送信する
		 */
		String	msg	=	modifyCheck();	// 提出可能か
		if(!isEmpty(msg)){
			putParameter(MESSAGE, msg);
			
		}else{
			/*
			 * ファイルを受け取る
			 */
		    boolean	chk	=	doFile(); 
			if(chk){ 
				/*
				 * 提出状況書き込み
				 */
			    writeData();
			}
		}
	}
	/**
	 * 提出状況書き込み
	 * 提出状況を書き込み，ダミーの解答レコードも作製する
	 * 
	 * オーバーライドされる
	 */
	void	writeData(){
	    setKadaiInfo(setUpdateTime(),setDisposal());
		htb.put("_updateTime",submitTime);
		createAnsRecord();
	}
	
    /**
     * ダミーの解答レコードを作成する
     * 解答レコードがないと採点できないためダミーで作成しておく
     * 
     */
	void	createAnsRecord(){
		/*
		 * 課題レコードを書き込むディレクトリを検査し、なければ作成する
		 */
		File		parent	=	new File(para.kadaiAnsDir(teUid, aplec_key, kadai_key));
		if(!parent.exists()){
			boolean result	=	parent.mkdirs();
			if(LOG.fa) LOG.println("　⇒　mkdirs " + para.kadaiAnsDir(teUid, aplec_key, kadai_key ) + " is " + result);
		}
		/*
		 * 課題レコードを作成する
		 */
		htb.put("_stNumber", stNumber);
		AnswerRecord	ans		= new AnswerRecord(htb); // 入力結果からレコードを構成する
		String			path	= para.stFilePath(teUid,aplec_key,kadai_key,stNumber);	// 提出ファイルへのフルパス
		boolean      	chk  	= writeAnswer(path,ans);
		if(!chk) { LOG.errStop(out,"★ StKadaiReport #sub_wrt() : ★ 解答をファイルに書けません.");}		
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
	/**
	 * 解答レコードのファイルを削除する
	 */
	void	deleteAnsRecord(){
		String	path	= 	para.stFilePath(teUid,aplec_key,kadai_key,stNumber);	// 提出ファイルへのフルパス
		File	ansFP	=	new File(path);
		ansFP.delete();
	}
	/**
	 * ポストした特定の名前のファイルのファイルポインタを得る
	 * @param fname	ファイル名
	 * @return			ファイルポインタ
	 */
	public	File	getFileFP(String fname){
			
		String	dir	=	para.getKadaiPostDir(teUid,aplec_key,kadai_key);
		File	fp	=	new File(dir + fname);	
		return	fp;
	}
	/**
	 *  ファイルが提出可能かどうか
	 * @return	可能な場合nullを返す．可能でない場合はメッセージを返す
	 */
	String	modifyCheck(){
		
		String	msg	=	null;
		// 期限が過ぎた
		if(kar.isOver()){
			if(LOG.fa) LOG.println("■ 期限切れです");
			doFile(false);					// ファイルがあれば消す
			msg	=	"★★ もう提出期限が過ぎましたので、この課題は操作できません ★★";
		}
		/*
		 * 採点済み
		 * 
		 * ただしタイピング課題は自動採点なので何度でも提出可
		 * とするため，チェックから除外する
		 */
		if(!shubetsu.equals(KadaiDefRecord.TYPE)){
			if(disposal.equals(KadaiInfo.GRADED)){
				if(LOG.fa) LOG.println("■ 採点済みです");
				doFile(false);				// ファイルがあれば消す
				msg	=	"★★ すでに採点されていますのでこの課題は操作できません ★★";
			}
		}
		
		return	msg;		
	}
	/**
	 * データベースに課題の状況を登録する
	 * @param TimeString	
	 */
	void	setKadaiInfo(String TimeString,String disposalString){
		if(LOG.fa) LOG.println("class StKadai #setKadaiInfo() : データベースに状況を登録する の先頭です");
		//
		// 先読みして有無を確かめる
		int	cnt	= kdf.read_KadaiInfo();
		if(cnt==0){// 新規
			//
			kdf.add_data(shubetsu,disposalString,TimeString,subject,points);
			kdf.insert_KadaiInfo();
		}else{// 更新
			//
			kdf.set_data(shubetsu,disposalString,TimeString,subject,points);
			kdf.update_KadaiInfo();
		}
	}
	/** 課題の提出時間をsubmitTimeとハッシュにセットする */
	String setUpdateTime(){
		submitTime	=	getDate_short();	// "yy/mm/dd hh:mm:ss"
		return	submitTime;
	}
	/** 
	 * 課題の提出状況をdisposalにセットする
	 * ひとつでも提出済みファイルが存在すれば KadaiInfo.SUBMITTED をセットする
	 * @return
	 */
	String	setDisposal(){
		//
		// 提出ファイルが存在すれば didposal = SUBMITTED
		// ファイル提出の場合は WORKING はありえない
		if(isExistFile()){
			disposal = KadaiInfo.SUBMITTED;	// 提出
		}else{
			disposal = KadaiInfo.NOTYET;	// 未提出
		}
		return	disposal;
	}
	/**  
	 * 提出ファイルの有無を調べる
	 * 複数ファイルの提出の分別には対応していない．ひとつでも提出があるとtrue を返す
	 * @return	　ひとつ以上の提出ファイルが存在する時 true そうでないとき false
	 */
	boolean	isExistFile(){
		//
		// ファイルポストディレクトリ
		String 	dir 	= para.getKadaiPostDir2(teUid,aplec_key,kadai_key);
		Files	fl		= new Files(dir);			// 提出ファイルを処理するクラス
		String [] files = fl.list(stNumber);
		if(files==null)	return	false;
		return	true;
	}
	//
	//  課題ファイルの受け取り処理
	//　　マルチパートで読み込んだファイルをチェックしファイル名を変更して適当なディレクトリに移し true を返す
	//	　読み込んだファイルがない場合は、マルチパート入力で作成されたディレクトリを削除し false を返す
	//    エラーが発生したらマルチパート入力で作成されたディレクトリを削除しメッセージを _massage に入れ false を返す
	//
	boolean doFile(){
		return doFile(true);
	}
	boolean doFile(boolean dofile){
	    if(LOG.fa)	LOG.println("■StKadai#doFile()");
	    
		// ファイル処理 
	    String savedir	 = (String)htb.get("_savedir");			// ファイルを書き込んだ一時ディレクトリ
    	if(!dofile){
			delDir(savedir);	// ファイルを受け取らない場合はディレクトリごと消去して戻る
			return true;
		}
		String mfilename = (String)htb.get("_onlyFilename");	// ファイル名のみ
        String dataFile  = (String)htb.get("_fileName");		// savedir + onlyFilename == 一時記録ファイルへのフルパス
        String files	 = (String)htb.get("_counts");			// マルチパートで読み込んだファイル数
		int	fcnt	 = Integer.parseInt(files);
		//
		// ファイルの存在チェック
		if(fcnt > 0){ // ファイルを読み込んでいれば１以上
            if(LOG.fa) LOG.println("class stwork#doFile() :  file name = " + dataFile);
			File mfp   = new File(dataFile);
            if(mfp.length()==0){    // （長さがゼロならファイル名の間違い）
                putParameter(MESSAGE,"★ 指定されたファイルは存在しません");
                delDir(savedir);	// 作業用のディレクトリとファイルがあれば再帰的に消す
				return false;
            }
        }else{
			delDir(savedir);		// 作業用のディレクトリとファイルがあれば再帰的に消す
			return false;			// ファイルを読み込んでいない
		}
		//
		if(fcnt > 0){ 							// ファイルポストを受け付け＆ファイルがポストされている
			// 移動先ディレクトリ（例えば /home/pc/kawaba/file/102/000013/ ）のFileオブジェクトを返す
			//
			File 	mfp			= new File(dataFile);									// 一時ファイル格納場所（ここにある）
			String  dir			= para.getKadaiPostDir( teUid, aplec_key, kadai_key);	// String で例えば /home/pc/kawaba/file/ck-2651/ を返す
			File 	moveTo		= new File(dir);
			if(!moveTo.isDirectory()){													// ディレクトリがなければ作る
				moveTo.mkdirs();
			}
			String  newFile		= getNewFile(dir,htb); // ファイル名は拡張子をそのままにして、学籍番号と連番に変更される
			/*
			 * （タイピング課題などの場合）間違った種類のファイルは受け取らない
			 * getNewFile()でチェックしている．getNewFile()はオーバーライドされる
			 */
			if(newFile==null){
			    putParameter(MESSAGE,"★ ファイルの種類が違います");
			    delDir(savedir);		// 作業用のディレクトリとファイルがあれば再帰的に消す
				return false;			// ファイルを読み込んでいない			    
			}
			
			//
            boolean flag   	= mfp.renameTo(new File(moveTo,newFile));	// 移動処理
			delDir(savedir);								// ディレクトリを削除
			//
			if(!flag) {
				LOG.println("class stwork #doFile() : ポストされたファイルをリネームできない");
			}else{
			    submittedFilePath	=	dir + newFile;
			    submittedFileName	=	newFile;
			    
			}
			//
		}else{
			delDir(savedir);	// ディレクトリとファイルを削除
		}
		return true;
	}
	/**
	 * 結局学生のポストしたファイル名は　学籍番号－連番.元の拡張子　というスタイルにした 2003.5.1
	 * ファイル名を 学籍番号-n.ext に整形する （n は連番）
	 * 
	 * タイピング課題ファイルでは，正しいファイル名でなければ null を返す
	 * 
	 * @param dir
	 * @param htb
	 * @return
	 */
	String getNewFile(String dir,Hashtable htb){
	    if(LOG.fa)	LOG.println("■StKadai#getNewFile()");
	    
		String oldFile  = (String)htb.get("_onlyFilename");
		String Trailer 	= getTriler(oldFile);				// abc.ckd.xyd ⇒ "xyd"   abc ⇒ ""   abc. ⇒ ""
		String stNum	= (String)htb.get("_stNumber");
		// dir は例えば /home/pc/kawaba/file/ck-2651/
		// ここにすでに同一のファイル名がないかどうかチェックする
		String newFile	=	"";
	    newFile	=	getNewName(stNum, Trailer, dir);

		return newFile;
	}
	/**
	 * 新しいファイル名を得る
	 * 課題によってはここをオーバーライドする
	 * ファイルは連番になる
	 * 
	 * @param stNum
	 * @param Trailer
	 * @param dir
	 * @return
	 */
	String	getNewName(String stNum, String Trailer, String dir){
	    if(LOG.fa)	LOG.println("■StKadai#getNewName()");
	    
	    /*
		 * ファイルリストを得る
		 */
	    File	dirFp	=	new	File(dir);
	    String	[]files	=	dirFp.list();
	    /*
	     * 空なら最初のファイル名を返す
	     */
	    if(files==null){
	        return	stNum + "-1"  + "." + Trailer;
	    }
	    /*
	     * 同じ学籍番号のファイル名をソートしてスタックに
	     */
	    int			n		=	files.length;
	    Arrays.sort(files);
	    java.util.Stack	stk			=	new	java.util.Stack();
	    

	    for(int	 i=0; i<n; i++){
	        /*
	         * ファイル名の学籍番号部分を取り出す
	         */
	        String	body		=	FileGear.fileNameBody(files[i]);
	        String	body_stNum	=	(new	Csv(body,"-")).get(0);
	        /*
	         * ファイル名の拡張子を得る（ないときは""をあてる）
	         */
	        String	ext		=	FileGear.getExt(files[i]);
	        if(ext==null){
	            ext="";
	        }
	        if(body_stNum.equals(stNum)&&ext.equals(Trailer)){
		        /*
		         *	abcd-1 → "1"
		         *	ファイル番号をスタックに積む 
		         */
		        Csv		cs		=	new	Csv(body,"-");
	            stk.push(cs.get(1));
	        }
	    }
	    /*
	     * 同じ学籍番号のファイルがなければ最初のファイル名を返す 
	     */
	    if(stk.empty()){
	        return	stNum + "-1"  + "." + Trailer;
	    }
	    /*
	     * 最大のファイル番号を取り出してそれから最大のファイル番号を得る
	     */
	    int		max		=	Integer.parseInt((String)stk.pop());
	    String	num		=	stNum + "-" + String.valueOf(max + 1) + "." + Trailer;
	    if(LOG.fa) LOG.println("fileName="+num);
	    return	num;
	    
	    
	    /*
	    int n = 1;
		String newFile;
		while(true){
			newFile  = stNum + "-" + String.valueOf(n) + "." + Trailer;
			//
			String test  = dir + newFile;
			File   fp    = new File(test);
			if(!fp.isFile()){
				break;
			}
			n++;
		}
		return newFile;
		*/
	}
	//
	//
	//
	String getTriler(String filename){
		Csv chk = new Csv(filename,".");
		if(chk.size() <= 1) return "";	// 拡張子がないとき
		//
		return chk.get(chk.size() -1);	// 最後の要素
	}
    //
    //    ans : 学生の解答画面の表示
    //
    //       parameter   teUid,kadaiKey,stNumber
    //
    //　　各課題ごとにディレクトリを作成し，各解答は一つのファイルとしてそこに書き込む．
    //　　 解答ファイルのパス＝ ／教員ディレクトリ／解答ディレクトリ／講義実施キー／課題キー／
    //  
    //　　（例）  /home/kawaba01/answers/103/00013/matsuda.ans  
	//					⇒	Parm.kadaiAnsDir(teUid,aplec_key,kadai_key) = /home/kawaba01/answers/103/00013/
    //  
    //    最初に，ディレクトリがあるかどうか調べ，無かったら作る
    //    次にファイルがあるかどうか調べ，あれば読みこんで訂正モードでの作業に移る．なければ
    //    新規作成とする．
    //
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
	 * レポート・ファイル提出・試験の課題を表示する.<br>
	 * インスタンスフィールドtempFlagを参照して、一時ファイルを読み込むかAnswerRecordを読込むかきめる
	 * 
	 * @param	editmode	表示モード（true なら編集、falseなら新規）
	 */
	public  void	display(boolean editmode){
		if(LOG.fa)	LOG.println("■ Sample #display(boolean editmode)");
		
		if(!editmode){
			putParameter(MESSAGE,"");
		}
		putInitdata();	// ファイル提出課題の初期表示データを作成しハッシュにセットする
		//
        // その他の項目をハッシュにセットする
        putKadaiTitle();	// タイトル
        putKadaiData();		// 実施期間
		putPointAndTime();	// 得点と提出日
		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
    }

	/**
	 * ファイル提出課題の初期表示データを作成しハッシュにセットする
	 */
	void	putInitdata(){
		
		Exam	exam	= new Exam(rec.content(),para.getEmlConfPath());
		
		/*
		 * グラフィックスのパスとURLを指定してHTMLを作成する
		 */
		String	graphicUrl	=	para.getAttachURL(teUid, lec_key, kadai_key);
		String	graphicPath	=	para.getAttachDIR(teUid, lec_key, kadai_key);
		exam.setImgPath( graphicUrl );
		exam.setImgDestinationPath( graphicPath );
		/*
		 * 資料データではないという設定をしておく
		 * これにより、ファイルへのリンクがイメージへのリンクと同じになる 
		 */
		exam.setNonHtmlFlag();		
		/*
		 * HTML の生成
		 * \ " ' はもとの文字にもどしておく 
		 */
		String	epmlToHtmlText	= Gear.toNormalString(exam.createHtml());
		htb.put("_answer",epmlToHtmlText);

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
	
	void putPointAndTime(){
		/*
		 * 得点と提出時間
		 */
		htb.put("_p",points);
		htb.put("_time",submitTime);

		//////////////////// 提出状況の可視化 //////////////////////////////
		putParameter("kubun",disposal);
		putParameter("con", KadaiInfo.MSG[Integer.parseInt(disposal)]);
		////////////////////////////////////////////////////////////////////
	}

	public void	write(String key,Vector exHtml){

		
		if(key.equals("RepotFileList")){
			
			RepotFileList(exHtml);
			
		}else if(key.equals("RepotFile_blankBLK")){
			RepotFile_blankBLK(exHtml);
			
		}
	}	
	void RepotFileList(Vector exHtml){		
		String[] fileList	=	getFileList();
		if(fileList==null){
			htb.put("_FileCounts","0");
			return;
		}
		int	n			=	fileList.length;
		htb.put("_FileCounts",String.valueOf(n));
		//
		for(int i=0; i<n; i++){
			htb.put("_fname", fileList[i]);
			htb.put("_fileUrl", getPostUrl()+fileList[i]);
			//
			printVector(exHtml);
		}
	}
	// 部分的に切り取ったソースデータ（exHtml）を出力するか否か、ハッシュテーブル
	// (htb)を使って判断し、出力する場合には、htb を使ってexHtml の内容を書き換えて
	// 出力する．個々の処理内容は、key で特定される．
	//
	public void	RepotFile_blankBLK(Vector exHtml){
	
		int	n	= Integer.parseInt( strHash(htb,"_FileCounts") );// 既に表示した行があるか
		if(n > 0)	return;
		//
		printVector(exHtml);
		return;
	}

	/**
	 * ポストしたファイル名のリストを得る 
	 * @return　 特定の学生のポストしたファイル名リストの配列．なければnullを返す
	 */
	public	String[] getFileList(){
		String  filePath 	= para.getKadaiPostDir2(teUid,aplec_key,kadai_key);
		Files	fl			= new Files(filePath);						// 提出ファイルを処理するクラス
		String [] files 	= fl.list(stNumber);
		return	files;			
	
	}
	/**
	 * ポストされたファイルのあるディレクトリへのURLを得る<br>
	 * サーバシステムのドメインではドメイン名でなくIPアドレスで返す
	 * @return　ポストされたファイルのあるディレクトリへのURL
	 */
	public	String	getPostUrl(){
		//
		String aRef		= 	"";
		String product	=	para.getViewSwitch();
		aRef		=	para.getKadaiPostURL(teUid,aplec_key,kadai_key); // URLを返す
		return	aRef;		
	}
}
