package tktools;
/**
 * 電子メール送信ユーティリティ
 * 
 * 電子メールを送信するスタティックメソッド
 * 添付ファイル付きの送信も可能
 * 
 * (C)Takashi KAWABA 2000- 
 * 
 *    例示：（添付ファイル付き送信）
 *      try{
 *	       	String fullPath = zipDir + File.separator + zipFile;
 *
 *			jmSender.sendMP(host,to,from,subject,body,fullPath,zipFile);
 *	    
 *	    }catch(Exception e){
 *	        e.printStacktarce();
 *	    }finally{
 *			// ファイルは不要なので削除しておく（ディレクトリも消す）
 *			delDir(zipDir);
 *		}
 * 
 * 
 */
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.Address;
import jakarta.mail.MessagingException;
import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;

public class jmSender {
    private Session session;
    private Transport transport;
    //
    public jmSender() throws NoSuchProviderException {
        session = Session.getInstance(new Properties(), null);
        //session.setDebug(Boolean.getBoolean("mail.debug"));
        transport = session.getTransport("smtp");
    }
    // 接続
    public void connect(String host) throws MessagingException {
        transport.connect(host, -1, null, null);
    }
    // 切断
    public synchronized void disconnect() {
        try {
            transport.close();
        } catch (MessagingException e) {}
    }
    // 送信
    public void send(   String to,
                        String from,
                        String subject,
                        String body)
                        throws MessagingException, AddressException
    {
        MimeMessage msg = createMessage();
        setHeaders(msg, to, from);
        try {
            msg.setSubject(MimeUtility.encodeText(subject, "ISO-2022-JP", "B"));
        } catch (java.io.UnsupportedEncodingException e){
            // 何もしない
        }
        msg.setText(body, "ISO-2022-JP");
        send(msg);
    }
    // 送信（添付ファイル付き）
    public void sendMP( String to,
                        String from,
                        String subject,
                        String body,
                        String filepath, // ファイル名を含むフルパス（読み出し用）
                        String filename) // ファイル名のみ（添付ファイル名となる）
                        throws MessagingException, AddressException,IOException
    {
        MimeMessage msg = createMessage();
        setHeaders(msg, to, from);
        try {
            msg.setSubject(MimeUtility.encodeText(subject, "ISO-2022-JP", "B"));
        } catch (java.io.UnsupportedEncodingException e){
            // 何もしない
        }
        msg.setContent(createAttachmentPart(body,filepath,filename));
        msg.saveChanges();
        send(msg);
    }
    // 即時送信
    public static void send(    String host,
                                String to,
                                String from,
                                String subject,
                                String body)
                throws MessagingException, AddressException
    {
        jmSender s = new jmSender();
        MimeMessage msg = s.createMessage();
        s.setHeaders(msg, to, from);
        try {
            msg.setSubject( MimeUtility.encodeText(subject, "ISO-2022-JP", "B"));
        } catch (java.io.UnsupportedEncodingException e){
            // 何もしない
        }
        msg.setText(body, "ISO-2022-JP");
        s.connect(host);
        s.send(msg);
        s.disconnect();
    }
    // 即時送信（添付ファイル付き）
    public static void sendMP(  String host,
                                String to,
                                String from,
                                String subject,
                                String body,
                                String filepath, // ファイル名を含むフルパス（読み出し用）
                                String filename) // ファイル名のみ（添付ファイル名となる）
                throws MessagingException, AddressException,IOException
    {
        jmSender s = new jmSender();
        MimeMessage msg = s.createMessage();
        s.setHeaders(msg, to, from);
        try {
            msg.setSubject( MimeUtility.encodeText(subject, "ISO-2022-JP", "B"));
        } catch (java.io.UnsupportedEncodingException e){
            // 何もしない
            System.out.println("** send ERR **");
        }
        msg.setContent(s.createAttachmentPart(body,filepath,filename));
        msg.saveChanges();
        s.connect(host);
        s.send(msg);
        s.disconnect();
    }
    // ///////// その他の処理 ////////////////
    //
    // 添付ファイルの処理
    public MimeMultipart createAttachmentPart(String body,String filepath,String filename) throws MessagingException,IOException {
        //
        MimeBodyPart    textPart = new MimeBodyPart();
        textPart.setText(body,"ISO-2022-JP");
        //
        MimeBodyPart    filePart = new MimeBodyPart();
        filePart.setDataHandler(new DataHandler(new FileDataSource(filepath)));
        filePart.setFileName(filename);
        //
        MimeMultipart   mp = new MimeMultipart();
        mp.addBodyPart(textPart);
        mp.addBodyPart(filePart);
        return mp;
    }
    //
    // メッセージオブジェクトの作成
    public MimeMessage createMessage() {
        return new MimeMessage(session);
    }
    // ヘッダの付加　／送信者，受信者，メーラー
    // （注意）to = "some1@foo.com,some2@foo.com,...." のようにCSV形式で複数の送信先を設定可能
    public void setHeaders(MimeMessage msg, String to, String from) throws MessagingException, AddressException {
        msg.setFrom(""); // ★もとはnullだった。""でいいかどうか疑問
        msg.addFrom(InternetAddress.parse(from, true));
        msg.setRecipients(MimeMessage.RecipientType.TO,InternetAddress.parse(to, true));
        msg.setHeader("X-Mailer", "jmSender 1.0");
    }
    // 送信の下請けメソッド
    public void send(MimeMessage msg) throws MessagingException {
        send(msg, msg.getAllRecipients());
    }
    public synchronized void send(MimeMessage msg, Address[] envelopeTo) throws MessagingException {
        msg.setSentDate(new Date());
        // メッセージIDをつける
        session.getProperties().put("mail.from",((InternetAddress)msg.getFrom()[0]).getAddress());
        msg.saveChanges();
        //
        transport.sendMessage(msg, envelopeTo);
    }
    protected void finalize() throws Throwable {
        disconnect();
    }
}
