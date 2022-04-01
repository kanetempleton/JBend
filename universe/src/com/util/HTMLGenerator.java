package com.util;

public class HTMLGenerator {

    public HTMLGenerator() {

    }

    public static String generateTable(String id, String[] colNames, String[][] data) {
        String build = "";
        if (id!=null && id.length()>0) {
            build = "<table id="+id+">";
        } else {
            build = "<table>";
        }
        if (colNames.length > 0 && data.length > 0 && data[0].length >0 ) {
            build += "<tr>";
            for (String c: colNames) {
                build+="<th>"+c+"</th>";
            }
            build += "</tr>";
            for (int i=0; i<data.length; i++) {
                if (data[i].length == colNames.length) {
                    build += "<tr>";
                    for (String c: data[i]) {
                        build+="<th>"+c+"</th>";
                    }
                    build += "</tr>";
                }
            }
        }
        return build+"</table>";
    }

    public static String generateTable(String[] colNames, String[][] data) {
        return generateTable("",colNames,data);
    }
}
