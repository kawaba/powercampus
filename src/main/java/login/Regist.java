/*
 * 作成日: 2004/12/17
 *
 * TODO
 */
package login;

import java.io.*;
import java.util.*;

import	tktools.*;
import database.*;
import framework.*;
import	cabinet.*;
import mailutil.MailUtil;


/**
 *
 *
 *　新規ユーザー登録
 *
 	#
	# ##################
	#   Regist
	# ##################
	#
	<program $login.Regist>
		<dispatch  html=Regist.html  number=20  class=login.Regist />
		<variable>
		  <receive   NUMBER />
		  <accept    CMD />
		  
		  <form      GROUP DOMAIN TMAIL PASSWORD TNAME THKANA shozoku url_t url_s />
		</variable>
	</program>
 */
public class Regist  extends SuperPlayer implements PCvar{

	/* ************ 
	 *  作業用変数
	 **************/
	/**
	 *  プログラムリターンコード 
	 */
	String		ret;
	/**
	 * 次の処理での表示モード 
	 */
	String		disp_mode;
	/**
	 *  処理分岐コード
	 */
	String 		cmd;

	/** ユーザーID発生用 */
    String 		numberFile; 
	
	/**
	 * データベースプール
	 */
	DbConnectionBroker	broker;
	/**
	 * ユーザー検索のためのクラス
	 */
	registDB			db;
	
	
	/**
	 * 作成したユーザーID
	 */
	String			teUid;
	
	public	Regist(){
		super();
		if(LOG.fa) LOG.println("■ Tlogin #コンストラクタ");
	}	

	/**
	 * コンストラクタが引数をもてないので、コンストラクタで行う内容をこの initialize() に書く．
	 * out, htb, para は SuperPlayer クラスのインスタンス変数なので、
	 * 引数にとらずとも利用できるが、変数を明示する意味で列挙してある
	 * 
	 * @param out			出力ポインタ
	 * @param htb			システムハッシュ
	 * @param para			パラメータ
	 */
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
    	
		broker	=	(DbConnectionBroker)(htb.get(BROKER));
    	db		=	new registDB(broker);

