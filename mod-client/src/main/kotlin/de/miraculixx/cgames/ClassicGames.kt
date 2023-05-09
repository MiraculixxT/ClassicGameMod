package de.miraculixx.cgames

import com.labymedia.ultralight.UltralightRenderer
import de.miraculixx.cgames.ultralight.UltralightEngine
import de.miraculixx.cgames.utils.logger
import kotlinx.coroutines.runBlocking
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents
import net.kyori.adventure.audience.Audience
import net.minecraft.client.Minecraft
import java.io.File

lateinit var console: Audience
lateinit var ultralightRenderer: UltralightRenderer
lateinit var client: Minecraft
val rootConfigFolder = File("config/classicgames")

class ClassicGames : ModInitializer {
    private lateinit var config: File

    override fun onInitialize() {
        ClientLifecycleEvents.CLIENT_STARTED.register(ClientLifecycleEvents.ClientStarted { client: Minecraft ->
            de.miraculixx.cgames.client = client
            logger.info("Booted up")

            if (!rootConfigFolder.exists()) rootConfigFolder.mkdir()
            runBlocking {
                UltralightEngine.init()
            }
        })

        ClientLifecycleEvents.CLIENT_STOPPING.register(ClientLifecycleEvents.ClientStopping { client: Minecraft? ->
            logger.info("Shut down")
        })
    }
}