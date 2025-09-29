/**
 * DateInfo<br>
 *  一連の日付がそれぞれ休日かどうかを判定する.
 * @version 1.0  05/04/2004
 * @author  Takashi Kawaba 
 */
package tktools;
import java.util.Calendar;
import java.util.GregorianCalendar;
//import	java.io.*;
//
// javadoc -charset Windows-31J -version -author -public DateInfo.java
// http://java.sun.com/j2se/1.4/ja/docs/ja/tooldocs/win32/javadoc.html#javadoctags
//
/**
 * DateInfo<br>
 * 　一連の日付がそれぞれ休日かどうかを判定する.<br>
 *	<br>
 *	コンストラクタに与える furikaeFlag は振替休日を判定するフラグ．<br>
 *  通常は false を指定するが、テストしたい最初の日付が振り替え休日ならtrue を指定する<code>
 *<pre>    GregorianCalendar   day   = new GregorianCalendar();
 *    DateInfo  info  = new DateInfo(day,false );
 *    if(info.isHollyday()){
 *        休日の処理
 *    }
 *
 *    連続して休日かどうか調べるときは直前の<code>furikaeFlagを info </code>から求めてコンストラクタに渡す
 *
 *    while(･･･････){
 *        day.add(Calendar.DAY_OF_MONTH,1);   // 次の日に進める
 *        info  = new DateInfo(day,info.furikaeFlag());
 *        if(info.isHollyday()){
 *            休日の処理
 *        }
 *    }</pre></code>
 *<br>
 * @version 1.0  05/04/2004
 * @author  Takashi Kawaba
*/
public class DateInfo extends Object {
	//
	// 日付け
	GregorianCalendar	cal;
	int			year;			// 年
	int			month;			// 月
	int			today;			// 日
	int			weekday;		// 曜日
	boolean		furikaeFlag;	// 振り替えフラグ
	boolean		hollyday;		// 休日の時true;
    //
    //  2000 - 2033 までの春分の日の日付
    //
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
	//
	static	final String	[] dateStr	= {"日", "月", "火", "水", "木", "金", "土" };
	
    /**
     * 休日かどうか調べるためのクラスを生成する.<br>
     *   @param _date         休日かどうか検査する最初の日付 <br>
     *   @param _furikaeFlag  振り替え休日であることを示すフラグ（日付が振替休日なら true を指定）<br>
     *
     */
	 
