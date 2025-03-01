package net.integr.parabol_renderer.font

import me.x150.renderer.font.FontRenderer
import net.integr.parabol_renderer.ParabolRenderer
import java.awt.Font
import java.awt.FontFormatException
import java.io.IOException

object ParabolFontManager {
    private val fontCache = mutableMapOf<String, MutableMap<Int, FontRenderer>>()
    private val customFontCache = mutableMapOf<String, Font>()

    init {
        registerCustomFont("assets/parabol-renderer/font/InterDisplay.ttf", "InterDisplay")
    }

    private var defaultFont: String = "InterDisplay"

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

    fun getDefaultFontRenderer(pxlSize: Float): FontRenderer {
        return getOrLoadFontRenderer(defaultFont, pxlSize)
    }

    fun getOrLoadFontRenderer(font: String, pxlSize: Float): FontRenderer {
        val fnt: Font = if (fontCache.containsKey(font) && fontCache[font]!!.containsKey(pxlSize.toInt())) {
            return fontCache[font]!![pxlSize.toInt()]!!
        } else if (customFontCache.containsKey(font)) {
            customFontCache[font]!!
        } else Font.decode(font)

        val fntR = FontRenderer(fnt, pxlSize)
        fntR.roundCoordinates(false)

        if (!fontCache.containsKey(font)) {
            fontCache[font] = mutableMapOf()
        }

        fontCache[font]!![pxlSize.toInt()] = fntR

        return fntR
    }
}