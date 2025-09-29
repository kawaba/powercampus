/*
  講義定義レコードのセクションデータの処理をカプセル化する 
*/
package kamoku;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Vector;
import database.Database;
import epml.tools.Regularizer;
import framework.LOG;
//import java.lang.Exception;
import tktools.Gear;
/*
■講義セクション実施データ

　te_aplec_key は講義実施データのキー　teUid + '-' + aplec_key を意味する
　
　例示：　kawaba01-003
  
　フルキーの例示： kawaba01-003-00012

  実施にかかる日付と講義メモのデータ

create table app_sect (
       te_aplec_key     CHAR(12)  NOT NULL,
       sect_key         CHAR(5)   NOT NULL,
       s_mm             CHAR(2),
       s_dd             CHAR(2),
       e_mm             CHAR(2),
       e_dd             CHAR(2),
       memo             text
);

// キーは複数列に設定する
create index sect_idx on app_sect (te_aplec_key,sect_key);

*/
public class KamokuSecApRecord extends Object{
	//
	// 定数
	public static int	TE_APLEC_KEY	= 0;	// te_kec_key   12 桁
    public static int	SECT_KEY		= 1;	// sect_key      5 桁
	public static int	S_MM			= 2;	// 実施開始月
	public static int	S_DD			= 3;	// 同日
	public static int	E_MM			= 4;	// 実施終了月（空白でも可）
	public static int	E_DD			= 5;	// 同日（空白でも可）
	public static int	MEMO			= 6;	// 講義メモ（講義実施前、実施後のメモ）
	//
	public static int	MIN		= TE_APLEC_KEY;
	public static int	MAX		= MEMO;
	//
	public static String [] dayOfWeek = {"","日","月","火","水","木","金","土"};  // 6=金 
	//
	Vector rec;
	SectionRecord record;
	
	public Vector get_rec_of_KamokuSecApRecord() {
		return rec;
	}
	public SectionRecord getRecord() {
		return record;
	}
	//
	///////////////////////////////////////////////////
	//
	//   コンストラクタ
	//
	///////////////////////////////////////////////////
	// 空のレコードを生成する
	public KamokuSecApRecord(){
		//
		Vector w = new Vector(20,10);
		rec	= copy(w);
	}
	//
	// Vector から生成する（レコード中に null があれば "" にする）
	public KamokuSecApRecord(Vector w){
			rec	= copy(w);
	}
	// 
	// キーでデータベースを検索して生成する（レコード中に null があれば "" にする）
	public KamokuSecApRecord(String _te_aplec_key,String _sect_key,Database db){
		if(LOG.fa) LOG.println( "コンストラクタ KamokuSecApRecord()を実行します","te_aplec_key=" + _te_aplec_key + " / sect_key=" + _sect_key);
		//
		rec	= new Vector (20,10);
		for(int i=MIN; i<=MAX; i++){
			rec.add(i,"");
		}
		if(_te_aplec_key != null){
			Vector w = db.getSectionAp(_te_aplec_key,_sect_key);
			rec		 = copy(w);
		}
	}

	// キーでデータベースを検索して生成する
	public KamokuSecApRecord(Database db, String _te_aplec_key,String _sect_key){

			record = db.getSectionRecord(_te_aplec_key,_sect_key);
		
	}
	
