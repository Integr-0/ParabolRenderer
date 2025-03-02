package net.integr.parabol.renderer

import net.integr.parabol.renderer.util.svg.SVGFile
import me.x150.renderer.render.MSAAFramebuffer
import me.x150.renderer.render.MaskedBlurFramebuffer
import me.x150.renderer.render.Renderer2d
import net.integr.parabol.renderer.font.FontManager
import net.integr.parabol.renderer.font.builder.ParabolText
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import org.jetbrains.annotations.Range
import java.awt.Color

class Render2dContext private constructor(private val mat: MatrixStack, private val samples: Int) {
    companion object {
        fun create(mat: MatrixStack, samples: Int = 8): Render2dContext {
            return Render2dContext(mat, samples)
        }

        fun textHeight(text: ParabolText, font: String, size: Float): Float {
            return FontManager.getOrLoadFontRenderer(font, size).getTextHeight(text)
        }

        fun textHeight(text: ParabolText, size: Float): Float {
            return FontManager.getDefaultFontRenderer(size).getTextHeight(text)
        }

        fun textWidth(text: ParabolText, font: String, size: Float): Float {
            return FontManager.getOrLoadFontRenderer(font, size).getTextWidth(text)
        }

        fun textWidth(text: ParabolText, size: Float): Float {
            return FontManager.getDefaultFontRenderer(size).getTextWidth(text)
        }
    }

    private var bufferSwapAllowed = true


    fun useMultisample(block: Render2dContext.() -> Unit) {
        if (!bufferSwapAllowed) {
            throw IllegalStateException("Buffer swap to useMultisample(...) is not allowed here.")
        }
        bufferSwapAllowed = false
        MSAAFramebuffer.use(samples) {
            this.block()
        }
        bufferSwapAllowed = true
    }

    fun useBlurMask(kernelSize: Int = 11, sigma: Float = 7f, block: Render2dContext.() -> Unit) {
        if (!bufferSwapAllowed) {
            throw IllegalStateException("Buffer swap to useBlurMask(...) is not allowed here.")
        }

        bufferSwapAllowed = false
        MaskedBlurFramebuffer.use {
            this.block()
        }

        MaskedBlurFramebuffer.draw(kernelSize, sigma)
        bufferSwapAllowed = true
    }

    fun texture(
        x0: Double,
        y0: Double,
        width: Double,
        height: Double,
        u: Float,
        v: Float,
        regionWidth: Double,
        regionHeight: Double,
        textureWidth: Double,
        textureHeight: Double
    ) {
        Renderer2d.renderTexture(
            mat,
            x0,
            y0,
            width,
            height,
            u,
            v,
            regionWidth,
            regionHeight,
            textureWidth,
            textureHeight
        )
    }

    fun texture(x: Double, y: Double, width: Double, height: Double) {
        Renderer2d.renderTexture(mat, x, y, width, height, 0.0f, 0.0f, width, height, width, height)
    }

    fun texture(texture: Identifier, x: Double, y: Double, width: Double, height: Double) {
        Renderer2d.renderTexture(mat, texture, x, y, width, height)
    }

    fun ellipse(
        originX: Double,
        originY: Double,
        radX: Double,
        radY: Double,
        ellipseColor: Color = Color.WHITE,
        segments: @Range(from = 4L, to = 360L) Int = 360
    ) {
        Renderer2d.renderEllipse(mat, ellipseColor, originX, originY, radX, radY, segments)
    }

    fun circle(
        originX: Double,
        originY: Double,
        rad: Double,
        circleColor: Color = Color.WHITE,
        segments: @Range(from = 4L, to = 360L) Int = 360
        ) {
        Renderer2d.renderEllipse(mat, circleColor, originX, originY, rad, rad, segments)
    }

    fun ellipseOutline(
        originX: Double,
        originY: Double,
        radX: Double,
        radY: Double,
        width: @Range(from = 0L, to = Long.MAX_VALUE) Double,
        height: @Range(from = 0L, to = Long.MAX_VALUE) Double,
        ellipseColor: Color = Color.WHITE,
        segments: @Range(from = 4L, to = 360L) Int = 360
    ) {
        Renderer2d.renderEllipseOutline(mat, ellipseColor, originX, originY, radX, radY, width, height, segments)
    }

    fun quad(x1: Double, y1: Double, x2: Double, y2: Double, color: Color = Color.WHITE) {
        Renderer2d.renderQuad(mat, color, x1, y1, x2, y2)
    }

    fun roundedQuad(
        fromX: Double,
        fromY: Double,
        toX: Double,
        toY: Double,
        radTL: Float,
        radTR: Float,
        radBL: Float,
        radBR: Float,
        c: Color = Color.WHITE
    ) {
        Renderer2d.renderRoundedQuad(mat, c, fromX, fromY, toX, toY, radTL, radTR, radBL, radBR, samples.toFloat())
    }

    fun roundedQuad(
        x: Double,
        y: Double,
        x1: Double,
        y1: Double,
        rad: Float,
        c: Color = Color.WHITE
    ) {
        Renderer2d.renderRoundedQuad(mat, c, x, y, x1, y1, rad, rad, rad, rad, samples.toFloat())
    }

    fun roundedOutline(
        fromX: Double,
        fromY: Double,
        toX: Double,
        toY: Double,
        radTL: Float,
        radTR: Float,
        radBL: Float,
        radBR: Float,
        outlineWidth: Float,
        c: Color = Color.WHITE
    ) {
        Renderer2d.renderRoundedOutline(
            mat,
            c,
            fromX,
            fromY,
            toX,
            toY,
            radTL,
            radTR,
            radBL,
            radBR,
            outlineWidth,
            samples.toFloat()
        )
    }

    fun roundedOutline(
        fromX: Double,
        fromY: Double,
        toX: Double,
        toY: Double,
        rad: Float,
        width: Float,
        c: Color = Color.WHITE
    ) {
        Renderer2d.renderRoundedOutline(mat, c, fromX, fromY, toX, toY, rad, rad, rad, rad, width, samples.toFloat())
    }

    fun line(x: Double, y: Double, x1: Double, y1: Double, color: Color = Color.WHITE) {
        Renderer2d.renderLine(mat, color, x, y, x1, y1)
    }

    fun text(text: ParabolText, x: Double, y: Double, font: String, size: Float) {
        FontManager.getOrLoadFontRenderer(font, size).drawText(mat, text, x, y)
    }

    fun text(text: ParabolText, x: Double, y: Double, size: Float) {
        FontManager.getDefaultFontRenderer(size).drawText(mat, text, x, y)
    }

    fun screenWidth(): Int {
        return ParabolRenderer.MC.window.scaledWidth
    }

    fun screenHeight(): Int {
        return ParabolRenderer.MC.window.scaledHeight
    }

    fun screenCenterX(): Int {
        return ParabolRenderer.MC.window.scaledWidth / 2
    }

    fun screenCenterY(): Int {
        return ParabolRenderer.MC.window.scaledHeight / 2
    }

    fun svg(svg: SVGFile, x: Double, y: Double, width: Double, height: Double, tint: Color) {
        svg.render(mat, x, y, width.toFloat(), height.toFloat(), tint)
    }
}