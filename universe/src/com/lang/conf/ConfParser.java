
package com.lang.conf;

import com.lang.conf.syntax.*;
import com.util.Tools;

public class ConfParser {

    public ConfParser() {

    }

    public static String parse(String tokens) {
        if (tokens.contains("ERROR")) {
            return "ERROR";
        }
        String[] t = tokens.split(" ");
        if (t.length < 2)
            return "ERROR";
        if (!t[0].equals("APPDEF")) {
            return "ERROR";
        }
        return check(t,0);
    }

    private static String check(String[] tokens, int t) {
        boolean appdef = false;
        boolean def = false;
        boolean func = false;
        boolean done = false;
        String out = "";
        //String build = "";

        String syntax[] = extractSyntax(tokens);
        return Tools.tokenize(syntax,"\n");
    }


    public static String[] extractSyntax(String[] tokens) {
        String out = "";
        String build = "";
        boolean def = false;
        boolean appdef = false;
        for (int i=0; i<tokens.length; i++) {
            if (isKeyword(tokens[i])) {
                if (tokens[i].equals("DEF"))
                    def=true;
                if (tokens[i].equals("APPDEF"))
                    appdef=true;
                if (build.length() > 0) {
                    if (tokens[i].equals("BLANKLINE")) {
                        if (!def && !appdef) {
                            build += tokens[i] + " ";
                            out += "{ " + build + "}\n";
                            build = "";
                        } else {
                            out += "{ " + build + "}\n";
                            if (!appdef)
                                out += "{ FED }\n";
                            build = "";
                            if (def)
                                def = false;
                            if (appdef)
                                appdef=false;
                        }
                    } else {
                        out += "{ " + build + "}\n";
                        build = tokens[i]+" ";
                    }
                } else {
                    build += tokens[i]+" ";
                }
            } else {
                build += tokens[i]+" ";
            }
        }
    //    System.out.println("found syntax:\n"+out);
        return out.split("\n");
    }

    public static String getPattern(String keyword) {
        switch (keyword) {
            case "APPDEF":
                return "APPDEF $x BLANKLINE";
            case "DEF":
                return "DEF $x {FUNC} BLANKLINE";
        }
        return "ERROR";
    }

    public static int syntaxMatch(String[] tokens, int t) {
        if (t >= tokens.length || t<0)
            return -1;
        String syntax[] = match(tokens[t]).split(" ");
        int synlen = syntax.length;
        if (t + synlen > tokens.length)
            return -1;

        int nextsyn = nextSyntax(tokens,t);
        if (nextsyn == -1) {
            return -2; //done?
        }
        for (int i=0; i<(nextsyn-t); i++) {
            if (!tokenMatch(tokens,i+t,syntax[i]))
                return -1;
        }
        return nextsyn;
    }

    //gets the next block of syntax starting at index t
    public static int nextSyntax(String[] tokens, int t) {
        int start = -1;
        int end = -1;
        String out = "";

        if (t>=tokens.length)
            return -1;

        for (int i=t; i<tokens.length; i++) {
            if (start==-1 && isKeyword(tokens[i])) {
                start = i;
            }
            else if (start!=-1 && isKeyword(tokens[i])) {
                end = i-1;
                break;
            }
        }
        if (start != -1 && end != -1 && end > start) {
            out = "{ ";
            for (int i = start; i<=end; i++) {
                out += tokens[i]+" ";
            }
            out +="}";
        }
    //    System.out.println("extracted syntax: "+out);
        return end+1;
    }

    public static boolean tokenMatch(String[] tokens, int t, String pattern) {
        if (pattern.equals("$x")) { //variable name
           // Regex r = new Regex("token");
            return isVarWord(tokens[t]);
        }
        if (pattern.equals("(FUNC)+")) { //one or more functions
            int len = getFunctionLength(tokens,t);
            return isFunction(tokens,t,len);
        }
        return tokens[t].equals(pattern);
    }

    private static boolean isFunction(String[] tokens, int t, int length) {
        if (length <=0 || t<0 || t+length>=tokens.length)
            return false;
        if (!tokens[t].equals("FUNC"))
            return false;
        for (int i=1; i<length; i++) {
            if (!isVarWord(tokens[t+i]))
                return false;
        }
        return true;
    }

    private static int getFunctionLength(String[] tokens, int t) {
        int i=1;
        boolean flag = false;
        while (!flag) {
            if (t+i >= tokens.length) {
                return -1;
            }
            if (isKeyword(tokens[t+i])) {
                return i;
            } else {
                i++;
            }
        }
        return -1;
    }

    private static boolean isKeyword(String x) {
        return x.equals("APPDEF") || x.equals("BLANKLINE") || x.equals("DEF") || x.equals("FUNC");
    }

    // letters, numbers, periods only
    private static boolean isVarWord(String x) {
        if (isKeyword(x))
            return false;
        byte[] btes = x.getBytes();
        for (byte b: btes) {
            if (!(b == 46 || (b >= 48 && b<=57 && b>= 65 && b<= 90 && b>= 97 && b <= 122)))
                return false;
        }
        return true;
    }


    public static String match(String token) {
        switch (token) {
            case "APPDEF":
                return "APPDEF $x BLANKLINE";
            case "DEF":
                return "DEF $x (FUNC)+ BLANKLINE";
            case "FUNC":
                return "FUNC $x+";
        }
        return "ERROR";
    }
}