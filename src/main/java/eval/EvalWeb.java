/*
課題をWEBモードで見て評価する

*/
package eval;

import java.io.*;
import java.util.*;

import jbbs.BbsInfoDB;
import database.*;
import framework.*;
import tktools.*;

import	epml.*;
import kadai.*;
/**
 * 
 *
 *
 	#
	# ##################
	#     EvalWeb
	# ##################
	#
	<program $eval.EvalWeb>
		<dispatch  html=EvalWeb.html  number=1520  class=eval.EvalWeb />
		<variable>
		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA HOMEURL DIVISION
		             title lec_key aplec_key kadai_key initRecNo />
		  <accept    CMD    />
		  <keep      recNo stNumber />
		  
		  <form      sortMode search/>
		</variable>
	</program> 
 *
 *
 * 変数の説明
 *
 * 1. receive 
 * 		initRecNo	--　最初に表示するレコードの番号
 * 						TEXT モードでの表示から復帰したときのみ値がある
 * 2. accept
 * 3. keep
 * 		recNo		-- 現在対象にしている提出ファイルのファイル番号
 * 
 * 4. form
 *		sortMode	-- ソート状態（"ON"なら提出時間順）デフォルトは番号順
 * 
 */
public class EvalWeb extends SuperPlayer implements KadaiVar{
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
	String		teUid;
	String		lec_key;
	String		aplec_key;
	String		te_lec_key;
	String		kadai_key;
	
