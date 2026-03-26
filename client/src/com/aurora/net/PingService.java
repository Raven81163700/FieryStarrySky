package com.aurora.net;

import java.io.IOException;

public final class PingService {

    private final TcpClient tcpClient;

    public PingService(String host, int port) {
        this.tcpClient = new TcpClient(host, port);
    }

    public int measureLatencyMillis() {
        long start = System.currentTimeMillis();
        String line;
        try {
            line = tcpClient.sendAndReadLine(GameProtocol.buildPing());
        } catch (IOException e) {
            return -1;
        }

        if (!GameProtocol.CMD_PING_ACK.equals(line)) {
            return -1;
        }

        long elapsed = System.currentTimeMillis() - start;
        if (elapsed < 0) {
            return -1;
        }
        if (elapsed > 2147483647L) {
            return 2147483647;
        }
        return (int) elapsed;
    }
}
