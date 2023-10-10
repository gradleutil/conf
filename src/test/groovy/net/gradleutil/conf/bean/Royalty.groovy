package net.gradleutil.conf.bean

import net.gradleutil.conf.annotation.Optional
import net.gradleutil.conf.annotation.ToStringIncludeNames

import static net.gradleutil.conf.util.ConfUtil.setBeanFromConf
import static net.gradleutil.conf.util.ConfUtil.setBeanFromConfigFile

@ToStringIncludeNames
class Royalty  implements Serializable {

    Royalty(){ }

    Royalty(File conf, File confOverride){
        setBeanFromConfigFile(this, conf, confOverride)
    }

    Royalty(String conf, Boolean ignoreMissingProperties = false){
        setBeanFromConf(this, conf, ignoreMissingProperties)
    }

    @Optional
    List<Royalty> children = [] as List<Royalty>

    @Optional
    String name

}

