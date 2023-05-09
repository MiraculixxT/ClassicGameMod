package de.miraculixx.cgames

import com.labymedia.ultralight.UltralightRenderer
import com.labymedia.ultralight.UltralightView
import com.labymedia.ultralight.config.UltralightViewConfig
import de.miraculixx.cgames.utils.logger
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.kyori.adventure.audience.Audience
import net.minecraft.client.Minecraft
import java.io.File

lateinit var console: Audience
lateinit var ultralightRenderer: UltralightRenderer

class ClassicGames : ModInitializer {
    private lateinit var config: File

    override fun onInitialize() {
        ClientLifecycleEvents.CLIENT_STARTED.register(ClientLifecycleEvents.ClientStarted { client: Minecraft ->
            client.player
            logger.info("Booted up")

            val config = UltralightViewConfig()
                .enableImages(true)
                .enableJavascript(true)
                .isAccelerated(true)
                .userAgent("ClassicGames-Client-1.1")
            ultralightRenderer = UltralightRenderer.create()
            val window = client.window
            val view = ultralightRenderer.createView(window.width.toLong(), window.height.toLong(), config)
            view.loadURL("https://mutils.de")
        })

        ClientLifecycleEvents.CLIENT_STOPPING.register(ClientLifecycleEvents.ClientStopping { client: Minecraft? ->
            logger.info("Shut down")
        })
    }
}