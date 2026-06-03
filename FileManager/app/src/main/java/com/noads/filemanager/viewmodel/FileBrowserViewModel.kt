package com.noads.filemanager.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.io.File
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FileBrowserViewModel : ViewModel() {
    private fun isRootFolder(file: File): Boolean {

        return file.isDirectory &&
                file.parentFile?.absolutePath ==
                "/storage/emulated/0"
    }

    private val home = File("/storage/emulated/0")

    var currentDirectory by mutableStateOf(home)
        private set

    var files by mutableStateOf<List<File>>(emptyList())
        private set

    private val backStack = mutableListOf<File>()
    private val forwardStack = mutableListOf<File>()

    data class ClipboardItem(
        val files: Set<File>,
        val cut: Boolean
    )

    var clipboard by mutableStateOf<ClipboardItem?>(null)
        private set

    var clipboardStatus by mutableStateOf<String?>(null)
        private set

    var selectedFiles by mutableStateOf<Set<File>>(emptySet())
        private set

    var lastError by mutableStateOf<String?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        refresh()
    }

    fun refresh() {

        viewModelScope.launch(Dispatchers.IO) {

            isLoading = true

            try {

                val result =
                    currentDirectory
                        .listFiles()
                        ?.sortedWith(
                            compareBy<File> { !it.isDirectory }
                                .thenBy { it.name.lowercase() }
                        )
                        ?: emptyList()

                files = result

            } catch (e: Exception) {

                lastError =
                    e.message
                        ?: "Error leyendo carpeta"

                files = emptyList()

            } finally {

                isLoading = false
            }
        }
    }

    fun open(file: File) {

        if (!file.isDirectory) return

        clearSelection()

        backStack.add(currentDirectory)
        forwardStack.clear()

        currentDirectory = file

        refresh()
    }

    fun back() {

        if (backStack.isEmpty()) return

        clearSelection()

        forwardStack.add(currentDirectory)

        currentDirectory =
            backStack.removeAt(backStack.lastIndex)

        refresh()
    }

    fun forward() {

        if (forwardStack.isEmpty()) return

        clearSelection()

        backStack.add(currentDirectory)

        currentDirectory =
            forwardStack.removeAt(forwardStack.lastIndex)

        refresh()
    }

    fun home() {

        if (currentDirectory == home) return

        clearSelection()

        backStack.add(currentDirectory)
        forwardStack.clear()

        currentDirectory = home

        refresh()
    }

    fun toggleSelection(file: File) {

        selectedFiles =
            if (selectedFiles.contains(file)) {
                selectedFiles - file
            } else {
                selectedFiles + file
            }
    }

    fun clearSelection() {
        selectedFiles = emptySet()
    }

    fun clearError() {
        lastError = null
    }

    fun copy(file: File) {

        clipboard =
            if (selectedFiles.isNotEmpty()) {

                ClipboardItem(
                    files = selectedFiles,
                    cut = false
                )

            } else {

                ClipboardItem(
                    files = setOf(file),
                    cut = false
                )
            }

        clipboardStatus =
            "📋 ${clipboard!!.files.size} elemento(s) copiado(s)"

        clearSelection()
    }

    fun cut(file: File) {
        if (isRootFolder(file)) {

            lastError =
                "No se puede mover una carpeta principal"

            return
        }

        clipboard =
            if (selectedFiles.isNotEmpty()) {

                ClipboardItem(
                    files = selectedFiles,
                    cut = true
                )

            } else {

                ClipboardItem(
                    files = setOf(file),
                    cut = true
                )
            }

        clipboardStatus =
            "✂ ${clipboard!!.files.size} elemento(s) cortado(s)"

        clearSelection()
    }

    fun paste() {

        val item = clipboard ?: return

        item.files.forEach { source ->

            if (!source.exists()) return@forEach

            val target =
                File(
                    currentDirectory,
                    source.name
                )
            if (
                source.absolutePath ==
                target.absolutePath
            ) {
                return@forEach
            }

            try {

                if (source.isDirectory) {
                    if (
                        target.absolutePath.startsWith(
                            source.absolutePath
                        )
                    ) {
                        return@forEach
                    }

                    source.copyRecursively(
                        target,
                        overwrite = true
                    )

                    if (item.cut) {
                        source.deleteRecursively()
                    }

                } else {

                    source.copyTo(
                        target,
                        overwrite = true
                    )

                    if (item.cut) {
                        source.delete()
                    }
                }

            } catch (e: Exception) {

                lastError =
                    e.message
                        ?: "Error copiando ${source.name}"
            }
        }

        clipboard = null

        clipboardStatus = null

        clearSelection()

        refresh()
    }

    fun deleteSelection() {

        if (selectedFiles.isEmpty()) {
            return
        }

        selectedFiles.forEach { file ->
            if (isRootFolder(file)) {

                lastError =
                    "${file.name} es una carpeta principal"

                return@forEach
            }

            try {

                if (!file.deleteRecursively()) {

                    lastError =
                        "No se pudo eliminar ${file.name}"
                }

            } catch (e: Exception) {

                lastError =
                    e.message
                        ?: "Error eliminando ${file.name}"
            }
        }

        clearSelection()

        refresh()
    }

    fun delete(file: File) {
        if (isRootFolder(file)) {

            lastError =
                "No se puede eliminar una carpeta principal"

            return
        }

        try {

            val success =
                file.deleteRecursively()

            if (!success) {

                lastError =
                    "No se pudo eliminar ${file.name}"

                return
            }

            refresh()

        } catch (e: Exception) {

            lastError =
                e.message
                    ?: "Error eliminando archivo"
        }
    }

    fun rename(

        file: File,
        newName: String
    ) {
        if (isRootFolder(file)) {

            lastError =
                "No se puede renombrar una carpeta principal"

            return
        }

        try {

            if (newName.isBlank()) {
                lastError = "Nombre inválido"
                return
            }

            val target =
                File(
                    file.parentFile,
                    newName
                )

            if (target.exists()) {
                lastError = "Ya existe un archivo con ese nombre"
                return
            }

            if (!file.renameTo(target)) {
                lastError = "No se pudo renombrar"
                return
            }

            refresh()

        } catch (e: Exception) {

            lastError =
                e.message
                    ?: "Error renombrando archivo"
        }
    }

    fun createFolder(name: String) {

        try {

            if (name.isBlank()) {
                lastError = "Nombre inválido"
                return
            }

            val folder =
                File(
                    currentDirectory,
                    name
                )

            if (folder.exists()) {

                lastError =
                    "Ya existe un elemento con ese nombre"

                return
            }

            if (!folder.mkdir()) {

                lastError =
                    "No se pudo crear la carpeta"

                return
            }

            refresh()

        } catch (e: Exception) {

            lastError =
                e.message
                    ?: "Error creando carpeta"
        }
    }
}