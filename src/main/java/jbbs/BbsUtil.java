/*
 *
 */
package jbbs;
import tktools.*;
import	framework.*;

import java.util.*;
import java.text.*;
import java.io.*;

import database.DbConnectionBroker;

/**
 * 
 */
public class BbsUtil implements PCvar{

	public	BbsUtil(){
	}
	/**
	 * ','をセパレータとみなして Csv を作成する
	 * 
	 */
	public	static	Csv	makeAttachmentCsv(String fileList){

		Csv	cs	=	new	Csv(fileList);
		return	cs;
		
	}
	/**
	 * アップロードファイルリストをCSVに作成する
	 * 
	 * 
	 * 添付ファイルには先頭に"*"を付加する
	 * @param 	ht	アップロードファイルのハッシュ
	 * @return		アップロードファイルリスト
	 */
	public	static	String	getAttachmentCSV(Hashtable ht){
		if(LOG.fa)	LOG.println("■ BbsMakeForum #getAttachmentCSV()");
		
		StringBuffer	bf	=	new StringBuffer();
		Enumeration		en	=	ht.keys();
		boolean		fl	=	false;
		while(en.hasMoreElements()){
			if(fl)	bf.append(",");
			String	fname	=	(String)en.nextElement();
			String	mark	=	Gear.strHashSP(ht, fname);
			if(!Gear.isEmpty(mark)&&!Gear.isEmpty(fname)){
				bf.append(mark);
				bf.append(fname);
				fl	=	true;
			}else if(!Gear.isEmpty(fname)){
			    bf.append(fname);
			    fl	=	true;
			}
		}
		if(bf.length()==0)	return	"";
		return	bf.toString();
	}
	public	static	boolean	isHtml(Csv cs, int pos){
		if(LOG.fa)	LOG.println("■ BbsForumPreview #isAttachment()");

		if(pos >= cs.size())	return	false;
		
		String	fname	=	cs.get(pos);
		String	ext		=	FileGear.getExt(fname);
		if(Gear.isEmpty(ext)){// 拡張子なし
			return	false;
		}
		ext		=	(ext).toLowerCase();
		if(ext.equals("html")||ext.equals("htm")){
		    return	true;
		}else{
		    return	false;
		}
	}
	/**
	 * フォーマット記述行（## から始まる行）を取り除く
	 * @param content
	 * @return
	 */
	public	static	String	trimFormat(String	content){
	    StringBuffer	buf	=	new	StringBuffer();
	    BufferedReader	in	=	new	BufferedReader( new StringReader(content));
	    String			ln;
	    try {
            while((ln=in.readLine())!=null){
                
                if(!isFormat(ln)){
                    buf.append(ln);
                    buf.append(CR);
                }
            }
            in.close();
        } catch (IOException e) {
            // TODO 自動生成された catch ブロック
            e.printStackTrace();
        }
	    return	buf.toString();
	}
	/*
	 * フォーマット記述かどうか
	 */
	public	static	boolean	isFormat(String	ln){
        if(ln!=null){
            String	chk	=	ln.trim();
            if((chk.trim()).length()>=2){
                if((chk.substring(0,2)).equals("##")){
                   if(LOG.fa){
                       LOG.println("[" + true +  "]" + chk);
                   }
                   return	true;
                }
            }
        }
        LOG.println("[" + false +  "]" + ln);
	    return	false;
	    
	}
	
	
	/**
	 * ゲストユーザーの情報の初期値をハッシュにセットする
	 * @return
	 */
	public	static	Hashtable	mkGuestProperty(Param para){
	    
	    return	setInitialData(
	            				"guest",
	            				"guest",
	            				DIV_GUEST,
	            				"ゲスト",
	            				"",
	            				para
	    );
	}
	
