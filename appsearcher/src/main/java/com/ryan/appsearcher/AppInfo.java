package com.ryan.appsearcher;

import java.io.Serializable;

public class AppInfo implements Comparable<AppInfo>, Serializable
{
    private String appName = "";
    private String appOpen = "";
    private short numTime = 0;

    public AppInfo(String appName, String appOpen)
    {
        this.appName = appName;
        this.appOpen = appOpen;
    }

    public AppInfo(String appName, String appOpen, short numTime)
    {
        this.appName = appName;
        this.appOpen = appOpen;
        this.numTime = numTime;
    }

    public AppInfo() {this.appName = ""; this.appOpen = ""; }

    public int compareTo(AppInfo other)
    {
        return this.getAppName().compareTo(other.getAppName());
    }

    public boolean equals(Object other)
    {
        return this.getAppOpen().equals(((AppInfo)other).getAppOpen());
    }

    public String getAppName()
    {
        return appName;
    }

    public String getAppOpen()
    {
        return appOpen;
    }

    public short getNumTime() { return numTime; }

    public void increment() { numTime ++; }

    public String toString() { return "NAME: " + appName + "\tOPEN: " + appOpen + "\tNUM OPEN: " + numTime; }

}
