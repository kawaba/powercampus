package mailutil;
import java.io.File;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.StreamCorruptedException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalTime;
import java.util.Enumeration;
import java.util.Vector;
import framework.Cp932;
import framework.LOG;
//
public class eXmailSender {
	
   //
    final String CR = System.getProperty("line.separator");
    final String PS = File.separator;
    //
    String from;
    String title;
    Vector vt;
    String template;
    String mailAdmin;
    Vector itms;
    String servletURL;
    
    String bulk;
    //
    // ダミーの値
    String mailhost;
    String sysUserName;
    String  midnight;
    String  [] pathString = {"","",""};
    String  mailpassword;
    //              
    public  eXmailSender(
                    String _from,				// 差出人メールアドレス
                    String _title,				// メール表題
                    Vector _vt,					// 差し込みデータ（またはメールアドレス一覧表）
                    String _template,			// 送信テンプレート（またはメール本文）
                    String _mailAdmin,			// メール管理者（logger@mail-and-work.net）
                    Vector _itms,				// 差し込み項目（またはメールアドレス一覧の項目名）
                    String _servletURL,		// eXmail server への URL
                    String _bulk				// 差し込みモードの時 "ON"
                    
    		){		// POP before SMTP 認証用ＩＤだがこれも現在ではダミーデータでよい
    	
    	
        //
		if(LOG.fa) LOG.println("class eXmailSender #eXmailSender() : コンストラクタです");
		//
        mailhost        = "abc@com"; // ダミーでよい
        from            = new String (_from);
        title           = new String (_title);
        vt              = (Vector)_vt.clone();
        template    	= new String (_template);
        mailAdmin       = new String (_mailAdmin);
        itms            = (Vector)_itms.clone();
        servletURL      = new String (_servletURL);
        sysUserName     = "someone"; // ダミーでよい
        //
        // ダミーの値を埋める
        midnight        = "OFF";            // これでないといけない
        //pathString      = new String[3];    	// これでいい
        mailpassword    = "ok_Im_a_System"; // これでないといけない（システムアクセスであることを表す）
        bulk = _bulk;
        //
		//DBG.outVector(itms,"_itms");
		//DBG.outVector(itms,"itms");
		
    }
	//
    // 送信処理
    public String sendMail(){
    	if(LOG.tr) LOG.println("★eXmailSender#sendMail："+LocalTime.now());
		//
        String s1 = ""; //サーバーからの返信
		// Cp932 はMS932エンコーディングをJISエンコーディングに補正する
        String titleJis    = Cp932.forJisMail(title);
        String templateJis = Cp932.forJisMail(template);
        // vt は，アドレス部が空白のレコードを捨て，さらにVectorをTrimする
        Vector vtJis    = reTable(vt); // cp932 変換を含む
        Vector itmsJis  = convertToJis(itms); // cp932 変換

        if(LOG.fa) dump(itmsJis,vtJis,from,titleJis,templateJis,mailAdmin,servletURL,mailhost,sysUserName);
        try{
            // httpurl からオブジェクト出力ストリームを作成する
            URL u = new URL(servletURL);
            URLConnection uc = u.openConnection();
            uc.setDoOutput(true);
            uc.setDoInput(true);
            uc.setUseCaches(false);
            try{

            	if(LOG.fa) LOG.println("★データ送信開始："+LocalTime.now());

            	
                ObjectOutputStream out = new ObjectOutputStream(uc.getOutputStream());
                // 出　力
                // 
                // 2003.1.11 eXmail の変更に伴いこちらも変更した
                //out.writeObject(sysUserName); // pop3認証用ユーザー名（システムユーザー名）
                //out.writeObject(sysUserName); // pop3認証用パスワード（システムユーザーの場合この値は使われない）
                //
                out.writeObject(mailhost);   	// メールサーバー
                out.writeObject(titleJis);     // メールタイトル
                out.writeObject(from);       	// 発信者メールアドレス
                out.writeObject(templateJis);  // 送信文テンプレート
                out.writeObject(mailAdmin);  	// メール管理者のメールアドレス
                out.writeObject(vtJis);      	// データ配列全体
                out.writeObject(itmsJis);    	// データ配列の項目名
                //
                // 以下は追加部分（ダミーを送る） 2003.1.11 eXmail の変更に伴いこちらも変更した
                out.writeObject(midnight);          // ミッドナイトオプションフラグ 2003.1.4 追加
                //
                out.writeObject(servletURL);        // サーバーサーブレットのURL　/2002.9.7 追加
                //
                out.writeObject(pathString[0]);     // 台帳のパス
                out.writeObject(pathString[1]);
                out.writeObject(pathString[2]);
                out.writeObject(mailpassword);      // システムアクセスであることをｦすため必須
                
                out.writeObject(bulk);      // 差し込みモードの時"ON" （一斉送信できないことを示す）

                //
                out.flush();
                out.close();
                
                // 経過時間
                if(LOG.fa) LOG.println("★データ送信終了："+LocalTime.now());

            	if(LOG.fa) LOG.println("★データ受信開始："+LocalTime.now());
                //
                try{
                    ObjectInputStream in = new ObjectInputStream(uc.getInputStream());
                    //入　力
                    try{
                        s1 = (String)in.readObject(); // 結果の取得
                        in.close();
                        //
                        if(LOG.fa) LOG.println("サーバーから受け取ったメッセージです"+ s1);
                        if(LOG.fa) LOG.println(s1); // デバッグ用
                        //
                    }catch(StreamCorruptedException e3){
                        System.out.println("ストリームの制御情報に一貫性がない");
                    }catch(ClassNotFoundException e4){
                        System.out.println("直列化されたオブジェクトのクラスが見つからなかった");
                    }catch(InvalidClassException  e5){
                        System.out.println("直列化で使用されるクラスになんらかの不具合があった!!!");
                    }catch(OptionalDataException e6){
                        System.out.println("プリミティブデータが、オブジェクトではなくストリームに見つかった");
                    }catch(IOException e7){
                        System.out.println("通常の入出力関連の例外のどれかが発生した");
                    }
                }catch(StreamCorruptedException e1){
                    System.out.println("入力ストリームを開けません");
                }catch(IOException e2){
                    System.out.println("サーバーからのデータが読めません");
                }
                
                // 経過時間
                if(LOG.fa) LOG.println("★データ受信終了："+LocalTime.now());
                
                //
            }catch(NotSerializableException e9){
                System.out.println("直列化の対象オブジェクトが java.io.Serializable インタフェースをﾀ装していない");
            }catch(InvalidClassException e1){
                System.out.println("基本となる OutputStream が例外をスローした場合");
            }catch(IOException e8){
                System.out.println("直列化で使用されるクラスになんらかの不具合があった");
            }
        }catch(MalformedURLException e10){
            System.out.println("文字列に指定されたプロトコルが未知である");
        }catch(IOException e11){
            System.out.println("入出力例外が発生した");
        }
	
		return	s1;
    }
    //
    // ここでは，アドレス部が空白など，無効なレコードを消去することによりデータ配列を再作成する
    public Vector reTable(Vector v){
        Vector cv = new Vector(500,100);
        for(Enumeration e = v.elements(); e.hasMoreElements(); ){
            Vector temp = (Vector)e.nextElement(); // 行データ
            if(chksub((String)temp.elementAt(0))) {// アドレスが書いてあるかどうか
                cv.add( convertToJis(temp) ); // Cp932にてJISに変換してから移す
            }
        }
        cv.trimToSize(); // ﾀ使用領域分に行サイズを縮める
        return cv;
    }
    // JIS 変換
    public Vector convertToJis(Vector v){
        int n = v.size();
        String str;
        Vector w = new Vector(30,10);
        for(int i=0; i<n; i++){
            str = Cp932.forJisMail( (String)v.get(i) );
            w.add(i,str);
        }
        return w;
    }
    // 文字列のnull，空白チェック
    public boolean chksub(String s){
        if((s != null) && (s.length() > 0)) return true;
        return false;
    }
    //
    ///////////////////////////////////////  デバッグ用　/////////////////
    public void dump(Vector _items,Vector _vt,String _from,String _title,String _template,String _mailAdmin,String _servletURL,String _mailhost,String _sysUserName){
        //
        String method = "class eXmailSender #dump() : ";
		LOG.println(method + "送信パラメータリストです");
        LOG.println(method +  "   from        = "+ _from);
        LOG.println(method +  "   title       = "+ _title);
		LOG.println(method +  "   template    = "+ _template);
        LOG.println(method +  "   mailAdmin   = "+ _mailAdmin);
        LOG.println(method +  "   servletURL  = "+ _servletURL);
        LOG.println(method +  "   mailhost    = "+ _mailhost);
        LOG.println(method +  "   sysUserName = "+ _sysUserName);

        //
        LOG.outVector(_items,"items  項目名リスト" );
        //
        LOG.println(method +  "vt 差し込みデータ");
		for(int j=0; j<_vt.size(); j++){
            Vector v = (Vector)_vt.get(j);
            for(int k=0; k<v.size(); k++){
                System.out.print((String)v.get(k) + " : " );
            }
            System.out.println("");
        }
    }	
}
