package com.db.queries;

import com.db.DatabaseUtility;

/*
    TableExistsQuery
     - run a query to check if a table exists within a database

 */

public class TableExistsQuery extends ServerQuery {


    public TableExistsQuery(DatabaseUtility U) {
        super(U,"SHOW TABLES LIKE '"+U.getTable()+"';");
      //  System.out.println("created table query object!");
    }

    public void done() {
        //System.out.println("finished query!");
        int size = responseSize();
        if (size==1)
            setReturnValue("true");
        else
            setReturnValue("false");

        finish();
    }

    public void finish() {

    }



}
