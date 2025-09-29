package _epml;

/**
 * ERB(3,1,2)  #[～/]
 * ラジオボタン、チェックボックスによる選択問題クラス
 * 問題は、複数のERBからなる（グループになる）
 * 
 * 正解語は、先頭に * が付加されている
 * 
 * グループ内に正解語が２つ以上ある時、チェックボックスになる
 * 
 * @author kawaba
 *
 */
enum Btype{ RADIO,	// ラジオボタン
			CHECK}	// チェックボックス

public class ErbToken extends EpmlToken {
	/*
	 * btypeはEmlParserがセットする
	 */
	private Integer hai_ten;	// 配点
	private boolean judge;		// 正解語の時true
	private String seikai_go;	// 正解の時、正解語。不正解なら空文字
	private Btype	btype;		// ラジオボタンかチェックボックスか
	
	public ErbToken() {}
	
	public ErbToken(NoteType type, String note, String value, Integer number, Integer group, Integer seq,Integer hai_ten) {
		super(type, note, value, number, group, seq);

		this.hai_ten = hai_ten;
		/*
		 * 先頭が * なら正解なのでjudgeにtrueをセットし、
		 * 正解語に先頭の*を取り去った値をセットする
		 * 不正解語なら、judgeにfalseをセットする
		 */
		if(value.startsWith("*")) {
			seikai_go = value.substring(1);
			judge = true;
		}else {
			seikai_go = "";
			judge = false;
		}
		
		// btypeはEpmlParserがセットする
		
	}
	
	@Override
	public String toString() {
		return "ErbToken [hai_ten=" + hai_ten + ", judge=" + judge + 
				", seikai_go=" + seikai_go + ", btype=" + btype + ", type=" + type + 
				", note=" + note + ", value=" + value + ", number=" + number
				+ ", group=" + group + ", seq=" + seq + "]";
	}

	public Integer getHai_ten() {
		return hai_ten;
	}

	public void setHai_ten(Integer hai_ten) {
		this.hai_ten = hai_ten;
	}

	public boolean isJudge() {
		return judge;
	}

	public void setJudge(boolean judge) {
		this.judge = judge;
	}

	public String getSeikai_go() {
		return seikai_go;
	}

	public void setSeikai_go(String seikai_go) {
		this.seikai_go = seikai_go;
	}

	public Btype getBtype() {
		return btype;
	}

	public void setBtype(Btype btype) {
		this.btype = btype;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void main(String[] agrs) throws Exception {

		ErbToken erb = new ErbToken(NoteType.ERB,
										"ERB(3,1,2)",
										"dog",
										3,1,2,
										10);
		System.out.println(erb);
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////		
	
	
}
