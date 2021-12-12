package com.example.aasha

import android.text.Editable
import android.text.TextWatcher
import android.widget.ImageButton

class ButtonObserver(private val button: ImageButton) : TextWatcher {

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        if (p0.toString().trim().isNotEmpty()) {
            button.isEnabled = true
            button.setImageResource(R.drawable.ic_message_send_red)
        } else {
            button.isEnabled = false
            button.setImageResource(R.drawable.ic_message_send)
        }
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
    override fun afterTextChanged(p0: Editable?) {}
}