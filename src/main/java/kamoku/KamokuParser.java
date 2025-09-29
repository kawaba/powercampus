/*
 * 作成日: 2005/02/14
 *
 * TODO
 */
package kamoku;
import		java.util.*;
import 		tktools.*;
import		xmlparser.*;
/**
 *　科目定義Xmlを読み込んで科目情報と各フィールドの情報（KamokuItem）を作成し
 *　保持する.<br>
 *
 *  xmlでは title タグは必須．
 *  また、serial も指定してあること．
 * 
 */
public class KamokuParser implements Iterator{
	/**
	 * 科目定義情報をあらわすxmlテキスト
	 */
	String			xmlStr;
	/**
	 * xmlパーサーオブジェクト
	 * ひとつのタグ（またはテキスト）を順次返すメソッド nextXml()をもつ
	 */
	SimpleParser	sp;
	/**
	 * 科目定義項目名のVector
	 * xmlで定義された順序で項目名を格納する
	 */
	Vector			items;
	/**
	 * 科目名
	 */
	String			title;
	/**
	 * 定義ファイルのシリアル番号
	 */
	String			serial;
	/**
	 * iteratorのための変数
	 */
	int				pos;	// 現在位置
	int				max;	// 要素数
	
	/**
	 * xmlを引数にとるコンストラクタ
	 * 科目名タグ title が定義されていないと例外を発生する
	 * 
	 * @param xmlStr
	 */
	public	KamokuParser(String	xmlStr) throws xmlException {
		
		this.xmlStr	=	xmlStr;
		sp			=	new	SimpleParser(xmlStr);
		items		=	new	Vector(30);
		title		=	null;
		parse();

		pos			=	0;
		max			=	items.size();
		//System.out.println("max="+max);
	
	}
	/**
	 * 科目名を返す
	 * 
	 * @return
	 */
	public	String	getTitle(){
		return		title;
	}
	/**
	 * シリアルを返す
	 * @return
	 */
	public	String	getSerial(){
		return		serial;
	}
	/**
	 * 強制的にserialをセットする
	 * @param str
	 */
	public	void	setSerial(String str){
		serial	=	str;
	}
	/**
	 * 強制的にタイトルをセットする
	 * @param str
	 */
	public	void	setTitle(String str){
		title	=	str;
		
		for(int k=0; k<max; k++){
			KamokuItem	ki		=	(KamokuItem)items.get(k);
			String		name	=	ki.getTagName();
			if(name.equals("title")){
				ki.setText(str);	// 強制的にタイトルをセットする
				break;
			}
		}
	}
	/**
	 * 繰り返し処理でさらに科目定義項目があるかどうか
	 * @return		さらに科目定義項目があるとき true
	 */
	public boolean hasNext(){
		if(pos < max){
			return	true;
		}else{
			return	false;
		}
	}
	/**
	 * 繰り返し処理で次の科目定義項目を返す
	 * 返すオブジェクトの実体は XmlToken オブジェクトである
	 * 
	 * @return
	 */
	public Object next(){
		if(hasNext()){
			KamokuItem	kr	=	(KamokuItem)(items.get(pos));
			pos++;
			return	kr;
		}else{
			return	null;
		}
		
	}
	/**
	 * 繰り返し処理で最後に返された要素を削除する
	 *
	 */
	public void remove(){
		if(pos>0){
			int	at			=	pos -1;
			KamokuItem	kr	=	(KamokuItem)items.get(at);
			items.remove(at);
		}
	}

	/**
	 * 繰り返し処理を初期化する
	 *
	 */
	public	void	reset(){
		pos	=	0;
	}
	
	/**
	 * 次のテキストフィールド項目を返す
	 * なければnullを返す
	 * @return
	 */
	public	Object nextTextField(){
	    
	    Object	result;
	    while((result=next())!=null){
	       if( ((KamokuItem)result).isTextField() ){
	           break;
	       }
	    }
	    return	result;
	    
	}
	/**
	 * 次のテキストエリア項目を返す
	 * なければnullを返す
	 * @return
	 */
	public	Object nextTextArea(){
	    
	    Object	result;
	    while((result=next())!=null){
	       if( ((KamokuItem)result).isTextArea() ){
	           break;
	       }
	    }
	    return	result;
	    
	}
	/**
	 * 同名のテキストフィールドを返す
	 * ない場合はnullを返す
	 * @param tagName
	 * @return
	 */
	Object	sameTextField(String tagName){
	    
	    reset();
	    Object	result;
	    while((result=nextTextField())!=null){
	        if( tagName.equals(((KamokuItem)result).getTagName()) ){
	            break;
	        }
	    }
	    return	result;
	}
	/**
	 * 同名のテキストフィールドを返す
	 * ない場合はnullを返す
	 * @param tagName
	 * @return
	 */
	Object	sameTextArea(String tagName){
	    
	    reset();
	    Object	result;
	    while((result=nextTextArea())!=null){
	        if( tagName.equals(((KamokuItem)result).getTagName()) ){
	            break;
	        }
	    }
	    return	result;
	}
	
