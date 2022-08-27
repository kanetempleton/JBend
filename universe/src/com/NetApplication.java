package com;

public abstract class NetApplication implements Runnable {

    private String appName;
    public NetApplication(String appname) {
        appName=appname;
    }

    public void run() {
        System.out.println("Starting application: "+appName);
        Main.launcher.nextStage();
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

    public String getAppName() {return appName;}


}