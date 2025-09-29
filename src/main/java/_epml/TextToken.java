package _epml;

/**
 * EpmlTokenと同じ
 * Epml以外のテキストをvalueに持つ
 * 
 * @author kawaba
 *
 */
public class TextToken extends EpmlToken {

	public TextToken() {}
		
	public TextToken(NoteType type, String note, String value, Integer number, Integer group, Integer seq) {
		super(type, note, value, number, group, seq);

	}
	/*
	 * テキストを返す
	 */
	public String getText() {
		return value;
	}
	
	@Override
	public String toString() {
		return "TextToken [type=" + type + ", note=" + note + ", value=" + value + ", number=" + number + ", group=" + group + ", seq=" + seq + "]";
	}
	
}
