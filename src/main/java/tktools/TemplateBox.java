
/*
	雛型を集めたファイルを読み、キーと対応するパターンを返す．
　　
	返されるhtmlの最後の行データには改行コードが付いていない


*/
package tktools;

import java.io.*;
import java.util.*;
//
public class TemplateBox extends Object {
	
	/* HTMLファイルであれば、どの改行コードであってもブラウザが適切に処理してくれるので
	   改行コードは１文字で済む \n (0D) を使う
		
		UNIX 		<LF> 		0A  
		Windows 	<CR><LF> 	0D 0A 
		Macintosh 	<CR> 		0D 

	*/
	Hashtable	ht;
	
	public	TemplateBox(String tempPath){
		//if(DBG.fa) DBG.println("class TemplateBox #TemplateBox() : コンストラクタ の先頭です");
		//
		ht	= new Hashtable(100);
	    try {
			// Windows-31J で読まないと、不正なデータになる！！
			BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(tempPath),"Windows-31J"));
			//
    	    String str;
    	    while ((str = in.readLine()) != null) {
        	    if((str.length() > 0) && ((str.charAt(0) == '@')||(str.charAt(0) == '#'))){	// #,@ のどちらでも可
					StringBuffer bf		= new StringBuffer(2048);
					//
					char		 mk		= str.charAt(0);
					String 		 key	= str.substring(1);			// @ を取った残りの文字列
					while( (str = in.readLine()) != null) {
						if (str.equals("-----")){
							break;
						}
						bf.append(str);
						bf.append("\n");
						/*
						if((mk != '#')&&( key.charAt(0)!='$' )){	// 行区切りはフォーマットスケルトンのときのみ
							bf.append("\n");		// 挿入する．編集指示HTML(@$･･･)では<pre>のために改行が反映されてしまう
						}
						*/
					}
					int len = bf.length();	// 最後の行からは改行コードを除く
					bf.deleteCharAt(len-1);	// <pre> を使うHTML対応のため
					String	dt	= new String(bf.toString());
					//
					//if(DBG._xtbl) DBG.println("           wiki.txt = " + dt);
					//
					ht.put(key,dt);
				}
        	}
        	in.close();
    	} catch (IOException e) {
    		System.out.println("■■■■　TemplateBox, no file : " + tempPath );
		}
		
	}
	//
	// キーによってテンプレートを読み出す
	//
	public	String	get(String key){
		String	dt 	= (String) ht.get(key);
		return	dt;
	}
	//
	// キーによってテンプレートを読み出しベクターにいれて返す
	//
	public	Vector	getVector(String key){
		String		dt 	= (String) ht.get(key);
		Vector		v	=	StringGear.StringToVector(dt);
		return		v;
	}	
}