/*
     個別の書き換え処理を担当するインターフェース
	 
	 このインターフェースを implements した OptionPrint クラスがあるので、
	 通常は、OptionPrint クラスを継承したクラスを定義し、その中で、以下の
	 メソッドのうち必要なものだけを定義する．
	 

*/
package epml;
//import	epml.tools.*;
import java.io.*;
import java.util.*;
//
public interface Printer {
	//
	/* ----------------------------------------------------------------------------------
	■ write() メソッド --- 処理の挿入
	　　　指示された処理を行う.
		  例えば、HTML で以下のように書いてあると、key を持ってこの write() を実行する
		　write() は、<table>以下を表示する直前に実行される
		
		（例）
			<P> ･････ </p>
			%@key%
			<table>
			<tr>
			<td> %_replace1% </td> <td> %_replace2% </td>
			</tr>
			<tr> ･････ </tr>	
	
	*/
	void	write(String key,PrintWriter out,Hashtable htb);
	
	
	/* ----------------------------------------------------------------------------------
	■ write() メソッド --- HTMLの書き換え
	　　exHtml の内容を出力する．
		
	　　テーブルの行要素のように反復して出力しなければならないHTMLが exHtml に格納されている
	　　出力する場合には PrintView を生成し、PrintView#PrintByVector() を使う．
	
		例えば以下のようなHTMLでは key1 の処理では、1⇒ から 2⇒ までが含まれているので、kye1 の
		処理では再帰的な処理が必須である．		

		（例）
			<tr> ･････ </tr>
			%@key1%
			<<
		1⇒ <tr>
			<td> %_replace1% </td> <td> %_replace2% </td>
			</tr>
			%@key2%
			<<
			<tr>
			<td> %_replace1% </td> <td> %_replace2% </td>
		    </tr>
		2⇒	>>
			>>
			<tr> ･････ </tr>
			
	 	（注）
			PrintView に渡す OptionPrint オブジェクトには、this を指定する．例えば以下のようになる．
			
			void write(String key,PrintWriter out,Hashtable htb,Vector exHtml){
				... ...
				
				if(...){
					... ...
				
				}else if(key.equals("key1"){
					... ...
					... ...
		   			PrintView pv	= new PrintView(out);
	       			pv.PrintByVector(htb,exHtml,this);
				
				}else if(key.equals("key2"){
				    ... ...
				}
			}
	*/
	void	write(String key,PrintWriter out,Hashtable htb,Vector exHtml);
	
	
	/* ----------------------------------------------------------------------------------
	■ blockWrite() メソッド
	　　exHtml の内容を出力するかどうか判定する
		
		例えば以下では、key2 で、 %#key2% ～ #END の間のHTMLを出力するかどうか
		判断する．
		出力する場合には PrintView を生成し、PrintView#PrintByVector() を使う．
		
		（例）
			<table>
			%@key1%
			<<
		　　<tr>
			<td> %_replace1% </td> <td> %_replace2% </td>
			</tr>
			>>
			%#blockkey1%
			<<
			<tr>
			<td> 空白です </td> <td> データがありません </td>
		    </tr>
			#END
			</table>
		
	 	（注）
			PrintView に渡す OptionPrint オブジェクトには、this を指定する．例えば以下のようになる．
			
			void blockWrite(String key,PrintWriter out,Hashtable htb,Vector exHtml){
				... ...
				
				if(...){
					... ...
				
				}else if(key.equals("blockkey1"){
					... ...
					... ...
		   			PrintView pv	= new PrintView(out);
	       			pv.PrintByVector(htb,exHtml,this);
				
				}else if(...){
				    ... ...
				}
			}
	*/
	void	blockWrite(String key,PrintWriter out,Hashtable htb,Vector exHtml);
	
	// ----------------------------------------------------------------------------------
	
}