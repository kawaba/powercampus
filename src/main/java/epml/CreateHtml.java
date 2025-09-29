/*
     記述項をHTMLに置き換える出力処理クラス
     問題を初期表示するためのクラス
     ハッシュに置き換えの文字列を put して表示させる働き

*/
package epml;
//import java.io.*;
//import java.text.*;
import java.util.Hashtable;
import epml.tools.Csv;
import epml.tools.DBG;
import epml.tools.TemplateBox;
//
public class CreateHtml extends OptionWriter {
	
	TemplateBox				tb;
	EmbededNumberLists		enl;
	EmbededWordLists		ewl;
	EmbededTextFields		etf;
	EmbededRadioButtons		erb;
	EmbededTextAreas		eta;
	//
	// 問題項目表示用
	static	String [] numClass  = {"num20","num18","num16","num14","num12","num10","num9","num9"}; // フォントサイズより１クラス（2px）下げる
	static	String [] inpClass  = {"inp20","inp18","inp16","inp14","inp12","inp10","inp9","inp9"}; // フォントサイズより１クラス（2px）下げる
	static	String [] txaClass  = {"txa20","txa18","txa16","txa14","txa12","txa10","txa9","txa9"}; // フォントサイズより１クラス（2px）下げる
	static	String [] rbClass   = {"0","rb20","rb18", "rb16", "rb14", "rb12", "rb10", "rb9" }; // 
	//
	// 誤りを示す反転表示用
	static	String [] numClassR  = {"num20r","num18r","num16r","num14r","num12r","num10r","num9r","num9r"}; // フォントサイズより１クラス（2px）下げる
	static	String [] inpClassR  = {"inp20r","inp18r","inp16r","inp14r","inp12r","inp10r","inp9r","inp9r"}; // フォントサイズより１クラス（2px）下げる
	static	String [] txaClassR  = {"txa20r","txa18r","txa16r","txa14r","txa12r","txa10r","txa9r","txa9r"}; // フォントサイズより１クラス（2px）下げる
	static	String [] rbClassR   = {"0","rb20r","rb18r", "rb16r", "rb14r", "rb12r", "rb10r", "rb9r" }; 		// 
	//
	static	int    [] fontSize  = { 0,     20,     18,     16,     14,     12,     10,     9}; // フォントサイズ
	static	int    [] lineHeight= { 0,     22,     20,     18,     16,     14,     12,    11}; // フォントサイズ
//	static	String [] fontClass = { "0","k20",  "k18",  "k16",  "k14",  "k12",  "k10",  "k9"}; // 1 - 7 に対応
	static	String [] tdClass   = { "0","td20","td18", "td16", "td14", "td12", "td10", "td9" }; // 語群テーブルのセル
	//static	String [] sqClass   = {"0","sq20","sq18", "sq16", "sq14", "sq12", "sq10", "sq9" }; // 
	
