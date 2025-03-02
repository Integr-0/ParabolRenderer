/*
 * Copyright © 2024 Integr
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.integr.parabol.renderer.font

import com.mojang.blaze3d.systems.RenderSystem
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap
import net.integr.parabol.renderer.mixin.INativeImage
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.util.Identifier
import org.lwjgl.system.MemoryUtil
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.font.FontRenderContext
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.awt.image.DataBuffer
import kotlin.Any
import kotlin.Boolean
import kotlin.ByteArray
import kotlin.Char
import kotlin.DoubleArray
import kotlin.FloatArray
import kotlin.IllegalArgumentException
import kotlin.Int
import kotlin.IntArray
import kotlin.Long
import kotlin.ShortArray
import kotlin.Throwable
import kotlin.code
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.sqrt


internal class GlyphMap(private val fromIncl: Char, private val toExcl: Char, font: Font, identifier: Identifier, padding: Int, private var extraItalicPadding: Float = 0f) {
    private val font: Font
    val bindToTexture: Identifier
    private val pixelPadding: Int
    private val glyphs = Char2ObjectArrayMap<Glyph>()
    var width: Int = 0
    var height: Int = 0

    private var generated: Boolean = false

    init {
        this.font = font
        this.bindToTexture = identifier
        this.pixelPadding = padding
    }

    fun getGlyph(c: Char): Glyph {
        if (!generated) {
            generate()
        }
        return glyphs[c]
    }

    fun destroy() {
        MinecraftClient.getInstance().textureManager.destroyTexture(this.bindToTexture)
        glyphs.clear()
        this.width = -1
        this.height = -1
        generated = false
    }

    fun contains(c: Char): Boolean {
        return c in fromIncl..<toExcl
    }

    private fun getFontForGlyph(c: Char): Font {
        if (font.canDisplay(c)) {
            return font
        }
        return this.font
    }

    private fun generate() {
        if (generated) {
            return
        }
        val range = toExcl.code - fromIncl.code - 1
        val charsVert = (ceil(sqrt(range.toDouble())) * 1.5).toInt()
        glyphs.clear()
        var generatedChars = 0
        var charNX = 0
        var maxX = 0
        var maxY = 0
        var currentX = 0
        var currentY = 0
        var currentRowMaxY = 0
        val glyphs1: MutableList<Glyph> = mutableListOf()
        val af = AffineTransform()
        val frc = FontRenderContext(af, true, false)
        while (generatedChars <= range) {
            val currentChar = (fromIncl.code + generatedChars).toChar()
            val font: Font = getFontForGlyph(currentChar)
            val stringBounds: Rectangle2D = font.getStringBounds(currentChar.toString(), frc)
            val width = ceil(stringBounds.width + extraItalicPadding).toInt()
            val height = ceil(stringBounds.height).toInt()
            generatedChars++
            maxX = max(maxX.toDouble(), (currentX + width).toDouble()).toInt()
            maxY = max(maxY.toDouble(), (currentY + height).toDouble()).toInt()
            if (charNX >= charsVert) {
                currentX = 0
                currentY += currentRowMaxY + pixelPadding
                charNX = 0
                currentRowMaxY = 0
            }
            currentRowMaxY = max(currentRowMaxY.toDouble(), height.toDouble()).toInt()
            glyphs1 += Glyph(currentX, currentY, width, height, currentChar, this)
            currentX += width + pixelPadding
            charNX++
        }
        val bi = BufferedImage(
            max((maxX + pixelPadding).toDouble(), 1.0).toInt(),
            max((maxY + pixelPadding).toDouble(), 1.0).toInt(),
            BufferedImage.TYPE_INT_ARGB
        )
        width = bi.width
        height = bi.height
        val g2d = bi.createGraphics()
        g2d.color = Color(255, 255, 255, 0)
        g2d.fillRect(0, 0, width, height)
        g2d.color = Color.WHITE

        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF)
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF)
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)

        for (glyph in glyphs1) {
            g2d.font = getFontForGlyph(glyph.value)
            val fontMetrics = g2d.fontMetrics
            g2d.drawString(glyph.value.toString(), glyph.u, glyph.v + fontMetrics.ascent)
            glyphs.put(glyph.value, glyph)
        }
        registerBufferedImageTexture(bindToTexture, bi)
        generated = true
    }

    companion object {
        @Suppress("CAST_NEVER_SUCCEEDS")
        fun registerBufferedImageTexture(i: Identifier?, bi: BufferedImage) {
            try {
                val ow = bi.width
                val oh = bi.height
                val image = NativeImage(NativeImage.Format.RGBA, ow, oh, false)
                val ptr: Long = (image as INativeImage).getPointer()
                val backingBuffer = MemoryUtil.memIntBuffer(ptr, image.width * image.height)
                val unD: Any
                val unRa = bi.raster
                val unCm = bi.colorModel
                val numBands = unRa.numBands
                val dataType = unRa.dataBuffer.dataType
                unD = when (dataType) {
                    DataBuffer.TYPE_BYTE -> ByteArray(numBands)
                    DataBuffer.TYPE_USHORT -> ShortArray(numBands)
                    DataBuffer.TYPE_INT -> IntArray(numBands)
                    DataBuffer.TYPE_FLOAT -> FloatArray(numBands)
                    DataBuffer.TYPE_DOUBLE -> DoubleArray(numBands)
                    else -> throw IllegalArgumentException("Unknown data buffer type: $dataType")
                }

                for (y in 0 until oh) {
                    for (x in 0 until ow) {
                        unRa.getDataElements(x, y, unD)
                        val a = unCm.getAlpha(unD)
                        val r = unCm.getRed(unD)
                        val g = unCm.getGreen(unD)
                        val b = unCm.getBlue(unD)
                        val colorData = a shl 24 or (b shl 16) or (g shl 8) or r
                        backingBuffer.put(colorData)
                    }
                }
                val tex = NativeImageBackedTexture(image)
                tex.upload()
                if (RenderSystem.isOnRenderThread()) {
                    MinecraftClient.getInstance().textureManager.registerTexture(i, tex)
                } else {
                    RenderSystem.recordRenderCall {
                        MinecraftClient.getInstance().textureManager.registerTexture(i, tex)
                    }
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
}