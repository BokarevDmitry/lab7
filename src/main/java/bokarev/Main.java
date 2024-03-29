package bokarev;

import javafx.util.Pair;
import org.zeromq.*;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;

import java.util.HashMap;
import java.util.Map;


public class Main {
    private static final String FRONTEND_ADDR = "tcp://localhost:5559";
    private static final String BACKEND_ADDR = "tcp://localhost:5560";
    private static final String GET = "GET";
    private static final String SET = "SET";
    private static final String NEW = "NEW";
    private static  final String NOTIFY = "NOTIFY";
    private static final int DOUBLE_TIMEOUT = 10000;
    private static final String DASH = "-";

    private static HashMap<Pair<Integer, Integer>, Pair<ZFrame, Long>> storage = new HashMap<>();

    public static void main(String[] args) {
        ZMQ.Context context = ZMQ.context(1);
        Socket frontend = context.socket(SocketType.ROUTER);
        Socket backend = context.socket(SocketType.ROUTER);

        frontend.bind(FRONTEND_ADDR);
        backend.bind(BACKEND_ADDR);
        System.out.println("launch and connect broker");

        Poller items = context.poller(2);
        items.register(frontend, Poller.POLLIN);
        items.register(backend, Poller.POLLIN);
        boolean more = true;

        while (!Thread.currentThread().isInterrupted()) {
            items.poll();

            if (isClientRequest(items)) {
                handleClient(frontend, backend, more);
            }

            if (isStorageMessage(items)) {
               handleStorage(frontend, backend, more);
            }
        }
    }

    private static void handleClient(Socket frontend, Socket backend, boolean more) {
        while (true) {
            ZMsg message = ZMsg.recvMsg(frontend);
            ZFrame address = message.unwrap();
            for (ZFrame f : message) {
                if (isGetMessage(f)) {
                    handleClientRequest(GET, backend, message, address, null);
                    break;
                }
                if (isSetMessage(f)) {
                    ZFrame value = message.pollLast();
                    handleClientRequest(SET, backend, message, address, value);
                    break;
                }
            }
            more = frontend.hasReceiveMore();
            if (!more) break;
        }
    }

    private static void handleClientRequest(String type, Socket backend, ZMsg message, ZFrame address, ZFrame value) {
        ZMsg newMessage = new ZMsg();
        boolean found = false;

        int index = Integer.parseInt(message.getLast().toString());
        for (Map.Entry<Pair<Integer, Integer>, Pair<ZFrame, Long>> entry : storage.entrySet()) {
            if (index >= entry.getKey().getKey() && index < entry.getKey().getValue() && isAlive(entry)) {
                found = true;
                newMessage.add(entry.getValue().getKey().duplicate());
                newMessage.add(address);
                if (type.equals(GET)) {
                    newMessage.add(message.getLast());
                } else {
                    newMessage.add("" + index);
                    newMessage.add(value);
                }
                break;
            }
        }
        if (found) {
            newMessage.send(backend);
        } else {
            ZMsg errorMessage = new ZMsg();
            errorMessage.wrap(address);
            errorMessage.add("No hash at " + index);
            errorMessage.send(backend);
        }
    }

    private static void handleStorage (Socket frontend, Socket backend, boolean more) {
        while (true) {
            ZMsg message = ZMsg.recvMsg(backend);
            ZFrame address = message.pop();
            String checkFrame = message.popString();
            System.out.println(checkFrame);
            String[] interval;

            switch (checkFrame) {
                case NEW:
                    interval = message.popString().split(DASH);
                    storage.put(new Pair<>(Integer.parseInt(interval[0]),
                            Integer.parseInt(interval[1])), new Pair<>(address, System.currentTimeMillis()));
                    break;

                case NOTIFY:
                    interval = message.popString().split(DASH);
                    storage.replace(new Pair<>(Integer.parseInt(interval[0]),
                            Integer.parseInt(interval[1])), new Pair<>(address, System.currentTimeMillis()));
                    break;

                default:
                    message.wrap(message.pop());
                    message.send(frontend);
            }
            more = backend.hasReceiveMore();
            if (!more) break;
        }
    }



    private static boolean isAlive(Map.Entry<Pair<Integer, Integer>, Pair<ZFrame, Long>> entry) {
        long now = System.currentTimeMillis();
        if (now - entry.getValue().getValue() > DOUBLE_TIMEOUT) {
            storage.remove(entry);
            return false;
        }
        return true;
    }

    private static boolean isClientRequest(Poller items) {
        if (items.pollin(0)) return true;
        return false;
    }

    private static boolean isStorageMessage(Poller items) {
        if (items.pollin(1)) return true;
        return false;
    }

    private static boolean isGetMessage(ZFrame f) {
        if (f.toString().equals(GET)) return true;
        return false;
    }

    private static boolean isSetMessage(ZFrame f) {
        if (f.toString().equals(SET)) return true;
        return false;
    }
}

