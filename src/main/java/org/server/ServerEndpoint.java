package org.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.CloseReason;
import javax.websocket.Extension;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author repetti
 */
@javax.websocket.server.ServerEndpoint(value = "/mars")
public class ServerEndpoint {
	private static final Logger log = LoggerFactory.getLogger(ServerEndpoint.class);
    private static final ExecutorService es = Executors.newSingleThreadExecutor();
	private Session session;
    private final SimpleServerSession sss = new SimpleServerSession();
    private final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();
    private final ExecutorService sender = Executors.newSingleThreadExecutor();
    private final AtomicBoolean active = new AtomicBoolean();

    private static final class Message {
        final Object syncObject = new Object();
        final String message;
        volatile boolean sent;

        private Message(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            final StringBuffer sb = new StringBuffer("Message{");
            sb.append(syncObject);
            sb.append(", message='").append(message).append('\'');
            sb.append('}');
            return sb.toString();
        }
    }

	public ServerEndpoint() {
        log.info("TextSocket websocket created");
	}
	
	@OnOpen
	public void onOpen(Session session) {
        log.info("onOpen");
        this.session = session;
        active.set(true);
        sender.submit(new Runnable() {
            @Override
            public void run() {
                System.out.println("sender start");
                while (active.get()) {
                    Message msg;
                    try {
                        msg = messageQueue.poll(10, TimeUnit.SECONDS);
                    } catch (InterruptedException e) {
                        //TODO close
                        continue;
                    }
                    if (msg == null) {
                        continue;
                    }
                    try {
                        if (ServerEndpoint.this.session == null || ServerEndpoint.this.session.isOpen()) {
                            ServerEndpoint.this.session.getBasicRemote().sendText(msg.message);
                            msg.sent = true;
                            log.info("sent {}", msg);
                        }
                    } catch (IOException e) {
                        msg.sent = false;
                        log.info("not sent {}", msg, e);
                    } finally {
                        synchronized (msg.syncObject) {
                            msg.syncObject.notifyAll();
                        }
                    }
                }
                System.out.println("sender end");

            }
        });
        sss.setEndpoint(this);
		sessionInfo(session);
    }
	
	@OnClose
    public void onClose(CloseReason reason) {
        log.info("onClose {}", reason);
        active.set(false);
        sss.close();
		System.out.println(" - " + reason);
		this.session = null;
	}

	@OnError
    public void onError(Throwable t) {
        log.info("onError", t);
//        sss.close();
	}

	@OnMessage
	public void onMessage(String message) {
        log.info("onMessage {}", message);
        sss.message(message);
	}

	private static void sessionInfo(Session session) {
		System.out.println("starting " + session);
		System.out.println("getNegotiatedSubprotocol: " +  session.getNegotiatedSubprotocol());
		System.out.println("getId: " +  session.getId());
		System.out.println("getProtocolVersion: " +  session.getProtocolVersion());
		System.out.println("getQueryString: " +  session.getQueryString());
		System.out.println("getMaxTextMessageBufferSize: " +  session.getMaxTextMessageBufferSize());

		System.out.println("  getNegotiatedExtensions size=" + session.getNegotiatedExtensions().size());
		for (Extension i : session.getNegotiatedExtensions()) {
			System.out.print("  getNegotiatedExtensions: " + i.getName() + " - ");
			for (Extension.Parameter p : i.getParameters()) {
				System.out.print(p.getName() + ":" + p.getValue());
			}
			System.out.println();
		}

		System.out.println("getPathParameters: " +  session.getPathParameters());
		System.out.println("getRequestParameterMap: " +  session.getRequestParameterMap());
		System.out.println("getRequestURI: " +  session.getRequestURI());
		System.out.println("isOpen: " +  session.isOpen());
		System.out.println("isSecure: " +  session.isSecure());
	}

//    private boolean sendUnsafe(final String msg) {
//        log.info("send {}", msg);
//        if (session == null || session.isOpen()) {
//            return false;
//        }
//        try {
//            session.getBasicRemote().sendText(msg);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//        return true;
//    }

    boolean send(final String msg) {
        final Message m = new Message(msg);
        synchronized (m.syncObject) {
            if (!messageQueue.offer(m)) {
                log.info("Queue overflow, message dropped: {}", msg);
                return false;
            }
            try {
                TimeUnit.SECONDS.timedWait(m.syncObject, 5);
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            }
        }
        return m.sent;

//
//        es.submit(new Runnable() {
//            @Override
//            public void run() {
//                try {
////                    s.getAsyncRemote().sendText(msg);
//                } catch (Exception e) {
//                    log.error("sending failed", e);
//                }
//                log.info("sent {}", msg);
//            }
//        });
    }

}
