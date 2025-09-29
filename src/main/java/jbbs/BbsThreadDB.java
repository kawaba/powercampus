/*
 *
 * 
 * 
 */
package jbbs;
import	 tktools.*;

import  java.sql.*;
import	 java.util.*;

import database.DbConnectionBroker;

import framework.LOG;

/**
 スレッドデータベースの内容
  
  userid       このスレッドの設置者ID
  date         開設日時
  rating       支持度（評価）
  subject      スレッド名
  content      内容
  attachment   添付ファイルリスト（CSV形式）
  views        閲覧数

create table bbsthread_GROUPNAME (
     owner_forumkey   VARCHAR(17),
     threadkey        CHAR(4),
     userid           CHAR(12),
     date             VARCHAR(30),
     rating           VARCHAR(6),
     subject          VARCHAR(100),
     content          TEXT,
     attachment       TEXT,
     views            VARCHAR(7)     
);
create index bbsthread_GROUPNAME_idx on bbsthread_GROUPNAME (owner_forumkey, threadkey);

**/
public class BbsThreadDB {

	/** データベースのフィールド名 */
	public static final String OWNER_FORUM_KEY	= "_owner_forumkey";
	public static final String THREAD_KEY		= "_threadkey";
	public static final String USERID			= "_userid";
	public static final String DATE				= "_date";
	public static final String RATING			= "_rating";
	public static final String SUBJECT			= "_subject";
	public static final String CONTENT			= "_content";
	public static final String ATTACHMENT		= "_attachment";
	public static final String VIEWS				= "_views";     
	
	/** 定数 */
	private final String m1		= "\'";
	private final String m2		= "\',";
	private final String m3		= ",";
	
	public	static final String	HANDLE_MODE		=	"1";
	public	static final String	NOT_HANDLE_MODE	=	"0";
	
	/**  view の増減指示値 */
	public	static final String	INCREASE		=	"1";
	public	static final String	DECREASE		=	"-1";

