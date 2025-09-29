/*
 * 作成日: 2005/02/04
 *
 * TODO
 */
package tktools;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import framework.LOG;

/**
 *
 */
public class DateGear {
    
    

	/**
	 * 現在の月に対応したシーズン文字列を返す<br>
	 * 
	 * 例えば現在が５月なら getSeason("4,4,4,9,9,9,9,9,9,9,9,9") は文字列 "4" を返す
	 * 例えば現在が２月なら getSeason("4,9") は文字列 "9" を返す
	 * 
	 * @param termCsv	シーズンを表す文字列を12個並べたCSV文字列．<br>
	 *                  最初の文字が１月に、最後の文字が１２月に対応する．<br>
	 * 　　　　　　　　　例：　"b,b,b,a,a,a,a,a,b,b,b,b"　（規定値）<br>
	 * 
	 *                  ※ 指定しないと上の規定値が使われる．
	 * 
	 * @return			現在の月に対応するシーズン文字列
	 */
	public static String	getSeason(String termCsv){
		if(LOG.fa) LOG.println("■ Gear #getyear()");
		
		/*
		 * シーズン文字列を配列にセットする
		 */
		Csv	term	=	null;
		if(Gear.isEmpty(termCsv)){
			term	=	new	Csv("b,b,b,a,a,a,a,a,b,b,b,b");
		}else{
			term	=	new	Csv(termCsv);
			if(term.size()!=12){
				term	=	new	Csv("b,b,b,a,a,a,a,a,b,b,b,b");
			}
		}
		/*
		 * 現在の月を得る
		 */
		GregorianCalendar	cal		=	new GregorianCalendar();
		int					month	=	cal.get(Calendar.MONTH);
		if(LOG.fa){
			LOG.println("  □ 月:" + month);
		}
		/*
		 * 月に応じたシーズン文字列を返す
		 */
		return	term.get(month);
	}
	
	public static String	getSeason(){
		return	getSeason("");
	}
	
    /** 二つの日付の差を秒数で返す */
    public static	long	difSeconds(GregorianCalendar start, GregorianCalendar now){
		/* 秒単位での差 */
		long	seconds		=	( ((Calendar)now).getTimeInMillis() - ((Calendar)start).getTimeInMillis() ) / 1000;
 		return	seconds;
	}
	/**
	 * 秒をmm:ss に直す
	 * @param seconds
	 * @return　mm:ss の文字列
	 */
    public static	String	secToMinutes(long seconds){
	    
	    long	m	=	seconds	/ 60;
	    long	s	=	seconds % 60;
	    return	String.valueOf(m) + ":" + get00type((int)s);
	    
	}
    
    /**
     * 現在の日付の文字列を得る
     * @return
     */
    public static String getDate(){
        return getDate("yyyy年MM月dd日HH時mm分ss秒");
    }
    /**
     * 現在の日付の文字列を得る
     * @return
     */
    public static String getShortDate(){
        return getDate("MM-dd HH:mm:ss");
    }
    /**
     * 現在の日付の文字列をyyyymmddHHMMの形式で得る
     * @return
     */
    public	static String getDateString(){
    	return	CalToStr(currentDay());
    	
    }
    /**
     * 現在の日付の文字列をyyyymmddHHMMmmssの形式で得る
     * @return
     */
    public	static String getLongDateString(){
    	return	getDate("yyyyMMddHHmmss");
    }
    /**
     * 現在の時分秒を６桁の固定長文字列で得る
     * @return
     */
    public	static	String	getTimeString(){
    	return	timeString(currentDay());
    }
    /**
     * 現在の時分秒を８桁の固定長文字列で得る
     * @return
     */
    public	static	String	getMiliTimeString(){
    	return	miliTimeString(currentDay());
    }
    /**
     * 現在の日時要素を２桁の固定長文字列で得る
     * @param elm  --- 要素指定文字列．YY MM DD hh mm ss ms のいづれか
     * @return
     */
    public static String getCalender(String elm){
        return	calender(currentDay(), elm);

    }
    /**
     * 特定の日付の文字列をyyyymmddHHMMmmssの形式で得る
     * @param cal
     * @return
     */
    public	static String getLongDateString(Calendar cal){
    	return	getDateFromCal("yyyyMMddHHmmss", cal);
    	
    }
    /**
     * yyyyMMddHH などを指定して現在の日付の文字列を得る
     * @param form
     * @return
     */
    public static  String getDate(String form){
        SimpleDateFormat format = new SimpleDateFormat(form);
        String strDate = format.format(Calendar.getInstance().getTime());
        return strDate;
    }
    /**
     * yyyyMMddHH などを指定して特定の日付の文字列を得る
     * @param form
     * @return
     */
    public static  String getDateFromCal(String form, Calendar cal){
        SimpleDateFormat format = new SimpleDateFormat(form);
        String strDate = format.format(cal.getTime());
        return strDate;
    }    
    
