package net.integr.parabol_renderer

import me.x150.renderer.event.RenderEvents
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.integr.parabol_renderer.engine.Parabol2dCtx
import net.integr.parabol_renderer.font.ParabolFontManager
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import org.slf4j.LoggerFactory
import java.awt.Color


class ParabolRenderer : ClientModInitializer {

    companion object {
        const val MOD_ID = "parabol-renderer"
        val LOGGER = LoggerFactory.getLogger("ParabolRenderer")
        val VERSION = FabricLoader.getInstance().getModContainer(MOD_ID).get().metadata.version.toString()
        val MC = MinecraftClient.getInstance()
    }

    override fun onInitializeClient() {
        ParabolFontManager.setDefaultFont("MyFont")
        RenderEvents.HUD.register { drawCtx ->
            val ctx = Parabol2dCtx.create(drawCtx.matrices)

            ctx.apply {
                useMultisample {
                    circle(10.0, 10.0, 10.0, Color(24, 124, 75))
                }

                val theText = Text.literal("The quick brown fox jumps over the lazy dog\n")
                    .append(Text.literal("italic\n").styled { it.withItalic(true) }.withColor(Color.GREEN.rgb))
                    .append(Text.literal("bold\n").styled { it.withBold(true) }
                    .append(Text.literal("bold italic\n").styled { it.withBold(true).withItalic(true) })
                    .append(Text.literal("under\n").styled { it.withUnderline(true) })
                    .append(Text.literal("strikethrough\nwith nl\n").styled { it.withStrikethrough(true) })
                    .append(Text.literal("Special chars: 1234@æđðħſ.ĸ|aa{a}()")))

                useMultisample {
                    circle(80.0, 80.0, 20.0, Color(245, 124, 75))
                }

                useBlurMask {
                    val width = 50
                    val height = 50
                    quad(
                        drawCtx.scaledWindowWidth/2.0 - width/2.0,
                        drawCtx.scaledWindowHeight/2.0 - height/2.0,
                        drawCtx.scaledWindowWidth/2.0 + width/2.0,
                        drawCtx.scaledWindowHeight/2.0 + height/2.0
                    )
                }

                text(theText, 10f, 10f, 1f, 20f)
                text(theText, 10f, 40f, 1f, "Arial",20f)
            }

        }

        RenderEvents.WORLD.register {

        }

        LOGGER.info("Parabol Rendering System V${VERSION} loaded.")

    }
}
