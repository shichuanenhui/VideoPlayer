package com.tainzhi.android.danmu.advancedanmu

import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.Drawable
import com.tainzhi.android.danmu.advancedanmu.view.OnDanmuTouchListener
import com.tainzhi.android.danmu.dp
import com.tainzhi.android.danmu.sp

/**
 * @author:      tainzhi
 * @mail:        qfq61@qq.com
 * @date:        2020/9/10 15:10
 * @description:
 **/

class Danmu : OnDanmuTouchListener {

    var paddingLeft = 0
    var paddingRight = 0
    var marginLeft = 30.dp()
    var marginRight = 0

    // user avatar
    var avatar: Bitmap? = null
    var avatarWidth = 30.dp()
    var avatarHeight = 30.dp()

    // 用户图像描边(默认白色描边)
    var avatarStrokes = true

    // 用户等级标签
    var levelBitmap: Bitmap? = null
    var levelBitmapWidth = 36.dp()
    var levelBitmapHeight = 16.dp()
    var levelBitmapMarginLeft = 5.dp()
    var levelBitmapMarginRight = 0

    // 用户等级标签文本
    var levelText: CharSequence? = null
    var levelTextSize = 14f.sp()
    var levelTextColor = 0

    // 弹幕文本 = 用户name + 弹幕文本
    var text: CharSequence? = null
    var textSize = 12f.sp()
    var textColor = 0
    var textMarginLeft = 5.dp()
    var textMeasuredWith = 0 // 通过DanmuDispatcher.measure后获取
    var textMeasuredHeight = 0 // 通过DanmuDispatcher.measure后获取

    var textBackground: Drawable? = null
    var textBackgroundMarginLeft = 20.dp()
    var textBackgroundPadding = Rect(15.dp(), 3.dp(), 15.dp(), 3.dp())

    var position = Point(0, 0)

    var width = 0
    var height = 0
    var channelIndex = 0
    var isMoving = true
    var isAlive = true
    var displayType = 0
    var attached = false
    var priority = PRIORITY_NORMAL
    var isMeasured = false

    var speed = 0f

    var enableTouch = true

    /**
     * 点击后, 弹幕停止/继续 滚动
     */
    var touchCallback: ((danmu: Danmu) -> Unit)? = { danmu -> danmu.isMoving = !danmu.isMoving }

    /**
     * 只有显示在可见区域内才能响应onTouch
     */
    override fun onTouch(x: Float, y: Float): Boolean {
        return x >= position.x && x <= position.x + width && y >= position.y && y <= position.y + height
    }

    companion object {

        const val RIGHT_TO_LEFT = 1
        const val LEFT_TO_RIGHT = 2


        const val PRIORITY_SYSTEM = 100
        const val PRIORITY_NORMAL = 50
    }
}