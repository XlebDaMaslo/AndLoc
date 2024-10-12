package com.example.test.network

import okhttp3.*
import okio.ByteString

class WebSocketAct {
    private val client = OkHttpClient()
    private val request = Request.Builder().url("https://wrwyjs-104-28-244-75.ru.tuna.am").build()
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
    fun sendLocationData(latitude: Double?, longitude: Double?, rsrp: Int?) {
        latitude?.let { lat ->
            longitude?.let { lon ->
                rsrp?.let { rsrpValue ->
                    val jsonData = """{"rsrp": $rsrpValue, "lat": $lat, "lon": $lon}"""
                    webSocket?.send(jsonData)
                }
            }
        }
    }
}
