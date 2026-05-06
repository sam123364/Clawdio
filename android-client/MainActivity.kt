package com.example.intentionalai

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val apiService = ApiService("http://10.0.2.2:3000") // 10.0.2.2 points to localhost from Android emulator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // For simplicity, we just use a basic code-generated layout here, 
        // but you should use a real XML layout in a full app.
        setContentView(R.layout.activity_main)

        // Check if we have usage stats permission
        if (!UsageTracker.hasUsageStatsPermission(this)) {
            val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
            startActivity(intent)
        }

        // Demo trigger button
        findViewById<Button>(R.id.btnSimulateInstagram).setOnClickListener {
            triggerIntentCheck("Instagram")
        }

        // Get Summary button
        findViewById<Button>(R.id.btnGetSummary).setOnClickListener {
            fetchMissedMessages()
        }
    }

    private fun fetchMissedMessages() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getNotifications()
                val logContent = response?.getString("content") ?: "No missed messages yet! 🦞"
                
                withContext(Dispatchers.Main) {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Missed Messages")
                        .setMessage(logContent)
                        .setPositiveButton("Close", null)
                        .show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun triggerIntentCheck(appName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Check intent from backend
                val checkResponse = apiService.checkIntent(appName)
                val message = checkResponse?.getString("message") ?: "Why are you opening $appName?"

                withContext(Dispatchers.Main) {
                    showIntentDialog(appName, message)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showIntentDialog(appName: String, question: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Intent Check")
        builder.setMessage(question)

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("Submit") { _, _ ->
            val userIntent = input.text.toString()
            evaluateUserIntent(appName, userIntent)
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun evaluateUserIntent(appName: String, intent: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 2. Send the intent response to backend
                val response = apiService.sendIntentResponse(appName, intent)
                val decision = response?.getString("decision")
                val message = response?.getString("message")

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "$decision: $message", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
