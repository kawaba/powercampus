/*
    ★CSV形式の文字列データを扱うラッパークラス 
*/
package tktools;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.Vector;
//import java.lang.Exception;
//
public class Csv {
    String  dlmt;
    int    count;
    String str;
    Vector v;
    //
    // Vectorから生成する
    public Csv(Vector w) {
       	v     = new Vector(30,10);
       	dlmt  = ","; //CSVデリミッタ（普通はコンマ）
       	count = 0;
		str	  = "";
		//
		if((w!=null)&&(w.size()>0)){
	        for(int k=0; k<w.size(); k++){
    	        if(w.get(k) == null) w.set(k,new String(" "));
        	    String s = ((String)w.get(k)).replace(',',' '); // 文字列中のコンマは空白にする
        	}
        	v = w;
        	count = v.size();
        	dlmt  = ",";
        	//
        	boolean fl = false;
	        StringBuffer bf = new StringBuffer(5000);
    	    for(int i=0; i<count; i++){
        	    if(fl){ bf.append(","); }
            	String s = ((String)v.get(i)).trim();
	            if(s.length()!=0){
    	            bf.append(s);
        	    }else{
                	bf.append(" ");
	            }
    	        fl = true;
        	}
        	str = bf.toString();
        }
    }
    //
    public Csv(String s) {
       	v = new Vector(30,10);
       	dlmt = ","; //CSVデリミッタ（普通はコンマ）
       	count = 0;
		//
        if(isEmpty(s)){
			str="";
		}else{
        	str = fairingCSV(s);
        	StringTokenizer st = new StringTokenizer(str,dlmt);
        	while(st.hasMoreElements()){
            	v.add((st.nextToken()).trim()); // 両端の空白は取る
            	count++;
        	}
		}
    }
    public Csv(String s,String dlm) {
       	v = new Vector(30,10);
       	dlmt = dlm; //CSVデリミッタ（普通はコンマ）
       	count = 0;
		//
        if(isEmpty(s)){
			str="";
		}else{
        	str = fairingCSV(s);
        	StringTokenizer st = new StringTokenizer(str,dlmt);
        	while(st.hasMoreElements()){
            	v.add((st.nextToken()).trim()); // 両端の空白は取る
            	count++;
        	}
		}
    }
    //
	//  sw = true なら 長さゼロの要素は無視する
	//
	public Csv(String s,String dlm,boolean sw) {
       	v = new Vector(30,10);
       	dlmt = dlm; //CSVデリミッタ（普通はコンマ）
       	count = 0;
		//
        if(isEmpty(s)){
			str="";
		}else{
        	str = fairingCSV(s);
        	StringTokenizer st = new StringTokenizer(str,dlmt);
        	while(st.hasMoreElements()){
            	String	temp = (st.nextToken()).trim();
				if(sw){
					if( (temp!=null)&&(temp.length()>0) ){
						v.add(temp);
            			count++;
					}
				}else{
					v.add(temp); // 両端の空白は取る
            		count++;
				}
        	}
		}
    }
	// 要素の数をｋ個に指定して生成する
	// 多すぎる場合は捨て、少ないと  "-" で補う
	public Csv(String s,String dlm,int k) {
       	v = new Vector(30,10);
       	dlmt = dlm; //CSVデリミッタ（普通はコンマ）
       	count = 0;
		//
        if(isEmpty(s)){
			str="";
		}else{
        	str = fairingCSV(s);
        	StringTokenizer st = new StringTokenizer(str,dlmt);
        	while(st.hasMoreElements()){
            	v.add((st.nextToken()).trim()); // 両端の空白は取る
            	count++;
				if(count == k) break; // k 要素以上はとらない 2003.8.10
        	}
			if(count < k){
    	        for( ; count < k; count++){
					v.add("-");
            	}
        	}
		}
    }
    //
	// 要素の数をｋ個に指定して生成する
	// 多すぎる場合は捨て、少ないと filler の該当位置の文字列、または "-" で補う
	//
	public Csv(String s,String dlm,int k, String [] filler) {
       	v = new Vector(30,10);
       	dlmt = dlm; //CSVデリミッタ（普通はコンマ）
       	count = 0;
		//
        if(isEmpty(s)){
			str="";
		}else{
        	str = fairingCSV(s);
        	StringTokenizer st = new StringTokenizer(str,dlmt);
        	while(st.hasMoreElements()){
            	v.add((st.nextToken()).trim()); // 両端の空白は取る
            	count++;
				if(count == k) break; // k 要素以上はとらない 2003.8.10
        	}
			if(count < k){
    	        for( ; count < k; count++){
					if(filler!=null){
						v.add(filler[count]);
					}else{
						v.add("-");
					}
            	}
        	}
		}
    }
    /**
     * 指定された要素を取り除く
     */
	public	void	deleteElement(String key){
		Vector			v2	=	new Vector(30);
		StringBuffer	bf	=	new StringBuffer();
		boolean		cm	=	false;
		for(int i=0; i<v.size(); i++){
			String	elm	=	(String)v.get(i);
			if(!elm.equals(key)){
				if(cm)	bf.append(dlmt);
				bf.append(elm);
				v2.add(elm);
			}
			cm	=	true;
		}
		v 		=	v2;
		count 	=	v.size();
		str		=	"";
		if(count>0) str =	bf.toString();
		
	}
	
