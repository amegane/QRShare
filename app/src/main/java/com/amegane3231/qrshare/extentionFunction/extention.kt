package com.amegane3231.qrshare.extentionFunction

import android.text.Spannable
import androidx.core.text.getSpans
import com.amegane3231.qrshare.hashTagSpan.HashTagSpan

fun CharSequence.isURL(): Boolean {
    val regex = "(http://|https://|www.)[\\w.\\-/:#?=&;%~+]+"
    val urls = regex.toRegex(RegexOption.IGNORE_CASE).findAll(this).map { it.value }
    return urls.toList().isNotEmpty()
}

fun Spannable.isEditing(): Boolean {
    val spans = this.getSpans<Any>()
    return spans.any { this.getSpanFlags(it) and Spannable.SPAN_COMPOSING == Spannable.SPAN_COMPOSING }
}

fun Spannable.removeHashTagSpans() {
    val hashTagSpans = this.getSpans<HashTagSpan>()
    hashTagSpans.forEach {
        this.removeSpan(it)
    }
}