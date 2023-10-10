package net.gradleutil.conf.bean.server;

import java.io.Serializable;

public class App  implements Serializable {

    public String cfengine;

    public String webXMLOverride;

    public String webXML;

    public String serverHomeDirectory;

    public String serverConfigDir;

    public Boolean webXMLOverrideForce;

    public String libDirs;

    public String webConfigDir;

    public String logDir;

    public String restMappings;

    public String wARPath;

    public Boolean sessionCookieSecure;

    public Boolean sessionCookieHTTPOnly;

    public App(){ }

    public App cfengine(String cfengine){ this.cfengine = cfengine; return this; }
    ;

    public App webXMLOverride(String webXMLOverride){ this.webXMLOverride = webXMLOverride; return this; }
    ;

    public App webXML(String webXML){ this.webXML = webXML; return this; }
    ;

    public App serverHomeDirectory(String serverHomeDirectory){ this.serverHomeDirectory = serverHomeDirectory; return this; }
    ;

    public App serverConfigDir(String serverConfigDir){ this.serverConfigDir = serverConfigDir; return this; }
    ;

    public App webXMLOverrideForce(Boolean webXMLOverrideForce){ this.webXMLOverrideForce = webXMLOverrideForce; return this; }
    ;

    public App libDirs(String libDirs){ this.libDirs = libDirs; return this; }
    ;

    public App webConfigDir(String webConfigDir){ this.webConfigDir = webConfigDir; return this; }
    ;

    public App logDir(String logDir){ this.logDir = logDir; return this; }
    ;

    public App restMappings(String restMappings){ this.restMappings = restMappings; return this; }
    ;

    public App wARPath(String wARPath){ this.wARPath = wARPath; return this; }
    ;

    public App sessionCookieSecure(Boolean sessionCookieSecure){ this.sessionCookieSecure = sessionCookieSecure; return this; }
    ;

    public App sessionCookieHTTPOnly(Boolean sessionCookieHTTPOnly){ this.sessionCookieHTTPOnly = sessionCookieHTTPOnly; return this; }
    ;

}
