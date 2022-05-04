package com.console;

import com.Main;
import com.db.*;
import com.db.crud.lang.*;

import java.util.Arrays;

public abstract class Command {
    String name,info;
    int pad;
    Command(String n, String inf) {
        name=n;
        info=inf;
        pad=3;
    }
    abstract void run(String[] args, String[] rBuf);
    abstract void help();
    void doCommand(String[] args, String[] rBuf) {
        if (checkHelp(args)) {
            help();
        }
        else {
            run(args,rBuf);
        }
    }
    void shortenIndent() {
        if (--pad<=0)
            pad=1;
    }
    String tabSpacing() {
        int i=0;
        String b = "";
        while (i++<pad)
            b+="\t";
        return b;
    }
    boolean checkHelp(String[] args) {
        if (args.length>1)
            if (args[1].equalsIgnoreCase("-help"))
                return true;
        return false;
    }
}



class Help extends Command {
    Help() {
        super("help","list all commands with their descriptions");
    }
    void run(String[] args, String[] rBuf) {
        Console.output("command\t\t\tdescription");
        Console.output("-------\t\t\t------------");
        Console.output("<any> -help\t\tprint detailed usage information");
        rBuf[0]="";
        for (Command c: Console.commands) {
            rBuf[0] += c.name+": "+c.info+"<br>";
            Console.output(c.name + "" + c.tabSpacing() + "" + c.info);
        }
    }
    void help() {
        Console.output("No additional info.");
    }
}




class Connections extends Command {
    Connections() {
        super("connections","display information about current registered connections");
        shortenIndent();
    }
    void run(String[] args, String[] rBuf) {
        rBuf[0]="";
        System.out.println("tcp::>");
        if (Main.launcher.getServer()!=null) {
            Main.launcher.getServer().printConnectionsInfo(rBuf);
        }
        System.out.println("ws::>");
        if (Main.launcher.getWebsocketServer()!=null) {
            Main.launcher.getWebsocketServer().printConnectionsInfo(rBuf);
        }
        System.out.println("http::>");
        if (Main.launcher.getWebServer()!=null) {
            Main.launcher.getWebServer().printConnectionsInfo(rBuf);
        }
    }
    void help() {
        Console.output("No additional info.");
    }
}




class MySQL extends Command {
    MySQL() {
        super("mysql","perform database operations");
    }
    void run(String[] args, String[] rBuf) {
        if (args.length<2)
            Console.output("Syntax error. Type \"mysql -help\" for usages.");
        else {
            switch (args[1]) {
                case "connect":
                    Main.launcher.databaseManager().connectToDatabase();
                    return;
                case "disconnect":
                    Main.launcher.databaseManager().disconnectFromDatabase();
                    return;
                case "query":
                    String q = "";
                    for (int i=2; i<args.length-1; i++) {
                        q+=args[i]+" ";
                    }
                    if (args.length>2)
                        q+=args[args.length-1];
                    Main.launcher.databaseManager().query(null,q);
                    return;
                case "set":
                    if (args.length!=4)
                        Console.output("Syntax error. Example execution: mysql set url localhost");
                    else
                        switch (args[2]) {
                            case "url":
                                Main.launcher.databaseManager().setDatabaseURL(args[3],-1);
                                break;
                            case "port":
                                Main.launcher.databaseManager().setDatabaseURL(null,Integer.parseInt(args[3]));
                                break;
                            case "db":
                                Main.launcher.databaseManager().setCurrentDatabase(args[3]);
                                break;
                            case "user":
                                Main.launcher.databaseManager().setLoginCredentials(args[3],null);
                                break;
                            case "pass":
                                Main.launcher.databaseManager().setLoginCredentials(null,args[3]);
                                break;
                            default:
                                Console.output("Specified data field "+args[2]+" does not exist.");
                                break;
                        }
                    return;
                case "info":
                    Console.output("current fields:");
                    Console.output("\tdb_url: "+Main.launcher.databaseManager().getURL());
                    Console.output("\tdb_port: "+Main.launcher.databaseManager().getPort());
                    Console.output("\tdb_name: "+Main.launcher.databaseManager().getDatabaseName());
                    Console.output("\tdb_user: "+Main.launcher.databaseManager().getDatabaseUser());
                    Console.output("\tdb_pass: "+Main.launcher.databaseManager().getDatabasePassword());
                    return;
                default:
                    Console.output("Syntax error. See \"mysql -help\" for usage information.");
                    return;
            }
        }
    }
    void help() {
        Console.output("default fields:");
        Console.output("\tdb_url: localhost");
        Console.output("\tdb_port: 3306");
        Console.output("\tdb_name: com");
        Console.output("\tdb_user: root");
        Console.output("\tdb_pass: admin");
        Console.output("usages:");
        Console.output("\tmysql <connect/disconnect>\t\t\tconnect or disconnect from current database");
        Console.output("\tmysql query $Q \t\t\t\t\tsend query $Q to connected database");
        Console.output("\tmysql set <url/port/db/user/pass> $val \t\tset connection fields. 'db' param is db_name");
        Console.output("\tmysql info\t\t\t\t\tdisplay current values of connection fields");
    }
}

