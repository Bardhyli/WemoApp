package org.myhome.wemo;

import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/*
 * http://www.makermusings.com/2015/07/13/amazon-echo-and-home-automation/
 */
public class WemoEmulator {

    private final static int SSDP_PORT = 1900;

    private final static String M_SEARCH_RESPONSE =
            "HTTP/1.1 200 OK\r\n" +
                    "CACHE-CONTROL: max-age=86400\r\n" +
                    "DATE: %s\r\n" +
                    "EXT:\r\n" +
                    "LOCATION: %s\r\n" +
                    "OPT: \"http://schemas.upnp.org/upnp/1/0/\"; ns=01\r\n" +
                    "01-NLS: %s\r\n" +
                    "SERVER: Unspecified, UPnP/1.0, Unspecified\r\n" +
                    "ST: urn:Belkin:device:**\r\n" +
                    "USN: uuid:Socket-1_0-%s::urn:Belkin:device:**\r\n\r\n";

    private List<VirtualSwitch> virtualSwitchList = new ArrayList<>();

    public void addVirtualSwitch(String name, int port, VirtualSwitchAction action) {
        VirtualSwitch newSwitch = new VirtualSwitch(name, port, action);
        virtualSwitchList.add(newSwitch);
    }

    private String getHostIPAddress() {
        String localhost = "127.0.0.1";
        Enumeration en = null;
        try {
            en = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            // Cannot retrieve network interface
            return localhost;
        }
        while (en.hasMoreElements()) {
            NetworkInterface ni = (NetworkInterface) en.nextElement();
            Enumeration ee = ni.getInetAddresses();
            while (ee.hasMoreElements()) {
                InetAddress ia = (InetAddress) ee.nextElement();
                if (ia.isLoopbackAddress()) {
                    continue;
                }
                if (ia.isSiteLocalAddress()) {
                    return ia.getHostAddress();
                }
            }
        }
        // Cannot determine host's IP address
        return localhost;
    }

    public void run() {
        try {
            byte[] rxbuf = new byte[1024];
            InetAddress multicastAddress = InetAddress.getByName("239.255.255.250");
            MulticastSocket socket = new MulticastSocket(SSDP_PORT);
            socket.setReuseAddress(true);
            socket.setSoTimeout(0);
            socket.joinGroup(multicastAddress);

            while (true) {
                DatagramPacket packet = new DatagramPacket(rxbuf, rxbuf.length);
                socket.receive(packet);
                String packetStr = new String(packet.getData());
                if (packetStr.contains("M-SEARCH") && packetStr.contains("urn:Belkin:device:**")) {
                    sendUPnPResponse(packet);
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                }
            }
        } catch (SocketTimeoutException ex) {
            // Shouldn't happen
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void sendUPnPResponse(DatagramPacket p) {
        DateFormat df = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        String today = df.format(new Date());
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket();
            for (VirtualSwitch s : virtualSwitchList) {
                String url = String.format("http://%s:%d/setup.xml", getHostIPAddress(), s.getServer().getPort());
                String respStr = String.format(M_SEARCH_RESPONSE, today, url, s.getUUID(), s.getSerial());
                byte[] resp = respStr.getBytes();
                DatagramPacket packet = new DatagramPacket(resp, resp.length, p.getAddress(), p.getPort());
                socket.send(packet);
            }
        } catch (IOException e) {
            // Nothing we can do
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }
}
