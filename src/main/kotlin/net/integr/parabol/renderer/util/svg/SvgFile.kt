package net.integr.parabol.renderer.util.svg

import com.github.weisj.jsvg.SVGDocument
import com.github.weisj.jsvg.SVGRenderingHints
import com.github.weisj.jsvg.attributes.ViewBox
import com.github.weisj.jsvg.attributes.paint.SimplePaintSVGPaint
import com.github.weisj.jsvg.parser.DefaultParserProvider
import com.github.weisj.jsvg.parser.DomProcessor
import com.github.weisj.jsvg.parser.SVGLoader
import com.mojang.blaze3d.systems.RenderSystem
import me.x150.renderer.render.Renderer2d
import me.x150.renderer.util.RendererUtils
import net.integr.parabol.renderer.ParabolRenderer
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.AbstractTexture
import net.minecraft.client.texture.MissingSprite
import net.minecraft.client.util.math.MatrixStack
import org.intellij.lang.annotations.Language
import java.awt.Color
import java.awt.Paint
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.Closeable
import java.nio.charset.StandardCharsets
import java.util.*
import javax.swing.JComponent
import kotlin.math.ceil
import kotlin.math.floor


class SvgFile(@Language("SVG") val svgSource: String, private val originalWidth: Int, private val originalHeight: Int) : Closeable {
    private var memoizedGuiScale: Int = -1
    private var memoizedColor: Color? = null
    private var id: AbstractTexture? = null
    private var isMcTexture: Boolean = false

    private fun redraw(width: Float, height: Float, tintColor: Color) {
        if (this.id != null) {
            this.close()
        }

        try {
            val loader = SVGLoader()
            val doc: SVGDocument = checkNotNull(loader.load(ByteArrayInputStream(svgSource.toByteArray(StandardCharsets.UTF_8)), object : DefaultParserProvider() {
                override fun createPreProcessor(): DomProcessor {
                    return DomProcessor { root ->
                        val dynamicColor = DynamicAWTSvgPaint(tintColor)
                        val uniqueIdForDynamicColor = UUID.randomUUID().toString()
                        root.children().forEach {
                            it.registerNamedElement(uniqueIdForDynamicColor, dynamicColor)
                            it.attributeNode().attributes()["fill"] = uniqueIdForDynamicColor
                        }
                    }
                }
            }))

            val bi = BufferedImage(floor(width.toDouble()).toInt(), floor(height.toDouble()).toInt(), BufferedImage.TYPE_INT_ARGB)
            val g = bi.createGraphics()
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE)
            g.setRenderingHint(SVGRenderingHints.KEY_SOFT_CLIPPING, SVGRenderingHints.VALUE_SOFT_CLIPPING_ON)
            g.setRenderingHint(SVGRenderingHints.KEY_IMAGE_ANTIALIASING, SVGRenderingHints.VALUE_IMAGE_ANTIALIASING_ON)

            doc.render(null as JComponent?, g, ViewBox(floor(width), floor(height)))
            g.dispose()
            this.id = RendererUtils.bufferedImageToNIBT(bi)
        } catch (var7: Throwable) {
            ParabolRenderer.LOGGER.error("Failed to render SVG", var7)
            this.isMcTexture = true
            this.id = MinecraftClient.getInstance().textureManager.getTexture(MissingSprite.getMissingSpriteId())
        }
    }

    fun render(stack: MatrixStack, x: Double, y: Double, renderWidth: Float, renderHeight: Float, tintColor: Color) {
        val guiScale = RendererUtils.getGuiScale()
        if (this.memoizedGuiScale != guiScale || this.id == null || this.memoizedColor != tintColor) {
            this.memoizedGuiScale = guiScale
            this.memoizedColor = tintColor
            this.redraw(
                (this.originalWidth * this.memoizedGuiScale).toFloat(),
                (this.originalHeight * this.memoizedGuiScale).toFloat(),
                tintColor
            )
        }

        if (!RendererUtils.isSkipSetup()) {
            RenderSystem.enableBlend()
            RenderSystem.defaultBlendFunc()
        }

        RenderSystem.setShaderTexture(0, id!!.glId)
        Renderer2d.renderTexture(stack, x, y, ceil(renderWidth.toDouble()), ceil(renderHeight.toDouble()))
    }

    override fun close() {
        checkNotNull(this.id != null) { "Already closed" }
        if (!this.isMcTexture) {
            id!!.close()
        }

        this.id = null
    }

    class DynamicAWTSvgPaint internal constructor(private var color: Color) : SimplePaintSVGPaint {
        override fun paint(): Paint {
            return color
        }
    }
}