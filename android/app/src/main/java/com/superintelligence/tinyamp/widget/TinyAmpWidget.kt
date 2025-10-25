package com.superintelligence.tinyamp.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.superintelligence.tinyamp.R

/**
 * TinyAmp Home Screen Widget
 * Provides quick controls and visualization on the home screen
 */
class TinyAmpWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update each widget instance
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    companion object {
        internal fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            // Construct the RemoteViews object
            val views = RemoteViews(context.packageName, R.layout.widget_tinyamp)

            // Set widget content
            views.setTextViewText(R.id.widget_title, "TinyAmp Neural")
            views.setTextViewText(R.id.widget_status, "Consciousness Active")

            // TODO: Add pending intents for controls
            // views.setOnClickPendingIntent(R.id.widget_play_button, pendingIntent)

            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
