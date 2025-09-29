/*
     Power Campus Calender

*/
package export;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Vector;
import database.Database;
import database.DbConnectionBroker;
import database.KeyGen;
import framework.LOG;
import framework.Param;
import framework.SuperPlayer;
import kadai.KadaiDefRecord;
import kamoku.KamokuDefRecord;
import kamoku.KamokuItem;
import kamoku.KamokuParser;
import kamoku.KamokuRevParser;
import kamoku.KamokuSecDefRecord;
import kamoku.KamokuVar;
import refer.ReferenceDefRecord;
import tktools.Csv;
import tktools.FileGear;
import tktools.TemplateBox;
import tktools.TextToken;
import tktools.UnZip;
import xmlparser.xmlException;

/**
 * 
 	#
	# ##################
	#   import
	# ##################
	#
	<program $export.Import>
		<dispatch  html=import.html  number=205  class=export.Import />
		<variable>
		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA/>
		  <accept    CMD    UPLODE  />
		  <keep      />
		  <form      />
		</variable>
	</program> 
 *
 */

public class Import extends SuperPlayer implements KamokuVar{

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
	
	/**
	 * インポートファイルへのフルパス
	 */
	String	importFilePath;
	
	/** 教師id */
	String teUid;

	/** グループid */
	String szDB;
	
	/** 新しい講義キー */
	String	lec_key;	
	
	/** 新しい教師＋講義キー */
	String	te_lec_key;		


	public Import(){
		super();
		if(LOG.fa) LOG.println("■ Import #コンストラクタ");
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
		if(LOG.fa) LOG.println("■ Import #initialize()");
		broker	=	getDbConnection();
		db		=	new Database(broker);
		
		teUid	=	getParameter(TUID);
		szDB	=	getParameter(GROUP);

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
		if(LOG.tr) LOG.outHash(htb,"■Import #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		if(cmd.equals("IMPORT")){
			if(LOG.tr) LOG.println("class exwork #sub_import() [IMPORT]: インポート処理 の先頭です");
			//
			// ファイルを受け取る
			boolean	result	=	importFile();
			if(result){
				// データを再配置する
				resetKamoku();
			}
			
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示

		}else if(cmd.equals("ERROR_FILE")){
			htb.put("_msg","★ ファイル名を指定してください");
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示

		}else if(cmd.equals("RETURN")){
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;	// 復帰			

		}else{
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示			
		}
		/*
		 * 次に表示するときあるいはリターンしてきた時の表示モードをセットしてからリターンする
		 */
		putParameter(DISP_KEY,disp_mode);
		return	ret;
	}
	//
	// 科目定義ファイルの受け取り
	//
	boolean importFile(){
		String method = "■import #importFile() : ";
		if(LOG.fa) LOG.println(method + ": 科目定義ファイルの受け取り の先頭です");
		//
		// マルチパート入力で受け取った変数を得る
        String savedir   = getParameter(UPLOAD_DIR_NAME);  		// 一時的格納場所 2003.6.15
        String mfilename = getParameter(UPLOAD_FILE_NAME);		// ファイル名のみ
        String mailFile  = getParameter(UPLOAD_FILE_PATH);		// savedir + onlyFilename == 一時記録ファイルへのフルパス
        String files	 = getParameter(UPLOAD_COUNTS);			// マルチパートで読み込んだファイル数
		//
		boolean eflag    = false;
		int		fcnt	 = Integer.parseInt(files);
		//
        if(mfilename!=null){
        	if(LOG.fa) LOG.println(method + "科目定義ファイル名/ file name =" + mfilename);
			//
			String ext	= getTriler(mfilename);
			if( !ext.equals("zip") ) {
				if(LOG.fa) LOG.println(method + "ファイルの種類が違います．インポートするのは科目定義ファイルでなくてはなりません/ file name = " + mfilename);
				htb.put("_msg","★ ファイルの種類が違います．インポートするのは科目定義ファイルでなくてはなりません");
               	eflag = true;
			}else{
				//
				File mfp   = new File(mailFile);
    	        if(mfp.length()==0){    // （長さがゼロならファイル名の間違い）
	    	        if(LOG.fa) LOG.println(method + "指定された科目定義ファイルは存在しません/ file name = " + mfilename);
            	    htb.put("_msg","★ 指定された科目定義ファイルは存在しません");
                	eflag = true;
	            }
    	    }
		}else{
			eflag = true;
			htb.put("_msg","★ 科目定義ファイルを指定してください");
			if(LOG.fa) LOG.println(method + "科目定義ファイルを指定してください");
        }
        //
        if(eflag){// エラーありの場合
            if(LOG.fa) LOG.println(method + "エラーがありましたので科目定義ファイルを取得できませんでした");
			//
			// もし送信された差込ファイルがあれば削除する
            if((mailFile != null)&&(mailFile.length() > 0)){
                File saveDir    = new File(savedir);
                if(saveDir.exists()){
                    deleteDir(saveDir);
                }
            }
            return false;
        }// ** ここで終わり **
        //
        // もし送信された科目定義ファイルがあれば正規のディレクトリに移動する
		if((mailFile != null)&&(mailFile.length() > 0)){
            File 	mfp    = new File(mailFile);			// 一時格納場所（ここにある）
            String  dir    = para.importDir(teUid);			// インポート用ディレクトリのフルパス ex.  /home/pc/kawaba01/import/
			File    moveTo = new File(dir);
			if(!moveTo.isDirectory()){	// ディレクトリがなければ作る
				moveTo.mkdirs();
			}else{
				deleteFiles(moveTo);	// 移動先の(dir)のファイルを全て消してから移動処理を行う
			}
            //
            boolean flag   = mfp.renameTo(new File(moveTo,mfp.getName()));	// 移動処理
           	deleteDir(new File(savedir));									// 作業ディレクトリの消去
            //
			if(!flag){
               	LOG.errStop("KamokuApRecord class #addMeibo() :指定されたファイルをコピーできない");
				return false;
            }
            // インポートファイルへのフルパスを記憶しておく
			importFilePath	=	para.importDir(teUid) + mfilename;
			if(LOG.fa) LOG.println(method + "正常にファイルを格納しました/ FILE NAME = " + importFilePath );
        }
		//
		return true;
	}
	//
	// ファイル拡張子を取り出す
	String getTriler(String filename){
		Csv chk = new Csv(filename,".");
		if(chk.size() <= 1) return "";	// 拡張子がないとき
		//
		return chk.get(chk.size() -1);	// 最後の要素
	}

