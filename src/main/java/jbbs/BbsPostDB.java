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
///////////////////////////////////////////////////////////////////////////////////////

　　教師・学生のPOSTテーブル（ＢＢＳ）

    ポストしたデータを記録する
    dispName はハンドル名またはユーザー名．メモである．
    link はスレッド順を表す
    例：00003-00021-00033　　00003にリンクしている00021の記事にリンクするレコード．
    　　　　　　　　　　　　　最後の 00033 は自分の postkey 

///////////////////////////////////////////////////////////////////////////////////////

create table bbspost_GROUPNAME (
     bbskey          VARCHAR(22),
     postkey         CHAR(5),
     userid          VARCHAR(12),
     date            VARCHAR(30),
     rating          VARCHAR(6),
     subject         VARCHAR(100),
     content         TEXT,
     attachment      TEXT,
     dispname        VARCHAR(30),
     link            TEXT
);
create index bbspost_GROUPNAME_idx on bbspost_GROUPNAME (bbskey, postkey);

 */
public class BbsPostDB {

	/** データベースのフィールド名 */
	public static final String BBS_KEY		= "_bbskey";
	public static final String POST_KEY		= "_postkey";
	public static final String USERID			= "_post_ownerid";
	public static final String DATE			= "_date";
	public static final String RATING			= "_rating";
	public static final String SUBJECT		= "_subject";
	public static final String CONTENT		= "_content";
	public static final String ATTACHMENT		= "_attachment";
	public static final String DISP_NAME		= "_dispname";     
	public static final String LINK			= "_link";     

	public static final String LINKTOP		= "_linkTop";     

	
	private final String m1		= "\'";
	private final String m2		= "\',";
	private final String m3		= ",";

	public	static final String	HANDLE_MODE		=	"1";
	public	static final String	NOT_HANDLE_MODE	=	"0";

