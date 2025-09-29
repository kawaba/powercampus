/*
 * 作成日: 2004/07/19
 *
 * 
 * 
 */
package tktools;

public class tkException extends Exception {
	//
	//
	private int		value;
	private String		message;
	//
	//
	public tkException(String s){
		value	= -1;
		message = s;
	}

	public tkException(int k,String s){
		value	= k;
		message = s;
	}
	//
	public String toString(){
		return	getMessage();
	}
	//
	public String getMessage(){
		//StringWriter	sw	=	new StringWriter();
		//PrintWriter		out	=	new	PrintWriter(sw);
		//printStackTrace(out);
		//String	trace		=	sw.toString();
		//String	CR			=	System.getProperty("line.separator");
		return	 message ;
	}
	public	int	getValue(){
			return	value; 
	}
}
