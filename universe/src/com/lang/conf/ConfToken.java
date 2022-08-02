
package com.lang.conf;

public class ConfToken {

    private String name;
    private String data;

    public ConfToken(String name, String dat) {
        this.name=name;
        this.data=dat;
    }

    public static enum tokens {

    }

    public String getName(){return name;}
    public String getData(){return data;}

    public String toString() {
        if (this.data==null) {
            return name;
        }
        return "$("+data+")";
    }
}