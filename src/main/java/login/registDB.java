/*
 * 作成日: 2004/12/19
 *
 * TODO
 */
package login;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;

import database.DbConnectionBroker;

import framework.LOG;
import	framework.PCvar;
import tktools.Csv;
import tktools.Gear;

/**
 *
 */
public class registDB implements PCvar {

	private final String m1		= "\'";
	private final String m2		= "\',";
	private final String m3		= ",";
	//
	DbConnectionBroker Broker;
	
	public	registDB(DbConnectionBroker Broker){
		
		this.Broker	=	Broker;
	}
	
	////////////////////////////////////////////////////////////////////////
	//
	//
	//		ユーザー登録関係　2003.9.7 (regist テーブル) 
	//                        2005.8   desktop 用に改訂 
	//
	//
	////////////////////////////////////////////////////////////////////////
	//
	//
	// ユーザー登録レコードの挿入
	/*
 	create table regist (
 		user_id         VARCHAR(12) PRIMARY KEY,,
 		user_active     CHAR(1) ,
 		szDB            VARCHAR(30) DEFAULT '',
 		teMail          VARCHAR(50) ,
 		passwd          VARCHAR(20) ,
 		teName          VARCHAR(20) ,
 		hurigana        VARCHAR(30) ,
 		shozoku         VARCHAR(50) ,
 		url_t           VARCHAR(150) ,
 		url_s           VARCHAR(150) ,
 		note            VARCHAR(100),
 		domain          VARCHAR(50)
    );
    */
	public	int insert_regist(Hashtable htb){
		if(LOG.fa) LOG.println("class Database #insert_regist() : ユーザー登録レコードの挿入 の先頭です");
		//
		Connection   conn  	= null;
       	Statement 	 stmt  	= null;
		String  	cmd 	= insert_regist_query(htb);  // DB members に 全データを追加
		int 		count	 = 0;
       	try {
          	conn 	= Broker.getConnection(); /** Broker からDB Connection を得る */
			stmt 	= conn.createStatement();
			count 	= stmt.executeUpdate(cmd);
		}catch(Exception e){
			htb.put("_msg","★ このユーザーIDは既に登録されています");
			System.out.println("### 失敗  Database #insert_regist()" + e);
			count = 0;
			//
		} finally {
            try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
       	    //
           	if(LOG.fa) System.out.println("## bye");
			Broker.freeConnection(conn);
       	}
		return count;
	}
	// 挿入クエリ文字列を作る
	// 値は xdb で設定済み
	//
	String insert_regist_query(Hashtable htb){
		if(LOG.fa) LOG.outHash(htb,"class Database #insert_regist_query() : 挿入クエリ文字列を作る の先頭です");
		//
		String	user_id			= Gear.strHashIncludeNull(htb,"_user_id");
		String	user_active		= Gear.strHashIncludeNull(htb,"_user_active");
		//
		String	szDB			= Gear.strHashIncludeNull(htb,"_szDB");

		String	teMail			= Gear.strHashIncludeNull(htb,"_teMail");
		String	passwd			= Gear.strHashIncludeNull(htb,"_passwd");
		
		String	teName			= Gear.strHashIncludeNull(htb,"_teName");
		String	hurigana		= Gear.strHashIncludeNull(htb,"_hurigana");
		String	shozoku			= Gear.strHashIncludeNull(htb,"_shozoku");
		String	url_t			= Gear.strHashIncludeNull(htb,"_url_t");
		String	url_s			= Gear.strHashIncludeNull(htb,"_url_s");

		String	note			= Gear.strHashIncludeNull(htb,"_note");
		String	domain			= Gear.strHashIncludeNull(htb,"_domain");

		/*
	 	create table regist (
	 		user_id         VARCHAR(12) PRIMARY KEY,
	 		user_active     CHAR(1) ,
	 		szDB            VARCHAR(30) DEFAULT '',
	 		teMail          VARCHAR(50) ,
	 		passwd          VARCHAR(20) ,
	 		teName          VARCHAR(20) ,
	 		hurigana        VARCHAR(30) ,
	 		shozoku         VARCHAR(50) ,
	 		url_t           VARCHAR(150) ,
	 		url_s           VARCHAR(150) ,
 		    note            VARCHAR(100),
 		    domain          VARCHAR(50)
	    );
	*/		
		StringBuffer bf = new  StringBuffer(2000);
		bf.append(" INSERT INTO regist VALUES ( ");
		bf.append(m1 + user_id + m2 );
		bf.append(m1 + user_active + m2 );
		bf.append(m1 + szDB + m2 );
		bf.append(m1 + teMail + m2 );
		bf.append(m1 + passwd + m2 );
		bf.append(m1 + teName + m2 );
		bf.append(m1 + hurigana + m2 );
		bf.append(m1 + shozoku + m2 );
		bf.append(m1 + url_t + m2 );
		bf.append(m1 + url_s + m2 );
		bf.append(m1 + note + m2 );
		bf.append(m1 + domain + m1 );
		bf.append(" )" );

		
		//bf.append(m1 + bunnya + m2 );
		//bf.append(m1 + address + m2 );
		//bf.append(m1 + homedir + m2 );
		//
		String cmd = bf.toString();
		if(LOG.fa) LOG.println("class Database #insert_regist_query() : Q = " + cmd);
		if(LOG.fa) System.out.println(cmd); 
		//
		return cmd;
	}
	//
	// コンバートによる userInfo の書き込み
	//
	//
	public	int convertInfo(Hashtable htb){
		Connection   conn  	= null;
       	Statement 	 stmt	= null;
		String  	 cmd 	= makeConvertInfo(htb);  // DB members に 全データを追加
		int 	count		= 0;
       	try {
          	conn 	= Broker.getConnection(); /** Broker からDB Connection を得る */
			stmt 	= conn.createStatement();
			count 	= stmt.executeUpdate(cmd);
		}catch(Exception e){
			System.out.println("### 失敗:convertInfo(Hashtable htb) " + e);
			count = 0;
			//
		} finally {
            try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
       	    //
			Broker.freeConnection(conn);/** connection を Broker に返す */
       	}
		return count;
	}
	// クエリ文字列を作る
	/*
	create table membersInfo (
	    user_id         VARCHAR(12)   PRIMARY KEY,
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
	
	public	String makeConvertInfo(Hashtable htb){
		//
		String 	user_id			= Gear.strHashIncludeNull(htb,"_user_id");
		String	user_active		= Gear.strHashIncludeNull(htb,"_user_active");
		String  user_db		    = Gear.strHashIncludeNull(htb,"_szDB");

		String 	user_mail		= Gear.strHashIncludeNull(htb,"_teMail");
		String 	user_passwd		= Gear.strHashIncludeNull(htb,"_passwd") ;
		String 	user_name		= Gear.strHashIncludeNull(htb,"_teName");
		String 	user_hurigana	= Gear.strHashIncludeNull(htb,"_hurigana");

		String  user_url_t	    = Gear.strHashIncludeNull(htb,"_url_t");
		String 	note			= Gear.strHashIncludeNull(htb,"_note");
		String 	domain			= Gear.strHashIncludeNull(htb,"_domain");
		//
		StringBuffer bf = new  StringBuffer(2000);
		bf.append(" INSERT INTO membersInfo VALUES ( ");
		bf.append(m1 + user_id + m2 );
		bf.append(m1 + user_active + m2 );
		bf.append(m1 + user_db + m2 );			// 2003.3.8  追加

		bf.append(m1 + user_mail + m2 );
		bf.append(m1 + user_passwd + m2 );
		bf.append(m1 + user_name + m2 );
		bf.append(m1 + user_hurigana + m2 );
		//
		bf.append(m1 + user_url_t + m2 );		// 2003.6.17 追加
		
		bf.append(m1 + DIV_TEACHER + m2 );
		
		bf.append(m1 + note + m2 );
		bf.append(m1 + domain + m1 );
		bf.append(" )" );
		//
		String cmd = bf.toString();
		if(LOG.fa) LOG.println("■Database #makeConvertInfo() : members DB への追加" + cmd); 
		//
		return cmd;
	}
	/*
	 * コンバートによる shozoku の書き込み
	 * 
	create table shozoku (
    	sz_id         VARCHAR(30) PRIMARY KEY,
    	sz_active     CHAR(1)       DEFAULT '0',
    	sz_name       VARCHAR(50)   DEFAULT '',
    	sz_note       VARCHAR(100)  DEFAULT '',
    	sz_url_s      VARCHAR(100)  DEFAULT '',
    	sz_domain     VARCHAR(50)   DEFAULT ''
    );	 
	 */
	public	int convertShozoku(Hashtable htb){
		Connection   conn  	= null;
       	Statement 	 stmt	= null;
		String  	 cmd 	= makeConvertShozoku(htb);
		int 		count	= 0;
       	try {
          	conn 	= Broker.getConnection(); /** Broker からDB Connection を得る */
			stmt 	= conn.createStatement();
			count 	= stmt.executeUpdate(cmd);
		}catch(Exception e){
			System.out.println("### 失敗 class Database #convertShozoku() :" + e);
			count = 0;
			//
			//htb.put("_alart","登録処理できません。このメールアドレスはすでに登録されている可能性があります。");	
			//fpath2 = para.getHtmlPath() + para.getFileName("REGIST");				// 登録ページのファイル名を得る
			//paramPrint(para.getHtmlOut(),htb,para,fpath2);							// 登録ページを出力する
		} finally {
            try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
       	    //
			Broker.freeConnection(conn);/** connection を Broker に返す */
       	}
		return count;
	}
	// クエリ文字列を作る
/*
	create table shozoku (
    	sz_id         VARCHAR(30) PRIMARY KEY,
    	sz_active     CHAR(1)       DEFAULT '0',
		sz_name       VARCHAR(50)   DEFAULT '',
    	sz_note       VARCHAR(100)  DEFAULT '',
		sz_url_s      VARCHAR(100)  DEFAULT ''
	);
*/	
	
