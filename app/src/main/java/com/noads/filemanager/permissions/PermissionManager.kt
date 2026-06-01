package com.noads.filemanager.permissions

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.provider.Settings

object PermissionManager {

    fun hasPermission(): Boolean {

        return Environment.isExternalStorageManager()
    }

    fun requestPermission(
        context: Context
    ) {

        val intent =
            Intent(
                Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            )

        context.startActivity(intent)
    }
}