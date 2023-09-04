package com.example.to_do.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    abstract var bindingInflationFunction : (LayoutInflater, ViewGroup?, Boolean) -> VB
    private lateinit var _binding : VB
    protected lateinit var binding : VB
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = bindingInflationFunction(layoutInflater , container , false)
        binding = _binding
        setup()
        addCallBacks()
        return _binding.root
    }
    abstract fun setup()
    abstract fun addCallBacks()
}