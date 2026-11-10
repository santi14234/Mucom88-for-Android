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
import android.net.Uri
import android.view.View
import android.text.InputFilter
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import android.view.inputmethod.EditorInfo
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
    private val pcmPanel = LinearLayout(ctx)
    private val pcmInfo = TextView(ctx)
    private val pcmListScroll = ScrollView(ctx)
    private val pcmList = LinearLayout(ctx)
    private val tabs = ArrayList<TextView>()
    private var pcm: PcmBank? = null
    private val leftCol = LinearLayout(ctx)
    private val panel = LinearLayout(ctx)
    private var landscape = false
    private val prefs = ctx.getSharedPreferences("ui", Context.MODE_PRIVATE)
    private var fracPortrait = prefs.getFloat("fracP", 0.40f)
    private var fracLand = prefs.getFloat("fracL", 0.42f)
    private val splitter = Splitter(ctx) { d -> onSplit(d) }
    private var bankUri: Uri? = null
    private var bankName = "mucompcm.bin (incluido)"
    private val views = ArrayList<View>()

    private val chanRegex = Regex("^[A-K]+(?=\\s)")
    private val macroDefRegex = Regex("^#\\s*\\*\\d+\\s*\\{")
    private val tokenRegex = Regex(
        "(@(?:%\\d+|\"[^\"]*\"|\\d+))" +
            "|(\\*\\d+|[\\\\¥]=?[\\d,]*)" +
            "|((?:yDM|yTL|yKA|yDR|ySR|ySL|ySE|MF|MW|MC|ML|MD|RF|Rm|vm|[tTCovVqplDKkHRMEPwySsmJL])" +
            "(?:\\$[0-9A-Fa-f]+|[\\d,+\\-])*)" +
            "|([<>])" +
            "|([\\[\\]()/&^{}])"
    )

    init {
        orientation = LinearLayout.VERTICAL
        setBackgroundColor(C.BG)
        val pad = context.dp(8)
        setPadding(pad, pad, pad, 0)

        leftCol.orientation = LinearLayout.VERTICAL
        leftCol.addView(
            buttonRow(
                context.pillButton("NUEVO", C.ORANGE) { newFile() },
                context.pillButton("ABRIR", C.ORANGE) { log("ABRIR: llega en el Día 5") },
                context.pillButton("GUARDAR", C.ORANGE) { log("GUARDAR: llega en el Día 5") }
            )
        )
        leftCol.addView(
            buttonRow(
                context.pillButton("▶ PLAY", C.GREEN) { log("PLAY: la música llega cuando conectemos el chip FM (Día 3-4)") },
                context.pillButton("■ STOP", C.GREEN) { log("STOP: sin música todavía (Día 3-4)") },
                context.pillButton("COMPILAR", C.GREEN) { log("COMPILAR: llega en el Día 4") }
            )
        )

        val hint = TextView(context)
        hint.text = "‹ desliza a la izquierda: Tonos FM"
        hint.setTextColor(C.MUTED)
        hint.textSize = 11f
        hint.gravity = Gravity.END
        hint.setPadding(0, context.dp(2), 0, context.dp(4))
        leftCol.addView(hint, LinearLayout.LayoutParams(MATCH, WRAP))

        editor.setText(TEMPLATE)
        editor.typeface = Fonts.mono
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
        editor.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
        leftCol.addView(editor, LinearLayout.LayoutParams(MATCH, 0, 1f))

        panel.orientation = LinearLayout.VERTICAL
        panel.setBackgroundColor(C.PANEL)

        val tabRow = LinearLayout(context)
        tabRow.orientation = LinearLayout.HORIZONTAL
        listOf("CONSOLA", "MONITOR", "TECLADO", "ADPCM").forEachIndexed { i, name ->
            val t = TextView(context)
            t.text = name
            t.textSize = 11f
            t.gravity = Gravity.CENTER
            t.setPadding(0, context.dp(8), 0, context.dp(8))
            t.setOnClickListener { showTab(i) }
            tabs.add(t)
            tabRow.addView(t, LinearLayout.LayoutParams(0, WRAP, 1f))
        }
        panel.addView(tabRow, LinearLayout.LayoutParams(MATCH, WRAP))

        console.typeface = Fonts.mono
        console.textSize = 11f
        console.setTextColor(C.TEXT)
        console.setPadding(context.dp(8), context.dp(4), context.dp(8), context.dp(4))
        console.text = "MUCOM88 para Android v0.2 (Día 2)\nFuente: " + (if (Fonts.loaded) "DejaVu Sans Mono (incluida)" else "del sistema (no se pudo cargar editor.ttf)") + "\nConversor de muestras WAV listo. Chip FM y compilador: Días 3 y 4\n"
        loadDefaultPcm()
        consoleScroll.addView(console)

        monitor.typeface = Fonts.mono
        monitor.textSize = 11f
        monitor.setTextColor(C.TEXT)
        monitor.setPadding(context.dp(8), context.dp(4), context.dp(8), context.dp(4))
        monitor.text = buildMonitor()

        buildPcmPanel()

        views.add(consoleScroll)
        views.add(monitor)
        views.add(keyboard)
        views.add(pcmPanel)

        val body = FrameLayout(context)
        views.forEach { body.addView(it, FrameLayout.LayoutParams(MATCH, MATCH)) }
        panel.addView(body, LinearLayout.LayoutParams(MATCH, 0, 1f))
        addView(leftCol, LinearLayout.LayoutParams(MATCH, 0, 1f))
        addView(splitter, LinearLayout.LayoutParams(MATCH, context.dp(16)))
        addView(panel, LinearLayout.LayoutParams(MATCH, 0, 1f))
        applyOrientation()

        showTab(0)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val land = w > h
        if (land != landscape) {
            landscape = land
            post { applyOrientation() }
        }
    }

    private fun applyOrientation() {
        val pad = context.dp(8)
        val bar = context.dp(16)
        val frac = if (landscape) fracLand else fracPortrait
        splitter.vertical = landscape
        if (landscape) {
            orientation = LinearLayout.HORIZONTAL
            setPadding(pad, pad, pad, pad)
            leftCol.layoutParams = LinearLayout.LayoutParams(0, MATCH, 1f - frac)
            splitter.layoutParams = LinearLayout.LayoutParams(bar, MATCH)
            panel.layoutParams = LinearLayout.LayoutParams(0, MATCH, frac)
        } else {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, 0)
            leftCol.layoutParams = LinearLayout.LayoutParams(MATCH, 0, 1f - frac)
            splitter.layoutParams = LinearLayout.LayoutParams(MATCH, bar)
            panel.layoutParams = LinearLayout.LayoutParams(MATCH, 0, frac)
        }
        splitter.invalidate()
    }

    // Arrastrar la barra: delta > 0 = el panel se hace más pequeño.
    private fun onSplit(delta: Float) {
        val a = if (landscape) leftCol.width else leftCol.height
        val b = if (landscape) panel.width else panel.height
        val total = (a + b).toFloat()
        if (total <= 0f) return
        val f = ((b - delta) / total).coerceIn(0.2f, 0.75f)
        if (landscape) fracLand = f else fracPortrait = f
        (leftCol.layoutParams as LinearLayout.LayoutParams).weight = 1f - f
        (panel.layoutParams as LinearLayout.LayoutParams).weight = f
        requestLayout()
        prefs.edit().putFloat(if (landscape) "fracL" else "fracP", f).apply()
    }

    private fun loadDefaultPcm() {
        try {
            val bank = PcmBank.loadDefault(context)
            pcm = bank
            console.append("ADPCM: mucompcm.bin cargado (${bank.entries.size} sonidos). Ver pestaña ADPCM.\n")
            refreshPcmUi()
        } catch (e: Exception) {
            console.append("No se pudo cargar mucompcm.bin: ${e.message}\n")
        }
    }

    private fun buildPcmPanel() {
        pcmPanel.orientation = LinearLayout.VERTICAL

        val row = LinearLayout(context)
        row.orientation = LinearLayout.HORIZONTAL
        val saveBtn = context.pillButton("GUARDAR", C.ORANGE) { saveBank(false) }
        saveBtn.setOnLongClickListener {
            saveBank(true)
            true
        }
        val bs = listOf(
            context.pillButton("NEW +", C.GREEN) { newBank() },
            context.pillButton("+ WAV", C.ORANGE) { addWav() },
            context.pillButton("CARGAR", C.ORANGE) { loadBank() },
            saveBtn
        )
        for (b in bs) {
            b.textSize = 11f
            b.setPadding(context.dp(2), context.dp(7), context.dp(2), context.dp(7))
            val lp = LinearLayout.LayoutParams(0, WRAP, 1f)
            lp.setMargins(context.dp(3), context.dp(3), context.dp(3), context.dp(3))
            row.addView(b, lp)
        }
        pcmPanel.addView(row, LinearLayout.LayoutParams(MATCH, WRAP))

        pcmInfo.typeface = Fonts.mono
        pcmInfo.textSize = 11f
        pcmInfo.setTextColor(C.MUTED)
        pcmInfo.setPadding(context.dp(8), context.dp(2), context.dp(8), context.dp(2))
        pcmPanel.addView(pcmInfo, LinearLayout.LayoutParams(MATCH, WRAP))

        pcmList.orientation = LinearLayout.VERTICAL
        pcmListScroll.addView(pcmList)
        pcmPanel.addView(pcmListScroll, LinearLayout.LayoutParams(MATCH, 0, 1f))
    }

    private fun refreshPcmUi() {
        val bank = pcm ?: return
        pcmList.removeAllViews()
        bank.entries.forEachIndexed { i, e ->
            val row = LinearLayout(context)
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER_VERTICAL
            row.setPadding(context.dp(8), 0, context.dp(4), 0)

            val label = TextView(context)
            label.typeface = Fonts.mono
            label.textSize = 11f
            label.setTextColor(if (e.added) C.CH_GREEN else C.TEXT)
            label.setSingleLine(true)
            label.text = String.format("@%-2d %-14s %6d B", i + 1, e.name, e.adpcm.size)
            row.addView(label, LinearLayout.LayoutParams(0, WRAP, 1f))

            val play = TextView(context)
            play.text = "▶"
            play.textSize = 14f
            play.setTextColor(Color.WHITE)
            play.setPadding(context.dp(12), context.dp(8), context.dp(12), context.dp(8))
            play.setOnClickListener { AudioPreview.play(e.adpcm, e.rate) }
            row.addView(play, LinearLayout.LayoutParams(WRAP, WRAP))

            if (e.added) {
                val del = TextView(context)
                del.text = "✕"
                del.textSize = 14f
                del.setTextColor(0xFFFF6B6B.toInt())
                del.setPadding(context.dp(12), context.dp(8), context.dp(12), context.dp(8))
                del.setOnClickListener {
                    bank.entries.remove(e)
                    refreshPcmUi()
                    log("Quitada: ${e.name}")
                }
                row.addView(del, LinearLayout.LayoutParams(WRAP, WRAP))
            }
            pcmList.addView(row, LinearLayout.LayoutParams(MATCH, WRAP))
        }
        pcmInfo.text = "$bankName\n${bank.entries.size}/${PcmBank.MAX_ENTRIES} muestras · memoria ${bank.usedBytes() / 1024} KB de 256 KB"
    }

    private fun addWav() {
        val act = context as? MainActivity ?: return
        act.openDocument(arrayOf("audio/*", "application/octet-stream")) { uri ->
            try {
                val wav = WavReader.read(act.readUri(uri))
                showAddDialog(wav, act.displayName(uri).take(14))
            } catch (e: Exception) {
                log("WAV: ${e.message}")
            }
        }
    }

    private fun showAddDialog(wav: WavData, defaultName: String) {
        val box = LinearLayout(context)
        box.orientation = LinearLayout.VERTICAL
        box.setPadding(context.dp(20), context.dp(8), context.dp(20), context.dp(4))

        val secs = wav.samples.size.toDouble() / wav.rate
        val info = TextView(context)
        info.text = String.format("%.2f s · %d Hz · %d muestras", secs, wav.rate, wav.samples.size)
        info.textSize = 13f
        info.setTextColor(Color.WHITE)
        box.addView(info, LinearLayout.LayoutParams(MATCH, WRAP))

        val nameEdit = EditText(context)
        nameEdit.setText(defaultName)
        nameEdit.setSingleLine(true)
        nameEdit.hint = "Nombre (máx. 14)"
        nameEdit.setTextColor(Color.WHITE)
        nameEdit.setHintTextColor(C.MUTED)
        nameEdit.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(14))
        nameEdit.imeOptions = EditorInfo.IME_FLAG_NO_EXTRACT_UI
        box.addView(nameEdit, LinearLayout.LayoutParams(MATCH, WRAP))

        val options = listOf("Original" to 0, "22 kHz" to 22050, "16 kHz" to 16000, "11 kHz" to 11025)
        val group = RadioGroup(context)
        group.orientation = RadioGroup.HORIZONTAL
        options.forEachIndexed { i, opt ->
            val rb = RadioButton(context)
            rb.id = 1000 + i
            rb.text = opt.first
            rb.textSize = 12f
            rb.setTextColor(Color.WHITE)
            group.addView(rb)
        }
        group.check(1000)
        box.addView(group, LinearLayout.LayoutParams(MATCH, WRAP))

        val hint = TextView(context)
        hint.text = "Máximo 64 KB por muestra. Si no cabe, baja la frecuencia."
        hint.textSize = 11f
        hint.setTextColor(C.MUTED)
        box.addView(hint, LinearLayout.LayoutParams(MATCH, WRAP))

        val scroll = ScrollView(context)
        scroll.addView(box)

        AlertDialog.Builder(context)
            .setTitle("Añadir muestra")
            .setView(scroll)
            .setPositiveButton("OK") { _, _ ->
                val idx = (group.checkedRadioButtonId - 1000).coerceIn(0, options.size - 1)
                confirmAdd(wav, nameEdit.text.toString(), defaultName, options[idx].second)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmAdd(wav: WavData, typed: String, defaultName: String, targetRate: Int) {
        val bank = pcm ?: return
        if (bank.entries.size >= PcmBank.MAX_ENTRIES) {
            log("Banco lleno: máximo ${PcmBank.MAX_ENTRIES} muestras")
            return
        }
        val name = typed.trim().ifEmpty { defaultName }.take(14)
        val rate = if (targetRate in 1 until wav.rate) targetRate else wav.rate
        val shorts = WavReader.resample(wav.samples, wav.rate, rate)
        val adpcm = AdpcmCodec.encode(shorts)
        if (adpcm.size > PcmBank.MAX_SAMPLE_BYTES) {
            log("\"$name\" ocupa ${adpcm.size} B y el máximo por muestra es ${PcmBank.MAX_SAMPLE_BYTES} B. Baja la frecuencia o recorta el WAV.")
            return
        }
        if (bank.usedBytes() + PcmBank.spacing(adpcm.size) > PcmBank.RAM_BYTES) {
            log("No cabe: la memoria ADPCM es de 256 KB")
            return
        }
        bank.entries.add(PcmEntry(name, PcmBank.newUnk(), adpcm, rate, true))
        refreshPcmUi()
        log("Añadida @${bank.entries.size}: $name (${adpcm.size} B, $rate Hz)")
        AudioPreview.play(adpcm, rate)
    }

    private fun loadBank() {
        val act = context as? MainActivity ?: return
        act.openDocument(arrayOf("*/*"), true) { uri ->
            try {
                val bytes = act.readUri(uri)
                val bank = PcmBank.parse(bytes)
                if (bank.entries.isEmpty() && bytes.size < PcmBank.HEADER) {
                    log("CARGAR: ese archivo no parece un banco ADPCM de MUCOM88")
                } else {
                    pcm = bank
                    bankUri = uri
                    bankName = act.fileName(uri)
                    refreshPcmUi()
                    log("Banco cargado: $bankName (${bank.entries.size} muestras)")
                }
            } catch (e: Exception) {
                log("CARGAR: ${e.message}")
            }
        }
    }

    // NEW +: elige vacío o con las 16 de mucompcm.bin, y luego el explorador pide nombre y carpeta.
    private fun newBank() {
        AlertDialog.Builder(context)
            .setTitle("Nuevo banco de muestras")
            .setItems(arrayOf("Vacío", "Con las 16 de mucompcm.bin")) { _, which ->
                chooseNewFile(which == 1)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun chooseNewFile(withDefault: Boolean) {
        val act = context as? MainActivity ?: return
        act.createDocument("mi_banco.bin") { uri ->
            try {
                val bank = if (withDefault) PcmBank.loadDefault(context) else PcmBank(ArrayList())
                act.writeUri(uri, bank.toBytes())
                pcm = bank
                bankUri = uri
                bankName = act.fileName(uri)
                refreshPcmUi()
                log("Banco nuevo: $bankName (${bank.entries.size} muestras). Usa + WAV para añadir.")
            } catch (e: Exception) {
                log("NEW +: ${e.message}")
            }
        }
    }

    // GUARDAR: sobrescribe el archivo del banco; si no hay, o con pulsación larga, abre el explorador.
    private fun saveBank(saveAs: Boolean) {
        val act = context as? MainActivity ?: return
        val bank = pcm ?: return
        val uri = bankUri
        if (uri != null && !saveAs) {
            try {
                val bytes = bank.toBytes()
                act.writeUri(uri, bytes)
                log("Guardado en $bankName (${bank.entries.size} muestras, ${bytes.size} bytes)")
                return
            } catch (e: Exception) {
                log("No se pudo sobrescribir $bankName; elige dónde guardar")
            }
        }
        act.createDocument(if (bankUri != null) bankName else "mi_banco.bin") { newUri ->
            try {
                val bytes = bank.toBytes()
                act.writeUri(newUri, bytes)
                bankUri = newUri
                bankName = act.fileName(newUri)
                refreshPcmUi()
                log("Guardado como $bankName (${bank.entries.size} muestras, ${bytes.size} bytes)")
            } catch (e: Exception) {
                log("GUARDAR: ${e.message}")
            }
        }
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
        for (sp in e.getSpans(0, e.length, ForegroundColorSpan::class.java)) e.removeSpan(sp)
        var pos = 0
        for (line in e.toString().split("\n")) {
            val s = pos
            val semi = line.indexOf(';')
            val codeEnd = if (semi >= 0) semi else line.length
            if (semi >= 0) paint(e, s + semi, s + line.length, C.MUTED)
            val code = line.substring(0, codeEnd)

            var from = 0
            if (code.startsWith("#")) {
                val m = macroDefRegex.find(code)
                if (m != null) {
                    paint(e, s, s + m.range.last + 1, C.MACRO)
                    from = m.range.last + 1
                } else {
                    paint(e, s, s + codeEnd, C.YELLOW)
                    from = codeEnd
                }
            } else {
                val ch = chanRegex.find(code)
                if (ch != null) {
                    paint(e, s, s + ch.range.last + 1, C.CH_GREEN)
                    from = ch.range.last + 1
                }
            }

            if (from < codeEnd) {
                for (m in tokenRegex.findAll(code.substring(from))) {
                    val color = when {
                        m.groups[1] != null -> C.TONE
                        m.groups[2] != null -> C.MACRO
                        m.groups[3] != null -> C.CMD
                        m.groups[4] != null -> C.CMD
                        else -> C.BRACKET
                    }
                    paint(e, s + from + m.range.first, s + from + m.range.last + 1, color)
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
