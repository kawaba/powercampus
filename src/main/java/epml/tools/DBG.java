package epml.tools;
import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/*
■デバッグ用クラス

*/
//
public class DBG extends Object{

    public final static  boolean tr		= true;	// トレースする
	public final static  boolean fa		= false;	// トレースしない
	//
	public static	PrintWriter		logs;
	public	static	boolean		log 	= false;
	
	// コンストラクタ
	public DBG (){
		
	}
    /**
     * 標準出力の切り替え
     * @param para
     */
    public static void	logOn(PrintWriter logsfp){
        logs 	=	logsfp;
        log		=	true;
    }    
    public static void	logOff(){
        log		=	false;
    }    	
	public	static	void println(String s1, String s2){
		
		if(log){
		    logs.println(s1 + ":" + s2);
		}else{
		    System.out.println(s1 + ":" + s2);
		}
	}

	public	static	void println(String s){
		
		if(log){
		    logs.println(DateGear.getShortDate() +" --- "+ s);
		}else{
		    System.out.println(DateGear.getShortDate() +" --- "+ s);
		}
	}
	public	static	void println(int s){
		
		if(log){
		    logs.println(String.valueOf(s));
		}else{
		    System.out.println(s);
		}
	}
	public	static	void print(String s){
		
		if(log){
		    logs.print(s);
		}else{
		    System.out.print(s);
		}
	}
	public	static	void print(int s){
		
		if(log){
		    logs.print(String.valueOf(s));
		}else{
		    System.out.print(s);
		}
	}	
	public static void printVector(Vector v){
		if(v.size() <= 0) {
			System.out.println("\n## ベクターは空です \n");
			if(log) logs.println("\n## ベクターは空です \n");
		}else{
			for(int i=0; i<v.size(); i++){
				System.out.println(get000type(i+1) + ": " + (String)v.get(i));
				if(log) logs.println(get000type(i+1) + ": " + (String)v.get(i));
			}
		}
	}
	//
	// Vector の内容を全て表示する
	public static void outVector(Vector v,String msg){
        if(v.size() <= 0) {
			println("\n## " + msg + ": ベクターは空です ## ----- \n");
		}else{
        	println("\n## " + msg + ": ベクターの値リストです ## ----- \n\n");
			for(int i=0; i<v.size(); i++){
				System.out.println(get000type(i+1) + ": " + (String)v.get(i));
			}
		}
        println("##----------");
    }
	//
	// Vector の内容を全て表示する
	public static void outVectorCsv(Vector v,String msg){
        if(v.size() <= 0) {
			println("\n## " + msg + ": ベクターは空です ## ----- \n");
		}else{
        	println("\n## " + msg + ": ベクターの値リストです ## ----- \n\n");
			for(int i=0; i<v.size(); i++){
				println(get000type(i+1) + ": " + ((Csv)v.get(i)).getCsvString() );
			}
		}
        println("## ----------");
    }
	// Vector の内容を全て表示する
	public static void outVector2(Vector v,String msg){
		if(v.size() <= 0) {
			println("\n## " + msg + ": ベクターは空です ## ----- \n");
		}else{
        	println("\n## " + msg + ": ベクターの値リストです ## ----- \n\n");
			for(int i=0; i<v.size(); i++){
				outVector((Vector)v.get(i),String.valueOf(v.size()) + " 個のうちの " + String.valueOf(i) + " 番目の要素ベクター");
			}
		}
        println("##----------");
    }
	//
	// Hashtable の内容を全て表示する
    public static void outHash(Hashtable htb,String msg){
        String tb = getParamList2(htb,msg);
        println(tb);
        println("#----------");
    }
	//
	// 実行継続が不能なとき、エラー画面を表示して停止する
	public static void errStop(String msg){
		DBG.errStop(new PrintWriter(System.out), msg);
	}
	public static void errStop(PrintWriter out,String msg){
		StringBuffer bf = new StringBuffer(5000);
		bf.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		bf.append("<html>");
		bf.append("<head><title>実行継続不能なエラー</title>");
		bf.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=Shift_JIS\"></head>");
		bf.append("<body>");
		bf.append("## 実行継続が不能なエラーが発生した．<p>");
		bf.append("   理由: " + msg + "<p>");
		bf.append("</body></html>");
		//
		out.println(bf.toString());
		out.close();
		return;
		//
	}
	//
	// 実行を停止したいときとき、停止画面を表示して停止する
	public static void debugStop(PrintWriter out,String msg){
		StringBuffer bf = new StringBuffer(5000);
		bf.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		bf.append("<html>");
		bf.append("<head><title>実行停止</title>");
		bf.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=Shift_JIS\"></head>");
		bf.append("<body>");
		bf.append("## 指示により実行を停止しました．<p>");
		bf.append("   理由: " + msg + "<p>");
		bf.append("</body></html>");
		//
		out.println(bf.toString());
		out.close();
		return;
		//
	}
	//
	// 実行を停止したいときとき、ハッシュの内容を表示して停止する
	public static void debugStop2(PrintWriter out,Hashtable htb,String msg){
		String data = getParamList( htb,"");
		StringBuffer bf = new StringBuffer(5000);
		bf.append("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\">");
		bf.append("<html>");
		bf.append("<head><title>実行停止</title>");
		bf.append("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=Shift_JIS\"></head>");
		bf.append("<body>");
		bf.append("## 指示により実行を停止しました．<p>");
		bf.append("   理由: " + msg + "<p>");
		bf.append(data);
		bf.append("</body></html>");
		//
		out.println(bf.toString());
		out.close();
		return;
		//
	}
	//
	// ハッシュテーブル内の全パラメータを文字列にして返す
    public static String getParamList2(Hashtable htb,String msg){
        StringBuffer bf = new StringBuffer(5000);
        //
        if(htb.size() <= 0) {
			bf.append("\n## " + msg + ": ハッシュテーブル内は空です ## ----- \n");
		}else{
        	bf.append("\n## " + msg + ": ハッシュテーブル内の変数リストです ## ----- \n\n");
			Enumeration e = htb.keys();
    	    while(e.hasMoreElements()){
            	String key    = (String)e.nextElement();
        	    String data   = "";
				
				//
				//Object obj = (Object)htb.get(key);
				//DBG.println("class DBG #getParamList2() : clssName = " + obj.getClass().getName());
				//
				Object	obj		= (Object)htb.get(key);
				String type		= obj.getClass().getName();
				if( type.equals("java.lang.String") ){
					data   = (String)htb.get(key);
				}else{
					data   = "<not String type >";
				}
            	bf.append( "(" + key + " : " + data + ")" + "\n");
        	}
        	bf.append("\n");
		}
        return bf.toString();
    }
    //
    // ハッシュテーブル内の全パラメータをHTMLにして返す
    public static String getParamList(Hashtable htb,String msg){
        StringBuffer bf = new StringBuffer(5000);
        //
        if(htb.size() <= 0) {
			bf.append("<br>## " + msg + ": ハッシュテーブル内は空です ## ----- <br>");
		}else{
        	bf.append("<br>## " + msg + ": ハッシュテーブル内の変数リストです ## ----- <p>");
			Enumeration e = htb.keys();
    	    while(e.hasMoreElements()){
            	String key    = (String)e.nextElement();
        	    String data   = (String)htb.get(key);
            	bf.append( "(" + key + " : " + data + ")" + "<br>");
        	}
        	bf.append("<p>");
		}
        return bf.toString();
    }
	// 先頭を０で埋めて 3 桁の整数にする
    public static String get000type(int s){
        String  dt      = String.valueOf(s);
        if((dt == null)||(dt.length() == 0)) return "000";
        int     pos     = dt.length();
        String  pattern = "000" + dt;
        return  pattern.substring(pos);
    }

}