// ConfLexer.java
// the lexer for my configuration scripts
// process of a programming language is as follows:
// Lexer: converts text into tokens
// Parser: defines tokens and identifies token syntax patterns

//NOTE: this is a massive work in progress and this does not currently work at all

// code --> [ Lexer ] --> tokens --> [ Parser ] --> syntax --> [ Interpreter ] --> execution

package com.lang.conf;

public class ConfLexer {

  //  private HashMap<String,String> tokenMap;

    public ConfLexer() {
  //      tokenMap = new HashMap<>();
//        tokenMap.put("app-id","APPDEF");
//        tokenMap.put("")
    }



    public static String parse(String input) {
        String lines[] = input.replace("\r","").split("\n");
        String out = "";
     //   ArrayList<ConfToken> tokens = new ArrayList<>();
        boolean firstline=true;
        for (int i=0; i<lines.length; i++) {
           // System.out.println("line (len="+lines[i].length()+"): "+lines[i]);
            if (firstline) {
                if (lines[i].startsWith("app-id:")) {
                    if (lines[i].split(":").length==2) {
                        out += "APPDEF "+lines[i].split(":")[1].replace(" ","")+" ";
                        firstline=false;
                        continue;
                //        tokens.add()
//                        out += tokenMap.get("app-id")+" "+
                    } else {return "ERROR";}
                } else {return "ERROR";}
            } else { //process other lines
                if (lines[i].length()==0) {
                    out += "BLANKLINE ";
                }
                else if (lines[i].replace(" ","").endsWith(":")) { //definition lines
                    if (lines[i].replace(" ","").split(":").length==1) { //def line
                        out += "DEF "+lines[i].split(":")[0]+" ";
                    }
                }
                else { //lines inside definition lines
                    if (lines[i].contains("EOF")) {
                        out += "EOF";
                    } else {
                        out += "FUNC ";
                        for (String w : lines[i].split(" ")) {
                            out += w + " ";
                        }
                    }
                }
            }
        }
        return out;
    }
}