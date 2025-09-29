package stkadai;

import framework.*;
import kadai.*;
//
/**
 * タイピング試験の開始画面
 * 
 * stkadai.StKadaiWriting を実行するようにStKadaiStartExam をオーバーライド
 * 
 *
	#
	# #######################
	#     StKadaiStartExam
	# #######################
	#
	<program $stkadai.StKadaiStartWriting>
		<dispatch  html=StKadaiStartExam.html  number=2515  class=stkadai.StKadaiStartWriting />
		<variable>
		  <receive   NUMBER STAMP GROUP StUID TUID aplec_key lec_key title kadai_key/>
		  <accept    CMD    UPLODE  />
		  <keep      clock />
		  
		  <form      />
		</variable>
	</program> 
*
*
* 変数の説明
*
* 1. receive 
* 2. accept
* 3. keep
* 		clock ---- 開始時のタイムスタンプ
* 4. form
*
* 
*/
public class StKadaiStartWriting extends StKadaiStartExam {

	
	public	StKadaiStartWriting(){
		super();
		if(LOG.fa) LOG.println("■ StKadaiStartWriting #コンストラクタ");
		
	}
	public	String	dispatch(){
		if(LOG.fa) LOG.println("■StKadaiStartWriting #dispatch()");
		//
		cmd			=	getParameter(CMD);	// null の場合は ""を返す
		ret			=	DISPATCH_DEFAULT;
		disp_mode	=	DISP_NEW;
		
		if( cmd.equals("SEND")){

			KadaiApRecord	kar	= new KadaiApRecord(te_aplec_key,kadai_key,db);		// 課題実施レコード
			/*
			 * 実行可能かチェックしてから開始する
			 */
			boolean		ok	=	checkAll(kar);
			if(ok){
				/*
				 * 開始時刻を システムハッシュ に書き込んでおいてから開始する
				 * また，ＨＯＬＤと開始時刻をKadaiInfoにも書き込む
				 */
				String	stamp	=	setClock();
				setHoldRecord(stamp);
				
				disp_mode		=	DISP_NEW;
				ret				=	"$stkadai.StKadaiWriting";
				
			}else{
				disp_mode	=	DISP_EDIT;
				ret			=	DISPATCH_DEFAULT;					
			}
			
		}else if(cmd.equals("CANCEL")){
			/*
			 * 取り消し
			 */	
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_RETURN;
		
		}else{
			disp_mode	=	DISP_EDIT;
			ret			=	DISPATCH_DEFAULT;

		}
		/*
		 * 次に表示するときあるいはリターンしてきた時の表示モードをセットしてからリターンする
		 */
		putParameter(DISP_KEY,disp_mode);
		return	ret;		
	}

}
