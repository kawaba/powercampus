//
//  eXmail server へ差し込みメール送信を依頼する
//
//
package mailutil;
import java.io.*;
import java.util.*;
import java.net.*;

import framework.Cp932;
//
public class exSender extends Object {
    //
    private     boolean DEBUG       = false;
    private     boolean DEBUG_ON    = false;
    //
    final String CR = System.getProperty("line.separator");
    final String PS = File.separator;
    //
    String template_;    // 読みこんだテンプレートデータ
    //
    String servletURL;
    //
    String mailhost;
    String from;
    String title_;
    Vector vt;
    String templatePath;
    String mailAdmin;
    Vector itms;
    //String user;
    String sysUserName;
    //
    // ダミーの値
    String  midnight;
    String  [] pathString;
    String  mailpassword;
    //              
    public  exSender( String _mailhost,
                    String _from,
                    String _title,
                    Vector _vt,
                    String _templatePath,
                    String _mailAdmin,
                    Vector _itms,
                    //String _user,
                    String _servletURL,
                    String _sysUserName){
        //
        mailhost        = new String(_mailhost);
        from            = new String (_from);
        title_          = new String (_title);
        vt              = (Vector)_vt.clone();
        templatePath    = new String (_templatePath);
        mailAdmin       = new String (_mailAdmin);
        itms            = (Vector)_itms.clone();
        //user            = new String (_user);
        servletURL      = new String (_servletURL);
        sysUserName     = new String (_sysUserName);
        //
        // ダミーの値を埋める
        midnight        = "OFF";            // これでないといけない
        pathString      = new String[3];    // null に初期化されるがこれでいい
        mailpassword    = "ok_Im_a_System"; // これでないといけない（システムアクセスであることを表す）
        //
        getTemplate(templatePath);
    }
    // テンプレートファイルを読みこむ
    public void getTemplate(String path){
        //
        //System.out.println("template path = " + path);
		//
		StringBuffer bf = new StringBuffer(10000);
        BufferedReader in = null;
        try{
            in  = new BufferedReader(new InputStreamReader(new FileInputStream(path),"Windows-31J"));
            String line;
            while((line=in.readLine())!=null){
                bf.append(line + CR);
            }
            in.close();
        }catch(IOException e){
            System.out.println("★テンプレートファイルを開けません．(exSender.java)" + path);
            System.out.println(e);
            template_ = "テンプレートファイルを開けませんでした．(exSender.java) :" + path;
            return;
        }
        template_ = bf.toString();
    }
    // 送信処理
    public void sendMail(){
        //
        //
        // Cp932 はMS932エンコーディングをJISエンコーディングに補正する
        String title    = Cp932.forJisMail(title_);
        String template = Cp932.forJisMail(template_);
        // vt は，アドレス部が空白のレコードをﾌて，さらにVectorをTrimする
        Vector vtJis    = reTable(vt); // cp932 変換を含む
        Vector itmsJis  = convertToJis(itms); // cp932 変換
        //
        try{
            // httpurl からオブジェクト出力ストリームを作成する
            URL u = new URL(servletURL);
            URLConnection uc = u.openConnection();
            uc.setDoOutput(true);
            uc.setDoInput(true);
            uc.setUseCaches(false);
            try{
                ObjectOutputStream out = new ObjectOutputStream(uc.getOutputStream());
                // 出　力
                // 
                // 2003.1.11 eXmail の変更に伴いこちらも変更した
                //out.writeObject(sysUserName); // pop3認証用ユーザー名（システムユーザー名）
                //out.writeObject(sysUserName); // pop3認証用パスワード（システムユーザーの場合この値は使われない）
                //
                out.writeObject(mailhost);   // メールサーバー
                out.writeObject(title);      // メールタイトル
                out.writeObject(from);       // 発信ﾒメールアドレス
                out.writeObject(template);   // 送信文テンプレート
                out.writeObject(mailAdmin);  // メール管理ﾒのメールアドレス
                out.writeObject(vtJis);      // データ配列全体
                out.writeObject(itmsJis);    // データ配列の項目名
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
                //
                out.flush();
                out.close();
                //
                try{
                    ObjectInputStream in = new ObjectInputStream(uc.getInputStream());
                    //入　力
                    try{
                        String s1 = (String)in.readObject(); // 結果の取得
                        in.close();
                        //
                        if(DEBUG) System.out.println(s1 + CR); // デバッグ用
                        //
                    }catch(StreamCorruptedException e3){
                        System.out.println("ストリームの制御情報に一貫性がない");
                    }catch(ClassNotFoundException e4){
                        System.out.println("直列化されたオブジェクトのクラスが見つからなかった");
                    }catch(InvalidClassException  e5){
                        System.out.println("直列化で使用されるクラスになんらかの不具合があった");
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
    public void debugpr(
            String template_,    // 読みこんだテンプレートデータパス
            String servletURL,
            String mailhost,
            String from,
            String title_,
            Vector vt,
            String templatePath,
            String mailAdmin,
            Vector itms,
            String uer,
            String sysUserName
        ){
            //
            System.out.println(template_);
            System.out.println(servletURL);
            System.out.println(mailhost);
            System.out.println(from);
            System.out.println(title_);
            System.out.println(templatePath);
            System.out.println(mailAdmin);
            System.out.println(uer);
            System.out.println(sysUserName);


        for(int i=0; i<itms.size(); i++){
            System.out.println((String)itms.get(i) + " : " );
        }
        for(int j=0; j<vt.size(); j++){
            Vector v = (Vector)vt.get(j);
            for(int k=0; k<v.size(); k++){
                System.out.print((String)v.get(k) + " : " );
            }
            System.out.println();
        }

    }
    ///////////////////////////////////////  デバッグ用　/////////////////
}