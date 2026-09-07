package com.aos.feature.home.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aos.core.domain.model.Profile
import com.aos.core.domain.model.ProfileType

@Composable
fun ProfileSwitcherBar(
    profiles: List<Profile>,
    activeProfile: Profile?,
    onProfileSelect: (Profile) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPinDialogForProfile by remember { mutableStateOf<Profile?>(null) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.35f))
                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                .horizontalScroll(scrollState)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            profiles.forEach { profile ->
                val isSelected = activeProfile?.id == profile.id
                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    animationSpec = tween(durationMillis = 200),
                    label = "profileBgColor"
                )
                val contentColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimary else Color.White.copy(alpha = 0.7f),
                    animationSpec = tween(durationMillis = 200),
                    label = "profileContentColor"
                )

                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(bgColor)
                        .clickable {
                            if (activeProfile?.type == ProfileType.Kids && profile.type != ProfileType.Kids) {
                                // Guard switching out of Kids mode with PIN
                                showPinDialogForProfile = profile
                                pinInput = ""
                                pinError = false
                            } else {
                                onProfileSelect(profile)
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = getProfileIcon(profile.type),
                        contentDescription = profile.name,
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = profile.name,
                        color = contentColor,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                    )
                }
            }
        }
    }

    // Kids Mode PIN Dialog
    showPinDialogForProfile?.let { targetProfile ->
        AlertDialog(
            onDismissRequest = { showPinDialogForProfile = null },
            title = { Text("Çocuk Modundan Çıkış") },
            text = {
                androidx.compose.foundation.layout.Column {
                    Text("Çocuk modunu kapatmak için lütfen ebeveyn PIN kodunu girin:")
                    Spacer(modifier = Modifier.size(12.dp))
                    OutlinedTextField(
                        value = pinInput,
                        onValueChange = {
                            pinInput = it
                            pinError = false
                        },
                        label = { Text("PIN Kodu") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        isError = pinError,
                        singleLine = true
                    )
                    if (pinError) {
                        Text(
                            text = "Hatalı PIN kodu!",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val requiredPin = activeProfile?.pinCode ?: "1234"
                        if (pinInput == requiredPin) {
                            showPinDialogForProfile = null
                            onProfileSelect(targetProfile)
                        } else {
                            pinError = true
                        }
                    }
                ) {
                    Text("Onayla")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinDialogForProfile = null }) {
                    Text("İptal")
                }
            }
        )
    }
}

fun getProfileIcon(type: ProfileType): ImageVector = when (type) {
    ProfileType.Normal -> Icons.Default.Home
    ProfileType.Work -> Icons.Default.Work
    ProfileType.Focus -> Icons.Default.CenterFocusStrong
    ProfileType.Night -> Icons.Default.Bedtime
    ProfileType.Kids -> Icons.Default.ChildCare
    ProfileType.Car -> Icons.Default.DirectionsCar
}
