package com.aos.feature.home.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp

@Composable
fun HomeItemContextMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    hasPopupWidget: Boolean = false,
    onOpenPopupWidget: () -> Unit = {},
    onAddPopupWidget: () -> Unit = {},
    onRemovePopupWidget: () -> Unit = {},
    onRemoveFromHome: () -> Unit,
    onAppInfo: () -> Unit,
    onUninstall: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        offset = DpOffset(0.dp, 8.dp)
    ) {
        if (hasPopupWidget) {
            DropdownMenuItem(
                text = { Text("Açılır Widget'ı Göster") },
                onClick = {
                    onDismissRequest()
                    onOpenPopupWidget()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = "Açılır Widget'ı Göster",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )

            DropdownMenuItem(
                text = { Text("Açılır Widget'ı Kaldır") },
                onClick = {
                    onDismissRequest()
                    onRemovePopupWidget()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Açılır Widget'ı Kaldır",
                        tint = Color(0xFFFF5252)
                    )
                }
            )
        } else {
            DropdownMenuItem(
                text = { Text("Açılır Widget Ata") },
                onClick = {
                    onDismissRequest()
                    onAddPopupWidget()
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Widgets,
                        contentDescription = "Açılır Widget Ata",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }

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
