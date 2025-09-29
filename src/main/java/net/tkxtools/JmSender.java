/**
 * 電子メール送信ライブラリ 
 * Copyright (c) 2015 Takashi Kawaba Released under the MIT license
 * https://opensource.org/licenses/MIT
 */
package net.tkxtools;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.activation.DataHandler;
import jakarta.activation.FileDataSource;
import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Part;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.ContentDisposition;
import jakarta.mail.internet.ContentType;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.internet.MimeUtility;

public class JmSender {

	private Session session;
	private Transport transport;
	private AuthenticatorUtil authutil;

	/**
	 * SmtpAuth対応のコンストラクタ
	 * 
	 * @param smtpUser
	 * @param smtpPassword
	 * @throws NoSuchProviderException
	 */
	public JmSender(String smtpUser, String smtpPassword) throws NoSuchProviderException {
		if((smtpUser == null) || (smtpPassword == null)) {
			Logger.getLogger(JmSender.class.getName()).log(Level.SEVERE, null, "ユーザー情報がない");

		} else {
			/* smtpAuthのためのsessionを作成する */
			Properties prop = new Properties();
			prop.put("mail.smtp.auth", "true");
			prop.put("mail.smtp.starttls.enable", "true");

			authutil = new AuthenticatorUtil(smtpUser, smtpPassword);

			session = Session.getInstance(prop, authutil);
		}
		transport = session.getTransport("smtp");
		/* try {
		 * transport = session.getTransport("smtp");
		 * } catch (NoSuchProviderException ex) {
		 * Logger.getLogger(JmSender.class.getName()).log(Level.SEVERE, ex.toString());
		 * } */
	}

	/**
	 * pop-before-smtp対応のコンストラクタ 事前に、JmAuthenticatorでサーバーと接続しておく
	 * 
	 * @throws NoSuchProviderException
	 */
	/* public JmSender() throws NoSuchProviderException {
	 * session = Session.getInstance(new Properties(), null);
	 * transport = session.getTransport("smtp");
	 * } */
	/**
	 * 接続
	 *
	 * @param host
	 * @throws MessagingException
	 */
	public void connect(String host) throws MessagingException {
		connect(host, -1);
	}

	/**
	 * 接続 (ポートも指定） SMTP_AUTH の場合は、session からuser, passwordが自動的に
	 * 採取されるので引数の最後の2つはnullのままでよい
	 *
	 * @param host
	 *            メールサーバー
	 * @param port
	 *            接続するポート番号
	 * @throws MessagingException
	 */
	public void connect(String host, int port) throws MessagingException {
		transport.connect(host, port, null, null);
		/* try {
		 * transport.connect(host, port, null, null);
		 * } catch (MessagingException ex) {
		 * Logger.getLogger(JmSender.class.getName()).log(Level.SEVERE, ex.toString());
		 * } */
	}

	/**
	 * 切断
	 * 
	 * @throws MessagingException
	 *
	 */
	public synchronized void disconnect() {
		//transport.close();
		try {
			transport.close();
		} catch (MessagingException e) {

		}
	}

	/**
	 * 平文メール送信処理
	 *
	 * @param to
	 * @param from
	 * @param subject
	 * @param body
	 */
	public void send(String to, String from, String subject, String body) throws UnsupportedEncodingException, MessagingException {
		send(to, from, subject, body, "TO");
	}

	/**
	 * 平文メール送信処理
	 * 
	 * @param to
	 * @param from
	 * @param subject
	 * @param body
	 * @param type
	 * @throws MessagingException
	 * @throws UnsupportedEncodingException
	 */
	public void send(String to, String from, String subject, String body, String type) throws UnsupportedEncodingException, MessagingException

