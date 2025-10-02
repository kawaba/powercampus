package tktools;

// パスフレーズによる暗号化
// （DES 暗号使用）
import java.io.UnsupportedEncodingException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class DesEncrypter {
    Cipher ecipher;
    Cipher dcipher;
    //
    // 錯乱因子
    byte[] salt = {
        (byte)0xA9, (byte)0x9B, (byte)0xC8, (byte)0x32,
        (byte)0x56, (byte)0x35, (byte)0xE3, (byte)0x03
    };
    // 錯乱繰り返し回数
    int iterationCount = 19;
    //
    // コンストラクタ
    public DesEncrypter(String passPhrase) {
            try {
                // 暗号キーを生成
                KeySpec keySpec = new PBEKeySpec(passPhrase.toCharArray(), salt, iterationCount);
                SecretKey key = SecretKeyFactory.getInstance("PBEWithMD5AndDES").generateSecret(keySpec);
                //
                // Cipher オブジェクトのインスタンスを生成する
                ecipher = Cipher.getInstance(key.getAlgorithm());
                dcipher = Cipher.getInstance(key.getAlgorithm());
                //
                // Cipher に渡すPBEパラメータを準備
                AlgorithmParameterSpec paramSpec = new PBEParameterSpec(salt, iterationCount);
                //
                // Cipher オブジェクトを初期化する
                ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
                dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
                //
            } catch (java.security.InvalidAlgorithmParameterException e) {
            } catch (java.security.spec.InvalidKeySpecException e) {
            } catch (javax.crypto.NoSuchPaddingException e) {
            } catch (java.security.NoSuchAlgorithmException e) {
            } catch (java.security.InvalidKeyException e) {
            }
    }
    // 暗号化
    public String encrypt(String str) {
        try {
            // 対象文字列を utf-8 のバイト列にする
            byte[] utf8 = str.getBytes("UTF8");
            // 暗号化
            byte[] enc = ecipher.doFinal(utf8);
            // 文字列にするため base64 変換する
            return Base64.encodeBytes(enc);
		} catch (UnsupportedEncodingException e3) {

		} catch (javax.crypto.BadPaddingException e1) {
			 
		} catch (IllegalBlockSizeException e2) {
        	
        }
        return null;
    }
    // 復号化
    public String decrypt(String str) {
        try {
            // base64 からもとのバイナリに戻す
            byte[] dec = Base64.decode(str);
            // 平文化
            byte[] utf8 = dcipher.doFinal(dec);
            // utf-8 の文字列に変換
            return new String(utf8, "UTF8");
        } catch (javax.crypto.BadPaddingException e) {
        } catch (IllegalBlockSizeException e) {
        } catch (java.io.IOException e) {
        }
        return null;
    }
    /*
    // 使い方の例示
    public static void main(String[] args) {
        
        try {
            String passphrase   = "This is a secret key !";
            String targetString = "暗－号－文－で－す!";
            //
            // Create encrypter/decrypter class
            DesEncrypter encrypter = new DesEncrypter(passphrase);
            
            // Encrypt
            String strIn = Cp932.toJIS(targetString);
            //
            String encrypted = encrypter.encrypt(strIn);
            System.out.println("encrypted = " + encrypted);
            System.out.println();
            // Decrypt
            String decrypted = encrypter.decrypt(encrypted);
            String strOut = Cp932.toCp932(decrypted);
            System.out.println("decrypted = " + strOut);
        } catch (Exception e) {
        }
    }
    */
}