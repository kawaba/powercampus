package kadai;
import java.io.*;
//import java.text.*;
import java.util.*;

import framework.LOG;


public class AnswerRecord extends Object implements Serializable{

	//public	static	 final long serialVersionUID = -9170665261890627783L;

	//
    String  teUid;
    String  classKey;
    String  kadaiKey;
	//
    String  className;
    String  title;
	//
    String  stNumber;
    String  stName;
	//
    String  mode; 			// 完成（checked) か作成途中 ("") か
    String  eval;   		// 採点結果を通知したかどうか　"" または "done"
    String  updateTime;
    String  answer;
    String  score;  		// 得点
    String  message;		// メッセージ
	//
	String  f1;	// 将来への予備
	String  f2;
	String  f3;
    //
    public AnswerRecord(Hashtable htb) {
        String str = null;
        if(htb.get("_teUid")    	!= null)	{teUid    	= new String((String)htb.get("_teUid"));    	}else{ teUid    = ""; }	// 教員キー
        if(htb.get("_aplec_key") 	!= null)	{classKey 	= new String((String)htb.get("_aplec_key")); 	}else{ classKey = ""; }	// 講義実施キー
        if(htb.get("_kadai_key") 	!= null)	{kadaiKey 	= new String((String)htb.get("_kadai_key")); 	}else{ kadaiKey = ""; }	// 課題キー
        //
		if(htb.get("_title") 		!= null)	{className 	= new String((String)htb.get("_title")); 		}else{ className = "";}	// クラス名（講義名）
		if(htb.get("_kadai_title")	!= null)	{title    	= new String((String)htb.get("_kadai_title"));	}else{ title    = ""; }	// 課題題名
		//
        if(htb.get("_stNumber") 	!= null)	{stNumber	= new String((String)htb.get("_stNumber")); 	}else{ stNumber = ""; }	// 学籍番号
        if(htb.get("_kname")   	!= null)	{stName   	= new String((String)htb.get("_kname"));   	}else{ stName   = ""; }	// 学生氏名
        //
        if(htb.get("_disposal") 	!= null)	{mode     	= new String((String)htb.get("_disposal")); 	}else{ mode     = ""; }	// 提出か否か
        if(htb.get("_eval")     	!= null)	{eval     	= new String((String)htb.get("_eval"));     	}else{ eval     = ""; }	// 採点結果を通知したかどうか
        if(htb.get("_updateTime") 	!= null)	{updateTime = new String((String)htb.get("_updateTime"));	}else{ updateTime ="";}	// 最新提出日時
		if(htb.get("_answer")   	!= null)	{answer   	= new String((String)htb.get("_answer"));   	}else{ answer   = ""; }	// 解答
        if(htb.get("_score")    	!= null)	{score    	= new String((String)htb.get("_score"));    	}else{ score    = ""; }	// 得点
        if(htb.get("_message")  	!= null)	{message  	= new String((String)htb.get("_message"));  	}else{ message  = ""; }	// コメント
		//
		if(htb.get("_f1")  			!= null)	{f1  		= new String((String)htb.get("_f1"));  	}else{ f1  = "";	  }	// タイピング文字数
		if(htb.get("_f2")  			!= null)	{f2  		= new String((String)htb.get("_f2"));  	}else{ f2  = "";	  }	// 予備欄－２
		if(htb.get("_f3")  			!= null)	{f3  		= new String((String)htb.get("_f3"));  	}else{ f3  = "";	  }	// 予備欄－３
    }
    //
    public String  getTeUid()       { return    teUid; }
    public String  getClassKey()    { return    classKey; }
    public String  getKadaiKey()    { return    kadaiKey; }
	//
    public String  getClassName()   { return    className; }
    public String  getTitle()       { return    title;  }
	//
    public String  getStNumber()    { return    stNumber; }
    public String  getStUid()    	{ return    stNumber; }
    public String  getStName()      { return    stName; }
	//
    public String  getDisposal()    { return    mode; }
    public String  getMode()        { return    mode; }
    public String  getEval()        { return    eval; }
    public String  getUpdateTime()  { return    updateTime; }
    public String  getAnswer()      { return    answer; }
    public String  getScore()       { return    score; }
    public String  getMessage()     { return    message; }
	//
    public String  getF1()     		{ return    f1; }
	public String  getF2()     		{ return    f2; }
    public String  getF3()     		{ return    f3; }
	//
	// 作成済みが否かのフラグをオン・オフする 2003.6.17 追加
    public void  modeSetOn()        { mode = "checed"; }
	public void  modeSetOff()       { mode = ""; }
	//
	// 採点し通知が済んだか否かのフラグをオン・オフする 2003.6.17 追加
    public void  evalSetOn()        { eval = "done"; }
	public void  evalSetOff()       { eval = ""; }
	//
	public void  setMessage(String s) { message = new String(s); }
    public void  setScore(String s)   { score   = new String(s); }
    public void  setEval(String s)    { eval    = new String(s); }
    public void  setAns(String s)     { answer  = new String(s); }
    public void  setMSEA(String s1,String s2,String s3,String s4){
        setMessage(s1);
        setScore(s2);
        setEval(s3);
        setAns(s4);
    }
    //
    public boolean isSubmitted() { return mode.equals("checked");}
    //
    public void printRecord(){
        LOG.println("class AnswerRecord #printRecord() : 解答レコードの内容です");
        LOG.println("      teUid      :" 	+ teUid);
        LOG.println("      classKey   :" + classKey);
        LOG.println("      kadaiKey   :" + kadaiKey);
        //
        LOG.println("      className  :" + className);
        LOG.println("      title      :" + title);
		//
        LOG.println("      stNumber   :" + stNumber);
        LOG.println("      stName     :" + stName);
		//
		LOG.println("      mode       :" + mode);
        LOG.println("      eval       :" + eval);
        LOG.println("      updateTime :" + updateTime);
        LOG.println("      answer     :" + answer);
        LOG.println("      score      :" + score);
        LOG.println("      message    :" + message);
		//
		LOG.println("      f1         :" + f1);
		LOG.println("      f2         :" + f1);
		LOG.println("      f3         :" + f1);
    }
}
/*
	// バックアップのために全ての解答レコードをテキストとしてファイルに書き込む
	public void answer_backup(Param para){
		Vector  users = para.getUserIdList();
		int	    n 	  = users.size();
		for(int i=0; i<n; i++){// 一人の教師についての処理
			String teUid = (String)users.get(i);							// 教師ＩＤ
			String 	path = para.getBackupDir(teUid) ;						// 教師毎のバックアップディレクトリ名
			Kadai	kd	 = new Kadai(para.getUserKadaiPath(teUid));			// 教師毎の課題オブジェクト
			//
			Vector  keys    = kd.getAllRecords();
			int		m	    = keys.size();
			//
			for(int k=0; k<m; k++){// １つの課題についての処理
				String keyrec = (String)keys.get(k);
				Csv	   kcsv   = new Csv(keyrec,"%");
				String key	  = kcsv.get(1);							// 課題キー
				Csv    temp	  = new Csv(key,"-");
				String classKey = temp.get(0);							// クラスキー
				Answer ans		 = new Answer(teUid,classKey,key,para);	// 対応する解答
				String [] fnames = ans.getFilenames();					// 解答ファイル名のリスト
				//
				String kadaiBaukupPath = path + key + PS;  	// ex. /home/pc/kawaba01/backup/ss-0010/
				File bkfp = new File(kadaiBaukupPath);		// ディレクトリの存在をチェックしてなければ作る
				if(!bkfp.isDirectory()){
					bkfp.mkdirs();
				}
				int		nn	= ans.getTotal();							// 解答数
				for(int j=0; j<nn; j++){// １つの解答についての処理
					String filename = kadaiBaukupPath + fnames[j];
			        try{
				        //boolean 値が true の場合、println() メソッドでは出力バッファをフラッシュする
						PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(filename)),true);
						//
						AnswerRecord rec = ans.getdAnsRecordAt(j);
						String ansrec = 
							rec.getMode() + "$" + rec.getDisposal() + "$" + rec.getTeUid() + "$" + rec.getClassKey() 
							+ "$" + rec.getClassName()  + "$" + rec.getKadaiKey() 	+ "$" + rec.getStUid()
							+ "$" + rec.getTitle() 		+ "$" + rec.getAnswer() 	+ "$" + rec.getStNumber()
							+ "$" + rec.getStName()		+ "$" + rec.getUpdateTime() + "$" + rec.getMessage()
							+ "$" + rec.getScore() 		+ "$" + rec.getEval();
						//
						out.println(ansrec);	//
	            		out.close();
		        	}catch (Exception exc) {
        		    	System.out.println("kadai_backup() : 課題バックアップレコード出力エラー");
        			}
				}
				//
			}
		}
	}

*/