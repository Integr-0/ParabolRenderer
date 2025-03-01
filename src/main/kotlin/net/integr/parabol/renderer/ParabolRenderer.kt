package net.integr.parabol.renderer

import me.x150.renderer.event.RenderEvents
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.loader.api.FabricLoader
import net.integr.parabol.renderer.engine.Parabol2dCtx
import net.integr.parabol.renderer.engine.Parabol3dCtx
import net.integr.parabol.renderer.engine.font.builder.ParabolText
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
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
        RenderEvents.HUD.register { drawCtx ->
            val ctx = Parabol2dCtx.create(drawCtx.matrices)

            ctx.apply {
                useMultisample {
                    circle(10.0, 10.0, 10.0, Color(24, 124, 75))
                }

                val text = ParabolText.literal("The quick brown fox jumps over the ")
                    .append("lazy ").styled(ParabolText.Style.BOLD).colored(getRainbowColor())
                    .append("dog!")


                useMultisample {
                    circle(80.0, 80.0, 20.0, Color(245, 124, 75))
                }

                val width = 50
                val height = 50

                useBlurMask {
                    roundedQuad(
                        screenCenterX() - width/2.0,
                        screenCenterY() - height/2.0,
                        screenCenterX() + width/2.0,
                        screenCenterY() + height/2.0,
                        10f
                    )
                }

                useMultisample {
                    roundedQuad(
                        screenCenterX() - width/2.0,
                        screenCenterY() - height/2.0,
                        screenCenterX() + width/2.0,
                        screenCenterY() + height/2.0,
                        10f,
                        Color(100, 100, 100, 100)
                    )

                    roundedOutline(
                        screenCenterX() - width/2.0,
                        screenCenterY() - height/2.0,
                        screenCenterX() + width/2.0,
                        screenCenterY() + height/2.0,
                        10f,
                        1f,
                        Color(245, 124, 75)
                    )
                }

                text(text, 10.0, 10.0, 15f)
                text(text, 10.0, 30.0, 10f)
                text(text, 10.0, 40.0, 5f)
            }

        }

        RenderEvents.WORLD.register { matrices ->
            val ctx = Parabol3dCtx.create(matrices)
            ctx.useThroughWalls {
                useMultisample {
                    edged(Color(245, 124, 75, 100), Color(245, 124, 75), Vec3d(0.0, 0.0, 0.0), Vec3d(1.0, 1.0, 1.0))
                }
            }
        }

        LOGGER.info("Parabol Rendering System V$VERSION loaded.")

    }
}
