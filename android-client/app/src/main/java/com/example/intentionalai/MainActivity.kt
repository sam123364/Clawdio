package com.example.intentionalai

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val apiService = ApiService("http://192.168.0.125:18791")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupTabs()
        checkPermissions()
        startBouncerService()
        refreshDashboard()

        // Handle auto-trigger from background service
        val triggerApp = intent.getStringExtra("trigger_app")
        if (triggerApp != null) {
            triggerIntentCheck(triggerApp)
        }
    }

    private fun setupTabs() {
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val viewPager = findViewById<ViewPager2>(R.id.viewPager)

        viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int = 4
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> DashboardFragment()
                    1 -> ChatFragment()
                    2 -> ShieldFragment()
                    else -> HistoryFragment()
                }
            }
        }

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Dashboard"
                1 -> "Chat"
                2 -> "Shield"
                else -> "History"
            }
        }.attach()
    }

    fun sendChatMessage(message: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // OpenClaw Native Webhook format: { "ok": true, "result": { "reply": "..." } }
                val response = apiService.sendChat(message)
                val result = response?.optJSONObject("result")
                val reply = result?.optString("reply") ?: "Bouncer is thinking..."
                
                withContext(Dispatchers.Main) {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Bouncer 🛡️")
                        .setMessage(reply)
                        .setPositiveButton("Understood", null)
                        .show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Brain Sync Failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun refreshDashboard() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getSummary()
                val score = 100 - (response?.getInt("vaguePercentage") ?: 0)
                withContext(Dispatchers.Main) {
                    // This is a simple way to update fragments. In a real app, use a ViewModel.
                    val dashboard = supportFragmentManager.findFragmentByTag("f0") as? DashboardFragment
                    dashboard?.view?.findViewById<android.widget.TextView>(R.id.tvScore)?.text = "$score%"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun checkPermissions() {
        if (!UsageTracker.hasUsageStatsPermission(this)) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
    }

    private fun startBouncerService() {
        val serviceIntent = Intent(this, BouncerService::class.java)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }

    fun fetchMissedMessages() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.getNotifications()
                val summary = response?.getString("summary") ?: "No missed messages! 🛡️"
                withContext(Dispatchers.Main) {
                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("Daily Summary")
                        .setMessage(summary)
                        .setPositiveButton("Dismiss", null)
                        .show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Connection Error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun triggerIntentCheck(appName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.checkIntent(appName)
                val result = response?.optJSONObject("result")
                val message = result?.optString("message") ?: "Bouncer: Why are you opening $appName?"
                withContext(Dispatchers.Main) {
                    showIntentDialog(appName, message)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showIntentDialog(appName: String, question: String) {
        val input = EditText(this)
        input.hint = "Be honest..."
        AlertDialog.Builder(this)
            .setTitle("🛡️ BOUNCER INTERCEPT")
            .setMessage(question)
            .setView(input)
            .setCancelable(false) // Freeze the screen
            .setPositiveButton("Submit to AI") { _, _ ->
                val userIntent = input.text.toString()
                evaluateUserIntent(appName, userIntent)
            }
            .setNegativeButton("Leave") { _, _ ->
                goHome()
            }
            .show()
    }

    private fun evaluateUserIntent(appName: String, intent: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = apiService.sendIntentResponse(appName, intent)
                val result = response?.optJSONObject("result")
                val decision = result?.optString("decision") ?: "redirect"
                val message = result?.optString("message") ?: "Mindless usage detected."
                
                withContext(Dispatchers.Main) {
                    if (decision == "redirect") {
                        AlertDialog.Builder(this@MainActivity)
                            .setTitle("🛡️ ACCESS DENIED")
                            .setMessage(message)
                            .setCancelable(false)
                            .setPositiveButton("Go Back") { _, _ -> goHome() }
                            .show()
                    } else {
                        Toast.makeText(this@MainActivity, "✅ Access Granted: $message", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun goHome() {
        val startMain = Intent(Intent.ACTION_MAIN)
        startMain.addCategory(Intent.CATEGORY_HOME)
        startMain.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
    }
}
