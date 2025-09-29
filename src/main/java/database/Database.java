/*
データベースの更新と問い合わせ
*/
//import java.io.*;
//import java.text.*;
package database;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.Vector;
import faq.FAQ;
import faq.FAQmail;
import framework.LOG;
import framework.PCvar;
import kadai.KadaiInfo;
import kamoku.SectionRecord;
import note.NOTE;
//
import student.Student;
import tktools.Csv;
import tktools.Gear;
//
public class Database extends Object implements PCvar{
//
// デバッグ用
public static final boolean DEBUG    = false;
//
private final String m1		= "\'";
private final String m2		= "\',";
private final String m3		= ",";
//
DbConnectionBroker Broker;
//
public Database(DbConnectionBroker _Broker){
	Broker 				= _Broker;
}

///////////////////////////////////////////////////////////////

//  課題提出履歴 関係

///////////////////////////////////////////////////////////////
//
//
/*
create table kadai_000000 (
	stNumber           VARCHAR(12)   NOT NULL,
	te_aplec_key       CHAR(12)      NOT NULL,
	kadai_key          CHAR(6)       NOT NULL,
	shubetsu           CHAR(1)       NOT NULL,
	saiten_flag        CHAR(1)       DEFAULT '0',
	date_str           text          DEFAULT ''
	subject            text          DEFAULT '',
	points             VARCHAR(3)    DEFAULT ''
);
create index kadai_000000_idx on kadai_000000 (stNumber,te_aplec_key);	
*/
// 得点と採点済みフラグを更新する
public int	updateKadaiScore( String szDB,String stNumber,String te_aplec_key,String kadai_key,String score,String disposal){
	if(LOG.fa) LOG.println("class Database #updateKadaiScore() : 得点を更新する の先頭です");
	//
	Connection   conn  = null;
   	Statement 	 stmt  = null;
	int  		 cnt   = 0;
	//
	try {
      	conn 	= Broker.getConnection(); /** Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		cnt     = stmt.executeUpdate(queary_updateKadaiScore( szDB, stNumber, te_aplec_key, kadai_key, score,disposal));
	}catch(Exception e){
		System.out.println("#exwk ### 失敗:queary_update_KadaiInfo() " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/** connection を Broker に返す */
   	}
		if(LOG.fa) LOG.println("class Database #queary_update_KadaiInfo() : 課題提出履歴データベースのレコード更新件数 = " + cnt);
	return cnt;
}
//
// KadaiInfo データベースの更新用クエリの作成
String queary_updateKadaiScore(String szDB,String stNumber,String te_aplec_key,String kadai_key,String score,String disposal){
	//
	String Q = " UPDATE  kadai_" + szDB + "  set  " 
				//
				+ "saiten_flag 	= "  + m1 + disposal + m2
				+ "points 		= "  + m1 + score  	 + m1
				//
				+ "  WHERE stNumber   = " + m1 + stNumber     + m1
				+ "  AND te_aplec_key = " + m1 + te_aplec_key + m1
				+ "  AND kadai_key    = " + m1 + kadai_key    + m1;
				//
	if(LOG.fa) LOG.println("class Database #queary_updateKadaiScore() : ＦＡＱデータベースの更新用クエリ = " + Q);
	return Q;
}
//
// 全課題提出履歴を読み出す
public int read_KadaiInfo_all(String szDB,String stNumber,String te_aplec_key,Vector rec){
	if(LOG.fa) LOG.println("class Database #read_KadaiInfo_all() : 全課題提出履歴を読み出す の先頭です");
	if(LOG.fa) LOG.println("          stNumber     = " + stNumber);
	if(LOG.fa) LOG.println("          te_aplec_key = " + te_aplec_key);
	//
	Connection   conn  = null;
   	Statement 	 stmt  = null;
   	int rowCount = 0;
	try {
		String cmd 	= "SELECT * FROM  kadai_" + szDB + " " 
				+ "  WHERE stNumber     = " + m1 + stNumber     + m1
				+ "  AND   te_aplec_key = " + m1 + te_aplec_key + m1;
		
		if(LOG.fa) LOG.println("read_KadaiInfo_all() : SQL = " + cmd);
		//
      	conn 	= Broker.getConnection(); 
		stmt 	= conn.createStatement();
		ResultSet rs = stmt.executeQuery(cmd);
		while(rs.next()){
			Vector v = new Vector(20,10);
			v.add(rs.getString("stNumber"));
			v.add(rs.getString("te_aplec_key"));
			//
			v.add(rs.getString("kadai_key"));
			v.add(rs.getString("shubetsu"));
			v.add(rs.getString("saiten_flag"));
			v.add(rs.getString("date_str"));
			v.add(rs.getString("subject"));
			v.add(rs.getString("points"));
			//
			rec.add(v);
			rowCount++;
			if(LOG.fa) LOG.println(rowCount);
		}
	}catch(Exception e){
		System.out.println("### 失敗: read_KadaiInfo_all()" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);
   	}
	if(LOG.fa) LOG.println( "class Database #read_KadaiInfo_all() : 全課題提出履歴を読み出す の検索結果＝" + String.valueOf(rowCount) );
	return rowCount;
}
//
//　特定のレコードを読み出す
//
public int read_KadaiInfo(String szDB,Vector v){
	if(LOG.fa) LOG.outVector(v,"class Database #read_KadaiInfo() :  特定のレコードを読み出す の先頭です");
	//
	Connection   conn   = null;
   	Statement 	 stmt   = null;
	//
	int	cnt = 0;
	try {
       	// Broker からDB Connection を得る
       	conn 		 = Broker.getConnection();
		stmt 		 = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query_read_KadaiInfo(szDB,v));
		while(rs.next()){
			//
			v.add(rs.getString("shubetsu"));
			v.add(rs.getString("saiten_flag"));
			v.add(rs.getString("date_str"));
			v.add(rs.getString("subject"));
			v.add(rs.getString("points"));
			//
			++cnt;
			break;
		}
		//
	}catch(Exception e){
		System.out.println("#### 失敗:read_KadaiInfo() " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
       	Broker.freeConnection(conn);
   	}
    if(LOG.fa) LOG.outVector(v,"class Database #read_KadaiInfo() : 検索結果 " + cnt + " 件" );
	return cnt;
}
//
// 課題提出履歴データベースの読み出しクエリーを作る
String query_read_KadaiInfo(String szDB,Vector v){
		if(LOG.fa) LOG.println("class Database #query_read_KadaiInfo() : 課題提出履歴データベースのクエリーを作る　の先頭です");
	//
	String stNumber		= (String) v.get(KadaiInfo.STNUMBER);
	String te_aplec_key	= (String) v.get(KadaiInfo.TE_APLEC_KEY);
	String kadai_key	= (String) v.get(KadaiInfo.KADAI_KEY);
	String Q 			= " SELECT * FROM  kadai_" + szDB + " "  
	                             + " WHERE stNumber 	= " + m1 + stNumber 	+ m1 
								 + " AND   te_aplec_key = " + m1 + te_aplec_key + m1
								 + " AND   kadai_key    = " + m1 + kadai_key    + m1;
	//
	if(LOG.fa) LOG.println("class Database #query_read_KadaiInfo() : 課題提出履歴データベースの読み出しクエリー = " + Q);
	return Q;
}
//
//  KadaiInfoデータベースのレコードを追加する
//
public int insert_KadaiInfo( String szDB,Vector v ){
	if(LOG.fa) LOG.outVector(v,"class Database #insert_KadaiInfo() :  KadaiInfoデータベースのレコードを追加する の先頭です");
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int cnt = 0;
	//
	try {
      	conn 	= Broker.getConnection();
		stmt 	= conn.createStatement();
		cnt  	= stmt.executeUpdate(query_insert_KadaiInfo(szDB,v));
		//
	}catch(Exception e){
		System.out.println("### 失敗:insert_KadaiInfo() " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);
   	}
		if(LOG.fa) LOG.println("class Database #insert_KadaiInfo() : KadaiInfo データベースのレコード追加件数 = " + cnt);
	return cnt; // 件数（普通は１）
}	
// KadaiInfoデータベースのレコード追加のクエリーを作る
String 	query_insert_KadaiInfo(String szDB,Vector v){
	//
	String stNumber		= (String)(v.get(KadaiInfo.STNUMBER));
	String te_aplec_key	= (String)(v.get(KadaiInfo.TE_APLEC_KEY));
	String kadai_key	= (String)(v.get(KadaiInfo.KADAI_KEY));
	//
	String shubetsu		= (String)(v.get(KadaiInfo.SHUBETSU));
	String saiten_flag	= (String)(v.get(KadaiInfo.SAITEN_FLAG));
	String date_str		= (String)(v.get(KadaiInfo.DATE_STR));
	String subject		= (String)(v.get(KadaiInfo.SUBJECT));
	String points		= (String)(v.get(KadaiInfo.POINTS));
	//
	String Q = " INSERT INTO kadai_" + szDB + "  VALUES (" 
	            + m1 + stNumber  	+ m2 
				+ m1 + te_aplec_key + m2 
				+ m1 + kadai_key  	+ m2
				+ m1 + shubetsu  	+ m2
				+ m1 + saiten_flag  + m2
				+ m1 + date_str  	+ m2
				+ m1 + subject  	+ m2
				+ m1 + points  		+ m1
				+ ")";
	//
	if(LOG.fa) LOG.println("class Database #query_insert_KadaiInfo() : KadaiInfoデータベースのレコード追加のクエリー = " + Q);
	return Q;
}
//
// 課題提出履歴データベースのレコードを更新する
public int update_KadaiInfo(String szDB, Vector v ){
	if(LOG.fa) LOG.outVector(v,"class Database #update_KadaiInfo() : 課題提出履歴データベースのレコードを更新する の先頭です");
	//
	Connection   conn  = null;
   	Statement 	 stmt  = null;
	int  		 cnt   = 0;
	//
	try {
      	conn 	= Broker.getConnection(); /** Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		cnt     = stmt.executeUpdate(queary_update_KadaiInfo(szDB,v));
	}catch(Exception e){
		System.out.println("#exwk ### 失敗:queary_update_KadaiInfo() " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/** connection を Broker に返す */
   	}
		if(LOG.fa) LOG.println("class Database #queary_update_KadaiInfo() : 課題提出履歴データベースのレコード更新件数 = " + cnt);
	return cnt;
}
//
// KadaiInfo データベースの更新用クエリの作成
String queary_update_KadaiInfo(String szDB,Vector v){
	//
	String stNumber		= (String)(v.get(KadaiInfo.STNUMBER));
	String te_aplec_key	= (String)(v.get(KadaiInfo.TE_APLEC_KEY));
	String kadai_key	= (String)(v.get(KadaiInfo.KADAI_KEY));
	//
	String shubetsu		= (String)(v.get(KadaiInfo.SHUBETSU));
	String saiten_flag	= (String)(v.get(KadaiInfo.SAITEN_FLAG));
	String date_str		= (String)(v.get(KadaiInfo.DATE_STR));
	String subject		= (String)(v.get(KadaiInfo.SUBJECT));
	String points		= (String)(v.get(KadaiInfo.POINTS));
	//
	String Q = " UPDATE  kadai_" + szDB + "  set  " 
				//
				+ "shubetsu 	= "  + m1 + shubetsu  	+ m2
				+ "saiten_flag 	= "  + m1 + saiten_flag + m2
				+ "date_str 	= "  + m1 + date_str  	+ m2
				+ "subject 		= "  + m1 + subject  	+ m2
				+ "points 		= "  + m1 + points  	+ m1
				//
				+ "  WHERE stNumber   = " + m1 + stNumber     + m1
				+ "  AND te_aplec_key = " + m1 + te_aplec_key + m1
				+ "  AND kadai_key    = " + m1 + kadai_key    + m1;
				//
	if(LOG.fa) LOG.println("class Database #queary_update_KadaiInfo() : ＦＡＱデータベースの更新用クエリ = " + Q);
	return Q;
}
//
// 特定の学生の特定の講義の課題提出履歴全件を削除する
public int delete_KadaiInfo( String szDB,Vector v ){
	if(LOG.fa) LOG.outVector(v,"class Database #delete_KadaiInfo() :  課題提出履歴データベースのレコードを削除する の先頭です");
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int 		  cnt 		= 0;
	//
	try {
		conn 	= Broker.getConnection(); 
		stmt 	= conn.createStatement();
		cnt		= stmt.executeUpdate(queary_delete_KadaiInfo(szDB,v)); // ここでdeleteする
		//
	}catch(Exception e){
		System.out.println("### 失敗:delete_KadaiInfo()" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);
   	}
		if(LOG.fa) LOG.println("class Database #delete_KadaiInfo() : 課題提出履歴データベースのレコード削除件数 = " + cnt);
	return cnt; // 件数（普通は１）
}
//
// 特定の学生の特定の講義の課題提出履歴全件を削除するクエリの作成
String queary_delete_KadaiInfo(String szDB,Vector v){
	//
	String stNumber		= (String)(v.get(KadaiInfo.STNUMBER));
	String te_aplec_key	= (String)(v.get(KadaiInfo.TE_APLEC_KEY));
	String Q 			=  " DELETE  from  kadai_" + szDB + " " 	
				+ "  WHERE stNumber   = " + m1 + stNumber     + m1
				+ "  AND te_aplec_key = " + m1 + te_aplec_key + m1;
	//
	if(LOG.fa) LOG.println("class Database #queary_delete_KadaiInfo() : 課題提出履歴データベースの削除用クエリ = " + Q);
	return Q;
}
//
// 特定の学生の特定の講義の特定の課題提出履を削除する
public int delete_A_KadaiInfo( String szDB,Vector v ){
	if(LOG.fa) LOG.outVector(v,"class Database #delete_KadaiInfo() :  課題提出履歴データベースのレコードを削除する の先頭です");
	//
	Connection   conn  		= null;
	Statement 	 stmt  		= null;
	int 		  cnt 		= 0;
	//
	try {
		conn 	= Broker.getConnection(); 
		stmt 	= conn.createStatement();
		cnt		= stmt.executeUpdate(queary_delete_A_KadaiInfo(szDB,v)); // ここでdeleteする
		//
	}catch(Exception e){
		System.out.println("### 失敗:delete_KadaiInfo()" + e);
	} finally {
		try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
		//
		Broker.freeConnection(conn);
	}
	if(LOG.fa) LOG.println("class Database #delete_KadaiInfo() : 課題提出履歴データベースのレコード削除件数 = " + cnt);
	return cnt; // 件数（普通は１）
}
//
// 特定の学生の特定の講義の特定の課題提出履を削除するクエリの作成
String queary_delete_A_KadaiInfo(String szDB,Vector v){
	//
	String stNumber		= (String)(v.get(KadaiInfo.STNUMBER));
	String te_aplec_key	= (String)(v.get(KadaiInfo.TE_APLEC_KEY));
	String kadai_key	= (String)(v.get(KadaiInfo.KADAI_KEY));
	
	String Q 			=  " DELETE  from  kadai_" + szDB + " " 	
				+ "  WHERE stNumber   = " + m1 + stNumber     + m1
				+ "  AND te_aplec_key = " + m1 + te_aplec_key + m1
				+ "  AND kadai_key    = " + m1 + kadai_key    + m1;
	//
	if(LOG.fa) LOG.println("class Database #queary_delete_KadaiInfo() : 課題提出履歴データベースの削除用クエリ = " + Q);
	return Q;
}

//
//  ★学生の課題提出履歴データデータベースを作成するクエリ 2004.3.
//
/*
create table kadai_000000 (
	stNumber           VARCHAR(12)   NOT NULL,
	te_aplec_key       CHAR(12)      NOT NULL,
	kadai_key          CHAR(6)       NOT NULL,
	shubetsu           CHAR(1)       NOT NULL,
	saiten_flag        CHAR(1)       DEFAULT '0',
	date_str           text          DEFAULT ''
	subject            text          DEFAULT '',
	points             VARCHAR(3)    DEFAULT ''
);
create index kadai_000000_idx on kadai_000000 (stNumber,te_aplec_key);	
*/

	//
// ★各大学向けの学生の課題提出履歴データベースを作成する
public int create_KadaiInfo(String tbl){
	if(LOG.fa) LOG.println("class Database #create_KadaiInfo() : 各大学向けの学生の課題提出履歴データベースを作成する の先頭です");
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = -1;
	//
	String QUERY = create_KadaiInfo_Query(tbl);
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY);
		//
	}catch(Exception e){
		System.out.println("### 失敗:create_KadaiInfo(String tbl) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // （０が返る）
}
// 各大学向けの学生の課題提出履歴データベースを作成するクエリ
String create_KadaiInfo_Query(String tbl){
	if(LOG.fa) LOG.println("class Dataase #create_KadaiInfo_Query() : 各大学向けの学生の課題提出履歴データベースを作成するクエリ の先頭です");
	if(LOG.fa) LOG.println("          tbl = " + tbl);
	//
	String Q = "CREATE TABLE " +  "kadai_" + tbl + " ( " + 
						" stNumber        VARCHAR(12)        NOT  NULL,   "
					+	" te_aplec_key    VARCHAR(16)        NOT  NULL,   "
					+	" kadai_key          CHAR(6)         NOT  NULL,   "
					+	" shubetsu           CHAR(1)         NOT  NULL,   "
					+	" saiten_flag        CHAR(1)         DEFAULT '0', "
					+   " date_str           TEXT            DEFAULT '',  "
					+   " subject            TEXT            DEFAULT '',  "
					+   " points          VARCHAR(3)         DEFAULT ''   "
	                +   ")" ;
	return Q;
}
//
//
// ★各大学向けの学生の課題提出履歴データベースのインデックスを作成する
public int create_KadaiInfo_Idx(String tbl){
	if(LOG.fa) LOG.println("class Database #create_KadaiInfo_Idx() : 各大学向けの学生の課題提出履歴データベースのインデックスを作成する の先頭です");
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = -1;
	//
	String QUERY = create_KadaiInfo_Idx_Query(tbl);
	try {
      	conn 	= Broker.getConnection();
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY);
		//
	}catch(Exception e){
		System.out.println("### 失敗:createTable(String tbl) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);
   	}
	return n; // （０が返る）
}
// ★学生の課題提出履歴データデータベースのインデックス作成
public String create_KadaiInfo_Idx_Query(String tbl){
	if(LOG.fa) LOG.println("class Dataase #create_KadaiInfo_Idx_Query() : 学生の課題提出履歴データデータベースのインデックス作成 の先頭です");
	//
	String Q = "CREATE INDEX kadai_" + tbl + "_idx  ON  kadai_" + tbl + " (stNumber,te_aplec_key)" ;
	return Q;
}

// ★フォーラムデータベースの作成
public int create_BbsForum(String tbl){
	if(LOG.fa) LOG.println("■ Database #create_BbsForum() : フォーラム関連データベースの作成");
	//
	Connection   conn  		= null;
	Statement 	 stmt  		= null;
	int n = -1;
	//
	String QUERY = create_BbsForum_Query(tbl);
	if(LOG.fa) LOG.println("Query=" + QUERY);
	try {
		conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY);
		//
	}catch(Exception e){
		System.out.println("### 失敗:create_BbsForum(String tbl) " + e);
		if(LOG.fa) LOG.println("### 失敗:create_BbsForum(String tbl) " + e);
	} finally {
		try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
		//
		Broker.freeConnection(conn);/* connection を Broker に返す */
	}
	return n; // （０が返る）
}
String create_BbsForum_Query(String tbl){
	if(LOG.fa) LOG.println("■ Database  #create_BbsForum_Query()");
	if(LOG.fa) LOG.println("          tbl = " + tbl);
	//
	String Q = "CREATE TABLE " +  "bbsforum_" + tbl + " ( " + 

			"ownerkey   VARCHAR(12), "	+
			"forumkey   CHAR(4), "		+
			"alive      CHAR(1), "		+
			"gpflag     CHAR(1), "		+
			"rtflag     CHAR(1), "		+
			"hdflag     CHAR(1), "		+
			"date       VARCHAR(30), "	+
			"subject    VARCHAR(100), "	+
			"content    TEXT, "			+
			"attachment TEXT, "			+
			"relation   TEXT, "			+
			"watch      TEXT"           +
			");   "						+
			"create index bbsforum_" + tbl + "_idx on bbsforum_" + tbl +"  (ownerkey, forumkey);";				
					
	return Q;
}

// ★スレッドデータベースの作成
public int create_BbsThread(String tbl){
	if(LOG.fa) LOG.println("■ Database #create_BbsThread() : フォーラム関連データベースの作成");
	//
	Connection   conn  		= null;
	Statement 	 stmt  		= null;
	int n = -1;
	//
	String QUERY = create_BbsThread_Query(tbl);
	try {
		conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY);
		//
	}catch(Exception e){
		System.out.println("### 失敗:create_BbsThread(String tbl) " + e);
	} finally {
		try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
		//
		Broker.freeConnection(conn);/* connection を Broker に返す */
	}
	return n; // （０が返る）
}
String create_BbsThread_Query(String tbl){
	if(LOG.fa) LOG.println("■ Dataase #create_BbsThread_Query()");
	if(LOG.fa) LOG.println("          tbl = " + tbl);
	//
	String Q = "CREATE TABLE " +  "bbsthread_" + tbl + " ( " 

	+ "owner_forumkey   VARCHAR(17),"
	+ "threadkey        CHAR(4),"
	+ "userid           VARCHAR(12),"
	+ "date             VARCHAR(30),"
	+ "rating           VARCHAR(6),"
	+ "subject          VARCHAR(100),"
	+ "content          TEXT,"
	+ "attachment       TEXT,"
	+ "views            VARCHAR(7)"  
	+ ");   "
	+ "create index bbsthread_" + tbl + "_idx on bbsthread_" + tbl +"  (owner_forumkey, threadkey);";				
					
	return Q;
}

