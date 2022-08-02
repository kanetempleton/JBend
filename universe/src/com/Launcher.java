package com;

import com.server.Server;
import com.server.protocol.*;
import com.db.crud.lang.*;
//import com.game.*;

import java.util.*;
import com.func.*;


import com.db.*;
import com.console.*;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import com.server.login.*;
import com.util.crypt.*;
import com.util.*;
import com.lang.conf.*;

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
    private Thread processes[];
    private int numThreads,numProcesses;
    private int dbThread,dbProcess;

    public static final boolean USING_LOGIN_ENCRYPTION = false;
    public static int DEBUG_SERVER_LEVEL = 0;

    public HashMap<String,Integer> threadMap;
    public HashMap<Integer,String> threadMapInverse;
    public HashMap<Integer,Server> serverMap;

    // data

    public HashMap<String,String> lookup;

    // app info
    public String app_id;
    private String app_var_def;

    public Launcher() {
        stage=0;
        dbThread=0;
        dbProcess=0;
        numProcesses=0;
        cfg=new String[10][2];
        app_id="undefined";
        app_var_def="undefined";
        lookup = new HashMap<>();
        for (int i=0; i<cfg.length; i++) {
            for (int j=0; j<cfg[i].length; j++) {
                cfg[i][j]="DNE";
            }
        }
        numThreads=0;
        threads=new Runnable[100];
        processes = new Thread[100];
        databaseLock = new DatabaseLock();
        cryptoHandler = new CryptoHandler();
        threadMap = new HashMap<>();
        threadMapInverse = new HashMap<>();
       // loadConfig();
      //  addConsole();
        serverMap = new HashMap();
        loadConfig();
    }


    public Launcher(int maxThreads) {
        stage=0;
        dbThread=0;
        dbProcess=0;
        numProcesses=0;
        cfg=new String[10][2];
        app_id="undefined";
        app_var_def="undefined";
        lookup = new HashMap<>();
        for (int i=0; i<cfg.length; i++) {
            for (int j=0; j<cfg[i].length; j++) {
                cfg[i][j]="DNE";
            }
        }
        numThreads=0;
        threads=new Runnable[maxThreads];
        processes = new Thread[maxThreads];
        databaseLock = new DatabaseLock();
        cryptoHandler = new CryptoHandler();
        threadMap = new HashMap<>();
        threadMapInverse = new HashMap<>();
       // loadConfig();
       // addConsole();
        serverMap = new HashMap();
        loadConfig();
    }

    public Launcher(String settings) {
        dbThread=0;
        stage=0;
        numProcesses=0;
        dbProcess=0;
        cfg=new String[10][2];
        app_id="undefined";
        app_var_def="undefined";
        lookup = new HashMap<>();
        for (int i=0; i<cfg.length; i++) {
            for (int j=0; j<cfg[i].length; j++) {
                cfg[i][j]="DNE";
            }
        }
        numThreads=0;
        threads=new Runnable[10];
        processes = new Thread[10];
        databaseLock = new DatabaseLock();
        cryptoHandler = new CryptoHandler();
        threadMap = new HashMap<>();
        threadMapInverse = new HashMap<>();
        if (!(settings.equalsIgnoreCase("local") || settings.equalsIgnoreCase("development")))
            loadConfig();
       // addConsole();
        serverMap = new HashMap();
        loadConfig();
    }


    public void loadConfig() {
        //Console.output("loading configuration...");
        String conf = FileManager.fileDataAsString("config/application.conf").replace("\r","");

        String configtokens = ConfLexer.parse(conf);
        System.out.println(configtokens);

        System.out.println("lex'd. now doing parsing...");

        String syntaxtokens = ConfParser.parse(configtokens);
      //  System.out.println(syntaxtokens);

        ArrayList<ConfFunction> program = ConfInterpreter.interpret(syntaxtokens.split("\n"));
        execute(program);

      //  ConfInterpreter interpreter = new ConfInterpreter(syntaxtokens.split("\n"));
        //interpreter.interpret();
       // System.out.println(interpreter.toString());
      //  interpreter.execute();


        //int i=0;
        /*String lines[] = conf.split("\n");
        for (String line: lines) {
            System.out.println("line: "+line);
        }*/
       /* if (!conf.equalsIgnoreCase("DNE")) {
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
                        System.out.println("loaded configuration for: "+field+"");
                        i++;
                    }
                } else {
                    System.out.println("misformatted line: "+p);
                }
            }
        } else {
            System.out.println("can't find config file:"+conf);
        }*/
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

    //TODO: bounds check
    public Runnable getThread(String name) {
        return threads[threadMap.get(name)];
    }

    public void loadThread(Runnable r, String name) {
        System.out.println("Loading thread "+name+" into queue...");
        if (numThreads>=threads.length) {
            System.out.println("Launcher cannot hold any more threads.");
            return;
        }
        if (threadMap.containsKey(name)) {
            String name2 = name+"_dupe";
            threadMap.put(name2,numThreads);
            threadMapInverse.put(numThreads,name2);
            System.out.println("duplicate thread name, renamed to "+name2);
        } else {
            threadMap.put(name, numThreads); //store the index for this thread name
            threadMapInverse.put(numThreads, name);
        }
        threads[numThreads]=r;
        if (r instanceof Server) {
            if (serverMap==null)
                System.out.println("servermap");
            if (r==null)
                System.out.println("r");
           // System.out.println("servermap");
            serverMap.put(((Server)r).getPort(),(Server)r);
        }
        if (r instanceof Console)
            console = (Console)threads[numThreads];
        if (r instanceof DatabaseManager) {
            dbThread = numThreads;
            databaseManager = (DatabaseManager) threads[numThreads];
        }
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
        System.out.println("[LAUNCHER] Loaded thread["+numThreads+"]:"+threadMapInverse.get(numThreads));
        numThreads++;
    }

    public Server getServer(int port) {
        if (serverMap.containsKey(port)) {
            return serverMap.get(port);
        }
        return null;
    }

    public void startThreads() {
        if (stage<numThreads) {
            String asdf = threadMapInverse.get(stage);
            System.out.println("[LAUNCHER] Starting thread["+stage+"]:"+asdf+"...");
            if (asdf.contains("ataserver"))
                dbProcess = stage;
            processes[numProcesses] = new Thread(threads[stage]);
            processes[numProcesses++].start();
        } else {
            System.out.println("stage "+stage+" >= numThreads "+numThreads);
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
            loadThread(new Console(br),"Console");
        }
    }
    public void addDatabaseManager() {
        addDatabaseManager("localhost","jbend","root","admin");
    }
    public void addDatabaseManager(String addr, String name, String user, String pass) {
        DatabaseManager m = new DatabaseManager();

        //outdated:
        String dbURL = getConfig("db_addr").equalsIgnoreCase("DNE") ? "localhost" : getConfig("db_addr");
        String dbName = getConfig("db_name").equalsIgnoreCase("DNE") ? "jbend" : getConfig("db_name");
        String dbUser = getConfig("db_user").equalsIgnoreCase("DNE") ? "root" : getConfig("db_user");
        String dbPass = getConfig("db_pass").equalsIgnoreCase("DNE") ? "admin" : getConfig("db_pass");
        m.setDB_url(addr);
        m.setDB_name(name);
        m.setDB_user(user);
        m.setDB_pass(pass);
        loadThread(m,"DatabaseManager");
    }
    public void realoadDatabaseManager() {
        Console.output("Restarting database manager...");
        databaseManager = null;
        processes[dbProcess].stop();
        threads[dbThread] = null;
        processes[dbProcess] = null;
        DatabaseManager m = new DatabaseManager();

        String dbURL = getConfig("db_addr").equalsIgnoreCase("DNE") ? "localhost" : getConfig("db_addr");
        String dbName = getConfig("db_name").equalsIgnoreCase("DNE") ? "jbend" : getConfig("db_name");
        String dbUser = getConfig("db_user").equalsIgnoreCase("DNE") ? "root" : getConfig("db_user");
        String dbPass = getConfig("db_pass").equalsIgnoreCase("DNE") ? "admin" : getConfig("db_pass");
        m.setDB_url(dbURL);
        m.setDB_name(dbName);
        m.setDB_user(dbUser);
        m.setDB_pass(dbPass);

        threads[dbThread] = m;
        String asdf = threadMapInverse.get(dbThread);
        System.out.println("[LAUNCHER] Starting thread["+stage+"]:"+asdf+"...");
        processes[dbProcess] = new Thread(threads[dbThread]);
        processes[dbProcess].start();

       // String asdf = threadMapInverse.get(stage);
       // System.out.println("[LAUNCHER] Starting thread["+stage+"]:"+asdf+"...");
       // new Thread(threads[stage]).start();

      //  loadThread(m,"DatabaseManager");
    }
    public void addLoginHandler() {
        loadThread(new LoginHandler(),"LoginHandler");
    }
    public void addCareTaker(long delay) {
        loadThread(new CareTaker(delay),"CareTaker");
    }
    public void addTCPServer(int port) {
        TCP tcp = new TCP(port);
        loadThread(new Server(tcp),"TCP");
    }
    public void addHTTPServer(int port) {
        HTTP http = new HTTP(HTTP.DEFAULT_HOME_DIRECTORY,port);
        loadThread(new Server(http),"HTTP");
    }
    public void addHTTPServer(String homeDir, int port) {
        HTTP http = new HTTP(homeDir,port);
        loadThread(new Server(http),"HTTP");
    }
    public void addWebSocketServer(int port) {
        WebSocket ws = new WebSocket(port);
        loadThread(new Server(ws),"WebSocket");
    }


    //pretty sure this method is outdated and unused...
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
        System.out.println("[LAUNCHER] next stage...");
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

    public void rebootDatabaseManager() {

    }

    // functions from conf file

    public void execute(ArrayList<ConfFunction> A) {
        for (ConfFunction f: A) {
            execute(f);
        }
    }

    public void execute(ConfFunction f) {
        if (f instanceof AppDef) {
            setApplicationName(f.args(0));
        }
        else if (f instanceof Def) {
            loadDef(f.args(0));
        }
        else if (f instanceof Fed) {
            closeDef();
        }
        else if (f instanceof Func) {
            execfunc((Func)f);
        } else {
            System.out.println("execution error");
        }
    }

    public void setApplicationName(String n) {
        app_id = n;
    }

    public void loadDef(String d) {
        app_var_def=d;
        store("def",d);
        System.out.println(""+get("def")+":");
    }

    public boolean inDef(){return !get("def").equals("DNE");}

    public void closeDef() {

        String def = get("def");
        switch (def) {
            case "dataserver":
                if (!is("database-url","DNE") && !is("database-name","DNE")
                    && !is("database-cred-user","DNE") && !is("database-cred-pass","DNE")) {
                    addDatabaseManager(get("database-url"),get("database-name"),get("database-cred-user"),get("database-cred-pass"));
                } else {
                    System.out.println("database config incomplete, using defaults...");
                    addDatabaseManager();
                }
                break;
            case "proxy":
                int pport = Integer.parseInt(get("proxy-port"));
                TCPProxy prox = new TCPProxy(pport);


                String[] proxymaps = getKeys_byPrefix("proxy-map-");
                for (String ipkey : proxymaps) {
                    String ip = ipkey.split("-map-")[1];
                    String ipval = get(ipkey);
                    prox.mapPort(ip,Integer.parseInt(ipval));
                    System.out.println("mapping: "+ip+" -> "+ipval);
                }
                Server proxserver = new Server(prox,4096);
                loadThread(proxserver,"proxy");
                break;
            case "webserver":

                HTTP http_protocol = new HTTP(get("webserver-home"),Integer.parseInt(get("webserver-port")));
                Server http = new Server(http_protocol,4096);
                loadThread(http,"HTTP");
                break;
            default:
                break;
        }
        System.out.println();
    }

    public void execfunc(Func f) {
        String function = f.function_name();
        if (f.function_name().startsWith("Func_"))
            function = f.function_name().split("_")[1];
        if (function.contains("-"))
            function = function.split("-")[0];

        String def = def();
        if (def().contains("-"))
            def=def().split("-")[0];

        switch (function) {
            case "source":
                switch (def) {
                    case "data":
                        System.out.println("[PLACEHOLDER] load dataserver credentials "+Tools.tokenize(f.args()," "));
                        break;
                    case "login":
                        System.out.println("[PLACEHOLDER] load loginserver credentials "+Tools.tokenize(f.args()," "));
                        break;
                    case "dataserver":
                       // System.out.println("[PLACEHOLDER] load database credentials "+Tools.tokenize(f.args()," "));
                      //  addDatabaseManager();
                        if (f.args().length<2) {
                            System.out.println("dataserver.source: misformat");
                            break;
                        }
                        store("database-url",f.args(0));
                        store("database-name",f.args(1));
                        break;
                    default:
                        System.out.println("[func] "+function+": execution undefined for "+def+"");
                        break;
                }
            break;
            case "listen":
                switch (def) {
                    case "proxy":
                     //   System.out.println("[PLACEHOLDER] make proxy server listen on port "+Tools.space(f.args()));
                        if (f.args().length == 1)
                            store("proxy-port",f.args(0));
                        break;
                    case "console":
                       // System.out.println("[PLACEHOLDER] initialize console here.");
                        addConsole();
                        break;
                    case "webserver":
                     //   System.out.println("[PLACEHOLDER] load webserver on port "+Tools.space(f.args()));

                        if (f.args().length == 1) {
                            int num_webservers = 0;
                            if (!get("num-webservers").equals("DNE")) {
                                num_webservers = Integer.parseInt(get("num-webservers"));
                            }
                            String tag = "webserver";//-"+num_webservers+"";
                            store("webserver-port",f.args(0));
                            // store("num-webservers",num_webservers+1); // DO THIS ELSEWHERE

                        }
                        break;
                    default:
                        System.out.println("[func] "+function+": execution undefined for "+def+"");
                        break;
                }
                break;
            case "map":
                switch (def) {
                    case "proxy":
                       // System.out.println("[PLACEHOLDER] define proxy mapping "+Tools.space(f.args()));
                        if (f.args().length == 2)
                            store("proxy-map-"+f.args(0), f.args(1));
                        break;
                    default:
                        System.out.println("[func] "+function+": execution undefined for "+def+"");
                        break;
                }
                break;
            case "home":
                switch (def) {
                    case "webserver":
                        System.out.println("[PLACEHOLDER] define http home directory "+Tools.space(f.args()));
                        store("webserver-home",f.args(0));
                        break;
                    default:
                        System.out.println("[func] "+function+": execution undefined for "+def+"");
                        break;
                }
                break;
            case "credentials":
                switch (def) {
                    case "dataserver":
                     //   System.out.println("[PLACEHOLDER] define http home directory "+Tools.space(f.args()));
                        if (f.args().length<2) {
                            System.out.println("dataserver.credentials: misformat");
                            break;
                        }
                        if (f.args().length==2) {
                            store("database-cred-user", f.args(0));
                            store("database-cred-pass", f.args(1));
                        } else if (f.args().length==3) {
                            String encrypt_method = f.args(2);
                            System.out.println("[func] "+function+": encrypt mode "+encrypt_method+" not supported yet");
                            store("database-encrypt-pass",f.args(2));
                        }
                        break;
                    default:
                        System.out.println("[func] "+function+": execution undefined for "+def+"");
                        break;
                }
                break;
            default:
                System.out.println("[func] undefined function "+f);
                break;
        }
    }

    public String def(){return get("def");}

    public String getApplicationName(){return app_id;}

    public String get(String key) {
        if (lookup.containsKey(key))
            return lookup.get(key);
        return "DNE";
    }

    public void store(String key, String value) {
        lookup.put(key,value);
      //  System.out.println("stored data entry for "+key);
    }

    public boolean is(String key,String x) {
        return get(key).equals(x);
    }

    public String[] getKeys(String keyspec) {
        String out = "";
        return null;
    }

    public String[] getKeys_byPrefix(String p) {
        String[] k = new String[Tools.filter_keys_prefix(lookup,p).length];
        int i=0;
        for (String newkey : Tools.filter_keys_prefix(lookup,p)) {
            k[i++] = p+""+newkey;
        }
        return k;
    }


}