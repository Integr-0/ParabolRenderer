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

package net.integr.parabol.renderer.engine.font

import com.mojang.blaze3d.systems.RenderSystem
import it.unimi.dsi.fastutil.chars.Char2IntArrayMap
import it.unimi.dsi.fastutil.chars.Char2ObjectArrayMap
import it.unimi.dsi.fastutil.chars.Char2ObjectFunction
import it.unimi.dsi.fastutil.objects.*
import net.integr.parabol.renderer.ParabolRenderer
import net.integr.parabol.renderer.engine.font.builder.ParabolText
import net.minecraft.client.gl.ShaderProgramKeys
import net.minecraft.client.render.*
import net.minecraft.client.render.VertexFormat.DrawMode
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.Identifier
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.awt.Font
import java.io.Closeable
import java.util.*
import java.util.stream.Collectors
import java.util.stream.IntStream
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.pow
import kotlin.random.Random


class ParabolFontRenderer(private val initFont: Font, private val originalSize: Float, private val charsPerPage: Int, private val padding: Int) : Closeable {
    companion object {
        fun make(font: Font, size: Float): ParabolFontRenderer {
            return ParabolFontRenderer(font, size)
        }
    }

    private val glyphPageCache: Object2ObjectMap<Identifier, ObjectList<DrawEntry>> = Object2ObjectOpenHashMap()
    private val allGlyphs = Char2ObjectArrayMap<ParabolGlyph>()
    private var scaleMul = 0

    private var fontDefault: Font? = null
    private val mapsDefault: ObjectList<ParabolGlyphMap> = ObjectArrayList()
    private var fontBold: Font? = null
    private val mapsBold: ObjectList<ParabolGlyphMap> = ObjectArrayList()
    private var fontItalic: Font? = null
    private val mapsItalic: ObjectList<ParabolGlyphMap> = ObjectArrayList()
    private var fontBoldItalic: Font? = null
    private val mapsBoldItalic: ObjectList<ParabolGlyphMap> = ObjectArrayList()

    private var previousGameScale = -1
    private var initialized = false

    init {
        init(initFont, originalSize)
    }

    constructor(font: Font, sizePx: Float) : this(font, sizePx, 256, 5)

    private fun sizeCheck() {
        val gs = ParabolRenderer.MC.window.scaleFactor.toInt()
        if (gs != this.previousGameScale) {
            close()
            init(initFont, this.originalSize)
        }
    }

    private fun init(font: Font, sizePx: Float) {
        check(!initialized) { "Double call to init()" }
        initialized = true
        this.previousGameScale = ParabolRenderer.MC.window.scaleFactor.toInt()
        this.scaleMul = this.previousGameScale
        this.fontDefault = font.deriveFont(sizePx * this.scaleMul).deriveFont(Font.PLAIN)
        this.fontBold = font.deriveFont(sizePx * this.scaleMul).deriveFont(Font.BOLD)
        this.fontItalic = font.deriveFont(sizePx * this.scaleMul).deriveFont(Font.ITALIC)
        this.fontBoldItalic = font.deriveFont(sizePx * this.scaleMul).deriveFont(Font.BOLD or Font.ITALIC)
    }

    private fun generateMap(from: Char, to: Char, font: Int): ParabolGlyphMap {
        val gm = ParabolGlyphMap(from, to, fontFromStyle(font)!!, randomIdentifier(), padding, if (font == Font.ITALIC || font == Font.BOLD or Font.ITALIC) 1.3f else 0f)
        mapsFromStyle(font).add(gm)
        return gm
    }

    private fun fontFromStyle(style: Int): Font? {
        return when(style) {
            Font.PLAIN -> fontDefault
            Font.BOLD -> fontBold
            Font.ITALIC -> fontItalic
            Font.BOLD or Font.ITALIC -> fontBoldItalic
            else -> null
        }
    }

