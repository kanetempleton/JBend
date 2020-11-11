package com.db.crud.lang;

public class Interpreter {

    private String[] inputData;
    private Syntax[] inputSyntax;
    private Object[] inputAux;

    private Value[] outputValue;

    public Interpreter(String[] dat, Syntax[] syn, Object[] aux) {
        inputAux=aux;
        inputData=dat;
        inputSyntax=syn;
    }

    public void process() {

        int inAuxPos = 0;
        int inDatPos = 0;

        int outPos = 0;
        Value[] outVal = new Value[inputAux.length];

        for (int i=0; i<inputSyntax.length;i++) {
            Syntax curSyn = inputSyntax[i];
            if (curSyn == Syntax.TYPE) {
                Type t = (Type) inputAux[inAuxPos++];
                if (t == Type.TABLE) {
                    Value v = new Value(t,inputData[inDatPos++]);
                    outVal[outPos++]=v;
                    continue;
                }
                if (t == Type.LIST) {

                    boolean flag = false;
                    while (!flag) {
                        Type ty = (Type) inputAux[inAuxPos++];
                        if (ty == Type.NAME) {
                            Value v = new Value(ty,inputData[inDatPos++]);
                        }
                        else if (ty == Type.DATATYPE) {
                            Value v = new Value(ty,inputData[inDatPos++]);
                        }
                        else if (ty == Type.MODIFIER) {
                            Value v = new Value(ty,inputData[inDatPos++]);
                        } else {
                            flag=true; //end of list
                        }
                    }
                    //idk yet
                    //finish with
                    //new Function(eq,table,data) use values..
                    //values=[(table,$name),(list,args)]
                    //function=f(values)
                }
            }
            if (curSyn == Syntax.OPERATOR) {
                Value v = new Value(Type.FUNCTION,inputAux[inAuxPos++].toString());
                outVal[outPos++]=v;
                continue;
            }
        }

        outputValue = new Value[outPos];
        for (int i=0; i<outPos; i++) {
            outputValue[i]=outVal[i];
        }


        // [(table,task),(function,equals),(
    }
}
