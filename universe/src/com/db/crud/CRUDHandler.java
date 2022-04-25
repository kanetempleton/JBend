package com.db.crud;

import com.console.Console;
import com.db.DatabaseUtility;
import com.db.ServerQuery;

public class CRUDHandler extends DatabaseUtility implements Runnable {

    private String table;

    public CRUDHandler(String t) {
       // table = t;
    }

    public void start() {
        //initTable();
    }

    public void initTable(String name) {

    }


    public void serverAction(ServerQuery Q) {

    }

    public void run() {
       // Console.output("starting CRUD handler for "+t);
        start();
    }
}