	public DateInfo(GregorianCalendar _date,boolean _furikaeFlag){
        //
		//
		cal				=	_date;								// 開始日のカレンダー
		year			=	cal.get(Calendar.YEAR);				// 年
		month			= 	cal.get(Calendar.MONTH);			// 月(実際の月数より１少ない数）
		today			=	cal.get(Calendar.DAY_OF_MONTH);		// 日
		weekday			=	cal.get(Calendar.DAY_OF_WEEK) - 1;	// 曜日（0 ～ 6) ０は日曜日
		furikaeFlag		=	_furikaeFlag;						// 振替休日フラグ
		//
		// 休日かどうかをセットする
		if(getHollydayFlag(today, month, year, weekday)){
			hollyday	=	true;
		}else if(furikaeFlag){
			furikaeFlag	=	false;
			hollyday	= 	true;
		}else{
			hollyday	= 	false;
		}
    }
    /**
     * 休日かどうか判定する．
     * 休日であってきさらに振り替え休日が発生するときは 振り替えフラグを<code> true </code>に設定する．
     * 振り替えフラグの設定はコンストラクタで行われる．
     * 振り替えフラグは<code> furikaeFlag() </code>で取得できる．
     * @return 休日のとき true、平日の時 false 
     * 
     */
	// 休日かどうか
	public	boolean	isHollyday(){
		return	hollyday;
	}
    /**
     * 更新された振替フラグを返す．
     * 振り替えフラグの設定はコンストラクタで行われる．
     * @return 振り替え休日のとき true、平日の時 false 
     * 
     */
	// 
	public	boolean	furikaeFlag(){
		return	furikaeFlag;
	}
	//
	public	String	year(){
		year	= year	-	2000;
		return	get00String(year);
	}
	//
	public	String	month(){
		return	get00String(month + 1);
	}
	//
	public	String	day(){
		return	get00String(today);
	}
	//
	public	String	weekday(){
		return	dateStr[weekday];
	}
	//
	//
	//
	boolean	getHollydayFlag(int dd, int wm, int wy, int pos){
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
	//
	//  日曜日かどうか( pos は 0～6 なのでカレンダーでの値にあわせるには＋１する）
	boolean isSunday(int	pos){
		int	chk	= pos + 1;
		if( chk	== Calendar.SUNDAY)	{ return true; }
		return	false;
	}
	//
	//  第二月曜日かどうか( pos は 0～6 なのでカレンダーでの値にあわせるには＋１する）
	boolean isSecondMonday(int dd, int	pos){
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
	boolean isThirdMonday(int dd, int	pos){
		int	chk	= pos + 1;
		if( chk	== Calendar.MONDAY)	{
			if((dd >14)&&(dd<22)){
				return	true;
			}
		}
		return	false;
	}
	//
	// 元旦
	boolean	isGantan(int dd, int wm, int wy , int pos){
		if((dd==1) && (wm==1)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	// 成人の日
	boolean	isSeijin_no_hi(int dd, int wm, int wy , int pos){
		if((wm==1)&&(isSecondMonday(dd,pos)))	return	true;
		return 	false;
	}
	// 建国記念日
	boolean	isKenkokuKinen_bi(int dd, int wm, int wy , int pos){
		if((dd==11) && (wm==2)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	// 春分の日
	boolean	isShunBun_no_hi(int dd, int wm, int wy , int pos){
		if((wm==3)&&(dd==haru_bun[wy - 2000])){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	// みどりの日
	boolean	isMidoei_no_hi(int dd, int wm, int wy , int pos){
		if((dd==29) && (wm==4)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	// 憲法記念日
	boolean	isKenpouKinen_bi(int dd, int wm, int wy , int pos){
		if((dd==3) && (wm==5)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	// 国民の休日
	boolean	isKokiminn_no_Kyujitsu(int dd, int wm, int wy , int pos){
		if((dd==4) && (wm==5)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	// こどもの日
	boolean	isKodomo_no_hi(int dd, int wm, int wy , int pos){
		if((dd==5) && (wm==5)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	// 海の日
	boolean	isUmi_no_hi(int dd, int wm, int wy , int pos){
		if((wm==7) && isThirdMonday(dd,pos)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	// 敬老の日
	boolean	isKeirou_no_hi(int dd, int wm, int wy , int pos){
		if((dd==15) && (wm==9)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	// 秋分の日
	boolean	isShuuBun_no_hi(int dd, int wm, int wy , int pos){
		//
		if((wm==9)&&(dd==aki_bun[wy - 2000])){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	// 体育の日
	boolean	isTaiku_no_hi(int dd, int wm, int wy , int pos){
		if((wm==10) && isSecondMonday(dd,pos)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	// 文化の日
	boolean	isBunka_no_hi(int dd, int wm, int wy , int pos){
		if((dd==3) && (wm==11)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	// 勤労感謝の日
	boolean	isKinrouKansha_no_hi(int dd, int wm, int wy , int pos){
		if((dd==23) && (wm==11)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	// 天皇誕生日
	boolean	isTennouTanjo_bi(int dd, int wm, int wy , int pos){
		if((dd==23) && (wm==12)){
			if(isSunday(pos)){	furikaeFlag	= true; }
			return	true;
		}
		return 	false;
	}
	//
	// 先頭を０で埋めて 2 桁の整数にする
    String get00String(int n){
        //
		String	s		=	String.valueOf(n);
        int     pos 	=	s.length();
        String  pattern =	"00" + s;
        return  pattern.substring(pos);
    }
}
