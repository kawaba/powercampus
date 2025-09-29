package _epml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ASEL  [+～/]  
 * 選択肢一覧表
 * 
 * このクラスではENL全体のパースなど具体的な操作は行わない
 * 1行に表示する選択肢の数と、ランダムな表示順序を記録する
 * 
 * 全体をパースして順序文字列を生成するのはEpmlParserクラス
 * このノートがある位置に選択肢一覧表を表示する
 *  
 * 例　[+3/]
 * 
 * EpmlPaserが、すべての正解語とダミー選択肢を読み取り、
 * ランダムな順番に並び替える。
 * また、記録のために、その並び順を文字列にして
 * ASELの、～の末尾に付加する。つまり、valueを次のように書き換える
 * 
 * 例　3/7-3-9-4-1-2-5-6-8
 * 
 * ・1行に3列表示する例
 * ・解答欄1（seq==1)の正解語を"1983年"とすると、
 * 　５番目にあるので、(5)1983年 と選択肢一覧表に表示される
 * 　表示位置は2行目の2つ目になる
 * ・並び順を削除すると、パース時に新たな並び順を再作成する
 * 
 * ・同じ問題番号、同じグループのENLをENL(n,g,1)とすると
 * 　その選択肢番号（sentaku_shi）に"5"をセットする
 * 
 * ・コンストラクタは、ランダムな並び順がある場合、
 * 　並び順のリストを作成する（表示に利用される）
 * 
 * @author kawaba
 *
 */
public class AselToken extends EpmlToken {

	private Integer rows;
	private List<Integer> orderList;
	
	public AselToken() {}

	public AselToken(NoteType type, String note, String value, Integer number, Integer group, Integer seq) {
		super(type, note, value, number, group, seq);
		/*
		 * valueが/で区切られた順序文字列を含むかどうか調べる
		 * 含む場合は、orderList配列を作成する
		 */
		String[] elements = value.split("/");
		if(elements.length>1) {
			createOrderList(elements[1]);
		}
		
		/* 行数をセットする
		 * parseInte()で例外は発生しない
		 */
		rows = Integer.parseInt(elements[0]);
	}
	
	private void createOrderList(String orders) {
		/*
		 * 順序文字列 order を'-'で分割して配列にし、
		 * その要素をIntegerに変換してorderListにセットする
		 */
		orderList = new ArrayList<Integer>();
		Arrays.stream(orders.split("-"))
							.map(Integer::parseInt)
							.forEach(orderList::add);
	}

	public Integer getRows() {
		return rows;
	}

	public void setRows(Integer rows) {
		this.rows = rows;
	}

	public List<Integer> getOrderList() {
		return orderList;
	}

	public void setOrderList(List<Integer> orderList) {
		this.orderList = orderList;
	}

	@Override
	public String toString() {
		return "AselToken [rows=" + rows + ", orderList=" + orderList + ", type=" + type + ", note=" + note + ", value=" + value + ", number=" + number + ", group=" + group + ", seq=" + seq + "]";
	}

	////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void main(String[] agrs) throws Exception {

		AselToken asel = new AselToken(NoteType.ASEL,
										"ASEL(1,1,0)",
										"3/7-3-9-4-1-2-5-6-8",
										1,1,0);
		System.out.println(asel);
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////	
}
