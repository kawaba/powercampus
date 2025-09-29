/*
 *
 */
package xmlparser;

/**
 * タグのひとつのアトリビュートを保持するクラス<br>
 * 
 * ○○＝□□ という形式の各アトリビュートは（名前、値）の組に構成する
 * ただし、○○ しかないものは値を "" とする
 * 
 */
public class Attribute extends	Object{
		
	String	name;
	String	value;
	
	public	Attribute(){
		name	=	"";
		value	=	"";
	}
	
	public	Attribute(String name, String value){
		this.name	=	name;
		this.value	=	value;
	}
	
	public	String	getName(){
		return	name;
	}
	
	public	String	getValue(){
		return	value;
	}
		
}
