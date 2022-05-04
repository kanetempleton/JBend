package com.server.login;

import com.db.*;
import com.server.entity.*;
import com.Main;
import com.server.web.*;
import com.console.*;

import java.util.*;

public class LoginHandler extends DatabaseUtility implements Runnable {


    private boolean awake;
    private ArrayList<String> bannedUsers;
    private ArrayList<String> bannedIPs;
    private HashMap<String,Integer> pwAttempts;
    private HashMap<String,Long> pwTimeouts;

    private static final int TIME_BETWEEN_CLEARING_LOGIN_ATTEMPTS = 300000; //5mins

    public LoginHandler() {
        bannedIPs = new ArrayList<>();
        bannedUsers = new ArrayList<>();
        pwAttempts = new HashMap<>();
        pwTimeouts = new HashMap<>();
        awake=true;
    }

    @Override
    public void run() {
        initData();
        Console.output("Login handler launched successfully!");
       // Main.launcher.nextStage();
        while (awake) {
            pop();
        }
    }

    public void initData() {
        new ServerQuery(Main.launcher.getLoginHandler(),"select name from bans where name!='none'") {
            public void done() {
                for (String x: this.getResponses()) {
                    //System.out.println("banned user="+DatabaseManager.getValue(x,"name"));
                    bannedUsers.add(DatabaseManager.getValue(x,"name"));
                }
                new ServerQuery(Main.launcher.getLoginHandler(),"select ip from bans where ip!='none'") {
                    public void done() {
                        for (String x: this.getResponses()) {
                            //System.out.println("banned ip="+DatabaseManager.getValue(x,"ip"));
                            bannedIPs.add(DatabaseManager.getValue(x,"ip"));
                        }
                        Main.launcher.nextStage();
                    }
                };
                //Main.launcher.nextStage();
            }
        };
    }

    public void loginCheck(ServerConnection c, String user, String pass) {
        if (userBanned(user) || user.equalsIgnoreCase("none")) {
            c.sendText("urbannedgtfo");
            return;
        }
        if (pwTimeouts.containsKey(user)) {
            if (System.currentTimeMillis()>=pwTimeouts.get(user)) {
                pwTimeouts.remove(user);
            } else {
                pwTimeouts.put(user, System.currentTimeMillis() + TIME_BETWEEN_CLEARING_LOGIN_ATTEMPTS);
                c.sendText("toomanyattempts");
                return;
            }
        }
        try {
            if (Main.launcher.USING_LOGIN_ENCRYPTION) {
                String sendpw = "";
                byte[] k = (new String("jjf8943hr203hfao")).getBytes();//block size = 16
                byte[] IV = (new String("1234567890abcdef")).getBytes();
                String cat = "" + (new String(IV)) + "";
                String checkq = "SELECT COUNT(*) FROM users WHERE username='" + user + "';";
                String q = "SELECT password FROM users WHERE username='" + user + "';";
                String info = user + "," + pass;
                //new ServerQuery(this,Main.launcher.getWebserver(),c,ServerQuery.LOGIN_REQUEST,q);
                c.setName(user);
                new ServerQuery(this, c.getServer(), c, ServerQuery.CHECK_FOR_USER, checkq, q, new String[]{info});
            } else {
                String sendpw = "";
               // byte[] k = (new String("jjf8943hr203hfao")).getBytes();//block size = 16
               // byte[] IV = (new String("1234567890abcdef")).getBytes();
               // String cat = "" + (new String(IV)) + "";
                String checkq = "SELECT COUNT(*) FROM users WHERE username='" + user + "';";
                String q = "SELECT password FROM users WHERE username='" + user + "';";
                String info = user + "," + pass;
                //new ServerQuery(this,Main.launcher.getWebserver(),c,ServerQuery.LOGIN_REQUEST,q);
                c.setName(user);
                new ServerQuery(this, c.getServer(), c, ServerQuery.CHECK_FOR_USER, checkq, q, new String[]{info});
            }
        } catch (Exception e) {
            e.printStackTrace(); //fuck bounds checkin ass bitch
        }
    }