	//
	DbConnectionBroker 	Broker;
	String				szDB;
	//
	public	BbsPostDB(String szDB,DbConnectionBroker Broker){
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
	 * TreeSet に記事を格納して返す
	 * Post一覧に表示する順序で記事を格納したTreeSetを返す
	 * 
	 * TreeSetの要素はComparableインターフェースを持つBbsArticle
	 * である．BbsArticle は並び順を制御する．
	 * 
	 * @param bbskey
	 * @return
	 */
	public TreeSet getSortedBbsPosts(String bbskey){
		if(LOG.fa) LOG.println("■ BbsPostDB #getSortedBbsPosts()");
		
		TreeSet	ts	=	new	TreeSet();
		
		String	cmd 	= 	" SELECT * FROM  " + dbName("bbspost") + "  WHERE bbskey = " + m1 + bbskey + m1;
		if(LOG.fa) LOG.println("BbsPostDB #readBbsPosts() : SQL = " + cmd);
		
		Connection  conn	=	null;
		Statement 	stmt	=	null;
		try {
			conn 		 	=	Broker.getConnection();
			stmt 			=	conn.createStatement();
			ResultSet	rs	=	stmt.executeQuery(cmd);
			//
			while(rs.next()){
				Hashtable	dt	=	new Hashtable(20);
				dt.put(BBS_KEY		,setValidData(rs.getString("bbskey")	, "????????????_????_????"));
				dt.put(POST_KEY		,setValidData(rs.getString("postkey")	, "?????"));
				dt.put(USERID		,setValidData(rs.getString("userid")	, ""));
				dt.put(DATE			,setValidData(rs.getString("date")		, ""));
				dt.put(RATING		,setValidData(rs.getString("rating")	, "0"));
				dt.put(SUBJECT		,setValidData(rs.getString("subject")	, ""));
				dt.put(CONTENT		,setValidData(rs.getString("content")	, ""));
				dt.put(ATTACHMENT	,setValidData(rs.getString("attachment"), ""));
				dt.put(DISP_NAME	,setValidData(rs.getString("dispname")	, ""));
				dt.put(LINK			,setValidData(rs.getString("link")		, ""));
				/*
				 * リンクの先頭キーだけを取り出しておく
				 */
				dt.put(LINKTOP, linkTop( (String)(dt.get(LINK))) );

				BbsArticle	ba	=	new	BbsArticle(dt);
				ts.add(ba);
			}
		}catch(Exception e){
			if(LOG.fa) { LOG.println("#### 失敗: BbsPostDB #getSortedBbsPosts ( bbskey=" + bbskey + " )" + e); }
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return ts;
	}
	
	
	/**
	 * bbskey によって、リンクキーの順にレコードを読み出す
	 * 日付は古いものが先に来る
	 * １レコードをひとつのハッシュテーブルに格納
	 * これを引数の Vector に 読み出し順に格納する
	 * 
	 * @param bbskey	読み出しキー	
	 * @param v		検索レコードをを格納するベクター．レコードはハッシュオブジェクト．
	 * @return			読み出し件数
	 */
	public int readBbsPosts(String bbskey, Vector v){
		if(LOG.fa) LOG.println("■ BbsPostDB #readBbsPosts()");
				
		return	readBbsPosts(bbskey, v, " ORDER BY link ASC , date  ASC");
	}
	/**
	 * bbskey によって、リンクキーの順にレコードを読み出す
	 * 日付は新しいものが先にくる
	 * １レコードをひとつのハッシュテーブルに格納
	 * これを引数の Vector に 読み出し順に格納する
	 * 
	 * @param bbskey	読み出しキー	
	 * @param v		検索レコードをを格納するベクター．レコードはハッシュオブジェクト．
	 * @return			読み出し件数
	 */	
	public int readBbsPostsRev(String bbskey, Vector v){
		if(LOG.fa) LOG.println("■ BbsPostDB #readBbsPostsRev()");
		return	readBbsPosts(bbskey, v, " ORDER BY link ASC , date  DESC");
	}
	/**
	 * bbskey によって、日付順にレコードを読み出す
	 * 日付は新しいものが先にくる
	 * １レコードをひとつのハッシュテーブルに格納
	 * これを引数の Vector に 読み出し順に格納する
	 * 
	 * @param bbskey	読み出しキー	
	 * @param v		検索レコードをを格納するベクター．レコードはハッシュオブジェクト．
	 * @return			読み出し件数
	 */	
	public int readBbsPostsDateOredrDEC(String bbskey, Vector v){
		if(LOG.fa) LOG.println("■ BbsPostDB #readBbsPostsRev()");
		return	readBbsPosts(bbskey, v, " ORDER BY  date  DESC");
	}
	/**
	 * bbskey によって、日付順にレコードを読み出す
	 * 日付は古いものが先にくる
	 * １レコードをひとつのハッシュテーブルに格納
	 * これを引数の Vector に 読み出し順に格納する
	 * 
	 * @param bbskey	読み出しキー	
	 * @param v		検索レコードをを格納するベクター．レコードはハッシュオブジェクト．
	 * @return			読み出し件数
	 */	
	public int readBbsPostsDateOredrASC(String bbskey, Vector v){
		if(LOG.fa) LOG.println("■ BbsPostDB #readBbsPostsRev()");
		return	readBbsPosts(bbskey, v, " ORDER BY  date  ASC");
	}

	/**
	 * bbskey によって、レコードを読み出す．読み出し順を order で指定する．
	 * １レコードをひとつのハッシュテーブルに格納
	 * これを引数の Vector に 読み出し順に格納する
	 * 
	 * @param bbskey	読み出しキー	
	 * @param v		検索レコードをを格納するベクター．レコードはハッシュオブジェクト．
	 * @return			読み出し件数
	 */
	public int readBbsPosts(String bbskey, Vector v, String order){
		if(LOG.fa) LOG.println("■ BbsPostDB #readBbsPosts()");
		
		String	cmd 	= 	" SELECT * FROM  " + dbName("bbspost") + "  WHERE bbskey = " + m1 + bbskey + m1 + order ;
		if(LOG.fa) LOG.println("BbsPostDB #readBbsPosts() : SQL = " + cmd);
		
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
				dt.put(BBS_KEY		,setValidData(rs.getString("bbskey")	, "????????????_????_????"));
				dt.put(POST_KEY		,setValidData(rs.getString("postkey")	, "?????"));
				dt.put(USERID		,setValidData(rs.getString("userid")	, ""));
				dt.put(DATE			,setValidData(rs.getString("date")		, ""));
				dt.put(RATING		,setValidData(rs.getString("rating")	, "0"));
				dt.put(SUBJECT		,setValidData(rs.getString("subject")	, ""));
				dt.put(CONTENT		,setValidData(rs.getString("content")	, ""));
				dt.put(ATTACHMENT	,setValidData(rs.getString("attachment"), ""));
				dt.put(DISP_NAME	,setValidData(rs.getString("dispname")	, ""));
				dt.put(LINK			,setValidData(rs.getString("link")		, ""));
				/*
				 * リンクの先頭キーだけを取り出しておく
				 */
				dt.put(LINKTOP, linkTop( (String)(dt.get(LINK))) );
				
				toNomal(dt);
				v.add(dt);
				count++;
			}
		}catch(Exception e){
			if(LOG.fa) { LOG.println("#### 失敗: BbsPostDB #readBbsPosts ( bbskey=" + bbskey + " )" + e); }
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}
	/**
	 * リンクキーの先頭キーを返す
	 * リンクキーは必ずある
	 * 
	 * @param link
	 * @return
	 */
	String	linkTop(String link){
	    Csv	cs	=	new	Csv(link,"_ ");
	    return	cs.get(0);
	    
	}
	/**
	 * あるスレッドの支持度を全てベクターに入れて返す
	 * 
	 * @param bbskey
	 * @param dt
	 * @return
	 */
	public int	getRatingPoints(String bbskey, Vector dt){
		if(LOG.fa) LOG.println("■ BbsPostDB #getRatingPoints()");
		
		String	cmd 	= 	" SELECT * FROM  " + dbName("bbspost") + "  WHERE bbskey = " + m1 + bbskey + m1;
		if(LOG.fa) LOG.println("BbsPostDB #getRatingPoints() : SQL = " + cmd);
		
		int			count	=	0;
		Connection  conn	=	null;
		Statement 	stmt	=	null;
		try {
			conn 		 	=	Broker.getConnection();
			stmt 			=	conn.createStatement();
			ResultSet	rs	=	stmt.executeQuery(cmd);
			//
			while(rs.next()){
				dt.add( setValidData(rs.getString("rating")	, "0"));
				count++;
			}
		}catch(Exception e){
			if(LOG.fa) { LOG.println("#### 失敗: BbsPostDB #getRatingPoints ( bbskey=" + bbskey + " )" + e); }
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;		
	}

	/**
	 * 特定のレコードを１件読み出す
	 * @param bbskey
	 * @param postkey
	 * @return
	 */	
	public int readBbsPost(String bbskey, String postkey, Hashtable dt){
		if(LOG.fa) LOG.println("■ BbsPostDB #readBbsPost()");
		String	cmd 	= 	" SELECT * FROM  " + dbName("bbspost") + "  WHERE bbskey = " + m1 + bbskey + m1 + " AND postkey = " + m1 + postkey + m1; 
		if(LOG.fa) LOG.println("BbsPostDB #readBbsPost() : SQL = " + cmd);
		
		int		count	=	0;
		Connection  conn	=	null;
		Statement 	stmt	=	null;
		try {
			conn 		 	=	Broker.getConnection();
			stmt 			=	conn.createStatement();
			ResultSet	rs	=	stmt.executeQuery(cmd);
			//
			while(rs.next()){
				dt.put(BBS_KEY		,setValidData(rs.getString("bbskey")	, "????????????_????_????"));
				dt.put(POST_KEY		,setValidData(rs.getString("postkey")	, "?????"));
				dt.put(USERID		,setValidData(rs.getString("userid")	, ""));
				dt.put(DATE			,setValidData(rs.getString("date")		, ""));
				dt.put(RATING		,setValidData(rs.getString("rating")	, "0"));
				dt.put(SUBJECT		,setValidData(rs.getString("subject")	, ""));
				dt.put(CONTENT		,setValidData(rs.getString("content")	, ""));
				dt.put(ATTACHMENT	,setValidData(rs.getString("attachment"), ""));
				dt.put(DISP_NAME	,setValidData(rs.getString("dispname")	, ""));
				dt.put(LINK			,setValidData(rs.getString("link")		, ""));
				//
				toNomal(dt);
				count++;
			}
		}catch(Exception e){
			if(LOG.fa) { LOG.println("#### 失敗: BbsPostDB #readBbsPost()") ;}
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}	

	/**
	 * ポストデータレコードを更新する
	 * 
	 * @param bbskey		オーナーキー
	 * @param postkey		ポストデータキー
	 * @param dt			更新データを入れたハッシュ
	 * @return				更新件数（通常は１）
	 */
	public int updateBbsPost( String bbskey, String postkey, Hashtable dt){
		if(LOG.fa) LOG.println("■ BbsPostDB #updateBbsPost()");

		int		count	=	0;
		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		String		query	=	updateQueryBbsPost(bbskey, postkey, dt);
		//
		try {
			conn 	=	Broker.getConnection();
			stmt 	=	conn.createStatement();
			count	=	stmt.executeUpdate(query);
		
		}catch(Exception e){
			System.out.println("#### 失敗:BbsPostDB #updateBbsPost( " + bbskey + "," + postkey + e);
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}	
	String	updateQueryBbsPost(String bbskey, String postkey, Hashtable dt ){
		if(LOG.fa) LOG.println("■ BbsPostDB #updateQueryBbsPost()");

	    /*
	     * \ " ' を特殊文字に直して書き込む
	     */
	    toDefined(dt);

		String	query	=	"UPDATE "+ dbName("bbspost") + " set " 

							+ " userid     = "  + m1 + Gear.strHash(dt,USERID		, "")  + m2
							+ " date       = "  + m1 + Gear.strHash(dt,DATE	  		, "")  + m2
							+ " rating     = "  + m1 + Gear.strHash(dt,RATING		, "")  + m2
							+ " subject    = "  + m1 + Gear.strHash(dt,SUBJECT		, "")  + m2
							+ " content    = "  + m1 + Gear.strHash(dt,CONTENT		, "")  + m2
							+ " attachment = "  + m1 + Gear.strHash(dt,ATTACHMENT	, "")  + m2
							+ " dispname   = "  + m1 + Gear.strHash(dt,DISP_NAME  	, "")  + m2
							+ " link       = "  + m1 + Gear.strHash(dt,LINK	  		, "")  + m1
							//
							+ " WHERE bbskey  = " + m1 + bbskey    + m1
							+ " AND   postkey = " + m1 + postkey   + m1;							

		if(LOG.fa) LOG.println("class BbsPostDB #updateQueryBbsPost() : query =" +  query);
		return query;
	}	

	/**
	 * 新規レコードを挿入する
	 * @param bbskey		オーナーキー
	 * @param postkey		ポストデータキー
	 * @param dt			挿入データを入れたハッシュ
	 * @return				挿入件数（通常は１）
	 */
	public int insertBbsPost( String bbskey, String postkey, Hashtable dt ){
		if(LOG.fa) LOG.println("■ BbsPostDB #insertBbsPost()");
		
		Connection  conn  	= null;
		Statement 	stmt  	= null;
		int 		count	= 0;
		String query = insertQueryBbsPost(bbskey, postkey, dt);
		//
		try {
			conn 	= Broker.getConnection();
			stmt 	= conn.createStatement();
			count	= stmt.executeUpdate(query);
			
		}catch(Exception e){
			System.out.println("### 失敗:BbsPostDB #insertBbsPost(" + bbskey +"," + postkey + ")" + e);
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}
	String	insertQueryBbsPost(String bbskey, String postkey, Hashtable dt ){
		if(LOG.fa) LOG.println("■ BbsPostDB #insertQueryBbsPost()");

	    /*
	     * \ " ' を特殊文字に直して書き込む
	     */
	    toDefined(dt);

		String query = " INSERT INTO " + dbName("bbspost") + "  VALUES (" 
					+ m1 + Gear.strHash(dt,BBS_KEY		,"????????????_????_????")	+ m2 
					+ m1 + Gear.strHash(dt,POST_KEY		,"?????")					+ m2 

					+ m1 + Gear.strHash(dt,USERID   	, "")  	  + m2
					+ m1 + Gear.strHash(dt,DATE	  		, "")     + m2
					+ m1 + Gear.strHash(dt,RATING		, "")     + m2
					+ m1 + Gear.strHash(dt,SUBJECT		, "")     + m2
					+ m1 + Gear.strHash(dt,CONTENT		, "")     + m2
					+ m1 + Gear.strHash(dt,ATTACHMENT	, "")     + m2
					+ m1 + Gear.strHash(dt,DISP_NAME	, "")     + m2
					+ m1 + Gear.strHash(dt,LINK			, "")     + m1
					+ ")";

		if(LOG.fa) LOG.println("BbsPostDB #insertQueryBbsPost() : query =" +  query);
		return query;
	}	
	
	/**
	 * 
	 * @param bbskey		bbsキー
	 * @param postkey		ポストデータキー
	 * @return				削除件数（通常は１）
	 */
	public int deleteBbsPost( String bbskey, String postkey){
		if(LOG.fa) LOG.println("■ BbsPostDB #deleteBbsPost()");

		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		int 		n		=	0;
		String		query	=	"DELETE from " 	+ dbName("bbspost") 
												+ " WHERE bbskey  = " + m1 + bbskey + m1
												+ " AND   postkey = " + m1 + postkey    	+ m1;							
		if(LOG.fa) LOG.println("BbsPostDB #deleteBbsPost() : query = " +  query);
		
		try {
			conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
			stmt 	= conn.createStatement();
			n  		= stmt.executeUpdate(query); // ここでdeleteする
			//
		}catch(Exception e){
			System.out.println("### 失敗:BbsPostDB #deleteBbsPost(" + bbskey +"," + postkey + ")" + e);
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			//
			Broker.freeConnection(conn);/* connection を Broker に返す */
		}
		return n; // 件数（普通は１）
	}	
	/**
	 * 特定のスレッドの全ての記事を削除する
	 * 
	 * @param bbskey		bbsキー
	 * @return				削除件数（通常は１）
	 */
	public int deleteAllBbsPost( String bbskey){
		if(LOG.fa) LOG.println("■ BbsPostDB #deleteAllBbsPost()");

		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		int 		n		=	0;
		String		query	=	"DELETE from " 	+ dbName("bbspost") 
												+ " WHERE bbskey  = " + m1 + bbskey + m1;
		if(LOG.fa) LOG.println("BbsPostDB #deleteBbsPost() : query = " +  query);
		
		try {
			conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
			stmt 	= conn.createStatement();
			n  		= stmt.executeUpdate(query); // ここでdeleteする
			//
		}catch(Exception e){
			System.out.println("### 失敗:BbsPostDB #deleteAllBbsPost(" + bbskey + ")" + e);
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			//
			Broker.freeConnection(conn);/* connection を Broker に返す */
		}
		return n; // 件数（普通は１）
	}	
	
	
}
