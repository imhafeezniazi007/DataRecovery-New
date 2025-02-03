package com.example.datarecovery.models

sealed class RecylerViewModel {
    data class Item(
        val type: Int
    ) : RecylerViewModel()

    data class More(
        val type: Int
    ) : RecylerViewModel()
}
