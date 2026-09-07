package com.aos.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aos.core.domain.model.LauncherItem
import com.aos.core.ui.theme.DockShape

@Composable
fun DockBar(
    dockItems: List<LauncherItem>,
    onOpenAppDrawer: () -> Unit,
    onAppClick: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
            .height(72.dp)
            .clip(DockShape)
            .background(Color.Black.copy(alpha = 0.25f)),
        contentAlignment = Alignment.Center
    ) {
        val context = androidx.compose.ui.platform.LocalContext.current

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Default Phone Shortcut
            IconButton(onClick = { com.aos.core.common.util.LauncherSystemActions.openDialer(context) }) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Telefon",
                    tint = Color.White
                )
            }

            // Default Browser Shortcut
            IconButton(onClick = { com.aos.core.common.util.LauncherSystemActions.openBrowser(context) }) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Tarayıcı",
                    tint = Color.White
                )
            }

            // Central App Drawer Opener
            IconButton(onClick = onOpenAppDrawer) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = "Uygulama Çekmecesi",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Default Messages Shortcut
            IconButton(onClick = { com.aos.core.common.util.LauncherSystemActions.openMessaging(context) }) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Mesajlar",
                    tint = Color.White
                )
            }

            // Default Camera Shortcut
            IconButton(onClick = { com.aos.core.common.util.LauncherSystemActions.openCamera(context) }) {
                Icon(
                    imageVector = Icons.Default.Camera,
                    contentDescription = "Kamera",
                    tint = Color.White
                )
            }
        }
    }
}
