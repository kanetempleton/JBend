package com.db.crud;

import com.console.Console;
import com.db.DatabaseUtility;
import com.db.ServerQuery;
import com.db.queries.*;
import com.util.Misc;
import com.util.Tools;

import java.util.ArrayList;

public class CRUDHandler<X extends CRUDObject> extends DatabaseUtility implements Runnable {

    public static final String PRIMARY_KEY_STANDARD_TYPE = "VARCHAR(63)";
    public static final String PRIMARY_KEY_STANDARD_NAME = "id";
    public static final String STANDARD_SQL_FIELD_TYPE = "TEXT";

    private String primaryKey; //field name of the identifying column
    private ArrayList<CRUDObject> objectList; //active objects
    private String ignoreFields[]; //names of instance variables to ignore when computing database structure
    private String savedFields[]; //names of instance variables to save to database
    private boolean ignore_mode;

    public CRUDHandler(String t, String p, boolean ig) {
        setTable(t);
        primaryKey=p;
        objectList = new ArrayList<>();
        ignoreFields = new String[]{"default"};
        savedFields = new String[]{PRIMARY_KEY_STANDARD_NAME};
        ignore_mode = ig;
    }

    public CRUDHandler(String t, String p) {
        setTable(t);
        primaryKey=p;
        objectList = new ArrayList<>();
        ignoreFields = new String[]{"default"};
        savedFields = new String[]{PRIMARY_KEY_STANDARD_NAME};
        ignore_mode = false;
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
                    //table exists, check structure
                    checkTableStructure();
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

    //
    private void checkTableStructure() {
        System.out.println("Checking table structure for "+getTable()+"...");

        new ColumnInfoQuery(this,"both") {
            public void done() {
                System.out.println("finished col names query");
                String b = "";
                String c = "";
                for (String[] x: response_getValues()) {
                    b+=x[0]+";:;";
                    c+=x[1]+";:;";
                }
                String[] colNames = b.split(";:;");
                String[] colTypes = c.split(";:;");
                //System.out.println("the resulting structure is:\n"+structure_string(colNames,colTypes,true));
                String classStructure = computeTableStructure();
                String dbStructure = structure_string(colNames,colTypes,true);
                int[][] compare = compare_structures(classStructure,dbStructure);
                if (structures_match(compare)) {
                    System.out.println("database structure matches! ALL GOOD!!");
                } else {
                    if (!structures_sameSize(compare)) { //added or deleted fields, main work here
                        int x = compare[0][0];
                        if (x > 0) {
                            System.out.println("[UNHANDLED] size mismatch... Class has " + x + " more fields than database.");
                        }
                        else {
                            System.out.println("[UNHANDLED] size mismatch... Class has " + (-1 * x) + " fewer fields than database.");
                        }
                    } else { //name or type mismatch, somewhat minor
                        if (!structures_namesMatch(compare)) {
                            System.out.println("[UNHANDLED] name mismatch... at columns: "+Tools.string(compare[1]));
                        }
                        if (!structures_typesMatch(compare)) {
                            System.out.println("[UNHANDLED] type mismatch... at columns: "+Tools.string(compare[2]));
                        }
                    }
                }
            }
        };
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
    public String structure_string(String[] names, String[] types, boolean pkey) {
        String b = "(";
        if (pkey)
            b = "("+PRIMARY_KEY_STANDARD_NAME+" "+PRIMARY_KEY_STANDARD_TYPE+"";
        for (int i = 0; i < names.length; i++)
            if (!pkey || (pkey && !names[i].equals(PRIMARY_KEY_STANDARD_NAME)))
                b += ", " + names[i] + " " + types[i] + "";
        return b+")";
    }
    public String structure_string(String[] names, boolean pkey) {
        String b = "(";
        if (pkey)
            b = "("+PRIMARY_KEY_STANDARD_NAME+" "+PRIMARY_KEY_STANDARD_TYPE+"";
        for (int i = 0; i < names.length; i++)
            b += ", " + names[i] + " " + getSQLTypeForField(names[i]) + "";
        return b+")";
    }
    public String[] extractNamesFromStructure(String s) {
        String b = "";
        String[] x = s.substring(1,s.length()-1).replace(", ",",").split(",");
        for (String y: x) {
            b+= y.split(" ")[0]+";:;";
        }
        return b.split(";:;");
    }
    public String[] extractTypesFromStructure(String s) {
        String b = "";
        String[] x = s.substring(1,s.length()-1).replace(", ",",").split(",");
        for (String y: x) {
            b+= y.split(" ")[1]+";:;";
        }
        return b.split(";:;");
    }

    //out[0] = mismatch in length
    //out[1] = indices of name mismatches
    //out[2] = indices of type mismatches
    public int[][] compare_structures(String a, String b) {
        System.out.println("comparing structures...");
        System.out.println("class structure: "+a);
        System.out.println("DB structure: "+b);
        int[][] out = new int[][]{{0},{-1},{-1}};
        String[] n1 = extractNamesFromStructure(a);
        String[] n2 = extractNamesFromStructure(b);
        String[] t1 = extractTypesFromStructure(a);
        String[] t2 = extractTypesFromStructure(b);
        System.out.println("class[n]: "+Tools.string(n1));
        System.out.println("class[t]: "+Tools.string(t1));
        System.out.println("DB[n]: "+Tools.string(n2));
        System.out.println("DB[t]: "+Tools.string(t2));
        if (n1.length!=n2.length)
            /*Tools.append(out[0],n1.length-n2.length);*/ out[0][0] = n1.length-n2.length;
        else if (t1.length!=t2.length)
           /* Tools.append(out[0],t1.length-t2.length);*/ out[0][0] = t1.length-t2.length;
           if (out[0][0]!=0)
               return out;

        for (int i=n1.length-1; i>=0; i--) {
            if (!n1[i].equals(n2[i]))
                /*Tools.append(out[1],i);*/ out[1][0] = i;
            if (!t1[i].equalsIgnoreCase(t2[i]))
                /*Tools.append(out[2],i);*/ out[2][0] = i;
        }
        System.out.println("comparison structure:\n"+Tools.string(out));
        return out;
    }
    private boolean structures_sameSize(int[][] compare) {
        return compare[0][0] == 0;
    }
    private boolean structures_namesMatch(int[][] compare) {
        return compare[1][0] == -1 && compare[1].length==1;
    }
    private boolean structures_typesMatch(int[][] compare) {
        return compare[2][0] == -1 && compare[2].length==1;
    }
    private boolean structures_match(int[][] compare) {
       // int[][] compare = compare_structures(a,b);
        return (structures_sameSize(compare) && structures_namesMatch(compare) && structures_typesMatch(compare));
    }
    public String computeTableStructure() {
        System.out.println("computing table structure!!!");
        if (classtype==null) {
            System.out.println("class type has not been assigned for this handler.");
            return "null";
        }
        String[] names = Misc.fieldNames(classtype,ignoreFields);//dummy.fieldNames();
        String out = "("+PRIMARY_KEY_STANDARD_NAME+" "+PRIMARY_KEY_STANDARD_TYPE+"";
        if (!ignore_mode) {
            for (int i = 0; i < savedFields.length; i++)
                out += ", " + savedFields[i] + " " + getSQLTypeForField(names[i]) + "";
        } else {
            for (int i = 0; i < names.length; i++)
                out += ", " + names[i] + " " + getSQLTypeForField(names[i]) + "";
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



    public String[] saveFields() {
        return savedFields;
    }

    public void setSaveFields(String[] ig) {
        savedFields = new String[ig.length];
        for (int i=0; i<ig.length; i++) {
            savedFields[i] = ig[i];
        }
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

    public boolean ignoreMode(){return ignore_mode;}
    public void setIgnoreMode(boolean b){ignore_mode=b;}

}
