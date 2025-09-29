/*
   ★ユーザー設定情報を操作する

　　　設定情報は、menber データベースの note フィールドにある Csv データ
　　　初期値は、# のみ。複数の場合のデリミッタは $ を前提する
	
	  exwork.conf に初期値があり、初回登録時に設定される

*/
//import java.io.*;
//import java.text.*;
//import java.util.*;
//import java.lang.Exception;
package setup;
import database.Database;
import tktools.*;
//
public class Setup extends Object{
    //
    private static final String LETTER = "abcdefghijklmnopqrstuvwxyz_ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGIT  = "1234567890";
    private static final String LETTERorDigit = ".abcdefghijklmnopqrstuvwxyz_ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    //
	//
	Database	db;
	String		uid;
	
	final	String  DLM = "$";	// 区切り文字
	public	String	message;
	//
	// 0 は設定無しの意味
	int			gyo;		// 時間割表示行数	　　　　[4]		0,1 ～ 9
	int 		night;		// 夜間過程表示行数	　　　　[4]		0,1 ～ 9
	int			course;		// e-Learning コースの有無　[4]		0,1 ～ 9
	//
	public	static	int			max_gyo    = 9;
	public	static	int			max_night  = 9;
	public	static	int			max_course = 9;
	//
	//
	String		faq_editorsRows; 	// FAQ作成エディタの行数
	String		kadai_editorsRows;	// EPML課題作成エディタの行数
	String		html_editorsRows;	// EPMLHTML作成エディタの行数
	//
	int		    deltaRows 	= 3;		// 増減単位
	int			rowMax		= 50;		// 最大行数
	int			rowMin		= 16;		// 最少行数
	String		FAQ_ROWS	= "19";		// 規定値
	String		KADAI_ROWS	= "19";
	String		HTML_ROWS	= "19";
	//
	//
	///////////////////////////////////////////////////////////////////////
	//
	//   コンストラクタ
	//
	//     データベースから設定情報を読み込む
	//
	//
	///////////////////////////////////////////////////////////////////////
	//
	public Setup	(String _uid, Database _db){
		//
		db		= _db;
		uid		= _uid;
		message	= "";	// エラーメッセージ
		//
		// 規定値の設定
		gyo		= 5;	// 一般講義は一日５時間
		night	= 0;	// 夜間課程なし
		course	= 0;	// e-Learning なし
		//
		//
		String setdt	= db.getSetupInfo(uid);	// member データベースの note フィールドを読む
		Csv		cs 		= new Csv("#");			// 全て規定値のままというサイン
		//
		if((setdt != null)&&(setdt.length() > 0)){
			cs = new Csv(setdt,DLM);
		}
		//
		String	s1	= cs.get(0);
		if(!s1.equals("#")){
			//
			// 通常時間数
			if(isNDigit(s1,1)){
				gyo	= Integer.parseInt(s1);
				if(gyo > max_gyo)		gyo = max_gyo;		// １日は９時限を最高とする(0-8)
			}else{
				gyo = 0;	// 設定無し
			}
			//
			// 夜間過程時間数
			String s2	= cs.get(1);
			if(isNDigit(s2,1)){
				night	= Integer.parseInt(s2);
				if(night > max_night)	night = max_night;	// １日は９時限を最高とする(0-8)
			}else{
				night = 0;
			}
			// e-Learning 課程の行数
			String s3	= cs.get(2);
			if(isNDigit(s3,1)){
				course	= Integer.parseInt(s3);
				if(course > max_course)		course = max_course;		//
			}else{
				course = 0;
			}
			// これから以下は新規に追加したので値があるかどうか不定である
			// cs.get(i) が "-" であれば、値がないケース
			//
			// 行数
			kadai_editorsRows	=	cs.get(3);	if(kadai_editorsRows.equals("-")) 	kadai_editorsRows	= KADAI_ROWS;
			html_editorsRows	=	cs.get(4);	if(html_editorsRows.equals("-")) 	html_editorsRows	= HTML_ROWS;
			faq_editorsRows		=	cs.get(5);	if(faq_editorsRows.equals("-")) 	faq_editorsRows		= FAQ_ROWS;
		}
	}
	//
	public	String	msg()	{ return	message;	} // エラーメッセージを返す
	//
	// 設定情報を返す
	public int			rows()				{ return gyo; }
	public int			nightSchool()		{ return night;} 
	public int			eLearningSchool()	{ return course;}
	//
	// 最大値を返す
	public int	maxGyo()    { return max_gyo;    }
	public int	maxNight()  { return max_night;  }
	public int	maxCourse() { return max_course; }
	//
	//
	// エディタ行数
	public int	kadaiRows()	{ return toInteger(kadai_editorsRows, 20);}
	public int	htmlRows()	{ return toInteger(html_editorsRows, 20);}
	public int	faqRows()	{ return toInteger(faq_editorsRows, 20);}
	//
	public String	s_kadaiRows()	{ return toIntegerString(kadai_editorsRows, "20");}
	public String	s_htmlRows()	{ return toIntegerString(html_editorsRows, "20");}
	public String	s_faqRows()		{ return toIntegerString(faq_editorsRows, "20");}
	//
	public int	deltaRows()	{ return	deltaRows; }	// 増減単位を返す
	public	int	rowMax()	{ return	rowMax;		}	
	public	int	rowMin()	{ return	rowMin;		}	

	
	/**
	 * 文字列を数値に直して返す
	 * 数値文字列として不正な場合は指定された規定値を返す
	 * @param str
	 * @param defaultValue
	 * @return
	 */	
	public	String 	toIntegerString(String str, String defaultValue){
		
		if(isNumberString(str)){
			return	str;
		}else{
			return	defaultValue;
		}
		
	}
	/**
	 * 文字列を数値に直して返す
	 * 数値文字列として不正な場合は指定された規定値を返す
	 * @param str
	 * @param defaultValue
	 * @return
	 */	
	public	int 	toInteger(String str, int defaultValue){
		
		if(isNumberString(str)){
			return	Integer.parseInt(str);
		}else{
			return	defaultValue;
		}
		
	}
	public	boolean	isNumberString(String str){
		
		int	num	= 0;
		try{
			num	=	Integer.parseInt(str.trim());
		}catch(NumberFormatException e){
			return	false;
		}
		return	true;
	}


