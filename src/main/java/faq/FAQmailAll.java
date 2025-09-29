/*
 	質問メールレコードの処理をカプセル化する 
*
　■Ｑ＆Ａメール用データベース
		
		実施講義単位にまとめ、受け取り時間順に並べる
		
		seq_no ---- 受付番号 ６桁 KeyGen で発生
		　　　　　　個人単位（講義単位ではない）
		
		内容変更に関してはＦＡＱに対する追加・削除・訂正も連動して行う
		
	create table faq_mail (
       te_aplec_key     CHAR(12)        NOT NULL,
	   seq_no           CHAR(6)         NOT NULL,
       rvdate           VARCHAR(16)     NOT NULL,
       lec_key          CHAR(3)         NOT NULL,
       stNumber         VARCHAR(12)     NOT NULL,
       ml_title         VARCHAR(200)    DEFAULT  '',
       ml_body          text            DEFAULT  '',
       read_flag        VARCHAR(6)      DEFAULT 'OFF',
	   faq_title        VARCHAR(200)    DEFAULT '',
       faq_flag         VARCHAR(6)      DEFAULT 'OFF'
	);
	
	create index faq_mail_idx on faq_mail (te_aplec_key,seq_no);

*/
//
package faq;

import java.text.*;
import java.util.*;

import database.Database;
import database.KeyGen;
import framework.LOG;
//import java.io.*;
//import java.lang.Exception;

import tktools.*;

public class FAQmailAll extends Object {
	//
	String		from;
	String		to;
	//
    Database	db;
	Vector		all;	// レコード集合体（内容は FAQmail オブジェクト）
	Vector		key;	// キーを入れる
	int			size;
	//
	Vector		idx;	// ソート済みベクター
	boolean		sortFlag;
	//
	// コンストラクタ
	public FAQmailAll(Hashtable htb,Database _db){
		if(LOG.fa) LOG.println("class FAQmail #FAQmailAll() :  のコンストラクタの入り口です");
		//
		
		db			= _db;
		idx			= new Vector();
		sortFlag	= false;
		//
		all				= new Vector (50,20);
		Vector temp		= new Vector (50,20);
		String	te_aplec_key	= KeyGen.get_te_aplec_key(htb);
		db.read_FAQmail_all(te_aplec_key,temp);	// 全てのメールを all に得る
		//
		size	= temp.size();
		for(int i=0; i<size; i++){
			FAQmail fm	= new FAQmail(db);
			fm.byVector( (Vector)temp.get(i) );
			all.add(fm);
		}
		dateSort();		// 日付順－学籍番号順でソートしておく
		//
		// テスト出力
		if(LOG.fa){
			LOG.println("class FAQmail #FAQmailAll() : コンストラクタでのソートなしのレコードです");
			for(int i=0; i<size; i++){
				FAQmail	fm = (FAQmail)all.get(i);
				fm.testPirnt();
			}
		}
		// テスト出力
		if(LOG.fa){
			LOG.println("class FAQmail #FAQmailAll() : コンストラクタでのソート済みのレコードです");
			for(int i=0; i<size; i++){
				FAQmail	fm = getSorted(i);
				fm.testPirnt();
			}
		}
		
	}
	// コンストラクタ
	public FAQmailAll(String te_aplec_key,Database _db){
		if(LOG.fa) LOG.println("class FAQmail #FAQmailAll() :  の二つ目のコンストラクタの入り口です");
		//
		
		db			= _db;
		idx			= new Vector();
		sortFlag	= false;
		//
		all				= new Vector (50,20);
		Vector temp		= new Vector (50,20);
		//String	te_aplec_key	= KeyGen.get_te_aplec_key(htb);
		db.read_FAQmail_all(te_aplec_key,temp);	// 全てのメールを all に得る
		//
		size	= temp.size();
		for(int i=0; i<size; i++){
			FAQmail fm	= new FAQmail(db);
			fm.byVector( (Vector)temp.get(i) );
			all.add(fm);
		}
		dateSort();		// 日付順－学籍番号順でソートしておく
		//
		// テスト出力
		if(LOG.fa){
			LOG.println("class FAQmail #FAQmailAll() : コンストラクタでのソートなしのレコードです");
			for(int i=0; i<size; i++){
				FAQmail	fm = (FAQmail)all.get(i);
				fm.testPirnt();
			}
		}
		// テスト出力
		if(LOG.fa){
			LOG.println("class FAQmail #FAQmailAll() : コンストラクタでのソート済みのレコードです");
			for(int i=0; i<size; i++){
				FAQmail	fm = getSorted(i);
				fm.testPirnt();
			}
		}
		
	}
	//
	// レコード数を返す
	//
	public	int	size()	{ return size; }
	//
	//
	//  未読レコードをカウントして返す
	//
	public int	notReads(){
		int n=0;
		for(int i=0; i<size; i++ ){
			FAQmail	fm	= get(i);
			if(fm.isNotRead())	n++;
		}
		return n;
	}
	//
	// レコードを受け付け日付－学籍番号でソートする
	//
	public void	dateSort(){
		sortFlag = true;
		sort(FAQmail.RVDATE,FAQmail.STNUMBER);
	}
	//
	//  レコードを学籍番号－受付日付でソートする
	//
	public void	stNumberSortr(){
		sortFlag = true;
		sort(FAQmail.STNUMBER,FAQmail.RVDATE);
		//
		// テスト出力
		if(LOG.fa){
			LOG.println("class FAQmail #stNumberSortr() : ソート済みのレコード番号配列です");
			for(int i=0; i<size; i++){
				LOG.println("      [" +  i + "] " + ((Integer)(idx.get(i))).intValue() );
			}
		}
	}
	//
	// ソート済みのｉ番目のレコードを返す
	//
	public FAQmail getSorted(int i){
		//
		if(!sortFlag){
			LOG.errStop("class FAQmailAll #getSorted : 元のレコードがソートされていません");
		}
		//
		Integer	index	= (Integer)idx.get(i);
		int		x		= index.intValue();
		FAQmail  rec	= (FAQmail)all.get(x);
		return rec;
	}
	//
	//  第ｉ番目のレコードを返す
	//
	public FAQmail	get(int i){
		if((i < 0)||(i > size-1 )){
			LOG.println("★★ ｉが範囲外です. i = " + i);
			return null;
		}
		//
		return	(FAQmail)all.get(i);
	}
	//
	// レコードの受取日が特定の期間内かどうか調べるための期間を設定する
	public void regTerm(String syy,String smm,String eyy,String emm){
		if(LOG.fa) LOG.println("class FAQmailAll #regTerm() : レコードの受取日が特定の期間内かどうか調べるための期間を設定する の入り口です");
		if(LOG.fa) LOG.println("      from = " + syy + "/" + smm);
		if(LOG.fa) LOG.println("      to   = " + eyy + "/" + emm);
		//
		int	sy	= Integer.parseInt(syy);
		int	sm	= Integer.parseInt(smm);
		int	ey	= Integer.parseInt(eyy);
		int	em	= Integer.parseInt(emm) + 1;	// 翌月の1日
		//
		from	= CalToStr_app(sy,sm,false);
		to		= CalToStr_app(ey,em,false);
		//
	}
	//
	//ｉ番目のレコードの受取日が特定の期間内かどうか調べる
	public boolean	dateCheck(int i){
		if(LOG.fa) LOG.println("class FAQmailAll #dateCheck() : ｉ番目のレコードの受取日が特定の期間内かどうか調べる の入り口です");
		//
		if(isEmpty(from) || isEmpty(to) ) LOG.errStop("class FAQmailAll #dateCheck() : 期間が設定されていません");
		//
		//
		FAQmail	rec		= getSorted(i);
		String datew 	= (String) rec.rvdate();
		if(LOG.fa) LOG.println("class FAQmailAll #dateCheck() : 期間の開始 　　　= " + from);
		if(LOG.fa) LOG.println("class FAQmailAll #dateCheck() : 期間の終了　　　 = " + to);
		if(LOG.fa) LOG.println("class FAQmailAll #dateCheck() : レコードの受付日 = " + datew);
		//
		if( (datew.compareTo(from) >= 0) && (datew.compareTo(to) < 0) )	return true;
		return false;
	}
	//ｉ番目のレコードが未読かどうか調べる
	public boolean	notRead(int i){
		if(LOG.fa) LOG.println("class FAQmailAll #faqCheck() : ｉ番目のレコードがFAQに登録済みかどうか調べる の入り口です");
		//
		FAQmail	rec		= getSorted(i);
		return	rec.isNotRead();
		//
	}
	//ｉ番目のレコードがＦＡＱ登録済みかどうか
	public boolean	isOnFaq(int i){
		if(LOG.fa) LOG.println("class FAQmailAll #readCheck() : ｉ番目のレコードが未読かどうか調べる の入り口です");
		//
		FAQmail	rec		= getSorted(i);
		return	rec.isOnFaq();
	}
	
