package com.aos.feature.appdrawer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import com.aos.feature.appdrawer.components.HiddenVaultDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aos.core.domain.model.AppInfo
import com.aos.core.domain.model.ProfileType
import com.aos.core.ui.components.AosAppIcon
import com.aos.feature.appdrawer.components.AppDrawerContextMenu
import com.aos.feature.appdrawer.components.SuggestedAppsRow
import kotlinx.coroutines.launch

@Composable
fun AppDrawerScreen(
    viewModel: AppDrawerViewModel,
    onAppClick: (packageName: String, activityName: String) -> Unit,
    onAddToHomeScreen: (AppInfo) -> Unit,
    onAppInfo: (packageName: String) -> Unit,
    onUninstall: (packageName: String) -> Unit,
    onClose: () -> Unit,
    onOpenSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val gridState = rememberLazyGridState()
    val coroutineScope = rememberCoroutineScope()
    var isVaultOpen by remember { mutableStateOf(false) }

    // Distinct alphabet letters present in apps
    val alphabet = remember(uiState.filteredApps) {
        uiState.filteredApps
            .mapNotNull { it.label.firstOrNull()?.uppercaseChar() }
            .distinct()
            .sorted()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Uygulama ara...", color = Color.Gray) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Ara",
                        tint = Color.White
                    )
                },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onOpenSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Başlatıcı Ayarları",
                                tint = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        IconButton(onClick = { isVaultOpen = true }) {
                            Icon(
                                imageVector = if (uiState.isVaultUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = "Gizli Kasa",
                                tint = if (uiState.hiddenApps.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f)
                            )
                        }
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Kapat",
                                tint = Color.White
                            )
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.extraLarge,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White.copy(alpha = 0.15f),
                    unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            uiState.activeProfile?.let { prof ->
                if (prof.type != ProfileType.Normal) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Aktif Mod: ${prof.name} (Filtrelenmiş Liste)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }

            // AI Suggested Apps Shelf
            if (uiState.suggestedApps.isNotEmpty() && uiState.searchQuery.isBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                SuggestedAppsRow(
                    suggestions = uiState.suggestedApps,
                    onAppClick = onAppClick
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Apps Grid with Side Alphabet Bar
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(4),
                        contentPadding = PaddingValues(vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        itemsIndexed(
                            items = uiState.filteredApps,
                            key = { _, app -> app.packageName + app.activityName }
                        ) { _, app ->
                            var isContextMenuVisible by remember { mutableStateOf(false) }

                            Box(contentAlignment = Alignment.Center) {
                                AosAppIcon(
                                    label = app.label,
                                    packageName = app.packageName,
                                    activityName = app.activityName,
                                    onClick = { onAppClick(app.packageName, app.activityName) },
                                    onLongClick = { isContextMenuVisible = true }
                                )

                                AppDrawerContextMenu(
                                    expanded = isContextMenuVisible,
                                    onDismissRequest = { isContextMenuVisible = false },
                                    onAddToHomeScreen = { onAddToHomeScreen(app) },
                                    onAppInfo = { onAppInfo(app.packageName) },
                                    onHideApp = { viewModel.hideApp(app.packageName) },
                                    onUninstall = { onUninstall(app.packageName) }
                                )
                            }
                        }
                    }

                    // A..Z Fast-Scroll Alphabet Sidebar
                    if (alphabet.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(start = 6.dp, end = 2.dp)
                                .align(Alignment.CenterVertically),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            alphabet.forEach { letter ->
                                Text(
                                    text = letter.toString(),
                                    color = Color.White.copy(alpha = 0.65f),
                                    fontSize = 11.sp,
                                    modifier = Modifier
                                        .clickable {
                                            val targetIndex = uiState.filteredApps.indexOfFirst {
                                                it.label.firstOrNull()?.uppercaseChar() == letter
                                            }
                                            if (targetIndex >= 0) {
                                                coroutineScope.launch {
                                                    gridState.scrollToItem(targetIndex)
                                                }
                                            }
                                        }
                                        .padding(vertical = 1.5.dp, horizontal = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (isVaultOpen) {
        HiddenVaultDialog(
            isPinSet = uiState.isVaultPinSet,
            isUnlocked = uiState.isVaultUnlocked,
            hiddenApps = uiState.hiddenApps,
            onDismiss = { isVaultOpen = false },
            onSetPin = { pin -> viewModel.setVaultPin(pin) },
            onVerifyPin = { pin -> viewModel.verifyVaultPin(pin) },
            onUnhideApp = { pkg -> viewModel.unhideApp(pkg) },
            onLockVault = { viewModel.lockVault() },
            onAppClick = onAppClick
        )
    }
}

