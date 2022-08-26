package com;

public abstract class NetApplication implements Runnable {

    private String configFile;
    private Launcher launcher;
    private String appName;
    public NetApplication(String appname) {
       // configFile=config;
        launcher=new Launcher();
        appName=appname;
    }

    public void startApplication() {
       // launcher.loadThread(this,appName);
        launcher.startThreads();
       // (new Thread(this)).start();
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