	Answer		ans;
	/**
	 * 
	 *
	 */
	public	EvalWeb(){
		super();
		if(LOG.fa) LOG.println("■ Sample #コンストラクタ");
	}
	/**
	 * 
	 * @param out
	 * @param htb
	 * @param para
	 */
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		broker		=	getDbConnection();
		db			=	new Database(broker);	
		szDB		= 	getParameter("szDB");
		teUid		=	getParameter("teUid");
		lec_key		= 	getParameter("lec_key");
		aplec_key	=	getParameter("aplec_key");
		te_lec_key	=	KeyGen.get_te_lec_key2( teUid, lec_key);
		kadai_key	= 	getParameter("kadai_key");
		/*
		 * Web変数 "sortMode" を見て、解答ファイルのソートモードを設定する
		 * 設定がなければ番号順がデフォルトである
		 */
		ans			=	new	Answer(htb, para, db);
		sortAnsewers();

	}
	/**
	 * 
	 *
	 */
	public	String	dispatch(){
		if(LOG.fa) LOG.outHash(htb,"■Sample #dispatch()");
		if(LOG.fa) LOG.println("■Sample #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		//
	
		if(cmd.equals("RETURN")){
			/*
			 * 一覧表画面へ戻る
			 */
			disp_mode		=	DISP_EDIT;
			ret				=	DISPATCH_RETURN;
		
		}else if(cmd.equals("SEARCH")){
			/*
			 * 学籍番号で検索
			 */
			sub_search();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
			
		}else if(cmd.equals("SORT")){
			/*
			 * answer のソートは initialize() ですんでいるのでここでは何もしない。
			 */
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;			
			
		}else if(cmd.equals("WRITE")){
			/*
			 * 採点を書き込む
			 */
			sub_wrt();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;				
			
		}else if(cmd.equals("FIRST")){
			/*
			 * 先頭のレコードを表示
			 */
			sub_top();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;				
			
		}else if(cmd.equals("LAST")){
			/*
			 * 末尾ののレコードを表示
			 */
			sub_btm();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;				
			
		}else if(cmd.equals("NEXT")){
			/*
			 * 次のレコードを表示
			 */
			sub_fwd();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;				
			
		}else if(cmd.equals("BACK")){
			/*
			 * 前のレコードを表示
			 */
			sub_bwd();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;				
		
		}else if(cmd.equals("TEXT_MODE")){
			/*
			 * text 表示モードへ
			 */
			putParameter("gotoTextMode","YES");
			putParameter("initRecNo", getParameter("recNo"));
			disp_mode	=	DISP_NEW;
			ret			=	"$eval.EvalText";

		}else if(cmd.equals("EXECUTE")){
			/*
			 *　プログラムの実行
			 */
			disp_mode	=	DISP_NEW;
			ret			=	getParameter(PROGRAM);

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
	 * データを検索して現在のレコード番号にセットする
	 *
	 */
	public void sub_search(){
		String	num	=	getParameter("search").trim();
		int		pos	=	ans.search(num);
		setRecordNum(pos);
		
	}
	/**
	 * 現在のソートモードを answer に設定する
	 * Webでの指定を反映する
	 * 最初は設定値がないがそのときはgetParameter("sortMode")で "" が返るのでOK
	 */
	public	void	sortAnsewers(){
		
		String	mode	=	getParameter("sortMode");

		if(mode.equals("ON")){
			ans.changeSortmode(DATE_MODE);

		}else{
			ans.changeSortmode(NUMBER_MODE);
		}
		
	}
	/**
	 * データを書き込む
	 * 
	 * レコード番号はweb変数 recNo にある。
	 *
	 */
	public void sub_wrt(){
		int pos 	 = getRecordNum(); 			// 現在のレコード番号
		String msg   = getParameter("_message");// 2005.3 削除したが形だけ残す
		String score = getParameter("_score");
		String eval  = getParameter("_eval");	// 採点結果を通知したかどうか
		String a     = getParameter("_answer");

		AnswerRecord ar = ans.writeWebAnsRecord(pos,msg,score,eval); 	// （Webなので解答は書き換えない）
		update_KadaiInfo(ar);											// KadaiInfo を更新
		//
	    sub_fwd(); // 次のレコード
	}
	/**
	 * KadaiInfo の得点を更新する
	 * @param ans
	 */
	public	void update_KadaiInfo(AnswerRecord ansr){

		String 		te_aplec_key	= 	KeyGen.get_te_aplec_key2(ansr.getTeUid(),ansr.getClassKey());
		String 		score			= 	ansr.getScore();
		KadaiInfo 	kdi				=	new KadaiInfo(szDB,db);
		kdi.updateScore( getParameter("stNumber"), te_aplec_key, kadai_key, score);
	}
	/**
	 * 現在のレコード番号を得る
	 * ０ オリジン
	 * @return
	 */
	int	getRecordNum(){
		int		pos	=	Integer.parseInt(getParameter("recNo")) -1;
		return	pos;
	}
	/**
	 * レコード番号をシステムハッシュにセットする
	 * １オリジン
	 * @return
	 */
	String	setRecordNum(int pos){
		String	posStr	=	String.valueOf(pos + 1);
		putParameter("recNo", posStr);
		return	posStr;
	}
	/**
	 * 次のレコード番号を得る
	 * 
	 */
	int	incNum(){
		int	pos	=	getRecordNum();
		if(isOK(pos+1)){
			return	pos + 1;
		}else{
			return	pos;
		}
	}
	/**
	 * 一つ前のレコード番号を得る
	 * @return
	 */
	int	decNum(){
		int	pos	=	getRecordNum();
		if(isOK(pos-1)){
			return	pos - 1;
		}else{
			return	pos;
		}		
	}
	/**
	 * レコード番号として正しいか
	 * @param num
	 * @return
	 */
	boolean	isOK(int num){
		int	n	=	ans.getTotal();
		if((num>=0)&&(num<n)){
			return	true;
		}else{
			return	false;
		}
	}
	/**
	 * 先頭のレコードを指すようポインタを
	 * システムハッシュにセットする
	 *
	 */
	public void sub_top(){
		setRecordNum(0);
	}
	/**
	 * 最後のレコード 
	 *
	 */
	public void sub_btm(){
		setRecordNum(ans.getTotal()-1);
		
	}
	/**
	 * 次のレコード
	 *
	 */
	public void sub_fwd(){
		setRecordNum(incNum());
	}
	/**
	 * 前のレコード
	 */
	public void sub_bwd(){
		setRecordNum(decNum());
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
	public  void	display(boolean editmode){
		if(LOG.fa)	LOG.println("■ Sample #display(boolean editmode)");
		
		if(!editmode){
			putParameter(MESSAGE,"");
			/*
			 * 最初はform 変数 "sortMode" の値がないのでここで提出順に設定しておく
			 */
			putParameter("_sortMode","OFF"); 
			/*
			 * 最初に表示するレコード番号を設定する
			 */			
			setRecordNum(0);
		}
		/*
		 * テキストモードからの復帰の時はセッション共通変数から表示するレコード番号を得る
		 * 復帰かどうかは "gotoTextMode" に残してある
		 */
		getReturnedRecNumber();
		/*
		 * 検索窓の表示をクリア
		 */
		putParameter("_search"," ");
		/*
		 * 表示に必要な情報をシステムハッシュにセットする
		 */
		setSort( getParameter("_sortMode") ); 
		setRecord();
		setDefRecordInfo();
		setApRecordInfo();
		setCountInfo();
		/*
		 * 解答をHTMLに直してシステムハッシュにセットする
		 */
		putParameter("_html", setHtml());
		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}
	/*
	 * テキストモードからの復帰のとき
	 * 初期表示番号をセッション共通変数から得る
	 */
	public	void	getReturnedRecNumber(){
		if(getParameter("gotoTextMode").equals("YES")){
			
			Object	obj	=	getSessionVar("initRecNo");// nullかもしれない
			if(obj==null){
				setRecordNum(0);
				if(LOG.fa){
					LOG.println("■■■EvalWeb #display()");
					LOG.println("     recNo= null");
				}
				
			}else{
				String	recNo	=	(String)obj;
				setRecordNum(Integer.parseInt(recNo)-1); // ゼロオリジン
				if(LOG.fa){
					LOG.println("■■■EvalWeb #display()");
					LOG.println("     recNo=" + recNo);
				}
			}
			putParameter("gotoTextMode","NO");
		}

		
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
	/**
	 * レコード情報をシステムハッシュにセットする
	 */
	void	setRecord(){
		int				pos	=	getRecordNum();
		AnswerRecord	rec	=	ans.getdAnsRecordAt(pos);

		htb.put("_rec"			,String.valueOf(Gear.get000type(pos+1)));
		htb.put("_answerString"	,rec.getAnswer());
	    htb.put("_message"		,rec.getMessage());
	    htb.put("_score"		,rec.getScore());
	    htb.put("_eval"			,rec.getEval());
	    htb.put("_stUid"		,rec.getStUid());
	    htb.put("_stNumber"		,rec.getStNumber());
	    htb.put("_stName"		,rec.getStName());
	    htb.put("_updateTime"	,rec.getUpdateTime());
	    htb.put("_disposal"		,rec.getDisposal());
	    
	    setIconInfo(rec);
		setUrlInfo(rec);
	}
	/**
	 * アイコンファイル名と結果通知状況ファイル名
	 *
	 */
	void	setIconInfo(AnswerRecord ar){
		/*
		 * 提出状態
		 */
		String kubun;
		if((ar.getDisposal()).equals(CHECKED)){
			kubun = KadaiInfo.SUBMITTED;// 提出済み( = 3)
        }else{
			kubun = KadaiInfo.WORKING;	// 作成中( = 2) 
        }
		htb.put("_kubun" ,kubun);
		/*
		 * 結果の通知
		 */
		String sent;
		if((ar.getEval()).equals(MAIL_DONE)){
			sent = "1"; // 結果送信済みマーク
		}else{
			sent = "0";
		}
		htb.put("_sent"  ,sent);		
	}
	/**
	 * 提出ファイルのアイコンとそのURL
	 *
	 */
	void	setUrlInfo(AnswerRecord ar){
		/*
		 * 課題ファイルのあるディレクトリで
		 */
		String  	filePath 	= para.getKadaiPostDir2(teUid,aplec_key,kadai_key);
		Files		fl			= new Files(filePath);						// 提出ファイルを処理するクラス
		/*
		 * htmlテンプレートからファイルとそのURLを記述するためのテンプレートを得る
		 */
		TemplateBox	tbx			=	new TemplateBox(para.htmlTemplate());
		String 		lineHtml 	=	tbx.get("report");
		/*
		 * 
		 */
		String 		aRef		= 	"";
		String 		product		=	para.getViewSwitch();
		if( (product.equals("SYSTEM")) || (product.equals("SYSTEM_VIEW")) ){
			aRef	=	para.getKadaiPostURL(htb,teUid,aplec_key,kadai_key); // IPアドレスでURLを返す
		}else{
			aRef	=	para.getKadaiPostURL(teUid,aplec_key,kadai_key); // URLを返す
		}
		/*
		 * 
		 */
		Hashtable	fh 		= new Hashtable();
		String [] 	files 	= fl.list(ar.getStNumber());
		if(files != null) {
			StringBuffer bf = new StringBuffer(2000);
			boolean sp		= false;
			for(int m=0; m<files.length; m++){
				if(sp) bf.append(", "); 
				//
				fh.put("_link",aRef + files[m]);
				fh.put("_fileName",files[m]);
				String s = substitute(lineHtml,fh);
				bf.append(s);
				sp = true;
			}
			htb.put("_fileList",bf.toString());
		}else{
			htb.put("_fileList","-");
		}
		htb.put("_max","");		
	}	
	/**
	 * 課題定義レコードにかかる情報
	 */
	void	setDefRecordInfo(){
		
		KadaiDefRecord	kadaiDef	=	ans.getKadaiDefRecord();
		String 			kadai_Icon	= 	kadaiDef.kadaiIcon();// 課題アイコンファイル名
		htb.put("_kadaiIcon",kadai_Icon);
		//
		htb.put("_titleString",kadaiDef.title());
	    htb.put("_kadai_title",kadaiDef.title());// 2003.4.10 追加
	}
	/**
	 * 課題実施レコードにかかる情報
	 */
	void	setApRecordInfo(){
		KadaiApRecord	kadaiAp	=	ans.getKadaiApRecord();
		htb.put("_syy",kadaiAp.s_yyyy());
	    htb.put("_smm",kadaiAp.s_month());
	    htb.put("_sdd",kadaiAp.s_day());
	    htb.put("_shh",kadaiAp.s_hour());
	    htb.put("_stt",kadaiAp.s_minute());
	    htb.put("_eyy",kadaiAp.e_yyyy());
	    htb.put("_emm",kadaiAp.e_month());
	    htb.put("_edd",kadaiAp.e_day());
	    htb.put("_ehh",kadaiAp.e_hour());
	    htb.put("_ett",kadaiAp.e_minute());
		//
		htb.put("_start",kadaiAp.getStartDate2());// 2003.4.10 追加
		htb.put("_end",  kadaiAp.getEndDate2());  // 2003.4.10 追加
	}
	/**
	 * 提出されている解答の数についての情報
	 */
	void	setCountInfo(){
		htb.put("_n1",String.valueOf(ans.getAll()));
	    htb.put("_n2",String.valueOf(ans.getTotal()));
	    htb.put("_n3",String.valueOf(ans.getSubmitted()));
	    htb.put("_n4",String.valueOf(ans.getMaking()));
	}
	/**
	* 解答を html に直したものを返す
	*/
	public	String	setHtml(){
		
		/*
		 * 先頭にフォーマットを付加したPMLを得る
		 */
		String	answerString	= pmlStr( getParameter("_answerString") );
		/*
		 * レポート課題なのでPMLのみパースしてHTMLを生成する
		 */
		Exam	exam			= new Exam(answerString,para.getEmlConfPath());

		String	graphicUrl	=	para.kadaiAttachUrl(teUid, aplec_key, kadai_key,  getParameter("stNumber"));
		String	graphicPath	=	para.kadaiAttachDir(teUid, aplec_key, kadai_key,  getParameter("stNumber"));
		exam.setNonHtmlFlag();// 資料ではなく課題データであることを exam に伝える
		exam.setImgPath( graphicUrl );
		exam.setImgDestinationPath( graphicPath );
		
		return	exam.createHtml();
	}
	/**
	 * PMLをパースしたHTMLを返す
	 * @param content	内容を表すPML
	 * @return			同 HTML
	 */
	String	pmlStr(String pml){
		/*
		 * pmlテキストに段落エイリアスを付加する
		 * テンプレートがあるのでそれを読み込んでアペンドする
		 */
		String		text	=	getFormat() + Gear.lineSeparator() + pml;
		return		text;
	}	
	/**
	 * 学生のそれぞれのフォーマットを読み込んで返す
	 * @return
	 */
	String	getFormat(){
		
		String		format	=	"";
		Hashtable	rec		=	getInfoRecord(getParameter("stNumber"));
		if(rec!=null){
			format	=	Gear.strHash(rec,BbsInfoDB.FORMAT);
		}else{
			format	=	para.formatTemplate();
		}
		return	format;
		
	}
	/**
	 * ユーザーIDからユーザー情報をハッシュテーブルで得る
	 * 存在しない時は null を返す
	 * 
	 * @return
	 */
	Hashtable	getInfoRecord(String uid){
		if(LOG.fa) LOG.println("■ BbsPost #getInfoRecord()");

		BbsInfoDB	infodb	=	new BbsInfoDB(uid, szDB, broker);
		Hashtable	dt		=	new Hashtable();
		int		count	=	infodb.readBbsInfo(uid, dt);
		if(count>0)	return	dt;
		return			null;
	}

}
	
