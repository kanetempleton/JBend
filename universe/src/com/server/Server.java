package com.server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.*;
import com.server.entity.ServerConnection;
import com.console.Console;
import com.server.protocol.*;
import com.db.*;
import com.server.web.*;
import com.util.*;
import com.Main;


public class Server implements Runnable {

    private int port;
    private Protocol activeProtocol;

    private int numConnections,nextConnectionID; //connection management stuff
    private LinkedList<Integer> recycledIDs;

    private ServerSocketChannel socketChannel;
    private ByteBuffer buffer;
    private Selector selector;

    private ServerConnection connections[];

    //private Console console;
    private DatabaseManager databaseManager;

    public static DatabaseLock dbLock;

    public static boolean sendDatabase;
    public static String sendDBText;

    private WebPackets webPacketHandler;
    private ConnectionManager connectionManager;

    private long lastTickTime;

    private int messageBufferSize;

    public static final int MESSAGE_BUFFER_SIZE = 2147000000; //THIS IS EXTREMELY IMPORTANT!!!!!!


    public Server(Protocol protocol) {

        activeProtocol = protocol;
        port = protocol.getPort();
        socketChannel = null;
        messageBufferSize = MESSAGE_BUFFER_SIZE;
        buffer = ByteBuffer.allocate(messageBufferSize);
        selector = null;
        dbLock = new DatabaseLock();
        sendDatabase=false;
        sendDBText = "";
        webPacketHandler = new WebPackets();
        lastTickTime=0;
        connectionManager = new ConnectionManager(protocol.getName());

        /*databaseManager = new DatabaseManager();
        new Thread(databaseManager).start();*/


        numConnections = 0;
        nextConnectionID=0;
        recycledIDs = new LinkedList<>();
        connections = new ServerConnection[MAX_CONNECTIONS];
        for (int i=0; i<MAX_CONNECTIONS;i++)
            connections[i]=null;

        //console = null;

    }

    public Server(Protocol protocol, int bufSize) {
        activeProtocol = protocol;
        port = protocol.getPort();
        socketChannel = null;
        messageBufferSize = bufSize;
        buffer = ByteBuffer.allocate(messageBufferSize);
        selector = null;
        dbLock = new DatabaseLock();
        sendDatabase=false;
        sendDBText = "";
        webPacketHandler = new WebPackets();
        lastTickTime=0;
        connectionManager = new ConnectionManager(protocol.getName());

        /*databaseManager = new DatabaseManager();
        new Thread(databaseManager).start();*/


        numConnections = 0;
        nextConnectionID=0;
        recycledIDs = new LinkedList<>();
        connections = new ServerConnection[MAX_CONNECTIONS];
        for (int i=0; i<MAX_CONNECTIONS;i++)
            connections[i]=null;

    }




    /* run():
        called when this thread is started
    */
    public void run() {
       // initData();
        startServer();
    }

