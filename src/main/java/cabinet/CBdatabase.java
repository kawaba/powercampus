/*
 * 作成日: 2005/02/04
 *
 * TODO
 */
package cabinet;

import java.util.*;
import java.io.File;
import java.sql.*;

import database.DbConnectionBroker;
import framework.*;
import tktools.*;
//
/**
 * ファイルキャビネットデータベース
 *
 *　ファイルキャビネット用のテーブルを操作する
 *　
 *　(C) Takashi KAWABA 2005-
 *
 */

/*
/////////////////////////////////////////////////////

　　ファイルキャビネットデータベース

    seqkey       = yyyymmddhhmmss-nnn
                   nnn はCBkey テーブルから得る
    
     subject       TEXT　　　　　内容説明
     owner         VARCHAR(10)   登録者
     fname         VARCHAR(256)  ファイル名(英字)
     kubun         VARCHAR(5),   ファイル種別（拡張子） 
     size          VARCHAR(20)   ファイルサイズ
     filepath      VARCHAR(256)  アクセス用PATH
     fileurl       VARCHAR(256)  アクセス用URL
     date_from     CHAR(12)      登録日
     idx           CHAR(3)       インデックスフラグ（000,001,010,011）       


/////////////////////////////////////////////////////
create table cabinet_GROUPNAME (
     seqkey        CHAR(18)       PRIMARY KEY,
     subject       TEXT,
     owner         VARCHAR(10),      
     fname         VARCHAR(256),
     kubun         CHAR(5),  
     size          VARCHAR(20),
     filepath      VARCHAR(256),
     fileurl       VARCHAR(256),
     date_from     CHAR(12),
     idx           CHAR(3)
);
create index cabinet_GROUPNAME_idx on cabinet_GROUPNAME (seqkey);

*/
public class CBdatabase implements CBvar{
	
	DbConnectionBroker	Broker;
	String				szDB;		// グループ名
	
	public CBdatabase(DbConnectionBroker Broker, String szDB){
		this.Broker 	= Broker;		
		this.szDB		= szDB;
		if(LOG.fa)	LOG.println("■ CBdatabase #コンストラクタ");
		
	}
	public	static	String	makeKey(String uid, String lec_key){
		return	uid + "-" + lec_key;
	}
	/**
	 * レコードを新規登録し、IDによる検索レコードにも登録する
	 * 
	 * @param dt	レコードを記載したハッシュテーブル
	 * @param id	登録キー
	 * @return
	 */
	public	int	insertByid(Hashtable dt, String	id){
		if(LOG.fa)	LOG.println("■ CBdatabase #insertByid()");
		// レコードを新規登録
		putParameter(dt, IDX, "001");
		int			n		=	insert(dt);
		
		// インデックステーブルにも登録
		CBidIndex	cbid	=	new	CBidIndex(Broker, szDB);
		cbid.insert(id,getParameter(dt,KEY));
		return	n;
		
	}
	
