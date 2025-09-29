package framework;
import		java.util.*;
import		java.io.*;
import		xmlparser.*;
import		tktools.*;


/**
 * dispatch情報クラス．
 * 
 * 	xml形式のディスパッチデータからタグを読み込んで解析し、結果を保持する．
 * 	論理プログラム名及びWEBシート番号からディスパッチデータを検索するメソッドを提供する．
 */

public class Dispatch extends Object implements PCvar{
	
	/**
	 * xmlパーサーオブジェクト
	 * ひとつのタグ（またはテキスト）を順次返すメソッド nextXml()をもつ
	 */
	SimpleParser	sp;
	/**
	 * 論理プログラム名
	 */
	String			keyValue;
	
	/** グローバル変数名リスト */
	Hashtable		global;	

	/** 変数名エイリアスリスト */
	Hashtable		alias;	
	

	/* プログラム情報 */
	Hashtable		dsp;
	Hashtable		numberDsp;
	
	/* 変数リスト(variable) */
	Hashtable		received_vars;	// 起動時に受け取る変数のリスト
	Hashtable		web_vars;		// webからアクセプトする変数のリスト
	Hashtable		work_vars;		// webに保存する変数のリスト
	Hashtable		vals;			// 全変数を統合したリスト
	
	/* プログラム番号、対応するHTMLファイル名、クラス名前 */
	String			number;
	String			html;
	String			className;
	
		
	
	public	Dispatch(String xml) {

		sp				=	new	SimpleParser(xml);

		alias			=	new	Hashtable(300);
		dsp				=	new	Hashtable(100);
		numberDsp		=	new	Hashtable(100);
		
		vals			=	new	Hashtable(100);
		received_vars	=	new	Hashtable(100);
		web_vars		=	new	Hashtable(100);
		work_vars		=	new	Hashtable(100);
		
		try{
			parse();
			if(LOG.fa){
				test();
			}
		}catch(xmlException e){
			System.out.println("■ DispatchList: xml記述エラー");
			//System.out.println(xml.substring(0,e.getValue()));
			System.out.println("△" + e.getMessage());
			System.out.println("");
		}
	}

	
	/**
	 * キーからnumberを得る
	 */
	public	String	getNumber(String key){
		Csv	cs	=	(Csv)dsp.get(key);
		if(cs==null)	return	null;
		return	cs.get(0);
	}
	
	/**
	 * キーから html を得る
	 */
	public	String	getHtml(String key){
		Csv	cs	=	(Csv)dsp.get(key);
		if(cs==null)	return	null;
		return	cs.get(1);
	}

	/**
	 * キーから className を得る
	 */
	public	String	getClassName(String key){
		Csv	cs	=	(Csv)dsp.get(key);
		if(cs==null)	return	null;
		return	cs.get(2);
	}

	/**
	 * 全てのプログラムキーの列挙を得る
	 */
	public	Enumeration	getKeys(){
		return	dsp.keys();
		
	}
	/**
	 * シート番号からkey を得る
	 */
	public	String	getKey(int num) throws PCException{
		String	key	=	String.valueOf(num);
		Csv	cs	=	(Csv)numberDsp.get(key);
		if(cs==null)	throw (new PCException("★不正なプログラム番号：number=" + num));
		return	cs.get(0);
	}
	
	/**
	 * シート番号からhtml を得る
	 */
	public	String	getHtml(int num) throws PCException{
		String	key	=	String.valueOf(num);
		Csv	cs	=	(Csv)numberDsp.get(key);
		if(cs==null)	throw (new PCException("★不正なプログラム番号：number=" + num));
		return	cs.get(1);
	}

	/**
	 * シート番号からclassName を得る
	 */
	public	String	getClassName(int num) throws PCException{
		String	key	=	String.valueOf(num);
		Csv	cs	=	(Csv)numberDsp.get(key);
		if(cs==null)	throw (new PCException("★不正なプログラム番号：number=" + num));
		return	cs.get(2);
	}
	/**
	 * 全てのシート番号の列挙を得る
	 */
	public	Enumeration	getNumberKeys(){
		return	numberDsp.keys();
		
	}	


	/** 論理変数名から実変数名を得る */
	public	String	getVariableName(String key){
		String	varName	=	Gear.strHashIncludeNull(alias,key);
		if(Gear.isEmpty(varName)){
			// エイリアスにないものはキーを実変数名とする
			return	key;
		}
		return	varName;
	}
	

