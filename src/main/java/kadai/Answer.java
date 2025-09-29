/*
特定の課題について，レコードを表示するためのアクセスメソッドを提供する
*/
package kadai;

import java.io.*;
//import java.text.*;
import java.util.*;

import meibo.Meibo;

import kamoku.KamokuApRecord;

import database.Database;
import database.KeyGen;
import framework.LOG;
import framework.Param;

import	tktools.*;

public class Answer implements KadaiVar{
  
	
	final String CR = System.getProperty("line.separator");
	final String PS = File.separator;
	final static 	String CHECKED	= "checed";	//(checked ではない)　 完成したので提出する mkAnswer.html の _radio_disposal の値　

	//
  	Hashtable 	htb;
  	Param   	para;
	Database	db;
	//
	String  teUid;      // 担当者ＩＤ
	String	lec_key;	// 講義定義キー
	String	aplec_key;	// 講義実施キー
	String  kadai_key;  // 課題キー
	//
	String	te_aplec_key;
	String  te_lec_key;
	//
	KadaiDefRecord	kadaiDef;	// 課題定義レコード
	KadaiApRecord	kadaiAp;	// 課題実施レコード
	KamokuApRecord	kar;		// 科目実施レコード
	//
	Meibo		mb;
	/**
	 * ファイル名配列のアクセスモード（番号順か提出順か）
	 */
	String		sortflag	=	NUMBER_MODE;	// DATE_MODE
	
  	String  answerPath; // 解答ファイルのあるディレクトリパス
  	//
	String []ansFiles1; // 解答作成者（作成中含む）のファイル名リスト
	String []ansFiles2; // 提出日時でソートした解答作成者（作成中含む）のファイル名リスト
  	//
  	String  meiboPath; // クラス名簿ファイルのパス
  	//
  	String  path;      // 課題のあるディレクトリパス　= {userdir}/{kadaiKey}/
  	//
  	int     all;        // submited + making + nothing
  	int     total;      // submited + making
  	int     submitted;
  	int     making;
  	//
  	AnswerRecord    temp;  // 作業用解答レコード
  	int             index; // 作業用レコード番号（実際に読みこんだレコードの番号）
  	//

