package net.gradleutil.conf.bean.server;

import java.io.Serializable;

public class ErrorPage  implements Serializable {

    public String _default;

    public String _500;

    public String _404;

    public ErrorPage(){ }

    public ErrorPage _default(String _default){ this._default = _default; return this; }
    ;

    public ErrorPage _500(String _500){ this._500 = _500; return this; }
    ;

    public ErrorPage _404(String _404){ this._404 = _404; return this; }
    ;

}
