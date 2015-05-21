package org.repetti.srs.ui;

import org.repetti.utils.LoggerHelperSlf4j;

/**
 * Created on 21/05/15.
 *
 * @author repetti
 */
public class SimpleMainDebug {
    static {
        LoggerHelperSlf4j.setDebug();
    }

    public static void main(String[] args) {
        SimpleMain.main(args);
    }
}
