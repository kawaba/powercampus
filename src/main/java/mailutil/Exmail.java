/*
	class Exmail     (c) T.Kawaba  2003.8.3 All rights reserved
	
	exMail への送信をカプセル化する
	
	データファイルへのパスを受け取ってチェックし、送信可能な新しいファイルを作成する
	送信メソッドや表示のためのデータ提供を行う

*/
//
package mailutil;

import java.io.*;
//import java.text.*;
import java.util.*;

import meibo.Meibo;

import database.Database;
import framework.LOG;
import framework.Param;
//import java.net.*;
import student.Student;
import tktools.*;
//
public class Exmail extends Object{
	//
	public	final	static	String NAME_OF_I = "temp_i.csv";
	public	final	static	String NAME_OF_K = "temp_k.csv";
	//
	PrintWriter out;				//
	Param 		para;				//
	//
	Database	db;					//
	String		teUid;
	String		aplec_key;
	String		szDB;				//
	Meibo		mb;					// クラス名簿
	String 		Fpath_in;			// 送信されたファイルへのフルパス		例：/home/pc/temp/RCF9837/test.csv
	String		path_i;				// 新しく作成するファイルへのフルパス）	例：/home/pc/kawaba01/mailtemp/temp_i.csv
	String		path_k;				// 新しく作成するファイルへのフルパス）	例：/home/pc/kawaba01/mailtemp/temp_k.csv
	//
	Vector		vFile;				// Fpath_o を読み込んだVector
	int			pos;				// データの何番目が学籍番号欄か
	//
	int			columns;			// 正しいデータの列数（ヘッダーでカウント）
	int			rows;				// 正しいデータの行数（データ部のみ）
	Vector		items;				// ヘッダ（第１列 mailto）
	Vector		dt_i;				// データ（第１列 ケータイアドレス）
	Vector		dt_k;				// データ（第１列 メールアドレス）
	//
	Vector		errData;			// 差込データのうち学籍番号が間違っているもののリスト（データ部含む）
	boolean		empty;				// 空を示すフラグ
	//
	// デバッグ用定数
	String	thisClass	= "■class Exmail #";
	//
	//　コンストラクタ
	//
	//    生成に成功しない場合、empty = true とするので、isEmpty() で調べる
	//    empty の場合そのインスタンスは使用できないので捨てること
	//
	public Exmail(String _Fpath_in,String _teUid,String _aplec_key,Meibo _mb,String _szDB,Database _db,PrintWriter _out,Param _para){
		if(LOG.fa) LOG.println(thisClass + "コンストラクタ #Exmail() の先頭です");
		if(LOG.fa) LOG.println("    " + "_Fpath_in=" + _Fpath_in);
		if(LOG.fa) LOG.println("    " + "   _teUid=" + _teUid);
		if(LOG.fa) LOG.println("    " + "    _szDB=" + _szDB);
		//
		out			= _out;
		para		= _para;
		db			= _db;
		teUid		= _teUid;
		aplec_key	= _aplec_key;
		szDB		= _szDB;
		mb			= _mb;
		Fpath_in	= _Fpath_in;
		//
		path_i		= para.getMailTempDir(teUid,aplec_key) + "temp_i.csv";	//  /home/kawaba01/mailtemp/temp_i.csv
		path_k		= para.getMailTempDir(teUid,aplec_key) + "temp_k.csv";	//  /home/kawaba01/mailtemp/temp_k.csv
		//
		vFile	= new Vector(200,100);
		items	= new Vector(20,10);
		dt_i	= new Vector(200,100);
		dt_k	= new Vector(200,100);
		errData	= new Vector(100,10);
		//
		boolean ret		= loadToVector(Fpath_in,vFile);
		if(!ret){
			empty		= true;
			if(LOG.fa) LOG.println("　　 ★empty(1) = true ");
		}else{
			empty		= false;
			
			if( !createData() )	{
				empty = true;
			}
			if(LOG.fa) LOG.println("　　 ★empty(2) = " + empty);
			
		}
		if(LOG.fa) LOG.println("■■ Exmail#コンストラクタ: " + "isEmptyObject() =" + String.valueOf(empty) );
		if(LOG.fa) LOG.println("　　 fpath = " + Fpath_in );
		//
	}
	//
	//  正しいデータを生成する
	//
	boolean	createData(){
		if(LOG.fa) LOG.println(thisClass + "■■ Exmail#createData(): 入り口です");
		//
		int i;
		int	n	= vFile.size();								// vFile はコンストラクタでサイズは０でないことが分かっている
		Csv cs = null;
		for(i=0; i<n; i++){									// 先頭から１件づつデータを見て、ヘッダー項目を探す。
			cs	= new	Csv( (String)vFile.get(i) );		// 学籍番号とか番号といった文字列の項目が出てくればそれを
			if(isHeadder(cs))	break;						// ヘッダー行と判断する
		}
		if(i==n)	return	false;	// 正しい項目名がなくヘッダーを特定できなかった
		if(i==n-1)	return	false;	// ヘッダーのみでデータ部がない（２以上のサイズが必要）
		//
		int topLineNum	= i;									// ただしいデータの行数と列数を求め、クラス変数に格納しておく
		if(LOG.fa) LOG.println(thisClass + "createData(): ヘッダ行は " + topLineNum + " 行目です");
		pos				= headderPosition(cs);					// さらに、データの何番目が学籍番号欄か調べ、これをもとに
		columns			= setItems(topLineNum,pos);				// 正しい順列でデータを格納した項目とデータ部のベクター
		rows	 		= setData(topLineNum,pos,columns);		// を生成する。これらにはメールアドレス欄とその値も含まれる
		if(LOG.fa) LOG.println(thisClass + "createData(): setItems() と setData() が終了しました");
		if(LOG.fa) LOG.println(thisClass + "createData():  ========>>>>> pos= " + pos +",columns=" + columns + ",rows=" + rows );
		//
		if(rows==0)		return	false;	// データ部生成において正しいデータが一件もない場合はエラーリターンする
		boolean	ret = writeTemp();		// ＯＫならデータをファイルとして書き出す
		return	ret;
	}
	//
	//  正しい順列でデータを格納した項目部のベクター（items）を生成する
	//
	//　　入力
	//     	topLineNum --- オリジナルの vFile の何番目が項目行のデータかを示す値
	//     	pos ---------- 項目行のなかで、何番目の要素が「学籍番号」かを示す値
	//
	//　　戻り値 --- 行項目数
	//
	int	setItems(int topLineNum,int pos){
		if(LOG.fa) LOG.println(thisClass + "setItems(): 入り口です");
		//
		items.add("mailto");
		items.add("番号");
		Csv	cs	= new Csv( (String)vFile.get(topLineNum) );
		int max = cs.size();
		//
		int	fn = 1;
		for(int i=0; i<max; i++){								// vFile から items に要素を転記する
			if(i!=pos){											// ただし、"番号"項目は既に転記済みなので
				String	itemName = cs.get(i);					// バイパスする
				if( isEmpty(itemName) ){						// また、項目名が空白なものは filler* の形
					itemName = "filler" + String.valueOf(fn);	// にして書き込む．* は１オリジンの数値
					++fn;
				}
				items.add( itemName );
			}
		}
		if(LOG.fa) LOG.outVector(items,thisClass + "生成した項目名ベクター(items)です.項目数は[" + (max+1) + "] 件です" );
		return	max + 1;	// "mailto" がひとつ余計に増えるため
	}
	//
	// 正しい順列でデータを格納したデータ部のベクター（dt_i, dt_k）を生成する
	//
	//　　入力
	//     	topLineNum --- オリジナルの vFile の何番目が項目行のデータかを示す値
	//     	pos ---------- 項目行のなかで、何番目の要素が「学籍番号」かを示す値
	//     	columns  ----- メールアドレス欄も含めた総カラム数
	//
	//　　戻り値 --- データ部分の行数
	//
	int	setData(int topLineNum,int pos,int columns){
		if(LOG.fa) LOG.println(thisClass + "setData(): 入り口です");
		if(LOG.fa) LOG.println(thisClass + "setData():   topLineNum ---" + topLineNum);
		if(LOG.fa) LOG.println(thisClass + "setData():   pos ----------" + pos);
		if(LOG.fa) LOG.println(thisClass + "setData():   columns ------" + columns);
		//
		int start	= topLineNum + 1;	// topLine は項目行．
		int	n 		= vFile.size();		// サイズは２以上あることを createData() で確かめ済み
		if(LOG.fa) LOG.println(thisClass + "各データ行を処理します／start [" + start + "] ～ n [" + n + "]" );
		for(int i=start; i<n; i++){
			// 準備
			if(LOG.fa) LOG.println(thisClass + "１．準備" );
			Vector	aLine	= new Vector (20,10);				// vFile から読み込んだ１行をそのままベクター aLine に移す
			aLine.add(" ");										// aLine にはまず mailto の分のダミーを " " で書き込む．また、
			String	line	= (String)vFile.get(i);				// vFileの1行の項目数を column - 1 になるよう調整する.
			if(LOG.fa) LOG.println(thisClass + "　入力=" + line);
			Csv		cs		= new Csv(line,",",columns - 1);	// -1 するのは先頭の mailto カラムが vFile にないからである
			//													// Csv は項目数を指定すると、多いときは捨て、不足すると"-"を補う
			// 転記
			if(LOG.fa) LOG.println(thisClass + "２．転記" );
			if(LOG.fa) LOG.println(thisClass + "    ---> Csv is " + cs.getCsvString() );
			aLine.add( cs.get(pos) );				// オリジナルデータの pos 番目が学籍番号なのでこれを最初に転記する．
			for(int k=0; k<(columns-1); k++){		// メールアドレス欄はすでにダミーを書いているのでこれで２番目となる．
				if(k!=pos){							// 
					aLine.add( cs.get(k) );			// ループの中で学籍番号を除いて順に詰めて aLine に登録していく
				}									// データのエラーチェックはこの後．
			}
			// チェック・構成
			if(LOG.fa) LOG.println(thisClass + "３．構成" );
			//if(DBG.fa) DBG.outVector(aLine,thisClass + "     aLine の内容です" );
			String 	stNumber	= (String)aLine.get(1);		// 名簿を学籍番号引いて、データにかかれている番号が間違いないか
			Csv		meiboData	= mb.get(stNumber);			// 確かめる．正しければ dt_i,di_k  に登録するが、間違っていた場合は 
			if(meiboData != null){							// errData に登録する
				Vector aLine_k	= (Vector)aLine.clone();
				addMailaddress( stNumber,aLine,aLine_k );	// ここまででは、aLine にメールアドレスが入っていないので、データベース
				dt_i.add( aLine );							// を引いて e-mail ケータイのアドレスを求め、各々追加記録する
				dt_k.add( aLine_k );						// 
			}else{ 
				errData.add( aLine );
			}
		}
		return dt_i.size();
	}
	//
	// データにメールアドレス、ケータイアドレスを付加する
	//
	void addMailaddress( String stNumber,Vector aLine_i,Vector aLine_k ){
		if(LOG.fa) LOG.println(thisClass + "addMailaddress(): 入り口です");
		//
		Student st	= new Student(szDB,stNumber,db);
		aLine_i.set(0,st.email());			// データベースを引くが登録がない場合もある
		aLine_k.set(0,st.keitai());			// その場合 "-" が値として返される
		return;
	}
	//
	// データをファイルとして書き出す
	//
	boolean	writeTemp(){
		if(LOG.fa) LOG.println(thisClass + "writeTemp(): 入り口です");
		//
		boolean	ret_i = writeOut(path_i,dt_i);
		boolean	ret_k = writeOut(path_k,dt_k);
		return (ret_i && ret_k);
	}
	boolean writeOut(String path,Vector dt){
		if(LOG.fa) LOG.println(thisClass + "writeOut(): 入り口です");
		//
		File fp = new File(path);
        try {
			//
            PrintWriter out = new PrintWriter(new OutputStreamWriter (new FileOutputStream(fp),"Windows-31J"),true);
            outVectorToCSV(items,out,true);
    		int	n = dt.size();
	        for(int i=0; i<n; i++){
                Vector v = (Vector)dt.get(i);
                outVectorToCSV(v,out,true);
            }
            out.close();
        }catch (Exception exc) {
            System.out.println("差込データ出力エラー");
			return false;
        }
		return true;
	}
    //
	// １次元ベクトルをＣＳＶ形式で出力する
	//
    void outVectorToCSV(Vector v,PrintWriter out,boolean newline){
		if(LOG.fa) LOG.println(thisClass + "outVectorToCSV(): 入り口です");
		//
        boolean flag = false;
        for (Enumeration e = v.elements() ; e.hasMoreElements() ;) {
            if(flag){
                out.print(",");
            }
            String s = (String)(e.nextElement());
            if(s != null){
                out.print(s);
            }else{
                out.print("");
            }
            flag = true;
        }
        if(newline) out.println();//改行する
    }
	//
	// ヘッダー行か調べる
	//
	boolean	isHeadder(Csv cs){
		if(LOG.fa) LOG.println(thisClass + "isHeadder(): 入り口です");
		//
		int	i;
		int	n = cs.size();
		for(i=0; i<n; i++){					// Csv で受け取るので、その各々をヘッダー判定文字列と比較する
			if( isBango(cs.get(i)) ) break;	// 受け取ったCsvの中に番号といった内容のものがあればそれがヘッダーである
		}									// 
		if(i==n)	return	false;
		return		true;
	}
	int	headderPosition(Csv cs){
		if(LOG.fa) LOG.println(thisClass + "headderPosition(): 入り口です");
		//
		int i;
		int	n = cs.size();
		for(i=0; i<n; i++){					// Csv で受け取るので、その各々をヘッダー判定文字列と比較する
			if( isBango(cs.get(i)) ) break;	// 受け取ったCsvの中に番号といった内容のものがあればそれがヘッダーである
		}									// 
		if(i==n){
			
			return	-1;				// isHeadder() のあとで実行するので起こりえないが念のため
		}
		return		i;
	}
	//
	//  学籍番号項目か
	//
	// 定数
	String [] bango	= {"番号","学籍番号","学番"};
	//
	public boolean	isBango (String s){
		if(LOG.fa) LOG.println(thisClass + "isBango(): 入り口です [" + s + "]を受け取りました");
		//
		String ss = ctirm(s);							// 受け取った文字列から漢字と半角のスペースを
		int n = bango.length;							// 完全に取り去ってから、判定文字列と比較する
		int i;
		for(i=0; i<n; i++){							//
			if(LOG.fa) LOG.println(thisClass + "isBango():  [" + ss + "] vs [" + bango[i] + "] i=" + i );
			if( ss.equals( bango[i] ) ){
				if(LOG.fa) LOG.println("   isBango() ------[" + i + "] -----------------------------------> return true");
				return	true;	//
			}												//
		}
		if(LOG.fa) LOG.println("   isBango() ------[" + i + "] -----------------------------------> return false");
		return	false;
	}
	//
    // ファイルデータをVectorに格納する
    //
	public boolean loadToVector(String fpath,Vector Vhtml){
		if(LOG.fa) LOG.println(thisClass + "loadToVector(): 入り口です");
		//
        BufferedReader in = null;
        String         line;
        try{
            in = new BufferedReader(new InputStreamReader(new FileInputStream(fpath),"Windows-31J"));
            while((line=in.readLine())!=null){
            	if(LOG.fa) LOG.println("line: " + line);
                if(line.length() > 0) {
                	if(LOG.fa) LOG.println(" --> added");
                	Vhtml.add(line); 
                }else{
                	if(LOG.fa) LOG.println(" --> droped");
                	
                }
            }
        }catch (IOException e){
            LOG.errStop("class Exmail  コンストラクタ⇒loadToVector() : can't read " + fpath);
			return false;
        }
        if(LOG.fa) LOG.println(" data is " + vFile.size() + "件");
        if(vFile.size() > 0)	return true;
		return false;
    }
	//
	//  オブジェクトは空かどうか
	//
	public boolean	isEmptyObject()	{
		if(LOG.fa) LOG.println(thisClass + "isEmptyObject(): 入り口です");
		//
	 	return empty;
	}
	//
	//
	//  String が空かどうか
	//
	public boolean	isEmpty(String s){
		if(LOG.fa) LOG.println(thisClass + "isEmpty(): 入り口です");
		//
		if(s==null)	return true;			// null チェックをしておかないと trim() できない
		//
		String ss = s.trim();					// 文字列は trim() して両端の
		if(ss.length() == 0)	return true;	// 空白文字を取ってから判断する
		//
		return	false;
	}
	//
	//
	//  文字列から空白を取り除いた文字列を作成する
	//
	String	ctirm(String str){
		if(LOG.fa) LOG.println(thisClass + "ctirm(): 入り口です");
		//
		int	len	= str.length();
		if(len==0)	return	"";
		//
		Vector  temp 	= new Vector(50,10);	// 半角、全角の漢字でない char だけを格納する配列として temp を作る
		for(int i=0; i<len; i++){				// 文字列から１文字づつ取り出して文字種をチェックし
			char a = str.charAt(i);				// パスすれば teip に格納する
			if( (a != ' ')&&(a != '　') ){
				temp.add(new Character(a));
			}
		}
		int sz = temp.size();					// temp のサイズがゼロでないことを確かめてから
		String 	newStr = "";
		if(sz>0){								// サイズ分の char 配列を作成し
			char [] cstr = new char[sz];		// この中に temp から１文字づつ取り出して埋めていく
			for(int j=0; j<sz; j++){
				cstr[j] = ((Character)temp.get(j)).charValue();
			}
			newStr = new String(cstr);			// 最後に char 配列から文字列 targetString を生成する
			if(LOG.fa) LOG.println("class Exmail #ctirm() : ［" + str + "］から 空白を取り除いた文字列 ----->［" + newStr + "］" );
		}
		return	newStr;
	}
    //
    //      ユーティリティ
    //
	//-------------------------- ファイル削除 --------------------------------
	boolean makeDir(String dir){
		return (new File(dir)).mkdirs();
	}
	boolean delDir(String dir){
		File fp = new File(dir);
		return deleteDir(fp);
	}
    // 再帰的にファイルとディレクトリを消す
	boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
	}
    // 再帰的にファイルを消す（親ディレクトリは消さない）
	boolean deleteFiles(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return true;
	}
}