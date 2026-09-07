package com.aos.launcher

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LauncherApps
import android.net.Uri
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.speech.RecognizerIntent
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.aos.core.common.util.LauncherSystemActions
import com.aos.core.domain.model.AssistantResult
import com.aos.core.domain.model.CommandType
import com.aos.core.domain.model.DarkModeOption
import com.aos.core.domain.model.SearchEngineOption
import com.aos.core.data.diagnostic.AosCrashReporter
import com.aos.core.ui.theme.AOSTheme
import com.aos.feature.appdrawer.AppDrawerScreen
import com.aos.feature.appdrawer.AppDrawerViewModel
import com.aos.feature.home.HomeScreen
import com.aos.feature.home.HomeViewModel
import com.aos.feature.home.widget.LauncherWidgetHost
import com.aos.feature.settings.SettingsScreen
import com.aos.feature.settings.SettingsViewModel
import com.aos.launcher.service.AosNotificationListenerService
import com.aos.launcher.ui.onboarding.OnboardingScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LauncherActivity : ComponentActivity() {

    private lateinit var widgetHost: LauncherWidgetHost
    private lateinit var appWidgetManager: AppWidgetManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Offline Crash Reporter
        AosCrashReporter.init(applicationContext)

        // Initialize Widget Host & Manager (L002 & L008)
        widgetHost = LauncherWidgetHost(applicationContext)
        appWidgetManager = AppWidgetManager.getInstance(applicationContext)

        // L006: System Wallpaper show flag
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER,
            WindowManager.LayoutParams.FLAG_SHOW_WALLPAPER
        )

        enableEdgeToEdge()

        setContent {
            val homeViewModel: HomeViewModel = hiltViewModel()
            val homeUiState by homeViewModel.uiState.collectAsStateWithLifecycle()
            val themeConfig = homeUiState.userPreferences.themeConfig
            val notificationCounts by AosNotificationListenerService.notificationCounts.collectAsStateWithLifecycle()

            val isDark = when (themeConfig.darkMode) {
                DarkModeOption.System -> isSystemInDarkTheme()
                DarkModeOption.Light -> false
                DarkModeOption.Dark -> true
            }

            AOSTheme(
                darkTheme = isDark,
                dynamicColor = themeConfig.useDynamicColors
            ) {
                if (!homeUiState.userPreferences.isOnboardingCompleted) {
                    OnboardingScreen(
                        onCompleteOnboarding = {
                            homeViewModel.setOnboardingCompleted(true)
                        }
                    )
                } else {
                    var isAppDrawerOpen by remember { mutableStateOf(false) }
                    var isSettingsOpen by remember { mutableStateOf(false) }

                    val appDrawerViewModel: AppDrawerViewModel = hiltViewModel()
                    val settingsViewModel: SettingsViewModel = hiltViewModel()

                    // L001: Back press override - Do not exit launcher, return to home
                    BackHandler(enabled = true) {
                        when {
                            isSettingsOpen -> isSettingsOpen = false
                            isAppDrawerOpen -> isAppDrawerOpen = false
                            else -> {
                                // Already on home screen, do nothing
                            }
                        }
                    }

                Box(modifier = Modifier.fillMaxSize()) {
                    // Main Home Screen
                    HomeScreen(
                        viewModel = homeViewModel,
                        notificationCounts = notificationCounts,
                        onOpenAppDrawer = { isAppDrawerOpen = true },
                        onOpenNotifications = {
                            LauncherSystemActions.expandNotificationShade(this@LauncherActivity)
                        },
                        onDoubleTapSleep = {
                            LauncherSystemActions.lockDevice(this@LauncherActivity)
                        },
                        onOpenSettings = { isSettingsOpen = true },
                        onOpenSearch = { engine ->
                            val queryUrl = when (engine) {
                                SearchEngineOption.Google -> "https://www.google.com"
                                SearchEngineOption.DuckDuckGo -> "https://duckduckgo.com"
                                SearchEngineOption.Bing -> "https://www.bing.com"
                            }
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(queryUrl))
                                startActivity(intent)
                            } catch (e: Exception) {
                                Toast.makeText(this@LauncherActivity, "Tarayıcı açılamadı", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onOpenVoiceSearch = {
                            try {
                                val voiceIntent = Intent(RecognizerIntent.ACTION_WEB_SEARCH)
                                startActivity(voiceIntent)
                            } catch (e: Exception) {
                                Toast.makeText(this@LauncherActivity, "Sesli arama başlatılamadı", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onExecuteAssistantResult = { result ->
                            when (result.type) {
                                CommandType.OpenApp -> {
                                    appDrawerViewModel.recordAppLaunch(result.payload)
                                    launchApplication(result.payload, "")
                                }
                                CommandType.SwitchProfile -> {
                                    val profileId = result.payload.toLongOrNull() ?: 1L
                                    homeViewModel.switchProfile(profileId)
                                }
                                CommandType.Search -> {
                                    val engine = homeUiState.userPreferences.themeConfig.searchEngine
                                    val baseUrl = when (engine) {
                                        SearchEngineOption.Google -> "https://www.google.com/search?q="
                                        SearchEngineOption.DuckDuckGo -> "https://duckduckgo.com/?q="
                                        SearchEngineOption.Bing -> "https://www.bing.com/search?q="
                                    }
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(baseUrl + Uri.encode(result.payload)))
                                        startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(this@LauncherActivity, "Tarayıcı açılamadı", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                CommandType.LockScreen -> {
                                    LauncherSystemActions.lockDevice(this@LauncherActivity)
                                }
                                CommandType.OpenSettings -> {
                                    isSettingsOpen = true
                                }
                                CommandType.Unrecognized -> {
                                    Toast.makeText(this@LauncherActivity, result.message, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onAppClick = { pkg, activity ->
                            appDrawerViewModel.recordAppLaunch(pkg)
                            launchApplication(pkg, activity)
                        },
                        onAppInfo = { pkg -> openAppInfo(pkg) },
                        onUninstall = { pkg -> uninstallApp(pkg) }
                    )

                    // App Drawer Overlay
                    AnimatedVisibility(
                        visible = isAppDrawerOpen,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        AppDrawerScreen(
                            viewModel = appDrawerViewModel,
                            onAppClick = { pkg, activity ->
                                if (homeViewModel.isAppBlockedByActiveProfile(pkg)) {
                                    val profile = homeUiState.activeProfile
                                    Toast.makeText(
                                        this@LauncherActivity,
                                        "${profile?.name ?: "Aktif mod"}: Bu uygulama sınırlandırılmıştır.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    isAppDrawerOpen = false
                                    appDrawerViewModel.recordAppLaunch(pkg)
                                    launchApplication(pkg, activity)
                                }
                            },
                            onAddToHomeScreen = { app ->
                                homeViewModel.findFirstEmptyCellAndAddApp(app)
                                Toast.makeText(this@LauncherActivity, "${app.label} ana ekrana eklendi", Toast.LENGTH_SHORT).show()
                            },
                            onAppInfo = { pkg -> openAppInfo(pkg) },
                            onUninstall = { pkg -> uninstallApp(pkg) },
                            onClose = { isAppDrawerOpen = false },
                            onOpenSettings = {
                                isAppDrawerOpen = false
                                isSettingsOpen = true
                            }
                        )
                    }

                    // Settings Screen Overlay
                    AnimatedVisibility(
                        visible = isSettingsOpen,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                    ) {
                        SettingsScreen(
                            viewModel = settingsViewModel,
                            onBack = { isSettingsOpen = false }
                        )
                    }
                }
            }
        }
    }
}

    // L002: AppWidgetHost lifecycle management to prevent memory leaks
    override fun onStart() {
        super.onStart()
        widgetHost.startListening()
    }

    override fun onStop() {
        super.onStop()
        widgetHost.stopListening()
    }

    private fun launchApplication(packageName: String, activityName: String) {
        try {
            val launcherApps = getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
            val userHandle = Process.myUserHandle()

            if (activityName.isNotEmpty()) {
                val componentName = ComponentName(packageName, activityName)
                launcherApps.startMainActivity(componentName, userHandle, null, null)
            } else {
                val intent = packageManager.getLaunchIntentForPackage(packageName)
                if (intent != null) {
                    startActivity(intent)
                } else {
                    Toast.makeText(this, "Uygulama başlatılamadı: $packageName", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Hata: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openAppInfo(packageName: String) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Uygulama bilgisi açılamadı", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uninstallApp(packageName: String) {
        try {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Kaldırma başlatılamadı", Toast.LENGTH_SHORT).show()
        }
    }
}
