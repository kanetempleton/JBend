package com.server.protocol;

import com.server.entity.ServerConnection;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import com.Main;
import java.util.ArrayList;
import com.server.web.*;
import com.console.*;

class Response {
    ArrayList<String> headers;
    String body;

    Response() {
        new Response("");
    }
    Response(String b) {
        headers = new ArrayList<String>();
        headers.add("HTTP/1.1 200 OK");
        headers.add("Content-Type: text/html");
        body = b;
    }
    String response() {
        String r = "";
        for (String h: headers) {
            r+=h+"\r\n";
        }
        r+="\r\n";
        r+=body;
        return r;
    }
    void addCookie(Cookie c) {
        headers.add("Set-Cookie: "+c.field()+"="+c.value()+""); //Max-Age=3600',"Content-Type": "text/html"});
    }
}

public class HTTP extends Protocol {

    public static final String DEFAULT_HOME_DIRECTORY = "res/front/";

    public static final String WAIT_FOR_RESPONSE = "WAIT4RESPONSEDONTSENDTHIS";

    protected String indexDirectory;
    protected ArrayList<Cookie> cookies;
    private int port;

    public HTTP(String indDir, int prt) {
        super();
        indexDirectory=indDir;
        port=prt;
        cookies = new ArrayList<Cookie>();
    }