    public Answer(Hashtable _htb,Param _para,Database _db){
		if(LOG.fa) LOG.outHash(_htb,"class Answer #Answer() : コンストラクタの入り口です");
		//
		htb		= _htb;
		db		= _db;
		para    = _para;
		//
		teUid     		= getParameter("teUid");
		lec_key  		= getParameter("lec_key");
		aplec_key  		= getParameter("aplec_key");
		kadai_key  		= getParameter("kadai_key");
		te_aplec_key	= KeyGen.get_te_aplec_key(htb);
		te_lec_key		= KeyGen.get_te_lec_key(htb);
		//
		initialize();
	}
	/**
	 * 状態の初期化を行う
	 *
	 */
	void initialize(){

		kadaiDef	= new KadaiDefRecord(te_lec_key,kadai_key,db);
		kadaiAp		= new KadaiApRecord (te_aplec_key,kadai_key,db);
		//
		// 課題パスから解答ファイルを調べて変数データを取得する
		// 名簿ファイルを得、名簿から総数を得る
		kar 	= new KamokuApRecord(teUid,aplec_key,db);	// レコードに名簿ファイル名を持つ
		mb		= kar.getMeibo(para);
		if(mb!=null){
			initialize_sub();
		}else{
			all			= 0;
			total 		= 0;
			submitted 	= 0;
			making    	= 0;
		}
	}
	/**
	 * 名簿ファイルを使って状態の初期化を行う
	 *
	 */
	void initialize_sub(){
		/*
		 * クラスの人数合計
		 */
		all 	= mb.getCounts(); 
		/*
		 *  解答ファイル名を配列に得る
		 */
		answerPath 	= para.kadaiAnsDir(teUid, aplec_key, kadai_key) + PS;
		if(LOG.fa){
		    LOG.println("*** answerPath=" + answerPath);
		}
		File fp  	= new File(answerPath);
		ansFiles1 	= fp.list();
		/*
		 * さらに計数を調べる
		 */
		if((ansFiles1 == null) || (ansFiles1.length == 0)){
			total 		= 0;
			submitted 	= 0;
			making   	= 0;
		}else{
			/*
			 * ファイル名（学籍番号）の順にソートしておく
			 * 総提出ファイル数を求める
			 */
			Arrays.sort(ansFiles1); 
			total 		= ansFiles1.length;
			/*
			 * さらに各種計数データを得る
			 * 作成済みと作成中の数を求める
			 */
			getCounts();
			/*
			 * 提出時間でソートしたファイルindexを作っておく
			 */
			makeAnsFiles2();
			
		}
	}
	/**
	 * 提出日時でソートしたファイル名配列 ansFiles2 を作成する
	 *
	 */
	public void  makeAnsFiles2(){
		if(total == 0) 	return;
		//
		ansFiles2	= new String [total];
		for(int i=0; i<total; i++){
			AnswerRecord rec 	= getdAnsRecordAt(i);
			if(rec==null){
			    continue;
			}
			String updateTime 	= rec.getUpdateTime();					// "yyyy-MM-dd HH:mm:ss" の形式
			String fname		= rec.getStUid() + para.ansEXT();		// 学籍番号 + 解答ファイル拡張子
			ansFiles2[i]		= updateTime + "%" + String.valueOf(i);
		}
		Arrays.sort(ansFiles2);
		//
		for(int k=0; k<total; k++){
			Csv cs = new Csv(ansFiles2[k],"%");
			ansFiles2[k] = cs.get(1);
		}
		
	}	
	/**
	 * 作成済みと作成中の数を求める
	 */
	void getCounts(){
		submitted 	= 0;	// 提出完了
	    making   	= 0;	// 作成中
	    for(int i=0; i<total; i++){
	    	/*
	    	 * readObject を使って読み込む
	    	 * 	古いレコードを読み込んだ時は null が返る 
	    	 */
	    	temp = getAnsRecord(answerPath + getAnsFile(i));
	    	if(temp==null){
	    	    continue;
	    	    
	    	}else{
	    	    if(temp.isSubmitted()){
	    	        submitted++;
	        	
	    	    }else{
	    	        making++;
	    	    }
	    	}
	    }
	}
	/**
	 * ソートモードの切り替え
	 * @param ss
	 */
	public	void	changeSortmode(String key){
		if((key.equals(NUMBER_MODE)) || (key.equals(DATE_MODE))){
			sortflag	=	key;
		}
	}
	/**
	 * 現在のソートモードを得る
	 * @return
	 */
	public	String	sortmode(){
		return		sortflag;

	}
	/**
	 * ソートモードを判断してi 番目のファイル名を返す
	 * @return
	 */
	String	getAnsFile(int i){
		if(sortmode().equals(NUMBER_MODE)){
			return	ansFiles1[i];
		
		}else{
			int	pos	=	Integer.parseInt(ansFiles2[i]);
			return	ansFiles1[pos];
		}
	}
	String	[] getSnsFileArray(){

		if(sortmode().equals(NUMBER_MODE)){
			return	ansFiles1;
		
		}else{
			return	ansFiles2;
		}
		
	}
	/**
	 *  ◎計数データを返す
	 * @return
	 */
	public int getSubmitted()      { return submitted; }
	public int getMaking()         { return making; }
	public int getAll()            { return all; }
	
