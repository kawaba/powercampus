/*
     課題リストの表示
     
     ------------------------------------------------------------------------
     2023.6.3 ページング処理を追加
     ------------------------------------------------------------------------
     workList.html
     ・ページ送りのアイコンを追加
     ・submitでコマンドを送信するように変更
     WorkList.java
     ・ ページ処理用の変数を追加
	 	 maxLines     １ページに表示する行数（20)
	 	 allLines     全行数（DBから取得）
	 	 maxPages     全ページ数　maxPage　= (allLines-1) / maxLines + 1;　
	 	 page 　      現在のページ番号（１オリジン）
	 ・pageをaccept変数として保存するようにする
	 　 ⇒dispatch.xmlに変数を追加することが必要
	 ・pageから、開始行位置、終了行位置を計算し、for文の制御を行う
     ------------------------------------------------------------------------
     
     
*/
package kadai;

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Vector;
import database.Database;
import database.DbConnectionBroker;
import database.KeyGen;
import framework.LOG;
import framework.Param;
import framework.SuperPlayer;
import kamoku.KamokuSecDefRecord;
import kamoku.KamokuSectionDEF;

/**
 * 課題一覧・作成・割付
 * 
 * 課題を一覧し、割付を行なう．
 * タイトルを入力して新規作成も行なう．
 * 
 * 
 	#
	# ##################
	#   WorkList
	# ##################
	#
	<program $kadai.WorkList>
		<dispatch  html=workList.html  number=300  class=kadai.WorkList />
		<variable>
		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA lec_key sect_key title />
		  <accept    CMD    kadai_key kadai_seq past_kadai_seq page />
		  <keep      />
		  
		  <work      />
		  <form      ref_seqNum_%_n% _assign_%_n% />
		</variable>
	</program>  
 *
 * 1.accept 変数
 *	kadai_key			（エディタでの編集または削除が選択されたとき）課題キー
 *	past_kadai_seq		（順序変更が選択されたとき）変更前のシーケンス番号
 *	
 *
 * 2.keep 変数

 *
 * 3.form 変数
 *  kadai_seqNum_%_n%	順序を変更する課題のシーケンス番号（%_n% の部分は 1 からの番号）
 *  assign_%_n%			割り付ける課題のシーケンス番号（%_n% の部分は 1 からの番号）
 *
 */
public class WorkList extends SuperPlayer {

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

	String			teUid;
	String			lec_key;
	String			sect_key;
	String 			te_lec_key;
	
	/* **************************************************
	 * ページ処理用の変数
	 * maxLines     １ページに表示する行数
	 * allLines     全行数
	 * maxPages     全ページ数
	 * page 　      現在のページ番号（１オリジン）	 */
	
