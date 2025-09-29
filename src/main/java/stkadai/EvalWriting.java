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
public class EvalWriting extends Object{

    String	inStr;		// 入力した文字列
    String	title;		// 評価項目名
    Vector	table;		// 評価基準データ
    
    String	levelValue	=	null;	// ファイルから得た実績文字数
    String	scoreValue	=	null;	// 得点
    
    public	EvalWriting(String inStr, String title, Vector table){
        this.inStr		=	inStr;
        this.title		=	title;
        this.table		=	table;
        
        eval();
        
    }
    
    /**
     * 実績値（練習で到達した値）を得る
     * @return
     */
    public	String	getLevel(){

        return	levelValue;
        
    }
    /**
     * 評価点を得る
     * @return
     */
    public String	getScore(){
        return	scoreValue;
    }
    
    
    /**
     * 評価基準表から得点を求める
     * 
     * @return		得点
     */
    public	void	eval(){
        if(LOG.fa) LOG.println("■ EvalTyping #eval() ");
        /*
         * 評価レベル＝文字数とする
         */
        int	counts	=	StringGear.howManyChar(inStr);
        levelValue	=	String.valueOf(counts);
        /*
         * 評価点を得る
         */
        scoreValue		=	setScore(levelValue);

    }
    /**
     * 実績値に対応する得点を得る
     * @param levelStr  入力した文字数
     * @return
     */
    public	String	setScore(String	levelStr){
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

}
