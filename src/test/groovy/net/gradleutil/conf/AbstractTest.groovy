package net.gradleutil.conf


import spock.lang.Specification

class AbstractTest extends Specification {


    String getPackageName() {
        'net.gradleutil.conf.temp.' + this.class.simpleName.toLowerCase()
    }

    String getBase() {
        'src/test/groovy/' + packageName.replace('.', '/') + '/'
    }

    File getBaseDir() {
        new File(getBase())
    }

    def setup() {
        new File(base).with {
            if (exists()) {
//                deleteDir()
            }
            mkdirs()
        }
    }

}
