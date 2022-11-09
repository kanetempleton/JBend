package com.server.web.pages;
import com.util.*;

public class PageBuilder {

    private String ui,html;
    private String title,desc;

    public static String load(String filename) {
        PageBuilder page = new PageBuilder(filename);
        page.process();
        return page.html;
    }

    public PageBuilder(String filename) {
        ui = FileManager.fileDataAsString("res/front/"+filename);
        html = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n";
        title=null;
        desc=null;
    }

    public void process() {
        generateHeader();
        generateBody();
        html += "</html>\n";
    }


    // header methods
    private void generateHeader() {
        html += "<head>\n";

        loadTitle();
        loadKeywords();

        html += "</head>\n";

        ui = StringUtils.deleteLine(ui,2);
        ui = StringUtils.deleteLine(ui,1);
        ui = StringUtils.deleteLine(ui,0);
    }

    private void loadTitle() {
        String[] lines = ui.split("\n");
        title=lines[0];
        desc=lines[1];
        html += "<title>"+title+"</title>\n";
        html += "<meta name=\"description\" content=\""+desc+"\">\n";

    }

    private void loadKeywords() {
        String[] lines = ui.split("\n");
        String[] keyz=lines[2].replace("[","").replace("]","").split(",");
        html += "<meta name=\"keywords\" content=\""+Tools.comma_string(keyz)+"\">\n";
    }




    //body methods
    private void generateBody() {
        html += "<body>\n";

        loadScripts();
        loadSiteNavigation();
        loadBodyHTML();


        html += "</body>\n";
    }

    private void loadScripts() {

    }

    private void loadSiteNavigation() {

        String navhtml = "<nav>\n<ul>\n" +
                "<li><a href=\"/\">Home</a></li>\n";
        navhtml += "</ul>\n</nav>\n";
        ui = ui.replace("[navbar]",navhtml);
    }

    private void loadBodyHTML() {
        replaceBuilderFunctions();
        html += ui;
    }

    private void replaceBuilderFunctions() {
        String[] lines = ui.split("\n");
        for (int i=0; i<lines.length; i++) {
            if (lines[i].startsWith("$[") && lines[i].endsWith("]")) {
                lines[i] = translateCode(lines[i].substring(2,lines[i].length()-2));
            }
        }
        ui = StringUtils.linesToText(lines);
    }

    private String translateCode(String code) {
        String F = "";
        String A = "";
        String function = "";
        String args = "";
        boolean open = false;
        for (int i=0; i<code.length();i++) {
            if (code.charAt(i) == '(') {
                open = true;
            }
            else if (code.charAt(i) == ')') {
                open = false;
                F = F.length() == 0 ? function : F + "," + function;
                A = A.length() == 0 ? args : A + ","+args;
                function = "";
                args = "";
            }
            else if (open) {
                args += code.charAt(i);
            } else {
                function += code.charAt(i);
            }
        }
        String out = "";
        String[] functions = F.split(",").length==0 ? new String[]{F} : F.split(",");
        String[] arguments = A.split(",").length==0 ? new String[]{A} : A.split(",");
        if (functions.length != arguments.length) {
            return "<p> PageBuilder Syntax Error for "+code+" </p>\n";
        }
        for (int i=0; i<functions.length; i++) {
            out += processFunction(functions[i],arguments[i]);
        }
        return out;
    }

    private String processFunction(String f, String args) {
        return "<p> PageBuilder Placeholder for Function: "+f+"("+args+") </p>\n";
    }



}


/* SPECIFICATION

    .ui file:
    - First 3 lines must follow the following format format:
    Page title text
        Page description
        [keyword1, keyword2, keyword3]

    - The 4th line can optionally be [navbar] to include site navigation at the top of page
    - Each line in the remainder of the file must either be:
        - html code
        - page builder function (specified below)

    Page builder functions: $[function1(args11 args12) function2(args21 args22)]
    - this sequence must be the only text contents of the line it is called in
    - the final character in the line must be ']' (no spaces or tabs at end)
    - no commas allowed

 */