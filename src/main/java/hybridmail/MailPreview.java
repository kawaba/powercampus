package hybridmail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import database.Database;
import database.DbConnectionBroker;
import framework.LOG;
import framework.Param;
import framework.SuperPlayer;
import mailutil.Exmail;
import tktools.Csv;

/**
*
*
	#
	# ##################
	#    MailPreview
	# ##################
	#
	<program $hybridmail.MailPreview>
		<dispatch  html=MailPreview.html  number=6030  class=hybridmail.MailPreview />
		<variable>
		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA DIVISION HOMEURL title lec_key aplec_key 
		             ckbox  max_students  total_view sendEmail sendKmail emText  kmText />
		  <accept    CMD    UPLODE  />
		  <keep      view    />
		  
		  <form      />
		</variable>
	</program> 
*
*
* 変数の説明
*
* 1. receive 
* 		emText --- e-mail のテンプレート文(HybridMail ではテキストエリア変数)
* 		kmText --- keitai のテンプレート文(HybridMail ではテキストエリア変数)
*
* * 2. accept
* 3. keep
* 		view   --- 現在表示しているレコード番号
* 
* 4. form
*
* 
*/
public class MailPreview extends SuperPlayer {

	
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
	Database	db;
	
	
	String  		teUid;
	String  		aplec_key;
	Csv				toList;		// 送信先のリストを　0 と 1 の並びで表したもの
	String 			method;


	public	MailPreview(){
		super();
		if(LOG.fa) LOG.println("■ MailPreview #コンストラクタ");
	}	
	
	@Override
	public	void initialize(PrintWriter out, Hashtable htb, Param para){
		broker	=	getDbConnection();
		db		=	new Database(broker);	
		
   		
		// メール送信選択（ケータイ／e-mail）をハッシュに設定しておく
		String sw_keitai	= strHash(htb,"_sw_keitai");
		if(sw_keitai!=null){
			htb.put("_sendKmail","ON");
		}else{
			htb.put("_sendKmail","OFF");
		}
		String sw_email	= strHash(htb,"_sw_email");
		if(sw_email!=null){
			htb.put("_sendEmail","ON");
		}else{
			htb.put("_sendEmail","OFF");
		}
		
		// 送信先を表すチェックボックスの情報をシステムハッシュから受け取る
		toList	= new Csv(getParameter("ckbox"));
		
		// キー
		teUid		= strHash(htb,"_teUid");
		aplec_key	= strHash(htb,"_aplec_key");
		//
		
    }