	/**
	 * ファイル総数を返す
	 * @return
	 */
	public int getTotal(){
		return total;
	}	
	/**
	 * 課題のアイコンファイル名を返す
	 * @return
	 */
	public String kadaiIcon() {
		String 	kadai_Icon	= kadaiDef.kadaiIcon();
		return 	kadai_Icon;
	}	
	/**
	 * POS番目のAnswerRecordを読み出して返す
	 * 
	 * @param pos
	 * @return
	 */
	public AnswerRecord getdAnsRecordAt(int pos){
		AnswerRecord arec;
		if((pos >= total)||(pos < 0)){
			return null;
		}
		arec = getAnsRecord(answerPath + getAnsFile(pos));
		
		/* デバッグ用にレコードを表示する */
		if(LOG.fa){
			LOG.println("■Answer #getAnsRecordAt( int pos)");
			arec.printRecord();
		}
		return arec;

	}
	/**
	 * 学籍番号でファイルを探し、現在のソートモードで何番目のレコードか
	 * その番号を返す。レコードがないと-1 を返す。
	 * @param num
	 * @return	
	 */
	public	int search(String num){
		AnswerRecord	rec	=	null;
		int				n	=	getTotal();
		for(int i=0; i<n; i++){
			rec				=	getdAnsRecordAt(i);
			String	suNum	=	rec.getStNumber();
			if(suNum.equals(num)){
				return		i;
			}
		}
		return	-1;
	}
	/**
	 * ◎ファイルパスからAnswerオブジェクトを読みこんで返す．
	 * 
	 * @param fname
	 * @return			ファイルがない場合は null を返す．
	 */
	public AnswerRecord getAnsRecord(String fname){
	    ObjectInputStream  objIn  = null;
	    AnswerRecord ans = null;
	    try{
	        objIn = new ObjectInputStream(new FileInputStream(fname));
	        try{
	            ans = (AnswerRecord)objIn.readObject();
	            objIn.close();
	        }catch(ClassNotFoundException e1){
	            System.out.println("ClassNotFoundException:" + e1);
	            ans = null;
	        }catch(IOException e2){
	            System.out.println("IOException:" + e2);
	            ans = null;
	        }
	    }catch(IOException e3){
	        System.out.println("can't Open :IOException:" + e3);
	        ans = null;
	    }
	    return ans;
	}	

	/**
	 * （学籍番号、提出日時）のハッシュを作成する
	 * @return
	 */
	public Hashtable	makeSmb(){
      Hashtable smb	= new Hashtable(all * 2);
		for(int i=0; i<total; i++){
			AnswerRecord rec 	= getdAnsRecordAt(i);
			String updateTime 	= rec.getUpdateTime();	// "yyyy-MM-dd HH:mm:ss" の形式
			String stUid		= rec.getStNumber();	// 学籍番号
			smb.put(stUid,updateTime);
		}
		return	smb;
	}

	/**
	 * ファイルリストを返す
	 * @return
	 */
	public String [] getFilenames()	{ return getSnsFileArray(); }
	/**
	 * この解答に対する課題定義レコードを返す
	 * @return
	 */
	public KadaiDefRecord getKadaiDefRecord()		{ return kadaiDef;}
	/**
	 * この課題に対する課題実施レコードを返す
	 * @return
	 */
	public KadaiApRecord  getKadaiApRecord()		{ return	kadaiAp;}
	/**
	 * この課題に対する科目実施レコードを返す
	 * @return
	 */
	public KamokuApRecord	getKamokuApRecord() 	{ return kar;}	
	/**
	 * 全ての解答レコードを削除する
	 *
	 */
	public void deleteAnsRecords(){
	    for(int i=0; i<total; i++){
	        File fp = new File(answerPath + getAnsFile(i));
	        fp.delete();
	    }
	    ansFiles1   = 	null;
	    ansFiles2	=	null;
	    total       = 0;
	    submitted   = 0;
	    making      = 0;
	}
	/**
	 * 特定の学生の解答ファイルを削除して、クラスも初期化し直す
	 * 
	 */
    public	void	deleteAnsRecord(String	stNumber){
		/* パスがディレクトリなら再帰的に全てを消す */
		Gear.delDir(answerPath + getFilePath(stNumber));
		initialize();
     }
  	/**
	 * 学生の解答ファイルへのパスを返す
	 * 現在は学籍番号＝ファイル名 + 拡張子で、拡張子は Param オブジェクトの
	 * メソッド ansEXT() で得ることができる
	 * 
	 */
	public	String	getFilePath(String stNumber){
		
		return	stNumber + para.ansEXT();
	}
	/**
	 * 学籍番号をキーとするハッシュ表を作って返す
	 * @return
	 */
    public Hashtable getHash(){
        Hashtable ht = new Hashtable(all * 2);
        //
        for(int i=0; i<total; i++){
            AnswerRecord ans = getdAnsRecordAt(i);
            if(ans != null){
                String number = ans.getStNumber();
                ht.put(number,ans);
            }
        }
        return ht;
    }
    /**
     * 解答レコードの値を更新して書き込む
     * （TEXT の解答用）
     * @param pos
     * @param s1
     * @param s2
     * @param s3
     * @param s4
     * @return
     */
    public AnswerRecord writeAnsRecord(int pos,String s1,String s2,String s3,String s4){
        AnswerRecord r = getdAnsRecordAt(pos);
        r.setMSEA(s1,s2,s3,s4);
        writeAnsRec(answerPath + getAnsFile(pos), r);
  		//
  		return	r;
    }
    /**
     * 解答レコードの値を更新して書き込む
     * （ＷＥＢの解答用）
     * Web モードで採点している場合は、解答(s4)は変更がないのでそのままにする
     * 
     * @param pos
     * @param s1
     * @param s2
     * @param s3
     * @return
     */
    public AnswerRecord writeWebAnsRecord(int pos,String s1,String s2,String s3){
        AnswerRecord r = getdAnsRecordAt(pos);
        //r.setMessage(s1);
        r.setScore(s2);
        r.setEval(s3);
        writeAnsRec(answerPath + getAnsFile(pos),r);
  		//
  		return	r;
    }
    
