package net.gradleutil.conf.bean.server;

import java.io.Serializable;

public class XNIOOption  implements Serializable {

    public String wORKERNAME;

    public XNIOOption(){ }

    public XNIOOption wORKERNAME(String wORKERNAME){ this.wORKERNAME = wORKERNAME; return this; }
    ;

}
