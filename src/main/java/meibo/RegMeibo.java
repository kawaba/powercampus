package meibo;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import cabinet.CBdatabase;
import cabinet.CBvar;
import database.Database;
import database.DbConnectionBroker;
import database.KeyGen;
import framework.LOG;
import framework.Param;
import framework.SuperPlayer;
import framework.SuperPrint;
import kamoku.KamokuApRecord;
import mailutil_service.Recipient;
import mailutil_service.Util;
import student.Student;
import tktools.Csv;
import tktools.FileGear;
import tktools.Gear;
/**
* クラス名簿を登録する
*
	#
	# ##################
	#     RegMeibo
	# ##################
	#
	<program $meibo.RegMeibo>
		<dispatch  html=RegMeibo.html  number=6010  class=meibo.RegMeibo />
		<variable>
 		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA DIVISION  title lec_key aplec_key />
		  <accept    CMD    UPLODE StUID />
		  <keep      />
		  
		  <form      />
		</variable>
	</program> 
*
*
* 変数の説明
*
* 1. receive 
* 2. accept
* 		StUID ----- 登録情報を送信するときその学生ID
* 3. keep
* 4. form
*
* 
*/
public class RegMeibo extends SuperPlayer  implements CBvar{

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
	/**
	 * データベースプール
	 */
	DbConnectionBroker	broker;
	/**
	 * ユーザー検索のためのクラス
	 */
	Database	db;
	
	String  	teUid;
	String  	aplec_key;
	String  	szDB;
	
