package _epml;

import epml.EpmlTokenException;
import epml.tools.Csv;
import epml.tools.DBG;

/**
 * EPML文のパーサ。
 * getToken()メソッドを呼び出すと、EpmlTokenオブジェクトを返す
 * EpmlTokenクラスはすべてのトークンクラスのスーパークラス
 *  
 * @author Takashi Kawaba
 */

public class EpmlTokenizer {
	/*
	 * EPML文のバッファ
	 */
	private String buffer;			// 問題原文のあるバッファ
	private int max;				// 問題原文の長さ
	private int p;					// バッファ内の現在位置を指すポインタ（ゼロオリジン）
	/*
	 * トークンオブジェクト作成のために参照される変数
	 */
	private Integer hai_ten = 10; 			// 現在の配点。デフォルトは10点
	private Integer groupNumERB = 0;		// グループ番号（ERB 用：複数の選択項目からなる　：ラジオボタン、チェックボックス）
	private Integer groupNumENL = 0;		// グループ番号（ENL 用：複数のフィールドからなる：埋め込み型の番号記入フィールド）
		
	private boolean flag_ERB = false;		// グループ内ならtrue : 最初はfalseにしておく
	private boolean flag_ENL = false;		// グループ内ならtrue : 最初はtrueにしておく

	private String note;					// トークン記号（埋め込み用） ex. ENL(1)、ERB(1, 2)など
	private String value;					// 制御記号を含まない文字列（EPMLの属性データ部分）

	// トークンごとの一連番号のカウンタ
	int nNUM=0;		// Number				問題番号
	int nENL=0;		// EmbededNumberLists 	番号選択
	int nEWL=0;		// EmbededWordLists		文を選択する
	int nETF=0;		// EmbededTextFields	１行テキスト入力
	int nERB=0;		// EmbededRadioButtons　ラジオボタン、チェックボックス
	int nETA=0;		// EmbededTextArea		記述式

	int nEPT=0;		// 配点 EmbededPoinT			
	int nTEXT=0;	// 一般テキスト
	int nADMY=0;	// ダミー選択肢リスト
	int nSEL=0;		// 選択肢一覧表

	/**
	 * コンストラクタ
	 * 
	 * @param buffer
	 */
	public EpmlTokenizer(String buffer) {
		this.buffer = buffer;
		max = buffer.length();
		p = 0;
	}

