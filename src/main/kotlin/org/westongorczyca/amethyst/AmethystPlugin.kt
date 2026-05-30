package org.westongorczyca.amethyst

import com.mojang.brigadier.Command
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

class AmethystPlugin : JavaPlugin() {

    override fun onEnable() {
        val teleportManager = TeleportManager(this)
        val manager = this.lifecycleManager
        server.pluginManager.registerEvents(TeleportMoveListener(teleportManager), this)

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val spawnCommand = Commands.literal("spawn")
                .executes { context ->
                    val player = context.source.sender as? Player ?: return@executes Command.SINGLE_SUCCESS

                    val spawnLocation = player.world.spawnLocation

                    teleportManager.startTeleport(player, spawnLocation, warmupSeconds = 3)
                    Command.SINGLE_SUCCESS
                }
        }

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) {
            val rtpCommand = Commands.literal("rtp")
                .executes { context ->
                    val player = context.source.sender as? Player ?: return@executes Command.SINGLE_SUCCESS

                    val target = findSafeLocation(player.world, 20000)
                    if (target != null) {
                        teleportManager.startTeleport(player, target, warmupSeconds = 5)
                    }

                    Command.SINGLE_SUCCESS
                }
        }
    }

    override fun onDisable() {
        logger.info("Amethyst disabled!")
    }
}