package org.repetti.srs.core;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;
import java.security.Security;

/**
 * https://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html
 *
 * @author repetti
 */
public class ParametrizedCoder {
    private static final Logger log = LoggerFactory.getLogger(ParametrizedCoder.class);

    private static final int defaultSaltLength = 128;
    /**
     * bc/docs/specifications.html
     * <p/>
     * PBEWithMD5AndDES
     * PBKDF2WithHmacSHA1
     */
    private static final String defaultKeyAlgorithm = "PBKDF2WithHmacSHA1";
    private static final String provider = "BC";

    private final SecureRandom random = new SecureRandom();
    private final SecretKeyFactory keyFactory;
//    private final Cipher cipher;

    public ParametrizedCoder() {
        try {
            Security.insertProviderAt(new BouncyCastleProvider(), 1);
            keyFactory = SecretKeyFactory.getInstance(defaultKeyAlgorithm, "BC");
        } catch (Exception e) {
            log.error("Unable to init coder class", e);
            throw new RuntimeException("Unable to start session security", e);
        }
//        try {
//            java.lang.reflect.Field field = Class.forName("javax.crypto.JceSecurity").getDeclaredField("isRestricted");
//            field.setAccessible(true);
//            field.set(null, java.lang.Boolean.FALSE);
//        } catch (Exception e) {
//            log.error("Unable to init coder class", e);
//            throw new RuntimeException("Unable to start session security", e);
//        }
    }

    public byte[] encode(byte[] plain, char[] passPhrase, String algorithm, int keyLength, int iterationCount) {
//        byte[] b = new byte[defaultSaltLength];
//        ByteBuffer bb = ByteBuffer.wrap(b);
        byte[] salt = random.generateSeed(defaultSaltLength);
//        bb.put(salt);

        byte[] res;
        byte[] iv;
        try {
            final Cipher cipher = Cipher.getInstance(algorithm, "BC");
            PBEKeySpec keySpec = new PBEKeySpec(passPhrase, salt, iterationCount, keyLength);//, salt.getBytes(), iterations, defaultKeyLength);
            SecretKey key = keyFactory.generateSecret(keySpec);
            SecretKeySpec cipherKey = new SecretKeySpec(key.getEncoded(), algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, cipherKey/*, random*/);
            AlgorithmParameters params = cipher.getParameters();
            iv = params.getParameterSpec(IvParameterSpec.class).getIV();
            res = cipher.doFinal(plain);
        } catch (Exception e) {
            throw new RuntimeException("Unable to encode", e);
        }
        byte[] ret = new byte[4 + salt.length + 4 + iv.length + res.length];
        ByteBuffer bb = ByteBuffer.wrap(ret);
        log.info("salt length = " + salt.length);
        bb.putInt(salt.length);
        bb.put(salt);
        log.info("iv length = " + iv.length);
        bb.putInt(iv.length);
        bb.put(iv);
        bb.put(res);

        log.info("complete length = " + ret.length);
        return ret;
    }

    public byte[] decode(byte[] coded, char[] passPhrase, String algorithm, int keyLength, int iterationCount) {
        ByteBuffer bb = ByteBuffer.wrap(coded);
        final int saltLength = bb.getInt();
        log.info("salt length = " + saltLength);
        byte[] salt = new byte[saltLength];
        bb.get(salt, 0, saltLength);
        final int ivLength = bb.getInt();
        log.info("iv length = " + ivLength);
        byte[] iv = new byte[ivLength];
        bb.get(iv, 0, ivLength);
        byte[] code = new byte[coded.length - saltLength - 4 - ivLength - 4];
        bb.get(code, 0, code.length);

        byte[] ret;
        try {
            final Cipher cipher = Cipher.getInstance(algorithm, provider);
            PBEKeySpec keySpec = new PBEKeySpec(passPhrase, salt, iterationCount, keyLength);//, salt.getBytes(), iterations, defaultKeyLength);
            SecretKey key = keyFactory.generateSecret(keySpec);
            SecretKeySpec cipherKey = new SecretKeySpec(key.getEncoded(), algorithm);
            cipher.init(Cipher.DECRYPT_MODE, cipherKey, new IvParameterSpec(iv));
            ret = cipher.doFinal(code);
        } catch (Exception e) {
            throw new RuntimeException("Unable to decode", e);
        }
        return ret;
    }

}
