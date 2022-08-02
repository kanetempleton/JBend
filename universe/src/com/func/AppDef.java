package com.func;

import com.Main;

public class AppDef extends ConfFunction {

    //String appName;
    public AppDef(String appname) {
        super("AppDef",new String[]{appname});
        //appName=appname;
    }

    public void execute() {
       // System.out.println("app name: "+args(0));
        if (args(0).length()>0)
            Main.launcher.setApplicationName(args(0));
    }

    public String toString() {
        return "AppDef("+args(0)+")";
    }


}