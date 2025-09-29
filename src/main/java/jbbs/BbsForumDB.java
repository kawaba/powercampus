/*
 * 
 */
package jbbs;
import	 tktools.*;
import  java.sql.*;
import	 java.util.*;

import database.DbConnectionBroker;

import framework.LOG;
/**
 フォーラムデータベースの内容

 alive     活動中か否か 　　0=close&hidden  1=close  2=active  規定値： 2
 gpflag    グループ学習か　 0=no      1=yes     規定値: 0
 rtflag    支持度必須か　   0=no      1=yes     規定値: 0
 hdflag    ハンドル必須か   0=no      1=yes     規定値: 1

create table bbsforum_GROUPNAME (
     ownerkey   VARCHAR(12),
     forumkey   CHAR(4),  
     alive      CHAR(1),
     gpflag     CHAR(1),
     rtflag     CHAR(1),
     hdflag     CHAR(1),
     date       VARCHAR(30),
     subject    VARCHAR(100),
     content    TEXT,
     attachment TEXT,
     relation   TEXT
);
create index bbsforum_GROUPNAME_idx on bbsforum_GROUPNAME (ownerkey, forumkey);
*/

public class BbsForumDB {
	private final String m1		= "\'";
	private final String m2		= "\',";
	private final String m3		= ",";
	//
	public	static final String	HIDDEN			=	"0";	
	public	static final String	CLOSED			=	"1";
	public	static final String	ACTIVE			=	"2";	// default

	public	static final String	GROUP_LEARNING	=	"1";
	public	static final String	NORMAL_BBS		=	"0";	// default

	public	static final String	RATING_MODE		=	"1";	// default
	public	static final String	NOT_RATING_MODE	=	"0";	

	public	static final String	BY_HANDLE		=	"1";	// default
	public	static final String	BY_UID			=	"0";

	public	static final String	OWNER_KEY		=	"_ownerkey";
	public	static final String	FORUM_KEY		=	"_forumkey";
	public	static final String	ALIVE_FLAG		=	"_alive";
	public	static final String	GROUP_FLAG		=	"_gpflag";
	public	static final String	RATING_FLAG		=	"_rtflag";
	public	static final String	HANDLE_FLAG		=	"_hdflag";
	public	static final String	DATE			=	"_date";
	public	static final String	SUBJECT			=	"_subject";
	public	static final String	CONTENT			=	"_content";
	public	static final String	ATTACHMENT		=	"_attachment";
	public	static final String	RELATION		=	"_relation";
	public	static final String	WATCH			=	"_watch";

