package net.gradleutil.conf.bean

import net.gradleutil.conf.annotation.Optional
import net.gradleutil.conf.annotation.ToStringIncludeNames

import static net.gradleutil.conf.util.ConfUtil.setBeanFromConf
import static net.gradleutil.conf.util.ConfUtil.setBeanFromConfigFile

@ToStringIncludeNames
class MinecraftConfig  implements Serializable {

    MinecraftConfig(){ }

    MinecraftConfig(File conf, File confOverride){
        setBeanFromConfigFile(this, conf, confOverride)
    }

    MinecraftConfig(String conf, Boolean ignoreMissingProperties = false){
        setBeanFromConf(this, conf, ignoreMissingProperties)
    }

    @Optional
    String databasePath = "./data.db"

    @Optional
    Git git

    @Optional
    String schema

    @Optional
    String minecraftDataDir = "\\AppData\\Roaming\\.minecraft"

    @Optional
    List<Minecraft> minecrafts = [] as List<Minecraft>

    String name

    @Optional
    List<Repository> modRepo = [] as List<Repository>

    @Optional
    List<Repository> publishing = [] as List<Repository>

}

@ToStringIncludeNames
class Git  implements Serializable {

    Git(){ }

    String repoUri = "'https://github.com/minecraft'"

    String dir = "'~/minecraft'"

}

@ToStringIncludeNames
class Minecraft  implements Serializable {

    Minecraft(){ }

    @Optional
    List<Mod> mods = [] as List<Mod>

    Type type
    enum Type  {
            VANILLA("vanilla"),FORGE("forge"),SPIGOT("spigot"),PAPER("paper")

            private final String name;

            private Type(String s) {
                name = s;
            }

            public boolean equalsName(String otherName) {
                return name.equals(otherName);
            }

            public String toString() {
               return this.name;
            }
        }

    Version version
    enum Version  {
            V1_19_1("1.19.1"),V1_19("1.19"),V1_19_SNAPSHOT("1.19-Snapshot"),V1_18_2("1.18.2"),V1_18_1("1.18.1"),V1_18("1.18"),V1_18_SNAPSHOT("1.18-Snapshot"),V1_17_1("1.17.1"),V1_17("1.17"),V1_17_SNAPSHOT("1.17-Snapshot"),V1_16_5("1.16.5"),V1_16_4("1.16.4"),V1_16_3("1.16.3"),V1_16_2("1.16.2"),V1_16_1("1.16.1"),V1_16("1.16"),V1_16_SNAPSHOT("1.16-Snapshot"),V1_15_2("1.15.2"),V1_15_1("1.15.1"),V1_15("1.15"),V1_15_SNAPSHOT("1.15-Snapshot"),V1_14_4("1.14.4"),V1_14_3("1.14.3"),V1_14_2("1.14.2"),V1_14_1("1.14.1"),V1_14("1.14"),V1_14_SNAPSHOT("1.14-Snapshot"),V1_13_2("1.13.2"),V1_13_1("1.13.1"),V1_13("1.13"),V1_13_SNAPSHOT("1.13-Snapshot"),V1_12_2("1.12.2"),V1_12_1("1.12.1"),V1_12("1.12"),V1_12_SNAPSHOT("1.12-Snapshot"),V1_11_2("1.11.2"),V1_11_1("1.11.1"),V1_11("1.11"),V1_11_SNAPSHOT("1.11-Snapshot"),V1_10_2("1.10.2"),V1_10_1("1.10.1"),V1_10_SNAPSHOT("1.10-Snapshot"),V1_10("1.10"),V1_9_4("1.9.4"),V1_9_3("1.9.3"),V1_9_2("1.9.2"),V1_9_1("1.9.1"),V1_9("1.9"),V1_9_SNAPSHOT("1.9-Snapshot"),V1_8_9("1.8.9"),V1_8_8("1.8.8"),V1_8_7("1.8.7"),V1_8_6("1.8.6"),V1_8_5("1.8.5"),V1_8_4("1.8.4"),V1_8_3("1.8.3"),V1_8_2("1.8.2"),V1_8_1("1.8.1"),BETA_1_8_1("Beta 1.8.1"),V1_8("1.8"),V1_8_SNAPSHOT("1.8-Snapshot"),V1_7_10("1.7.10"),V1_7_9("1.7.9"),V1_7_8("1.7.8"),V1_7_7("1.7.7"),V1_7_6("1.7.6"),V1_7_5("1.7.5"),V1_7_4("1.7.4"),V1_7_3("1.7.3"),BETA_1_7_3("Beta 1.7.3"),V1_7_2("1.7.2"),BETA_1_7("Beta 1.7"),BETA_1_6_6("Beta 1.6.6"),V1_6_4("1.6.4"),V1_6_2("1.6.2"),V1_6_1("1.6.1"),V1_5_2("1.5.2"),V1_5_1("1.5.1"),V1_5_0("1.5.0"),V1_4_7("1.4.7"),V1_4_6("1.4.6"),V1_4_5("1.4.5"),V1_4_4("1.4.4"),V1_4_2("1.4.2"),V1_3_2("1.3.2"),V1_3_1("1.3.1"),V1_2_5("1.2.5"),V1_2_4("1.2.4"),V1_2_3("1.2.3"),V1_2_2("1.2.2"),V1_2_1("1.2.1"),V1_1("1.1"),V1_0_0("1.0.0"),V1_0("1.0")

            private final String name;

            private Version(String s) {
                name = s;
            }

            public boolean equalsName(String otherName) {
                return name.equals(otherName);
            }

            public String toString() {
               return this.name;
            }
        }

}

@ToStringIncludeNames
class Mod  implements Serializable {

    Mod(){ }

    @Optional
    List<ModArtifact> artifacts = [] as List<ModArtifact>

    String name

    @Optional
    String description

    @Optional
    String id

    @Optional
    String slug

    @Optional
    List<Author> authors = [] as List<Author>

}

@ToStringIncludeNames
class ModArtifact  implements Serializable {

    ModArtifact(){ }

    Minecraft minecraft

    @Optional
    String mavenDependency

    @Optional
    String downloadUrl

    @Optional
    String name

    String version

    @Optional
    String hash

    @Optional
    String fileId

    @Optional
    List<ModArtifact> dependencies = [] as List<ModArtifact>

}

@ToStringIncludeNames
class Author  implements Serializable {

    Author(){ }

    @Optional
    String name

    @Optional
    Long id

    @Optional
    String url

}

@ToStringIncludeNames
class Repository  implements Serializable {

    Repository(){ }

    @Optional
    String path

    @Optional
    BigInteger port

    @Optional
    Credential credentials

    @Optional
    String host

    Type type
    enum Type  {
            FILE("file"),MAVEN("maven"),SCP("scp"),GIT("git")

            private final String name;

            private Type(String s) {
                name = s;
            }

            public boolean equalsName(String otherName) {
                return name.equals(otherName);
            }

            public String toString() {
               return this.name;
            }
        }

    @Optional
    String url

}

@ToStringIncludeNames
class Credential  implements Serializable {

    Credential(){ }

    @Optional
    String password

    @Optional
    String passphrase

    @Optional
    String identityFile

    @Optional
    String url

    @Optional
    String username

}
