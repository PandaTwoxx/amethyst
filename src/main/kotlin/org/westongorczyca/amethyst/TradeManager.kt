package org.westongorczyca.amethyst

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

class TradeInventoryHolder(val session: TradeSession) : InventoryHolder {
    override fun getInventory(): Inventory = session.inventory
}

class TradeManager {
    private val activeSessions = mutableSetOf<TradeSession>()

    fun startTrade(playerA: Player, playerB: Player) {
        val session = TradeSession(playerA, playerB)
        val holder = TradeInventoryHolder(session)

        val inv = Bukkit.createInventory(holder, 54, Component.text("Trading with ${playerB.name}"))
        session.inventory = inv

        val divider = ItemStack(Material.GRAY_STAINED_GLASS_PANE).apply {
            itemMeta = itemMeta?.apply { displayName(Component.empty()) }
        }
        for (row in 0..5) {
            inv.setItem((row * 9) + 4, divider)
        }

        val notReady = ItemStack(Material.RED_STAINED_GLASS_PANE).apply {
            itemMeta = itemMeta?.apply { displayName(Component.text("Click to Accept", net.kyori.adventure.text.format.NamedTextColor.RED)) }
        }
        inv.setItem(45, notReady)
        inv.setItem(53, notReady)

        activeSessions.add(session)
        playerA.openInventory(inv)
        playerB.openInventory(inv)
    }
}