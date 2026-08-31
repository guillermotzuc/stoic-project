package com.proyecto360.health.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import com.proyecto360.health.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object WidgetUpdater {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * Rebuilds widget chrome (phrase, buttons) and invalidates the Compromisos
     * ListView so [RemoteViewsService.RemoteViewsFactory.onDataSetChanged] runs.
     */
    suspend fun refreshNow(context: Context) {
        val appContext = context.applicationContext
        withContext(Dispatchers.Main) {
            val manager = AppWidgetManager.getInstance(appContext)
            val ids = MeDayWidgetReceiver.allWidgetIds(appContext)
            ids.forEach { id ->
                MeDayWidgetReceiver.updateAppWidget(appContext, manager, id)
            }
        }
    }

    /** Only reloads the Compromisos collection adapter (fast path after toggles). */
    fun notifyListDataChanged(context: Context) {
        val appContext = context.applicationContext
        val manager = AppWidgetManager.getInstance(appContext)
        val ids = MeDayWidgetReceiver.allWidgetIds(appContext)
        if (ids.isNotEmpty()) {
            manager.notifyAppWidgetViewDataChanged(ids, R.id.widget_todo_list)
        }
    }

    fun refresh(context: Context) {
        val appContext = context.applicationContext
        scope.launch {
            refreshNow(appContext)
        }
    }
}