	//------------------------------------------------------------------------------
	//  全ての要素を "" に初期化したのち、null でないものだけをコピーする
	//  コピーは、レコードサイズを超えて行われることはない
	Vector copy(Vector w){
		//
		Vector v = new Vector( 20,10 );
		for(int i=MIN; i<=MAX; i++){	// "" に初期化しておく
			v.add(i,"");
		}
		// w をコピーする
		// null の要素は コピーしない
		//
		for(int i=MIN; ( (i<w.size())&&(i<=MAX) ); i++){
			if(w.get(i)!=null){	v.set(i,w.get(i)); }
		}
		return v;
	}
	
	
	///////////////////////////////////////////////////
	//
	//　　データベースの更新
	//
	///////////////////////////////////////////////////
	//
	// 挿入
	public int insert(Database db){
		int    n = db.insertSectionAp( rec ); 
		return n;
	}
	// 更新
	public int update(Database db){
		int    n = db.updateSectionAp( rec );
		return n;
	}
	// 削除
	public int delete(Database db){
		int    n = db.deleteSectionAp( te_aplec_key(),sect_key() );
		return n;
	}
	///////////////////////////////////////////////////
	//
	//　　レコードの値をセットする
	//
	///////////////////////////////////////////////////
	//
	public void set_te_aplec_key(String str)	{ rec.set(TE_APLEC_KEY,str); 	}
	public void set_sect_key(String str)		{ rec.set(SECT_KEY,str); 		}
	//
	public void set_s_mm(String str)			{ rec.set(S_MM,str); 	}
	public void set_s_dd(String str)			{ rec.set(S_DD,str); 	}
	public void set_e_mm(String str)			{ rec.set(E_MM,str); 	}
	public void set_e_dd(String str)			{ rec.set(E_DD,str); 	}
	public void set_memo(String str)			{ rec.set(MEMO,str); 	}
	//
	///////////////////////////////////////////////////
	//
	//　　レコードの値を返す
	//
	///////////////////////////////////////////////////
	//
	// データベースから引いたとき、実はレコードが空でなかったかどうか
	public boolean isEmpty(){
		if(te_aplec_key().length()==0)	return true;
		return	false;
	}
	//
	// セクションのフルキーを返す（課題等の検索用）
	public String te_aplec_sect_key()	{	return  te_aplec_key() + "-" + sect_key(); }
	//
	public String te_aplec_key()		{	return (String)rec.get(TE_APLEC_KEY); 	}
	public String sect_key()			{	return (String)rec.get(SECT_KEY); 		}
	//
	public String s_mm()				{	return ((String)rec.get(S_MM)).trim(); }
	public String s_dd()				{	return ((String)rec.get(S_DD)).trim(); }
	public String e_mm()				{	return ((String)rec.get(E_MM)).trim(); }
	public String e_dd()				{	return ((String)rec.get(E_DD)).trim(); }
	public String memo()				{	return ((String)rec.get(MEMO)).trim(); }
	//
	//
	// 開始月を半角数字で返す
	// 05 または 5 であれば、返値は "05" となる
	// それ以外では "" を返すので受け取り側でチェックする
	public String	getStartMonth(){
		Regularizer	rgl	=	new Regularizer();
		String		mm	=	rgl.toDigit( rgl.removeSpace( s_mm() ) );	// 空白文字を取り除いて処理
		if(Gear.isEmpty(mm))	return	"";
		if(mm.length() != 2){
			if(mm.length() == 1)	return	"0" + mm;
			return	"";
		}
		return	mm;
	}
	//
	// 開始日を半角数字で返す
	// 05 または 5 であれば、返値は "05" となる
	// それ以外では "" を返すので受け取り側でチェックする
	public String	getStartDay(){
		Regularizer	rgl	=	new Regularizer();
		String		dd	=	rgl.toDigit( rgl.removeSpace( s_dd() ) );
		if(Gear.isEmpty(dd))	return	"";
		if(dd.length() != 2){
			if(dd.length() == 1)	return	"0" + dd;
			return	"";
		}
		return	dd;
	}
	//
	public String	getEndMonth(){
		return	get00Str(e_mm());
	}
	public String	getEndDay(){
		return	get00Str(e_dd());
	}
	public String	get00Str(String str){
		Regularizer	rgl	=	new Regularizer();
		String		dd	=	rgl.toDigit( rgl.removeSpace( str ) );
		if(Gear.isEmpty(dd))	return	"";
		if(dd.length() != 2){
			if(dd.length() == 1)	return	"0" + dd;
			return	"";
		}
		return	dd;
	}
	
	//
	// 日付の曜日を返す
	//
	public String dayOfWeekFrom(String yyyy){
			return	dayOfWeekStr(fullYear(yyyy) ,getStartMonth() ,getStartDay());
		
	}
	public String dayOfWeekTo(String yyyy){
		if(!isValidYear(yyyy))	return	"";
		if(!isValidEndDate())	return	"";
		return	dayOfWeekStr(fullYear(yyyy) ,getEndMonth() ,getEndDay());
		
	}
	//
	// 開始日のカレンダーオブジェクトを返す
	// 開始日がない場合は null を返す
	public	GregorianCalendar	startDay(String yyyy){
		if(!isValidYear(yyyy))	return	null;
		if(!isValidStartDate())	return	null;
		return	calStr(fullYear(yyyy) ,getStartMonth() ,getStartDay());
	}
	
	
	//
	// 日付を  ○月○日（□）～　 ○月○日（□）　の形に整形して返す
	//
	public String dateString(String yyyy){
		if(!isValidYear(yyyy))	return	"";
		if(!isValidStartDate())	return	"";
		String	retStr	= 	formattedDate(fullYear(yyyy) ,getStartMonth() ,getStartDay());
		if(!isValidEndDate())	return	retStr;
		//
		String	retStr2	=	formattedDate(fullYear(yyyy) ,getEndMonth() ,getEndDay());
		return	retStr + "～" + retStr2;
	}
	boolean	isValidYear(String y){
		if(isEmpty(y))	return	false;
		if((y.length() != 2) && (y.length() != 4))	return	false;
		return	true;
	}
	boolean	isValidStartDate(){
		if( isEmpty(s_mm())||isEmpty(s_dd()) )	return	false;
		return	true;
	}
	boolean	isValidEndDate(){
		if( isEmpty(e_mm())||isEmpty(e_dd()) )	return	false;
		return	true;
	}
	String	fullYear(String y){
		if(y.length()==2){
			y = "20" + y;
		}
		return	y;
	}
	//
	//
	//  特に日付を  ○月○日（□）　の形に整形して返す
	//
	public String formattedDate(String yyyy,String mm, String dd){
    	int	year	= Integer.parseInt(yyyy) ;
		int	month	= Integer.parseInt(mm);
		int	day		= Integer.parseInt(dd) ;
		
        LocalDate date = LocalDate.of(year, month, day);
        
        // 曜日のフォーマットを日本語で指定
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM月dd日（E）");
        return date.format(formatter);
	}
	
	
	// 日付のカレンダーオブジェクトを返す
	public GregorianCalendar calStr(String yyyy,String mm, String dd){
    	int	year	= Integer.parseInt(yyyy) ;
		int	month	= Integer.parseInt(mm) -1;	// 0 オリジン
		int	day		= Integer.parseInt(dd) ;
		//
		if((month>=0)&&(month<=1)){ // yyyy は年度指定なので、１月から２月は年を１増やす
			year++;
		}
		return	new GregorianCalendar(year, month, day);
	}
	// 日付の曜日文字列を返す
	public String dayOfWeekStr(String yyyy,String mm, String dd){
    	int	year	= Integer.parseInt(yyyy) ;
		int	month	= Integer.parseInt(mm) -1;	// 0 オリジン
		int	day		= Integer.parseInt(dd) ;
		//
		if((month>=0)&&(month<=1)){ // yyyy は年度指定なので、１月から２月は年を１増やす
			year++;
		}
		GregorianCalendar xdate2 	= new GregorianCalendar(year, month, day);
		Calendar xdate 	= xdate2;
		int dwIndex 	= xdate.get(Calendar.DAY_OF_WEEK);    // 6=Friday
		//
		return	dayOfWeek[dwIndex];
	}
	
