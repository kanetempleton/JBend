/* TODO: support multiple concurrent database connections

 */

package com.db;

import java.sql.*;
import com.console.*;
import java.util.LinkedList;
import com.server.entity.ServerConnection;
import com.Main;



public class DatabaseManager implements Runnable  {

    private String dburl,dbname,dbuser,dbpass;
    private int dbport;
    private Connection connection;

    private LinkedList<Query> queryQueue;

    private boolean connect,disconnect,reconnect;
    public boolean awake;
    public boolean dontsleep;
    private DBFunctions quickFunctions;

    public DatabaseManager() {
        dburl = "127.0.0.1";
        dbport = 3306;
        dbname = "jbend";
        dbuser = "root";
        dbpass = "admin";
        connection = null;
        queryQueue = new LinkedList<>();
        disconnect=awake=false;
        connect=true;
        reconnect=false;
        dontsleep=false;
        quickFunctions = new DBFunctions();
        //connection = DriverManager.getConnection(dburl,dbuser,dbpass);
    }


    public void runStartupDatabaseCheck() {
    //    quickFunctions.createTable("maps","mapcode INTEGER, data TEXT");
        quickFunctions.createTable("users","id INTEGER PRIMARY KEY AUTO_INCREMENT, username TEXT, password TEXT, email TEXT, privileges INTEGER");
    //    quickFunctions.createTable("players","username TEXT, x INTEGER, y INTEGER, z INTEGER");
        quickFunctions.createTable("settings","name TEXT, value TEXT");
        quickFunctions.createTable("bans","name TEXT, ip TEXT");
    }

    @Override
    public void run() {
        while (true) {
            awake=true;
            if (reconnect) {
                disconnect();
                connect=true;
            }
            if (connect) {
                connect();
                connect=false;
                if (reconnect) {
                    reconnect=false;
                    dontsleep=false;
                }
            } else if (disconnect) {
                disconnect();
                disconnect=false;
            } else {
                while (queryQueue.size() > 0) {
                    Query Q = queryQueue.removeFirst();
                    doQuery(Q);
                }
            }
            if (!dontsleep) {
                awake = false;
                enterSleep();
            }
        }
    }


    public static void wakeUp() {
        if (Main.launcher.databaseManager().awake)
            return;
        synchronized(Main.launcher.databaseLock){
            Main.launcher.databaseLock.notifyAll();
        }
    }
    public static void enterSleep() {
        synchronized(Main.launcher.databaseLock){
            try{
                Main.launcher.databaseLock.wait();
            } catch(InterruptedException e){
                Console.output("sleep interrupted");
            }
        }
    }

    public void connectToDatabase() {
        connect=true;
        wakeUp();
        //connect();
    }
    public void disconnectFromDatabase() {
        disconnect=true;
        wakeUp();
        //disconnect();
    }
    /* query(c,q):
        insert a query into the queue to be executed and trigger a wake up
     */
    public void query(ServerConnection c, String q) {
        Query Q = new Query(c,q);
        queryQueue.addLast(Q);
        //Main.server.sendDatabase = true;
        wakeUp();
    }
    public void query(Query Q) {
        queryQueue.addLast(Q);
        wakeUp();
    }


    /* doQuery(q):
        perform query processing logic.
     */

    private void doQuery(Query Q) {
        ServerConnection c = Q.from();
        String q = Q.contents();
        String rv = "";
        if (connection==null) {
            Console.output("Error: not connected to a database.");
            return;
        }

        if (Q.logResults())
            Console.output("Attempting query: "+q);
        try (Statement s = connection.createStatement();) {
            if (s.execute(q)) {
                int numCols = s.getResultSet().getMetaData().getColumnCount();
                while (s.getResultSet().next()) {
                    String row = "";
                    for (int i = 1; i <= numCols; i++) {
                        row += "["+s.getResultSet().getMetaData().getColumnName(i)+"="+s.getResultSet().getString(i) + "] ";
                    }
                    rv=row;
                    if (Q.getAttachedServerQuery()!=null)
                        Q.getAttachedServerQuery().appendResponse(rv);
                    if (Q.logResults())
                        Console.output("Row: "+row+""); //process rows here

                }
                s.getResultSet().close();

            }
            //s.close();
        } catch (SQLException e) {
            Console.output("query error: "+e.getMessage());
            //disconnect();
           // connect();
        }
        if (Q.logResults())
            Console.output("Query complete.");

        if (rv.length()>0) {
            String build = "";
            String subbuild = "";
            int stage = 0;
            for (int i=0; i<rv.length(); i++) {
                char ch = rv.charAt(i);
                if (ch == '[' && stage==0) {
                    if (build.length()>0)
                        build+=',';
                    stage=1;
                    continue;
                }
                if (ch == ']' && stage==1) {
                    stage=0;
                    continue;
                }
                if (stage==1) {
                    build+=ch;
                }
                //build+=rv.charAt(i);

            }
            rv=build;
        }

       // System.out.println("rv:"+rv);
        if (rv.startsWith("COUNT(*)=")) {
            rv = rv.substring(9);
        }
        if (Q.getAttachedServerQuery()!=null) {
            Q.getAttachedServerQuery().finish(rv);
        }
    }

