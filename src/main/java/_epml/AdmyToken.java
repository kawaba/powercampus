package _epml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ADMY [&～,～、～ /]
 * ENLの選択肢に追加するダミー選択肢リストのクラス
 * 
 * @author kawaba
 *
 */
public class AdmyToken extends EpmlToken {

	private List<String> dummies;	// ダミー選択肢のリスト

	public AdmyToken() {
	}

	public AdmyToken(NoteType type, String note, String value, Integer number, Integer group, Integer seq) {
		super(type, note, value, number, group, seq);
		
		/* value ; "aaa,bbb,cc"
		 * valueをコンマで分割してdummiesに登録する */
		dummies = new ArrayList<>();
		Arrays.stream(value.split(",")).forEach(dummies::add);
	}

	@Override
	public String toString() {
		return "AdmyToken [dummies=" + dummies + ", type=" + type + ", note=" + note + ", value=" + value + ", number=" + number + ", group=" + group + ", seq=" + seq + "]";
	}

	public List<String> getDummies() {
		return dummies;
	}

	public void setDummies(List<String> dummies) {
		this.dummies = dummies;
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void main(String[] agrs) throws Exception {

		AdmyToken tkn = new AdmyToken(NoteType.ADMY,
				"ADMY(3,1,0)",
				"dog,cat,rabbit",
				3, 1, 0);
		System.out.println(tkn);
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////
}
