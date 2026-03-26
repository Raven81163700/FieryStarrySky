package com.aurora.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;

public final class TcpClient {

    private final String host;
    private final int port;

    public TcpClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String sendAndReadLine(String payload) throws IOException {
        SocketConnection socket = null;
        InputStream in = null;
        OutputStream out = null;
        InputStreamReader reader = null;
        OutputStreamWriter writer = null;

        try {
            socket = (SocketConnection) Connector.open("socket://" + host + ":" + port, Connector.READ_WRITE, true);
            in = socket.openInputStream();
            out = socket.openOutputStream();
            reader = new InputStreamReader(in, "UTF-8");
            writer = new OutputStreamWriter(out, "UTF-8");

            writer.write(payload);
            writer.flush();

            StringBuffer sb = new StringBuffer();
            int c;
            while ((c = reader.read()) != -1) {
                if (c == GameProtocol.MSG_SEPARATOR) {
                    break;
                }
                sb.append((char) c);
            }
            return sb.toString();
        } finally {
            closeQuietly(writer);
            closeQuietly(reader);
            closeQuietly(out);
            closeQuietly(in);
            closeQuietly(socket);
        }
    }

    private void closeQuietly(OutputStreamWriter writer) {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException e) {
            }
        }
    }

    private void closeQuietly(InputStreamReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException e) {
            }
        }
    }

    private void closeQuietly(OutputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
            }
        }
    }

    private void closeQuietly(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
            }
        }
    }

    private void closeQuietly(SocketConnection socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }
}
