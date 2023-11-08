package valentinood.checkers.util;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

public final class NetworkUtils {
    private NetworkUtils() {}


    public static boolean isPortFree(String host, int port) {
        try (Socket socket = new Socket(host, port)) {
            socket.close();
            return false;
        } catch (ConnectException ex) {
            return true;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
