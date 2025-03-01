package net.integr.parabol.renderer

import net.fabricmc.api.ClientModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import org.slf4j.LoggerFactory

class ParabolRenderer : ClientModInitializer {
    companion object {
        const val MOD_ID = "parabol-renderer"
        val LOGGER = LoggerFactory.getLogger("ParabolRenderer")!!
        val VERSION = FabricLoader.getInstance().getModContainer(MOD_ID).get().metadata.version.toString()
        val MC = MinecraftClient.getInstance()!!
    }

    override fun onInitializeClient() {
        LOGGER.info("Parabol Rendering System V$VERSION loaded.")
    }
}
