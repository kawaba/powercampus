package tktools;

import java.io.*;
import java.util.*;

public class Conf extends Object {
    BufferedReader in;
    Hashtable tbl;
    int count = 0;
    String    delm; // デリミッタ
    //

    public Conf(String fname) // ファイル名
                 throws FileNotFoundException,IOException {
        infoConst(fname,"=");
    }
    public Conf(String fname,// ファイル名
                String ds)   // デリミタ
                 throws FileNotFoundException,IOException {
        infoConst(fname,ds);
    }
    //
    public void infoConst(String fname,// ファイル名
                          String ds)   // デリミタ
                            throws FileNotFoundException,IOException {
        tbl = new Hashtable(100);
        File f = new File(fname);
        if(!f.isFile()) return;     // user.conf がなければなにもしない
        //
        //in  = new BufferedReader(new InputStreamReader(new FileInputStream(fname),"Windows-31J"));
        in  = new BufferedReader(new InputStreamReader(new FileInputStream(fname)));
        //
        String line;
        String name,data;
        while((line=in.readLine())!=null){
            if((line.length()>0)&&(line.charAt(0) != '#')){
                Vector v = new Vector(10,2);
                StringTokenizer st = new StringTokenizer(line,ds);
                while(st.hasMoreTokens()){
                    v.add((st.nextToken()).trim()); // 両端の空白は取る
                }
                // 要素が２個以上あれば，先頭をキーとし，残りの要素をひとつにまとめてハッシュにいれる
                // 残りの要素のデリミタは％になる
                if(v.size() > 2){
                    StringBuffer bf = new StringBuffer(2000);
                    for(int k=1; k<v.size(); k++){
                        bf.append((String)v.get(k));
                        if(k < (v.size() -1)) bf.append("%"); // 最後の要素でなければ％を付加する
                    }
                    name = (String)(v.get(0));
                    data = bf.toString();
                    tbl.put(name,data);
                     ++count;
                // 要素が２個ならそのままハッシュに登録する
                }else if(v.size()==2){
                    name = (String)(v.get(0));
                    data = (String)(v.get(1));
                    tbl.put(name,data);
                     ++count;
                // 要素が１個ならキーのみとし，データは空白文字をひとつ入れる
                }else{
                    name = (String)(v.get(0));
                    data = " ";
                    tbl.put(name,data);
                     ++count;
                }
            }
        }
        in.close();
    }
    public int count(){ return count;}
    public String property(String key){ return (String)(tbl.get(key));}
    public String get(String key){ return (String)(tbl.get(key));}
}