	/**
	 * ■ EPML文からトークン（EpmlToken）を取り出して返す
	 * 　 繰り返し呼び出すことですべてのトークンを取り出せる
	 *    トークンがなくなるとnullを返す
	 * 
	 * @return EpmlToken トークンオブジェクト。トークンがなくなった場合はnull
	 * @throws EpmlTokenException
	 */
	public EpmlToken getToken() throws EpmlTokenException {

		try {

			char tokenId = nextTokenID();

			if(tokenId==NUM) {
				/*
				 * $[1/]のような問題番号
				 * getValue()でトークン（tkn）の値（ex,"1")を、変数valueに代入する
				 * 問題番号の出現回数を+1する
				 * NUM(1,0,0)のようなノートを作成する
				 * NUMでは、グループ番号と項目連番は常に0とする
				 * NumberTokenオブジェクトを作成して返す
				 */
				value = getValue();			// トークンのvalue部分を得る
				++nNUM;						// 番号の出現回数を+1する
				note = _NUM + nNUM + ",0,0" + RBLACKET;  // ex. NUM(1,0,0)
				
				return new NumberToken(NoteType.NUM,note,value, nNUM, 0, 0);
				
			}else if(tokenId == ENL_EWL) { 

				flag_ERB = false;
				
				/* ![～/] ドロップダウンリストか選択肢問題
				 * valueをコンマで分割して複数の値項目からなるかどうか調べる
				 * ex. "100,105,200" 
				 * 
				 * 複数項目ならドロップダウンリスト問題、単一項目なら番号選択問題になる
				 * 
				 */
				value = getValue();
				Csv cs = new Csv(value, ",|");	// デリミッタ

				if(cs.notZeroLengthCount() > 1) {
					/*
					 * 空白ではない選択肢が２つ以上ある場合（ドロップダウンリスト）
					 * ドロップダウンリストのカウンタを+1し、ノートを作成する
					 * EwlTokenオブジェクトを作成して返す
					 */
					++nEWL;
					note = _EWL + nNUM + ",0," + nEWL + RBLACKET;		// ex. EWL(3,0,2) 　2はシーケンス番号
					return new EwlToken(NoteType.EWL, note, value, nNUM, 0, nEWL,hai_ten);

				} else {

					/* 正解1つしかないので、文末の選択肢一覧から該当の番号をテキストボックスに書き込む形式
					 * ENL(1)のような埋め込み記号を作成
					 * 数値は問題中のシーケンス番号
					 * また、idに種別を表す番号EWL（=1)をセットする */

					++nENL;
					if(!flag_ENL) {				// 新しい問題文の始まり（解答の選択肢群が出現した時、終わりの目印として、falseになる）
						flag_ENL = true;		// ひとつ要素を加えるので true にする
						++groupNumENL;			// 新しいグループになるのでグループ番号を１増やす（１から始まる）
						nENL = 1;				// 要素番号もクリアしておく（１オリジン）
					}
					if(value.length() == 0) {
						throw new EpmlTokenException(p, "選択語が指定されていません．");
					}
					note = _ENL + nNUM + "," + groupNumENL + "," + nENL + RBLACKET;
					return new EnlToken(NoteType.ENL,note,value,nNUM, groupNumENL, nENL, hai_ten);
					
				}

			} else if(tokenId == ETF) { 
				/* !{ /}
				 * 文中に埋め込んだテキストボックスに解答語句を書き込む形式
				 * ETF(2)のような埋め込み記号を作成
				 * 数値は問題中のシーケンス番号
				 * idにETF（=3）をセットする */

				value = getValue();
				++nETF;
				note = _ETF + nNUM + ",0,"+ nETF + RBLACKET;
				flag_ERB = false;
				
				return new EtfToken(NoteType.ETF,note, value, nNUM, 0, nETF, hai_ten);
				
				 
			} else if(tokenId == ERB) {	// SEL='s'
				/* #[ /]
				 * 複数行のリストから、ラジオボタンで正解をチェックする形式．
				 * 複数の正解語があるとチェックボックスとなる
				 * idにERB（=4）をセットする */
				value = getValue();

				++nERB;
				if(!flag_ERB) {		// 新しいラジオボタン、チェックボックスのグループの始まりなら
					++groupNumERB;	// グループ番号を１増やす
					nERB = 1;		// シーケンス番号も初期化する
				}

				/* 例 ERB(3,1,2) */
				note = _ERB + nNUM + "," + groupNumERB + "," + nERB + RBLACKET;
				flag_ERB = true;

				return new ErbToken(NoteType.ERB, note, value, nNUM, groupNumERB, nERB, hai_ten); 
				
			} else if(tokenId == ETA) {
				/* #{ /}
				 * テキストエリアに解答文を書き込む形式
				 * ETA(1)のような埋め込み記号を作成
				 * 数値は問題中のシーケンス番号
				 * idにETA（=5）をセットする */

				value = getValue();
				++nETA;
				note = _ETA + nNUM + ",0," + nETA + RBLACKET;

				flag_ERB = false;	// 問題は１つのETAで完結するので、連続するETAはない
				
				return new EtaToken(NoteType.ETA, note, value, nNUM, 0, nETA, hai_ten);

			} else if(tokenId == EPT) {
				/* [@nn /]
				 * 配点
				 * 埋め込み文字列は、例えば、EPT(1)のようになる
				 * idにEPT（=6）をセットする */

				value = getValue();
				++nEPT;
				note = _EPT + "0,0," + nEPT + RBLACKET;
				/*
				 * 他のトークンオブジェクト作成に必要なので、配点をフィールド変数に入れておく
				 */
				hai_ten = Integer.parseInt(value);

				flag_ERB = false;
				
				return new EptToken(NoteType.EPT,note,value, nEPT, 0, 0);

			} else if(tokenId == TIME) {
				/* {@nn /}
				 * 制限時間（ {@nn/} ）
				 * 埋め込み文字列は、TIME(1)になる。数値は１以外ない
				 * idにTIME（=9）をセットする */

				value = getValue();
				note = _TIME + "0,0,0" + RBLACKET;	// 未使用のダミー
				flag_ERB = false;
				
				return new TimeToken(NoteType.TIME, note, value, 0,0,0);
				
			} else if(tokenId == ADMY) {
				/* [& /]
				 * 追加的なダミーの埋め込み語句（ [&aaa,bbb,ccc] ）
				 * 埋め込み文字列は、例えば、AENL(1)のようになる
				 * idにAENL（=7）をセットする */

				value = getValue();
				++nADMY;
				note = _ADMY + nNUM + "," + groupNumENL + ",0"  + RBLACKET;
				flag_ERB = false;
				
				return new AdmyToken(NoteType.ADMY, note, value, nNUM, groupNumENL, 0);

			} else if(tokenId == ASEL) {
				/* [+ nn/3-1-4-2-5/]
				 * 解答選択肢書き込み位置 -- ひとつの問題が終了したとみなせるのでnewENLFLAG=trueとし、次の問題に備えておく
				 * 埋め込み文字列は、例えば、ASET(3,1,0) のようになる
				 */
				value = getValue();
				++nSEL;
				note = _ASEL + nNUM + "," + groupNumENL + ",0" + RBLACKET;	
				
				flag_ENL = flag_ERB = false;		// グループが終了した
				
				return new AselToken(NoteType.ASEL, note, value, nNUM, groupNumENL, 0);

			} else {	// TXT
				/*
				 * EPML以外のテキスト 
				 */
				value = getNormalString();
				++nTEXT;
				note = _TEXT + nNUM + ",0," + nTEXT + RBLACKET;


				/* ラジオボタン、チェックボックスの問題は複数の選択肢があるので、その最後を判定する
				 * 現在のトークンが改行のみ、または、空白行であれば、まだ、問題は続いている
				 * 現在のflag_ERBが true でかつテキストが空白と改行のみの文字列なら true となる
				 */
				flag_ERB = (flag_ERB && value.isBlank());
				return new TextToken(NoteType.TEXT, note, value, nNUM, 0, nTEXT);
			}
			//
		} catch (EpmlTokenException e) {
			int ret = 0;
			if(e.getMessageValue() == EpmlTokenException.EndOfBuffer) {
				return null;

			} else {
				DBG.println("＊＊＊　例外発生　＊＊＊");
				e.printStackTrace();
				DBG.println("＊＊＊＊＊＊＊＊＊＊＊＊");
				throw e;

			}
		}

	}

