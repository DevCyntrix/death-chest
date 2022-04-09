plugins {
    `java-library`
    id("xyz.jpenilla.run-paper") version "1.0.6"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
}

group = "de.helixdevs"
version = "1.3.2"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    }
    maven {
        url = uri("https://maven.enginehub.org/repo/")
    }
    maven {
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }
    maven {
        url = uri("https://repo.dmulloy2.net/repository/public/")
    }
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    api("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")

    // Protection Support
    api("com.sk89q.worldguard:worldguard-bukkit:7.0.7")
    api("com.plotsquared:PlotSquared-Core:6.6.1")
    api("com.plotsquared:PlotSquared-Bukkit:6.6.1") { isTransitive = false }
    api("com.github.TechFortress:GriefPrevention:16.18")

    // Animation Support
    api("com.comphenix.protocol:ProtocolLib:4.7.0")

    // Hologram Support
    api("com.github.sainttx.Holograms:holograms-api:0f5b027")
    api("com.github.decentsoftware-eu:decentholograms:2.3.1")
    api("com.gmail.filoghost.holographicdisplays:holographicdisplays-api:2.4.9")

    implementation("org.jetbrains:annotations:23.0.0")
    compileOnly("org.projectlombok:lombok:1.18.22")
    annotationProcessor("org.projectlombok:lombok:1.18.22")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
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
    }
    runServer {
        minecraftVersion("1.18.2")
    }
}

bukkit {
    name = "DeathChest"
    main = "de.helixdevs.deathchest.DeathChestPlugin"
    apiVersion = "1.15"
    authors = listOf("CyntrixAlgorithm")
    softDepend = listOf("WorldGuard", "HolographicDisplays", "ProtocolLib", "DecentHolograms", "GriefPrevention")
    commands {
        register("deathchest") {
            description = "The admin command for reloading the plugin's configuration"
            permission = "deathchest.admin"
            usage = "§c/<command> <reload>"
        }
    }
}