    /**
     * うるう年かどうか(すこしいい加減)
     * @param yy
     * @return
     */
    public static boolean isLeapYear(String yy){
        int ck = Integer.parseInt(yy) % 4;
        return ck == 0;
    }
	/**
	 * 現在の年度を得る
	 * @return
	 */
	public static String	getyear(){
		GregorianCalendar	cal		=	new GregorianCalendar();
		int				year	=	cal.get(Calendar.YEAR);
		int				month	=	cal.get(Calendar.MONTH);
		if(month<2){	// 2月までを同じ年度とする
			year--;
		}
		return	String.valueOf(year);
	}
	/**
	 * 今日の日付のカレンダーオブジェクトを返す
	 * @return
	 */
    public static GregorianCalendar currentDay(){
        return new GregorianCalendar();
    }	
    /**
     * 特定の日付を yyyy-mm-dd-HH-MM の形の文字列で返す
     * @param date
     * @return
     */
    public static String CalToStr(GregorianCalendar date){
        return CalToStr(date,true);
    }
    /**
     * 特定の日付を文字列で返す
     * @param date	日付
     * @param sw	整形スイッチ
     * @return		sw=true のときyyyymmddHHMM  falseのときyyyy-mm-dd/ HH:MM を返す
     *
     */
    public static String CalToStr(GregorianCalendar date,boolean sw){
        int _yy = date.get(Calendar.YEAR);
        int _mm = date.get(Calendar.MONTH) + 1;
        int _dd = date.get(Calendar.DATE);
        int _HH = date.get(Calendar.HOUR_OF_DAY);   // 2003.2.15
        int _MM = date.get(Calendar.MINUTE);
        //
        String yy = String.valueOf(_yy);    // 4桁固定
        String mm = get00type(_mm);
        String dd = get00type(_dd);
        String HH = get00type(_HH);
        String MM = get00type(_MM);
        //
        String datestr = yy +  mm +  dd +  HH +  MM;
        if(!sw) datestr = yy + "-" + mm +  "-" + dd +  "/ " + HH +  ":" + MM;
        return datestr;
    }
    /**
     * 特定の日付の要素を２桁の固定長文字列で得る
     * 
     * @param date
     * @param elm
     * @return
     */
    public static String calender(GregorianCalendar date, String elm){
        
    	if(elm.equals("YY")){
        	return	get00type(date.get(Calendar.YEAR));
        	
        }else if(elm.equals("MM")){
        	return	get00type(date.get(Calendar.MONTH) + 1);
        			
        }else if(elm.equals("DD")){
        	return	get00type(date.get(Calendar.DATE));

        }else if(elm.equals("hh")){
        	return	get00type(date.get(Calendar.HOUR_OF_DAY));

        }else if(elm.equals("mm")){
        	return	get00type(date.get(Calendar.MINUTE));
        
        }else if(elm.equals("ss")){
        	return	get00type(date.get(Calendar.SECOND));

        }else if(elm.equals("ms")){
        	return	get00type(date.get(Calendar.MILLISECOND));

        }else{
        	return	"??";
        }
    }

