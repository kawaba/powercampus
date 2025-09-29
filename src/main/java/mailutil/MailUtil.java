/*
     Power Campus MailUtil

*/
package mailutil;
import  tktools.*;
//
import java.io.*;
import java.util.*;
//import jakarta.mail.NoSuchProviderException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.AddressException;

import framework.LOG;
import framework.Param;
import framework.SuperPrint;
//import jakarta.mail.internet.InternetAddress;
//import jakarta.mail.internet.MimeMessage;

//
public class MailUtil extends Object {
    //
    //
   public MailUtil(){
        //
        //if(DBG._tr090) DBG.outHash(_htb,"MailUtil のコンストラクタです");
        //
    }
    //
	// pop-before-smtp 認証を確認して１件のメールを送信する
	//
	public void sendInfo(String host,String to,String from,String title,String msg,Param para,boolean email){
        // 必要なら pop before SMTP のためにシステムユーザー名で認証を受けておく
        String pop = para.popBeforSmtp();
        if(pop.equals("yes")){
			String  user    = "";                                    // POP user名
            String  passwd  = "";                                // pop パスワード
            if(email){
				user    = para.getMailmaster();                                    // POP user名
            	passwd  = para.getMailmasterPass();                                // pop パスワード
            }else{
				user    = para.getMailmaster_k();                                    // POP user名
    	        passwd  = para.getMailmasterPass_k();                                // pop パスワード
			}
			boolean auth    = jmAuthenticate.authenticate(host,user,passwd);          // チェック
            if(!auth){
                String s1 = "★pop before smtp で認証に失敗しました。UID/PASSWD を確認してください。<P>";
                String s2 = "   host=" + host + "<br>";
                String s3 = "   user=" + user + "<br>";
                String s4 = "   passwd=" + passwd + "<br>";
				String s5 = "   auth=" + auth + "<br>";
                Gear.errPrint(para.getResponseWriter(),s1 + s2 + s3 + s5);	// s4 は出さない
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
	// テンプレートにパラメータを埋め込んで送信本文を作る
	public String mktemplate(String path,Hashtable htb,Param para){
        //String template = para.stInfoTempPath();
        String 			template 	= path;							// 送信文テンプレートへのパス
		StringWriter 	strOut 		= new StringWriter(5000);
        PrintWriter 	sout     	= new PrintWriter( strOut );
        //
  		SuperPrint		sp			= new SuperPrint(sout);			// 2004.3 SuperPrint を使う方式に改めた
        sp.repPrint(template,htb,false);    // メールなので false 
        //
        String msg = strOut.toString(); 	// 編集された受領確認用送信メッセージ
		return msg;
	}
	//
	// POP before SMTP 認証を行う
	public void auth_POPbeforeSMT(Param para){
		if(LOG.fa) LOG.println("class exwork #auth_POPbeforeSMT() : POP before SMTP 認証を行う の先頭です");
		//
		String host		= para.getMailhost();
		String sysUser	= "";
		String sysPass	= "";
		String pbs = para.popBeforSmtp();
		if(pbs.equals("yes")){
			sysUser	= para.getMailmaster();
			sysPass = para.getMailmasterPass();
			boolean ret = jmAuthenticate.authenticate(host,sysUser,sysPass);
			if(ret){
				if(LOG.fa) LOG.println("           ☆☆ 認証をパスしました");
			}else{
				if(LOG.fa) LOG.println("           ★★ 認証に失敗しました");
			}
		}else{
			if(LOG.fa) LOG.println("           ☆☆ 認証は不要でした");
		}
	}
	//
	//  一通のメールを送信する
	//
	public void send_To_email(String to,String from,String title,String body,Param para){
		//
		String host	= para.getMailhost();
		if(LOG.fa){
			LOG.println("class exwork #send_To_email() :  一通のメールを送信する の先頭です");
			LOG.println("      host   = " + host);
			LOG.println("       to    = " + to);
			LOG.println("       from  = " + from);
			LOG.println("       title = " + title);
			LOG.println("       body  = " + body );
		}
		//
		String sysUser	= "";
		String sysPass	= "";
		String pbs = para.popBeforSmtp();
		if(pbs.equals("yes")){
			sysUser	= para.getMailmaster();
			sysPass = para.getMailmasterPass();
			boolean ret = jmAuthenticate.authenticate(host,sysUser,sysPass);
			if(LOG.fa) LOG.println("class exwork #send_To_email(): jmAuthenticate is " + ret);
			if(LOG.fa) LOG.println("      host is " + host);
			if(LOG.fa) LOG.println("      user is " + sysUser);
			if(LOG.fa) LOG.println("      Pass is " + sysPass);
		}
		send(host,to,from,title,body);
	}
	//一通の携帯メールを送信する
	void send_To_keitai(String to,String from,String title,String body,Param para){
		//
		String host	= para.getMailhostToKeitai();
		if(LOG.fa){
			LOG.println("class exwork #send_To_keitai() :  一通の携帯メールを送信する の先頭です");
			LOG.println("      host   = " + host);
			LOG.println("       to    = " + to);
			LOG.println("       from  = " + from);
			LOG.println("       title = " + title);
			LOG.println("       body  = " + body );
		}
		//
		String sysUser	= "";
		String sysPass	= "";
		String pbs = para.popBeforSmtp_k();
		if(pbs.equals("yes")){
			sysUser	= para.getMailmaster_k();
			sysPass = para.getMailmasterPass_k();
			boolean ret = jmAuthenticate.authenticate(host,sysUser,sysPass);
			if(LOG.fa) LOG.println("class exwork #send_To_email(): jmAuthenticate is " + ret);
			if(LOG.fa) LOG.println("      host is " + host);
			if(LOG.fa) LOG.println("      user is " + sysUser);
			if(LOG.fa) LOG.println("      Pass is " + sysPass);
		}
		send(host,to,from,title,body);
	}
	//
	//  メッセージ送信
	//
	void send(String host,String to,String from, String title,String msg){
		if(LOG.fa)  LOG.println("class stwork #send() : メッセージ送信 の先頭です");
		try{
            jmSender.send(host,to,from,title,msg);
        }catch(AddressException e2){
        }catch(MessagingException e1){
        }
	}

}

