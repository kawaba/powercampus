//import	epml.*;
package kamoku;

import java.io.*;
import java.util.*;

import database.*;
import framework.*;
import	tktools.*;
import refer.*;
import kadai.*;
/**
*
*
	#
	# ##################
	#   KamokuPlan
	# ##################
	#
	<program $kamoku.KamokuPlan>
		<dispatch  html=kamokuPlan.html  number=1150  class=kamoku.KamokuPlan />
		<variable>
		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA lec_key notation />
		  <accept    CMD    UPLODE />
		  <keep      totalSections   seq_old  sect_key />
		  
		  <form      seq  wseq  subject  contentPlan  todo/>
		</variable>
	</program> 

 *
 *	notation	--	呼び出し元で設定した初期表示用メッセージ
 *
 *	totalSections	総セクション数
 *
 * 	sect_key		現在、何番目のセクションを表示しているか．
 * 　　　　			最初は"" ．作成して保存する際に設定される．
 * 
 *	seq_old			現在のシーケンス番号（変更を感知するためWebに保存）
 *
**/
public class KamokuPlan  extends SuperPlayer implements KamokuVar{
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
	
	public	KamokuPlan(){
		super();
		if(LOG.fa) LOG.println("■ Sample #コンストラクタ");
	}
	//	
	/**
	 * イニシャライザ
	 * コンストラクタが引数をもてないので、コンストラクタで行う内容をこの initialize() に書く．
	 * out, htb, para は SuperPlayer クラスのインスタンス変数なので、
	 * 引数にとらずとも利用できるが、変数を明示する意味で列挙してある
	 * 
	 * @param out			出力ポインタ
	 * @param htb			システムハッシュ
	 * @param para			パラメータ	 * 
	 */
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
	public	String	dispatch(){
		if(LOG.fa) LOG.outHash(htb,"■SKamokuPlan#dispatch()");
		if(LOG.fa) LOG.println("■KamokuPlan#dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		if(cmd.equals("UPDATE")){
			doUpadte();
			ret	=	DISPATCH_DEFAULT;	// 再表示

			
		}else if(cmd.equals("SECT_LIST")){
			// リストボタンのシーケンス番号からレコードを表示
			String 		seq = getParameter("seq");
			boolean 	ok 	= editSecRecordFromList(seq,htb);
			if(ok){
				disp_mode	=	DISP_EDIT;
			}else{
				disp_mode	=	DISP_NEW;;
			}
			ret	=	DISPATCH_DEFAULT;
			
		}else if(cmd.equals("BACK")){
			go_back2(htb);
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
			
		}else if(cmd.equals("FWD")){
			go_forward2(htb);
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
			
		}else if(cmd.equals("CLEAR")){
			// 新規モードで表示し直す(sect_key を消すことが大事)
			disp_mode	=	DISP_NEW;
			ret			=	DISPATCH_DEFAULT;
			
		
		}else if(cmd.equals("DELETE")){
			// セクションの削除
			// セクションキーがあれば更新モード
			String sect_key = getParameter("sect_key");
			if(  (sect_key==null) || (sect_key.length()==0)  ){
				putParameter("_msg","☆ 新規のデータは削除できません.消すには［クリア］を押してください");
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;
				
			}else{
				String te_lec_key		= KeyGen.get_te_lec_key(htb);
				KamokuSecDefRecord ksd	= new KamokuSecDefRecord();
				ksd.set_te_lec_key(te_lec_key);
				ksd.set_sect_key(sect_key);
				setUpSequence(htb, "DELETE");	// （削除の前に）シーケンス番号を調整する
				//
				ksd.delete( db );	// 削除
				disp_mode	=	DISP_NEW;
				ret			=	DISPATCH_DEFAULT;
			}
		}else if(cmd.equals("EDIT")){
			// レコードをデータベースから取得する
			editSecRecord(htb,para);
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
			
		}else if(cmd.equals("REF_EDIT_WRT")){
			// 資料編集へ
			if( doUpdateSecOK(htb)){
				/*
				 * セクションレコードを保存しておく
				 * sect_key があれば更新、なければ新規モードで処理
				 */
				String sect_key = getParameter("sect_key");
				if((sect_key==null)||(sect_key.length()==0)){
					addSecRecord(htb,para);
				}else{
					updateSecRecord(htb,para);
				}
				/*
				 * 資料編集へ移る
				 */
				disp_mode	=	DISP_NEW;
				ret			=	"$refer.RefList";
				
			}else{
				putParameter("_msg","☆ タイトル欄が空欄です。タイトル欄に記入がないと資料の登録はできません");
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;
			}
			
		}else if(cmd.equals("KADAI_EDIT_WRT")){
			// 課題編集へ
			if( doUpdateSecOK(htb)){
				/*
				 * セクションレコードを保存しておく
				 * sect_key があれば更新、なければ新規モードで処理
				 */
				String _sect_key = getParameter("sect_key");		// セクションキーを取り出す
				if((_sect_key==null)||(_sect_key.length()==0)){		// なければ新規
					addSecRecord(htb,para);
				}else{
					updateSecRecord(htb,para);						// あれば更新
				}
				/*
				 * 課題編集へ移る
				 */
				disp_mode	=	DISP_NEW;
				ret			=	"$kadai.WorkList";
				
			}else{
				putParameter("_msg","☆ タイトル欄が空欄です。タイトル欄に記入がないと課題の登録はできません");
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;
			}			
			
		}else if(cmd.equals("REF_EDIT")){
			/*
			 * 資料編集へ移る
			 */
			disp_mode	=	DISP_NEW;
			ret			=	"$refer.RefList";
			
		}else if(cmd.equals("KADAI_EDIT")){
			/*
			 * 課題編集へ移る
			 */
			disp_mode	=	DISP_NEW;
			ret			=	"$kadai.WorkList";
			
		}else if(cmd.equals("RETURN")){
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;	// 復帰				
			
		}else{
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;	// 復帰

		}
		/*
		 * 次に表示するときあるいはリターンしてきた時の表示モードをセットしてからリターンする
		 */
		putParameter(DISP_KEY,disp_mode);
		return	ret;	
	}
	
