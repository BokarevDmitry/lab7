package bokarev;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import java.util.Scanner;


public class Client {

    private static final String FRONTEND_ADDR = "tcp://localhost:5559";

    public static void main(String[] args) {
        try (ZContext context = new ZContext()){
            Socket req = context.createSocket(SocketType.REQ);
            req.connect(FRONTEND_ADDR);
            System.out.println("Connected to Proxy Frontend 5559...");
            Scanner in = new Scanner(System.in);
            while (true) {
                String[] command = in.nextLine().split(" ");
                if (command[0].equals("STOP")) {
                    break;
                }
                ZMsg message = new ZMsg();
                for (int i=0; i<command.length; i++) {
                    message.add(command[i]);
                }
                message.send(req);
                ZMsg response = ZMsg.recvMsg(req);
                System.out.println(response.pop().toString());
                response.destroy();
                message.destroy();
            }
        }
    }
}
