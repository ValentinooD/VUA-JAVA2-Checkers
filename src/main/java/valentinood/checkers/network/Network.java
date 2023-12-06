package valentinood.checkers.network;

import javafx.application.Platform;
import valentinood.checkers.network.annotations.OnFXThread;
import valentinood.checkers.network.annotations.SingleRegistration;
import valentinood.checkers.network.jndi.ConfigurationReader;
import valentinood.checkers.network.packet.Packet;
import valentinood.checkers.network.packet.PacketConnectionDisconnect;
import valentinood.checkers.network.packet.PacketConnectionKeepAlive;
import valentinood.checkers.network.packet.PacketConnectionResult;
import valentinood.checkers.network.rmi.RemoteChatService;
import valentinood.checkers.network.server.Server;
import valentinood.checkers.util.NetworkUtils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.Socket;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
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
    private boolean disconnected = false;
    public static RemoteChatService remoteChatService;

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
        if (NetworkUtils.isPortFree(
                ConfigurationReader.getString(ConfigurationReader.Key.HOST), port)) {
            server = new Server(port);
            new Thread(() -> server.start(), "Server").start();
            Thread.sleep(2500); // wait a second for the server to start
        }

        Registry registry = LocateRegistry.getRegistry(
                ConfigurationReader.getString(ConfigurationReader.Key.HOST),
                ConfigurationReader.getInt(ConfigurationReader.Key.RMI_PORT));
        remoteChatService = (RemoteChatService) registry.lookup(RemoteChatService.REMOTE_OBJECT_NAME);

        client = new Socket(ConfigurationReader.getString(ConfigurationReader.Key.HOST), port);
        connect(client);
    }

    private void connect(Socket socket) {
        try {
            oos = new ObjectOutputStream(socket.getOutputStream());
            ois = new ObjectInputStream(socket.getInputStream());

            new Thread(this::handleQueue, "NetworkClient-PacketQueue").start();
            new Thread(() -> listen(client), "NetworkClient-Listener").start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void chat(String message) {
        try {
            remoteChatService.send(message);
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }
    }

    public void send(Packet packet) {
        packetQueue.add(packet);
    }

    private void handleQueue() {
        while (!disconnected) {
            try {
                while (!packetQueue.isEmpty() && !disconnected) {
                    Packet packet = packetQueue.remove();
                    oos.writeObject(packet);

//                    System.out.println("-> " + packet);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void listen(Socket socket) {
        try {
            while (!disconnected && socket.isConnected()) {
                if (socket.getInputStream().available() == 0) continue;

                // Handle listen
                Object object = ois.readObject();
                if (object == null) continue;

//                System.out.println("<- " + object);

                if (object instanceof Packet) {
                    sendEvent((Packet) object);
                }

                if (object instanceof PacketConnectionResult packet) {
                    if (!packet.getResult().isAllowed()) {
                        disconnect();

                        break;
                    }
                }

                if (object instanceof PacketConnectionDisconnect) {
                    disconnect();
                    break;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            disconnect();
        }
    }

    public void stop() {
        try {
            // writing directly
            oos.writeObject(new PacketConnectionDisconnect());
            disconnect();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void disconnect() {
        try {
            disconnected = true;
            ois.close();
            oos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void sendEvent(Packet packet) {
        try {
            List<PacketListener<?>> list = listenerMap.getOrDefault(packet.getClass(), new ArrayList<>());
            if (list.isEmpty()) return;

            for (PacketListener<?> listener : list) {
                Method method = listener.getClass().getMethod("received", packet.getClass());
                method.setAccessible(true);

                if (method.getDeclaringClass().isAnnotationPresent(OnFXThread.class)) {
                    Platform.runLater(() -> {
                        try {
                            method.invoke(listener, packet);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } else {
                    method.invoke(listener, packet);
                }
            }
        } catch (Exception ex) {
            System.err.println("Exception while processing packet event: " + packet);
            ex.printStackTrace();
        }
    }

    public void register(PacketListener<?> listener) {
        Class<?> handlerClass = listener.getClass();

        // TODO: find a way to do this
        if (handlerClass.isSynthetic()) {
            System.err.println(handlerClass.getName() + " is synthetic (likely lambda) and cannot be registered as an event. Ignored.");
            return;
        }

        ParameterizedType pt = (ParameterizedType) handlerClass.getGenericInterfaces()[0];
        Type actualType = pt.getActualTypeArguments()[0];
        Class<? extends Packet> clazz = (Class<? extends Packet>) actualType;

        List<PacketListener<?>> list = listenerMap.getOrDefault(clazz, new ArrayList<>());

        if (handlerClass.isAnnotationPresent(SingleRegistration.class) && !list.isEmpty()) {
            System.err.println(handlerClass.getName() + " is being registered twice but has @SingleRegistration. Ignored.");
            return;
        }

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
        }, "NetworkClient-StartOnThread").start();
    }

    public void sendOnThread(Packet packet) {
        new Thread(() -> send(packet), "NetworkClient-SendOnThread").start();
    }

    public static void main(String[] args) {
        System.out.println("[SERVER] Starting standalone server on port 1908");
        Server srv = new Server(ConfigurationReader.getInt(ConfigurationReader.Key.SERVER_PORT));
        srv.start();
    }
}
