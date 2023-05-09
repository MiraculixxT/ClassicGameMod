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

val transitiveInclude: Configuration by configurations.creating {
    exclude(group = "com.mojang")
    exclude(group = "org.jetbrains.kotlin")
    exclude(group = "org.jetbrains.kotlinx")
}

dependencies {
    minecraft("com.mojang:minecraft:${minecraftVersion}")
    mappings(loom.officialMojangMappings())

    transitiveInclude(implementation("com.labymedia:ultralight-java-base:0.4.6")!!)
    transitiveInclude(implementation("com.labymedia:ultralight-java-databind:0.4.6")!!)
    transitiveInclude(implementation("com.labymedia:ultralight-java-gpu:0.4.6")!!)
    transitiveInclude(implementation("org.zeroturnaround:zt-zip:1.15")!!)

    modImplementation("net.silkmc:silk-core:1.9.8")
    modImplementation("net.silkmc:silk-commands:1.9.8")
    modImplementation("net.fabricmc:fabric-loader:0.14.19")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.80.0+1.19.4")
    modImplementation("net.fabricmc:fabric-language-kotlin:1.9.4+kotlin.1.8.21")
    modImplementation(include("net.kyori:adventure-platform-fabric:5.8.0")!!)
    modImplementation(include("me.lucko", "fabric-permissions-api", "0.2-SNAPSHOT"))

    transitiveInclude.resolvedConfiguration.resolvedArtifacts.forEach {
        include(it.moduleVersion.id.toString())
    }
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
            freeCompilerArgs += "-Xskip-prerelease-check"
        }
    }
}