package com.db.crud;

public enum DBAttribute {
    PRIMARY_KEY,
    UNIQUE,
    AUTO_INCREMENT

    ;

    public static DBAttribute attribute(char t) {
        switch (t) {
            case 'p':
                return PRIMARY_KEY;
            case 'u':
                return UNIQUE;
            case 'a':
                return AUTO_INCREMENT;
        }
        System.out.println("Invalid attribute character.");
        return null;
    }

    public static DBAttribute[] attributes(String s) {
        DBAttribute[] atrs = new DBAttribute[s.length()];
        for (int i=0; i<s.length(); i++) {
            char c = s.charAt(i);
            DBAttribute a = attribute(c);
            if (a==null)
                return null;
            atrs[i] = a;
        }
        return atrs;
    }


}
