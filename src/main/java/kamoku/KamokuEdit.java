/*
 * シラバスを作成する
 *
 * 
 */
package kamoku;
import framework.*;
import		tktools.*;

import 		java.io.PrintWriter;
import 		java.util.Hashtable;
import 		java.util.Vector;
import 		database.Database;
import 		database.KeyGen;
import 		database.DbConnectionBroker;
import		xmlparser.*;
/**
*
*
	#
	# ##################
	#   KamokuEdit
	# ##################
	#
	<program $kamoku.KamokuEdit>
		<dispatch  html=kamokuEdit.html  number=220  class=kamoku.KamokuEdit />
		<variable>
		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA  lec_key/>
		  <accept    CMD    UPLODE  />
		  <keep      alterFlag />
		  
		  <work      />
		  <form      />
		</variable>
	</program> 
*/

public class KamokuEdit extends SuperPlayer implements KamokuVar{

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
	Database			db;
	/**
	 * 科目キー
	 * 新規作成の場合は""
	 */
	String		lec_key;
	/**
	 * 教師id
	 */
	String		teUid;
	/**
	 * データの更新処理で科目定義XMLファイルが新バージョンに変わったかどうかを記録する
	 * 
	 * CHANGED			変わった(こちらだけ意味がある)
	 * NOT_CHANGED		変わらない（"" 使わない）
	 */
	String		alterFlag;
	
	
	public	KamokuEdit(){
		super();
		if(LOG.fa) LOG.println("■ Sample #コンストラクタ");
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
		
		teUid	=	getParameter(TUID);
		lec_key	=	getParameter("lec_key");
		
		alterFlag	=	getParameter("alterFlag"); // 設定されてなければ ""

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
		if(LOG.fa) LOG.outHash(htb,"■Sample #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		if(cmd.equals("SAVE")){
			/*
			 * 保存する
			 */
			save();
			
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示

		}else if(cmd.equals("PREVIEW")){
			/*
			 * 保存してプレビューする
			 */
		    save();
		    disp_mode	=	DISP_NEW;
			ret			=	"$kamoku.SyllabusPrev";			

		}else if(cmd.equals("RETURN")){
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;	// 復帰			

		}else{
			// エラー対策
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;	// 復帰			
		}
		/*
		 * 次に表示するときあるいはリターンしてきた時の表示モードをセットしてからリターンする
		 */
		putParameter(DISP_KEY,disp_mode);
		return	ret;
	}
	/**
	 * 入力・編集された内容からXmlを再構成してデータベースに書き戻す
	 *
	 */
	public	void	save(){
		
		if(isEmpty(lec_key)){
			insertRecord();
		}else{
			updateRecord();
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
	/**
	 * 更新マークを備考欄にセットした科目定義レコードのスケルトンを得る
	 * @return	科目定義レコード
	 */
	public	KamokuDefRecord	getEmptyRecord(){
		KamokuDefRecord	kdr		=	new	KamokuDefRecord();
		kdr.set_bikou( UPDATED_MARK );	// 更新した印
		return	kdr;
	}
	/**
	 * 科目レコードを新規作成して記録する
	 *
	 */
	public	void	insertRecord(){
		if(LOG.fa) LOG.println("KamokuEdit #insertRecord()");
		
		KamokuParser	old		=	getSyllabus();
		String			newXml	=	updateXml(old);

		KamokuParser	kp		=	getKamokuParser(newXml);
		String			title	=	kp.getTitle();
		if(isEmpty(title)){
			putParameter(MESSAGE,"★科目名が記入されていません");
			return;
		}
		// レコードキーを作成
		KeyGen			gen		=	new	KeyGen(teUid, db);
		lec_key					=	gen.nextLec();
		// データベースレコードに保存しておくため
		putParameter("lec_key", lec_key);
		KamokuDefRecord	kdr		=	getEmptyRecord();
		kdr.set_teuid(teUid);
		kdr.set_lec_key(lec_key);
		kdr.set_title(title);
		kdr.set_content(newXml);
		kdr.insert(db);
		
	}
	/**
	 * 科目レコードを更新する
	 *
	 */
	public	void	updateRecord(){
		if(LOG.fa) LOG.println("KamokuEdit #updateRecord()");
		
		/*
		 * 科目定義XMLが新しくなり再定義したときはKamokuParserは
		 * データベースレコードではなく、科目定義XMLから作成する
		 * 判断するために、alterFlag をウェブ変数として保存してある
		 */
		KamokuParser	old		=	null;
		if(!alterFlag.equals(CHANGED)){
			KamokuDefRecord	kdr		=	new	KamokuDefRecord(teUid, lec_key, db);
			String			oldxml	=	kdr.content();
			old						=	getKamokuParser( oldxml );
			
		}else{
			old	=	getSyllabus();
			/*
			 * これでデータベースにかかれるのでalterFlagはクリアしておく
			 */
			putParameter("alterFlag","");
		}

		// 更新後のデータとしてのXML
		String			newXml	=	updateXml(old);
		KamokuParser	kp		=	getKamokuParser(newXml);
		String			title	=	kp.getTitle();
		if(isEmpty(title)){
			putParameter(MESSAGE,"★科目名が記入されていません");
			return;
		}		
		//	 データベースレコードに保存しておくため
		putParameter("lec_key", lec_key);
		KamokuDefRecord	newkdr	=	getEmptyRecord();
		newkdr.set_teuid(teUid);
		newkdr.set_lec_key(lec_key);
		newkdr.set_title(title);
		newkdr.set_content(newXml);
		newkdr.update(db);
	}
	
	/**
	 * 所与の科目定義XMLデータと実際の入力値から、科目定義XMLを更新して生成する
	 * 
	 * @param xml
	 */
	public	String	updateXml(KamokuParser kp){
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
			buf.append(getXml(ki, tb));
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
	public	String	getXml(KamokuItem ki, TemplateBox tb){
		if(LOG.fa) LOG.println("KamokuEdit #getXml()");
		
		String	xml	=	null;
		if(ki.isTextArea()){
			xml	=	textAreaXml(ki, tb);
			
		}else if(ki.isTextField()){
			xml	=	textFiledXml(ki, tb);
			
		}else if(ki.isListBox()){
			xml	=	listBoxXml(ki, tb);
			
		}else if(ki.isCheckBox()){
			xml	=	checkBoxXml(ki, tb);
			
		}
		return	xml;
	}	

	public	String	textAreaXml(KamokuItem ki, TemplateBox tb){
		if(LOG.fa) LOG.println("KamokuEdit #textAreaXml()");
		
		String		html	=	tb.get("TextAreaXml");
		Hashtable	ht		=	new	Hashtable();

		ht.put("_name"	, ki.getTagName());
		ht.put("_label"	, ki.getLabel());
		ht.put("_row"	, (isEmpty(ki.getRow()) ? DEFAULT_ROWS   : ki.getRow()) );
		ht.put("_col"	, (isEmpty(ki.getCol()) ? DEFAULT_COLS_A : ki.getCol()) );
		
		String	content	=	getItem( ki.getTagName() );// _ の付加をしない
		ht.put("_content", content);

		return	StringGear.replace(html, ht, false);		
	}
	
	public	String	textFiledXml(KamokuItem ki, TemplateBox tb){
		if(LOG.fa) LOG.println("KamokuEdit #textFiledXml()");
		
		String		html	=	tb.get("TextFieldXml");
		Hashtable	ht		=	new	Hashtable();

		ht.put("_name"	, ki.getTagName());
		ht.put("_label"	, ki.getLabel());
		ht.put("_col"	, (isEmpty(ki.getCol()) ? DEFAULT_COLS_A : ki.getCol()) );

		String	content	=	getItem( ki.getTagName() );
		ht.put("_content", content);

		return	StringGear.replace(html, ht, false);	
	}
	
	public	String	listBoxXml(KamokuItem ki, TemplateBox tb){
		if(LOG.fa) LOG.println("KamokuEdit #listBoxXml()");
		
		String		html	=	tb.get("ListBoxXml");	// xmlテンプレート
		Hashtable	ht		=	new	Hashtable();

		ht.put("_name"	, ki.getTagName());
		ht.put("_label"	, ki.getLabel());

		StringBuffer	buf	=	new	StringBuffer();
		boolean		spc	=	false;
		Vector			elm	=	ki.getElm();

		String	value	=	getItem(ki.getTagName());// 選択された選択肢文字列
		for(int i=0; i<elm.size(); i++){

			// elm ないの要素には選択されていたものに * がついているのでこれを取ってから比較する
			String	oldItem	=	reset((String)elm.get(i));
			String	item		=	"";
			if(value.equals(oldItem)){
				item	=	set(oldItem);
			}else{
				item	=	reset(oldItem);
			}
			if(spc)	buf.append(" ");
			buf.append(item);
			spc	=	true;
		}
		String	content	=	buf.toString();
		ht.put("_content", content);

		return	StringGear.replace(html, ht, false);	
	}

	public	String	checkBoxXml(KamokuItem ki, TemplateBox tb){
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
			String	value	=	getItem(ki.getTagName() + String.valueOf(i));// 選択されてなければnull
			if(Gear.isEmpty(value)){
				item	=	reset(item);
			}else{
				item	=	set(item);
			}
			if(spc)	buf.append(" ");
			buf.append(item);
			spc	=	true;
		}
		String	content	=	buf.toString();
		ht.put("_content", content);

		return	StringGear.replace(html, ht, false);	
	}	
	
	/**
	 * 要素の先頭にマーク(*)が付いていれば取り去る
	 * @param item
	 * @return
	 */
	public	String	reset(String item){
		if(LOG.fa) LOG.println("KamokuEdit #reset()");
		
		if(Gear.isEmpty(item))	return	item;
		
		if(item.charAt(0)=='*'){
			return	item.substring(1);
		}else{
			return	item;
		}
	}
	/**
	 * 要素の先頭にマーク(*)が付いていなければ付ける
	 * @param item
	 * @return
	 */
	public	String	set(String item){
		if(LOG.fa) LOG.println("KamokuEdit #set()");
		
		if(Gear.isEmpty(item))	return	item;
		
		if(item.charAt(0)=='*'){
			return	item;
		}else{
			return	"*" + item;
		}
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

		String			groupFile	=	para.groupSyllbusPath(getParameter(GROUP));	// グループファイル
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
	/*////////////////////////////////////////////////////////////////////////////
	 * 
	 *    表 示 処 理
	 * 
	 * 　　    以下はコントローラーが呼び出す表示メソッドである．
	 * 　　    一般には、このクラス内のメソッドから直接呼び出さない．
	 * 
	 *////////////////////////////////////////////////////////////////////////////
	
	public  void	display(boolean editmode){
		if(LOG.fa)	LOG.println("■ KamokuEdit #display(boolean editmode)");
		
		if(isEmpty(lec_key)){
			/*
			 * 新規の科目の場合
			 */
		    setNewRecord();
		
		}else{
			/*
			 * 更新の科目の場合
			 * 古い形式からXMLへ更新済みのレコードかどうかチェック
			 * 
			 */
			KamokuDefRecord	kdr	=	new	KamokuDefRecord(teUid, lec_key, db);
			if( kdr.bikou().equals( UPDATED_MARK ) ){
				/*
				 * 更新済み
				 * XMLからHTMLを作成してシステムハッシュに格納する
				 */
				if(!setEditRecord()){
					/*
					 * 致命的エラーの処理が必要では
					 */
				    return;
				}
			}else{
				/*
				 * 更新未済
				 * 更新されていないレコードはXML形式にコンバートしてから更新処理を行なう
				 */
				KamokuConvert	kc	=	new	KamokuConvert();
				kc.setInit(out, htb, para);		// 本来ならフレームワークが行なう処理を手動で行なう
				kc.initialize(out, htb, para);	// 
				//
				String			xml	=	kc.convert(kdr);
				kdr.set_content(xml);
				kdr.set_bikou( UPDATED_MARK );
				kdr.update(db);				
				/*
				 * XMLからHTMLを作成してシステムハッシュに格納する
				 */
				if(!setEditRecord()){
					/*
					 * 致命的エラーの処理が必要では
					 */
					return;
				}
				
			}
		}
		
		if(!editmode){
			htb.put(MESSAGE,"");
		}
		
		/* strHash(htb,DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}
	/**
	 * XML定義から部分HTMLを作成する
	 * 編集レコードのための入力フィールド部分にあたる
	 */
	public	boolean	setEditRecord(){
		if(LOG.fa)	LOG.println("■ KamokuEdit #setEditRecord()");
		/*
		 * 科目レコードからxmlを取り出して科目パーサーを作成する
		 */
		KamokuDefRecord	kdr	=	new	KamokuDefRecord(teUid, lec_key, db);
		String			xml	=	kdr.content();
		KamokuParser	kp	=	getKamokuParser(xml);
		/*
		 * 致命的エラーの処理が必要では
		 */
		if(kp==null)	return	false;
		/*
		 * 科目定義xml のserialを調べて、更新しようとする定義ファイルよりも
		 * 新しい serial の定義ファイルがある場合は、新規にレコードを作成する．
		 * 
		 * 		古い科目定義をやめて新しい定義に移行したい場合は、serial を新しく
		 * 		した定義ファイルをシステムにセットアップしておけばよい．
		 * 		セットアップは、管理者がセットアップするグループ定義と個人がセットアップする
		 * 		ユーザー定義があるが、いずれか、serial の新しい（大きい）方が自動的に使われる．
		 *  	ref. getSyllabus()
		 */
		String			serial		=	kp.getSerial(); 
		KamokuParser	creation	=	getSyllabus();// もっともserial の大きいxml
		int				comp		=	creation.getSerial().compareTo(serial);
		if(comp>0){
			/*
		     * 新しい定義に移行したことをウェブ変数に残す
			 * 更新処理で元にするXML定義をデータベースから取らないようにするため必要
			 */
			putParameter("alterFlag",CHANGED);
			/*
			 * 科目名を引き継ぐため初期セットしてから入力フィールドHTMLを作成する
			 * 		テキストフィールドとテキストエリアは，
			 * 		同じ項目名があり同じタイプであれば値を引き継く
			 */
			moveToNewDefinition(kp,creation);
			creation.setTitle(kp.getTitle());
			
			putParameter("serial",creation.getSerial());
			/*
			 * 表示用HTMLを作成してセットする
			 */
			setRecord(creation);
		}else{
			/*
			 * creationのserialが同じ（あるいは小さい）だったときは，
			 * 科目レコードのシラバス定義をそのまま使う
			 */
		    putParameter("serial",serial);
			/*
			 * 表示用HTMLを作成してセットする
			 */
			setRecord(kp);
		}
		return	true;
		
	}
	/**
	 * 古い科目定義から新しい科目定義へ移行したとき
	 * テキストエリア，テキストフィールドの項目で同名のものがあれば
	 * データを引き継ぐ
	 * 
	 * @param source
	 * @param dest
	 */
	void	moveToNewDefinition(KamokuParser source, KamokuParser dest){
	    
	    moveTextArea(source	, dest);
	    moveTextField(source, dest);
	    
	    if(LOG.fa){
	        /* 内容を出力する */
	        LOG.println("");
	        LOG.println("--- source ---");
	        source.print();
	        LOG.println("");
	        LOG.println("---  dest  ---");
	        dest.print();
	    }
	    
	}
	/**
	 * 同名のテキストエリアのデータを引き継ぐ
	 * @param source
	 * @param dest
	 */
	void	moveTextArea(KamokuParser source, KamokuParser dest){
	    
	    Object	item;
	    source.reset();
	    while((item=source.nextTextArea())!=null){
	        /*
	         * 同じtagNameのフィールドを捜す
	         */
	        String		tagName	=	((KamokuItem)item).getTagName();
	        Object		obj		=	dest.sameTextArea(tagName);
	        if(obj!=null){
	            /*
	             * sourceのテキストをdestにセットする
	             */
	            String	dt	=	((KamokuItem)item).getText();
	            if(!isEmpty(dt)){
	                ((KamokuItem)obj).setText(dt);
	            }
	        }
	    }
	}
	/**
	 * 同名のテキストフィールドのデータを引き継ぐ
	 * @param source
	 * @param dest
	 */
	void	moveTextField(KamokuParser source, KamokuParser dest){
	    
	    Object	item;
	    source.reset();
	    while((item=source.nextTextField())!=null){
	        /*
	         * 同じtagNameのフィールドを捜す
	         */
	        String		tagName	=	((KamokuItem)item).getTagName();
	        Object		obj		=	dest.sameTextField(tagName);
	        if(obj!=null){
	            /*
	             * sourceのテキストをdestにセットする
	             */
	            String	dt	=	((KamokuItem)item).getText();
	            if(!isEmpty(dt)){
	                ((KamokuItem)obj).setText(dt);
	            }
	        }
	    }
	}
	/**
	 * 新規レコードのための入力フィールドを作成する
	 */
	public	void	setNewRecord(){
		if(LOG.fa)	LOG.println("■ KamokuEdit #setNewRecord()");
		
		KamokuParser	kp	=	getSyllabus();
		
		putParameter("serial",kp.getSerial());
		setRecord(kp);
		
	}

	/**
	 * 所与の科目定義XMLデータから、項目入力用のHTMLを生成する
	 * HTMLは putParameter("_html", ・・・) によってシステムハッシュにセットしておく
	 * 
	 * @param xml
	 */
	public	void	setRecord(KamokuParser	kp){
		if(LOG.fa)	LOG.println("■ KamokuEdit #setRecord(String xml)");
		
		TemplateBox		tb	=	new	TemplateBox(para.kamokuTemplatePath());
		StringBuffer	buf	=	new	StringBuffer(2048);
		
		kp.reset();
		while(kp.hasNext()){
			KamokuItem ki	=	(KamokuItem)kp.next();
			buf.append(makeHtml(ki, tb));
		}		
		putParameter("_html", buf.toString());
		return;
	}
	/**
	 * type の違いにより適切なhtmlを生成して返す
	 * 
	 * @param ki
	 * @param tb
	 * @return
	 */
	public	String	makeHtml(KamokuItem ki, TemplateBox	tb){
		if(LOG.fa)	LOG.println("■ KamokuEdit #makeHtml()");

		String	html	=	null;
		if(ki.isTextArea()){
			html	=	mkTextArea(ki, tb);
			
		}else if(ki.isTextField()){
			html	=	mkTextField(ki, tb);
			
		}else if(ki.isListBox()){
			html	=	mkListBox(ki, tb);
			
		}else if(ki.isCheckBox()){
			html	=	mkCheckBox(ki, tb);
			
		}
		return	html;
		
	}
	/**
	 * テキストフィールド用
	 * @param ki
	 * @param tb
	 * @return
	 */
	public	String	mkTextField(KamokuItem ki, TemplateBox	tb){
		if(LOG.fa)	LOG.println("■ KamokuEdit #mkTextField()");

		String		html	=	tb.get("enterTextfieldTemplate");
		Hashtable	ht		=	new	Hashtable();

		ht.put("_name"	, ki.getTagName());
		ht.put("_label"	, ki.getLabel());
		ht.put("_size"	, (isEmpty(ki.getCol()) ? DEFAULT_COLS_F : ki.getCol()) );
		ht.put("_content",(isEmpty(ki.getText()) ? "" : ki.getText()));

		return	StringGear.replace(html, ht, false);
	}
	/**
	 * テキストエリア用
	 * @param ki
	 * @param tb
	 * @return
	 */
	public	String	mkTextArea(KamokuItem ki, TemplateBox	tb){
		if(LOG.fa)	LOG.println("■ KamokuEdit #mkTextArea()");
		
		String		html	=	tb.get("enterTextareaTemplate");
		Hashtable	ht		=	new	Hashtable();

		ht.put("_name"	, ki.getTagName());
		ht.put("_label", ki.getLabel());
		ht.put("_row", (isEmpty(ki.getRow()) ? DEFAULT_ROWS   : ki.getRow()) );
		ht.put("_col", (isEmpty(ki.getCol()) ? DEFAULT_COLS_A : ki.getCol()) );

		String	content	=	(isEmpty(ki.getText()) ? "" : ki.getText());
		ht.put("_content", content);

		return	StringGear.replace(html, ht, false);
	}
	/**
	 * リストボックス用
	 * @param ki
	 * @param tb
	 * @return
	 */
	public	String	mkListBox(KamokuItem ki, TemplateBox tb){
		if(LOG.fa)	LOG.println("■ KamokuEdit #mkListBox()");
		
		// 最初にリスト要素のhtmlを作成する
		StringBuffer	buf		=	new	StringBuffer();
		Hashtable		ht		=	new	Hashtable();
		
		Vector		elm		=	ki.getElm();
		for(int i=0; i<elm.size(); i++){
			String item	=	(String)elm.get(i);
			if(item.charAt(0)=='*'){
				ht.put("_sel","selected");
				ht.put("_item", ((String)elm.get(i)).substring(1));// 先頭の * を取る
			}else{
				ht.put("_sel","");
				ht.put("_item", (String)elm.get(i));
			}
			String	html	=	tb.get("enterListBoxItemTemplate");
			String	data	=	StringGear.replace(html, ht, false);
			buf.append(data);
			buf.append(CR);
		}
		String	items	=	buf.toString();
		
		// リストボックス本体
		String	html	=	tb.get("enterListBoxTemplate");
		ht.put("_items"	, items);
		ht.put("_name"	, ki.getTagName());
		ht.put("_label"	, ki.getLabel());
		
		return	StringGear.replace(html, ht, false);
	}
	/**
	 * チェックボックス用
	 * @param ki
	 * @param tb
	 * @return
	 */
	public	String	mkCheckBox(KamokuItem ki, TemplateBox	tb){
		if(LOG.fa)	LOG.println("■ KamokuEdit #mkCheckBox()");

		// 最初にチェックボックス要素のhtmlを作成する
		StringBuffer	buf		=	new	StringBuffer();
		Hashtable		ht		=	new	Hashtable();
		
		Vector		elm		=	ki.getElm();
		for(int i=0; i<elm.size(); i++){
			String item	=	(String)elm.get(i);
			if(item.charAt(0)=='*'){
				ht.put("_chk","checked");
				ht.put("_item", ((String)elm.get(i)).substring(1));// 先頭の * を取る
			}else{
				ht.put("_chk","");
				ht.put("_item", (String)elm.get(i));
			}
			
			ht.put("_name"	, ki.getTagName());
			ht.put("_k"		, String.valueOf(i));
			String	html	=	tb.get("enterCheckBoxItemTemplate");
			String	data	=	StringGear.replace(html, ht, false);
			buf.append(data);
			buf.append(CR);
		}
		String	items	=	buf.toString();
		
		// チェックボックス本体
		String	html	=	tb.get("enterCheckBoxTemplate");
		ht.put("_items"	, items);
		ht.put("_label"	, ki.getLabel());
		
		return	StringGear.replace(html, ht, false);
	}


	public	static	void	main(String []arg){

		String			path1	=	"D:\\PowerCampus\\java\\conf\\temp\\syllabusOLD.xml";
		String			path2	=	"D:\\PowerCampus\\java\\conf\\temp\\syllabusNEW.xml";
		KamokuEdit		ke		=	new	KamokuEdit();
		KamokuParser	kpOLD	=	ke.sub(path1);
		KamokuParser	kpNEW	=	ke.sub(path2);
		ke.moveToNewDefinition(kpOLD, kpNEW);
		
	}		

	public	KamokuParser	sub(String path){

		String	xml		=	FileGear.getFileData(path);
		if(Gear.isEmpty(xml)){
			System.out.println("data is empty!");
			return	null;
		}
		KamokuParser	kp	=	null;
		try{
			kp	=	new KamokuParser(xml);
		}catch(xmlException e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		while(kp.hasNext()){
			KamokuItem ki	=	(KamokuItem)kp.next();
			ki.print();
		}
		kp.reset();
		return	kp;
	}		
	
}
