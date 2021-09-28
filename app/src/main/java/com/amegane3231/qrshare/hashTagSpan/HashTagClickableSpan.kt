package com.amegane3231.qrshare.hashTagSpan

import android.text.style.ClickableSpan
import android.view.View

class HashTagClickableSpan constructor(
    private val callback: (content: CharSequence) -> Unit,
    private val content: CharSequence
) : ClickableSpan(), HashTagSpan {
    override fun onClick(widget: View) {
        callback(content)
    }
}