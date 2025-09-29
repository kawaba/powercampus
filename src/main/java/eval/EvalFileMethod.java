/*
 * 作成日: 2005/04/30
 *
 */
package eval;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;

import database.Database;
import database.KeyGen;

import kadai.AnswerRecord;
import kadai.KadaiInfo;
import	framework.*;

/**
 *	ファイル提出課題の採点データを書き込むためのクラス
 *
 *　
 */
public class EvalFileMethod {
    
    Hashtable	htb;
    Param		para;
	Database	db;
	String 		szDB;
    
    
    String		teUid;
    String		aplec_key;
    String		kadai_key;
    String		te_aplec_key;
    
    
    public	EvalFileMethod(String teUid, String aplec_key, String kadai_key, 
            							     Hashtable htb, Param para, String szDB, Database db){
        this.teUid		=	teUid;
        this.aplec_key	=	aplec_key;
        this.kadai_key	=	kadai_key;
        
        this.htb		=	htb;
        this.para		=	para;
        this.szDB		=	szDB;
        this.db			=	db;
        te_aplec_key	=	KeyGen.get_te_aplec_key2(teUid,aplec_key);
    }
    
	/**
	 * ファイル提出課題の採点データを書き込む
	 * 
	 * ファイル提出課題の採点データを作成する
	 * 　新規で解答ファイルがない場合は作成し点数を書き込む
	 * 　更新で解答ファイルがある場合は得点のみアップデートして書き込む
	 * 
	 * これらと同時に，課題情報ファイルにも得点を状態を書き込む
	 * 
	 */
	public	void	writeEval(String stNumber, String score, String level){
		if(LOG.fa) LOG.println("■ EvalFileMethod #writeEval() ");
		
		String	ansPath	=	para.stFilePath(teUid, aplec_key, kadai_key, stNumber);
		File	ansFp	=	new File(ansPath);
		if(ansFp.exists()){
			updateFileRecord(stNumber, score, level);
			
		}else{
			createFileRecord(stNumber, score, level);
		}
		update_KadaiInfo(stNumber, score);		
	}    
	/**
	* KadaiInfo を更新する
	* 
	* @param ans
	*/	
	public	void update_KadaiInfo(String stNumber, String score){
	    if(LOG.fa) LOG.println("■ EvalFileMethod #update_KadaiInfo() ");
	    //
		KadaiInfo kdi	= new KadaiInfo(szDB,db);
		kdi.updateScore( stNumber, te_aplec_key, kadai_key, score);
	}	    
	/**
	* 解答レコードを新規作成し得点と練習レベルを書き込む
	* 
	* answerフィールドに練習レベルを記入しておく
	* 
	* @param stNumber
	* @param score
	* @return
	*/
	public AnswerRecord createFileRecord(String stNumber, String score, String level){
		if(LOG.fa) LOG.println("■ EvalFileMethod #createFileRecord() ");
		
		File		parent	=	new File(para.kadaiAnsDir(teUid, aplec_key, kadai_key));
		if(!parent.exists()){
			boolean result	=	parent.mkdirs();
			if(LOG.fa) LOG.println("　⇒　mkdirs " + para.kadaiAnsDir(teUid, aplec_key, kadai_key ) + " is " + result);
		}
		/* 
		 * _stNumber がシステムハッシュにあることが前提
		 */
		htb.put("_stNumber", stNumber);
		String			ansPath	=	para.stFilePath(teUid, aplec_key, kadai_key, stNumber);
		AnswerRecord	r		= 	new AnswerRecord(htb);
		r.setScore(score);
		r.setAns(level);
		
		writeAnsRec(ansPath,r);
		//
		return	r;
	} 
	/**
	* 解答レコードの得点と練習レベルを更新する
	* 
	* @param stNumber
	* @param score
	* @return
	*/
	public AnswerRecord updateFileRecord(String stNumber, String score, String level){
		if(LOG.fa) LOG.println("■ EvalFileMethod #updateFileRecord() ");
		
		File		parent	=	new File(para.kadaiAnsDir(teUid, aplec_key, kadai_key));
		if(!parent.exists()){
			boolean result	=	parent.mkdirs();
			if(LOG.fa) LOG.println("　⇒　mkdirs " + para.kadaiAnsDir(teUid, aplec_key, kadai_key ) + " is " + result);
		}
	
		String		ansPath	=	para.stFilePath(teUid, aplec_key, kadai_key, stNumber);
		AnswerRecord	r 	= 	getAnsRecord(ansPath);
		r.setScore(score);
		r.setAns(level);
		
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
		if(LOG.fa) LOG.println("■ EvalFileMethod #writeAnsRec() ");
	
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
	* ◎ファイルパスからAnswerオブジェクトを読みこんで返す．
	* 
	* @param fname
	* @return			ファイルがない場合は null を返す．
	*/
	public AnswerRecord getAnsRecord(String fname){
		if(LOG.fa) LOG.println("■ EvalFileMethod #getAnsRecord() ");
	
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

}
