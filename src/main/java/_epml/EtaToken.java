package _epml;

/**
 * ETA   #{row,col|text}
 * テキストエリアにコードやテキストを書き込む問題
 * 自動採点はしない
 * textはテキスト、rowは表示行数、colは表示桁数
 * 
 * textは解答だが、なくてもよい
 * 
 * @author kawaba
 * 
 */
public class EtaToken extends EpmlToken {

	private Integer hai_ten;
	private Integer rows;
	private Integer cols;
	private String seikai_text = "";
	
	public EtaToken() {}

	public EtaToken(NoteType type, String note, String value, Integer number, Integer group, Integer seq, Integer hai_ten) {
		super(type, note, value, number, group, seq);
		
		this.hai_ten = hai_ten;
		/*
		 * valueを[,]と[|]で分割する
		 * rows, colsは必ず指定されているので、
		 * 最後のtextのみ、指定されているかチェックしてセットする
		 */
		String[] ss = value.split("[,|]");
		rows = Integer.parseInt(ss[0]);
		cols = Integer.parseInt(ss[1]);
		if(ss.length>2) {
			seikai_text = ss[2];
		}
	}

	@Override
	public String toString() {
		return "EtaToken [hai_ten=" + hai_ten + ", rows=" + rows + ", cols=" + cols + 
				", seikai_text=" + seikai_text + ", type=" + type + ", note=" + note + 
				", value=" + value + ", number=" + number + 
				", group=" + group + ", seq=" + seq + "]";
	}
	

	public Integer getHai_ten() {
		return hai_ten;
	}

	public void setHai_ten(Integer hai_ten) {
		this.hai_ten = hai_ten;
	}

	public Integer getRows() {
		return rows;
	}

	public void setRows(Integer rows) {
		this.rows = rows;
	}

	public Integer getCols() {
		return cols;
	}

	public void setCols(Integer cols) {
		this.cols = cols;
	}

	public String getSeikai_text() {
		return seikai_text;
	}

	public void setSeikai_text(String seikai_text) {
		this.seikai_text = seikai_text;
	}



	////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static void main(String[] agrs) throws Exception {

		EtaToken eta = new EtaToken(NoteType.ETA,
										"ETA(3,0,1)",
										"10,40",
										3,0,1,
										10);
		System.out.println(eta);
	}
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////////////////		
}
