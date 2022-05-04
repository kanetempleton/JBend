package com.db.crud;

import com.Main;
import com.console.Console;
import com.db.DatabaseUtility;
import com.db.ServerQuery;
import com.db.queries.*;
import com.util.Misc;
import com.util.Tools;

import java.util.ArrayList;
import java.util.Arrays;

/* IMPORTANT!!!
 * usage of CRUDHandler classes is implied that it will be running on a Launcher thread
 * you MUST call Main.launcher.nextStage() once the table init phase is complete
 * i mean.. i'm just gonna code that here now so it probably doesn't really matter
 * but since i'm doing that, every CRUDHandler will automatically
 * trigger the Launcher to start its next thread
 */

public class CRUDHandler<X extends CRUDObject> extends DatabaseUtility implements Runnable {

    public static boolean DEBUG_CRUD = false;

    public static final String PRIMARY_KEY_STANDARD_TYPE = "VARCHAR(63)";
    public static final String PRIMARY_KEY_STANDARD_NAME = "id";
    public static final String STANDARD_SQL_FIELD_TYPE = "VARCHAR(2048)";
    public static final String DEFAULT_NULL_VALUE = "UNASSIGNED";

    private String primaryKey; //field name of the identifying column
    private ArrayList<CRUDObject> objectList; //active objects
    private String ignoreFields[]; //names of instance variables to ignore when computing database structure
    private String savedFields[]; //names of instance variables to save to database
    private boolean ignore_mode;

    private boolean ready;

    public CRUDHandler(String t, String p, boolean ig) {
        setTable(t);
        primaryKey=p;
        objectList = new ArrayList<>();
        ignoreFields = new String[]{"default"};
        savedFields = new String[]{PRIMARY_KEY_STANDARD_NAME};
        ignore_mode = ig;
        ready = false;
    }

    public CRUDHandler(String t, String p) {
        setTable(t);
        primaryKey=p;
        objectList = new ArrayList<>();
        ignoreFields = new String[]{"default"};
        savedFields = new String[]{PRIMARY_KEY_STANDARD_NAME};
        ignore_mode = false;
        ready = false;
    }

    /*public String[] syncedFields() {
        return new
    }*/

    //hmmmmmmmmmmmmm.......... no fricking way.
    //do i need to make sure this stays running?
    //... yeahhhhh... i totally do -_-
    //fuck this im going home
    public void go() {
        initTable();
    }

    public void initTable() {
        System.out.println(tag()+"Initializing table...");
        checkTableExistence();
    }

    //TODO: get table structure

