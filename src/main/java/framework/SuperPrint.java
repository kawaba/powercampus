/*
 * 汎用の書き換えクラス 2005.2 新バージョンに書き換えの版 1.1.2
 * 
 * templateは Windows-31J で作成された表示用のHTMLファイル 置き換える文字列は %_tx_userName%
 * のように、％文字で挟んだパラメータ名である。
 *  
 */
package framework;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import tktools.Gear;
import tktools.Stack;
import tktools.StringGear;

public class SuperPrint extends Object {
    //
    /** パーセント文字 */
    static final String PERCENT = "%";

    private static final String LETTER = "abcdefghijklmnopqrstuvwxyz_ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String DIGIT = "1234567890";

    private static final String LETTERorDigit = ".abcdefghijklmnopqrstuvwxyz_ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";

    protected PrintWriter out; // 出力先

    boolean crlfFlag; // println() <== true, print() <== false

    boolean presetFlag;

    /**
     * デフォルトコンストラクタ
     */
    public SuperPrint() {
    	if(LOG.fa)	LOG.println("■ SuperPrint #コンストラクタ");
    }

    /**
     * 通常のコンストラクタ PowerCampus Framework では使わない
     * 
     * @param _out
     */
    public SuperPrint(PrintWriter _out) {
        out = _out;
        crlfFlag = true;
    }

    /**
     * コンストラクタの代わりにクラスを初期化する
     * 
     * PowerCampus Framework 用
     */
    public void setInit(PrintWriter _out) {
        out = _out;
        presetFlag = true;
        crlfFlag = true;
    }

    public void crlf_off() {
        crlfFlag = false;
    }

    public void crlf_on() {
        crlfFlag = true;
    }

    public void vecPrint(Vector html, Hashtable htb) {
        printVector(html, htb, true);
    }

    /**
     * hidden タグの変数を実際の出力に先立って、実数で埋めてしまうかどうか を決めるタグの値をセット、リセットする。
     * 
     * true ----------- 事前に埋める false ---------- 最後に埋める（出力の中で変数が生成される時はこちらを）
     */
    public void setPresetFlag() {
        presetFlag = true;

    }

    public void clearPresetFlag() {
        presetFlag = false;

    }

    public void vecPrint(Vector html, Hashtable htb, boolean notmail) {
        printVector(html, htb, notmail);
    }

    /**
     * 変数、個別処理、反復処理、ブロック判定など全てに対応 入力はベクターに限定
     * 
     * @param html
     * @param htb
     */
    public void printVector(Vector html, Hashtable htb) {
        printVector(html, htb, true);
    }

