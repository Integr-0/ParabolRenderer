package net.integr.parabol.renderer.engine.font.builder

import java.awt.Color
import java.awt.Font

class ParabolText private constructor(){
    private var start: Part? = null

    companion object {
        fun literal(text: String): ParabolText {
            val txt = ParabolText()
            txt.start = Part(text, null)
            return txt
        }
    }

    private fun getLast(): Part {
        var current = start!!
        while (current.next != null) {
            current = current.next!!
        }

        return current
    }

    fun append(text: String): ParabolText {
        val part = Part(text, null)

        getLast().next = part

        return this
    }

    fun styled(style: Style): ParabolText {
        if (start == null) {
            throw IllegalStateException("Cannot style empty text")
        }

        getLast().style = style
        return this
    }

    fun colored(color: Color): ParabolText {
        if (start == null) {
            throw IllegalStateException("Cannot color empty text")
        }

        getLast().color = color
        return this
    }

    fun getString(): String {
        val builder = StringBuilder()
        var current = start

        while (current != null) {
            builder.append(current.text)
            current = current.next
        }

        return builder.toString()
    }

    fun visit(visitor: (Part) -> Unit) {
        var current = start
        while (current != null) {
            visitor(current)
            current = current.next
        }
    }

    data class Part(val text: String, var next: Part?, var style: Style = Style.NORMAL, var color: Color = Color.WHITE)

    enum class Style {
        ITALIC,
        BOLD,
        BOLD_ITALIC,
        NORMAL;

        fun toJavaFontStyle(): Int {
            return when (this) {
                ITALIC -> Font.ITALIC
                BOLD -> Font.BOLD
                BOLD_ITALIC -> Font.BOLD or Font.ITALIC
                NORMAL -> Font.PLAIN
            }
        }
    }
}