	{
		MimeMessage msg = createMessage();
		msg.setSubject(MimeUtility.encodeText(Win2Jis(subject), "iso-2022-jp", "B"));

		String mailbody = body + " ";// ? が付くのをフィックスした
		msg.setText(Win2Jis(mailbody), "iso-2022-jp");
		setHeaders(msg, to, from, type);
		send(msg);
		/* try {
		 * MimeMessage msg = createMessage();
		 * msg.setSubject(MimeUtility.encodeText(Win2Jis(subject), "iso-2022-jp", "B"));
		 * 
		 * String mailbody = body + " ";// ? が付くのをフィックスした
		 * msg.setText(Win2Jis(mailbody), "iso-2022-jp");
		 * setHeaders(msg, to, from, type);
		 * send(msg);
		 * } catch (UnsupportedEncodingException | MessagingException ex) {
		 * Logger.getLogger(JmSender.class.getName()).log(Level.SEVERE, ex.toString());
		 * } */
	}

	/**
	 * 複数の添付ファイル付き平文メール
	 *
	 * @param to
	 *            // 送信先メールアドレス
	 * @param from
	 *            // 送信元メールアドレス
	 * @param subject
	 *            // メールタイトル
	 * @param body
	 *            // メール本文
	 * @param fileDir
	 *            // 添付ファイルのあるディレクトリ
	 * @param flist
	 *            // ファイル名のみのリスト（添付ファイル名となる）
	 */
	public void send(String to, String from, String subject, String body, String fileDir, List<String> flist)
			throws MessagingException, IOException {
		send(to, from, subject, body, fileDir, flist, "TO");
	}

	/**
	 * 複数の添付ファイル付き平文メール
	 * 
	 * @param to
	 * @param from
	 * @param subject
	 * @param body
	 * @param fileDir
	 * @param flist
	 * @param type
	 * @throws MessagingException
	 * @throws IOException
	 */
	public void send(String to, String from, String subject, String body, String fileDir, List<String> flist, String type)
			throws MessagingException, IOException {
		MimeMessage msg = createMessage();
		setHeaders(msg, to, from, type);
		msg.setSubject(MimeUtility.encodeText(Win2Jis(subject), "ISO-2022-JP", "B"));
		body += " ";// ? が付くのをフィックスした
		msg.setContent(createAttachmentParts(Win2Jis(body), fileDir, flist));
		msg.saveChanges();
		send(msg);
		/* try {
		 * MimeMessage msg = createMessage();
		 * setHeaders(msg, to, from, type);
		 * msg.setSubject(MimeUtility.encodeText(Win2Jis(subject), "ISO-2022-JP", "B"));
		 * body += " ";// ? が付くのをフィックスした
		 * msg.setContent(createAttachmentParts(Win2Jis(body), fileDir, flist));
		 * msg.saveChanges();
		 * send(msg);
		 * } catch (MessagingException | IOException ex) {
		 * Logger.getLogger(JmSender.class.getName()).log(Level.SEVERE, ex.toString());
		 * } */
	}

	/**
	 * HTMLメール送信処理
	 * 
	 * @param to
	 * @param from
	 * @param subject
	 * @param body
	 */
	public void sendHtml(String to, String from, String subject, String body)
			throws AddressException, MessagingException, UnsupportedEncodingException {
		sendHtml(to, from, subject, body, "TO");
	}

	/**
	 * HTMLメール送信処理
	 * 
	 * @param to
	 * @param from
	 * @param subject
	 * @param body
	 * @param type
	 * @throws MessagingException
	 * @throws AddressException
	 * @throws UnsupportedEncodingException
	 */
	public void sendHtml(String to, String from, String subject, String body, String type)
			throws AddressException, MessagingException, UnsupportedEncodingException {
		MimeMessage msg = createMessage();
		setHeaders(msg, to, from, type);
		msg.setSubject(MimeUtility.encodeText(Win2Jis(subject), "ISO-2022-JP", "B"));
		body += " ";// ? が付くのをフィックスした
		msg.setContent(Win2Jis(body), "text/html; charset=iso-2022-jp");	// html
		msg.saveChanges();
		send(msg);
		/* try {
		 * MimeMessage msg = createMessage();
		 * setHeaders(msg, to, from, type);
		 * msg.setSubject(MimeUtility.encodeText(Win2Jis(subject), "ISO-2022-JP", "B"));
		 * body += " ";// ? が付くのをフィックスした
		 * msg.setContent(Win2Jis(body), "text/html; charset=iso-2022-jp"); // html
		 * msg.saveChanges();
		 * send(msg);
		 * } catch (MessagingException | UnsupportedEncodingException ex) {
		 * Logger.getLogger(JmSender.class.getName()).log(Level.SEVERE, ex.toString());
		 * } */
	}

