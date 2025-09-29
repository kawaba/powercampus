package kamoku;
import  tktools.*;
import java.io.*;
import java.util.*;

import database.Database;
import database.DbConnectionBroker;
import framework.*;
/**
 * 
 * 科目を時間割に割り付けて講義を生成する
 * また、時間割からの削除も行なう
 * 
 * 
 * 
  	#
	# ##################
	#   Assignment
	# ##################
	#
	<program $kamoku.Assignment>
		<dispatch  html=assign.html  number=230  class=kamoku.Assignment />
		<variable>
		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA shubetsu worder wdate  aplec_key/>
		  <accept    CMD    UPLODE  />
		  <keep      />
		  
		  <form      assign />
		</variable>
	</program> 
 *
 *　assign はチェックボックスの値で、
 *    <input name="_assign" type="radio" value="%_lec_key%:%_titleItem%" %_checked%>
 *  となっている．値にlec_keyと科目名の合成文字列を持つ．
 *
 */
public class Assignment  extends SuperPlayer {

	/* ************ 
	*  作業用変数
	**************/
	/**
	*  プログラムリターンコード 
	*/
	String			ret;
	/**
	* 次の処理での表示モード 
	*/
	String			disp_mode;
	/**
	*  処理分岐コード
	*/
	String 			cmd;	
	/**
	* 教師情報
	*/
	Database 		db;
	/**
	* 教師ユーザ名
	*/
	String			teUid;
	/**
	 * 講義実施キー
	 */
	String			aplec_key;
	//
	public	Assignment(){
		super();
		if(LOG.fa) LOG.println("■ Timetable #コンストラクタ");
	}	
	/**
	* コンストラクタが引数をもてないので、コンストラクタで行う内容をこの initialize() に書く．
	* out, htb, para は SuperPlayer クラスのインスタンス変数なので、ここで値を渡す
	* 
	* @param out			出力ポインタ
	* @param htb			システムハッシュ
	* @param para			パラメータ
	*/
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		super.setInit(out, htb, para);
		
		teUid		= 	getParameter(TUID);
		aplec_key	=	getParameter("aplec_key");
		
