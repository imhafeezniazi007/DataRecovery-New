package com.example.datarecovery.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.datarecovery.R
import com.example.datarecovery.databinding.FragmentStep2Binding
import com.example.datarecovery.databinding.FragmentStep3Binding
import com.example.datarecovery.databinding.FragmentStep4Binding
import com.example.datarecovery.utils.AppPreferences

class Step4Fragment : Fragment() {
    lateinit var binding: FragmentStep4Binding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentStep4Binding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setmTheme()
    }
    fun setmTheme(){
        var mtheme = AppPreferences.getInstance(requireContext()).getTheme
        if(mtheme == 0){
            binding.mainLayout.background = resources.getDrawable(R.drawable.light_theme_bg)
        }else if(mtheme == 1){
            binding.mainLayout.background = resources.getDrawable(R.drawable.dark_theme_bg)
        }else if(mtheme == 2){
            binding.mainLayout.background = resources.getDrawable(R.drawable.theme1_bg)
        }else if(mtheme == 3){
            binding.mainLayout.background = resources.getDrawable(R.drawable.theme2_bg)
        }else if(mtheme == 4){
            binding.mainLayout.background = resources.getDrawable(R.drawable.theme3)
        }else if(mtheme == 5){
            binding.mainLayout.background = resources.getDrawable(R.drawable.theme4)
        }else if(mtheme == 6){
            binding.mainLayout.background = resources.getDrawable(R.drawable.theme5)
        }
    }
}