package com.example.screenmirror

import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI

class SignalingClient(
    serverUrl: String,
    private val onMessage: (String) -> Unit
) {
    private val client: WebSocketClient

    init {
        client = object : WebSocketClient(URI(serverUrl)) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                println("Signaling: connected")
            }

            override fun onMessage(message: String?) {
                if (message != null) {
                    onMessage(message)
                }
            }

            override fun onClose(code: Int, reason: String?, remote: Boolean) {
                println("Signaling: closed - $reason")
            }

            override fun onError(ex: Exception?) {
                println("Signaling: error - ${ex?.message}")
            }
        }
    }

    fun connect() {
        client.connect()
    }

    fun send(message: String) {
        if (client.isOpen) {
            client.send(message)
        }
    }

    fun close() {
        client.close()
    }
}
