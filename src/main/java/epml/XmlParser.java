package epml;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import epml.tools.DBG;
//import java.text.*;
//import java.util.*;
/*
	   xml形式の解答データをパースする
	   		・パーサは、指定されたタグに含まれるデータを取り出す機能を基本とする
	   		・<ENL> を指定すると <ENL> ～ </ENL> の間に含まれる文字列を取得する
			・これに対して<count>を指定すると 6 を取得するので、<question>を指定して６回の呼び出しで全ての問題項目を取得できる
			・<question>を指定したのちさらに<key>と<answer>を指定して、問題キーと解答の組を得ることができる
			
			<ENL>
			  <count>
			    6
			  </count>
			  <question>
			      <key>
			        ENL01
			      </key>
			      <answer>
			        spaceship
			      </answer>
			  </question>
			  <question>
			      <key>
			        ENL02
			      </key>
			      <answer>
			        cosmo
			      </answer>
			  </question>
			  ･･･　･･･　･･･
			  ･･･　･･･　･･･
            </ENL>			
			
*/

public class XmlParser {
	//
	private	String			xml;	// もとのxml文書
	private	BufferedReader	in;		// 行単位に読み込むためのリーダー
	private	String			data;	// 取り出した行データ
	private	String			type;	// data にtagを含むとき "TAG" そうでないとき "DATA"
	private	String			ln;		// 読み取った行データ
	//
	final String CR = System.getProperty("line.separator");
	// コンストラクタ
	//
	public XmlParser(String	str){
		if(DBG.fa) DBG.println("class XmlParser #XmlParser() : コンストラクタ の先頭です");
		//
		data	= "";
		type	= "";
		ln		= null;
		xml	= new String(str);
		in	= new  BufferedReader( new StringReader(xml) );
		try{
			in.mark( xml.length() );	// 開始位置を記録
		}catch(IOException e){
			
		}
	}
	//
	// 読み出し位置を先頭に戻す
	public	void	reset(){
		try{
			in.reset();
			
		}catch(IOException e){
		}
	}
	//
	// 取得したデータタイプを返す
	public	String	getType()	{ return type; }
	//
	// tag の示す部分のデータを取得する
	public	String	get(String tag) throws ExamException{
		if(DBG.fa) DBG.println("class XmlParser #get() : tag の示す部分のデータを取得する の先頭です");
		//
		try{
			locate(tag);				// 頭だし
			data	= getData(tag);		// データを得る
			type	= "DATA";
			if(isTag()){
				type	= "TAG";
			}
		}catch(ExamException e){
			throw	e;
		}
		return	data;
	}
	//
	// tag の示す行に位置付ける
	public	void	locate(String tag) throws ExamException{
		if(DBG.fa) DBG.println("class XmlParser #locate() : tag の示す行に位置付ける の先頭です");
		//
		String	startTag	=	tag.trim();						// <abc>
		// タグの出現行まで勧める
		boolean	done	= false;
		try{
			while((ln=in.readLine())!=null){
				if((ln.trim()).equals(startTag)){
					done	= true;
					break;
				}
			}
		}catch(IOException e){
			throw	new ExamException(100,"入出力エラー： 文字列ストリームを読めない" + CR + e);
		}
		if(!done) throw	new ExamException(101,"パースエラー： 指定されたタグ " + startTag + " が見つからない");
		//
	}
	//
	// 終了タグまでの行データを取得する
	public	String	getData(String tag) throws ExamException{
		if(DBG.fa) DBG.println("class XmlParser #getData() : 終了タグまでの行データを取得する");
		//
		String	startTag	=	tag.trim();						// <abc>
		String	endTag		=	"</" + startTag.substring(1);	// </abc>
		// タグの出現行まで勧める
		StringBuffer	bf		= new StringBuffer(1000);
		boolean		done	= false;
		try{
			boolean	newLine	= false;
			while((ln=in.readLine())!=null){
				if((ln.trim()).equals(endTag)){
					done	= true;
					break;
				}
				if(newLine)	bf.append(CR);	// データが１行の時はＣＲを含めない
				newLine	= true;
				bf.append(ln);
			}
		}catch(IOException e){
			throw	new ExamException(100,"入出力エラー： 文字列ストリームを読めない" + CR + e);
		}
		if(!done) throw	new ExamException(101,"パースエラー： 指定されたタグ " + endTag + " が見つからない");
		//		
		return bf.toString();
	}
	//
	// data 中にタグを含むかどうか
	public	boolean	isTag(){
		//
		boolean		ret	= false;	// false ==> タグを含まない　の意味
		BufferedReader 	in2	= new  BufferedReader( new StringReader(data) );
		String			ln2 = null;
		try{
			while((ln2=in2.readLine())!=null){
				String	ck 	= ln2.trim();
				int	n	= ck.length();
				//
				if(n>0){							// 長さが１以上で
					if(ck.charAt(0)=='<'){			// 先頭文字が　<
						if(ck.charAt(n-1)=='>'){	// 末尾文字が  >
							ret	= true;
							break;
						}
					}
				}
			}
			in2.close();
		}catch(IOException e){
			
		}
		return	ret;
	}
}
