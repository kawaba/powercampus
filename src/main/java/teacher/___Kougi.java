
package teacher;

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
import kadai.KadaiDEF;
import kadai.KadaiDefRecord;
import kamoku.KamokuApRecord;
import kamoku.KamokuSecApRecord;
import kamoku.KamokuSecDefRecord;
import kamoku.KamokuSectionDEF;
import kamoku.SectionRecord;
import refer.ReferenceDEF;
import refer.ReferenceDefRecord;
import tktools.Csv;
import tktools.DateInfo;
import tktools.Gear;
import tktools.StringGear;
/**
 *	講義実施画面を表示する
 *
	#
	# ##################
	#   Kougi
	# ##################
	#
	<program $teacher.Kougi>
		<dispatch  html=kougi.html  number=500  class=teacher.Kougi />
		<variable>
		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA HOMEURL DIVISION title lec_key aplec_key  stNumber stPasswd stMail />
		  <accept    CMD    UPLODE    kadai_key sect_key />
		  <keep      sect_seq totalSections edit_mode/>
		  
		  <form      autodate_off secList memo/>
		</variable>
	</program>
 *	
 *
 * 1.keep変数
 *	sect_seq		--	現在、何番目のセクションを表示しているか（display() でセットして記録）
 *	totalSections	-- 	この科目の総セクション数（display() でセットして記録）
 *	edit_mode		--  編集エリアにあるセクションのセクション実施レコードが記録済み("update")か否か("new")を記録
 *                      display()の中でset_sectionEditArea()を呼んで毎回セットする
 * 2.accept変数
 *	kadai_key		--  課題名をクリックした時に返される課題キー
 *	sect_key		--	課題編集、資料編集を選択した時に返されるセクションキー．あるセクションを特定するためのキー．
 *	
 * 3.form変数
 *	autodate_off	--	「日付の自動更新を行なわない」を選択するチェックボックス
 *	secList			--	上段の編集領域のリストボックスで選んだ表示セクションのシーケンス番号（ "1" "2" など）
 *	memo			--	講義メモを書くテキストエリア
 *
*/
public class ___Kougi extends SuperPlayer implements BbsVar{

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
	/**
	 * フォーラム用識別子
	 */
	String			relation;	// forumDB の relation に設定する識別子
	

	String		szDB;
	String		teUid;

