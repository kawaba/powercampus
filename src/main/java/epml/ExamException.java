package epml;
//import	epml.tools.*;

// パーサーでの例外クラス
// 
public class ExamException extends Exception {
	//
	public static final int	Fatal				= 0;
	
	//
	public static final int	EndOfBuffer			= -1;
	//

	//
	private static final String[] msg = {	"",
											""
										 };
	//
	private int		value;
	private String	message;
	//
	public ExamException(int k,String s){
		value	= k;
		message = s;
	}
	public ExamException(int _value){
		if(_value	== EndOfBuffer){
			value 	= _value;
			message = "バッファが空である";
		}else{
			value	= _value;
			message = msg[value];
		}
	}
	//
	public ExamException(String s){
		value	= 0;
		message = s;
	}
	public ExamException(int _value,int v){
		value	= _value;
		message = msg[value] + " (" + v + ")";
	}
	//
	public String toString(){
		return	"ExamException: " + message;
	}
	//
	public String getMessage(){
		return	"ExamException: " + message;
		
	}
	public String getNontitleMessage(){
		return	 "▼" + message;
		
	}
	public int getMessageValue(){
		return	value;
		
	}
	
}