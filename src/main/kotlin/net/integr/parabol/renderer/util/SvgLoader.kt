package net.integr.parabol.renderer.util

import me.x150.renderer.render.SVGFile
import net.integr.parabol.renderer.ParabolRenderer

object SvgLoader {
    fun loadIcon(icon: String, w: Int, h: Int): SVGFile {
        val str = ParabolRenderer::class.java.classLoader.getResourceAsStream("parabol-assets/icons/$icon.svg")?.bufferedReader()?.readText()
            ?: throw IllegalArgumentException("Icon not found: $icon")

        val file = SVGFile(str, w, h)

        return file
    }
}