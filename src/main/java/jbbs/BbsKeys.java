/*
 * 作成日: 2004/08/12
 *
 * BBSのキーを生成する
 * 
 */
package jbbs;
import		tktools.*;
import 	java.sql.*;

import database.DbConnectionBroker;

import framework.LOG;

/**
 * BBSのキー
 *  
 * create table forumkey_GROUPNAME (
 *     ownerkey         VARCHAR(12)    PRIMARY KEY,
 *     forumkey         CHAR(4),
 * }
 * 
 * create table threadkey_GROUPNAME (
 *     owner_forumkey   VARCHAR(17)    PRIMARY KEY,
 *     threadkey        CHAR(4)
 * }
 * 
 * create table postkey_GROUPNAME (
 *     bbskey           VARCHAR(22)   PRIMARY KEY,
 *     postkey          CHAR(4)
 * }
 * 
 */
public class BbsKeys extends Object {
	
	private final String m1		= "\'";
	private final String m2		= "\',";
	private final String m3		= ",";
	//
	final	String	TOP_FORUM_KEY	=	"0001";	
	final	String	TOP_THREAD_KEY	=	"0001";
	final	String	TOP_POST_KEY	=	"00001";
	//
	DbConnectionBroker 	Broker;
	String				szDB;
	String				userid;
	//
	public	BbsKeys(String userid, String szDB, DbConnectionBroker Broker){
		this.userid	= userid;
		this.szDB		= szDB;
		this.Broker 	= Broker;
	}

	/** テーブル名を返す */
	String	dbName(String dbname){
		return		dbname + "_" + szDB;
	}
	/** ４桁のキーの値を１増やしたものを返す */
	String	increase(String	key){
		int	val	=	Integer.parseInt(key);
		val++;
		return	Gear.get0000type(val);
	}
	/** ５桁のキーの値を１増やしたものを返す */
	String	increase_5d(String	key){
		int	val	=	Integer.parseInt(key);
		val++;
		return	Gear.get00000type(val);
	}	
	/*
	 * キーを作成するメソッド
	 * 
	 *	mbcd08910000					ownerkey(12)
	 *	mbcd08910000_0001				owner_forumkey(17)
	 *	mbcd08910000_0001_0002			bbskey(22)	
	 *	mbcd08910000_0001_0002_00013  	full key : _ で各部分を連結 28桁
	 */
	/**
	 * フォラムのフルキーを作成する 
	 * @param ownerKey		オーナーキー（ユーザーID）
	 * @param forumKey		フォーラムキー
	 * @return				フォラムのフルキー
	 */
	public	static String	ownerForumKey(String ownerKey, String forumKey){
		return	ownerKey + "_" + forumKey;
	}
	/**
	 * スレッドのフルキーを作成する 
	 * @param ownerKey		オーナーキー（ユーザーID）
	 * @param forumKey		フォーラムキー
	 * @param threadKey	スレッドキー
	 * @return				スレッドのフルキー
	 */
	public	static	String	bbsKey(String ownerKey, String forumKey, String threadKey){
		return	ownerForumKey(ownerKey, forumKey) + "_" + threadKey;
	}
	/**
	 * スレッドのフルキーを作成する 
	 * @param ownerForumKey	フォーラムのフルキー
	 * @param threadKey		スレッドキー
	 * @return					スレッドのフルキー
	 */
	public	static	String	bbsKey(String ownerForumKey, String threadKey){
		return	ownerForumKey + "_" + threadKey;
	}
	
	
	
	////////////////////////////////////
	//////////  フォーラムキー　　///////
	////////////////////////////////////
	/** フォーラムキーを得る */
	public	synchronized	String	getForumKey(String ownerkey){
		String	key	=	readForumkey(ownerkey);
		if(Gear.isEmpty(key)){
			key	=	initForumKey(ownerkey);
		}else{
			increaseForumKey(ownerkey,key);
		}
		return	key;
	}
	/** フォーラムキーのレコードを作成する **/	
	String	initForumKey(String ownerkey){
		/* 次に使うキーをデータベースに記録しておく */
		insertForumkey(ownerkey,increase(TOP_FORUM_KEY));
		return	TOP_FORUM_KEY;
	}
	/** フォーラムキーの値を１増やして、データベースを更新する */
	void	increaseForumKey(String ownerkey, String key){
		String	newkey	=	increase(key);
		updateForumkey(ownerkey,newkey);	
	}
	
