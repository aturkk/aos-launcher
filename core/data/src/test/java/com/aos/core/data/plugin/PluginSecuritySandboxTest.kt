package com.aos.core.data.plugin

import com.aos.core.domain.model.AosPlugin
import com.aos.core.domain.model.PluginManifest
import com.aos.core.domain.model.PluginPermission
import com.aos.core.domain.model.PluginType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PluginSecuritySandboxTest {

    private lateinit var sandbox: PluginSecuritySandbox

    @Before
    fun setUp() {
        sandbox = PluginSecuritySandbox()
    }

    private class TestPlugin(
        override val manifest: PluginManifest,
        override val isEnabled: Boolean = true
    ) : AosPlugin {
        override fun initialize(): Boolean = true
        override fun terminate() {}
    }

    @Test
    fun `checkPermission returns true when permission is declared`() {
        val plugin = TestPlugin(
            manifest = PluginManifest(
                id = "test.plugin",
                name = "Test Plugin",
                versionName = "1.0",
                author = "Author",
                description = "Desc",
                type = PluginType.SearchProvider,
                requiredPermissions = listOf(PluginPermission.ProvideSearch)
            )
        )

        assertTrue(sandbox.checkPermission(plugin, PluginPermission.ProvideSearch))
        assertFalse(sandbox.checkPermission(plugin, PluginPermission.TriggerActions))
    }

    @Test(expected = SecurityException::class)
    fun `enforcePermission throws SecurityException when permission is missing`() {
        val plugin = TestPlugin(
            manifest = PluginManifest(
                id = "test.plugin",
                name = "Unprivileged Plugin",
                versionName = "1.0",
                author = "Author",
                description = "Desc",
                type = PluginType.General,
                requiredPermissions = emptyList()
            )
        )

        sandbox.enforcePermission(plugin, PluginPermission.TriggerActions)
    }

    @Test
    fun `enforcePermission does not throw when permission is granted`() {
        val plugin = TestPlugin(
            manifest = PluginManifest(
                id = "test.plugin",
                name = "Privileged Plugin",
                versionName = "1.0",
                author = "Author",
                description = "Desc",
                type = PluginType.Widget,
                requiredPermissions = listOf(PluginPermission.ProvideWidgets)
            )
        )

        sandbox.enforcePermission(plugin, PluginPermission.ProvideWidgets)
    }
}
