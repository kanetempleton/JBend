package com.db.crud.lang;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Map tokens -> functions or abstract syntax?
// absyn = {type,expr,value}
// type = {}
// expr = {}
// value = (type,data)
// abstract syntax goes to interpreter
public class Parser {

    private String[] inputData;
    private Token[] inputTokens;
    private String[] outputData;
    private Syntax[] outputSyntax;
    private Object[] outputAux;

    public Parser(String[] inS, Token[] inT) {
        inputData = inS;
        inputTokens = inT;
    }

    //task=[name:text;p,complete:bool]
    //data: [task,name,text,p,complete,bool]
    //tok:  [STRING,EQUALS,BROPEN,STRING,COLON,STRING,SEMICOLON,STRING,COMMA,STRING,COLON,STRING,BRCLOSE
    //Syn = [Type,Op,Type]
    //Aux = [Table,Equal,List,DatType,Name,Mod,DatType,Name]
    // syn=[table, equal, list]
    // aux=[arg,mod,arg]
    //  : List = BROPEN ListArgs BRCLOSE
    //  : ListArgs = STRING COLON STRING Mods | ListArgs COMMA ListArgs
    //  : Mods = SEMICOLON STRING
    // --> Expr = [equal(table,list)]
    //              Expr.eval()
    //
    // type,expr,list
    // expr = type op type

    //outSyn = [TYPE,OP,TYPE]
    //outVal = [Table,Equals,List]
    //outDat = [task,name,text,p,complete,bool]

    /*
        STRING -> type:name
        EQUALS -> expression:eq
        BROPEN -> type:list

        [type,expr,type]
        [


    Data=["task","name","text","p","complete","bool",]
    Syn=[type,op,type] -> matches expression "eql"
    Aux=[table,equal,list]

    from Syn: type
        -> Aux: table   value(table,'task')
        Syn: op
        -> Aux: equal
        Syn: type
        -> Aux: list

    {text}                      -> Lexer ->                 ({tokens},{data})
    ({tokens},{data})           -> Parser ->                ({syntax},{aux},{data})
    ({syntax},{aux},{data})     -> Interpreter ->           ({expression},{data})
    ({expression},{data})       -> Evaluator ->             <action>

    check if Syn matches an expression
    -> matches a new eql expr
    --> convert aux to expression
    ---> eql(table,list)

    create expression from aux
    -> expr(data,aux)




     */
    // Eq(TYPE,LIST)
    // TYPE Eq LIST
    public void processInput() {
        int tokPos = 0;
        int strPos = 0;
        //Object[] outData = new Object[inputData.length];
        Object[] outAux = new Object[inputTokens.length];
        Syntax[] outSyn = new Syntax[inputTokens.length];
       // Type[] outType = new Type[inputData.length];

        int outStrPos = 0;
        int outSynPos = 0;
        int outTypePos = 0;
        int outAuxPos = 0;

        boolean makingList=false;
        boolean fetchedArgName=false;
        boolean fetchedModifiers = true;

        while (tokPos < inputTokens.length) {
            Token curTok = inputTokens[tokPos++];

            if (curTok == Token.STRING) {
                if (tokPos==1) {
                   // outData[outStrPos++]=inputData[strPos++];
                    outSyn[outSynPos++]=Syntax.TYPE;
                    outAux[outAuxPos++]=Type.TABLE;
                    continue;
                }
                if (makingList) {
                    if (fetchedModifiers) {
                        if (!fetchedArgName) {
                            fetchedArgName = true;
                            outAux[outAuxPos++] = Type.NAME;
                            //outData[outStrPos++]=inputData[strPos++];
                        } else {
                            fetchedArgName = false;
                            outAux[outAuxPos++] = Type.DATATYPE;
                        }
                    } else {
                        fetchedModifiers=true;
                        outAux[outAuxPos++] = Type.MODIFIER;
                    }
                }
                //todo: args handling
            }
            if (curTok == Token.EQUALS) {
                outAux[outAuxPos++]=Token.EQUALS;
                outSyn[outSynPos++]=Syntax.OPERATOR;
                continue;
            }
            if (curTok == Token.BRACKET_OPEN) {
                if (!makingList) {
                    makingList=true;
                    outSyn[outSynPos++]=Syntax.TYPE;
                    outAux[outAuxPos++]=Type.LIST;
                    continue;
                } else {
                    System.out.println("parsing error: unexpected nested opening bracket");
                }
            }
            if (curTok == Token.COLON) {
                if (makingList) {
                    if (fetchedArgName)
                        continue;
                    else
                        System.out.println("parsing error: no argument name found before colon");
                } else {
                    System.out.println("parsing error: unexpected colon outside of list declaration");
                }
            }
            if (curTok == Token.SEMICOLON) {
                if (makingList) {
                    fetchedModifiers = false;
                    continue;
                }
            }
            if (curTok == Token.COMMA) {
                //handle errors
            }
            if (curTok == Token.BRACKET_CLOSE) {
                makingList=false;
                continue;
            }
        }

        outputSyntax = new Syntax[outSynPos];
        outputAux = new Object[outAuxPos];

        for (int i=0; i<outSynPos; i++) {
            outputSyntax[i]=outSyn[i];
        }
        for (int i=0; i<outAuxPos; i++) {
            outputAux[i]=outAux[i];
        }

        System.out.println("Parsing complete.");
        System.out.print("Data=[");
        for (String s: inputData) {
            System.out.print("\""+s+"\",");
        }
        System.out.print("]\nSyntax=[");
        for (Syntax s: outputSyntax) {
            System.out.print("\""+s.toString()+"\",");
        }
        System.out.print("]\nAux=[");
        for (Object s: outputAux) {
            System.out.print("\""+s.toString()+"\",");
        }
        System.out.println("]");
    }
}
