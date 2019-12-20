package bokarev;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

public class Storage {
    private static int left;
    private static int right;
    private static final String BACKEND_ADDR = "tcp://localhost:5560";
    private static final String GET = "GET";
    private static final String SET = "SET";
    private static final String NEW = "NEW";
    private static final String DASH = "-";
    private static final String NOTIFY = "NOTIFY";
    private static final String VALUE_CHANGED = "VALUE CHANGED";

    public static void main(String[] args) {
        String cache = args[0];
        left = Integer.parseInt(args[1]);
        right = Integer.parseInt(args[2]);
        char[] arr = new char[right-left+1];
        for (int i=0; i<right-left+1; i++) {
            arr[i] = cache.charAt(i);
        }

        try (ZContext context = new ZContext()){
            Socket dealer = context.createSocket(SocketType.DEALER);
            dealer.connect(BACKEND_ADDR);
            System.out.println("Connected to Proxy Backend 5560...");
            long start = System.currentTimeMillis();
            Poller poller = context.createPoller(1);
            poller.register(dealer, Poller.POLLIN);
            ZMsg messageSend = new ZMsg();
            messageSend.add(NEW);
            messageSend.addString(left + DASH + right);
            messageSend.send(dealer);

            while (!Thread.currentThread().isInterrupted()) {
                poller.poll(1);
                if (System.currentTimeMillis() - start > 5000) {
                    messageSend = new ZMsg();
                    messageSend.add(NOTIFY);
                    messageSend.addString(left + DASH + right);
                    messageSend.send(dealer);
                    start = System.currentTimeMillis();
                }
                if (poller.pollin(0)) {
                    ZMsg messageReceive = ZMsg.recvMsg(dealer);
                    if (messageReceive.size() == 2) {
                        int index = Integer.parseInt(messageReceive.pollLast().toString());
                        ZFrame address = messageReceive.pop();
                        String answer = ""+arr[index];
                        sendMessage(GET, address, answer, dealer);
                    }

                    if (messageReceive.size() == 3) {
                        String value = messageReceive.pollLast().toString();
                        int index = Integer.parseInt(messageReceive.pollLast().toString());
                        ZFrame address = messageReceive.pop();
                        arr[index] = value.charAt(0);
                        sendMessage(SET, address, VALUE_CHANGED, dealer);
                    }
                }
            }
        }
    }

    private static void sendMessage(String type, ZFrame address, String answer, Socket to) {
        ZMsg responseMessage = new ZMsg();
        responseMessage.add(type);
        responseMessage.add(address);
        responseMessage.add(answer);
        responseMessage.send(to);
    }

}
