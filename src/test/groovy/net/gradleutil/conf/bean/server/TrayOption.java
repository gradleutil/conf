package net.gradleutil.conf.bean.server;

import java.io.Serializable;

public class TrayOption  implements Serializable {

    public String image;

    public String action;

    public Boolean disabled;

    public String label;

    public String url;
    public String path;
    public String command;

    public TrayOption(){ }

    public TrayOption image(String image){ this.image = image; return this; }
    ;

    public TrayOption action(String action){ this.action = action; return this; }
    ;

    public TrayOption disabled(Boolean disabled){ this.disabled = disabled; return this; }
    ;

    public TrayOption label(String label){ this.label = label; return this; }
    ;

    public TrayOption url(String url){ this.url = url; return this; }
    ;
    public TrayOption path(String path){ this.path = path; return this; }
    ;
    public TrayOption command(String command){ this.command = command; return this; }
    ;

}
