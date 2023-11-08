package valentinood.checkers.network;

import valentinood.checkers.network.packet.Packet;

public interface PacketListener<T extends Packet> {
    void received(T packet) throws Exception;
}
