/*
	学生データベースを処理するクラス

	create table Student (
	    id  			VARCHAR(12)  PRIMARY KEY ,
		kname 			VARCHAR(20) ,
		
		email 			VARCHAR(60) ,
		keitai 			VARCHAR(60) ,
		
		stPasswd 		VARCHAR(20) ,
		mailselections 	VARCHAR(50) ,  <-------- 携帯で受け取るメールの種類番号のCSV    0,1,1,0,1,0,0,1
		classInfo 		VARCHAR(400),  <-------- 受講しているクラスキーのcsv            kawaba01-001,kawaba01-012,moritann-003
		
		active_i        CHAR(1)     default '-',  <----------- アドレス登録後確認されれば '1' (active) になる
		active_k        CHAR(1)     default '-',  <----------- 同上
		number_i        VARCHAR(3)  default '-',  <----------- アドレス登録確認に使う確認番号
		number_k        VARCHAR(3)  default '-'   <----------- 同上
	);	
		
	データベース名は利用グループごとに異なるので、データベース名もパラメータ化して処理する

*/ 

package student;
import java.util.Hashtable;
import java.util.Vector;
import database.Database;
import framework.LOG;
import tktools.Csv;
//
public class Student extends Object{
    //
	// 定数
	public static int	ID				= 0;	// stNumber
	public static int	KNAME			= 1;
	
	public static int	EMAIL			= 2;	
	public static int	KEITAI			= 3;	
	
	public static int	STPASSWD		= 4;	
	public static int	MAILSELECTIONS	= 5;	
	public static int	CLASSINFO		= 6;	
	
