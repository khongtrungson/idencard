package com.idencard.android.card;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Arrays;

/**
 * Communicates with a JavaCard simulator (e.g. jCardSim / JCOP / vsmartcard)
 * over TCP Socket.
 */
public class SocketCardTransceiver implements CardTransceiver {

    private final String host;
    private final int port;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private boolean connected = false;

    public SocketCardTransceiver() {
        // 10.0.2.2 is Android emulator's loopback alias to host machine
        this("10.0.2.2", 9025);
    }

    public SocketCardTransceiver(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public synchronized void connect() throws IOException {
        disconnect();
        socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), 3000);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        connected = true;
    }

    @Override
    public synchronized byte[] transmit(byte[] commandApdu) throws IOException {
        if (!connected || socket == null || socket.isClosed()) {
            throw new IOException("CardTransceiver is not connected to socket");
        }

        // Write length + APDU payload
        out.writeShort(commandApdu.length);
        out.write(commandApdu);
        out.flush();

        // Read response length + bytes
        int respLength = in.readUnsignedShort();
        byte[] response = new byte[respLength];
        in.readFully(response);
        return response;
    }

    @Override
    public synchronized void disconnect() {
        connected = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException ignored) {}
    }

    @Override
    public boolean isConnected() {
        return connected && socket != null && socket.isConnected() && !socket.isClosed();
    }

    @Override
    public String getTransceiverName() {
        return "Socket Simulator (" + host + ":" + port + ")";
    }
}
