package com.example.datarecovery.views.adapters

import android.content.Context
import com.example.datarecovery.interfaces.DataListener
import com.example.datarecovery.models.FilesModel

abstract class MyAbstractItem(val context: Context,
                              private var list: ArrayList<FilesModel>,
                              val dataListener: DataListener
) {
    abstract val type: Int

    companion object {
        const val GRID_TYPE = 0
        const val LIST_TYPE = 1
    }
}