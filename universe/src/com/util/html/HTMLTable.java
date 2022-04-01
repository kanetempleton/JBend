package com.util.html;

public class HTMLTable {

    private String element_id;
    private String[] column_names;
    private String[][] column_data;
    private String[][] column_output;

    private String tableStyle;

    public HTMLTable(String id, String[] colNames, String[][] data) {
        element_id=id;
        column_names=colNames;
        column_data=data;
        column_output = data;
        tableStyle = "";
    }

    public HTMLTable(String[] colNames, String[][] data) {
        element_id=null;
        column_names=colNames;
        column_data=data;
        column_output = data;
        tableStyle = "";
    }

    public void addTableBorders(int px, String color) {
        tableStyle = "border='"+px+"px "+color+"'";
    }

    public void addBasicBorders() {
        addTableBorders(1,"solid black");
    }

    public void addHrefToColumn(String colHead, String uri) {
        int x = 0;
        for (int i=0; i<column_names.length; i++) {
            if (column_names[i].equals(colHead)) {
                x=i;
                break;
            }
        }
        for (int i=0; i<column_output.length; i++) {
            column_output[i][x] = "<a href=\""+uri+"?"+column_names[x]+"="+column_data[i][x]+"\">"+column_output[i][x]+"</a>";
            //<a href=\"tickets.html?id="+this.responseParamValue(i,"ticket_id")+"\">
        }
    }


    public String toString() {
        String build = "<table";
        if (element_id!=null && element_id.length()>0) {
            build += " id='"+element_id+"'";
        }
        if (tableStyle!=null && tableStyle.length()>0) {
            build += " "+tableStyle;
        }
        build += ">";
        if (column_names.length > 0 && column_output.length > 0 && column_output[0].length >0 ) {
            build += "<tr>";
            for (String c: column_names) {
                build+="<th>"+c+"</th>";
            }
            build += "</tr>";
            for (int i=0; i<column_output.length; i++) {
                if (column_output[i].length == column_names.length) {
                    build += "<tr>";
                    for (String c: column_output[i]) {
                        build+="<th>"+c+"</th>";
                    }
                    build += "</tr>";
                }
            }
        }
        return build+"</table>";
    }

}
