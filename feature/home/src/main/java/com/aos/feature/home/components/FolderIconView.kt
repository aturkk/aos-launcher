package com.aos.feature.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aos.core.domain.model.LauncherItem
import com.aos.core.ui.components.AppIconImage
import com.aos.core.ui.theme.FolderShape
import com.aos.core.ui.theme.IconLabelShadow
import com.aos.core.ui.theme.SquircleShape

@Composable
fun FolderIconView(
    folder: LauncherItem.FolderItem,
    showLabel: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .width(72.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Folder 2x2 miniature container
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(FolderShape)
                .background(Color.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            val previewItems = folder.items.take(4)

            Column(
                verticalArrangement = Arrangement.spacedBy(3.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    previewItems.getOrNull(0)?.let {
                        AppIconImage(packageName = it.packageName, size = 20.dp)
                    }
                    previewItems.getOrNull(1)?.let {
                        AppIconImage(packageName = it.packageName, size = 20.dp)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    previewItems.getOrNull(2)?.let {
                        AppIconImage(packageName = it.packageName, size = 20.dp)
                    }
                    previewItems.getOrNull(3)?.let {
                        AppIconImage(packageName = it.packageName, size = 20.dp)
                    }
                }
            }
        }

        if (showLabel) {
            Text(
                text = folder.title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    shadow = Shadow(color = IconLabelShadow, blurRadius = 6f)
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}
