package com.aos.feature.appdrawer.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Composable
fun AppDrawerContextMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onAddToHomeScreen: () -> Unit,
    onAppInfo: () -> Unit,
    onHideApp: () -> Unit,
    onUninstall: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = DpOffset(0.dp, 8.dp)
    ) {
        DropdownMenuItem(
            text = { Text("Ana Ekrana Ekle") },
            onClick = {
                onDismissRequest()
                onAddToHomeScreen()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Ana Ekrana Ekle",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        )

        DropdownMenuItem(
            text = { Text("Uygulamayı Gizle (Kasa)") },
            onClick = {
                onDismissRequest()
                onHideApp()
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.VisibilityOff,
                    contentDescription = "Uygulamayı Gizle",
                    tint = MaterialTheme.colorScheme.tertiary
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
