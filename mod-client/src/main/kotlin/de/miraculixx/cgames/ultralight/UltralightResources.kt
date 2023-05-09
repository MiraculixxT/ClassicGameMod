/*
 * This file is part of LiquidBounce (https://github.com/CCBlueX/LiquidBounce)
 *
 * Copyright (c) 2016 - 2023 CCBlueX
 *
 * LiquidBounce is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LiquidBounce is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LiquidBounce. If not, see <https://www.gnu.org/licenses/>.
 */
package de.miraculixx.cgames.ultralight

import de.miraculixx.cgames.rootConfigFolder
import de.miraculixx.cgames.utils.WebClient
import de.miraculixx.cgames.utils.logger
import org.zeroturnaround.zip.ZipUtil
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

    val ultralightRoot = File(rootConfigFolder, "ultralight")
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
                WebClient.getFile(nativeUrl)?.let { writeBytes(it) } ?: logger.error("Failed to download Ultralight assets!")
            }

            // Extract resources from zip archive
            logger.info("Extracting resources...")
            ZipUtil.unpack(pkgNatives, ultralightRoot)
            versionsFile.createNewFile()
            versionsFile.writeText(LIBRARY_VERSION.toString())

            // Make sure to delete zip archive to save space
            logger.debug("Deleting resources bundle...")
            pkgNatives.delete()

            logger.info("Successfully loaded resources.")
        }.onFailure {
            logger.error("Unable to download resources", it)

            exitProcess(-1)
        }
    }

}
