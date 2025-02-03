package com.example.datarecovery.views.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.viewpager.widget.ViewPager
import com.example.datarecovery.MainActivity
import com.example.datarecovery.R
import com.example.datarecovery.base.BaseActivity
import com.example.datarecovery.databinding.ActivityAboutUsBinding
import com.example.datarecovery.databinding.ActivityAllDuplicateBinding
import com.example.datarecovery.databinding.ActivityHowToUseBinding
import com.example.datarecovery.utils.AppPreferences
import com.example.datarecovery.views.adapters.ViewPagerAdapter
import com.example.datarecovery.views.fragments.*
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator

class HowToUseActivity : BaseActivity(), ViewPager.OnPageChangeListener {
    var pos = 0
    lateinit var binding: ActivityHowToUseBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHowToUseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setmTheme()
        val dotsIndicator = findViewById<DotsIndicator>(R.id.dots_indicator)
        val viewPager = findViewById<ViewPager>(R.id.viewPager)
        val adapter= ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(Step1Fragment(),getString(R.string.images))
        adapter.addFragment(Step2Fragment(),getString(R.string.videos))
        adapter.addFragment(Step3Fragment(),getString(R.string.audios))
        adapter.addFragment(Step4Fragment(),getString(R.string.contacts))
        binding.viewPager.adapter=adapter
        dotsIndicator.setViewPager(viewPager)

        viewPager.addOnPageChangeListener(this)


        findViewById<TextView>(R.id.doneTV).setOnClickListener {
            viewPager.setCurrentItem(pos+1,true)
            if(pos == 3){
                AppPreferences.getInstance(this@HowToUseActivity).setFirstTimeUser(false)
                startActivity(Intent(this@HowToUseActivity,ProActivity::class.java))
                finish()
            }
        }
        findViewById<TextView>(R.id.prevTV).setOnClickListener {
            viewPager.setCurrentItem(pos-1,true)
        }
    }

    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    override fun onPageSelected(position: Int) {
        pos = position
        Log.d("TAG", "onPageSelected: $position")
        if(position==3){
            findViewById<TextView>(R.id.doneTV).text = getString(R.string.done)
        }else if(position == 0){
            findViewById<TextView>(R.id.prevTV).visibility = View.GONE
            findViewById<TextView>(R.id.doneTV).text = getString(R.string.next)
        }else{
            findViewById<TextView>(R.id.prevTV).visibility = View.VISIBLE
            findViewById<TextView>(R.id.doneTV).text = getString(R.string.next)
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
    }

    fun setmTheme(){
        var mtheme = AppPreferences.getInstance(this).getTheme
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

    override fun onBackPressed() {
        exitDialog()
    }
    fun exitDialog() {
        var imageDialog: AlertDialog? = null
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@HowToUseActivity)
        val inflater = layoutInflater
        val view: View = inflater.inflate(R.layout.exit_dialog, null)
        builder.setView(view)
        imageDialog = builder.create()
        val tapAgainLayout = view.findViewById<ConstraintLayout>(R.id.tapAgainLayout)


        tapAgainLayout.setOnClickListener {
            finishAffinity()
        }
        imageDialog.window!!
            .setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )

        val window: Window = imageDialog.window!!
        val wlp: WindowManager.LayoutParams = window.attributes

        wlp.gravity = Gravity.BOTTOM
        wlp.flags = wlp.flags and WindowManager.LayoutParams.FLAG_DIM_BEHIND.inv()
        window.attributes = wlp
        if (!this@HowToUseActivity.isFinishing) {
            imageDialog.show()
        }

    }

}