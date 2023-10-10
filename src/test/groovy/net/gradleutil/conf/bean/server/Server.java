package net.gradleutil.conf.bean.server;

import java.io.Serializable;
import java.util.List;

public class Server  implements Serializable {

    public String trayicon;

    public Jvm jvm;

    public App app;

    public Boolean console;

    public Boolean debug;

    public String commandboxHome;

    public String profile;

    public Boolean trayEnable;

    public Env env;

    public Runwar runwar;

    public Long stopsocket;

    public Boolean trace;

    public Boolean dockEnable;

    public Long startTimeout;

    public Web web;

    public String name;

    public List<TrayOption> trayOptions;

    public String openBrowserURL;

    public Boolean openBrowser;

    public Server(){ }

    public Server trayicon(String trayicon){ this.trayicon = trayicon; return this; }
    ;

    public Server jvm(Jvm jvm){ this.jvm = jvm; return this; }
    ;

    public Server app(App app){ this.app = app; return this; }
    ;

    public Server console(Boolean console){ this.console = console; return this; }
    ;

    public Server debug(Boolean debug){ this.debug = debug; return this; }
    ;

    public Server commandboxHome(String commandboxHome){ this.commandboxHome = commandboxHome; return this; }
    ;

    public Server profile(String profile){ this.profile = profile; return this; }
    ;

    public Server trayEnable(Boolean trayEnable){ this.trayEnable = trayEnable; return this; }
    ;

    public Server env(Env env){ this.env = env; return this; }
    ;

    public Server runwar(Runwar runwar){ this.runwar = runwar; return this; }
    ;

    public Server stopsocket(Long stopsocket){ this.stopsocket = stopsocket; return this; }
    ;

    public Server trace(Boolean trace){ this.trace = trace; return this; }
    ;

    public Server dockEnable(Boolean dockEnable){ this.dockEnable = dockEnable; return this; }
    ;

    public Server startTimeout(Long startTimeout){ this.startTimeout = startTimeout; return this; }
    ;

    public Server web(Web web){ this.web = web; return this; }
    ;

    public Server name(String name){ this.name = name; return this; }
    ;

    public Server trayOptions(List<TrayOption> trayOptions){ this.trayOptions = trayOptions; return this; }
    ;

    public Server openBrowserURL(String openBrowserURL){ this.openBrowserURL = openBrowserURL; return this; }
    ;

    public Server openBrowser(Boolean openBrowser){ this.openBrowser = openBrowser; return this; }
    ;

}