    public void registerNewUser(ServerConnection c, String user, String pass, String email) {
        if (user.equalsIgnoreCase("none")) {
            c.sendText("registeralreadyexists");
            return;
        }
        email = email.replace("%40","@");
        boolean ucheck = usernameCheck(user);
        boolean pcheck = passwordCheck(pass);
        boolean echeck = emailCheck(email);
        if (!ucheck) {
            c.sendText("failedusercheck");
          //  c.disconnect();
            return;
        }
        if (!pcheck) {
            c.sendText("failedpwcheck");
            //c.disconnect();
            return;
        }
        if (!echeck) {
            c.sendText("failedemailcheck");
          //  c.disconnect();
            return;
        }
        String sendpw = "";
        byte[] k = (new String("jjf8943hr203hfao")).getBytes();//block size = 16
        byte[] IV = (new String("1234567890abcdef")).getBytes();//
        String cat = ""+(new String(IV))+"";
       // String cpw = "jf9p8Nd1#(RU-="+pass.substring(0,pass.length()/2)+"fafKFF:b,23"+pass.substring(pass.length()/2)+"z";
        //System.out.println("cpw");
        String crp = Main.launcher.cryptography().bytesToHex(Main.launcher.cryptography().cbcEncrypt(pass.getBytes(),k,IV));
        sendpw = cat+crp;//cat+crp;
        String spw = ""+sendpw;
        String mmail = ""+email;
        new ServerQuery(Main.launcher.getLoginHandler(),c,"select COUNT(*) from users where email='"+email+"';") {
            public void done() {
                if (Integer.parseInt(this.getResponse())==0) { //ok!
                    String checkq = "SELECT COUNT(*) FROM users WHERE username='"+user+"';";
                    String q = "INSERT INTO users(username,password,email,privileges) VALUES('"+user+"','"+spw+"','"+mmail+"',0);";
                    new ServerQuery(Main.launcher.getLoginHandler(),c.getServer(),c,ServerQuery.CHECK_FOR_USER,checkq,q);
                } else if (Integer.parseInt(this.getResponse())==1) { //existing email
                    this.sendHTTP("dupeemail");
                    this.close();
                } else { //multiple players exist - delete the entire database
                    this.sendHTTP("whatthefucklmao");
                    this.close();
                }
            }
        };


    }