    	/*
    	 * 教師ユーザーID発生用ファイル．必ず作成して値１を書き込んでおく
    	 */
    	numberFile 		= Gear.strHash(htb, NUMBER_FILE);
    	

	}
	/**
	 * 受け入れコードにより処理を分岐させるメソッド．
	 * 
	 * 受け入れコードはシステムハッシュ htb から strHash(htb, DISPATCH_KEY); で受け取る
	 * 
	 * 終了コードとして次に起動したいプログラムエイリアス（論理プログラム名）を指定する
	 * 論理プログラム名は dispatch.xml に指定したものを使う
	 * 
	 * 自分自身の画面を再表示したい場合は終了コードに "SELF" を指定する
	 * 呼び出しプログラムへ復帰するには終了コードに "RETURN" を指定する
	 * 
	 * @return
	 */	
	public	String	dispatch(){
		if(LOG.fa) LOG.outHash(htb,"■■ BbsPost #dispatch()");
		/*
		 * 処理分岐
		 */
		cmd			=	Gear.strHash(htb,DISPATCH_KEY);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;				// WEB表示
		disp_mode	=	DISP_NEW;
		
		if(Gear.isEmpty(cmd)||cmd.equals("REGIST")){// 登録ボタン
			/*
			 *  データの正当性チェック
			 */
			if(isValid()){
			    /*
			     * IDを生成しその他のデータも生成してシステムハッシュに格納する
			     */
			    makeIDandData();
				/*
				 * データベースに登録しユーザー環境を作成する
				 * セッションは作成しないので，リターンしてもセッション切れとなり
				 * login 処理をコールすることになる
				 */
				if(regist()){					// 同じメールアドレスがすでにあればエラーで帰る
					makeUserDB();				// ユーザーDB作成
					ret	=	DISPATCH_RETURN;	// 呼び出し元へ復帰
					
				}else{
					disp_mode	=	DISP_EDIT;
					ret			=	DISPATCH_DEFAULT;	// 再表示
				}
				
			}else{
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;	// 再表示
			}
		
		}else	if(cmd.equals("CLEAR")){// クリアボタン
			disp_mode	=	DISP_NEW;
			ret			=	DISPATCH_DEFAULT;	// 再表示
			
		}else	if(cmd.equals("RETURN")){// クリアボタン
			disp_mode	=	DISP_NEW;
			ret			=	DISPATCH_RETURN;	// 戻る
			
		}else{
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示
			
		}
		/*
		 * 次に表示するときあるいはリターンしてきた時の表示モードをセットしてからリターンする
		 */
		htb.put(DISP_KEY,disp_mode);
		return	ret;
	}
	/**
	 * 登録データの正当性チェック
	 * @return
	 */
	public boolean isValid(){
		if(LOG.fa) LOG.outHash(htb,"Regist #isValid() : ★登録データの正当性チェック");

		String msg = isOK(); 
        htb.put("_msg",msg);
        if( msg.equals("ok") ){ 
			return true;
        }
		return	false;
	}
	/**
	 * ユーザー登録．<br>
	 * 重複登録はエラーとなる
	 * @return
	 */
	public	boolean regist(){
	
		int result = db.insert_regist(htb);	// ユーザー登録処理
		if(result==0){
			return false;
		}
		return	true;
	}
	/**
	 *	関連データベースを生成する（members, membersinfo,szDB）
	 *	membersとmembersinfoテーブルにエントリーを追加する
	 *	shozokuテーブルにエントリーがあるかどうか調べなければszDBテーブルを作成する
	 */	
	public void makeUserDB(){
		db.convertInfo(htb);
		String id = strHash(htb, GROUP);
		int 	n = db.exitShozoku(id);
		if(n==0){
			db.convertShozoku(htb);
		}
		//
		// 　ユーザーのエントリーを作成する．
		String	newID	=	strHash(htb,"_user_id");
		userAdd();
		//
		//  登録内容をメールでユーザーに送信する
		String	keyUser	= para.getSysadmin();
		String	from	= keyUser;
		String	to		= strHash(htb,"_teMail");
		
		String	title	= "★ PowerCampusのユーザー登録が完了しました";
		String	body	= "　新 規 登 録 者：" + Gear.strHash(htb,"_teName")	+ CR +
				  		  "　グ ル ー プ 名：" + Gear.strHash(htb,"_szDB")		+ CR + CR +
				          "　■ ユーザー名 ：" + Gear.strHash(htb,"_user_id")	+ CR +
				          "　■ パスワード ：" + Gear.strHash(htb,"_passwd")    + CR +
		  				  "　■ Ｅ－メール ：" + Gear.strHash(htb,TMAIL);
		
		MailUtil	ml	=	new	MailUtil();
		/*
		 * ★ 2005.9. NOTE
   　	 * Windows プラットフォームでは
         * サーブレットでのエンコードを "text/plain; charset=MS932" と指定すればCp932は不要
         * ただし、電子メールでは文字化けするので出力時のみに forJisMail() でJISに変換する
   　    *
   　    * Linux プラットフォームでは
         * サーブレットでのエンコードを "text/plain; charset=Shift-JIS" と指定してCp932を利用する
         * ただし、電子メールでは出力時には適用しない
         *
		 */
		if(Cp932.isCp932){
		    // Windowsシステムなら
		    ml.send_To_email(to, from, Cp932.forJisMail(title), Cp932.forJisMail(body), para);

		}else{
		    // linux なら
		    ml.send_To_email( to, from, title, body, para);
		    
		}
		return;
	}
	//
	//  登録データチェック
	//
	public	String	isOK(){
		//
        // 全項目に記入が必要
		//
	    String	uid				= strHash(htb,"_user_id");
	    String	teMail			= strHash(htb,"_teMail");
		String	passwd			= strHash(htb,"_passwd");
		String	teName			= strHash(htb,"_teName");
		String	hurigana		= strHash(htb,"_hurigana");
		String	shozoku			= strHash(htb,"_shozoku");
		//
		String	szDB			= strHash(htb,"_szDB");
		String	domain			= strHash(htb,"_domain");
		//
        if(Gear.isSpaceOrNull(uid))      	return "★ ユーザー名を記入してください";
        if(Gear.isSpaceOrNull(passwd))      return "★ パスワードを記入してください";

        if(Gear.isSpaceOrNull(teMail))      return "★ メールアドレスを記入してください";
        if(!Gear.isMailaddress(teMail))		return "★ メールアドレスの書き方に間違いがあります";
		if(passwd.length()<5)				return "★ パスワードは５文字以上の長さにしてください";
        if(Gear.isSpaceOrNull(teName))      return "★ 漢字で名前を記入してください";
        if(Gear.isSpaceOrNull(hurigana))    return "★ ふりがながを記入してください";
        if(Gear.isSpaceOrNull(shozoku))     return "★ 所属を記入してください";
        

		if(Gear.isSpaceOrNull(szDB))       	return "★ グループ名を記入してください";

		if(!StringGear.isSmallHankaku(uid))		return	"ユーザー名に使える文字は小文字の英字とアンダーバー（_）と数字です";
		if(!StringGear.isSmallHankaku( passwd))	return	"パスワードに使える文字は小文字の英字とアンダーバー（_）と数字です";
		
		if(!StringGear.isMailaddress(teMail))	return	"メールアドレスとして正しくありません";
		if(!StringGear.isSmallHankaku(szDB))	return	"グループ名に使える文字は小文字の英字とアンダーバー（_）と数字です";
		if(!StringGear.isDomain(domain))		return "ドメイン名として正しくありません";
		//
		/*
		 * 入力せず、初期値として設定することとした．
		 */
		String	url_t			= para.getInitTeacherURL();
		String	url_s			= para.getInitStudentURL();
		htb.put("_url_t",url_t);
		htb.put("_url_s",url_s);

        return "ok";
    }
	/**
	 * ユーザーIDの作成などを行い，登録のためにシステムハッシュに
	 * 格納しておく
	 */
	void	makeIDandData(){

	    /*
	     * ユーザー名は入力するように変更した 2005.9
	     * 
	    teUid = "";
		try{
			teUid	= getID();		// ８桁のユーザーIDを発生
		
		}catch(PCException e){
			PrintWriter out	= para.getResponseWriter();
			errPrint(out,e.getMessage());	// エラーがあるとここでプログラムは停止する
			return;
		}
		*/
		teUid	=	strHash(htb,"_user_id");
		htb.put("_teUid"  ,teUid);			// 後の処理で必要になるので
		//
		//  その他の必要なデータ項目もハッシュにセットしておく
		//
		htb.put("_user_active","1");		// 初期値
		String	note = para.getInitValue();	// 初期値をコンフィギュレーションファイルから読む
		htb.put("_note",note);
	}
	
	public	boolean isSystem(){
		if( (para.getSysadmin()).equals(Param.SYSTEM_ADMIN_ID) )	return	true;
		return false;
	}
	
	// 	変換された日付と連番からなるユーザーＩＤを生成して返す
	//  一日に999,999人登録可能．2028年まで有効.
	String getID() throws PCException{
		GregorianCalendar now = new GregorianCalendar();
		int yy = now.get(Calendar.YEAR) - 2004;
		int mm = now.get(Calendar.MONTH) + 1;
		int dd = now.get(Calendar.DATE);
		//
		String _yy = String.valueOf((char)('a' + yy));		// a ～ z   2028年まで使える
		String _mm = String.valueOf((char)('a' + mm));		// a ～ g
		String _dd = String.valueOf((char)('a' + dd - 1));	// a ～ z, 1 ～ 5
		if(dd > 26) { _dd = String.valueOf(dd-26); }
		//
		String s ="";
		try{
			s = _yy + _mm + _dd + getNumber();		// ８桁
		}catch(PCException e){
			throw e;
		}
		//
		return s;
	}
	// システムで管理する１から９９９９９までの数字文字列を返す　<DBを使わない>
	synchronized String getNumber() throws PCException {
		String fname = numberFile;
		int num = 0;
        try{
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fname)));
            String str;
			if( (str=in.readLine() ) != null){
				num	  = Integer.parseInt(( str ).trim());
				if(num > 99999) num = 0;
			}else{
				num   = 0;	// ファイルはあったが中身がなかった時
			}
			in.close();
		}catch(IOException e){
			num   = 0;	// ファイルがなかった場合など
		}
		//
		++num;
        try{
			PrintWriter pr 	  = new PrintWriter(new OutputStreamWriter(new FileOutputStream(fname)));
			pr.println(num);
			pr.close();
		}catch(IOException e){
			throw (new PCException ("class exwork #getNumber() : numberFile に書けない．" + e));
		}
		String	cnt		= String.valueOf(num);
		int 	pos		= cnt.length();
		String	pattern	= "00000" + cnt;
		return  pattern.substring(pos);
	}
	/**
	 * 基本のディレクトリがあるかどうか確認してなければ作成する
	 *
	 */
	void	initDirectory(){
	    
	    File	home	=	new	File( para.getHomedir() );
	    File	group	=	new	File( para.getGroupdir() );
	    File	bbs		=	new	File( para.getBbsdir() );
	    
	    
	    if(!home.exists()){
	        home.mkdirs();
	    }
	    if(!group.exists()){
	        group.mkdirs();
	    }
	    if(!bbs.exists()){
	        bbs.mkdirs();
	    }
	    
	}
	// 
	// 新規登録処理
	//  １．システムディレクトリの作成とテンプレートファイルのコピー
	//		解答ファイル保存用				answer
	//		クラスファイル一時保存用		temp
	//		クラスファイル保存用			classes
	//		採点結果ファイル保存用			meibo
	//		メール用差込ファイル保存用		mailtemp （2003.8 追加）
	//		この他に,file,zip,backup などのディレクトリも必要だが、実行時に作成される
	//		採点結果送信用テンプレートファイルをユーザーディレクトリにコピー
	//		
	//	２．
	//		
	//	３．ユーザーの学生データベースを作成する
	//		学生データベース（szDB）を作成し、さらに、shozoku テーブルでこの所属をアクティブ(=1)にする
	//		学生ノートデータベースを作成
	//      学生課題提出履歴データベースを作成
	//		
	//	４．ユーザーのキーセットをキーデータベース（KeyGen) に追加する
	//	
	//	※ 大学名ロゴはインストール先大学分は作成してあるが、他のグループ名を作成した場合は設置責任者が作成しなければならない
	//	
	boolean  userAdd(){
        if(LOG.fa) LOG.outHash(htb,"userAdd() の先頭です");
		//
		/*
		 * 基本のディレクトリがあるかどうか確認してなければ作成する
		 */
        initDirectory();
        
        
        Database	dbms	=	new Database(broker);
		dbms.MembersInfo(teUid,htb);				// ユーザー登録情報を得る
		String 	homedir = para.getHomedir();		// ユーザーホームの起点
		String 	userid 	= strHash(htb,"_user_id");
		//
		String 	temp  	= dbms.getUserInfo( userid);// 2003.6.3 訂正 データベースmembers テーブル から使用する学生DB名を求める
		Csv		cs      = new Csv (temp,"%");		// temp = DBname + "%" + teName + "%" + url_t + "%" + note である
		//
		String szDB		= cs.get(0);			// 学生データベース名
		String name     = cs.get(1);			// 教員氏名（ユーザー名）
		String url_t    = cs.get(2);			// 教員の戻りURL．
		String note     = cs.get(3);			// 設定情報　2003.7.1 追加 初期値は#（=規定値のまま）。デリミッタは$
		//
		// shozoku テーブルはかならずあるという前提
		// shozoku テーブル中にユーザーの所属データは必ず登録してあるという前提
		//
		// 1.ディレクトリがなければ作る。また必要なディレクトリとファイルも作成する
		//   この他に,file,zip,backup などのディレクトリも必要だが、実行時に作成される
		//
		String  path 	 = homedir + teUid + File.separator;
		File	dir		 = new File(path);
		if(!dir.isDirectory()){
			//
			if(LOG.fa) LOG.println("■Regist #userAdd() makedirs! ");
			dir.mkdirs();	// ユーザーディレクトリを作成
			//
			File fp1 = new File(path + para.getAnsDirName());		// 解答ファイル保存用			answer
			File fp2 = new File(path + para.getTempClassDirName()); // クラスファイル一時保存用		temp
			File fp3 = new File(path + para.getClassDirName());		// クラスファイル保存用			classes
			File fp4 = new File(path + para.getMeiboDirName());		// 採点結果ファイル保存用		meibo
			File fp5 = new File(path + para.getMailTempDirName());	// メール用差込ファイル保存用	mailtemp  2003.8 追加
			File fp6 = new File(path + para.getAnsTempDirName());	// 解答ファイル保存用			anstemp
			File fp7 = new File(path + para.getQDirName());			// アンケート集計結果ファイル   qdir
			File fp8 = new File(path + para.getKadaiDirName());		// 課題のグラフィックス保存用　 kadai

			fp1.mkdir();
			fp2.mkdir();
			fp3.mkdir();
			fp4.mkdir();
			fp5.mkdir();
			fp6.mkdir();
			fp7.mkdir();
			fp8.mkdir();
			//
			String sfile = para.GetDefaultTemplatePath();		// 採点結果送信用テンプレートをコピー
			String dfile = homedir + teUid + "/template.txt";
			boolean flag = Gear.copyFile(sfile,dfile);
			if(!flag) return false;
		}
		//
		// 2.  ユーザーのグループに関するデータベースを作成する
		//
		//   新規登録の場合、
		// 　そのデータベースがなければ作る
		//
		Csv rec = dbms.shozoku_search(szDB);
		if(rec==null){
			//
			// shozoku テーブル中にユーザーの所属データは必ず登録してあるという前提
			if(LOG.fa) LOG.println("★Regist #userAdd() :shozoku テーブル中にユーザーの所属データがない");
			return false;
		}else{
			// グループに関するデータベースを作らねばならないかどうか、shozoku テーブルを見て調べる
			// shozoku テーブルは 学生データベース名をキーとするレコードがあり、その中でフラグは初期値では 0(ノンアクティブ) である
			String szActive = rec.get(1);
			if(szActive.equals("0")) {
				if(LOG.fa) LOG.println("■Regist #userAdd() make Databases ");

				dbms.createTable(szDB);				// アクティブになっていなければ 学生データベース を作成する
				//
				dbms.createNote(szDB);				// 2003.8 追加　学生のノート
				dbms.create_Note_Idx(szDB);			// 同 インデックス作成
				//
				dbms.create_KadaiInfo(szDB);		// 2004.3 追加　学生の課題提出履歴
				dbms.create_KadaiInfo_Idx(szDB);	// 同 インデックス作成
				//
				/*
				 * jbbs 関連データベース
				 */
				dbms.create_BbsForum(szDB);
				dbms.create_BbsThread(szDB);
				dbms.create_BbsPost(szDB);
				dbms.create_bbsInfo(szDB);
				dbms.create_bbslog(szDB);
				dbms.create_jbbskes(szDB);
				/*
				 * ファイルキャビネット関連データベース
				 */
				CBdatabase cb	=	new CBdatabase(broker, szDB);
				cb.createCabinetDatatables();
				/*
				 * シラバス定義XMLファイルのコピー
				 */
				copySyllabusData();
				/*
				 * さらに、shozoku でこの所属をアクティブにする
				 */
				dbms.setAccountToActive(szDB,"1");
			}
		}
		//
		// 3.ユーザーのキーデータベースを生成する
		//
		/*
			create table key_gen (
    			teUid           CHAR(8)  PRIMARY KEY,
    			lec             CHAR(3)  DEFAULT '001',
				aplec           CHAR(3)  DEFAULT '001',
    			sect            CHAR(5)  DEFAULT '00001',
    			kadai           CHAR(6)  DEFAULT '000001',
    			ref             CHAR(6)  DEFAULT '000001'
				faq             CHAR(6)  DEFAULT '000001'
			); 		
		*/
		Vector keySet = new Vector(10,10);
		keySet.add( teUid );		// 0
		keySet.add("001");			// 1
		keySet.add("001");			// 2
		keySet.add("00001");		// 3
		keySet.add("000001");		// 4
		keySet.add("000001");		// 5
		keySet.add("000001");		// 6
		//
		dbms.insertKeyGen( keySet );	// キーセットデータベースにユーザーのレコードを登録する
		//
		/*
		 * pc.conf のグループ名初期値を更新しておく
		 */
		setInitialGroupName(szDB);
		/*
		 * 再起動しなくても変更が反映されるように 
		 * Param のメンバーデータ info: prop の要素を書き換えておく
		 * put は synchronized メソッド
		 */
		para.put("SHOZOKU_DB", szDB);
		/*
		 * タイトルグラフックスの作成
		 * 一時的なグラフィックス。カスタマイズはユーザー。
		 */
		makeTitleGraphics(szDB);
		
		return true;
	}
	/**
	 * タイトルグラフックスの作成
	 *
	 */
	void	makeTitleGraphics(String	initDBname){
	    
	    String	sample	=	para.getTitleLogo();						// サンプルロゴへのフルパス
	    String	logo	=	para.getLogodir() + initDBname + ".gif";	// ロゴファイル
	    try {
            FileGear.copyBinryFile(sample, logo);
        } catch (IOException e) {
            LOG.println("■makeTitleGraphics");
            LOG.println(e.getMessage());
            LOG.println("from:" + sample);
            LOG.println("to  :" + logo);
  
            
        }
	    
	}
	/**
	 * pc.conf のグループ名初期値を更新する
	 * @param initDBname
	 */
	void	setInitialGroupName(String initDBname){
	    
	    String	conf	=	para.sysConfFile();
	    String	temp	=	conf + ".temp";
	    String	old		=	conf + ".old";
	    
	    try {
            BufferedReader	in		=	new	BufferedReader(new FileReader( conf ));
            PrintWriter		out		=	new	PrintWriter(temp);
            String			line;
            while((line=in.readLine())!=null){
                
                /* SHOZOKU_DB だけを書き換える */
                out.println(modifyGroup(line, initDBname));

            }
            in.close();
            out.close();
            
        } catch (Exception e) {
        }	    
	    /*
	     * ファイル名を付け替える
	     */
	    File	oldFp	=	new	File(old);
	    if(oldFp.exists()){
	        oldFp.delete();
	    }
	    FileGear.rename(conf, old);
	    FileGear.rename(temp, conf);
	    
	}
	
	String	modifyGroup(String line, String initDBname){
	    
	    String	ret	=	line;
	    Csv		cs	=	new	Csv(line, "=");
	    if(cs.get(0).equals("SHOZOKU_DB")){
	        ret	=	"SHOZOKU_DB    = " +  initDBname; 
	    }
	    return	ret;
	}	
	/**
	 * シラバス定義XMLデータがない場合はオリジナルからコピーする
	 * また、雛形をファイルキャビネットに登録する
	 */
	public void	copySyllabusData(){
		/*
		 * パラメータ取得
		 */
		String		szDB	=	Gear.strHash(htb,GROUP);
		String		id		=	Gear.strHash(htb,TUID);
		String		fname	=	"syllabus.xml";
		String		subject	=	"科目定義ファイルの雛形";
		if(LOG.fa){
			LOG.println("■PowerCampus #copySyllabusData()");
			LOG.println("	szDB-----"	+ szDB);
			LOG.println("	id ------" 	+ id);
			LOG.println("	fname ---" 	+ fname);
			LOG.println("	subject -" 	+ subject);
		}
		/*
		 * グループの科目定義ファイルがあるかどうか調べる
		 * 
		 *  dat = /var/pc/data/○○○.xml
		 * 
		 */
		String	dst			=	para.groupSyllbusPath(szDB);
		File	dstFp		=	new	File(dst);
		/*
		 * キャビネット用のディレクトリを作成する
		 */
		File	cabinetdirFP	=	new File(para.fileCabinetPath(szDB, id));
		cabinetdirFP.mkdirs();
		/*
		 * キャビネットに科目定義xmlファイルを登録する
		 */
		String	cabinet		=	para.fileCabinetPath(szDB, id) + fname;
		String	src			=	para.groupSyllbusPath("TEMPLATE");
		if(LOG.fa){
			LOG.println("");
			LOG.println("	cabinet --- "	+ cabinet);
			LOG.println("	dst ------- "	+ dst);
			LOG.println("	src -------"	+ src);
		}

		
		File	fp	=	new	File(dst);
		if(fp.exists())		return;	
		/*
		 * 定義ファイルが無いのでコピーする
		 * 	1.グループの科目定義ファイル
		 * 	2.個人の科目定義ファイルの雛形（ファイルキャビネット）
		 * 
		 */
		try{
			FileGear.copyBinryFile(src, dst);
			FileGear.copyBinryFile(src, cabinet);
		
		}catch(IOException e){
			e.printStackTrace();
		}
		/*
		 * 個人用の雛形をファイルキャビネットに登録する
		 * 
		 */
		CBdatabase	cb		=	new	CBdatabase((DbConnectionBroker)htb.get(BROKER), Gear.strHash(htb,GROUP));
		cb.regist(id, fname, subject, para);

		
	}

	/*////////////////////////////////////////////////////////////////////////////
	 * 
	 *    表 示 処 理
	 * 
	 * 　　    以下はコントローラーが呼び出す表示メソッドである．
	 * 　　    一般には、このクラス内のメソッドから直接呼び出さない．
	 * 
	 *////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 出力処理
	 */
	public  void	display(boolean editmode){
		if(LOG.fa)	LOG.println("■ Regist #display(boolean editmode)");
		
		if(!editmode){
			htb.put(MESSAGE,"");
			clearScreen(htb, para);
			
		}
		
		/* strHash(htb,DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v,htb);		
	}
    /**
     * ユーザー登録ページの初期値を設定する
     * @param _htb
     * @param para
     * @return
     */
    Hashtable  clearScreen(Hashtable _htb,Param para) {
    	if(LOG.fa) LOG.println("class exwork #clearScreen() : ユーザー登録ページの初期値を設定する の先頭です");
        
        _htb.put("_msg","");
        _htb.put("_teMail","");
        _htb.put("_user_id","");
        
        String	initszDB = para.getInitDB();
        if(initszDB.equals("dummy")){
            initszDB	=	"";
        }
        _htb.put("_szDB",initszDB);	// pc.conf で設定した初期値
        
        _htb.put("_passwd","");
        _htb.put("_teName","");
        _htb.put("_hurigana","");
        _htb.put("_shozoku","");
        _htb.put("_url_t",para.getInitTeacherURL());// pc.conf で設定した初期値
        _htb.put("_url_s",para.getInitStudentURL());	
		
       	_htb.put("_bunnya","-");					// 設定しない
       	_htb.put("_address","-");					// 設定しない
		//
        return  _htb;
    }	



}
