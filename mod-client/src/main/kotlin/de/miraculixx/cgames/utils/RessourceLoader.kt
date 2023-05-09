package de.miraculixx.cgames.utils

import java.io.File
import kotlin.system.exitProcess

class UltralightResources {

    companion object {

        /**
         * Exact library version of the LabyMod Ultralight Bindings.
         */
        private const val LIBRARY_VERSION = 0.46
        private const val CLIENT_CLOUD = "https://cloud.liquidbounce.net/LiquidBounce"
    }

    private val ultralightRoot = File("assets/cgames", "ultralight")
    val binRoot = File(ultralightRoot, "bin")
    val cacheRoot = File(ultralightRoot, "cache")
    val resourcesRoot = File(ultralightRoot, "resources")

    private val OS = System.getProperty("os.name").lowercase()
    var IS_WINDOWS = OS.indexOf("win") >= 0
    var IS_MAC = OS.indexOf("mac") >= 0 || OS.indexOf("darwin") >= 0
    var IS_UNIX = OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0 || OS.indexOf("aix") > 0

    /**
     * Download resources
     */
    suspend fun downloadResources() {
        runCatching {
            val versionsFile = File(ultralightRoot, "VERSION")

            // Check if library version is matching the resources version
            if (versionsFile.exists() && versionsFile.readText().toDoubleOrNull() == LIBRARY_VERSION) {
                return
            }

            // Make sure the old natives are being deleted
            if (binRoot.exists()) {
                binRoot.deleteRecursively()
            }

            if (resourcesRoot.exists()) {
                resourcesRoot.deleteRecursively()
            }

            // Translate os to path
            val os = when {
                IS_WINDOWS -> "win"
                IS_MAC -> "mac"
                IS_UNIX -> "linux"
                else -> error("unsupported operating system")
            }

            logger.info("Downloading v$LIBRARY_VERSION resources... (os: $os)")
            val nativeUrl = "${CLIENT_CLOUD}/ultralight_resources/$LIBRARY_VERSION/$os-x64.zip"

            // Download resources
            ultralightRoot.mkdir()
            val pkgNatives = File(ultralightRoot, "resources.zip").apply {
                createNewFile()
                val data = WebClient.getFile(nativeUrl)
                if (data != null) writeBytes(data)
                else error("Failed to download resources!")
            }

            // Extract resources from zip archive
            logger.info("Extracting resources...")
            ZipUtils.unZipFolder(pkgNatives.path, ultralightRoot.path)
            versionsFile.createNewFile()
            versionsFile.writeText(LIBRARY_VERSION.toString())

            // Make sure to delete zip archive to save space
            logger.debug("Deleting resources bundle...")
            pkgNatives.delete()

            logger.info("Successfully loaded resources.")
        }.onFailure {
            logger.error("Unable to download resources")

            exitProcess(-1)
        }
    }
}