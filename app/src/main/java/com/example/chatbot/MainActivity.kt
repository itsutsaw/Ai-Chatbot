package com.example.chatbot

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val openAIKey = "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" // Replace with your OpenAI API key

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val userInput = findViewById<EditText>(R.id.userInput)
        val sendButton = findViewById<Button>(R.id.sendButton)
        val chatLayout = findViewById<LinearLayout>(R.id.chatLayout)

        sendButton.setOnClickListener {
            val message = userInput.text.toString().trim()
            if (message.isNotEmpty()) {
                addMessageToChat("You: $message", chatLayout)
                fetchAIResponse(message) { response ->
                    runOnUiThread {
                        addMessageToChat("Bot: $response", chatLayout)
                    }
                }
                userInput.setText("")
            }
        }
    }

    private fun addMessageToChat(message: String, chatLayout: LinearLayout) {
        val textView = TextView(this).apply {
            text = message
            textSize = 16f // Increase font size if needed
            setPadding(16, 8, 16, 8) // Padding for better readability
        }
        chatLayout.addView(textView)
    }

    private fun fetchAIResponse(message: String, callback: (String) -> Unit) {
        val client = OkHttpClient()
        val jsonObject = JSONObject().apply {
            put("model", "gpt-3.5-turbo")  // Correct model name
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", message)  // Use the message as user input
                })
            })
            put("max_tokens", 100)
            put("temperature", 0.7)
        }

        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaType(),
            jsonObject.toString()
        )

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")  // Correct endpoint for chat models
            .post(body)
            .addHeader("Authorization", "Bearer $openAIKey")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback("Failed to fetch response: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val responseJson = JSONObject(responseBody)
                    val botResponse = responseJson
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                        .trim()
                    callback(botResponse)
                } else {
                    callback("Error: ${response.code}")
                }
            }
        })
    }
}
