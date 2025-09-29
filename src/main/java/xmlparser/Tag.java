/*
 *
 */
package xmlparser;
import	java.util.*;
import	tktools.*;



/**
 * 括弧（<, </, ,> ,/>） を除いた１個のタグ文字列を受け取り、タグ名とアトリビュートの
 * Vector を作成する
 * 
 * 先頭の１wordを名前として取り出し、残りは複数のアトリビュートとする
 * ○○＝□□ という形式の各アトリビュートは（名前、値）の組に構成する
 * ただし、○○ しかないものは値を "" とする
 * 各アトリビュートは Attributeオブジェクトとして作成し、それらを Vectorに保持する
 * 
 */
public class Tag {


	final	char	SPC	=	' ';
	
	String		buffer;		// 原文
	
	Vector		attrs;
	HashMap		attrMap;
	
	int		pos;
	int		save;
	int		sz;

	/** タグ名 */
	String		name;
	
	int		status;

	/** 現在の位置 */
	int		p;
	int		max;
	
	boolean	valueFlag;
	
	public	Tag	(){
		attrs	=	new Vector(20);
		attrMap	=	new	HashMap(20);
		pos		=	0;		
		
		name	=	"";
		buffer	=	"";
		p		=	0;
		max		=	0;
		status	=	0;
	}
	public	Tag	(String	buffer){
		attrs	=	new Vector(20);
		attrMap	=	new	HashMap(20);
		pos		=	0;

		name			=	"";
		this.buffer	=	buffer;
		p		=	0;
		max		=	buffer.length();
		status	=	0;

		/* タグ名を取り出して、それ以外をアトリビュートとする */
		parse();
		if(attrs.size()>0){
			name	=	((Attribute)(attrs.get(0))).getName();
			attrs.removeElementAt(0);
		}
		sz	=	attrs.size();
		pos	=	0;
		/*
		 * アトリビュートのマップを作成する
		 */
		for(int k=0; k<sz; k++){
			Attribute	atr	=	(Attribute)attrs.get(k);
			attrMap.put(atr.getName(), atr.getValue());
		}
		
	}
	/**
	 * アトリビュート名で値を引いて返す
	 * @param key	アトリビュート名
	 * @return		値（値がない場合は null ではなく"" を返す）
	 */
	public	String	get(String key){
		
		if(attrMap.isEmpty())	return	null;
		//
		String	value	=	(String)attrMap.get(key);
		if(Gear.isEmpty(value)){
			value	=	"";
		}
		return	value;
	}
	
	public	void	reset(){
		pos	=	0;
	}
	
	public	int	size(){
		return	sz;
	}
	/**
	 * タグをアトリビュートに分解してそのベクターを作成する
	 * 
	 * @param s	ひとつのタグ
	 */
	void	parse(){
		
		String		name	=	"";
		String		value	=	"";
		char		c;
		while(true){
			name	=	"";
			value	=	"";

			/* (1) アトリビュート名を採取する */
			String	dt	=	nextWord();
			if(isEmpty(dt)){
				/* 
				 * データがなかった
				 * データエンドなので終了する．
				 */
				return;
			}else{
				if(dt.equals("=")){
					/* エラーデータなので無視する */
					continue;
				}
				name	=	dt;
			}
			/* 
			 * アトリビュート値がなく次の名前を読み込んでしまったとき
			 * それをバッファに戻せるよう位置を記憶しておく
			 */
			push();	
			
			
			/* (2) ＝を採取する */
			dt	=	nextWord();
			if(isEmpty(dt)){
				/* 
				 * = がないのでアトリビュート名だけだった．
				 * value は空白のままオブジェクトを作成.
				 * データエンドなので終了する．
				 */
				attrs.add(new Attribute(name,value));
				return;
			
			}else if(!dt.equals("=")){
				/* 
				 * 次のアトリビュート名が出現したので
				 * このワードをバッファに戻し、value は空白のままオブジェクトを作成
				 * 先頭から処理を再開する
				 */	
				pop();
				attrs.add(new Attribute(name,value));
				continue;
		
			}
			
			/* (3) アトリビュート値を採取する */
			dt	=	nextWord();
			if(isEmpty(dt)){
				/*
				 * = はあったがアトリビュート値が書かれてなかった				 
				 * データエンドなので終了する．
				 */
				attrs.add(new Attribute(name,""));
				return;
			}else{
				value	=	dt;
				attrs.add(new Attribute(name,value));
			}
		}
	}

	void	push(){
		save	=	p;
	}

	void	pop(){
		p	=	save;
	}
	

