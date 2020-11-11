/* WebSocket.java
    api for WebSocket protocol
 */

package com.server.protocol;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.server.entity.ServerConnection;
import com.Main;
import com.console.*;

public class WebSocket extends Protocol {

    private int portno;

    public WebSocket() {
        super();
        this.portno=8080;
    }
    public WebSocket(int port) {
        super();
        this.portno=port;
    }


    /* processMessage(c,data):
        process packet data after it has been converted to plaintext.
        completes handshake for new connections,
        otherwise replies to any message from an active connection with "testing"
     */
    @Override
    public void processMessage(ServerConnection c, String data) {
        if (data==null || c==null)
            return;
        if (!c.isActive()) { //handshake needed
            if (c.getState()==HANDSHAKE_INCOMPLETE) {
                String R = new String(webSocketHandshakeResponse(data));
                if (!R.equals(BAD_REQUEST)) {
                    sendMessage(c,R,false);
                    //Main.launcher.getServer().messageToClient(c, R);
                    c.setState(ACTIVE);
                }
            } else {
                c.getServer().dropConnection(c);
            }
        } else { //receiving messages from client after handshake
            Console.output("Received web message from "+c+":\n\t <"+data+">");
            String dhex = Main.launcher.cryptography().bytesToHex(data.getBytes());
            Console.output("as bytes = "+dhex);
            if (dhex.equals("03 EF BF BD")) {
                System.out.println("websocket client force closed, disconnecting...");
                c.disconnect();
            }
            if (data.startsWith("pong::")) {
                if (data.split("::").length<2) {
                    c.pong("kms");
                } else {
                    c.pong(data.split("::")[1]);
                }
            }
            if (data.startsWith("game::")) {
                int plen = Integer.parseInt(data.split("::")[1]);
               // Main.launcher.game().getPacketHandler().handleNetworkPacket(c,plen,data.split("::")[2]);
            }
            //sendMessage(c, "testing ");

        }
    }


    /* decodeMessage(c,data):
        data is in form of masked websocket frame.
        returns decoded frame text.
    */
    @Override
    protected String decodeMessage(ServerConnection c, String data) {
        if (!c.isActive()) { //handshake needed
            return data;
        } else { //receiving messages from client after handshake
            byte[] msgBytes = new byte[c.getServer().getBuffer().remaining()];
            c.getServer().getBuffer().get(msgBytes); //copy bytes from buffer to byte array
            String recvd = new String(decodeFrame(msgBytes));
            return recvd;
        }
    }

    @Override
    public void processCustomMessage(ServerConnection c, String data) {

    }


    /* encodeMessage(c,data):
        returns handshake response if connection is not registered,
        otherwise returns unmasked websocket text frame containing data
     */
    @Override
    public byte[] encodeMessage(ServerConnection c, String data) {
        if (!c.isActive() && c.getState()==HANDSHAKE_INCOMPLETE)
            return webSocketHandshakeResponse(data);
        return encodeFrame(data.getBytes());
    }



    //PROTOCOL-SPECIFIC PRIVATE METHODS

