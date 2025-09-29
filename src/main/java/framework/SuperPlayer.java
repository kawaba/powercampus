/*
 * 全てのプラグインの画面を持つクラスの親クラス
 * 
 * (c) T.Kawaba 2004.8 -
 * 
 */
package framework;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Vector;
import jakarta.servlet.http.HttpSession;
import database.DbConnectionBroker;
import tktools.Csv;
import tktools.Gear;
import tktools.StringGear;
import tktools.tkException;
/**
 * 全ての画面を持つクラスの親クラス
 * 
 * 動的な起動を行うためにディスパッチテーブル(dispatch.xml)を作成する必要がある
 * 
 * ＜例示＞
 * <xml>
 *  <programSection>
 * 　　　<program	$bbsforum>
 * 　　　	<dispatch  number=3100	html=forumList.html	class=jbbs.BbsForum />
 *      </program>
 * 　　　<program	$bbsInfo>
 * 　　　	<dispatch  number=3000	html=bbsInfo.html	class=jbbs.BbsInfo />
 *      </program>
 *  </programSection>
 * </xml>
 * 
 *
 *  
 * プラグインはひとつのパッケージとして作成する．
 * 既存プログラムからプラグインを起動するには画面に新たなボタンを加えて
 * ボタンのリターンコードとしてプログラム名（論理プログラム名）を指定しておく．
 * 
 * 逆にプラグインから既存プログラムを呼び出したい場合は、ディスパッチテーブルを
 * 参照して起動したいプログラム名（論理プログラム名）を dispatch メソッドの戻り値
 * とすればよい．
 * 
 */
public abstract class SuperPlayer extends SuperPrint implements PCvar{
	
	//	
	protected Hashtable		htb;
	protected Param			para;
	protected Param			para2;

	public	SuperPlayer(){
		super();
		if(LOG.fa)	LOG.println("■ SuperPlayer #コンストラクタ");
	}
	public	void	setInit(PrintWriter out, Hashtable htb, Param para){
		super.setInit(out);
		this.para	=	para;
		this.htb	=	htb;
	}
	/**
	 * コンストラクタが引数をもてないので、コンストラクタで行う内容をこの initialize() に書く．
	 * 
	 * @param out			出力ポインタ
	 * @param htb			システムハッシュ
	 * @param parameter	構成情報
	 */
	public	abstract void		initialize(PrintWriter out,Hashtable htb, Param parameter);
	
	/**
	 * 受け入れコードにより処理を分岐させるメソッド．
	 * 
	 * 受け入れコードはシステムハッシュ htb から strHash(htb, Param.DISPATCH_KEY); で受け取る
	 * 
	 * 終了コードとして次に起動したいプログラムエイリアス（論理プログラム名）を指定する
	 * 論理プログラム名は dispatch.xml に指定したものを使う
	 * 
	 * 自分自身の画面を再表示したい場合は終了コードに "SELF" を指定する
	 * 呼び出しプログラムへ復帰するには終了コードに "RETURN" を指定する
	 * 
	 * @return
	 */
	public abstract String	dispatch();

	/**
	 * 自分自身の画面の表示処理を行う.
	 * 
	 * 画面の新規表示とデータを保持したままの再表示のどちらを行うのかを判断するのに Param.DISP_KEY
	 * を使う．
	 * 
	 * dispatch() の中などで Param.DISP_KEYをキーとして 新規モードの場合は Param.DISP_NEW
	 * を、再表示モードの場合は Param.DISP_EDIT をシステムハッシュに設定しておく．
	 * 
	 * display() の引数 editmode としてboolean 値で受け取ることができる．
	 * 
	 * パラメータの埋め込みや複雑な表示処理のために、親クラスである SuperPrint のメソッドを
	 * オーバーライドできる．詳細は SuperPrint クラスのチュートリアルを参照すること．
	 * 
	 * @param editmode		新規モードのときtrue、それ以外は false
	 */
	public abstract void		display(boolean editmode);