	String	nextWord(){
		StringBuffer	bf	=	new StringBuffer();
		String			value;
		char	c;
		
		skipSpace();
		if((c=nextTextChar())==0){
			return	null;
		}		
		
		/* 先頭が＝だったらそれを返す */
		if(c=='='){
			return	"=";
		}
		p--;
		/*
		 * 先頭が文字の場合は空白か＝が出現するまで文字を採取する
		 * ＝が出現したらそれは採取文字から除外して採取を終了する
		 */
		while((c=nextTextChar())!=0){

			if(c=='='){
				--p;
				break;
			}else if(c==SPC){// 半角スペース
				break;
			}else{
				bf.append(c);
			}
		}	
		
		if(bf.length()>0){
			return	bf.toString();
		}
		/* これは発生しないが念のため */
		return	null;
	}


	/** タグの名前を返す */
	public	String	getName(){
		return	name;
	}
	/**
	 * アトリビュートをひとつ取り出す
	 * 
	 * @return		アトリビュートオブジェクト．データエンドなら null を返す
	 */
	public	Attribute	next(){
		
		Attribute	a	=	null;
		if(pos<sz){
			a	=	(Attribute)attrs.get(pos);
			pos++;
		}
		return	a;
	}

	/**
	 * バッファから制御文字でない文字を１文字取って返す．
	 * 制御文字は無視される
	 */
	/**
	 * 空白・改行をスキップする
	 */
	void	skipSpace(){
		/* nextTextChar() はコントロール文字をひとつの空白として読み込む */
		char	c;
		while ((c=nextTextChar())!=0){
			if(!isSpace(c)){
				putBackChar();
				return;
			}
		}
	}
	/**
	 * 改行文字が現れるまでのデータをスキップする
	 * 改行文字の直前までポインタを進めて返る
	 */
	void	skipComment(){
		/* nextChar() はバッファからそのままの文字を読み込む */
		char	c;
		while((c=nextChar())!=0){
			if(isCR(c)){
				putBackChar();
				return;
			}
		}
	}
	/**
	 *  空白文字かどうかのチェック
	 * @param c
	 * @return
	 */
	boolean	isSpace(char c){
		if(c==' ')			return	true;
		if(c=='　')		return	true;
		return false;
	}	
	/**
	 * 改行文字かどうかの判断
	 * @param c	文字
	 * @return		改行文字(\n または \r)なら true
	 */
	boolean	isCR(char	c){
		if(Character.isISOControl(c)){
			if((c=='\n')||(c=='\r')){
				return	true;	
			}
		}
		return	false;
	}
	/**
	 * バッファが空かどうか
	 * @return
	 */
	boolean	EOB(){
		return (p >= max);	// 最後のとき true
	}
	/**
	 * 文字列が空かどうかテストする
	 * @param str
	 * @return
	 */
	boolean isEmpty(String str){

		if(str==null) 			return  true;

		str	=	str.trim();
		if(str.length()==0)	return  true;
		return false;
	}
	/**
	 * バッファから１文字取って返す．ポインタは＋１される
	 * コントロール文字は一文字の空白として返す
	 * @return		取り出した文字
	 */
	char	nextTextChar(){
		char	c;
		c = nextChar();
		if(c==0)	return	c;
		
		/* 制御文字は空白文字として返す */
		if(Character.isISOControl(c)){
			c = SPC;	
		}
		return	c;
	}
	/**
	 * バッファから１文字取って返す．ポインタは＋１される
	 * @return		取り出した文字
	 */
	char nextChar(){

		char	c = 0;
		if( !EOB() ){
			c = buffer.charAt(p);
			p++;
		}else{
			c = 0;	// バッファが空
			return	 c;	
		}
		return	rglToHan(c);// ( <,=,/,> ) を半角にそろえる
	}
	/**
	 *  <,=,/,> を半角にそろえる
	 */
	char	rglToHan(char c){
		if((c=='<')||(c=='＜'))		return	'<';
		if((c=='=')||(c=='＝'))		return	'=';
		if((c=='>')||(c=='＞'))		return	'>';
		if((c=='/')||(c=='／'))		return	'/';
		return	c;
	}
	/**
	 * バッファに１文字戻す
	 *
	 */	
	void	putBackChar(){
		--p;
	}


	/* テスト用 */
	
	
	public	static	void	main(String []arg){
		
		String	data	=	"  dispatch  key     =       html    =    number=3600 ";
		Tag		tg	=	new Tag(data);
		//
		System.out.println("Tag name:" + tg.getName());
		Attribute	at;
		for(int i=0; i<tg.size(); i++){
			at	=	tg.next();
			System.out.println(":" + at.getName() + ":" + at.getValue() + ":");			
		}
	}


}
