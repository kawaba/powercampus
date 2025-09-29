//
//  解答レコードがある学生を送信対象とするが、採点が済んでいない学生（score= ""）には
//　送信しない。また、送信したら,eval = done とする。
//　eval が done であるレコードは既に結果送信済みなので送信対象とはしない。
//  なお、作成中でも、解答があり採点してあるものには送信する
//  
package eval;

import java.io.*;

import student.Student;
import tktools.*;
import java.util.*;

import mailutil.exSender;
import meibo.Meibo;

import kadai.Answer;
import kadai.AnswerRecord;
import kadai.KadaiDefRecord;
import kamoku.KamokuApRecord;

import database.Database;
import database.KeyGen;
import framework.*;


// 採点結果をクラス全員にメールで知らせる
public class Announce2 extends Thread implements PCvar{
    final 		String  PS 		= File.separator;
	private     boolean DEBUG   = false;
    private     boolean DBG     = true;
	//
	PrintWriter 	out;
	Hashtable		htb;
	Param 			para;
	Database		db;
	String			szDB;
	//
	Hashtable      	hashPosted;
	String			shubetsu;	// 課題種別
	//
    public Announce2(PrintWriter _out,Hashtable _htb,String _szDB,Param _para,Database _db){
		hashPosted	= new Hashtable(500);
		out		= _out;
		htb		= _htb;
		para	= _para;
		db		= _db;
		szDB	= _szDB;
		
        String  teUid       = 	(String)htb.get(TUID);
        String  lec_key   	= 	strHash(htb,"_lec_key");
        String	te_lec_key	=	KeyGen.get_te_lec_key2(teUid, lec_key);
        String  kadai_key   = 	strHash(htb,"_kadai_key");
		
		KadaiDefRecord	kdr	=	new	KadaiDefRecord(te_lec_key, kadai_key, db);
		shubetsu			=	kdr.shubetsu();
	}
	// 送信処理
	public void run(){
		Answer as1 = send(true,hashPosted);			// インターネットメールへ送信
		//System.out.println("-------------------------------- 1 回目　終了");
		
		Answer as2 = send(false,hashPosted);		// 携帯へ送信
		//System.out.println("-------------------------------- 2 回目　終了");
		
		//
		// どちらをつかってもいいが
		// 送信済みの処理を
		//
        String  teUid       = (String)htb.get(TUID);
        String  aplec_key   = strHash(htb,"_aplec_key");
        String  kadai_key   = strHash(htb,"_kadai_key");
		String  kadaiPath   = para.kadaiAnsDir(teUid,aplec_key,kadai_key) + PS;
		//
		Hashtable 	ht 	 = as1.getHash();	// 解答レコードのハッシュ。学籍番号でレコードを得る。
		String		stNumber;
		//
		Enumeration stNums =  hashPosted.keys();
		while(stNums.hasMoreElements()){
			stNumber 			= (String)(stNums.nextElement());
			AnswerRecord arec 	= (AnswerRecord)ht.get(stNumber);
			arec.evalSetOn();	// = done
			String asPath 		= kadaiPath + stNumber + ".ans";	// 課題ファイル名
			as1.writeAnsRec(asPath,arec);							// レコードをファイルに書き込み			
		}
	}
	public Answer  send(boolean email,Hashtable hashPosted){
		/*
		 * 教師情報をシステムハッシュにセットする
		 * 
		 * 
		 */
        String teUid       	= (String)htb.get(TUID);
		db.MembersInfo(teUid, htb);
		String	name		=	Gear.strHash(htb, TNAME);
		String  mail        =	Gear.strHash(htb, TMAIL);
        
        String path        	= para.GetDefaultTemplatePath();	// e-mail
		if(!email) path   	= para.GetDefaultTemplatePath_k();	// 携帯

        // 送信データをそろえる準備
        String  aplec_key   = strHash(htb,"_aplec_key");
        String  kadai_key   = strHash(htb,"_kadai_key");
        String  className   = strHash(htb,"_title");
        String  kadaiTitle  = strHash(htb,"_kadai_title");
        String  dateStart   = strHash(htb,"_start");
        String  dateEnd     = strHash(htb,"_end");
        //
		KamokuApRecord	kar	= new KamokuApRecord(teUid,aplec_key,db);	// レコードに名簿ファイル名を持つ 
		Meibo	meibo		= kar.getMeibo(para);
        //
        // ハッシングした解答レコードオブジェクトを得る
        Answer 			as = new Answer(htb,para,db); 
        Hashtable 		ht = as.getHash();
        //
        // データ
        String  mailhost    = para.getMailhost();
        String  from        = mail;
        String  title       = "［" + className + "］ 課題の評価結果のお知らせ";
        String  kadaiPath   = para.kadaiAnsDir(teUid,aplec_key,kadai_key) + PS;
		//
        Vector  dt          = makeTable(email,hashPosted,teUid,meibo,as,name,mail,className,kadaiPath,kadaiTitle,dateStart,dateEnd);
        if(dt.size() == 0){
			System.out.println("-------------------------------- 途中 EXIT しました / email = " + email);
			
			return as;	// 携帯などの登録がないときがあるので
        }
        String  mailadmin   = para.getMailadmin();
        Vector  itms        = makeHedder_gr(); // 全部システム側で作ることに
        String  user        = name;
        String  servletURL  = para.getServletUrl();
        String  sysUserName = para.getSysUser();    // eXbinder mail のシステムユーザー名
        //
		exSender es = new exSender (mailhost,from,title,dt,path,mailadmin,itms,servletURL,sysUserName); // 2003.8.13 user を削除
		es.sendMail();
        //
        return as;

    }
    // 送信用名簿ヘッダを作る
    public Vector makeHedder_gr(){

        Vector v = new Vector(15,10);
        v.add("メールアドレス");
        v.add("番号");
        v.add("氏名");

        v.add("得点");
        v.add("解答");
        v.add("担当");
        v.add("担当メールアドレス");
        v.add("クラス名");
        v.add("課題名");
        v.add("開始日時");
        v.add("終了日時");
        //
        return v;
    }
    // 送信用テーブルデータを作る
	//
	//  解答レコードがある学生を送信対象とするが、採点が済んでいない学生（score= ""）には
	//　送信しない。また、送信したら,eval = done とする。
	//　eval が done であるレコードは既に結果送信済みなので送信対象とはしない。
	//  なお、作成中でも、解答があり採点してあるものには送信する
	//  
	//  特殊な場合に備えて、eval を操作するボタンが採点画面に必要である
	//
    public Vector makeTable(boolean email,	// インターネットメールか携帯メールか
							Hashtable  hashPosted,	// 送信したことを覚えておく
							String teUid,
							Meibo  meibo,
                            Answer as,
                            String name,
                            String mail,
                            String className,
                            String kadaiPath,
							String kadaiTitle,
                            String dateStart,
                            String dateEnd){
        //
		Hashtable 	ht 	 = as.getHash();	// 解答レコードのハッシュ。学籍番号でレコードを得る。
		String 		mode = "";
        Vector 		vr 	 = new Vector(300,100);
        int 		n 	 = meibo.getCounts();			// クラスの人数
        for(int i=0; i<n; i++){
	        Vector vc = new Vector(20,10);
			String stNumber = meibo.getNumber(i); 			// 学籍番号
			Student st		= new Student(szDB,stNumber, db);
			String mailaddress = st.email();
			if(!email){	mailaddress = st.keitai(); }	// インターネットメールでなければ携帯のアドレスを
			//
			if(!mailaddress.equals(Student.EMPTY))	{	// EMPTY = "-"　アドレスがあれば送信する

			    vc.add(mailaddress);			// メールアドレス
				vc.add(stNumber);				// 学籍番号
				vc.add(st.kname());       		// 学生の氏名
				//
    	        // 解答ファイルから
        	    Object obj;
				//
				String score = "　";
    	        String ans   = "　";
        	    String msg   = "　";
            	String eval  = "*"; // 初期値
	            if((obj=ht.get(stNumber))!=null){	// 解答があれば送信対象とする
    	            //
					
	                score = ((AnswerRecord)obj).getScore().trim();
					if(score.length() ==0){ score = "　";}
    	            
					msg   = ((AnswerRecord)obj).getMessage().trim();
    	            if(msg.length()   ==0){ msg   = "　";}
        	        /*
        	         * 解答をメールに添付するのはe-mailでレポートのみ
        	         */
   	                ans	=	"　";
    	            if(shubetsu.equals(KadaiDefRecord.REPO)){
    	                if(email){
    	                    ans = 	((AnswerRecord)obj).getAnswer().trim();
    	                    if(ans.length()==0){ ans   = "　";}
    	                }
    	            }
					//
	                // 評価が済んでメールで送信したとき eval に done を入れる事にした。 2003.4.11
					eval  = ((AnswerRecord)obj).getEval().trim(); 
					if(eval.length()  ==0){ eval  = "*";}
					
		            // 提出され採点されたレコードのみにメールを出す（採点したレコードも含める 2003.4.11）
					//
					if(!eval.equals("done")){ // 未送信のレコードのみ対象とする
						if (!score.equals("　")) { // 採点済みなら送信する　＜提出・作成中に関わらず＞
						    vc.add(score);
		        	    	vc.add(ans);
    		        		//
        		    		vc.add(name);
	        	    		vc.add(mail);
		        	    	vc.add(className);
	    		    	    vc.add(kadaiTitle);
    	    		    	vc.add(dateStart);
	        	    		vc.add(dateEnd);
		        	    	//
	    		        	vc.trimToSize();
							vr.add(vc);
							//
							// 送信済みを覚えておく（２度送信するので）
							hashPosted.put(stNumber,"done");
						}
					}
				}
			}
        }
        return vr;
    }
	String strHash(Hashtable htb,String key){
		//
		String	str	= (String) htb.get(key);
		if(str==null){
			System.out.println("★★★★★★★★★★★★★★★★★★★★★★★★★★★");
			System.out.println("★★   　　　　　　　　　　　　　　　　　　　　   ★★");
			System.out.println("★★   ハッシュから取り出したデータは NULL です   ★★");
			System.out.println("★★   key = " + key );
			System.out.println("★★★★★★★★★★★★★★★★★★★★★★★★★★★");
		}
		return str;
	}
	

}