    /**
     * 更新済みの解答レコードを書き込む（試験問題用)
     * 
     * 
     * @param pos
     * @param r
     * @return
     */
	public AnswerRecord writeAnsRecord(int pos,AnswerRecord r){
		writeAnsRec(answerPath + getAnsFile(pos),r);
		return	r;
	}    
	/**
	 * 解答レコードを新規作成し得点を書き込む（FILEの解答用）
	 * 
	 * @param stNumber
	 * @param score
	 * @return
	 */
	public AnswerRecord createFileRecord(String stNumber, String score){
		
		String			ansPath	=	para.stFilePath(teUid, aplec_key, kadai_key, stNumber);
		AnswerRecord	r		= 	new AnswerRecord(htb);
		r.setScore(score);
		writeAnsRec(ansPath,r);
		//
		return	r;
	} 
	/**
	 * 解答レコードの得点のみ更新する（FILEの解答用）
	 * 
	 * @param stNumber
	 * @param score
	 * @return
	 */
	public AnswerRecord updateFileRecord(String stNumber, String score){
		
		String			ansPath	=	para.stFilePath(teUid, aplec_key, kadai_key, stNumber);
		AnswerRecord	r 		= 	getAnsRecord(ansPath);
		if(r==null){
		    return	null;
		}
		r.setScore(score);
		writeAnsRec(ansPath,r);
		//
		return	r;
	}    
	/**
　	 * パスを指定して解答レコードを書き込む
	 *
	 * @param path
	 * @param r
	 * @return
	 */
	public boolean  writeAnsRec(String path,AnswerRecord r){
      ObjectOutputStream  objOut  = null;
      try{
          objOut = new ObjectOutputStream(new FileOutputStream(path));
          try{
              objOut.writeObject(r); // Serializable
              objOut.flush();
              objOut.close();
              return true;
          }catch(IOException e1){
          }
      }catch(IOException e2){
      }
      return false;
	}
	
	/**
	 * システムハッシュからキーで文字列を取り出す
	 * キーの先頭に _ が付いていない場合は付加してから
	 * 使う
	 *
	 ** @param key
	 * @return
	 */
	public String getParameter(String key){
		String	setkey	=	key;
		if(!Gear.isEmpty(key)){
			char	ch	=	key.charAt(0);
			if(ch!='_'){
				setkey	=	"_" + key;
			}
			return	Gear.strHash(htb, setkey);
		}
		return	"";
	}	

}


