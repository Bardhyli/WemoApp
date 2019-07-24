package org.myhome.wemo;

import org.simpleframework.http.core.ContainerSocketProcessor;
import org.simpleframework.transport.connect.SocketConnection;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HTTPServer {

    private HTTPRequestHandler container;
    private int port;


    public HTTPServer(int port, VirtualSwitch virtualSwitch) {
        try {
            this.port = port;
            container = new HTTPRequestHandler(virtualSwitch);
            ContainerSocketProcessor processor = new ContainerSocketProcessor(container, 1);
            SocketConnection connection = new SocketConnection(processor);
            InetSocketAddress address = new InetSocketAddress(port);
            connection.connect(address);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public int getPort() {
        return port;
    }
}
