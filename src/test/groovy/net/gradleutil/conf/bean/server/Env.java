package net.gradleutil.conf.bean.server;

import java.io.Serializable;

public class Env  implements Serializable {

    public String aNYTHINGHERE;

    public String tHESEAREADDED;

    public Env(){ }

    public Env aNYTHINGHERE(String aNYTHINGHERE){ this.aNYTHINGHERE = aNYTHINGHERE; return this; }
    ;

    public Env tHESEAREADDED(String tHESEAREADDED){ this.tHESEAREADDED = tHESEAREADDED; return this; }
    ;

}