	/** キーから実変数名リストをCSV文字列で得る */
	public	String	getVariableList(String key) {
		
		// 変数エイリアスのリストを得る
		// valsは論理プログラム名をキーとして全変数のCSV文字列を持つ
		String	varlist	=	(String)vals.get(key);
		//System.out.println("■etVariableList(String key): varlist=" + varlist);
		Csv		cs		=	new Csv(varlist,",");
		
		/* エイリアスを実変数名に変換したCSVを作成して返す */
		StringBuffer	bf	=	new	StringBuffer();
		boolean		cma	=	false;
		for(int i=0; i<cs.size(); i++){
			if(cma)	bf.append(",");
			
			//エイリアスにないものはキーを実変数名とする
			String		var	=	Gear.strHashIncludeNull(alias,cs.get(i));
			if(Gear.isEmpty(var)){
				var = cs.get(i);
			}
			bf.append(var);
			cma	=	true;
		}
		if(bf.length()>0)	return	bf.toString();
		return	"";
	}
	
	/** キーから論理変数リストをCSV文字列で得る */
	public	String	getAliasList(String key){
		
		/* 論理変数リストを得る */
		String	varlist	=	(String)vals.get(key);
		Csv		cs		=	new Csv(varlist,",");
		
		/* CSVで返す */
		StringBuffer	bf	=	new	StringBuffer();
		boolean		cma	=	false;
		for(int i=0; i<cs.size(); i++){
			if(cma)	bf.append(",");
			bf.append(cs.get(i));
			cma	=	true;
		}
		if(bf.length()>0)	return	bf.toString();
		return	"";
	}

	/** クラスキーから reserves 論理変数名リストを得る */
	public String	getReservedVarList(String key){
		String	list	=	Gear.strHash(received_vars, key);
		if(Gear.isEmpty(list))	return	"";
		
		return	list;
	}
	/** クラスキーから web 論理変数リストを得る */
	public String	getWebVarList(String key){
		String	list	=	Gear.strHash(web_vars, key);
		if(Gear.isEmpty(list))	return	"";
		
		return	list;
	}	
	/** クラスキーから work 論理変数リストを得る */
	public String	getWorkVarList(String key){
		String	list	=	Gear.strHash(work_vars, key);
		if(Gear.isEmpty(list))	return	"";
		
		return	list;
	}

