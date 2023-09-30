import io.papermc.hangarpublishplugin.model.Platforms

plugins {
    `java-library`
    id("xyz.jpenilla.run-paper") version "2.1.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.papermc.hangar-publish-plugin") version "0.0.5"
}

group = "com.github.devcyntrix"
version = "2.0.2"

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
    compileOnly("net.kyori:adventure-platform-bukkit:4.3.0")
    compileOnly("net.kyori:adventure-text-minimessage:4.14.0")
    compileOnly("net.kyori:adventure-text-serializer-legacy:4.14.0")

    // Command library
    compileOnly("cloud.commandframework:cloud-core:1.7.1")
    compileOnly("cloud.commandframework:cloud-bukkit:1.7.1")

    implementation("org.bstats:bstats-bukkit:3.0.2")

    // Protection Support
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.7")
    compileOnly("com.plotsquared:PlotSquared-Core:6.8.1") { isTransitive = false }
    compileOnly("com.plotsquared:PlotSquared-Bukkit:6.8.1") { isTransitive = false }
    compileOnly("com.github.TechFortress:GriefPrevention:16.18") { isTransitive = false }
    compileOnly("br.net.fabiozumbi12.RedProtect:RedProtect-Core:7.7.3") { isTransitive = false }
    compileOnly("br.net.fabiozumbi12.RedProtect:RedProtect-Spigot:7.7.3") { isTransitive = false }
    compileOnly("pl.minecodes.plots:plugin-api:4.0.0")

    // Animation Support
    compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0") { isTransitive = false }

    // Placeholder API
    compileOnly("me.clip:placeholderapi:2.11.2") { isTransitive = false }

    compileOnly("org.apache.commons:commons-text:1.10.0")
    compileOnly("org.jetbrains:annotations:23.0.0")

    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
    targetCompatibility = JavaVersion.VERSION_17
    sourceCompatibility = JavaVersion.VERSION_17
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
        filesMatching("plugin.yml") {
            expand(Pair("projectVersion", project.version))
        }
    }
    runServer {
        minecraftVersion("1.20.1")
    }
    test {
        useJUnitPlatform()
    }
    shadowJar {
        relocate("org.bstats", "com.github.devcyntrix.deathchest.metrics")
    }
}

hangarPublish {
    publications.register("DeathChest") {
        version.set(project.version as String)
        namespace("CyntrixAlgorithm", "DeathChest")
        changelog.set("https://github.com/DevCyntrix/death-chest/blob/main/CHANGELOG")

        apiKey.set(System.getenv("API_KEY"))

        if (!project.version.toString().contains('-')) {
            channel.set("Release")
        } else {
            channel.set("Snapshot")
        }

        platforms {
            register(Platforms.PAPER) {
                jar.set(tasks.shadowJar.flatMap { it.archiveFile })
                platformVersions.set(listOf("1.17-1.20.1"))
                dependencies.url("ProtocolLib", "https://www.spigotmc.org/resources/protocollib.1997/") {
                    required.set(false)
                }
                dependencies.url("PlaceholderAPI", "https://www.spigotmc.org/resources/placeholderapi.6245/") {
                    required.set(false)
                }
                dependencies.url("GriefPrevention", "https://www.spigotmc.org/resources/griefprevention.1884/") {
                    required.set(false)
                }
                dependencies.url("RedProtect", "https://www.spigotmc.org/resources/redprotect-anti-grief-server-protection-region-management-1-7-1-19.15841/") {
                    required.set(false)
                }
                dependencies.url("GriefDefender", "https://www.spigotmc.org/resources/1-12-2-1-19-4-griefdefender-claim-plugin-grief-prevention-protection.68900/") {
                    required.set(false)
                }
                dependencies.url("WorldGuard", "https://dev.bukkit.org/projects/worldguard") {
                    required.set(false)
                }
            }
        }

    }
}