	/**
	 * バッファから次の文字を取り出し、どのEPML文字か、あるいは一般文字かを表す記号文字を返す
	 * EPML文字だった場合、呼び出し側でgetValue()によりEPMLのvalueを得ることができる
	 * 一般文字だった場合、呼び出し側でgetNormalString()によりテキストを得ることができる
	 */
	public char nextTokenID() throws EpmlTokenException {

		/* バッファが空だと例外を発生
		 * 単に終了を意味する．エラーではない */
		char c1 = 0;
		char c2 = 0;

		// １文字目を取り出す
		if((c1 = nextChar()) == 0) { // バッファから1文字取って返す。バッファエンドなら0を返す
			throw new EpmlTokenException(EpmlTokenException.EndOfBuffer);
		}
		/*  */
		char mark = firstChar(c1);	// '$'  '['   '{'  '#'   '!'   '/'   '*'  のどれかを返す
		
		if(mark==DOLLER) {
			if((c2 = nextChar()) == 0) {
				// c1を読み込んだあと、c2の読み込みで終端に達した時の処理
				return bufferEndReturn();
			}
			
			mark = secondChar(c2);
			if(mark==LB) {	// [
				return NUM;		// $[  問題番号
			
			}else {
				// c1はEPML文字だったが、c2が一般文字だった時の処理
				return notEpmlCharReturn();
			}
		
		}else if(mark == SHP) {

			/* #{･･･/} テキスト領域
			 * #[･･･/] チェックボックス、ラジオボタンによる選択肢 */

			if((c2 = nextChar()) == 0) {
				// c1を読み込んだあと、c2の読み込みで終端に達した時の処理
				return bufferEndReturn();
			}
			// 2文字目を取り出す
			mark = secondChar(c2);
			//
			if(mark == LB) {
				return ERB;

			} else if(mark == LBM) {
				return ETA;

			} else {
				// c1はEPML文字だったが、c2が一般文字だった時の処理
				return notEpmlCharReturn();
			}
			//
		} else if(mark == LBM) {		// '{'  制限時間
			/* {@30/} のケース：制限時間 */
			if((c2 = nextChar()) == 0) {
				/*
				 * nextChar()で終端に達した場合、ポインタpの値は増えないで0を返す
				 * c1を戻すために、ポインタを1つ戻す
				 */
				putBackChar();
				return TEXT;
			}
			mark = secondChar(c2);		// ２文字目を得る
			//
			if(mark == ATMK) {
				return TIME;
			} else {
				// c1はEPML文字だったが、c2が一般文字だった時の処理
				return notEpmlCharReturn();
			}
			//
		} else if(mark == LB) {	// [ であればさらにチェック
			/* [@10 /] 配点
			 * [& abc,def /] ダミー選択肢
			 * [+ 語群(3) /] 選択肢語群 */

			if((c2 = nextChar()) == 0) {
				// c1を読み込んだあと、c2の読み込みで終端に達した時の処理
				return bufferEndReturn();
			}
			mark = secondChar(c2);
			//
			if(mark == ATMK) {
				return EPT;

			} else if(mark == AMP) {
				return ADMY;

			} else if(mark == PLUS) {
				return ASEL;

			} else {
				// c1はEPML文字だったが、c2が一般文字だった時の処理
				return notEpmlCharReturn();
			}
			//
		} else if(mark == SLASH) {
			/* /] /}
			 * 
			 * 終端記号なので読み飛ばして再帰処理を行う 
			 */
			if((c2 = nextChar()) == 0) {
				// c1を読み込んだあと、c2の読み込みで終端に達した時の処理
				return bufferEndReturn();
			}
			mark = secondChar(c2);
			if((mark == RB) || (mark == RBM)) {
				mark = nextTokenID();
				return mark;

			} else {
				// c1はEPML文字だったが、c2が一般文字だった時の処理
				return notEpmlCharReturn();
			}
			//
		} else if(mark == EXCLA) {
			/* ![ /] 語群式選択肢、ドロップダウン選択肢
			 * !{ /} 記述式 */

			if((c2 = nextChar()) == 0) {
				// c1を読み込んだあと、c2の読み込みで終端に達した時の処理
				return bufferEndReturn();
			}
			mark = secondChar(c2);
			//
			if(mark == LB) {
				return ENL_EWL;
				//
			} else if(mark == LBM) {
				return ETF;
				//
			} else {
				// c1はEPML文字だったが、c2が一般文字だった時の処理
				return notEpmlCharReturn();
			}
		} else {
			/* 
			 * その他の場合、一般の文字なので文字列にして返す
			 * ポインタを１つ戻して、
			 * c1を一般テキストとして取り出せるようにする
			 */
			putBackChar();
			return TEXT;	
		}
	}
	/**
	 * c1を読み込んだあと、c2の読み込みで終端に達した時の処理
	 * @return 
	 */
	public char bufferEndReturn() {
		/*
		 * nextChar()で終端に達した場合、ポインタpの値は増えないで0を返す
		 * c1を戻すために、ポインタを1つ戻す
		 */
		putBackChar();
		return TEXT;		
	}
	
