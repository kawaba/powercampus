/*
 * 個人情報
  bbsInfo_GROUPNAME
  
      	 * <<BbsInfoDB>>
      	 *  userid            VARCHAR(12)  PRIMARY KEY
      	 *  passwd            VARCHAR(20)  PRIMARY KEY
      	 *  division          CHAR(1)      DEFAULT '4'
      	 *  name              VARCHAR(30)
      	 *  mail			  VARCHAR(30)
      	 *  handle            VARCHAR(30)
      	 *  iconfile          VARCHAR(30)
      	 *  signature         TEXT
      	 *  formatStyle       TEXT
      	 *  editor            CHAR(2)

 *
 *  
 * 教師と学生は、kougi,StKougiからforumへ分岐する際にこのデータベースに記録される 
 * また、BbsLoginから入った場合は、パスワードチェックの際にこのデータベースに登録される
 * 
 */
package jbbs;
import	 tktools.*;
import  java.sql.*;
import	 java.util.*;
import database.DbConnectionBroker;
import framework.LOG;
/**
 * bbsInfo_GROUP テーブルを直接操作する
 * 
 */
public class BbsInfoDB extends Object{
    
	public	static	final	String	USER_ID		=	"_userid";
	public	static	final	String	USER_PASSWD	=	"_passwd";
	public	static	final	String	DIVISION	=	"_division";
	public	static	final	String	NAME		=	"_name";
	public	static	final	String	MAIL		=	"_mail";
	public	static	final	String	HANDLE		=	"_handle";
	public	static	final	String	ICON_FILE	=	"_iconfile";
	public	static	final	String	SIGNATURE	=	"_signature";
	public	static	final	String	FORMAT		=	"_formatStyle";
	public	static	final	String	EDITOR		=	"_editor";
	    
	
	
	/** エディタ行数の範囲 */
	public	static	final	int	maxRows		=	60;
	public	static	final	int	minRows		=	20;
	public	static	final	int	delta		=	2;		//増分
	
	private final String m1		= "\'";
	private final String m2		= "\',";
	private final String m3		= ",";
	//
	public	static	final	String	SUPER		=	"0";
	public	static	final	String	TEACHER		=	"1";
	public	static	final	String	ASSISTANT	=	"2";
	public	static	final	String	STUDENT		=	"3";
	public	static	final	String	GUEST		=	"4";
	
	public	static	final	String	STD_FMT		=	"## p3 flow = 700";
	public	static	final	String	CR			=	Gear.CR;
	//
	DbConnectionBroker 	Broker;
	String				szDB;
	String				userid;
	//
	public	BbsInfoDB(String userid, String szDB, DbConnectionBroker Broker){
		this.userid		= userid;
		this.szDB		= szDB;
		this.Broker 	= Broker;
	}
	/**
	 * 実際には usereidは使っていないのでこちらのコンストラクタでよいはず
	 * @param szDB
	 * @param Broker
	 */
	public	BbsInfoDB(String szDB, DbConnectionBroker Broker){
		this.userid		= "";
		this.szDB		= szDB;
		this.Broker 	= Broker;
	}	
	/** テーブル名を返す */
	String	dbName(String dbname){
		return		dbname + "_" + szDB;
	}
	/** null を "" として返す */
	String	nullFilter(String str,String dt){
		if(Gear.isEmpty(str))	return	dt;
		return	str;	
	}
	
	/** 初期値をハッシュにセットする */
	public static Hashtable	getInitialProfile(String uid, String name){
		Hashtable tb	=	new Hashtable(); 
		tb.put(USER_ID		, uid);
		tb.put(USER_PASSWD	, "");
		tb.put(DIVISION		, GUEST);
		tb.put(NAME			, name);
		tb.put(MAIL			, "");
		tb.put(HANDLE		, "");
		tb.put(ICON_FILE	, "");
		tb.put(SIGNATURE	, "");
		tb.put(FORMAT		, "## $p1(indent=20 size=16px weight=bold)" + CR + STD_FMT);
		tb.put(EDITOR		, "20");
		return tb;	
	}
	
