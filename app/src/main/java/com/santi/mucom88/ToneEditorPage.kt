package com.santi.mucom88

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.SeekBar
import android.widget.TextView

class ToneEditorPage(ctx: Context) : ScrollView(ctx) {

    private val names = arrayOf(
        "Attack Rate", "Decay Rate", "Sustain Rate", "Release Rate", "Sustain Level",
        "Total Level", "KeyScale Rate", "Multiple", "Detune", "FB / ALG"
    )
    private val maxes = intArrayOf(31, 31, 31, 15, 15, 127, 3, 15, 7, 7)
    private val vals = arrayOf(
        intArrayOf(31, 31, 31, 31),
        intArrayOf(18, 14, 10, 10),
        intArrayOf(0, 4, 4, 3),
        intArrayOf(6, 6, 6, 6),
        intArrayOf(2, 2, 2, 2),
        intArrayOf(36, 45, 18, 0),
        intArrayOf(0, 0, 1, 1),
        intArrayOf(10, 0, 0, 0),
        intArrayOf(3, 3, 3, 3),
        intArrayOf(7, 0, 0, 0)
    )

    private val cells = Array(10) { arrayOfNulls<TextView>(4) }
    private val rowLabels = arrayOfNulls<TextView>(10)
    private val colHeads = arrayOfNulls<TextView>(4)
    private val envs = Array(4) { EnvelopeView(ctx, "OP${it + 1}") }
    private val algo = AlgorithmView(ctx)
    private val editLabel = TextView(ctx)
    private val seek = SeekBar(ctx)
    private var selRow = 0
    private var selCol = 0

