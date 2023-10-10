package net.gradleutil.conf.bean.server;

import java.io.Serializable;

public class Alias  implements Serializable {

    public String _Foo;

    public String _Js;

    public Alias(){ }

    public Alias _Foo(String _Foo){ this._Foo = _Foo; return this; }
    ;

    public Alias _Js(String _Js){ this._Js = _Js; return this; }
    ;

}