	/**
	 *  新しい講義定義キーを作成する
	 */
	void	createKeys(){
		KeyGen	kg	= new KeyGen(teUid,db);
		lec_key		= kg.nextLec();	
		te_lec_key	= KeyGen.get_te_lec_key2(teUid,lec_key);	
	}
	
	/**
	 * 科目定義データを再配置する
	 * 
	 */
	void resetKamoku(){
		if(LOG.tr) LOG.println("■import #resetKamoku() : 科目定義データを再配置する の先頭です");
		/*
		 * 新しいキーを作成して保持する
		 * lec_key, te_lec_key
		 */
		/////////////
		createKeys();
		/////////////

		// zipファイルをimport作業ディレクトリに解凍する
		UnZip	uz	=	new	UnZip(importFilePath, para.importDirName(teUid));
		uz.unzip();
		
		if(LOG.tr) LOG.println("★解凍完了");
		
		String	path;
		
		// 科目関連データを（zipを解凍することによって）ファイルシステムにコピーする
		/*
		 * 資料emlファイル
		 */
		path	=	para.importDir(teUid)+KAMOKU_EML_FILE;
		if(isExist(path)){// 上のファイルはない場合があるので、存在を確認してから処理する
			UnZip	eml		=	new	UnZip(path, para.getEpmlPathName(teUid, lec_key));
			eml.unzip();
		}
		
		if(LOG.tr) LOG.println("★資料復元完了");
		
		/*
		 * 資料htmlファイル
		 */
		path	=	para.importDir(teUid)+KAMOKU_HTML_FILE;
		if(isExist(path)){	
			UnZip	html	=	new	UnZip(path, para.getHtmlPathName(teUid, lec_key));
			html.unzip();
		}
		if(LOG.tr) LOG.println("★資料ファイル復元完了");
		
		/*
		 * 課題関連グラフィックス
		 */
		path	=	para.importDir(teUid)+KAMOKU_GRPH_FILE;
		if(isExist(path)){	
			UnZip	grph	=	new	UnZip(path, para.getKamokuAttachPathName(teUid, lec_key));
			grph.unzip();
		}
		if(LOG.tr) LOG.println("★講義関連グラフィックス復元完了");
		
		// ■■ 科目定義ファイルから科目定義情報を復元する ■■
		Course	csr			= readCourseFile(para.importDir(teUid)+KAMOKU_DEF_FILE);	// 科目定義ファイル名 kamoku.def（固定）
		
		if(csr==null) {
			LOG.print("科目定義情報 csr が null");
		}else {
			LOG.print("科目定義情報 csr は nullでない");
		}
		
		if(LOG.tr) LOG.println("★科目定義情報復元完了");
		
		//
		/*
		 *  1. 科目定義レコード
		 */
		exKamoku		ekm		= csr.get_kamoku();
		ekm.keyEdit(teUid,lec_key);	// 新しいキーに書き換え
		KamokuDefRecord	kdrec	= new KamokuDefRecord( ekm.contents() );
		kdrec.insert(db);
		
		if(LOG.tr) LOG.println("★科目定義レコード復元完了");
		/*
		 * 1.1 シラバスタイトル
		 */
		String			title	=	kdrec.title();
		putParameter("title", title);
		
		/*
		 * 1.2 シラバスXMLをチェック更新
		 */
		KamokuParser	kp		=	getKamokuParser(kdrec.content(), kdrec);
		if(kp==null){
			alart(kdrec);// システムのxmlをセットしてDBに書く
			kp		=	getKamokuParser(kdrec.content(), kdrec);// kdrec.content()はalart()で更新済み
		}
		if(LOG.tr) LOG.println("★科目パーサを取得した");
		
		String	serial	=	"";
		try{
			serial	=	para.getKamokuSerial(szDB, teUid);
		}catch(xmlException e){
			
		}
		if(LOG.tr) LOG.println("★科目定義レコード復元完了");
		/*
		 * 1.3 正しいシリアルのXMLに修正
		 */
		kp.setSerial(serial);
		KamokuRevParser	krp		=	new	KamokuRevParser(kp, para.kamokuTemplatePath());
		String			newXml	=	krp.getXml();
		kdrec.set_content( newXml );
		kdrec.update(db);
		
		/*
		 * 2. セクションレコード
		 */
		Vector	sections	= csr.get_Sections();
		int		n			= sections.size();
		for(int i=0; i<n; i++){
			exSection esc	= (exSection) sections.get(i);
			esc.keyEdit(te_lec_key);
			KamokuSecDefRecord ksdf	= new KamokuSecDefRecord(esc.contents());
			ksdf.insert(db);
		}
		
		/*
		 * 3. 資料レコード
		 */
		Vector	refs		= csr.get_References();	// 全てのリファレンスレコード
		int		n1			= refs.size();
		for(int i=0; i<n1; i++){
			exReference erf	= (exReference) refs.get(i);
			erf.keyEdit(te_lec_key, lec_key);
			ReferenceDefRecord rfRec	= new ReferenceDefRecord(erf.contents());
			rfRec.insert(db);
		}
		
		/*
		 * 4. 課題レコード
		 */
		Vector	kadais		= csr.get_Kadai();
		int		n2			= kadais.size();
		for(int i=0; i<n2; i++){
			exKadai ekd	= (exKadai) kadais.get(i);
			ekd.keyEdit(te_lec_key);
			KadaiDefRecord kdRec	= new KadaiDefRecord(ekd.contents());
			kdRec.insert(db);
		}
		
	}
	/**
	 * ファイルがあるかどうか調べる
	 * @param path
	 * @return
	 */
	public	boolean	isExist(String path){
		File	fp	=	new	File(path);
		return	fp.exists();
	}
	//
	// 科目定義データを読み込む
	//
    public Course readCourseFile(String fname){
		if(LOG.tr) LOG.println("■import #readCourseFile() : 科目定義データを読み込む の先頭です");
		if(LOG.tr) LOG.println("          fname = " + fname);
		//
		ObjectInputStream  objIn  = null;
        Course crs = null;
        try{
            objIn = new ObjectInputStream(new FileInputStream(fname));
            try{
                crs = (Course)objIn.readObject();
                objIn.close();
            }catch(ClassNotFoundException e1){
            	LOG.println("ClassNotFoundException:" + e1);
                crs = null;
            }catch(IOException e2){
            	LOG.println("IOException:" + e2);
                crs = null;
            }
        }catch(IOException e3){
        	LOG.println("can't Open :IOException:" + e3);
            crs = null;
        }
        return crs;
    }
    
