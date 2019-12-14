package bokarev;

import javafx.util.Pair;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import java.util.HashMap;
import java.util.Map;


public class Main {
    private static final String FRONTEND_ADDR = "tсp://localhost:5559";
    private static final String BACKEND_ADDR = "tcp://localhost:5560";
    private static final String GET = "GET";
    private static final String SET = "SET";
    private static final String NEW = "NEW";
    private static  final String NOTIFY = "NOTIFY";
    private static final int DOUBLE_TIMEOUT = 10000;
    private static final String DASH = "-";

    private static Socket frontend;
    private static Socket backend;
    private static HashMap<Pair<Integer, Integer>, Pair<ZFrame, Long>> storage = new HashMap<>();



    public static void main(String[] args) {
        try (ZContext context = new ZContext()){
            frontend = context.createSocket(SocketType.ROUTER);
            frontend.bind(FRONTEND_ADDR);

            backend = context.createSocket(SocketType.ROUTER);
            backend.bind(BACKEND_ADDR);

            Poller items = context.createPoller(2);
            items.register(frontend, Poller.POLLIN);
            items.register(backend, Poller.POLLIN);
            boolean more;

            while (!Thread.currentThread().isInterrupted()) {
                items.poll();

                if (items.pollin(0)) {
                    while (true) {
                        ZMsg message = ZMsg.recvMsg(frontend);
                        ZFrame address = message.unwrap();
                        for (ZFrame f : message) {
                            if (f.toString().equals(GET)) {
                                ZMsg getMessage = new ZMsg();
                                boolean found = false;
                                int index = Integer.parseInt(message.getLast().toString());
                                for (Map.Entry<Pair<Integer, Integer>, Pair<ZFrame, Long>> entry : storage.entrySet()) {
                                    if (index >= entry.getKey().getKey() && index < entry.getKey().getValue() && isAlive(entry)) {
                                        found = true;
                                        getMessage.add(entry.getValue().getKey().duplicate());
                                        getMessage.add(address);
                                        getMessage.add(message.getLast());
                                        break;
                                    }
                                }
                                send(getMessage, found, address, index);
                                break;
                            }
                            if (f.toString().equals(SET))

                        }
                    }
                }
            }




        }

    }
}


    /*public void run() {
        ZContext ctx = new ZContext();

// execute state snapshot request
        ZMQ.Socket snapshot = ctx.createSocket(SocketType.ROUTER);
        snapshot.bind("tcp://*:5556");
        ZMQ.Socket publisher = ctx.createSocket(SocketType.PUB);
        publisher.bind("tcp://*:5557");
        ZMQ.Socket collector = ctx.createSocket(SocketType.PULL);
        collector.bind("tcp://*:5558");

        ZMQ.Poller poller = ctx.createPoller(2);
        poller.register(collector, ZMQ.Poller.POLLIN);
        poller.register(snapshot, ZMQ.Poller.POLLIN);
        long sequence = 0;
        while (!Thread.currentThread().isInterrupted()) {
            if (poller.poll(1000) < 0) break;
        }
// apply state updates from main thread
        if (poller.pollin(0)) {
            kvsimple kvMsg = kvsimple.recv(collector);
            if (kvMsg == null) break;
            kvMsg.setSequence(++sequence);
            kvMsg.send(publisher);
            //clonesrv3.kvMap.put(kvMsg.getKey(), kvMsg);
            System.out.printf("I: publishing update %5d\n", sequence);
        }


        if(poller.pollin(1)) {
            byte[] identity = snapshot.recv(0);
            if (identity == null) return; // Interrupted
            String request = snapshot.recvStr();
            if (!request.equals("ICANHAZ?")) {
                System.out.println("E: bad request, aborting");
                return;
            }
            Iterator<Map.Entry<String, kvsimple>> iter = kvMap.entrySet().iterator();
            while (iter.hasNext()) {
                Map.Entry<String, kvsimple> entry = iter.next();
                kvsimple msg = entry.getValue();
                System.out.println("Sending message " + entry.getValue().getSequence());
                this.sendMessage(msg, identity, snapshot);
            }
            // now send end message with sequence number
            System.out.println("Sending state snapshot = " + sequence);
            snapshot.send(identity, ZMQ.SNDMORE);
            kvsimple message = new kvsimple("KTHXBAI", sequence, "".getBytes());
            message.send(snapshot);
        }

        System.out.printf(" Interrupted\n%d messages handled\n", sequence);
        ctx.destroy();
    }

    private void sendMessage(kvsimple msg, byte[] identity, ZMQ.Socket snapshot) {
        snapshot.send(identity, ZMQ.SNDMORE);
        msg.send(snapshot);
    }
*/


