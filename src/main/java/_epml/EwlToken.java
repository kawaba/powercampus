package _epml;

import java.util.Arrays;

/**
 * EWL   ![～,～,～/]
 * ドロップダウンリストから正解を選ぶ問題
 * valueは、選択語のリスト
 * 正解は先頭に*がある
 * 
 * @author kawaba
 *
 */
public class EwlToken extends EpmlToken {
	
	private Integer hai_ten;
	private String	seikai_go;
	
	public EwlToken() {}
	
	public EwlToken(NoteType type, String note, String value, Integer number, Integer group, Integer seq, Integer hai_ten) {
		super(type, note, value, number, group, seq);
		
		this.hai_ten = hai_ten;
		/* 
		 * valueはcsvの単語リストで正解語だけ、先頭に*がある
		 * 　value:  dog,*cat,rabbit
		 * 
		 * valueをコンマでsplitし、*で始まる要素を正解語にセットする
		 * 正解語に*は付けない
		 */
		Arrays.stream(value.split(","))
			.filter(v->v.startsWith("*"))
			.forEach(v->seikai_go = v.substring(1));
	}

	@Override
	public String toString() {
		return "EwlToken [hai_ten=" + hai_ten + ", seikai_go=" + seikai_go + 
				", type=" + type + ", note=" + note + ", value=" + value + 
				", number=" + number + ", group=" + group + ", seq=" + seq	+ "]";
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
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void main(String[] agrs) throws Exception {

		EwlToken ewl = new EwlToken(NoteType.EWL,
										"EWL(3,0,2)",
										"dog,cat,*rabbit,rabbits",
										3,0,2,
										10);
		System.out.println(ewl);
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////	
}
