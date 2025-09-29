package stkadai;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import epml.Exam;
import tktools.Csv;
import tktools.Gear;
import tktools.StringGear;
import framework.*;
import kadai.*;

//
/**
 * 学生用の課題参照処理<br>
 * 正解を見る処理は、Web の中に記述したjavaScript が別ウィンドウに起動する
 *
 *
 *
 *
	#
	# ######################
	#   StKadaiWritingView
	# ######################
	#
	<program $stkadai.StKadaiWritingView>
		<dispatch  html=StKadaiWritingView.html  number=2540  class=stkadai.StKadaiWritingView />
		<variable>
		  <receive   NUMBER STAMP GROUP StUID StNAME TUID  aplec_key lec_key kadai_key title/>
		  <accept    CMD     />
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
 * 3. keep
 * 4. form
 *
 * 
 */
public class StKadaiWritingView extends StKadaiExamView {

	Answer		ans;
	int			cn;		// ヒストグラムの階級の数
	Vector 		tbl;	// 階級値
	int	 	[]	cval;	// 度数
    
	public	StKadaiWritingView(){
		super();
		if(LOG.fa) LOG.println("■ StKadaiExamView #コンストラクタ");
	}
	/*
	 * イニシャライザ
	 * 
	 * 表示のためにAnswerオブジェクトをあらかじめ作成しておく
	 * 
	 *  (非 Javadoc)
	 * @see framwork.SuperPlayer#initialize(java.io.PrintWriter, java.util.Hashtable, framwork.Param)
	 */
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		
	    super.initialize(out, htb, para);
	    ans	=	new	Answer(htb, para, db);
		
    }	
	public	String	dispatch(){
		if(LOG.fa) LOG.outHash(htb,"■Sample #dispatch()");
		if(LOG.fa) LOG.println("■Sample #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		if(cmd.equals("END")){
			/*
			 * リターンする
			 */
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;
			
		}else if(cmd.equals("CLEAR")){
		    
		    if(kar.isOver()){
				/*
				 * 期限が過ぎた（起こりえる．StKougi.java では期間が過ぎると無条件に開ける．未受験の場合はここで処理）
				 */
			    putParameter(MESSAGE,"★★ 試験期間が終了しているので再受験できません");
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;
				    
			}else{		    
			    /*
			     * 受験データを消去する（再受験可能にする）
				 * 課題提出情報の削除＋提出した課題ファイルも削除
				 */
				delKadaiFile();
				delInfo();
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_RETURN;	// 再表示		    
		    
			}
		}else{
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
		}
		/*
		 * 次に表示するときあるいはリターンしてきた時の表示モードをセットしてからリターンする
		 */
		putParameter(DISP_KEY,disp_mode);
		return	ret;
    }
	/**
	* 提出した課題ファイルの削除
	* 
	*/
	void	delKadaiFile(){
		ans.deleteAnsRecord(stNumber);
	}
	/**
	* 特定の学生のこの課題提出情報を削除する<br>
	* 削除すると再提出が可能になる
	*/
	void	delInfo(){
		KadaiInfo kdf		=	new KadaiInfo(szDB,db);
		kdf.set_keys(stNumber,te_aplec_key,kadai_key);
		kdf.delete_A_KadaiInfo();

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

		
		if(key.equals("List")){
			List(exHtml);
			
		}else if(key.equals("histogram")){
		    histogram(exHtml);

		}else if(key.equals("rankingList")){
		    rankingList(exHtml);
		
		}
	}
    /**
     * テーブルを表示するかどうか
     * 
     * 解答データがあれば表示する
     */
    void	List(Vector exHtml){
        
        int	n	=	ans.getTotal();
        if(n>0){
            printVector(exHtml);
        }
    }
    /**
     * ランキングリスト
     * 
     * 解答ファイルの項目 f1 に入力文字数が書き込まれている．
     * ランキングのためには解答ファイルを見ればよい．
     * 
     * @param exHtml
     */
    void	rankingList(Vector exHtml){
        
        /*
         * ヒストグラムを初期化しておく
         */
        initHistogram();
        /*
         * EntryItem はComparableインターフェースを持つので
         * TreeSetに格納するとソート済みの状態になる
         */
        TreeSet		ranking	=	new	TreeSet();
        int			n		=	ans.getTotal();	
        for(int i=0; i<n; i++){
            /* 
             * rec.getF1() は入力文字数を格納した項目から値を得る
             */
            AnswerRecord	rec		=	ans.getdAnsRecordAt(i);
            String			strlen	=	rec.getF1();
            /*
             * 過渡的な処理
             */
            if(isEmpty(strlen)){
                strlen	=	getLength(rec.getAnswer());
            }
            /*
             * 60 点未満は番号のみ表示
             */
            String	nameStr	=	rec.getStName();
            int		score	=	0;
            try{
                score	=	Integer.parseInt(rec.getScore());
            }catch(NumberFormatException e){
                score	=	0;
            }
            if(score<60){
                nameStr	=	" * * * ";
            }
            ranking.add( new EntryItem( rec.getStNumber(), nameStr, strlen ));
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
             * ヒストグラムにvalue（文字数）を登録する
             * 表示は後で histogram() が実行するのでここではデータを作成するだけ
             */
            setHistogram(value);
            
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
    /**
     * ヒストグラムの階級ベクトルを得る
     * @return
     */
    void	initHistogram(){
        /*
         * ヒストグラムの階級(cv)＝評価基準を得るためHTMLを作成させる
         */
		String	pmlText	=	(new KadaiDefRecord(te_lec_key,kadai_key,db)).content();
		Exam	exam	=	new Exam(pmlText, para.getEmlConfPath());
		exam.createHtml();
        tbl				=	exam.getCriterion();
        /*
         * ヒストグラム用変数を初期化する
         * 
         * cn に +1 しているのは最上位の階級値「それ以上」が
         * cv に含まれていないため
         */
        cn		=	tbl.size();
        cval	=	new	int[cn+1];
        
        for(int i=0; i<cn+1; i++){
            cval[i]	=	0;
        }
        
    }
    String	getLength(String ansText){
	    /*
	     * 課題をパースしてハッシュにセットし，入力したテキストだけを得る
	     */
	    Exam 	ex		= 	new Exam( ansText, para.getEmlConfPath());
	    ex.doEPML();
	    ex.setHash(htb);
	    String	text	=	getItem("ETA(1)");
	    /*
	     * 文字数を計算して返す
	     */
	    int		chars	=	StringGear.howManyChar(text);
	    return	String.valueOf(chars);
	    
    }
    /**
     * ヒストグラムの階級に文字数を登録する
     * @param a		文字数
     */
    void	setHistogram(String a){
        
        int	order	=	getOrder(a);
        cval[order]++;
        
    }
    /**
     * 実績値に対応する階級値の番号を得る(0～cn)
     * 
     * @param levelStr  入力した文字数
     * @return
     */
    public	int	getOrder(String	levelStr){
        /*
         * 比較に使う文字列の長さ
         */
        int			LENGTH	=	8;
        
        String		level	=	StringGear.setLength(levelStr, LENGTH); // 評価対象の文字数
        String		lv		=	"";
        int			pos		=	0;
        /*
         * 最初は階級値（0 ～ cn）の最大番号としておく
         */
        int		score	=	cn;

        for(int i=0; i<cn; i++){
            /*
             * 上位のレベルから下へ向かって比較していく
             */
            Csv		cs	=	new	Csv( (String)tbl.get(cn-i-1));
            lv			=	StringGear.setLength(cs.get(0), LENGTH); // 階級値（文字数）
            pos			=	cn-i-1;									 // 階級値の何番目か
            int		cmp	=	level.compareTo(lv);	// 負，０，正
            
            if(cmp > 0){
                if(LOG.fa){
                    LOG.println("             lv=" + lv);
                    LOG.println("            pos=" + pos);
                    LOG.println("          level=" +level);
                }
                break;
                
            }else if(cmp == 0){
                score	=	pos;
                if(LOG.fa){
                    LOG.println("             lv=" + lv);
                    LOG.println("            pos=" + pos);
                    LOG.println("          level=" +level);
                }
                break;
                
            }else{
                score	=	pos;
                if(LOG.fa){
                    LOG.println("             lv=" + lv);
                    LOG.println("            pos=" + pos);
                    LOG.println("          level=" +level);
                }
                
            }
        }
        return	score;
        
    }
    
    void	histogram(Vector exHtml){
        
        int		max		=	cval.length-1;
        String	rank	=	getLevel((String)(tbl.get(max-1))) + "文字超";
        int		cnt		=	cval[cval.length-1];
        int		sum		=	cnt;
        //
        putParameter("rank", rank);
        putParameter("cnt",  String.valueOf( sum ));
        putParameter("sum",  String.valueOf( sum ));
        printVector(exHtml);
        
        for(int i=0; i<cval.length-1; i++){
            rank	=	getLevel((String)(tbl.get(max-i-1))) + "文字まで";
            cnt		=	cval[max-i-1];
            sum		+=	cnt;
            
            putParameter("rank", rank);
            putParameter("cnt",  String.valueOf(cnt));
            putParameter("sum",  String.valueOf(sum));
            printVector(exHtml);
        }
       
    }
    
	String	getLevel(String lv){
	    Csv	cs	=	new	Csv(lv);
	    return	cs.get(0);
	    
	}
}
