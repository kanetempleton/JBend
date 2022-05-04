package com.db.crud;

import com.console.Console;
import com.util.Tools;

import java.lang.reflect.Field;
import java.util.function.Function;

public class CRUDObject {

    private CRUDHandler handler;

    private String id;
    private boolean loaded;
    private boolean updated;

    private Function loadFn;


    public CRUDObject(CRUDHandler H, String id) {
        handler = H;
        this.id = id;
        updated = false;
        loaded = false;
        loadFn = null;
    //    sync = new String[]{"key"};
    }

    public CRUDObject(CRUDHandler H) {
        handler = H;
        this.id = "null";
        updated = false;
        loaded = false;
        loadFn = null;
     //   sync = new String[]{"key"};
    }

    private boolean ignore(String field){
        for (String g: handler.ignoreFields()) {
            if (field.equals(g)) {
                return true;
            }
        }
        return false;
    }

    private boolean include(String field){
        for (String g: handler.saveFields()) {
            if (field.equals(g)) {
                return true;
            }
        }
        return false;
    }



    // INTERFACE

    // set the instance variables
    public void setFields(String[] fields, Object[] values) {
        for (int i=0; i<fields.length; i++) {
            if (handler.ignoreMode()) {
                if (!ignore(fields[i]))
                    this.setFieldValue(fields[i], values[i]);
            } else {
                if (include(fields[i]))
                    this.setFieldValue(fields[i], values[i]);
            }
        }
    }


    //save the thing into a database

    //// CRUD METHODS \\\\\

    //Create:
    //INSERT INTO table (new entry)
    public void store() {
        // TODO: CHECK IF ENTRY EXISTS -> ERROR
        // CREATE ENTRY
        handler.create(this);
    }

    //Read:
    public void LOAD() {
        System.out.println("Loaded data to object "+id+"");
        loaded=true;
        load(); //call custom load method lol....... no way
    }

    public void LOAD(Function f) {
        System.out.println("Loaded data to object "+id+"");
        loaded=true;
        loadFn=f;
        load(); //call custom load method lol....... no way
    }

    // SELECT FROM table (load existing entry)
    // override this lmao
    public void load() {
        //System.out.println("X.LOAD()!");
        handler.read(this);
    }

    //MODIFY TABLE (update existing entry)
    public void save() {
        // CHECK IF ENTRY EXISTS -> STORE
        // UPDATE TABLE
        handler.update(this);
    }

    //MODIFY TABLE  (delete this entry from database)
    public void delete() {
        // DELETE FROM TABLE
        handler.delete(this);
    }



    // BACKEND

    public Object getFieldValue(String fieldName) {
        return getFieldValue(this,fieldName);
    }
    public void setFieldValue(String fieldName, Object setValue) {
        if (fieldName.equals("id")) {
            String newid = (String)getFieldValue("id");
            Console.output("overwrite id:"+getID()+"-->"+newid+" from database call.");
            return;
        }
        Console.output("[id="+this.getID()+"] set value of field: "+fieldName+" = "+setValue.toString());
        setFieldValue(this,fieldName,setValue);
        Console.output("[id="+this.getID()+"] testing from object: "+this.getFieldValue(fieldName));
    }