    init {
        setBackgroundColor(C.NAVY)
        isFillViewport = true

        val root = LinearLayout(context)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(context.dp(10), context.dp(8), context.dp(10), context.dp(8))
        addView(root, FrameLayout.LayoutParams(MATCH, WRAP))

        val title = TextView(context)
        title.text = "FM Parameters"
        title.setTextColor(Color.WHITE)
        title.textSize = 14f
        title.typeface = Typeface.MONOSPACE
        root.addView(title, LinearLayout.LayoutParams(MATCH, WRAP))

        val hint = TextView(context)
        hint.text = "desliza a la derecha › para volver al editor"
        hint.setTextColor(C.MUTED)
        hint.textSize = 11f
        hint.setPadding(0, 0, 0, context.dp(6))
        root.addView(hint, LinearLayout.LayoutParams(MATCH, WRAP))

        val rowH = context.dp(28)

        val left = LinearLayout(context)
        left.orientation = LinearLayout.VERTICAL
        left.addView(headerRow(), LinearLayout.LayoutParams(MATCH, rowH))
        for (r in 0 until 10) left.addView(makeRow(r), LinearLayout.LayoutParams(MATCH, rowH))

        val right = LinearLayout(context)
        right.orientation = LinearLayout.VERTICAL
        right.addView(View(context), LinearLayout.LayoutParams(MATCH, rowH))
        for (e in envs) {
            val lp = LinearLayout.LayoutParams(MATCH, 0, 1f)
            lp.setMargins(context.dp(6), 0, 0, context.dp(2))
            right.addView(e, lp)
        }

        val body = LinearLayout(context)
        body.orientation = LinearLayout.HORIZONTAL
        body.addView(left, LinearLayout.LayoutParams(0, MATCH, 2f))
        body.addView(right, LinearLayout.LayoutParams(0, MATCH, 1f))
        root.addView(body, LinearLayout.LayoutParams(MATCH, rowH * 11))

        val algoLp = LinearLayout.LayoutParams(MATCH, context.dp(84))
        algoLp.topMargin = context.dp(8)
        root.addView(algo, algoLp)

        editLabel.setTextColor(Color.WHITE)
        editLabel.textSize = 12f
        editLabel.typeface = Typeface.MONOSPACE
        val labLp = LinearLayout.LayoutParams(MATCH, WRAP)
        labLp.topMargin = context.dp(10)
        root.addView(editLabel, labLp)

        val ctl = LinearLayout(context)
        ctl.orientation = LinearLayout.HORIZONTAL
        ctl.gravity = Gravity.CENTER_VERTICAL
        ctl.addView(context.pillButton("−", C.GREEN) { bump(-1) }, LinearLayout.LayoutParams(context.dp(44), WRAP))
        ctl.addView(seek, LinearLayout.LayoutParams(0, WRAP, 1f))
        ctl.addView(context.pillButton("+", C.GREEN) { bump(1) }, LinearLayout.LayoutParams(context.dp(44), WRAP))
        root.addView(ctl, LinearLayout.LayoutParams(MATCH, WRAP))

        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    vals[selRow][selCol] = progress
                    refresh()
                }
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}

            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        val info = TextView(context)
        info.text = "ToneNo : 0    Name : (sin nombre)    octave : 1"
        info.setTextColor(C.MUTED)
        info.textSize = 11f
        info.typeface = Typeface.MONOSPACE
        val infoLp = LinearLayout.LayoutParams(MATCH, WRAP)
        infoLp.topMargin = context.dp(10)
        root.addView(info, infoLp)

        refresh()
    }

    private fun headerRow(): LinearLayout {
        val row = LinearLayout(context)
        row.orientation = LinearLayout.HORIZONTAL
        row.addView(View(context), LinearLayout.LayoutParams(0, MATCH, 2.6f))
        for (c in 0 until 4) {
            val tv = TextView(context)
            tv.text = "OP${c + 1}"
            tv.setTextColor(Color.WHITE)
            tv.textSize = 11f
            tv.typeface = Typeface.MONOSPACE
            tv.gravity = Gravity.CENTER
            colHeads[c] = tv
            row.addView(tv, LinearLayout.LayoutParams(0, MATCH, 1f))
        }
        return row
    }

    private fun makeRow(r: Int): LinearLayout {
        val row = LinearLayout(context)
        row.orientation = LinearLayout.HORIZONTAL

        val lab = TextView(context)
        lab.text = names[r]
        lab.setTextColor(Color.WHITE)
        lab.textSize = 11f
        lab.typeface = Typeface.MONOSPACE
        lab.gravity = Gravity.CENTER_VERTICAL
        lab.setSingleLine(true)
        rowLabels[r] = lab
        row.addView(lab, LinearLayout.LayoutParams(0, MATCH, 2.6f))

        for (c in 0 until 4) {
            val cell = TextView(context)
            cell.setTextColor(Color.WHITE)
            cell.textSize = 12f
            cell.typeface = Typeface.MONOSPACE
            cell.gravity = Gravity.CENTER
            cell.setOnClickListener { select(r, c) }
            cells[r][c] = cell
            val lp = LinearLayout.LayoutParams(0, MATCH, 1f)
            lp.setMargins(context.dp(1), context.dp(1), context.dp(1), context.dp(1))
            row.addView(cell, lp)
        }
        return row
    }

    private fun select(r: Int, c: Int) {
        if (r == 9 && c != 0 && c != 2) return
        selRow = r
        selCol = c
        refresh()
    }

    private fun bump(d: Int) {
        val v = (vals[selRow][selCol] + d).coerceIn(0, maxes[selRow])
        vals[selRow][selCol] = v
        refresh()
    }

    private fun refresh() {
        for (r in 0 until 10) {
            rowLabels[r]?.setBackgroundColor(if (r == selRow) C.NAVY_ROW else Color.TRANSPARENT)
            for (c in 0 until 4) {
                val tv = cells[r][c] ?: continue
                tv.text = if (r == 9 && c != 0 && c != 2) "---" else vals[r][c].toString()
                if (r == selRow && c == selCol) {
                    tv.background = roundBg(C.NAVY_SEL, context.dpf(4f))
                } else {
                    tv.setBackgroundColor(Color.TRANSPARENT)
                }
            }
        }
        for (c in 0 until 4) {
            colHeads[c]?.setBackgroundColor(if (c == selCol) C.NAVY_ROW else Color.TRANSPARENT)
        }
        for (op in 0 until 4) {
            val e = envs[op]
            e.ar = vals[0][op]
            e.dr = vals[1][op]
            e.sr = vals[2][op]
            e.rr = vals[3][op]
            e.sl = vals[4][op]
            e.tl = vals[5][op]
            e.invalidate()
        }
        algo.fb = vals[9][0]
        algo.alg = vals[9][2]
        algo.invalidate()

        val what = if (selRow == 9) {
            if (selCol == 0) "Feedback" else "Algorithm"
        } else {
            names[selRow]
        }
        editLabel.text = "OP${selCol + 1} · $what: ${vals[selRow][selCol]}  (0-${maxes[selRow]})"
        seek.max = maxes[selRow]
        seek.progress = vals[selRow][selCol]
    }
}
