/**
 * pop-before-smtp 方式の実装
 * Copyright (c) 2015 Takashi Kawaba Released under the MIT license
 * https://opensource.org/licenses/MIT* *
 */
package net.tkxtools;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import jakarta.mail.MessagingException;
import jakarta.mail.NoSuchProviderException;
import jakarta.mail.Session;
import jakarta.mail.Store;

public class JmAuthenticate implements AutoCloseable {
    private Store store;
    /**
     * POP 認証
     * @param host
     * @param user
     * @param password
     * @return
     */
    public static boolean authenticate(String host,String user,String password){
        try (JmAuthenticate jm = new JmAuthenticate()) {
            jm.connect(host,user,password);
            // try-with-resourcesで自動的にclose()が呼ばれる
        }catch(NoSuchProviderException e1){
            return false;
        }catch(MessagingException e2){
            return false;
        }
        return true;
    }
    /**
     * コンストラクタ
     * @throws NoSuchProviderException
     */
    public JmAuthenticate() throws NoSuchProviderException {
        Session session = Session.getInstance(new Properties(), null);
        store = session.getStore("pop3");
    }
    /**
     * 接続
     * @param host
     * @param user
     * @param password
     * @throws MessagingException
     */
    public synchronized void connect(String host,
                                     String user,
                                     String password) throws MessagingException {
        store.connect(host, -1, user, password);
    }
    /**
     * 切断
     */
    public synchronized void disconnect() {
        try {
            store.close();
        } catch (MessagingException e) {
            Logger.getLogger(JmSender.class.getName()).log(Level.SEVERE, null,e);
        }
    }

    @Override
    public void close() {
        disconnect();
    }
}