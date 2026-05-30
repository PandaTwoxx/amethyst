package org.westongorczyca.amethyst

import org.bukkit.entity.Player
import java.util.UUID

class CombatManager {

    private val combatTags = mutableMapOf<UUID, Long>()
    private val cooldownMillis = 10_000L // 10 seconds


    fun tagPlayer(player: Player) {
        val expireTime = System.currentTimeMillis() + cooldownMillis
        combatTags[player.uniqueId] = expireTime
    }

    fun isInCombat(uuid: UUID): Boolean {
        val expireTime = combatTags[uuid] ?: return false

        if (System.currentTimeMillis() > expireTime) {
            combatTags.remove(uuid)
            return false
        }
        return true
    }

    fun getRemainingTime(uuid: UUID): Long {
        val expireTime = combatTags[uuid] ?: return 0L
        val remainingMillis = expireTime - System.currentTimeMillis()
        return if (remainingMillis > 0) (remainingMillis / 1000) + 1 else 0L
    }

    fun clearTag(uuid: UUID) {
        combatTags.remove(uuid)
    }
}