	public CreateHtml(	String	htmlPath,
						EmbededNumberLists		_enl,
						EmbededWordLists		_ewl,
						EmbededTextFields		_etf,
						EmbededRadioButtons		_erb,
						EmbededTextAreas		_eta){
		
		tb		= new TemplateBox(htmlPath);
		enl		= _enl;
		ewl		= _ewl;
		etf		= _etf;
		erb		= _erb;
		eta		= _eta;
		//
	}
	public CreateHtml(	String	htmlPath,
						Exam	examObj){
		
		tb		= new TemplateBox(htmlPath);
		enl		= examObj.getENL();
		ewl		= examObj.getEWL();
		etf		= examObj.getETF();
		erb		= examObj.getERB();
		eta		= examObj.getETA();
		//
	}
	// ALST(1/7) ⇒  ALST 
	String	divide(String s){
		Csv	cs	= new Csv(s,"()");
		return  cs.get(0);
	}
	// (REPLACE)
	// ハッシュテープル(htb)を使って key で特定される出力処理を行う
	//
	@Override
	public void	write(String originalKey,StringBuffer out,Hashtable htb){
		//
		String subkey	=  divide(originalKey);
		//
		if(subkey.equals("ENL")){
			ENL(originalKey,out,htb);
		
		}else if(subkey.equals("EWL")){
			EWL(originalKey,out,htb);
			
		}else if(subkey.equals("ETF")){
			ETF(originalKey,out,htb);
			
		}else if(subkey.equals("ERB")){
			ERB(originalKey,out,htb);
			
		}else if(subkey.equals("ETA")){
			ETA(originalKey,out,htb);
			
		}else if(subkey.equals("ALST")){	// ENL の選択肢一覧
			ALST(originalKey,out,htb);
			
		}else{
			System.out.println("class CreateHtml #write() : キーの指定が間違っている．key:" + originalKey);
			return;
			//
		}
	}
	//
	/*
		解答の語群を表示する　
		
		ALST(2/7)で ２はオブジェクト番号,７ は列数指定．
		　２であれば、ENL(2,*) の語群を列挙する．
		　７であれば、%&xtblColumn%　～ %% を７回実行する
		  ７でなく"－"であれば規定値として５回実行する
		  %&xtblRaw% ～ %% の繰り返しは、語群の語数により自動
			
			■語群表
			
			%&xtblRaw%       行の繰り返し
			%&xtblColumn%    列の繰り返し
			
			%_classname%     スタイルクラス名
			%_word%          選択語
			
			#wordtable
			<table border="0" class="xtb">
			<tr>
			<td class="xtd">
			<table border="0">
			%&xtblRaw%
			<tr>
			%&xtblColumn%
			<td class="%_classname%">%_word%</td>%%
			</tr>%%
			</table>
			</td>
			</tr>
			</table>
	*/
	void	ALST(String key,StringBuffer out,Hashtable htb){
		//
		// 置換対象のHTML
		String	htmlDoc	= tb.get("wordtable");				// 最後の行から改行コードを除くように動作する
		Replace	rp		= new Replace(htmlDoc,htb,this);	// 反復処理なので this が必要
		//
		String	str	=getString(key,'(',')');				// 例：ALST(1/6) , ALST(1/6/2-4-3-1) から　( )内を取り出す 
		Csv 	cs	= new Csv(str,"(/)");
		int		grp	= Integer.parseInt( cs.get(0) ) - 1;	// グループ番号を０オリジンに
		//
		rp.put("name",  "ALST");							//
		rp.put("group", String.valueOf(grp));				// ゼロオリジンのグループ番号．
		rp.put("column",cs.get(1));							// (2/7) ⇒ 7
		//
		EnlObject	item	= enl.get(grp);					// グループオブジェクト
		int			size	= item.dispSize();				// ダミーを含めた表示項目数
		rp.put("max",String.valueOf(size));					// ハッシュに
		//
		rp.replacing();
		out.append(rp.getText());
	}
	//
	// 文字列 source の中で、from と to で挟まれた部分を取り出す
	String	getString(String source,char from ,char to){
		//
		int	pos1	= source.indexOf(from);
		if(pos1 < 0)		return	"";			// 開始文字がない
		int pos2	= source.indexOf(to);
		if(pos2 < 0)		return	"";			// 終了文字がない
		if((pos2-pos1)<=1)	return	"";			// [] で中身がないか][ で順序が逆
		//
		return	source.substring(pos1+1,pos2);
	}
	//
	// 記述項目を入力するテキストフィールドを出力する
		/*
			■テキストエリア
			%_name% ------ フォームオブジェクト名
			%_classname% -- 表示用スタイルシートクラス名
			%_length% ------- カラム数
			%_height% ---- 行の高さ
			
			#textArea
			<textarea name="%_name%" class="%_classname%" style="width: %_length%px; height: %_height%px;"></textarea>
		*/
	//
	void	ETA(String key,StringBuffer out,Hashtable htb){
		//
		cETA item = eta.get(key);
		if(item == null){
			System.out.println("class CreateHtml #ETA() : item is null !");
			return;
		}
		// 表示幅の決定
		int	k		= item.fontLevel();				// 1～7 
		int	line	= lineHeight[k];				// ライン高（ PX 単位）
		int	height	= line * item.getRows() + 6;	// 
		//
		int	font	= fontSize[k];					// フォント大きさ（ PX 単位）
		int    length	= font * item.getCols();
		//
		//
		// 置換対象のHTML
		String	htmlDoc	= tb.get("textArea");		// 最後の行から改行コードを除くように動作する
		//
		htb.put("_name",key);						//
		htb.put("_classname",txaClass[k-1]);		//
		htb.put("_height",String.valueOf(height));	// 表示高(px)
		htb.put("_length",String.valueOf(length));	// 表示幅(px)
		htb.put("_init","");	// 初期値
		//
		Replace	rp	= new Replace(htmlDoc,htb);		// 単なる書き換え
		rp.replacing();
		out.append(rp.getText());
		
	}
	//
	// ラジオボタンまたはチェックボックスによる選択項目を出力する.
	// 複数正解のときチェックボックスになる
	
