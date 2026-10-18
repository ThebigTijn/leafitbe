import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

plugins {
    java
    alias(libs.plugins.lombok)
    alias(libs.plugins.shadow)
    alias(libs.plugins.paperweight)
    alias(libs.plugins.run.paper)
}

val MAINTAINERS = listOf("ThebigTijn")

group = "dev.outfoxed"
version = "1"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.deepsite.gg/releases")
    maven("https://maven.deepsite.gg/snapshots")
    maven("https://repo.papermc.io/repository/maven-public/")
}

// Our SNAPSHOTs change often, so don't cache them for Gradle's default 24h.
configurations.all {
    resolutionStrategy.cacheChangingModulesFor(5, TimeUnit.MINUTES)
}

lombok.version = libs.versions.lombok.get()

dependencies {
    paperweight.paperDevBundle(libs.versions.paper.get())
    implementation(libs.modulemanager.spigot)
    implementation(libs.commandlib.paper)
    implementation(libs.bstats.bukkit)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.launcher)
}

tasks.test { useJUnitPlatform() }

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 25
}

tasks.processResources {
    inputs.property("version", version)
    filesMatching("paper-plugin.yml") { expand("version" to version) }
}

tasks.jar { enabled = false }
tasks.build { dependsOn(tasks.shadowJar) }

tasks.shadowJar {
    archiveFileName = "leafitbe.jar"
    relocate("com.jazzkuh.modulemanager", "dev.outfoxed.leafitbe.libs.modulemanager")
    relocate("com.jazzkuh.commandlib", "dev.outfoxed.leafitbe.libs.commandlib")
    relocate("org.bstats", "dev.outfoxed.leafitbe.libs.bstats")

    // Paper provides these at runtime.
    exclude("net/kyori/**", "org/slf4j/**", "javax/**", "org/jetbrains/**", "org/intellij/**", "org/jspecify/**")
    exclude("META-INF/versions/**", "META-INF/maven/**", "module-info.class")

    // Read by /leafitbe version.
    manifest {
        attributes["Built-By"] = System.getProperty("user.name")
        attributes["Built-JDK"] = System.getProperty("java.version")
        attributes["Build-Time"] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
        attributes["Implementation-Version"] = version
        attributes["Maintainers"] = MAINTAINERS.joinToString(", ")
    }
}

tasks.runServer {
    minecraftVersion("26.2")
    runDirectory(file("run"))
    jvmArgs("-Dcom.mojang.eula.agree=true", "-Dfile.encoding=UTF-8")
}
