/*
 * 作成日: 2005/05/05
 *
 */
package jbbs;
import		java.util.*;


/**
 * 記事を並べ替えるための単位オブジェクト
 * 記事は一旦これに保持したのち，TreeSet に保持することで
 * ここで定めた順序で並ぶようになる
 */
public class BbsArticle implements Comparable {

    Hashtable	elm;
    
    public	BbsArticle(){
        
        this.elm	=	new Hashtable();
    }

    public	BbsArticle(Hashtable elm){
        
        this.elm	=	elm;
    }
    
    public	String	getLink(){
        
        String	link	=	(String)elm.get(BbsPostDB.LINK);
        return	link;
        
    }
    public	String	getLinkTop(){
        
        String	linkTop	=	(String)elm.get(BbsPostDB.LINKTOP);
        return	linkTop;
        
    }
    public	Hashtable	getElm(){
        return	elm;
    }
 
    
    /* (非 Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object arg) {

        String	link1		=	getLink();
        String	link2		=	((BbsArticle)arg).getLink();
        
        String	linktop1	=	getLinkTop();
        String	linktop2	=	((BbsArticle)arg).getLinkTop();
        
        /*
         * リンクトップキーの降順でまず並べる
         */
        if(linktop1.compareTo(linktop2)>0){
            return	-1;
            
        }else	if(linktop1.compareTo(linktop2)<0){
            return	1;
            
        }else{
            /*
             * リンクトップキーが同じもの同士では
             * リンクキーの昇順に並べる
             */
            if(link1.compareTo(link2)>0){
                return	1;
                
            }else	if(link1.compareTo(link2)<0){
                return	-1;
                
            }else{
                return	0;
            }
        }
    }

}
