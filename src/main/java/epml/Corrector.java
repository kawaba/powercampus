package epml;
import	epml.tools.*;
import java.io.*;
import java.util.*;

//
/*
		記述式問題項目（TextField,TextArea）について、解答が合っているか否かを決めるための一覧表を
		表示するHTMLを生成して返す．
		
		項目数の回数だけ checkListHtml(int k) を呼ぶことで、各々のHTMLを取得できる．
		
		コンストラクタでは誤解答を収集するが、その結果は引数のExam オブジェクト ans に残っている．
		

*/
public class Corrector extends OptionWriter {
	//
	final String CR 		= System.getProperty("line.separator");
	final String CHK_WIDTH	= "700";	// 解答語修正HTMLでの表示幅
	Exam				exam;
	EmbededTextFields 	answer;
	String 				parsedText;		// 正解と不正解を蓄積したEPML原文のテキスト
	Replace				rp;
	TemplateBox			tb;
	//
	String				emlPath;		// 設定ファイルへのパス     ex.  /var/pc/conf/exwork.conf
	//
	// 正解と不正解を蓄積したEPML原文のテキストを作成する時に使用するコンストラクタ
	// correctEPML() を引き続き実行する
	public	Corrector(String _emlPath){
		if(DBG.fa) DBG.println("class Corrector #コンストラクタ の先頭です");
		//
		emlPath		= _emlPath;
		exam		= null;
		answer		= null;
		parsedText	= null;
		rp			= null;
		tb			= null;
		//
	}
	// 正解と不正解を蓄積したEPML原文のテキストを作成する
	public	String correctEPML(Exam _ans,Vector chg){
		if(DBG.fa) DBG.print("■■■ Corrector #correctEPML() の先頭です");
		//
		exam			= _ans;
		parsedText		= exam.doEPML();				// パースされた中間テキスト（原文の中の特定の１行を見つけるのに使う）
		answer			= exam.getETF();				// 正解の ETF
		//
		emlPath			= exam.getemlPath();			// eml [epml] ライブラリの設定ファイルへのパス．ここでは exwork.conf を利用している
		String	epPath	= exam.epmlPath();				// epml.txt
		String	hmPath	= exam.hmacPath();				// hmac.txt
		tb				= new TemplateBox(epPath);
		//
		int		max	= chg.size();

		for(int	k=0; k<max; k++){
			String	epml	= (String)chg.get(k);					// ユーザの解答から作成したepml
			Exam charange	= new Exam(epml,emlPath);				// ｋ番目の解答を得て
			charange.doEPML();										// 変数を初期化し
			answer.preGrades(charange.getETF());					// 正解と突合せ、不正解語をanswer に蓄積する
		}
		
		//
		return	exam.toEpml();	// epml 原文テキストに直したものを返す（蓄積データ付き）
	}
	//
	/*	
		第ｋ番目の問題項目のチェックリストのHTMLを返す．
		
	　　 _html で置き換えるデータ（#incorects）
	
	#checkList
		
		<table border="0">
		<tr>
		<td ><img src="img/spacer.gif" alt="" width="25" height="1"></td>
		<td class="k14"><pre>
		■ 問題の該当行
		　
		%_htmls%</td>
		</tr>
		</table>
		<hr style="color: blue"><p>
		<table border="0">
		<tr>
		<td ><img src="img/spacer.gif" alt="" width="15" height="1"></td>
		<td class="xtd">
		<table border="0">
		<tr>
		<td  class="k14">■ 過去に追加した正解語（訂正可能）<br></td>
		%&correctRaw%
		<tr>
		%&correctColumn%
		<td  class="k12" style="padding-right: 15px;"><input name="correctRate_%_num%" type="text" class="num10" style="width: 22px;"><span style="border-bottom: 1px dotted blue;"><input type="checkbox" name="correct%_num%" value="1" class="k12" style="valign: middle;" >%_word%</span></td>%%
		</tr>%%
		<td  class="k14">■ 今回の不正解語一覧（正解語へ追加可能）<br></td>
		%&incorrectRaw%
		<tr>
		%&incorrectColumn%
		<td  class="k12" style="padding-right: 15px;"><input name="incorrectRate_%_num%" type="text" class="num10" style="width: 22px;"><span style="border-bottom: 1px dotted blue;"><input type="checkbox" name="incorrect%_num%" value="1" class="k12" style="valign: middle;" >%_word%</span></td>%%
		</tr>%%
		</table>
		</td>
		</tr>
		</table>
	*/
	//　第ｋ番目の問題項目のチェックリストのHTMLを取得する処理の時に使用するコンストラクタ
	//　correctEPML()で作成し、保存していた epml原文を入力に使う
	//　引き続き hawmany() や checkListHtml() 、update() を実行する
	public	Corrector(String epml,String _emlPath){
		if(DBG.fa) DBG.println("class Corrector #コンストラクタ の先頭です");
		//
		emlPath		= _emlPath;
		exam		= new Exam(epml,emlPath);
		parsedText	= exam.doEPML();
		answer		= exam.getETF();

		tb			= new TemplateBox(exam.epmlPath());
		rp			= null;
	}
	/**
	 * exam にグラフィックスのURLを伝える
	 * @param graphicUrl
	 */
	public	void	setImgPath( String	graphicUrl ){
		exam.setImgPath( graphicUrl );
	}
	/**
	 * exam にグラフィックスの絶対パスを伝える
	 * @param graphicPath
	 */
	public void	setImgDestinationPath( String graphicPath ){
		exam.setImgDestinationPath(graphicPath );
	}
	/**
	 * exam に資料データでないことを伝え、HTMLの中のグラフィックパス
	 * の記述に指定したパスを使わせるようにする
	 */
	public void	setNonHtmlFlag(){
		exam.setNonHtmlFlag();
	}
	
