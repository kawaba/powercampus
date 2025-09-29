
package framework;
import	tktools.Csv;
import	tktools.Gear;
import	tktools.tkException;
import java.util.*;
/**
 * プログラムキー（ex.  $BbsInfo）のスタック
 * スタック状態がプログラム遷移を表す
 * 
 * getHidden() ではプログラムキーを使ってWEBに埋め込むための
 * hiddenタグを作り出す．
 * 
 */

public class DispatchStack extends Object{

	tktools.Stack		sequence;	// 
	Dispatch			dp;			// 
	Csv					retSeq;		// シーケンス文字列のCsv
	
	final String SEQUENCE_TAG		=	"<input type=\"hidden\" name=\"_sys.SEQUENCE\" value=\"%_sys.SEQUENCE%\">" 	+ Gear.CR ;
	final String HASH_template		=	"<input type=\"hidden\" name=\"_sys.&&&\" value=\"%_sys.&&&%\">"  			+ Gear.CR;
	final String VAR_template		=	"<input type=\"hidden\" name=\"_&&&\" value=\"%_&&&%\">"  					+ Gear.CR;		
	final String NUMBER_template	=	"<input type=\"hidden\" name=\"_sheet\" value=\"$$$\">"  					+ Gear.CR;		

	/**
	 * 
	 * @param seq		プログラムシーケンスを表すCSV形式文字列 ex.  $prog1:$prog2:$prog3
	 *                  新しく起動されたクラスキーが右端に追加されていく
	 * @param dp		dispatch.xml の解析結果を保持して、クラス情報を取得できる
	 */
	public	DispatchStack(String seq, Dispatch dp){
		
		sequence	=	new tktools.Stack();
		retSeq		=	new Csv(seq,":");
		
		for(int i=0; i<retSeq.size(); i++){
			sequence.push(retSeq.get(i));
		}
		this.dp		=	dp;
		
	}
	/**
	 * プログラムシーケンスを考慮した HIDDEN タグを作成する
	 * 
	 * 例示
	 * 　classKey 	= $login.ReLogin
	 *   seq		= "$login.Tlogin:$teacher.Timetable:$kamoku.KamokuList"
	 *   で実際のdispatch.xmlを使って実行した．
	 * 
	 * 　上段は$login.ReLoginの変数タグ
	 * 　下段はプログラムシーケンスに応じて作成されたタグ
	 * 
	 *	<input type="hidden" name="_stay" value="%_stay%">
	 *	<input type="hidden" name="_szDB" value="%_szDB%">
	 *	<input type="hidden" name="_teUid" value="%_teUid%">
	 *	<input type="hidden" name="_teMail" value="%_teMail%">
	 *	<input type="hidden" name="_userName" value="%_userName%">
	 *	<input type="hidden" name="_user_hurigana" value="%_user_hurigana%">
	 *	<input type="hidden" name="_menu" value="%_menu%">
	 *	<input type="hidden" name="_opFile" value="%_opFile%">
	 *	<input type="hidden" name="_userMail" value="%_userMail%">
	 *	<input type="hidden" name="_passwd" value="%_passwd%">
	 *	
	 *	<input type="hidden" name="_sys.SEQUENCE" value="%_sys.SEQUENCE%">
	 *	<input type="hidden" name="_sys.$login.Tlogin" value="%_sys.$login.Tlogin%">
	 *	<input type="hidden" name="_sys.$teacher.Timetable" value="%_sys.$teacher.Timetable%">
	 *	<input type="hidden" name="_sys.$kamoku.KamokuList" value="%_sys.$kamoku.KamokuList%">
	 * 
	 * @param classKey		実行しようとしている論理プログラム名
	 * @return				WEBに埋め込むタグ
	 */
	public	String	getHiddenTags(String classKey){
		
		StringBuffer	buf	=	new StringBuffer(5000);
		/*
		 * 変数タグを生成し、bufにセットして戻る
		 * このタグの %_<変数名>% の部分は、出力時に htb 中の値で置き換えられる
		 */
		getVarTags(VAR_template, NUMBER_template, classKey, buf);
		/*
		 * シーケンスのhiddenタグを埋め込む．
		 * このタグの %_sys.SEQUENCE% の部分は、出力時に htb 中の値で置き換えられる
		 */
		buf.append(SEQUENCE_TAG);
		/*
		 * プログラムシーケンス（_sys.SEQUENCE）にある全てのプログラムについて
		 * base64エンコードしたハッシュデータを埋め込むためのhiddenタグを埋め込む
		 * このタグの %_sys.<classname>% の部分は、出力時に htb 中の値で置き換えられる
		 */
		getHashTags(HASH_template, buf);
		return	buf.toString();
	}
	/**
	 * 全てのHIDDEN タグを作成する
	 * @param classKey		実行しようとしている論理プログラム名
	 * @return				WEBに埋め込む全ての変数タグ文字列
	 */
	public	String	getVariableTags(String classKey){
		/*
		 * 変数タグを生成し、bufにセットして戻る
		 * このタグの %_<変数名>% の部分は、出力時に htb 中の値で置き換えられる
		 */
		StringBuffer	buf	=	new StringBuffer(5000);
		getVarTags(VAR_template, NUMBER_template, classKey, buf);
		return	buf.toString();
	}	
	/**
	 * 変数名のHIDDENタグを作成する 
	 * @param temp			タグテンプレート
	 * @param classKey		論理プログラム名
	 * @param buf			タグバッファ
	 */
	public	void	getVarTags(String vartemp, String numtemp, String classKey, StringBuffer buf){

		/* 実変数名のリストをCSV形式で得る */
		String	varCsv = null;
		varCsv	=	dp.getVariableList(classKey);
		//System.out.println("■varlist=" + varCsv);
		
		if(Gear.isEmpty(varCsv))	return;
		Csv	cs	=	new Csv(varCsv,",");
		/* 論理変数名のリストをCSV形式で得てCsvを作成 */
		Csv	names	=	new Csv(dp.getAliasList(classKey));

		/* タグに実変数名を埋め込んでバッファに入れる */
		for(int i=0; i<cs.size(); i++){
			String	rep	=	cs.getElement(i);
			String	tag	=	"";
			if((names.get(i)).equals("NUMBER")){
				tag	=	getNumberTag(numtemp, classKey);
			}else{
				tag	=	Gear.replace(vartemp, "&&&", rep);
			}
			buf.append( tag);
		}
		return;
	}
	/**
	 * シート番号を dispatch.xml から読んで NUMBER タグを作る
	 * @param numtemp		ナンバータグテンプレート
	 * @param classKey		論理プログラム名
	 * @return
	 */
	String	getNumberTag(String numtemp, String classKey){
		/* WEBシート番号を得る */
		String	sheet	=	dp.getNumber(classKey);
		if(Gear.isEmpty(sheet)){
			System.out.println("■ 論理プログラム名に対応するシート番号がdispatch.xmlに定義されていない");
			return	"";
		}
		//String	tmp	=	Gear.replace(numtemp,"&&&",classKey);
		//String	tag	=	Gear.replace(tmp, "$$$", sheet);
		String	tag	=	Gear.replace(numtemp, "$$$", sheet);
		return	tag;		
	}
	/**
	 * プログラムシーケンスタグを作成する
	 * @param temp			タグテンプレート
	 * @param buf			タグバッファ
	 */
	public	void	getHashTags(String temp, StringBuffer buf){
		Enumeration		en	=	sequence.elements();
		while(en.hasMoreElements()){
			String	str	=	(String)en.nextElement();
			String	tag	=	Gear.replace(temp,"&&&",str);
			buf.append(tag);
		}
		return;		
	}
	/**
	 * 次にリターンするプログラム名を返す
	 * @return
	 */
	public	String	getReturnProgramName(){
		if(retSeq.size()==0)	return	"";
		return		retSeq.get(0);
	}
	/**
	 * システムハッシュの内容から、特定の論理クラス名のシステムハッシュを現在のシステムハッシュに
	 * 展開する．現在のシステムハッシュの内容を全てクリアーしてから展開するのでそれまでのデータは残らない．
	 * 
	 * @param htb				システムハッシュ
	 * @param classKey			論理クラス名
	 * @return					新しいシステムハッシュ
	 * @throws tkException		システムハッシュに該当するクラス名がないときは例外を発生する
	 */
	public	Hashtable	extractHashByName(Hashtable htb, String classKey) throws tkException{
		/*
		 * htbの中の展開するハッシュアーカイブ
		 */
		String	hashKey			=	"_sys." + classKey;
		String	hashArchive		=	Gear.strHashIncludeNull(htb,hashKey);
		/*
		 * hashteble にデコードする
		 */
		if(Gear.isEmpty(hashArchive)) throw (new tkException("★ システムハッシュにハッシュアーカイブがない : key=" + hashKey));
		Hashtable	work	=	(Hashtable)Base64.decodeToObject(hashArchive);
		/*
		 * htb から他の _sys.**** というキーの要素を全て取り出してworkに追加する．
		 */
		addSysArchaive(work, htb, classKey);
		
		return		work;		
		
	}
	/** 
	 * プログラムシーケンスをPOPし、得られた論理クラス名に該当するシステムハッシュを現在の
	 * システムハッシュに展開する．現在のシステムハッシュの内容を全てクリアーしてから展開する
	 * のでそれまでのデータは残らない．
	 * 
	 * ただし、ここで展開しない他の _sys.**** というキーの要素は消さないでそのまま引き継ぐ．
	 * 
	 * @param 		htb		システムハッシュ
	 * @return		POPしたクラスキー（論理クラス名）		
	 * @throws tkException　システムハッシュの中に指定されたキーのハッシュアーカイブがないと例外 tkException を発生する
	 */
	public	Hashtable	extractLastHash(Hashtable htb) throws tkException{
		/*
		 * 
		 */
		String	classKey		=	(String)sequence.lastElement();
		String	hashKey			=	"_sys." + classKey;
		
		String	hashArchive		=	Gear.strHashIncludeNull(htb,hashKey);
		if(Gear.isEmpty(hashArchive)) throw (new tkException("★ システムハッシュにハッシュアーカイブがない : key=" + hashKey));
		Hashtable	work	=	(Hashtable)Base64.decodeToObject(hashArchive);

		/*
		 * htb から他の _sys.**** というキーの要素を全て取り出してworkに追加する．
		 */
		addSysArchaive(work, htb, classKey);
		
		return	work;
	}
	/**
	 * htb からclassKey以外の _sys.**** というキーの要素を全て取り出してworkに追加する
	 * 
	 * @param work
	 * @param htb
	 */
	void	addSysArchaive(Hashtable work, Hashtable htb, String classKey){
		
		for(int i=0; i<retSeq.size(); i++){
			String	key	=	"_sys."	+	retSeq.get(i);
			/*
			 * Csvはシーケンスの古い順にならんでいるので、今回復元するシーケンスである
			 * classKey (=最後の要素のキー)が出現するまでサーチして全て work に追加する．
			 */
			if(key.equals(classKey)){
				break;
			}else{
				work.put( key, htb.get(key) );
				if(LOG.fa) LOG.println("☆ DispatchStack #addSysArchaive(): added key = " + key);
			}
		}
	}	
	/**
	 * プログラムの遷移を記録するための値を作成してシステムハッシュにセットする
	 * "_sys.<classKey>"はプログラムのシステムハッシュ全体をbase64アーカイブしたもの
	 * "_sys.SEQUENCE"  は遷移してきたプログラム名のCSVによるリスト
	 * さらに、与えられたプログラムのキーをシーケンスに追加(push)する
	 * 
	 * (1)現在のシステムハッシュ全体をbase64エンコードしてハッシュに"_sys.<classKey>"をキーとして格納する
	 *    ただし、serializeble でないオブジェクトは含まれない．また、
	 * 
	 * (2)プログラムシーケンスにfromKeyを追加してシステムハッシュに "_sys.SEQUENCE"をキーとして登録する 
	 */
	public	void	updateHashWithPush(String classKey, Hashtable htb) {
		/*
		 * htbのうち serializable な要素のみ新しいハッシュに格納して返す
		 */
		Hashtable	archive		=	getArchivableHash(htb);
		/*
		 * archive から_sys.****というキーの要素を全て取り除く．
		 * それらはこれまで保存されたスタックであるから、現在のシステムハッシュに含めない
		 */
		delSysArchaive(archive, htb);
		/* 
		 * archive（のこった現在のシステムハッシュ全体）をbase64エンコーディングして
		 * 現在のシステムハッシュhtbに追加しておく．現在の状態なのでシーケンス更新前に行う
		 */
		htb.put("_sys."+classKey, Base64.encodeObject( archive ));
		/* 
		 * 現在のクラスキー（論理プログラム名）を追加した上でプログラムシーケンスを求め
		 * システムハッシュの内容を更新しておく．
		 */
		push(classKey);
		htb.put("_sys.SEQUENCE", getSequence());
		return	;	
	}
	/**
	 * htb から _sys.**** というキーの要素を全て削除する
	 * 
	 * @param work
	 * @param htb
	 */
	void	delSysArchaive(Hashtable work, Hashtable htb){
		
		for(int i=0; i<retSeq.size(); i++){
			String	key	=	"_sys."	+	retSeq.get(i);
			work.remove( key );
			if(LOG.fa) LOG.println("☆ DispatchStack #delSysArchaive(): deleted key = " + key);
		}
	}		
	/**
	 * serializable な要素のみ新しいハッシュに格納して返す
	 * @param 		ht	元のハッシュ
	 * @return		serializable な要素のみ含む新しいハッシュ
	 */
	Hashtable	getArchivableHash(Hashtable ht){
		
		Hashtable	tb	=	new Hashtable(100);
		Class	[] intf	=	null;
		Enumeration	en	=	ht.keys();
		while(en.hasMoreElements()){
			String	key		=	(String)en.nextElement();
			String	type	=	(ht.get(key)).getClass().getName();
			intf			=	(ht.get(key)).getClass().getInterfaces();
			boolean flag	=	false;
			StringBuffer bf =	new StringBuffer();
			for(int i=0; i<intf.length; i++){
				bf.append(intf[i].getName());
				bf.append("  ");
				if((intf[i].getName()).equals("java.io.Serializable")){
					flag	=	true;
					break;
				}
			}
			if(flag){
				tb.put(key,ht.get(key));
				if(LOG.fa){
					LOG.println( "○" + key + ": " + type + " // list=" + bf.toString());
				}				
			}else{
				if(LOG.fa){
					LOG.println( "×" + key + ": " + type + " // list=" + bf.toString());
				}
			}
		
		}
		return	tb;
	}
	/**
	 * プログラムシーケンス文字列を作成して返す
	 * 各プログラムエイリアスの間のデリミッタは ":" 
	 * 
	 * 実行してもスタックは空にならない
	 * 
	 * @return		プログラムシーケンス文字列
	 */
	public	String	getSequence(){
		
		StringBuffer	bf	=	new StringBuffer();
		boolean		cma	=	false;
		
		Enumeration	en	=	sequence.elements();
		while(en.hasMoreElements()){
			if(cma)	bf.append(":");
			bf.append(en.nextElement());
			cma	=	true;
		}
		if(bf.length()>0)	return	bf.toString();
		return	"";
	}
	
