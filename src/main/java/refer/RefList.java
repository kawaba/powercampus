
package refer;

import java.io.*;
import java.util.*;

import kamoku.KamokuSecDefRecord;
import cabinet.*;
import export.*;
import	tktools.*;
import database.*;
import framework.*;
import	kamoku.*;

/**
 * 資料一覧・作成・割付
 * 
 * 資料を一覧し、割付を行なう．
 * タイトルを入力して新規作成も行なう．
 * 
 * 
 	#
	# ##################
	#   RefList
	# ##################
	#
	<program $refer.RefList>
		<dispatch  html=refList.html  number=400  class=refer.RefList />
		<variable>
		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA lec_key sect_key title />
		  <accept    CMD    ref_key past_ref_seq />
		  <keep      ref_seqNum past_ref_url/>
		  
		  <work      />
		  <form      ref_shubetsu  ref_title  ref_url  ref_seqNum_%_n% _assign_%_n% />
		</variable>
	</program>  
 *
 * 1.accept 変数
 *	ref_key			（削除または編集が選択されたとき）資料キー
 *	past_ref_seq	（順序変更が選択されたとき）変更前のシーケンス番号
 *	
 *
 * 2.keep 変数
 *  ref_seqNum		編集中の資料のシーケンス番号
 *	past_ref_url	書き込み時にURLを変更したかどうかをチェックするため保存しておく
 *
 * 2.form 変数
 *  ref_shubetsu	作成・編集する資料の種別	
 *  ref_title		作成・編集する資料のタイトル
 *  ref_url			作成・編集する資料のURL
 *  ref_seqNum_%_n%	順序を変更する資料のシーケンス番号（%_n% の部分は 1 からの番号）
 *  assign_%_n%		割り付ける資料のシーケンス番号（%_n% の部分は 1 からの番号）
 *
 */
public class RefList extends SuperPlayer implements CBvar{
	
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
	
