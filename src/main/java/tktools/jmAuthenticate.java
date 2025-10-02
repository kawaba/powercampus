package tktools;

import java.util.Properties;
import jakarta.mail.MessagingException;
import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Session;
import jakarta.mail.Store;

public class jmAuthenticate implements AutoCloseable {
    private Store store;

    // POP認証
    public static boolean authenticate(String host, String user, String password) {
        try (jmAuthenticate jm = new jmAuthenticate()) {
            jm.connect(host, user, password);
            // try-with-resourcesで自動的にclose()が呼ばれる
        } catch (MessagingException e) {
            return false;
        }
        return true;
    }

    // コンストラクタ
    public jmAuthenticate() throws NoSuchProviderException {
        Session session = Session.getInstance(new Properties(), null);
        store = session.getStore("pop3");
    }

    // 接続
    public synchronized void connect(String host, String user, String password) throws MessagingException {
        store.connect(host, -1, user, password);
    }

    // 切断
    public synchronized void disconnect() {
        try {
            store.close();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        disconnect();
    }
}

