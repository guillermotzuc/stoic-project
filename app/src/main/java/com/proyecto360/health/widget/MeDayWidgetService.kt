package com.proyecto360.health.widget

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.proyecto360.health.HealthApplication
import com.proyecto360.health.R
import com.proyecto360.health.data.CommitmentTodo
import kotlinx.coroutines.runBlocking

class MeDayWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return MeDayRemoteViewsFactory(applicationContext)
    }
}

private class MeDayRemoteViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private var todos: List<CommitmentTodo> = emptyList()

    override fun onCreate() = Unit

    override fun onDataSetChanged() {
        // Called on a binder thread after notifyAppWidgetViewDataChanged.
        todos = try {
            val app = context.applicationContext as HealthApplication
            runBlocking { app.commitmentRepository.getTodayTodos() }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override fun onDestroy() {
        todos = emptyList()
    }

    override fun getCount(): Int = todos.size

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_todo_item)
        val todo = todos.getOrNull(position) ?: return views

        views.setTextViewText(R.id.todo_text, todo.text)
        views.setImageViewResource(
            R.id.todo_checkbox,
            if (todo.isDone) R.drawable.ic_widget_check_on else R.drawable.ic_widget_check_off
        )
        if (todo.isDone) {
            views.setInt(
                R.id.todo_text,
                "setPaintFlags",
                Paint.STRIKE_THRU_TEXT_FLAG or Paint.ANTI_ALIAS_FLAG
            )
            views.setTextColor(R.id.todo_text, Color.parseColor("#6B7C74"))
        } else {
            views.setInt(R.id.todo_text, "setPaintFlags", Paint.ANTI_ALIAS_FLAG)
            views.setTextColor(R.id.todo_text, Color.parseColor("#1A2A24"))
        }

        val fillIn = Intent().apply {
            putExtra(MeDayWidgetReceiver.EXTRA_TODO_ID, todo.id)
        }
        views.setOnClickFillInIntent(R.id.todo_row, fillIn)
        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long =
        todos.getOrNull(position)?.id ?: position.toLong()

    override fun hasStableIds(): Boolean = true
}
