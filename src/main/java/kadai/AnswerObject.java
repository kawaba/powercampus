/*
 * 作成日: 2005/03/18
 *
 * TODO
 */
package kadai;

import database.DbConnectionBroker;
import	java.io.*;

/**
 *
 */
public class AnswerObject implements KadaiVar{
	
	/**
	 * データベースプール
	 */
	DbConnectionBroker	broker;
	/**
	 * DB検索のためのクラス
	 */
	AnswerDB	db;
	/**
	 * グループ名
	 */
	String		szDB;
	/**
	 * 解答ファイルのあるディレクトリ
	 */	
	String		dir;
	File		fp;
	/*
	 * キー
	 */
	String	stNumber;
	String	te_aplec_key;
	String	kadaikey;
	

	public	AnswerObject(){
		
	}
	public	AnswerObject(DbConnectionBroker broker, String	szDB, String dir){
		this.szDB		=	szDB;
		this.dir		=	dir;
		this.broker		=	broker;
		db				=	new AnswerDB(broker);
		
		File	fp		=	new	File(dir);
		
		
		
		
	}
	public	void	setKeys(String stNumber, String te_aplec_key, String kadaikey){
		this.stNumber		=	stNumber;
		this.te_aplec_key	=	te_aplec_key;
		this.kadaikey		=	kadaikey;
		
	}
	/**
	 * レコード１件記録する
	 *
	 */
	public	void write(AnswerRecord r){
		
		/*
		 * 解答ファイルを保存する
		 */
		writeAnsRec(r);

		/*
		 * データベースに初期データを記録する
		 */
		
		
		
		
	}
    /**
	 * 解答レコードを書き込む
     *
     * @param path
     * @param r
     * @return
     */
	public boolean  writeAnsRec(AnswerRecord r){
        ObjectOutputStream  objOut  = null;
        try{
            objOut = new ObjectOutputStream(new FileOutputStream(fp));
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
	
	
	
	
	
	
	
	
	
}
