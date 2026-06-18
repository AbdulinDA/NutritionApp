package com.abdulin.nutritionapp.presentation.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.abdulin.nutritionapp.R
import com.abdulin.nutritionapp.presentation.MainActivity

class NutritionWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: androidx.glance.GlanceId) {
        val openDiaryLabel = context.getString(R.string.widget_open_diary)
        val kcalLabel = context.getString(R.string.unit_kcal)
        val mlLabel = context.getString(R.string.unit_ml)

        provideContent {
            GlanceTheme {
                NutritionWidgetContent(
                    openDiaryLabel = openDiaryLabel,
                    kcalLabel = kcalLabel,
                    mlLabel = mlLabel
                )
            }
        }
    }

    @Composable
    private fun NutritionWidgetContent(
        openDiaryLabel: String,
        kcalLabel: String,
        mlLabel: String
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surface)
                .padding(12.dp)
                .appWidgetBackground(),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Text(
                text = "Nutrition",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.primary
                )
            )
            Spacer(modifier = GlanceModifier.height(8.dp))

            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Horizontal.CenterHorizontally
            ) {
                Column(horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
                    Text(text = "1540", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold))
                    Text(text = kcalLabel, style = TextStyle(fontSize = 10.sp))
                }
                Spacer(modifier = GlanceModifier.width(16.dp))
                Column(horizontalAlignment = Alignment.Horizontal.CenterHorizontally) {
                    Text(text = "1250", style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold))
                    Text(text = mlLabel, style = TextStyle(fontSize = 10.sp))
                }
            }

            Spacer(modifier = GlanceModifier.height(12.dp))

            androidx.glance.Button(
                text = openDiaryLabel,
                onClick = actionStartActivity<MainActivity>()
            )
        }
    }
}

class MainWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = NutritionWidget()
}
