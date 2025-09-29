/*
 *
 */
package tktools;

import	java.util.*;
/**
 * 
 */
public class Stack extends Object{
	
	Vector	stk;
	int	max;
	
	boolean	vartual;

	public	Stack(){
		stk		=	new Vector();
		max		=	0;
	}
	public	Stack(int	n){
		stk		=	new Vector(n);
		max		=	0;
	}	
	public	int		size(){
		
		return	stk.size();
	}
	
	public	boolean	empty(){
		if(max > 0)	return	false;
		return		true;
	}

	public	Object	push(Object obj){
		stk.add(obj);
		max++;
		return	obj;
		
	}
	public	Object	pop(){
		Object	obj	=	null;
		if(max > 0){
			max--;
			obj	=	stk.get(max);
			stk.removeElementAt(max);
			return	obj;
		}
		return	null;
	}

	/* 
	 * 末尾（スタックトップ）のオブジェクトを返す
	 * スタックの要素は削除しない
	 */
	public	Object	lastElement(){
		Object	obj	=	null;
		if(max > 0){
			obj	=	stk.get(max-1);
			return	obj;
		}
		return	null;
	}

	public	Enumeration	elements(){
		return	stk.elements();	
	}

	public	static void	main(String [] args){
		
		Stack	st	=	new	Stack();
		st.push("aaa"); 
		st.push("bbbb"); 
		st.push("ccccc"); 
		//
		System.out.println("size=" + st.size());
		Enumeration en = st.elements();
		while(en.hasMoreElements()){
			System.out.println(en.nextElement());	
			
		}
		System.out.println("size=" + st.size());
		while(!st.empty()){
			System.out.println(st.pop());	
			
		}
		System.out.println("size=" + st.size());
	}


}
