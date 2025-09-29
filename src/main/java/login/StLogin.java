package login;

import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Vector;
import database.Database;
import database.DbConnectionBroker;
import framework.Param;
import framework.SuperPlayer;
import student.Student;

public class StLogin
		extends SuperPlayer
		implements LoginVar {
	String ret;
	String disp_mode;
	String cmd;
	DbConnectionBroker broker;
	Database db;

	@Override
	public void initialize(PrintWriter out, Hashtable htb, Param para) {
		this.broker = getDbConnection();
		this.db = new Database(this.broker);
	}

	@Override
	public String dispatch() {
		if(login()) {
			setSession(getParameter("_stNumber"), getParameter("_szDB").toLowerCase());

			putParameter("_stPasswd", "");
			if(!autoLogin()) {
				this.disp_mode = "EDIT";
				this.ret = "RETURN";
			} else {
				putParameter("_autoLogin", "ON");
				this.disp_mode = "NEW";
				this.ret = "$student.StTable";
			}
		} else {
			this.disp_mode = "EDIT";
			this.ret = "SELF";
		}
		putParameter("_display_mode", this.disp_mode);
		return this.ret;
	}

	boolean autoLogin() {
		String mail = getParameter("_stMail");
		if(!isEmpty(mail)) {
			return true;
		}
		return false;
	}

	public boolean login() {
		String szDB = getParameter("_szDB").trim().toLowerCase();
		String stNumber = getParameter("_stNumber").trim();
		String stPasswd = getParameter("_stPasswd").trim();
		
		if(stNumber.length()>10) {	// SQLインジェクション対策
			putParameter("_msg", "★不正なログイン操作です");
			return false;
		}

		Student st = new Student(szDB, stNumber, this.db);
		if(!st.isEmptyRecord()) {
			String passwd = st.stPasswd().trim();
			if(!stPasswd.equals(passwd)) {
				putParameter("_msg", "★パスワードが違います");
				return false;
			}
			return true;
		}
		putParameter("_msg", "★ユーザー名またはグループ名が違っています");
		return false;
	}

	@Override
	public void display(boolean editmode) {
		if(!editmode) {
			putParameter("_msg", "");
			putParameter("_stNumber", "");
			putParameter("_stPasswd", "");
			putParameter("_szDB", "");
		}
		Vector v = loadHtml(getParameter("_dispFile"));
		printVector(v);
	}
}
