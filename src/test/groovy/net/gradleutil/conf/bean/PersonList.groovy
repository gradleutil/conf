package net.gradleutil.conf.bean

import net.gradleutil.conf.annotation.Optional
import net.gradleutil.conf.annotation.ToStringIncludeNames

import static net.gradleutil.conf.util.ConfUtil.setBeanFromConf
import static net.gradleutil.conf.util.ConfUtil.setBeanFromConfigFile

@ToStringIncludeNames
class PersonList  implements Serializable {

    PersonList(){ }

    PersonList(File conf, File confOverride){
        setBeanFromConfigFile(this, conf, confOverride)
    }

    PersonList(String conf, Boolean ignoreMissingProperties = false){
        setBeanFromConf(this, conf, ignoreMissingProperties)
    }

        @Optional
        List<Person> people =  []  as List<Person>

}

@ToStringIncludeNames
class Person  implements Serializable {

    Person(){ }

        @Optional
        String firstName 

        @Optional
        String lastName 

        @Optional
        Long age 

}
