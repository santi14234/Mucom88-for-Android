package com.santi.mucom88

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.widget.TextView

val MATCH = ViewGroup.LayoutParams.MATCH_PARENT
val WRAP = ViewGroup.LayoutParams.WRAP_CONTENT

object C {
    val BG = 0xFF161616.toInt()
    val PANEL = 0xFF101012.toInt()
    val EDITOR_BG = 0xFF1E1E22.toInt()
    val ORANGE = 0xFFD99A00.toInt()
    val GREEN = 0xFF00775C.toInt()
    val NAVY = 0xFF00004B.toInt()
    val NAVY_SEL = 0xFF6C7AA6.toInt()
    val NAVY_ROW = 0xFF1A1A78.toInt()
    val TEXT = 0xFFE6E6E6.toInt()
    val MUTED = 0xFF9A9A9A.toInt()
    val YELLOW = 0xFFE5C34F.toInt()
    val CH_GREEN = 0xFF5DCAA5.toInt()
    val TONE = 0xFFF0997B.toInt()
    val BRACKET = 0xFF85B7EB.toInt()
    val CMD = 0xFF7DCFE8.toInt()
    val MACRO = 0xFFC792EA.toInt()
    val CYAN = 0xFF22C7E8.toInt()
    val AMBER = 0xFFF0A800.toInt()
    val ROW_WHITE = 0xFFDDDDDD.toInt()
    val ROW_GREEN = 0xFF4CAF50.toInt()

    fun channelColor(ch: Char): Int = when (ch) {
        'D', 'E', 'F' -> CYAN
        'G' -> ROW_GREEN
        'K' -> AMBER
        else -> ROW_WHITE
    }
}

fun Context.dp(v: Int): Int = (v * resources.displayMetrics.density + 0.5f).toInt()

fun Context.dpf(v: Float): Float = v * resources.displayMetrics.density

fun Context.spf(v: Float): Float =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, v, resources.displayMetrics)

fun roundBg(color: Int, radiusPx: Float): GradientDrawable =
    GradientDrawable().apply {
        setColor(color)
        cornerRadius = radiusPx
    }

fun Context.pillButton(label: String, color: Int, onClick: () -> Unit): TextView {
    val tv = TextView(this)
    tv.text = label
    tv.setTextColor(Color.WHITE)
    tv.textSize = 12f
    tv.typeface = Typeface.DEFAULT_BOLD
    tv.gravity = Gravity.CENTER
    tv.background = roundBg(color, dpf(8f))
    tv.setPadding(dp(4), dp(10), dp(4), dp(10))
    tv.isClickable = true
    tv.setOnClickListener { onClick() }
    return tv
}
