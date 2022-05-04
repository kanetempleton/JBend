package com.db.queries;

import com.db.DatabaseUtility;
import com.util.Tools;

public class UpdateQuery extends ServerQuery {


    public UpdateQuery(DatabaseUtility U, String[] setFields, String[] setValues, String[] condFields, String[] condVals) {
        super(U, "UPDATE " + U.getTable() + " SET " + setstring(setFields,setValues) + " WHERE " + condstring(condFields,condVals) + ";");
    }

    private static String setstring(String[] setFields, String[] setValues) {
        return Tools.comma_string(setFields,setValues,"=");
    }

    //TODO: this only does equality conditions
    // needs gte, lte, etc
    /* */
    //TODO: the condition values are are always taken as text with the 'values' marks around it
    private static String condstring(String[] condF, String[] condV) {
        return Tools.comma_string(condF,condV,"=");
    }
}