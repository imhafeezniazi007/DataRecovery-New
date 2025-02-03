package com.example.datarecovery.models

import java.io.Serializable

class Duplicate(duplicateFiles: ArrayList<DuplicateFile>) : Serializable {
    private var duplicateFiles: ArrayList<DuplicateFile>
    fun getDuplicateFiles(): ArrayList<DuplicateFile> {
        return duplicateFiles
    }

    fun setDuplicateFiles(duplicateFiles: ArrayList<DuplicateFile>) {
        this.duplicateFiles = duplicateFiles
    }

    override fun toString(): String {
        return """
            {duplicateFiles=$duplicateFiles}
            
            """.trimIndent()
    }

    init {
        this.duplicateFiles = duplicateFiles
    }
}