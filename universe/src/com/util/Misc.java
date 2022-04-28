package com.util;

import com.db.crud.CRUDObject;

import java.lang.reflect.Field;
import java.util.Arrays;

public class Misc {


    //f(s) -> t
    // want to get A[ a1 a2 .. ] -> A[ f(a1) ... ]

    /*public void stamp(Object[] A) {
        Arrays.stream(A).map(x -> x*2);
    }*/

    private static boolean ignore(String field, String[] ignoreFields){
      //  System.out.println("checking ignore factor for "+field);
        for (String g: ignoreFields) {
      //      System.out.println("... compared to "+g);
            if (field.equals(g)) {
          //      System.out.println("ignored");
                return true;
            }
        }
        return false;
    }
    private static boolean include(String field, String[] includeFields) {
        for (String g: includeFields) {
            //      System.out.println("... compared to "+g);
            if (field.equals(g)) {
                //      System.out.println("ignored");
                return true;
            }
        }
        return false;
    }

    public static Object[] fieldValues_include(Class k, Object o, String[] inc) {
        Field[] fields = k.getDeclaredFields();
        String b = "";
        int size = 0;
        for (Field field : fields) {
            //gives the names of the fields
            if (include(field.getName(),inc)) {
                field.setAccessible(true);
                size++;
            }
        }
        Object[] out = new Object[size];
        int i=0;
        for (Field field:fields) {
            if (include(field.getName(),inc)) {
                field.setAccessible(true);
                try {
                    out[i] = fields[i++].get(o);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return out;
    }

    public static String[] fieldNames_include(Class k, String[] inc) {
        Field[] fields = k.getDeclaredFields();
        String b = "";
        for (Field field : fields) {
            //gives the names of the fields
            if (include(field.getName(),inc)) {
                field.setAccessible(true);
                b += field.getName() + ";;";
            }
        }
        return b.split(";;");
    }

    public static String[] fieldNames_ignore(Class k, String[] ignore) {
        Field[] fields = k.getDeclaredFields();
        String b = "";
        for (Field field : fields) {
            //gives the names of the fields
            if (!ignore(field.getName(),ignore)) {
                field.setAccessible(true);
                b += field.getName() + ";;";
            }
        }
        return b.split(";;");
    }

    public static String[] fieldNames(Object o) {
        Field[] fields = o.getClass().getDeclaredFields();
        String b = "";
        for (Field field : fields) {
            //gives the names of the fields
            field.setAccessible(true);
            System.out.println(field.getName());
            b+=field.getName()+";;";
        }
        return b.split(";;");
    }

    public static String[] fieldNames(Class k) {
        return fieldNames_ignore(k,new String[]{});
    }
}
