package com.aos.feature.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AiSettingsSection(
    isAiEnabled: Boolean,
    hasUsagePermission: Boolean,
    onAiToggle: (Boolean) -> Unit,
    onRequestPermission: () -> Unit,
    onClearData: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showClearDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Yapay Zeka & Akıllı Öneriler",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // AI Suggestions Toggle
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Akıllı Uygulama Tahmini", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "Günün saatine ve rutinlerinize göre App Drawer'da en olası uygulamaları önerir",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }
            Switch(
                checked = isAiEnabled,
                onCheckedChange = onAiToggle
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Permission Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(14.dp))
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = if (hasUsagePermission) Color(0xFF4CAF50) else Color(0xFFFF9800),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hasUsagePermission) "Kullanım Erişimi: İzin Verildi" else "Kullanım Erişimi: Gerekli",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (hasUsagePermission) Color(0xFF4CAF50) else Color(0xFFFF9800)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (hasUsagePermission) "Tüm veriler sadece cihaz içinde işlenir (0 bulut bağımlılığı)" else "Daha isabetli tahmin için sistem ayarlarından izin verin",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        color = Color.Gray
                    )
                }

                if (!hasUsagePermission) {
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedButton(onClick = onRequestPermission) {
                        Text("İzin Ver", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Reset Learning Data
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .clickable { showClearDialog = true }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.DeleteSweep,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Öğrenilen Verileri Sıfırla", style = MaterialTheme.typography.bodyMedium)
                Text("Yerel kullanım kayıtlarını temizler ve algoritmayı sıfırlar", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Verileri Sıfırla") },
            text = { Text("Tüm yerel uygulama başlatma geçmişi ve yapay zeka öğrenim verileri silinecektir. Emin misiniz?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearData()
                        showClearDialog = false
                    }
                ) {
                    Text("Sıfırla", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Vazgeç")
                }
            }
        )
    }
}
