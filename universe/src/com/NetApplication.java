package com;

public abstract class NetApplication implements Runnable {

    private String configFile;
    private Launcher launcher;
    public NetApplication() {
       // configFile=config;
        launcher=new Launcher();
    }

    public void startApplication() {
        launcher.startThreads();
        (new Thread(this)).start();
    }

    public void run() {
        System.out.println("Starting application...");
        while (true) {
            try {
                runApplication();
               // Thread.sleep(500);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public abstract void runApplication();


}