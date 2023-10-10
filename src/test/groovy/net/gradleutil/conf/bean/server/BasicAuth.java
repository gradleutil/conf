package net.gradleutil.conf.bean.server;

import java.io.Serializable;

public class BasicAuth  implements Serializable {

    public Boolean enable;

    public User users;

    public BasicAuth(){ }

    public BasicAuth enable(Boolean enable){ this.enable = enable; return this; }
    ;

    public BasicAuth users(User users){ this.users = users; return this; }
    ;

}
