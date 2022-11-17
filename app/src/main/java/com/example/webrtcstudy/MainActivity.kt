package com.example.webrtcstudy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.webrtcstudy.databinding.ActivityMainBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val db = Firebase.firestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }
    fun init() = with(binding){
        start.setOnClickListener {
            val roomID = roomID.text.toString()
            db.collection("calls")
                .document(roomID)
                .get()
                .addOnSuccessListener {
                    Log.e(TAG,"${it.data}")
                    if (it["type"]=="OFFER" || it["type"]=="ANSWER" || it["type"]=="END_CALL") {
                        Log.e("Rsupport","${it["type"]}")
                    } else {
                        val intent = Intent(this@MainActivity, WebRTCConnectActivity::class.java)
                        intent.putExtra("roomID",roomID)
                        intent.putExtra("isJoin",false)
                        Log.e("main","${it["type"]}")
                        startActivity(intent)
                    }
                }
                .addOnFailureListener {
                    Log.e(TAG,"${it}")
                }
        }
        join.setOnClickListener {
            val roomID = roomID.text.toString()
            val intent = Intent(this@MainActivity, WebRTCConnectActivity::class.java)
            intent.putExtra("roomID",roomID)
            intent.putExtra("isJoin",true)
            startActivity(intent)

        }
    }
    companion object{
        private const val TAG = "MainActivity"
    }
}