package com.server.protocol;

import com.server.entity.ServerConnection;
import com.console.*;
import java.util.*;
import com.server.*;
import com.Launcher;

public class TCPProxy extends Protocol {

    private int portNum;
    private HashMap<String,Integer> portMap;
    private HashMap<String,Server> serverMap;
    private Client proxyClient;

    public TCPProxy(int port) {
        super();
        portNum=port;
        portMap=new HashMap();
        serverMap = new HashMap();
        proxyClient = null;
    }

    private static final String WELCOME_GREETING = "supnoob";

    public void mapPort(String host, int port) {
        portMap.put(host,port);
    }

    public int forwardPort(String host) {
        if (portMap.containsKey(host))
            return portMap.get(host);
        return 80;
    }

    public void mapServer(String host, Server s) {

    }

    public Server forwardServer(String host) {
        return serverMap.get(host);
    }

    /* processMessage(c,data):
        currently functions as a simple echo server
     */
    public void processMessage(ServerConnection c, String data) {
         Console.output("message received from "+c.toShortString()+" ; length = "+data.length());
         if (Launcher.DEBUG_SERVER_LEVEL >= 1) {
             Console.output("data: "+data);
         }

         String[] lines = data.split("\\r\\n");
         String uri = "/";
         String method = "GET";
         if (lines.length>0 && lines[0].split(" ").length>=2) {
             if (lines[0].split(" ")[0].equals("GET")) {
                 uri = lines[0].split(" ")[1];
           //      Console.output("uri identified: "+uri);
             } else {
                 method = lines[0].split(" ")[0];
                 uri = lines[0].split(" ")[1];
             }
         }
         String host = "";
         for (String l:lines) {
             if (l.contains("Host: "))
                 host = l.replace(" ","").split(":")[1];
         }
       //  Console.output("proxying http host: "+host+" : "+method+" "+uri);
         int pport = forwardPort(host);
         if (pport!=this.portNum) {
             Console.output("found mapping: "+host+": "+method+" "+uri+" ---> "+pport);
         }
         proxyClient = new Client(host,pport);
         proxyClient.connectToServer(data);
         //Console.output("escape line");
         String resp = "";
         String dat = proxyClient.getResponseData();
         if (uri.contains(".js")) {
             resp = filterJSResponse(dat);
         }
         else {
             resp = dat.replace("\\0","");
         }
         int kount = 0;
         for (int i=0; i<resp.getBytes().length; i++) {
             if (resp.getBytes()[i] == 0) {
                 //System.out.println("zero byte found");
                 break;
             } else {
                 kount++;
             }
         }
         byte[] outbyte = new byte[kount];
         kount=0;
         for (int i=0; i<resp.getBytes().length; i++) {
             if (resp.getBytes()[i] != 0)
                 outbyte[kount++]=resp.getBytes()[i];
         }
         String outstring = new String(outbyte);
         if (resp!=null) {
            // Console.output("got response of length " + resp.length());
            // Console.output("forwarding response of length " + outstring.length());
            // System.out.print("bytes = [");
            // for (int i=0; i<10; i++) {
            //     System.out.print(resp.getBytes()[outstring.getBytes().length-1-10+i]+", ");
            // }
             //System.out.println("]");
             c.sendMessage(outstring);
             c.disconnect();
         }
         else {
             Console.output("null response");
         }

//         Main.launcher.getServer(pport).

        /*if (!c.isWelcome()) {
            if (data.equals(WELCOME_GREETING)) {
                c.setWelcome(true);
            } else {
                Console.output("connection not following welcoming protocol... goodbye :) "+c);
                c.sendMessage("You are not welcome.");
                c.disconnect();
            }
        }
        // System.out.println("Echoing data back to client...");
        // sendMessage(c,data);
        if (data.startsWith("pong::")) {
            if (data.split("::").length<2) {
                c.pong("kms");
            } else {
                c.pong(data.split("::")[1]);
            }
        }*/
    }

    private String filterJSResponse(String resp) {
        String[] content = resp.split("\r\n");
        Console.output("filtering JS content, crlf splits: "+content.length);
        String out = "";
        for (String blox : content) {
            Console.output("testing block length "+blox.length()+":\n"+blox);
            if (blox.contains("HTTP") && blox.contains("200") && blox.contains("OK"))
                continue;
            if (blox.contains("Content-Type:") && blox.contains("javascript"))
                continue;
            if (blox.length()<1)
                continue;
            out += blox;
        }
        out = out.replace("\r\n"," ").replace("\r"," ");
        Console.output("final filtered content:\n"+out);
        return out;
    }

    @Override
    public void processCustomMessage(ServerConnection c, String data) { }
    public int getPort() { return portNum; }
    public int getID() { return Protocol.TCP;}
    public String getName() { return "TCP";}

}