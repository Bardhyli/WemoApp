package org.myhome.wemo;

import org.apache.commons.codec.digest.DigestUtils;

import java.util.UUID;

public class VirtualSwitch {
    private String name;
    private String uuid;
    private String serial;
    private HTTPServer server;
    private VirtualSwitchAction action;

    public VirtualSwitch(String switchName, int port, VirtualSwitchAction action) {
        this.action = action;
        name = switchName;
        uuid = UUID.randomUUID().toString();
        serial = DigestUtils.md5Hex(name).toUpperCase();
        server = new HTTPServer(port, this);
    }

    public VirtualSwitchAction getAction() {
        return action;
    }

    public String getName() {
        return name;
    }

    public String getUUID() {
        return uuid;
    }

    public String getSerial() {
        return serial;
    }

    public HTTPServer getServer() {
        return server;
    }
}
