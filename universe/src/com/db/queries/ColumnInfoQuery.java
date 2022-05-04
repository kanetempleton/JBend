package com.db.queries;

import com.db.DatabaseUtility;

public class ColumnInfoQuery extends ServerQuery {

    public ColumnInfoQuery(DatabaseUtility U, String function, boolean log) {
       // super(U,"SELECT "+translate(function)+" FROM information_schema.columns WHERE table_name='"+U.getTable()+"';");
        super(U,"SHOW COLUMNS FROM "+U.getTable()+";",log);
    }

    public ColumnInfoQuery(DatabaseUtility U, String function) {
        // super(U,"SELECT "+translate(function)+" FROM information_schema.columns WHERE table_name='"+U.getTable()+"';");
        super(U,"SHOW COLUMNS FROM "+U.getTable()+";");
    }

    public void done() {
        String[][] r = this.response_getValues();
        String b = "";
        String c = "";
        for (int i=0; i<r.length; i++) {
            b += responseParamValue(i,"Field")+";:;";
            c += responseParamValue(i,"Type")+";:;";
        }
        setReturnValue(b+";;;;"+c);
      //  System.out.println("test this!!!\n"+ Tools.string(b.split(";:;")));
      //  System.out.println("test this!!!\n"+ Tools.string(c.split(";:;")));
        finish();
    }

    //override this
    public void finish() {

    }

    private static String translate(String f) {
        if (f.equalsIgnoreCase("names") || f.equalsIgnoreCase("name"))
            return "column_name";
        if (f.equalsIgnoreCase("types") || f.equalsIgnoreCase("data") || f.equalsIgnoreCase("data_types"))
            return "data_type";
        if (f.equalsIgnoreCase("both"))
            return "column_name,data_type";
        return "column_name";
    }
}
