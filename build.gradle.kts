plugins {
    `java-library`
    id("xyz.jpenilla.run-paper") version "2.0.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.github.devcyntrix"
version = "1.5.24-mineplots"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://maven.enginehub.org/repo/")

    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://raw.githubusercontent.com/FabioZumbi12/RedProtect/mvn-repo/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repository.minecodes.pl/releases")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("com.google.inject:guice:7.0.0")
    compileOnly("org.spigotmc:spigot-api:1.17.1-R0.1-SNAPSHOT")

    // Command library
    compileOnly("cloud.commandframework:cloud-core:1.7.1")
    compileOnly("cloud.commandframework:cloud-bukkit:1.7.1")

    // Protection Support
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.7")
    compileOnly("com.plotsquared:PlotSquared-Core:6.8.1") { isTransitive = false }
    compileOnly("com.plotsquared:PlotSquared-Bukkit:6.8.1") { isTransitive = false }
    compileOnly("com.github.TechFortress:GriefPrevention:16.18") { isTransitive = false }
    compileOnly("br.net.fabiozumbi12.RedProtect:RedProtect-Core:7.7.3") { isTransitive = false }
    compileOnly("br.net.fabiozumbi12.RedProtect:RedProtect-Spigot:7.7.3") { isTransitive = false }
    compileOnly("pl.minecodes.plots:plugin-api:4.0.0")


    compileOnly("com.griefdefender:api:2.1.0-SNAPSHOT")

    // Animation Support
    compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0") { isTransitive = false }

    // Placeholder API
    compileOnly("me.clip:placeholderapi:2.11.2") { isTransitive = false }

    //implementation("com.github.MP-MC:Canvas:941db8c981")
    compileOnly("org.apache.commons:commons-text:1.10.0")
    compileOnly("org.jetbrains:annotations:23.0.0")

    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name()
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        from("src/main/resources") {
            include("**")
        }

    }
    runServer {
        minecraftVersion("1.19.4")
    }
    test {
        useJUnitPlatform()
    }
}