	public	void editRecord(Hashtable ht){
		String		teUid	= strHash(ht,"_teUid");
		String		lec_key	= strHash(ht,"_lec_key");
		KamokuDefRecord	kr	= new KamokuDefRecord( teUid, lec_key, db);	// データベースを引いて生成
		kr.setToHash(ht);
		//
		if(LOG.fa) LOG.outHash(ht,"■ class exwk #editRecord() : ハッシュの内容です");
		return;
	}
	//
	//　レコードを書き込んで（次の）レコードを表示する
	//
	void doUpadte(){
		if( doUpdateSecOK(htb)){
			/*
			 * 自動ページ送りをするかどうか（するときは "ON")
			 */
		    String autoChange	=	getParameter("autoChange");
		    /*
			 *  sect_key があれば更新、なければ新規モードとする
			 */
			String _sect_key = getParameter("_sect_key");// セクションキーを取り出す
			if((_sect_key==null)||(_sect_key.length()==0)){
				// 新規
				addSecRecord(htb,para);
				if(!isEmpty(autoChange)){
				    disp_mode	=	DISP_NEW;
				}else{
				    /*
				     * レコードを進めないので，このままでは seq_old（レコード番号）が空になる
				     * レコード番号を変更したとき変更前のレコード番号が "" だとアボートするので
				     * ここでは現在のレコード番号を seq_old に代入しておく
				     */
				    putParameter("seq_old", getParameter("seq"));
				    
				    disp_mode	=	DISP_EDIT;
				}

			}else{
				// 更新
				updateSecRecord(htb,para);
				putParameter("_msg","★ 内容を更新しました");
				//
				// 次のレコードへ進める
				if(!isEmpty(autoChange)){
				    String total = getParameter("_totalSections");
				    String seq	 = getParameter("_seq");
				    //
				    int	max	= Integer.parseInt( total );
				    int	cur	= Integer.parseInt( seq );
				    if(cur < max){
				        /*
				         * 次のレコードをセットして表示する
				         */
				        ++cur;
				        editSecRecordFromList(String.valueOf(cur),htb);
				        disp_mode	=	DISP_EDIT;	// 表示
					
				        // 次がなければ新規入力用のレコードを表示する
				    }else{
				        disp_mode	=	DISP_NEW;	// 新規表示
				    }
				}else{
				    disp_mode	=	DISP_EDIT;
				}
			}
		}else{
			putParameter("_msg","☆ 回数またはタイトル欄が空白です。入力するか編集するかしないと［登録／更新］はできません");
			disp_mode	=	DISP_EDIT;	// 継続表示
		}
	}
	
