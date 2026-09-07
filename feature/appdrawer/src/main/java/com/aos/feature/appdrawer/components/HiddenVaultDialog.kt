package com.aos.feature.appdrawer.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.aos.core.domain.model.AppInfo
import com.aos.core.ui.components.AosAppIcon
import kotlinx.coroutines.launch

@Composable
fun HiddenVaultDialog(
    isPinSet: Boolean,
    isUnlocked: Boolean,
    hiddenApps: List<AppInfo>,
    onDismiss: () -> Unit,
    onSetPin: (String) -> Unit,
    onVerifyPin: suspend (String) -> Boolean,
    onUnhideApp: (String) -> Unit,
    onLockVault: () -> Unit,
    onAppClick: (packageName: String, activityName: String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var enteredPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var isSettingNewPin by remember { mutableStateOf(!isPinSet) }
    var firstEnteredPin by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = {
            onLockVault()
            onDismiss()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = {
                        onLockVault()
                        onDismiss()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color.White
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gizli Kasa",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    if (isUnlocked) {
                        IconButton(onClick = {
                            onLockVault()
                            enteredPin = ""
                        }) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Kilitle",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(48.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (!isUnlocked) {
                    // PIN Entry / Setup View
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val promptTitle = when {
                            isSettingNewPin && firstEnteredPin == null -> "Yeni 4 Haneli PIN Belirleyin"
                            isSettingNewPin && firstEnteredPin != null -> "PIN Kodunuzu Tekrar Edin"
                            else -> "Kasa PIN Kodunu Girin"
                        }

                        val promptDesc = when {
                            isSettingNewPin && firstEnteredPin == null -> "Gizlenen uygulamaları korumak için bir PIN seçin"
                            isSettingNewPin && firstEnteredPin != null -> "Doğrulama için lütfen aynı PIN'i girin"
                            else -> "Kasayı açmak ve uygulamaları görmek için PIN girin"
                        }

                        Text(
                            text = promptTitle,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = promptDesc,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(28.dp))

                        // PIN Dot Indicators
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            repeat(4) { index ->
                                val isFilled = index < enteredPin.length
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isFilled) MaterialTheme.colorScheme.primary
                                            else Color.White.copy(alpha = 0.2f)
                                        )
                                )
                            }
                        }

                        pinError?.let { err ->
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = err,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Numeric Keypad (1..9, Backspace, 0)
                        NumericKeypad(
                            onDigitClick = { digit ->
                                if (enteredPin.length < 4) {
                                    val newPin = enteredPin + digit
                                    enteredPin = newPin
                                    pinError = null

                                    if (newPin.length == 4) {
                                        if (isSettingNewPin) {
                                            if (firstEnteredPin == null) {
                                                firstEnteredPin = newPin
                                                enteredPin = ""
                                            } else {
                                                if (firstEnteredPin == newPin) {
                                                    onSetPin(newPin)
                                                    isSettingNewPin = false
                                                    firstEnteredPin = null
                                                } else {
                                                    pinError = "PIN kodları eşleşmedi! Tekrar deneyin."
                                                    enteredPin = ""
                                                    firstEnteredPin = null
                                                }
                                            }
                                        } else {
                                            coroutineScope.launch {
                                                val verified = onVerifyPin(newPin)
                                                if (!verified) {
                                                    pinError = "Hatalı PIN! Lütfen tekrar deneyin."
                                                    enteredPin = ""
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            onBackspace = {
                                if (enteredPin.isNotEmpty()) {
                                    enteredPin = enteredPin.dropLast(1)
                                    pinError = null
                                }
                            }
                        )
                    }
                } else {
                    // Vault Unlocked Content
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Gizlenen Uygulamalar (${hiddenApps.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "PIN Değiştir",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.clickable {
                                    isSettingNewPin = true
                                    firstEnteredPin = null
                                    enteredPin = ""
                                    onLockVault()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (hiddenApps.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        imageVector = Icons.Default.LockOpen,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = "Kasa Boş",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Uygulama çekmecesinde herhangi bir uygulamaya basılı tutarak 'Uygulamayı Gizle (Kasa)' seçeneğini kullanabilirsiniz.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(hiddenApps, key = { it.packageName }) { app ->
                                    HiddenAppRow(
                                        app = app,
                                        onLaunch = { onAppClick(app.packageName, app.activityName) },
                                        onUnhide = { onUnhideApp(app.packageName) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HiddenAppRow(
    app: AppInfo,
    onLaunch: () -> Unit,
    onUnhide: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .clickable { onLaunch() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AosAppIcon(
            label = app.label,
            packageName = app.packageName,
            activityName = app.activityName,
            onClick = onLaunch
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = app.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = app.packageName,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
        }

        OutlinedButton(
            onClick = onUnhide,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(
                imageVector = Icons.Default.Visibility,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Görünür Yap", fontSize = 12.sp)
        }
    }
}

@Composable
private fun NumericKeypad(
    onDigitClick: (String) -> Unit,
    onBackspace: () -> Unit
) {
    val keypadRows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("", "0", "DEL")
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        keypadRows.forEach { row ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                row.forEach { item ->
                    when (item) {
                        "" -> {
                            Spacer(modifier = Modifier.size(68.dp))
                        }
                        "DEL" -> {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .clickable { onBackspace() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Backspace,
                                    contentDescription = "Geri Al",
                                    tint = Color.White
                                )
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.08f))
                                    .clickable { onDigitClick(item) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item,
                                    color = Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
