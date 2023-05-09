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
package de.miraculixx.cgames.ultralight.theme

import de.miraculixx.cgames.rootConfigFolder
import java.io.File
import java.nio.file.StandardWatchEventKinds.*

class Game(val name: String) {

    internal val themeFolder = File("$rootConfigFolder/games", name)

    val exists: Boolean
        get() = themeFolder.exists()

    fun page(name: String): Page? {
        val page = Page(this, name)

        if (page.exists) {
            return page
        }
        return null
    }

}

class Page(game: Game, val name: String) {

    private val pageFolder = File(game.themeFolder, name)

    val viewableFile: String
        get() = "file:///${File(pageFolder, "index.html").absolutePath}"

    val exists: Boolean
        get() = pageFolder.exists()

    private val watcher by lazy {
        val path = pageFolder.toPath()
        val watchService = path.fileSystem.newWatchService()
        path.register(watchService, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)
        watchService
    }

    fun hasUpdate(): Boolean {
        val watchKey = watcher.poll()
        val shouldUpdate = watchKey?.pollEvents()?.isNotEmpty() == true
        watchKey?.reset()
        return shouldUpdate
    }

    fun close() {
        watcher.close()
    }

    override fun toString() = "Page($name, $viewableFile)"

}
