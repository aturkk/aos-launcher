package com.aos.core.common.util

import android.accessibilityservice.AccessibilityService
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.Toast

object LauncherSystemActions {

    var accessibilityServiceInstance: AccessibilityService? = null

    @SuppressLint("WrongConstant")
    fun expandNotificationShade(context: Context) {
        val service = accessibilityServiceInstance
        if (service != null) {
            service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_NOTIFICATIONS)
            return
        }

        try {
            val statusBarService = context.getSystemService("statusbar")
            val statusBarManagerClass = Class.forName("android.app.StatusBarManager")
            val expandMethod = statusBarManagerClass.getMethod("expandNotificationsPanel")
            expandMethod.invoke(statusBarService)
        } catch (e: Exception) {
            // Fallback: Accessibility service guidance
            Toast.makeText(context, "Bildirimleri açmak için Erişilebilirlik izni gerekebilir", Toast.LENGTH_SHORT).show()
        }
    }

    fun lockDevice(context: Context) {
        val service = accessibilityServiceInstance
        if (service != null) {
            val success = service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_LOCK_SCREEN)
            if (!success) {
                Toast.makeText(context, "Ekran kilitlenemedi", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Çift dokunmayla kilitlemek için AOS Erişilebilirlik Servisi'ni açın", Toast.LENGTH_LONG).show()
            try {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    fun openDialer(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Telefon uygulaması bulunamadı", Toast.LENGTH_SHORT).show()
        }
    }

    fun openBrowser(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Tarayıcı bulunamadı", Toast.LENGTH_SHORT).show()
        }
    }

    fun openMessaging(context: Context) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_MESSAGING)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            try {
                val fallback = Intent(Intent.ACTION_VIEW).apply {
                    type = "vnd.android-dir/mms-sms"
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(fallback)
            } catch (_: Exception) {
                Toast.makeText(context, "Mesajlar uygulaması bulunamadı", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun openCamera(context: Context) {
        try {
            val intent = Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Kamera bulunamadı", Toast.LENGTH_SHORT).show()
        }
    }
}
