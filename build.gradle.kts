plugins {
    `java-library`
    id("xyz.jpenilla.run-paper") version "1.0.6"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "de.helixdevs"
version = "1.4.4"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://maven.enginehub.org/repo/")

    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://raw.githubusercontent.com/FabioZumbi12/RedProtect/mvn-repo/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.18-R0.1-SNAPSHOT")

    // Protection Support
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.7")
    compileOnly("com.plotsquared:PlotSquared-Core:6.8.1")
    compileOnly("com.plotsquared:PlotSquared-Bukkit:6.8.1") { isTransitive = false }
    compileOnly("com.github.TechFortress:GriefPrevention:16.18")
    compileOnly("br.net.fabiozumbi12.RedProtect:RedProtect-Core:7.7.3") { isTransitive = false }
    compileOnly("br.net.fabiozumbi12.RedProtect:RedProtect-Spigot:7.7.3") { isTransitive = false }


    // Animation Support
    compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0")

    // Hologram Support
    compileOnly("com.github.sainttx.Holograms:holograms-api:0f5b027")
    compileOnly("com.github.decentsoftware-eu:decentholograms:2.3.1")
    compileOnly("com.gmail.filoghost.holographicdisplays:holographicdisplays-api:2.4.9")


    implementation("org.apache.commons:commons-text:1.9")
    implementation("org.jetbrains:annotations:23.0.0")
    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.22")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
    }
    shadowJar {
        relocate("org.apache", "de.helixdevs.deathchest.lib.org.apache")
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
    processResources {
        filteringCharset = Charsets.UTF_8.name()
    }
    runServer {
        minecraftVersion("1.19")
    }
    test {
        useJUnitPlatform()
    }
}

bukkit {
    name = "DeathChest"
    main = "de.helixdevs.deathchest.DeathChestPlugin"
    apiVersion = "1.18"
    website = "https://www.spigotmc.org/resources/death-chest.101066/"
    authors = listOf("CyntrixAlgorithm")
    softDepend = listOf("WorldGuard", "HolographicDisplays", "ProtocolLib", "DecentHolograms", "GriefPrevention")
    commands {
        register("deathchest") {
            description = "The admin command for reloading the plugin's configuration"
            permission = "deathchest.admin"
            usage = "??c/<command> <reload>"
        }
    }
}