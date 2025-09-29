/*
*
*

*/
package student;

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Vector;

import kamoku.KamokuDefRecord;
import kamoku.KamokuItem;
import kamoku.KamokuParser;
import tktools.FileGear;
import tktools.TemplateBox;
import tktools.TextToken;
import xmlparser.xmlException;

import database.Database;
import database.DbConnectionBroker;

import framework.*;

/**
*
*
	#
	# ##################
	#   StSyllabus
	# ##################
	#
	<program $student.StSyllabus>
		<dispatch  html=StSyllabus.html  number=5400  class=student.StSyllabus />
		<variable>
		  <receive   GROUP  NUMBER STAMP StUID StCLASSINFO StNAME StMAIL StKEITAI  TUID  lec_key/>
		  <accept    CMD     />
		  <keep      />
		  
		  <work      />
		  <form      />
		</variable>
	</program> 
*/
public class StSyllabus extends SuperPlayer{

	/* ************ 
	 *  作業用変数
	 **************/
	/**
	 *  プログラムリターンコード 
	 */
	String		ret;
	/**
	 * 次の処理での表示モード 
	 */
	String		disp_mode;
	/**
	 *  処理分岐コード
	 */
	String 		cmd;
	/**
	 * データベースプール
	 */
	DbConnectionBroker	broker;
	/**
	 * ユーザー検索のためのクラス
	 */
	Database	db;
	
	String		szDB;
	String		teUid;
	String		lec_key;
	
	public	StSyllabus(){
		super();
		if(LOG.fa) LOG.println("■ StSyllabus #コンストラクタ");
	}	

	/**
	 * コンストラクタが引数をもてないので、コンストラクタで行う内容をこの initialize() に書く．
	 * out, htb, para は SuperPlayer クラスのインスタンス変数なので、
	 * 引数にとらずとも利用できるが、変数を明示する意味で列挙してある
	 * 
	 * @param out			出力ポインタ
	 * @param htb			システムハッシュ
	 * @param para			パラメータ
	 */
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		broker	=	getDbConnection();
		db		=	new Database(broker);
		