    public Object getField(String name) {
        try {
            Class c = this.getClass();
            Field f = c.getDeclaredField(name);
            return f.get(this);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }



    public static Object getFieldValue(CRUDObject o, String fieldName) {
        try {
            Class crudclass = CRUDObject.class;
            Class curclass = o.getClass();
            Class superclass = o.getClass().getSuperclass();
            Field f = crudclass.getDeclaredField(fieldName);
            f.setAccessible(true);
            return f.get(o);
        } catch (NoSuchFieldException e) {
            try {
                Field f = o.getClass().getDeclaredField(fieldName);
                f.setAccessible(true);
                return f.get(o);
            } catch (Exception e2) {
                e2.printStackTrace();
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    // this might have to just be outdated and unused
    public static void setFieldValue(CRUDObject o, String fieldName, Object setValue) {
        try {
            Class crudclass = CRUDObject.class;
            Class curclass = o.getClass();
            Class superclass = o.getClass().getSuperclass();
            Field f = curclass.getDeclaredField(fieldName);

            f.setAccessible(true);
            f.set(o,setValue);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //
    public String[] fieldNames() {
        return handler.fieldNames();
        /*
        Field[] fields = CRUDObject.class.getDeclaredFields();
        String b = "";
        for (Field field : fields) {
            //gives the names of the fields
            if (handler.ignoreMode()) {
                if (!ignore(field.getName())) {
                    field.setAccessible(true);
                    b += field.getName() + ";;";
                }
            } else {
                if (include(field.getName())) {
                    field.setAccessible(true);
                    b += field.getName() + ";;";
                }
            }
        }
        return b.split(";;");

         */
    }

    public String[] fieldTypes() {
        return handler.fieldTypes();
        /*
        Field[] fields = CRUDObject.class.getDeclaredFields();
        String b = "";
        for (Field field : fields) {
            //gives the names of the fields
            if (handler.ignoreMode()) {
                if (!ignore(field.getName())) {
                    field.setAccessible(true);
                    b += handler.getSQLTypeForField(field.getName()) + ";;";
                }
            } else {
                if (include(field.getName())) {
                    field.setAccessible(true);
                    b += handler.getSQLTypeForField(field.getName()) + ";;";
                }
            }
        }
        return b.split(";;");*/
    }

    public Object[] fieldValues() {
        return handler.fieldValues(this);
        /*
        Field[] fields = CRUDObject.class.getDeclaredFields();
        String b = "";
        int size = 0;
        for (Field field : fields) {
            //gives the names of the fields
            if (handler.ignoreMode()) {
                if (!ignore(field.getName())) {
                    field.setAccessible(true);
                    size++;
                    //b += field.getName() + ";;";
                }
            } else {
                if (include(field.getName())) {
                    field.setAccessible(true);
                    size++;
                    //b += field.getName() + ";;";
                }
            }
        }
        Object[] out = new Object[size];
        int i=0;
        for (Field field : fields) {
            //gives the names of the fields
            if (handler.ignoreMode()) {
                if (!ignore(field.getName())) {
                    field.setAccessible(true);
                    try {
                        out[i++] = field.get(this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                   // size++;
                    //b += field.getName() + ";;";
                }
            } else {
                if (include(field.getName())) {
                    field.setAccessible(true);
                   // size++;
                    //b += field.getName() + ";;";
                    try {
                        out[i++] = field.get(this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
       // System.out.println("computed field values here they are\n"+ Tools.string(out));
        return out;
        */
    }

    public String[] fieldNames_include_all() {
        Field[] fields = CRUDObject.class.getDeclaredFields();
        String b = "";
        for (Field field : fields) {
            //gives the names of the fields
                field.setAccessible(true);
                b += field.getName() + ";;";
        }
        return b.split(";;");
    }

    public static void testCRUDObject() {

      /*  System.out.println("testing from CRUDObject class...");
        CRUDHandler tasks = new CRUDHandler("tasks","id");
       // CRUDObject o = new CRUDObject(tasks);
      //  System.out.println("direct access: "+o.objectName+","+o.objectID);
      //  o.objectID = 420;
      //  System.out.println("direct access: "+o.objectName+","+o.objectID);
        for (String x: o.fieldNames()) {
            System.out.println(x+": "+o.getFieldValue(x));
        }

        o.setFieldValue("objectID",777);
        System.out.println("changed object ID to 777");
        System.out.println("objectID: "+o.getFieldValue("objectID"));*/


    }

    /*
    String propertyName = "foo";
    yourClass.getClass().getSuperClass().getDeclaredField(propertyName);
     */


    public boolean isUpdated() {return updated;}
    public void setUpdated(boolean b){updated = b;}
    public String getKey() {return id;}
    public void setKey(String k) {id=k;}

    public void setID(String id) {
        this.id = id;
    }
    public String getID() {return id;}
    public boolean loaded() {return loaded;}


    /*public String[] syncedFields() {return sync;}
    public void addSyncedFields(String[] fldz) {
        String[] out = new String[fldz.length+1];
        int i=0;
        out[i++] = sync[0];
        for (String f: fldz) {
            out[i++]=f;
        }
        sync = new String[out.length];
        for (int j=0; j<out.length; j++)
            sync[j]=out[j];
    }*/
}


