package _epml;
/**
 * ENL ![～/]
 * 選択肢一覧から正解を番号で選んで解答する問題
 * 解答欄は複数あり、このクラスはその中の1つの解答欄
 * 
 * @author kawaba
 *
 */
public class EnlToken extends EpmlToken {
	/*
	 * 正解語とダミー選択肢をランダムに並べ替えて、
	 * その時の正解語の並び位置を選択肢番号とする。
	 * 選択肢番号は、グループ内で並べ替え処理が行
	 * われる時に決定される。
	 */
	private	Integer hai_ten;		// 配点
	private String seikai_go;		// 正解語
	private Integer sentaku_shi;	// 選択肢番号
	
	public EnlToken() {}

	public EnlToken(NoteType type, String note, String value, Integer number, Integer group, Integer seq,Integer hai_ten) {
		super(type, note, value, number, group, seq);
		
		this.hai_ten = hai_ten;
		seikai_go = value;
		
		// sentaku_shiはEpmlParserがセットする
	}

	@Override
	public String toString() {
		return "EnlToken [hai_ten=" + hai_ten + ", seikai_go=" + seikai_go + ", sentaku_shi=" + sentaku_shi + ", type=" + type + ", note=" + note + ", value=" + value + ", number=" + number
				+ ", group=" + group + ", seq=" + seq + "]";
	}

	public Integer getHai_ten() {
		return hai_ten;
	}

	public void setHai_ten(Integer hai_ten) {
		this.hai_ten = hai_ten;
	}

	public String getSeikai_go() {
		return seikai_go;
	}

	public void setSeikai_go(String seikai_go) {
		this.seikai_go = seikai_go;
	}

	public Integer getSentaku_shi() {
		return sentaku_shi;
	}

	public void setSentaku_shi(Integer sentaku_shi) {
		this.sentaku_shi = sentaku_shi;
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void main(String[] agrs) throws Exception {

		EnlToken ewl = new EnlToken(NoteType.ENL,
										"ENL(3,1,2)",
										"dog",
										3,1,2,
										10);
		System.out.println(ewl);
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////		
}
