/*
 * 作成日: 2004/07/19
 *
 * 
 * 
 */
package xmlparser;

public class xmlException extends Exception {
	//
	//
	private int		value;
	private String		message;
	//
	//
	public xmlException(String s){
		value	= -1;
		message = s;
	}

	public xmlException(int k,String s){
		value	= k;
		message = s;
	}
	//
	public String toString(){
		return	getMessage();
	}
	//
	public String getMessage(){
		return	"xmlException [" + value + "]: " + message;
	}
	public	int	getValue(){
			return	value; 
	}
}
