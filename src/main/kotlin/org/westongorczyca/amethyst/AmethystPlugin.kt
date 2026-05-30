package org.westongorczyca.amethyst

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin

@Suppress("unused")
class AmethystPlugin : JavaPlugin() {

    override fun onEnable() {
        val combatManager = CombatManager()
        val teleportManager = TeleportManager(this, combatManager)
        val homeManager = HomeManager(this)
        server.pluginManager.registerEvents(CombatListener(combatManager), this)
        server.pluginManager.registerEvents(TeleportMoveListener(teleportManager), this)

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val registrar = event.registrar()
            val spawnCommand = Commands.literal("spawn")
                .executes { context ->
                    val player = context.source.sender as? Player ?: return@executes Command.SINGLE_SUCCESS

                    val spawnLocation = player.world.spawnLocation

                    teleportManager.startTeleport(player, spawnLocation, warmupSeconds = 3)
                    Command.SINGLE_SUCCESS
                }

            registrar.register(spawnCommand.build(), "Go to spawn", emptyList())
        }

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val registrar = event.registrar()
            val rtpCommand = Commands.literal("rtp")
                .executes { context ->
                    val player = context.source.sender as? Player ?: return@executes Command.SINGLE_SUCCESS

                    val target = findSafeLocation(player.world, 20000)
                    if (target != null) {
                        teleportManager.startTeleport(player, target, warmupSeconds = 5)
                    }

                    Command.SINGLE_SUCCESS
                }

            registrar.register(rtpCommand.build(), "Random teleport within 20000 blocks of spawn", emptyList())
        }

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val registrar = event.registrar()
            val setHomeCommand = Commands.literal("sethome")
                .then(Commands.argument("name", StringArgumentType.word()))
                .executes { context ->
                    val player: Player = context.source.sender as? Player ?: return@executes Command.SINGLE_SUCCESS
                    homeManager.setHome(player.uniqueId, StringArgumentType.getString(context, "name"), player.location)

                    Command.SINGLE_SUCCESS
                }

            registrar.register(setHomeCommand.build(), "Set a home location", emptyList())
        }

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val registrar = event.registrar()
            val homeCommand = Commands.literal("home")
                .then(Commands.argument("name", StringArgumentType.word()))
                .executes { context ->
                    val player: Player = context.source.sender as? Player ?: return@executes Command.SINGLE_SUCCESS
                    val location = homeManager.getHome(player.uniqueId, StringArgumentType.getString(context, "name"))
                    if (location != null) {
                        teleportManager.startTeleport(player, location, warmupSeconds = 5)
                    }

                    Command.SINGLE_SUCCESS
                }

            registrar.register(homeCommand.build(), "Teleport to the home", emptyList())
        }

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val registrar = event.registrar()
            val combatCommand = Commands.literal("incombat")
                .executes { context ->
                    val player: Player = context.source.sender as? Player ?: return@executes Command.SINGLE_SUCCESS
                    if(combatManager.isInCombat(player.uniqueId)){
                        player.sendMessage("You are in combat for another ${combatManager.getRemainingTime(player.uniqueId)} seconds.")
                    }else{
                        player.sendMessage("You are not in combat")
                    }

                    Command.SINGLE_SUCCESS
                }

            registrar.register(combatCommand.build(), "Check if you are in combat", emptyList())
        }
    }

    override fun onDisable() {
        logger.info("Amethyst disabled!")
    }
}