	/** 初期値をハッシュにセットする */
	public static	Hashtable	setInitialData(String userid, String passwd, String division, String name, String mail, Param para){
		Hashtable	rec	=	new Hashtable();
		//
		rec.put("_userid"	,userid);
		rec.put("_passwd"	,passwd);
		rec.put("_division"	,division);
		rec.put("_name"		,name);
		rec.put("_mail"		,mail);
		//
		rec.put("_handle"	,"");
		rec.put("_iconfile"	,"");
		rec.put("_signature",mail);
		
		if(para!=null){
		    rec.put("_formatStyle"	,para.formatTemplate());	// 初期値
		}else{
		    rec.put("_formatStyle"	,"");
		}
		
		rec.put("_editor"	,"20");
		
		return	rec;
	}
	/**
	 * キーでユーザー情報を引いてフォーマットを返す
	 * @param uid
	 * @param szDB
	 * @param broker
	 * @return
	 */
	public	static String	getFormat(String uid,String szDB, DbConnectionBroker broker){
	    
	    Hashtable	ht	=	getUserInfo(uid, szDB, broker);
	    if(ht!=null){
	        return	Gear.strHash(ht,BbsInfoDB.FORMAT);
	    }else{
	        return	null;
	    }
	}	
	/**
	 * キーでユーザー情報を引いて全情報を返す
	 * @param ownerkey
	 * @param szDB
	 * @param broker
	 * @return
	 */
	public static Hashtable	getUserInfo(String uid,String szDB, DbConnectionBroker broker){
		BbsInfoDB	infoDB	=	new BbsInfoDB(szDB, broker);
		Hashtable	record	=	new Hashtable();
		int			n		=	infoDB.readBbsInfo(uid,record);
		if(n==0){
		    return	null;
		}
		return	record;
		
	}
	/**
	 * キーでユーザー情報を引いて氏名を返す
	 * @param uid
	 * @param szDB
	 * @param broker
	 * @return
	 */
	public static String	getOwnerName(String uid,String szDB, DbConnectionBroker broker){
		BbsInfoDB	infoDB		=	new BbsInfoDB(uid, szDB, broker);
		Hashtable	record		=	new Hashtable();
		infoDB.readBbsInfo(uid,record);
		return	Gear.strHashSP(record, BbsInfoDB.NAME);
		
	}
	/**
	 * キーでユーザー情報を引き、レコードを返す.レコードない場合はnullを返す
	 * @param uid
	 * @param szDB
	 * @param broker
	 * @return
	 */
	public static Hashtable	getOwnerRecord(String uid,String szDB, DbConnectionBroker broker){
		BbsInfoDB	infoDB		=	new BbsInfoDB(uid, szDB, broker);
		Hashtable	record		=	new Hashtable();
		int			cnt			=	infoDB.readBbsInfo(uid,record);
		if(cnt==0)	return	null;
		return	record;
		
	}
	/**
	 * long の日付(_data)を表す文字列を"yyyy/MM/dd HH:mm:ss"形式の日付文字列にして返す
	 * @param dateStr
	 * @return
	 */
	public	static String	getFormattedDate(String dateStr){
		GregorianCalendar	cl	=	getDate(dateStr);
		return	formattedDate(cl,"yyyy/MM/dd HH:mm:ss");
	}
	/**
	 * long の日付(_data)を表す文字列をGregorianCalendar オブジェクトに直して返す
	 * @param dateStr
	 * @return
	 */	public	static GregorianCalendar	getDate(String dateStr){
		
		long				dateValue	=	Long.parseLong(dateStr);
		GregorianCalendar	cal			=	new	GregorianCalendar();
		cal.setTimeInMillis(dateValue);
		return	cal;
	}
	/**
	 * データベースレコード（ハッシュテーブル）からlong の日付(_data)フィールドを取り出して
	 * GregorianCalendar オブジェクトに直して返す
	 * 
	 * @param ht	レコード
	 * @return		年月日文字列
	 */
	public	static GregorianCalendar	getDate(Hashtable ht){
		
		long				dateValue	=	Long.parseLong(Gear.strHash(ht,"_date"));
		GregorianCalendar	cal			=	new	GregorianCalendar();
		cal.setTimeInMillis(dateValue);
		return	cal;
	}

	/**
	 * データベースレコード（ハッシュテーブル）から long の文字列表現になった日付(_data)フィールド
	 * を取り出してlong の ミリ秒値を返す． 
	 * 
	 * @param ht
	 * @return
	 */
	public	static long	getDateValue(Hashtable ht){
		
		long	dateValue	=	Long.parseLong(Gear.strHash(ht,BbsForumDB.DATE));
		return	dateValue;
	}


	/**
	 * データベースレコード（ハッシュテーブル）から long の文字列表現になった日付(_data)フィールド
	 * を取り出しフォーマットされた日付文字列を返す
	 * 
	 * @param record
	 * @return
	 */
	public	static String	getFormattedDate(Hashtable record){
		GregorianCalendar	cl	=	getDate(record);
		return	formattedDate(cl,"yyyy/MM/dd HH:mm:ss");
	}
	
	/** カレンダーオブジェクトから form で指定した書式の日付文字列を得る */
	public  static String	formattedDate(Calendar cal,String form){
		SimpleDateFormat format = new SimpleDateFormat(form);
		String strDate = format.format(cal.getTime());
		return strDate;
	}	
	
	/** 今日の日付のlong値を返す */
	public	static	long	getTodayInMilis(){
		return	(Calendar.getInstance()).getTimeInMillis();
	}

	/** 今日の日付のlong値の文字列表現を返す */
	public	static	String	getTodayStringInMilis(){
		return	String.valueOf(getTodayInMilis());
	}
}