	/**
	 * パース．
	 * セクションタグを判別してそれぞれの内容をパースする．
	 * @throws xmlException
	 */
	void	parse()	throws xmlException {
			
		XmlToken	xml;
		while((xml=sp.nextXml())!=null){
			
			if(xml.getTagName().equals("programSection")){// タグ名
				programSection();
				
			}else if(xml.getTagName().equals("variableSection")){
				variableSection();
			
			}else{
				continue;
			}
		}		
	}
	/**
	 * 変数エイリアスセクションのタグをパースする
	 * 
	 * グローバル変数リストとエイリアス変数リストを
	 * 作成する．
	 * 
	 * 例示
	 * 　　エイリアス ("NUMBER", "sheet")
	 * 
	 * @throws xmlException
	 */
	void	variableSection()  throws xmlException {
		XmlToken	xml;
		while((xml=sp.nextXml())!=null){

			/* タグ以外はスキップする */
			if(!xml.isTag()){
				continue;
			}
			/* variableSection　タグなら終了 */
			if(xml.getTagName().equals("variableSection")){
				if(xml.isEtag()){
					break;
				}else{
					throw new xmlException("variableSection タグが閉じられる前に再度出現した");
				}				
			}
			/* global または var タグでなければスキップ */
			if(xml.getTagName().equals("var")){
				if(LOG.fa){
					LOG.println("-- xml/variableSection/var --");
					LOG.println(xml.getToken());
				}			
				getAlias(xml);
				
			}else if(xml.getTagName().equals("gloval")){
				if(LOG.fa){
					LOG.println("-- xml/variableSection/global --");
					LOG.println(xml.getToken());
				}
				getGlobal(xml);
				
			}else{
				continue;
			}
		}
	}
	/**
	 * グローバル変数リストに要素を加える
	 * @param xml
	 */
	void	getGlobal(XmlToken	xml){
		Attribute	at		=	xml.nextAttr();
		if(at!=null){// ない場合はnullが返るので
			String		name	=	at.getName();
			String		val		=	at.getValue();
			global.put(name, val);// Hashtable
		}
	}
	/**
	 * エイリアス変数リストに要素を加える
	 * @param xml
	 */
	void	getAlias(XmlToken	xml){
		Attribute	at		=	xml.nextAttr();
		if(at!=null){// ない場合はnullが返るので
			String		name	=	at.getName();
			String		val		=	at.getValue();
			alias.put(name, val);// Hashtable
		}
	}
	/**
	 * プログラムセクションのタグをパースする
	 * 
	 * @throws xmlException
	 */
	void	programSection()  throws xmlException {
	
		XmlToken	xml;
		while((xml=sp.nextXml())!=null){

			/* タグ以外はスキップする */
			if(!xml.isTag()){
				continue;
			}
			/* programSection　タグなら終了 */
			if(xml.getTagName().equals("programSection")){
				if(xml.isEtag()){
					break;
				}else{
					throw new xmlException("programSection タグが閉じられる前に再度出現した");
				}
			}
			/* programタグでなければスキップ */
			if(!xml.getTagName().equals("program")){
				continue;
			}
			
			/*
			 * 論理プログラム名を得る.なければエラー
			 */
			Attribute	progAttr = xml.nextAttr();
			if(progAttr==null){
				throw	new xmlException(sp.location(),"program タグにキーが書かれていない");
			}
			keyValue	=	progAttr.getName();
			//
			if(LOG.fa){
				LOG.println("-- xml/programSection --");
				LOG.println(xml.getToken());
			}
			/*
			 * </program> が現れるまで処理を行う
			 * dispatchタグとvariableタグをパースする
			 */
			while((xml=sp.nextXml())!=null){
				if(!xml.isTag()){
					continue;
				}
				
				String	tagName	=	xml.getTagName();
				if(tagName.equals("dispatch")){
					getDispatch(xml);
				}else if(tagName.equals("variable")){
					getVriables();
				}else if(tagName.equals("program")){
					break;
				}
			}
			
			
		}
	}
	/**
	 * variableタグをパースしてハッシュに格納する
	 * 
	 * ハッシュの中には、論理プログラム名をキーとして、変数リストのCSV文字列が格納される
	 * 
	 *　 例	("$setup.SetupView",  "NUMBER,STAMP,GROUP,TUID,TMAIL,TNAME,KANA,CMD,UPLODE")
	 * 
	 * @throws xmlException
	 */
	void	getVriables()  throws xmlException {
		XmlToken	xml;
		while((xml=sp.nextXml())!=null){
			if(!xml.isTag()){
				continue;
			}
			String	tagName	=	xml.getTagName();
			if(tagName.equals( XRECEIVE )){			// receive
				getReceivedVars(xml);
			}else if(tagName.equals( XACCEPT )){	// accept
				getWebVars(xml);
			}else if(tagName.equals( XSET )){		// set
				getWorkVars(xml);
			}else if(tagName.equals("variable")){
				if(xml.isEtag()){
					break;
				}else{
					throw new xmlException("variable タグが閉じられる前に再度出現した");
				}
			}
		}
		Csv	list_receive	=	new Csv(Gear.strHash(received_vars,keyValue));
		Csv	list_web		=	new Csv(Gear.strHash(web_vars,keyValue));
		Csv	list_work		=	new Csv(Gear.strHash(work_vars,keyValue));
		//
		StringBuffer	tbuf	=	new StringBuffer();
		combine(tbuf, list_receive);
		combine(tbuf, list_web);
		combine(tbuf, list_work);
		//
		vals.put(keyValue, tbuf.toString());
		//System.out.println("■■■" + keyValue + "■■" + tbuf.toString());
	}
	/**
	 * csv の要素をバッファにCSV文字列として全て追加する
	 *
	 * @param buf
	 * @param cs
	 */
	void	combine(StringBuffer buf, Csv cs){
		
		boolean	flag			=	false;
		if(buf.length()>0)	flag	=	true;
		
		for(int i=0; i<cs.size(); i++){
			if(flag)	buf.append(",");
			buf.append(cs.get(i));
			flag	=	true;
		}
		
	}
	
