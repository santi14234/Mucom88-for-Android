package com.santi.mucom88

class WavData(val samples: ShortArray, val rate: Int)

object WavReader {

    private fun le16(b: ByteArray, p: Int): Int =
        (b[p].toInt() and 0xFF) or ((b[p + 1].toInt() and 0xFF) shl 8)

    private fun le16s(b: ByteArray, p: Int): Int =
        (b[p].toInt() and 0xFF) or (b[p + 1].toInt() shl 8)

    private fun le32(b: ByteArray, p: Int): Int =
        le16(b, p) or (le16(b, p + 2) shl 16)

    fun read(b: ByteArray): WavData {
        if (b.size < 44 ||
            String(b, 0, 4, Charsets.ISO_8859_1) != "RIFF" ||
            String(b, 8, 4, Charsets.ISO_8859_1) != "WAVE"
        ) {
            throw IllegalArgumentException("No es un archivo WAV válido")
        }
        var pos = 12
        var fmtFound = false
        var format = 0
        var channels = 0
        var rate = 0
        var bits = 0
        var dataStart = -1
        var dataLen = 0
        while (pos + 8 <= b.size) {
            val id = String(b, pos, 4, Charsets.ISO_8859_1)
            val len = le32(b, pos + 4)
            val body = pos + 8
            if (id == "fmt ") {
                if (body + 16 > b.size) break
                format = le16(b, body)
                channels = le16(b, body + 2)
                rate = le32(b, body + 4)
                bits = le16(b, body + 14)
                fmtFound = true
            } else if (id == "data") {
                dataStart = body
                dataLen = if (len < 0 || body + len > b.size) b.size - body else len
                break
            }
            if (len < 0) break
            pos = body + len + (len and 1)
        }
        if (!fmtFound || dataStart < 0) throw IllegalArgumentException("WAV incompleto (falta fmt o data)")
        if (format != 1 && format != 0xFFFE) throw IllegalArgumentException("Solo se admite WAV PCM sin comprimir")
        if (bits != 16 && bits != 8) throw IllegalArgumentException("Solo se admite WAV de 8 o 16 bits")
        if (channels < 1 || channels > 2) throw IllegalArgumentException("Solo se admite mono o estéreo")
        if (rate < 1000) throw IllegalArgumentException("Frecuencia de muestreo no válida")

        val bytesPer = bits / 8
        val frameBytes = bytesPer * channels
        val frames = dataLen / frameBytes
        if (frames == 0) throw IllegalArgumentException("El WAV está vacío")

        val out = ShortArray(frames)
        for (f in 0 until frames) {
            var sum = 0
            for (c in 0 until channels) {
                val p = dataStart + f * frameBytes + c * bytesPer
                sum += if (bits == 16) le16s(b, p) else ((b[p].toInt() and 0xFF) - 128) shl 8
            }
            out[f] = (sum / channels).toShort()
        }
        return WavData(out, rate)
    }

    // Solo reduce la frecuencia (promedia las muestras de cada ventana).
    fun resample(src: ShortArray, from: Int, to: Int): ShortArray {
        if (to >= from) return src
        val ratio = from.toDouble() / to
        val n = (src.size / ratio).toInt()
        val out = ShortArray(n)
        for (i in 0 until n) {
            val a = (i * ratio).toInt()
            val e = minOf(src.size, maxOf(a + 1, ((i + 1) * ratio).toInt()))
            var sum = 0L
            for (k in a until e) sum += src[k].toLong()
            out[i] = (sum / (e - a)).toInt().toShort()
        }
        return out
    }
}
