ppackage com.santi.mucom88

import android.content.Context
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

object Fonts {
    private var cachedTypeface: Typeface? = null

    fun get(context: Context): Typeface {
        if (cachedTypeface == null) {
            cachedTypeface = try {
                Typeface.createFromAsset(context.assets, "fonts/romfont.ttf")
            } catch (e: Exception) {
                Typeface.MONOSPACE // Fallback si no encuentra el archivo
            }
        }
        return cachedTypeface!!
    }

    // Aplica la fuente recursivamente a cualquier vista o layout
    fun applyTo(view: View) {
        val font = get(view.context)
        if (view is TextView) {
            view.typeface = font
        } else if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                applyTo(view.getChildAt(i))
            }
        }
    }
}
