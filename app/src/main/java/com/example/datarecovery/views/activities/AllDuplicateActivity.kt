package com.example.datarecovery.views.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.datarecovery.R
import com.example.datarecovery.base.BaseActivity
import com.example.datarecovery.databinding.ActivityAllDuplicateBinding
import com.example.datarecovery.databinding.ActivityDuplicateBinding
import com.example.datarecovery.interfaces.DataListener
import com.example.datarecovery.models.ContactModel
import com.example.datarecovery.models.Duplicate
import com.example.datarecovery.models.DuplicateFile
import com.example.datarecovery.utils.AppPreferences
import com.example.datarecovery.views.adapters.DuplicateParentAdapter
import com.example.datarecovery.views.adapters.ViewPagerAdapter
import com.example.datarecovery.views.fragments.AudiosFragment
import com.example.datarecovery.views.fragments.ContactsFragment
import com.example.datarecovery.views.fragments.ImagesFragment
import com.example.datarecovery.views.fragments.VideosFragment
import kotlinx.coroutines.*

class AllDuplicateActivity : BaseActivity() {
    lateinit var binding: ActivityAllDuplicateBinding
    var duplicateImages = ArrayList<Duplicate>()
    var deletedImages =ArrayList<DuplicateFile>()
    var duplicateAudios = ArrayList<Duplicate>()
    var deletedAudios =ArrayList<DuplicateFile>()
    var duplicateVideos = ArrayList<Duplicate>()
    var deletedVideos =ArrayList<DuplicateFile>()
    var duplicateContacts = ArrayList<ArrayList<ContactModel>?>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllDuplicateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showHideProgress(true,object :DataListener{
            override fun onRecieve(any: Any) {
                if(any as Boolean){
                    finish()
                }
            }

        })
        CoroutineScope(Dispatchers.IO).launch {
             duplicateImages =getDuplicateImages(this@AllDuplicateActivity)
            if (duplicateImages.isNotEmpty()) {
                duplicateImages.withIndex()?.forEach { fileParent ->
                    fileParent.value.getDuplicateFiles().withIndex().forEach { fileChild ->
                        Log.d("TAG", "testttt: ${duplicateImages[fileParent.index].getDuplicateFiles()[fileChild.index].getFile()}")
                        Log.d("TAG", "testttt: ${duplicateImages[fileParent.index].getDuplicateFiles()[fileChild.index].isSelect}")
                        if (fileChild.index != 0) {
                            duplicateImages[fileParent.index].getDuplicateFiles()[fileChild.index].isSelect = true
                            deletedImages.add(duplicateImages[fileParent.index].getDuplicateFiles()[fileChild.index])
                        }
                    }
                }
            }
            duplicateAudios =getDuplicateAudios(this@AllDuplicateActivity)
            if (duplicateAudios.isNotEmpty()) {
                duplicateAudios.withIndex()?.forEach { fileParent ->
                    fileParent.value.getDuplicateFiles().withIndex().forEach { fileChild ->
                        if (fileChild.index != 0) {
                            duplicateAudios[fileParent.index].getDuplicateFiles()[fileChild.index].isSelect = true
                            deletedAudios.add(duplicateAudios[fileParent.index].getDuplicateFiles()[fileChild.index])
                        }
                    }
                }
            }
            duplicateVideos =getAllDuplicateVideos(this@AllDuplicateActivity)
            if (duplicateVideos.isNotEmpty()) {
                duplicateVideos.withIndex().forEach { fileParent ->
                    fileParent.value.getDuplicateFiles().withIndex().forEach { fileChild ->
                        if (fileChild.index != 0) {
                            duplicateVideos[fileParent.index].getDuplicateFiles()[fileChild.index].isSelect = true
                            deletedVideos.add(duplicateVideos[fileParent.index].getDuplicateFiles()[fileChild.index])
                        }
                    }
                }
            }
            duplicateContacts =getDuplicateContacts(this@AllDuplicateActivity)
           withContext(Dispatchers.Main){
               val adapter= ViewPagerAdapter(supportFragmentManager)
               adapter.addFragment(ImagesFragment(),getString(R.string.images))
               adapter.addFragment(VideosFragment(),getString(R.string.videos))
               adapter.addFragment(AudiosFragment(),getString(R.string.audios))
               adapter.addFragment(ContactsFragment(),getString(R.string.contacts))
               binding.viewPager.adapter=adapter
               binding.tbLayout.setupWithViewPager(binding.viewPager)
               showHideProgress(false,object :DataListener{
                   override fun onRecieve(any: Any) {
                       if(any as Boolean){
                           finish()
                       }
                   }

               })
           }
        }
     clickListener()
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