package org.westongorczyca.amethyst

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerQuitEvent

class CombatListener(private val combatManager: CombatManager) : Listener {
    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        combatManager.clearTag(event.entity.uniqueId)
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPVPDamage(event: EntityDamageByEntityEvent){
        val victim = event.entity as? Player ?: return
        val attacker = event.damager as? Player ?: return

        if (victim == attacker) return

        if (!combatManager.isInCombat(victim.uniqueId)) {
            victim.sendMessage(Component.text("You are now in combat! Do not log out.", NamedTextColor.RED))
        }
        combatManager.tagPlayer(victim)

        if (!combatManager.isInCombat(attacker.uniqueId)) {
            attacker.sendMessage(Component.text("You are now in combat! Do not log out.", NamedTextColor.RED))
        }
        combatManager.tagPlayer(attacker)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player

        if (combatManager.isInCombat(player.uniqueId)) {
            player.health = 0.0
            combatManager.clearTag(player.uniqueId)

            event.quitMessage(
                Component.text("${player.name} combat logged and was executed", NamedTextColor.DARK_RED)
            )
        }
    }
}