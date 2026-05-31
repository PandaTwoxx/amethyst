package org.westongorczyca.amethyst.util

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

    // NEW: Modern 1.21.4+ String-based Item Model Keys
    lateinit var shardModelKey: NamespacedKey
    lateinit var templateModelKey: NamespacedKey
    lateinit var pickaxeModelKey: NamespacedKey

    fun init(plugin: JavaPlugin) {
        templateShardKey = NamespacedKey(plugin, "echo_template_shard")
        smithingTemplateKey = NamespacedKey(plugin, "echo_smithing_template")
        echoPickaxeKey = NamespacedKey(plugin, "echo_pickaxe")

        // Define the model string keys (this generates "amethyst:echo_template_shard", etc.)
        shardModelKey = NamespacedKey(plugin, "echo_template_shard")
        templateModelKey = NamespacedKey(plugin, "echo_smithing_template")
        pickaxeModelKey = NamespacedKey(plugin, "echo_pickaxe")
    }

    fun createTemplateShard(): ItemStack {
        return ItemStack(Material.AMETHYST_SHARD).apply {
            itemMeta = itemMeta?.apply {
                displayName(Component.text("Echo Upgrade Shard", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))
                persistentDataContainer.set(templateShardKey, PersistentDataType.BOOLEAN, true)
                
                // NEW: Direct String Item Model Assignment
                setItemModel(shardModelKey)
            }
        }
    }

    fun createSmithingTemplate(): ItemStack {
        return ItemStack(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE).apply {
            itemMeta = itemMeta?.apply {
                displayName(Component.text("Echo Upgrade", NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false))
                persistentDataContainer.set(smithingTemplateKey, PersistentDataType.BOOLEAN, true)
                
                // NEW: Direct String Item Model Assignment
                setItemModel(templateModelKey)
            }
        }
    }

    fun createEchoPickaxe(): ItemStack {
        return ItemStack(Material.DIAMOND_PICKAXE).apply {
            itemMeta = itemMeta?.apply {
                displayName(Component.text("Echo Pickaxe", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false))

                val loreLines = listOf(
                    // Line 1: Blank space
                    Component.empty(), 
                    
                    Component.text("Ability:")
                        .color(NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                        
                    Component.text(" Imbued with the power of echolocation,")
                        .color(NamedTextColor.BLUE)
                        .decoration(TextDecoration.ITALIC, false),
                        
                    Component.text(" allowing the user to vibrate all nearby")
                        .color(NamedTextColor.BLUE)
                        .decoration(TextDecoration.ITALIC, false),
                        
                    Component.text(" blocks when breaking one, mining a 3x3")
                        .color(NamedTextColor.BLUE)
                        .decoration(TextDecoration.ITALIC, false),
                        
                    Component.text(" tunnel instead of a 1x1.")
                        .color(NamedTextColor.BLUE)
                        .decoration(TextDecoration.ITALIC, false)
                )

                lore(loreLines)
                persistentDataContainer.set(echoPickaxeKey, PersistentDataType.BOOLEAN, true)
                
                // NEW: Direct String Item Model Assignment
                setItemModel(pickaxeModelKey)
            }
        }
    }
}
