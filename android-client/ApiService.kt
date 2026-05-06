package com.example.intentionalai

import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ApiService(private val baseUrl: String) {

    fun checkIntent(appName: String): JSONObject? {
        return postRequest("$baseUrl/intent-check", """{"app": "$appName"}""")
    }

    fun sendIntentResponse(appName: String, intent: String): JSONObject? {
        val payload = """{"app": "$appName", "intent": "${intent.replace("\"", "\\\"")}"}"""
        return postRequest("$baseUrl/intent-response", payload)
    }

    fun sendNotificationRaw(jsonPayload: String): JSONObject? {
        return postRequest("$baseUrl/notification", jsonPayload)
    }

    fun getNotifications(): JSONObject? {
        var connection: HttpURLConnection? = null
        try {
            val url = URL("$baseUrl/get-notifications")
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                return JSONObject(response)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection?.disconnect()
        }
        return null
    }

    private fun postRequest(urlString: String, jsonPayload: String): JSONObject? {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json; utf-8")
            connection.setRequestProperty("Accept", "application/json")
            connection.doOutput = true

            OutputStreamWriter(connection.outputStream).use { os ->
                os.write(jsonPayload)
                os.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                return JSONObject(response)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection?.disconnect()
        }
        return null
    }
}
