package com.example.to_do.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {
    abstract val bindingInflationFunction : (LayoutInflater) -> VB
    private lateinit var _binding : VB
    protected lateinit var binding : VB
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = bindingInflationFunction(layoutInflater)
        binding = _binding
        setContentView(_binding.root)
        setup()
        addCallbacks()
    }
    abstract fun setup()
    abstract fun addCallbacks()
}