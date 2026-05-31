package org.westongorczyca.amethyst

import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.persistence.PersistentDataType

class EchoPickaxeListener : Listener {
    private val breakingBlocks = mutableSetOf<Block>()

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onBlockBreak(event: BlockBreakEvent) {
        val player = event.player
        val mainHandItem = player.inventory.itemInMainHand

        val meta = mainHandItem.itemMeta ?: return
        if (!meta.persistentDataContainer.has(CustomItems.echoPickaxeKey, PersistentDataType.BOOLEAN)) return

        val centerBlock = event.block

        if (breakingBlocks.contains(centerBlock)) return

        spawnEchoParticle(centerBlock)

        val targetFace = player.getTargetBlockFace(5) ?: return
        val blocksToBreak = get3x3Blocks(centerBlock, targetFace)

        for (block in blocksToBreak) {
            if (block == centerBlock) continue

            val breakEvent = BlockBreakEvent(block, player)

            breakingBlocks.add(block)
            org.bukkit.Bukkit.getPluginManager().callEvent(breakEvent)

            if (!breakEvent.isCancelled) {
                block.breakNaturally(mainHandItem)
                spawnEchoParticle(block)
            }
            breakingBlocks.remove(block)
        }
    }

    private fun spawnEchoParticle(block: Block) {
        val centerLocation = block.location.add(0.5, 0.5, 0.5)
        block.world.spawnParticle(Particle.SCULK_CHARGE_POP, centerLocation, 5, 0.2, 0.2, 0.2, 0.0)
    }

    private fun get3x3Blocks(center: Block, face: BlockFace) : List<Block> {
        val list = mutableListOf<Block>()
        val world = center.world

        val (modX1, modY1, modZ1) = when (face) {
            BlockFace.UP, BlockFace.DOWN -> Triple(1, 0, 0)
            BlockFace.NORTH, BlockFace.SOUTH -> Triple(1, 0, 0)
            else -> Triple(0, 1, 0)
        }

        val (modX2, modY2, modZ2) = when (face) {
            BlockFace.UP, BlockFace.DOWN -> Triple(0, 0, 1)
            BlockFace.NORTH, BlockFace.SOUTH -> Triple(0, 1, 0)
            else -> Triple(0, 0, 1)
        }

        for (i in -1..1) {
            for (j in -1..1) {
                val x = center.x + (i * modX1) + (j * modX2)
                val y = center.y + (i * modY1) + (j * modY2)
                val z = center.z + (i * modZ1) + (j * modZ2)

                val relativeBlock = world.getBlockAt(x, y, z)

                if (relativeBlock.type.isBlock && !relativeBlock.isEmpty && relativeBlock.type != org.bukkit.Material.BEDROCK) {
                    list.add(relativeBlock)
                }
            }
        }

        return list
    }
}