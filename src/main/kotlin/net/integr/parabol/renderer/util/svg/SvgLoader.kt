package net.integr.parabol.renderer.util.svg

import net.integr.parabol.renderer.ParabolRenderer

object SvgLoader {
    fun loadParabolIcon(icon: String): SvgIcon {
        val str = ParabolRenderer::class.java.classLoader.getResourceAsStream("parabol-assets/icons/$icon.svg")?.bufferedReader()?.readText()
            ?: throw IllegalArgumentException("Icon not found: $icon")

        val file = SvgIcon(str)

        return file
    }

    fun loadIcon(icon: String): SvgIcon {
        val str = ParabolRenderer::class.java.classLoader.getResourceAsStream("icon")?.bufferedReader()?.readText()
            ?: throw IllegalArgumentException("Icon not found: $icon")

        val file = SvgIcon(str)

        return file
    }
}