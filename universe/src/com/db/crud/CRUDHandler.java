package com.db.crud;

import com.console.Console;
import com.db.DatabaseUtility;
import com.db.ServerQuery;
import com.db.queries.CreateTableQuery;
import com.db.queries.DeleteTableQuery;
import com.db.queries.TableExistsQuery;
import com.util.Misc;

import java.util.ArrayList;

public class CRUDHandler<X extends CRUDObject> extends DatabaseUtility implements Runnable {

    public static final String PRIMARY_KEY_STANDARD_TYPE = "VARCHAR(63)";
    public static final String PRIMARY_KEY_STANDARD_NAME = "id";
    public static final String STANDARD_SQL_FIELD_TYPE = "TEXT";

    private String primaryKey; //field name of the identifying column
    private ArrayList<CRUDObject> objectList; //active objects
    private String ignoreFields[]; //names of instance variables to ignore when computing database structure

    public CRUDHandler(String t, String p) {
        setTable(t);
        primaryKey=p;
        objectList = new ArrayList<>();
        ignoreFields = new String[]{"default"};
    }

    /*public String[] syncedFields() {
        return new
    }*/

    public void start() {
        initTable();
    }

    public void initTable() {
        System.out.println("Initializing table "+getTable()+"...");
        checkTableExistence();
       // checkTableStructure();
        System.out.println("Initialization complete!");
    }

    //TODO: get table structure

    private void checkTableExistence() {
        System.out.println("Checking table existence for "+getTable()+"...");

        ServerQuery q = new TableExistsQuery(this) {
            public void finish() {
                if (this.getReturnValue().equals("true")) {
                    //table exists, do nothing
                } else if(this.getReturnValue().equals("false")) {
                    CreateTableQuery q2 = new CreateTableQuery(this.getUtil());
                } else {
                    System.out.println("the return value was null.");
                }
            }
        };

     /*   new ServerQuery(this,"SHOW TABLES LIKE \""+table+"\"") {
            public void done() {
                if (this.responseSize()==0) { //table doesn't exist so create it
                    String query = "CREATE TABLE "+table+"("+primaryKey+" VARCHAR(64) PRIMARY KEY,";
                    new ServerQuery(this.util(),"CREATE TABLE "+table+"("+primaryKey+" VARCHAR(64) PRIMARY KEY, title TEXT, customerName TEXT, customerPhone TEXT, customerEmail TEXT, info TEXT, status TEXT, dueDate TEXT)") {
                        public void done() {
                            System.out.println("Successfully initialized database table: tickets");
                        }
                    };
                } else { //table does exist so check its structure
                    checkTableStructure();
                }
            }
        };*/
    }
    private void checkTableStructure() {
        System.out.println("Checking table structure for "+getTable()+"...");
    }

    //delete the database table lmaooooooo
    public void drop() {
        System.out.println("DELETING TABLE! "+getTable());
        new DeleteTableQuery(this);
    }

    //not too sure about this one!
    public void load(String identifier) {
        //select * from table where id='crud_id'
        //new CRUDObject(this,fields,values)
        new ServerQuery(this,"SELECT * FROM "+getTable()+" WHERE "+primaryKey+"='"+identifier+"';") {
            public void done() {
                if (this.responseSize()==1) { //this should be the case...
                    CRUDObject x = new CRUDObject((CRUDHandler) this.getUtil());
                    String[] fields = this.response_getFields();
                    String[] values = this.response_getValues()[0];
                    x.setFields(fields,values);
                }
            }
        };
    }


    public void serverAction(ServerQuery Q) {

    }

    public void run() {
        Console.output("starting CRUD handler for "+getTable());
        start();
    }


    /// methodszzz


    // create a new database-sync'd object
    // should generate a blank-slate object
    // that is ready to be stored in database
    // note: args[0] should be the primary key
    public CRUDObject create(String[] args) {
        CRUDObject o = new CRUDObject(this,args[0]);
        // store o in database
        return o;
    }


    public String queryTableStructure() {
        return "structure from select";
    }
    public String computeTableStructure() {
        System.out.println("computing table structure!!!");
        if (classtype==null) {
            System.out.println("class type has not been assigned for this handler.");
            return "null";
        }
        String[] names = Misc.fieldNames(classtype,ignoreFields);//dummy.fieldNames();
        String out = "("+PRIMARY_KEY_STANDARD_NAME+" "+PRIMARY_KEY_STANDARD_TYPE+"";
        for (int i=0; i<names.length; i++) {
            //if (i==names.length-1) {
             //   out+=", "+names[i]+" "+getSQLTypeForField(names[i])+")"
            //} else {
                out+=", "+names[i]+" "+getSQLTypeForField(names[i])+"";
            //}
        }
        out+=")";
        return out;
    }
    public String getSQLTypeForField(String field) {
        return STANDARD_SQL_FIELD_TYPE;
    }

    private Class<X> classtype = null;

    public void assignClass(Class<X> t) {
        classtype = t;
        System.out.println("assigned class type : "+classtype.getName());
    }
   // public static String


    public static void testCRUDHandler() {
        CRUDHandler taskHandler = new CRUDHandler("tasks","id");
        taskHandler.start();

        //test creating for database



        //test loading from database

        //
    }

    public String[] ignoreFields() {
        return ignoreFields;
    }
    public void setIgnoreFields(String[] ig) {
        ignoreFields = new String[ig.length];
        for (int i=0; i<ig.length; i++) {
            ignoreFields[i] = ig[i];
        }
    }

}