	public	RefList(){
		super();
		if(LOG.fa) LOG.println("■ RefList #コンストラクタ");
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
		if(LOG.fa) LOG.println("■RefList #initialize()");
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
	public	String	dispatch(){
		//if(DBG.fa) DBG.outHash(htb,"■RefList #dispatch()");
		if(LOG.fa) LOG.println("■RefList #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		if(LOG.fa) LOG.println("■RefList #dispatch(): cmd=" + cmd);

		if( cmd.equals("SENDFILE") ){
			makeZip();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示
			
		}else if(cmd.equals("CABINET")){
			ret			=	"$cabinet.FileCabinet";
			disp_mode	=	DISP_NEW;				

		}else if(cmd.equals("CLEAR")){
			clearRefRecord();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示
			
			
		}else if(cmd.equals("UPDATE")){
			if(!update()){
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;	// 再表示

			}else{
			    clearRefRecord();
				disp_mode	=	DISP_NEW;
				ret			=	DISPATCH_DEFAULT;	// 再表示
			}

		}else if(cmd.equals("RETURN")){
			// WEB作成資料を登録していて実際のWEBを作成していない場合どうするか
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;	// 復帰	
			
		}else if(cmd.equals("EDITOR")){
			boolean	flag	=	openEditor();
			if(flag){
				// 必要はないが受け渡しをはっきりするため記述する
				putParameter("ref_title",getParameter("ref_title"));
				
				disp_mode	=	DISP_NEW;
				ret			=	"$refer.RefEditor";				

			}else{
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;	// 再表示
			}
		
		}else if(cmd.equals("ORDER")){
			order();
			/*
			 * 表示のやり直し．
			 * ref_key が残っていない場合はeditRefRecord()でfalseが帰るので
			 * clearRefRecord()で画面表示をクリアする
			 */
			if(!editRefRecord()){
				clearRefRecord();
			}
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示

		}else if(cmd.equals("ASSIGN")){
			/*
			 * 割り付けの更新処理
			 * セクションレコードの資料リストフィールドを更新する
			 */
			updateSecRefField();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示

		}else if(cmd.equals("EDIT")){
			editRefRecord();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示
			
		}else if(cmd.equals("DELETE")){

		    /*
			 * 削除対照の資料定義レコードから，そのシーケンス番号を得る
			 */
		    ReferenceDefRecord	rdf			=	getRefDefRecord();
			String				targetSeq	=	rdf.seq_number();
			/*
			 * 他の資料レコードのシーケンス番号を調整する
			 */
			updateSequence(targetSeq);
			
			/*
			 * 資料を削除する
			 */
			delete();
			/*
			 * この科目の全てのKamokuSectionDefRecordについて、資料キーフィールドから
			 * 削除した資料キーを消去しておく
			 */
			updateKamokuSections(getParameter("ref_key"));
			
			
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示

		}else{
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 復帰			
	
		}
		/*
		 * 次に表示するときあるいはリターンしてきた時の表示モードをセットしてからリターンする
		 */
		putParameter(DISP_KEY,disp_mode);
		return	ret;
	}
	/*
	 * キーを引いて資料定義レコードを得る
	 */
	ReferenceDefRecord	getRefDefRecord(){
		
	    String		te_lec_key	= KeyGen.get_te_lec_key2( getParameter(TUID), getParameter("lec_key") );
		String		ref_key		= getParameter("_ref_key");
		if(isEmpty(ref_key)){
			return null;
		}
		ReferenceDefRecord ksd 	= new ReferenceDefRecord( te_lec_key, ref_key, db);	// データベースを引いて生成
		return		ksd;
	}
	/**
	 * レコードを編集領域へコピーする
	 * 
	 * @param htb
	 */
	boolean editRefRecord(){
		if(LOG.fa) LOG.println("■RefList #editRefRecord()");
		//
		String		te_lec_key	= KeyGen.get_te_lec_key(htb); // te_lec_key は常にこれで作ること
		String		ref_key	= getParameter("_ref_key");
		if(isEmpty(ref_key)){
			LOG.println("★RefList #editRefRecord()：ref_key が空です");
			return false;
		}
		ReferenceDefRecord ksd 	= new ReferenceDefRecord( te_lec_key, ref_key, db);	// データベースを引いて生成
		//
		String seq 		= ksd.seq_number();
		String seq_old	= new String( seq );
		putParameter("_ref_seqNum"	 ,seq);
		putParameter("_past_ref_seq" ,seq);	// シーケンス番号を変えたかどうか判断するため覚えておく
		//
		putParameter("_ref_key"		,ksd.ref_key());
		putParameter("_ref_shubetsu",ksd.shubetsu());
		putParameter("_ref_title"	,ksd.title());
		putParameter("_ref_url"		,ksd.url());
		/*
		 * 書き込み時にURLを変更したかどうかをチェックするため
		 * 保存しておく
		 */
		putParameter("_past_ref_url",ksd.url());
		return true;
	}	
	/**
	 * セクションレコードの資料リストフィールドを更新する
	 * 
	 */
	void 	updateSecRefField(){
		if(LOG.fa) LOG.println("■RefList #updateSecRefField()");
		//
		// この科目に属する全ての資料レコードを得る
		String			te_lec_key	=	KeyGen.get_te_lec_key2(getParameter(TUID), getParameter("lec_key"));
		ReferenceDEF	refDef 		= 	new	ReferenceDEF(te_lec_key, db);
		int				n 			=	refDef.size();
		Vector  		reflist 	=	new Vector(20,10);
		//
		// "YES"とされた資料の資料キーを取り出し、Vector に入れる
		for(int i=0; i<n; i++){
			String cb 			= "_assign_" + String.valueOf(i+1);	// １オリジンで
			String cb_value		= getParameter(cb);
			if( cb_value.equals("YES")){
				
				reflist.add(refDef.ref_key(i));	// 第i番目の資料キーをリストに加える
			}
		}
		if(LOG.fa) LOG.outVector(reflist,"□ 割り付け資料のリスト");
		// 現在のセクションレコードを得る
		String sect_key			= getParameter("_sect_key");
		KamokuSecDefRecord rec	= new KamokuSecDefRecord( te_lec_key, sect_key, db);
		//
		// 登録された資料があればリストとしてこのセクションレコードに登録する
		if(reflist.size() > 0){
			rec.set_ref_list(reflist);// Vector から登録できる＜完全な書き換えになる＞
		}else{
			rec.set_ref_list("");// リストを消去するため（文字列からも登録できる）
		}
		rec.update(db);	// 更新を書き込む
	}
	/**
	 *	課題の順序を並べ替える
	 *	seq_old は変更前の表示の並び番号
	 */
	void	order(){
		if(LOG.fa) LOG.println("■RefList #order()");
		
		String	seq_old		= 	getParameter("_past_ref_seq");
		String	seq			= 	getParameter("ref_seqNum_" + seq_old);
		changeSequence(seq, seq_old);
	}
	/**
	 * シーケンス番号のチェックと更新    ※ htb にも
	 *　　番号を挿入すると全体のシーケンス番号を書き換える必要がある
	 *
	 *    全部のシーケンス番号を得て
	 *
	 *     INSERTモードのとき
	 *         編集モードなら
	 *            ０．番号を変えていなければ何もしない
	 *　　　　　　１．今回の番号が飛び番号になっている（エラー）⇒　最終番号にした上で他のものの番号をずらす
	 *        　　２．今回の番号がすでにある番号と重なっている　⇒　重なる番号のデータはシーケンスをひとつずらす
	 *
	 *         新規作成なら
	 *			  １．今回の番号が飛び番号になっている（エラー）⇒　最終番号にするだけ
	 *            ２．今回の番号がすでにある番号と重なっている　⇒　重なる番号のデータはシーケンスをひとつずらす
	 *
	 *      DELETEモードのとき
	 *      　１．対象レコードより下のシーケンスのレコードは、シーケンス番号を１減らす
	 * 		　２．ただし、最後のレコードの場合は、何もしない
	 * 
	 * @param htb
	 * @param mode
	 * @return
	 */
	void changeSequence( String seq, String seq_old){
		if(LOG.fa) LOG.println("■RefList #changeSequence()");

		String			te_lec_key	=	KeyGen.get_te_lec_key2(getParameter(TUID), getParameter("lec_key"));
		ReferenceDEF 	ksd			= 	new ReferenceDEF(te_lec_key,db);
		int 			max			=	ksd.size();
		int 			seqNum		=	Integer.parseInt(seq);
		if(seqNum >= max + 1){
			seqNum = max;	// 最大番号にして以下の処理を適用する
		}
		int seqNum_old	= Integer.parseInt(seq_old);
		//
		if(LOG.fa) LOG.println("□RefList #changeSequence()");
		if(LOG.fa) LOG.println("           seq_old = " + seq_old);
		if(LOG.fa) LOG.println("           seq 　　= " + seq);
		//
		if(seqNum == seqNum_old){
			return;
			
		}else if(seqNum < seqNum_old){
			for(int k = seqNum; k<seqNum_old; k++){	// new から old-1 までのレコードのシーケンス番号を＋１する
				int  				N	= getRecordNumber(k);				// シーケンスは１オリジンだが、レコードは０オリジンなので－１する
				ReferenceDefRecord rec 	= ksd.get(N);
				rec.set_seq_number( get000String( String.valueOf(k+1) ) );	// 00 形式で書き込むこと
				rec.update(db);
			}
			
		}else if(seqNum_old < seqNum){
			for(int k = seqNum_old + 1; k<=seqNum; k++){		// new+1 から old+1 までのレコードのシーケンス番号を－１する
				int 				N 	= getRecordNumber(k);	// シーケンスは１オリジンだが、レコードは０オリジンなので－１する
				ReferenceDefRecord rec 	= ksd.get(N);
				rec.set_seq_number( get000String (String.valueOf(k-1) ) );	// 00 形式で書き込むこと
				rec.update(db);
			}
		}
		int N = getRecordNumber(seqNum_old);			// シーケンスは１オリジンだが、レコードは０オリジンなので－１する
		ReferenceDefRecord rec = ksd.get(N);
		rec.set_seq_number( get000String( String.valueOf(seqNum) ) );	// 00 形式で書き込むこと
		rec.update(db);		
		return	;
	}
	
	/**
	 * 記入データを記録する
	 *
	 */
	boolean 	update(){
		if(LOG.fa) LOG.println("■RefList #update()");
		
		String		shubetsu	=	getParameter("ref_shubetsu");	// do_updateで消されるので先に得ておく
		boolean 	ret 		= 	doUpdateRefRecord(htb);			// 更新
		if(!ret){
			putParameter(MESSAGE, "★ 記入されてない項目があります");
			return	false;
		}else{
			if(LOG.fa) LOG.println("shubetst="+ shubetsu);
			if(shubetsu.equals(ReferenceDefRecord.REF_HTML)){
				putParameter(MESSAGE, "■WEB教材を作成するには<img src='/pc/images/webSymbol.gif' align='absmiddle' >をクリックしてください．");
				
			}
			return	true;
		}
		
	}
	/**
	 *  EPML エディタを開くための処理
	 */
	public boolean openEditor(){
		if(LOG.fa) LOG.println("■RefList #openEditor()");
		
		String	shubetu	= getParameter("_ref_shubetsu");
		if(!shubetu.equals(ReferenceDefRecord.REF_HTML)){
			putParameter("_msg","★ WEBを作成するには、種別を「作成Web教材」としてください");
			return	false;
		}
		
		String	title	= getParameter("_ref_title");
		if(isEmpty(title)){
			putParameter("_msg","★ WEBを作成するには、先にタイトルを入力してください");
			return	false;
		}
		/* 
		 * ここで資料レコードをデータベースに登録しておく
		 * 
		 * ref_key がないとこの後の作業がやりにくいのでここで登録してしまう
		 * 新規の場合、url は一時的に INTERNAL_FILE と書いておいて、新規登録処理に実際のファイルを指すよう書き換える
		 */
		String	ref_key	= getParameter("_ref_key");
		if(isEmpty(ref_key)){
			/* ref_urlにファイル名など指定してなく空白ならINTERNAL_FILEを設定する
			 * ファイル名を指定してある時はそれを残す
			 */
			String	temp	=	getParameter("_ref_url");
			if(isEmptyData(temp)){
				putParameter("_ref_url","INTERNAL_FILE");
			
			}else{
				/* ファイル名が指定してある場合は先頭に*印を付加し、他のURLと区別できるようにしておく
				 * doUpdateRefRecord()では、WEB参照などがありこれらのURLと区別するため
				 */
				putParameter("_ref_url","*" + temp);
			}
		}
		/*
		 * 
		 */
		boolean ret 	= doUpdateRefRecord(htb);	// 更新
		if(ret){
		    return	true;	// editor を起動してよい
		}else{
		    return	false;
		}
		    
	}
	/**
	 * 表示データをクリアする
	 *
	 */
	void clearRefRecord(){
		if(LOG.fa) LOG.println("■RefList #clearRefRecord()");
		
		// 表示用データをクリア
		putParameter("_ref_seqNum"	 ,"");
		putParameter("_past_ref_seq"  ,"");		// シーケンス番号を変えたかどうか判断するため覚えておく
		//
		putParameter("_ref_key"	,"");
		putParameter("_ref_shubetsu"	,"");
		putParameter("_ref_title"	,"");
		putParameter("_ref_url"		,"");
		
		// リストボックスの表示
		int shubetsu = 0; // 絶対に合わない
		int	n		 = ReferenceDefRecord.REF_NUM;
		for(int i=0; i<n; i++){
			String key	= "_lbx" + String.valueOf(i+1);
			if(i!=shubetsu){
				putParameter(key,"");
			}else{
				putParameter(key,"checked");
			}
		}
		return;
	}
	/**
	 * この科目で作成した全てのWEB教材をZIPにして
	 * ファイルキャビネットに置く
	 * 
	 * @return
	 */
	void	makeZip(){
		if(LOG.fa) LOG.println("■RefList #makeZip()");
		
		String			teUid		=	getParameter(TUID);
		String			lec_key		=	getParameter("lec_key");
		KamokuExport	ke			=	new	KamokuExport(teUid, lec_key, db, para);
		String			fileName	=	ke.writeHtmlZip();
		if(!isEmpty(fileName)){
			
		    uploadZip(ke);
			
			putParameter(MESSAGE, "■この科目で作成した全てのWEB教材をファイルキャビネットに登録しました．");

		}else{
			putParameter(MESSAGE, "■この科目には作成済みのWEB教材はありません．");
		}
	}
	/**
	 * WE教材のアーカイブをファイルキャビネットに登録する
	 * 
	 * すでに登録してある時は更新する
	 * そうでなければ新規登録する
	 * 
	 * @param ke
	 */
	/**
	 * ファイルキャビネットデータベースに新規または更新で登録する
	 *
	 */
	void	uploadZip(KamokuExport ke){
		/*
		 * 重複を調べる
		 */
		Hashtable	tb		=	mkUpdateRecord(ke.getHtmlZipFilename());

		if(tb==null){
		    createDB(ke);
		    
		}else{
		    updateDB(tb);
		}		
	}
	/**
	 * 更新レコードを得る
	 * 存在チェックを兼ねる
	 * 
	 * @param fname
	 * @return
	 */
	Hashtable	mkUpdateRecord(String fname){
		if(LOG.fa)	LOG.println("■ RefLidt #mkUpdateRecord() :" + fname);
		
		Hashtable	record	=	new	Hashtable(30);
		CBdatabase	cbd		=	new CBdatabase(broker, getParameter(GROUP));
		int			count	=	cbd.readRecord(fname, record);
		if(count==0){
		    return	null;
		}
		// 更新日付の書き換え
		String		from	=	DateGear.getLongDateString();	// yyyyMMddHHmmss (14桁)
		putParameter(record, FROM, from.substring(0,12));		// yyyyMMddHHmm(12桁)
		
		return	record;
	}	
	/**
	 * 日付を更新してレコードをアップデートする
	 * 
	 * zipファイルはすでに新しく作成されているので、日付を更新するだけでいい
	 * @param ke
	 */
	void	updateDB(Hashtable record){
	    
	    String		seqkey	=	getParameter(record, KEY);
		CBdatabase	cbd		=	new CBdatabase(broker, getParameter(GROUP));
		cbd.update(seqkey, record);	    
	    
	}
	
	void	createDB(KamokuExport ke){
	    
	    Hashtable	record	=	makeCabinetRecord(ke);
		// レコードを登録、IDによるキーレコードも登録
		CBdatabase 	cb		=	new	CBdatabase(broker, getParameter(GROUP));
		cb.insertByid(record, getParameter(TUID));
	    
	    
	}
	public Hashtable makeCabinetRecord(KamokuExport ke){
		if(LOG.fa) LOG.println("■RefList #addCabinet()");
		
		Hashtable	tb		=	new	Hashtable();
		CBkey		cbkey	=	new	CBkey(broker);
		String		key		=	cbkey.getNextNumber(getParameter(GROUP));	
		File		fp		=	new File(ke.getFilePath());
		long		len		=	(fp.length() + 512 )/ 1024;
		if(len==0)	len	=	1;
		GregorianCalendar	to	=	DateGear.calculateMonth(DateGear.currentDay(),1,true);

		putParameter(tb,KEY		, key);
		putParameter(tb,SUBJECT	, "Web作成教材（" + ke.kamokuName() +"）");
		putParameter(tb,OWNER	, getParameter(TUID));
		putParameter(tb,FNAME	, ke.getHtmlZipFilename());
		putParameter(tb,KUBUN	, "zip");
		putParameter(tb,SIZE	, String.valueOf(len)+"KB");	
		putParameter(tb,PATH	, ke.getHtmlZipPath());
		putParameter(tb,URL		, ke.getHtmlZipUrl());
		putParameter(tb,FROM	, (DateGear.getLongDateString(  )).substring(0,12));	// 今日の日付（yyyyMMddHHmm）
		putParameter(tb,IDX		, "001");	// 個人用
		
		return	tb;
		
		// レコードを登録、IDによるキーレコードも登録
		//cb.insertByid(tb, getParameter(TUID));
		
	}	
	/**
	 * htmlファイル名、epmlファイル名を変更する
	 * @param url_old	現在のURL（ファイル名）
	 * @param url		変更したいURL（ファイル名）
	 * @return			変更後の正しいURL
	 */
	String	changeUrl(String url_old, String url){
		if(LOG.fa) LOG.println("■RefList #changeUrl()");

		String	teUid		= 	getParameter("_teUid");
		String	lec_key		= 	getParameter("_lec_key");
		
		String	htmlPath	=	para.getHtmlPath(teUid,lec_key);		// /home/pc/(teUid)/html/(lec_key)
		//String	htmlPath_ex	=	para.getHtmlPath_ex(teUid,lec_key);		// /home/pc/<UID>/html_ex/<lec_key> エクスポート用
		String	epmlPath	=	para.getEpmlPath(teUid,lec_key);		// /home/pc/(teUid)/epml/(lec_key)
		/*
		 * ファイル名を ref_url の値から取り出すように変更 2004.9.1
		 * 新しいファイル名
		 */
		String	htmlName	=	Gear.getFileNameBody(url) + ".html";
		//String	htmlName_ex	=	Gear.getFileNameBody(url) + ".html";
		String	epmlname	=	Gear.getFileNameBody(url) + ".epml";

		File	html		=	new	File(htmlPath 		+ htmlName);
		//File	htmlex		=	new	File(htmlPath_ex 	+ htmlName_ex);
		File	epml		=	new	File(epmlPath 		+ epmlname);
		/*
		 * 古いファイル名
		 */
		String	old_htmlName	=	Gear.getFileNameBody(url_old) + ".html";
		String	old_htmlName_ex	=	Gear.getFileNameBody(url_old) + ".html";
		String	old_epmlname	=	Gear.getFileNameBody(url_old) + ".epml";
		
		File	old_html		=	new	File(htmlPath 		+ old_htmlName);
		//File	old_htmlex		=	new	File(htmlPath_ex 	+ old_htmlName_ex);
		File	old_epml		=	new	File(epmlPath 		+ old_epmlname);
		/*
		 * 名前の変更
		 */
		old_html.renameTo(html);
		//old_htmlex.renameTo(htmlex);
		old_epml.renameTo(epml);
		
		return para.getParentUrl(teUid,lec_key,getParameter("_ref_key")) + htmlName;
	}	
	
	/**
	 * 資料を削除する
	 *
	 */
	void delete(){
		if(LOG.fa) LOG.println("■RefList #deleteRefRecord()");
		//
		String				ref_key		= 	getParameter("_ref_key");// 選択されたレコード
		String				teUid		=	getParameter(TUID);
		String				lec_key		=	getParameter("lec_key");
		String				te_lec_key	= 	KeyGen.get_te_lec_key2(teUid, lec_key);

		ReferenceDefRecord	rdf			=	new	ReferenceDefRecord(te_lec_key, ref_key, db);
		/*
		 * web教材ならまずファイルを消去する
		 */
		String				shubetsu	=	rdf.shubetsu();
		String				url			=	rdf.url();
		if(shubetsu.equals(ReferenceDefRecord.REF_HTML)){
			
			deleteFiles(teUid, lec_key, ref_key, url);
		}
		/*
		 * データベースからレコードを削除する 
		 */
		rdf.delete(db);

	}
	/**
	 * この科目の全てのKamokuSectionDefRecordについて、資料キーフィールドから所与の資料キーを消去する
	 * 
	 * @param refkey	消去する資料キー
	 */
	void	updateKamokuSections(String refkey){
	    if(LOG.fa) LOG.println("★updateKamokuSections() / refkey=" + refkey);
	    if(LOG.fa) LOG.println("この科目の全てのKamokuSectionDefRecordについて、資料キーフィールドから所与の資料キーを消去する");
	    
		String				te_lec_key	=	KeyGen.get_te_lec_key2(getParameter(TUID), getParameter("lec_key"));
		KamokuSectionDEF	ksd			=	new	KamokuSectionDEF(te_lec_key,db);
		int					n			=	ksd.size();// 全セクション数
		for(int i=0; i<n; i++){
			
			KamokuSecDefRecord	ksdr	=	ksd.get(i);
			ksdr.updateRefkeys(refkey,db);// 消去
		}
	}
	/**
	 * 資料を削除したとき、他の資料レコードのレコードのシーケンスフィールドの値を書き直す
	 * @param _te_lec_key
	 * @param _seq
	 * @return
	 */	
	void updateSequence(String seq_old){
		
		if(LOG.fa) LOG.println("★deleteSequence()の先頭です/ seq_old=" + seq_old);
		//
		String	te_lec_key	=	KeyGen.get_te_lec_key2(getParameter(TUID) , getParameter("lec_key"));
		ReferenceDEF ksd	= 	new ReferenceDEF(te_lec_key, db);
		int max				= 	ksd.size();
		int seqNum			= 	Integer.parseInt(seq_old);
		if(seqNum == max)	return ;	// 最大番号のレコードならなにもしない
		//
		for(int k=seqNum+1; k<=max; k++){		// new+1 から max までのシーケンス番号を－１する
			int N = getRecordNumber(k);			// シーケンスは１オリジンだが、レコードは０オリジンなので－１する
			ReferenceDefRecord rec = ksd.get(N);
			rec.set_seq_number( get000String( String.valueOf(k-1) ) );	// 000 形式で書き込むこと
			rec.update(db);
		}
		return;
	}	
	/**
	 * epml, html の各ファイルを消す
	 * 
	 * @param teUid
	 * @param lec_key
	 * @param ref_key
	 * @param url
	 */
	void	deleteFiles(String teUid,String lec_key, String ref_key, String url){
		if(LOG.fa) LOG.println("■RefList #deleteFiles()");

		String	epmlPath	=	para.getEpmlPathName(teUid,lec_key);
		String	htmlPath	=	para.getHtmlPathName(teUid,lec_key);
		//
		Files	epf			= 	new Files(epmlPath);
		Files	htf			= 	new Files(htmlPath);
		//
		String	pmlName		= 	para.getPmlFilename(url, ref_key);// URLからファイル名を作成
		String	htmlName	=	para.getHtmlFilename(url, ref_key);
		//
		epf.deleteFile(pmlName);
		htf.deleteFile(htmlName);
		//
	}
	/**
	 * 資料を科目に登録する
	 * @param htb
	 * @return
	 */
	boolean doUpdateRefRecord(Hashtable htb){
		if(LOG.fa) LOG.println("■RefList #doUpdateRefRecord()");
		//
		String	ref_key	= getParameter("_ref_key");
		boolean ret	= true;
		if( isUpdateRefOK()){
			
			/*
			 * タイトルに　' が含まれているとDBに書けないので特殊文字に変換する
			 */
		    String	title	=	getParameter("_ref_title");
		    putParameter("_ref_title", Gear.toDefindStr(title));
		    
		    
		    // ref_key があれば更新、なければ新規モードとする
			// 資料キーを取り出す
			if(isEmpty(ref_key)){								// なければ新規
				addRefRecord(htb);
			}else{
				updateRefRecord(htb);							// あれば更新
			}
			//
			//  種別が教材WEBの場合は諸データをクリアしてはいけない
			String	shubetsu	= getParameter("ref_shubetsu");
			if(!shubetsu.equals(ReferenceDefRecord.REF_HTML)){
				clearRefRecord();	// 編集欄をクリアーする（ putParameter("_ref_key","" ）も実行してキー値を消しておく）mode=NEW も
			}
			//
		}else{
			ret	= false;
		}
		return ret;
	}
	/**
	 * 更新してよいかどうか
	 * 
	 * @param htb
	 * @return
	 */
	boolean isUpdateRefOK(){
		if(LOG.fa) LOG.println("■RefList #isUpdateRefOK()");
		//
		String	ref_seqNum		= getParameter("_ref_seqNum");		// シーケンス番号（１オリジン）
		String	ref_shubetsu	= getParameter("_ref_shubetsu");	// 資料種別
		String	ref_title		= getParameter("_ref_title");		// 資料タイトル
		String	ref_url			= getParameter("_ref_url");			// 資料ＵＲＬ
		//
		ReferenceDefRecord chk  = new ReferenceDefRecord();
		chk.set_seq_number(ref_seqNum);
		chk.set_shubetsu(ref_shubetsu);
		chk.set_title(ref_title);
		chk.set_url(ref_url);
		//
		boolean flag = true;
		if(!chk.isUpdateRefOK())  flag = false;
		if(LOG.fa) LOG.println("isUpdateRefOK()の結果は、" + String.valueOf(flag) + " です");
		return flag;
	}
	String	getNewSeqNum(){
		if(LOG.fa) LOG.println("■RefList #getNewSeqNum()");
		
		String			te_lec_key	=	KeyGen.get_te_lec_key2(getParameter(TUID), getParameter("lec_key"));
		ReferenceDEF	refDef 		= 	new	ReferenceDEF(te_lec_key, db);
		int				n 			=	refDef.size();
		return			StringGear.get000type(n+1);
	}
	/**
	 * 資料データを新規作成する
	 * @param htb
	 */
	void addRefRecord( Hashtable htb ){
		if(LOG.fa) LOG.println("■RefList #addRefRecord()");
		//
		String teUid		= getParameter("_teUid");
		String lec_key		= getParameter("_lec_key");
		KeyGen kg			= new KeyGen(teUid,db);
		//
		String  te_lec_key	= KeyGen.get_te_lec_key(htb);		// teUid と lec_key を別々に持っているので
		String	ref_key		= kg.nextRef();						// ６桁の資料キー
		putParameter("_ref_key",ref_key);						// 記憶しておく
		//
		/*
		 * シーケンス番号を得る
		 * ウェブ変数にもセットしておく
		 */
		String	ref_seqNum	= getNewSeqNum();					// シーケンス番号を作成してセットする
		putParameter("_ref_seqNum"	 ,ref_seqNum);
		putParameter("_past_ref_seq" ,ref_seqNum);				// シーケンス番号を変えたかどうか判断するため覚えておく
		
		
		String	ref_shubetsu= getParameter("_ref_shubetsu");	// 資料種別
		String	ref_title	= getParameter("_ref_title");		// 資料タイトル
		String	ref_url		= getParameter("_ref_url");			// 資料ＵＲＬ
		
		/*
		 * HTML 作成で新規にファイルを作る時、ref_url　には ファイル名か仮のファイル名 "INTERNAL_FILE" が入っている
		 * ここではそれをチェックして、実際の URL に書き換える
		 * 
		 * 以下は /home/pc/mwb9l124/html/027/ の内容
		 * ひとつの科目について、htmlがまとめて格納され、files/ 以下にグラフィックスデータ
		 * がある．また、while.htmlのように、名前を変更した場合もここに格納されている．
		 * 
		 * drwxr-xr-x    3 root     root         4096  1月 13 01:10 ./
		 * drwxr-xr-x   16 root     root         4096  1月 13 15:31 ../
		 * drwxr-xr-x    2 root     root         4096  1月 13 09:37 files/
		 * -rw-r--r--    1 root     root        16303 10月 24 22:54 ht000132.html
		 * -rw-r--r--    1 root     root        20759 10月 24 22:56 ht000134.html
		 * -rw-r--r--    1 root     root        11427 10月 25 13:10 ht000135.html
		 * -rw-r--r--    1 root     root         9883 10月 25 00:34 ht000138.html
		 * -rw-r--r--    1 root     root          720 10月 24 22:29 ht000139.html
		 * -rw-r--r--    1 root     root         2146 10月 24 22:56 ht000140.html
		 * -rw-r--r--    1 root     root        12919 10月 24 22:46 ht000143.html
		 * -rw-r--r--    1 root     root        16050  1月 13 09:38 ht000144.html
		 * -rw-r--r--    1 root     root        16283  1月 13 01:10 ht000147.html
		 * -rw-r--r--    1 root     root          498 10月 24 23:22 ht000148.html
		 * -rw-r--r--    1 root     root           43 10月  5 11:52 spacer.gif
		 * -rw-r--r--    1 root     root        31472  1月 11 14:53 while.html
		 * 
		 */
		if(ref_url.equals("INTERNAL_FILE")){
			/* ref_key が確定するので URL を作ってハッシュに入れる
			 * 
			 * 例示：　/user/<UID>/html/[lec_key]/ht[ref_key].html
			 */
			ref_url	= para.getUrl(teUid, lec_key, ref_key);
			putParameter("_ref_url",ref_url);

		}else if( (!isEmpty(ref_url)) && (ref_url.charAt(0)=='*') ){
			/* ファイル名が指定してあるのでそれを使うようにURLを作成する
			 * 先頭の*印を取ってHTMLファイルURLを作成する 
			 * 
			 * 例示：　/user/<UID>/html/[lec_key]/[指定ファイル名].html
			 */
			ref_url = ref_url.substring(1);
			ref_url	= para.getParentUrl(teUid, lec_key, ref_key) + Gear.getFileNameBody(ref_url) + ".html";
			putParameter("_ref_url",ref_url);	
		}
		/*
		 * 後での書き込み時にURLを変更したかどうかをチェックするため保存しておく
		 */
		putParameter("_past_ref_url",ref_url);
		
		//
		// シーケンス番号のチェックと更新
		//ref_seqNum = setRefSeq(htb,"NEW");						// シーケンス番号を全体に渡って調整
		//
		// レコードに値を入れて書き込む
		ReferenceDefRecord	rdRec	= new ReferenceDefRecord();		// 空の科目レコードを生成
		//
		rdRec.set_te_lec_key(te_lec_key);
		rdRec.set_ref_key(ref_key);
		rdRec.set_seq_number(ref_seqNum);
		//
		rdRec.set_shubetsu(ref_shubetsu);
		rdRec.set_title(ref_title);
		rdRec.set_url(ref_url);
		//
		rdRec.insert(db);	// 書き込み
		return;
	}
	/**
	 * 資料データを更新する
	 * @param htb
	 */
	void updateRefRecord(Hashtable htb){
		if(LOG.fa) LOG.println("■RefList #updateRefRecord()");
		//
		String  te_lec_key	= KeyGen.get_te_lec_key(htb);
		String	ref_key		= getParameter("_ref_key");
		//
		String	ref_seqNum	= getParameter("_ref_seqNum");		// シーケンス番号（１オリジン）
		String	ref_shubetsu= getParameter("_ref_shubetsu");	// 資料種別
		String	ref_title	= getParameter("_ref_title");		// 資料タイトル
		String	ref_url		= getParameter("_ref_url");			// 資料ＵＲＬ
		/*
		 * Web教材の場合でURLの中のファイル名は毎回書き直し可能
		 * URL変更とファイル名変更に対処する
		 */
		if(ref_shubetsu.equals(ReferenceDefRecord.REF_HTML)){
			ref_url	=	fixUrl(ref_key, ref_url);
		}
		//
		// シーケンス番号のチェックと更新
		//ref_seqNum = setRefSeq(htb,"EDIT");
		//
		// レコードに値を入れて更新する
		ReferenceDefRecord	rdRec	= new ReferenceDefRecord();		// 空の科目レコードを生成
		//
		rdRec.set_te_lec_key(te_lec_key);
		rdRec.set_ref_key(ref_key);
		rdRec.set_seq_number(ref_seqNum);
		//
		rdRec.set_shubetsu(ref_shubetsu);
		rdRec.set_title(ref_title);
		rdRec.set_url(ref_url);
		//
		rdRec.update(db);	// 書き込み
		/*
		 * 後での書き込み時にURLを変更したかどうかをチェックするため保存しておく
		 */
		putParameter("_past_ref_url",ref_url);
		
		
		//
		return;
	}
	/**
	 * 正しいURLを作成する
	 * 実際には
	 * 	・教師キーと科目キーからディレクトリへのURLを求める
	 * 	・URL から拡張子を含まないファイル名だけを取り出し、拡張子を"html"とする
	 * 
	 * @param ref_key
	 * @param ref_url
	 * @return
	 */
	String	fixUrl(String ref_key, String ref_url){
		
		/*
		 * 書き込み以前のURLを得る
		 * update()では、この値は常にwebに保存されている
		 * 変更がなければそのままを返す
		 */
		String	pastUrl	=	getParameter("past_ref_url");
		if(pastUrl.equals(ref_url)){
			return	ref_url;
		}
		/*
		 * 変更があればファイル名の変更を行なった上でURLを書き換える
		 */
		String	oldNameBody	=	FileGear.getFileNameBody(pastUrl);// URL末尾のファイル名（拡張子含まず）
		String	oldHtml		=	oldNameBody + ".html";
		String	oldPml		=	oldNameBody + ".epml";
		
		String	newNameBody	=	Gear.getFileNameBody( FileGear.getLastUrl(ref_url) );
		String	newHtml		=	newNameBody + ".html";
		String	newPml		=	newNameBody + ".epml";
		
		/*
		 * 教材ウェブのあるディレクトリを得て、ファイル名を変更する
		 */
		String	htmlpath		=	para.getHtmlPath(getParameter(TUID), getParameter("lec_key"));
		String	htmlsouName		=	htmlpath + oldHtml;
		String	htmldestName	=	htmlpath + newHtml;
		boolean	html		=	FileGear.rename(htmlsouName, htmldestName);
		

		
		String	pmlpath			=	para.getEpmlPath(getParameter(TUID), getParameter("lec_key"));
		String	pmlsouName		=	pmlpath + oldPml;
		String	pmldestName		=	pmlpath + newPml;
		/*
		 * 古いバージョンではファイル名の先頭はep ,新しいバージョンではURLから取るので htである．
		 * 互換性を取るため存在をチェックし引き継ぐ必要がある
		 */
		String	oldPml_Oldversion		=	"ep" + oldPml.substring(2);
		String	pmlsouName_Oldversion	=	pmlpath + oldPml_Oldversion;
		File fp	=	new File(pmlpath + oldPml_Oldversion);
		if(fp.exists()){
			pmlsouName	=	pmlsouName_Oldversion;
		}		
		boolean	pml			=	FileGear.rename(pmlsouName, pmldestName);
		
		if(LOG.fa){

			LOG.println("");
			LOG.println("■■ RefList #fixUrl() : souName  =" + htmlsouName);
			LOG.println("　　                   : destName =" + htmldestName);
			LOG.println("　　                   : fret     =" + html);
			LOG.println("");
			LOG.println("                       : souName  =" + pmlsouName);
			LOG.println("　　                   : destName =" + pmldestName);
			LOG.println("　　                   : fret     =" + pml);
			LOG.println("");
		}
		/*
		 * 変更後のURLを作成して返す
		 */
		String	url	= para.getParentUrl(getParameter(TUID), getParameter("lec_key"), ref_key) + newHtml;
		putParameter("ref_url", url);
		return	url;
	}
	/**
	 * セクションレコードの資料リストフィールドを更新する
	 * @param htb
	 */
	void 	updateSecRerField(Hashtable htb){
		if(LOG.fa) LOG.println("■RefList #updateSecRerField()");
		//
		// この科目に属する全ての資料レコードを得る
		String te_lec_key	= 	KeyGen.get_te_lec_key(htb);
		ReferenceDEF refDef = 	new	ReferenceDEF(te_lec_key, db);
		int	n = refDef.size();
		Vector  reflist = new Vector(20,10);
		//
		// チェックされた資料の資料キーを取り出し、Vector に入れる
		for(int i=0; i<n; i++){
			String cb 			= "_cb" + String.valueOf(i+1);	// １オリジンで
			String cb_value		= getParameter(cb);
			if(cb_value!=null){// チェックされていないものは _cb* がないので必ずnullになる
				if( cb_value.equals("ON")){
					reflist.add(refDef.ref_key(i));	// // 第i番目の資料キーを取り出す
				}
			}
		}
		// 登録された資料があればリストとしてセクションレコードに登録する
		String sect_key			= getParameter("_sect_key");
		KamokuSecDefRecord rec	= new KamokuSecDefRecord( te_lec_key, sect_key, db);
		if(reflist.size() > 0){
			rec.set_ref_list(reflist);// Vector から登録できる
		}else{
			rec.set_ref_list("");// リストを消去するため（文字列からも登録できる）
		}
		rec.update(db);	// 更新を書き込む
	}
	/**
	 * シーケンス番号からレコード番号を得る
	 * @param k
	 * @return
	 */
	int getRecordNumber(int k){	
		if(LOG.fa) LOG.println("■RefList #getRecordNumber()");
		
		return	k-1;	// １オリジンをゼロオリジンに補正する
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
	public  void	display(boolean editmode){
		if(LOG.fa) LOG.println("■RefList #display()");
		
		if(!editmode){
			//DBG.println("■RefList #display(): NOT edit-mode!!");
			putParameter(MESSAGE,"");
			putParameter("_ref_title","");
			putParameter("_ref_url","");
			putParameter("shubetsu", ReferenceDefRecord.REF_WEB);// ウェブ参照（規定値）
		}
		setShubetsuSelection();
		String	teUid			=	getParameter(TUID);
		String	lec_key			=	getParameter("lec_key");
		String	sect_key		=	getParameter("sect_key");
		KamokuSecDefRecord	ksd	=	new KamokuSecDefRecord(KeyGen.get_te_lec_key2(teUid, lec_key), sect_key, db);
		putParameter("kai",ksd.seq_number());
		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}
	/**
	 * 種別リストボックス表示設定．
	 * 現在エディット領域にある資料の種別に合致した選択状態をセットする．
	 * 
	 * getParameter("_ref_shubetsu")の初期値は「ウェブ参照」である
	 * 
	 */
	void	setShubetsuSelection(){
		if(LOG.fa) LOG.println("■RefList #setShubetsu()");
		
		String	shubetsu	=	getParameter("_ref_shubetsu");
		int		n			=	ReferenceDefRecord.SHUBETSU_MAX;// 種別の数
		for(int	 i=0; i<n; i++){
			if(shubetsu.equals(String.valueOf(i+1))){	// 種別は"1"から始まる
				putParameter("sel_" + String.valueOf(i+1),"selected");
			}else{
				putParameter("sel_" + String.valueOf(i+1),"");
			}
		}
	}
	/**
	 * ハッシュテーブル(htb)を使って部分的に切り取ったソースデータ（exHtml）
	 * の内容を置き換えて出力処理する．表などの反復出力に利用するが個々の処理
	 * 内容は、key で特定される．
	 */
	public void	write(String key,Vector exHtml){
		if(LOG.fa) LOG.println("■RefList #write()");
		
		if(key.equals("reference_List")){
			reference_List(exHtml);
			
		}else if(key.equals("ref_seq_list")){
			ref_seq_list(exHtml);
			
		}else if(key.equals("reference_Empty")){
			reference_Empty(exHtml);
			
		}
	}
	/**
	 * 資料リストを表示する
	 * @param exHtml
	 */
	void	reference_List(Vector exHtml){
		if(LOG.fa) LOG.println("■RefList #reference_List()");
		/*
		 *このセクションで使用中の資料のキーリストを調べるのに使う 
		 */
		String 				te_lec_key	= 	KeyGen.get_te_lec_key(htb);
		String				sect_key	=	getParameter("sect_key");
		KamokuSecDefRecord 	krec 		= 	new KamokuSecDefRecord(te_lec_key, sect_key, db);
		/*
		 * この科目の全ての定義済み資料を得る
		 * reference_Empty()のために資料数をシステムハッシュに記憶
		 */
		ReferenceDEF	refDef	= new ReferenceDEF(te_lec_key,db);
		int 			n 		= refDef.size();
		putParameter("_refDefSize",String.valueOf(n));
		
		for(int i=0; i<n; i++){
			if(LOG.fa) LOG.println("■RefList #reference_List()：loop-" + i);
			/*
			 * この科目の全ての定義済み課題を表示する
			 */
			ReferenceDefRecord	rec	= 	refDef.get(i);	// シーケンス番号順に取り出す
			//
			putParameter("_n"			, String.valueOf(i+1) );
			putParameter("_ref_key"		, rec.ref_key());	
			putParameter("_shubetsu"	, rec.shubetsuName());
			putParameter("_ref_title"	, rec.title());	
			putParameter("_refIcon"		, rec.refIcon());
			putParameter("_urlstr"		, rec.url());
			/*
			 * このセクションで使用しているものはリストボックスを　YES にする
			 */
			if( krec.isUseThisRefKey( rec.ref_key() ) ){ // 使っているかどうか調べる
				putParameter("_op_1","");
				putParameter("_op_2","selected");	// YES
			}else{
				putParameter("_op_1","selected");	// NO
				putParameter("_op_2","");
			}
			if(isEmpty( rec.url())){
				putParameter("_urlFlag","CHANGE");
			}else{
				putParameter("_urlFlag","");
			}
			printVector(exHtml);
		}
	}
	/**
	 * 課題画面でシーケンス番号リストを表示する.
	 * 何番目の課題かをreference_List()から受け取る
	 * @param exHtml
	 */	
	void	ref_seq_list(Vector exHtml){
		if(LOG.fa) LOG.println("■RefList #ref_seq_list()");
		
		int		seq		=	Integer.parseInt( getParameter("n") );
		int 	total	=	total(getParameter(TUID), getParameter("lec_key"));
		for(int i=0; i<total; i++){
			/*
			 * value
			 */
			String opt = String.valueOf(i+1);
			putParameter("_s",opt);		// 1 オリジン
			/*
			 * selected
			 */
			if(seq == i+1){
				putParameter("_sel","selected");
			}else{
				putParameter("_sel","");
			}
			/*
			 *	ラベル  000タイプの文字列
			 */
			putParameter("_ss",get000type(i+1));	
			printVector(exHtml);
		}

	}		
	/**
	 * この科目の全ての定義済み資料の総数を得る
	 * 
	 */
	int	total(String teUid, String lec_key){
		if(LOG.fa) LOG.println("■RefList #total()");
		
		String 			te_lec_key	= KeyGen.get_te_lec_key2(teUid, lec_key);
		ReferenceDEF	refDef		= new ReferenceDEF(te_lec_key,db);
		return			refDef.size();
  	}
	/**
	 * 資料が１件もないとき、空白の行を表示する．
	 * reference_List()から総資料数（_refDefSize）を受け取る

	 * @param exHtml
	 */
	void	reference_Empty(Vector exHtml){
		if(LOG.fa) LOG.println("■RefList #reference_Empty()");

		int	n	= Integer.parseInt( getParameter("_refDefSize") );	// 既に表示した行があるか
		if(n > 0)	return;
		printVector(exHtml);
		return;
	}

}