    public void serverAction(ServerQuery q) {
        switch (q.type()) {
            case ServerQuery.CHECK_FOR_USER:
                if (Integer.parseInt(q.getResponse()) > 0) {
                    if (q.getPayload().startsWith("INSERT")) { //registration
                        q.sendHTTP("registeralreadyexists");
                        q.close();
                    }
                    else if (q.getPayload().startsWith("SELECT")) { //login requests
                        String loginInfo = q.getExtraData(0);
                        new ServerQuery(this,q.fromConnection().getServer(),q.fromConnection(),ServerQuery.LOGIN_REQUEST,q.getPayload(),loginInfo);
                    }
                } else {
                    if (q.getPayload().startsWith("INSERT")) //
                        new ServerQuery(this, q.fromConnection().getServer(), q.fromConnection(), ServerQuery.REGISTER_REQUEST, q.getPayload());
                    else if (q.getPayload().startsWith("SELECT")) {
                        q.sendHTTP("logindne");
                        q.close();
                    }
                }
                break;
            case ServerQuery.REGISTER_REQUEST:
                //q.fromServer().getAPI().sendMessage(q.fromConnection(),HTTP.HTTP_OK+"registersuccess");
                q.sendHTTP("registersuccess");
                q.close();
                //q.fromConnection().setNeedsReply(false);
                //q.fromConnection().disconnect(Main.launcher.getWebserver());
                break;
            case ServerQuery.LOGIN_REQUEST:
               // System.out.println("login request result: "+q.getResponse());
                String result = "";
                if (Main.launcher.USING_LOGIN_ENCRYPTION) {
                    String[] rs = q.getResponse().split("=");
                    if (rs.length != 2) {
                        Console.output("login format error: couldn't find password");
                        q.sendHTTP("loginsilly");
                        q.close();
                        return;
                    }
                    result = q.getResponse().split("=")[1];
                    if (result.length() <= 16) {
                        Console.output("login format error: password too short");
                        q.sendHTTP("loginsilly");
                        q.close();
                        return;
                    }
                    byte[] k = (new String("jjf8943hr203hfao")).getBytes();//block size = 16
                    String iv = result.substring(0, 16); //get IV from the password
                    byte[] IV = iv.getBytes();
                    String cat = "" + (new String(IV)) + "";
                    String cppw = result.substring(16);
                    byte[] cpw = Main.launcher.cryptography().hexToBytes(cppw);
                    byte[] decpw = Main.launcher.cryptography().cbcDecrypt(cpw, k, IV);
                    result = new String(decpw);
                } else {
                    result = q.getResponse().split("=")[1];
                }
                if (result.length() > 0 && q.getPayload().contains(",")) {
                    String pwCheck = q.getPayload().split(",")[1];

                    if (pwCheck.equals(result)) {
                        q.addCookie(new Cookie("usr",q.fromConnection().getName(),"/"));
                      //  q.addCookie(new Cookie("ukey",q.fromConnection().getName(),"/"));
                        q.sendHTTP("loginsuccess");
                        q.close();
                        pwAttempts.remove(q.fromConnection().getName());
                    } else {
                        String user = q.fromConnection().getName();
                        if (!pwAttempts.containsKey(user)) {
                            pwAttempts.put(user,1);
                            q.sendHTTP("logininvalid");
                            q.close();
                        } else {
                            int kount = pwAttempts.remove(user);
                           // pwAttempts.remove(user);
                            pwAttempts.put(user,kount+1);
                            if (kount+1>=5) {
                                pwTimeouts.put(user,System.currentTimeMillis()+TIME_BETWEEN_CLEARING_LOGIN_ATTEMPTS);
                                //pwAttempts.remove(user);
                                q.sendHTTP("toomanyattempts");
                                q.close();
                            } else {
                                q.sendHTTP("logininvalid");
                                q.close();
                            }
                        }
                    }
                } else {
                    q.sendHTTP("loginsilly");
                    q.close();
                }
                break;
        }
    }



    //password max length = 20, min length = 6
    private boolean passwordCheck(String pw) {
        if (pw.length()<6 || pw.length()>20)
            return false;
        for (byte b: pw.getBytes()) {
            if (!allowedByte_password(b))
                return false;
        }
        return true;
    }


    //username max length = 14, min length = 1
    private boolean usernameCheck(String un) {
        if (un.length()<1 || un.length()>14)
            return false;
        for (byte b: un.getBytes()) {
            if (!allowedByte_username(b))
                return false;
        }
        return true;
    }

    //min length = 5 (a@b.c) max length = 100???
    //must include @ sign duh
    private boolean emailCheck(String em) {
        if (em.length()<5 || em.length()>100)
            return false;
        boolean flag = false;
        for (byte b: em.getBytes()) {
            if (b==64)
                flag=true;
            if (!allowedByte_email(b))
                return false;
        }
        return flag;
    }

    //password must be:
    //only printable chars, not the @ sign
    private boolean allowedByte_password(byte b) {
        int x = (int)b;
        if (x<32 || x>126 || x==64)
            return false;
        return true;
    }

    //email must be:
    //only printable chars
    private boolean allowedByte_email(byte b) {
        int x = (int)b;
        if (x<32 || x>126)
            return false;
        return true;
    }

    //usernames should only allow:
    //spaces, numbers, letters
    private boolean allowedByte_username(byte b) {
        int x = (int)b;
        if (x==32 || x>=48&&x<=57 || x>=65&&x<=90 || x>=97&&x<=122)
            return true;
        return false;
    }


    public boolean userBanned(String u) {
        return bannedUsers.contains(u);
    }

    public boolean ipBanned(String u) {
        return bannedIPs.contains(u);
    }

    //these methods are not permanent; no data put into database
    public void addToBannedUsers(String u) {
        bannedUsers.add(u);
    }

    public void addToBannedIPs(String u) {
        bannedIPs.add(u);
    }

    public void removeBannedUser(String u) {
        bannedUsers.remove(u);
    }

    public void removeBannedIP(String u) {
        bannedIPs.remove(u);
    }



}