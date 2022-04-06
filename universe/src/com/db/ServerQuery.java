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
    private String response_params[][][];

    private String returnValue;

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
        response_params = new String[][][]{};
        returnValue = "null";
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
        response_params = new String[][][]{};
        returnValue = "null";
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
        response_params = new String[][][]{};
        returnValue = "null";
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
        response_params = new String[][][]{};
        returnValue = "null";
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
        response_params = new String[][][]{};
        returnValue = "null";
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
        response_params = new String[][][]{};
        returnValue = "null";
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
        response_params = new String[][][]{};
        returnValue = "null";
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
        response_params = new String[][][]{};
        returnValue = "null";
        execute();
    }

    private void execute() {
        Main.launcher.databaseManager().query(query);
        util.pendingRequests().add(this);
    }

    public void finish(String re) {
        System.out.println("done()! responseSize="+responseSize());
        response_params = new String[responseSize()][2][];
        for (int i=0; i<responseSize(); i++) {
            loadResponseParams(i);
        }
        response=re;
        completed=true;
        util.pendingRequests().remove(this);
        util.serverAction(this);
        done();
    }

    public void done() {
       /* System.out.println("done()! responseSize="+responseSize());
        response_params = new String[responseSize()][2][];
        for (int i=0; i<responseSize(); i++) {
            loadResponseParams(i);
        }*/

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
        //String[][] params = responseParams(i);
        for (int j=0; j<response_params[i][0].length; j++) {
            if (response_params[i][0][j].equals(field)) {
                return response_params[i][1][j];
            }
        }
        return "DNE"; //field does not exist
    }

    public void loadResponseParams(int i) {
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
        response_params[i] = new String[][]{field.split(",;,"),val.split(",;,")};
    }

    public String[][] responseParams(int i) {
        if (response_params.length > 0 && response_params.length<=i+1) {
            return response_params[i];
        }
        return new String[][]{{"not"},{"found"}};
       /* else {
            loadResponseParams(i);
            return response_params[i];
        }*/
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

    public void setReturnValue(String r) {
        returnValue=r;
    }
    public String getReturnValue() {return returnValue;}

    //response_params[i:0,..,numResults][j:0,1][k:0,..,numFields] =
    // result i's field name k or value name k

    public String[] response_getFields() {
        return response_params[0][0];
    }

    public String[][] response_getValues() {
        int numFields = response_params[0][0].length;
        int numResults = response_params.length;
        String[][] out = new String[numResults][numFields];
        for (int i=0; i<numResults; i++) {
            for (int j=0; j<numFields; j++) {
                out[i][j] = response_params[i][1][j];
            }
        }
        return out;
    }

    public DatabaseUtility getUtil() {return util;}
}