	/**
	 * 
	 *
	 */
	public	RegMeibo(){
		super();
		if(LOG.fa) LOG.println("■ Sample #コンストラクタ");
	}
	/**
	 * 
	 */
	@Override
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		broker	=	getDbConnection();
		db		=	new Database(broker);	
		//
		szDB		= getParameter(GROUP);
		teUid		= getParameter(TUID);
		aplec_key	= strHash(htb,"_aplec_key");
		
    }
    //
	@Override
	public	String	dispatch(){
		if(LOG.fa) LOG.outHash(htb,"■Sample #dispatch()");
		if(LOG.fa) LOG.println("■Sample #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		// 
		if(cmd.equals("UPDATE")){
			/*
			 * 名簿登録
			 */
			updateMeibo();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;			
			
		}else if(cmd.equals("SEND")){
			/*
			 * 学生にパスワード送信
			 */
			forStudent(htb,para);
			String	msg	=	"☆ 指定の学生に登録情報を送信しました";
			htb.put("_msg", msg);
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;			
			
		}else if(cmd.equals("DOWNLOAD")){
			/*
			 * 名簿をダウンロードする
			 */
			download();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;			
			
		}else if(cmd.equals("CABINET")){
			/*
			 * ファイルキャビネットを開く
			 */
			disp_mode	=	DISP_NEW;
			ret			=	"$cabinet.FileCabinet";			
			
		}else if(cmd.equals("RETURN")){
			/*
			 * 書き込まずに終了
			 */
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;			

		}else if(cmd.equals("ERROR_FILE")){

			String	msg	=	"";
			htb.put("_msg", msg);
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;			
			
		}else{
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;			
		}		
		/*
		 * 次に表示するときあるいはリターンしてきた時の表示モードをセットしてからリターンする
		 */
		putParameter(DISP_KEY,disp_mode);
		return	ret;
	}
	/**
	 * 名簿をダウンロードする
	 *
	 * 現在の名簿をファイルキャビネットに登録してダウンロードできるようにする
	 * ファイル名は英字のmeiboに変更する
	 */
	void	download(){
		
		/*
		 * 名簿の有無を確かめて、ファイルキャビネットパスに名簿ファイルをコピーする
		 */
		KamokuApRecord	kar 	= 	new KamokuApRecord(teUid,aplec_key,db);
		boolean		copyOK	=	copyFile(kar);
		/*
		 * ファイルキャビネットデータベースに登録する
		 * 更新はデータベースに変更がないのでregistはファイルの重複を調べた上、
		 * 新規の場合のみ尊くを行なう
		 */
		if(copyOK){
			CBdatabase	cbd	=	new	CBdatabase(broker, szDB);
			cbd.regist(teUid, meiboFileName(), subject(), para);
			putParameter(MESSAGE, "★ファイルキャビネットに名簿を登録しました");
			
		}else{
			putParameter(MESSAGE, "★名簿ファイルを取得できません");
		}
	}
	/**
	 * 同名のファイルがすでにファイルキャビネットにないかどうか
	 * @return　あればtrue
	 */
	boolean	isDupe(){
		String		cabinetFilePath	=	filePath() + meiboFileName();
		File		fp				=	new	File(cabinetFilePath);
		return		fp.exists();
		
	}
	/**
	 * 名簿の有無を確かめて、ファイルキャビネットパスに名簿ファイルをコピーする
	 * @param kar
	 * @return
	 */
	boolean	copyFile(KamokuApRecord	kar){
		Meibo			mb	= kar.getMeibo(para);
		if(mb==null){
			/*
			 * 名簿未登録
			 */
			putParameter(MESSAGE, "★名簿が登録されていません");
			return	false;
		}
		String		cabinetFilePath	=	filePath() + meiboFileName();
		try{
			FileGear.copyBinryFile(kar.getMeiboPath(para), cabinetFilePath);
			
		}catch(IOException e){
			return	false;
			//e.printStackTrace();
		}
		return	true;
	}
	/**
	 * ファイルキャビネットにおける絶対ファイルパスを返す
	 * @param userid
	 * @param fname
	 * @return
	 */
	String	filePath(){
		String	path	=	para.fileCabinetPath(getParameter(GROUP), teUid);
		return	path;
		
	}
	/**
	 * ファイルキャビネットにおけるファイルURLを返す
	 * @param userid
	 * @param fname
	 * @return
	 */
	String	fileURL(){
		String	url	=	para.fileCabinetURL(getParameter(GROUP), teUid) + meiboFileName();
		return	url;
	}
	/**
	 * 名簿データの件名を作成して返す
	 * @return
	 */
	String	subject(){
		String	aplec_key 	= 	getParameter("aplec_key");
		String	wdateString	= 	KeyGen.wdateTypeA(aplec_key);
		String	title		=	getParameter("title");
		String	subject		=	title + "（" + wdateString +"）" + "のクラス名簿";
		return	subject;

	}
	/**
	 * 名簿ファイル名を作成して返す
	 * @return
	 */
	String	meiboFileName(){
		String	fname	=	"meibo-" + getParameter("lec_key") + "-" +  getParameter("aplec_key") + ".csv";
		return	fname;
	}
	
	
	/**
	 * 名簿を登録・更新する
	 */
	void	updateMeibo(){
		
		// 実施レコード
		/*
		 * 科目実施レコードから名簿を取得して、なければ新規とする
		 */
		KamokuApRecord	kar = new KamokuApRecord(teUid,aplec_key,db);
		Meibo	mb			= kar.getMeibo(para);
		boolean flag = false;
		if(mb==null){
			insert(kar);
			
		}else{
			update(kar);
		}
		/*
		 * 以上で
		 */
	}
	void	insert(KamokuApRecord kar){
		/*
		 * 名簿ファイル新規受け取り
		 */
		boolean	flag = kar.addMeibo(out, htb, para);
		if(flag){
			/*
			 * ファイル名が確定したので名簿オブジェクトを作成する
			 * ファイル内容の正当性はkar.addMeibo(out, htb, para)でチェック済み
			 * e-mail,学籍番号,氏名,keitai  の４項目からなる（空欄には"－"が入っている）
			 */
			Meibo newMeibo	= new Meibo( kar.getMeiboPath( para)); 
			updateDatabase(out,htb, para, null, newMeibo, null);
			
		}else{
			/*
			 * エラーメッセージはすでに kar.addMeibo(out, htb, para) でセットされているのでなにもしない
			 */
			return;
		}
	}
	/**
	 * 
	 * @param kar
	 */
	void	update(KamokuApRecord kar){
		
		boolean	flag = kar.updateMeibo(out,htb, para);
		Meibo m1 = kar.getM1();// なくなった学生データ
		Meibo m2 = kar.getM2();// 追加された学生データ
		Meibo m3 = kar.getM3();// 変更された学生データ
		if(flag){
			/*
			 * 更新モードでデータベースを更新する
			 */ 
			updateDatabase(out,htb, para,m1,m2,m3);
			
		}else{
			/*
			 * エラーメッセージはすでに kar.addMeibo(out, htb, para) でセットされているのでなにもしない
			 */
			return;

		}
	}
	//
	//
    // 新しく追加・更新したクラスデータから、メールアドレスデータベースを更新する
	//
	//　クラス名簿ファイルには、メールアドレス、番号、氏名、ケータイ　
	//  の４項目のみが存在することを前提している。
	//　データベースにはもっと多くの項目があるがそれらの変更はこの処理では扱わない
	//
	//  Meibo クラスの deleted,added,modified は更新する名簿データ
	//  新規登録は added に含まれる
	//  null か size() ==0 の場合は処理をバイパスすること
	//
	//	Meibo には e-mail,学籍番号,氏名,keitai  の４項目からなる（空欄には"－"が入っている
	//
	//
	//
	void updateDatabase(PrintWriter out,Hashtable htb,Param para,Meibo deleted,Meibo added,Meibo modified){
        if(LOG.fa)  LOG.outHash(htb,"class RegMail #updateDatabase() の先頭です");
		//
		String	te_aplec_key	= KeyGen.get_te_aplec_key(htb);	// 必ずこれを使う
		String  classinfo 		= te_aplec_key;	// 教員コード＋クラスキー
		//
		// deleted の処理 --- 直前のクラス名簿にあったが新名簿では削除された学生
		//                    データベースの classinfo から teUid-aplec_key を抹消しておかねばならない
		//                    もしも、classinfo が０件になればレコードも削除する
		//                    データベース中には、必ず存在するはずだが、他のクラスの処理で削除されてしまっている可能性もある
		//                    削除されてしまっていれば、特に何もしなくて良い
		//
		if(deleted != null){
			if(LOG.fa)  LOG.println("class RegMail #updateDatabase(): 削除されたデータの処理を行います");
			//
			if(LOG.fa){
				LOG.println("");
				LOG.println("# 削除実行　名簿リスト #---------------");
				for(int k=0; k<deleted.size(); k++){
					LOG.println( "      (" + String.valueOf(k) + ") " +(deleted.get(k)).toCSV() );
				}
			}
			//
			for(int i=0; i<deleted.getCounts(); i++){
				String stNumber = deleted.getNumber(i);
				Student st 		= new Student(szDB,stNumber,db);// データベースから学籍番号でデータを引いてみる
				//
				// 他のクラスの処理のためにデータベースにない可能性もあるので対処しておく
				if(!(st.isEmptyRecord())){ // 空ではない
					String  classinfoOld = st.classInfo();							// 更新前の classinfo
					String  classinfoNew = delInfo(classinfoOld,classinfo);			// classinfoOld から classinfo を取り去る
					//
					// どのクラスにも所属していない状況ならレコード自体を抹消する
					// （classinfo が無しになればデータそのものを削除する）
					if(classinfoNew == null){
						st.delete();	// レコードを削除する
					}else{
						st.set_classInfo(classinfoNew);
						st.update(); // レコードを更新する
					}
				}
			}
		}
		// modified の処理 --- 直前のクラス名簿にもあったが新名簿では内容が変更されている学生
		//                     メールアドレス、ケータイ、漢字氏名をこのデータで置き換える
		//                     空欄になっているものは置き換えずにそのままとする
		//                     メールアド、ケータイで "*" があれば、記載を抹消する
		//　　　　　　　　　　 データベース中には、必ず存在するはずだが、他のクラスの処理で削除されてしまっている可能性もある。
		//                     削除されてしまっていれば、特に何もしなくて良い
		//
		if(modified != null){
			if(LOG.fa)  LOG.println("class RegMail #updateDatabase(): 変更されたデータの処理を行います");
			//
			if(LOG.fa){
				LOG.println("");
				LOG.println("# 更新実行　名簿リスト #---------------");
				for(int k=0; k<modified.size(); k++){
					LOG.println( "      (" + String.valueOf(k) + ") " +(modified.get(k)).toCSV() );
				}
			}
			//
			for(int i=0; i<modified.getCounts(); i++){
				// 新しいデータ
				String stNumber = modified.getNumber(i);
				String kname	= modified.getName(i);
	           	String email	= modified.getMailAd(i);
				String keitai	= modified.getKeitai(i);
				//
				if(isEmpty(email))	email	= "-";
				if(isEmpty(keitai))	keitai	= "-";
				//
				// 他のクラスの処理のために既にデータベースにある可能性もあるので対処
				Student	st	= new Student(szDB,stNumber,db);// データベースから学籍番号でデータを引いてみる
				//
				// 他のクラスの処理のためにデータベースから抹消されている可能性もあるので対処しておく
				if(st.isEmptyRecord()){
					//
					// 空レコードの場合、フィールドには初期値がすでにセットされているのでnullのフィールドはない.
					// 空レコードではデータベースから検索したときにパスワードは id しているが念のためここでもそうする
					// 新しい値をセットする
					st.set_kname(kname);			// 漢字氏名
					st.set_email(email);			// 電子メール
					st.set_keitai(keitai);			// ケータイ
					st.set_stPasswd(stNumber);		// 初期パスワードとして学籍番号を登録する
					st.set_classInfo(te_aplec_key);	// この講義のキーコード
					//
					if(LOG.fa)  st.DBG_print("第( "  + String.valueOf(i+1) +  " ) 件目の更新した学生データです.DBに登録します");
					st.insert();
					//
				}else{
					// 新しいデータが空欄だった場合、従来の値（検索結果）を宛てる
					if( !( kname.equals("-"))  && !((kname.trim()).equals("")) )	st.set_kname(kname);	// 新しい値が空でないときデータベースにセットする
					if( !( email.equals("-"))  && !((email.trim()).equals("")) )	st.set_email(email);	// 同上
					if( !( keitai.equals("-")) && !((keitai.trim()).equals("")))	st.set_keitai(keitai);	// 同上
					//
					if( (email.trim()).equals("*")  )  email  = "-";  // メールアドレスでは "*" が指定されていれば削除とみなす
					if( (keitai.trim()).equals("*") )  keitai = "-";  // 同上
					//
					//String  classinfoOld = st.classInfo();							// 更新前の classinfo
					//String  classinfoNew = classinfoOld + "," + aplec_key;			// classinfoOld に aplec_key を追加する
					//st.set_classInfo(classinfoNew);
					//
					if(LOG.fa) st.DBG_print("class RegMail #updateDatabase() : 新規データの登録");
					//
					//メールレコードの更新をかける
					st.update();
				}
			}
		}
		// added の処理 --- 全くの新規クラスの名簿全体か、あるいは訂正で直前の名簿に追加された学生
		//                  単にそのままデータベースに追加する
		//                  データベース中に既に存在する場合は modified と同じ処理をする
		if(added != null){
			if(LOG.fa)  LOG.println("class RegMail #updateDatabase(): 新規のデータの処理を行います");
			//
			if(LOG.fa){
				LOG.println("");
				LOG.println("# 新規実行　名簿リスト #---------------");
				for(int k=0; k<added.size(); k++){
					LOG.println( "      (" + String.valueOf(k) + ") " +(added.get(k)).toCSV() );
				}
			}
			//
			for(int i=0; i<added.getCounts(); i++){
				//
				// 新しいデータ（入力データ）
				String stNumber = added.getNumber(i);	// 学籍番号
				String kname	= added.getName(i);		// 漢字氏名
	           	String email	= added.getMailAd(i);	// 電子メール
				String keitai	= added.getKeitai(i);	// ケータイ
				//
				if(isEmpty(email))	email	= "-";
				if(isEmpty(keitai))	keitai	= "-";
				//
				// 他のクラスの処理のために既にデータベースにある可能性もあるので対処
				Student	st	= new Student(szDB,stNumber,db);// データベースから学籍番号でデータを引いてみる
				//
				if(st.isEmptyRecord()){	// レコードは空
					//
					// 空レコードの場合、フィールドには初期値がすでにセットされているのでnullのフィールドはない.
					// 空レコードではデータベースから検索したときにパスワードは id しているが念のためここでもそうする
					// 新しい値をセットする
					st.set_kname(kname);			// 漢字氏名
					st.set_email(email);			// 電子メール
					st.set_keitai(keitai);			// ケータイ
					st.set_stPasswd(stNumber);		// 初期パスワードとして学籍番号を登録する
					st.set_classInfo(te_aplec_key);	// この講義のキーコード
					//
					if(LOG.fa)  st.DBG_print("第( "  + String.valueOf(i+1) +  " ) 件目の更新した学生データです.DBに登録します");
					st.insert();
					//
				}else{
					// 新しいデータが空欄だった場合、従来の値（検索結果）を宛てる
					if( !( kname.equals("-"))  && !((kname.trim()).equals("")) )	st.set_kname(kname);	// 新しい値が空でないときデータベースにセットする
					if( !( email.equals("-"))  && !((email.trim()).equals("")) )	st.set_email(email);	// 同上
					if( !( keitai.equals("-")) && !((keitai.trim()).equals("")))	st.set_keitai(keitai);	// 同上
					//
					if( (email.trim()).equals("*")  )  email  = "-";  // メールアドレスでは "*" が指定されていれば削除とみなす
					if( (keitai.trim()).equals("*") )  keitai = "-";  // 同上
					//
					/*
					 * 教師が学生としてログインするIDの場合、classInfoOld == "-" のケースがある．
					 * （このIDは、教師時間割表示処理において、なければ自動的にszDBに追加される）
					 * 
					 * これをそのままにしておくといけないので、チェックして取り除く
					 */
					String  classinfoOld 	= 	st.classInfo();							// 更新前の classinfo
					String  classinfoNew	=	"";
					if(isEmptyData(classinfoOld) || (classinfoOld.trim()).equals("-")){
					    classinfoNew	=	te_aplec_key;
					    
					}else{
					    classinfoNew = classinfoOld + "," + te_aplec_key;
					    
					}
					st.set_classInfo(classinfoNew);
					//
					if(LOG.fa) st.DBG_print("class RegMail #updateDatabase() : 新規データの登録");
					//
					//メールレコードの更新をかける
					st.update();
				}
			}
		}
	}
	//
	// ★名簿削除のために削除データの処理を上から抜き出したもの
	//
	// 与えられた名簿オブジェクトにより現在のデータベースの学生の受講情報を削除する
	//
	void  deleteUpdate(Meibo deleted,String te_aplec_key,String szDB){
		if(LOG.fa) LOG.println("class exwork #deleteUpdate() : 与えられた名簿オブジェクトにより現在のデータベースの学生の受講情報を削除する の先頭です");
		//
		int n = deleted.getCounts();
		if(LOG.fa) LOG.println( "      [" + n + "] 件の学生情報を処理します");
		//
		for(int i=0; i<n; i++){
			String stNumber = deleted.getNumber(i);
			Student st 		= new Student(szDB,stNumber,db);// データベースから学籍番号でデータを引いてみる
			//
			// 他のクラスの処理のためにデータベースにない可能性もあるので対処しておく
			if(!(st.isEmptyRecord())){ // 空ではない
				String  classinfoOld = st.classInfo();							// 更新前の classinfo
				String  classinfoNew = delInfo(classinfoOld,te_aplec_key);		// classinfoOld から te_aplec_key を取り去る
				//
				if(LOG.fa) LOG.println("           削除前 = " + classinfoOld);
				if(LOG.fa) LOG.println("           削除後 = " + classinfoNew);
				//
				// どのクラスにも所属していない状況ならレコード自体を抹消する
				// （classinfo が無しになればデータそのものを削除する）
				if(classinfoNew == null){
					st.delete();	// レコードを削除する
					if(LOG.fa) LOG.println("           学生 " + stNumber + "を削除しました");
				}else{
					st.set_classInfo(classinfoNew);
					st.update(); // レコードを更新する
				}
			}
		}
	}
	//
	// classinfoOld から classinfo を取り去る
	String delInfo(String classinfoOld, String classinfo){
		//
		Csv 		 oldM 	= new Csv(classinfoOld);
		StringBuffer bf 	= new StringBuffer(1000);
		boolean     flag 	= false;
		//
		for(int i=0; i<oldM.size(); i++){
			String info = oldM.get(i);
			if( !info.equals(classinfo) ){
				if(flag){
					bf.append(",");
				}
				bf.append(info);
				flag = true;
			}
		}
		if(LOG.fa){
			System.out.println();
			System.out.println("### exwk/delInfo() ---- flag がfalseならnullが返る");
			System.out.println("1.flag = " + flag);
			System.out.println("2.buff = " + bf.toString());
		}
		
		if(flag){
			String  ret = bf.toString();
			return  ret;
		}
		return null;
	}	
    // 
    // 学生に登録情報を通知する
    // 
    public void forStudent(Hashtable htb,Param para){

		String	teUid      	= (String)htb.get("_teUid");
		db.MembersInfo(teUid, htb);
		String 	teName 		= Gear.strHash(htb, "_user_name");
		String 	teMail		= Gear.strHash(htb, "_teMail");
		//
		String	stNumber	= strHash(htb,"_stNumber");
		Student	st 			= new Student(szDB,stNumber,db);
		if(st.isEmptyRecord()){
			LOG.println("class exwork #forStudent() : ★ 学籍番号でデータベースを引けませんでした");// ありえないが
			return;
		}
		String stName	= st.kname();
		String stMail	= st.email();
		String stKeitai	= st.keitai();
		String stPasswd	= st.stPasswd();
		//
		htb.put("_teName",teName);
		htb.put("_teMail",teMail);
		//
		htb.put("_stName",stName);
		htb.put("_stNumber",stNumber);
		htb.put("_stPasswd",stPasswd);
		htb.put("_stMail",stMail);
		htb.put("_stKeitai",stKeitai);
		//
        String from     = teMail;				// 担当教員のメールアドレス
        String title    = "[登録情報の通知]";	// メールタイトル
		//
		// e-mail送信
		String to	= st.email();
		if(!to.equals(Student.EMPTY)){							// 登録があれば送信
			
			String template = para.stInfoTempPath();
			String msg  	= mktemplate(template,htb,para);
			
			/* ***********************************************************
			//sendInfo(host,to,from,title,msg,para,true);
			メールサービスを利用するように変更した(2021.4.1)
			************************************************************** */			
			List<Recipient> ls = Util.createToList(st.id(), to, Util.ms932_utf8(stName));
			Util.sendMailService(from, Util.ms932_utf8(title), "TO", Util.ms932_utf8(msg), ls);
			
			/* ************************************************************ */
		}
		// 携帯メール送信
		to = st.keitai();
		if(!to.equals(Student.EMPTY)){							// 登録があれば送信
			String template = para.stInfoTempPath();
			String msg  	= mktemplate(template,htb,para);
			
			/* ***********************************************************
			//sendInfo(host,to,from,title,msg,para,true);
			メールサービスを利用するように変更した(2021.4.1)
			************************************************************** */			
			
			List<Recipient> ls = Util.createToList(st.id(), to, Util.ms932_utf8(stName));
			Util.sendMailService(from, Util.ms932_utf8(title), "TO", Util.ms932_utf8(msg), ls);
			
			/* ************************************************************ */			
		}
    }
	
    /*
    //
	// pop-before-smtp 認証を確認して１件のメールを送信する
	//
	public void sendInfo(String host,String to,String from,String title,String msg,Param para,boolean email){
        // 必要なら pop before SMTP のためにシステムユーザー名で認証を受けておく
        String pop = para.popBeforSmtp();
        if(pop.equals("yes")){
			String  user    = "";                                    // POP user名
            String  passwd  = "";                                // pop パスワード
            if(email){
				user    = para.getMailmaster();                                    // POP user名
            	passwd  = para.getMailmasterPass();                                // pop パスワード
            }else{
				user    = para.getMailmaster_k();                                    // POP user名
    	        passwd  = para.getMailmasterPass_k();                                // pop パスワード
			}
			boolean auth    = jmAuthenticate.authenticate(host,user,passwd);          // チェック
            if(!auth){
                String s1 = "★pop before smtp で認証に失敗しました。UID/PASSWD を確認してください。<P>";
                String s2 = "   host=" + host + "<br>";
                String s3 = "   user=" + user + "<br>";
                String s4 = "   passwd=" + passwd + "<br>";
				String s5 = "   auth=" + auth + "<br>";
                errPrint(para.getResponseWriter(),s1 + s2 + s3 + s5);	// s4 は出さない
                return;
            }
        }
        try{
            jmSender.send(host,to,from,title,msg);
        }catch(Exception e){
            System.out.println(to + "へのメール送信に失敗しました (" + getDate() + "）");
            System.out.println( e );
        }
	}
	*/
	
	//
	// テンプレートにパラメータを埋め込んで送信本文を作る
	String mktemplate(String path,Hashtable htb,Param para){
        //String template = para.stInfoTempPath();
        String 			template 	= path;							// 送信文テンプレートへのパス
		StringWriter 	strOut 		= new StringWriter(5000);
        PrintWriter 	sout     	= new PrintWriter( strOut );
        //
  		SuperPrint		sp			= new SuperPrint(sout);			// 2004.3 SuperPrint を使う方式に改めた
        sp.repPrint(template,htb,false);    // メールなので false 
        //
        String msg = strOut.toString(); 	// 編集された受領確認用送信メッセージ
		return msg;
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
	 * 画面を表示する
	 * 
	 * @param	editmode	表示モード（true なら編集、falseなら新規）
	 */
	@Override
	public  void	display(boolean editmode){
		if(LOG.fa)	LOG.println("■ Sample #display(boolean editmode)");
		
		if(!editmode){
			putParameter(MESSAGE,"");
		}
		String aplec_key 	= strHash(htb,"_aplec_key");
		String wdateString	= KeyGen.wdateTypeA(aplec_key);		// 曜日時限の文字列(aplec_key は曜日と時限から作ったキー)
		htb.put("_wdateString",wdateString);
		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}
	/**
	 * 
	 */
    @Override
	public void write(String key,Vector exHtml){

		if(key.equals("meiboList")){
			meiboList(exHtml);
    	
		}else if(key.equals("meiboBLK")){
			meibo_blankColumnBLK(exHtml);
			
		}
    }	
    /**
     * 
     * @param exHtml
     */
    void	meiboList(Vector exHtml){
		// 名簿ファイルを得る
		String  teUid		= strHash(htb,"_teUid");
		String  aplec_key	= strHash(htb,"_aplec_key");
		KamokuApRecord	kar = new KamokuApRecord(teUid,aplec_key,db);	// レコードに名簿ファイル名を持つ
		Meibo	mb			= kar.getMeibo(para);
		//
		// 初期状態では名簿ファイルがないので必ずチェック
		if(mb!=null){
			// 名簿データの表示
			int n = mb.getCounts();
			for(int i=0; i<n; i++){
				// 行番号
        		String  numStr = "0000" + String.valueOf(i+1);
            	int     posE = numStr.length();
	        	int     posS = posE - 4;
        		String  num  = numStr.substring(posS,posE);
				htb.put("_k",num);
        		//
            	// 名簿ファイルから
				String stNumber = mb.getNumber(i);	htb.put("_stNumber",stNumber);
				String stName	= mb.getName(i); 	htb.put("_stName",stName);
				//
				String szDB 	= strHash(htb,"_szDB");
            	Student st = new Student(szDB,stNumber,db);	// データベースを検索してメールアドレスを求める
				// st が DB にない場合は id のみセットした空のレコードが返る
				// 空かどうかは、Student.isEmptyRecord() で調べる
				if(!st.isEmptyRecord()){
					String stMail	= st.email();		htb.put("_stMail",stMail);
					String stKeitai	= st.keitai();		htb.put("_stKeitai",stKeitai);
					String stPasswd	= st.stPasswd();	htb.put("_stPasswd",stPasswd);
				}else{
					String stMail	= mb.getMailAd(i);	htb.put("_stMail",stMail);
					String stKeitai	= mb.getKeitai(i);	htb.put("_stKeitai",stKeitai);
					String stPasswd	= st.stPasswd();	htb.put("_stPasswd","");
				}
	            printVector(exHtml);
			}
			htb.put("_exist_meibo","YES");

		}else{
			// 空白行を表示するため、情報をhtbに残しておく
			// %#meibo_blankColumnBLK% で処理するので、htb は残っている
			htb.put("_exist_meibo","NO");
		}
    }
    /**
     * 
     * @param exHtml
     */
    void	meibo_blankColumnBLK(Vector exHtml){
		String 	chk	= strHash(htb,"_exist_meibo");
		if(chk.equals("NO")){
			printVector(exHtml);
		}
		return;
    	
    }

}

