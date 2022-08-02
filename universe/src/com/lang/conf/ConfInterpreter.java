package com.lang.conf;

import com.func.*;
import java.util.*;

import com.util.Tools;

public class ConfInterpreter {

    private ArrayList<ConfFunction> program;
    private String[] syntax;
    public ConfInterpreter(String[] syn) {
        syntax=syn;
        program = new ArrayList<>();
    }

    public static ArrayList<ConfFunction> interpret(String[] s) {
        ArrayList<ConfFunction> out = new ArrayList<>();
        for (int i=0; i<s.length; i++) {
            ConfFunction f = getFunction(s[i]);
            if (f!=null) {
                out.add(f);
            }
        }
        return out;
    }

    public void interpret() {
        for (int i=0; i<syntax.length; i++) {
            ConfFunction f = getFunction(syntax[i]);
            if (f!=null) {
                program.add(f);
            }
        }
    }
    public String toString() {
        String out = "";
        for (int i=0; i<program.size(); i++) {
            out += program.get(i)+"\n";
        }
        return out;
    }
    public void execute() {
        for (int i=0; i<program.size(); i++) {
            program.get(i).execute();
        }
    }

    public static ConfFunction getFunction(String s) {
        String[] words = s.replace("{ ","").replace(" }","").split(" ");
        String def="";
        if (words[0].equals("APPDEF")) {
            if (words.length == 2) {
                return new AppDef(words[1]);
            } else {
                return null;
            }
        }
        if (words[0].equals("DEF")) {
            if (words.length == 2) {
                //def = words[1];
                return new Def(words[1]);
            } else {
                return null;
            }
        }
        if (words[0].equals("FED")) {
          //  String enddef = def+"";
          //  def = "";
            return new Fed();
        }
        if (words[0].equals("FUNC")) {
            if (words.length < 2) {
                return null;
            } else {
//                String[] fargs = Tools.substring_array(words,2,words.length-1);
                return new Func(words[1],Tools.substring_array(words,2,words.length-1));
            }
        }
        return null;
    }
}