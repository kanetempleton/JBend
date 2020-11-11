package com.server.web;

public class Cookie {
    private String field,value;
    private boolean hasMaxAge;
    private int maxAge;
    private String path;
    public Cookie(String k, String v) {
       this(k,v,".");
    }
    public Cookie(String k, String v, String p) {
        field=k;
        value=v;
        maxAge=3600; //1 hour
        hasMaxAge=false;
        path=p;
    }
    public String field(){return field;}
    public String value(){return value;}
    public void setAge(int age) {
        maxAge=age;
        hasMaxAge=true;
    }
    public String header() {
        if (!path.equals("."))
            return "Set-Cookie: "+field+"="+value+"; Path="+path+"\r\n";
        return "Set-Cookie: "+field+"="+value+"\r\n";
    }
    public String toString() {
        return field+"="+value;
    }
}