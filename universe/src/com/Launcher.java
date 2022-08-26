package com;

import com.server.Server;
import com.server.protocol.*;
import com.db.crud.lang.*;
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

public class Launcher implements Runnable {

    public static final String JBEND_VERSION = "1.4.0";
    /* releases for version 1
        1.0: Initial JBend (tcp,http,ws,messy database+login+security+encryption) ok this needs cleaned up
        1.1: JBend Jar + Custom App stuff (via ServerQuery + DatabaseUtility)
         - how to make custom with config?
         - fix the stupid webpackets thing that should never have existed
        1.2: CRUD auto-management and database schema checking
        1.3: proxy server
         - behaving weirdly
     -> 1.4: full configuration
         - routes in application.conf
        1.5: data management + security
         - JSON support
         - local cache
         - encryption modes
         - access control
         - enable encryption mode for specified communication link
        1.6: database server
         - backup/restore
        1.7: login server
        1.8: caretaker
         - reboot servers + database regularly
        1.9: html generator
        2.0: FULL DISTRIBUTED JBEND RELEASE
             (distribution + documentation)
             - generate config files
             - readme and use cases
             - documentation
             - simple install scripts
             - app shell
             - version releases
             - live todo list
     */


    private Server server,websocketserver,webserver;
    private DatabaseManager databaseManager;
    private Console console;
    private LoginHandler loginHandler;
    private CryptoHandler cryptoHandler;
    private CareTaker caretaker;
    private String cfg[][];
    public static int stage;
    public static DatabaseLock databaseLock;
    private boolean running;

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

    private NetApplication app;
    private boolean appLoaded;


    public Launcher(NetApplication a) {
        app=a;
        appLoaded =false;
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
        serverMap = new HashMap();
        initLauncher();
    }

    public void run() {

        initLauncher();
    }

    public void startLauncher() {
        running = true;

    }




    public void initLauncher() {
       // running=true;
        System.out.println("Welcome. You are using JBend version "+fetchVersion("config/version.conf"));

        //start the console
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        loadThread(new Console(br),"Console");
        //startThreads();
        //nextStage();
        //configuration
        initialDataStores();
        loadConfig("config/application.conf");
        //startThreads();

    }


    public void initialDataStores() {
        store("console-on","true");
    }


    /* loadConfig
        path to app config file; default is config/application.conf

     */
    public void loadConfig(String filename) {
        String conf = FileManager.fileDataAsString(filename).replace("\r","");

        String configtokens = ConfLexer.parse(conf);
        String syntaxtokens = ConfParser.parse(configtokens);
        //System.out.println("parsed config: "+filename);

        ArrayList<ConfFunction> program = ConfInterpreter.interpret(syntaxtokens.split("\n"));
        execute(program);
      //  System.out.println("config params loaded.");
    }


    public void loadThread(Runnable r, String name) {
       // System.out.println("Loading thread "+name+" into queue...");
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
        System.out.println("[LAUNCHER] Loaded thread["+numThreads+"] "+name);
        numThreads++;
    }

    public void nextStage() {
        stage++;
        //startNextThread();
       // System.out.println("[LAUNCHER] next stage...");
        startThreads();
    }


    public void startThreads() {
        if (stage<numThreads) {
            String asdf = threadMapInverse.get(stage);
            System.out.println("[LAUNCHER] Starting thread["+stage+"]:"+asdf+"...");
            if (asdf.contains("atabase"))
                dbProcess = stage;
            processes[numProcesses] = new Thread(threads[stage]);
            processes[numProcesses++].start();
        } else {
            if (!appLoaded) {
                loadThread(app, app.getAppName());
                appLoaded = true;
                nextStage();
            }
            System.out.println("[Launcher] Successfully started sequence of "+numThreads+" threads:");
            for (int i=0; i<numThreads; i++) {
                System.out.println("Thread["+i+"]="+threadMapInverse.get(i));
            }
            System.out.println("> Type 'help' for list of Console commands");
        }
    }



    //TODO: bounds check
    public Runnable getThread(String name) {
        return threads[threadMap.get(name)];
    }


    public Server getServer(int port) {
        if (serverMap.containsKey(port)) {
            return serverMap.get(port);
        }
        return null;
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
       // System.out.println(""+get("def")+":");
    }



    public boolean inDef(){return !get("def").equals("DNE");}

    public static boolean INTERPRETER_OUTPUT = false;

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
                    //System.out.println("mapping: "+ip+" -> "+ipval);
                }
                Server proxserver = new Server(prox,4096);
                loadThread(proxserver,"proxy");
                break;
            case "webserver":
                System.out.println("[UNLOAD WEBSERVER] "+get("webserver-home")+" "+Integer.parseInt(get("webserver-port")));
                HTTP http_protocol = new HTTP(get("webserver-home"),Integer.parseInt(get("webserver-port")));
                Server http = new Server(http_protocol,4096);
                loadThread(http,"HTTP");
                break;
            default:
                break;
        }
      //  System.out.println();
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
                       // addConsole();
                        store("console-on","true");
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
                        //System.out.println("[PLACEHOLDER] define http home directory "+Tools.space(f.args()));
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


    // annoying access methods

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

    public String fetchVersion(String versionfile) {
        try {
            String v = FileManager.fileDataAsString(versionfile).replace("\r", "").replace("\n", "").replace(".",";;;");
        //    System.out.println("version file: "+versionfile+": "+v);
            String[] n = v.split(";;;");
         //   System.out.println("len:"+v.split(";;;").length);
            if (n.length >= 3) {
                int x = Integer.parseInt(n[0]);
                int y = Integer.parseInt(n[1]);
                int z = Integer.parseInt(n[2]);
                String vur = x + "." + y + "." + (z + 1);
                try {
                    FileManager.writeFile(versionfile,vur);
                    //Runtime.getRuntime().exec("echo " + vur + " > " + versionfile);
                    return vur;
                } catch (Exception e) {
                    System.out.println("Exception writing file "+versionfile);
                   // e.printStackTrace();
                    return vur;
                }
            }
        } catch (Exception e) {
            System.out.println("Processing exception fetching version from "+versionfile);
            return JBEND_VERSION;
        }
        System.out.println("No case found for "+versionfile);
        return JBEND_VERSION;
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


}