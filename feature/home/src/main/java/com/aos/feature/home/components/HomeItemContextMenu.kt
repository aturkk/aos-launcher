package com.aos.feature.home.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Composable
fun HomeItemContextMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onRemoveFromHome: () -> Unit,
    onAppInfo: () -> Unit,
    onUninstall: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = DpOffset(0.dp, 8.dp)
    ) {
        DropdownMenuItem(
            text = { Text("Ana Ekrandan Kaldır") },
            onClick = {
                onDismissRequest()
                onRemoveFromHome()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Ana Ekrandan Kaldır",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        )

        DropdownMenuItem(
            text = { Text("Uygulama Bilgisi") },
            onClick = {
                onDismissRequest()
                onAppInfo()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Uygulama Bilgisi"
                )
            }
        )

        DropdownMenuItem(
            text = { Text("Uygulamayı Kaldır") },
            onClick = {
                onDismissRequest()
                onUninstall()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = "Kaldır",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        )
    }
}
