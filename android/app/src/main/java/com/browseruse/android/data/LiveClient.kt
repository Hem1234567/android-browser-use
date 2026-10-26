package com.browseruse.android.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import java.util.concurrent.TimeUnit

class LiveClient(
    private val onFrame: (Bitmap) -> Unit,
    private val onStatus: (String) -> Unit
) {

    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var socket: WebSocket? = null

    fun connect(url: String) {
        close()
        val request = Request.Builder().url(url).build()
        socket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                onStatus("live")
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                val array = bytes.toByteArray()
                val header = FrameDecoder.header(array) ?: return
                if (header.type == FrameDecoder.TYPE_JPEG) {
                    val payload = FrameDecoder.payload(array) ?: return
                    val bitmap = BitmapFactory.decodeByteArray(payload, 0, payload.size)
                    if (bitmap != null) onFrame(bitmap)
                }
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                onStatus(text)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                onStatus("offline")
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                onStatus("closed")
            }
        })
    }

    fun send(text: String) {
        socket?.send(text)
    }

    fun close() {
        socket?.close(1000, null)
        socket = null
    }
}
