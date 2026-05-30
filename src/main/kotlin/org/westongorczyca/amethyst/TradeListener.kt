package org.westongorczyca.amethyst

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.event.inventory.InventoryCloseEvent

class TradeListener(private val plugin: JavaPlugin) : Listener {

    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        val holder = event.inventory.holder as? TradeInventoryHolder ?: return
        val player = event.whoClicked as? Player ?: return
        val session = holder.session

        val slot = event.rawSlot
        val isPlayerA = player == session.playerA

        if (slot >= 54) return

        val column = slot % 9

        if (column == 4) {
            event.isCancelled = true
            return
        }

        if (slot == 45 && isPlayerA) {
            event.isCancelled = true
            session.playerAAccepted = !session.playerAAccepted
            updateStatusIndicators(session)
            return
        }
        if (slot == 53 && !isPlayerA) {
            event.isCancelled = true
            session.playerBAccepted = !session.playerBAccepted
            updateStatusIndicators(session)
            return
        }

        if (isPlayerA && column > 3) {
            event.isCancelled = true
            return
        }
        if (!isPlayerA && column < 5) {
            event.isCancelled = true
            return
        }

        session.playerAAccepted = false
        session.playerBAccepted = false
        updateStatusIndicators(session)
    }

    private fun updateStatusIndicators(session: TradeSession) {
        val inv = session.inventory

        val green = ItemStack(Material.GREEN_STAINED_GLASS_PANE)
        val red = ItemStack(Material.RED_STAINED_GLASS_PANE)

        inv.setItem(45, if (session.playerAAccepted) green else red)
        inv.setItem(53, if (session.playerBAccepted) green else red)

        if (session.playerAAccepted && session.playerBAccepted) {
            completeTrade(session)
        }
    }

    private fun completeTrade(session: TradeSession) {
        session.isFinalizing = true

        val inv = session.inventory
        val leftItems = mutableListOf<ItemStack>()
        val rightItems = mutableListOf<ItemStack>()

        for (slot in 0 until 54) {
            val item = inv.getItem(slot) ?: continue
            if (item.type == org.bukkit.Material.AIR) continue

            val column = slot % 9

            if (column < 4) {
                leftItems.add(item)
            }
            else if (column > 4 && slot != 53) {
                if (slot != 45) {
                    rightItems.add(item)
                }
            }
        }

        inv.clear()
        giveItemsOrDrop(session.playerA, rightItems)
        giveItemsOrDrop(session.playerB, leftItems)

        session.playerA.sendMessage(net.kyori.adventure.text.Component.text("Trade completed successfully", net.kyori.adventure.text.format.NamedTextColor.GREEN))
        session.playerB.sendMessage(net.kyori.adventure.text.Component.text("Trade completed successfully", net.kyori.adventure.text.format.NamedTextColor.GREEN))

        org.bukkit.Bukkit.getScheduler().runTask(plugin, Runnable {
            session.playerA.closeInventory()
            session.playerB.closeInventory()
        })
    }

    private fun giveItemsOrDrop(player: Player, items: List<ItemStack>) {
        for (item in items) {
            val overFlow = player.inventory.addItem(item)

            if (overFlow.isNotEmpty()) {
                for (leftOver in overFlow.values) {
                    player.world.dropItemNaturally(player.location, leftOver)
                }
            }
        }
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val holder = event.inventory.holder as? TradeInventoryHolder ?: return
        val session = holder.session

        if (session.isFinalizing) return

        session.isFinalizing = true

        val inv = event.inventory
        val refundA = mutableListOf<ItemStack>()
        val refundB = mutableListOf<ItemStack>()

        for (slot in 0 until 54) {
            val item = inv.getItem(slot) ?: continue
            if (item.type == Material.AIR) continue

            val column = slot % 9

            if (column < 4 && slot != 45) {
                refundA.add(item)
            }
            else if (column > 4 && slot != 53) {
                refundB.add(item)
            }
        }

        inv.clear()

        giveItemsOrDrop(session.playerA, refundA)
        giveItemsOrDrop(session.playerB, refundB)

        // 6. Notify both players that the transaction was canceled
        val cancelMessage = net.kyori.adventure.text.Component.text(
            "The trade was canceled or closed. Items have been returned.",
            net.kyori.adventure.text.format.NamedTextColor.RED
        )
        session.playerA.sendMessage(cancelMessage)
        session.playerB.sendMessage(cancelMessage)

        org.bukkit.Bukkit.getScheduler().runTask(plugin, Runnable {
            if (event.player == session.playerA) {
                session.playerB.closeInventory()
            } else {
                session.playerA.closeInventory()
            }
        })
    }
}