	///////////////////////////////////////////////////////////////////////////
	//
	//  出力メソッド
	//
	///////////////////////////////////////////////////////////////////////////
	/**
	 * 出力メソッド
	 */
	public void printVector(Vector html){
		if(LOG.fa) LOG.println("□SuperPlayer#printVector(Vector html)");
		printVector( html, true);
	}
	public void printVector(Vector html,boolean notmail){
		//if(DBG.fa) DBG.println("□SuperPlayer#printVector(Vector html,boolean notmail)");
		if(LOG.fa) LOG.outHash(htb,"★★SuperPlayer#printVector(Vector html,boolean notmail)");
		printVector(html,htb,notmail);
	}	
	
	/**
	 * /ハッシュテープル(htb)を使って key で特定される出力処理を行う
	 */
	@Override
	public void	write(String key, Hashtable htb){
		if(LOG.fa) LOG.println("□SuperPlayer#write(String key, Hashtable htb)");
		write(key);
	}
	public	void	write(String key){
		if(LOG.fa) LOG.println("□SuperPlayer#write(String key)");
		System.out.println("★ SuperPlayer #write()-1 : このメソッドはオーバーライドしてください．");
		System.out.println("   key="+key);

	}
	/**
	 * ハッシュテーブル(htb)を使って部分的に切り取ったソースデータ（exHtml）
	 * の内容を置き換えて出力処理する．表などの反復出力に利用するが個々の処理内容は、key で特定される．
	 */
	@Override
	public void	write(String key,Vector exHtml,Hashtable htb){
		if(LOG.fa) LOG.println("□SuperPlayer#write(String key,Vector exHtml,Hashtable htb)");
		write(key,exHtml);
	}
	public void	write(String key,Vector exHtml){
		if(LOG.fa) LOG.println("□SuperPlayer#write(String key,Vector exHtml)");
		System.out.println("★ SuperPlayer #write()-2 : このメソッドはオーバーライドしてください．");
	}
	/**
	 * 部分的に切り取ったソースデータ（exHtml）を出力するか否か、ハッシュテーブル
	 * (htb)を使って判断し、出力する場合には、htb を使ってexHtml の内容を書き換えて
	 * 出力する．個々の処理内容は、key で特定される．
	 */

	@Override
	public void	blockWrite(String key,Vector exHtml,Hashtable htb){
		if(LOG.fa) LOG.println("□SuperPlayer#blockWrite(String key,Vector exHtml,Hashtable htb)");
		blockWrite(key,exHtml);
	}
	public void	blockWrite(String key,Vector exHtml){
		if(LOG.fa) LOG.println("□SuperPlayer#blockWrite(String key,Vector exHtml)");
		System.out.println("★ SuperPlayer #blockWrite() : このメソッドはオーバーライドしてください．");
	}

	/**
	 *  フレームワークのディスパッチ処理で利用されるメソッド
	 * 
	 *  親クラスを初期化し、
	 *  全ての受け取りパラメータを受け取り
	 *  その他の初期化処理を行なってから
	 *  dispatch()を実行する 
	 */
	public	String	dispatch(PrintWriter out,Hashtable htb, Param parameter){
		setInit(out,htb,parameter);
		getParameters();
		initialize(out,htb,parameter);
		return	dispatch();
	}
	/**
	 * 親プログラムを知らせる
	 * @return
	 */
	public	String	parent(){
	    return	getParameter(PARENT);
	}
	/**
	 * 親プログラムの有無を知らせる
	 * @return
	 */
	public	boolean	existParent(){
	    return	!isEmpty( getParameter(PARENT) );
	}
	
	/**
	 *  フレームワークの表示処理で利用されるメソッド
	 * 
	 *  親クラスを初期化し、
	 *  全ての受け取りパラメータを受け取り
	 *  その他の初期化処理を行なってから
	 *  display()を実行する 
	 */	
	public	void	display(PrintWriter out,Hashtable htb, Param parameter){
		setInit(out,htb,parameter);
		getParameters();
		initialize(out,htb,parameter);
		display();
	}
	public	void	display(){
		String	mode	=	getParameter(DISP_KEY);
		//if(DBG.fa) DBG.println("■SuperPlayer #display(): getParameter(DISP_KEY)=" + getParameter(DISP_KEY));
		
		if(isEmpty(mode) || mode.equals(DISP_NEW)){
			display(false);
		}else{
			display(true);
		}
	}