	/**
	 * received ハッシュに、論理プログラム名をキーとしてCSV形式でreceived変数リストを登録する
	 * 
 	 * @param xml	ひとつのタグ
	 */
	void	getReceivedVars(XmlToken xml){

		Attribute	attr;
		xml.reset();
		boolean		cm	=	false;
		StringBuffer	bf	=	new StringBuffer();
		while((attr=xml.nextAttr())!=null){

			if(cm)	bf.append(",");
			//
			String	name	=	attr.getName();
			String	val		=	attr.getValue();
			bf.append(name);
			cm	=	true;
		}
		if(bf.length()>0){
			received_vars.put(keyValue, bf.toString());
		}else{
			received_vars.put(keyValue, "");
		}
	}
	/**
	 * received ハッシュに、論理プログラム名をキーとしてCSV形式でreceived変数リストを登録する
	 * 
	 * @param xml	ひとつのタグ
	 */
	void	getWebVars(XmlToken xml){

		Attribute	attr;
		xml.reset();
		boolean		cm	=	false;
		StringBuffer	bf	=	new StringBuffer();
		while((attr=xml.nextAttr())!=null){

			if(cm)	bf.append(",");
			//
			String	name	=	attr.getName();
			String	val		=	attr.getValue();
			bf.append(name);
			cm	=	true;
		}
		if(bf.length()>0){
			web_vars.put(keyValue, bf.toString());
		}else{
			web_vars.put(keyValue, "");
		}
	}
	/**
	 * received ハッシュに、論理プログラム名をキーとしてCSV形式でreceived変数リストを登録する
	 * 
	 * @param xml	ひとつのタグ
	 */
	void	getWorkVars(XmlToken xml){

		Attribute	attr;
		xml.reset();
		boolean		cm	=	false;
		StringBuffer	bf	=	new StringBuffer();
		while((attr=xml.nextAttr())!=null){

			if(cm)	bf.append(",");
			//
			String	name	=	attr.getName();
			String	val		=	attr.getValue();
			bf.append(name);
			cm	=	true;
		}
		if(bf.length()>0){
			work_vars.put(keyValue, bf.toString());
		}else{
			work_vars.put(keyValue, "");
		}
	}
	/**
	 * ディスパッチタグをパースする．
	 * 論理プログラム名をキーとするハッシュを作成する．
	 * プログラム番号をキーとするハッシュを作成する・
	 * 
	 * 例示
	 * 	("$StPasswd","3010,passwd.html,student.StPasswd")
	 * 	("3010","$StPasswd,passwd.html,student.StPasswd")
	 * 
	 * @param xml
	 * @throws xmlException
	 */
	void	getDispatch(XmlToken xml) throws xmlException {
		
		String	number		=	"-";
		String	html		=	"-";
		String	className	=	"-";
		//			
		Attribute	attr;
		//
		xml.reset();
		while((attr=xml.nextAttr())!=null){
				
			String	name	=	attr.getName();
			String	val		=	attr.getValue();
				
			if(name.equals("number")){
				if(isDigit(val)){
					number	=	val;					
				}else{
					throw	new xmlException(sp.location(),"number が数値形式でない");
				}
			}else if(name.equals("html")){
				html		=	val;
						
			}else if(name.equals("class")){
				className	=	val;
											
			}else{
				throw	new xmlException(sp.location(),keyValue + "<---属性名の誤り");
			}				
		}
		if(className.equals("-"))	throw	new xmlException(sp.location(),"class が指定されていない");
		if( !(number.equals("-")) && (html.equals("-")) )	throw	new xmlException(sp.location(),"html が指定されていない");
		if( (number.equals("-")) && !(html.equals("-")) )	throw	new xmlException(sp.location(),"number が指定されていない");
		//
		Csv	cs	=	new Csv(number + "," + html + "," + className);
		dsp.put(keyValue,cs);
		//
		if(!number.equals("-")){
			Csv	cs2	=	new Csv(keyValue + "," + html + "," + className);
			numberDsp.put(number,cs2);
		}
		
	}

	// 文字列が数字がどうかチェックする
	boolean isDigit(String s){
		if((s == null)||(s.length()==0))    return false;
		//
		int len = s.length();
		for(int i=0; i<len; i++){
			char ch = matchDigit(s.charAt(i));
			if(ch == '*')       return false;
		}
		return true;
	}
	// 数字文字列にして返す
	String toDigit(String s){
		if((s == null)||(s.length()==0))    return "";
		//
		StringBuffer bf = new StringBuffer(100);
		int len = s.length();
		for(int i=0; i<len; i++){
			char ch = matchDigit(s.charAt(i));
			if(ch == '*')       return "";
			bf.append(ch);
		}
		return bf.toString();
	}
	//
	char	matchDigit(char c){
		if((c=='0')||(c=='０'))	return	'0';
		if((c=='1')||(c=='１'))	return	'1';
		if((c=='2')||(c=='２'))	return	'2';
		if((c=='3')||(c=='３'))	return	'3';
		if((c=='4')||(c=='４'))	return	'4';
		if((c=='5')||(c=='５'))	return	'5';
		if((c=='6')||(c=='６'))	return	'6';
		if((c=='7')||(c=='７'))	return	'7';
		if((c=='8')||(c=='８'))	return	'8';
		if((c=='9')||(c=='９'))	return	'9';
		return '*';
	}


	/* テスト用 */
	
