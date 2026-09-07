package com.aos.feature.home.widget

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.Context

class LauncherWidgetHost(context: Context) : AppWidgetHost(context, APPWIDGET_HOST_ID) {

    companion object {
        const val APPWIDGET_HOST_ID = 1024
    }

    override fun onCreateView(
        context: Context,
        appWidgetId: Int,
        appWidget: AppWidgetProviderInfo?
    ): AppWidgetHostView {
        return super.onCreateView(context, appWidgetId, appWidget).apply {
            setAppWidget(appWidgetId, appWidget)
        }
    }
}