    /* processMessage(c,data):
        process incoming data as an HTTP request
        then close the connection which data came from
    */
    @Override
    public void processMessage(ServerConnection c, String data) {
        try {
            processHTTPRequest(c, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
       // if (!c.needsReply())
       //     c.disconnect(Main.launcher.getWebserver());
        //Main.launcher.getServer().dropConnection(c);
    }

    protected void processHTTPRequest(ServerConnection c, String data) {
        if (c==null) {
            System.out.println("null connection request");
            return;
        }
        String[] lines = data.split("\r\n");
        if (lines.length<1) {
            sendBadRequest(c);
            return;
        }
        String[] words = lines[0].split(" ");
        if (words.length<3) {
            sendBadRequest(c);
            return;
        }
        String url = words[1];
        boolean imageRequest = false;
        for (String L:lines) { //processing headers
           /* if (L.startsWith("Accept:")) {
                if (L.contains("image")||L.contains("png")) {
                    imageRequest = true;
                }
            }*/
            if (L.startsWith("Cookie:")) {
                if (L.split("Cookie: ").length > 1) {
                    String ck = L.split("Cookie: ")[1];
                    for (String kv: ck.split("; ")) {
                        String[] v = kv.split("=");
                        if (v.length==2) {
                            if (v[0].equals("usr")) {
                                if (Main.launcher.getLoginHandler().userBanned(v[1])) {
                                    String send = HTTP_OK+"";
                                    Cookie cook = new Cookie("usr","none; expires=Thu, 01 Jan 1970 00:00:00 GMT","/");
                                    // Cookie cook = new Cookie("usr","none","/");
                                    send+=cook.header();
                                    send+="\r\nyour account has been banned, so you have been logged out";
                                    c.sendMessage(send);
                                    return;
                                }
                            }
                            Cookie coo = new Cookie(v[0],v[1]);
                            cookies.add(coo);
                        }
                    }
                    if (c!=null)
                        c.setCookies(cookies);
                    cookies = new ArrayList<>();
                }
            }
        }
        String response = "";

        if (words[1].contains(".png")) {
            imageRequest=true;
        }

        switch (words[0]) {
            case "GET":
                if (imageRequest) {
                    byte[] resp = response_GET_image(c, words[1], words[2]);
                    sendBytes(c, resp);
                    c.disconnect();
                    return;
                }
                else
                    response = response_GET(c,words[1],words[2]);
                //Console.output("GET:\n"+data+"\n:GET.");
                break;
            case "POST":
                response = response_POST(c,url,lines);
                break;
            case "PUT":
                response = response_PUT(c,url,lines);
                break;
            case "PATCH":
                response = response_PATCH();
                break;
            case "DELETE":
                response = response_DELETE();
                break;
            default:
                response = fileResponse(HTTP_BAD_REQUEST,BAD_REQUEST_PATH);
                break;
        }
        //System.out.println("<response>"+response+"</response>");
        if (!response.equals(WAIT_FOR_RESPONSE)) {
            System.out.println("sending reply to "+c);
            sendMessage(c, response);
        }
    }

    protected String response_GET(ServerConnection c,String uri, String version) {

        uri = uri.replace("//","/");
        String path = fullPath(uri);
        path = path.replace("//","/");
        Console.output("[Request] GET "+path+" from "+c);
        if (uri.equals("/")) {
            Response R = new Response(fileContents(INDEX_PATH));
            return R.response();// return fileResponse(HTTP_OK, INDEX_PATH);
        }
        else {
            if (uri.contains("favicon")) {
                return HTTP_OK+"\r\nnofavicon";
            }
            File f = new File(path);
            if (f.exists()) {
                if (uri.contains("play.js")) {
                    String rspHead =  HTTP_OK_JS+"\r\n";
                    String adr = Main.launcher.getConfig("ws_addr");
                    String rspIPLine = adr.equalsIgnoreCase("DNE") ? "ws://127.0.0.1:"+Main.launcher.getWebsocketServer().getPort()+"/ws" : adr;
                    rspIPLine = "var wsUri = \""+rspIPLine+"\";";
                    String rspBody = fileData(fullPath(uri));
                    String rsp = rspHead+rspIPLine+rspBody;
                    return rsp;
                } else {
                    if ( uri.contains("menu.js") || uri.contains("player.js")
                            || uri.contains("sprite.js") || uri.contains("spritestore.js") || uri.contains("button.js")
                            || uri.contains("text.js")) {
                        String dx = fileResponse(HTTP_OK_JS/*+"Content-Type: text/javascript\r\n"*/, uri);
                       // System.out.println(dx);
                        return dx;
                    }
                    if (uri.contains(".css")) {
                        String dx = fileResponse(HTTP_OK_CSS/*+"Content-Type: text/javascript\r\n"*/, uri);
                       // System.out.println(dx);
                        return dx;
                    }
                    return fileResponse(HTTP_OK, uri);
                }
            }
            else {
                Console.output("Resource not found! "+path);
                return fileResponse(HTTP_NOT_FOUND, NOT_FOUND_PATH);
            }
        }

    }

    protected byte[] response_GET_image(ServerConnection c,String uri, String version) {
        uri = uri.replace("//","/");
        Console.output("[Request IMG] GET "+uri+" from "+c);
        if (uri.equals("/")) {
            return (new String(HTTP_NOT_FOUND+"\r\n"+fileContents(NOT_FOUND_PATH))).getBytes();
        }
        else {
            if (uri.contains("favicon")) {
                return (new String(HTTP_OK+"\r\nnofavicon")).getBytes();
            }
            String filePath = "res/front/"+uri;
            File f = new File(filePath);
            if (f.exists()) {
                String head = HTTP_OK+"Content-Type: image/png\r\n\r\n";
                byte[] fr = imageData(filePath);
                byte[] hdr = head.getBytes();
                byte[] sendme = new byte[fr.length+hdr.length];
                int j=0;
                for (int i=0; i<hdr.length; i++) {
                    sendme[j++]=hdr[i];
                }
                for (int i=0; i<fr.length; i++) {
                    sendme[j++]=fr[i];
                }
                return sendme;
            }
            else {
                Console.log("resource not found: "+filePath);
                return (new String(HTTP_NOT_FOUND + "\r\n" + fileContents(NOT_FOUND_PATH))).getBytes();
            }
        }

    }

    protected String response_POST(ServerConnection c, String uri, String[] lines) {
        Console.output("[Request] POST "+uri+" from "+c);
        String[] buildMeDaddy = new String[69420];
        int howmany = 0;
        int startReading = 0;
        for (String L: lines) {
            if (L.startsWith("packet=")) {
                Console.output("[POST] '"+L+"'");
                Main.launcher.getServer().getWebPacketHandler().processWebPacket(c,uri,L);
                c.setNeedsReply(true);
                return WAIT_FOR_RESPONSE;
            }
        }
        String theFuckingAnswer = HTTP_OK+"\r\nretry";
        return theFuckingAnswer;
    }

    protected String response_PUT(ServerConnection c, String uri, String[] lines) {
        if (c==null) {
            return "null";
        }
        Console.output("[Request] PUT "+uri+" from "+c);
        for (String L: lines) {
            if (L.startsWith("packet=")) {
                Console.output("[PUT] '"+L+"'");
                Main.launcher.getServer().getWebPacketHandler().processWebPacket(c,uri,L);
                c.setNeedsReply(true);
                return WAIT_FOR_RESPONSE;
            }
        }
        return HTTP_OK+"\r\nretry";
        //return notSupported("PUT");
    }

    @Override
    public void processCustomMessage(ServerConnection c, String data) {

    }


    protected String response_PATCH() {
        return notSupported("PATCH");
    }
    protected String response_DELETE() {
        return notSupported("DELETE");
    }

    protected void sendBadRequest(ServerConnection c)  {
        sendMessage(c,fileResponse(HTTP_BAD_REQUEST,BAD_REQUEST_PATH),false);
    }

    @Override
    public void sendText(ServerConnection c, String s) {
        try {
            sendMessage(c, HTTP_OK + "\r\n" + s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //c.disconnect();
    }

    protected String fileData(String path)  {
       // path = path.replace("//","/");
        //String str = FileUtils.readFileToString(file);
        try {
            String str = Files.readString(Paths.get(path));
            return str;
        } catch (Exception e) {
            System.out.println("Error finding path "+path);
            e.printStackTrace();
        }
        return "";
    }

    protected byte[] imageData(String path)  {
        // path = path.replace("//","/");
        //String str = FileUtils.readFileToString(file);
        try {
            byte[] dat = Files.readAllBytes(Paths.get(path));
            return dat;
        } catch (Exception e) {
            System.out.println("Error reading from path "+path);
            e.printStackTrace();
        }
        return (new String("")).getBytes();
    }

    protected String notSupported(String nos) {
        String build = HTTP_OK;
        build+=fileData(fullPath(NOT_SUPPORTED_PATH));
        build+="    <p>Request type: "+nos+" </p>\n</body>\n</html>\n";
        return build;
    }

    protected String fullPath(String rel) {
        return indexDirectory+""+rel;
    }

    protected String fileResponse(String head, String relPath) {
        return head+"\r\n"+fileData(fullPath(relPath));
    }

    protected String fileContents(String path) {
        return fileResponse("",path);
    }


    public int getPort() {
        return port;
    }
    public int getID() {
        return Protocol.HTTP;
    }
    public String getName() {
        return "HTTP";
    }

    private static final String INDEX_PATH = "index.html";
    private static final String BAD_REQUEST_PATH = "pages/status/400.html";
    private static final String NOT_FOUND_PATH = "pages/status/404.html";
    private static final String NOT_SUPPORTED_PATH = "pages/status/nosupport.html";

    public static final String HTTP_OK = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\n";
    public static final String HTTP_OK_JS = "HTTP/1.1 200 OK\r\nContent-Type: text/javascript\r\n";
    public static final String HTTP_OK_CSS = "HTTP/1.1 200 OK\r\nContent-Type: text/css\r\n";
    public static final String HTTP_BAD_REQUEST = "HTTP/1.1 400 BAD REQUEST\r\nContent-Type: text/html\r\n";
    public static final String HTTP_NOT_FOUND = "HTTP/1.1 404 Not Found\r\nContent-Type: text/html\r\n";

}