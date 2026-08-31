package com.proyecto360.health.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.proyecto360.health.HealthApplication
import com.proyecto360.health.MainActivity
import com.proyecto360.health.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Classic collection widget: ListView + RemoteViewsFactory.
 * List data is refreshed with [AppWidgetManager.notifyAppWidgetViewDataChanged].
 */
class MeDayWidgetReceiver : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_TOGGLE_TODO -> {
                val todoId = intent.getLongExtra(EXTRA_TODO_ID, -1L)
                if (todoId <= 0L) return
                val pendingResult = goAsync()
                scope.launch {
                    try {
                        val app = context.applicationContext as HealthApplication
                        // Persist without full widget rebuild; invalidate ListView only.
                        app.commitmentRepository.toggleTodo(todoId, refreshWidget = false)
                        WidgetUpdater.notifyListDataChanged(context)
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
            else -> super.onReceive(context, intent)
        }
    }

    companion object {
        const val ACTION_TOGGLE_TODO = "com.proyecto360.health.widget.ACTION_TOGGLE_TODO"
        const val EXTRA_TODO_ID = "com.proyecto360.health.widget.EXTRA_TODO_ID"

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val app = context.applicationContext as HealthApplication
            val phrase = app.phraseRepository.phraseOfTheDay().phrase

            val views = RemoteViews(context.packageName, R.layout.widget_me_day).apply {
                setTextViewText(R.id.widget_phrase_text, "“$phrase”")

                val serviceIntent = Intent(context, MeDayWidgetService::class.java).apply {
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    // Unique URI so each widget instance gets its own adapter.
                    data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
                }
                setRemoteAdapter(R.id.widget_todo_list, serviceIntent)
                setEmptyView(R.id.widget_todo_list, R.id.widget_todo_empty)

                val toggleTemplate = Intent(context, MeDayWidgetReceiver::class.java).apply {
                    action = ACTION_TOGGLE_TODO
                }
                setPendingIntentTemplate(
                    R.id.widget_todo_list,
                    PendingIntent.getBroadcast(
                        context,
                        appWidgetId,
                        toggleTemplate,
                        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                    )
                )

                setOnClickPendingIntent(
                    R.id.widget_add_todo,
                    activityPendingIntent(
                        context,
                        appWidgetId,
                        Intent(context, QuickAddTodoActivity::class.java),
                        requestCodeOffset = 10
                    )
                )
                setOnClickPendingIntent(
                    R.id.widget_btn_post,
                    activityPendingIntent(
                        context,
                        appWidgetId,
                        Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                            putExtra(WidgetIntents.EXTRA_DESTINATION, WidgetIntents.DEST_POST)
                        },
                        requestCodeOffset = 20
                    )
                )
                setOnClickPendingIntent(
                    R.id.widget_btn_exam,
                    activityPendingIntent(
                        context,
                        appWidgetId,
                        Intent(context, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                                Intent.FLAG_ACTIVITY_SINGLE_TOP
                            putExtra(WidgetIntents.EXTRA_DESTINATION, WidgetIntents.DEST_EXAM)
                        },
                        requestCodeOffset = 30
                    )
                )
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_todo_list)
        }

        private fun activityPendingIntent(
            context: Context,
            appWidgetId: Int,
            intent: Intent,
            requestCodeOffset: Int
        ): PendingIntent {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            return PendingIntent.getActivity(
                context,
                appWidgetId + requestCodeOffset,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        fun allWidgetIds(context: Context): IntArray {
            val manager = AppWidgetManager.getInstance(context)
            return manager.getAppWidgetIds(ComponentName(context, MeDayWidgetReceiver::class.java))
        }
    }
}
