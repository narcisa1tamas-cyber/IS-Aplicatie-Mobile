package com.example.is_aplicatie_mobile.streaming

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object MjpegStreamReader {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS)  // fără timeout — stream infinit
        .build()

    /**
     * Deschide stream-ul MJPEG și emite frame-uri ca [ImageBitmap] pe măsură ce sosesc.
     * Detectează frame-urile prin markerii JPEG: SOI (0xFF 0xD8) și EOI (0xFF 0xD9).
     */
    fun streamFrames(url: String): Flow<ImageBitmap> = flow {
        val request = Request.Builder()
            .url(url)
            .addHeader("ngrok-skip-browser-warning", "true")
            .addHeader("User-Agent", "MedBotApp/1.0")
            .addHeader("Cache-Control", "no-cache")
            .build()

        client.newCall(request).execute().use { response ->
            val inputStream = response.body?.byteStream() ?: return@flow
            val buffered = BufferedInputStream(inputStream, 65_536)

            val jpegBuffer = ByteArrayOutputStream(65_536)
            val readBuf = ByteArray(8_192)
            var inJpeg = false
            var prevByte = -1

            while (true) {
                val bytesRead = buffered.read(readBuf)
                if (bytesRead == -1) break

                for (i in 0 until bytesRead) {
                    val b = readBuf[i].toInt() and 0xFF

                    if (!inJpeg) {
                        // Detectează SOI — începutul unui frame JPEG
                        if (prevByte == 0xFF && b == 0xD8) {
                            inJpeg = true
                            jpegBuffer.reset()
                            jpegBuffer.write(0xFF)
                            jpegBuffer.write(0xD8)
                        }
                    } else {
                        jpegBuffer.write(b)
                        // Detectează EOI — sfârșitul frame-ului JPEG
                        if (prevByte == 0xFF && b == 0xD9) {
                            inJpeg = false
                            val frameBytes = jpegBuffer.toByteArray()
                            BitmapFactory.decodeByteArray(frameBytes, 0, frameBytes.size)
                                ?.asImageBitmap()
                                ?.let { emit(it) }
                            jpegBuffer.reset()
                        }
                    }
                    prevByte = b
                }
            }
        }
    }.flowOn(Dispatchers.IO)
}