    public void printVector(Vector html, Hashtable htb, boolean notmail) {

        if (LOG.fa)  LOG.println("■SuperPrint#PrintVector(Vector html,Hashtable htb,boolean notmail)");

        /*
         * htmlを前処理 % を _PERCENT_ に置き換得る．また、hiddenタグの埋め込みを行なう
         */
        htb.put("_PCNT_", "%");
        htb.put("_PERCENT_", "%");
        
        setSystemVar(html, htb);

        BufferedReader in, in2;
        String line, _line;
        // templateは Windows-31J で作成された表示用の HTML ファイル
        // 置き換える文字列は %_tx_userName% のように、％文字で挟んだ
        // パラメータ名である。パラメータの増減があっても、template を修正するだけでよい。
        if (html == null) {
            System.out.println("** paramPrintByVector() : 雛型の文字列が空である");
            return;
        }
        if (html.size() == 0) {
            System.out.println("** paramPrintByVector() : 雛型の文字列が１件もない");
            return;
        }
        int n = html.size();
        //
        boolean printFlag;
        for (int k = 0; k < n; k++) {
            line = Cp932.toJIS((String) (html.get(k))); // 一度、JISに基準化
            //
            // htb にある指定された変数が true ならば、
            // あらかじめコメント行として作成しておいたHTMLの１行を直下の１行と置き換える
            // 変数名はコメント行に書き込んである (2003.4.11 追加)
            //
            if (isComment(line)) { // コメント行の検出
                boolean rflag = replaceChk(line, htb); // 置き換えるのかどうか
                if (rflag) {
                    k++;
                    line = Cp932.toJIS((String) (html.get(k))); // 次の行がデータ部分
                    k++; // --> を消す
                    k++; // 置き換える行を消す
                }
            }
            //
            //パラメータを切り出す
            StringTokenizer st = new StringTokenizer(line, "%");
            String dt = "";
            printFlag = true;
            while (st.hasMoreTokens()) {
                String tk = st.nextToken();
                String tk2 = "   "; // ダミーを入れておく
                String tkn = tk.trim();

                if (tk.charAt(0) == '_') {
                    if ((tk.length() > 2) && (tk.charAt(1) == '$')) {
                        tk2 = tk; // コピーを保存して
                        tk = "_" + tk.substring(2); // $ 記号を取り除く
                    }
                    String ps = (String) htb.get(tk); //ハッシュ表を参照
                    if (ps == null) {
                        dt += "";
                        
                    } else {
                        
                        if (tk2.charAt(1) == '$') { // 常に参照するので tk2 は null
                                                    // ではいけない
                            ps = replaceToBR(ps); // 2003.9.2 データ中の改行を<br>に変換する
                        }
                        // 修正.クライアントはWindowsを仮定
                        dt += ps; // 最後の出力部分で全部を対象にすることに変更した
                    }
                    /*
                     * pct タグによる処理 2005.2 -
                     * 
                     * <pct:x> の7文字を最低字数とする
                     */
                } else if ((tkn.length() > 6) && (tkn.substring(0, 5).equals("<pct:"))) {
                    if (dt.length() != 0){
                        printOut(printFlag, notmail, dt); // 現在までの分を出力する
                    }
                    dt = "";
                    Vector exHtml 	= new Vector(20, 10); // 反復用雛型データを入れるベクター
                    String tagName 	= getTagName(tkn); // writeメソッドで処理選択のキーとなる
                    if (isBlockTag(((String) (html.get(k + 1))).trim())) { // 先読みしてみる

                        int ckFlag = 0;
                        for (k = k + 1; k < n; k++) {// 次行から先を読む．cp932() 不要
                            String str = (String) (html.get(k));
                            //
                            if (isBlockTag(str)) {// 反復開始指示子
                                if (ckFlag == 0) {
                                    ckFlag++; // "<pct: block>" を捨てる
                                } else {
                                    ckFlag++;
                                    exHtml.add(str); // "<pct: block>" 自体を
                                                     // exHtml へ格納（再帰）
                                }
                            } else if (isBlockEndTag(str)) {// 反復終了指示子
                                ckFlag--;
                                if (ckFlag == 0) {
                                    break; // "<pct: /block>" は捨てる．取り込み終了
                                } else {
                                    exHtml.add(str); // "<pct: /block>" 自体を
                                                     // exHtml へ格納（再帰）
                                }
                            } else {
                                exHtml.add(str);
                            }
                        }
                        write(tagName.substring(1), exHtml, htb); // 個別プログラムごとに特殊な処理
                        printFlag = false; // 標準の出力をしない

                    } else {
                        write(tagName.substring(1), htb); // 個別プログラムごとに特殊な処理(exHtml
                                                          // は不要)

                    }

                    //特殊な処理（反復を含む）
                } else if (tk.charAt(0) == '@') {

                    if (dt.length() != 0)
                        printOut(printFlag, notmail, dt); // 現在までの分を出力する
                    dt = "";
                    //
                    Vector exHtml = new Vector(20, 10); // 反復用雛型データを入れるベクター
                    String chk = ((String) (html.get(k + 1))).trim(); // 先読みをしてみる．cp932()
                                                                      // 不要
                    if (chk.equals("<<")) {
                        // 反復指定あり
                        //
                        int ckFlag = 0;
                        for (k = k + 1; k < n; k++) {// 次行から先を読む．cp932() 不要
                            String str = ((String) (html.get(k))).trim();
                            //
                            if (str.equals("<<")) {// 反復開始指示子
                                if (ckFlag == 0) {
                                    ckFlag++; // "<<" は捨てる
                                } else {
                                    ckFlag++;
                                    exHtml.add(str); // "<<" 自体を exHtml へ格納（再帰）
                                }
                            } else if (str.equals(">>")) {// 反復終了指示子
                                ckFlag--;
                                if (ckFlag == 0) {
                                    break; // ">>" は捨てる．取り込み終了
                                } else {
                                    exHtml.add(str); // ">>" 自体を exHtml へ格納（再帰）
                                }
                            } else {
                                exHtml.add(str);
                            }
                        }
                        write(tk.substring(1), exHtml, htb); // 個別プログラムごとに特殊な処理
                        //
                        printFlag = false; // 標準の出力をしない
                        //
                    } else {
                        // 反復指定なし
                        //
                        write(tk.substring(1), htb); // 個別プログラムごとに特殊な処理(exHtml
                                                     // は不要)
                    }
                    //printFlag = false; // 標準の出力をしない
                    //
                    // ブロックを再帰処理する（ブロック全体を出力しない処理を含む）
                } else if (tk.charAt(0) == '#') {// %#(label)% から #end
                                                 // までをブロックとして取り出す．paramPrintBLK()
                                                 // でブロックの出力の可否を判断
                    //
                    Vector exHtml = new Vector(50, 10); // 反復用雛型データを入れるベクター
                    //
                    // #end が出現するまで全てを exHtml に取り込む
                    for (k = k + 1; k < n; k++) { // 次行から
                        String str = ((String) (html.get(k))).trim(); // 先を読む．cp932()
                                                                      // 不要
                        if (str.equals("#end"))
                            break; // 終端
                        exHtml.add(str); // << 以下にデータがなかったときのためk+1から始めるので
                    }
                    blockWrite(tk.substring(1), exHtml, htb); // このブロックを出力するか否かを決める．出力にはPrintByVector()を再帰的に呼び出す
                    printFlag = false; // 標準の出力をしない
                } else {
                    dt += tk;
                }
            }
            printOut(printFlag, notmail, dt);
        }
    }

    /**
     * 出力
     * 
     * @param printFlag
     * @param notmail
     * @param dt
     */
    public void printOut(boolean printFlag, boolean notmail, String dt) {
        if (printFlag) {
            if (notmail){
                if (crlfFlag) {
                    String	pstr	=	Cp932.toCp932(dt);
                    
                    //LOG.println(pstr);
                    
                    out.println(pstr); // Cp932 にもどしてから表示する（改行あり）
                } else {
                    String	pstr	=	Cp932.toCp932(dt);
                    
                    //LOG.println(pstr);
                    
                    out.print(pstr); // Cp932 にもどしてから表示する（改行なし）
                }
            }else{
                if (crlfFlag) {
                    if(Cp932.isCp932){
                        //LOG.println("forJisMail(dt)-1");
                        out.println(Cp932.forJisMail(dt));// Windows
                        
                    }else{
                        out.println(dt); // そのまま書く
                        
                    }
                    
                } else {
                    if(Cp932.isCp932){
                        //LOG.println("forJisMail(dt)-2");
                        out.print(Cp932.forJisMail(dt));// Windows
                        
                    }else{
                        out.print(dt); // そのまま書く
                        
                    }
                }
            }
        }
    }

    /**
     * コメント行かどうか検査する
     * 
     * @param line
     * @return
     */
    boolean isComment(String line) {
        // 改行のみ
        if (Gear.isEmpty(line))
            return false;

        // <!-- は最低でも４文字
        String test = line.trim();
        if (test.length() < 4)
            return false;

        // <!-- との一致を検査
        if (test.charAt(0) != '<')
            return false;
        if (test.charAt(1) != '!')
            return false;
        if (test.charAt(2) != '-')
            return false;
        if (test.charAt(3) != '-')
            return false;
        return true;
    }

