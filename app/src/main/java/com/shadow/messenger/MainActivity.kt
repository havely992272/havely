package com.shadow.messenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        val titleText = findViewById<TextView>(R.id.titleText)
        val messageInput = findViewById<EditText>(R.id.messageInput)
        val sendButton = findViewById<Button>(R.id.sendButton)
        
        titleText.text = "Shadow Messenger v1.0"
        
        sendButton.setOnClickListener {
            val message = messageInput.text.toString()
            if (message.isNotEmpty()) {
                // Здесь будет логика отправки сообщения
                titleText.text = "Sent: $message"
                messageInput.text.clear()
            }
        }
    }
}