    /*  webSocketHandShakeResponse(msg):
        returns the handshake response if msg is in format of handshake request
    */
    private byte[] webSocketHandshakeResponse(String msg) {
        Matcher get = Pattern.compile("^GET").matcher(msg);
        try {
            if (get.find()) {
                Matcher match = Pattern.compile("Sec-WebSocket-Key: (.*)").matcher(msg);
                match.find();
                byte[] response = ("HTTP/1.1 101 Switching Protocols\r\n"
                        + "Connection: Upgrade\r\n"
                        + "Upgrade: websocket\r\n"
                        + "Sec-WebSocket-Accept: "
                        + Base64.getEncoder().encodeToString(MessageDigest.getInstance("SHA-1").digest((match.group(1) + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").getBytes("UTF-8")))
                        + "\r\n\r\n").getBytes("UTF-8");
                return (new String(response)).getBytes();
            }
        } catch (UnsupportedEncodingException e) {
            Console.output("unsupported encoding");
        } catch (NoSuchAlgorithmException e) {
            Console.output("no algorithm");
        } catch (IllegalStateException e) {
            Console.output("illegal state or bad regex match");
        }

        return (new String(BAD_REQUEST)).getBytes();
    }



    /* decodeFrame(msg):
        returns the proper ascii bytes of a decoded message
        from a websocket client.
        found this algorithm at:
        https://stackoverflow.com/questions/18368130/how-to-parse-and-validate-a-websocket-frame-in-java/18368334
     */
    private byte[] decodeFrame(byte[] msg) {

        System.out.println("decoding websocket message: "+Main.launcher.cryptography().bytesToHex(msg));

        Console.log("byte 0 of ws message: "+(int)msg[0]);

        int maskIndex = 2;
        byte[] maskBytes = new byte[4];
        String er = "decode-error";
        if (msg.length==0)
            return decodeFrame(er.getBytes());

        if ((msg[1] & (byte) 127) == 126) {
            maskIndex = 4;
        } else if ((msg[1] & (byte) 127) == 127) {
            maskIndex = 10;
        }

        System.arraycopy(msg, maskIndex, maskBytes, 0, 4);

        byte[] message = new byte[msg.length - maskIndex - 4];

        for (int i = maskIndex + 4; i < msg.length; i++) {
            message[i - maskIndex - 4] = (byte) (msg[i] ^ maskBytes[(i - maskIndex - 4) % 4]);
        }

        return message;
    }

    /* encodeFrame(msg):
        encodes msg into websocket bytes to send over TCP
        created from referencing RFC 6455
        https://tools.ietf.org/html/rfc6455
     */
    public byte[] encodeFrame(byte[] msg) {

        byte textFrame = (byte)0b10000001; //first 8 bits of frame
        byte maskedPayloadLen = (byte)0;
        byte unmaskedPayloadLen = (byte)0;
        byte extPayloadLen[] = new byte[8];
        byte maskingKey[] = new byte[4];
        if (msg.length<126) { //msg lengths that can be expressed in 7 bits; 0 to 125 chars in length
            //should be set to 126 if using 2 bytes as length
            //should be set to 127 if using 8 bytes as length with MSB as 0
            maskedPayloadLen = (byte) (0b10000000 + msg.length);
            unmaskedPayloadLen = (byte) msg.length;
            //basic unmaksed sending of a short message
            byte unmaskedShortFrame[] = new byte[2+msg.length];
            unmaskedShortFrame[0]=textFrame;
            unmaskedShortFrame[1]=unmaskedPayloadLen;
            for (int i=0; i<msg.length; i++)
                unmaskedShortFrame[i+2]=msg[i];

            return unmaskedShortFrame;
        }
        else if (msg.length<65535) { //16 bit msg lengths; >125 and <=65536 chars in length
            maskedPayloadLen = (byte)0b11111110;
            extPayloadLen[0] = (byte)((msg.length)>>>8); //right shift to get first half as one byte
            extPayloadLen[1] = (byte)(((msg.length)^(0b1111111100000000))&(msg.length)); //get second half as one byte
            /* ACTUAL WORK HERE:
            1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 1 1 0 3 4 0 0 0 0 0 0 0 0 0 0 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 1 1 2 2 2 0 0 2 2 2 2 2 2 2 2 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 1 1 0 3 4 0 0 0 0 0 0 0 0 0 0 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 1 1 2 2 2 0 0 2 2 2 2 2 2 2 2 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 1 1 0 3 4 0 0 0 0 0 0 0 0 0 0 1 1 0 0 0 0 0 0 0 0 0 0 0 0 0 1 1 2 2 2 0 0 2 2 2 2 2 2 2 2 1
                payload length = 7+16 bits
                The length of the "Payload data", in bytes: if 0-125, that is the
      payload length.  If 126, the following 2 bytes interpreted as a
      16-bit unsigned integer are the payload length.  If 127, the
      following 8 bytes interpreted as a 64-bit unsigned integer (the
      most significant bit MUST be 0) are the payload length.  Multibyte
      length quantities are expressed in network byte order.  Note that
      in all cases, the minimal number of bytes MUST be used to encode
      the length, for example, the length of a 124-byte-long string
      can't be encoded as the sequence 126, 0, 124.  The payload length
      is the length of the "Extension data" + the length of the
      "Application data".  The length of the "Extension data" may be
      zero, in which case the payload length is the length of the
      "Application data".

             */
            unmaskedPayloadLen = 126;
            String z = String.format("%16s", Integer.toBinaryString(msg.length)).replace(" ", "0");
            String b = "";
            String c = "";
            for (int i=0; i<z.length(); i++) {
                if (i<8) {
                    b+=z.charAt(i);
                } else {
                    c+=z.charAt(i);
                }
            }
            byte len1 =  (byte)Integer.parseInt(b, 2);
            byte len2 =  (byte)Integer.parseInt(c, 2);
            //basic unmaksed sending of a medium length message
            byte unmaskedMedFrame[] = new byte[4+msg.length];
            unmaskedMedFrame[0]=textFrame;
            unmaskedMedFrame[1]=unmaskedPayloadLen;
            unmaskedMedFrame[2]=len1;
            unmaskedMedFrame[3]=len2;
            for (int i=0; i<msg.length; i++)
                unmaskedMedFrame[i+4]=msg[i];

            return unmaskedMedFrame;
        }
        else { // message lengths up to a 64 bit integer. currently no actual support for this
            for (int i=0; i<8; i++)
                extPayloadLen[i] = (byte)0;
        }

        //basic unmaksed sending of a short message
        byte unmaskedShortFrame[] = new byte[2+msg.length];
        unmaskedShortFrame[0]=textFrame;
        unmaskedShortFrame[1]=unmaskedPayloadLen;
        for (int i=0; i<msg.length; i++)
            unmaskedShortFrame[i+2]=msg[i];

        return unmaskedShortFrame;
    }


    public static final String BAD_REQUEST = "HTTP/1.1 400 BAD REQUEST\r\nContent-Type: text/html\r\n\r\n";
    public static final int AWAITING_REGISTRATION = 100;
    public static final int HANDSHAKE_INCOMPLETE = 101;
    public static final int ACTIVE = 102;

    public int getPort() {
        return this.portno;
    }
    public int getID() { return Protocol.WEBSOCKET;}
    public String getName() {return "WebSocket";}

}