    /**
     * 行データの入れ替えチェック
     * 
     * 直前のコメントブロック内に代替のHTMLを配しておき、フラグが true のとき、該当の１行をこの代替行に置き換える。
     * 置き換えの対象になるのは１行だけであることに注意
     * 
     * フラグ名はコメント内に書き込まれている。 例示： <!--
     * 
     * @fileflag
     * 
     * 
     * replaceChk() は、このフラグを取り出し、ハッシュ（htb） をチェックして true か false を返す。
     * 
     * <!--
     * @fileflag
     * <td width="38" valign="middle" align="center"></td>
     * -->
     * <td width="10" valign="middle" align="center"></td>
     * <td width="28" valign="middle" align="center"></td>
     * 
     * <!--$_fileflag などの中から、 _fileflag の部分を取り出し その名前をキーとして htb から値を検索して返す
     * キーが定義されてないか値が "" であれば false を返す
     * 
     * @param line
     * @param htb
     * @return
     */
    public boolean replaceChk(String line, Hashtable htb) {
        //DBG.println("■SuperPrint #replaceChk()");
        /*
         * 含まれる名前を求める
         */
        String key = StringGear.getName(line, "@$");// null かもしれない
        if (key == null)
            return false;
        /*
         * システムハッシュに名前をキーとする値があればtrue. 値は "" でなければなんでもいい. _ の付加は
         * SuperPlayer#putParameter() の動作に合わせるため
         */
        if (key.charAt(0) != '_') {
            key = "_" + key;
        }
        String fl = Gear.strHash(htb, key);
        if (Gear.isEmpty(fl)) {
            //DBG.println("□□□" + key + " is " + false);
            return false;
        } else {
            //DBG.println("□□□" + key + " is " + true);
            return true;
        }
    }

    /**
     * ＄や＠で始まるフラグ名を文字列から取り出す
     * 
     * @param line
     * @return フラグ名。ない場合はnullを返す
     */
    String getName(String line) {
        StringBuffer bf = new StringBuffer();
        StringReader sk = new StringReader(line);
        int c;
        try {
            /*
             * @ か $ の頭だし
             */
            while ((c = sk.read()) != -1) {
                if ((c == '@') || (c == '$')) {
                    break;
                }
            }
            if (c == -1)
                return null;
            /*
             * 空白が出てくるか文字列の最後になるまで取り出す
             */
            while ((c = sk.read()) != -1) {
                if (StringGear.isSpaceChar((char) c)) {// 漢字空白も含める
                    break;
                }
                bf.append(c);
            }

        } catch (IOException e) {

        }

        if (bf.length() == 0)
            return null;
        return bf.toString();
    }

