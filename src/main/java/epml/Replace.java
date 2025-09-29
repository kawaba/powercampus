/*
     汎用の書き換えクラス

	 templateは Windows-31J で作成された表示用のHTMLファイル
     置き換える文字列は %_tx_userName% のように、％文字で挟んだパラメータ名である。
	 
	 通常はこれを継承したクラスを作成し、write メソッドをオーバーライドする
	 （単純な置き換えしかしないのであれば、write メソッドは不要．replace クラスをそのまま使えばよい）
	 
	  ■ public	void write(String key,StringBuffer text,Hashtable htb)
	  　
	  　☆単純な置き換えではなく、元テキストの特定の位置で何か特別な処理をしたい場合に使う書き換えメソッド
		
		%@key% を埋め込んでおく
	  　
	  　key  : 置き換えテキストの特定の位置に埋め込んでいたキーであり、write メソッドではこれによって処理を分ける
	  　text : write で置き換えを行う直前までの元テキストが入っている．置き換えた文字列をこのバッファにappendすることで処理終了
	  　htb  : 処理に必要となるデータが格納されている．一般にはwebのフォームオブジェクト変数とその値の組などである．
	  
	  ■ public	void write(String key,StringBuffer text,Hashtable htb,StringBuffer subBuf)
		
		☆表などで１行分のテンプレートデータを繰り返し使って全体を表示する場合など、反復のある置き換えの時に使うメソッド
		
	  　%&key% を埋め込み、取り込む部分文字列の最後に %% を付加する
	  　
	  	%&correctRaw%
	  	<tr>
		%&correctColumn%
		  <td  class="k12" style="padding-right: 15px;">
		    <input type="checkbox" name="correct%_num%" value="1" class="k12" style="valign: middle;" >%_word%
		  </td>%%
		</tr>%%
	    
	    反復置き換え指定は上のように入れ子にして指定することができる
		
		
	  　key  : 置き換えテキストに埋め込んでいたキーであり、write メソッドではこれによって処理を分ける
	  　text : write で置き換えを行う直前までの元テキストが入っている．置き換えた文字列をこのバッファにappendすることで処理終了
	  　htb  : 置き換えの際に必要となるデータが格納されている．一般にはwebのフォームオブジェクト変数とその値の組である．
	  　subBuffer :
	          ・ 置き換えテキストの中に埋め込んだ範囲指定文字によって切り取った部分文字列．toString() で取り出して使う
			  ・ 一般にはこの文字列の中にはさらに置き換えるべき文字列がある．
			  ・テーブルの一行分のテンプレートなどで、その中に置き換えるべき文字列が %_varible% と言う形で埋め込んである
			  

*/
package epml;
import	epml.tools.*;
//import java.io.*;
import java.util.*;
//
public class Replace extends Object {
	//
	private	String		preText;	// 原文
	private	Hashtable	htb;		// 置き換えのデータ
	OptionWriter			opt;		//
	//
	private	StringBuffer 	text;		// 完成文
	private	int			max;		// 問題原文の長さ
	private	int			p;			// バッファ内の位置（ゼロオリジン）
	private	String			tkn;		// トークン
	private	int			id;			// トークン種別
	//
	//
	// トークン区切り記号
	private static final char DLM = '%';
	private static final char RPL = '_';
	private static final char OPT = '@';
	private static final char LBK = '&';
	private static final char BLK = '#';
	//
	// トークン種別コード
	public static final int RPLACE		= 1;
	public static final int OPTION		= 2;
	public static final int LOOPBLK		= 3;
	public static final int BLOCK		= 4;
	public static final int STRING		= 20;
	//
	//
	public	Replace(String s,Hashtable _htb){
		if(DBG.fa) DBG.println("class Replace #コンストラクタ の先頭です(opt は null)");
		//
		preText	= new String(s);
		htb		= _htb;
		//
		p		= 0;
		max		= preText.length();
		text	= new StringBuffer(10240);
		opt		= null;
	}
	//
	public	Replace(String s,Hashtable _htb, OptionWriter _opt){
		if(DBG.fa) DBG.println("class Replace #コンストラクタ の先頭です-0");
		//
		preText	= new String(s);
		htb		= _htb;
		//
		p		= 0;
		max		= preText.length();
		text	= new StringBuffer(10240);
		opt		= _opt;
	}
	// オーバーロードされる
	// 書き換え１
	public void	write(String tkn,StringBuffer text,Hashtable htb){
		if(opt!=null){
			opt.write( tkn, text, htb);
		}
	}
	//　書き換え２
	public void	write(String tkn,StringBuffer text,Hashtable htb,StringBuffer subBuf){
		if(opt!=null){
			opt.write( tkn, text, htb, subBuf);
		}
	}
	
