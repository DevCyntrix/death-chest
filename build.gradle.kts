plugins {
    `java-library`
    id("xyz.jpenilla.run-paper") version "2.0.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.github.devcyntrix"
version = "1.5.24"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://maven.enginehub.org/repo/")

    maven("https://repo.codemc.io/repository/maven-public/")
    maven("https://repo.dmulloy2.net/repository/public/")
    maven("https://raw.githubusercontent.com/FabioZumbi12/RedProtect/mvn-repo/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.glaremasters.me/repository/bloodshot")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.17.1-R0.1-SNAPSHOT")

    // Protection Support
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.7")
    compileOnly("com.plotsquared:PlotSquared-Core:6.8.1") { isTransitive = false }
    compileOnly("com.plotsquared:PlotSquared-Bukkit:6.8.1") { isTransitive = false }
    compileOnly("com.github.TechFortress:GriefPrevention:16.18") { isTransitive = false }
    compileOnly("br.net.fabiozumbi12.RedProtect:RedProtect-Core:7.7.3") { isTransitive = false }
    compileOnly("br.net.fabiozumbi12.RedProtect:RedProtect-Spigot:7.7.3") { isTransitive = false }

    compileOnly("com.griefdefender:api:2.1.0-SNAPSHOT")

    // Animation Support
    compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0") { isTransitive = false }

    // Hologram Support
    compileOnly("com.github.sainttx.Holograms:holograms-api:0f5b027") { isTransitive = false }
    compileOnly("com.github.decentsoftware-eu:decentholograms:2.5.2") { isTransitive = false }
    compileOnly("com.gmail.filoghost.holographicdisplays:holographicdisplays-api:2.4.9") { isTransitive = false }

    // Placeholder API
    compileOnly("me.clip:placeholderapi:2.11.2") { isTransitive = false }

    //implementation("com.github.MP-MC:Canvas:941db8c981")
    compileOnly("org.apache.commons:commons-text:1.10.0")
    compileOnly("org.jetbrains:annotations:23.0.0")

    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
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
    }
    runServer {
        minecraftVersion("1.19.4")
    }
    test {
        useJUnitPlatform()
    }
}

bukkit {
    name = "DeathChest"
    main = "com.github.devcyntrix.deathchest.DeathChestPlugin"
    apiVersion = "1.13"
    website = "https://www.spigotmc.org/resources/death-chest.101066/"
    authors = listOf("CyntrixAlgorithm")
    softDepend = listOf(
        "WorldGuard",
        "ProtocolLib",
        "GriefPrevention",
        "GriefDefender",
        "PlaceholderAPI",
        "RedProtect"
    )
    libraries = listOf(
        "org.apache.commons:commons-text:1.9"
    )
    commands {
        register("deathchest") {
            description = "The admin command for reloading the plugin's configuration"
            permission = "deathchest.command.deathchest"
            usage = "§c/<command> <reload|deleteInWorld [<world>]>"
        }
    }

    permissions {
        register("deathchest.command.report") {
            description = "The permission to create, read and delete reports of the plugin"
        }
        register("deathchest.command.deleteInWorld") {
            description = "The permission to delete all chests in all or a specific worlds"
        }
        register("deathchest.command.reload") {
            description = "The permission to reload the configuration file of the DeathChest plugin"
        }
        register("deathchest.admin") {
            children = listOf(
                "deathchest.command.deathchest",
                "deathchest.command.report",
                "deathchest.command.deleteInWorld",
                "deathchest.command.reload"
            )
        }
    }
}