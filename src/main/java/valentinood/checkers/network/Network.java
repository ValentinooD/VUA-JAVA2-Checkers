package valentinood.checkers.network;

import valentinood.checkers.Constants;
import valentinood.checkers.network.packet.Packet;
import valentinood.checkers.network.packet.PacketConnectionKeepAlive;
import valentinood.checkers.network.packet.PacketConnectionResult;
import valentinood.checkers.network.server.Server;
import valentinood.checkers.util.NetworkUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Network {
    private Server server = null;
    private Socket client;

    private final Queue<Packet> packetQueue;
    private final Map<Class<? extends Packet>, List<PacketListener<?>>> listenerMap;

    private final int port;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public Network(int port) {
        this.port = port;
        this.packetQueue = new ConcurrentLinkedQueue<>();
        this.listenerMap = new HashMap<>();

        register(new PacketListener<PacketConnectionKeepAlive>() {
            @Override
            public void received(PacketConnectionKeepAlive packet) throws Exception {
                send(new PacketConnectionKeepAlive(packet.getNumber()));
            }
        });
    }

    public void start() throws Exception {
        // if the port is free that means the server doesn't exist, and it will create a server
        if (NetworkUtils.isPortFree(Constants.DEFAULT_HOST, port)) {
            server = new Server(port);
            new Thread(() -> server.start()).start();
            Thread.sleep(2500); // wait a second for the server to start
        }

        client = new Socket(Constants.DEFAULT_HOST, port);

        new Thread(() -> connect(client)).start();
    }

    private void connect(Socket socket) {
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            new Thread(this::handleQueue).start();
            listen(socket);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void send(Packet packet) {
        packetQueue.add(packet);
    }

    private void handleQueue() {
        while (true) {
            try {
                while (!packetQueue.isEmpty()) {
                    Packet packet = packetQueue.remove();
                    oos.writeObject(packet);

                    System.out.println("-> " + packet);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void listen(Socket socket) throws IOException {
        try {
            while (true) {
                // Handle listen
                Object object = ois.readObject();
                if (object == null) continue;

                System.out.println("<- " + object);

                if (object instanceof Packet) {
                    sendEvent((Packet) object);
                }

                if (object instanceof PacketConnectionResult packet) {
                    if (!packet.getResult().isAllowed()) {
                        disconnect();

                        ois.close();
                        oos.close();
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            oos.close();
            ois.close();
        }
    }

    private void disconnect() {
        System.out.println("Stopped");
    }

    private void sendEvent(Packet packet) {
        try {
            List<PacketListener<?>> list = listenerMap.getOrDefault(packet.getClass(), new ArrayList<>());
            if (list.isEmpty()) return;

            for (PacketListener<?> listener : list) {
                Method method = listener.getClass().getMethod("received", packet.getClass());
                method.setAccessible(true);
                method.invoke(listener, packet);
            }
        } catch (Exception ex) {
            System.err.println("Exception while processing packet event: " + packet);
            ex.printStackTrace();
        }
    }

    public void register(PacketListener<?> listener) {
        Class<?> handlerClass = listener.getClass();
        ParameterizedType pt = (ParameterizedType) handlerClass.getGenericInterfaces()[0];
        Type actualType = pt.getActualTypeArguments()[0];
        Class<? extends Packet> clazz = (Class<? extends Packet>) actualType;

        List<PacketListener<?>> list = listenerMap.getOrDefault(clazz, new ArrayList<>());
        list.add(listener);
        listenerMap.put(clazz, list);
    }

    public void unregister(Class<? extends Packet> clazz) {
        listenerMap.remove(clazz);
    }

    public void startOnThread() {
        new Thread(() -> {
            try {
                start();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    public void sendOnThread(Packet packet) {
        new Thread(() -> send(packet)).start();
    }

    public static void main(String[] args) {
        System.out.println("[SERVER] Starting standalone server on port 1908");
        Server srv = new Server(1908);
        srv.start();
    }
}
