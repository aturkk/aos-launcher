package com.aos.feature.settings.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aos.core.common.result.Result
import com.aos.core.domain.model.SyncStatus
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BackupSyncSection(
    syncStatus: SyncStatus,
    onExportBackup: suspend (password: String) -> Result<ByteArray>,
    onImportBackup: suspend (data: ByteArray, password: String) -> Result<Unit>,
    onTriggerCloudSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var passwordInput by remember { mutableStateOf("") }
    var importDataInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.CloudSync,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Yedekleme & Senkronizasyon",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Ana ekran düzeninizi, profillerinizi ve tema ayarlarınızı AES-256 şifreleme ile güvenceye alın.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Cloud Sync Box
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.05f))
                .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Bulut Senkronizasyonu",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = when (syncStatus) {
                                is SyncStatus.Idle -> "Otomatik eşitleme beklemede"
                                is SyncStatus.Syncing -> "Bulut ile eşitleniyor..."
                                is SyncStatus.Success -> {
                                    val df = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                                    "Son senkronizasyon: ${df.format(Date(syncStatus.lastSyncTime))}"
                                }
                                is SyncStatus.Error -> "Hata: ${syncStatus.message}"
                            },
                            fontSize = 12.sp,
                            color = when (syncStatus) {
                                is SyncStatus.Success -> MaterialTheme.colorScheme.primary
                                is SyncStatus.Error -> MaterialTheme.colorScheme.error
                                else -> Color.Gray
                            }
                        )
                    }

                    Button(
                        onClick = onTriggerCloudSync,
                        enabled = syncStatus !is SyncStatus.Syncing,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (syncStatus is SyncStatus.Syncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Eşitle", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Export & Import Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedButton(
                onClick = {
                    passwordInput = ""
                    showExportDialog = true
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Upload,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Şifreli Yedek Al", fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = {
                    passwordInput = ""
                    importDataInput = ""
                    showImportDialog = true
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Geri Yükle", fontSize = 12.sp)
            }
        }
    }

    // Export Backup Dialog
    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { if (!isLoading) showExportDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Yedekleme Parolası Belirleyin") },
            text = {
                Column {
                    Text(
                        text = "Yedek dosyanız AES-256 ile şifrelenecektir. Geri yüklemek için bu parolayı unutmamanız gerekir.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Parola") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (passwordInput.length < 4) {
                            Toast.makeText(context, "Parola en az 4 karakter olmalıdır.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isLoading = true
                        coroutineScope.launch {
                            val result = onExportBackup(passwordInput)
                            isLoading = false
                            showExportDialog = false
                            if (result is Result.Success) {
                                val base64 = android.util.Base64.encodeToString(result.data, android.util.Base64.NO_WRAP)
                                clipboardManager.setText(AnnotatedString(base64))
                                Toast.makeText(
                                    context,
                                    "Yedekleme başarılı! Şifreli veri panoya kopyalandı (${result.data.size} bayt).",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else if (result is Result.Error) {
                                Toast.makeText(context, "Hata: ${result.exception.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isLoading && passwordInput.isNotBlank()
                ) {
                    Text(if (isLoading) "Şifreleniyor..." else "Yedekle & Kopyala")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showExportDialog = false },
                    enabled = !isLoading
                ) {
                    Text("İptal")
                }
            }
        )
    }

    // Import Backup Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { if (!isLoading) showImportDialog = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Restore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            title = { Text("Yedekten Geri Yükle") },
            text = {
                Column {
                    Text(
                        text = "Panoya kopyalanmış şifreli yedeği yapıştırın ve parolanızı girin.",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = importDataInput,
                        onValueChange = { importDataInput = it },
                        label = { Text("Şifreli Yedek Metni (Base64)") },
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Yedekleme Parolası") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (passwordInput.isBlank() || importDataInput.isBlank()) {
                            Toast.makeText(context, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isLoading = true
                        coroutineScope.launch {
                            try {
                                val bytes = android.util.Base64.decode(importDataInput.trim(), android.util.Base64.DEFAULT)
                                val result = onImportBackup(bytes, passwordInput)
                                isLoading = false
                                showImportDialog = false
                                if (result is Result.Success) {
                                    Toast.makeText(context, "Yedek başarıyla geri yüklendi!", Toast.LENGTH_LONG).show()
                                } else if (result is Result.Error) {
                                    Toast.makeText(context, "Geri yükleme başarısız: ${result.exception.localizedMessage}", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                Toast.makeText(context, "Geçersiz yedek verisi biçimi.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isLoading && passwordInput.isNotBlank() && importDataInput.isNotBlank()
                ) {
                    Text(if (isLoading) "Çözülüyor..." else "Geri Yükle")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showImportDialog = false },
                    enabled = !isLoading
                ) {
                    Text("İptal")
                }
            }
        )
    }
}
