package valentinood.checkers.network.server;

import valentinood.checkers.game.piece.PieceTeam;
import valentinood.checkers.network.packet.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Calendar;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private final int port;

    private final Random random;
    private final Map<Socket, PlayerConnection> connections;
    private final Map<Integer, Socket> portsMap;

    public Server(int port) {
        this.port = port;
        this.random = new Random();
        this.connections = new ConcurrentHashMap<>();
        this.portsMap = new ConcurrentHashMap<>();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Listening on " + serverSocket.getLocalPort());

            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("Received connection: " + socket.getPort());
                new Thread(() -> handle(socket)).start();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void handle(Socket socket) {
        try {
            if (socket.isClosed()) {
                System.out.println("Connection closed: " + socket.getPort());
                return;
            }

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            oos.writeObject(new PacketConnection()); // tells the client we're waiting for them

            while (true) {
                if (socket.isClosed()) break;

                Object in = ois.readObject();
                if (in == null) continue;

                if (!connections.containsKey(socket)) {
                    Object obj = handleBeginConnection(in);

                    if (obj instanceof PacketConnectionResult packet) {
                        if (packet.getResult() == PacketConnectionResult.ConnectionResult.ACCEPTED) {
                            PlayerConnection connection = new PlayerConnection(socket, ois, oos, packet.getUsername(), packet.getColumns(), packet.getRows());
                            connections.put(socket, connection);

                            System.out.println("Port " + socket.getPort() + " is " + packet.getUsername());

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

                            System.out.println("Port " + socket.getPort() + " (" + packet.getUsername() + ") is forwarding to " + other.getSocket().getPort() + " (" + other.getName() + ")");
                        }
                    }

                    continue;
                }

                PlayerConnection pc = connections.getOrDefault(socket, null);
                // Handle keep alive
                long millis = Calendar.getInstance().getTimeInMillis();

                if (millis - pc.getLastKeepAlive() > 5000) {
                    int number = random.nextInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
                    oos.writeObject(new PacketConnectionKeepAlive(number));
                    pc.setKeepAliveNumber(number);
                    pc.setLastKeepAlive(millis);
                }

                if (in instanceof PacketConnectionKeepAlive packet) {
                    if (packet.getNumber() == pc.getKeepAliveNumber()) {
                        pc.setLastKeepAlive(millis);
                    } else {
                        System.out.println("Wrong number");
                    }

                    continue;
                }

                if (pc.getPortForward() == -1) {
                    continue;
                } else {
                    PlayerConnection other = connections.get(portsMap.get(pc.getPortForward()));
                    other.getObjectOutputStream().writeObject(in);

                    System.out.println(socket.getPort() + " -> " + other.getPortForward() + ": " + in);
                }
            }

            System.out.println("Connection closed: " + socket.getPort());
        } catch (Exception ex) {
            if (ex instanceof SocketException) return;

            if (connections.containsKey(socket)) {
                System.err.println("Exception caused by socket " + connections.get(socket) + ":");
            } else {
                System.err.println("Exception caused by socket " + socket.getPort() + ":");
            }

            ex.printStackTrace();
        }
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
}
