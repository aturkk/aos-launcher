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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import com.aos.feature.appdrawer.components.HiddenVaultDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.text.style.TextAlign
import com.aos.core.ui.theme.getIconShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
    onStartPlaceApp: (AppInfo) -> Unit = onAddToHomeScreen,
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
    var selectedCategory by remember { mutableStateOf(com.aos.core.domain.model.AppCategory.All) }
    var contextMenuApp by remember { mutableStateOf<AppInfo?>(null) }

    val displayedApps = remember(uiState.filteredApps, selectedCategory, uiState.searchQuery) {
        if (uiState.searchQuery.isNotBlank() || selectedCategory == com.aos.core.domain.model.AppCategory.All) {
            uiState.filteredApps
        } else {
            uiState.filteredApps.filter { it.category == selectedCategory }
        }
    }

    // Distinct alphabet letters present in apps
    val alphabet = remember(displayedApps) {
        displayedApps
            .mapNotNull { it.label.firstOrNull()?.uppercaseChar() }
            .distinct()
            .sorted()
    }

    val categoryCounts = remember(uiState.filteredApps) {
        uiState.filteredApps.groupBy { it.category }.mapValues { it.value.size }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.90f))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Smart Launcher Signature Left Vertical Category Rail (visible when not searching)
            if (uiState.searchQuery.isBlank()) {
                com.aos.feature.appdrawer.components.VerticalCategoryRail(
                    selectedCategory = selectedCategory,
                    onSelectCategory = { selectedCategory = it },
                    categoryCounts = categoryCounts
                )
            }

            // Main Content Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 8.dp)
            ) {
                // Search Bar Composable definition
                val searchBarContent = @Composable {
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
                }

                // Top Search Bar (if not configured at bottom)
                if (!uiState.searchBarAtBottom) {
                    Spacer(modifier = Modifier.height(12.dp))
                    searchBarContent()
                    Spacer(modifier = Modifier.height(8.dp))
                }

                uiState.activeProfile?.let { prof ->
                    if (prof.type != ProfileType.Normal) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Aktif Mod: ${prof.name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                // Universal Smart Search Results (Math, Contacts, Web & AI Chips)
                com.aos.feature.appdrawer.components.SmartSearchResults(
                    searchQuery = uiState.searchQuery,
                    mathResult = uiState.mathResult,
                    contactResults = uiState.contactResults,
                    aiChipsEnabled = uiState.aiSearchChipsEnabled
                )

                // Category Title Header (Smart Launcher style)
                if (uiState.searchQuery.isBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = selectedCategory.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "${displayedApps.size} uygulama",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }

                // AI Suggested Apps Shelf
                if (uiState.suggestedApps.isNotEmpty() && uiState.searchQuery.isBlank() && selectedCategory == com.aos.core.domain.model.AppCategory.All) {
                    SuggestedAppsRow(
                        suggestions = uiState.suggestedApps,
                        onAppClick = onAppClick,
                        iconPackPackage = uiState.selectedIconPackPackage
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Main Apps Grid with Side Alphabet Bar
                if (uiState.isLoading) {
                    Box(modifier = Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                } else if (uiState.errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = uiState.errorMessage ?: "Uygulamalar yüklenemedi",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else if (displayedApps.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            if (uiState.searchQuery.isNotBlank()) {
                                Text(
                                    text = "\"${uiState.searchQuery}\" için sonuç bulunamadı",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                TextButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                    Text("Aramayı Temizle", color = MaterialTheme.colorScheme.primary)
                                }
                            } else if (selectedCategory != com.aos.core.domain.model.AppCategory.All) {
                                Text(
                                    text = "${selectedCategory.title} kategorisinde uygulama bulunamadı",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                TextButton(onClick = { selectedCategory = com.aos.core.domain.model.AppCategory.All }) {
                                    Text("Tüm Uygulamaları Göster", color = MaterialTheme.colorScheme.primary)
                                }
                            } else {
                                Text(
                                    text = "Yüklü uygulama bulunamadı",
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                        LazyVerticalGrid(
                            state = gridState,
                            columns = GridCells.Fixed(4),
                            contentPadding = PaddingValues(vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            itemsIndexed(
                                items = displayedApps,
                                key = { _, app -> app.packageName + app.activityName }
                            ) { _, app ->
                                Box {
                                    AosAppIcon(
                                        label = app.label,
                                        packageName = app.packageName,
                                        activityName = app.activityName,
                                        iconPackPackage = uiState.selectedIconPackPackage,
                                        shape = getIconShape(uiState.iconShape),
                                        onClick = { onAppClick(app.packageName, app.activityName) },
                                        onLongClick = { contextMenuApp = app }
                                    )

                                    if (contextMenuApp == app) {
                                        AppDrawerContextMenu(
                                            expanded = true,
                                            onDismissRequest = { contextMenuApp = null },
                                            onAddToHomeScreen = {
                                                val target = app
                                                contextMenuApp = null
                                                onStartPlaceApp(target)
                                            },
                                            onAppInfo = {
                                                val pkg = app.packageName
                                                contextMenuApp = null
                                                onAppInfo(pkg)
                                            },
                                            onHideApp = {
                                                val pkg = app.packageName
                                                contextMenuApp = null
                                                viewModel.hideApp(pkg)
                                            },
                                            onUninstall = {
                                                val pkg = app.packageName
                                                contextMenuApp = null
                                                onUninstall(pkg)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // A..Z Fast-Scroll Alphabet Sidebar
                        if (alphabet.isNotEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(start = 2.dp, end = 2.dp)
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
                                                val targetIndex = displayedApps.indexOfFirst {
                                                    it.label.firstOrNull()?.uppercaseChar() == letter
                                                }
                                                if (targetIndex >= 0) {
                                                    coroutineScope.launch {
                                                        gridState.scrollToItem(targetIndex)
                                                    }
                                                }
                                            }
                                            .padding(vertical = 1.dp, horizontal = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Search Bar (if configured at bottom for Thumb Zone)
                if (uiState.searchBarAtBottom) {
                    Spacer(modifier = Modifier.height(8.dp))
                    searchBarContent()
                    Spacer(modifier = Modifier.height(10.dp))
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

