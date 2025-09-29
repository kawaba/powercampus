package tktools;

import java.text.StringCharacterIterator;

public class IsHankaku {
	private static final String LETTERorDigit  	= ".abcdefghijklmnopqrstuvwxyz_ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
	char	notValid;
	int		pos;
	
	public	IsHankaku(){
		notValid	=	' ';
		pos			=	-1;
		
	}
	
	public	boolean	isLettorOrDigit(String test){
		
		StringCharacterIterator	sc	=	new StringCharacterIterator(test);
		int	i=0;
		for(char c = sc.first(); c != StringCharacterIterator.DONE; c = sc.next()) {
			
			if(LETTERorDigit.indexOf((int)c) == -1) {
				// 許されない文字発見
				pos			=	i;
				notValid	=	c;
		    	return	false;
		    }
			i++;
		}
		return	true;
	}
	public	char	errorChar(){
		return		notValid;
	}
	public	int		errorPos(){
		return		pos;
	}
	
	/*
	public	static	void	main(String [] arags){
		
		String		test	=	"kanji123１２３漢字_!#$%&abc￥\"";
		IsHankaku	is		=	new	IsHankaku();
		boolean	ret		=	is.isLettorOrDigit(test);
		if(!ret){
			System.out.println("error ="+ is.errorChar());
			System.out.println("pos   ="+ is.errorPos());
			
		}
	}
	*/
}