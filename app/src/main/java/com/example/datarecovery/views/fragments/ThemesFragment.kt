package com.example.datarecovery.views.fragments

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.datarecovery.MainActivity
import com.example.datarecovery.R
import com.example.datarecovery.databinding.FragmentThemesBinding
import com.example.datarecovery.interfaces.DataListener
import com.example.datarecovery.utils.AppPreferences
import com.example.datarecovery.utils.Constant
import com.example.datarecovery.utils.ThemesUtil
import com.example.datarecovery.views.activities.ProActivity
import com.example.datarecovery.views.adapters.ThemesAdapterNew
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase
import java.lang.Math.abs


class ThemesFragment : Fragment() {
    lateinit var binding: FragmentThemesBinding
    lateinit var  viewPager:ViewPager2
    var mPos = -1
    private var retryAttempt = 0.0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentThemesBinding.inflate(inflater, container, false)

        return binding.root    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
          viewPager = view.findViewById<ViewPager2>(R.id.viewPager)
        val adapter= ThemesAdapterNew(requireContext())

        viewPager?.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        viewPager.adapter=adapter
        viewPager.offscreenPageLimit = 1

        val pageMargin = resources.getDimensionPixelOffset(R.dimen.pageMargin).toFloat()
        val pageOffset = resources.getDimensionPixelOffset(R.dimen.offset).toFloat()

        viewPager.setPageTransformer { page, position ->
            val myOffset: Float = position * -(2 * pageOffset + pageMargin)
            if (position < -1) {
                page.setTranslationX(-myOffset)
            } else if (position <= 1) {
                val scaleFactor =
                    Math.max(0.7f, 1 - abs(position - 0.14285715f))
                page.setTranslationX(myOffset)
                page.setScaleY(scaleFactor)
                page.setAlpha(scaleFactor)
            } else {
                page.setAlpha(0F)
                page.setTranslationX(myOffset)
            }
        }

