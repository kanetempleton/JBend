package com.db.queries;

import com.db.DatabaseUtility;
import com.db.ServerQuery;

public class DeleteTableQuery extends ServerQuery {

    public DeleteTableQuery(DatabaseUtility U) {
        super(U,"DROP TABLE "+U.getTable()+";");
    }
}
