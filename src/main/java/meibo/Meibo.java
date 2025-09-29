/*
★クラス名簿の生成と関連処理をカプセル化する 


*/
package meibo;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
//import java.text.*;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Vector;
import framework.Cp932;
import framework.LOG;
//import java.lang.Exception;
import tktools.Csv;
/*
    meibo.csv

    # 講義名簿＜順序固定＞
    #
    #　｛メールアドレス｝,｛番号｝,｛氏名｝,{携帯}
    #

*/

public class Meibo{
    /*
     * 名簿ファイルへのパス 
     */
    String		path;	// パスから生成した時のみ値がある
    //
    Csv       	headder;
    Vector    	meibo;
	Vector	  	meibo1;	// 名簿
	Vector	  	meibo2;	// 課題提出時間で並べ替えた名簿
	Vector	  	meibo3;	// 学籍番号で並べ替えた名簿
	//
	boolean   	mbFlag;	// 課題提出時間で並べ替えた名簿が作成済みかどうか
	boolean   	mbFlag_3;	// 学籍番号で並べ替えた名簿が作成済みかどうか
	//
    Hashtable 	hsMeibo;
    int       	cnt;
	boolean   	sw;		// meibo を使うかmeibo2 を使うか（sw = true なら meibo）
    //
	boolean	errflag;		// 生成時にヘッダ検査でエラーが見つかったとき、セットされる isErrorHeader() でしらべる
	boolean   	someErrorFlag;	// 生成時に学籍番号未記入などでエラーとなったデータがあったとき、true にセットし、後からは isSomeError() で調べる
	//
	/*
		歴史的な経緯から、データの並び順は
		
		    メールアドレス、学籍番号、氏名、ケータイ　
			
		を想定して処理する
		入力データの実際の並び順は違っているので、ヘッダーの名前から実際の並び順を調べ order[] に値を入れる。

		例）
		　  学籍番号、氏名、メールアドレス、ケータイ
		    
			[] order = {2,0,1,3}
		  
		　  第ｉ番目の列データは 実データでは order[i]番目のデータ
		
	*/
	int		[] order = {0,1,2,3}; // 並び順を決めるための配列初期値
	//
    /*
		   ヘッダー行の約束 -- 	漢字にて項目名をつけ、各々は次のいずれかでなくてはならない
		                       	項目数は４つでなくてはならない
								項目の順序はどうでもいい
	*/
	
