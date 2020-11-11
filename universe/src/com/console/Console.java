/* Console.java
    used for processing terminal commands typed by system admins
 */

package com.console;
import com.Main;
import java.io.BufferedReader;
import java.io.IOException;

public class Console implements Runnable {

    public static Command[] commands;
    private BufferedReader reader;
    private String build;

    public boolean needsRefresh,inputPrompt;

    public static void initCommands() {
        commands = new Command[] {
                new Help(), new Connections(), new MySQL(),
                new Crypto(), new Kick(), new Ping(), new Ban(), new Unban(),
                new Entities(), new Lex()
        };
        System.out.println("[Console] Initialized!");
    }

    public Console(BufferedReader br) {
        reader=br;
        build="";
        needsRefresh=inputPrompt=false;
        initCommands();
    }

    public void listen() {
        if (needsRefresh) {
            if (build.length()>0)
                System.out.print(">> " + build);
            needsRefresh=false;
            inputPrompt=true;
        }
        try {
            char ch = 0;
            if (reader.ready()) {
                try {
                    ch = (char) reader.read(); //read input one char at a time
                    build += ch;
                } catch (IOException e) {
                    System.out.println("Error reading from input device");
                }
            }
            if (!reader.ready() && build.length() > 0) {
                build = build.substring(0, build.length() - 1); //remove \n at end
                processCommand();
                build = "";
                inputPrompt=false;
                //needsRefresh=true;
                //System.out.print(">> ");
            }
        } catch (IOException e) {
            Console.output("IOException when listening for text: "+e.getMessage());
        }
    }



    public void processCommand() {
        String[] arg = build.split(" ");
        String[] meaninglessBuffer = {""};
        for (Command c: commands)
            if (c.name.equals(arg[0]))
                c.doCommand(arg,meaninglessBuffer);
    }

    public void processCommand(String com, String[] repBuf) {
        String[] arg = com.split(" ");
        for (Command c: commands)
            if (c.name.equals(arg[0]))
                c.doCommand(arg,repBuf);
    }

    public void run() {
        Main.launcher.nextStage();
        while (true) {
            listen();
        }
    }

    public static void output(String from, String msg) {
        System.out.println("["+from+"] " + msg);
    }

    public static void output(String msg) {
        try {
            if (!Main.launcher.console().needsRefresh && Main.launcher.console().inputPrompt) {
                if (Main.launcher.console().build.length()>0)
                    System.out.println();
            }
            output(Class.forName(Thread.currentThread().getStackTrace()[2].getClassName()).getSimpleName(),msg);
            Main.launcher.console().needsRefresh=true;
        } catch (Exception e) {
            System.out.println("[ERROR] problem with something");
        }
    }

    public static void log(String msg) {
        try {
            if (!Main.launcher.console().needsRefresh && Main.launcher.console().inputPrompt) {
                if (Main.launcher.console().build.length()>0)
                    System.out.println();
            }
            output(Class.forName(Thread.currentThread().getStackTrace()[2].getClassName()).getSimpleName(),msg);
            Main.launcher.console().needsRefresh=true;
        } catch (Exception e) {
            System.out.println("[ERROR] problem with something");
        }
    }



}

