package net.gradleutil.conf.bean.server;

import java.io.Serializable;

public class Rewrite  implements Serializable {

    public String statusPath;

    public Long configReloadSeconds;

    public Boolean enable;

    public Boolean logEnable;

    public String config;

    public Rewrite(){ }

    public Rewrite statusPath(String statusPath){ this.statusPath = statusPath; return this; }
    ;

    public Rewrite configReloadSeconds(Long configReloadSeconds){ this.configReloadSeconds = configReloadSeconds; return this; }
    ;

    public Rewrite enable(Boolean enable){ this.enable = enable; return this; }
    ;

    public Rewrite logEnable(Boolean logEnable){ this.logEnable = logEnable; return this; }
    ;

    public Rewrite config(String config){ this.config = config; return this; }
    ;

}
