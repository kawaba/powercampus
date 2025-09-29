/*
 * 作成日: 2005/02/04
 *
 * TODO
 */
package cabinet;

import java.sql.*;
import database.DbConnectionBroker;
import framework.*;
import tktools.*;

/**
 *  各グループごとの補助キー発生器

 *	create table cbkey (
 *		groupkey        VARCHAR(10)   PRIMARY KEY,
 *		seqkey          CHAR(3)
 *	);
 *
 */
public class CBkey implements CBvar{
	
	DbConnectionBroker	Broker;
	
	public CBkey(DbConnectionBroker Broker){
		this.Broker 	= Broker;		
	}
	/**
	 * 次のキーを得る
	 * 
	 * @param prmkey	グループ（ex. kwc ）
	 * @return
	 */
	public	String	getNextNumber(String prmkey){
		if(LOG.fa) LOG.println("■CBkey #getNextNumber()");
		String	str	=	read(prmkey);
		
		if(Gear.isEmpty(str)){
			str	=	"001";
			insert(prmkey,"001");
		}
		int		num;
		try{
			num	=	Integer.parseInt(str);
		}catch(NumberFormatException e){
			e.printStackTrace();
			num	=	0;
		}
		
		if(num == 999){
			// 初期値に戻す
			update(prmkey,"001");
		}else{
			// 1 増やす
			String	newStr	=	StringGear.get000type(num + 1);
			update(prmkey,newStr);
		}
		return	DateGear.getLongDateString() + "-" + str;
	}
	
	/**
	 * 現在のキーを得る
	 * 
	 * @param prmkey
	 * @return
	 */
	public	String read(String prmkey){
		
		String	cmd 	= 	" SELECT * FROM  cbkey  WHERE groupkey = " + m1 + prmkey + m1;
		if(LOG.fa) LOG.println("CBkey #read() : SQL = " + cmd);
		
		String		retkey	=	"";
		Connection  conn	= null;
		Statement 	stmt	= null;
		try {
			conn 		 	=	Broker.getConnection();
			stmt 			=	conn.createStatement();
			ResultSet	rs	=	stmt.executeQuery(cmd);
			rs.next();
			retkey			=	rs.getString("seqkey");

		}catch(Exception e){
			if(LOG.fa) {
				LOG.println("#### 失敗:CBkey #read( " + prmkey + " )" );
				e.printStackTrace();
			}
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return retkey;
	}
	/**
	 * 現在のキーを更新する
	 * 
	 * @param prmkey
	 * @param datakey
	 * @return
	 */
	int update( String prmkey, String datakey ){
		
		int		count	=	0;
		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		String		query	=	"UPDATE  cbkey set seqkey = " + m1 + datakey + m1 + " WHERE  groupkey = " + m1 + prmkey + m1;
		if(LOG.fa) LOG.println("CBkey #update() : query =" +  query);
		
		try {
			conn 	=	Broker.getConnection();
			stmt 	=	conn.createStatement();
			count	=	stmt.executeUpdate(query);
		
		}catch(Exception e){
			System.out.println("#### 失敗:CBkey #update( " + prmkey + " )" );
			e.printStackTrace();
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}
	/**
	 * 新しいキーレコードを挿入する
	 * 
	 * @param prmkey
	 * @param datakey
	 * @return
	 */
	int insert( String prmkey, String datakey ){
		
		Connection  conn  	= null;
		Statement 	stmt  	= null;
		int 		count	= 0;
		String query = "INSERT INTO  cbkey VALUES (" + m1 + prmkey + m2 + m1 + datakey + m1 + ")";
		if(LOG.fa) LOG.println("CBkey #insert() : query = " +  query);
		
		try {
			conn 	= Broker.getConnection();
			stmt 	= conn.createStatement();
			count	= stmt.executeUpdate(query);
			
		}catch(Exception e){
			System.out.println("#### 失敗:CBkey #insert( " + prmkey + " ," + datakey + ")");
			e.printStackTrace();
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count; // 件数（普通は１）
	}
	/**
	 * キーレコードを削除する
	 * 
	 * @param prmkey
	 * @return
	 */
	int delete( String prmkey){

		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		int 		n		=	0;
		String		query	=	"DELETE from  cbkey  WHERE groupkey = "  + m1 + prmkey + m1;
		if(LOG.fa) LOG.println("CBkey #delete() : query = " +  query);
		
		try {
			conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
			stmt 	= conn.createStatement();
			n  		= stmt.executeUpdate(query); // ここでdeleteする
			//
		}catch(Exception e){
			System.out.println("### 失敗:CBkey #delete(" + prmkey + ")");
			e.printStackTrace();
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			//
			Broker.freeConnection(conn);/* connection を Broker に返す */
		}
		return n; // 件数（普通は１）
	}	
	
}





















