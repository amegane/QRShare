package com.amegane3231.qrshare.hashTagSpan

import android.text.style.ForegroundColorSpan
import androidx.annotation.ColorInt

class HashTagForegroundColorSpan constructor(@ColorInt color: Int) :
    ForegroundColorSpan(color), HashTagSpan
