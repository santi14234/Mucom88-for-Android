package com.santi.mucom88

import android.content.Context
import android.graphics.Typeface

object Fonts {
    var mono: Typeface = Typeface.MONOSPACE
        private set

    // Busca assets/fonts/editor.ttf; si no existe, usa la monoespaciada del sistema.
    fun init(ctx: Context) {
        mono = try {
            Typeface.createFromAsset(ctx.assets, "fonts/editor.ttf")
        } catch (e: Exception) {
            Typeface.MONOSPACE
        }
    }
}
