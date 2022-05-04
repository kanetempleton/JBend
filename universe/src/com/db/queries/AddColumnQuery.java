package com.db.queries;

import com.db.DatabaseUtility;
import com.db.ServerQuery;

public class AddColumnQuery extends ServerQuery {

    public AddColumnQuery(DatabaseUtility U, String colName, String colType, String defaultValue) {
        super(U,"ALTER TABLE "+U.getTable()+" ADD COLUMN "+colName+" "+colType+" NOT NULL DEFAULT "+defaultValue+";");
    }

    public AddColumnQuery(DatabaseUtility U, String colName, String colType) {
        super(U,"ALTER TABLE "+U.getTable()+" ADD COLUMN "+colName+" "+colType+";");
    }

    public AddColumnQuery(DatabaseUtility U, String[] multiCols, String defaultValue) {
        super(U,"ALTER TABLE "+U.getTable()+" "+add_cols_string(multiCols,defaultValue));
    }

    private static String add_cols_string(String[] multi, String defaultValue) {
        String b = "";
        for (int i=0; i<multi.length; i++) {
            if (i!=multi.length-1) {
                b+="ADD COLUMN "+multi[i]+"";
                if (defaultValue!=null) {
                    b+=" NOT NULL DEFAULT "+defaultValue+"";
                }
                b+=", ";
            } else {
                b+="ADD COLUMN "+multi[i]+"";
                if (defaultValue!=null) {
                    b+=" NOT NULL DEFAULT "+defaultValue+"";
                }
                b+=";";
            }
        }
        return b;
    }

}