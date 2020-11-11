package com.db.crud.lang;


class SynMap {
    Syntax[] matchingSyn;
    Expression method;
    SynMap(Syntax[] in,Expression meth) {
        matchingSyn=in;
        method=meth;
    }
}

public class Expression {

    private String[] dat;
    private Syntax[] syn;

    public Expression(String[] d, Syntax[] s){
        dat=d;
        syn=s;
    }

    public static final Syntax[] eql = {Syntax.TYPE,Syntax.OPERATOR,Syntax.TYPE};
    public static final Object[] equal = {Type.TABLE,Token.EQUALS,Type.LIST};
    private SynMap[] validSyn = {new SynMap(eql,EQUALS())};

    public Expression findExpression() {
        for (SynMap map: validSyn) {
            Syntax[] seq = map.matchingSyn;
            if (seq.length!=syn.length)
                continue;
            int flag = 0;
            for (int i=0; i<syn.length; i++) {
                if (seq[i]!=syn[i]) {
                    flag = 1;
                    break;
                }
            }
            if (flag==0)
                return null;//findExpressionForSyntaxPattern(map);
        }
        return null;
    }

    public void evaluate() {

    }


    public Expression EQUALS() {
        return new Expression(dat,syn) {
            public void evaluate() {

            }
        };
    }
}
