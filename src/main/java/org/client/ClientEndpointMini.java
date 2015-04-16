package org.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Date: 03/02/15
 * Time: 15:30
 *
 * @author repetti
 */
@javax.websocket.ClientEndpoint
public class ClientEndpointMini extends Endpoint {
    private static final Logger log = LoggerFactory.getLogger(ClientEndpointMini.class);
    private static final ExecutorService es = Executors.newSingleThreadExecutor();

    @Override
    public void onOpen(final Session session, EndpointConfig config) {
        log.debug("onOpen {} {}", session, config);
        session.addMessageHandler(new MessageHandler.Whole<String>() {
            @Override
            public void onMessage(String message) {
                log.debug("onMessage s {}", message);

            }
        });
        session.getAsyncRemote().sendText("mini test");
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        super.onClose(session, closeReason);
        log.debug("onClose {} {}", session, closeReason);
        synchronized (ClientMini.syncObj) {
            ClientMini.syncObj.notifyAll();
        }
    }

    @Override
    public void onError(Session session, Throwable throwable) {
        super.onError(session, throwable);
        log.debug("onError {} {}", session, throwable);
    }

    public static void send(final Session s, final String msg) {
        log.info("send {}", msg);
        es.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    s.getBasicRemote().sendText(msg);
                } catch (Exception e) {
                    log.error("sending failed", e);
                }
                log.info("sent {}", msg);
            }
        });
    }

}
