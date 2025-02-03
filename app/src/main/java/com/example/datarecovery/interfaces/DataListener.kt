package com.example.datarecovery.interfaces

interface DataListener {

    fun onRecieve(any: Any)
    fun onClick(any: Any){}
    fun onClickWatchAd(any: Any){}
    fun onPremium(any: Any){}
}