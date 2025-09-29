package _epml;
/**
 * EPT  @[n/]
 * 配点を指定する
 * 
 * 問題中に複数配置し、その位置以降、次の配点が出現するまでの配点を指定する
 * 複数個の配点指定がある
 * 
 * 採点クラス（Scoring）が参照する
 * 
 * @author kawaba
 *
 */
public class EptToken extends EpmlToken {
	
	public Integer hai_ten;

	public EptToken() {}

	public EptToken(NoteType type, String note, String value, Integer number, Integer group, Integer seq) {
		super(type, note, value, number, group, seq);
		
		/*
		 * valueを整数化して配点とする
		 */
		hai_ten = Integer.parseInt(value);
	}

	@Override
	public String toString() {
		return "EptToken [hai_ten=" + hai_ten + ", type=" + type + ", note=" + note + ", value=" + value + 
				", number=" + number + ", group=" + group + ", seq=" + seq + "]";
	}

	public Integer getHai_ten() {
		return hai_ten;
	}

	public void setHai_ten(Integer hai_ten) {
		this.hai_ten = hai_ten;
	}
}