	////////////////////////////////////
	//////////  スレッドキー　　/////////
	////////////////////////////////////
	/** スレッドキーを得る */
	public	synchronized	String	getThreadKey(String owner_forumkey){
		String	key	=	readThreadkey(owner_forumkey);
		if(Gear.isEmpty(key)){
			key	=	initThreadKey(owner_forumkey);
		}else{
			increaseThreadKey(owner_forumkey,key);
		}
		return	key;
	}
	/** スレッドキーのレコードを作成する **/	
	String	initThreadKey(String owner_forumkey){
		insertThreadkey(owner_forumkey,increase(TOP_THREAD_KEY));
		return	TOP_THREAD_KEY;
	}
	/** スレッドキーの値を１増やして、データベースを更新する */
	void	increaseThreadKey(String owner_forumkey, String key){
		String	newkey	=	increase(key);
		updateThreadkey(owner_forumkey,newkey);	
	}
	
	////////////////////////////////////
	//////////  ポストキー　　///////////
	////////////////////////////////////
	/** ポストキーを得る */
	public	synchronized	String	getPostKey(String bbskey){
		String	key	=	readPostkey(bbskey);
		if(Gear.isEmpty(key)){
			key	=	initPostKey(bbskey);
		}else{
			increasePostKey(bbskey, key);
		}
		return	key;
	}
	/** ポストキーのレコードを作成する **/	
	String	initPostKey(String bbskey){
		insertPostkey(bbskey,increasePostKey(bbskey, TOP_POST_KEY));
		return	TOP_POST_KEY;
	}
	/** ポストキーの値を１増やして、データベースを更新する */
	String	increasePostKey(String bbskey,String key){
		String	newkey	=	increase_5d(key);
		updatePostkey(bbskey,newkey);
		return	newkey;
	}

////////////  データベースアクセス　/////////////////////////////////////////////////////////////////////	

	//////////  フォーラムキー   /////////
	