	public static int	ACTIVE_I		= 7;	
	public static int	ACTIVE_K		= 8;	
	public static int	NUMBER_I		= 9;	
	public static int	NUMBER_K		= 10;	
	//
	// 定数２（パラメータ並びを名前で指定するのに使う）
	public static	int	MAIL_SEL			= 0;
	public static	int	EDITOR_ROWS			= 1;
	public static	int	EDITOR_ROWS_MAX		= 2;
	public static	int	EDITOR_ROWS_MIN		= 3;
	public static	int	EDITOR_ROWS_UNIT	= 4;
	//
	public static int	MIN		= ID;	
	public static int	MAX		= NUMBER_K;	
	//
	String		szDB;		// データベース名
	Database	db;			// データベースクラス
	Vector		rec;		// データレコード
	boolean		empty = true;		//空かどうか
	//
	public static final String EMPTY = "-";
	//
	// 旧版から引き継いだ変数
	//
	String szName;			// 認証でデータベースをサーチしたときに得られる所属大学名(テーブル名と同じ)
	//
	String id;				// 学籍番号
	String kname;			// 漢字氏名
	String email;			// 電子メール
	String keitai;			// 携帯メール
	String stPasswd;		// 学生のパスワード
	//
	String mailselections;	// 携帯で受信するメールの選択	例）0,1,1,0,1,0,0,1　　"*"なら全てを受け取ることに（初期値）
	String classInfo;		// 受講情報						例）kawaba01-za,kawaba01-zb,moritann-aa
	//
	String active_i;
	String active_k;
	String number_i;
	String number_k;
	//
	///////////////////////////////////////////////////
	//
	//   コンストラクタ
	//
	///////////////////////////////////////////////////
	//
	// キーでデータベースを検索して生成するあるいは空のレコードを生成する
	//
	// 　　	teUid が null なら全て "" のレコードを生成する
	//		レコード中に null があれば "" にする
	//
	//
	//      ※ 該当レコードがデータベース中にないときは空のレコードが生成される
	// 
	public Student(String _szDB,String id, Database _db){
        if(LOG.fa) LOG.println("コンストラクタStudent()の先頭です（ＤＢから生成）" + "/ id =" + id );
		//
		szDB		= _szDB;
		db 			= _db;
		rec			= new Vector (20,10);
		//
		// データベースから検索し、フィールド項目の値が null だった場合、メールアドレスと漢字氏名は
		//  '-' にかえ、パスワードは id に、設定情報(mailselections)は '*' に替え、その他の項目は
		// "" にされる。したがって、返されるフィールド項目に NULL はない。
		int 	cnt = db.studentAllInfo(szDB, id, rec);
		
		//
		//
		int  pos = rec.size();
		for(int i=pos; i<=MAX; i++){
			rec.add("");
		}
		//
		// 空かどうかをセット
		empty	= false;
		if(cnt==0) 	empty = true;
		//
		if(empty){
			set_id(id);
			set_kname("-");
			set_email("-");
			set_keitai("-");
			//
			set_stPasswd(id);
			set_mailselections("*");
			set_classInfo("");
			//
			set_active_i("");
			set_active_k("");
			set_number_i("");
			set_number_k("");
		}
		if(LOG.fa) LOG.outVector(rec,"データベース検索 Student の値");
	}
	//
	public boolean isEmptyRecord(){
		return empty;
	}
	////////////////////////////////////////////////////
	// 
	//  初期設定情報を読み込む(mailselections を代用している）
	//
	////////////////////////////////////////////////////
	//
	// 初期値を得る
	// 初期値がなければ入力した値を初期値としてデータベースに書き込む
	public	String	initialValues(int pos,String init){
		if(LOG.fa) LOG.println("class Student #initialValues() : ■ 初期値を得る の先頭です");
		//
		String	str	= mailselections();
		//
		// 初期値を持って帰る
		// 指定された並びの初期値がない場合は、初期値をデータベースに書き込みそれから取る
		Csv		cs	= new Csv(str,"$");
		if(cs.size() < pos + 1){
			str	= init;						// 	"*$16"
			set_mailselections(str);		// 初期値を設定
			update();						// 書き込む
		}
		//
		Csv	cc	= new Csv(str,"$");
		if((pos < cc.size())&&(pos >= 0)){
			return	cc.get(pos);
		}
		return	"";
	}
	//
	// 初期値を変更する
	// pos 番目の初期値を item とする．
	// 初期値に pos 番目がないときは init を新たな初期値として、その pos 番目を変更する
	public	void	update_init(int pos,String item,String init){
		if(LOG.fa) LOG.println("class Student #updateHtmlRows() : ■ 初期値を変更する の先頭です");
		//
		String	str		= mailselections();
		Csv		cs		= new Csv(str,"$");
		if(cs.size() < pos+1){
			cs	= new Csv(init,"$");
		}
		//
		StringBuffer	bf		= new StringBuffer(50);	// max 50
		boolean			flag	= false;
		for(int k=0; k<cs.size(); k++){
			if(flag) bf.append("$");
			//
			if(k==pos){
				bf.append(item);
			}else{
				bf.append(cs.get(k));
			}
			flag	= true;
		}
		String strNew	= bf.toString();
		set_mailselections(strNew);		// 値を設定
		update();						// 書き込む
	}
	
