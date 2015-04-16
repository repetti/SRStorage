package org.client;

import org.apache.tomcat.websocket.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.Endpoint;
import javax.websocket.MessageHandler;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author repetti
 */
public class ClientMini {
    public static final Object syncObj = new Object();
    private static final Logger log = LoggerFactory.getLogger(ClientMini.class);
//    private static final String url = "ws://auth:18080/us-server/wsus";
//    private static final String url = "ws://auth:18080/tomcat7embedNoWebXml-1/websocket";
//    private static final String url = "ws://echo.websocket.org/";
//    private static final String url = "wss://echo.websocket.org/";
//    private static final String url = "ws://0.0.0.0:8080/native_test/chat";
private static final String url = "ws://spring:m%40nag3r@auth:18080/us-server/wsus";

    public static void main(String[] args) throws Exception {
        List<javax.websocket.Extension> extensions = Constants.INSTALLED_EXTENSIONS;
        ClientEndpointConfig cec = ClientEndpointConfig.Builder.create()
                .extensions(extensions)
                .build();
        javax.websocket.WebSocketContainer container =
                javax.websocket.ContainerProvider.getWebSocketContainer();

        ClientEndpointMini clientEndpointMini = new ClientEndpointMini();

        System.out.println(container.getInstalledExtensions());

//        container.setDefaultMaxSessionIdleTimeout(1000);
        URI uri = new URI(url);
        System.out.println("uri: " + uri);
        Session s = container.connectToServer((Endpoint) clientEndpointMini, cec, uri);
        s.addMessageHandler(new MessageHandler.Whole<ByteBuffer>() {
            @Override
            public void onMessage(ByteBuffer message) {
                log.debug("onMessage b {}", message);
            }
        });
        s.addMessageHandler(new MessageHandler.Whole<PongMessage>() {
            @Override
            public void onMessage(PongMessage message) {
                ByteBuffer bb  = message.getApplicationData();
//                bb.asLongBuffer().get()
                log.debug("onMessage p {}", Arrays.toString(bb.array()));
                log.debug("ping now    {}", Arrays.toString(ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array()));
            }
        });

//        container.connectToServer(MyClientEndpoint.class, new URI(url));
        TimeUnit.SECONDS.sleep(1);

        log.info("do wait");
        synchronized (syncObj) {
            syncObj.wait();
        }
        log.info("do end");
        System.exit(0);

    }
}
