package com.santi.mucom88

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import android.view.MotionEvent
import android.view.View
import kotlin.math.max
import kotlin.math.min

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
        typeface = Fonts.mono
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

private class AlgDef(
    val pos: Array<FloatArray>,
    val links: Array<IntArray>,
    val outs: IntArray,
    val bottomOut: Boolean = false
)

private fun p(c: Float, r: Float) = floatArrayOf(c, r)

private fun l(a: Int, b: Int) = intArrayOf(a, b)

private val ALGS = arrayOf(
    AlgDef(arrayOf(p(0f, 0f), p(1f, 0f), p(2f, 0f), p(3f, 0f)), arrayOf(l(0, 1), l(1, 2), l(2, 3)), intArrayOf(3)),
    AlgDef(arrayOf(p(0f, 0f), p(0f, 1f), p(1f, 0.5f), p(2f, 0.5f)), arrayOf(l(0, 2), l(1, 2), l(2, 3)), intArrayOf(3)),
    AlgDef(arrayOf(p(0f, 0f), p(0f, 1f), p(1f, 1f), p(2f, 0.5f)), arrayOf(l(0, 3), l(1, 2), l(2, 3)), intArrayOf(3)),
    AlgDef(arrayOf(p(0f, 0f), p(1f, 0f), p(1f, 1f), p(2f, 0.5f)), arrayOf(l(0, 1), l(1, 3), l(2, 3)), intArrayOf(3)),
    AlgDef(arrayOf(p(0f, 0f), p(1f, 0f), p(0f, 1f), p(1f, 1f)), arrayOf(l(0, 1), l(2, 3)), intArrayOf(1, 3)),
    AlgDef(arrayOf(p(0f, 1f), p(1f, 0f), p(1f, 1f), p(1f, 2f)), arrayOf(l(0, 1), l(0, 2), l(0, 3)), intArrayOf(1, 2, 3)),
    AlgDef(arrayOf(p(0f, 0f), p(1f, 0f), p(1f, 1f), p(1f, 2f)), arrayOf(l(0, 1)), intArrayOf(1, 2, 3)),
    AlgDef(arrayOf(p(0f, 0f), p(1f, 0f), p(2f, 0f), p(3f, 0f)), arrayOf(), intArrayOf(0, 1, 2, 3), true)
)

class AlgorithmView(ctx: Context) : View(ctx) {
    var fb = 0
    var alg = 0