    /* startServer():
        disconnect the server from the internet and delete hard drive
     */
    public void startServer() {


        boolean killServer = false;
        int xx=0;

        try {
            if (activeProtocol instanceof HTTP) {
                ((HTTP)activeProtocol).loadRoutes();
            }
            Console.output(activeProtocol.getName()+"","Attempting to start server on port "+port+"...");

            socketChannel = ServerSocketChannel.open();
            socketChannel.socket().bind(new InetSocketAddress(port));
            socketChannel.configureBlocking(false);

            selector = Selector.open();
            socketChannel.register(selector, SelectionKey.OP_ACCEPT);


            Console.output(activeProtocol.getName()+"","Started successfully!");
            lastTickTime=System.currentTimeMillis()+TIME_BETWEEN_PING_PONG_CHECKS*2;
            Main.launcher.nextStage();

            int i=0;
            while (!killServer) {

                int numConnects = selector.select(MAX_SIM_CONNECTS); //accept MAX_SIM_CONNECTS incoming connections at a time

                if (numConnects != 0) {
                    Set keys = selector.selectedKeys(); //current sockets
                    Iterator it = keys.iterator();
                    while (it.hasNext()) {
                        SelectionKey key = (SelectionKey)it.next(); //key to deal with

                        if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) { //incoming connection

                            Socket s = socketChannel.socket().accept();
                            String ip = s.getInetAddress().toString();
                            if (ip.startsWith("/"))
                                ip = ip.substring(1);
                            if (Main.launcher.getLoginHandler()!=null) {
                                if (Main.launcher.getLoginHandler().ipBanned(ip)) {
                                    Console.output(activeProtocol.getName() + "", "ACCESS ATTEMPT FROM BANNED IP: " + ip);
                                    disconnect(s);
                                    continue;
                                }
                            }
                            int con = connectionManager.recordConnectionAttempt(ip);
                            if (con>1) {
                                Console.output(activeProtocol.getName()+"","repeat connection from "+ip+" #"+con);
                            }
                            if (con<0) {
                                disconnect(s);
                                continue;
                            }

                            SocketChannel sc = s.getChannel();
                            sc.configureBlocking(false);

                            // Register it with the selector for reading
                            sc.register(selector, SelectionKey.OP_READ);


                            if (!activeProtocol.getName().equals("HTTP"))
                                registerConnection(s); //register the connection with server

                        } else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) { //incoming data
                            SocketChannel sc = (SocketChannel) key.channel();

                            try {
                                sc = (SocketChannel) key.channel(); //clear the buffer
                                buffer.clear();
                                buffer.put(new byte[1024]);
                                buffer.clear();
                                sc.read(buffer);
                                buffer.flip();

                                //read data from buffer
                                if (buffer.limit() != 0) {
                                    if (activeProtocol.getName().equals("HTTP")) {
                                        Socket s = sc.socket();
                                        registerConnection(s);
                                    }
                                    String data = new String(buffer.array(), Charset.forName("UTF-8"));
                                    //if (!this.getAPI().getName().equals("WebSocket"))
                                     //   System.out.println("Received message from " + sc.socket().toString() /*data*/  + ">");
                                    ServerConnection cur = getConnection(sc.socket());
                                    processIncomingMessage(cur,data);
                                }
                                // remove dead connections from selector and close
                                if (buffer.limit() == 0) {
                                    key.cancel();
                                    Socket s = null;
                                    try {
                                        s = sc.socket();
                                        s.close();
                                    } catch (IOException ie) {
                                        System.err.println("Error closing socket " + s + ": " + ie);
                                    }
                                }

                            } catch (IOException ie) {
                                System.out.println(ie);
                                Socket sss = sc.socket();
                                try {
                                    Console.output(activeProtocol.getName()+"","Closing corrupted socket: "+sss);
                                    sss.close();
                                } catch (Exception eie) {
                                    Console.output(activeProtocol.getName()+"","Error closing socket " + sss + ": " + eie);
                                }
                                Console.output(activeProtocol.getName()+"","Corrupted socket channel " + sc);
                            }
                        }
                    }
                    keys.clear();
                }

                tick(); //extra server logic
            }
            //end of server loop; shut down stuff