	/**
	 * レコードを新規登録し、classkeyによる検索レコードにも登録する
	 * 
	 * @param dt	レコードを記載したハッシュテーブル
	 * @param id	登録キー
	 * @return
	 */	
	public	int	insertByClass(Hashtable dt, String	classkey){
		if(LOG.fa)	LOG.println("■ CBdatabase #insertByClass()");

		// レコードを新規登録
		putParameter(dt, IDX, "010");
		int				n		=	insert(dt);

		// インデックステーブルにも登録
		CBclassIndex	cbcl	=	new	CBclassIndex(Broker, szDB);
		cbcl.insert(classkey, getParameter(dt,KEY));
		return	n;
		
	}
	/**
	 * インデックスフラグを加算してレコードを更新する
	 * 
	 * @param dt
	 * @param idxAdd
	 */
	public	void	addIdx(Hashtable dt, String idxAdd){
		if(LOG.fa)	LOG.println("■ CBdatabase #addIdx()");

		String	idxVal	=	getParameter(dt, IDX);
		String	pkey	=	getParameter(dt, KEY);

		putParameter(dt, IDX, Add(idxVal, idxAdd));
		update(pkey, dt);
		
	}
	/**
	 * インデックスフラグを減算してレコードを更新する
	 * 
	 * @param dt
	 * @param idxSub
	 */
	public	void	subIdx(Hashtable dt, String idxSub){
		if(LOG.fa)	LOG.println("■ CBdatabase #subIdx()");

		String	idxVal	=	getParameter(dt, IDX);
		String	pkey	=	getParameter(dt, KEY);

		putParameter(dt, IDX, Sub(idxVal, idxSub));
		update(pkey, dt);
	}	
	/**
	 * インデックスフラグを加算する
	 * 　インデックスフラグは 個人用＝001 クラス用＝010 なので加算すると 011 となる
	 * 
	 * @param a1
	 * @param a2
	 * @return
	 */
	public	String	Add(String a1, String a2){
		if(LOG.fa)	LOG.println("■ CBdatabase #Add()");
		
		StringBuffer	buf	=	new StringBuffer();
		int				n	=	Math.min(a1.length(),a2.length());
		for(int i=0; i<n; i++){
			if((a1.charAt(i)=='1')||(a2.charAt(i)=='1')){
				buf.append("1");
			}else{
				buf.append("0");
			}
		}
		return	buf.toString();
	}
	/**
	 * インデックスフラグを減算する
	 * @param a1
	 * @param a2
	 * @return
	 */
	public	String	Sub(String a1, String a2){
		if(LOG.fa)	LOG.println("■ CBdatabase #Sub()");
		
		StringBuffer	buf	=	new StringBuffer();
		int				n	=	Math.min(a1.length(),a2.length());
		for(int i=0; i<n; i++){
			if((a1.charAt(i)=='1')&&(a2.charAt(i)=='1')){
				buf.append("0");
			}else{
				buf.append(a1.charAt(i));
			}
		}
		return	buf.toString();
	}	
	/**
	 * ハッシュからキーで文字列を取り出す
	 * キーの先頭に _ が付いていない場合は付加してから使う
	 *
	 * @param ht
	 * @param key
	 * @return
	 */
	public  String getParameter(Hashtable ht, String key){
		if(LOG.fa)	LOG.println("■ CBdatabase #getParameter()");

		if(!Gear.isEmpty(key)){
			char	ch	=	key.charAt(0);
			if(ch!='_'){
				key	=	"_" + key;
			}
			return	Gear.strHash(ht, key);
		}
		return	"";
	}	
	/**
	 * ハッシュに文字列の値をセットする
	 * キーの先頭に _ が付いていない場合は付加してから 使う
	 * 
	 * @param ht
	 * @param key
	 * @param data
	 * @return
	 */
	public  boolean putParameter(Hashtable ht, String key, String data){
		if(LOG.fa)	LOG.println("■ CBdatabase #putParameter()");

		if(!Gear.isEmpty(key)){
			char	ch	=	key.charAt(0);
			if(ch!='_'){
				key	=	"_" + key;
			}
			if(data==null){
				data = "";
			}
			ht.put(key, data);
			return	true;
		}
		return false;
	}	

	/** テーブル名を返す */
	String	dbName(String dbname){
		if(LOG.fa)	LOG.println("■ CBdatabase #dbName()");

		return		dbname + "_" + szDB;
	}
	/** 初期値をセットする */
	public Hashtable	setInitRecord(Hashtable tb){
		if(LOG.fa)	LOG.println("■ CBdatabase #setInitRecord()");
		
		String		from	=	DateGear.getLongDateString();	// yyyyMMddHHmmss (14桁)
		String		key		=	"";
		
		putParameter(tb, KEY,		key);
		putParameter(tb, SUBJECT,	"");
		putParameter(tb, OWNER, 	"");
		putParameter(tb, FNAME,		"");
		putParameter(tb, KUBUN, 	"");
		putParameter(tb, SIZE, 		"");
		putParameter(tb, PATH, 		"");
		putParameter(tb, URL, 		"");
		putParameter(tb, FROM, 		from.substring(0,12));	// yyyyMMddHHmm(12桁)
		putParameter(tb, IDX, 		"");
		
		return tb;	
	}
	/**
	 * 新しいキーを得る
	 * @param szDB
	 * @param dateString
	 * @return
	 */
	String	getKey(String szDB, String dateString){
		if(LOG.fa)	LOG.println("■ CBdatabase #getKey()");

		CBkey	cb		=	new CBkey(Broker);
		String	key		=	cb.getNextNumber(szDB);
		return	dateString + "-" + key;
	}
	
