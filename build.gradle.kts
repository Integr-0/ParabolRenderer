import net.thebugmc.gradle.sonatypepublisher.PublishingType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.0.0"
    id("fabric-loom") version "1.8.9"
    id("maven-publish")
    id("net.thebugmc.gradle.sonatype-central-portal-publisher") version "1.2.4"
    `java-library`
    signing
}

description = "Internal rendering library for Parabol."
version = project.property("mod_version") as String
group = project.property("maven_group") as String

base {
    archivesName.set(project.property("archives_base_name") as String)
}

val targetJavaVersion = 21
java {
    toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    withSourcesJar()
    withJavadocJar()
}


repositories {

}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:${project.property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${project.property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${project.property("loader_version")}")
    modImplementation("net.fabricmc:fabric-language-kotlin:${project.property("kotlin_loader_version")}")

    modImplementation("net.fabricmc.fabric-api:fabric-api:${project.property("fabric_version")}")

    include(modImplementation("io.github.0x3c50.renderer:renderer-fabric:1.2.5")!!)
}

tasks.processResources {
    inputs.property("version", project.version)
    inputs.property("minecraft_version", project.property("minecraft_version"))
    inputs.property("loader_version", project.property("loader_version"))
    filteringCharset = "UTF-8"

    filesMatching("fabric.mod.json") {
        expand(
            "version" to project.version,
            "minecraft_version" to project.property("minecraft_version"),
            "loader_version" to project.property("loader_version"),
            "kotlin_loader_version" to project.property("kotlin_loader_version")
        )
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release.set(targetJavaVersion)
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.fromTarget(targetJavaVersion.toString()))
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${project.base.archivesName}" }
    }
}

val sonatypeUser = project.findProperty("sonatypeUser") as String
val sonatypePassword = project.findProperty("sonatypePassword") as String

centralPortal {
    username = sonatypeUser
    password = sonatypePassword
    name = "parabol-renderer"
    publishingType = PublishingType.AUTOMATIC
    jarTask = tasks.create<Jar>("builtModJar") {
        from(tasks.jar.get())
        archiveClassifier = null
    }

    pom {
        name = "parabol-renderer"
        description = "Internal rendering library for Parabol."
        url = "https://github.com/Integr-0/ParabolRenderer"
        licenses {
            license {
                name = "Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0"
            }
        }
        developers {
            developer {
                id = "integr"
                name = "Integr"
            }
        }
        scm {
            connection = "scm:git:https://github.com/Integr-0/ParabolRenderer.git"
            developerConnection = "scm:git:https://github.com/Integr-0/ParabolRenderer.git"
            url = "https://github.com/Integr-0/ParabolRenderer"
        }
    }
}

