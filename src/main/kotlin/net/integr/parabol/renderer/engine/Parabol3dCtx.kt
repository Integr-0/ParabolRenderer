package net.integr.parabol.renderer.engine

import me.x150.renderer.render.MSAAFramebuffer
import me.x150.renderer.render.Renderer3d
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.Vec3d
import java.awt.Color

class Parabol3dCtx private constructor(private val mat: MatrixStack, private val samples: Int) {
    companion object {
        fun create(mat: MatrixStack, samples: Int = 8): Parabol3dCtx {
            return Parabol3dCtx(mat, samples)
        }

        fun fadingBlock(outlineColor: Color, fillColor: Color, start: Vec3d, dimensions: Vec3d, lifeTimeMs: Long) {
            Renderer3d.renderFadingBlock(outlineColor, fillColor, start, dimensions, lifeTimeMs)
        }
    }

    private var throughWallsAllowed = true
    private var bufferSwapAllowed = true

    fun useThroughWalls(block: Parabol3dCtx.() -> Unit) {
        if (!throughWallsAllowed) {
            throw IllegalStateException("Swap to useThroughWalls(...) is not allowed here.")
        }
        throughWallsAllowed = false
        Renderer3d.renderThroughWalls()
        this.block()
        Renderer3d.stopRenderThroughWalls()
        throughWallsAllowed = true
    }

    fun useMultisample(block: Parabol3dCtx.() -> Unit) {
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

    fun Color.modify(redOverwrite: Int, greenOverwrite: Int, blueOverwrite: Int, alphaOverwrite: Int): Color {
        return Renderer3d.modifyColor(this, redOverwrite, greenOverwrite, blueOverwrite, alphaOverwrite)
    }
}