	////////////////////////////////////////////////////
	// - STATIC -
	//  学籍番号が正しいかどうかチェックする
	//
	////////////////////////////////////////////////////
	//
	static	boolean	isStudent(String tbl,String id,Database db){
		Vector dummy = new Vector(20,10);
		int n 		 =db.studentAllInfo(tbl,id,dummy);
		if(n==0) return false;
		return	 true;
	}
	//
	///////////////////////////////////////////////////
	//
	//　　データベースの更新
	//
	///////////////////////////////////////////////////
	//
	// データベースに書き込む（更新）
	public int insert(){
		int    n = db.insertStudent(szDB, this ); 		// !!
		return n;
	}
	public int update(){
		int    n = db.updateStudent(szDB, this ); 		// !!
		return n;
	}
	public int delete(){
		int    n = db.deleteStudent(szDB, id() );
		return n;
	}
	///////////////////////////////////////////////////
	//
	//　　レコードの値をセットする
	//
	///////////////////////////////////////////////////
	//
	public void set_id(String str)				{ if(isEmpty(str)) {str = "-"; } rec.set(ID,str); 				}
	public void set_kname(String str)			{ if(isEmpty(str)) {str = "-"; } rec.set(KNAME,str); 			}
	//
	public void set_email(String str)			{ if(isEmpty(str)) {str = "-"; } rec.set(EMAIL,str); 			}
	public void set_keitai(String str)			{ if(isEmpty(str)) {str = "-"; } rec.set(KEITAI,str); 			}
	//
	public void set_stPasswd(String str)		{ if(isEmpty(str)) {str = "-"; } rec.set(STPASSWD,str); 		}
	public void set_mailselections(String str)	{ if(isEmpty(str)) {str = "*"; } rec.set(MAILSELECTIONS,str); 	}
	public void set_classInfo(String str)		{ if(isEmpty(str)) {str = "-"; } rec.set(CLASSINFO,str); 		}
	//
	public void set_active_i(String str)		{ if(isEmpty(str)) {str = "-"; } rec.set(ACTIVE_I,str); 		}
	public void set_active_k(String str)		{ if(isEmpty(str)) {str = "-"; } rec.set(ACTIVE_K,str); 		}
	public void set_number_i(String str)		{ if(isEmpty(str)) {str = "-"; } rec.set(NUMBER_I,str); 		}
	public void set_number_k(String str)		{ if(isEmpty(str)) {str = "-"; } rec.set(NUMBER_K,str); 		}
	//
	void	set_szName(String s)			{ szName = s;} // 旧版との互換性のため残す
	//
	///////////////////////////////////////////////////
	//
	//　　レコードの値を返す
	//
	///////////////////////////////////////////////////
	//
	// 文字列の空チェック
	public boolean isEmpty(String str){
		if((str==null)||(str.length()==0)) return true;
		return	false;
	}
	
	String  szName()		{ return szDB();}
	String  szDB()			{ return szDB;}
	//
	public String id()				{ return (String)rec.get(ID); 			}
	public String kname()			{ return (String)rec.get(KNAME); 		}
	//
	public String email()			{ return (String)rec.get(EMAIL); 		}
	public String keitai()			{ return (String)rec.get(KEITAI); 		}
	//
	public String stPasswd()		{ return (String)rec.get(STPASSWD); 	}
	public String mailselections()	{ return (String)rec.get(MAILSELECTIONS);}
	public String classInfo()		{ return (String)rec.get(CLASSINFO); 	}
	//
	public String active_i()		{ return (String)rec.get(ACTIVE_I); 	}
	public String active_k()		{ return (String)rec.get(ACTIVE_K); 	}
	public String number_i()		{ return (String)rec.get(NUMBER_I); 	}
	public String number_k()		{ return (String)rec.get(NUMBER_K); 	}
	//
	//
	//  デバッグ用プリント
	//
	public void DBG_print(){
		DBG_print("");
	}
	//
	public void DBG_print(String msg){
		LOG.println(msg);
		//
		LOG.println("     id				=" + id() );
		LOG.println("     kname			=" + kname() );
		
		LOG.println("     email			=" + email() );
		LOG.println("     keitai			=" + keitai() );
		
		LOG.println("     stPasswd		=" + stPasswd() );
		LOG.println("     mailselections	=" + mailselections() );
		LOG.println("     classInfo		=" + classInfo() );
		
		LOG.println("     active_i		=" + active_i() );
		LOG.println("     active_k		=" + active_k() );
		LOG.println("     number_i		=" + number_i() );
		LOG.println("     number_k		=" + number_k() );
	}
	//
	////////////////////////////////////////////////////////////////////////////////
	//
	//   ハッシュとのやり取り
	//
	//
	////////////////////////////////////////////////////////////////////////////////
	//
	// ハッシュにデータを返す
	public void setToHash(Hashtable htb){
		//
		htb.put("_d"				,id()				);
		htb.put("_kname"			,kname()			);
		//
		htb.put("_email"			,email()			);
		htb.put("_keitai"			,keitai()			);
		//
		htb.put("_stPasswd"			,stPasswd()			);
		htb.put("_mailselections"	,mailselections()	);
		htb.put("_classInfo" 		,classInfo()		);
		//
		htb.put("_active_i"			,active_i()			);
		htb.put("_active_k"			,active_k()			);
		htb.put("_number_i"			,number_i()			);
		htb.put("_number_k"			,number_k()			);
	}
	//
	// ハッシュのデータで内容を埋める
	public void setFromHash(Hashtable htb){
		//
		set_id( (String)htb.get("_id") );
		set_kname( (String)htb.get("_kname") );
		//
		set_email( (String)htb.get("_email") );
		set_keitai( (String)htb.get("_keitai") );
		
		set_stPasswd( (String)htb.get("_stPasswd") );
		set_mailselections( (String)htb.get("_mailselections") );
		set_classInfo( (String)htb.get("_classInfo") );
		//
		set_active_i( (String)htb.get("_active_i") );
		set_active_k( (String)htb.get("_active_k") );
		set_number_i( (String)htb.get("_number_i") );
		set_number_k( (String)htb.get("_number_k") );
		//
	}

