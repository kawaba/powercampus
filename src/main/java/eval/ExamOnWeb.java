
package eval;
import framework.LOG;
import stkadai.AnsweredEPML;

/**
 * 
 *	EvalWeb では学生のレポート課題を採点するが、ここでは試験問題データを採点する。
 *　元データはどちらもPMLであることに変わりは無いので、EvalWeb を拡張して、表示
 *　データ作成メソッドのみ試験問題用に変更している。
 *
 *  なおこのクラスは採点を修正したり、学生の解答を見たりするためのもので、自動
 *  採点はできない。
 *
 *
 *
 	#
	# ##################
	#     ExamOnWeb
	# ##################
	#
	<program $eval.ExamOnWeb>
		<dispatch  html=EvalWeb.html  number=1560  class=eval.ExamOnWeb />
		<variable>
		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA HOMEURL DIVISION
		             title lec_key aplec_key kadai_key initRecNo />
		  <accept    CMD    />
		  <keep      recNo stNumber />
		  
		  <form      sortMode search/>
		</variable>
	</program>
 *
 *
 * 変数の説明
 *
 * 1. receive 
 * 		initRecNo	--　最初に表示するレコードの番号
 * 						TEXT モードでの表示から復帰したときのみ値がある
 * 2. accept
 * 3. keep
 * 		recNo		-- 現在対象にしている提出ファイルのファイル番号
 * 
 * 4. form
 *		sortMode	-- ソート状態（"ON"なら提出時間順）デフォルトは番号順
 * 
 */
public class ExamOnWeb extends EvalWeb{
	
	
	public	ExamOnWeb(){
		super();
		if(LOG.fa) LOG.println("■ Sample #コンストラクタ");
	
	}
	
	@Override
	public	String	setHtml(){
		
		AnsweredEPML	aepml	=	new	AnsweredEPML(broker, para, szDB, getParameter("stNumber"), teUid, lec_key, aplec_key, kadai_key);
		
		String			html	=	aepml.getAnswerHtml();
		return	html;
	}	
}