	/**
	 * 所余のxmlから科目パーサーを作成して返す<br>
	 * @param xml
	 * @param kdrec
	 * @return
	 */
	KamokuParser	getKamokuParser(String	xml, KamokuDefRecord kdrec){
		if(LOG.fa)	LOG.println("■ import #getKamokuParser()");
		KamokuParser	kp	=	null;
		try{
			kp	=	new	KamokuParser(xml);
			
		}catch(xmlException e){
			return null;
			
		}
		return	kp;
		
	}
	/**
	 * 何らかの原因で受け取ったデータのXMLが不正なものだった場合、
	 * 現在の新しいシラバスXML（値は未記入）を content にセットしてデータベースを更新する
	 * それにも誤りがあれば、システムのXMLをcontent にセットしてデータベースを更新する
	 */
	void	alart(KamokuDefRecord kdrec){
		if(LOG.fa)	LOG.println("■ import #alart()");
		try{
			kdrec.set_content( para.getKamokuXml(szDB, teUid) );
			
		}catch(xmlException e){
			putParameter(MESSAGE, "★システムの科目定義XMLが正しい記述ではありませんのでシステムのXMLを代用しました．<br>" + e.getMessage());
			kdrec.set_content( FileGear.getFileData(para.syllabusPath()) );
		}
		kdrec.update(db);
		
	}
/////////////////////////////////////////////////////////////////////////////////////////////////////
//
//     出　　力　　処　　理
//
/////////////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 出力処理
	 */
	@Override
	public  void	display(boolean editmode){
		if(LOG.fa)	LOG.println("■ import #display(boolean editmode)");
		
		if(!editmode){
			htb.put(MESSAGE,"");
		}
		
		/* strHash(htb,DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);	
	}

	@Override
	public	void	write(String key){
		if(key.equals("line")){
			line();
			
		}else if(key.equals("kamokuDsp")){
			kamokuDsp();

		}else if(key.equals("items")){
			items();
			
		}
	}
	/**
	 * 区切り線を引く
	 *
	 */
	void	line(){
		// lec_keyがnullであればまだインポート前である．
		// 初期表示処理ではラインは表示しない．
		if(lec_key==null){
			return;	
		}
		TemplateBox	tbox	=	new	TemplateBox(para.kamokuTemplatePath());
		Vector		v		=	tbox.getVector("line");
		printVector(v);
		
	}
	void	kamokuDsp(){
		// lec_keyがnullであればまだインポート前である．
		// 初期表示処理ではシラバスは表示しない．
		if(lec_key==null){
			return;	
		}
		TemplateBox	tbox	=	new	TemplateBox(para.kamokuTemplatePath());
		Vector		v		=	tbox.getVector("kamokuDsp");
		printVector(v);		
	}
	/**
	 * 科目シラバスの各項目を出力する
	 *
	 */
	void	items(){
		if(LOG.fa) LOG.println("■Import#listItems()");		
		
		// lec_keyがnullであればまだインポート前である．
		// 初期表示処理では科目シラバスは表示しない．
		if(lec_key==null){
			return;	
		}
		// データベースから講義定義レコードを読み込み、content(講義シラバスXML)を得る
		KamokuDefRecord	kdrec	= 	new KamokuDefRecord(teUid, lec_key, db);
		String	xml				=	kdrec.content();
		
		// xmlから科目パーサを作成し定義情報を順次出力する
		KamokuParser	kp		=	getKamokuParser(xml, kdrec);
		if(kp==null){
			putParameter(MESSAGE, "★科目定義XMLに誤りがありましたのでシステムのXMLファイルを代用しました．");
			alart(kdrec);
			return;
			
		}else{
			printList(kp);
		}
	}
	void	printList(KamokuParser	kp){
		if(LOG.fa) LOG.println("■Import#printList()");		
		
		TemplateBox	tbox	=	new	TemplateBox(para.kamokuTemplatePath());
		while(kp.hasNext()){
			KamokuItem	ki	=	(KamokuItem)kp.next();
			putParameter("label"	, ki.getLabel());
			
			if(ki.isTextArea() || ki.isTextField()){
				putParameter("content"	, ki.getText());
				Vector	v	=	tbox.getVector("viewTemplate01");
				printVector(v);
				
			}else if(ki.isListBox() || ki.isCheckBox()){
				/*
				 * 選択肢のうち、先頭に＊が付いているもののみを取り出して連結する
				 * ＊はその項目が選択されたことを意味する
				 */
				TextToken		tkn		=	new TextToken(ki.getText());
				String			item	=	"";
				StringBuffer	buf		=	new	StringBuffer();
				boolean		spcflag	=	false;
				while((item=tkn.getNext())!=null){
					if(item.charAt(0)=='*'){
						if(spcflag){
							buf.append(" ");
						}
						buf.append(item.substring(1));
						spcflag	=	true;
					}
				}
				putParameter("content"	, buf.toString());
				
				Vector	v	=	StringToVector(tbox.get("viewTemplate02"));
				printVector(v);
			}
		}
		
	}

}