// ★ポストデータベースの作成
public int create_BbsPost(String tbl){
	if(LOG.fa) LOG.println("■ Database #create_BbsPost() : フォーラム関連データベースの作成");
	//
	Connection   conn  		= null;
	Statement 	 stmt  		= null;
	int n = -1;
	//
	String QUERY = create_BbsPost_Query(tbl);
	try {
		conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY);
		//
	}catch(Exception e){
		System.out.println("### 失敗:create_BbsPost(String tbl) " + e);
	} finally {
		try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
		//
		Broker.freeConnection(conn);/* connection を Broker に返す */
	}
	return n; // （０が返る）
}
String create_BbsPost_Query(String tbl){
	if(LOG.fa) LOG.println("■ Dataase #create_BbsPost_Query()");
	if(LOG.fa) LOG.println("          tbl = " + tbl);
	//
	String Q = "CREATE TABLE " +  "bbspost_" + tbl + " ( " 

	+ "bbskey          VARCHAR(22),"
	+ "postkey         CHAR(5),"
	+ "userid          VARCHAR(12),"
	+ "date            VARCHAR(30),"
	+ "rating          VARCHAR(6),"
	+ "subject         VARCHAR(100),"
	+ "content         TEXT,"
	+ "attachment      TEXT,"
	+ "dispname        VARCHAR(30),"
	+ "link            TEXT"
	+ ");   "
	+ "create index bbspost_" + tbl + "_idx on bbspost_" + tbl +"  (bbskey, postkey);";				
					
	return Q;
}


// ★ユーザー情報データの作成
public int create_bbsInfo(String tbl){
	if(LOG.fa) LOG.println("■ Database #create_bbsInfo() : フォーラム関連データベースの作成");
	//
	Connection   conn  		= null;
	Statement 	 stmt  		= null;
	int n = -1;
	//
	String QUERY = create_bbsInfo_Query(tbl);
	try {
		conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY);
		//
	}catch(Exception e){
		System.out.println("### 失敗:create_bbsInfo(String tbl) " + e);
	} finally {
		try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
		//
		Broker.freeConnection(conn);/* connection を Broker に返す */
	}
	return n; // （０が返る）
}
String create_bbsInfo_Query(String tbl){
	if(LOG.fa) LOG.println("■ Dataase #create_bbsInfo_Query()");
	if(LOG.fa) LOG.println("          tbl = " + tbl);
	//
	String Q = "CREATE TABLE " +  "bbsInfo_" + tbl + " ( " 

	+ "userid            VARCHAR(12)  PRIMARY KEY,"
	+ "passwd            VARCHAR(20)  ,"
	+ "division          CHAR(1)      DEFAULT '4',"
	+ "name              VARCHAR(30),"
	+ "mail				 VARCHAR(50),"
	+ "handle            VARCHAR(30),"
	+ "iconfile          VARCHAR(30),"
	+ "signature         TEXT,"
	+ "formatStyle       TEXT,"
	+ "editor            CHAR(2)"
	+ ");";
					
	return Q;
}

// ★ユーザー閲覧ログの作成
public int create_bbslog(String tbl){
	if(LOG.fa) LOG.println("■ Database #create_bbslog() : フォーラム関連データベースの作成");
	//
	Connection   conn  		= null;
	Statement 	 stmt  		= null;
	int n = -1;
	//
	String QUERY = create_bbslog_Query(tbl);
	try {
		conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY);
		//
	}catch(Exception e){
		System.out.println("### 失敗:create_bbslog(String tbl) " + e);
	} finally {
		try{
		    if(stmt != null)  {
		        stmt.close();
		    }
		}catch(SQLException e1){

		};
		//
		Broker.freeConnection(conn);/* connection を Broker に返す */
	}
	return n; // （０が返る）
}
String create_bbslog_Query(String tbl){
	if(LOG.fa) LOG.println("■ Dataase #create_bbslog_Query()");
	if(LOG.fa) LOG.println("          tbl = " + tbl);
	//
	String Q = "CREATE TABLE " +  "bbslog_" + tbl + " ( " 

	+ "userid          VARCHAR(12),"
	+ "owner_forumkey  VARCHAR(17),"
	+ "posts           CHAR(4),"
	+ "lastvisit       VARCHAR(30)"
	+ ");"
	+ "create index bbslog_" + tbl +"_idx on bbslog_" + tbl + " (userid, owner_forumkey);";						
	return Q;
}

// ★キー情報データベースの作成
public int create_jbbskes(String tbl){
	if(LOG.fa) LOG.println("■ Database #create_bbsInfo() : フォーラム関連データベースの作成");
	//
	Connection   conn  		= null;
	Statement 	 stmt  		= null;
	int n = -1;
	//
	String QUERY = create_jbbskes_Query(tbl);
	try {
		conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY);
		//
	}catch(Exception e){
		System.out.println("### 失敗:create_jbbskes(String tbl) " + e);
	} finally {
		try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
		//
		Broker.freeConnection(conn);/* connection を Broker に返す */
	}
	return n; // （０が返る）
}
String create_jbbskes_Query(String tbl){
	if(LOG.fa) LOG.println("■ Dataase #create_jbbskes_Query()");
	if(LOG.fa) LOG.println("          tbl = " + tbl);
	//
	String q1 = "create table forumkey_" + tbl + " ("
				+ "ownerkey         VARCHAR(12)    PRIMARY KEY,"
				+ "forumkey         CHAR(4)"
				+ ");";

	String q2 = "create table threadkey_" + tbl + " ("
				+ "owner_forumkey   VARCHAR(17)    PRIMARY KEY,"
				+ "threadkey        CHAR(4)"
				+ ");";
			
	String q3 = "create table postkey_" + tbl + " ("
				+ "bbskey           VARCHAR(22)   PRIMARY KEY,"
				+ "postkey          CHAR(5)"
				+ ");";				

	String Q = q1 + q2 + q3;
					
	return Q;
}




///////////////////////////////////////////////////////////////

//  学生用ノート関係

///////////////////////////////////////////////////////////////
//
//  全件を読み出す
//
//
// 全ＮＯＴＥを読み出す
public int read_NOTE_all(String szDB,String stNumber,String te_aplec_key,Vector rec){
	if(LOG.fa) LOG.println("class Database #read_NOTE_all() : 全ＮＯＴＥを読み出す の先頭です");
	if(LOG.fa) LOG.println("          stNumber     = " + stNumber);
	if(LOG.fa) LOG.println("          te_aplec_key = " + te_aplec_key);
	//
	Connection   conn  = null;
   	Statement 	 stmt  = null;
   	int rowCount = 0;
	try {
		String cmd 	= "SELECT * FROM  note_" + szDB + " " 
				+ "  WHERE stNumber     = " + m1 + stNumber     + m1
				+ "  AND   te_aplec_key = " + m1 + te_aplec_key + m1;
		
		if(LOG.fa) LOG.println("read_NOTE_all() : SQL = " + cmd);
		//
      	conn 	= Broker.getConnection(); 
		stmt 	= conn.createStatement();
		ResultSet rs = stmt.executeQuery(cmd);
		while(rs.next()){
			Vector v = new Vector(20,10);
			v.add(rs.getString("stNumber"));
			v.add(rs.getString("te_aplec_key"));
			v.add(rs.getString("sect_key"));
			v.add(rs.getString("note"));
			rec.add(v);
			rowCount++;
			if(LOG.fa) LOG.println(rowCount);
		}
	}catch(Exception e){
		System.out.println("### 失敗: read_NOTE_all()" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);
   	}
	if(LOG.fa) LOG.println( "class Database #read_NOTE_all() : 全ＮＯＴＥを読み出す の検索結果＝" + String.valueOf(rowCount) );
	return rowCount;
}
//
//　特定のレコードを読み出す
//
public int read_NOTE(String szDB,Vector v){
	if(LOG.fa) LOG.outVector(v,"class Database #read_NOTE() :  質問メールのレコードを読み出す の先頭です");
	//
	Connection   conn   = null;
   	Statement 	 stmt   = null;
	//
	int	cnt = 0;
	try {
       	// Broker からDB Connection を得る
       	conn 		 = Broker.getConnection();
		stmt 		 = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query_read_NOTE(szDB,v));
		while(rs.next()){
			//
			v.add(rs.getString("note"));
			//
			++cnt;
			break;
		}
		//
	}catch(Exception e){
		System.out.println("#### 失敗:read_NOTE() " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
       	Broker.freeConnection(conn);
   	}
    if(LOG.fa) LOG.outVector(v,"class Database #read_NOTE() : 検索結果 " + cnt + " 件" );
	return cnt;
}
//
// ＮＯＴＥデータベースの読み出しクエリーを作る
String query_read_NOTE(String szDB,Vector v){
		if(LOG.fa) LOG.println("class Database #query_read_NOTE() : ＮＯＴＥデータベースのクエリーを作る　の先頭です");
	//
	String stNumber		= (String) v.get(NOTE.STNUMBER);
	String te_aplec_key	= (String) v.get(NOTE.TE_APLEC_KEY);
	String sect_key		= (String) v.get(NOTE.SECT_KEY);
	String Q 			= " SELECT * FROM  note_" + szDB + " "  
	                             + " WHERE stNumber 	= " + m1 + stNumber 	+ m1 
								 + " AND   te_aplec_key = " + m1 + te_aplec_key + m1
								 + " AND   sect_key     = " + m1 + sect_key     + m1;
	//
	if(LOG.fa) LOG.println("class Database #query_read_NOTE() : ＮＯＴＥデータベースの読み出しクエリー = " + Q);
	return Q;
}
//
//  NOTEデータベースのレコードを追加する
//
public int insert_NOTE( String szDB,Vector v ){
	if(LOG.fa) LOG.outVector(v,"class Database #insert_NOTE() :  NOTEデータベースのレコードを追加する の先頭です");
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int cnt = 0;
	//
	try {
      	conn 	= Broker.getConnection();
		stmt 	= conn.createStatement();
		cnt  	= stmt.executeUpdate(query_insert_NOTE(szDB,v));
		//
	}catch(Exception e){
		System.out.println("### 失敗:insert_NOTE() " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);
   	}
		if(LOG.fa) LOG.println("class Database #insert_NOTE() : NOTE データベースのレコード追加件数 = " + cnt);
	return cnt; // 件数（普通は１）
}	
// NOTEデータベースのレコード追加のクエリーを作る
String 	query_insert_NOTE(String szDB,Vector v){
	//
	String stNumber		= (String)(v.get(NOTE.STNUMBER));
	String te_aplec_key	= (String)(v.get(NOTE.TE_APLEC_KEY));
	String sect_key		= (String)(v.get(NOTE.SECT_KEY));
	String note			= (String)(v.get(NOTE.NOTES));
	//
	String Q = " INSERT INTO note_" + szDB + "  VALUES (" 
	            + m1 + stNumber  	+ m2 
				+ m1 + te_aplec_key + m2 
				+ m1 + sect_key  	+ m2
				+ m1 + note  		+ m1
				+ ")";
	//
	if(LOG.fa) LOG.println("class Database #query_insert_NOTE() : NOTEデータベースのレコード追加のクエリー = " + Q);
	return Q;
}
//
// ＮＯＴＥデータベースのレコードを更新する
public int update_NOTE(String szDB, Vector v ){
	if(LOG.fa) LOG.outVector(v,"class Database #update_NOTE() : ＮＯＴＥデータベースのレコードを更新する の先頭です");
	//
	Connection   conn  = null;
   	Statement 	 stmt  = null;
	int  		 cnt   = 0;
	//
	try {
      	conn 	= Broker.getConnection(); /** Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		cnt     = stmt.executeUpdate(queary_update_NOTE(szDB,v));
	}catch(Exception e){
		System.out.println("#exwk ### 失敗:queary_update_NOTE() " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/** connection を Broker に返す */
   	}
		if(LOG.fa) LOG.println("class Database #insert_NOTE() : ＮＯＴＥデータベースのレコード更新件数 = " + cnt);
	return cnt;
}
//
// NOTE データベースの更新用クエリの作成
String queary_update_NOTE(String szDB,Vector v){
	//
	String stNumber		= (String)(v.get(NOTE.STNUMBER));
	String te_aplec_key	= (String)(v.get(NOTE.TE_APLEC_KEY));
	String sect_key		= (String)(v.get(NOTE.SECT_KEY));
	String note			= (String)(v.get(NOTE.NOTES));
	//
	String Q = " UPDATE  note_" + szDB + "  set  " 
				//
				+ "note = "  + m1 + note  + m1
				+ "  WHERE stNumber   = " + m1 + stNumber     + m1
				+ "  AND te_aplec_key = " + m1 + te_aplec_key + m1
				+ "  AND sect_key     = " + m1 + sect_key     + m1;
				//
	if(LOG.fa) LOG.println("class Database #queary_update_NOTE() : ＦＡＱデータベースの更新用クエリ = " + Q);
	return Q;
}
//
// ある講義のＮＯＴＥ全件を削除する
public int delete_NOTE( String szDB,Vector v ){
	if(LOG.fa) LOG.outVector(v,"class Database #delete_NOTE() :  ＮＯＴＥデータベースのレコードを削除する の先頭です");
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int 		  cnt 		= 0;
	//
	try {
		conn 	= Broker.getConnection(); 
		stmt 	= conn.createStatement();
		cnt		= stmt.executeUpdate(queary_delete_NOTE(szDB,v)); // ここでdeleteする
		//
	}catch(Exception e){
		System.out.println("### 失敗:delete_NOTE()" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);
   	}
		if(LOG.fa) LOG.println("class Database #delete_NOTE() : ＮＯＴＥデータベースのレコード削除件数 = " + cnt);
	return cnt; // 件数（普通は１）
}
//
// ある講義のＮＯＴＥ全件を削除するクエリの作成
String queary_delete_NOTE(String szDB,Vector v){
	//
	String stNumber		= (String)(v.get(NOTE.STNUMBER));
	String te_aplec_key	= (String)(v.get(NOTE.TE_APLEC_KEY));
	String Q 			=  " DELETE  from  note_" + szDB + " " 	
				+ "  WHERE stNumber   = " + m1 + stNumber     + m1
				+ "  AND te_aplec_key = " + m1 + te_aplec_key + m1;
	//
	if(LOG.fa) LOG.println("class Database #queary_delete_NOTE() : ＮＯＴＥデータベースの削除用クエリ = " + Q);
	return Q;
}
//
// ★各大学向けの学生のノートデータベースを作成する
public int createNote(String tbl){
	if(LOG.fa) LOG.println("class Database #createNote() : 各大学向けの学生のノートデータベースを作成する の先頭です");
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = -1;
	//
	String QUERY = createNoteQuery1(tbl);
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY);
		//
	}catch(Exception e){
		System.out.println("### 失敗:createTable(String tbl) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // （０が返る）
}

//
// ★各大学向けの学生のノートデータベースのインデックスを作成する
public int create_Note_Idx(String tbl){
	if(LOG.fa) LOG.println("class Database #create_Note_Idx() : 各大学向けの学生のノートデータベースのインデックスを作成する の先頭です");
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = -1;
	//
	String QUERY = create_Note_Idx_Query(tbl);
	try {
      	conn 	= Broker.getConnection();
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY);
		//
	}catch(Exception e){
		System.out.println("### 失敗:create_Note_Idx(String tbl) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);
   	}
	return n; // （０が返る）
}


//
//  ★学生のノートデータデータベースを作成するクエリ 2003.8.
//
/*
  create table note_kwassui (
     stNumber     	  VARCHAR(12)  NOT NULL,
     te_aplec_key     CHAR(12)     NOT NULL,
     sect_key         CHAR(5)      NOT NULL,
     note             text         DEFAULT ''
  );	
*/


String createNoteQuery1(String tbl){
	if(LOG.fa) LOG.println("class Dataase #createNoteQuery_1() : 学生のノートデータデータベースを作成するクエリ の先頭です");
	if(LOG.fa) LOG.println("          tbl = " + tbl);
	//
	String Q = "CREATE TABLE " +  "note_" + tbl + " ( " + 
						" stNumber        VARCHAR(12)        NOT  NULL,  "
					+	" te_aplec_key    VARCHAR(16)        NOT  NULL,  "
					+	" sect_key           CHAR(5)         NOT  NULL,  "
					+   " note               TEXT            DEFAULT '' "
	                +   ")" ;
	return Q;
}
// ★学生のノートデータデータベースのインデックス作成
public String create_Note_Idx_Query(String tbl){
	if(LOG.fa) LOG.println("class Dataase #create_Note_Idx_Query() : 学生のノートデータデータベースのインデックス作成 の先頭です");
	//
	String Q = "CREATE INDEX note_" + tbl + "_idx ON note_" + tbl + "  (stNumber,te_aplec_key)" ;
	return Q;
}

//////////////////////////////////////////////////////////////////////////
//
//■ ＦＡＱデータベース
//
///////////////////////////////////////////////////////////////////////////
//
/*
■ＦＡＱデータベース
	
	定義講義単位にまとめ、受けつけ番号順に並べる
	
	seq_no ---- 受付番号 ６桁 KeyGen で発生
	　　　　　　個人単位（講義単位ではない）
	
	内容はfaq_mailの変更に伴い連動して追加・削除・訂正される

	create table FAQ (
   		te_lec_key       CHAR(12)       NOT NULL,
   		seq_no           VARCHAR(6)     NOT NULL,
   		faq_title        VARCHAR(200)   DEFAULT '-',
   		faq_body         text           DEFAULT '-'
	);
	create index FAQ_idx on  FAQ(te_lec_key,seq_no);

*/
//
//  (1) ＦＡＱデータベースのレコードを読み出す //
//
//
//  全件を読み出す
//
//
// 全ＦＡＱを読み出す
public int read_FAQ_all(String te_lec_key,Vector rec){

	Connection   conn  = null;
   	Statement 	 stmt  = null;
   	int rowCount = 0;
	try {
		String cmd 	= "SELECT * FROM FAQ  WHERE te_lec_key = " + m1 + te_lec_key + m1;
		if(LOG.fa) LOG.println("read_FAQ_all() : SQL = " + cmd);
		//
      	conn 	= Broker.getConnection(); 
		stmt 	= conn.createStatement();
		ResultSet rs = stmt.executeQuery(cmd);
		while(rs.next()){
			Vector v = new Vector(20,10);
			v.add(rs.getString("te_lec_key"));
			v.add(rs.getString("seq_no"));
			v.add(rs.getString("faq_title"));
			v.add(rs.getString("faq_body"));
			rec.add(v);
			rowCount++;
			if(LOG.fa) LOG.println(rowCount);
		}
	}catch(Exception e){
		System.out.println("### 失敗: read_FAQ_all()" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);
   	}
	if(LOG.fa) LOG.println( "class Database #read_FAQ_all() : 全ＦＡＱを読み出す の検索結果＝" + String.valueOf(rowCount) );
	return rowCount;
}
//
//　特定のレコードを読み出す
//
public int read_FAQ(Vector v){
	if(LOG.fa) LOG.outVector(v,"class Database #read_FAQ() :  質問メールのレコードを読み出す の先頭です");
	//
	Connection   conn   = null;
   	Statement 	 stmt   = null;
	//
	int	cnt = 0;
	try {
       	// Broker からDB Connection を得る
       	conn 		 = Broker.getConnection();
		stmt 		 = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query_read_FAQ(v));
		while(rs.next()){
			//
			v.add(rs.getString("faq_title"));
	    	v.add(rs.getString("faq_body"));
			//
			++cnt;
			break;
		}
		//
	}catch(Exception e){
		System.out.println("#### 失敗:read_FAQ() " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
       	Broker.freeConnection(conn);
   	}
    if(LOG.fa) LOG.outVector(v,"class Database #read_FAQ() : 検索結果 " + cnt + " 件" );
	return cnt;
}
//
// ＦＡＱデータベースの読み出しクエリーを作る
String query_read_FAQ(Vector v){
		if(LOG.fa) LOG.println("class Database #query_read_FAQ() : ＦＡＱデータベースのクエリーを作る　の先頭です");
	//
	String te_lec_key	= (String) v.get(FAQ.TE_LEC_KEY);
	String seq_no		= (String) v.get(FAQ.SEQ_NO);
	String Q 			= " SELECT * FROM  FAQ  WHERE te_lec_key = " + m1 + te_lec_key + m1 + "  AND  seq_no = " + m1 + seq_no + m1;	
	//
	if(LOG.fa) LOG.println("class Database #query_read_FAQ() : ＦＡＱデータベースの読み出しクエリー = " + Q);
	return Q;
}
//
//  (2) ＦＡＱデータベースのレコードを追加する  //
//
public int insert_FAQ( Vector v ){
	if(LOG.fa) LOG.outVector(v,"class Database #insert_FAQ() :  ＦＡＱデータベースのレコードを追加する の先頭です");
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int cnt = 0;
	//
	try {
      	conn 	= Broker.getConnection();
		stmt 	= conn.createStatement();
		cnt  	= stmt.executeUpdate(query_insert_FAQ(v));
		//
	}catch(Exception e){
		System.out.println("### 失敗:insert_FAQ() " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);
   	}
		if(LOG.fa) LOG.println("class Database #insert_FAQ() : ＦＡＱデータベースのレコード追加件数 = " + cnt);
	return cnt; // 件数（普通は１）
}	
// ＦＡＱデータベースのレコード追加のクエリーを作る
String 	query_insert_FAQ(Vector v){
	//
	String te_lec_key	= (String)(v.get(FAQ.TE_LEC_KEY));
	String seq_no		= (String)(v.get(FAQ.SEQ_NO));
	String faq_title	= (String)(v.get(FAQ.FAQ_TITLE));
	String faq_body		= (String)(v.get(FAQ.FAQ_BODY));
	//
	String Q = " INSERT INTO FAQ  VALUES (" 
	            + m1 + te_lec_key  	+ m2 
				+ m1 + seq_no  		+ m2
				+ m1 + faq_title  	+ m2
				+ m1 + faq_body 	+ m1
				+ ")";
	//
	if(LOG.fa) LOG.println("class Database #query_insert_FAQ() : ＦＡＱデータベースのレコード追加のクエリー = " + Q);
	return Q;
}
//
// ＦＡＱデータベースのレコードを更新する
public int update_FAQ( Vector v ){
	Connection   conn  = null;
   	Statement 	 stmt  = null;
	int  		 cnt   = 0;
	//
	try {
      	conn 	= Broker.getConnection(); /** Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		cnt     = stmt.executeUpdate(queary_update_FAQ(v));
	}catch(Exception e){
		System.out.println("#exwk ### 失敗:queary_update_FAQ() " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/** connection を Broker に返す */
   	}
		if(LOG.fa) LOG.println("class Database #insert_FAQ() : ＦＡＱデータベースのレコード更新件数 = " + cnt);
	return cnt;
}
//
// ＦＡＱデータベースの更新用クエリの作成
String queary_update_FAQ(Vector v){
	//
	String te_lec_key	= (String)(v.get(FAQ.TE_LEC_KEY));
	String seq_no		= (String)(v.get(FAQ.SEQ_NO));
	String faq_title	= (String)(v.get(FAQ.FAQ_TITLE));
	String faq_body		= (String)(v.get(FAQ.FAQ_BODY));
	//
	String Q = " UPDATE  FAQ  set  " 
				//
				+ "faq_title = "  + m1 + faq_title  + m2
				+ "faq_body  = "  + m1 + faq_body  + m1
				+ " WHERE te_lec_key = " + m1 + te_lec_key + m1 + " AND seq_no = " + m1 + seq_no + m1;
				//
	if(LOG.fa) LOG.println("class Database #queary_update_FAQ() : ＦＡＱデータベースの更新用クエリ = " + Q);
	return Q;
}
//
// ＦＡＱデータベースのレコードを削除する
public int delete_FAQ( Vector v ){
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int 		  cnt 		= 0;
	//
	try {
		conn 	= Broker.getConnection(); 
		stmt 	= conn.createStatement();
		cnt		= stmt.executeUpdate(queary_delete_FAQ(v)); // ここでdeleteする
		//
	}catch(Exception e){
		System.out.println("### 失敗:delete_FAQ()" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);
   	}
		if(LOG.fa) LOG.println("class Database #delete_FAQ() : ＦＡＱデータベースのレコード削除件数 = " + cnt);
	return cnt; // 件数（普通は１）
}
//
// ＦＡＱデータベースの削除用クエリの作成
String queary_delete_FAQ(Vector v){
	//
	String te_lec_key	= (String)(v.get(FAQ.TE_LEC_KEY));
	String seq_no		= (String)(v.get(FAQ.SEQ_NO));
	String Q 			=  " DELETE  from  FAQ "	+ " WHERE te_lec_key = "  + m1 + te_lec_key + m1 + " AND seq_no = " + m1 + seq_no + m1;
	//
	if(LOG.fa) LOG.println("class Database #queary_delete_FAQ() : ＦＡＱデータベースの削除用クエリ = " + Q);
	return Q;
}





//////////////////////////////////////////////////////////////////////////
//
//■ Ｑ＆Ａデータベース
//
///////////////////////////////////////////////////////////////////////////
/*
■Ｑ＆Ａメール用データベース
	
	実施講義単位にまとめ、受け取り時間順に並べる
	
	seq_no ---- 受付番号 ６桁 KeyGen で発生
	　　　　　　個人単位（講義単位ではない）
	
	内容変更に関してはＦＡＱに対する追加・削除・訂正も連動して行う
	
create table faq_mail (
   te_aplec_key     VARCHAR(16)     NOT NULL,
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
//  質問メールのレコードを読み出す //
//
public int read_FAQmail(Vector v){
	if(LOG.fa) LOG.outVector(v,"class Database #read_FAQmail() :  質問メールのレコードを読み出す の先頭です");
	//
	Connection   conn   = null;
   	Statement 	 stmt   = null;
	//
	int	cnt = 0;
	try {
       	// Broker からDB Connection を得る
       	conn 		 = Broker.getConnection();/** Broker からDB Connection を得る */
		stmt 		 = conn.createStatement();
		ResultSet rs = stmt.executeQuery(query_read_FAQmail(v));
		while(rs.next()){
			//
			//--------------------------------
			//v.add(rs.getString("te_aplec_key")); すでに入っている add すると順番が狂う
			//v.add(rs.getString("seq_no"));
			v.add(rs.getString("rvdate"));
			v.add(rs.getString("lec_key"));
	    	v.add(rs.getString("stNumber"));
	    	v.add(rs.getString("ml_title"));
	   		v.add(rs.getString("ml_body"));
	    	v.add(rs.getString("read_flag"));
			v.add(rs.getString("faq_title"));
	    	v.add(rs.getString("faq_flag"));
			//--------------------------------
			++cnt;
			break;
		}
		//
	}catch(Exception e){
		System.out.println("#### 失敗:read_FAQmail() " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
       	Broker.freeConnection(conn);/** Broker に Connection を戻す */
   	}
    if(LOG.fa) LOG.outVector(v,"class Database #read_FAQmail() : 検索結果 " + cnt + " 件" );
	return cnt;
}
//
// クエリーを作る
String query_read_FAQmail(Vector v){
		if(LOG.fa) LOG.println("class Database #query_read_FAQmail() : クエリーを作る　の先頭です");
	//
	String te_aplec_key = (String) v.get(FAQmail.TE_APLEC_KEY);
	String seq_no		= (String) v.get(FAQmail.SEQ_NO);
	String cmd 			= " SELECT * FROM  FAQmail  WHERE te_aplec_key = " + m1 + te_aplec_key + m1 + "  AND seq_no = " + m1 + seq_no + m1;	
	//
	if(LOG.fa) LOG.println("class Database #query_read_FAQmail() : Q = " + cmd);
	return cmd;
}
//
// ある教員宛ての特定のコマの講義についての全質問メールを読み出す
public int read_FAQmail_all(String te_aplec_key,Vector rec){

	Connection   conn  = null;
   	Statement 	 stmt  = null;
   	int rowCount = 0;
	try {
		String cmd 	= "SELECT * FROM FAQmail  WHERE te_aplec_key = " + m1 + te_aplec_key + m1;
		if(LOG.fa) LOG.println("read_FAQmail_all() : SQL = " + cmd);
		//
      	conn 	= Broker.getConnection(); 
		stmt 	= conn.createStatement();
		ResultSet rs = stmt.executeQuery(cmd);
		while(rs.next()){
			Vector v = new Vector(20,10);
			v.add(rs.getString("te_aplec_key"));
			v.add(rs.getString("seq_no"));
			v.add(rs.getString("rvdate"));
			v.add(rs.getString("lec_key"));
	    	v.add(rs.getString("stNumber"));
	    	v.add(rs.getString("ml_title"));
	   		v.add(rs.getString("ml_body"));
	    	v.add(rs.getString("read_flag"));
			v.add(rs.getString("faq_title"));
	    	v.add(rs.getString("faq_flag"));
			rec.add(v);
			rowCount++;
			if(LOG.fa) LOG.println(rowCount);
		}
	}catch(Exception e){
		System.out.println("### 失敗: read_FAQmail_all()" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);
   	}
	if(LOG.fa) LOG.println( "Database:getDefLectures() :検索結果＝" + String.valueOf(rowCount) );
	return rowCount;
}


