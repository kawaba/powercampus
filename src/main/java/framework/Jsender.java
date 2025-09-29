/*
 * 作成日: 2005/02/03
 *
 * TODO
 */
package framework;

import tktools.*;

/**
 *	pop-befor-smtp かどうかを調べて、必要なら認証を受けて
 *  メールを送信する
 */
public class Jsender {

	Param		para;
	boolean	email;
	
	public	Jsender(Param	para){
		this.para	=	para;
		email		=	true;
	}
	
	public	void	setEmail(){
		email	=	true;
	}
	public	void	setKeitai(){
		email	=	false;
	}
	
	//
	//	 pop-before-smtp 認証を確認して１件のメールを送信する
	public void send(String host,String to,String from,String title,String msg){
		// 必要なら pop before SMTP のためにシステムユーザー名で認証を受けておく
		String pop = "";
		if(email){
			pop = para.popBeforSmtp();		// pop 認証がいるかどうか
		}else{
			pop = para.popBeforSmtp_k();
		}
		//
		if(pop.equals("yes")){
			String  user    = "";   // POP user名
			String  passwd  = "";   // pop パスワード
			if(email){
				user    = para.getMailmaster();             // POP user名
				passwd  = para.getMailmasterPass();         // pop パスワード
			}else{
				user    = para.getMailmaster_k();           // POP user名
		        passwd  = para.getMailmasterPass_k();       // pop パスワード
			}
			boolean auth    = jmAuthenticate.authenticate(host,user,passwd);          // チェック
			if(!auth){
				String s1 = "★pop before smtp で認証に失敗しました。UID/PASSWD を確認してください。" + Gear.CR;
				String s2 = "   host=" + host + Gear.CR;
				String s3 = "   user=" + user + Gear.CR;
				String s4 = "   passwd=" + passwd + Gear.CR;
				System.out.println(s1 + s2 + s3 + s4);
				// 送信しない
				return;
			}
		}
		try{
			jmSender.send(host,to,from,title,msg);
		}catch(Exception e){
			System.out.println(to + "へのメール送信に失敗しました (" + Gear.getDate() + "）");
			System.out.println( e );
		}
	}	
}
