package com.santi.mucom88

import android.content.Context

class PcmBank(val data: ByteArray, val names: List<String>) {

    companion object {
        const val DEFAULT_ASSET = "mucompcm.bin"

        fun loadDefault(context: Context): PcmBank {
            val bytes = context.assets.open(DEFAULT_ASSET).use { it.readBytes() }
            return PcmBank(bytes, readNames(bytes))
        }

        private fun readNames(d: ByteArray): List<String> {
            val out = ArrayList<String>()
            var i = 0
            while ((i + 1) * 32 <= d.size) {
                val base = i * 32
                var ok = true
                for (k in 0 until 16) {
                    val b = d[base + k].toInt() and 0xFF
                    if (b < 0x20 || b > 0x7E) {
                        ok = false
                        break
                    }
                }
                if (!ok) break
                out.add(String(d, base, 16, Charsets.US_ASCII).trim())
                i++
            }
            return out
        }
    }
}
