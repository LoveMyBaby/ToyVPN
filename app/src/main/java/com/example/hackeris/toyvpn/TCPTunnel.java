package com.example.hackeris.toyvpn;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by hackeris on 15/9/8.
 */
public class TCPTunnel {

    private String mAddress;
    private int mPort;

    private SocketChannel mChannel;

    private TCPTunnel(String address, int port) throws IOException {
        mAddress = address;
        mPort = port;
        mChannel = SocketChannel.open();
    }

    public static TCPTunnel connectTo(String address, int port) {

        try {
            return new TCPTunnel(address, port);
        } catch (IOException e) {
            return null;
        }
    }

    public final long write(ByteBuffer[] buffers) throws IOException {

        return mChannel.write(buffers);
    }

    public final long read(ByteBuffer[] buffers) throws IOException {

        return mChannel.read(buffers);
    }
}
