package mailutil_service;

/*
宛先データ
 */
public class Recipient {

    private boolean flag;   // 状態の記録用
    private String id;
    private String name;
    private String email;
    private String message; // メール本文（個別に持つ場合はここに） 
    private String mr;      // 敬称

    public Recipient(boolean 	flag, 
    				  String 	id, 
    				  String 	name, 
    				  String 	email, 
    				  String 	message, 
    				  String 	mr) {
        super();
        this.flag = flag;
        this.id = id;
        this.name = name;
        this.email = email;
        this.message = message;
        this.mr = mr;
    }

    public Recipient() {
    }

    @Override
    public String toString() {
        return "Recipient [flag=" + flag + ", id=" + id + ", name=" + name + ", email=" + email + ", message=" + message + ", mr=" + mr + "]";
    }

    public String summary() {
        return id + ",　" + name + ", " + email;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMr() {
        return mr;
    }

    public void setMr(String mr) {
        this.mr = mr;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