		szDB	=	getParameter(GROUP);
		teUid	=	getParameter(TUID);
		lec_key	=	getParameter("lec_key");

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
		if(LOG.fa) LOG.println("■ StSyllabus #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		if(cmd.equals("RETURN")){
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;	// 復帰			

		}else{
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示
		}
		/*
		 * 次に表示するときあるいはリターンしてきた時の表示モードをセットしてからリターンする
		 */
		putParameter(DISP_KEY,disp_mode);
		return	ret;
	}
	
	/*////////////////////////////////////////////////////////////////////////////
	 * 
	 *    表 示 処 理
	 * 
	 * 　　    以下はコントローラーが呼び出す表示メソッドである．
	 * 　　    一般には、このクラス内のメソッドから直接呼び出さない．
	 * 
	 *////////////////////////////////////////////////////////////////////////////
    
	/**
	 * 出力処理
	 */
	public  void	display(boolean editmode){
		if(LOG.fa) LOG.println("■ StSyllabus #display()");
		
		if(!editmode){
			htb.put(MESSAGE,"");
		}
		
		/* strHash(htb,DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);	
	}

	public	void	write(String key){
		if(LOG.fa) LOG.println("■ StSyllabus #write()");

		if(key.equals("line")){
			line();
			
		}else if(key.equals("kamokuDsp")){
			kamokuDsp();

		}else if(key.equals("items")){
			items();
			
		}
	}
	/**
	 * 区切り線を引く
	 *
	 */
	void	line(){
		if(LOG.fa) LOG.println("■ StSyllabus #line()");
		
		// lec_keyがnullであればまだインポート前である．
		// 初期表示処理ではラインは表示しない．
		if(lec_key==null){
			return;	
		}
		TemplateBox	tbox	=	new	TemplateBox(para.kamokuTemplatePath());
		Vector		v		=	tbox.getVector("line");
		printVector(v);
		
	}	
	void	kamokuDsp(){
		if(LOG.fa) LOG.println("■ StSyllabus #kamokuDsp()");

		// lec_keyがnullであればまだインポート前である．
		// 初期表示処理ではシラバスは表示しない．
		if(lec_key==null){
			return;	
		}
		TemplateBox	tbox	=	new	TemplateBox(para.kamokuTemplatePath());
		Vector		v		=	tbox.getVector("kamokuDsp");
		printVector(v);		
	}
	/**
	 * 科目シラバスの各項目を出力する
	 *
	 */
	void	items(){
		if(LOG.fa) LOG.println("■ StSyllabus #items()");
		
		// lec_keyがnullであればまだインポート前である．
		// 初期表示処理では科目シラバスは表示しない．
		if(lec_key==null){
			return;	
		}
		// データベースから講義定義レコードを読み込み、content(講義シラバスXML)を得る
		KamokuDefRecord	kdrec	= 	new KamokuDefRecord(teUid, lec_key, db);
		String	xml				=	kdrec.content();
		
		// xmlから科目パーサを作成し定義情報を順次出力する
		KamokuParser	kp		=	getKamokuParser(xml, kdrec);
		if(kp==null){
			putParameter(MESSAGE, "★科目定義XMLに誤りがありましたのでシステムのXMLファイルを代用しました．");
			alart(kdrec);
			return;
			
		}else{
			printList(kp);
		}
	}
	void	printList(KamokuParser	kp){
		if(LOG.fa) LOG.println("■StSyllabus #printList()");		
		
		TemplateBox	tbox	=	new	TemplateBox(para.kamokuTemplatePath());
		while(kp.hasNext()){
			KamokuItem	ki	=	(KamokuItem)kp.next();
			putParameter("label"	, ki.getLabel());
			
			if(ki.isTextArea() || ki.isTextField()){
				putParameter("content"	, ki.getText());
				Vector	v	=	tbox.getVector("viewTemplate01");
				printVector(v);
				
			}else if(ki.isListBox() || ki.isCheckBox()){
				/*
				 * 選択肢のうち、先頭に＊が付いているもののみを取り出して連結する
				 * ＊はその項目が選択されたことを意味する
				 */
				TextToken		tkn		=	new TextToken(ki.getText());
				String			item	=	"";
				StringBuffer	buf		=	new	StringBuffer();
				boolean		spcflag	=	false;
				while((item=tkn.getNext())!=null){
					if(item.charAt(0)=='*'){
						if(spcflag){
							buf.append(" ");
						}
						buf.append(item.substring(1));
						spcflag	=	true;
					}
				}
				putParameter("content"	, buf.toString());
				
				Vector	v	=	StringToVector(tbox.get("viewTemplate02"));
				printVector(v);
			}
		}
		
	}    

	/**
	 * 所余のxmlから科目パーサーを作成して返す<br>
	 * @param xml
	 * @param kdrec
	 * @return
	 */
	KamokuParser	getKamokuParser(String	xml, KamokuDefRecord kdrec){
		if(LOG.fa) LOG.println("■ StSyllabus #getKamokuParser()");

		KamokuParser	kp	=	null;
		try{
			kp	=	new	KamokuParser(xml);
			
		}catch(xmlException e){
			return null;
			
		}
		return	kp;
		
	}
	/**
	 * 何らかの原因で受け取ったデータのXMLが不正なものだった場合、
	 * 現在の新しいシラバスXML（値は未記入）を content にセットしてデータベースを更新する
	 * それにも誤りがあれば、システムのXMLをcontent にセットしてデータベースを更新する
	 */
	void	alart(KamokuDefRecord kdrec){
		if(LOG.fa) LOG.println("■ StSyllabus #alart()");

		try{
			kdrec.set_content( para.getKamokuXml(szDB, teUid) );
			
		}catch(xmlException e){
			putParameter(MESSAGE, "★システムの科目定義XMLが正しい記述ではありませんのでシステムのXMLを代用しました．<br>" + e.getMessage());
			kdrec.set_content( FileGear.getFileData(para.syllabusPath()) );
		}
		kdrec.update(db);
		
	}	

}

