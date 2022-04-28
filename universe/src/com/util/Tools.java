package com.util;

import java.util.ArrayList;

public class Tools {


    //also doesn't work bro wtf
    public static Object[] subtract(Object[] A, Object[] B) {
        System.out.println("subtracting "+string(A)+" from "+string(B));
        int count = A.length+B.length;
        for (Object a : A) {
            if (contains(B,a))
                count--;
        }
        for (Object b: B) {
            if (contains(A,b))
                count--;
        }
        Object[] out = new Object[count];
        int i=0;
        for (Object a: A) {
            if (!contains(B,a))
                out[i++] = a;
        }
        for (Object b: B) {
            if (!contains(A,b))
                out[i++]=b;
        }
        return out;
    }

    public static boolean contains(Object[] A, Object B) {
        for (Object a: A) {
            if (a.equals(B))
                return true;
        }
        return false;
    }

    //TODO: maybe doesn't work
    public static void append(Object[] A, Object B) {
        Object[] C = new Object[A.length+1];
        for (int i=0; i<C.length; i++) {
            if (i==C.length-1)
                C[i] = B;
            else
                C[i] = A[i];
        }
        A = new Object[C.length];
        for (int i=0; i<C.length; i++) {
            A[i] = C[i];
        }
    }

    //TODO: doesn't work
    public static void append(int[][] A, int B, int row) {
        append(A[row],B);
    }

    public static void append(int[] A, int B) {
        int[] C = new int[A.length+1];
        for (int i=0; i<C.length; i++) {
            if (i==C.length-1)
                C[i] = B;
            else
                C[i] = A[i];
        }
        A = new int[C.length];
        for (int i=0; i<C.length; i++) {
            A[i] = C[i];
        }
    }

    public static void append(Object[][] A, Object B, int row) {
        append(A[row],B);
    }


    public static String string(Object[] A) {
        String b = "[ ";
        for (int i=0; i<A.length; i++) {
            if (i!=A.length-1)
                b+=A[i]+", ";
            else
                b+=A[i]+" ]";
        }
        return b;
    }

    public static String string(Object[][] A) {
        String b = "{ ";
        for (int i=0; i<A.length; i++) {
            if (i!=A.length-1)
                b+=string(A[i])+"\n";
            else
                b+=string(A[i])+" }";
        }

        return b;
    }

    public static String string(int[] A) {
        String b = "[ ";
        for (int i=0; i<A.length; i++) {
            if (i!=A.length-1)
                b+=A[i]+", ";
            else
                b+=A[i]+" ]";
        }
        return b;
    }

    public static String string(int[][] A) {
        String b = "{ ";
        for (int i=0; i<A.length; i++) {
            if (i!=A.length-1)
                b+=string(A[i])+"\n";
            else
                b+=string(A[i])+" }";
        }

        return b;
    }

    public static String string(ArrayList A) {
        String b = "[ ";
        for (int i=0; i<A.size(); i++) {
            if (i!=A.size()-1)
                b+=A.get(i)+", ";
            else
                b+=A.get(i)+" ]";
        }
        return b;
    }

    public static String[] string_array(Object[] A) {
        String[] out = new String[A.length];
        for (int i=0; i<out.length; i++) {
            out[i] = ""+A[i].toString();
        }
        return out;
    }

    public static String comma_string(Object[] A) {
        return separated_string(A,",");
    }

    public static String separated_string(Object[] A, String sep) {
        String b = "";
        for (int i=0; i<A.length; i++) {
            if (i!=A.length-1) {
                b+=A[i]+""+sep;
            } else {
                b+=A[i];
            }
        }
        return b;
    }
}
