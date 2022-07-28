/*
    Moved the client to its own class to run in parallel with gui
    This class handles the client's connection to server

    Written by Kane Templeton Jan 13, 2019
 */

package com.server;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;


public class Client implements Runnable {

    private String ServerIP;
    private int ServerPort;


    private int serverID;

    public boolean connected = false;

    //public PacketHandler packetHandler;
    //public OutputHandler outputHandler;
    public SocketChannel socketChannel;
    private String responseData;

    public Client(String ip, int port) {
        ServerIP = ip;
        ServerPort = port;
        //packetHandler = new PacketHandler();
        serverID = -1;
        socketChannel = null;
        responseData=null;
    }


    /* connectToServer():
        establishes connection to the NIO Server
        also creates a PacketHandler to process incoming packets
    */



    public String getResponseData() {
        return responseData;
    }
    public void connectToServer(String mesg) {

        Socket serverSocket = null;
        DataOutputStream outStream = null;
        DataInputStream inStream = null;
        int myServerID = -2;
        boolean closeConnection = false;
        Selector selector = null;
        //SocketChannel socketChannel = null;
        ByteBuffer readBytes = ByteBuffer.allocate(mesg.getBytes().length*10);

        try {
            System.out.println("Attempting to connect to "+ServerIP+":"+ServerPort);
            socketChannel = SocketChannel.open();
            socketChannel.connect(new InetSocketAddress(ServerIP,ServerPort));
            socketChannel.configureBlocking(false);

            selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_READ);

            System.out.println("Successfully connected to "+ServerIP+":"+ServerPort);
            connected=true;
           // outputHandler = new OutputHandler(socketChannel);

            //sendMessage(Packet.CONNECT,"salt",socketChannel);

            //gameClient = new GameClient("Game Test",500,600);


            int ticks = 0;

           // BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            String build = "";
            while (!closeConnection) {

                //gameClient.loop();

                //text input from terminal
               /* char ch = 0;
                if (br.ready()) {
                    try {
                        ch = (char)br.read();
                        //System.out.println("char "+ch);
                        build+=ch;
                    } catch (IOException e) {
                        System.out.println("Error reading from Input device");
                    }
                }
                if (!br.ready() && build.length()>0) {
                    build = build.substring(0, build.length()-1); //remove \n at end
                    if (build.equalsIgnoreCase("disconnect")) {
                        disconnect(socketChannel);
                    }
                    else {
                        Packet p = new Packet(Packet.TEXT,build);
                        packetHandler.sendPacket(p,socketChannel);
                        //sendPacket(p,socketChannel);
                    }
                    //sendMessage(Packet.TEXT,build,socketChannel);
                    build="";
                }*/
                //end of text input from terminal

                sendMessage(mesg);



                //handle connections
                int numConnects = selector.select(1);
                if (numConnects != 0) {

                    Set keys = selector.selectedKeys(); //current sockets
                    Iterator it = keys.iterator();
                    while (it.hasNext()) {
                        SelectionKey key = (SelectionKey)it.next(); //key to deal with

                        if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) { //incoming data from server
                            SocketChannel sc = (SocketChannel)key.channel();
                            readBytes.clear();
                            sc.read(readBytes);
                            readBytes.flip();
                            String data = new String( readBytes.array(), Charset.forName("UTF-8") );
                            System.out.println("Client Received data: "+data.length());
                            responseData=data;
                            closeConnection=true;


                           /* String[] incoming = Packet.packets(data); //process incoming data stream as packets
                            for (int i=0; i<incoming.length; i++) {
                                Packet p = new Packet(incoming[i]);
                                packetHandler.handle(p,socketChannel);
                                //System.out.println("Message Received: "+p.toString());
                                if (p.getData().split(" ")[1].equalsIgnoreCase("REJECT")) {
                                    System.out.println("Server is full. Connection refused.");
                                    //closeConnection = true;
                                    continue;
                                }
                                if (p.getType().equals(Packet.CONNECT)) {
                                    serverID = Integer.parseInt(p.getData().split(" ")[2]);
                                    System.out.println("Successfully registered with server. Your ID is "+serverID);
                                }
                                if (p.getType().equalsIgnoreCase(Packet.DISCONNECT)) {
                                    System.out.println("Disconnecting...");
                                    socketChannel.close();
                                    closeConnection=true;
                                    break;
                                }
                            }*/
                        }
                        if (closeConnection)
                            break;
                    }
                    keys.clear();
                }
            }




        } catch (IOException ex) {
            System.out.println("oh no an io exception");
        }

        //close

        try {
            //outStream.close();
            // serverSocket.close();
            socketChannel.close();
        } catch(IOException ex) {
            System.out.println(ex);
        }
    }

    /** disconnect(s)
     * sends a disconnection request to the server.
     * this is the appropriate way to disconnect.
     * clients that do not disconnect with this method
     * will eventually be kicked by the server's check-alive polls
     *
     * @param s: SocketChannel to send packet thru
     */
    private void disconnect(SocketChannel s) {
      //  Packet P = new Packet(Packet.DISCONNECT,serverID+"");
      //  packetHandler.sendPacket(P,s);
    }

    /* getID():
        returns the ID that the client has been registered with at the server
    */

    public int getID() {
        return serverID;
    }

    /* run():
        called when the NIOClient thread is started in Main
        just connects to server and enters infinite loop
    */
    @Override
    public void run() {
        connectToServer("");
    }

    /** sendMessage(type,text,s)
     * sends a message to server
     *
     * @param type: packet type. use static constants in Packet class ex: Packet.TEXT
     * @param text: the text to attach to the packet
     * @param s: SocketChannel to send the packet thru

     */


    public void sendMessage(String msg) {
      // P.setData(Main.serverClient.getID()+" "+P.getData());
        //byte[] fin;
        byte[] m = msg.getBytes();
        int count=0;
        for (int i=0; i<m.length; i++) {
            if (m[i]!=0)
                count++;
        }
        byte[] fin = new byte[count];
        int k=0;
        for (int i=0; i<m.length; i++) {
            if (m[i]!=0)
                fin[k++]=m[i];
        }
     //   DataOutputStream outStream = new DataOutputStream(socketChannel.socket().getOutputStream());
    //    ByteBuffer bytebuf = ByteBuffer.wrap(mes.getBytes());
     //   s.getChannel().write(bytebuf);
        ByteBuffer bytebuf = ByteBuffer.wrap(fin);
        System.out.println("Sending to "+ServerIP+":"+ServerPort+": size="+fin.length+"\nbytes=[");
        for (int i=0; i<10; i++) {
            System.out.print(fin[i]+", ");
        }
        System.out.print("... ");
        for (int i=0; i<9; i++) {
            System.out.print(fin[fin.length-1-9+i]+", ");
        }
        System.out.println(fin[fin.length-1]+" ]");

        try {
            socketChannel.write(bytebuf);
        } catch (IOException ex) {
         //   Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }








}
