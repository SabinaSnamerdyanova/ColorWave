package com.example.colorwave.audio

import android.content.Context
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.sqrt

object FileAnalyzer {
    fun analyze(context: Context, uri: Uri): AudioFeatures {
        val extractor = MediaExtractor()
        return try {
            extractor.setDataSource(context, uri, null)
            val trackIndex = (0 until extractor.trackCount).firstOrNull {
                extractor.getTrackFormat(it).getString(MediaFormat.KEY_MIME)?.startsWith("audio/") == true
            } ?: return defaultFeatures()

            extractor.selectTrack(trackIndex)
            val format = extractor.getTrackFormat(trackIndex)
            val duration = format.getLong(MediaFormat.KEY_DURATION)
            val codec = MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME)!!)
            codec.configure(format, null, null, 0)
            codec.start()

            var rmsSum = 0f
            var zcrSum = 0f
            var deltaSum = 0f
            var peakAmp = 0.01f
            var count = 0L
            val bufferInfo = MediaCodec.BufferInfo()

            val points = (1..25).map { it / 26.0 }
            for (p in points) {
                extractor.seekTo((duration * p).toLong(), MediaExtractor.SEEK_TO_CLOSEST_SYNC)
                var frames = 0
                while (frames < 10) {
                    val inIdx = codec.dequeueInputBuffer(5000)
                    if (inIdx >= 0) {
                        val buf = codec.getInputBuffer(inIdx)!!
                        val size = extractor.readSampleData(buf, 0)
                        if (size < 0) break
                        codec.queueInputBuffer(inIdx, 0, size, extractor.sampleTime, 0)
                        extractor.advance()
                    }
                    val outIdx = codec.dequeueOutputBuffer(bufferInfo, 5000)
                    if (outIdx >= 0) {
                        val outBuf = codec.getOutputBuffer(outIdx)!!
                        outBuf.order(ByteOrder.LITTLE_ENDIAN)
                        var prevV = 0f
                        while (outBuf.remaining() >= 2) {
                            val v = outBuf.short / 32768f
                            val av = abs(v)
                            if (av > peakAmp) peakAmp = av
                            rmsSum += v * v
                            val delta = abs(v - prevV)
                            deltaSum += delta
                            if ((v > 0 && prevV < 0) || (v < 0 && prevV > 0)) zcrSum++
                            prevV = v
                            count++
                        }
                        frames++
                        codec.releaseOutputBuffer(outIdx, false)
                    }
                }
                codec.flush()
            }
            codec.stop()
            codec.release()
            extractor.release()

            if (count == 0L) count = 1
            val gain = 1.0f / peakAmp.coerceAtLeast(0.05f)

            AudioFeatures(
                energy = (sqrt(rmsSum / count) * gain * 1.5f).coerceIn(0f, 1f),
                brightness = (zcrSum / count * 60f).coerceIn(0f, 1f),
                valence = (deltaSum / count * gain * 25f).coerceIn(0f, 1f),
                complexity = (deltaSum / count * 30f).coerceIn(0f, 1f),
                seed = (rmsSum.toLong() + uri.toString().hashCode())
            )
        } catch (e: Exception) {
            defaultFeatures()
        }
    }

    private fun defaultFeatures() = AudioFeatures(0.5f, 0.5f, 0.5f, 0.5f, 0L)
}
