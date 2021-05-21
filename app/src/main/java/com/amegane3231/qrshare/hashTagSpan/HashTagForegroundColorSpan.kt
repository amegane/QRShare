package com.amegane3231.qrshare.hashTagSpan

import android.text.style.ForegroundColorSpan
import androidx.annotation.ColorInt

class HashTagForegroundColorSpan(@ColorInt color: Int) : ForegroundColorSpan(color), HashTagSpan