            System.out.println("Shutting down server...");
            socketChannel.close();

        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("Server has encountered IOException.");
        }
    }

    //Communication methods

    /* processIncomingMessage(c,data):
        handle data when it is received from a client.
     */
    private void processIncomingMessage(ServerConnection c, String data) {
        if (activeProtocol != null)
            activeProtocol.processPacket(c, data);
    }



    //Various message to client methods


    /* messageToClient(s,<String> mes):
        send message to a client thru their socket connection in form of string
     */
    public void messageToClient(Socket s, String mes) {
        if (s==null)
            return;
        try {
            DataOutputStream outStream = new DataOutputStream(s.getChannel().socket().getOutputStream());
            ByteBuffer bytebuf = ByteBuffer.wrap(mes.getBytes());
            s.getChannel().write(bytebuf);
        } catch (IOException ex) {
            System.out.println("Error sending message: "+mes);
        }
    }
    /* messageToClient(s,<byte[]> mes):
        send message to a client thru their socket connection in form of byte array
     */
    public void messageToClient(Socket s, byte[] mes) {
        if (s==null)
            return;
        try {
            DataOutputStream outStream = new DataOutputStream(s.getChannel().socket().getOutputStream());
       //     Console.log("[DEBUG] messageToClient(s,mes) with mes size = "+mes.length);

            ByteBuffer bytebuf = ByteBuffer.wrap(mes);

            ByteBuffer b2 = ByteBuffer.allocateDirect(mes.length);
            b2.put(mes);
            //Console.log("[DEBUG] byte buffer is direct? "+b2.isDirect());
//            Console.log("[DEBUG] b1 array size = "+bytebuf.array().length+"; capacity = "+bytebuf.capacity()+"; limit = "+bytebuf.limit()+" readOnly = "+bytebuf.isReadOnly());

           // Console.log("[DEBUG] b2 array size = "+b2.array().length+"; capacity = "+b2.capacity()+"; limit = "+b2.limit()+" readOnly = "+b2.isReadOnly());

            s.getChannel().write(bytebuf);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    /* messageToClient(c,mes):
        send message to a client thru their connection object.
        only takes string arguments.
     */
    public void messageToClient(ServerConnection c, String msg) {
        messageToClient(c.getSocket(),msg);
    }



    // Connection methods

    /* registerConnection(s):
        create a connection object attached to socket connection s
        should probably change id system to something based on
        hashing client ports
     */

    public static final int DEBUG_LEVEL = 2;

    public void debug(int l, String m) {
        if (DEBUG_LEVEL>=l)
            Console.output(activeProtocol.getName()+"",m);
    }
    public static void debug_global(int l, String m) {
        if (DEBUG_LEVEL>=l)
            Console.output(m);
    }
    
    private void registerConnection(Socket s) {
        if (s==null || nextConnectionID<0) {
            System.out.println("register connect failure");
            System.out.println("nextCon,numCon,MAXCON:"+nextConnectionID+","+numConnections+","+MAX_CONNECTIONS);
            return;
        }
        debug(1,"Attempting to register connection for "+s.toString()+"...");
        ServerConnection c = new ServerConnection(this,s);
        numConnections++;
        //System.out.println("nextCon,numCon,MAXCON:"+nextConnectionID+","+numConnections+","+MAX_CONNECTIONS);
        if (nextConnectionID<MAX_CONNECTIONS) {
            connections[nextConnectionID] = c;
            c.setConnectionID(nextConnectionID++);
            if (this.getAPI().getName().equals("WebSocket"))
                c.setState(WebSocket.HANDSHAKE_INCOMPLETE);
            Console.output(activeProtocol.getName()+"","Registered new connectionID="+c.getConnectionID()+" to "+s.toString());
            c.setNextPing(System.currentTimeMillis()+DEFAULT_PING_GAP_TIME);
        }
        else if (recycledIDs.size() > 0) { //all ids will be recycled at this point
            System.out.println("Recycle!");
            int next = recycledIDs.removeFirst();
            if (next<0||next>=connections.length)
                return;
            while (connections[next]!=null) {
                if (recycledIDs.size()==0) {
                    System.out.println("The server is full. Connection rejected.");
                    dropConnection(c);
                    return;
                }
                else
                    next = recycledIDs.removeFirst();
            }
            if (next<0||next>=connections.length)
                return;
            connections[next]=c;
            c.setConnectionID(next);
            if (this.getAPI().getName().equals("WebSocket"))
                c.setState(WebSocket.HANDSHAKE_INCOMPLETE);
            System.out.println("Registered recycled connectionID="+c.getConnectionID()+" to "+s.toString());
            c.setNextPing(System.currentTimeMillis()+DEFAULT_PING_GAP_TIME);
        }
        else {
            System.out.println("The server is full. Connection rejected.");
            dropConnection(c);
            return;
        }
    }

    /* disconnect(s):
        disconnect a tcp socket connection.
        most basic layer of ending a connection.
     */
    private void disconnect(Socket s)  {
        if (s==null)
            return;
        try {
            if (getConnection(s)!=null) {
                int x = getConnection(s).getConnectionID();
                connections[x]=null;
                numConnections--;
               // nextConnectionID=x;
                recycledIDs.addLast(x);
            }
            if (!activeProtocol.getName().equals("HTTP"))
                Console.output(activeProtocol.getName()+"","Disconnecting "+s.toString());
            s.close();
        } catch (NullPointerException e) {
            System.out.println("Error disconnecting client "+s.toString());
        } catch (IOException e) {
            Console.output(activeProtocol.getName()+"","disconnect IOException: "+e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* dropConnection(c):
        unregister this connection and disconnect its socket
        also do user-related stuff when it exists
     */
    public void dropConnection(ServerConnection c) {
        int id = c.getConnectionID();
        disconnect(c.getSocket());
        if (id<0||id>=connections.length)
            return;
        if (connections[id]!=null) {
            connections[id] = null;
            numConnections--;
        }
        recycledIDs.addLast(id); //reuse their connection id so we dont run out of space
    }

    public ServerConnection getConnectionFromID(int id) {
        for (ServerConnection c: connections) {
            if (c==null)
                continue;
            if (c.getConnectionID() == id)
                return c;
        }
        return null;
    }

    public int kickConnection(int id) {
        ServerConnection c = getConnectionFromID(id);
        return kickConnection(c);
    }

    public int kickConnection(ServerConnection c) {
        if (c!=null) {
            Console.output(activeProtocol.getName()+"","attempting to kick connectionID="+c.getConnectionID()+" from "+this.getAPI().getName());
            dropConnection(c);
            return 1;
        } else {
            System.out.println("error kicking connectionID; not found in this server");
            return 0;
        }
    }

    /* getConnection(s):
        returns the connection object associated with a socket.
        based on port socket is connected to
     */
    private ServerConnection getConnection(Socket s) {
        for (ServerConnection c: connections) {
            if (c==null)
                continue;
            if (c.getPort() == s.getPort())
                return c;
        }

        return null;
    }

    private static final int DEFAULT_PING_TTR = 5000;
    private static final int DEFAULT_PING_GAP_TIME = 30000;

    private void ping(ServerConnection c) {
        sendMessage(c,"pingfromserver");
        c.setPing(true);
        c.setTTR(System.currentTimeMillis()+DEFAULT_PING_TTR);
        c.setPong("iamalive");
        c.setNextPing(0);
    }

    public void pong(ServerConnection c, String reply) {
        if (reply.equals(c.getPong())) {
            c.setPing(false);
            c.setTTR(0);
            c.setPong("");
            c.setNextPing(System.currentTimeMillis() + DEFAULT_PING_GAP_TIME);
        } else {
            Console.output(activeProtocol.getName()+"","incorrect pong response ("+reply+") kicking "+c);
            kickConnection(c);
        }
    }

    private void pingCheck(ServerConnection c) {
        if (c==null || c.getNextPing()==0 || c.getPing())
            return;
        if (System.currentTimeMillis() >= c.getNextPing()) {
            ping(c);
        }
    }

    public void pongCheck(ServerConnection c) {
        if (c==null || c.getTTR()==0 || !c.getPing())
            return;
        if (System.currentTimeMillis() >= c.getTTR()) {
            Console.output(activeProtocol.getName()+"","Connection did not pong in time, kicking "+c);
            kickConnection(c);
        }
    }


    private static final long TIME_BETWEEN_PING_PONG_CHECKS = 22000;

    private void tick() {
        long now = System.currentTimeMillis();
        if (now>=TIME_BETWEEN_PING_PONG_CHECKS+lastTickTime) {
            for (ServerConnection c: connections) {
                pingCheck(c);
                pongCheck(c);
            }
            lastTickTime=System.currentTimeMillis();
        }
    }

    public void pingpongCheck() {
        for (ServerConnection c: connections) {
            if (c!=null)
                ping(c);
           // pingCheck(c);
           // pongCheck(c);
        }
    }

    public void dropAllConnections() {
        for (ServerConnection c: connections)
            kickConnection(c);
    }

    public void printConnectionsInfo(String[] rBuf) {
        int count[]=new int[300];
        int manCount=0;
        int nullCount=0;

        rBuf[0]+="<br>"+this.getAPI().getName()+" connections:<br>";
        for (ServerConnection c: connections) {
            if (c==null) {
                nullCount++;
                continue;
            }
            rBuf[0]+=c.toString()+"<br>";
            System.out.println(c.toString());
            count[c.getState()]++;
            manCount++;
        }
        rBuf[0]+="total connections: "+manCount+"<br><br>";
        System.out.println("Connection Statistics:");
        System.out.println("\tConnections awaiting registration: "+count[100]);
        System.out.println("\tConnections awaiting handshake: "+count[101]);
        System.out.println("\tConnections active: "+count[102]);
        System.out.println("\tTotal connections (counted manually): "+manCount);
        System.out.println("\tTotal null connections: "+nullCount);
        System.out.println("\tTotal connections (stored in server): "+numConnections);

    }

    public int getPort() {
        return port;
    }

    public void sendMessage(ServerConnection c, String s) {
        activeProtocol.sendMessage(c,s);
    }

    public ByteBuffer getBuffer() {return buffer; }
    public DatabaseManager getDatabaseManager() {return databaseManager; }
    public Protocol getAPI() {return activeProtocol;}

    public WebPackets getWebPacketHandler() {return webPacketHandler;}

    public void setWebPacketHandler(WebPackets wp) {
        webPacketHandler = wp;
    }

    public static final int HTTP_PORT = 80;
    public static final int RESTFUL_PORT = 8080;
    public static final int MAX_SIM_CONNECTS = 10; //max number of connections to listen for at once
    public static final int MAX_CONNECTIONS = 100; //max number of connected clients at one time

}