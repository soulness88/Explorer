package com.noads.filemanager.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.noads.filemanager.viewmodel.FileBrowserViewModel

@Composable
fun FileBrowserScreen(
    viewModel: FileBrowserViewModel
) {

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Button(
            onClick = {
                viewModel.back()
            }
        ) {
            Text("Atrás")
        }

        Text(
            text = viewModel.currentDirectory.absolutePath
        )

        LazyColumn {

            items(viewModel.files) { file ->

                ListItem(

                    headlineContent = {
                        Text(file.name)
                    },

                    supportingContent = {

                        Text(
                            if (file.isDirectory)
                                "Carpeta"
                            else
                                "${file.length()} bytes"
                        )
                    },

                    modifier = Modifier.clickable {

                        if (file.isDirectory)
                            viewModel.open(file)
                    }
                )
            }
        }
    }
}