	/**
	 * 複数の添付ファイル付きHTMLメール送信処理
	 * 
	 * @param to
	 * @param from
	 * @param subject
	 * @param body
	 * @param fileDir
	 * @param flist
	 */
	public void sendHtml(String to, String from, String subject, String body, String fileDir, List<String> flist)
			throws AddressException, MessagingException, IOException {
		sendHtml(to, from, subject, body, fileDir, flist, "TO");
	}

	/**
	 * 複数の添付ファイル付きHTMLメール送信処理
	 *
	 * @param to
	 * @param from
	 * @param subject
	 * @param body
	 * @param fileDir
	 *            // 添付ファイルのあるディレクトリ
	 * @param type
	 *            // TO , CC , BCC
	 * @param flist
	 *            // ファイル名のみのリスト（添付ファイル名となる）
	 * @throws MessagingException
	 * @throws AddressException
	 * @throws IOException
	 */
	public void sendHtml(String to, String from, String subject, String body, String fileDir, List<String> flist, String type)
			throws AddressException, MessagingException, IOException {
		MimeMessage msg = createMessage();
		setHeaders(msg, to, from, type);
		msg.setSubject(MimeUtility.encodeText(Win2Jis(subject), "ISO-2022-JP", "B"));
		body += " ";// ? が付くのをフィックスした
		msg.setContent(createHtmlAndAttachmentParts(Win2Jis(body), fileDir, flist), "text/html; charset=iso-2022-jp");
		msg.saveChanges();
		send(msg);
		/* try {
		 * MimeMessage msg = createMessage();
		 * setHeaders(msg, to, from, type);
		 * msg.setSubject(MimeUtility.encodeText(Win2Jis(subject), "ISO-2022-JP", "B"));
		 * body += " ";// ? が付くのをフィックスした
		 * msg.setContent(createHtmlAndAttachmentParts(Win2Jis(body), fileDir, flist), "text/html; charset=iso-2022-jp");
		 * msg.saveChanges();
		 * send(msg);
		 * } catch (MessagingException | IOException ex) {
		 * Logger.getLogger(JmSender.class.getName()).log(Level.SEVERE, ex.toString());
		 * } */
	}

	//////////////////////　下請けメソッド　　　/////////////////////////////////////////////////////////
	/**
	 * 複数添付ファイルの処理
	 *
	 * @param body
	 * @param path
	 * @param flist
	 * @return
	 * @throws MessagingException
	 * @throws IOException
	 */
	protected MimeMultipart createAttachmentParts(String body, String path, List<String> flist) throws MessagingException, IOException {
		MimeMultipart mp = new MimeMultipart();

		MimeBodyPart textPart = new MimeBodyPart();
		textPart.setText(Win2Jis(body), "ISO-2022-JP");	// WindowsではCP932対応処理
		mp.addBodyPart(textPart);
		/* ファイルパートの作成 */
		for(String fileName : flist) {
			/* ファイルパスを調べて、ファイルが存在する時だけ
			 * マルチパート処理する */
			String filePath = pathString(path) + fileName; // 元の名前
			File fp = new File(filePath);
			if(fp.exists()) {
				MimeBodyPart filePart = new MimeBodyPart();
				filePart.setDataHandler(new DataHandler(new FileDataSource(filePath)));
				setFileName(filePart, fileName, "ISO-2022-JP", "ja");
				mp.addBodyPart(filePart);
			}
		}
		return mp;
	}

