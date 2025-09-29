package temp;

public class Util {
    public static String trim(String s){
        String[] ss = s.split("[\\s　]");
        String ret = "";
        for(String p : ss){
            ret += p;
        }
        return ret;
    }
    
	public static void main(String[] args) {
		String s = " aaa bbb　cccc ";
		String ss = Util.trim(s);
		
		System.out.println(ss);
		

	}

}