	/**
	 * c1（1文字目）はEPML文字だったが、c2（2文字目）が一般文字だった時の処理
	 * @return
	 */
	public char notEpmlCharReturn() {
		/* 
		 * ポインタを二つ戻して、
		 * c1,c2を一般テキストとして取り出せるようにする
		 */
		putBackChar();
		putBackChar();
		return TEXT;			
	}
	
	/**
	 * 一般文字列の取り出し
	 * 先頭文字が一般文字列と分かっているときに、
	 * 一般文字列を一区切り取り出して返す
	 */
	String getNormalString() {
		// 一般の文字なので文字列にして返す
		//
		String bf = "";
		char c=nextChar();
		
		while(true) {
			/*
			 * 1文字目は一般文字または一般文字として扱うEPML文字と分かっているので、
			 * 無条件にバッファに追加する
			 */
			bf += c;			
			c = nextChar();

			if(c==0) {
				/*
				 * 終端に達したのでbreakする
				 */
				break;
			}
			if(firstChar(c) != TEXT) {
				/*
				 * EPML文字なので文字を戻して
				 * breakする
				 */
				putBackChar();
				break;
			}			
		}
		return bf;
	}

	/**
	 * すでに先頭がEPML記号（![ や #[ など）で始まっている時、
	 * 残りの終了文字（/] や /}）までの文字列をtknに取得する
	 * テキストトークンになる部分なので、両端の空白を削除して返す
	 * 
	 * @throws データがないときはfalse
	 */
	private String getValue() {
		StringBuffer bf = new StringBuffer(1024);
		char c1 = 0;
		char c2 = 0;

		if((c1 = nextChar()) == 0) {
			return "";
		}
		putBackChar();
		//
		while((c1 = nextChar()) != 0) {
			/*
			 * '/'が出てくるまでバッファに読み出す
			 */
			if(c1 != SLASH) {
				bf.append(c1);
				continue;
			}
			
			/*
			 * c1に END（/）が入っている時、
			 * c2に次の文字を読み出す
			 */
			if((c2 = nextChar()) == 0) {
				 /* 
				  * 文字がなければc1の/をバッファにいれる
				  */
				bf.append(c1);
				break;

			} else {
				char check = lastChar(c2);
				if((check == RB) || (check == RBM)) {
					/*
					 * c2が ]か} なら終了なのでbreakする
					 */
					break;

				} else if(c2 == SLASH) {
					/* 
					 * c2も / の場合、
					 * c1を一般文字としてバッファに入れ、
					 * c2は戻して次の繰り返しで再度解析する 
					 */
					bf.append(c1);
					putBackChar();

				} else {
					/*
					 * c1は / 、c2は一般文字なので
					 * c1,c2共に一般文字としてバッファに入れる
					 */
					bf.append(c1);
					bf.append(c2);
				}
			}
		}
		/*
		 * 
		 */
		return bf.toString().strip();	// 両端の空白を削除して返す
	}

