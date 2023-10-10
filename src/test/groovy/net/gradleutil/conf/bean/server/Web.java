package net.gradleutil.conf.bean.server;

import java.io.Serializable;
import java.util.List;

public class Web  implements Serializable {

    public String rulesFile;

    public Alias aliases;

    public Rewrite rewrites;

    public Boolean gzipEnable;

    public Boolean blockSensitivePaths;

    public BasicAuth basicAuth;

    public AJP aJP;

    public Boolean blockFlashRemoting;

    public HTTP hTTP;

    public List<String> rules;

    public SSL sSL;

    public String webroot;

    public Boolean accessLogEnable;

    public Boolean directoryBrowsing;

    public String rulesFileGlob;

    public String gzipPredicate;

    public ErrorPage errorPages;

    public String host;

    public List<String> rulesFiles;

    public Long maxRequests;

    public String welcomeFiles;

    public Boolean blockCFAdmin;

    public Web(){ }

    public Web rulesFile(String rulesFile){ this.rulesFile = rulesFile; return this; }
    ;

    public Web aliases(Alias aliases){ this.aliases = aliases; return this; }
    ;

    public Web rewrites(Rewrite rewrites){ this.rewrites = rewrites; return this; }
    ;

    public Web gzipEnable(Boolean gzipEnable){ this.gzipEnable = gzipEnable; return this; }
    ;

    public Web blockSensitivePaths(Boolean blockSensitivePaths){ this.blockSensitivePaths = blockSensitivePaths; return this; }
    ;

    public Web basicAuth(BasicAuth basicAuth){ this.basicAuth = basicAuth; return this; }
    ;

    public Web aJP(AJP aJP){ this.aJP = aJP; return this; }
    ;

    public Web blockFlashRemoting(Boolean blockFlashRemoting){ this.blockFlashRemoting = blockFlashRemoting; return this; }
    ;

    public Web hTTP(HTTP hTTP){ this.hTTP = hTTP; return this; }
    ;

    public Web rules(List<String> rules){ this.rules = rules; return this; }
    ;

    public Web sSL(SSL sSL){ this.sSL = sSL; return this; }
    ;

    public Web webroot(String webroot){ this.webroot = webroot; return this; }
    ;

    public Web accessLogEnable(Boolean accessLogEnable){ this.accessLogEnable = accessLogEnable; return this; }
    ;

    public Web directoryBrowsing(Boolean directoryBrowsing){ this.directoryBrowsing = directoryBrowsing; return this; }
    ;

    public Web rulesFileGlob(String rulesFileGlob){ this.rulesFileGlob = rulesFileGlob; return this; }
    ;

    public Web gzipPredicate(String gzipPredicate){ this.gzipPredicate = gzipPredicate; return this; }
    ;

    public Web errorPages(ErrorPage errorPages){ this.errorPages = errorPages; return this; }
    ;

    public Web host(String host){ this.host = host; return this; }
    ;

    public Web rulesFiles(List<String> rulesFiles){ this.rulesFiles = rulesFiles; return this; }
    ;

    public Web maxRequests(Long maxRequests){ this.maxRequests = maxRequests; return this; }
    ;

    public Web welcomeFiles(String welcomeFiles){ this.welcomeFiles = welcomeFiles; return this; }
    ;

    public Web blockCFAdmin(Boolean blockCFAdmin){ this.blockCFAdmin = blockCFAdmin; return this; }
    ;

}
