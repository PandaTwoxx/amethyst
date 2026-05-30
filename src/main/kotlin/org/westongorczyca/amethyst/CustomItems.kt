package org.westongorczyca.amethyst

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

object CustomItems {
    lateinit var templateShardKey: NamespacedKey
    lateinit var smithingTemplateKey: NamespacedKey
    lateinit var echoPickaxeKey: NamespacedKey

    fun init(plugin: JavaPlugin) {
        templateShardKey = NamespacedKey(plugin, "echo_template_shard")
        smithingTemplateKey = NamespacedKey(plugin, "echo_smithing_template")
        echoPickaxeKey = NamespacedKey(plugin, "echo_pickaxe")
    }

    fun createTemplateShard(): ItemStack {
        return ItemStack(Material.AMETHYST_SHARD).apply {
            itemMeta = itemMeta?.apply {
                displayName(Component.text("Echo Template Shard", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false))
                lore(listOf(Component.text("Combine with echo shards and amethyst to craft a template.", NamedTextColor.GRAY)))
                persistentDataContainer.set(templateShardKey, PersistentDataType.BOOLEAN, true)
            }
        }
    }

    fun createSmithingTemplate(): ItemStack {
        return ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE).apply {
            itemMeta = itemMeta?.apply {
                displayName(Component.text("Echo Smithing Template", NamedTextColor.DARK_AQUA).decoration(TextDecoration.ITALIC, false))
                lore(listOf(Component.text("Echo Upgrade", NamedTextColor.BLUE)))
                persistentDataContainer.set(smithingTemplateKey, PersistentDataType.BOOLEAN, true)
            }
        }
    }

    fun createEchoPickaxe(): ItemStack {
        return ItemStack(Material.DIAMOND_PICKAXE).apply {
            itemMeta = itemMeta?.apply {
                displayName(Component.text("Echo Pickaxe", NamedTextColor.DARK_AQUA).decoration(TextDecoration.ITALIC, false))
                lore(listOf(Component.text("Allows you to mine a 3x3 area.", NamedTextColor.DARK_PURPLE)))
                persistentDataContainer.set(echoPickaxeKey, PersistentDataType.BOOLEAN, true)
            }
        }
    }
}