	public	___Kougi(){
		super();
		if(LOG.fa) LOG.println("■ Kougi #コンストラクタ");
		
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
		teUid	= 	getParameter("_teUid");
		szDB	= 	getParameter("_szDB");


	}
	void	setRelation(){
		/*
		 * フォーラムのための識別子を作成しシステムハッシュに格納しておく 
		 * ここでの識別子には年度が含まれる ⇒ 年度を越えてアクセスできない
		 * 
		 * 半期の講義時間割りに対応するためシーズン文字列を年度に加えた(2005.3)
		 * ex. 2005a-mwb9l124-103
		 * 
		 * // 2月までを同じ年度とする 2022.3 後期は２月まで
		 */
		relation	=	KeyGen.getRelation(teUid, getParameter("aplec_key"), "b,b,a,a,a,a,a,a,b,b,b,b");
		putParameter(BBS_RELATION, relation);
		
		if(LOG.fa) LOG.println("Kougi #initialize() : relation=" + relation);
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
		if(LOG.fa) LOG.outHash(htb,"■Kougi #dispatch()");
		if(LOG.fa) LOG.println("■Kougi #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		putParameter("_invokeName", "");

		// セクションデータを書き込む
		if(cmd.equals("UPDATE")){
			//
			// この講義の実施データは「書き込み」ボタンで書き込むところだが、
			// 操作がわかりずらいので「更新」ボタンで毎回書き込むことにした
			writeLectureOP();
			writeSectionOP();				// データを書いて
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示
			
		// セクションデータを書き込み、自動発生フラグがOFFなら以降の日付も自動発生する
		}else if(cmd.equals("SETDATE")){
			writeSequenialSectionOP();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示
			
		// セクションデータを編集領域に転記する
		}else if(cmd.equals("SECT_EDIT")){// _sect_key と _sect_seq が htb に入っている
			//
			// 以下の２行は display() の中で行っているので不要
			//String seq = strHash(htb,"_sect_seq");	// シーケンス番号でデータをセットする
			//set_sectionEditArea(seq ,htb);
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示
			
		// リストボタンの指示からセクションデータを編集領域に転記する
		}else if(cmd.equals("SECT_LIST")){// _sect_key と _sect_seq が htb に入っている
			//
			String seq = getParameter("_secList");	// リストボタンのシーケンス番号でデータをセットする
			putParameter("_sect_seq",seq);
			//set_sectionEditArea(seq ,htb);
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示
		
		// フォーラム
		}else if(cmd.equals("FORUM")){
			
		    /*
		     * ユーザーデータベースに登録がなければ初期値を登録しておく
		     */
		    addToBbsUserDB();
		    /*
			 * 教師キーをBBSオーナーキーとして渡す
			 */
		    putParameter(BBS_OWNER_KEY, teUid);
		    /*
		     * リレーションキーをセットする
		     */
		    setRelation();
		    
		    disp_mode	=	DISP_NEW;// 新規モード
			ret			=	"$jbbs.BbsForum";
			
        // ファイルキャビネット
		}else if(cmd.equals("CABINET")){
			disp_mode	=	DISP_NEW;// 新規モード
			ret			=	"$cabinet.FileCabinet";
			
		// 時間割画面に戻る
		}else if(cmd.equals("RETURN")){
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;	// 復帰			

		// ひとつ前のセクションを編集領域に転記する
		}else if(cmd.equals("BACK")){
			//
			go_back();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示
			
		// ひとつ先のセクションを編集領域に転記する
		}else if(cmd.equals("FWD")){
			//
			go_forward();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示
			
		// セクションそのものを編集し直す
		}else if(cmd.equals("EDIT")){
		    kamokuEdit();
			disp_mode	=	DISP_EDIT;
			ret			=	"$kamoku.KamokuPlan";
		    
		// 資料を編集する
		}else if(cmd.equals("EDIT_REF")){
			disp_mode	=	DISP_NEW;// 新規モード
			ret			=	"$refer.RefList";

		// 課題を編集する
		}else if(cmd.equals("EDIT_KADAI")){
			disp_mode	=	DISP_NEW;// 新規モード
			ret			=	"$kadai.WorkList";
		
		}else if(cmd.equals("TERM")){
			disp_mode	=	DISP_NEW;// 新規モード
			ret			=	"$kadai.WorkTerm";

		// 課題を参照・採点する
		}else if(cmd.equals("KADAI")){
			
			disp_mode	=	DISP_NEW;// 新規モード
			ret			=	"$eval.EvalList";
			
		// メール送信画面へ
		}else if(cmd.equals("MAIL")){
			disp_mode	=	DISP_NEW;// 新規モード
			ret			=	"$hybridmail.HybridMail";

		// 名簿を参照・更新する
		}else if(cmd.equals("MEIBO")){
			disp_mode	=	DISP_NEW;// 新規モード
			ret			=	"$meibo.RegMeibo";
			
		// Ｑ＆Ａメール
		}else if(cmd.equals("QA")){
			disp_mode	=	DISP_NEW;// 新規モード
			ret			=	"$faq.CommentSheet";
			
		// FAQを見る
		}else if(cmd.equals("FAQ")){
			disp_mode	=	DISP_NEW;// 新規モード
			ret			=	"$faq.FaqTeacher";

		}else{
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示
			
			/*
			 * 別画面でプログラムを起動
			
			htb.put("_invokeName", menu);
			display();
            return;
             */
        }
		/*
		 * 次に表示するときあるいはリターンしてきた時の表示モードをセットしてからリターンする
		 */
		putParameter(DISP_KEY,disp_mode);
		return	ret;
		
    }
	/**
	 * 教師ユーザーをデータベースに登録する
	 * 
	 * 登録があるかどうか調べて登録および修正を行う
	 * 
	 * @param mail
	 * @param passwd
	 */
	void	addToBbsUserDB(){
	    
	    BbsInfoDB	bbsDB	=	new	BbsInfoDB(szDB, broker);
	    Hashtable	test	=	new	Hashtable();
	    int	result			=	bbsDB.readBbsInfo(getParameter(TUID), test);
	    if(result==0){
	        /* ＤＢに登録する */
	        insertRecord(bbsDB);
	    
	    }else{
	        /*
	         * 学生ユーザーとして、あるいはゲストユーザーとして最初に
	         * ログインしているとDIVISIONが学生、またはゲストで登録されている
	         * ここでDIVISION が正しくかどうかチェックして正しくなければ書き直す
	         */
	        String	division	=	strHash(test, DIVISION);
	        if(!division.equals(DIV_TEACHER)){
	            bbsDB.updateDiv(teUid, DIV_TEACHER);// DVISION だけを更新
	        }
	    }
	}
	/**
	 * 教師レコードをBBSデータベースに登録する
	 * @param bbsDB
	 */
	void	insertRecord(BbsInfoDB	bbsDB){

	    Hashtable ht	=	new	Hashtable();
	    db.MembersInfo(teUid, ht);
	    
	    String		pass	=	strHash(ht, PASSWORD);
	    String		name	=	strHash(ht, UNAME);
	    String		mail	=	strHash(ht, MAIL);
	    String		div		=	strHash(ht, DIVISION);
	    Hashtable	rec		=	BbsUtil.setInitialData(teUid, pass, div, name, mail, para);
	    
	    bbsDB.insertBbsInfo( teUid, rec);
	}

	void	kamokuEdit(){
		String	sect_seq	= 	getParameter("_sect_seq");
		putParameter("_seq",sect_seq);
		putParameter("autoChange", "ON");
		/*
		 * 初期表示するレコードをハッシュにセットしなくていけない
		 * （仕様がよくない．KamokuPlan を訂正する必要がある）
		 */
		editSecRecordFromList(sect_seq);
	
	}

	// セクションリストを得て、シーケンス番号からレコードを得る
	// そのレコードを編集領域へ移す
	boolean editSecRecordFromList(String seq){
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
		putParameter("_sect_key"	,ksd.sect_key());	// セクションキー
		putParameter("_subject"		,ksd.title());
		putParameter("_contentPlan"	,ksd.content());
		putParameter("_todo"		,ksd.note());
		return true;
	}
	//
	//  講義実施データを書き込む
	//
	void writeLectureOP(){
		String aplec_key	= getParameter("_aplec_key");
		KamokuApRecord kar	= new KamokuApRecord( teUid, aplec_key, db);
		//
		// データに変更がなければ書き込まない
		if(isNoedit(kar)){
			if(LOG.fa)  LOG.println("class exwork #writeLectureOP() : 講義実施データを書き込む、では同じデータなので書き込みはしません");
			return;
		}
		String yyyy		= getParameter("_yyyy");
		String term		= getParameter("_term");
		String unit		= getParameter("_unit");
		//
		kar.set_yyyy(yyyy);
		kar.set_term(term);
		kar.set_unit(unit);
		//
		kar.update(db);
		return;
	}
	boolean	isNoedit(KamokuApRecord kar){
		//
		String yyyy		= getParameter("_yyyy");
		String term		= getParameter("_term");
		String unit		= getParameter("_unit");
		//
		String yyyy2	= kar.yyyy();
		String term2	= kar.term();
		String unit2	= kar.unit();
		//
		// １つでも違っていると書き込み必要
		if(!yyyy.equals(yyyy2))		return	false;
		if(!term.equals(term2))		return	false;
		if(!unit.equals(unit2))		return	false;
		//
		return true;
	}
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
		putParameter("_sect_seq", String.valueOf(cur));
		//set_sectionEditArea(cur,htb);
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
		putParameter("_sect_seq", String.valueOf(cur));
		//set_sectionEditArea(cur,htb);
		return;
	}
	//  シーケンス番号の値を渡してセクション編集領域のデータをセットする
	//  シーケンス番号は１オリジンなので注意
	//
	void set_sectionEditArea(int seq ){
		set_sectionEditArea(String.valueOf(seq) );
	}
	/** **********************************************************************************************
	 * 
	 * ここで最上段に表示される「今日の講義内容」をセットしている
	 * _contentPlan が講義内容なので、これをPML化すると、多彩な表示が可能になる
	 * _contentPlan は、現状は、そのまま kougi.html に差し込まれている（503行目）
	 * kougi.htmlでは、_$contentPlanとなっている。$があると、差し替え文字列の中の改行を<br>に置き換える
	 * 
	 * 
	 *************************************************************************************************/
	void set_sectionEditArea(String seq ){
        if(LOG.fa) LOG.outHash(htb,"set_sectionEditArea()の先頭です");
        if(LOG.fa) LOG.println("シーケンス番号＝" + seq);
		//
		//-------------------------------------------------------------------------------------
		putParameter("_sect_seq", seq); 			// 現在のシーケンス番号を覚えておく
		//-------------------------------------------------------------------------------------
		//
		// 全てのセクションレコードのリストを得て、シーケンス番号で該当のレコードを取り出す
		
		int seqNo				= Integer.parseInt(seq) -1 ;	// seq は１オリジン,seqNo は０オリジン
		KamokuSecDefRecord rec = kamoku_sec_def().get(seqNo);
		//
		putParameter("_sect_key"	,rec.sect_key());		// セクションキー
		putParameter("_subject"	   	,rec.title());			// タイトル
		/*
		 * 講義内容の説明をPMLで記述するので、表示用のHTMLに変換する
		 * PML以外のテキストは従来どおり表示される
		 */
		String HTML_text = pml_to_Html(rec.content());
		putParameter("_contentPlan" ,HTML_text);			// 内容
		putParameter("_todo"        ,rec.note());			// 備考
		//
		// セクションキーとそれに付属する資料、課題のキーリスト
		String	sect_key = rec.sect_key();
		putParameter("_sect_key",sect_key);				// セクションキー
		//
		// セクション実施レコード（空の場合がある）
		// 年度や単位など。空の場合がある。
		// また、書き込みに備えてモードを設定しておく
		KamokuSecApRecord ksar	= new KamokuSecApRecord ( teUid_lec_key(), sect_key, db);
		if(ksar.isEmpty()){
			putParameter("_edit_mode","new");
		}else{
			putParameter("_edit_mode","update");
		}
		// 日付の曜日
		// 
		String	s1	=	ksar.dayOfWeekFrom(getParameter("_yyyy"));
		String	s2	=	ksar.dayOfWeekTo(getParameter("_yyyy"));
		if(!isEmpty(s1)){
			putParameter("_dw1", "［" + s1 + "］");
		}else{
			putParameter("_dw1", "");
		}
		if(!isEmpty(s2)){
			putParameter("_dw2", "［" + s2 + "］");
		}else{
			putParameter("_dw2", "");
		}
		//
		// シーケンス番号
		putParameter("_no",seq);
		// セクション実施レコードの表示データ
		putParameter("_s_mm",ksar.s_mm());
		putParameter("_s_dd",ksar.s_dd());
		putParameter("_e_mm",ksar.e_mm());
		putParameter("_e_dd",ksar.e_dd());
		putParameter("_memo",ksar.memo());	
        //
		if(LOG.fa) LOG.println("set_sectionEditArea()の出口です");
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
	//
	//  編集したセクション実施データを書き込む
	//
	void writeSectionOP(){
        if(LOG.fa) LOG.outHash(htb,"writeSectionOP()の先頭です");
		KamokuSecApRecord	ksar = new KamokuSecApRecord(); // 空のレコード
		String te_aplec_key		 = KeyGen.get_te_aplec_key(htb);
		String sect_key			 = getParameter("_sect_key");
		//
		ksar.set_te_aplec_key(te_aplec_key);
		ksar.set_sect_key(sect_key);
		ksar.set_s_mm( getParameter("_s_mm") );
		ksar.set_s_dd( getParameter("_s_dd") );
		ksar.set_e_mm( getParameter("_e_mm") );
		ksar.set_e_dd( getParameter("_e_dd") );
		ksar.set_memo ( getParameter("_memo") );
		
		//
		String mode = getParameter("_edit_mode");
		if(mode.equals("new")){
			ksar.insert(db);
		}else{
			ksar.update(db);
		}
	}
	//
	//  このセクションで設定した実施日を元に、後続のセクションにも実施日を書き込む
	//  ７日後というパターンを繰り返す
	//
	void writeSequenialSectionOP(){
        if(LOG.fa) LOG.outHash(htb,"writeSequenialSectionOP()の先頭です");
		//
		// Webで指定された日付とメモをセクション実施レコードに書き込む
		writeSectionOP();
		//
		// 自動設定フラグがOFFになっていればシーケンス番号を１プラスして戻る
		String	autodate_off	= getParameter("_autodate_off");
		if(!isEmpty(autodate_off,"_autodate_off")){
			go_forward();
			return;
		}
		//
		// 講義の実施年度を得る（課目実施レコードから）
		KamokuApRecord	kar		=	new	KamokuApRecord( teUid, getParameter("_aplec_key"), db );
		String			yyyy	=	kar.getYear();	// 4桁の半角数字文字列または""を返す
		if(yyyy.length()==0){
			// 年度が得られない場合は何もせずに復帰する
			putParameter(MESSAGE,"★ 最初に講義実施年度を西暦で書き込んでください");
			return;
		}
		GregorianCalendar	today	=	new GregorianCalendar();
		int					year	=	Gear.toInt(yyyy, today.get(Calendar.YEAR) );
		//
		// セクション定義クラスを作って総セクション数を求める
		String				te_lec_key	=	KeyGen.get_te_lec_key(htb);
		KamokuSectionDEF	ksDef		=	new KamokuSectionDEF(te_lec_key,db);
		int					max			=	ksDef.size();
		
		// 現在のセクション番号が総セクション数と同じなら処理を終了する
		String	secNoStr	=	getParameter("_sect_seq");
		int		secNo		=	Gear.toInt(secNoStr,1);		// 数値に直す．エラーなら１として返す．
		if(secNo >= max)	return;
		//
		// 現在のセクション実施レコードの日付を求める
		String				te_aplec_key	=	KeyGen.get_te_aplec_key(htb);
		KamokuSecApRecord	rec				=	new	KamokuSecApRecord(te_aplec_key,getParameter("_sect_key"),db);
		String				mm				=	rec.getStartMonth();
		String				dd				=	rec.getStartDay();
		if(Gear.isEmpty(mm) || Gear.isEmpty(dd)){
			// 月と日が得られない場合は何もせずに復帰する
			putParameter(MESSAGE,"★ 実施月と実施日を記入してください");
			return;
		}
		int	month	=	Gear.toInt(mm, today.get(Calendar.MONTH) + 1 );
		int	day		=	Gear.toInt(dd, today.get(Calendar.DATE) );
		//
		// 開始日付のカレンダーオブジェクトを生成する
		// 月はゼロオリジンなので１引いておく
		GregorianCalendar	cal	=	new GregorianCalendar(year,month-1,day);
		
		
		// 残りのセクション実施レコードについてシーケンスエンドになるまで以下を繰り返す
		for(int	k=secNo+1; k<=max; k++){
			// セクションのシーケンス番号から次のセクションキーを得て
			// 日付をセットするためにセクション実施レコードを取得する
			String				sectKey		=	ksDef.sect_key(k-1);	// ゼロオリジンなので
			KamokuSecApRecord	secApRec	=	new	KamokuSecApRecord(te_aplec_key,sectKey,db);
			//
			// ７日周期の次の日時を得てレコードに書き込む
			// 休日はスキップされている
			DateInfo			dinfo		=	getNextDay(cal,7);
			//
			secApRec.set_te_aplec_key(te_aplec_key);
			secApRec.set_sect_key(sectKey);
			secApRec.set_s_mm( dinfo.month() );
			secApRec.set_s_dd( dinfo.day() );
			//
			//　セクション実施レコードを書き込む
			int	n	=	secApRec.update(db);
			if(n==0){
				n = secApRec.insert(db);
			}
		}
		// 次の回へ進めておく
		go_forward();
	}
	
	/**********************************************************
	 * 
	 * 	年を生成する処理を追加した　2024.3.2
	 *   
	 ***********************************************************/
	// セクション番号から年の値を得る
	private int yearNumber(int sectionNumber) {
		int 	yy = Integer.parseInt(getNendo()); 						// 講義の実施年度を得る
		int		newYearSectionNumber = SectionNumberForTheNewYear(yy);	// それ以降が新年度になるセクション番号
		return	newYearSectionNumber<=sectionNumber ? yy+1 : yy; 		// より大きなセクション番号なら年を+1する
	}
	
	/**********************************************************
	 * 
	 * 	新年度になるセクション番号を返す　2024.3.2
	 *   
	 ***********************************************************/
	private int SectionNumberForTheNewYear(int yyyy){
		//
		int 	ajustNumber 	= 0;				// 年度を年に変換するための調整値（０か１）
		int 	currentMonth 	= -1;				// 比較する月の値（1-12）
		int		returnValue	= -1;
		
		// シーケンス順にセクションをチェックする
		int max = sectionSize();
		for(int	index=0; index<max; index++){
			
			SectionRecord record = secRecord(index);	// セクションレコードを得る
			int recordMonth 	= record.startMonth();	// レコードの月の値

			// 前回の月よりも今回の月が小さければ、ajustNumberを1にする
			if(currentMonth>recordMonth) {
				ajustNumber = 1;
			}
			currentMonth 		= recordMonth;			// 比較する月の値を更新する

			LocalDate recordDate = record.startDay(yyyy + ajustNumber); 	// レコードの日付
			LocalDate today 	 = LocalDate.now();							// 今日の日付
			
			// 今日の日付と同じかより大きければ、それが新年度最初のセクション
			if(today.equals(recordDate) || today.isBefore(recordDate)) {
				returnValue =index;
				break;
			}
		}
		return	returnValue;
	}	
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	/** **************************************************************************
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
		return getParameter("_aplec_key");
	}

	// 教師の講義定義キーを返す
	private String teUid_lec_key() {
		return teacher_id() + "-" + lec_key();
	}

	// 教師の講義実施キーを返す
	private String teUid_aplec_key() {
		return teacher_id() + "-" + aplec_key();
	}

	// index番目のセクションキーを返す
	private String section_key(int index) {
		return kamoku_sec_def().sect_key(index);
	}

	//////////////////////////////////////////////////////////////////
	// 科目の実施レコードを返す
	private KamokuApRecord kamoku_ap_record() {
		return new KamokuApRecord(teacher_id(), aplec_key(), db);
	}

	// セクション定義レコードを取得する
	private KamokuSectionDEF kamoku_sec_def() {
		return new KamokuSectionDEF(teUid_lec_key(), db);
	}

	// 科目の中のindex番目のセクションレコードを返す
	private SectionRecord secRecord(int index) {
		return new KamokuSecApRecord(db, teUid_aplec_key(), section_key(index)).getRecord();
	}

	////////////////////////////////////////////////////////////////////
	// 実施年度を返す
	private String getNendo() {
		return kamoku_ap_record().getYear();
	}

	// セクションの数を返す
	private int sectionSize() {
		return kamoku_sec_def().size();
	}

	//
	// interval 日後を計算して DateInfo オブジェクトを返す
	// 休日ならさらにinterval 日後を計算する
	DateInfo	getNextDay(GregorianCalendar cal, int interval){
		//
		// interval 日後を計算して休日ならばさらにそれを繰り返す
		// 休日でない interval 日後の DateInfo を求める
		DateInfo	dif	= getNextDaySub(cal,false,interval);
		while(dif.isHollyday()) {
			dif	=	getNextDaySub(cal, dif.furikaeFlag(), interval);
		}
		return	dif;
	}
	DateInfo	getNextDaySub(GregorianCalendar cal, boolean flag,int interval){
		//
		DateInfo	dif	= new DateInfo(cal,flag);
		for(int k=1; k<=interval; k++){
			cal.add(Calendar.DAY_OF_MONTH,1); 
			dif	=	new DateInfo(cal,dif.furikaeFlag());
		}
		return	dif;
	}
	////////////////////////////////////////////////////////////////////////////////////
	//
	//   　　　出　　力　　処　　理
	//
	////////////////////////////////////////////////////////////////////////////////////
	//
	//
	// 講義画面を表示する
	//
	@Override
	public void display(boolean editmode){
		if(LOG.fa) LOG.println("■ Kougi #display(boolean editmode)");
		
		if(!editmode){
			putParameter(MESSAGE,"");
		}
		String	aplec_key	= getParameter("_aplec_key");
		KamokuApRecord kap	= new KamokuApRecord(teUid, aplec_key, db);
		//
		// 実施年度を得る
		// なければ現在日時より作成する
		String yyyy = kap.yyyy();
		if(isEmpty(yyyy)){
			Calendar cal = new GregorianCalendar();
   			int	year 	 = cal.get(Calendar.YEAR);
			int month 	 = cal.get(Calendar.MONTH);  // 0=Jan, 1=Feb, ...
			if(month <= 1) year--;		// 2月までを同じ年度とする
			//
			yyyy = String.valueOf(year);
		}
		putParameter("_yyyy",yyyy);
		putParameter("_term",kap.term());
		putParameter("_unit",kap.unit());
		//
		// 日付発生のチェックボックス．チェックされていると日付の自動生成を行わない．
		// チェックされている時のvalue は "checked" だが、この変数がない場合もある．
		// ない場合は "" を設定する．初期値は "" である．
		String	autodate_off	=	getParameter("_autodate_off");
		if(isEmpty(autodate_off,"_autodate_off")){
			putParameter("_autodate_off","");
		}
		//
		// 学生画面表示用アカウントとパスワード
		//setStudentLogin();
		//
		// セクションの有無を調べる．なければ表示しない
		String te_lec_key		= KeyGen.get_te_lec_key(htb);
		KamokuSectionDEF ksd	= new KamokuSectionDEF(te_lec_key,db);
		if(ksd.size()==0){
			putParameter(MESSAGE,"※ 実施計画が作成されていないので講義画面を表示できません．<br>&nbsp;　実施計画を作成してください．");
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;	// 復帰			

		}else{
			String total = String.valueOf( ksd.size() );
			String seq   = getParameter("_sect_seq");	// 表示するレコードのシーケンス番号
			//System.out.println("■ seqnumber = " + getDisplaySeqnumber());
			// 自動設定を要求する "-1" であれば、表示すべきレコードのシーケンス番号を検索してhtbにセットする
			if(isEmpty(seq)||seq.equals("-1")){
				String	sqStr	=	getDisplaySeqnumber();
				if(sqStr.equals("-1")){
					putParameter("_sect_seq","1");
				}else{
					putParameter("_sect_seq",sqStr);
				}
			}
			putParameter("_totalSections",total);			// 総セクション数
			/** ********************************************
			 * 最上段に表示するセクションをDBから取り出して
			 * ハッシュにセットする　（486行目） 
			 * *********************************************/
			set_sectionEditArea(getParameter( "_sect_seq"));
		}
		