    /**
     * 特定の日付の時分秒を６桁の固定長文字列で得る
     * 
     * @param date
     * @return
     */
    public static String timeString(GregorianCalendar date){
        int _HH = date.get(Calendar.HOUR_OF_DAY);   // 2003.2.15
        int _MM = date.get(Calendar.MINUTE);
        int _SS = date.get(Calendar.SECOND);
        //
        String HH = get00type(_HH);
        String MM = get00type(_MM);
        String SS = get00type(_SS);
        //
        String datestr = HH +  MM + SS;
        return datestr;
    }    
    /**
     * 特定の日付の時分秒ミリ秒を８桁の固定長文字列で得る
     *
     * @param date
     * @return
     */
    public static String miliTimeString(GregorianCalendar date){
        int _HH = date.get(Calendar.HOUR_OF_DAY);   // 2003.2.15
        int _MM = date.get(Calendar.MINUTE);
        int _SS = date.get(Calendar.SECOND);
        int _MS = date.get(Calendar.MILLISECOND);
        //
        String HH = get00type(_HH);
        String MM = get00type(_MM);
        String SS = get00type(_SS);
        String MS = get00type(_MS);
        //
        String datestr = HH +  MM + SS + MS;
        return datestr;
    }    
	/**
	 * 年、月で特定される日付けを CalToStr と同じ文字列にして返す
	 * @param yy
	 * @param mm
	 * @param sw
	 * @return
	 */
    public static String CalToStr_app(int yy,int mm,boolean sw){
		GregorianCalendar theDay = new GregorianCalendar(yy, mm-1, 1,0,0,0); // yy年mm月1日0時0分0秒
		return CalToStr(theDay, sw);
	}
	/**
	 * ミリセコンドを表す文字列からフォーマットされた日付文字列を作成して返す
	 * @param miliStr
	 * @return
	 */
	public	static	String	gatDateFromMiliStr(String miliStr){
		if(Gear.isEmpty(miliStr))	return	"";
		long	mili	=	Long.parseLong(miliStr);
		return	gatDateFromMili(mili);
	}
	/**
	 * ミリセコンドを表すlong の整数値からフォーマットされた日付文字列を作成して返す
	 * @param mili
	 * @return
	 */	
	public	static	String	gatDateFromMili(long mili){
		GregorianCalendar	cal	=	new	GregorianCalendar();
		cal.setTimeInMillis(mili);
		return	getFormattedDate(cal,"yyyy/MM/dd HH:mm:ss");		
	}
	/**
	 * カレンダーオブジェクトから form で指定した書式の日付文字列を得る
	 * @param cal
	 * @param form
	 * @return
	 */
	public static String getFormattedDate(Calendar cal,String form){
		SimpleDateFormat format = new SimpleDateFormat(form);
		String strDate = format.format(cal.getTime());
		return strDate;
	}
    /**
     * cal から minutes だけ先のカレンダーオブジェクトを計算して返す
     * @param cal
     * @param minutes
     * @return
     */
    public static GregorianCalendar calculateDay(GregorianCalendar cal,int minutes){
        return calculateDayM(cal,minutes);
    }
    /**
     * cal から hours だけ先のカレンダーオブジェクトを計算して返す
     * @param cal
     * @param hours
     * @return
     */
    public static GregorianCalendar calculateDayH(GregorianCalendar cal,int hours){
        GregorianCalendar cc = calculateDayHour(cal,hours,true);
        return cc;
    }
    //////////////////////////////////////////////////////////////////////////////////
    //
    /**
     * cal から minutes だけ先のカレンダーオブジェクトを計算して返す
     * @param cal
     * @param minutes
     * @return
     */
    public	static GregorianCalendar calculateDayM(GregorianCalendar cal,int minutes){
        GregorianCalendar cc = calculateDayMinutes(cal,minutes,true);
        return cc;
    }
    /**
     * cal から minutes だけ先（前）のカレンダーオブジェクトを計算して返す
     * @param cal
     * @param minutes
     * @param sw
     * @return
     */
    public	static GregorianCalendar calculateDayMinutes(GregorianCalendar cal,int minutes,boolean sw){
        if(sw){
            cal.add(Calendar.MINUTE,minutes);
        }else{
            cal.add(Calendar.MINUTE,-1*minutes);
        }
        return cal;
    }
    /**
     *  cal から hour だけ先（前）のカレンダーオブジェクトを計算して返す
     * @param cal
     * @param hours
     * @param sw
     * @return
     */
    public	static GregorianCalendar calculateDayHour(GregorianCalendar cal,int hours,boolean sw){
        if(sw){
            cal.add(Calendar.HOUR_OF_DAY,hours);
        }else{
            cal.add(Calendar.HOUR_OF_DAY,-1*hours);
        }
        return cal;
    }
    /**
     *  cal から days だけ先（前）のカレンダーオブジェクトを計算して返す
     * @param cal
     * @param days
     * @param sw
     * @return
     */
    public	static GregorianCalendar calculateDay(GregorianCalendar cal,int days,boolean sw){
        if(sw){
            cal.add(Calendar.DAY_OF_MONTH,days);
        }else{
            cal.add(Calendar.DAY_OF_MONTH,-1*days);
        }
        return cal;
    }
    /**
     *  cal から month だけ先（前）のカレンダーオブジェクトを計算して返す
     * @param cal
     * @param month
     * @param sw
     * @return
     */
    public	static GregorianCalendar calculateMonth(GregorianCalendar cal,int month,boolean sw){
        if(sw){
            cal.add(Calendar.MONTH,month);
        }else{
            cal.add(Calendar.MONTH,-1*month);
        }
        return cal;
    }
    /**
     * int を２桁の文字列にして返す
     */
    static String get00type(int s){
        String  dt      = String.valueOf(s);
        if((dt == null)||(dt.length() == 0)) return "00";
        int     pos     = dt.length();
        String  pattern = "00" + dt;
        return  pattern.substring(pos);
    }
 
    
}
