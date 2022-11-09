package com.util;

import java.util.function.Function;

public class ArrayUtils {

    // X[0..n-1] -> X[a..b]
    public static Object[] subarray(Object[] X, int a, int b) {
        if (bounds_check(X,a,b)) {
            Object[] out = new Object[b-a+1];
            for (int i=a; i<=b; i++) {
                out[i-a]=X[i];
            }
            return out;
        }
        return null;
    }

    // X[0..n-1] -> X[0..k]
    public static Object[] to(Object[] X, int k) { return subarray(X,0,k); }
    // X[1..n-1] -> X[k..n-1]
    public static Object[] from(Object[] X, int k) { return subarray(X,k,X.length-1); }




    /// Array Property Methods

    // Condition checking
    public static int equals_at(Object[] X, Object y) {
        int i=0;
        for (Object x: X) {
            if (x.equals(y))
                return i;
            i++;
        }
        return -1;
    }


    // Bounds checking

    public static boolean bounds_check(Object[] X, int i) {
        return i>=0 && i<X.length;
    }
    public static boolean bounds_check(Object[] X, int a, int b) {
        return bounds_check(X,a)&&bounds_check(X,b);
    }


    /// Array Conversion Methods

    public static String[] string(Object[] A) {
        String[] out = new String[A.length];
        for (int i=0; i<out.length; i++) {
            out[i] = ""+A[i].toString();
        }
        return out;
    }


    /// Array Combination Methods

    public static Object[] merge(Object[] A, Object[] B) {
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


}