	/**
	 * 個人用ファイルキャビネットにファイルを登録する
	 * 
	 * @param userid	-- 教師ID
	 * @param fname		-- ファイル名
	 * @param subject	-- 表示用タイトル
	 * @param kubun		-- ファイル種別
	 * @param para		-- システムパラメータオブジェクト
	 */
	public	void	regist(String userid, String fname, String subject, Param para){
		
		String		fpath	=	para.fileCabinetPath(szDB, userid) + fname;
		String		furl	=	para.fileCabinetURL(szDB, userid) + fname;
		
		String		kubun	=	"";
		String		ext		=	FileGear.getExt(fname);
		if(!Gear.isEmpty(ext)){
			kubun	=	"." + FileGear.getExt(fname);
		}
		Hashtable	dataHash=	new	Hashtable(20);
		CBkey		cbkey	=	new	CBkey(Broker);
		String		key		=	cbkey.getNextNumber(szDB);	
		File		fp		=	new File(fpath);
		long		len		=	(fp.length() + 512 )/ 1024;
		if(len==0)	len	=	1;
		GregorianCalendar	to	=	DateGear.calculateMonth(DateGear.currentDay(),1,true);

		putParameter(dataHash,KEY		, key);
		putParameter(dataHash,SUBJECT	, subject);
		putParameter(dataHash,OWNER		, userid);
		putParameter(dataHash,FNAME		, fname);
		putParameter(dataHash,KUBUN		, kubun);// ".cvs"
		putParameter(dataHash,SIZE		, String.valueOf(len)+"KB");	
		putParameter(dataHash,PATH		, fpath);
		putParameter(dataHash,URL		, furl);
		putParameter(dataHash,FROM		, (DateGear.getLongDateString(  )).substring(0,12));	// 今日の日付（yyyyMMddHHmm）
		putParameter(dataHash,IDX		, "001");	// 例：個人用="001"
		/*
		 * 新規登録
		 * 
		 * 　更新登録では、CBdatabase のみ更新する
		 * 　（CBidIndexに変更はないから）
		 * 
		 */
		Hashtable	tb	=	new	Hashtable();
		int			n	=	readRecord(fname,tb);

		if(n==0){
			insertByid(dataHash, userid);
		}else{
			update(getParameter(tb, KEY), dataHash);
		}
	}
	/**
	 *  特定のファイル名のレコードを読む
	 *  
	 * @param prmkey
	 * @param dt
	 * @return
	 */
	public int readRecord(String filename,Hashtable dt){
		
		String	cmd 	= 	" SELECT * FROM  " + dbName("cabinet") + "  WHERE fname = " + m1 + filename + m1;
		if(LOG.fa) LOG.println("CBdatabase #read() : SQL = " + cmd);
		
		int		count	=	0;
		Connection  conn	=	null;
		Statement 	stmt	=	null;
		try {
			conn 		 	=	Broker.getConnection();
			stmt 			=	conn.createStatement();
			ResultSet	rs	=	stmt.executeQuery(cmd);
			//
			while(rs.next()){
				//
				putParameter(dt,KEY,	rs.getString("seqkey"));
				putParameter(dt,SUBJECT,rs.getString("subject"));
				putParameter(dt,OWNER,	rs.getString("owner"));
				putParameter(dt,FNAME,	rs.getString("fname"));
				putParameter(dt,KUBUN,	rs.getString("kubun"));
				putParameter(dt,SIZE,	rs.getString("size"));
				putParameter(dt,PATH,	rs.getString("filepath"));
				putParameter(dt,URL,	rs.getString("fileurl"));
				putParameter(dt,FROM,	rs.getString("date_from"));
				putParameter(dt,IDX,	rs.getString("idx"));
				count++;
			}
			
		}catch(Exception e){
			/*
			 * 利用の中では失敗してもエラーではない
			 */
			if(LOG.fa) { 
				LOG.println("#### なし:CBdatabase #read( " + filename + " )") ;
				e.printStackTrace();
			}
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}		
	/**
	 *  レコードを読む
	 *  
	 * @param prmkey
	 * @param dt
	 * @return
	 */
	public int read(String prmkey,Hashtable dt){
		
		String	cmd 	= 	" SELECT * FROM  " + dbName("cabinet") + "  WHERE seqkey = " + m1 + prmkey + m1 + "  ORDER BY seqkey ASC";
		if(LOG.fa) LOG.println("CBdatabase #read() : SQL = " + cmd);
		
		int		count	=	0;
		Connection  conn	=	null;
		Statement 	stmt	=	null;
		try {
			conn 		 	=	Broker.getConnection();
			stmt 			=	conn.createStatement();
			ResultSet	rs	=	stmt.executeQuery(cmd);
			//
			while(rs.next()){
				//
				putParameter(dt,KEY,	rs.getString("seqkey"));
				putParameter(dt,SUBJECT,rs.getString("subject"));
				putParameter(dt,OWNER,	rs.getString("owner"));
				putParameter(dt,FNAME,	rs.getString("fname"));
				putParameter(dt,KUBUN,	rs.getString("kubun"));
				putParameter(dt,SIZE,	rs.getString("size"));
				putParameter(dt,PATH,	rs.getString("filepath"));
				putParameter(dt,URL,	rs.getString("fileurl"));
				putParameter(dt,FROM,	rs.getString("date_from"));
				putParameter(dt,IDX,	rs.getString("idx"));
				count++;
			}
			
		}catch(Exception e){
			/*
			 * 利用の中では失敗してもエラーではない
			 */
			if(LOG.fa) { 
				LOG.println("#### 失敗:CBdatabase #read( " + prmkey + " )") ;
				e.printStackTrace();
			}
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}	
	/**
	 *  全てのキーを読み出す
	 *  
	 * @param prmkey
	 * @param dt
	 * @return
	 */
	public int readAllKeys(Vector dt){
		
		String	cmd 	= 	" SELECT * FROM  " + dbName("cabinet") + "  ORDER BY seqkey ASC";
		if(LOG.fa) LOG.println("CBdatabase #read() : SQL = " + cmd);
		
		int			count	=	0;
		Connection  conn	=	null;
		Statement 	stmt	=	null;
		try {
			conn 		 	=	Broker.getConnection();
			stmt 			=	conn.createStatement();
			ResultSet	rs	=	stmt.executeQuery(cmd);
			while(rs.next()){
				dt.add(rs.getString("seqkey"));
				count++;
			}
		}catch(Exception e){
			/*
			 * 利用の中では失敗してもエラーではない
			 */
			if(LOG.fa) { 
				LOG.println("#### 失敗:CBdatabase #readAllKeys") ;
				e.printStackTrace();
			}
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}	

	/** 
	 * レコードを更新する 
	 * 
	 * @param prmkey
	 * @param dt
	 * @return
	 */
	public int update( String prmkey,Hashtable dt){
		
		int		count	=	0;
		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		String		query	=	updateQuery(prmkey, dt);
		//
		try {
			conn 	=	Broker.getConnection();
			stmt 	=	conn.createStatement();
			count	=	stmt.executeUpdate(query);
		
		}catch(Exception e){
			//LOG.println("#### 失敗:CBdatabase #update( " + prmkey + ", Hashtable dt) ");
			//e.printStackTrace();
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}	
	String	updateQuery(String prmkey, Hashtable dt ){
		String	query	=	"UPDATE "+ dbName("cabinet") + " set " 
							+ "subject   = "  + m1 + getParameter(dt,SUBJECT) + m2
							+ "owner     = "  + m1 + getParameter(dt,OWNER)   + m2
							+ "fname     = "  + m1 + getParameter(dt,FNAME)   + m2
							+ "kubun     = "  + m1 + getParameter(dt,KUBUN)   + m2
							+ "size      = "  + m1 + getParameter(dt,SIZE)    + m2
							+ "filepath  = "  + m1 + getParameter(dt,PATH)    + m2
							+ "fileurl   = "  + m1 + getParameter(dt,URL)     + m2
							+ "date_from = "  + m1 + getParameter(dt,FROM)    + m2
							+ "idx       = "  + m1 + getParameter(dt,IDX)     + m1
							//
							+ " WHERE seqkey  = " + m1 + prmkey     + m1;

		if(LOG.fa) LOG.println("CBdatabase #updateQuery() : query =" +  query);
		return query;
	}
	
	/**
	 *  レコードを挿入する
	 * 
	 * @param dt
	 * @return
	 */
	public int insert( Hashtable dt ){
		
		Connection  conn  	= null;
		Statement 	stmt  	= null;
		int 		count	= 0;
		String query = insertQuery(dt);
		//
		try {
			conn 	= Broker.getConnection();
			stmt 	= conn.createStatement();
			count	= stmt.executeUpdate(query);
			
		}catch(Exception e){
			System.out.println("### 失敗:CBdatabase #insert()");
			e.printStackTrace();
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			Broker.freeConnection(conn);
		}
		return count;
	}
	String	insertQuery(Hashtable dt ){
		String query = " INSERT INTO " + dbName("cabinet") + "  VALUES (" 
					+ m1 + getParameter(dt,KEY)     + m2 
					+ m1 + getParameter(dt,SUBJECT) + m2 
					+ m1 + getParameter(dt,OWNER)   + m2 
					+ m1 + getParameter(dt,FNAME)   + m2
					+ m1 + getParameter(dt,KUBUN)   + m2
					+ m1 + getParameter(dt,SIZE)    + m2 
					+ m1 + getParameter(dt,PATH) 	+ m2 
					+ m1 + getParameter(dt,URL) 	+ m2 
					+ m1 + getParameter(dt,FROM)    + m2 
					+ m1 + getParameter(dt,IDX)	    + m1
					+ ")";
		if(LOG.fa) LOG.println("CBdatabase #insertQuery() : query =" +  query);
		return query;
	}
	
	/**
	 *  レコードを削除する 
	 * 
	 * @param prmkey
	 * @return
	 */
	public int delete( String prmkey){

		Connection  conn  	=	null;
		Statement 	stmt  	=	null;
		int 		n		=	0;
		String		query	=	"DELETE from " + dbName("cabinet") + " WHERE seqkey = "  + m1 + prmkey + m1;
		if(LOG.fa) LOG.println("CBdatabase #delete() : query = " +  query);
		
		try {
			conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
			stmt 	= conn.createStatement();
			n  		= stmt.executeUpdate(query); // ここでdeleteする
			//
		}catch(Exception e){
			System.out.println("### 失敗: #delete(" + prmkey + ")");
			e.printStackTrace();
			
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			//
			Broker.freeConnection(conn);/* connection を Broker に返す */
		}
		return n; // 件数（普通は１）
	}
	
	/**
	 * ファイルキャビネットに必要なデータベーステーブルを一括作成する
	 *
	 */
	public	void	createCabinetDatatables(){
		/*
		 * キー発生DBに登録し、初期値１を設定しておく
		 */
		CBkey	cb	=	new	CBkey(Broker);
		cb.insert(szDB,"001");
		/*
		 * テーブルを作成する
		 */
		create_cabinetDB(szDB);
		create_CBclassIndex(szDB);
		create_CBidIndex(szDB);
	}
	
	
	/**
	 * cabinet テーブルを作成する
	 * @param tbl	グループ名
	 * @return
	 */	
	public int create_cabinetDB(String tbl){
		if(LOG.fa) LOG.println("■ CBdatabase #create_cabinetDB() : ファイルキャビネとデータベースの作成");
		//
		Connection   conn  		= null;
		Statement 	 stmt  		= null;
		int n = -1;
		//
		String QUERY = create_cabinetDB_Query(tbl);
		try {
			conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
			stmt 	= conn.createStatement();
			n  		= stmt.executeUpdate(QUERY);
			//
		}catch(Exception e){
			System.out.println("■ CBdatabase ### 失敗:create_cabinetDB_Query(String tbl) " + e);
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			//
			Broker.freeConnection(conn);/* connection を Broker に返す */
		}
		return n; // （０が返る）
	}
	String create_cabinetDB_Query(String tbl){
		if(LOG.fa) LOG.println("■ CBdatabase  #create_cabinetDB_Query()");
		if(LOG.fa) LOG.println("          tbl = " + tbl);
		
		
		String Q = "create table cabinet_" + tbl + " ( " + 
			     "seqkey        CHAR(18)       PRIMARY KEY, " +
			     "subject       TEXT, " 		+
			     "owner         VARCHAR(12), " 	+      
			     "fname         VARCHAR(256), " +
			     "kubun         CHAR(5), " 		+
			     "size          VARCHAR(20), " 	+
			     "filepath      VARCHAR(256), " +
			     "fileurl       VARCHAR(256), " +
			     "date_from     CHAR(12), " 	+
			     "idx           CHAR(3) " 		+
			   ");  ";
		return Q;
	}	
	
	/**
	 * class 別のインデックス テーブルを作成する
	 * @param tbl	グループ名
	 * @return
	 */	
	public int create_CBclassIndex(String tbl){
		if(LOG.fa) LOG.println("■ CBdatabase #create_CBclassIndex() : ファイルキャビネとデータベースの作成");
		//
		Connection   conn  		= null;
		Statement 	 stmt  		= null;
		int n = -1;
		//
		String QUERY = create_CBclassIndex_Query(tbl);
		try {
			conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
			stmt 	= conn.createStatement();
			n  		= stmt.executeUpdate(QUERY);
			//
		}catch(Exception e){
			System.out.println("■ CBdatabase ### 失敗:create_CBclassIndex_Query(String tbl) " + e);
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			//
			Broker.freeConnection(conn);/* connection を Broker に返す */
		}
		return n; // （０が返る）
	}
	String create_CBclassIndex_Query(String tbl){
		if(LOG.fa) LOG.println("■ CBdatabase  #create_CBclassIndex_Query()");
		if(LOG.fa) LOG.println("          tbl = " + tbl);
		
		
		String Q = " create table CBclassIndex_" + tbl + " ( " 	+
			       " accessclass  VARCHAR(16), "			  	+
			       " seqkey       VARCHAR(18)  ); " 			+
			       " create index CBclassIndex_" + tbl +"_idx on CBclassIndex_" + tbl  + " (accessclass); ";
		return Q;
	}	
	
	/**
	 * id 別のインデックステーブルを作成する
	 * @param tbl	グループ名
	 * @return
	 */	
	public int create_CBidIndex(String tbl){
		if(LOG.fa) LOG.println("■ CBdatabase #create_CBidIndex() : ファイルキャビネとデータベースの作成");
		//
		Connection   conn  		= null;
		Statement 	 stmt  		= null;
		int n = -1;
		//
		String QUERY = create_CBidIndex_Query(tbl);
		try {
			conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
			stmt 	= conn.createStatement();
			n  		= stmt.executeUpdate(QUERY);
			//
		}catch(Exception e){
			System.out.println("■ CBdatabase ### 失敗:create_CBidIndex_Query(String tbl) " + e);
		} finally {
			try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
			//
			Broker.freeConnection(conn);/* connection を Broker に返す */
		}
		return n; // （０が返る）
	}
	String create_CBidIndex_Query(String tbl){
		if(LOG.fa) LOG.println("■ CBdatabase  #create_CBidIndex_Query()");
		if(LOG.fa) LOG.println("          tbl = " + tbl);
		
		
		String Q = " create table CBidIndex_" + tbl + " ( " 	+
			       " id           VARCHAR(12), "			  	+
			       " seqkey       VARCHAR(18)  ); " 			+
			       " create index CBidIndex_" + tbl +"_idx on CBidIndex_" + tbl  + " ( id ); ";
		return Q;
	}	
}
