package com.doro.music.ui.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Music Player Widget 的 Receiver
 *
 * 负责接收系统广播并管理 Widget 的生命周期。
 */
class MusicPlayerWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = MusicPlayerWidget()
}
