package com.db.queries;

import com.db.DatabaseUtility;
import com.db.ServerQuery;
import com.util.Tools;

public class DeleteQuery extends ServerQuery {

    public DeleteQuery(DatabaseUtility U, String[] condFields, String[] condVals) {
        super(U,"DELETE FROM "+U.getTable()+" WHERE "+ Tools.comma_string(condFields,condVals,"=")+";");
    }
}
