package net.gradleutil.conf.bean.server;

import java.io.Serializable;
import java.util.List;

public class Runwar  implements Serializable {

    public List<String> args;

    public String jarPath;

    public UndertowOption undertowOptions;

    public XNIOOption xNIOOptions;

    public Runwar(){ }

    public Runwar args(List<String> args){ this.args = args; return this; }
    ;

    public Runwar jarPath(String jarPath){ this.jarPath = jarPath; return this; }
    ;

    public Runwar undertowOptions(UndertowOption undertowOptions){ this.undertowOptions = undertowOptions; return this; }
    ;

    public Runwar xNIOOptions(XNIOOption xNIOOptions){ this.xNIOOptions = xNIOOptions; return this; }
    ;

}
