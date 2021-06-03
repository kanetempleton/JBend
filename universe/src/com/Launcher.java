package com;

import com.server.Server;
import com.server.protocol.*;
import com.db.crud.lang.*;
//import com.game.*;

import java.util.Scanner;

import com.db.*;
import com.console.*;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import com.server.login.*;
import com.util.crypt.*;
import com.util.*;

import javax.xml.crypto.Data;

public class Launcher {


    private Server server,websocketserver,webserver;
    private DatabaseManager databaseManager;
    private Console console;
    private LoginHandler loginHandler;
    private CryptoHandler cryptoHandler;
    private CareTaker caretaker;
    private String cfg[][];
    public static int stage;
    public static DatabaseLock databaseLock;

    private Runnable threads[];
    private int numThreads;

    public Launcher() {
        stage=0;
        cfg=new String[10][2];
        for (int i=0; i<cfg.length; i++) {
            for (int j=0; j<cfg[i].length; j++) {
                cfg[i][j]="DNE";
            }
        }
        numThreads=0;
        threads=new Runnable[10];
        databaseLock = new DatabaseLock();
        cryptoHandler = new CryptoHandler();
        loadConfig();
        addConsole();
    }


    public Launcher(int maxThreads) {
        stage=0;
        cfg=new String[10][2];
        for (int i=0; i<cfg.length; i++) {
            for (int j=0; j<cfg[i].length; j++) {
                cfg[i][j]="DNE";
            }
        }
        numThreads=0;
        threads=new Runnable[maxThreads];
        databaseLock = new DatabaseLock();
        cryptoHandler = new CryptoHandler();
        loadConfig();
        addConsole();
    }

    public Launcher(String settings) {
        stage=0;
        cfg=new String[10][2];
        for (int i=0; i<cfg.length; i++) {
            for (int j=0; j<cfg[i].length; j++) {
                cfg[i][j]="DNE";
            }
        }
        numThreads=0;
        threads=new Runnable[10];
        databaseLock = new DatabaseLock();
        cryptoHandler = new CryptoHandler();
        if (!(settings.equalsIgnoreCase("local") || settings.equalsIgnoreCase("development")))
            loadConfig();
        addConsole();
    }


    public void loadConfig() {
        System.out.println("loading configuration...");
        String conf = FileManager.fileDataAsString("env.cfg").replace("\n","");
        int i=0;
        if (!conf.equalsIgnoreCase("DNE")) {
            String pairs[] = conf.split("=;");
            for (String p: pairs) {
                String parts[] = p.split(":=");
                if (parts.length==2) {
                    String field = parts[0];
                    String value = parts[1];
                    if (field.startsWith("#")) {
                        field = "DNE";
                        value = "DNE";
                    }
                    if (i<cfg.length) {
                        cfg[i][0] = field;
                        cfg[i][1] = value;
                        i++;
                    }
                } else {
                    System.out.println("misformatted line: "+p);
                }
            }
        } else {
            System.out.println("can't find config file:"+conf);
        }
        System.out.println("config loaded.");
    }

    public String getConfig(String s) {
        if (cfg==null)
            return "DNE";
        for (String[] c: cfg) {
            if (c==null || c[0]==null || c[1]==null)
                return "DNE";
            if (c[0].equals(s))
                return c[1];
        }
        return "DNE";
    }

    public Server getHTTPServer(int port) {
        for (int i=0; i<numThreads; i++) {
            Runnable r = threads[i];
            if (threads[i]==null)
                continue;
            if (r instanceof Server) {
                if (((Server)r).getAPI().getName().equals("HTTP")){
                    if (((Server) r).getPort() == port)
                        return (Server) r;
                }
            }
        }
        return null;
    }

