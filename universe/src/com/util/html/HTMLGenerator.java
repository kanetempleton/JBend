package com.util.html;

public class HTMLGenerator {

    public HTMLGenerator() {

    }

   /* public static String generateTable(String id, String[] colNames, String[][] data) {

        System.out.println("printing colNames[]...");
        for (int i=0; i<colNames.length; i++) {
            System.out.print(colNames[i]+",");
        }
        System.out.println("\ndone");
        System.out.println("printing data[][]...");
        for (int i=0; i<data.length; i++) {
            for (int j=0; j<data[i].length; j++) {
                System.out.print(data[i][j]+",");
            }
            System.out.println();
        }
        System.out.println("done");
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
                System.out.println("processing data: "+i);
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
    }*/

   public static String generateTable(String id, String[] colNames, String[][]data) {
       HTMLTable T = new HTMLTable(id,colNames,data);
       return T.toString();
   }
   public static String generateTable(String[] colNames, String[][] data) {
       return generateTable("",colNames,data);
   }

   /*
   public  HTMLTable generateTable(String id, String[] colNames, String[][] data) {
       HTMLTable T = new HTMLTable(id,colNames,data);
       return T;
   }*/
}
