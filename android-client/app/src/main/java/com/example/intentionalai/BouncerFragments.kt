package com.example.intentionalai

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment

class DashboardFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)
        return view
    }
}

class ChatFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        
        val btnSend = view.findViewById<android.widget.ImageButton>(R.id.btnSend)
        val etMessage = view.findViewById<android.widget.EditText>(R.id.etMessage)
        
        btnSend.setOnClickListener {
            val msg = etMessage.text.toString()
            if (msg.isNotEmpty()) {
                (activity as? MainActivity)?.sendChatMessage(msg)
                etMessage.setText("")
            }
        }
        return view
    }
}

class ShieldFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_shield, container, false)
        view.findViewById<Button>(R.id.btnTestShield).setOnClickListener {
            (activity as? MainActivity)?.triggerIntentCheck("Simulated Social App")
        }
        return view
    }
}

class HistoryFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_history, container, false)
        view.findViewById<Button>(R.id.btnRefreshHistory).setOnClickListener {
            (activity as? MainActivity)?.fetchMissedMessages()
        }
        return view
    }
}