	/**
	 * 複数添付ファイルの処理 本文はHTML送信用
	 *
	 * @param body
	 * @param path
	 * @param flist
	 * @return
	 * @throws MessagingException
	 * @throws IOException
	 */
	protected MimeMultipart createHtmlAndAttachmentParts(String body, String path, List<String> flist) throws MessagingException, IOException {
		MimeMultipart mp = new MimeMultipart();
		MimeBodyPart textPart = new MimeBodyPart();

		// html のための変更
		textPart.setContent(body, "text/html; charset=iso-2022-jp");
		mp.addBodyPart(textPart);
		/* ファイルパートの作成 */
		for(String fileName : flist) {
			/* ファイルパスを調べて、ファイルが存在する時だけ
			 * マルチパート処理する */
			String filePath = pathString(path) + fileName; // 元の名前
			File fp = new File(filePath);
			if(fp.exists()) {
				MimeBodyPart filePart = new MimeBodyPart();
				filePart.setDataHandler(new DataHandler(new FileDataSource(filePath)));
				setFileName(filePart, fileName, "ISO-2022-JP", "ja");
				mp.addBodyPart(filePart);
			}
		}
		return mp;
	}

	/**
	 * メッセージオブジェクトの作成
	 *
	 * @return
	 */
	protected MimeMessage createMessage() {
		return new MimeMessage(session);
	}

	/**
	 * ヘッダの付加 ／送信者，受信者，メーラー （注意）to = "some1@foo.com,some2@foo.com,...."
	 * のようにCSV形式で複数の送信先を設定可能
	 *
	 * @param msg
	 * @param to
	 * @param from
	 * @param type
	 *            CC, BCC, TO
	 * @throws MessagingException
	 * @throws AddressException
	 */
	protected void setHeaders(MimeMessage msg, String to, String from, String type) throws MessagingException, AddressException {
		//msg.setFrom(null);
		Message.RecipientType rtype;
		switch (type.toLowerCase()) {
		case "bcc":
			rtype = MimeMessage.RecipientType.BCC;
			break;
		case "cc":
			rtype = MimeMessage.RecipientType.CC;
			break;
		default:
			rtype = MimeMessage.RecipientType.TO;
			break;
		}
		//        
		msg.addFrom(InternetAddress.parse(from, true));
		msg.setRecipients(rtype, InternetAddress.parse(to, true));
		msg.setHeader("Content-Transfer-Encoding", "7bit"); // 2003.2.23 追加
		msg.setHeader("X-Mailer", "jmSender 1.1"); // 2003.2.23 up

	}

	/**
	 * 送信の下請けメソッド
	 *
	 * @param msg
	 * @throws MessagingException
	 */
	protected void send(MimeMessage msg) throws MessagingException {
		send(msg, msg.getAllRecipients());
	}

	/**
	 * 送信の下請けメソッド
	 *
	 * @param msg
	 * @param envelopeTo
	 * @throws MessagingException
	 */
	public synchronized void send(MimeMessage msg, Address[] envelopeTo) throws MessagingException {
		msg.setSentDate(new Date());
		// メッセージIDをつける
		session.getProperties().put("mail.from", ((InternetAddress) msg.getFrom()[0]).getAddress());
		msg.saveChanges();
		//
		transport.sendMessage(msg, envelopeTo);
	}

	/**
	 * 終端にFile.separatorをもつパス文字列を返す
	 *
	 * @param path
	 * @return
	 */
	String pathString(String path) {
		int n = path.length();
		String e = path.substring(n - 1);
		if(e.equals(File.separator)) {
			return path;
		}

		return path + File.separator;
	}

	/**
	 * ファイル名の拡張子を返す
	 *
	 * @param fname
	 *            ファイル名文字列
	 * @return 拡張子、無い時は ""
	 */
	protected String getExt(String fname) {
		int pos = fname.lastIndexOf('.');
		if(pos < 0) {
			return "";
		}

		StringBuilder bf = new StringBuilder();
		int ln = fname.length();
		if(pos < ln - 1) {
			pos++;
		} else {
			return "";
		}
		for(int i = pos; i < ln; i++) {
			char c = fname.charAt(i);
			bf.append(c);
		}
		if(bf.length() == 0) {
			return "";
		}
		return "." + bf.toString();
	}

