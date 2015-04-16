package org.repetti.srs.core;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.Security;

/**
 * @author repetti
 */
public class Coder {
    private static final int defaultSaultLength = 64;
    private static final int iterationCount = 128;
    private static final int defaultKeyLength = 64;
    private static final String defaultAlgorithm = "Blowfish";

    private final SecureRandom random = new SecureRandom();
    private final SecretKeyFactory keyFactory;
    private final Cipher cipher;

    public Coder() {
        try {
            Security.insertProviderAt(new BouncyCastleProvider(), 1);
            keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES", "BC");
            cipher = Cipher.getInstance("Blowfish/ECB/PKCS7Padding", "BC");
        } catch (Exception e) {
            throw new RuntimeException("Unable to start session security", e);
        }
    }

    public byte[] encode(byte[] plain, char[] passPhrase) {
//        byte[] b = new byte[defaultSaultLength];
//        ByteBuffer bb = ByteBuffer.wrap(b);
        byte[] salt = random.generateSeed(defaultSaultLength);
//        bb.put(salt);

        byte[] res;
        try {
            PBEKeySpec keySpec = new PBEKeySpec(passPhrase, salt, iterationCount, defaultKeyLength);//, salt.getBytes(), iterations, defaultKeyLength);
            SecretKey key = keyFactory.generateSecret(keySpec);
            SecretKeySpec cipherKey = new SecretKeySpec(key.getEncoded(), defaultAlgorithm);
            cipher.init(Cipher.ENCRYPT_MODE, cipherKey/*, random*/);
            res = cipher.doFinal(plain);
        } catch (Exception e) {
            throw new RuntimeException("Unable to generate session", e);
        }
        byte[] ret = new byte[4 + salt.length + res.length];
        ByteBuffer bb = ByteBuffer.wrap(ret);
        bb.putInt(defaultSaultLength);
        bb.put(salt);
        bb.put(res);

        return ret;
    }

    public byte[] decode(byte[] coded, char[] passPhrase) {
        ByteBuffer bb = ByteBuffer.wrap(coded);
        final int saltLength = bb.getInt();
        byte[] salt = new byte[saltLength];
        bb.get(salt, 0, saltLength);
        byte[] code = new byte[coded.length - saltLength - 4];
        bb.get(code, 0, code.length);

        byte[] ret;
        try {
            PBEKeySpec keySpec = new PBEKeySpec(passPhrase, salt, iterationCount, defaultKeyLength);//, salt.getBytes(), iterations, defaultKeyLength);
            SecretKey key = keyFactory.generateSecret(keySpec);
            SecretKeySpec cipherKey = new SecretKeySpec(key.getEncoded(), defaultAlgorithm);
            cipher.init(Cipher.DECRYPT_MODE, cipherKey/*, random*/);
            ret = cipher.doFinal(code);
        } catch (Exception e) {
            throw new RuntimeException("Unable to generate session", e);
        }
        return ret;
    }

}
