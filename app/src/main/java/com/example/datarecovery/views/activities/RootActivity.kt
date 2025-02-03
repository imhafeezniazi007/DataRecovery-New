package com.example.datarecovery.views.activities

import android.os.Bundle
import android.view.View
import com.example.datarecovery.R
import com.example.datarecovery.base.BaseActivity
import com.example.datarecovery.databinding.ActivityRootBinding
import com.example.datarecovery.utils.AppPreferences

class RootActivity : BaseActivity() {
    lateinit var binding: ActivityRootBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRootBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setmTheme()
        clickListener()
        if(AppPreferences.getInstance(this).isAppPurchased){
            binding.adsLoadingTV.visibility = View.GONE
            binding.nativeAdLayout.visibility = View.GONE
            binding.border.visibility = View.GONE
            binding.border1.visibility = View.GONE
        }

    }

    private fun clickListener() {
        binding.backIV.setOnClickListener {
            onBackPressed()
        }
    }
    fun setmTheme(){
        val mtheme = AppPreferences.getInstance(this).getTheme
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