package com.noads.filemanager.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.io.File

class FileBrowserViewModel : ViewModel() {

    var currentDirectory by mutableStateOf(
        File("/storage/emulated/0")
    )

    var files by mutableStateOf<List<File>>(
        emptyList()
    )

    init {
        refresh()
    }

    fun refresh() {

        files =
            currentDirectory.listFiles()
                ?.sortedBy { it.name.lowercase() }
                ?: emptyList()
    }

    fun open(file: File) {

        if (file.isDirectory) {

            currentDirectory = file

            refresh()
        }
    }

    fun back() {

        currentDirectory.parentFile?.let {

            currentDirectory = it

            refresh()
        }
    }
}