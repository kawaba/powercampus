/*
     汎用の書き換えクラス

	 templateは Windows-31J で作成された表示用のHTMLファイル
     置き換える文字列は %_tx_userName% のように、％文字で挟んだパラメータ名である。

*/
package epml;
import	epml.tools.*;
import java.io.*;
import java.util.*;
//
public class PrintView extends Object {
	//
	boolean			crlfFlag;
	PrintWriter		out;
	//
	public	PrintView(PrintWriter _out){
		crlfFlag = true;
		out	= _out;
	}
	public	void crlf_off()	{ crlfFlag = false; }
	public	void crlf_on()	{ crlfFlag = true;  }
	//
	//
	public	void PrintString(Hashtable htb,String buf){
		PrintString(htb,buf, null,true);
	}
	public	void PrintString(Hashtable htb,String buf,OptionPrint prt){
		PrintString(htb,buf, prt,true);
	}
	public	void PrintString(Hashtable htb,String buf,OptionPrint prt,boolean notmail){
		//
		BufferedReader in = new BufferedReader(new StringReader(buf));
		//
		Vector  html	= new Vector(100);
        String	line;
        try{
			while((line=in.readLine())!=null){
				html.add(line);
			}
			in.close();
		}catch(IOException e){
		}
		PrintVector(htb, html, prt,notmail);
		//
	}
	//
	//
    public void Print(Hashtable htb,String template, OptionPrint prt){
        Print( htb, template, prt,true);
    }
    public void Print(Hashtable htb,String template, OptionPrint prt,boolean notmail){	// メール用の時 false を
		//
        if(template==null)	return;
		//
		BufferedReader  in,in2;
        String          _line;
        String          line;
        try{
            in = new BufferedReader(new InputStreamReader(new FileInputStream(template),"Windows-31J"));
            boolean printFlag;
			while((_line=in.readLine())!=null){
                line = Cp932.toJIS(_line); // 一度、JISに基準化
                //パラメータを切り出す
                StringTokenizer st = new StringTokenizer(line,"%");
                String dt = "";
                printFlag = true;
                while(st.hasMoreTokens()){
                    String tk = st.nextToken();
                    if(tk.charAt(0)=='_'){
                        String ps = strHash(htb,tk); //ハッシュ表を参照
                        if(ps==null){
                            dt += tk;//ハッシュになければ変換しない
                        }else{
                            //dt += Cp932.toCp932(ps);  // 2002.9.28 修正.クライアントはWindowsを仮定
                            dt += ps;                   // 最後の出力部分で全部を対象にすることに変更した
                        }
                    }else if(tk.charAt(0)=='@'){//特殊な処理
                        prt.write(tk.substring(1),out,htb); // 個別プログラムごとに特殊な処理
                        printFlag = false; // 標準の出力をしない
                    }else{
                        dt += tk;
                    }
                }
                // １行分の書き出し
				if(printFlag){
                    if(notmail){
                        out.println(Cp932.toCp932(dt));     // Cp932 にもどしてから表示する
                    }else{
                       out.println(dt);    // そのまま書く
                    }
                }
            }
            in.close();
        }catch (IOException e){
            System.out.println("paramPrint(): can't read file: " + template);
        }
    }
	//
    //
    // ファイルではなくVectorからHTMLを読み出すタイプ
    // 
    // 繰り返しのある特殊な書き換えのときもこちらを使う
    public void PrintVector(Hashtable htb,Vector html,OptionPrint prt){
        PrintVector( htb, html, prt,true);
    }
    public void PrintVector(Hashtable htb,Vector html,OptionPrint prt,boolean notmail){
		//System.out.println("■PrintVector()");
		//
        BufferedReader  in,in2;
        String          line,_line;
        // templateは Windows-31J で作成された表示用の HTML ファイル
        // 置き換える文字列は %_tx_userName% のように、％文字で挟んだ
        // パラメータ名である。パラメータの増減があっても、template を修正するだけでよい。
        if(html        == null) {System.out.println("** paramPrintByVector() : 雛型の文字列が空である");   return;}
        if(html.size() == 0)    {System.out.println("** paramPrintByVector() : 雛型の文字列が１件もない"); return;}
        int n = html.size();
        //
        boolean printFlag;
        for(int k=0; k<n; k++){
            line = Cp932.toJIS((String)(html.get(k))); // 一度、JISに基準化
            //
			// htb にある指定された変数が true ならば、
			// あらかじめコメント行として作成しておいたHTMLの１行を直下の１行と置き換える
			// 変数名はコメント行に書き込んである　(2003.4.11 追加)
			//
			if((line!=null)&&(line.length() > 4)&&(line.charAt(0)=='<')&&(line.charAt(1)=='!')){ // コメント行の検出
				//System.out.println("■Do replaceChk()");
				boolean rflag = replaceChk(line,htb);  // 置き換えるのかどうか
				if(rflag){
					k++; line = Cp932.toJIS((String)(html.get(k))); // 次の行
					k++;	// --> を消す
					k++;	// 置き換える行を消す
				}
			}
			//
			//パラメータを切り出す
			StringTokenizer st 		= new StringTokenizer(line,"%");
            String dt = "";
            printFlag = true;
            while(st.hasMoreTokens()){
				String tk 	= st.nextToken();
                String tk2	= "   ";// ダミーを入れておく
				if(tk.charAt(0)=='_'){
                    if( (tk.charAt(1)=='$')&&(tk.length()>2) ){
						tk2	= tk;						// コピーを保存して
						tk	= "_" + tk.substring(2);	// $ 記号を取り除く
					}
					String ps = (String)htb.get(tk); //ハッシュ表を参照
                    if(ps==null){
                        dt += tk;//ハッシュになければ変換しない
                    }else{
						if(tk2.charAt(1)=='$'){		// 常に参照するので tk2 は null ではいけない
							ps = replaceToBR(ps);	// 2003.9.2 データ中の改行を<br>に変換する
						}
                        //dt += Cp932.toCp932(ps);  // 2002.9.28 修正.クライアントはWindowsを仮定
                        dt += ps;                   // 最後の出力部分で全部を対象にすることに変更した
                    }
                //特殊な処理（反復を含む）
				}else if(tk.charAt(0)=='@'){
                    
					if(dt.length() !=0) printOut(printFlag,notmail,dt);	// 現在までの分を出力する
					dt = "";
					
					
					Vector exHtml = new Vector(20,10);                  // 反復用雛型データを入れるベクター
                    String chk    = ((String)(html.get(k+1))).trim();  	// 先読みをしてみる．cp932() 不要
					if(chk.equals("<<")){
						// 反復指定あり 
						//
                        int	ckFlag = 0;
						for(k=k+1 ; k< n; k++) {// 次行から先を読む．cp932() 不要
                            String str= ((String)(html.get(k))).trim(); 
							//
							if(str.equals("<<")){// 反復開始指示子
								if(ckFlag==0){
									ckFlag++;		// "<<" は捨てる
								}else{
									ckFlag++;
									exHtml.add(str); // "<<" 自体を exHtml へ格納（再帰）
								}
							}else if(str.equals(">>")){// 反復終了指示子
								ckFlag--;
								if(ckFlag==0){
									break;	// ">>" は捨てる．取り込み終了
								}else{
									exHtml.add(str); // ">>" 自体を exHtml へ格納（再帰）
								}
							}else{
								exHtml.add(str);
							}
                        }
						prt.write(tk.substring(1),out,htb,exHtml); // 個別プログラムごとに特殊な処理
						//
						printFlag = false; // 標準の出力をしない
						//
					}else{
						// 反復指定なし
						//
		                prt.write(tk.substring(1),out,htb);  // 個別プログラムごとに特殊な処理(exHtml は不要)
					}
                    //printFlag = false; // 標準の出力をしない
                	//
				// ブロックを再帰処理する（ブロック全体を出力しない処理を含む）
				}else if(tk.charAt(0)=='#'){// %#(label)% から #end までをブロックとして取り出す．paramPrintBLK() でブロックの出力の可否を判断
					//
                    Vector exHtml = new Vector(50,10);                              // 反復用雛型データを入れるベクター
                    //
					// #end が出現するまで全てを exHtml に取り込む
					for(k=k+1 ; k< n; k++) {                                        // 次行から
                        String str= ((String)(html.get(k))).trim();   				// 先を読む．cp932() 不要
                        if(str.equals("#end"))  break;            					// 終端
                        exHtml.add(str);  											// << 以下にデータがなかったときのためk+1から始めるので
                    }
					prt.blockWrite(tk.substring(1),out,htb,exHtml); // このブロックを出力するか否かを決める．出力にはPrintByVector()を再帰的に呼び出す 
                    printFlag = false; // 標準の出力をしない
                }else{
                    dt +=  tk;
                }
			}
			printOut(printFlag,notmail,dt);
        }
    }
	
