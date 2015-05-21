package org.repetti.srs.ui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Date: 05/05/15
 *
 * @author repetti
 */
public class SimpleMain {
    private static final Logger log = LoggerFactory.getLogger(SimpleMain.class);

    public static void main(String[] args) {
        new SimpleMain().start();
        log.debug("initialized");
    }

    private void start() {
        new SimpleFrame().setVisible(true);
        log.debug("Window shown");
    }

}
