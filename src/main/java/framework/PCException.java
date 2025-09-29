package framework;
//
// 例外クラス
// 
public class PCException extends Exception {
	//
	public static final int	Fatal				= 0;
	public static final int	EndOfBuffer			= -1;
	//
	private int		value;
	private String	message;
	//
	private static final String[] msg = {	"致命的なエラー",
											"致命的なエラー１",
											"致命的なエラー２",
											"致命的なエラー３" ,
											"致命的なエラー４",
											"致命的なエラー５"};
	//
	public PCException(int _value){
		if(_value	== EndOfBuffer){
			value 	= _value;
			message = "バッファが空である";
		}else{
			value	= _value;
			message = msg[value];
		}
	}
	public PCException(String s){
		value	= 0;
		message = s;
	}
	public PCException(int k,String s){
		value	= k;
		message = s;
	}
	//
	public String toString(){
		return	"PowerCampus Exception: " + message;
	}
	//
	public String getMessage(){
		return	"PowerCampus Exception: " + message;
		
	}
	public int getMessageValue(){
		return	value;
		
	}
	public void setMessageValue(int p){ value = p; }
}