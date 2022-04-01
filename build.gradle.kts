plugins {
    `java-library`
    id("xyz.jpenilla.run-paper") version "1.0.6"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
}

group = "de.helixdevs"
version = "1.2.3"

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
}

dependencies {
    api("org.spigotmc:spigot-api:1.18.2-R0.1-SNAPSHOT")
    api("com.gmail.filoghost.holographicdisplays:holographicdisplays-api:2.4.9")
    api("com.sk89q.worldguard:worldguard-bukkit:7.0.7")
    api("com.comphenix.protocol:ProtocolLib:4.7.0")

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
    apiVersion = "1.18"
    authors = listOf("CyntrixAlgorithm")
    softDepend = listOf("WorldGuard", "HolographicDisplays", "ProtocolLib")
    commands {
        register("deathchest") {
            description = "The admin command for reloading the plugin's configuration"
            permission = "deathchest.admin"
            usage = "§c/<command> <reload>"
        }

    }
}