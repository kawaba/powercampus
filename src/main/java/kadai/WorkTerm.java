
package kadai;
import java.io.*;
import java.util.*;

import database.*;
import framework.*;

/**
 *  課題の提出期限と受験パスワードを設定する
 *
 	#
	# ##################
	#   WorkTerm
	# ##################
	#
	<program $kadai.WorkTerm>
		<dispatch  html=workTerm.html  number=330  class=kadai.WorkTerm />
		<variable>
		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA lec_key aplec_key kadai_key />
		  <accept    CMD    UPLODE  />
		  <keep      offset kadai_shubetsu kadai_title/>
		  
		  <form      ex_passwd s_yyyy s_month s_day s_hour s_minute e_yyyy e_month e_day e_hour e_minute />
		</variable>
	</program> 
 *
 * 変数の説明
 *
 * 1. receive 
 * 2. accept
 * 3. keep
 *      offset				カレンダー表示開始月を決めるオフセット
 * 		kadai_shubetsu		課題種別．表示のために新規表示の時にDBから求めて記憶しておく．
 * 		kadai_title			課題名．表示のために新規表示の時にDBから求めて記憶しておく．
 * 
 * 4. form
 *      ex_passwd     	受験パスワード
 * 		s_yyyy s_month s_day s_hour s_minute　受付開始年月日時分
 *      e_yyyy e_month e_day e_hour e_minute  受付終了年月日時分
 */

/**
 * 課題実施レコードを生成する
 * 課題実施期間と試験の実行パスワードを設定する
 */
public class WorkTerm extends SuperPlayer {
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
	String			aplec_key;

	String			te_lec_key;
	String			te_aplec_key;
	String			kadaikey;
	
	String			shubetsu;
	String			kadai_title;
		
	// 休日
	boolean			furikaeFlag;
	//
	// 背景色コード
	String			sundayColor;	// 日曜の背景色
	String			weekdayColor;	// ウィークデイの背景色
	String			todayColor;		// 今日の背景色
	//
	// 今日
	int			year;	// 当年
	int			month;	// 当月
	int			today;	// 当日
	//
	// カレンダーデータ
	int					offset;		// オフセット
	GregorianCalendar	dispCal;	// 表示用カレンダー
	//
	// 2000 - 2033 までの春分の日の日付
	static	final int	[] haru_bun	= {20, 20, 21, 21, 20, 20, 21, 21, 20, 20, 
	                           		   21, 21, 20, 20, 21, 21, 20, 20, 21, 21, 
							  		   20, 20, 21, 21, 20, 20, 20, 21, 20, 20, 
							   		   20, 21, 20, 20 };
	//
	// 2000 - 2033 まで秋分の日の日付
	static	final int	[] aki_bun	= {23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
	                           	 	   23, 23, 22, 23, 23, 23, 22, 23, 23, 23,
	                           		   22, 23, 23, 23, 22, 23, 23, 23, 22, 23,
	                           		   23, 23, 22, 23 };

	public	WorkTerm(){
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
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		broker	=	getDbConnection();
		db		=	new Database(broker);			

		furikaeFlag		=	false;
		//
		sundayColor		=	"#FFB6C1";
		weekdayColor	=	"#FFFFFF";
		todayColor		=	"#00FFFF";
		//
		GregorianCalendar wk	=	new GregorianCalendar();
		year	=	wk.get(Calendar.YEAR);
		month	= 	wk.get(Calendar.MONTH);
		today	=	wk.get(Calendar.DAY_OF_MONTH);
		//
		String temp	=	getParameter("_offset");
		if(isEmpty(temp,"_offset")){
			offset	= 0;
		}else{
			offset	= Integer.parseInt(temp);
		}
		dispCal	=	null;
		//
		teUid			=	getParameter( "_teUid");
		lec_key			= 	getParameter( "_lec_key");
		aplec_key		=	getParameter( "_aplec_key");
		kadaikey		=	getParameter( "_kadai_key");
		
		te_lec_key		=	KeyGen.get_te_lec_key2(teUid, lec_key);
		te_aplec_key	= 	KeyGen.get_te_aplec_key2(teUid, aplec_key);
		if(LOG.fa) LOG.println("■WorkTerm　コンストラクタ: te_aplec_key =" + te_aplec_key);
		
		shubetsu		="";
		kadai_title		="";
		
	}	

