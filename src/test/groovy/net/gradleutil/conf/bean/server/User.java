package net.gradleutil.conf.bean.server;

import java.io.Serializable;

public class User  implements Serializable {

    public String userName2;

    public String userName1;

    public User(){ }

    public User userName2(String userName2){ this.userName2 = userName2; return this; }
    ;

    public User userName1(String userName1){ this.userName1 = userName1; return this; }
    ;

}
