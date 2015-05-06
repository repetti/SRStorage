package org.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.Session;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author repetti
 */
public class SimpleServerSession {
    public static final String CMD = "cmd";
    public static final String CMD_LOGIN = "login";
    public static final String CMD_CONNECT = "connect";
    public static final String PARAM = "param";
    private static final Logger log = LoggerFactory.getLogger(SimpleServerSession.class);
    private ScheduledExecutorService es = Executors.newSingleThreadScheduledExecutor();
    private ServerEndpoint endpoint;

    public void setEndpoint(final ServerEndpoint endpoint) {
        log.info("setEndpoint {}", endpoint);
        this.endpoint = endpoint;
        es.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                System.out.println("hb");
                endpoint.send("heartbeat");
            }
        }, 5, 60, TimeUnit.SECONDS);
    }

    public void close() {
        log.info("close");
        es.shutdown();
    }

    public void message(String message) {
        log.info("message {}", message);
        try {
            ConfigLoader cl = ConfigLoader.loadJsonString(message);
//            JsonNode j = JsonHelper.parse(message);
//            ObjectNode o = JsonHelper.castToObjectNode(j);
//            JsonHelper.getString()
            switch (cl.getStringNotNull(CMD)) {
                case CMD_LOGIN:
                    endpoint.send("ok");
                    break;
                case CMD_CONNECT:
//                    endpoint.
                    break;
                default:
                    endpoint.send("command not found");
                    break;
            }
        } catch (UtilsException e) {
            endpoint.send("unable to parse [" + message + "]: " + e.toString());
        }
//        endpoint.send("1: " + message);

    }
}
