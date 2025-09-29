package stkadai;

import java.io.File;
import java.util.*;

import epml.Exam;
import eval.EvalFileMethod;
import framework.*;
import tktools.*;
import kadai.*;

 
/**
 *	学生用のタイピング課題およびその参照
 *
 *　ファイル提出課題の中に全て定義してあるが，ランキングリストを表示するために
 *　継承して別クラスとした．ランキングのために，練習レベルを解答ファイルに書き込む
 *　処理を追加している
 *
 	#
	# ##################
	#     StKadaiTyping
	# ##################
	#
	<program $stkadai.StKadaiTyping>
		<dispatch  html=StKadaiTyping.html  number=2445  class=stkadai.StKadaiTyping />
		<variable>
		  <receive   NUMBER STAMP GROUP StUID  StNAME  TUID aplec_key lec_key title kadai_key/>
		  <accept    CMD    UPLODE  deleteFname />
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
 * 		deleteFname --- 削除の時、ファイル名
 * 3. keep
 * 4. form
 *
 * 
 */
public class StKadaiTyping extends StKadaiFile {
    
    String	levelVal;
    String	scoreVal;
    

    public	StKadaiTyping(){
		super();
		if(LOG.fa) LOG.println("■ StKadaiTyping #コンストラクタ");
	}
	/**
	 * 提出状況書き込み
	 * 提出状況を書き込み，ダミーの解答レコードも作製する
	 * 
	 * オーバーライド
	 */
	void	writeData(){
	    setKadaiInfo(setUpdateTime(),setDisposal());
		htb.put("_updateTime",submitTime);
		createAnsRecord();
		/*
		 * タイピング練習ファイルの場合は採点も行う
		 */
        evalType();
	}
   
	/**
	 * タイピング練習ファイルを採点する
	 *
	 */
	void	evalType(){
	    if(LOG.fa) LOG.println("■ StKadaiFile #evalType() ");
	    
	    String	points	=	eval();
	    /*
	     * エラーがあれば採点しないで戻る
	     */
	    if(points==null){
	        return;
	    }
	    /*
	     * 採点結果を書き込む
	     * 課題ファイルと課題提出情報ファイルの両方を更新する
	     * 
	     * levle（入力した文字数） は answer に書かれる．
	     * EntryItem の AnswerRecord を引数にとるコンストラクタでは，
	     * levelの順にソートするようになっている．
	     */
	    EvalFileMethod	efm	=	new	EvalFileMethod(teUid, aplec_key, kadai_key, htb, para, szDB, db);
	    efm.writeEval(stNumber, points, levelVal);
	    /*
	     * 表示用のデータを再度更新する
	     */
	    setInfo();
	    
	}
	/**
	 * タイプ課題の評価点を得る
	 * エラーがあれば null を返す
	 * 
	 * @return
	 */
	String	eval(){
	    if(LOG.fa) LOG.println("■ StKadaiFile #eval() ");

	    /*
	     * 課題をパースして評価項目名と評価基準を得る
	     * 
	     */
	    Exam 	ex		= 	new Exam( rec.content(), para.getEmlConfPath());
	    ex.createHtml();
	    String	item	=	ex.getCriterionName();
	    Vector	table	=	ex.getCriterion();
	    
	    if( isEmpty(item) || (table==null) || table.size()==0){
	        return	null;
	    }
	    /*
	     * 採点評価用オブジェクトで採点する
	     */
	    EvalTyping	et	=	new	EvalTyping(submittedFilePath, item, table);

	    /*
	     * evalType() で使うので保存する 
	     */
	    scoreVal	=	et.eval();
	    levelVal	=	et.getLevel();
	    
	    if(LOG.fa){
	        LOG.println("    ★LV   =" + levelVal);
	        LOG.println("    ★得点 =" + scoreVal);
	    }
	    
	    return	scoreVal;
	}
	/**
	 * タイプ成績用ファイル名を得るためにオーバーライド
	 * 同名ファイルは上書きなので，常に <stNumber-1.sei> となる
	 * 既存ファイルがあれば消す
	 * 
	 * @param stNum
	 * @param Trailer
	 * @param dir
	 * @return
	 */
	String	getNewName(String stNum, String trailer, String dir){

	    /*
	     * 間違ったファイル拡張子の場合は，null を返す
	     * （呼び出し元は null ならばファイルを受け取らないようにしている）
	     */
	    if(!trailer.equals(EvalTyping.getExt())){
	        return	null;
	    }
	    
	    String newFile;
		newFile  = stNum + "-1" + "." + trailer;
		//
		String test  = dir + newFile;
		File   fp    = new File(test);
		if(fp.exists() ){
			fp.delete();
		}
		return newFile;
	}
	/*////////////////////////////////////////////////////////////////////////////
	 * 
	 *    表 示 処 理
	 * 
	 * 　　    以下はコントローラーが呼び出す表示メソッドである．
	 * 　　    一般には、このクラス内のメソッドから直接呼び出さない．
	 * 
	 *////////////////////////////////////////////////////////////////////////////

    public void	write(String key,Vector exHtml){

		
		if(key.equals("RepotFileList")){
			
			RepotFileList(exHtml);
			
		}else if(key.equals("RepotFile_blankBLK")){
			RepotFile_blankBLK(exHtml);
			
		}else if(key.equals("ranking")){
		    ranking(exHtml);

		}else if(key.equals("rankingList")){
		    rankingList(exHtml);
		
		}
	}
    /**
     * ランキングテーブルを表示するかどうか
     * 
     * ポストされているデータがあれば表示する
     */
    void	ranking(Vector exHtml){
		String  filePath 	= para.getKadaiPostDir(teUid,aplec_key,kadai_key);
		Files	fl			= new Files(filePath);						// 提出ファイルを処理するクラス
		if(fl.size()>0){
		    printVector(exHtml);
		}
    }
    /**
     * ランキングリスト
     * 
     * 解答ファイルに練習レベルが書き込まれているので，
     * ランキングのためには解答ファイルを見ればよい．
     * 
     * 
     * 
     * @param exHtml
     */
    void	rankingList(Vector exHtml){
        /*
         * teUid,lec_key, aplec_key, kadai_key が必要だが，
         * htb にはこれらが含まれている
         */
        Answer		ans		=	new	Answer(htb, para, db);
        TreeSet		ranking	=	new	TreeSet();
        /*
         * ランキングオブジェクトを読み出して格納する
         * ソート済みの状態になる
         */
        int	n	=	ans.getTotal();	
        for(int i=0; i<n; i++){
            ranking.add( new EntryItem( ans.getdAnsRecordAt(i) ));
        }
        /*
         * ランキング表の表示
         */
        int			order		=	0;
        String		preValue	=	"";
        Iterator	it			=	ranking.iterator();
        while(it.hasNext()){
            EntryItem	item		=	(EntryItem)it.next();
            String		stNumber	=	item.getStNumber();
            String		stName		=	item.getStName();
            String		value		=	item.getValue();
            /*
             * valueが異なっていれば order を 1 進める
             */
            if(!value.equals(preValue)){
                order++;
                preValue	=	value;
            }
            String		orderStr	=	Gear.get000type(order);	
            
            putParameter("r"		, orderStr);
            putParameter("ch"		, value);
            putParameter("stNumber"	, stNumber);
            putParameter("stName"	, stName);
            printVector(exHtml);
            
        }
    }
}
















