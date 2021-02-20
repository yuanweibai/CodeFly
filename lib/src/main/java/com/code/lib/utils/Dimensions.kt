package com.code.lib.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.os.Build
import android.text.Layout
import android.text.TextPaint
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager
import java.util.*

object Dimensions {

    private val DP_TO_PX_CACHE: MutableMap<Float, Float> = HashMap(50)

    private var SCREEN_WIDTH_PX_CACHE = -1
    private var SCREEN_HEIGHT_PX_CACHE = -1
    private var SCREEN_WIDTH_DP_CACHE = -1
    private var SCREEN_HEIGHT_DP_CACHE = -1
    private var SCREEN_ONE_DP_TO_PX = -1f
    private var SCREEN_ONE_PX_TO_DP = -1f
    private var STATUS_BAR_HEIGHT = -1
    private var SCREEN_DENSITY_DPI = -1

    private var textPaint: TextPaint? = null

    fun dp2px(dp: Float): Float {
        if (DP_TO_PX_CACHE.containsKey(dp)) {
            return DP_TO_PX_CACHE[dp]!!
        }
        if (SCREEN_ONE_DP_TO_PX < 0) {
            SCREEN_ONE_DP_TO_PX = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                1f,
                Resources.getSystem().displayMetrics
            )
        }
        val ret = SCREEN_ONE_DP_TO_PX * dp
        DP_TO_PX_CACHE[dp] = ret
        return ret
    }

    fun px2dp(px: Float): Float {
        if (SCREEN_ONE_PX_TO_DP < 0) {
            val resources = Resources.getSystem()
            val metrics = resources.displayMetrics
            SCREEN_ONE_PX_TO_DP = 1 / (metrics.densityDpi / 160f)
        }
        return SCREEN_ONE_PX_TO_DP * px
    }

    fun getDensityDpi(): Int {
        if (SCREEN_DENSITY_DPI < 0) {
            val resources = Resources.getSystem()
            val metrics = resources.displayMetrics
            SCREEN_DENSITY_DPI = metrics.densityDpi
        }
        return SCREEN_DENSITY_DPI
    }

    fun px2sp(pxValue: Float): Int {
        val fontScale = Resources.getSystem().displayMetrics.scaledDensity
        return (pxValue / fontScale + 0.5f).toInt()
    }

    fun sp2px(spValue: Float): Int {
        val fontScale = Resources.getSystem().displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }

    fun getScreenWidthPx(): Int {
        if (SCREEN_WIDTH_PX_CACHE < 0) {
            val metrics = Resources.getSystem().displayMetrics
            SCREEN_WIDTH_PX_CACHE = metrics.widthPixels
        }
        return SCREEN_WIDTH_PX_CACHE
    }

    fun getScreenHeightPx(): Int {
        if (SCREEN_HEIGHT_PX_CACHE < 0) {
            val metrics = Resources.getSystem().displayMetrics
            SCREEN_HEIGHT_PX_CACHE = metrics.heightPixels
        }
        return SCREEN_HEIGHT_PX_CACHE
    }

    fun getScreenDimensionsInDp(context: Context): Point? {
        if (SCREEN_WIDTH_DP_CACHE < 0 || SCREEN_HEIGHT_DP_CACHE < 0) {
            // APIs prior to v13 gave the screen dimensions in pixels. We convert them to DIPs before returning them.
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val displayMetrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(displayMetrics)
            SCREEN_WIDTH_DP_CACHE = px2dp(displayMetrics.widthPixels.toFloat()).toInt()
            SCREEN_HEIGHT_DP_CACHE = px2dp(displayMetrics.heightPixels.toFloat()).toInt()
        }
        return Point(SCREEN_WIDTH_DP_CACHE, SCREEN_HEIGHT_DP_CACHE)
    }

    /**
     * 此方法有缺陷，当text中有汉子和字符混合使用时，计算的宽不准确；
     *
     * @param text
     * @param fontSize
     * @return
     */
    fun getTextWidth(text: String?, fontSize: Float): Float {
        if (textPaint == null) {
            textPaint = TextPaint()
        }
        textPaint!!.textSize = dp2px(fontSize).toFloat()
        // 此方法也可以
//        float textWidth = textPaint.measureText(text);
        return Layout.getDesiredWidth(text, textPaint)
    }

    /**
     * 此方法可以弥补[.getTextWidth]的缺陷
     *
     * @param text
     * @param textPaint 可以公共[TextView.getPaint]来获取
     * @return
     */
    fun getTextWidth(text: String?, textPaint: TextPaint): Float {
//        return Layout.getDesiredWidth(text,textPaint); //此方法也行
        return textPaint.measureText(text)
    }


    fun getTextHeight(fontSize: Float): Int {
        val fontScale = Resources.getSystem().displayMetrics.scaledDensity
        return (fontSize * fontScale + 0.5f).toInt()
    }

    fun getStatusBarHeight(): Int {
        if (STATUS_BAR_HEIGHT == -1) {
            val resources = Resources.getSystem()
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                STATUS_BAR_HEIGHT = resources.getDimensionPixelSize(resourceId)
            }
        }
        return STATUS_BAR_HEIGHT
    }

    /**
     * 获取屏幕尺寸，单位为英寸
     * 计算屏幕尺寸应该使用精确密度：xdpi ydpi来计算
     * 使用归一化密度：densitydpi是错误的，它是固定值，
     * 120 160 240 320 480,根据dp计算像素才使用它
     *
     * @param context
     * @return
     */
    fun getScreenSizeInInch(context: Activity): Double {
        return if (Build.VERSION.SDK_INT >= 17) {
            val point = Point()
            context.windowManager.defaultDisplay.getRealSize(point)
            val dm = context.resources.displayMetrics
            val x = Math.pow(point.x / dm.xdpi.toDouble(), 2.0)
            val y = Math.pow(point.y / dm.ydpi.toDouble(), 2.0)
            Math.sqrt(x + y)
        } else {
            val dm = DisplayMetrics()
            context.windowManager.defaultDisplay.getMetrics(dm)
            val heightInInch = dm.heightPixels.toDouble() / dm.densityDpi.toDouble()
            val widthInInch = dm.widthPixels.toDouble() / dm.densityDpi.toDouble()
            Math.sqrt(
                heightInInch * heightInInch
                        + widthInInch * widthInInch
            )
        }
    }

    private var nonCompatDensity = 0f

    fun setCustomDensity(activity: Activity, application: Application) {
        val metrics = application.resources.displayMetrics
        if (nonCompatDensity == 0f) {
            nonCompatDensity = metrics.density
        }
        val targetDensity = metrics.widthPixels / 360f
        val targetDensityDpi = (160 * targetDensity).toInt()
        metrics.density = targetDensity
        metrics.scaledDensity = targetDensity
        metrics.densityDpi = targetDensityDpi
        val activityMetrics = activity.resources.displayMetrics
        activityMetrics.density = targetDensity
        activityMetrics.scaledDensity = targetDensity
        activityMetrics.densityDpi = targetDensityDpi
    }
}