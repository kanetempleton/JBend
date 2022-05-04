package com.db.queries;

import com.db.DatabaseUtility;
import com.db.ServerQuery;
import com.util.Tools;

public class SelectQuery extends ServerQuery {

    public SelectQuery(DatabaseUtility U, String[] cols, String[] fieldConditions, String[] valueConditions) {
        super(U,"SELECT "+ Tools.comma_string(cols)+" FROM "+U.getTable()+" WHERE "+matchFieldsValues(fieldConditions,valueConditions)+";");
    }

    public SelectQuery(DatabaseUtility U, String[] fieldCond, String[] valCond) {
        this(U,new String[]{"*"},fieldCond,valCond);
    }

    public SelectQuery(DatabaseUtility U, String f, String v) {
        this(U,new String[]{"*"},new String[]{f},new String[]{v});
    }

    public SelectQuery(DatabaseUtility U, String[] cols, String f, String v) {
        this(U,cols,new String[]{f},new String[]{v});
    }

    private static String matchFieldsValues(String[] F, String[] V) {
        if (F.length!=V.length)
            return "mismatch";
        String b = "";
        for (int i=0; i<F.length; i++) {
            b+=F[i]+"="+V[i];
            if (i!=F.length-1)
                b+=" AND ";
        }
        return b;
    }
}