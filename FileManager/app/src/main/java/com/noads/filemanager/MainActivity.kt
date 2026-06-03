package com.noads.filemanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.noads.filemanager.ui.FileBrowserScreen
import com.noads.filemanager.viewmodel.FileBrowserViewModel
import com.noads.filemanager.ui.theme.FileManagerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FileManagerTheme {
                val vm: FileBrowserViewModel = viewModel()
                FileBrowserScreen(
                    viewModel = vm
                )
            }
        }
    }
}