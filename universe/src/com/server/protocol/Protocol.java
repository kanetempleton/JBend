package com.server.protocol;

import com.server.entity.ServerConnection;

public abstract class Protocol {

    // API-specific methods:


    /* processMessage(c,data):
        HANDLES LOGIC:
        performs processing logic on the decoded plaintext from an incoming message.
        most application functionality to be performed on top of the protocol in use
        should be written inside of this method.
        must be overridden in API subclass to provide implementation.
     */
    public abstract void processMessage(ServerConnection c, String data);

    /* decodeMessage(c,data):
        returns packet data converted into plaintext string, or null if data is in improper format.
        each protocol may have its own way of encoding messages.
        this method is defined per API to decode packets according to protocol.
        if no decoding is necessary, do not override this method in API subclass
     */
    protected String decodeMessage(ServerConnection c, String data) {return data;}

    /* encodeMessage(c,data):
        encodes data into proper format to be sent to clients by the current protocol.
        if no encoding is necessary, do not override this method in API subclass
     */
    public byte[] encodeMessage(ServerConnection c, String data) {return data.getBytes();}



    //API properties
    public abstract int getPort();
    public abstract int getID();
    public abstract String getName();


    //NON-ABSTRACT METHODS: generic to all APIs. server integration

    /* processPacket(c,data):
        method called by server on any API when receiving data thru TCP socket.
        attempts to retrieve plaintext message in case packet is encoded,
        then processes the plaintext message according to specific API
     */
    public void processPacket(ServerConnection c, String data) {
        try {
            String send = decodeMessage(c, data);
            processMessage(c, send);
            processCustomMessage(c,send);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract void processCustomMessage(ServerConnection c, String data);

    /* sendMessage(c,data,encode):
        method called by server on any API to send data thru TCP socket.
        if encode flag is set to true,
            attempts to encode message according to specific API,
            then sends encoded message across TCP connection.
        else sends raw data.
        if encode flag is ommitted, it will set to true by default
     */
    public void sendMessage(ServerConnection c, String data, boolean encode) {
        try {
            if (encode) {
                byte[] send = encodeMessage(c, data);
                c.getServer().messageToClient(c.getSocket(), send);
                if (this.getName().equals("HTTP"))
                    c.disconnect();
            } else {
                c.getServer().messageToClient(c, data);
                if (this.getName().equals("HTTP"))
                    c.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendMessage(ServerConnection c, String data) {
        sendMessage(c,data,true);
    }

    public void sendText(ServerConnection c, String data) { //override for sending plain text
        sendMessage(c,data,false);
    }


    public void sendBytes(ServerConnection c, byte[] bytes) {
        try {
            c.getServer().messageToClient(c.getSocket(), bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final int TCP = 0;
    public static final int WEBSOCKET = 1;
    public static final int HTTP = 3;


}