	/**
	 * 解析したデータを全て出力する
	 *
	 */
	public	void	print(){
	    reset();
	    while(hasNext()){
			KamokuItem ki	=	(KamokuItem)next();
			ki.print();
		}		
		reset();
	}
	/** 
	 *	科目定義情報をパースする
	 *　科目名タグ title が定義されていないと例外を発生する
	 */
	void	parse() throws xmlException {

		// タイトルをクリアしておく
		title	=	null;
		
		/*
		 * kamoku タグから serial を取得する
		 */
		XmlToken	kamoku	=	skipHeadder("kamoku");
		if(kamoku==null){
			String	msg	=	"★科目定義ファイルに kamoku タグが指定されていません<br><br>" +
            "kamoku タグは serial と共に例えば以下のように書きます<br><br>" + 
			"&lt;kamoku  serial= 10&gt; <br>";
			throw new xmlException(sp.location(), msg);
			
		}
		serial	=	kamoku.get("serial");
		if(Gear.isEmpty(serial)){
			String	msg	=	"★科目定義ファイルに serial が指定されていません<br><br>" +
			                "serial は 整数値で kamoku タグのアトリビュートとして例えば以下のように書きます<br><br>" + 
							"&lt;kamoku  serial= 10&gt; <br>";
			throw new xmlException(sp.location(), msg);
		}
		
		/*
		 * 内容タグの解析
		 */
		XmlToken	tag,txt;
		// 最初はタグから始まる
		while(((tag=sp.nextXml())!=null)&&(!tag.getTagName().equals("kamoku"))){
			if(!tag.isTag()){
				throw new xmlException(sp.location(), "タグがあるべきところにテキストが書かれている|→" + tag.token()); 
			}
			// ２番目はテキストかまたは終了タグ
			txt	=	sp.nextXml();
			if(txt.isText()){
				// 科目定義項目を生成してベクターに保存する
				KamokuItem	ki	=	new	KamokuItem(tag, txt.getText());
				items.add(ki);
				
				//科目名を採取する
				if(tag.getTagName().equals("title")){
					title	=	txt.getTextString();// 改行コードを除いた文字列を返す
				}				
				
				// 最後の終了タグを読んでおく
				txt	=	sp.nextXml();
				if(!txt.isEtag()){
					throw new xmlException(sp.location(),"終了タグがなく、次のタグが書かれている|→"+ txt.token());
				}else{
					if(!txt.getTagName().equals(tag.getTagName())){
						throw new xmlException(sp.location(),"終了タグが開始タグとマッチしない|→");
					}
				}
				
			}else if(txt.isEtag()){
				if(txt.getTagName().equals(tag.getTagName())){
					String	type	=	tag.get("type");
					if(!type.equals("text")){
						throw new xmlException(sp.location(),"選択肢のテキストがない|→"+ txt.token());
					}else{
						KamokuItem	ki	=	new	KamokuItem(tag, "");
						items.add(ki);

						//科目名を採取する
						if(tag.getTagName().equals("title")){
							title	=	"";
						}						
					}
				}else{
					throw new xmlException(sp.location(),"終了タグが開始タグとマッチしない|→"+ txt.token());
				}
			}else{
				throw new xmlException(sp.location(),"終了タグがなく、次のタグが書かれている|→"+ txt.token());
			}
		}
		
		/*
		 * title タグは必須なので定義されているかどうかチェックする
		 */
		if(title==null){
			title			=	"";
			String	errmsg	=	"★科目名を指定する title タグが定義されていません<br><br>"+
								"科目定義の xml では、科目名を指定するため、必ず title タグを指定してください．<br>" +
								"  【タイトルタグの例示】<br>" +
								"      &lt;title label=科目名 type=text col=80&gt; <br>" +
								"      &lt;/title&gt;";
			throw new xmlException(sp.location(),errmsg );
		}
		
	}
	/**
	 * 指定したタグが出てくるまで読み飛ばす
	 * そのタグの情報(Xmltoken)を返す
	 * 
	 * @param tagname
	 * @throws xmlException
	 */
	XmlToken	skipHeadder(String tagname) throws xmlException {
		XmlToken	xml;
		while((xml=sp.nextXml())!=null){
			if(xml.isTag()){
				if(xml.getTagName().equals(tagname)){
					break;
				}
			}
		}
		return xml;
	}

	public	static	void	main(String []arg){

		String	path	=	"P:\\pc_data\\group\\kwc\\tkawaba\\syllabus\\syllabus.xml";
		String	xml		=	FileGear.getFileData(path);
		if(Gear.isEmpty(xml)){
			System.out.println("data is empty!");
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
	}	
}
