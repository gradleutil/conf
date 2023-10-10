package net.gradleutil.conf.bean.server;

import java.io.Serializable;

public class AJP  implements Serializable {

    public Long port;

    public Boolean enable;

    public AJP(){ }

    public AJP port(Long port){ this.port = port; return this; }
    ;

    public AJP enable(Boolean enable){ this.enable = enable; return this; }
    ;

}
