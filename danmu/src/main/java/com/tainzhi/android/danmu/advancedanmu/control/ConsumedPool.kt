package com.tainzhi.android.danmu.advancedanmu.control

import android.content.Context
import android.graphics.Canvas
import com.tainzhi.android.danmu.advancedanmu.Channel
import com.tainzhi.android.danmu.advancedanmu.Channel.Companion.MAX_COUNT_IN_SCREEN
import com.tainzhi.android.danmu.advancedanmu.Danmu
import com.tainzhi.android.danmu.advancedanmu.control.speed.ISpeedController
import com.tainzhi.android.danmu.advancedanmu.painter.DanmuPainter
import com.tainzhi.android.danmu.advancedanmu.painter.IDanmuPainter
import com.tainzhi.android.danmu.advancedanmu.painter.L2RPainter
import com.tainzhi.android.danmu.advancedanmu.painter.R2LPainter

/**
 * @author:      tainzhi
 * @mail:        qfq61@qq.com
 * @date:        2020/9/9 下午9:39
 * @description:
 **/

class ConsumedPool(val context: Context) {
    
    companion object {
        const val TAG = "Danmu.ConsumedPool"
    }
    
    val danmuPainterMap = hashMapOf<Int, IDanmuPainter>()
    
    @Volatile
    var mixedDanmuViewQueue = arrayListOf<Danmu>()
    
    var speedController: ISpeedController? = null
    
    private var channels: Array<Channel>? = null
    
    private var isDrawing: Boolean = false
    
    init {
        danmuPainterMap[Danmu.LEFT_TO_RIGHT] = L2RPainter()
        danmuPainterMap[Danmu.RIGHT_TO_LEFT] = R2LPainter()
        hide(false)
    }

    fun addPainter(danmuPainter: DanmuPainter, key: Int) {
        if (!danmuPainterMap.containsKey(key)) {
            danmuPainterMap[key] = danmuPainter
        }
    }

    fun hide(hide: Boolean) {
        danmuPainterMap.forEach { (_, u) ->
            u.hide = hide
        }
    }

    fun hideAll(hide: Boolean) {
        danmuPainterMap.forEach { (_, u) ->
            u.hideAll = hide
        }
    }

    fun release() {
        danmuPainterMap.clear()
        mixedDanmuViewQueue.clear()
    }

    fun isDrawnQueueEmpty(): Boolean =
        if (mixedDanmuViewQueue.isEmpty()) {
            isDrawing = false
            true
        } else {
            false
        }
    
    
    fun put(danmus: List<Danmu>) {
        mixedDanmuViewQueue.addAll(danmus)
    }
    
    fun draw(canvas: Canvas) {
        drawEveryElement(mixedDanmuViewQueue, canvas)
    }
    
    @Synchronized
    private fun drawEveryElement(danmus: ArrayList<Danmu>, canvas: Canvas) {
        isDrawing = true
        if (danmus.isNullOrEmpty()) return
        var index = 0
        while (index < if (danmus.size > MAX_COUNT_IN_SCREEN) MAX_COUNT_IN_SCREEN else danmus.size) {
            val danmu = danmus[index]
            if (danmu.isAlive) {
                val painter = getPainter(danmu)
                val channel = channels?.get(danmu.channelIndex) ?: return
                channel.dispatch(danmu)
                if (danmu.attached) {
                    performDraw(danmu, painter, canvas, channel)
                }
                index++
            } else {
                // 已经失效的Danmu, 移除ConsumedPool
                danmus.removeAt(index)
            }
        }
        isDrawing = false
    }
    
    private fun getPainter(danmu: Danmu): IDanmuPainter =
        danmuPainterMap[danmu.displayType]!!
    
    private fun performDraw(
        danmu: Danmu,
        danmuPainter: IDanmuPainter,
        canvas: Canvas,
        channel: Channel
    ) {
        danmuPainter.execute(canvas, danmu, channel)
    }
    
    fun divide(width: Int, height: Int) {
        channels = Channel.createChannels(width, height)
    }
}