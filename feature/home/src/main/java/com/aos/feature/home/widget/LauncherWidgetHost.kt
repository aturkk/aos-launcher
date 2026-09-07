package com.aos.feature.home.widget

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.view.GestureDetector
import android.view.MotionEvent

class LauncherAppWidgetHostView(context: Context) : AppWidgetHostView(context) {
    var onLongClickAction: (() -> Unit)? = null

    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onLongPress(e: MotionEvent) {
            onLongClickAction?.invoke()
        }
    })

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(ev)
        return super.onInterceptTouchEvent(ev)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }
}

class LauncherWidgetHost(context: Context) : AppWidgetHost(context, APPWIDGET_HOST_ID) {

    companion object {
        const val APPWIDGET_HOST_ID = 1024
    }

    override fun onCreateView(
        context: Context,
        appWidgetId: Int,
        appWidget: AppWidgetProviderInfo?
    ): AppWidgetHostView {
        return LauncherAppWidgetHostView(context).apply {
            setAppWidget(appWidgetId, appWidget)
        }
    }
}
