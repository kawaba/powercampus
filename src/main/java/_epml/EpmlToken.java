package _epml;

/**
 * Epmlのすべてのトークンのスーパークラス
 *  
 * @author kawaba
 *
 */
public class EpmlToken {

	protected NoteType type;	// ノートの型番号(1～20)
	protected String note;		// ノート表記 ex. ENL(3), ERB(1,2) など
	protected String value;		// 解答、問題テキスト
	
	protected Integer number;	// 問題番号
	protected Integer group;	// グループ番号
	protected Integer seq;		// 順序番号

	public EpmlToken() {}
	
	public EpmlToken(NoteType 	type, 
					  String 	note, 
					  String 	value,
					  Integer	number,
					  Integer 	group, 
					  Integer 	seq)

	{
		this.type	= type;
		this.note	= note;
		this.value	= value;
		this.number = number;
		this.group 	= group;
		this.seq	= seq;
	}

	@Override
	public String toString() {
		return "EpmlToken [type=" + type + ", note=" + note + ", value=" + value + ", number=" + number + ", group=" + group + ", seq=" + seq + "]";
	}


	public NoteType getType() {
		return type;
	}


	public void setType(NoteType type) {
		this.type = type;
	}


	public String getNote() {
		return note;
	}


	public void setNote(String note) {
		this.note = note;
	}


	public String getValue() {
		return value;
	}


	public void setValue(String value) {
		this.value = value;
	}


	public Integer getNumber() {
		return number;
	}


	public void setNumber(Integer number) {
		this.number = number;
	}


	public Integer getGroup() {
		return group;
	}


	public void setGroup(Integer group) {
		this.group = group;
	}


	public Integer getSeq() {
		return seq;
	}


	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	
}
