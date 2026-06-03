package com.noads.filemanager.data

import java.io.File

object FileOperations {

    fun rename(
        file: File,
        newName: String
    ): Boolean {

        return file.renameTo(
            File(
                file.parent,
                newName
            )
        )
    }

    fun delete(
        file: File
    ): Boolean {

        return file.deleteRecursively()
    }

    fun copy(
        source: File,
        destination: File
    ) {

        source.copyTo(
            File(destination, source.name),
            overwrite = true
        )
    }

    fun move(
        source: File,
        destination: File
    ) {

        copy(source, destination)

        source.deleteRecursively()
    }
}