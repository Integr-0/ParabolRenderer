package net.integr.parabol.renderer

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.MathHelper
import org.slf4j.LoggerFactory
import java.awt.Color


class ParabolRenderer : ClientModInitializer {
    companion object {
        const val MOD_ID = "parabol-renderer"
        val LOGGER = LoggerFactory.getLogger("ParabolRenderer")!!
        val VERSION = FabricLoader.getInstance().getModContainer(MOD_ID).get().metadata.version.toString()
        val MC = MinecraftClient.getInstance()
    }

    private fun getRainbowColor(): Color {
        val x = System.currentTimeMillis() % 2000 / 1000f
        val pi = Math.PI.toFloat()

        val r = 0.5f + 0.5f * MathHelper.sin(x * pi)
        val g = 0.5f + 0.5f * MathHelper.sin((x + 4f / 3f) * pi)
        val b = 0.5f + 0.5f * MathHelper.sin((x + 8f / 3f) * pi)
        return Color((r * 255).toInt(), (g * 255).toInt(), (b * 255).toInt())
    }

    override fun onInitializeClient() {
        LOGGER.info("Parabol Rendering System V$VERSION loaded.")
    }
}
