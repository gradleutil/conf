package net.gradleutil.conf.util

import groovy.transform.CompileStatic

@CompileStatic
class SemVer implements Serializable {

    static enum PatchLevel {
        MAJOR, MINOR, PATCH
    }

    private int major, minor, patch

    SemVer(String version) {
        def versionParts = version.tokenize('.')
        if (versionParts.size() != 3) {
            throw new IllegalArgumentException("Wrong version format - expected MAJOR.MINOR.PATCH - got ${version}")
        }
        this.major = versionParts[0].toInteger()
        this.minor = versionParts[1].toInteger()
        this.patch = versionParts[2].toInteger()
    }

    SemVer(int major, int minor, int patch) {
        this.major = major
        this.minor = minor
        this.patch = patch
    }

    SemVer bumpMajor() {
        bump(PatchLevel.MAJOR)
    }

    SemVer bumpMinor() {
        bump(PatchLevel.MINOR)
    }

    SemVer bumpPatch() {
        bump(PatchLevel.PATCH)
    }

    int getMajor() {
        major
    }

    int getMinor() {
        minor
    }

    int getPatch() {
        patch
    }

    SemVer bump(PatchLevel patchLevel) {
        switch (patchLevel) {
            case PatchLevel.MAJOR:
                return new SemVer(major + 1, 0, 0)
                break
            case PatchLevel.MINOR:
                return new SemVer(major, minor + 1, 0)
                break
            case PatchLevel.PATCH:
                return new SemVer(major, minor, patch + 1)
                break
            default:
                throw new IllegalArgumentException("Must be one of MAJOR, MINOR, PATCH")
                break
        }
    }

    String toString() {
        return "${major}.${minor}.${patch}"
    }


}
