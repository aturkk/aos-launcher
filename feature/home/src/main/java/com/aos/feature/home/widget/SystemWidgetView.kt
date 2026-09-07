package com.aos.feature.home.widget

import android.appwidget.AppWidgetManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun SystemWidgetView(
    appWidgetId: Int,
    widgetHost: LauncherWidgetHost,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val appWidgetManager = remember { AppWidgetManager.getInstance(context) }
    val appWidgetInfo = remember(appWidgetId) { appWidgetManager.getAppWidgetInfo(appWidgetId) }

    if (appWidgetInfo != null) {
        AndroidView(
            factory = {
                widgetHost.createView(context, appWidgetId, appWidgetInfo)
            },
            modifier = modifier.fillMaxSize()
        )
    } else {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Widget Bulunamadı", color = Color.Gray)
        }
    }
}
