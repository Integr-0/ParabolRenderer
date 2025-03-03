package net.integr.parabol.renderer.util.svg

import net.minecraft.client.util.math.MatrixStack
import org.intellij.lang.annotations.Language
import java.awt.Color

class SvgIcon(@Language("SVG") val svgSource: String) {
    private val files = mutableMapOf<Pair<Int, Int>, SvgFile>()

    private fun request(width: Int, height: Int): SvgFile {
        return files.getOrPut(width to height) {
            SvgFile(svgSource, width, height)
        }
    }

    fun render(stack: MatrixStack, x: Double, y: Double, renderWidth: Float, renderHeight: Float, tintColor: Color) {
        val file = request(renderWidth.toInt(), renderHeight.toInt())
        file.render(stack, x, y, renderWidth, renderHeight, tintColor)
    }
}