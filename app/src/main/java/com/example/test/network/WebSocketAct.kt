package com.example.test.network

import android.content.Context
import okhttp3.*
import okio.ByteString
import com.example.test.location.LocationAct
import com.example.test.CellInfoAct
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log
import com.example.test.map.SignalPoint
import org.json.JSONObject

class WebSocketAct(context: Context) {
    private val client = OkHttpClient()
    private val request = Request.Builder().url("ws://192.168.0.12:8000/ws").build()
    private var webSocket: WebSocket? = null
    private val cellInfoAct = CellInfoAct(context)

    init {
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocketAct", "WebSocket opened")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocketAct", "Message received: $text")
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                Log.d("WebSocketAct", "Message received: ${bytes.hex()}")
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocketAct", "Closing WebSocket: $code $reason")
                webSocket.close(1000, null)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocketAct", "WebSocket failure: ${t.message}")
            }
        })
    }

    fun sendLocationData(locationAct: LocationAct) {
        val latitude = locationAct.latitude.value
        val longitude = locationAct.longitude.value
        val rsrp = cellInfoAct.getRsrp()

        if (latitude != null && longitude != null && rsrp != null) {
            val jsonData = """{"rsrp": $rsrp, "lat": $latitude, "lon": $longitude}"""
            Log.d("WebSocketAct", "Sending data: $jsonData")
            webSocket?.send(jsonData)
        }
    }

    fun sendMapData(signalPoint: SignalPoint) {
        val jsonData = mapToJson(signalPoint)
        Log.d("WebSocketAct", "Sending map data: $jsonData")
        webSocket?.send(jsonData)
    }

    private fun mapToJson(signalPoint: SignalPoint): String {
        val jsonObject = JSONObject()
        jsonObject.put("latitude", signalPoint.latitude)
        jsonObject.put("longitude", signalPoint.longitude)
        jsonObject.put("rsrp", signalPoint.rsrp)
        return jsonObject.toString()
    }
}