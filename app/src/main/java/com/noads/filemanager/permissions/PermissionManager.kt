package com.noads.filemanager.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat

object PermissionManager {

    fun hasPermission(
        context: Context
    ): Boolean {

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            Environment.isExternalStorageManager()

        } else {

            val readGranted =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED

            val writeGranted =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED

            readGranted && writeGranted
        }
    }

    fun requestPermission(
        context: Context
    ) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

            val intent = Intent(
                Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
            )

            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
            )

            context.startActivity(intent)
        }
    }
}