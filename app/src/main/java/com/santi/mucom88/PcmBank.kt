package com.santi.mucom88

import android.content.Context

class PcmEntry(
    var name: String,
    val unk: ByteArray,
    val adpcm: ByteArray,
    val rate: Int,
    val added: Boolean
)

// Formato de mucompcm.bin:
//  - 32 entradas de 32 bytes (1024 bytes): nombre(16, Shift-JIS) + 12 bytes + offset(u16, en bloques de 4 bytes) + tamaño(u16, en bytes)
//  - los datos ADPCM empiezan en 0x400; cada muestra va seguida de 16 bytes en cero.
class PcmBank(val entries: MutableList<PcmEntry>) {

    val names: List<String> get() = entries.map { it.name }

    fun usedBytes(): Int = entries.sumOf { spacing(it.adpcm.size) }

    fun toBytes(): ByteArray {
        val total = HEADER + usedBytes()
        val out = ByteArray(total)
        var off = HEADER
        entries.forEachIndexed { i, e ->
            val base = i * 32
            for (k in 0 until 16) out[base + k] = 0x20
            val nb = e.name.toByteArray(charset("Shift_JIS"))
            System.arraycopy(nb, 0, out, base, minOf(nb.size, 14))
            System.arraycopy(e.unk, 0, out, base + 16, 12)
            val offw = (off - HEADER) / 4
            out[base + 28] = (offw and 0xFF).toByte()
            out[base + 29] = ((offw shr 8) and 0xFF).toByte()
            out[base + 30] = (e.adpcm.size and 0xFF).toByte()
            out[base + 31] = ((e.adpcm.size shr 8) and 0xFF).toByte()
            System.arraycopy(e.adpcm, 0, out, off, e.adpcm.size)
            off += spacing(e.adpcm.size)
        }
        return out
    }

    companion object {
        const val DEFAULT_ASSET = "mucompcm.bin"
        const val MAX_ENTRIES = 32
        const val HEADER = 0x400
        const val GAP = 16
        const val RAM_BYTES = 262144
        const val MAX_SAMPLE_BYTES = 65535
        const val DEFAULT_RATE = 16000

        fun spacing(size: Int): Int = ((size + GAP + 3) / 4) * 4

        fun newUnk(): ByteArray {
            val u = ByteArray(12)
            u[6] = 0x20
            u[7] = 0x20
            u[10] = 0x32
            return u
        }

        fun loadDefault(context: Context): PcmBank {
            val bytes = context.assets.open(DEFAULT_ASSET).use { it.readBytes() }
            return parse(bytes)
        }

        fun parse(d: ByteArray): PcmBank {
            val list = ArrayList<PcmEntry>()
            for (i in 0 until MAX_ENTRIES) {
                val base = i * 32
                if (base + 32 > d.size) break
                var ok = true
                for (k in 0 until 16) {
                    val b = d[base + k].toInt() and 0xFF
                    if (!((b in 0x20..0x7E) || b >= 0xA1)) {
                        ok = false
                        break
                    }
                }
                if (!ok || d[base].toInt() == 0x20) break
                val offw = (d[base + 28].toInt() and 0xFF) or ((d[base + 29].toInt() and 0xFF) shl 8)
                val size = (d[base + 30].toInt() and 0xFF) or ((d[base + 31].toInt() and 0xFF) shl 8)
                val off = HEADER + offw * 4
                if (size == 0 || off + size > d.size) break
                val name = String(d, base, 16, charset("Shift_JIS")).trim()
                list.add(
                    PcmEntry(
                        name,
                        d.copyOfRange(base + 16, base + 28),
                        d.copyOfRange(off, off + size),
                        DEFAULT_RATE,
                        false
                    )
                )
            }
            return PcmBank(list)
        }
    }
}