	public	void	push(String	key){
		sequence.push(key);
	}

	/**
	 * シーケンススタックからひとつポップする
	 * なにもなければ null を返す
	 * 
	 * @return	popした論理クラス名
	 */
	public	String	pop(){
		return	(String)(sequence.pop());
	}
	/**
	 * シーケンススタックから、指定されたクラス名が出現するまでポップする
	 * なければ null を返す
	 * 
	 * @param progKey 最後にpopした論理クラス名
	 * @return
	 */
	public	String	popTo(String progKey){
		
		String	prog	;
		while( ((prog=pop())!=null)&&(!prog.equals(progKey)) ){
		}
		return	prog;
	}


	public	String	getReturnClassKey(){
		return	pop();
	}

	/* テスト用 */
	
	
	public	static	void	main(String []arg){

		String	path	=	"D:\\PowerCampusBasic\\conf\\dispatch.xml";
		String	xml		=	Gear.getFileData(path);
		if(Gear.isEmpty(xml)){
			System.out.println("data is empty!");
		}
		Dispatch	dl		=	new Dispatch(xml);		
		String		seqstr	=	"$login.Tlogin:$teacher.Timetable:$kamoku.KamokuList";
		DispatchStack	ds	=	new DispatchStack(seqstr,dl);


		
		// popTo() のテスト
		String	gotoKey	=	ds.popTo("$teacher.Timetable");
		System.out.println("■gotoKey = " + gotoKey);
		System.out.println("■getSequence() = " + ds.getSequence());
		
		// hidden タグの生成テスト
		String	tags		=	ds.getHiddenTags(gotoKey);
		System.out.println("■hidden tags");
		System.out.println(tags);		
	}	
	
}
