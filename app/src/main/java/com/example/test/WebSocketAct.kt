package com.example.test.network

import okhttp3.*
import okio.ByteString

class WebSocketAct {
    private val client = OkHttpClient()
    private val request = Request.Builder().url("https://0yzyb4-2a01-620-c199-8d01-7d67-7da-7165-a1b8.ru.tuna.am").build()
    private var webSocket: WebSocket? = null

    // WebSocket Initialization
    init {
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                println("WebSocket opened")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                println("Message received: $text")
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                println("Message received: ${bytes.hex()}")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                println("Closing WebSocket: $code $reason")
                webSocket.close(1000, null)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                println("WebSocket failure: ${t.message}")
            }
        })
    }

    // Sending geolocation data to the server via WebSocket
    fun sendLocationData(
        latitude: Double?,
        longitude: Double?,
        rsrp: Int?,
        cellId: Int?,
        lac: Int?,
        cellType: String?
    ) {
        val jsonData = """
            {
                "latitude": $latitude,
                "longitude": $longitude,
                "rsrp": $rsrp,
                "cellId": $cellId,
                "lac": $lac,
                "cellType": "$cellType"
            }
        """.trimIndent()
        webSocket?.send(jsonData)
    }
}