	//
	//  ひとつ先へ
	//
	void go_back2(Hashtable htb){
		String total = getParameter("_totalSections");
		String seq	 = getParameter("_seq");
		//
		int	max	= Integer.parseInt( total );
		int	cur	= Integer.parseInt( seq );
		if(cur > 1){
			--cur;
		}else{
			cur = max;
		}
		editSecRecordFromList(String.valueOf(cur),htb);
		return;
	}
	//
	//　ひとつ前へ
	//
	void go_forward2(Hashtable htb){
		String total = getParameter("_totalSections");
		String seq	 = getParameter("_seq");
		//
		int	max	= Integer.parseInt( total );
		int	cur	= Integer.parseInt( seq );
		if(cur < max){
			++cur;
		}else{
			cur = 1;
		}
		editSecRecordFromList(String.valueOf(cur),htb);
	}
	
	// セクションリストを得て、シーケンス番号からレコードを得る
	// そのレコードを編集領域へ移す
	public boolean editSecRecordFromList(String seq,Hashtable htb){
        if(LOG.fa) LOG.outHash(htb,"editSecRecordFromList()の先頭です");
		//
		// 全てのセクションレコードのリストを得て、シーケンス番号で該当のレコードを取り出す
		String	te_lec_key 		= KeyGen.get_te_lec_key(htb);
		KamokuSectionDEF ksdef	= new KamokuSectionDEF(te_lec_key,db);
		int	n	= ksdef.size();
		putParameter("_totalSections",String.valueOf(n));			// 全部の件数を覚えておく
		//
		int seqNo				= Integer.parseInt(seq) - 1 ;	// seq は１オリジン,seqNo は０オリジン
		// 最大番号なら空白レコード
		if(n <= seqNo){
			return false;
		}
		KamokuSecDefRecord ksd	= ksdef.get(seqNo);
		//
		putParameter("_seq",ksd.seq_number());
		String seq_old	= 	new String( ksd.seq_number() );
		putParameter("_seq_old"  ,seq_old);					// シーケンス番号を変えたかどうか判断するため覚えておく
		//
		putParameter("_sect_key"		,ksd.sect_key());	// セクションキー
		putParameter("_subject"		,ksd.title());
		putParameter("_contentPlan"	,ksd.content());
		putParameter("_todo"			,ksd.note());
		return true;
	}
	//
	//  編集に必要な項目をハッシュに入れる（科目登録画面２）
	//  （ sect_key は既にハッシュに入っている　)
	//
	public void editSecRecord(Hashtable htb,Param para){
        if(LOG.fa) LOG.outHash(htb,"editSecRecord()の先頭です");
		/*
		 * 全てのセクションレコードのリストを得てリストボックス表示のために全件数を覚えておく
		 */
		String	te_lec_key 		= KeyGen.get_te_lec_key(htb);
		KamokuSectionDEF ksdef	= new KamokuSectionDEF(te_lec_key,db);
		int	n	= ksdef.size();
		putParameter("_totalSections",String.valueOf(n));
		/*
		 * セクションキーで該当のレコードを得る
		 */
		String		sect_key	= getParameter("_sect_key");
		KamokuSecDefRecord ksd 	= new KamokuSecDefRecord( te_lec_key, sect_key, db);	// データベースを引いて生成
		putParameter("_seq"		,ksd.seq_number());
		String seq_old	= 	new String( ksd.seq_number() );
		putParameter("_seq_old"  ,seq_old);	// シーケンス番号を変えたかどうか判断するため覚えておく
		//
		putParameter("_subject"		,ksd.title());
		putParameter("_contentPlan"	,ksd.content());
		putParameter("_todo"		,ksd.note());
		return;
	}
	//
    //
	// 登録・更新処理をしていいか（順序番号とタイトル名が入力してあるか）
	//
	public boolean doUpdateSecOK( Hashtable htb ){
        if(LOG.fa) LOG.outHash(htb,"doUpdateSecOK()の先頭です");
		String subject = (getParameter("_subject")).trim();
		if(subject.length() == 0){ // 科目名が入力してあること
			return	false;
		}
		String seq = (getParameter("_seq")).trim();
		if(seq.length() == 0){ // 順序が入力してあること
			return	false;
		}
		return	true;
	}	
	//  ------------------------------
	//  シーケンス番号のチェックと更新    ※ htb にも
	//  ------------------------------
	//　　番号を挿入すると全体のシーケンス番号を書き換える必要がある
	//    全部のシーケンス番号を得て
	//
	//      INSERTモードのとき
	//         編集モードなら
	//            ０．番号を変えていなければ何もしない
	//　　　　　　１．今回の番号が飛び番号になっている（エラー）⇒　最終番号にした上で他のものの番号をずらす
	//        　　２．今回の番号がすでにある番号と重なっている　⇒　重なる番号のデータはシーケンスをひとつずらす
	//
	//         新規作成なら
	//			  １．今回の番号が飛び番号になっている（エラー）⇒　最終番号にするだけ
	//            ２．今回の番号がすでにある番号と重なっている　⇒　重なる番号のデータはシーケンスをひとつずらす
	//
	//      DELETEモードのとき
	//      　１．対象レコードより下のシーケンスのレコードは、シーケンス番号を１減らす
	// 		　２．ただし、最後のレコードの場合は、何もしない
	//
	String	setUpSequence(Hashtable htb, String mode){
        if(LOG.fa) LOG.outHash(htb,"setUpSequence()の先頭です");
		//
		String	_seq			= getParameter("_wseq");//////  wseq で見る
		String	_seq_old		= getParameter("_seq_old");
		String	_te_lec_key		= KeyGen.get_te_lec_key(htb);
		String	_sect_key		= getParameter("_sect_key");
		//
		if(mode.equals("EDIT")){
			if( _seq_old.equals(_seq) )	return	_seq; 			// 編集モードでシーケンス番号を変えていないときはなにもしない
			_seq = setupSeqUpdate(_te_lec_key, _seq_old,_seq);	// 飛び番号なら最大番号にしたシーケンス番号、それ以外はもとのまま
		}
		else if(mode.equals("NEW")){
			_seq = setupSeqNew(_te_lec_key, _seq);	// 新規入力モード
			//
		}else{	// DELETE モード
			_seq = setupSeqDel(_te_lec_key, _seq);
		}
		String new_seq = get00String(_seq);	// 表示のためhtb にも値を登録しておく
		putParameter("_seq",new_seq);
		return	new_seq;
	}
	//
	//
	//　削除モードの場合
	//
	String setupSeqDel(String _te_lec_key, String _seq){
        if(LOG.fa) LOG.println("setupSeqDel()の先頭です/ te_lec=" + _te_lec_key + ",seq=" + _seq);
		KamokuSectionDEF ksd	= new KamokuSectionDEF(_te_lec_key,db);
		int max		= ksd.size();
		int seq		= Integer.parseInt(_seq);
		if(seq == max)	return String.valueOf(seq);	// 最大番号のレコードならなにもしない
		//
		for(int k=seq+1; k<=max; k++){		// new+1 から max までのシーケンス番号を－１する
			int N = getRecordNumber(k);		// シーケンスは１オリジンだが、レコードは０オリジンなので－１する
			KamokuSecDefRecord rec = ksd.get(N);
			rec.set_seq_number( get00String( String.valueOf(k-1) ) );	// 00 形式で書き込むこと
			rec.update(db);
		}
		return String.valueOf(seq);
	}
	//
	//　新規モードの場合
	//
	String setupSeqNew(String _te_lec_key, String _seq){
        if(LOG.fa) LOG.println("setupSeqNew()の先頭です/ te_lec=" + _te_lec_key + ",seq=" + _seq);
		//
		KamokuSectionDEF ksd	= new KamokuSectionDEF(_te_lec_key,db);
		int max		= ksd.size();
		int seq		= Integer.parseInt(_seq);
		if(seq >= max + 1){
		 	seq = max + 1;					// 最大番号にする（飛び番号は最大番号になる）
			return String.valueOf(seq);		// あとはなにもしない
		}
		for(int k=seq; k<=max;  k++){		// new から max までのレコードのシーケンス番号を＋１する
			int N = getRecordNumber(k);		// シーケンスは１オリジンだが、レコードは０オリジンなので－１する
			KamokuSecDefRecord rec = ksd.get(N);
			rec.set_seq_number( get00String( String.valueOf(k+1) ) );	// 00 形式で書き込むこと
			rec.update(db);
		}
		return	String.valueOf(seq);
	}
	//
	//  編集モードでシーケンス番号を変えたときの処理
	//
	String setupSeqUpdate(String _te_lec_key, String _seq_old, String _seq){
        if(LOG.fa) LOG.println("setupSeqInsert()の先頭です/ te_lec=" + _te_lec_key + ",seq_old=" + _seq_old + ",seq=" + _seq);
		//
		KamokuSectionDEF ksd	= new KamokuSectionDEF(_te_lec_key,db);
		int max		= ksd.size();
		int seq		= Integer.parseInt(_seq);
		if(seq >= max + 1){
		 	seq = max;	// 最大番号にして以下の処理を適用する
		}
		int seq_old	= Integer.parseInt(_seq_old);
		if(seq < seq_old){
			for(int k = seq; k<seq_old; k++){	// new から old-1 までのレコードのシーケンス番号を＋１する
				int N = getRecordNumber(k);		// シーケンスは１オリジンだが、レコードは０オリジンなので－１する
				KamokuSecDefRecord rec = ksd.get(N);
				rec.set_seq_number( get00String( String.valueOf(k+1) ) );	// 00 形式で書き込むこと
				rec.update(db);
			}
		}else{
			for(int k = seq; k>seq_old; k--){	// new から old+1 までのレコードのシーケンス番号を－１する
				int N = getRecordNumber(k);		// シーケンスは１オリジンだが、レコードは０オリジンなので－１する
				KamokuSecDefRecord rec = ksd.get(N);
				rec.set_seq_number( get00String (String.valueOf(k-1) ) );	// 00 形式で書き込むこと
				rec.update(db);
			}
		}
		return	String.valueOf(seq);
		//
	}
	// 
	// シーケンス番号からレコード番号を得る
	//
	int getRecordNumber(int k){	
		return	k-1;	// １オリジンをゼロオリジンに補正する
	}
	// セクションデータを新規作成する
	//
	void addSecRecord(Hashtable htb,Param para){
        if(LOG.fa) LOG.outHash(htb,"addSecRecord()の先頭です");
		//
		String teUid		= getParameter("_teUid");
		KeyGen kg			= new KeyGen(teUid,db);
		//
		String  te_lec_key	= KeyGen.get_te_lec_key(htb);	// teUid と lec_key を別々に持っているので
		String	sect_key	= kg.nextSect();				// ５桁のセクションキー
		String	seq			= getParameter("_seq");	
		String	subject		= getParameter("_subject");	
		String	contentPlan	= getParameter("_contentPlan");	
		String	todo		= getParameter("_todo");
		//
		// シーケンス番号のチェックと更新
		seq = setUpSequence(htb,"NEW");
		putParameter("_seq",seq);// 念のため
		//
		// レコードに値を入れて書き込む
		KamokuSecDefRecord	ksd	= new KamokuSecDefRecord();		// 空の科目レコードを生成
		//
		ksd.set_te_lec_key(te_lec_key);
		ksd.set_sect_key(sect_key);
		ksd.set_seq_number(seq);
		//
		ksd.set_title(subject);
		ksd.set_content(contentPlan);
		ksd.set_note(todo);
		//
		ksd.insert(db);	// 書き込み
		///////////////////////////////////////////
		// 後の処理で必要になるのでhtbに入れておく
		
		putParameter("_sect_key",sect_key);
		putParameter("_seq",seq);
		///////////////////////////////////////////
		return;
	}
	//
	// セクションデータを更新する
	//
	void updateSecRecord(Hashtable htb,Param para){
        if(LOG.fa) LOG.outHash(htb,"updateSecRecord()の先頭です");
		//
		String  te_lec_key	= KeyGen.get_te_lec_key(htb);
		String	sect_key	= getParameter("_sect_key");
		String	seq			= getParameter("_seq");	
		//
		String	subject		= getParameter("_subject");	
		String	contentPlan	= getParameter("_contentPlan");	
		String	todo		= getParameter("_todo");
		//
		// シーケンス番号のチェックと更新
		seq = setUpSequence(htb,"EDIT");
		//
		// レコードを一度検索してから値を入れて更新する（課題リスト、資料リストを消さないため）
		KamokuSecDefRecord	ksd	= new KamokuSecDefRecord(te_lec_key,sect_key,db);
		//
		ksd.set_te_lec_key(te_lec_key);
		ksd.set_sect_key(sect_key);
		ksd.set_seq_number(seq);
		//
		ksd.set_title(subject);
		ksd.set_content(contentPlan);
		ksd.set_note(todo);
		//
		ksd.update(db);	// 書き込み
		//
		return;
	}
	//
	////////////////////////////////////////////////////////////////////////////////////
	//
	//   　　　出　　力　　処　　理
	//
	////////////////////////////////////////////////////////////////////////////////////
	//
	/**
	 * 表示処理本体
	 * @param editmode	編集モードのとき true
	 */
	public  void	display(boolean editmode){
		if(LOG.fa)	LOG.println("■ kamokuPlan #display(boolean editmode)");

		if(!editmode){
			//
			// シーケンスリストボックスに次の番号を表示するため、現在のレコード数を求める
			String te_lec_key 			= KeyGen.get_te_lec_key(htb);
			KamokuSectionDEF	ksd 	= new KamokuSectionDEF(te_lec_key,db);
			int					total	=  ksd.size();
			putParameter("totalSections",String.valueOf(total));
			putParameter("seq"			,get00String( String.valueOf(total + 1)) );	// 次だから１プラス
			//
			putParameter("seq_old"		,"");
			/*
			 * 呼び出し元から表示依頼されているメッセージがないかどうか調べる
			 */
			String	premsg	=	getParameter("notation");
			if(isEmpty(premsg)){
			    putParameter(MESSAGE		,"★ 作成済みの計画を編集するには下段のリストの<span style='color:#6699CC'>タイトル</span>をクリックしてください．");
			
			}else{
			    putParameter(MESSAGE, premsg);
				    
			}
			putParameter("subject"		,"");
			putParameter("contentPlan"	,"");
			putParameter("todo"			,"");
			putParameter("sect_key"		,"");	// セクションキー
			/*
			 * 初期値は自動送りＯＮ
			 */
			putParameter("autoChange", "ON");
		}
		String autoChange	=	getParameter("autoChange");
		/*
		 * 自動送りのチェックボックス表示を設定
		 */
		if(!isEmpty(autoChange)){
		    putParameter("checked_autoMode", "checked");
		}else{
		    putParameter("checked_autoMode", "");
		}
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);	
		return;
	}

	//
	// ハッシュテーブル(htb)を使って部分的に切り取ったソースデータ（exHtml）
	// の内容を置き換えて出力処理する．表などの反復出力に利用するが個々の処理
	// 内容は、key で特定される．
	//
	public void	write(String key,Vector exHtml){
		if(LOG.fa) LOG.println("■kamokuPlan #write()");
		
		if(key.equals("selectSection")){
			selectSection(exHtml);

		}else if(key.equals("changeSectionSeq")){
			changeSectionSeq(exHtml);
			
		}else if(key.equals("Def_ref_List")){
			disp_refDefList(exHtml,"_kx","_refIconx","_refx","_ref_urlx");
		
		}else if(key.equals("Def_kadai_List")){
			disp_kadaiDefList(exHtml,"_mx","_kadaiIconx","_kadaix");

		}else if(key.equals("sectDefList")){
			sectDefList(exHtml);
			
		}else if(key.equals("referenceList")){
			referenceList(exHtml);
			
		}else if(key.equals("kadaiList")){
			kadaiList(exHtml);
			
		}else if(key.equals("refDefList")){
			disp_refDefList(exHtml,"_k","_refIcon","_ref","_ref_url");

		}else if(key.equals("kadaiDefList")){
			disp_kadaiDefList(exHtml, "_m", "_kadaiIcon", "_kadai");

		}
	}
	void	referenceList(Vector exHtml){
		if(LOG.fa) LOG.println("■kamokuPlan #referenceList()");
		/*
		 * このセクションに属する全ての資料キーのリスト refkeys を得てその個数を調べる
		 */
		String 				te_lec_key	= 	KeyGen.get_te_lec_key(htb);   // 講義キー
		String 				sect_key	= 	getParameter("_sect_key");
		KamokuSecDefRecord	ksdRec		= 	new  KamokuSecDefRecord(te_lec_key, sect_key,db);
		Csv 				refkeys 	= 	ksdRec.refKeys();
		int 				n 			= refkeys.size();// 資料キーの数
		if(n==0){
			return;

		}else{
			printVector(exHtml);
		}
	}
	void	kadaiList(Vector exHtml){
		if(LOG.fa) LOG.println("■■kamokuPlan #kadaiList()");
		/*
		 * このセクションに属する全ての資料キーのリスト refkeys を得てその個数を調べる
		 */
		String 				te_lec_key	= 	KeyGen.get_te_lec_key(htb);   // 講義キー
		String 				sect_key	= 	getParameter("_sect_key");
		// このセクションに属する全ての課題キーのリスト kadaikeys を得る
		KamokuSecDefRecord	ksdRec		= new  KamokuSecDefRecord(te_lec_key, sect_key,db);
		Csv 				kadaikeys 	= ksdRec.kadaiKeys();
		int 				n 			= kadaikeys.size();// 課題キーの数
		if(n==0){
			return;

		}else{
			printVector(exHtml);
		}
	}
	void	selectSection(Vector exHtml){
		if(LOG.fa) LOG.println("■kamokuPlan #selectSection()");
		seqOptionList(exHtml);
	}
	void	changeSectionSeq(Vector exHtml){
		if(LOG.fa) LOG.println("■kamokuPlan #changeSectionSeq()");
		seqOptionList(exHtml);
	}	
	/**
	 * 上段のシーケンス変更のオプションリスト,セクション選択のオプションリストを表示する
	 * どちらも同じ処理でよい．
	 * @param exHtml
	 */
	void	seqOptionList(Vector exHtml){
		if(LOG.fa) LOG.println("■kamokuPlan #seqOptionList()");
		
		String total = getParameter("_totalSections");
		String seq	 = getParameter("_seq");
		if(LOG.fa) LOG.println("           total = " + total);
		if(LOG.fa) LOG.println("           seq   = " + seq);
		//
		if( total == null){
			LOG.errStop( out,"総セクション数が設定されていない : paramPrintOPT() key = sectDef_optionList" );
			return;
		}
		int kei		= Integer.parseInt(total);
		int seqNo	= Integer.parseInt(seq);
		for(int i=0; i<kei+1; i++){
			// value
			String opt = String.valueOf(i+1);
			putParameter("_x",opt);
			// selected
			if(seqNo==(i+1)){
				putParameter("_sel","selected");
			}else{
				putParameter("_sel","");
			}
			// ラベル
			putParameter("_xx",getSStype(i+1));
			printVector(exHtml);
		}
		
	}
	/**
	 * 下段のセクション一覧を表示する
	 * この表示の中で各回ごとに資料一覧と課題一覧を表示する
	 * @param exHtml
	 */
	void	sectDefList(Vector exHtml){
		if(LOG.fa) LOG.println("■kamokuPlan #sectDefList()");
		
		String te_lec_key		= 	KeyGen.get_te_lec_key(htb);   // 科目キー
		if(!KeyGen.is_te_lec_key(te_lec_key)){
			LOG.errStop( out,"科目キーがない.または異常(001) :" + te_lec_key + ":" );
			return;
		}
		//
		if(LOG.fa) LOG.println("sectDefList: te_lec_key = " + te_lec_key);
		if(LOG.fa) LOG.println("sectDefList: この科目の全てのセクションレコードを取得します");
		KamokuSectionDEF ksd	= new KamokuSectionDEF(te_lec_key, db);
		int	n	= ksd.size();
		for(int i=0; i<n; i++){
			KamokuSecDefRecord	 	ksdrec	= ksd.get(i);
			putParameter("_e"      ,  	StringGear.get00type(i+1));		// 編集用アイコンの番号、１オリジン
			putParameter("_subject",  	(ksdrec.title()).trim());
			putParameter("_contentPlan", (ksdrec.content()).trim());
			putParameter("_todo", 	 	(ksdrec.note()).trim());
			//
			// 表示のために
			htb.put("ref_list",ksdrec.refKeys());		// 資料リスト Csv （ 先頭に _ をつけないとデバッグでは表示されない）
			htb.put("kadai_list",ksdrec.kadaiKeys());	// 課題リスト Csv
			putParameter("_sect_key",ksdrec.sect_key());		// セクションキー
			printVector(exHtml);
		}
	}
	
	/**
	 * 資料リストを表示する
	 * @param exHtml
	 * @param s1
	 * @param s2
	 * @param s3
	 * @param s4
	 */
	void disp_refDefList(Vector exHtml,String s1,String s2,String s3,String s4){
		if(LOG.fa) LOG.println("■kamokuPlan #disp_refDefList()");
		
		String te_lec_key		= 	KeyGen.get_te_lec_key(htb);   // 講義キー
		if( !KeyGen.is_te_lec_key(te_lec_key)){
			LOG.errStop( out,"講義キーがない.または異常(002) :" + te_lec_key + ":" );
			return;
		}

		// この科目の資料レコードを全て得る
		String sect_key		= getParameter("_sect_key");
		ReferenceDEF refDef	= new ReferenceDEF(te_lec_key, db);	
		//
		// このセクションに属する全ての資料キーのリスト refkeys を得る
		KamokuSecDefRecord	ksdRec	= new  KamokuSecDefRecord(te_lec_key, sect_key,db);
		Csv refkeys = ksdRec.refKeys();
		/*
		 * シーケンスでソート済みのレコード
		 * n は古いデータでは削除されたものも含むので実際にそれだけの資料は存在しない可能性がある
		 */
		int 	n 	= refkeys.size();
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
			int itemNember	=	1;
			for(int i=0; i<n; i++){ // 全てのキーから
				/*
				 * 資料キーでハッシュを検索するが、古いレコードでは削除されたキーもふくまれているので
				 * null が返されることがある。
				 */
				ReferenceDefRecord	 rdrec	= refDef.fromKeyHash( te_lec_key, refkeys.get(i) ); // キーでハッシュを検索する
				if(rdrec!=null){
					putParameter(s1, String.valueOf(itemNember) + ")" );	// 項目番号表示用
					itemNember++;
					
					putParameter(s2, rdrec.refIcon());				// アイコンデータ名
					putParameter(s3, rdrec.title());  				// 資料名
					if( !isEmpty(rdrec.url())){
						putParameter(s4, rdrec.url());  	// URLがあればそれだけでリンクを張る事に変更 2003.9.4
						putParameter("_ref_blank","");	// URL なしと置き換えをしない
					}else{
						putParameter("_ref_blank","replace");	// URL なしと置き換えをする
						putParameter(s4, "#");
					}
					//
					aliveRefkeys.add( rdrec.ref_key() );        // 有効なキーは記憶しておく
					if(LOG.fa) LOG.println("□□ alive に登録した refkeys.get() =" + rdrec.ref_key() +  " = " +  rdrec.title() );
					printVector(exHtml);
				}
			}
		}
	}
    /**
     * 課題リストを表示する
     * @param out
     * @param htb
     * @param para
     * @param exHtml
     * @param s1
     * @param s2
     * @param s3
     */
	void disp_kadaiDefList(Vector exHtml,String s1,String s2,String s3){
		if(LOG.fa) LOG.println("■kamokuPlan #disp_kadaiDefList()");
		//
		String te_lec_key		= 	KeyGen.get_te_lec_key(htb);   // 講義キー
		if( !KeyGen.is_te_lec_key(te_lec_key)){
			LOG.errStop( out,"講義キーがない.または異常(002) :" + te_lec_key + ":" );
			return;
		}
		// この科目の課題レコードを全て得る
		String sect_key		= getParameter("_sect_key");
		KadaiDEF kadaiDef	= new KadaiDEF(te_lec_key, db);	
		//
		// このセクションに属する全ての課題キーのリスト kadaikeys を得る
		KamokuSecDefRecord	ksdRec	= new  KamokuSecDefRecord(te_lec_key, sect_key,db);
		Csv kadaikeys 	= ksdRec.kadaiKeys();
		int 	n 		= kadaikeys.size();// 課題キーの数
		//
		if(n==0){
			putParameter(s1 , "-");			// 項目番号表示用
			putParameter(s2 , "spacer.gif");
			putParameter(s3 , "&nbsp;");
			printVector(exHtml);

		}else{
			int	itemNumber	=	1;
			Vector aliveKadaikeys = new Vector(10,10);
			for(int i=0; i<n; i++){ // 全てのキーから
				/*
				 * 課題キーでハッシュを検索するが、古いレコードでは削除されたキーもふくまれているので
				 * null が返されることがある。
				 */
				KadaiDefRecord	 kdrec	= kadaiDef.fromKeyHash( te_lec_key, kadaikeys.get(i) );
				if(kdrec!=null){
					putParameter(s1, String.valueOf(itemNumber) + ")" );	// 項目番号表示用
					itemNumber++;
					
					putParameter(s2, kdrec.kadaiIcon());				// アイコンデータ名
					putParameter(s3, kdrec.title());  				// 課題名
					putParameter("_kadai_key", kadaikeys.get(i));		// 課題キー
					//
					aliveKadaikeys.add( kdrec.kadai_key() );        // 有効なキーは記憶しておく
					if(LOG.fa) LOG.println("□□ alive に登録した kadaikeys.get() =" + kdrec.kadai_key() +  " = " +  kdrec.title() );
					printVector(exHtml);
				}
			}
		}
	}

}
