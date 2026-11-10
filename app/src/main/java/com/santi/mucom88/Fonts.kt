package com.santi.mucom88

import android.content.Context
import android.graphics.Typeface

object Fonts {
    var mono: Typeface = Typeface.MONOSPACE
        private set
    var loaded: Boolean = false
        private set

    // Usa assets/fonts/editor.ttf (DejaVu Sans Mono). Si falla, la monoespaciada del sistema.
    fun init(ctx: Context) {
        try {
            mono = Typeface.createFromAsset(ctx.assets, "fonts/editor.ttf")
            loaded = true
        } catch (e: Exception) {
            mono = Typeface.MONOSPACE
            loaded = false
        }
    }
}