	//
	//  現在のハッシュにある内容で、問題EPMLを書き換える
	//  ハッシュの内容は、「checkListHtml(int k)が生成する正解語の採択を処理するHTML」の実行後出力である．
	//
	public	String	update(Hashtable htb){
		if(DBG.fa) DBG.println("class Corrector #update() : 現在のハッシュにある内容で、問題EPMLを書き換える の先頭です");
		//
		//  ハッシュから対象となる項目名とシーケンス番号を得る
		//
		String	key	= strHash(htb,"_key");
		Csv		cs	= new Csv(key,"()");	// ETF(1)  ⇒ s(0) = ETF  cs(1) = 1
		//
		// 正解語リストを入手しその個数ｎを調べる．
		// 空の正解語リストと空の不正解語リストを作成する
		// ハッシュに入っている正解語のフォーム変数名は correctNum_(n) である．その値があれば（１ならば）
		// (n)番目の正解語リストを空の正解語リストに add する．そうでなければ、不正解語リストへ加える
		// 最後まで処理したら、元の正解語リストをクリアし、新たな正解語リストで更新する
		//
		// 不正解語リストを入手しその個数ｎを調べる
		// 不正解語 incorrectNum_(n) がハッシュに入っていれば、これを正解語リストに加え、そうでなければ空の不正解語リストに加える
		// 最後まで処理したら、元の不正解語リストをクリアし、新たな不正解語リストで更新する
		//
		// 正解語リストと不正解語リストが書き換わったので Exam#toEpml() を使って状態が変化したEPMLをEPML文に書き戻す
		// 関数の戻り値として、書き換えたEPML文を返す
		//
		int	pos			= Integer.parseInt(cs.get(1)) -1 ;	// ゼロオリジンに
		Vector	clist		= answer.correctList(pos);
		int	cmax		= clist.size();
		Vector	corrects	= new Vector(10,10);
		Vector	incorrects	= new Vector(10,10);
		for(int	k=0; k<cmax; k++){
			String	target	= "correctNum_" + String.valueOf(k);
			if(strHash(htb,target)!=null){
				corrects.add( (String)clist.get(k) );
			}else{
				incorrects.add( (String)clist.get(k) );
			}
		}
		//
		Vector	ilist		= answer.incorrectList(pos);
		int		imax		= ilist.size();
		for(int	k=0; k<imax; k++){
			String	target	= "incorrectNum_" + String.valueOf(k);
			if(strHash(htb,target)!=null){
				corrects.add( (String)ilist.get(k) );
			}else{
				incorrects.add( (String)ilist.get(k) );
			}
		}
		if(corrects.size() !=0){
			answer.updateAllCorrectWords(pos,corrects);
			answer.updateAllIncorrectWords(pos,incorrects);
		}
		//
		if(DBG.fa){
			EmbededTextFields test = exam.getETF();
			Vector v1 = test.correctList( pos);
			Vector v2 = test.incorrectList( pos);
			DBG.outVector(v1,"【正解リスト】");
			DBG.outVector(v2,"【不正解リスト】");
		}
		String	newEPML	= exam.toEpml();
		//
		if(DBG.fa){
			DBG.println("【変換EPML文-再】");
			DBG.println(newEPML);
		}
		return	newEPML;
		
	}
	//
	// 問題項目はいくつあるか
	//
	public	int		howmany()	{ return	answer.size(); }
	//
	// ｋ番目の問題項目のチェック用ＨＴＭＬを返す
	//
	public	String	checkListHtml(int k){
		if(DBG.fa) DBG.println("class Corrector #checkListHtml() : 第ｋ番目の問題項目のチェックリストのHTMLを返す． の先頭です");
		//
		// 記述式項目がひとつもない時は、その由を表示するHTMLを返す
		if(howmany()==0){
			return	tb.get("toNextMsg");
		}
		// 記述式項目がひとつもない時は、k<0 で呼ばれる可能性があるのでそれに対処
		if(howmany()<0){
			return	tb.get("toNextMsg");
		}
		//
		// 置換対象のHTML
		String	htmlDoc	= tb.get("checkList");			// 最後の行から改行コードを除くように動作する
		Hashtable htb	= new Hashtable(30);
		rp	= new Replace(htmlDoc,htb,this);			// 反復処理なので 処理する関数が必要．このクラスの中で準備するのでthisを指定する．
		//
		// 単純置き換えのターゲット表示HTML
		rp.put("_htmls",targetHtml(k));						// 問題の対象個所１行を表示するHTML
		rp.put("column","4");								// １行に表示すべき語数
		rp.put("_key","ETF(" + String.valueOf(k+1) + ")");	// 項目名
		//
		// 正解語
		Vector	correctWords	= correctList(k);		// 正解語リストを取得して
		rp.put("correctWords",correctWords);	 		// ハッシュにいれておく
		//
		// 不正解語
		Vector	words	= incorrectList(k);				// 不正解語リストを取得して
		rp.put("words",words);							// ハッシュにいれておく
		//
		return	rp.subst();	// 置き換え結果を返す
	}
	// 問題項目数
	int	size(){
		return answer.size();
	}
	//
	// 第ｋ番目の正解語のリスト
	Vector	correctList(int k){
		//
		return	answer.correctList(k);
	}
	//
	// 第ｋ番目の問題項目の不正解語のリスト
	Vector	incorrectList(int k){
		//
		return	answer.incorrectList(k);
	}
	/*
		不正解リスト表示HTMLの参照個所を示すためのHTMLを返す
		ETF(1) ～ ETF(n) までのｋ番目の項目についてであるから、「正規でないのは%@ETF(1)%番です」のように
		問題原文がパースされている．パースした問題文を得て、%@ETF(k)% を含む行を抽出し参照行として表示する．
	*/
	public	String	targetHtml(int k){
		if(DBG.fa) DBG.println("class Corrector #targetHtml() : 不正解リスト表示HTMLの参照個所を示すためのHTMLを返す の先頭です");
		// 
	  	//String	red_1	= "<span style='border-bottom: 2px solid red'>";
	  	//String	red_2	= "</span>";
		String	red_1	= "_SPAN_RED_FROM_";
		String	red_2	= "_SPAN_RED_TO_";
		String	tx01	= null;
		String	tx02	= null;
		String	tx03	= null;
		//
		String	targetName	= "%@ETF(" + String.valueOf(k+1) + ")%";	// １オリジン
		String	targetRed	= red_1 + targetName + red_2;
		//
		/*
		if(DBG._epml){
			DBG.print("■ 正解をパースしたテキスト");
			DBG.print(parsedText);
			DBG.print("■ ターゲット名 -->:" + targetName + ":");
		}
		*/
		String	format	= "## flow = " + CHK_WIDTH + CR;		// flow で表示するため書式指定を追加する
		String temp = getTargerLine(parsedText,targetName);		// パースしたEPML文からターゲットを含む行を抽出する
		if(DBG.fa) DBG.println("★★(1) targetLine = " + temp);
		
		tx01 = substitute(format + temp,targetName,targetRed);	// ターゲットの個所の背景色を赤にする
		if(DBG.fa) DBG.println("★★(2) substtLine = " + tx01);
		
		//
		tx02 =	exam.doHMAC( tx01);	// HMACをパース
		if(DBG.fa) DBG.println("★★(3) substtLine(HMACをパース) = " + tx02);
		//
		tx03 =  exam.subst(tx02);	// EPML要素をHTMLに置き換え
		if(DBG.fa) DBG.println("★★(4) substtLine(EPML要素をHTMLに置き換え) = " + tx03);
		
		tx03 = substitute(tx03, "_SPAN_RED_FROM_", "<span style='border-bottom: 2px solid red'>");
		tx03 = substitute(tx03, "_SPAN_RED_TO_"  , "</span>");
		
		return	tx03;
	}
	// source の中からtargetName を含む行を見つけて返す
	String getTargerLine(String source,String targetName){
		if(DBG.fa) DBG.println("class Corrector #getTargerLine() : source の中からtargetName を含む行を見つけて返す の先頭です");
		//
		if((source==null)||(source.length()==0)) return "";
		//
		BufferedReader	in		= new BufferedReader(new StringReader(source));
		String			matched	= "";
		//
		try{
			while((matched=in.readLine())!=null){
				if(matched.length()>0){
					if(matched.indexOf(targetName)>=0){	// この行データ中に %@ETF(k)% がある
						return	matched;
					}
				}
			}
			in.close();
		}catch(IOException e){
		}
		return "";
	}
    //
    // ファイルデータをVectorに格納する
    public void loadToVector(String fpath,Vector Vhtml){
        BufferedReader in = null;
        String         line;
        try{
            in = new BufferedReader(new InputStreamReader(new FileInputStream(fpath),"Windows-31J"));
            while((line=in.readLine())!=null){
                if(line.length() > 0) { Vhtml.add(line); }
            }
        }catch (IOException e){
            System.out.println("paramPrint(): can't read :" + fpath);
        }
    }
    // ファイルのコピー
	public boolean copyFile(String sfile,String dfile){
		try{
			BufferedReader in  = new BufferedReader(new InputStreamReader(new FileInputStream(sfile),"Windows-31J"));
			PrintWriter	   out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(dfile),"Windows-31J")));
			//
            String dt;
            while((dt=in.readLine())!=null){
				out.println(dt);
			}
		    in.close();
			out.close();
		}catch(IOException e){
			System.out.println(e);
			return false;
		}
		return true;
	}
	// source 文字列の全ての target を rep に置き換える
	public String	substitute(String source,String target,String rep){
		//
		int	pos 		= source.indexOf(target);
		if(pos<0)	return	source;
		//
		int	len			= target.length();
		String	str1	= "";
		String	str2	= "";
		//
		try{
			str1	= source.substring(0,pos);
		}catch(IndexOutOfBoundsException  e){
			str1 = "";
		}
		try{
			str2	= substitute(source.substring(pos+len),target,rep);
		}catch(IndexOutOfBoundsException  e){
			str2 = "";
		}
		//
		return	str1 + rep + str2;
	}
	// 
	//  Replace から呼ばれる書き換えメソッド
	//
	//--------- OptionWriter の write() メソッドをオーバーロードする ----------------------------
	//
	// (OPTION)
	// ハッシュテーブル(htb)を使って部分的に切り取ったソースデータ（ext）の
	// 内容を置き換えて out に出力する．表などの反復出力に利用する．
	// 個々の処理内容は、key で特定される．
	//
	public void	write(String key,StringBuffer out,Hashtable htb,StringBuffer ext){
		if(DBG.fa) DBG.println("class Corrector #write() : 書き換えの分岐処理 の先頭です");
		//
		//
		if(key.equals("correctRaw")){
			correctRaw( key, out, htb, ext);
			//
		}else if(key.equals("correctColumn")){
			correctColumn( key, out, htb, ext);
			//
		}else if(key.equals("incorrectRaw")){
			incorrectRaw( key, out, htb, ext);
			//
		}else if(key.equals("incorrectColumn")){
			incorrectColumn( key, out, htb, ext);
			//
		}else{
			System.out.println("class Corrector (OPTION) #write() : キーの指定が間違っている．key:" + key);
			return;
			//
		}
	}	
	/*
	  ○正解語リストの行処理
	    　リストは１行に４個をリストアップする．
		　語数をチェックして制御する．
		　
		　準備すべき変数
	        num	---- ｋ番目のｋ
			word --- ｋ番目の不正解語
			
		　ext に受け取るhtml （%&correctColumn% がカラム制御の変数）
			<tr>
			　%&correctColumn%
			　<td  class="k12" style="padding-right: 15px;"><input name="rate_%_num%" type="text" class="num10" style="width: 22px;">
			　　<span style="border-bottom: 1px dotted blue;">
			　　　<input type="checkbox" name="ckb_%_num%" value="1" class="k12" style="valign: middle;" >%_word%
			　　</span>
			　</td>%%
			</tr>
	*/
	void correctRaw(String key,StringBuffer out,Hashtable htb,StringBuffer ext){
		if(DBG.fa) DBG.println("class Corrector #correctRaw() : 正解語リストの行処理 の先頭です");
		//if(DBG._epml)  DBG.println(ext.toString());
		//if(DBG._epml)  DBG.outHash(htb,"correctRaw() へ渡すデータ");
		//
		Replace		rp		= new Replace(ext.toString(),htb, this);
		//
		int		cols	= Integer.parseInt( (String)htb.get("column") );	// １行に表示する項目数
		Vector		words	= (Vector)htb.get("correctWords");					// ★正解語
		int		size	= words.size();										// 総表示項目数
		//
		int	pos = 0;
		for(int k=size; k > 0; k=k-cols ){
			//
			rp.put("from",String.valueOf( size - k ));	// スタート番号（ゼロオリジン）
			out.append( rp.subst() );
		}
		
	}
	/*
		　ext に受け取るhtml
				
			  <td  class="k12" style="padding-right: 15px;"><input name="rate_%_num%" type="text" class="num10" style="width: 22px;">
			　　<span style="border-bottom: 1px dotted blue;">
			　　　<input type="checkbox" name="ckb_%_num%" value="1" class="k12" style="valign: middle;" >%_word%
			　　</span>
			　</td>
	*/
	void correctColumn(String key,StringBuffer out,Hashtable htb,StringBuffer ext){
		if(DBG.fa) DBG.println("class Corrector #correctColumn() : 正解語リストのカラム処理 の先頭です");
		//
		Replace	rp		= new Replace(ext.toString(),htb, this);
		//
		int	from	= Integer.parseInt( (String)rp.get("from") );		// 出力開始番号（ゼロオリジン）
		Vector	words	= (Vector)htb.get("correctWords");						// ★正解語
		int	max		= words.size();										// 総表示項目数
		int	cols	= Integer.parseInt( (String)rp.get("column") );		// １行の項目数
		//
		int	remain 	= max - from;	// 総残り個数
		//
		//if(DBG._epml) {
		//	DBG.println("■ calss Corrector #correctColumn() での入力パラメータです ");
		//	DBG.println("from/"+from+"　max/"+max+"　cols/"+ cols + "　remain/"+remain);
		//}
		if(cols < remain){
			for(int	k=from; k<(from+cols); k++){
				//
				String	dispStr	= (String)words.get(k);	// 正解語
				rp.put("_word",dispStr);				// htbに入れる
				rp.put("_num",String.valueOf(k));
				out.append( rp.subst() );
			}
		}else{
			for(int	k=from; k<(from+remain); k++){
				String	dispStr	= (String)words.get(k);	// 正解語
				rp.put("_word",dispStr);				// htbに入れる
				rp.put("_num",String.valueOf(k));
				out.append( rp.subst() );
			}
			for(int	k=(from+remain); k<(from+cols); k++){
				String	dispStr	= "<td>　</td>";	// 空白
				out.append( dispStr );				// 変換しない
			}
		}
		
	}
	/*
	  ○不正解語リストの行処理
	    　リストは１行に４個をリストアップする．
		　語数をチェックして制御する．
		　
		　準備すべき変数
	        num	---- ｋ番目のｋ
			word --- ｋ番目の不正解語
			
		　ext に受け取るhtml （%&incorrectColumn% がカラム制御の変数）
			<tr>
			　%&incorrectColumn%
			　<td  class="k12" style="padding-right: 15px;"><input name="rate_%_num%" type="text" class="num10" style="width: 22px;">
			　　<span style="border-bottom: 1px dotted blue;">
			　　　<input type="checkbox" name="ckb_%_num%" value="1" class="k12" style="valign: middle;" >%_word%
			　　</span>
			　</td>%%
			</tr>
	*/
	void incorrectRaw(String key,StringBuffer out,Hashtable htb,StringBuffer ext){
		if(DBG.fa) DBG.println("class Corrector #incorrectRaw() : 不正解語リストの行処理 の先頭です");
		//
		Replace		rp		= new Replace(ext.toString(),htb, this);
		//
		int		cols	= Integer.parseInt( (String)htb.get("column") );	// １行に表示する項目数
		Vector		words	= (Vector)htb.get("words");							// ■不正解語
		int		size	= words.size();										// 総表示項目数
		//
		int	pos = 0;
		for(int k=size; k > 0; k=k-cols ){
			//
			rp.put("from",String.valueOf( size - k ));	// スタート番号（ゼロオリジン）
			out.append( rp.subst() );
		}
	
	}
	/*
		　ext に受け取るhtml
				
			  <td  class="k12" style="padding-right: 15px;"><input name="rate_%_num%" type="text" class="num10" style="width: 22px;">
			　　<span style="border-bottom: 1px dotted blue;">
			　　　<input type="checkbox" name="ckb_%_num%" value="1" class="k12" style="valign: middle;" >%_word%
			　　</span>
			　</td>
	*/
	void incorrectColumn(String key,StringBuffer out,Hashtable htb,StringBuffer ext){
		if(DBG.fa) DBG.println("class Corrector #incorrectColumn() : 不正解語リストのカラム処理 の先頭です");
		//
		Replace		rp		= new Replace(ext.toString(),htb, this);
		//
		int			from	= Integer.parseInt( (String)rp.get("from") );		// 出力開始番号（ゼロオリジン）
		Vector		words	= (Vector)htb.get("words");							// ■不正解語
		int			max		= words.size();										// 総表示項目数
		int			cols	= Integer.parseInt( (String)rp.get("column") );		// １行の項目数
		//
		int			remain 	= max - from;	// 総残り個数
		//
		//if(DBG._epml) {
		//	DBG.println("■ calss Corrector #incorrectColumn() での入力パラメータです ");
		//	DBG.println("from/"+from+"　max/"+max+"　cols/"+ cols + "　remain/"+remain);
		//}
		if(cols < remain){
			for(int	k=from; k<(from+cols); k++){
				//
				String	dispStr	= (String)words.get(k);	// 不正解語
				rp.put("_word",dispStr);				// htbに入れる
				rp.put("_num",String.valueOf(k));
				out.append( rp.subst() );
			}
		}else{
			for(int	k=from; k<(from+remain); k++){
				String	dispStr	= (String)words.get(k);	// 不正解語
				rp.put("_word",dispStr);				// htbに入れる
				rp.put("_num",String.valueOf(k));
				out.append( rp.subst() );
			}
			for(int	k=(from+remain); k<(from+cols); k++){
				String	dispStr	= "<td>　</td>";	// 空白
				out.append( dispStr );				// 変換しない
			}
		}
		
	}
	String strHash(Hashtable htb,String key){
		//
		String	str	= (String) htb.get(key);
		if(str==null){
			DBG.println("★ key =  " + key );
			DBG.println("★★★★★★★★★★★★★★★★★★★★★★★★★★");
			DBG.println("★★   　　　　　　　　　　　　　　　　　　　　   ★★");
			DBG.println("★★   ハッシュから取り出したデータは NULL です   ★★");
			DBG.println("★★   　　　　　　　　　　　　　　　　　　　　   ★★");
			DBG.println("★★★★★★★★★★★★★★★★★★★★★★★★★★★");
		}
		return str;
	}
}