	DbConnectionBroker 	Broker;
	String				szDB;
	//
	public	BbsForumDB(String szDB,DbConnectionBroker Broker){
		this.szDB		= szDB;
		this.Broker 	= Broker;
	}
	/**
	 * ハッシュテーブル内のレコードについて，
	 * タイトルと内容に含まれる \ " ' に対応する特殊文字を元に戻す
	 * 
	 * @param ht
	 */
	void	toNomal(Hashtable ht){
	    
	    String	subject		=	Gear.strHash(ht,SUBJECT);
	    String	content		=	Gear.strHash(ht,CONTENT);
	    //
	    
	    if(!Gear.isEmpty(subject)){
	        ht.put(SUBJECT, Gear.toNormalString(subject));
	    }
	    if(!Gear.isEmpty(content)){
	        ht.put(CONTENT, Gear.toNormalString(content));
	    }	    
	}
	/**
	 * ハッシュテーブル内のレコードについて，
	 * タイトルと内容に含まれる \ " ' を対応する特殊文字に直す
	 * 
	 * @param ht
	 */
	void	toDefined(Hashtable ht){
	    String	subject		=	Gear.strHash(ht,SUBJECT);
	    String	content		=	Gear.strHash(ht,CONTENT);
	    //
	    if(!Gear.isEmpty(subject)){
	        ht.put(SUBJECT, Gear.toDefindStr(subject));
	    }
	    if(!Gear.isEmpty(content)){
	        ht.put(CONTENT, Gear.toDefindStr(content));
	    }
	}
	/** null なら dt を返す */
	String	setValidData(String str,String dt){
		if(Gear.isEmpty(str))	return	dt;
		return	str;	
	}
	/** テーブル名を返す */
	String	dbName(String dbname){
		return		dbname + "_" + szDB;
	}
	/**
	 * ownerkey forumkey でひとつのレコードを読む
	 * レコードをハッシュテーブルに格納して返す
	 */
	public int readBbsForums(String ownerkey, String forumkey, Hashtable tb){
		
		String	cmd 	= 	" SELECT * FROM  " + dbName("bbsforum") + "  WHERE	ownerkey = " + m1 + ownerkey + m1 
																		+ " AND forumkey = " + m1 + forumkey + m1;
		if(LOG.fa) LOG.println("BbsForumDB #readBbsForum() : SQL = " + cmd);
		
		int		count	=	0;
		Connection  conn	=	null;
		Statement 	stmt	=	null;
		try {
			conn 		 	=	Broker.getConnection();
			stmt 			=	conn.createStatement();
			ResultSet	rs	=	stmt.executeQuery(cmd);
			//
			while(rs.next()){
				tb.put(OWNER_KEY	,setValidData(rs.getString("ownerkey")	,"????????????"));
				tb.put(FORUM_KEY	,setValidData(rs.getString("forumkey")	,"????"));
				tb.put(ALIVE_FLAG	,setValidData(rs.getString("alive")		,HIDDEN));
				tb.put(GROUP_FLAG	,setValidData(rs.getString("gpflag")	,NORMAL_BBS));
				tb.put(RATING_FLAG	,setValidData(rs.getString("rtflag")	,NOT_RATING_MODE));
				tb.put(HANDLE_FLAG	,setValidData(rs.getString("hdflag")	,BY_HANDLE));
				tb.put(DATE			,setValidData(rs.getString("date")		,""));
				tb.put(SUBJECT		,setValidData(rs.getString("subject")	,""));
				tb.put(CONTENT		,setValidData(rs.getString("content")	,""));
				tb.put(ATTACHMENT	,setValidData(rs.getString("attachment"),""));
				tb.put(RELATION		,setValidData(rs.getString("relation")	,""));
				tb.put(WATCH		,setValidData(rs.getString("watch")		,""));
				//
				toNomal(tb);
				count++;
			}
		}catch(Exception e){
			if(LOG.fa) { LOG.println("#### 失敗: BbsForumDB #readBbsForums ( ownerkey=" + ownerkey + ", forumkey =" +forumkey +  ")" + e); }
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}	

	/**
	 * ownerkey で複数のレコードを読む
	 * １レコードをひとつのハッシュテーブルに格納
	 * これを引数の Vector に 読み出し順に格納する
	 * 同じリレーションのものだけ
	 * 
	 */
	public int readBbsForums(String ownerkey, String relation, Vector v){
		
		String	cmd 	= 	" SELECT * FROM  " + dbName("bbsforum") + "  WHERE ownerkey = " + m1 + ownerkey + m1 + " AND relation = " + m1 + relation + m1 + " ORDER BY date DESC" ;
		if(LOG.fa) LOG.println("BbsForumDB #readBbsForum() : SQL = " + cmd);
		
		int		count	=	0;
		Connection  conn	=	null;
		Statement 	stmt	=	null;
		try {
			conn 		 	=	Broker.getConnection();
			stmt 			=	conn.createStatement();
			ResultSet	rs	=	stmt.executeQuery(cmd);
			//
			while(rs.next()){
				Hashtable	dt	=	new Hashtable(20);
				dt.put(OWNER_KEY	,setValidData(rs.getString("ownerkey")	,"????????????"));
				dt.put(FORUM_KEY	,setValidData(rs.getString("forumkey")	,"????"));
				dt.put(ALIVE_FLAG	,setValidData(rs.getString("alive")		,ACTIVE));
				dt.put(GROUP_FLAG	,setValidData(rs.getString("gpflag")	,NORMAL_BBS));
				dt.put(RATING_FLAG	,setValidData(rs.getString("rtflag")	,NOT_RATING_MODE));
				dt.put(HANDLE_FLAG	,setValidData(rs.getString("hdflag")	,BY_HANDLE));
				dt.put(DATE			,setValidData(rs.getString("date")		,""));
				dt.put(SUBJECT		,setValidData(rs.getString("subject")	,""));
				dt.put(CONTENT		,setValidData(rs.getString("content")	,""));
				dt.put(ATTACHMENT	,setValidData(rs.getString("attachment"),""));
				dt.put(RELATION		,setValidData(rs.getString("relation")	,""));
				dt.put(WATCH		,setValidData(rs.getString("watch")		,""));
				//
				toNomal(dt);
				v.add(dt);
				count++;
			}
		}catch(Exception e){
			if(LOG.fa) { LOG.println("#### 失敗: BbsForumDB #readBbsForums ( ownerkey=" + ownerkey + " )" + e); }
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}
	/**
	 * ownerkey で複数のレコードを読む
	 * １レコードをひとつのハッシュテーブルに格納
	 * これを引数の Vector に 読み出し順に格納する
	 * 同じリレーションのものだけ
	 * 
	 */
	public int readNotRelatedBbsForums(String ownerkey, String relation, Vector v){
		
		String	cmd 	= 	" SELECT * FROM  " + dbName("bbsforum") + "  WHERE ownerkey = " + m1 + ownerkey + m1 + " AND relation <> " + m1 + relation + m1 + " ORDER BY date DESC" ;
		if(LOG.fa) LOG.println("BbsForumDB #readBbsForum() : SQL = " + cmd);
		
		int		count	=	0;
		Connection  conn	=	null;
		Statement 	stmt	=	null;
		try {
			conn 		 	=	Broker.getConnection();
			stmt 			=	conn.createStatement();
			ResultSet	rs	=	stmt.executeQuery(cmd);
			//
			while(rs.next()){
				Hashtable	dt	=	new Hashtable(20);
				dt.put(OWNER_KEY	,setValidData(rs.getString("ownerkey")	,"????????????"));
				dt.put(FORUM_KEY	,setValidData(rs.getString("forumkey")	,"????"));
				dt.put(ALIVE_FLAG	,setValidData(rs.getString("alive")		,ACTIVE));
				dt.put(GROUP_FLAG	,setValidData(rs.getString("gpflag")	,NORMAL_BBS));
				dt.put(RATING_FLAG	,setValidData(rs.getString("rtflag")	,NOT_RATING_MODE));
				dt.put(HANDLE_FLAG	,setValidData(rs.getString("hdflag")	,BY_HANDLE));
				dt.put(DATE			,setValidData(rs.getString("date")		,""));
				dt.put(SUBJECT		,setValidData(rs.getString("subject")	,""));
				dt.put(CONTENT		,setValidData(rs.getString("content")	,""));
				dt.put(ATTACHMENT	,setValidData(rs.getString("attachment"),""));
				dt.put(RELATION		,setValidData(rs.getString("relation")	,""));
				dt.put(WATCH		,setValidData(rs.getString("watch")		,""));
				//
				toNomal(dt);
				v.add(dt);
				count++;
			}
		}catch(Exception e){
			if(LOG.fa) { LOG.println("#### 失敗: BbsForumDB #readBbsForums ( ownerkey=" + ownerkey + " )" + e); }
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}
	/**
	 * ownerkey で複数のレコードを読む
	 * １レコードをひとつのハッシュテーブルに格納
	 * これを引数の Vector に 読み出し順に格納する
	 * 
	 */
	public int readBbsForums(String ownerkey, Vector v){
		
		String	cmd 	= 	" SELECT * FROM  " + dbName("bbsforum") + "  WHERE ownerkey = " + m1 + ownerkey + m1 + " ORDER BY date DESC" ;
		if(LOG.fa) LOG.println("BbsForumDB #readBbsForum() : SQL = " + cmd);
		
		int		count	=	0;
		Connection  conn	=	null;
		Statement 	stmt	=	null;
		try {
			conn 		 	=	Broker.getConnection();
			stmt 			=	conn.createStatement();
			ResultSet	rs	=	stmt.executeQuery(cmd);
			//
			while(rs.next()){
				Hashtable	dt	=	new Hashtable(20);
				dt.put(OWNER_KEY	,setValidData(rs.getString("ownerkey")	,"????????????"));
				dt.put(FORUM_KEY	,setValidData(rs.getString("forumkey")	,"????"));
				dt.put(ALIVE_FLAG	,setValidData(rs.getString("alive")		,ACTIVE));
				dt.put(GROUP_FLAG	,setValidData(rs.getString("gpflag")	,NORMAL_BBS));
				dt.put(RATING_FLAG	,setValidData(rs.getString("rtflag")	,NOT_RATING_MODE));
				dt.put(HANDLE_FLAG	,setValidData(rs.getString("hdflag")	,BY_HANDLE));
				dt.put(DATE			,setValidData(rs.getString("date")		,""));
				dt.put(SUBJECT		,setValidData(rs.getString("subject")	,""));
				dt.put(CONTENT		,setValidData(rs.getString("content")	,""));
				dt.put(ATTACHMENT	,setValidData(rs.getString("attachment"),""));
				dt.put(RELATION		,setValidData(rs.getString("relation")	,""));
				dt.put(WATCH		,setValidData(rs.getString("watch")		,""));
				//
				toNomal(dt);
				v.add(dt);
				count++;
			}
		}catch(Exception e){
			if(LOG.fa) { LOG.println("#### 失敗: BbsForumDB #readBbsForums ( ownerkey=" + ownerkey + " )" + e); }
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}	
	
	
	/**
	 * フォーラムレコードを更新する
	 * @param ownerkey		オーナーキー
	 * @param forumkey		フォーラムキー
	 * @param dt			更新データを入れたハッシュ
	 * @return				更新件数（通常は１）
	 */
	public int updateBbsForum(Hashtable dt){
		
		int		count	=	0;
		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		String		query	=	updateQueryBbsForum(dt);
		//
		try {
			conn 	=	Broker.getConnection();
			stmt 	=	conn.createStatement();
			count	=	stmt.executeUpdate(query);
		
		}catch(Exception e){
			System.out.println("#### 失敗:BbsForumDB #updateBbsForum( " + Gear.strHash(dt,"_ownerkey") + ", Hashtable dt) " + e);
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}	
	String	updateQueryBbsForum(Hashtable dt ){
	    /*
	     * \ " ' を特殊文字に直して書き込む
	     */
	    toDefined(dt);
	    
		String	query	=	"UPDATE "+ dbName("bbsforum") + " set " 
							+ " alive   = "  	+ m1 + Gear.strHash(dt,ALIVE_FLAG  	, HIDDEN)  	    	+ m2
							+ " gpflag  = "  	+ m1 + Gear.strHash(dt,GROUP_FLAG 	, NORMAL_BBS)     	+ m2
							+ " rtflag  = "  	+ m1 + Gear.strHash(dt,RATING_FLAG 	, NOT_RATING_MODE)	+ m2
							+ " hdflag  = "  	+ m1 + Gear.strHash(dt,HANDLE_FLAG 	, BY_HANDLE)		+ m2
							+ " date    = "  	+ m1 + Gear.strHash(dt,DATE	  , "")     				+ m2
							+ " subject = "  	+ m1 + Gear.strHash(dt,SUBJECT, "")     				+ m2
							+ " content = "  	+ m1 + Gear.strHash(dt,CONTENT, "")     				+ m2
							+ " attachment = "  + m1 + Gear.strHash(dt,ATTACHMENT, "")     				+ m1
							/*
							 * relation はアップデートしない
							 * watch    はアップデートしない
							 */
							//+ " relation   = "  + m1 + Gear.strHash(dt,RELATION, "")     				+ m1
							//
							+ " WHERE ownerkey  = " + m1 + Gear.strHash(dt,OWNER_KEY) + m1
							+ " AND   forumkey  = " + m1 + Gear.strHash(dt,FORUM_KEY) + m1;							

		if(LOG.fa) LOG.println("class BbsForumDB #updateQueryBbsForum() : query =" +  query);
		return query;
	}
	/**
	 * リレーションのみを更新する
	 * @param ownerkey		オーナーキー
	 * @param forumkey		フォーラムキー
	 * @param dt			更新データを入れたハッシュ
	 * @return				更新件数（通常は１）
	 */
	public int updateRelation(String ownerkey, String forumkey, String relation){
		
		int		count	=	0;
		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		String		query	=	updateRelationQuery(ownerkey, forumkey, relation);
		//
		try {
			conn 	=	Broker.getConnection();
			stmt 	=	conn.createStatement();
			count	=	stmt.executeUpdate(query);
		
		}catch(Exception e){
			System.out.println("#### 失敗:BbsForumDB #updateRelation( " + ownerkey + ", " + forumkey + ")" );
			e.printStackTrace();
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}	
	String	updateRelationQuery(String ownerkey, String forumkey, String relation){
		String	query	=	"UPDATE "+ dbName("bbsforum") + " set " 
							+ " relation   = "  	+ m1 + relation	+ m1
							+ " WHERE ownerkey  = " + m1 + ownerkey + m1
							+ " AND   forumkey  = " + m1 + forumkey + m1;							

		if(LOG.fa) LOG.println("class BbsForumDB #updateRelationQuery() : query =" +  query);
		return query;
	}
	/**
	 * ＷＡＴＣＨのみを更新する
	 * @param ownerkey		オーナーキー
	 * @param forumkey		フォーラムキー
	 * @param dt			更新データを入れたハッシュ
	 * @return				更新件数（通常は１）
	 */
	public int updateWatch(String ownerkey, String forumkey, String watch){
		
		int		count	=	0;
		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		String		query	=	updateWatchQuery(ownerkey, forumkey, watch);
		//
		try {
			conn 	=	Broker.getConnection();
			stmt 	=	conn.createStatement();
			count	=	stmt.executeUpdate(query);
		
		}catch(Exception e){
			System.out.println("#### 失敗:BbsForumDB #updateWatch( " + ownerkey + ", " + forumkey + ")" );
			e.printStackTrace();
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}	
	String	updateWatchQuery(String ownerkey, String forumkey, String watch){
		String	query	=	"UPDATE "+ dbName("bbsforum") + " set " 
							+ " watch           = " + m1 + watch	+ m1
							+ " WHERE ownerkey  = " + m1 + ownerkey + m1
							+ " AND   forumkey  = " + m1 + forumkey + m1;							

		if(LOG.fa) LOG.println("class BbsForumDB #updateWatchQuery() : query =" +  query);
		return query;
	}	
	/**
	 * フォーラムのアライブフラグを更新する
	 * @param ownerkey		オーナーキー
	 * @param forumkey		フォーラムキー
	 * @param dt			更新データを入れたハッシュ
	 * @return				更新件数（通常は１）
	 */
	public int updateAlive(String ownerkey, String forumkey, String alive){
		
		int		count	=	0;
		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		String		query	=	updateAliveQuery(ownerkey, forumkey, alive);
		//
		try {
			conn 	=	Broker.getConnection();
			stmt 	=	conn.createStatement();
			count	=	stmt.executeUpdate(query);
		
		}catch(Exception e){
			System.out.println("#### 失敗:BbsForumDB #updateAlive( " + ownerkey + ", " + forumkey + ")" );
			e.printStackTrace();
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}	
	String	updateAliveQuery(String ownerkey, String forumkey, String alive){
		String	query	=	"UPDATE "+ dbName("bbsforum") + " set " 
							+ " alive   = "  	+ m1 + alive	+ m1
							+ " WHERE ownerkey  = " + m1 + ownerkey + m1
							+ " AND   forumkey  = " + m1 + forumkey + m1;							

		if(LOG.fa) LOG.println("class BbsForumDB #updateAliveQuery() : query =" +  query);
		return query;
	}	

	/**
	 * 新規レコードを挿入する
	 * @param ownerkey		オーナーキー
	 * @param forumkey		フォーラムキー
	 * @param dt			挿入データを入れたハッシュ
	 * @return				挿入件数（通常は１）
	 */
	public int insertBbsForum(Hashtable dt ){
	    /*
	     * \ " ' を特殊文字に直して書き込む
	     */
	    toDefined(dt);
	    
		Connection  conn  	= null;
		Statement 	stmt  	= null;
		int 		count	= 0;
		String query = insertQueryBbsForum(dt);
		//
		try {
			conn 	= Broker.getConnection();
			stmt 	= conn.createStatement();
			count	= stmt.executeUpdate(query);
			
		}catch(Exception e){
			System.out.println("### 失敗:BbsForumDB #insertBbsForum(" + Gear.strHash(dt,"_ownerkey") +"," + Gear.strHash(dt,"_forumkey") + ")" + e);
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}
	String	insertQueryBbsForum(Hashtable dt ){
		String query = " INSERT INTO " + dbName("bbsforum") + "  VALUES (" 
					+ m1 + Gear.strHash(dt,OWNER_KEY	,"????????????")	+ m2 
					+ m1 + Gear.strHash(dt,FORUM_KEY	,"????")			+ m2 
					+ m1 + Gear.strHash(dt,ALIVE_FLAG	 	, HIDDEN)  	    + m2
					+ m1 + Gear.strHash(dt,GROUP_FLAG 	, NORMAL_BBS)     	+ m2
					+ m1 + Gear.strHash(dt,RATING_FLAG 	, NOT_RATING_MODE) 	+ m2
					+ m1 + Gear.strHash(dt,HANDLE_FLAG 	, BY_HANDLE) 		+ m2
					+ m1 + Gear.strHash(dt,DATE	 		, "")     			+ m2
					+ m1 + Gear.strHash(dt,SUBJECT		, "")     			+ m2
					+ m1 + Gear.strHash(dt,CONTENT		, "")     			+ m2
					+ m1 + Gear.strHash(dt,ATTACHMENT	, "")     			+ m2
					+ m1 + Gear.strHash(dt,RELATION		, "")     			+ m2
					+ m1 + Gear.strHash(dt,WATCH		, "")     			+ m1
					+ ")";
		if(LOG.fa) LOG.println("BbsForumDB #insertQueryBbsForum() : query =" +  query);
		return query;
	}	
	
	/**
	 * 
	 * @param ownerkey		オーナーキー
	 * @param forumkey		フォーラムキー
	 * @return				削除件数（通常は１）
	 */
	public int deleteBbsForum( String ownerkey, String forumkey){

		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		int 		n		=	0;
		String		query	=	"DELETE from " 	+ dbName("bbsforum") 
												+ " WHERE ownerkey  = " + m1 + ownerkey    + m1
												+ " AND   forumkey  = " + m1 + forumkey    + m1;							
		if(LOG.fa) LOG.println("BbsForumDB #deleteBbsForum() : query = " +  query);
		
		try {
			conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
			stmt 	= conn.createStatement();
			n  		= stmt.executeUpdate(query); // ここでdeleteする
			//
		}catch(Exception e){
			System.out.println("### 失敗:BbsForumDB #deleteBbsForum(" + ownerkey +"," + forumkey + ")" + e);
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			//
			Broker.freeConnection(conn);/* connection を Broker に返す */
		}
		return n; // 件数（普通は１）
	}	
	
}