	public void	test(){
		
		Enumeration	keys	=	getKeys();
		while(keys.hasMoreElements()){
			String	key	=(String)(keys.nextElement());
			System.out.println("■ key      :" + key);
			System.out.println(" ・number :" + getNumber(key));
			System.out.println(" ・html   :" + getHtml(key));
			System.out.println(" ・class  :" + getClassName(key));
			
			System.out.println(" ・alias  :" + getAliasList(key));
			System.out.println(" 　　○reserved valiables :" + getReservedVarList(key));
			System.out.println(" 　　○web      valiables :" + getWebVarList(key));
			System.out.println(" 　　○work     valiables :" + getWorkVarList(key));
			System.out.println(" ・values :" + getVariableList(key));

		}
		Enumeration	numkeys	=	getNumberKeys();
		while(numkeys.hasMoreElements()){
			String	keynum	=(String)(numkeys.nextElement());
			int	k		=	Integer.parseInt(keynum);
			System.out.println("■ number      :" + k);
			try{
				System.out.println(" ・key    :" + getKey(k));
				System.out.println(" ・html   :" + getHtml(k));
				System.out.println(" ・class  :" + getClassName(k));
			}catch(PCException e){
				e.printStackTrace();
			}
		}
		
		
	}
	
	
	public	static	void	main(String []arg){
		
		/*
			try {
				// Convert a byte array to base64 string
				//byte[] buf = new byte[]{0x12, 0x23};
				String	str	=	"あいうえお漢字";
				byte[] buf = str.getBytes();
				
				String s = new sun.misc.BASE64Encoder().encode(buf);
				System.out.println("str1=" + s);
				// Convert base64 string to a byte array
				buf = new sun.misc.BASE64Decoder().decodeBuffer(s);
				String	s2	=	new String(buf); 
				System.out.println("str2=" + s2);
			
			} catch (IOException e) {
			}
*/
		
		
		
		
		String	path	=	"D:\\PowerCampus\\conf\\conf_203\\dispatch.xml";
		String	xml		=	getFile(path);
		if(Gear.isEmpty(xml)){
			System.out.println("data is empty!");
		}
		Dispatch	dl	=	new Dispatch(xml);
		
		
		//
		Enumeration	keys	=	dl.getKeys();
		while(keys.hasMoreElements()){
			String	key	=(String)(keys.nextElement());
			System.out.println("■ key      :" + key);
			System.out.println(" ・number :" + dl.getNumber(key));
			System.out.println(" ・html   :" + dl.getHtml(key));
			System.out.println(" ・class  :" + dl.getClassName(key));
			
			System.out.println(" ・alias  :" + dl.getAliasList(key));
			System.out.println(" 　　○reserved valiables :" + dl.getReservedVarList(key));
			System.out.println(" 　　○web      valiables :" + dl.getWebVarList(key));
			System.out.println(" 　　○work     valiables :" + dl.getWorkVarList(key));
			System.out.println(" ・values :" + dl.getVariableList(key));
		}
		Enumeration	numkeys	=	dl.getNumberKeys();
		while(numkeys.hasMoreElements()){
			String	keynum	=(String)(numkeys.nextElement());
			int	k		=	Integer.parseInt(keynum);
			System.out.println("■ number      :" + k);
			try{
				System.out.println(" ・key    :" + dl.getKey(k));
				System.out.println(" ・html   :" + dl.getHtml(k));
				System.out.println(" ・class  :" + dl.getClassName(k));
			}catch(PCException e){
				e.printStackTrace();
			}
		}		
		//

	
		
		
	}
	static  public String getFile(String fname){
		//
		String	str;
		try{
			str	= getBytesFromFile(fname);
		}catch(UnsupportedEncodingException e1){
			str	= "";
		}catch(IOException e2){
			str	= "";
		}
		return	str;
	}	
	static String getBytesFromFile(String fname) throws IOException,UnsupportedEncodingException {
		//
		File	file	= new File(fname);
		InputStream is 	= new FileInputStream(file);
		long 	length 	= file.length();	// Get the size of the file
		if (length > Integer.MAX_VALUE) {
			// File is too large
		}
		//
		byte[] bytes = new byte[(int)length];	// Create the byte array to hold the data
		// Read in the bytes
		int offset 	= 0;
		int numRead = 0;
		while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
			offset += numRead;
		}
		// Ensure all the bytes have been read in
		if (offset < bytes.length) {
			throw new IOException("Could not completely read file "+file.getName());
		}
		is.close();
		String data = "";
		try{
			data	= new String(bytes,"Windows-31J");
		}catch(UnsupportedEncodingException e){
			System.out.println("■ class files #getBytesFromFile() : Windows-31Jエンコードができない");
			System.out.println(e);
		}
		return	data;
	
	
	}

}

