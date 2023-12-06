package valentinood.checkers.network.server;

import valentinood.checkers.Constants;
import valentinood.checkers.game.piece.PieceTeam;
import valentinood.checkers.network.jndi.ConfigurationReader;
import valentinood.checkers.network.packet.*;
import valentinood.checkers.network.rmi.RemoteChatService;
import valentinood.checkers.network.rmi.RemoteChatServiceImpl;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Calendar;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private final int port;

    private ServerSocket serverSocket;
    private final Random random;
    private final Map<Socket, PlayerConnection> connections;
    private final Map<Integer, Socket> portsMap;

    private RemoteChatService remoteChatService;

    private boolean running = true;

    public Server(int port) {
        this.port = port;
        this.random = new Random();
        this.connections = new ConcurrentHashMap<>();
        this.portsMap = new ConcurrentHashMap<>();
    }

    public void start() {
        new Thread(this::inactivityWatchdog, "ServerInactivityWatchdog").start();

        try {
            startRmiChat();

            serverSocket = new ServerSocket(port);
            log("Listening on " + serverSocket.getLocalPort());

            while (running) {
                Socket socket = serverSocket.accept();
                log("Received connection: " + socket.getPort());
                new Thread(() -> handle(socket), "ServerConnectionThread-" + socket.getPort()).start();
            }

            serverSocket.close();
        } catch (IOException e) {
            if (e.getMessage().equals("Socket closed")) {
                log("Server shut down");
                return;
            }

            throw new RuntimeException(e);
        }
    }

    private void startRmiChat() throws RemoteException {
        Registry registry = LocateRegistry.createRegistry(ConfigurationReader.getInt(ConfigurationReader.Key.RMI_PORT));
        remoteChatService = new RemoteChatServiceImpl();
        RemoteChatService skeleton = (RemoteChatService) UnicastRemoteObject.exportObject(
                remoteChatService,
                ConfigurationReader.getInt(ConfigurationReader.Key.RMI_PORT)
        );
        registry.rebind(RemoteChatService.REMOTE_OBJECT_NAME, skeleton);
        log("RemoteChatService registered");
    }

    public void handle(Socket socket) {
        try {
            if (socket.isClosed()) {
                log("Connection closed: " + socket.getPort());
                return;
            }

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            oos.writeObject(new PacketConnection()); // tells the client we're waiting for them

            while (running) {
                if (socket.isClosed()) break;

                PlayerConnection pc = connections.getOrDefault(socket, null);

                // Handle keep alive
                if (pc != null) {
                    long millis = System.currentTimeMillis();
                    if (pc.getKeepAlive() == null && millis - pc.getLastKeepAlive() > Constants.KEEP_ALIVE_INTERVAL_MILLIS) {
                        int number = random.nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE);

                        PacketConnectionKeepAlive p = new PacketConnectionKeepAlive(number);
                        oos.writeObject(new PacketConnectionKeepAlive(number));

                        pc.setKeepAlivePacket(p);
                        pc.setLastKeepAlive(millis);
                    } else if (millis - pc.getLastKeepAlive() > Constants.KEEP_ALIVE_INTERVAL_MILLIS * 2) {
                        log(pc + " timed out.");
                        disconnect(pc);
                        break;
                    }
                }

                // If nothing is available continue, so that we don't block the thread
                if (socket.getInputStream().available() == 0) continue;

                Object in = ois.readObject();
                if (in == null) continue;

                if (!connections.containsKey(socket)) {
                    Object obj = handleBeginConnection(in);

                    if (obj instanceof PacketConnectionResult packet) {
                        if (packet.getResult() == PacketConnectionResult.ConnectionResult.ACCEPTED) {
                            PlayerConnection connection = new PlayerConnection(socket, ois, oos, packet.getUsername(), packet.getColumns(), packet.getRows());
                            connections.put(socket, connection);

                            log("Port " + socket.getPort() + " is " + packet.getUsername());

                            oos.writeObject(obj);

                            PlayerConnection other = assignForwardPorts(socket, connection);
                            if (other == null) continue;

                            PacketGameBegin pgb = new PacketGameBegin(other.getColumns(), other.getRows());
                            pgb.setTeam(PieceTeam.Blue, other.getName());
                            pgb.setTeam(PieceTeam.Red, connection.getName());

                            // other
                            pgb.setPlayerTeam(PieceTeam.Blue);
                            other.getObjectOutputStream().writeObject(pgb);

                            // connection
                            pgb.setPlayerTeam(PieceTeam.Red);
                            oos.writeObject(pgb);

                            log("Port " + socket.getPort() + " (" + packet.getUsername() + ") is forwarding to " + other.getSocket().getPort() + " (" + other.getName() + ")");
                        }
                    }

                    continue;
                }

                if (in instanceof PacketConnectionKeepAlive packet) {
                    assert pc != null;
                    if (packet.getNumber() == pc.getKeepAlive().getNumber()) {
                        pc.setLastKeepAlive(System.currentTimeMillis());
                        pc.setKeepAlivePacket(null);
                    } else {
                        log(pc + " returned an incorrect KeepAlive number. Disconnected.");
                        disconnect(pc);
                    }

                    continue;
                }

                if (in instanceof PacketConnectionDisconnect) {
                    disconnect(pc, new PacketConnectionDisconnect("Disconnect initiated by client: " + ((PacketConnectionDisconnect) in).getMessage()));
                    return;
                }

                if (pc.getPortForward() == -1) {
                    continue;
                } else {
                    PlayerConnection other = connections.get(portsMap.get(pc.getPortForward()));
                    other.getObjectOutputStream().writeObject(in);
                    log(socket.getPort() + " -> " + other.getSocket().getPort() + ": " + in);
                }
            }

            log("Connection closed: " + socket.getPort());
        } catch (Exception ex) {
            if (ex instanceof SocketException) return;
            if (ex instanceof EOFException) return;

            if (connections.containsKey(socket)) {
                log("Exception caused by socket " + connections.get(socket) + ":");
            } else {
                log("Exception caused by socket " + socket.getPort() + ":");
            }

            ex.printStackTrace();
        }
    }

    private void log(String string) {
        System.err.println("[SERVER] " + string);
    }

    private void disconnect(PlayerConnection pc) throws IOException {
        disconnect(pc, null);
    }

    private void disconnect(PlayerConnection pc, PacketConnectionDisconnect packet) throws IOException {
        if (packet != null) pc.getObjectOutputStream().writeObject(packet);

        Socket socket = pc.getSocket();
        socket.close();
        portsMap.remove(socket.getPort());
        connections.remove(socket);
    }

    private PlayerConnection assignForwardPorts(Socket socket, PlayerConnection newConnection) {
        for (Map.Entry<Socket, PlayerConnection> set : connections.entrySet()) {
            if (set.getValue() == newConnection) continue;
            if (set.getValue().getPortForward() != -1) continue;
            if (set.getValue().getColumns() != newConnection.getColumns()) continue;
            if (set.getValue().getRows() != newConnection.getRows()) continue;

            portsMap.put(socket.getPort(), socket);
            portsMap.put(set.getKey().getPort(), set.getKey());

            newConnection.setPortForward(set.getKey().getPort());
            set.getValue().setPortForward(socket.getPort());

            return set.getValue();
        }

        return null;
    }

    private Object handleBeginConnection(Object in) {
        if (!(in instanceof PacketConnection)) {
            // Replied with something other than a connection packet
            return new PacketConnectionResult(PacketConnectionResult.ConnectionResult.BAD_PACKET);
        }

        if (in instanceof PacketConnectionRequest packet) {
            if (packet.getGridWidth() <= 0 || packet.getGridHeight() <= 0 || packet.getName().isBlank()
                    || packet.getGridWidth() > 20 || packet.getGridHeight() > 20
                    || packet.getGridWidth() != packet.getGridHeight()) {

                return new PacketConnectionResult(PacketConnectionResult.ConnectionResult.BAD_PACKET);
            } else {
                return new PacketConnectionResult(packet.getName(), packet.getGridWidth(), packet.getGridHeight(), PacketConnectionResult.ConnectionResult.ACCEPTED);
            }
        }

        return null;
    }

    private void inactivityWatchdog() {
        try {
            while (running) {
                if (!connections.isEmpty()) continue;

                // Keeps track of when all connections have disconnected
                long millisAtNoConnections = System.currentTimeMillis();

                // Enter loop when connections are empty
                while (connections.isEmpty()) {
                    Thread.sleep(500);
                    long current = Calendar.getInstance().getTimeInMillis();

                    // If server idles for 10 seconds
                    if (current - millisAtNoConnections >= 10_000) {
                        log("Server has been inactive for 10 seconds. Shutting down...");
                        running = false;
                        try {
                            serverSocket.close();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
