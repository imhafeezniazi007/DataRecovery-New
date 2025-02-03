package com.example.datarecovery.views.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import com.akexorcist.localizationactivity.core.LocalizationActivityDelegate
import com.akexorcist.localizationactivity.core.OnLocaleChangedListener
import com.example.datarecovery.R
import com.example.datarecovery.base.BaseActivity
import com.example.datarecovery.databinding.ActivityLanguageBinding
import com.example.datarecovery.interfaces.DataListener
import com.example.datarecovery.utils.AppPreferences
import com.example.datarecovery.views.adapters.LanguagesAdapter

class LanguageActivity : BaseActivity(), OnLocaleChangedListener {
    lateinit var binding: ActivityLanguageBinding
    private val mlocalizationDelegate = LocalizationActivityDelegate(this)

    var position =6
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLanguageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setmTheme()
        mlocalizationDelegate.addOnLocaleChangedListener(this)

        initViews()
        binding.btn.setOnClickListener {

            if(position != -1) {
                if (position == 1) {
                    setLanguage("af")
                } else if (position == 2) {
                    setLanguage("ar")
                } else if (position == 3) {
                    setLanguage("zh")
                } else if (position == 4) {
                    setLanguage("da")
                } else if (position == 5) {
                    setLanguage("nl")
                } else if (position == 6) {
                    setLanguage("en")
                } else if (position == 7) {
                    setLanguage("fr")
                } else if (position == 8) {
                    setLanguage("de")
                } else if (position == 9) {
                    setLanguage("hi")
                } else if (position == 10) {
                    setLanguage("it")
                } else if (position == 11) {
                    setLanguage("ja")
                } else if (position == 12) {
                    setLanguage("ko")
                } else if (position == 13) {
                    setLanguage("ms")
                } else if (position == 14) {
                    setLanguage("no")
                } else if (position == 15) {
                    setLanguage("pt")
                } else if (position == 16) {
                    setLanguage("ru")
                } else if (position == 17) {
                    setLanguage("es")
                } else if (position == 18) {
                    setLanguage("sv")
                } else if (position == 19) {
                    setLanguage("th")
                } else if (position == 20) {
                    setLanguage("tr")
                } else if (position == 21) {
                    setLanguage("uk")
                } else if (position == 22) {
                    setLanguage("vi")
                }
                startActivity(Intent(this@LanguageActivity,HowToUseActivity::class.java))

/*                Handler().postDelayed({
                                      startActivity(Intent(this@LanguageActivity,HowToUseActivity::class.java))
                    finish()
                },1000)*/
            }else{
                Toast.makeText(this@LanguageActivity,getString(R.string.please_select_language),Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun initViews() {
        val languages = ArrayList<String>()
        languages.add("Afrikaans")
        languages.add("Arabic")
        languages.add("Chinese")
        languages.add("Danish")
        languages.add("Dutch")
        languages.add("English")
        languages.add("French")
        languages.add("German")
        languages.add("Hindi")
        languages.add("Italian")
        languages.add("Japanese")
        languages.add("Korean")
        languages.add("Malay")
        languages.add("Norwegian")
        languages.add("Portuguese")
        languages.add("Russian")
        languages.add("Spanish")
        languages.add("Swedish")
        languages.add("Thai")
        languages.add("Turkish")
        languages.add("Ukrainian")
        languages.add("Vietnamese")

        var adapter = LanguagesAdapter(this,languages,object :DataListener{
            override fun onRecieve(any: Any) {
                 position = any as  Int
                position += 1;


            }
        })
        binding.recylerview.adapter = adapter
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

    override fun onBackPressed() {
        exitDialog()
    }
    fun exitDialog() {
        var imageDialog: AlertDialog? = null
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@LanguageActivity)
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
        if (!this@LanguageActivity.isFinishing) {
            imageDialog.show()
        }

    }


}