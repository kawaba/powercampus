package kamoku;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * データベースから取り出した
 * セクション実施レコードを格納する
 * 
 * @author kawaba
 *
 */
public class SectionRecord {
	private String te_aplec_key;
	private String sect_key;
	private String start_month;
	private String start_day;
	private String end_month;
	private String end_day;
	private String memo;
	
	public SectionRecord(String te_aplec_key, String sect_key, String start_month, String start_day, String end_month, String end_day, String memo) {
		this.te_aplec_key = te_aplec_key;
		this.sect_key = sect_key;
		this.start_month = start_month;
		this.start_day = start_day;
		this.end_month = end_month;
		this.end_day = end_day;
    	this.memo = "***";
    }
	
    private String dateFormatted(LocalDate start, LocalDate end) {
	        
	        // 曜日のフォーマットを日本語で指定
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M月d日（E）");
	        
	        // フォーマットして出力
	        String startStr = start.format(formatter);
	        String endStr   = end.format(formatter);
	        return startStr + "～" + endStr;
    }
	
	// 開始日をintで返す
	public int startMonth() {
		return Integer.parseInt(start_month.strip());
	}
	// 開始月をintで返す
	public int startDay() {
		return Integer.parseInt(start_day.strip());
	}
	
	// LocalDate型の開始日付を返す
	public LocalDate startDay(int yy) {
		return LocalDate.of(yy, startMonth(), startDay());
	}
	
	// 終了月をintで返す
	public int endMonth() {
		return Integer.parseInt(end_month.strip());
	}
	// 終了日をintで返す
	public int endDay() {
		return Integer.parseInt(end_day.strip());
	}
	
	// LocalDate型の終了日付を返す
	public LocalDate endDay(int yy) {
		return LocalDate.of(yy, endMonth(), endDay());
	}
	
	public String getTe_aplec_key() {
		return te_aplec_key;
	}
	public void setTe_aplec_key(String te_aplec_key) {
		this.te_aplec_key = te_aplec_key;
	}
	public String getSect_key() {
		return sect_key;
	}
	public void setSect_key(String sect_key) {
		this.sect_key = sect_key;
	}
	public String getStart_month() {
		return start_month;
	}
	public void setStart_month(String start_month) {
		this.start_month = start_month;
	}
	public String getStart_day() {
		return start_day;
	}
	public void setStart_day(String start_day) {
		this.start_day = start_day;
	}
	public String getEnd_month() {
		return end_month;
	}
	public void setEnd_month(String end_month) {
		this.end_month = end_month;
	}
	public String getEnd_day() {
		return end_day;
	}
	public void setEnd_day(String end_day) {
		this.end_day = end_day;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	@Override
	public String toString() {
		return "SectonAp [te_aplec_key=" + te_aplec_key + ", sect_key=" + sect_key + ", start_month=" + start_month + ", start_day=" + start_day + ", end_month=" + end_month + ", end_day=" + end_day
				+ ", memo=" + memo + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sect_key == null) ? 0 : sect_key.hashCode());
		result = prime * result + ((te_aplec_key == null) ? 0 : te_aplec_key.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if(getClass() != obj.getClass())
			return false;
		SectionRecord other = (SectionRecord) obj;
		if(sect_key == null) {
			if(other.sect_key != null)
				return false;
		} else if(!sect_key.equals(other.sect_key))
			return false;
		if(te_aplec_key == null) {
			if(other.te_aplec_key != null)
				return false;
		} else if(!te_aplec_key.equals(other.te_aplec_key))
			return false;
		return true;
	}

	
}
