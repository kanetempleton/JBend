package com.util;

import java.util.HashMap;


class ConnectionData {

    int numAttempts;
    long expire;
    long lastAttempt;
    String ip;
    long banTime;

    ConnectionData(String ipadr) {
        ip=ipadr;
        lastAttempt=System.currentTimeMillis();
        expire = System.currentTimeMillis()+1000;
        banTime=0;
    }

}

public class ConnectionManager {

    private HashMap<String,ConnectionData> connectionAttempts;

    private static final int MAX_CONNECT_ATTEMPTS_TCP = 5;
    private static final int MAX_CONNECT_ATTEMPTS_WEBSOCKET = 5;
    private static final int MAX_CONNECT_ATTEMPTS_HTTP = 50;
    private String apiName;
    private int maxAttempts;

    public ConnectionManager(String apiType) {
        connectionAttempts = new HashMap<>();
        apiName=apiType;
        maxAttempts=MAX_CONNECT_ATTEMPTS_TCP;
        if (apiName.equals("HTTP"))
            maxAttempts=MAX_CONNECT_ATTEMPTS_HTTP;
        else if (apiName.equals("WebSocket"))
            maxAttempts=MAX_CONNECT_ATTEMPTS_WEBSOCKET;
    }

    public int recordConnectionAttempt(String ipa) {
        if (connectionAttempts.containsKey(ipa) && !apiName.equals("HTTP")) {
            ConnectionData d = connectionAttempts.get(ipa);
            d.numAttempts++;
            long now = System.currentTimeMillis();
            if (d.banTime>0) {
                if (now>=d.banTime) {
                    d.banTime=0;
                    System.out.println("unbanning: "+ipa);
                    return 0;
                } else {
                    System.out.println("another connection attempt was made from temporarily-banned ip: "+ipa);
                    return -2;
                }
            }
            if (now-d.lastAttempt < 1000) {
                if (d.numAttempts>=maxAttempts) {
                    d.lastAttempt=now;
                    System.out.println("too many connection attempts from "+ipa+"; banning attempts for 5 minutes");
                    d.banTime=now+300000; //ban for 5 minutes
                    return -1;
                } else {
                    d.lastAttempt=now;
                    return d.numAttempts;
                }
            } else {
                d.numAttempts=1;
                d.lastAttempt=now;
                return d.numAttempts;
            }
        } else {
            ConnectionData d = new ConnectionData(ipa);
            d.numAttempts=1;
            connectionAttempts.put(ipa,d);
            return d.numAttempts;
        }
    }


}