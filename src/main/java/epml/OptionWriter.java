/*
     オプションのプリントを処理するクラス



*/
package epml;
//import	epml.tools.*;
//import java.io.*;
import java.util.*;
//
//abstract class OptionPrint implements Printer {

public class OptionWriter implements AdditionalPrint {

	
	public OptionWriter(){
		
	}
	// ハッシュテープル(htb)を使って key で特定される出力処理を行う
	//
	public void	write(String key,StringBuffer out,Hashtable htb){
		
	}
	// ハッシュテーブル(htb)を使って部分的に切り取ったソースデータ（exHtml）
	// の内容を置き換えて出力処理する．表などの反復出力に利用するが個々の処理
	// 内容は、key で特定される．
	//
	public void	write(String key,StringBuffer out,Hashtable htb,StringBuffer ext){
		
	}
	// 部分的に切り取ったソースデータ（exHtml）を出力するか否か、ハッシュテーブル
	// (htb)を使って判断し、出力する場合には、htb を使ってexHtml の内容を書き換えて
	// 出力する．個々の処理内容は、key で特定される．
	//
	public void	blockWrite(String key,StringBuffer out,Hashtable htb,StringBuffer ext){
		
		
	}
}
