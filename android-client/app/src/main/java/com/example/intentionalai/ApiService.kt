package com.example.intentionalai

import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class ApiService(private val baseUrl: String) {

    fun checkIntent(appName: String): JSONObject? {
        return postRequest("$baseUrl/plugins/webhooks/intent", """{"action": "create_flow", "app": "$appName"}""")
    }

    fun sendIntentResponse(appName: String, intent: String): JSONObject? {
        val payload = """{"action": "resume_flow", "app": "$appName", "intent": "${intent.replace("\"", "\\\"")}"}"""
        return postRequest("$baseUrl/plugins/webhooks/intent", payload)
    }

    fun sendNotificationRaw(jsonPayload: String): JSONObject? {
        // Wrap payload for OpenClaw webhook action
        val wrapped = """{"action": "create_flow", "data": $jsonPayload}"""
        return postRequest("$baseUrl/plugins/webhooks/notification", wrapped)
    }

    fun getNotifications(): JSONObject? {
        return getRequest("$baseUrl/plugins/webhooks/notification?action=list_flows")
    }

    fun getSummary(): JSONObject? {
        return getRequest("$baseUrl/plugins/webhooks/intent?action=get_task_summary")
    }

    fun sendChat(message: String): JSONObject? {
        return postRequest("$baseUrl/plugins/webhooks/intent", """{"action": "create_flow", "goal": "${message.replace("\"", "\\\"")}"}""")
    }

    private fun getRequest(urlString: String): JSONObject? {
        var connection: HttpURLConnection? = null
        try {
            val url = URL(urlString)
            connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("x-openclaw-webhook-secret", "bouncer-secret-123")

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
            connection.setRequestProperty("x-openclaw-webhook-secret", "bouncer-secret-123")
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