    public void loadThread(Runnable r) {
        if (numThreads>=threads.length) {
            System.out.println("Launcher cannot hold any more threads.");
            return;
        }
        threads[numThreads]=r;
        if (r instanceof Console)
            console = (Console)threads[numThreads];
        if (r instanceof DatabaseManager)
            databaseManager = (DatabaseManager) threads[numThreads];
        if (r instanceof LoginHandler)
            loginHandler = (LoginHandler) threads[numThreads];
        if (r instanceof CryptoHandler)
            cryptoHandler = (CryptoHandler) threads[numThreads];
        if (r instanceof CareTaker)
            caretaker = (CareTaker) threads[numThreads];
        if (r instanceof Server) {
            Server s = (Server) r;
            if (s.getAPI().getName().equals("TCP"))
                server = (Server) threads[numThreads];
            if (s.getAPI().getName().equals("HTTP"))
                webserver = (Server) threads[numThreads];
            if (s.getAPI().getName().equals("WebSocket"))
                websocketserver = (Server) threads[numThreads];
        }
        numThreads++;
    }

    public void startThreads() {
        if (stage<numThreads) {
            new Thread(threads[stage]).start();
        }
    }

    public void addStandardThreads() {
        //addConsole();
        addDatabaseManager();
        addLoginHandler();
        addCareTaker(1800000);
        addTCPServer(43594);
        addHTTPServer(8069);
        addWebSocketServer(42069);
        //addCareTaker(1800000);
    }


    public void addConsole() {
        if (console==null) {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            loadThread(new Console(br));
        }
    }
    public void addDatabaseManager() {
        DatabaseManager m = new DatabaseManager();

        String dbURL = getConfig("db_addr").equalsIgnoreCase("DNE") ? "localhost" : getConfig("db_addr");
        String dbName = getConfig("db_name").equalsIgnoreCase("DNE") ? "jbend" : getConfig("db_name");
        String dbUser = getConfig("db_user").equalsIgnoreCase("DNE") ? "root" : getConfig("db_user");
        String dbPass = getConfig("db_pass").equalsIgnoreCase("DNE") ? "admin" : getConfig("db_pass");
        m.setDB_url(dbURL);
        m.setDB_name(dbName);
        m.setDB_user(dbUser);
        m.setDB_pass(dbPass);
        loadThread(m);
    }
    public void addLoginHandler() {
        loadThread(new LoginHandler());
    }
    public void addCareTaker(long delay) {
        loadThread(new CareTaker(delay));
    }
    public void addTCPServer(int port) {
        TCP tcp = new TCP(port);
        loadThread(new Server(tcp));
    }
    public void addHTTPServer(int port) {
        HTTP http = new HTTP(HTTP.DEFAULT_HOME_DIRECTORY,port);
        loadThread(new Server(http));
    }
    public void addHTTPServer(String homeDir, int port) {
        HTTP http = new HTTP(homeDir,port);
        loadThread(new Server(http));
    }
    public void addWebSocketServer(int port) {
        WebSocket ws = new WebSocket(port);
        loadThread(new Server(ws));
    }


