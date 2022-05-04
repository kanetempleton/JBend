package com.db.queries;

import com.db.DatabaseUtility;
import com.db.ServerQuery;
import com.util.Tools;

public class DropColumnQuery extends ServerQuery {

    public DropColumnQuery(DatabaseUtility U, String col) {
        // super(U,"SELECT "+translate(function)+" FROM information_schema.columns WHERE table_name='"+U.getTable()+"';");
        super(U,"ALTER TABLE "+U.getTable()+" DROP COLUMN "+col+";");
    }

    public DropColumnQuery(DatabaseUtility U, String[] cols) {
        // super(U,"SELECT "+translate(function)+" FROM information_schema.columns WHERE table_name='"+U.getTable()+"';");
        super(U,"ALTER TABLE "+U.getTable()+" "+colstring(cols)+";");
    }

    private static String colstring(String[] cols) {
        String b = "DROP COLUMN ";
        for (int i=0; i<cols.length; i++) {
            if (i!=cols.length-1) {
                b+=cols[i]+", DROP COLUMN ";
            } else {
                b+=cols[i]+"";
            }
        }
        return b;
    }
}
