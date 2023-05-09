package de.miraculixx.cgames.commands

import net.silkmc.silk.commands.command

class PlayCommand {
    val playCommand = command("play") {
        requires { it.isPlayer }

    }
}