    public void launch() {
        Scanner s = new Scanner(System.in);
        Protocol httpProtocol, wssProtocol, tcpProtocol;
        httpProtocol = new HTTP(HTTP.DEFAULT_HOME_DIRECTORY,8069);
        wssProtocol = new WebSocket(42069);
        tcpProtocol = new TCP(43594);

        databaseLock = new DatabaseLock();
        cryptoHandler = new CryptoHandler();
       /* String z = String.format("%16s", Integer.toBinaryString(458)).replace(" ", "0");
        System.out.println("ws message ;; "+z);
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
        System.out.println("len1="+len1+";len2="+cryptoHandler.bytesToHex(len3));
        if (1+1==2)
            return;*/



    /*    byte[] M = (new String("hillary clinton riggedthe2016elections")).getBytes();
        byte[] k = (new String("jjf8943hr203hfao")).getBytes();//new byte[16]; //key size = size of one msg block (16 bytes)
        byte[] IV = (new String("1234567890abcdef")).getBytes();//new byte[16];

        byte[] C = cryptoHandler.cbcEncrypt(M,k,IV);
        byte[] D = cryptoHandler.cbcDecrypt(C,k,IV);
        System.out.println("newc:"+new String(C));
        System.out.println("newd:<"+new String(D)+">");

        if (1+1==2)
            return;*/

        //byte[] D = cryptoHandler.elGamalEncrypt(3,(new String("whats up noob")).getBytes());
        //byte[] E = cryptoHandler.elGamalDecrypt(D);
        //try with p=199, q=193
/*        int p = 199;
        int q = 193;
        byte[] F = cryptoHandler.rsaEncrypt(cryptoHandler.find_e_value(p,q),p*q,(new String("hello sir")).getBytes());
        System.out.println("F="+(new String(F)));
        byte[] G = cryptoHandler.rsaDecrypt(p,q,F);
        System.out.println("G="+(new String(G)));
*/

      //  if (1+1==2)
        //    return;

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        console = new Console(br);
        //new Thread(console).start();

        server = new Server(tcpProtocol);
        //new Thread(server).start();

        webserver = new Server(httpProtocol);
        //new Thread(webserver).start();

        websocketserver = new Server(wssProtocol);
        //new Thread(websocketserver).start();

        databaseManager = new DatabaseManager();
        //if (cfg.equalsIgnoreCase("local")) {
            databaseManager.setDB_url("localhost");
            databaseManager.setDB_name("jbend");
            databaseManager.setDB_user("root");
            databaseManager.setDB_pass("admin");
        //}
        //new Thread(databaseManager).start();

        loginHandler = new LoginHandler();
        //new Thread(loginHandler).start();

        //new Thread(game).start();

        caretaker= new CareTaker(1800000); //check for database connection every 30mins
        //new Thread(caretaker).start();

        startNextThread();

    }

    public void nextStage() {
        stage++;
        //startNextThread();
        startThreads();
    }


    private void startNextThread() {
        switch (stage) {
            case 0:
                new Thread(console).start();
                break;
            case 1:
                new Thread(databaseManager).start();
                break;
            case 3:
                new Thread(server).start();
                break;
            case 4:
                new Thread(webserver).start();
                break;
            case 5:
                new Thread(websocketserver).start();
                break;
            case 2:
                new Thread(loginHandler).start();
                break;
            case 7:
              //  new Thread(game).start();
               /* System.out.println("testing lexer...");
                String[] testInput = {"task=[name:text;p,complete:bool]"};
                Lexer L = new Lexer(testInput);
                L.processLine();
                System.out.println("lexer test done.");*/
               // Parser P = new Parser(L.outputData(),L.outputTokens());
                //P.processInput();
                //System.out.println("parser test done.");
                break;
            case 6:
                new Thread(caretaker).start();
                break;
        }
    }



    public Server getServer() {
        if (server==null) {
            for (Runnable r : threads) {
                if (r instanceof Server)
                    if (((Server) r).getAPI().getName().equals("TCP"))
                        return (Server) r;
            }
        }
        return server;
    }

    public Server getWebServer() {
        if (webserver==null) {
            for (Runnable r : threads) {
                if (r instanceof Server)
                    if (((Server) r).getAPI().getName().equals("HTTP"))
                        return (Server) r;
            }
        }
        return webserver;
    }

    public Server getWebsocketServer() {
        if (websocketserver==null) {
            for (Runnable r : threads) {
                if (r instanceof Server)
                    if (((Server) r).getAPI().getName().equals("WebSocket"))
                        return (Server) r;
            }
        }
        return websocketserver;
    }

    public DatabaseManager databaseManager() {
        if (databaseManager==null) {
            for (Runnable r : threads) {
                if (r instanceof DatabaseManager)
                    return (DatabaseManager) r;
            }
        }
        return databaseManager;
    }

    public Console console() {
        if (console==null) {
            for (Runnable r : threads) {
                if (r instanceof Console)
                    return (Console) r;
            }
        }
        return console;
    }

    public LoginHandler getLoginHandler() {
        if (loginHandler==null) {
            for (Runnable r : threads) {
                if (r instanceof LoginHandler)
                    return (LoginHandler) r;
            }
        }
        return loginHandler;
    }

    public CryptoHandler cryptography() {
        if (cryptoHandler==null) {
            for (Runnable r : threads) {
                if (r instanceof CryptoHandler)
                    return (CryptoHandler) r;
            }
        }
        return cryptoHandler;
    }
}