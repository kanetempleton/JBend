package com.db.crud.lang;

public class Function {

    private String fName;
    private String[] fData;
    private Object[] fAux;
    public Function(String name, String[] data, Object[] aux) {
        fAux=aux;
        fData=data;
        fName=name;
    }
    public String getName() {return fName;}
    public String[] getData() {return fData;}
    public Object[] getAux() {return fAux;}

    public void execute() {

    }

    public static Function equals(String[] dat, Object[] aux) {
        return new Function("eql",dat,aux) {
            public void execute() {
                int i=0;
                int j=0;
                int k=0;
                String query = "CREATE TABLE IF NOT EXISTS ";
                while (k<getAux().length) {
                    Object o = getAux()[k++];

                }
            }
        };
    }
}