	//
	// 長さがゼロでない要素の個数を返す
	public int	notZeroLengthCount(){
		int cn	= 0;
		for(int i=0; i<count; i++){
			if(!isEmpty(get(i))) cn++;
		}
		return	cn;
	}
	//
	public boolean	isEmpty(String s){
		if((s==null) || (s.length()==0))	return true;
		return false;
	}
	public	boolean isEmptyElement(int i){
		if(isEmpty(get(i)))	return	true;
		return	false;
	}
	//
	//  二つのcsvが同じかどうかチェックする
	//
	public boolean equals(Csv cs){
		String s1 = cs.getCsvString();
		return str.equals(s1);
	}
    public Csv copy(){
        String s = new String(str);
        return new Csv(s);
    }
    // 第ｉ番目の要素を返す
    public String get(int i) {
        if(count==0)	return	"-";
        if((i >= count)||(i < 0)){
            return "-"; // null は返さない
        }
        return (String)v.get(i);
    }
    // 第ｉ番目の要素を返す
    public String getElement(int i) {
		if(count==0)	return	null;
        if((i >= count)||(i < 0)){
            return null; // nullを返す
        }
        return (String)v.get(i);
    }
    //
	// ベクター要素を連結したCSV文字列の長さを返す
    public int length(){ return str.length(); }
	//
	// CSV　文字列を返す
	public String getCsvString() {return str;}
    //
	// vectorにした全リストを返す
    public Vector getItems()	{ return v; }
	// 文字列を要素とするVectorを返す
	public Vector toVector()	{ return v; }
	//
    //
    // 要素の個数を返す
    public int size() {return count;}
    //
    // １次元ベクトルをＣＳＶ形式で出力する
    public String toCSV(){
        return toCSV(dlmt);
    }
    //
    public String toCSV(String dl){
        String csv = new String("");
        boolean flag = false;
        for (Enumeration e = v.elements() ; e.hasMoreElements() ;) {
            if(flag){
                csv = csv + dlmt;
            }
            String s = (String)(e.nextElement());
            if(s != null){
                csv = csv + s;
            }else{
                csv = csv + " ";
            }
            flag = true;
        }
        return csv;
    }
    //csv形式のデータで、連続する,(コンマ)の間にスペースを挿入する
    //先頭か末尾に,(コンマ)があれば、各々、先頭、末尾にスペースを挿入する
    public String fairingCSV(String ml){
        String space = " ";
        StringBuffer sbf = new StringBuffer(2000);//１行は1000文字以内
        //
        String s = ml.substring(0,1);
        if(s.equals(dlmt)){ sbf.append(space);}//行先頭の,の処理=>空白文字を置く
        boolean flag = false;
        //
        for(int i=0; i<ml.length(); i++){
            String ch =  ml.substring(i,i+1);
            if(ch.equals(dlmt)){
                if(flag){//直前に,をappendした
                    sbf.append(space); // 空白文字
                    sbf.append(ch);
                }else{
                    sbf.append(ch);
                    flag = true;
                }
            }else{
                flag = false;
                sbf.append(ch);
            }
        }
        if(flag) sbf.append(space); // 最後が,だった場合空白文字を置く
        String  str = sbf.toString();// 整形後の行データ
        return  str;
    }
 
}