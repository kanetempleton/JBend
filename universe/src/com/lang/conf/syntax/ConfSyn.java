package com.lang.conf.syntax;

public class ConfSyn {


    private String syntaxName;
    private String args[];

    public ConfSyn(String nom, String rgz[]) {
        syntaxName = nom;
        args = rgz;
    }

    public boolean match(String[] tokens, int token) {
        return false;
    }

    public String toString() {
        String out = syntaxName+"( ";
        for (int i=0; i<args.length; i++) {
            out += args[i]+" ";
        }
        out += ")";
        return out;
    }

    public String syntaxName() {return syntaxName;}
    public String[] args() {
        return args;
    }
    public String args(int i){return args[i];}
    public int tokenLength() {return this.toString().replace("( "," ").replace(" )","").split("").length;}


}