	public	String makeConvertShozoku(Hashtable htb){
		//
		String 	sz_id		= Gear.strHashIncludeNull(htb,"_szDB");
		String 	sz_active	= "0";
		String 	sz_name		= Gear.strHashIncludeNull(htb,"_shozoku") ;
		/*
		 * 最初に登録するユーザーは
		 * この所属の責任者として氏名とIDをCSV形式で記録することにした
		 * 2004.10.28
		 */
		String	sz_note		=	Gear.strHashIncludeNull(htb,"_user_id") + "," + Gear.strHashIncludeNull(htb,"_teName");
		String 	sz_url_s	=	Gear.strHashIncludeNull(htb,"_url_s");
		//
		String 	sz_domain	=	Gear.strHashIncludeNull(htb,"_domain");
		//
		StringBuffer bf = new  StringBuffer(2000);
		bf.append(" INSERT INTO shozoku VALUES ( ");
		bf.append(m1 + sz_id + m2 );
		bf.append(m1 + sz_active + m2 );
		bf.append(m1 + sz_name + m2 );
		bf.append(m1 + sz_note + m2 );
		bf.append(m1 + sz_url_s + m2 );
		bf.append(m1 + sz_domain + m1 );
		bf.append(" )" );
		//
		String cmd = bf.toString();
		//
		return cmd;
	}
		
