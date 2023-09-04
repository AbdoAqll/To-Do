package com.example.to_do.ui.get_started

import android.content.Intent
import android.view.LayoutInflater
import com.example.to_do.MainActivity
import com.example.to_do.R
import com.example.to_do.base.BaseActivity
import com.example.to_do.databinding.ActivityGetStatedBinding

class GetStated : BaseActivity<ActivityGetStatedBinding>() {
    override val bindingInflationFunction: (LayoutInflater) -> ActivityGetStatedBinding = ActivityGetStatedBinding::inflate

    override fun setup() {
        val window = this.window
        window.statusBarColor = resources.getColor(R.color.card_color)
    }

    override fun addCallbacks() {
        binding.getStarted.setOnClickListener {
            val intent = Intent(this , MainActivity :: class.java)
            startActivity(intent)
            finish()
        }
    }
}