	// 範囲をチェックする
	public	boolean	rowCheck(String	row){
		//
		int	num	= 0;
		try{
			num	=	Integer.parseInt(row.trim());
		}catch(NumberFormatException e){
			message	= "行数は半角数字で記入してください";
			return	false;
		}
		//
		if(num < rowMin){
			message	= "行数が少なすぎます（最小値=" + String.valueOf(rowMin) + ")";
			return	false;
		}
		if(num > rowMax){
			message	= "行数が多すぎます（最大値=" + String.valueOf(rowMax) + ")";
			return	false;
		}
		return	true;
	}
	//
	///////////////////////////////////////////////////////////////////////
	//
	//   設定情報を受け取る
	//
	///////////////////////////////////////////////////////////////////////
	//
	// 全てを一括して
	public void		updateRecord(String a,String b,String c,String d,String e,String f){
		updateRows(a);
		updateNight(b);
		updateCourse(c);
		//
		updateKadaiRows(e);
		updateHtmlRows(f);
		updateFaqRows(d);
	}
	// １．時間割行数
	public void		updateRows(String m){
		if(!isNDigit(m,1))		return;
		int  mm		= Integer.parseInt(m);
		if((mm >= 0)&&(mm <= max_gyo)){ 
			gyo		= mm;
		}
	}
	// ２．夜間過程
	public void		updateNight(String m){
		if(!isNDigit(m,1))		return;
		int  mm		= Integer.parseInt(m);
		if((mm >= 0)&&(mm <= max_night)){ 
			night		= mm;
		}
	}
	// ３．e-Learning コース
	public void		updateCourse(String m){
		if(!isNDigit(m,1))		return;
		int  mm		= Integer.parseInt(m);
		if((mm >= 0)&&(mm <= max_course)){ 
			course		= mm;
		}
	}
	// ４．EPML課題作成エディタの行数
	//
	public void updateKadaiRows(String e){
		kadai_editorsRows	=	toIntegerString(e, "20");
	}
	// ５．HTML作成エディタの行数
	//
	public void updateHtmlRows(String f){
		html_editorsRows	=	toIntegerString(f, "20");
	}
	// ６．FAQ作成エディタの行数
	//
	public void updateFaqRows(String d){
		faq_editorsRows		=	toIntegerString(d, "20");
	}
	
	
	
	///////////////////////////////////////////////////////////////////////
	//
	// 全情報をデータベースに書き込む
	//
	///////////////////////////////////////////////////////////////////////
	//
	public void	wrtInfo(){
		String 	str;
		
		//
		str	= String.valueOf(gyo) + DLM + String.valueOf(night) + DLM + String.valueOf(course) + DLM +
		toIntegerString(kadai_editorsRows, "20") + DLM + toIntegerString(html_editorsRows, "20") + DLM + toIntegerString(faq_editorsRows, "20");
		//
		db.wrtSetupInfo(uid, str);
	}
	//
	//////////////////////////////////  UTILTIES  ////////////////////////////////////
	//
    // 文字列がｎ桁の英字かどうかチェックする
    boolean isNLetter(String s,int n){
        if((s == null)||(s.length()==0))    return false;
        if(s.length() != n )                return false;
        //
        for(int i=0; i<n; i++){
            char ch = s.charAt(i);
            if(!xisLetter(ch))  return false;
        }
        return true;
    }
    //
    // 文字列がｎ桁の数字がどうかチェックする
    boolean isNDigit(String s,int n){
        if((s == null)||(s.length()==0))    return false;
        if(s.length() != n )                return false;
        //
        for(int i=0; i<n; i++){
            char ch = s.charAt(i);
            if(!xisDigit(ch))       return false;
        }
        return true;
    }
    // 文字列が英数字からなるかどうかチェックする
    boolean isHankaku(String s){
        if((s == null)||(s.length()==0))    return false;
        int n = s.length();
        //
        for(int i=0; i<n; i++){
            char ch = s.charAt(i);
            if(!xisLETTERorDigit(ch))   return false;
        }
        return true;
    }
    //
    boolean xisLetter(char ch){
        int n = LETTER.length();
        for(int i=0; i<n; i++){
            if(ch == LETTER.charAt(i))  return  true;
        }
        return false;
    }
    boolean xisDigit(char ch){
        int n = DIGIT.length();
        for(int i=0; i<n; i++){
            if(ch == DIGIT.charAt(i))   return  true;
        }
        return false;
    }
    boolean xisLETTERorDigit(char ch){
        int n = LETTERorDigit.length();
        for(int i=0; i<n; i++){
            if(ch == LETTERorDigit.charAt(i))   return  true;
        }
        return false;
    }	
	
	
}