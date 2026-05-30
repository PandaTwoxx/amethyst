package org.westongorczyca.amethyst

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID

class HomeManager(private val plugin: JavaPlugin) {

    private val homeStorage = mutableMapOf<UUID, MutableMap<String, Location>>()
    private val config: FileConfiguration get() = plugin.config

    init {
        loadHomesFromConfig()
    }

    fun setHome(uuid: UUID, name: String, location: Location) {
        homeStorage.computeIfAbsent(uuid) { mutableMapOf() }[name.lowercase()] = location

        val path = "homes.$uuid.${name.lowercase()}"
        config.set("$path.world", location.world.name)
        config.set("$path.x", location.x)
        config.set("$path.y", location.y)
        config.set("$path.z", location.z)
        config.set("$path.yaw", location.yaw.toDouble())
        config.set("$path.pitch", location.pitch.toDouble())

        plugin.saveConfig()
    }


    fun getHome(uuid: UUID, name: String): Location? {
        return homeStorage[uuid]?.get(name.lowercase())
    }

    fun getHomeNames(uuid: UUID): List<String> {
        return homeStorage[uuid]?.keys?.toList() ?: emptyList()
    }

    private fun loadHomesFromConfig() {
        val homesSection = config.getConfigurationSection("homes") ?: return

        for (uuidString in homesSection.getKeys(false)) {
            val uuid = try {
                UUID.fromString(uuidString)
            } catch (_: IllegalArgumentException) {
                throw IllegalArgumentException("Invalid UUID: $uuidString")
            }

            val playerSection = homesSection.getConfigurationSection(uuidString) ?: continue
            val playerHomes = mutableMapOf<String, Location>()

            for (homeName in playerSection.getKeys(false)) {
                val path = "$uuidString.$homeName"

                val worldName = playerSection.getString("$path.world") ?: continue
                val world = Bukkit.getWorld(worldName) ?: continue // Skip if world is missing

                val x = playerSection.getDouble("$path.x")
                val y = playerSection.getDouble("$path.y")
                val z = playerSection.getDouble("$path.z")
                val yaw = playerSection.getDouble("$path.yaw").toFloat()
                val pitch = playerSection.getDouble("$path.pitch").toFloat()

                playerHomes[homeName.lowercase()] = Location(world, x, y, z, yaw, pitch)
            }

            if (playerHomes.isNotEmpty()) {
                homeStorage[uuid] = playerHomes
            }
        }
    }
}