	/* 現在のフォーラムキーを得る */
	String readForumkey(String prmkey){
		
		String	cmd 	= 	" SELECT * FROM  " + dbName("forumkey") + "  WHERE ownerkey = " + m1 + prmkey + m1;
		if(LOG.fa) LOG.println("class BbsKeys #readForumkey() : SQL = " + cmd);
		
		String		retkey	=	"";
		Connection  conn	= null;
		Statement 	stmt	= null;
		try {
			//DBG.println("■■■ readForumkey()");
			conn 		 	=	Broker.getConnection();
			stmt 			=	conn.createStatement();
			ResultSet	rs	=	stmt.executeQuery(cmd);
			rs.next();
			retkey			=	rs.getString("forumkey");

		}catch(Exception e){
			e.printStackTrace();
			if(LOG.fa) { LOG.println("#### 失敗:class BbsKeys #readForumkey( " + prmkey + " )" + e); }
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return retkey;
	}
	
	/* 現在のフォーラムキーを更新する */
	int updateForumkey( String prmkey, String datakey ){
		
		int		count	=	0;
		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		String		query	=	"UPDATE "+ dbName("forumkey") + " set forumkey = " + m1 + datakey + m1 + " WHERE  ownerkey = " + m1 + prmkey + m1;
		if(LOG.fa) LOG.println("class BbsKeys #updateForumkey() : query =" +  query);
		
		try {
			conn 	=	Broker.getConnection();
			stmt 	=	conn.createStatement();
			count	=	stmt.executeUpdate(query);
		
		}catch(Exception e){
			System.out.println("#### 失敗:class BbsKeys #updateForumkey( " + prmkey + ", " + datakey + " ) " + e);
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}

	/* 新しいフォーラムキーレコードを挿入する */
	int insertForumkey( String prmkey, String datakey ){
		
		Connection  conn  	= null;
		Statement 	stmt  	= null;
		int 		count	= 0;
		String query = "INSERT INTO " + dbName("forumkey") +  " VALUES (" + m1 + prmkey + m2 + m1 + datakey + m1 + ")";
		if(LOG.fa) LOG.println("class BbsKeys #insertForumkey() : query = " +  query);
		
		try {
			conn 	= Broker.getConnection();
			stmt 	= conn.createStatement();
			count	= stmt.executeUpdate(query);
			
		}catch(Exception e){
			System.out.println("### 失敗:class BbsKeys #insertForumkey(" + prmkey +"," + datakey + ")" + e);
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count; // 件数（普通は１）
	}
	
	/* フォーラムキーレコードを削除する */
	int deleteForumkey( String prmkey){

		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		int 		n		=	0;
		String		query	=	"DELETE from " + dbName("forumkey") + " WHERE ownerkey = "  + m1 + prmkey + m1;
		if(LOG.fa) LOG.println("class BbsKeys #deleteForumkey() : query = " +  query);
		
		try {
			conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
			stmt 	= conn.createStatement();
			n  		= stmt.executeUpdate(query); // ここでdeleteする
			//
		}catch(Exception e){
			System.out.println("### 失敗:class Bbskeys #deleteForumkey(" + prmkey + ")" + e);
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			//
			Broker.freeConnection(conn);/* connection を Broker に返す */
		}
		return n; // 件数（普通は１）
	}	


	//////////  スレッドキー   /////////


	/* 現在のスレッドキーを得る */
	String readThreadkey(String prmkey){
		
		String	cmd 	= 	" SELECT * FROM  " + dbName("threadkey") + "  WHERE owner_forumkey = " + m1 + prmkey + m1;
		if(LOG.fa) LOG.println("class BbsKeys #readThreadkey() : SQL = " + cmd);
		
		String		retkey	=	"";
		Connection  conn	= null;
		Statement 	stmt	= null;
		try {
			conn 		 	=	Broker.getConnection();
			stmt 			=	conn.createStatement();
			ResultSet	rs	=	stmt.executeQuery(cmd);
			rs.next();
			retkey			=	rs.getString("threadkey");

		}catch(Exception e){
			if(LOG.fa) { LOG.println("#### 失敗:class BbsKeys #readThreadkey( " + prmkey + " )" + e); }
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return retkey;
	}
	
	/* 現在のスレッドキーを更新する */
	int updateThreadkey( String prmkey, String datakey ){
		
		int		count	=	0;
		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		String		query	=	"UPDATE "+ dbName("threadkey") + " set threadkey = " + m1 + datakey + m1 + " WHERE  owner_forumkey = " + m1 + prmkey + m1;
		if(LOG.fa) LOG.println("class BbsKeys #updateThreadkey() : query =" +  query);
		
		try {
			conn 	=	Broker.getConnection();
			stmt 	=	conn.createStatement();
			count	=	stmt.executeUpdate(query);
		
		}catch(Exception e){
			System.out.println("#### 失敗:class BbsKeys #updateThreadkey( " + prmkey + ", " + datakey + " ) " + e);
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}

	/* 新しいスレッドキーレコードを挿入する */
	int insertThreadkey( String prmkey, String datakey ){
		
		Connection  conn  	= null;
		Statement 	stmt  	= null;
		int 		count	= 0;
		String query = "INSERT INTO " + dbName("threadkey") +  " VALUES (" + m1 + prmkey + m2 + m1 + datakey + m1 + ")";
		if(LOG.fa) LOG.println("class BbsKeys #insertThreadkey() : query = " +  query);
		
		try {
			conn 	= Broker.getConnection();
			stmt 	= conn.createStatement();
			count	= stmt.executeUpdate(query);
			
		}catch(Exception e){
			System.out.println("### 失敗:class BbsKeys #insertThreadkey(" + prmkey +"," + datakey + ")" + e);
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count; // 件数（普通は１）
	}
	
	/* スレッドキーレコードを削除する */
	int deleteThreadkey( String prmkey){

		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		int 		n		=	0;
		String		query	=	"DELETE from " + dbName("threadkey") + " WHERE owner_forumkey = "  + m1 + prmkey + m1;
		if(LOG.fa) LOG.println("class BbsKeys #deleteThreadkey() : query = " +  query);
		
		try {
			conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
			stmt 	= conn.createStatement();
			n  		= stmt.executeUpdate(query); // ここでdeleteする
			//
		}catch(Exception e){
			System.out.println("### 失敗:class Bbskeys #deleteThreadkey(" + prmkey + ")" + e);
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			//
			Broker.freeConnection(conn);/* connection を Broker に返す */
		}
		return n; // 件数（普通は１）
	}	



	//////////  ポストキー   /////////


	/* 現在のポストキーを得る */
	String readPostkey(String prmkey){
		
		String	cmd 	= 	" SELECT * FROM  " + dbName("postkey") + "  WHERE bbskey = " + m1 + prmkey + m1;
		if(LOG.fa) LOG.println("class BbsKeys #readPostkey() : SQL = " + cmd);
		
		String		retkey	=	"";
		Connection  conn	= null;
		Statement 	stmt	= null;
		try {
			conn 		 	=	Broker.getConnection();
			stmt 			=	conn.createStatement();
			ResultSet	rs	=	stmt.executeQuery(cmd);
			rs.next();
			retkey			=	rs.getString("postkey");

		}catch(Exception e){
			if(LOG.fa) { LOG.println("#### 失敗:class BbsKeys #readPostkey( " + prmkey + " )" + e); }
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return retkey;
	}
	
	/* 現在のポストキーを更新する */
	int updatePostkey( String prmkey, String datakey ){
		
		int		count	=	0;
		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		String		query	=	"UPDATE "+ dbName("postkey") + " set postkey = " + m1 + datakey + m1 + " WHERE  bbskey = " + m1 + prmkey + m1;
		if(LOG.fa) LOG.println("class BbsKeys #updatePostkey() : query =" +  query);
		
		try {
			conn 	=	Broker.getConnection();
			stmt 	=	conn.createStatement();
			count	=	stmt.executeUpdate(query);
		
		}catch(Exception e){
			System.out.println("#### 失敗:class BbsKeys #updatePostkey( " + prmkey + ", " + datakey + " ) " + e);
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}

	/* 新しいポストキーレコードを挿入する */
	int insertPostkey( String prmkey, String datakey ){
		
		Connection  conn  	= null;
		Statement 	stmt  	= null;
		int 		count	= 0;
		String query = "INSERT INTO " + dbName("postkey") +  " VALUES (" + m1 + prmkey + m2 + m1 + datakey + m1 + ")";
		if(LOG.fa) LOG.println("class BbsKeys #insertPostkey() : query = " +  query);
		
		try {
			conn 	= Broker.getConnection();
			stmt 	= conn.createStatement();
			count	= stmt.executeUpdate(query);
			
		}catch(Exception e){
			System.out.println("### 失敗:class BbsKeys #insertPostkey(" + prmkey +"," + datakey + ")" + e);
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count; // 件数（普通は１）
	}
	
	/* ポストキーレコードを削除する */
	int deletePostkey( String prmkey){

		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		int 		n		=	0;
		String		query	=	"DELETE from " + dbName("postkey") + " WHERE bbskey = "  + m1 + prmkey + m1;
		if(LOG.fa) LOG.println("class BbsKeys #deletePostkey() : query = " +  query);
		
		try {
			conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
			stmt 	= conn.createStatement();
			n  		= stmt.executeUpdate(query); // ここでdeleteする
			//
		}catch(Exception e){
			System.out.println("### 失敗:class Bbskeys #deletePostkey(" + prmkey + ")" + e);
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			//
			Broker.freeConnection(conn);/* connection を Broker に返す */
		}
		return n; // 件数（普通は１）
	}	

}














