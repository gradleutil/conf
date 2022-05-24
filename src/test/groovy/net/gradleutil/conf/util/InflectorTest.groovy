package net.gradleutil.conf.util

import spock.lang.Specification

class InflectorTest extends Specification {

    def "test SemVer bumps"() {
        setup:
        def version
        def inflector = Inflector.instance

        when:
        version = inflector.pluralize('sheet')
        then:
        version == 'sheets'

        when:
        version = inflector.singularize('sheets')
        then:
        version == 'sheet'

    }


}
