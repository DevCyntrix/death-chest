plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "1.3.5"
    id("xyz.jpenilla.run-paper") version "1.0.6"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.1"
}

group = "de.helixdevs"
version = "1.1"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.enginehub.org/repo/")
    }
    maven {
        url = uri("https://repo.codemc.io/repository/maven-public/")
    }
}

dependencies {
    api("com.gmail.filoghost.holographicdisplays:holographicdisplays-api:2.4.9")
    api("com.sk89q.worldguard:worldguard-bukkit:7.0.7")

    paperDevBundle("1.18.2-R0.1-SNAPSHOT")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }
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
}

bukkit {
    name = "DeathChest"
    main = "de.helixdevs.deathchest.DeathChestPlugin"
    apiVersion = "1.18"
    authors = listOf("CyntrixAlgorithm")
    softDepend = listOf("WorldGuard", "HolographicDisplays")
    commands {
        register("deathchest") {
            description = "The admin command for reloading the plugin's configuration"
            permission = "deathchest.admin"
            usage = "§c/<command> <reload>"
        }

    }
}