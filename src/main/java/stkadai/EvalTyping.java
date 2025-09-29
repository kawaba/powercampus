/*
 * 作成日: 2005/04/30
 *
 */
package stkadai;
import		java.util.*;

import framework.LOG;
import		tktools.*;
/**
 * 評価クラス
 * 　タイピング課題の評価を行うクラス
 * 　オーバーライド可能
 * 
 *	評価基準表から得点を求めるためのクラス
 *	getLevel()をオーバーライドすると他のタイピングソフトに対応できる
 *  また，ext　に適当な拡張子を再設定する必要がある
 *
 */
public class EvalTyping extends Object{

    String	path;		// 提出ファイルへの完全パス
    String	title;		// 評価項目名
    Vector	table;		// 評価基準データ
    
    static	String	ext	=	"sei";
    
    String	levelValue	=	null;	// ファイルから得た実績文字数
    String	scoreValue	=	null;	// 得点
    
    public	EvalTyping(String path, String title, Vector table){
        this.path		=	path;
        this.title		=	title;
        this.table		=	table;
        
    }
    /**
     * ファイル拡張子を返す
     * 
     * 
     * @return
     */
    public	static	String	getExt()	{	return	ext;	}
    
    
    /**
     * 評価基準表から得点を求める
     * 
     * @return		得点
     */
    public	String	eval(){
        if(LOG.fa) LOG.println("■ EvalTyping #eval() ");
        /*
         * ファイル内容をベクターにロードする
         * 長さ０の行データは無視される
         */
        Vector	v			=	new	Vector(100);
        FileGear.loadToVector(path,v);	
        /*
         * 実績レベルを求めて，得点を得る
         */
        levelValue		=	getLevel(v);
        scoreValue		=	getScore(levelValue);
        
        return	scoreValue;
    }
    /**
     * 実績値（練習で到達した値）を得る
     * @return
     */
    public	String	getLevel(){
        if(Gear.isEmpty(levelValue)){
            Vector	v		=	new	Vector(100);
            FileGear.loadToVector(path,v);	
            levelValue		=	getLevel(v);
        }
        return	levelValue;
        
    }
    /**
     * 実績値に対応する得点を得る
     * @param level
     * @return
     */
    public	String	getScore(String	levelStr){
        /*
         * 比較に使う文字列の長さ
         */
        int			LENGTH	=	8;
        
        String		level	=	StringGear.setLength(levelStr, LENGTH);
        String		lv		=	"";
        String		pt		=	"";
        /*
         * 最初は満点としておく
         */
        String		score	=	"100";

        int			n		=	table.size();
        for(int i=0; i<n; i++){
            /*
             * 上位のレベルから下へ向かって比較していく
             */
            Csv		cs	=	new	Csv( (String)table.get(n-i-1));
            lv			=	StringGear.setLength(cs.get(0), LENGTH);
            pt			=	cs.get(1);
            int		cmp	=	level.compareTo(lv);	// 負，０，正
            
            if(cmp > 0){
                if(LOG.fa){
                    LOG.println("             lv=" + lv);
                    LOG.println("             pt=" + pt);
                    LOG.println("          level="+level);
                }
                break;
                
            }else if(cmp == 0){
                score	=	pt;
                if(LOG.fa){
                    LOG.println("             lv=" + lv);
                    LOG.println("             pt=" + pt);
                    LOG.println("          level="+level);
                }
                break;
                
            }else{
                score	=	pt;
                if(LOG.fa){
                    LOG.println("             lv=" + lv);
                    LOG.println("             pt=" + pt);
                    LOG.println("          level="+level);
                }
                
            }
        }
        return	score;
        
    }
    /**
     * 文字列タイプの実数表現から小数点以下を取り去ったものを返す
     * @param lv
     * @return
     */
    String	getInt(String lv){
        Csv	cs	=	new	Csv(lv,".");
        return	cs.get(0);
        
    }
    /**
     * 提出された成績ファイルを読んで，評価項目に対する
     * 実績値（練習で到達した値）を得る
     * 一般にはこれをオーバーライドしてクラスを作成する
     * 
     * Vector		v 		練習データを行単位にベクターに読み込んだもの
     * @return
     */
    public	String	getLevel(Vector	v){
        if(LOG.fa) LOG.println("■ EvalTyping #getLevel() ");

        /*
         * 練習データファイル( mikatype.sei )の記述
         * 
         *	練習時間　    0時間 6分 3秒
         *	ポジション練習                                         0時間 1分26秒
         *	ホームポジション                      0.0              0時間 0分 0秒
         *	上一段                                0.0              0時間 0分 0秒
         *	ホームポジション＋上一段              0.0              0時間 0分 0秒
         *	下一段                                0.0              0時間 0分 0秒
         *	ホームポジション＋下一段              0.0              0時間 0分 0秒
         *	ホームポジション＋上一段＋下一段      0.0              0時間 0分 0秒
         *	数字                                  0.0              0時間 0分 0秒
         *	全段                                 69.0 05/04/29     0時間 1分 0秒
         *	基本英単語練習                        0.0              0時間 0分 0秒
         *	ＭＳＤＯＳコマンド練習                0.0              0時間 0分 0秒
         *	Ｃ言語練習                            0.0              0時間 0分 0秒
         *	パスカル練習                          0.0              0時間 0分 0秒
         *	フォートラン練習                      0.0              0時間 0分 0秒
         *	ＢＡＳＩＣ練習                        0.0              0時間 0分 0秒
         *	８０８６アセンブラ練習                0.0              0時間 0分 0秒
         *	ローマ字ランダム練習                  0.0              0時間 0分24秒
         *	ローマ字単語練習                    111.0 05/04/29     0時間 1分 0秒
         */
        
        String	lv		=	"";
        int		n		=	v.size();
        
        for(int i=0; i<n; i++){
            /*
             * 半角空白をデリミタとして要素を切りだす
             */
            String	line		=	(String)v.get(i);
            StringTokenizer	stk	=	new	StringTokenizer(line, " ");
            if(stk.countTokens()<2){
                continue;
            }
            /*
             * 第一要素が指定項目と等しいときそのレベル（第２要素）を得る
             */
            String	item	=	stk.nextToken();
            if(item.equals(title)){
                lv	=	getInt(stk.nextToken());
                break;
            }
        }
        return	lv;
    }
    /*
    public	static	void	main(String arg[]){
        
        String	s	=	EvalTyping.setLength("120",10);
        System.out.println(":" + s + ": (" + s.length() + ")" );
    }
    */
    
}
