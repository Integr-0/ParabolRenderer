package net.integr.parabol_renderer.engine

import me.x150.renderer.render.MSAAFramebuffer
import me.x150.renderer.render.MaskedBlurFramebuffer
import me.x150.renderer.render.Renderer2d
import net.integr.parabol_renderer.font.ParabolFontManager
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import org.jetbrains.annotations.Range
import java.awt.Color

class Parabol2dCtx private constructor(private val mat: MatrixStack, private val samples: Int) {
    companion object {
        fun create(mat: MatrixStack, samples: Int = 8): Parabol2dCtx {
            return Parabol2dCtx(mat, samples)
        }
    }

    private var bufferSwapAllowed = true


    fun useMultisample(block: Parabol2dCtx.() -> Unit) {
        if (!bufferSwapAllowed) {
            throw IllegalStateException("Buffer swap to useMultisample(...) is not allowed here.")
        }
        bufferSwapAllowed = false
        MSAAFramebuffer.use(samples) {
            this.block()
        }
        bufferSwapAllowed = true
    }

    fun useBlurMask(kernelSize: Int = 8, sigma: Float = 4f, block: Parabol2dCtx.() -> Unit) {
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
        stack: MatrixStack,
        x: Double,
        y: Double,
        x1: Double,
        y1: Double,
        rad: Float,
        c: Color = Color.WHITE
    ) {
        Renderer2d.renderRoundedQuad(stack, c, x, y, x1, y1, rad, rad, rad, rad, samples.toFloat())
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

    fun text(text: Text, x: Float, y: Float, a: Float, font: String, size: Float) {
        ParabolFontManager.getOrLoadFontRenderer(font, size).drawText(mat, text, x, y, a)
    }

    fun text(text: Text, x: Float, y: Float, a: Float, size: Float) {
        ParabolFontManager.getDefaultFontRenderer(size).drawText(mat, text, x, y, a)
    }

    fun textHeight(text: Text, font: String, size: Float): Float {
        return ParabolFontManager.getOrLoadFontRenderer(font, size).getTextHeight(text)
    }

    fun textHeight(text: Text, size: Float): Float {
        return ParabolFontManager.getDefaultFontRenderer(size).getTextHeight(text)
    }

    fun textWidth(text: Text, font: String, size: Float): Float {
        return ParabolFontManager.getOrLoadFontRenderer(font, size).getTextWidth(text)
    }

    fun textWidth(text: Text, size: Float): Float {
        return ParabolFontManager.getDefaultFontRenderer(size).getTextWidth(text)
    }
}