package net.integr.parabol.renderer.engine.font

import net.integr.parabol.renderer.ParabolRenderer
import java.awt.Font
import java.awt.FontFormatException
import java.io.IOException

object ParabolFontManager {
    private val fontCache = mutableMapOf<String, MutableMap<Int, ParabolFontRenderer>>()
    private val customFontCache = mutableMapOf<String, Font>()

    init {
        registerCustomFont("parabol-assets/font/Roboto-Regular.ttf", "RobotoRegular")
    }

    private var defaultFont: String = "RobotoRegular"

    fun registerCustomFont(path: String, name: String) {
        try {
            val fontStream = ParabolRenderer::class.java.classLoader.getResourceAsStream(path)
                ?: throw IOException("Font file not found: $path")
            val fnt = Font.createFont(Font.TRUETYPE_FONT, fontStream)
            ParabolRenderer.LOGGER.info("Loaded font: $name")
            customFontCache[name] = fnt
        } catch (e: IOException) {
            ParabolRenderer.LOGGER.error("Failed to load font: ${e.message}")
        } catch (e: FontFormatException) {
            ParabolRenderer.LOGGER.error("Invalid font format: $path")
        }
    }

    fun setDefaultFont(font: String) {
        defaultFont = font
    }

    fun getDefaultFontRenderer(pxlSize: Float): ParabolFontRenderer {
        return getOrLoadFontRenderer(defaultFont, pxlSize)
    }

    fun getOrLoadFontRenderer(font: String, pxlSize: Float): ParabolFontRenderer {
        val fnt: Font = if (fontCache.containsKey(font) && fontCache[font]!!.containsKey(pxlSize.toInt())) {
            return fontCache[font]!![pxlSize.toInt()]!!
        } else if (customFontCache.containsKey(font)) {
            customFontCache[font]!!
        } else Font.decode(font)

        val fntR = ParabolFontRenderer.make(fnt, pxlSize)

        if (!fontCache.containsKey(font)) {
            fontCache[font] = mutableMapOf()
        }

        fontCache[font]!![pxlSize.toInt()] = fntR

        return fntR
    }
}