        viewPager?.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                Log.d("TAG", "onPageSelected: $position")
                Log.d("TAG", "onPageSelected: ${Constant.unlockAllTheme}")
                mPos = position
                if(!AppPreferences.getInstance(requireContext()).isAppPurchased){
                    if(!Constant.unlockAllTheme) {
                        if (position == 0 || position == 1) {
                            binding.btn.visibility = View.VISIBLE
                            binding.btnUnlock.visibility = View.GONE
                        } else {
                            binding.btn.visibility = View.INVISIBLE
                            binding.btnUnlock.visibility = View.VISIBLE
                        }
                        if (Constant.watchedAdTheme == position) {
                            binding.btn.visibility = View.VISIBLE
                            binding.btnUnlock.visibility = View.GONE
                        }
                    }else{
                        binding.btn.visibility = View.VISIBLE
                        binding.btnUnlock.visibility = View.GONE
                    }
                }else{
                        binding.btn.visibility = View.VISIBLE
                        binding.btnUnlock.visibility = View.GONE

                }

            }

            override fun onPageScrollStateChanged(state: Int) {
                super.onPageScrollStateChanged(state)
            }

            override fun onPageScrolled(position: Int,
                                        positionOffset: Float,
                                        positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)

            }
        })

        clickListener()
    }

    private fun clickListener() {
        binding.btn.setOnClickListener{
            AppPreferences.getInstance(requireContext()).setTheme(mPos)
            ThemesUtil.changeToTheme(requireActivity() as MainActivity, mPos);
            /*if(mPos == 0) {
               AppPreferences.getInstance(requireContext()).setTheme(1)
                ThemesUtil.changeToTheme(requireActivity() as MainActivity, 1);
            }else if(mPos == 1) {
               AppPreferences.getInstance(requireContext()).setTheme(2)
                ThemesUtil.changeToTheme(requireActivity() as MainActivity, 2);
            }else if(mPos == 2) {
               AppPreferences.getInstance(requireContext()).setTheme(3)
                ThemesUtil.changeToTheme(requireActivity() as MainActivity, 3);
            }else if(mPos == 3) {
               AppPreferences.getInstance(requireContext()).setTheme(4)
                ThemesUtil.changeToTheme(requireActivity() as MainActivity, 4);
            }else if(mPos == 4) {
               AppPreferences.getInstance(requireContext()).setTheme(5)
                ThemesUtil.changeToTheme(requireActivity() as MainActivity, 5);
            }else if(mPos == 5) {
               AppPreferences.getInstance(requireContext()).setTheme(6)
                ThemesUtil.changeToTheme(requireActivity() as MainActivity, 6);
            }*/
        }
        binding.btnUnlock.setOnClickListener {
           startActivity(Intent(requireActivity(),ProActivity::class.java))

//            unlockDuplicateFileremoverAdDialog(object : DataListener {
//                override fun onRecieve(any: Any) {
//                    val watchAd = any as Boolean
//                    (requireActivity() as MainActivity).loadingAdProgress(true)
//                    if (watchAd) {
//                        Log.d("createRewardedAd", "onRecieve: ${mPos}")
//                        if(mPos == 2){
//                            createRewardedAd()
//                        }else if(mPos == 3){
//                            createRewardedAd4()
//                        }else if(mPos == 4){
//                           createRewardedAd5()
//                        } else if(mPos == 5){
//                           createRewardedAd6()
//                        } else if(mPos == 6){
//                           createRewardedAd7()
//                        }
//
//                    /*if(mPos == 3){
//                            adsId= admobRewardedThemeOneId()
//                        }else if(mPos == 4){
//                            adsId= admobRewardedThemeTwoId()
//                        }else if(mPos == 5){
//                            adsId= admobRewardedThemeThreeId()
//                        } else if(mPos == 6){
//                            adsId= admobRewardedThemeFourId()
//                        } else if(mPos == 7){
//                            adsId= admobRewardedThemeFiveId()
//                        }*/
//
//                        /*(requireActivity() as MainActivity).loadRewardedAd(adsId,object : DataListener {
//                            override fun onRecieve(any: Any) {
//                                var flag = any as Boolean
//                                Log.d("DuplicateFragment", " loadRewardedAd onRecieve: $flag")
//                                (requireActivity() as MainActivity).loadingAdProgress(false)
//                                if (flag) {
//                                    Toast.makeText(
//                                        requireContext(),
//                                        getString(R.string.something_went_wrong),
//                                        Toast.LENGTH_LONG
//                                    ).show()
//                                }
//                            }
//                            override fun onClick(any: Any) {
//                                super.onClick(any)
//                                binding.btn.visibility = View.VISIBLE
//                                binding.btnUnlock.visibility = View.GONE
//                                AppPreferences.getInstance(requireContext()).setTheme(mPos)
//                                ThemesUtil.changeToTheme(requireActivity() as MainActivity, mPos);
//                                Constant.watchedAdTheme = mPos
//
//                            }
//
//                            override fun onClickWatchAd(any: Any) {
//                                super.onClickWatchAd(any)
//                                if(any as Boolean){
//                                    (requireActivity() as MainActivity).loadingAdProgress(false)
//                                }
//                            }
//                        })*/
//
//                    } else {
//                        Toast.makeText(
//                            requireContext(),
//                            getString(R.string.something_went_wrong),
//                            Toast.LENGTH_LONG
//                        ).show()
//                    }
//                }
//
//                override fun onClick(any: Any) {
//                    super.onClick(any)
//                    if(any as Boolean){
//                        viewPager.setCurrentItem(0);
//                    }
//                }
//
//                override fun onClickWatchAd(any: Any) {
//                    super.onClickWatchAd(any)
//                    if(any as Boolean){
//                        val intent  = Intent(requireActivity(), ProActivity::class.java)
//                        intent.putExtra("from","main")
//                        startActivity(intent)
//                    }
//                }
//
//            })
        }
    }
    fun unlockDuplicateFileremoverAdDialog(dataListener: DataListener) {
        var imageDialog: AlertDialog? = null
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val view: View = inflater.inflate(R.layout.unlock_theme_dialog, null)
        builder.setView(view)
        builder.setCancelable(false)
        imageDialog = builder.create()
        imageDialog.setCancelable(false)
        val premiumLayout = view.findViewById<ConstraintLayout>(R.id.premiumLayout)
        val adIV = view.findViewById<ImageView>(R.id.adIV)
        val cancel = view.findViewById<ImageView>(R.id.cancelIV)

        adIV.setOnClickListener {
            dataListener.onRecieve(true)
            imageDialog.dismiss()
        }
        cancel.setOnClickListener {
            dataListener.onClick(true)
            imageDialog.dismiss()
        }
        premiumLayout.setOnClickListener {
            dataListener.onClickWatchAd(true)
            imageDialog.dismiss()
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
        imageDialog.show()

    }

    override fun onResume() {
        super.onResume()

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d("TAGGGGG", "onAttach: ")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("TAGGGGG", "onDetach: ")
    }

}