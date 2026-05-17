package com.example.colorwave.audio

import android.content.Context
import android.media.*
import android.net.Uri
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.sqrt

object FileAnalyzer {

    fun analyze(context: Context, uri: Uri): AudioFeatures {
        val extractor = MediaExtractor()

        try {
            extractor.setDataSource(context, uri, null)

            val trackIndex = (0 until extractor.trackCount)
                .firstOrNull {
                    extractor.getTrackFormat(it)
                        .getString(MediaFormat.KEY_MIME)
                        ?.startsWith("audio/") == true
                } ?: return fallback(uri)

            extractor.selectTrack(trackIndex)
            val format = extractor.getTrackFormat(trackIndex)

            val codec = MediaCodec.createDecoderByType(
                format.getString(MediaFormat.KEY_MIME)!!
            )
            codec.configure(format, null, null, 0)
            codec.start()

            var low = 0f
            var mid = 0f
            var high = 0f
            var rms = 0f
            var samples = 0
            var seed = 0L

            val bufferInfo = MediaCodec.BufferInfo()
            var extractorDone = false
            var decodeDone = false

            while (!decodeDone && samples < 300_000) {

                if (!extractorDone) {
                    val inIndex = codec.dequeueInputBuffer(10_000)
                    if (inIndex >= 0) {
                        val input = codec.getInputBuffer(inIndex)!!
                        val size = extractor.readSampleData(input, 0)

                        if (size < 0) {
                            codec.queueInputBuffer(
                                inIndex, 0, 0, 0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                            extractorDone = true
                        } else {
                            codec.queueInputBuffer(
                                inIndex, 0, size,
                                extractor.sampleTime, 0
                            )
                            extractor.advance()
                        }
                    }
                }

                val outIndex = codec.dequeueOutputBuffer(bufferInfo, 10_000)
                if (outIndex >= 0) {
                    val output = codec.getOutputBuffer(outIndex)!!
                    output.order(ByteOrder.LITTLE_ENDIAN)

                    while (output.remaining() >= 2) {
                        val v = output.short / 32768f
                        val a = abs(v)

                        rms += v * v

                        when {
                            samples % 3 == 0 -> low += a
                            samples % 3 == 1 -> mid += a
                            else -> high += a
                        }

                        if (samples % 500 == 0) {
                            seed += (a * 10_000).toLong()
                        }

                        samples++
                    }

                    codec.releaseOutputBuffer(outIndex, false)

                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                        decodeDone = true
                    }
                }
            }

            codec.stop()
            codec.release()
            extractor.release()

            if (samples == 0) return fallback(uri)

            val total = low + mid + high + 0.0001f

            return AudioFeatures(
                energy = (sqrt(rms / samples) * 2f).coerceIn(0f, 1f),
                brightness = (high / total).coerceIn(0f, 1f),
                complexity = (mid / total).coerceIn(0f, 1f),
                valence = ln(1 + low / total).coerceIn(0f, 1f),
                seed = seed xor uri.toString().hashCode().toLong()
            )

        } catch (e: Exception) {
            return fallback(uri)
        } finally {
            extractor.release()
        }
    }

    private fun fallback(uri: Uri) =
        AudioFeatures(
            energy = 0.4f,
            brightness = 0.4f,
            complexity = 0.4f,
            valence = 0.4f,
            seed = uri.hashCode().toLong()
        )
}