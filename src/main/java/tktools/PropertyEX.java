package tktools;
import java.io.*;
import java.util.*;
/*
    データの形と処理結果
    aa = bb%cc%dd ---> { key | data } = { aa | bb,cc,dd }  delm = "%" を指定
    aa = bb,cc,dd ---> { key | data } = { aa | bb,cc,dd }  delm = "," を指定
    aa,bb,cc,dd   ---> ( key | data } = { aa,bb,cc,dd | "△"  }
    aa=bb=cc=dd   ---> ( key | data } = { aa | bb%cc%dd }

*/
public class PropertyEX extends Property {
    static final String NOEXIST = "NO_DATA"; // データが無いとき返す値
    String delm2;
    Hashtable exh;
    //
    public PropertyEX(String fname,String dl) throws FileNotFoundException,IOException {
        super(fname); // データが’＝’で区切られていないと全てキーとしてtblに格納される
        delm2 = dl;
        exh  = new Hashtable(100);
        // user.conf があれば以下の処理を行う
		if(tbl.size() > 0){
	        Enumeration e = tbl.keys();
    	    while(e.hasMoreElements()){
        	    String key    = (String)e.nextElement(); // ハッシュキーを取り出し
            	String data   = (String)tbl.get(key);    // 対応するデータ部を得る
	            StringTokenizer st = new StringTokenizer(data,delm2); // データ部を分解．delm2 は "%" や "," など
    	        Vector v = new Vector(10,2);
        	    while(st.hasMoreTokens()){
            	   v.add((st.nextToken()).trim()); // 両端の空白は取って，データ部をcsv形式に詰め込む
            	}
            	if(v.size() >= 2){
	                exh.put(key,v);
    	        }
        	}
		}
    }
    // 検索値のキーがあるかないか
	public boolean isExist(String key){
		Vector v = (Vector)(exh.get(key));
		if(v == null) return false;
		return true;
	}
	// 検索値が複数の値を持つとき，そのi番目の値を返す
    // iの値が不正なときは null を返す
    public String getAt(String key,int i){
        Vector v = (Vector)(exh.get(key));
        if(v != null){
            if((i >= 0)&&(i < v.size())){
                return (String)(v.get(i));
            }
        }
        return null;
    }
    // pos 番目のデータのリストを返す(nullを返したりしない)
    // ここで，キーとデータが連結される（ただのCSVデータになる）
    public Vector getListAt(int pos){
        Vector v      = new Vector(100,10);
        Enumeration e = exh.keys();
        while(e.hasMoreElements()){
            String key    = (String)e.nextElement();
            String data   = getAt(key,pos);
            if(data != null){
                v.add(data);
            }else{
                v.add(NOEXIST);
            }
        }
        return v;
    }
    public Vector getKeyList(){
        Vector v      = new Vector(100,10);
        Enumeration e = exh.keys();
        while(e.hasMoreElements()){
            v.add((String)(e.nextElement()));
        }
        return v;

    }
}
//




