package com.santi.mucom88

// ADPCM-B del YM2608 (el mismo que usa MUCOM88 para el canal K).
// 4 bits por muestra, primer nibble = nibble alto.
object AdpcmCodec {
    private val ADJ = intArrayOf(57, 57, 57, 57, 77, 102, 128, 153)

    fun decode(adpcm: ByteArray): ShortArray {
        val out = ShortArray(adpcm.size * 2)
        var acc = 0
        var step = 127
        var n = 0
        for (b in adpcm) {
            val v = b.toInt() and 0xFF
            for (half in 0..1) {
                val nib = if (half == 0) (v shr 4) else (v and 0x0F)
                val mag = nib and 7
                val delta = ((2 * mag + 1) * step) shr 3
                acc += if ((nib and 8) != 0) -delta else delta
                acc = acc.coerceIn(-32768, 32767)
                step = ((step * ADJ[mag]) / 64).coerceIn(127, 24576)
                out[n++] = acc.toShort()
            }
        }
        return out
    }

    // Devuelve los datos rellenados hasta un múltiplo de 8 bytes (como en mucompcm.bin).
    fun encode(pcm: ShortArray): ByteArray {
        val bytes = (pcm.size + 1) / 2
        val padded = ((bytes + 7) / 8) * 8
        val out = ByteArray(maxOf(padded, 8)) { 0x80.toByte() }
        var pred = 0
        var step = 127
        for (i in pcm.indices) {
            var diff = pcm[i].toInt() - pred
            var nib = 0
            if (diff < 0) {
                nib = 8
                diff = -diff
            }
            var mag = (diff * 4) / step
            if (mag > 7) mag = 7
            nib = nib or mag
            val delta = ((2 * mag + 1) * step) shr 3
            pred += if ((nib and 8) != 0) -delta else delta
            pred = pred.coerceIn(-32768, 32767)
            step = ((step * ADJ[mag]) / 64).coerceIn(127, 24576)
            val idx = i shr 1
            out[idx] = if ((i and 1) == 0) {
                (nib shl 4).toByte()
            } else {
                (((out[idx].toInt() and 0xF0) or nib)).toByte()
            }
        }
        return out
    }
}
