/*
 * 作成日: 2005/02/04
 *
 * TODO
 */
package cabinet;

import java.util.*;
import java.sql.*;
import database.DbConnectionBroker;
import framework.*;

/**
 * class 別のインデックス
 * 
 *  accessclass  =  id-lec-key or "everyone" 
 *
 * create table CBclassIndex_GROUPNAME ( 
 *      accessclass  VARCHAR(12),
 *      seqkey       VARCHAR(18)
 *	); 
 * create index CBclassIndex_GROUPNAME_idx on CBclassIndex_GROUPNAME (accessclass);
 * 
 */
public class CBclassIndex implements CBvar{
	
	DbConnectionBroker	Broker;
	String				szDB;		// グループ名
	
	public CBclassIndex(DbConnectionBroker Broker, String szDB){
		this.Broker 	= Broker;		
		this.szDB		= szDB;

		if(LOG.fa)	LOG.println("■ CBclassIndex #コンストラクタ");
	}
	/**
	 * ID と科目キーからアクセスキーを作成して返す
	 * @param id
	 * @param lec_key
	 * @return
	 */
	static	public	String	mkKey(String id, String lec_key){
		if(LOG.fa)	LOG.println("■ CBclassIndex #mkKey()");
		return	id + "-" + lec_key;
	}
	/**
	 *  テーブル名を返す
	 *  
	 * @param dbname
	 * @return
	 */
	public	String	dbName(String dbname){
		if(LOG.fa)	LOG.println("■ CBclassIndex #dbName()");
		return		dbname + "_" + szDB;
	}
	/**
	 * 全てのレコードを得る
	 * @param v
	 * @return
	 */
	public	int read(Vector v){
		if(LOG.fa)	LOG.println("■ CBclassIndex #read()[1]");
		
		String	cmd 	= 	" SELECT * FROM  " + dbName("CBclassIndex") +  " ORDER BY seqkey ASC";
		if(LOG.fa) LOG.println("CBclassIndex #read() : SQL = " + cmd);
		
		int 		count	= 0;
		Connection  conn	= null;
		Statement 	stmt	= null;
		try {
			conn 		 	=	Broker.getConnection();
			stmt 			=	conn.createStatement();
			ResultSet	rs	=	stmt.executeQuery(cmd);
			//
			while(rs.next()){
				v.add( rs.getString("seqkey") );
				count++;
			}			
		}catch(Exception e){
			if(LOG.fa) {
				LOG.println("#### 失敗:CBclassIndex #read()[1]" );
				e.printStackTrace();
			}
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}
	
	/**
	 * 該当するレコード集合を得る
	 * 
	 * @param prmkey
	 * @return
	 */
	public	int read(String prmkey, Vector v){
		if(LOG.fa)	LOG.println("■ CBclassIndex #read()[2]");
		
		String	cmd 	= 	" SELECT * FROM  " + dbName("CBclassIndex") + " WHERE accessclass = " + m1 + prmkey + m1 + "ORDER BY seqkey ASC";
		if(LOG.fa) LOG.println("CBclassIndex #read() : SQL = " + cmd);
		
		int 		count	= 0;
		Connection  conn	= null;
		Statement 	stmt	= null;
		try {
			conn 		 	=	Broker.getConnection();
			stmt 			=	conn.createStatement();
			ResultSet	rs	=	stmt.executeQuery(cmd);
			//
			while(rs.next()){
				v.add( rs.getString("seqkey") );
				count++;
			}			
		}catch(Exception e){
			if(LOG.fa) {
				LOG.println("#### 失敗:CBclassIndex #read( " + prmkey + " )[2]" );
				e.printStackTrace();
			}
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}
	/**
	 * レコードの有無を調べる
	 *  
	 * @param prmkey
	 * @return
	 */
	public	boolean exists(String prmkey, String seqkey){
		if(LOG.fa)	LOG.println("■ CBclassIndex #exists()");
		
		String	cmd 	= 	" SELECT * FROM  " + dbName("CBclassIndex") + "  WHERE accessclass = " + m1 + prmkey + m1+ 
        																		   "AND seqkey = " + m1 + seqkey + m1;
		if(LOG.fa) LOG.println("CBclassIndex #read() : SQL = " + cmd);
		
		int 		count	= 0;
		Connection  conn	= null;
		Statement 	stmt	= null;
		try {
			conn 		 	=	Broker.getConnection();
			stmt 			=	conn.createStatement();
			ResultSet	rs	=	stmt.executeQuery(cmd);
			while(rs.next()){
				count++;
			}
		}catch(Exception e){
			// 失敗ではない
			if(LOG.fa) {
				LOG.println("#### ない:CBclassIndex #read( " + prmkey + " )" );
				e.printStackTrace();
			}
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		if(count>0){
			return	true;
		}else{
			return false;
		}	
	}	
	/**
	 * レコードを挿入する
	 * 
	 * @param prmkey
	 * @param datakey
	 * @return
	 */
	public	int insert( String prmkey, String datakey ){
		if(LOG.fa)	LOG.println("■ CBclassIndex #insert()");
		
		Connection  conn  	= null;
		Statement 	stmt  	= null;
		int 		count	= 0;
		String query = "INSERT INTO " + dbName("CBclassIndex") +  " VALUES (" + m1 + prmkey + m2 + m1 + datakey + m1 + ")";
		if(LOG.fa) LOG.println("CBclassIndex #insert() : query = " +  query);
		
		try {
			conn 	= Broker.getConnection();
			stmt 	= conn.createStatement();
			count	= stmt.executeUpdate(query);
			
		}catch(Exception e){
			System.out.println("#### 失敗:CBclassIndex #insert( " + prmkey + " ," + datakey + ")");
			e.printStackTrace();
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count; // 件数（普通は１）
	}
	/**
	 * 該当するレコードを削除する
	 * 
	 * @param prmkey
	 * @return
	 */
	public	int delete( String prmkey, String data){
		if(LOG.fa)	LOG.println("■ CBclassIndex #delete(String prmkey, String data)");

		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		int 		n		=	0;
		String		query	=	"DELETE from " + dbName("CBclassIndex") + " WHERE accessclass = "  + m1 + prmkey + m1 
		                                                                + " AND   seqkey = "       + m1 + data + m1;
		if(LOG.fa) LOG.println("CBclassIndex #delete() : query = " +  query);
		
		try {
			conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
			stmt 	= conn.createStatement();
			n  		= stmt.executeUpdate(query); // ここでdeleteする
			//
		}catch(Exception e){
			System.out.println("### 失敗:CBclassIndex #delete(" + prmkey + " ," + data + ")");
			e.printStackTrace();
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			//
			Broker.freeConnection(conn);/* connection を Broker に返す */
		}
		return n; // 件数（普通は１）
	}
	/**
	 * 該当するレコードを削除する
	 * 
	 * @param prmkey
	 * @return
	 */
	public	int delete( String data){
		if(LOG.fa)	LOG.println("■ CBclassIndex #delete(String data)");

		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		int 		n		=	0;
		String		query	=	"DELETE from " + dbName("CBclassIndex") + " WHERE seqkey = "       + m1 + data + m1;
		if(LOG.fa) LOG.println("CBclassIndex #delete() : query = " +  query);
		
		try {
			conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
			stmt 	= conn.createStatement();
			n  		= stmt.executeUpdate(query); // ここでdeleteする
			//
		}catch(Exception e){
			System.out.println("### 失敗:CBclassIndex #delete(" + data + ")");
			e.printStackTrace();
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			//
			Broker.freeConnection(conn);/* connection を Broker に返す */
		}
		return n; // 件数（普通は１）
	}		
	
}





















