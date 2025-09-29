package _epml;

/**
 * TIME　@{n/}
 * 制限時間（分単位）を指定する
 *  
 * このクラスでは制限時間を保持するだけ。
 * 制限時間の操作は試験を実行するJavaプログラムと試験ウェブに埋め込んだJavascriptで行う
 * 
 * Java -- 受験開始時にタイムスタンプを含むHOLDレコードをDBに保存する
 * 　　　　受験終了時にHOLDレコードをクリアする
 * 　　　　受験開始時にHOLDレコードがあり、制限時間を過ぎていれば受験できない
 * 　　　　受験開始時にHOLDレコードがあり、制限時間内なら、残りの時間で再受験できる
 * 
 * Javascript 
 * 　　setTimeout()関数により、残り時間＝開始時刻＋制限時間 - 現在時刻　の計算を毎秒ごとに行う
 * 　　残り時間がゼロになると、submitして終了する
 *  
 * @author kawaba
 *
 */
public class TimeToken extends EpmlToken {
	
	private Integer timelimit;

	public TimeToken() {}

	public TimeToken(NoteType type, String note, String value, Integer number, Integer group, Integer seq) {
		super(type, note, value, number, group, seq);
		/*
		 * valueから制限時間をセットする
		 */
		timelimit = Integer.parseInt(value);
	}

	@Override
	public String toString() {
		return "TimeToken [timelimit=" + timelimit + ", type=" + type + ", note=" + note + ", "
				+ "value=" + value + ", number=" + number + ", group=" + group + ", seq=" + seq + "]";
	}

	public Integer getTimelimit() {
		return timelimit;
	}

	public void setTimelimit(Integer timelimit) {
		this.timelimit = timelimit;
	}
	
	
}