	// 同じ preText で htb の要素を変更して何回も置き換えを実行しては結果を受け取る
	// 場合は、毎回、以下の初期化を行う必要がある．
	//
	public void	init(){
		p			= 0;				// preText の位置を指すポインタをゼロに
		int delPos	= text.length();
		text.delete(0,delPos);			// 結果を格納するバッファ内容を消去する
	}
	//
	// ハッシュテーブル操作
	public	Object	put(Object key,Object obj ){
		return	htb.put(key,obj);
	}
	public	Object	get(Object key){
		return	htb.get(key);
	}
	//
	public	String getText(){
		return	text.toString();
	}
	// 変数初期化機能を含む
	// 反復的な置き換えではこれを使う
	public String	subst(){
		return	subst(false);
	}
	public String	subst(boolean	cp_932){
		//
		replacing();
		String str  =  new String(text.toString());
		init();
		
		if(cp_932){
			return	Cp932.toCp932(Cp932.toJIS(str));		// cp932
		}
		return str;
	}
	public void	replacing(){
		
		//DBG.outHash(htb,"replacing()の先頭です．");
		//
		while( ( nextToken()) > 0 ){
			//
			// 置き換え
			if(id==RPLACE){
				String str	= (String)htb.get(tkn);
				if(str!=null){
					text.append(str);
				}else{
					text.append(tkn);					// これでいいかどうか
				}
			//
			// オプション処理
			}else if(id==OPTION){
				if(opt!=null){
					opt.write(tkn,text,htb);			// tkn をキーとしてテキストを生成しtext に付加する
				}else{
					this.write(tkn,text,htb);			// 2004.1 継承して使えるように訂正
				}
			// 反復置き換え処理
			}else if(id==LOOPBLK){
				StringBuffer	subBuf	= new StringBuffer(10240);
				blockString(subBuf);					// %% までを全て取り出してバッファに入れる．途中の %& --- %% ブロックはデータとして扱う．
				if(opt!=null){
					opt.write(tkn,text,htb,subBuf);		// tkn をキーとして、繰り返しsubBufからテキストを生成してtext に追加する
				}else{
					this.write(tkn,text,htb,subBuf);	// 2004.1 継承して使えるように訂正
				}
			// ブロック処理
			}else if(id==BLOCK){
				
				
				
			//
			// 文のパート
			}else{	// STR
				text.append(tkn);
				//
			}
		}
		return ;
	}
	//
	// バッファから次のトークンを取り出す．
	//
	private int nextToken(){
		//System.out.println("##### nextToken");
		//
		StringBuffer	buf 	= new StringBuffer(1024);
		boolean			ret	= false;
		//
		id	= -1;	// 設定されていない状態
		char	c = 0;
		if( (c=nextChar()) == 0 ){	return	-1;}	// 単に終了を意味する．（id=-1 で帰る）
		//
		// 
		if(c == DLM){	// %
			if( (c=nextChar()) == 0 ){
				buf.append(c);
				id	= STRING;
				//
			}
			if(c==RPL){				// _　（単純置き換え）
				id	= RPLACE;
				buf.append(c);
				ret	= nextWord(buf,DLM);	// DLM が出現するまで読み込む
				if(!ret){					// DLM が出現せず最後まで読んでしまった
					id	= STRING;			// 文字列として処理する
				}
				//
			}else if(c==OPT){		// @　（オプション処理）
				id	= OPTION;
				ret	= nextWord(buf,DLM);
				if(!ret){
					id	= STRING;
				}
				//
			}else if(c==LBK){		// ＆ （反復置き換え）
				id	= LOOPBLK;
				ret	= nextWord(buf,DLM);	// キーをとり出す　%&abcd% ⇒　abcd
				if(!ret){
					id	= STRING;
				}
				//
			}else if(c==BLK){		// #
				id	= BLOCK;
				ret	= nextWord(buf,DLM);
				if(!ret){
					id	= STRING;
				}
				//
			}else{
				id	= STRING;
				buf.append(DLM);	// ％自体もデータだったので
				buf.append(c);
				ret	= nextWord(buf,DLM);
				if(ret){
					putBackChar();	// 次の制御文字だから
				}
			}
		}else{
			id	= STRING;
			buf.append(c);
			ret	= nextWord(buf,DLM);
			if(ret){
				putBackChar();	// 次の制御文字だから
			}
		}
		tkn	= buf.toString();
		return id;
	}
	//
	//
	private boolean nextWord(StringBuffer buf,char endChar){
		//System.out.println("##### nextWord");
		//
		char 	c  		= 0;
		boolean	endFlag	= false;
		while( (c=nextChar())!=0){
			if(c==endChar){
				endFlag	= true;
				break;
			}
			buf.append(c);
		}
		return endFlag;
	}
	//
	//
	private void blockString(StringBuffer buf){
		//System.out.println("##### nextWord");
		//
		int		count	= 0;
		char 	c  		= 0;
		//boolean	endFlag	= false;
		while( (c=nextChar())!=0){
			if(c=='%'){
				/*DBG.print("char-% ==>:" + c + ":");*/
				//
				if((c=nextChar())=='&'){
					count++;
					buf.append('%');
					buf.append('&');
					/*DBG.print("char-& ==>:" + c + ":" + buf.toString());*/
				}else if(c=='%'){
					if(count == 0){
						//endFlag	= true;
						/*DBG.print("char-% Break ==>:" + c + ":"+ buf.toString());*/
						break;
					}else{
						buf.append('%');
						buf.append('%');
						/*DBG.print("char-% Cont ==>:" + c + ":"+ buf.toString());*/
						count--;
					}
				}else{
					buf.append('%');
					buf.append(c);
					/*DBG.print("char-0 ==>:" + c + ":"+ buf.toString());*/
				}
			}else{
				buf.append(c);
				/*DBG.print("char-0 ==>:" + c + ":"+ buf.toString());*/
			}
		}
		if(DBG.fa){
			DBG.print("■class Replace #blockString() : 反復置き換え用のブロックデータ");
			String	ck = buf.toString();
			if((ck!=null)&&(ck.length()>0)){
				DBG.print(ck);
			}else{
				DBG.print("   ===> ブロックデータがない");
				DBG.print(preText);
			}
		}
		/*
		if(DBG._xtbl){
			DBG.println("★class Replace #blockString() :");
			DBG.println(buf.toString());
		}
		*/
		//return endFlag;
	}
	//
	// バッファから１文字取って返す．ポインタは＋１される
	private char nextChar(){
		//System.out.print("##### nextChar() = ");
		char	c = 0;
		if( !EOB() ){
			c = preText.charAt(p);
			p++;
		}else{
			c = 0;	// バッファが空
		}
		//System.out.println(c);
		return	c;
	}
	// バッファを１文字戻す
	private void	putBackChar(){
		--p;
	}
	// バッファが空かどうか
	boolean	EOB(){
		return (p >= max);	// 最後のとき true
	}
	// tkn の文字数が１以上か調べる．ただし、半角、全角のスペースと改行コードは
	// 文字数に含めない
	// zero でなければ true を返す
	boolean count(){
		int len = tkn.length();
		for(int k=0; k<len; k++){
			if( isCount(tkn.charAt(k)) ) return true;
		}
		return false;
	}
	boolean isCount(char c){
		if( (c==' ')||(c=='　') ){
			return false;
		}
		if(Character.isISOControl(c)) return false;	// 制御文字
		//
		return true;
	}
}