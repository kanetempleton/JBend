package com;


import com.util.*;

public class Main {

    public static Launcher launcher;

    public static void main(String[] args) {

        String x = "";

       // Tools.testchop();



        for (int i=0; i<args.length; i++) {
            String a = args[i];
            switch (i) {
                case 0:
                    if (a.equalsIgnoreCase("local")) {
                        x = a;
                    }
                    break;
            }
        }

      //  launcher = new Launcher();
        //new Thread(launcher).start();
        //launcher.addStandardThreads();
       // launcher.startThreads();
    }


}
