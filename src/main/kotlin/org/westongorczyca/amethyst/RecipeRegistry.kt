package org.westongorczyca.amethyst

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.RecipeChoice
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.SmithingTransformRecipe

class RecipeRegistry(private val plugin: JavaPlugin) {
    fun registerAll() {
        registerTemplateCrafting()
        registerPickaxeSmithing()
        registerTemplateDuplication()
    }

    private fun registerTemplateCrafting() {
        val key = NamespacedKey(plugin, "echo_template_crafting")
        val recipe = ShapedRecipe(key, CustomItems.createSmithingTemplate())

        recipe.shape(
            "ASA",
            "AEA",
            "AAA"
        )

        recipe.setIngredient('S', RecipeChoice.ExactChoice(CustomItems.createTemplateShard()))
        recipe.setIngredient('E', Material.ECHO_SHARD)
        recipe.setIngredient('A', Material.AMETHYST_SHARD)

        Bukkit.addRecipe(recipe)
    }

    private fun registerTemplateDuplication() {
        val key = NamespacedKey(plugin, "echo_template_duplication")

        val recipe = ShapedRecipe(key, CustomItems.createSmithingTemplate().apply { amount = 2 })

        recipe.shape(
            "DTD",
            "DED",
            "DDD"
        )

        recipe.setIngredient('D', org.bukkit.Material.DIAMOND)
        recipe.setIngredient('T', org.bukkit.inventory.RecipeChoice.ExactChoice(CustomItems.createSmithingTemplate()))
        recipe.setIngredient('E', org.bukkit.Material.ECHO_SHARD)

        org.bukkit.Bukkit.addRecipe(recipe)
    }

    fun registerPickaxeSmithing() {
        val key = NamespacedKey(plugin, "echo_pickaxe_smithing")

        val recipe = SmithingTransformRecipe(
            key,
            CustomItems.createEchoPickaxe(),
            RecipeChoice.ExactChoice(CustomItems.createSmithingTemplate()), // Custom Template required
            RecipeChoice.MaterialChoice(Material.DIAMOND_PICKAXE),          // Base Item
            RecipeChoice.MaterialChoice(Material.IRON_INGOT)                // Modifier material cost
        )

        Bukkit.addRecipe(recipe)
    }
}