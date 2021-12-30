package com.example.aasha

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.aasha.databinding.ActivityFullImageBinding

class FullImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityFullImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // binding.photoView.setImageResource()
    }
}