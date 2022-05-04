package com.db.queries;

import com.db.DatabaseUtility;
import com.db.crud.CRUDHandler;

public class CreateTableQuery extends ServerQuery {

    public CreateTableQuery(DatabaseUtility U, String fields) {
        super(U,"CREATE TABLE IF NOT EXISTS "+U.getTable()+"("+fields+");");
    }

    public CreateTableQuery(DatabaseUtility U, CRUDHandler H) {
        super(U,"CREATE TABLE IF NOT EXISTS "+U.getTable()+""+H.computeTableStructure()+";");
    }

    public CreateTableQuery(DatabaseUtility U) {
        super(U,"CREATE TABLE IF NOT EXISTS "+U.getTable()+""+((CRUDHandler)U).computeTableDeclaration()+";");
    }
}
