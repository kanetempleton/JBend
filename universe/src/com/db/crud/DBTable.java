package com.db.crud;

import java.util.ArrayList;

public class DBTable {

    private String name;
    private DBField[] fields;
    private int numFields;
    private boolean valid;

    private ArrayList<DBEntry> entries;

    public DBTable(String n) {
        name=n;
        fields=null;
        numFields=0;
        valid=false;
        entries=null;
    }

    public DBTable(String n, DBField[] flds) {
        name = n;
        fields = flds;
        numFields = fields.length;
        valid=false;
        entries=null;
        checkValidity();
    }

    public void checkValidity() {
        if (fields == null || numFields==0) {
            System.out.println("Table "+name+" invalid: no fields.");
            return;
        }
        boolean foundPK = false;

        for (DBField f: fields) {
            for (DBAttribute a: f.attributes()) {
                if (a == DBAttribute.PRIMARY_KEY) {
                    if (foundPK) {
                        System.out.println("Table "+name+" invalid: multiple primary key fields.");
                        return;
                    }
                    foundPK=true;
                }
            }
        }

        valid=true;
    }


    public void addEntry(DBEntry e) {
        entries.add(e);
    }


}
