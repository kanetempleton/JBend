package com.db;

import com.server.entity.ServerConnection;

public class Query {

    private ServerConnection from;
    private String query;
    private ServerQuery partOf;
    private boolean logResults;

    public Query(ServerConnection c, String q) {
        from=c;
        query=q;
        partOf=null;
        logResults=true;
    }

    public Query(ServerConnection c, ServerQuery p, String q) {
        from=c;
        query=q;
        partOf=p;
        logResults=true;
    }

    public ServerConnection from() {return from;}
    public String contents() {return query;}
    public ServerQuery getAttachedServerQuery() {
        return partOf;
    }
    public void attachToServerQuery(ServerQuery sq) {
        partOf=sq;
    }
    public void setLog(boolean b) {logResults=b;}
    public boolean logResults(){return logResults;}

}