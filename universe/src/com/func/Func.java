package com.func;

import com.util.Tools;

public class Func extends ConfFunction {

    private String function_name;
    public Func(String fname, String[] fargs) {
        super("Func["+fname+"]",fargs);
        function_name=fname;
    }

    public void execute() {
        System.out.println(""+function_name+"("+Tools.comma_string(args())+")");
    }

    public String toString() {
        String out = "Func_"+function_name+"(";
        for (int i=0; i<args().length; i++) {
            if (i==args().length - 1)
                out += args(i)+"";
            else
                out += args(i)+" ";
        }
        out+=")";
        return out;
    }

    public String function_name(){return function_name;}

}