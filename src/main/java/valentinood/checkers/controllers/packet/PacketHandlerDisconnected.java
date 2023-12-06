package valentinood.checkers.controllers.packet;

import javafx.application.Platform;
import valentinood.checkers.network.PacketListener;
import valentinood.checkers.network.annotations.SingleRegistration;
import valentinood.checkers.network.packet.PacketConnectionDisconnect;
import valentinood.checkers.util.AlertUtils;

@SingleRegistration
public class PacketHandlerDisconnected implements PacketListener<PacketConnectionDisconnect> {
    @Override
    public void received(PacketConnectionDisconnect packet) throws Exception {
        Platform.runLater(() -> {
            AlertUtils.information("You have been disconnected", packet.getMessage());
            Platform.exit();
        });
    }
}
