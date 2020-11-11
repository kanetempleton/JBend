package com.db.crud;

public class CRUDObject {

    private String tableName;


    public CRUDObject(String t) {
        tableName = t;
    }


    public static void testCRUDObject() {
        CRUDObject o = new CRUDObject("tasks");
    }
}
