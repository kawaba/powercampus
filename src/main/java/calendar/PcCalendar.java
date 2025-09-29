/*
Power Campus Calender

*/
package calendar;
import java.util.*;
import java.io.*;

import framework.*;

/**
*
*
	#
	# ##################
	#     PcCalendar
	# ##################
	#
	<program $calendar.PcCalendar>
		<dispatch  html=pcCalendar.html  number=50  class=calendar.PcCalendar />
		<variable>
		  <receive   NUMBER STAMP/>
		  <accept    CMD />
		  <keep      offset />
		  
		  <form      />
		</variable>
	</program> 
*
*
* 変数の説明
*
* 1. receive 
* 2. accept
* 3. keep
* 4. form
*
* 
*/
public class PcCalendar extends SuperPlayer {

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
	// 休日
	boolean			furikaeFlag;

	//
	//	 背景色コード
	String			sundayColor;	// 日曜の背景色
	String			weekdayColor;	// ウィークデイの背景色
	String			todayColor;		// 今日の背景色
	//
	//	 今日
	int			year;	// 当年
	int			month;	// 当月
	int			today;	// 当日
	//
	//	 カレンダーデータ
	int					offset;		// オフセット
	GregorianCalendar	dispCal;	// 表示用カレンダー
	//
	//
	//	 2000 - 2033 までの春分の日の日付
	static	final int	[] haru_bun	= {20, 20, 21, 21, 20, 20, 21, 21, 20, 20, 
	                          		   21, 21, 20, 20, 21, 21, 20, 20, 21, 21, 
							  		   20, 20, 21, 21, 20, 20, 20, 21, 20, 20, 
							   		   20, 21, 20, 20 };
	//
	//	 2000 - 2033 まで秋分の日の日付
	static	final int	[] aki_bun	= {23, 23, 23, 23, 23, 23, 23, 23, 23, 23,
	                          	 	   23, 23, 22, 23, 23, 23, 22, 23, 23, 23,
	                          		   22, 23, 23, 23, 22, 23, 23, 23, 22, 23,
	                          		   23, 23, 22, 23 };
	//