	/**
	 * バッファから１文字取って返す．ポインタは＋１される
	 * 
	 * @return
	 */
	char nextChar() {
		char c = 0;
		if(!EOB()) {
			c = buffer.charAt(p);
			p++;
		} else {
			c = 0;	// バッファが空
		}
		return c;
	}

	/**
	 * バッファを１文字戻す
	 * 
	 */
	void putBackChar() {
		--p;
	}

	/**
	 * バッファが空かどうか
	 * 
	 * @return
	 */
	boolean EOB() {
		return (p >= max);	// 最後のとき true
	}

	/**
	 * １文字目のEPML文字を判定する
	 * 
	 * @return
	 */
	char firstChar(char c) {
		if(c == LB)
			return LB;		// [
		if(c == LBM)
			return LBM;		// {
		if(c == SHP)
			return SHP;		// #
		if(c == EXCLA)
			return EXCLA;		// !
		if(c == DOLLER)
			return DOLLER;		// $
		return TEXT;
	}

	/**
	 * ２文字目のEPML文字を判定する
	 * 
	 * @param c
	 * @return
	 */
	char secondChar(char c) {
		if(c == LB)
			return LB;	// [
		if(c == LBM)
			return LBM;	// {
		if(c == PLUS)
			return PLUS;	// +
		if(c == ATMK)
			return ATMK;	// @
		if(c == AMP)
			return AMP;	// &
		return TEXT;
	}

	/**
	 * 最後のEPML文字（ ] or } ）を判定する
	 * 
	 * @param c
	 * @return
	 */
	char lastChar(char c) {
		if(c == RB)
			return RB;		// ]	
		if(c == RBM)
			return RBM;		// }
		return TEXT;			// * （テキスト）
	}

	// EPML文字
	private static final char DOLLER = '$';
	private static final char LB = '[';
	private static final char RB = ']';
	private static final char LBM = '{';
	private static final char RBM = '}';
	private static final char SHP = '#';
	private static final char ATMK = '@';
	private static final char AMP = '&';
	private static final char PLUS = '+';
	private static final char EXCLA = '!';
	private static final char SLASH = '/';

	// nextTokenId()が返すトークンID
	private static final char NUM = 'd';		// $[ の代わり　問題番号
	private static final char ERB = 's';		// #[ の代わり　正しい文をチェックする問題
	private static final char ETA = 'w';		// #{ の代わり　テキストエリアに記述する問題
	private static final char EPT = 't';		// [@ の代わり　配点
		
	private static final char ADMY = 'a';	// [& の代わり　ダミー選択肢
	private static final char ASEL = 'b';	// [+ の代わり	選択肢リスト
	private static final char TIME = 'x';	// {@ の代わり　制限時間
	
	private static final char ENL_EWL='e';	// ![  
	private static final char ETF = 'f';		// #{ の代わり　テキストエリアに記述する問題
	private static final char TEXT = '*';	// 一般の文字列の代わり

	// ノート開始文字列（問題文に埋め込む）
	private static final String _NUM = "NUM(";	// NUM(3,0,0) 問題番号
	private static final String _ERB = "ERB(";	// ERB(3,1,2) 選択ボタン
	private static final String _ETA = "ETA(";	// ETA(3,0,1) テキストエリア
	private static final String _EPT = "EPT(";	// EPT(0,0,2) 配点
	
	private static final String _ADMY = "ADMY(";	// ADMY(3,1,0) ダミー選択肢
	private static final String _ASEL = "ASEL(";	// ASEL(3,1,0) 選択肢のりスト
	private static final String _TIME = "TIME(";	// TIME(0,0,0) 制限時間

	private static final String _ENL = "ENL(";	// ENL(3,1,2) 選択肢記入欄
	private static final String _EWL = "EWL(";	// EWL(3,0,2) ドロップダウンリスト
	private static final String _ETF = "ETF(";	// ETF(3,0,2) テキストフィールド
	
	private static final String _TEXT = "TEXT(";	// TEXT(3,0,2) EPMLではないテキスト 

	// ノート終了文字列
	private static final String RBLACKET = ")";	// 上記の右括弧．間にCSVでのパラメータが入る

}