		/*
			■ラジオボタン
			%_name% ------- フォームオブジェクト名
			%_chk%  ------- チェックされたかどうか（生成時は "" ）/ checked
			%_msg% -------- 選択肢内容
			%_classname% -- 表示用スタイルシートクラス名
			
			#radioButton
			<input type="radio" name="%_name%" value="1" class="%_classname%" %_chk% >%_msg%
			
			■チェックボックス
			%_name% ------- フォームオブジェクト名
			%_chk%  ------- チェックされたかどうか（生成時は "" ）/ checked
			%_msg% -------- 選択肢内容
			%_classname% -- 表示用スタイルシートクラス名
			
			#checkBox
			<input type="checkbox" name="%_name%" id="%_name%" value="1" class="%_classname%" %_chk% >%_msg%
		*/
	//
	void	ERB(String key,StringBuffer out,Hashtable htb){
		//
		Csv 	cs 		= new Csv(key,",)");				// ERB(1,3) ⇒ ERB(1  3  ""
		String	mkey	= cs.get(0) + ")";					// ERB(1)
		String	val		= cs.get(1);
		int	skey	= Integer.parseInt(val) -1;			// 3 -1 = 2 （０オリジンだから）
		//
		cERB item = erb.get(mkey);
		if(item == null){
			System.out.println("class CreateHtml #ERB() : item is null !");
			return;
		}
		//
		// テンプレートの選択
		Replace	rp	= null;		// 単なる書き換え
		String htmlDoc;
		if(item.isCheckbox()){
			htmlDoc	= tb.get("checkBox");
			rp		= new Replace(htmlDoc,htb);
			rp.put("_name",key);				// ERB(1,1) など（個々の項目で名前を変えねばならない）
		}else{
			htmlDoc	= tb.get("radioButton");
			rp		= new Replace(htmlDoc,htb);
			rp.put("_name",mkey);				// ERB(1) など
		}
		//
		// 置換対象のHTML
		int	level	= item.fontLevel();				// 1～7 
		rp.put("_classname",rbClass[level]);		//
		rp.put("_msg", item.dispOf(skey));			// 表示すべき文字列
		rp.put("_chk","");							// どれもチェックされていない
		rp.put("_val",val);							// value の値［添え字から、ERB(1,3) なら　3 になる］
		rp.replacing();
		out.append(rp.getText());
	}
	//
	// 記述項目を入力するテキストフィールドを出力する
		/*
			■テキストフィールド
			%_name% ------- フォームオブジェクト名
			%_length% ----- フィールド長
			%_classname% -- 表示用スタイルシートクラス名
			
			#textField
			<input type="text" name="%_name%"  class="%_classname%" style="width: %_length%px">
		*/
	//
	void	ETF(String key,StringBuffer out,Hashtable htb){
		
		cETF item = etf.get(key);
		if(item == null){
			System.out.println("class CreateHtml #ETF() : item is null !");
			return;
		}
		
		
	////////// 2023.05 修正 //////////////////////////////////////////////////////////////////////////////////////
	// 異常にサイズが大きくなるので、固定長に変更する
	// epm.txtのETFも、padding: 3px; を追加した
		
		// 表示幅の決定
		int	k		= item.fontLevel();			// 1～7 
	//	int	font	= fontSize[k];				// フォント大きさ（ PX 単位）
	//	int len		= item.getWidth();			// 最長文字数
	//	int	size	= font * len;
		
		int font = 16;
		int len	  = item.getWidth();			// 最長文字数

		int size = 25; 
		if(len < 10) 		size = 10*5;
		else if (len <15)	size = 15*5;
		else if (len <20)	size = 20*5;
		else if (len <25)	size = 25*5;
		else if (len <30)	size = 30*5;
		else if (len <35)	size = 35*5;
		else 				size = 40*5;

		//
		// 置換対象のHTML
		String	htmlDoc	= tb.get("textField");		// 最後の行から改行コードを除くように動作する
		//
		htb.put("_name",key);						//
		htb.put("_classname",inpClass[k]);			//

	//	htb.put("_length",String.valueOf(size));	// 表示幅(px)
		htb.put("_lenx",String.valueOf(size));		// 表示幅(px)
		htb.put("_alx", "left");
		
		htb.put("_init","");	// 初期値
		//
		Replace	rp	= new Replace(htmlDoc,htb);		// 単なる書き換え
		rp.replacing();
		out.append(rp.getText());
		
	}
	//
	// 数値番号を入力するテキストフィールドを出力する
	//
		/*
			■テキストフィールド
			%_name% ------- フォームオブジェクト名
			%_length% ----- フィールド長
			%_classname% -- 表示用スタイルシートクラス名
			
			#textField
			<input type="text" name="%_name%"  class="%_classname%" style="width: %_length%px">
		*/
	//
	void	ENL(String key,StringBuffer out,Hashtable htb){
		//
		cENL item = enl.get(key);
		if(item == null){
			System.out.println("class CreateHtml #ENL() : item is null !");
			return;
		}
		int		k	= item.fontLevel();
		String	htmlDoc	= tb.get("textField");
		//
		htb.put("_name",key);				//
		htb.put("_classname",numClass[k]);	//
		
		
		///////////////////////////////////////////////////////
		htb.put("_lenx","25");			// 固定値
		htb.put("_alx", "center");		// 中央揃え
		////////////////////////////////////////////////////////
		
		
		htb.put("_init","");	// 初期値
		//
		Replace	rp	= new Replace(htmlDoc,htb);	// 単なる書き換え
		rp.replacing();
		out.append(rp.getText());
	}
	//
	// 選択肢を入れたドロップダウンリスト
		/*
			■ドロップダウンリスト( println で書く)
			%_name% ------- フォームオブジェクト名
			%_val% -------- 選択された値
			%_Item% ------- キャプション
			%_Init% ------- 初期のキャプション（value=0,selected）
			%_sel% -------- 選択されたかどうか（生成時は "" ）/ selected
			%_classname% -- 表示用スタイルシートクラス名
			%@dropdownList% は反復指示子
			
			@dropdownlist
				<select name="%_name%" class="%_classname%">
				<option value="0" %_init% >------</option>
				%&dropdownItems%
				<option value="%_val%" %_select%>%_item%    %_sel% </option>%%
				</select>
		*/
	//
	void	EWL(String key,StringBuffer out,Hashtable htb){
		cEWL item = ewl.get(key);
		htb.put("item_key",key);
		if(item == null){
			System.out.println("class CreateHtml #EWL() : item is null !");
			return;
		}
		int		k		= item.fontLevel();
		String	htmlDoc	= tb.get("dropdownlist");
		//
		// <select name="%_name%" class="%_classname%">
		// <option value="0" %_init% >------</option>
		//
		htb.put("_name",key);				//
		htb.put("_classname",inpClass[k]);	//
		htb.put("_init","selected");	//
		//
		Replace	rp	= new Replace(htmlDoc,htb,this);// 再帰的書き換え
		rp.replacing();
		out.append(rp.getText());
	}
	//-----------------------------------------------------------------------------------
	//
	// (OPTION)
	// ハッシュテーブル(htb)を使って部分的に切り取ったソースデータ（ext）の
	// 内容を置き換えて out に出力する．表などの反復出力に利用する．
	// 個々の処理内容は、key で特定される．
	//
	@Override
	public void	write(String key,StringBuffer out,Hashtable htb,StringBuffer ext){
		//
		//
		if(key.equals("dropdownItems")){
			dropdownItemsMethod( key, out, htb, ext);
			//
		}else if(key.equals("xtblRaw")){
			xtblRawMethod( key, out, htb, ext);
			//
		}else if(key.equals("xtblColumn")){
			xtblColumnMethod( key, out, htb, ext);
			//
		}else{
			System.out.println("class CreateHtml (OPTION) #write() : キーの指定が間違っている．key:" + key);
			return;
			//
		}
	}
	//
	/*
	    EWL() の出力から呼び出され、選択肢の出力のため下記の置き換えを行う
		<option value="%_val%" %_select%>%_item%    %_sel% </option>
	*/
	void	dropdownItemsMethod(String key,StringBuffer out,Hashtable htb,StringBuffer ext){
		String	item_key	= (String)htb.get("item_key");
		cEWL 	item 		= ewl.get(item_key);
		if(item == null){
			System.out.println("class CreateHtml #write() : item is null !");
			return;
		}
		String	htmlDoc	= ext.toString();
		Replace	rp		= new Replace(htmlDoc,htb);	// 単なる書き換え
		//
		rp.put("_sel","");
		int	n = item.sizeOfItems();	// 選択肢の数
		String dsp;
		for(int k=0; k<n; k++){
			dsp		= item.getItem(k);
			rp.put("_item",dsp);
			rp.put("_val", String.valueOf(k+1));	// rp の中のhashに対する操作
			rp.put("_select","");					// どれも選択されていない
			rp.replacing();
			out.append(rp.getText());
			rp.init();	// 次の繰り返しのため必ず実行する
		}
	}
	//
	/*
		解答の語群を表示する　
		
		ALST(2/7/3-2-4-1-5) で は選択肢並び順が指定されているのでこの順で並べる
		//
		ALST(2/7)で ２はオブジェクト番号,７ は列数指定．
		　２であれば、ENL(2,*) の語群を列挙する．
		　７であれば、%&xtblColumn%　～ %% を７回実行する
		  ７でなく"－"であれば規定値として５回実行する
		  %&xtblRaw% ～ %% の繰り返しは、語群の語数により自動
			
			■語群表
			
			%&xtblRaw%       行の繰り返し
			%&xtblColumn%    列の繰り返し
			
			%_classname%     スタイルクラス名
			%_word%          選択語
			
			#wordtable
			<table border="0" class="xtb">
			<tr>
			<td class="xtd">
			<table border="0">
			%&xtblRaw%
			<tr>
			%&xtblColumn%
			<td class="%_classname%">%_word%</td>%%
			</tr>%%
			</table>
			</td>
			</tr>
			</table>
			
	　　■ htbのパラメータ
		Csv cs	= new Csv(key,"(/)");
		htb.put("name",  cs.get(0));	// ALST(2/7) ⇒ ALST
		htb.put("group", String.valueOf( Integer.parseInt( cs.get(1) )-1) );	// ALST(2/7) ⇒ 2 - 1 = 1
		htb.put("column",cs.get(2));	// ALST(2/7) ⇒ 7
		htb.put("max",size);			// ダミーを含めた表示項目数
	*/
	// 行の書き出し
	void	xtblRawMethod(String key,StringBuffer out,Hashtable htb,StringBuffer ext){
		if(DBG.fa) DBG.outHash(htb,"class CreateHtml #xtblRawMethod() : 行の書き出し の先頭です");
		//
		Replace		rp		= new Replace(ext.toString(),htb, this);
		int			cols	= Integer.parseInt( (String)htb.get("column") );	// １行に表示する項目数
		int			size	= Integer.parseInt( (String)htb.get("max") );		// ダミーを含めた表示項目数
		//
		int	pos = 0;
		for(int k=size; k > 0; k=k-cols ){
			//
			rp.put("from",String.valueOf( size - k ));// スタート番号（ゼロオリジン）
			out.append( rp.subst() );
		}
	}
	// 列の書き出し
	void	xtblColumnMethod(String key,StringBuffer out,Hashtable htb,StringBuffer ext){
		if(DBG.fa) DBG.println("class CreateHtml #xtblColumnMethod() : 列の書き出し の先頭です");
		//
		Replace		rp		= new Replace(ext.toString(),htb, this);
		//
		int			from	= Integer.parseInt( (String)rp.get("from") );		// 出力開始番号（ゼロオリジン）
		int			max		= Integer.parseInt( (String)rp.get("max") );		// 総出力個数
		int			cols	= Integer.parseInt( (String)rp.get("column") );		// １行の項目数
		int			grp		= Integer.parseInt( (String)rp.get("group") );		// グループ番号
		EnlObject	obj		= enl.get(grp);										// グループオブジェクト
		int		fontLevel	= ((enl.get(grp)).get(0)).fontLevel();				// 第０番目の項目から取ったフォントサイズ
		rp.put("_classname",tdClass[fontLevel]);								// 表示用スタイルクラス名（フォントサイズに連動）
		//
		int			remain 	= max - from;	// 総残り個数
		//
		if(DBG.fa) {
			DBG.println("from/"+from+"　max/"+max+"　cols/"+cols+"　grp/"+grp+"　fontLevel/"+fontLevel+"　remain/"+remain);
		}
		if(cols < remain){
			for(int	k=from; k<(from+cols); k++){
				//
				String	dispStr	= obj.dispItem(k);	// dispItem は番号付き
				rp.put("_word",dispStr);			// htbに入れる
				out.append( rp.subst() );
			}
		}else{
			for(int	k=from; k<(from+remain); k++){
				String	dispStr	= obj.dispItem(k);
				rp.put("_word",dispStr);			// htbに入れる
				out.append( rp.subst() );
			}
			for(int	k=(from+remain); k<(from+cols); k++){
				String	dispStr	= "　";				// 空白文字
				rp.put("_word",dispStr);			// htbに入れる
				out.append( rp.subst() );
			}
		}
	}
}