    private val boxCarrier = Paint().apply { color = Color.WHITE }
    private val boxMod = Paint().apply { color = 0xFFB8C4FF.toInt() }
    private val num = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = ctx.dpf(1.5f)
    }
    private val fbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
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
        typeface = Fonts.mono
    }

    private fun arrowHead(c: Canvas, x: Float, y: Float, s: Float) {
        c.drawLine(x, y, x - s, y - s * 0.6f, stroke)
        c.drawLine(x, y, x - s, y + s * 0.6f, stroke)
    }

    override fun onDraw(canvas: Canvas) {
        val dpv = context.dpf(1f)
        val w = width.toFloat()
        val h = height.toFloat()
        canvas.drawRect(1f, 1f, w - 1f, h - 1f, border)
        canvas.drawText("Algorithm  (FB $fb / ALG $alg)", w / 2f, 15f * dpv, title)

        val a = ALGS[alg.coerceIn(0, 7)]
        var maxCol = 0
        var maxRow = 0f
        for (q in a.pos) {
            maxCol = max(maxCol, q[0].toInt())
            maxRow = max(maxRow, q[1])
        }
        val ncols = maxCol + 1
        val pad = 6f * dpv
        val titleH = 22f * dpv
        val loopH = 10f * dpv
        val busH = if (a.bottomOut) 12f * dpv else 0f
        val availH = h - titleH - loopH - 2f * pad - busH
        val bw = min(28f * dpv, availH / (maxRow * 1.35f + 1f))
        val rs = bw * 1.35f
        val outW = 36f * dpv
        val step = min(76f * dpv, (w - 2f * pad - bw - outW) / max(ncols - 1, 1))
        val diagW = (ncols - 1) * step + bw + outW
        val left = (w - diagW) / 2f
        val total = bw + maxRow * rs
        val top = titleH + loopH + pad + (availH - total) / 2f

        val xs = FloatArray(4) { left + a.pos[it][0] * step }
        val ys = FloatArray(4) { top + a.pos[it][1] * rs + bw / 2f }

        for (k in a.links) {
            val x1 = xs[k[0]] + bw
            val y1 = ys[k[0]]
            val x2 = xs[k[1]]
            val y2 = ys[k[1]]
            if (y1 == y2) {
                canvas.drawLine(x1, y1, x2, y2, stroke)
            } else {
                val xm = (x1 + x2) / 2f
                canvas.drawLine(x1, y1, xm, y1, stroke)
                canvas.drawLine(xm, y1, xm, y2, stroke)
                canvas.drawLine(xm, y2, x2, y2, stroke)
            }
        }

        if (a.bottomOut) {
            val busY = ys[0] + bw / 2f + 9f * dpv
            for (o in a.outs) {
                val cx = xs[o] + bw / 2f
                canvas.drawLine(cx, ys[o] + bw / 2f, cx, busY, stroke)
            }
            val xEnd = xs[3] + bw + 22f * dpv
            canvas.drawLine(xs[0] + bw / 2f, busY, xEnd, busY, stroke)
            arrowHead(canvas, xEnd, busY, 5f * dpv)
        } else {
            var busX = 0f
            var minY = Float.MAX_VALUE
            var maxY = 0f
            for (o in a.outs) {
                busX = max(busX, xs[o] + bw + 10f * dpv)
                minY = min(minY, ys[o])
                maxY = max(maxY, ys[o])
            }
            for (o in a.outs) canvas.drawLine(xs[o] + bw, ys[o], busX, ys[o], stroke)
            if (a.outs.size > 1) canvas.drawLine(busX, minY, busX, maxY, stroke)
            val midY = (minY + maxY) / 2f
            val xEnd = busX + 22f * dpv
            canvas.drawLine(busX, midY, xEnd, midY, stroke)
            arrowHead(canvas, xEnd, midY, 5f * dpv)
        }

        for (i in 0 until 4) {
            val carrier = i in a.outs
            canvas.drawRect(xs[i], ys[i] - bw / 2f, xs[i] + bw, ys[i] + bw / 2f, if (carrier) boxCarrier else boxMod)
            num.textSize = bw * 0.6f
            canvas.drawText((i + 1).toString(), xs[i] + bw / 2f, ys[i] + bw * 0.22f, num)
        }

        if (fb > 0) {
            fbPaint.strokeWidth = (1f + fb * 0.4f) * dpv
            val bx = xs[0]
            val by = ys[0] - bw / 2f
            val loop = Path()
            loop.moveTo(bx + bw * 0.25f, by)
            loop.lineTo(bx + bw * 0.25f, by - 8f * dpv)
            loop.lineTo(bx + bw * 0.75f, by - 8f * dpv)
            loop.lineTo(bx + bw * 0.75f, by)
            canvas.drawPath(loop, fbPaint)
        }
    }
}

class PianoRowsView(ctx: Context) : View(ctx) {
    private val white = Paint().apply { color = 0xFFD0D0D0.toInt() }
    private val black = Paint().apply { color = 0xFF111111.toInt() }
    private val label = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = ctx.spf(11f)
        typeface = Fonts.mono
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

// Barra arrastrable para cambiar el tamaño del panel (CONSOLA / MONITOR / TECLADO / ADPCM).
class Splitter(ctx: Context, private val onDrag: (Float) -> Unit) : View(ctx) {
    var vertical = false
    private var last = 0f
    private val dot = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0xFF7A7A86.toInt() }

    override fun onDraw(canvas: Canvas) {
        val r = context.dpf(2.2f)
        val gap = context.dpf(8f)
        val cx = width / 2f
        val cy = height / 2f
        for (i in -1..1) {
            if (vertical) canvas.drawCircle(cx, cy + i * gap, r, dot)
            else canvas.drawCircle(cx + i * gap, cy, r, dot)
        }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                last = if (vertical) e.rawX else e.rawY
                parent.requestDisallowInterceptTouchEvent(true)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                val cur = if (vertical) e.rawX else e.rawY
                onDrag(cur - last)
                last = cur
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                parent.requestDisallowInterceptTouchEvent(false)
                return true
            }
        }
        return super.onTouchEvent(e)
    }
}