	//
	//
	//
    String get00type(int s){
        String  dt      = String.valueOf(s);
        if((dt == null)||(dt.length() == 0)) return "00";
        int     pos     = dt.length();
        String  pattern = "00" + dt;
        return  pattern.substring(pos);
    }
	boolean	isEmpty(String s){
		if( (s==null)||(s.length()==0)) return true;
		return false;
	}
	
	//
	//
	//
   //
    // 今日の日付のカレンダーオブジェクトを返す
    GregorianCalendar currentDay(){
        return new GregorianCalendar();
    }
    // cal から minutes だけ先のカレンダーオブジェクトを計算して返す
    GregorianCalendar calculateDay(GregorianCalendar cal,int minutes){
        return calculateDayM(cal,minutes);
    }
    GregorianCalendar calculateDayM(GregorianCalendar cal,int minutes){
        GregorianCalendar cc = calculateDayMinutes(cal,minutes,true);
        return cc;
    }
    // cal から hours だけ先のカレンダーオブジェクトを計算して返す
    GregorianCalendar calculateDayH(GregorianCalendar cal,int hours){
        GregorianCalendar cc = calculateDayHour(cal,hours,true);
        return cc;
    }
    //  cal から minutes だけ先（前）のカレンダーオブジェクトを計算して返す
    GregorianCalendar calculateDayMinutes(GregorianCalendar cal,int minutes,boolean sw){
        if(sw){
            cal.add(Calendar.MINUTE,minutes);
        }else{
            cal.add(Calendar.MINUTE,-1*minutes);
        }
        return cal;
    }
    //  cal から hour だけ先（前）のカレンダーオブジェクトを計算して返す
    GregorianCalendar calculateDayHour(GregorianCalendar cal,int hours,boolean sw){
        if(sw){
            cal.add(Calendar.HOUR_OF_DAY,hours);
        }else{
            cal.add(Calendar.HOUR_OF_DAY,-1*hours);
        }
        return cal;
    }
    // 特定の日付を yyyy-mm-dd-HH-MM の文字列で返す
    String CalToStr(GregorianCalendar date){
        return CalToStr(date,true);
    }
    String CalToStr(GregorianCalendar date,boolean sw){
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
	// 年、月で特定される日付けを CalToStr と同じ文字列にして返す
    String CalToStr_app(int yy,int mm,boolean sw){
		GregorianCalendar theDay = new GregorianCalendar(yy, mm-1, 1,0,0,0); // yy年mm月1日0時0分0秒
		return CalToStr(theDay, sw);
	}
	//
    String get000type(int s){
        String  dt      = String.valueOf(s);
        if((dt == null)||(dt.length() == 0)) return "000";
        int     pos     = dt.length();
        String  pattern = "000" + dt;
        return  pattern.substring(pos);
    }
    String getSStype(int s){
        String  dt      = String.valueOf(s);
        if((dt == null)||(dt.length() == 0)) return "00";
        int     pos     = dt.length();
        String  pattern = "  " + dt + " ";
        return  pattern.substring(pos);
    }
	
    //---------------------------------------------------------------------------------
    //
    //現在の日付の文字列を得る
    public String getDate(){
        return getDate("yyyy年MM月dd日HH時mm分ss秒");
    }
    // yyyyMMddHH などを指定する
    public String getDate(String form){
        SimpleDateFormat format = new SimpleDateFormat(form);
        String strDate = format.format(Calendar.getInstance().getTime());
        return strDate;
    }
    // うるう年かどうか(すこしいい加減)
    public boolean isLeapYear(String yy){
        int ck = Integer.parseInt(yy) % 4;
        return ck == 0;
    }
}