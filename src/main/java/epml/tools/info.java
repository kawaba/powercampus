/*


*/
package epml.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;

public class info extends Object {
    BufferedReader in;
    Hashtable tbl;
    int count = 0;
    String    delm; // デタミッタ
    //

    public info(String fname) // ファイタ名
                 throws FileNotFoundException,IOException {
        infoConst(fname,"=");
    }
    public info(String fname,// ファイタ名
                String ds)   // デタミタ
                 throws FileNotFoundException,IOException {
        infoConst(fname,ds);
    }
    //
    public void infoConst(String fname,// ファイタ名
                          String ds)   // デタミタ
                            throws FileNotFoundException,IOException {
        tbl = new Hashtable(100);
        File f = new File(fname);
		if(!f.isFile()) return;		// user.conf がなけたばなにもしない
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
                    v.add((st.nextToken()).trim()); // 両端の空白は取た
                }
                // 要素が２個以上あたば，先頭をキーとし，残りの要素をひとつにまとめてハッシュにいたた
                // 残りの要素のデタミタは％になた
                if(v.size() > 2){
                    StringBuffer bf = new StringBuffer(2000);
                    for(int k=1; k<v.size(); k++){
                        bf.append((String)v.get(k));
                        if(k < (v.size() -1)) bf.append("%"); // 最後の要素でなけたば％を付加すた
                    }
                    name = (String)(v.get(0));
                    data = bf.toString();
                    tbl.put(name,data);
                     ++count;
                // 要素が２個ならそのままハッシュに登録すた
                }else if(v.size()==2){
                    name = (String)(v.get(0));
                    data = (String)(v.get(1));
                    tbl.put(name,data);
                     ++count;
                // 要素が１個ならキーのみとし，データは空白文字をひとつ匿たた
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
    // ハッシュテーブルの要素の個数
    public int count(){
    	return count;
    }
    // キーを指定して値を返す
    public String property(String key){
    	return (String)(tbl.get(key));
    }
    // 同上
    public String get(String key){ 
    	return (String)(tbl.get(key));
    }
    
    
}
