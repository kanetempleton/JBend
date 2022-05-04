package com.db.queries;

import com.db.DatabaseUtility;
import com.util.Tools;

public class InsertEntryQuery extends ServerQuery {

    public InsertEntryQuery(DatabaseUtility U, String[] fields, String[] values) {
        super(U,"INSERT INTO "+U.getTable()+"("+Tools.comma_string(fields)+") VALUES("+Tools.comma_string(values)+");");
    }

    public InsertEntryQuery(DatabaseUtility U, String[] fields, String[] types, String[] values) {
        super(U,"INSERT INTO "+U.getTable()+"("+Tools.comma_string(fields)+") VALUES("+parseVals(fields,types,values)+");");
    }

    public static String parseVals(String[] fields, String[] types, String[] values) {
        if (fields.length!=types.length || fields.length!=values.length) {
            return "parsing error";
        }
        String[] b = new String[values.length];
        for (int i=0; i<fields.length; i++) {
            if (types[i].equalsIgnoreCase("TEXT") ||
                    types[i].equalsIgnoreCase("VARCHAR") ||
                    types[i].startsWith("varchar") ||
                    types[i].startsWith("VARCHAR")) {
                b[i]="'"+values[i]+"'";
            } else {
                b[i]=values[i];
            }
        }
        return Tools.comma_string(b);
    }

}