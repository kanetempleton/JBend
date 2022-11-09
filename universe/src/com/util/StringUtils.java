package com.util;

public class StringUtils {

    public static String deleteLine(String text, int lineNo) {
        String[] l = lines(text);
        if (lineNo >= l.length) {
            return text;
        }
        if (lineNo == l.length-1) {
            return linesToText(ArrayUtils.subarray(l,0,lineNo-1));
        }
        if (lineNo == 0) {
            return linesToText(ArrayUtils.subarray(l,1,l.length-1));
        }
        return linesToText(ArrayUtils.merge(ArrayUtils.subarray(l,0,lineNo-1),ArrayUtils.subarray(l,lineNo+1,l.length-1)));
    }

    public static String replaceLine(String text, int lineNo, String replaceWith) {
        String[] l = lines(text);
        l[lineNo] = replaceWith;
        return linesToText(l);
    }

    public static String[] lines(String text) {return text.split("\n");}

    public static String linesToText(Object[] lines) {
        return unsplit(lines,"\n")+"\n";
    }

    public static String unsplit(Object[] X, String d) {
        String out = "";
        for (int i=0; i<X.length-1; i++) {
            out += X[i] + d;
        }
        out += X[X.length-1];
        return out;
    }

}