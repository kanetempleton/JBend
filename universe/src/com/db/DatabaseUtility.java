package com.db;

import com.server.Server;

import java.util.ArrayList;
import com.util.HTMLGenerator;

public abstract class DatabaseUtility {
    private ArrayList<ServerQuery> pending;
    private ArrayList<ServerQuery> ready;

    private String table;

    public DatabaseUtility() {
        pending = new ArrayList<>();
        ready = new ArrayList<>();
    }

    public DatabaseUtility(String tableName) {
        pending = new ArrayList<>();
        ready = new ArrayList<>();
        table=tableName;
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
    public void setTable(String t) {table=t;}

    public abstract void serverAction(ServerQuery q);

    public ServerQuery select_html_table(String[] fieldNames, String[] fieldConditions, String[] valueConditions) {
        String toSelect = "";
        for (int f=0; f<fieldNames.length; f++) {
            if (f!=fieldNames.length-1)
                toSelect += fieldNames[f]+",";
            else
                toSelect += fieldNames[f];
        }

        String buildQuery = "SELECT "+toSelect+" FROM "+table+"";
        if (fieldConditions!=null && fieldConditions.length>0 && fieldConditions[0].length()>0
            && valueConditions!=null && valueConditions.length>0 && valueConditions[0].length()>0
            && fieldConditions.length == valueConditions.length) {
            buildQuery += " WHERE ";
            for (int i=0; i<fieldConditions.length; i++) {
                if (i!=fieldConditions.length-1)
                    buildQuery += fieldConditions[i]+"="+valueConditions[i]+", ";
                else
                    buildQuery += fieldConditions[i]+"="+valueConditions[i];
            }
            buildQuery+=";";
        }
        return new ServerQuery(this,buildQuery) {
            public void done() {
                String[] dat = new String[this.responseSize()];

                for (int i=0; i<this.responseSize(); i++) {
                    dat[i] = "";
                    for (String f: fieldNames) {
                        dat[i]+=""+this.responseParamValue(i,f)+",;,";
                    }
                    //this.responseParamValue(i,"title");
                }
                String[][] dat2 = new String[this.responseSize()][fieldNames.length];
                for (int i=0; i<this.responseSize(); i++) {
                    dat2[i] = dat[i].split(",;,");
                }
                String htmlTable = HTMLGenerator.generateTable(fieldNames,dat2);
                setReturnValue(htmlTable);
            }
        };
    }

}