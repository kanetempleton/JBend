/* TCP.java
    standard messaging protocol for TCP sockets
    default API shell if no others are chosen
 */


package com.server.protocol;

import com.server.entity.ServerConnection;
import com.console.*;

public class TCP extends Protocol {

    private int portNum;

    public TCP(int port) {
        super();
        portNum=port;
    }

    private static final String WELCOME_GREETING = "supnoob";

    /* processMessage(c,data):
        currently functions as a simple echo server
     */
    public void processMessage(ServerConnection c, String data) {
        Console.output("message received: "+data);
        if (!c.isWelcome()) {
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
        }
    }

    @Override
    public void processCustomMessage(ServerConnection c, String data) { }
    public int getPort() { return portNum; }
    public int getID() { return Protocol.TCP;}
    public String getName() { return "TCP";}

}