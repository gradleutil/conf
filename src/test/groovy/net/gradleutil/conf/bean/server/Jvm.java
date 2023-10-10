package net.gradleutil.conf.bean.server;

import java.io.Serializable;
import java.util.List;

public class Jvm  implements Serializable {

    public List<String> args;

    public String javaVersion;

    public Long minHeapSize;

    public Long heapSize;

    public String javaHome;

    public Jvm(){ }

    public Jvm args(List<String> args){ this.args = args; return this; }
    ;

    public Jvm javaVersion(String javaVersion){ this.javaVersion = javaVersion; return this; }
    ;

    public Jvm minHeapSize(Long minHeapSize){ this.minHeapSize = minHeapSize; return this; }
    ;

    public Jvm heapSize(Long heapSize){ this.heapSize = heapSize; return this; }
    ;

    public Jvm javaHome(String javaHome){ this.javaHome = javaHome; return this; }
    ;

}
