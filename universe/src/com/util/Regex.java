package com.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex {

    private String expr;
    public Regex(String x) {
        expr=x;
    }

    public boolean match(String s) {
        Pattern pattern = Pattern.compile(expr);
        Matcher matcher = pattern.matcher(s);
        return matcher.find();
    }

    public boolean match(String s, int flags) {
        Pattern pattern = Pattern.compile(expr,flags);
        Matcher matcher = pattern.matcher(s);
        return matcher.find();
    }

    public boolean match(String s, boolean caseSensitive) {
        return caseSensitive ? match(s) : match(s,Pattern.CASE_INSENSITIVE);
    }

    public static boolean match(String w, String p) {
        Regex r = new Regex(p);
        return r.match(w);
    }
    public static boolean match(String w, String p, boolean caseSensitive) {
        Regex r = new Regex(p);
        return r.match(w,caseSensitive);
    }

    public static Regex word() {
        return new Regex("^[a-z]+$");
    }




}
