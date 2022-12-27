package com.server.protocol;

import com.server.entity.ServerConnection;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.File;
import com.Main;
import java.util.ArrayList;
import com.server.web.*;
import com.console.*;
import com.util.FileManager;
import com.server.*;
import com.util.Regex;
import com.server.web.pages.PageBuilder;

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


class Route {
    String uri;
    String res;
    Route(String u,String r) {
        uri=u;
        res=r;
    }
    String getURI() {return uri;}
    String getResource() {return res;}
    public String toString() {return uri+":"+res;}
}

public class HTTP extends Protocol {

    public static final String DEFAULT_HOME_DIRECTORY = "res/front/";

    public static final String WAIT_FOR_RESPONSE = "WAIT4RESPONSEDONTSENDTHIS";

    protected String indexDirectory;
    protected ArrayList<Cookie> cookies;
    protected ArrayList<Route> routes;
    private int port;

    public HTTP(String indDir, int prt) {
        super();
        indexDirectory=indDir;
        port=prt;
        cookies = new ArrayList<Cookie>();
        routes = new ArrayList<Route>();
       // loadRoutes();
    }

    public void loadRoutes() {
        String[] lines = FileManager.fileDataAsString("routes.cfg").split("\n");
        for (String l: lines) {
            String[] keyval = l.replace(" ","").split("->");
            if (keyval.length!=2) {
                Console.output("There is an error in your routes.cfg syntax! No routes were loaded.");
                return;
            }
            String uri = keyval[0];
            String rsc = keyval[1];

            if (uri.contains("*")) {
                for (String f: FileManager.listFiles(DEFAULT_HOME_DIRECTORY+""+rsc.split("/")[1])) {
                    System.out.println("file: "+f);
                    System.out.println("file name: "+f.split(".")[0]);
                    System.out.println("file extension: "+f.split(".")[1]);
                    if (Regex.match(f,Regex.FILE_NAME,false)) { // syntax of line: /* -> /dirname/*.html
                        addRoute(uri.replace("*",f.split(".")[0]),rsc.replace("*",f));
                    }
                }
            }
            addRoute(uri,rsc);
            Console.output("Added route: "+uri+"");
        }
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
            if (L.startsWith("Cookie:") || L.contains("cookie:") || L.contains("Cookie:")) {
                if (L.split("ookie: ").length > 1) {
                    String ck = L.split("ookie: ")[1];
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

        //@TODO: add all of the image formats
        //https://developer.mozilla.org/en-US/docs/Web/Media/Formats/Image_types
        if (words[1].contains(".png")||words[1].contains(".jpg")
            || words[1].contains(".JPG")||words[1].contains("jpeg") || words[1].contains("favicon")) {
            imageRequest=true;
        }

        boolean pdfRequest = false;
        if (words[1].contains(".pdf")) {
            pdfRequest = true;
        }

        switch (words[0]) {
            case "GET":
                if (imageRequest) {
                    byte[] resp = response_GET_image(c, words[1], words[2]);
                    sendBytes(c, resp);
                    c.disconnect();
                    return;
                }
                else if (pdfRequest) {
                    byte[] resp = response_GET_pdf(c, words[1], words[2]);
                    sendBytes(c,resp);
                }
                else {
                    //System.out.println("notIMAGE REQUEST");
                  //  System.out.println("showing cookies for GET:");
                    for (Cookie z: c.getCookies()) {
                      //  System.out.println("Cookie:"+z);
                    }
                    response = response_GET(c, words[1], words[2]);
                }
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
           // System.out.println("sending reply to "+c);
            sendMessage(c, response);
            c.disconnect();
        }
    }

    private static String protectedResources0[] = {"timeline","blog"};
    private static String protectedAccessNames[] = {"kane","naomi","anne"};

    protected String response_GET(ServerConnection c,String uri, String version) {


        uri = uri.replace("//", "/");
        String paramString = "";
        if (uri.contains("?")) {
            String[] split = uri.split("\\?");
            if (split.length > 1) {
                paramString = split[1];
                uri = split[0];
            }
        }
        String path = fullPath(uri);
        path = path.replace("//", "/");
        String[] params = new String[]{};
        if (paramString.length() > 0) {
            params = paramString.split("&");
        }
        String pf = "";
        String pv = "";
        boolean permitted = true;
        for (int i = 0; i < protectedResources0.length; i++) {
            if (path.contains(protectedResources0[i])) {
                permitted = false;
            }
        }
        for (String p : params) {
            String[] dat = p.split("=");
            if (dat.length > 1) {
                System.out.println("GET params found: " + dat[0] + " = " + dat[1]);
                pf += dat[0] + ",;,";
                pv += dat[1] + ",;,";
            }

                if (!permitted) {
                    for (int j = 0; j < protectedAccessNames.length; j++) {
                        if (dat[0].equalsIgnoreCase("user") && dat[1].equalsIgnoreCase(protectedAccessNames[j])) {
                            permitted = true;
                            System.out.println("Access granted to user " + dat[1] + " to resource: " + uri);
                        } else {
                            System.out.println("Access denied to user " + dat[1] + " to resource: " + uri + ". Custom login response sent.");
                            return HTTP_BAD_REQUEST + "\r\n" + "The file you tried to access either does not exist, or you are not logged in as an authorized user to view this page.<br>" +
                                    "If you are an authorized user with access, please try revisiting this page with the following link: https://kanetempleton.com/" + uri + "?user=YOUR_USER_ID<br>" +
                                    "Replace the YOUR_USER_ID with the user ID you were given to log in with. This is temporary until I get an actual login system re-working.<br>" +
                                    "You will be able to view your permitted access pages if you access them in this fashion.";

                        }

                    }
                }
            }

            Console.output("[Request] GET " + path + " from " + c.toShortString());
            if (uri.equals("/")) {
                Response R = new Response(fileContents(INDEX_PATH));
                return R.response();// return fileResponse(HTTP_OK, INDEX_PATH);
            } else {
                if (uri.contains("favicon")) {
                    return HTTP_OK + "\r\nnofavicon";
                }
           /* String routes = checkRoutes(uri);
            if (routes!=null) {
                return routes;
            }*/
                String route = route(uri);
                // uri = route(uri); //use routes
                path = fullPath(route);
                File f = new File(path);
                if (f.exists()) { //only do this if we allow direct GETs
                    byte[] custResponse;
                    if (pf.split(",;,").length > 0 && pf.split(",;,")[0].length() > 0 && pv.split(",;,").length > 0 && pv.split(",;,")[0].length() > 0) {
                        custResponse = processGET(c, uri, route, pf.split(",;,"), pv.split(",;,"));
                    } else {
                        custResponse = processGET(c, uri, route, new String[]{}, new String[]{});
                    }
                    // byte[] custResponse = processGET(c,uri,pf.split(",;,"),pv.split(",;,"));
                    if (custResponse != null) {
                        return new String(custResponse);
                    }
                    if (uri.contains("play.js")) {
                        String rspHead = HTTP_OK_JS + "\r\n";
                        String adr = Main.launcher.getConfig("ws_addr");
                        String rspIPLine = adr.equalsIgnoreCase("DNE") ? "ws://127.0.0.1:" + Main.launcher.getWebsocketServer().getPort() + "/ws" : adr;
                        rspIPLine = "var wsUri = \"" + rspIPLine + "\";";
                        String rspBody = fileData(fullPath(uri));
                        String rsp = rspHead + rspIPLine + rspBody;
                        return rsp;
                    } else {
                        if (route.contains(".js")) {
                            String dx = fileResponse(HTTP_OK_JS/*+"Content-Type: text/javascript\r\n"*/, route);
                            // System.out.println(dx);
                            return dx;
                        }
                        if (route.contains(".css")) {
                            String dx = fileResponse(HTTP_OK_CSS/*+"Content-Type: text/javascript\r\n"*/, route);
                            // System.out.println(dx);
                            return dx;
                        }
                        if (route.contains(".ui")) {
                            return PageBuilder.load(route);
                        }

                        // default: standard 200 OK response with an html page
                        return fileResponse(HTTP_OK, route);
                    }
                } else {
                    Console.output("Resource not found! " + path);
                    return fileResponse(HTTP_NOT_FOUND, NOT_FOUND_PATH);
                }
            }

    }

    public String fileHTML(String uri) {
        return fileData(fullPath(uri));
    }

    //multiHTMLResponse(HTTP_OK, "jizz-time:69", new String[]{"<html>extra shit</html>",fileHTML(uri)});

    // extra header with no \r\n
    public  String multiHTMLResponse(String header, String[] html) {
        String out = header+"\r\n";
        for (String h: html) {
            out += h;
        }
        return out;
    }

    public String multiHTMLResponse_noTags(String header, String[] html) {
        String out = header+"\r\n";
        out += "<!doctype html><html>";
        for (String h: html) {
            out += h;
        }
        out +="</html>";
        return out;
    }

    public String fileHTML_noTags(String uri) {
        String out = fileData(fullPath(uri));
        out = out.replace("<!doctype html>","");
        out = out.replace("<html>","");
        out = out.replace("</html>","");
        return out;
    }

    public byte[] processGET(ServerConnection c, String uri, String resource, String[] paramFields, String[] paramValues) {
        return null;
    }

    //return the uri if no route exists, else return the resource for the route
    private String route(String uri) {
        for (Route r: routes) {
            String relURI = uri.replace(DEFAULT_HOME_DIRECTORY,"");
          //  System.out.println("route: "+r);
          //  System.out.println("relURI="+relURI+" ; r.uri = "+r.getURI());
            if (relURI.equalsIgnoreCase(r.getURI()) ||
                    relURI.equalsIgnoreCase(r.getURI()+"/")) {
                return r.getResource();
            }
        }
        return uri;
    }

    // private GET methods
    private String checkRoutes(String uri) {
    //    System.out.println("checking route: "+uri.replace(DEFAULT_HOME_DIRECTORY,""));
        for (Route r: routes) {
            String relURI = uri.replace(DEFAULT_HOME_DIRECTORY,"");
          //  System.out.println("route: "+r);
          //  System.out.println("relURI="+relURI+" ; r.uri = "+r.getURI());
            if (relURI.equalsIgnoreCase(r.getURI())) {
            //    System.out.println("checking fullpath: "+fullPath(r.getResource()));
                return HTTP_OK+"\r\n"+fileData(fullPath(r.getResource()));
            }
        }
        return null;
    }



    protected byte[] response_GET_image(ServerConnection c,String uri, String version) {
        uri = uri.replace("//","/");
        Console.output("[Request IMG] GET "+uri+" from "+c.toShortString());
        if (uri.equals("/")) {
            return (new String(HTTP_NOT_FOUND+"\r\n"+fileContents(NOT_FOUND_PATH))).getBytes();
        }
        else {
           /* if (uri.contains("favicon")) {
                return (new String(HTTP_OK+"\r\nnofavicon")).getBytes();
            }*/
            String filePath = "res/front/"+uri;
            File f = new File(filePath);
            if (f.exists()) {
//Transfer-Encoding: chunked
                byte[] fr = imageData(filePath);
                String head = "HTTP/1.1 200 OK\r\nContent-Type: image/jpeg\r\nConnection: Keep-Alive\r\nContent-Length: "+fr.length+"\r\n\r\n";
               // String head = "HTTP/1.1 200 OK\r\nContent-Type: image/png\r\n\r\n";
                byte[] hdr = head.getBytes();
                byte[] sendme = new byte[fr.length+hdr.length];
                int j=0;
                for (int i=0; i<hdr.length; i++) {
                    sendme[j++]=hdr[i];
                }
                for (int i=0; i<fr.length; i++) {
                    sendme[j++]=fr[i];
                }
              //  System.out.println("sendme=\n"+(new String(sendme)).substring(0,hdr.length+1));
                Server.debug_global(1,"Image Response length: "+sendme.length);
                Server.debug_global(2,"Image Response header: "+head);
                return sendme;
            }
            else {
                Console.log("resource not found: "+filePath);
                return (new String(HTTP_NOT_FOUND + "\r\n" + fileContents(NOT_FOUND_PATH))).getBytes();
            }
        }

    }

    protected byte[] response_GET_pdf(ServerConnection c,String uri, String version) {
        uri = uri.replace("//","/");
        Console.output("[Request PDF] GET "+uri+" from "+c.toShortString());
        if (uri.equals("/")) {
            return (new String(HTTP_NOT_FOUND+"\r\n"+fileContents(NOT_FOUND_PATH))).getBytes();
        }
        else {
            String filePath = "res/front/"+uri;
            File f = new File(filePath);
            if (f.exists()) {
//Transfer-Encoding: chunked
                byte[] fr = imageData(filePath);
                String head = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\nConnection: Keep-Alive\r\nContent-Length: "+fr.length+"\r\n\r\n";
                // String head = "HTTP/1.1 200 OK\r\nContent-Type: image/png\r\n\r\n";
                byte[] hdr = head.getBytes();
                byte[] sendme = new byte[fr.length+hdr.length];
                int j=0;
                for (int i=0; i<hdr.length; i++) {
                    sendme[j++]=hdr[i];
                }
                for (int i=0; i<fr.length; i++) {
                    sendme[j++]=fr[i];
                }
                //  System.out.println("sendme=\n"+(new String(sendme)).substring(0,hdr.length+1));
                Server.debug_global(1,"PDF Response length: "+sendme.length);
                Server.debug_global(2,"PDF Response header: "+head);
                return sendme;
            }
            else {
                Console.log("resource not found: "+filePath);
                return (new String(HTTP_NOT_FOUND + "\r\n" + fileContents(NOT_FOUND_PATH))).getBytes();
            }
        }

    }


    protected String response_POST(ServerConnection c, String uri, String[] lines) {
        Console.output("[Request] POST "+uri+" from "+c.toShortString());
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
        Console.output("[Request] PUT "+uri+" from "+c.toShortString());
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

    protected static String fileData(String path)  {
        path = path.replace("//","/");
        //String str = FileUtils.readFileToString(file);
        try {
            //System.out.println("path:"+path);
            String str = Files.readString(Paths.get(path));
            //str = str.replace("//","/");
            return str;
        } catch (Exception e) {
            Path currentRelativePath = Paths.get("");
            String s = currentRelativePath.toAbsolutePath().toString();
            //System.out.println("Current relative path is: " + s);
            System.out.println("[fileData]Error finding path "+path);
            e.printStackTrace();
        }
        return "";
    }

    protected byte[] imageData(String path)  {
         path = path.replace("//","/");
        //String str = FileUtils.readFileToString(file);
        try {
            byte[] dat = Files.readAllBytes(Paths.get(path));
            return dat;
        } catch (Exception e) {
            Path currentRelativePath = Paths.get("");
            String s = currentRelativePath.toAbsolutePath().toString();
           // System.out.println("Current relative path is: " + s);
            System.out.println("[imageData]Error reading from path "+path);
            e.printStackTrace();
        }
        return (new String("")).getBytes();
    }

    public void addRoute(String uri, String res) {
        routes.add(new Route(uri,res));
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

    public static final String HTTP_OK = "HTTP/1.1 200 OK\r\nContent-Type: module\r\n";
    public static final String HTTP_OK_JS = "HTTP/1.1 200 OK\r\nContent-Type: text/javascript\r\n";
    public static final String HTTP_OK_CSS = "HTTP/1.1 200 OK\r\nContent-Type: text/css\r\n";
    public static final String HTTP_BAD_REQUEST = "HTTP/1.1 400 BAD REQUEST\r\nContent-Type: text/html\r\n";
    public static final String HTTP_NOT_FOUND = "HTTP/1.1 404 Not Found\r\nContent-Type: text/html\r\n";

}