	public	PcCalendar(){
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
		String temp	=	strHash(htb,"_offset");
		if(isEmpty(temp,"_offset")){
			offset	= 0;
		}else{
			offset	= Integer.parseInt(temp);
		}
		dispCal	=	null;
		
	}	
	public	String	dispatch(){
		if(LOG.fa) LOG.outHash(htb,"■Sample #dispatch()");
		if(LOG.fa) LOG.println("■Sample #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		// 当月を表示
		if( cmd.equals("NOW")){
			if(LOG.fa) LOG.println("    □ cmd = NEXT");
			//
			offset	= 0;
			htb.put("_offset",String.valueOf(offset));
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
			
		// 翌月から表示
		}else if( cmd.equals("NEXT")){
			if(LOG.fa) LOG.println("    □ cmd = NEXT");
			//
			offset++;
			htb.put("_offset",String.valueOf(offset));
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
		
		// 前月から表示
		}else if(cmd.equals("BACK")){
			if(LOG.fa) LOG.println("    □ cmd = BACK");
			//
			offset--;
			htb.put("_offset",String.valueOf(offset));
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
			
		// エラー
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
		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}	
	public void write(String key,Vector exHtml){
		if(key.equals("calTable")){
			calTable(exHtml);
			
		}else if(key.equals("calLine")){
			calLine(exHtml);
			
		}
	}
	/**
	 * ひと月分のカレンダーを表示する
	 * 
	 * @param exHtml
	 */
	void	calTable(Vector exHtml){
		if(LOG.fa) LOG.println("class PcCalendar #write() : ■calTable /  ひと月分のカレンダーを表示する の先頭です");
		//
		dispCal	=	setThisMonth(-1);			// 先月
		printVector(exHtml,htb);
		//
		dispCal	=	setThisMonth(0);			// 当月
		printVector(exHtml,htb);
		//
		dispCal	=	setThisMonth(1);			// 翌月
		printVector(exHtml,htb);
		
		
	}
	/**
	 * １週間（１行）分を複数回出力する
	 * 
	 * @param exHtml
	 */
	void	calLine(Vector exHtml){

		dispCal.set(Calendar.DAY_OF_MONTH,1);
		
		int	wmonth	= dispCal.get(Calendar.MONTH);	// 当月（チェック用）
		int	wyear	= dispCal.get(Calendar.YEAR);	// 当年（チェック用）
		boolean	ret	= true;
		while(ret){
			ret	=	write7days(exHtml,htb,wyear,wmonth);
		}
	}
	/**
	 * 先月・当月・翌月のカレンダーに調整して、タイトル文字列を htb に入れる
	 * 
	 * @param dif
	 * @return
	 */
	public GregorianCalendar setThisMonth(int dif){
		if(LOG.fa) LOG.println("           offset = " + offset);
		//
		GregorianCalendar	disp	=	new GregorianCalendar();
		int					step	=	dif + offset;
		if(step!=0)	{ disp.add(Calendar.MONTH, step); }
		//
		int		yy	=	disp.get(Calendar.YEAR);
		int		mm	= 	disp.get(Calendar.MONTH);
		htb.put("_yymm",String.valueOf(yy) + "年" + String.valueOf(mm+1) + "月");
		//
		return	disp;
	}
	/**
	 * １週間分（１行）を表示設定する
	 * @param exHtml
	 * @param htb
	 * @param wy
	 * @param wm
	 * @return
	 */
	boolean	write7days(Vector exHtml,Hashtable htb,int wy,int wm){
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
		printVector(exHtml,htb);
		if(dispCal.get(Calendar.MONTH)==wm)	return	true;
		return	false;
	}
	/**
	 * １日分の表示設定
	 * pos は 0 ～ 6 で日曜から土曜に対応する
	 * wm（当月）、wy（当年)
	 * dd は何日かを表す数
	 * 
	 * @param pos
	 * @param dd
	 * @param wm
	 * @param wy
	 */
	void	setCal(int pos,int dd,int wm,int wy){
		//
		// 曜日ごとに違う色の設定
		String	colorKey	= "_bgcolor" + String.valueOf(pos);
		// 日曜なら
		if(isSunday(pos)){
			htb.put(colorKey,sundayColor);
		// その他なら
		}else{
			htb.put(colorKey,weekdayColor);
		}
		//
		// その他の休日等について（日曜が休日のこともあるので））
		// 休日なら
		if(isHoliday(dd,wm,wy,pos)){
			htb.put(colorKey,sundayColor);
		//
		// 休日でなくても振替休日なら
		}else if(furikaeFlag){
			htb.put(colorKey,sundayColor);
			furikaeFlag	=	false;
		}
		// いづれにせよ当日なら
		if( (dd==today)&&( wm==month)&&(wy==year) ){
			htb.put(colorKey,todayColor);
		}
		//
		// 日付の文字を設定
		// getS2type() は編集メソッド．文字を二桁で右詰めにする
		String	dateKey		= "_d" + String.valueOf(pos);
		htb.put(dateKey, getS2type(dd));						// １桁の数字なら先頭に空白を付加
	}
	/**
	 * １日分の空白設定
	 * 
	 * @param pos
	 */
	void	setBlank(int pos){
		//
		// 曜日ごとに違う色の設定（日にちは入らなくても背景色は設定することが必要）
		String	colorKey	= "_bgcolor" + String.valueOf(pos);
		// 日曜なら
		if( (pos+1)	== Calendar.SUNDAY){
			htb.put(colorKey,sundayColor);
		// その他なら
		}else{
			htb.put(colorKey,weekdayColor);
		}		
		// 数字は表示しないので日本語空白文字を表示する
		String	dateKey		= "_d" + String.valueOf(pos);
		htb.put(dateKey, "　");
	}

	/**
	 * 日曜日かどうか( pos は 0～6 なのでカレンダーでの値にあわせるには＋１する）
	 * @param pos
	 * @return
	 */
	public boolean isSunday(int	pos){
		int	chk	= pos + 1;
		if( chk	== Calendar.SUNDAY)	{ return true; }
		return	false;
	}
	/**
	 *  第二月曜日かどうか( pos は 0～6 なのでカレンダーでの値にあわせるには＋１する）
	 * @param dd
	 * @param pos
	 * @return
	 */
		//
//		 
	public boolean isSecondMonday(int dd, int	pos){
		int	chk	= pos + 1;
		if( chk	== Calendar.MONDAY)	{
			if((dd >7)&&(dd<15)){
				return	true;
			}
		}
		return	false;
	}
	/**
	 * 第三月曜日かどうか( pos は 0～6 なのでカレンダーでの値にあわせるには＋１する）
	 * @param dd
	 * @param pos
	 * @return
	 */
		//
//		  
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
	/**
	 * 元旦
	 * @param dd
	 * @param wm
	 * @param wy
	 * @param pos
	 * @return
	 */
	public	boolean	isGantan(int dd, int wm, int wy , int pos){
		if((dd==1) && (wm==1)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	/**成人の日
	 * 
	 * @param dd
	 * @param wm
	 * @param wy
	 * @param pos
	 * @return
	 */
	public	boolean	isSeijin_no_hi(int dd, int wm, int wy , int pos){
		if((wm==1)&&(isSecondMonday(dd,pos)))	return	true;
		return 	false;
	}
	/**
	 * 建国記念日
	 * 
	 * @param dd
	 * @param wm
	 * @param wy
	 * @param pos
	 * @return
	 */
	public	boolean	isKenkokuKinen_bi(int dd, int wm, int wy , int pos){
		if((dd==11) && (wm==2)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	/**
	 * 春分の日
	 * @param dd
	 * @param wm
	 * @param wy
	 * @param pos
	 * @return
	 */
	public	boolean	isShunBun_no_hi(int dd, int wm, int wy , int pos){
		if((wm==3)&&(dd==haru_bun[wy - 2000])){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	/**
	 * みどりの日
	 * @param dd
	 * @param wm
	 * @param wy
	 * @param pos
	 * @return
	 */
	public	boolean	isMidoei_no_hi(int dd, int wm, int wy , int pos){
		if((dd==29) && (wm==4)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	/**
	 * 憲法記念日
	 * @param dd
	 * @param wm
	 * @param wy
	 * @param pos
	 * @return
	 */
	public	boolean	isKenpouKinen_bi(int dd, int wm, int wy , int pos){
		if((dd==3) && (wm==5)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	/**
	 * 国民の休日
	 * @param dd
	 * @param wm
	 * @param wy
	 * @param pos
	 * @return
	 */
	public	boolean	isKokiminn_no_Kyujitsu(int dd, int wm, int wy , int pos){
		if((dd==4) && (wm==5)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	/**
	 * こどもの日
	 * @param dd
	 * @param wm
	 * @param wy
	 * @param pos
	 * @return
	 */
	public	boolean	isKodomo_no_hi(int dd, int wm, int wy , int pos){
		if((dd==5) && (wm==5)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	/**
	 * 海の日
	 * @param dd
	 * @param wm
	 * @param wy
	 * @param pos
	 * @return
	 */
	public	boolean	isUmi_no_hi(int dd, int wm, int wy , int pos){
		if((wm==7) && isThirdMonday(dd,pos)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	/**
	 * 敬老の日
	 * @param dd
	 * @param wm
	 * @param wy
	 * @param pos
	 * @return
	 */
	public	boolean	isKeirou_no_hi(int dd, int wm, int wy , int pos){
		if((dd==15) && (wm==9)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	/**
	 * 秋分の日
	 * @param dd
	 * @param wm
	 * @param wy
	 * @param pos
	 * @return
	 */
	public	boolean	isShuuBun_no_hi(int dd, int wm, int wy , int pos){
		//
		if((wm==9)&&(dd==aki_bun[wy - 2000])){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	/**
	 * 体育の日
	 * @param dd
	 * @param wm
	 * @param wy
	 * @param pos
	 * @return
	 */
	public	boolean	isTaiku_no_hi(int dd, int wm, int wy , int pos){
		if((wm==10) && isSecondMonday(dd,pos)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	/**
	 * 文化の日
	 * @param dd
	 * @param wm
	 * @param wy
	 * @param pos
	 * @return
	 */
	public	boolean	isBunka_no_hi(int dd, int wm, int wy , int pos){
		if((dd==3) && (wm==11)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	/**
	 * 勤労感謝の日
	 * @param dd
	 * @param wm
	 * @param wy
	 * @param pos
	 * @return
	 */
	public	boolean	isKinrouKansha_no_hi(int dd, int wm, int wy , int pos){
		if((dd==23) && (wm==11)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	/**
	 * 天皇誕生日
	 * @param dd
	 * @param wm
	 * @param wy
	 * @param pos
	 * @return
	 */
	public	boolean	isTennouTanjo_bi(int dd, int wm, int wy , int pos){
		if((dd==23) && (wm==12)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}

	
	
}
