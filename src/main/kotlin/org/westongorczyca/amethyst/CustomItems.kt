package org.westongorczyca.amethyst

import org.bukkit.NamespacedKey
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
}