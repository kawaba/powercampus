/*
１つの科目の全てのレコードを集合したクラス


*/
package export;
import java.io.Serializable;
import java.util.Vector;
import kamoku.KamokuDefRecord;


public class Course extends Object implements Serializable{
/**
	 * 
	 */
	private static final long serialVersionUID = -1717363274205502099L;
	//private static final long serialVersionUID = 1L;
//
exKamoku	kamoku;
Vector		section;
Vector		reference;
Vector		kadai;
//
//
public Course(KamokuDefRecord	kdr) {
	//
	kamoku		= kdr.obj();
	section		= new Vector(30,10);
	reference	= new Vector(30,10);
	kadai		= new Vector(30,10);
	
	
	
}
//
//
public void	add_section(exSection sec){
	section.add(sec);
}
//
//
public void	add_reference(exReference ref){
	reference.add(ref);
}
//
//
public void	add_kadai(exKadai kd){
	kadai.add(kd);
}
//
//
public exKamoku	get_kamoku()		{	return	kamoku; }
//
public Vector		get_Sections()		{	return  section;}
//
public Vector		get_References()	{	return  reference;}
//
public Vector		get_Kadai()			{	return  kadai;}

}