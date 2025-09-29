/**
 * 電子メール送信ユーティリティ 
 * Copyright (c) 2015 Takashi Kawaba Released under the MIT license
 * https://opensource.org/licenses/MIT* 
 */
package net.tkxtools;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author
 */
public class Mail {

	/**
	 * 即時送信 /POP-BEFORE-SMPT対応、ポート指定
	 *
	 * @param smtpUser
	 * @param smtpPassword
	 * @param host
	 * @param port
	 * @param to
	 * @param from
	 * @param subject
	 * @param body
	 * @param type
	 */
	/* public static boolean sendPBS(String smtpUser, String smtpPassword, String host, int port, String to, String from, String subject, String body, String type) {
	 * if(!JmAuthenticate.authenticate(host, smtpUser, smtpPassword)) {
	 * Logger.getLogger(JmSender.class.getName()).log(Level.SEVERE, "Pop認証できない");
	 * return false;
	 * }
	 * try {
	 * JmSender js = new JmSender();
	 * js.connect(host, port);
	 * js.send(to, from, subject, body, type);
	 * js.disconnect();
	 * return true;
	 * } catch (Exception e) {
	 * return false;
	 * }
	 * } */
	/**
	 * 即時送信 / SMTP-AUTH対応
	 *
	 * @param smtpUser
	 * @param smtpPassword
	 * @param host
	 * @param port
	 * @param to
	 * @param from
	 * @param subject
	 * @param body
	 */
	public static boolean send(String smtpUser, String smtpPassword, String host, int port, String to, String from, String subject, String body) {
		return send(smtpUser, smtpPassword, host, port, to, from, subject, body, "TO");
	}

	/**
	 * 即時送信 / SMTP-AUTH対応、タイプ指定
	 *
	 * @param smtpUser
	 * @param smtpPassword
	 * @param host
	 * @param port
	 * @param to
	 * @param from
	 * @param subject
	 * @param body
	 * @param type
	 */
	public static boolean send(String smtpUser, String smtpPassword, String host, int port, String to, String from, String subject, String body, String type) {
		try {
			JmSender js = new JmSender(smtpUser, smtpPassword);
			js.connect(host, port);
			js.send(to, from, subject, body, type);
			js.disconnect();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 即時送信 / SMTP-AUTH対応、添付ファイル付き
	 *
	 * @param smtpUser
	 * @param smtpPassword
	 * @param host
	 * @param port
	 * @param to
	 * @param from
	 * @param subject
	 * @param body
	 * @param fileDir
	 * @param flist
	 */
	public static boolean send(String smtpUser, String smtpPassword, String host, int port, String to, String from, String subject, String body, String fileDir, List<String> flist) {
		return send(smtpUser, smtpPassword, host, port, to, from, subject, body, fileDir, flist, "TO");

	}

	/**
	 * 即時送信 / SMTP-AUTH対応、ポート指定、添付ファイル付き
	 *
	 * @param smtpUser
	 * @param smtpPassword
	 * @param host
	 * @param port
	 * @param to
	 * @param from
	 * @param subject
	 * @param body
	 * @param fileDir
	 * @param flist
	 * @param type
	 */
	public static boolean send(String smtpUser, String smtpPassword, String host, int port, String to, String from, String subject, String body, String fileDir, List<String> flist, String type) {
		try {
			JmSender js = new JmSender(smtpUser, smtpPassword);
			js.connect(host, port);
			js.send(to, from, subject, body, fileDir, flist, type);
			js.disconnect();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 即時送信 / HTMLメール
	 * 
	 * @param smtpUser
	 * @param smtpPassword
	 * @param host
	 * @param port
	 * @param to
	 * @param from
	 * @param subject
	 * @param htmlFile
	 */
	public static boolean sendHtml(String smtpUser, String smtpPassword, String host, int port, String to, String from, String subject, String htmlFile) {
		return sendHtml(smtpUser, smtpPassword, host, port, to, from, subject, htmlFile, "TO");
	}

	/**
	 * 即時送信 / HTMLメール タイプ指定
	 * 
	 * @param smtpUser
	 * @param smtpPassword
	 * @param host
	 * @param port
	 * @param to
	 * @param from
	 * @param subject
	 * @param htmlFile
	 * @param type
	 */
	public static boolean sendHtml(String smtpUser, String smtpPassword, String host, int port, String to, String from, String subject, String htmlFile, String type) {
		try {
			JmSender js = new JmSender(smtpUser, smtpPassword);
			js.connect(host, port);
			js.sendHtml(to, from, subject, htmlFile, type);
			js.disconnect();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 即時送信 / HTMLメール、ファイル添付
	 * 
	 * @param smtpUser
	 * @param smtpPassword
	 * @param host
	 * @param port
	 * @param to
	 * @param from
	 * @param subject
	 * @param htmlFile
	 * @param fileDir
	 * @param flist
	 */
	public static boolean sendHtml(String smtpUser, String smtpPassword, String host, int port, String to, String from, String subject, String htmlFile, String fileDir, List<String> flist) {
		return sendHtml(smtpUser, smtpPassword, host, port, to, from, subject, htmlFile, fileDir, flist, "TO");
	}

	/**
	 * 即時送信 / HTMLメール、ファイル添付、タイプ指定
	 * 
	 * @param smtpUser
	 * @param smtpPassword
	 * @param host
	 * @param port
	 * @param to
	 * @param from
	 * @param subject
	 * @param htmlFile
	 * @param type
	 * @param fileDir
	 * @param flist
	 */
	public static boolean sendHtml(String smtpUser, String smtpPassword, String host, int port, String to, String from, String subject, String htmlFile, String fileDir, List<String> flist,
			String type) {
		try {
			JmSender js = new JmSender(smtpUser, smtpPassword);
			js.connect(host, port);
			js.sendHtml(to, from, subject, htmlFile, fileDir, flist, type);
			js.disconnect();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * 全ファイルデータを読みだして文字列にして返す
	 *
	 * @param path
	 *            ファイルのあるパス
	 * @return ファイルデータの文字列
	 */
	public static String getText(String path) {
		StringBuilder text = new StringBuilder();
		try(InputStream is = new FileInputStream(path);
				BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));) {

			String line;
			while((line = in.readLine()) != null) {
				text.append(line);
				//text.append("\n");
			}
		} catch (IOException e) {
			Logger.getLogger(JmSender.class.getName()).log(Level.SEVERE, e + "★ファイルが見つからない:" + path);
		}
		return text.toString();
	}

}
