package net.gradleutil.conf.bean.server;

import java.io.Serializable;

public class HTTP  implements Serializable {

    public Long port;

    public Boolean enable;

    public HTTP(){ }

    public HTTP port(Long port){ this.port = port; return this; }
    ;

    public HTTP enable(Boolean enable){ this.enable = enable; return this; }
    ;

}
