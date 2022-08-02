package com.func;

public class Def extends ConfFunction {


    public Def(String defname) {
        super("Def", new String[]{defname});
    }

    public void execute() {
        System.out.println("starting definition for "+args(0));
    }

    public String toString() {
        return "Def("+args(0)+") {";
    }
}