	/////////////////////////////////////////////////////////////
	//
	//    ソートメソッド
	//
	/////////////////////////////////////////////////////////////
	//
	// レコードを第ｋ番目の項目の順で並び替える
	public void sort(int k,int added){
		int n			= all.size();
		if(n < 1 ) return;
		String  [] rec 	= new String[n];		// ソート用配列
		for(int i=0; i<n; i++){
			String n_3  = get000type(i);
			FAQmail vd	= (FAQmail)all.get(i);
			rec[i]	    = vd.get(k) + vd.get(added) + "$" + n_3;	// (ｋ+ added)番目の項目＋ レコード番号 （$はデリミッタ） 
		}
		if(n > 1) { Arrays.sort(rec);	} // ソート(n==1 ならソートしない)
		//
		Vector temp  = new Vector(100,50);
		for(int i=0; i<n; i++){
			Csv      cs  = new Csv(rec[i],"$");			// デリミッタ指定
			Integer  num = new Integer( cs.get(1) ); 	// レコード番号部分
			//
			temp.add(num);// レコード番号を登録していく
		}
		idx = temp;	// ソート済みと差し替える
	}
	// 先頭を０で埋めて 3 桁の整数にする
    String get000type(int s){
        String  dt      = String.valueOf(s);
        if((dt == null)||(dt.length() == 0)) return "000";
        int     pos     = dt.length();
        String  pattern = "000" + dt;
        return  pattern.substring(pos);
	}
    //----------------- 日付暗号化用 -------------------------------------
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
    // 特定の日付を yyyy-mm-dd-HH-MM の形ｮの文字列で返す
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
    String get00type(int s){
        String  dt      = String.valueOf(s);
        if((dt == null)||(dt.length() == 0)) return "00";
        int     pos     = dt.length();
        String  pattern = "00" + dt;
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
	// 文字列が空かどうかテストする
	//
	boolean isEmpty(String str){
		if(str==null) 			return  true;
		if(str.length()==0)		return  true;
		return false;
	}
}