	void	printOut(boolean printFlag,boolean notmail,String dt){
		if(printFlag){
        	if(notmail){
            	if(crlfFlag){
					out.println(Cp932.toCp932(dt));   // Cp932 にもどしてから表示する（改行あり）
				}else{
					out.print(Cp932.toCp932(dt));     // Cp932 にもどしてから表示する（改行なし）
				}
			}else{
				if(crlfFlag){
                   	out.println(dt);    // そのまま書く
				}else{
					out.print(dt);
				}
            }
		}
	}
	
	//
	// 文字列 line 内の変数をハッシュ表 ht の変数で置き換えた
	// 結果の文字列を返す
	String substitute(String line,Hashtable ht){
		StringBuffer bf = new StringBuffer(1000);
		if(line==null) return "";
		//
        StringTokenizer st = new StringTokenizer(line,"%");
        boolean printFlag = true;
        while(st.hasMoreTokens()){
            String tk = st.nextToken();
            if(tk.charAt(0)=='_'){
                String ps = (String)ht.get(tk); //ハッシュ表を参照
                if(ps==null){
                    bf.append(tk);	//ハッシュになければ変換しない
                }else{
                    bf.append(ps);
                }
            }else{
                bf.append(tk);
            }
        }
		return Cp932.toCp932(bf.toString());
	}
	// ウェブ表示のために
	// 書き換え文字列中の改行文字(\n)を<br>に置き換える
    String replaceToBR(String ps){
		//
		return replace(ps,"\n","<br>");
	}
	// 
	// 文字列 str 中の全ての pattern を replace に置き換える
	String replace(String str, String pattern, String replace) {
        int s = 0;
        int e = 0;
        StringBuffer result = new StringBuffer();
    	//
        while ((e = str.indexOf(pattern, s)) >= 0) {
            result.append(str.substring(s, e));
            result.append(replace);
            s = e+pattern.length();
        }
        result.append(str.substring(s));
        return result.toString();
    }
	
