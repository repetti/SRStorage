package org.repetti.srs.core.test;

import org.junit.Test;
import org.repetti.srs.core.ParametrizedCoder;
import org.repetti.srs.core.StringHelper;

import static org.junit.Assert.assertEquals;

/**
 * Date: 05/05/15
 *
 * @author repetti
 */
public class ParametrizedCoderTest {

    private static long time = System.nanoTime();

    /**
     * AES: 128/192/256
     * Blowfish: 64
     */
    @Test
    public void testSimple() {
        ParametrizedCoder c = new ParametrizedCoder();

        String text = "text";
        String pass = "pass";
        String algorithm = "AES/CTR/PKCS7Padding";
        int keyLength = 256;
        int iterationCount = 100;

        byte[] textOriginal = text.getBytes();

        printTime("inited");
        byte[] res = c.encode(textOriginal, pass.toCharArray(), algorithm, keyLength, iterationCount);
        printTime("encoded");

        System.out.println(StringHelper.toHexString(res));

        byte[] ret = c.decode(res, pass.toCharArray(), algorithm, keyLength, iterationCount);
        printTime("decoded");
        String retText = new String(ret);
        System.out.println(retText + " " + new String(textOriginal));
    }

    public static void printTime(String marker) {
        long old = time;
        time = System.nanoTime();
        System.out.println(marker + ": " + time(time - old));
    }

    public static String time(long nanoTime) {
        return nanoTime / 1000_000_000 + "." + (nanoTime / 1000_000 % 1000);
    }

    private boolean check(ParametrizedCoder c, String text, String pass, String algorithm, int keyLength, int iterationCount) {
        byte[] textOriginal = text.getBytes();
        byte[] res = c.encode(textOriginal, pass.toCharArray(), algorithm, keyLength, iterationCount);
        byte[] ret = c.decode(res, pass.toCharArray(), algorithm, keyLength, iterationCount);
        String retText = new String(ret);
        try {
            assertEquals("texts are not same", text, retText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return text.equals(retText);
    }

}
