package mailutil_service;

import java.util.ArrayList;
import java.util.List;

/**
 * 1件だけの送信先データをリストとして作成し、
 * メールサービスを使って送信するためのユーティリティクラス
 */

public class Util {
	
	// 1件だけ（学生宛）の宛先リストを作成
	public static List<Recipient> createToList(String id,			// 宛先ID（学籍番号など） 
												String to, 			// 宛先メール
										 		String to_name){	// 宛先氏名
												

		List<Recipient> ls = new ArrayList<>();
		ls.add(new Recipient(true, 			// trueは送信対象であることを示す
							  id, 			// 宛先のID
							  to_name, 		// 宛先氏名
							  to, 			// 宛先メール
							  "本文ダミー",
							  "様"));		// 宛先に付加する敬称
		return ls;
		
	}
	// 送信データを作成して、送信サービスを呼び出す
	public static void sendMailService(String sender, // 送信元メール
								 String title, 			// メール表題
								 String flag, 			// "TO"、"CC"、"BCC"
								 String body, 			// 本文
								 List<Recipient> list	// 宛先リスト
		) {
		DataSet ds = new DataSet(sender,	// 送信元メール
								 title, 	// メール表題
								 "TO", 		// １件の通常送信
								 body, 		// 本文
								 list		// 宛先リスト
		);
		JmClient client = new JmClient();
		client.send(ds);
		client.close();
	}
	/**
	 * ms932文字列をutf-8に変換して返す
	 * 
	 * @param ms932 ms932の文字列
	 * @return utf-8の文字列
	 */
	public static String ms932_utf8(String ms932){
		String utf8=null;
		try {
			String temp = new String(ms932.getBytes("MS932"), "MS932");
			utf8 = new String(temp.getBytes("UTF-8"), "UTF-8" );
		}catch(Exception e) {
			System.out.println("★変換できない");
		}
		return utf8;
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////	
}
