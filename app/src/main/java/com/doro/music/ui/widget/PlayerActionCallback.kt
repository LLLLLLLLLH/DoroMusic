package com.doro.music.ui.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.action.ActionCallback
import com.doro.music.player.PlayActionDispatcher
import com.doro.music.player.PlayerConnector
import com.doro.music.player.model.PlayAction
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

/**
 * Widget 播放控制回调
 *
 * 接收用户的播放控制操作（播放/暂停、上一首、下一首），
 * 通过 Koin 获取 PlayActionDispatcher 分发到播放引擎。
 */
class PlayerActionCallback : ActionCallback, KoinComponent {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: androidx.glance.action.ActionParameters
    ) {
        val action = parameters[ACTION_KEY] ?: return

        val dispatcher: PlayActionDispatcher = get()
        val connector: PlayerConnector = get()

        // 确保播放服务已连接
        connector.connect()

        when (action) {
            ACTION_TOGGLE_PLAY -> dispatcher.dispatch(PlayAction.TogglePlay)
            ACTION_NEXT -> dispatcher.dispatch(PlayAction.Next)
            ACTION_PREV -> dispatcher.dispatch(PlayAction.Prev)
        }

        // 刷新 Widget UI
        MusicPlayerWidget().update(context, glanceId)
    }
}
