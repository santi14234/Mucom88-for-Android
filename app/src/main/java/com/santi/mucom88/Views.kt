package com.santi.mucom88

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.view.View

class EnvelopeView(ctx: Context, private val label: String) : View(ctx) {
    var ar = 31
    var dr = 0
    var sr = 0
    var rr = 6
    var sl = 0
    var tl = 0

    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = ctx.dpf(2f)
        strokeCap = Paint.Cap.ROUND
    }
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFFFFB347.toInt()
        textSize = ctx.spf(11f)
        textAlign = Paint.Align.RIGHT
        typeface = Typeface.MONOSPACE
    }

    override fun onDraw(canvas: Canvas) {
        val pad = context.dpf(6f)
        val l = pad
        val r = width - pad
        val top = pad
        val bot = height - pad
        val peak = top + (bot - top) * (tl / 127f)
        val yD = if (dr == 0) peak else peak + (bot - peak) * (sl / 15f)
        val yS = yD + (bot - yD) * (sr / 31f) * 0.8f

        val wA = 0.04f + 0.22f * (31 - ar) / 31f
        val wD = if (dr == 0) 0.02f else 0.05f + 0.25f * (31 - dr) / 31f
        val wS = 0.30f
        val wR = 0.08f + 0.20f * (15 - rr) / 15f
        val sum = wA + wD + wS + wR
        val span = r - l
        val x1 = l + span * wA / sum
        val x2 = x1 + span * wD / sum
        val x3 = x2 + span * wS / sum

        line.color = 0xFF3DDC57.toInt()
        canvas.drawLine(l, bot, x1, peak, line)
        line.color = 0xFF6C7AFF.toInt()
        canvas.drawLine(x1, peak, x2, yD, line)
        canvas.drawLine(x2, yD, x3, yS, line)
        line.color = 0xFFFF3B30.toInt()
        canvas.drawLine(x3, yS, r, bot, line)

        canvas.drawText(label, width - pad, top + text.textSize, text)
    }
}

class AlgorithmView(ctx: Context) : View(ctx) {
    var fb = 0
    var alg = 0

    private val box = Paint().apply { color = Color.WHITE }
    private val num = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = ctx.spf(14f)
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = ctx.dpf(1.5f)
    }
    private val border = Paint().apply {
        color = 0xFF8888AA.toInt()
        style = Paint.Style.STROKE
        strokeWidth = ctx.dpf(1f)
    }
    private val title = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = ctx.spf(11f)
        textAlign = Paint.Align.CENTER
        typeface = Typeface.MONOSPACE
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        canvas.drawRect(1f, 1f, w - 1f, h - 1f, border)
        canvas.drawText("Algorithm  (FB $fb / ALG $alg)", w / 2f, context.dpf(15f), title)

        val bw = h * 0.34f
        val boxTop = h * 0.50f
        val gap = (w - 4 * bw) / 5f
        for (i in 0 until 4) {
            val x = gap + i * (bw + gap)
            canvas.drawRect(x, boxTop, x + bw, boxTop + bw, box)
            canvas.drawText((i + 1).toString(), x + bw / 2f, boxTop + bw * 0.72f, num)
            val cy = boxTop + bw / 2f
            if (i < 3) {
                canvas.drawLine(x + bw, cy, x + bw + gap, cy, stroke)
            } else {
                canvas.drawLine(x + bw, cy, w - context.dpf(4f), cy, stroke)
            }
        }

        val loopH = bw * 0.35f
        val p = Path()
        p.moveTo(gap + bw * 0.25f, boxTop)
        p.lineTo(gap + bw * 0.25f, boxTop - loopH)
        p.lineTo(gap + bw * 0.75f, boxTop - loopH)
        p.lineTo(gap + bw * 0.75f, boxTop)
        canvas.drawPath(p, stroke)
    }
}

class PianoRowsView(ctx: Context) : View(ctx) {
    private val white = Paint().apply { color = 0xFFD0D0D0.toInt() }
    private val black = Paint().apply { color = 0xFF111111.toInt() }
    private val label = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = ctx.spf(11f)
        typeface = Typeface.MONOSPACE
        isFakeBoldText = true
    }
    private val blackKeys = intArrayOf(0, 1, 3, 4, 5)

    override fun onDraw(canvas: Canvas) {
        val rows = 11
        val labelW = context.dpf(18f)
        val rowH = height / rows.toFloat()
        val whiteCount = 49
        val kw = (width - labelW) / whiteCount
        for (r in 0 until rows) {
            val top = r * rowH
            val ch = 'A' + r
            label.color = C.channelColor(ch)
            canvas.drawText(ch.toString(), context.dpf(4f), top + rowH * 0.72f, label)
            for (i in 0 until whiteCount) {
                val x = labelW + i * kw
                canvas.drawRect(x + 0.5f, top + 1f, x + kw - 0.5f, top + rowH - 1f, white)
            }
            for (i in 0 until whiteCount) {
                if ((i % 7) in blackKeys) {
                    val x = labelW + i * kw + kw * 0.68f
                    canvas.drawRect(x, top + 1f, x + kw * 0.64f, top + rowH * 0.62f, black)
                }
            }
        }
    }
}