	@Override
	public	String	dispatch(){
		if(LOG.fa) LOG.println("■Sample #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		if(cmd.equals("VIEW_NEXT")){
			if(LOG.fa) LOG.println(method + "(VIEW_NEXT) 次のデータを表示します");		// 表示番号の制御を行うが、有効な番号は
			//																				// １～ＭＡＸまでである。０番はヘッダ行なので使えない
			int	total	= Integer.parseInt( strHash(htb,"_total_view"));					// 表示処理の display() が使うデータはヘッダも含んだ
			int	k		= Integer.parseInt( strHash(htb,"_view"));							// ものであるためこのような仕様になっている
			if( k < total-1){																// 総件数はヘッダ行も含んでいることにも注意
				++k;
			}else{																			// 総件数１０のとき有効な番号は １～９(total-1)までである！
				k=1;																		//
			}
			int	pos	=	dispNum(k);
			if( pos	< 0) {
				/*
				 * プレビューできないのでリターンする
				 */
				htb.put("_msg","★ 送信先が１件も指定されていません");
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_RETURN;
			}			
			/*
			 * pos番目の差込データと入力されたメール本文を元に、差込確認画面を表示する
			 * posの値は view に記録されるので何時でも参照できる
			 */
			putParameter("view", String.valueOf(pos));
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;			
			//
		}else if(cmd.equals("VIEW_BACK")){
			if(LOG.fa) LOG.println(method + "(VIEW_BACK) 前のデータを表示します");
			//
			int	total	= Integer.parseInt( strHash(htb,"_total_view"));
			int	k		= Integer.parseInt( strHash(htb,"_view"));
			if( k > 1){
				--k;
			}else{
				k=total-1;
			}
			int	pos	=	dispNum(k);
			if( pos	< 0) {
				/*
				 * プレビューできないのでリターンする
				 */
				htb.put("_msg","★ 送信先が１件も指定されていません");
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_RETURN;
			}			
			/*
			 * pos番目の差込データと入力されたメール本文を元に、差込確認画面を表示する
			 * posの値は view に記録されるので何時でも参照できる
			 */
			putParameter("view", String.valueOf(pos));
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;			

		}else if(cmd.equals("VIEW_ENTER")){
			if(LOG.fa) LOG.println(method + "(VIEW_ENTER) 指定された番号のデータを表示します");
			//
			int		total	= Integer.parseInt( strHash(htb,"_total_view"));
			String 	banStr 	= strHash(htb,"_ban");
			int	k;
			if( isDigitx(banStr) ){
				k	= Integer.parseInt(banStr);				// Ｋは１～ totol -1 の間でなくてはならない
				if(k < 1)  			k 	= 1;				// 調整してから表示する
				if(k > total - 1)	k	= total - 1;		// 
			}else{
				htb.put("_msg","★ 番号は半角の数字で指定してください");
				k	= Integer.parseInt( strHash(htb,"_view"));
			}
			/*
			 * ENTER出来るということは、少なくともひとつは表示すべきデータがある
			 */
			int	pos	=	dispNum(k);
			/*
			 * pos番目の差込データと入力されたメール本文を元に、差込確認画面を表示する
			 * posの値は view に記録されるので何時でも参照できる
			 */
			putParameter("view", String.valueOf(pos));
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;			

		}else if(cmd.equals("RETURN")){
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;			
			
        }else{
			/*
			 *　再表示
			 */
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;
        }
		/*
		 * 次に表示するときあるいはリターンしてきた時の表示モードをセットしてからリターンする
		 */
		putParameter(DISP_KEY,disp_mode);
		return	ret;    }
	//
    // {}で囲まれた部分をパースして置きかえる
    // 列番号ではなく項目名を使って置き換える方式に変更(2002.4)
    // {<BR>} の CR への置き換え機能の追加により，複数行からなる置き換え文字列に対応した(2002.5.10)
    protected String makeBody(Hashtable title,Vector v,String body){
		
    	/*
    	try {
			LOG.logOn(new PrintWriter("w:\\log\\log.txt"));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}
    	
    	LOG.println  ("class MailPreview #makeBody() : {}で囲まれた部分をパースして置きかえます");
		LOG.outHash(title,"項目名のハッシュテーブルです");
		LOG.outVector(v,"差込データです");
		LOG.println("置き換える本文です");
		LOG.println(body);
		LOG.println("");
        */
    	
		StringBuffer bf = new StringBuffer(102400);// これで十分か？100KB
        String dt;
        try{
            BufferedReader r = new BufferedReader(new StringReader(body));
            boolean flag = false;
            while((dt=r.readLine())!=null){
                int ps=0;
                if(flag) bf.append(CR);// 最後
                flag = true;
                StringTokenizer st = new StringTokenizer(dt,"{}");
                while(st.hasMoreTokens()){
                    String s = st.nextToken();
                    if((s.startsWith("$"))&&(s.length()>=2)){//＄･･で最低2文字
                        try{
                            String key = s.substring(1);
                            Integer no = (Integer)title.get(key);
                            if(no != null){
                                // <BR>を含む場合整形が必要
                                // chkBRは文字列中の全ての<BR>をCRに置き換える
                                // インデントの文字数を半角空白であるとして数えるのでインデントを正しく表示するには
                                // テンプレートで空白を空けるときに，全角でなく半角スペースを使わねばならない
                                String ss = (String)(v.elementAt(no.intValue()));
                                String temp = chkBR(ss,ps);
                                bf.append(temp);
                            }else{
                                bf.append(s); ps+=s.length();
                            }
                        }catch(NumberFormatException e){
                            bf.append(s); ps+=s.length();
                        }
                    }else{ //通常の行
                        bf.append(s); ps+=s.length();
                    }
                }
            }
        }catch (IOException ee){
            System.out.println("can't readln() dt.txt");
        }
        String all = bf.toString();
        return all;
    }
    // もしあれば，それをCRに置き換える
    // この処理は残りの文字列に対して再帰的に繰り返される
    public String chkBR(String s,int pos){
        int at;
        if((at=s.indexOf("{<BR>}"))==-1) return s;
        //
        StringBuffer sp = new StringBuffer(500);
        for(int i=0; i<pos; i++){
            sp.append(" ");
        }
        String indent = sp.toString();

        //
        StringBuffer bf = new StringBuffer(5000);
        replace(bf,at,s,indent);
        return bf.toString();
    }
    public void replace(StringBuffer bf,int at,String s,String indent){
        bf.append(s.substring(0,at));
        bf.append(CR);
        String str = s.substring(at+6);
        if(str.length()>0){
            if((at=str.indexOf("{<BR>}")) != -1 ){
                bf.append(indent);
                replace(bf,at,str,indent);
            }else{
                bf.append(indent + str);
            }
        }
        return;
    }
	//
	// 実際に表示可能かどうか番号をチェックして修正する
	// k は　１ オリジン
	// k が送信先として指定されていない番号であれば、近くの送信先番号を探して返す
	// 送信先が１件も指定さていない場合は -1 を返す
	int	dispNum(int	k){
		String	pos	=  toList.get(k-1);
		if(pos.equals("1"))			return	k;
		//
		int		n	= toList.size();
		for(int i=k; i<n; i++){
			pos	= toList.get(i);
			if(pos.equals("1")){
				return	i+1;
			}
		}
		for(int j=0; j<k-1; j++){
			pos	= toList.get(j);
			if(pos.equals("1")){
				return	j+1;
			}
		}
		return	-1;	// 該当がない場合
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
	 * 第ｋ件目のデータでプレビュー画面を表示する
	 * 
	 * @param	editmode	表示モード（true なら編集、falseなら新規）
	 */
	@Override
	public  void	display(boolean editmode){
		if(LOG.fa) LOG.println("class MailPreview #displayView()");
		
		if(!editmode){
			putParameter(MESSAGE,"");
			/*
			 * 先頭データの最近傍のデータ
			 */
			putParameter("view",String.valueOf(dispNum(1)));
		}
		/*
		 * kは第ｋ件目のデータを指す。
		 * 不正なKの値は生じない
		 */
		int		k	=	Integer.parseInt(getParameter("view"));
		/*
		 * Exmail クラスが作成しているデータファイルをベクターに読み込む
		 * データは temp_i がe-mail 用、temp_k 携帯メール用であるが
		 * 第０列のメールアドレスが違うだけで、あとは同じなので temp_i を使う.
		 * ｋ件目のデータを表示するだけなので、第０行目のヘッダー、ｋ行目のデータだけを使えばよい
		 * 
		 */
		String	aplec_key	= 	strHash(htb,"_aplec_key");
		String	teUid		= 	strHash(htb,"_teUid"); 
		Vector	vFile		= 	new Vector(200,100); 
		String	path		= 	para.getMailTempDir(teUid,aplec_key) + Exmail.NAME_OF_I; 
		loadToVector(path,vFile); 
		Csv		items	= new Csv( (String)vFile.get(0) ); 
		Csv		data_k	= new Csv( (String)vFile.get(k) );
		if(LOG.fa){
			LOG.println(" ");
			LOG.println("★1★items: ");
			LOG.println(items.toCSV());
			LOG.println("--------");
		}
		if(LOG.fa){
			LOG.println(" ");
			LOG.println("★2★data_k: ");
			LOG.println(data_k.toCSV());
			LOG.println("--------");
		}
		/*
		 * ０行目のデータから項目名をキーとし、列番号を値とする。
		 * ハッシュを作成する。書き換えで使う。
		 */
		Hashtable ht	= new Hashtable(200);
        for(int i=0; i<items.size(); i++){ 
            ht.put(items.get(i),new Integer(i)); 
        }
		/*
		 * ｋ行目のデータをベクターに変換しておくが、これは変換メソッドの仕様に従っただけ。
		 */
		Vector	dt_k	= data_k.toVector();
		/*
		 * これらは入力されたメール文で、_emTextは e-mail 用、_kmText は携帯メール用
		 * 共に差込のテンプレートとして扱う
		 */
		String	emtext	= getParameter("emText"); 
		String	kmtext	= getParameter("kmText"); 
		if(LOG.fa){
			LOG.println(" ");
			LOG.println("★3★emtext: ");
			LOG.println(emtext);
			LOG.println("--------");
		}
		/*
		 * makeBody はテンプレートの差込項目を差込データで置き換えて、
		 * メール文を作成する。e-mail 用と携帯用を同時に作成し、
		 * 表示のために、おのおの htb に埋め込んでおく
		 */
		String	emBody	= "";
		String	kmBody	= "";
		if(emtext.length() > 0) emBody	= makeBody(ht,dt_k,emtext); 
		if(kmtext.length() > 0) kmBody	= makeBody(ht,dt_k,kmtext); 
		htb.put("_emText_conf",emBody); 
		htb.put("_kmText_conf",kmBody); 
		if(LOG.fa){
			LOG.println(" ");
			LOG.println("★4★emBody: ");
			LOG.println(emBody);
			LOG.println("--------");
		}		
		/*
		 * 総件数（０行目のヘッダ部含む）と現在表示しているデータの番号を覚えておく。
		 * 表示データを送ったり戻したりするのに必要である
		 * 番号を指定するテキストボックスにも数字を入れておく
		 */
		htb.put("_total_view",String.valueOf(vFile.size())); 
		htb.put("_view",String.valueOf(k)); 
		htb.put("_ban",String.valueOf(k)); 

		/* getParameter(DISPFILE)にはファイルの完全パス名が入っている */
		Vector	v	=	loadHtml(getParameter(DISPFILE));
		printVector(v);		

    }
	/**
	 * 
	 */
    @Override
	public void write(String key,Vector exHtml,Hashtable htb){
    	
    	if(key.equals("sendMark")){
    		sendMark(exHtml);
			
		}
    }
	/**
	 * メール送信画面の表示（送信するメール種類のチェックボックス）
	 * 送信選択を覚えておくために、プログラムでは
	 * 
	 *  		_sendEmail --- e-mail を送信する
	 *  		_sendKmail --- ケータイを送信する
	 * 
	 * を設定する。以下の処理はこの値に基づいてチェックボックスの状態を設定する
	 */
	void	sendMark(Vector exHtml){
		if(LOG.fa) LOG.outHash(htb,"class MailPreview #write()");
		//
		String sendEmail	= strHash(htb,"_sendEmail"); if(isEmpty(sendEmail))	sendEmail="OFF";
		if(sendEmail.equals("ON")){
			htb.put("_imSign","checked");
		}else{
			htb.put("_imSign","");
		}
		//
		String sendKmail	= strHash(htb,"_sendKmail"); if(isEmpty(sendKmail))	sendKmail="OFF";
		if(sendKmail.equals("ON")){
			htb.put("_kmSign","checked");
		}else{
			htb.put("_kmSign","");
		}
		printVector(exHtml,htb);

	}
	
}