    /**
     * ブロックタグかどうか
     * 
     * @param tag
     * @return
     */
    public boolean isBlockTag(String tag) {
        String name = getTagName(tag.trim());
        if (name.equals("block")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * ブロック終了タグかどうか
     * 
     * @param tag
     * @return
     */
    public boolean isBlockEndTag(String tag) {
        String name = getTagName(tag.trim());
        if (name.equals("/block")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * PCTタグからタグ名を取得する
     * 
     * @param pct
     * @return
     */
    public String getTagName(String pct) {
        String tagName = "";
        try {
            int len = pct.length();
            int start = pct.indexOf(":") + 1;
            tagName = (pct.substring(start, len - 1)).trim();
        } catch (StringIndexOutOfBoundsException e) {
            tagName = "";
            //DBG.println("★pct=" + pct);
            //e.printStackTrace();
        }
        return tagName;

    }

    /**
     * 出力前にパーセント記号の書き換えとhiddenタグの付加を行う
     * 
     * @param html
     *            htmlデータ
     * @param ht
     *            システムハッシュ
     */
    void setSystemVar(Vector html, Hashtable ht) {
        /*
         * 再帰呼び出しのために何度も実行されることを防ぐために一度実行したときに システムハッシュに記録を残す．
         * 記録を参照して実行済みなら何もしないでリーターンする
         */
        String check = strHash(ht, "sys.setSystemVar");
        if (!isEmpty(check))
            return;

        setPercent(ht);
        addHiddenTags(html, ht);
        ht.put("sys.setSystemVar", "DONE");
    }

    /**
     * _PERCENT_ を "%" に置き換えられるようハッシュに変数を追加する <br>% を区切り文字として扱うので %
     * そのものがデータとして書いてあるとそれは空白になってしまう % をそのまま使いたい時は %_PERCENT_% または
     * %_PCNT_%と書いておく．
     * 
     * @param ht
     *            システムハッシュ
     */
    void setPercent(Hashtable ht) {

        ht.put("_PERCENT_", PERCENT);
        ht.put("_PCET_", PERCENT);
        ht.put("_PCNT_", PERCENT);

    }

    /**
     * システムハッシュにあるhiddenタグデータを </form>の直前に付加する
     * 
     * @param html
     *            htmlデータ
     * @param ht
     *            システムハッシュ
     */
    void addHiddenTags(Vector html, Hashtable ht) {

        /* hidden タグがなければなにもしない */
        String tempTags = Gear.strHashIncludeNull(ht, "_sys.HIDDEN");
        if (isEmpty(tempTags))
            return;

        // presetFlagが立っていれば、タグ内の %変数% はハッシュ内のデータで実データに置き換えておく
        String tags = tempTags;
        if (presetFlag) {
            tags = replaceTagToData(tempTags, ht);
        }

        Stack stk = new Stack(50);
        String pattern1 = "</form>";
        String pattern2 = "</FORM>";
        int size = html.size();
        int pos = size - 1;
        /*
         * htmlを１行単位で含むVector htmlの要素を末尾からひとつずつ スタックに積み、html からは削除する．その際、
         * </form>のある データかどうかを調べる
         */
        boolean	find	=	false;
        for (int i = 0; i < size; i++) {
            pos = size - 1 - i;
            String str = (String) stk.push(html.get(pos));
            html.removeElementAt(pos);
            if (str.lastIndexOf(pattern1) != -1) {
                find	=	true;
                break;
            } else if (str.lastIndexOf(pattern2) != -1) {
                find	=	true;
                break;
            }
        }
        if(find){
	        /*
	         * タグデータを１行づつhtmlの末尾に追加する
	         */
	        BufferedReader sr = new BufferedReader(new StringReader(tags));
	        String line;
	        try {
	            while ((line = sr.readLine()) != null) {
	                html.add(line);
	            }
	        } catch (IOException e) {
	            System.out.println("★ SuperPrint # addHiddenTags(): ");
	            e.printStackTrace();
	        }
	        /*
	         * スタックのデータをhtmlに戻す
	         */
	        while ((line = (String) stk.pop()) != null) {
	            html.add(line);
	        }
        }else{
	        /*
	         * スタックのデータをhtmlに戻す
	         * <form>のないHtmlは、特殊なケースで、再びシステムに入力となることはないので
	         * タグデータは書かなくてよい
	         */
            String line;
	        while ((line = (String) stk.pop()) != null) {
	            html.add(line);
	        }
        }
    }

    public String replaceTagToData(String temp, Hashtable ht) {

        String tags = substitute(temp, ht, false);
        return tags;
    }

    //
    //	 文字列 dataStr 内の変数をハッシュ表 ht の変数で置き換えた
    //	 結果の文字列を返す
    //
    //	 データを作成するだけなら printData = false を指定する
    //
    public String substitute(String dataStr, Hashtable ht) {
        return substitute(dataStr, ht, true);
    }

    public String substitute(String dataStr, Hashtable ht, boolean printData) {

        setPercent(ht);
        StringBuffer bf = new StringBuffer(1000);
        if (dataStr == null)
            return "";
        //
        StringTokenizer st = new StringTokenizer(dataStr, "%");
        boolean printFlag = true;
        while (st.hasMoreTokens()) {
            String tk = st.nextToken();
            if (tk.charAt(0) == '_') {
                String ps = (String) ht.get(tk); //ハッシュ表を参照
                if (ps == null) {
                    bf.append(""); //ハッシュになければ空白とする
                } else {
                    bf.append(ps);
                }
            } else {
                bf.append(tk);
            }
        }
        String ret = bf.toString(); // そのまま
        if (printData) {
            ret = Cp932.toCp932(ret); // Cp932 にもどす
        }
        return ret;
    }

    //	 ウェブ表示のために
    //	 書き換え文字列中の改行文字(\n)を<br>に置き換える
    public String replaceToBR(String ps) {
        //
        return replace(ps, "\n", "<br>");
    }

    //	 
    //	 文字列 str 中の全ての pattern を replace に置き換える
    public String replace(String str, String pattern, String replace) {
        int s = 0;
        int e = 0;
        StringBuffer result = new StringBuffer();
        //
        while ((e = str.indexOf(pattern, s)) >= 0) {
            result.append(str.substring(s, e));
            result.append(replace);
            s = e + pattern.length();
        }
        result.append(str.substring(s));
        return result.toString();
    }

    //
    //
    //	     2-2. 単に出力するだけのルーチン(パラメータの置き換えをしない)
    //
    //
    public void simplePrint(String template) {
        BufferedReader in;
        String line, _line;
        try {
            // templateは Windows-31J で作成された表示用の HTML ファイル
            in = new BufferedReader(new InputStreamReader(new FileInputStream(template), "Windows-31J"));
            while ((_line = in.readLine()) != null) {
                line = Cp932.toJIS(_line);
                out.println(Cp932.toCp932(line)); // Cp932 に戻してから表示する
                //
            }
            in.close();
            out.close();
            return;

        } catch (IOException e) {
            System.out.println("paramPrint(): can't read parameters.html:"
                    + template);
        }
    }

    //
    //	 ファイルデータをVectorに格納する
    public void loadToVector(String fpath, Vector Vhtml) {
        BufferedReader in = null;
        String line;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(fpath), "Windows-31J"));
            while ((line = in.readLine()) != null) {
                if (line.length() > 0) {
                    Vhtml.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("paramPrint(): can't read :" + fpath);
        }
    }

    //	 ファイルのコピー
    public boolean copyFile(String sfile, String dfile) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    new FileInputStream(sfile), "Windows-31J"));
            PrintWriter out = new PrintWriter(
                    new BufferedWriter(new OutputStreamWriter(
                            new FileOutputStream(dfile), "Windows-31J")));
            //
            String dt;
            while ((dt = in.readLine()) != null) {
                out.println(dt);
            }
            in.close();
            out.close();
        } catch (IOException e) {
            System.out.println(e);
            return false;
        }
        return true;
    }

    //
    //	 メールアドレスとして正しい形式か
    //
    public boolean isMailaddress(String s) {
        //
        // ヌル文字でない
        if (s == null)
            return false;
        //
        // ３文字以上 x@y の文字列である
        int len = s.length();
        if (len < 3)
            return false;
        //
        // @ が含まれいている
        int pos = s.indexOf("@");
        if (pos == -1)
            return false;
        // @ の前後に文字がある
        if (s.endsWith("@"))
            return false;
        if (s.startsWith("@"))
            return false;
        //
        return true;
    }

    //	-------------------------- ファイル削除 --------------------------------
    public boolean makeDir(String dir) {
        return (new File(dir)).mkdirs();
    }

    public boolean delDir(String dir) {
        File fp = new File(dir);
        return deleteDir(fp);
    }

    //	 再帰的にファイルとディレクトリを消す
    public boolean deleteDir(File dir) {
        //
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return dir.delete();
    }

    //	 再帰的にファイルを消す（親ディレクトリは消さない）
    public boolean deleteFiles(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // The directory is now empty so delete it
        return true;
    }

    //	 ----------------------------------------------------------------------
    //
    //	 文字列のnull，空白チェック
    public boolean isSpaceOrNull(String s) {
        if ((s != null) && (s.length() > 0) && !isSpace(s))
            return false;
        return true;
    }

    //	 漢字空白を含んで、全て空白文字かどうかチェックする
    public boolean isSpace(String s) {
        if ((s.length() == 0) || (s == null))
            return false;
        String space = " "; // 空白文字
        //
        String s1;
        int n = s.length();
        for (int i = 0; i < n; i++) {
            s1 = s.substring(i, i + 1);
            if ((!s1.equals(space)) && (!s1.equals("　"))) { //  ascii
                                                             // と漢字の空白文字を比較
                //System.out.println(s + " = not Space");
                return false;
            }
        }
        // System.out.println(s + " = Space !");
        return true;
    }

    //
    //	 文字列が数字がどうかチェックする
    public boolean isDigitx(String s) {
        if ((s == null) || (s.length() == 0))
            return false;
        //
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char ch = s.charAt(i);
            if (!xisDigit(ch))
                return false;
        }
        return true;
    }

    //	 文字列がｎ桁の英字かどうかチェックする
    public boolean isNLetter(String s, int n) {
        if ((s == null) || (s.length() == 0))
            return false;
        if (s.length() != n)
            return false;
        //
        for (int i = 0; i < n; i++) {
            char ch = s.charAt(i);
            if (!xisLetter(ch))
                return false;
        }
        return true;
    }

    //
    //	 文字列がｎ桁の数字がどうかチェックする
    public boolean isNDigit(String s, int n) {
        if ((s == null) || (s.length() == 0))
            return false;
        if (s.length() != n)
            return false;
        //
        for (int i = 0; i < n; i++) {
            char ch = s.charAt(i);
            if (!xisDigit(ch))
                return false;
        }
        return true;
    }

    //	 文字列が英数字からなるかどうかチェックする
    public boolean isHankaku(String s) {
        if ((s == null) || (s.length() == 0))
            return false;
        int n = s.length();
        //
        for (int i = 0; i < n; i++) {
            char ch = s.charAt(i);
            if (!xisLETTERorDigit(ch))
                return false;
        }
        return true;
    }

    //	 文字列がｎ桁以上ｍ桁以下の英数字からなるかどうかチェックする
    public int isHankaku(String s, int min, int max) {
        if ((s == null) || (s.length() == 0))
            return -1;
        int n = s.length();
        //
        if (n < min)
            return -1;
        if (n > max)
            return 1;

        for (int i = 0; i < n; i++) {
            char ch = s.charAt(i);
            if (!xisLETTERorDigit(ch))
                return 9;
        }
        return 0;
    }

    ////////////////////////////////////////////// //
    public boolean xisLetter(char ch) {
        int n = LETTER.length();
        for (int i = 0; i < n; i++) {
            if (ch == LETTER.charAt(i))
                return true;
        }
        return false;
    }

    public boolean xisDigit(char ch) {
        int n = DIGIT.length();
        for (int i = 0; i < n; i++) {
            if (ch == DIGIT.charAt(i))
                return true;
        }
        return false;
    }

    public boolean xisLETTERorDigit(char ch) {
        int n = LETTERorDigit.length();
        for (int i = 0; i < n; i++) {
            if (ch == LETTERorDigit.charAt(i))
                return true;
        }
        return false;
    }

    //
    //	----------------- 日付暗号化用 -------------------------------------
    /**
     * 今日の日付のカレンダーオブジェクトを返す
     */
    public GregorianCalendar currentDay() {
        return new GregorianCalendar();
    }

    /**
     * cal から minutes だけ先のカレンダーオブジェクトを計算して返す
     * 
     * @param cal
     * @param minutes
     * @return
     */
    public GregorianCalendar calculateDay(GregorianCalendar cal, int minutes) {
        return calculateDayM(cal, minutes);
    }

    public GregorianCalendar calculateDayM(GregorianCalendar cal, int minutes) {
        GregorianCalendar cc = calculateDayMinutes(cal, minutes, true);
        return cc;
    }

    /**
     * cal から hours だけ先のカレンダーオブジェクトを計算して返す
     * 
     * @param cal
     * @param hours
     * @return
     */
    public GregorianCalendar calculateDayH(GregorianCalendar cal, int hours) {
        GregorianCalendar cc = calculateDayHour(cal, hours, true);
        return cc;
    }

    /**
     * cal から minutes だけ先（前）のカレンダーオブジェクトを計算して返す
     * 
     * @param cal
     * @param minutes
     * @param sw
     * @return
     */
    public GregorianCalendar calculateDayMinutes(GregorianCalendar cal,
            int minutes, boolean sw) {
        if (sw) {
            cal.add(Calendar.MINUTE, minutes);
        } else {
            cal.add(Calendar.MINUTE, -1 * minutes);
        }
        return cal;
    }

    /**
     * cal から hour だけ先（前）のカレンダーオブジェクトを計算して返す
     * 
     * @param cal
     * @param hours
     * @param sw
     * @return
     */
    public GregorianCalendar calculateDayHour(GregorianCalendar cal, int hours,
            boolean sw) {
        if (sw) {
            cal.add(Calendar.HOUR_OF_DAY, hours);
        } else {
            cal.add(Calendar.HOUR_OF_DAY, -1 * hours);
        }
        return cal;
    }

    /**
     * 特定の日付を yyyy-mm-dd-HH-MM の形ｮの文字列で返す
     * 
     * @param date
     * @return
     */
    public String CalToStr(GregorianCalendar date) {
        return CalToStr(date, true);
    }

    public String CalToStr(GregorianCalendar date, boolean sw) {
        int _yy = date.get(Calendar.YEAR);
        int _mm = date.get(Calendar.MONTH) + 1;
        int _dd = date.get(Calendar.DATE);
        int _HH = date.get(Calendar.HOUR_OF_DAY); // 2003.2.15
        int _MM = date.get(Calendar.MINUTE);
        //
        String yy = String.valueOf(_yy); // 4桁固定
        String mm = get00type(_mm);
        String dd = get00type(_dd);
        String HH = get00type(_HH);
        String MM = get00type(_MM);
        //
        String datestr = yy + mm + dd + HH + MM;
        if (!sw)
            datestr = yy + "-" + mm + "-" + dd + "/ " + HH + ":" + MM;
        return datestr;
    }

    /** 時間だけ返す */
    public String CalToStrShort(GregorianCalendar date, boolean sw) {
        int _HH = date.get(Calendar.HOUR_OF_DAY); // 2003.2.15
        int _MM = date.get(Calendar.MINUTE);
        //
        String HH = get00type(_HH);
        String MM = get00type(_MM);
        //
        String datestr = HH + MM;
        if (!sw)
            datestr = HH + ":" + MM;
        return datestr;
    }

    /**
     * 日付文字列を秒単位までのＣＳＶ形式で返す 月の値はｰ1されている事に注意
     * 
     * @param date
     *            日付
     * @return ＣＳＶ文字列 例：2004,10,02,13,12,22
     */
    public String timeCsv(GregorianCalendar date) {
        int _yy = date.get(Calendar.YEAR);
        int _mm = date.get(Calendar.MONTH);
        int _dd = date.get(Calendar.DATE);
        int _HH = date.get(Calendar.HOUR_OF_DAY); // 2003.2.15
        int _MM = date.get(Calendar.MINUTE);
        int _SS = date.get(Calendar.SECOND);
        //
        String yy = String.valueOf(_yy); // 4桁固定
        String mm = get00type(_mm);
        String dd = get00type(_dd);
        String HH = get00type(_HH);
        String MM = get00type(_MM);
        String SS = get00type(_SS);
        //
        String datestr = yy + "," + mm + "," + dd + "," + HH + "," + MM + ","
                + SS;
        return datestr;
    }

    // 年、月で特定される日付けを CalToStr と同じ文字列にして返す
    public String CalToStr_app(int yy, int mm, boolean sw) {
        GregorianCalendar theDay = new GregorianCalendar(yy, mm - 1, 1, 0, 0, 0); // yy年mm月1日0時0分0秒
        return CalToStr(theDay, sw);
    }

    //現在の日付の文字列を得る
    public String getDate_short() {
        String dt = getDate("yyyy/MM/dd HH:mm:ss");
        return dt.substring(2); // " yy/mm/dd hh:mm:ss " の 17 文字
    }

    public String getDate() {
        return getDate("yyyy年MM月dd日HH時mm分ss秒");
    }

    // yyyyMMddHH などを指定する
    public String getDate(String form) {
        SimpleDateFormat format = new SimpleDateFormat(form);
        String strDate = format.format(Calendar.getInstance().getTime());
        return strDate;
    }

    /** 指定した日付について、指定した書式の日付文字列を得る */
    public String getFormattedDate_short(GregorianCalendar cal) {
        String dt = getFormattedDate(cal, "yyyy/MM/dd HH:mm:ss");
        return dt.substring(2); // " yy/mm/dd hh:mm:ss " の 17 文字
    }

    /** カレンダーオブジェクトから form で指定した書式の日付文字列を得る */
    public String getFormattedDate(Calendar cal, String form) {
        SimpleDateFormat format = new SimpleDateFormat(form);
        String strDate = format.format(cal.getTime());
        return strDate;
    }

    // うるう年かどうか(すこしいい加減)
    public boolean isLeapYear(String yy) {
        int ck = Integer.parseInt(yy) % 4;
        return ck == 0;
    }

    //
    public String get00type(int s) {
        String dt = String.valueOf(s);
        if ((dt == null) || (dt.length() == 0))
            return "00";
        int pos = dt.length();
        String pattern = "00" + dt;
        return pattern.substring(pos);
    }

    public String get000type(int s) {
        String dt = String.valueOf(s);
        if ((dt == null) || (dt.length() == 0))
            return "000";
        int pos = dt.length();
        String pattern = "000" + dt;
        return pattern.substring(pos);
    }

    // 先頭を空白で埋めて２桁の整数文字にする(末尾に空白を１個付加)
    public String getSStype(int s) {
        String dt = String.valueOf(s);
        if ((dt == null) || (dt.length() == 0))
            return "00";
        int pos = dt.length();
        String pattern = "  " + dt + " ";
        return pattern.substring(pos);
    }

    // 先頭を空白で埋めて２桁の整数文字にする
    public String getS2type(int s) {
        String dt = String.valueOf(s);
        if ((dt == null) || (dt.length() == 0))
            return "00";
        int pos = dt.length();
        String pattern = "  " + dt;
        return pattern.substring(pos);
    }

    // 先頭を０で埋めて 2 桁の整数にする
    public String get00String(String s) {
        //
        String dt = s.trim();
        if ((dt == null) || (dt.length() == 0))
            return "00";
        int pos = dt.length();
        String pattern = "00" + dt;
        return pattern.substring(pos);
    }

    // 先頭を０で埋めて 3 桁の整数にする
    public String get000String(String s) {
        //
        String dt = s.trim();
        if ((dt == null) || (dt.length() == 0))
            return "000";
        int pos = dt.length();
        String pattern = "000" + dt;
        return pattern.substring(pos);
    }

    //---------------------------------------------------------------------------------
    //
    // メッセージを出力してクライアントに返す
    // プログラムはここで終了する
    public static void errPrint(PrintWriter out, String str) {
        out.println("<html>");
        out.println("<head><title>Servlet1</title></head>");
        out.println("<body>");
        out.println("* * *  致命的なエラーのために処理を中止しました  * * *<p>");
        out.println(str);
        out.println("</body></html>");
        out.close();
    }

    // 同上．ただしインスタンスメソッド
    public void errPrint(String str) {
        out.println("<html>");
        out.println("<head><title>Servlet1</title></head>");
        out.println("<body>");
        out.println("* * *  致命的なエラーのために処理を中止しました  * * *<p>");
        out.println(str);
        out.println("</body></html>");
        out.close();
    }

    /**
     * 文字列の空テスト
     * 
     * @param str
     * @return
     */
    public boolean isEmpty(String str) {
        if (str == null)
            return true;
        if (str.length() == 0)
            return true;
        return false;
    }

    /**
     * 文字列の空テスト. SuperPrint の置き換えではkeyがnullの場合key名が値となるので こうしていたが、現在は
     * keyがnullの場合 "" が値となるので、実際には不要 (2005.02) SuperPrint の置き換え用
     * 
     * @param str
     * @param keyNmae
     * @return
     */

    public boolean isEmpty(String str, String keyNmae) {
        if (str == null)
            return true;
        if (str.length() == 0)
            return true;
        if (str.equals(keyNmae))
            return true; // SuperPrint の置き換えではkeyがnullの場合key名が値となるので
        return false;
    }

    /**
     * 文字列の空テスト． 空白をtrimしてテストする
     * 
     * @param str
     * @return
     */
    public boolean isEmptyData(String str) {
        if (str == null)
            return true;
        if ((str.trim()).length() == 0)
            return true;
        return false;
    }

    //
    /**
     * 返す値がnullだったら""を返す
     * 
     * @param htb
     * @param key
     * @return
     */
    public static String strHash(Hashtable htb, String key) {
        //
        if (htb == null) {
            System.out.println("★htb is null : SuperPrint#strHash()");
        }
        if (key == null) {
            System.out.println("★key is null : SuperPrint#strHash()");
        }
        String str = (String) htb.get(key);
        if (str == null) {
            return "";
        }
        return str;
    }

    /**
     * 返す値がnullだったら "0" を返す
     * 
     * @param htb
     * @param key
     * @return
     */
    public String strHashZero(Hashtable htb, String key) {
        //
        String str = (String) htb.get(key);
        if (isEmpty(str))
            return "0";
        return str;
    }

    /**
     * 返す値がnullだったらそのままnullを返す
     * @param htb
     * @param key
     * @return
     */
    public String strHashWithNullAlart(Hashtable htb, String key, String comment) {
        //
        String str = (String) htb.get(key);
        if (comment != null) {
            if ((LOG.fa) && (str == null)) {
                LOG.println("★ key =  " + key + "/ " + comment);
                LOG.println("★★★★★★★★★★★★★★★★★★★★★★★★★★");
                LOG.println("★★   　　　　　　　　　　　　　　　　　　　　   ★★");
                LOG.println("★★   ハッシュから取り出したデータは NULL です   ★★");
                LOG.println("★★   　　　　　　　　　　　　　　　　　　　　   ★★");
                LOG.println("★★★★★★★★★★★★★★★★★★★★★★★★★★★");
            }
        }
        return str;
    }

    /**
     * 削除予定の古いメソッド
     */

    /**
     * パラメータの置き換えをしない単なるプリント
     * 入力ファイルは Windows-31J
     * 
     * @param filePath
     */
    public void print(String filePath) {
        BufferedReader in;
        String line, _line;
        try {
            // templateは Windows-31J で作成された表示用の HTML ファイル
            in = new BufferedReader(new InputStreamReader(new FileInputStream(
                    filePath), "Windows-31J"));
            while ((_line = in.readLine()) != null) {
                line = Cp932.toJIS(_line);
                out.println(Cp932.toCp932(line)); // Cp932 に戻してから表示する
            }
            in.close();
            out.close();
            return;

        } catch (IOException e) {
            System.out.println("paramPrint(): can't read " + filePath);
        }
    }

    /**
     * String を入力データとして書き換えを行う
     * @param buf
     * @param htb
     */
    public void strPrint(String buf, Hashtable htb) {
        strPrint(buf, htb, true);
    }

    /**
     * 
     * @param buf
     * @param htb
     * @param notmail
     */
    public void strPrint(String buf, Hashtable htb, boolean notmail) {
        //
        BufferedReader in = new BufferedReader(new StringReader(buf));
        //
        Vector html = new Vector(100);
        String line;
        try {
            while ((line = in.readLine()) != null) {
                html.add(line);
            }
            in.close();
        } catch (IOException e) {
        }
        printVector(html, htb, notmail);
    }

    /**
     * 反復を伴わない書き換え－１（変数の置き換えだけ）
     * このメソッドのみを使う時は、write()などのオーバーライドは不要
     * 
     * @param fname
     * @param htb
     */
    public void repPrint(String fname, Hashtable htb) {
        repPrint(fname, htb, true);
    }

    /**
     * 反復を伴わない書き換え－１（変数の置き換えだけ）
     * 
     * @param fname
     * @param htb
     * @param notmail
     */
    public void repPrint(String fname, Hashtable htb, boolean notmail) { // メール用の時 false を
        //
        if (fname == null)
            return;
        //
        setPercent(htb);
        BufferedReader in, in2;
        String _line;
        String line;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(
                    fname), "Windows-31J"));
            boolean printFlag;
            while ((_line = in.readLine()) != null) {
                line = Cp932.toJIS(_line); // 一度、JISに基準化
                //パラメータを切り出す
                StringTokenizer st = new StringTokenizer(line, "%");
                String dt = "";
                printFlag = true;
                while (st.hasMoreTokens()) {
                    String tk = st.nextToken();
                    String tk2 = "   ";// ダミーを入れておく
                    if (tk.charAt(0) == '_') {
                        if ((tk.charAt(1) == '$') && (tk.length() > 2)) {
                            tk2 = tk; // コピーを保存して
                            tk = "_" + tk.substring(2); // $ 記号を取り除く
                        }
                        String ps = strHash(htb, tk); //ハッシュ表を参照
                        if (ps == null) {
                            //dt += tk;//ハッシュになければ変換しない
                            dt += "";
                        } else {
                            if (tk2.charAt(1) == '$') { // 常に参照するので tk2 は null ではいけない
                                ps = replaceToBR(ps); // 2003.9.2 データ中の改行を<br>に変換する
                            }
                            //dt += Cp932.toCp932(ps);			// 2002.9.28 修正.クライアントはWindowsを仮定
                            dt += ps; // 最後の出力部分で全部を対象にすることに変更した
                            //if(DBG.fa){
                            //	if( (ps.length()>5)&&(ps.charAt(0)=='0')&&(ps.charAt(1)=='9')){
                            //		DBG.println("□output■" + ps + "=" + Gear.toHexString(ps));
                            //	}
                            //}                            

                        }
                    } else {
                        dt += tk;
                    }
                }

                // １行分の書き出し
                if (printFlag) {
                    if (notmail) {
                        //if(DBG.fa){
                        //	if( dt.indexOf("textarea") > 0){
                        //		DBG.println("□output last□" + Cp932.toCp932(dt) + "=" + Gear.toHexString(Cp932.toCp932(dt)));
                        //	}
                        //} 
                        out.println(Cp932.toCp932(dt)); // Cp932 にもどしてから表示する
                    } else {
                        out.println(dt); // そのまま書く
                    }
                }
            }
            in.close();
        } catch (IOException e) {
            System.out.println("class SuperPrint #Print(): can't read file: "
                    + fname);
        }
    }

    /**
     * 反復を伴わない書き換え－２(＠で指定する個別の置き換えも行う)
     * @param fname
     * @param htb
     */
    public void optPrint(String fname, Hashtable htb) {
        optPrint(fname, htb, true);
    }

    public void optPrint(String fname, Hashtable htb, boolean notmail) { // メール用の時 false を
        //
        if (fname == null)
            return;
        //
        setPercent(htb);
        BufferedReader in, in2;
        String _line;
        String line;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(
                    fname), "Windows-31J"));
            boolean printFlag;
            while ((_line = in.readLine()) != null) {
                line = Cp932.toJIS(_line); // 一度、JISに基準化
                //パラメータを切り出す
                StringTokenizer st = new StringTokenizer(line, "%");
                String dt = "";
                printFlag = true;
                while (st.hasMoreTokens()) {
                    String tk = st.nextToken();
                    String tk2 = "   ";// ダミーを入れておく
                    if (tk.charAt(0) == '_') {
                        if ((tk.charAt(1) == '$') && (tk.length() > 2)) {
                            tk2 = tk; // コピーを保存して
                            tk = "_" + tk.substring(2); // $ 記号を取り除く
                        }
                        String ps = strHash(htb, tk); //ハッシュ表を参照
                        if (ps == null) {
                            //dt += tk;//ハッシュになければ変換しない
                            dt += "";
                        } else {
                            if (tk2.charAt(1) == '$') { // 常に参照するので tk2 は null ではいけない
                                ps = replaceToBR(ps); // 2003.9.2 データ中の改行を<br>に変換する
                            }
                            //dt += Cp932.toCp932(ps);			// 2002.9.28 修正.クライアントはWindowsを仮定
                            dt += ps; // 最後の出力部分で全部を対象にすることに変更した
                        }
                    } else if (tk.charAt(0) == '@') {//特殊な処理
                        write(tk.substring(1), htb); // 個別プログラムごとに特殊な処理
                        printFlag = false; // 標準の出力をしない
                    } else {
                        dt += tk;
                    }
                }
                // １行分の書き出し
                if (printFlag) {
                    if (notmail) {
                        out.println(Cp932.toCp932(dt)); // Cp932 にもどしてから表示する
                    } else {
                        out.println(dt); // そのまま書く
                    }
                }
            }
            in.close();
        } catch (IOException e) {
            System.out.println("class SuperPrint #Print(): can't read file: "
                    + fname);
        }
    }

    //////////////////////////////////////////////////////////////////////////	/
    //
    //	  オーバーロードされるメソッド
    //
    //////////////////////////////////////////////////////////////////////////	/

    //
    //	 ハッシュテープル(htb)を使って key で特定される出力処理を行う
    //
    public void write(String key, Hashtable htb) {
        System.out
                .println("★ class SuperPrint #write()-1 : このメソッドはオーバーライドしてください．");
        System.out.println("   key=" + key);
    }

    //	 ハッシュテーブル(htb)を使って部分的に切り取ったソースデータ（exHtml）
    //	 の内容を置き換えて出力処理する．表などの反復出力に利用するが個々の処理
    //	 内容は、key で特定される．
    //
    public void write(String key, Vector exHtml, Hashtable htb) {

        System.out
                .println("★ class SuperPrint #write()-2 : このメソッドはオーバーライドしてください．");

    }

    //	 部分的に切り取ったソースデータ（exHtml）を出力するか否か、ハッシュテーブル
    //	 (htb)を使って判断し、出力する場合には、htb を使ってexHtml の内容を書き換えて
    //	 出力する．個々の処理内容は、key で特定される．
    //
    public void blockWrite(String key, Vector exHtml, Hashtable htb) {

        System.out
                .println("★ class SuperPrint #blockWrite() : このメソッドはオーバーライドしてください．");

    }

    /**
     * デバッグ用
     * 
     * @param arg
     */
    static public void main(String[] arg) {

        /*
         Vector	v		=	new Vector();
         String	path	=	"e:\\temp\\test.html";
         SuperPrint	sp	=	new	SuperPrint(new PrintWriter(System.out));

         sp.loadToVector(path, v);
         if(v.size()==0){
         System.out.println("data is empty!");
         }
         Hashtable	ht	=	new Hashtable();
         sp.printVector(v,ht);
         */

        /*
         String	temp1	=	"<pct: @alpha>";
         String	temp2	=	"<pct: alpha>";
         String	temp3	=	"< pct : /alpha-1 >";
         
         SuperPrint	sp	=	new	SuperPrint(new PrintWriter(System.out));
         System.out.println("temp1=" + sp.getTagName(temp1));
         System.out.println("temp2=" + sp.getTagName(temp2));
         System.out.println("temp3=" + sp.getTagName(temp3));
         */
    }

}