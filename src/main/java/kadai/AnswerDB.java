/*
 * 作成日: 2005/03/18
 *
 * TODO
 */
package kadai;
import java.util.*;
import java.sql.*;

import database.DbConnectionBroker;
import framework.LOG;

/**
 *	課題提出履歴データベース
 *
 	
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
	
 *
 */
public class AnswerDB {
	
	private final String m1		= "\'";
	private final String m2		= "\',";
	private final String m3		= ",";	
	
	DbConnectionBroker Broker;
	
	public	AnswerDB(DbConnectionBroker Broker){
		this.Broker		=	Broker;
		
	}

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
						+	" te_aplec_key       CHAR(12)        NOT  NULL,   "
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
	
}
