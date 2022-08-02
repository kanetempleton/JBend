package com.func;

public class ConfFunction {

    private String name;
    private String[] args;
    public ConfFunction(String n) {
        name=n;
        args = new String[]{""};
    }
    public ConfFunction(String n, String[] r) {
        name=n;
        args=r;
    }

    public String getName(){return name;}
    public String[] args(){return args;}
    public String args(int i){return args[i];}

    public void execute() {

    }

    public String returnValue() {
        return "null";
    }

    public String toString() {
        return "UNSPEC-FUNC";
    }
}