	//
	//
	////////////////////////////////////////////////////////////////////////////////
	//
	// 		以下は検索時のコンテナとして使うコンストラクタ
	//
	//		（古いバージョンとの互換性を維持するため残してある）
	//
	////////////////////////////////////////////////////////////////////////////////
	//
	//
    public Student(){
		id		= "";
		kname	= "";
		email	= "";
		keitai	= "";
		//
		stPasswd 		="";
		mailselections	= "";
		classInfo		= "";
		szName 			= "";
		//
		active_i		= "-";
		active_k		= "-";
		number_i		= "-";
		number_k		= "-";
		//
    }
    public Student(String _id,String _kname,String _email,String _keitai){
        if(LOG.fa) LOG.println("コンストラクタ　Student(String _id,String _kname,String _email,String _keitai)　の先頭です");
		if(LOG.fa) LOG.println("    id      = " + _id);
		if(LOG.fa) LOG.println("    keitai  = " + _kname);
		if(LOG.fa) LOG.println("    email   = " + _email);
		if(LOG.fa) LOG.println("    keitai   = " + _keitai);
		if(LOG.fa) LOG.println(" ");
		//
		id		= _id;
		kname	= (((_kname  != null) && (_kname.length()>0))  ? _kname  : "-");	// nul lや "" にしない
		email	= (((_email  != null) && (_email.length()>0))  ? _email  : "-");
		keitai	= (((_keitai != null) && (_keitai.length()>0)) ? _keitai : "-");
		//
		// これらは初期値では埋めないで、必要なときに設定する
		stPasswd 		= "";
		mailselections	= "";
		classInfo		= "";
		szName  		= "";
		//
		active_i		= "-";
		active_k		= "-";
		number_i		= "-";
		number_k		= "-";
    }
    public Student(String _id,String _kname,String _email,String _keitai,String _stPasswd,String _mailselections,String _classInfo){
        if(LOG.fa) LOG.println("コンストラクタ　Student(String _id,String _kname,String _email,String _keitai,String _stPasswd,String _mailselections,String _classInfo)　の先頭です");
		
		id		= _id;
		kname	= 		  (((_kname  != null) 		  && (_kname.length()>0))  			? _kname  			: "-" );	// nul lや "" にしない
		email	= 		  (((_email  != null) 		  && (_email.length()>0))  			? _email  			: "-" );
		keitai	= 		  (((_keitai != null) 		  && (_keitai.length()>0)) 			? _keitai 			: "-" );
		stPasswd 		= (((_stPasswd != null) 	  && (_stPasswd.length()>0)) 		? _stPasswd 		: id  );
		mailselections	= (((_mailselections != null) && (_mailselections.length()>0)) 	? _mailselections 	: "*" );
		classInfo		= (((_classInfo != null) 	  && (_classInfo.length()>0)) 		? _classInfo 		: ""  );
		//
		// これは初期値では埋めないで、必要なときに設定する
		szName  		= "";
		//
		active_i		= "-";
		active_k		= "-";
		number_i		= "-";
		number_k		= "-";
    }
	//
    public Student(String _id,String _kname,String _email,String _keitai,String _stPasswd,String _mailselections,String _classInfo,
				   String _active_i,String _active_k,String _number_i,String _number_k){
        if(LOG.fa) LOG.println("全フィールドを取得するコンストラクタの先頭です");
		//
		//
		id		= _id;
		kname	= 		  (((_kname  != null) 		  && (_kname.length()>0))  			? _kname  			: "-" );	// nul lや "" にしない
		email	= 		  (((_email  != null) 		  && (_email.length()>0))  			? _email  			: "-" );
		keitai	= 		  (((_keitai != null) 		  && (_keitai.length()>0)) 			? _keitai 			: "-" );
		stPasswd 		= (((_stPasswd != null) 	  && (_stPasswd.length()>0)) 		? _stPasswd 		: id  );
		mailselections	= (((_mailselections != null) && (_mailselections.length()>0)) 	? _mailselections 	: "*" );
		classInfo		= (((_classInfo != null) 	  && (_classInfo.length()>0)) 		? _classInfo 		: ""  );
		//
		//
		szName  		= "";
		//
		active_i		= (((_active_i != null) 	  && (_active_i.length()>0)) 		? _active_i 		: ""  );
		active_k		= (((_active_k != null) 	  && (_active_k.length()>0)) 		? _active_k 		: ""  );
		number_i		= (((_number_i != null) 	  && (_number_i.length()>0)) 		? _number_i 		: ""  );
		number_k		= (((_number_k != null) 	  && (_number_k.length()>0)) 		? _number_k 		: ""  );
    }
	///////////////////////////////////////////////////////////////////////
	//
	//		 旧版から引き継いだメソッド
	//
	/////////////////////////////////////////////////////////////////////// 
	//
	// 新規登録
	int run_insert(String szDB,Database db){
		return db.insert(szDB,this);		
	}
	// 全メールレコードを更新する
	int run_updateAll(String szDB,Database db){
		return db.update(szDB,this,"ALLMAIL");
	}
	// emailを更新する
	int  run_updateEmail(String szDB,Database db){
		return db.update(szDB,this,"EMAIL");
	}
	// keitaiを更新する
	int run_updateKeitai(String szDB,Database db){
		return db.update(szDB,this,"KEITAI");
	}
	// mailselections を更新する
	int  run_updateMailselections(String szDB,Database db){
		return db.update(szDB,this,"MSEL");
	}
	// classInfo を更新する
	int  run_updateClassInfo(String szDB,Database db){
		return db.update(szDB,this,"CINFO");
	}
	// stPasswd を更新する
	int  run_updateStPasswd(String szDB,Database db){
		return db.update(szDB,this,"PASS");
	}
	// 削除
	int run_delete(String szDB,Database db){
		return db.delete(szDB,this);
	}


	public String getId() {
		return id;
	}
	public String getKname() {
		return kname;
	}
	public String getEmail() {
		return email;
	}
	public String getKeitai() {
		return keitai;
	}
	public String getStPasswd() {
		return stPasswd;
	}
	@Override
	public String toString() {
		return "Student [id=" + id() + ", kname=" + kname() + ", email=" + email() + ", keitai=" + keitai() + ", stPasswd=" + stPasswd() + "]";
	}
	
	
	
}