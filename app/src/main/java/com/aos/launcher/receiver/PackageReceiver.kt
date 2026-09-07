package com.aos.launcher.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aos.core.domain.repository.AppListRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PackageReceiver : BroadcastReceiver() {

    @Inject
    lateinit var appListRepository: AppListRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_PACKAGE_ADDED,
            Intent.ACTION_PACKAGE_REMOVED,
            Intent.ACTION_PACKAGE_CHANGED -> {
                scope.launch {
                    appListRepository.refreshApps()
                }
            }
        }
    }
}