	//
	//  所属レコードがあるかどうかのチェック
	//
	public	int exitShozoku(String _sz_id){
		String       sz_id   = _sz_id.trim();
		Connection   conn  	 = null;
       	Statement 	 stmt  	 = null;
       	//ID が同じものを取り出す
		String QUERY = shozokuQuery2(sz_id);	// 全レコード内容をCSVで返すクエリ
		int		n    = 0;
		Csv     record = null;
		try {
          	conn 	= Broker.getConnection(); /* Broker からDB Connection を得る */
			stmt 	= conn.createStatement();
			ResultSet rs = stmt.executeQuery(QUERY);
			//
			while(rs.next()){
				n++;
			}
		}catch(Exception e){
			//System.out.println("### 失敗:search(String tbl,String _id) " + e);
			// 検索できなかったということで失敗ではない。st に null を返す
			if(LOG.fa) System.out.println("# no data !");
			record = null; // 念のため
		} finally {
            try{if(stmt != null)  {stmt.close();}}  catch(SQLException e1){};
       	    //
			Broker.freeConnection(conn);/* connection を Broker に返す */
       	}
		if(LOG.fa) LOG.println("■ Database #exitShozoku() count = " + n);
		return n; // 全レコード件数
	}
	//
	// shozoku テーブルを検索するクエリを作成
	String shozokuQuery2(String sz_id){
		String Q = "SELECT * FROM " + "shozoku" + " WHERE sz_id = " + m1 + sz_id + m1 ;
		if(LOG.fa) LOG.println("■ Database #shozokuQuery2() cmd = " + Q);
		return Q;		
	}

}