    private fun mapsFromStyle(style: Int): ObjectList<ParabolGlyphMap> {
        return when(style) {
            Font.PLAIN -> mapsDefault
            Font.BOLD -> mapsBold
            Font.ITALIC -> mapsItalic
            Font.BOLD or Font.ITALIC -> mapsBoldItalic
            else -> ObjectArrayList()
        }
    }

    private fun locateGlyph0(glyph: Char, font: Int): ParabolGlyph {
        for (map in mapsFromStyle(font)) {
            if (map.contains(glyph)) {
                return map.getGlyph(glyph)
            }
        }
        val base = floorNearestMulN(glyph.code, charsPerPage)
        val glyphMap: ParabolGlyphMap = generateMap(base.toChar(), (base + charsPerPage).toChar(), font)
        return glyphMap.getGlyph(glyph)
    }

    fun drawText(stack: MatrixStack, text: ParabolText, x: Double, y: Double) {
        drawText(
            stack,
            text,
            x.toFloat(),
            y.toFloat()
        )
    }

    private fun drawText(
        stack: MatrixStack,
        text: ParabolText,
        x: Float,
        y: Float
    ) {
        var yI = y

        sizeCheck()
        stack.push()
        yI -= 3f
        stack.translate(roundToDecimal(x.toDouble()), roundToDecimal(yI.toDouble()), 0.0)
        stack.scale(1f / this.scaleMul, 1f / this.scaleMul, 1f)

        RenderSystem.enableBlend()
        RenderSystem.defaultBlendFunc()
        RenderSystem.disableCull()
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR)
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR)

        RenderSystem.setShader(ShaderProgramKeys.POSITION_TEX_COLOR)
        var bb: BufferBuilder
        val mat = stack.peek().positionMatrix
        var xOffset = 0f
        var yOffset = 0f
        synchronized(glyphPageCache) {
            text.visit {
                val txt = it.text
                val chars = txt.toCharArray()
                val style = it.style.toJavaFontStyle()
                val color = it.color
                val r = color.red.toFloat() / 255f
                val g = color.green.toFloat() / 255f
                val b = color.blue.toFloat() / 255f
                val a = color.alpha.toFloat() / 255f

                for (i in chars.indices) {
                    val c = chars[i]

                    if (c == '\n') {
                        yOffset += getTextPartHeight(it) * scaleMul
                        xOffset = 0f
                        continue
                    }

                    val glyph = locateGlyph0(c, style)
                    if (glyph.value != ' ') {
                        val i1: Identifier = glyph.owner.bindToTexture
                        val entry = DrawEntry(xOffset, yOffset, r, g, b, a, glyph)
                        glyphPageCache.computeIfAbsent(i1,
                            Object2ObjectFunction<Identifier, ObjectList<DrawEntry>> { _: Any -> ObjectArrayList() })
                            .add(entry)
                    }

                    xOffset += glyph.width
                }
            }

            for (identifier in glyphPageCache.keys) {
                RenderSystem.setShaderTexture(0, identifier)
                val objects: List<DrawEntry> = glyphPageCache[identifier]!!.toList()

                bb = Tessellator.getInstance().begin(DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR)

                for ((xo, yo, cr, cg, cb, ca, glyph) in objects) {
                    val owner: ParabolGlyphMap = glyph.owner
                    val w: Float = glyph.width.toFloat()
                    val h: Float = glyph.height.toFloat()
                    val u1: Float = glyph.u.toFloat() / owner.width
                    val v1: Float = glyph.v.toFloat() / owner.height
                    val u2: Float = (glyph.u + glyph.width.toFloat())/ owner.width
                    val v2: Float = (glyph.v + glyph.height.toFloat()) / owner.height

                    bb.vertex(mat, xo + 0, yo + h, 0f).texture(u1, v2).color(cr, cg, cb, ca)
                    bb.vertex(mat, xo + w, yo + h, 0f).texture(u2, v2).color(cr, cg, cb, ca)
                    bb.vertex(mat, xo + w, yo + 0, 0f).texture(u2, v1).color(cr, cg, cb, ca)
                    bb.vertex(mat, xo + 0, yo + 0, 0f).texture(u1, v1).color(cr, cg, cb, ca)
                }

                BufferRenderer.drawWithGlobalProgram(bb.end())

            }
            glyphPageCache.clear()
        }
        stack.pop()
    }

    fun getTextWidth(text: ParabolText): Float {
        var width = 0f

        text.visit {
            width += getTextPartWidth(it)
        }

        return width
    }

    fun getStringWidth(text: String): Float {
        return getTextWidth(ParabolText.literal(text))
    }

    fun getTextHeight(text: ParabolText): Float {
        val heights = mutableListOf<Float>()

        text.visit {
            heights.add(getTextPartHeight(it))
        }

        return heights.max()
    }

    fun getStringHeight(text: String): Float {
        return getTextHeight(ParabolText.literal(text))
    }

    private fun getTextPartWidth(textPart: ParabolText.Part): Float {
        val c = stripControlCodes(textPart.text).toCharArray()
        var currentLine = 0f
        var maxPreviousLines = 0f
        for (c1 in c) {
            if (c1 == '\n') {
                maxPreviousLines = max(currentLine.toDouble(), maxPreviousLines.toDouble()).toFloat()
                currentLine = 0f
                continue
            }
            val glyph = locateGlyph0(c1, textPart.style.toJavaFontStyle())
            currentLine += (glyph.width.toFloat() / scaleMul.toFloat()).toFloat()
        }
        return max(currentLine.toDouble(), maxPreviousLines.toDouble()).toFloat()
    }

    private fun getTextPartHeight(textPart: ParabolText.Part): Float {
        var c = stripControlCodes(textPart.text).toCharArray()
        if (c.isEmpty()) {
            c = charArrayOf(' ')
        }
        var currentLine = 0f
        var previous = 0f
        for (c1 in c) {
            if (c1 == '\n') {
                if (currentLine == 0f) {
                    currentLine = (Objects.requireNonNull(locateGlyph0(' ', textPart.style.toJavaFontStyle()))!!.height.toFloat() / scaleMul.toFloat())
                }
                previous += currentLine
                currentLine = 0f
                continue
            }
            val glyph = locateGlyph0(c1, textPart.style.toJavaFontStyle())
            currentLine =
                (glyph.height.toFloat() / scaleMul.toFloat()).coerceAtLeast(currentLine)
        }
        return currentLine + previous
    }


    override fun close() {
        try {
            for (map in mapsDefault) {
                map.destroy()
            }
            mapsDefault.clear()
            for (map in mapsBold) {
                map.destroy()
            }
            mapsBold.clear()
            for (map in mapsItalic) {
                map.destroy()
            }
            mapsItalic.clear()
            for (map in mapsBoldItalic) {
                map.destroy()
            }
            mapsBoldItalic.clear()
            allGlyphs.clear()
            initialized = false
        } catch (ignored: Exception) {
        }
    }

    @JvmRecord
    internal data class DrawEntry(
        val atX: Float,
        val atY: Float,
        val r: Float,
        val g: Float,
        val b: Float,
        val a: Float,
        val toDraw: ParabolGlyph
    )

    private fun floorNearestMulN(x: Int, n: Int): Int {
        return n * floor(x.toDouble() / n.toDouble()).toInt()
    }

    private fun stripControlCodes(text: String): String {
        val chars = text.toCharArray()
        val f = StringBuilder()
        var i = 0
        while (i < chars.size) {
            val c = chars[i]
            if (c == '§') {
                i++
                i++
                continue
            }
            f.append(c)
            i++
        }
        return f.toString()
    }

    private fun randomIdentifier(): Identifier {
        return Identifier.of(ParabolRenderer.MOD_ID, "temp/" + randomString())
    }

    private fun randomString(): String {
        return IntStream.range(0, 32).mapToObj { _: Int ->
            (Random.nextInt(
                'a'.code,
                'z'.code + 1
            ).toChar()).toString()
        }.collect(Collectors.joining())
    }

    private fun roundToDecimal(n: Double): Double {
        val factor: Double = 10.0.pow(1.0)
        return Math.round(n * factor) / factor
    }
}