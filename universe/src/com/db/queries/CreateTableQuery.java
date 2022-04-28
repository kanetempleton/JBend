package com.db.queries;

import com.db.DatabaseUtility;
import com.db.ServerQuery;
import com.db.crud.CRUDHandler;
import com.db.crud.CRUDObject;

public class CreateTableQuery extends ServerQuery {

    public CreateTableQuery(DatabaseUtility U, String fields) {
        super(U,"CREATE TABLE IF NOT EXISTS "+U.getTable()+"("+fields+");");
    }

    public CreateTableQuery(DatabaseUtility U, CRUDHandler H) {
        super(U,"CREATE TABLE IF NOT EXISTS "+U.getTable()+""+H.computeTableStructure()+";");
    }

    public CreateTableQuery(DatabaseUtility U) {
        super(U,"CREATE TABLE IF NOT EXISTS "+U.getTable()+""+((CRUDHandler)U).computeTableStructure()+";");
    }
}
