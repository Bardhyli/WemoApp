package org.myhome.wemo;

import org.apache.commons.io.IOUtils;
import org.simpleframework.http.Request;
import org.simpleframework.http.Response;
import org.simpleframework.http.core.Container;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.StringWriter;

public class HTTPRequestHandler implements Container {


    final private static String SETUP_XML = "<?xml version=\"1.0\"?>\n" +
            "<root>\n" +
            "  <device>\n" +
            "    <deviceType>urn:MyHome:device:controllee:1</deviceType>\n" +
            "    <friendlyName>%s</friendlyName>\n" +
            "    <manufacturer>Belkin International Inc.</manufacturer>\n" +
            "    <modelName>Virtual Switch</modelName>\n" +
            "    <modelNumber>1.0</modelNumber>\n" +
            "    <UDN>uuid:Socket-1_0-%s</UDN>\n" +
            "  </device>\n" +
            "</root>";

    final private static String GET_STATE_XML = "<?xml version=\"1.0\"?>\n" +
            "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" +
            " <s:Body>\n" +
            "   <u:GetBinaryStateResponse xmlns:u=\"urn:Belkin:service:basicevent:1\">\n" +
            "     <BinaryState>%d</BinaryState>\n" +
            "   </u:GetBinaryStateResponse>\n" +
            " </s:Body>\n" +
            "</s:Envelope>";

    final private static String SET_STATE_XML = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">\n" + "" +
            " <s:Body>\n" + "" +
            "   <u:SetBinaryState xmlns:u=\"urn:Belkin:service:basicevent:1\">\n" +
            "     <BinaryState>%d</BinaryState>\n" +
            "   </u:SetBinaryState>\n" +
            " </s:Body>\n" +
            "</s:Envelope>";

    private VirtualSwitch virtualSwitch;

    public HTTPRequestHandler(VirtualSwitch virtualSwitch) {
        this.virtualSwitch = virtualSwitch;
    }

    private void setResponse(Response response, String resp) {
        try {
            PrintStream body = response.getPrintStream();
            body.print(resp);
            body.close();
        } catch (IOException e) {
            response.setCode(404);
        }
    }

    public void handle(Request request, Response response) {
        long time = System.currentTimeMillis();
        response.setValue("Server", "MyHomeWemoVirtualSwitch/1.0");
        response.setDate("Date", time);
        response.setDate("Last-Modified", time);
        String path = request.getPath().getPath();
        response.setCode(200);
        response.setContentType("text/xml");
        if (path.startsWith("/setup.xml")) {
            String setupXML = String.format(SETUP_XML, virtualSwitch.getName(), virtualSwitch.getSerial());
            setResponse(response, setupXML);
            return;
        }
        String soapAction = request.getValue("SOAPACTION");
        VirtualSwitchAction action = virtualSwitch.getAction();
        if ("\"urn:Belkin:service:basicevent:1#GetBinaryState\"".equals(soapAction)) {
            String getStateXML = String.format(GET_STATE_XML, action.getState(virtualSwitch) ? 1 : 0);
            setResponse(response, getStateXML);
            return;
        }
        if ("\"urn:Belkin:service:basicevent:1#SetBinaryState\"".equals(soapAction)) {
            try {
                InputStream is = request.getInputStream();
                StringWriter writer = new StringWriter();
                IOUtils.copy(is, writer, "UTF-8");
                String content = writer.toString();
                if (content.contains("<BinaryState>1</BinaryState>")) {
                    action.changeStatus(virtualSwitch, true);
                }
                if (content.contains("<BinaryState>0</BinaryState>")) {
                    action.changeStatus(virtualSwitch, false);
                }
                String setStateXML = String.format(SET_STATE_XML, action.getState(virtualSwitch) ? 1 : 0);
                setResponse(response, setStateXML);
            } catch (IOException e) {
                response.setCode(404);
            }
        }
    }
}