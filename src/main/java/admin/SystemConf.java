/*
 * 作成日: 2005/02/17
 *
 * TODO
 */
package admin;

import java.io.PrintWriter;
import java.util.Hashtable;

import framework.Param;
import framework.SuperPlayer;

/**
 *
 	<program $admin.SystemConf>
		<dispatch  html=sample.html  number=10010  class=admin.SystemConf />
		<variable>
		  <receive   NUMBER STAMP GROUP TUID TMAIL TNAME KANA/>
		  <accept    CMD    UPLODE  />
		  <keep      />
		  
		  <work      />
		  <form      />
		</variable>
	</program> *
 */
public class SystemConf extends SuperPlayer {

	/* (非 Javadoc)
	 * @see framwork.SuperPlayer#initialize(java.io.PrintWriter, java.util.Hashtable, framwork.Param)
	 */
	public void initialize(PrintWriter out, Hashtable htb, Param parameter) {
		// TODO 自動生成されたメソッド・スタブ

	}

	/* (非 Javadoc)
	 * @see framwork.SuperPlayer#dispatch()
	 */
	public String dispatch() {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	/* (非 Javadoc)
	 * @see framwork.SuperPlayer#display(boolean)
	 */
	public void display(boolean editmode) {
		// TODO 自動生成されたメソッド・スタブ

	}

}
