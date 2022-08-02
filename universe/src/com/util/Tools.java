package com.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

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

    public static String tokenize(String[] X, String token) {
        String out = "";
        for (int i=0; i<X.length; i++) {
            if (i==X.length-1)
                out+=X[i];
            else
                out+=X[i]+""+token;
        }
        return out;
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

    public static String space(String[] A) {
        return tokenize(A," ");
    }

    public static String comma_string(Object[] A) {
        return separated_string(A,",");
    }
    public static String comma_string(Object[] A,Object[] B, String delim) {
        return separated_string(A,B,delim,",");
    }

    public static String[] newInstance(String[] A) {
        String B[] = new String[A.length];
        for (int i=0; i<A.length; i++) {
            B[i] = ""+A[i];
        }
        return B;
    }

    /*  Input: (A[] = [a1 a2 .. aN], S)
        Output: "a1Sa2S...SaN"
     */
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

    /*  Input: (A = [a1 a2 .. aN], B[b1 b2 .. bN], x, y)
        Output: a1xb1ya2xb2y...yaNxbN
     */
    public static String separated_string(Object[] A, Object[] B, String sep1, String sep2) {
        String b = "";
        for (int i=0; i<A.length; i++) {
            if (i!=A.length-1) {
                b+=A[i]+""+sep1+""+B[i]+""+sep2;
            } else {
                b+=A[i]+""+sep1+""+B[i]+"";
            }
        }
        return b;
    }

    public static Object[] join(Object[] A, Object[] B) {
        Object[] output = new Object[A.length+B.length];
        int z=0;
        for (int i=0; i<A.length; i++) {
            output[z++]=A[i];
        }
        for (int i=0; i<B.length; i++) {
            output[z++]=B[i];
        }
        return output;
    }

    public static Object[] join(Object A, Object[] B) {
        Object[] C = new Object[1];
        C[0] = A;
        return join(C,B);
    }
    public static Object[] join(Object A[], Object B) {
        Object[] C = new Object[1];
        C[0] = B;
        return join(A,B);
    }
    public static Object[] join(Object A, Object B) {
        Object[] C = new Object[2];
        C[0] = A;
        C[1] = B;
        return C;
    }


    public static String[] join(String[] A, String[] B) {
        String[] output = new String[A.length+B.length];
        int z=0;
        for (int i=0; i<A.length; i++) {
            output[z++]=""+A[i];
        }
        for (int i=0; i<B.length; i++) {
            output[z++]=""+B[i];
        }
        return output;
    }

    public static String[] join(String A, String[] B) {
        String[] C = new String[1];
        C[0] = ""+A;
        return join(C,B);
    }
    public static String[] join(String A[], String B) {
        String[] C = new String[1];
        C[0] = ""+B;
        return join(A,B);
    }
    public static String[] join(String A, String B) {
        String[] C = new String[2];
        C[0] = ""+A;
        C[1] = ""+B;
        return C;
    }


    // ARRAY CREATION METHODS

    // f: x -> [ x ]
    public static Object[] A(Object x) {
        Object[] out = new Object[1];
        out[0]=x;
        return out;
    }

    public static String[] substring_array(String[] X, int start, int end) {
        String[] out = new String[end-start+1];
        int j=0;
        for (int i=start; i<=end; i++) {
            out[j++]=X[i];
        }
        return out;
    }

    public static String[] A(String x) {
        return new String[] {x};
    }

    // f: (a,b) -> [ a b ]
    public static Object[] A(Object a, Object b) { return join(a,b); }


    public Object[] map(Object[] X, Function F) {
        return Arrays.stream(X).map(F).toArray();
    }

    public static Function<Integer, Integer> dbl = x -> x*2;
    public static Function<String, String> dblS = (
                s -> s+""+s
            );
    public static String doubleString(String x) {
        return dblS.apply(x);
    }


    public static boolean fieldValuePair(String[] fields, String[] values, String f, String v) {
        if (fields.length!=values.length) {
            System.out.println("field-value comparison but mismatch in length; returned false by default");
            return false;
        }
        for (int i=0; i<fields.length; i++) {
            if (fields[i].equals(f) && values[i].equals(v))
                return true;
        }
        return false;
    }
}



/* LIST OF METHODS

    ARRAYS:
     * newInstance(A[])
     * join(A[],B[])


 */