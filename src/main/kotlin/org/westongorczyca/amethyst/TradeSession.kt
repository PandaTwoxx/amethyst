package org.westongorczyca.amethyst

import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

class TradeSession(val playerA: Player, val playerB: Player) {
    lateinit var inventory: Inventory

    var playerAAccepted = false
    var playerBAccepted = false

    // Track if the trade is locked into its final countdown phase
    var isFinalizing = false

    fun getOther(player: Player): Player {
        return if (player == playerA) playerB else playerA
    }
}