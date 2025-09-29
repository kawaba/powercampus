/*
 * 作成日: 2005/05/06
 *
 */
package tktools;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;

import		framework.*;
/**
 * スレッド化したメール送信
 */
public class SendMail extends Thread {

    String	to;
    String	from;
    String	title;
    String	body;
    Param	para;
    
    public	SendMail(String to, String from, String title, String body, Param para){
        
        this.to		=	to;
        this.from	=	from;
        this.title	=	title;
        this.body	=	body;
        this.para	=	para;
        
    }
  
    public void run(){
        
       send_To_email(to,from,title,body,para);
        
    }
	public	void send_To_email(String to,String from,String title,String body,Param para){
		//
		String host	= para.getMailhost();
		//
		String sysUser	= "";
		String sysPass	= "";
		String pbs = para.popBeforSmtp();
		if(pbs.equals("yes")){
			sysUser	= para.getMailmaster();
			sysPass = para.getMailmasterPass();
			boolean ret = jmAuthenticate.authenticate(host,sysUser,sysPass);
		}
		send(host,to,from,title,body);
		//System.out.println("* end *");
	}
	//
	//	  メッセージ送信
	//
	public		void send(String host,String to,String from, String title,String msg){
		try{
	       jmSender.send(host,to,from,title,msg);
	   }catch(AddressException e2){
	   }catch(MessagingException e1){
	   }
	}	
}
