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

class TeleportManager(private val plugin: JavaPlugin) {

    private val pendingTeleports = mutableMapOf<UUID, Location>()

    fun startTeleport(player: Player, destination: Location, warmupSeconds: Int = 5) {
        val uuid = player.uniqueId

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