    /* queryUpdate(q):
        execute a query that updates a table in database
        returns the number of records affected

        TODO: implement thread-level support for this
     */

    public int queryUpdate(String q) {
        if (connection==null) {
            Console.output("Error: not connected to a database.");
            return -1;
        }
        try (Statement s = connection.createStatement();) {
            int rv = s.executeUpdate(q);
            //s.close();
            return rv;
        } catch (SQLException e) {
            Console.output("SQL Error: "+e.getMessage());
        }
        return -1;
    }

    public void resetConnection() {
        dontsleep=true;
        reconnect=true;
        wakeUp();
    }

    /* setDatabaseURL(host,port):
        set host url and port of database to connect to.
        pass host as null or port as -1 to leave unchanged
     */
    public void setDatabaseURL(String host, int port) {
        if (host!=null)
            dburl=host;
        if (port!=-1)
            dbport=port;
        Console.output("Updated database url: "+dburl+":"+dbport);
    }

    public void setCurrentDatabase(String name) {
        dbname = name;
        Console.output("Updated database name to "+dbname);
        connect();
    }

    /* setLoginCredentials(user,pass):
        set the login fields for database connectivity.
        pass a parameter as null to leave it unchanged
     */
    public void setLoginCredentials(String user, String pass) {
        if (user!=null)
            dbuser = user;
        if (pass!=null)
            dbpass = pass;
        Console.output("Updated login credentials: user="+dbuser+" ; pass="+dbpass);
        checkRefresh();
    }


    private void connect() {
        Console.output("Attempting database connection to "+getFullURL());
        try {
            if (connection != null)
                disconnectFromDatabase();
            connection = DriverManager.getConnection(getFullURL(), dbuser, dbpass);
            Console.output("Connection opened successfully!");
          //  runStartupDatabaseCheck();
            Main.launcher.nextStage();
        } catch (SQLException e) {
            Console.output("Error connecting to database.");
            e.printStackTrace();
        }
    }

    private void disconnect() {
        if (connection==null) {
            Console.output("Error: not connected to a database.");
            return;
        }
        Console.output("Disconnecting from current database...");
        try {
            connection.close();
            connection = null;
            Console.output("Connection closed successfully.");
        } catch (SQLException e) {
            Console.output("Error disconnecting from database.");
        }
    }

    public String getFullURL() {
        return "jdbc:mysql://"+dburl+":"+dbport+"/"+dbname+"?useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=America/Chicago";
    }

    private void checkRefresh() {
        if (connection!=null)
            Console.output("You will need to disconnect and connect again for these changes to apply.");
    }



    /*
    private String dburl,dbname,dbuser,dbpass;
    private int dbport;
    private Connection connection;
     */
    public String getURL(){return dburl;}
    public String getDatabaseName(){return dbname;}
    public String getDatabaseUser(){return dbuser;}
    public String getDatabasePassword() {return dbpass;}
    public int getPort() {return dbport;}
    protected LinkedList<Query> getQueue() {return queryQueue;}


    public static String getValue(String s, String field) {
        String inner = s.substring(1,s.length()-2);
        if (inner.split("=").length==2) {
            String f = inner.split("=")[0];
            String v = inner.split("=")[1];
            if (f.equals(field))
                return v;
        }
        return "null";
    }

    public void setDB_url(String url) {
        dburl=url;
    }
    public void setDB_name(String name) {
        dbname=name;
    }
    public void setDB_user(String usr) {
        dbuser=usr;
    }
    public void setDB_pass(String pass) {
        dbpass=pass;
    }

    public DBFunctions quickFunctions() {
        return quickFunctions;
    }
}