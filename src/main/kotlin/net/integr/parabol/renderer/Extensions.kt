package net.integr.parabol.renderer

import me.x150.renderer.render.Renderer3d
import java.awt.Color

fun Color.modify(redOverwrite: Int, greenOverwrite: Int, blueOverwrite: Int, alphaOverwrite: Int): Color {
    return Renderer3d.modifyColor(this, redOverwrite, greenOverwrite, blueOverwrite, alphaOverwrite)
}