	//
	DbConnectionBroker 	Broker;
	String				szDB;
	//
	public	BbsThreadDB(String szDB,DbConnectionBroker Broker){
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
	 * owner_forumkey で複数のレコードを読む
	 * １レコードをひとつのハッシュテーブルに格納
	 * これを引数の Vector に 読み出し順に格納する
	 * 
	 * 作成日付で降順にソートした状態の結果を返す
	 * 
	 * @param owner_forumkey	オーナー+フォーラムキー
	 * @param v				検索レコードをを格納するベクター．レコードはハッシュオブジェクト．
	 * @return					読み出し件数
	 */
	public int readBbsThreads(String owner_forumkey, Vector v){
		String	order	=	" ORDER BY date DESC ";
		return	readBbsThreads(owner_forumkey, v, order);
	}

	/**
	 * owner_forumkey で複数のレコードを読む
	 * １レコードをひとつのハッシュテーブルに格納
	 * これを引数の Vector に 読み出し順に格納する
	 * 
	 * userid で昇順にソートした状態の結果を返す．
	 * グループ学習用
	 * 
	 * @param owner_forumkey	オーナー+フォーラムキー
	 * @param v				検索レコードをを格納するベクター．レコードはハッシュオブジェクト．
	 * @return					読み出し件数
	 */
	public int readBbsGroupThreads(String owner_forumkey, Vector v){
		String	order	=	" ORDER BY userid ASC ";
		return	readBbsThreads(owner_forumkey, v, order);
	}
	/**
	 * owner_forumkey で複数のレコードを読む
	 * １レコードをひとつのハッシュテーブルに格納
	 * これを引数の Vector に 読み出し順に格納する
	 * 
	 * rating で降順にソートした状態の結果を返す．
	 * グループ学習用
	 * 
	 * @param owner_forumkey	オーナーキー
	 * @param v				検索レコードを格納するベクター．レコードはハッシュオブジェクト．
	 * @return					読み出し件数
	 */
	public int readBbsThreadsByRating(String owner_forumkey, Vector v){
		String	order	=	" ORDER BY rating DESC ";
		return	readBbsThreads(owner_forumkey, v, order);
	}

	/**
	 * owner_forumkey で複数のレコードを読む
	 * １レコードをひとつのハッシュテーブルに格納
	 * これを引数の Vector に 読み出し順に格納する
	 * orderでソートした状態の結果を返す．
	 * 
	 * @param owner_forumkey	オーナーキー
	 * @param v				検索レコードをを格納するベクター．レコードはハッシュオブジェクト．
	 * @param order			並べ替え指定
	 * @return					読み出し件数
	 */
	public int readBbsThreads(String owner_forumkey, Vector v, String order){
		
		String	cmd 	= 	" SELECT * FROM  " + dbName("bbsthread") + "  WHERE owner_forumkey = " + m1 + owner_forumkey + m1 + order ;
		if(LOG.fa) LOG.println("BbsThreadDB #readBbsThreads() : SQL = " + cmd);
		
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
				dt.put(OWNER_FORUM_KEY	,setValidData(rs.getString("owner_forumkey"), "????????????_????"));
				dt.put(THREAD_KEY		,setValidData(rs.getString("threadkey")		, "????"));

				dt.put(USERID			,setValidData(rs.getString("userid")		, ""));
				dt.put(DATE				,setValidData(rs.getString("date")			, ""));
				dt.put(RATING			,setValidData(rs.getString("rating")		, "-"));
				dt.put(SUBJECT			,setValidData(rs.getString("subject")		, ""));
				dt.put(CONTENT			,setValidData(rs.getString("content")		, ""));
				dt.put(ATTACHMENT		,setValidData(rs.getString("attachment")	, ""));
				dt.put(VIEWS			,setValidData(rs.getString("views")			, ""));
				//
				toNomal(dt);
				v.add(dt);
				count++;
			}
		}catch(Exception e){
			if(LOG.fa) { LOG.println("#### 失敗: BbsThreadDB #readBbsThreads ( owner_forumkey=" + owner_forumkey + " )" + e); }
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}	
	/**
	 * 特定のフォーラムに属する全てのスレッドのスレッドキーを求める
	 * @param owner_forumkey
	 * @param v
	 * @return
	 */
	public int getKeys(String owner_forumkey, Vector v){
		
		String	cmd 	= 	" SELECT * FROM  " + dbName("bbsthread") + "  WHERE owner_forumkey = " + m1 + owner_forumkey + m1;
		if(LOG.fa) LOG.println("BbsThreadDB #getKeys() : SQL = " + cmd);
		
		int		count	=	0;
		Connection  conn	=	null;
		Statement 	stmt	=	null;
		try {
			conn 		 	=	Broker.getConnection();
			stmt 			=	conn.createStatement();
			ResultSet	rs	=	stmt.executeQuery(cmd);
			//
			while(rs.next()){
				setValidData(rs.getString("threadkey")		, "????");
				v.add( setValidData(rs.getString("threadkey")		, "????") );
				count++;
			}
		}catch(Exception e){
			if(LOG.fa) { LOG.println("#### 失敗: BbsThreadDB #getKeys ( owner_forumkey=" + owner_forumkey + " )" + e); }
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}
	
	
	public int readBbsThreadRecord(String owner_forumkey, String threadkey, Hashtable dt){
		
		String	cmd 	= 	" SELECT * FROM  " + dbName("bbsthread") + "  WHERE owner_forumkey = " + m1 + owner_forumkey + m1
		                                                                     + " AND threadkey = " + m1 + threadkey + m1;
		
		if(LOG.fa) LOG.println("BbsThreadDB #readBbsThreads() : SQL = " + cmd);
		
		int		count	=	0;
		Connection  conn	=	null;
		Statement 	stmt	=	null;
		try {
			conn 		 	=	Broker.getConnection();
			stmt 			=	conn.createStatement();
			ResultSet	rs	=	stmt.executeQuery(cmd);
			//
			while(rs.next()){
				dt.put(OWNER_FORUM_KEY	,setValidData(rs.getString("owner_forumkey"), "????????????_????"));
				dt.put(THREAD_KEY		,setValidData(rs.getString("threadkey")		, "????"));

				dt.put(USERID			,setValidData(rs.getString("userid")		, ""));
				dt.put(DATE				,setValidData(rs.getString("date")			, ""));
				dt.put(RATING			,setValidData(rs.getString("rating")		, "-"));
				dt.put(SUBJECT			,setValidData(rs.getString("subject")		, ""));
				dt.put(CONTENT			,setValidData(rs.getString("content")		, ""));
				dt.put(ATTACHMENT		,setValidData(rs.getString("attachment")	, ""));
				dt.put(VIEWS			,setValidData(rs.getString("views")			, ""));
				//
				toNomal(dt);
				count++;
			}
		}catch(Exception e){
			if(LOG.fa) { LOG.println("#### 失敗: BbsThreadDB #readBbsThreadRecord ()" + e); }
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}	
	
	
	/**
	 * スレッドレコードを更新する
	 * @param owner_forumkey	オーナーキー
	 * @param threadkey		スレッドキー
	 * @param dt			更新データを入れたハッシュ
	 * @return				更新件数（通常は１）
	 */
	public int updateBbsThread( String owner_forumkey, String threadkey, Hashtable dt){
		
		int		count	=	0;
		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		String		query	=	updateQueryBbsThread(owner_forumkey, threadkey, dt);
		//
		try {
			conn 	=	Broker.getConnection();
			stmt 	=	conn.createStatement();
			count	=	stmt.executeUpdate(query);
		
		}catch(Exception e){
			System.out.println("#### 失敗:BbsThreadDB #updateBbsThread( " + owner_forumkey + ", Hashtable dt) " + e);
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}	
	String	updateQueryBbsThread(String owner_forumkey, String threadkey, Hashtable dt ){
	    /*
	     * \ " ' を特殊文字に直して書き込む
	     */
	    toDefined(dt);

	    String	query	=	"UPDATE "+ dbName("bbsthread") + " set " 

							+ " userid 		 = "  + m1 + Gear.strHash(dt,USERID			, "")  	+ m2
							+ " date         = "  + m1 + Gear.strHash(dt,DATE	  		, "")  	+ m2
							+ " rating       = "  + m1 + Gear.strHash(dt,RATING	   	 	, "")  	+ m2
							+ " subject      = "  + m1 + Gear.strHash(dt,SUBJECT	    , "")  	+ m2
							+ " content      = "  + m1 + Gear.strHash(dt,CONTENT	    , "")  	+ m2
							+ " attachment   = "  + m1 + Gear.strHash(dt,ATTACHMENT  	, "")  	+ m2
							+ " views        = "  + m1 + Gear.strHash(dt,VIEWS	  		, "")  	+ m1
							//
							+ " WHERE owner_forumkey  = " + m1 + owner_forumkey    + m1
							+ " AND   threadkey       = " + m1 + threadkey         + m1;							

		if(LOG.fa) LOG.println("class BbsThreadDB #updateQueryBbsThread() : query =" +  query);
		return query;
	}	
	/**
	 * view の値を更新する
	 * 
	 * @param owner_forumkey
	 * @param threadkey
	 * @param viewstr
	 * @return
	 */
	public int updateViews(String owner_forumkey, String threadkey,String viewstr){
		if(LOG.fa) LOG.println("■ BbsThreadDB #updateViews()");
		
		int		count	=	0;
		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		String		query	=	updateQuery_updateViews(owner_forumkey, threadkey, viewstr);
		//
		try {
			conn 	=	Broker.getConnection();
			stmt 	=	conn.createStatement();
			count	=	stmt.executeUpdate(query);
		
		}catch(Exception e){
			System.out.println("★ #### 失敗:BbsThreadDB #updateViews() ");
			e.printStackTrace();
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}	
	String	updateQuery_updateViews(String owner_forumkey, String threadkey, String viewstr ){

	    String	query	=	"UPDATE "+ dbName("bbsthread") + " set " 

							+ " views        = "  + m1 + viewstr	+ m1
							//
							+ " WHERE owner_forumkey  = " + m1 + owner_forumkey    + m1
							+ " AND   threadkey       = " + m1 + threadkey         + m1;							

		if(LOG.fa) LOG.println("class BbsThreadDB #updateQuery_updateViews() : query =" +  query);
		return query;
	}	
	
	/**
	 * view の値を更新する
	 * 
	 * @param owner_forumkey
	 * @param threadkey
	 * @param viewstr
	 * @return
	 */
	public int updateRating(String owner_forumkey, String threadkey,String rating){
		if(LOG.fa) LOG.println("■ BbsThreadDB #updateRating()");
		
		int		count	=	0;
		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		String		query	=	updateQuery_updateRating(owner_forumkey, threadkey, rating);
		//
		try {
			conn 	=	Broker.getConnection();
			stmt 	=	conn.createStatement();
			count	=	stmt.executeUpdate(query);
		
		}catch(Exception e){
			System.out.println("★ #### 失敗:BbsThreadDB #updateRating() ");
			e.printStackTrace();
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}
	/**
	 * rating の値を更新する
	 * @param owner_forumkey
	 * @param threadkey
	 * @param rating
	 * @return
	 */	
	String	updateQuery_updateRating(String owner_forumkey, String threadkey, String rating ){
		String	query	=	"UPDATE "+ dbName("bbsthread") + " set " 

							+ " rating       = "  + m1 + rating + m1
							//
							+ " WHERE owner_forumkey  = " + m1 + owner_forumkey    + m1
							+ " AND   threadkey       = " + m1 + threadkey         + m1;							

		if(LOG.fa) LOG.println("class BbsThreadDB #updateQuery_updateRating() : query =" +  query);
		return query;
	}	
	
	/**
	 * 新規レコードを挿入する
	 * @param owner_forumkey		オーナーキー
	 * @param threadkey		フォーラムキー
	 * @param dt			挿入データを入れたハッシュ
	 * @return				挿入件数（通常は１）
	 */
	public int insertBbsThread( Hashtable dt ){
		
		Connection  conn  	= null;
		Statement 	stmt  	= null;
		int 		count	= 0;
		String query = insertQueryBbsThread(dt);
		//
		try {
			conn 	= Broker.getConnection();
			stmt 	= conn.createStatement();
			count	= stmt.executeUpdate(query);
			
		}catch(Exception e){
			System.out.println("### 失敗:BbsThreadDB #insertBbsThread()" + e);
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}
	String	insertQueryBbsThread( Hashtable dt ){
	    /*
	     * \ " ' を特殊文字に直して書き込む
	     */
	    toDefined(dt);

	    String query = " INSERT INTO " + dbName("bbsthread") + "  VALUES (" 
					+ m1 + Gear.strHash(dt,OWNER_FORUM_KEY,"????????????_????")	+ m2 
					+ m1 + Gear.strHash(dt,THREAD_KEY		,"????")			+ m2 

					+ m1 + Gear.strHash(dt,USERID 			, "")  	  + m2
					+ m1 + Gear.strHash(dt,DATE	  			, "")     + m2
					+ m1 + Gear.strHash(dt,RATING			, "")     + m2
					+ m1 + Gear.strHash(dt,SUBJECT			, "")     + m2
					+ m1 + Gear.strHash(dt,CONTENT			, "")     + m2
					+ m1 + Gear.strHash(dt,ATTACHMENT		, "")     + m2
					+ m1 + Gear.strHash(dt,VIEWS	  		, "")     + m1
					+ ")";
		if(LOG.fa) LOG.println("BbsThreadDB #insertQueryBbsThread() : query =" +  query);
		return query;
	}	
	
	/**
	 * 
	 * @param owner_forumkey		オーナーキー
	 * @param threadkey		フォーラムキー
	 * @return				削除件数（通常は１）
	 */
	public int deleteBbsThread( String owner_forumkey, String threadkey){

		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		int 		n		=	0;
		String		query	=	"DELETE from " 	+ dbName("bbsthread") 
												+ " WHERE owner_forumkey  = " + m1 + owner_forumkey + m1
												+ " AND   threadkey       = " + m1 + threadkey    	+ m1;							
		if(LOG.fa) LOG.println("BbsThreadDB #deleteBbsThread() : query = " +  query);
		
		try {
			conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
			stmt 	= conn.createStatement();
			n  		= stmt.executeUpdate(query); // ここでdeleteする
			//
		}catch(Exception e){
			System.out.println("### 失敗:BbsThreadDB #deleteBbsThread(" + owner_forumkey +"," + threadkey + ")" + e);
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			//
			Broker.freeConnection(conn);/* connection を Broker に返す */
		}

		return n; // 件数（普通は１）
	}
		
}
