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
        ui = FileManager.fileDataAsString(filename);
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
        html += ui;
    }



}