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

import com.labymedia.ultralight.UltralightJava
import com.labymedia.ultralight.UltralightPlatform
import com.labymedia.ultralight.UltralightRenderer
import com.labymedia.ultralight.config.FontHinting
import com.labymedia.ultralight.config.UltralightConfig
import com.labymedia.ultralight.gpu.UltralightGPUDriverNativeUtil
import com.labymedia.ultralight.plugin.logging.UltralightLogLevel
import de.miraculixx.cgames.client
import de.miraculixx.cgames.ultralight.hooks.UltralightIntegrationHook
import de.miraculixx.cgames.ultralight.hooks.UltralightScreenHook
import de.miraculixx.cgames.ultralight.impl.BrowserFileSystem
import de.miraculixx.cgames.ultralight.impl.glfw.GlfwClipboardAdapter
import de.miraculixx.cgames.ultralight.impl.glfw.GlfwCursorAdapter
import de.miraculixx.cgames.ultralight.impl.glfw.GlfwInputAdapter
import de.miraculixx.cgames.ultralight.impl.renderer.CpuViewRenderer
import de.miraculixx.cgames.ultralight.js.bindings.UltralightStorage
import de.miraculixx.cgames.utils.ThreadLock
import de.miraculixx.cgames.utils.logger
import net.minecraft.client.gui.screens.Screen
import org.joml.Matrix3dStack

object UltralightEngine {

    val window = client.window.window
    var platform = ThreadLock<UltralightPlatform>()
    var renderer = ThreadLock<UltralightRenderer>()

    lateinit var clipboardAdapter: GlfwClipboardAdapter
    lateinit var cursorAdapter: GlfwCursorAdapter
    lateinit var inputAdapter: GlfwInputAdapter

    val inputAwareOverlay: ViewOverlay?
        get() = viewOverlays.find { it is ScreenViewOverlay && client.screen == it.screen && it.state == ViewOverlayState.VISIBLE }
    private val viewOverlays = mutableListOf<ViewOverlay>()

    val resources = UltralightResources()

    /**
     * Frame limiter
     */
    private const val MAX_FRAME_RATE = 60
    private var lastRenderTime = 0.0

    /**
     * Initializes the platform
     */
    suspend fun init() {
        logger.info("Loading ultralight...")
        initNatives()

        // Setup platform
        logger.debug("Setting up ultralight platform")
        platform.lock(UltralightPlatform.instance())
        platform.get().setConfig(
            UltralightConfig()
                .animationTimerDelay(1.0 / MAX_FRAME_RATE)
                .scrollTimerDelay(1.0 / MAX_FRAME_RATE)
                .resourcePath(resources.resourcesRoot.absolutePath)
                .cachePath(resources.cacheRoot.absolutePath)
                .fontHinting(FontHinting.SMOOTH)
        )
        platform.get().usePlatformFontLoader()
        platform.get().setFileSystem(BrowserFileSystem())
        platform.get().setClipboard(GlfwClipboardAdapter())
        platform.get().setLogger { level, message ->
            @Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
            when (level) {
                UltralightLogLevel.ERROR -> logger.error("[Ul] $message")
                UltralightLogLevel.WARNING -> logger.warn("[Ul] $message")
                UltralightLogLevel.INFO -> logger.info("[Ul] $message")
            }
        }

        // Setup renderer
        logger.info("Setting up ultralight renderer")

        val ulRenderer = UltralightRenderer.create()
        ulRenderer.logMemoryUsage()
        renderer.lock(ulRenderer)

        // Setup hooks
        UltralightIntegrationHook
        UltralightScreenHook

        UltralightStorage

        // Setup GLFW adapters
        clipboardAdapter = GlfwClipboardAdapter()
        cursorAdapter = GlfwCursorAdapter()
        inputAdapter = GlfwInputAdapter()

        logger.info("Successfully loaded ultralight!")
    }

    /**
     * Initializes the natives, this is required for ultralight to work.
     *
     * This will download the required natives and resources and load them.
     */
    private suspend fun initNatives() {
        // Check resources
        logger.info("Checking resources...")
        resources.downloadResources()

        // Load natives from native directory inside root folder
        logger.info("Loading ultralight natives")
        val natives = resources.binRoot.toPath()
        logger.info("Native path: $natives")

        logger.debug("Loading UltralightJava")
        UltralightJava.load(natives)
        logger.debug("Loading UltralightGPUDriver")
        UltralightGPUDriverNativeUtil.load(natives)
    }

    fun shutdown() {
        cursorAdapter.cleanup()
    }

    fun update() {
        viewOverlays
            .forEach(ViewOverlay::update)
        renderer.get().update()
    }

    fun render(layer: RenderLayer, matrices: Matrix3dStack) {
        frameLimitedRender()

        viewOverlays
            .filter { it.layer == layer && it.state != ViewOverlayState.HIDDEN }
            .forEach {
                it.render(matrices)
            }
    }

    private fun frameLimitedRender() {
        val frameTime = 1.0 / MAX_FRAME_RATE
        val time = System.nanoTime() / 1e9
        val delta = time - lastRenderTime

        if (delta < frameTime) {
            return
        }

        renderer.get().render()
        lastRenderTime = time
    }

    fun resize(width: Long, height: Long) {
        viewOverlays.forEach { it.resize(width, height) }
    }

    fun newSplashView() =
        ViewOverlay(RenderLayer.SPLASH_LAYER, newViewRenderer()).also { viewOverlays += it }

    fun newOverlayView() =
        ViewOverlay(RenderLayer.OVERLAY_LAYER, newViewRenderer()).also { viewOverlays += it }

    fun newScreenView(screen: Screen, adaptedScreen: Screen? = null, parentScreen: Screen? = null) =
        ScreenViewOverlay(newViewRenderer(), screen, adaptedScreen, parentScreen).also { viewOverlays += it }

    /**
     * Removes the view overlay from the list of overlays
     */
    fun removeView(viewOverlay: ViewOverlay) {
        viewOverlay.setOnStageChange {
            if (it == ViewOverlayState.END) {
                viewOverlay.free()
                viewOverlays.remove(viewOverlay)
            }
        }
    }

    /**
     * Creates a new view renderer
     */
    private fun newViewRenderer() = CpuViewRenderer()

}

enum class RenderLayer {
    OVERLAY_LAYER, SCREEN_LAYER, SPLASH_LAYER
}
