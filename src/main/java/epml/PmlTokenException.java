

// パーサーでの例外クラス
// 
package epml;
//import	epml.tools.*;
public class PmlTokenException extends Exception {
	//
	public static final int	Fatal				= 0;
	
	public static final int	NoLeftBlancket		= 2;
	public static final int	NoRightBlancket	= 3;
	public static final int	IllegalCharactor	= 4;
	public static final int	NoParam				= 5;
	public static final int	IllegalParams		= 6;
	public static final int	IllegalParameter	= 7;
	public static final int	IllegalEditor		= 8;
	public static final int	IllegalStyle		= 9;
	public static final int	Illegallocation		= 10;
	//
	public static final int	EndOfBuffer			= -1;
	//

	//
	private static final String[] msg = {	"",
											"",
											"開始括弧がない",
											"閉じ括弧がない",
											"$ 記号に続く文字が正しくない",
											"（）内にパラメータがひとつも指定されていない",
											"指定したパラメータの個数が正しくない",
											"指定したパラメータの値が正しくない",
											"編集項がフォーマット指示項をまたいで指定されている",
											"スタイル指示語が不正である",
											"フォーマットロケーション指示語が不正である"
										 };
	//
	private int		value;
	private String	message;
	//
	public PmlTokenException(int _value){
		if(_value	== EndOfBuffer){
			value 	= _value;
			message = "バッファが空である";
		}else{
			value	= _value;
			message = msg[value];
		}
	}
	//
	public PmlTokenException(String s){
		value	= 0;
		message = s;
	}
	public PmlTokenException(int _value,int v){
		value	= _value;
		message = msg[value] + " (" + v + ")";
	}
	public PmlTokenException(int k,String s){
		value	= k;
		message = s;
	}
	//
	@Override
	public String toString(){
		return	"WikiTokenException: " + message;
	}
	//
	@Override
	public String getMessage(){
		return	"WikiTokenException: " + message;
		
	}
	public String getNontitleMessage(){
		return	 "▼ " + message;
		
	}
	public int getMessageValue(){
		return	value;
		
	}
	
	public String getMessageString(){
		return	message;
		
	}
}