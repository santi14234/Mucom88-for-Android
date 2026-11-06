package com.santi.mucom88

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.Editable
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.widget.doAfterTextChanged

class EditorPage(ctx: Context) : LinearLayout(ctx) {

    private val editor = EditText(ctx)
    private val console = TextView(ctx)
    private val consoleScroll = ScrollView(ctx)
    private val monitor = TextView(ctx)
    private val keyboard = PianoRowsView(ctx)
    private val tabs = ArrayList<TextView>()
    private val views = ArrayList<View>()

    private val chanRegex = Regex("^[A-K]+(?=\\s)")
    private val toneRegex = Regex("@\\d+")
    private val bracketRegex = Regex("[\\[\\]()]")

    init {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(C.BG)
        val pad = context.dp(8)
        setPadding(pad, pad, pad, 0)

        addView(
            buttonRow(
                context.pillButton("NUEVO", C.ORANGE) { newFile() },
                context.pillButton("ABRIR", C.ORANGE) { log("ABRIR: llega en el Día 5") },
                context.pillButton("GUARDAR", C.ORANGE) { log("GUARDAR: llega en el Día 5") }
            )
        )
        addView(
            buttonRow(
                context.pillButton("▶ PLAY", C.GREEN) { log("PLAY: el audio llega en el Día 2") },
                context.pillButton("■ STOP", C.GREEN) { log("STOP: el audio llega en el Día 2") },
                context.pillButton("COMPILAR", C.GREEN) { log("COMPILAR: llega en el Día 4") }
            )
        )

        val hint = TextView(context)
        hint.text = "‹ desliza a la izquierda: Tonos FM"
        hint.setTextColor(C.MUTED)
        hint.textSize = 11f
        hint.gravity = Gravity.END
        hint.setPadding(0, context.dp(2), 0, context.dp(4))
        addView(hint, LinearLayout.LayoutParams(MATCH, WRAP))

        editor.setText(TEMPLATE)
        editor.typeface = Typeface.MONOSPACE
        editor.textSize = 13f
        editor.setTextColor(C.TEXT)
        editor.setBackgroundColor(C.EDITOR_BG)
        editor.gravity = Gravity.TOP or Gravity.START
        editor.inputType = InputType.TYPE_CLASS_TEXT or
            InputType.TYPE_TEXT_FLAG_MULTI_LINE or
            InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        editor.setPadding(context.dp(8), context.dp(6), context.dp(8), context.dp(6))
        editor.doAfterTextChanged { e -> if (e != null) highlight(e) }
        highlight(editor.text)
        addView(editor, LinearLayout.LayoutParams(MATCH, 0, 3f))

        val panel = LinearLayout(context)
        panel.orientation = LinearLayout.VERTICAL
        panel.setBackgroundColor(C.PANEL)

        val tabRow = LinearLayout(context)
        tabRow.orientation = LinearLayout.HORIZONTAL
        listOf("CONSOLA", "MONITOR", "TECLADO").forEachIndexed { i, name ->
            val t = TextView(context)
            t.text = name
            t.textSize = 12f
            t.gravity = Gravity.CENTER
            t.setPadding(0, context.dp(8), 0, context.dp(8))
            t.setOnClickListener { showTab(i) }
            tabs.add(t)
            tabRow.addView(t, LinearLayout.LayoutParams(0, WRAP, 1f))
        }
        panel.addView(tabRow, LinearLayout.LayoutParams(MATCH, WRAP))

        console.typeface = Typeface.MONOSPACE
        console.textSize = 11f
        console.setTextColor(C.TEXT)
        console.setPadding(context.dp(8), context.dp(4), context.dp(8), context.dp(4))
        console.text = "MUCOM88 para Android v0.1 (Día 1)\nMotor de audio: pendiente (Días 2 a 4)\n"
        consoleScroll.addView(console)

        monitor.typeface = Typeface.MONOSPACE
        monitor.textSize = 11f
        monitor.setTextColor(C.TEXT)
        monitor.setPadding(context.dp(8), context.dp(4), context.dp(8), context.dp(4))
        monitor.text = buildMonitor()

        views.add(consoleScroll)
        views.add(monitor)
        views.add(keyboard)

        val body = FrameLayout(context)
        views.forEach { body.addView(it, FrameLayout.LayoutParams(MATCH, MATCH)) }
        panel.addView(body, LinearLayout.LayoutParams(MATCH, 0, 1f))
        addView(panel, LinearLayout.LayoutParams(MATCH, 0, 2f))

        showTab(0)
    }

    private fun buttonRow(vararg buttons: TextView): LinearLayout {
        val row = LinearLayout(context)
        row.orientation = LinearLayout.HORIZONTAL
        for (b in buttons) {
            val lp = LinearLayout.LayoutParams(0, WRAP, 1f)
            lp.setMargins(context.dp(3), context.dp(3), context.dp(3), context.dp(3))
            row.addView(b, lp)
        }
        return row
    }

    private fun showTab(i: Int) {
        views.forEachIndexed { idx, v ->
            v.visibility = if (idx == i) View.VISIBLE else View.GONE
        }
        tabs.forEachIndexed { idx, t ->
            t.setTextColor(if (idx == i) Color.WHITE else C.MUTED)
            t.typeface = if (idx == i) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
    }

    private fun log(msg: String) {
        console.append(msg + "\n")
        showTab(0)
        consoleScroll.post { consoleScroll.fullScroll(View.FOCUS_DOWN) }
    }

    private fun newFile() {
        editor.setText(TEMPLATE)
        log("Archivo nuevo")
    }

    private fun highlight(e: Editable) {
        for (s in e.getSpans(0, e.length, ForegroundColorSpan::class.java)) e.removeSpan(s)
        var pos = 0
        for (line in e.toString().split("\n")) {
            val s = pos
            when {
                line.startsWith("#") -> paint(e, s, s + line.length, C.YELLOW)
                line.startsWith(";") -> paint(e, s, s + line.length, C.MUTED)
                else -> {
                    chanRegex.find(line)?.let { paint(e, s, s + it.range.last + 1, C.CH_GREEN) }
                    toneRegex.findAll(line).forEach {
                        paint(e, s + it.range.first, s + it.range.last + 1, C.TONE)
                    }
                    bracketRegex.findAll(line).forEach {
                        paint(e, s + it.range.first, s + it.range.last + 1, C.BRACKET)
                    }
                }
            }
            pos += line.length + 1
        }
    }

    private fun paint(e: Editable, a: Int, b: Int, color: Int) {
        if (b > a) e.setSpan(ForegroundColorSpan(color), a, b, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    private fun buildMonitor(): CharSequence {
        val sb = SpannableStringBuilder()
        sb.append("  Inst Vol Det   Adr  Note   LFO Rev p qtz\n")
        for (ch in 'A'..'K') {
            val start = sb.length
            if (ch == 'G') {
                sb.append("$ch ---- 000 00000 0000 ------ OFF OFF - 000\n")
            } else {
                sb.append("$ch ---- 000 00000 0000 o1     OFF OFF - 000\n")
            }
            sb.setSpan(
                ForegroundColorSpan(C.channelColor(ch)),
                start,
                sb.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
        return sb
    }

    companion object {
        const val TEMPLATE =
            "#mucom88 1.7\n#driver mucom88\n#title Mi canción\n#composer Santi\n#author Santi\n" +
                "#voice voice.dat\n#pcm mucompcm.bin\n#comment Sin comentario\n\n" +
                "A C192T150 @9v10l4o4q2 cdefgab>c2\n"
    }
}