	public	String	dispatch(){
		if(LOG.fa) LOG.outHash(htb,"■WorkTerm #dispatch()");
		if(LOG.fa) LOG.println("■WorkTerm #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		if(cmd.equals("WRITE")){
			// 書き込み
			doUpdateKadaiAP();
			putParameter(MESSAGE,"★ 課題提出期間を書き込みました");
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示
			
		}else if( cmd.equals("NEXT")){
			// 翌月から表示
			if(LOG.fa) LOG.println("    □ cmd = NEXT");
			//
			offset++;
			putParameter("_offset",String.valueOf(offset));
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示
		
    	}else if(cmd.equals("BACK")){
			// 前月から表示
			if(LOG.fa) LOG.println("    □ cmd = BACK");
			//
			offset--;
			putParameter("_offset",String.valueOf(offset));
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示
			
		}else if( cmd.equals("RETURN")){
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;
			
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
	// 課題実施レコードを書き込む
	void doUpdateKadaiAP(){
		if(LOG.fa) LOG.println("■WorkTerm #doUpdateKadaiAP(): te_aplec_key " + te_aplec_key);
		// 実施レコード
		if(isNewKadaiAp()){		// 新規モードなら課題実施キーをセットする
			if(LOG.fa) LOG.println("□新規課題実施レコード");
			write_insert_Ap();	// 実施データ
		}else{
			if(LOG.fa) LOG.println("□更新課題実施レコード");
			write_update_Ap();
		}
	}
	//
	// 新規モードか
	boolean	isNewKadaiAp(){
		if(LOG.fa) LOG.println("■WorkTerm #isNewKadaiAp(): te_aplec_key " + te_aplec_key);

		//
		// 実際に課題実施レコードを引いてみる
		if(KadaiApRecord.isExist(te_aplec_key,kadaikey,db)){
			return false;
		}
		return true;
	}
	//  課題実施レコードを挿入モードで書き込む
	void write_insert_Ap(){
		if(LOG.fa) LOG.println("■WorkTerm #write_insert_Ap(): te_aplec_key " + te_aplec_key);
		//
		// レコードを生成して書き込み
		KadaiApRecord kar	= new KadaiApRecord(te_aplec_key,kadaikey);
		kar.setFromHash(getFromHash());
		kar.insert(db);		// データを書き込む
	}
	// 課題実施レコードを更新モードで書き込む
	void write_update_Ap(){
		if(LOG.fa) LOG.println("■WorkTerm #write_update_Ap(): te_aplec_key " + te_aplec_key);
		//
		// レコードを生成して書き込み
		KadaiApRecord kar		=	new KadaiApRecord(te_aplec_key,kadaikey);
		kar.setFromHash(getFromHash());
		kar.update(db);			// データを書き込む
	}
	
	// ハッシュからデータをセットする
	public Hashtable getFromHash(){
		
		Hashtable	work	=	new Hashtable(30);
		putParameter(work,"_saiten_flag",getParameter("_saiten_flag"));
		//
		putParameter(work, "_s_yyyy"	,getParameter("_s_yyyy"	));
		putParameter(work, "_s_month"	,getParameter("_s_month"));
		putParameter(work, "_s_day"		,getParameter("_s_day"	));
		putParameter(work, "_s_hour"	,getParameter("_s_hour"	));
		putParameter(work, "_s_minute" 	,getParameter("_s_minute"));
		//
		putParameter(work, "_e_yyyy"	,getParameter("_e_yyyy"	));
		putParameter(work, "_e_month"	,getParameter("_e_month"));
		putParameter(work, "_e_day"		,getParameter("_e_day"	));
		putParameter(work, "_e_hour"	,getParameter("_e_hour"	));
		putParameter(work, "_e_minute" 	,getParameter("_e_minute"));
		//
		putParameter(work, "_passwd" ,getParameter("_ex_passwd"));
		
		return	work;
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
		}
		/*
		 * 課題の種別と課題名
		 */
		KadaiDefRecord	kdr	=	new KadaiDefRecord(te_lec_key, kadaikey, db);
		shubetsu			=	kdr.shubetsu();
		kadai_title			=	kdr.title();
		putParameter("_kadai_shubetsu", shubetsu);
		putParameter("_kadai_title"	 , kadai_title);
									
		/*
		 * new KadaiApRecord(te_aplec_key, kadaikey, db)では、必ずレコードが得られる
		 * レコードが存在しなければ初期値を埋めたレコードが作成される
		 */
		KadaiApRecord	kar		=	new KadaiApRecord(te_aplec_key, kadaikey, db);
		
		putParameter("_s_yyyy"	, kar.s_yyyy());
		putParameter("_s_month"	, kar.s_month());
		putParameter("_s_day"	, kar.s_day());
		putParameter("_s_hour"	, kar.s_hour());
		putParameter("_s_minute", kar.s_minute());
		putParameter("_e_yyyy"	, kar.e_yyyy());
		putParameter("_e_month"	, kar.e_month());
		putParameter("_e_day"	, kar.e_day());
		putParameter("_e_hour"	, kar.e_hour());
		putParameter("_e_minute", kar.e_minute());
		putParameter("_ex_passwd"	, kar.passwd());
		
		/*
		 * 課題のアイコンファイル名
		 */
		String	kadaiIcon	=	KadaiDefRecord.kadaiIcon(getParameter("_kadai_shubetsu"));
		if(!isEmpty(kadaiIcon)){
			putParameter("_kadaiIcon",kadaiIcon);
		}else{
			putParameter("_kadaiIcon",KadaiDefRecord.kadaiIcon("0"));	// spacer.gif
		}
		/* 表示
		 * getParameter(DISPFILE)にはファイルの完全パス名が入っている 
		 */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}
	public void	write(String key){
		//
		//
        // 開始年
        if(key.equals("set_syy")){
            String x = getParameter("_s_yyyy");
			if(LOG.fa)  {if(x==null)  LOG.debugStop(out,"_s_yyyy_ が null です:  exwork class #paramPrintOPT()"); }
			int yyN = Integer.parseInt(x); // 指定された開始年
            setOptionNumber(out,yyN,yyN,yyN+3);
            // 開始月
        }else if(key.equals("set_smm")){
            String x = getParameter("_s_month");
			if(LOG.fa)  {if(x==null)  LOG.debugStop(out,"_s_month_ が null です:  exwork class #paramPrintOPT()"); }
            int mmN = Integer.parseInt(x);// 指定された開始月
            setOptionNumber(out,mmN,1,12);
        // 開始日
        }else if(key.equals("set_sdd")){
            String x = getParameter("_s_day");
			if(LOG.fa)  {if(x==null)  LOG.debugStop(out,"_s_day_ が null です:  exwork class #paramPrintOPT()"); }
            int ddN = Integer.parseInt(x);// 指定された開始日
            setOptionNumber(out,ddN,1,31);
        // 開始時
        }else if(key.equals("set_shh")){
            String x = getParameter("_s_hour");
			if(LOG.fa)  {if(x==null)  LOG.debugStop(out,"_s_hour_ が null です:  exwork class #paramPrintOPT()"); }
            int hhN = Integer.parseInt(x);// 指定された開始時
            setOptionNumber(out,hhN,0,23);
        // 開始分
        }else if(key.equals("set_stt")){
            String x = getParameter("_s_minute");
			if(LOG.fa)  {if(x==null)  LOG.debugStop(out,"_s_minute_ が null です:  exwork class #paramPrintOPT()"); }
            int ttN = Integer.parseInt(x);// 指定された開始分
            setOptionNumber(out,ttN,0,55,5);
        //終了年
        }else if(key.equals("set_eyy")){
            String x = getParameter("_e_yyyy");
			if(LOG.fa)  {if(x==null)  LOG.debugStop(out,"_e_yyyy_ が null です:  exwork class #paramPrintOPT()"); }
            int yyN = Integer.parseInt(x);// 指定された終了年
            setOptionNumber(out,yyN,yyN-1,yyN+1);
        // 終了月
        }else if(key.equals("set_emm")){
            String x = getParameter("_e_month");
			if(LOG.fa)  {if(x==null)  LOG.debugStop(out,"_e_month_ が null です:  exwork class #paramPrintOPT()"); }
            int mmN = Integer.parseInt(x);// 指定された終了月
            setOptionNumber(out,mmN,1,12);
        // 終了日
        }else if(key.equals("set_edd")){
            String x = getParameter("_e_day");
			if(LOG.fa)  {if(x==null)  LOG.debugStop(out,"_e_day_ が null です:  exwork class #paramPrintOPT()"); }
            int ddN = Integer.parseInt(x);// 指定された終了日
            setOptionNumber(out,ddN,1,31);
        // 終了時
        }else if(key.equals("set_ehh")){
            String x = getParameter("_e_hour");
			if(LOG.fa)  {if(x==null)  LOG.debugStop(out,"_e_hour_ が null です:  exwork class #paramPrintOPT()"); }
            int hhN = Integer.parseInt(x);// 指定された開始時
            setOptionNumber(out,hhN,0,23);
        // 終了分
        }else if(key.equals("set_ett")){
            String x = getParameter("_e_minute");
			if(LOG.fa)  {if(x==null)  LOG.debugStop(out,"_e_minute_ が null です:  exwork class #paramPrintOPT()"); }
            int ttN = Integer.parseInt(x);// 指定された終了分
            setOptionNumber(out,ttN,0,55,5);
			//
		}
	}
	//
	// HTML中に整数の option 指示子を繰り返し生成する
    //
	public void setOptionNumber(PrintWriter out,int selectedNumber,int startNumber,int endNumber){
        setOptionNumber(out,selectedNumber,startNumber,endNumber,1);
    }
    public void setOptionNumber(PrintWriter out,int selectedNumber,int startNumber,int endNumber,int step){
        for(int k=startNumber; k<=endNumber; k+=step){
            String value = null;
            if(k >= 10) {
                value = String.valueOf(k);
            }else{
                value = "0" + String.valueOf(k);
            }
            String sel="\">";
            if(k==selectedNumber){sel = "\"  selected>";}
            String opt = "   <option value=\"" + value + sel + String.valueOf(k) + "</option>";
            out.println(opt);
        }
    }
	//
	//
	// カレンダー出力
    public void write(String key,Vector exHtml){
		//
		// ひと月分のカレンダーを表示する
		if(key.equals("calTable1")){
			if(LOG.fa) LOG.println("class PcCalendar #write() : ■calTable /  ひと月分のカレンダーを表示する の先頭です");
			//
			dispCal	=	setThisMonth(-1);			// 先月
			printVector(exHtml);
			//
		}else if(key.equals("calTable2")){
			dispCal	=	setThisMonth(0);			// 当月
			printVector(exHtml);
			//
		}else if(key.equals("calTable3")){
			dispCal	=	setThisMonth(1);			// 翌月
			printVector(exHtml);
			
        // １週間（１行）分を複数回出力する
		}else if(key.equals("calLine")){
            if(LOG.fa) LOG.println("class PcCalendar #write() : ■calLine / １週間（１行）分を複数回出力する の先頭です");
        	//
			dispCal.set(Calendar.DAY_OF_MONTH,1);
			
			int	wmonth	= dispCal.get(Calendar.MONTH);	// 当月（チェック用）
			int	wyear	= dispCal.get(Calendar.YEAR);	// 当年（チェック用）
			boolean	ret	= true;
			while(ret){
				ret	=	write7days(exHtml,wyear,wmonth);
			}
		}
    }
	//
	// 先月・当月・翌月のカレンダーに調整して、タイトル文字列をシステムハッシュに入れる
	public GregorianCalendar setThisMonth(int dif){
		if(LOG.fa) LOG.println("           offset = " + offset);
		//
		GregorianCalendar	disp	=	new GregorianCalendar();
		int					step	=	dif + offset;
		if(step!=0)	{ disp.add(Calendar.MONTH, step); }
		//
		int		yy	=	disp.get(Calendar.YEAR);
		int		mm	= 	disp.get(Calendar.MONTH);
		putParameter("_yymm",String.valueOf(yy) + "年" + String.valueOf(mm+1) + "月");
		//
		return	disp;
	}
	//
	// １週間分（１行）を表示設定する
	boolean	write7days(Vector exHtml,int wy,int wm){
		if(LOG.fa) LOG.println("class PcCalendar #write7days() : ■７日分を出力する の先頭です");
		//
		int	wdayNum	= dispCal.get(Calendar.DAY_OF_WEEK) -1 ;	// 1(sun) ～ 7(sat) を　0 ～ 6 に
		int k,i,j;
		// 最初の週の１日より前の空白部分の表示設定
		for(k=0; k<wdayNum; k++){
			setBlank(k);
		}
		// １週分の表示設定
		// ７回未満で当月の間繰り返す
		for(j=k; (j<7)&&(dispCal.get(Calendar.MONTH)==wm); j++){
			//
			// j 日の表示設定を行い、１日進める
			setCal(j, dispCal.get(Calendar.DAY_OF_MONTH), wm, wy);
			dispCal.add(Calendar.DAY_OF_MONTH,1);			// １日進める
		}
		// 最後の週の空白部分の表示設定
		for(i=j; i<7; i++){
			setBlank(i);
		}
		printVector(exHtml);
		if(dispCal.get(Calendar.MONTH)==wm)	return	true;
		return	false;
	}
	//
	// １日分の表示設定
	// pos は 0 ～ 6 で日曜から土曜に対応する
	// wm（当月）、wy（当年)
	// dd は何日かを表す数
	void	setCal(int pos,int dd,int wm,int wy){
		//
		// 曜日ごとに違う色の設定
		String	colorKey	= "_bgcolor" + String.valueOf(pos);
		// 日曜なら
		if(isSunday(pos)){
			putParameter(colorKey,sundayColor);
		// その他なら
		}else{
			putParameter(colorKey,weekdayColor);
		}
		//
		// その他の休日等について（日曜が休日のこともあるので））
		// 休日なら
		if(isHoliday(dd,wm,wy,pos)){
			putParameter(colorKey,sundayColor);
		//
		// 休日でなくても振替休日なら
		}else if(furikaeFlag){
			putParameter(colorKey,sundayColor);
			furikaeFlag	=	false;
		}
		// いづれにせよ当日なら
		if( (dd==today)&&( wm==month)&&(wy==year) ){
			putParameter(colorKey,todayColor);
		}
		//
		// 日付の文字を設定
		// getS2type() は編集メソッド．文字を二桁で右詰めにする
		String	dateKey		= "_d" + String.valueOf(pos);
		putParameter(dateKey, getS2type(dd));						// １桁の数字なら先頭に空白を付加
	}
	// １日分の空白設定
	void	setBlank(int pos){
		//
		// 曜日ごとに違う色の設定（日にちは入らなくても背景色は設定することが必要）
		String	colorKey	= "_bgcolor" + String.valueOf(pos);
		// 日曜なら
		if( (pos+1)	== Calendar.SUNDAY){
			putParameter(colorKey,sundayColor);
		// その他なら
		}else{
			putParameter(colorKey,weekdayColor);
		}		
		// 数字は表示しないので日本語空白文字を表示する
		String	dateKey		= "_d" + String.valueOf(pos);
		putParameter(dateKey, "　");
	}
	//
	//  日曜日かどうか( pos は 0～6 なのでカレンダーでの値にあわせるには＋１する）
	public boolean isSunday(int	pos){
		int	chk	= pos + 1;
		if( chk	== Calendar.SUNDAY)	{ return true; }
		return	false;
	}
	//
	//  第二月曜日かどうか( pos は 0～6 なのでカレンダーでの値にあわせるには＋１する）
	public boolean isSecondMonday(int dd, int	pos){
		int	chk	= pos + 1;
		if( chk	== Calendar.MONDAY)	{
			if((dd >7)&&(dd<15)){
				return	true;
			}
		}
		return	false;
	}
	//
	//  第三月曜日かどうか( pos は 0～6 なのでカレンダーでの値にあわせるには＋１する）
	public boolean isThirdMonday(int dd, int	pos){
		int	chk	= pos + 1;
		if( chk	== Calendar.MONDAY)	{
			if((dd >14)&&(dd<22)){
				return	true;
			}
		}
		return	false;
	}
	//
	public boolean	isHoliday(int dd, int wm, int wy, int pos){
		//
		wm = wm + 1;
		//
		if(isGantan(dd, wm, wy ,pos)) 				return	true;
		if(isSeijin_no_hi(dd, wm, wy ,pos)) 		return	true;
		if(isKenkokuKinen_bi(dd, wm, wy ,pos)) 		return	true;
		if(isShunBun_no_hi(dd, wm, wy ,pos)) 		return	true;
		if(isMidoei_no_hi(dd, wm, wy ,pos)) 		return	true;
		if(isKenpouKinen_bi(dd, wm, wy ,pos)) 		return	true;
		if(isKokiminn_no_Kyujitsu(dd, wm, wy ,pos)) return	true;
		if(isKodomo_no_hi(dd, wm, wy ,pos)) 		return	true;
		if(isUmi_no_hi(dd, wm, wy ,pos)) 			return	true;
		if(isKeirou_no_hi(dd, wm, wy ,pos)) 		return	true;
		if(isShuuBun_no_hi(dd, wm, wy ,pos)) 		return	true;
		if(isTaiku_no_hi(dd, wm, wy ,pos)) 			return	true;
		if(isBunka_no_hi(dd, wm, wy ,pos)) 			return	true;
		if(isKinrouKansha_no_hi(dd, wm, wy ,pos)) 	return	true;
		if(isTennouTanjo_bi(dd, wm, wy ,pos)) 		return	true;
		//
		return false;
	}
	// 元旦
	public	boolean	isGantan(int dd, int wm, int wy , int pos){
		if((dd==1) && (wm==1)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	// 成人の日
	public	boolean	isSeijin_no_hi(int dd, int wm, int wy , int pos){
		if((wm==1)&&(isSecondMonday(dd,pos)))	return	true;
		return 	false;
	}
	// 建国記念日
	public	boolean	isKenkokuKinen_bi(int dd, int wm, int wy , int pos){
		if((dd==11) && (wm==2)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	// 春分の日
	public	boolean	isShunBun_no_hi(int dd, int wm, int wy , int pos){
		//
		if((wm==3)&&(dd==haru_bun[wy - 2000])){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	// みどりの日
	public	boolean	isMidoei_no_hi(int dd, int wm, int wy , int pos){
		if((dd==29) && (wm==4)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	// 憲法記念日
	public	boolean	isKenpouKinen_bi(int dd, int wm, int wy , int pos){
		if((dd==3) && (wm==5)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	// 国民の休日
	public	boolean	isKokiminn_no_Kyujitsu(int dd, int wm, int wy , int pos){
		if((dd==4) && (wm==5)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	// こどもの日
	public	boolean	isKodomo_no_hi(int dd, int wm, int wy , int pos){
		if((dd==5) && (wm==5)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	// 海の日
	public	boolean	isUmi_no_hi(int dd, int wm, int wy , int pos){
		if((wm==7) && isThirdMonday(dd,pos)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	// 敬老の日
	public	boolean	isKeirou_no_hi(int dd, int wm, int wy , int pos){
		if((dd==15) && (wm==9)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	// 秋分の日
	public	boolean	isShuuBun_no_hi(int dd, int wm, int wy , int pos){
		//
		if((wm==9)&&(dd==aki_bun[wy - 2000])){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	// 体育の日
	public	boolean	isTaiku_no_hi(int dd, int wm, int wy , int pos){
		if((wm==10) && isSecondMonday(dd,pos)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	// 文化の日
	public	boolean	isBunka_no_hi(int dd, int wm, int wy , int pos){
		if((dd==3) && (wm==11)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	// 勤労感謝の日
	public	boolean	isKinrouKansha_no_hi(int dd, int wm, int wy , int pos){
		if((dd==23) && (wm==11)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	// 天皇誕生日
	public	boolean	isTennouTanjo_bi(int dd, int wm, int wy , int pos){
		if((dd==23) && (wm==12)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}

}

