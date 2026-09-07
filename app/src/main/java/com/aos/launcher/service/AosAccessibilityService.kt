package com.aos.launcher.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.aos.core.common.util.LauncherSystemActions

class AosAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        LauncherSystemActions.accessibilityServiceInstance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // No event interception needed, service used only for global actions
    }

    override fun onInterrupt() {
        // Interrupted
    }

    override fun onDestroy() {
        super.onDestroy()
        if (LauncherSystemActions.accessibilityServiceInstance == this) {
            LauncherSystemActions.accessibilityServiceInstance = null
        }
    }
}
