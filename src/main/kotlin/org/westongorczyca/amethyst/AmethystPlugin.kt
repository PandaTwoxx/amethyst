package org.westongorczyca.amethyst

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.LootGenerateEvent
import org.bukkit.plugin.java.JavaPlugin
import kotlin.random.Random

@Suppress("unused")
class AmethystPlugin : JavaPlugin(), Listener {

    override fun onEnable() {
        val combatManager = CombatManager()
        val teleportManager = TeleportManager(this, combatManager)
        val homeManager = HomeManager(this)
        val tradeManager = TradeManager()
        CustomItems.init(this)
        RecipeRegistry(this).registerAll()
        server.pluginManager.registerEvents(TradeListener(this), this)
        server.pluginManager.registerEvents(this, this)
        server.pluginManager.registerEvents(EchoPickaxeListener(), this)
        server.pluginManager.registerEvents(CombatListener(combatManager), this)
        server.pluginManager.registerEvents(TeleportMoveListener(teleportManager), this)

        lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS) { event ->
            val registrar = event.registrar()

            val spawnCommand = Commands.literal("spawn")
                .executes { context ->
                    val player = context.source.sender as? Player ?: return@executes Command.SINGLE_SUCCESS
                    teleportManager.startTeleport(player, player.world.spawnLocation, warmupSeconds = 3)
                    Command.SINGLE_SUCCESS
                }
            registrar.register(spawnCommand.build(), "Go to spawn", emptyList())

            val rtpCommand = Commands.literal("rtp")
                .executes { context ->
                    val player = context.source.sender as? Player ?: return@executes Command.SINGLE_SUCCESS
                    val target = findSafeLocation(player.world, 10000)
                    if (target != null) {
                        teleportManager.startTeleport(player, target, warmupSeconds = 5)
                    } else {
                        player.sendMessage(net.kyori.adventure.text.Component.text("No safe location found!", net.kyori.adventure.text.format.NamedTextColor.RED))
                    }
                    Command.SINGLE_SUCCESS
                }
            registrar.register(rtpCommand.build(), "Random teleport within 20000 blocks of spawn", emptyList())

            val setHomeCommand = Commands.literal("sethome")
                .then(
                    Commands.argument("name", StringArgumentType.word())
                        .executes { context ->
                            val player = context.source.sender as? Player ?: return@executes Command.SINGLE_SUCCESS
                            val homeName = StringArgumentType.getString(context, "name")

                            homeManager.setHome(player.uniqueId, homeName, player.location)
                            player.sendMessage(net.kyori.adventure.text.Component.text("Home '$homeName' set!", net.kyori.adventure.text.format.NamedTextColor.GREEN))
                            Command.SINGLE_SUCCESS
                        }
                )
            registrar.register(setHomeCommand.build(), "Set a home location", emptyList())

            val homeCommand = Commands.literal("home")
                .then(
                    Commands.argument("name", StringArgumentType.word())
                        .suggests { context, builder ->
                            val player = context.source.sender as? Player ?: return@suggests builder.buildFuture()
                            homeManager.getHomeNames(player.uniqueId).forEach { builder.suggest(it) }
                            builder.buildFuture()
                        }
                        .executes { context ->
                            val player = context.source.sender as? Player ?: return@executes Command.SINGLE_SUCCESS
                            val homeName = StringArgumentType.getString(context, "name")
                            val location = homeManager.getHome(player.uniqueId, homeName)

                            if (location != null) {
                                teleportManager.startTeleport(player, location, warmupSeconds = 5)
                            } else {
                                player.sendMessage(net.kyori.adventure.text.Component.text("Home not found!", net.kyori.adventure.text.format.NamedTextColor.RED))
                            }
                            Command.SINGLE_SUCCESS
                        }
                )

            val tpaCommand = Commands.literal("tpa")
                .then(
                    Commands.argument("player", io.papermc.paper.command.brigadier.argument.ArgumentTypes.player())
                        .executes { context ->
                            val sender = context.source.sender as? Player ?: return@executes Command.SINGLE_SUCCESS

                            // Fetch the resolved player selector argument natively
                            val targetSelector = context.getArgument("player", io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver::class.java)
                            val target = targetSelector.resolve(context.source).firstOrNull()

                            if (target == null) {
                                sender.sendMessage(net.kyori.adventure.text.Component.text("Player not found.", net.kyori.adventure.text.format.NamedTextColor.RED))
                                return@executes Command.SINGLE_SUCCESS
                            }

                            if (sender == target) {
                                sender.sendMessage(net.kyori.adventure.text.Component.text("You cannot send a request to yourself!", net.kyori.adventure.text.format.NamedTextColor.RED))
                                return@executes Command.SINGLE_SUCCESS
                            }

                            teleportManager.sendRequest(sender, target)
                            Command.SINGLE_SUCCESS
                        }
                )
            registrar.register(tpaCommand.build(), "Request to teleport to another player", emptyList())

            val tpacceptCommand = Commands.literal("tpaccept")
                .executes { context ->
                    val player = context.source.sender as? Player ?: return@executes Command.SINGLE_SUCCESS
                    teleportManager.acceptRequest(player)
                    Command.SINGLE_SUCCESS
                }
            registrar.register(tpacceptCommand.build(), "Accept a pending teleport request", emptyList())

            val tpdenyCommand = Commands.literal("tpdeny")
                .executes { context ->
                    val player = context.source.sender as? Player ?: return@executes Command.SINGLE_SUCCESS
                    teleportManager.denyRequest(player)
                    Command.SINGLE_SUCCESS
                }
            registrar.register(tpdenyCommand.build(), "Deny a pending teleport request", emptyList())
            registrar.register(homeCommand.build(), "Teleport to a home", emptyList())

            val combatCommand = Commands.literal("incombat")
                .executes { context ->
                    val player = context.source.sender as? Player ?: return@executes Command.SINGLE_SUCCESS
                    if (combatManager.isInCombat(player.uniqueId)) {
                        player.sendMessage("You are in combat for another ${combatManager.getRemainingTime(player.uniqueId)} seconds.")
                    } else {
                        player.sendMessage("You are not in combat")
                    }
                    Command.SINGLE_SUCCESS
                }
            registrar.register(combatCommand.build(), "Check if you are in combat", emptyList())
            val tpaHereCommand = Commands.literal("tpahere")
                .then(
                    Commands.argument("player", io.papermc.paper.command.brigadier.argument.ArgumentTypes.player())
                        .executes { context ->
                            val sender = context.source.sender as? Player ?: return@executes Command.SINGLE_SUCCESS

                            val targetSelector = context.getArgument("player", io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver::class.java)
                            val target = targetSelector.resolve(context.source).firstOrNull()

                            if (target == null) {
                                sender.sendMessage(net.kyori.adventure.text.Component.text("Player not found.", net.kyori.adventure.text.format.NamedTextColor.RED))
                                return@executes Command.SINGLE_SUCCESS
                            }

                            if (sender == target) {
                                sender.sendMessage(net.kyori.adventure.text.Component.text("You cannot teleport request yourself!", net.kyori.adventure.text.format.NamedTextColor.RED))
                                return@executes Command.SINGLE_SUCCESS
                            }
                            teleportManager.sendHereRequest(sender, target)
                            Command.SINGLE_SUCCESS
                        }
                )
            registrar.register(tpaHereCommand.build(), "Request another player to teleport to your location", emptyList())

            val tradeCommand = Commands.literal("trade")
                .then(
                    Commands.argument("player", io.papermc.paper.command.brigadier.argument.ArgumentTypes.player())
                        .executes { context ->
                            val sender = context.source.sender as? Player ?: return@executes Command.SINGLE_SUCCESS

                            val targetSelector = context.getArgument("player", io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver::class.java)
                            val target = targetSelector.resolve(context.source).firstOrNull()

                            if (target == null) {
                                sender.sendMessage(
                                    net.kyori.adventure.text.Component.text(
                                        "Player not found.",
                                        net.kyori.adventure.text.format.NamedTextColor.RED
                                    )
                                )
                                return@executes Command.SINGLE_SUCCESS
                            }

                            tradeManager.startTrade(sender, target)
                            Command.SINGLE_SUCCESS
                        }
                )
            registrar.register(tradeCommand.build(), "Open a trade window with another player", emptyList())

            val giveCommand = Commands.literal("amgive")
                .requires{ source -> source.sender.isOp() && source.sender is Player }
                .then(
                    Commands.argument("item", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .suggests { _, builder ->
                            // Provide neat drop-down tab completion in chat
                            builder.suggest("shard")
                            builder.suggest("template")
                            builder.suggest("pickaxe")
                            builder.buildFuture()
                        }
                        .executes { context ->
                            val player = context.source.sender as? Player ?: return@executes com.mojang.brigadier.Command.SINGLE_SUCCESS
                            val itemType = com.mojang.brigadier.arguments.StringArgumentType.getString(context, "item").lowercase()

                            // Resolve the item from your CustomItems factory
                            val itemToGive = when (itemType) {
                                "shard" -> CustomItems.createTemplateShard()
                                "template" -> CustomItems.createSmithingTemplate()
                                "pickaxe" -> CustomItems.createEchoPickaxe()
                                else -> {
                                    player.sendMessage(net.kyori.adventure.text.Component.text("Unknown custom item type!", net.kyori.adventure.text.format.NamedTextColor.RED))
                                    return@executes com.mojang.brigadier.Command.SINGLE_SUCCESS
                                }
                            }

                            // Give the item cleanly using your safe inventory fallback method
                            val overFlow = player.inventory.addItem(itemToGive)
                            if (overFlow.isNotEmpty()) {
                                for (leftOver in overFlow.values) {
                                    player.world.dropItemNaturally(player.location, leftOver)
                                }
                                player.sendMessage(net.kyori.adventure.text.Component.text("Your inventory was full! Item dropped at your feet.", net.kyori.adventure.text.format.NamedTextColor.YELLOW))
                            } else {
                                player.sendMessage(
                                    net.kyori.adventure.text.Component.text("Gave you 1x ", net.kyori.adventure.text.format.NamedTextColor.GREEN)
                                        .append(itemToGive.itemMeta?.displayName() ?: net.kyori.adventure.text.Component.text(itemType))
                                )
                            }

                            com.mojang.brigadier.Command.SINGLE_SUCCESS
                        }
                )
            registrar.register(giveCommand.build(), "Give custom Amethyst items", listOf("agive"))
        }
    }

    @EventHandler
    fun onLootGenerate(event: LootGenerateEvent) {
        val lootTableKey = event.lootTable.key.key

        if (lootTableKey.contains("ancient_city")) { 
            if (Random.nextDouble() < 0.30) {
                event.loot.add(CustomItems.createTemplateShard()) 
            } 
        }

        if (lootTableKey.contains("mineshaft")) { 
            if (Random.nextDouble() < 0.10) {
                event.loot.add(CustomItems.createTemplateShard()) 
            } 
        }
    }

    override fun onDisable() {
        logger.info("Amethyst disabled!")
    }
}