package com.aos.core.data.plugin

import com.aos.core.domain.model.AosPlugin
import com.aos.core.domain.model.PluginPermission
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PluginSecuritySandbox @Inject constructor() {

    fun checkPermission(plugin: AosPlugin, permission: PluginPermission): Boolean {
        return plugin.manifest.requiredPermissions.contains(permission)
    }

    fun enforcePermission(plugin: AosPlugin, permission: PluginPermission) {
        if (!checkPermission(plugin, permission)) {
            throw SecurityException("Güvenlik İhlali: '${plugin.manifest.name}' eklentisi '${permission.name}' iznine sahip değil.")
        }
    }
}
