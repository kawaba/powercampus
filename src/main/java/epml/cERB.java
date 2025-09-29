/*
	
	EmbededRadioButtons（複数行のテキストのどれかにチェックを入れて
	正解を示すクラス）の要素クラス
 */
package epml;

import epml.tools.*;
import java.util.*;

public class cERB extends Object {
	//
	int ansSet; // 正解がセットされる度に＋１する（正解の有無などチェックするため）
	//
	String name; // 一意名　 "ERB(1)" など
	String group; // 出現番号（1 など)　＝ 複数選択肢をまとめるグループ番号
	String seqNum; // = group
	Vector disp; // 問題文ごとの解答語（表示にしか使われない）
	Vector ans; // 問題文ごとの複数の解答語の正誤
	Vector eval; // 問題文ごとの正誤を表すマーク．採点時に付与するのでそれまでは空白．正解="t" 不正解="f"

	boolean chkbox; // 複数の解答があるときtrue,それ以外はfalse
	//
	int ap; // 配点（複数選択肢の場合はその各々の点数となることに注意）
	int points; // 得点（複数選択肢の場合は各選択肢得点の合計点）
	int fontLevel; // フォントレベル

	//
	// boolean sw; // true (default)なら間違いを減点しない．
	//
	public cERB(String _group, String _ans, int _ap, int _fontLevel) {
		if (DBG.fa)
			DBG.println("class cERB #cERB() : コンストラクタ の先頭です");
		//
		ansSet = 0;
		group = _group;
		seqNum = _group;
		ap = _ap;
		//
		disp = new Vector(10, 5);
		ans = new Vector(10, 5); // 正解なら１、不正解なら０を入れる
		eval = new Vector(10, 5); // 採点して正解ならt 不正解ならf を入れる．採点されるまで値はない．
		add(_ans); // 要素を追加
		name = "ERB(" + group + ")";
		points = 0;
		fontLevel = _fontLevel;
		//
		chkbox = false;
		// sw = true;
	}

	// for DEBUG ans の内容を返す
	public String displayAns() {
		StringBuffer bf = new StringBuffer(100);
		boolean flag = false;
		for (int i = 0; i < ans.size(); i++) {
			if (flag)
				bf.append(", ");
			bf.append((String) (ans.get(i)));
		}
		return bf.toString();
	}

	// 正解の数を返す
	public int corects() {
		return ansSet;
	}

	// 複数正解（チェックボックスタイプ）か
	public boolean isCheckbox() {
		return chkbox;
	}

	/**
	 * 問題文を取り込む 問題文は、「*あいうえお~ck~t」のように、~ で区切られたオプション指定をもつ場合がある． ck
	 * はチェックボックであることを明示するために、学生の解答につけるもの t/f は採点後に、正解であるか否かを示すために採点処理で付加するものである
	 * ck はラジオボタンの場合は付加しないので、採点後の問題文にない場合があるが、t/f は採点すると 必ず付加される．
	 * 
	 * @param _ans
	 */
	public void add(String _ans) {
		//
		// チェックボックスの場合、選択語の末尾に "~ck " が付いていることがあるので
		// これを削除して、記録を残す
		String temp = _ans.trim();
		Csv cs = new Csv(temp, "~");
		int max = cs.size();

		// 問題文とオプション設定を得る
		String ansline = "";
		String ck1 = "";
		String ck2 = "";
		if (max == 1) {
			ansline = cs.get(0);
			ck1 = "";
			ck2 = "";
		} else if (max == 2) {
			ansline = cs.get(0);
			ck1 = cs.get(1);
			ck2 = "";
		} else if (max == 3) {
			ansline = cs.get(0);
			ck1 = cs.get(1);
			ck2 = cs.get(2);
		}
		// 問題文を取り込みながら、解答のシーケンス（0/1）を設定する
		if (match(ansline.charAt(0)) == '*') {
			ans.add("1"); // 正解に１（正解）を追加
			disp.add((ansline.substring(1)).trim()); // 表示問題に＊を除いて追加
			++ansSet; // 正解数を１プラス
			//
		} else {
			ans.add("0"); // 正解に０（誤り）を追加
			disp.add(ansline); // 表示問題に追加
		}
		if (ansSet > 1)
			chkbox = true; // 複数正解ならチェックボックス処理をＯＮにしておく
		//
		// オプション設定を取り込む
		setOption(ck1);
		setOption(ck2);

	}

	/**
	 * オプション文字列を取り込む<br>
	 * 問題がチェックボックスであることを示すckオプション、または 採点の結果この問題文への解答が正解か否かを示す文字列（t/f）をVecor eval
	 * へ取り込む 採点されていれば、（t/f）は全ての問題文に付加されている
	 * 
	 * @param ck
	 *            オプション指定が含まれる文字列
	 */
	void setOption(String ck) {
		if (ck.length() == 0)
			return; // 文字列がなければそのままリターンする
		//
		if (ck.equals("ck")) {
			chkbox = true;
		} else if (ck.equals("t")) {
			eval.add(new String("t"));
		} else if (ck.equals("f")) {
			eval.add(new String("f"));
		}
	}

	/**
	 * 全角のアスタリスクのみを半角にし、その他はそのまま返す<br>
	 * 
	 * @param c
	 *            検査する文字
	 * @return
	 */
	char match(char c) {
		if ((c == '*') || (c == '＊')) {
			return '*';
		}
		return c;
	}

	// 採点方法を変更する
	// public void setSW(boolean t) { sw = t; }

	// 要素数
	public int size() {
		return disp.size();
	}

	// ｋ番目の解答語の正誤を返す
	public String answer(int k) {
		return (String) ans.get(k);
	}

