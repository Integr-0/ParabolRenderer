package net.integr.parabol.renderer

import me.x150.renderer.render.MSAAFramebuffer
import me.x150.renderer.render.Renderer3d
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3d
import java.awt.Color

class Render3dContext private constructor(private val mat: MatrixStack, private val samples: Int) {
    companion object {
        fun create(mat: MatrixStack, samples: Int = 8): Render3dContext {
            return Render3dContext(mat, samples)
        }

        fun fadingBlock(outlineColor: Color, fillColor: Color, start: Vec3d, dimensions: Vec3d, lifeTimeMs: Long) {
            Renderer3d.renderFadingBlock(outlineColor, fillColor, start, dimensions, lifeTimeMs)
        }
    }

    private var throughWallsAllowed = true
    private var bufferSwapAllowed = true

    fun useThroughWalls(block: Render3dContext.() -> Unit) {
        if (!throughWallsAllowed) {
            throw IllegalStateException("Swap to useThroughWalls(...) is not allowed here.")
        }
        throughWallsAllowed = false
        Renderer3d.renderThroughWalls()
        this.block()
        Renderer3d.stopRenderThroughWalls()
        throughWallsAllowed = true
    }

    fun useMultisample(block: Render3dContext.() -> Unit) {
        if (!bufferSwapAllowed) {
            throw IllegalStateException("Buffer swap to useMultisample(...) is not allowed here.")
        }
        bufferSwapAllowed = false
        MSAAFramebuffer.use(samples) {
            this.block()
        }
        bufferSwapAllowed = true
    }

    fun outline(color: Color, start: Vec3d, dimensions: Vec3d) {
        Renderer3d.renderOutline(mat, color, start, dimensions)
    }

    fun edged(colorFill: Color, colorOutline: Color, start: Vec3d, dimensions: Vec3d) {
        Renderer3d.renderEdged(mat, colorFill, colorOutline, start, dimensions)
    }

    fun filled(color: Color, start: Vec3d, dimensions: Vec3d) {
        Renderer3d.renderFilled(mat, color, start, dimensions)
    }

    fun line(color: Color, start: Vec3d, end: Vec3d) {
        Renderer3d.renderLine(mat, color, start, end)
    }
}