package org.westongorczyca.amethyst

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor

class TeleportMoveListener(private val manager: TeleportManager) : Listener{
    @EventHandler
    fun onMove(event: PlayerMoveEvent) {
        val player = event.player
        if (!manager.isTeleporting(player.uniqueId)) return

        val from = event.from
        val to = event.to

        if (from.blockX != to.blockX || from.blockY != to.blockY || from.blockZ != to.blockZ) {
            manager.cancelTeleport(player.uniqueId)
            player.sendMessage(Component.text("Teleport cancelled", NamedTextColor.RED))
            player.sendActionBar(Component.empty())
        }
    }
}