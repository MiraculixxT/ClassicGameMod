import BuildConstants.minecraftVersion
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.withType

plugins {
    id("fabric-loom")
}

repositories {
    mavenCentral()
    maven {
        name = "JitPack"
        setUrl("https://jitpack.io")
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings(loom.officialMojangMappings())

    include(implementation("com.labymedia:ultralight-java-base:0.4.6")!!)
    include(implementation("com.labymedia:ultralight-java-databind:0.4.6")!!)
    include(implementation("com.labymedia:ultralight-java-gpu:0.4.6")!!)

    modImplementation("net.silkmc:silk-core:1.9.8")
    modImplementation("net.silkmc:silk-commands:1.9.8")
    modImplementation("net.fabricmc:fabric-loader:0.14.19")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.80.0+1.19.4")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.9.4+kotlin.1.8.21")
    modImplementation(include("net.kyori:adventure-platform-fabric:5.8.0")!!)
    modImplementation(include("me.lucko", "fabric-permissions-api", "0.2-SNAPSHOT"))
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs += "-Xskip-prerelease-check"
        }
    }
}