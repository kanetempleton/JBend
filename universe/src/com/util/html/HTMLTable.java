package com.util.html;

import java.util.Arrays;

public class HTMLTable {

    private String element_id;
    private String[] column_names;
    private String[][] column_data;
    private String[][] column_output;

    private String tableStyle;

    public HTMLTable(String id, String[] colNames, String[][] data) {
        element_id=id;
        column_names=new String[colNames.length];
        int i=0;
        for (String s: colNames) {
            column_names[i++] = s;
        }
        i=0;
        int j=0;
        column_data=new String[data.length][colNames.length];
        for (String[] col : data) {
            for (String d : col) {
                column_data[i][j++] = d;
            }
            j=0;
            i++;
        }
        column_output = new String[data.length][colNames.length];
        for (String[] col : data) {
            for (String d : col) {
                column_output[i][j++] = d;
            }
            j=0;
            i++;
        }
        tableStyle = "";
    }

    public HTMLTable(String[] colNames, String[][] data) {
        element_id=null;
        column_names=new String[colNames.length];
        int i=0;
        for (String s: colNames) {
            column_names[i++] = s;
        }
        i=0;
        int j=0;
        column_data=new String[data.length][colNames.length];
        for (String[] col : data) {
            for (String d : col) {
                column_data[i][j++] = d;
            }
            j=0;
            i++;
        }
        i=0;
        j=0;
        column_output = new String[data.length][colNames.length];
        for (String[] row : data) {
            for (String d : row) {
                column_output[i][j++] = d;
            }
            j=0;
            i++;
        }
        tableStyle = "";
    }


    public void appendStyle(String style) {
        tableStyle += " "+style;
    }

    public void addTableBorders(int px, String color) {
        tableStyle += " border='"+px+"px "+color+"'";
    }

    public void addBasicBorders() {
        addTableBorders(1,"solid black");
    }

    public void appendColumnToEnd(String colName, String colVal) {
        String[] names2 = new String[column_names.length+1];
        String[][] data2 = new String[column_data.length][column_names.length+1];
        String[][] out2 = new String[column_data.length][column_names.length+1];
        for (int i=0; i<column_names.length; i++) {
            names2[i] = column_names[i];
        }
        names2[column_names.length] = colName;
        for (int i=0; i<column_data.length; i++) {
            for (int j=0; j<column_names.length+1; j++) {
                if (j==column_names.length) {
                    data2[i][j] = colVal;
                    out2[i][j] = colVal;
                } else {
                    data2[i][j] = column_data[i][j];
                    out2[i][j] = column_output[i][j];
                }
            }
        }
        column_names = Arrays.copyOf(names2,names2.length);
        column_data = Arrays.copyOf(data2,data2.length);
        column_output = Arrays.copyOf(out2,out2.length);

    }

    public void printColumnData() {
        for (int i=0; i<column_data.length; i++) {
            for (int j=0; j<column_data[i].length; j++) {
                System.out.println("[HTMLTable] column_data[i][j]="+column_data[i][j]);
            }
        }
    }

    public void addFormToRow(String colHead, String rowMatch) {
        int x = 0;
        for (int i=0; i<column_names.length; i++) {
            if (column_names[i].equals(colHead)) {
                x=i;
                break;
            }
        } //column[i][x] = data entry i for column x
        int y = 0;
        for (int i=0; i<column_names.length; i++) {
            if (column_data[i][x].equals(rowMatch)) {
                y=i;
                break;
            }
        }
        for (int i=0; i<column_data[y].length; i++) {
            column_output[y][i] = "<input id=\"form_"+column_names[i]+"\" type=\"text\" value=\""+column_data[y][i]+"\">";
        }
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
           // System.out.println("what the fuck column_data[i][x]="+column_data[i][x]);
            column_output[i][x] = "<a href=\""+uri+"?"+column_names[x]+"="+column_data[i][x]+"\">"+column_output[i][x]+"</a>";
           // System.out.println("what the fuck 2 column_data[i][x]="+column_data[i][x]);
            //<a href=\"tickets.html?id="+this.responseParamValue(i,"ticket_id")+"\">
        }
    }
    public void addHrefToColumn(String colHead, String uri, String colDat) {
        int x = 0;
        for (int i=0; i<column_names.length; i++) {
            if (column_names[i].equals(colHead)) {
                x=i;
                break;
            }
        }
        int y = 0;
        for (int i=0; i<column_names.length; i++) {
            if (column_names[i].equals(colDat)) {
                y=i;
                break;
            }
        }
        for (int i=0; i<column_output.length; i++) {
         //   System.out.println("column_data[i][y]="+column_data[i][y]);
            column_output[i][x] = "<a href=\""+uri+"?"+column_names[x]+"="+column_data[i][y]+"\">"+column_output[i][x]+"</a>";
            //<a href=\"tickets.html?id="+this.responseParamValue(i,"ticket_id")+"\">
        }
    }


    //col[toThisCol] gets href to refTo?colDatName=colDatValue
    //toThisCol: name of column to add link to
    //refTo: uri to link to
    //colDatName: data for
    public void addHrefToColumn(String toThisCol, String refTo, String colDatName, String colDatValue) {
        int x = 0;
        for (int i=0; i<column_names.length; i++) {
            if (column_names[i].equals(toThisCol)) {
                x=i;
                break;
            }
        }
        int y = 0;
        for (int i=0; i<column_names.length; i++) {
            if (column_names[i].equals(colDatName)) {
                y=i;
                break;
            }
        }
       /* int z = 0;
        for (int i=0; i<column_names.length; i++) {
            if (column_names[i].equals(colDatValue)) {
                z=i;
                break;
            }
        }*/
        for (int i=0; i<column_output.length; i++) {
            //   System.out.println("column_data[i][y]="+column_data[i][y]);
            column_output[i][x] = "<a href=\""+refTo+"?"+colDatName+"="+column_data[i][y]+"\">"+column_output[i][x]+"</a>";
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
