package bokarev;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;


public class Main {
    private static final String FRONTEND_ADDR = "t"




    public static void main(String[] args) {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket responder = context.socket(SocketType.REP);
        responder.connect("tcp://localhost:5560");
        System.out.println("Waiting for requests...");
        while (!Thread.currentThread().isInterrupted()) {
            String string = responder.recvStr(0);
            System.out.printf("Received request: [%s]\n", string);
            responder.send("World");
        }
        responder.close();
        context.term();

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