    private void checkTableExistence() {
      //  System.out.println("Checking table existence for "+getTable()+"...");

        ServerQuery q = new TableExistsQuery(this) {
            public void finish() {
                if (this.getReturnValue().equals("true")) {
                    //table exists, check structure
                    checkTableStructure();
                } else if(this.getReturnValue().equals("false")) {
                    CreateTableQuery q2 = new CreateTableQuery(this.getUtil()) {
                        public void done() {
                            System.out.println(tag()+"Created database table: "+getTable());
                            finishInitPhase();
                        }
                    };
                } else {
                    System.out.println(tag()+"the return value was null.");
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
        //System.out.println("Checking table structure for "+getTable()+"...");

        new ColumnInfoQuery(this,"both") {
            public void finish() {
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
                String[] class_names = extractNamesFromStructure(classStructure);
                String[] class_types = extractTypesFromStructure(classStructure);
                String dbStructure = structure_string(colNames,colTypes,true);
                int[][] compare = compare_structures(classStructure,dbStructure);
                if (structures_match(compare)) {
                    System.out.println(tag()+"database structure matches! ALL GOOD!!");
                    finishInitPhase();
                } else {
                    if (!structures_sameSize(compare)) { //added or deleted fields, main work here
                        int x = compare[0][0];
                        if (x > 0) {
                            //TODO: this needs more checking
                            // -> same # of fields should also check that each field is same
                            System.out.println(tag()+"size mismatch... Class has " + x + " more fields than database.");
                            //add fields from class to database table
                            //Table.structure.append(F \ col); <--- need this function!
                            Object[] xfields = Tools.subtract(class_names,colNames);
                            String[] queries = new String[xfields.length];
                            for (int i=0; i<xfields.length; i++) {
                                queries[i] = xfields[i]+" "+STANDARD_SQL_FIELD_TYPE;
                            }
                            System.out.println(tag()+" the differing fields are: "+Tools.string(xfields));
                            new AddColumnQuery(this.getUtil(),queries,"'"+DEFAULT_NULL_VALUE+"'") {
                                public void done() {
                                    finishInitPhase();
                                }
                            };

                            //new AddColumnQuery(this,)
                        }
                        else {
                            System.out.println(tag()+" size mismatch... Class has " + (-1 * x) + " fewer fields than database.");
                            //drop extra fields from table
                            //Table.structure.drop(col \ F);
                            String[] dropCols = Tools.string_array(Tools.subtract(class_names,colNames));
                            System.out.println(tag()+" the differing fields are: "+Tools.string(Tools.subtract(class_names,colNames)));

                            new DropColumnQuery(this.getUtil(),dropCols) {
                              public void done() {
                                  finishInitPhase();
                              }
                            };

                        }
                    } else { //name or type mismatch, somewhat minor
                        //TODO: unsure how to handle these mismatches
                        // -> prompt the user with the issue and options for fixing...
                        if (!structures_namesMatch(compare)) {
                            System.out.println("[UNHANDLED] name mismatch... at columns: "+Tools.string(compare[1]));
                            finishInitPhase();
                        }
                        else if (!structures_typesMatch(compare)) {
                            System.out.println("[UNHANDLED] type mismatch... at columns: "+Tools.string(compare[2]));
                            finishInitPhase();
                        }
                    }
                }
            }
        };
    }

    public void start() {
        System.out.println("CRUD HANDLER RUNNING!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
    }

    private void finishInitPhase() {
        Console.output(tag()+" Table initialization complete!");
        Main.launcher.nextStage();
        ready = true;
        start();
    }

    //delete the table lmaooooooo
    public void drop() {
        System.out.println(tag()+"DELETING TABLE!!!!!!!!!!!!!");
        new DeleteTableQuery(this);
    }



    public void serverAction(ServerQuery Q) {

    }

    public void run() {
        Console.output("starting CRUD handler for "+getTable());
        go();
    }


    /// object-specific methods

    public String[] fieldNames() {
        if (!ignoreMode()) {
            return Tools.join("id",Tools.newInstance(Misc.fieldNames_include(classtype,savedFields)));
        }
        else
            return Tools.newInstance(Misc.fieldNames_ignore(classtype,ignoreFields));
    }

    public String[] fieldValues(CRUDObject x) {
        if (!ignoreMode())
            return Tools.join(x.getID(),Tools.string_array(Misc.fieldValues_include(classtype,x,savedFields)));
        else
            return Tools.string_array(Misc.fieldValues_include(classtype,x,savedFields)); //TODO
    }

    public String[] fieldTypes() {
        return Tools.string_array(Arrays.stream(Tools.newInstance(fieldNames())).map(this::getSQLTypeForField).toArray());
    }


    ///// CRUD METHODS \\\\\

    // add entry x into databas
    public void create(CRUDObject x) {
        if (!ready) {
            Console.output("ERROR: tried to perform CRUD on an object before table "+getTable()+" was initialized.");
            return;
        }
        if (x.getID().equals("null")) {
            Console.output("ERROR: tried to insert an object with unassigned ID to table "+getTable());
            return;
        }
        //TODO: add option for ignore mode
        String[] names = Misc.fieldNames_include(classtype,savedFields);
        String[] vals = Tools.string_array(Misc.fieldValues_include(classtype,x,savedFields));
        String[] types = Tools.string_array(Arrays.stream(names).map(this::getSQLTypeForField).toArray());


        System.out.println("fields = "+Tools.comma_string(names));
        System.out.println("types = "+Tools.comma_string(types));
        System.out.println("values = "+Tools.comma_string(vals));




        System.out.println("x.fields = "+Tools.string(x.fieldNames()));
        System.out.println("x.types = "+Tools.string(x.fieldTypes()));
        System.out.println("x.values = "+Tools.string(x.fieldValues()));


        new InsertEntryQuery(this,x.fieldNames(),x.fieldTypes(),Tools.string_array(x.fieldValues()));
    }


    //save data from database into object X
    //TODO: fix threading issue
    //TODO: handle nonexistent queries
    public void read(CRUDObject O) {
        Console.output("Reading into X...");
        new SelectQuery(this,"id","'"+O.getID()+"'") {
            public void done() {
                for (String f: this.response_getFields()) {
                    if (this.responseSize() == 0) {
                        System.out.println("LOADING ERROR. No object with the id "+O.getID()+" was found.");
                        return;
                    }
                   // X.setFieldValue(f,this.responseParamValue(0,f));
                    if (!f.equals("id"))
                        Misc.setField(classtype,O,f,this.responseParamValue(0,f));
                    else
                        O.setID(this.responseParamValue(0,f));
                }
                O.LOAD();

            }

        };
    }

    public void load(String id, CRUDObject O) {
        new SelectQuery(this,"id","'"+id+"'") {
            public void done() {
                for (String f: this.response_getFields()) {
                    if (this.responseSize() == 0) {
                        System.out.println("LOADING ERROR. No object with the id "+id+" was found.");
                        return;
                    }
                    // X.setFieldValue(f,this.responseParamValue(0,f));
                    if (!f.equals("id"))
                        Misc.setField(classtype,O,f,this.responseParamValue(0,f));
                    else
                        O.setID(this.responseParamValue(0,f));
                }
                O.LOAD();

            }

        };
    }

    public X load(String id) {
        X x = null;
        new SelectQuery(this,"id","'"+id+"'") {
            public void done() {
                if (this.responseSize() == 0) {
                    System.out.println("LOADING ERROR. No object with the id "+id+" was found.");
                    return;
                }
                for (String f: this.response_getFields()) {
                    // X.setFieldValue(f,this.responseParamValue(0,f));
                    if (!f.equals("id"))
                        Misc.setField(classtype,x,f,this.responseParamValue(0,f));
                    else
                        x.setID(this.responseParamValue(0,f));
                }
                x.LOAD();

            }

        };
        return x;
    }


    //this is prob what we should doo
    public void update(CRUDObject X) {
        Console.output("updating object "+X.getID()+"");
        new UpdateQuery(this,X.fieldNames(),Tools.string_array(X.fieldValues()),Tools.A("id"),Tools.A(X.getID())) {
            public void done() {
                Console.output("the update was a success.");
            }
        };
    }

    public void delete(CRUDObject X) {
        Console.output("deleting record of object "+X.getID()+"...");
        new DeleteQuery(this,Tools.A("id"),Tools.A(X.getID())) {
            public void done() {
                Console.output("the deletion was a success.");
            }
        };
    }

    public void delete(String id) {
        Console.output("deleting record of object "+id+"...");
        new DeleteQuery(this,Tools.A("id"),Tools.A(id)) {
            public void done() {
                Console.output("the deletion was a success.");
            }
        };
    }


    //// END CRUD \\\\\\\

    /*

        select <cols> from <table> where <field> = <value>

        select(cols, field, value)


     */


    // create a new database-sync'd object
    // should generate a blank-slate object
    // that is ready to be stored in database
    // note: args[0] should be the primary key
    /*
    public CRUDObject create(String[] args) {

        CRUDObject o = new CRUDObject(this,args[0]);
        // store o in database
        return o;
    }

     */


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
      //  System.out.println("comparing structures...");
        debug("class structure: "+a);
        debug("DB structure: "+b);
        int[][] out = new int[][]{{0},{-1},{-1}};
        String[] n1 = extractNamesFromStructure(a);
        String[] n2 = extractNamesFromStructure(b);
        String[] t1 = extractTypesFromStructure(a);
        String[] t2 = extractTypesFromStructure(b);
        debug("class[n]: "+Tools.string(n1));
        debug("class[t]: "+Tools.string(t1));
        debug("DB[n]: "+Tools.string(n2));
        debug("DB[t]: "+Tools.string(t2));
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
        //System.out.println("comparison structure:\n"+Tools.string(out));
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
       // System.out.println("computing table structure!!!");
        if (classtype==null) {
            System.out.println(tag()+"class type has not been assigned for this handler.");
            return "null";
        }
        //TODO: add ignore mode option
        // - but i think i already roughly handled this...
        String[] names = Misc.fieldNames_ignore(classtype,ignoreFields);//dummy.fieldNames();
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

    public String computeTableDeclaration() {
        // System.out.println("computing table structure!!!");
        if (classtype==null) {
            System.out.println(tag()+"class type has not been assigned for this handler.");
            return "null";
        }
        //TODO: add ignore mode option
        // - but i think i already roughly handled this...
        String[] names = Misc.fieldNames_ignore(classtype,ignoreFields);//dummy.fieldNames();
        String out = "("+PRIMARY_KEY_STANDARD_NAME+" "+PRIMARY_KEY_STANDARD_TYPE+" PRIMARY KEY UNIQUE";
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
        Console.output("assigned class type : "+classtype.getName());
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

    private String tag() {
        return "[SCHEMA:"+this.getTable()+"] ";
    }

    private void debug(String x) {
        if (DEBUG_CRUD)
            Console.output("[DEBUG] "+x);
    }

    public boolean ready(){return ready;}

}
