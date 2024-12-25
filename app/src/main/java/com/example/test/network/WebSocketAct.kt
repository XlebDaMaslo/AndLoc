package com.example.test.network

import android.content.Context
import okhttp3.*
import okio.ByteString
import com.example.test.location.LocationAct
import com.example.test.CellInfoAct
import java.io.File
import java.io.IOException
import android.os.Environment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log

class WebSocketAct(context: Context) {
    private val client = OkHttpClient()
    private val request = Request.Builder().url("ws://s2zbb1-2a01-620-c18f-c101-811b-cde1-3385-31bd.ru.tuna.am").build()
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

    fun sendMapData() {
        CoroutineScope(Dispatchers.IO).launch {
            val fileContent = readMapDataFromFile()
            fileContent?.let {
                Log.d("WebSocketAct", "Sending map data: $it")
                webSocket?.send(it)
            }
        }
    }


    private fun readMapDataFromFile(): String? {
        val fileName = "map_data.txt"
        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "MapData")
        val file = File(directory, fileName)

        return try {
            if (file.exists()) {
                file.readText()
            } else {
                Log.d("WebSocketAct", "File not found")
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("WebSocketAct", "Error reading file: ${e.message}")
            null
        }
    }
}