package de.miraculixx.cgames.commands

import de.miraculixx.cgames.ultralight.UltralightEngine
import de.miraculixx.cgames.ultralight.ViewOverlayState
import de.miraculixx.cgames.ultralight.theme.Game
import de.miraculixx.cgames.ultralight.theme.Page
import net.silkmc.silk.commands.clientCommand

object PlayCommand {
    val playCommand = clientCommand("play") {
        literal("test") {
            runs {
                val view = UltralightEngine.newOverlayView()
                view.focus()
                view.state = ViewOverlayState.VISIBLE
                view.loadPage(Page(Game("test"), "main"))
                view.update()
                UltralightEngine.update()
            }
        }
    }
}