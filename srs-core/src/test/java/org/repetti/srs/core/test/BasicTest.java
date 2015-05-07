package org.repetti.srs.core.test;

import org.junit.Test;
import org.repetti.srs.core.Coder;
import org.repetti.utils.StringHelper;

/**
 * Date: 04/05/15
 *
 * @author repetti
 */
public class BasicTest {
    private static long time = System.nanoTime();

    @Test
    public void testSimple() throws Exception {
        Coder c = new Coder();

        String text = "Thrift (англ. бережливость, произносится как θrɪft) — язык описания интерфейсов, который используется для определения и создания служб под разные языки программирования. Является фреймворком к удаленному вызову процедур (RPC). Используется компанией Facebook в качестве масштабируемого кросс-языкового сервиса по разработке. Сочетает в себе программный конвейер с движком генерации кода для разработки служб, в той или иной степени эффективно и легко работающих между такими языками как C#, C++, Cappuccino, Cocoa, Delphi, Erlang, Go, Haskell, Java, OCaml, Perl, PHP, Python, Ruby и Smalltalk. Проще говоря, Thrift является двоичным протоколом связи. С апреля 2007 разрабатывается как open source проект компанией Apache Software Foundation.";
        String pass = "pass";

        byte[] textOriginal = text.getBytes();

        printTime("before");
        byte[] res = c.encode(textOriginal, pass.toCharArray());
        printTime("encoded");
        System.out.println(StringHelper.toHexString(res));

        byte[] ret = c.decode(res, pass.toCharArray());
        printTime("decoded");
        String retText = new String(ret);
        System.out.println(retText + "\n" + new String(textOriginal));
    }

    public static void printTime(String marker) {
        long old = time;
        time = System.nanoTime();
        System.out.println(marker + ": " + time(time - old));
    }

    public static String time(long nanoTime) {
        return nanoTime / 1000_000_000 + "." + (nanoTime / 1000_000 % 1000);
    }
}
