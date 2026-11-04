package com.unciv.web

import com.badlogic.gdx.graphics.Pixmap
import com.unciv.ui.components.fonts.FontFamilyData
import com.unciv.ui.components.fonts.FontImplementation
import com.unciv.ui.components.fonts.FontMetricsCommon
import com.unciv.ui.components.fonts.Fonts
import org.teavm.jso.browser.Window
import org.teavm.jso.canvas.CanvasRenderingContext2D
import org.teavm.jso.dom.html.HTMLCanvasElement

/**
 * Glyph source for the in-game font atlas: draws text onto an offscreen <canvas> and reads the pixels back.
 * This replaces DesktopFont (java.awt). First draft - metrics are approximations, tune against real rendering.
 */
class WebFont : FontImplementation {
    private var family = Fonts.DEFAULT_FONT_FAMILY
    private var size = 50

    private val canvas = Window.current().document.createElement("canvas") as HTMLCanvasElement
    private val ctx = canvas.getContext("2d") as CanvasRenderingContext2D

    override fun setFontFamily(fontFamilyData: FontFamilyData, size: Int) {
        family = fontFamilyData.invariantName
        this.size = size
    }

    override fun getFontSize() = size

    override fun getCharPixmap(symbolString: String): Pixmap {
        ctx.font = "${size}px \"$family\", sans-serif"
        var width = Math.ceil(ctx.measureText(symbolString).width).toInt()
        val metrics = getMetrics()
        val height = metrics.height.toInt().coerceAtLeast(1)
        if (width <= 0) width = size  // e.g. tab
        canvas.width = width
        canvas.height = height
        ctx.font = "${size}px \"$family\", sans-serif"  // resizing a canvas resets its state
        ctx.clearRect(0.0, 0.0, width.toDouble(), height.toDouble())
        ctx.fillStyle = "white"
        ctx.textBaseline = "alphabetic"
        ctx.fillText(symbolString, 0.0, metrics.leading + metrics.ascent)

        val rgba = ctx.getImageData(0.0, 0.0, width.toDouble(), height.toDouble()).data
        val argb = IntArray(width * height) { i ->
            val a = rgba.get(i * 4 + 3).toInt() and 0xff
            // glyphs are drawn white; keep only the alpha like DesktopFont does
            (a shl 24) or 0xffffff
        }
        return Fonts.pixmapFromArgb(width, height, argb)
    }

    /** Browsers can't enumerate installed fonts; offer a few generic families. */
    override fun getSystemFonts(): Sequence<FontFamilyData> =
        sequenceOf("sans-serif", "serif", "monospace").map { FontFamilyData(it) }

    override fun getMetrics(): FontMetricsCommon {
        val ascent = size * 0.93f
        val descent = size * 0.24f
        return FontMetricsCommon(ascent = ascent, descent = descent, height = ascent + descent, leading = 0f)
    }
}
