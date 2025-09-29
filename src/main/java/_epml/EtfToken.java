package _epml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
/**
 * ETF   ![正解語CSV/]
 * テキストフィールドに単語を記入する問題
 *  
 * valueは正解語
 * 採点クラス（Scoring）が、採点時に、解答を見て正解語と不正解語を追加する
 * 
 * EPMLテキストとして出力する時は、 ![正解語CSV/不正解語CSV/] の形になる
 * コンストラクタがこれを処理できるようにする
 * 
 *  
 * @author kawaba
 *
 */
public class EtfToken extends EpmlToken {
	private Integer hai_ten;
	private List<String> corrects;
	private List<String> incorrects;
	
	public EtfToken() {}
	
	public EtfToken(NoteType type, String note, String value, Integer number, Integer group, Integer seq, Integer hai_ten) {
		super(type, note, value, number, group, seq);
		
		this.hai_ten = hai_ten;
		/*
		 * valueを"/"で分割して配列valsに入れる
		 * vals[0]は正解語リスト（初期は要素は1つだけ）
		 * vals[1]は不正解語リスト
		 * 
		 * vals[0]を","で分割して要素を正解語リストに加える
		 * vals[1]があれば、","で分割して不正解語リストに加える
		 * 
		 */
		corrects = new ArrayList<String>();
		incorrects = new ArrayList<String>();
		
		String[] vals = value.split("/");
						
		Arrays.stream(vals[0].split(",")).forEach(corrects::add);
		if(vals.length>1) {
			Arrays.stream(vals[1].split(",")).forEach(incorrects::add);
		}
	}
	
	@Override
	public String toString() {
		return "EtfToken [hai_ten=" + hai_ten + ", corrects=" + corrects + ", incorrects=" + incorrects 
				+ ", type=" + type + ", note=" + note + ", value=" + value + ", number=" + number + ", group="
				+ group + ", seq=" + seq + "]";
	}

	public Integer getHai_ten() {
		return hai_ten;
	}

	public void setHai_ten(Integer hai_ten) {
		this.hai_ten = hai_ten;
	}

	public List<String> getCorrects() {
		return corrects;
	}

	public void setCorrects(List<String> corrects) {
		this.corrects = corrects;
	}

	public List<String> getIncorrects() {
		return incorrects;
	}

	public void setIncorrects(List<String> incorrects) {
		this.incorrects = incorrects;
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void main(String[] agrs) throws Exception {

		EtfToken etf = new EtfToken(NoteType.ETF,
										"ETF(3,0,2)",
										"dog,cat/rabbit,rabbits",
										3,0,2,
										10);
		System.out.println(etf);
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	
}
