package _epml;
/**
 * $[～/]
 * 問題番号（大問の番号）のクラス
 * 1つの問題の中に、複数のタイプの複数の小問がある
 * 大問ごとに得点を集計するために利用する
 * 各小問オブジェクトは、この問題番号を属性として持つ
 * 
 * @author kawaba
 *
 */
public class NumberToken extends EpmlToken {
	private Integer ban_go;		// 問題番号
	


	public NumberToken() {}

	public NumberToken(NoteType type, String note, String value, Integer number, Integer group, Integer seq) {
		super(type, note, value, number, group, seq);
		ban_go = Integer.parseInt(value);
	}

	public Integer getBan_go() {
		return ban_go;
	}

	public void setBan_go(Integer ban_go) {
		this.ban_go = ban_go;
	}

	@Override
	public String toString() {
		return "NumberToken [ban_go=" + ban_go + ", type=" + type + 
				", note=" + note + ", value=" + value + ", number=" + number + 
				", group=" + group + ", seq=" + seq + "]";
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void main(String[] agrs) throws Exception {

		NumberToken num = new NumberToken(NoteType.ETF,
										"NUM(2,0,0)",
										"2",
										2,0,0
										);
		System.out.println(num);
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////	
	
}
