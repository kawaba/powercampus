package epml.tools;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class MyProperty {
    TreeMap<String, String> map = new TreeMap<String, String>();
    String  delm = "="; // デリミッタ

    public MyProperty(String fname){ 
        this(fname,"=");
    }
    public MyProperty(String fname, String ds) {  // デリミタ
    	
        try(var in  = new BufferedReader(new InputStreamReader(new FileInputStream(fname)))) {

        	String line;
            while((line=in.readLine())!=null){
            	
                if((line.length()>0)&&(line.charAt(0) != '#')){
                	
                    StringTokenizer st = new StringTokenizer(line,ds);
                    String key = st.nextToken();	// キー
                    
                    // データ部分を取り出す
                    String data = "";
                    while(st.hasMoreTokens()){
                        data += st.nextToken().trim();
                    }
                    map.put(key,data);
                    
                } // それ以外は読み捨て
            }        	
        }
        catch (Exception e) {
        	System.out.println(fname + " がない");
        }
    }
  
    public int size(){
    	return map.size();
    }
    
    public String get(String key){
    	return (map.get(key));
    }
    
    /**
     * プロパティを追加する
     * @param key
     * @param data
     */
    public	void	put(String key, String data){
        /* キーがnullや空白なら何もしない **/
        if((key==null)||(key.trim().length()==0)){
            return;
        }else{
            map.put(key,data);
        }
        
    }
    /**
     * mapのゲッター
     * @return　map
     */
	public TreeMap<String, String> getMap() {
		return map;
	}
	/**
	 * delmのゲッター
	 * @return delimitter
	 */
	public String getDelm() {
		return delm;
	}
	/**
	 * 全要素の表示
	 */
	public void printAllElements() {
		map.forEach((k,v)->System.out.println(k + ":" + v));
	}
	
	
    public static void main(String[] args) {
		var prop = new MyProperty("D:\\★powercampus\\pc\\system\\config\\pc.conf");
		
//		System.out.println("a=" + prop.get("a"));
//		System.out.println("b=" + prop.get("b"));
//		System.out.println("c=" + prop.get("c"));
//		System.out.println("size=" + prop.size());
		
		prop.getMap().forEach((k,v)->System.out.println(k + "\t" + v));
	}


    
}