		db			=	new Database((DbConnectionBroker)htb.get(BROKER));
	}
	/**
	 * 受け入れコードにより処理を分岐させるメソッド．
	 * 受け入れコードは getParameter(CMD) で受け取る
	 * 
	 * 終了コードとして次に起動したいプログラムエイリアス（論理プログラム名）を指定する
	 * 論理プログラム名は dispatch.xml に指定したものを使う
	 * 
	 * 自分自身の画面を再表示したい場合は終了コードに DISPATCH_DEFAULT を指定する
	 * 呼び出しプログラムへ復帰するには終了コードに   DISPATCH_RETURN  を指定する
	 *
	 * disp_mode とは
	 * 　　次にこの画面を表示するときの表示モード(DISP_NEW=新規表示、DISP_EDIT=編集表示)．
	 * 　　あるいは次に起動するプログラムで使われる表示モード
	 *　（注）
	 * 　　他のプログラムが終了して、DISPATCH_RETURN でこのプログラムが呼び出される時は 
	 * 　　コントローラーにより display(boolean disp_mode) へ直接復帰する．
	 * 　　この場合、disp_mode はコントローラーによってDISP_EDIT に設定されている 
	 */	
	public	String	dispatch(){
		if(LOG.fa) LOG.outHash(htb,"■Assignment #dispatch()");
		if(LOG.fa) LOG.println("■Assignment #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		if(cmd.equals("UPDATE")){
			/*
			 * assign は lec_key と title の合成文字列を値として持つ
			 */
			String	lec_key_and_title	= getParameter("assign");
			// 選択があれば
			if(lec_key_and_title!=null){
				Csv cs = new Csv(lec_key_and_title,":");
				String lec_key	= cs.get(0);
				String title	= cs.get(1);
				/*
				 * レコードの存在を確かめてあれば書き換え、なければ挿入する
				 */
				writeAplec(lec_key,title);
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_RETURN;// 復帰			

			}else{
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;// 再表示	
			}
			
		}else if(cmd.equals("CLEAR")){
			deleteAprec();
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;// 復帰	

		}else if(cmd.equals("RETURN")){
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;// 復帰			

		}else{
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;// 再表示				
	
		}
		/*
		 * 次に表示するときあるいはリターンしてきた時の表示モードをセットしてからリターンする
		 */
		putParameter(DISP_KEY,disp_mode);
		return	ret;
	}	
	/**
	 * 講義を登録する
	 * @param lec_key
	 * @param title
	 */
	void writeAplec(String lec_key,String title){
		if(LOG.fa) LOG.outHash(htb,"writeAplec()の先頭です");
		if(LOG.fa) LOG.println("           lec_key = " + lec_key);
		if(LOG.fa) LOG.println("           title   = " + title);
		//
		String	shubetsu	= getParameter("shubetsu");
		String	wdate		= getParameter("wdate");
		String	worder		= getParameter("worder");
		//
		KamokuApRecord kar	= new KamokuApRecord();
		kar.set_teuid(teUid);
		kar.set_aplec_key(aplec_key);
		kar.set_lec_key(lec_key);
		kar.set_shubetsu(shubetsu);
		kar.set_wdate(wdate);
		kar.set_worder(worder);
		kar.set_title(title);
		//
		KamokuApRecord	temp = new KamokuApRecord(teUid,aplec_key,db);	// レコードを検索してみる
		if(!temp.isEmpty()){
			kar.update(db);
		}else{
			kar.insert(db);
		}
	}
	/**
	 *講義を削除する（科目割り付けの解除で呼ばれる） 
	 *
	 */
	void deleteAprec(){
		if(LOG.fa) LOG.outHash(htb,"deleteAprec()の先頭です");
		/*
		 * KamokuDelete.delete_a_apps() はひとつの講義実施クラスだけを削除する
		 */
		String			szDB	= 	getParameter(GROUP);
		KamokuDelete	kdel	=	new KamokuDelete(db);
		kdel.delete_a_apps(teUid, aplec_key, szDB, para);

	}
	////////////////////////////////////////////////////////////////////////////////////////////////////	/
	//
	//	出　　力　　処　　理
	//
	////////////////////////////////////////////////////////////////////////////////////////////////////	/
	/**
	 *  画面を表示する
	 */
	public	void	display(boolean	editmode){
		if(LOG.fa) LOG.outHash(htb,"display_assignKougi()の先頭です");

		if(!editmode){
			htb.put(MESSAGE,"");
		}
		KamokuApRecord	kar	= new KamokuApRecord(teUid,aplec_key,db);	// レコードを検索してみる
		String chk	= "*";
		if(!kar.isEmpty()){
			chk = kar.lec_key();// 割付があればchk にはlec_keyが入る
		}
		putParameter("chk",chk);

		/* strHash(htb,DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);	
	}
	/**
	 * ブロックの出力
	 */
	public void write(String key,Vector exHtml){
	   
		if(key.equals("assign_list")){
			outputList(exHtml);
			
	   }else{
	   		outputEmpty(exHtml);
			
	   }
	}
	/**
	 * 科目リストの表示
	 * @param exHtml
	 */
	void	outputList(Vector exHtml){
		
		if(LOG.fa) LOG.println("科目割付画面を表示します");
		//
		String 		teUid	= getParameter(TUID);
		KamokuDEF	kd		= new KamokuDEF(teUid, db);
		int			n		= kd.size();
		String chk = getParameter("chk");
		for(int i=0; i<n; i++){
			KamokuDefRecord	kdr	= kd.get(i);
			//
			putParameter("k"		 	,	String.valueOf(i+1) );
			putParameter("lec_key"		,	kdr.lec_key() );		// 科目キー
			putParameter("titleItem" 	,	kdr.title()	   );		// 科目名
			//putParameter("contentItem"	,	kdr.content()  );		// 概要
			//
			// ラジオボタンの表示
			if(chk.equals( kdr.lec_key() )){
				putParameter("checked","checked");
			}else{
				putParameter("checked","");
			}
			//
			printVector(exHtml);
		}
		/*
		 * 出力件数を記録しておく
		 */
		putParameter("output", String.valueOf(n));
		
	}
	/**
	 * カラリストの表示
	 *
	 */
	void	outputEmpty(Vector exHtml){
		String	output	=	getParameter("output");
		if(output.equals("0")){
			printVector(exHtml);
		}
	}
}

