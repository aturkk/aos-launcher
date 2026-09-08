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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.aos.core.domain.model.Profile
import com.aos.core.domain.model.ProfileType

@Composable
fun ProfileManagementSection(
    profiles: List<Profile>,
    onSelectActiveProfile: (Long) -> Unit,
    onUpdateProfile: (Profile) -> Unit,
    modifier: Modifier = Modifier
) {
    var editingProfile by remember { mutableStateOf<Profile?>(null) }
    var pendingProfileToActivate by remember { mutableStateOf<Profile?>(null) }
    var pinDialogInput by remember { mutableStateOf("") }
    var pinDialogError by remember { mutableStateOf(false) }

    val activeProfile = profiles.firstOrNull { it.isActive }

    fun requestProfileSwitch(targetProfile: Profile) {
        if (targetProfile.id == activeProfile?.id) return
        if (activeProfile != null && !activeProfile.pinCode.isNullOrBlank()) {
            pendingProfileToActivate = targetProfile
            pinDialogInput = ""
            pinDialogError = false
        } else {
            onSelectActiveProfile(targetProfile.id)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Mod & Profil Yönetimi",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Farklı ortamlara (İş, Odak, Çocuk vb.) göre uygulama erişimlerini ve bildirimleri özelleştirin.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        profiles.forEach { profile ->
            ProfileItemRow(
                profile = profile,
                onActivate = { requestProfileSwitch(profile) },
                onEdit = { editingProfile = profile }
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // PIN Challenge Dialog for switching away from PIN-protected profile
    pendingProfileToActivate?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingProfileToActivate = null },
            title = { Text("Ebeveyn / Profil PIN Doğrulaması") },
            text = {
                Column {
                    Text(
                        "${activeProfile?.name ?: "Mevcut profil"} modundan çıkmak için lütfen PIN kodunuzu girin.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = pinDialogInput,
                        onValueChange = {
                            pinDialogInput = it
                            pinDialogError = false
                        },
                        label = { Text("PIN Kodu") },
                        isError = pinDialogError,
                        supportingText = if (pinDialogError) {
                            { Text("Hatalı PIN kodu", color = MaterialTheme.colorScheme.error) }
                        } else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (pinDialogInput == activeProfile?.pinCode) {
                            onSelectActiveProfile(target.id)
                            pendingProfileToActivate = null
                        } else {
                            pinDialogError = true
                        }
                    }
                ) {
                    Text("Onayla")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingProfileToActivate = null }) {
                    Text("İptal")
                }
            }
        )
    }

    // Profile Edit Dialog
    editingProfile?.let { profile ->
        ProfileEditDialog(
            profile = profile,
            onDismiss = { editingProfile = null },
            onSave = { updated ->
                onUpdateProfile(updated)
                editingProfile = null
            }
        )
    }
}

@Composable
private fun ProfileItemRow(
    profile: Profile,
    onActivate: () -> Unit,
    onEdit: () -> Unit
) {
    val isSelected = profile.isActive

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onActivate)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getProfileIcon(profile.type),
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                if (isSelected) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Aktif",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }

            val subtitle = when (profile.type) {
                ProfileType.Kids -> "PIN Korumalı | Sadece izinli uygulamalar"
                ProfileType.Work, ProfileType.Focus -> "${profile.blockedPackages.size} sınırlandırılmış uygulama"
                ProfileType.Night -> "Mavi ışık azaltma ve koyu tema"
                ProfileType.Car -> "Büyük butonlar ve güvenli sürüş"
                ProfileType.Normal -> "Varsayılan başlatıcı deneyimi"
            }

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Düzenle",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ProfileEditDialog(
    profile: Profile,
    onDismiss: () -> Unit,
    onSave: (Profile) -> Unit
) {
    var name by remember { mutableStateOf(profile.name) }
    var pinCode by remember { mutableStateOf(profile.pinCode ?: "") }
    var isScheduleEnabled by remember { mutableStateOf(profile.isScheduleEnabled) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("${profile.name} Yapılandırması") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Profil Adı") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (profile.type == ProfileType.Kids) {
                    Spacer(modifier = Modifier.height(14.dp))
                    OutlinedTextField(
                        value = pinCode,
                        onValueChange = { if (it.length <= 6) pinCode = it },
                        label = { Text("Ebeveyn PIN Kodu (4-6 hane)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Otomatik Zamanlama", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Belirlenen saatlerde bu profile otomatik geçiş yap",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = isScheduleEnabled,
                        onCheckedChange = { isScheduleEnabled = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        profile.copy(
                            name = name,
                            pinCode = if (profile.type == ProfileType.Kids) pinCode else profile.pinCode,
                            isScheduleEnabled = isScheduleEnabled
                        )
                    )
                }
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

private fun getProfileIcon(type: ProfileType): ImageVector = when (type) {
    ProfileType.Normal -> Icons.Default.Home
    ProfileType.Work -> Icons.Default.Work
    ProfileType.Focus -> Icons.Default.CenterFocusStrong
    ProfileType.Night -> Icons.Default.Bedtime
    ProfileType.Kids -> Icons.Default.ChildCare
    ProfileType.Car -> Icons.Default.DirectionsCar
}
