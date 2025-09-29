
package epml;
//import java.io.*;
//import java.text.*;
import java.util.Hashtable;
import epml.tools.Csv;
import epml.tools.DBG;
//
/**
	 解答済みのEPML文の記述項からHTMLを生成する<br>
	 
	 ただし、ハッシュにあるユーザー入力値で初期値を設定する
	 解答済みのEPML文からHTMLを生成するのに用いる
	 
	 学生が試験問題解答中に一時保存した後、再表示する時に用いる
	 また、教師が学生の解答をWeb形式で閲覧するときもこのクラスを使用する
	 
	 クラス変数などを流用するため CreateHtml から派生させている

*/
public class CreateInitializedHtml extends CreateHtml {
	
	Hashtable	data;	// 試験問題に対するユーザー入力データを持つハッシュテーブル
	
	public CreateInitializedHtml(Hashtable	_htb,
						String				htmlPath,
						EmbededNumberLists		_enl,
						EmbededWordLists		_ewl,
						EmbededTextFields		_etf,
						EmbededRadioButtons		_erb,
						EmbededTextAreas		_eta){
		
		super(htmlPath,_enl,_ewl,_etf,_erb,_eta);
		
		if(DBG.fa) DBG.println("class CreateInitializedHtml #コンストラクタ の先頭です");
		//
		data	= _htb;
		
		//
	}
	// ハッシュテーブルからキーで値を検索して返す
	// null を返すことがある．
	// 文字列が空かどうか
	public boolean isEmptyData(String str){
		if(str==null) 					return  true;
		if((str.trim()).length()==0)	return  true;
		return false;
	}
	// ハッシュテーブルからキーで値を検索して返す
	// なければnull を返す
	public String strHashNL(Hashtable htb,String key){
		//
		String	str	= (String) htb.get(key);
		if(isEmptyData(str)){
			str = null;
		}
		return 	str;
	}
	// ハッシュテーブルからキーで値を検索して返す
	// null になる場合は "" を返す
	public String strHashSP(Hashtable htb,String key){
		//
		String	str	= (String) htb.get(key);
		if(isEmptyData(str)){
			str = "";
		}
		return 	str;
	}
	// ハッシュテーブルからキーで値を検索して返す
	// null になる場合は "0" を返す
	public String strHashZERO(Hashtable htb,String key){
		//
		String	str	= (String) htb.get(key);
		if(isEmptyData(str)){
			str = "0";
		}
		return 	str;
	}
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
	//
	// 記述項目を入力するテキストエリアを出力する
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
	@Override
	void	ETA(String key,StringBuffer out,Hashtable htb){
		if(DBG.fa) DBG.println("class CreateInitializedHtml #ETA() :  記述項目を入力するテキストエリアを出力する の先頭です");
		//
		cETA item = eta.get(key);
		if(item == null){
			System.out.println("class CreateHtml #ETA() : item is null !");
			return;
		}
		// 表示幅の決定
		int		k		= item.fontLevel();				// 1～7 
		int		line	= lineHeight[k];				// ライン高（ PX 単位）
		int		height	= line * item.getRows() + 6;	// 
		//
		int		font	= fontSize[k];					// フォント大きさ（ PX 単位）
		int     	length	= font * item.getCols();
		//
		//
		// 置換対象のHTML
		String	htmlDoc	= tb.get("textArea");		// 最後の行から改行コードを除くように動作する
		//
		htb.put("_name",key);						//
		htb.put("_classname",txaClass[k-1]);		//
		htb.put("_height",String.valueOf(height));	// 表示高(px)
		htb.put("_length",String.valueOf(length));	// 表示幅(px)
		//
		// init は初期表示文字列．textfield の value である．
		
		
		
		
		String	init	= strHashSP(data,key);		// 例えば、key = "ETA(1)"
		if(init.equals(Exam.NOT_DISCRIBED)){
			init	=	"";
		}
		htb.put("_init",init);
		//if(DBG._stwk) DBG.println("           ▼key  = " + key);
		//if(DBG._stwk) DBG.println("           ▼init = " + init);
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
	
	/**
	 * キーからグループ名を取り出す
	 * 
	 * @param key		ERB(a,b) の形式のキー値
	 * @return			文字列になおした "ERB(a)" 
	 * 
	 */
	String	getGroupNum(String key){
		Csv 	cs 		= new Csv(key,",)");		// ERB(1,3) ⇒ ERB(1  3  ""
		return	cs.get(0) + ")";					// ERB(1)
	}
	/**
	 * キーからグループ内のシーケンス番号文字を取り出す
	 * 
	 * @param key		ERB(a,b) の形式のキー値
	 * @return			b
	 */
	String	getSeqNum(String key){
		Csv 	cs 		= new Csv(key,",)");		// ERB(1,3) ⇒ ERB(1  3  ""
		return	cs.get(1);
	}
	/**
	 * キーからグループ内のシーケンス番号を作る
	 * 
	 * @param key		ERB(a,b) の形式のキー値
	 * @return			ゼロオリジンの整数になおしたbの値
	 */
	int	getSeqCount(String key){
		Csv 	cs 		= new Csv(key,",)");		// ERB(1,3) ⇒ ERB(1  3  ""
		String	val		= cs.get(1);
		return	Integer.parseInt(val) -1;			// 3 -1 = 2 （０オリジンだから）
	}
	
	
	@Override
	void	ERB(String key,StringBuffer out,Hashtable htb){
		if(DBG.fa) DBG.println("class CreateInitializedHtml #ERB() :  ラジオボタンまたはチェックボックスによる選択項目を出力する の先頭です");
		//
		// ラジオボタンでもチェックボックスでも、%ERB(a,b)% がEPMLに埋め込んであるので、
		// 常にkey は ERB(a,b)となる．　
		String	mkey	= getGroupNum(key);
		String	val		= getSeqNum(key);
		int	skey	= getSeqCount(key);
		
		cERB item = erb.get(mkey);
		//
		if(item == null){System.out.println("class CreateHtml #ERB() : item is null !"); return;} // 念のため
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
		int level	= item.fontLevel();				// 1～7 
		rp.put("_classname",rbClass[level]);		//
		rp.put("_msg", item.dispOf(skey));			// 表示すべき文字列
		rp.put("_val",val);							// value の値［添え字から、ERB(1,3) なら　3 になる］
		//
		// 選択されている番号とその正誤をハッシュから得る．
		String temp = "";
		if(item.isCheckbox()){
			// チェックボックスでは [ ERB(1,1) ,0] [ERB(1,2),1]  [ERB(1,3),1] のように複数ある
			// temp の値は 1,2,3,･･･ のいづれか．シーケンス番号に等しい
			// 1~- とか 1~t とか 1~t~ck などとなっている
			temp	= strHashSP(data,key);		
		}else{
			// ラジオボタンでは[ ERB(1) ,3 ]のように,ひとつしかない
			// 正解は何番目かを表わす値 1,2,3,･･･ のいづれか
			// 3~- とか 3~t などとなっている
			temp	= strHashSP(data,mkey);		// 
		}
		// ~以下はExam# createAnswerText() で学生の解答のepmlを作成するときに必ず付加されている
		Csv		csv		=	new Csv(temp,"~");
		String	init	=	csv.get(0);
		String	eval	=	csv.get(1);
		//
		// 不正解なら表示するテキストボックスの背景を警告色に変えるため、CSSのクラスをR付きのものに変更する
		if(eval.equals("f")){
			rp.put("_classname",rbClassR[level]);
		}
		//
		if(init.equals(val)){
			//if(DBG.fa){DBG.println("合致した：init=" + init + " / " + "val=" + val);}
			rp.put("_chk","checked");				// チェックされている
		}else{
			//if(DBG.fa){DBG.println("異なる：init=" + init + " / " + "val=" + val);}
			rp.put("_chk","");
		}
		//
		
		rp.replacing();
		out.append(rp.getText());
	}
	void	setForRadio(Replace rp, String init){
		
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
	@Override
	void	ETF(String key,StringBuffer out,Hashtable htb){
		if(DBG.fa) DBG.println("class CreateInitializedHtml #ETF() :  記述項目を入力するテキストフィールドを出力する の先頭です");
		//
		cETF item = etf.get(key);
		if(item == null){
			System.out.println("class CreateHtml #ETF() : item is null !");
			return;
		}
		// 表示幅の決定
		int	k		= item.fontLevel();			// 1～7 
		
	////////// 2023.05 修正 //////////////////////////////////////////////////////////////////////////////////////
	// 異常にサイズが大きくなるので、固定長に変更する
	// epm.txtのETFも、padding: 3px; を追加した
	
	//	int	font	= fontSize[k];				// フォント大きさ（ PX 単位）
	//	int len		= item.getWidth();			// 最長文字数
	//	int	size	= font * len;
	
		
		int font = 16;
		int len	  = item.getWidth();			// 最長文字数
		int size = 10; 
		
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
		
	/////////////////////////////////////////////////////////////////////////////////////////////////////////	
		
		//
		String	temp	=	strHashSP(data,key);	// 例えば、key = "ETF(1)"
		Csv		cs		=	new Csv(temp,"~");
		String	init	=	cs.get(0);
		if(init.equals(Exam.NOT_DISCRIBED)){
			init	=	"";
		}
		// 原因不明
		if(init.equals("-")){ init = ""; }
		//
		String	eval	=	cs.get(1);				// 正誤、または "-"(採点未済)
		htb.put("_init",init);
		//
		// 不正解なら表示するテキストボックスの背景を警告色に変えるため、CSSのクラスをR付きのものに変更する
		if(eval.equals("f")){
			htb.put("_classname",inpClassR[k]);
		}
		//
		//if(DBG._stwk) DBG.println("           ▼key  = " + key);
		//if(DBG._stwk) DBG.println("           ▼init = " + init);
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
	@Override
	void	ENL(String key,StringBuffer out,Hashtable htb){
		if(DBG.fa) DBG.println("class CreateInitializedHtml #ENL() :  数値番号を入力するテキストフィールドを出力する の先頭です");
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
		
		
		//
		// 解答をハッシュ(data)から取り出す
		// 解答はその正誤を含む形式なのでCsvオブジェクトで分離する[ ref. EmbededNumberLists#setHash() ]
		// 例えば、解答番号が５ならば、解答データは、5~t, 5~f, 5~- のいづれか．
		// 付加された文字で、"t"は正解、"f"は不正解、"-"は採点未済の場合を表す
		//
		String	temp	=	strHashSP(data,key);	// 例えば、key = "ENL(1,2)"
		Csv		cs		=	new Csv(temp,"~");
		String	init	=	cs.get(0);				// 解答番号
		String	eval	=	cs.get(1);				// 正誤、または "-"(採点未済)
		// 原因不明
		if(init.equals("-")){ init = ""; }
		if(init.equals("0")){ init = ""; }
		//
		htb.put("_init",init);						// 表示する解答番号をセット
		//
		// 不正解なら表示するテキストボックスの背景を警告色に変えるため、CSSのクラスをR付きのものに変更する
		if(eval.equals("f")){
			htb.put("_classname",numClassR[k]);
		}
		//if(DBG._stwk) DBG.println("           ▼key  = " + key);
		//if(DBG._stwk) DBG.println("           ▼init = " + init);
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
			<option value="0" selected>-選択－</option>
			%&dropdownItems%
			<option value="%_val%">%_item%    %_sel% </option>%%
			</select>
		*/
	//
	@Override
	void	EWL(String key,StringBuffer out,Hashtable htb){
		if(DBG.fa) DBG.println("class CreateInitializedHtml #EWL() :  選択肢を入れたドロップダウンリス の先頭です");
		//
		cEWL item = ewl.get(key);
		htb.put("item_key",key);
		if(item == null){
			System.out.println("class CreateHtml #EWL() : item is null !");
			return;
		}
		int	k		= item.fontLevel();
		String	htmlDoc	= tb.get("dropdownlist");
		//
		// <select name="%_name%" class="%_classname%">
		// <option value="0" %_init% >------</option>
		//
		htb.put("_name",key);						//
		htb.put("_classname",inpClass[k]);			//
		// 
		String	temp	= 	strHashZERO(data,key);	// なければ"0"を返すが、常に何かの値がある
		Csv		cs		= 	new Csv(temp,"~");
		String	init	=	cs.get(0);
		String	eval	=	cs.get(1);
		if(eval.equals("f")){
			htb.put("_classname",inpClassR[k]);
		}
		// key で引けるハッシュ値に正誤情報を含まない選択番号を入れなおしておく
		// これは、選択肢リストでどの項目をを初期表示するか決定するために dropdownItemsMethod() で使う
		data.put(key,init);
		htb.put("itemKey",key);	// 例えば EWL(2) などを記憶しておく
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
	@Override
	void	dropdownItemsMethod(String key,StringBuffer out,Hashtable htb,StringBuffer ext){
		if(DBG.fa) DBG.println("class CreateInitializedHtml #dropdownItemsMethod() :  EWL() の出力から呼び出され、選択肢の出力のため下記の置き換えを行う の先頭です");
		//
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
			String	number	= String.valueOf(k+1);
			rp.put("_val",number );	// rp の中のhashに対する操作
			//
			//  ｋ番目のリスト項目を埋め込む処理の中で、その項目が選ばれている項目であれば
			//  この問題項目のキーが EWL(2) の値のとき、data の中のEWL(2)の値はｋである．
			//  これを調べて、ｋ番目のリスト項目に "selected" をつけるかどうか決める
			//  
			//  EWL(2) のような問題項目キーはEWLの処理メソッドでhtbの中にitem_keyとして記憶
			//  してあるので、これを使って data の中身を調べる
			//
			String	EWL_KEY	= strHashSP(htb,"itemKey");		// itemKey は例えば "EWL(2)" など
			String	sel		= strHashZERO(data,EWL_KEY);	// この値が data で何になっているか調べる
			if(sel.equals(number)){
				htb.put("_select","selected");
			}else{
				htb.put("_select","");
			}			
			rp.replacing();
			out.append(rp.getText());
			rp.init();	// 次の繰り返しのため必ず実行する
		}
	}
}