	int	maxLines = 20; 
	int allLines;
	int maxPage;
	int page;	
	/* *************************************************/
	
	
	public	WorkList(){
		super();
		if(LOG.fa) LOG.println("■ WorkList #コンストラクタ");
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
		broker		=	getDbConnection();
		db			=	new Database(broker);			
		
		teUid		=	getParameter( "_teUid");
		lec_key		=	getParameter( "_lec_key");
		sect_key	=	getParameter("_sect_key");
		
		te_lec_key	= 	KeyGen.get_te_lec_key2(teUid, lec_key);
		
		
		/* *** ページ処理 変数の初期化　2023.5.31 ************************************************************/

		// 変わる可能性があるので、毎回計算する 
		KadaiDEF	kdef	= new KadaiDEF(te_lec_key,db);
		maxLines	= 20; 
		allLines	= kdef.size();
		maxPage		= (allLines-1) / maxLines + 1; 
		
		// 現在のページ番号がシステムハッシュにあればそれを使う。なければ最後のページを表示する
		if(getParameter("_page").isEmpty()) 	page = maxPage;	
		else									page = Integer.valueOf(getParameter("_page")); 		

		/* ****************************************************************************************************/

		
	}
	@Override
	public	String	dispatch(){
		if(LOG.fa) LOG.outHash(htb,"■WorkList #dispatch()");
		if(LOG.fa) LOG.println("■WorkList #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		/*
		 * 危険な記号を特種文字に変えておく
		 */
		//Gear.revConvertHash(htb,"_kadai_title");
		//Gear.revConvertHash(htb,"_kadai_content");

		if(cmd.equals("ASSIGN")){
			if(LOG.tr) LOG.println("■WorkList #dispatch(): ASSIGN ");
			/*
			 * 割り付けの更新処理
			 * セクションレコードの課題リストフィールドを更新する
			 */
			
			String kadaiKey = getParameter("_kadai_key");
			String kadaiSeq = getParameter("_past_kadai_seq");
			String yes_no   = getParameter("_assign_" + kadaiSeq);
			
			// 課題を表示するかどうか 2023.6.14 修正
			updateSecKadaiField(kadaiKey,yes_no);
			
			//updateSecKadaiField();
			
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示
		
		}else if(cmd.equals("ORDER")){
			/*
			 * 課題の順序を並べ替える
			 * seq_old は変更前の表示の並び番号
			 */
			String	seq_old		= 	getParameter("_past_kadai_seq");
			String	seq			= 	getParameter("_kadai_seqNum_" + seq_old);
			
			if(LOG.fa) LOG.println("□□ cmd = " + cmd );
			if(LOG.fa) LOG.println("□　seq_old	="+seq_old );
			if(LOG.fa) LOG.println("□　seq		="+seq );
			
			changeSequence(seq, seq_old);
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示
			
			
		}else if(cmd.equals("EDIT")){
			/*
			 * 課題を編集する
			 * システムハッシュには _kadai_key, _past_kadai_seq(=kadai_seq) がセットされている
			 */
			String kadai_seq	=	getParameter("_past_kadai_seq");
			htb.put("_kadai_seqNum", get000String( kadai_seq));
			
			disp_mode	=	DISP_NEW;
			ret			=	"$kadai.WorkEdit";
			
		// 
		}else if(cmd.equals("CREATE")){
			/*
			 * 新規作成画面を開く
			 * 新規作成と判別できるように、システムハッシュには _kadai_key として "" をセットする
			 * また、_kadai_seq には現在の課題数 + 1 をセットする 
			 */
			KadaiDEF	kadaiDef 	= 	new	KadaiDEF(te_lec_key, db);
			int		n 				=	kadaiDef.size();
			htb.put("_kadai_seqNum", get000String(String.valueOf(n + 1)));
			htb.put("_kadai_key", "");

			disp_mode	=	DISP_NEW;
			ret			=	"$kadai.WorkEdit";
		
		}else if(cmd.equals("DELETE")){
			/*
			 * 他の課題レコードのシーケンス番号を調整する
			 */
			String	seq_old		= 	getParameter("_past_kadai_seq");
			updateSequence(seq_old);
			/*
			 * 課題を削除する
			 */
			String	kadaikey	= 	getParameter("_kadai_key");
			deleteKadaiRecord(kadaikey);
			
			/*
			 * この科目の全てのKamokuSectionDefRecordについて、資料キーフィールドから
			 * 削除した資料キーを消去しておく
			 */
			updateKamokuSections(getParameter("_kadai_key"));
			
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示
			
		}else if(cmd.equals("RETURN")){
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;	// 復帰			
			
		}else if(cmd.equals("TOP")){
			page = 1;
			putParameter("_page",String.valueOf(page));
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示

		}else if(cmd.equals("PREVIOUS")){
			if(page>1) {
				page--;
				putParameter("_page",String.valueOf(page));
			}
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示				
			
		}else if(cmd.equals("NEXT")){
			if(page<maxPage) {
				page++;
				putParameter("_page",String.valueOf(page));
			}
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示	
			
		}else if(cmd.equals("END")){
			page = maxPage;
			putParameter("_page",String.valueOf(page));
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示
			
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
	
	/**
	 * セクションレコードの課題リストフィールドを更新する
	 */
	void 	updateSecKadaiField(String kadaiKey, String yes_no){
		
		String sect_key			= getParameter("_sect_key");
		KamokuSecDefRecord rec	= new KamokuSecDefRecord( te_lec_key, sect_key, db);
		
		if(yes_no.equals("YES")) {
			rec.addKadaiKey(kadaiKey, db);		// 掲示（YES）
		}
		else {
			rec.updateKadaikeys(kadaiKey, db);	// 非掲示(NO)	
		}
		
	}
	/*
	 * セクションレコードの課題リストフィールドを更新する
	 *
	 *
	 * この処理はページングでは使えないので上に作成し直した。
	 * 変更した課題のみ、掲示、非掲示を切り替えるようにした。
	 * 
	 * 
	void 	updateSecKadaiField(){
		if(LOG.fa) LOG.outHash(htb,"■ WorkList #updateSecKadaiField()");
		
		//
		// この科目に属する全ての課題レコードを得る。シーケンス番号順に並んでいる
		KadaiDEF	kadaiDef 	= 	new	KadaiDEF(te_lec_key, db);
		int			n 			=	kadaiDef.size();
		Vector  	kadailist 	=	new Vector(20,10);
		
		
		//
		// "YES"とされた課題の課題キーを取り出し、Vector に入れる
		//
		for(int i=0; i<n; i++){
			String cb 			= "_assign_" + String.valueOf(i+1);	// １オリジンで
			String cb_value		= getParameter(cb);
			if( cb_value.equals("YES")){
				
				kadailist.add(kadaiDef.kadai_key(i));	// 課題キーを取り出してベクターに入れる
			}
		}
		if(LOG.fa) LOG.outVector(kadailist,"□ 割り付け課題のリスト");
		// 現在のセクションレコードを得る
		String sect_key			= getParameter("_sect_key");
		KamokuSecDefRecord rec	= new KamokuSecDefRecord( te_lec_key, sect_key, db);
		//
		// 登録された課題があればリストとしてこのセクションレコードに登録する
		if(kadailist.size() > 0){
			rec.set_kadai_list(kadailist);// Vector から登録できる＜完全な書き換えになる＞
		}else{
			rec.set_kadai_list("");// リストを消去するため（文字列からも登録できる）
		}
		rec.update(db);	// 更新を書き込む
	}
	
	*/
	
	/**
	 * 編集中の課題を削除する
	 * @param htb
	 */
	void deleteKadaiRecord(String kadai_key){
		if(LOG.fa) LOG.outHash(htb,"class WorkList #deleteKadaiRecord()の先頭です");
		//
		KadaiDefRecord  rec 		= 	new KadaiDefRecord();
		rec.set_te_lec_key(te_lec_key);
		rec.set_kadai_key( kadai_key);
		rec.delete(db);
	}
	/**
	 * この科目の全てのKamokuSectionDefRecordについて、課題キーフィールドから所与の課題キーを消去する
	 * 
	 * @param kadaikey	消去する課題キー
	 */
	void	updateKamokuSections(String kadaikey){
		
		String				te_lec_key	=	KeyGen.get_te_lec_key2(getParameter(TUID), getParameter("lec_key"));
		KamokuSectionDEF	ksd			=	new	KamokuSectionDEF(te_lec_key,db);
		int					n			=	ksd.size();
		for(int i=0; i<n; i++){
			
			KamokuSecDefRecord	ksdr	=	ksd.get(i);
			ksdr.updateKadaikeys(kadaikey,db);// 消去
		}
	}	
	/**
	 * 課題を削除したとき、他のレコードのレコードのシーケンスフィールドの値を書き直す
	 * @param _te_lec_key
	 * @param _seq
	 * @return
	 */	
	void updateSequence(String seq_old){
		
		if(LOG.fa) LOG.println("★deleteSequence()の先頭です/ seq_old=" + seq_old);
		//
		KadaiDEF ksd	= new KadaiDEF(te_lec_key,db);
		int max			= ksd.size();
		int seqNum			= Integer.parseInt(seq_old);
		if(seqNum == max)	return ;	// 最大番号のレコードならなにもしない
		//
		for(int k=seqNum+1; k<=max; k++){		// new+1 から max までのシーケンス番号を－１する
			int N = getRecordNumber(k);			// シーケンスは１オリジンだが、レコードは０オリジンなので－１する
			KadaiDefRecord rec = ksd.get(N);
			rec.set_seq_number( get000String( String.valueOf(k-1) ) );	// 000 形式で書き込むこと
			rec.update(db);
		}
		return;
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
	//
	//  シーケンス番号を変えたときの処理
	//
	void changeSequence( String seq, String seq_old){
        if(LOG.fa) LOG.println("★changeSequence()の先頭です/  seq_old=" + seq_old + ",seq=" + seq);
		
		//
		KadaiDEF ksd	= new KadaiDEF(te_lec_key,db);
		int max		= ksd.size();
		int seqNum		= Integer.parseInt(seq);
		if(seqNum >= max + 1){
			seqNum = max;	// 最大番号にして以下の処理を適用する
		}
		int seqNum_old	= Integer.parseInt(seq_old);
		//
		if(LOG.fa) LOG.println("class exwork #setupSeqUpdate_Kadai() : 番号ずらしの処理 です");
		if(LOG.fa) LOG.println("           seq_old = " + seq_old);
		if(LOG.fa) LOG.println("           seq 　　= " + seq);
		//
		
		if(seqNum == seqNum_old){
			return;
			
		}else if(seqNum < seqNum_old){
			for(int k = seqNum; k<seqNum_old; k++){	// new から old-1 までのレコードのシーケンス番号を＋１する
				int N 				= getRecordNumber(k);			// シーケンスは１オリジンだが、レコードは０オリジンなので－１する
				KadaiDefRecord rec 	= ksd.get(N);
				rec.set_seq_number( get000String( String.valueOf(k+1) ) );	// 00 形式で書き込むこと
				rec.update(db);
			}
			
		}else if(seqNum_old < seqNum){
			for(int k = seqNum_old + 1; k<=seqNum; k++){	// new+1 から old+1 までのレコードのシーケンス番号を－１する
				int N 				= getRecordNumber(k);				// シーケンスは１オリジンだが、レコードは０オリジンなので－１する
				KadaiDefRecord rec 	= ksd.get(N);
				rec.set_seq_number( get000String (String.valueOf(k-1) ) );	// 00 形式で書き込むこと
				rec.update(db);
			}
		}
		int N = getRecordNumber(seqNum_old);			// シーケンスは１オリジンだが、レコードは０オリジンなので－１する
		KadaiDefRecord rec = ksd.get(N);
		rec.set_seq_number( get000String( String.valueOf(seqNum) ) );	// 00 形式で書き込むこと
		rec.update(db);		
		return	;
		//
	}
	// テキストエリアの行数が空でないかチェックして空だったら初期値をセットする
	void	setEditorRows(Hashtable htb,int nl){
		// テキストエリアの行数
		String	rows	= getParameter("_editor_rows");
		if(isEmptyRowParam(rows)){
			htb.put("_editor_rows",String.valueOf(nl));		//  初期値nl行
		}
	}
	boolean isEmptyRowParam(String str){
		if(str==null) 					return  true;
		if(str.length()==0)				return  true;
		if(str.equals("_editor_rows"))	return  true;	// HTMLの　<input type="hidden" name="_editor_rows"    value="%_editor_rows%"> から 
		return false;									// 値が設定されていない時の文字列 "_editor_rows" を拾ってしまう
	}
	/**
	 * シーケンス番号からレコード番号を得る
	 * @param k
	 * @return
	 */
	int getRecordNumber(int k){	
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
	 * 画面を表示する
	 * 
	 * @param	editmode	表示モード（true なら編集、falseなら新規）
	 */
	@Override
	public  void	display(boolean editmode){
		if(LOG.fa)	LOG.println("■ Sample #display(boolean editmode)");
		
		if(!editmode){
			htb.put(MESSAGE,"");
		}
		KamokuSecDefRecord	ksd	=	new KamokuSecDefRecord(te_lec_key,sect_key,db);
		putParameter("_kai",ksd.seq_number());
		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}
	
	
	@Override
	public void	write(String key,Vector exHtml){

		if(key.equals("kadai_List")){
			kadai_List(exHtml);
		
		}else if(key.equals("kadai_seq_list")){
			kadai_seq_list(exHtml);
			
		}else if(key.equals("blankColumn")){
			blankColumn(exHtml);
			
		}
	}	
	public	void	kadai_List(Vector exHtml){
		if(LOG.fa) LOG.println("■ WorkList #kadai_List():");
		/*
		 *このセクションで使用中の課題のキーリストを調べるのに使う 
		 */
		KamokuSecDefRecord krec = 	new KamokuSecDefRecord(te_lec_key, sect_key, db);
		/*
		 * この科目の全ての定義済み課題を得る
		 * blockWriteのために課題数をシステムハッシュに記憶
		 */
		KadaiDEF	kadaiDef	= new KadaiDEF(te_lec_key,db);
		int 		n 			= kadaiDef.size();
		putParameter("_kadaiDefSize",String.valueOf(n));
		
		
		/* ***** 2023.6.3 ページングを追加 ********************************************/
		
		int from = (page-1) * maxLines;
		int until = from + 20;
		if(until>allLines)	until = allLines;

		
	//	for(int i=0; i<n; i++){
		for(int i=from; i<until; i++){
			/*
			 * この科目の全ての定義済み課題を表示する
			 */
			KadaiDefRecord	rec	= 	kadaiDef.get(i);	// シーケンス番号順に取り出す
			//
			putParameter("_n"				, String.valueOf(i+1) );
			putParameter("_kadai_key"		, rec.kadai_key());	
			putParameter("_shubetsu"		, rec.shubetsuName());
			putParameter("_kadai_title"	, rec.title());	
			putParameter("_kadaiIcon"		, rec.kadaiIcon());
			/*
			 * このセクションで使用しているもはリストボックスを　YES にする
			 */
			if( krec.isUseThisKadaiKey( rec.kadai_key() ) ){ // 使っているかどうか調べる
				putParameter("_op_1","");
				putParameter("_op_2","selected");	// YES
			}else{
				putParameter("_op_1","selected");	// NO
				putParameter("_op_2","");
			}
			if(LOG.fa) LOG.println("□ _n = " + getParameter("_n"));
			printVector(exHtml);
		}
		
	}
	public	void	kadai_seq_list(Vector exHtml){
		/*
		 * 課題画面でシーケンス番号リストを表示する
		 * 何番目の課題かを受け取る
		 */ 
		if(LOG.fa) LOG.println("■ WorkList #kadai_seq_list");
		if(LOG.fa) LOG.println("□ seq = " + getParameter("_n"));
		
		int		seq		=	Integer.parseInt( getParameter("_n") );
		int 	total	=	total(teUid, lec_key);
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
	 * この科目の全ての定義済み課題の総数を得る
	 */
	int	total(String teUid, String lec_key){
		
		String 		te_lec_key	= KeyGen.get_te_lec_key2(teUid, lec_key);
		KadaiDEF	kadaiDef	= new KadaiDEF(te_lec_key,db);
		return	kadaiDef.size();
  	}	
  	
	/**
	 * 部分的に切り取ったソースデータ（exHtml）を出力するか否か、
	 */
	public void	blankColumn(Vector exHtml){
		if(LOG.fa)  LOG.println("課題登録画面でブランク行を表示します");
		//
		int	n	= Integer.parseInt( getParameter("_kadaiDefSize") );	// 既に表示した行があるか
		if(n > 0)	return;
		//
		printVector(exHtml);
		return;
	}

}
