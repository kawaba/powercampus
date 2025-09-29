package mailutil_service;

import java.util.List;
import jakarta.xml.bind.annotation.XmlRootElement;

/*
  差し込み送信に必要なデータセット
  メッセージ本文はSashikomi_Recipientの中にある
  
 */
@XmlRootElement
public class DataSet {

    private String sender;              // 送信者メール
    private String title;               // メールタイトル
    private String type;                // "CC" or "BCC" or "TO"
    private String message;            // 本文またはテンプレート
    private List<Recipient> dlist;      // 宛先のリスト

    public DataSet(String sender, 
    				String title, 
    				String type, 
    				String template, 
    				List<Recipient> dlist) 
    {
    	
        this.sender = sender;
        this.title = title;
        this.type = type;
        this.message = template;
        this.dlist = dlist;
    }

    public DataSet() {
    }

    public static int checkData(DataSet data) {
        if (data == null) {
            return 1;
        }
        if (data.sender == null || data.sender.isBlank()) {
            return 2;
        }
        if (data.title == null || data.title.isBlank()) {
            return 3;
        }
        if (data.type == null || data.type.isBlank()) {
            return 4;
        }
        if(data.message == null || data.message.isBlank()){ // テンプレートか本文を必ずいれておく
            return 5;
        }
        if (data.dlist == null || data.dlist.isEmpty()) {
            return 6;
        }

        return 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DataSet{" + "sender=" + sender + ", title=" + title + ", type=" + type + ", template=" + message + ",\n");
        dlist.forEach(r->sb.append("\t"+ r.toString()+"\n"));
        sb.append("}\n");
    	
    	return sb.toString();
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Recipient> getDlist() {
        return dlist;
    }

    public void setDlist(List<Recipient> dlist) {
        this.dlist = dlist;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}

