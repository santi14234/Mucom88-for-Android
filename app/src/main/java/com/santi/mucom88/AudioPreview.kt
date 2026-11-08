package com.santi.mucom88

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack

// Vista previa de muestras: decodifica el ADPCM y lo reproduce con AudioTrack.
object AudioPreview {
    private var track: AudioTrack? = null

    fun play(adpcm: ByteArray, rate: Int) {
        stop()
        val pcm = AdpcmCodec.decode(adpcm)
        if (pcm.isEmpty()) return
        try {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
            val fmt = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(maxOf(rate, 4000))
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
            val t = AudioTrack.Builder()
                .setAudioAttributes(attrs)
                .setAudioFormat(fmt)
                .setBufferSizeInBytes(pcm.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
            t.write(pcm, 0, pcm.size)
            t.setNotificationMarkerPosition(pcm.size)
            t.setPlaybackPositionUpdateListener(object : AudioTrack.OnPlaybackPositionUpdateListener {
                override fun onMarkerReached(at: AudioTrack?) {
                    if (at != null && at === track) stop()
                }

                override fun onPeriodicNotification(at: AudioTrack?) {}
            })
            track = t
            t.play()
        } catch (e: Exception) {
            stop()
        }
    }

    fun stop() {
        val t = track
        track = null
        if (t != null) {
            try {
                t.stop()
            } catch (e: Exception) {
            }
            t.release()
        }
    }
}