	String [] item_0 = {"メール","メールアドレス","電子メール","電子メールアドレス","mailto","mailTo"};
	String [] item_1 = {"学籍番号","番号","学番"};
	String [] item_2 = {"氏名","名前","学生氏名"};
	String [] item_3 = {"ケータイ","ケータイアドレス","携帯","携帯アドレス","携帯メール","携帯メールアドレス"};
	//
	//
	// 空の名簿
    public Meibo(){
        headder = null;
        //meibo   = new Vector(100,50);
        meibo   = new Vector(100,50);
        meibo1  = new Vector(100,50);
        meibo2  = new Vector(100,50);
        meibo3  = new Vector(100,50);
        hsMeibo = new Hashtable(500);		
        mbFlag   = false;	// meibo2 未作成のマーク
        mbFlag_3 = false;	// meibo3 未作成のマーク
		cnt 	= 0;
		sw  	= true;
		hsMeibo	= new Hashtable(500);
		//
		errflag		  = false;
		someErrorFlag = false;
	}
	//
    // 名簿ファイルへのパスを得て生成する
    public Meibo(String _path){
		String  method = "class meibo コンストラクタ #Meibo(String path) :" ;
		if(LOG.tr) LOG.println(method + " の入り口です．path = " + path);
        //
		
		this.path		=	_path;
		errflag		  	= 	false;
		someErrorFlag 	= 	false;
		headder 		= 	null;
		//
		String	[] HEADDER	= {"番号","氏名","メール","携帯"};
		//
		//meibo   = new Vector(100,50);
        meibo1  = new Vector(100,50);
        meibo2  = new Vector(100,50);
        meibo3  = new Vector(100,50);
		
        hsMeibo = new Hashtable(500);
        //
        mbFlag   = false;	// meibo2 未作成のマーク
        mbFlag_3 = false;	// meibo3 未作成のマーク
		//
		BufferedReader in = null;
        try{
            in  = new BufferedReader(new InputStreamReader(new FileInputStream(path),"Windows-31J"));
			String line;
            Csv    tmp;
			Csv    csv;
			// 
			if(((line=in.readLine())!=null)&&((line.trim()).length()>=5)) {	//１行目はヘッダーという約束．"氏名,番号" で 最低５文字以上はいるはず
                // 要素の数をｋ個に指定して生成（多すぎる場合は捨て、少ないと配列 HEADDER の該当位置の文字列で補う）
				headder = new Csv(Cp932.toJIS(line),",",4,HEADDER);
				if(LOG.fa) LOG.println("class Meibo # コンストラクタ : headder = " + headder.getCsvString() );
				//
				// ヘッダーのチェック
				if(!isRegal(headder)){
					errflag	= true;
					headder = new Csv("タイトル行１行目の書き方が不正,-,-,-",",",4); // 要素の数をｋ個に指定して生成（多すぎる場合は捨て、少ないと "-" を補う）
				}
            }else{
                headder = new Csv("名簿ファイルにデータがない,-,-,-",",",4); // 要素の数をｋ個に指定して生成（多すぎる場合は捨て、少ないと "-" を補う）
				errflag	= true;
            }
            headder = arrange(headder); // 並び替えたヘッダー
			if(LOG.fa) LOG.println(method + "並び替えたデータ=" + headder.getCsvString() );
			// ２行目以降はデータ
			// ヘッダーエラーだとここはパイパスする
            if(!errflag){
				while((line=in.readLine())!=null){
    	            if((line.length()>0)&&(line.charAt(0) != '#')){ // # 行は無視
						tmp = new Csv(Cp932.toJIS(line),",",4); // 要素の数をｋ個に指定して生成（多すぎる場合は捨て、少ないと "-" を補う）
						//
						csv = arrange(tmp); // データの並び順をヘッダ情報から調整する
						if(LOG.fa) LOG.println(method + "並び替えたデータ=" + csv.getCsvString() );
						//
						// 空白データの混入を防ぐ
		            	int test = 0;
						for(int i=0; i<4; i++){
							String s = csv.get(i);
							//
							// 空白のデータをカウントする（全て空白かどうか調べるため））
							if( (s==null)||(s.length()==0)||(s.equals(" "))||(s.equals("-")) )  test++;
							//
							// キーである学籍番号（i=1）が空白ならデータに入れない
							if( ( (s==null)||(s.length()==0)||(s.equals(" "))||(s.equals("-")) ) && i==1)  test = 5;
						}
						if(test < 4){
							meibo1.add(csv); // 両端の空白は取る
    	        	        hsMeibo.put(csv.get(1),csv); // 学籍番号をキーとするハッシュテーブルも作る
						}else{
							someErrorFlag = true;
						}
	                }
    	        }
				if(LOG.fa) LOG.println(method + " meibo オブジェクトを生成しました");
				//
			}else{
				if(LOG.fa) LOG.println(method + "タイトル行の書き方が不正です title = " + line);
				if(LOG.fa) LOG.println(method + "　　⇒　空の meibo オブジェクトを生成しました");
			}
            cnt = meibo1.size();
            in.close();
        }catch(IOException e){
            cnt = -1;
			errflag	= true;
			if(LOG.fa) LOG.println(method + "★名簿ファイルを開けません．");
        }
		sw		= true;		// meibo を使う
		meibo 	= meibo1;
    }
	//
	//
    public Meibo(String _headder,Vector v,Hashtable tb){
		errflag		  = false;
		someErrorFlag = false;
		//
		headder = new Csv(_headder);
		hsMeibo = tb;
		meibo1  = v;
		sw	    = true;		// meibo を使う
		meibo   = meibo1;
        cnt     = meibo.size();
	}
	//
	//  名簿ヘッダーが正しいかどうか
	//
	boolean isRegal(Csv headder){
		if(LOG.fa) LOG.println("class meibo #isRegal() : の入り口です");
		//
		int	num = -1;
		//
		// 列項目の順列を示す order を作成する
		// どれかの項目について作成に失敗したときは false を返す
		//
		num = checkAndSet(0,item_0,headder);	if(num<0) {if(LOG.fa) LOG.println("class meibo #isRegal() :return order[0] is faile !"); return false;}
		num = checkAndSet(1,item_1,headder);	if(num<0) {if(LOG.fa) LOG.println("class meibo #isRegal() :return order[1] is faile !"); return false;}
		num = checkAndSet(2,item_2,headder);	if(num<0) {if(LOG.fa) LOG.println("class meibo #isRegal() :return order[2] is faile !"); return false;}
		num = checkAndSet(3,item_3,headder);	if(num<0) {if(LOG.fa) LOG.println("class meibo #isRegal() :return order[3] is faile !"); return false;}
		//
		if(LOG.fa) LOG.println("class meibo #isRegal() :return true !");
		return true;
	}
	//
	//   idx    --- order の添え字
	//   item   --- idx 番目の項目文字列の候補配列
	//   cs ------- 入力したヘッダー項目のＣｓｖ
	//
	int checkAndSet(int idx,String [] item,Csv cs){
		if(LOG.fa) LOG.println("class meibo #checkAndSet() : の入り口です");
		//
		for(int i=0; i<item.length; ++i){		// 全ての候補と比較
			String itm  = item[i];
			for(int k=0; k<cs.size(); k++){		// 全ての入力文字項目と比較
				//
				String 	target 	= cs.get(k);				// ターゲット文字列を取り出し長さが０以上であることを確かめて
				int		len 	= target.length();			// 文字列から空白文字を取り除く
				if(len > 0){
					//
					Vector  temp 	= new Vector(50,10);	// 半角、全角の漢字でない char だけを格納する配列として temp を作る
					for(int m=0; m<len; m++){				// 文字列から１文字づつ取り出して文字種をチェックし
						char a = target.charAt(m);			// パスすれば temp に格納する
						if( (a != ' ')&&(a != '　') ){
							temp.add(new Character(a));
						}
					}
					int sz = temp.size();					// temp のサイズがゼロでないことを確かめてから
					if(sz>0){								// サイズ分の char 配列を作成し
						char [] str = new char[sz];			// この中に temp から１文字づつ取り出して埋めていく
						for(int w=0; w<sz; w++){
							str[w] = ((Character)temp.get(w)).charValue();
						}
						String targetString = new String(str);	// 最後に char 配列から文字列 targetString を生成する
						if(LOG.fa) LOG.println("class exwork #checkAndSet() : 空白を取り除いた文字列 -----> " + targetString );
						//
						if( itm.equals(targetString) ){
							order[idx] = k;				// idx の文字項目は入力ｋ番目の項目である
							return k;					// （重複することはない）
						}
					}
				}
			}
		}		
		return -1;	// ｉｄｘ番目の項目に対する入力がない
	}
	//
	//  order によって並び替えたCsv を返す
	//
	Csv	arrange(Csv csv){
		//
		Vector v = new Vector (10);
		for(int i=0; i<4; i++){
			v.add(" ");
		}
		//
		for(int i=0; i<csv.size(); i++){
			v.set(i,csv.get( order[i] ));
		}
		Csv temp = new Csv(v);
		return temp;
	}
	//
	// 生成時に部分的なエラーがあったかどうか
	//
	public boolean	isSomeError()	{	return someErrorFlag; }
	public boolean	isErrorHeader()	{	return errflag; }
	/*
	 * ヘッダとデータ内容の正当性チェックの結果
	 */
	public	boolean	isInvalid(){// 不正か
	    return	someErrorFlag || errflag;
	    
	}
	//
	//
	//  二つの名簿の差分を取った名簿を作成する（削除分）
	//
	//   xm をオリジナルとし、オリジナルの中で this ではなくなってしまった
	//   学生データ（Csv レコード）を取り出して、あらたな Meibo を作成して
	//   返す
	//
	//   xm を this の中から引く方法で見つける
	//
    public Meibo dif_deleted(Meibo xm){
        Vector 		v	= new Vector(20,10);
		Hashtable	hs	= new Hashtable(500);
		//
		for(int i=0; i<xm.size(); i++){
			String id	= xm.getNumber(i);
			Csv csv		= get(id);		// ハッシュで引く
			if(csv==null){				// 該当がないとき＝削除されたデータ（ xm(i) )
				Csv deleted = xm.get(i);
				v.add(deleted);
				hs.put(deleted.get(1),deleted);
			}
		}
		Meibo m = new Meibo((xm.getHeadder()).getCsvString(),v,hs);
		return m;
    }
	//
	//  二つの名簿の差分を取った名簿を作成する（追加分）
	//
	//   xm をオリジナルとし、this で新たに追加された学生データ（Csv レコード）
	//   を取り出して、あらたな Meibo を作成して返す
	//
	//   this を xm の中から引く方法で見つける
	//
    public Meibo dif_added(Meibo xm){
        Vector 		v	= new Vector(20,10);
		Hashtable	hs	= new Hashtable(500);
		//
		for(int i=0; i<size(); i++){
			String id	= getNumber(i);
			Csv csv		= xm.get(id);		// ハッシュで引く
			if(csv==null){				    // 該当がないとき＝追加されたデータ（ get(i) )
				Csv added = get(i);
				v.add(added);
				hs.put(added.get(1),added);
			}
		}
		Meibo m = new Meibo(headder.getCsvString(),v,hs);
		return m;
    }
	//
	//  二つの名簿の差分を取った名簿を作成する（変更分）
	//
	//   xm をオリジナルとし、オリジナルにもあるデータだが、this では内容が変更された
	//   学生データ（Csv レコード）を取り出して、あらたな Meibo を作成して返す
	//
    public Meibo dif_modified(Meibo xm){
        Vector 		v	= new Vector(20,10);
		Hashtable	hs	= new Hashtable(500);
		//
		for(int i=0; i<size(); i++){
			Csv newCsv  = get(i);
			String id	= getNumber(i);
			//
			Csv oldCsv	= xm.get(id);		// ハッシュオリジナルを引く
			if(oldCsv!=null){				    // オリジナルに該当がある（== csv）とき
				if(!newCsv.equals(oldCsv)){	// しかも内容が異なるとき
					v.add(newCsv);
					hs.put(newCsv.get(1),newCsv);
				}
			}
		}
		Meibo m = new Meibo(headder.getCsvString(),v,hs);
		return m;
    }
	//
	// ベクターに入れた名簿を返す
	//
    public Vector getMeibo()	{ return meibo;} 
	//
	// 課題提出時間で並べ替えた名簿を作る
	//
	//  smb は（学籍番号、課題提出時間）のハッシュ
	//  Answer#makeSmb() で生成できる
	//　提出していない学籍番号の提出時間は含まれない
	//
	public void makeMeibo2(Hashtable smb){
		if(mbFlag) return;	// 既に生成済み
		//
		String [] mb = new String [cnt];
		String noUpdateTime = "9999-99-99 99:99:99";
		for(int i=0; i<cnt; i++){
			String num  = getNumber(i);
			String updateTime = (String)smb.get(num);
			//
			// ソートキーを生成する
			String sortKey = noUpdateTime + "%" + num;
			if(updateTime != null){
				sortKey	= updateTime + "%" + num;
			}
			mb[i] = sortKey;
		}
		// sort する
		Arrays.sort(mb);
		//
		// 
		for(int k=0; k<cnt; k++){
			Csv 	cs 		= new Csv(mb[k],"%");
			String	number  = cs.get(1);
			Csv	    data	= get(number);
			meibo2.add(data);
		}
		mbFlag = true; // 作成したら ture にする
	}
	//
	// 学籍番号順に並べた名簿（Vector）を meibo3 として作成する
	//
	public void makeMeibo3(){
		if(mbFlag_3) return;	// 既に生成済み
		//
		//DBG.outVectorCsv(meibo,"★現在の名簿データ");
		
		String [] mb = new String [cnt];// 文字配列に学籍番号を取り出す
		for(int i=0; i<cnt; i++){
			mb[i]  = getNumber(i);
		}
		// sort する
		Arrays.sort(mb);
		// 
		for(int k=0; k<cnt; k++){
			String	number  = mb[k];
			Csv	    data	= get(number);
			meibo3.add(data);
		}
		//DBG.outVectorCsv(meibo3,"★ソート済みの名簿データ");
		
		
		mbFlag_3 = true; // 作成したら ture にする
	}
	// 現在の状態を得る
	public boolean getSw() { return sw;}
	//
	// 名簿ファイルの切り替え
	public void setSw(boolean ss){
		//DBG.println("class Meibo #setSw() : 名簿ファイルの切り替え です");
		//DBG.println("      ss = " + ss);
		sw 	= ss;
		if(ss){
			if(!mbFlag_3) makeMeibo3();	// 作成されてなければ作る
			meibo 	= meibo3;			// 学籍番号順
			//DBG.println("      meibo3 に切り替えました");
		}else{
			if(!mbFlag) return;	// meibo2 が作成されていないので
			//if(!mbFlag) makeMeibo2();	// 作成されてなければ作る
			meibo 	= meibo2;			// 提出時間順
		}
	}
	//
    // 生成失敗か
    boolean isNull(){
        return  (cnt==-1) ? true : false;
    }   
    // 学籍番号チェック
    // 該当がなかったら null を返す．あれば，そのレコードを返す．
    public Csv numberChk(String number){
        Csv csv;
        for(int i=0; i<cnt; i++){
            csv = (Csv)meibo.get(i);
            if( number.equals(csv.get(1)) ) return csv;
        }
        return null;
    }
    // 件数を返す
    public int getCounts(){ return cnt; }
    public int size()     { return cnt; }
    //
    // 名簿のヘッダーを返す
    public Csv getHeadder(){ return headder; }