	// 要素データ
	public String name() {
		return name;
	}

	public String seqNum() {
		return seqNum;
	}

	public int fontLevel() {
		return fontLevel;
	}

	//
	// ｋ番目の表示文字列を返す（ｋはゼロオリジン）
	public String dispOf(int k) {
		//
		String dispStr = (String) disp.get(k);
		return dispStr;
	}

	// ｋ番目の表示文字列のキーを返す
	public String keyOf(int k) {

		return "ERB(" + group + "," + String.valueOf(k) + ")";
	}

	//
	// ｋ番目の表示文字列とその正誤をCSVで返す
	public Csv answerOf(int k) {
		String dispStr = (String) disp.get(k);
		String ansStr = (String) ans.get(k);
		return new Csv(ansStr + "," + dispStr);
	}

	// CSVデータで解答をセットする
	public void setAnswer(Csv cs) {
		if (DBG.fa)
			DBG.println("class cERB #setAnswer() : CSVデータで解答をセットする の先頭です");
		if (DBG.fa)
			DBG.println("        cs =" + cs.getCsvString());
		//
		ans.removeAllElements(); // 全解答を消去
		for (int k = 0; k < size(); k++) {
			ans.add(cs.get(k));
		}
	}

	// 得点
	public void setPoints(int pt) {
		points = pt;
	}

	public int getPoints() {
		return points;
	}

	public void clsPoints() {
		points = 0;
	}

	// 配点
	public int getAp() {
		return ap;
	}

	public void setAp(int pt) {
		ap = pt;
	}

	public void clsAp() {
		ap = 0;
	}

	/**
	 * 正誤をセットする
	 * 
	 * @param e
	 *            正誤文字（t/f）
	 */
	public void setEval(String e, int k) {
		//
		// eval の要素がなければひとまず全要素を""に初期化する
		if (eval.size() == 0) {
			for (int i = 0; i < disp.size(); i++) {
				eval.add("");
			}
		}
		// k番目の要素にeをセットする
		eval.set(k, e);
	}

	/** 正誤を得る */
	public String getEval(int k) {
		// eval に要素がないか添え字が要素数よりも大きければ "" を返す
		if ((eval.size() == 0) || (eval.size() < (k + 1))) {
			return "";
		}
		return (String) (eval.get(k));
	}

	/**
	 * 採点する<br>
	 * 
	 * 本オブジェクトを正解として、解答と照合し採点した点数を返す また、正誤の状態を学生の解答オブジェクトにセットする
	 * 
	 * @param ansS
	 *            学生の解答オブジェクト
	 */
	public int grades(cERB ansS) {
		//
		int ansAp = ap; // 配点
		points = 0;
		int n = size();
		
		boolean miss = false;	// 解答に間違いがあった場合true 
		for(int k=0; k<n; k++){
			int	chk = isOK(ansS, k);
			if(chk==0){
				miss = true;
			}
		}
		if(miss){
			points  = 0;	// ひとつでも間違いがあると0点
		}else{
			points	= ap;
		}
		return	points;
		
		
		/*
		for (int k = 0; k < n; k++) {
		
			
			int pts = isOK(ansS, k); // ｋ番目の項目の正誤を調べる
			// if(sw){
			if (!chkbox) {
				if (pts >= 0)
					points += (pts * ansAp); // pts =
											 // -1,0,1　チェックすべき個所の正誤のみ見る方式（正解ひとつのとき）
			} else {
				points += (pts * ap); // チェックしてはいけないところにチェックをつけると減点する方式(複数解答のとき)
			}
		}
		// マイナス得点になる場合は０点とする
		if (points < 0) {
			points = 0;
		}
		*/
		// System.out.println("★★★"+ansS.toString()+"★★★");

		
	}

	// 正解か
	public int isOK(cERB ansS, int k) {
		Vector _ansS_ans = ansS.ans;
		String ansST = (String) _ansS_ans.get(k); // 学生の答え
		String answer = (String) ans.get(k); // 正解
		//
		
		if(answer.equals(ansST)){
			ansS.setEval("t", k);
			return 1;	// 正解
			
		}else{
			ansS.setEval("f", k);
			return	0;	// 不正解
		}
			
		
		/*
		
		if ((answer.equals(ansST)) && (answer.equals("1"))) { // 正解
			ansS.setEval("t", k);
			return 1;
		} else if (answer.equals(ansST)) { // 正解（ただしチェックなしのところ）
			ansS.setEval("t", k);
			return 0;
		}
		ansS.setEval("f", k);
		return -1; // 不正解
		*/
	}

	// 同じ問題か
	public boolean isSame(cERB obj) {
		if (name.equals(obj.name))
			return true;
		return false;
	}

	//
	public String toString() {
		String CR = System.getProperty("line.separator");
		StringBuffer bf = new StringBuffer(1024);
		bf.append(name + "/(grp)" + group + "/(sq)" + seqNum + "/(ckbx)"
				+ chkbox + "/(ap)" + ap + "/(得点)" + points + "/(font)"
				+ fontLevel + CR);
		getAll(bf);
		return bf.toString();
	}

	// 全ての解答のＣＳＶ表現を得る
	public void getAll(StringBuffer bf) {
		String CR = System.getProperty("line.separator");
		int size = disp.size();
		for (int i = 0; i < size; i++) {
			bf.append("      要素(" + i + ")" + getSel(i) + CR);
		}
		return;
	}

	// ｋ番目の選択肢と正誤
	public String getSel(int k) {
		return " 正誤=" + (String) ans.get(k) + " " + (String) disp.get(k) + " "
				+ (String) (eval.get(k));

	}

}