	/* レコードを読む */
	public int readBbsInfo(String prmkey,Hashtable dt){
		
		String	cmd 	= 	" SELECT * FROM  " + dbName("BbsInfo") + "  WHERE userid = " + m1 + prmkey + m1;
		if(LOG.fa) LOG.println("class BbsInfoDB #readBbsInfo() : SQL = " + cmd);
		
		int		count	=	0;
		Connection  conn	=	null;
		Statement 	stmt	=	null;
		try {
			conn 		 	=	Broker.getConnection();
			stmt 			=	conn.createStatement();
			ResultSet	rs	=	stmt.executeQuery(cmd);
			//
			while(rs.next()){
				dt.put(USER_ID		,nullFilter(rs.getString("userid")		,"????????????"));
				dt.put(USER_PASSWD	,nullFilter(rs.getString("passwd")		,""));
				
				dt.put(DIVISION		,nullFilter(rs.getString("division")	,GUEST));
				dt.put(NAME			,nullFilter(rs.getString("name")		,""));
				dt.put(MAIL			,nullFilter(rs.getString("mail")		,""));
				dt.put(HANDLE		,nullFilter(rs.getString("handle")		,""));
				dt.put(ICON_FILE	,nullFilter(rs.getString("iconfile")	,""));
				dt.put(SIGNATURE	,nullFilter(rs.getString("signature")	,""));
				dt.put(FORMAT		,nullFilter(rs.getString("formatStyle")	,""));
				dt.put(EDITOR		,nullFilter(rs.getString("editor")		,"20"));
				count++;
			}
		}catch(Exception e){
			if(LOG.fa) { LOG.println("#### 失敗:class BbsInfoDB #readBbsInfo( " + prmkey + " )" + e); }
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}	

	/* レコードを更新する */
	public int updateBbsInfo( String prmkey,Hashtable dt){
		
		int		count	=	0;
		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		String		query	=	updateQueryBbsInfo(prmkey, dt);
		//
		try {
			conn 	=	Broker.getConnection();
			stmt 	=	conn.createStatement();
			count	=	stmt.executeUpdate(query);
		
		}catch(Exception e){
			System.out.println("#### 失敗:class BbsInfoDB #updateBbsInfo( " + prmkey + ", Hashtable dt) " + e);
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}	
	String	updateQueryBbsInfo(String prmkey, Hashtable dt ){
		String	query	=	"UPDATE "+ dbName("BbsInfo") + " set " 
							+ "passwd      = "  + m1 + Gear.strHash(dt,USER_PASSWD	,"")  	+ m2
							+ "division    = "  + m1 + Gear.strHash(dt,DIVISION		,GUEST) + m2
							+ "name        = "  + m1 + Gear.strHash(dt,NAME			,"")    + m2
							+ "mail        = "  + m1 + Gear.strHash(dt,MAIL			,"")    + m2
							+ "handle      = "  + m1 + Gear.strHash(dt,HANDLE		,"")    + m2
							+ "iconfile    = "  + m1 + Gear.strHash(dt,ICON_FILE	,"")   	+ m2
							+ "signature   = "  + m1 + Gear.strHash(dt,SIGNATURE	,"")  	+ m2
							+ "formatStyle = "  + m1 + Gear.strHash(dt,FORMAT		,"")    + m2
							+ "editor      = "  + m1 + Gear.strHash(dt,EDITOR		,"20")  + m1
							//
							+ " WHERE userid  = " + m1 + prmkey     + m1;

		if(LOG.fa) LOG.println("class BbsInfoDB #updateQueryBbsInfo() : query =" +  query);
		return query;
	}
	
	/* レコードを挿入する */
	public int insertBbsInfo( String prmkey, Hashtable dt ){
		
		Connection  conn  	= null;
		Statement 	stmt  	= null;
		int 		count	= 0;
		String query = insertQueryBbsInfo(prmkey,dt);
		//
		try {
			conn 	= Broker.getConnection();
			stmt 	= conn.createStatement();
			count	= stmt.executeUpdate(query);
			
		}catch(Exception e){
		    if(LOG.fa) LOG.println("### 失敗:class BbsInfoDB #insertBbsInfo(" + prmkey +",datakey )" + e);
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}
	String	insertQueryBbsInfo(String prmkey, Hashtable dt ){
		String query = " INSERT INTO " + dbName("BbsInfo") + "  VALUES (" 
					+ m1 + Gear.strHash(dt,USER_ID		,"????????????")+ m2 
					+ m1 + Gear.strHash(dt,USER_PASSWD	,"")			+ m2
					+ m1 + Gear.strHash(dt,DIVISION		,GUEST)  		+ m2 
					+ m1 + Gear.strHash(dt,NAME			,"")    		+ m2
					+ m1 + Gear.strHash(dt,MAIL			,"")    		+ m2
					+ m1 + Gear.strHash(dt,HANDLE		,"")    		+ m2 
					+ m1 + Gear.strHash(dt,ICON_FILE	,"")  			+ m2
					+ m1 + Gear.strHash(dt,SIGNATURE	,"") 			+ m2
					+ m1 + Gear.strHash(dt,FORMAT		,"") 			+ m2 
					+ m1 + Gear.strHash(dt,EDITOR		,"20") 			+ m1
					+ ")";
		if(LOG.fa) LOG.println("class BbsInfoDB #insertQueryBbsInfo() : query =" +  query);
		return query;
	}
	
	/* レコードを削除する */
	public int deleteBbsInfo( String prmkey){

		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		int 		n		=	0;
		String		query	=	"DELETE from " + dbName("BbsInfo") + " WHERE userid = "  + m1 + prmkey + m1;
		if(LOG.fa) LOG.println("class BbsInfoDB #deleteBbsInfo() : query = " +  query);
		
		try {
			conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
			stmt 	= conn.createStatement();
			n  		= stmt.executeUpdate(query); // ここでdeleteする
			//
		}catch(Exception e){
			System.out.println("### 失敗:class BbsInfoDB #deleteBbsInfo(" + prmkey + ")" + e);
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			//
			Broker.freeConnection(conn);/* connection を Broker に返す */
		}
		return n; // 件数（普通は１）
	}	
	
	/* エディタの行数を更新する */
	public int updateEditorRow( String prmkey,String rows){
		
		int		count	=	0;
		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		String		query	=	updateQueryEditorRow(prmkey, rows);
		//
		try {
			conn 	=	Broker.getConnection();
			stmt 	=	conn.createStatement();
			count	=	stmt.executeUpdate(query);
		
		}catch(Exception e){
			System.out.println("#### 失敗:class BbsInfoDB #updateEditorRow( " + prmkey + ", Hashtable dt) " + e);
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}	
	String	updateQueryEditorRow(String prmkey, String rows ){
		String	query	=	"UPDATE "+ dbName("BbsInfo") + " set editor = " + m1 + rows + m1 + " WHERE userid = " + m1 + prmkey + m1;

		if(LOG.fa) LOG.println("■ class BbsInfoDB #updateQueryEditorRow() : query =" +  query);
		return query;
	}
	
	/**
	 * アイコンファイル名だけを更新する
	 */
	public int updateIcon( String prmkey,String iconfileName){
		
		int		count	=	0;
		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		String		query	=	updateQueryupdateIcon(prmkey, iconfileName);
		//
		try {
			conn 	=	Broker.getConnection();
			stmt 	=	conn.createStatement();
			count	=	stmt.executeUpdate(query);
		
		}catch(Exception e){
			System.out.println("#### 失敗:class BbsInfoDB #updateIcon( " + prmkey + ", Hashtable dt) " + e);
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}	
	String	updateQueryupdateIcon(String prmkey, String iconfileName ){
		String	query	=	"UPDATE "+ dbName("BbsInfo") + " set iconfile = " + m1 + iconfileName + m1 + " WHERE userid = " + m1 + prmkey + m1;

		if(LOG.fa) LOG.println("■ class BbsInfoDB #updateQueryupdateIcon() : query =" +  query);
		return query;
	}
	/**
	 * DIVISION だけを更新する
	 */
	public int updateDiv( String prmkey,String div){
		int		count	=	0;
		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		String		query	=	updateQueryUpdateDiv(prmkey, div);
		//
		try {
			conn 	=	Broker.getConnection();
			stmt 	=	conn.createStatement();
			count	=	stmt.executeUpdate(query);
		
		}catch(Exception e){
			System.out.println("#### 失敗:class BbsInfoDB #updateDiv( " + prmkey + "," + div +") " + e);
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	    
	}
	String	updateQueryUpdateDiv(String prmkey, String div ){
		String	query	=	"UPDATE "+ dbName("BbsInfo") + " set division = " + m1 + div + m1 + " WHERE userid = " + m1 + prmkey + m1;

		if(LOG.fa) LOG.println("■ class BbsInfoDB #updateQueryUpdateSig() : query =" +  query);
		return query;
	}
	/**
	 * 個人情報だけを更新する
	 */		
	public int updateSig( String prmkey,String sig){
		
		int		count	=	0;
		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		String		query	=	updateQueryUpdateSig(prmkey, sig);
		//
		try {
			conn 	=	Broker.getConnection();
			stmt 	=	conn.createStatement();
			count	=	stmt.executeUpdate(query);
		
		}catch(Exception e){
			System.out.println("#### 失敗:class BbsInfoDB #updateSig( " + prmkey + "," + sig +") " + e);
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}	
	String	updateQueryUpdateSig(String prmkey, String sig ){
		String	query	=	"UPDATE "+ dbName("BbsInfo") + " set signature = " + m1 + sig + m1 + " WHERE userid = " + m1 + prmkey + m1;

		if(LOG.fa) LOG.println("■ class BbsInfoDB #updateQueryUpdateSig() : query =" +  query);
		return query;
	}
	/**
	 * ユーザーのメールアドレスを返す
	 * @param uid
	 * @return
	 */
	public	String	getMailAddress(String	uid){
	    Hashtable	ht	=	new	Hashtable();
	    readBbsInfo(uid, ht);
	    String		adr	=	Gear.strHash(ht, MAIL);
	    return		adr;
	}
	
}
