package net.gradleutil.conf.bean.server;

import java.io.Serializable;

public class SSL  implements Serializable {

    public String keyPass;

    public Long port;

    public String keyFile;

    public Boolean enable;

    public String certFile;

    public SSL(){ }

    public SSL keyPass(String keyPass){ this.keyPass = keyPass; return this; }
    ;

    public SSL port(Long port){ this.port = port; return this; }
    ;

    public SSL keyFile(String keyFile){ this.keyFile = keyFile; return this; }
    ;

    public SSL enable(Boolean enable){ this.enable = enable; return this; }
    ;

    public SSL certFile(String certFile){ this.certFile = certFile; return this; }
    ;

}
