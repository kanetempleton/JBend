package com.db;

import com.server.*;
import com.server.entity.*;
import com.Main;
import com.server.protocol.*;
import com.server.web.*;
import java.util.ArrayList;

public class ServerQuery {


    public static final int REGISTER_REQUEST = 132;
    public static final int LOGIN_REQUEST = 332;
    public static final int CHECK_FOR_USER = 240;

    private Server fromServer;
    private int type;

    private DatabaseUtility util;
    private String response;
    private boolean completed;
    private Query query;
    private String payload;
    private String extra_data[];
    private ArrayList<Cookie> outgoingCookies;
    private ArrayList<String> multiResponse;

    public ServerQuery(ServerQuery S, ServerConnection c, String q) {
        util=S.util();
        fromServer=c.getServer();
        completed=false;
        response="";
        payload="";
        this.type=-1;
        extra_data = new String[5];
        for (int i=0; i<5; i++)
            extra_data[i] = "";
        query = new Query(c,this,q);
        outgoingCookies = new ArrayList<>();
        multiResponse = new ArrayList<>();
        execute();
    }

    public ServerQuery(DatabaseUtility U, ServerConnection c, String q) {
        util=U;
        fromServer=c.getServer();
        completed=false;
        response="";
        payload="";
        this.type=-1;
        extra_data = new String[5];
        for (int i=0; i<5; i++)
            extra_data[i] = "";
        query = new Query(c,this,q);
        outgoingCookies = new ArrayList<>();
        multiResponse = new ArrayList<>();
        execute();
    }

    public ServerQuery(DatabaseUtility U, String q) {
        util=U;
        fromServer=null;
        completed=false;
        response="";
        payload="";
        this.type=-1;
        extra_data = new String[5];
        for (int i=0; i<5; i++)
            extra_data[i] = "";
        query = new Query(null,this,q);
        outgoingCookies = new ArrayList<>();
        multiResponse = new ArrayList<>();
        execute();
    }
    public ServerQuery(DatabaseUtility U, String q, boolean log) {
        util=U;
        fromServer=null;
        completed=false;
        response="";
        payload="";
        this.type=-1;
        extra_data = new String[5];
        for (int i=0; i<5; i++)
            extra_data[i] = "";
        query = new Query(null,this,q);
        query.setLog(log);
        outgoingCookies = new ArrayList<>();
        multiResponse = new ArrayList<>();
        execute();
    }

    public ServerQuery(DatabaseUtility U, ServerConnection c, int type, String q) {
        util=U;
        fromServer=c.getServer();
        completed=false;
        response="";
        payload="";
        this.type=type;
        extra_data = new String[5];
        for (int i=0; i<5; i++)
            extra_data[i] = "";
        query = new Query(c,this,q);
        outgoingCookies = new ArrayList<>();
        multiResponse = new ArrayList<>();
        execute();
    }
    public ServerQuery(DatabaseUtility U, Server s, ServerConnection c, int type, String q) {
        util=U;
        fromServer=s;
        completed=false;
        response="";
        payload="";
        this.type=type;
        extra_data = new String[5];
        for (int i=0; i<5; i++)
            extra_data[i] = "";
        query = new Query(c,this,q);
        outgoingCookies = new ArrayList<>();
        multiResponse = new ArrayList<>();
        execute();
    }
    public ServerQuery(DatabaseUtility U, Server s, ServerConnection c, int type, String q, String p) {
        util=U;
        fromServer=s;
        completed=false;
        response="";
        payload=p;
        this.type=type;
        extra_data = new String[5];
        for (int i=0; i<5; i++)
            extra_data[i] = "";
        query = new Query(c,this,q);
        outgoingCookies = new ArrayList<>();
        multiResponse = new ArrayList<>();
        execute();
    }
    public ServerQuery(DatabaseUtility U, Server s, ServerConnection c, int type, String q, String p, String[] x) {
        util=U;
        fromServer=s;
        completed=false;
        response="";
        payload=p;
        this.type=type;
        extra_data = new String[5];
        for (int i=0; i<x.length; i++) {
            if (i>=5)
                break;
            extra_data[i] = x[i];
        }
        query = new Query(c,this,q);
        outgoingCookies = new ArrayList<>();
        multiResponse = new ArrayList<>();
        execute();
    }

    private void execute() {
        Main.launcher.databaseManager().query(query);
        util.pendingRequests().add(this);
    }

    public void finish(String re) {
        response=re;
        completed=true;
        util.pendingRequests().remove(this);
        util.serverAction(this);
        done();
    }

    public void done() {

    }


    // response = 1 row from database table
    public String getResponse(int i) {
        return multiResponse.get(i);
    }

    public int responseSize() {
        return multiResponse.size();
    }

    //returns the first index of a response with fieldname=field, value=val
    //ex: responseIndex("user","kane") will get the
    public int responseIndex(String field, String val) {
        for (int i=0; i<this.responseSize(); i++) {
            if (responseParamValue(i,field).equals(val))
                return i;
        }
        return -1; //not found
    }

    public String responseParamValue(int i, String field) {
        String r = getResponse(i);
        String[][] params = responseParams(i);
        for (int j=0; j<params[0].length; j++) {
            if (params[0][j].equals(field)) {
                return params[1][j];
            }
        }
        return "DNE"; //field does not exist
    }

    public String[][] responseParams(int i) {
        String r = getResponse(i);
        String field = "";
        String val = "";
        String build = "";
        boolean open = false;
        for (int j=0; j<r.length(); j++) {
            char c = r.charAt(j);
            if (c == '[') {
                open = true;
            }
            else if (c == '=' && open) {
                field += build+",;,";
                build = "";
            }
            else if (c == ']') {
                open = false;
                val += build+",;,";
                build = "";
            }
            else if (open) {
                build+=c;
            }
        }
        return new String[][]{field.split(",;,"),val.split(",;,")};
    }

    public int type() {return type;}
    public Server fromServer() {return fromServer;}
    public ServerConnection fromConnection() {return query.from();}
    public String getResponse() {
        return response;
    }
    public String getPayload() {return payload;}
    public void setPayload(String p){payload=p;}
    public String getExtraData(int i) {
        if (i>=5)
            return "";
        return extra_data[i];
    }
    public void setExtraData(int i, String to) {
        if (i>=5) {
            System.out.println("index out of bounds. data not saved");
            return;
        }
        extra_data[i]=to;
    }

    public void addCookie(Cookie c) {
        this.outgoingCookies.add(c);
    }

    public void sendHTTP(String body) {
        String send = HTTP.HTTP_OK+"";
        for (Cookie co: this.outgoingCookies)
            send+=co.header();
        send+="\r\n"+body;
        this.fromServer().getAPI().sendMessage(this.fromConnection(),send);
    }

    public void close() {
        this.fromConnection().setNeedsReply(false);
        this.fromConnection().disconnect(fromServer);
    }

    public DatabaseUtility util() {
        return util;
    }

    public void appendResponse(String r) {
        multiResponse.add(r);
    }

    public ArrayList<String> getResponses() {
        return multiResponse;
    }
}