/*
 * 作成日: 2005/02/17
 *
 * TODO
 */
package kamoku;

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Vector;
import database.Database;
import database.DbConnectionBroker;
import framework.LOG;
import framework.Param;
import framework.SuperPlayer;
import tktools.FileGear;
import tktools.StringGear;
import tktools.TemplateBox;
import xmlparser.xmlException;
/**
 *
 */
public class KamokuConvert extends SuperPlayer  implements KamokuVar{

	String		szDB;
	String		teUid;
	String		tname;
	Database	db;
	
	/**
	 * データベースプール
	 */
	DbConnectionBroker	broker;
	
	KamokuDEF	kd;
	int			size;
	
	public	KamokuConvert(){
		super();
	}
	/* (非 Javadoc)
	 * @see framwork.SuperPlayer#initialize(java.io.PrintWriter, java.util.Hashtable, framwork.Param)
	 */
	@Override
	public void initialize(PrintWriter out, Hashtable htb, Param para) {
		
		setInit(out,htb,para);
		
		// TODO 自動生成されたメソッド・スタブ
		this.szDB	=	getParameter(GROUP);
		this.teUid	=	getParameter(TUID);
		this.tname	= 	getParameter(TNAME);
		
		broker		=	getDbConnection();
		db			=	new Database(broker);
		
	}

	/* (非 Javadoc)
	 * @see framwork.SuperPlayer#dispatch()
	 */
	@Override
	public String dispatch() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	/* (非 Javadoc)
	 * @see framwork.SuperPlayer#display(boolean)
	 */
	@Override
	public void display(boolean editmode) {
		// TODO 自動生成されたメソッド・スタブ

	}	
	public	KamokuConvert(String szDB, String teUid, String tname, PrintWriter out, Hashtable ht, Param para, Database db){
		
		this.szDB	=	szDB;
		this.teUid	=	teUid;
		this.tname	= 	tname;
		this.db		=	db;
		this.para	=	para;
		
		
		kd			=	new	KamokuDEF(teUid, db);
		size		=	kd.size();
		
	}

	public	void	convertAll(){
		
		for(int	 i=0; i<size; i++){
			
			KamokuDefRecord	kdr	=	kd.get(i);
			String			xml	=	convert(kdr);
			kdr.set_content(xml);
			kdr.set_bikou( UPDATED_MARK );
			kdr.update(db);
			
		}
	}
	
	public	String	convert(KamokuDefRecord kdr){
		
		KamokuParser	kp	=	getSyllabus();		
		String			xml	=	updateXml(kp, kdr);
		return			xml;
		
	}
	
	/**
	 * 所与の科目定義XMLデータとデータベース中の値から、科目定義XMLを更新して生成する
	 * 
	 * @param xml
	 */
	public	String	updateXml(KamokuParser kp, KamokuDefRecord kdr){
		if(LOG.fa) LOG.println("KamokuEdit #updateXml()");
		
		StringBuffer	buf		=	new	StringBuffer(2048);	// 新しいXML
		TemplateBox		tb		=	new	TemplateBox(para.kamokuTemplatePath());
		buf.append(tb.get("headderXml"));
		/*
		 * <kamoku serial=_number> という科目タグを読み、_number を実際の serial に書き換える
		 */
		String	kamokuTag		=	StringGear.substitute(tb.get("kamokuTag"), "_number", kp.getSerial());
		buf.append(kamokuTag);
		//
		kp.reset();
		while(kp.hasNext()){
			KamokuItem ki	=	(KamokuItem)kp.next();
			buf.append(getXml(ki, tb, kdr));
		}
		
		buf.append(tb.get("bottomXml"));
		return	buf.toString();
		
	}
	/**
	 * type を判断してXmlデータを作成する
	 * 
	 * @param ki
	 * @return
	 */
	public	String	getXml(KamokuItem ki, TemplateBox tb, KamokuDefRecord kdr){
		if(LOG.fa) LOG.println("KamokuEdit #getXml()");
		
		String	xml	=	null;
		if(ki.isTextArea()){
			xml	=	textAreaXml(ki, tb, kdr);
			
		}else if(ki.isTextField()){
			xml	=	textFiledXml(ki, tb, kdr);
			
		}else if(ki.isListBox()){
			xml	=	listBoxXml(ki, tb, kdr);
			
		}else if(ki.isCheckBox()){
			xml	=	checkBoxXml(ki, tb, kdr);
			
		}
		return	xml;
	}	