class Crypto extends Command {
    Crypto() {
        super("crypto","perform cryptography operations");
    }
    void run(String[] args, String[] rBuf) {
        if (args.length<4)
            Console.output("syntax error; see crypto -help");
        else {
            if (args[1].equalsIgnoreCase("xor")) {
                byte[] b1 = args[2].getBytes();
                byte[] b2 = args[3].getBytes();
                System.out.println("xor result = "+new String(Main.launcher.cryptography().xor(b1,b2)));
            }
            if (args[1].equalsIgnoreCase("encrypt")) {
                if (args.length<5) {
                    Console.output("bad");
                    return;
                }

                byte[] b1 = args[2].getBytes();
                byte[] b2 = args[3].getBytes();
                byte[] b3 = args[4].getBytes();
                //System.out.println("encr result = "+Main.launcher.cryptography().encrypt(b1,b2,b3));
            }
        }
    }
    void help() {
        Console.output("usages:");
        Console.output("\tcrypto <encrypt/decrypt> $key $text\t\t\tencrypt/decrypt");
        Console.output("\tcrypto xor $text1 $text2 \t\t\t\t\txor two texts");
    }
}

class Kick extends Command {
    Kick() {
        super("kick","drop a connection from the server");
    }
    void run(String[] args, String[] rBuf) {
        if (args.length!=3) {
            Console.output("syntax error; see kick -help");
            rBuf[0]="syntax error";
        }
        else {
            try {
                int id = Integer.parseInt(args[2]);
                if (args[1].equalsIgnoreCase("tcp")) {
                    if (id==-1) {
                        Main.launcher.getServer().dropAllConnections();
                        rBuf[0] = "Kicked all TCP connections!";
                        System.out.println(rBuf[0]);
                    } else {
                        int x = Main.launcher.getServer().kickConnection(id);
                        if (x == 0) {
                            rBuf[0] = "connection id not found on tcp server";
                            System.out.println(rBuf[0]);
                        } else if (x == 1) {
                            rBuf[0] = "successfully kicked connection from tcp server";
                            System.out.println(rBuf[0]);
                        }
                    }
                } else if (args[1].equalsIgnoreCase("http")) {
                    if (id==-1) {
                        Main.launcher.getWebServer().dropAllConnections();
                        rBuf[0] = "Kicked all HTTP connections!";
                        System.out.println(rBuf[0]);
                    } else {
                        int x = Main.launcher.getWebServer().kickConnection(id);
                        if (x == 0) {
                            rBuf[0] = "connection id not found on http server";
                            System.out.println(rBuf[0]);
                        } else if (x == 1) {
                            rBuf[0] = "successfully kicked connection from tcp server";
                            System.out.println(rBuf[0]);
                        }
                    }
                } else if (args[1].equalsIgnoreCase("ws")) {
                    if (id==-1) {
                        Main.launcher.getWebsocketServer().dropAllConnections();
                        rBuf[0] = "Kicked all websocket connections!";
                        System.out.println(rBuf[0]);
                    } else {
                        int x = Main.launcher.getWebsocketServer().kickConnection(id);
                        if (x == 0) {
                            rBuf[0] = "connection id not found on ws server";
                            System.out.println(rBuf[0]);
                        } else if (x == 1) {
                            rBuf[0] = "successfully kicked connection from tcp server";
                            System.out.println(rBuf[0]);
                        }
                    }
                } else {
                    rBuf[0]="invalid server; use tcp/http/ws";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    void help() {
        Console.output("usages:");
        Console.output("kick <tcp/ws/http> $connectionID");
        //rBuf[0]="usage: kick <tcp/ws/http> $connectionID";
    }
}


class Ping extends Command {
    Ping() {
        super("ping","ping all server connections");
    }
    void run(String[] args, String[] rBuf) {
        if (args.length!=1) {
            Console.output("syntax error; see ping -help");
            rBuf[0]="syntax error";
        }
        else {
            try {
                rBuf[0]="Attempting ping pong...<br>";
                if (Main.launcher.getServer()!=null) {
                    rBuf[0]+="pinging all TCP connections...<br>";
                    Main.launcher.getServer().pingpongCheck();
                }
               /* if (Main.launcher.getWebServer()!=null) {
                    rBuf[0]+="pinging all HTTP connections...<br>";
                    Main.launcher.getWebServer().pingpongCheck();
                }*/
                if (Main.launcher.getWebsocketServer()!=null) {
                    rBuf[0]+="pinging all WebSocket connections...<br>";
                    Main.launcher.getWebsocketServer().pingpongCheck();
                }
                rBuf[0]+="Done!";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    void help() {
        Console.output("usages:");
        Console.output("ping");
        //rBuf[0]="usage: kick <tcp/ws/http> $connectionID";
    }
}


class Ban extends Command {
    Ban() {
        super("ban","ban a user from these services");
    }
    void run(String[] args, String[] rBuf) {
        if (args.length!=3) {
            Console.output("syntax error; see ban -help");
            rBuf[0]="syntax error";
        }
        else {
            try {
                boolean flag = false;
                boolean remove=false;
                if (args[1].equals("user")) {
                    flag=false;
                } else if (args[1].equals("ip")) {
                    flag=true;
                } else {
                    Console.output("syntax error; use ban <user/ip> $personToBan");
                    return;
                }
                String banname = args[2];
                if (banname.length()>14 && !flag) {
                    rBuf[0]="no action taken; max username is 14 chars";
                    Console.output(rBuf[0]);
                    return;
                } else {
                    if (!flag) { //username ban
                        rBuf[0]="user ban success: "+banname;
                        new ServerQuery(Main.launcher.getLoginHandler(), "select COUNT(*) from bans where name='" + banname + "';") {
                            public void done() {
                                if (Integer.parseInt(this.getResponse()) == 0) { //ok!
                                    String q = "INSERT INTO bans(name) VALUES('" + banname + "');";
                                    new ServerQuery(Main.launcher.getLoginHandler(), q) {
                                        public void done() {
                                            Main.launcher.getLoginHandler().addToBannedUsers(banname);
                                        }
                                    };
                                } else { //already banned
                                    System.out.println("ALREADY BANNED!");
                                }
                            }
                        };
                    } else { //ipban
                        rBuf[0]="ipban success: "+banname;
                        new ServerQuery(Main.launcher.getLoginHandler(), "select COUNT(*) from bans where ip='" + banname + "';") {
                            public void done() {
                                if (Integer.parseInt(this.getResponse()) == 0) { //ok!
                                    String q = "INSERT INTO bans(ip) VALUES('" + banname + "');";
                                    new ServerQuery(Main.launcher.getLoginHandler(), q) {
                                        public void done() {
                                            Main.launcher.getLoginHandler().addToBannedIPs(banname);
                                        }
                                    };
                                } else { //already banned
                                    System.out.println("ALREADY BANNED!");
                                }
                            }
                        };
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    void help() {
        Console.output("usages:");
        Console.output("ban <name/ip> $personToBan");
        //rBuf[0]="usage: kick <tcp/ws/http> $connectionID";
    }
}

class Unban extends Command {
    Unban() {
        super("unban","unban a user from these services");
    }
    void run(String[] args, String[] rBuf) {
        if (args.length!=3) {
            Console.output("syntax error; see ban -help");
            rBuf[0]="syntax error";
        }
        else {
            try {
                boolean flag = false;
                if (args[1].equals("user")) {
                    flag=false;
                } else if (args[1].equals("ip")) {
                    flag=true;
                } else {
                    rBuf[0]="unban success";
                    Console.output("syntax error; use unban <user/ip> $personToUnban");
                    return;
                }
                String banname = args[2];
                if (banname.length()> 14 && !flag) {
                    rBuf[0]="no action taken; max username is 14 chars";
                    Console.output(rBuf[0]);
                    return;
                } else {
                    if (!flag) { //username unban
                        rBuf[0]="unban user success: "+banname;
                        new ServerQuery(Main.launcher.getLoginHandler(), "delete from bans where name='" + banname + "';") {
                            public void done() {
                                Main.launcher.getLoginHandler().removeBannedUser(banname);
                            }
                        };
                    } else { //ip unban
                        rBuf[0]="unban ip success: "+banname;
                        new ServerQuery(Main.launcher.getLoginHandler(), "delete from bans where ip='" + banname + "';") {
                            public void done() {
                                Main.launcher.getLoginHandler().removeBannedIP(banname);
                            }
                        };
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    void help() {
        Console.output("usages:");
        Console.output("unban <name/ip> $personToUnban");
        //rBuf[0]="usage: kick <tcp/ws/http> $connectionID";
    }
}



class Entities extends Command {
    Entities() {
        super("entities","display all game entities");
    }
    void run(String[] args, String[] rBuf) {
        if (args.length!=1) {
            Console.output("syntax error; see entities -help");
            rBuf[0]="syntax error";
        }
        else {
            try {
               // Main.launcher.game().entityManager().listEntities();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    void help() {
        Console.output("usages:");
        Console.output("unban <name/ip> $personToUnban");
        //rBuf[0]="usage: kick <tcp/ws/http> $connectionID";
    }
}

class Lex extends Command {
    Lex() {
        super("lexer","test dbm lang lexer");
    }
    void run(String[] args, String[] rBuf) {
        if (args.length<2) {
            Console.output("syntax error; see lexer -help");
            rBuf[0]="syntax error";
        }
        else {
            try {
                // Main.launcher.game().entityManager().listEntities();
                String b = "";
                int i=0;
                for (String a: args)
                    if (i++>0)
                        b += a+" ";
                b=b.substring(0,b.length()-1);
                Lexer L = new Lexer(new String[]{b});
                L.processLine();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    void help() {
        Console.output("usages:");
        Console.output("lexer $inputString");
        //rBuf[0]="usage: kick <tcp/ws/http> $connectionID";
    }
}