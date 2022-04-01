package com.db;


public class DBFunctions extends DatabaseUtility {


    public void createTable(String name, String fields) {
        new ServerQuery(this,"create table if not exists "+name+"("+fields+")") {
            public void done() {
               // System.out.println("result = "+getResponse());
            }
        };
    }

    public void updateTable(String table, String updates, String recordCondition) {
        new ServerQuery(this,"update "+table+" set "+updates+" where "+recordCondition) {
            public void done() {
                // System.out.println("result = "+getResponse());
            }
        };
    }

    public void insertRecord(String table, String values) {
        new ServerQuery(this,"insert into "+table+" values("+values+")") {
            public void done() {
                // System.out.println("result = "+getResponse());
            }
        };
    }



    public void serverAction(ServerQuery Q) {

    }



}