	public	String	textAreaXml(KamokuItem ki, TemplateBox tb, KamokuDefRecord kdr){
		if(LOG.fa) LOG.println("KamokuEdit #textAreaXml()");
		
		String		html	=	tb.get("TextAreaXml");
		Hashtable	ht		=	new	Hashtable();

		ht.put("_name"	, ki.getTagName());
		ht.put("_label"	, ki.getLabel());
		ht.put("_row"	, (isEmpty(ki.getRow()) ? DEFAULT_ROWS   : ki.getRow()) );
		ht.put("_col"	, (isEmpty(ki.getCol()) ? DEFAULT_COLS_A : ki.getCol()) );
		
		//String	content	=	getItem( ki.getTagName() );// _ の付加をしない
		
		String	content	=	toTextArea(ki.getTagName(), kdr);
		
		ht.put("_content", content);

		return	StringGear.replace(html, ht, false);		
	}
	public String	toTextArea(String tagName, KamokuDefRecord kdr){
		/*
		gaiyou
		book
		sankou
		hyoka
		youken
		keyword
		bikou
		*/
		String	content	=	"";
		if(tagName.equals("gaiyou")){
			content	=	kdr.content();
			
		}else if(tagName.equals("book")){
			content	=	kdr.textbook();
			
		}else if(tagName.equals("sankou")){
			content	=	kdr.ref_book() + CR + kdr.ref_url();
			
		}else if(tagName.equals("hyoka")){
			content	=	kdr.hyoka();
			
		}else if(tagName.equals("youken")){
			content	=	kdr.note();
			
		}else if(tagName.equals("keyword")){
			content	=	kdr.keywords();
			
		}else if(tagName.equals("bikou")){
			content	=	kdr.bikou();
			
		}
		return	content;
	
	}
	public	String	textFiledXml(KamokuItem ki, TemplateBox tb, KamokuDefRecord kdr){
		if(LOG.fa) LOG.println("KamokuEdit #textFiledXml()");
		
		String		html	=	tb.get("TextFieldXml");
		Hashtable	ht		=	new	Hashtable();

		ht.put("_name"	, ki.getTagName());
		ht.put("_label"	, ki.getLabel());
		ht.put("_col"	, (isEmpty(ki.getCol()) ? DEFAULT_COLS_A : ki.getCol()) );

		String	content	=	toTextFiled(ki.getTagName(), kdr);
		ht.put("_content", content);

		return	StringGear.replace(html, ht, false);	
	}
	public String	toTextFiled(String tagName, KamokuDefRecord kdr){
		/*
		 title
		 etitle
		 subtitle
		 tantou
		*/
		String	content	=	"";
		if(tagName.equals("title")){
			content	=	kdr.title();
			
		}else if(tagName.equals("tantou")){
			content	=	tname;
		}
		return	content;
	}	
	public	String	listBoxXml(KamokuItem ki, TemplateBox tb, KamokuDefRecord kdr){
		if(LOG.fa) LOG.println("KamokuEdit #listBoxXml()");
		
		String		html	=	tb.get("ListBoxXml");	// xmlテンプレート
		Hashtable	ht		=	new	Hashtable();

		ht.put("_name"	, ki.getTagName());
		ht.put("_label"	, ki.getLabel());

		StringBuffer	buf	=	new	StringBuffer();
		boolean		spc	=	false;
		Vector			elm	=	ki.getElm();

		for(int i=0; i<elm.size(); i++){
			String	item	=	(String)elm.get(i);
			if(spc)	buf.append(" ");
			buf.append(item);
			spc	=	true;
		}
		String	content	=	buf.toString();
		ht.put("_content", content);

		return	StringGear.replace(html, ht, false);	
	}

	public	String	checkBoxXml(KamokuItem ki, TemplateBox tb, KamokuDefRecord kdr){
		if(LOG.fa) LOG.println("KamokuEdit #checkBoxXml()");
		
		String		html	=	tb.get("CheckBoxXml");	// xmlテンプレート
		Hashtable	ht		=	new	Hashtable();

		ht.put("_name"	, ki.getTagName());
		ht.put("_label"	, ki.getLabel());
		
		StringBuffer	buf	=	new	StringBuffer();
		boolean		spc	=	false;
		Vector			elm	=	ki.getElm();// 選択肢
		
		for(int i=0; i<elm.size(); i++){
			String	item	=	(String)elm.get(i);
			if(spc)	buf.append(" ");
			buf.append(item);
			spc	=	true;
		}
		String	content	=	buf.toString();
		ht.put("_content", content);

		return	StringGear.replace(html, ht, false);	
	}	

	/**
	 * シラバス定義xmlのパーサーを帰す
	 * ユーザー定義とグループ定義で serial の大きい方からパーサーを作成して返す
	 * ただしユーザー定義ファイルはユーザーがアップロードしない限り存在しない
	 * 
	 * serial の大小は文字列として比較して得る
	 * 
	 * 
	 * @return	シラバス定義xmlのパーサー
	 */
	public	KamokuParser	getSyllabus(){
		if(LOG.fa)	LOG.println("■ KamokuEdit #getSyllabusPath()");

		String			groupFile	=	para.groupSyllbusPath(szDB);	// グループファイル
		KamokuParser	group		=	getKamokuParser( FileGear.getFileData(groupFile) );

		String			userFile	=	para.userSyllbusPath(getParameter(GROUP), getParameter(TUID));	// ユーザーファイル
		if(!FileGear.isExistFile(userFile)){
			return	group;
		
		}else{
			KamokuParser	user	=	getKamokuParser( FileGear.getFileData(userFile)  );
			int				com		=	(group.getSerial()).compareTo(user.getSerial());
			if(com >= 0){
				return	group;
			}else{
				return	user;
			}
		}
		
		
	}
	/**
	 * KamokuParserを生成する
	 * xmlException でシステムを停止させる
	 * @param xml	科目定義XML
	 * @return		生成できなかった時はウェブにメッセージを表示し、nullを返す．
	 */
	public	KamokuParser	getKamokuParser(String xml){
		if(LOG.fa) LOG.println("KamokuEdit #getKamokuParser()");
		
		KamokuParser	kp		=	null;
		try{
			kp	=	new	KamokuParser(xml);
		}catch(xmlException e){
			errPrint(e.getMessage());
			// 以下の実行はとまらない
		}
		if(kp==null) LOG.println("★ KamokuEdit #getKamokuParser(): parser is null");
		return	kp;
	}
	
		
		
	
}