	protected String Win2Jis(String s) {
		return Cp932.isWindows() ? Cp932.forJisMail(s) : s;
	}

	//////////////////////////////////////////////////////////////////////
	////////////////////   内部クラス　　//////////////////////////////////
	//////////////////////////////////////////////////////////////////////
	/**
	 * Authenticatorクラス 任意のユーザー名／パスワードの獲得方法を定義できる session の生成にこのクラスオブジェクトを使う
	 */
	class AuthenticatorUtil extends jakarta.mail.Authenticator {

		private String strUser;
		private String strPwd;

		/**
		 * コンストラクタでユーザー名／パスワードを獲得する仕様とした
		 *
		 * @param user
		 * @param password
		 */
		public AuthenticatorUtil(String user, String password) {
			this.strUser = user;
			this.strPwd = password;
		}

		/**
		 * PasswordAuthenticationを返す オーバーライド必須
		 */
		@Override
		protected PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(strUser, strPwd);
		}
	}

	//////////////////////////////////////////////////////////////////////////////
	///
	///   日本語添付ファイル名への対応
	///   ozacc-mail library の setFileName メソッドを使用しています。
	///   出典 ozacc-mail library http://sourceforge.jp/projects/spring-ext/ 
	///
	//////////////////////////////////////////////////////////////////////////////
	/**
	 * This method set Content-Disposition: with RFC2231 encoding. It is
	 * required JavaMail1.2.
	 * Part#setFileName()のマルチバイト対応版です。 JavaMail1.2でなければコンパイルできません
	 * 
	 * @param part
	 * @param filename
	 * @param charset
	 * @param lang
	 * @throws MessagingException
	 */
	public void setFileName(Part part, String filename, String charset, String lang)
			throws MessagingException {
		// Set the Content-Disposition "filename" parameter
		ContentDisposition disposition;
		String[] strings = part.getHeader("Content-Disposition");
		if(strings == null || strings.length < 1) {
			disposition = new ContentDisposition(Part.ATTACHMENT);
		} else {
			disposition = new ContentDisposition(strings[0]);
			disposition.getParameterList().remove("filename");
		}
		part.setHeader("Content-Disposition", disposition.toString()
				+ encodeParameter("filename", filename, charset, lang));
		ContentType cType;
		strings = part.getHeader("Content-Type");
		if(strings == null || strings.length < 1) {
			cType = new ContentType(part.getDataHandler().getContentType());
		} else {
			cType = new ContentType(strings[0]);
		}
		try {
			// I want to public the MimeUtility#doEncode()!!!
			String mimeString = MimeUtility.encodeWord(filename, charset, "B");
			// cut <CRLF>...
			StringBuffer sb = new StringBuffer();
			int i;
			while((i = mimeString.indexOf('\r')) != -1) {
				sb.append(mimeString.substring(0, i));
				mimeString = mimeString.substring(i + 2);
			}
			sb.append(mimeString);
			cType.setParameter("name", new String(sb));
		} catch (UnsupportedEncodingException e) {
			throw new MessagingException("Encoding error", e);
		}
		part.setHeader("Content-Type", cType.toString());
	}

	/**
	 * This method encodes the parameter.
	 * <P>
	 * But most MUA cannot decode the encoded parameters by this method. <BR>
	 * I recommend using the "Content-Type:"'s name parameter both.
	 * </P>
	 */
	/**
	 * ヘッダのパラメタ部のエンコードを行います。
	 * <P>
	 * 現状は受信できないものが多いのでこのメソッドだけでは使えません。 <BR>
	 * Content-Disposition:のfilenameのみに使用し、さらに Content-Type:のnameにMIME
	 * encodingでの記述も行うのが妥当でしょう。 <BR>
	 * パラメタは必ず行頭から始まるものとします。 (ヘッダの開始行から折り返された位置を開始位置とします)
	 * </P>
	 * <P>
	 * foldingの方針はascii/non ascii境界のみをチェックします。 現状は連続するascii/non
	 * asciiの長さのチェックは現状行っていません。 (エンコード後のバイト数でチェックしなければならないのでかなり面倒)
	 * </P>
	 *
	 * @param name
	 *            パラメタ名
	 * @param value
	 *            エンコード対象のパラメタ値
	 * @param encoding
	 *            文字エンコーディング
	 * @param lang
	 *            言語指定子
	 * @return エンコード済み文字列 ";\r\n name*0*=ISO-8859-2''・・・;\r\n name*1*=・・"
	 */
	// 1.全体をエンコードして長かったら半分に切ってエンコードを繰り返す
	public String encodeParameter(String name, String value, String encoding, String lang) {
		StringBuffer result = new StringBuffer();
		StringBuffer encodedPart = new StringBuffer();
		boolean needWriteCES = !isAllAscii(value);
		boolean CESWasWritten = false;
		boolean encoded;
		boolean needFolding = false;
		int sequenceNo = 0;
		int column;
		while(value.length() > 0) {
			// index of boundary of ascii/non ascii
			int lastIndex;
			boolean isAscii = value.charAt(0) < 0x80;
			for(lastIndex = 1; lastIndex < value.length(); lastIndex++) {
				if(value.charAt(lastIndex) < 0x80) {
					if(!isAscii) {
						break;
					}
				} else {
					if(isAscii) {
						break;
					}
				}
			}
			if(lastIndex != value.length()) {
				needFolding = true;
			}
			RETRY: while(true) {
				encodedPart.setLength(0);
				String target = value.substring(0, lastIndex);
				byte[] bytes;
				try {
					if(isAscii) {
						bytes = target.getBytes("us-ascii");
					} else {
						bytes = target.getBytes(encoding);
					}
				} catch (UnsupportedEncodingException e) {
					bytes = target.getBytes(); // use default encoding
					encoding = MimeUtility.mimeCharset(MimeUtility.getDefaultJavaCharset());
				}
				encoded = false;
				// It is not strict.
				column = name.length() + 7; // size of " " and "*nn*=" and ";"
				for(int i = 0; i < bytes.length; i++) {
					if((bytes[i] >= '0' && bytes[i] <= '9')
							|| (bytes[i] >= 'A' && bytes[i] <= 'Z')
							|| (bytes[i] >= 'a' && bytes[i] <= 'z') || bytes[i] == '$'
							|| bytes[i] == '.' || bytes[i] == '!') {
						encodedPart.append((char) bytes[i]);
						column++;
					} else {
						encoded = true;
						encodedPart.append('%');
						String hex = Integer.toString(bytes[i] & 0xff, 16);
						if(hex.length() == 1) {
							encodedPart.append('0');
						}
						encodedPart.append(hex);
						column += 3;
					}
					if(column > 76) {
						needFolding = true;
						lastIndex /= 2;
						continue RETRY;
					}
				}
				result.append(";\r\n ").append(name);
				if(needFolding) {
					result.append('*').append(sequenceNo);
					sequenceNo++;
				}
				if(!CESWasWritten && needWriteCES) {
					result.append("*=");
					CESWasWritten = true;
					result.append(encoding).append('\'');
					if(lang != null) {
						result.append(lang);
					}
					result.append('\'');
				} else if(encoded) {
					result.append("*=");
					/* 本当にcharacter encodingは先頭パートに書かないとだめなのか? if (encoded) {
					 * result.append("*="); if (!CESWasWritten && needWriteCES) {
					 * CESWasWritten = true;
					 * result.append(encoding).append('\''); if (lang != null)
					 * result.append(lang); result.append('\''); } */
				} else {
					result.append('=');
				}
				result.append(new String(encodedPart));
				value = value.substring(lastIndex);
				break;
			}
		}
		return new String(result);
	}

	/**
	 * check if contains only ascii characters in text.
	 * 
	 * @param text
	 * @return
	 */
	public static boolean isAllAscii(String text) {
		for(int i = 0; i < text.length(); i++) {
			if(text.charAt(i) > 0x7f) { // non-ascii
				return false;
			}
		}
		return true;
	}

}
