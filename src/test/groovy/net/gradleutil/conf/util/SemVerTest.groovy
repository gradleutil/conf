package net.gradleutil.conf.util

import spock.lang.Specification

import static net.gradleutil.conf.util.SemVer.PatchLevel.MAJOR
import static net.gradleutil.conf.util.SemVer.PatchLevel.MINOR
import static net.gradleutil.conf.util.SemVer.PatchLevel.PATCH

class SemVerTest extends Specification {

    def "test SemVer bumps"() {
        setup:
        def version
        def semVer = new SemVer("0.0.1")

        when:
        version = semVer.bump(MAJOR).toString()
        then:
        version == '1.0.0'

        when:
        version = semVer.bump(MINOR).toString()
        then:
        version == '0.1.0'

        when:
        version = semVer.bump(PATCH).toString()
        then:
        version == '0.0.2'

        when:
        version = semVer.bumpMajor().bumpMinor().bumpPatch().toString()
        then:
        version == '1.1.1'
    }


}