	/**
	 * ファイルをベクターにロードして返す
	 * @param path		ファイルの完全パス名
	 * @return			ベクター
	 */
	public	Vector	loadHtml(String path){
		Vector	v	=	new Vector(500);
		Gear.loadToVector(path, v);
		v.trimToSize();
		return	v;
	}
	/**
	 * Stringをベクターに入れて返す
	 * @param 	str
	 * @return	
	 */
	public Vector	StringToVector(String str){
		
        return	StringGear.StringToVector(str);
		
	}
	/**
	 *  全ての受け取りパラメータを取得する
	 *  (ユーザーでオーバーライド)
	 */
	public	void	getParameters(){
		
	}
	/**
	 * システムハッシュからキーで文字列を取り出す
	 * 
	 * putIParameter, getParameter は web 上の変数に対応するためキー値の先頭に
	 * _(アンダーバー）を自動的に付加する．web 上の変数は %_variable% のように
	 * 先頭に _ を付ける規則になっているからである．
	 * 
	 * これに対して getItem,putItem はキー値の先頭に_(アンダーバー）を自動的に付加しない
	 * 単にフォーム変数を取得・設定するだけの場合に使う．
	 *
	 ** @param key
	 * @return
	 */
	public String getItem(String key){
		if(!isEmpty(key)){
			return	strHash(htb, key);
		}
		return	"";
	}	
	/**
	 * システムハッシュに文字列の値をセットする
	 * 
	 * @param key
	 * @param data
	 * @return
	 */
	public	boolean putItem(String key, String data){
		if(!isEmpty(key)){
			try{
				putToHash(htb, key, data);
			}catch(tkException e){
				errPrint(e.getMessage());	// スタックトレースを含む
			}
			return	true;
		}
		return false;
	}
	/**
	 * システムハッシュにオブジェクトを置く
	 * 
	 * @param key
	 * @param obj
	 * @return
	 */
	public	Object	putObject(String key, Object obj){
		htb.put(key, obj);
		return	obj;
	}
	/**
	 * システムハッシュからオブジェクトを取り出す
	 * @param key
	 * @return
	 */
	public	Object	getObject(String key){
		return	htb.get(key);
	}
	/**
	 * システムハッシュからキーで文字列を取り出す
	 * キーの先頭に _ が付いていない場合は付加してから
	 * 使う
	 *
	 ** @param key
	 * @return
	 */
	public String getParameter(String key){
		String	setkey	=	key;
		if(!isEmpty(key)){
			char	ch	=	key.charAt(0);
			if(ch!='_'){
				setkey	=	"_" + key;
			}
			return	strHash(htb, setkey);
		}
		return	"";
	}	
	/**
	 * システムハッシュに文字列の値をセットする
	 * キーの先頭に _ が付いていない場合は付加してから
	 * 使う
	 * @param key
	 * @param data
	 * @return
	 */
	public	boolean putParameter(String key, String data){
		String	setkey	=	key;
		if(!isEmpty(key)){
			char	ch	=	key.charAt(0);
			if(ch!='_'){
				setkey	=	"_" + key;
			}
			try{
				putToHash(htb, setkey, data);
			}catch(tkException e){
				errPrint(e.getMessage());	// スタックトレースを含む
			}
			return	true;
		}
		return false;
	}
	/**
	 * ハッシュからキーで文字列を取り出す
	 * キーの先頭に _ が付いていない場合は付加してから使う
	 *
	 * @param ht
	 * @param key
	 * @return
	 */
	public  String getParameter(Hashtable ht, String key){
		String	setkey	=	key;
		if(!Gear.isEmpty(key)){
			char	ch	=	key.charAt(0);
			if(ch!='_'){
				setkey	=	"_" + key;
			}
			return	Gear.strHash(ht, setkey);
		}
		return	"";
	}	
	/**
	 * ハッシュに文字列の値をセットする
	 * キーの先頭に _ が付いていない場合は付加してから使う
	 * 
	 * @param ht
	 * @param key
	 * @param data
	 * @return
	 */
	
