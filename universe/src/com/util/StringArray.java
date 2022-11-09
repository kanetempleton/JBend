package com.util;

import java.util.function.Function;
import java.util.*;

public class StringArray {

    // returns the first j elements in the tokenization array of X which are not equal to match
    // X = x1 + token + x2 + token + ... + xk + token + ... + token + xn
    // -> [ x1, x2, ... , xj ] : j == k-1
    // where xk == match and xi != match for i=1,2,...,k-1
    public static String[] split_until(String X, String token, String match) {
        String[] S = X.split(token);
        int found = ArrayUtils.equals_at(S,match);
        return string.apply(ArrayUtils.to(S,found));
    }

    public static Object[] map(Object[] X, Function F) {
        return Arrays.stream(X).map(F).toArray();
    }



    public static Function<Object,String> str_obj = (
            X -> X+""
            );

    public static Function<Object[],String[]> string = (
            X -> (String[])map(X,str_obj)
    );
}