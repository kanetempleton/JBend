package com.db;

import java.util.ArrayList;

public abstract class DatabaseUtility {
    private ArrayList<ServerQuery> pending;
    private ArrayList<ServerQuery> ready;

    private String table;

    public DatabaseUtility() {
        pending = new ArrayList<>();
        ready = new ArrayList<>();
    }

    public ArrayList<ServerQuery> readyRequests() {
        return ready;
    }

    public ArrayList<ServerQuery> pendingRequests() {
        return pending;
    }

    public void pop() {
       /* if (ready.size()>0) {
            System.out.println("poppin query");
            ServerQuery q = ready.remove(0);
            serverAction(q);
        }*/

    }

    public String getTable(){return table;}

    public abstract void serverAction(ServerQuery q);
}