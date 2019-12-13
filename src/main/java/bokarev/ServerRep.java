package bokarev;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class ServerRep {
    public static void main(String[] args) {
        ZContext context = new ZContext();
        ZMQ.Socket socket = context.createSocket(SocketType.REP);
        try {
            socket.bind("tcp://localhost:5555");
            System.out.println("bind");
            while (!Thread.currentThread().isInterrupted()) {
                String req = socket.recvStr();
                socket.send("reply!" + req);
            }
        } finally {
            context.destroySocket(socket);
            context.destroy();
        }



    }
}
