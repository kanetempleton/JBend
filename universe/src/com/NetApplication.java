package com;

public abstract class NetApplication {

    private String configFile;
    private Launcher launcher;
    public NetApplication(String config) {
        configFile=config;
        launcher=null;
    }

    public void startApplication() {
        launcher = new Launcher();
    }


}