package org.xman.nosql.util;


import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import java.io.FileInputStream;
import java.security.KeyStore;

public class SslUtil {
    public static KeyManager[] getKeyManagers() {
        String pass = "lib.pass";
        try {
            KeyStore keyStore = KeyStore.getInstance("JKS");

            keyStore.load(new FileInputStream("keystore.jks"), pass.toCharArray());

            KeyManagerFactory keyManagerFactory = KeyManagerFactory
                    .getInstance(KeyManagerFactory.getDefaultAlgorithm());

            keyManagerFactory.init(keyStore, pass.toCharArray());

            return keyManagerFactory.getKeyManagers();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
