package com.goi.foodtracker.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.goi.foodtracker.MainActivity
import com.goi.foodtracker.R
import com.goi.foodtracker.widget.WidgetActionReceiver.Companion.ACTION_WIDGET_LOG
import com.goi.foodtracker.widget.WidgetActionReceiver.Companion.EXTRA_FEELING_SCORE
import com.goi.foodtracker.widget.WidgetActionReceiver.Companion.EXTRA_FOOD_NAME

class FoodTrackerWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { id ->
            updateWidget(context, appWidgetManager, id)
        }
    }

    companion object {
        fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
            val views = RemoteViews(context.packageName, R.layout.widget_food_tracker)

            // "Add entry" button opens MainActivity
            val openAppIntent = PendingIntent.getActivity(
                context, 0,
                Intent(context, MainActivity::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_add_entry_btn, openAppIntent)

            // Preset food buttons
            val presetButtons = listOf(
                R.id.widget_btn_water to "Water",
                R.id.widget_btn_coffee to "Coffee",
                R.id.widget_btn_tea to "Tea",
                R.id.widget_btn_alcohol to "Alcohol",
                R.id.widget_btn_granola to "Granola",
                R.id.widget_btn_bread to "Bread/Toast",
                R.id.widget_btn_fruit to "Fruit",
                R.id.widget_btn_dairy to "Dairy"
            )
            presetButtons.forEach { (viewId, foodName) ->
                views.setOnClickPendingIntent(viewId, makeFoodPendingIntent(context, foodName, viewId))
            }

            // Feeling buttons
            val feelingButtons = listOf(
                R.id.widget_btn_feeling_1 to 1,
                R.id.widget_btn_feeling_2 to 2,
                R.id.widget_btn_feeling_3 to 3,
                R.id.widget_btn_feeling_4 to 4
            )
            feelingButtons.forEach { (viewId, score) ->
                views.setOnClickPendingIntent(viewId, makeFeelingPendingIntent(context, score, viewId))
            }

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun makeFoodPendingIntent(context: Context, foodName: String, requestCode: Int): PendingIntent {
            val intent = Intent(context, WidgetActionReceiver::class.java).apply {
                action = ACTION_WIDGET_LOG
                putExtra(EXTRA_FOOD_NAME, foodName)
            }
            return PendingIntent.getBroadcast(
                context, requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        private fun makeFeelingPendingIntent(context: Context, score: Int, requestCode: Int): PendingIntent {
            val intent = Intent(context, WidgetActionReceiver::class.java).apply {
                action = ACTION_WIDGET_LOG
                putExtra(EXTRA_FEELING_SCORE, score)
            }
            return PendingIntent.getBroadcast(
                context, requestCode + 500,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