	//
	////////////////////////////////////////////////////////////////////////
	//
	//　>>>>>  行データの入れ替えチェック  <<<<<
	//
	//  直前のコメントブロック内に代替のHTMLを配しておき、フラグが true の
	//  とき、該当の１行をこの代替行に置き換える。フラグ名はコメント内に
	//　書き込まれている。
	//
	//  replaceChk() は、このフラグを取り出し、ハッシュ（htb） をチェックし
	//  てtrue か false を返す。
	//
	////////////////////////////////////////////////////////////////////////
	
	/*
	<!--$_fileflag
	      <td width="38" valign="middle" align="center">　</td>
	-->
	      <td width="10" valign="middle" align="center">　</td><td width="28" valign="middle" align="center">　</td>
	*/
	// <!--$_fileflag  などの中から、 _fileflag の部分を取り出し
	// その名前をキーとして htb から値を検索して返す
	// キーが定義されてないか値が "" であれば false を返す
	//
	boolean replaceChk(String line,Hashtable htb){
		//
		StringTokenizer st = new StringTokenizer(line,"$");
        if(st.countTokens() < 2) {
			return false;
		}
		//
		String dt = st.nextToken();			// <!-- 
        dt = st.nextToken();				// _fileflag
		String fl = strHash(htb,dt);		// 値は "" でなければなんでもいい
		if(fl==null) 			{  return false;}
		if(fl.length() == 0)	{  return false;}
		return	true;
	}
    //
    //
    //     2-2. 単に出力するだけのルーチン(パラメータの置き換えをしない)
    //
    //
    public void simplePrint(String template){
        BufferedReader  in;
        String          line,_line;
        try{
            // templateは Windows-31J で作成された表示用の HTML ファイル
            in = new BufferedReader(new InputStreamReader(new FileInputStream(template),"Windows-31J"));
            while((_line=in.readLine())!=null){
                line = Cp932.toJIS(_line);
                out.println(Cp932.toCp932(line));   // Cp932 に戻してから表示する
                //
            }
            in.close();
            out.close();
			return;
			
        }catch (IOException e){
            System.out.println("paramPrint(): can't read parameters.html:"+template);
        }
    }
	//
	//
	String strHash(Hashtable htb,String key){
		//
		String	str	= (String) htb.get(key);
		/*
		if(str==null){
			DBG.println("★★★★★★★★★★★★★★★★★★★★★★★★★★★");
			DBG.println("★★   　　　　　　　　　　　　　　　　　　　　   ★★");
			DBG.println("★★   ハッシュから取り出したデータは NULL です   ★★");
			DBG.println("★★   key = " + key );
			DBG.println("★★★★★★★★★★★★★★★★★★★★★★★★★★★");
		}
		*/
		return str;
	}
}