/*

int read_FAQmail_all(String te_aplec_key,Vector rec){
	if(DBG._tr090) DBG.println("class Database #read_FAQmail_all() :  ある教員宛ての全質問メールを読み出す の先頭です");
	//
	//
	Connection 	conn = null;
	Statement	stmt = null;
	int	cnt = 0;
	try {
       	// Broker からDB Connection を得る
       	conn	= Broker.getConnection();
		stmt	= conn.createStatement();
		String 		cmd 	= " SELECT * FROM  FAQmail  WHERE te_aplec_key = " + m1 + te_aplec_key + m1;
		ResultSet 	rs		= stmt.executeQuery(cmd);
		//ResultSet 	rs		= stmt.executeQuery(query_read_FAQmail_all(te_aplec_key));
		while(rs.next()){
			//
			Vector v = new Vector(15,10);
			//--------------------------------
			v.add(rs.getString("te_aplec_key"));
			v.add(rs.getString("seq_no"));
			v.add(rs.getString("rvdate"));
			v.add(rs.getString("lec_key"));
	    	v.add(rs.getString("stNumber"));
	    	v.add(rs.getString("ml_title"));
	   		v.add(rs.getString("ml_body"));
	    	v.add(rs.getString("read_flag"));
			v.add(rs.getString("faq_title"));
	    	v.add(rs.getString("faq_flag"));
			//--------------------------------
			rec.add(v);
			++cnt;
			break;
		}
		if(DBG._tr090) DBG.println("class Database #query_read_FAQmail() : Q = " + cmd);
		//
	}catch(Exception e){
		System.out.println("#### 失敗:read_FAQmail() " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
       	Broker.freeConnection(conn);
   	}
    if(DBG._tr090) DBG.outVector2(rec,"class Database #read_FAQmail_all() : 検索結果 全 [" + cnt + "] 件でした" );
	return cnt;
}
//
// クエリーを作る
String query_read_FAQmail_all(String te_aplec_key){
		if(DBG._tr090) DBG.println("class Database #query_read_FAQmail() : クエリーを作る　の先頭です");
	//
	String cmd 			= " SELECT * FROM  FAQmail  WHERE te_aplec_key = " + m1 + te_aplec_key + m1;	
	//
	if(DBG._tr090) DBG.println("class Database #query_read_FAQmail() : Q = " + cmd);
	return cmd;
}
*/
//
//  質問メールのレコードを追加する  //
//
public int insert_FAQmail( Vector v ){
	if(LOG.fa) LOG.outVector(v,"class Database #insert_FAQmail() :  質問メールのレコードを追加する の先頭です");
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int cnt = 0;
	//
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		cnt  	= stmt.executeUpdate(query_update_FAQmail(v));
		//
	}catch(Exception e){
		System.out.println("### 失敗:insert_FAQmail() " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	if(LOG.fa) LOG.println("class Database #insert_FAQmail() : 追加件数 = " + cnt);
	return cnt; // 件数（普通は１）
}	
//
// 質問メールののレコード追加のクエリーを作る
String 	query_update_FAQmail(Vector v){
		if(LOG.fa) LOG.println("class Database #query_FAQmail() : 質問メールののレコード追加のクエリーを作る　の先頭です");
	//
	String te_aplec_key	= (String)(v.get(FAQmail.TE_APLEC_KEY));
	String seq_no		= (String)(v.get(FAQmail.SEQ_NO));
	String rvdate		= (String)(v.get(FAQmail.RVDATE));
	String lec_key		= (String)(v.get(FAQmail.LEC_KEY));
	String stNumber		= (String)(v.get(FAQmail.STNUMBER));
	String ml_title		= (String)(v.get(FAQmail.ML_TITLE));
	String ml_body		= (String)(v.get(FAQmail.ML_BODY));
	String read_flag	= (String)(v.get(FAQmail.READ_FLAG));
	String faq_title	= (String)(v.get(FAQmail.FAQ_TITLE));
	String faq_flag		= (String)(v.get(FAQmail.FAQ_FLAG));
	//
	String Q = "INSERT INTO  FAQmail  VALUES (" 
	            + m1 + te_aplec_key + m2 
	            + m1 + seq_no  		+ m2 
				+ m1 + rvdate  		+ m2
	            + m1 + lec_key  	+ m2 
				+ m1 + stNumber  	+ m2
	            + m1 + ml_title  	+ m2 
				+ m1 + ml_body  	+ m2
				+ m1 + read_flag  	+ m2
				+ m1 + faq_title  	+ m2
				+ m1 + faq_flag 	+ m1
				+ ")";
	//
	if(LOG.fa) LOG.println("class Database #query_FAQmail() : Q = " + Q);
	return Q;
}	
//
// 質問メールのレコードを更新する
public int update_FAQmail( Vector v ){
	Connection   conn  = null;
   	Statement 	 stmt  = null;
	int  		 cnt   = 0;
	//
	try {
      	conn 	= Broker.getConnection(); /** Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		cnt     = stmt.executeUpdate(queary_update_FAQmail(v));
	}catch(Exception e){
		System.out.println("#exwk ### 失敗:queary_update_FAQmail() " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/** connection を Broker に返す */
   	}
		if(LOG.fa) LOG.println("class Database #insert_FAQmail() : 質問メールのレコード更新件数 = " + cnt);
	return cnt;
}
//
// 質問メールの更新用クエリの作成
String queary_update_FAQmail(Vector v){
	//
	String te_aplec_key	= (String)(v.get(FAQmail.TE_APLEC_KEY));
	String seq_no		= (String)(v.get(FAQmail.SEQ_NO));
	String rvdate		= (String)(v.get(FAQmail.RVDATE));
	String lec_key		= (String)(v.get(FAQmail.LEC_KEY));
	String stNumber		= (String)(v.get(FAQmail.STNUMBER));
	String ml_title		= (String)(v.get(FAQmail.ML_TITLE));
	String ml_body		= (String)(v.get(FAQmail.ML_BODY));
	String read_flag	= (String)(v.get(FAQmail.READ_FLAG));
	String faq_title	= (String)(v.get(FAQmail.FAQ_TITLE));
	String faq_flag		= (String)(v.get(FAQmail.FAQ_FLAG));
	//
	String Q = " UPDATE  FAQmail  set  " 
				//
				+ "rvdate = "  + m1 + rvdate  + m2
				+ "lec_key = "  + m1 + lec_key  + m2
				+ "stNumber = "  + m1 + stNumber  + m2
				+ "ml_title = "  + m1 + ml_title  + m2
				+ "ml_body = "  + m1 + ml_body  + m2
				+ "read_flag = "  + m1 + read_flag  + m2
				+ "faq_title = "  + m1 + faq_title  + m2
				+ "faq_flag = "  + m1 + faq_flag  + m1
				+ " WHERE te_aplec_key = " + m1 + te_aplec_key + m1 + " AND seq_no = " + m1 + seq_no + m1;
				//
	if(LOG.fa) LOG.println("class Database #queary_update_FAQmail() : 質問メールの更新用クエリ = " + Q);
	return Q;
}
//-------------------------------------------------------------------------------------------------------------------
//
// 本文のみ更新（read フラグは自動的に ＯＮ）
public int update_FAQmail_body( String te_aplec_key,String seq_no,String body ){
	Connection   conn  = null;
   	Statement 	 stmt  = null;
	int  		 cnt   = 0;
	//
	try {
      	conn 	= Broker.getConnection();
		stmt 	= conn.createStatement();
		String	q	= queary_update_FAQmail_body( te_aplec_key, seq_no, body );
		if(LOG.fa) LOG.println("queary_update_FAQmail_body  = " + q);
		cnt     = stmt.executeUpdate(queary_update_FAQmail_body( te_aplec_key, seq_no, body ));
	}catch(Exception e){
		System.out.println("#exwk ### 失敗:update_FAQmail_body() " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);
   	}
		if(LOG.fa) LOG.println("class Database #update_FAQmail_body() : 本文のみ更新（read フラグは自動的に ＯＮ) 更新件数 = " + cnt);
	return cnt;
}
//
// 本文のみ更新（read フラグは自動的に ＯＮ）
String queary_update_FAQmail_body(String te_aplec_key,String seq_no,String body){
	//
	String flag = "ON";
	String Q = " UPDATE FAQmail set " + 
	            " ml_body   = "  + m1 + body  + m2
			  + " read_flag = "  + m1 + flag  + m1
		      + " WHERE te_aplec_key = " + m1 + te_aplec_key + m1 + " AND seq_no = " + m1 + seq_no + m1;
	if(LOG.fa) LOG.println("class Database #queary_update_FAQmail_body() : 本文のみ更新（read フラグは自動的に ＯＮ）用クエリ = " + Q);
	return Q;
}
//
// 本文のみ更新（read フラグはそのまま）
public int update_FAQmail_body_2( String te_aplec_key,String seq_no,String body ){
	Connection   conn  = null;
   	Statement 	 stmt  = null;
	int  		 cnt   = 0;
	//
	try {
      	conn 	= Broker.getConnection();
		stmt 	= conn.createStatement();
		cnt     = stmt.executeUpdate(queary_update_FAQmail_body_2( te_aplec_key, seq_no, body ));
	}catch(Exception e){
		System.out.println("#exwk ### 失敗:update_FAQmail_body_2() " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);
   	}
		if(LOG.fa) LOG.println("class Database #update_FAQmail_body_2() : 本文のみ更新（read フラグはそのまま) 更新件数 = " + cnt);
	return cnt;
}
//
// 本文のみ更新（read フラグは自動的に ＯＮ）
String queary_update_FAQmail_body_2(String te_aplec_key,String seq_no,String body){
	//
	String Q = " UPDATE FAQmail set " + 
	            " ml_body   = "  + m1 + body  + m1
		      + " WHERE te_aplec_key = " + m1 + te_aplec_key + m1 + " AND seq_no = " + m1 + seq_no + m1;
	if(LOG.fa) LOG.println("class Database #queary_update_FAQmail_body_2() : 本文のみ更新（read フラグはそのまま）用クエリ = " + Q);
	return Q;
}

//-------------------------------------------------------------------------------------------------------------------
//
// 未読フラグ更新
public int update_FAQmail_readFlag( String te_aplec_key,String seq_no,String readFlag ){
	Connection   conn  = null;
   	Statement 	 stmt  = null;
	int  		 cnt   = 0;
	//
	try {
      	conn 	= Broker.getConnection();
		stmt 	= conn.createStatement();
		cnt     = stmt.executeUpdate(queary_update_FAQmail_readFlag( te_aplec_key, seq_no, readFlag ));
	}catch(Exception e){
		System.out.println("#exwk ### 失敗:update_FAQmail_readFlag() " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);
   	}
		if(LOG.fa) LOG.println("class Database #update_FAQmail_readFlag() : 未読フラグ更新件数 = " + cnt);
	return cnt;
}
//
// 未読フラグ更新用クエリの作成
String queary_update_FAQmail_readFlag(String te_aplec_key,String seq_no,String read_flag){
	//
	String Q = " UPDATE FAQmail set read_flag = "  + m1 + read_flag  + m1  + " WHERE te_aplec_key = " + m1 + te_aplec_key + m1 + " AND seq_no = " + m1 + seq_no + m1;
	if(LOG.fa) LOG.println("class Database #queary_update_FAQmail_readFlag() : 未読フラグ更新用クエリ = " + Q);
	return Q;
}

//-------------------------------------------------------------------------------------------------------------------
//
// ＦＡＱフラグ＆タイトル＆本文更新
public int update_FAQmail_faq_set( String te_aplec_key,String seq_no,String faqFlag, String faqTitle ,String body){
	Connection   conn  = null;
   	Statement 	 stmt  = null;
	int  		 cnt   = 0;
	//
	try {
      	conn 	= Broker.getConnection(); 
		stmt 	= conn.createStatement();
		cnt     = stmt.executeUpdate(queary_update_FAQmail_faq_set( te_aplec_key, seq_no, faqFlag ,faqTitle ,body));
	}catch(Exception e){
		System.out.println("#exwk ### 失敗:queary_update_FAQmail_faq_set() " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);
   	}
		if(LOG.fa) LOG.println("class Database #queary_update_FAQmail_faq_set() : ＦＡＱフラグ＆タイトル＆本文　更新件数 = " + cnt);
	return cnt;
}
//
// ＦＡＱフラグ＆タイトル更新用クエリの作成
String queary_update_FAQmail_faq_set(String te_aplec_key,String seq_no,String faq_flag  ,String faq_title,String ml_body){
	//
	String Q = " UPDATE FAQmail set " + 
	   " faq_flag = "           +   m1 + faq_flag     + m2 + 
	   " ml_body  = "           +   m1 + ml_body      + m2 + 
	   " faq_title = "          +   m1 + faq_title    + m1 + 
	   " WHERE te_aplec_key = " +   m1 + te_aplec_key + m1 + 
	   " AND    seq_no = "      +   m1 + seq_no       + m1;
		 
	if(LOG.fa) LOG.println("class Database #queary_update_FAQmail_faq_set() : ＦＡＱフラグ＆タイトル更新用クエリ = " + Q);
	return Q;
}

//
// ＦＡＱフラグ＆タイトル更新
public int update_FAQmail_faqFlag( String te_aplec_key,String seq_no,String faqFlag, String faqTitle ){
	Connection   conn  = null;
   	Statement 	 stmt  = null;
	int  		 cnt   = 0;
	//
	try {
      	conn 	= Broker.getConnection(); 
		stmt 	= conn.createStatement();
		cnt     = stmt.executeUpdate(queary_update_FAQmail_faqFlag( te_aplec_key, seq_no, faqFlag ,faqTitle));
	}catch(Exception e){
		System.out.println("#exwk ### 失敗:update_FAQmail_faqFlag() " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);
   	}
		if(LOG.fa) LOG.println("class Database #update_FAQmail_faqFlag() : ＦＡＱフラグ＆タイトル更新件数 = " + cnt);
	return cnt;
}
//
// ＦＡＱフラグ＆タイトル更新用クエリの作成
String queary_update_FAQmail_faqFlag(String te_aplec_key,String seq_no,String faq_flag  ,String faq_title){
	//
	String Q = " UPDATE FAQmail set " + 
	   " faq_flag = "           +   m1 + faq_flag     + m2 + 
	   " faq_title = "          +   m1 + faq_title    + m1 + 
	   " WHERE te_aplec_key = " +   m1 + te_aplec_key + m1 + 
	   " AND    seq_no = "      +   m1 + seq_no       + m1;
		 
	if(LOG.fa) LOG.println("class Database #queary_update_FAQmail_faqFlag() : ＦＡＱフラグ＆タイトル更新用クエリ = " + Q);
	return Q;
}


//
// ＦＡＱフラグ＆タイトル更新
public int update_FAQmail_faqFlagOnly( String te_aplec_key,String seq_no,String faqFlag){
	Connection   conn  = null;
   	Statement 	 stmt  = null;
	int  		 cnt   = 0;
	//
	try {
      	conn 	= Broker.getConnection(); 
		stmt 	= conn.createStatement();
		cnt     = stmt.executeUpdate(queary_update_FAQmail_faqFlagOnly( te_aplec_key, seq_no, faqFlag));
	}catch(Exception e){
		System.out.println("#exwk ### 失敗:update_FAQmail_faqFlag() " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);
   	}
		if(LOG.fa) LOG.println("class Database #update_FAQmail_faqFlag() : ＦＡＱフラグ＆タイトル更新件数 = " + cnt);
	return cnt;
}
//
// ＦＡＱフラグ＆タイトル更新用クエリの作成
String queary_update_FAQmail_faqFlagOnly(String te_aplec_key,String seq_no,String faq_flag){
	//
	String Q = " UPDATE FAQmail set " + 
	   " faq_flag = "           +   m1 + faq_flag     + m1 + 
	   " WHERE te_aplec_key = " +   m1 + te_aplec_key + m1 + 
	   " AND    seq_no = "      +   m1 + seq_no       + m1;
		 
	if(LOG.fa) LOG.println("class Database #queary_update_FAQmail_faqFlagOnly() : ＦＡＱフラグ＆タイトル更新用クエリ = " + Q);
	return Q;
}

//--------------------------------------
//
// 質問メールのレコードを削除する
public int delete_FAQmail( Vector v ){
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int 		  cnt 		= 0;
	//
	try {
		conn 	= Broker.getConnection(); 
		stmt 	= conn.createStatement();
		cnt		= stmt.executeUpdate(queary_delete_FAQmail(v)); // ここでdeleteする
		//
	}catch(Exception e){
		System.out.println("### 失敗:delete_FAQmail( )" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);
   	}
		if(LOG.fa) LOG.println("class Database #delete_FAQmail() : 質問メールのレコード削除件数 = " + cnt);
	return cnt; // 件数（普通は１）
}
//
// 質問メールの削除用クエリの作成
String queary_delete_FAQmail(Vector v){
	//
	String te_aplec_key	= (String)(v.get(FAQmail.TE_APLEC_KEY));
	String seq_no		= (String)(v.get(FAQmail.SEQ_NO));
	//
	String Q =  " DELETE  from  FAQmail "	+ " WHERE te_aplec_key = "  + m1 + te_aplec_key + m1 + " AND seq_no = " + m1 + seq_no + m1;
	//
	if(LOG.fa) LOG.println("class Database #queary_delete_FAQmail() : 質問メールの削除用クエリ = " + Q);
	return Q;
}
//
////////////////////////////////////////////////////
//
//　　講義実施関係
//
////////////////////////////////////////////////////
//
/*
■講義実施データ

  lec_key は 講義定義データのサブキー部分 teUid-lec_key で講義定義キーとなる

  meibo はクラスの名簿ファイル名

  shubetsu  1 = 一般講義
        2 = 夜間講義
		3 = e-Learning

  3 の場合、worder のみ意味を持つ

　yyyy は年度だが、 2003 とか 平成１５ か　決められないので VARCHAR にしてある
　term も前期、前後期、通年などいろいろな書き方があるので同上

　unit は単位数。1.5 とかもありえる 

  title は lecture のtitleと同じ（表示用の冗長情報）

-----------------------------------------------------------
create table app_lecture (
   teUid            VARCHAR(12) NOT NULL,
   aplec_key        CHAR(3) NOT NULL,
   lec_key          CHAR(3) NOT NULL,
   meibo            VARCHAR(100),
   shubetsu         CHAR(1),
   wdate            CHAR(1),
   worder           CHAR(1),
   yyyy             VARCHAR(20),
   term             VARCHAR(20),
   unit             VARCHAR(3),
   title            VARCHAR(100)
);
-----------------------------------------------------------
// キーは複数列に設定する
create index app_lecture_idx on app_lecture (teUid,aplec_key);
-----------------------------------------------------------
   teUid			教員キー
   aplec_key		講義実施キー
   lec_key			講義定義キー
   meibo			名簿ファイル名
   shubetsu			種別（1:一般、2:夜間、3:e-Learning)
   wdate			曜日番号（0-5)
   worder			時限番号(0-4)
   yyyy				実施年度
   term				実施期
   unit				単位数
   title			科目名（冗長情報）
*/
//
//
//  特定の教員の全ての科目実施データをベクターに入れて返す
public int getApLectures(String teUid,Vector vrec){

	Connection   conn  = null;
   	Statement 	 stmt  = null;
   	int rowCount = 0;
	try {
		String cmd 	= "SELECT * FROM app_lecture  WHERE teuid = " + m1 + teUid + m1;
		if(LOG.fa) LOG.println("getDefLectures() : SQL = " + cmd);
		//
      	conn 	= Broker.getConnection(); 
		stmt 	= conn.createStatement();
		ResultSet rs = stmt.executeQuery(cmd);
		while(rs.next()){
			Vector v = new Vector(20,10);
			//
			v.add(teUid);
   			v.add(rs.getString("aplec_key"));
   			v.add(rs.getString("lec_key"));
   			v.add(rs.getString("meibo"));
   			v.add(rs.getString("shubetsu"));
   			v.add(rs.getString("wdate"));
   			v.add(rs.getString("worder"));
   			v.add(rs.getString("yyyy"));
   			v.add(rs.getString("term"));
   			v.add(rs.getString("unit"));
   			v.add(rs.getString("title"));
			//
			vrec.add(v);
			rowCount++;
			if(LOG.fa) LOG.println(rowCount);
		}
	}catch(Exception e){
		System.out.println("### 失敗: getDefLectures()" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);
   	}
	if(LOG.fa) LOG.println( "Database:getDefLectures()","実施レコードの数＝" + String.valueOf(rowCount) );
	return rowCount;
}
//
//
// 特定の教員の講義実施キーで特定される科目実施データをベクターに入れて返す
public Vector getApLecture(String _teUid,String _aplec_key){
    if(LOG.fa) LOG.println("getApLecture()の先頭です","_teUid=" + _teUid + "_aplec_key=" + _aplec_key);
	//
	String teUid    	= _teUid.trim();
	String aplec_key    = _aplec_key.trim();
	String cmd 		= " SELECT * FROM app_lecture WHERE teUid = " + m1 + teUid + m1 + "  AND aplec_key = " + m1 + aplec_key + m1;
	if(LOG.fa) LOG.println("getApLecture() : SQL CMD = " + cmd);
	Connection   conn   = null;
   	Statement 	 stmt   = null;
	//
	Vector v = new Vector(20,10);
	int rowCount = 0;
	try {
       	// Broker からDB Connection を得る
       	conn 		 = Broker.getConnection();/** Broker からDB Connection を得る */
		stmt 		 = conn.createStatement();
		ResultSet rs = stmt.executeQuery(cmd);
		while(rs.next()){ // 得られるレコードは１件だけ(2013.7.14)
   			v.add(teUid);
   			v.add(aplec_key);
   			v.add(rs.getString("lec_key"));
   			v.add(rs.getString("meibo"));
   			v.add(rs.getString("shubetsu"));
   			v.add(rs.getString("wdate"));
   			v.add(rs.getString("worder"));
   			v.add(rs.getString("yyyy"));
   			v.add(rs.getString("term"));
   			v.add(rs.getString("unit"));
   			v.add(rs.getString("title"));
			//
			++rowCount;
			break;
		}
		//
	}catch(Exception e){
		System.out.println("#### 失敗:getApLecture(String _teUid,String _aplec_key) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
       	Broker.freeConnection(conn);/** Broker に Connection を戻す */
   	}
    if(LOG.fa) LOG.outVector(v,"getApLecture()での検索結果 " + rowCount + " 件" );
	return v;
	//
}
//
// 新しい科目実施レコードを挿入する
public int insertApLecture( Vector v ){
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = -1;
	//
	String QUERY = insert_ApLecture_Query(v);
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY);
		//
	}catch(Exception e){
		System.out.println("### 失敗:insertApLecture( Vector v ) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 件数（普通は１）
}
//
// 挿入用クエリの作成
String insert_ApLecture_Query(Vector v){
	//
	String teUid		= (String)(v.get(0));
   	String aplec_key	= (String)(v.get(1));
   	String lec_key		= (String)(v.get(2));
   	String meibo		= (String)(v.get(3));
   	String shubetsu		= (String)(v.get(4));
   	String wdate		= (String)(v.get(5));
   	String worder		= (String)(v.get(6));
   	String yyyy			= (String)(v.get(7));
   	String term			= (String)(v.get(8));
   	String unit			= (String)(v.get(9));
   	String title		= (String)(v.get(10));
	//
	String Q = "INSERT INTO app_lecture  VALUES (" 
	            + m1 + teUid  		+ m2 
				+ m1 + aplec_key  	+ m2
				+ m1 + lec_key  	+ m2
				+ m1 + meibo 		+ m2
				+ m1 + shubetsu 	+ m2
				+ m1 + wdate 		+ m2
				+ m1 + worder 		+ m2
				+ m1 + yyyy 		+ m2
				+ m1 + term 		+ m2
				+ m1 + unit 		+ m2
				+ m1 + title 		+ m1
				+ ")";
	if(LOG.fa) LOG.println("Query = " +  Q);
	return Q;
}
//
// 科目実施レコードを更新する
public int updateApLecture( Vector v ){
	Connection   conn  = null;
   	Statement 	 stmt  = null;
	int  		 count = 0;
	//
	String QUERY = update_ApLecture_Query(v);
	//
	try {
      	conn 	= Broker.getConnection(); /** Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		count  = stmt.executeUpdate(QUERY);
	}catch(Exception e){
		System.out.println("#exwk ### 失敗:updateApLecture( Vector v ) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/** connection を Broker に返す */
   	}
	return count;
}
//
// 更新用クエリの作成
String update_ApLecture_Query(Vector v){
	//
	String teUid		= (String)(v.get(0));
   	String aplec_key	= (String)(v.get(1));
   	String lec_key		= (String)(v.get(2));
   	String meibo		= (String)(v.get(3));
   	String shubetsu		= (String)(v.get(4));
   	String wdate		= (String)(v.get(5));
   	String worder		= (String)(v.get(6));
   	String yyyy			= (String)(v.get(7));
   	String term			= (String)(v.get(8));
   	String unit			= (String)(v.get(9));
   	String title		= (String)(v.get(10));
	//
	String Q = "UPDATE  app_lecture  set  " 
				//
				+ "lec_key = "  + m1 + lec_key  	+ m2
				+ "meibo = "    + m1 + meibo 		+ m2
				+ "shubetsu = " + m1 + shubetsu 	+ m2
				+ "wdate = "    + m1 + wdate 		+ m2
				+ "worder = "   + m1 + worder 		+ m2
				+ "yyyy = "     + m1 + yyyy 		+ m2
				+ "term = "     + m1 + term 		+ m2
				+ "unit = "     + m1 + unit 		+ m2
				+ "title = "    + m1 + title 		+ m1
				+ " WHERE teUid = "  + m1 + teUid + m1 + " AND aplec_key = " + m1 + aplec_key + m1;
				//
	if(LOG.fa) LOG.println("Query = " +  Q);
	return Q;
}
//
// 科目実施レコードを削除する
public int deleteApLecture( String _teUid, String _aplec_key ){
	String teUid    	= _teUid.trim();
	String aplec_key    = _aplec_key.trim();
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = 0;
	//
	String QUERY =  "DELETE from app_lecture "
					+ " WHERE teUid = "  + m1 + teUid + m1 + " AND aplec_key = " + m1 + aplec_key + m1;
	//
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY); // ここでdeleteする
		//
	}catch(Exception e){
		System.out.println("### 失敗:deleteApLecture( String teuid, String aplec_key ) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 件数（普通は１）
}	
//
//
//
//=====================================================================
//  特定の科目（定義）に関する科目実施レコードをベクターに入れて返す
//=====================================================================
public int get_some_ApLectures(String teUid, String lec_key, Vector vrec){
	if(LOG.fa) LOG.println("class Database #get_some_ApLectures() : 特定の科目（定義）に関する科目実施レコードをベクターに入れて返す の先頭です");
	if(LOG.fa) LOG.println("           teUid   = " + teUid);
	if(LOG.fa) LOG.println("           lec_key = " + lec_key);
	//
	Connection   conn  = null;
   	Statement 	 stmt  = null;
   	int rowCount = 0;
	try {
		String cmd 	= "SELECT * FROM app_lecture  WHERE teuid = " + m1 + teUid + m1 + " AND lec_key = " + m1 + lec_key + m1;
		if(LOG.fa) LOG.println("class Database #get_some_ApLectures() : SQL = " + cmd);
		//
      	conn 	= Broker.getConnection(); 
		stmt 	= conn.createStatement();
		ResultSet rs = stmt.executeQuery(cmd);
		while(rs.next()){
			Vector v = new Vector(20,10);
			//
			v.add(teUid);
   			v.add(rs.getString("aplec_key"));
   			v.add(rs.getString("lec_key"));
   			v.add(rs.getString("meibo"));
   			v.add(rs.getString("shubetsu"));
   			v.add(rs.getString("wdate"));
   			v.add(rs.getString("worder"));
   			v.add(rs.getString("yyyy"));
   			v.add(rs.getString("term"));
   			v.add(rs.getString("unit"));
   			v.add(rs.getString("title"));
			//
			vrec.add(v);
			rowCount++;
			if(LOG.fa) LOG.outVector(v,"<===========================>");
		}
	}catch(Exception e){
		System.out.println("### 失敗: getDefLectures()" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);
   	}
	if(LOG.fa) LOG.println( "class Database #get_some_ApLectures() : 実施レコードの数＝" + String.valueOf(rowCount) );
	return rowCount;
}
//
//============================================================
//  特定の講義（定義）に関連する科目実施レコードを全て削除する
//  アプリから直接実行する
//============================================================
//
public int delete_some_ApLectures( String _teUid, String _lec_key ){
	if(LOG.fa) LOG.println("class Database #delete_some_ApLectures() :  特定の講義（定義）に関連する科目実施レコードを全て削除する の先頭です");
	if(LOG.fa) LOG.println("      　　teUid = " + _teUid);
	if(LOG.fa) LOG.println("      　lec_key = " + _lec_key);
	//
	String teUid    	= _teUid.trim();
	String lec_key      = _lec_key.trim();
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = 0;
	//
	String QUERY =  "DELETE from app_lecture "
					+ " WHERE teUid = "  + m1 + teUid + m1 + " AND lec_key = " + m1 + lec_key + m1;
	if(LOG.fa) LOG.println("class Database #delete_some_ApLectures() : SQL = " + QUERY);
	//
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY); // ここでdeleteする
		//
	}catch(Exception e){
		System.out.println("### 失敗:delete_some_ApLectures() " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 
}
//
////////////////////////////////////////////////////
//
// 　　講義定義関係
//
////////////////////////////////////////////////////
//
/*
■講義定義データ
------------------------------------------------------
create table lecture (
   teUid            VARCHAR(12) NOT NULL,
   lec_key          CHAR(3) NOT NULL,
   title            VARCHAR(100),
   content          text,
   seiseki_hyoka    text,
   keywords         text,
   textbook         text,
   ref_texts        text,
   ref_urls         text,
   bikou            text
);
------------------------------------------------------
// キーは複数列に設定する
create index lectur_idx on lecture (teUid,lec_key);	
------------------------------------------------------
   teUid
   lec_key
   title
   content
   seiseki_hyoka
   keywords
   textbook
   ref_texts
   ref_urls
   bikou
*/
//
//  特定の教員の全ての科目定義データをベクターに入れて返す
public int getDefLectures(String teUid,Vector vrec){

	Connection   conn  = null;
   	Statement 	 stmt  = null;
   	int rowCount = 0;
	try {
		String cmd 	= "SELECT * FROM lecture  WHERE teuid = " + m1 + teUid + m1;
		if(LOG.fa) LOG.println("getDefLectures() : SQL = " + cmd);
		//
      	conn 	= Broker.getConnection(); 
		stmt 	= conn.createStatement();
		ResultSet rs = stmt.executeQuery(cmd);
		while(rs.next()){
			Vector v = new Vector(20,10);
			v.add(rs.getString("teUid"));
			v.add(rs.getString("lec_key"));
			v.add(rs.getString("title"));
			v.add(rs.getString("content"));
			v.add(rs.getString("seiseki_hyoka"));
			v.add(rs.getString("keywords"));
			v.add(rs.getString("textbook"));
			v.add(rs.getString("ref_texts"));
			v.add(rs.getString("ref_urls"));
			v.add(rs.getString("note"));
			v.add(rs.getString("bikou"));
			vrec.add(v);
			rowCount++;
			if(LOG.fa) LOG.println(rowCount);
		}
	}catch(Exception e){
		System.out.println("### 失敗: getDefLectures()" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);
   	}
	if(LOG.fa) LOG.println( "Database:getDefLectures()","講義定義レコードの数＝" + String.valueOf(rowCount) );
	return rowCount;
}
//
//
// 特定の教員の科目定義キーで特定される科目定義データをベクターに入れて返す
public Vector getDefLecture(String _teUid,String _lec_key){
    if(LOG.fa) LOG.println("getDefLecture()の先頭です" + "/ teUid=" + _teUid + " ,lec_key=" + _lec_key);
	//
	String teUid    = _teUid.trim();
	String lec_key  = _lec_key.trim();
	//
	Connection  conn   	 = null;
	   	Statement 	stmt  	 = null;
	int 		rowCount = 0;
	Vector 		v 		 = new Vector(20,10);
	try {
		//
		String cmd 	 = " SELECT * FROM lecture  WHERE teUid = " + m1 + teUid + m1 + " AND lec_key = " + m1 + lec_key + m1;
		if(LOG.fa) LOG.println("getDefLecture() : SQL CMD = " + cmd);
       	//
		conn 		 = Broker.getConnection();
		stmt 		 = conn.createStatement();
		ResultSet rs = stmt.executeQuery(cmd);
		while(rs.next()){
   			v.add(teUid);
   			v.add(lec_key);
   			v.add(rs.getString("title"));
   			v.add(rs.getString("content"));
   			v.add(rs.getString("seiseki_hyoka"));
   			v.add(rs.getString("keywords"));
   			v.add(rs.getString("textbook"));
   			v.add(rs.getString("ref_texts"));
   			v.add(rs.getString("ref_urls"));
   			v.add(rs.getString("note"));
   			v.add(rs.getString("bikou"));
			//
			if(LOG.fa) {
   				LOG.println("getDefLecture() での検索結果");
				System.out.println(teUid);
   				System.out.println(lec_key);
   				System.out.println(rs.getString("title"));
   				System.out.println(rs.getString("content"));
   				System.out.println(rs.getString("seiseki_hyoka"));
   				System.out.println(rs.getString("keywords"));
   				System.out.println(rs.getString("textbook"));
   				System.out.println(rs.getString("ref_texts"));
   				System.out.println(rs.getString("ref_urls"));
   				System.out.println(rs.getString("note"));
   				System.out.println(rs.getString("bikou"));
			}
			++rowCount;
			break;
		}
		//
	}catch(Exception e){
		System.out.println("#### 失敗:getDefLecture(String _teUid,String _lec_key) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
       	Broker.freeConnection(conn);
   	}
	if(LOG.fa) LOG.println( "Database:getDefLecture()","検索したレコードの数＝" + String.valueOf(rowCount) );
	return v;
	//
}
//
// 新しい科目実施レコードを挿入する
public int insertDefLecture( Vector v ){
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = -1;
	//
	String QUERY = insert_DefLecture_Query(v);
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY);
		//
	}catch(Exception e){
		System.out.println("### 失敗:insertDefLecture( Vector v ) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 件数（普通は１）
}
//
// 挿入用クエリの作成
String insert_DefLecture_Query(Vector v){
	//
	String teUid			= (String)(v.get(0));
   	String lec_key			= (String)(v.get(1));
	//
    String title			= (String)(v.get(2));
    String content			= (String)(v.get(3));
    String seiseki_hyoka	= (String)(v.get(4));
    String keywords			= (String)(v.get(5));
    String textbook			= (String)(v.get(6));
    String ref_texts		= (String)(v.get(7));
    String ref_urls			= (String)(v.get(8));
    String note				= (String)(v.get(9));
    String bikou			= (String)(v.get(10));
	//
	String Q = "INSERT INTO lecture  VALUES (" 
	            + m1 + teUid  			+ m2 
				+ m1 + lec_key  		+ m2
				+ m1 + title 			+ m2
				+ m1 + content 			+ m2
				+ m1 + seiseki_hyoka 	+ m2
				+ m1 + keywords 		+ m2
				+ m1 + textbook 		+ m2
				+ m1 + ref_texts 		+ m2
				+ m1 + ref_urls 		+ m2
				+ m1 + note		 		+ m2
				+ m1 + bikou 			+ m1
				+ ")";
	if(LOG.fa) LOG.println("★insert_DefLecture_Query() :Query = " +  Q);
	return Q;
}
//
// 科目定義レコードを更新する
public int updateDefLecture( Vector v ){
	Connection   conn  = null;
   	Statement 	 stmt  = null;
	int  		 count = 0;
	//
	String QUERY = update_DefLecture_Query(v);
	//
	try {
      	conn 	= Broker.getConnection(); /** Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		count  = stmt.executeUpdate(QUERY);
	}catch(Exception e){
		System.out.println("#exwk ### 失敗:updateDefLecture( Vector v ) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/** connection を Broker に返す */
   	}
	return count;
}
//
// 更新用クエリの作成
String update_DefLecture_Query(Vector v){
	//
	String teUid			= (String)(v.get(0));
   	String lec_key			= (String)(v.get(1));
	//
    String title			= (String)(v.get(2));
    String content			= (String)(v.get(3));
    String seiseki_hyoka	= (String)(v.get(4));
    String keywords			= (String)(v.get(5));
    String textbook			= (String)(v.get(6));
    String ref_texts		= (String)(v.get(7));
    String ref_urls			= (String)(v.get(8));
    String note				= (String)(v.get(9));
    String bikou			= (String)(v.get(10));
	//
	String Q = "UPDATE  lecture  set  " 
				//
				+ "title = "  			+ m1 + title  			+ m2
				+ "content = "    		+ m1 + content 			+ m2
				+ "seiseki_hyoka = " 	+ m1 + seiseki_hyoka 	+ m2
				+ "keywords = "    		+ m1 + keywords 		+ m2
				+ "textbook = "   		+ m1 + textbook 		+ m2
				+ "ref_texts = "     	+ m1 + ref_texts 		+ m2
				+ "ref_urls = "     	+ m1 + ref_urls 		+ m2
				+ "note = "     		+ m1 + note				+ m2
				+ "bikou = "     		+ m1 + bikou 			+ m1
				+ " WHERE teUid = "  + m1 + teUid + m1 + " AND lec_key = " + m1 + lec_key + m1;
				//
	if(LOG.fa) LOG.println("Query = " +  Q);
	return Q;
}
//
// 科目レコードを削除する
public int deleteDefLecture( String _teUid, String _lec_key ){
	String teUid    = _teUid.trim();
	String lec_key  = _lec_key.trim();
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = 0;
	//
	String QUERY =  "DELETE from lecture "
					+ " WHERE teUid = "  + m1 + teUid + m1 + " AND lec_key = " + m1 + lec_key + m1;
	//
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY); // ここでdeleteする
		//
	}catch(Exception e){
		System.out.println("### 失敗:deleteDefLecture( String teuid, String lec_key ) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	if(LOG.fa) LOG.println("Query = " +  QUERY);
	return n; // 件数（普通は１）
}
//
////////////////////////////////////////////////////
//
// 　　セクション定義関係
//
////////////////////////////////////////////////////
/*
■講義セクションデータ（講義定義の一部）
create table sect (
   te_lec_key       VARCHAR(16)  NOT NULL,
   sect_key         CHAR(5)   NOT NULL,
   seq_number       VARCHAR(2),
   title            text,
   content          text,
   note             text,
   ref_list         VARCHAR(200),
   kadai_list       VARCHAR(200)
);

// キーは複数列に設定する
create index sect_idx on sect (te_lec_key,sect_key);

キーリストの例

000001$000001%000013 など６桁の資料キー、または課題キー。
デリミッタは '$' 
kawaba01-002 のような te_lec キーは、所属セクションのキーと同じだから省略してある

*/	
//  特定の教員の全てのセクション定義データをベクターに入れて返す
public int getSectionDefs(String key,Vector vrec){

	Connection   conn  = null;
   	Statement 	 stmt  = null;
   	int rowCount = 0;
	try {
		String cmd 	= "SELECT * FROM sect  WHERE te_lec_key = " + m1 + key + m1;
		if(LOG.fa) LOG.println("getDefLectures() : SQL = " + cmd);
		//
      	conn 	= Broker.getConnection(); 
		stmt 	= conn.createStatement();
		ResultSet rs = stmt.executeQuery(cmd);
		while(rs.next()){
			//
   			Vector v = new Vector(20,10);
			v.add(key);
   			v.add(rs.getString("sect_key"));
   			v.add(rs.getString("seq_number"));
   			v.add(rs.getString("title"));
   			v.add(rs.getString("content"));
   			v.add(rs.getString("note"));
   			v.add(rs.getString("ref_list"));
   			v.add(rs.getString("kadai_list"));
			//
			vrec.add(v);rowCount++;
			if(LOG.fa) LOG.println(rowCount);
		}
	}catch(Exception e){
		System.out.println("### 失敗: getDefLectures()" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);
   	}
	if(LOG.fa) LOG.println( "Database:getSectionDefs()","セクション定義レコードの数＝" + String.valueOf(rowCount) );
	return rowCount;
}
	//
//
// 特定の科目のセクション定義キーで特定されるセクション定義データをベクターに入れて返す
public Vector getSectionDef(String te_lec_key, String sect_key){
	//
	Connection   conn   = null;
   	Statement 	 stmt   = null;
	Vector v = new Vector(20,10);
	int rowCount = 0;
	try {
		String cmd 		= " SELECT * FROM sect  WHERE te_lec_key = " + m1 + te_lec_key + m1 + "  AND sect_key = " + m1 + sect_key + m1;
		if(LOG.fa) LOG.println("getSectionDef() : SQL CMD = " + cmd);
       	// Broker からDB Connection を得る
       	conn 		 = Broker.getConnection();/** Broker からDB Connection を得る */
		stmt 		 = conn.createStatement();
		ResultSet rs = stmt.executeQuery(cmd);
		while(rs.next()){
			v.add(te_lec_key);
   			v.add(sect_key);
			//
   			v.add(rs.getString("seq_number"));
   			v.add(rs.getString("title"));
   			v.add(rs.getString("content"));
   			v.add(rs.getString("note"));
   			v.add(rs.getString("ref_list"));
   			v.add(rs.getString("kadai_list"));
			//
			++rowCount;
			break;
		}
		//
	}catch(Exception e){
		System.out.println("#### 失敗:getSectionDef(String _te_lec_key,String _sect_key) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
       	Broker.freeConnection(conn);
   	}
	return v;
	//
}
//
// 新しいセクションレコードを挿入する
public int insertSectionDef( Vector v ){
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = -1;
	//
	String QUERY = insert_SectionDef_Query(v);
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY);
		//
	}catch(Exception e){
		System.out.println("### 失敗:insertSectionDef( Vector v )" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 件数（普通は１）
}
//
// 挿入用クエリの作成
String insert_SectionDef_Query(Vector v){
	//
	String te_lec_key	= (String)(v.get(0));
   	String sect_key		= (String)(v.get(1));
	//
    String seq_number	= (String)(v.get(2));
    String title		= (String)(v.get(3));
    String content		= (String)(v.get(4));
    String note			= (String)(v.get(5));
    String ref_list		= (String)(v.get(6));
    String kadai_list	= (String)(v.get(7));
	//
	String Q = "INSERT INTO sect  VALUES (" 
	            + m1 + te_lec_key  	+ m2 
				+ m1 + sect_key  	+ m2
				+ m1 + seq_number 	+ m2
				+ m1 + title 		+ m2
				+ m1 + content 		+ m2
				+ m1 + note 		+ m2
				+ m1 + ref_list 	+ m2
				+ m1 + kadai_list 	+ m1
				+ ")";
	if(LOG.fa) LOG.println("Query = " +  Q);
	return Q;
}
//
// セクション定義レコードを更新する
public int updateSectionDef( Vector v ){
	//
	Connection   conn  = null;
   	Statement 	 stmt  = null;
	int  		 count = 0;
	//
	try {
		String QUERY = update_SectionDef_Query(v);
		//
      	conn 	= Broker.getConnection(); /** Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		count  = stmt.executeUpdate(QUERY);
	}catch(Exception e){
		System.out.println("#exwk ### 失敗:updateSectionDef( Vector v ) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/** connection を Broker に返す */
   	}
	if(LOG.fa) LOG.println("■ Database # updateSectionDef() でレコードを更新しました");
	return count;
}
//
// 更新用クエリの作成
String update_SectionDef_Query(Vector v){
	//
	String te_lec_key	= (String)(v.get(0));
   	String sect_key		= (String)(v.get(1));
	//
    String seq_number	= (String)(v.get(2));
    String title		= (String)(v.get(3));
    String content		= (String)(v.get(4));
    String note			= (String)(v.get(5));
    String ref_list		= (String)(v.get(6));
    String kadai_list	= (String)(v.get(7));
	//
	String Q = "UPDATE  sect  set  " 
				//
				+ "seq_number = "  	+ m1 + seq_number  	+ m2
				+ "title = "    	+ m1 + title 		+ m2
				+ "content = " 		+ m1 + content 		+ m2
				+ "note = "    		+ m1 + note 		+ m2
				+ "ref_list = "     + m1 + ref_list 	+ m2
				+ "kadai_list = "   + m1 + kadai_list 	+ m1
				+ " WHERE te_lec_key = " + m1 + te_lec_key + m1 + " AND sect_key = " + m1 + sect_key + m1;
				//
	if(LOG.fa) LOG.println("■ Database # update_SectionDef_Query()Query = " +  Q);
	return Q;
}
//
// セクション定義レコードを削除する
public int deleteSectionDef( String te_lec_key,String sect_key ){
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = 0;
	//
	try {
		String QUERY =  "DELETE from sect "	+ " WHERE te_lec_key = "  + m1 + te_lec_key + m1 + " AND sect_key = " + m1 + sect_key + m1;
		if(LOG.fa) LOG.println("deleteSectionDef() = " +  QUERY);
		//
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY); // ここでdeleteする
		//
	}catch(Exception e){
		System.out.println("### 失敗:deleteDefLecture( String _te_lec_key,String _sect_key ) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 件数（普通は１）
}
//============================================================
//  特定の講義（定義）に関連するセクションレコードを全て削除する
//  アプリから直接実行する
//============================================================
//
public int delete_some_SectionDefs( String te_lec_key ){
	if(LOG.fa) LOG.println("class Database #delete_some_SectionDefs() :  特定の講義（定義）に関連するセクションレコードを全て削除する の先頭です");
	if(LOG.fa) LOG.println("      te_lec_key = " + te_lec_key);
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = 0;
	//
	try {
		String QUERY =  "DELETE from sect "	+ " WHERE te_lec_key = "  + m1 + te_lec_key + m1 ;
		if(LOG.fa) LOG.println("deleteSectionDef() = " +  QUERY);
		//
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY); // ここでdeleteする
		//
	}catch(Exception e){
		System.out.println("★★［失敗］class Database #delete_some_SectionDefs() :  特定の講義（定義）に関連するセクションレコードを全て削除する に失敗しました" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 
}

//
////////////////////////////////////////////////////
//
// 　　セクション実施関係
//
////////////////////////////////////////////////////
//
/*
■講義セクション実施データ

	te_aplec_key は講義実施データのキー　teUid + '-' + aplec_key を意味する
	
		例示：　kawaba01-003
		フルキーの例示： kawaba01-003-00012

	実施にかかる日付と講義メモのデータ
-------------------------------------------------------------
create table app_sect (
   te_aplec_key     VARCHAR(16)  NOT NULL,
   sect_key         CHAR(5)   NOT NULL,
   s_mm             CHAR(2),
   s_dd             CHAR(2),
   e_mm             CHAR(2),
   e_dd             CHAR(2),
   memo             text
);
-------------------------------------------------------------
// キーは複数列に設定する
create index sect_idx on app_sect (te_aplec_key,sect_key);	
-------------------------------------------------------------
*/
// ある講義実施レコードの全てのセクション実施データを得る
public int getSectionAps(String te_aplec_key,Vector vrec){
	if(LOG.fa) LOG.println( "Database: getSectionAps() を実行します");
	Connection   conn  = null;
   	Statement 	 stmt  = null;
   	int rowCount = 0;
	try {
		String cmd 	= "SELECT * FROM app_sect  WHERE te_aplec_key = " + m1 + te_aplec_key + m1;
		if(LOG.fa) LOG.println("getDefLectures() : SQL = " + cmd);
		//
      	conn 	= Broker.getConnection(); 
		stmt 	= conn.createStatement();
		ResultSet rs = stmt.executeQuery(cmd);
		while(rs.next()){
			Vector v = new Vector(20,10);
			//
			v.add(te_aplec_key);
   			v.add(rs.getString("sect_key"));
   			v.add(rs.getString("s_mm"));
   			v.add(rs.getString("s_dd"));
   			v.add(rs.getString("e_mm"));
   			v.add(rs.getString("e_dd"));
   			v.add(rs.getString("memo"));
			//
			vrec.add(v);
			rowCount++;
			if(LOG.fa) LOG.println(rowCount);
		}
	}catch(Exception e){
		System.out.println("### 失敗: getDefLectures()" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);
   	}
	if(LOG.fa) LOG.println( "Database:getSectionAps()","セクションレコードの数＝" + String.valueOf(rowCount) );
	return rowCount;
}


//
// 特定の科目のセクション実施キーで特定されるセクション実施データをベクターに入れて返す
public Vector getSectionAp(String te_aplec_key,String sect_key){
	if(LOG.fa) LOG.println( "Database: getSectionAp() を実行します");
	//
	Vector v = new Vector(20,10);
	Connection   conn   = null;
   	Statement 	 stmt   = null;
	//
	int rowCount = 0;
	try {
		String cmd 		= " SELECT * FROM app_sect  WHERE te_aplec_key = " + m1 + te_aplec_key + m1 + "  AND sect_key = " + m1 + sect_key + m1;
		if(LOG.fa) LOG.println("getSectionAp() : SQL CMD = " + cmd);
       	// Broker からDB Connection を得る
       	conn 		 = Broker.getConnection();/** Broker からDB Connection を得る */
		stmt 		 = conn.createStatement();
		ResultSet rs = stmt.executeQuery(cmd);
		while(rs.next()){
			v.add(te_aplec_key);
   			v.add(sect_key);
			//
   			v.add(rs.getString("s_mm"));
   			v.add(rs.getString("s_dd"));
   			v.add(rs.getString("e_mm"));
   			v.add(rs.getString("e_dd"));
   			v.add(rs.getString("memo"));
			//
			++rowCount;
			break;
		}
		//
	}catch(Exception e){
		System.out.println("#### 失敗:getSectionAp(String _te_aplec_key,String _sect_key) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
       	Broker.freeConnection(conn);/** Broker に Connection を戻す */
   	}
	if(LOG.fa) LOG.println( "Database:getSectionAp()実行結果","セクションレコードの数＝" + String.valueOf(rowCount) );
	return v;
	//
}
public SectionRecord getSectionRecord(String te_aplec_key,String sect_key){
	if(LOG.fa) LOG.displn( "■start: getSectionRecord("+te_aplec_key+", " + sect_key+")");
	//
	Connection   conn   = null;
   	Statement 	 stmt   = null;
	//
   	SectionRecord rec = null;
	try {
		
		String cmd 		= " SELECT * FROM app_sect  WHERE te_aplec_key = " + m1 + te_aplec_key + m1 + "  AND sect_key = " + m1 + sect_key + m1;
		conn 		 = Broker.getConnection();/** Broker からDB Connection を得る */
		if(conn!=null) {
			stmt 		 = conn.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			while (rs.next()) {
				String month = rs.getString("s_mm"); 
				String day	 = rs.getString("s_dd");
				rec =  new SectionRecord(te_aplec_key, sect_key, month, day, "", "", "");
				break;
			}
		}else {
			if(LOG.tr) LOG.displn( "Conn is null !!!");
		}
	}catch(Exception e){
		if(LOG.tr) LOG.displn("#### 失敗:+getSectionRecord("+te_aplec_key+", " + sect_key+")");
		if(LOG.tr) LOG.displn(e.toString());
		rec =null;
	}finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
       	Broker.freeConnection(conn);
   	}
	return rec;
}
//
// 新しいセクションレ実施コードを挿入する
public int insertSectionAp( Vector v ){
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = -1;
	//
	String QUERY = insert_SectionAp_Query(v);
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY);
		//
	}catch(Exception e){
		System.out.println("### 失敗:insertSectionAp( Vector v )" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 件数（普通は１）
}
//
// 挿入用クエリの作成
String insert_SectionAp_Query(Vector v){
	//
	String te_aplec_key	= (String)(v.get(0));
   	String sect_key		= (String)(v.get(1));
	//
    String s_mm			= (String)(v.get(2));
    String s_dd			= (String)(v.get(3));
    String e_mm			= (String)(v.get(4));
    String e_dd			= (String)(v.get(5));
    String memo			= (String)(v.get(6));
	//
	String Q = "INSERT INTO app_sect  VALUES (" 
	            + m1 + te_aplec_key	+ m2 
				+ m1 + sect_key  	+ m2
				+ m1 + s_mm 		+ m2
				+ m1 + s_dd 		+ m2
				+ m1 + e_mm 		+ m2
				+ m1 + e_dd 		+ m2
				+ m1 + memo 		+ m1
				+ ")";
	if(LOG.fa) LOG.println("Query = " +  Q);
	return Q;
}
//
// セクション実施レコードを更新する
public int updateSectionAp( Vector v ){
	Connection   conn  = null;
   	Statement 	 stmt  = null;
	int  		 count = 0;
	//
	String QUERY = update_SectionAp_Query(v);
	//
	try {
      	conn 	= Broker.getConnection(); /** Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		count  = stmt.executeUpdate(QUERY);
	}catch(Exception e){
		System.out.println("#exwk ### 失敗:updateSectionAp( Vector v ) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/** connection を Broker に返す */
   	}
	return count;
}
//
// 更新用クエリの作成
String update_SectionAp_Query(Vector v){
	//
	String te_aplec_key	= (String)(v.get(0));
   	String sect_key		= (String)(v.get(1));
	//
    String s_mm			= (String)(v.get(2));
    String s_dd			= (String)(v.get(3));
    String e_mm			= (String)(v.get(4));
    String e_dd			= (String)(v.get(5));
    String memo			= (String)(v.get(6));
	//
	String Q = "UPDATE  app_sect  set  " 
				//
				+ "s_mm = "  + m1 + s_mm  + m2
				+ "s_dd = "  + m1 + s_dd  + m2
				+ "e_mm = "  + m1 + e_mm  + m2
				+ "e_dd = "  + m1 + e_dd  + m2
				+ "memo = "  + m1 + memo  + m1
				+ " WHERE te_aplec_key = " + m1 + te_aplec_key + m1 + " AND sect_key = " + m1 + sect_key + m1;
				//
	if(LOG.fa) LOG.println("Query = " +  Q);
	return Q;
}
//
// セクション実施レコードを削除する
public int deleteSectionAp( String _te_aplec_key,String _sect_key ){
	String te_aplec_key   	= _te_aplec_key.trim();
	String sect_key			= _sect_key.trim();
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = 0;
	//
	String QUERY =  "DELETE from app_sect "
					+ " WHERE te_aplec_key = "  + m1 + te_aplec_key + m1 + " AND sect_key = " + m1 + sect_key + m1;
	//
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY); // ここでdeleteする
		//
	}catch(Exception e){
		System.out.println("### 失敗:deleteSectionAp( String _te_aplec_key,String _sect_key )" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 件数（普通は１）
}
//=================================================================
//  特定の講義（実施）に関連するセクション実施レコードを削除する
//=================================================================
//
public int delete_some_SectionAps( String _te_aplec_key){
	if(LOG.fa) LOG.println("class Database #delete_some_SectionAps() : 特定の講義（実施）に関連するセクション実施レコードを削除する の先頭です");
	if(LOG.fa) LOG.println("      te_aplec_key = " + _te_aplec_key);
	//
	String te_aplec_key   	= _te_aplec_key.trim();
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = 0;
	//
	String QUERY =  "DELETE from app_sect "
					+ " WHERE te_aplec_key = "  + m1 + te_aplec_key + m1;
	//
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY); // ここでdeleteする
		//
	}catch(Exception e){
		System.out.println("### 失敗:delete_some_SectionAps()" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 
}
//
////////////////////////////////////////////////////
//
// 　　課題定義関係
//
////////////////////////////////////////////////////
//
/*

■課題データ

  講義定義の中で作成するデータ
	
		teUid       = kawaba01 ------- 12 桁．一意なユーザーキー
		lec_key     = 003    --------- 3 桁．講義の連番
		kadai_key   = 000012 --------- 6 桁．課題の連番
		
		te_lect_key = kawaba01-003 --- 16 桁．　
		
		full key は　==>  kawaba01-003-000012   --- 23 桁
	
------------------------------------------------------------------
	<<< 2002.8.4 変更 >>>
	
create table kadai (
 	te_lec_key         VARCHAR(16)  NOT NULL,
 	kadai_key          CHAR(6)   NOT NULL,
 	seq_number         CHAR(2),
 	shubetsu           CHAR(1),
 	title              VARCHAR(200),
 	content            text
);
	
// キーは複数列に設定する
create index kadai_idx on kadai (te_lec_key,kadai_key);


*/
//
// ある講義に属する全ての課題を得る
public int getKadaiDefs(String te_lec_key,Vector vrec){
	Connection   conn  = null;
   	Statement 	 stmt  = null;
   	int rowCount = 0;
	try {
		String cmd 	= "SELECT * FROM kadai WHERE te_lec_key = " + m1 + te_lec_key + m1;
		if(LOG.fa) LOG.println("getKadaiDefs() : SQL = " + cmd);
		//
      	conn 	= Broker.getConnection(); 
		stmt 	= conn.createStatement();
		ResultSet rs = stmt.executeQuery(cmd);
		while(rs.next()){
			Vector v = new Vector(20,10);
			//
			v.add(te_lec_key);
   			v.add(rs.getString("kadai_key"));
   			v.add(rs.getString("seq_number"));
   			v.add(rs.getString("shubetsu"));
			v.add(rs.getString("title"));
   			v.add(rs.getString("content"));
			//
			vrec.add(v);
			rowCount++;
			if(LOG.fa) LOG.println(rowCount);
		}
	}catch(Exception e){
		System.out.println("### 失敗: getReferenceDefs()" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);
   	}
	if(LOG.fa) LOG.println( "Database:getKadaiDefs()","□課題レコードの数＝" + String.valueOf(rowCount) );
	return rowCount;
}
//
//
// 特定の課題定義データをベクターに入れて返す
public Vector getKadaiDef(String _te_lec_key,String _kadai_key){
	String te_lec_key   = _te_lec_key.trim();
	String kadai_key		 = _kadai_key.trim();
	//
	String cmd 		= " SELECT * FROM kadai  WHERE te_lec_key = " + m1 + te_lec_key + m1 + " AND kadai_key = " + m1 + kadai_key + m1;
	if(LOG.fa) LOG.println("getSectionDef() : SQL CMD = " + cmd);
	Connection   conn   = null;
   	Statement 	 stmt   = null;
	//
	Vector v = new Vector(20,10);
	try {
       	// Broker からDB Connection を得る
       	conn 		 = Broker.getConnection();/** Broker からDB Connection を得る */
		stmt 		 = conn.createStatement();
		ResultSet rs = stmt.executeQuery(cmd);
		int rowCount = 0;
		while(rs.next()){
			v.add(te_lec_key);
   			v.add(kadai_key);
			//
   			v.add(rs.getString("seq_number"));
			v.add(rs.getString("shubetsu"));
   			v.add(rs.getString("title"));
   			v.add(rs.getString("content"));
			//
			++rowCount;
			break;
		}
		//
	}catch(Exception e){
		System.out.println("#### 失敗: getKadaiDef(String _te_lec_key,String _kadai_key) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
       	Broker.freeConnection(conn);/** Broker に Connection を戻す */
   	}
	return v;
	//
}
//
// 新しい課題レコードを挿入する
public int insertKadaiDef( Vector v ){
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = -1;
	//
	String QUERY = insert_KadaiDef_Query(v);
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY);
		//
	}catch(Exception e){
		System.out.println("### 失敗:insertKadaiDef( Vector v )" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 件数（普通は１）
}
//
// 挿入用クエリの作成
String insert_KadaiDef_Query(Vector v){
	//
	String te_lec_key	= (String)(v.get(0));
   	String kadai_key		= (String)(v.get(1));
	//
    String seq_number		= (String)(v.get(2));
    String shubetsu			= (String)(v.get(3));
    String title			= (String)(v.get(4));
    String content			= (String)(v.get(5));
	//
	String Q = "INSERT INTO kadai  VALUES (" 
	            + m1 + te_lec_key  		+ m2 
				+ m1 + kadai_key  		+ m2
				+ m1 + seq_number 		+ m2
				+ m1 + shubetsu 		+ m2
				+ m1 + title 			+ m2
				+ m1 + content 			+ m1
				+ ")";
	if(LOG.fa) LOG.println("insert_KadaiDef_Query = " +  Q);
	return Q;
}
//
// 課題定義レコードを更新する
public int updateKadaiDef( Vector v ){

	Connection   conn  = null;
   	Statement 	 stmt  = null;
	int  		 count = 0;
	//
	String QUERY = update_kadaiDef_Query(v);
	//
	try {
      	conn 	= Broker.getConnection(); /** Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		count  = stmt.executeUpdate(QUERY);
	}catch(Exception e){
		System.out.println("#exwk ### 失敗:updateKadaiDef( Vector v ) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/** connection を Broker に返す */
   	}
	if(LOG.fa) LOG.println("■ Database #updateKadaiDef() done=" + count);
	return count;
}
//
// 更新用クエリの作成
String update_kadaiDef_Query(Vector v){
	//
	String te_lec_key		= (String)(v.get(0));
   	String kadai_key		= (String)(v.get(1));
	//
    String seq_number		= (String)(v.get(2));
    String shubetsu			= (String)(v.get(3));
    String title			= (String)(v.get(4));
    String content			= (String)(v.get(5));
	//
	String Q = "UPDATE  kadai  set  " 
				//
				+ "seq_number = "  	+ m1 + seq_number  	+ m2
				+ "shubetsu = "     + m1 + shubetsu 	+ m2
				+ "title = " 		+ m1 + title 		+ m2
				+ "content = "    	+ m1 + content 		+ m1
				+ " WHERE te_lec_key = " + m1 + te_lec_key + m1 + " AND kadai_key = " + m1 + kadai_key + m1;
				//
	
	if(LOG.fa){ 
		String Q2 = "UPDATE  kadai  set  " 
				//
				+ "seq_number = "  	+ m1 + seq_number  	+ m2
				+ "shubetsu = "     + m1 + shubetsu 	+ m2
				+ "title = " 		+ m1 + title 		+ m2
				+ "content = "    	+ m1 + "・・・" 	+ m1
				+ " WHERE te_lec_key = " + m1 + te_lec_key + m1 + " AND kadai_key = " + m1 + kadai_key + m1;
		LOG.println("■ Database #update_kadaiDef_Query() = " +  Q2);
	}
	return Q;
}
//
// 課題定義レコードを削除する
public int deleteKadaiDef( String _te_lec_key,String _kadai_key ){
	String te_lec_key  = _te_lec_key.trim();
	String kadai_key		= _kadai_key.trim();
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = 0;
	//
	String QUERY =  "DELETE from kadai "
					+ " WHERE te_lec_key = "  + m1 + te_lec_key + m1 + " AND kadai_key = " + m1 + kadai_key + m1;
	//
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY); // ここでdeleteする
		//
	}catch(Exception e){
		System.out.println("### 失敗:deleteKadaiDef( String _te_lec_key,String _kadai_key ) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 件数（普通は１）
}
//=========================================================================
// 特定の講義（定義）に関連する課題レコードを全て削除する
//=========================================================================
//
public int delete_some_KadaiDefs( String _te_lec_key){
	if(LOG.fa) LOG.println("class Database #delete_some_KadaiDefs() :  特定の講義（定義）に関連する課題レコードを全て削除する の先頭です");
	if(LOG.fa) LOG.println("      te_lec_key = " + _te_lec_key);
	//
	String te_lec_key  = _te_lec_key.trim();
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = 0;
	//
	String QUERY =  "DELETE from kadai "
					+ " WHERE te_lec_key = "  + m1 + te_lec_key + m1;
	//
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY); // ここでdeleteする
		//
	}catch(Exception e){
		System.out.println("★★［失敗］class Database #delete_some_KadaiDefs() :  特定の講義（定義）に関連する課題レコードを全て削除する に失敗しました" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 
}
//
////////////////////////////////////////////////////
//
// 　　課題実施関係
//
////////////////////////////////////////////////////
//
/*
■app_kadai テーブルは、時間割に割り付けた講義の課題についての補足データ
	
		te_oplec_key で一覧リストを得ることができるが、
	 直接キーでの  検索を行う処理が中心になる
		 
  saiten_flag  0 = 未採点
    	       1 = 採点済み
	
--------------------------------------------------------------------------
		<<< 2002.8.4 変更 >>>
	
	create table app_kadai (
 		te_aplec_key       VARCHAR(16)  NOT NULL,
 		kadai_key          CHAR(6)   NOT NULL,
 		saiten_flag        CHAR(1),
 		s_yyyy             CHAR(4),
 		s_month            CHAR(2),
 		s_day              CHAR(2),
 		s_hour             CHAR(2),
 		s_minute           CHAR(2),
 		e_yyyy             CHAR(4),
 		e_month            CHAR(2),
 		e_day              CHAR(2),
 		e_hour             CHAR(2),
 		e_minute           CHAR(2),
 		passwd			   VARCHAR(20)
	);
	
	// キーは複数列に設定する
	create index app_kadai_idx on app_kadai (te_aplec_key,kadai_key);
*/
//
// ある講義の全ての課題実施データを得る
public int getKadaiAps(String te_aplec_key,Vector vrec){
	if(LOG.fa) LOG.println( "Database: getKadaiAps() を実行します");
	//
	Connection   conn   = null;
   	Statement 	 stmt   = null;
	//
	int rowCount = 0;
	try {
		String cmd 		= " SELECT * FROM app_kadai WHERE te_aplec_key = " + m1 + te_aplec_key + m1;
		if(LOG.fa) LOG.println("getKadaiAps() : SQL CMD = " + cmd);
       	//
       	conn 		 = Broker.getConnection();
		stmt 		 = conn.createStatement();
		ResultSet rs = stmt.executeQuery(cmd);
		while(rs.next()){
   			Vector v = new Vector(20,10);
			v.add(te_aplec_key);
   			v.add(rs.getString("kadai_key"));
			//
   			v.add(rs.getString("saiten_flag"));
   			v.add(rs.getString("s_yyyy"));
   			v.add(rs.getString("s_month"));
   			v.add(rs.getString("s_day"));
   			v.add(rs.getString("s_hour"));
   			v.add(rs.getString("s_minute"));
			//
   			v.add(rs.getString("e_yyyy"));
   			v.add(rs.getString("e_month"));
   			v.add(rs.getString("e_day"));
   			v.add(rs.getString("e_hour"));
   			v.add(rs.getString("e_minute"));
			//
			v.add(rs.getString("passwd"));
			//
			vrec.add(v);
			++rowCount;
			//break;
		}
		//
	}catch(Exception e){
		System.out.println("#### 失敗:getKadaiAps(String _te_aplec_key,Vector v)" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
       	Broker.freeConnection(conn);/** Broker に Connection を戻す */
   	}
	if(LOG.fa) LOG.println( "Database: getKadaiAps() の実行結果 " + String.valueOf(rowCount) + " 件");
	return rowCount;
	//
}
//
//
// 特定の課題実施データをベクターに入れて返す
// 
//
public int getKadaiAp(String _te_aplec_key,String _kadai_key,Vector v){
	String te_aplec_key  = _te_aplec_key.trim();
	String kadai_key		  = _kadai_key.trim();
	//
	String cmd 		= " SELECT * FROM app_kadai  WHERE te_aplec_key = " + m1 + te_aplec_key + m1 + " AND kadai_key = " + m1 + kadai_key + m1;
	if(LOG.fa) LOG.println("getKadaiAp() : SQL CMD = " + cmd);
	Connection   conn   = null;
   	Statement 	 stmt   = null;
	//
	int rowCount = 0;
	if(v==null)  v = new Vector(20,10);// 念のため
	try {
       	// Broker からDB Connection を得る
       	conn 		 = Broker.getConnection();/** Broker からDB Connection を得る */
		stmt 		 = conn.createStatement();
		ResultSet rs = stmt.executeQuery(cmd);
		while(rs.next()){
			v.add(te_aplec_key);
   			v.add(kadai_key);
			//
   			v.add(rs.getString("saiten_flag"));
   			v.add(rs.getString("s_yyyy"));
   			v.add(rs.getString("s_month"));
   			v.add(rs.getString("s_day"));
   			v.add(rs.getString("s_hour"));
   			v.add(rs.getString("s_minute"));
			//
   			v.add(rs.getString("e_yyyy"));
   			v.add(rs.getString("e_month"));
   			v.add(rs.getString("e_day"));
   			v.add(rs.getString("e_hour"));
   			v.add(rs.getString("e_minute"));
			//
			v.add(rs.getString("passwd"));
			//
			++rowCount;
			break;
		}
		//
	}catch(Exception e){
		System.out.println("#### 失敗: getKadaiAp(String _te_aplec_key,String _kadai_key) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
       	Broker.freeConnection(conn);/** Broker に Connection を戻す */
   	}
	return rowCount;
	//
}
//
// 新しい課題レコードを挿入する
public int insertKadaiAp( Vector v ){
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = -1;
	//
	String QUERY = insert_KadaiAp_Query(v);
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY);
		//
	}catch(Exception e){
		System.out.println("### 失敗:insertKadaiAp( Vector v )" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 件数（普通は１）
}
//
// 挿入用クエリの作成
String insert_KadaiAp_Query(Vector v){
	//
	String te_aplec_key = (String)(v.get(0));
   	String kadai_key		 = (String)(v.get(1));
	//
    String saiten_flag		= (String)(v.get(2));
    String s_yyyy			= (String)(v.get(3));
    String s_month			= (String)(v.get(4));
    String s_day			= (String)(v.get(5));
    String s_hour			= (String)(v.get(6));
    String s_minute			= (String)(v.get(7));
	//
    String e_yyyy			= (String)(v.get(8));
    String e_month			= (String)(v.get(9));
    String e_day			= (String)(v.get(10));
    String e_hour			= (String)(v.get(11));
    String e_minute			= (String)(v.get(12));
	String passwd			= (String)(v.get(13));
	//
	String Q = "INSERT INTO app_kadai  VALUES (" 
	            + m1 + te_aplec_key + m2 
				+ m1 + kadai_key  		 + m2
				+ m1 + saiten_flag 		 + m2
				+ m1 + s_yyyy 		  	 + m2
				+ m1 + s_month 			 + m2
				+ m1 + s_day 			 + m2
				+ m1 + s_hour 			 + m2
				+ m1 + s_minute 		 + m2
				+ m1 + e_yyyy 			 + m2
				+ m1 + e_month 			 + m2
				+ m1 + e_day 			 + m2
				+ m1 + e_hour 			 + m2
				+ m1 + e_minute 		 + m2
				+ m1 + passwd			 + m1
				+ ")";
	if(LOG.fa) LOG.println("insert_KadaiAp_Query = " +  Q);
	return Q;
}
//
// 課題実施レコードを更新する
public int updateKadaiAp( Vector v ){
	Connection   conn  = null;
   	Statement 	 stmt  = null;
	int  		 count = 0;
	//
	String QUERY = update_kadaiAp_Query(v);
	//
	try {
      	conn 	= Broker.getConnection(); /** Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		count  = stmt.executeUpdate(QUERY);
	}catch(Exception e){
		System.out.println("#exwk ### 失敗:updateKadaiAp( Vector v ) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/** connection を Broker に返す */
   	}
	return count;
}
//
// 更新用クエリの作成
String update_kadaiAp_Query(Vector v){
	//
	String te_aplec_key	= (String)(v.get(0));
   	String kadai_key			= (String)(v.get(1));
	//
    String saiten_flag		= (String)(v.get(2));
    String s_yyyy			= (String)(v.get(3));
    String s_month			= (String)(v.get(4));
    String s_day			= (String)(v.get(5));
    String s_hour			= (String)(v.get(6));
    String s_minute			= (String)(v.get(7));
	//
    String e_yyyy			= (String)(v.get(8));
    String e_month			= (String)(v.get(9));
    String e_day			= (String)(v.get(10));
    String e_hour			= (String)(v.get(11));
    String e_minute			= (String)(v.get(12));
	//
	String passwd			= (String)(v.get(13));
	//
	String Q = "UPDATE  app_kadai  set  " 
				//
				+ "saiten_flag = "  + m1 + saiten_flag  + m2
				+ "s_yyyy = "    	+ m1 + s_yyyy 		+ m2
				+ "s_month = " 		+ m1 + s_month 		+ m2
				+ "s_day = "    	+ m1 + s_day 		+ m2
				+ "s_hour = "    	+ m1 + s_hour 		+ m2
				+ "s_minute = " 	+ m1 + s_minute 	+ m2
				+ "e_yyyy = "    	+ m1 + e_yyyy 		+ m2
				+ "e_month = " 		+ m1 + e_month 		+ m2
				+ "e_day = "    	+ m1 + e_day 		+ m2
				+ "e_hour = "    	+ m1 + e_hour 		+ m2
				+ "e_minute = " 	+ m1 + e_minute 	+ m2
				+ "passwd = " 		+ m1 + passwd 		+ m1
				//
				+ " WHERE te_aplec_key = " + m1 + te_aplec_key + m1 + " AND kadai_key = " + m1 + kadai_key + m1;
				//
	if(LOG.fa) LOG.println("update_kadaiAp_Query = " +  Q);
	return Q;
}
//
// 課題実施レコードを削除する
public int deleteKadaiAp( String _te_aplec_key,String _kadai_key ){
	String te_aplec_key  = _te_aplec_key.trim();
	String kadai_key		  = _kadai_key.trim();
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = 0;
	//
	String QUERY =  "DELETE from app_kadai "
					+ " WHERE te_aplec_key = "  + m1 + te_aplec_key + m1 + " AND kadai_key = " + m1 + kadai_key + m1;
	//
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY); // ここでdeleteする
		//
	}catch(Exception e){
		System.out.println("### 失敗:deleteKadaiAp( String _te_aplec_key,String _kadai_key ) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 件数（普通は１）
}
//=================================================================
//  特定の講義（実施）に関連する課題実施レコードレコードを削除する
//=================================================================
//
public int delete_some_KadaiAps( String _te_aplec_key){
	if(LOG.fa) LOG.println("class Database #delete_some_KadaiAps() : 特定の講義（実施）に関連する課題実施レコードレコードを削除する の先頭です");
	if(LOG.fa) LOG.println("      te_aplec_key = " + _te_aplec_key);
	//
	String te_aplec_key  = _te_aplec_key.trim();
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = 0;
	//
	String QUERY =  "DELETE from app_kadai "
					+ " WHERE te_aplec_key = "  + m1 + te_aplec_key + m1;
	//
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY); // ここでdeleteする
		//
	}catch(Exception e){
		System.out.println("### 失敗:delete_some_KadaiAps() " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 
}


//
////////////////////////////////////////////////////
//
// 　　資料定義関係
//
////////////////////////////////////////////////////
//
/*
	■資料データ
	
	　講義セクション定義の中で作成するデータ
	
	 teUid       = kawaba01 ------- 12 桁．一意なユーザーキー
	 lec_key     = 003    --------- 3 桁．講義の連番
	 ref_key     = 000012 --------- 6 桁．資料の連番
	 
	 te_lec_key = kawaba01-003 ----------- 16 桁．　
	 full key は　==>  kawaba01-003-000012 ---- 23 桁
	
	  shubetsu:   1 = ウェブ資料
		          2 = ビデオ
				  3 = 書籍・雑誌
				  4 = プリント等
				  5 = その他
----------------------------------------------------------------
		<<< 2002.8.4 変更 >>>
	
	create table reference (
 		te_lec_key         VARCHAR(16)  NOT NULL,
 		ref_key            CHAR(6)   NOT NULL,
 		seq_number         CHAR(2),
 		shubetsu           CHAR(1),
 		title              VARCHAR(200),
 		url                VARCHAR(200)
	);
	
	// キーは複数列に設定する
	create index reference_idx on reference (te_lec_key,ref_key);
*/
//
// ある科目に属する全ての資料レコードを得る
public int getReferenceDefs(String te_lec_key,Vector vrec){

	Connection   conn  = null;
   	Statement 	 stmt  = null;
   	int rowCount = 0;
	try {
		String cmd 	= "SELECT * FROM reference  WHERE te_lec_key = " + m1 + te_lec_key + m1;
		if(LOG.fa) LOG.println("getReferenceDefs() : SQL = " + cmd);
		//
      	conn 	= Broker.getConnection(); 
		stmt 	= conn.createStatement();
		ResultSet rs = stmt.executeQuery(cmd);
		while(rs.next()){
			Vector v = new Vector(20,10);
			//
			v.add(te_lec_key);
   			v.add(rs.getString("ref_key"));
   			v.add(rs.getString("seq_number"));
   			v.add(rs.getString("shubetsu"));
   			v.add(rs.getString("title"));
   			v.add(rs.getString("url"));
			//
			vrec.add(v);
			rowCount++;
			if(LOG.fa) LOG.println(rowCount);
		}
	}catch(Exception e){
		System.out.println("### 失敗: getReferenceDefs()" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);
   	}
	if(LOG.fa) LOG.println( "Database:getReferenceDefs()","実施レコードの数＝" + String.valueOf(rowCount) );
	return rowCount;
}
//
//
// 特定の資料レコードをキーで検索して返す
public Vector getReferenceDef(String te_lec_key,String ref_key){
	//
	Connection   conn   = null;
   	Statement 	 stmt   = null;
	//
	Vector v = new Vector(20,10);
	try {
		String cmd 		= " SELECT * FROM reference  WHERE te_lec_key = " + m1 + te_lec_key + m1 + " AND ref_key = " + m1 + ref_key + m1;
		if(LOG.fa) LOG.println("getReferenceDef() : SQL CMD = " + cmd);
       	//
       	conn 		 = Broker.getConnection();
		stmt 		 = conn.createStatement();
		ResultSet rs = stmt.executeQuery(cmd);
		int rowCount = 0;
		while(rs.next()){
			v.add(te_lec_key);
   			v.add(ref_key);
			//
   			v.add(rs.getString("seq_number"));
   			v.add(rs.getString("shubetsu"));
   			v.add(rs.getString("title"));
   			v.add(rs.getString("url"));
			//
			++rowCount;
			break;
		}
		//
	}catch(Exception e){
		System.out.println("#### 失敗: getReferenceDef(String _te_lec_key,String _ref_key)" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
       	Broker.freeConnection(conn);/** Broker に Connection を戻す */
   	}
	return v;
	//
}
//
// 新しい資料レコードを挿入する
public int insertReferenceDef( Vector v ){
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = -1;
	//
	String QUERY = insert_ReferenceDef_Query(v);
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY);
		//
	}catch(Exception e){
		System.out.println("### 失敗:insertReferenceDef( Vector v )" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 件数（普通は１）
}
//
// 挿入用クエリの作成
String insert_ReferenceDef_Query(Vector v){
	//
	String te_lec_key	= (String)(v.get(0));
   	String ref_key			= (String)(v.get(1));
	//
    String seq_number		= (String)(v.get(2));
    String shubetsu			= (String)(v.get(3));
    String title			= (String)(v.get(4));
    String url				= (String)(v.get(5));
	//
	String Q = "INSERT INTO reference  VALUES (" 
	            + m1 + te_lec_key  + m2 
				+ m1 + ref_key  		+ m2
				+ m1 + seq_number 		+ m2
				+ m1 + shubetsu 		+ m2
				+ m1 + title 			+ m2
				+ m1 + url	 			+ m1
				+ ")";
	if(LOG.fa) LOG.println("insert_ReferenceDef_Query = " +  Q);
	return Q;
}
//
// 資料レコードを更新する
public int updateReferenceDef( Vector v ){
	Connection   conn  = null;
   	Statement 	 stmt  = null;
	int  		 count = 0;
	//
	String QUERY = update_ReferenceDef_Query(v);
	//
	try {
      	conn 	= Broker.getConnection(); /** Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		count  = stmt.executeUpdate(QUERY);
	}catch(Exception e){
		System.out.println("#exwk ### 失敗:updateReferenceDef( Vector v ) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/** connection を Broker に返す */
   	}
	return count;
}
//
// 更新用クエリの作成
String update_ReferenceDef_Query(Vector v){
	//
	String te_lec_key	= (String)(v.get(0));
   	String ref_key			= (String)(v.get(1));
	//
    String seq_number		= (String)(v.get(2));
    String shubetsu			= (String)(v.get(3));
    String title			= (String)(v.get(4));
    String url				= (String)(v.get(5));
	//
	String Q = "UPDATE  reference  set  " 
				//
				+ "seq_number = "  	+ m1 + seq_number  	+ m2
				+ "shubetsu = "     + m1 + shubetsu 	+ m2
				+ "title = " 		+ m1 + title 		+ m2
				+ "url = "    		+ m1 + url	 		+ m1
				
				+ " WHERE te_lec_key = " + m1 + te_lec_key + m1 + " AND ref_key = " + m1 + ref_key + m1;
				//
	if(LOG.fa) LOG.println("update_ReferenceDef_Query = " +  Q);
	return Q;
}
//
// 資料レコードを削除する
public int deleteReferenceDef( String _te_lec_key,String _ref_key ){
	String te_lec_key  = _te_lec_key.trim();
	String ref_key			= _ref_key.trim();
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = 0;
	//
	String QUERY =  "DELETE from reference "
					+ " WHERE te_lec_key = "  + m1 + te_lec_key + m1 + " AND ref_key = " + m1 + ref_key + m1;
	//
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY); // ここでdeleteする
		//
	}catch(Exception e){
		System.out.println("### 失敗:deleteReferenceDef( String _te_lec_key,String _ref_key ) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 件数（普通は１）
}
//
//=========================================================================
// 特定の講義（定義）に関連する資料レコードを全て削除する
//=========================================================================
//
public int delete_some_ReferenceDefs( String _te_lec_key ){
	if(LOG.fa) LOG.println("class Database #delete_some_ReferenceDefs() :   特定の講義（定義）に関連する資料レコードを全て削除する の先頭です");
	if(LOG.fa) LOG.println("      te_lec_key = " + _te_lec_key);
	//
	String te_lec_key  = _te_lec_key.trim();
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = 0;
	//
	String QUERY =  "DELETE from reference "
					+ " WHERE te_lec_key = "  + m1 + te_lec_key + m1;
	//
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY); // ここでdeleteする
		//
	}catch(Exception e){
		System.out.println("★★［失敗］class Database #delete_some_ReferenceDefs() :   特定の講義（定義）に関連する資料レコードを全て削除する に失敗しました" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 
}
//
////////////////////////////////////////////////////
//
// 　　キー値関係
//
////////////////////////////////////////////////////
//
/*
■個人用キーの値
	
		キーは連番である
		１つキーを取り出すたびに、値を１増やしておく
		１つキーを取り出すたびに、「次の値セット」としてレコードをＣＳＶで書き出しておく
		
			teUid    ------- 教員のユーザＩＤ（システムで完全に一意）
			lec ------------ 講義定義キー
			appled --------- 講義実施キー
			sect ----------- セクションキー
			kadai ---------- 課題キー
			ref ------------ 資料キー
		faq ------------ ＦＡＱキー
			
		セクションキーと課題定義キーは定義と実施では同じものを使う．
		上位に付加するのが講義キーか講義実施キーかで区別できるから．
------------------------------------------------------------
create table key_gen (
	teUid           VARCHAR(12)  PRIMARY KEY,
	lec             CHAR(3)  DEFAULT '001',
	aplec           CHAR(3)  DEFAULT '001',
	sect            CHAR(5)  DEFAULT '00001',
	kadai           CHAR(6)  DEFAULT '000001',
	ref             CHAR(6)  DEFAULT '000001'
	faq             CHAR(6)  DEFAULT '000001'
); 	
------------------------------------------------------------
*/
// キーレコードを得る
public Vector getKeyGen(String _teUid, Vector v){
	String teUid  = _teUid.trim();
	//
	String cmd 		= " SELECT * FROM key_gen  WHERE teUid = " + m1 + teUid + m1;
	if(LOG.fa) LOG.println("class Databese #getKeyGen() : SQL = " + cmd);
	Connection   conn   = null;
   	Statement 	 stmt   = null;
	//
	try {
       	// Broker からDB Connection を得る
       	conn 		 = Broker.getConnection();/** Broker からDB Connection を得る */
		stmt 		 = conn.createStatement();
		ResultSet rs = stmt.executeQuery(cmd);
		int rowCount = 0;
		while(rs.next()){
			v.add(teUid);
			//
   			v.add(rs.getString("lec"));
   			v.add(rs.getString("aplec"));
   			v.add(rs.getString("sect"));
   			v.add(rs.getString("kadai"));
   			v.add(rs.getString("ref"));
   			v.add(rs.getString("faq"));
			//
			++rowCount;
			break;
		}
		//
	}catch(Exception e){
		System.out.println("#### 失敗: ggetKeyGen(String _teUid, Vector rec)" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
       	Broker.freeConnection(conn);/** Broker に Connection を戻す */
   	}
	return v;
	//
}
//
// キーレコードを更新する
public int updateKeyGen( String _teUid, Vector v ){
	String teUid  = _teUid.trim();
	//
	Connection   conn  = null;
   	Statement 	 stmt  = null;
	int  		 count = 0;
	//
	String QUERY = update_KeyGen_Query(v);
	//
	try {
      	conn 	= Broker.getConnection(); /** Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		count  = stmt.executeUpdate(QUERY);
	}catch(Exception e){
		System.out.println("#exwk ### 失敗:updateKeyGen( String _teUid, Vector v ) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/** connection を Broker に返す */
   	}
	return count;
}
//
// 更新用クエリの作成
String update_KeyGen_Query(Vector v){
	//
	String teUid	= (String)(v.get(0));
	//
   	String lec		= (String)(v.get(1));
    String aplec	= (String)(v.get(2));
    String sect		= (String)(v.get(3));
    String kadai	= (String)(v.get(4));
    String ref		= (String)(v.get(5));
    String faq		= (String)(v.get(6));
	//
	String Q = "UPDATE  key_gen  set  " 
				//
				+ "lec = "  	+ m1 + lec  	+ m2
				+ "aplec = "    + m1 + aplec 	+ m2
				+ "sect = " 	+ m1 + sect 	+ m2
				+ "kadai = "    + m1 + kadai	+ m2
				+ "ref = "    	+ m1 + ref	 	+ m2
				+ "faq = "    	+ m1 + faq	 	+ m1
				
				+ " WHERE teUid = " + m1 + teUid + m1;
				//
	if(LOG.fa) LOG.println("class Databese #update_KeyGen_Query() : Q =" +  Q);
	return Q;
}
int insertKeyGen( String _teUid, Vector rec ){
	
	return 1;
}
//
// 新しいキーレコードを挿入する
public int insertKeyGen( Vector v ){
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = -1;
	//
	String QUERY = insert_KeyGen_Query(v);
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY);
		//
	}catch(Exception e){
		System.out.println("### 失敗:insertKeyGen( Vector v )" + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 件数（普通は１）
}
//
// 挿入用クエリの作成
String insert_KeyGen_Query(Vector v){
	//
	String teUid	= (String)(v.get(0));
	//
   	String lec		= (String)(v.get(1));
    String aplec	= (String)(v.get(2));
    String sect		= (String)(v.get(3));
    String kadai	= (String)(v.get(4));
    String ref		= (String)(v.get(5));
    String faq		= (String)(v.get(6));
	//
	String Q = "INSERT INTO key_gen  VALUES (" 
	            + m1 + teUid  	+ m2 
	            + m1 + lec  	+ m2 
				+ m1 + aplec  	+ m2
				+ m1 + sect 	+ m2
				+ m1 + kadai 	+ m2
				+ m1 + ref 		+ m2
				+ m1 + faq 		+ m1
				+ ")";
	if(LOG.fa) LOG.println("class Databese #insert_KeyGen_Query() : Q = " +  Q);
	return Q;
}
//
// キーレコードを削除する
public int deleteKeyGen( String _teUid){
	String teUid  = _teUid.trim();
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = 0;
	//
	String QUERY =  "DELETE from key_gen "	+ " WHERE teUid = "  + m1 + teUid + m1;
	//
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY); // ここでdeleteする
		//
	}catch(Exception e){
		System.out.println("### 失敗:deleteKeyGen( String _teUid) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 件数（普通は１）
}
//
////////////////////////////////////////////////////////////////////////////////////
//
//		学生データベース関係
//
//　　　	一般にはテーブル名を引数に取る
//			キーは学籍番号（primary）
//
//
////////////////////////////////////////////////////////////////////////////////////
//
//
// tbl には組織名（shozoku）を当てる事にした 2003.6.3
// 学籍番号で検索し、受講データを返す
//
public	Csv searchInfo(String tbl,String _id){
	String       id = _id.trim();		// 学籍番号
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
   	//ID が同じものを取り出す
	String QUERY = getQuery(tbl,id);	// 全レコード内容を返すクエリ
	int		n    = 0;
	Csv cs  = null;
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		ResultSet rs = stmt.executeQuery(QUERY);
		//
		while(rs.next()){
			n++;
			String classInfo		= rs.getString("classInfo");		// 受講情報	例）kawaba01-za,kawaba01-zb,moritann-aa
			cs = new Csv(classInfo);
		}
	}catch(Exception e){
		//System.out.println("### 失敗:search(String tbl,String _id) " + e);
		// 検索できなかったということで失敗ではない。st に null を返す
		if(DEBUG) System.out.println("# no data !");
		cs = null; // 念のため
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return cs; // 受講情報のCscオブジェクトを返す
	
	
}
//
// ★ tbl には組織名（shozoku）を当てる事にした 2003.6.3
// 　　学籍番号で検索し、全項目のデータを返す
//
public	Student search(String tbl,String _id){
	String       id = _id.trim();		// 学籍番号
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
   	//ID が同じものを取り出す
	
	int		n    = 0;
	Student st  = null;
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		String QUERY = getQuery(tbl,id);	// 全レコード内容を返すクエリ
		if(LOG.fa) LOG.println(QUERY);
		//
		ResultSet rs = stmt.executeQuery(QUERY);
		//
		while(rs.next()){
			n++;
			String kname  = rs.getString("kname");
			String email  = rs.getString("email");
			String keitai = rs.getString("keitai");
			//
			String stPasswd			= rs.getString("stpasswd");			// 学生のパスワード
			String mailselections	= rs.getString("mailselections");	// 携帯で受信するメールの選択	例）0,1,1,0,1,0,0,1  OR "*"
			String classInfo		= rs.getString("classInfo");		// 受講情報						例）kawaba01-za,kawaba01-zb,moritann-aa
			//
			String active_i	= rs.getString("active_i");
			String active_k	= rs.getString("active_k");
			String number_i	= rs.getString("number_i");
			String number_k	= rs.getString("number_k");
			//
			if(LOG.fa){
				LOG.println("class Database #search() : 取得したデータ");
				LOG.println("   szDB      =" + tbl);
				LOG.println("   stNumeber =" + id);
				LOG.println("   stPassd   =" + stPasswd);

				LOG.println("   kname     =" + kname);
				LOG.println("   email     =" + email);
				LOG.println("   keitai    =" + keitai);

				LOG.println("   mailselections   =" + mailselections);
				LOG.println("   classInfo  =" + classInfo);
				LOG.println("   active_i   =" + active_i);
				LOG.println("   active_k   =" + active_k);
				LOG.println("   number_i   =" + number_i);
				LOG.println("   number_k   =" + number_k);
				
				LOG.println("");
			}
			st = new Student(id,kname,email,keitai,stPasswd,mailselections,classInfo,active_i,active_k,number_i,number_k);
		}
	}catch(Exception e){
		//System.out.println("### 失敗:search(String tbl,String _id) " + e);
		// 検索できなかったということで失敗ではない。st に null を返す
		if(DEBUG) System.out.println("# no data !");
		st = null; // 念のため
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return st; // アドレスオブジェクトを返す
}

////////////////////////////////////////////////////////////////////////////////////
//
// ｓｚＤＢ関係 (shozoku)
//
////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 全ての所属IDをVectorで得る
	 */
	public	Vector	shozokuList(){
		Connection  conn  	= 	null;
		Statement 	stmt  	= 	null;
		String 		QUERY 	= 	"SELECT * FROM shozoku";
		int			n    	= 	0;
		Vector		list	=	new Vector ();
		try {
			conn 			= Broker.getConnection(); /* Broker からDB Connection を得る */
			stmt 			= conn.createStatement();
			ResultSet rs 	= stmt.executeQuery(QUERY);
			//
			while(rs.next()){
				n++;
				list.add(rs.getString("sz_id"));	// 所属ID
			}
		}catch(Exception e){
			System.out.println("### 失敗:shozokuList() :");
			e.printStackTrace();
			
		} finally {
		    try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			//
			Broker.freeConnection(conn);/* connection を Broker に返す */
		}
		return list;    
	}


/**
 * 所属管理者のＩＤを得る
 * @param szDB
 * @return
 */
public  String	getShozokuAdminID(String szDB){
	Hashtable	szRecord	=	shozokuRead(szDB);
	String		note		=	Gear.strHash(szRecord, "sz_note");
	if(Gear.isEmpty(note))	return	"";
	
	Csv			cs			=	new Csv(note);
	String		adminID		=	"";
	if(cs.size()>=2){
		adminID		=	cs.get(0);
	}
	return	adminID;
}
/**
 * 所属管理者のメールアドレス得る
 * @param szDB
 * @return
 */
public  String	getShozokuAdminMail(String szDB){
	String	shozokuAdminID	=	getShozokuAdminID(szDB);
	if(Gear.isEmpty(shozokuAdminID))	return	"";
	
	Hashtable	adminRec	=	new	Hashtable();
	MembersInfo(shozokuAdminID,adminRec);
	String		mail		=	Gear.strHash(adminRec, "_user_mail");
	return	mail;
}

//
//	学生用の各URLを返す
//
public	String getUrl_s(String _szDB){
	String szid = _szDB.trim();
	String cmd 		= " SELECT * FROM shozoku WHERE sz_id = " + m1 + szid + m1;
	if(LOG.fa) LOG.println("class Database #getUrl_s() :SQL = " + cmd);
	//
	Connection   conn  = null;
   	Statement 	 stmt  = null;
   	String 		 url_s = "#";
	//
	try {
       	// Broker からDB Connection を得る
       	conn 		 = Broker.getConnection();/** Broker からDB Connection を得る */
		stmt 		 = conn.createStatement();
		ResultSet rs = stmt.executeQuery(cmd);
		int rowCount = 0;
		while(rs.next()){
			url_s = rs.getString("sz_url_s");
			++rowCount;
			break;
		}
		//
	}catch(Exception e){
		System.out.println("#### 失敗:getUrl_s(String _szDB) " + e);
	} finally {
		try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
       	Broker.freeConnection(conn);/** Broker に Connection を戻す */
   	}
	// D-0011
	if(LOG.fa) LOG.println("class Database #getUrl_s() : 学生用の各URLを返す の出口です");
	if(LOG.fa) LOG.println("           url_s = " + url_s);
	if(LOG.fa) LOG.println("           url_s が null の場合 # に変換します");
	if((url_s==null)||(url_s.length()==0)) url_s = "#";	// 念のため
	return url_s;
	//
}
//
// 所属（shozoku）データベースのアクティブでないアカウントを指定の状態に更新する（０＝ノンアクティブ、１＝アクティブ、２＝廃棄）
public	int setAccountToActive(String szDB,String sw){
	Connection   conn  = null;
   	Statement 	 stmt  = null;
	int  		 count = 0;
	//
	String QUERY = 
		"UPDATE shozoku set sz_active = " + m1 + sw + m1 + " WHERE sz_id = " + m1 + szDB + m1;
	try {
      	conn 	= Broker.getConnection(); /** Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		count  = stmt.executeUpdate(QUERY);
	}catch(Exception e){
		System.out.println("#exwk ### 失敗:setAccountToActive() " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/** connection を Broker に返す */
   	}
	return count;
}	//

//
// note の書き変え
public	int updateShozokuNote(String szDB,String noteStr){
	Connection   conn  = null;
	Statement 	 stmt  = null;
	int  		 count = 0;
	//
	String QUERY = "UPDATE shozoku set sz_note = " + m1 + noteStr + m1 + " WHERE sz_id = " + m1 + szDB + m1;
	if(LOG.fa) LOG.println("■ Database #setShozokuNote() ");
	if(LOG.fa) LOG.println("□ query="+ QUERY);
	
	try {
		conn 	= Broker.getConnection(); /** Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		count  = stmt.executeUpdate(QUERY);
	}catch(Exception e){
		System.out.println("#exwk ### 失敗:setShozokuNote() " + e);
	} finally {
		try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
		//
		Broker.freeConnection(conn);/** connection を Broker に返す */
	}
	return count;
}	
//
//  学生データベースは大学単位。shozoku テーブルにそのリストがある
//  ここでは、レコードキー（例、kwassui ）からテーブルを検索し、
/// 該当があればそのレコードを返す。なければ null を返す。
//
public	Hashtable shozokuRead(String _sz_id){
	String      sz_id   = 	_sz_id.trim();
	Connection  conn  	= 	null;
	Statement 	stmt  	= 	null;
	String 		QUERY 	= 	shozokuQuery(sz_id);
	int		n    	= 	0;
	Hashtable	rectb	=	new Hashtable ();
	try {
		conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		ResultSet rs = stmt.executeQuery(QUERY);
		//
		while(rs.next()){
			n++;
			rectb.put("sz_active"	,rs.getString("sz_active"));	// アクティブか否か
			rectb.put("sz_name"		,rs.getString("sz_name"));		// 大学名、学部名など
			rectb.put("sz_note"		,rs.getString("sz_note"));		// 最初の登録者のIDなど
			rectb.put("sz_url_s"	,rs.getString("sz_url_s"));		// その所属での学生の戻りURL
			rectb.put("sz_domain"	,rs.getString("sz_domain"));	// その所属のドメイン名
		}
	}catch(Exception e){
		//System.out.println("### 失敗:search(String tbl,String _id) " + e);
		// 検索できなかったということで失敗ではない。st に null を返す
		LOG.println("# 所属（　"+sz_id+"　）を検索したがデータがない Database.java:4372");
		rectb = null; // 念のため

	} finally {
	    try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
		//
		Broker.freeConnection(conn);/* connection を Broker に返す */
	}
	return rectb; // 全レコードを返す
}


//
//  学生データベースは大学単位。shozoku テーブルにそのリストがある
//  ここでは、レコードキー（例、kwassui ）からテーブルを検索し、該当があればそのレコードを
//  Csv にして返す。
//  例えば、所属名は csvrecord.get(2) である。
//  該当がない場合は null を返すので受け取り側でチェックすること(認証にも使う)
//
public	Csv shozoku_search(String _sz_id){
	String       sz_id   = _sz_id.trim();
	Connection   conn  	 = null;
   	Statement 	 stmt  	 = null;
   	//ID が同じものを取り出す
	String QUERY = shozokuQuery(sz_id);	// 全レコード内容をCSVで返すクエリ
	int		n    = 0;
	Csv     record = null;
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		ResultSet rs = stmt.executeQuery(QUERY);
		//
		while(rs.next()){
			n++;
			String sz_active = rs.getString("sz_active");	// アクティブか否か
			String sz_name   = rs.getString("sz_name");		// 大学名、学部名など
			String sz_note   = rs.getString("sz_note");		// 最初の登録者のIDなど
			String sz_url_s  = rs.getString("sz_url_s");	// その所属での学生の戻りURL
			String sz_domain = rs.getString("sz_domain");	// その所属のドメイン名
			//
			//
			String sz = sz_id + "," + sz_active + "," + sz_name + "," + sz_note + "," + sz_url_s + "," + sz_domain;
			record    = new Csv(sz);
		}
	}catch(Exception e){
		//System.out.println("### 失敗:search(String tbl,String _id) " + e);
		// 検索できなかったということで失敗ではない。st に null を返す
		if(DEBUG) System.out.println("# no data !");
		record = null; // 念のため
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return record; // 全レコードのcsvを返す
}
// shozoku テーブルを検索するクエリを作成
String shozokuQuery(String sz_id){
	String Q = "SELECT * FROM " + "shozoku" + " WHERE sz_id = " + m1 + sz_id + m1 ;
	if(DEBUG) System.out.println("Query = " + Q);
	return Q;		
}




////////////////////////////////////////////////////////////////////////////////////
//
//     教師ユーザー情報 (membersinfo)
//
////////////////////////////////////////////////////////////////////////////////////
//
//
// ユーザ情報をユーザーＩＤで引く（membersInfo テーブル）
/*
create table membersInfo (
    user_id         CHAR(8)   PRIMARY KEY,
    user_active     CHAR(1),
    user_db         VARCHAR(30),
    user_mail       VARCHAR(50),
    user_passwd     VARCHAR(20),
    user_name       VARCHAR(20),
    user_hurigana   VARCHAR(30),
    user_url_t      VARCHAR(150),
    user_division   CHAR(1),
    note            VARCHAR(100),
	domain          VARCHAR(50)
);
 */
public	int MembersInfo(String _id,Hashtable htb){
	if(LOG.fa) LOG.println("■ Database #MembersInfo() :  ユーザ情報をキーで引く");
	String id 		= _id.trim();
	Connection   conn  = null;
   	Statement 	 stmt  = null;
   	
   	
   	int rowCount = 0;
	try {
		String cmd 	 = " SELECT * FROM membersInfo WHERE user_id = " + m1 + id + m1;
		if(LOG.fa) LOG.println("CMD = " + cmd);
       	// Broker からDB Connection を得る
       	conn 		 = Broker.getConnection();/** Broker からDB Connection を得る */
		stmt 		 = conn.createStatement();
		ResultSet rs = stmt.executeQuery(cmd);
		while(rs.next()){
			//
		    String	uid	=	rs.getString("user_id");
		    htb.put(TUID,uid);
			htb.put(UID,uid);
			htb.put("_user_id",uid);	// このキーを使っている
			
			String  mail	= rs.getString("user_mail");
			htb.put("_user_mail",mail);
			htb.put("_mail",mail);
			htb.put(MAIL,mail);
			htb.put(TMAIL,mail);
			
			htb.put(PASSWORD, rs.getString("user_passwd"));
			htb.put("_active",rs.getString("user_active"));
			
			String	name	=	rs.getString("user_name");
			htb.put("_user_name",name);
			htb.put("_name",name);
			htb.put(TNAME,name);
			htb.put(UNAME,name);
			
			htb.put(THKANA,rs.getString("user_hurigana"));

			String s = rs.getString("note");
			if(s==null) s = " ";
			htb.put("_note",s);
			
			String uk = rs.getString("user_db");		// 2003.3.8 追加 2003.6.2 user_db に変更
			if(uk==null) uk = "12345678";				// ダミーキー（あり得ない）
			htb.put(GROUP,uk);							// szDB
			htb.put("user_db",uk);
			htb.put("_user_db",uk);

			String url_t = rs.getString("user_url_t");	// 終了時に表示する教員用URL　2003.6.17
			if (url_t.length() == 0) url_t = "#";
			htb.put("_url_t",url_t);
			htb.put(HOMEURL,url_t);		// コードで使っているので
			
			htb.put(DIVISION, rs.getString("user_division"));
			
			htb.put(DOMAIN, rs.getString("domain"));
			//
			++rowCount;
			break;
		}
		htb.put("_counts",String.valueOf(rowCount)); // 何件あったか記録しておく
		//
	}catch(Exception e){
		htb.put("_alart","err : NO DATA");	
		System.out.println("#### 失敗:MembersInfo(String _mail_key,Hashtable htb) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
       	Broker.freeConnection(conn);/** Broker に Connection を戻す */
   	}
	if(LOG.fa) LOG.outHash(htb,String.valueOf(rowCount) + " 件該当がありました");
	return rowCount;
	//
}
public	boolean isValidUser(String _key,String _passwd_in){

    String key			= _key.trim();
	String passwd_in	= _passwd_in.trim();
	Connection   conn   = null;
   	Statement 	 stmt   = null;
   	boolean		 ret    = false;
	try {
       	conn 		     = Broker.getConnection();/** Broker からDB Connection を得る */
		stmt 		 	 = conn.createStatement();
		ResultSet rs 	 = stmt.executeQuery("SELECT user_passwd FROM membersInfo WHERE user_id = " + m1 + key + m1);
		String passwd_db = "";
		while(rs.next()){
			passwd_db = rs.getString("user_passwd");
			break;
		}
		if(passwd_db.equals(passwd_in)){
			ret = true;
		}else{
			ret = false;
		}
		
	}catch(Exception e){
		if(LOG.fa) {
			System.out.println("class Database #isUser() : エラー発生");
			System.out.println("Exception occured " + e);
		}
		ret	= false;
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
       	Broker.freeConnection(conn);/** Broker に Connection を戻す */
   	}
	return ret;
}

	/**
	 * 教師のパスワードを上書きで変更する
	 */
	public int updateMemberPasswd(String userid, String passwd){
		Connection   conn  = null;
			Statement 	 stmt  = null;
		int  		 count = 0;
		//
		String QUERY = "UPDATE membersInfo set user_passwd = " + m1 + passwd + m1 + " WHERE user_id = " + m1 + userid + m1;
		if(LOG.fa) LOG.println("writeMemberPasswd() : QUERY = " + QUERY);
		//
		try {
		  	conn 	= Broker.getConnection(); /** Broker からDB Connection を得る */
			stmt 	= conn.createStatement();
			count  = stmt.executeUpdate(QUERY);
		}catch(Exception e){
			System.out.println("#exwk ### 失敗:writeMemberPasswd() " + e);
		} finally {
		    try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			    //
			Broker.freeConnection(conn);/** connection を Broker に返す */
			}
		return count;
	}
	

	//  データベース名と共に氏名,url_tも返す
	public	String getUserInfo(String _userid){
		String 	userid 		=	_userid.trim();
		String 	cmd 		=	" SELECT * FROM membersInfo WHERE user_id = " + m1 + userid + m1;
		if(LOG.fa) System.out.println("■ Database #getUserInfo()　：SQL CMD = " + cmd);
		Connection  conn  	= 	null;
	   	Statement 	stmt  	= 	null;
	   	String 		DBname 	= 	"";
		String		teName 	= 	"";
		String		url_t  	= 	"#";
		String		note   	= 	"";
		//
		try {
	       	// Broker からDB Connection を得る
	       	conn 		 = Broker.getConnection();/** Broker からDB Connection を得る */
			stmt 		 = conn.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			int rowCount = 0;
			while(rs.next()){
				DBname = rs.getString("user_db");
				teName = rs.getString("user_name");
				url_t  = rs.getString("user_url_t");
				note   = rs.getString("note");			// max 100 byte  / 2003.7.1 追加（ユーザー設定情報）
				++rowCount;
				break;
			}
			//
		}catch(Exception e){
			System.out.println("#### 失敗:getUserInfo() " + e);
		} finally {
	        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
	   	    //
	       	Broker.freeConnection(conn);/** Broker に Connection を戻す */
	   	}
		return DBname + "%" + teName + "%" + url_t + "%" + note;
		//
	}

	//
	//  ユーザーデータをキーで引いて、設定情報を返す(members テーブルより)
	public	String getSetupInfo(String _uid){
		String uid 	= _uid.trim();
		String cmd 	= " SELECT * FROM membersInfo WHERE user_id = " + m1 + uid + m1;
		if(DEBUG) System.out.println("SQL CMD = " + cmd);
		Connection   conn  = null;
	   	Statement 	 stmt  = null;
	   	String 		 setup = "#";	// 初期値
		//
		try {
	       	// Broker からDB Connection を得る
	       	conn 		 = Broker.getConnection();/** Broker からDB Connection を得る */
			stmt 		 = conn.createStatement();
			ResultSet rs = stmt.executeQuery(cmd);
			int rowCount = 0;
			while(rs.next()){
				setup = rs.getString("note");	// 設定情報 設定されていれば Csv または #
				if(setup==null)			setup = "#";
				if(setup.length() == 0)	setup = "#";
				++rowCount;
				break;
			}
			//
		}catch(Exception e){
			System.out.println("#### 失敗:setupInfo(String _mail_key) " + e);
		} finally {
	        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
	   	    //
	       	Broker.freeConnection(conn);/** Broker に Connection を戻す */
	   	}
		if(DEBUG) System.out.println("## setupInfo(" + uid + " ) = " + setup);
		return setup;
		//
	}
	//
	// 設定情報をデータベースに書き込む(members テーブルの note フィールド)
	//
	public	int wrtSetupInfo(String _id,String _note){
		String id 		= _id.trim();
		String cmd 		=  "UPDATE  membersInfo  set  note = " + m1 + _note  + m1 + " WHERE user_id = "  + m1 + id + m1;
		//
		Connection   conn  = null;
	   	Statement 	 stmt  = null;
	   	int	 		 n 	   = -1;	// 初期値
		//
		try {
	      	conn 	= Broker.getConnection(); /** Broker からDB Connection を得る */
			stmt 	= conn.createStatement();
			n  		= stmt.executeUpdate(cmd);
			//
		}catch(Exception e){
			System.out.println("#exwk ### 失敗:wrtSetupInfo() " + e);
		} finally {
	        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
	   	    //
			Broker.freeConnection(conn);/** connection を Broker に返す */
	   	}
		return n;
		//
	}
////////////////////////////////////////////////////////////////////////////////////
//
//		学生データベース関係
//
//　　　	一般にはテーブル名を引数に取る
//			キーは学籍番号（primary）
//
//
////////////////////////////////////////////////////////////////////////////////////
//
//
// ★ tbl には組織名（shozoku）を当てる事にした 2003.6.3
// 　　学籍番号で検索し、全項目のデータを返す
//
public	int studentAllInfo(String tbl,String id,Vector st){
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
   	//ID が同じものを取り出す
	String QUERY = getQuery(tbl,id);	// 全レコード内容を返すクエリ
	int		n    = 0;
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		ResultSet rs = stmt.executeQuery(QUERY);
		//
		while(rs.next()){
			n++;
			//
			String	kname		= rs.getString("kname"); 	if(isEmpty(kname))	 	kname	= "-";
			String	email		= rs.getString("email"); 	if(isEmpty(email)) 		email	= "-";
			String	keitai		= rs.getString("keitai"); 	if(isEmpty(keitai)) 	keitai	= "-";
			//
			String	stpasswd		= rs.getString("stpasswd");			if(isEmpty(stpasswd)) 		stpasswd		= id;
			String	mailselections	= rs.getString("mailselections"); 	if(isEmpty(mailselections)) mailselections	= "*";
			String	classInfo		= rs.getString("classInfo"); 		if(isEmpty(classInfo)) 		classInfo		= "";
			//
			String	active_i	= rs.getString("active_i") ; 	if(isEmpty(active_i)) 	active_i	= "";
			String	active_k	= rs.getString("active_k") ; 	if(isEmpty(active_k)) 	active_k	= "";
			String	number_i	= rs.getString("number_i") ; 	if(isEmpty(number_i)) 	number_i	= "";
			String	number_k	= rs.getString("number_k") ; 	if(isEmpty(number_k)) 	number_k	= "";
			//
			st.add(id);
			st.add(kname);
			st.add(email);
			st.add(keitai);
			//
			st.add(stpasswd);			// 学生のパスワード
			st.add(mailselections);		// 携帯で受信するメールの選択	例）0,1,1,0,1,0,0,1  OR "*"
			st.add(classInfo);			// 受講情報						例）kawaba01-za,kawaba01-zb,moritann-aa
			//
			st.add(active_i);
			st.add(active_k);
			st.add(number_i);
			st.add(number_k);
			//
		}
	}catch(Exception e){
		//System.out.println("### 失敗:search(String tbl,String _id) " + e);
		// 検索できなかったということで失敗ではない。st に null を返す
		if(DEBUG) System.out.println("# no data !");
		st = null; // 念のため
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // アドレスオブジェクトを返す
}
boolean isEmpty(String s){
	if(s==null){
		return 	true;
	}else if(s.length()==0){
		return	true;
	}
	return false;
}
//
//
public	int	insertStudent(String szDB, Student st ){
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = 0;
	//
	String QUERY = insertQuery(szDB,st);
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY);
		//
	}catch(Exception e){
		System.out.println("### 失敗:insertStudent() " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 件数（普通は１）		
}
//
// ★新規追加のクエリ
//
String insertQuery(String tbl,Student st){
	String id		= st.id();
	String kname	= st.kname();	// null とはならない
	String email	= st.email();	// null とはならない
	String keitai	= st.keitai();	// null とはならない
	//
	String stPasswd			= st.stPasswd();			// 学生の初期パスワード（＝学籍番号）
	String mailselections	= st.mailselections();		// 携帯で受信するメールの選択	例）0,1,1,0,1,0,0,1
	String classInfo		= st.classInfo();			// 受講情報を１つだけ		　　例）kawaba01-za
	String active_i			= st.active_i();
	String active_k			= st.active_k();
	String number_i			= st.number_i();
	String number_k			= st.number_k();
	//
	String Q = "INSERT INTO " +  tbl 
	            + " VALUES (" + m1 + id  + m2 
				+ m1 + kname  + m2
				+ m1 + email  + m2
				+ m1 + keitai + m2
				+ m1 + stPasswd + m2
				+ m1 + mailselections + m2
				+ m1 + classInfo + m2
				+ m1 + active_i + m2
				+ m1 + active_k + m2
				+ m1 + number_i + m2
				+ m1 + number_k 
				+ m1+ ")";
	if(LOG.fa)  LOG.println("Database class #insertQuery() :Query = " + Q);
	return Q;
}
//
//  学生データをアップデートする
//
public	int	updateStudent(String szDB, Student st ){
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = 0;
	//
	String QUERY = updateQueryALL(szDB,st);
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY);
		//
	}catch(Exception e){
		System.out.println("### 失敗:insertStudent() " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 件数（普通は１）		
}
//
// ★アップデートのクエリ
//
//
String updateQueryALL(String tbl,Student st){
	//
	String id	= st.id();
	//
	String kname	= st.kname();	// null とはならない
	String email	= st.email();	// null とはならない
	String keitai	= st.keitai();	// null とはならない
	//
	String stPasswd			= st.stPasswd();	// null とはならない
	String mailselections	= st.mailselections();	// null とはならない
	String classInfo		= st.classInfo();	// null とはならない
	//
	String active_i	= st.active_i();
	String active_k	= st.active_k();
	String number_i	= st.number_i();
	String number_k	= st.number_k();
	
	String Q = "UPDATE " + tbl + " set " 
	
	         + " kname  = "  + m1 + kname  + m2
	         + " email  = "  + m1 + email  + m2
	         + " keitai = "  + m1 + keitai + m2
	
	         + " stPasswd        = " + m1 + stPasswd  + m2
	         + " mailselections  = " + m1 + mailselections  + m2
	         + " classInfo       = " + m1 + classInfo  + m2
	
	         + " active_i  = " + m1 + active_i  + m2
	         + " active_k  = " + m1 + active_k  + m2
	         + " number_i  = " + m1 + number_i  + m2
	         + " number_k  = " + m1 + number_k  + m1
	
			 + " WHERE id = " + m1 + id + m1;
	//
	if(LOG.fa)  LOG.println("Database class #updateQueryALL():");
	if(LOG.fa)  LOG.println("   ⇒  Query = " + Q);
	return Q;
}
//
// 各大学向けの学生データベースを作成する＜テーブル名は所属名（大学名）＞
public	int createTable(String tbl){
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = -1;
	//
	String QUERY = createQuery(tbl);
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY);
		//
	}catch(Exception e){
		System.out.println("### 失敗:createTable(String tbl) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // （０が返る）
}
//
//  ★グループの学生データデータベースを作成するクエリ 2003.6.
//
String createQuery(String tbl){
	/*
	String Q = "CREATE TABLE " + "x" + tbl + " ( " + 
                     "id  VARCHAR(12)  PRIMARY KEY ,kname VARCHAR(20) ,email VARCHAR(60) ,keitai VARCHAR(60)"
					 + " )" ;
	*/
	String Q = "CREATE TABLE " +  tbl + " ( " + 
                       "id  VARCHAR(12)  PRIMARY KEY ,kname VARCHAR(20) ,email VARCHAR(60) ,keitai VARCHAR(60)"
					 + " ,stPasswd VARCHAR(20) ,mailselections VARCHAR(50) ,classInfo text" 
					 + " ,active_i CHAR(60) ,active_k CHAR(60) ,number_i VARCHAR(3) ,number_k VARCHAR(3)"
					 + " )" ;
	return Q;
}
//
// メールアドレスを新規登録する
public	int insert(String tbl,Student st){
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = -1;
	//
	String QUERY = insertQuery(tbl,st);
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY);
		//
	}catch(Exception e){
		System.out.println("### 失敗:insert(String tbl,Student st) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 件数（普通は１）
}
//
// レコードを削除する
public	int deleteStudent(String tbl,String id){
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = 0;
	//
	try {
		String QUERY = "DELETE from " +  tbl + " WHERE id = " + m1 + id + m1;
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY); // ここでdeleteする
		//
	}catch(Exception e){
		System.out.println("### 失敗:insert(String tbl,Student st) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 件数（普通は１）
}
//
public	int delete(String tbl,Student st){
	//
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = 0;
	//
	String QUERY = deleteQuery(tbl,st);
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY); // ここでdeleteする
		//
	}catch(Exception e){
		System.out.println("### 失敗:insert(String tbl,Student st) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 件数（普通は１）
}
//
// メールアドレスを更新する
public	int update(String tbl,Student st){
	return update(tbl,st,"ALLMAIL");
}
// ★データベース内の各項目のみ変更する
public	int update(String tbl,Student st,String sw){
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = -1;
	//
	String QUERY = "";
	if(sw.equals("ALLMAIL")) {
		QUERY = updateQuery_ALLMAIL(tbl,st);
	}else if(sw.equals("EMALIL")){
		QUERY = updateQuery_EMAIL(tbl,st);
	}else if(sw.equals("KEITAI")){
		QUERY = updateQuery_KEITAI(tbl,st);
	}else if(sw.equals("PASS")){
		QUERY = updateQuery_stPasswd(tbl,st);
	}else if(sw.equals("MSEL")){
		QUERY = updateQuery_mailselections(tbl,st);
	}else if(sw.equals("CINFO")){
		QUERY = updateQuery_classInfo(tbl,st);
	}else if(sw.equals("KAKUNIN_I")){
		QUERY = updateQuery_kakunin_i(tbl,st);
	}else if(sw.equals("KAKUNIN_K")){
		QUERY = updateQuery_kakunin_k(tbl,st);
	}
	//
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY);
		//
	}catch(Exception e){
		System.out.println("### 失敗:update(String tbl,Student st) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 更新件数（普通は１）
}

//
// id（学籍番号）で検索して全レコードを返すためのクエリ
String getQuery(String tbl,String id){
	String Q = "SELECT * FROM " + tbl + " WHERE id = " + m1 + id + m1 ;
	if(DEBUG) System.out.println("Query = " + Q);
	return Q;
}
//
public	String updateQuery_ALLMAIL(String tbl,Student st){
	String id		= st.id();
	String kname	= st.kname();	// null とはならない
	String email	= st.email();	// null とはならない
	String keitai	= st.keitai();	// null とはならない
	//
	String Q = "UPDATE " + tbl + " set " 
	         + " kname  = "          + m1 + kname  + m2
	         + " email  = "          + m1 + email  + m2
	         + " keitai = "          + m1 + keitai + m1
			 + " WHERE id = " + m1 + id + m1;
	if(DEBUG) System.out.println("Query = " + Q);
	return Q;
}
public	String updateQuery_stPasswd(String tbl,Student st){
	String id		= st.id();
	String stPasswd	= st.stPasswd();	// null とはならない
	//
	String Q = "UPDATE " +  tbl + " set " 
	         + " stPasswd  = " + m1 + stPasswd  + m1
			 + " WHERE id = "  + m1 + id + m1;
	if(DEBUG) System.out.println("Query = " + Q);
	return Q;
}
public	String updateQuery_mailselections(String tbl,Student st){
	String id		= st.id();
	String mailselections	= st.mailselections();	// null とはならない
	//
	String Q = "UPDATE " +  tbl + " set " 
	         + " mailselections  = " + m1 + mailselections  + m1
			 + " WHERE id = "        + m1 + id + m1;
	if(DEBUG) System.out.println("Query = " + Q);
	return Q;
}
public	String updateQuery_classInfo(String tbl,Student st){
	String id		= st.id();
	String classInfo	= st.classInfo();	// null とはならない
	//
	String Q = "UPDATE " +  tbl + " set " 
	         + " classInfo  = " + m1 + classInfo  + m1
			 + " WHERE id = "   + m1 + id + m1;
	if(DEBUG) System.out.println("Query = " + Q);
	return Q;
}
//
public	String updateQuery_EMAIL(String tbl,Student st){
	String id		= st.id();
	String email	= st.email();	// null とはならない
	//
	String Q = "UPDATE " +  tbl + " set " 
	         + " email  = " + m1 + email  + m1
			 + " WHERE id = " + m1 + id + m1;
	if(DEBUG) System.out.println("Query = " + Q);
	return Q;
}
//
//
public	String updateQuery_KEITAI(String tbl,Student st){
	String id		= st.id();
	String keitai			= st.keitai();			// null とはならない
	//
	String Q = "UPDATE " +  tbl + " set " 
	         + " keitai 		= " + m1 + keitai + m1
			 + " WHERE id = " + m1 + id + m1;
	if(DEBUG) System.out.println("Query = " + Q);
	return Q;
}
//
// インターネットメール確認番号アップデート
public	String updateQuery_kakunin_i(String tbl,Student st){
	String id		= st.id();
	String active	= st.active_i();
	String number	= st.number_i();
	//
	String Q = "UPDATE " +  tbl + " set " 
	         + " active_i  = " + m1 + active  + m2
	         + " number_i  = " + m1 + number  + m1
			 + " WHERE id = "  + m1 + id + m1;
	if(DEBUG) System.out.println("Query = " + Q);
	return Q;
}
// ケータイメール確認番号アップデート
public	String updateQuery_kakunin_k(String tbl,Student st){
	String id		= st.id();
	String active	= st.active_k();
	String number	= st.number_k();
	//
	String Q = "UPDATE " +  tbl + " set " 
	         + " active_k  = " + m1 + active  + m2
	         + " number_k  = " + m1 + number  + m1
			 + " WHERE id = "  + m1 + id + m1;
	if(DEBUG) System.out.println("Query = " + Q);
	return Q;
}
//
public	String deleteQuery(String tbl,Student st){
	String id		= st.id();
	String Q = "DELETE from " +  tbl + " WHERE id = " + m1 + id + m1;
	if(DEBUG) System.out.println("Query = " + Q);
	return Q;
}
//
//////////////////////////////////////////////////////////////////////////////////////////////////////
//
//メールアドレス登録・更新関係
//
///////////////////////////////////////////////////////////////////////////////////////////////////////
//
//
// ★データベースのメールアドレスのみ変更する
// 
public int update_email(String tbl,String id,String email){
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = 0;
	//
	String QUERY = updateQuery_EMAIL(tbl,id,email);
	//
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY);
		//
	}catch(Exception e){
		System.out.println("### 失敗:update(String tbl,Student st) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 更新件数（普通は１）
}
//
//
public String updateQuery_EMAIL(String tbl,String id,String email){
	//
	String Q = "UPDATE " +  tbl + " set " 
	         + " email  = " + m1 + email  + m1
			 + " WHERE id = " + m1 + id + m1;
	if(LOG.fa) LOG.println("Query = " + Q);
	return Q;
}
//
//
// ★データベースの携帯アドレスのみ変更する
// 
public int update_keitai(String tbl,String id,String keitai){
	Connection   conn  		= null;
   	Statement 	 stmt  		= null;
	int n = 0;
	//
	String QUERY = updateQuery_KEITAI(tbl,id,keitai);
	//
	try {
      	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
		stmt 	= conn.createStatement();
		n  		= stmt.executeUpdate(QUERY);
		//
	}catch(Exception e){
		System.out.println("### 失敗:update(String tbl,Student st) " + e);
	} finally {
        try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
   	    //
		Broker.freeConnection(conn);/* connection を Broker に返す */
   	}
	return n; // 更新件数（普通は１）
}
//
//
public String updateQuery_KEITAI(String tbl,String id,String keitai){
	//
	String Q = "UPDATE " +  tbl + " set " 
	         + " keitai 		= " + m1 + keitai + m1
			 + " WHERE id = " + m1 + id + m1;
	if(LOG.fa) LOG.println("Query = " + Q);
	return Q;
}
String strHash(Hashtable htb,String key){
	return strHash( htb, key,null);
}
String strHash(Hashtable htb,String key,String comment){
	//
	String	str	= (String) htb.get(key);
	if(comment!=null){
		if(str==null){
			LOG.println("★ key =  " + key + "/ " + comment);
			LOG.println("★★★★★★★★★★★★★★★★★★★★★★★★★★");
			LOG.println("★★   　　　　　　　　　　　　　　　　　　　　   ★★");
			LOG.println("★★   ハッシュから取り出したデータは NULL です   ★★");
			LOG.println("★★   　　　　　　　　　　　　　　　　　　　　   ★★");
			LOG.println("★★★★★★★★★★★★★★★★★★★★★★★★★★★");
		}
	}
	return str;
}
String strHashSP(Hashtable htb,String key){
	//
	String	str	= (String) htb.get(key);
	if(str==null){
		/*
		DBG.println("★★★★★★★★★★★★★★★★★★★★★★★★★★★");
		DBG.println("★★   　　　　　　　　　　　　　　　　　　　　   ★★");
		DBG.println("★★   ハッシュから取り出したデータは NULL です   ★★");
		DBG.println("★★   長さゼロの空白に変換しました　　　　　　   ★★");
		DBG.println("★★   key = " + key );
		DBG.println("★★★★★★★★★★★★★★★★★★★★★★★★★★★");
		*/
		str = "";
	}
	if(LOG.fa)  LOG.println("str = :"  + str + ":");
	return 	str;
}
//
// 今日の日付をjava.sql.Date 型のオブジェクトで返す
java.sql.Date getCurrentDay(){
	String date = currentDayString();
	return java.sql.Date.valueOf(date);
}
//
// 今日の日付を yyyy-mm-dd の形式の文字列で返す
String currentDayString(){
	GregorianCalendar now = new GregorianCalendar();
	if(DEBUG) System.out.print( "Today is " + Datestring(now) );
	return Datestring(now);
}
//
// 特定の日付を yyyy-mm-dd の形式の文字列で返す
String Datestring(GregorianCalendar date){
	int _yy = date.get(Calendar.YEAR);
	int _mm = date.get(Calendar.MONTH) + 1;
	int _dd = date.get(Calendar.DATE);
	//
	String yy = String.valueOf(_yy);	// 4桁固定
	String mm = get00type(_mm);
	String dd = get00type(_dd);
	//
	String datestr = yy + "-" + mm + "-" + dd;
	if(DEBUG) System.out.println("date = " + datestr);
	return datestr;
}
String get00type(int s){
	String  dt      = String.valueOf(s);
	if((dt == null)||(dt.length() == 0)) return "00";
	int 	pos		= dt.length();
	String	pattern	= "00" + dt;
	return  pattern.substring(pos);
}
//
// 特定の日付をjava.sql.Date 型のオブジェクトで返す
java.sql.Date getDaySQL(GregorianCalendar date){
	String dt = Datestring(date);
	return java.sql.Date.valueOf(dt);
}
}