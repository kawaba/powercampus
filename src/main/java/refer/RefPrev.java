
package refer;

import java.io.*;
import java.util.*;

import jbbs.BbsInfoDB;
import database.*;
import framework.*;
import	epml.*;
import	tktools.*;
/**
 * 
 * 
 	#
	# ##################
	#   RefPrev
	# ##################
	#
	<program $refer.RefPrev>
		<dispatch  html=refPrev.html  number=420  class=refer.RefPrev/>
		<variable>
		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA lec_key  />
		  <accept    CMD    UPLODE />
		  <keep      pmlFilePath />
		  
		  <work      />
		  <form      />
		</variable>
	</program> 
 *
 */
public class RefPrev extends SuperPlayer{

	/* ************ 
	 *  作業用変数
	 **************/
	/**
	 *  プログラムリターンコード 
	 */
	String		ret;
	/**
	 * 次の処理での表示モード 
	 */
	String		disp_mode;
	/**
	 *  処理分岐コード
	 */
	String 		cmd;
	/**
	 * データベースプール
	 */
	DbConnectionBroker	broker;
	/**
	 * ユーザー検索のためのクラス
	 */
	Database			db;

	String			teUid;
	String			lec_key;
	String			pml;
	String			pmlPath;
	
	public	RefPrev(){
		super();
		if(LOG.fa) LOG.println("■ Sample #コンストラクタ");
	}	
	/**
	 * コンストラクタが引数をもてないので、コンストラクタで行う内容をこの initialize() に書く．
	 * out, htb, para は SuperPlayer クラスのインスタンス変数なので、
	 * 引数にとらずとも利用できるが、変数を明示する意味で列挙してある
	 * 
	 * @param out			出力ポインタ
	 * @param htb			システムハッシュ
	 * @param para			パラメータ
	 */
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		broker	=	getDbConnection();
		db		=	new Database(broker);			

		teUid	= getParameter(TUID);
		lec_key	= getParameter("lec_key");
		/*
		 * プレビューするpmlファイルへの絶対パスを受け取り
		 * ファイルを読み込んでpmlに文字データとしてセットする
		 */
		pmlPath	= 	getParameter("pmlFilePath");
		pml		=	FileGear.getFileData(pmlPath);
		
	}
	public	String	dispatch(){
		if(LOG.fa) LOG.outHash(htb,"■Sample #dispatch()");
		if(LOG.fa) LOG.println("■Sample #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		if(cmd.equals("VIEW")){
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示

		}else if(cmd.equals("RETURN")){
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;	// 復帰			

		}else{
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;	// 再表示
	
		}
		/*
		 * 次に表示するときあるいはリターンしてきた時の表示モードをセットしてからリターンする
		 */
		putParameter(DISP_KEY,disp_mode);
		return	ret;
	}
	/**
	 * 個人情報ファイルからフォーマットを読む
	 * 
	 * 個人情報ファイルがnullまたは""なら規定値を読む
	 * 
	 * @param uid
	 * @return
	 */
	public Hashtable getFormat(){
	    
		/*
		 * pmlテキストに段落エイリアスを付加する
		 * テンプレートがあるのでそれを読み込んでセットする
		 */
		String		format	=	para.formatTemplate();

		Hashtable	rec		=	getInfoRecord(teUid);
		if(rec!=null){
			String	tempFormat	=	Gear.strHash(rec,BbsInfoDB.FORMAT);
			if(!isEmpty(tempFormat.trim())){
			    format	=	tempFormat;
			}
		}
		Property	prop	=	null;
		try {
            prop	=	new	Property(new StringReader(format), ":");
        } catch (IOException e) {
            
            e.printStackTrace();
        }
		return	prop.getHash();
	}
	/**
	 * ユーザーIDからユーザー情報をハッシュテーブルで得る
	 * 存在しない時は null を返す
	 * 
	 * @return
	 */
	Hashtable	getInfoRecord(String uid){
		if(LOG.fa) LOG.println("■ BbsPost #getInfoRecord()");

		BbsInfoDB	infodb	=	new BbsInfoDB(uid, getParameter(GROUP), broker);
		Hashtable	dt		=	new Hashtable();
		int		count	=	infodb.readBbsInfo(uid, dt);
		if(count>0)	return	dt;
		return			null;
	}
	/*////////////////////////////////////////////////////////////////////////////
	 * 
	 *    表 示 処 理
	 * 
	 * 　　    以下はコントローラーが呼び出す表示メソッドである．
	 * 　　    一般には、このクラス内のメソッドから直接呼び出さない．
	 * 
	 *////////////////////////////////////////////////////////////////////////////
	
	/**
	 * 出力処理
	 *  
	 * 相対パスが標準なのは、htmlファイルをエクスポートする時のためである
	 * プレビューでは相対パスでは表示されないので、ここでの表示にだけ完全パスをセットする
	 * よう EXAM の設定パラメータを変える．
	 * 
	 */
	public  void	display(boolean editmode){
		if(LOG.fa)	LOG.println("■ Sample #display(boolean editmode)");
		
		if(!editmode){
			putParameter(MESSAGE,"");
		}
		/*
		 * グラフィックスデータへの絶対URL
		 */
		String	imgPath	=	para.getAliasUrl(teUid,lec_key);
		/*
		 * グラフィックスデータへの絶対URLをHTMLに埋め込むよう指定する
		 */
		Exam	exam	= 	new Exam(pml ,para.getEmlConfPath());
		exam.setImgPath(imgPath);
		/* 
		 * システムグラフィックスをユーザーのHTMLディレクトリへコピーするため imgPath の実際のパスを
		 * コピー先の絶対パスをexam に知らせておく．プレビューすることで必要なグラフィックスがシステ
		 * ムディレクトリからユーザーディレクトリへコピーされる
		 */
		exam.setImgDestinationPath(para.getUploadPath(teUid,lec_key));
		
		/*
		 * 生成した html を返す（またはエラーメッセージ）epml でなく pml のパースのみ
		 * \ " ' はもとの文字列に戻す
		 */
		String	htmldata =	Gear.toNormalString(exam.createHtml());
		putParameter("_html",convert(htmldata) );
		
		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		
	}
	/** 本家システムであればURLをIPアドレス表記に変える */
	String	convert(String html){
		String temp	=	"";
		String product	=	para.getViewSwitch();
		if( (product.equals("SYSTEM")) || (product.equals("SYSTEM_VIEW")) ){
			String		server_ip	=	getParameter("_server_ip");
			temp	=	replace(html, "http://mail-and-work.net", "http://" + server_ip);
			return	temp;
		}else{
			return html;
		}
		
	}
}
