

// パーサーでの例外クラス
// 
package _epml;
//import	epml.tools.*;
public class EpmlTokenException extends Exception {
	//
	public static final int	Fatal				= 0;
	
	public static final int	EndOfBuffer_SHP		= 2;
	public static final int	IllegalWord_SHP		= 3;
	public static final int	EndOfBuffer_RBL		= 4;
	public static final int	EmptyString			= 5;
	//
	public static final int	EndOfBuffer			= -1;
	//
	private static final String[] msg = {	"",
											"",
											"# 記号に続く記号がない",
											"# 記号に続く記号が [ 又は { でない" ,
											"[ 記号に続く文字を検査しようとしたがバッファエンドになった",
											"文字列が空である"};
	//
	private int		value;
	private String	message;
	//
	
	public EpmlTokenException(int _value){
		if(_value	== EndOfBuffer){
			value 	= _value;
			message = "バッファが空である";
		}else{
			value	= _value;
			message = msg[value];
		}
	}
	public EpmlTokenException(String s){
		value	= 0;
		message = s;
	}
	public EpmlTokenException(int k,String s){
		value	= k;
		message = s;
	}
	
	//
	public String toString(){
		return	"TokenException: " + message;
	}
	//
	public String getMessage(){
		return	"TokenException: " + message;
		
	}
	public String getNontitleMessage(){
		return	"▼" + message;
	}
	public int getMessageValue(){
		return	value;
		
	}
	public void setMessageValue(int p){ value = p; }
}