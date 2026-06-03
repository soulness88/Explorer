package com.noads.filemanager.ui

import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.noads.filemanager.viewmodel.FileBrowserViewModel
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.*
import java.io.File
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.IntSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.FilledIconButton
import androidx.compose.ui.unit.coerceIn
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.IconButtonDefaults


@Composable
fun FileBrowserScreen(
    viewModel: FileBrowserViewModel
) {

    var selectedFile by remember { mutableStateOf<File?>(null) }
    var showMenu by remember { mutableStateOf(false) }

    var showRenameDialog by remember { mutableStateOf(false) }

    var showCreateFolderDialog by remember {
        mutableStateOf(false)
    }

    var newFolderName by remember {
        mutableStateOf("")
    }

    var renameText by remember {
        mutableStateOf("")
    }

    var menuPosition by remember {
        mutableStateOf(Offset.Zero)
    }

    val windowInfo = LocalWindowInfo.current
    val windowSize: IntSize = windowInfo.containerSize

    val density = LocalDensity.current

    val screenWidthDp = with(density) {
        windowSize.width.toDp()
    }
    val screenHeightDp = with(density) {
        windowSize.height.toDp()
    }

    val toolbarIconSize =
        (screenWidthDp / 18)
            .coerceIn(22.dp, 32.dp)

    val iconSize =
        (screenWidthDp / 14)
            .coerceIn(24.dp, 40.dp)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {

            // 🧭 BOTONERA (SIEMPRE VISIBLE)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilledIconButton(
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    onClick = {

                        newFolderName = ""
                        showCreateFolderDialog = true
                    }
                ) {
                    Icon(
                        Icons.Default.CreateNewFolder,
                        contentDescription = "Nueva Carpeta",
                        modifier = Modifier.size(toolbarIconSize)
                    )
                }
                FilledIconButton(
                    onClick = { viewModel.back() }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        modifier = Modifier.size(toolbarIconSize)
                    )
                }
                FilledIconButton(onClick = { viewModel.home() }
                ) {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "Inicio",
                        modifier = Modifier.size(toolbarIconSize)
                    )
                }

                FilledIconButton(onClick = { viewModel.forward() }
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Adelante",
                        modifier = Modifier.size(toolbarIconSize)
                    )
                }

                FilledIconButton(
                    onClick = { viewModel.paste() },
                    enabled = viewModel.clipboard != null
                ) {
                    Icon(
                        Icons.Default.ContentPaste,
                        contentDescription = "Pegar",
                        modifier = Modifier.size(toolbarIconSize)
                    )
                }
            }

            // RUTA SUPERIOR
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {

                        Text(
                            text = "Ubicación",
                            style = MaterialTheme.typography.labelMedium
                        )

                        Spacer(
                            modifier = Modifier.height(4.dp)
                        )

                        Text(
                            text = viewModel.currentDirectory.absolutePath
                        )
                    }
                }
            }

            viewModel.clipboardStatus?.let { status ->

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = status,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            if (viewModel.selectedFiles.isNotEmpty()) {

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "${viewModel.selectedFiles.size} seleccionado(s)",
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            val estimatedMenuWidthDp = 180
            val estimatedMenuHeightDp = 220

            val menuOffset = with(density) {

                var x = menuPosition.x.toDp()
                var y = menuPosition.y.toDp()

                // Evitar que se salga por la derecha
                if (x + estimatedMenuWidthDp.dp > screenWidthDp) {
                    x -= estimatedMenuWidthDp.dp
                }

                // Evitar que se salga por abajo
                if (y + estimatedMenuHeightDp.dp > screenHeightDp) {
                    y -= estimatedMenuHeightDp.dp
                }

                // Evitar coordenadas negativas
                if (x < 0.dp) x = 0.dp
                if (y < 0.dp) y = 0.dp

                DpOffset(x, y)
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                offset = menuOffset
            ) {
                DropdownMenuItem(
                    text = { Text("Renombrar") },
                    onClick = {

                        renameText = selectedFile?.name ?: ""

                        showMenu = false
                        showRenameDialog = true
                    }
                )
                DropdownMenuItem(
                    text = { Text("Copiar") },
                    onClick = {
                        selectedFile?.let { viewModel.copy(it) }
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Cortar") },
                    onClick = {
                        selectedFile?.let { viewModel.cut(it) }
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Eliminar") },
                    onClick = {

                        if (viewModel.selectedFiles.isNotEmpty()) {
                            viewModel.deleteSelection()
                        } else {
                            selectedFile?.let {
                                viewModel.delete(it)
                            }
                        }

                        showMenu = false
                    }
                )
            }

            // LISTA
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    bottom = 16.dp
                )
            ) {
                items(viewModel.files) { file ->
                    var itemPosition by remember {
                        mutableStateOf(Offset.Zero)
                    }
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = 4.dp,
                                vertical = 2.dp
                            )
                    ) {
                        ListItem(
                            colors = ListItemDefaults.colors(
                                containerColor =
                                    if (viewModel.selectedFiles.contains(file))
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                                    else
                                        MaterialTheme.colorScheme.surface
                            ),
                            leadingContent = {

                                Icon(
                                    imageVector =
                                        if (file.isDirectory)
                                            Icons.Default.Folder
                                        else
                                            Icons.Default.Description,

                                    contentDescription = null,

                                    tint =
                                        if (file.isDirectory)
                                            MaterialTheme.colorScheme.primary
                                        else
                                            MaterialTheme.colorScheme.secondary,

                                    modifier = Modifier.size(iconSize)
                                )
                            },
                            headlineContent = {
                                Text(
                                    text =
                                        if (viewModel.selectedFiles.contains(file))
                                            "✓ ${file.name}"
                                        else
                                            file.name
                                )
                            },
                            supportingContent = {
                                Text(
                                    if (file.isDirectory)
                                        "Carpeta"
                                    else
                                        "Archivo"
                                )
                            },
                            modifier = Modifier
                                .onGloballyPositioned { coordinates ->
                                    itemPosition = coordinates.positionInWindow()
                                }
                                .pointerInput(file) {
                                    detectTapGestures(
                                        onTap = {

                                            // Si estás en modo selección → no abrir
                                            if (viewModel.selectedFiles.isNotEmpty()) {
                                                viewModel.toggleSelection(file)
                                            } else {
                                                viewModel.open(file)
                                            }
                                        },
                                        onDoubleTap = {

                                            // ACTIVA SELECCIÓN MULTIPLE
                                            viewModel.toggleSelection(file)
                                        },
                                        onLongPress = { offset ->
                                            // MENÚ CONTEXTUAL (igual que antes)
                                            selectedFile = file

                                            menuPosition = Offset(
                                                itemPosition.x + offset.x,
                                                itemPosition.y + offset.y
                                            )
                                            showMenu = true
                                        }
                                    )
                                }
                        )
                    }
                }
            }

            if (showRenameDialog) {
                AlertDialog(
                    onDismissRequest = { showRenameDialog = false },
                    title = { Text("Renombrar") },
                    text = {
                        OutlinedTextField(
                            value = renameText,
                            onValueChange = { renameText = it },
                            singleLine = true
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            selectedFile?.let { file ->
                                viewModel.rename(
                                    file,
                                    renameText
                                )
                            }
                            showRenameDialog = false
                        }) {
                            Text("Aceptar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showRenameDialog = false
                        }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            if (showCreateFolderDialog) {

                AlertDialog(

                    onDismissRequest = {
                        showCreateFolderDialog = false
                    },

                    title = {
                        Text("Nueva carpeta")
                    },

                    text = {

                        OutlinedTextField(
                            value = newFolderName,
                            onValueChange = {
                                newFolderName = it
                            },
                            singleLine = true
                        )
                    },

                    confirmButton = {

                        TextButton(
                            onClick = {

                                viewModel.createFolder(
                                    newFolderName
                                )

                                newFolderName = ""

                                showCreateFolderDialog = false
                            }
                        ) {
                            Text("Crear")
                        }
                    },

                    dismissButton = {

                        TextButton(
                            onClick = {
                                newFolderName = ""
                                showCreateFolderDialog = false
                            }
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            if (viewModel.files.isEmpty()) {
                Text("Carpeta vacía o sin acceso")
            }
        }

        viewModel.lastError?.let { error ->

            AlertDialog(

                onDismissRequest = {
                    viewModel.clearError()
                },

                title = {
                    Text("Error")
                },

                text = {
                    Text(error)
                },

                confirmButton = {

                    TextButton(
                        onClick = {
                            viewModel.clearError()
                        }
                    ) {
                        Text("Aceptar")
                    }
                }
            )
        }
    }
}