    // 学籍番号をキーで検索し，該当のデータを返す
    public Csv get(String key){
        Csv csv  = (Csv)hsMeibo.get(key);
        if(csv == null){
			if(LOG.fa) LOG.outHash(hsMeibo,"class Meibo # Csv get(" +  key + ") は null です");
			return null; // 念のため書いておく
		}
		if(LOG.fa) LOG.outHash(hsMeibo,"class Meibo # Csv get(" +  key + ") は 該当がありました");
        return csv;
    }
    //
    // 題ｉ番目の名簿データを返す
    public Csv get(int i){
        return ((Csv)(meibo.get(i)));
    }
    // 題ｉ番目の名簿データに含まれるメールアドレスを返す
    public String getMailAd(int i){
        return ((Csv)(meibo.get(i))).get(0);
    }
    // 題ｉ番目の名簿データに含まれる学籍番号を返す
    public String getNumber(int i){
        return ((Csv)(meibo.get(i))).get(1);
    }
    // 題ｉ番目の名簿データに含まれる氏名を返す
    public String getName(int i){
        return ((Csv)(meibo.get(i))).get(2);
    }
    // 題ｉ番目の名簿データに含まれる携帯メールアドレスを返す
    public String getKeitai(int i){
        return ((Csv)(meibo.get(i))).get(3);
    }
    /**
	 * 名簿ファイルに教師ID記述がなければ追加し、
	 * ファイルそのものを更新しておく
	 * 番号でソートされた状態のファイルになり、しかも教師IDは末尾になる
	 * 
	 * 名簿ファイル受け取り時に、KamokuApRecord.addMeibo(), KamokuApRecord.updateMeibo() 
	 * から呼ばれる
	 *      
	 * @param teUid
     * @param tname
     * @param tmail
     */
    public	void	complete(String teUid, String tname, String tmail){
        
        /*
         * 番号でソート済みの名簿に切り替える
         */
        setSw(true);
        /*
         * 教師データが名簿ファイルに含まれなければ追加する
         */
        Csv	rec	=	(Csv)hsMeibo.get(teUid);
        if(rec==null){
            /*
             * 内部データにレコードを追加（教師IDは末尾に位置する）
             */
            String	csvStr	=	tmail + "," + teUid + "," + tname + ",-";
    		Csv		cs		=	new	Csv(csvStr);
            meibo.add(cs);
        }
        /*
         * 内部データをファイルに書き戻す
         */
        rewrite();
        
    }
    public	void	rewrite(){
        /*
         * 名簿ファイルを出力する
         * 携帯メールは指定しないものと仮定している
         */
        PrintWriter	out;
        try{
            out  			=	new PrintWriter(new OutputStreamWriter(new FileOutputStream(path),"Windows-31J"));
            String	headder	=	"番号,氏名,メール";
            out.println(headder);
            //
            int	n	=	meibo.size();
            for(int i=0; i<n; i++){
                Csv	cs 	=	(Csv)meibo.get(i);
                String	line	=	cs.get(1) + "," + cs.get(2) + "," + cs.get(0);
                out.println(line);
            }
            out.close();
        
        }catch(IOException e){
            e.printStackTrace();
        }
        
    }
    @Override
	public String toString() {
    	StringBuilder sb = new StringBuilder();
        sb.append("--- Meibo ---\n");
        for(int i=0; i<meibo.size(); i++){
            Csv	cs 	=	(Csv)meibo.get(i);
            String	line	=	"\t[" + cs.get(1) + "," + cs.get(2) + "," + cs.get(0) + "," + cs.get(3) + "]\n";
            sb.append(line);
        }    	
        sb.append("-------------\n");
    	return sb.toString();
    }
    
}