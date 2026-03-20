package com.goi.foodtracker.widget

import android.appwidget.AppWidgetManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.goi.foodtracker.data.repository.LogRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WidgetActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var logRepository: LogRepository

    override fun onReceive(context: Context, intent: Intent) {
        val foodName = intent.getStringExtra(EXTRA_FOOD_NAME)
        val feelingScore = intent.getIntExtra(EXTRA_FEELING_SCORE, -1).takeIf { it > 0 }

        if (foodName != null || feelingScore != null) {
            CoroutineScope(Dispatchers.IO).launch {
                logRepository.saveEntry(
                    foods = if (foodName != null) listOf(foodName) else emptyList(),
                    feelingScore = feelingScore
                )
                // Refresh widget
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val ids = appWidgetManager.getAppWidgetIds(
                    ComponentName(context, FoodTrackerWidget::class.java)
                )
                val updateIntent = Intent(context, FoodTrackerWidget::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                }
                context.sendBroadcast(updateIntent)
            }
        }
    }

    companion object {
        const val ACTION_WIDGET_LOG = "com.goi.foodtracker.ACTION_WIDGET_LOG"
        const val EXTRA_FOOD_NAME = "extra_food_name"
        const val EXTRA_FEELING_SCORE = "extra_feeling_score"
    }
}
