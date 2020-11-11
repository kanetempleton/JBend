package com.db.crud;

public class DBField {

    private String name;
    private DBType type;
    private DBAttribute[] attributes;

    public DBField(String n, DBType t) {
        name=n;
        type=t;
        attributes=null;
    }
    public DBField(String n, DBType t, DBAttribute[] atr) {
        name=n;
        type=t;
        attributes=atr;
    }

    public static DBField field(String n, String t) {
        if (t.equalsIgnoreCase("text")) {
            return new DBField(n,DBType.TEXT);
        }
        if (t.equalsIgnoreCase("int") || t.equalsIgnoreCase("integer")) {
            return new DBField(n,DBType.INTEGER);
        }
        if (t.equalsIgnoreCase("decimal") || t.equalsIgnoreCase("float") || t.equalsIgnoreCase("double")) {
            return new DBField(n,DBType.DECIMAL);
        }
        if (t.equalsIgnoreCase("boolean") || t.equalsIgnoreCase("bool")) {
            return new DBField(n,DBType.BOOL);
        }
        System.out.println("invalid field type given");
        return null;
    }

    public static DBField field(String n, String t, String atr) {
        DBField fld = field(n,t);
        fld.setAttributes(DBAttribute.attributes(atr));
        return null;
    }

    public void setAttributes(DBAttribute[] atrs) {
        attributes=atrs;
    }

    public void setAttributes(String s) {
        attributes = DBAttribute.attributes(s);
    }

    public DBAttribute[] attributes() {
        return attributes;
    }
}
