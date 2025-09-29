/*
     Power Campus Calender

*/
package kamoku;
import student.Student;
import  tktools.*;
import java.io.*;
import java.util.*;

import meibo.Meibo;
import note.NOTE;

import kadai.KadaiInfo;

import database.Database;
import database.KeyGen;
import faq.FAQmail;
import faq.FAQmailAll;
import framework.LOG;
import framework.Param;
//
public class KamokuDelete extends Object {
    //
    Database        db;
    //
   public KamokuDelete(Database _db){
        //
        //if(DBG._tr090) DBG.outHash(_htb,"KamokuDelete のコンストラクタです");
        //
        db      = _db;
		
    }
    //
	//
	// 科目削除処理
	//
	void delete_kamokuInfo(String szDB,String teUid,String lec_key,Param para){
		if(LOG.fa) LOG.println("class exwork #delete_kamokuInfo() : 科目削除処理 の先頭です");
		if(LOG.fa) LOG.println("           szDB  = " + szDB);
		if(LOG.fa) LOG.println("           teUid = " + teUid);
		//
		Vector	teApplecs	= all_te_aplec_recs( teUid, lec_key);
		//
		// 以下の手順で削除を行う
		//   	1.学生データベース更新
		//   	2.学生のノートを削除
		//		3.課題実施レコード削除
		//   	4.セクション実施レコード削除
		//   	5.ディレクトリ削除
		//      6.このコマに対する質問メールを全て消去
		//		7.講義実施レコードを削除する
		delete_some_apps( teApplecs, szDB, para);
		//
		// 以下の手順で削除を行う
		//		1.セクション定義レコード
		//		2.課題定義レコード
		//		3.資料定義レコード
		//      4.uid/eml/, uid/html/, uid/kadai/ の中の各ディレクトリ（資料と課題に付いてのグラフィックス）
		//      
		String te_lec_key	= KeyGen.get_te_lec_key2(teUid,lec_key);
		delete_some_defTables(te_lec_key);
		delete_some_defDir(teUid, lec_key, para);	// 2005.2 データディレクトリの削除
		//
		// この科目（定義）自体を削除する
		KamokuDefRecord kd	=	new KamokuDefRecord();	// 空のレコードを生成
		kd.set_teuid(teUid);
		kd.set_lec_key(lec_key);
		kd.delete( db );
	}
	//
	// 特定の講義にかかる講義（実施）レコードを全て求める
	//
	public Vector  all_te_aplec_recs(String teUid,String lec_key){
		if(LOG.fa) LOG.println("class exwork #all_te_aplec_keys() : 特定の講義にかかる講義（実施）キーを全て求める の先頭です");
		if(LOG.fa) LOG.println("           teUid   = " + teUid);
		if(LOG.fa) LOG.println("           lec_key = " + lec_key);
		//
		Vector	vrec	= new Vector(10,10);
		int n	= db.get_some_ApLectures(teUid, lec_key, vrec);
		return  vrec;
	}
	//
	//
	// 特定の科目を割り付けて生成された複数の講義について、全ての実施情報を削除する
	//
	public void delete_some_apps(Vector teAppLecs,String szDB,Param para){
		if(LOG.fa) LOG.println("class exwork #delete_some_app_sec_app_kadai() : 特定の講義（定義）にかかる学生データベースの履修情報を削除し、セクション、課題の実施データも消去する");
		if(LOG.fa) LOG.outVector2(teAppLecs,"class exwork #delete_some_app_sec_app_kadai() : 講義実施レコードのベクターです");
		//
		int n	= teAppLecs.size();
		if(LOG.fa) LOG.println("class exwork #delete_some_app_sec_app_kadai() : 発見した科目実施レコードの総数は [" + n + "] 件です");
		for(int i=0; i<n; i++){
			// キーを取得する
			Vector 	te_app_lecture	= (Vector) teAppLecs.get(i);
			String 	teUid			= (String) te_app_lecture.get(KamokuApRecord.TEUID);
			String 	aplec_key		= (String) te_app_lecture.get(KamokuApRecord.AP_LEC);
			
			delete_a_apps(teUid, aplec_key, szDB, para);
		}
	}
	/**
	 * ひとつの講義実施クラスを削除する
	 * @param teUid
	 * @param aplec_key
	 * @param szDB
	 * @param para
	 */
	public	void	delete_a_apps(String teUid, String aplec_key, String szDB,Param para){
		
		String	te_aplec_key	= KeyGen.get_te_aplec_key2(teUid,aplec_key);
		//
		// クラス名簿による学生データベースの更新（削除アップデート）
		KamokuApRecord	kar = new KamokuApRecord(teUid,aplec_key,db);	// レコードに名簿ファイル名を持つ.なければ新規
		Meibo	mb			= kar.getMeibo(para);						// para は名簿ファイルを格納するパスを持つ
		// クラス登録がないと名簿がないので null が返る
		if(mb!=null){
			if(LOG.fa) LOG.println("class exwork #delete_some_app_sec_app_kadai() : 履修データを削除します");
			// 1.学生データベース更新
			deleteUpdate(mb, te_aplec_key, szDB);						// 現在の名簿ファイルを削除名簿としてアップデートする
			//
			// 2. 学生のノートを削除
			delete_notes(mb,te_aplec_key,szDB);							// 学生のノートを削除する
			//
			// 3. 学生の課題提出履歴を削除
			delete_kadais(mb,te_aplec_key,szDB);
		}
		//
		// 3. 4. データベースレコードを削除
		db.delete_some_KadaiAps  (te_aplec_key);
		db.delete_some_SectionAps(te_aplec_key);
		//
		// 5. ディレクトリを削除
		delete_some_directories(teUid, aplec_key, para);
		//
		// 6. このコマに対する質問メールを全て消去
		delete_all_faqmail(te_aplec_key);
		//
		// 7. 講義実施レコードを削除する
		kar.delete(db);		
	}
	//
	// 特定の科目のセクション、課題、資料の定義レコードを全て削除する
	//
	public void delete_some_defTables(String te_lec_key){
		if(LOG.fa) LOG.println("class exwork #delete_some_defTables() : 特定の講義（定義）にかかるセクション、課題、資料の定義レコードを全て削除する");
		if(LOG.fa) LOG.println("           te_lec_key = " + te_lec_key);
		//
		db.delete_some_SectionDefs(te_lec_key );
		db.delete_some_KadaiDefs( te_lec_key);
		db.delete_some_ReferenceDefs( te_lec_key );
		if(LOG.fa) LOG.println("class exwork #delete_some_defTables() : 処理終了");
	}
	public	void	delete_some_defDir(String teUid, String lec_key, Param para){
		
		// eml ファイル
		FileGear.delDir(para.getEpmlPathName(teUid, lec_key));
		// html ファイル
		FileGear.delDir(para.getHtmlPathName(teUid, lec_key));
		// 課題説明用グラフィックス
		FileGear.delDir(para.getKamokuAttachPathName(teUid, lec_key));
		
		
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
				/*
				 * 学生の削除は年度境界でおこなうと不都合がある
				 * 削除して登録とう処理パターンから携帯アドレスや個人情報の再登録が必要になる
				 * そこで，たとえば毎年５月ころに責任者権限で受講クラスの存否をチェックし，
				 * ひとつもなければ削除する，という形式に変更する．
				 * このとき，ファイルキャビネットなどのほかのディレクトリも消去する
				 * 削除メニューは教師の「設定」の中に入れ，実行にはパスワードを求める方式．
				 * 改定は次回のアップグレード時に行うが，ここはコメントアウトしておく
				 */
				//
				// どのクラスにも所属していない状況ならレコード自体を抹消する
				// （classinfo が無しになればデータそのものを削除する）
				/*
				if(classinfoNew == null){
					st.delete();	// レコードを削除する
					if(DBG._tr090) DBG.println("           学生 " + stNumber + "を削除しました");
				}else{
					st.set_classInfo(classinfoNew);
					st.update(); // レコードを更新する
				}
				*/
				if(classinfoNew == null){
				    classinfoNew	=	" ";
				}
				st.set_classInfo(classinfoNew);
				st.update(); // レコードを更新する
				
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
    //	2004.2.3
	//　名簿ファイルから学籍番号を得て、各学生の該当講義のノートを全て削除する
	//　名簿ファイルは null でないことが前提
	public	void	delete_notes(Meibo mb,String te_aplec_key,String szDB){
		//
		NOTE	note	= new NOTE(szDB,db);			// オブジェクトを生成して、szDB と db がセットされるだけ
		int		max		= mb.size();					// 名簿に含まれる学生数
		for(int	k=0; k<max; k++){
			String	stNumber	= mb.getNumber(k);
			int 	count		= note.delete_NOTE(stNumber,te_aplec_key);	// 特定の実施講義でこの学生が書いたノートを全て消去する
			if(LOG.fa) LOG.println("■ ノート削除 " + stNumber + "=" + count + " 件");
		}
	}
    //	2004.3.28
	//　名簿ファイルから学籍番号を得て、各学生の該当講義の課題提出履歴を全て削除する
	//　名簿ファイルは null でないことが前提
	public	void	delete_kadais(Meibo mb,String te_aplec_key,String szDB){
		//
		KadaiInfo	kinfo	= new KadaiInfo(szDB,db);		// オブジェクトを生成して、szDB と db がセットされるだけ
		int		max		= mb.size();					// 名簿に含まれる学生数
		for(int	k=0; k<max; k++){
			String	stNumber	= mb.getNumber(k);
			int 	count		= kinfo.delete_KadaiInfo(stNumber,te_aplec_key);	// 特定の実施講義の課題提出履歴を全て消去する
			if(LOG.fa) LOG.println("■ 課題提出履歴削除 " + stNumber + "=" + count + " 件");
		}
	}
	//
	//
	//  特定の講義にかかるサブディレクトリを全て消去する
	//
	//     getUserPath(teUid) ---> ex. /home/pc/kawaba/
	/*
			answers --------- 解答ファイル
			anstemp --------- 試験問題解答の一時保存場所
			classes --------- クラス名簿
			file ------------ ファイル提出
		    mailtemp -------- 差し込みファイル
			zip ------------- zip ファイル作業ディレクトリ（zip を利用するとクラス毎に作成される）
			
			以下の作業ディレクトリはクラスではなく教員に従属するので消さない
				temp ------ 
				meibo ----- 
				import ---- 
			
			これらのディレクトリ配下の aplec_key （課題実施キー）を名前とするディレクトリ
			を全て消す
	*/
	//
	public void delete_some_directories(String teUid,String aplec_key,Param para){
		if(LOG.fa) LOG.println("class exwork #delete_some_directories() : 特定の実施科目にかかるサブディレクトリを全て消去する の先頭です");
		if(LOG.fa) LOG.println("           teUid = " + teUid);
		if(LOG.fa) LOG.println("       aplec_key = " + aplec_key);
		//
		// 消去するディレクトリの取得
	 	String filePostDir		= para.filePost	 ( teUid,aplec_key);	File	filePostDirFP	= new File(filePostDir);
	 	String classMeiboDir	= para.classMeibo( teUid,aplec_key);	File	classMeiboDirFP	= new File(classMeiboDir);
	 	String answersDir		= para.answers	 ( teUid,aplec_key);	File	answersDirFP	= new File(answersDir);
		String ansTempDir		= para.ansTemp	 ( teUid,aplec_key);	File	ansTempDirFP	= new File(ansTempDir);
	 	String sashikomi		= para.sashikomi ( teUid,aplec_key);	File	sashikomiFP		= new File(sashikomi);
	 	String zip				= para.zip 		 ( teUid,aplec_key);	File	zipFP			= new File(zip);
		//
		if(LOG.fa){
			if(LOG.fa) LOG.println("class exwork #delete_some_directories() : 取得したディレクトリの一覧");
			if(LOG.fa) LOG.println("           filePostDir   = " + filePostDir);
			if(LOG.fa) LOG.println("           classMeiboDir = " + classMeiboDir);
			if(LOG.fa) LOG.println("           answersDir    = " + answersDir);
			if(LOG.fa) LOG.println("           ansTempDir    = " + ansTempDir);
			if(LOG.fa) LOG.println("           sashikomi     = " + sashikomi);
			if(LOG.fa) LOG.println("           zip           = " + zip);
		}
		// 削除
		Gear.deleteDir(filePostDirFP);
		Gear.deleteDir(classMeiboDirFP);
		Gear.deleteDir(answersDirFP);
		Gear.deleteDir(ansTempDirFP);
		Gear.deleteDir(sashikomiFP);
		Gear.deleteDir(zipFP);
		//
		if(LOG.fa) LOG.println("class exwork #delete_some_directories() : 削除終了");
	}
	// 2004.2.3
	// te_aplec_key で特定されるコマで登録された質問メールデータを全て消去する
	//
	public void	delete_all_faqmail(String te_aplec_key){
		if(LOG.fa) LOG.println("class exwork #delete_all_faqmail() : te_aplec_key で特定されるコマで登録された質問メールデータを全て消去する の先頭です");
		//
		// FAQmailAllオブジェクトは生成すると、te_aplec_key で特定される全てのFAQmailをデータベースより取り出し
		// 内部配列として保持している．これを get()メソッドで順次取り出し、FAQmailの削除メソッドで消去する．
		//
		FAQmailAll	fqAll	= new FAQmailAll(te_aplec_key,db);
		int			max		= fqAll.size();
		//System.out.println("★ max = " + max);
		for(int k=0; k<max; k++){
			FAQmail	fq	= fqAll.get(k);
			if(LOG.fa) LOG.println("■ FAQメール削除 : " + fq.seq_no());
			fq.delete_FAQmail();
		}
	}
	/**
	 * レストアのために科目定義情報だけを全て削除する
	 * 
	 * @param teUid
	 * @param lec_key
	 * @param para
	 */
	public	void	deleteForRestore(String teUid, String lec_key, Param para){
		// 以下の手順で削除を行う
		//		1.セクション定義レコード
		//		2.課題定義レコード
		//		3.資料定義レコード
		//      4.uid/eml/, uid/html/, uid/kadai/ の中の各ディレクトリ（資料と課題に付いてのグラフィックス）
		//      
		String te_lec_key	= KeyGen.get_te_lec_key2(teUid,lec_key);
		delete_some_defTables(te_lec_key);
		delete_some_defDir(teUid, lec_key, para);	// 2005.2 データディレクトリの削除
		//
		// この科目（定義）自体を削除する
		KamokuDefRecord kd	=	new KamokuDefRecord();	// 空のレコードを生成
		kd.set_teuid(teUid);
		kd.set_lec_key(lec_key);
		kd.delete( db );
		
	}
}

