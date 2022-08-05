package com.server.entity;

import java.net.*;
import com.server.protocol.*;
import com.server.*;

import java.util.ArrayList;
import com.server.web.*;

public class ServerConnection {

    private Socket socket; //socket associated with connection for data sending
    private int connectionID; //set to -1 until server assigns an id
    private int port;
    private int state;
    private boolean needsReply; //true if waiting for a server response
    private Server serverOf; //reference to the server that this connection belongs to
    private ArrayList<Cookie> cookies; //stores cookies from HTTP responses
    private String name; //associate a username with the connection
    private boolean ping; //set to true when server initiates ping pong to check if connection is active
    private long ttr; //time to respond to ping before getting kicked
    private String pong; //required pong response
    private long nextPing; //next time to ping the connection
    private boolean isWelcome; //server handshakes


    public ServerConnection(Server of, Socket s) {
        socket=s;
        connectionID=-1;
        port = s.getPort();
        state=0;
        if (of.getAPI().getName().equals("WebSocket"))
            state=WebSocket.AWAITING_REGISTRATION;
        needsReply=false;
        serverOf=of;
        cookies = new ArrayList<>();
        name="";
        ping=false;
        ttr=0;
        pong="";
        nextPing=0;
        isWelcome=false;
    }


    public String toString() {
        String ip=(((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress()).toString().replace(":",".");
        long ttrdisp = 0;
        long nxtpingdisp = 0;
        long now = System.currentTimeMillis();
        if (ttr>0) {
            ttrdisp = ttr-now;
        }
        if (nextPing>0) {
            nxtpingdisp = nextPing-now;
        }
        if (serverOf.getAPI().getName().equals("WebSocket"))
            return "[Connection: id="+connectionID+" port="+port+"] state: "+getStateString()+", ip="+ip+"ping="+ping+" pong="+pong+" ttr="+(ttrdisp)+" nextPing="+nxtpingdisp+" ";
        return "[Connection: id="+connectionID+" port="+port+" ip="+ip
                +" ping="+ping+" pong="+pong+" ttr="+(ttrdisp)+" nextPing="+nxtpingdisp+" ";
    }

    public String toShortString() {
        String ip=(((InetSocketAddress) socket.getRemoteSocketAddress()).getAddress()).toString().replace(":",".");
        return "[Connection: "+connectionID+", "+ip+"]";
    }

    private String getStateString() {
        switch (state) {
            case WebSocket.AWAITING_REGISTRATION:
                return "Awaiting registration from server";
            case WebSocket.HANDSHAKE_INCOMPLETE:
                return "Awaiting WebSocket handshake completion";
            case WebSocket.ACTIVE:
                return "Ready for data transfer";
        }
        return "Invalid state";
    }

    public void setConnectionID(int id) {connectionID=id;}
    public void setState(int s) {state=s;}

    public void disconnect() {
        serverOf.dropConnection(this);
    }

    public void disconnect(Server S) {
        S.dropConnection(this);
    }

    public void sendMessage(String s) {
        serverOf.getAPI().sendMessage(this,s);
    }
    public void sendText(String s) {serverOf.getAPI().sendText(this,s);}

    public Socket getSocket() {return socket;}
    public int getConnectionID() {return connectionID;}
    public boolean isActive(){return (state==WebSocket.ACTIVE);}
    public int getPort(){return port;}
    public int getState(){return state;}
    public boolean needsReply() {return needsReply;}
    public void setNeedsReply(boolean b) {needsReply=b;}
    public Server getServer(){return serverOf;}

    public String getCookie(String x) {
        for (Cookie c: cookies) {
            if (c.field().equals(x))
                return c.value();
        }
        return "";
    }

    public ArrayList<Cookie> getCookies() {
        return cookies;
    }

    public void setCookies(ArrayList<Cookie> cookies) {
        this.cookies = cookies;
    }

    public String getName(){return name;}
    public void setName(String n){name=n;}

    public void setPing(boolean b) {ping=b;}
    public boolean getPing(){return ping;}
    public void setTTR(long r) {ttr=r;}
    public long getTTR(){return ttr;}
    public void setPong(String s) {pong=s;}
    public String getPong(){return pong;}
    public void setNextPing(long l) {nextPing=l;}
    public long getNextPing() {return nextPing;}
    public boolean isWelcome(){return isWelcome;}
    public void setWelcome(boolean b) {isWelcome=b;}

    public void pong(String dat) {
        serverOf.pong(this,dat);
    }
}