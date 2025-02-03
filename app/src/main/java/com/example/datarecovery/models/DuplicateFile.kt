package com.example.datarecovery.models

import java.io.File

class DuplicateFile(file: File, isSelect: Boolean) {
    private var file: File
    var isSelect: Boolean

    fun getFile(): File {
        return file
    }

    fun setFile(file: File) {
        this.file = file
    }

    override fun toString(): String {
        return "{" +
                "file =" + file +
                ", isSelect =" + isSelect +
                '}'
    }

    init {
        this.file = file
        this.isSelect = isSelect
    }
}