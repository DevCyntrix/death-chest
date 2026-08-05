import io.papermc.hangarpublishplugin.model.Platforms

plugins {
    java
    id("xyz.jpenilla.run-paper") version "3.0.2"
    id("com.gradleup.shadow") version "9.6.0"
    id("io.papermc.hangar-publish-plugin") version "0.1.4"
}

group = "com.github.devcyntrix"
version = "3.0.1"

repositories {
    mavenCentral()
    maven("https://libraries.minecraft.net")
    maven("https://repo.papermc.io/repository/maven-public/")
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
    compileOnly("com.mojang:brigadier:1.0.18")
    compileOnly("com.google.inject:guice:7.0.0")
    compileOnly("org.spigotmc:spigot-api:1.20-R0.1-SNAPSHOT")
    compileOnly("net.kyori:adventure-platform-bukkit:4.4.1")

    // Command library
    compileOnly("cloud.commandframework:cloud-core:1.8.4")
    compileOnly("cloud.commandframework:cloud-bukkit:1.8.4")

    // bStats
    implementation("org.bstats:bstats-bukkit:3.2.1")

    // Protection Support
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.9")

    //implementation(platform("com.intellectualsites.bom:bom-newest"))
    compileOnly("com.intellectualsites.plotsquared:plotsquared-core:7.2.1")
    compileOnly("com.intellectualsites.plotsquared:plotsquared-bukkit:7.2.1") { isTransitive = false }

    compileOnly("com.github.TechFortress:GriefPrevention:16.18.4") { isTransitive = false }
    compileOnly("io.github.fabiozumbi12.RedProtect:RedProtect-Core:8.1.2") { isTransitive = false }
    compileOnly("io.github.fabiozumbi12.RedProtect:RedProtect-Spigot:8.1.2") { isTransitive = false }
    compileOnly("pl.minecodes.plots:plugin-api:4.6.1")

    // Animation Support
    compileOnly("com.comphenix.protocol:ProtocolLib:5.3.0") { isTransitive = false }

    // Placeholder API
    compileOnly("me.clip:placeholderapi:2.11.6") { isTransitive = false }

    // Lock
    compileOnly("com.griefcraft:lwc:2.3.2-dev")

    compileOnly("org.apache.commons:commons-text:1.15.0")
    compileOnly("org.jetbrains:annotations:23.0.0")

    // TESTING

    testImplementation("org.junit.jupiter:junit-jupiter:6.1.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.mockbukkit.mockbukkit:mockbukkit-v1.21:4.110.0")
    // Paper is necessary for the mockbukkit library
    testImplementation("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")

    // Adventure
    testImplementation("net.kyori:adventure-platform-bukkit:4.3.0")
    testImplementation("net.kyori:adventure-text-minimessage:4.14.0")

    // Text
    testImplementation("org.apache.commons:commons-text:1.15.0")

    // Command
    testImplementation("cloud.commandframework:cloud-core:1.8.4")
    testImplementation("cloud.commandframework:cloud-bukkit:1.8.4")

    // Logging
    testImplementation("ch.qos.logback:logback-classic:1.5.38")

}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

val javaLauncherService = javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(25))
}

tasks {
    jar {
        enabled = false
    }
    assemble {
        dependsOn(shadowJar)
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    javadoc {
        options.encoding = "UTF-8"
    }
    processResources {
        filteringCharset = "UTF-8"
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
        filesMatching("plugin.yml") {
            expand(Pair("projectVersion", project.version))
        }
    }
    compileTestJava {
        options.release.set(21)
    }
    test {
        useJUnitPlatform()

        testLogging {
            events("passed", "skipped", "failed")
            showCauses = true
            showExceptions = true
        }
    }
    runServer {
        minecraftVersion("26.2")
        javaLauncher = javaLauncherService

    }
    shadowJar {
        relocate("org.bstats", "com.github.devcyntrix.deathchest.metrics")
        archiveFileName.set("deathchest.jar")
    }
}


hangarPublish {
    publications.register("DeathChest") {
        version.set(project.version as String)
        id = "DeathChest"
        changelog.set("https://github.com/DevCyntrix/death-chest/blob/main/CHANGELOG")

        apiKey.set(System.getenv("API_KEY"))

        if (!project.version.toString().contains('-')) {
            channel.set("Release")
        } else {
            channel.set("Snapshot")
        }

        platforms {
            register(Platforms.PAPER) {
                jar = tasks.shadowJar.flatMap { it.archiveFile }
                println(jar.get().asFile)
                println(version)
                platformVersions.set(listOf("1.20-26.2"))
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
                dependencies.url("minePlots", "https://builtbybit.com/resources/mineplots.21646/") {
                    required.set(false)
                }
                dependencies.url("LocketteX", "https://www.spigotmc.org/resources/lockettex-optimized-simple-chest-protection-plugin.73184/") {
                    required.set(false)
                }
                dependencies.url("LWC", "https://www.spigotmc.org/resources/lwc-extended.69551/") {
                    required.set(false)
                }
            }
        }

    }
}