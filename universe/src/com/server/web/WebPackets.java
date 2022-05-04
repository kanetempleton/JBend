package com.server.web;

import com.db.queries.ServerQuery;
import com.server.entity.*;
import com.Main;
import com.console.*;
import com.server.protocol.HTTP;

public class WebPackets {

    public WebPackets() {

    }

    public void processPOST(ServerConnection c, String uri, int packetID, String[] fields, String[] values) {

        System.out.println("Custom POST processing for uri="+uri+", packetID="+packetID);

    }

    //THIS PROCESSES POST REQUESTS
    public void processWebPacket(ServerConnection c, String uri, String data) {
        System.out.println("PROCESSING WEB PACKET!!!!");
        String[] pairs = data.split("&");
        String[] fields = new String[pairs.length-1];
        String[] values = new String[pairs.length-1];
        int j=0,k=0;
        int packetType = -1;
        for (String p: pairs) {
            String[] ab = p.split("=");
            if (ab.length!=2) {
                System.out.println("WEB PACKET PROCESSING ERROR FOR "+p);
                c.disconnect();
                return;
            }
            if (j==0 && p.split("=")[0].equals("packet")) {
                packetType = Integer.parseInt(ab[1]);
                j++;
            } else {
                fields[k] = ab[0];
                values[k++] = ab[1];
            }
        }
        Console.output("packetype was "+packetType);
        String usr = "";
        processPOST(c,uri,packetType,fields,values);
        switch (packetType) {
            case 0:
                handleRegistration(c,fields,values);
                break;
            case 1:
                handleLogin(c,fields,values);
                break;
            case 2: //old login method; not used anymore
              /*  usr = "";
                for (Cookie co: c.getCookies()) {
                    if (co.field().equals("usr"))
                        usr=co.value();
                }
                if (usr.equals("")) {
                    c.sendMessage(HTTP.HTTP_OK+"\r\nnotloggedin");
                    c.disconnect();
                } else {
                    String x = usr;
                    new ServerQuery(Main.launcher.getLoginHandler(),c,"select COUNT(*) from players where username='"+usr+"';") {
                        public void done() {
                            if (Integer.parseInt(this.getResponse())==0) { //create new player
                                this.sendHTTP("newplayer");
                                this.close();
                            } else if (Integer.parseInt(this.getResponse())==1) { //existing player
                                Main.launcher.game().controller().loginPlayer(x,c);
                                this.sendHTTP("existingplayer");
                                this.close();
                            } else { //multiple players exist - delete the entire database
                                this.sendHTTP("toomanyusers");
                                this.close();
                            }
                        }
                    };
                }*/
                break;
            case 3://logout
                usr="";
                for (Cookie co: c.getCookies()) {
                    if (co.field().equals("usr"))
                        usr=co.value();
                }
                if (usr.equals("")) {
                    c.sendMessage(HTTP.HTTP_OK+"\r\nnotloggedin");
                    c.disconnect();
                } else {
                    String send = HTTP.HTTP_OK+"";
                    Cookie cook = new Cookie("usr","none; expires=Thu, 01 Jan 1970 00:00:00 GMT","/");
                   // Cookie cook = new Cookie("usr","none","/");
                    send+=cook.header();
                    send+="\r\nlogoutbye";
                    c.sendMessage(send);
                }
                break;
            case 420: //admin command from web
                usr = "";
                for (Cookie co: c.getCookies()) {
                    if (co.field().equals("usr"))
                        usr=co.value();
                }
                if (usr.equals("") || !uri.equals("/pages/portal/fuzz")) {
                    c.sendMessage(HTTP.HTTP_OK+"\r\nwyd");
                    c.disconnect();
                    return;
                }
                new ServerQuery(Main.launcher.getLoginHandler(),c,"select privileges from users where username='"+usr+"';") {
                    public void done() {
                        String[] x = getResponse().split("rivileges=");
                        if (x.length<2) {
                            sendHTTP("gtfo");
                            return;
                        }
                        int p = Integer.parseInt(x[1]);
                        if (p!=3) { //non-admin
                            sendHTTP("gtfo");
                        } else {
                            for (int k=0; k<fields.length; k++) {
                                if (fields[k].equals("inp")) {
                                    String com = values[k];
                                    Console.output("command typed from "+c+": "+com);
                                    if (com.length()==0 || com.startsWith("+")) {
                                        sendHTTP("nonsense");
                                        return;
                                    }
                                    String[] args = com.split("\\+"); //apparently this is very necessary to have double \\
                                    String newCom = "";
                                    for (String a: args) {
                                        if (a.equals(args[args.length-1]))
                                            newCom+=a+"";
                                        else
                                            newCom+=a+" ";
                                    }

                                    String[] rBuf = {"blank"};
                                    String reply = "comresp[]+:+::+++:::";
                                    if (newCom.split(" ")[0].equalsIgnoreCase("mysql")) {
                                        rBuf[0]="you cannot do database queries thru the web portal yet";
                                    } else {
                                        Main.launcher.console().processCommand(newCom, rBuf);
                                    }
                                    reply+=rBuf[0];
                                    sendHTTP(reply);
                                }
                            }
                        }
                    }
                };
                break;
            default:
                //c.sendMessage("unsupported web packet gtfo");
                break;
        }
    }


    public void handleRegistration(ServerConnection c, String[] fields, String[] values) {
        String user = "";
        String email = "";
        String pass = "";
        for (int i=0; i<fields.length; i++) {
            switch (fields[i]) {
                case "username":
                    user = values[i];
                    break;
                case "email":
                    email = values[i];
                    break;
                case "password":
                    pass = new String(Main.launcher.cryptography().rsaDecrypt(199,193,values[i].getBytes()));
                    break;
            }
        }

        //System.out.println("[placeholder] register "+user+","+email+","+pass);
        Main.launcher.getLoginHandler().registerNewUser(c,user,pass,email);
    }

    private void handleLogin(ServerConnection c, String[] fields, String[] values) {
        String user = "";
        String email = "";
        String pass = "";
        for (int i=0; i<fields.length; i++) {
            switch (fields[i]) {
                case "username":
                    user = values[i];
                    break;
                case "email":
                    email = values[i];
                    break;
                case "password":
                    if (Main.launcher.USING_LOGIN_ENCRYPTION) {
                        pass = new String(Main.launcher.cryptography().rsaDecrypt(199, 193, values[i].getBytes()));
                    } else {
                        pass = values[i];
                    }
                    break;
            }
        }

       // System.out.println("[placeholder] login "+user+","+email+","+pass);
        Main.launcher.getLoginHandler().loginCheck(c,user,pass);
    }

}