	public  boolean putParameter(Hashtable ht, String key, String data){
		String	setkey	=	key;
		if(!Gear.isEmpty(key)){
			char	ch	=	key.charAt(0);
			if(ch!='_'){
				setkey	=	"_" + key;
			}
			try{
				putToHash(ht, setkey, data);
			}catch(tkException e){
				errPrint(e.getMessage());	// スタックトレースを含む
			}
			return	true;
		}
		return false;
	}
	/**
	 * ハッシュに文字列の値をセットする
	 * 文字列の値がnullだった場合は例外を発生して実行を停止する
	 * @param h
	 * @param key
	 * @param data
	 * @throws tkException
	 */
	public	void	putToHash(Hashtable h, String key, String data) throws tkException {
		if(data!=null){
			h.put(key, data);
		}else{
			throw (new tkException ("ハッシュにnull値をputした．"));
		}
	}
	/**
	 * １変数のセッション情報
	 * 
	 * ログインプログラムが利用
	 */
	public void	setSession(String id){

		HttpSession session  	= (HttpSession)htb.get("_httpSession");
		
		///////////////////////////////////////////////////////
		session.setAttribute(PC_ID	,id);
		///////////////////////////////////////////////////////
 		/*
 		 * セッション共通変数用のハッシュテーブルを作成して保存する。
 		 * このハッシュは特殊な用途にしか用いられない。現在のところ
 		 * EvalWeb,EvalText でレコード番号の受け渡しに使われるだけ。
 		 * 将来的には削除する方針
 		 */
 		Hashtable	sessionVars	=	new	Hashtable();
 		session.setAttribute(SESSION_VARS, sessionVars);
	
	}
	/**
	 * ２変数のセッション情報
	 * 
	 * ログインプログラムが利用 
	 */
	public void	setSession(String id, String id2){

		HttpSession session  	= (HttpSession)htb.get("_httpSession");

		///////////////////////////////////////////////
		session.setAttribute(PC_ID	, id);
		session.setAttribute(PC_ID2	, id2);
		//////////////////////////////////////////////
	
	}	
	/**
	 * セッション共通変数からオブジェクトを取り出す
	 * @param 	key
	 * @return	object ただしnullの場合がある。
	 */
	public	Object	getSessionVar(String key){
		HttpSession session  	= (HttpSession)htb.get("_httpSession");
		Hashtable	sessionVars	= (Hashtable)session.getAttribute(SESSION_VARS);
		return	sessionVars.get(key);
		
	}
	/**
	 * セッション共通変数にオブジェクトを格納する
	 * null値は格納されない
	 * 
	 * @param key
	 * @param obj
	 * @return		成功すると true
	 */
	
	public	boolean	setSessionVar(String key, Object obj){
		HttpSession session  	= (HttpSession)htb.get("_httpSession");
		Hashtable	sessionVars	= (Hashtable)session.getAttribute(SESSION_VARS);
		
		if(obj!=null){
			sessionVars.put(key,obj);
			session.setAttribute(SESSION_VARS, sessionVars);
			return	true;
		}
		return	false;
	}

	/**
	 * DbConnectionBrokerを返す
	 * フレームワークはデータベースアクセスのためにDbConnectionBrokerをシステムハッシュに
	 * 必ず書き込む．これはそれを受け取る処理．データベースアクセスを行なうモジュールでは
	 * 必す実行する必要がある．
	 * 
	 * @return
	 */
	public	DbConnectionBroker getDbConnection(){
		
		DbConnectionBroker	broker	=	(DbConnectionBroker)(htb.get(BROKER));
		if(LOG.fa) {
			if(broker==null)	LOG.println("■ SuperPlayer #getDbConnection() :broker is null");
		}
		return	broker;

	}
	/**
	 * リターンするプログラム名を得る
	 * @return
	 */
	public	String	getReturnProgName(){
		String	seqstr	=	getProgramSequence(htb);
		if(isEmpty(seqstr)){
			return	"";
		}else{
			Csv	cs	=	new Csv(seqstr,":");
			return	cs.get(0);
		}
	}
	/**
	 * プログラムシーケンス文字列を得る
	 * @param htb
	 * @return
	 */
    public	String	getProgramSequence(Hashtable htb){
		String	seq	=	Gear.strHashIncludeNull(htb, SYS_SEQUENCE);
		if(Gear.isEmpty(seq)){
			htb.put(SYS_SEQUENCE, "");
			seq	=	"";
		}    	
    	return	seq;
    }
}
