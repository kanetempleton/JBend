package com.util;

import com.Main;
import com.db.*;
import com.console.*;

public class CareTaker implements Runnable {

    private long breakTime;
    private boolean ready;

    public CareTaker(long freq) {
        breakTime=freq;
        ready=false;
    }

    @Override
    public void run() {
        Console.output("Server caretaker started successfully!");
        Main.launcher.nextStage();
        while (true) {
            doStuff();
            try {
                Thread.sleep(breakTime);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void doStuff() {
        if (!ready) {
            ready = true;
        }
        else {
           // Main.launcher.databaseManager().query(null,"select value from settings where name='dummyQuery';");
            new ServerQuery(Main.launcher.getLoginHandler(),"select value from settings where name='dummyQuery';",false) {
                public void done() {
                    if (this.getResponse()==null || this.getResponse().length() <=0 ) {
                        System.out.println("database connection problems... attempting restart");
                        Main.launcher.databaseManager().resetConnection();
                        //Main.launcher.realoadDatabaseManager();//databaseManager().connectToDatabase();
                    } else if (this.getResponse().equals("value=420 OK")) {
                        System.out.println("database alive check OK");
                    } else {
                        Console.output("query worked but bad response wtf");
                        Main.launcher.databaseManager().resetConnection();
                    }
                }
            };
        }
    }
}
