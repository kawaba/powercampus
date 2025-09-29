/*
 * 作成日: 2005/05/01
 *
 */
package stkadai;
import		kadai.*;
import		tktools.*;

/**
 *　タイピングのランキングを作成するための要素オブジェクト
 *　ソーティングメソッドを持つので，TreeSet に保持される
 */
public class EntryItem extends Object implements Comparable {
    
    String	stNumber;
    String	stName;
    String	value;
    
    String	order;
    
    public	EntryItem(){
        this.stNumber	=	"";
        this.stName		=	"";
        this.value		=	"";
        this.order		=	"";
        
    }
    
    public	EntryItem(String stNumber, String stName, String value){
        this.stNumber	=	stNumber;
        this.stName		=	stName;
        this.value		=	value;
        
        this.order		=	"";
        
    }
    /**
     * 解答レコードから作成する
     * ファイル提出課題では AnswerRecord の answer フィールドは空なので，
     * value（文字数） を answer フィールドに書くことにしてある．
     * 
     * 
     * @param ans
     */
    public	EntryItem(AnswerRecord ans){
        
        this.stNumber	=	ans.getStNumber();
        this.stName		=	ans.getStName();
        this.value		=	ans.getAnswer();
        
        this.order		=	"";
    }
    
    
    /**
     * 学籍番号を得る
     * @return
     */
    public	String	getStNumber(){
        return	stNumber;
    }
    /**
     * 氏名を得る
     * @return
     */
    public	String	getStName(){
        return	stName;
    }
    /**
     * エントリーレベルを得る
     * @return
     */
    public	String	getValue(){
        return	value;
    }
    /**
     * 順位を得る(空白かもしれない）
     * @return
     */
    public	String	getOrder(){
        return	order;
    }
    
    /**
     * 順位を設定する
     * 
     * @param order
     */
    public	void	setOrder(String order){
        this.order		=	order;
        
    }
    /**
     * 比較
     * @param	o
     */
    public	int	compareTo(Object o){
        
        EntryItem	item	=	(EntryItem)o;
        /*
         * 8桁の右詰の固定長にして比較する
         */
        int			CMPLEN	=	8;
        String		val1	=	StringGear.setLength(value, CMPLEN);
        String		val2	=	StringGear.setLength(item.value, CMPLEN);
        
        int			cmp		=	val1.compareTo(val2);
        if(cmp !=0){
            /*
             * 逆順にするので符号を反転
             */
            return	-cmp;
        }
        /*
         * value が同じなら学籍番号の順とする 
         */
        cmp	=	stNumber.compareTo(item.stNumber);
        return	cmp;
    }

}
