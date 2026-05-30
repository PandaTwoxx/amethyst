package org.westongorczyca.amethyst

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.UUID
import kotlin.random.Random

class TeleportManager(private val plugin: JavaPlugin, private var combatManager: CombatManager) {
    private val pendingRequests = mutableMapOf<UUID, UUID>()
    private val requestExpirations = mutableMapOf<UUID, Long>()

    private val requestTimeoutMillis = 60_000L
    private val pendingTeleports = mutableMapOf<UUID, Location>()
    private val isHereRequest = mutableMapOf<UUID, Boolean>()

    fun sendHereRequest(sender: Player, target: Player) {
        val targetUuid = target.uniqueId

        pendingRequests[targetUuid] = sender.uniqueId
        requestExpirations[targetUuid] = System.currentTimeMillis() + requestTimeoutMillis
        isHereRequest[targetUuid] = true // Mark this specifically as a /tpahere request

        sender.sendMessage(net.kyori.adventure.text.Component.text("Teleport-here request sent to ${target.name}.", net.kyori.adventure.text.format.NamedTextColor.GREEN))

        target.sendMessage(
            net.kyori.adventure.text.Component.text("${sender.name} wants you to teleport to them.\n", net.kyori.adventure.text.format.NamedTextColor.YELLOW)
                .append(net.kyori.adventure.text.Component.text("Type /tpaccept to accept or /tpdeny to decline. Expires in 60s.", net.kyori.adventure.text.format.NamedTextColor.GOLD))
        )
    }

    fun sendRequest(sender: Player, target: Player) {
        val targetUuid = target.uniqueId

        pendingRequests[targetUuid] = sender.uniqueId
        requestExpirations[targetUuid] = System.currentTimeMillis() + requestTimeoutMillis

        sender.sendMessage(net.kyori.adventure.text.Component.text("Teleport request sent to ${target.name}.", net.kyori.adventure.text.format.NamedTextColor.GREEN))

        target.sendMessage(
            net.kyori.adventure.text.Component.text("${sender.name} has requested to teleport to you.\n", net.kyori.adventure.text.format.NamedTextColor.YELLOW)
                .append(net.kyori.adventure.text.Component.text("Type /tpaccept to accept or /tpdeny to decline. Expires in 60s.", net.kyori.adventure.text.format.NamedTextColor.GOLD))
        )
    }

    fun acceptRequest(target: Player) {
        val targetUuid = target.uniqueId
        val senderUuid = pendingRequests[targetUuid]
        val expireTime = requestExpirations[targetUuid]
        val isHere = isHereRequest[targetUuid] ?: false

        if (senderUuid == null || expireTime == null || System.currentTimeMillis() > expireTime) {
            pendingRequests.remove(targetUuid)
            requestExpirations.remove(targetUuid)
            isHereRequest.remove(targetUuid)
            target.sendMessage(net.kyori.adventure.text.Component.text("You have no active or unexpired teleport requests.", net.kyori.adventure.text.format.NamedTextColor.RED))
            return
        }

        val sender = org.bukkit.Bukkit.getPlayer(senderUuid)
        if (sender == null || !sender.isOnline) {
            target.sendMessage(net.kyori.adventure.text.Component.text("The player who sent the request is no longer online.", net.kyori.adventure.text.format.NamedTextColor.RED))
            return
        }

        pendingRequests.remove(targetUuid)
        requestExpirations.remove(targetUuid)
        isHereRequest.remove(targetUuid)

        if (isHere) {
            target.sendMessage(net.kyori.adventure.text.Component.text("Request accepted! Preparing to teleport you to ${sender.name}...", net.kyori.adventure.text.format.NamedTextColor.GREEN))
            sender.sendMessage(net.kyori.adventure.text.Component.text("${target.name} accepted your request. Teleporting them to you...", net.kyori.adventure.text.format.NamedTextColor.GREEN))

            // Target gets the warmup because target is the one moving!
            startTeleport(target, sender.location, warmupSeconds = 5)
        } else {
            target.sendMessage(net.kyori.adventure.text.Component.text("Request accepted! Teleporting ${sender.name}...", net.kyori.adventure.text.format.NamedTextColor.GREEN))

            startTeleport(sender, target.location, warmupSeconds = 5)
        }
    }

    fun denyRequest(target: Player) {
        val targetUuid = target.uniqueId
        val senderUuid = pendingRequests[targetUuid]

        if (senderUuid == null) {
            target.sendMessage(net.kyori.adventure.text.Component.text("You have no pending requests to deny.", net.kyori.adventure.text.format.NamedTextColor.RED))
            return
        }

        pendingRequests.remove(targetUuid)
        requestExpirations.remove(targetUuid)

        target.sendMessage(net.kyori.adventure.text.Component.text("You denied the request.", net.kyori.adventure.text.format.NamedTextColor.RED))

        val sender = org.bukkit.Bukkit.getPlayer(senderUuid)
        sender?.sendMessage(net.kyori.adventure.text.Component.text("${target.name} denied your teleport request.", net.kyori.adventure.text.format.NamedTextColor.RED))
    }

    fun startTeleport(player: Player, destination: Location, warmupSeconds: Int = 5) {
        val uuid = player.uniqueId
        if (combatManager.isInCombat(uuid)) {
            val secondsLeft = combatManager.getRemainingTime(uuid)
            player.sendMessage(
                Component.text("You cannot teleport while in combat! Wait $secondsLeft seconds.", NamedTextColor.RED)
            )
            return
        }

        if (pendingTeleports.containsKey(uuid)) {
            player.sendMessage(Component.text("You are already preparing to teleport", NamedTextColor.RED))
            return
        }

        pendingTeleports[uuid] = destination
        player.sendMessage(Component.text("Teleporting in $warmupSeconds seconds... Don't move!", NamedTextColor.YELLOW))

        object : BukkitRunnable() {
            var secondsLeft = warmupSeconds

            override fun run() {
                if (!pendingTeleports.containsKey(uuid)) {
                    this.cancel()
                    return
                }

                if (secondsLeft > 0) {
                    player.sendActionBar(
                        Component.text("Teleporting in ", NamedTextColor.GRAY)
                            .append(Component.text("$secondsLeft...", NamedTextColor.GOLD))
                    )
                    secondsLeft--
                } else {
                    player.sendActionBar(Component.text("Teleporting", NamedTextColor.GREEN))

                    player.teleportAsync(destination).thenAccept {
                        player.sendMessage(Component.text("Teleported successfully", NamedTextColor.GREEN))
                        pendingTeleports.remove(uuid)
                    }
                    this.cancel()
                }
            }
        }.runTaskTimer(plugin, 0L, 20L)
    }

    fun cancelTeleport(uuid: UUID) {
        pendingTeleports.remove(uuid)
    }

    fun isTeleporting(uuid: UUID): Boolean = pendingTeleports.containsKey(uuid)
}

fun findSafeLocation(world: org.bukkit.World, radius: Int): Location? {
    var attempts = 0
    while (attempts < 15) {
        attempts++
        val x = Random.nextInt(-radius, radius + 1)
        val z = Random.nextInt(-radius, radius + 1)
        val block = world.getHighestBlockAt(x, z)

        if (block.type != Material.LAVA && block.type != Material.WATER && block.type != Material.AIR) {
            return block.location.add(0.5, 1.0, 0.5)
        }
    }
    return null
}