package com.db.queries;

import com.db.DatabaseUtility;
import com.db.ServerQuery;

public class AlterColumnTypeQuery extends ServerQuery {

    public AlterColumnTypeQuery(DatabaseUtility U, String colName, String colType) {
        super(U,"ALTER TABLE "+U.getTable()+" MODIFY "+colName+" "+colType+";");
    }

}