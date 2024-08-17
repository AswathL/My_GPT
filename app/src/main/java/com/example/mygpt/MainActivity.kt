package com.example.mygpt

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.internal.ViewUtils.hideKeyboard
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val question = findViewById<EditText>(R.id.etQuestion)
        val submit = findViewById<Button>(R.id.btnSubmit)
        val responseTextView = findViewById<TextView>(R.id.txtResponse)


        submit.setOnClickListener {
            val qst = question.text.toString()
            Toast.makeText(this,"Submitted", Toast.LENGTH_SHORT).show()
            hideKeyboard()
            question.setText("")
            getResponse(qst) { response ->
                runOnUiThread {
                    responseTextView.text = response
                }
            }
        }
    }
    private fun hideKeyboard() {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }

    private fun getResponse(qst: String, callback: (String) -> Unit) {
        val apiKey = "" //Add your api key here
        val url = "https://api.openai.com/v1/chat/completions"
        val requestBody = """
            {
                "model": "gpt-3.5-turbo",
                "messages": [{"role": "user", "content": "$qst"}],
                "max_tokens": 512
            }
        """.trimIndent()
        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("Error", "API call failed", e)
                callback("API call failed")
            }

            override fun onResponse(call: Call, response: Response) {
                Log.e("res","${response.code}")
                val body = response.body?.string()
                if (body != null) {
                    try {
                        Log.d("Response", body)
                        val jsonObject = JSONObject(body)
                        val choices = jsonObject.getJSONArray("choices")
                        val textResult = choices.getJSONObject(0).getJSONObject("message").getString("content")
                        callback(textResult)
                    } catch (e: Exception) {
                        Log.e("Error", "JSON parsing error", e)
                        callback("JSON parsing error")
                    }
                } else {
                    Log.d("Response", "empty")
                    callback("Empty response")
                }
            }
        })
    }
}
