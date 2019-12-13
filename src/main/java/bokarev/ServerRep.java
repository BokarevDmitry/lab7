package bokarev;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.SortedMap;

public class ServerRep {
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
