package com.db.crud.lang;


import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.util.Regex;

//1. string -> tokens
// s -> Lexer -> Parser -> Interpreter
public class Lexer {

    private String[] lines;
    private int position;
    private String[][] outputData;
    private Token[][] outputTokens;

    public Lexer(String[] inputLines) {
        lines = inputLines;
        position = 0;
        outputData = new String[lines.length][];
        outputTokens = new Token[lines.length][];
    }

    //task=[name:text;p,complete:bool]
    public void processLine() {
        String line = nextLine();
        System.out.println("lexer testing line {"+line+"}");
        System.out.println(Regex.word().match(line));
        int cpos = 0;
        String build = "";
        Token[] outToken = new Token[line.length()];
        String[] outString = new String[line.length()];
        int outSizeStr = 0;
        int outSizeTok = 0;
        Token curTok = Token.UNDEFINED;
        boolean clear = false;
        while (cpos<line.length()) {

            System.out.println("build is "+build);

            if (clear) {
                clear=false;
                build = "";
                System.out.println("reset build");
            }

            String c = ""+line.charAt(cpos++);

            if (Regex.match(" ",c)) {
                continue; //ignore spaces
            }
            if (Regex.word().match(build+c,false)) {
                build+=c;
                curTok = Token.STRING;
                continue;
            } else {
                if (build.length()>0) {
                    clear = true;
                    outString[outSizeStr++]=build;
                    outToken[outSizeTok++]=curTok;
                }
            }
            if (Regex.match("=",c)) {
              //  outString[outSizeStr++]=build;
               // outToken[outSizeTok++]=curTok;
               // build="";
                curTok = Token.EQUALS;
                outToken[outSizeTok++]=curTok;
                continue;
            }
            if (Regex.match("\\[",c)) {
                curTok = Token.BRACKET_OPEN;
                outToken[outSizeTok++] = curTok;
                continue;
            }
            if (Regex.match("\\:",c)) {
                curTok = Token.COLON;
                outToken[outSizeTok++] = curTok;
                continue;
            }
            if (Regex.match("\\;",c)) {
                curTok = Token.SEMICOLON;
                outToken[outSizeTok++] = curTok;
                continue;
            }
            if (Regex.match("\\,",c)) {
                curTok = Token.COMMA;
                outToken[outSizeTok++] = curTok;
                continue;
            }
            if (Regex.match("\\]",c)) {
                curTok = Token.BRACKET_CLOSE;
                outToken[outSizeTok++] = curTok;
                continue;
            }
        }
        outputData[position-1] = new String[outSizeStr];
        outputTokens[position-1] = new Token[outSizeTok];
        for (int i=0; i<outSizeStr; i++)
            outputData[position-1][i]=outString[i];
        for (int i=0; i<outSizeTok; i++)
            outputTokens[position-1][i]=outToken[i];

        System.out.println("Line processing complete. Results:");
        System.out.print("Data=[");
        for (String s: outputData[position-1]) {
            System.out.print("\""+s+"\",");
        }
        System.out.print("]\nTokens=[");
        for (Token t: outputTokens[position-1]) {
            System.out.print("\""+t.toString()+"\",");
        }
        System.out.println("]");
    }

    public boolean hasNextLine() {
        return position<lines.length;
    }

    public String nextLine() {
        if (hasNextLine())
            return lines[position++];
        return "[LEXER ERROR] No more lines!";
    }

    public void throwError(String msg) {
        System.out.println("[LEXER ERROR] "+msg);
    }


    public String[] outputData() {
        return outputData[position-1];
    }
    public Token[] outputTokens() {
        return outputTokens[position-1];
    }

    //how to handle list args?
    //[a:b,c:d,...] treat as one arg until comma?

    /*

        Expression: TEXT EQUALS ARGS or TABLE EQUALS ARGS? prob text
                    TEXT OP ARGS



     */


}