		/* strHash(htb,DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);	
	}
	// ハッシュテーブル(htb)を使って部分的に切り取ったソースデータ（exHtml）
	// の内容を置き換えて出力処理する．表などの反復出力に利用するが個々の処理
	// 内容は、key で特定される．
	//
	@Override
	public void	write(String key,Vector exHtml){
		if(LOG.fa) LOG.println("■ Kougi #write(String key,Vector exHtml)");
		
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
	void	edit_ref_list(Vector exHtml){
		if(LOG.fa) LOG.println("■ Kougi #edit_ref_list()");
		disp_refDefList(exHtml,"_x","_x_refIcon","_x_ref","_x_ref_url");
		
	}
	void	edit_kadai_list(Vector exHtml){
		if(LOG.fa) LOG.println("■ Kougi #edit_kadai_list()");
		disp_kadaiDefList(exHtml,"_y","_y_kadaiIcon","_y_kadai");

	}
	void	sect_list(Vector exHtml){
		if(LOG.fa) LOG.println("■ Kougi #sect_list()");
		
		String yyyy				=   getParameter("_yyyy");		// 空白でも構わない
		String te_lec_key		= 	KeyGen.get_te_lec_key(htb);   	// 科目キー
		if(!KeyGen.is_te_lec_key(te_lec_key)){
			LOG.errStop( out,"科目キーがない.または異常(001) :" + te_lec_key + ":" );
			return;
		}
		//
		KamokuSectionDEF ksd	= new KamokuSectionDEF(te_lec_key, db);
		int	n	= ksd.size();
		for(int i=0; i<n; i++){
			// セクション定義レコード
			KamokuSecDefRecord	 	ksdrec		= 	ksd.get(i);
			String 					sect_key	= 	ksdrec.sect_key();
			putParameter("_seq",String.valueOf(i+1));	// 編集領域に転記するときに要るのでシーケンス番号をhtmlに埋め込んでおく
			putParameter("_sect_key",sect_key);			// 後の資料や課題表示で必要になるのでここで入れておく（再帰処理で値は残る）
			//
			// セクション実施レコード
			KamokuSecApRecord		ksap_rec	= new KamokuSecApRecord(KeyGen.get_te_aplec_key(htb) ,sect_key,db);
			//
			putParameter("_k"				,String.valueOf(i+1) );					// 編集用アイコンの番号、１オリジン
			putParameter("_e"				,StringGear.get00type(i+1));			// 第○回の表示用
			putParameter("_sect_subject"	,(ksdrec.title()).trim() );				// セクションタイトル
			putParameter("_sect_content"	,(ksdrec.content()).trim() );			// セクション内容
			putParameter("_sect_todo"		,(ksdrec.note()).trim() );				// 備考・ToDo（学生への注意・指示など）
			//
			//DBG.println("===> yyyy = " + yyyy);
			putParameter("_dateString"	,ksap_rec.dateString(yyyy));			// 実施日。○月○日（□）～　 ○月○日（□）　の形
			putParameter("_sect_memo"	,(ksap_rec.memo()).trim() );			// 実施の備忘
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
	//
	// 資料リストを表示する
	//
	//  htb に teUid  と lec_key は必須
	//
	void disp_refDefList(Vector exHtml,String s1,String s2,String s3,String s4){
		if(LOG.fa) LOG.println("■ Kougi #disp_refDefList()");
		
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
		int 	n 		= 	refkeys.size();
		if(n==0){
			putParameter(s1 , "-");			// 項目番号表示用
			putParameter(s2 , "spacer.gif");
			putParameter(s3 , "&nbsp;");
			putParameter(s4 , "&nbsp;");
			//
			printVector(exHtml);
			//
		}else{
			//
			Vector aliveRefkeys = new Vector(10,10);
			//
			int		itemNumber	=	1;
			for(int i=0; i<n; i++){ // 全てのキーから
				/*
				 * 資料キーでハッシュを検索するが、古いレコードでは削除されたキーもふくまれているので
				 * null が返されることがある。
				 */
				ReferenceDefRecord	 rdrec	= refDef.fromKeyHash( te_lec_key, refkeys.get(i) ); // キーでハッシュを検索する
				if(rdrec!=null){
					/*
					 * 削除済みデータが残っていた場合、i を使うと項目番号が飛んでしまうので 
					 * itemNumber を使う 	
					 */
					putParameter(s1, String.valueOf(itemNumber) + ")" );	// 項目番号表示用
					itemNumber++;
					/*
					 * その他の項目
					 */
					putParameter(s2, rdrec.refIcon());				// アイコンデータ名
					putParameter(s3, rdrec.title());  				// 資料名
					if( !isEmpty(rdrec.url())){
						putParameter(s4, rdrec.url());  			// URLがあればそれだけでリンクを張る事に変更 2003.9.4
						putParameter("refFlag","");					// URL なしと置き換えをしない
					}else{
						putParameter("refFlag","replace");			// URL なしと置き換えをする
						putParameter(s4, "#");
					}
					//
					aliveRefkeys.add( rdrec.ref_key() );        // 有効なキーは記憶しておく
					if(LOG.fa) LOG.println("□□ alive に登録した refkeys.get() =" + rdrec.ref_key() +  " = " +  rdrec.title() );
					//
					printVector(exHtml);
				}
			}
		}
	}
	//
    //
	// 課題リストを表示する
	//
	//  htb に teUid  と lec_key は必須
	//
	void disp_kadaiDefList(Vector exHtml,String s1,String s2,String s3){
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
		Csv kadaikeys = ksdRec.kadaiKeys();
		int 	n 		= 	kadaikeys.size();
		//
		putParameter("kadaiEmptyFlag" ,"");
		if(n==0){
			putParameter(s1 , "-");			// 項目番号表示用
			putParameter(s2 , "spacer.gif");
			putParameter(s3 , "&nbsp;");
			/*
			 * 課題の種別アイコンにリンクが付いているので、データがない場合に不都合．
			 * リンクの無い行データに差し替えるためのタグ
			 */
			putParameter("kadaiEmptyFlag" ,"★");
			printVector(exHtml);
		}else{
			int	itemNumber	=	1;
			Vector aliveKadaikeys = new Vector(10,10);
			for(int i=0; i<n; i++){ // 全てのキーから
				String			kadaiKey	=	kadaikeys.get(i);
				KadaiDefRecord	 kdrec	= kadaiDef.fromKeyHash( te_lec_key, kadaikeys.get(i) ); // キーでハッシュを検索する
				if(kdrec!=null){
					/*
					 * 削除済みデータが残っていた場合、i を使うと項目番号が飛んでしまうので 
					 * itemNumber を使う 	
					 */
					putParameter(s1, String.valueOf(itemNumber) + ")" );	// 項目番号表示用
					itemNumber++;
					/*
					 * その他の項目
					 */
					putParameter(s2, kdrec.kadaiIcon());			// アイコンデータ名
					putParameter(s3, kdrec.title());  				// 課題名
					putParameter("_kadai_key", kadaiKey);			// 課題キー

					aliveKadaikeys.add( kdrec.kadai_key() );    	// 有効なキーは記憶しておく
					printVector(exHtml);
				}
			}
		}
	}

}
